package orchi.HHCloud.auth.Exceptions;

public class VerifyException extends Exception {

	public VerifyException(String message){
		super(message);
	}

	public VerifyException(Exception e){
		super(e);
	}
}
