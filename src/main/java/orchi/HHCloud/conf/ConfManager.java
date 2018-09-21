package orchi.HHCloud.conf;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;
import java.util.Iterator;

public class ConfManager {
    private static ConfManager instance;
    private Configuration config;

    public ConfManager() {
        System.out.printf(String.valueOf(new File("./application.properties")));
        Configurations configs = new Configurations();
        try {
            PropertiesConfiguration c = configs.properties(new File("./application.properties"));
            c.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
            setConfig(c);
            //setConfig(configs.properties(new File("./application.properties")));
           // prepare();
            Iterator<String> iter = config.getKeys();
            while (iter.hasNext()) {
                String key = iter.next();
                System.out.println(key + ": " + getConfig().getString(key));
            }

        } catch (ConfigurationException cex) {
            cex.printStackTrace();

        }
    }

    public static ConfManager getInstance() {
        if (instance == null) {
            instance = new ConfManager();
        }
        return instance;
    }

    public void prepare() throws ConfigurationException {
        Parameters params = new Parameters();

        FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class)
                        .configure(params.properties()
                                .setFile(new File("./application.properties"))
                                .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
        // Configuration config = builder.getConfiguration();

		/*MultiFileConfigurationBuilder<PropertiesConfiguration> builder2 =
            new MultiFileConfigurationBuilder<PropertiesConfiguration>(PropertiesConfiguration.class)
		    .configure(params.multiFile()
		        .setFilePattern("./application.properties")
		        .setManagedBuilderParameters(params.properties()
		        		.setListDelimiterHandler(new DefaultListDelimiterHandler(',')))
		    );*/
        Configuration config = builder.getConfiguration();
        setConfig(config);
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
