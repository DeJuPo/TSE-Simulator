package main.java.de.bsi.tsesimulator.tlv;

import org.bouncycastle.util.Arrays;

import main.java.de.bsi.tsesimulator.exceptions.ValueNullException;
import main.java.de.bsi.tsesimulator.exceptions.ValueTooBigException;

/**
 * Represents an ASN.1 tag. <br>
 * Note: this class only supports low-tag number form.  
 * @author dpottkaemper
 * @since 1.0
 */
public class Tag {
	private byte[] tagContent;
	
	/**
	 * This is used when the tag is already known in the class constructing the TLVObject. 
	 * The tag is already known in case of it being found in the ASN1Constants class.
	 * 
	 * It should only be used when the tag does not exceed the capacity of one byte, e.g. the tag is equal or smaller than 0x7F.
	 * 0x80 should therefore not be used.
	 * 
	 * @param tagAsByte the tag in byte form, e.g. 0x02. Must be passed down from the class constructing a TLVObject.
	 * @since 1.0
	 */
	public void setBytesFromByte(byte tagAsByte) {
		tagContent=new byte[1];
		this.tagContent[0] = tagAsByte;
	}
	
	
	
	/**
	 * This is used for the IMPLICIT tags found in the TR.
	 * The tags must be passed down from the class constructing the TLVObject.
	 * @param tagAsInt the tag in integer form, e.g. 0x80. must be passed down from the class constructing a TLBObject.
	 */
	public void setBytesFromInteger(int tagAsInt) {
		// tag content should fit in one byte when using the implicit tagged types from the TR
		tagContent=new byte[1];
		this.tagContent[0] = (byte) tagAsInt;
	}
	

	/**
	 * This is used mainly for testing purposes, when one already has the tagContent value as a byte array.
	 * 
	 * Note: current version of simulator does not support ASN.1 tags longer than one byte.
	 * @param inputArray the tag as a byte array
	 * @throws ValueNullException if the array passed in is null
	 * @throws ValueTooBigException if the array is longer than one byte
	 */
	public void setBytesFromByteArray(byte[] inputArray) throws ValueNullException, ValueTooBigException {
		if(inputArray == null) {
			throw new ValueNullException("ASN.1 Tag can not be set from null array.");
		}
		if(inputArray.length > 1) {
			throw new ValueTooBigException("This simulator version does not support ASN.1 Tags longer than one byte!");
		}
		
		this.tagContent=inputArray;
	}
	
	
	
	/**
	 * Standard getter method for the tagContent
	 * @return
	 */
	public byte[] getTagContent() {
		return this.tagContent;
	}
	
	//------------------------------------------CHECKS FOR "IS XYZ"---------------------------------------
	/**
	 * Checks if the tag is Universal.
	 * @return true if the tag is UNIVERSAL.
	 */
	//universal if bit 8=0 && bit 7=0
	public boolean isUniversal() {
		byte firstTagOctet = this.tagContent[0];
		boolean bit8set = (((firstTagOctet >>> 7) & 1) != 0);			//is the bit that represents 2^7 (the 8th bit) set? 
		boolean bit7set = (((firstTagOctet >>> 6) & 1) != 0);
		
		return ((!bit8set) && (!bit7set));
	}
	
	/**
	 * Checks if the tag is Application
	 * @return true if the tag is APPLICATION
	 */
	//application if bit 8 = 0 && bit 7 = 1
	public boolean isApplication() {
		byte firstTagOctet = this.tagContent[0];
		boolean bit8set = (((firstTagOctet >>> 7) & 1) != 0);
		boolean bit7set = (((firstTagOctet >>> 6) & 1) != 0);
		
		return ((!bit8set) && bit7set);
	}
	
	/**
	 * Checks if the tag is Context Specific
	 * @return true if the tag is CONTEXT SPECIFIC
	 */
	//context specific if bit 8 = 1 && bit 7 = 0
	public boolean isContextSpecific() {
		byte firstTagOctet = this.tagContent[0];
		boolean bit8set = (((firstTagOctet >>> 7) & 1) != 0);
		boolean bit7set = (((firstTagOctet >>> 6) & 1) != 0);
		
		return (bit8set && (!bit7set));
	}
	
	/**
	 * Checks if the tag is Private
	 * @return true if the tag is PRIVATE
	 */
	//private if bit 8 = 1 && bit 7 = 1
	public boolean isPrivate() {
		byte firstTagOctet = this.tagContent[0];
		boolean bit8set = (((firstTagOctet >>> 7) & 1) != 0);
		boolean bit7set = (((firstTagOctet >>> 6) & 1) != 0);
		
		return (bit8set && bit7set);
	}
	
	
	/**
	 * Check if the tag is primitive
	 * @return true if the tag is primitive
	 */
	public boolean isPrimitive() {
		byte firstTagOctet = this.tagContent[0];
		
		boolean isSetBit6 = (((firstTagOctet >>> 5) & 1) != 0);
		
		return (!isSetBit6);
	}
	
	/**
	 * Check if the tag is constructed (and can be appended)
	 * @return true if the tag is constructed
	 */
	public boolean isConstructed() {
		byte firstTagOctet = this.tagContent[0];
		boolean isSetBit6 = (((firstTagOctet >>> 5) & 1) != 0);
		
		return isSetBit6;
	}
	
	/**
	 * Check if the tag is of type "null" and therefore has no value
	 * @return true if the tag is null
	 */
	public boolean isNull() {
		byte firstTagOctet = this.tagContent[0];
		//mask is a class name of primitive NULL already shifted to the left 2 times
		byte mask = 0b00010100;
		
		//shift the first tag octet 2 bits to the left padding with zeroes to the right, to get rid of the first two class bits
		firstTagOctet = (byte) (firstTagOctet << 2);
		
		//compare shifted first octet with the mask per XOR. if the tag is a NULL, it should equal zero.
		byte shouldBeZeroIfTagIsNull = (byte) (mask ^firstTagOctet);
		return (shouldBeZeroIfTagIsNull == 0);
	}
	
	/**
	 * Check if the tag is constructed and is a SEQUENCE.
	 * Used for checking, if a TLVObject[] given to the constructor of a LogMessage is with the SEQUENCE wrapper or without.
	 * @return true, if the tag is a SEQUENCE. currently returns false every time because it is not yet used.
	 */
	public boolean isSequence() {
		byte firstTagOctet = this.tagContent[0];
		//mask is a class name of constructed SEQUENCE already shifted to the left 2 times
		byte mask = (byte) 0b11000000;
		
		//shift the first tag octet 2 bits to the left padding with zeroes to the right, to get rid of the first two class bits
		firstTagOctet = (byte) (firstTagOctet << 2);
		
		//compare shifted first octet with the mask per XOR. if the tag is a NULL, it should equal zero.
		byte shouldBeZeroIfTagIsSequence = (byte) (mask ^firstTagOctet);
		
		//return the combination of bit mask check and constructed check
		return ((shouldBeZeroIfTagIsSequence == 0) && (this.isConstructed()));
	}
	
	//------------------------------------------TOSTRING METHODS---------------------------------------------
	
	/**
	 * Uses main.java.de.bsi.tsesimulator.tlv.TLVUtility to create a string representation for the tagContent.
	 * The content is separated by a single space for readability.
	 */
	public String toString() {
		return TLVUtility.byteArrayToString(tagContent);
	}
	
	
	/**
	 * Returns the binary representation of each element of the tagContent via the Integer.toBinaryString() method.
	 * This leads to leading zeroes being cut from the string.
	 * @return the String representation without leading zeros.
	 */
	public String toBinaryString() {
		StringBuilder str = new StringBuilder();
		for(byte b : this.tagContent) {
			str.append(Integer.toBinaryString(b));
			str.append(" ");
		}
		return str.toString();
	}
	
	/**
	 * Returns the binary representation of each element of the tagContent via the Integer.toBinaryString() method and adds leading zeros.
	 * This leads to a full representation of the tagContent as 8 character long substrings
	 * @return the String representation with leading zeros.
	 */
	public String toBinaryStringWithLeadingZeroes() {
		StringBuilder str = new StringBuilder();
		for(byte b : this.tagContent) {
			str.append(("00000000"+Integer.toBinaryString(b)).substring(Integer.toBinaryString(b).length()));
			str.append(" ");
		}
		return str.toString();
	}
	
	/*
	 * Two Tag objects are equal, if their tag content is equal. This method uses a bouncycastle.org method to check whether arrays are equal.
	 */
	public boolean equals(Tag tagObj) {
		boolean isEqual = Arrays.areEqual(this.getTagContent(), tagObj.getTagContent());
		return isEqual;
	}
	
}
