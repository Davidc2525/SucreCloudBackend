package orchi.HHCloud.stores.FsStore;

import orchi.HHCloud.store.StoreProvider;
import orchi.HHCloud.stores.HdfsStore.HdfsManager;
import orchi.HHCloud.stores.HdfsStore.HdfsStoreProvider;


public class FsStore extends HdfsStoreProvider implements StoreProvider {
	
	@Override
	public void init(){
		
	}
	
	@Override
	public void start(){
		HdfsManager.getInstance(true);
	}
}
