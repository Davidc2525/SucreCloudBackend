package orchi.HHCloud.share;

public class NotShareException extends ShareException{
    public NotShareException(Exception e) {
        super(e);
    }

    public NotShareException(String e) {
        super(e);
    }
}
