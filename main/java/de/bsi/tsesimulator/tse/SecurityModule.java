/**
 * 
 */
package main.java.de.bsi.tsesimulator.tse;

import java.io.IOException;
import java.security.SignatureException;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import main.java.de.bsi.seapi.SEAPI;
import main.java.de.bsi.seapi.exceptions.ErrorFinishTransactionFailed;
import main.java.de.bsi.seapi.exceptions.ErrorInvalidTime;
import main.java.de.bsi.seapi.exceptions.ErrorNoLogMessage;
import main.java.de.bsi.seapi.exceptions.ErrorNoTransaction;
import main.java.de.bsi.seapi.exceptions.ErrorSigningSystemOperationDataFailed;
import main.java.de.bsi.seapi.exceptions.ErrorStartTransactionFailed;
import main.java.de.bsi.seapi.exceptions.ErrorUpdateTimeFailed;
import main.java.de.bsi.seapi.exceptions.ErrorUpdateTransactionFailed;
import main.java.de.bsi.seapi.holdertypes.SyncVariantsHolder;
import main.java.de.bsi.seapi.holdertypes.UpdateVariantsHolder;
import main.java.de.bsi.tsesimulator.constants.ASN1Constants;
import main.java.de.bsi.tsesimulator.constants.ConfigConstants;
import main.java.de.bsi.tsesimulator.constants.Constants;
import main.java.de.bsi.tsesimulator.exceptions.ClientIdAlreadyRegisteredException;
import main.java.de.bsi.tsesimulator.exceptions.ClientIdNotRegisteredException;
import main.java.de.bsi.tsesimulator.exceptions.ErrorSignatureCounterOverflow;
import main.java.de.bsi.tsesimulator.exceptions.ErrorTransactionCounterOverflow;
import main.java.de.bsi.tsesimulator.exceptions.SigningOperationFailedException;
import main.java.de.bsi.tsesimulator.exceptions.TooManyClientsException;
import main.java.de.bsi.tsesimulator.exceptions.TooManyOpenTransactionsException;
import main.java.de.bsi.tsesimulator.exceptions.ValueNullException;
import main.java.de.bsi.tsesimulator.exceptions.ValueTooBigException;
import main.java.de.bsi.tsesimulator.msg.SystemLogMessage;
import main.java.de.bsi.tsesimulator.msg.TransactionLogMessage;
import main.java.de.bsi.tsesimulator.preferences.PropertyValues;
import main.java.de.bsi.tsesimulator.tlv.TLVObject;

/**
 * Class represents the secure element (or security module as it is called in TR-03153) present in the TSE.<br>
 * It consists of a {@linkplain ERSSpecificModule} and a {@linkplain CryptoCore} which are both used to create {@linkplain LogMessage}s. The SecurityModule stores the values of supported 
 * update variants, supported time synchronization variants and the status of itself, mostly, whether it has been disabled or not.
 * In Addition to that, it buffers the last log message it created in internal volatile storage so that {@linkplain SEAPI#readLogMessage(main.java.de.bsi.seapi.holdertypes.ByteArrayHolder)} 
 * can access that last log message.
 * @author dpottkaemper
 * @since 1.0
 */
public class SecurityModule {
	private boolean secureElementIsDisabled = false;		//should track whether the secure element is disabled or not
	private UpdateVariantsHolder supportedUpdates = new UpdateVariantsHolder();		//stores the possible update variants. Currently, ONLY signed is supported.
	private SyncVariantsHolder supportedTimeSync = new SyncVariantsHolder();	//stores the type of time sync mechanism the SecurityModule supports. 
	
	
	private ERSSpecificModule erssm = null;
	private CryptoCore cryptoCore = null;	
	private byte[] serialNumber = new byte[32];		//serial number is calculated with a SHA-256 bit function. -> serial number is 256 bit long
	
	private byte[] latestLogMessage = null;	//stores reference to last log message created. Only necessary, because "readLogMessage" exists in SE API.
	
	private HashSet<String> clientsUsingTSE;	//stores the clientIds of clients that are currently registered for using the TSE 
	private int maxNumberClients;			//stores the maximum number of clients that are allowed to use the TSE at any point
	
	/**
	 * Default constructor for the Security Module. 
	 * Constructs a CryptoCore and a ERSSpecificModule, then uses the CryptoCore to calculate the serialNumber.<br>
	 * 
	 * <b>Changes in version 1.4:</b><br>
	 * The value of supportedUpdates is now dependent on the value of <i>updateTransactionMode</i> in config.properties.
	 * Please check the annotations in that file to find out, whether aggregated updates are yet implemented.<br>
	 * Since version 1.4, this constructor also sets the value of supportedTimeSync. Please read the configuration file and the handbook for more info
	 * about the time sync method.<br>
	 * 
	 * <b>Changes in version 1.5:</b><br>
	 * Now stores the maximum number of clients that may use the TSE simultaneously internally. If reading that value from the configuration file fails, the maximum number 
	 * of clients is set to the value of {@linkplain Constants#MAX_NUMBER_OF_CLIENTS}.
	 * Creates a HashSet to store the clientIds that are registered.
	 * 
	 * @throws IOException is thrown by the CryptoCore constructor if it fails at loading the keys for its algorithm objects OR <br> 
	 * is thrown by {@linkplain PropertyValues#getInstance()} when no path to the resources directory was set via {@linkplain PropertyValues#setPathToResourceDirectory(String)}
	 * @since 1.0
	 * @version 1.5
	 */
	public SecurityModule() throws IOException {
		this.cryptoCore = new CryptoCore();
		this.serialNumber = this.cryptoCore.getSerialNumber();
		this.erssm = new ERSSpecificModule(cryptoCore, serialNumber);
		
		//check config.properties file for transaction mode
		if(PropertyValues.getInstance().getValue(ConfigConstants.CFG_TAG_UPDATE_TRANSACTION_MODE).equalsIgnoreCase(ConfigConstants.CFG_ENTRY_UPDATE_TRANSACTION_MODE_SIGNED)) {
			this.supportedUpdates.setValue(SEAPI.UpdateVariants.signedUpdate);
		}
		else if(PropertyValues.getInstance().getValue(ConfigConstants.CFG_TAG_UPDATE_TRANSACTION_MODE).equalsIgnoreCase(ConfigConstants.CFG_ENTRY_UPDATE_TRANSACTION_MODE_UNSIGNED)) {
			this.supportedUpdates.setValue(SEAPI.UpdateVariants.unsignedUpdate);
		}
		
		//check config.properties file for TimeSync mode
		String timeSyncString = PropertyValues.getInstance().getValue(ConfigConstants.CFG_TAG_TIME_FORMAT);
		if(timeSyncString.equalsIgnoreCase(ConfigConstants.CFG_ENTRY_TIME_FORMAT_UNIXT)) {
			this.supportedTimeSync.setValue(SEAPI.SyncVariants.unixTime);
		}
		else if(timeSyncString.equalsIgnoreCase(ConfigConstants.CFG_ENTRY_TIME_FORMAT_GENT)) {
			this.supportedTimeSync.setValue(SEAPI.SyncVariants.generalizedTime);
		}
		else if(timeSyncString.equalsIgnoreCase(ConfigConstants.CFG_ENTRY_TIME_FORMAT_UTC)) {
			this.supportedTimeSync.setValue(SEAPI.SyncVariants.utcTime);
		}
		
		//create the clientsUsingTSE hash set with the capacity stored in config.properties (or Constants)
		//try to read from the config file, if that fails, resort to default
		try {
			maxNumberClients = Integer.parseInt(PropertyValues.getInstance().getValue(ConfigConstants.CFG_TAG_MAX_NUMBER_CLIENTS));
		} catch(Exception e) {
			if(e instanceof IOException){
				throw new IOException(e.getMessage(), e);
			}
			maxNumberClients = Constants.DEFAULT_MAX_NUMBER_OF_CLIENTS;
		}
		if(maxNumberClients < 1) {
			maxNumberClients = Constants.DEFAULT_MAX_NUMBER_OF_CLIENTS;
		}
		//create the HahSet storing the registered clients 
		clientsUsingTSE = new HashSet<String>();
		
		//set the "isDisabled" state to false.
		this.secureElementIsDisabled = false;
	}
	
	/**
	 * The constructor that shall be used when a persistence file was loaded. Is called by the {@linkplain TSEController}.<br>
	 * 
	 * Constructs a CryptoCore and a ERSSpecificModule with the specified time, the specified signature counter and the specified transaction number.
	 * Sets the enabled state of the security module as well.<br>
	 * 
	 * <b>Changes in version 1.4:</b><br>
	 * The value of supportedUpdates is now dependent on the value of <i>updateTransactionMode</i> in config.properties.
	 * Please check the annotations in that file to find out, whether aggregated updates are yet implemented.<br>
	 * Since version 1.4, this constructor also sets the value of supportedTimeSync. Please read the configuration file and the handbook for more info
	 * about the time sync method.<br>
	 * 
	 * <b>Changes in version 1.5:</b><br>
	 * Now stores the maximum number of clients that may use the TSE simultaneously internally. If reading that value from the configuration file fails, the maximum number 
	 * of clients is set to the value of {@linkplain Constants#DEFAULT_MAX_NUMBER_OF_CLIENTS}.
	 * Creates a HashSet to store the clientIds that are registered.
	 * 
	 * @param isDisabledLoaded the loaded status of the SE. true if it has been disabled.
	 * @param signatureCounterLoaded the loaded signature counter.
	 * @param transactionNumberLoaded the loaded transaction number.
	 * @param clockTimeUnixLoaded the loaded time in Unix time.
	 * @throws IOException is thrown by the CryptoCore constructor if it fails at loading the keys for its algorithm objects OR <br> 
	 * is thrown by {@linkplain PropertyValues#getInstance()} when no path to the resources directory was set via {@linkplain PropertyValues#setPathToResourceDirectory(String)}
	 * @since 1.0
	 * @version 1.5
	 */
	public SecurityModule(boolean isDisabledLoaded, long signatureCounterLoaded, long transactionNumberLoaded, long clockTimeUnixLoaded) throws IOException {
		//assign the loaded disabled/enabled status 
		this.secureElementIsDisabled = isDisabledLoaded;
		//check config.properties file for transaction mode
		if(PropertyValues.getInstance().getValue(ConfigConstants.CFG_TAG_UPDATE_TRANSACTION_MODE).equalsIgnoreCase(ConfigConstants.CFG_ENTRY_UPDATE_TRANSACTION_MODE_SIGNED)) {
			this.supportedUpdates.setValue(SEAPI.UpdateVariants.signedUpdate);
		}
		else if(PropertyValues.getInstance().getValue(ConfigConstants.CFG_TAG_UPDATE_TRANSACTION_MODE).equalsIgnoreCase(ConfigConstants.CFG_ENTRY_UPDATE_TRANSACTION_MODE_UNSIGNED)) {
			this.supportedUpdates.setValue(SEAPI.UpdateVariants.unsignedUpdate);
		}
		
		//check config.properties file for TimeSync mode
		String timeSyncString = PropertyValues.getInstance().getValue(ConfigConstants.CFG_TAG_TIME_FORMAT);
		if(timeSyncString.equalsIgnoreCase(ConfigConstants.CFG_ENTRY_TIME_FORMAT_UNIXT)) {
			this.supportedTimeSync.setValue(SEAPI.SyncVariants.unixTime);
		}
		else if(timeSyncString.equalsIgnoreCase(ConfigConstants.CFG_ENTRY_TIME_FORMAT_GENT)) {
			this.supportedTimeSync.setValue(SEAPI.SyncVariants.generalizedTime);
		}
		else if(timeSyncString.equalsIgnoreCase(ConfigConstants.CFG_ENTRY_TIME_FORMAT_UTC)) {
			this.supportedTimeSync.setValue(SEAPI.SyncVariants.utcTime);
		}
		
		//create the clientsUsingTSE hash set with the capacity stored in config.properties (or Constants)
		//try to read from the config file, if that fails, resort to default
		try {
			maxNumberClients = Integer.parseInt(PropertyValues.getInstance().getValue(ConfigConstants.CFG_TAG_MAX_NUMBER_CLIENTS));
		} catch(Exception e) {
			if(e instanceof IOException){
				throw new IOException(e.getMessage(), e);
			}
			maxNumberClients = Constants.DEFAULT_MAX_NUMBER_OF_CLIENTS;
		}
		if(maxNumberClients < 1) {
			maxNumberClients = Constants.DEFAULT_MAX_NUMBER_OF_CLIENTS;
		}
		//create the HashSet storing the registered clients 
		clientsUsingTSE = new HashSet<String>();
		
		//create the CryptoCore with the loaded values
		this.cryptoCore = new CryptoCore(signatureCounterLoaded, clockTimeUnixLoaded);
		//get the serial number from the newly created CryptoCore
		this.serialNumber = this.cryptoCore.getSerialNumber();
		
		//with the CryptoCore ,the serial number and the loaded transaction number create the ERSSpecificModule
		this.erssm = new ERSSpecificModule(cryptoCore, serialNumber, transactionNumberLoaded);
	}
	
//------------------------------------------GETTER & SETTER METHODS-------------------------------------------------------------------------------------
	boolean getSecureElementIsDisabled() {
		return this.secureElementIsDisabled;
	}
	
	public UpdateVariantsHolder getUpdateVariants() {
		return this.supportedUpdates;
	}
	
	public SyncVariantsHolder getSyncVariants() {
		return this.supportedTimeSync;
	}
	
	/**
	 * Separate method to disable the secure element as the TR-03151 implicates in the description of the disableSecureElement function.
	 * @return true, if this secure element has been disabled.
	 */
	boolean setDisabled() {
		secureElementIsDisabled = true;
		return secureElementIsDisabled;
	}
	
	/**
	 * fetches the internally stored number of clients that are the maximum for clients using the TSE simulator simultaneously.
	 * @return maximum number of clients that can use the SecureElement at the same time.
	 * @since 1.0
	 * @version 1.5
	 */
	public int getMaxNumberClients() {
		return maxNumberClients;
	}
	
	/**
	 * Calls {@linkplain ERSSpecificModule#getMaxNumberOpenTransactions()} to determine the maximum number of transactions that may be opened simultaneously. 
	 * It is used by {@linkplain TSEController#getMaxNumberOfTransactions(main.java.de.bsi.seapi.holdertypes.LongHolder)}.
	 * @return the maximum number of transactions fetched from the {@linkplain ERSSpecificModule}
	 */
	public int getERSSMMaxNumberOpenTransactions() {
		return erssm.getMaxNumberOpenTransactions();
	}
	
	/**
	 * Fetches the stored serial number.
	 * @return a byte array with the serial number.
	 */
	public byte[] getSerialNumber() {
		return this.serialNumber;
	}
	
	/**
	 * Fetches the CryptoCore used by this SecurityModule
	 * @return the crypto core
	 */
	public CryptoCore getCryptoCore() {
		return this.cryptoCore;
	}
	
	/**
	 * Used by {@linkplain TSEController} to obtain the last log message created for {@linkplain TSEController#readLogMessage(main.java.de.bsi.seapi.holdertypes.ByteArrayHolder)}. 
	 * Be advised that in the case of a storage failure, the last log message created might not be found on persistent storage but through the invocation of 
	 * this method.
	 * @return the last log message that was created as a byte array.
	 * @throws ErrorNoLogMessage if there is no log message to return
	 */
	public byte[] getLatestLogMessage() throws ErrorNoLogMessage {
		if(latestLogMessage == null) {
			throw new ErrorNoLogMessage();
		}
		return this.latestLogMessage;
	}
	
	/**
	 * Used by {@linkplain TSEController} to get the number of clients that are currently registered to use the TSE. 
	 * This number is used to determine the number of clients for {@linkplain TSEController#getCurrentNumberOfClients(main.java.de.bsi.seapi.holdertypes.LongHolder)}.
	 * 
	 * @see {@linkplain TSEController#registerClient(String)}, {@linkplain TSEController#deregisterClient(String)}
	 * @return the number of currently registered clients
	 */
	public int getNumberOfRegisteredClients() {
		return clientsUsingTSE.size();
	}
	
	/**
	 * Calls the ERSSpecificModule to get the number of currently open transactions.
	 * @return the value of {@linkplain ERSSpecificModule#getNumberOfOpenTransactions()} 
	 */
	public int getNumberOfERSSMopenTransactions() {
		return this.erssm.getNumberOfOpenTransactions();
	}
	
	/**
	 * used to get the transaction counter from the ERSSpecificModule. Primarily used for persisting that counter.
	 * @return the transaction counter of the ERSSpecificModule.
	 */
	long getCurrentTransactionCounter() {
		return this.erssm.getCurrentTransactioncounter();
	}
	
	/**
	 * Used to get the current time of the clock in the CryptoCore as Unix time.
	 * @return the current time as Unix time.
	 */
	long getCurrentTimeFromCryptoCore() {
		return this.cryptoCore.getTimeAsUnixTime();
	}
	
//----------------------------------------------------TRANSACTION METHODS------(INPUT FUNCTIONS)-------------------------------------------------------------
	
	/**
	 * Calls the startTransaction method in the ERSSpecificModule class and wraps the result with the SEQUENCE wrapper.
	 * @param clientID  - represents the ID of the application that has invoked the function.
	 * @param processData - the process data that has to be logged as an octet string
	 * @param processType - represents the type of the transaction as defined by the application. Is OPTIONAL according to TR-03151 but MUST requirement
	 * according to TR-03153..
	 * @param additionalData - OPTIONAL. currently reserved for future use.
	 * @return a TransactionLogMessage encoded as an ASN1 encoded byte array with the SEQUENCE wrapper attached to it.
	 * @throws ValueNullException if the transaction log message with the SEQUENCE wrapper could not be converted to a TLV byte array due to a 
	 * required value being absent.
	 * @throws ValueTooBigException if the transaction log message with the SEQUENCE wrapper could not be converted to a TLV byte array due to 
	 * a value being too big for conversion into an ASN1 encoded TLV byte array.
	 * @throws SigningOperationFailedException if the signature operation in the CryptoCore fails and is propagated to the ERSSpecificModule.
	 * @throws ErrorTransactionCounterOverflow when incrementing the transaction counter would result in an illegal value for that counter
	 * @throws ErrorSignatureCounterOverflow when incrementing the signature counter would result in an illegal value for that counter
	 * @throws ErrorStartTransactionFailed if the clientID is that of an unregistered client
	 * @throws TooManyOpenTransactionsException if starting this transaction would be one too many
	 * @since 1.0
	 * @version 1.5
	 */
	public synchronized byte[] startTransaction(String clientID, byte[] processData, String processType, byte[] additionalData) throws ValueNullException, 
		ValueTooBigException, SigningOperationFailedException, ErrorSignatureCounterOverflow, ErrorTransactionCounterOverflow, 
		ErrorStartTransactionFailed, TooManyOpenTransactionsException {
		//first, check if the client invoking this is registered. If the client is not registered, throw an exception!
		if(!clientsUsingTSE.contains(clientID)) {
			throw new ErrorStartTransactionFailed("ClientId " +clientID +" not registered for TSE usage!");
		}
		//if the client is registered, start the process as normal:
		byte[] logmessageWithoutSequenceWrapper = null;
		try {
			logmessageWithoutSequenceWrapper = erssm.startTransaction(clientID, processData, processType, additionalData);
		} catch (SignatureException e1) {
			throw new SigningOperationFailedException("SignatureException caught\n" +e1.getMessage());
		}
		
		//the byte array given back by the ErsSpecificModule is without the necessary "SEQUENCE" wrapper. 
		TLVObject transactionLogWithSequenceWrapper = new TLVObject();
		transactionLogWithSequenceWrapper.setTagWithByteElement(ASN1Constants.UNIVERSAL_SEQUENCE);
		transactionLogWithSequenceWrapper.setValue(logmessageWithoutSequenceWrapper);
		
		//convert the SEQUENCE-wrapped TLVObect into a TLV byte array
		byte[] transactionLogMessageByteArray = null;
		try {
			transactionLogMessageByteArray = transactionLogWithSequenceWrapper.toTLVByteArray();
		} catch (ValueNullException e) {
			throw new ValueNullException(e.getMessage());
		} catch (ValueTooBigException e) {
			throw new ValueTooBigException(e.getMessage());
		}
		//set the latestLogMessage byte array:
		latestLogMessage = transactionLogMessageByteArray;
		
		return transactionLogMessageByteArray;
	}
	
	/**
	 * Calls the updateTransaction method in the ERSSpecificModule class and wraps the result with the SEQUENCE wrapper.
	 * @param clientID - represents the ID of the application that has invoked the function.
	 * @param transactionNumber - number of the transaction to be updated. Has to exist for the correct execution of this function.
	 * @param processData - the process data that has to be logged as an octet string
	 * @param processType - represents the type of the transaction as defined by the application. Is OPTIONAL according to TR-03151 but MUST requirement
	 * according to TR-03153.
	 * @return a TransactionLogMessage encoded as an ASN1 encoded byte array with the SEQUENCE wrapper attached to it.
	 * @throws ErrorNoTransaction is thrown by the {@linkplain ERSSpecificModule} if there is no open transaction with the specified transaction number.
	 * @throws ValueTooBigException - is thrown by {@linkplain TransactionLogMessage#toMinorTLVByteArray()} if a value is too big to be converted to
	 * an ASN1 DER encoded byte array.
	 * @throws ValueNullException  - is thrown by {@linkplain TransactionLogMessage#toMinorTLVByteArray()} if a value that should be present is not.
	 * For example, a missing processType (processType == null) would cause such an Exception.
	 * @throws SigningOperationFailedException if the signature operation in the CryptoCore fails and is propagated to the ERSSpecificModule.
	 * @throws ErrorSignatureCounterOverflow when incrementing the signature counter would result in an illegal value for that counter
	 * @throws ErrorUpdateTransactionFailed if the function is invoked by an unregistered clientId
	 * @since 1.0
	 * @version 1.5
	 */
	public synchronized byte[] updateTransaction(String clientID, long transactionNumber, byte[] processData, String processType) throws ErrorNoTransaction, ValueNullException, ValueTooBigException, SigningOperationFailedException, ErrorSignatureCounterOverflow, ErrorUpdateTransactionFailed {
		//first, check if the client invoking this is registered. If the client is not registered, throw an exception!
		if(!clientsUsingTSE.contains(clientID)) {
			throw new ErrorUpdateTransactionFailed("ClientId " +clientID +" not registered for TSE usage!"); 
		}
		//if the client is registered, start the process as normal:
		byte[] logmessageWithoutSequenceWrapper;
		try {
			logmessageWithoutSequenceWrapper = erssm.updateTransaction(clientID, transactionNumber, processData, processType);
		} catch (SignatureException e1) {
			throw new SigningOperationFailedException("SignatureException caught.\n" +e1.getMessage());
		}
		
		//the byte array given back by the ErsSpecificModule is without the necessary "SEQUENCE" wrapper. 
		TLVObject transactionLogWithSequenceWrapper = new TLVObject();
		transactionLogWithSequenceWrapper.setTagWithByteElement(ASN1Constants.UNIVERSAL_SEQUENCE);
		transactionLogWithSequenceWrapper.setValue(logmessageWithoutSequenceWrapper);
		
		//convert the SEQUENCE-wrapped TLVObect into a TLV byte array
		byte[] transactionLogMessageByteArray = null;
		try {
			transactionLogMessageByteArray = transactionLogWithSequenceWrapper.toTLVByteArray();
		} catch (ValueNullException e) {
			e.printStackTrace();
		} catch (ValueTooBigException e) {
			e.printStackTrace();
		}
		//set the latestLogMessage byte array:
		latestLogMessage = transactionLogMessageByteArray;
		
		return transactionLogMessageByteArray;
	}
	
	/**
	 * Calls the FinishTransaction method in the ERSSpecificModule class and wraps the result with the SEQUENCE wrapper.
	 * @param clientID represents the ID of the application that has invoked the function.
	 * @param transactionNumber number of the transaction to be finished. Has to exist for the correct execution of this function.
	 * @param processData the process data that has to be logged as an octet string
	 * @param processType represents the type of the transaction as defined by the application. Is OPTIONAL according to TR-03151 but MUST requirement
	 * according to TR-03153.
	 * @param additionalData OPTIONAL. currently reserved for future use.
	 * @return a TransactionLogMessage encoded as an ASN1 encoded byte array with the SEQUENCE wrapper attached to it.
	 * @throws ErrorNoTransaction thrown by the {@linkplain ERSSpecificModule} if no open transaction exists with that particular transaction number.
	 * @throws ValueTooBigException - is thrown by {@linkplain TransactionLogMessage#toMinorTLVByteArray()} if a value is too big to be converted to
	 * an ASN1 DER encoded byte array.
	 * @throws ValueNullException  - is thrown by {@linkplain TransactionLogMessage#toMinorTLVByteArray()} if a value that should be present is not.
	 * For example, a missing processType (processType == null) would cause such an Exception.
	 * @throws SigningOperationFailedException if the signature operation in the CryptoCore fails and is propagated to the ERSSpecificModule.
	 * @throws ErrorSignatureCounterOverflow when incrementing the signature counter would result in an illegal value for that counter
	 * @throws ErrorFinishTransactionFailed if the function is invoked by an unregistered clientId
	 * @since 1.0
	 * @version 1.5
	 */
	public synchronized byte[] finishTransaction(String clientID, long transactionNumber, byte[] processData, String processType, byte[] additionalData) throws ErrorNoTransaction, 
	ValueNullException, ValueTooBigException, SigningOperationFailedException, ErrorSignatureCounterOverflow, ErrorFinishTransactionFailed {
		//first, check if the client invoking this is registered. If the client is not registered, throw an exception!
		if(!clientsUsingTSE.contains(clientID)) {
			throw new ErrorFinishTransactionFailed("ClientId " +clientID +" not registered for TSE usage!"); 
		}
		//if the client is registered, start the process as normal:
		byte[] logmessageWithoutSequenceWrapper;
		try {
			logmessageWithoutSequenceWrapper = erssm.finishTransaction(clientID, transactionNumber, processData, processType, additionalData);
		} catch (SignatureException e1) {
			throw new SigningOperationFailedException("SignatureException caught.\n" +e1.getMessage());
		}
		
		//the byte array given back by the ErsSpecificModule is without the necessary "SEQUENCE" wrapper. 
		TLVObject transactionLogWithSequenceWrapper = new TLVObject();
		transactionLogWithSequenceWrapper.setTagWithByteElement(ASN1Constants.UNIVERSAL_SEQUENCE);
		transactionLogWithSequenceWrapper.setValue(logmessageWithoutSequenceWrapper);
		
		//convert the SEQUENCE-wrapped TLVObect into a TLV byte array
		byte[] transactionLogMessageByteArray = null;
		try {
			transactionLogMessageByteArray = transactionLogWithSequenceWrapper.toTLVByteArray();
		} catch (ValueNullException e) {
			e.printStackTrace();
		} catch (ValueTooBigException e) {
			e.printStackTrace();
		}
		//set the latestLogMessage byte array:
		latestLogMessage = transactionLogMessageByteArray;
		
		return transactionLogMessageByteArray;
	}
	
//----------------------------------------------------SYSTEM METHODS------( FUNCTIONS)-------------------------------------------------------------
	//---------------------------------------------------MAINTENANCE FUNCTIONS---------------------------------------------------------
	
	/**
	 * Calls {@linkplain ERSSpecificModule#initialize(String)} to create a SystemLogMessage and wraps it with the SEQUENCE wrapper.
	 * @param description the description of the SE API, either set by the manufacturer or on fist call of {@linkplain TSEController#initialize(String)}.
	 * @return a SystemLogMessage encoded as an ASN1 encoded byte array with the SEQUENCE wrapper attached.
	 * @throws ErrorSigningSystemOperationDataFailed 
	 * @throws SigningOperationFailedException if the signature operation in the CryptoCore fails and is propagated to the ERSSpecificModule.
	 * @throws ErrorSignatureCounterOverflow when incrementing the signature counter would result in an illegal value for that counter
	 * @throws ValueTooBigException is thrown by {@linkplain SystemLogMessage#toMinorTLVByteArray()} if a value exceeds the limits of what can be encoded in ASN.1 
	 * DER format
	 * @throws ValueNullException is thrown by {@linkplain SystemLogMessage#toMinorTLVByteArray()} if a value that should have been present is not, and therefore null
	 * @since 1.0
	 */
	public synchronized byte[] initialize(String description) throws ErrorSigningSystemOperationDataFailed, SigningOperationFailedException, ErrorSignatureCounterOverflow, ValueNullException, ValueTooBigException {
		byte[] logmessageWithoutSequenceWrapper;
		try {
			logmessageWithoutSequenceWrapper = erssm.initialize(description);
		} catch (SignatureException e1) {
			throw new SigningOperationFailedException("SignatureException caught.\n" +e1.getMessage()) ;
		}
		
		//the byte array given back by the ERSSpecificModule is without the necessary SEQUENCE wrapper
		TLVObject systemLogWithSequenceWrapper = new TLVObject();
		systemLogWithSequenceWrapper.setTagWithByteElement(ASN1Constants.UNIVERSAL_SEQUENCE);
		systemLogWithSequenceWrapper.setValue(logmessageWithoutSequenceWrapper);
		
		//convert the SEQUENCE-wrapped TLVObect into a TLV byte array
		byte[] systemLogMessageByteArray = null;
		try {
			systemLogMessageByteArray = systemLogWithSequenceWrapper.toTLVByteArray();
		} catch (ValueNullException | ValueTooBigException e) {
			e.printStackTrace();
		}
		//set the latestLogMessage byte array:
		latestLogMessage = systemLogMessageByteArray;
		
		return systemLogMessageByteArray;
 	}
	
	/**
	 * Calls the updateTime method in the {@linkplain ERSSpecificModule}. The parameter explicitTimeWasPassed is used to distinguish between the two 
	 * ERSSpecificModule updateTime methods. If an explicit time was passed, the {@linkplain TSEController} would pass that time to this method. If no explicit
	 * time was passed, the TSEController would pass null and false to indicate that the parameterless updateTime of the ERSSpecificModule has
	 * to be used. The result is a SEQUENCE-wrapped {@linkplain SystemLogMessage} if the execution of this function is successful.
	 * @param newTime - either a valid time that shall be set in the CryptoCore's clock or null depending on the value of explicitTimeWasPassed.
	 * @param explicitTimeWasPassed - if this is true, the ERSSpecificModule method to explicitly set a new time is called. If this is false, the 
	 * parameterless variant of this method is called instead.
	 * @return a SystemLogMessage encoded as an ASN1 encoded byte array with the SEQUENCE wrapper attached to it.
	 * @throws ErrorUpdateTimeFailed can only be raised, if the explicit time setting is used. And only, if the new time is before 1.1.2019 0:0:0 or 
	 * after the beginning of the year 2100.
	 * @throws ValueTooBigException - is thrown by {@linkplain TransactionLogMessage#toMinorTLVByteArray()} if a value is too big to be converted to
	 * an ASN1 DER encoded byte array.
	 * @throws ValueNullException  - is thrown by {@linkplain TransactionLogMessage#toMinorTLVByteArray()} if a value that should be present is not.
	 * For example, a missing time would cause such an Exception.
	 * @throws SigningOperationFailedException if the signature operation in the CryptoCore fails and is propagated to the ERSSpecificModule.
	 * @throws ErrorInvalidTime if a value for the new time is provided and that value is out of bounds. 
	 * @throws ErrorSignatureCounterOverflow when incrementing the signature counter would result in an illegal value for that counter
	 * @since 1.0
	 */
	public synchronized byte[] updateTime(ZonedDateTime newTime, boolean explicitTimeWasPassed) throws ErrorUpdateTimeFailed, ValueNullException, ValueTooBigException, SigningOperationFailedException, ErrorInvalidTime, ErrorSignatureCounterOverflow {
			byte[] logmessageWithoutSequenceWrapper = null;
			
			try {
				//if the time was explicitly passed, use the corresponding method in the ERSSpecificModule
				if(explicitTimeWasPassed) {
					//this may throw ErrorUpdateTimeFailed if the time has an illegal value, that is, if the time is before 1.1.2019 or after 1.1.2100
					logmessageWithoutSequenceWrapper = erssm.updateTime(newTime);
				}
				else {
					logmessageWithoutSequenceWrapper = erssm.updateTime();
				}
			} catch(SignatureException e1) {
				throw new SigningOperationFailedException("Signature exception caught.\n" +e1.getMessage());
			}
	
			//byte array given back by the ERSSpecificModule is without the necessary SEQUENCE wrapper
			TLVObject systemLogWithSequenceWrapper = new TLVObject();
			systemLogWithSequenceWrapper.setTagWithByteElement(ASN1Constants.UNIVERSAL_SEQUENCE);
			systemLogWithSequenceWrapper.setValue(logmessageWithoutSequenceWrapper);
			
			//convert the SEQUENCE-wrapped TLVObect into a TLV byte array
			byte[] systemLogMessageByteArray = null;
			try {
				systemLogMessageByteArray = systemLogWithSequenceWrapper.toTLVByteArray();
			} catch (ValueNullException | ValueTooBigException e) {
				e.printStackTrace();
			}
			//set the latestLogMessage byte array:
			latestLogMessage = systemLogMessageByteArray;
			
			return systemLogMessageByteArray;
	}
	
	/**
	 * Calls {@linkplain ERSSpecificModule#disableSecureElement()} to create a SEQUENCE-wrapped {@linkplain SystemLogMessage} logging the process of disabling the SecureElement. 
	 * A separate function to disable the secure element is needed, because the TR-03151 requires
	 * the de-activation being a separate step.
	 * @return a SystemLogMessage encoded as an ASN1 encoded byte array with the SEQUENCE wrapper attached to it.
	 * @throws SigningOperationFailedException if the signature operation in the CryptoCore fails and is propagated to the ERSSpecificModule.
	 * @throws ErrorSignatureCounterOverflow when incrementing the signature counter would result in an illegal value for that counter
	 * @throws ValueTooBigException is thrown by {@linkplain SystemLogMessage#toMinorTLVByteArray()} if a value exceeds the limits of what can be encoded in ASN.1 
	 * DER format
	 * @throws ValueNullException is thrown by {@linkplain SystemLogMessage#toMinorTLVByteArray()} if a value that should have been present is not, and therefore null
	 * @since 1.0
	 */
	public synchronized byte[] disableSE() throws SigningOperationFailedException, ErrorSignatureCounterOverflow, ValueNullException, ValueTooBigException {
		byte[] logMessageWithoutSequenceWrapper = null;
		//try to invoke the ERSSpecificModule functionality to log the disableSE operation
		try {
			logMessageWithoutSequenceWrapper = this.erssm.disableSecureElement();
		} catch (SignatureException e) {
			throw new SigningOperationFailedException("SignatureException caught.\n", e);
		}
		
		//byte array given back by the ERSSpecificModule is without the necessary SEQUENCE wrapper
		TLVObject systemLogWithSequenceWrapper = new TLVObject();
		systemLogWithSequenceWrapper.setTagWithByteElement(ASN1Constants.UNIVERSAL_SEQUENCE);
		systemLogWithSequenceWrapper.setValue(logMessageWithoutSequenceWrapper);
		
		//convert the SEQUENCE-wrapped TLVObect into a TLV byte array
		byte[] systemLogMessageByteArray = null;
		try {
			systemLogMessageByteArray = systemLogWithSequenceWrapper.toTLVByteArray();
		} catch (ValueNullException | ValueTooBigException e) {
			e.printStackTrace();
		}
		//set the latestLogMessage byte array:
		latestLogMessage = systemLogMessageByteArray;
		
		return systemLogMessageByteArray;
	}
	
	//---------------------------------------USER MANAGEMENT AND AUTHENTICATION------------------------------------------------------------
	/**
	 * Is used by the {@linkplain TSEController} to have the {@linkplain ERSSpecificModule} create a SystemLogMessage logging the attempt to authenticate
	 * a user. The SecurityModule gets the SystemLogMessage without the SEQUENCE wrapper back from the ERSSpecificModule and adds the wrapper.
	 * @param userID the userId that was passed from the outside to the TSEController.
	 * @param role the number of the role that the user has. Irrelevant if the userId is not managed by the TSE.
	 * @param authenticationResult the result of the authentication, true if the user has been authenticated, false otherwise.
	 * @param userIdManagedByTSE indicates whether the userId is known by the TSE or not. If the userId is known, is is true, if not it is false.
	 * Influences the way the ERSSpecificModule constructs the SystemLogMessage.
	 * @return a {@linkplain SystemLogMessage} encoded as an ASN1 encoded byte array with the SEQUENCE wrapper attached to it.
	 * @throws SigningOperationFailedException if the CryptoCore fails to sign the LogMessage and throws a SignatureException.
	 * @throws ErrorSignatureCounterOverflow when incrementing the signature counter would result in an illegal value for that counter
	 * @throws ValueTooBigException is thrown by {@linkplain SystemLogMessage#toMinorTLVByteArray()} if a value exceeds the limits of what can be encoded in ASN.1 
	 * DER format
	 * @throws ValueNullException is thrown by {@linkplain SystemLogMessage#toMinorTLVByteArray()} if a value that should have been present is not, and therefore null
	 * @see {@linkplain ERSSpecificModule#authenticateUser(String, int, boolean, boolean)}
	 */
	public synchronized byte[] authenticateUser(String userID, int role, boolean authenticationResult, boolean userIdManagedByTSE) throws SigningOperationFailedException, ErrorSignatureCounterOverflow, ValueNullException, ValueTooBigException {
		byte[] logmessageWithoutSequenceWrapper = null;
		
		//use the ERSSpecificModule to create the SystemLogMessage without the SEQUENCE wrapper
		try {
			logmessageWithoutSequenceWrapper = erssm.authenticateUser(userID, role, authenticationResult, userIdManagedByTSE);
		} catch (SignatureException e) {
			throw new SigningOperationFailedException("SignatureException caught.\n", e);
		}
		
		//byte array given back by the ERSSpecificModule is without the necessary SEQUENCE wrapper
		TLVObject systemLogWithSequenceWrapper = new TLVObject();
		systemLogWithSequenceWrapper.setTagWithByteElement(ASN1Constants.UNIVERSAL_SEQUENCE);
		systemLogWithSequenceWrapper.setValue(logmessageWithoutSequenceWrapper);
		
		//convert the SEQUENCE-wrapped TLVObect into a TLV byte array
		byte[] systemLogMessageByteArray = null;
		try {
			systemLogMessageByteArray = systemLogWithSequenceWrapper.toTLVByteArray();
		} catch (ValueNullException | ValueTooBigException e) {
			e.printStackTrace();
		}
		//set the latestLogMessage byte array:
		latestLogMessage = systemLogMessageByteArray;
		
		return systemLogMessageByteArray;
	}
	
	
	/**
	 * Is used by the {@linkplain TSEController} to have the {@linkplain ERSSpecificModule} create a SystemLogMessage logging the successful log out 
	 * operation of a user. The SecurityModule gets the SystemLogMessage without the SEQUENCE wrapper back from the ERSSpecificModule and adds the wrapper.
	 * <br><b>Note: as of version 1.5, there is no way the simulator performs a log out due to time out.</b><br>
	 * This means, that in the systemOperationData of every SystemLog produced by this method, the log out reason is "user".
	 * 
	 * @param userId the ID of the user that wants to log out. Is provided externally to the TSEController.
	 * @return a {@linkplain SystemLogMessage} encoded as an ASN1 encoded byte array with the SEQUENCE wrapper attached to it.
	 * @throws ErrorSignatureCounterOverflow when incrementing the signature counter would result in an illegal value for that counter
	 * @throws SigningOperationFailedException if the CryptoCore fails to sign the LogMessage and throws a SignatureException.
	 * @see {@linkplain ERSSpecificModule#logOut(String, int)}
	 * @version 1.5
	 * @throws ValueTooBigException is thrown by {@linkplain SystemLogMessage#toMinorTLVByteArray()} if a value exceeds the limits of what can be encoded in ASN.1 
	 * DER format
	 * @throws ValueNullException is thrown by {@linkplain SystemLogMessage#toMinorTLVByteArray()} if a value that should have been present is not, and therefore null
	 */
	public synchronized byte[] logOutUser(String userId) throws ErrorSignatureCounterOverflow, SigningOperationFailedException, ValueNullException, ValueTooBigException {
		byte[] logmessageWithoutSequenceWrapper = null;
		
		//use the ERSSpecificModule to create the SystemLogMessage without the SEQUENCE wrapper
		try {
			logmessageWithoutSequenceWrapper = erssm.logOut(userId, Constants.LOGOUT_LOGOUTCAUSE_USER);
		} catch (SignatureException e) {
			throw new SigningOperationFailedException("SignatureException caught.\n", e);
		}
		
		//byte array given back by the ERSSpecificModule is without the necessary SEQUENCE wrapper
		TLVObject systemLogWithSequenceWrapper = new TLVObject();
		systemLogWithSequenceWrapper.setTagWithByteElement(ASN1Constants.UNIVERSAL_SEQUENCE);
		systemLogWithSequenceWrapper.setValue(logmessageWithoutSequenceWrapper);
		
		//convert the SEQUENCE-wrapped TLVObect into a TLV byte array
		byte[] systemLogMessageByteArray = null;
		try {
			systemLogMessageByteArray = systemLogWithSequenceWrapper.toTLVByteArray();
		} catch (ValueNullException | ValueTooBigException e) {
			e.printStackTrace();
		}
		//set the latestLogMessage byte array:
		latestLogMessage = systemLogMessageByteArray;
		
		return systemLogMessageByteArray;
	}
	
	/**
	 * Is used by the {@linkplain TSEController} to have the {@linkplain ERSSpecificModule} create a SystemLogMessage logging the invocation of the function 
	 * unblockUser. The SecurityModule gets the SystemLogMessage without the SEQUENCE wrapper back from the ERSSpecificModule and adds the wrapper.
	 * 
	 * @param userId the ID of the user that wants to be unblocked. Is provided externally to the TSEController.
	 * @return a {@linkplain SystemLogMessage} encoded as an ASN1 encoded byte array with the SEQUENCE wrapper attached to it.
	 * @throws ErrorSignatureCounterOverflow when incrementing the signature counter would result in an illegal value for that counter
	 * @throws SigningOperationFailedException if the CryptoCore fails to sign the LogMessage and throws a SignatureException.
	 * @version 1.5
	 * @throws ValueTooBigException is thrown by {@linkplain SystemLogMessage#toMinorTLVByteArray()} if a value exceeds the limits of what can be encoded in ASN.1 
	 * DER format
	 * @throws ValueNullException is thrown by {@linkplain SystemLogMessage#toMinorTLVByteArray()} if a value that should have been present is not, and therefore null
	 * @see {@linkplain ERSSpecificModule#unblockUser(String, int)}
	 */
	public synchronized byte[] unblockUser(String userID, int unblockResult) throws ErrorSignatureCounterOverflow, SigningOperationFailedException, ValueNullException, ValueTooBigException {
		byte[] logmessageWithoutSequenceWrapper = null;
		
		//use the ERSSpecificModule to create the SystemLogMessage without the SEQUENCE wrapper
		try {
			logmessageWithoutSequenceWrapper = erssm.unblockUser(userID, unblockResult);
		} catch (SignatureException e) {
			throw new SigningOperationFailedException("SignatureException caught.\n", e);
		}
		
		//byte array given back by the ERSSpecificModule is without the necessary SEQUENCE wrapper
		TLVObject systemLogWithSequenceWrapper = new TLVObject();
		systemLogWithSequenceWrapper.setTagWithByteElement(ASN1Constants.UNIVERSAL_SEQUENCE);
		systemLogWithSequenceWrapper.setValue(logmessageWithoutSequenceWrapper);
		
		//convert the SEQUENCE-wrapped TLVObect into a TLV byte array
		byte[] systemLogMessageByteArray = null;
		try {
			systemLogMessageByteArray = systemLogWithSequenceWrapper.toTLVByteArray();
		} catch (ValueNullException | ValueTooBigException e) {
			e.printStackTrace();
		}
		//set the latestLogMessage byte array:
		latestLogMessage = systemLogMessageByteArray;
		
		return systemLogMessageByteArray;
	}
	
//--------------------------------REGISTER + DEREGISTER---------------------------------------------------------------------------
	/**
	 * Used by {@linkplain TSEController#registerClient(String)} to add a new clientId to the set of registered ones. 
	 * @param clientId the client to be added
	 * @throws ClientIdAlreadyRegisteredException if the client had been registered before
	 * @throws TooManyClientsException if adding the client would result in one client too many for the TSE
	 */
	public void registerClient(String clientId) throws ClientIdAlreadyRegisteredException, TooManyClientsException {
		//check if the clientId is already registered
		if(clientsUsingTSE.contains(clientId)) {
			throw new ClientIdAlreadyRegisteredException();
		}
		//check if adding the new clientId would violate the maximum number of clients
		if((clientsUsingTSE.size()+1) > maxNumberClients) {
			throw new TooManyClientsException();
		}
		//everything went okay: add the clientId to the registered clientId list
		clientsUsingTSE.add(clientId);
	}
	
	/**
	 * Used by {@linkplain TSEController#deregisterClient(String)} to remove a client from the set of registered ones. 
	 * @param clientId the client to be removed
	 * @throws ClientIdNotRegisteredException if attempting to remove a client that is not even registered
	 */
	public void deregisterClient(String clientId) throws ClientIdNotRegisteredException {
		//check if the client is registered, it can not be removed if it isn't
		if(!clientsUsingTSE.contains(clientId)) {
			throw new ClientIdNotRegisteredException();
		}
		//de-register the client from the TSE
		clientsUsingTSE.remove(clientId);
	}
	
//--------------------------------GRACEFUL SHUTDOWN------------------------------------------------------------------------------
	/**
	 * Used by 	{@linkplain TSEController#gracefulShutdown()} to de-register all the currently registered clients. This is performed so that another 
	 * dummy client can then be used to close all remaining open transactions, even if the TSE technically is a single client TSE.
	 * This method just checks if there are still elements inside the underlying HashSet storing the registered clients and deletes them if there are.<br>
	 * Note: after this method returns, there are no clients registered on the TSE.
	 */
	public void deregisterAllClients() {
		//if there is no client to be logged out, return
		if(clientsUsingTSE.isEmpty()) {
			return;
		}
		//else: make the HashSet empty
		else {
			clientsUsingTSE.clear();
			return;
		}
	}
	
	/**
	 * Gets a Set of the transaction numbers associated with currently open transactions from the {@linkplain ERSSpecificModule}.
	 * Used for {@linkplain TSEController#gracefulShutdown()}.
	 * @return a Set of transaction numbers of currently open transactions
	 * @since 1.5
	 */
	public synchronized  Set<Long> getERSSMOpenTransactionNumbers(){
		return erssm.getOpenTransactionNumbers();
	}
	
}
