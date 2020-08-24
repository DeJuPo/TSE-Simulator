/**
 * 
 */
package main.java.de.bsi.tsesimulator.tse.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.compress.utils.IOUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import main.java.de.bsi.seapi.exceptions.ErrorDeleteStoredDataFailed;
import main.java.de.bsi.seapi.exceptions.ErrorExportCertFailed;
import main.java.de.bsi.seapi.exceptions.ErrorParameterMismatch;
import main.java.de.bsi.seapi.exceptions.ErrorStorageFailure;
import main.java.de.bsi.seapi.exceptions.ErrorUnexportedStoredData;
import main.java.de.bsi.tsesimulator.constants.ConfigConstants;
import main.java.de.bsi.tsesimulator.constants.Constants;
import main.java.de.bsi.tsesimulator.exceptions.LoadingFailedException;
import main.java.de.bsi.tsesimulator.exceptions.SignatureCounterException;
import main.java.de.bsi.tsesimulator.msg.LogMessage;
import main.java.de.bsi.tsesimulator.preferences.PropertyValues;
import main.java.de.bsi.tsesimulator.tse.TSEController;
import main.java.de.bsi.tsesimulator.utils.TARUtils;
import main.java.de.bsi.tsesimulator.utils.filefilters.LogmessageFileFilter;

/**
 * This class represents the normal Storage of the TSE. In this storage, the log files are kept until they are exported.
 * The Storage class manages this export in addition to the actual storing on disc of each log. It also manages all the other export functions that require 
 * fetching and filtering of log files, certificate files or similar.
 * 
 * It does, however, <b>not</b> manage how persistent values are stored between shutdowns of the simulator.
 * @see PersistentStorage 
 * @see PersistedValues
 * 
 * @author dpottkaemper
 * @version 1.5
 */
public class Storage {
	
	private String pathToStorageDir;
	
	/**
	 * Creates a Storage object which serves as the TSE's storage during its runtime. Each Storage is created with a path that points to the actual directory
	 * on disc which stores the files. That path is built by {@linkplain PropertyValues} and fetched via {@linkplain PropertyValues#getPathToNormalStorage()}.
	 * It is therefore necessary, that {@linkplain PropertyValues#setPathToResourceDirectory(String)} has been called before constructing a Storage.
	 * @throws LoadingFailedException if constructing the path in {@linkplain PropertyValues} fails. 
	 * @version 1.5
	 */
	public Storage() throws LoadingFailedException {
		//reads the path to the storage directory from the config.properties file and stores it in pathToStorageDir
		try {
			pathToStorageDir = PropertyValues.getInstance().getPathToNormalStorage();
		} catch (IOException e) {
			throw new LoadingFailedException("Reading path to storage from config.properties failed. Original message:\n" +e.getMessage(), e);
		}
	}
	
	
//----------------------------------------------STORE DATA-----------------------------------------------------------------------------
	
	/**
	 * Stores TransactionLogMessages in the directory specified by the path in config.properties. The messages are stored in their byte array 
	 * TLV format and are named according to TR-03151. During the process of storing the created log message file, the attribute <i>last modified</i> 
	 * is set to the value of log time. This is necessary for the correct performance of the function {@linkplain TSEController#exportData(ZonedDateTime, ZonedDateTime, int, main.java.de.bsi.seapi.holdertypes.ByteArrayHolder)}.
	 * The name structure is: <br><br>
	 * 
	 * DATE-FORMAT_DATE_sig-SIGNATURE-COUNTER_LOG_No-TRANSACTION_TYPE_Client-CLIENT-ID_Fc-FILE-COUNTER.log <br>
	 * The values in parenthesis are parameters. The values with || between them are a "must be exactly one of the mentioned values". <br>
	 * (Unixt||Utc||Gent)_(logtime)_Sig-(signatureCounter)_Log-Tra_No-(transactionNumber)_(Start||Update||Finish)_Client-(clientId).log <br> <br>
	 * 
	 * If a file with exactly the same name as the LogMessage that shall be saved exists, the new log message is saved under the same name but with 
	 * the addition of "_Fc-y" in front of the .log extension. The y is an integer and it counts how many files with the same name already exist. <br>
	 * This means, that for example a duplicate of the file <b>Unixt_1554988806_Sig-1_Log-Tra_No-1_Start_Client-CRE-AM-X2019J00123456.log</b> would be 
	 * saved as <b>Unixt_1554988806_Sig-1_Log-Tra_No-1_Start_Client-CRE-AM-X2019J00123456_Fc-1.log</b>.
	 * @param transactionLogCompleteTLVByteArray - the whole TransactionLogMessage in its TLV byte array form with the SEQUENCE wrapper.
	 * @param logtime - the logTime value of the log message in UnixTime format. Currently, ONLY UnixTime is supported. Is used to set the attribute <i>last modified</i> as well.
	 * @param signatureCounter - the signatureCounter value of the TransactionLogMessage.
	 * @param transactionNumber - the transactionNumber value of the log message.
	 * @param operationType - the operationType of the log message. If it is not "StartTransaction", "UpdateTransaction" or "FinishTransaction" an Error 
	 * is thrown because it is an unknown operation type.
	 * @param clientId - the clientID of the TranasctionLogMessage.
	 * @throws ErrorStorageFailure <blockquote>- if the TransactionLogMessage had an unknown operationType associated with it and it could therefore not be
	 * stored under a legal file name. <br>
	 * - if writing to the file that shall store the log message fails because of IOExceptions or other Exceptions.
	 * @version 1.5
	 */
	public void storeTransactionLog(byte[] transactionLogCompleteTLVByteArray, long logtime, long signatureCounter, long transactionNumber,
			String operationType, String clientId) throws ErrorStorageFailure {
		//create the file name for the storage of the logmessage 
		StringBuilder fileNameBuilder = null;
		//surround with try-catch because of PropertyValues maybe not knowing where to find config.properties
		try {
			fileNameBuilder = new StringBuilder(PropertyValues.getInstance().getValue(ConfigConstants.CFG_TAG_TIME_FORMAT));
		} catch (IOException e1) {
			throw new ErrorStorageFailure("Read time format from configuration file failed. Original message:\n"+e1.getMessage(), e1);
		}
		fileNameBuilder.append('_').append(logtime).append("_Sig-");
		fileNameBuilder.append(signatureCounter).append("_Log-Tra_No-").append(transactionNumber);
		
		//check which operationType is present and modify the String accordingly
		switch(operationType.toLowerCase()) {
			case "starttransaction":
				fileNameBuilder.append("_Start_Client-");
				break;
			
			case "updatetransaction":
				fileNameBuilder.append("_Update_Client-");
				break;
				
			case "finishtransaction":
				fileNameBuilder.append("_Finish_Client-");
				break;
			default:
				throw new ErrorStorageFailure("No known operation type!");	//this could be changed to just appending "_UNKNOWN-OPERATION-TYPE_Client-"
																			//instead of throwing an Exception. The TR does not specify what to do.
		}
		
		//append the clientId and the ".log"
		fileNameBuilder.append(clientId).append(".log");
		
		//create a new file to store the logmessage in
		File logmessageFile = new File(pathToStorageDir, fileNameBuilder.toString());
		//if there's no parent file in the pathToStorageDir present, create one:
		if(!logmessageFile.getParentFile().exists()) {
			logmessageFile.getParentFile().mkdirs();
		} 
		//if there's already a file with the same name the number of files with that same name has to be counted.
		else if(logmessageFile.exists()) {
			//NEVER EVER DELETE logmessageFile if it existed before. this just deletes the old log message. we do not want that!
			
			
			//create a substring to scan for equal file names without the possible "_Fc-FILE-COUNTER" part
				//first get only the new file name without the ".log" part. Necessary, because one wants to scan for files containing the 
				//same (Unixt||Utc||Gent)_(logtime)_Sig-(signatureCounter)_Log-Tra_No-(transactionNumber)_(Start||Update||Finish)_Client-(clientId)
				//part, but maybe have different "_Fc-FILE-COUNTER.log" endings
			String[] splitFileName = fileNameBuilder.toString().split(".log");
			
			//create a new LogmessageFileFilter and feed the beginning of the duplicate name into it
			LogmessageFileFilter fileNameFilter = new LogmessageFileFilter();
			fileNameFilter.setFileName(splitFileName[0]);
			//this should filter all logmessages with almost the same name as the new logmessage from the storageDir
			File[] filesWithAlmostSameName = logmessageFile.getParentFile().listFiles(fileNameFilter);
			//get the number of files that are named similarly
			int fileCounter = filesWithAlmostSameName.length;
			
			//create a new file name for the logmessage file (add an _Fc-x) and then create a new File to store the contents in 
			String filenameNew = splitFileName[0] +"_Fc-" +(fileCounter) +".log";
			logmessageFile = new File(pathToStorageDir, filenameNew);
		}
		
		//create a FileOutputStream to write the logmessage byte array to the file
		try {
			FileOutputStream outputStream = new FileOutputStream(logmessageFile);
			//attempt to write the TLV byte array to the file
			outputStream.write(transactionLogCompleteTLVByteArray);
			outputStream.close();
		} catch (FileNotFoundException e) {
			throw new ErrorStorageFailure("FileNotFoundException caught.\n" +e.getMessage() +"\n");
		} catch (IOException e) {
			throw new ErrorStorageFailure("IOException caught.\n" +e.getMessage() +"\n");
		} catch(Exception e) {
			throw new ErrorStorageFailure();
		}
		//last, set the "lastModifiedTime" to the value of logTime
		//multiply logtime with 1000 because 1 second = 1000 milliseconds? "setLasModified" expects milliseconds since unix epoch
		logmessageFile.setLastModified((logtime*1000));
	}
	
	/**
	 * Stores SystemLogMessages in the directory specified by the path in config.properties. The messages are stored in their byte array 
	 * TLV format and are named according to TR-03151.  During the process of storing the created log message file, the attribute <i>last modified</i> 
	 * is set to the value of log time. This is necessary for the correct performance of the function {@linkplain TSEController#exportData(ZonedDateTime, ZonedDateTime, int, main.java.de.bsi.seapi.holdertypes.ByteArrayHolder)}.
	 * The name structure is: <br><br>
	 * 
	 * DATE-FORMAT_DATE_sig-SIGNATURE-COUNTER_LOG_TYPE_Fc-FILE-COUNTER.log <br>
	 * The values in paranthesis are parameters. The values with || between them are a "must be exactly one of the specified values". <br>
	 * (Unixt||Utc||Gent)_(logtime)_Sig-(signatureCounter)_Log-Sys_(Initialize||UpdateTime||DisableSecureElement||AuthenticateUser||LogOut||UnblockUser).log<br><br>
	 *
	 * If a file with exactly the same name as the LogMessage that shall be saved exists, the new logmessage is saved under the same name but with 
	 * the addition of "_Fc-y" in front of the .log extension. The y is an integer and it counts how many files with the same name already exist. <br>
	 * This means, that for example a duplicate of the file <b>Unixt_1554990849_Sig-2_Log-Sys_Initialize.log</b> would be saved as 
	 * <b>Unixt_1554990849_Sig-2_Log-Sys_Initialize_Fc-1.log</b>.
	 * @param systemLogCompleteTLVByteArray - the whole SystemLogMessage in its TLV byte array form with the SEQUENCE wrapper.
	 * @param logTime - the logTime value of the logmessage in UnixTime format. Currently, ONLY UnixTime is supported. Is used to set the attribute <i>last modified</i> as well.
	 * @param signatureCounter - the signatureCounter value of the SystemLogMessage.
	 * @param operationType - the operationType of the logmessage. Should be exactly in the format that TR-03151 wants it to be.
	 * @throws ErrorStorageFailure if writing to the file that shall store the logmessage fails because of IOExceptions or other Exceptions.
	 */
	public void storeSystemLog(byte[] systemLogCompleteTLVByteArray, long logTime, long signatureCounter, String operationType) throws ErrorStorageFailure {
		//create the file name for the storage of the logmessage 
		StringBuilder fileNameBuilder = null;
		//surround with try-catch because of PropertyValues maybe not knowing where to find config.properties
		try {
			fileNameBuilder = new StringBuilder(PropertyValues.getInstance().getValue(ConfigConstants.CFG_TAG_TIME_FORMAT));
		} catch (IOException e1) {
			throw new ErrorStorageFailure("Read time format from configuration file failed. Original message:\n"+e1.getMessage(), e1);
		}	
		fileNameBuilder.append('_').append(logTime).append("_Sig-");
		fileNameBuilder.append(signatureCounter).append("_Log-Sys_").append(operationType).append(".log");
		
		//create a new file to store the logmessage in
		File logmessageFile = new File(pathToStorageDir, fileNameBuilder.toString());
		//if there's no parent file in the pathToStorageDir present, create one:
		if(!logmessageFile.getParentFile().exists()) {
			logmessageFile.getParentFile().mkdirs();
		} 
		//if there's already a file with the same name the number of files with that same name has to be counted.
		else if(logmessageFile.exists()) {
			//NEVER EVER DELETE logmessageFile if it existed before. this just deletes the old logmessage. we do not want that!
			
			//create a substring to scan for equal file names without the possible "_Fc-FILE-COUNTER" part
				//first get only the new file name without the ".log" part. Necessary, because one wants to scan for files containing the 
				//same (Unixt||Utc||Gent)_(logtime)_Sig-(signatureCounter)_Log-Tra_No-(transactionNumber)_(Start||Update||Finish)_Client-(clientId)
				//part, but maybe have different "_Fc-FILE-COUNTER.log" endings
			String[] splitFileName = fileNameBuilder.toString().split(".log");
			
			//create a new LogmessageFileFilter and feed the beginning of the duplicate name into it
			LogmessageFileFilter fileNameFilter = new LogmessageFileFilter();
			fileNameFilter.setFileName(splitFileName[0]);
			//this should filter all logmessages with almost the same name as the new logmessage from the storageDir
			File[] filesWithAlmostSameName = logmessageFile.getParentFile().listFiles(fileNameFilter);
			//get the number of files that are named similarly
			int fileCounter = filesWithAlmostSameName.length;
			
			//create a new file name for the logmessage file (add an _Fc-x) and then create a new File to Store the contents in 
			String filenameNew = splitFileName[0] +"_Fc-" +(fileCounter) +".log";
			logmessageFile = new File(pathToStorageDir, filenameNew);
		}
		
		//create a FileOutputStream to write the logmessage byte array to the file
		try {
			FileOutputStream outputStream = new FileOutputStream(logmessageFile);
			//attempt to write the TLV byte array to the file
			outputStream.write(systemLogCompleteTLVByteArray);
			outputStream.close();
		} catch (FileNotFoundException e) {
			throw new ErrorStorageFailure("FileNotFoundException caught.\n" +e.getMessage() +"\n");
		} catch (IOException e) {
			throw new ErrorStorageFailure("IOException caught.\n" +e.getMessage() +"\n");
		} catch(Exception e) {
			throw new ErrorStorageFailure();
		}
		
		//last, set the "lastModifiedTime" to the value of logTime
		//multiply logtime with 1000 because 1 second = 1000 milliseconds? "setLasModified" expects milliseconds since unix epoch
		logmessageFile.setLastModified((logTime*1000));
	}
	
	/**
	 * @deprecated
	 * Please note that this function does not do anything at this moment. If, and only if, in the future one wants the simulator to produce AuditLogs, which it is incapable of producing 
	 * as of version 1.4, this would be the method to store those logs.
	 */
	public void storeAuditLog() {
		
	}
	
//------------------------------------------------EXPORT DATA------------------------------------------------------------------
	/**
	 * This method is an updated version of its predecessors with the same name. It is used to export any combination of log messages from the TSE storage.<br>
	 * Depending on the type of exportData function call in the TSEController, the controller may provide an array of all log files present (via {@linkplain #listLogFiles()} or 
	 * a more restricted list.
	 * 
	 * For example, in the case of a start/and or an endDate provided, the caller of this method has to ensure that the files given to this method are only the ones
	 * that shall be exported. Preferably, one of the {@linkplain #listFilesStartDateEndDate(ZonedDateTime, ZonedDateTime)} or similar has
	 * been used in advance.
	 * <br>Be aware that there can only be one TAR-Archive that is exported at a time and that the method can not clearly distinguish the an old
	 * exportedData.txt from a new one. This should not be an issue, if the byte array returned is converted back into a TAR archive and stored somewhere different 
	 * from the storage defined by the config.properties file.<br>
	 * 
	 * @param seapiDescription the description of the SE API. Has to be provided by the {@linkplain TSEController} and is used to create the info.csv file.
	 * @param manufacturerInfo information about the manufacturer. Has to be provided by the {@linkplain TSEController} and is used to create the info.csv file.
	 * @param versionInfo information about the current version of the TSE. Has to be provided by the {@linkplain TSEController} and is used to create the info.csv file.
	 * @param unixTimeAtCallingOfStoreMethod used to set the <i>mtime</i> in each file header of the TAR archive. Because of limited access to the {@linkplain CryptoCore}'s clock,
	 * this has to be provided by the TSEController at the time of the exportData call
	 * @param filesThatShallBeExported an array of the files that shall be exported
	 * @return the byte array representation of the TAR archive containing an info.csv file, the certificate of the public key used for signature
	 * creation and the log files that are exported.
	 * @since 1.4 
	 * @version 1.5
	 */
	public byte[] exportData(String seapiDescription, String manufacturerInfo, String versionInfo, long unixTimeAtCallingOfStoreMethod, 
			File[] filesThatShallBeExported) {
		//create a Java File object of the exportedData.txt file to later work with that reference
		File exportDataList = new File(pathToStorageDir, Constants.FILE_NAME_EXPORT_DATA_TXT);
	
		//create a list of all filenames
		String[] fileNameList = new String[filesThatShallBeExported.length];
		int loopVar = 0;
		for(File f : filesThatShallBeExported) {
			fileNameList[loopVar] = f.getName();
			loopVar++;
		}
				
		//A. TAR archive SHALL include a file named "info.csv" with the content "description:", $1, "manufacturer:", $2, "version:", $3
		//create the text for the info.csv file
		File infoCSV = createInfoCSV(seapiDescription, manufacturerInfo, versionInfo);
		
		//B. TAR archive should include all the (filtered) log messages: already filtered beforehand by TSEController. 
		//' filesThatShallBeExported ' contains only those that are to be exported
		
		//C. TAR archive should export the certificate(s) used to verify the signatures. 
		// for the sake of the simulator, export all certificates found in config.properties "keyDir"
		File[] certificateFiles = null;
		try {
			certificateFiles = listCertificateFiles();
		} catch (IOException e2) {
			//this IOException is thrown by PropertyValues.getInstance, used for PropertyValues.getInstance().getPathToKeyDir.
			//the simulator can not throw an Exception like "ErrorExportFailed" because there is no such generic exception for that function defined in the SE API
			//ugly solution: return an array filled with an error string. 
			e2.printStackTrace();
			String errorString = "IOException occurred. Most likely caused by missing path to resource directory in PropertyValues class. "
					+ "Try calling PropertyValues.setPathToResourceDirectory(pathToResourceDir). Could not export data!";		
			return errorString.getBytes();
		}
		//create a list of all certificate filenames
		String[] certFileNameList = new String[certificateFiles.length];
		int loopVar2 = 0;
		for(File cert : certificateFiles) {
			certFileNameList[loopVar2] = cert.getName();
			loopVar2++;
		}
		
		//create the TAR archive that will be returned with all necessary files
		try {
			TARUtils.createTARArchiveForExportData(infoCSV, filesThatShallBeExported, fileNameList, certificateFiles, certFileNameList, unixTimeAtCallingOfStoreMethod);
		} catch (IOException e) {
			//according to TR-03151 no IOException can occur when exporting data (or at least there is no Exception defined for that.
				//workaround: if IOException occurs, return an error byte array
			e.printStackTrace();
			String errorString = "IOError occurred. Could not export data.";			
			return errorString.getBytes();
		}
		//if the creation of the tar archive was successful:
		//before returning the TAR archive, put all the exported data into the exportedData.txt
		
		//create a set of all filenames that are to be exported
		HashSet<String> fileNameSet = new HashSet<String>(Arrays.asList(fileNameList));
		
		//if exportedData.txt already exists, read its contents into the HashSet<String>, because sets only add something if it does not already exist in that set
		//this results in a set of all files that have already been exported
		if(exportDataList.exists()) {
			try(Stream<String> stream = Files.lines(exportDataList.toPath())){
				stream.forEach(fileNameSet::add);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//now write all file names (back) into the text file!
		try {
			FileWriter exportWriter = new FileWriter(exportDataList);
			Iterator<String> iter = fileNameSet.iterator();
			while(iter.hasNext()) {
				exportWriter.write(iter.next() +"\n");		//check in test if +"\n" is necessary. it is
			}
			exportWriter.close();
		} catch (IOException e1) {
			//Ignore possible IO exception, this is out of scope of a simulator
			e1.printStackTrace();
		}

		
		//now read the whole TAR archive from the file into a byte array.
			//maximum length of an array in Java: Integer.Max_VALUE-1 = 2.147.483.646
			//because TAR archive is exported in a byte array, that means, the exported TAR archive can not exceed 2.147.483.646 byte
			//or ~ 2.14748 GB
			
			//first, create a Java File object of the tar archive:
			File tarArchiveFile = new File(pathToStorageDir, Constants.FILE_NAME_EXPORT_LOGS_TAR);
			//create the byte array that will be returned:
			byte[] tarArchiveAsByteArray = null;
			try {
				FileInputStream inFromTar = new FileInputStream(tarArchiveFile);
				tarArchiveAsByteArray = IOUtils.toByteArray(inFromTar);
				inFromTar.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		return tarArchiveAsByteArray;
		
	}

	
	/**
	 * Exports the certificate files stored in the TSE. This method only works as intended, if the certificates are stored and named as is described in the TR.
	 * It creates a TAR archive using {@linkplain TARUtils#createTARArchiveForExportCertificates(File[], String[], long)} and returns that as a byte array.
	 * A time has to be provided to this method to set the <i>mtime</i> attribute of the TAR archive in accordance with TR-03151.
	 * @param unixTimeAtCallingOfStoreMethod the time this method was called. 
	 * @return a TAR archive containing all certificates stored in the TSE.
	 * @throws ErrorExportCertFailed if no certificates are found in the designated storage directory (the keyDir path of config.properties) or if {@linkplain #listCertificateFiles()} throws it
	 */
	public byte[] exportCertificateData(long unixTimeAtCallingOfStoreMethod) throws ErrorExportCertFailed {
		//Function shall collect the certificate chains.
		// for the sake of the simulator, export all certificates found in config.properties "keyDir"
		File[] certificateFiles = null;
		//surround with try-catch, because ultimately PropertyValue.getInstance() might throw an IOException
		try {
			certificateFiles = listCertificateFiles();
		} catch (IOException e1) {
			throw new ErrorExportCertFailed(e1.getMessage(), e1);
		}
		
		//check if the file list is empty or non-existent
		if(certificateFiles==null) {
			throw new ErrorExportCertFailed("No certificates found in keyDir!\n");
		}
		if(certificateFiles.length==0) {
			throw new ErrorExportCertFailed("No certificates found in keyDir!\n");
		}
		
		//create a list of all certificate filenames
		String[] certFileNameList = new String[certificateFiles.length];
		int loopVar = 0;
		for(File cert : certificateFiles) {
			certFileNameList[loopVar] = cert.getName();
			loopVar++;
		}
		
		//create the TAR archive that will be returned with all necessary files
		try {
			TARUtils.createTARArchiveForExportCertificates(certificateFiles, certFileNameList, unixTimeAtCallingOfStoreMethod);
		} catch (IOException e) {
			//if something happens during export, throw ErrorExportCertFailed as well, because the TAR-archive creation has malfuntioned
			throw new ErrorExportCertFailed("Creating TAR-archive failed!", e);
		}
		
		//now read the whole TAR archive from the file into a byte array.
		//maximum length of an array in Java: Integer.Max_VALUE-1 = 2.147.483.646
		//because TAR archive is exported in a byte array, that means, the exported TAR archive can not exceed 2.147.483.646 byte
		//or ~ 2.14748 GB
		
		//first, create a Java File object of the tar archive:
		File tarArchiveFile = new File(pathToStorageDir, Constants.FILE_NAME_EXPORT_CERTS_TAR);
		//create the byte array that will be returned:
		byte[] tarArchiveAsByteArray = null;
		try {
			FileInputStream inFromTar = new FileInputStream(tarArchiveFile);
			tarArchiveAsByteArray = IOUtils.toByteArray(inFromTar);
			inFromTar.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		return tarArchiveAsByteArray;
	}
	
	
//------------------------------------------------DELETE DATA------------------------------------------------------------------
	/**
	 * Provides the means to delete stored messages from the storageDir.
	 * It first checks the storageDIr for the presence of a text file named "exportedData.txt".
	 * This file stores the names of all the log files each time they are exported. If such a file is not present, it can be concluded, that
	 * there was no attempt in the past to export data.
	 * <br>If exportedData.txt exists, the content is then read and compared to the log messages present in the storage directory.
	 * It is then checked, that all log files present in the directory can be found in exportedData.txt and have therefore been exported.
	 * 
	 * The method is synchronized so no two processes try to delete data at the same time.<br>
	 * 
	 * Note: if an error arises during the deletion process, there is no measure in place to ensure the storage is left in a consistent state.
	 * @throws ErrorUnexportedStoredData if data is present in the storageDir but not on the list of exported files
	 * @throws ErrorDeleteStoredDataFailed if something happens during the deletion process that messes the deletion process up, this is thrown
	 */
	public synchronized void deleteStoredData() throws ErrorUnexportedStoredData, ErrorDeleteStoredDataFailed {
		//create a File instance of the list 
		File exportedDataList = new File(pathToStorageDir, Constants.FILE_NAME_EXPORT_DATA_TXT);
		//if expotredData.txt does not exist, no data has been exported in the past (or the user has deleted it from the filesystem, but
		//that will not be taken into account here)
		if(!exportedDataList.exists()) {
			throw new ErrorUnexportedStoredData("Please export all data before deleting any!");
		}
		
		List<String> fileNamesList = null;
		//create a List of type string to hold the file names, then read every line into that list
		try {
			fileNamesList = Files.readAllLines(exportedDataList.toPath());
		} catch (IOException e) {
			throw new ErrorDeleteStoredDataFailed("Failed to read exportedData.txt!\n", e);
		}
			
		//create a File instance for the storageDir to get a list of all present files
		File storageDirectory = new File(pathToStorageDir);
		//create a FileFilter which returns only the real log message file (all files in the storageDirectory ending with .log)
		LogmessageFileFilter onlyLogmessagesFilter = new LogmessageFileFilter("");
		//use that filter to get all the log messages in a list
		File[] fileArray = storageDirectory.listFiles(onlyLogmessagesFilter);
 		
		
		//go through the fileArray and look if each file name is contained in the fileNamesList
		for(File f : fileArray) {
			//if one name is not on the exportedData.txt, throw an Exception
			if(!fileNamesList.contains(f.getName())) {
				throw new ErrorUnexportedStoredData();
			}
		}
		
		//if no exception was thrown, all data can be deleted!
		try {
			FileUtils.cleanDirectory(storageDirectory);
		} catch (IOException e) {
			throw new ErrorDeleteStoredDataFailed("Deleting content of the storage directory was unsuccessful!", e);
		}
	}
	
	
	
//------------------------------------------------UTILITY------------------------------------------------------------------
	
//------------------------------------------------FILE LIST MAKERS------------------------------------------------------
	/**
	 * Method for filtering log files created between two points in time. The length of the array returned can also be used to be compared
	 * to maxNumberRecords in {@linkplain TSEController #exportData(ZonedDateTime, ZonedDateTime, int, main.java.de.bsi.seapi.holdertypes.ByteArrayHolder)}
	 * and {@linkplain TSEController#exportData(ZonedDateTime, ZonedDateTime, String, int, main.java.de.bsi.seapi.holdertypes.ByteArrayHolder)}.
	 * The caller has to make sure, that at least one of the two parameters is present and that both are valid. That means, both have to be in the
	 * range of {@linkplain Constants#EARLIEST_LEGAL_TIME} - {@linkplain Constants#LATEST_LEGAL_TIME}.<br>
	 * 
	 * The caller has to be aware of the fact, that in case of a quick running simulator, calling this method <b>directly after</b> creating a {@linkplain LogMessage} may cause issues.
	 * 
	 * @param startDate the date after which logs shall be filtered. Is optional, if endDate has been provided.
	 * @param endDate the date before which logs shall be filtered. Is optional, if startDate is present.
	 * @return a list containing all the files that match the desired time frame and end with .log
	 */
	public File[] listFilesStartDateEndDate(ZonedDateTime startDate, ZonedDateTime endDate) {
		//check which one of the two is present.
		boolean startDateNull = (startDate == null);
		boolean endDateNull = (endDate == null);
		
		//create a SuffixFileFilter filtering .log files
		SuffixFileFilter dotLog = new SuffixFileFilter(".log");
		
		//create a Collection<File> that stores the filtered files
		Collection<File> fileCollection = null;
		
		//if both startDate AND endDate are present, the time between them (inclusive endDate) has to be considered
			if((!startDateNull) && (!endDateNull)) {
				//1. make a file filter which accepts files created AFTER the startDate
				AgeFileFilter youngerThanStartDate = new AgeFileFilter((startDate.toInstant().toEpochMilli() - 50), false);	//has to be EpochMilli, otherwise this is not working as it should!
																															//'-50' must be present to assert that [startDate,endDate] is an inclusive interval
				//2. make a file filter which accepts files created BEFORE the endDate (inclusive)
				AgeFileFilter olderOrEqualToEndDate = new AgeFileFilter(endDate.toInstant().toEpochMilli(), true);
				//3. create a List<IOFileFilter>
				List<IOFileFilter> filterList = new ArrayList<IOFileFilter>();
				filterList.add(dotLog);
				filterList.add(youngerThanStartDate);
				filterList.add(olderOrEqualToEndDate);
				//4. create an AndFileFilter from the list
				AndFileFilter combinationFilter = new AndFileFilter(filterList);
				//5. fill the fileCollection
				fileCollection = FileUtils.listFiles(new File(pathToStorageDir), combinationFilter, null);
				
				//return the Collection as an array
				return fileCollection.toArray(new File[0]);
			}
		//if only startDate is present, log files after this date shall be filtered
			if((!startDateNull) && endDateNull) {
				//1. make a file filter which accepts files created AFTER the startDate
				AgeFileFilter youngerThanStartDate = new AgeFileFilter((startDate.toInstant().toEpochMilli() - 50), false);	//has to be EpochMilli, otherwise this is not working as it should!
																															//'-50' must be present to assert that [startDate,endDate] is an inclusive interval
				//2. create an AndFileFilter combining dotLog and youngerThanStartdate
				AndFileFilter combinationFilter = new AndFileFilter(dotLog, youngerThanStartDate);
				//3. fill the fileCollection
				fileCollection = FileUtils.listFiles(new File(pathToStorageDir), combinationFilter, null);
				
				//return the Collection as an array
				return fileCollection.toArray(new File[0]);
			}
		//if only endDate is present, log files before this date inclusive shall be filtered
			if(startDateNull && (!endDateNull)) {
				//1. make a file filter which accepts files created BEFORE the endDate (inclusive)
				AgeFileFilter olderOrEqualToEndDate = new AgeFileFilter(endDate.toInstant().toEpochMilli(), true);
				//2. create an AndFileFilter combining dotLog and olderOrEqualToEndDate
				AndFileFilter combinationFilter = new AndFileFilter(dotLog, olderOrEqualToEndDate);
				//3. fill the fileCollection
				fileCollection = FileUtils.listFiles(new File(pathToStorageDir), combinationFilter, null);
				
				//return the Collection as an array
				return fileCollection.toArray(new File[0]);
			}
			
		//if everything went wrong, return null
		return null;
	}
	
	/**
	 * Searches the storage for log files whose signature counter lies between startSigCntr and endSigCntr (inclusive).
	 * If no LogFiles with signature counters in the specified interval are found, the return value is null.
	 * 
	 * Reminder: File names are constructed as follows:<br>
	 * 	-TransactionLogs: (Unixt||Utc||Gent)_(logtime)_Sig-(signatureCounter)_Log-Tra_No-(transactionNumber)_(Start||Update||Finish)_Client-(clientId).log <br><br>
	 *  -SystemLogs: 	  (Unixt||Utc||Gent)_(logtime)_Sig-(signatureCounter)_Log-Sys_(Initialize||UpdateTime||DisableSecureElement||AuthenticateUser||LogOut||UnblockUser).log<br><br>
	 *  -AuditLogs:		  (Unixt||Utc||Gent)_(logtime)_Sig-(signatureCounter)_Log-Aud.log<br><br>
	 *  
	 *  Optionally, there could be an "_Fc-FILECOUNTER" before the ".log", this should only happen in the case of {@linkplain SystemLogMessageg} or AuditLogMessages. 
	 * 
	 * @param startSigCntr - lower bound of the interval, > 0.
	 * @param endSigCntr - upper bound of the interval, > 0.
	 * @return an array containing all the files whose signature counter lies in the provided interval. The array is empty, if no 
	 * log messages with the specified signature counters could be found.
	 * @throws SignatureCounterException - one or both signature counters provided are not truly positive.
	 */
	public File[] listFilesSignatureCounter(long startSigCntr, long endSigCntr) throws SignatureCounterException {
		//check if both counter are non-negative. Signature counters MUST be unsigned integers according to BSI TR-03153.
		if(startSigCntr <= 0 || endSigCntr <= 0) {
			throw new SignatureCounterException("One or both signature counters provided were <= 0.");
		}
		if(startSigCntr > endSigCntr) {
			throw new SignatureCounterException(startSigCntr +" > " +endSigCntr +" should be the other way around.");
		}
		
		//there should be a filter for the case of startSigCntr == endSigCntr. This has no use case in the actual TSE Sim, but could be handy in development.
		if(startSigCntr==endSigCntr) {		
			//create a simple filter for filtering the files with the specified signature counter. Note: this could be ANY logfile! not just transaction logs!
			String regEx = "^(Gent|Unixt|Utc){1}_{1}((\\d{14}(\\.\\d\\d\\d){0,1})|(\\d{10}(\\d\\d){0,1}Z{1})|(\\d{1,19})){1}_Sig-" +startSigCntr+"{1}_Log-(Sys_(Initialize|UpdateTime|DisableSecureElement|AuthenticateUser|LogOut|UnblockUser){1}|Aud|Tra_No-(\\d{1,19})_(Start|Update|Finish){1}_Client-(\\S)+){1}(_Fc-(\\d)+){0,1}\\.log$";
			//if audit logs are implemented but not found, try the regex below:
			//String regEx = "^(Gent|Unixt|Utc){1}_{1}((\\d{14}(\\.\\d\\d\\d){0,1})|(\\d{10}(\\d\\d){0,1}Z{1})|(\\d{1,19})){1}_Sig-" +startSigCntr+"{1}_Log-(Sys_(Initialize|UpdateTime|DisableSecureElement|AuthenticateUser|LogOut|UnblockUser){1}|(Aud){1}|Tra_No-(\\d{1,19})_(Start|Update|Finish){1}_Client-(\\S)+){1}(_Fc-(\\d)+){0,1}\\.log$";

			//create a Collection, because a good filter can be used:
			RegexFileFilter logsWithSignatureCounter = new RegexFileFilter(regEx);
					
			//create a collection containing the filtered files
			Collection<File> fileCollection = null;
			
			fileCollection = FileUtils.listFiles(new File(pathToStorageDir), logsWithSignatureCounter, null);
			//check if any Files were found with the specified signatureCounter, return null if there are no logs with the specified counter.
			if(fileCollection.isEmpty()) {
				return null;
			}
			else {
				return fileCollection.toArray(new File[0]);
			}
		}
		
		//this problem will be solved using a similar approach to the filter that searches for log messages in an interval of transaction numbers.
		else {
			//create a Collection which will store all the log files:
			Collection<File> intervalCollection = null;
			
			//create a RegExFileFilter for the first log message matching this signature counter
			String regExStart = "^(Gent|Unixt|Utc){1}_{1}((\\d{14}(\\.\\d\\d\\d){0,1})|(\\d{10}(\\d\\d){0,1}Z{1})|(\\d{1,19})){1}_Sig-" +startSigCntr+"{1}_Log-(Sys_(Initialize|UpdateTime|DisableSecureElement|AuthenticateUser|LogOut|UnblockUser){1}|Aud|Tra_No-(\\d{1,19})_(Start|Update|Finish){1}_Client-(\\S)+){1}(_Fc-(\\d)+){0,1}\\.log$";
			//if audit logs are implemented but not found, try the regex below:
			//String regEx = "^(Gent|Unixt|Utc){1}_{1}((\\d{14}(\\.\\d\\d\\d){0,1})|(\\d{10}(\\d\\d){0,1}Z{1})|(\\d{1,19})){1}_Sig-" +startSigCntr+"{1}_Log-(Sys_(Initialize|UpdateTime|DisableSecureElement|AuthenticateUser|LogOut|UnblockUser){1}|(Aud){1}|Tra_No-(\\d{1,19})_(Start|Update|Finish){1}_Client-(\\S)+){1}(_Fc-(\\d)+){0,1}\\.log$";

			RegexFileFilter logWithStartCounter = new RegexFileFilter(regExStart);
			//use the regex to fill the collection, preventing NullPointerExceptions in the loop
			intervalCollection = FileUtils.listFiles(new File(pathToStorageDir), logWithStartCounter, null);
			
			//go through the signature counters contained in the interval, starting at the second one (because the first has already been filtered!)
			for(long currentSigCntr=(startSigCntr+1); currentSigCntr<=endSigCntr; currentSigCntr++) {
				//create a temporary collection which stores the files filtered temporarily
				Collection<File> filesForCurrentCounter = null;
				String regExTemp = "^(Gent|Unixt|Utc){1}_{1}((\\d{14}(\\.\\d\\d\\d){0,1})|(\\d{10}(\\d\\d){0,1}Z{1})|(\\d{1,19})){1}_Sig-" +currentSigCntr+"{1}_Log-(Sys_(Initialize|UpdateTime|DisableSecureElement|AuthenticateUser|LogOut|UnblockUser){1}|Aud|Tra_No-(\\d{1,19})_(Start|Update|Finish){1}_Client-(\\S)+){1}(_Fc-(\\d)+){0,1}\\.log$";
				//if audit logs are implemented but not found, try the regex below:
				//String regEx = "^(Gent|Unixt|Utc){1}_{1}((\\d{14}(\\.\\d\\d\\d){0,1})|(\\d{10}(\\d\\d){0,1}Z{1})|(\\d{1,19})){1}_Sig-" +currentSigCntr+"{1}_Log-(Sys_(Initialize|UpdateTime|DisableSecureElement|AuthenticateUser|LogOut|UnblockUser){1}|(Aud){1}|Tra_No-(\\d{1,19})_(Start|Update|Finish){1}_Client-(\\S)+){1}(_Fc-(\\d)+){0,1}\\.log$";

				RegexFileFilter currentCounterFilter = new RegexFileFilter(regExTemp);
				
				//use the filter to add the log messages to the collection
				filesForCurrentCounter = FileUtils.listFiles(new File(pathToStorageDir), currentCounterFilter, null);
				//check if the collection is empty. If its not, add the content to the intervalCollection
				if(!filesForCurrentCounter.isEmpty()) {
					intervalCollection.addAll(filesForCurrentCounter);
				}
			}
			//if the intervalCollection is empty, return null
			if(intervalCollection.isEmpty()) {
				return null;
			}
			else {
				return intervalCollection.toArray(new File[0]);
			}
		}
	}
	
	/**
	 * This method is used by the {@linkplain TSEController} to filter stored transaction log files according to (a) certain transaction number(s).
	 * The {@linkplain Storage} is searched for transaction log files with file names, which match the given transaction number(s).
	 * The method employs checks to determine if the values of startNumber and endNumber are in the expected range. If they are, the following actions depend on whether the filtering 
	 * shall occur taking only one transactionNumber or two into account. 
	 * 
	 * The case using only one transaction number tends to be much quicker, since the filtering can be done without the need for a loop.
	 * The method uses regular expressions to search for TransactionLogMessages with the specified transaction number. It does expect the file names to be formatted according to 
	 * BSI TR-03151 chapter 5.1.2. 
	 * Reminder: the TR expects TransactionLogs to have file names constructed like this:<br>
	 * (Unixt||Utc||Gent)_(logtime)_Sig-(signatureCounter)_Log-Tra_No-(transactionNumber)_(Start||Update||Finish)_Client-(clientId).log <br><br>
	 * 
	 * @param startNumber - the lower bound of the interval. Has to be truly greater than zero.
	 * @param endNumber - the upper bound of the interval. Has to be truly greater than zero.
	 * @return a collection of files containing all Transaction-Logs with the specified transaction number(s). Null, if none were found.
	 * @throws ErrorParameterMismatch if one or both transaction numbers are <= 0 or if startNumber < endNumber.
	 */
	public Collection<File> listFilesTransactionNumbers(long startNumber, long endNumber) throws ErrorParameterMismatch {
		//check if one or both counters are <= 0. If that's the case, throw an exception.
		if(startNumber <= 0 || endNumber <= 0) {
			throw new ErrorParameterMismatch("Both transaction numbers must be > 0.");
		} 
		if(startNumber > endNumber) {
			throw new ErrorParameterMismatch(startNumber +" > " +endNumber +" should be the other way around.");
		}
		
		//check if both numbers are the same
		if(startNumber==endNumber) {
			//create a simple filter for filtering the files
			String regEx = "^(Gent|Unixt|Utc){1}_{1}((\\d{14}(\\.\\d\\d\\d){0,1})|(\\d{10}(\\d\\d){0,1}Z{1})|(\\d{1,19})){1}_Sig-{1}(\\d{1,19})_Log-Tra_No-"+startNumber +"{1}_{1}(Start|Update|Finish)_Client-{1}(\\S)+\\.log{1}$";
			//create a Collection, because a good filter can be used:
			RegexFileFilter transactionLogsWithTransactionNumber = new RegexFileFilter(regEx);
					
			//create a collection containing the filtered files
			Collection<File> fileCollection = null;
					//List<File> fileCollection = null;
			
			fileCollection = FileUtils.listFiles(new File(pathToStorageDir), transactionLogsWithTransactionNumber, null);
			//check if any Files were found with the specified transactionNumber, return null if there are no transactionLogs with the specified number
			if(fileCollection.isEmpty()) {
				return null;
			}
			else {
				return fileCollection;
			}
		}
		
		//if the startNumber < endNumber, a more complex filter has to be used
		else {
			//create a more advanced filter, then use that
			//idea: create a Collection. Then create a regEx for each transaction number, use a RegesFileFilter to filter for those files and add the result of each transaction 
			//number to the existing collection. Check if the collection is empty. If yes -> return null. If no -> return collection.toArray
			Collection<File> intervalCollection = null;
			
			//this part has to be here, to prevent NullPointerException. 
				String regExStartNumber = "^(Gent|Unixt|Utc){1}_{1}((\\d{14}(\\.\\d\\d\\d){0,1})|(\\d{10}(\\d\\d){0,1}Z{1})|(\\d{1,19})){1}_Sig-{1}(\\d{1,19})_Log-Tra_No-"+startNumber +"{1}_{1}(Start|Update|Finish)_Client-{1}(\\S)+\\.log{1}$";
				//create a Collection, because a good filter can be used:
				RegexFileFilter transactionLogsWithStartNumber = new RegexFileFilter(regExStartNumber);
				intervalCollection = FileUtils.listFiles(new File(pathToStorageDir), transactionLogsWithStartNumber, null);
				
			//loop through the transaction numbers contained in the interval.
			for(long currentTransactionNumber=(startNumber+1); currentTransactionNumber <= endNumber; currentTransactionNumber++) {
				Collection<File> filesForCurrentNumber = null;
				String regExTemp = "^(Gent|Unixt|Utc){1}_{1}((\\d{14}(\\.\\d\\d\\d){0,1})|(\\d{10}(\\d\\d){0,1}Z{1})|(\\d{1,19})){1}_Sig-{1}(\\d{1,19})_Log-Tra_No-"+currentTransactionNumber +"{1}_{1}(Start|Update|Finish)_Client-{1}(\\S)+\\.log{1}$";			
				
				RegexFileFilter currentNumberFilter = new RegexFileFilter(regExTemp);
				
				//use the filter to list all the files corresponding to the current transaction number
				filesForCurrentNumber = FileUtils.listFiles(new File(pathToStorageDir), currentNumberFilter, null);
							
				//check whether filesForCurrentNumber != null. If thats the case, add it to the intervalCollection
				//attention: one can NOT conclude, that if one transaction number from the interval is missing, then all following transaction numbers should be missing and
					//therefore, the loop can be terminated. This would cause errors, if the function "deleteStorage" in the TSE was called. 
					//This method could have caused the deletion of the earlier transaction logs from the interval, leaving the newer ones intact.
				if(!filesForCurrentNumber.isEmpty()) {
					intervalCollection.addAll(filesForCurrentNumber);
				}
			}	
			//if there were no transaction logs found for the specified interval, return null
			if(intervalCollection.isEmpty()) {
				return null;
			}
			else {
				return intervalCollection;
			}
		}
	}
	
	
	
	
	/**
	 * This method is used by the {@linkplain TSEController} for those exportData variants that filter by transaction number(s).
	 * Since TR-03151 requires an extended search for SystemLogs and AuditLogs in the interval the transaction(s) in question was/were performed only, if there are any 
	 * TransactionLogs with that transaction number(s) stored, the filtering of those can not be performed by {@linkplain #listFilesTransactionNumbers(long, long)}.
	 * Another reason is the fact that a call of {@linkplain TSEController#updateTime(ZonedDateTime)} could mess up the sequence in which the log files are stored on 
	 * the file system. <br>
	 * This method here takes an interval of signature counters, often those corresponding to a/multiple pre-filtered transaction log message(s), and searches 
	 * in that interval for SystemLogs and AuditLogs.
	 * The search for those log messages may consume a lot of time, since the search is performed for each signature counter not already contained in the interval.
	 * 
	 * @param sigCntrsAlreadyPresent a list of signature counters that form an interval 
	 * @return a collection of files, more specifically, a linked list, of the System and AuditLogs missing from the signature counter interval passed in. Null, if no such log files were found.
	 * @throws SignatureCounterException if the interval is too short or not present at all (null)
	 */
	public Collection<File> listFilesAuditLogsSystemLogsSignatureCounter(List<Long> sigCntrsAlreadyPresent) throws SignatureCounterException {
		if(sigCntrsAlreadyPresent == null || sigCntrsAlreadyPresent.isEmpty()) {
			throw new SignatureCounterException("Interval null.");
		}
		//get first and last elements 
		Long startSigCntr = sigCntrsAlreadyPresent.get(0);
		Long endSigCntr = sigCntrsAlreadyPresent.get(sigCntrsAlreadyPresent.size()-1);
		//create a check to confirm, that sigCntrsAlreadyPresent is long enough to not immediately throw an IndexOutOfBounds
		if((startSigCntr == endSigCntr) || (endSigCntr-startSigCntr==1)) {
			throw new SignatureCounterException("Interval consists of too few signature counters that are consecutive as well.");
		}
		
		//go through the list of signature counters. Whenever one is missing for a continuous interval, search for the corresponding log message, but 
			//search only for system and audit logs. 
			//Because the transaction logs with the specified number have already been filtered, we now only need the other types of logs
		Collection<File> missingSysAudLogs = new LinkedList<File>();
		
		//maybe change this so that currentSigCntr < endSigCntr
		for(long currentSigCntr=(startSigCntr+1); currentSigCntr<endSigCntr; currentSigCntr++) {		
			//if the signature counter is already present, continue
			if(sigCntrsAlreadyPresent.contains(currentSigCntr)) {
				continue;
			}
			//else, search for the log with currentSigCntr, if that is not a transaction log (those would have already been filtered)
			else {
				//create the FileFilter to get that exact system or audit log
				Collection<File> filesForCurrentCounter = null;	
				String regExTemp ="^(Gent|Unixt|Utc){1}_{1}((\\d{14}(\\.\\d\\d\\d){0,1})|(\\d{10}(\\d\\d){0,1}Z{1})|(\\d{1,19})){1}_Sig-"+(currentSigCntr) +"{1}_Log-(Sys_(Initialize|UpdateTime|DisableSecureElement|AuthenticateUser|LogOut|UnblockUser){1}|(Aud){1}){1}(_Fc-(\\d)+){0,1}\\.log$";
				RegexFileFilter currentCounterFilter = new RegexFileFilter(regExTemp);
				
				//use the filter to add the log messages to the collection
				filesForCurrentCounter = FileUtils.listFiles(new File(pathToStorageDir), currentCounterFilter, null);

				//if that log message exists and is not an transaction log, add it to the missingSysAudLogs Collection and insert its signature counter 
				//into the sigCntrsAlreadyPresent List so that the iterator can iterate correctly
				if(!filesForCurrentCounter.isEmpty()) {
					missingSysAudLogs.addAll(filesForCurrentCounter);
				}
			}
			
		}
		
		//if no additional files were found, return nothing. Else, return the files found.
		if(missingSysAudLogs.isEmpty()) {
			return null;
		}
		else {
			return missingSysAudLogs;
		}
	}
	
	
	/**
	 * <b>Old version of {@linkplain #listLogFiles()}.</b><br>
	 * Method used by {@linkplain TSEController#exportData(int, main.java.de.bsi.seapi.holdertypes.ByteArrayHolder)} to get all logs that could possibly be exported. 
	 * Replaces the former used method <code>public int numberOfAvailableLogs()</code>. <br>
	 * {@linkplain #listLogFiles()} uses this method as well internally.
	 * 
	 * It searches the directory specified in storageDir for files ending with ".log".<br>
	 * If something goes wrong, i.e. the path to the storage directory can not be resolved properly or an I/O error occurs,
	 * the list of files found will be null and the return value of this function will be null as well.
	 * Otherwise, all files ending with ".log" will be returned. 
	 * 
	 * Please be aware, that unlike with the predecessor of this method, the caller has to make sure, that it does not continue working with a null value.
	 * 
	 * @return all log message files in the storage directory, if there are any. Null otherwise.
	 * @since 1.4
	 */
	public File[] listLogFilesOLD() {
		//create a File object of the storage dir
		 File storageDirectory = new File(pathToStorageDir);
		 //create a FileFilter which returns only the real logmessage files. this means, all files in the storageDirectory ending with .log .
		 LogmessageFileFilter onlyLogmessagesFilter = new LogmessageFileFilter("");
		//use that filter to get all the logmessages in a list
		File[] fileList = storageDirectory.listFiles(onlyLogmessagesFilter);
		
		//return the list. TSEController has to check whether the list is null or has a length of zero.
		return fileList;
	}
	
	
	/**
	 * Uses {@linkplain #listLogFilesOLD()} to fetch an array of File objects containing all log messages. Then converts that array into an ArrayList
	 * through the usage of <code> List<File> list = new ArrayList<File>(Arrays.asList(arrayOfLogfiles))</code><br>
	 * This results in the old array being used as a backing for the new list. Any changes to the list will be written through to the underlying array.
	 * @return all log message files in the storage directory, if there are any. Null otherwise.
	 */
	public Collection<File> listLogFiles() {
		//using the old file filter, since we know it works (and using NIO package seems too much hassle!)
		File[] listedFilesOldMethod = listLogFilesOLD();
		//check if the list is empty, because likely the conversion will fail with NullPointerException otherwise
		if(listedFilesOldMethod == null) {
			return null;
		}
		//create the list that will be returned
		List<File> listedFilesCollection = new ArrayList<File>(Arrays.asList(listedFilesOldMethod));
		//return that
		return listedFilesCollection;
	}
	
	
	
	/**
	 * Used for filtering only those files, that were created by a certain client.
	 * Caller has to make sure the File[] provided is already filtered and that the clientID is not null.
	 * Mainly used by {@linkplain TSEController} for the exportData methods.
	 * If the pre-filtered files contain SystemLogs and/or AuditLogs, those are returned as well.
	 * @param clientID the clientId whose log files shall be filtered
	 * @param prefilteredFiles an array of files that already match a certain condition (e.g. were all created in a certain period of time).
	 * @return the array of files filtered again for that specific clientID
	 */
	public File[] listFilesClientIDOLD(String clientID, File[] prefilteredFiles) {
		//create an array list since at this point in time, we do not know how many pre-filtered files were created by the clientID.
		ArrayList<File> clientIDFiltered = new ArrayList<File>();
		
		
		//create two Strings matching exactly the client ID ( so it's impossible that a clientID ab and a client id abc sometimes get filtered the same.
			//e.g. filtering for "containing ab" would yield abc as well.
		String clientIDWithFC = "_Client-" +clientID +"_Fc-";
		String clientIDWithoutFC = "_Client-" +clientID +".log";
		
		//go through array and check if a client corresponds to this client ID
		for(File f : prefilteredFiles) {
			//accept if 
				//it's a Syslog containing _Log-Sys_
				//it's an AuditLog containing _Log-Aud_
				//contains clientIDWithFC or clientIDWithoutFC
			String fName = f.getName();
			if((fName.contains("_Log-Sys_")) || (fName.contains("_Log-Aud_")) || (fName.contains(clientIDWithoutFC)) || (fName.contains(clientIDWithFC))) {
				clientIDFiltered.add(f);
			}
		}
		
		return clientIDFiltered.toArray(new File[0]);
	}
	
	
	/**
	 * Used for filtering only those files, that were created by a certain client.
	 * Caller has to make sure the Collection<File> provided is already filtered and that the clientID is not null.
	 * Mainly used by {@linkplain TSEController} for the exportData methods.
	 * If the pre-filtered files contain SystemLogs and/or AuditLogs, those are returned as well.
	 * @param clientID the clientId whose log files shall be filtered
	 * @param prefilteredFiles a collection of files that already match a certain condition (e.g. were all created in a certain period of time).
	 * @return the array of files filtered again for that specific clientID
	 */
	public Collection<File> listFilesClientID(String clientID, Collection<File> prefilteredFiles) {
		//create an array list since at this point in time, we do not know how many pre-filtered files were created by the clientID.
		ArrayList<File> clientIDFiltered = new ArrayList<File>();

		//create two Strings matching exactly the client ID ( so it's impossible that a clientID ab and a client id abc sometimes get filtered the same.
			//e.g. filtering for "containing ab" would yield abc as well.
		String clientIDWithFC = "_Client-" +clientID +"_Fc-";
		String clientIDWithoutFC = "_Client-" +clientID +".log";	
		//go through array and check if a client corresponds to this client ID
		for(File f : prefilteredFiles) {
			//accept if 
				//it's a Syslog containing _Log-Sys_
				//it's an AuditLog containing _Log-Aud_
				//contains clientIDWithFC or clientIDWithoutFC
			String fName = f.getName();
			if((fName.contains("_Log-Sys_")) || (fName.contains("_Log-Aud_")) || (fName.contains(clientIDWithoutFC)) || (fName.contains(clientIDWithFC))) {
				clientIDFiltered.add(f);
			}
		}
		
		return clientIDFiltered;
	}
	

	/**
	 * Used for filtering those Transaction Log files that belong to a transaction which has a transaction number from a certain interval and during whose lifetime
	 * at least one operation (start, update, or finish) was performed by a particular clientId.<br>
	 * It takes a Collection of transaction log files that were filtered by their clientId and a Collection of transaction log files that were filtered by their transaction numbers.
	 * It then identifies those transactions whose transaction numbers appear in both lists.<br>
	 * <br>
	 * Is used in {@linkplain TSEController#exportData(long, long, String, int, main.java.de.bsi.seapi.holdertypes.ByteArrayHolder)} where filtering only by transaction number interval 
	 * or clientId alone does not suffice.<br>
	 * This method does not expect System- or AuditLogs as input and is strictly focused on returning TransactionLogs.
	 * A method that filters for the other log types may be needed after using this method. <br>
	 * Please note: this method of filtering is <b>not optimized</b> and may take some time.
	 * @param preFilteredByClientId collection of files that were all created by the same clientId. Preferably only transaction logs.
	 * @param preFilteredByTransactionNumbers collection of files that bear a certain transaction number or whose transaction number lies in a certain interval.
	 * @return null, if the clientId was not involved in any transactions with the provided transaction numbers. The collection containing those logs were clientId was at least 
	 * once involved otherwise.
	 */
	public Collection<File> listFilesClientIdAndTransactionNumber(Collection<File> preFilteredByClientId, Collection<File> preFilteredByTransactionNumbers){
		//check input
		if((preFilteredByClientId==null) || (preFilteredByTransactionNumbers==null) || (preFilteredByClientId.isEmpty()) || (preFilteredByTransactionNumbers.isEmpty())) {
			return null;
		}
		
		Collection<File> relevantTransactions = new ArrayList<File>();	//the collection of files that are to be exported
																			//will contain all transaction logs of those transactions that have at least one log file contained 
																			//in both input Collections 
		
		Collection<Long> relevantTransactionNumbers = new ArrayList<Long>();	//Collection which lists the relevant Transaction Numbers, e.g. the numbers of transactions where the client Id was involved at least once.
																				
		//create a copy of Collection<File> preFilteredByTransactionNumbers to avoid ConcurrentModificationException 
		Collection<File> preFilteredByTransactionNumbers_COPY = new ArrayList<File>();
		preFilteredByTransactionNumbers_COPY.addAll(preFilteredByTransactionNumbers);
		
		//go through the collection of Files that include only those transaction logs that were created by a certain clientId
		for(File currentFilePrefilteredByClientId : preFilteredByClientId) {
			//get the Transaction Number of currentFile:
			String[] splitFileName = currentFilePrefilteredByClientId.getName().split("_");
			//if the file name was formatted correctly, getting the substring after No- should be the transactionNumber
			String tNr = splitFileName[4].substring(3);
			Long currentFileTNr = Long.parseLong(tNr);
			
			//does the current transaction number exist in the list of relevantTransactionNumbers? nothing to do, go on with next file from preFilteredByClientId
			if(relevantTransactionNumbers.isEmpty() || (!relevantTransactionNumbers.contains(currentFileTNr))) {
				//if it does not contain the current transaction number, add it to the list
				relevantTransactionNumbers.add(currentFileTNr);
				//create an Iterator over the copy of files pre filtered by transaction number
				Iterator<File> prefilteredTNIterator = preFilteredByTransactionNumbers_COPY.iterator();
				
				while(prefilteredTNIterator.hasNext()) {
					File current = prefilteredTNIterator.next();
					//if the current file contains the newly discovered relevant transaction number...
					if(current.getName().contains(("_Log-Tra_No-"+currentFileTNr.toString()))){
						//...add the current file to the relevantTransactions collection
						relevantTransactions.add(current);
						//remove that entry from prefilteredByTransactionNumber, so the next time this iterator has to work, its search structure is smaller
						preFilteredByTransactionNumbers.remove(current);
						//DO NOT REMOVE current FROM currentFilePrefilteredByClientId, THIS CAUSES ConcurrentModificationException !
					}
				}
			}
			//delete all entries from the preFilteredByTransactionNumbers_COPY that were inserted into relevantTransactions
			preFilteredByTransactionNumbers_COPY.clear();;
			preFilteredByTransactionNumbers_COPY.addAll(preFilteredByTransactionNumbers);
		}
		
		if(relevantTransactions.isEmpty()) {
			return null;
		}
		return relevantTransactions;
	}
	
	
	
	/**
	 * Used for fetching the certificate files used by the TSE-Simulator. Does not work, if the directory specified by config.properties "keyDir" 
	 * does not contain all the certificate files. Does not work either, if the certificate files are not named according to BSI TR-03151. This includes 
	 * not having the 256 Bit public key hash in their file name.<br> This is due to the usage of regular expressions for filtering the files.
	 * @return an array of files that are contained in the keyDir and are certificate files.
	 * @throws IOException if {@linkplain PropertyValues#getInstance()} throws it
	 */
	public File[] listCertificateFiles() throws IOException {
		//create a Collection, because of efficiency
		Collection<File> certificateFilesCollection = null;
		
		//create a RegEx for all possible Certificate Files use that in creating the filter
		//exactly 64 characters from the set {0-9, A-F}. Why? Because of the 256 Bit hash of the public key used in the certificate being encoded as hexadecimal values.
		//following that, either "_X509" OR "_CVC"
		//and in the end, one of the following: ".cer", ".CER", ".crt", ".CRT", ".pem", ".PEM", ".der", ".DER"
		//EVERYTHING ELSE SOULD NOT BE A CERTIFICATE
		
		//^([0-9]|[A-F]|[a-f]){64}_(X509|CVC){1}\.(cer|CER|crt|CRT|pem|PEM|der|DER){1}$
		String regExCertFile = "^([0-9]|[A-F]|[a-f]){64}_(X509|CVC){1}\\.(cer|CER|crt|CRT|pem|PEM|der|DER){1}$";
		RegexFileFilter certificateFilesFileFilter = new RegexFileFilter(regExCertFile);
		
		//fetch the path to the keyDir from config.properties. May cause IOError, if PropertyValues does not know where config.properties is located
		String pathToKeyDir = PropertyValues.getInstance().getPathToKeyDir();
		certificateFilesCollection = FileUtils.listFiles(new File(pathToKeyDir), certificateFilesFileFilter, null);
		
		//check if the collection contains the TSE-Certificate. If not, try to add it.
		File certFile = new File(pathToKeyDir ,PropertyValues.getInstance().getValue(ConfigConstants.CFG_TAG_TSE_CERT));
		if(!certificateFilesCollection.contains(certFile)) {
			certificateFilesCollection.add(certFile);
		}
		
		//check if the collection has remained empty. If yes, return null. Return the array of found files otherwise
		if(certificateFilesCollection.isEmpty()) {
			return null;
		}
		return certificateFilesCollection.toArray(new File[0]);
	}

//------------------------------INFO CSV CREATION-------------------------------------------------------------
	
	/**
	 * Method for creating the info.csv file in the storageDir so it may be included in the TAR-archive that will be exported.
	 * All parameters have to be provided by the {@linkplain TSEController}.
	 * For more information on how the info.csv file shall be constructed, see BSI TR-03153 Version 1.0.1 Tabelle 4.
	 * @param description the description of the SE API.
	 * @param manufacturerInfo information about the manufacturer.
	 * @param version information about the version of the SE API. This shall not be confused with the version present in each log message. 
	 * @return a File named info.csv. The content is set according to BSI TR-03151 chapter 5.1.1.
	 */
	private File createInfoCSV(String description, String manufacturerInfo, String version) {
		//create the text for the info.csv file
			//The values $1, $2 and $3 SHALL be enclosed in double quotes and may contain commas. 
			StringBuilder infoCSVBuilder = new StringBuilder("\"description:\",\"").append(description).append("\",\"manufacturer:\",\"");
			infoCSVBuilder.append(manufacturerInfo).append("\",\"version:\",\"").append(version).append("\"\n");
			
			//check if info.csv is already there.
			File infoCSV = new File(pathToStorageDir, Constants.FILE_NAME_INFO_CSV);
			//if the file isn't there, make one and write the info string into it
			if(!(infoCSV.exists())){
				try {
					FileWriter fileOut = new FileWriter(infoCSV);
					fileOut.write(infoCSVBuilder.toString());
					fileOut.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			//if info.csv already exists because of prior simulator runs (for example) check if the contents are the same and write the new info if not
			else {
				try {
					BufferedReader fileIn = new BufferedReader(new FileReader(infoCSV)); 
					StringBuilder oldInfoCSVBuilder = new StringBuilder();
					while(fileIn.ready()) {
						oldInfoCSVBuilder.append(fileIn.readLine());
					}
					//check if content of old info.csv == current info
					if(!(oldInfoCSVBuilder.toString().equalsIgnoreCase(infoCSVBuilder.toString()))) {
						FileWriter fileOut = new FileWriter(infoCSV);
						fileOut.write(infoCSVBuilder.toString());
						fileOut.close();
					}
					fileIn.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		//return info.csv 
		return infoCSV;
	}
	
	
}
