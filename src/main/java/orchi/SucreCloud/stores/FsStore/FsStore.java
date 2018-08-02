package orchi.SucreCloud.stores.FsStore;

import orchi.SucreCloud.store.Store;
import orchi.SucreCloud.stores.hdfsStore.HdfsManager;
import orchi.SucreCloud.stores.hdfsStore.HdfsStoreProvider;

public class FsStore extends HdfsStoreProvider implements Store {
	
	@Override
	public void init(){
		
	}
	
	@Override
	public void start(){
		HdfsManager.getInstance(true);
	}
}
