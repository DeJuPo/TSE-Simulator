package main.java.de.bsi.seapi;

/**
 * This class defines the constants that are used in the context of the SE API
 */
public final class Constant {

    /**
     * This constant defines the return value of the functions of the SE API that
     * indicates that the execution of a function has been successful
     */
    public static final short EXECUTION_OK = 0;

    /**
     * Constant that defines a return value for the SE API function
     * authenticateUser. This return value indicates that the authentication attempt
     * has failed
     */
    public static final short AUTHENTICATION_FAILED = -4000;

    /**
     * Constant that defines a return value for the SE API function unblockUser.
     * This return value indicates that the attempt to unblock a PIN entry has
     * failed.
     */
    public static final short UNBLOCK_FAILED = -4001;

}
