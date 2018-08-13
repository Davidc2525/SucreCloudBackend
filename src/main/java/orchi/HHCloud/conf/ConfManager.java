package orchi.HHCloud.conf;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.combined.MultiFileConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class ConfManager {
	private static ConfManager instance;
	private Configuration config;

	public ConfManager() {
		Configurations configs = new Configurations();
		try {
			setConfig(configs.properties(new File("./application.properties")));
			//prepare();
			Iterator<String> iter = config.getKeys();
			while(iter.hasNext()){
				String key = iter.next();
				System.out.println(key+": "+getConfig().getString(key));
			}
			

		} catch (ConfigurationException cex) {
			cex.printStackTrace();

		}
	}
	
	public void prepare() throws ConfigurationException{
		Parameters params = new Parameters();
	
		
		MultiFileConfigurationBuilder<PropertiesConfiguration> builder =
		    new MultiFileConfigurationBuilder<PropertiesConfiguration>(PropertiesConfiguration.class)
		    .configure(params.multiFile()
		        .setFilePattern("./application.properties")
		        .setManagedBuilderParameters(params.properties())
		    );
		Configuration config = builder.getConfiguration();
		setConfig(config);
	}

	public static ConfManager getInstance() {
		if (instance == null) {
			instance = new ConfManager();
		}
		return instance;
	}

	/**
	 * @return the config
	 */
	public Configuration getConfig() {
		return config;
	}

	/**
	 * @param config the config to set
	 */
	private void setConfig(Configuration config) {
		this.config = config;
	}
}
