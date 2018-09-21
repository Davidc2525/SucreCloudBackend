package orchi.HHCloud.Api.Auth;

import orchi.HHCloud.Start;
import orchi.HHCloud.Util;
import orchi.HHCloud.auth.DefaultAuthProvider;
import orchi.HHCloud.auth.Exceptions.*;
import orchi.HHCloud.user.Exceptions.UserException;
import orchi.HHCloud.user.Exceptions.UserNotExistException;
import orchi.HHCloud.user.LoginDataUser;
import orchi.HHCloud.user.User;
import orchi.HHCloud.user.UserManager;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.mortbay.log.Log;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Login extends HttpServlet {

    private static final long serialVersionUID = 1L;
    public static ThreadPoolExecutor executorw2;
    /**
     *
     */
    private static String ACCESS_CONTROL_ALLOW_ORIGIN = Start.conf.getString("api.headers.aclo");
    ;
    private static ObjectMapper om = new ObjectMapper();
    /**
     * proveedor de auttenticacion
     */
    private static DefaultAuthProvider authProvider;

    public static void setCookie(HttpSession session, HttpServletResponse resp) {
        Cookie c = new Cookie(Start.conf.getString("app.name") + "-S", session.getId());
        c.setMaxAge(60 * 60 * 24 * 365);
        c.setPath("/");
        resp.addCookie(c);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        executorw2 = new ThreadPoolExecutor(10, 1000, 50000L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(100000));

        //om = new ObjectMapper();
        om.enable(org.codehaus.jackson.map.SerializationConfig.Feature.INDENT_OUTPUT);
        om.getJsonFactory();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        executorw2.execute(new Task(req.getAsyncContext()));
    }

    public static class AuthJsonResponse {
        public String status = "ok";
        public String sesid = null;
        public String userid = null;
        public boolean auth = false;
        public boolean exist = false;
        public String msg = "";
        public String username = null;
        public String password = null;

        public AuthJsonResponse(String m, boolean auth) {

            msg = m;
            this.auth = auth;
        }

        public AuthJsonResponse setSesId(String id) {
            sesid = id;
            return this;
        }

        public AuthJsonResponse setUsernameError(String error) {
            username = error;
            return this;
        }

        public AuthJsonResponse setPasswordError(String error) {
            password = error;
            return this;
        }

        public String getSesid() {
            return sesid;
        }

        public String getUserid() {
            return userid;
        }

        public AuthJsonResponse setUserid(String userid) {
            this.userid = userid;
            return this;
        }

        public boolean isAuth() {
            return auth;
        }

        public boolean isExist() {
            return exist;
        }

        public AuthJsonResponse setExist(boolean e) {
            exist = e;
            return this;
        }

        public String getMsg() {
            return msg;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getStatus() {
            return status;
        }

        public AuthJsonResponse setStatus(String status) {
            this.status = status;
            return this;
        }
    }

    public static class Task implements Runnable {

        private AsyncContext ctx;

        public Task(AsyncContext asyncCtx) {
            this.ctx = asyncCtx;
        }

        public void writeResponse(AuthJsonResponse data) {
            try {
                ((HttpServletResponse) ctx.getResponse()).setHeader("Content-type", "application/json");
                ((HttpServletResponse) ctx.getResponse()).setHeader("Access-Control-Allow-Credentials", "true");
                ((HttpServletResponse) ctx.getResponse()).setHeader("Access-Control-Allow-Origin", ACCESS_CONTROL_ALLOW_ORIGIN);
                ((HttpServletResponse) ctx.getResponse()).setHeader("Content-encoding", "gzip");

                om.writeValue(new GzipCompressorOutputStream(ctx.getResponse().getOutputStream()), data);
                ctx.complete();
            } catch (IOException e) {
                e.printStackTrace();
                ctx.complete();
            }

        }

        ;

        @Override
        public void run() {
            try {
                this.process();
            } catch (JsonGenerationException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public LoginDataUser createUserLoginWithRequest(JSONObject jsonArgs) {
            //HttpServletRequest req = ((HttpServletRequest) ctx.getRequest());
            String username = jsonArgs.has("username") ? jsonArgs.getString("username") : "";
            String password = jsonArgs.has("password") ? jsonArgs.getString("password") : "";
            boolean isRemember = jsonArgs.has("remember") ? jsonArgs.getBoolean("remember") : false;


            return new LoginDataUser().bind(username, password, isRemember);
        }

        public void process() throws JsonGenerationException, JsonMappingException, IOException, InterruptedException {
            HttpServletRequest req = ((HttpServletRequest) ctx.getRequest());
            HttpServletResponse resp = ((HttpServletResponse) ctx.getResponse());
            HttpSession s = req.getSession(false);
            JSONObject jsonArgs = Util.parseParams(req);
            Log.info("{}", jsonArgs.toString(2));

            if (s == null) {

                if (jsonArgs.has("username") && jsonArgs.has("password")) {

                    try {
                        LoginDataUser newUser = createUserLoginWithRequest(jsonArgs);

                        Start.getLoginAndOut().logInCallBack(ctx, newUser, (/** LoginDataSuccess */loginData) -> {
                            HttpSession session = ((HttpServletRequest) loginData.getCtx().getRequest())
                                    .getSession(false);

                            session.setAttribute("uid", loginData.getUser().getId());

                            if (newUser.isRemember()) {
                                session.setMaxInactiveInterval(-1);
                            }

                            writeResponse(new AuthJsonResponse("login", true).setUserid(loginData.getUser().getId())
                                    .setSesId(session.getId()));

                        });

                    } catch (AuthUsernameException e) {
                        writeResponse(new AuthJsonResponse(e.getMessage(), false).setUsernameError(e.getMessage()).setStatus("error"));
                    } catch (AuthPasswordException e) {
                        writeResponse(new AuthJsonResponse(e.getMessage(), false).setPasswordError(e.getMessage()).setStatus("error"));
                    } catch (AuthUserNotExistsException e) {
                        writeResponse(new AuthJsonResponse(e.getMessage(), false).setUsernameError(e.getMessage()).setStatus("error"));
                    } catch (AuthExceededCountFaildException e) {
                        writeResponse(new AuthJsonResponse(e.getMessage(), false).setPasswordError(e.getMessage()).setStatus("error"));
                    } catch (AuthException e) {
                        writeResponse(new AuthJsonResponse(e.getMessage(), false).setStatus("error"));
                    } catch (Exception e) {
                        writeResponse(new AuthJsonResponse(e.getMessage(), false).setStatus("error"));
                    }

                } else {
                    writeResponse(new AuthJsonResponse("you need send data", false)
                            .setUsernameError("username is required")
                            .setPasswordError("password is required")
                            .setStatus("error"));
                }
            } else {
                User user = null;//= createUserLoginWithRequest();
                try {
                    user = UserManager.getInstance().getUserProvider().getUserById((String) s.getAttribute("uid"));
                } catch (UserNotExistException e) {
                    e.printStackTrace();
                    writeResponse(new AuthJsonResponse(e.getMessage(), false)
                            .setUsernameError(e.getMessage())
                            .setStatus("error"));
                } catch (UserException e) {
                    e.printStackTrace();
                    writeResponse(new AuthJsonResponse(e.getMessage(), false)
                            .setExist(false)
                            .setStatus("error"));
                }

                /**
                 * luego de iniciar session, el usuario seguro quiere que su navegador recuerde su sesion
                 * para eso, justo despoes de iniciar la session, se envia otra solicitud de iniciar session
                 * pero en los argumentos solo puede estar el parametro remember, q es lo unico que se usara
                 * para hacer un seteo de una cookie de sesion con los datos de la session (de la cookie de session)
                 * para asi pasarle al navegador la nueva fecha de expiracion de la cookie de sesion.
                 *
                 * si se envia luego de iniciar sesion otra solisitud de inicio de sesion, y el parametro remember es false
                 * no se envia la nueva cookie al navegador.
                 * */
                LoginDataUser newUser = createUserLoginWithRequest(jsonArgs);
                if (newUser.isRemember()) {
                    setCookie(s, resp);

                    writeResponse(new AuthJsonResponse("session maxage set", false)
                            .setExist(true)
                            .setUserid(user.getId())
                            .setSesId(s.getId()));

                    return;
                }

                writeResponse(new AuthJsonResponse("session aleardy create", false)
                        .setExist(true)
                        .setUserid(user.getId())
                        .setSesId(s.getId()));


            }

        }

    }
}
