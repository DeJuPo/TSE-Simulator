/**
 * 
 */
package main.java.de.bsi.tsesimulator.tse;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bouncycastle.util.Arrays;

import main.java.de.bsi.seapi.Constant;
import main.java.de.bsi.seapi.SEAPI;
import main.java.de.bsi.seapi.exceptions.ErrorCertificateExpired;
import main.java.de.bsi.seapi.exceptions.ErrorDeleteStoredDataFailed;
import main.java.de.bsi.seapi.exceptions.ErrorDescriptionNotSetByManufacturer;
import main.java.de.bsi.seapi.exceptions.ErrorDescriptionSetByManufacturer;
import main.java.de.bsi.seapi.exceptions.ErrorDisableSecureElementFailed;
import main.java.de.bsi.seapi.exceptions.ErrorExportCertFailed;
import main.java.de.bsi.seapi.exceptions.ErrorExportSerialNumbersFailed;
import main.java.de.bsi.seapi.exceptions.ErrorFinishTransactionFailed;
import main.java.de.bsi.seapi.exceptions.ErrorGetCurrentNumberOfClientsFailed;
import main.java.de.bsi.seapi.exceptions.ErrorGetCurrentNumberOfTransactionsFailed;
import main.java.de.bsi.seapi.exceptions.ErrorGetMaxNumberOfClientsFailed;
import main.java.de.bsi.seapi.exceptions.ErrorGetMaxNumberTransactionsFailed;
import main.java.de.bsi.seapi.exceptions.ErrorGetSupportedUpdateVariantsFailed;
import main.java.de.bsi.seapi.exceptions.ErrorGetTimeSyncVariantFailed;
import main.java.de.bsi.seapi.exceptions.ErrorIdNotFound;
import main.java.de.bsi.seapi.exceptions.ErrorInvalidTime;
import main.java.de.bsi.seapi.exceptions.ErrorNoDataAvailable;
import main.java.de.bsi.seapi.exceptions.ErrorNoLogMessage;
import main.java.de.bsi.seapi.exceptions.ErrorNoTransaction;
import main.java.de.bsi.seapi.exceptions.ErrorParameterMismatch;
import main.java.de.bsi.seapi.exceptions.ErrorReadingLogMessage;
import main.java.de.bsi.seapi.exceptions.ErrorRestoreFailed;
import main.java.de.bsi.seapi.exceptions.ErrorRetrieveLogMessageFailed;
import main.java.de.bsi.seapi.exceptions.ErrorSeApiNotInitialized;
import main.java.de.bsi.seapi.exceptions.ErrorSecureElementDisabled;
import main.java.de.bsi.seapi.exceptions.ErrorSigningSystemOperationDataFailed;
import main.java.de.bsi.seapi.exceptions.ErrorStartTransactionFailed;
import main.java.de.bsi.seapi.exceptions.ErrorStorageFailure;
import main.java.de.bsi.seapi.exceptions.ErrorStoringInitDataFailed;
import main.java.de.bsi.seapi.exceptions.ErrorTimeNotSet;
import main.java.de.bsi.seapi.exceptions.ErrorTooManyRecords;
import main.java.de.bsi.seapi.exceptions.ErrorTransactionNumberNotFound;
import main.java.de.bsi.seapi.exceptions.ErrorUnexportedStoredData;
import main.java.de.bsi.seapi.exceptions.ErrorUpdateTimeFailed;
import main.java.de.bsi.seapi.exceptions.ErrorUpdateTransactionFailed;
import main.java.de.bsi.seapi.exceptions.ErrorUserIdNotAuthenticated;
import main.java.de.bsi.seapi.exceptions.ErrorUserIdNotManaged;
import main.java.de.bsi.seapi.exceptions.ErrorUserNotAuthenticated;
import main.java.de.bsi.seapi.exceptions.ErrorUserNotAuthorized;
import main.java.de.bsi.seapi.holdertypes.AuthenticationResultHolder;
import main.java.de.bsi.seapi.holdertypes.ByteArrayHolder;
import main.java.de.bsi.seapi.holdertypes.LongHolder;
import main.java.de.bsi.seapi.holdertypes.ShortHolder;
import main.java.de.bsi.seapi.holdertypes.SyncVariantsHolder;
import main.java.de.bsi.seapi.holdertypes.UnblockResultHolder;
import main.java.de.bsi.seapi.holdertypes.UpdateVariantsHolder;
import main.java.de.bsi.seapi.holdertypes.ZonedDateTimeHolder;
import main.java.de.bsi.tsesimulator.constants.ASN1Constants;
import main.java.de.bsi.tsesimulator.constants.ConfigConstants;
import main.java.de.bsi.tsesimulator.constants.Constants;
import main.java.de.bsi.tsesimulator.exceptions.AddingUserFailedException;
import main.java.de.bsi.tsesimulator.exceptions.ClientIdAlreadyRegisteredException;
import main.java.de.bsi.tsesimulator.exceptions.ClientIdNotRegisteredException;
import main.java.de.bsi.tsesimulator.exceptions.ErrorSignatureCounterOverflow;
import main.java.de.bsi.tsesimulator.exceptions.ErrorTransactionCounterOverflow;
import main.java.de.bsi.tsesimulator.exceptions.GracefulShutdownFailedException;
import main.java.de.bsi.tsesimulator.exceptions.LoadingFailedException;
import main.java.de.bsi.tsesimulator.exceptions.ModifyingUserFailedException;
import main.java.de.bsi.tsesimulator.exceptions.NoExternalConfigFilesException;
import main.java.de.bsi.tsesimulator.exceptions.PersistingFailedException;
import main.java.de.bsi.tsesimulator.exceptions.RemovingUserFailedException;
import main.java.de.bsi.tsesimulator.exceptions.SignatureCounterException;
import main.java.de.bsi.tsesimulator.exceptions.SigningOperationFailedException;
import main.java.de.bsi.tsesimulator.exceptions.TLVException;
import main.java.de.bsi.tsesimulator.exceptions.TooManyClientsException;
import main.java.de.bsi.tsesimulator.exceptions.TooManyOpenTransactionsException;
import main.java.de.bsi.tsesimulator.exceptions.TooManyUsersException;
import main.java.de.bsi.tsesimulator.exceptions.UserAlreadyExistsException;
import main.java.de.bsi.tsesimulator.exceptions.ValueNullException;
import main.java.de.bsi.tsesimulator.exceptions.ValueTooBigException;
import main.java.de.bsi.tsesimulator.msg.LogMessage;
import main.java.de.bsi.tsesimulator.msg.SystemLogMessage;
import main.java.de.bsi.tsesimulator.msg.TransactionLogMessage;
import main.java.de.bsi.tsesimulator.preferences.PropertyValues;
import main.java.de.bsi.tsesimulator.preferences.UserlistValues;
import main.java.de.bsi.tsesimulator.tlv.TLVObject;
import main.java.de.bsi.tsesimulator.tlv.TLVUtility;
import main.java.de.bsi.tsesimulator.tse.storage.PersistedValues;
import main.java.de.bsi.tsesimulator.tse.storage.PersistentStorage;
import main.java.de.bsi.tsesimulator.tse.storage.Storage;
import main.java.de.bsi.tsesimulator.tse.usermanagement.User;





/**
 * Represents the class that appears as a <b>T</b>echnische <b>S</b>icherheits <b>E</b>inrichtung (technical security application) to the outside world.
 * It attempts to simulate such a TSE as defined in BSI TR-03153 and BSI-03151 and the additional technical guidelines mentioned there.
 * This class implements most of the Secure Element API ({@linkplain SEAPI} defined by TR-03151 and attempts to simulate the behavior of those functions 
 * as accurate as possible.<br>
 * 
 * This TSEController is the main class with which the user of the TSE-simulator should interact. Since the usage of this class may be rather complicated, 
 * it is necessary for the user to refer to the user guide of this simulator. That user guide describes in detail how the simulator can and should be configurated 
 * and used in own projects.<br><br>
 * 
 * Please be aware of the fact, that this program is only a simulator, <b>not</b> the one-and-only implementation of a TSE and should not be viewed as such.
 * 
 * @author dpottkaemper
 * @version 1.5
 * @since 1.0
 */
public class TSEController implements SEAPI {
	private SecurityModule securityModule;
	private Storage storage;
	private PersistentStorage persistentStorage;

	boolean tseIsInitialized = false;					//keeps track of whether the TSE has been initialized or not. 
	
	private String descriptionOfTheSEAPI;				//represents the description of the SE API that has been set by the manufacturer or 
														//should have been set with the initialization of the TSE
	private String manufacturerInformation = Constants.DEFAULT_MANUFACTURER_INFORMATION;	//to be used in the creation of the info.csv file by the storage class.
																							//what value this should have is not specified (?)
	private String versionInformation = Constants.DEFAULT_VERSION_INFORMATION;	//to be used in the creation of the info.csv file. What value this shall have is not specified.
	
	private User currentlyLoggedIn;						//stores the User that is currently logged in. Is initialized as null
														//in the constructor until a user logs in successfully. A "logged in user" in this sense is a user that
														//has been authenticated
	
	
//------------------------------------------CONSTRUCTORS----------------------------------------------------------------------------
	/**
	 * Default constructor for the TSEController. The TSEController constructs a SecurityModule (SecurityModule) and a Storage.
	 * If the construction of the security module fails due to an IOException, this constructor throws a LoadingFailedException.
	 * 
	 * <br>Depending on whether persisted values are found under the directory path specified in config.properties this method behaves differently.
	 * If {@linkplain PersistentStorage#persistentValuesExist()} returns true, the method attempts to read the values from the file and 
	 * uses them to construct the {@linkplain SecurityModule} and therefore the {@linkplain ERSSpecificModule} and {@linkplain CryptoCore}. This means that
	 * the clock and the signature counter of the CryptoCore are set to the stored values, as is the transaction counter in the ERSSpecificModule and the 
	 * stati of the TSE and the SecurityModule.
	 * <br>If no persistence file is found, everything is constructed with its default values. That means, "0" for the transaction number and the
	 * signature counter, "false" for seIsDisabled and "false" for tseIsInitialized.
	 * @throws LoadingFailedException if the constructor of the {@linkplain SecurityModule}, {@linkplain Storage} or {@linkplain PersistentStorage} throws an IOException,
	 * and/or if reading the configuration file fails due to a missing path to the resource directory. <br> Mitigation: Use {@linkplain PropertyValues#setPathToResourceDirectory(String)} before 
	 * constructing the TSEController.
	 */
	public TSEController() throws LoadingFailedException {
		//create the storages. they are independent (more or less) from the saved values.
		this.storage = new Storage();
		this.persistentStorage = new PersistentStorage();
		
		//check if a persistence file exists that can be de-serialized
		if(this.persistentStorage.persistentValuesExist()) {
			//get the values that can be loaded in
			PersistedValues loaded = this.persistentStorage.loadFromPersistedFile();
			
			//set the initialization status to what has been loaded
			this.tseIsInitialized = loaded.getTseIsInitialized();
			//create the security module with all loaded parameters
			try {
				this.securityModule = new SecurityModule(loaded.getSeIsDisabled(), loaded.getSignatureCounterStatus(), loaded.getTransactionNumberStatus(), 
						loaded.getCryptoCoreClockStatus());
			} catch (IOException e) {
				throw new LoadingFailedException("IOException caught. Most likely cause: keys for signature algorithms"
						+ " could not be loaded.\n", e);
			}
			this.descriptionOfTheSEAPI = loaded.getDescriptionOfTheSEAPI();
		}
		
		//if there are no persisted values, fill everything with defaults
		else {
			try {
				this.securityModule = new SecurityModule();
			} catch (IOException e) {
				throw new LoadingFailedException("IOException caught. Most likely cause: keys for signature algorithms"
						+ " could not be loaded.\n", e);
			}
			this.tseIsInitialized = false;
			
			//check if the simulator user wants the default descriptionOfTheSeapi or if they want to set one themselves
			try {
				if(PropertyValues.getInstance().getValue(ConfigConstants.CFG_TAG_DESCRIPTION_SET_BY_MANUFACTURER).equalsIgnoreCase(ConfigConstants.CFG_ENTRY_DESCRIPTION_SET_BY_MANUFACTURER_TRUE)) {
					this.descriptionOfTheSEAPI = Constants.DEFAULT_DESCRIPTION_OF_SEAPI;
				}
			} catch (IOException e) {
				throw new LoadingFailedException("Failed to load from PropertyValues. Most likely cause: no path to resources set. Original message:\n" +e.getMessage(), e);
			}
		}
		
		//set the User to "null"
		this.currentlyLoggedIn = null;
	}
	
	
	/**
	 * Constructor for the TSEController. Has to be given the path to the main configuration file, config.properties.
	 * 
	 * The TSEController constructs a SecurityModule (SecurityModule) and a Storage.
	 * If the construction of the security module fails due to an IOException, this constructor throws a LoadingFailedException.
	 * 
	 * <br>Depending on whether persisted values are found under the directory path specified in config.properties this method behaves differently.
	 * If {@linkplain PersistentStorage#persistentValuesExist()} returns true, the method attempts to read the values from the file and 
	 * uses them to construct the {@linkplain SecurityModule} and therefore the {@linkplain ERSSpecificModule} and {@linkplain CryptoCore}. This means that
	 * the clock and the signature counter of the CryptoCore are set to the stored values, as is the transaction counter in the ERSSpecificModule and the 
	 * stati of the TSE and the SecurityModule.
	 * <br>If no persistence file is found, everything is constructed with its default values. That means, "0" for the transaction number and the
	 * signature counter, "false" for seIsDisabled and "false" for tseIsInitialized. Depending on the configuration, this may also set the 
	 * "description of the SE API".
	 * @param pathToConfigFile the (absolute) path pointing to the resource directory
	 * 
	 * 
	 * path pointing to config.properties.
	 * @throws NoExternalConfigFilesException if the path to the config.properties file is null,
	 * @throws LoadingFailedException if during the construction of the {@linkplain SecurityModule} an {@linkplain IOException} is thrown.
	 * 
	 * 
	 * if the constructor of the {@linkplain SecurityModule}, {@linkplain Storage} or {@linkplain PersistentStorage} throws an IOException,
	 * and/or if reading the configuration file fails due to a missing path to the resource directory. <br> Mitigation: Use {@linkplain PropertyValues#setPathToResourceDirectory(String)} before 
	 * constructing the TSEController.
	 */
	public TSEController(String pathToResourceDir) throws NoExternalConfigFilesException, LoadingFailedException {
		//check if the path to the resource directory is not null, throw an Exception if it is
		if(pathToResourceDir == null) {
			throw new NoExternalConfigFilesException("Path to resource directory was null. Please provide a valid path to the resource directory!");
		}

		//check if the path to resource dir does not end with a File separator character. If it does, remove it (and hope there's only one of them)
		if(pathToResourceDir.endsWith(File.separator)) {
			pathToResourceDir = pathToResourceDir.substring(0, pathToResourceDir.length()-1);
		}
		
		//check set the path to the resource directory through PorpertyValues. If the path does not exist, there will be an IOException
		PropertyValues.setPathToResourceDirectory(pathToResourceDir);
		//try to load an instance of the class, to check if it really works:
		PropertyValues pvinstance = null;
		try {
			pvinstance = PropertyValues.getInstance();
		} catch (IOException e1) {
			throw new NoExternalConfigFilesException("No file named \"config.properties\" was found in the resource directory.\n" +e1.getMessage(), e1);
		}
		if(pvinstance==null) {
			throw new NoExternalConfigFilesException("No file named \"config.properties\" was found in the resource directory.\n");
		}
		
		//There is no check to ensure the config.properties file is formatted according to what is expected.
		//The simulator assumes, the user is smart enough to configure the file according to the simulator handbook.

		//after the path is checked and stored, the building of the TSE can start:
		
		//create the storages. they are independent (more or less) from the saved values.
		this.storage = new Storage();
		this.persistentStorage = new PersistentStorage();
		
		//check if a persistence file exists that can be de-serialized
		if(this.persistentStorage.persistentValuesExist()) {
			//get the values that can be loaded in
			PersistedValues loaded = this.persistentStorage.loadFromPersistedFile();
			
			//set the initialization status to what has been loaded
			this.tseIsInitialized = loaded.getTseIsInitialized();
			//create the security module with all loaded parameters
			try {
				this.securityModule = new SecurityModule(loaded.getSeIsDisabled(), loaded.getSignatureCounterStatus(), loaded.getTransactionNumberStatus(), 
						loaded.getCryptoCoreClockStatus());
			} catch (IOException e) {
				throw new LoadingFailedException("IOException caught. Most likely cause: keys for signature algorithms"
						+ " could not be loaded.\n", e);
			}
			this.descriptionOfTheSEAPI = loaded.getDescriptionOfTheSEAPI();
		}
		
		//if there are no persisted values, fill everything with defaults
		else {
			try {
				this.securityModule = new SecurityModule();
			} catch (IOException e) {
				throw new LoadingFailedException("IOException caught. Most likely cause: keys for signature algorithms"
						+ " could not be loaded.\n", e);
			}
			this.tseIsInitialized = false;
			
			//check if the simulator user wants the default descriptionOfTheSeapi or if they want to set one themselves
			if(pvinstance.getValue(ConfigConstants.CFG_TAG_DESCRIPTION_SET_BY_MANUFACTURER).equalsIgnoreCase(ConfigConstants.CFG_ENTRY_DESCRIPTION_SET_BY_MANUFACTURER_TRUE)) {
				this.descriptionOfTheSEAPI = Constants.DEFAULT_DESCRIPTION_OF_SEAPI;
			}
		}
		
		//set the User to "null"
		this.currentlyLoggedIn = null;
	}
	

//--------------------------------------------------METHODS NOT SPECIFIED BY THE SEAPI--------------------------------------------------------
	/**
	 * Used to register clients for the usage of the TSE. Only registered clients may use the TSE to document transactions. 
	 * A logged in user with the role of "admin" is required to invoke the function successfully. <br>
	 * Please note: keeping track of which clientIds are logged in and which are not is not performed by the TSE simulator. 
	 * @param clientId the ID of the client to be added [INPUT PARAMETER, REQUIRED]
	 * @return if the execution of the function has been successful, the return value {@linkplain Constant#EXECUTION_OK} SHALL be returned
	 * @throws ErrorSecureElementDisabled if the secure element of the TSE has been disabled
	 * @throws ErrorSeApiNotInitialized if the SeApi has not yet been initialized
	 * @throws ErrorParameterMismatch if the clientId is absent or not an ASN.1 PrintableString
	 * @throws ErrorUserNotAuthenticated if no user is logged in
	 * @throws ErrorUserNotAuthorized if a user is logged in but does not have the role of "admin"
	 * @throws ClientIdAlreadyRegisteredException if attempting to register a client that has already been registered before
	 * @throws TooManyClientsException if adding this client would result in a violation of maxNumberOfClients in config.properties and/or {@linkplain Constants#MAX_NUMBER_OF_CLIENTS}
	 */
	public short registerClient(String clientId) throws ErrorSecureElementDisabled, ErrorSeApiNotInitialized, ErrorParameterMismatch, ErrorUserNotAuthenticated, 
								ErrorUserNotAuthorized, ClientIdAlreadyRegisteredException, TooManyClientsException {
		//if the secure element is disabled throw an exception
		if(this.securityModule.getSecureElementIsDisabled()) {
			throw new ErrorSecureElementDisabled();
		}
		//if the TSE has not been initialized throw an exception
		if(!tseIsInitialized) {
			throw new ErrorSeApiNotInitialized();
		}
		//if the clientId is in an illegal format or not present at all throw exception
		if(!TLVUtility.isASN1_PrintableString(clientId)) {
			throw new ErrorParameterMismatch();
		}
		//if there is no user logged in throw an exception
		if((currentlyLoggedIn == null)) {
			throw new ErrorUserNotAuthenticated();
		}
		//if a user is logged in, but their role is not admin
		if(!currentlyLoggedIn.getRole().equalsIgnoreCase("admin")) {
			throw new ErrorUserNotAuthorized();
		}
		//proceed adding the client, propagate any exception arising from that
		this.securityModule.registerClient(clientId);
		//return constant execution ok if everything went okay up to this point
		return Constant.EXECUTION_OK;
	}
	
	/**
	 * Used to unregister clients from the TSE. Unregistered clients can no longer use the TSE to document their transactions. Clients may be re-registered using 
	 * {@linkplain TSEController#registerClient(String)}.<br>
	 * Please note: keeping track of which clientIds are logged in and which are not is not performed by the TSE simulator. 
	 * Therefore, the caller of this function might remove clients which still have open transactions. It is the responsibility of the caller to make sure, those transactions 
	 * are terminated properly by using another client or re-registering the client that started them. 
	 * @param clientId the ID of the client to be removed [INPUT PARAMETER, REQUIRED]
	 * @return if the execution of the function has been successful, the return value {@linkplain Constant#EXECUTION_OK} SHALL be returned
	 * @throws ErrorSecureElementDisabled if the secure element of the TSE has been disabled
	 * @throws ErrorSeApiNotInitialized if the SeApi has not yet been initialized
	 * @throws ErrorParameterMismatch if the clientId is absent or not an ASN.1 PrintableString
	 * @throws ErrorUserNotAuthenticated if no user is logged in
	 * @throws ErrorUserNotAuthorized if a user is logged in but does not have the role of "admin"
	 * @throws ClientIdNotRegisteredException if attempting to remove a client that is currently not registered
	 */
	public short deregisterClient(String clientId) throws ErrorSecureElementDisabled, ErrorSeApiNotInitialized, ErrorParameterMismatch, ErrorUserNotAuthenticated,
								  ErrorUserNotAuthorized, ClientIdNotRegisteredException {
		//note: client A may start a transaction, be unregistered after and then client B finishes that transaction
		//if the secure element is disabled throw an exception
		if(this.securityModule.getSecureElementIsDisabled()) {
			throw new ErrorSecureElementDisabled();
		}
		//if the TSE has not been initialized throw an exception
		if(!tseIsInitialized) {
			throw new ErrorSeApiNotInitialized();
		}
		//if the clientId is in an illegal format or not present at all throw exception
		if(!TLVUtility.isASN1_PrintableString(clientId)) {
			throw new ErrorParameterMismatch();
		}
		//if there is no user logged in throw an exception
		if((currentlyLoggedIn == null)) {
			throw new ErrorUserNotAuthenticated();
		}
		//if a user is logged in, but their role is not admin
		if(!currentlyLoggedIn.getRole().equalsIgnoreCase("admin")) {
			throw new ErrorUserNotAuthorized();
		}
		//proceed removing the client, propagate any exception arising from that
		securityModule.deregisterClient(clientId);
		//return constant execution ok if everything went okay up to this point
		return Constant.EXECUTION_OK;
	}
	

	/**
	 * Performs a graceful shutdown for the TSEController. This is useful, because one can not simply turn off the program in which the TSEController 
	 * exists, rewrite some tests, and then use the TSEController (and therefore the simulator) as if nothing had happened. 
	 * This method aims to help with that problem:<br>
	 * It closes all remaining open transactions with a generic message indicating they were closed by a graceful shutdown,<br>
	 * logs out any user that is still logged in,<br>
	 * de-registers any clients still registered<br>
	 * and persists all relevant data.
	 * 
	 * Please note: using the simulator would now look like this:
	 * TSEController currentlyUsedController = new TSEController();
	 * TEST
	 * currentlyUsedController.gracefulShutdown()
	 * currentlyUsedController = null;
	 * currentlyUsedController = new TSEController(); //this loads the persisted values!
	 *
	 * <br>
	 * depending on number of open transactions, this may take a while!
	 * @return {@linkplain Constant#EXECUTION_OK} if the execution went okay
	 * @throws GracefulShutdownFailedException 
	 * @throws PersistingFailedException if storing the important values of the simulator in {@linkplain PersistentStorage} fails
	 */
	public synchronized short gracefulShutdown() throws GracefulShutdownFailedException, PersistingFailedException {
		//1. call SecurityModule to de-register all the remaining clients from the TSE
		//		Necessary, because in the case of numberOfClientsRegistered == maxNumberOfClients, logging another client in to close all remaining transactions would cause problems
		this.securityModule.deregisterAllClients();
		
		//2. get the number of open transactions from the ERSSpecificModule to determine if any of those should be closed. 
		int remainingOpenTransactions = this.securityModule.getNumberOfERSSMopenTransactions();
		//3. if there are still open transactions, close those with some kind of dummy data
		if(remainingOpenTransactions > 0) {
			//3.A: try to register the client responsible for closing the transactions
			try {
				this.securityModule.registerClient(Constants.GRACEFUL_SHUTDOWN_CLIENTID);
			} catch (ClientIdAlreadyRegisteredException e) {
			} catch (TooManyClientsException e) {
				throw new GracefulShutdownFailedException(e.getMessage(), e);
			}
			//3.B: get a set of transaction numbers of the still open transactions
			Set<Long> transactionNumbers = this.securityModule.getERSSMOpenTransactionNumbers();
			//3.C: clone that set, because otherwise, a ConcurrentModificationException will be thrown
			List<Long> transactionNumbersClone = new ArrayList<Long>(transactionNumbers);
			//3D: Iterate over the transaction numbers and finish each one
			Iterator<Long> iter = transactionNumbersClone.iterator();
			while(iter.hasNext()) {
				Long currentNumber = iter.next();
				byte[] resultOfFinishTransaction = null;
				try {
					resultOfFinishTransaction = this.securityModule.finishTransaction(Constants.GRACEFUL_SHUTDOWN_CLIENTID,currentNumber.longValue(),
							Constants.GRACEFUL_SHUTDOWN_PROCESSDATA, Constants.GRACEFUL_SHUTDOWN_PROCESSTYPE, null);
					
					//try to construct a TransactionLogMessage from the resultOfStartTransaction byte array
					TransactionLogMessage resultingTransactionLog = (TransactionLogMessage) createCompleteLogMessageFromByteArray(resultOfFinishTransaction, (short) 1);
					//store the log in the storage
					this.storage.storeTransactionLog(resultOfFinishTransaction, resultingTransactionLog.getLogTime(), resultingTransactionLog.getSignatureCounter(),
							resultingTransactionLog.getTransactionNumber(), resultingTransactionLog.getOperationType(), resultingTransactionLog.getClientID());
				} catch (ErrorNoTransaction | ValueNullException | ValueTooBigException
						| SigningOperationFailedException | ErrorSignatureCounterOverflow
						| ErrorFinishTransactionFailed | ErrorRetrieveLogMessageFailed | ErrorStorageFailure e) {
					throw new GracefulShutdownFailedException("Failed closing transaction number " +currentNumber.longValue() +".\n" +e.getMessage(), e);
				}
			}
			//3.C: try to de-register the client responsible for closing the transactions
			try {
				this.securityModule.deregisterClient(Constants.GRACEFUL_SHUTDOWN_CLIENTID);
			} catch (ClientIdNotRegisteredException e) {}
		}
		
		//4. logOut a user, if there is still one logged in
		if(currentlyLoggedIn != null) {
			try {
				logOut(currentlyLoggedIn.getUserID());
			} catch (ErrorUserIdNotManaged | ErrorSigningSystemOperationDataFailed | ErrorUserIdNotAuthenticated
					| ErrorRetrieveLogMessageFailed | ErrorStorageFailure | ErrorSecureElementDisabled
					| ErrorSignatureCounterOverflow e) {
				throw new GracefulShutdownFailedException("LogOut of user failed\n"+e.getMessage(), e);
			}
		}
		//5. get the values that are to be persisted
		//get the current signature counter from the CryptoCore
		long sigCntrToBePersisted;
		try {
			sigCntrToBePersisted = this.securityModule.getCryptoCore().getSignatureCounter();
		} catch (ErrorSignatureCounterOverflow e1) {
			throw new GracefulShutdownFailedException("Retrieval of CryptoCore signature counter failed!\n"+e1.getMessage(), e1);
		}	
		//get the transaction number from the ERSSM
		long transactionNumberToBePersisted = this.securityModule.getCurrentTransactionCounter();
		
		//6. try to persist the values
		this.persistentStorage.storeLatestValues(this.securityModule.getCurrentTimeFromCryptoCore(), tseIsInitialized, securityModule.getSecureElementIsDisabled(),
			sigCntrToBePersisted, transactionNumberToBePersisted, descriptionOfTheSEAPI);			

		//set all values of this simulator to "null" or their default value
		//This is done, because a turned off TSE would not be able to function as well
		this.securityModule = null;
		this.persistentStorage = null;
		this.tseIsInitialized = false;
		this.descriptionOfTheSEAPI = null;
		this.manufacturerInformation = null;
		this.versionInformation = null;
		this.currentlyLoggedIn = null;
		
		//as usual: if it worked, return execution ok
		return Constant.EXECUTION_OK;
	}
	
	
	/**
	 * Used to add a {@linkplain User} to the simulator. <br>
	 * <b>THIS SHOULD ONLY BE USED FOR TESTING PURPOSES! NOT INCLUDED IN THE SE API!</b>
	 * @return {@linkplain Constant#EXECUTION_OK} if everything went okay
	 * @throws UserAlreadyExistsException if attempting to add a user that was already present
	 * @throws AddingUserFailedException if something happened preventing the user data from being stored successfully 
	 * @throws IllegalArgumentException if the userId was too long or the role was an illegal one
	 * @throws TooManyUsersException if adding a user would result in more users than specified in {@linkplain Constants#MAX_STORED_USERS}
	 * @see {@linkplain PersistentStorage#addUser(User)}
	 */
	public short addUserToSimulator(String UserId, String role, byte[] pin, byte[] puk) throws TooManyUsersException, IllegalArgumentException, AddingUserFailedException, UserAlreadyExistsException {		
		//hash pin and puk
		byte[] hashedPin = this.securityModule.getCryptoCore().hashByteArray(pin);
		byte[] hashedPuk = this.securityModule.getCryptoCore().hashByteArray(puk);
		
		//set the PINRetry and PUKRetry values to the default value in Constants
		User newUser = new User(UserId, role, hashedPin, hashedPuk, Constants.MAX_RETRIES, Constants.MAX_RETRIES);
		this.persistentStorage.addUser(newUser);
		
		//if everything went okay, return that
		return Constant.EXECUTION_OK;
	}
	
	/**
	 * Used to remove a {@linkplain User} from the simulator. Does not check whether any users would remain after removing one. <br>
	 * <b>THIS SHOULD ONLY BE USED FOR TESTING PURPOSES! NOT INCLUDED IN THE SE API!</b>
	 * @param userId the ID of the user that shall be removed
	 * @return {@linkplain Constant#EXECUTION_OK} if everything went okay
	 * @throws RemovingUserFailedException if something happens preventing the user from being removed, for example an IOError
	 */
	public short removeUserFromSimulator(String userId) throws RemovingUserFailedException {	
		this.persistentStorage.removeUser(userId);
		
		//if everything went okay, return that
		return Constant.EXECUTION_OK;
	}
	
	/**
	 * Attempts to remove every user currently stored for this simulator. <br>
	 * <b>THIS SHOULD ONLY BE USED FOR TESTING PURPOSES! NOT INCLUDED IN THE SE API!</b>
	 * @return {@linkplain Constant#EXECUTION_OK}
	 * @throws RemovingUserFailedException if invoking the removal operation on persistent storage results in an exception <br> 
	 * or if {@linkplain UserlistValues#getInstance()} throws it.
	 */
	public short removeAllUsersFromSimulator() throws RemovingUserFailedException {	
		//get all user names present
		Set<String> allUsernames = null;
		try {
			allUsernames = UserlistValues.getInstance().getAllPropertyNames();
		} catch (IOException e) {
			throw new RemovingUserFailedException("Failed to .getInstance() in class UserlistValues. Original message:\n"+e.getMessage(), e);
		}
		//iterate over all user names and call PersistentStorage.removeUser on each of them
		for(String currentUserToBeDeleted : allUsernames) {
			persistentStorage.removeUser(currentUserToBeDeleted);
		}
		
		//if everything went okay, return that
		return Constant.EXECUTION_OK;
	}
	
//-----------------------------------Refactored Methods-----------------------------------------------------------
	/**
	 * Used internally to make this program more readable. Is reused nearly every time a LogMessage has to be created. 
	 * logType = <br>
	 * <ul>
	 * <li>1 : TransactionLog </li>
	 * <li>2 : SystemLog </li>
	 * <li>3 : AuditLog (not yet implemented) </li>
	 * </ul>
	 * @param resultingLog the byte array representation of a {@linkplain LogMessage} created via a call of the appropriate {@linkplain SecurityModule} function
	 * @param logType a number indicating the type of log message that is contained in resultingLog. See above for more information.
	 * @return either a {@linkplain TransactionLogMessage} or a {@linkplain SystemLogMessage}. Null, if no valid case was selected.
	 * @throws ErrorRetrieveLogMessageFailed if an error occurs while turning the byte array into a proper LogMessage
	 */
	private LogMessage createCompleteLogMessageFromByteArray(byte[] resultingLog, short logType) throws ErrorRetrieveLogMessageFailed {
		//case 1: transaction log
		if(logType == 1) {
			//try to construct a TransactionLogMessage from the resultingLog byte array
			TransactionLogMessage resultingTransactionLog = null;
			try {
				TLVObject[] resultOfTransactionWITHSequenceWrapper = TLVObject.decodeASN1ByteArrayToTLVObjectArray(resultingLog);
				//the value of the TLVObject[] without the first entry should be the "real" logmessage
				TLVObject[] resultOfTransactionWITHOUTSequenceWrapper = new TLVObject[resultOfTransactionWITHSequenceWrapper.length-1];
				System.arraycopy(resultOfTransactionWITHSequenceWrapper, 1, resultOfTransactionWITHOUTSequenceWrapper, 0, resultOfTransactionWITHOUTSequenceWrapper.length);
				resultingTransactionLog = new TransactionLogMessage(resultOfTransactionWITHOUTSequenceWrapper);
			} catch (Exception e) {
				throw new ErrorRetrieveLogMessageFailed(e.getMessage(), e);
			}
			return resultingTransactionLog;
		}
		//case 2: system log
		if(logType == 2) {
			//try to construct a SystemLogMessage from the resultingLog byte array
			SystemLogMessage resultingSysLog = null;
			try {
				TLVObject[] resultOfSysOpWITHSequenceWrapper = TLVObject.decodeASN1ByteArrayToTLVObjectArray(resultingLog);
				//the value of the TLVObject[] without the first entry should be the part of the Syslog we want (the first entry is just the SEQUENCE wrapper)
				TLVObject[] resultOfSysOpWITHOUTSequenceWrapper = new TLVObject[resultOfSysOpWITHSequenceWrapper.length-1];
				System.arraycopy(resultOfSysOpWITHSequenceWrapper, 1, resultOfSysOpWITHOUTSequenceWrapper, 0, resultOfSysOpWITHOUTSequenceWrapper.length);
				resultingSysLog = new SystemLogMessage(resultOfSysOpWITHOUTSequenceWrapper);
			} catch (Exception e) {
				throw new ErrorRetrieveLogMessageFailed(e.getMessage(), e);
			} 
			return resultingSysLog;
		}
		//case 3: audit log (NOT YET IMPLEMENTED!)
		if(logType == 3) {
			return null;
		}
		//in case something goes wrong: return null
		return null;
	}
	
//--------------------------------------------------MAINTENANCE FUNCTIONS---------------------------------------------------------------------	
	/* (non-Javadoc)
	 * @see main.java.de.bsi.seapi.SEAPI#initialize(java.lang.String)
	 */
	@Override
	public short initialize(String description)
			throws ErrorSigningSystemOperationDataFailed, ErrorStoringInitDataFailed, ErrorRetrieveLogMessageFailed,
			ErrorStorageFailure, ErrorCertificateExpired, ErrorSecureElementDisabled, ErrorUserNotAuthorized,
			ErrorUserNotAuthenticated, ErrorDescriptionSetByManufacturer, ErrorSignatureCounterOverflow {
		//if SecureElement is disabled, the Exception ErrorSecureElementDisabled has to be thrown
		if(this.securityModule.getSecureElementIsDisabled()) {
			throw new ErrorSecureElementDisabled();
		}

		//if the user that invokes this functionality is not authenticated, ErrorUserNotAuthenticated has to be thrown in accordance with BSI TR-03151
		if(currentlyLoggedIn==null) {
			throw new ErrorUserNotAuthenticated();
		}
		//if the user is authenticated but not authorized, ErrorUserNotAuthorized has to be thrown in accordance with BSI TR-03151
		if(!(currentlyLoggedIn.getRole().equalsIgnoreCase("Admin"))) {
			throw new ErrorUserNotAuthorized();
		}
		
		//if the TSE has already be initialized, do not allow another initialize function call
			//since in neither BSI TR-03151 Version 1.0.1 nor the Amendment to that TR that was published on 2nd of December 2019 a separate Exception was 
			//defined for that case, recycle the ErrorStoringInitDataFailed exception
		if(tseIsInitialized) {
			throw new ErrorStoringInitDataFailed("TSE already initialized!\n");
		}	
		
		//if the description has been set by the manufacturer, ErrorDescriptionSetByManufacturer shall be thrown in accordance with BSI TR-03151
		if(this.descriptionOfTheSEAPI != null) {
			throw new ErrorDescriptionSetByManufacturer();
			//this "if not null, throw Exception" prevents multiple calls of initialize in its version WITH a parameter.
			//According to TR-03151, chapter 4.3.1.4, the function shall exit if the description has been set.
		}
		
		//format-check the description if it is an ASN1 PrintableString or not
		if((description == null) || !(TLVUtility.isASN1_PrintableString(description))) {
			throw new ErrorStoringInitDataFailed("The description provided is not an ASN1 PrintableString!\n");
		}
		
	//1. call the SecureElement functionality to create the SystemLogMessage
		byte[] resultOfInitialize = null;
		try {
			resultOfInitialize = this.securityModule.initialize(description);
		} catch (SigningOperationFailedException e) {
			throw new ErrorSigningSystemOperationDataFailed("Signing operation in the CryptoCore failed.", e);
		} catch (ValueNullException e) {
			throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value null", e);
		} catch (ValueTooBigException e) {
			throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value too big", e);
		}
		
	//2. function shall retrieve the parts of the log message from the secure element
		if(resultOfInitialize == null) {
			throw new ErrorRetrieveLogMessageFailed();
		}
		SystemLogMessage resultingSysLog = (SystemLogMessage) createCompleteLogMessageFromByteArray(resultOfInitialize, (short) 2);
	
	//3. Store the syslog. if that fails, ErrorStoragefailure shall be raised
		try {
			this.storage.storeSystemLog(resultOfInitialize, resultingSysLog.getLogTime(), resultingSysLog.getSignatureCounter(), resultingSysLog.getOperationType());
		} catch (Exception e) {
			throw new ErrorStorageFailure(e.getMessage(), e);
		}
		
	//4. INITIALIZE! SET TSE IS INITIALIZED TO TRUE!!! SET THE DESCRIPTION TO THE PROVIDED ONE!
		this.tseIsInitialized = true;
		this.descriptionOfTheSEAPI = description;
		
	//5. check if the certificate of the TSE is expired. If the certificate is expired, this function MAY throw an exception because at this point, the
		//TSE has been initialized and a SystemLog has been created to log this.
		if(securityModule.getCryptoCore().isCertificateExpired()) {
			throw new ErrorCertificateExpired();
		}
			
		
	//6. if everything was successful, return EXECUTION_OK
		return Constant.EXECUTION_OK;
	}

	/* (non-Javadoc)
	 * @see main.java.de.bsi.seapi.SEAPI#initialize()
	 */
	@Override
	public short initialize() throws ErrorSigningSystemOperationDataFailed, ErrorRetrieveLogMessageFailed,
			ErrorStorageFailure, ErrorCertificateExpired, ErrorSecureElementDisabled, ErrorUserNotAuthorized,
			ErrorUserNotAuthenticated, ErrorDescriptionNotSetByManufacturer, ErrorSignatureCounterOverflow {
		//ErrorCertificateExpired MAY be thrown, not SHALL be thrown

		//if SecureElement is disabled, the Exception ErrorSecureElementDisabled has to be thrown
		if(this.securityModule.getSecureElementIsDisabled()) {
			throw new ErrorSecureElementDisabled();
		}

		//if the user that invokes this functionality is not authenticated, ErrorUserNotAuthenticated has to be thrown in accordance with BSI TR-03151
		if(currentlyLoggedIn==null) {
			throw new ErrorUserNotAuthenticated();
		}
		//if the user is authenticated but not authorized, ErrorUserNotAuthorized has to be thrown in accordance with BSI TR-03151
		if(!(currentlyLoggedIn.getRole().equalsIgnoreCase("Admin"))) {
			throw new ErrorUserNotAuthorized();
		}
		
		//if the TSE has already be initialized, do not allow another initialize function call
			//since in neither BSI TR-03151 Version 1.0.1 nor the Amendment to that TR that was published on 2nd of December 2019 a separate Exception was 
			//defined for that case, recycle the ErrorSigningSystemOperationDataFailed exception
		if(tseIsInitialized) {
			throw new ErrorSigningSystemOperationDataFailed("TSE already initialized!\n");
		}	
		
		//if the description has not been set by the manufacturer, ErrorDescriptionNotSetByManufacturer shall be thrown in accordance with BSI TR-03151
		if(this.descriptionOfTheSEAPI == null) {
			throw new ErrorDescriptionNotSetByManufacturer();
		}
		
		//1. call the SecureElement functionality to create the SystemLogMessage
			byte[] resultOfInitialize = null;
			try {
				resultOfInitialize = this.securityModule.initialize(null);
			} catch (SigningOperationFailedException e) {
				throw new ErrorSigningSystemOperationDataFailed("Signing operation in the CryptoCore failed.", e);
			} catch (ValueNullException e) {
				throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value null", e);
			} catch (ValueTooBigException e) {
				throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value too big", e);
			}
			
		//2. function shall retrieve the parts of the log message from the secure element
			if(resultOfInitialize == null) {
				throw new ErrorRetrieveLogMessageFailed();
			}
			SystemLogMessage resultingSysLog = (SystemLogMessage) createCompleteLogMessageFromByteArray(resultOfInitialize, (short) 2);
		
		//3. store the syslog. if that fails, ErrorStoragefailure shall be raised
			try {
				this.storage.storeSystemLog(resultOfInitialize, resultingSysLog.getLogTime(), resultingSysLog.getSignatureCounter(), resultingSysLog.getOperationType());
			} catch (Exception e) {
				throw new ErrorStorageFailure(e.getMessage(), e);
			}
		//4. INITIALIZE! SET TSE IS INITIALIZED TO TRUE!!!
			this.tseIsInitialized = true;
			
		//5. check if the certificate of the TSE is expired. If the certificate is expired, this function MAY throw an exception because at this point, the
			//TSE has been initialized and a SystemLog has been created to log this
			if(securityModule.getCryptoCore().isCertificateExpired()) {
				throw new ErrorCertificateExpired();
			}
			
		//6. if everything was successful, return EXECUTION_OK
			return Constant.EXECUTION_OK;
	}

	
	/* (non-Javadoc)
	 * @see main.java.de.bsi.seapi.SEAPI#updateTime(java.time.ZonedDateTime)
	 */
	@Override
	public short updateTime(ZonedDateTime newDateTime)
			throws ErrorUpdateTimeFailed, ErrorRetrieveLogMessageFailed, ErrorStorageFailure, ErrorSeApiNotInitialized,
			ErrorCertificateExpired, ErrorSecureElementDisabled, ErrorUserNotAuthorized, ErrorUserNotAuthenticated, 
			ErrorInvalidTime, ErrorSignatureCounterOverflow {
		//if the secure element is disabled, ErrorSecureElementDisabled has to be thrown
		if(this.securityModule.getSecureElementIsDisabled()) {
			throw new ErrorSecureElementDisabled();
		}
		
		//if the TSE has not been initialized, ErrorSeApiNotInitialized has to be thrown 
		if(!this.tseIsInitialized) {
			throw new ErrorSeApiNotInitialized();
		}

		//if the user that invokes this functionality is not authenticated, ErrorUserNotAuthenticated has to be thrown in accordance with BSI TR-03151
		if(currentlyLoggedIn==null) {
			throw new ErrorUserNotAuthenticated();
		}
		//if the user is authenticated but not authorized, ErrorUserNotAuthorized has to be thrown in accordance with BSI TR-03151
		if(!((currentlyLoggedIn.getRole().equalsIgnoreCase("Admin")) || (currentlyLoggedIn.getRole().equalsIgnoreCase("TimeAdmin")))) {
			throw new ErrorUserNotAuthorized();
		}
		
		//check if the input is null
		if(newDateTime == null) {
			throw new ErrorInvalidTime();
		}
		//if the input is an otherwise illegal value, the CryptoCore will throw an ErrorUpdateTimeFailed and it will be propagated up
		
	//1. SHALL use the feature of the SecureElement to set the time.
		byte[] resultOfUpdateTime = null;
		try {
			resultOfUpdateTime = this.securityModule.updateTime(newDateTime, true);
		} catch (ValueNullException e) {
			throw new ErrorUpdateTimeFailed("ValueNullException caught.\n" +e.getMessage() +"\n");
		} catch (ValueTooBigException e) {
			throw new ErrorUpdateTimeFailed("ValueTooBigException caught.\n" +e.getMessage() +"\n");
		} catch (SigningOperationFailedException e) {
			throw new ErrorRetrieveLogMessageFailed("SigningOperationFailedException caught.\n" +e.getMessage());
		}
		
	//2. Next, the function SHALL retrieve the parts of the logmessage determined by the secure element.
		//if the execution of this functionality fails, an ErrorRetrieveLogMessageFailed SHALL be raised.
		//if the result of the update time method is null, the logmessage could not be retrieved correctly
		if(resultOfUpdateTime == null) {
			throw new ErrorRetrieveLogMessageFailed();
		}
		SystemLogMessage resultingSysLog = (SystemLogMessage) createCompleteLogMessageFromByteArray(resultOfUpdateTime, (short) 2);
		
	//3. store the data of the SysLog. If that fails, ErrorStoragefailure SHALL be raised
		try {
			this.storage.storeSystemLog(resultOfUpdateTime, resultingSysLog.getLogTime(), resultingSysLog.getSignatureCounter(), resultingSysLog.getOperationType());
		} catch (Exception e) {
			throw new ErrorStorageFailure(e.getMessage());
		}
		
	//4. check if the certificate of the TSE is expired. If the certificate is expired, this function SHALL throw an exception because at this point, the
		//TSE has been initialized and a SystemLog has been created to log this.
		if(securityModule.getCryptoCore().isCertificateExpired()) {
			throw new ErrorCertificateExpired();
		}

	//5. if execution was successful, return Execution Ok
		return Constant.EXECUTION_OK;
	}

	/* (non-Javadoc)
	 * @see main.java.de.bsi.seapi.SEAPI#updateTime()
	 */
	@Override
	public short updateTime()
			throws ErrorUpdateTimeFailed, ErrorRetrieveLogMessageFailed, ErrorStorageFailure, ErrorSeApiNotInitialized,
			ErrorCertificateExpired, ErrorSecureElementDisabled, ErrorUserNotAuthorized, ErrorUserNotAuthenticated, 
			ErrorSignatureCounterOverflow {
		//if the secure element is disabled, ErrorSecureElementDisabled has to be thrown
		if(this.securityModule.getSecureElementIsDisabled()) {
			throw new ErrorSecureElementDisabled();
		}
		
		//if the TSE has not been initialized, ErrorSeApiNotInitialized has to be thrown
		if(!tseIsInitialized) {
			throw new ErrorSeApiNotInitialized();
		}
		
		//if the user that invokes this functionality is not authenticated, ErrorUserNotAuthenticated has to be thrown in accordance with BSI TR-03151
		if(currentlyLoggedIn==null) {
			throw new ErrorUserNotAuthenticated();
		}
		//if the user is authenticated but not authorized, ErrorUserNotAuthorized has to be thrown in accordance with BSI TR-03151
		if(!((currentlyLoggedIn.getRole().equalsIgnoreCase("Admin")) || (currentlyLoggedIn.getRole().equalsIgnoreCase("TimeAdmin")))) {
			throw new ErrorUserNotAuthorized();
		}
		
	//1. SHALL instruct the SecureElement to use its time synchronization feature. If that fails, the ErrorUpdateTimeFailed is raised
		byte[] resultOfUpdateTime = null;
		try {
			resultOfUpdateTime = this.securityModule.updateTime(null, false);
		} catch (ValueNullException e) {
			throw new ErrorUpdateTimeFailed("ValueNullException caught.\n" +e.getMessage() +"\n");
		} catch (ValueTooBigException e) {
			throw new ErrorUpdateTimeFailed("ValueTooBigException caught.\n" +e.getMessage() +"\n");
		} catch (SigningOperationFailedException e) {
			throw new ErrorRetrieveLogMessageFailed("SigningOperationFailedException caught.\n" +e.getMessage());
		} catch (ErrorInvalidTime e) {
			throw new ErrorUpdateTimeFailed("ErrorInvalid time caught.\n Should not have been thrown when using this function.\n" +e.getMessage());
		}
		
	//2. Next, the function SHALL retrieve the parts of the logmessage determined by the secure element.
		//if the execution of this functionality fails, an ErrorRetrieveLogMessageFailed SHALL be raised.
		//if the result of the update time method is null, the logmessage could not be retrieved correctly
		if(resultOfUpdateTime == null) {
			throw new ErrorRetrieveLogMessageFailed();
		}
		SystemLogMessage resultingSysLog = (SystemLogMessage) createCompleteLogMessageFromByteArray(resultOfUpdateTime, (short) 2);
		
	//3. store the data of the SysLog. If that fails, ErrorStoragefailure SHALL be raised
		try {
			this.storage.storeSystemLog(resultOfUpdateTime, resultingSysLog.getLogTime(), resultingSysLog.getSignatureCounter(), resultingSysLog.getOperationType());
		} catch (Exception e) {
			throw new ErrorStorageFailure(e.getMessage());
		}
		
	//4. check if the certificate of the TSE is expired. If the certificate is expired, this function SHALL throw an exception because at this point, the
		//TSE has been initialized and a SystemLog has been created to log this.
		if(securityModule.getCryptoCore().isCertificateExpired()) {
			throw new ErrorCertificateExpired();
		}
			
	//5. if execution was successful, return Execution Ok
		return Constant.EXECUTION_OK;
	}

	/* (non-Javadoc)
	 * @see main.java.de.bsi.seapi.SEAPI#disableSecureElement()
	 */
	@Override
	public short disableSecureElement()
			throws ErrorDisableSecureElementFailed, ErrorTimeNotSet, ErrorRetrieveLogMessageFailed, ErrorStorageFailure,
			ErrorCertificateExpired, ErrorSecureElementDisabled, ErrorUserNotAuthorized, ErrorUserNotAuthenticated, 
			ErrorSignatureCounterOverflow, ErrorSeApiNotInitialized {
		//if the secure element is already disabled, it can not be disabled a second time. ErrorSecureElementDisabled has to be thrown
		if(this.securityModule.getSecureElementIsDisabled()) {
			throw new ErrorSecureElementDisabled();
		}
		
		//if the TSE has not been initialized, throw ErrorSeApiNotInitialized
		if(!tseIsInitialized) {
			throw new ErrorSeApiNotInitialized();
		}

		//if the user that invokes this functionality is not authenticated, ErrorUserNotAuthenticated has to be thrown in accordance with BSI TR-03151
		if(currentlyLoggedIn==null) {
			throw new ErrorUserNotAuthenticated();
		}
		//if the user is authenticated but not authorized, ErrorUserNotAuthorized has to be thrown in accordance with BSI TR-03151
		if(!(currentlyLoggedIn.getRole().equalsIgnoreCase("Admin"))) {
			throw new ErrorUserNotAuthorized();
		}
	
	//1. the function shall invoke the functionality of the secure element to log its deactivation
		byte[] resultOfDisableSE = null;
		try {
			resultOfDisableSE = this.securityModule.disableSE();
		} catch (SigningOperationFailedException e) {
			throw new ErrorRetrieveLogMessageFailed("Signing of the disableSecureElement SystemLogMessage failed.\n", e);
		} catch (ErrorSignatureCounterOverflow e) {
			throw new ErrorSignatureCounterOverflow(e.getMessage(), e);
		} catch (Exception e) {
			throw new ErrorRetrieveLogMessageFailed(e.getMessage(), e);
		}
	//2. the function has to retrieve the log message parts
		if(resultOfDisableSE == null) {
			throw new ErrorRetrieveLogMessageFailed();
		}
		SystemLogMessage resultingSysLog = (SystemLogMessage) createCompleteLogMessageFromByteArray(resultOfDisableSE,(short) 2);

	//3. the function shall then invoke the disable method in the Secure Element
		try {
			this.securityModule.setDisabled();
		} catch (Exception e){
			throw new ErrorDisableSecureElementFailed();
		}
		
	//4. store the SystemLogMessage
		try {
			this.storage.storeSystemLog(resultOfDisableSE, resultingSysLog.getLogTime(), resultingSysLog.getSignatureCounter(), resultingSysLog.getOperationType());
		} catch (Exception e) {
			throw new ErrorStorageFailure("Storing SystemLogMessage failed.\n",e);
		}
	//5. check if the certificate of the TSE is expired. If the certificate is expired, this function SHALL throw an exception because at this point, the
		//TSE has been initialized and a SystemLog has been created to log this.
		if(securityModule.getCryptoCore().isCertificateExpired()) {
			throw new ErrorCertificateExpired();
		}	
		
	//6. if everything was okay, return EXECUTION_OK
		return Constant.EXECUTION_OK;
	}

	
//---------------------------------------------INPUT FUNCTIONS---------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * Note: the processType is NOT OPTIONAL in the context of TR-03153. This TR specifies, that the processType MUST refer to the "type-of-process"
	 * that initiated the logging. 
	 * @see main.java.de.bsi.seapi.SEAPI#startTransaction(java.lang.String, byte[], java.lang.String, byte[], main.java.de.bsi.seapi.holdertypes.LongHolder, main.java.de.bsi.seapi.holdertypes.ZonedDateTimeHolder, main.java.de.bsi.seapi.holdertypes.ByteArrayHolder, main.java.de.bsi.seapi.holdertypes.LongHolder, main.java.de.bsi.seapi.holdertypes.ByteArrayHolder)
	 * 
	 */
	@Override
	public short startTransaction(String clientId, byte[] processData, String processType, byte[] additionalData,
			LongHolder transactionNumber, ZonedDateTimeHolder logTime, ByteArrayHolder serialNumber,
			LongHolder signatureCounter, ByteArrayHolder signatureValue)
			throws ErrorStartTransactionFailed, ErrorRetrieveLogMessageFailed, ErrorStorageFailure,
			ErrorSeApiNotInitialized, ErrorTimeNotSet, ErrorCertificateExpired, ErrorSecureElementDisabled, 
			ErrorTransactionCounterOverflow, ErrorSignatureCounterOverflow {
		//if the secure element is disabled, this exception has to be thrown
		if(this.securityModule.getSecureElementIsDisabled()) {
			throw new ErrorSecureElementDisabled();
		}
		
		//if the TSE is not initialized, throw ErrorSeApiNotInitialized
		if(!this.tseIsInitialized) {
			throw new ErrorSeApiNotInitialized();
		}
	//0. FORMAT CHECKS! check input data that is used to create log message for conformity to ASN.1 rules and if they are present
		if((!TLVUtility.isASN1_PrintableString(clientId)) || (!TLVUtility.isASN1_OctetString(processData)) || (!TLVUtility.isASN1_PrintableString(processType))) {
			throw new ErrorStartTransactionFailed();
		}
		//check if the processType is longer than 100 characters (if yes, throw an Exception)
		if(processType.length() > 100) {
			throw new ErrorStartTransactionFailed();
		}
		//check all the XYZHolder parameters. They are all REQUIRED except signatureValue. If signatureValue == null, nevermind.
		if((transactionNumber==null) || (logTime==null) || (serialNumber==null) || (signatureCounter==null)) {
			throw new ErrorStartTransactionFailed("REQUIRED parameter was null!\n");
		}
	
	//1. Function shall invoke the SecureElement functionality and pass on client ID process type and processData.
			//SecureElement shall generate a Transaction number
			// if the execution of the SecureElement functionality fails, ErrorStartTransactionFailed shall be thrown 
		byte[] resultOfStartTransaction = null;
		try {
			resultOfStartTransaction = this.securityModule.startTransaction(clientId, processData, processType, additionalData);
			//if anything goes wrong with the creation of the LogMessage throw an ErrorStartTransactionFailed
		} catch(ErrorTransactionCounterOverflow e) {
			throw new ErrorTransactionCounterOverflow(e.getMessage(), e);
		} catch(ErrorSignatureCounterOverflow e) {
			throw new ErrorSignatureCounterOverflow(e.getMessage(), e);
		} catch(ValueNullException e) {
			throw new ErrorStartTransactionFailed("ValueNullException caught.\n" +e.getMessage() +"\n");
		} catch (ValueTooBigException e) {
			throw new ErrorStartTransactionFailed("ValueTooBigException caught.\n" +e.getMessage() +"\n");
		} catch (TooManyOpenTransactionsException e) {
			throw new ErrorStartTransactionFailed(e.getMessage(), e);
		} catch(Exception e) {
			throw new ErrorStartTransactionFailed(e.getMessage(), e);
		}
		
	//2. Next, the function SHALL retrieve the parts of the logmessage determined by the secure element.
			//if the execution of this functionality fails, an ErrorRetrieveLogMessageFailed SHALL be raised.
		if(resultOfStartTransaction == null) {
			throw new ErrorRetrieveLogMessageFailed();
		}
		TransactionLogMessage resultingTransactionLog = (TransactionLogMessage) createCompleteLogMessageFromByteArray(resultOfStartTransaction, (short) 1);
	//3. The input data and the data of the retrieved log message parts SHALL be stored. If the data has not been stored successfully, the 
			//exception ErrorStorageFailure shall be raised
		try {
			this.storage.storeTransactionLog(resultOfStartTransaction, resultingTransactionLog.getLogTime(), resultingTransactionLog.getSignatureCounter(),
					resultingTransactionLog.getTransactionNumber(), resultingTransactionLog.getOperationType(), resultingTransactionLog.getClientID());
		} catch(Exception e) {
			throw new ErrorStorageFailure(e.getMessage());
		}
	
	//4. return the relevant data in the holder types
		transactionNumber.setValue(resultingTransactionLog.getTransactionNumber());
		
		Instant logTimeFromTransactionLog = Instant.ofEpochSecond(resultingTransactionLog.getLogTime());
		logTime.setValue(ZonedDateTime.ofInstant(logTimeFromTransactionLog, ZoneId.systemDefault()));
		
		serialNumber.setValue(resultingTransactionLog.getSerialNumber());
		signatureCounter.setValue(resultingTransactionLog.getSignatureCounter());
		//referring to 0., format checks: if signatureValue == null, nothing serious happens, but: do not try to return into an object thats null
		if(!(signatureValue==null)) {
			signatureValue.setValue(resultingTransactionLog.getSignatureValue());
		}
	//5. check if the certificate of the TSE is expired. If the certificate is expired, this function SHALL throw an exception because at this point, the
		//TSE has been initialized and a SystemLog has been created to log this.
	
		if(securityModule.getCryptoCore().isCertificateExpired()) {
			throw new ErrorCertificateExpired();
		}
		
		return Constant.EXECUTION_OK;
	}

	/* (non-Javadoc)
	 * @see main.java.de.bsi.seapi.SEAPI#updateTransaction(java.lang.String, long, byte[], java.lang.String, main.java.de.bsi.seapi.holdertypes.ZonedDateTimeHolder, main.java.de.bsi.seapi.holdertypes.ByteArrayHolder, main.java.de.bsi.seapi.holdertypes.LongHolder)
	 */
	@Override
	public short updateTransaction(String clientId, long transactionNumber, byte[] processData, String processType,
			ZonedDateTimeHolder logTime, ByteArrayHolder signatureValue, LongHolder signatureCounter)
			throws ErrorUpdateTransactionFailed, ErrorStorageFailure, ErrorRetrieveLogMessageFailed, ErrorNoTransaction,
			ErrorSeApiNotInitialized, ErrorTimeNotSet, ErrorCertificateExpired, ErrorSecureElementDisabled, ErrorSignatureCounterOverflow {
		//if SE is disabled, exception has to be thrown
		if(this.securityModule.getSecureElementIsDisabled()) {
			throw new ErrorSecureElementDisabled();
		}
		//if the TSE is not initialized, throw ErrorSeApiNotInitialized
		if(!tseIsInitialized) {
			throw new ErrorSeApiNotInitialized();
		}
		
	//0. FORMAT CHECKS! check input data that is used to create log message for conformity to ASN.1 rules and if they are present
		if((!TLVUtility.isASN1_PrintableString(clientId)) || (!TLVUtility.isASN1_OctetString(processData)) || (!TLVUtility.isASN1_PrintableString(processType))
				|| (!TLVUtility.isASN1_Integer(transactionNumber))) {
			throw new ErrorUpdateTransactionFailed();
		}
		//check if the processType is longer than 100 characters (if yes, throw an Exception)
		if(processType.length() > 100) {
			throw new ErrorUpdateTransactionFailed();
		}
//TODO: if updateTransactionMode=advanced is going to be implemented, these checks right here will have to be adjusted.
		//the XYZHolders are CONDITIONAL. 
		//in case of signed updates: logTime and signatureCounter are REQUIRED, signatureValue is OPTIONAL
		if((logTime==null) || (signatureCounter==null)) {
			throw new ErrorUpdateTransactionFailed("REQUIRED parameter was null!\n");
		}
		
	//1. SHALL invoke the functionality of the secure element to update a transaction. If this fails, an ErrorUpdateTransactionFailed SHALL be raised
	//2. the secure element SHALL check whether the transactionNumber belongs to an open transaction. If no open transaction with that number
			//exists the function SHALL raise an ErrorNoTransaction
			byte[] resultOfUpdateTransaction = null;
			try {
				resultOfUpdateTransaction = this.securityModule.updateTransaction(clientId, transactionNumber, processData, processType);
			} catch(ErrorNoTransaction e) {
				throw new ErrorNoTransaction();
			} catch(ErrorSignatureCounterOverflow e) {
				throw new ErrorSignatureCounterOverflow(e.getMessage(), e);
			} catch(Exception e) {
				throw new ErrorUpdateTransactionFailed();
			}
	
	//3. with signed updates: the parts of the logmessage created by the secure element SHALL be retrieved.
			//if that fails, the ErrorRetrieveLogMessageFailed SHALL be raised.
			if(resultOfUpdateTransaction == null) {
				throw new ErrorRetrieveLogMessageFailed();
			}
			TransactionLogMessage resultingTransactionLog = (TransactionLogMessage) createCompleteLogMessageFromByteArray(resultOfUpdateTransaction, (short) 1);
			
	//4. the input data and the parts of the logmessage SHALL be stored. If this fails, an ErrorStorageFailure SHALL be raised
			try {
				this.storage.storeTransactionLog(resultOfUpdateTransaction, resultingTransactionLog.getLogTime(), resultingTransactionLog.getSignatureCounter(),
						resultingTransactionLog.getTransactionNumber(), resultingTransactionLog.getOperationType(), resultingTransactionLog.getClientID());
			} catch(Exception e) {
				throw new ErrorStorageFailure(e.getMessage());
			}
			
	//5. return the relevant data in the holder types
			Instant logTimeFromTransactionLog = Instant.ofEpochSecond(resultingTransactionLog.getLogTime());
			logTime.setValue(ZonedDateTime.ofInstant(logTimeFromTransactionLog, ZoneId.systemDefault()));
			
			//referring to 0., format checks: if signatureValue == null, nothing serious happens, but: do not try to return into an object that is null
			if(!(signatureValue==null)) {
				signatureValue.setValue(resultingTransactionLog.getSignatureValue());
			}
			signatureCounter.setValue(resultingTransactionLog.getSignatureCounter());
	//6. check if the certificate of the TSE is expired. If the certificate is expired, this function SHALL throw an exception because at this point, the
			//TSE has been initialized and a SystemLog has been created to log this.
			if(securityModule.getCryptoCore().isCertificateExpired()) {
				throw new ErrorCertificateExpired();
			}
			
		return Constant.EXECUTION_OK;
	}

	/* (non-Javadoc)
	 * @see main.java.de.bsi.seapi.SEAPI#finishTransaction(java.lang.String, long, byte[], java.lang.String, byte[], main.java.de.bsi.seapi.holdertypes.ZonedDateTimeHolder, main.java.de.bsi.seapi.holdertypes.ByteArrayHolder, main.java.de.bsi.seapi.holdertypes.LongHolder)
	 */
	@Override
	public short finishTransaction(String clientId, long transactionNumber, byte[] processData, String processType,
			byte[] additionalData, ZonedDateTimeHolder logTime, ByteArrayHolder signatureValue,
			LongHolder signatureCounter)
			throws ErrorFinishTransactionFailed, ErrorRetrieveLogMessageFailed, ErrorStorageFailure,
			ErrorSeApiNotInitialized, ErrorTimeNotSet, ErrorCertificateExpired, ErrorSecureElementDisabled, 
			ErrorNoTransaction, ErrorSignatureCounterOverflow {
		
		//if SE disabled, throw exception
		if(this.securityModule.getSecureElementIsDisabled()) {
			throw new ErrorSecureElementDisabled();
		}
		//if the TSE is not initialized, throw ErrorSeApiNotInitialized
		if(!tseIsInitialized) {
			throw new ErrorSeApiNotInitialized();
		}
		
	//0. FORMAT CHECKS! check input data that is used to create log message for conformity to ASN.1 rules and if they are present
		if((!TLVUtility.isASN1_PrintableString(clientId)) || (!TLVUtility.isASN1_OctetString(processData)) || (!TLVUtility.isASN1_PrintableString(processType))
				|| (!TLVUtility.isASN1_Integer(transactionNumber))) {
			throw new ErrorFinishTransactionFailed();
		}
		//check if the processType is longer than 100 characters (if yes, throw an Exception)
		if(processType.length() > 100) {
			throw new ErrorFinishTransactionFailed();
		}
		//check all the XYZHolder parameters. They are all REQUIRED except signatureValue. If signatureValue == null, nevermind.
		if((logTime==null) || (signatureCounter==null)) {
			throw new ErrorFinishTransactionFailed("REQUIRED parameter was null!\n");
		}
		
	//1. the function SHALL invoke the functionality of the secure element to finish a transaction. If the execution of this fails, an ErrorFinishTransactionFailed
			//SHALL be raised
		byte[] resultOfFinishTransaction = null;
		try {
			resultOfFinishTransaction = this.securityModule.finishTransaction(clientId, transactionNumber, processData, processType, additionalData);
		} catch(ErrorNoTransaction e) {
			throw new ErrorNoTransaction();
		} catch(ErrorSignatureCounterOverflow e) {
			throw new ErrorSignatureCounterOverflow(e.getMessage(), e);
		} catch(Exception e) {
			throw new ErrorFinishTransactionFailed();
		}
		
	//2. Next, the function SHALL retrieve the parts of the logmessage determined by the secure element.
		//if the execution of this functionality fails, an ErrorRetrieveLogMessageFailed SHALL be raised.
			if(resultOfFinishTransaction == null) {
				throw new ErrorRetrieveLogMessageFailed();
			}
			//try to construct a TransactionLogMessage from the resultOfStartTransaction byte array
			TransactionLogMessage resultingTransactionLog = (TransactionLogMessage) createCompleteLogMessageFromByteArray(resultOfFinishTransaction, (short) 1);
			
	//3. since only signed updates are supported: the parts of the logmessage SHALL be retrieved from the secure element and stored
			//if this fails, an ErrorStorageFailure SHALL be raised
			try {
				this.storage.storeTransactionLog(resultOfFinishTransaction, resultingTransactionLog.getLogTime(), resultingTransactionLog.getSignatureCounter(),
						resultingTransactionLog.getTransactionNumber(), resultingTransactionLog.getOperationType(), resultingTransactionLog.getClientID());
			} catch(Exception e) {
				throw new ErrorStorageFailure(e.getMessage());
			}
	//4. return the relevant data in the holder types
			Instant logTimeFromTransactionLog = Instant.ofEpochSecond(resultingTransactionLog.getLogTime());
			logTime.setValue(ZonedDateTime.ofInstant(logTimeFromTransactionLog, ZoneId.systemDefault()));
			
			//referring to 0., format checks: if signatureValue == null, nothing serious happens, but: do not try to return into an object that is null
			if(!(signatureValue==null)) {
				signatureValue.setValue(resultingTransactionLog.getSignatureValue());
			}
			signatureCounter.setValue(resultingTransactionLog.getSignatureCounter());
	//5. check if the certificate of the TSE is expired. If the certificate is expired, this function SHALL throw an exception because at this point, the
			//TSE has been initialized and a SystemLog has been created to log this.
			if(securityModule.getCryptoCore().isCertificateExpired()) {
				throw new ErrorCertificateExpired();
			}

		return Constant.EXECUTION_OK;
	}

	
//-----------------------------------------------EXPORT FUNCTIONS-------------------------------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see main.java.de.bsi.seapi.SEAPI#exportData(long, java.lang.String, main.java.de.bsi.seapi.holdertypes.ByteArrayHolder)
	 */
	@Override
	public short exportData(long transactionNumber, String clientId, ByteArrayHolder exportedData)
			throws ErrorTransactionNumberNotFound, ErrorIdNotFound, ErrorSeApiNotInitialized, ErrorParameterMismatch {
		//if the TSE is not initialized, throw ErrorSeApiNotInitialized
		if(!tseIsInitialized) {
			throw new ErrorSeApiNotInitialized();
		}
		//0. FORMAT CHECKS! The function shall check the input parameters for validity. If any of the checks fails, ErrorParameterMismatch shall be thrown.
		if(exportedData == null) {
			throw new ErrorParameterMismatch("REQUIRED parameter was null!\n");
		}
		if((!TLVUtility.isASN1_PrintableString(clientId))) {
			throw new ErrorParameterMismatch("clientId shall be a printable string.\n");	//former exception: ErrorIdNotFound("clientId is not in the correct format!");
		}
		
		//use the storage to search for logs that bear the specified transaction number:
		Collection<File> transactionLogsFilteredByStorage = null;
		transactionLogsFilteredByStorage = this.storage.listFilesTransactionNumbers(transactionNumber, transactionNumber);
		
		//check whether filtering for that specific transaction number returned anything. 
		//if the array is empty, there was no transaction log with the specified number
		if( (transactionLogsFilteredByStorage==null) || (transactionLogsFilteredByStorage.size() == 0) ) {
			throw new ErrorTransactionNumberNotFound("No transaction log could be found for transaction number " +transactionNumber +"!");
		}
		
		//filter those for the specified clientId. If the result is null, throw the exception ErrorIdNotFound
		Collection<File> transactionLogsWithClientId = this.storage.listFilesClientID(clientId, transactionLogsFilteredByStorage);
		//if no data is available (transactionLogsWithClientId == null or transactionLogsWithClientId.length == 0) throw ErrorIdNotFound();
		if( (transactionLogsWithClientId == null) || (transactionLogsWithClientId.size() == 0) ) {
			throw new ErrorIdNotFound();
		} 
		//if there were files found for the specified transaction number AND clientId, get the lowest and highest signature counter value present.
		//this does not necessarily have to be the first and last entry in the returned array, since this array is sorted according to file name. 
		//This means, that the files are sorted according to their time stamp which could have changed during a transaction.
			//Example: startTransaction -> updateTime(timeBeforeStartTransaction) -> finishTransaction.
		
		//to hopefully save time, create a Collection<Long> which saves all the signature counters. Then sort that after getting start and finish. If the sorted array list has no 
			//gaps in it, one does not have to filter the stored logs again by signature counter.
		List<Long> transactionLogSigCntr = new ArrayList<Long>();
		
		//use filteredByStorage, not filteredByClientId. Otherwise, only parts of the transaction may be exported
		for(File entry : transactionLogsFilteredByStorage) {
			//get the signature counter from the file name
			String[] splitFileName = entry.getName().split("_");
			//if the file name was formatted correctly, getting the substring after Sig- should be the signatureCounter
			String sigCntr = splitFileName[2].substring(4);
			Long entrySigCntr = Long.parseLong(sigCntr);
			transactionLogSigCntr.add(entrySigCntr);
		}
		
		//sort the saved signature counters
		Collections.sort(transactionLogSigCntr);
		
		//create the File[] that will be exported
		File[] filteredForExport = null;
		
		//if the interval is continuous, then the assertion (signatureCounterOfLastElement - indexOfLastElement == signatureCounterOfFirstElement) should be true.
		//If it is not (that means, the expression is true for the '==' being replaced with '!='), additional filtering will be necessary:
		if( (transactionLogSigCntr.get(transactionLogSigCntr.size()-1) - (transactionLogSigCntr.size()-1)) != transactionLogSigCntr.get(0) ) {
			//we already have those files with the correct transaction number. Now we need only those with the missing signature counter
			Collection<File> logsFilteredBySigCntr= null;
			try {
				logsFilteredBySigCntr = this.storage.listFilesAuditLogsSystemLogsSignatureCounter(transactionLogSigCntr);
			} catch (SignatureCounterException e) {
				throw new ErrorTransactionNumberNotFound("Transaction number exists, but no logs could be found for the corresponding signature counter interval.");
			}

			
			//if there are elements in the collection that contains only syslogs and audit logs in the signature counter interval, add those to the existing collection
			//Remember: the existing collection only contains transaction logs 
			if((logsFilteredBySigCntr != null) && (!logsFilteredBySigCntr.isEmpty())) {
				transactionLogsFilteredByStorage.addAll(logsFilteredBySigCntr);
			}
		}
		
		//assign the filteredForExport array
		filteredForExport = transactionLogsFilteredByStorage.toArray(new File[0]);	
		
		//if everything went OK up to this point, call the export function of the storage!
		byte[] dataToBeExported = null;
		
		//the export function needs the SE API description, the manufacturer info, the serialNumber and the time at the calling of the method
		//get the current time:
		long timeOfMethodCall = this.securityModule.getCurrentTimeFromCryptoCore();
		dataToBeExported = this.storage.exportData(descriptionOfTheSEAPI, manufacturerInformation, versionInformation, timeOfMethodCall, filteredForExport);
		//set the value of the exportedData to the dataToBeExported array
		exportedData.setValue(dataToBeExported);
		
		//return EXECUTION_OK
		return Constant.EXECUTION_OK;
	}

	/* (non-Javadoc)
	 * @see main.java.de.bsi.seapi.SEAPI#exportData(long, main.java.de.bsi.seapi.holdertypes.ByteArrayHolder)
	 */
	@Override
	public short exportData(long transactionNumber, ByteArrayHolder exportedData)
			throws ErrorTransactionNumberNotFound, ErrorSeApiNotInitialized, ErrorParameterMismatch {
		//if the TSE is not initialized, throw ErrorSeApiNotInitialized
		if(!tseIsInitialized) {
			throw new ErrorSeApiNotInitialized();
		}
		
		//check input parameter
		if(exportedData == null) {
			throw new ErrorParameterMismatch("REQUIRED parameter was null!\n");
		}
		
		//use the storage to search for logs that bear the specified transaction number:
		Collection<File> transactionLogsFilteredByStorage = null;
		transactionLogsFilteredByStorage = this.storage.listFilesTransactionNumbers(transactionNumber, transactionNumber);
		
		//check whether filtering for that specific transaction number returned anything. 
		//if the array is empty, there was no transaction log with the specified number
		if(transactionLogsFilteredByStorage==null) {
			throw new ErrorTransactionNumberNotFound("No transaction log could be found for transaction number " +transactionNumber +"!");
		}
		
		//if there were files found for the specified transaction number, get the lowest and highest signature counter value present.
		//this does not necessarily have to be the first and last entry in the returned array, since this array is sorted according to file name. 
		//This means, that the files are sorted according to their time stamp which could have changed during a transaction.
			//Example: startTransaction -> updateTime(timeBeforeStartTransaction) -> finishTransaction.
		
		//to hopefully save time, create a Collection<Long> which saves all the signature counters. Then sort that after getting start and finish. If the sorted array list has no 
			//gaps in it, one does not have to filter the stored logs again by signature counter.
		List<Long> transactionLogSigCntr = new ArrayList<Long>();
		
		for(File entry : transactionLogsFilteredByStorage) {
			//get the signature counter from the file name
			String[] splitFileName = entry.getName().split("_");
			//if the file name was formatted correctly, getting the substring after Sig- should be the signatureCounter
			String sigCntr = splitFileName[2].substring(4);
			Long entrySigCntr = Long.parseLong(sigCntr);
			transactionLogSigCntr.add(entrySigCntr);
		}
				
		//sort the saved signature counters (the logs in storage could have been unordered, so change that!)
		Collections.sort(transactionLogSigCntr);
		
		//create the File[] that will be exported
		File[] filteredForExport = null;
		
		
		
		//if the interval is continuous, then the assertion (signatureCounterOfLastElement - indexOfLastElement == signatureCounterOfFirstElement) should be true.
		//If it is not (that means, the expression is true for the '==' being replaced with '!='), additional filtering will be necessary:
		if( (transactionLogSigCntr.get(transactionLogSigCntr.size()-1) - (transactionLogSigCntr.size()-1)) != transactionLogSigCntr.get(0) ) {
			//we already have those files with the correct transaction number. Now we need only those with the missing signature counter
			Collection<File> logsFilteredBySigCntr= null;
			try {
				logsFilteredBySigCntr = this.storage.listFilesAuditLogsSystemLogsSignatureCounter(transactionLogSigCntr);
			} catch (SignatureCounterException e) {
				throw new ErrorTransactionNumberNotFound("Transaction number exists, but no logs could be found for the corresponding signature counter interval.");
			}

			
			
			//if there are elements in the collection that contains only syslogs and audit logs in the signature counter interval, add those to the existing collection
			if( (logsFilteredBySigCntr != null) && (!logsFilteredBySigCntr.isEmpty()) ) {
				transactionLogsFilteredByStorage.addAll(logsFilteredBySigCntr);
			}
		}
		
		
		
		//assign the filteredForExport array
		filteredForExport = transactionLogsFilteredByStorage.toArray(new File[0]);	

		//if everything went OK up to this point, call the export function of the storage!
		byte[] dataToBeExported = null;
		
		//the export function needs the SE API description, the manufacturer info, the serialNumber and the time at the calling of the method
		//get the current time:
		long timeOfMethodCall = this.securityModule.getCurrentTimeFromCryptoCore();
		dataToBeExported = this.storage.exportData(descriptionOfTheSEAPI, manufacturerInformation, versionInformation, timeOfMethodCall, filteredForExport);
		//set the value of the exportedData to the dataToBeExported array
		exportedData.setValue(dataToBeExported);
		
		//return EXECUTION_OK
		return Constant.EXECUTION_OK;
	}

	/* (non-Javadoc)
	 * @see main.java.de.bsi.seapi.SEAPI#exportData(long, long, int, main.java.de.bsi.seapi.holdertypes.ByteArrayHolder)
	 */
	@Override
	public short exportData(long startTransactionNumber, long endTransactionNumber, int maximumNumberRecords,
			ByteArrayHolder exportedData) throws ErrorParameterMismatch, ErrorTransactionNumberNotFound,
			ErrorTooManyRecords, ErrorSeApiNotInitialized {
		
		//if the TSE is not initialized, throw ErrorSeApiNotInitialized
		if(!tseIsInitialized) {
			throw new ErrorSeApiNotInitialized();
		}
		//0. FORMAT CHECKS! startTransactionnumber > 0 AND endTransactionNumber > 0 AND startTransactionNumber < endTransactionNumber checks are performed 
			//by the storage class. maximumNumberRecords >= 0 can be deducted from the description of what that parameter does. 
		if(maximumNumberRecords<0) {
			throw new ErrorParameterMismatch("maximumNumberRecords < 0 does not make sense.\n");
		}
		if(exportedData == null) {
			throw new ErrorParameterMismatch("REQUIRED parameter was null!\n");
		}
		
		//1. The function shall check whether any transactions are stored for the interval
			//If the parameters are mismatched, the exception ErrorParameterMismatch is thrown by the storage class
		Collection<File> transactionLogsFilteredByStorage = this.storage.listFilesTransactionNumbers(startTransactionNumber, endTransactionNumber);
		//1.1 If no data has been found for the interval, the function shall raise the exception ErrorTransactionNumberNotFound
		if((transactionLogsFilteredByStorage == null) || (transactionLogsFilteredByStorage.size() == 0)) {
			throw new ErrorTransactionNumberNotFound();
		}
		
		//1.2 Filter for the signature counter of the first log in the interval and the last log
			//if there were files found for the specified transaction number, get the lowest and highest signature counter value present.
			//this does not necessarily have to be the first and last entry in the returned array, since this array is sorted according to file name. 
			//This means, that the files are sorted according to their time stamp which could have changed during a transaction.
			//Example: startTransaction -> updateTime(timeBeforeStartTransaction) -> finishTransaction.
				
			//to hopefully save time, create a Collection<Long> which saves all the signature counters. Then sort that after getting start and finish. If the sorted array list has no 
				//gaps in it, one does not have to filter the stored logs again by signature counter.
			List<Long> transactionLogSigCntr = new ArrayList<Long>();
			
			for(File entry : transactionLogsFilteredByStorage) {
				//get the signature counter from the file name
				String[] splitFileName = entry.getName().split("_");
				//if the file name was formatted correctly, getting the substring after Sig- should be the signatureCounter
				String sigCntr = splitFileName[2].substring(4);
				Long entrySigCntr = Long.parseLong(sigCntr);
				transactionLogSigCntr.add(entrySigCntr);
			}
		//1.3 check whether the logs in the first array have already a continuous interval of signature counters. If thats the case, we do not have to filter again.
			//sort the saved signature counters
			Collections.sort(transactionLogSigCntr);
		
			//create the File[] that will be exported
			File[] filteredForExport = null;
			
		//1.4 if the interval is continuous, then the assertion (signatureCounterOfLastElement - indexOfLastElement == signatureCounterOfFirstElement) should be true.
			//If it is not (that means, the expression is true for the '==' being replaced with '!='), additional filtering will be necessary 
			//(getting the missing Syslogs and AuditLogs):
			if( (transactionLogSigCntr.get(transactionLogSigCntr.size()-1) - (transactionLogSigCntr.size()-1)) != transactionLogSigCntr.get(0) ) {
				//we already have those files with the correct transaction number. Now we need only those with the missing signature counter
				Collection<File> logsFilteredBySigCntr= null;
				try {
					logsFilteredBySigCntr = this.storage.listFilesAuditLogsSystemLogsSignatureCounter(transactionLogSigCntr);
				} catch (SignatureCounterException e) {
					throw new ErrorTransactionNumberNotFound("Transaction number exists, but no logs could be found for the corresponding signature counter interval.");
				}

				
				
				//if there are elements in the collection that contains only syslogs and audit logs in the signature counter interval, add those to the existing collection
				if( (logsFilteredBySigCntr != null) && (!logsFilteredBySigCntr.isEmpty())) {
					transactionLogsFilteredByStorage.addAll(logsFilteredBySigCntr);
				}
			}

		//1.5 assign the filteredForExport array
			filteredForExport = transactionLogsFilteredByStorage.toArray(new File[0]);	
		
		//1.6 If maxNumberRecords has been provided and it's number is not null, the function shall check whether the amount of records found is less than or 
			//equal to maxNumberRecords. If not, the function shall throw the exception ErrorTooManyRecords
		if((maximumNumberRecords != 0) && (maximumNumberRecords < filteredForExport.length)) {
			throw new ErrorTooManyRecords();
		}
	
		//1.7 If everything went great up to this point, prepare for export!
		byte[] dataToBeExported = null;
		
		//the export function needs the SE API description, the manufacturer info, the serialNumber and the time at the calling of the method
		//get the current time:
		long timeOfMethodCall = this.securityModule.getCurrentTimeFromCryptoCore();
		dataToBeExported = this.storage.exportData(descriptionOfTheSEAPI, manufacturerInformation, versionInformation, timeOfMethodCall, filteredForExport);
		//set the value of the exportedData to the dataToBeExported array
		exportedData.setValue(dataToBeExported);
				
		//1.8 Lastly return EXECUTION_OK
		return Constant.EXECUTION_OK;
	}

	/* (non-Javadoc)
	 * @see main.java.de.bsi.seapi.SEAPI#exportData(long, long, java.lang.String, int, main.java.de.bsi.seapi.holdertypes.ByteArrayHolder)
	 */
	@Override
	public short exportData(long startTransactionNumber, long endTransactionNumber, String clientId,
			int maximumNumberRecords, ByteArrayHolder exportedData) throws ErrorParameterMismatch,
			ErrorTransactionNumberNotFound, ErrorIdNotFound, ErrorTooManyRecords, ErrorSeApiNotInitialized {
		//if the TSE is not initialized, throw ErrorSeApiNotInitialized
		if(!tseIsInitialized) {
			throw new ErrorSeApiNotInitialized();
		}
		//0. FORMAT CHECKS! startTransactionNumber > 0 AND endTransactionNumber > 0 AND startTransactionNumber < endTransactionNumber checks are performed 
			//by the storage class. maximumNumberRecords >= 0 can be deducted from the description of what that parameter does. 
		if((!TLVUtility.isASN1_PrintableString(clientId))) {
			throw new ErrorParameterMismatch("clientId shall be a printable string.\n");
		}
		if(maximumNumberRecords < 0) {
			throw new ErrorParameterMismatch("maximumNumberRecords < 0 does not make sense.\n");
		}
		if(exportedData == null) {
			throw new ErrorParameterMismatch("REQUIRED parameter was null!\n");
		}
		
		//1. The function shall check whether any transactions are stored for the interval
			//If the parameters are mismatched, the exception ErrorParameterMismatch is thrown by the storage class
		Collection<File> transactionLogsFilteredByStorage = this.storage.listFilesTransactionNumbers(startTransactionNumber, endTransactionNumber);
		//1.1a If no data has been found for the interval, the function shall raise the exception ErrorTransactionNumberNotFound
		if((transactionLogsFilteredByStorage == null) || (transactionLogsFilteredByStorage.size() == 0)) {
			throw new ErrorTransactionNumberNotFound();
		}
		
		//1.1b If a clientId is present, the function shall check for transactions belonging to that clientId
		Collection<File> transactionLogsFilteredByClientId = this.storage.listFilesClientID(clientId, transactionLogsFilteredByStorage);
			//If no transactions are stored for that particular clientId throw exception ErrorIdNotFound
		if((transactionLogsFilteredByClientId == null) || (transactionLogsFilteredByClientId.size() == 0)) {
			throw new ErrorIdNotFound();
		}
		
		//observe: only those transaction logs shall be included in the export and therefore define the signature counter interval in which System- and Audit-Logs have to 
			//be exported, that have transaction numbers lying inside the interval [startTransactionNumber, endTransactionNumber] AND that were created with at least 
			//one operation (start, update, finish) performed by clientId.
		
			//if the content of transactionLogsFilteredByStorage == content of transactionLogsFilteredByClientId then the length of both collections should be equal.
			//It would also mean, that there are no such log messages that were created wholly without any contribution of client == clientId.
			//Therefore, no additional filtering would be needed
					

		Collection<File> transactionLogsWhereClientIdWasInvolvedInTransaction;
		//So, if transactionLogsFilteredByClientId.size() < transactionLogsFilteredByStorage.size()	there are transaction logs that need to be sorted out
		if(transactionLogsFilteredByClientId.size() < transactionLogsFilteredByStorage.size()) {
			transactionLogsWhereClientIdWasInvolvedInTransaction = this.storage.listFilesClientIdAndTransactionNumber(transactionLogsFilteredByClientId, transactionLogsFilteredByStorage);
		}
		else {
			//if not, go on with the logs filtered by transaction number
			transactionLogsWhereClientIdWasInvolvedInTransaction = transactionLogsFilteredByStorage;
		}
		
		
		
		//1.2 Filter for the signature counter of the first log in the interval and the last log
			//if there were files found for the specified transaction number, get the lowest and highest signature counter value present.
			//this does not necessarily have to be the first and last entry in the returned array, since this array is sorted according to file name. 
			//This means, that the files are sorted according to their time stamp which could have changed during a transaction.
			//Example: startTransaction -> updateTime(timeBeforeStartTransaction) -> finishTransaction.
				
			//to hopefully save time, create a Collection<Long> which saves all the signature counters. Then sort that after getting start and finish. If the sorted array list has no 
				//gaps in it, one does not have to filter the stored logs again by signature counter.
			List<Long> transactionLogSigCntr = new ArrayList<Long>();
			
			//use transactionLogsWhereClientIdWasInvolvedInTransaction, as to not export unnecessary transaction logs
			for(File entry : transactionLogsWhereClientIdWasInvolvedInTransaction) {
				//get the signature counter from the file name
				String[] splitFileName = entry.getName().split("_");
				//if the file name was formatted correctly, getting the substring after Sig- should be the signatureCounter
				String sigCntr = splitFileName[2].substring(4);
				Long entrySigCntr = Long.parseLong(sigCntr);
				transactionLogSigCntr.add(entrySigCntr);
			}
		//1.3 check whether the logs in the first array have already a continuous interval of signature counters. If thats the case, we do not have to filter again.
			//sort the saved signature counters
			Collections.sort(transactionLogSigCntr);
			
			//create the File[] that will be exported
			File[] filteredForExport = null;
		
		//1.4 if the interval is continuous, then the assertion (signatureCounterOfLastElement - indexOfLastElement == signatureCounterOfFirstElement) should be true.
			//If it is not (that means, the expression is true for the '==' being replaced with '!='), additional filtering will be necessary 
			//(getting the missing Syslogs and AuditLogs):
			if( (transactionLogSigCntr.get(transactionLogSigCntr.size()-1) - (transactionLogSigCntr.size()-1)) != transactionLogSigCntr.get(0) ) {
				//we already have those files with the correct transaction number. Now we need only those with the missing signature counter
				Collection<File> logsFilteredBySigCntr= null;
				try {
					logsFilteredBySigCntr = this.storage.listFilesAuditLogsSystemLogsSignatureCounter(transactionLogSigCntr);
				} catch (SignatureCounterException e) {
					throw new ErrorTransactionNumberNotFound("Transaction number exists, but no logs could be found for the corresponding signature counter interval.");
				}

				//if there are elements in the collection that contains only syslogs and audit logs in the signature counter interval, add those to the existing collection
				//Remember: the existing collection only contains transaction logs with the specified clientId
				if( (logsFilteredBySigCntr != null) && (!logsFilteredBySigCntr.isEmpty())) {
					transactionLogsWhereClientIdWasInvolvedInTransaction.addAll(logsFilteredBySigCntr);
				}
			}
			
		//1.5 assign the filteredForExport array
			filteredForExport = transactionLogsWhereClientIdWasInvolvedInTransaction.toArray(new File[0]);
		
		//1.6 If maxNumberRecords has been provided and it's number is not null, the function shall check whether the amount of records found is less than or 
			//equal to maxNumberRecords. If not, the function shall throw the exception ErrorTooManyRecords
		if((maximumNumberRecords != 0) && (maximumNumberRecords < filteredForExport.length)) {
			throw new ErrorTooManyRecords();
		}
		
		//1.7 If everything went great up to this point, prepare for export!
		byte[] dataToBeExported = null;
		
		//the export function needs the SE API description, the manufacturer info, the serialNumber and the time at the calling of the method
		//get the current time:
		long timeOfMethodCall = this.securityModule.getCurrentTimeFromCryptoCore();
		dataToBeExported = this.storage.exportData(descriptionOfTheSEAPI, manufacturerInformation, versionInformation, timeOfMethodCall, filteredForExport);
		//set the value of the exportedData to the dataToBeExported array
		exportedData.setValue(dataToBeExported);
				
		//1.8 Lastly return EXECUTION_OK
		return Constant.EXECUTION_OK;
	}

	//works
	/* (non-Javadoc)
	 * @see main.java.de.bsi.seapi.SEAPI#exportData(java.time.ZonedDateTime, java.time.ZonedDateTime, int, main.java.de.bsi.seapi.holdertypes.ByteArrayHolder)
	 */
	@Override
	public short exportData(ZonedDateTime startDate, ZonedDateTime endDate, int maximumNumberRecords,
			ByteArrayHolder exportedData)
			throws ErrorParameterMismatch, ErrorNoDataAvailable, ErrorTooManyRecords, ErrorSeApiNotInitialized {
		//if the TSE is not initialized, throw ErrorSeApiNotInitialized
		if(!tseIsInitialized) {
			throw new ErrorSeApiNotInitialized();
		}
		//0. FORMAT CHECKS! is everything present that should be present, e.g. is the clientID not null?	
		if(maximumNumberRecords < 0) {
			throw new ErrorParameterMismatch("maximumNumberRecords < 0 does not make sense.\n");
		}
		if(exportedData == null) {
			throw new ErrorParameterMismatch("REQUIRED parameter was null!\n");
		}
		
		//save the status of the start and the end date in a boolean, because what this method does is VERY conditional
		boolean startDateNull = (startDate == null);
		boolean endDateNull = (endDate==null);
		
			//if both startDate and endDate are null, throw an exception
			if(startDateNull && endDateNull) {
				throw new ErrorParameterMismatch("startDate and/or endDate have to be provided!");
			}
			//if end date is present, check if the date is viable
			if(!endDateNull) {
				if((endDate.isBefore(Constants.EARLIEST_LEGAL_TIME)) || (endDate.isAfter(Constants.LATEST_LEGAL_TIME))) {
					throw new ErrorParameterMismatch("Provided endDate is not legal! Must be between 1.1.2019 and 1.1.2100!");
				}
				//if the startDate is present as well, make sure that is legal (aka is BEFORE endDate but after EARLIEST_LEGAL_TIME)
				if(!startDateNull) {
					if((startDate.isBefore(Constants.EARLIEST_LEGAL_TIME)) || (startDate.isAfter(endDate))) {
						throw new ErrorParameterMismatch("Provided startDate is not legal! Must be after 1.1.2019 and before endDate (if provided)");
					}
				}
			}
			
			//if the endDate is not present but startDate is, check if the startDate is viable on its own
			if(!startDateNull && endDateNull) {
				if((startDate.isBefore(Constants.EARLIEST_LEGAL_TIME)) || (startDate.isAfter(Constants.LATEST_LEGAL_TIME))) {
					throw new ErrorParameterMismatch("Provided startDate is not legal! Must be between 1.1.2019 and 1.1.2100!");
				}
			}
			
			//call listFilesStartDateEndDate(ZonedDateTime startDate, ZonedDateTime endDate) 
			//use the length to compare it to maxNumberRecords(if present)
			File[] filteredByStorage = this.storage.listFilesStartDateEndDate(startDate, endDate);
				//if no data is available (aka filtered by storage == null or filteredByStorage.length == 0) throw ErrorNoDataAvailable 
				if((filteredByStorage == null) || (filteredByStorage.length == 0)) {
					throw new ErrorNoDataAvailable();
				}
				//if data is available for that period of time, the function shall check if it is <= maxNumberRecords (except when maxNumberRecords is 0)
				if((maximumNumberRecords != 0) && (maximumNumberRecords < filteredByStorage.length)) {
					throw new ErrorTooManyRecords("maximumNumberRecords was  " +maximumNumberRecords +"  but  " +filteredByStorage.length +"  records are available.");
				}
				
			//if everything went OK up to this point, call the export function of the storage!
			byte[] dataToBeExported = null;
			
			//the export function needs the SE API description, the manufacturer info, the serialNumber and the time at the calling of the method
			//get the current time:
			long timeOfMethodCall = this.securityModule.getCurrentTimeFromCryptoCore();
			dataToBeExported = this.storage.exportData(descriptionOfTheSEAPI, manufacturerInformation,versionInformation, timeOfMethodCall, filteredByStorage);
			//set the value of the exportedData to the dataToBeExported array
			exportedData.setValue(dataToBeExported);
			
		//return EXECUTION_OK
		return Constant.EXECUTION_OK;
	}

	/* (non-Javadoc)
	 * @see main.java.de.bsi.seapi.SEAPI#exportData(java.time.ZonedDateTime, java.time.ZonedDateTime, java.lang.String, int, main.java.de.bsi.seapi.holdertypes.ByteArrayHolder)
	 */
	@Override
	public short exportData(ZonedDateTime startDate, ZonedDateTime endDate, String clientId, int maximumNumberRecords,
			ByteArrayHolder exportedData) throws ErrorParameterMismatch, ErrorNoDataAvailable, ErrorIdNotFound,
			ErrorTooManyRecords, ErrorSeApiNotInitialized {
		
		//if the TSE is not initialized, throw ErrorSeApiNotInitialized
		if(!tseIsInitialized) {
			throw new ErrorSeApiNotInitialized();
		}
		//0. FORMAT CHECKS! startTransactionNumber > 0 AND endTransactionNumber > 0 AND startTransactionNumber < endTransactionNumber checks are performed 
		//by the storage class. maximumNumberRecords >= 0 can be deducted from the description of what that parameter does. 
		if((!TLVUtility.isASN1_PrintableString(clientId))) {
			throw new ErrorParameterMismatch("clientId shall be a printable string.\n");
		}
		if(maximumNumberRecords < 0) {
			throw new ErrorParameterMismatch("maximumNumberRecords < 0 does not make sense.\n");
		}
		if(exportedData == null) {
			throw new ErrorParameterMismatch("REQUIRED parameter was null!\n");
		}
		
		
		//save the status of the start and the end date in a boolean, because what this method does is VERY conditional
			boolean startDateNull = (startDate == null);
			boolean endDateNull = (endDate==null);
			
			//if both are null, exception
			if(startDateNull && endDateNull) {
				throw new ErrorParameterMismatch("startDate and/or endDate have to be provided!");
			}
			//if end date is present, check if the date is viable
			if(!endDateNull) {
				if((endDate.isBefore(Constants.EARLIEST_LEGAL_TIME)) || (endDate.isAfter(Constants.LATEST_LEGAL_TIME))) {
					throw new ErrorParameterMismatch("Provided endDate is not legal! Must be between 1.1.2019 and 1.1.2100!");
				}
				//if the startDate is present as well, make sure that is legal (aka is BEFORE endDate but after EARLIEST_LEGAL_TIME)
				if(!startDateNull) {
					if((startDate.isBefore(Constants.EARLIEST_LEGAL_TIME)) || (startDate.isAfter(endDate))) {
						throw new ErrorParameterMismatch("Provided startDate is not legal! Must be after 1.1.2019 and before endDate (if provided)");
					}
				}
			}
			
			//if the endDate is not present but startDate is check if the startDate is viable on its own
			if(!startDateNull && endDateNull) {
				if((startDate.isBefore(Constants.EARLIEST_LEGAL_TIME)) || (startDate.isAfter(Constants.LATEST_LEGAL_TIME))) {
					throw new ErrorParameterMismatch("Provided startDate is not legal! Must be between 1.1.2019 and 1.1.2100!");
				}
			}
			
			//call listFilesStartDateEndDate(ZonedDateTime startDate, ZonedDateTime endDate) 
			//use the length to compare it to maxNumberRecords(if present)
			File[] filteredByStorageStage1 = this.storage.listFilesStartDateEndDate(startDate, endDate);
				//if no data is available (aka filtered by storage == null or filteredByStorage.length == 0) throw ErrorNoDataAvailable 
				if((filteredByStorageStage1 == null) || (filteredByStorageStage1.length == 0)) {
					throw new ErrorNoDataAvailable();
				}
				
				//filter files again, now filtering by clientID
			File[] filteredByStorageStage2 = this.storage.listFilesClientIDOLD(clientId, filteredByStorageStage1);	
				//if no data is available (aka filtered by storage == null or filteredByStorage.length == 0) throw ErrorIdNotFound();
				if((filteredByStorageStage2 == null) || (filteredByStorageStage2.length == 0)) {
					throw new ErrorIdNotFound();
				}
				
				//if data is available for that period of time, the function shall check if it is <= maxNumberRecords (except when maxNumberRecords is 0)
				if((maximumNumberRecords != 0) && (maximumNumberRecords < filteredByStorageStage2.length)) {
					throw new ErrorTooManyRecords();
				}
				
			//if everything went OK up to this point, call the export function of the storage!
			byte[] dataToBeExported = null;
			
			//the export function needs the SE API description, the manufacturer info, the serialNumber and the time at the calling of the method
			//get the current time:
			long timeOfMethodCall = this.securityModule.getCurrentTimeFromCryptoCore();
			dataToBeExported = this.storage.exportData(descriptionOfTheSEAPI, manufacturerInformation,versionInformation, timeOfMethodCall, filteredByStorageStage2);
			//set the value of the exportedData to the dataToBeExported array
			exportedData.setValue(dataToBeExported);
			
		//return EXECUTION_OK
		return Constant.EXECUTION_OK;
	}
	
	/* (non-Javadoc)
	 * @see main.java.de.bsi.seapi.SEAPI#exportData(int, main.java.de.bsi.seapi.holdertypes.ByteArrayHolder)
	 */
	@Override
	public short exportData(int maximumNumberRecords, ByteArrayHolder exportedData)
			throws ErrorTooManyRecords, ErrorSeApiNotInitialized, ErrorParameterMismatch {
		//if the TSE is not initialized, throw ErrorSeApiNotInitialized
		if(!tseIsInitialized) {
			throw new ErrorSeApiNotInitialized();
		}
		//FORMAT CHECKS:
		if(maximumNumberRecords < 0) {
			throw new ErrorParameterMismatch("maximumNumberRecords < 0 does not make sense.\n");
		}
		if(exportedData == null) {
			throw new ErrorParameterMismatch("REQUIRED parameter was null!\n");
		}
		
		//look in the storage for available logs
		File [] storedLogs = this.storage.listLogFilesOLD();
		//use the length to compare it to maxNumberRecords
		
		//according to documentation of the internally used Java.io.File.listFiles() , storedLogs should only be null, if the path name (of the storageDir
			//from config.properties) did not denote a directory or if an I/O error occurred.
			//The array will otherwise exist, but have a length of zero.
			if(storedLogs == null) {
				throw new ErrorTooManyRecords("Something went wrong fetching the logs. Check pathname and/or try again!");
			}
			//if there was no Error but no available data either, return an empty array and EXECUTION_OK
			if(storedLogs.length == 0) {
				byte[] noDataFound = {};
				exportedData.setValue(noDataFound);
				return Constant.EXECUTION_OK;
			}
		
			//if the number of logs that can be exported is greater than the value of maxNumberRecords
			//AND the maximumNumberRecords was not zero, throw an Exception
			if((maximumNumberRecords != 0) && (maximumNumberRecords < storedLogs.length)) {
				throw new ErrorTooManyRecords("maximumNumberRecords was  " +maximumNumberRecords +"  but  " +storedLogs.length +"  records are available.");
			}

			//if everything went OK up to this point, call the export function of the storage!
			byte[] dataToBeExported = null;
			
			//the export function needs the SE API description, the manufacturer info, the serialNumber and the time at the calling of the method
			//get the current time:
			long timeOfMethodCall = this.securityModule.getCurrentTimeFromCryptoCore();
			dataToBeExported = this.storage.exportData(descriptionOfTheSEAPI, manufacturerInformation, versionInformation, timeOfMethodCall, storedLogs);
				
				
			//set the value of the exportedData to the dataToBeExported array
			exportedData.setValue(dataToBeExported);
			
			//return EXECUTION_OK
			return Constant.EXECUTION_OK;
	}
	
	
	
	/* (non-Javadoc)
	 * @see main.java.de.bsi.seapi.SEAPI#exportCertificates(main.java.de.bsi.seapi.holdertypes.ByteArrayHolder)
	 */
	@Override
	public short exportCertificates(ByteArrayHolder certificates)
			throws ErrorExportCertFailed, ErrorSeApiNotInitialized {
		 //0. check input:
		 if(certificates == null) {
			 throw new ErrorExportCertFailed("REQUIRED parameter was null!\n");
		 }
		
		//1. if the SE API is not initialized, throw ErrorSeApiNotInitialized
		if(!tseIsInitialized) {
			throw new ErrorSeApiNotInitialized();
		}
		
		//2. the function shall collect the certificates used to create signatures in the TSE
			//This means it shall collect the actual TSE-Certificate and any other certificate
		//if everything went OK up to this point, call the export certificates function of the storage!
		byte[] dataToBeExported = null;
		
		//get the current time:
		long timeOfMethodCall = this.securityModule.getCurrentTimeFromCryptoCore();
		//if the list of certificate files is non-existent or empty, or if something happens during TAR-archive creation, the Storage class throws an 
			//ErrorExportCertFailed exception
		dataToBeExported = this.storage.exportCertificateData(timeOfMethodCall);
		
		//set the value of the exportedData to the dataToBeExported array
		certificates.setValue(dataToBeExported);
		
		return Constant.EXECUTION_OK;
	}

	//is optional
	/* (non-Javadoc)
	 * @see main.java.de.bsi.seapi.SEAPI#restoreFromBackup(byte[])
	 */
	@Override
	public short restoreFromBackup(byte[] restoreData)
			throws ErrorRestoreFailed, ErrorSeApiNotInitialized, ErrorUserNotAuthorized, ErrorUserNotAuthenticated {
		if(!tseIsInitialized) {
			throw new ErrorSeApiNotInitialized();
		}
		//is this exception necessary?
		if(restoreData==null) {
			throw new ErrorRestoreFailed("REQUIRED parameter was null!\n");
		}
		
		//if there is no user currently logged in 
		if(currentlyLoggedIn == null) {
			throw new ErrorUserNotAuthenticated();
		}
		
		//if the currently authenticated user does not have the authority to invoke this funtionality
		if(!(currentlyLoggedIn.getRole().equalsIgnoreCase("Admin"))) {
			throw new ErrorUserNotAuthorized();
		}
//TODO: implement correctly in the future if desired.		
		
		return Constant.EXECUTION_OK;
	}

	
	

	/** (non-Javadoc)
	 * @see main.java.de.bsi.seapi.SEAPI#readLogMessage(main.java.de.bsi.seapi.holdertypes.ByteArrayHolder)
	 */
	@Override
	public short readLogMessage(ByteArrayHolder logMessage)
			throws ErrorNoLogMessage, ErrorReadingLogMessage, ErrorSeApiNotInitialized, ErrorSecureElementDisabled, ErrorParameterMismatch {
		//if the SE is disabled, throw this exception
		if(this.securityModule.getSecureElementIsDisabled()) {
			throw new ErrorSecureElementDisabled();
		}
		//if the TSE has not been initialized, throw new ErrorSeApiNotInitialized
		if(!tseIsInitialized) {
			throw new ErrorSeApiNotInitialized();
		}
		//if there is nothing to write the output to, throw an exception
		if(logMessage == null) {
			throw new ErrorParameterMismatch("REQUIRED parameter was null!\n");
		}
		byte[] tmpByteArray = null;
		//1. function SHALL retrieve parts of the log message from the secure element
		try {
			tmpByteArray = securityModule.getLatestLogMessage();
		} catch(ErrorNoLogMessage e) {
			throw new ErrorNoLogMessage(e.getMessage(), e);
			//2. if reading the log message fails, throw the appropriate exception 
		} catch(Exception e2) {
			throw new ErrorReadingLogMessage();
		}
		//3. create a complete log message from the received parts (do nothing, because log message is already complete)
			//then return the log message in the ByteHolder
		logMessage.setValue(tmpByteArray);
		//4. return Execution_Ok 
		return Constant.EXECUTION_OK;
	}

	/* (non-Javadoc)
	 * @see main.java.de.bsi.seapi.SEAPI#exportSerialNumbers(main.j
	 * ava.de.bsi.seapi.holdertypes.ByteArrayHolder)
	 */
	@Override
	public short exportSerialNumbers(ByteArrayHolder serialNumbers)
			throws ErrorExportSerialNumbersFailed, ErrorSeApiNotInitialized {
		//if the TSE has not been initialized, throw new ErrorSeApiNotInitialized
		if(!tseIsInitialized) {
			throw new ErrorSeApiNotInitialized();
		}
		//if the input parameter is null, respond accordingly. Either ErrorExportSerialNumbersFailed OR add ErrorParameterMismatch to this function as well
		if(serialNumbers == null){
			throw new ErrorExportSerialNumbersFailed("REQUIRED parameter was null!\n");
		}
		
			//this section creates the serial number that can be exported
			//it does not have to look up which key is used for which type of log, because the simulator signs 
			//everything with the same key.
		
		//create the TLVs for BSI TR-03151 chapter 9 IMPLICIT Tags
			//set transaction log tag to 0x80 and value to 0xff (aka TRUE)
			TLVObject transactionLog = new TLVObject();
			transactionLog.setTagWithIntegerElement(0x80);
			byte[] boolTrue = {(byte) 0xFF};
			transactionLog.setValue(boolTrue);
		
			//set system log tag to 0x81 and value to 0xff (aka TRUE)
			TLVObject systemLog = new TLVObject();
			systemLog.setTagWithIntegerElement(0x81);
			systemLog.setValue(boolTrue);
			
			//set audit log tag to 0x82 and value to 0x00 (aka FALSE) 
			TLVObject auditLog = new TLVObject();
			auditLog.setTagWithIntegerElement(0x82);
			auditLog.setValueWithIntegerElement(0);
			
		//create signedLogmessageType (SEQUENCE)
			TLVObject signedLogmessageType = new TLVObject();
			signedLogmessageType.setTagWithByteElement(ASN1Constants.UNIVERSAL_SEQUENCE);
			//append transactionLog, systemLog and auditLog to this TLVObject
			try {
				signedLogmessageType.appendChild(transactionLog);
				signedLogmessageType.appendChild(systemLog);
				signedLogmessageType.appendChild(auditLog);
			} catch (TLVException | ValueNullException | ValueTooBigException e) {
				throw new ErrorExportSerialNumbersFailed("Unable to append signedLogMessageType children.\nCause:\t" +e.getMessage());
			}
			
		//create serialNumber (OCTET STRING)
			TLVObject serialNumber = new TLVObject();
			serialNumber.setTagWithByteElement(ASN1Constants.UNIVERSAL_OCTET_STRING);
			try {
				serialNumber.setValue(this.securityModule.getSerialNumber());
			} catch(Exception e) {
				throw new ErrorExportSerialNumbersFailed("Unable to fetch serial number from secure element!\n" +e.getMessage());
			}
			
		//create serialNumberRecord (SEQUENCE)
			TLVObject serialNumberRecord = new TLVObject();
			serialNumberRecord.setTagWithByteElement(ASN1Constants.UNIVERSAL_SEQUENCE);
			//try to append the serialNumber and the signedLogmessageType to this
			try {
				serialNumberRecord.appendChild(serialNumber);
				serialNumberRecord.appendChild(signedLogmessageType);
			} catch (TLVException | ValueNullException | ValueTooBigException e) {
				throw new ErrorExportSerialNumbersFailed("Unable to append serialNumberRecord children.\nCause:\t" +e.getMessage());
			}
				
		//create setSerialNumbers (SEQUENCE OF)
		TLVObject setSerialNumbers = new TLVObject();
		setSerialNumbers.setTagWithByteElement(ASN1Constants.UNIVERSAL_SEQUENCE);
			//try to append serialNumberRecord to this
			try {
				setSerialNumbers.appendChild(serialNumberRecord);
			} catch (TLVException | ValueNullException | ValueTooBigException e) {
				throw new ErrorExportSerialNumbersFailed("Unable to append setSerialNumbers child.\nCause:\t" +e.getMessage());
			}
		//convert TLVObject into tlv byte array	
		byte [] setSerialNumbersByteArray = null;
		try {
			setSerialNumbersByteArray = setSerialNumbers.toTLVByteArray();
		} catch (ValueNullException | ValueTooBigException e) {
			throw new ErrorExportSerialNumbersFailed("Unable to convert setSerialNumbers into an ASN.1 byte array.\nCause:\t" +e.getMessage());
		}
		
		//if the TLVObject could be converted into a TLV byte array, set the return value
		serialNumbers.setValue(setSerialNumbersByteArray);
		
		return Constant.EXECUTION_OK;
	}

	
//--------------------------------------------------UTILITY FUNCTIONS-----------------------------------------------------------------------
	/** (non-Javadoc)
	 * @see main.java.de.bsi.seapi.SEAPI#getMaxNumberOfClients(main.java.de.bsi.seapi.holdertypes.LongHolder)
	 */
	@Override
	public short getMaxNumberOfClients(LongHolder maxNumberClients)
			throws ErrorGetMaxNumberOfClientsFailed, ErrorSeApiNotInitialized, ErrorSecureElementDisabled {
		//if SE disabled, throw exception
		if(this.securityModule.getSecureElementIsDisabled()) {
			throw new ErrorSecureElementDisabled();
		}
		//if the TSE has not been initialized, throw new ErrorSeApiNotInitialized
		if(!tseIsInitialized) {
			throw new ErrorSeApiNotInitialized();
		}
		//if there is no LongHolder to write the value to:
		if(maxNumberClients == null) {
			throw new ErrorGetMaxNumberOfClientsFailed("REQUIRED parameter was null!\n");
		}
		try {
			maxNumberClients.setValue((long) this.securityModule.getMaxNumberClients());
		} catch (Exception e) {
			throw new ErrorGetMaxNumberOfClientsFailed(e.getMessage(), e);
		}
		return Constant.EXECUTION_OK;
	}

	/** (non-Javadoc)
	 * @see main.java.de.bsi.seapi.SEAPI#getCurrentNumberOfClients(main.java.de.bsi.seapi.holdertypes.LongHolder)
	 */
	@Override
	public short getCurrentNumberOfClients(LongHolder currentNumberClients)
			throws ErrorGetCurrentNumberOfClientsFailed, ErrorSeApiNotInitialized, ErrorSecureElementDisabled {
		//if SE disabled, throw exception
		if(this.securityModule.getSecureElementIsDisabled()) {
			throw new ErrorSecureElementDisabled();
		}
		//if TSE was never initialized, throw exception
		if(!tseIsInitialized) {
			throw new ErrorSeApiNotInitialized();
		}
		//if there is no LongHolder to write the value to:
		if(currentNumberClients == null) {
			throw new ErrorGetCurrentNumberOfClientsFailed("REQUIRED parameter was null!\n");
		}
		try {
			currentNumberClients.setValue((long) securityModule.getNumberOfRegisteredClients());
		} catch(Exception e) {
			throw new ErrorGetCurrentNumberOfClientsFailed(e.getMessage(), e);
		}
		return Constant.EXECUTION_OK;
	}

	
	/*
	 * (non-Javadoc)
	 * @see main.java.de.bsi.seapi.SEAPI#getMaxNumberOfTransactions(main.java.de.bsi.seapi.holdertypes.LongHolder)
	 */
	public short getMaxNumberOfTransactions(LongHolder maxNumberTransactions)
			throws ErrorGetMaxNumberTransactionsFailed, ErrorSeApiNotInitialized, ErrorSecureElementDisabled {
		//if SE disabled, throw exception
		if(this.securityModule.getSecureElementIsDisabled()) {
			throw new ErrorSecureElementDisabled();
		}
		//if the se api is not initialized throw errorSeApiNotinitialized
		if(!tseIsInitialized) {
			throw new ErrorSeApiNotInitialized();
		}
		//if there is no LongHolder to write the value to:
		if(maxNumberTransactions == null) {
			throw new ErrorGetMaxNumberTransactionsFailed("REQUIRED parameter was null!\n");
		}
		//write the maximum number of transactions to the maxNumberTransactions
		try {
			maxNumberTransactions.setValue((long) this.securityModule.getERSSMMaxNumberOpenTransactions());
		} catch(Exception e) {
			throw new ErrorGetMaxNumberTransactionsFailed();
		}
		
		return Constant.EXECUTION_OK;
	}

	
	/**
	 * Uses the {@linkplain SecurityModule#getNumberOfERSSMopenTransaction()} to get the number of currently open transactions, casts this value to long and
	 * sets it in the currentNumberTransactions.
	 * @see {@linkplain main.java.de.bsi.seapi.SEAPI#getCurrentNumberOfTransactions(main.java.de.bsi.seapi.holdertypes.LongHolder)}
	 * @version 1.0
	 */
	public short getCurrentNumberOfTransactions(LongHolder currentNumberTransactions)
			throws ErrorGetCurrentNumberOfTransactionsFailed, ErrorSeApiNotInitialized, ErrorSecureElementDisabled {
		//if SE disabled, throw exception
		if(this.securityModule.getSecureElementIsDisabled()) {
			throw new ErrorSecureElementDisabled();
		}
		//if the SE API is not initialized throw errorSeApiNotInitialized
		if(!tseIsInitialized) {
			throw new ErrorSeApiNotInitialized();
		}
		//if there is no LongHolder to write the value to:
		if(currentNumberTransactions == null) {
			throw new ErrorGetCurrentNumberOfTransactionsFailed("REQUIRED parameter was null!\n");
		}
		
		try {
			currentNumberTransactions.setValue((long) this.securityModule.getNumberOfERSSMopenTransactions());
		} catch(Exception e) {
			throw new ErrorGetCurrentNumberOfTransactionsFailed();
		}
		
		return Constant.EXECUTION_OK;
	}

	
	
	/**
	 * Uses {@linkplain SecurityModule#getUpdateVariants()} to return the supported update variants. What the supported update variants are is currenty 
	 * hard-coded in the SecurityModule class. Should be fetched from a configuration file in later releases.
	 * @version 1.0
	 * @see {@linkplain main.java.de.bsi.seapi.SEAPI#getSupportedTransactionUpdateVariants(main.java.de.bsi.seapi.holdertypes.UpdateVariantsHolder)}
	 */
	public short getSupportedTransactionUpdateVariants(UpdateVariantsHolder supportedUpdateVariants)
			throws ErrorGetSupportedUpdateVariantsFailed, ErrorSeApiNotInitialized, ErrorSecureElementDisabled {
		//if SE disabled, throw exception
		if(this.securityModule.getSecureElementIsDisabled()) {
			throw new ErrorSecureElementDisabled();
		}
		//if SE API not initialized, throw ErrorSeApiNotInitialized 
		if(!tseIsInitialized) {
			throw new ErrorSeApiNotInitialized();
		}
		//if there is no LongHolder to write the value to:
		if(supportedUpdateVariants == null) {
			throw new ErrorGetSupportedUpdateVariantsFailed("REQUIRED parameter was null!\n");
		}
		
		//try to get the supported update variants from the Security Module.
		try {
			supportedUpdateVariants.setValue(this.securityModule.getUpdateVariants().getValue());
		} catch(Exception e) {
			throw new ErrorGetSupportedUpdateVariantsFailed();
		}
		
		return Constant.EXECUTION_OK;
	}

	/* (non-Javadoc)
	 * @see main.java.de.bsi.seapi.SEAPI#deleteStoredData()
	 */
	@Override
	public short deleteStoredData() throws ErrorDeleteStoredDataFailed, ErrorUnexportedStoredData,
			ErrorSeApiNotInitialized, ErrorUserNotAuthorized, ErrorUserNotAuthenticated {
	//1. if the SE API is not initialized an ErrorSeApiNotInitialized shall be thrown
		if(!tseIsInitialized) {
			throw new ErrorSeApiNotInitialized();
		}
		
	//2. the SE API SHALL check if the user has the status authenticated. If not, ErrorUserNotAuthenticated SHALL be thrown.
		if(currentlyLoggedIn==null) {
			throw new ErrorUserNotAuthenticated();
		}
		
	//3. if the user is authenticated the SE API SHALL check if the user is authorized to invoke this function. If he/she is not authorized, an
		//ErrorUserNotAuthorized SHALL be thrown
		if(!(currentlyLoggedIn.getRole().equalsIgnoreCase("Admin"))) {
			throw new ErrorUserNotAuthorized();
		}
	//4. SE API SHALL check, if the storage contains unexported data. If yes, an ErrorUnexportedStoredData SHALL be thrown 
		//and the function has to exit WITHOUT changing anything in the storage
	//4b. if deleting the stored files fails for any other reason, the function SHALL throw an ErrorDeleteStoredDataFailed and exit
		//4a & 4b are delegated down to the Storage class. If the storage throws Exceptions, those are propagated up.
		this.storage.deleteStoredData();
		
		return Constant.EXECUTION_OK;
	}
	
	/* (non-Javadoc)
	 * @see main.java.de.bsi.seapi.SEAPI#getTimeSyncVariant(main.java.de.bsi.seapi.holdertypes.SyncVariantsHolder)
	 */
	@Override
	public short getTimeSyncVariant(SyncVariantsHolder supportedSyncVariant)
			throws ErrorGetTimeSyncVariantFailed, ErrorSeApiNotInitialized, ErrorSecureElementDisabled {
		//if SE disabled, throw exception
		if(this.securityModule.getSecureElementIsDisabled()) {
			throw new ErrorSecureElementDisabled();
		}
		//if SE API not initialized, throw ErrorSeApiNotInitialized 
		if(!tseIsInitialized) {
			throw new ErrorSeApiNotInitialized();
		}
		//if the Holder is null, throw ErrorGetTimeSyncVariantFailed
		if(supportedSyncVariant == null) {
			throw new ErrorGetTimeSyncVariantFailed("REQUIRED parameter was null!\n");
		}
		//try to get the supported time sync variant from the Security Module.
		try {
			supportedSyncVariant.setValue(this.securityModule.getSyncVariants().getValue());
		} catch(Exception e) {
			throw new ErrorGetTimeSyncVariantFailed();
		}
		
		return Constant.EXECUTION_OK;
	}
	
	
//------------------------------------------AUTHENTICATION FUNCTIONS---------------------------------------------------------------------
	//ALL AUTHENTICATION FUNCTIONS SHALL WORK IF THE TSE IS NOT YET INITIALIZED
//if new version of tr -> make necessary changes in creation of syslog systemoperationdata	
	//-> currently, the ERSSpecificModule creates an empty value for the system log message if the user id is not managed by the TSE. This means, the operation data for 
	//that field 0x82 inside SystemOperationData of the Syslog is of length 0x00 and has no value. It is essentially an ASN.1 Null

	/* (non-Javadoc)
	 * @see main.java.de.bsi.seapi.SEAPI#authenticateUser(java.lang.String, byte[], main.java.de.bsi.seapi.holdertypes.AuthenticationResultHolder, main.java.de.bsi.seapi.holdertypes.ShortHolder)
	 */
	@Override
	public short authenticateUser(String userId, byte[] pin, AuthenticationResultHolder authenticationResult,
			ShortHolder remainingRetries) throws ErrorSigningSystemOperationDataFailed, ErrorRetrieveLogMessageFailed,
			ErrorStorageFailure, ErrorSecureElementDisabled, ErrorSignatureCounterOverflow, ErrorParameterMismatch {
	//0. Format checks:
		if((authenticationResult==null) || (remainingRetries==null)) {
			throw new ErrorParameterMismatch("REQUIRED parameter was null!\n");
		}
		
	//1.	set the value of the authenticationResult to "failed"
		authenticationResult.setValue(AuthenticationResult.failed);
		
	//2.	check if the SE is disabled, if yes, throw an Exception	
		if(this.securityModule.getSecureElementIsDisabled()) {
			throw new ErrorSecureElementDisabled();
		}
		
	//3.a: check if the user id is null (or not an asn1 printable string) (log the attempt with a SystemLogMessage but do not store anything about the user, because they do not exist)
		if(!(TLVUtility.isASN1_PrintableString(userId))) {
			//log the attempt but be careful with null values!
			byte[] resultOfAuthenticateUser = null;
				try {
					resultOfAuthenticateUser = this.securityModule.authenticateUser("", Constants.AUTHENTICATEUSER_ROLE_UNKNOWNUSERID, false, false);
				} catch (SigningOperationFailedException e) {
					throw new ErrorSigningSystemOperationDataFailed("SigningOperation failed.\n" ,e);
				} catch (ValueNullException e) {
					throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value null", e);
				} catch (ValueTooBigException e) {
					throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value too big", e);
				}
		//3a.a store the Syslog
			if(resultOfAuthenticateUser == null) {
				throw new ErrorRetrieveLogMessageFailed();
			}
			SystemLogMessage resultingSysLog = (SystemLogMessage) createCompleteLogMessageFromByteArray(resultOfAuthenticateUser, (short) 2);
		//3a.b store the data of the SysLog. If that fails, ErrorStoragefailure SHALL be raised
			try {
				this.storage.storeSystemLog(resultOfAuthenticateUser, resultingSysLog.getLogTime(), resultingSysLog.getSignatureCounter(), resultingSysLog.getOperationType());
			} catch (Exception e) {
				throw new ErrorStorageFailure(e.getMessage());
			}
			//set the authenticationResult to unknown userID and set the number of remaining retries to -1
			authenticationResult.setValue(AuthenticationResult.unknownUserId);
			remainingRetries.setValue((short) -1);
			
			return Constant.AUTHENTICATION_FAILED;
		}//End "if userId == null"
		
	//3.b: check if the user id is not managed by the SE API
		//3.b.i: use a boolean for this, as it greatly improves readability and dealing with the possible IOException from UserlistValues.getInstance()
			boolean userManagedBySeApi = true;
			try {
				userManagedBySeApi = UserlistValues.getInstance().containsKey(userId);
			} catch (IOException e1) {
				userManagedBySeApi = false;
			}

		if(!userManagedBySeApi) {
			//log the attempt but be careful with null values!
			byte[] resultOfAuthenticateUser = null; 			
			try {
				resultOfAuthenticateUser = this.securityModule.authenticateUser(userId, Constants.AUTHENTICATEUSER_ROLE_UNKNOWNUSERID, false, false);
			} catch (SigningOperationFailedException e) {
				throw new ErrorSigningSystemOperationDataFailed("SigningOperation failed.\n" ,e);
			} catch (ValueNullException e) {
				throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value null", e);
			} catch (ValueTooBigException e) {
				throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value too big", e);
			}
			
		//3b.a store the Syslog
			if(resultOfAuthenticateUser == null) {
				throw new ErrorRetrieveLogMessageFailed();
			}
			//try to construct a SystemLogMessage from the resultOfUpdateTime byte array
			SystemLogMessage resultingSysLog = (SystemLogMessage) createCompleteLogMessageFromByteArray(resultOfAuthenticateUser, (short) 2);
		//3b.b store the data of the SysLog. If that fails, ErrorStoragefailure SHALL be raised
			try {
				this.storage.storeSystemLog(resultOfAuthenticateUser, resultingSysLog.getLogTime(), resultingSysLog.getSignatureCounter(), resultingSysLog.getOperationType());
			} catch (Exception e) {
				throw new ErrorStorageFailure(e.getMessage());
			}
			//set the authenticationResult to unknown userID and set the number of remaining retries to -1
			authenticationResult.setValue(AuthenticationResult.unknownUserId);
			remainingRetries.setValue((short) -1);
			
			return Constant.AUTHENTICATION_FAILED;
		}//End "if userIdnot managed by TSE"
		
	//now is the time to load the persisted User:
		User storedUser = null;
		try {
			storedUser = this.persistentStorage.loadUser(userId);
		} catch (LoadingFailedException e) {
			throw new ErrorStorageFailure("Loading the user data corresponding to " +userId +" failed!\n", e);
		}
		//check that storedUser != null:
		if(storedUser == null) {
			throw new ErrorStorageFailure("Loading the user data corresponding to " +userId +" failed!\n");
		}
	//and to save the role of that user, so it can be used later:
		@SuppressWarnings("unused")
		int roleOfUser = Constants.AUTHENTICATEUSER_ROLE_UNKNOWNUSERID;
		if(storedUser.getRole().equalsIgnoreCase("admin")) {
			roleOfUser = Constants.AUTHENTICATEUSER_ROLE_ADMIN;
		}
		if(storedUser.getRole().equalsIgnoreCase("timeAdmin")) {
			roleOfUser = Constants.AUTHENTICATEUSER_ROLE_TIMEADMIN;
		}
		

	//4. check the pinRetryCounter of the user
		//4.a retry counter <=0 -> log failed attempt
		if(storedUser.getRemainingPINRetries()<=0) {
			//4.a.a: set authenticationResult to pinIsBlocked
			authenticationResult.setValue(SEAPI.AuthenticationResult.pinIsBlocked);
			//4.a.b: the value of remaining retires shall be set to the value of the pin retry counter
			remainingRetries.setValue(storedUser.getRemainingPINRetries());
			//4.a.c: the function shall perform the actions defined in 4.7.1.4.1:
				//4.a.c.a: create a byte array to hold the result:
				byte[] resultOfAuthenticateUser = null;
				//4.a.c.b: invoke secure element functionality:
				try {
					resultOfAuthenticateUser = this.securityModule.authenticateUser(userId, roleOfUser, false, true);
				} catch (SigningOperationFailedException e) {
					throw new ErrorSigningSystemOperationDataFailed("SigningOperation failed.\n" ,e);
				} catch (ValueNullException e) {
					throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value null", e);
				} catch (ValueTooBigException e) {
					throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value too big", e);
				}
				//4.a.c.c: retrieve parts of the log message determined by the secure element
				if(resultOfAuthenticateUser==null) {
					throw new ErrorRetrieveLogMessageFailed();
				}
				SystemLogMessage resultingSysLog = (SystemLogMessage) createCompleteLogMessageFromByteArray(resultOfAuthenticateUser, (short) 2);
				//4.a.c.d: store the log message on the normal storage
				try {
					this.storage.storeSystemLog(resultOfAuthenticateUser, resultingSysLog.getLogTime(), resultingSysLog.getSignatureCounter(), resultingSysLog.getOperationType());
				} catch (Exception e) {
					throw new ErrorStorageFailure(e.getMessage());
				}
				//4.a.c.e: return Authentication Failed
				return Constant.AUTHENTICATION_FAILED;
		}
		
		//4.b if the pin retry counter is greater than 0, decrement the counter by one
		short newPinRetryCounter = (short) (storedUser.getRemainingPINRetries() -1);
		
	//5. check pin value
		//5.a: if the pin is not correct:
		if((pin == null) || (!Arrays.areEqual(storedUser.getHashedPIN(), this.securityModule.getCryptoCore().hashByteArray(pin)))) {
			//5.a.a: set the value of remaining retries to the decreased pin retry counter (newPinRetryCounter)
			remainingRetries.setValue(newPinRetryCounter);
			//5.a.b: make sure, that the new, lower retry counter is stored in persistentStorage
			User modifiedUser = new User(storedUser.getUserID(), storedUser.getRole(), storedUser.getHashedPIN(), storedUser.getHashedPUK(), newPinRetryCounter, storedUser.getRemainingPUKRetries());
			try {
				this.persistentStorage.writeModifiedUserdataToStorage(modifiedUser);
			} catch (ModifyingUserFailedException| IllegalArgumentException e) {
				throw new ErrorStorageFailure(e.getMessage(), e);
			}
			//5.a.c: perform chapter 4.7.1.4.1 to log failed authentication attempt
				//5.a.c.a: create a byte array to hold the result:
				byte[] resultOfAuthenticateUser = null;
				//5.a.c.b: invoke secure element functionality:
				try {
					resultOfAuthenticateUser = this.securityModule.authenticateUser(userId, roleOfUser, false, true);
				} catch (SigningOperationFailedException e) {
					throw new ErrorSigningSystemOperationDataFailed("SigningOperation failed.\n" ,e);
				} catch (ValueNullException e) {
					throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value null", e);
				} catch (ValueTooBigException e) {
					throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value too big", e);
				}
				//5.a.c.c: retrieve parts of the log message determined by the secure element
				if(resultOfAuthenticateUser==null) {
					throw new ErrorRetrieveLogMessageFailed();
				}
				SystemLogMessage resultingSysLog = (SystemLogMessage) createCompleteLogMessageFromByteArray(resultOfAuthenticateUser, (short) 2);
				//5.a.c.d: store the log message on the normal storage
				try {
					this.storage.storeSystemLog(resultOfAuthenticateUser, resultingSysLog.getLogTime(), resultingSysLog.getSignatureCounter(), resultingSysLog.getOperationType());
				} catch (Exception e) {
					throw new ErrorStorageFailure(e.getMessage());
				}
				//5.a.c.e: return Authentication Failed
				return Constant.AUTHENTICATION_FAILED;
		}
		
		//5.b: if the pin is correct:
		if(Arrays.areEqual(storedUser.getHashedPIN(), this.securityModule.getCryptoCore().hashByteArray(pin))) {
			//5.b.b: set the value of the pinRetryCounter to maximum value 
			newPinRetryCounter = Constants.MAX_RETRIES;
				//create a new User from this to save to storage:
			User modifiedUser = new User(storedUser.getUserID(), storedUser.getRole(), storedUser.getHashedPIN(), storedUser.getHashedPUK(), newPinRetryCounter, storedUser.getRemainingPUKRetries());
			try {
				this.persistentStorage.writeModifiedUserdataToStorage(modifiedUser);
			} catch (ModifyingUserFailedException| IllegalArgumentException e) {
				throw new ErrorStorageFailure(e.getMessage(), e);
			}
			//5.b.a set the status of userId to authenticated:
			this.currentlyLoggedIn = modifiedUser;
			//5.b.c: set the value of remainingRetries to that value
			remainingRetries.setValue(modifiedUser.getRemainingPINRetries());
			//5.b.d: set the value of authenticationResult to "ok"
			authenticationResult.setValue(SEAPI.AuthenticationResult.ok);
		}
		
		//6. invoke functionality of Secure Element to determine log message parts for successful authentication attempt:
				//(if that fails, throw ErrorSigningSystemOperationDataFailed
			//6.a: create the byte array holding the log message:
		byte[] resultOfAuthenticateUser = null;
			//6.b: invoke securityModule functionality (remember: use role from before point 4., because it influences how the Syslog is created!)
		try {
			resultOfAuthenticateUser = this.securityModule.authenticateUser(userId, roleOfUser, true, true);
		} catch (SigningOperationFailedException e) {
			throw new ErrorSigningSystemOperationDataFailed();
		} catch (ValueNullException e) {
			throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value null", e);
		} catch (ValueTooBigException e) {
			throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value too big", e);
		}
			//6.d:check if reultOfAuthenticateuser == null, of yes, throw Exception
			if(resultOfAuthenticateUser==null) {
				throw new ErrorRetrieveLogMessageFailed();
			}
		
		//7. retrieve parts of the log message from SecureElement. 
				//(if that fails, throw ErrorRetrieveLogmessageFailed)
			SystemLogMessage resultingSysLog = (SystemLogMessage) createCompleteLogMessageFromByteArray(resultOfAuthenticateUser, (short) 2);
		//8. store the log message on the normal storage
				//(if that fails, throw errorStorageFailure)
			try {
				this.storage.storeSystemLog(resultOfAuthenticateUser, resultingSysLog.getLogTime(), resultingSysLog.getSignatureCounter(), resultingSysLog.getOperationType());
			} catch (Exception e) {
				throw new ErrorStorageFailure(e.getMessage());
			}
	
		//9. if everything went okay up to this point, return Execution_Ok
		return Constant.EXECUTION_OK;
	}

	/* (non-Javadoc)
	 * @see main.java.de.bsi.seapi.SEAPI#logOut(java.lang.String)
	 */
	@Override
	public short logOut(String userId)
			throws ErrorUserIdNotManaged, ErrorSigningSystemOperationDataFailed, ErrorUserIdNotAuthenticated,
			ErrorRetrieveLogMessageFailed, ErrorStorageFailure, ErrorSecureElementDisabled, ErrorSignatureCounterOverflow {
		//0. check if the Secure element has been disabled
		if(this.securityModule.getSecureElementIsDisabled()) {
			throw new ErrorSecureElementDisabled();
		}
		//1. check if userId is managed by SE API. If not (or if unable to determine because of IOException), raise ErrorUserIdNotManaged and exit:
		try {
			if( (!TLVUtility.isASN1_PrintableString(userId)) || (!UserlistValues.getInstance().containsKey(userId)) ){
				throw new ErrorUserIdNotManaged();
			}
		} catch (IOException e1) {
			throw new ErrorUserIdNotManaged("Caused by UserlistValues.getInstance().\n"+e1.getMessage(), e1 );
		}
		//2. check, if the userId has status "authenticated". If not, raise ErrorUserIdNotAuthenticated and exit:
		if((currentlyLoggedIn == null) || !(currentlyLoggedIn.getUserID().equalsIgnoreCase(userId))) {
			throw new ErrorUserIdNotAuthenticated();
		}
		//3. set the status of the user to "not authenticated"
		currentlyLoggedIn = null;
		//4. invoke secure element functionality to create the log message. If that fails, raise ErrorSigningSystemOperationDataFailed
		byte[] resultOfLogOut = null;
		try {
			resultOfLogOut = this.securityModule.logOutUser(userId);
		} catch (SigningOperationFailedException e) {
			throw new ErrorSigningSystemOperationDataFailed();
		} catch (ValueNullException e) {
			throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value null", e);
		} catch (ValueTooBigException e) {
			throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value too big", e);
		}
		//5. retrieve log message from secure element. If that fails, raise ErrorRetrieveLogMessageFailed
		SystemLogMessage resultingSysLog = (SystemLogMessage) createCompleteLogMessageFromByteArray(resultOfLogOut, (short) 2);
		//6. store the system log in normal storage. If that fails, raise ErrorStorageFailure
		try {
			this.storage.storeSystemLog(resultOfLogOut, resultingSysLog.getLogTime(), resultingSysLog.getSignatureCounter(), resultingSysLog.getOperationType());
		} catch (Exception e) {
			throw new ErrorStorageFailure(e.getMessage());
		}
		//7. If everything went okay up to this point, return Execution_Ok
		return Constant.EXECUTION_OK;
	}

	/* (non-Javadoc)
	 * @see main.java.de.bsi.seapi.SEAPI#unblockUser(java.lang.String, byte[], byte[], main.java.de.bsi.seapi.holdertypes.UnblockResultHolder)
	 */
	@SuppressWarnings("unused")
	@Override
	public short unblockUser(String userId, byte[] puk, byte[] newPin, UnblockResultHolder unblockResult)
			throws ErrorSigningSystemOperationDataFailed, ErrorRetrieveLogMessageFailed, ErrorStorageFailure,
			ErrorSecureElementDisabled, ErrorSignatureCounterOverflow, ErrorParameterMismatch {
		//0. format check:
		if(unblockResult==null) {
			throw new ErrorParameterMismatch("REQUIRED parameter was null!\n");
		}
		//1. set value of unblockResult to failed
		unblockResult.setValue(SEAPI.UnblockResult.failed);
		
		//2. check if the Secure element has been disabled
		if(this.securityModule.getSecureElementIsDisabled()) {
			throw new ErrorSecureElementDisabled();
		}
		
		//3. check if the userId is managed by the SE API
			//3.a: save if userId is null and if userId is managed by the TSE
			boolean userIdIsASN1String = TLVUtility.isASN1_PrintableString(userId);
			boolean userManagedBySeApi = true;
				//attention: if the userId is null, DO NOT attempt to use UserlistValues.getInstance().containsKey(userId), this would cause a NullPointerException
				//Solution surround with check for userId is an ASN.1 PrintableString
				if(userIdIsASN1String) {
					try {
						userManagedBySeApi = UserlistValues.getInstance().containsKey(userId);
					} catch (IOException e2) {
						userManagedBySeApi = false;
					}
				}
			//3.b: check if that userId is not managed or null
			if((!userIdIsASN1String) || (!userManagedBySeApi)) {
				//3.b.a: set value of unblockResult to "unknownUserId"
				unblockResult.setValue(SEAPI.UnblockResult.unknownUserId);
				//3.b.b: create the log message parts
				byte[] resultOfUnblockUser = null;
					//3.b.b.a: if userId == null or otherwise not printable ASN1 String:
					if(!userIdIsASN1String) {
						try {
							resultOfUnblockUser = this.securityModule.unblockUser("", Constants.UNBLOCKUSER_UNBLOCKRESULT_UNKNOWNUSERID);
						} catch (SigningOperationFailedException e) {
							throw new ErrorSigningSystemOperationDataFailed();
						} catch (ValueNullException e) {
							throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value null", e);
						} catch (ValueTooBigException e) {
							throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value too big", e);
						}
					}
					//3.b.b.b: if userId == printable ASN1 String:
					else {
						try {
							resultOfUnblockUser = this.securityModule.unblockUser(userId, Constants.UNBLOCKUSER_UNBLOCKRESULT_UNKNOWNUSERID);
						} catch (SigningOperationFailedException e) {
							throw new ErrorSigningSystemOperationDataFailed();
						} catch (ValueNullException e) {
							throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value null", e);
						} catch (ValueTooBigException e) {
							throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value too big", e);
						}
					}
				//3.b.c: retrieve log message parts from secure element. If that fails, raise ErrorRetrieveLogMessageFailed:
				SystemLogMessage resultingSysLog = (SystemLogMessage) createCompleteLogMessageFromByteArray(resultOfUnblockUser, (short) 2);
				//3.b.d:. store the system log in normal storage. If that fails, raise ErrorStorageFailure
				try {
					this.storage.storeSystemLog(resultOfUnblockUser, resultingSysLog.getLogTime(), resultingSysLog.getSignatureCounter(), resultingSysLog.getOperationType());
				} catch (Exception e) {
					throw new ErrorStorageFailure(e.getMessage());
				}
				//3.b.e: return unblock failed
				return Constant.UNBLOCK_FAILED;
			}//END "if userId == null or not managed by TSE"
			
		//now is the time to load the persisted User:
		User storedUser = null;
		try {
			storedUser = this.persistentStorage.loadUser(userId);
		} catch (LoadingFailedException e) {
			throw new ErrorStorageFailure("Loading the user data corresponding to " +userId +" failed!\n", e);
		}
		//check that storedUser != null:
		if(storedUser == null) {
			throw new ErrorStorageFailure("Loading the user data corresponding to " +userId +" failed!\n");
		}
		//4. perform countermeasures against password guessing. Approach: just use retry counters, like the one for the PIN
		//check pukRetryCounter of the user
			//4.a: PUK retry counter <= 0 log the failed attempt
			if(storedUser.getRemainingPUKRetries() <= 0) {
				//4.a.a: set unblockResult to failed
				unblockResult.setValue(SEAPI.UnblockResult.failed);
				//4.a.b: perform actions in 4.7.3.4.1:
				//4.a.b.a: invoke secure element
				byte[] resultOfUnblockUser = null;
				try {
					resultOfUnblockUser = this.securityModule.unblockUser(userId, Constants.UNBLOCKUSER_UNBLOCKRESULT_FAILED);
				} catch (SigningOperationFailedException e) {
					throw new ErrorSigningSystemOperationDataFailed();
				} catch (ValueNullException e) {
					throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value null", e);
				} catch (ValueTooBigException e) {
					throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value too big", e);
				}
				//4.a.b.b: retrieve log message
				SystemLogMessage resultingSysLog = (SystemLogMessage) createCompleteLogMessageFromByteArray(resultOfUnblockUser, (short) 2);
				//4.a.b.c: store the log message on the normal storage
				try {
					this.storage.storeSystemLog(resultOfUnblockUser, resultingSysLog.getLogTime(), resultingSysLog.getSignatureCounter(), resultingSysLog.getOperationType());
				} catch (Exception e) {
					throw new ErrorStorageFailure(e.getMessage());
				}
				//4.a.b.d: return Authentication Failed
				return Constant.UNBLOCK_FAILED;
			}
			//4.b if the PUK retry counter is greater than 0, decrement the counter by one
			short newPukRetryCounter = (short) (storedUser.getRemainingPUKRetries() -1);
			
		//5. check the PUK value
			//5.a: PUK not correct:
			if((puk == null) || (!Arrays.areEqual(storedUser.getHashedPUK(), this.securityModule.getCryptoCore().hashByteArray(puk)))) {
				//5.a.a: make sure, the new, lower PUK retry counter is stored in persistentStorage:
				User modifiedUser = new User(storedUser.getUserID(), storedUser.getRole(), storedUser.getHashedPIN(), storedUser.getHashedPUK(), storedUser.getRemainingPINRetries(), newPukRetryCounter);
				try {
					this.persistentStorage.writeModifiedUserdataToStorage(modifiedUser);
				} catch (ModifyingUserFailedException|IllegalArgumentException e) {
					throw new ErrorStorageFailure(e.getMessage(), e);
				}
				//5.a.b: perform actions in 4.7.3.4.1:
				//5.a.b.a: invoke secure element
				byte[] resultOfUnblockUser = null;
				try {
					resultOfUnblockUser = this.securityModule.unblockUser(userId, Constants.UNBLOCKUSER_UNBLOCKRESULT_FAILED);
				} catch (SigningOperationFailedException e) {
					throw new ErrorSigningSystemOperationDataFailed();
				} catch (ValueNullException e) {
					throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value null", e);
				} catch (ValueTooBigException e) {
					throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value too big", e);
				}
				//5.a.b.b: retrieve log message
				SystemLogMessage resultingSysLog = (SystemLogMessage) createCompleteLogMessageFromByteArray(resultOfUnblockUser, (short) 2);
				//5.a.b.c: store the log message on the normal storage
				try {
					this.storage.storeSystemLog(resultOfUnblockUser, resultingSysLog.getLogTime(), resultingSysLog.getSignatureCounter(), resultingSysLog.getOperationType());
				} catch (Exception e) {
					throw new ErrorStorageFailure(e.getMessage());
				}
				//5.a.b.d: return Authentication Failed
				return Constant.UNBLOCK_FAILED;
			}//END "if PUK not correct"
		
		//6. if the PUK is correct:
		if(Arrays.areEqual(storedUser.getHashedPUK(), this.securityModule.getCryptoCore().hashByteArray(puk))) {
			//6.a: substitute current PIN of the user for the new PIN. If that fails, set unblockResult to Error and log the failed attempt
				//6.a.a: check if the pin is null. If thats the case, log the failed attempt and exit the function.
				if(newPin == null) {
					//set unblock result to error
					unblockResult.setValue(SEAPI.UnblockResult.error);
					//invoke secure element fuctionality
					byte[] resultOfUnblockUser = null;
					try {
						resultOfUnblockUser = this.securityModule.unblockUser(userId, Constants.UNBLOCKUSER_UNBLOCKRESULT_ERROR);
					} catch (SigningOperationFailedException e1) {
						throw new ErrorSigningSystemOperationDataFailed();
					} catch (ValueNullException e) {
						throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value null", e);
					} catch (ValueTooBigException e) {
						throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value too big", e);
					}
					//retrieve the log message:
					SystemLogMessage resultingSysLog = (SystemLogMessage) createCompleteLogMessageFromByteArray(resultOfUnblockUser, (short) 2);
					//store syslog in storage
					try {
						this.storage.storeSystemLog(resultOfUnblockUser, resultingSysLog.getLogTime(), resultingSysLog.getSignatureCounter(), resultingSysLog.getOperationType());
					} catch (Exception e1) {
						throw new ErrorStorageFailure(e1.getMessage());
					}
					return Constant.UNBLOCK_FAILED;		
				}
			//6.b: set the retry counter of the PIN to the maximum possible value. (Do the same with the PUK retry counter). If that fails, set unblockResult to Error and log the failed attempt
			byte[] newHashedPin = this.securityModule.getCryptoCore().hashByteArray(newPin);
			User modifiedUser = new User(userId, storedUser.getRole(), newHashedPin, storedUser.getHashedPUK(), Constants.MAX_RETRIES, Constants.MAX_RETRIES);
			try {
				this.persistentStorage.writeModifiedUserdataToStorage(modifiedUser);
			} catch (ModifyingUserFailedException|IllegalArgumentException e) {
				//6.b.a: set unblockResult
				unblockResult.setValue(SEAPI.UnblockResult.error);
				//6.b.b: invoke secure element
				byte[] resultOfUnblockUser = null;
				try {
					resultOfUnblockUser = this.securityModule.unblockUser(userId, Constants.UNBLOCKUSER_UNBLOCKRESULT_ERROR);
				} catch (SigningOperationFailedException e1) {
					throw new ErrorSigningSystemOperationDataFailed();
				} catch (ValueNullException e1) {
					throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value null", e);
				} catch (ValueTooBigException e1) {
					throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value too big", e);
				}
				//6.b.c: retrieve the log message:
				SystemLogMessage resultingSysLog = (SystemLogMessage) createCompleteLogMessageFromByteArray(resultOfUnblockUser, (short) 2);
				//6.b.d: store syslog in storage
				try {
					this.storage.storeSystemLog(resultOfUnblockUser, resultingSysLog.getLogTime(), resultingSysLog.getSignatureCounter(), resultingSysLog.getOperationType());
				} catch (Exception e1) {
					throw new ErrorStorageFailure(e1.getMessage());
				}
				return Constant.UNBLOCK_FAILED;			
			}
		}
		//7. set value of unblockResult to ok
		unblockResult.setValue(SEAPI.UnblockResult.ok);
		//8. invoke secure element to create a logmessage:
		byte[] resultOfUnblockUser = null;
		try {
			resultOfUnblockUser = this.securityModule.unblockUser(userId, Constants.UNBLOCKUSER_UNBLOCKRESULT_OK);
		} catch (SigningOperationFailedException e) {
			throw new ErrorSigningSystemOperationDataFailed();
		} catch (ValueNullException e) {
			throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value null", e);
		} catch (ValueTooBigException e) {
			throw new ErrorSigningSystemOperationDataFailed("Creating system log failed. Cause: value too big", e);
		}
		//9. retrieve the log message from secure element
		SystemLogMessage resultingSysLog = (SystemLogMessage) createCompleteLogMessageFromByteArray(resultOfUnblockUser, (short) 2);
		//10. store the data on normal storage
		try {
			this.storage.storeSystemLog(resultOfUnblockUser, resultingSysLog.getLogTime(), resultingSysLog.getSignatureCounter(), resultingSysLog.getOperationType());
		} catch (Exception e) {
			throw new ErrorStorageFailure(e.getMessage());
		}
		
		//11. return execution ok
		return Constant.EXECUTION_OK;
	}


}
