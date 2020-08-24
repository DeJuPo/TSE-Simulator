/**
 * 
 */
package main.java.de.bsi.tsesimulator.exceptions;

/**
 * Is thrown when registering a clientId through {@linkplain TSEController#registerClient(String)} fails due to the clientId already being registered.
 * @author dpottkaemper
 *
 */
public class ClientIdAlreadyRegisteredException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2308558577314620624L;

	/**
	 * 
	 */
	public ClientIdAlreadyRegisteredException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public ClientIdAlreadyRegisteredException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public ClientIdAlreadyRegisteredException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ClientIdAlreadyRegisteredException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public ClientIdAlreadyRegisteredException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
