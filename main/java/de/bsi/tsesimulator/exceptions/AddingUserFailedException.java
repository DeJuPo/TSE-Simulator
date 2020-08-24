/**
 * 
 */
package main.java.de.bsi.tsesimulator.exceptions;

/**
 * If the attempt to add a user to the TSE simulator fails. This could be because the user data could not be saved to a file.
 * @author dpottkaemper
 * @since 1.0
 * @see {@linkplain RemovingUserFailedException}, {@linkplain ModifyingUserFailedException}
 */
public class AddingUserFailedException extends SimulatorException {

	/**
	 * Generated serial version UID.
	 */
	private static final long serialVersionUID = -3205791109098117536L;

	/**
	 * 
	 */
	public AddingUserFailedException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public AddingUserFailedException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public AddingUserFailedException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public AddingUserFailedException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public AddingUserFailedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
