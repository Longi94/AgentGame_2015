/*******************************************************************************
	
	AgentGame 15.00.
	Copyright Peter Eredics (BUTE-DMIS) 2010-2015.
	
	GameConfig.java - configuration class
	
*******************************************************************************/


import java.io.*;
import java.util.Properties;


/**
 * This class allows access to the settings in the configuration file specified
 * in the mas2j file as paramter of the GameEnvironment 
 * 
 * @author Peter Eredics
 */
public class GameConfig {
	/** The Properties object to easily access settings */
	Properties properties = new Properties();
	
	
	/**
	 * Constructor reading the configuration file
	 * @param configFileName	Name of the configuration file
	 */
	public GameConfig(String configFileName) {
		InputStream configFile = null;
		
		// Open file
		try {
			configFile = new FileInputStream(configFileName);
		} catch (FileNotFoundException e) {
			System.out.println("\n\n * - * - * - * - * - * - * - * - * - * - * - * - * - * - * - * - * - * - * - * - * - * - * - * - * \n\n" +
					"   Error reading game configuration from AgentGame.conf!\n" +
					"   Make sure to download the latest AgentGame.conf file from\n" +
					"   http://agentgame.mit.bme.hu/agentgame/letoltes\n" +
					"   before running the simulator!\n" +
					"\n * - * - * - * - * - * - * - * - * - * - * - * - * - * - * - * - * - * - * - * - * - * - * - * - * \n\n");
			e.printStackTrace();
		}
		
		// Read configuration
		try {
			properties.load(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Read integer value from the configuration file
	 * @param key				The key of the value to read
	 * @return					Integer read from the file
	 */
	public int getInt(String key) {
		return Integer.parseInt(properties.getProperty(key));
	}
	
	
	/**
	 * Read string value from the configuration file
	 * @param key				The key of the value to read
	 * @return					String read from the file
	 */
	public String getString(String key) {
		return properties.getProperty(key);
	}
	
	/**
	 * Read integer value from the configuration file
	 * @param key				The key of the value to read
	 * @param defaultValue		The default value to return if not set
	 * @return					Integer read from the file
	 */
	public int getInt(String key, int defaultValue) {
		return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
	}
}
