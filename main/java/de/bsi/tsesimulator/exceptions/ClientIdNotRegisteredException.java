/**
 * 
 */
package main.java.de.bsi.tsesimulator.exceptions;

/**
 * Thrown when attempting to de-register a clientId from the TSE which is not currently registered. 
 * @author dpottkaemper
 *
 */
public class ClientIdNotRegisteredException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 894827451663673155L;

	/**
	 * 
	 */
	public ClientIdNotRegisteredException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public ClientIdNotRegisteredException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public ClientIdNotRegisteredException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ClientIdNotRegisteredException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public ClientIdNotRegisteredException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
