package orchi.HHCloud;


 
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

//Copyright 2018 Google LLC
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

//[START gmail_quickstart] 
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;

public class SendMail {
 private static final String APPLICATION_NAME = "HHCloud";
 private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
 private static final String TOKENS_DIRECTORY_PATH = "tokens";

 /**
  * Global instance of the scopes required by this quickstart.
  * If modifying these scopes, delete your previously saved credentials/ folder.
  */
 private static final List<String> SCOPES = Arrays.asList(GmailScopes.GMAIL_SEND,GmailScopes.GMAIL_LABELS);
 private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
  
 /**
  * Creates an authorized Credential object.
  * @param HTTP_TRANSPORT The network HTTP Transport.
  * @return An authorized Credential object.
  * @throws IOException If the credentials.json file cannot be found.
  */
 private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
     // Load client secrets.
     InputStream in = SendMail.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
     GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
 
     // Build flow and trigger user authorization request.
     GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
             HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
             .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
             .setAccessType("offline")
             .build();
     return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
 }

 public static void main(String... args) throws IOException, GeneralSecurityException {
     // Build a new authorized API client service.
     final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
     Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
             .setApplicationName(APPLICATION_NAME)
             .build();

     // Print the labels in the user's account.
    /* String user = "me";
     ListLabelsResponse listResponse = service.users().labels().list(user).execute();
     List<Label> labels = listResponse.getLabels();
     if (labels.isEmpty()) {
         System.out.println("No labels found.");
     } else {
         System.out.println("Labels:");
         for (Label label : labels) {
             System.out.printf("- %s\n", label.getName());
         }
     }
     */
     
     try {
    	 String body=""
    	 		+ "<p>Hola {user}</p>"
    	 		+ "<p>Visita este vinculo para verificar tu direccion de correo electronico.</p>"
    	 		+ "<p><a href='#'>Link</a></p>"
    	 		+ "<p>Gracias.</p>"
    	 		+ "<p>El equipo de HHCloud</p>";
		sendMessage(service, "me",createEmail("david25pcxtreme@gmail.com","david25pcxtreme@gmail.com","test send html",body));
	} catch (MessagingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
 }

	public static MimeMessage createEmail(String to, String from, String subject, String bodyText)
			throws MessagingException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		MimeMessage email = new MimeMessage(session);

		email.setFrom(new InternetAddress(from));
		email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
		email.setSubject(subject);
		email.setContent(bodyText, "text/html; charset=\"UTF-8\"");
		return email;
	}
	
	 public static Message createMessageWithEmail(MimeMessage emailContent)
	            throws MessagingException, IOException {
	        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	        emailContent.writeTo(buffer);
	        byte[] bytes = buffer.toByteArray();
	        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
	        Message message = new Message();
	        message.setRaw(encodedEmail);
	        return message;
	    }
	 
	public static Message sendMessage(Gmail service, String userId, MimeMessage emailContent)
			throws MessagingException, IOException {
		Message message = createMessageWithEmail(emailContent);
		message = service.users().messages().send(userId, message).execute();

		System.out.println("Message id: " + message.getId());
		System.out.println(message.toPrettyString());
		return message;
	}
}
//[END gmail_quickstart]