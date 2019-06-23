package orchi.HHCloud.user;

import orchi.HHCloud.Start;
import orchi.HHCloud.provider.GetProvider;
import orchi.HHCloud.provider.ProviderManager;
import orchi.HHCloud.provider.ProviderManagerInstance;
import orchi.HHCloud.user.role.RoleProvider;
import orchi.HHCloud.user.search.SearchUserProvider;
import orchi.HHCloud.user.userAvailable.UserAvailableProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * administrador de proveedor de usuarios
 */
@ProviderManager

public class UserManager {
    private static UserManager instance;
    private static String classNameUserProvider = Start.conf.getString("user.usermanager.provider.user");
    private static String classNameUserRoleProvider = Start.conf.getString("user.usermanager.provider.user.role");
    private static String classNameUserAvailableProvider = Start.conf.getString("user.usermanager.provider.user.available");
    private static String classNameSearchUserProvider = Start.conf.getString("user.usermanager.provider.user.search");
    private Logger logger = LoggerFactory.getLogger(UserManager.class);
    private UserProvider userProvider;
    private UserAvailableProvider userAvailableProvider;
    private SearchUserProvider searchUserProvider;


    private RoleProvider roleProvider;

    /**
     * inicia con proveedor por defecto {@link orchi.HHCloud.user.EmbeddedUserProvider}
     */
    public UserManager() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException {
        try {
            Class<UserProvider> clazzUP = (Class<UserProvider>) Class.forName(classNameUserProvider);

            Class<RoleProvider> clazzURP = (Class<RoleProvider>) Class.forName(classNameUserRoleProvider);

            Class<UserAvailableProvider> clazzUAP = (Class<UserAvailableProvider>) Class.forName(classNameUserAvailableProvider);

            Class<SearchUserProvider> clazzSAP = (Class<SearchUserProvider>) Class.forName(classNameSearchUserProvider);

            userProvider = clazzUP.newInstance();
            userAvailableProvider = clazzUAP.newInstance();

            roleProvider = clazzURP.newInstance();
            roleProvider.init();

            userProvider.init();
            userAvailableProvider.init();

            searchUserProvider = clazzSAP.newInstance();
            searchUserProvider.prepare();
            searchUserProvider.init();


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * obtener la instancia estatica de {@link orchi.HHCloud.user.UserManager}
     */
    @ProviderManagerInstance
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
    @GetProvider
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


    @GetProvider
    public UserAvailableProvider getUserAvailableProvider() {
        return userAvailableProvider;
    }

    @GetProvider
    public RoleProvider getUserRoleProvider() {
        return roleProvider;
    }

    @GetProvider
    public SearchUserProvider getSearchUserProvider() {
        return searchUserProvider;
    }

}


