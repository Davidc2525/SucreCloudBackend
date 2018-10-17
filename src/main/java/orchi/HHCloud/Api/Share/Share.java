package orchi.HHCloud.Api.Share;

import orchi.HHCloud.Api.API;
import orchi.HHCloud.Api.ServiceTaskAPIImpl;
import orchi.HHCloud.Api.annotations.Operation;
import orchi.HHCloud.ParseParamsMultiPart2;
import orchi.HHCloud.Start;
import orchi.HHCloud.share.*;
import orchi.HHCloud.store.StoreProvider;
import orchi.HHCloud.store.arguments.DownloadArguments;
import orchi.HHCloud.store.arguments.ListArguments;
import orchi.HHCloud.store.arguments.MoveOrCopyArguments;
import orchi.HHCloud.store.response.ListResponse;
import orchi.HHCloud.store.response.MoveOrCopyResponse;
import orchi.HHCloud.stores.HdfsStore.MoveOrCopyOperation;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.Exceptions.UserException;
import orchi.HHCloud.user.User;
import orchi.HHCloud.user.UserProvider;
import orchi.HHCloud.user.Users;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.slf4j.Logger;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Operation(name = "fs::ls")
@Operation(name = "fs::status")
@Operation(name = "fs::download")

@Operation(name = "user::list", isRequired = true)
@Operation(name = "user::delete", isRequired = true)
@Operation(name = "user::copy", isRequired = true)

@Operation(name = "own::get", isRequired = true)
@Operation(name = "own::share", isRequired = true)
@Operation(name = "own::delete", isRequired = true)
@Operation(name = "own::set_users_path", isRequired = true)
@Operation(name = "own::set_mode", isRequired = true)
public class Share extends API {

    public static String apiName = "/share";
    private static Logger log = org.slf4j.LoggerFactory.getLogger(Share.class);
    private static String ACCESS_CONTROL_ALLOW_ORIGIN = Start.conf.getString("api.headers.aclo");
    private static ObjectMapper om;
    private static StoreProvider sp = Start.getStoreManager().getStoreProvider();
    private static ShareProvider shp = Start.getShareManager().getShareProvider();
    private ThreadPoolExecutor executor;

    @Override
    public void init() throws ServletException {
        executor = new ThreadPoolExecutor(1000, 10000, 50000L, TimeUnit.MICROSECONDS,
                new LinkedBlockingQueue<Runnable>(100000));

        om = new ObjectMapper();
        om.enable(org.codehaus.jackson.map.SerializationConfig.Feature.INDENT_OUTPUT);
        om.getJsonFactory();

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        CompletableFuture.runAsync(new Task(req.startAsync()), executor);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        CompletableFuture.runAsync(new Task(req.startAsync()), executor);
    }

    public static class Task extends ServiceTaskAPIImpl implements Runnable {


        public Task(AsyncContext ctx) {
            super(ctx);
        }

        @Override
        public void run() {
            try {
                process();
            } catch (Exception e) {
                try {
                    e.printStackTrace();
                    sendError(e.getMessage());
                } catch (Exception e1) {
                    getCtx().complete();
                }
            }
        }

        private void process() throws Exception {
            HttpServletRequest reqs = (HttpServletRequest) getCtx().getRequest();
            HttpServletResponse resps = (HttpServletResponse) getCtx().getResponse();
            HttpSession session = reqs.getSession(false);
            resps.setHeader("Access-Control-Allow-Origin", ACCESS_CONTROL_ALLOW_ORIGIN);
            resps.setHeader("Content-type", "application/json");
            resps.setHeader("Access-Control-Allow-Credentials", "true");


            JSONObject jsonArgs = null;
            ParseParamsMultiPart2 p = null;
            String op = "none";
            try {
                p = new ParseParamsMultiPart2(reqs);
                op = p.getString("op");
                if (op == null) {
                    op = "none";
                }
                op = op.toLowerCase();

                checkAvailability(apiName, op);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            if (p.getString("args") != null) {
                jsonArgs = new JSONObject(p.getString("args"));
            } else {
                jsonArgs = new JSONObject();
            }

            switch (op) {

                case "own::get":
                    new OwnerOperations(getCtx(), jsonArgs).get();
                    break;

                case "own::share":
                    new OwnerOperations(getCtx(), jsonArgs).share();
                    break;

                case "own::delete":
                    new OwnerOperations(getCtx(), jsonArgs).delete();
                    break;

                case "own::set_users_path":
                    new OwnerOperations(getCtx(), jsonArgs).setUsersToPath();
                    break;

                case "own::set_mode":
                    new OwnerOperations(getCtx(), jsonArgs).setMode();
                    break;


                case "user::list":
                    new UserOperations(getCtx(), jsonArgs).list();
                    break;

                case "user::delete":
                    new UserOperations(getCtx(), jsonArgs).delete();
                    break;

                case "user::copy":
                    new UserOperations(getCtx(), jsonArgs).copy();
                    break;


                case "fs::status":
                    new FsOperations(getCtx(), jsonArgs).getStatus();
                    break;
                case "fs::download":
                    new FsOperations(getCtx(), jsonArgs).download();
                    break;

                case "fs::ls":
                    new FsOperations(getCtx(), jsonArgs).list();
                    return;
            }

        }
    }


    public static class Response {
        public String status = "ok";
        public String msg = "ok";
        public Object payload;
        public String error;

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

        public Object getPayload() {
            return payload;
        }

        public void setPayload(Object payload) {
            this.payload = payload;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            status = "error";
            this.error = error;
        }


    }

    public static abstract class Operations {

        public HttpServletRequest reqs;
        public HttpServletResponse resps;
        public AsyncContext ctx;
        public JSONObject jsonArgs;
        public ShareProvider shp = Start.getShareManager().getShareProvider();

        public Operations(AsyncContext ctx, JSONObject jsonArgs) {
            this.ctx = ctx;
            this.jsonArgs = jsonArgs;
            reqs = (HttpServletRequest) this.ctx.getRequest();
            resps = (HttpServletResponse) this.ctx.getResponse();
        }

    }

    public static class UserOperations extends Operations {
        private UserProvider up = Start.getUserManager().getUserProvider();
        private StoreProvider sp = Start.getStoreManager().getStoreProvider();

        public UserOperations(AsyncContext ctx, JSONObject jsonArgs) {
            super(ctx, jsonArgs);
        }

        public void list() throws IOException {
            /*JSONObject args = jsonArgs;
            Mode mode = args.has("mode") ? Mode.valueOf(args.getString("mode").toUpperCase()) : Mode.P;
            List<Object> toUsers = args.has("users") ? args.getJSONArray("users").toList() : null;
            String path = args.has("path") ? args.getString("path") : null;

            if (path == null) {
                hasError = true;
                response.setMsg("Debe espesificar una rruta");
                response.setStatus("error");
                response.setError("path missing");
            }*/


            boolean hasError = false;
            Response response = new Response();

            DataUser ownerUser = null;
            if (!hasError) {
                HttpSession s = reqs.getSession(false);
                String uid = (String) s.getAttribute("uid");

                try {
                    ownerUser = (DataUser) up.getUserById(uid);
                } catch (UserException e) {
                    e.printStackTrace();

                    hasError = true;
                    response.setMsg(e.getMessage());
                    response.setStatus("error");
                    response.setError("user_error");
                }

            }

            if (!hasError) {
                Shared shared = shp.getSharedWithMe(ownerUser);
                response.setPayload(shared);

                resps.getWriter().println(om.writeValueAsString(response));
                ctx.complete();
            }

        }

        public void delete() throws IOException {
            JSONObject args = jsonArgs;
            // Mode mode = args.has("mode") ? Mode.valueOf(args.getString("mode").toUpperCase()) : Mode.P;
            //List<Object> toUsers = args.has("users") ? args.getJSONArray("users").toList() : null;
            String path = args.has("path") ? args.getString("path") : null;
            String owner = args.has("owner") ? args.getString("owner") : null;

            boolean hasError = false;
            Response response = new Response();

            DataUser ownerUser = null;
            DataUser user = null;

            if (path == null) {
                hasError = true;
                response.setMsg("Debe espesificar una rruta");
                response.setStatus("error");
                response.setError("path missing");
            }

            if (!hasError) {
                HttpSession s = reqs.getSession(false);
                String uid = (String) s.getAttribute("uid");

                try {
                    user = (DataUser) up.getUserById(uid);
                } catch (UserException e) {
                    e.printStackTrace();

                    hasError = true;
                    response.setMsg(e.getMessage());
                    response.setStatus("error");
                    response.setError("user_error");
                }

            }

            if (!hasError) {

                try {
                    ownerUser = (DataUser) up.getUserById(owner);
                } catch (UserException e) {
                    e.printStackTrace();

                    hasError = true;
                    response.setMsg(e.getMessage());
                    response.setStatus("error");
                    response.setError("user_error::owner");
                }

            }

            if (!hasError) {

                shp.deleteSharedWith(ownerUser, user, Paths.get(path));

                /*if(shp.isShared(ownerUser,Paths.get(path))){

                }else{
                    hasError = true;
                    response.setMsg("La ruta que desea editar no se encuentra actualmente compartida");
                    response.setStatus("error");
                    response.setError("user_error::owner");
                }*/
            }

            resps.getWriter().println(om.writeValueAsString(response));
            ctx.complete();

        }

        public void copy() throws IOException, UserException {
            JSONObject args = jsonArgs;
            // Mode mode = args.has("mode") ? Mode.valueOf(args.getString("mode").toUpperCase()) : Mode.P;
            //List<Object> toUsers = args.has("users") ? args.getJSONArray("users").toList() : null;
            String sPath = args.has("spath") ? args.getString("spath") : null;
            String srcPath = args.has("srcpath") ? args.getString("srcpath") : null;
            String dstPath = args.has("dstpath") ? args.getString("dstpath") : null;
            String owner = args.has("owner") ? args.getString("owner") : null;


            boolean hasError = false;
            Response response = new Response();

            DataUser ownerUser = null;
            DataUser user = null;

            if (sPath == null) {
                hasError = true;
                response.setMsg("Debe espesificar rruta compartida");
                response.setStatus("error");
                response.setError("spath_missing");
            }

            if (!hasError) {
                if (srcPath == null) {
                    hasError = true;
                    response.setMsg("Debe espesificar rruta de entrada");
                    response.setStatus("error");
                    response.setError("srcpath_missing");
                }
            }

            if (!hasError) {
                if (dstPath == null) {
                    hasError = true;
                    response.setMsg("Debe espesificar rruta de destino");
                    response.setStatus("error");
                    response.setError("dstpath_missing");
                }
            }

            if (!hasError) {
                HttpSession s = reqs.getSession(false);
                String uid = (String) s.getAttribute("uid");

                try {
                    user = (DataUser) up.getUserById(uid);
                } catch (UserException e) {
                    e.printStackTrace();

                    hasError = true;
                    response.setMsg(e.getMessage());
                    response.setStatus("error");
                    response.setError("user_error");
                }

            }

            if (!hasError) {

                try {
                    ownerUser = (DataUser) up.getUserById(owner);
                } catch (UserException e) {
                    e.printStackTrace();

                    hasError = true;
                    response.setMsg(e.getMessage());
                    response.setStatus("error");
                    response.setError("user_error::owner");
                }

            }

            Path ssPath = Paths.get("/", Paths.get(sPath).normalize() + "");
            Path subPath = Paths.get("/", Paths.get(srcPath).normalize() + "");
            Path sPATH = Paths.get(ssPath.normalize() + "/" + subPath.normalize()).normalize();

            if (!hasError) {
                if (shp.isShared(ownerUser, ssPath)) {
                    Mode mode = shp.getMode(ownerUser, ssPath);
                    if (mode == Mode.P) {

                    } else if (mode == Mode.U) {
                        HttpSession s = reqs.getSession(false);

                        if (s == null) {
                            hasError = true;
                            response.setError("session_require");
                            response.setStatus("error");
                            response.setMsg("Esta rruta esta compartida en modo privado con algunos usuarios, tienes que iniciar sesion y debe estar compartida contigo.");
                        } else {
                            DataUser u2 = (DataUser) Start.getUserManager().getUserProvider().getUserById((String) s.getAttribute("uid"));
                            if (!shp.isSharedWith(ownerUser, u2, ssPath) || shp.isShared(u2, ssPath)) {

                                hasError = true;
                                response.setError("not_share_with_you");
                                response.setStatus("error");
                                response.setMsg("Esta rruta esta en modo privado y no se encuentra conpartida con tigo");

                            }
                        }
                    }
                } else {
                    hasError = true;
                    response.setMsg("'" + sPath + "' Esta rruta no esta conpartida y no puedes acceder a '" + srcPath + "'");
                    response.setStatus("error");
                    response.setError("not_share");
                }
            }

            if (!hasError) {
                if (!sp.exists(ownerUser, sPATH)) {
                    hasError = true;
                    response.setMsg("La rruta fuente no existe " + srcPath);
                    response.setStatus("error");
                    response.setError("spath_no_exist");
                }
            }

            if (!hasError) {
                if (sp.exists(user, Paths.get(dstPath))) {
                    hasError = true;
                    response.setMsg("La rruta de destino ya existe " + dstPath);
                    response.setStatus("error");
                    response.setError("dpath_exist");
                }
            }

            if (!hasError) {
                MoveOrCopyArguments arg = new MoveOrCopyArguments();
                arg.setDstPath(Paths.get(dstPath));
                arg.setSrcPath(sPATH);

                arg.setUser(ownerUser);
                MoveOrCopyOperation moc = new MoveOrCopyOperation(arg);
                moc.setContextDst(user);
                moc.setContextSrc(ownerUser);

                MoveOrCopyResponse res = moc.call();

                response.setPayload(res);
            }

            resps.getWriter().println(om.writeValueAsString(response));
            ctx.complete();
        }
    }

    public static class OwnerOperations extends Operations {

        private StoreProvider sp = Start.getStoreManager().getStoreProvider();
        private UserProvider up = Start.getUserManager().getUserProvider();

        public OwnerOperations(AsyncContext ctx, JSONObject jsonArgs) {
            super(ctx, jsonArgs);

        }

        public void share() throws IOException {
            JSONObject args = jsonArgs;
            Mode mode = args.has("mode") ? Mode.valueOf(args.getString("mode").toUpperCase()) : Mode.P;
            List<Object> toUsers = args.has("users") ? args.getJSONArray("users").toList() : null;
            String path = args.has("path") ? args.getString("path") : null;

            Users users = new Users();

            if (toUsers != null) {
                if (toUsers.size() > 0) {
                    toUsers.stream()
                            .map(id -> String.valueOf(id))
                            .filter(id -> !id.equalsIgnoreCase(""))
                            .filter(id -> id.length() > 5)
                            .forEach((id) -> {
                                DataUser u = new DataUser();
                                u.setId(id);
                                users.add(u);
                            });
                }
            }

            boolean hasError = false;
            Response response = new Response();

            DataUser ownerUser = null;

            if (path == null) {
                hasError = true;
                response.setMsg("Debe espesificar una rruta");
                response.setStatus("error");
                response.setError("path missing");
            }

            if (!hasError) {
                HttpSession s = reqs.getSession(false);
                String uid = (String) s.getAttribute("uid");

                try {
                    ownerUser = (DataUser) up.getUserById(uid);
                } catch (UserException e) {
                    e.printStackTrace();

                    hasError = true;
                    response.setMsg(e.getMessage());
                    response.setStatus("error");
                    response.setError("user_error");
                }

            }

            if (!hasError) {
                if (sp.exists(ownerUser, Paths.get(path))) {

                    if (shp.isShared(ownerUser, Paths.get(path))) {
                        hasError = true;
                        response.setMsg("Esta rruta ya se encuentra compartida");
                        response.setStatus("error");
                        response.setError("yet_share");
                    } else {
                        try {
                            shp.createShare(ownerUser, users, Paths.get(path), mode);
                        } catch (ShareException e) {
                            e.printStackTrace();
                            hasError = true;
                            response.setMsg(e.getMessage());
                            response.setStatus("error");
                            response.setError("error_to_create_share");
                        }
                    }


                } else {
                    hasError = true;
                    response.setMsg("No puedes compartir una rruta que no existe");
                    response.setStatus("error");
                    response.setError("path_share_not_exist");
                }
            }

            resps.getWriter().println(om.writeValueAsString(response));
            ctx.complete();
        }

        public void delete() throws IOException {
            JSONObject args = jsonArgs;
            //Mode mode = args.has("mode") ? Mode.valueOf(args.getString("mode").toUpperCase()) : Mode.P;
            //List<Object> toUsers = args.has("users") ? args.getJSONArray("users").toList() : null;
            String path = args.has("path") ? args.getString("path") : null;
            boolean recursive = args.has("recursive") ? args.getBoolean("recursive") : false;

            boolean hasError = false;

            Response response = new Response();
            DataUser ownerUser = null;
            if (path == null) {
                hasError = true;
                response.setMsg("Debe espesificar una rruta");
                response.setStatus("error");
                response.setError("path missing");
            }

            if (!hasError) {
                HttpSession s = reqs.getSession(false);
                String uid = (String) s.getAttribute("uid");

                try {
                    ownerUser = (DataUser) up.getUserById(uid);
                } catch (UserException e) {
                    e.printStackTrace();

                    hasError = true;
                    response.setMsg(e.getMessage());
                    response.setStatus("error");
                    response.setError("user_error");
                }

            }

            if (!hasError) {
                shp.deleteShare(ownerUser, Paths.get(path), recursive);
            }
            resps.getWriter().println(om.writeValueAsString(response));
            ctx.complete();
        }

        public void setUsersToPath() throws IOException {
            JSONObject args = jsonArgs;
            Mode mode = args.has("mode") ? Mode.valueOf(args.getString("mode").toUpperCase()) : null;
            List<Object> toUsers = args.has("users") ? args.getJSONArray("users").toList() : null;
            String path = args.has("path") ? args.getString("path") : null;

            Users users = new Users();

            if (toUsers != null) {
                if (toUsers.size() > 0) {
                    toUsers.stream()
                            .map(id -> String.valueOf(id))
                            .filter(id -> !id.equalsIgnoreCase(""))
                            .filter(id -> id.length() > 5)
                            .forEach((id) -> {
                                DataUser u = new DataUser();
                                u.setId(id);
                                users.add(u);
                            });
                }
            }

            boolean hasError = false;

            Response response = new Response();
            DataUser ownerUser = null;
            if (path == null) {
                hasError = true;
                response.setMsg("Debe espesificar una rruta");
                response.setStatus("error");
                response.setError("path missing");
            }

            if (!hasError) {
                HttpSession s = reqs.getSession(false);
                String uid = (String) s.getAttribute("uid");

                try {
                    ownerUser = (DataUser) up.getUserById(uid);
                } catch (UserException e) {
                    e.printStackTrace();

                    hasError = true;
                    response.setMsg(e.getMessage());
                    response.setStatus("error");
                    response.setError("user_error");
                }

            }

            if (!hasError) {
                if (!shp.isShared(ownerUser, Paths.get(path))) {
                    hasError = true;
                    response.setMsg("Esta rruta no se encuentra compartida");
                    response.setStatus("error");
                    response.setError("not_share");
                }
            }

            if (!hasError) {
                shp.setSharedWith(ownerUser, users, Paths.get(path));
                if (mode != null) {
                    shp.setMode(ownerUser, Paths.get(path), mode);
                }
            }
            resps.getWriter().println(om.writeValueAsString(response));
            ctx.complete();


        }

        public void setMode() throws IOException {
            JSONObject args = jsonArgs;
            Mode mode = args.has("mode") ? Mode.valueOf(args.getString("mode").toUpperCase()) : null;
            //List<Object> toUsers = args.has("users") ? args.getJSONArray("users").toList() : null;
            String path = args.has("path") ? args.getString("path") : null;

            boolean hasError = false;

            Response response = new Response();
            DataUser ownerUser = null;
            if (path == null) {
                hasError = true;
                response.setMsg("Debe espesificar una rruta");
                response.setStatus("error");
                response.setError("path missing");
            }

            if (!hasError) {
                HttpSession s = reqs.getSession(false);
                String uid = (String) s.getAttribute("uid");

                try {
                    ownerUser = (DataUser) up.getUserById(uid);
                } catch (UserException e) {
                    e.printStackTrace();

                    hasError = true;
                    response.setMsg(e.getMessage());
                    response.setStatus("error");
                    response.setError("user_error");
                }

            }

            if (!hasError) {
                if (!shp.isShared(ownerUser, Paths.get(path))) {
                    hasError = true;
                    response.setMsg("Esta rruta no se encuentra compartida");
                    response.setStatus("error");
                    response.setError("not_share");
                }
            }

            if (!hasError) {

                if (mode != null) {
                    shp.setMode(ownerUser, Paths.get(path), mode);
                }
            }

            resps.getWriter().println(om.writeValueAsString(response));
            ctx.complete();
        }

        public void get() throws IOException {
            JSONObject args = jsonArgs;
            //Mode mode = args.has("mode") ? Mode.valueOf(args.getString("mode").toUpperCase()) : null;
            //List<Object> toUsers = args.has("users") ? args.getJSONArray("users").toList() : null;
            String path = args.has("path") ? args.getString("path") : null;
            boolean wUsers = args.has("wusers") ? args.getBoolean("wusers") : false;


            boolean hasError = false;

            Response response = new Response();
            DataUser ownerUser = null;
            if (path == null) {
                hasError = true;
                response.setMsg("Debe espesificar una rruta");
                response.setStatus("error");
                response.setError("path missing");
            }

            if (!hasError) {
                HttpSession s = reqs.getSession(false);
                String uid = (String) s.getAttribute("uid");

                try {
                    ownerUser = (DataUser) up.getUserById(uid);
                } catch (UserException e) {
                    e.printStackTrace();

                    hasError = true;
                    response.setMsg(e.getMessage());
                    response.setStatus("error");
                    response.setError("user_error");
                }

            }

            if (!hasError) {
                if (!shp.isShared(ownerUser, Paths.get(path))) {
                    hasError = true;
                    response.setMsg("Esta rruta no se encuentra compartida");
                    response.setStatus("error");
                    response.setError("not_share");
                }
            }

            if (!hasError) {
                try {
                    orchi.HHCloud.share.Share share = shp.getShare(ownerUser, Paths.get(path), wUsers);
                    response.setPayload(share);
                } catch (NotShareException e) {
                    hasError = true;
                    response.setMsg(e.getMessage());
                    response.setStatus("error");
                    response.setError("not_share");
                }
            }

            resps.getWriter().println(om.writeValueAsString(response));
            ctx.complete();
        }
    }

    public static class FsOperations extends Operations {

        public FsOperations(AsyncContext ctx, JSONObject jsonArgs) {
            super(ctx, jsonArgs);
        }

        public void list() throws Exception {
            ListArguments listArgs = new ListArguments();
            Path sPath = Paths.get("/", Paths.get(jsonArgs.getString("spath")).normalize() + "");
            Path subPath = Paths.get("/", Paths.get(jsonArgs.getString("subpath")).normalize() + "");
            String idUser = jsonArgs.getString("owner");
            listArgs.setPath(Paths.get(sPath.normalize() + "/" + subPath.normalize()).normalize());
            DataUser u = (DataUser) Start.getUserManager().getUserProvider().getUserById(idUser);
            boolean hasError = false;
            Response response = new Response();

            if (shp.isShared(u, sPath)) {
                Mode pMode = shp.getMode(u, sPath);
                if (pMode == Mode.P) {

                    listArgs.setUser(u);
                    ListResponse res = sp.list(listArgs);

                    response.setPayload(res);
                    if (res.getStatus() == "error") {
                        hasError = true;
                        response.setStatus("error");
                        response.setError(res.getError());
                        response.setMsg(res.getMsg());
                    }

                } else if (pMode == Mode.U) {
                    HttpSession s = reqs.getSession(false);

                    if (s == null) {
                        hasError = true;
                        response.setError("session_require");
                        response.setStatus("error");
                        response.setMsg("Esta rruta esta compartida en modo privado con algunos usuarios, tienes que iniciar sesion y debe estar compartida contigo.");
                    } else {
                        DataUser u2 = (DataUser) Start.getUserManager().getUserProvider().getUserById((String) s.getAttribute("uid"));
                        if (shp.isSharedWith(u, u2, sPath) || shp.isShared(u2, sPath)) {
                            listArgs.setUser(u);
                            ListResponse res = sp.list(listArgs);

                            response.setPayload(res);
                            if (res.getStatus() == "error") {
                                hasError = true;
                                response.setStatus("error");
                                response.setError(res.getError());
                                response.setMsg(res.getMsg());
                            }

                        } else {
                            hasError = true;
                            response.setError("not_share_with_you");
                            response.setStatus("error");
                            response.setMsg("Esta rruta esta en modo privado y no se encuentra conpartida con tigo");

                        }
                    }

                }
            } else {
                hasError = true;
                response.setError("not_share");
                response.setStatus("error");
                response.setMsg("'" + sPath + "' Esta rruta no esta conpartida y no puedes acceder a '" + listArgs.getPath() + "'");

            }

            resps.getWriter().println(om.writeValueAsString(response));
            ctx.complete();
        }

        public void getStatus() throws Exception {
            list();
        }

        public void download() throws UserException, IOException {
            Path sPath = Paths.get("/", Paths.get(jsonArgs.getString("spath")).normalize() + "");
            Path subPath = Paths.get("/", Paths.get(jsonArgs.getString("subpath")).normalize() + "");
            String owner = jsonArgs.has("owner") ? jsonArgs.getString("owner") : null;
            Response response = new Response();

            boolean hasError = false;

            User u = null;
            if (owner != null) {
                try {
                    u = Start.getUserManager().getUserProvider().getUserById(owner);
                } catch (UserException e) {
                    e.printStackTrace();
                    hasError = true;
                    response.setMsg(e.getMessage());
                    response.setStatus("error");
                    response.setError("user_error");

                }
            }

            if (!hasError) {
                if (shp.isShared(u, sPath)) {
                    Mode pMode = shp.getMode(u, sPath);
                    if (pMode == Mode.P) {
                        DownloadArguments downArgs = new DownloadArguments();
                        downArgs.setPath(Paths.get(sPath.normalize() + "/" + subPath.normalize()).normalize());
                        downArgs.setUser(u);
                        downArgs.setCtx(ctx);
                        sp.download(downArgs);
                        return;

                    } else if (pMode == Mode.U) {
                        HttpSession s = reqs.getSession(false);
                        if (s == null) {
                            hasError = true;
                            response.setError("session_require");
                            response.setStatus("error");
                            response.setMsg("Esta rruta esta compartida en modo privado con algunos usuarios, tienes que iniciar sesion y debe estar compartida contigo.");
                        } else {
                            DataUser u2 = (DataUser) Start.getUserManager().getUserProvider().getUserById((String) s.getAttribute("uid"));
                            if (shp.isSharedWith(u, u2, sPath) || shp.isShared(u2, sPath)) {
                                DownloadArguments downArgs = new DownloadArguments();
                                downArgs.setPath(Paths.get(sPath.normalize() + "/" + subPath.normalize()).normalize());
                                downArgs.setUser(u);
                                downArgs.setCtx(ctx);
                                sp.download(downArgs);
                                return;

                            } else {
                                hasError = true;
                                response.setError("not_share_with_you");
                                response.setStatus("error");
                                response.setMsg("Esta rruta esta en modo privado y no se encuentra conpartida con tigo");

                            }
                        }

                    }
                } else {
                    hasError = true;
                    response.setError("not_share");
                    response.setStatus("error");
                    response.setMsg("'" + sPath + "' Esta rruta no esta conpartida y no puedes acceder a '" + Paths.get(sPath.normalize() + "/" + subPath.normalize()).normalize() + "'");

                }
            }

            resps.getWriter().println(om.writeValueAsString(response));
            ctx.complete();

        }

    }
}