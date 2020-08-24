/**
 * 
 */
package main.java.de.bsi.tsesimulator.utils;

import java.util.Arrays;

/**
 * General utility class. 
 * @author dpottkaemper
 * @version 1.0
 */
public class Utils {

	/**
	 * Should not be instantiated, has only static methods.
	 */
	private Utils() {}

	/**
	 * A variation of the {@linkplain #concatAll(Object[], Object[]...)} method to append one byte array to another without the usage of loops.
	 * If one of the arrays is null, this method returns the other array. If both arrays are null, it returns null.
	 * @param firstArray the array whose values shall comprise the first part of the result array
	 * @param secondArray the array whose values shall comprise the second part of the result array
	 * @return the concatenated arrays
	 * @version 1.5
	 * @since 1.0
	 */
	public static byte[] concatTwoByteArrays(byte[] firstArray, byte[] secondArray) {
		if((firstArray == null) && (secondArray == null)) {
			return null;
		}
		if(firstArray == null) {
			return secondArray;
		}
		if(secondArray == null) {
			return firstArray;
		}
		//default case should be, both arrays != null
		else {
			byte[] concatenatedArray = new byte[firstArray.length+secondArray.length];
			
			concatenatedArray=Arrays.copyOf(firstArray, firstArray.length+secondArray.length);
			System.arraycopy(secondArray, 0, concatenatedArray, firstArray.length, secondArray.length);
			
			return concatenatedArray;
		}
	}
	
	
	/**
	 * Concatenates any number of byte arrays (at least, until the computer running the program runs out of memory, or the resulting byte array becomes bigger than 
	 * a Java array may ever be)
	 * @param firstArray 
	 * @param rest
	 * @return the concatenation of all those arrays
	 */
	public static byte[] concatAnyNumberOfByteArrays(byte[] firstArray, byte[]...rest) {
		int lengthTotal = firstArray.length;
		for(byte[] arrayElement : rest) {
			lengthTotal+=arrayElement.length;
		}
		byte[] toBeReturned = Arrays.copyOf(firstArray, lengthTotal);
		int offset = firstArray.length;
		
		for(byte[] arrayElement : rest) {
			System.arraycopy(arrayElement, 0, toBeReturned, offset, arrayElement.length);
			offset+=arrayElement.length;
		}
		
		return toBeReturned;
	}
	
	
	
	/**
	 * concatenates any number of arrays of type T. 
	 * @param first an array or type T[]
	 * @param rest more arrays of type T[]
	 * @return the concatenation of all those arrays
	 * @version 1.0
	 */
	public static <T> T[] concatAll(T[] first, T[]... rest) {
		  int totalLength = first.length;
		  for (T[] array : rest) {
		    totalLength += array.length;
		  }
		  T[] result = Arrays.copyOf(first, totalLength);
		  int offset = first.length;
		  for (T[] array : rest) {
		    System.arraycopy(array, 0, result, offset, array.length);
		    offset += array.length;
		  }
		  return result;
		}
	
	
	public static String asciiValuesToString(int[] asciiValues) {
        StringBuilder str =new StringBuilder();
        for(int i: asciiValues){
            str.append(Character.toString((char)i));
        }
        return str.toString();
	}
	
	
}
