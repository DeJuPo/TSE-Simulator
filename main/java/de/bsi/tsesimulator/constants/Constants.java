package main.java.de.bsi.tsesimulator.constants;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import main.java.de.bsi.tsesimulator.tse.TSEController;

/**
 * Class that provides values that likely will not change in the future for all classes of the TSE simulator. Values that are subject to constant change by the 
 * user of the simulator can be configured using a configuration file. Please note, that some of the provided values are default values that are only used as a 
 * fallback solution for when the user has provided illegal values in the configuration file.<br><br>
 * 
 * Warning: Javadoc may not represent regular expressions appropriately due to tagging issues. Refer to source code for actual regular expressions if necessary!
 * @author dpottkaemper
 *
 */
public abstract class Constants {		
	//custom constants for graceful shutdown
	/**
	 * Value: <b>GracefulShutdownClient</b><br>
	 * ClientId of the dummy-client that is used to close open transactions during the graceful shutdown.<br>
	 * @see {@linkplain TSEController#gracefulShutdown()}
	 */
	public static final String GRACEFUL_SHUTDOWN_CLIENTID = "GracefulShutdownClient";
	
	/**
	 * Value: <b>{0x00}</b><br>
	 * Dummy-processData used to close open transactions during a graceful shutdown.<br>
	 * @see {@linkplain TSEController#gracefulShutdown()}
	 */
	public static final byte[] GRACEFUL_SHUTDOWN_PROCESSDATA = {0x00};
	
	/**
	 * Value: <b>GracefulShutdownProcess</b><br>
	 * Dummy-processType used to close open transactions during graceful shutdown.<br>
	 * @see {@linkplain TSEController#gracefulShutdown()}
	 */
	public static final String GRACEFUL_SHUTDOWN_PROCESSTYPE = "GracefulShutdownProcess";
	
	
	//version of log message format and update times
	/**
	 * Value: <b>2</b><br>
	 * Represents the current version of the log message format
	 */
	public static final int VERSION = 2;
	
	/**
	 * Value: <b>45</b><br>
	 * Maximum time in seconds after which UpdateTransaction must be called, if process data has changed.
	 * Currently unused.
	 */
	public static final int MAX_UPDATE_DELAY = 45;
	
	/**
	 * Value: <b>45</b><br>
	 * Maximum time in seconds after which the SE must secure the process data if no other UpdateTransaction or FinishTransaction has been called.
	 * Currently unused.
	 */
	public static final int MAX_PROTECTION_DELAY = 45;

	//maintenance related constants/constants for the info.csv file
	/**
	 * Value: <b>TSE-Sim-Company TSE-1337</b><br>
	 * Default value to be used, when the description of the SE API has already been set by the manufacturer.
	 */
	public static final String DEFAULT_DESCRIPTION_OF_SEAPI = "TSE-Sim-Company TSE-1337";
	
	/**
	 * Value: <b>MANUFACTURER-INFO</b><br>
	 * Default value for the manufacturer information, to be inserted into info.csv file by TSEController
	 */
	public static final String DEFAULT_MANUFACTURER_INFORMATION = "MANUFACTURER-INFO";
	
	/**
	 * Value: <b>TSE-Sim-DevVersion</b><br>
	 * Default value for the version information, to be inserted into info.csv file by TSEController.
	 */
	public static final String DEFAULT_VERSION_INFORMATION = "TSE-Sim-DevVersion";
	
	//Usermanagement related constants
	/**
	 * Value: <b>5</b><br>
	 * Maximum number of users that can be managed by the TSE. Just a restriction so that someone
	 * using the simulator can not add as many users as they want.
	 */
	public static final short MAX_STORED_USERS = 5;
	
	/**
	 * Value: <b>100</b><br>
	 * The maximum length of a userId is hereby defined as 100 characters.
	 */
	public static final int MAX_USERID_LENGTH = 100;
	
	/**
	 * Value: <b>3</b><br>
	 * The maximum amount of retries for each log-in attempt
	 */
	public static final short MAX_RETRIES = 3;
	
		//values related to the enum datatype in chapter 6.4-6.6 in BSI TR-03151 Version 1.0.1
	/**
	 * Value: <b>0</b><br>
	 */
	public static final int AUTHENTICATEUSER_ROLE_ADMIN = 0;
	
	/**
	 * Value: <b>1</b><br>
	 */
	public static final int AUTHENTICATEUSER_ROLE_TIMEADMIN = 1;
	
	/**
	 * Value: <b>2</b><br>
	 * Currently logged as 0x82 0x00, the same an ASN.1 Null value would be. Could change in the future.
	 */
	public static final int AUTHENTICATEUSER_ROLE_UNKNOWNUSERID = 2;
	
	
	/**
	 * Value: <b>0</b><br>
	 */
	public static final int LOGOUT_LOGOUTCAUSE_USER = 0;
	
	/**
	 * Value: <b>1</b><br>
	 */
	public static final int LOGOUT_LOGOUTCAUSE_TIMEOUT = 1;
	
	
	/**
	 * Value: <b>0</b><br>
	 */
	public static final int UNBLOCKUSER_UNBLOCKRESULT_OK = 0;
	
	/**
	 * Value: <b>1</b><br>
	 */
	public static final int UNBLOCKUSER_UNBLOCKRESULT_FAILED = 1;
	
	/**
	 * Value: <b>2</b><br>
	 */
	public static final int UNBLOCKUSER_UNBLOCKRESULT_UNKNOWNUSERID = 2;	
	
	/**
	 * Value: <b>2</b><br>
	 */
	public static final int UNBLOCKUSER_UNBLOCKRESULT_ERROR = 3;
	
	//Utility function related constants
	/**
	 * Value: <b>8</b><br>
	 * Default maximum number of clients that can use the functionality of the SE API simultaneously. 
	 * Used only in case the config.properties file contains an illegal value.
	 */
	public static final int DEFAULT_MAX_NUMBER_OF_CLIENTS = 8;
	
	/**
	 * Value: <b>512</b><br>
	 * Default maximum number of transactions that can be opened simultaneously on the SE API.
	 * Used only in case the config.properties file contains an illegal value.
	 */
	public static final int DEFAULT_MAX_NUMBER_OF_TRANSACTIONS = 512;
	
	//custom viable times (used for checking the zonedDateTime values against in TSEController)
	/**
	 * EARLIEST_LEGAL_TIME is 1.1.2019 0:00. The time zone used is the one returned by {@linkplain ZoneId#systemDefault()}.
	 */
	public static final ZonedDateTime EARLIEST_LEGAL_TIME = ZonedDateTime.ofInstant(Instant.ofEpochSecond(1546297200L), ZoneId.systemDefault());
	/**
	 * LATEST_LEGAL_TIME is 1.1.2100 12:00. The time zone used is the one returned by {@linkplain ZoneId#systemDefault()}.
	 */
	public static final ZonedDateTime LATEST_LEGAL_TIME = ZonedDateTime.ofInstant(Instant.ofEpochSecond(4102444800L), ZoneId.systemDefault());
	
	
	//Custom default values for testing purposes
	/**
	 * Value: <b>-9000</b><br>
	 * Initial value of each transaction number in each transaction log. Is used for testing purposes, to see if the assignment of transaction numbers works as intended.
	 */
	public static final long ILLEGAL_TRANSACTION_NUMBER = -9000;
	
	/**
	 * Value: <b>-9001</b><br>
	 * Initial value of each signature counter in each log. Is used for testing purposes, to see if the assignment of signature counters works as intended.
	 */
	public static final long ILLEGAL_SIGNATURE_COUNTER = -9001;
	
	/**
	 * Value: <b>-9002</b><br>
	 * Initial value of each log time in each log. Is used for testing purposes, to see if the assignment of log times works as intended.
	 */
	public static final long ILLEGAL_LOG_TIME = -9002;				

	//Logmessage Object Identifiers as constants
	/**
	 * Value: <b>0.4.0.127.0.7.3.7.1.1</b><br>
	 */
	public static final String TRANSACTION_LOG_OID = "0.4.0.127.0.7.3.7.1.1";	//OID transaction log from bsi-de applications sE-API sE-API-dataformats
	
	/**
	 * Value: <b>0.4.0.127.0.7.3.7.1.2</b><br>
	 */
	public static final String SYSTEM_LOG_OID = "0.4.0.127.0.7.3.7.1.2";		//OID system log from bsi-de applications sE-API sE-API-dataformats
	
	/**
	 * Value: <b>0.4.0.127.0.7.3.7.1.3</b><br>
	 */
	public static final String AUDIT_LOG_OID = "0.4.0.127.0.7.3.7.1.3";			//OID audit log from bsi-de applications sE-API sE-API-dataformats
	
	
	//Algorithm Object Identifiers as constants
		//ECDSA {0, 4 , 0 , 127 , 0 , 7 , 1 , 1 , 4 , 1}
		//ECDSA_PLAIN_SHA_224 is mentioned in TR-03151 but TR-03116-5 requires a minimum of 256 bit for the output length of the hash function
	public static final String ECDSA_PLAIN_SHA_256 = "0.4.0.127.0.7.1.1.4.1.3"; //OID ECDSA plain sha256 from bsi-de algorithms id-ecc signatures ecdsa-plain-signatures
	public static final String ECDSA_PLAIN_SHA_384 = "0.4.0.127.0.7.1.1.4.1.4";
	public static final String ECDSA_PLAIN_SHA_512 = "0.4.0.127.0.7.1.1.4.1.5";
	
	public static final String ECDSA_PLAIN_SHA3_256 = "0.4.0.127.0.7.1.1.4.1.9";
	public static final String ECDSA_PLAIN_SHA3_384 = "0.4.0.127.0.7.1.1.4.1.10";
	public static final String ECDSA_PLAIN_SHA3_512 = "0.4.0.127.0.7.1.1.4.1.11";
	
	
		//ECSDSA {0, 4 , 0 , 127 , 0 , 7 , 1 , 1 , 4 , 4}
		//ECSDSA_PLAIN_SHA_224 is mentioned in TR-03151 but TR-03116-5 requires a minimum of 256 bit for the output length of the hash function
	public static final String ECSDSA_PLAIN_SHA_256 = "0.4.0.127.0.7.1.1.4.4.2"; //OID ECSDSA plain sha256 from bsi-de algorithms id-ecc signatures ecsdsa-plain-signatures
	public static final String ECSDSA_PLAIN_SHA_384 = "0.4.0.127.0.7.1.1.4.4.3";
	public static final String ECSDSA_PLAIN_SHA_512 = "0.4.0.127.0.7.1.1.4.4.4";
	
	public static final String ECSDSA_PLAIN_SHA3_256 = "0.4.0.127.0.7.1.1.4.4.6";
	public static final String ECSDSA_PLAIN_SHA3_384 = "0.4.0.127.0.7.1.1.4.4.7";
	public static final String ECSDSA_PLAIN_SHA3_512 = "0.4.0.127.0.7.1.1.4.4.8";

		//Algorithm hash map with the values from above and the names as the keys
	/**
	 * Maps the algorithm object identifiers according to BSI TR-03151 to the names they have. 
	 * Example: ECDSA_PLAIN_SHA_256 = 0.4.0.127.0.7.1.1.4.1.3<br>
	 * OID ECDSA plain sha256 from bsi-de algorithms id-ecc signatures ecdsa-plain-signatures<br>
	 * <br>
	 * Only those algorithms that are applicable to the simulator are mapped. That means, there is no ECDSA_PLAIN_SHA_224, for example.
	 */
	public static final Map<String, String> ALGORITHM_OID_MAP;
	static {
		final Map<String, String> algorithmOIDmap = new HashMap<>();
		algorithmOIDmap.put("ECDSA_PLAIN_SHA_256", ECDSA_PLAIN_SHA_256);
		algorithmOIDmap.put("ECDSA_PLAIN_SHA_384", ECDSA_PLAIN_SHA_384);
		algorithmOIDmap.put("ECDSA_PLAIN_SHA_512", ECDSA_PLAIN_SHA_512);
		algorithmOIDmap.put("ECDSA_PLAIN_SHA3_256", ECDSA_PLAIN_SHA3_256);
		algorithmOIDmap.put("ECDSA_PLAIN_SHA3_384", ECDSA_PLAIN_SHA3_384);
		algorithmOIDmap.put("ECDSA_PLAIN_SHA3_512", ECDSA_PLAIN_SHA3_512);
		
		algorithmOIDmap.put("ECSDSA_PLAIN_SHA_256", ECSDSA_PLAIN_SHA_256);
		algorithmOIDmap.put("ECSDSA_PLAIN_SHA_384", ECSDSA_PLAIN_SHA_384);
		algorithmOIDmap.put("ECSDSA_PLAIN_SHA_512", ECSDSA_PLAIN_SHA_512);
		algorithmOIDmap.put("ECSDSA_PLAIN_SHA3_256", ECSDSA_PLAIN_SHA3_256);
		algorithmOIDmap.put("ECSDSA_PLAIN_SHA3_384", ECSDSA_PLAIN_SHA3_384);
		algorithmOIDmap.put("ECSDSA_PLAIN_SHA3_512", ECSDSA_PLAIN_SHA3_512);
		ALGORITHM_OID_MAP = Collections.unmodifiableMap(algorithmOIDmap);
	}
	
	
	
	
	//OID Format RegEx
	/**
	 * Value: <b><pre>{@code}^(urn:oid:){1}[0-2]{1}(\\.(([1-9]+[0-9]*)|0{1}))*$</pre></b><br>
	 */
	public static final String OID_URN_REGEX = "^(urn:oid:){1}[0-2]{1}(\\.(([1-9]+[0-9]*)|0{1}))*$";						//matches the urn:oid:1.3.6.1.4.1 notation
	
	/**
	 * Value: <b><pre>{@code}^[0-2]{1}(\\.(([1-9]+\\d*)|0{1}))*$</pre></b><br>
	 */
	public static final String OID_ASN1_SIMPLE_DOTS_REGEX =  "^[0-2]{1}(\\.(([1-9]+\\d*)|0{1}))*$";							//matches the most common asn1 notation, p.e. 1.3.6.1.4.1
	
	/**
	 * Value: <b><pre>{@code}^\\{{1}( )?[0-2]{1}( (([1-9]+[0-9]*)|0{1}))*( )?\\}{1}$</pre></b><br>
	 */
	public static final String OID_ASN1_SIMPLE_SPACES_REGEX =  "^\\{{1}( )?[0-2]{1}( (([1-9]+[0-9]*)|0{1}))*( )?\\}{1}$";	//matches the asn1 notation with spaces instead of dots
	
	/**
	 * Value: <b><pre>{@code}^\\{{1}( )?(i|I|j|J){1}(\\w|-){2,14}(\\((0|1|2){1}\\)){1}( (\\w|-)*(\\((([1-9]+[0-9]?)|[0]{1}){1}\\)))*( )?\\}{1}$</pre></b><br>
	 */
	public static final String OID_ASN1_ADVANCED_REGEX = "^\\{{1}( )?(i|I|j|J){1}(\\w|-){2,14}(\\((0|1|2){1}\\)){1}( (\\w|-)*(\\((([1-9]+[0-9]?)|[0]{1}){1}\\)))*( )?\\}{1}$";
																															//matches the asn1 notation with explanation, p.e. {iso(1) identified-organisation(3) dod(6) internet(1) private(4) enterprise(1)}
																															//currently does not match mixed notation like { iso(1) member-body(2) 840 113549 }
	
	//File name constants
	/**
	 * Value: <b>exportedLogs.tar</b><br>
	 */
	public static final String FILE_NAME_EXPORT_LOGS_TAR = "exportedLogs.tar";
	
	/**
	 * Value: <b>exportedCerts.tar</b><br>
	 */
	public static final String FILE_NAME_EXPORT_CERTS_TAR = "exportedCerts.tar";
	
	/**
	 * Value: <b>exportedData.txt</b><br>
	 */
	public static final String FILE_NAME_EXPORT_DATA_TXT = "exportedData.txt";
	
	/**
	 * Value: <b>info.csv</b><br>
	 */
	public static final String FILE_NAME_INFO_CSV = "info.csv";
	
	/**
	 * Value: <b>persistentValues.ser</b><br>
	 */
	public static final String FILE_NAME_PERSISTEDVALUES_SER = "persistentValues.ser";
}
