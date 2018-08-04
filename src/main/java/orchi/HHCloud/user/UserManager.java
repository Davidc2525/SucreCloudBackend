package orchi.HHCloud.user;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**administrador de proveedor de usuarios*/
public class UserManager {
	private Logger logger = LoggerFactory.getLogger(UserManager.class);
	private UserProvider userProvider;
	private static UserManager instance;
	
	/**inicia con proveedor por defecto {@link orchi.HHCloud.user.DefaultUserProvider}*/
	public UserManager() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		
		this(DefaultUserProvider.class);
	}
	
	/**inicia con un proveedor pasado como String*/
	@SuppressWarnings("unchecked")
	public UserManager(String provider) throws InstantiationException, IllegalAccessException, ClassNotFoundException {

		this((Class<? extends UserProvider>) Class.forName(provider));
	}

	/**inicia con un proveedor pasado como Class<? extends UserProvider>*/
	public UserManager(Class<? extends UserProvider> provider) throws InstantiationException, IllegalAccessException {
		
		userProvider = (UserProvider) provider.newInstance();
		logger.info("UserProvider: "+provider.getName());
		UserManager.instance = this;
	}

	/**devuelve el proveedor de usuario en la instancia*/
	public  UserProvider getUserProvider() {
		return userProvider;
	}
	/**aun no, setear nuevo administrador de usuario a la instancia*/
	public void setUserProvider(UserProvider userProvider) {
		logger.warn("Nuevo userProvider: "+userProvider.getClass().getName());
		this.userProvider = userProvider;
	}
	/**obtener la instancia estatica de {@link orchi.HHCloud.user.UserManger}*/
	public static UserManager getInstance() {
		if(instance == null){
			try {
				new UserManager();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return instance;
	}

}
