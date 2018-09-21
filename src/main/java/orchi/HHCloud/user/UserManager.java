package orchi.HHCloud.user;

import orchi.HHCloud.Start;
import orchi.HHCloud.user.userAvailable.UserAvailableProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * administrador de proveedor de usuarios
 */
public class UserManager {
    private static UserManager instance;
    private static String classNameUserProvider = Start.conf.getString("user.usermanager.provider.user");
    private static String classNameUserAvailableProvider = Start.conf.getString("user.usermanager.provider.user.available");
    private Logger logger = LoggerFactory.getLogger(UserManager.class);
    private UserProvider userProvider;
    private UserAvailableProvider userAvailableProvider;

    /**
     * inicia con proveedor por defecto {@link orchi.HHCloud.user.EmbeddedUserProvider}
     */
    public UserManager() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException {
        try {
            Class<UserProvider> clazzUP = (Class<UserProvider>) Class.forName(classNameUserProvider);

            Class<UserAvailableProvider> clazzUAP = (Class<UserAvailableProvider>) Class.forName(classNameUserAvailableProvider);

            userProvider = clazzUP.newInstance();
            userAvailableProvider = clazzUAP.newInstance();

            userProvider.init();
            userAvailableProvider.init();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * obtener la instancia estatica de {@link orchi.HHCloud.user.UserManger}
     */
    public static UserManager getInstance() {
        if (instance == null) {
            try {
                instance = new UserManager();
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    /**
     * devuelve el proveedor de usuario en la instancia
     */
    public UserProvider getUserProvider() {
        return userProvider;
    }

    /**
     * aun no, setear nuevo administrador de usuario a la instancia
     */
    public void setUserProvider(UserProvider userProvider) {
        logger.warn("Nuevo userProvider: " + userProvider.getClass().getName());
        this.userProvider = userProvider;
    }

    public UserAvailableProvider getUserAvailableProvider() {
        return userAvailableProvider;
    }
}
