/**
 * 
 */
package main.java.de.bsi.tsesimulator.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import main.java.de.bsi.tsesimulator.constants.Constants;
import main.java.de.bsi.tsesimulator.preferences.PropertyValues;

/**
 * Class for creating the TAR-archives that are used to export data from the TSE.
 * Uses Apache Commons compress.
 * @author dpottkaemper
 */
public class TARUtils {
	/**
	 * New method for creating a TAR-archive, now with the option to include more than one certificate file in the exported TAR-file.
	 * Uses Apache Commons Compress, the result should be in accordance with the TAR-archive structure mentioned in BSI TR-03151.  
	 * @param infoFile info.csv file a a Java File object
	 * @param logfilesToBeExported an array of the logfiles that are to be exported
	 * @param logfileFileNames an array containing the names of said logfiles. Has to be in the same order as the logfiles in the logfilesToBeExported array
	 * @param certificatefilesToBeExported an array of all certificate files used by the TSE-Simulator
	 * @param certificateFileNames an array containing the names of said certificate files. Has to be in the same order as the certificate files in the certificatefilesToBeExported array
	 * @param desiredMTime the time the archive was created, in this case, the export operation was started
	 * @throws IOException if either reading the content of a file fails and/or if writing to the TAR archive file fails.<br>
	 * Could also be thrown by {@linkplain PropertyValues#getPathToNormalStorage()} if {@linkplain PropertyValues#getInstance()} throws this exception.
	 * @since 1.4
	 * @version 1.5
	 */
	public static void createTARArchiveForExportData(File infoFile, File[] logfilesToBeExported, String[] logfileFileNames, File[] certificatefilesToBeExported, 
			String[] certificateFileNames,long desiredMTime) throws IOException {	
		//create the file that shall store the tar archive
		File tarArchiveFile = new File(PropertyValues.getInstance().getPathToNormalStorage(), Constants.FILE_NAME_EXPORT_LOGS_TAR);
		//check if that file already exists, if yes, delete
		if(tarArchiveFile.exists()) {
			tarArchiveFile.delete();
		}
		//create an ArchiveOutputStream to the tar file
		ArchiveOutputStream outToTAR = new TarArchiveOutputStream(new FileOutputStream(tarArchiveFile));
		
		//create an ArchiveEntry for the info.csv file and name it info.csv in the TAR archive (do not use the whole path name!)
		TarArchiveEntry infoEntry = new TarArchiveEntry(infoFile, Constants.FILE_NAME_INFO_CSV);
		infoEntry.setModTime((desiredMTime*1000)); 			// multiplied by 1000 because Java is expecting milliseconds (i think)
		//add the info csv file to the TarArchiveOutputStream 
		outToTAR.putArchiveEntry(infoEntry);
		//write the contents of the infoFile to the outputstream
			InputStream infoEntryIn = new FileInputStream(infoFile);
			IOUtils.copy(infoEntryIn, outToTAR);
		outToTAR.closeArchiveEntry();
		infoEntryIn.close();
		
		//write all of the logfiles to the TAR archive
			//create a counter that shall be used to match each file to its file name (aka without the whole path)
			int fileLoopVar = 0;
			//iterate through the logFilesToBeExported
			for(File logfile : logfilesToBeExported) {
				//create archive entries for each logfile to be exported in the same manner as above
				TarArchiveEntry logfileEntry = new TarArchiveEntry(logfile, logfileFileNames[fileLoopVar]);
				logfileEntry.setModTime((desiredMTime*1000));
				outToTAR.putArchiveEntry(logfileEntry);
					//copy the file contents
					InputStream logfileEntryIn = new FileInputStream(logfile);
					IOUtils.copy(logfileEntryIn, outToTAR);
				outToTAR.closeArchiveEntry();
				//increment the loop variable 
				fileLoopVar ++;
				//close the fileinputstream to release the resources
				logfileEntryIn.close();
			}
			
			
		//write all the certificate files to the TAR archive
			//reuse fileLoopVar
			fileLoopVar = 0;
			//iterate through the certificateFilesToBeExported
			for(File certificateFile : certificatefilesToBeExported) {
				//create archive entries for each certificate file that shall be exported
				TarArchiveEntry certFileEntry = new TarArchiveEntry(certificateFile, certificateFileNames[fileLoopVar]);
				certFileEntry.setModTime((desiredMTime*1000));
				outToTAR.putArchiveEntry(certFileEntry);
					//copy the file contents
					InputStream certFileEntryIn = new FileInputStream(certificateFile);
					IOUtils.copy(certFileEntryIn, outToTAR);
				outToTAR.closeArchiveEntry();
				//increment loopVar
				fileLoopVar++;
				//close fileInputStream to release resources
				certFileEntryIn.close();
			}
			
		//finish writing and close resources	
		outToTAR.finish();
		outToTAR.close();
	}
	
	
	/**
	 * Creates a TAR-archive using Apache Commons Compress containing only the certificate files. Since BSI TR-03151 chapter 4.5.2 lists only the export 
	 * of certificate chains it should be the case, that only those are to be exported by the TSE. Contrary to this, the description of exportData in chapter 
	 * 4.5.1 lists the log messages, the certificates and the information file. Therefore, a separate function for exporting certificates is needed.
	 * @param certificatefilesToBeExported a list of the certificate files that are to be exported
	 * @param certificateFileNames the corresponding file names of the aforementioned certificate files in the same order as they appear in the certificate file array
	 * @param desiredMTime the mtime that shall be set in every entry of the TAR archive
	 * @throws IOException if either reading the content of a file fails and/or if writing to the TAR archive file fails
	 * @since 1.4
	 */
	public static void createTARArchiveForExportCertificates(File[] certificatefilesToBeExported, 
			String[] certificateFileNames,long desiredMTime) throws IOException {
		//create the file that shall store the tar archive
		File tarArchiveFile = new File(PropertyValues.getInstance().getPathToNormalStorage(), Constants.FILE_NAME_EXPORT_CERTS_TAR);
		//check if that file already exists, if yes, delete
		if(tarArchiveFile.exists()) {
			tarArchiveFile.delete();
		}
		//create an ArchiveOutputStream to the tar file
		ArchiveOutputStream outToTAR = new TarArchiveOutputStream(new FileOutputStream(tarArchiveFile));
				
		//write all the certificate files to the TAR archive
		int fileLoopVar = 0;
		//iterate through the certificateFilesToBeExported
		for(File certificateFile : certificatefilesToBeExported) {
			//create archive entries for each certificate file that shall be exported
			TarArchiveEntry certFileEntry = new TarArchiveEntry(certificateFile, certificateFileNames[fileLoopVar]);
			certFileEntry.setModTime((desiredMTime*1000));
			outToTAR.putArchiveEntry(certFileEntry);
				//copy the file contents
				InputStream certFileEntryIn = new FileInputStream(certificateFile);
				IOUtils.copy(certFileEntryIn, outToTAR);
			outToTAR.closeArchiveEntry();
			//increment loopVar
			fileLoopVar++;
			//close fileInputStream to release resources
			certFileEntryIn.close();
		}
		
	//finish writing and close resources	
	outToTAR.finish();
	outToTAR.close();
	}

	
}
