/**
 * 
 */
package main.java.de.bsi.tsesimulator.exceptions;

/**
 * Is thrown when someone attempts to add a user under an ID that is already registered.
 * This is so that no one can overwrite existing user data unwillingly. If a TSE simulator user wants to overwrite 
 * existing user data, he/she can just delete the present files on his/her file system and modify the file <i>userlist.properties</i>.
 * @author dpottkaemper
 * @since 1.0
 */
public class UserAlreadyExistsException extends SimulatorException {

	/**
	 * Generated serial version UID.
	 */
	private static final long serialVersionUID = -5351258145001032030L;

	/**
	 * 
	 */
	public UserAlreadyExistsException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public UserAlreadyExistsException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public UserAlreadyExistsException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UserAlreadyExistsException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public UserAlreadyExistsException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
