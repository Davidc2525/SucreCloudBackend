package orchi.HHCloud.HHCloudAdmin;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import orchi.HHCloud.AdminService.Service;
import orchi.HHCloud.Start;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
import org.apache.xmlrpc.client.util.ClientFactory;

import java.net.MalformedURLException;
import java.net.URL;

public class Client {
    private XmlRpcClient client = null;
    private Service service = null;
    private IntegerProperty port = new SimpleIntegerProperty(Start.conf.getInt("admin.adminservice.port"));
    private BooleanProperty adminEnable = new SimpleBooleanProperty(Start.conf.getBoolean("admin.adminservice.enable"));

    public Client() throws MalformedURLException, XmlRpcException {
        if (!adminEnable.getValue()) {
            System.out.println("---------------------------------------------------");
            System.out.println("| EL SERVICIO DE ADMINISTRACION NO ESTA ACTIVADO. |");
            System.out.println("---------------------------------------------------");

            System.exit(1);
        }
        // create configuration
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL("http://localhost:" + port.getValue() + "/xmlrpc"));
        config.setEnabledForExtensions(true);
        config.setConnectionTimeout(60 * 1000);
        config.setReplyTimeout(60 * 1000);

        client = new XmlRpcClient();

        // use Commons HttpClient as transport
        client.setTransportFactory(
                new XmlRpcCommonsTransportFactory(client));
        // set configuration
        client.setConfig(config);

        // make the a regular call
        Object[] params = new Object[]
                {new Integer(2), new Integer(3)};
        Integer result = (Integer) client.execute("admin-service.suma", params);
        System.out.println("2 + 3 = " + result);

        // make a call using dynamic proxy
        ClientFactory factory = new ClientFactory(client);
        service = (Service) factory.newInstance(Service.class);
        int sum = service.suma(2, 4);
    }

    public Service getService() {
        return service;
    }
}
