package main.java.de.bsi.seapi.exceptions;

/**
 * This class defines the exception ErrorStoringInitDataFailed that is thrown if
 * the storing of the initialization data during the commissioning of the SE API
 * by the application operator failed
 *
 */
public final class ErrorStoringInitDataFailed extends java.lang.Exception {

    /**
     * Constructs a new ErrorStoringInitDataFailed exception with null as the value
     * for its detail message
     */
    public ErrorStoringInitDataFailed() {
        super();
    }

    /**
     * Constructs a new ErrorStoringInitDataFailed exception whereby its detail
     * message is initialized with the passed value
     * 
     * @param message
     *            value for the detail message of the exception
     */
    public ErrorStoringInitDataFailed(String message) {
        super(message);
    }

    /**
     * Constructs a new ErrorStoringInitDataFailed exception whereby its detail
     * message and cause are initialized with the appropriate passed values
     * 
     * @param message
     *            value for the detail message of the exception
     * @param cause
     *            value for the cause of the exception
     */
    public ErrorStoringInitDataFailed(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new ErrorStoringInitDataFailed exception whereby its detail
     * message and cause are initialized with the appropriate passed values.
     * Furthermore, it is specified whether or not suppression should be enabled or
     * disabled and whether or not the stack trace should be writable for this
     * exception.
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
    public ErrorStoringInitDataFailed(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
