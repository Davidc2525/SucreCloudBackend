package orchi.HHCloud.Api.Auth;

import com.google.api.client.util.Base64;
import orchi.HHCloud.Api.API;
import orchi.HHCloud.Api.ServiceTaskAPIImpl;
import orchi.HHCloud.Api.annotations.Operation;
import orchi.HHCloud.ParseParamsMultiPart2;
import orchi.HHCloud.Start;
import orchi.HHCloud.auth.Exceptions.VerifyException;
import orchi.HHCloud.mail.MailProvider;
import orchi.HHCloud.user.EmbeddedUserProvider;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.lang.text.StrSubstitutor;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.mortbay.log.Log;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Operation(name = "login", isRequired = false)
@Operation(name = "logout", isRequired = true)
@Operation(name = "verifyemail")
@Operation(name = "verifyemailpage")
/**
 * Api para autenticacion
 * */
public class Auth extends API {
    public static String apiName = "/auth";
    private static String appDomain = Start.conf.getString("app.host");
    //private static String ACCESS_CONTROL_ALLOW_ORIGIN = Start.conf.getString("api.headers.aclo");
    private static ThreadPoolExecutor executorw2;
    private static Logout logout;
    private static Login login;
    private static ObjectMapper om;
    private static String emailVerifyPage = null;

    public Auth() {
        login = new Login();
        logout = new Logout();
        executorw2 = new ThreadPoolExecutor(1000, 10000, 50000L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(10000));
        om = new ObjectMapper();
        om.enable(org.codehaus.jackson.map.SerializationConfig.Feature.INDENT_OUTPUT);
        om.getJsonFactory();

        try {
            emailVerifyPage = Streams.asString(EmbeddedUserProvider.class.getResourceAsStream("/verifyemail.html"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() throws ServletException {
        login.init();
        logout.init();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        doPost(req, resp);

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        executorw2.execute(new Task(req.startAsync()));
        System.err.println("tarea en proceso " + Thread.currentThread());

    }

    public Task getTask() {
        return new Task(null);
    }

    public static class JsonResponse {
        private String status = "ok";
        private String error = null;
        private Boolean vrified = false;
        private String msg = "N/A";

        public JsonResponse(Boolean verified, String msg) {
            this.setVrified(verified);
            this.msg = msg;
        }

        public String getMsg() {
            return msg;
        }

        public String getStatus() {
            return status;
        }

        public JsonResponse setStatus(String status) {
            this.status = status;
            return this;
        }

        public String getError() {
            return error;
        }

        public JsonResponse setError(String error) {
            this.error = error;
            return this;
        }

        public Boolean getVrified() {
            return vrified;
        }

        public void setVrified(Boolean vrified) {
            this.vrified = vrified;
        }

    }

    public static class Task extends ServiceTaskAPIImpl implements Runnable {

        public Task(AsyncContext ctx) {
            super(ctx);
        }

        public void writeResponse(JsonResponse responseContent) {
            try {

                ((HttpServletResponse) getCtx().getResponse()).setHeader("Access-Control-Allow-Credentials", "true");
                //((HttpServletResponse) getCtx().getResponse()).setHeader("Access-Control-Allow-Origin",ACCESS_CONTROL_ALLOW_ORIGIN);
                ((HttpServletResponse) getCtx().getResponse()).setHeader("Content-type", "application/json");
                //((HttpServletResponse) getCtx().getResponse()).setHeader("Access-Control-Allow-Origin", "*");
                ((HttpServletResponse) getCtx().getResponse()).setHeader("Content-encoding", "gzip");

                // ctx.getResponse().getWriter().print(om.writeValueAsString(data));
                om.writeValue(new GzipCompressorOutputStream(getCtx().getResponse().getOutputStream()),
                        responseContent);
                getCtx().complete();
            } catch (IOException e) {

                e.printStackTrace();
                getCtx().complete();
            }

        }

        @Override
        public void run() {

            HttpServletRequest req = ((HttpServletRequest) getCtx().getRequest());
            HttpServletResponse resp = ((HttpServletResponse) getCtx().getResponse());
            ParseParamsMultiPart2 p = null;
            try {
                p = new ParseParamsMultiPart2(req);
                checkAvailability(apiName, p.getString("op"));
            } catch (Exception e) {
                try {
                    sendError("server_error", e);
                } catch (Exception e1) {
                    getCtx().complete();
                    e1.printStackTrace();
                    return;

                }
                e.printStackTrace();
                return;
            }
            // ParseParamsMultiPart2 p =(ParseParamsMultiPart2)
            // req.getAttribute("params");

            JSONObject jsonArgs = null;
            ;
            try {
                jsonArgs = new JSONObject(p.getString("args"));
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            Log.info("{}", jsonArgs.toString(2));

            String op = null;
            op = p.getString("op");
            op = op != null ? op : "none";

            switch (op) {
                case "login":
                    executorw2.execute(new orchi.HHCloud.Api.Auth.Login.Task(getCtx()));
                    break;
                case "logout":
                    executorw2.execute(new orchi.HHCloud.Api.Auth.Logout.Task(getCtx()));
                    break;

                case "verifyemailpage":
                    HttpServletResponse response = ((HttpServletResponse) getCtx().getResponse());
                    HttpServletRequest request = (HttpServletRequest) getCtx().getRequest();
                    response.setHeader("Content-type","text/html");

                    String pagedefaultLocationRedirect = String.format("%s/SC/account", appDomain);
                    String pageidVeryfy = jsonArgs.has("token") ? jsonArgs.getString("token") : null;
                    boolean pageredirect = jsonArgs.has("redirect") ? jsonArgs.getBoolean("redirect") : false;
                    String pageredirectTo = jsonArgs.has("redirectTo") ? jsonArgs.getString("redirectTo") : pagedefaultLocationRedirect;


                    MailProvider mp = Start.getMailManager().getProvider();

                    String protocol = Start.conf.getString("api.protocol");
                    String host = Start.conf.getString("app.host");
                    String apiHost = Start.conf.getString("api.host");
                    int apiPort = Start.conf.getInt("api.port");
                    String apiUrl = String.format("%s://%s:%s", protocol,apiHost, apiPort);
                    String appUrl = host;
                    String args = request.getParameter("args");
                    String url = apiUrl + "/api/auth?op=verifyemail&args=" + args;
                    String appName = Start.conf.getString("app.name");

                    Map<String, String> values = new HashMap<String, String>();
                    values.put("url", url);
                    values.put("args", jsonArgs.toString(2));
                    values.put("redirect", Boolean.toString(pageredirect));
                    values.put("redirectTo", pageredirectTo);
                    values.put("appUrl", appUrl);
                    values.put("appName", appName);
                    String page = Auth.createBody(values, emailVerifyPage);

                    try {
                        response.getWriter().write(page);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    getCtx().complete();
                    break;

                case "verifyemail":
                    String defaultLocationRedirect = String.format("%s/SC/account", appDomain);
                    // verifyEmailOperation(jsonArgs);
                    String idVeryfy = jsonArgs.has("token") ? jsonArgs.getString("token") : null;
                    boolean redirect = jsonArgs.has("redirect") ? jsonArgs.getBoolean("redirect") : false;
                    String redirectTo = jsonArgs.has("redirectTo") ? jsonArgs.getString("redirectTo") : defaultLocationRedirect;

                    if (idVeryfy == null) {
                        writeResponse(new JsonResponse(false, "No se ha resivido ningun token")
                                .setError("token_missing")
                                .setStatus("error"));
                        return;
                    }

                    try {
                        Start.getAuthProvider().verifyEmail(idVeryfy);
                        if (redirect) {
                            resp.setStatus(HttpResponseStatus.FOUND.getCode());
                            resp.setHeader("Location", redirectTo);
                        }
                        writeResponse(new JsonResponse(true, "email verificado"));
                        getCtx().complete();
                    } catch (VerifyException e) {

                        writeResponse(
                                new JsonResponse(false, e.getMessage())
                                        .setError("verify_exception")
                                        .setStatus("error"));

                        e.printStackTrace();
                    }
                    break;

                default:
                    writeResponse(new JsonResponse(false, "debe espesificar una operacion").setError("operation_missing")
                            .setStatus("error"));
                    break;
            }

        }

    }

    private static String createBody(Map<String, String> values, String template) {
        StrSubstitutor sub = new StrSubstitutor(values);
        sub.setVariablePrefix("${{");
        sub.setVariableSuffix("}}");
        String resolvedString = sub.replace(template);
        return resolvedString;
    }

}
