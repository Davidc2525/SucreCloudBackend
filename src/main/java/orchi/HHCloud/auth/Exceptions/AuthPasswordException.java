package orchi.HHCloud.auth.Exceptions;

public class AuthPasswordException extends AuthException {

    /**
     *
     */
    private static final long serialVersionUID = 1889547393691640445L;

    public AuthPasswordException(String message) {
        super(message);
        //loger.warn(message);
    }

    public AuthPasswordException(Exception e) {
        super(e);
        //loger.warn(message);
    }

}
