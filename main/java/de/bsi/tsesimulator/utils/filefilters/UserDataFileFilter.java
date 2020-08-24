/**
 * 
 */
package main.java.de.bsi.tsesimulator.utils.filefilters;

import java.io.File;
import java.io.FileFilter;

/**
 * Used for searching for files containing user data in a certain directory. Files containing user data are stored in the persistentStorageDir and 
 * have the extension ".dat"
 * @author dpottkaemper
 *
 */
public class UserDataFileFilter implements FileFilter {
	private String fileName;
	private final String fileExtension = ".dat";		
	/**
	 * Default constructor.
	 */
	public UserDataFileFilter() {
	}
	
	/**
	 * Constructor which already sets the file name to what was passed.
	 * @param fileName the String that the file names shall begin with.
	 */
	public UserDataFileFilter(String fileName) {
		this.fileName = fileName;
	}


	/**
	 * Accepts files that start with the file name specified through {@linkplain UserDataFileFilter#UserDataFileFilter(String)} or {@linkplain UserDataFileFilter#setFileName(String)}
	 *  and end with <i>.dat</i>, the file name extension for files storing user data. 
	 * @see java.io.FileFilter#accept(java.io.File)
	 * @return true, if the file begins with <i>filename</i> and ends with <i>.dat</i>
	 * @param file the file that is either accepted or rejected
	 */
	@Override
	public boolean accept(File file) {
		if(file.getName().startsWith(fileName) && file.getName().endsWith(fileExtension)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Sets the file name to what was passed in. Causes the UserDataFileFilter to match files beginning with that String.
	 * @param fileName the String that shall be at the beginning of the file name.
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
