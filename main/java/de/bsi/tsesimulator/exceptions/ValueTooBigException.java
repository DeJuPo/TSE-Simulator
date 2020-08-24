package main.java.de.bsi.tsesimulator.exceptions;
/**
 * Indicates that a value in a byte array exceeds the length of what can be encoded with the ASN.1 DER long form of length.
 * Can also be thrown, when a tag being set with a byte array longer than one byte. The simulator does not support high-tag-number-form.
 * 
 * @author dpottkaemper
 *
 */

public class ValueTooBigException extends Exception {

	/**
	 * Generated serial version UID.
	 */
	private static final long serialVersionUID = 7658032663177052687L;

	public ValueTooBigException() {
		// TODO Auto-generated constructor stub
	}

	public ValueTooBigException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public ValueTooBigException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public ValueTooBigException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public ValueTooBigException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
