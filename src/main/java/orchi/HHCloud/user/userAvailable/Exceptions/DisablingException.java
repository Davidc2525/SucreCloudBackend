package orchi.HHCloud.user.userAvailable.Exceptions;

public class DisablingException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = 6667205884367087921L;
    public DisablingException(String message){
        super(message);
    }

    public DisablingException(Exception e){
        super(e);
    }
}
