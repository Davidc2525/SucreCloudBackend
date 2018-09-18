package orchi.HHCloud.user.Exceptions;

public class UsernameValidationException extends ValidationException {

    public UsernameValidationException(String message) {
        super(message);
    }

    public UsernameValidationException(Exception e) {
        super(e);
    }

}