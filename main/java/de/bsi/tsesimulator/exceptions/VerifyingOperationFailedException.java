/**
 * 
 */
package main.java.de.bsi.tsesimulator.exceptions;

/**
 * Is thrown whenever a verifying operation in the CryptoCore fails. 
 * Can also be thrown, if processing a log message to verify it fails.
 * @author dpottkaemper
 * @since 1.0
 */
public class VerifyingOperationFailedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7848272335340499661L;

	/**
	 * 
	 */
	public VerifyingOperationFailedException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public VerifyingOperationFailedException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public VerifyingOperationFailedException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public VerifyingOperationFailedException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public VerifyingOperationFailedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
