/**
 * 
 */
package main.java.de.bsi.tsesimulator.preferences;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import main.java.de.bsi.tsesimulator.constants.ConfigConstants;
import main.java.de.bsi.tsesimulator.exceptions.AddingUserFailedException;
import main.java.de.bsi.tsesimulator.exceptions.RemovingUserFailedException;
import main.java.de.bsi.tsesimulator.exceptions.UserAlreadyExistsException;

/**
 * Used to read and write from and to the userlist.properties file located at the path given through the config.properties file. 
 * This is only necessary because this is a simulator, not an emulator or a real TSE and the simulator user might want to add or delete users 
 * from the TSE. This should never be used as an approach to the storage of user data by a real TSE.<br>
 * Another function of this file and class is to easily determine whether a user exists for the simulator or not.
 * @author dpottkaemper
 * @since 1.0
 * @version 1.5
 */
public class UserlistValues {
	
	private Properties userlistValues = new Properties();	//stores the Properties tied to userlist.properties (the file that stores the user names and roles)		
	private static UserlistValues instance;					//stores the singleton instance of the simulator
	private static String pathToUserlistProperties;			//stores the path to userlist.properties
	
	/**
	 * Used to obtain the singleton instance of the user list values.
	 * Has to be used after {@linkplain PropertyValues#setPathToResourceDirectory(String)}, because PropertyValues builds the path to the user list file.
	 * @return the UserlistValues instance
	 * @throws IOException if {@linkplain UserlistValues#loadUserlistFile()} throws it, because no instance of UserlistValues existed prior to this call and the 
	 * construction of the instance failed.
	 */
	public static UserlistValues getInstance() throws IOException {
		if(instance == null){
            instance = new UserlistValues();
        }
        return instance;
	}
	
	/**
	 * Private constructor, just calls {@linkplain UserlistValues#loadUserlistFile()}.
	 * @throws IOException if {@linkplain UserlistValues#loadUserlistFile()} throws it
	 */
	private UserlistValues() throws IOException {
		loadUserlistFile();
	}
	
	/**
	 * Uses {@linkplain PropertyValues#getPathToUserlistProperties()} to fetch the path where the user list file is stored. The file name is denoted by the value 
	 * of {@linkplain ConfigConstants#CFG_TAG_PATH_TO_USERLIST_PROPERTIES} in config.properties.<br>
	 * Creates a BufferedInputStream on the file denoted by the value in config.properties, then reads the values stored in userlist.properties and closes the stream afterwards.
	 * @throws IOException if an IOException occurs while reading the file userlist.properties or while using {@linkplain PropertyValues}
	 */
	private void loadUserlistFile() throws IOException {
		//fetch the path to userlist.properties from PropertyValues
		pathToUserlistProperties = PropertyValues.getInstance().getPathToUserlistProperties();
		//read the path to the userlist.properties from pathToUserlistProperties
		BufferedInputStream stream =  new BufferedInputStream(new FileInputStream(pathToUserlistProperties));

		//load the stream into the Properties
		userlistValues.load(stream);
		
		//try to close the stream reading from the Properties file
		stream.close();
	}
		
	
	/**
	 * Gets the value corresponding to the key it has received.
	 * @param key searches for the value corresponding to the key in the Properties object that is created by reading from the userlist.properties file
	 * @return the value corresponding to the key or null if userlist.properties does not contain such a key.
	 * @since 1.0
	 */
    public String getValue(String key) {
    	return userlistValues.getProperty(key);
    }
    
    
    /**
     * Gets all the keys and packs them into a String Set.
     * @return a set containing all the keys currently associated with this UserlistValues instance.
     * @since 1.0
     */
    public Set<String> getAllPropertyNames(){
    	return userlistValues.stringPropertyNames();
    }
    
    /**
     * Checks if the String key is used as a key
     * @param key the supposed key as a String
     * @return true, if the key String is used as a key in userlist.properties
     */
    public boolean containsKey(String key) {
    	return userlistValues.containsKey(key);
    }
    
    /**
     * calculates the number of key-value-pairs based on the size of the Set returned by {@linkplain #getAllPropertyNames()}.
     * @return the number of key-value-pairs stored in userlist.properties
     */
    public int getNumberOfKeyValuePairs() {
    	return getAllPropertyNames().size();
    }
    
//____________________________________________________Adding + Removing Users ______________________________________________________________________________   
    /**
     * Attempts to add a new key-value-pair to the userlist.properties file. Checks if the key already exists and if the role is one 
     * of the two permitted roles. If adding the pair was successful, the instance of UserlistValues is reloaded, to reflect the changes 
     * and make them accessible to the simulator.<br>
     * Because of a dependency on the existence of a UserlistValues instance, it is recommended that the caller of this function also calls {@linkplain UserlistValues#setPathToConfigProperties(String)}
     *  first.
     * @param key new user-id to be added to the list.
     * @param value the role associated with this user-id.
     * @throws UserAlreadyExistsException if attempting to add an already existing user to the simulator
     * @throws AddingUserFailedException if updating userlist.properties fails, most likely due to an IOException
     * @version 1.5
     */
    public void addUserAndRole(String key, String value) throws UserAlreadyExistsException, AddingUserFailedException {
    	//if the user name already exists inside the userlist.properties file, throw an Exception
    	try {
			if(UserlistValues.getInstance().containsKey(key)) {
				throw new UserAlreadyExistsException();
			}
		} catch (IOException e1) {
			throw new AddingUserFailedException(e1.getMessage(), e1);
		}
    	//if the value does not equal either "admin" or "timeAdmin", throw an Exception. the simulator will only allow those 2 roles
    	if(!(value.equalsIgnoreCase("admin") || value.equalsIgnoreCase("timeAdmin"))) {
    		throw new AddingUserFailedException(value +" is not a legal role!\n");
    	}
    	//set the new values
    	userlistValues.setProperty(key, value);
    	//write the new values to the userlist.properties file
    	try {
			writeToPropertiesFile_Add(key, value);
		} catch (IOException e) {
			throw new AddingUserFailedException("Writing to userlist.properties failed! Please validate that file!\n", e);
		}
    	//if writing to the file was successful, reload UserlistValues
    	instance=null;
    	try {
			UserlistValues.getInstance();
		} catch (IOException e) {
			throw new AddingUserFailedException("Reloading UserlistValues failed!\t"+e.getMessage(), e);
		}
    }
    
    
    /**
     * Writes an added key-value pair to the userlist.properties file.
     * @param key the user to be written to userlist.properties
     * @param value the role of that user to be written to userlist.properties 
     * @throws IOException if writing to the file fails
     * @version 1.5
     */
    private void writeToPropertiesFile_Add(String key, String value) throws IOException {
    	//create an OutputStream on the actual file
    	FileOutputStream outToUserlistProperties = new FileOutputStream(new File(pathToUserlistProperties));
    	//create a meaningful comment:
    	String comment = "Added " +key +" = " +value;
    	//use that stream
    	userlistValues.store(outToUserlistProperties, comment);
    }
    
   
    /**
     * Attempts to remove a key-value-pair from the userlist.properties file. Checks if the key exists and the role is one of the two permitted roles.
     * If removing the pair was successful, the instance of UserlistValues is reloaded, to reflect the changes 
     * and make them accessible to the simulator. 
     * @param key user-id to be removed from the list.
     * @param value the role associated with this user-id.
     * @throws RemovingUserFailedException if attempting to remove a user from the simulator that does not exist or who does not have the role specified. Is 
     * also thrown, if updating userlist.properties fails, most likely due to an IOException.
     * @version 1.5
     */
    public void removeUserAndRole(String key, String value) throws RemovingUserFailedException {
    	//if the user name does not exist inside the userlist.properties file, throw an Exception
    	try {
			if(!(UserlistValues.getInstance().containsKey(key))) {
				throw new RemovingUserFailedException();
			}
		} catch (IOException e1) {
			throw new RemovingUserFailedException(e1.getMessage(), e1);
		}
    	//if the value does not equal either "admin" or "timeAdmin", throw an Exception. the simulator will only allow those 2 roles
    	if(!(value.equalsIgnoreCase("admin") || value.equalsIgnoreCase("timeAdmin"))) {
    		throw new RemovingUserFailedException(value +" is not a legal role!\n");
    	}
    	
    	//remove the key-value pair from the userlist.properties file
    	if(!(userlistValues.remove(key, value))){
    		throw new RemovingUserFailedException("Failed to remove user, maybe the user does not have the specified role?\n");
    	}
    	//write the new values to the userlist.properties file   	
		try {
			writeToPropertiesFile_Delete(key, value);
		} catch (IOException e) {
			throw new RemovingUserFailedException("Writing to userlist.properties failed! Please validate that file!\n", e);
		}
		
    	//if writing to the file was successful, reload UserlistValues
    	instance=null;
    	try {
			UserlistValues.getInstance();
		} catch (IOException e) {
			throw new RemovingUserFailedException("Reloading UserlistValues failed!\t"+e.getMessage(), e);
		}
    }
    
    /**
     * Writes the removal of a key-value pair to the userlist.properties file.
     * @param key the user to be removed from userlist.properties
     * @param value the role of that user 
     * @throws IOException if writing to the file fails
     * @version 1.5
     */
    private void writeToPropertiesFile_Delete(String key, String value) throws IOException {
    	//create an OutputStream on the actual file
    	FileOutputStream outToUserlistProperties = new FileOutputStream(new File(pathToUserlistProperties));
    	//create a meaningful comment:
    	String comment = "Removed " +key +" = " +value;
    	//use that stream
    	userlistValues.store(outToUserlistProperties, comment);
    }
    
	
}
