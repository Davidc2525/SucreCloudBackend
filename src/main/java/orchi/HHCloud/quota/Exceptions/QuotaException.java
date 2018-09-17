package orchi.HHCloud.quota.Exceptions;


public class QuotaException extends Exception{

	public QuotaException(String message) {
		super(message);
	}

	public QuotaException(Exception e) {
		super(e);
	}

}
