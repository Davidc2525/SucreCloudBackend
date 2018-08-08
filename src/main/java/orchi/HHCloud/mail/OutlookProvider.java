package orchi.HHCloud.mail;

import java.io.IOException;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import orchi.HHCloud.Start;
import orchi.HHCloud.mail.Exceptions.SendEmailException;

public class OutlookProvider implements MailProvider {
	private Session session;
	private String username;
	private String password;

	@Override
	public void init() {
		System.err.println("iniciando proveedor outlook");
		username = Start.conf.getString("mail.mailmanager.admin");
		password = Start.conf.getString("mail.mailmanager.admin.password");

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "outlook.office365.com");
		props.put("mail.smtp.port", "587");

		session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

	}

	@Override
	public void sendEmail(String from, String to, String subject, String body) throws SendEmailException {
		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			message.setSubject(subject);
			message.setContent(body, "text/html; charset=\"UTF-8\"");

			Transport.send(message);

		} catch (MessagingException e) {
			throw new SendEmailException(e);
		}
	}
}