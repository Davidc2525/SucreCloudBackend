package orchi.HHCloud.AdminService;


import orchi.HHCloud.Start;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import java.io.IOException;

public class AdminServer {
    private WebServer webServer;

    private boolean adminEnabled = Start.conf.getBoolean("admin.adminservice.enable");
    private int port = Start.conf.getInt("admin.adminservice.port");

    public AdminServer() throws IOException, XmlRpcException {


        webServer = new WebServer(port);

        XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();

        PropertyHandlerMapping phm = new PropertyHandlerMapping();

        phm.addHandler("admin-service", ServiceImpl.class);
        phm.addHandler(Service.class.getName(), ServiceImpl.class);

        xmlRpcServer.setHandlerMapping(phm);

        XmlRpcServerConfigImpl serverConfig =
                (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
        serverConfig.setEnabledForExtensions(true);
        serverConfig.setContentLengthOptional(false);
        serverConfig.setEnabledForExceptions(true);

        if (adminEnabled) {
            start();
        }
    }

    private void start() {
        System.out.println("Iniciando servidor de servicio en puerto: " + port);
        webServer.shutdown();
        try {
            webServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stop() {
        System.out.println("Deteniendo servidor de servicio");
        webServer.shutdown();
    }
}
