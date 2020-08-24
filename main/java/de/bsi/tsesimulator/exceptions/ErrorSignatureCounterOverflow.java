/**
 * 
 */
package main.java.de.bsi.tsesimulator.exceptions;


/**
 * This class defines the exception ErrorSignatureCounterOverflow which is thrown if the signature counter in the {@linkplain main.java.de.bsi.tsesimulator.tse.CryptoCore}
 * can not be incremented due to an impending overflow error.<br>
 * 
 * In some cases this exception may be thrown, if the signature counter has already entered an illegal state. This should only be the case if an illegal signature counter was somehow loaded from a persisted file.
 * 
 * <br>
 * May in the future be accompanied by ErrorSignatureCounterExhausted.
 * @author dpottkaemper
 *
 */
public class ErrorSignatureCounterOverflow extends Exception {

	/**
	 * Randomly generated serialVersionUID
	 */
	private static final long serialVersionUID = 7854204077500966827L;
	
	/**
     * Constructs a new ErrorSignatureCounterOverflow exception with null as the value for
     * its detail message
     */
    public ErrorSignatureCounterOverflow() {
        super();
    }

    /**
     * Constructs a new ErrorSignatureCounterOverflow exception whereby its detail message
     * is initialized with the passed value
     * 
     * @param message
     *            value for the detail message of the exception
     */
    public ErrorSignatureCounterOverflow(String message) {
        super(message);
    }

    /**
     * Constructs a new ErrorSignatureCounterOverflow exception whereby its detail message
     * and cause are initialized with the appropriate passed values
     * 
     * @param message
     *            value for the detail message of the exception
     * @param cause
     *            value for the cause of the exception
     */
    public ErrorSignatureCounterOverflow(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new ErrorSignatureCounterOverflow exception whereby its detail message
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
    public ErrorSignatureCounterOverflow(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
