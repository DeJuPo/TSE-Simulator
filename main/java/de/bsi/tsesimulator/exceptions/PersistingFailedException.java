/**
 * 
 */
package main.java.de.bsi.tsesimulator.exceptions;

import main.java.de.bsi.tsesimulator.tse.storage.PersistentStorage;

/**
 * Is thrown by the {@linkplain PersistentStorage} if there was an error in storing the current values of the TSESimulator.
 * An error could occur if there are older persisted values that are different from the new values and this difference is illegal (e.g.
 * the old signature counter is higher than the new). It could also occur if an IOException or a FileNotFouldException are raised when reading from such
 * an older file.
 * 
 * @author dpottkaemper
 *
 */
public class PersistingFailedException extends Exception {

	/**
	 * Generated serial version uid.
	 */
	private static final long serialVersionUID = 1275927410185892605L;

	/**
	 * 
	 */
	public PersistingFailedException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public PersistingFailedException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public PersistingFailedException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public PersistingFailedException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public PersistingFailedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
