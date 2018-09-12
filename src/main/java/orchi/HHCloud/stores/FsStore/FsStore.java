package orchi.HHCloud.stores.FsStore;

import orchi.HHCloud.Start;
import orchi.HHCloud.store.ContentSummary;
import orchi.HHCloud.store.StoreProvider;
import orchi.HHCloud.stores.HdfsStore.HdfsManager;
import orchi.HHCloud.stores.HdfsStore.HdfsStoreProvider;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.User;
import org.apache.hadoop.fs.FileSystem;

import java.nio.file.Path;


public class FsStore extends HdfsStoreProvider implements StoreProvider {
	
	@Override
	public void init(){
		
	}
	
	@Override
	public void start(){
		HdfsManager.getInstance(true);
	}

	@Override
	public void setQuota(User user, Path path, long size) {
		return;
	}

	@Override
	public void removeQuota(User user, Path path) {
		return;
	}

	@Override
	public ContentSummary getContentSummary(User user, Path path) {
		ContentSummary cs = new ContentSummary();
		DataUser dUser = (DataUser) user;
		boolean verified = dUser.isEmailVerified();
		try {
			org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path.toString());
			FileSystem fs = HdfsManager.getInstance().getFs();
			org.apache.hadoop.fs.ContentSummary fsCs = fs.getContentSummary(p);

			cs.setDirectoryCount(fsCs.getDirectoryCount());
			cs.setFileCount(fsCs.getFileCount());
			cs.setSpaceQuota(verified ? Start.getStoreManager().SPACE_QUOTA_SIZE : Start.getStoreManager().SPACE_QUOTA_SIZE_NO_VERIFIED_USER);
			cs.setSpaceConsumed(fsCs.getSpaceConsumed());
			cs.setLength(fsCs.getLength());
			cs.setQuota(fsCs.getQuota());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return cs;
	}
}
