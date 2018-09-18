package orchi.HHCloud.auth.Exceptions;

public class AuthUsernameException extends AuthException {

    /**
     *
     */
    private static final long serialVersionUID = -9086451212602589827L;

    public AuthUsernameException(String message) {
        super(message);
    }

    public AuthUsernameException(Exception e) {
        super(e);
    }


}
