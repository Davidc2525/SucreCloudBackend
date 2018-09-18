package orchi.HHCloud.user.Exceptions;

public class GenderValidationException extends ValidationException {
    public GenderValidationException(String message) {
        super(message);
    }

    public GenderValidationException(Exception e) {
        super(e);
    }
}
