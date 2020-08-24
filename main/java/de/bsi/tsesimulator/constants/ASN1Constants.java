package main.java.de.bsi.tsesimulator.constants;

/**
 * Provides constants needed in an ASN.1 context.
 * @author dpottkaemper
 *
 */
public abstract class ASN1Constants {
	
	/**
	 * ASN.1 Tag Universal Boolean = 0x01
	 */
	public static final byte UNIVERSAL_BOOLEAN = 0x01;
	
	/**
	 * ASN.1 Tag Universal Integer = 0x02
	 */
	public static final byte UNIVERSAL_INTEGER = 0x02;
	
	/**
	 * ASN.1 Tag Universal BitString = 0x03
	 */
	public static final byte UNIVERSAL_BIT_STRING = 0x03;
	
	/**
	 * ASN.1 Tag Universal OctetString = 0x04
	 */
	public static final byte UNIVERSAL_OCTET_STRING = 0x04;
	
	/**
	 * ASN.1 Tag Universal Null = 0x05
	 */
	public static final byte UNIVERSAL_NULL = 0x05;
	
	/**
	 * ASN.1 Tag Universal Object Identifier = 0x06
	 */
	public static final byte UNIVERSAL_OBJECT_INDENTIFIER = 0x06;
	
	/**
	 * ASN.1 Tag Universal Sequence = 0x30.<br>
	 * Sequence Tag alone would be only 0x10, but since Sequence is constructed, it is encoded as 0b00110000 = 0x30.
	 */
	public static final byte UNIVERSAL_SEQUENCE = 0x30;				//Sequence Tag normally is just 0x10 but this does not show that a SEQUENCE is constructed
	
	/**
	 * ASN.1 Tag Universal Set = 0x31.<br>
	 * Set Tag alone would be only 0x11, but since Set is constructed, it is encoded as 0b00110001 = 0x31.
	 */
	public static final byte UNIVERSAL_SET = 0x31;					//Set Tag is just 0x11, but because it's always constructed, it is 0x11 + 0b00100000 = 0x31
	
	/**
	 * ASN.1 Tag Universal PrintableString = 0x13
	 */
	public static final byte UNIVERSAL_PRINTABLE_STRING = 0x13;
	
	/**
	 * ASN.1 Tag Universal UTCTime = 0x17
	 */
	public static final byte UNIVERSAL_UTCTIME = 0x17;
	
}
