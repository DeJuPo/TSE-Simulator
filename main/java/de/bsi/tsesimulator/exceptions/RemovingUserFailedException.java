/**
 * 
 */
package main.java.de.bsi.tsesimulator.exceptions;

/**
 * If the attempt to remove a user from the TSE simulator fails. This could be because the user data does not exist, or the removal operation on the 
 * file failed.
 * @author dpottkaemper
 * @since 1.5
 * @see {@linkplain AddingUserFailedException}, {@linkplain ModifyingUserFailedException}
 */
public class RemovingUserFailedException extends SimulatorException {

	/**
	 * Generated serialVersionUID
	 */
	private static final long serialVersionUID = 6899339289928827816L;

	/**
	 * 
	 */
	public RemovingUserFailedException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public RemovingUserFailedException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public RemovingUserFailedException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public RemovingUserFailedException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public RemovingUserFailedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
