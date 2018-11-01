package orchi.HHCloud.cache;

public class CacheAleardyExistException extends Throwable {
    public CacheAleardyExistException(String s) {
        super(s);
    }

    public CacheAleardyExistException(Exception s) {
        super(s);
    }
}
