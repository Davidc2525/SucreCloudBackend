package orchi.HHCloud.auth.Exceptions;

public class AuthUserNotExistsException extends AuthException {

    /**
     *
     */
    private static final long serialVersionUID = 3761610862589116414L;

    public AuthUserNotExistsException(String message) {
        super(message);
        //loger.warn(message);
    }

    public AuthUserNotExistsException(Exception e) {
        super(e);
        //loger.warn(message);
    }

}
