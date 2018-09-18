package orchi.HHCloud.auth.Exceptions;

public class AuthExceededCountFaildException extends AuthException {

    /**
     *
     */
    private static final long serialVersionUID = 3967869639422758397L;

    public AuthExceededCountFaildException(String message) {
        super(message);
        //this.loger.warn(message);
    }

    public AuthExceededCountFaildException(Exception e) {
        super(e);
        //this.loger.warn(message);
    }

}
