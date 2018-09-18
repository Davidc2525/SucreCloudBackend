package orchi.HHCloud.user.Exceptions;

public class FirstNameValidationException extends ValidationException {

    public FirstNameValidationException(String message) {
        super(message);
    }

    public FirstNameValidationException(Exception e) {
        super(e);
    }

}
