package main.java.de.bsi.tsesimulator.exceptions;
/**
 * Indicates that a value that should be present has not yet been set or has been reset. This can be the case with, for example, log messages or ASN.1 structures.
 * @author dpottkaemper
 * 
 */

public class ValueNullException extends Exception {

	/**
	 * Generated serial version UID
	 */
	private static final long serialVersionUID = 7358489864144108094L;

	public ValueNullException() {
		// TODO Auto-generated constructor stub
	}

	public ValueNullException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public ValueNullException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public ValueNullException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public ValueNullException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
