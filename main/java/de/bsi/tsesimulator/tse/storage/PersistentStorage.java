/**
 * 
 */
package main.java.de.bsi.tsesimulator.tse.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import main.java.de.bsi.tsesimulator.constants.Constants;
import main.java.de.bsi.tsesimulator.exceptions.AddingUserFailedException;
import main.java.de.bsi.tsesimulator.exceptions.LoadingFailedException;
import main.java.de.bsi.tsesimulator.exceptions.ModifyingUserFailedException;
import main.java.de.bsi.tsesimulator.exceptions.PersistingFailedException;
import main.java.de.bsi.tsesimulator.exceptions.RemovingUserFailedException;
import main.java.de.bsi.tsesimulator.exceptions.TooManyUsersException;
import main.java.de.bsi.tsesimulator.exceptions.UserAlreadyExistsException;
import main.java.de.bsi.tsesimulator.preferences.PropertyValues;
import main.java.de.bsi.tsesimulator.preferences.UserlistValues;
import main.java.de.bsi.tsesimulator.tlv.TLVUtility;
import main.java.de.bsi.tsesimulator.tse.TSEController;
import main.java.de.bsi.tsesimulator.tse.usermanagement.User;
import main.java.de.bsi.tsesimulator.utils.filefilters.UserDataFileFilter;

/**
 * Stores the values that the TSE needs when it is started. For example, it stores the time when the TSE simulator is shut down correctly.
 * The path it stores the values in can be specified through the config.properties file. The last values are <b>always</b> saved to a file
 * named persistentValues.
 * Values are stored through the creation of a Java Object ({@linkplain PersistedValues} and the serialization of that object.
 * 
 * <br><br>Is responsible for finding the user data as well, e.g. comparing the PIN and the PUK that is entered to the ones stored in a file.
 * @author dpottkaemper
 * @since 1.0
 * @version 1.5
 */
public class PersistentStorage {
	
	private String pathToPersistentStorageDir;	//stores path to persistent storage for easy access 
	
	/**
	 * Default constructor for a PersistentStorage object. It uses {@linkplain PropertyValues#getPathToPersistentStorage()} to fetch the path to the directory used 
	 * as persistent storage. Due to how {@linkplain PropertyValues} builds the path and accesses the configuration file, it is necessary, that {@linkplain PropertyValues#setPathToResourceDirectory(String)}
	 *  has been called beforehand.
	 * @throws LoadingFailedException if constructing the path in {@linkplain PropertyValues} fails. 
	 * @version 1.5
	 */
	public PersistentStorage() throws LoadingFailedException {
		try {
			pathToPersistentStorageDir = PropertyValues.getInstance().getPathToPersistentStorage();
		} catch (IOException e) {
			throw new LoadingFailedException("Reading path to persistent storage from config.properties failed. Original message:\n" +e.getMessage(), e);
		}
	}
	
	
//---------------------------------------------PERSISTENT VALUES RELATED FUNCTIONS------------------------------------------------
	/**
	 * Searches for a file named "persistentValues.ser" in the path that leads to the directory storing persistent values.
	 * If the file exists, it was likely created by this class.
	 * @return true, if the persistentValues file in the persistentStorageDir exists. False otherwise.
	 */
	public boolean persistentValuesExist() {
		File storageDirectory = new File(pathToPersistentStorageDir);
		if(storageDirectory.exists()) {
			File persistentValues = new File(storageDirectory, "persistentValues.ser");
			if(persistentValues.exists()) {
				return true;
			}
		}
		return false;
	}
	

	/**
	 * Persists the values that shall be loaded in on start of the TSE simulator.
	 * Checks if a persistence file is already present. If there is, it is de-serialized from the file and the values are checked.
	 * If for example the new signature counter is less than the old signature counter, there is an error and this is not persisted.
	 * 
	 * @param cryptoCoreClockStatus the time of the clock of the CryptoCore in Unix time at the moment the method was called.
	 * @param tseIsInitialized the initialization status of the TSE.
	 * @param seIsDisabled the status of the secure element at the time of the method call.
	 * @param sigCntr the signature counter at the time of the method call.
	 * @param transactionCntr the transaction number at the time of the method call
	 * @param descriptionOfSEAPI the description of the SE API set inside the TSE
	 * @throws PersistingFailedException if the serialization or de-serialization of a file fails. is also thrown if a file reference
	 * is not found or an IOException occurs.
	 * @version 1.5
	 */
	public void storeLatestValues(long cryptoCoreClockStatus, boolean tseIsInitialized, boolean seIsDisabled, long sigCntr, long transactionCntr, String descriptionOfSEAPI) throws PersistingFailedException {
		//create a file object of the persistent storage directory
		File storageDirectory = new File(pathToPersistentStorageDir);
		
		//if the storageDirectory does not exist, make one
		if(!storageDirectory.exists()) {
			storageDirectory.mkdirs();
		}
		
		File persistenceFile = new File(storageDirectory, Constants.FILE_NAME_PERSISTEDVALUES_SER);
		//if there's a persistence file present, de-persist that and check each value against the new to be persisted value
		if(persistenceFile.exists()) {
			PersistedValues oldPersistedValues = null;
			try {
				//read from the old persisted file and create a new PersistedValues object
				FileInputStream fileInStream = new FileInputStream(persistenceFile);
				ObjectInputStream objectInStream = new ObjectInputStream(fileInStream);
				oldPersistedValues = (PersistedValues) objectInStream.readObject();
				//close the streams
				objectInStream.close();
				fileInStream.close();
				
				//check the values that can be checked, e.g. DO NOT CHECK THE CLOCK!
				if(oldPersistedValues.getSignatureCounterStatus() > sigCntr) {
					throw new PersistingFailedException("The old signature counter has a bigger value than the new signature counter. This should not happen!");
				}
				if(oldPersistedValues.getTransactionNumberStatus() > transactionCntr) {
					throw new PersistingFailedException("The old transaction counter has a bigger value than the new transaction counter. This should not happen!");
				}
				//if the old value was disabled and now it is not
				if(oldPersistedValues.getSeIsDisabled() && (!seIsDisabled)) {
					throw new PersistingFailedException("Should not be possible to re-enable a disabled SE.");
				}
				//if the old was initialized and now it is not
				if(oldPersistedValues.getTseIsInitialized() && (!tseIsInitialized)) {
					throw new PersistingFailedException("Should not be possible to de-initialize an initialized TSE.");
				}
				//if the old value had a description of the SE API that is different to the one present
				if(!oldPersistedValues.getDescriptionOfTheSEAPI().equals(descriptionOfSEAPI)) {
					throw new PersistingFailedException("Should not be possible to remove or modify a set description of a TSE.");
				}	
			} catch (FileNotFoundException e) {
				throw new PersistingFailedException("FileNotFoundException caught. The older persited value could not be found.\n", e);
			} catch (IOException e) {
				throw new PersistingFailedException("IOException caught. The older persisted values could not be de-serialized.\n", e);
			} catch (ClassNotFoundException e) {
				throw new PersistingFailedException("ClassNotFoundException caught. There seems to be no matching PersistedValues class.\n" , e);
			}
			//delete the old persisted file
			persistenceFile.delete();
		}
		
		//if everything is newer than the old persisted file or there was no persisted file in the first place
		PersistedValues newValues = new PersistedValues(cryptoCoreClockStatus, tseIsInitialized, seIsDisabled, sigCntr, transactionCntr, descriptionOfSEAPI);
		
		//Serialize the new values and save it
		try {
			FileOutputStream fileOutStream = new FileOutputStream(persistenceFile);
			ObjectOutputStream objectOutStream = new ObjectOutputStream(fileOutStream);
			objectOutStream.writeObject(newValues);
			//close the streams
			objectOutStream.close();
			fileOutStream.close();
		} catch (FileNotFoundException e) {
			throw new PersistingFailedException("FileNotFoundException caught. The file reference could not be resolved.\n", e);
		} catch (IOException e) {
			throw new PersistingFailedException("IOException caught. The new values could not be serialized.\n", e);
		}
	}
	
	
	
	/**
	 * Used to load the previously persisted values from the persistentValues.ser file.
	 * {@linkplain #persistentValuesExist()} should be called beforehand.
	 * @return the values retrieved from the persisted file.
	 * @throws LoadingFailedException if loading the persisted file fails for any reason
	 */
	public PersistedValues loadFromPersistedFile() throws LoadingFailedException {
		PersistedValues loadedValues = null;
		File persistenceFile = new File(pathToPersistentStorageDir, Constants.FILE_NAME_PERSISTEDVALUES_SER);
		if(!(persistenceFile.exists())) {
			return null;
		}
		
		try {
			FileInputStream fileIn = new FileInputStream(persistenceFile);
			ObjectInputStream objIn = new ObjectInputStream(fileIn);
			//read the PersistedValue from the file
			loadedValues = (PersistedValues) objIn.readObject();
			//close the streams
			objIn.close();
			fileIn.close();
		} catch (IOException|ClassNotFoundException e) {
			throw new LoadingFailedException(e.getMessage(), e);
		} 
		return loadedValues;
	}
	
	/**
	 * Deletes persisted values for the simulator. Is only useful in the TSE-Simulator context, should not be implemented in a real TSE.
	 * <br>Does not delete any user related files! Use the separate method {@linkplain TSEController#removeAllUsersFromSimulator()} to delete all users.
	 */
	public void deletePersistedValues() {
		File persistenceFile = new File(pathToPersistentStorageDir, Constants.FILE_NAME_PERSISTEDVALUES_SER);
		if(persistenceFile.exists()) {
			persistenceFile.delete();
		}
	}
	
//-------------------------------------------------USER FILE MANAGEMENT---------------------------------------------------------------------------------------
	/**
	 * Attempts to add a user to the simulator and create an entry for it in the userlist.properties file and the persistentStorageDir.
	 * All user data files are named "UserID_xyz_.dat" and contain the user data as a Java serialized file.<br>
	 * Please note, this is a feature that is not described in TR-03151 and is only featured for the sake of simulating a TSE. Without the ability to add and 
	 * and remove users, one would only be able to use users that were added in the production process of the simulator.
	 * @param newUser the user to be added to the simulator with all its parameters set.
	 * @throws TooManyUsersException if the number of users would exceed {@linkplain Constants#MAX_STORED_USERS} if the newUser was added.
	 * @throws UserAlreadyExistsException the user seems to already exists, whether this is in the userlist.properties or in the persistentStorageDir
	 * @throws IllegalArgumentException the userId or the role did not match the requirements. The ID has to be an ASN1 PrintableString and max
	 * {@linkplain Constants#MAX_USERID_LENGTH} long and the role has to be admin or timeAdmin.
	 * @throws AddingUserFailedException if for any reason the attempt to write the user to a user data file fails. Most common cause
	 * are IOExceptions.
	 */
	public void addUser(User newUser) throws TooManyUsersException, IllegalArgumentException, AddingUserFailedException, UserAlreadyExistsException {
	//look if the role and the ID match what is permitted
		if(!TLVUtility.isASN1_PrintableString(newUser.getUserID())) {
			throw new IllegalArgumentException("This userID must be an ASN.1 PrintableString");
		}
		
		if(newUser.getUserID().length() > Constants.MAX_USERID_LENGTH) {
			throw new IllegalArgumentException("This userID must be max 100 characters long.");
		}
		
		if(!(newUser.getRole().equalsIgnoreCase("admin")) && !(newUser.getRole().equalsIgnoreCase("timeAdmin"))) {
			throw new IllegalArgumentException("The user role is either admin or timeAdmin");
		}
		
		
	//look at the number of already stored users. Does it equal Constants.MAX_STORED_USERS?
		File storageDir = new File(pathToPersistentStorageDir);
		int filesFound = 0;
		//create a FileFilter to search for already present user data
		UserDataFileFilter userDataFilter = new UserDataFileFilter("UserID_");
		//list all files containing user data in an array using a UserDataFileFilter
		File[] currentlyPresentUserDataFiles = storageDir.listFiles(userDataFilter);
		//list the number of entries in userlist.properties
		short currentlyListedInUserlist = 0;
		//has to be inside try-catch, because persistent storage does not know whether UserlistValues.setPathToUserlistProperties has been used
		try {
			currentlyListedInUserlist = (short)UserlistValues.getInstance().getNumberOfKeyValuePairs();
		} catch (IOException e1) {
			throw new AddingUserFailedException("Reading from user list failed. Original message:\n"+e1.getMessage(), e1);
		}
		//check if there is something wrong with the number of user data files, the number of user data entries in userlist.properties, or both
		if(currentlyPresentUserDataFiles != null) {
			filesFound = currentlyPresentUserDataFiles.length;
			if(currentlyListedInUserlist != (short) filesFound) {
				throw new TooManyUsersException("\nalready present user data files: " +filesFound +"\nalready present user data according to userlist.properties: " +currentlyListedInUserlist
						+"\nBEWARE: mismatch between user data files and userlist.properties entries");
			}
			if(((short) filesFound >= Constants.MAX_STORED_USERS) || (currentlyListedInUserlist >= Constants.MAX_STORED_USERS)) {
				throw new TooManyUsersException("\nMAX_STORED_USERS = " +Constants.MAX_STORED_USERS +"\nalready present user data files: " +filesFound
						+"\nalready present user data according to userlist.properties: " +currentlyListedInUserlist);
			}
		}
	//if the number of already stored user data files does not exceed the maximum number, check if the user is already present:
		//create a new UserDataFileFilter for the file names that begin with the same user id
		String fileNameBeginning = "UserID_" +newUser.getUserID() +"_";
		UserDataFileFilter specificUserDataFilter = new UserDataFileFilter(fileNameBeginning);
		File[] filesWithSameName = storageDir.listFiles(specificUserDataFilter);
		
		
		//if the filesWithsameName array is not null, throw an Exception because that userId already exists
		if(filesWithSameName.length > 0) {
			throw new UserAlreadyExistsException("The userID that was attempted to be added already exists. Please try another userID.");
		}
		
	//create a file to write the data into
		String realFileNameWithExtension = fileNameBeginning +".dat";
		File userDataFile = new File(storageDir, realFileNameWithExtension);

		try {
			FileOutputStream fileOutStream = new FileOutputStream(userDataFile);
			ObjectOutputStream objectOutStream = new ObjectOutputStream(fileOutStream);
			objectOutStream.writeObject(newUser);
			//close the streams
			objectOutStream.close();
			fileOutStream.close();
		} catch(IOException e) {
			throw new AddingUserFailedException("Saving userdata to file failed.\n", e);
		}

	//add the user and role combination to the userlist.properties file via UserlistValues.
		//UserlistValues writes this to userlist.properties
		try {
			UserlistValues.getInstance().addUserAndRole(newUser.getUserID(), newUser.getRole());
		} catch (UserAlreadyExistsException e) {
			userDataFile.delete();
			throw new UserAlreadyExistsException("User does not exist in persistentStorageDir but in userlist.properties. \nUser data was not saved!");
		} catch (AddingUserFailedException e) {
			throw new AddingUserFailedException(e);
		} catch (IOException e) {
			throw new AddingUserFailedException("Reading from/writing to user list failed. Original message:\n"+e.getMessage(), e);
		}
	}
	
	/**
	 * Used to update the parameters that describe a certain user. This is used for resetting the number of retries and modifying the PIN and PUK.
	 * Called by the {@linkplain TSEController} in methods such as {@linkplain TSEController#unblockUser(String, byte[], byte[], main.java.de.bsi.seapi.holdertypes.UnblockResultHolder)}.
	 * The method checks, whether the caller is trying to modify the role of the user but other necessary checks are to be performed by the caller.
	 * This includes checking if the number of remaining retries is legal and that the PIN and PUK have the necessary length. 
	 * 
	 * @param modifiedUser the {@linkplain User} whose updated values shall be stored
	 * @throws ModifyingUserFailedException if modifying the user data fails due to IOExceptions or due to illegal operations
	 * @throws IllegalArgumentException the userId or the role did not match the requirements. The ID has to be an ASN1 PrintableString and max
	 * {@linkplain Constants#MAX_USERID_LENGTH} long and the role has to be admin or timeAdmin.
	 */
	public void writeModifiedUserdataToStorage(User modifiedUser) throws ModifyingUserFailedException, IllegalArgumentException {
		//0. look if the role and the ID match what is permitted
		if(!TLVUtility.isASN1_PrintableString(modifiedUser.getUserID())) {
			throw new IllegalArgumentException("This userID must be an ASN.1 PrintableString");
		}
		
		if(modifiedUser.getUserID().length() > Constants.MAX_USERID_LENGTH) {
			throw new IllegalArgumentException("This userID must be max 100 characters long.");
		}
		
		if(!(modifiedUser.getRole().equalsIgnoreCase("admin")) && !(modifiedUser.getRole().equalsIgnoreCase("timeAdmin"))) {
			throw new IllegalArgumentException("The user role is either admin or timeAdmin");
		}
		
		//1. check, if the user exists. Only an existing User can be modified.
			//User exists, when they	a: have an entry in userlist.properties
			//and						b: have a serialized file associated with them
		File storageDir = new File(pathToPersistentStorageDir);
		String fileNameBeginning = "UserID_" +modifiedUser.getUserID() +"_";
		UserDataFileFilter filterModifiedUser = new UserDataFileFilter(fileNameBeginning);
		File[] modifiedUserFile = storageDir.listFiles(filterModifiedUser);
		boolean isInUserlistProperties = false;
		//surround with try-catch, because maybe UserlistValues does not know where userlist.properties is located
		try {
			isInUserlistProperties = UserlistValues.getInstance().containsKey(modifiedUser.getUserID());
		} catch (IOException e1) {
			throw new ModifyingUserFailedException("Reading from user list failed. Original message:\n"+e1.getMessage(), e1);
		}
		
		if((modifiedUserFile == null) || (modifiedUserFile.length != 1) || (!isInUserlistProperties)) {
			throw new ModifyingUserFailedException("User does not exist in persistent storage and/or in userlist.properties!\n");
		}
		
		//2. after we made sure that the user exists, we have to compare the previous role and the current role. We DO NOT want to be able to change the role!
			//There should be no way, that a simple timeAdmin is promoted to admin
			//-> read the old User from the persisted file
		User oldUserValues = null;
		String oldUserRoleInUserlistProperties = null;
		//surround with try-catch because maybe UserlistValues does not know where userlist.properties is located
		try {
			//old user role according to userlist.properties
			oldUserRoleInUserlistProperties = UserlistValues.getInstance().getValue(modifiedUser.getUserID());
		} catch (IOException e1) {
			throw new ModifyingUserFailedException("Reading from user list failed. Original message:\n"+e1.getMessage(), e1);
		}	
		
		try {
			FileInputStream fileInStream = new FileInputStream(modifiedUserFile[0]);
			ObjectInputStream objectInStream = new ObjectInputStream(fileInStream);
			oldUserValues = (User) objectInStream.readObject();
			//close the streams
			objectInStream.close();
			fileInStream.close();
			//check, if new role == old roles (in both the serialized and the userlist.properties file)
			if(! ((modifiedUser.getRole().equals(oldUserValues.getRole()) && (modifiedUser.getRole().equals(oldUserRoleInUserlistProperties)))) ) {
				throw new ModifyingUserFailedException("Invalid operation, role can not be modified!\n");
			}
			//if the role is not being modified, delete the old persistence file
			modifiedUserFile[0].delete();
			
		} catch (IOException |ClassNotFoundException e) {
			throw new ModifyingUserFailedException(e.getMessage(), e);
		} 
		
		//Serialize the new values and save it
		try {
			FileOutputStream fileOutStream = new FileOutputStream(modifiedUserFile[0]);
			ObjectOutputStream objectOutStream = new ObjectOutputStream(fileOutStream);
			objectOutStream.writeObject(modifiedUser);
			//close the streams
			objectOutStream.close();
			fileOutStream.close();
		} catch(IOException e) {
			throw new ModifyingUserFailedException(e.getMessage(), e);
		}
	}
	
	
	/**
	 * Removes a {@linkplain User} from both the persistentStorageDir and the userlist.properties file. This method is only present for testing purposes.
	 * It does <b>not</b> check, whether any User(s) would be left after removing one. The caller has to make sure, that there still are users present for 
	 * the simulator to use.<br>
	 * If the user that shall be deleted does not exist, this method throws an exception.
	 * @param deleteUserId the user that shall be deleted
	 * @throws RemovingUserFailedException if something happens during the removal process, for example, there is no user to delete.
	 */
	public void removeUser(String deleteUserId) throws RemovingUserFailedException {
		//0. check, if the UserId is an ASN.1 printable String
		if(!TLVUtility.isASN1_PrintableString(deleteUserId)){
			throw new RemovingUserFailedException("Input userId was not an ASN.1 PrintableString!");
		}
		//1. check, if the user exists. Only an existing User can be modified.
			//User exists, when they	a: have an entry in userlist.properties
			//and						b: have a serialized file associated with them
		File storageDir = new File(pathToPersistentStorageDir);
		String fileNameBeginning = "UserID_" +deleteUserId +"_";
		UserDataFileFilter filterDeleteUser = new UserDataFileFilter(fileNameBeginning);
		File[] deleteUserFile = storageDir.listFiles(filterDeleteUser);
		boolean isInUserlistProperties = false;
		//surround with try-catch, because maybe UserlistValues does not know where userlist.properties is located
		try {
			isInUserlistProperties = UserlistValues.getInstance().containsKey(deleteUserId);
		} catch (IOException e) {
			throw new RemovingUserFailedException("Reading from user list failed. Original message:\n"+e.getMessage(), e);
		}
		
		if((deleteUserFile == null) || (deleteUserFile.length != 1) || (!isInUserlistProperties)) {
			throw new RemovingUserFailedException("User does not exist in persistent storage and/or in userlist.properties!\n");
		}
		
		//2. delete user from userlist.properties and persistentStorageDir
		String deleteUserRole;
		try {
			deleteUserRole = UserlistValues.getInstance().getValue(deleteUserId);
			UserlistValues.getInstance().removeUserAndRole(deleteUserId, deleteUserRole);
		} catch (IOException e) {
			throw new RemovingUserFailedException("Reading from/writing to user list failed. Original message:\n"+e.getMessage(), e);
		}
		
		deleteUserFile[0].delete();
	}
	
	/**
	 * This method loads a {@linkplain User} from a file in the directory specified by persistentStorageDir in config.properties. 
	 * Loading a user will most likely occur whenever the {@linkplain TSEController} attempts to perform some kind of user related operation.
	 * Loading a user from a file is necessary for checking the stored PIN and/or PUK against the one provided to the TSEController.
	 * @param userId the ID of the user that shall be loaded. Calling method has to make sure the user id exists.
	 * @return a {@linkplain User} constructed from the data on the file system
	 * @throws LoadingFailedException if something happens to the loading operation, for example the file does not exist or there are too many files with that user ID.
	 */
	public User loadUser(String userId) throws LoadingFailedException {
		//create a filter for finding the user
		String fileNameBeginning = "UserID_" +userId +"_";
		File directoryReference = new File(pathToPersistentStorageDir);
		
		//create a reference to the file where this user data is stored
		UserDataFileFilter specificUserDataFilefilter = new UserDataFileFilter(fileNameBeginning);
		File[] userDataFiles = directoryReference.listFiles(specificUserDataFilefilter);
		
		if((userDataFiles == null) || (userDataFiles.length == 0)) {
			throw new LoadingFailedException("No user data file with specified ID.");
		}
		if(userDataFiles.length > 1) {
			throw new LoadingFailedException("Too many users with that ID.");
		}
		
		//create a User object for storing the values
		User loadedUser = null;
		
		try {	
			FileInputStream fileInStream = new FileInputStream(userDataFiles[0]);
			ObjectInputStream objectInStream = new ObjectInputStream(fileInStream);
			loadedUser = (User) objectInStream.readObject();
			//close the streams
			objectInStream.close();
			fileInStream.close();
		} catch(IOException | ClassNotFoundException e)	{
			throw new LoadingFailedException(e.getMessage(), e);
		}
		
		//return the finished User
		return loadedUser;
	}
	
	

}
