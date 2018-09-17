package orchi.HHCloud.Api.Uploader;

import orchi.HHCloud.Api.API;
import orchi.HHCloud.Api.ServiceTaskAPIImpl;
import orchi.HHCloud.Api.annotations.Ignore;
import orchi.HHCloud.Api.annotations.SessionRequired;
import orchi.HHCloud.Start;
import orchi.HHCloud.quota.Exceptions.QuotaExceededException;
import orchi.HHCloud.store.response.UploaderResponse;
import orchi.HHCloud.user.BasicUser;
import orchi.HHCloud.user.User;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Ignore
@SessionRequired
public class Uploader extends API {
    private static Logger log = LoggerFactory.getLogger(Uploader.class);
    public static String apiName = "/uploader";
    private static String ACCESS_CONTROL_ALLOW_ORIGIN = Start.conf.getString("api.headers.aclo");
    private ThreadPoolExecutor executor;
    private static ObjectMapper om;

    @Override
    public void init(ServletConfig config) throws ServletException {
        executor = new ThreadPoolExecutor(1000, 10000, 50000L, TimeUnit.MICROSECONDS,
                new LinkedBlockingQueue<Runnable>(100000));
        om = new ObjectMapper();
        om.enable(org.codehaus.jackson.map.SerializationConfig.Feature.INDENT_OUTPUT);
        om.getJsonFactory();

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
        resp.setHeader("Access-Control-Allow-Origin", ACCESS_CONTROL_ALLOW_ORIGIN);
        resp.setHeader("Access-Control-Allow-Headers","X-PINGOTHER, Content-Type");
        resp.setHeader("Access-Control-Allow-Methods","POST");

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AsyncContext aContext = req.startAsync();
        aContext.setTimeout(Long.MAX_VALUE);
        executor.execute(new Task(aContext));
    }

    public static class Task extends ServiceTaskAPIImpl implements Runnable {

        public Task(AsyncContext ctx) {
            super(ctx);

        }

        private void proceess() {

            HttpServletRequest req = (HttpServletRequest) getCtx().getRequest();
            HttpServletResponse resp = (HttpServletResponse) getCtx().getResponse();
            boolean isMultipart = ServletFileUpload.isMultipartContent(req);
            UploaderResponse response = new UploaderResponse();
            resp.setHeader("Access-Control-Allow-Credentials", "true");
            resp.setHeader("Access-Control-Allow-Origin", ACCESS_CONTROL_ALLOW_ORIGIN);
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
                        if (name.equalsIgnoreCase("args")) {
                            String value = Streams.asString(stream);
                            String valueParamUrlDecoded = null;
                            try {
                                valueParamUrlDecoded = URLDecoder.decode(value, "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                valueParamUrlDecoded = value;
                                log.error("Name character encoding is not supported, the value is no modified {}", value);
                            }
                            log.debug("Param {}\nvalue {}\ndecoded {}", name, value, valueParamUrlDecoded);
                            args = new JSONObject(valueParamUrlDecoded);
                            process.setPathArgs(args.getString("path"));
                        }
                        System.out.println("Form field " + name + " with value " + " detected.");
                    } else {
                        if (name.equalsIgnoreCase("f")) {
                            System.out
                                    .println("File field " + name + " with file name " + item.getName() + " detected.");
                            process.toStore(path, stream, req);
                        }

                    }
                }
                response.setStatus("ok");
                response.setPath(Paths.get(args.getString("path")));
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

    /**
     * clase para proceso de subida, en el proceso de parseo, los parametros,
     * args,op,f, se leen en ese order, pudiendo haci crear un proceso de
     * armado, donde primero se obtiene de args la propiedad path, q es donde se
     * va ha almacenar dicho archivo, luego de tener el path de args, se llama
     * el metodo, toStore q con pathArgs y el path parasado por parametro se
     * arma la rruta de destino completa,
     * <p>
     * <pre>
     * pathArgs = rruta donde se encuentra el usuario, /mi musica
     * path = nombre del archivo, junto con la rruta relativa, artistas/coldplay/albums/parachute/trouble.mp3
     *
     * juntas = /mi musica/artistas/coldplay/albums/parachute/trouble.mp3
     *
     * teniendo esa rruta, se crea una nueva rruta de hdfs y se obtiene la rruta completa, con el contexto de almacenamiento de el usuario
     * quedando = /mi_dfs/${user_id}/files/mi musica/artistas/coldplay/albums/parachute/trouble.mp3
     * q sera la rruta a usarse para pasar a el metodo create de el proveedor de almacenamiento.
     * </pre>
     */
    private static class Process {
        private String pathArgs;
        private String pathFilename;

        public void toStore(String path, InputStream in, HttpServletRequest req) throws Exception {
            pathFilename = path;
            HttpSession session = req.getSession(false);

            if (session != null) {
                java.nio.file.Path p = Paths.get(pathArgs, path);
                String root = (String) session.getAttribute("uid");
                User user = new BasicUser();
                user.setId(root);
                Start.getStoreManager().getStoreProvider().create(user, p, in);
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
