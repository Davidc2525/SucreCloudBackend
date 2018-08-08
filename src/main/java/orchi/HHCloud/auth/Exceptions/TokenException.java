package orchi.HHCloud.auth.Exceptions;

public class TokenException extends RuntimeException{
	
	public TokenException(){
		super();
	}
	public TokenException(String message){
		super(message);
	}
	
	public TokenException(Exception e){
		super(e);
	}
}
