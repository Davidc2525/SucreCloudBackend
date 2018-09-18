/**
 * API.java
 */
package orchi.HHCloud.Api;

import orchi.HHCloud.Api.ApiManager.ApiDescriptor;

import javax.servlet.http.HttpServlet;

/**
 * @author david 13 ago. 2018
 */
public abstract class API extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = -265394068003702122L;
    public String apiName = "/";

    public void updateDescription(String apiName, String op) {
        ApiDescriptor apid = ApiManager.getApid(apiName);
        apid.calls++;
        apid.getOperation(op).calls++;
    }

}
