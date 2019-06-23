package orchi.HHCloud.Api.User;

import orchi.HHCloud.Api.API;
import orchi.HHCloud.Api.ServiceTaskAPIImpl;
import orchi.HHCloud.Api.annotations.Operation;
import orchi.HHCloud.Api.annotations.SessionRequired;
import orchi.HHCloud.ParseParamsMultiPart2;
import orchi.HHCloud.Start;
import orchi.HHCloud.auth.Exceptions.TokenException;
import orchi.HHCloud.cipher.CipherProvider;
import orchi.HHCloud.mail.Exceptions.SendEmailException;
import orchi.HHCloud.store.ContentSummary;
import orchi.HHCloud.store.ContextStore;
import orchi.HHCloud.store.StoreProvider;
import orchi.HHCloud.user.*;
import orchi.HHCloud.user.Exceptions.*;
import orchi.HHCloud.user.search.SearchUserProvider;
import orchi.HHCloud.user.search.UsersFound;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.mortbay.log.Log;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Api para gestion de usuario
 *
 * @author Colmenares David
 */
public class Users extends API {

    private static final long serialVersionUID = 3632921692211341012L;
    public static String apiName = "/user";
    //private static String ACCESS_CONTROL_ALLOW_ORIGIN = Start.conf.getString("api.headers.aclo");
    private static StoreProvider sp;
    private static UserProvider up;
    private static ObjectMapper om;
    private static CipherProvider cipher = Start.getCipherManager().getCipherProvider();
    private ThreadPoolExecutor executor;
    private static SearchUserProvider us;

    @Operation(name = "accountstatus")
    @SessionRequired
    public static void AccountStatusOperation(AsyncContext ctx, JSONObject jsonArgs) throws JsonGenerationException, JsonMappingException, IOException {
        HttpServletResponse resp = (HttpServletResponse) ctx.getResponse();
        HttpServletRequest req = (HttpServletRequest) ctx.getRequest();
        HttpSession session = req.getSession(false);
        String idInSession = (String) session.getAttribute("uid");
        boolean hasError = false;
        AccoutStatePayload payload = new AccoutStatePayload();
        AccountStateJsonResponse response = new AccountStateJsonResponse();
        DataUser user = null;

        try {
            user = (DataUser) up.getUserById(idInSession);
        } catch (UserNotExistException e) {
            hasError = true;
            response.setStatus("error");
            response.setError("user_no_exists");
            response.setMsg(e.getMessage());
        } catch (UserException e) {
            hasError = true;
            response.setStatus("error");
            response.setError("user_exception");
            response.setMsg(e.getMessage());
            // e.printStackTrace();
        }

        if (!hasError) {
            payload.setUser(user);
            payload.setStorageSummary(sp.getContentSummary(user, Paths.get("/")));
            response.setPayload(payload);
        }

        if (!hasError) {
            response.setStatus("ok");
            resp.getWriter().println(om.writeValueAsString(response));
        } else {
            resp.getWriter().println(om.writeValueAsString(response));
        }
        ctx.complete();

    }

    @Operation(name = "changepasswordbyrecover")
    public static void changePasswordByRecover(AsyncContext ctx, JSONObject jsonArgs) throws JsonGenerationException, JsonMappingException, IOException {
        HttpServletResponse resp = (HttpServletResponse) ctx.getResponse();
        //HttpServletRequest req = (HttpServletRequest) ctx.getRequest();

        boolean hasError = false;
        UserValidator validator = up.getValidator();
        JsonResponse response = new JsonResponse();
        String email = jsonArgs.has("email") ? jsonArgs.getString("email") : null;
        String token = jsonArgs.has("token") ? jsonArgs.getString("token") : null;
        String npassword = jsonArgs.has("password") ? jsonArgs.getString("password") : null;
        User user = null;

        if (email == null) {
            hasError = true;
            response.setStatus("error");
            response.setError("email_missing");
            response.setMsg("Debe suministrar su email");
        }
        try {
            User tmpUser = new DataUser();
            tmpUser.setPassword(npassword);
            validator.validatePassword(tmpUser);
        } catch (PasswordValidationException e) {
            hasError = true;
            response.setStatus("error");
            response.setError("password_validation");
            response.setMsg(e.getMessage());
            e.printStackTrace();
        } catch (ValidationException e1) {
            hasError = true;
            response.setStatus("error");
            response.setError("validation_exception");
            response.setMsg(e1.getMessage());
            e1.printStackTrace();
        }
        if (!hasError) {
            try {
                user = up.getUserByEmail(email);
            } catch (UserNotExistException e) {
                hasError = true;
                response.setStatus("error");
                response.setError("user_no_exists");
                response.setMsg(e.getMessage());
            } catch (UserException e) {
                hasError = true;
                response.setStatus("error");
                response.setError("user_exception");
                response.setMsg(e.getMessage());
                // e.printStackTrace();
            }
        }

        if (!hasError) {
            if (token != null) {
                try {
                    Start.getAuthProvider().revokeTokenToRecoveryPassword(token, user);
                } catch (TokenException e) {
                    hasError = true;
                    response.setStatus("error");
                    response.setError("token_exception");
                    response.setMsg(e.getMessage());
                    e.printStackTrace();
                }
            } else {
                hasError = true;
                response.setStatus("error");
                response.setError("token_missing");
                response.setMsg("Debe suministrar el codigo enviado a su correo para poder continuar el cambio  de contraseña.");
            }
        }


        if (!hasError) {
            jsonArgs.put("id", user.getId());
            jsonArgs.put("currentPassword", cipher.decrypt(user.getPassword()));
            changePasswordOperation(ctx, jsonArgs);
        } else {
            resp.getWriter().println(om.writeValueAsString(response));
            ctx.complete();
        }

    }

    @Operation(name = "sendverifyemail")
    @SessionRequired
    public static void sendVerifyEmailOperation(AsyncContext ctx, JSONObject jsonArgs) throws JsonGenerationException, JsonMappingException, IOException {
        HttpServletResponse resp = (HttpServletResponse) ctx.getResponse();
        HttpServletRequest req = (HttpServletRequest) ctx.getRequest();
        HttpSession session = req.getSession(false);
        String idInSession = (String) session.getAttribute("uid");

        boolean hasError = false;
        JsonResponse response = new JsonResponse();
        String email = jsonArgs.has("email") ? jsonArgs.getString("email") : null;
        DataUser user = null;

        if (email == null) {
            hasError = true;
            response.setStatus("error");
            response.setError("email_missing");
            response.setMsg("Debe suministrar un email");
        }

        if (!hasError) {
            try {
                user = (DataUser) up.getUserById(idInSession);
            } catch (UserNotExistException e) {
                hasError = true;
                response.setStatus("error");
                response.setError("user_no_exists");
                response.setMsg(e.getMessage());
            } catch (UserException e) {
                hasError = true;
                response.setStatus("error");
                response.setError("user_exception");
                response.setMsg(e.getMessage());
                // e.printStackTrace();
            }
        }

        if (!hasError) {
            if (user.isEmailVerified()) {
                hasError = true;
                response.setStatus("error");
                response.setError("account_is_verified");
                response.setMsg("Esta cuenta ya ha sido verificada");
            }
        }

        if (!hasError) {
            if (!user.getEmail().equals(email)) {
                hasError = true;
                response.setStatus("error");
                response.setError("violation_security");
                response.setMsg("El email suministrado no concuerda con el email de tu session actual.");
            } else {
                try {
                    up.sendVerifyEmail(user);
                } catch (UserException e) {
                    hasError = true;
                    response.setStatus("error");
                    response.setError("user_exception");
                    response.setMsg(e.getMessage());
                } catch (SendEmailException e) {
                    hasError = true;
                    response.setStatus("error");
                    response.setError("email_exception");
                    response.setMsg(e.getMessage());
                }
            }
        }

        if (!hasError) {
            response.setStatus("ok");
            response.setMsg("Se envio a tu correo un enlace para terminar con la verificacion de tu cuenta.");
            resp.getWriter().println(om.writeValueAsString(response));
        } else {
            resp.getWriter().println(om.writeValueAsString(response));
        }
        ctx.complete();

    }

    @Operation(name = "sendrecoveryemail")
    //@SessionRequired
    public static void sendRecoveryEmailOperation(AsyncContext ctx, JSONObject jsonArgs) throws JsonGenerationException, JsonMappingException, IOException {
        HttpServletResponse resp = (HttpServletResponse) ctx.getResponse();
        //HttpServletRequest req = (HttpServletRequest) ctx.getRequest();

        boolean hasError = false;
        JsonResponse response = new JsonResponse();
        String email = jsonArgs.has("email") ? jsonArgs.getString("email") : null;
        User user = null;
        if (email == null) {
            hasError = true;
            response.setStatus("error");
            response.setError("email_missing");
            response.setMsg("Debe suministrar su email");
        }

        if (!hasError) {
            try {
                user = up.getUserByEmail(email);
            } catch (UserNotExistException e) {
                hasError = true;
                response.setStatus("error");
                response.setError("user_no_exists");
                response.setMsg(e.getMessage());
            } catch (UserException e) {
                hasError = true;
                response.setStatus("error");
                response.setError("user_exception");
                response.setMsg(e.getMessage());
                // e.printStackTrace();
            }
        }

        if (!hasError) {
            try {
                up.sendRecoveryPasswordEmail(user);
            } catch (UserException e) {
                hasError = true;
                response.setStatus("error");
                response.setError("user_exception");
                response.setMsg(e.getMessage());

            } catch (SendEmailException e) {
                hasError = true;
                response.setStatus("error");
                response.setError("email_exception");
                response.setMsg("Error al enviar email de recuperacion: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (!hasError) {
            response.setStatus("ok");
            response.setMsg("Se envio un codigo a tu cuenta de email para poder cambiar tu contraseña.");
            resp.getWriter().println(om.writeValueAsString(response));
        } else {
            resp.getWriter().println(om.writeValueAsString(response));
        }
        ctx.complete();
    }

    @Operation(name = "get")
    @SessionRequired
    public static void getOperation(AsyncContext ctx, JSONObject jsonArgs) throws JsonGenerationException, JsonMappingException, IOException {
        HttpServletResponse resp = (HttpServletResponse) ctx.getResponse();

        HttpServletRequest req = (HttpServletRequest) ctx.getRequest();

        String by = jsonArgs.has("by") ? jsonArgs.getString("by") : "none";
        String identifier = jsonArgs.has("id") ? jsonArgs.getString("id") : "none";

        JsonResponse response = new JsonResponse();

        switch (by) {
            case "email":

                try {
                    DataUser user = (DataUser) up.getUserByEmail(identifier);
                    response.setPayload(user);
                    response.setStatus("ok");

                } catch (UserNotExistException e) {

                    response.setStatus("error");
                    response.setError("user_not_exist");
                    response.setMsg(e.getMessage());

                    e.printStackTrace();
                } catch (UserException e) {

                    response.setStatus("error");
                    response.setMsg(e.getMessage());

                    e.printStackTrace();
                }

                resp.getWriter().println(om.writeValueAsString(response));
                break;
            case "id":
                try {
                    DataUser user = (DataUser) up.getUserById(identifier);

                    response.setPayload(user);
                    response.setStatus("ok");

                } catch (UserNotExistException e) {

                    response.setStatus("error");
                    response.setError("user_not_exist");
                    response.setMsg(e.getMessage());

                    e.printStackTrace();
                } catch (UserException e) {

                    response.setStatus("error");
                    response.setError("user_exception");
                    response.setMsg(e.getMessage());

                    e.printStackTrace();
                }

                resp.getWriter().println(om.writeValueAsString(response));
                break;

            case "username":
                try {
                    DataUser user = (DataUser) up.getUserByUsername(identifier);

                    response.setPayload(user);
                    response.setStatus("ok");

                } catch (UserNotExistException e) {

                    response.setStatus("error");
                    response.setError("user_not_exist");
                    response.setMsg(e.getMessage());

                    e.printStackTrace();
                } catch (UserException e) {

                    response.setStatus("error");
                    response.setError("user_exception");
                    response.setMsg(e.getMessage());

                    e.printStackTrace();
                }

                resp.getWriter().println(om.writeValueAsString(response));
                break;

            default:
                response.setStatus("error");
                response.setError("by_missing");
                response.setMsg("se necesita saber como se buscara el usuario: email,username,id");
                resp.getWriter().println(om.writeValueAsString(response));
                break;
        }

        ctx.complete();
    }

    @Operation(name = "delete")
    @SessionRequired
    public static void deleteUserOperation(AsyncContext ctx, JSONObject jsonArgs) throws JsonGenerationException, JsonMappingException, IOException {
        HttpServletResponse resp = (HttpServletResponse) ctx.getResponse();
        //HttpServletRequest req = (HttpServletRequest) ctx.getRequest();

        boolean hasError = false;
        OpUpdateJsonResponse response = new OpUpdateJsonResponse();
        String id = jsonArgs.has("id") ? jsonArgs.getString("id") : "";

        id = id != null ? id : "";

        if (id == "") {
            hasError = true;
            response.appendError("uid", "Debe suministrar un id.");
        }

        try {
            up.getUserById(id);
        } catch (UserNotExistException e1) {
            hasError = true;
            response.appendError("user", e1.getMessage());
            e1.printStackTrace();
        } catch (UserException e1) {
            hasError = true;
            response.appendError("user", "Error al obtener informacion de usuario. (" + e1.getMessage() + ")");
            e1.printStackTrace();
        }

        User user = new DataUser();
        user.setId(id);
        if (!hasError) {
            try {
                up.deleteUser(user);
            } catch (UserException e) {
                response.setStatus("error");
                response.setError("edit_exception");
                response.setMsg(e.getMessage());

                hasError = true;
                e.printStackTrace();
            }
        }

        if (hasError) {
            response.setStatus("error");
            if (response.getErrors().size() > 0) {
                response.setError("fields");
                response.setMsg("Los campos suministrados deben tener algun problema.");
            }
            resp.getWriter().println(om.writeValueAsString(response));

        } else {
            response.setStatus("ok");
            response.setMsg("usuario eliminado");
            response.setPayload(user);
            resp.getWriter().println(om.writeValueAsString(response));

        }
        ctx.complete();
    }

    @Operation(name = "update")
    @SessionRequired
    public static void updateUserOperation(AsyncContext ctx, JSONObject jsonArgs) throws JsonGenerationException, JsonMappingException, IOException {
        HttpServletResponse resp = (HttpServletResponse) ctx.getResponse();
        //HttpServletRequest req = (HttpServletRequest) ctx.getRequest();

        boolean hasError = false;
        OpUpdateJsonResponse response = new OpUpdateJsonResponse();
        UserValidator validator = up.getValidator();
        String id = jsonArgs.has("id") ? jsonArgs.getString("id") : "";
        String email = jsonArgs.has("email") ? jsonArgs.getString("email") : null;
        String username = jsonArgs.has("username") ? jsonArgs.getString("username") : null;
        String firstName = jsonArgs.has("firstname") ? jsonArgs.getString("firstname") : null;
        String lastName = jsonArgs.has("lastname") ? jsonArgs.getString("lastname") : null;
        String gender = jsonArgs.has("gender") ? jsonArgs.getString("gender") : null;
        //String password 	= jsonArgs.has("password") ? jsonArgs.getString("password"):"";

        id = id != null ? id : "";

        if (id == "") {
            hasError = true;
            response.appendError("uid", "Debe suministrar un id.");
        }

        DataUser oldUser = null;
        try {
            oldUser = (DataUser) up.getUserById(id);
        } catch (UserNotExistException e1) {
            response.setError("user_not_exist");
            response.setStatus("error");
            response.setMsg(e1.getMessage());

            e1.printStackTrace();
        } catch (UserException e1) {
            response.setError("user_exception");
            response.setStatus("error");
            response.setMsg(e1.getMessage());
            e1.printStackTrace();
        }

        if (oldUser == null) {
            resp.getWriter().println(om.writeValueAsString(response));
            ctx.complete();
            return;
        }

        email = email != null ? email : oldUser.getEmail();
        username = username != null ? username : oldUser.getUsername();
        firstName = firstName != null ? firstName : oldUser.getFirstName();
        lastName = lastName != null ? lastName : oldUser.getLastName();
        gender = gender != null ? gender : oldUser.getGender();

        DataUser userParam = new DataUser();
        userParam.setId(oldUser.getId());
        userParam.setEmail(oldUser.getEmail());
        userParam.setUsername(username);
        userParam.setFirstName(firstName);
        userParam.setLastName(lastName);
        userParam.setGender(gender);
        userParam.setEmailVerified(oldUser.isEmailVerified());
        userParam.setPassword(oldUser.getPassword());

        if (oldUser != null) {

            if (!gender.equalsIgnoreCase(oldUser.getGender())) {
                try {
                    validator.validateGender(userParam);
                } catch (GenderValidationException maile) {

                    response.appendError("gender", maile.getMessage());

                    hasError = true;
                } catch (ValidationException e) {
                    response.appendError("gender", e.getMessage());

                    hasError = true;
                }
            }

            if (!email.equalsIgnoreCase(oldUser.getEmail())) {
                try {
                    validator.validateEmail(userParam);
                } catch (EmailValidationException maile) {

                    response.appendError("email", maile.getMessage());

                    hasError = true;

                } catch (ValidationException e) {
                    response.appendError("email", e.getMessage());

                    hasError = true;
                }
            }

            if (!username.equals(oldUser.getUsername())) {
                try {
                    validator.validateUsername(userParam);
                } catch (UsernameValidationException usernamee) {

                    response.appendError("username", usernamee.getMessage());

                    hasError = true;
                } catch (ValidationException e) {
                    e.printStackTrace();
                }
            }

            if (!firstName.equalsIgnoreCase(oldUser.getFirstName())) {
                try {
                    validator.validateFirstName(userParam);
                } catch (FirstNameValidationException fnamee) {

                    response.appendError("firstname", fnamee.getMessage());

                    hasError = true;
                } catch (ValidationException e) {
                    e.printStackTrace();
                }
            }

            if (!lastName.equalsIgnoreCase(oldUser.getLastName())) {
                try {
                    validator.validateLastName(userParam);
                } catch (LastNameValidationException lnamee) {

                    response.appendError("lastname", lnamee.getMessage());

                    hasError = true;
                } catch (ValidationException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!hasError) {
            try {
                userParam = (DataUser) up.editUser(userParam);
                userParam = (DataUser) up.getUserById(userParam.getId());
            } catch (UserException e) {
                response.setStatus("error");
                response.setError("edit_exception");
                response.setMsg(e.getMessage());

                hasError = true;
            }
        }

        if (hasError) {
            response.setStatus("error");
            if (response.getErrors().size() > 0) {
                response.setError("fields");
                response.setMsg("Los campos suministrados deben tener algun problema.");
            }
            resp.getWriter().println(om.writeValueAsString(response));

        } else {
            response.setStatus("ok");
            response.setMsg("usuario modificado");
            response.setPayload(userParam);
            resp.getWriter().println(om.writeValueAsString(response));
        }

        ctx.complete();
    }

    @Operation(name = "create")
    public static void createUserOperation(AsyncContext ctx, JSONObject jsonArgs) throws JsonGenerationException, JsonMappingException, IOException {
        HttpServletResponse resp = (HttpServletResponse) ctx.getResponse();
        //HttpServletRequest req = (HttpServletRequest) ctx.getRequest();

        boolean hasError = false;
        OpCreateJsonResponse response = new OpCreateJsonResponse();
        UserValidator validator = up.getValidator();
        String email = jsonArgs.has("email") ? jsonArgs.getString("email") : "";
        String username = jsonArgs.has("username") ? jsonArgs.getString("username") : "";
        String firstName = jsonArgs.has("firstname") ? jsonArgs.getString("firstname") : "";
        String lastName = jsonArgs.has("lastname") ? jsonArgs.getString("lastname") : "";
        String gender = jsonArgs.has("gender") ? jsonArgs.getString("gender") : "n";
        String password = jsonArgs.has("password") ? jsonArgs.getString("password") : "";

        email = email != null ? email : "";
        username = username != null ? username : "";
        firstName = firstName != null ? firstName : "";
        lastName = lastName != null ? lastName : "";
        gender = gender != null ? gender : "n";
        password = password != null ? password : "";


        DataUser user = new DataUser();
        user.setId(System.currentTimeMillis() + "");
        user.setEmail(email);
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(password);
        user.setGender(gender);
        user.setCreateAt(System.currentTimeMillis());

        try {
            validator.validateGender(user);
        } catch (GenderValidationException maile) {

            response.appendError("gender", maile.getMessage());

            hasError = true;
        } catch (ValidationException e) {
            response.appendError("gender", e.getMessage());

            hasError = true;
        }

        try {
            validator.validateEmail(user);
        } catch (EmailValidationException maile) {

            response.appendError("email", maile.getMessage());

            hasError = true;
        } catch (ValidationException e) {
            response.appendError("email", e.getMessage());

            hasError = true;
        }

        try {
            validator.validateUsername(user);
        } catch (UsernameValidationException usernamee) {

            response.appendError("username", usernamee.getMessage());

            hasError = true;
        } catch (ValidationException e) {
            e.printStackTrace();
        }

        try {
            validator.validatePassword(user);
        } catch (PasswordValidationException passworde) {

            response.appendError("password", passworde.getMessage());

            hasError = true;
        } catch (ValidationException e) {
            e.printStackTrace();
        }

        try {
            validator.validateFirstName(user);
        } catch (FirstNameValidationException fnamee) {

            response.appendError("firstname", fnamee.getMessage());

            hasError = true;
        } catch (ValidationException e) {
            e.printStackTrace();
        }

        try {
            validator.validateLastName(user);
        } catch (LastNameValidationException lnamee) {

            response.appendError("lastname", lnamee.getMessage());

            hasError = true;
        } catch (ValidationException e) {
            e.printStackTrace();
        }

        if (!hasError) {
            try {
                ContextStore.createUserContext(user);
                up.createUser(user);
            } catch (UserException e) {
                response.setStatus("error");
                response.setError("create_exception");
                response.setMsg(e.getMessage());

                hasError = true;
            }
        }

        if (hasError) {
            response.setStatus("error");
            if (response.getErrors().size() > 0) {
                response.setError("fields");
                response.setMsg("Los campos suministrados deben tener algun problema.");
            }
            resp.getWriter().println(om.writeValueAsString(response));

        } else {
            response.setStatus("ok");
            response.setMsg("usuario resgistrado");
            response.setPayload(user);
            resp.getWriter().println(om.writeValueAsString(response));

        }

        ctx.complete();
    }

    @Operation(name = "changepassword")
    @SessionRequired
    public static void changePasswordOperation(AsyncContext ctx, JSONObject jsonArgs) throws JsonGenerationException, JsonMappingException, IOException {
        HttpServletResponse resp = (HttpServletResponse) ctx.getResponse();
        JsonResponse response = new JsonResponse();
        UserValidator validator = up.getValidator();
        User user = null;
        boolean hasError = false;
        String id = jsonArgs.has("id") ? jsonArgs.getString("id") : "";
        String nPassword = jsonArgs.has("password") ? jsonArgs.getString("password") : "";
        String currentPassword = jsonArgs.has("currentPassword") ? jsonArgs.getString("currentPassword") : "";
        nPassword = nPassword != null ? nPassword : "";
        DataUser tmpPassUser = new DataUser();
        DataUser tmpPassUser2 = new DataUser();
        tmpPassUser.setPassword(nPassword);
        tmpPassUser2.setPassword(currentPassword);

        try {
            validator.validatePassword(tmpPassUser);
        } catch (PasswordValidationException e) {
            hasError = true;
            response.setStatus("error");
            response.setError("password_validation");
            response.setMsg(e.getMessage());
            e.printStackTrace();
        } catch (ValidationException e1) {
            hasError = true;
            response.setStatus("error");
            response.setError("validation_exception");
            response.setMsg(e1.getMessage());
            e1.printStackTrace();
        }

        try {
            validator.validatePassword(tmpPassUser2);
        } catch (PasswordValidationException e) {
            hasError = true;
            response.setStatus("error");
            response.setError("current_password_validation");
            response.setMsg(e.getMessage());
            e.printStackTrace();
        } catch (ValidationException e1) {
            hasError = true;
            response.setStatus("error");
            response.setError("validation_exception");
            response.setMsg(e1.getMessage());
            e1.printStackTrace();
        }

        if (id != null) {
            try {
                user = up.getUserById(id);
            } catch (UserNotExistException e) {
                hasError = true;
                response.setStatus("error");
                response.setError("user_not_exist");
                response.setMsg(e.getMessage());
                e.printStackTrace();
            } catch (UserException e) {
                hasError = true;
                response.setStatus("error");
                response.setError("user_exception");
                response.setMsg(e.getMessage());
                e.printStackTrace();
            }

        } else {
            hasError = true;
            response.setStatus("error");
            response.setError("uid");
            response.setMsg("El id no puede estar vacio o ser nulo.");

        }

        if (!hasError) {
            String storeCurrentPassword = user.getPassword();
            storeCurrentPassword = cipher.decrypt(storeCurrentPassword);
            if (!storeCurrentPassword.equals(currentPassword)) {
                hasError = true;
                response.setStatus("error");
                response.setError("current_password");
                response.setMsg("La contraseña actual introducida no concuerda con la contraseña.");
            }
        }

        if (!hasError) {
            try {
                user = up.changePasswordUser(new UserMutatorPassword(user, nPassword));
            } catch (UserMutatorException e) {
                hasError = true;
                response.setStatus("error");
                response.setError("user_mutator");
                response.setMsg(e.getMessage());
            } catch (UserException e) {
                hasError = true;
                response.setStatus("error");
                response.setError("user_exception");
                response.setMsg(e.getMessage());
                e.printStackTrace();
            }
        }

        if (hasError) {
            resp.getWriter().println(om.writeValueAsString(response));

        } else {
            response.setStatus("ok");
            response.setMsg("Contraseña cambiada satisfactoriamente.");
            response.setPayload(user);
            resp.getWriter().println(om.writeValueAsString(response));

        }
        ctx.complete();
    }

    @Operation(name = "search")
    private static void searchUserOperation(AsyncContext ctx, JSONObject jsonArgs) throws IOException {
        long initOp = System.currentTimeMillis();
        HttpServletResponse resp = (HttpServletResponse) ctx.getResponse();
        SearchResponse response = new SearchResponse();
        UserValidator validator = up.getValidator();
        User user = null;
        boolean hasError = false;
        String queryString = jsonArgs.has("query") ? jsonArgs.getString("query") : "";

        UsersFound result = us.search(queryString);
        orchi.HHCloud.user.Users users = new orchi.HHCloud.user.Users();
        users.addAll(result.getAll());
        response.setPayload(users);
        long endOp = System.currentTimeMillis();
        endOp = endOp-initOp;
        response.setTook(endOp);

        resp.getWriter().println(om.writeValueAsString(response));

        ctx.complete();
    }


    @Override
    public void init(ServletConfig config) throws ServletException {
        us = Start.getUserManager().getSearchUserProvider();
        up = Start.getUserManager().getUserProvider();
        sp = Start.getStoreManager().getStoreProvider();
        om = new ObjectMapper();
        om.enable(org.codehaus.jackson.map.SerializationConfig.Feature.INDENT_OUTPUT);
        om.getJsonFactory();
        executor = new ThreadPoolExecutor(1000, 10000, 10L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(1000000));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //executor.execute(new Task(req.startAsync()));
        CompletableFuture.runAsync(new Task(req.startAsync()), executor);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        //executor.execute(new Task(req.startAsync()));
        CompletableFuture.runAsync(new Task(req.startAsync()), executor);

    }

    public static class Task extends ServiceTaskAPIImpl implements Runnable {
        private AsyncContext ctx;

        public Task(AsyncContext ctx) {
            super(ctx);
            this.ctx = ctx;
        }

        public void sendError(Exception e2) {
            JsonResponse response = new JsonResponse();
            response.setStatus("error");
            response.setError("server_error");
            response.setMsg(e2.getMessage());

            try {
                ctx.getResponse().getWriter().println(om.writeValueAsString(response));
            } catch (IOException e) {
                e.printStackTrace();
            }
            ctx.complete();
        }

        @Override
        public void run() {
            try {
                process();
            } catch (NullPointerException e) {
                e.printStackTrace();
                sendError(e);
            } catch (JsonGenerationException e) {
                e.printStackTrace();
                sendError(e);
            } catch (JsonMappingException e) {
                e.printStackTrace();
                sendError(e);
            } catch (org.json.JSONException e) {
                e.printStackTrace();
                sendError(e);
            } catch (IOException e) {
                e.printStackTrace();
                sendError(e);
            }
        }

        private void process() throws JsonGenerationException, JsonMappingException, IOException {

            HttpServletRequest req = (HttpServletRequest) ctx.getRequest();
            HttpServletResponse resp = (HttpServletResponse) ctx.getResponse();
            //resp.setHeader("Access-Control-Allow-Origin", ACCESS_CONTROL_ALLOW_ORIGIN);
            resp.setHeader("Content-type", "application/json");
            resp.setHeader("Access-Control-Allow-Credentials", "true");

            ParseParamsMultiPart2 p = null;
            try {
                p = new ParseParamsMultiPart2(req);
                checkAvailability(apiName, p.getString("op"));
            } catch (Exception e) {
                sendError(e);
                e.printStackTrace();
                return;
            }
            //ParseParamsMultiPart2  p = (ParseParamsMultiPart2) req.getAttribute("params");

            JSONObject jsonArgs = new JSONObject(p.getString("args"));
            Log.info("{}", jsonArgs.toString(2));

            String op = p.getString("op");//jsonArgs.has("op")?jsonArgs.getString("op"):null;
            if (op == null) {
                op = "none";
            }
            op = op.toLowerCase();

            switch (op) {
                case "get"://session
                    getOperation(ctx, jsonArgs);
                    break;
                case "accountstatus"://session
                    AccountStatusOperation(ctx, jsonArgs);
                    break;
                case "create"://no session
                    createUserOperation(ctx, jsonArgs);
                    break;
                case "update"://session
                    updateUserOperation(ctx, jsonArgs);
                    break;

                case "delete"://session
                    deleteUserOperation(ctx, jsonArgs);
                    break;

                case "sendverifyemail"://session
                    sendVerifyEmailOperation(ctx, jsonArgs);
                    break;

                case "sendrecoveryemail"://no session
                    sendRecoveryEmailOperation(ctx, jsonArgs);
                    break;

                case "changepassword"://session
                    changePasswordOperation(ctx, jsonArgs);
                    break;

                case "changepasswordbyrecover"://no session
                    changePasswordByRecover(ctx, jsonArgs);
                    break;

                case "search"://no session
                    searchUserOperation(ctx, jsonArgs);
                    break;

                default:
                    JsonResponse response = new JsonResponse();
                    response.setStatus("error");
                    response.setError("op_not_espesified");
                    response.setMsg("No espesifico operacion.");

                    resp.getWriter().println(om.writeValueAsString(response));
                    ctx.complete();
                    break;
            }
        }

    }

    /*--------------------------------OPERACIONES------------------------**/
    public static class JsonResponse {
        public String status = "ok";
        public String msg = "ok";
        public User payload;
        private String error;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public User getPayload() {
            return payload;
        }

        public void setPayload(User payload) {
            this.payload = payload;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

    }

    public static class OpCreateJsonResponse {
        public String status = "ok";
        public String msg = "ok";
        public User payload;
        private String error;
        private Map<String, String> errors = new HashMap<String, String>();

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public User getPayload() {
            return payload;
        }

        public void setPayload(User payload) {
            this.payload = payload;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public Map<String, String> getErrors() {
            return errors;
        }

        public Map<String, String> appendError(String field, String error) {
            errors.put(field, error);
            return errors;
        }

    }

    public static class OpUpdateJsonResponse {
        public String status = "ok";
        public String msg = "ok";
        public User payload;
        private String error;
        private Map<String, String> errors = new HashMap<String, String>();

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public User getPayload() {
            return payload;
        }

        public void setPayload(User payload) {
            this.payload = payload;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public Map<String, String> getErrors() {
            return errors;
        }

        public Map<String, String> appendError(String field, String error) {
            errors.put(field, error);
            return errors;
        }

    }

    public static class AccoutStatePayload {
        private User user;
        private ContentSummary storageSummary;

        public ContentSummary getStorageSummary() {
            return storageSummary;
        }

        public void setStorageSummary(ContentSummary storageSummary) {
            this.storageSummary = storageSummary;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }


    }

    public static class AccountStateJsonResponse {
        private String status = "ok";
        private String error;
        private String msg = "ok";
        private AccoutStatePayload payload = null;


        public AccoutStatePayload getPayload() {
            return payload;
        }

        public void setPayload(AccoutStatePayload payload) {
            this.payload = payload;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

    }

    public static class SearchResponse {

        private long took = 0L;
        private String status = "ok";
        private String error;
        private String msg = "ok";

        public long getTook() {
            return took;
        }

        public void setTook(long took) {
            this.took = took;
        }

        public orchi.HHCloud.user.Users getPayload() {
            return payload;
        }

        public void setPayload(orchi.HHCloud.user.Users payload) {
            this.payload = payload;
        }

        private orchi.HHCloud.user.Users payload = null;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }
}
