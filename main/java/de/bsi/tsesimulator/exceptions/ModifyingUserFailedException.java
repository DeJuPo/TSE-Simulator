/**
 * 
 */
package main.java.de.bsi.tsesimulator.exceptions;

/**
 * This exception is thrown when modifying user data fails. This could be, for example, caused by the user being non existent and 
 * therefore unable to be modified or a failed writing operation on the file system.
 * @author dpottkaemper
 * @since 1.5
 * @see {@linkplain AddingUserFailedException}, {@linkplain RemovingUserFailedException}
 */
public class ModifyingUserFailedException extends SimulatorException {

	/**
	 * Generated serialVersionUID.
	 */
	private static final long serialVersionUID = -1257486961463163635L;

	/**
	 * 
	 */
	public ModifyingUserFailedException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public ModifyingUserFailedException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public ModifyingUserFailedException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ModifyingUserFailedException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public ModifyingUserFailedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
