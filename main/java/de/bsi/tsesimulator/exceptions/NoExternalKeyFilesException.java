/**
 * 
 */
package main.java.de.bsi.tsesimulator.exceptions;

/**
 * Is thrown whenever the simulator fails to locate the external key files or fails to read them.
 * @author dpottkaemper
 *
 */
public class NoExternalKeyFilesException extends SimulatorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8017260861999096524L;

	/**
	 * 
	 */
	public NoExternalKeyFilesException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public NoExternalKeyFilesException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public NoExternalKeyFilesException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NoExternalKeyFilesException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public NoExternalKeyFilesException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
