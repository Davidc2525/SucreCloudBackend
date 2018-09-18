package orchi.HHCloud.user.Exceptions;

public class UserMutatorException extends UserException {

    public UserMutatorException(String message) {
        super(message);
    }

    public UserMutatorException(Exception e) {
        super(e);
    }

}
