package orchi.HHCloud.auth;

import orchi.HHCloud.auth.Exceptions.AuthException;
import orchi.HHCloud.auth.Exceptions.AuthPasswordException;
import orchi.HHCloud.auth.Exceptions.AuthUsernameException;
import orchi.HHCloud.auth.Exceptions.TokenException;
import orchi.HHCloud.auth.Exceptions.VerifyException;
import orchi.HHCloud.auth.logIO.WraperLoginCallback;
import orchi.HHCloud.user.BasicUser;
import orchi.HHCloud.user.User;

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
	 *             <strong>orchi.HHCloud.auth.AuthProvider.authenticate(User,
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

	/**
    * Resibe como parametro el id token generado anteriormente, si aun es valido el token
    * edita el usuario binculado a ese token, validando el correo, cambiano su estado, emailVirefied a (true) con {@link orchi.HHCloud.user.UserProviderr.setVerifyEmail},
    * con eso el usuario queda verificado, si por alguna razon no se puede verificar, se debe lanzar una excepsion
    * de verificacion.
	*
	* @param idtoken token q se desea verificar
	* @throws VerifyException
	**/
	public void verifyEmail(String idtoken) throws VerifyException;

    /**
    * Crea un token de verificacion para el usuario pasado por parametro.
    **/
	public String createTokenToVerifyEmail(User user) throws TokenException;

    /**
    * Revoca el token pasado por parametro
    *
    */
	public String revokeTokenToVerifyEmail(String idToken) throws TokenException;
	
	/**
	 * Crear token para recuperar cambiar contraseña por motivo de olvido por parte de el usuario.
	 */
	public String createTokenToRecoveryPassword(User user);
	
	/**
	 * Revocar token de recuperacion de contraseña, luego de crear y suministrar la nueva contraseña junto con el token generado, se debe revocar.
	 * */
	public void revokeTokenToRecoveryPassword(String idToken,User user) throws TokenException;

}
