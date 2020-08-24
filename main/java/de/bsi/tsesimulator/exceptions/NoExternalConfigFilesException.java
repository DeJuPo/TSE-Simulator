/**
 * 
 */
package main.java.de.bsi.tsesimulator.exceptions;

/**
 * Is thrown whenever the paths passed to the Simulator do not point to the config.properties file. That could be the case because the file does 
 * not end in ".properties" or the path name was invalid.
 * 
 * @author dpottkaemper
 * @since 1.0.1
 */
public class NoExternalConfigFilesException extends SimulatorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3837545422476575250L;

	/**
	 * 
	 */
	public NoExternalConfigFilesException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public NoExternalConfigFilesException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public NoExternalConfigFilesException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NoExternalConfigFilesException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public NoExternalConfigFilesException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
