/**
 * 
 */
package main.java.de.bsi.tsesimulator.exceptions;

/**
 * <b>Changed the purpose of this exception!</b><br>
 * 
 * This exception is thrown whenever the signature counter used to filter certain kinds of log messages is outside the legal bounds. 
 * The legal bounds for a signature counter are implicitly defined by BSI TR-03153. The signature counter of the simulator shall only be of positive value,
 * except when it is first instantiated. Otherwise, a signature counter value of zero is illegal. Values below zero are always illegal. 
 * 
 * @author dpottkaemper
 * @version 1.4
 */
public class SignatureCounterException extends Exception {

	/**
	 * Generated serial version UID
	 */
	private static final long serialVersionUID = 4472626036634505198L;

	/**
	 * 
	 */
	public SignatureCounterException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public SignatureCounterException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public SignatureCounterException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public SignatureCounterException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public SignatureCounterException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
