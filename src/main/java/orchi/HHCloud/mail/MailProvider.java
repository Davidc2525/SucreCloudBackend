package orchi.HHCloud.mail;

import orchi.HHCloud.mail.Exceptions.SendEmailException;

public interface MailProvider {

	public void init();

	public void sendEmail(String from,String to,String subject,String body) throws SendEmailException;
}
