package main.java.de.bsi.tsesimulator.msg;

import main.java.de.bsi.tsesimulator.constants.ASN1Constants;
import main.java.de.bsi.tsesimulator.constants.Constants;
import main.java.de.bsi.tsesimulator.exceptions.TLVException;
import main.java.de.bsi.tsesimulator.exceptions.ValueNullException;
import main.java.de.bsi.tsesimulator.exceptions.ValueTooBigException;
import main.java.de.bsi.tsesimulator.tlv.ObjectIdentifier;
import main.java.de.bsi.tsesimulator.tlv.TLVObject;
import main.java.de.bsi.tsesimulator.tlv.TLVUtility;
import main.java.de.bsi.tsesimulator.tse.SecurityModule;
import main.java.de.bsi.tsesimulator.tse.CryptoCore;
import main.java.de.bsi.tsesimulator.tse.ERSSpecificModule;
import main.java.de.bsi.tsesimulator.utils.Utils;


/**
 * Represents a system log message for the simulator. Is used by {@linkplain ERSSpecificModule} to create system log messages for those events that have to be logged 
 * according to BSI TR-03151.<br>
 * The parameters managed by this class are the operation type, the system operation data and additional internal data. The value of each of these parameters is determined 
 * by the same technical guideline. 
 * @author dpottkaemper
 * 
 */
public class SystemLogMessage extends LogMessage {
	private String operationType;			//mandatory
	private byte[] systemOperationData;		//conditional (mandatory except in initialize() )
	private byte[] additionalIntData;		//optional

	//--------------------------------------CONSTRUCTORS-------------------------------------------------------------------------------
	/**
	 * The default constructor for a SystemLogMessage. Sets only the two parameters it can set independently, which are the version
	 * and the certifiedDataType. Everything else has to be set using the setter methods. It is recommended that the other
	 * constructors are used.
	 */
	public SystemLogMessage() {
		this.setVersion(Constants.VERSION);								//version is universal for all log messages
		this.setCertifiedDatatype(Constants.SYSTEM_LOG_OID);			//certifiedDataType is the OID, for all transaction logs it is the same
		
		//algorithm parameter has to be set using the LogMessage setAlgorithm method.
		//the ERSSpecificModule has to set the algorithm
		//serial number has to be set by someone who has access to it 
	}
	
	/**
	 * Constructor for a SystemLogMessage when the operationType and the serial number are known.
	 * In that case, these parameters are assigned with the values that were passed to this constructor and in addition,
	 * the version and certifiedDataType are set. The algorithm value has to be set manually.
	 * @param operationType the name of the operation that this SystemLogMessage logs. For example "UpdateTime".
	 * @param serialNumber serialNumber the serial number of the TSE. 
	 */
	public SystemLogMessage(String operationType,  byte[] serialNumber) {
		this.setVersion(Constants.VERSION);								//version is universal for all log messages
		this.setCertifiedDatatype(Constants.SYSTEM_LOG_OID);			//certifiedDataType is the OID, for all transaction logs it is the same
		this.setSerialNumber(serialNumber); 			//serial number has to be set by someone who has access to it (ERSSpecificModule or SecurityModule)
		
		//algorithm parameter has to be set using the LogMessage setAlgorithm method.
		//the ERSSpecificModule has to set the algorithm
		
		this.operationType=operationType;
	}
	
	/**
	 * Constructor for a SystemLogMessage when the operationType, the systemOperationData and the serialNumber are known.
	 * In that case, these parameters are assigned with the values that were passed to this constructor and in addition,
	 * the version and certifiedDataType are set. The algorithm value has to be set manually.
	 * @param operationType the name of the operation that this SystemLogMessage logs. For example "UpdateTime".
	 * @param systemOperationData the data that has to be logged with this SystemLogMessage, encoded as a TLV byte array as specified 
	 * by BSI TR-03151.
	 * @param serialNumber the serial number of the TSE. 
	 */
	public SystemLogMessage(String operationType, byte[] systemOperationData, byte[] serialNumber) {
		this.setVersion(Constants.VERSION);								//version is universal for all log messages
		this.setCertifiedDatatype(Constants.SYSTEM_LOG_OID);			//certifiedDataType is the OID, for all transaction logs it is the same
		this.setSerialNumber(serialNumber); 			//serial number has to be set by someone who has access to it (ERSSpecificModule or SecurityModule)
		
		//algorithm parameter has to be set using the LogMessage setAlgorithm method.
		//the ERSSpecificModule has to set the algorithm
		
		this.operationType=operationType;
		this.systemOperationData=systemOperationData;
	}
	
	
	/**
	 * Constructs a SystemLogMessage from a TLVObject[]. This is used to retrieve information like the signatureCounter from the signed 
	 * LogMessage without having to parse the whole TLV byte array each time. Instead, the byte array is decoded into a TLVObject[] via
	 * {@linkplain TLVObject#decodeASN1ByteArrayToTLVObjectArray(byte[])}. <br> The calling method has to make sure that the 
	 * TLVObject[] is without the Sequence-wrapper that the {@linkplain SecurityModule} attaches. The calling method has to also ensure,
	 * that the TLVobject[] does indeed represent a SystemLogMessage and that the content of that SystemLogMessage is in the correct order.
	 * Since SystemLogMessages differ very much in the amount of systemOperationData, this method only checks if the input is null. It
	 * does not check for minimum or maximum length like the same constructor in the {@linkplain TransactionLogMessage}.
	 * @param input a SystemLogMessage in its representation as a TLVObject[] without the SEQUENCE-wrapper.
	 * @throws ValueTooBigException gets thrown by the conversion methods {@linkplain TLVUtility#asn1Value_ByteArrayToInteger(byte[])} and {@linkplain TLVUtility#asn1Value_ByteArrayToLong(byte[])}.
	 * @throws ValueNullException gets thrown by the conversion methods {@linkplain TLVUtility#asn1Value_ByteArrayToInteger(byte[])} and {@linkplain TLVUtility#asn1Value_ByteArrayToLong(byte[])}.
	 * @throws NullPointerException if the input array is empty
	 * @see TransactionLogMessage#TransactionLogMessage(TLVObject[])
	 */
	public SystemLogMessage(TLVObject[] input) throws ValueNullException, ValueTooBigException, NullPointerException {
		if(input == null) {
			throw new NullPointerException("An empty array can not be converted to a SystemLogMessage!");
		}
		//1st entry should be the version
		this.setVersion(TLVUtility.asn1Value_ByteArrayToInteger(input[0].getValue()));
		
		//2nd entry should be the OID of the syslog
		this.setCertifiedDatatype(ObjectIdentifier.convertTLVValueToOID(input[1].getValue()));
		
		//3rd entry is the operation type in the certifiedData. create a string and then set the operation type with it
		String operationType = new String(input[2].getValue());
		this.setOperationType(operationType);
		
		//the order of the values above is always the same in every system log message.
			//the next value could be the conditional systemOperationData
			//this is present in every SystemLog EXCEPT one created by initialize()
		int index = 3;
		
		//check if input[3] is the systemOperationData which itself is encoded as a TLV byte array
//4th entry is the systemOperationData which itself is encoded as a TLV byte array. This should not be a problem
		if((input[index].getTag().getTagContent()[0]) == ((byte)0x81)) {
			this.setSystemOperationData(input[index].getValue());
			//update the index
			index++;
		}
		
		
//the order of the values above is always the same in every system log message.
	//the next value could be the optional additional internal data
//int index = 4;
		
		//check if input[index] is additionalInternalData
		if((input[index].getTag().getTagContent()[0]) == ((byte)0x82)) {
			this.setAdditionalInternalData(input[index].getValue());
			//update the index
			index++;
		}
		
		//next entry should be the serial number
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
		
		//get the log time. currently only unix time as a long supported
		this.setLogTime(TLVUtility.asn1Value_ByteArrayToLong(input[index].getValue()));
		index++;
		
		//get the signature value
		this.setSignatureValue(input[index].getValue());
	}
	
	
	
	//--------------------------------------SETTER METHODS-------------------------------------------------------------------------------
	/**
	 * Setter for the operation type.
	 * @param operationType the type of operation, e.g. "updateTime".
	 */
	public void setOperationType(String operationType) {
		this.operationType=operationType;
	}
	
	/**
	 * Setter for the system operation data. This data has to be encoded as an ASN.1 DER structure. It has to be structured the way the technical guideline 
	 * wants it to be structured.
	 * @param sysOpdata a byte array of system operation data. Depends on the operation that it logs. 
	 */
	public void setSystemOperationData(byte[] sysOpdata) {
		this.systemOperationData=sysOpdata;
	}
	
	/**
	 * Setter for additional internal data. Is optional.
	 * @param additionalIntData internal data structured as a BER encoded TLV object. 
	 */
	public void setAdditionalInternalData(byte[] additionalIntData) {
		this.additionalIntData=additionalIntData;
	}
	
	//--------------------------------------GETTER METHODS-------------------------------------------------------------------------------
	/**
	 * Getter for the operation type.
	 * @return the operation type
	 */
	public String getOperationType() {
		return this.operationType;
	}
	
	/**
	 * Getter for the system operation data.
	 * @return the system operation data in its ASN.1 TLV form.
	 */
	public byte[] getSystemOperationData() {
		return this.systemOperationData;
	}
	
	/**
	 * Getter for the additional internal data.
	 * @return the additional internal data in its TLV form, or null, if none is present. 
	 */
	public byte[] getAdditionalInternalData() {
		return this.additionalIntData;
	}
	
	//--------------------------------------TO BYTE ARRAY METHODS-------------------------------------------------------------------------------
	
	/**
	 * Converts the SystemLogMessage into its ASN.1 DER encoded form using {@linkplain TLVObject#toTLVByteArray()}. It assumes that every value that should be 
	 * present in a SystemLogMessage conforming to BSI TR-03151 is present. This is because it is only used to create SystemLogMessages in the context of this simulator 
	 * and the calling classes provide all the necessary values. <br>
	 * The missing values, signature counter, log time and signature value, can only be provided by the {@linkplain CryptoCore} after the upper part of the log message 
	 * has been converted into this TLV byte array. Hence the name <i>MinorTLVByteArray</i>, since it is not yet complete. The assembly of the whole log message in its 
	 * TLV byte array form is performed by {@linkplain ERSSpecificModule}. 
	 * @return the upper part of the SystemLogMessage in its ASN.1 DER encoded form.
	 * @throws ValueNullException if an (instance) parameter that should be present is not and therefore this Exception is thrown when trying to convert that
	 * particular value to a TLV byte array via {@linkplain TLVObject#toTLVByteArray()}.
	 * @throws ValueTooBigException if the value of an (instance) parameter is too long to have its length encoded according to the ASN1 DER rules,
	 * {@linkplain TLVObject#toTLVByteArray()} throws this exception.
	 */
	public byte[] toMinorTLVByteArray() throws ValueNullException, ValueTooBigException {
		byte[] systemLogAsMinorByteArray = null;
		
		//non elegant handling of the optional/conditional parameter problem. Should nevertheless be quicker than several if(something==null) checks in the method body
		boolean systemOperationDataPresent = !(systemOperationData==null);
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
		
		//if systemOperationData present:
		TLVObject systemOperationDataElement = new TLVObject();
		if(systemOperationDataPresent) {
			//systemOperationData to TLV
			//Context Specific IMPLICIT 1 OCTET STRING
			systemOperationDataElement.setTagWithIntegerElement(0x81);
			systemOperationDataElement.setValue(systemOperationData);
		}
		
		//if additional internal data present
		TLVObject additionalIntDataElement = new TLVObject();
		if(additionalIntDataPresent) {
			additionalIntDataElement.setTagWithIntegerElement(0x82);
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
				e1.printStackTrace();
			} catch (ValueNullException e1) {
				e1.printStackTrace();
			} catch (ValueTooBigException e1) {
				e1.printStackTrace();
			}
	        
	      //create all the byte arrays to hold the values
	        byte[] versionTLV=null;
	        byte[] certifiedDataTypeTLV =null;
			byte[] operationTypeTLV = null;
			byte[] systemOperationDataTLV = null;
			byte[] additionalIntDataTLV = null;
			byte[] serialNumberTLV = null;
			byte[] signatureAlgorithmTLV = null;
			
		//convert every value present to a TLV byte array	
			try {
				versionTLV = versionElement.toTLVByteArray();
				certifiedDataTypeTLV = certifiedDataTypeElement.toTLVByteArray();
				operationTypeTLV = operationTypeElement.toTLVByteArray();
				//systemOperationData is conditional
				if(systemOperationDataPresent) {
					systemOperationDataTLV = systemOperationDataElement.toTLVByteArray();
				}
				//additional internal data is optional
				if(additionalIntDataPresent) {
					additionalIntDataTLV=additionalIntDataElement.toTLVByteArray();
				}
				
				serialNumberTLV=serialNumberElement.toTLVByteArray();
				signatureAlgorithmTLV=signatureAlgorithmElement.toTLVByteArray();
			} catch (ValueNullException e) {
				throw new ValueNullException("The value of a TLVObject was not set and could not be converted to a TLV byte array");
			} catch (ValueTooBigException e) {
				throw new ValueTooBigException("The value of a TLVObject was too long to be ASN1 DER encoded. The TLVObject could not be converted to a TLV byte array");
			}
			
			//depending on what values are present, concatenate the TLV byte arrays into one byte array
			//if systemOperationData present and NO additionalIntData present:
			if(systemOperationDataPresent && (!additionalIntDataPresent)) {
				systemLogAsMinorByteArray=Utils.concatAnyNumberOfByteArrays(versionTLV, certifiedDataTypeTLV, operationTypeTLV, systemOperationDataTLV,
						serialNumberTLV, signatureAlgorithmTLV);
			}
			//if NO systemOperationData present and NO additionalIntData present:
			else if((!systemOperationDataPresent) && (!additionalIntDataPresent)) {
				systemLogAsMinorByteArray=Utils.concatAnyNumberOfByteArrays(versionTLV, certifiedDataTypeTLV, operationTypeTLV, 
						serialNumberTLV, signatureAlgorithmTLV);
			}
			//if systemOperationOperationData present and additionalIntData present
			else if(systemOperationDataPresent && additionalIntDataPresent) {
				systemLogAsMinorByteArray=Utils.concatAnyNumberOfByteArrays(versionTLV, certifiedDataTypeTLV, operationTypeTLV, systemOperationDataTLV,
						additionalIntDataTLV, serialNumberTLV, signatureAlgorithmTLV);
			}
			//if NO systemOperationOperationData present and additionalIntData present
			else if((!systemOperationDataPresent) && additionalIntDataPresent) {
				systemLogAsMinorByteArray=Utils.concatAnyNumberOfByteArrays(versionTLV, certifiedDataTypeTLV, operationTypeTLV, 
						additionalIntDataTLV,serialNumberTLV, signatureAlgorithmTLV);
			}
/*			
if(!additionalIntDataPresent) {
	systemLogAsMinorByteArray=Utils.concatAnyNumberOfByteArrays(versionTLV, certifiedDataTypeTLV, operationTypeTLV, systemOperationDataTLV,
			serialNumberTLV, signatureAlgorithmTLV);
}
else{
	systemLogAsMinorByteArray=Utils.concatAnyNumberOfByteArrays(versionTLV, certifiedDataTypeTLV, operationTypeTLV, systemOperationDataTLV,
			additionalIntDataTLV,serialNumberTLV, signatureAlgorithmTLV);
}
*/	
			return systemLogAsMinorByteArray;
	}
	
	//------------------------------------------------TO STRING METHODS--------------------------------------------------------

	/**
	 * Returns a String representation of a system log message object. It only returns the content of each parameter. If the log message
	 * should be represented in its TLV form, it is required that it is turned into a TLV byte array and that
	 *  {@linkplain main.java.de.bsi.tsesimulator.tlv.TLVObject#tlvObjectArrayToString(TLVObject[])} is used.
	 */
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append("Version:\t\t" +this.getVersion() +"\n");
		str.append("certifiedDataType:\t" +this.getCertifiedDatatype() +"\n");
		str.append("operationType:\t\t" +this.operationType +"\n");
		//check if systemOperationData present
		if(!(systemOperationData==null)) {
			str.append("systemOperationData\t" +TLVUtility.byteArrayToString(systemOperationData) +"\n");
		}
		//check if additional int data present
		if(!(additionalIntData==null)) {
			str.append("additionalInternaldata:\t" +TLVUtility.byteArrayToString(additionalIntData) +"\n");
		}
		
		str.append("serialNumber:\t\t" +TLVUtility.byteArrayToString(getSerialNumber()) +"\n");
		str.append("Algorithm:\t\t" +this.getAlgorithm() +"\n");
		
		//check if its a complete log message or just the upper part
		if(!(this.getSignatureCounter() == Constants.ILLEGAL_SIGNATURE_COUNTER)) {
			str.append("Signature Counter:\t" +this.getSignatureCounter() +"\n");
			str.append("Log Time:\t\t" +this.getLogTime() +"\n");
			str.append("Signature:\t\t" +TLVUtility.byteArrayToString(this.getSignatureValue()));
		}
		
		return str.toString();
	}
}
