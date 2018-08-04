package orchi.HHCloud.stores.FsStore;

import orchi.HHCloud.store.Store;
import orchi.HHCloud.stores.hdfsStore.HdfsManager;
import orchi.HHCloud.stores.hdfsStore.HdfsStoreProvider;

public class FsStore extends HdfsStoreProvider implements Store {
	
	@Override
	public void init(){
		
	}
	
	@Override
	public void start(){
		HdfsManager.getInstance(true);
	}
}
