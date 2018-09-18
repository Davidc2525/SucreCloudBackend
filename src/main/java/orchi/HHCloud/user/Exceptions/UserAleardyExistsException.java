package orchi.HHCloud.user.Exceptions;

public class UserAleardyExistsException extends UserException {


    public UserAleardyExistsException(String message) {
        super(message);
    }

    public UserAleardyExistsException(Exception e) {
        super(e);
    }


}
