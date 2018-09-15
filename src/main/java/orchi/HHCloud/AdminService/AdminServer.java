package orchi.HHCloud.AdminService;


import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import java.io.IOException;

public class AdminServer {
    public AdminServer() throws IOException, XmlRpcException {

        WebServer webServer = new WebServer(2626);

        XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();

        PropertyHandlerMapping phm = new PropertyHandlerMapping();

        phm.addHandler("admin-service",ServiceImpl.class);
        phm.addHandler(Service.class.getName(), ServiceImpl.class);

        xmlRpcServer.setHandlerMapping(phm);

        XmlRpcServerConfigImpl serverConfig =
                (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
        serverConfig.setEnabledForExtensions(true);
        serverConfig.setContentLengthOptional(false);

        webServer.start();
    }
}
