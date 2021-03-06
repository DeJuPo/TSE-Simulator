package main.java.de.bsi.tsesimulator.msg;

import main.java.de.bsi.tsesimulator.constants.Constants;

/**
 * The generic log message consisting of the following:<br>
 * <ul>
 * <li>version</li>
 * <li>certified datatype</li>
 * <li>serial number</li>
 * <li>algorithm</li>
 * <li>parameters</li>
 * <li>signature counter</li>
 * <li>log time</li>
 * <li>signature counter</li>
 * </ul>
 * A detailed description of the purpose and possible value(s) of each of these parameters can be found in BSI TR-03151.
 * @author dpottkaemper
 * @since 1.0
 *
 */
public class LogMessage {

	private int version;
	private String certifiedDatatype;
	private byte[] serialNumber=null;
	private String algorithm;
	private byte[] parameters=null;
	private long signatureCounter=Constants.ILLEGAL_SIGNATURE_COUNTER;
	private long logTime=Constants.ILLEGAL_LOG_TIME;
	private byte[] signaturevalue=null;
	
	/**
	 * Default constructor. Not really used, because only the subclasses {@linkplain TransactionLogMessage} and {@linkplain SystemLogMessage}.
	 */
	public LogMessage() {}
	
	//---------------------------------------------------all the getter & setter methods--------------------------------------------------------------------------
	/**
	 * Getter for the version.
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * Setter for the version.
	 * @param version the version to be set
	 */
	public void setVersion(int version) {
		this.version = version;
	}

	/**
	 * Getter for the certified data type.
	 * @return the certified data type
	 */
	public String getCertifiedDatatype() {
		return certifiedDatatype;
	}

	/**
	 * Setter for the certified data type.
	 * @param certifiedDatatype the certified data type, an OID.
	 */
	public void setCertifiedDatatype(String certifiedDatatype) {
		this.certifiedDatatype = certifiedDatatype;
	}

	/**
	 * Getter for the serial number.
	 * @return the serial number
	 */
	public byte[] getSerialNumber() {
		return serialNumber;
	}

	/**
	 * Setter for the serial number.
	 * @param serialNumber the serial number to be set
	 */
	public void setSerialNumber(byte[] serialNumber) {
		this.serialNumber = serialNumber;
	}

	/**
	 * Getter for the algorithm.
	 * @return the algorithm, an OID
	 */
	public String getAlgorithm() {
		return algorithm;
	}

	/**
	 * Used by the {@linkplain ERSSpecificModule} to set the algorithm OID of each log message. Since the log message does not know the algorithm oid without the 
	 * ERSSpecificModule, the algorithm is not set in the constructors of log messages.
	 * @param algorithm the algorithm object identifier
	 */
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	/**
	 * Getter for the algorithm parameters.
	 * @return the algorithm parameters, if any are present.
	 */
	public byte[] getParameters() {
		return parameters;
	}

	/**
	 * Setter for the algorithm parameters.
	 * @param parameters a byte array of algorithm parameters
	 */
	public void setParameters(byte[] parameters) {
		this.parameters = parameters;
	}

	/**
	 * Sets the signatureCounter value for a log message as generated by the {@linkplain CryptoCore}.
	 * @param signatureCounter: the new value for the signatureCounter of this log message
	 * @return the set signatureCounter value
	 */
	public long setSignatureCounter(long signatureCounter) {
		this.signatureCounter=signatureCounter;
		return this.signatureCounter;
	}
	
	/**
	 * Getter for the signature counter.
	 * @return the signature counter
	 */
	public long getSignatureCounter() {
		return this.signatureCounter;
	}
	
	/**
	 * Sets the logTime value for a log message as generated by the {@linkplain CryptoCore}
	 * @param logTime: the new value for the logTime of this message
	 * @return the newly set logTime value
	 */
	public long setLogTime(long logTime) {
		this.logTime = logTime;
		return this.logTime;
	}
	
	/**
	 * Getter for the log time.
	 * @return the log time, as Unix time. 
	 */
	public long getLogTime() {
		return this.logTime;
	}
	
	/**
	 * Sets the signatureValue for a log message as generated by the {@linkplain CryptoCore}
	 * @param signatureValue: the new value for the signatureValue of this message
	 * @return the set signatureValue
	 */
	public byte[] setSignatureValue(byte[] signatureValue) {
		this.signaturevalue=signatureValue;
		return this.signaturevalue;
	}
	
	/**
	 * Getter for the signature value.
	 * @return the signature value.
	 */
	public byte[] getSignatureValue() {
		return this.signaturevalue;
	}
}
