/**
 * 
 */
package main.java.de.bsi.tsesimulator.exceptions;

/**
 * Thrown when registering another client with the TSE would result in a violation of the limit given through maxNumberOfClients.
 * @author dpottkaemper
 *
 */
public class TooManyClientsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8269520909747778523L;

	/**
	 * 
	 */
	public TooManyClientsException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public TooManyClientsException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public TooManyClientsException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public TooManyClientsException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public TooManyClientsException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
