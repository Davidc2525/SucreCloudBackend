package orchi.HHCloud.user;

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

	public User verifyEmail(User user) throws UserException;
}
