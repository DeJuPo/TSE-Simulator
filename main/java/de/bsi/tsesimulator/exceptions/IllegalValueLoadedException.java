/**
 * 
 */
package main.java.de.bsi.tsesimulator.exceptions;

import main.java.de.bsi.tsesimulator.tse.CryptoCore;

/**
 * Thrown when attempting to load a value that is illegal in some way. An example could be attempting to load a signature counter with a negative value.
 * Is propagated up from classes such as {@linkplain CryptoCore}, {@linkplain ERSSpecificModule} and {@linkplain SecurityModule} to the {@linkplain TSEController}.
 * @author dpottkaemper
 *
 */
public class IllegalValueLoadedException extends IllegalArgumentException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5724747776526286337L;

	/**
	 * 
	 */
	public IllegalValueLoadedException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param s
	 */
	public IllegalValueLoadedException(String s) {
		super(s);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public IllegalValueLoadedException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public IllegalValueLoadedException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
