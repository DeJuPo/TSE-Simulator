package main.java.de.bsi.seapi.holdertypes;

/**
 * This class defines a holder class that enables the specification of output parameters of SE API functions 
 * of the type of a byte array
 */
public final class ByteArrayHolder {
	
	/**
	 * Encapsulated byte array value
	 */
	private byte[] value;
		
	/**
	 * This function returns the encapsulated byte array value
	 * @return encapsulated byte array value
	 */
	public byte[] getValue() {
		return value;
	}

	/**
	 * This function sets a new value for the encapsulated byte array
	 * @param value new value for the encapsulated byte array value
	 */
	public void setValue(byte[] value) {
		this.value = value;
	}
}
