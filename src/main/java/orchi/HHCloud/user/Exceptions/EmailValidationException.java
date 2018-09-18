package orchi.HHCloud.user.Exceptions;

public class EmailValidationException extends ValidationException {

    public EmailValidationException(String message) {
        super(message);
    }

    public EmailValidationException(Exception e) {
        super(e);
    }

}
