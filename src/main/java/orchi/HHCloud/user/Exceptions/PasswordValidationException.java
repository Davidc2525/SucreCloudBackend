package orchi.HHCloud.user.Exceptions;

public class PasswordValidationException extends ValidationException {

    public PasswordValidationException(String message) {
        super(message);
    }

    public PasswordValidationException(Exception e) {
        super(e);
    }

}
