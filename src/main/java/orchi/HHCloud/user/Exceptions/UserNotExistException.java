package orchi.HHCloud.user.Exceptions;

public class UserNotExistException extends UserException {

    public UserNotExistException(String message) {
        super(message);
    }

    public UserNotExistException(Exception e) {
        super(e);
    }

}
