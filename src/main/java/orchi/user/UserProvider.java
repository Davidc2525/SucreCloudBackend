package orchi.user;

import orchi.user.Exceptions.UserException;
import orchi.user.Exceptions.UserMutatorException;
import orchi.user.Exceptions.UserMutatorPassword;
import orchi.user.Exceptions.UserNotExistException;

/**
 * interface para proveedor de usuarios*/
public interface UserProvider {
	
	public UserValidator getUserValidator();
	/**obtener usuario por su id*/
	public User getUserById(String userId) throws UserNotExistException,UserException;
	/**obtener usuario por su correo (email)*/
	public User getUserByEmail(String userEmail) throws UserNotExistException,UserException;
	/**obtener usuario por su username (nombre de usuario)*/
	public User getUserByUsername(String userName) throws UserNotExistException,UserException;
	/**crear un nuevo usuario*/
	public void createUser(User newUser) throws UserException;
	/**eliminar un usuario*/
	public void deleteUser(User userId) throws UserException;
	/**cambiar la clave de un usuario "esto tengo q hacerlo en otro lugar"*/
	public void changePasswordUser(UserMutatorPassword userMutator) throws UserMutatorException,UserException;

}
