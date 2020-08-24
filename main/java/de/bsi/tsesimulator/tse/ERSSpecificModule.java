/**
 * 
 */
package main.java.de.bsi.tsesimulator.tse;

import java.io.IOException;
import java.security.SignatureException;
import java.time.ZonedDateTime;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import main.java.de.bsi.seapi.exceptions.ErrorInvalidTime;
import main.java.de.bsi.seapi.exceptions.ErrorNoTransaction;
import main.java.de.bsi.seapi.exceptions.ErrorSigningSystemOperationDataFailed;
import main.java.de.bsi.seapi.exceptions.ErrorUpdateTimeFailed;
import main.java.de.bsi.tsesimulator.constants.ASN1Constants;
import main.java.de.bsi.tsesimulator.constants.ConfigConstants;
import main.java.de.bsi.tsesimulator.constants.Constants;
import main.java.de.bsi.tsesimulator.exceptions.ErrorSignatureCounterOverflow;
import main.java.de.bsi.tsesimulator.exceptions.ErrorTransactionCounterOverflow;
import main.java.de.bsi.tsesimulator.exceptions.TLVException;
import main.java.de.bsi.tsesimulator.exceptions.TooManyOpenTransactionsException;
import main.java.de.bsi.tsesimulator.exceptions.ValueNullException;
import main.java.de.bsi.tsesimulator.exceptions.ValueTooBigException;
import main.java.de.bsi.tsesimulator.msg.LogMessage;
import main.java.de.bsi.tsesimulator.msg.SystemLogMessage;
import main.java.de.bsi.tsesimulator.msg.TransactionLogMessage;
import main.java.de.bsi.tsesimulator.preferences.PropertyValues;
import main.java.de.bsi.tsesimulator.tlv.TLVObject;
import main.java.de.bsi.tsesimulator.utils.Utils;

/**
 * Represents the electronic record-keeping system specific module of the simulator. 
 * It manages the transaction counter and the creation of {@linkplain LogMessage}s. The latter are created in 2 parts: first part is created by ERSSpecificModule itself 
 * since it only uses data already available to this class (input and counters kept by the ERSSpecificModule). The second part uses a {@linkplain CryptoCore} for the cryptographic 
 * signing operation and for adding the value <i>logTime</i> to the log message. <br>
 * The ERSSpecificModule is also responsible for setting data in {@linkplain SystemLogMessage}s.<br>
 * In addition to managing the transaction counter, the ERSSpecificModule manages a list of the currently open transactions and a {@linkplain CryptoCore} which 
 * it uses to create the signature value and to get the log time. 
 * 
 * @author dpottkaemper
 * @see {@linkplain SecurityModule} ,{@linkplain CryptoCore}
 */

public class ERSSpecificModule {
	private long transactionCounter;		//counts each transaction operation. Can count up to 9.223.372.036.854.775.808 - 1 transaction operations.
	private String algorithmOID;
	
	private int maxNumberOpenTransactions;	//stores the maximum number of transactions that can be open simultaneously
	private Map<Long, TLVObject[]> transactionsOpen;
	
	private CryptoCore cryptoCore;
	private byte[] serialNumber;
	
	/**
	 * Private parameterless constructor, since this class needs to be instantiated through the other constructors if one really wants to use it.<br>
	 * Use {@linkplain ERSSpecificModule#ERSSpecificModule(CryptoCore, byte[])} or {@linkplain ERSSpecificModule#ERSSpecificModule(CryptoCore, byte[], long)} instead!
	 */
	@SuppressWarnings("unused")
	private ERSSpecificModule() {}
	
	/**
	 * Constructor which sets the CryptoCore. Used by the SecurityModule class to help the ERSSpecificModule access the signing functionality of the CryptoCore.
	 * Also sets the serial number so the LogMessages can be created with the correct serial number.
	 * 
	 * The CryptoCore has to be given to the ERSSpecificModule so that it may use it to sign the log messages. Since the number of CryptoCores is not limited
	 * it could be possible that one ERSSpecificModule uses multiple CryptoCores in the future. That's the reason why the CryptoCore class can not provide a 
	 * static sign method and the ERSSpecificModule has to know its CryptoCore.
	 * 
	 * <b>Changes in version 1.5:</b><br>
	 * Now stores the maximum number of transactions that can be opened simultaneously internally. If reading that value from the configuration file fails, the maximum number 
	 * of transactions is set to the value of {@linkplain Constants#DEFAULT_MAX_NUMBER_OF_TRANSACTIONS}.
	 * 
	 * @param cryptoCoreForSigning the CryptoCore which signs all log messages. Note that in the future, 
	 * there could be different CryptoCores for different types of messages.
	 * @param serialNumber the serialNumber of the TSE which is calculated as a hash of the public key used to sign messages
	 * @throws IOException if reading configuration file through {@linkplain PropertyValues#getInstance()} fails 
	 */
	public ERSSpecificModule(CryptoCore cryptoCoreForSigning, byte[] serialNumber) throws IOException {
		this.transactionCounter = 0;
		this.transactionsOpen = new Hashtable<Long, TLVObject[]>();
		//fetch the maximum number of transactions from config.properties
		//try to read from the config file, if that fails or has an illegal value, resort to default
		try {
			this.maxNumberOpenTransactions = Integer.parseInt(PropertyValues.getInstance().getValue(ConfigConstants.CFG_TAG_MAX_NUMBER_TRANSACTIONS));
		} catch(Exception e) {
			if(e instanceof IOException){
				throw new IOException(e.getMessage(), e);
			}
			this.maxNumberOpenTransactions = Constants.DEFAULT_MAX_NUMBER_OF_TRANSACTIONS;
		}
		if(maxNumberOpenTransactions < 1) {
			this.maxNumberOpenTransactions = Constants.DEFAULT_MAX_NUMBER_OF_TRANSACTIONS;
		}
		
		//fetch the OID of the algorithm from the config.properties file and store it for easy access 
		algorithmOID = Constants.ALGORITHM_OID_MAP.get(PropertyValues.getInstance().getConstantsAlgorithmName());
		//store the reference to cryptoCore and serial number
		this.cryptoCore = cryptoCoreForSigning;
		this.serialNumber = serialNumber;
		
		String algorithmUsedByCryptoCore = Constants.ALGORITHM_OID_MAP.get(this.cryptoCore.getAlgorithm().getAlgorithmDefinition());
		if(!(this.algorithmOID.equalsIgnoreCase(algorithmUsedByCryptoCore))) {
			System.out.println("ALGORITHMS DO NOT MATCH!!!!!\nALGORITHM OIDs ARE DIFFERENT!");
		}
	}
	
	/**
	 * The constructor that is used if the transaction number was loaded from a persistence file. Is called by {@linkplain SecurityModule}. The CryptoCore has to be given to the ERSSpecificModule so that it may use it to sign the log messages. Since the number of CryptoCores is not limited
	 * it could be possible that one ERSSpecificModule uses multiple CryptoCores in the future. That's the reason why the CryptoCore class can not provide a 
	 * static sign method and the ERSSpecificModule has to know its CryptoCore.<br>
	 * 
	 * The ERSSpecificModule gets the serial number as well so it can use it to create the log messages correctly and does not have to ask the CryptoCore
	 * for its serial number each time it wants to create a log message.
	 * 
	 * <b>Changes in version 1.5:</b><br>
	 * Now stores the maximum number of transactions that can be opened simultaneously internally. If reading that value from the configuration file fails, the maximum number 
	 * of transactions is set to the value of {@linkplain Constants#DEFAULT_MAX_NUMBER_OF_TRANSACTIONS}.
	 * 
	 * @param cryptoCoreForSigning the CryptoCore which signs all log messages. Note that in the future, 
	 * there could be different CryptoCores for different types of messages.
	 * @param serialNumber the serialNumber of the TSE which is calculated as a hash of the public key used to sign messages
	 * @param loadedTransactionCounter the transaction counter that was loaded from a file.
	 * @throws IOException if reading configuration file through {@linkplain PropertyValues#getInstance()} fails 
	 */
	public ERSSpecificModule(CryptoCore cryptoCoreForSigning, byte[] serialNumber, long loadedTransactionCounter) throws IOException {
		//set the transaction counter to the value that was passed 
		this.transactionCounter = loadedTransactionCounter;
		//create the hashtable that stores the open transactions
		this.transactionsOpen = new Hashtable<Long, TLVObject[]>();
		//fetch the maximum number of transactions from config.properties
		//try to read from the config file, if that fails or has an illegal value, resort to default
		try {
			this.maxNumberOpenTransactions = Integer.parseInt(PropertyValues.getInstance().getValue(ConfigConstants.CFG_TAG_MAX_NUMBER_TRANSACTIONS));
		} catch(Exception e) {
			if(e instanceof IOException){
				throw new IOException(e.getMessage(), e);
			}
			this.maxNumberOpenTransactions = Constants.DEFAULT_MAX_NUMBER_OF_TRANSACTIONS;
		}
		if(maxNumberOpenTransactions < 1) {
			this.maxNumberOpenTransactions = Constants.DEFAULT_MAX_NUMBER_OF_TRANSACTIONS;
		}
		
		//fetch the OID of the algorithm from the config.properties file and store it for easy access
		algorithmOID = Constants.ALGORITHM_OID_MAP.get(PropertyValues.getInstance().getConstantsAlgorithmName());
		
		//assign the cryptoCore and the serial number
		this.cryptoCore = cryptoCoreForSigning;
		this.serialNumber = serialNumber;
		
		String algorithmUsedByCryptoCore = Constants.ALGORITHM_OID_MAP.get(this.cryptoCore.getAlgorithm().getAlgorithmDefinition());
		if(!(this.algorithmOID.equalsIgnoreCase(algorithmUsedByCryptoCore))) {
			System.out.println("ALGORITHMS DO NOT MATCH!!!!!\nALGORITHM OIDs ARE DIFFERENT!");
		}
	}

//----------------------------------------------------TRANSACTION METHODS------(INPUT FUNCTIONS)-------------------------------------------------------------
	/**
	 * The method that starts a transaction. It constructs a TrasactionlogMessage and fills it with the parameters it gets from its caller.
	 * After the construction of the upper part of the logmessage this method converts the logmessage into a byte array and passes it to the CryptoCore which signs
	 * the logmessage. The CryptoCore passes the bottom part of the logmessage with the signature counter, the logtime and the signature as a byte array back to this method
	 * which then adds this part to the upper logmessage byte array.
	 * @param cliendID - represents the ID of the application that has invoked the function.
	 * @param processData - the process data that has to be logged as an octet string
	 * @param processType - represents the type of the transaction as defined by the application. Is OPTIONAL according to TR-03151 but MUST requirement
	 * according to TR-03153..
	 * @param additionalData - OPTIONAL. currently reserved for future use.
	 * @return a TransactionLogMessage encoded as an ASN1 byte array.
	 * @throws ValueTooBigException is thrown by {@linkplain TransactionLogMessage#toMinorTLVByteArray()} if a value is too big to be converted to
	 * an ASN1 DER encoded byte array.
	 * @throws ValueNullException is thrown by {@linkplain TransactionLogMessage#toMinorTLVByteArray()} if a value that should be present is not.
	 * For example, a missing processType (processType == null) would cause such an Exception.
	 * @throws SignatureException if the signing functionality of the CryptoCore fails for any reason. First approach for fixing this should be to 
	 * make sure, that the bit length of the order of the base point G of the chosen curve is equal or smaller than the output of the hash
	 * function. E.g. SHA512 with brainpoolP256r1 works, but SHA256 with brainpoolP512r1 does not.
	 * @throws ErrorSignatureCounterOverflow when incrementing the signature counter would result in an illegal value for that counter
	 * @throws ErrorTransactionCounterOverflow when incrementing the transaction counter would result in an illegal value for that counter
	 * @throws TooManyOpenTransactionsException when starting this transaction would exceed the legal number of transactions that are allowed to be open simultaneously
	 * @see {@linkplain CryptoCore#sign(byte[])}
	 */
	public synchronized byte[] startTransaction(String cliendID, byte[] processData, String processType, byte[] additionalData) throws ValueNullException, ValueTooBigException, SignatureException, ErrorSignatureCounterOverflow, ErrorTransactionCounterOverflow, TooManyOpenTransactionsException {
		//check if the transaction counter would overflow. If thats the case, throw ErrorIncrementTransactionCounter exception
		if((this.transactionCounter >= (Long.MAX_VALUE - 1)) || (this.transactionCounter < 0)) {
			throw new ErrorTransactionCounterOverflow();
		}
		//check if the transaction would result in exceeding the storage space for simultaneously open transactions
		if((this.transactionsOpen.size() + 1) > maxNumberOpenTransactions) {
			throw new TooManyOpenTransactionsException();
		}
		
		//if transaction counter would not overflow and there is still space for one more transaction, increment the transaction counter
		this.transactionCounter++;
		//create the TransactionlogMessage
		TransactionLogMessage transactionLog = new TransactionLogMessage(cliendID, processData, processType, additionalData, this.transactionCounter, this.serialNumber);
		//set the operationType to "start transaction"
		transactionLog.setOperationtype("StartTransaction");
		//set the algorithm OID
		transactionLog.setAlgorithm(algorithmOID);
		
		
		//transform the upper part of the logmessage to a byte array. Then pass it to the CryptoCore so it may sign it and return the lower part of the message.
		byte[] upperTransactionLogMessageByteArray = transactionLog.toMinorTLVByteArray();
		byte[] lowerTransactionLogMessageByteArray = cryptoCore.sign(upperTransactionLogMessageByteArray);
		
		//concat the upper and the lower part and store it in another byte array
		byte[] finishedTransactionLogMessageByteArray = new byte[upperTransactionLogMessageByteArray.length+lowerTransactionLogMessageByteArray.length];
		finishedTransactionLogMessageByteArray = Utils.concatTwoByteArrays(upperTransactionLogMessageByteArray, lowerTransactionLogMessageByteArray);
		
		//add the TLVObject array that represents the logmessage to the hashtable that strores open transactions
			TLVObject[] transactionAsTLVObjectArray = null;
			try {
				transactionAsTLVObjectArray = TLVObject.decodeASN1ByteArrayToTLVObjectArray(finishedTransactionLogMessageByteArray);
			} catch (TLVException e) {
				e.printStackTrace();
			}
			this.transactionsOpen.put( transactionCounter, transactionAsTLVObjectArray);
			
		return finishedTransactionLogMessageByteArray;
	}
	
	/**
	 * Updates an open transaction with new data, provided the transaction to be updated already exists. Currently only self-contained updates are supported.
	 * This method creates a new TransactionLogMessage and passes the parameters to it. Since the updates currently do not support additional external data
	 * this value is set to "null" when the TransactionLogMessage is created.
	 * 
	 * @param clientID - represents the ID of the application that has invoked the function.
	 * @param transactionNumber - number of the transaction to be updated. Has to exist for the correct execution of this function.
	 * @param processData - the process data that has to be logged as an octet string
	 * @param processType - represents the type of the transaction as defined by the application. Is OPTIONAL according to TR-03151 but MUST requirement
	 * according to TR-03153.
	 * @return a TransactionLogMessage encoded as an ASN1 byte array.
	 * @throws ErrorNoTransaction if no open transaction exists with the given transaction number
	 * @throws ValueTooBigException - is thrown by {@linkplain TransactionLogMessage#toMinorTLVByteArray()} if a value is too big to be converted to
	 * an ASN1 DER encoded byte array.
	 * @throws ValueNullException  - is thrown by {@linkplain TransactionLogMessage#toMinorTLVByteArray()} if a value that should be present is not.
	 * For example, a missing processType (processType == null) would cause such an Exception.
	 * @throws SignatureException if the signing functionality of the CryptoCore fails for any reason. First approach for fixing this should be to 
	 * make sure, that the bit length of the order of the base point G of the chosen curve is equal or smaller than the output of the hash
	 * function. E.g. SHA512 with brainpoolP256r1 works, but SHA256 with brainpoolP512r1 does not.
	 * @throws ErrorSignatureCounterOverflow when incrementing the signature counter would result in an illegal value for that counter
	 * @see {@linkplain CryptoCore#sign(byte[])}
	 */
	public synchronized byte[] updateTransaction(String clientID, long transactionNumber, byte[] processData, 
			String processType) throws ErrorNoTransaction, ValueNullException, ValueTooBigException, SignatureException, ErrorSignatureCounterOverflow {
		//checks whether an open transaction with this transaction number exists
		boolean transactionExists = transactionsOpen.containsKey(transactionNumber);	//implement real check here

		if(!transactionExists) {
			throw new ErrorNoTransaction();
		}
		// create the upper transaction logmessage
		TransactionLogMessage transactionLog = new TransactionLogMessage(clientID, processData, processType, null, transactionNumber, this.serialNumber);
		//set the operation type to "UpdateTransaction"
		transactionLog.setOperationtype("UpdateTransaction");
		//set the algorithm OID
		transactionLog.setAlgorithm(algorithmOID);
		
		//convert the upper transaction log message to a tlv byte array
		byte[] upperTransactionLogMessage = transactionLog.toMinorTLVByteArray();
		//give the upper log message to tge cryptoCore and let it sign 
		byte[] lowerTransactionLogMessage = cryptoCore.sign(upperTransactionLogMessage);
		
		//create the array that will be returned
		byte[] finishedTransactionLogMessage = Utils.concatTwoByteArrays(upperTransactionLogMessage, lowerTransactionLogMessage);
		
		return finishedTransactionLogMessage;
	}
	
	/**
	 * Finishes a transaction with the provided process data. The method checks, whether the transaction that should be closed exists and if it does,
	 * it creates a TransactionLogMessage with the parameters passed in the method call. When the log message has been created successfully, it is
	 * removed from the list of open transactions. 
	 * @param clientID - represents the ID of the application that has invoked the function.
	 * @param transactionNumber - number of the transaction to be updated. Has to exist for the correct execution of this function.
	 * @param processData - the process data that has to be logged as an octet string
	 * @param processType - represents the type of the transaction as defined by the application. Is OPTIONAL.
	 * @param additionalData - OPTIONAL. currently reserved for future use.
	 * @return a TransactionLogMessage encoded as an ASN1 byte array.
	 * @throws ErrorNoTransaction if no open transaction exists with the given transaction number.<b>NOT REQUIRED TO BE THROWN AS OF VERSION 1.0.1 BSI TR-03151.</b>
	 * @throws ValueTooBigException - is thrown by {@linkplain TransactionLogMessage#toMinorTLVByteArray()} if a value is too big to be converted to
	 * an ASN1 DER encoded byte array.
	 * @throws ValueNullException  - is thrown by {@linkplain TransactionLogMessage#toMinorTLVByteArray()} if a value that should be present is not.
	 * For example, a missing processType (processType == null) would cause such an Exception.
	 * @throws SignatureException if the signing functionality of the CryptoCore fails for any reason. First approach for fixing this should be to 
	 * make sure, that the bit length of the order of the base point G of the chosen curve is equal or smaller than the output of the hash
	 * function. E.g. SHA512 with brainpoolP256r1 works, but SHA256 with brainpoolP512r1 does not.
	 * @throws ErrorSignatureCounterOverflow when incrementing the signature counter would result in an illegal value for that counter
	 * @see {@linkplain CryptoCore#sign(byte[])}
	 */
	public synchronized byte[] finishTransaction(String clientID, long transactionNumber, byte[] processData, 
			String processType, byte[] additionalData) throws ErrorNoTransaction, ValueNullException, ValueTooBigException, SignatureException, ErrorSignatureCounterOverflow {
		//checks whether an open transaction with this transaction number exists
		boolean transactionExists = transactionsOpen.containsKey(transactionNumber);

		if(!transactionExists) {
			throw new ErrorNoTransaction();
		}
		//create a new TransactionLogMessage Object
		TransactionLogMessage transactionLog = new TransactionLogMessage(clientID, processData, processType, additionalData, transactionNumber, this.serialNumber) ;
		//set the operation type to "FinishTransaction"
		transactionLog.setOperationtype("FinishTransaction");
		//set the algorithm OID
		transactionLog.setAlgorithm(algorithmOID);
		
		//create the upper part of the logmessage as a byte array
		byte[] upperTransactionLogMessage = transactionLog.toMinorTLVByteArray();
		//create the lower part of the log message by passing the upper part to the cryptoCore for signing
		byte[] lowerTransactionLogMessage = cryptoCore.sign(upperTransactionLogMessage);
		
		//create what will be returned
		byte[] finishedTransactionLogMessage = new byte[upperTransactionLogMessage.length+lowerTransactionLogMessage.length];
		finishedTransactionLogMessage = Utils.concatTwoByteArrays(upperTransactionLogMessage, lowerTransactionLogMessage);
		
		//remove the finished transaction from the map
		transactionsOpen.remove(transactionNumber);
		return finishedTransactionLogMessage;
	}
	
//----------------------------------------------------SYSTEM METHODS------( FUNCTIONS)-------------------------------------------------------------
	/**
	 * Is called by the TSEController when the TSE is initialized. Has to get the description of the SE API as a parameter. 
	 * In case of a call to {@linkplain TSEController#initialize()} causing the creation of the System Log, the description <b>will be null</b> and the resulting 
	 * System Log <b>will not</b> contain the field systemOperationData.
	 * <br>Will create a SystemLogMessage which logs the initialization.
	 * @param description the description of the SE API or null
	 * @return a SystemLogMessage encoded as a ASN1 TLV byte array. 
	 * @throws ErrorSigningSystemOperationDataFailed if the conversion of the description String to a TLV byte array fails.
	 * @throws SignatureException if the signing functionality of the CryptoCore fails for any reason. First approach for fixing this should be to 
	 * make sure, that the bit length of the order of the base point G of the chosen curve is equal or smaller than the output of the hash
	 * function. E.g. SHA512 with brainpoolP256r1 works, but SHA256 with brainpoolP512r1 does not. Should result into an ErrorSigningSystemOperationDataFailed
	 * in a superior class.
	 * @throws ErrorSignatureCounterOverflow when incrementing the signature counter would result in an illegal value for that counter
	 * @throws ValueTooBigException is thrown by {@linkplain SystemLogMessage#toMinorTLVByteArray()} if a value is too big to be converted to
	 * an ASN1 DER encoded byte array.
	 * @throws ValueNullException  - is thrown by {@linkplain SystemLogMessage#toMinorTLVByteArray()} if a value that should be present is not.
	 * For example, a missing processType (processType == null) would cause such an Exception.
	 * @see {@linkplain CryptoCore#sign(byte[])}
	 */
	public synchronized byte[] initialize(String description) throws ErrorSigningSystemOperationDataFailed, SignatureException, ErrorSignatureCounterOverflow, ValueNullException, ValueTooBigException {
		//create SystemLogMessage object
		SystemLogMessage systemLogMessage = null;
		
		//if description == null, create SystemLog without systemOperationData:
		if(description == null) {
			systemLogMessage = new SystemLogMessage("Initialize", serialNumber);
		}
		
		//else (if the description is not null):
		else {
			//create a TLVObject that represents the description 
			TLVObject descriptionTLV = new TLVObject();
			descriptionTLV.setTagWithByteElement((byte) 0x81);
			descriptionTLV.setValue(description.getBytes());
			
			//convert that TLVobject to a TLV encoded byte array
			byte[] systemOperationData = null;
			try {
				systemOperationData = descriptionTLV.toTLVByteArray();
			} catch (ValueNullException | ValueTooBigException e) {
				//if the systemOperationData can not be converted to a TLV byte array the "passing of the system operation data" has failed
				throw new ErrorSigningSystemOperationDataFailed();
			}
			
			//create a system log message with the correct systemOperationData, the operationType "Initialize" and the serial number of the TSE
			systemLogMessage = new SystemLogMessage("Initialize", systemOperationData, serialNumber);
		}
		//set the algorithm OID
		systemLogMessage.setAlgorithm(algorithmOID);
		
		//convert the "upper part" of the logmessage into a TLV byte array and pass that to the CryptoCore which returns the signed part of the logmessage as a byte array
		byte[] upperSystemLogMessage = systemLogMessage.toMinorTLVByteArray();
		byte[] lowerSystemLogMessage = cryptoCore.sign(upperSystemLogMessage);
		
		//concat the upper and the lower logmessage parts and return them
		byte[] finishedSystemLogMessage = Utils.concatTwoByteArrays(upperSystemLogMessage, lowerSystemLogMessage);
		
		return finishedSystemLogMessage;
	}

	/**
	 * The updateTime version for time updates with explicitly passed time. This method converts the newTime object to UnixTime (the time format currently 
	 * supported in this TSE simulator) and then uses the new time and the old time to create a TLV encoded byte array for the systemOperationData.
	 * The systemOperationdata is created according to BSI TR-03151 Appendix A.
	 * @param newTime - the time to be set on the CryptoCore's clock in the java.time.ZonedDateTime format.
	 * @return a SystemLogMessage encoded as a ASN1 TLV byte array. 
	 * @throws ValueTooBigException - is thrown by {@linkplain TransactionLogMessage#toMinorTLVByteArray()} if a value is too big to be converted to
	 * an ASN1 DER encoded byte array.
	 * @throws ValueNullException  - is thrown by {@linkplain TransactionLogMessage#toMinorTLVByteArray()} if a value that should be present is not.
	 * For example, a missing time (for example {@linkplain CryptoCore#getTimeAsUnixTime()} returning null) would cause such an Exception.
	 * @throws SignatureException if the signing functionality of the CryptoCore fails for any reason. First approach for fixing this should be to 
	 * make sure, that the bit length of the order of the base point G of the chosen curve is equal or smaller than the output of the hash
	 * function. E.g. SHA512 with brainpoolP256r1 works, but SHA256 with brainpoolP512r1 does not.
	 * @throws ErrorInvalidTime if the newTime is an illegal time, that is, if the new time is before 1.1.2019 or after 1.1.2100. In that case, no logmessage
	 * is created and no new time is set.
	 * @throws ErrorUpdateTimeFailed if the functionality of the {@linkplain CryptoCore} to update the time fails.
	 * @throws ErrorSignatureCounterOverflow when incrementing the signature counter would result in an illegal value for that counter
	 * @see {@linkplain CryptoCore#sign(byte[])}
	 */
	public synchronized byte[] updateTime(ZonedDateTime newTime) throws ValueNullException, ValueTooBigException, SignatureException, ErrorInvalidTime, ErrorUpdateTimeFailed, ErrorSignatureCounterOverflow {
		//create a TLVObject which stores the time before the update
		TLVObject oldTimeTLV = new TLVObject();
		oldTimeTLV.setTagWithByteElement((byte) 0x81);
		oldTimeTLV.setValueWithLongElement(cryptoCore.getTimeAsUnixTime());
		
		//create a TLVObject which stores the time after the update but only create the TLVObject and set the tag
		TLVObject newTimeTLV = new TLVObject();
		newTimeTLV.setTagWithByteElement((byte) 0x82);
		
		//try to set the new time. if the time can not be set, an exception is thrown
		cryptoCore.setClock(newTime);
		
		//get the new time after the update from the CryptoCore and store that in a TLV
		newTimeTLV.setValueWithLongElement(cryptoCore.getTimeAsUnixTime());
		
		//transform the 2 TLVs into byte arrays
		byte[] oldTimebyteArray = null;
		byte[] newTimebyteArray = null;
	
		//Try to transform the old and new time TLVs into byte arrays. If that fails, ValueTooBig or ValueNull Exceptions are thrown
		oldTimebyteArray = oldTimeTLV.toTLVByteArray();
		newTimebyteArray = newTimeTLV.toTLVByteArray();
		
		//concat them to create the SystemOperationData
		byte[] systemopData = Utils.concatTwoByteArrays(oldTimebyteArray, newTimebyteArray);
		
		//create a new SystemLogMessage
		SystemLogMessage updateTimeSyslog = new SystemLogMessage("UpdateTime", systemopData, this.serialNumber);
		updateTimeSyslog.setAlgorithm(algorithmOID);
		byte[] upperUpdateTimesyslog = updateTimeSyslog.toMinorTLVByteArray();
		byte[] lowerUpdateTimesyslog = cryptoCore.sign(upperUpdateTimesyslog);
		
		byte[] finishedSyslog = Utils.concatTwoByteArrays(upperUpdateTimesyslog, lowerUpdateTimesyslog);
		return finishedSyslog;
	}
	
	/**
	 * The updateTime version for updateTime without explicitly passed time. Tries to synchronize the CryptoCore's clock to the current system time.
	 * Creates the systemOperation data from the old time before the update and the time directly after the update. 
	 * The systemOperationdata is created according to BSI TR-03151 Appendix A.
	 * @return a SystemLogMessage encoded as an ASN1 TLV byte array.
	 * @throws ValueTooBigException - is thrown by {@linkplain TransactionLogMessage#toMinorTLVByteArray()} if a value is too big to be converted to
	 * an ASN1 DER encoded byte array.
	 * @throws ValueNullException  - is thrown by {@linkplain TransactionLogMessage#toMinorTLVByteArray()} if a value that should be present is not.
	 * For example, a missing time (for example {@linkplain CryptoCore#getTimeAsUnixTime()} returning null)  would cause such an Exception.
	 * @throws SignatureException if the signing functionality of the CryptoCore fails for any reason. First approach for fixing this should be to 
	 * make sure, that the bit length of the order of the base point G of the chosen curve is equal or smaller than the output of the hash
	 * function. E.g. SHA512 with brainpoolP256r1 works, but SHA256 with brainpoolP512r1 does not.
	 * @throws ErrorUpdateTimeFailed if the functionality of the {@linkplain CryptoCore} to synchronize its time fails.
	 * @throws ErrorSignatureCounterOverflow when incrementing the signature counter would result in an illegal value for that counter
	 * @see {@linkplain CryptoCore#sign(byte[])}
	 */
	public synchronized byte[] updateTime() throws ValueNullException, ValueTooBigException, SignatureException, ErrorUpdateTimeFailed, ErrorSignatureCounterOverflow {
		//create a TLVobject for the old time 
		TLVObject oldTimeTLV = new TLVObject();
		oldTimeTLV.setTagWithByteElement((byte) 0x81);;
		oldTimeTLV.setValueWithLongElement(cryptoCore.getTimeAsUnixTime());
		
		//transform the old time tlv into a byte array
		byte[] oldTimeByteArray =null;
		
		//Try to transform the old time TLV into byte array. If that fails, ValueTooBig or ValueNull Exceptions are thrown
		oldTimeByteArray =oldTimeTLV.toTLVByteArray();
		
		
		//create a new time TLVobject
		TLVObject newTimeTLV = new TLVObject();
		newTimeTLV.setTagWithByteElement((byte) 0x82);
		
		//attempt to synchronize the time
		cryptoCore.setClock();
		
		//get the new time from the scp
		newTimeTLV.setValueWithLongElement(cryptoCore.getTimeAsUnixTime());
		
		byte[] newTimeByteArray = null;
		//Try to transform the new time TLV into byte array. If that fails, ValueTooBig or ValueNull Exceptions are thrown
		newTimeByteArray = newTimeTLV.toTLVByteArray();
		
		//concat the 2 byte arrays to create the systemOperationData
		byte[] systemOpData = Utils.concatTwoByteArrays(oldTimeByteArray, newTimeByteArray);
		
		SystemLogMessage updateTimesyslog = new SystemLogMessage("UpdateTime", systemOpData, this.serialNumber);
		updateTimesyslog.setAlgorithm(algorithmOID);
		
		byte[] upperUpdateTimesyslog = updateTimesyslog.toMinorTLVByteArray();
		byte[] lowerUpdateTimesyslog = cryptoCore.sign(upperUpdateTimesyslog);
		
		//concat the upper and the lower byte array of the systemlog message
		byte[] finishedUpdateTimesyslog = Utils.concatTwoByteArrays(upperUpdateTimesyslog, lowerUpdateTimesyslog);
		return finishedUpdateTimesyslog;
	}
	
	/**
	 * Creates a system log message which logs the usage of the disableSecureElement function and the time it was invoked (or rather, the time it was called
	 * in the ERSSpecificModule).
	 * The systemOperationdata is created according to BSI TR-03151 Appendix A.
	 * @return a SystemLogMessage encoded as an ASN1 TLV byte array.
	 * @throws SignatureException if the signing functionality of the CryptoCore fails for any reason. First approach for fixing this should be to 
	 * make sure, that the bit length of the order of the base point G of the chosen curve is equal or smaller than the output of the hash
	 * function. E.g. SHA512 with brainpoolP256r1 works, but SHA256 with brainpoolP512r1 does not.
	 * @throws ErrorSignatureCounterOverflow when incrementing the signature counter would result in an illegal value for that counter
	 * @throws ValueTooBigException is thrown by {@linkplain SystemLogMessage#toMinorTLVByteArray()} if a value is too big to be converted to
	 * an ASN1 DER encoded byte array.
	 * @throws ValueNullException  - is thrown by {@linkplain SystemLogMessage#toMinorTLVByteArray()} if a value that should be present is not.
	 * For example, a missing processType (processType == null) would cause such an Exception.
	 * @see CryptoCore#sign(byte[])
	 */
	public byte[] disableSecureElement() throws SignatureException, ErrorSignatureCounterOverflow, ValueNullException, ValueTooBigException {
		//create a TLVobject that logs the time the deactivation was started
		TLVObject systemOpDataTLV = new TLVObject();
		systemOpDataTLV.setTagWithByteElement((byte) 0x81);
		systemOpDataTLV.setValueWithLongElement(cryptoCore.getTimeAsUnixTime());
		//convert the sysop data to a TLV byte array
		byte[] systemOpData = null;
				try {
					systemOpData = systemOpDataTLV.toTLVByteArray();
				} catch (ValueNullException | ValueTooBigException e) {
					e.printStackTrace();
				}
		//create a systemLogMessage
		SystemLogMessage syslog = new SystemLogMessage("DisableSecureElement", systemOpData, this.serialNumber);
		syslog.setAlgorithm(algorithmOID);
		//create the upper part of the logmessage and then sign it, get the lower part of the logessage
		byte[] upperSyslog = syslog.toMinorTLVByteArray();
		byte[] lowersyslog = cryptoCore.sign(upperSyslog);
		//concat both byte arrays to get the finished syslog
		byte[] finishedSyslog = Utils.concatTwoByteArrays(upperSyslog, lowersyslog);
		return finishedSyslog;
	}

//TODO: if the TR is updated to assign an explicit value to the roles in the enumeration, this (and the equivalent in the classes above) have to be updated.	
	/**
	 * The ERSSpecificModule version of the authenticateUser method. Has to get the userID, the user's role and the result from the TSE or the SecurityModule. 
	 * The role is either "admin" , "timeAdmin" or <b>empty</b> if the userID is not managed by the secure element.
	 * The authentication result is logged as "0xFF" for TRUE and "0x00" for false.
	 * @param userID - the ID of the user attempting to authenticate itself
	 * @param role - the role that is associated with the user, as determined by the TSE or the SecurityModule. Note that it is an ASN1 enumerated.
	 * @param authenticationResult - is determined by the TSE or the SecurityModule and is either TRUE (for authenticated) or FALSE (for not authenticated).
	 * Is encoded as 0xff for true and 0x00 for false in the systemOperationData.
	 * @param userIdManagedByTSE - true if the user id that has invoked the authentication function exists and is managed by the
	 * TSE. False otherwise. Relevant for the creation of the roleTLVObject and the role byte array. If the userID is not managed by
	 * the TSE, the role object will only consist of a Tag 0x82 and a length of 0. <b>Could cause problems if this is not correctly interpreted as a null value!</b>
	 * @return a SystemLogMessage encoded as an ASN1 TLV byte array.
	 * @throws SignatureException if the signing functionality of the CryptoCore fails for any reason. First approach for fixing this should be to 
	 * make sure, that the bit length of the order of the base point G of the chosen curve is equal or smaller than the output of the hash
	 * function. E.g. SHA512 with brainpoolP256r1 works, but SHA256 with brainpoolP512r1 does not.
	 * @throws ErrorSignatureCounterOverflow when incrementing the signature counter would result in an illegal value for that counter
	 * @throws ValueTooBigException is thrown by {@linkplain SystemLogMessage#toMinorTLVByteArray()} if a value is too big to be converted to
	 * an ASN1 DER encoded byte array.
	 * @throws ValueNullException  - is thrown by {@linkplain SystemLogMessage#toMinorTLVByteArray()} if a value that should be present is not.
	 * For example, a missing processType (processType == null) would cause such an Exception.
	 * @see {@linkplain CryptoCore#sign(byte[])}
	 */
	public byte[] authenticateUser(String userID, int role, boolean authenticationResult, boolean userIdManagedByTSE) throws SignatureException, ErrorSignatureCounterOverflow, ValueNullException, ValueTooBigException {
		//create a TLVObject for the userID
		TLVObject userIDtlv = new TLVObject();
		userIDtlv.setTagWithByteElement((byte) 0x81);
		userIDtlv.setValue(userID.getBytes());
		
		//create a roleTLVbyteArray so it may be later integrated into the systemOperationData, no matter
		//if it is really present or not 
		byte[] roleTLVbyteArray = null;
		
		//check if the userID is managed by the TSE and therefore has a role assigned to it
		if(userIdManagedByTSE) {
			//create a TLVobject for the role
			TLVObject roleTLV = new TLVObject();
			roleTLV.setTagWithByteElement((byte) 0x82); 
			roleTLV.setValueWithIntegerElement(role);
			
			try {
				roleTLVbyteArray = roleTLV.toTLVByteArray();
			} catch (ValueNullException | ValueTooBigException e) {
				e.printStackTrace();
			}
		}
		else {
			//create a byte array of length 2
			roleTLVbyteArray = new byte[2];
			//set Tag = 0x82
			roleTLVbyteArray[0] = (byte) 0x82;
			//set the length = 0x00
			roleTLVbyteArray[1] = 0x00;
		}
		
		//create a TLVObject for the authenticationResult
		TLVObject authenticationResultTLV = new TLVObject();
		authenticationResultTLV.setTagWithByteElement((byte) 0x83);
		//if the authenticationResult is true (aka the user is authenticated) set the value to 0xFF. because of DER encoding, a new byte array has to be
		//created to set the value
		if(authenticationResult) {
			byte[] authResultValue = {(byte) 0xFF};
			authenticationResultTLV.setValue(authResultValue);
		}
		//if it is false, set the authenticationResult value to 0
		else {
			authenticationResultTLV.setValueWithIntegerElement(0);
		}
		
		//transform all the TLVObjects that are always present to TLV byte arrays
		byte[] userIDbyteArray = null;
		byte[] authenticationResultByteArray = null;
		try {
			userIDbyteArray = userIDtlv.toTLVByteArray();
			
			authenticationResultByteArray = authenticationResultTLV.toTLVByteArray();
		} catch (ValueNullException | ValueTooBigException e) {
			e.printStackTrace();
		}
		
		//concat the 3 byte arrays to get the system operation data
		byte[] systemOpData = Utils.concatAnyNumberOfByteArrays(userIDbyteArray, roleTLVbyteArray, authenticationResultByteArray);
		
		//create a SystemLogmessage
		SystemLogMessage syslog = new SystemLogMessage("AuthenticateUser",systemOpData, this.serialNumber);
		syslog.setAlgorithm(algorithmOID);
		byte[] upperAuthUserSyslog = syslog.toMinorTLVByteArray();
		byte[] lowerAuthUserSyslog = cryptoCore.sign(upperAuthUserSyslog);
		
		//concat the 2 byte arrays representing the system log message 
		byte[] finishedSyslog = Utils.concatTwoByteArrays(upperAuthUserSyslog, lowerAuthUserSyslog);
		return finishedSyslog;
	}
	
	
	
	/**
	 * The LogOut method in the ERSSpecificModule that creates the systemOperationData for the SystemLogMessage from the userID and the logOutCase.
	 * If the userID is managed by the TSE or not has to be determined at a higher level. This method should only be called if the userID exists and
	 * is managed by the TSE.
	 * @param userID - the ID of the user attempting to be logged out
	 * @param logOutCase - indicates who initiated the logout process. Note that it is an ASN1 enumerated.
	 * @return a SystemLogMessage encoded as an ASN1 TLV byte array.
	 * @throws SignatureException if the signing functionality of the CryptoCore fails for any reason. First approach for fixing this should be to 
	 * make sure, that the bit length of the order of the base point G of the chosen curve is equal or smaller than the output of the hash
	 * function. E.g. SHA512 with brainpoolP256r1 works, but SHA256 with brainpoolP512r1 does not.
	 * @throws ErrorSignatureCounterOverflow when incrementing the signature counter would result in an illegal value for that counter
	 * @throws ValueTooBigException is thrown by {@linkplain SystemLogMessage#toMinorTLVByteArray()} if a value is too big to be converted to
	 * an ASN1 DER encoded byte array.
	 * @throws ValueNullException  - is thrown by {@linkplain SystemLogMessage#toMinorTLVByteArray()} if a value that should be present is not.
	 * For example, a missing processType (processType == null) would cause such an Exception.
	 * @see {@linkplain CryptoCore#sign(byte[])}
	 */
	public byte[] logOut(String userID, int logOutCase) throws SignatureException, ErrorSignatureCounterOverflow, ValueNullException, ValueTooBigException {
		//create a TLVObject for the userID
		TLVObject userIDtlv = new TLVObject();
		userIDtlv.setTagWithByteElement((byte) 0x81);
		userIDtlv.setValue(userID.getBytes());
		
		//create a TLVobject for the logOutCase
		TLVObject logOutCaseTLV = new TLVObject();
		logOutCaseTLV.setTagWithByteElement((byte) 0x82);
		logOutCaseTLV.setValueWithIntegerElement(logOutCase);
		
		//transform the TLVobjects to TLV encoded byte arrays
		byte[] userIDbyteArray = null;
		byte[] logOutCaseByteArray = null;
		try {
			userIDbyteArray = userIDtlv.toTLVByteArray();
			logOutCaseByteArray = logOutCaseTLV.toTLVByteArray();
		} catch (ValueNullException | ValueTooBigException e) {
			e.printStackTrace();
		}
		
		//concat the byte arrays to get the systemOperationData
		byte[] systemOpData = Utils.concatTwoByteArrays(userIDbyteArray, logOutCaseByteArray);
		
		//create the SystemLogMessage
		SystemLogMessage syslog = new SystemLogMessage("LogOut", systemOpData, this.serialNumber);
		syslog.setAlgorithm(algorithmOID);
		
		//create the upper and lower part of the systemLogMessage as a byte array
		byte[] upperLogOutSyslog = syslog.toMinorTLVByteArray();
		byte[] lowerLogOutSyslog = cryptoCore.sign(upperLogOutSyslog);
		
		//concat the upper and the lower part of the syslog
		byte[] finishedLogOutSyslog = Utils.concatTwoByteArrays(upperLogOutSyslog, lowerLogOutSyslog);
		
		return finishedLogOutSyslog;
	}
	
	/**
	 * Creates a SystemLogMessage to log the attempt to unblock a user. The log is created even if the userID is not managed by the TSE. In that case,
	 * the unblockResult is set to "unknownUserId". 
	 * @param userID the ID of the user attempting to be unblocked
	 * @param unblockResult indicates whether the user has been unblocked, the unblock failed, the userID is not managed by the TSE or there has been
	 * an error during the execution of the unblock functionality. Note that this is an ASN1 enumerated type.
	 * @return a SystemLogMessage encoded as an ASN1 TLV byte array.
	 * @throws SignatureException if the signing functionality of the CryptoCore fails for any reason. First approach for fixing this should be to 
	 * make sure, that the bit length of the order of the base point G of the chosen curve is equal or smaller than the output of the hash
	 * function. E.g. SHA512 with brainpoolP256r1 works, but SHA256 with brainpoolP512r1 does not.
	 * @throws ErrorSignatureCounterOverflow when incrementing the signature counter would result in an illegal value for that counter
	 * @throws ValueTooBigException is thrown by {@linkplain SystemLogMessage#toMinorTLVByteArray()} if a value is too big to be converted to
	 * an ASN1 DER encoded byte array.
	 * @throws ValueNullException  - is thrown by {@linkplain SystemLogMessage#toMinorTLVByteArray()} if a value that should be present is not.
	 * For example, a missing processType (processType == null) would cause such an Exception.
	 * @see {@linkplain CryptoCore#sign(byte[])}
	 */
	public byte[] unblockUser(String userID, int unblockResult) throws SignatureException, ErrorSignatureCounterOverflow, ValueNullException, ValueTooBigException {
		//create a TLVObject for the userID
		TLVObject userIdTLV = new TLVObject();
		userIdTLV.setTagWithByteElement((byte) 0x81);
		userIdTLV.setValue(userID.getBytes());
		
		//create a TLVobject for the unblock result
		TLVObject unblockResultTLV = new TLVObject();
		unblockResultTLV.setTagWithByteElement((byte) 0x82);
		unblockResultTLV.setValueWithIntegerElement(unblockResult);
		
		//transform both to ASN1 TLV byte arrays
		byte[] userIDbyteArray = null;
		byte[] unblockresultByteArray = null;
		try {
			userIDbyteArray = userIdTLV.toTLVByteArray();
			unblockresultByteArray = unblockResultTLV.toTLVByteArray();
		} catch (ValueNullException | ValueTooBigException e) {
			e.printStackTrace();
		}
		
		//concat the byte arrays to create the SystemOperationData
		byte[] systemOpData = Utils.concatTwoByteArrays(userIDbyteArray, unblockresultByteArray);
		
		//create the systemLogMessage
		SystemLogMessage syslog = new SystemLogMessage("UnblockUser", systemOpData, this.serialNumber);
		syslog.setAlgorithm(algorithmOID);
		
		//create the upper and the lower part of the SystemLogMessage as a ASN1 TLV byte array
		byte[] upperUnblockUserSyslog = syslog.toMinorTLVByteArray();
		byte[] lowerUnblockUserSyslog = cryptoCore.sign(upperUnblockUserSyslog);
		
		//concat the upper and lower syslog parts to form the finished system log encoded as an ASN1 byte array
		byte[] finishedUnblockUserSyslog = Utils.concatTwoByteArrays(upperUnblockUserSyslog, lowerUnblockUserSyslog);
		return finishedUnblockUserSyslog;
	}
	
//--------------------------------------------------------------------------------------------------
	/**
	 * Used for telling the TSEController how many transaction may simultaneously be opened at any given time. The internal value maxNumberOpenTransactions is determined 
	 * by the configuration file or the value of {@linkplain Constants#DEFAULT_MAX_NUMBER_OF_TRANSACTIONS}, in case of an invalid value in the config file.
	 * @return the internal value of maxNumberOpenTransactions
	 */
	public int getMaxNumberOpenTransactions() {
		return maxNumberOpenTransactions;
	}
	
	/**
	 * Used for telling the TSEController how many transactions are currently open. This is done via a call of transactionsOpen.size() from
	 * {@linkplain java.util.Map#size()}.
	 * @return the number of transactions currently considered open.
	 */
	public int getNumberOfOpenTransactions() {
		return transactionsOpen.size();
	}
	
	
	/**
	 * used for getting the current time so that it may be incorporated into the system log message created with an UpdateTime method
	 * @return
	 */
	public long getCurrentUnixTime() {
		return cryptoCore.getTimeAsUnixTime();
	}
	
	/**
	 * used for getting the algorithm oid that the ErsSpecificModule uses
	 * @return the algorithm oid in the "dot notation".
	 */
	public String getAlgorithmOID() {
		return this.algorithmOID;
	}
	
	/**
	 * Used to get the transaction counter so that it may be persisted.
	 * @return the current transaction counter
	 */
	long getCurrentTransactioncounter() {
		return this.transactionCounter;
	}
	
	/**
	 * Extracts the transactionNumber from a transaction logmessage encoded as a TLVObject array. The calling method must make sure that the 
	 * TLVObject array is a TransactionLogMessage. The method <b> does </b> check whether or not the TLVObject array is the SEQUENCE-wrapped version that the SecurityModule 
	 * produces or the plain version that the ErsSpecificModule produces.
	 * @return the transaction number of the logmessage that was passed in
	 */
	public static long getTransactionNumberFromTransactionLogmessage(TLVObject[] input) {
		TransactionLogMessage logmsg = null;
		
		//check if the input represents the SEQUENCE-wrapped version that the SecurityModule produces
		if(input[0].getTag().getTagContent()[0] == ASN1Constants.UNIVERSAL_SEQUENCE) {
			TLVObject[] inputWithoutWrapper = new TLVObject[input.length-1];
			System.arraycopy(input, 1, inputWithoutWrapper, 0, inputWithoutWrapper.length);
			try {
				logmsg = new TransactionLogMessage(inputWithoutWrapper);
			} catch (NullPointerException | TLVException | ValueNullException | ValueTooBigException e) {
				e.printStackTrace();
			}
		}
		
		else {
			try {
				logmsg = new TransactionLogMessage(input);
			} catch (NullPointerException | TLVException | ValueNullException | ValueTooBigException e) {
				e.printStackTrace();
			}
		}
		
		return logmsg.getTransactionNumber();
		
	}
	
	
	/**
	 * Obtains the transaction counters of the currently open transactions from the  Map<Long, TLVObject[]> transactionsOpen.
	 * Is used by {@linkplain TSEController#gracefulShutdown()} to close all remaining transactions via the usage of {@linkplain SecurityModule#finishTransaction(String, long, byte[], String, byte[])}.
	 * 
	 * @return a Set of transaction numbers of currently open transactions
	 * @since 1.5
	 */
	protected synchronized Set<Long> getOpenTransactionNumbers(){
		Set<Long> transactionNumberSet = transactionsOpen.keySet();
		
		return transactionNumberSet;
	}

}
