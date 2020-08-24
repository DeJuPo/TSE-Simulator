/**
 * 
 */
package main.java.de.bsi.tsesimulator.exceptions;

/**
 * This class defines the exception ErrorTransactionCounterOverflow. This exception is thrown if the transaction counter in the {@linkplain main.java.de.bsi.tsesimulator.tse.ERSSpecificModule} 
 * can not be incremented due to an impending overflow error.
 * 
 * <br>May in the future be accompanied by ErrorTransactionCounterExhausted.
 * @author dpottkaemper
 *
 */
public class ErrorTransactionCounterOverflow extends Exception {

	/**
	 * Randomly generated serial version UID
	 */
	private static final long serialVersionUID = 3865189141953931664L;

	/**
	 * Constructs a new ErrorTransactionCounterOverflow exception with null as the value for
     * its detail message
	 */
	public ErrorTransactionCounterOverflow() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Constructs a new ErrorTransactionCounterOverflow exception whereby its detail message
     * is initialized with the passed value
     * 
     * @param message
     *            value for the detail message of the exception
	 */
	public ErrorTransactionCounterOverflow(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	

	/**
	 * Constructs a new ErrorTransactionCounterOverflow exception whereby its detail message
     * and cause are initialized with the appropriate passed values
     * 
     * @param message
     *            value for the detail message of the exception
     * @param cause
     *            value for the cause of the exception
	 */
	public ErrorTransactionCounterOverflow(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Constructs a new ErrorTransactionCounterOverflow exception whereby its detail message
     * and cause are initialized with the appropriate passed values. Furthermore, it
     * is specified whether or not suppression should be enabled or disabled and
     * whether or not the stack trace should be writable for this exception.
     * 
     * @param message
     *            value for the detail message of the exception
     * @param cause
     *            value for the cause of the exception
     * @param enableSuppression
     *            specifies whether or not suppression should be enabled for this
     *            exception
     * @param writableStackTrace
     *            specifies whether or not the stack trace should be writable for
     *            this exception
	 */
	public ErrorTransactionCounterOverflow(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
