package orchi.user;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import orchi.SucreCloud.Start;
import orchi.SucreCloud.database.ConnectionProvider;
import orchi.SucreCloud.database.DbConnectionManager;
import orchi.user.Exceptions.UserException;
import orchi.user.Exceptions.UserMutatorException;
import orchi.user.Exceptions.UserMutatorPassword;
import orchi.user.Exceptions.UserNotExistException;

/**proveedor de usuarios por defecto*/
public class DefaultUserProvider implements UserProvider {
	private Map<String,User> usersById = new HashMap<String,User>();
	private Map<String,String> usersByEmail = new HashMap<String,String>();
	private Map<String,String> usersByUsername = new HashMap<String,String>();
	private ConnectionProvider provider;
	private Connection conn;


	public DefaultUserProvider() {
		
		usersById.put("23034087", new BasicUser().bind("23034087", "david.c", "david@orchi.com", "2525"));
		usersById.put("123", new BasicUser().bind("123", "luisa", "luisa@orchi.com", "2525"));
		usersById.put("345", new BasicUser().bind("345", "juan", "juan@orchi.com", "2525"));

		usersByEmail.put("david@orchi.com", "23034087");
		usersByEmail.put("luisa@orchi.com", "123");
		usersByEmail.put("juan@orchi.com", "345");

		usersByUsername.put("david.c", "23034087");
		usersByUsername.put("luisa", "23034087");
		usersByUsername.put("juan", "23034087");
	}



	@Override
	public User getUserById(String userId) throws UserNotExistException {
		//
		User user = usersById.get(userId);
		if(user == null){
			throw new UserNotExistException("Usuario con id"+userId+", no existe");
		}
		return user;
	}

	@Override
	public User getUserByEmail(String userEmail) throws UserNotExistException {
		String userId = usersByEmail.get(userEmail);
		if(userId == null){
			throw new UserNotExistException("Usuario con email: "+userEmail+", no existe");
		}

		User user = getUserById(userId);

		return user;
	}

	@Override
	public User getUserByUsername(String userName) throws UserNotExistException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createUser(CreateUser newUser) throws UserException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteUser(String userId) throws UserException {
		// TODO Auto-generated method stub

	}

	@Override
	public void changePasswordUser(UserMutatorPassword userMutator) throws UserMutatorException {
		// TODO Auto-generated method stub

	}

}
