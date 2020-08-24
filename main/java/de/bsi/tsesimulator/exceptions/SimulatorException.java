/**
 * 
 */
package main.java.de.bsi.tsesimulator.exceptions;

/**
 * The generic TSE-simulator exception. Is thrown whenever something unique to the simulator nature of this program fails.
 * Should be viewed as a superclass for every other simulator-related exception. 
 * @author dpottkaemper
 * @since 1.0
 */
public class SimulatorException extends Exception {

	/**
	 * Generated serial version UID.
	 */
	private static final long serialVersionUID = 3252223407338291922L;

	/**
	 * 
	 */
	public SimulatorException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public SimulatorException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public SimulatorException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public SimulatorException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public SimulatorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
