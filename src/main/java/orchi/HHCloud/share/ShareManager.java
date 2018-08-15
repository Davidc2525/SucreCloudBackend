package orchi.HHCloud.share;

import orchi.HHCloud.Start;
import orchi.HHCloud.store.RestrictedNames;

/**
 * @author david
 */
public class ShareManager {
	private static String defaultShareProvider = Start.conf.getString("share.sharemanager.provider");
	private static ShareProvider shareProvider;;
	private static ShareManager instance;

	public ShareManager() {
		try {
			Class<?> classProvider = Class.forName(defaultShareProvider);

			shareProvider = (ShareProvider) classProvider.newInstance();
			shareProvider.init();

		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			System.exit(1);
			e.printStackTrace();
		}
	}

	public ShareProvider getShareProvider() {
		return shareProvider;
	}

	public static ShareManager getInstance() {
		if (instance == null) {
			instance = new ShareManager();
		}
		return instance;
	}
}
