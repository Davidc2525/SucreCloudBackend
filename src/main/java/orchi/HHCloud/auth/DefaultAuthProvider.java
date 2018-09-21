package orchi.HHCloud.auth;

import orchi.HHCloud.Start;
import orchi.HHCloud.auth.Exceptions.*;
import orchi.HHCloud.auth.logIO.WraperLoginCallback;
import orchi.HHCloud.cipher.CipherProvider;
import orchi.HHCloud.quota.Exceptions.QuotaException;
import orchi.HHCloud.store.StoreManager;
import orchi.HHCloud.user.Exceptions.UserException;
import orchi.HHCloud.user.Exceptions.UserNotExistException;
import orchi.HHCloud.user.User;
import orchi.HHCloud.user.UserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Proveedor de autentcacion
 *
 * @author Colmenares David
 */
public class DefaultAuthProvider implements AuthProvider {
    private static Logger log = LoggerFactory.getLogger(DefaultAuthProvider.class);
    private static DefaultAuthProvider instance = null;
    private Map<String, String> users = new HashMap<String, String>();
    private Map<String, Integer> usersAuthFails = new HashMap<String, Integer>();
    private Map<String, User> tokensToVeryfyEmail = new HashMap<String, User>();
    private Map<String, TimeBasedToken> tokensTimeBaseById = new HashMap<String, TimeBasedToken>();
    private Map<String, User> tokesToRecoveryPassword = new HashMap<String, User>();
    private Map<String, TimeBasedToken> tokensTimeBaseByIdToRecoveryPassword = new HashMap<String, TimeBasedToken>();
    private UserProvider up;
    private CipherProvider cipherProvider = Start.getCipherManager().getCipherProvider();

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

    public static DefaultAuthProvider getInstance() {
        if (instance == null)
            instance = new DefaultAuthProvider();
        return instance;
    }

    @Override
    public void init() {
    }

    @Override
    public void authenticate(User authUser, WraperLoginCallback callback) throws AuthException {

        commonValidation(authUser.getEmail(), authUser.getPassword());

        User user = null;
        try {
            user = up.getUserByEmail(authUser.getEmail());
        } catch (UserNotExistException e) {
            throw new AuthUserNotExistsException(e.getMessage());
        } catch (UserException e) {
            throw new AuthException(e.getMessage());
        }

        checkPassword(user, authUser);

        callback.call(user);

    }

    private void checkPassword(User userStore, User authUser) throws AuthException, AuthExceededCountFaildException, AuthPasswordException {
        String userPassword = userStore.getPassword();

        if (userPassword == null) {
            throw new AuthUserNotExistsException(authUser.getEmail() + " no exist");
        }

        String userStorePasswordDecrypt = cipherProvider.decrypt(userPassword);

        if (!userStorePasswordDecrypt.equals(authUser.getPassword())) {
            Integer countFails = usersAuthFails.get(authUser.getEmail());
            if (countFails == null) {
                countFails = 0;
            }
            ++countFails;
            usersAuthFails.put(authUser.getEmail(), countFails);
            if (countFails >= 50) {
                throw new AuthExceededCountFaildException(
                        authUser.getEmail() + " exeedec the count retry auth " + countFails + " > 4, its locket.");
            }

            throw new AuthPasswordException(
                    "password: " + authUser.getPassword() + ", for " + authUser.getEmail() + " is incorrect");

        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public void verifyEmail(String idToken) throws VerifyException {
        User user = tokensToVeryfyEmail.get(idToken);
        try {
            log.debug(user + " " + idToken);
            revokeTokenToVerifyEmail(idToken);
            up.setVerifyEmail(user, true);

            //Start.getStoreManager().getStoreProvider().setQuota(user, Paths.get(""), StoreManager.SPACE_QUOTA_SIZE);
            Start.getQuotaManager().getProvider().setQuota(user, Paths.get(""), StoreManager.SPACE_QUOTA_SIZE);
        } catch (UserException e) {
            e.printStackTrace();
            try {
                up.setVerifyEmail(user, false);
            } catch (UserException e1) {
                e1.printStackTrace();
            }
            throw new VerifyException(e.getMessage());
        } catch (TokenException e) {
            e.printStackTrace();
            throw new VerifyException(e.getMessage());
        } catch (QuotaException e) {
            e.printStackTrace();
            try {
                up.setVerifyEmail(user, false);
            } catch (UserException e1) {
                e1.printStackTrace();
            }
            throw new VerifyException(e);
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
