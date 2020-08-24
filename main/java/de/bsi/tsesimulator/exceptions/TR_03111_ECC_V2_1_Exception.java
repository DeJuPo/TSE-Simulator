package main.java.de.bsi.tsesimulator.exceptions;

/**
 * This exception is thrown whenever a data conversion operation specified in BSI TR-03111 ECC V2.10 fails. 
 * Often, this is due to illegal inputs into the function.
 * The most common illegal input in the context of this simulator will likely be a mismatch between the chosen curve and the chosen hash function.
 * The bit length of the order of the base point of the chosen hash function <b>must</b> be equal or smaller than the bit length of 
 * the output of the hash function.
 * @author dpottkaemper
 *
 */
public class TR_03111_ECC_V2_1_Exception extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4098503523608930478L;

	public TR_03111_ECC_V2_1_Exception() {
		// TODO Auto-generated constructor stub
	}

	public TR_03111_ECC_V2_1_Exception(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public TR_03111_ECC_V2_1_Exception(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public TR_03111_ECC_V2_1_Exception(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public TR_03111_ECC_V2_1_Exception(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
