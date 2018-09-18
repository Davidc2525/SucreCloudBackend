/**
 * ServiceTaskAPIImpl.java
 */
package orchi.HHCloud.Api;

import orchi.HHCloud.Api.ApiManager.ApiDescriptor;
import org.json.JSONObject;
import org.mortbay.log.Log;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Debido a que el {@link ApiFilter} es bloqueante, con esta clase en cada api
 * se debe crear una tarea que se ejecuta dentro del contexto asincrono del
 * servlet separado de el hilo de la peticion, haciendo q la operacion de
 * administracion de las apis no sea bloqueante, ya q dentro de si se ejecutaran
 * con {@link ServiceTaskAPIImpl#checkAvailability(String, String)} las
 * validaciones correspondientes.
 *
 * @author david 14 ago. 2018
 */
public abstract class ServiceTaskAPIImpl implements ServiceTaskAPIInterface {

    private AsyncContext ctx;

    public ServiceTaskAPIImpl(AsyncContext ctx) {
        this.ctx = ctx;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * orchi.HHCloud.Api.ServiceTaskAPIInterface#sendError(java.lang.String)
     */
    @Override
    public void sendError(String e) throws Exception {
        sendError("server_error", new Exception(e));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * orchi.HHCloud.Api.ServiceTaskAPIInterface#sendError(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void sendError(String error, String ex) throws Exception {
        sendError(error, new Exception(ex));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * orchi.HHCloud.Api.ServiceTaskAPIInterface#sendError(java.lang.String,
     * java.lang.Exception)
     */
    @Override
    public void sendError(String error, Exception e) throws Exception {
        HttpServletRequest reqs = (HttpServletRequest) getCtx().getRequest();
        HttpServletResponse resps = (HttpServletResponse) getCtx().getResponse();

        JSONObject json = new JSONObject();
        json.put("status", "error");
        json.put("error", error);
        json.put("errorMsg", e.getMessage());
        json.put("msg", e.getMessage());
        resps.getWriter().println(json.toString(2));

        getCtx().complete();

        throw e;
    }

    /**
     * Ejecuta las validaciones de las apis, haciendo uso de la informacion
     * recabada por {@link ApiManager}
     *
     * @param apiName       nombre de la api
     * @param operationName nombre de la operacion dentro de la api
     * @author david
     **/
    @Override
    public void checkAvailability(String apiName, String operationName) throws Exception {
        HttpServletRequest reqs = (HttpServletRequest) getCtx().getRequest();
        HttpSession session = reqs.getSession(false);
        ApiDescriptor apid = ApiManager.getApid(apiName);
        orchi.HHCloud.Api.ApiManager.Operation op = null;

        if (apid != null) {

            if (!apid.isIgnored()) {
                if (apid.isGsr()) {
                    if (session == null) {
                        apid.wrongCalls++;
                        sendError("session", "Nesesitas iniciar session para usar esta api.");
                    } else {
                        apid.calls++;
                    }
                } else {
                    apid.calls++;
                }
                if (operationName != null) {
                    op = apid.getOperation(operationName);
                }
                if (op != null) {
                    if (op.sr) {
                        if (session == null) {
                            apid.wrongCalls++;
                            sendError("session", "Nesesitas iniciar session para usar esta operacion.");
                        } else {
                            op.calls++;
                        }
                    } else {
                        op.calls++;
                    }
                } else {
                    sendError("server_error", "Esa operacion no esta registrada.");
                }
            } else {
                apid.calls++;
                if (apid.isGsr()) {
                    if (session == null) {
                        apid.wrongCalls++;
                        sendError("session", "Nesesitas iniciar session para usar esta api.");
                    }
                }
            }
            if (Log.isDebugEnabled()) ApiManager.showDescriptions(apid.name);
        }
    }

    public AsyncContext getCtx() {
        return ctx;
    }

    public void setCtx(AsyncContext ctx) {
        this.ctx = ctx;
    }

}
