package orchi.HHCloud.user.userAvailable.Exceptions;

public class EnablingException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = 6667205884367087951L;
    public EnablingException(String message){
        super(message);
    }

    public EnablingException(Exception e){
        super(e);
    }

}
