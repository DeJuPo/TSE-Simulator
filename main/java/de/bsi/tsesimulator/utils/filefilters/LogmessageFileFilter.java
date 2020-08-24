/**
 * 
 */
package main.java.de.bsi.tsesimulator.utils.filefilters;

import java.io.File;
import java.io.FileFilter;

/**
 * Used to filter a directory for filenames ending with the extension ".log" and beginning with a String that can be specified.
 * Used for filtering the storageDir for duplicates of LogMessages and adding the appropriate "_Fc-x" to their filename.
 * @author dpottkaemper
 * @version 1.0
 * @since 1.0
 */
public class LogmessageFileFilter implements FileFilter{
	private String fileName;
	private final String fileExtension = ".log";
	/**
	 * Default constructor.
	 */
	public LogmessageFileFilter() {
	}
	
	/**
	 * Constructor which sets the fileName that shall be used for filtering. File name String has to be present at the beginning of
	 * each file the filter is applied to if that file shall be selected by the filter. <br> Example: if the fileName is <b>foo</b> then 
	 * <b>foo-123.log</b> would be selected, but <b>ffoo.log</b> would not be.
	 * @param fileNameToBeFiltered - the new file name beginning that shall be applied by the filter.
	 * 
	 */
	public LogmessageFileFilter(String fileNameToBeFiltered) {
		this.fileName = fileNameToBeFiltered;
	}

	
	/** 
	 * Accepts files that start with the file name specified through {@linkplain LogmessageFileFilter#LogmessageFileFilter(String)} or {@linkplain LogmessageFileFilter#setFileName(String)}
	 *  and end with <i>.log</i>, the default file name extension for log messages. 
	 * @see java.io.FileFilter#accept(java.io.File)
	 * @return true, if the file begins with <i>filename</i> and ends with <i>.log</i>
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
	 * Has to be set every time <b>BEFORE</b> using this filter so that it matches only filenames that contain the
	 * specified file name and start with it.
	 * <br>For example, if the fileName is set to "testFile" then testFile123.log and testFile.log would pass the filter but not
	 * test_File.log.
	 * 
	 * @param fileName - the file name that should be filtered.
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
