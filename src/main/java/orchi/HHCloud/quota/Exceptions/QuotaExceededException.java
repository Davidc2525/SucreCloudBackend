package orchi.HHCloud.quota.Exceptions;

public class QuotaExceededException extends Exception{
    public QuotaExceededException(String e){
        super(e);
    }
    public QuotaExceededException(Exception e){
        super(e);
    }
}
