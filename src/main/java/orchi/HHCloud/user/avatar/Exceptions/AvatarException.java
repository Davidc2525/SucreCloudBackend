package orchi.HHCloud.user.avatar.Exceptions;

public class AvatarException extends Exception{
    public AvatarException(String e){
        super(e);
    }

    public AvatarException(Exception e){
        super(e);
    }
}
