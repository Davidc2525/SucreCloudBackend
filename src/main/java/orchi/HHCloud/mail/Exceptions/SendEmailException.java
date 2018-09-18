package orchi.HHCloud.mail.Exceptions;

public class SendEmailException extends Exception {
    public SendEmailException(String message) {
        super(message);
    }

    public SendEmailException(Exception e) {
        super(e);
    }
}
