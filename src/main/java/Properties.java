import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Properties {

	private static Log log = LogFactory.getLog(Properties.class);
	private static PropertiesConfiguration config = null;
	
	private static final String propertiesFile = "sharon.properties";
	
	
	private static boolean usingDefaults=true;
	
	private static String fileBase = ".";
	
	public Properties(){
		readConfig();
	}
	

	private static void readConfig(){
		if(config==null){
			try {
				setConfig(new PropertiesConfiguration(propertiesFile));
				log.info("Read properties from "+propertiesFile);
				setUsingDefaults(false);
				log.debug("Properties base path = "+getFileBase());
			} catch  (ConfigurationException e) {
				try {
					setConfig(new PropertiesConfiguration("/var/sharon/"+propertiesFile));
					log.info("Read properties from /var/sharon/"+propertiesFile);
					setUsingDefaults(false);
					setFileBase("/var/sharon");
					log.debug("Properties base path = "+getFileBase());
				} catch (ConfigurationException e1) {
					log.error("Could not read sharon.properties, not found in /var/sharon either.");
					log.warn("Using default properties throughout.");
					config=null;
				}
			}
		}
	}
	

	
	public static PropertiesConfiguration getConfig() {
		readConfig();
		return config;
	}

	public static void setConfig(PropertiesConfiguration c) {
		config = c;
	}

	public static boolean isUsingDefaults() {
		readConfig();
		return usingDefaults;
	}

	public static void setUsingDefaults(boolean uDefaults) {
		usingDefaults = uDefaults;
	}

	public static String getFileBase() {
		return fileBase;
	}

	public static void setFileBase(String fBase) {
		fileBase = fBase;
	}

}
