package orchi.HHCloud.cache;

public class CacheDontExistException extends Throwable {
    public CacheDontExistException(String s) {
        super(s);
    }

    public CacheDontExistException(Exception s) {
        super(s);
    }
}
