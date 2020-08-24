/**
 * 
 */
package main.java.de.bsi.tsesimulator.utils;

import java.math.BigInteger;
import java.util.Arrays;

import main.java.de.bsi.tsesimulator.exceptions.TR_03111_ECC_V2_1_Exception;
import main.java.de.bsi.tsesimulator.tse.crypto.ECSDSA;
import main.java.de.bsi.tsesimulator.tse.crypto.SignatureAlgorithm;

/**
 * Implementations of functions mentioned in BSI TR-03111. 
 * @author dpottkaemper
 * @since 1.0
 */
public class TR_03111_Utils {

	/**
	 * Class only provides static methods, therefore should not be instantiated. 
	 */
	private TR_03111_Utils() {}
	
	
	
	
	/**
	 * Overloaded variant of {@linkplain #I2OS(int, int)} used to deal with BigIntegers as they appear in field elements. <br> Pads from the left with zeroes if necessary.
	 * @param x a positive BigInteger to be converted.
	 * @param length the desired length of the octet string.
	 * @return an octet string representation of x.
	 * @throws TR_03111_ECC_V2_1_Exception if x is negative or the desired length of the octet string is too short for x.
	 * @since 1.0
	 */
	public static byte[] I2OS(BigInteger x, int length) throws TR_03111_ECC_V2_1_Exception {
		if(x.compareTo(BigInteger.ZERO) < 0) {
			throw new TR_03111_ECC_V2_1_Exception("x has to be positive in order to be converted!");
		}
		
		BigInteger limit = new BigInteger("256");
		limit = limit.pow(length);
		
		if(x.compareTo(limit) >= 0) {
			throw new TR_03111_ECC_V2_1_Exception("Length l has to satisfy 256^l > x.");
		}
		
		//create what will be returned
		byte[] toBeReturned = new byte[length];
		byte nothing = 0;
		Arrays.fill(toBeReturned, nothing);
		
		//convert the BigInteger into a byte array
		byte[] xAsByteArray = x.toByteArray();
		
		//go from the lsb to the msb through x and assign the lsb of x to the lsb of toBeReturned, ...
		for(int positionsGoneLeft = 0; (positionsGoneLeft <= xAsByteArray.length-1) && (positionsGoneLeft <= toBeReturned.length-1); positionsGoneLeft++) {
			toBeReturned[toBeReturned.length-1-positionsGoneLeft] = xAsByteArray[xAsByteArray.length-1-positionsGoneLeft];
		}
		return toBeReturned;
	}
	
	
	
	/**
	 * Interpretation of the TR-03111 I2OS. <br> Takes an int as input and returns a byte array.<br> Might pad with zeroes from the left if necessary.
	 * @param x - the positive integer that shall be converted. 
	 * @param length -  the desired length of the octet string. 
	 * @return an octet string that represents the converted integer with the MSB at position 0.
	 * @throws TR_03111_ECC_V2_1_Exception
	 * @since 1.0
	 */
	public static byte[] I2OS(int x, int length) throws TR_03111_ECC_V2_1_Exception {
		if(x<0) {
			throw new TR_03111_ECC_V2_1_Exception("x has to be positive in order to be converted!");
		}
		
		if(Math.pow(256, length) <= x) {
			throw new TR_03111_ECC_V2_1_Exception("Length l has to satisfy 256^l > x.");
		}
		
		//create what will be returned with the desired length
		byte[] toBeReturned = new byte[length];
		byte nothing = 0;
		//fill the whole array with zeroes as to not forget the padding
		Arrays.fill(toBeReturned, nothing);
		
		//idea: go through byte array from least significant byte at toBeReturned[length-1] and fill each octet with the shifted int value
		int positionInArray = length -1;	//indicates the position in the array
		int shiftVar = 0;					//counts up the more x gets shifted
		
		while((shiftVar < 4) && (positionInArray >= 0)) {
			toBeReturned[positionInArray] = (byte) (x >> (8 * shiftVar));
			shiftVar ++;
			positionInArray--;
		}
		
		return toBeReturned;
	}
	
	/**
	 * A version of the TR-03111 OS2I method but it returns BigIntegers. 
	 * @param octetString - a byte array containing the most significant byte at position 0 and the least significant byte at position octetString.length-1 
	 * @return - a positive BigInteger representing the octet string
	 * @since 1.0
	 */
	public static BigInteger OS2BigInt(byte[] octetString) {
		//if array is null, return 0
		if(octetString == null) {
			return new BigInteger("0");
		}
		//if array is not null, but empty nevertheless, return 0
		if(octetString.length == 0) {
			return BigInteger.ZERO;
		}

		//if array is not empty, create a positive BigInteger from it
		//even if the msb is 1 (and the integer would be negative) the BigInteger is created as a positive number
		BigInteger x = new BigInteger(1, octetString);
		return x;
	}
	
	
	/**
	 * An interpretation of the TR-03111 FE2OS function. <br> Uses the {@linkplain #I2OS(BigInteger, int)} method, more precise:<br>
	 * Field element x e Fp is converted to octet string of length l = roundedUp(log256(p))
	 * with the function I2OS(x, l) where x is represented as an integer 
	 * @param fieldElementCoordinate - coordinate of an ECPoint
	 * @param p - the prime p that generates the field Fp
	 * @return - the octet string representation of the fieldElementCoordinate
	 * @since 1.0
	 */
	public static byte[] FE2OS(BigInteger fieldElementCoordinate, BigInteger p) {
		//length l = log256(p)
		//convert BigInt p into a double
		double pAsDouble = p.doubleValue();
		
		//calculate log256(p) = log(p) / log(256)
		double lAsDouble = (Math.log(pAsDouble) / Math.log(256));

		//round up to next integer because otherwise, length could be too small
		lAsDouble = Math.ceil(lAsDouble);
		
		int l = (int) lAsDouble;
		
		try {
			byte[] feAsOs = I2OS(fieldElementCoordinate, l);	
			return feAsOs;
		} catch (TR_03111_ECC_V2_1_Exception e) {
			e.printStackTrace();
		} 
		return null;
	}

	/**
	 * An interpretation of the TR-03111 OS2FE function. <br> Uses the {@linkplain #OS2BigInt(byte[])} method.
	 * @param fieldElementOS - octet string representing a field element coordinate
	 * @param p - the prime that generates the field Fp
	 * @return - the BigInteger representing a coordinate of a field element
	 * @since 1.0
	 */
	public static BigInteger OS2FE(byte[] fieldElementOS, BigInteger p) {
		BigInteger fieldElementCoordinate = OS2BigInt(fieldElementOS);
		fieldElementCoordinate = fieldElementCoordinate.mod(p);
		return fieldElementCoordinate;
	}
	
	
	
	/**
	 * Truncates the contents of a byte array to its x leftmost bits where x is determined by the value of leftmostBitsCount.<br>
	 * To achieve this, the method calculates how many bytes are needed and returns a cropped byte array.<br>
	 * It does pad the byte array with zeroes from the left if it is too short. However, if this method is called by
	 * {@linkplain ECDSA} or {@linkplain ECSDSA} they should have checked if it is legal to call the function.
	 * It would be illegal for them to call this function if the bit length of the output of the hash function used by a {@linkplain SignatureAlgorithm}
	 * was smaller than the bit length of the order of the EC base point.
	 * So, if an ECDSA or an ECSDSA calls this method, the case <b> originalValue.length < ((leftmostBitsCount + 7) / 8) </b> should never occur.
	 * 
	 * <br><br> <b>Note:</b> currently, the case "(originalValue bitcount < leftmostBitsCount) && ((leftmostBitsCount % 8) != 0)"
	 * is not covered.
	 * @param originalValue the byte array to be cropped or padded.
	 * @param leftmostBitsCount the number of bits that shall be returned beginning at the MSB (position zero) of the byte array.
	 * @return a byte array containing x leftmost bits of the original byte array. If leftmostBitsCount was bigger than the number of bits
	 * in the originalValue, the returned byte array is padded with zeroes from the left and then the bits from the originalValue
	 * follow.
	 * @since 1.0
	 */
	public static byte[] truncatedleftmostBits(byte[] originalValue, int leftmostBitsCount) {
		//determine how big the new array should be and create a byte array that big
		int newArraySize = (leftmostBitsCount +7) / 8;
		byte[] newValue = new byte[newArraySize];
		
		
		int rightShift = 8 - (leftmostBitsCount % 8);
		int leftShift = 8 - rightShift;
		
		//if leftmostBitsCount is a multiple of 8 (like it should be with most of the frequently used EC base points),
		//the originalValue bytes from the left are simply copied until reaching the size of the new array.
		//this is only applicable, if newArraySize is smaller or equal to the length of the originalValue
		if((rightShift == 8) && (newArraySize <= originalValue.length)) {
			System.arraycopy(originalValue, 0, newValue, 0, newArraySize);
		}
		
		//if the new value is not a multiple of 8 but the newArraySize is smaller or equal to the originalValue.length
		else if(newArraySize <= originalValue.length){
			newValue[0] = (byte) ((originalValue[0] & 0xff) >>> rightShift);
			
			for(int i = 0; i < newArraySize -1 ; i++) {
				byte upperPart = (byte) ((originalValue[i] & 0xff) << leftShift);
				byte lowerPart = (byte) ((originalValue[i+1] & 0xff) >>> rightShift);
				
				newValue[i+1] = (byte) (upperPart | lowerPart);
			}
		}
		
		//if the newArraySize is bigger than the originalValue.length, the newValue content is padded from the left with zeroes
		else {
			//if leftmostBitsCount is a multiple of 8 the padding consists of adding 0 bytes to the left
			if(rightShift == 8) {
				Arrays.fill(newValue, (byte) 0);
				System.arraycopy(originalValue, 0, newValue, (newArraySize - originalValue.length), originalValue.length);
			}
			else {
				//TODO: implement the shifting if the newValue has to be padded from the left with zeroes but 
				//the leftmostBitsCount is not a multiple of 8
			}
		}
		return newValue;
	}
	
	
}
