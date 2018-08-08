package orchi.HHCloud.user;

import orchi.HHCloud.mail.Exceptions.SendEmailException;
import orchi.HHCloud.user.Exceptions.UserException;
import orchi.HHCloud.user.Exceptions.UserMutatorException;
import orchi.HHCloud.user.Exceptions.UserMutatorPassword;
import orchi.HHCloud.user.Exceptions.UserNotExistException;

/**
 * interface para proveedor de usuarios
 */
public interface UserProvider {

	public UserValidator getValidator();

	/** obtener usuario por su id */
	public User getUserById(String userId) throws UserNotExistException, UserException;

	/** obtener usuario por su correo (email) */
	public User getUserByEmail(String userEmail) throws UserNotExistException, UserException;

	/** obtener usuario por su username (nombre de usuario) */
	public User getUserByUsername(String userName) throws UserNotExistException, UserException;

	/** crear un nuevo usuario */
	public void createUser(User newUser) throws UserException;

	/** eliminar un usuario */
	public void deleteUser(User user) throws UserException;

	/**
	 * cambiar la clave de un usuario "esto tengo q hacerlo en otro lugar"
	 * 
	 * @return User
	 */
	public User changePasswordUser(UserMutatorPassword userMutator) throws UserMutatorException, UserException;

	public User editUser(User userWithChanges) throws UserException;

	/**
	 * configurar verificacion de usuario, para esto se tiene q crear un token de verificacion con {@link orchi.HHCloud.auth.AuthProvider.createTokenToVerifyEmail}
	 * 
	 * */
	public User setVerifyEmail(User user) throws UserException;
	
	/**
	 * Envia un menssa al correo de el usuario con un enlace q contiene un token de, ese token permite verificar la cuenta de el usuario. 
	 * */
	public User sendVerifyEmail(User user) throws UserException,SendEmailException;
	
	/**
	 * <h1>tengo q crear la plantilla para este mensaje</h1>
	 * Envia un menssa al correo de el usuario con un token de, ese token le permite cambiar la contraseña de su cuenta por si se le olvido
	 * */
	public User sendRecoveryPasswordEmail(User user) throws UserException,SendEmailException;
}
