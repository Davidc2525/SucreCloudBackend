package orchi.HHCloud.Api.Avatar;

import orchi.HHCloud.Api.API;
import orchi.HHCloud.Api.ServiceTaskAPIImpl;
import orchi.HHCloud.Api.annotations.Ignore;
import orchi.HHCloud.ParseParamsMultiPart2;
import orchi.HHCloud.Start;
import orchi.HHCloud.quota.Exceptions.QuotaExceededException;
import orchi.HHCloud.store.StoreProvider;
import orchi.HHCloud.store.response.UploaderResponse;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.User;
import orchi.HHCloud.user.UserProvider;
import orchi.HHCloud.user.avatar.Bound;
import orchi.HHCloud.user.avatar.Exceptions.DeleteAvatarException;
import orchi.HHCloud.user.avatar.Exceptions.GetAvatarException;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Ignore
public class Avatar extends API {
    public static Logger log = LoggerFactory.getLogger(Avatar.class);
    public static String apiName = "/avatar";
    private static UserProvider up;
    private static ObjectMapper om;
    private StoreProvider sp;
    private ThreadPoolExecutor executor;

    @Override
    public void init(ServletConfig config) throws ServletException {
        sp = Start.getStoreManager().getStoreProvider();
        up = Start.getUserManager().getUserProvider();
        executor = new ThreadPoolExecutor(1000, 10000, 50000L, TimeUnit.MICROSECONDS,
                new LinkedBlockingQueue<Runnable>(100000));

        om = new ObjectMapper();
        om.enable(org.codehaus.jackson.map.SerializationConfig.Feature.INDENT_OUTPUT);
        om.getJsonFactory();
    }

    /**
     * show image avatar
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        executor.execute(new Task(req.startAsync()));
    }

    /**
     * set imate avatar
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        executor.execute(new TaskPost(req.startAsync()));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        executor.execute(new TaskDelete(req.startAsync()));
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /**Access-Control-Request-Method: POST
         Access-Control-Request-Headers: X-PINGOTHER, Content-Type

         Access-Control-Allow-Origin: http://foo.example
         Access-Control-Allow-Methods: POST
         Access-Control-Allow-Headers: X-PINGOTHER, Content-Type
         Access-Control-Max-Age: 86400*/

        resp.setHeader("Access-Control-Allow-Credentials", "true");
        //resp.setHeader("Access-Control-Allow-Origin", ACCESS_CONTROL_ALLOW_ORIGIN);
        resp.setHeader("Access-Control-Allow-Headers", "X-PINGOTHER, Content-Type");
        resp.setHeader("Access-Control-Allow-Methods", "POST,DELETE");

        AsyncContext aContext = req.startAsync();
        aContext.setTimeout(Long.MAX_VALUE);
        executor.execute(new Task2(aContext));
    }

    public static class Task2 extends ServiceTaskAPIImpl implements Runnable {

        public Task2(AsyncContext ctx) {
            super(ctx);

        }

        private void proceess() {

            HttpServletRequest req = (HttpServletRequest) getCtx().getRequest();
            HttpServletResponse resp = (HttpServletResponse) getCtx().getResponse();
            boolean isMultipart = ServletFileUpload.isMultipartContent(req);
            UploaderResponse response = new UploaderResponse();
            resp.setHeader("Access-Control-Allow-Credentials", "true");
            //resp.setHeader("Access-Control-Allow-Origin", ACCESS_CONTROL_ALLOW_ORIGIN);
            resp.setHeader("Content-type", "application/json");


            // parse request
            try {
                checkAvailability(apiName, null);

                getCtx().complete();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    sendError("server_error", e);
                } catch (Exception e1) {
                    getCtx().complete();
                    e1.printStackTrace();
                }

            }
        }

        @Override
        public void run() {
            proceess();
        }

    }

    public static class TaskPost extends ServiceTaskAPIImpl implements Runnable {


        public TaskPost(AsyncContext ctx) {
            super(ctx);
        }

        private void proceess() {

            HttpServletRequest req = (HttpServletRequest) getCtx().getRequest();
            HttpServletResponse resp = (HttpServletResponse) getCtx().getResponse();
            boolean isMultipart = ServletFileUpload.isMultipartContent(req);
            UploaderResponse response = new UploaderResponse();
            resp.setHeader("Access-Control-Allow-Credentials", "true");
            //resp.setHeader("Access-Control-Allow-Origin", ACCESS_CONTROL_ALLOW_ORIGIN);
            resp.setHeader("Content-type", "application/json");

            // crear un nuevo manejador de subida
            ServletFileUpload upload = new ServletFileUpload();

            // parse request
            try {
                checkAvailability(apiName, null);
                FileItemIterator iter = upload.getItemIterator(req);
                Process process = new Process();
                JSONObject args = null;
                while (iter.hasNext()) {
                    FileItemStream item = iter.next();
                    String name = item.getFieldName();
                    String path = item.getName();
                    InputStream stream = item.openStream();

                    if (item.isFormField()) {

                    } else {
                        if (name.equalsIgnoreCase("o")) {
                            System.out
                                    .println("File field " + name + " with file group " + item.getName() + " detected.");

                        }
                        if (name.equalsIgnoreCase("f")) {
                            System.out
                                    .println("File field " + name + " with file group " + item.getName() + " detected.");
                            process.toStore(path, stream, req);
                        }


                    }
                }
                response.setStatus("ok");
                //response.setPath(Paths.get(args.getString("path")));
                response.setMsg(String.format("%s subido sastifactoriamente.", process.getInfo()));
                resp.getWriter().print(om.writeValueAsString(response));
                getCtx().complete();
            } catch (QuotaExceededException e) {
                e.printStackTrace();
                try {
                    sendError("quota_exceeded", e);
                } catch (Exception e1) {
                    getCtx().complete();
                    e1.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    sendError("upload_error", e);
                } catch (Exception e1) {
                    getCtx().complete();
                    e1.printStackTrace();
                }

            }

        }

        @Override
        public void run() {
            proceess();
        }
    }

    public static class TaskDelete extends ServiceTaskAPIImpl implements Runnable {


        public TaskDelete(AsyncContext ctx) {
            super(ctx);
        }

        private void proceess() {

            HttpServletRequest req = (HttpServletRequest) getCtx().getRequest();
            HttpServletResponse resp = (HttpServletResponse) getCtx().getResponse();
            boolean isMultipart = ServletFileUpload.isMultipartContent(req);
            UploaderResponse response = new UploaderResponse();
            resp.setHeader("Access-Control-Allow-Credentials", "true");
            //resp.setHeader("Access-Control-Allow-Origin", ACCESS_CONTROL_ALLOW_ORIGIN);
            resp.setHeader("Content-type", "application/json");

            HttpSession session = req.getSession(false);
            try {
                checkAvailability(apiName, null);


                if (session != null) {
                    Process process = new Process();
                    String root = (String) session.getAttribute("uid");
                    User user = new DataUser();
                    user.setId(root);
                    //java.nio.file.Path p = Paths.get("avatars",user.getId());
                    //Start.getStoreManager().getStoreProvider().create(p, in);
                    try {
                        Start.getUserManager().getUserProvider().getAvatarProvider().delete(user);
                        response.setStatus("ok");
                        resp.getWriter().print(om.writeValueAsString(response));
                    } catch (DeleteAvatarException e) {
                        e.printStackTrace();
                        try {
                            sendError("user_exception", e);
                        } catch (Exception e1) {
                            getCtx().complete();
                            e1.printStackTrace();
                        }
                    }

                } else {
                    log.error("no tiene session");
                }


                getCtx().complete();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    sendError("server_error", e);
                } catch (Exception e1) {
                    getCtx().complete();
                    e1.printStackTrace();
                }

            }

        }

        @Override
        public void run() {
            proceess();
        }
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
                e.printStackTrace();
                try {
                    sendError(e.getMessage());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            } catch (GetAvatarException e) {
                e.printStackTrace();
                try {
                    sendError(e.getMessage());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

        }

        private void process() throws Exception, GetAvatarException {

            HttpServletRequest reqs = (HttpServletRequest) getCtx().getRequest();
            HttpServletResponse resps = (HttpServletResponse) getCtx().getResponse();

            //resps.setHeader("Access-Control-Allow-Origin", ACCESS_CONTROL_ALLOW_ORIGIN);
            //resps.setHeader("Content-type", "image/jpg");
            //resps.setHeader("cache-control", "max-age=31536000");
            String preEtag = reqs.getHeader("If-None-Match");

            JSONObject jsonArgs = null;
            ParseParamsMultiPart2 p = null;
            String id = "none";
            String bound = "0x0";
            try {
                p = new ParseParamsMultiPart2(reqs);
                id = p.getString("id");
                if (id == null) {
                    id = "none";
                }
                id = id.toLowerCase();

                bound = p.getString("size");
                if (bound == null) {
                    bound = "0x0";
                }
                checkAvailability(apiName, null, false);
            } catch (Exception e) {

                e.printStackTrace();
                sendError(e.getMessage());
                return;
            }


            resps.setHeader("Content-type", "image/jpg");
            DataUser user = new DataUser();
            user.setId(id);
            orchi.HHCloud.user.avatar.Avatar avatar = up.getAvatarProvider().get(user, Bound.from(bound));


            String hashtext = avatar.getHash();
            if (preEtag != null) {
                if (preEtag.equals(hashtext)) {
                    resps.setStatus(304);
                } else {
                    sendImage(avatar, hashtext);
                }
            } else {
                sendImage(avatar, hashtext);
            }
            getCtx().complete();
        }

        private void sendImage(orchi.HHCloud.user.avatar.Avatar avatar, String hashtext) throws IOException {
            System.out.println("send Image");
            HttpServletRequest reqs = (HttpServletRequest) getCtx().getRequest();
            HttpServletResponse resps = (HttpServletResponse) getCtx().getResponse();
            String fileName = "";
            Bound bound = avatar.getBound();
            fileName += avatar.getId();
            fileName += "_";
            fileName += bound.getWidth();
            fileName += "x";
            fileName += bound.getHeight();
            fileName += ".jpg";

            resps.setHeader("Content-type", "image/jpg");
            resps.setHeader("cache-control", "no-cache, max-age=86400");
            resps.setHeader("Content-Disposition", "filename=\"" + fileName + "\"");
            resps.setHeader("etag", hashtext);

            avatar.readStream(getCtx().getResponse().getOutputStream());
            getCtx().getResponse().getOutputStream().flush();
            getCtx().getResponse().getOutputStream().close();


        }
    }


    private static class Process {
        private String pathArgs;
        private String pathFilename;

        public void toStore(String path, InputStream in, HttpServletRequest req) throws Exception {
            pathFilename = path;
            HttpSession session = req.getSession(false);

            if (session != null) {
                String root = (String) session.getAttribute("uid");
                User user = new DataUser();
                user.setId(root);
                //java.nio.file.Path p = Paths.get("avatars",user.getId());
                //Start.getStoreManager().getStoreProvider().create(p, in);
                Start.getUserManager().getUserProvider().getAvatarProvider().set(user, in);
            } else {
                log.error("no tiene session");
            }
        }

        public String getInfo() {
            return Paths.get(pathArgs, pathFilename) + "";
        }

        public String getPathArgs() {
            return pathArgs;
        }

        public void setPathArgs(String pathArgs) {
            this.pathArgs = pathArgs;
        }

        public String getPathFilename() {
            return pathFilename;
        }

        public void setPathFilename(String pathFilename) {
            this.pathFilename = pathFilename;
        }

    }
}
