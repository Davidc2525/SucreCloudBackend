package orchi.HHCloud.Api.Opener;

import orchi.HHCloud.Api.API;
import orchi.HHCloud.Api.ServiceTaskAPIImpl;
import orchi.HHCloud.Api.annotations.Ignore;
import orchi.HHCloud.Start;
import orchi.HHCloud.share.Mode;
import orchi.HHCloud.share.ShareProvider;
import orchi.HHCloud.store.StoreProvider;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.User;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URLDecoder;


@Ignore
//@SessionRequired
public class Opener extends API {
    public static String apiName = "/opener";
    private static Logger log = LoggerFactory.getLogger(Opener.class);
    private static Long sizeRange = Start.conf.getLong("api.openner.range.size");
    //private static String ACCESS_CONTROL_ALLOW_ORIGIN = Start.conf.getString("api.headers.aclo");
    private static Long readedParts = 0L;
    private static StoreProvider sp;
    private static ShareProvider shp;
    private ThreadPoolExecutor executor;

    @Override
    public void init(ServletConfig config) throws ServletException {
        sp = Start.getStoreManager().getStoreProvider();
        executor = new ThreadPoolExecutor(1000, 10000, 50000L, TimeUnit.MICROSECONDS,
                new LinkedBlockingQueue<Runnable>(100000));

        shp = Start.getShareManager().getShareProvider();

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        executor.execute(new Task(req.startAsync()));
        //CompletableFuture.runAsync(new Task(req.startAsync()));

    }

    public static class Task extends ServiceTaskAPIImpl implements Runnable {

        public Task(AsyncContext ctx) {
            super(ctx);
            getCtx().setTimeout(Long.MAX_VALUE);
        }

        @Override
        public void run() {
            HttpServletRequest req = (HttpServletRequest) getCtx().getRequest();
            HttpServletResponse resp = (HttpServletResponse) getCtx().getResponse();

           // resp.setHeader("Access-Control-Allow-Origin", ACCESS_CONTROL_ALLOW_ORIGIN);
            resp.setHeader("Content-type", "application/json");
            resp.setHeader("Access-Control-Allow-Credentials", "true");
            resp.setHeader("Accept-Ranges", "bytes");
            // ParseParamsMultiPart2 p = (ParseParamsMultiPart2)
            // reqs.getAttribute("params");

            try {
                //params = new ParseParamsMultiPart2(req);
                //JsonArgs = new JSONObject(params.getString("args"));
                checkAvailability(apiName, null,false);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            log.debug("Abrir contenido de archivo.");

            HttpSession session = req.getSession(false);
            User ownerUser = new DataUser();
            String path = null;

            try {
                ParseOpenerParams params = new ParseOpenerParams(req);
                String type = "n";
                if (params.params.containsKey("type")) {
                    type = params.params.get("type");
                }

                if (type.equalsIgnoreCase("s")) {
                    String prePath = null;

                    if (!params.params.containsKey("owner")
                            && !params.params.containsKey("spath")
                            /*&& !params.params.containsKey("subpath")*/) {

                        sendError("missing_param", "Hace falta parametros");
                    }
                    String encodeOwner = params.params.get("owner");
                    String decodeOwner = new decodeParam(encodeOwner).decoded;

                    String endodeSPath = params.params.get("spath");
                    String decodeSPath = new decodeParam(endodeSPath).decoded;

                    String decodeSubPath = "";
                    if(params.params.containsKey("subpath")){
                        String endodeSubPath = params.params.get("subpath");
                        decodeSubPath = new decodeParam(endodeSubPath).decoded;
                    }

                    Path sPath = Paths.get("/", Paths.get(decodeSPath).normalize() + "");
                    Path subPath = Paths.get("/", Paths.get(decodeSubPath).normalize() + "");

                    prePath = Paths.get(sPath.normalize() + "/" + subPath.normalize()).normalize() + "";

                    ownerUser.setId(decodeOwner);
                    String viewUserId = "";
                    User viewUser = null;

                    ownerUser = Start.getUserManager().getUserProvider().getUserById(decodeOwner);

                    if (shp.isShared(ownerUser, sPath)) {
                        Mode mode = shp.getMode(ownerUser, sPath);
                        if (mode == Mode.P) {
                            path = prePath;
                        } else if (mode == Mode.U) {
                            if (session != null) {
                                viewUserId = (String) session.getAttribute("uid");
                                viewUser = Start.getUserManager().getUserProvider().getUserById(viewUserId);
                                if (shp.isSharedWith(ownerUser, viewUser, sPath) || ownerUser.equals(viewUser)) {
                                    path = prePath;
                                }
                            } else {
                                sendError("mising_session", "Esta rruta esta en modo privado, tienes que inciar sesion");
                            }
                        }
                    }
                } else if (type.equalsIgnoreCase("n")) {
                    if (session != null) {
                        ownerUser = ownerUser = Start.getUserManager().getUserProvider().getUserById((String) session.getAttribute("uid"));

                        String endodePath = params.params.get("path");
                        String decodePath = new decodeParam(endodePath).decoded;
                        path = decodePath;
                        log.debug("         OPENNER: {}",path);                        
                    } else {
                        sendError("mising_session", "Debe iniciar sesion");
                    }

                }

                if (path == null) {
                    sendError("mising_path", "No se ha generado una rruta, corriga argumentos");
                }

                ParseOpenerHeaders headers = new ParseOpenerHeaders(req);
                if (headers.headers.containsKey("Range")) {
                    String hRange = new ParseOpenerHeaders(req).headers.get("Range");
                    Long fileSize = sp.getSize(ownerUser, Paths.get(path));
                    String mime = Files.probeContentType(Paths.get(path.toString()));

                    orchi.HHCloud.store.Range range = new orchi.HHCloud.store.Range(hRange, fileSize);
                    Long[] ranges = range.range;
                    long contentLength = range.getContentLength();

                    log.debug("contenido parcial");
                    log.debug("cabesera {}", hRange);
                    //log.debug("ruta decodificada {}", decodePath);
                    log.debug("rango de consulta {} {}", ranges[0], ranges[1]);
                    log.debug("tamaño de contenido de salida {}", contentLength);
                    log.debug("tamaño del contenido total {}", fileSize);
                    log.debug("tipo de mime {}", mime);
                    log.debug("Usuario {}", ownerUser.getId());
                    log.debug("------------- {}", ++readedParts);

                    resp.setStatus(HttpResponseStatus.PARTIAL_CONTENT.getCode());
                    resp.setHeader("Content-Length", contentLength + "");
                    resp.setHeader("Content-Range", "bytes " + ranges[0] + "-" + ranges[1] + "/" + fileSize);
                    resp.setHeader("Content-Type", mime);
                    sp.read(ownerUser, Paths.get(path), range, resp.getOutputStream());
                    getCtx().complete();

                } else {

                    String mime = Files.probeContentType(Paths.get(path.toString()));
                    Long fileSize = sp.getSize(ownerUser, Paths.get(path));

                    log.debug("contenido total");
                    //log.debug("ruta decodificada {}", decodePath);
                    log.debug("tamaño del contenido total {}", fileSize);
                    log.debug("tipo de mime {}", mime);
                    log.debug("Usuario {}", ownerUser.getId());
                    log.debug("------------- {}", ++readedParts);

                    resp.setHeader("Content-Length", fileSize + "");
                    resp.setHeader("Content-Type", mime);
                    resp.addHeader("Content-Disposition", "filename=\"" + Paths.get(path).getFileName() + "\"");


                    sp.read(ownerUser, Paths.get(path), resp.getOutputStream());
                    getCtx().complete();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
                try {

                    sendError("error_server", e1);
                } catch (Exception e) {
                    getCtx().complete();
                    e.printStackTrace();
                }
                getCtx().complete();
                e1.printStackTrace();
            }


        }

    }

    public static class ParseOpenerParams {
        public Map<String, String> params = new HashMap<>();
        private HttpServletRequest r;

        public ParseOpenerParams(HttpServletRequest r) {
            this.r = r;

            Enumeration<String> nps = r.getParameterNames();
            while (nps.hasMoreElements()) {
                String name = nps.nextElement();
                params.put(name, this.r.getParameter(name));
            }
        }
    }

    public static class ParseOpenerHeaders {
        public Map<String, String> headers = new HashMap<>();
        private HttpServletRequest r;

        public ParseOpenerHeaders(HttpServletRequest r) {
            this.r = r;

            Enumeration<String> headerNames = r.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String hName = headerNames.nextElement();
                headers.put(hName, this.r.getHeader(hName));
            }
        }
    }

    public static class Range {

        public Long[] range = {0L, 0L};
        private String reg = "(?:bytes)=(\\d+)-(\\d+)?";
        private Pattern pattern = Pattern.compile(reg);
        private long contentLength;

        public Range(String headerRange, Long fileSize) {
            //System.out.println(headerRange);
            Matcher m = pattern.matcher(headerRange);
            // System.out.println(m.matches());
            if (m.matches()) {
                range[0] = Long.valueOf(m.group(1));
                if (m.group(2) == null) {
                    range[1] = (sizeRange);
                }
                if (m.group(2) != null) {
                    range[1] = Long.valueOf(m.group(2));
                }
                if (range[0] > range[1]) {
                    range[1] = range[0] + range[1];
                }

                if (range[1] > fileSize) {
                    range[1] = fileSize - 1;
                }

                setContentLength(range[1] - range[0] + 1);

            }

        }

        /**
         * @return the contentLength
         */
        public long getContentLength() {
            return contentLength;
        }

        /**
         * @param contentLength the contentLength to set
         */
        public void setContentLength(long contentLength) {
            this.contentLength = contentLength;
        }

    }

    public static class decodeParam {
        public String decoded = "";
        private String p;

        public decodeParam(String param) throws Exception {
            this.p = param;

            if (p != null && !p.equalsIgnoreCase("")) {
                String[] parts = p.split(":");

                for (String part : parts) {
                    
                    String _char = Character.toString( ( Character.toChars( Integer.valueOf(part, 16) ) )[0]);
                    log.debug("          caracter: {}",_char);
                    decoded += _char;
                }

                try {
                   //decoded = URLDecoder.decode(decoded, "UTF-8");
                   log.debug("      decoded: {} ",decoded);
                } catch (Exception e) {

                    log.error("Name character encoding is not supported, the value is no modified {}", decoded);
                }

            } else {
                throw new Exception("La cadena no puede ser null o estar vacia "+p);
            }

        }
    }

}
