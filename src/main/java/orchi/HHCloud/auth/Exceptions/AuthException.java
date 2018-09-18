package orchi.HHCloud.auth.Exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = 2112896651402297856L;
    Logger loger = LoggerFactory.getLogger(AuthException.class);

    public AuthException(String message) {
        super(message);
        loger.warn(message);
    }

    public AuthException(Exception e) {
        super(e);
        //loger.warn(message);
    }
}
