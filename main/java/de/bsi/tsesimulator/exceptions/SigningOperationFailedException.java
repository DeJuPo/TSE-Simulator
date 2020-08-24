/**
 * 
 */
package main.java.de.bsi.tsesimulator.exceptions;

/**
 * Is thrown when a signing operation in the CryptoCore fails.
 * @author dpottkaemper
 * @since 1.0
 */
public class SigningOperationFailedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4052894245702251379L;

	/**
	 * 
	 */
	public SigningOperationFailedException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public SigningOperationFailedException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public SigningOperationFailedException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public SigningOperationFailedException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public SigningOperationFailedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
