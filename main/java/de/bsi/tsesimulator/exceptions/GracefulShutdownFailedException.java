/**
 * 
 */
package main.java.de.bsi.tsesimulator.exceptions;

/**
 * Used when an operation fails during the graceful shutdown, making it less graceful. This could be caused by a transaction that was unable to be closed or similar.
 * @author dpottkaemper
 *
 */
public class GracefulShutdownFailedException extends SimulatorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 442206766285473472L;

	/**
	 * 
	 */
	public GracefulShutdownFailedException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public GracefulShutdownFailedException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public GracefulShutdownFailedException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public GracefulShutdownFailedException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public GracefulShutdownFailedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
