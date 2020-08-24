/**
 * 
 */
package main.java.de.bsi.tsesimulator.tse;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.PrivateKeyUsagePeriod;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.openssl.PEMParser;

import main.java.de.bsi.seapi.SEAPI;
import main.java.de.bsi.seapi.exceptions.ErrorInvalidTime;
import main.java.de.bsi.seapi.exceptions.ErrorUpdateTimeFailed;
import main.java.de.bsi.tsesimulator.constants.ASN1Constants;
import main.java.de.bsi.tsesimulator.constants.ConfigConstants;
import main.java.de.bsi.tsesimulator.exceptions.ECCException;
import main.java.de.bsi.tsesimulator.exceptions.ErrorSignatureCounterOverflow;
import main.java.de.bsi.tsesimulator.exceptions.IllegalValueLoadedException;
import main.java.de.bsi.tsesimulator.exceptions.TLVException;
import main.java.de.bsi.tsesimulator.exceptions.TR_03111_ECC_V2_1_Exception;
import main.java.de.bsi.tsesimulator.exceptions.ValueNullException;
import main.java.de.bsi.tsesimulator.exceptions.ValueTooBigException;
import main.java.de.bsi.tsesimulator.exceptions.VerifyingOperationFailedException;
import main.java.de.bsi.tsesimulator.preferences.PropertyValues;
import main.java.de.bsi.tsesimulator.tlv.TLVObject;
import main.java.de.bsi.tsesimulator.tse.crypto.ECDSA;
import main.java.de.bsi.tsesimulator.tse.crypto.ECSDSA;
import main.java.de.bsi.tsesimulator.tse.crypto.SignatureAlgorithm;
import main.java.de.bsi.tsesimulator.utils.Utils;

/**
 * This class represents the cryptographic service provider of the TSE.
 * It manages the chosen signature algorithm and uses it to create the signatures present in the log messages. 
 * It also parses the certificate corresponding to the private key of the TSE used for the signature creation to provide an answer to the question of whether 
 * this certificate has expired.
 * Additionally it is the guardian of the internal clock of the TSE which can not be accessed without using this CryptoCore.
 * <br>
 * Please note, that this class is <b>NOT</b> a cryptographic service provider as described in BSI-CC-PP-CSP! Hence, the name <i>CryptoCore</i> instead of <i>CryptoCore</i>.
 * Please be also aware of the fact, that this implementation may have flaws that could endanger a real TSE implementation and should not be seen a safe 
 * implementation for usage in projects other than this simulator.
 * 
 * @author dpottkaemper
 * @since 1.0
 */
public class CryptoCore {
	
	private long signatureCounter;				//signatureCounter counts up each time a signature has been created. Can count up to 9.223.372.036.854.775.808 - 1 signature operations.
	private Clock clock;						//internal clock of the CryptoCore
	
	private SignatureAlgorithm algorithm;		//stores the algorithm object that is used to create signatures
	
	private Date tseCertNotAfter;				//stores NotAfter value from TSE certificate 
	private Date tseCertNotBefore;				//stores NotBefore value from TSE certificate 
	private Date privKeyNotAfter;				//stores PrivateKeyUsagePeriod NotAfter value from TSE certificate, if that value is present
	private Date privKeyNotBefore;				//stores PrivateKeyUsagePeriod NotBefore value from TSE certificate, if that value is present

	
	/**
	 * The default constructor for the CryptoCore. It uses the config.properties file to determine which signature algorithm it shall use, then uses the chose 
	 * algorithm (either ECDSA or ECSDSA) to create a {@linkplain SignatureAlgorithm} object accordingly.
	 * This signature algorithm is then used to perform the signing operations.
	 * This constructor also sets the internal clock and the signature counter to the default values of the current system time and zero.
	 * 
	 * Additionally, the constructor also tries to parse the certificate corresponding to the private key of the TSE and store the relevant values in instance 
	 * variables. 
	 * @throws IOException if the keys for the ECSDSA or the ECDSA could not be found or parsed.
	 */
	public CryptoCore() throws IOException {
		if(PropertyValues.getInstance().getValue(ConfigConstants.CFG_TAG_SIGNATURE_ALGORITHM).equalsIgnoreCase(ConfigConstants.CFG_ENTRY_SIGNATURE_ALGORITHM_ECDSA)) {
			this.algorithm = new ECDSA();
		}
		else if(PropertyValues.getInstance().getValue(ConfigConstants.CFG_TAG_SIGNATURE_ALGORITHM).equalsIgnoreCase(ConfigConstants.CFG_ENTRY_SIGNATURE_ALGORITHM_ECSDSA)) {
			this.algorithm = new ECSDSA();
		}
		
		//initialize signature counter with zero if it is not loaded from a file
		this.signatureCounter= 0;//Long.MAX_VALUE - 1; 
		//set the clock to the system default 
		this.clock=Clock.system(ZoneId.systemDefault());	
		
		//parse TSE certificate to get Valid NotBefore and NotAfter plus PrivateKeyUsagePeriod if it exists
		parseTseCertificate();
	}

	/**
	 * The constructor that shall be used if the signature counter and the last time are to be loaded from a file.
	 * It checks if the signature counter is a legal, non-negative value.
	 * Like the default constructor, this one attempts to find and parse the certificate corresponding to the private key of the TSE. If this is successful, 
	 * the constructor then stores relevant values in instance variables for quick access.
	 * The primary usage of this constructor is by {@linkplain SecurityModule#SM(boolean, long, long, long)}
	 * @param loadedSigCntr the signature counter loaded from the persistence file.
	 * @param loadedUnixTime the time loaded from the persistence file
	 * @throws IOException if the keys for the ECSDSA or the ECDSA could not be found or parsed.
	 * @throws IllegalValueLoadedException if the signature counter loaded is illegal
	 */
	public CryptoCore(long loadedSigCntr, long loadedUnixTime) throws IOException, IllegalValueLoadedException{
		if(PropertyValues.getInstance().getValue(ConfigConstants.CFG_TAG_SIGNATURE_ALGORITHM).equalsIgnoreCase(ConfigConstants.CFG_ENTRY_SIGNATURE_ALGORITHM_ECDSA)) {
			this.algorithm = new ECDSA();
		}
		else if(PropertyValues.getInstance().getValue(ConfigConstants.CFG_TAG_SIGNATURE_ALGORITHM).equalsIgnoreCase(ConfigConstants.CFG_ENTRY_SIGNATURE_ALGORITHM_ECSDSA)) {
			this.algorithm = new ECSDSA();
		}
		//perform a check on the signature counter
		if(loadedSigCntr<0) {
			throw new IllegalValueLoadedException("Loaded signature counter " +loadedSigCntr +" illegal.");
		}
		this.signatureCounter = loadedSigCntr;
		
		//create a base clock that runs on the current time
		Clock baseclock = Clock.system(ZoneId.systemDefault());
		//create Duration objects of the baseClock time, the loaded time and the difference between them
		Duration loadedTimeDuration = Duration.ofSeconds(loadedUnixTime);
		Duration baseClockDuration = Duration.ofSeconds(baseclock.instant().getEpochSecond());
		Duration difference = loadedTimeDuration.minus(baseClockDuration);
		
		//create the crypto core clock as a clock from the base clock with difference offset
		this.clock = Clock.offset(baseclock, difference);
		
		//parse TSE certificate to get Valid NotBefore and NotAfter plus PrivateKeyUsagePeriod if it exists
		parseTseCertificate();
	}
	
	/**
	 * A method to get the current signature counter value from the CryptoCore.
	 * @return the signatureCounter value if it isn't negative
	 * @throws ErrorSignatureCounterOverflow if the signatureCounter value is negative. This should only happen, if the signatureCounter value overflows.
	 * @since 1.0
	 * @version 1.4
	 */
	public long getSignatureCounter() throws ErrorSignatureCounterOverflow {
		if(this.signatureCounter<0) {
			//throw new SignatureCounterException("The signature counter value is negative! This should only happen if the value exceeds Long.MAX_VALUE.");
			throw new ErrorSignatureCounterOverflow("The signature counter has entered an illegal state < 0 !");
		}
		return this.signatureCounter;
	}
	
	/**
	 * Calls the CryptoCore clock for the current time and converts it to UnixTime through the Clock.instant().getEpochSecond() method.
	 * @return the current UnixTime as a long.
	 */
	public long getTimeAsUnixTime() {
		return (clock.instant().getEpochSecond());
	}
	
	
	
	/**
	 * Takes a byte array, theoretically <b>any</b> byte array, adds a TLVObject containing the signature counter and one containing
	 * the log time, converts them both into TLV byte arrays, attaches them to the byte array it was given and then signs all of
	 * that.
	 * <br>For the signature creation it uses the {@linkplain SignatureAlgorithm} that has been configured. It returns the 
	 * byte arrays containing the signature counter, the log time and the signature itself.
	 * @param upperMessagePart the byte array that shall be signed
	 * @return 3 byte arrays concatenated, namely: the signature counter as a TLV byte array, the log time as a TLV byte array
	 * and the signature as a TLV byte array.
	 * @throws SignatureException if the underlying {@linkplain SignatureAlgorithm} throws an {@linkplain TR_03111_ECC_V2_1_Exception}.
	 * @throws ErrorSignatureCounterOverflow if the current signature counter value equals Long.MAX_VALUE - 1. This is requested by BSI TR-03153 chapter 4.1.
	 */
	public byte[] sign(byte[] upperMessagePart) throws SignatureException, ErrorSignatureCounterOverflow{
		//signature: only the signature as a byte array
		byte[] signatureValue = null;
		//the "bottom part" of a log message, to be returned to the ERSSpecificModule
		byte[] sigCntrLogtimeSignature = null;
		
		//check if the signature counter would overflow, if thats the case: throw ErrorIncrementSignatureCouner exception
		if((this.signatureCounter >= (Long.MAX_VALUE - 1)) || (this.signatureCounter < 0)) {
			throw new ErrorSignatureCounterOverflow();
		}
		
		//Increment the signature counter:
		this.signatureCounter++;
		
		//create a signatureCounter TLVObject that can convert itself into a DER encoded byte array
		TLVObject signatureCounterElement = new TLVObject();
		signatureCounterElement.setTagWithByteElement(ASN1Constants.UNIVERSAL_INTEGER);
		signatureCounterElement.setValueWithLongElement(signatureCounter);
		
		byte[] signatureCounterAsTLV = null;
		
		try {
			signatureCounterAsTLV = signatureCounterElement.toTLVByteArray();
		} catch (ValueNullException e) {
			e.printStackTrace();
		} catch (ValueTooBigException e) {
			e.printStackTrace();
		}
		
		//create a time stamp and convert it to a TLV byte array
		TLVObject logTimeElement = new TLVObject();
		logTimeElement.setTagWithByteElement(ASN1Constants.UNIVERSAL_INTEGER);
		logTimeElement.setValueWithLongElement(getTimeAsUnixTime());
		
		byte[] logTimeAsTLV = null;
		
		try {
			logTimeAsTLV = logTimeElement.toTLVByteArray();
		} catch (ValueNullException e) {
			e.printStackTrace();
		} catch (ValueTooBigException e) {
			e.printStackTrace();
		}
		
		//concatenate the upper half of the log message with the added parts from the crypto core so it can be signed
		byte[] toBeSigned = Utils.concatAnyNumberOfByteArrays(upperMessagePart, signatureCounterAsTLV, logTimeAsTLV);
		
		
		//sign the log message and save the data in the signature value byte array
		try {
			signatureValue = this.algorithm.sign(toBeSigned);
		} catch (TR_03111_ECC_V2_1_Exception e1) {
			throw new SignatureException("The algorithm failed its signing operation." +e1.getMessage());
		}
		
		//create a signatureTLV element that can be appended to sigCntrLogtimeSignature
		TLVObject signatureValueElement = new TLVObject();
		signatureValueElement.setTagWithByteElement(ASN1Constants.UNIVERSAL_OCTET_STRING);
		signatureValueElement.setValue(signatureValue);
		
		byte[] signatureAsTLV = null;
		
		try {
			signatureAsTLV=signatureValueElement.toTLVByteArray();
		} catch (ValueNullException e) {
			e.printStackTrace();
		} catch (ValueTooBigException e) {
			e.printStackTrace();
		}
		
		//concatenate what will be returned
		sigCntrLogtimeSignature=Utils.concatAnyNumberOfByteArrays(signatureCounterAsTLV,logTimeAsTLV, signatureAsTLV);
		
		//return the bottom part of the log message consisting of the signatureCounter ASN1_ByteArray, the logTime ASN1_byteArray and the 
		//signature ASN1_ByteArray
		return sigCntrLogtimeSignature;
	}
	
	
	/**
	 * Calls the {@link SignatureAlgorithm#verify(byte[], byte[])} method in its non-abstract implementation as defined by the signature algorithm used at runtime.
	 * @param signatureValue the byte array representing the signature one wants to verify
	 * @param unsignedValue the byte array representing the data which has been signed and corresponds to the signedValue
	 * @return true, if the signature could be verified. False otherwise.
	 * @throws VerifyingOperationFailedException if something happens inside the algorithm that causes the verification method to fail.
	 */
	public boolean isVerified(byte[] signatureValue, byte[] unsignedValue) throws VerifyingOperationFailedException {
		boolean isVerified = false;
		try {
			isVerified = this.algorithm.verify(signatureValue, unsignedValue);
		} catch (ECCException e) {
			throw new VerifyingOperationFailedException("The algorithm failed its verification operation." +e.getMessage());
		} catch (TR_03111_ECC_V2_1_Exception e) {
			throw new VerifyingOperationFailedException("The algorithm failed its verification operation." +e.getMessage());
		}
		return isVerified;
	}
	
	/**
	 * Provides access to the algorithm object used by this crypto core.
	 * @return the algorithm object
	 */
	public SignatureAlgorithm getAlgorithm() {
		return this.algorithm;
	}
	
	/**
	 * According to TR-03116-5 chapter 3.1.1 the serial number is calculated through hashing the public key used to verify the signatures 
	 * with a SHA-2 256 bit. This method performs the hashing. 
	 * @return the hash over the public key
	 */
	private byte[] calculateSerialNumber() {
		//get the public key that is used by the CryptoCore's signature algorithm
		PublicKey pubKeyAsKey = this.algorithm.getPublicKey();
		//save the public key as a TLV encoded byte array
		byte[] wholePubKeyInfoByte = pubKeyAsKey.getEncoded();
		//decode that TLV encoded byte array to a TLVObject array
		TLVObject[] wholePubKeyInfoTLV = null;
		try {
			wholePubKeyInfoTLV = TLVObject.decodeASN1ByteArrayToTLVObjectArray(wholePubKeyInfoByte);
		} catch (TLVException e1) {
			e1.printStackTrace();
		}
		
		//the last value in the TLVObject array should be the public key
		byte[] publicKeyValueWithLeadingZeroes = wholePubKeyInfoTLV[wholePubKeyInfoTLV.length-1].getValue();
		
		//from comparing the last value with the value openssl declares as the public key it's obvious that this public key is 
		//padded with leading zeroes and seems to always have a 04, signifying uncompressed encoding after the 00.
		//so let's get rid of those leading zeroes!
		byte[] publicKeyValue = new byte[publicKeyValueWithLeadingZeroes.length-1];
		System.arraycopy(publicKeyValueWithLeadingZeroes, 1, publicKeyValue, 0, publicKeyValue.length);
		
		
		//add the BC provider for good measure again (should already have been added, but better safe than sorry)
		Security.addProvider(new BouncyCastleProvider());
		MessageDigest md = null;
		try {
			//get a SHA256 message digest from the bouncycastle provider
			md = MessageDigest.getInstance("SHA256", "BC");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		}
		
		//feed the publicKeyValue to the message digest
		md.update(publicKeyValue);
		byte[] hashedPublicKey = md.digest();
		
		return hashedPublicKey;
	}
	
	/**
	 * Calculates the hash of the public key that is used to perform the verification of signatures and return that hash.
	 * @return the hash of the public key
	 */
	public byte[] getSerialNumber() {
		return calculateSerialNumber();
	}
	
	
	/**
	 * Sets the internal clock to a certain point in time. Gets called by the TSEController to provide the functionality of the {@linkplain SEAPI#updateTime(ZonedDateTime)}
	 * method.
	 * @param timeNew the new time in the Java ZonedDatetime format.
	 * @throws ErrorInvalidTime if the new time is before 1.1.2019 0:0:0 or after the beginning of the year 2100.
	 * @throws ErrorUpdateTimeFailed if the execution of this function fails due to any other reason
	 * @see {@linkplain java.time.ZonedDateTime}
	 * @since 1.0
	 */
	public synchronized void setClock(ZonedDateTime timeNew) throws ErrorInvalidTime, ErrorUpdateTimeFailed {
		try {
			//check if the new time is before or after a certain point in time. Necessary because no TSE should run prior to 2019 or after 2100. 
			//get the old time duration & the new time duration
			Duration oldTimeDuration = Duration.ofSeconds(this.clock.instant().getEpochSecond());
			Duration newTimeDuration = Duration.ofSeconds(timeNew.toInstant().getEpochSecond());
			
			//calculate the difference between new time and old time and store that
			//if the durationOffset is negative, the clock is being set backwards, otherwise it is being set to a future point in time
			Duration durationOffset = newTimeDuration.minus(oldTimeDuration);
			
			//store time temporarily in a new Clock object so that the newly set time can be checked BEFORE it is really set
			Clock newTime =  Clock.offset(this.clock, durationOffset);
			//check if timeNew is too early or too late. Really, if you are using this code after 2100, I thank you for having so much faith in my little program but, maybe, just maybe, you should use a newer technology?
			if((newTime.instant().isBefore(Instant.ofEpochSecond(1546297200L))) || (newTime.instant().isAfter(Instant.ofEpochSecond(4102444800L)))) {
				throw new ErrorInvalidTime("Time is not in 1.01.2019 0:0:0 < timeNew < 1.01.2100 0:0:0. Time has not been updated!");
			}
			//else set the new time and exit
			this.clock = newTime;
			return;
		} catch(ErrorInvalidTime e) {
			throw new ErrorInvalidTime(e.getMessage(), e.getCause());
		} catch(Exception e2) {
			throw new ErrorUpdateTimeFailed(e2.getMessage(), e2.getCause());
		}
	}
	
	
	/**
	 * Sets the internal clock to the current system clock with the default system time zone. Gets called by the TSEController to provide
	 * the functionality of the {@linkplain SEAPI#updateTime()} method.
	 * Should never raise an exception because the system time should always have a valid value. 
	 * @throws ErrorUpdateTimeFailed if the execution of this function fails
	 */
	public synchronized void setClock() throws ErrorUpdateTimeFailed {
		try {
			this.clock = Clock.systemDefaultZone();
			return;
		} catch(Exception e) {
			throw new ErrorUpdateTimeFailed(e.getMessage(), e.getCause());
		}
	}

	/**
	 * Hashes a byte array using the BouncyCastle implementation of the SHA-256 algorithm. Is used to hash user data related content.
	 * @param toBeHashed the byte array that shall be hashed
	 * @return the result as a byte array
	 */
	public byte[] hashByteArray(byte[] toBeHashed) {
		//SHA-2 256 seems to be a good idea. Because of this being only a simulator, we do not use any salt, although that would be a good idea.
		Security.addProvider(new BouncyCastleProvider());
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA256", "BC");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		}
		//prepare an array big enough to store the result in
		byte[] hashedValue = new byte[256];
		
		hashedValue = md.digest(toBeHashed);
		
		return hashedValue;
	}
	
	
//----------------------------------------CERTIFICATE CHECK---------------------------------------------------------------------------------------
	/**
	 * This function is used to check, whether the TSE certificate corresponding to the private key that is used for signing the log messages is still valid.
	 * The main class to use this function is {@linkplain TSEController} for all the functions that may raise an {@linkplain ErrorCertificateExpired} exception.
	 * 
	 * The check for validity is performed against the values of the fields <i>Validity</i> and the extension <i>PrivateKeyUsagePeriod</i>, if the certificate contains this 
	 * extension. 
	 * @return false, if the certificate is <b>not expired</b>, true otherwise.
	 */
	public boolean isCertificateExpired() {
		//Get current internal CryptoCore time. getTimeAsUnixTime can not be used for constructing Date, because Date expects a time in milliseconds.
		Date currentDate = new Date(this.clock.millis());
		
		//tseCertNotBefore < currentDate < tseCertNotAfter
		boolean wholeCertValid = ((currentDate.after(tseCertNotBefore)) && (currentDate.before(tseCertNotAfter)));
		//if PrivateKeyUsagePeriod extension exists in the certificate, take that into consideration
		if((privKeyNotBefore != null) && (privKeyNotAfter != null)) {
			boolean privKeyPeriodValid = ((currentDate.after(privKeyNotBefore)) && (currentDate.before(privKeyNotAfter)));
			//invert, because the parameters say if the cert is valid, and the return value says if it's expired
			return !(wholeCertValid && privKeyPeriodValid);
		}
		else {
			return (!wholeCertValid);
		}
	}
	
	/**
	 * This function parses the certificate corresponding to the private key of the TSE to store relevant values for later use.
	 * Those values are needed for the function {@linkplain #isCertificateExpired()}.
	 * @throws IOException if something prevents the function to read or parse the certificate correctly, or {@linkplain PropertyValues#getInstance()} throws it.
	 * @version 1.5
	 */
	private void parseTseCertificate() throws IOException {
		//get a Java File object and create an X509CertificateHolder for later use 
		String pathToTseCertFile = PropertyValues.getInstance().getPathToKeyDir()+File.separator+PropertyValues.getInstance().getValue(ConfigConstants.CFG_TAG_TSE_CERT);
		File tseCertFile = new File(pathToTseCertFile);
		X509CertificateHolder tseCert = null;
		
		if(PropertyValues.getInstance().getValue(ConfigConstants.CFG_TAG_TSE_CERT_ENCODING).equalsIgnoreCase(ConfigConstants.CFG_ENTRY_TSE_CERT_ENCODING_DER)) {
			//if the certificate file is DER encoded, use X509CertificateHolder(byte[] certEncoding) in org.bouncycastle.cert.X509CertificateHolder
			//since its documentation says to enter a DER or BER encoded certificate as bytes
			Path tseCertFilePath = Paths.get(tseCertFile.toURI());
			byte[] tseCertByteArray = Files.readAllBytes(tseCertFilePath);	//throws IOException (reads whole certificate file into byte array)
			tseCert = new X509CertificateHolder(tseCertByteArray);
		}	
		if(PropertyValues.getInstance().getValue(ConfigConstants.CFG_TAG_TSE_CERT_ENCODING).equalsIgnoreCase(ConfigConstants.CFG_ENTRY_TSE_CERT_ENCODING_PEM)) {
			//use org.bouncycastle.openssl.PEMParser to read a PEM encoded certificate. That PEMParser needs a java.io.Reader in its constructor
			FileReader tseCertIn = new FileReader(tseCertFile);
			PEMParser pemParser = new PEMParser(tseCertIn);
			
			tseCert = (X509CertificateHolder) pemParser.readObject();
			//close the resources
			pemParser.close();
		}
		if(tseCert == null) {
			throw new IOException("Certificate could not be read. Possible fault: encoding info in config.properties not correct.");
		}
		
		//save the values for NotBefore and NotAfter from the certificate
		tseCertNotBefore = tseCert.getNotBefore();
		tseCertNotAfter = tseCert.getNotAfter();
		//get private key usage period extension from the certificate (info: privateKeyUsagePeriod OID = 2.5.29.16)
		Extension privKeyUsagePeriodExtension = tseCert.getExtension(Extension.privateKeyUsagePeriod);
		//check if PrivateKeyUsagePeriod exists for the parsed certificate. It exists, if privKeyUsagePeriodExtension != null
		if(privKeyUsagePeriodExtension != null) {
			//create an ASN1EncodableObject from the Extension
			ASN1Encodable privKeyUsagePeriodEncodable = privKeyUsagePeriodExtension.getParsedValue();
			
			//use the ASN1Encodable to create an object of type PrivateKeyUsagePeriod, for easy access to the 
				//PrivateKeyUsagePeriod privKeyUsagePeriod = PrivateKeyUsagePeriod.getInstance(privKeyUsagePeriodExtension); //this throws an exception 
			PrivateKeyUsagePeriod privKeyUsagePeriod = PrivateKeyUsagePeriod.getInstance(privKeyUsagePeriodEncodable);
			
			//get the "not before" and "not after" fields
			ASN1GeneralizedTime privKeyNotBeforeGent = privKeyUsagePeriod.getNotBefore();
			ASN1GeneralizedTime privKeyNotAfterGent = privKeyUsagePeriod.getNotAfter(); 
					
			try {
				privKeyNotBefore = privKeyNotBeforeGent.getDate();
				privKeyNotAfter = privKeyNotAfterGent.getDate();				
			} catch (ParseException e) {
				throw new IOException("Parsing failed!", e);
			}
		}
		
		
	}

}
