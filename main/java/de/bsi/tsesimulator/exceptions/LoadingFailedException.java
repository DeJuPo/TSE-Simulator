/**
 * 
 */
package main.java.de.bsi.tsesimulator.exceptions;

import java.io.IOException;

/**
 * Is thrown when a failed loading operation causes the TSESimulator to not load successfully. For example, the config.properties file could maybe not 
 * be accessed or the files storing the preconfigured keys were not available.
 * In most cases, this causes a constructor to not work as it should and the TSESimulator should be restarted and it should be investigated, which file 
 * was the cause of this exception.
 * @author dpottkaemper
 * 
 */
public class LoadingFailedException extends IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 751696687509626876L;

	/**
	 * 
	 */
	public LoadingFailedException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public LoadingFailedException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public LoadingFailedException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public LoadingFailedException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
