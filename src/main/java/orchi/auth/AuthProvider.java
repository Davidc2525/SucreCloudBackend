package orchi.auth;

import java.util.Map;

import orchi.auth.Exceptions.AuthException;
import orchi.auth.Exceptions.AuthPasswordException;
import orchi.auth.Exceptions.AuthUsernameException;
import orchi.auth.logIO.LoginCallback;
import orchi.auth.logIO.WraperLoginCallback;
import orchi.user.BasicUser;
import orchi.user.User;

/**
 * Interfas para proveedor de autenticacion
 */
public interface AuthProvider {
	/**
	 * Inicia el proveedor
	 */
	public void init();

	/** llamar cuando se vaya a modificar el proveedor */
	public void destroy();

	/** validacion comun de los datos de entrada, funcion por default */
	default public void commonValidation(String username, String password) throws AuthException {
		if (username.equals("") || username == null) {
			throw new AuthUsernameException("Username is required");
		}
		if (password.equals("") || password == null) {
			throw new AuthPasswordException("Password is required");
		}
	}

	/**
	 * autenticacion de session, si no es valido, devuelve un throw si no lansa
	 * el throw, es por q se autentico bien, y logea el usuario
	 * 
	 * @deprecated en cambio tengo q usar
	 *             <strong>orchi.auth.AuthProvider.authenticate(User,
	 *             WraperLoginCallback)</strong> que recibe un lambda como
	 *             argumento para hacer lo ke kiera despoes de logear el usuario
	 *             (de hacer la logica de negocio)
	 */
	default public void authenticate(String username, String password) throws AuthException {
		authenticate(new BasicUser().bind(null, username, null, password), (user) -> {

		});
	};

	/**
	 * autenticacion de session, si no es valido, devuelve un throw si no lansa
	 * el throw, es por q se autentico bien, y logea el usuario,
	 * {@link WraperLoginCallback} primero hace la logica de negocio, antes de
	 * pasar el control quien llamo el metodo
	 * 
	 */
	public void authenticate(User user, WraperLoginCallback callback) throws AuthException;
	
	
	
}
