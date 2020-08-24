/**
 * 
 */
package main.java.de.bsi.tsesimulator.exceptions;

/**
 * Thrown when opening another transactions with the TSE would result in a violation of the limit given through maxNumberOfTransactions.<br>
 * The recommended action when encountering this exception is to first finish some of the open transactions and then proceed with starting new ones.
 * 
 * @author dpottkaemper
 *
 */
public class TooManyOpenTransactionsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 448003901614850364L;

	/**
	 * 
	 */
	public TooManyOpenTransactionsException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public TooManyOpenTransactionsException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public TooManyOpenTransactionsException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public TooManyOpenTransactionsException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public TooManyOpenTransactionsException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
