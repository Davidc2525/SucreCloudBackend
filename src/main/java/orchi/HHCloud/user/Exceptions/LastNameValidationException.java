package orchi.HHCloud.user.Exceptions;

public class LastNameValidationException extends ValidationException {

    public LastNameValidationException(String message) {
        super(message);
    }

    public LastNameValidationException(Exception e) {
        super(e);
    }

}
