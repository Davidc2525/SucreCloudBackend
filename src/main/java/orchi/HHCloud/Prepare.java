package orchi.HHCloud;

import java.io.IOException;

import orchi.HHCloud.mail.MailProvider;
import orchi.HHCloud.mail.Exceptions.SendEmailException;

public class Prepare {
	public static void main(String[] args) throws IOException {
		/**preparar la base de datos embebida*/
		String[] params = {"./src/main/resources/schema.sql"};
		org.apache.derby.tools.ij.main(params);
		
		
		/**preparar el api de gmail*/
		MailProvider mp = Start.getMailManager().getProvider();
		try {
			String admin = Start.conf.getString("mail.mailmanager.admin");
			mp.sendEmail(admin,admin, "activate service", "active service.");
		} catch (SendEmailException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
