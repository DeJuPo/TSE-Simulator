package main.java.de.bsi.seapi.exceptions;

/**
 * This class defines the exception ErrorCertificateExpired that is thrown if a
 * SE API function is invoked and the certificate with the public key for the
 * verification of the appropriate type of log messages is expired. Even if a
 * certificate expired, the log message parts are created by the Secure Element
 * and stored by the SE API. In this case, the exception ErrorCertificateExpired
 * is raised only after the data of the log message has been stored.
 */

public final class ErrorCertificateExpired extends java.lang.Exception {

    /**
     * Constructs a new ErrorCertificateExpired exception with null as the value for
     * its detail message
     */
    public ErrorCertificateExpired() {
        super();
    }

    /**
     * Constructs a new ErrorCertificateExpired exception whereby its detail message
     * is initialized with the passed value
     * 
     * @param message
     *            value for the detail message of the exception
     */
    public ErrorCertificateExpired(String message) {
        super(message);
    }

    /**
     * Constructs a new ErrorCertificateExpired exception whereby its detail message
     * and cause are initialized with the appropriate passed values
     * 
     * @param message
     *            value for the detail message of the exception
     * @param cause
     *            value for the cause of the exception
     */
    public ErrorCertificateExpired(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new ErrorCertificateExpired exception whereby its detail message
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
    public ErrorCertificateExpired(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
