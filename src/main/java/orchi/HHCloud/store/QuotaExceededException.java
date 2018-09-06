package orchi.HHCloud.store;

public class QuotaExceededException extends Exception{
    public QuotaExceededException(String e){
        super(e);
    }
    public QuotaExceededException(Exception e){
        super(e);
    }
}
