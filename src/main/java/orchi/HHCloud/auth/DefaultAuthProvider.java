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

	private Map<String, User> tokesToRecoveryPassword = new HashMap<String, User>();
	private Map<String, TimeBasedToken> tokensTimeBaseByIdToRecoveryPassword = new HashMap<String, TimeBasedToken>();
	private UserProvider up;
	private static DefaultAuthProvider instance = null;

	@Override
	public void init() {
	}

	public DefaultAuthProvider() {
		up = Start.getUserManager().getUserProvider();

		Executors.newScheduledThreadPool(10).scheduleWithFixedDelay(() -> {
			log.debug("recoriendo tokens para verificar tiempo de vida. {} tokens activos", tokensTimeBaseById.size());
			tokensTimeBaseById.forEach((idtoken, timebasetoken) -> {
				Long currentTime = System.currentTimeMillis();
				if (currentTime > timebasetoken.getTimeExpire()) {
					try {
						log.debug("tiempo de vida agotado para token: {}", idtoken);
						revokeTokenToVerifyEmail(idtoken);
						log.debug("token revokado: {}", idtoken);
					} catch (TokenException e) {
						e.printStackTrace();
					}
				}
			});
		}, 0, 10, TimeUnit.SECONDS);

		Executors.newScheduledThreadPool(10).scheduleWithFixedDelay(() -> {
			log.debug("recoriendo tokens (password) para verificar tiempo de vida. {} tokens activos",
					tokesToRecoveryPassword.size());
			tokensTimeBaseByIdToRecoveryPassword.forEach((idtoken, timebasetoken) -> {
				Long currentTime = System.currentTimeMillis();
				if (currentTime > timebasetoken.getTimeExpire()) {
					try {
						log.debug("tiempo de vida agotado para token: {}", idtoken);
						revokeTokenToRecoveryPassword(idtoken, timebasetoken.getOwner());
						log.debug("token revokado: {}", idtoken);
					} catch (TokenException e) {
						e.printStackTrace();
					}
				}
			});
		}, 0, 10, TimeUnit.SECONDS);

	}

	@Override
	public void authenticate(User authUser, WraperLoginCallback callback) throws AuthException {

		commonValidation(authUser.getUsername(), authUser.getPassword());

		User user = null;
		try {
			user = up.getUserByEmail(authUser.getUsername());
		} catch (UserNotExistException e) {
			throw new AuthUserNotExistsException(e.getMessage());
		} catch (UserException e) {
			throw new AuthException(e.getMessage());
		}

		String userPassword = user.getPassword();

		if (userPassword == null) {
			throw new AuthUserNotExistsException(authUser.getUsername() + " no exist");
		}

		if (!userPassword.equals(authUser.getPassword())) {
			Integer countFails = usersAuthFails.get(authUser.getUsername());
			if (countFails == null) {
				countFails = 0;
			}
			++countFails;
			usersAuthFails.put(authUser.getUsername(), countFails);
			if (countFails >= 50) {
				throw new AuthExceededCountFaildException(
						authUser.getUsername() + " exeedec the count retry auth " + countFails + " > 4, its locket.");
			}

			throw new AuthPasswordException(
					"password: " + authUser.getPassword() + ", for " + authUser.getUsername() + " is incorrect");

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
			revokeTokenToVerifyEmail(idToken);
			up.setVerifyEmail(user);
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
		tokensTimeBaseById.put(token, new TimeBasedToken(token, user));
		return token;
	}

	@Override
	public String revokeTokenToVerifyEmail(String idToken) throws TokenException {

		if (!tokensToVeryfyEmail.containsKey(idToken)) {
			tokensTimeBaseById.remove(idToken);
			throw new TokenException("El token no existe, ya fue usado o revocado.");
		}
		tokensToVeryfyEmail.remove(idToken);
		tokensTimeBaseById.remove(idToken);
		return idToken;
	}

	@Override
	public String createTokenToRecoveryPassword(User user) {
		String token = GenerateToken.newToken(10, "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
		tokesToRecoveryPassword.put(token, user);
		tokensTimeBaseByIdToRecoveryPassword.put(token, new TimeBasedToken(token, user));
		token = decorateToken(token);
		return token;
	}

	@Override
	public void revokeTokenToRecoveryPassword(String idToken, User user) throws TokenException {

		if (idToken.indexOf("-") != -1) {
			System.out.println("undecorating token " + idToken);
			idToken = unDecorateToken(idToken);
			System.out.println("undecorated token " + idToken);
		}

		/*
		 * verificar q el el usuario q quiere revocar el token
		 * sea el mismo q lo genero
		 */
		verifyOwnerTokenToRecoveryPassword(idToken, user);

		if (!tokesToRecoveryPassword.containsKey(idToken)) {
			tokesToRecoveryPassword.remove(idToken);
			throw new TokenException("El token no existe, ya fue usado o revocado.");
		}
		tokesToRecoveryPassword.remove(idToken);
		tokensTimeBaseByIdToRecoveryPassword.remove(idToken);
	}

	/**
	 * antes de revokar un token para recuperacion y cambion de contraseña,
	 * debemos estar seguro que el usuario q desea enviar la actualizacion de la
	 * contraseña sea el mismo q envio el token y no otro usuario, por medida de
	 * seguridad
	 */
	private void verifyOwnerTokenToRecoveryPassword(String token, User user) throws TokenException {
		if (!tokesToRecoveryPassword.containsKey(token)) {
			throw new TokenException("El token no existe, ya fue usado o revocado.");
		}
		User storeUser = tokesToRecoveryPassword.get(token);
		if (!storeUser.getId().equalsIgnoreCase(user.getId())) {
			throw new TokenException("El token no pertenece a este usuario.");
		}
	}

	/**
	 * decorar el token 
	 * 1234567890 -> 1-2-3-4-5-6-7-8-9-0
	 */
	private String decorateToken(String token) {
		String[] tokenP = token.split("");
		String decoratedToken = "";
		for (int x = 0; x < tokenP.length; x++) {
			String letter = tokenP[x];
			if (x == tokenP.length - 1) {
				decoratedToken += letter;
			} else {
				decoratedToken += letter + "-";
			}
		}
		return decoratedToken;
	}

	/**
	 * quitar decoracion a token 
	 * 1-2-3-4-5-6-7-8-9-0 -> 1234567890
	 */
	private String unDecorateToken(String token) {
		String[] tokenP = token.split("-");
		String unDecoratedToken = "";
		for (int x = 0; x < tokenP.length; x++) {
			String letter = tokenP[x];
			unDecoratedToken += letter;
		}
		return unDecoratedToken;
	}
}
