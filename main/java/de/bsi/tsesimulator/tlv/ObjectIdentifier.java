package main.java.de.bsi.tsesimulator.tlv;
/**
 * This class converts the object identifiers used in the TSE-simulator from String to byte[].
 * That means, converting it from the human readable form into the ASN.1 encoded form. 
 * 
 * @author dpottkaemper
 *
 */
public class ObjectIdentifier {
	
	/**
	 * Class only provides static methods, therefore should not be instantiated. 
	 */
	private ObjectIdentifier() {}
	
	/**
	 * converts OIDs to value components according to ASN1. 
	 * Can handle OIDs with values greater than 127 in them by calling {@linkplain #advancedConvertOIDtoTLVValue(int[])} in the method body if necessary.
	 * @param OID an object identifier in the "dot" notation, e.g. 0.4.0.127.0.7.3.7.1.1
	 * @return a byte array representation of the OID specified by the String
	 */
	public static byte[] convertOIDtoTLVValue(String OID) {
		//split the OID at the dots and produce an array of Strings
		String[] splitOID = OID.split("\\.");
		//create a new integer array to hold the values so they can be used to do maths
		int[] splitIntOID = new int[splitOID.length];
		int loopVariable = 0;
		for(String str : splitOID) {
			splitIntOID[loopVariable] = Integer.parseInt(str);
			loopVariable++;
		}
		//check if OID contains a number greater than 127
		boolean advancedOID = false;
		for(String str : splitOID) {
			int i = Integer.parseInt(str);
			if(i>127) {
				advancedOID=true;
				break;
			}
		}
		//if OID contains a number greater than 127, use another method and return its result
		if(advancedOID) {
			return advancedConvertOIDtoTLVValue(splitIntOID);
		}

			//only reached if no advanced OID
		//create the byte array that will be returned
		//in the context of the TSE simulator, there are no OIDs containing values greater than 127
		//therefore, the asn1Encoded byte array should have a length of ((number of dot-separated values in the OID) - 1)
		byte[] asn1EncodedOID = new byte[splitOID.length-1];
		
		
		//The first octet has value (40 * value1) + value2
		asn1EncodedOID[0] = (byte) ((40*splitIntOID[0]) + splitIntOID[1]);
		
		
		//The following octets, if any, encode value3, ..., valuen. 
        //Each value is encoded base 128, most significant digit first, with as few digits as possible, 
        //and the most significant bit of each octet except the last in the value's encoding set to "1." 
		
		if(splitIntOID.length > 3) {
			//Start with splitIntOID[2], as 0 and 1 are already encoded
			for(int i=2; i<splitIntOID.length; i++) {
				//cast each int to byte. should not be a problem in this context
				asn1EncodedOID[i-1] = (byte) splitIntOID[i];
			}
		}

		return asn1EncodedOID;
	}
	
	
	
	/**
	 * Converts OIDs to value components according to ASN1 standards. 
	 * Shall be called when the OID to be converted contains a number greater than 127.
	 * Shall only be called by {@link #convertOIDtoTLVValue(String)}.
	 * @param splitIntOID an integer array filled with the content of the String array. Necessary to perform calculations on the numbers.
	 * @return a byte array representation of the OID specified by the String array
	 */
	public static byte[] advancedConvertOIDtoTLVValue(int[] splitIntOID) {
		//determine how many octets are needed, to create asn1EncodedAdvancedOID with the correct length
		int numberOfOctets = 0;	
		
		for(int i : splitIntOID) {
			//determine how many octets are necessary for encoding splitIntOID[i] 
			int toBeAdded = (int) ((Math.floor(Math.log(i) / Math.log(128))) + 1);
			numberOfOctets+=toBeAdded;
		}
		//Subtract 1 from numberOfOctets, since the first 2 octets are encoded into one single octet
		numberOfOctets--;

		//create the byte array that will be returned
		byte[] asn1EncodedAdvancedOID = new byte[numberOfOctets];
		//first octet is (value1*40) + value2 
		asn1EncodedAdvancedOID[0] = (byte) ((40*splitIntOID[0]) + splitIntOID[1]);
		
		//offset is the added space from numbers which use more than one octet to be encoded
		int offset = 0;
		//iterate through rest of OID
		for(int k=2; k<splitIntOID.length; k++) {
			//if the current number is not advanced, just encode it
			if(splitIntOID[k]<128) {
				asn1EncodedAdvancedOID[k-1+offset]=(byte) splitIntOID[k];
			}
			//else use the variableLengthEncoding method to encode one number into a byte array and then add that to asn1EncodedAdvancedOID
			else {
				byte[] octetsToBeAdded=variableLengthEncoding(splitIntOID[k]);
				
				//add content of octetsToBeAdded to asn1EncodedAdvanced
				for(int l=0; l<octetsToBeAdded.length; l++) {
					asn1EncodedAdvancedOID[k-1+offset+l] = octetsToBeAdded[l];
				}
				
				//update the offset
				//only .length-1 because one octet would always be added for each number. 
				//The offset counts the octets that are "additional" because of the encoding
				offset+=(octetsToBeAdded.length-1);	
				
			}
		}	//end of outer for loop
		return asn1EncodedAdvancedOID;
	}

	/**
	 * Used by {@link #advancedConvertOIDtoTLVValue(String[], int[])}, performs ASN.1 variable length encoding.
	 * Is used to encode the length of a value if the length would exceed the normally available single octet for length encoding.
	 * @param toBeEncoded the integer that shall be encoded 
	 * @return a byte array encoding that integer base 128
	 */
	public static byte[] variableLengthEncoding(int toBeEncoded) {
		//number of octets needed to encode the value toBeEncoded in base 128
		int toBeAdded = (int) ((Math.floor(Math.log(toBeEncoded) / Math.log(128))) + 1);
		byte[] toBeReturned = new byte[toBeAdded];

		toBeReturned[toBeReturned.length-1]= (byte) toBeEncoded;
		
		//if the msb of the least significant octet is 1, set it to 0
		if(toBeReturned[toBeReturned.length-1] <= -1) {
			toBeReturned[toBeReturned.length-1] += 0b10000000;
		}
		
		//go through toBeReturned from the penultimate entry to the first and fill it with 
		//tobeEncoded bit-shifted multiples of 7 bits to the right and left-padded with zeroes.
		//then set the msb of each octet of toBeReturned to 1
		int loopVar=1;
		for(int i=toBeReturned.length-2; i>=0; i--) {
			toBeReturned[i] = (byte) (toBeEncoded >>> (loopVar*7));
			toBeReturned[i] +=0b10000000;
			loopVar++;
		}
		
		return toBeReturned;
	}
	
	
	/**
	 * The inverse method to {@linkplain #convertOIDtoTLVValue(String)}. 
	 * Takes a byte array that represents an ASN1 DER encoded OID and converts it to a String.
	 * Note: can only handle values that do not exceed the capacity of an integer
	 * @param tlvOIDValue the value byte array of an OID which has been encoded as an ASN1 DER TLV. 
	 * 
	 * @return a String in the "dot" notation that represents the OID. Example:  0.4.0.127.0.7.3.7.1.1
	 * @see TLVUtility#asn1Value_ByteArrayToInteger(byte[])
	 * @see TLVUtility#asn1Value_ByteArrayToLong(byte[])
	 */
	public static String convertTLVValueToOID(byte[] tlvOIDValue) {
		StringBuilder oidBuilder = new StringBuilder();
			//decode first octet
		//(40*value1) + value2 = firstOctet
		int value1 = tlvOIDValue[0] / 40;
		int value2 = tlvOIDValue[0] % 40;
		oidBuilder.append(value1).append('.').append(value2);
		
		//position serves as a loop varible
		//decoding starts at tlvOIDvalue[1] because first octet has already been decoded above
		int position = 1;

		while(position < tlvOIDValue.length) {
			//check if current OID part is encoded using a single octet
			if((tlvOIDValue[position] & 0x80) == 0) {
				//first bit is 0 -> no additional effort necessary
				oidBuilder.append('.').append(tlvOIDValue[position]);
				position++;
			}														
			else {
				boolean doneFlag = false;		//indicates if the end of the long encoded oid value has been reached
				int decodedOID = 0;				//saves the decoded value 
				
				do {
					int tmp = (tlvOIDValue[position] & 0b01111111);		//tmp saves the integer value from the current octet. masks first bit, because thats not part of the value
					
					//check if the last octet of the long length encoded value has been reached
					if((tlvOIDValue[position] & 0x80) == 0) {
						doneFlag = true;
					}	
					//shift the current value of the decodedOID 7 bits to the left, 
					//because long length encoded values are encoded with the MSB in the first octet.
					//then add the newest octet as int value to the decodedOID
					decodedOID = decodedOID << 7;
					decodedOID += tmp;
					
					//increase position
					position++;
					
				} while ((position < tlvOIDValue.length) && !doneFlag);	
				
				oidBuilder.append('.').append(decodedOID);
			}	//else end
		}	//outer while end
		return oidBuilder.toString();
	}
	

}
