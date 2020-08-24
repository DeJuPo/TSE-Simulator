/**
 * 
 */
package main.java.de.bsi.tsesimulator.exceptions;

/**
 * @author dpottkaemper
 * Thrown whenever something connected with elliptic curve cryptography fails and it is not as specific as a {@linkplain TR_03111_ECC_V2_1_Exception}.
 */
public class ECCException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7153535520096923556L;

	/**
	 * 
	 */
	public ECCException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public ECCException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public ECCException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ECCException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public ECCException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
