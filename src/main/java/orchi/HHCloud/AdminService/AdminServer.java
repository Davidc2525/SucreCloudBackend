package orchi.HHCloud.AdminService;


import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import orchi.HHCloud.Start;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import java.io.IOException;

public class AdminServer {
    private WebServer webServer;
    private IntegerProperty port = new SimpleIntegerProperty(Start.conf.getInt("admin.adminservice.port"));
    private BooleanProperty adminEnable = new SimpleBooleanProperty(Start.conf.getBoolean("admin.adminservice.enable"));

    public AdminServer() throws IOException, XmlRpcException {
        adminEnable.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    start();
                } else {
                    stop();
                }
            }
        });

        port.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (oldValue != newValue) {
                    stop();
                    start();
                }
            }
        });

        webServer = new WebServer(port.getValue());

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

        if (adminEnable.getValue()) {
            start();
        }
    }

    private void start() {
        System.out.println("Iniciando servidor de servicio en puerto: "+port.getValue());
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
