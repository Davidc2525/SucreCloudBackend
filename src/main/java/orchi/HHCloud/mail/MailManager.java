package orchi.HHCloud.mail;

import orchi.HHCloud.Start;

public class MailManager {
	private static MailManager instance;
	private static String nameProvider = Start.conf.getString("mail.mailmanager.defaultmailprovider");
	private MailProvider provider;
	
	
	public MailManager(){
		try {
			Class<? extends MailProvider> classProvider = (Class<? extends MailProvider>) Class.forName(nameProvider);
			
			provider = classProvider.newInstance();
			provider.init();
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public MailProvider getProvider(){
		return provider;
	}
	
	public static MailManager getInstance(){
		if(instance == null){
			instance = new MailManager();
		}
		return instance;
	}
}
