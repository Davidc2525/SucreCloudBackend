package orchi.HHCloud.share;

/**
 * @author david
 */
public class ShareManager {
	private static String defaultShareProvider = "";
	private static ShareProvider shareProvider;;
	private static ShareManager instance;

	public ShareManager() {
		this(defaultShareProvider);
	}

	public ShareManager(String defaultShareProvider2) {

	}

	public ShareManager(Class<? extends ShareProvider> defaultShareProvider2) {

	}
	
	public ShareProvider getShareProvider(){
		return shareProvider;
	}
	
	public static ShareManager getInstance(){
		if(instance == null){
			instance = new ShareManager();
		}
		return instance;
	}
}
