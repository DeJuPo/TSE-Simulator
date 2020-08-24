package main.java.de.bsi.tsesimulator.tlv;

import java.math.BigInteger;

import main.java.de.bsi.tsesimulator.constants.ASN1Constants;
import main.java.de.bsi.tsesimulator.constants.Constants;
import main.java.de.bsi.tsesimulator.exceptions.ValueNullException;
import main.java.de.bsi.tsesimulator.exceptions.ValueTooBigException;
import main.java.de.bsi.tsesimulator.msg.LogMessage;
import main.java.de.bsi.tsesimulator.tse.SecurityModule;
import main.java.de.bsi.tsesimulator.tse.TSEController;

/**
 * This class provides utility methods that are used in en- and decoding ASN1 byte arrays.
 * It also provides some methods to make byte arrays human-readable so that one gets a hexadecimal (or binary) representation of 
 * their content.
 * @author dpottkaemper
 * @since 1.0
 */

public class TLVUtility {
	/**
	 * Class only provides static methods, therefore should not be instantiated. 
	 */
	private TLVUtility() {}
	
	//----------------------------------------Other utility------------------------------------------------------------------
	/**
	 * The {@linkplain SecurityModule} returns the log message byte arrays wrapped in the SEQUENCE wrapper. That means, if 
	 * the byte array that the SecurityModule gives to the {@linkplain TSEController} is decoded into a TLVObject[], the first entry of that array will have the 
	 * 0x30 Tag and it will have the rest of the log message byte array as its value.
	 * This is not what the TSEController or any other class needs to construct {@linkplain LogMessage}.
	 * This method strips the SEQUENCE wrapper from the TLVObject[]. If the input does not begin with a UNIVERSAL SEQUENCE, it returns just the 
	 * TLVObject[].
 	 * @param inputWithSequence the TLVObject array that represents a LogMessage with the SEQUENCE wrapper.
	 * @return a TLVObject array without that SEQUENCE wrapper.
	 * @since 1.0
	 * @see {@linkplain TLVObject#decodeASN1ByteArrayToTLVObjectArray(byte[])}
	 */
	public static TLVObject[] removeSEQUENCEWrapper(TLVObject[] inputWithSequence) {
		//check if the input has a SEQUENCE wrapper
		if(inputWithSequence[0].getTag().getTagContent()[0] == (byte) ASN1Constants.UNIVERSAL_SEQUENCE) {
			//if it has, strip the first entry from the array and return the rest
			TLVObject[] withoutSequenceWrapper = new TLVObject[inputWithSequence.length-1];
			System.arraycopy(inputWithSequence, 1, withoutSequenceWrapper, 0, withoutSequenceWrapper.length);
			return withoutSequenceWrapper;
		}
		
		return inputWithSequence;
	}
	
	
	//----------------------------------------TO STRING UTILITY--------------------------------------------------------------
	/**
	 * Converts the content of a byte array to a String in hexadecimal form.
	 * 
	 * @param array the array one wants to convert to a human readable hexadecimal form
	 * @return the byte array in hexadecimal form for humans to read, without the "0x" prefix
	 */
	public static String byteArrayToString(byte[] array) {
		StringBuilder str = new StringBuilder();
		
		//iterate through whole array, convert each element from the array to hexadecimal string form, put a space between elements
		for(byte b : array) {
			str.append(String.format("%02X", b));
			str.append(" ");
		}
		return str.toString();
	}
	
	/**
	 * Converts the contents of a byte array to a String in binary form, leaving the leading zeroes intact.
	 * 
	 * @param array:
	 * 	 the array to be converted to a human readable binary form
	 * @return the byte array for humans to read in 8 substrings of 8 characters.
	 */
	public static String byteArrayToBinaryString(byte[] array) {
		StringBuilder str = new StringBuilder();
		for(byte b : array) {
			str.append(("00000000"+Integer.toBinaryString(b)).substring(Integer.toBinaryString(b).length()));
			str.append(" ");
		}
		return str.toString();
	}
	
	//----------------------------------------INTEGER & LONG TO ASN1 BYTE ARRAY--------------------------------------------------------------
	/**
	 * Converts a value which is given as an Integer into a byte array according to the ASN1 DER standard.
	 * @param toBeConverted
	 * @return the byte array according to asn1 standards or "null", if the method fails.
	 */
	public static byte[] integerToByteArray_ASN1_Value(int toBeConverted) {
		
		//create the temporary version of what will be returned
		byte[] tempToBeReturned = {
	            (byte)(toBeConverted >> 24),
	            (byte)(toBeConverted >> 16),
	            (byte)(toBeConverted >> 8),
	            (byte)toBeConverted};
		
		
		//converting positive integers
		if(toBeConverted >= 0) {
			//0 to 127: return only one octet
			if(toBeConverted<=127) {
				byte[] toBeReturned = new byte[1];
				toBeReturned[0] = tempToBeReturned[3];				
				return toBeReturned;
			}
			
			//128 to 32768-1 return two octets
			else if(toBeConverted<=32767) {
				byte[] toBeReturned = new byte[2];
				toBeReturned[0] = tempToBeReturned[2];				
				toBeReturned[1] = tempToBeReturned[3];
				return toBeReturned;
			}
			
			//32767 to 8388607 return three octets
			else if(toBeConverted<=8388607) {
				byte[] toBeReturned = new byte[3];
				toBeReturned[0] = tempToBeReturned[1];				
				toBeReturned[1] = tempToBeReturned[2];
				toBeReturned[2] = tempToBeReturned[3];
				return toBeReturned;
			}
			
			//above that: return four octets
			else {
				return tempToBeReturned;
			}
		}
		
		//converting negative integers
		else if(toBeConverted<0) {
			//-1 to -128: return one octet
			if(toBeConverted>=-128) {
				byte[] toBeReturned = new byte[1];
				toBeReturned[0] = tempToBeReturned[3];				
				return toBeReturned;
			}
			
			//-129 to -32768 return two octets
			else if(toBeConverted>=-32768) {
				byte[] toBeReturned = new byte[2];
				toBeReturned[0] = tempToBeReturned[2];				
				toBeReturned[1] = tempToBeReturned[3];
				return toBeReturned;
			}
			
			//-32769 to -8388608 return three octets
			else if(toBeConverted>=-8388608) {
				byte[] toBeReturned = new byte[3];
				toBeReturned[0] = tempToBeReturned[1];				
				toBeReturned[1] = tempToBeReturned[2];
				toBeReturned[2] = tempToBeReturned[3];
				return toBeReturned;
			}
			
			//above that: return four octets
			else {
				return tempToBeReturned;
			}
			
		}
		//if method fails for any reason, it returns null
		return null;
	}
	
	
	
	/**
	 * Converts a value which is given as a long into a byte array according to the ASN1 DER standard.
	 * @param toBeConverted a long to be converted
	 * @return a byte array representation of the long according to ASN1 DER standard.
	 */
	public static byte[] longToByteArray_ASN1_Value(long toBeConverted) {
			
			//create the temporary version of what will be returned
			byte[] tempToBeReturned = {
					(byte)(toBeConverted >> 56),
					(byte)(toBeConverted >> 48),
					(byte)(toBeConverted >> 40),
					(byte)(toBeConverted >> 32),
					(byte)(toBeConverted >> 24),
					(byte)(toBeConverted >> 16),
					(byte)(toBeConverted >> 8),
					(byte)toBeConverted};
			
			
			//converting positive long values
			if(toBeConverted >= 0) {
				//0 to 127: return only one octet
				if(toBeConverted<=127) {
					byte[] toBeReturned = new byte[1];
					toBeReturned[0] = tempToBeReturned[7];				
					return toBeReturned;
				}
				
				//128 to 32768-1 return two octets
				else if(toBeConverted<=32767) {
					byte[] toBeReturned = new byte[2];
					toBeReturned[0] = tempToBeReturned[6];				
					toBeReturned[1] = tempToBeReturned[7];
					return toBeReturned;
				}
				
				//32767 to 8388607 return three octets
				else if(toBeConverted<=8388607) {
					byte[] toBeReturned = new byte[3];
					toBeReturned[0] = tempToBeReturned[5];				
					toBeReturned[1] = tempToBeReturned[6];
					toBeReturned[2] = tempToBeReturned[7];
					return toBeReturned;
				}
				
				//8388608 to 2147483647 return four octets
				else if(toBeConverted<=2147483647) {
					byte[] toBeReturned = new byte[4];
					toBeReturned[0] = tempToBeReturned[4];				
					toBeReturned[1] = tempToBeReturned[5];
					toBeReturned[2] = tempToBeReturned[6];
					toBeReturned[3] = tempToBeReturned[7];
					return toBeReturned;
				}
				
				
				//the value 0 if x == y; a value less than 0 if x < y; and a value greater than 0 if x > y
				//2147483648 to 549755813887 return five octets
				else if((Long.compare(toBeConverted, 549755813888L))<0) {
					byte[] toBeReturned = new byte[5];
					toBeReturned[0] = tempToBeReturned[3];				
					toBeReturned[1] = tempToBeReturned[4];
					toBeReturned[2] = tempToBeReturned[5];
					toBeReturned[3] = tempToBeReturned[6];
					toBeReturned[4] = tempToBeReturned[7];
					return toBeReturned;
				}
				
				//549755813888 to 140737488355327 return six octets
				else if((Long.compare(toBeConverted, 140737488355328L))<0) {
					byte[] toBeReturned = new byte[6];
					toBeReturned[0] = tempToBeReturned[2];				
					toBeReturned[1] = tempToBeReturned[3];
					toBeReturned[2] = tempToBeReturned[4];
					toBeReturned[3] = tempToBeReturned[5];
					toBeReturned[4] = tempToBeReturned[6];
					toBeReturned[5] = tempToBeReturned[7];
					return toBeReturned;
				}
				
				//140737488355328 to 36028797018963967 return seven octets
				else if((Long.compare(toBeConverted, 36028797018963967L))<0) {
					byte[] toBeReturned = new byte[7];
					toBeReturned[0] = tempToBeReturned[1];				
					toBeReturned[1] = tempToBeReturned[2];
					toBeReturned[2] = tempToBeReturned[3];
					toBeReturned[3] = tempToBeReturned[4];
					toBeReturned[4] = tempToBeReturned[5];
					toBeReturned[5] = tempToBeReturned[6];
					toBeReturned[6] = tempToBeReturned[7];
					return toBeReturned;
				}
				
				
				//above: return eight octets
				else {
					return tempToBeReturned;
				}
			}
			
			
			
			//converting negative long values
			else if(toBeConverted<0) {
				//-1 to -128 1 octet 
				if(toBeConverted>=-128) {
					byte[] toBeReturned = new byte[1];
					toBeReturned[0] = tempToBeReturned[7];				
					return toBeReturned;
				}
				
				//-129 to -32768 2 octets
				else if(toBeConverted>=-32768) {
					byte[] toBeReturned = new byte[2];
					toBeReturned[0] = tempToBeReturned[6];				
					toBeReturned[1] = tempToBeReturned[7];
					return toBeReturned;
				}
				
				//-32769 to -8388608 3 octets
				else if(toBeConverted>=-8388608) {
					byte[] toBeReturned = new byte[3];
					toBeReturned[0] = tempToBeReturned[5];				
					toBeReturned[1] = tempToBeReturned[6];
					toBeReturned[2] = tempToBeReturned[7];
					return toBeReturned;
				}
				
				
				//-8388609 to -2147483648 4 octets
				else if(toBeConverted>=-2147483648) {
					byte[] toBeReturned = new byte[4];
					toBeReturned[0] = tempToBeReturned[4];				
					toBeReturned[1] = tempToBeReturned[5];
					toBeReturned[2] = tempToBeReturned[6];
					toBeReturned[3] = tempToBeReturned[7];
					return toBeReturned;
				}
				
				
				//the value 0 if x == y; a value less than 0 if x < y; and a value greater than 0 if x > y
				//-2147483649 to -549755813888 5 octets 
				else if((Long.compare(toBeConverted, -549755813889L))>0) {
					byte[] toBeReturned = new byte[5];
					toBeReturned[0] = tempToBeReturned[3];				
					toBeReturned[1] = tempToBeReturned[4];
					toBeReturned[2] = tempToBeReturned[5];
					toBeReturned[3] = tempToBeReturned[6];
					toBeReturned[4] = tempToBeReturned[7];
					return toBeReturned;
				}
				
				//-549755813889 to -140737488355328 6 octets
				else if((Long.compare(toBeConverted, -140737488355329L))>0) {
					byte[] toBeReturned = new byte[6];
					toBeReturned[0] = tempToBeReturned[2];				
					toBeReturned[1] = tempToBeReturned[3];
					toBeReturned[2] = tempToBeReturned[4];
					toBeReturned[3] = tempToBeReturned[5];
					toBeReturned[4] = tempToBeReturned[6];
					toBeReturned[5] = tempToBeReturned[7];
					return toBeReturned;
				}
				
				//-140737488355329 to 36028797018963968 7 octets
				else if((Long.compare(toBeConverted, -36028797018963969L))>0) {
					byte[] toBeReturned = new byte[7];
					toBeReturned[0] = tempToBeReturned[1];				
					toBeReturned[1] = tempToBeReturned[2];
					toBeReturned[2] = tempToBeReturned[3];
					toBeReturned[3] = tempToBeReturned[4];
					toBeReturned[4] = tempToBeReturned[5];
					toBeReturned[5] = tempToBeReturned[6];
					toBeReturned[6] = tempToBeReturned[7];
					return toBeReturned;
				}
				
				
				//below that: 8 octets
				else {
					return tempToBeReturned;
				}
			}
			
			//if method fails for any reason, it returns null
			return null;
		}
	
	//---------------------------------------------ASN1 BYTE ARRAY TO VALUE------------------------------------------------------------------
	//INFO: for asn1 byte array to oid string please look at ObjectIdentifier class
	//INFO: for asn1 byte array to String please use the Java String xyz = new String(bytearray) method
	/**
	 * Converts an asn1 encoded value part of a TLV back to an integer, if possible. This is done via the java BigInteger class, 
	 * through a temporary BigInteger object which is constructed from the content of the input array.
	 * @param asn1IntValue - the asn1 encoded integer value
	 * @return the int value decoded from the input array
	 * @throws ValueNullException if the input array is null
	 * @throws ValueTooBigException if the input array is longer than 4 bytes and its content can therefore not be stored in an int
	 * @see #asn1Value_ByteArrayToLong(byte[])
	 * @see ObjectIdentifier#convertTLVValueToOID(byte[])
	 */
	public static int asn1Value_ByteArrayToInteger(byte[] asn1IntValue) throws ValueNullException, ValueTooBigException {
		//check if the array is null
		if(asn1IntValue == null) {
			throw new ValueNullException("The input is null, not an encoded integer");
		}
		//check if the value 0 has been encoded
		if((asn1IntValue[0] == 0) && (asn1IntValue.length == 1) ) {
			return 0;
		}
		//check if the value is too long to be converted to an integer
		if((asn1IntValue.length>4)) {
			throw new ValueTooBigException("Java integer can only hold 32 bits or 4 octets. The passed value is bigger than that");
		}
		
		BigInteger tmpInt = new BigInteger(asn1IntValue);
		return tmpInt.intValue();
	}
	
	/**
	 * Converts an asn1 encoded value part of a TLV back to a long, if possible. This is done via the java BigInteger class, 
	 * through a temporary BigInteger object which is constructed from the content of the input array.
	 * @param asn1LongValue - the asn1 encoded long value
	 * @return the long value decoded from the byte array
	 * @throws ValueNullException if the input array is null
	 * @throws ValueTooBigException if the input array is longer than 8 bytes and its content can therefore not be stored in a long
	 * @see #asn1Value_ByteArrayToInteger(byte[])
	 * @see ObjectIdentifier#convertTLVValueToOID(byte[])
	 */
	public static long asn1Value_ByteArrayToLong(byte[] asn1LongValue) throws ValueNullException, ValueTooBigException {
		//check if the array is null
		if(asn1LongValue == null) {
			throw new ValueNullException("The input is null, not an encoded long");
		}
		//check if the value 0 has been encoded
		if((asn1LongValue[0] == 0) && (asn1LongValue.length == 1) ) {
			return 0L;
		}
		//check if the value is too long to be converted to a long
		if((asn1LongValue.length>8)) {
			throw new ValueTooBigException("Java long can only hold 64 bits or 8 octets. The passed value is bigger than that");
		}
		
		BigInteger tmpLong = new BigInteger(asn1LongValue);
		return tmpLong.longValue();
	}
	
	
	
	
	//-----------------------------------------format conformity checks-----------------------------------------------------------------
	/**
	 * Checks whether or not an Object is a ASN1 Printable String. To be used to check all the supposed printable String that are 
	 * inputs from outside the TSE.
	 * @param supposedPrintableString the object that might be a printable string
	 * @return true, if the string representation of the object only contains characters permitted by the ASN1 definition of a printable string.<br>
	 * false, otherwise, especially if the supposedPrintableString is null.
	 * @version 1.0
	 * @since 1.0
	 */
	//only Upper and lower case letters, digits, space, apostrophe, left/right parenthesis,
	//plus sign, comma, hyphen ("Bindestrich -"), full stop, solidus ("Schrägstrich /"), colon ("doppelpunkt :"), equal sign, question mark  are allowed
	public static boolean isASN1_PrintableString(Object supposedPrintableString) {
		if(supposedPrintableString == null) {
			return false;
		}
		String toBeTested = supposedPrintableString.toString();
		
		String printableStringRegex = "^([A-Za-z0-9 '()+,-./:=?])*$";
		return toBeTested.matches(printableStringRegex);
	}
	
	
	
	/**
	 * Checks whether or not an Object is an ASN1 OctetString. To be used to check all supposed octetc strings that are inputs from outside the TSE.
	 * @param supposedOctetString the object that might be an octet string
	 * @return true, if the supposedOctetString is of type Byte[] (This is the boxed version of the primitive byte[]) <b>and</b> the supposedOctetString
	 * is not null.
	 * @version 1.0
	 * @since 1.0
	 */
	public static boolean isASN1_OctetString(Object supposedOctetString) {
		if((supposedOctetString != null) &&(supposedOctetString.getClass().isArray())) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checks whether or not an Object is an ASN1 Integer. To be used to check all supposed integers that are inputs from outside the TSE.
	 * Does currently only accept Objects that can be boxed to Integer, Long, and BigInteger.
	 * @param supposedInteger the object that might be an integer
	 * @return true, if the supposedInteger is of type Integer, Long or BigInteger, and not null
	 * @version 1.0
	 * @since 1.0
	 */
	public static boolean isASN1_Integer(Object supposedInteger) {
		//null is no Integer
		if(supposedInteger == null) {
			return false;
		}
		//Integer, Long, and BigInteger are considered Integers
		if(supposedInteger instanceof Integer) {
			return true;
		}
		else if(supposedInteger instanceof Long) {
			return true;
		}
		else if(supposedInteger instanceof BigInteger) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checks whether or not an Object is an ASN1 ObjectIdentifier. To be used if at some point in time,
	 * OIDs are provided from outside of the TSE. In version 1.0 this only supports the following formats:<br><br>
	 * {1 3 6 1 4 1}<br>
	 *  0.4.0.127.0.7.1.1.4.4.8<br>
	 *  urn:oid:1.3.6.1.4.1.123.0<br>
	 *  {iso(1) identified-organisation(3) dod(6) internet(1) private(4) enterprise(1)}<br>
	 *  
	 * <br> This format is not recognized in version 1.0: { iso(1) member-body(2) 840 113549 }
	 * @param supposedOID - the supposedOID that is converted to a String and checked for compliance to one of the formats above.
	 * @return true, if the supposedOID can be converted into a String which conforms to one of the known OID formats, and is not null
	 */
	public static boolean isASN1_ObjectIdentifier(Object supposedOID) {
		if(supposedOID == null) {
			return false;
		}
		String toBeTested = supposedOID.toString();
		
		boolean isURN = toBeTested.matches(Constants.OID_URN_REGEX);
		boolean isASN1simpleDots = toBeTested.matches(Constants.OID_ASN1_SIMPLE_DOTS_REGEX);
		boolean isASN1simpleSpaces = toBeTested.matches(Constants.OID_ASN1_SIMPLE_SPACES_REGEX);
		boolean isASN1advanced = toBeTested.matches(Constants.OID_ASN1_ADVANCED_REGEX);
		
		return (isURN||isASN1simpleDots||isASN1simpleSpaces||isASN1advanced);
	}
	

}
