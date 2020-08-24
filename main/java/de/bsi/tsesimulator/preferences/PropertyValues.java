/**
 * 
 */
package main.java.de.bsi.tsesimulator.preferences;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import main.java.de.bsi.seapi.SEAPI;
import main.java.de.bsi.tsesimulator.constants.ConfigConstants;


/**
 * Used for reading the file config.properties which stores all of the relevant configurable data in the simulator. 
 * The path leading to config.properties has to be provided before using any of the functionalities provided by this class.
 * For further information on the values and parameters stored in config.properties, consult the handbook for this simulator.
 * @author dpottkaemper
 * @since 1.0
 * @version 1.5
 */
public class PropertyValues {
	private Properties configValues = new Properties();	//stores the Properties tied to the file config.properties	
	private static PropertyValues instance;				//stores the only instance of this class that is returned when .getInstance() is called
	private static String pathToResourceDirectory;		//stores the resources directory, meaning the one that contains config.properties, userlist.properties,
															//keyDir, normal storing and persistent storing
	private static String pathToConfigProperties;		//stores the path to config.properties
	
	

	/**
	 * This class does not know inherently where to find the config.properties file on the file system.
	 * It does also not know, where the important resource directory is located. That directory is important, because it is used as a point of reference for the 
	 * relative paths in config.properties pointing to the key directory, the storage directory and many more.
	 * Because of this the user has to tell the class where the resource directory is located on the file system, so this class may then 
	 * construct the necessary absolute paths to config.properties and the other files.
	 * <br><b>Important:</b> this function has to be called before ever using PropertyValues. 
	 * @param pathToResourceDir the absolute path to the directory containing config.properties, userlist.properties, storage, persistentStorage and keys. <br>
	 * Should <b>not</b> end with a path separator character (e.g. <b>/</b> or <b>\</b>)
	 */
	public static void setPathToResourceDirectory(String pathToResourceDir) {
		pathToResourceDirectory = pathToResourceDir;
	}
	
	
	/**
	 * Fetches the single instance of a PropertyValues object that is allowed to exist. If none exist yet, this creates the first one through the usage of 
	 * the private constructor.
	 * @return the PropertyValues object 
	 * @throws IOException if {@linkplain PropertyValues#loadConfigFile(String)} throws such an Exception
	 * @since 1.0
	 * @version 1.5
	 */
	public static PropertyValues getInstance() throws IOException {
		if(instance == null) {
			instance = new PropertyValues();
		}
		return instance;
	}
	
	/**
	 * Private constructor for a PropertyValues object, shall only be called <b>after</b> the function {@linkplain PropertyValues#setPathToConfigProperties(String)} has
	 *  been used.
	 * Without a path to such a file, the program <b>DOES NOT WORK</b>.
	 * @throws IOException if config.properties can not be found and/or read.
	 */
	private PropertyValues() throws IOException {
		loadConfigFile();
	}
	
	 
	/**
	 * Attempts to load the configuration file located inside the directory provided via {@linkplain PropertyValues#setPathToResourceDirectory(String)}.
	 * The configuration file has to be named {@linkplain ConfigConstants#CFG_NAME}, otherwise it will not be found.
	 * The configuration file stores every important parameter of the simulator that can be configured, for example which key to use and where the 
	 * certificates are located.  
	 * @throws IOException if the file does not exist and/or can not be read. 
	 * @since 1.5
	 */
	private void loadConfigFile() throws IOException {
		//if the path pointing to parent directory of config.properties has not yet been set, reading from that file is impossible
		if(pathToResourceDirectory == null) {
			throw new IOException("No path to directory containing config.properties set. Path needed!");
		}
		//create the path to config.properties:
		pathToConfigProperties = pathToResourceDirectory+File.separator+ConfigConstants.CFG_NAME;
		
		//create a BufferedInputStream to read the config.properties file. This could throw a FileNotFoundException
		BufferedInputStream stream = new BufferedInputStream(new FileInputStream(pathToConfigProperties));
		 
		//load the stream into the Properties. Might throw IOException.
		configValues.load(stream);
		//close the stream afterwards. Might throw IOException.
		stream.close();
	}
	
	//----------------------getPathTo XYZ functions ----------------------------------------------------------------------------------------------------
	/**
	 * Getter for the absolute path to the file userlist.properties.
	 * This expects the file storing the user list to be located inside the resource directory set by {@linkplain PropertyValues#setPathToResourceDirectory(String)} 
	 * and to be named according to the name found in the config.properties file under the tag {@linkplain ConfigConstants#CFG_TAG_PATH_TO_USERLIST_PROPERTIES}.
	 * <br><b>IMPORTANT:</b> should only be used after {@linkplain PropertyValues#setPathToResourceDirectory(String)} has been called once.
	 * @return the absolute path to the file storing the user list, most likely named userlist.properties<br>
	 */
	public String getPathToUserlistProperties() {
		String pathToUserlistProperties = pathToResourceDirectory+File.separator+instance.getValue(ConfigConstants.CFG_TAG_PATH_TO_USERLIST_PROPERTIES);
		return pathToUserlistProperties;
	}
	
	/**
	 * Getter for the absolute path to the directory denoted by {@linkplain ConfigConstants#CFG_TAG_KEY_DIR} in config.properties. 
	 * This expects the directory to be a child directory of the resources directory, set by {@linkplain PropertyValues#setPathToResourceDirectory(String)}.
	 * The returned path ends with the name of the key directory, <b>not</b> with a path separator character (e.g. <b>/</b> or <b>\</b>).
	 * <br><b>IMPORTANT:</b> should only be used after {@linkplain PropertyValues#setPathToResourceDirectory(String)} has been called once.
	 * @return the absolute path to the directory containing the keys and certificates for the TSE simulator
	 */
	public String getPathToKeyDir() {
		//absolute path to key directory = pathToResourceDirectory/keyDir
		String pathToKeyDir = pathToResourceDirectory+File.separator+instance.getValue(ConfigConstants.CFG_TAG_KEY_DIR);
		return pathToKeyDir;
	}
	
	/**
	 * Getter for the absolute path to the private key file used in the simulator.
	 * This expects the file containing the private key to be located inside a directory denoted by {@linkplain ConfigConstants#CFG_TAG_KEY_DIR} that is located inside 
	 * the resources directory. The name of the private key should match the one denoted by {@linkplain ConfigConstants#CFG_TAG_PRIV_KEY}.
	 * <br><b>IMPORTANT:</b> should only be used after {@linkplain PropertyValues#setPathToResourceDirectory(String)} has been called once.
	 * @return the absolute path to the private key of the TSE simulator
	 */
	public String getPathToPrivateKey() {
		//absolute path to private key = pathToResourceDirectory/keyDir/privateKeyName 
		String pathToPrivateKey = pathToResourceDirectory+File.separator+instance.getValue(ConfigConstants.CFG_TAG_KEY_DIR)
			+File.separator+instance.getValue(ConfigConstants.CFG_TAG_PRIV_KEY);
		return pathToPrivateKey;
	}
	
	/**
	 * Getter for the absolute path to the directory denoted by {@linkplain ConfigConstants#CFG_TAG_PATH_TO_STORAGE} in config.properties. 
	 * This expects the directory to be a child directory of the resources directory, set by {@linkplain PropertyValues#setPathToResourceDirectory(String)}.
	 * The returned path ends with the name of the storage directory, <b>not</b> with a path separator character (e.g. <b>/</b> or <b>\</b>).
	 * <br><b>IMPORTANT:</b> should only be used after {@linkplain PropertyValues#setPathToResourceDirectory(String)} has been called once.
	 * @return the absolute path to the directory used as the normal storage of the TSE simulator. Normal in that context means, the storage where everything targetable by 
	 * {@linkplain SEAPI#deleteStoredData()} is stored.
	 */
	public String getPathToNormalStorage() {
		//path to normal storage = pathToResourceDirectory/storageDir
		String pathToNormalStorage = pathToResourceDirectory+File.separator+instance.getValue(ConfigConstants.CFG_TAG_PATH_TO_STORAGE);
		return pathToNormalStorage;
	}
	
	/**
	 * Getter for the absolute path to the directory denoted by {@linkplain ConfigConstants#CFG_TAG_PATH_TO_PERSISTENT_STORAGE} in config.properties. 
	 * This expects the directory to be a child directory of the resources directory, set by {@linkplain PropertyValues#setPathToResourceDirectory(String)}.
	 * The returned path ends with the name of the persistent storage directory, <b>not</b> with a path separator character (e.g. <b>/</b> or <b>\</b>).
	 * <br><b>IMPORTANT:</b> should only be used after {@linkplain PropertyValues#setPathToResourceDirectory(String)} has been called once.
	 * @return the absolute path to the directory used as persistent storage for the TSE simulator. 
	 */
	public String getPathToPersistentStorage() {
		//path to persistent storage = pathToResourceDirectory/persistentStorageDir
		String pathToPersistentStorage = pathToResourceDirectory+File.separator+instance.getValue(ConfigConstants.CFG_TAG_PATH_TO_PERSISTENT_STORAGE);
		return pathToPersistentStorage;
	}
	
	
 
	/**
	 * Gets the value corresponding to the key it has received.
	 * @param key searches for the value corresponding to the key in the Properties object that is created by reading from the config.properties file
	 * @return the value corresponding to the key or null if config.properties does not contain such a key.
	 * @since 1.0
	 */
    public String getValue(String key) {
    	return configValues.getProperty(key);
    }
    
    
    
    /**
     * Gets all the keys and packs them into a String Set.
     * @return a set containing all the keys currently associated with this PropertyValues instance.
     * @since 1.0
     */
    public Set<String> getAllPropertyNames(){
    	return configValues.stringPropertyNames();
    }
    
    /**
     * Checks if the String key is used as a key
     * @param key the supposed key as a String
     * @return true, if the key String is used as a key in config.properties
     * @since 1.0
     */
    public boolean containsKey(String key) {
    	return configValues.containsKey(key);
    }
    

    /**
     * A method to get the name of the currently used algorithm in the format that is used in {@linkplain  main.java.de.bsi.tsesimulator.constants.Constants}.
     * This String can then be used as a key to the HashMap in the same class to get the OID of that algorithm. 
     * This method prevents each class that has to know the algorithm OID from building it on its own.<br>
     * 
     * IMPORTANT: before using this method, the caller has to make sure, an instance of {@linkplain PropertyValues} exists, otherwise this method 
     * does not know how to load it.
     * @return the name of the signature algorithm in the format ALGORITHM_PLAIN_HASH_HASHLENGTH where ALGORITHM is ECDSA or ECSDSA, HASH is SHA or SHA3 and HASHLENGTH is 256, 384 or 512.
     * @since 1.5
     * @version 1.5
     */
    public String getConstantsAlgorithmName() {
    	StringBuilder constantsAlgoNameBuilder = new StringBuilder(instance.getValue(ConfigConstants.CFG_TAG_SIGNATURE_ALGORITHM).toUpperCase());
    	constantsAlgoNameBuilder.append("_PLAIN_").append(instance.getValue(ConfigConstants.CFG_TAG_HASH_METHOD).toUpperCase());
    	constantsAlgoNameBuilder.append("_").append(instance.getValue(ConfigConstants.CFG_TAG_HASH_LENGTH).toUpperCase());
    	
    	return constantsAlgoNameBuilder.toString();
    }
    
	
}
