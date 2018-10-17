package orchi.HHCloud.share;

public class ShareException extends Exception {

    public ShareException(Exception e) {
        super(e);
    }

    public ShareException(String e) {
        super(e);
    }
}
