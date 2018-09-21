package orchi.HHCloud.user.userAvailable.Exceptions;

import orchi.HHCloud.user.Exceptions.UserException;

public class UserUnAvailableException extends UserException {
    /**
     *
     */
    private static final long serialVersionUID = 666720588436708792L;
    public UserUnAvailableException(String message){
        super(message);
    }
    public UserUnAvailableException(){
        super("Este usuario se encuetra inhabilidato");
    }

    public UserUnAvailableException(Exception e){
        super(e);
    }
}
