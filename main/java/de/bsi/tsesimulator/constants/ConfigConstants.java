/**
 * 
 */
package main.java.de.bsi.tsesimulator.constants;

/**
 * This class provides the tags for each time a key is used to load a value from through the class {@linkplain PropertyValues}.
 * @author dpottkaemper
 *
 */
public abstract class ConfigConstants {
	
	/**
	 * Value:<b>signatureAlgorithm</b>
	 */
	public static final String CFG_TAG_SIGNATURE_ALGORITHM = "signatureAlgorithm";

	/**
	 * Value:<b>ECDSA</b>
	 */
	public static final String CFG_ENTRY_SIGNATURE_ALGORITHM_ECDSA = "ECDSA";

	/**
	 * Value:<b>ECSDSA</b>
	 */
	public static final String CFG_ENTRY_SIGNATURE_ALGORITHM_ECSDSA = "ECSDSA";

	
	/**
	 * Value:<b>hashMethod</b>
	 */
	public static final String CFG_TAG_HASH_METHOD = "hashMethod";

	/**
	 * Value:<b>SHA</b>
	 */
	public static final String CFG_ENTRY_HASH_METHOD_SHA = "SHA";

	/**
	 * Value:<b>SHA3</b>
	 */
	public static final String CFG_ENTRY_HASH_METHOD_SHA3 = "SHA3";

	
	/**
	 * Value:<b>hashLength</b>
	 */
	public static final String CFG_TAG_HASH_LENGTH = "hashLength";

	/**
	 * Value:<b>curve</b>
	 */
	public static final String CFG_TAG_CURVE = "curve";
	

	/**
	 * Value:<b>updateTransactionMode</b>
	 */
	public static final String CFG_TAG_UPDATE_TRANSACTION_MODE = "updateTransactionMode";

	/**
	 * Value:<b>signed</b>
	 */
	public static final String CFG_ENTRY_UPDATE_TRANSACTION_MODE_SIGNED = "signed";

	/**
	 * Value:<b>unsigned</b>
	 */
	public static final String CFG_ENTRY_UPDATE_TRANSACTION_MODE_UNSIGNED = "unsigned";
	

	/**
	 * Value:<b>keyDir</b>
	 */
	public static final String CFG_TAG_KEY_DIR = "keyDir";

	/**
	 * Value:<b>privKey</b>
	 */
	public static final String CFG_TAG_PRIV_KEY = "privKey";

	/**
	 * Value:<b>privKeyEncoding</b>
	 */
	public static final String CFG_TAG_PRIV_KEY_ENCODING = "privKeyEncoding";

	/**
	 * Value:<b>tseCert</b>
	 */
	public static final String CFG_TAG_TSE_CERT = "tseCert";

	/**
	 * Value:<b>tseCertEncoding</b>
	 */
	public static final String CFG_TAG_TSE_CERT_ENCODING = "tseCertEncoding";

	/**
	 * Value:<b>DER</b>
	 */
	public static final String CFG_ENTRY_TSE_CERT_ENCODING_DER = "DER";

	/**
	 * Value:<b>PEM</b>
	 */
	public static final String CFG_ENTRY_TSE_CERT_ENCODING_PEM = "PEM";
	

	/**
	 * Value:<b>pathToUserlistPropertiesFile</b>
	 */
	public static final String CFG_TAG_PATH_TO_USERLIST_PROPERTIES = "pathToUserlistPropertiesFile";

	/**
	 * Value:<b>storageDir</b>
	 */
	public static final String CFG_TAG_PATH_TO_STORAGE = "storageDir";

	/**
	 * Value:<b>persistentStorageDir</b>
	 */
	public static final String CFG_TAG_PATH_TO_PERSISTENT_STORAGE = "persistentStorageDir";
	

	/**
	 * Value:<b>timeFormat</b>
	 */
	public static final String CFG_TAG_TIME_FORMAT = "timeFormat";

	/**
	 * Value:<b>Unixt</b>
	 */
	public static final String CFG_ENTRY_TIME_FORMAT_UNIXT = "Unixt"; 

	/**
	 * Value:<b>Gent</b>
	 */
	public static final String CFG_ENTRY_TIME_FORMAT_GENT = "Gent";

	/**
	 * Value:<b>Utc</b>
	 */
	public static final String CFG_ENTRY_TIME_FORMAT_UTC = "Utc"; 
	

	/**
	 * Value:<b>descriptionSetByManufacturer</b>
	 */
	public static final String CFG_TAG_DESCRIPTION_SET_BY_MANUFACTURER ="descriptionSetByManufacturer";

	/**
	 * Value:<b>true</b>
	 */
	public static final String CFG_ENTRY_DESCRIPTION_SET_BY_MANUFACTURER_TRUE = "true";

	/**
	 * Value:<b>false</b>
	 */
	public static final String CFG_ENTRY_DESCRIPTION_SET_BY_MANUFACTURER_FALSE = "false";
	

	/**
	 * Value:<b>maxNumberOfClients</b>
	 */
	public static final String CFG_TAG_MAX_NUMBER_CLIENTS = "maxNumberOfClients";

	/**
	 * Value:<b>maxNumberOfTransactions</b>
	 */
	public static final String CFG_TAG_MAX_NUMBER_TRANSACTIONS = "maxNumberOfTransactions";
	

	/**
	 * Value:<b>config.properties</b>
	 */
	public static final String CFG_NAME = "config.properties";
}
