/**
 * 
 */
package main.java.de.bsi.tsesimulator.exceptions;

import main.java.de.bsi.tsesimulator.constants.Constants;

/**
 * Is thrown when someone attempts to add a user to the simulator but the value {@linkplain Constants#MAX_STORED_USERS} has been reached.
 * In that case, this exception is thrown to let the simulator user know, he/she should delete a user before adding a new one. 
 * <br> In reality, this problem would not occur because the users would have been predefined when the TSE was manufactured.
 * @author dpottkaemper
 * @since 1.0
 */
public class TooManyUsersException extends SimulatorException {

	/**
	 * Generated serial version UID.
	 */
	private static final long serialVersionUID = 1867160858245354409L;

	/**
	 * 
	 */
	public TooManyUsersException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public TooManyUsersException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public TooManyUsersException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public TooManyUsersException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public TooManyUsersException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
