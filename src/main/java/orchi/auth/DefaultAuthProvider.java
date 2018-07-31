package orchi.auth;

import java.util.HashMap;
import java.util.Map;

import orchi.auth.Exceptions.AuthExceededCountFaildException;
import orchi.auth.Exceptions.AuthException;
import orchi.auth.Exceptions.AuthPasswordException;
import orchi.auth.Exceptions.AuthUserNotExistsException;
import orchi.auth.Exceptions.AuthUsernameException;
import orchi.auth.logIO.LoginCallback;
import orchi.auth.logIO.WraperLoginCallback;

import orchi.user.User;
import orchi.user.UserManager;
import orchi.user.Exceptions.UserNotExistException;

/**
 * Proveedor de autentcacion
 * 
 * @author Colmenares David
 * @see OrchiAuthProvider
 */
public class DefaultAuthProvider implements AuthProvider {
	private Map<String, String> users = new HashMap<String, String>();
	private Map<String, Integer> usersAuthFails = new HashMap<String, Integer>();
	private static DefaultAuthProvider instance = null;

	public void init() {

	}
	
	
	
	@Override
	public void authenticate(User auser, WraperLoginCallback callback) throws AuthException {

		commonValidation(auser.getUsername(), auser.getPassword());
		
		User user = null;
		try {
			user = UserManager.getInstance().getUserProvider().getUserByEmail(auser.getUsername());
		} catch (UserNotExistException e) {
			throw new AuthUserNotExistsException(e.getMessage());
		}

		String userPassword = user.getPassword();

		if (userPassword == null) {
			throw new AuthUserNotExistsException(auser.getUsername() + " no exist");
		}

		if (!userPassword.equals(auser.getPassword())) {
			Integer countFails = usersAuthFails.get(auser.getUsername());
			if (countFails == null) {
				countFails = 0;
			}
			++countFails;
			usersAuthFails.put(auser.getUsername(), countFails);
			if (countFails >= 50) {
				throw new AuthExceededCountFaildException(
						auser.getUsername() + " exeedec the count retry auth " + countFails + " > 4, its tube locket.");
			}

			throw new AuthPasswordException(
					"password: " + auser.getPassword() + ", for " + auser.getUsername() + " is incorrect");

		}

		callback.call(user);

	}

	/*@Override
	public void authenticate(String username, String password) throws AuthException {

		commonValidation(username, password);
		User user = null;
		try {
			user = UserManager.getInstance().getUserProvider().getUserByEmail(username);
		} catch (UserNotExistException e) {
			throw new AuthUserNotExistsException(e.getMessage());

		}

		String userPassword = user.getPassword();

		if (userPassword == null) {
			throw new AuthUserNotExistsException(username + " no exist");
		}

		if (!userPassword.equals(password)) {
			Integer countFails = usersAuthFails.get(username);
			if (countFails == null) {
				countFails = 0;
			}
			++countFails;
			usersAuthFails.put(username, countFails);
			if (countFails >= 5) { 
				throw new AuthExceededCountFaildException(
						username + " exeedec the count retry auth " + countFails + " > 4, its tube locket.");

			}

			throw new AuthPasswordException("password: " + password + ", for " + username + " is incorrect");

		}

		System.err.println(user + " se logeo");
		user = null;

	}*/

	public static DefaultAuthProvider getInstance() {
		if (instance == null)
			instance = new DefaultAuthProvider();
		return instance;
	}

	

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}



}
