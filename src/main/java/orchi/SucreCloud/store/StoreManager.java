package orchi.SucreCloud.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreManager {
	private static Logger log = LoggerFactory.getLogger(StoreManager.class);
	private static String defaultStore = "orchi.SucreCloud.stores.hdfsStore.HdfsStoreProvider";
	private static StoreManager instance = null;
	private Store storeProvider = null;

	public StoreManager() {
		this(defaultStore);
	}

	public StoreManager(String storeProviderClassName) {
		log.debug("Creando proveedor de almacenamiento: {}",storeProviderClassName);
		try {			
			@SuppressWarnings("unchecked")
			Class<? extends Store> ClassStore = (Class<? extends Store>) Class.forName(storeProviderClassName);

			storeProvider = ClassStore.newInstance();
			log.debug("Creado proveedor de almacenamiento: {}",storeProviderClassName);
		} catch (ClassNotFoundException e) {
			log.error("No se encontro la clase proveedora de almacenamiento: {}",storeProviderClassName);
			e.printStackTrace();
		} catch (InstantiationException e) {
			log.error("No se pudo iniciar el proveedor de almacenamiento: {}",storeProviderClassName);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			log.error("No se pudo iniciar el proveedor de almacenamiento: {}",storeProviderClassName);
			e.printStackTrace();
		}
	}

	public StoreManager(Class<? extends Store> storeProviderClass) {
		log.debug("Creando proveedor de almacenamiento: {}",storeProviderClass.getName());		
		try {
			Class<? extends Store> ClassStore = storeProviderClass;

			storeProvider = ClassStore.newInstance();
			log.debug("Creado proveedor de almacenamiento: {}",storeProviderClass.getName());
		} catch (InstantiationException e) {
			log.error("No se pudo iniciar el proveedor de almacenamiento: {}",storeProviderClass.getName());
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			log.error("No se pudo iniciar el proveedor de almacenamiento: {}",storeProviderClass.getName());
			e.printStackTrace();
		}
	}
	
	public Store getStoreProvider(){
		log.debug("Obtener proveedor de almacenamiento: {}",storeProvider.getClass().getName());
		return storeProvider;
	}

	public static StoreManager getInstance() {
		log.debug("Obtener de StoreManager");
		if (instance == null) {
			log.warn("Creando nueva instancia de StoreManager con proveedor de almacenamiento por defecto: {}",defaultStore);
			instance = new StoreManager();
		}

		return instance;
	}

}
