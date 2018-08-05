package orchi.HHCloud.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.*;

import orchi.HHCloud.Start;
import orchi.HHCloud.auth.Exceptions.AuthExceededCountFaildException;
import orchi.HHCloud.auth.Exceptions.AuthException;
import orchi.HHCloud.auth.Exceptions.AuthPasswordException;
import orchi.HHCloud.auth.Exceptions.AuthUserNotExistsException;
import orchi.HHCloud.auth.Exceptions.TokenException;
import orchi.HHCloud.auth.Exceptions.VerifyException;
import orchi.HHCloud.auth.logIO.WraperLoginCallback;
import orchi.HHCloud.user.User;
import orchi.HHCloud.user.UserProvider;
import orchi.HHCloud.user.Exceptions.UserException;
import orchi.HHCloud.user.Exceptions.UserNotExistException;

/**
 * Proveedor de autentcacion
 *
 * @author Colmenares David
 */
public class DefaultAuthProvider implements AuthProvider {
	private static Logger log = LoggerFactory.getLogger(DefaultAuthProvider.class);
	private Map<String, String> users = new HashMap<String, String>();
	private Map<String, Integer> usersAuthFails = new HashMap<String, Integer>();
	private Map<String, User> tokensToVeryfyEmail = new HashMap<String, User>();
	private Map<String, TimeBasedToken> tokensTimeBaseById = new HashMap<String, TimeBasedToken>();
	private UserProvider up;
	private static DefaultAuthProvider instance = null;

	@Override
	public void init() {
	}

	public DefaultAuthProvider() {
		up = Start.getUserManager().getUserProvider();
		
		Executors.newScheduledThreadPool(10).scheduleWithFixedDelay(()->{
			log.debug("recoriendo tokens para verificar tiempo de vida. {} tokens activos",tokensTimeBaseById.size());
			tokensTimeBaseById.forEach((idtoken,timebasetoken)->{
				Long currentTime = System.currentTimeMillis();
				if(currentTime > timebasetoken.getTimeExpire()){
					try {
						log.debug("tiempo de vida agotado para token: {}",idtoken);
						revokeToken(idtoken);
						log.debug("token revokado: {}",idtoken);
					} catch (TokenException e) {
						e.printStackTrace();
					}
				}
			});
		}, 0, 10, TimeUnit.SECONDS);
		
	}

	@Override
	public void authenticate(User auser, WraperLoginCallback callback) throws AuthException {

		commonValidation(auser.getUsername(), auser.getPassword());

		User user = null;
		try {
			user = up.getUserByEmail(auser.getUsername());
		} catch (UserNotExistException e) {
			throw new AuthUserNotExistsException(e.getMessage());
		} catch (UserException e) {
			throw new AuthException(e.getMessage());
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
						auser.getUsername() + " exeedec the count retry auth " + countFails + " > 4, its locket.");
			}

			throw new AuthPasswordException(
					"password: " + auser.getPassword() + ", for " + auser.getUsername() + " is incorrect");

		}

		callback.call(user);

	}

	public static DefaultAuthProvider getInstance() {
		if (instance == null)
			instance = new DefaultAuthProvider();
		return instance;
	}

	@Override
	public void destroy() {

	}

	@Override
	public void verifyEmail(String idToken) throws VerifyException {
		User user = tokensToVeryfyEmail.get(idToken);
		try {
			System.err.println(user + " " + idToken);
			revokeToken(idToken);
			up.verifyEmail(user);
		} catch (UserException e) {
			e.printStackTrace();
			throw new VerifyException(e.getMessage());
		} catch (TokenException e) {
			e.printStackTrace();
			throw new VerifyException(e.getMessage());
		}
	}

	@Override
	public String createTokenToVerifyEmail(User user) {
		String token = GenerateToken.newToken();
		tokensToVeryfyEmail.put(token, user);
		tokensTimeBaseById.put(token, new TimeBasedToken(token));
		return token;
	}

	@Override
	public String revokeToken(String idToken) throws TokenException {
		if (!tokensToVeryfyEmail.containsKey(idToken)) {
			tokensTimeBaseById.remove(idToken);
			throw new TokenException("El token no existe, ya fue usado o revocado.");
		}
		tokensToVeryfyEmail.remove(idToken);
		tokensTimeBaseById.remove(idToken);
		return idToken;
	}

}
