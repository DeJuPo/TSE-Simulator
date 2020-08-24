package main.java.de.bsi.tsesimulator.tlv;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import org.bouncycastle.util.Arrays;

import main.java.de.bsi.tsesimulator.exceptions.TLVException;
import main.java.de.bsi.tsesimulator.exceptions.ValueNullException;
import main.java.de.bsi.tsesimulator.exceptions.ValueTooBigException;
import main.java.de.bsi.tsesimulator.exceptions.VerifyingOperationFailedException;
import main.java.de.bsi.tsesimulator.msg.SystemLogMessage;
import main.java.de.bsi.tsesimulator.msg.TransactionLogMessage;
import main.java.de.bsi.tsesimulator.tse.CryptoCore;
import main.java.de.bsi.tsesimulator.utils.Utils;

/**
 * A <b>T</b>ag <b>L</b>ength <b>V</b>alue ASN.1 object. Can convert itself into an ASN.1 DER encoded byte array and can decode those byte arrays back into 
 * TLVObjects.
 * @author dpottkaemper
 *
 */
public class TLVObject {
	private Tag tag;
	private byte[] value;
	
	//-----------------------------------SET TAG METHODS------------------------------------------------------------------
	
	/**
	 * This is used when the tag is already known in the class constructing the TLVObject. 
	 * The tag is already known in case of it being found in the ASN1Constants class.
	 * @param tagAsByte the tag as a single byte
	 */
	public void setTagWithByteElement(byte tagAsByte) {
		this.tag=new Tag();
		tag.setBytesFromByte(tagAsByte);
	}
	
	
	
	/**
	 *  This is used for all the IMPLICIT types used in the TR. 
	 * @param i the tag as an integer
	 */
	public void setTagWithIntegerElement(int i) {
		this.tag=new Tag();
		tag.setBytesFromInteger(i);
	}

	
	//-----------------------------------SET VALUE METHODS------------------------------------------------------------------
	
	/**
	 * Sets the value array from an element already encoded as a byte array. The input byte array must be encoded according to asn1 rules.
	 * @param valueAsByteArray the value as a byte array
	 */
	public void setValue(byte[] valueAsByteArray){
		this.value=valueAsByteArray;
	}
	
	
	/**
	 * Sets the value from an Integer element, encodes the Integer element according to asn1 rules. The value should, in the context of the simulator, 
	 * never be smaller than 0.
	 * @param valueAsInt the value as an integer
	 */
	public void setValueWithIntegerElement(int valueAsInt) {
		if(valueAsInt<0) {
			//value should never be < 0 but, there could be a different behavior implemented, if such a case should occur
			this.value=TLVUtility.integerToByteArray_ASN1_Value(valueAsInt);
		}
		else {
			this.value=TLVUtility.integerToByteArray_ASN1_Value(valueAsInt);
		}
	}

	
	/**
	 * Sets the value from a long element, converting it to an asn1 encoded INTEGER. he value should, in the context of the simulator, 
	 * never be smaller than 0.
	 * @param valueAsLong the value as a long
	 */
	public void setValueWithLongElement(long valueAsLong) {
		if((Long.compare(valueAsLong, 0))<0) {
			//value should never be < 0 but, there could be a different behavior implemented, if such a case should occur
			this.value=TLVUtility.longToByteArray_ASN1_Value(valueAsLong);
		}
		else {
			this.value=TLVUtility.longToByteArray_ASN1_Value(valueAsLong);
		}
	}
	
	/**
	 * Appends a TLVObject to a constructed TLVObject
	 * @param appendix the TLVObject that will be appended to this TLVObject
	 * @throws TLVException if the TLVObject is not constructed
	 * @throws ValueTooBigException if the value component of the appendix is too long to be encoded
	 * @throws ValueNullException if the value component of the appendix has not been set
	 */
	public void appendChild(TLVObject appendix) throws TLVException, ValueNullException, ValueTooBigException {
		if((this.getTag().isPrimitive())) {
				System.out.println(TLVUtility.byteArrayToBinaryString(this.tag.getTagContent()));
			throw new TLVException("Appendix cannot be appended to this TLVObject. TLVObject is not constructed!");
		}
		//convert appendix TLV to byte array
		byte[] appendixAsTLVByteArray = appendix.toTLVByteArray();
		//create new value byte array
		
		//if the current value byte array is empty (like with an empty SEQUENCE) just set the appendix TLV byte array as new value
		if(this.value==null) {
			this.setValue(appendixAsTLVByteArray);
		}
		//else: concat the previous value and the new value
		else {
			byte[] updatedValue = Utils.concatTwoByteArrays(value, appendixAsTLVByteArray);
			//set the new value
			this.setValue(updatedValue);
		}
		
	}
	
	/**
	 * Returns all children of a TLVObject in the form of an array.<br>
	 * Uses {@linkplain #decodeASN1ByteArrayToTLVObjectArray(byte[])}.
	 * @return a TLVObject array containing the child at position 0, if the child is a parent then it contains its child at position 1, ...
	 */
	public TLVObject[] getAllChildren() {
		if(this.tag.isConstructed()) {
			//call public static TLVObject[] decodeASN1ByteArrayToTLVObjectArray(byte[] inputArray) on the value and return the result
			try {
				return TLVObject.decodeASN1ByteArrayToTLVObjectArray(this.value);
			} catch (TLVException e) {
				e.printStackTrace();
			}
		}
		//otherwise return null 
		return null;
	}
	
	//-----------------------------------GET TAG METHODS------------------------------------------------------------------
	/**
	 * The getter method for the tag object.
	 * @return the tag object
	 */
	public Tag getTag() {
		return this.tag;
	}
	
	
	
	//-----------------------------------GET LENGTH METHODS------------------------------------------------------------------
	
	/**
	 * Computes and then encodes the length of the value array according to ASN1 DER standard.
	 * Should only be called, if the value has been assigned.
	 * @return an ASN1 DER encoded representation of the value.
	 * @throws ValueNullException the value has not been set or has been reset. Length encoding therefore not possible.
	 * 
	 * @throws ValueTooBigException the value has a length greater than 2^125 - 1 and can't be ASN1 DER encoded.
	 */
	public byte[] getLengthAsByteArray() throws ValueNullException, ValueTooBigException {
		//if there is no value, the value does not have a length
		//-> throw exception
		if(this.value==null) {
			//if it is an UNIVERSAL NULL (Tag == 0x05) return the length as a single 00 octet
			if(this.tag.isNull()) {
				byte[] legitimateNullValue = {0};
				return legitimateNullValue;
			}
			//else throw an exception, because it should have a value
			throw new ValueNullException("The value of this TLV is not set");
		}

		byte[] toBeReturned=null;
		
		//DER encoding: "When the length is between 0 and 127, the short form of length must be used".
		if(this.value.length<=127) {
			toBeReturned=new byte[1];
			toBeReturned[0]=(byte) this.value.length;
		}
		//DER encoding: "When the length is 128 or greater, the long form of length must be used"
		else {
			//1. determine number of bytes to be used. 
			// this is calculated by getting the log of this.value.length with base 256.
			// == (log(this.value.length))/(log(256)
			double numberOfBytesAsDouble = Math.log(this.value.length) / Math.log(256);
			
			//Math.floor, because we do not want to get "2" if the input is "1.9". That would not follow the principle of "minimal number of octets"
			long numberOfBytesAsLong = (long) Math.floor(numberOfBytesAsDouble);
			
			//because of log, numberOfBytes is actually one octet more. -> add 1
			numberOfBytesAsLong++;
			
			//2. check whether the number of bytes is bigger than 126. If yes, throw an exception
			if(numberOfBytesAsLong>126) {
				throw new ValueTooBigException("The length of the value exceeds 2^125 - 1 and can not be encoded.");
			}
			
			//3. store number of bytes that are used for the length encoding in the beginning of the byte array (the first octet). Set the first bit of that octet to 1.
			byte firstOctet = (byte) numberOfBytesAsLong;
			firstOctet +=0b10000000;
			
			//4. store the real length in the following octets. That is an array of the length "numberOfBytesAsLong"
			byte[] followingOctets = new byte[(int) numberOfBytesAsLong];
			
			//beautiful bit shifting...
			for(int i=0; i<numberOfBytesAsLong; i++) {
				followingOctets[i] = (byte) (this.value.length >> (i*8) & 0xff);		
			}

			//define the length of the byte array that will represent the length
			toBeReturned = new byte[(int) (numberOfBytesAsLong+1)];
			toBeReturned[0]=firstOctet;
			
			//reverse the followingOctets array and put it into toBeReturned. 
			//that means: followingOctets[followingOctets.length-1] will be toBeReturned[1]
			//only go from j=0 to j<toBeReturned.length-1 because otherwise toBeReturned[j+1] causes IndexOutOfBounds
			for(int j=0; j<toBeReturned.length-1;j++) {
				toBeReturned[j+1]=followingOctets[followingOctets.length-1-j];
			}
		}
		return toBeReturned;
	}

	/**
	 * Returns the length of the value array as a long. 
	 * For correctly ASN1 DER encoded length please use {@link #getLengthAsByteArray() getLengthAsByteArray} method.
	 * @return value.length as a long.
	 */
	public long getLengthAsLong() {
		if(this.tag.isNull()) {
			return 0;
		}
		return this.value.length;
	}
	
	
//----------------------------------------------------TLV Encoder & Decoder-------------------------------------------------------------------
	/**
	 * Converts the whole TLVObject into an ASN1 DER encoded byte array. 
	 * @return the TLVObject as a byte array
	 * @throws ValueTooBigException thrown by {@link #getLengthAsByteArray()} if the length is too big to be encoded.
	 * @throws ValueNullException thrown by {@link #getLengthAsByteArray()} if the value has not been set and therefore is null and the TLV it belongs to
	 * is <b>not</b> tagged as a NULL.
	 */
	public byte[] toTLVByteArray() throws ValueNullException, ValueTooBigException {
		//check if it's an UNIVERSAL NULL
		if(this.tag.isNull() && (this.value ==null)) {
			byte[] legitimateNullValue= {05, 00};
			return legitimateNullValue;
		}
		
		//determine length of byte array needed:
		int lengthOfTLVObject = this.tag.getTagContent().length + this.getLengthAsByteArray().length + this.value.length;

		byte[] toBeReturned = new byte[lengthOfTLVObject];
		int offset=0;
		
		//fill the tag content into toBeReturned
		for(byte tagArrayElement : this.getTag().getTagContent()) {
			toBeReturned[offset] = tagArrayElement;
			offset++;
		}
		//fill the length encoded to asn1 standards into toBeReturned
		for(byte lengthArrayElement : this.getLengthAsByteArray()) {
			toBeReturned[offset] = lengthArrayElement;
			offset++;
		}
		//fill the value into toBeReturned
		for(byte valueArrayElement : this.getValue()) {
			toBeReturned[offset] = valueArrayElement;
			offset++;
		}
		return toBeReturned;
		
	}
	
	
	/**
	 * Takes a byte array as an argument and decodes its contents according to ASN1 DER standard.
	 * Used in the TSE simulator to get the log time from the byte array that the CryptoCore returns to the ERSSpecificModule. <br>
	 * Note: the method distinguishes between primitive and constructed TLVs. If it encounters a constructed TLV, it adds this TLV to the array that it returns, then also adds the child (or children) of the constructed TLV to this array.
	 * 
	 * @param inputArray - one or more TLVs encoded as an ASN1 byte array.
	 * @return - an array of TLVObjects where each represents one TLV that had been encoded in the byte array.
	 * @throws TLVException - if this Exception is thrown by {@link #decodeASN1ByteArrayToTLVObject(byte[], int, int, int)} because it could not decode
	 * the current TLV. 
	 */
	public static TLVObject[] decodeASN1ByteArrayToTLVObjectArray(byte[] inputArray) throws TLVException {
		//create an ArrayList capable of storing the TLVObjects until they can be converted to a TLVObject[]
		ArrayList<TLVObject> temporalToBeReturned = new ArrayList<TLVObject>();
		
		//goes through the inputArray and always first checks the tag to see what type of content should be expected
		int offsetCurrentTag = 0;
		while(offsetCurrentTag<inputArray.length) {
			TLVObject currentTLV = null;
			
			//idea: use the decodeASN1ByteArrayToTLVObject
			boolean shortLength = ((inputArray[offsetCurrentTag+1] & 0x80) == 0);
			int numberOfValueOctets = 0;
			int numberOfLengthOctets = 0;
			int numberOfTagOctets = 1;
			//1. get the tag
			byte firstTagOctet = inputArray[offsetCurrentTag];
			
			//2. determine if the TLV is primitive
			if(!(((firstTagOctet >>> 5) & 1) != 0)) {
				//3. get the primitive tlv object
					//3.1 get the length
						//3.1.1 if the short form of length is used, increment the numberOfLengthOctets to 1 and set the numberOfValueOctets to what the length octet says
				if(shortLength) {
					numberOfValueOctets = inputArray[offsetCurrentTag + 1];
					numberOfLengthOctets++;
				}
						//3.1.2 if the long form of length is used, get the length from the TLVObject.getLengthFromLongFormOfLengthByteArray method
				else {
					numberOfLengthOctets = (inputArray[offsetCurrentTag + 1] & 0b01111111);		//first octet has msb set to 1 and the other bits encode numberOfLengthOctets
					byte[] lengthOctets = new byte[numberOfLengthOctets];		//		store the length octets
					
							//copy from the input array from position offsetCurrentTag+1+1 because (offsetCurrentTag+1) is 
							//the position of the octet which says how many octets are needed to encode the length
							//therefore, the real length encoding starts at position (offsetCurrentTag+2)
					System.arraycopy(inputArray, (offsetCurrentTag+2), lengthOctets, 0, lengthOctets.length);	//copy only the octets encoding the length
					
					
					numberOfValueOctets = TLVObject.getLengthFromLongFormOfLengthByteArray(lengthOctets);	//decode the log form of length from the byte array
																											//store it in "length"
					numberOfLengthOctets++;		//because the length-encoding octet only encodes the FOLLOWING length octets. itself not counted.
				}
				
				//4. create what will be fed into decodeASN1ByteArrayToTLVObject
				byte[] primitiveTLVasByteArray = new byte[numberOfValueOctets + numberOfLengthOctets + numberOfTagOctets];
				System.arraycopy(inputArray, offsetCurrentTag, primitiveTLVasByteArray, 0, primitiveTLVasByteArray.length);
				try {
					currentTLV = TLVObject.decodeASN1ByteArrayToTLVObject(primitiveTLVasByteArray, numberOfTagOctets, numberOfLengthOctets, numberOfValueOctets);
				} catch (TLVException e) {
					throw new TLVException(e.getMessage());
				}
			}
			
				//C. decode the constructed tlv object. 
			else {

				//D.1 get the length of the whole constructed TLV encoded as a byte array
					//D.1.1 if the short form of length is used, just save the length
				if(shortLength) {
					numberOfValueOctets = inputArray[offsetCurrentTag + 1];
					numberOfLengthOctets++;
				}
					//D.1.2 if the long form of length is used, get the length from the TLVObject.getLengthFromLongFormOfLengthByteArray method
				else {
					numberOfLengthOctets = (inputArray[offsetCurrentTag + 1] & 0b01111111);		//first octet has msb set to 1 and the other bits encode numberOfLengthOctets
					byte[] lengthOctets = new byte[numberOfLengthOctets];		//ggf new byte[numberOfLengthOctets -1]			store the length octets
					

					//copy from the input array from position offsetCurrentTag+1+1 because (offsetCurrentTag+1) is 
					//the position of the octet which says how many octets are needed to encode the length
					//therefore, the real length encoding starts at position (offsetCurrentTag+2)
					System.arraycopy(inputArray, (offsetCurrentTag+2), lengthOctets, 0, lengthOctets.length);	//copy only the octets encoding the length
			
					
					numberOfValueOctets = TLVObject.getLengthFromLongFormOfLengthByteArray(lengthOctets);	//decode the log form of length from the byte array
																											//store it in "length"
					numberOfLengthOctets++;		//because the length-encoding octet only encodes the FOLLOWING length octets. itself not counted.
				}
				
				//create the parent TLVObject from the byte array
				byte[] constructedTLVValueasByteArray = new byte[numberOfValueOctets];
				System.arraycopy(inputArray, (offsetCurrentTag+numberOfTagOctets+numberOfLengthOctets), constructedTLVValueasByteArray, 0, constructedTLVValueasByteArray.length);
				
				currentTLV = new TLVObject();
				currentTLV.setTagWithByteElement(firstTagOctet);
				currentTLV.setValue(constructedTLVValueasByteArray);
				
				//set the number of value octets to 0 so that "update currentOffsetTag" goes to the tag of the child (hopefully) and does not skip the 
				//child because it adds the whole length of the child to currentOffsetTag through the child being counted in the numberOfValueOctets of 
				//the parent
				numberOfValueOctets=0;
			}
			
			//add the newly created TLVObject to the temporal ArrayList
			temporalToBeReturned.add(currentTLV);
			
			//update the offsetCurrentTag
			offsetCurrentTag += numberOfValueOctets + numberOfLengthOctets + numberOfTagOctets;
		}
		//convert the ArrayList into a real TLVObject array and return that
		TLVObject[] toBeReturned = temporalToBeReturned.toArray(new TLVObject[temporalToBeReturned.size()]);
		return toBeReturned;
	}
	
	
	/**
	 * The inverse to {@linkplain #toTLVByteArray()}. Takes a single encoded TLV element as a byte array and creates a TLVObject from its content.
	 * Can currently only handle low tag number form tags. Also does not encode child TLVs, only their "parent" TLV with the child TLV being contained 
	 * in the value component of the parent.
	 * @param tlvByteArray - a single TLVObject in its encoded byte array form.
	 * @return - a TLVObject with the tag and value encoded in the tlvByteArray.
	 * @throws TLVException - If the tlvByteArray is too short to be an ASN1 encoded TLV, if the tag is in high tag number form and/or if the byte array's length does not match (numberOftagOctets + numberOflengthOctets + numberOfValueOctets).
	 */
	public static TLVObject decodeASN1ByteArrayToTLVObject(byte[] tlvByteArray) throws TLVException {
			
		//check if tag and length octet are present. if not: exception
		if(tlvByteArray.length < 2) {
			throw new TLVException("ASN1 TLVs are at least 2 octets long!");
		}
		
		//save the tag in a byte
		byte firstTagOctet = tlvByteArray[0];
		//check if the tag is in low number form. if not throw exception
		if((firstTagOctet << 3) == 0b11111000) {
			throw new TLVException("Does not support high tag number form!");
		}
		//create what will be returned
		TLVObject toBeReturned = new TLVObject();
		
		//check and save the length (the numberOfValueOctets)
		//determine if short form of length is used
		//short form of length is used if the msb of the length octet is 0
		//the length is encoded in the first octet after the tag octet. 
		//currently, high tag number form is not supported.
		boolean shortLength = ((tlvByteArray[1] & 0x80) == 0);
		int numberOfValueOctets = 0;
		int numberOfLengthOctets = 0;
		int numberOfTagOctets = 1;
		
		if(shortLength) {
			numberOfValueOctets = tlvByteArray[1];
			numberOfLengthOctets++;
		}
		else {
			numberOfLengthOctets = (tlvByteArray[1] & 0b01111111);		//first octet has msb set to 1 and the other bits encode numberOfLengthOctets
			byte[] lengthOctets = new byte[numberOfLengthOctets];		//store the length octets
			System.arraycopy(tlvByteArray, 2, lengthOctets, 0, lengthOctets.length);	//copy only the octets encoding the length,
																						//starting at position 2 because 1 is the octet
																						//encoding how many octets are needed to encode
																						//the length
			
			numberOfValueOctets = TLVObject.getLengthFromLongFormOfLengthByteArray(lengthOctets);	//decode the log form of length from the byte array
																									//store it in "length"
			numberOfLengthOctets++;		//because the length-encoding octet only encodes the FOLLOWING length octets. itself not counted.
		}
		//check if the numberOfValueOctets corresponds to the length of the tlvByteArray
		//tlvByteArray.length == numberOfValueOctets + numberOfLengthOctets + numberOfTagOctets(which is currently always 1) should be true
		//otherwise: exception
		if(tlvByteArray.length != (numberOfValueOctets + numberOfLengthOctets + numberOfTagOctets)) {
			throw new TLVException("The input byte array is either too short or too long!");
		}
		
		//check if the decoded TLV has a total length of 2 AND has the tag NULL
		//if that's the case, return a TLVObject with tag NULL and no value
		if(((numberOfValueOctets + numberOfLengthOctets + numberOfTagOctets) == 2) && (((firstTagOctet << 2) ^ 0b00010100) == 0)) {
			toBeReturned.setTagWithByteElement(firstTagOctet);
			return toBeReturned;
		}
		
		//does it really matter for the decoding of a single byte array to TLVObject if the Tag is primitive or not
		//check if the tlv is primitive or constructed
		boolean isPrimitiveTag = !(((firstTagOctet >>> 5) & 1) != 0);
		
		if(isPrimitiveTag) {
			toBeReturned.setTagWithByteElement(firstTagOctet); 		//set the tag element from the first octet of the byte array
			byte[] valueByteArray = new byte[numberOfValueOctets];	//create a new array where only the value octets are stored
			System.arraycopy(tlvByteArray, (numberOfTagOctets+numberOfLengthOctets), valueByteArray, 0, valueByteArray.length);
			toBeReturned.setValue(valueByteArray);
		}
		
		return toBeReturned;
	}
	
	
	/**
	 * Overloaded version of the {@linkplain #decodeASN1ByteArrayToTLVObject(byte[])} method. To be used by {@linkplain #decodeASN1ByteArrayToTLVObjectArray(byte[])}.
	 * The caller of this method has to calculate the number of tag octets, the number of length octets and the number of value octets.
	 * Note: can only be called on primitive TLVs because it does not check if its a primitive tlv or not.
	 * @param tlvByteArray - the whole encoded TLV including the tag and the length octets. Has to be (numberOftagOctets + numberOflengthOctets + numberOfValueOctets) long. This is checked in the method body.
	 * @param numberOfTagOctets - the number of tag octets. Must be counted by the calling method and currently only the value "1" is supported, as the TR-03151 does not specify high tag number form tags.
	 * @param numberOfLengthOctets - the number of length octets. Must be counted by the calling method and can be "1" or more, depending on what kiind of length encoding has been used.
	 * @param numberOfValueOctets - the number of value octets as encoded by the length octets. Must be provided by the calling method. 
	 * @return - a TLVObject with the tag and value encoded in the tlvByteArray.
	 * @throws TLVException - If the tlvByteArray is too short to be an ASN1 encoded TLV, if the tag is in high tag number form and/or if the byte array's length does not match (numberOftagOctets + numberOflengthOctets + numberOfValueOctets).
	 */
	private static TLVObject decodeASN1ByteArrayToTLVObject(byte[] tlvByteArray, int numberOfTagOctets, int numberOfLengthOctets, int numberOfValueOctets) throws TLVException {
		//check if tag and length octet are present. if not: exception
		if(tlvByteArray.length < 2) {
			throw new TLVException("ASN1 TLVs are at least 2 octets long!");
		}
		
		//save the tag in a byte
		byte firstTagOctet = tlvByteArray[0];
		//check if the tag is in low number form. if not throw exception
		if((firstTagOctet << 3) == 0b11111000) {
			throw new TLVException("Does not support high tag number form!");
		}
		//create what will be returned
		TLVObject toBeReturned = new TLVObject();
		
		
		//check if the numberOfValueOctets corresponds to the length of the tlvByteArray
		//tlvByteArray.length == numberOfValueOctets + numberOfLengthOctets + numberOfTagOctets(which is currently always 1) should be true
		//otherwise: exception
		if(tlvByteArray.length != (numberOfValueOctets + numberOfLengthOctets + numberOfTagOctets)) {
			throw new TLVException("The input byte array is either too short or too long!");
		}
		
		//check if the decoded TLV has a total length of 2 AND has the tag NULL
		//if that's the case, return a TLVObject with tag NULL and no value
		if(((numberOfValueOctets + numberOfLengthOctets + numberOfTagOctets) == 2) && (((firstTagOctet << 2) ^ 0b00010100) == 0)) {
			toBeReturned.setTagWithByteElement(firstTagOctet);
			return toBeReturned;
		}
		
		//set Tag & Value in the toBeReturned TLVObject
		toBeReturned.setTagWithByteElement(firstTagOctet); 		//set the tag element from the first octet of the byte array
		byte[] valueByteArray = new byte[numberOfValueOctets];	//create a new array where only the value octets are stored
		System.arraycopy(tlvByteArray, (numberOfTagOctets+numberOfLengthOctets), valueByteArray, 0, valueByteArray.length);
		toBeReturned.setValue(valueByteArray);
	
		return toBeReturned;
	}
	
	
	/**
	 * Decodes long form of length asn1 encoded byte arrays to long.
	 * Takes only the "following length octets", NOT the first length octet that encodes how many octets encode the length.
	 * Should normally only be called by {@linkplain #decodeASN1ByteArrayToTLVObjectArray(byte[])}.
	 * @param lengthOctets - the "following length octets" after the one that encodes how many octets are needed to encode the length.
	 * @return - an integer which represents the value encoded in the length octets.
	 */
	public static int getLengthFromLongFormOfLengthByteArray(byte[] lengthOctets) {
		//store the first octet in the toBeReturned
		int toBeReturned = 0;
		
		//if the first bit is 1, Java would interpret it as negative.
		if((lengthOctets[0] & 0x80) != 0) {
			toBeReturned = (lengthOctets[0] & 0b01111111);	//set all bits from the byte array except first one
			toBeReturned += 128;							//add 128 because that's what a set msb in the byte array means
		}
		//if the msb from the octet is not set, there is no problem with Java possibly interpreting the byte as a negative value
		else {
			toBeReturned = lengthOctets[0];
		}
		
		//if there are additional octets, save each in the tmp variable, then shift toBeReturned to the left one byte, then add tmp to toBeReturned
		for(int loopVar = 1; loopVar < lengthOctets.length ; loopVar++) {
			int tmp = 0;
			//check if tmp would be negative because of Java interpreting everything as signed
			if((lengthOctets[loopVar] & 0x80) != 0) {
				tmp = (lengthOctets[loopVar] & 0b01111111);	//set all bits from the byte array except first one
				tmp += 128;									//add 128 because that's what a set msb in the byte array means
			}
			//if the msb from the octet is not set, there is no problem with Java possibly interpreting the byte as a negative value
			else {
				tmp = lengthOctets[loopVar];
			}
			 
			toBeReturned = toBeReturned << 8;		//shift toBeReturned one byte to the left to make space for the next octet to be added
			toBeReturned +=tmp;
		}
		return toBeReturned;
	}

//_______________________________________________Verify Logmessage_____________________________________________________________________________________________
	/**
	 * This method can be used to verify a {@linkplain TransactionLogMessage} or a {@linkplain SystemLogMessage} using a file on the file system and a {@linkplain CryptoCore}.
	 * The caller of the method has to make sure, that the passed in CryptoCore does use the same signature algorithm on the same curve with the same keys that were used
	 * to create the log message. In short, the configuration file of this program has to be set mostly to the same values that were used in the creation of the log message<br>
	 * Please note: this method was created for internal testing purposes. It is not recommended, to use the TSE, that created the signature on a log message, to 
	 * verify that same log message.
	 * @param transysLog a {@linkplain TransactionLogMessage} or a {@linkplain SystemLogMessage} as a Java File object 
	 * @param cryptoCore a {@linkplain CryptoCore} with a configuration as mentioned above
	 * @return true, if the log message could be verified. False otherwise.
	 * @throws IOException if reading the Java File from the file system fails
	 * @throws VerifyingOperationFailedException if something unusual happens during verification. This can be anything from a parsing error to an error in {@linkplain CryptoCore#isVerified(byte[], byte[])} method.
	 * @since 1.5
	 * @see {@linkplain CryptoCore#isVerified(byte[], byte[])}, {@linkplain CryptoCore#verify}
	 */
	public static boolean verifyTransactionOrSystemLog(File transysLog, CryptoCore cryptoCore) throws IOException, VerifyingOperationFailedException {
		//1. read the file into a byte array
		byte[] logAsByteArray = Files.readAllBytes(transysLog.toPath());
		
		//2. create a TLVObject array from that
		TLVObject[] logAsTLVObjectArraySequenceWrapper = null;
		try {
			logAsTLVObjectArraySequenceWrapper = TLVObject.decodeASN1ByteArrayToTLVObjectArray(logAsByteArray);
		} catch (TLVException e) {
			throw new VerifyingOperationFailedException(e.getMessage(), e);
		}
		
		//3. the resulting TLVObject array does have the SEQUENCE wrapper that is added to the Log Message around it. We have to remove it:
		TLVObject[] logAsTLVObjectArray = TLVUtility.removeSEQUENCEWrapper(logAsTLVObjectArraySequenceWrapper);
		
		//4. the last TLVObject of the new array represents the signature. Isolate the value of that:
		byte[] signatureValue = logAsTLVObjectArray[logAsTLVObjectArray.length - 1].getValue();
		
		//5. with the signature value isolated, we now have to create the byte array that has been signed and created the aforementioned signature
		//Problem: the logAsTLVObjectArray does contain the signatureAlgorithm field, that is defined as a SEQUENCE containing an ObjectIdentifier
			//ugly but quick solution: identify the TLVObject[i] (can be multiple!) which must be removed to create the value that was signed originally.
			//That means, remove the signatureValue TLVObject and the TLVObject representing the value of the SEQUENCE TLV signatureAlgorithm
		
		//5.1: create an array holding everything from the beginning of the log message to the signatureAlgorithm Sequence (inclusive)
			TLVObject[] tempLogUpper = new TLVObject[logAsTLVObjectArray.length - 4];	// -4 = signatureAlgorithm Sequence
			System.arraycopy(logAsTLVObjectArray, 0, tempLogUpper, 0, (logAsTLVObjectArray.length - 4));
		//5.2: create an array holding only the two values missing: logTime and signatureCounter. Signature itself does obviously not contribute to the signature computation	
			TLVObject[] tempLogLower = new TLVObject[2];
			System.arraycopy(logAsTLVObjectArray, (tempLogUpper.length + 1), tempLogLower, 0, 2);
		//5.3: combine the two arrays:
			TLVObject[] tempLogCombined = Utils.concatAll(tempLogUpper, tempLogLower);	
				
		//6. create the byte array holding the whole TLVObjects from tempLogCombined as byte arrays in ASN.1 DER form
		byte[] signedValue = null;
		//go through whole tempLogCombined array and convert each TLVObject to an ASN.1 byte array. Then append the result to signedValueAsArrayList
		try {
			for(TLVObject currentObject : tempLogCombined) {
				byte[] currentObjectAsByteArray = currentObject.toTLVByteArray();
				signedValue = Utils.concatTwoByteArrays(signedValue, currentObjectAsByteArray);
			}
		} catch(ValueNullException |ValueTooBigException e) {
			throw new VerifyingOperationFailedException(e.getMessage(), e);
		} 

		//7. now use the CryptoCore to verify the signature and the values that were signed
		return cryptoCore.isVerified(signatureValue, signedValue);
	}
	
	/**
	 * <b>Not yet tested, since AuditLogs are not yet implemented!</b><br>
	 * This method can be used to verify an AuditLog using a file on the file system and a {@linkplain CryptoCore}.
	 * The caller of the method has to make sure, that the passed in CryptoCore does use the same signature algorithm on the same curve with the same keys that were used
	 * to create the log message. In short, the configuration file of this program has to be set mostly to the same values that were used in the creation of the log message<br>
	 * Please note: this method was created for internal testing purposes. It is not recommended, to use the TSE, that created the signature on a log message, to 
	 * verify that same log message.<br>
	 * Only one additional value is present in audit logs when compared to the other log message types. That is <i>seAuditData</i>. This is the only reason, 
	 * a separate method dealing with AuditLogs was needed.
	 * @param auditLog an AuditLog as a Java File object 
	 * @param cryptoCore a {@linkplain CryptoCore} with a configuration as mentioned above
	 * @return true, if the log message could be verified. False otherwise.
	 * @throws IOException if reading the Java File from the file system fails
	 * @throws VerifyingOperationFailedException if something unusual happens during verification. This can be anything from a parsing error to an error in {@linkplain CryptoCore#isVerified(byte[], byte[])} method.
	 * @since 1.5
	 * @see {@linkplain CryptoCore#isVerified(byte[], byte[])}, {@linkplain CryptoCore#verify}
	 * @deprecated Due to AuditLogs not being implemented, this method could not yet be tested.
	 */
	public static boolean verifyAuditLog(File auditLog, CryptoCore cryptoCore) throws IOException, VerifyingOperationFailedException {
		//1. read the file into a byte array
		byte[] logAsByteArray = Files.readAllBytes(auditLog.toPath());
		
		//2. create a TLVObject array from that
		TLVObject[] logAsTLVObjectArraySequenceWrapper = null;
		try {
			logAsTLVObjectArraySequenceWrapper = TLVObject.decodeASN1ByteArrayToTLVObjectArray(logAsByteArray);
		} catch (TLVException e) {
			throw new VerifyingOperationFailedException(e.getMessage(), e);
		}
		
		//3. the resulting TLVObject array does have the SEQUENCE wrapper that is added to the Log Message around it. We have to remove it:
		TLVObject[] logAsTLVObjectArray = TLVUtility.removeSEQUENCEWrapper(logAsTLVObjectArraySequenceWrapper);
		
		//4. the last TLVObject of the new array represents the signature. Isolate the value of that:
		byte[] signatureValue = logAsTLVObjectArray[logAsTLVObjectArray.length - 1].getValue();
		
		//5. with the signature value isolated, we now have to create the byte array that has been signed and created the aforementioned signature
		//Problem: the logAsTLVObjectArray does contain the signatureAlgorithm field, that is defined as a SEQUENCE containing an ObjectIdentifier
			//ugly but quick solution: identify the TLVObject[i] (can be multiple!) which must be removed to create the value that was signed originally.
			//That means, remove the signatureValue TLVObject and the TLVObject representing the value of the SEQUENCE TLV signatureAlgorithm
		
		//5.1: create an array holding everything from the beginning of the log message to the signatureAlgorithm Sequence (inclusive)
			TLVObject[] tempLogUpper = new TLVObject[logAsTLVObjectArray.length - 5];	// -5 = signatureAlgorithm Sequence, not -4 because AuditLogs MUST have seAuditData
			System.arraycopy(logAsTLVObjectArray, 0, tempLogUpper, 0, (logAsTLVObjectArray.length - 5));
		//5.2: create an array holding only the three values missing: seAuditData, logTime and signatureCounter. Signature itself does obviously not contribute to the signature computation	
			TLVObject[] tempLogLower = new TLVObject[3];
			System.arraycopy(logAsTLVObjectArray, (tempLogUpper.length + 1), tempLogLower, 0, 3);
		//5.3: combine the two arrays:
			TLVObject[] tempLogCombined = Utils.concatAll(tempLogUpper, tempLogLower);	
				
		//6. create the byte array holding the whole TLVObjects from tempLogCombined as byte arrays in ASN.1 DER form
		byte[] signedValue = null;
		//go through whole tempLogCombined array and convert each TLVObject to an ASN.1 byte array. Then append the result to signedValueAsArrayList
		try {
			for(TLVObject currentObject : tempLogCombined) {
				byte[] currentObjectAsByteArray = currentObject.toTLVByteArray();
				signedValue = Utils.concatTwoByteArrays(signedValue, currentObjectAsByteArray);
			}
		} catch(ValueNullException |ValueTooBigException e) {
			throw new VerifyingOperationFailedException(e.getMessage(), e);
		} 

		//7. now use the CryptoCore to verify the signature and the values that were signed
		return cryptoCore.isVerified(signatureValue, signedValue);
	}

	//-----------------------------------GET VALUE METHODS, TO STRING METHODS------------------------------------------------------------------
	/**
	 * Getter method for the value. Throws a java.lang.NullPointerException if the value is null.
	 * @return the value byte array.
	 */
	public byte[] getValue() {
		return this.value;
	}
	
	/**
	 * Represents the TLVObject as a human readable String with the usage of {@link main.java.de.bsi.tsesimulator.tlv.Tag#toString()} for the Tag, 
	 * {@link java.lang.Integer#toHexString(int)} for the length and {@link main.java.de.bsi.tsesimulator.tlv.TLVUtility#byteArrayToString(byte[])} for the value.
	 * @return a human readable representation of the TLVObject
	 */
	public String toString() {
		StringBuilder str = new StringBuilder("Tag = " +this.tag.toString() +"\t");
		str.append(" Length = " +Integer.toHexString(this.value.length) +"\t");
		str.append(" Value = " +TLVUtility.byteArrayToString(value) +"\n");
		return str.toString();
	}
	
	/**
	 * Represents the TLVObject as a human readable String in binary form separated by indicators for the elements "Tag", "Length" and "Value".
	 * Uses {@link main.java.de.bsi.tsesimulator.tlv.Tag#toBinaryStringWithLeadingZeroes()} for the tag and
	 *  {@link main.java.de.bsi.tsesimulator.tlv.TLVUtility#byteArrayToBinaryString(byte[])} for the ASN1 DER encoded length and the value.
	 * @return a binary String representation of the TLVObject.
	 */
	public String toBinaryString() {
		StringBuilder str = new StringBuilder("Tag = " +this.tag.toBinaryStringWithLeadingZeroes() +"\t");
		try {
			str.append("Length = " +TLVUtility.byteArrayToBinaryString(getLengthAsByteArray()) +"\t");
		} catch (ValueNullException | ValueTooBigException e) {
			e.printStackTrace();
		}
		str.append("Value = " +TLVUtility.byteArrayToBinaryString(value) +"\n");
		return str.toString();
	}

	/**
	 * Static method to convert the contents of one TLVObject array to a String representation.
	 * Due to how SEQUENCE TLVs are currently handled, a sequence first appears with its contents as the value and then each content
	 * tlv appears.
	 * @param tlvobjects - an array of TLVObject objects 
	 * @return a pretty human-readable representation of the content of each TLVObject in the array
	 */
	public static String tlvObjectArrayToString(TLVObject[] tlvobjects) {
		StringBuilder stringBuilder = new StringBuilder("Tag in Hex\tLength in Hex\tValue in Hex\n");
		for(TLVObject obj : tlvobjects) {
			stringBuilder.append(obj.toString());
		}
		return stringBuilder.toString();
	}
	
	
	/**
	 * Determines whether 2 TLVObjects are equal. They are equal, if their tags and their value are equal.
	 * Used mainly for testing purposes.
	 * @param tlvObj the TLVObject to compare this to
	 * @return true, if they are equal, false otherwise
	 */
	public boolean equals(TLVObject tlvObj) {
		boolean tagEqual = Arrays.areEqual(this.getTag().getTagContent(), tlvObj.getTag().getTagContent());
		boolean valueEqual = Arrays.areEqual(this.getValue(), tlvObj.getValue());
		
		boolean isEqual =tagEqual && valueEqual;
		return isEqual;
	}
	

}
