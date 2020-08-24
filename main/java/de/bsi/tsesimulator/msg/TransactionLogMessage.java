package main.java.de.bsi.tsesimulator.msg;

import main.java.de.bsi.tsesimulator.constants.ASN1Constants;
import main.java.de.bsi.tsesimulator.constants.Constants;
import main.java.de.bsi.tsesimulator.exceptions.TLVException;
import main.java.de.bsi.tsesimulator.exceptions.ValueNullException;
import main.java.de.bsi.tsesimulator.exceptions.ValueTooBigException;
import main.java.de.bsi.tsesimulator.tlv.ObjectIdentifier;
import main.java.de.bsi.tsesimulator.tlv.TLVObject;
import main.java.de.bsi.tsesimulator.tlv.TLVUtility;
import main.java.de.bsi.tsesimulator.tse.ERSSpecificModule;
import main.java.de.bsi.tsesimulator.utils.Utils;


/**
 * Represents a transaction log message for the simulator. Is used by {@linkplain ERSSpecificModule} to create log messages logging transactions according to 
 * BSI TR-03151. The parameters managed by this class are the following: <br>
 * 
 * <ul>
 * <li>operation type: type of the operation this log, e.g. "StartTransaction"</li>
 * <li>client ID: the ID of the client invoking a start, update or finish of the transaction</li>
 * <li>process data: the original data that shall be logged, provided externally</li>
 * <li>process type: type of the process that is being logged</li>
 * <li>additional external data: optional data, provided externally</li>
 * <li>transaction number: the number of the transaction that is being logged</li>
 * <li>additional internal data: optional internal data</li>
 * </ul>
 * The value of each of these parameters is determined by the technical guideline mentioned above.
 * @author dpottkaemper
 *
 */
public class TransactionLogMessage extends LogMessage {
	//initialize all variables with default values
	
	//if any time in the future the unsigned update variant is implemented, this needs a version where processType is not mandatory. Until then, it is mandatory. 
	private String operationType;					
	private String clientID;
	private byte[] processData=null;
	private String processType;
	private byte[] additionalExtData=null;
	private long transactionNumber=Constants.ILLEGAL_TRANSACTION_NUMBER;
	private byte[] additionalIntData=null;
	
	//-----------------------------Constructors-------------------------------------------------------------------
	/**
	 * Default constructor that sets everything that can be set without the need for additional data.
	 * That means, it <b>only</b> sets the version and the certifiedDataType and everything else has to be set through the setter methods.
	 * Using this constructor is not recommended, the others are far better.
	 */
	public TransactionLogMessage() {
		this.setVersion(Constants.VERSION);								//version is universal for all log messages
		this.setCertifiedDatatype(Constants.TRANSACTION_LOG_OID);		//certifiedDataType is the OID, for all transaction logs it is the same
		//algorithm parameter has to be set using the LogMessage setAlgorithm method.
		//the ERSSpecificModule has to set the algorithm
		//serial number has to be set by someone who has access to it 
	}
	
	
	/**
	 * Constructs a TransactionLogMessage and sets all the data that can be set. Main constructor to later be used by the ERSSpecificModule and the TSEController to construct Transaction Log Messages.
	 * The operationType and the algorithm have to be set separately.
	 * @param clientID the ID of the client that initiated the logging of this process.
	 * @param processData the data of the process that shall be logged with a TransactionLogMessage.
	 * @param processType the type of the process that is being logged. 
	 * @param additionalExternalData reserved for future use, but could contain any additional data.
	 */
	public TransactionLogMessage(String clientID, byte[] processData, String processType, byte[] additionalExternalData, long transactionNumber, byte[] serialNumber) {
		this.setVersion(Constants.VERSION);								//version is universal for all log messages
		this.setCertifiedDatatype(Constants.TRANSACTION_LOG_OID);		//certifiedDataType is the OID, for all transaction logs it is the same
		this.setSerialNumber(serialNumber); 			//serial number has to be set by someone who has access to it (ERSSpecificModule or SecurityModule)
		
		//algorithm parameter has to be set using the LogMessage setAlgorithm method.
		//the ERSSpecificModule has to set the algorithm
		
		//set everything provided by the ERSSpecificModule:
		this.clientID=clientID;
		this.processData=processData;
		this.processType=processType;
		if(additionalExternalData!=null) {
			this.additionalExtData=additionalExternalData;
		}
		this.transactionNumber=transactionNumber;						
	}
	
	/**
	 * Constructs a TransactionLogMessage from a TLVObject[]. Is to be used to retrieve information like the signatureCounter without 
	 * having to go through a byte array. The calling method has to make sure that the TLVObjects in the array are in the correct order 
	 * for a TransactionLogMessage and that the TLVobjects are meant to represent the content of a Transaction Log.
	 * Checks the length of the input array to determine if the array is too short or too long to be converted into a transaction log.
	 * These values might change over time if the TR-03151 or the TR-03151 changes.
	 * <br>Please be aware of the fact, that the TLVObject[] has to represent the TransactionLogMessage in its "data only form". The first
	 * data present has to be the version, <b>not</b> the "wrapped-SEQUENCE" object that the SecurityModule produces.
 	 * @param input a transaction log message in its representation as a TLVObject[] without the SEQUENCE-wrapper.
	 * @throws ValueTooBigException gets thrown by the conversion methods {@linkplain TLVUtility#asn1Value_ByteArrayToInteger(byte[])} and {@linkplain TLVUtility#asn1Value_ByteArrayToLong(byte[])}.
	 * @throws ValueNullException gets thrown by the conversion methods {@linkplain TLVUtility#asn1Value_ByteArrayToInteger(byte[])} and {@linkplain TLVUtility#asn1Value_ByteArrayToLong(byte[])}.
	 * @throws NullPointerException if the input array is empty
	 * @throws TLVException if the length of the TLVObject array is too long or to short to be converted into a TransactionLogMessage
	 */
	public TransactionLogMessage(TLVObject[] input) throws NullPointerException, TLVException, ValueNullException, ValueTooBigException {
		//check if the input array is null
		if(input == null) {
			throw new NullPointerException("An empty array can not be converted to a TransactionLogMessage!");
		}
		//check if the input array is long enough to contain the information expected in a transaction log message
		//version+OID+operationType+clientID+processData+processType+transactionNumber+serialNumber+SEQUENCE signatureAlgorithm+algorithm+
			//signatureCounter+log time+signature = 13 minimum
		if((input.length < 13) ||(input.length > 16)) {
			throw new TLVException("input array length:\t" +input.length +"\trequired 13 <= input length <= 16\n");
		}
		//read the value of the first TLVObject and convert it into an int to set the version 
		this.setVersion(TLVUtility.asn1Value_ByteArrayToInteger(input[0].getValue()));
		
		//read the OID from the second TLVObjct to set the certifiedDataType
		this.setCertifiedDatatype(ObjectIdentifier.convertTLVValueToOID(input[1].getValue()));
		
		//create a new String from the value of the TLVObject representing the operationType
		String operationType = new String(input[2].getValue());
		this.setOperationtype(operationType);
		
		//create a new String for the clientID
		String clientid = new String(input[3].getValue());
		this.setClientID(clientid);
		
		//set the process data from input[4]
		this.setProcessData(input[4].getValue());
		
		//set the process type via new String
		String ptype = new String(input[5].getValue());
		this.setProcessType(ptype);
		
			//the values above are always present in a Transaction Log Message.
			//the following TLVObjects have to be checked for their Tag to determine which value they represent 
			//for example, one does not know if input[6] is additional external data or the transaction number
		//introduce an index for the input. index starts at 6 because that's the first entry in input which could be one of two data types
		int index = 6;
		

		//check if input[6] is additional external data 
		//do not forget to cast the 0x84 to byte because then the comparison fails!
		if(input[index].getTag().getTagContent()[0] == ((byte) 0x84)) {
			this.additionalExtData = input[index].getValue();
			//update the index
			index++;
		}
		
		//next entry in input has to be the transaction number
		this.setTransactionNumber(TLVUtility.asn1Value_ByteArrayToLong(input[index].getValue()));
		index++;
		
		//if the entry after the transaction number is additional internal data, set the internal data
		if(input[index].getTag().getTagContent()[0] == ((byte)0x86)) {
			this.setAdditionalIntData(input[index].getValue());
			index++;
		}
		
		//set the serial number
		this.setSerialNumber(input[index].getValue());
		index++;
		
		//get the algorithm from the SEQUENCE signature algorithm
		++index;	//because there is one TLVObject that represents the whole SEQUENCE
		//use the ObjectIdentifier method to convert the algorithm OID back into a normal String
		String algo = new String(ObjectIdentifier.convertTLVValueToOID(input[index].getValue()));
		this.setAlgorithm(algo);
		index++;
		
		//get the signature counter
		this.setSignatureCounter(TLVUtility.asn1Value_ByteArrayToLong(input[index].getValue()));
		index++;
		
		//get the log time. currently only Unix time as a long supported
		this.setLogTime(TLVUtility.asn1Value_ByteArrayToLong(input[index].getValue()));
		index++;
		
		//get the signature value
		this.setSignatureValue(input[index].getValue());
	}
	
	//-----------------------------------GETTER METHODS---------------------------------------------------
	/**
	 * Getter for the transaction number
	 * @return the transaction number
	 */
	public long getTransactionNumber() {
		return this.transactionNumber;
	}
	
	/**
	 * Getter for the operation type.
	 * @return the operation type, e.g. "StartTransaction".
	 */
	public String getOperationType() {
		return this.operationType;
	}
	
	/**
	 * Getter for the client ID
	 * @return the client ID
	 */
	public String getClientID() {
		return this.clientID;
	}
	
	//-----------------------------------SETTER METHODS----------------------------------------------------
	/**
	 * Setter for the operation type
	 * @param operationType the type of operation creating the log message
	 */
	public void setOperationtype(String operationType) {
		this.operationType=operationType;
	}
	
	/**
	 * Setter for the client ID.
	 * @param clientID the ID of the client invoking the logging process
	 */
	public void setClientID(String clientID) {
		this.clientID=clientID;
	}
	
	/**
	 * Setter for the process data.
	 * @param processData the process data in a byte array form
	 */
	public void setProcessData(byte[] processData) {
		this.processData=processData;
	}
	
	/**
	 * Setter for the process type.
	 * @param processtype the type of process that resulted in the log creation.
	 */
	public void setProcessType(String processtype) {
		this.processType = processtype;
	}
	
	/**
	 * Setter for the additional external data. 
	 * @param additionalExtData a byte array of additional external data.
	 */
	public void setAdditionalExtData(byte[] additionalExtData) {
		this.additionalExtData=additionalExtData;
	}
	
	/**
	 * Setter for the transaction number. 
	 * @param transactionNumber the transaction number of the log message.
	 */
	public void setTransactionNumber(long transactionNumber) {
		this.transactionNumber=transactionNumber;
	}
	
	/**
	 * Setter for the additional internal data. 
	 * @param additionalIntData a byte array of additional internal data
	 */
	public void setAdditionalIntData(byte[] additionalIntData) {
		this.additionalIntData=additionalIntData;
	}
	
	
	
	
	//---------------------------------------------------------------------TO BYTE ARRAY METHODS-----------------------------------------------
	
	/**
	 * Converts the TransactionLogMessage into its ASN.1 DER encoded form using {@linkplain TLVObject#toTLVByteArray()}. It assumes that every value that should be 
	 * present in a transaction log message is present, since it is only used by the simulator. Classes higher up the hierarchy have to make sure, that every 
	 * value is present. <br>
	 * The missing values signature counter, log time and signature value are provided by the {@linkplain CryptoCore} which uses the result of this very function 
	 * as input for the signature creation. 
	 * The assembly of the whole log message in its TLV byte array form is performed by {@linkplain ERSSpecificModule}. 
	 * @return a byte array of the log message containing everything except the signature counter, the log time and the signature value.
	 * @throws ValueNullException if an (instance) parameter that should be present is not and therefore this Exception is thrown when trying to convert that
	 * particular value to a TLV byte array via {@linkplain TLVObject#toTLVByteArray()}.
	 * @throws ValueTooBigException if the value of an (instance) parameter is too long to have its length encoded according to the ASN1 DER rules,
	 * {@linkplain TLVObject#toTLVByteArray()} throws this exception.
	 */
	public byte[] toMinorTLVByteArray() throws ValueNullException, ValueTooBigException {
		byte[] transactionLogAsMinorByteArray = null;
		//non elegant handling of the optional parameter problem. Should nevertheless be quicker than several if(something==null) checks in the method body
		boolean additionalExtDataPresent = !(additionalExtData==null);
		boolean additionalIntDataPresent = !(additionalIntData==null);
		
		//Version to TLV
		//UNIVERSAL 2 INTEGER
		TLVObject versionElement=new TLVObject();
		versionElement.setTagWithByteElement(ASN1Constants.UNIVERSAL_INTEGER);
		versionElement.setValueWithIntegerElement(this.getVersion());
		
		//certifiedDataType to TLV
		//UNIVERSAL 6 OBJECT IDENTIFIER
		TLVObject certifiedDataTypeElement= new TLVObject();
		certifiedDataTypeElement.setTagWithByteElement(ASN1Constants.UNIVERSAL_OBJECT_INDENTIFIER);
		certifiedDataTypeElement.setValue(ObjectIdentifier.convertOIDtoTLVValue(this.getCertifiedDatatype()));	//conversion to value delegated to
		
		//operationType to TLV
		//Context Specific IMPLICIT 0 PRINTABLE STRING
		TLVObject operationTypeElement = new TLVObject();
		operationTypeElement.setTagWithIntegerElement(0x80);
		operationTypeElement.setValue(operationType.getBytes());		
		
		//clientID to TLV
		//Context Specific IMPLICIT 1 PRINTABLE STRING
		TLVObject clientIDElement = new TLVObject();
		clientIDElement.setTagWithIntegerElement(0x81);
		clientIDElement.setValue(clientID.getBytes()); 		
		
		//processData to TLV
		//Context Specific IMPLICIT 2 OCTET STRING
		TLVObject processDataElement = new TLVObject();
		processDataElement.setTagWithIntegerElement(0x82);
		processDataElement.setValue(processData);
		
		//processType to TLV
		//Context Specific IMPLICIT 3 PRINTABLE STRING
		TLVObject processTypeElement = new TLVObject();
		processTypeElement.setTagWithIntegerElement(0x83);
		processTypeElement.setValue(processType.getBytes());	
		
		//if additional external data present, convert to TLV
		TLVObject additionalExtDataElement = new TLVObject();
		if(additionalExtDataPresent) {
			//additionalExtData to TLV
			//Context Specific IMPLICIT 4 OCTET STRING
			additionalExtDataElement.setTagWithIntegerElement(0x84);
			additionalExtDataElement.setValue(additionalExtData);
		}

		//transactionNumber to TLV
		//Context Specific IMPLICIT 5 INTEGER
		TLVObject transactionNumberElement = new TLVObject();
		transactionNumberElement.setTagWithIntegerElement(0x85);
		transactionNumberElement.setValueWithLongElement(transactionNumber);   
		
		//if additional internal data present, convert to TLV
		TLVObject additionalIntDataElement = new TLVObject();
		if(additionalIntDataPresent) {
			//additionalIntData to TLV
			//Context Specific IMPLICIT 6 OCTET STRING
			
			additionalIntDataElement.setTagWithIntegerElement(0x86);
			additionalIntDataElement.setValue(additionalIntData);
		}
		
		//serialNumber to TLV
		//UNIVERSAL 4 OCTET STRING
		TLVObject serialNumberElement = new TLVObject();
		serialNumberElement.setTagWithByteElement(ASN1Constants.UNIVERSAL_OCTET_STRING);
		serialNumberElement.setValue(this.getSerialNumber());
		
		//signatureAlgorithm SEQUENCE to TLV
		//UNIVERSAL 16 constructed SEQUENCE
			TLVObject signatureAlgorithmElement=new TLVObject();
			signatureAlgorithmElement.setTagWithIntegerElement(0x30);	
			
			//algorithm to TLV
			//UNIVERSAL 6 OBJECT IDENTIFIER
			TLVObject algorithmElement = new TLVObject();
			algorithmElement.setTagWithByteElement(ASN1Constants.UNIVERSAL_OBJECT_INDENTIFIER);
			//get the algorithm from the ERSSpecificModule and convert its OID to asn1
			algorithmElement.setValue(ObjectIdentifier.convertOIDtoTLVValue(this.getAlgorithm()));
			
			//THERE ARE NO PARAMETERS YET
			// Append algorithm to signatureAlgorithm TLV
	        try {
				signatureAlgorithmElement.appendChild(algorithmElement);
			} catch (TLVException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ValueNullException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ValueTooBigException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	       
	    //create all the byte arrays to hold the values
	        byte[] versionTLV=null;
	        byte[] certifiedDataTypeTLV =null;
			byte[] operationTypeTLV = null;
			byte[] clientIDTLV = null;
			byte[] processDataTLV = null;
			byte[] processTypeTLV = null;
			byte[] additionalExtDataTLV = null;		
			byte[] transactionNumberTLV = null;
			byte[] additionalIntDataTLV = null;
			byte[] serialNumberTLV = null;
			byte[] signatureAlgorithmTLV = null;
	    
		//create correct TLV byte arrays for each TLV
		try {
			versionTLV = versionElement.toTLVByteArray();
			certifiedDataTypeTLV = certifiedDataTypeElement.toTLVByteArray();
			operationTypeTLV = operationTypeElement.toTLVByteArray();
			clientIDTLV = clientIDElement.toTLVByteArray();
			processDataTLV = processDataElement.toTLVByteArray();
			processTypeTLV = processTypeElement.toTLVByteArray();
			//additional external data may or may not be present
			if(additionalExtDataPresent) {
				additionalExtDataTLV = additionalExtDataElement.toTLVByteArray();
			}
			transactionNumberTLV = transactionNumberElement.toTLVByteArray();
			//additional internal data may or may not be present
			if(additionalIntDataPresent) {
				 additionalIntDataTLV = additionalIntDataElement.toTLVByteArray();
			}
			serialNumberTLV = serialNumberElement.toTLVByteArray();
			//how to build the Signature algorithm element?
			signatureAlgorithmTLV = signatureAlgorithmElement.toTLVByteArray();		
		} catch (ValueNullException e) {
			throw new ValueNullException("The value of a TLVObject was not set and could not be converted to a TLV byte array");
		} catch (ValueTooBigException e) {
			throw new ValueTooBigException("The value of a TLVObject was too long to be ASN1 DER encoded. The TLVObject could not be converted to a TLV byte array");
		}
		
		//if no additional data present:
		if((!additionalExtDataPresent) && (!additionalIntDataPresent)) {
			transactionLogAsMinorByteArray = Utils.concatAnyNumberOfByteArrays(versionTLV, certifiedDataTypeTLV, operationTypeTLV,
					clientIDTLV, processDataTLV, processTypeTLV, transactionNumberTLV, serialNumberTLV, signatureAlgorithmTLV);
		}
		//if no additionalIntData present:
		else if(additionalExtDataPresent && (!additionalIntDataPresent)) {
			transactionLogAsMinorByteArray = Utils.concatAnyNumberOfByteArrays(versionTLV, certifiedDataTypeTLV, operationTypeTLV,
					clientIDTLV, processDataTLV, processTypeTLV, additionalExtDataTLV, transactionNumberTLV, serialNumberTLV, signatureAlgorithmTLV);
		}
		//if no additionalExtData present
		else if((!additionalExtDataPresent) && additionalIntDataPresent) {
			transactionLogAsMinorByteArray = Utils.concatAnyNumberOfByteArrays(versionTLV, certifiedDataTypeTLV, operationTypeTLV,
					clientIDTLV, processDataTLV, processTypeTLV, transactionNumberTLV, additionalIntDataTLV, serialNumberTLV, signatureAlgorithmTLV);
		}
		//if both additional data present
		else if(additionalExtDataPresent&&additionalIntDataPresent) {
			transactionLogAsMinorByteArray = Utils.concatAnyNumberOfByteArrays(versionTLV, certifiedDataTypeTLV, operationTypeTLV,
					clientIDTLV, processDataTLV, processTypeTLV, additionalExtDataTLV, transactionNumberTLV, additionalIntDataTLV, serialNumberTLV, 
					signatureAlgorithmTLV);
		}
		return transactionLogAsMinorByteArray;
	}	
	
//----------------------------------------------------------TO STRING METHODS--------------------------------------------------
	
	/**
	 * Returns a String representation of a transaction log message object. It only returns the content of each parameter. If the log message
	 * should be represented in its TLV form, it is required that it is turned into a TLV byte array and that
	 *  {@linkplain main.java.de.bsi.tsesimulator.tlv.TLVObject#tlvObjectArrayToString(TLVObject[])} is used.
	 */
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append("Version:\t\t" +this.getVersion() +"\n");
		str.append("certifiedDataType:\t" +this.getCertifiedDatatype() +"\n");
		str.append("operationType:\t\t" +this.operationType +"\n");
		str.append("clientID:\t\t" +this.clientID +"\n");
		str.append("processData:\t\t" +TLVUtility.byteArrayToString(processData) +"\n");
		str.append("processtype:\t\t" +this.processType +"\n");
		
		//check if additional ext data present
		if(!(additionalExtData == null)) {
			str.append("additionalExternalData:\t" +TLVUtility.byteArrayToString(additionalExtData) +"\n");
		}
		str.append("transactionNumber:\t" +this.transactionNumber +"\n");
		
		//check if additional int data present
		if(!(additionalIntData==null)) {
			str.append("additionalInternaldata:\t" +TLVUtility.byteArrayToString(additionalIntData) +"\n");
		}
		
		str.append("serialNumber:\t\t" +TLVUtility.byteArrayToString(getSerialNumber()) +"\n");
		str.append("Algorithm:\t\t" +this.getAlgorithm() +"\n");
		
		//check if its a complete logmessage or just the upper part
		if(!(this.getSignatureCounter() == Constants.ILLEGAL_SIGNATURE_COUNTER)) {
			str.append("Signature Counter:\t" +this.getSignatureCounter() +"\n");
			str.append("Log Time:\t\t" +this.getLogTime() +"\n");
			str.append("Signature:\t\t" +TLVUtility.byteArrayToString(this.getSignatureValue()));
		}
		
		return str.toString();
	}
}
