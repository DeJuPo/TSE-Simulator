/**
 * 
 */
package main.java.de.bsi.tsesimulator.exceptions;

/**
 * Generic exception for something wrong with TLVs in the simulator.
 * @author dpottkaemper
 *
 */
public class TLVException extends Exception {

	/**
	 * Generated serial version UID
	 */
	private static final long serialVersionUID = 2167614412937716990L;

	/**
	 * 
	 */
	public TLVException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public TLVException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public TLVException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public TLVException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public TLVException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
