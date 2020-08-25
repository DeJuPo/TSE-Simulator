# General Information
Program simulating a Technical Security System (TSS, TSE in German) according to BSI TR-03153.
Project conducted in cooperation with the Bundesamt für Sicherheit in der Informationstechnik (BSI).
More information on TSE and the Technical Guidelines surrounding it can be found at https://www.bsi.bund.de/DE/Themen/DigitaleGesellschaft/Grundaufzeichnungen/grundaufzeichnungen_node.html  (website only available in German).

# Set-up
Please note: this is a temporary quick-start guide, a more in-depth user guide will be provided in the future!

## Files included
* TSE-Simulator-TR-03153_v1-5-5.jar: .java and .class files of the program as one .jar file
* JavaDocs-TSE-Sim-v1-5-5: IDE-generated documentation files for the program
* main/java/de/bsi/: source code of the program, written in Java 1.8
	* seapi: slightly modified version of the SE API Java package provided at  (ZIP-file under _Anhang der BSI TR-03151_)
	* tsesimulator: the simulator packages and classes
* standard-config: all configuration files needed to get started with the simulator, including example key material and certificates (provided TSE key uses brainpoolP512r1 curve)
* test-files: four different test configurations (configA - configD), including only the files needed for each particular configuration
* LICENSE: the project's license
* NOTICE: additional information on the used libraries

## Libraries needed
This project uses additional libraries that are not provided on this GitHub. 
**The simulator will not work without these libraries!**
They need to be downloaded and installed for the program to work properly.
Note that the versions listed are the ones the program was developed with. 
Later releases should work just as well.

* The BouncyCastle Provider and Main Package: bcprov-jdk15on-161.jar
* The BouncyCastle PKIX/CMS/EAC/DVCS/PKCS/TSP/OPENSSL Package: bcpkix-jdk15on-161.jar
* The Apache Commons Compress Package: commons-compress-1.18.jar
* The Apache Commons IO Package: commons-io-2.6.jar

The BouncyCastle packages can be obtained here https://bouncycastle.org/latest_releases.html
The Apache Commons packages can be found here https://commons.apache.org/proper/commons-compress/ and here http://commons.apache.org/proper/commons-io/ 

## Files and directories the simulator uses
Since the simulator has no own hardware to use, it must rely on some directories and files to function properly. For the sake of readability, all files are referred to by their out-of-the-box names.

File name | purpose
------------ | -------------
standard-config **or** testFiles/configX | Root directory for the configuration and storage tree of the simulator. Acts as a point of reference for the location of all other files and directories.
The path to this directory has to be set prior to simulator usage. 

keys | The directory storing the private keys of the simulator and the certificates corresponding to those keys. 
Configured through config.properties

normalStoring | The directory acting as the normal storage of the TSE simulator. This is where log messages are stored, the TAR file created by exportData, the TAR file created by exportCertificates, the info.csv file and the record of already exported data is kept.
This directory can be cleaned through the usage of a deleteStoredData function call.
Configured through config.properties

persistentStoring | The directory acting as persistentStorage of the TSE simulator. This is where the files storing user data and, if created, the important simulator data is persisted after a gracefulShutdown function call.
Configured through config.properties

config.properties | The configuration file of the simulator. Several different values can be modified and affect the behaviour of the simulator. 

userlist.properties | Stores the userIds of the users that are registered to use the TSE simulator for an easy answer to the question ‚Is a user managed by the TSE simulator?‘.


All of those files and directories have to be accessible by the simulator for it to work.


# Using the simulator
If you have successfully downloaded all the files the program needs and set it up with your IDE of choice and have installed all the needed libraries correctly, you are able to use the simulator.

The class posing as the TSE to the outside is called _TSEController_. It should only be created one instance at a time, because the program will have issues if multiple TSEControllers exist. This is mainly caused by access to configuration files and storage.

To understand each function and to know in which order they should be called, please refer to BSI TR-03151 and the JavaDoc directory in this repository. 
Explaining all the functions and their purpose in this document would go too far.

## Setting the standard-config directory
Before creating a new TSEController, you have to tell the program where its resources are located, meaning, it has to know, where _standard-config_ (or _test-files/configX_) is located. This can be done 2 ways:
* Calling 'PropertyValues.setPathToResourceDirectory(_Path to standard-config on file system_)' followed by using the default constructor of the TSEController 'TSEController myTseController = new TSEController()'.
* Calling 'TSEController myTseController = new TSEController(_Path to standard-config on file system_)'

## Logging-in
The simulator comes with 2 default users, one with the role of admin and the other with the role of timeAdmin. For some functions it is necessary according to BSI TR-03151 for an authenticated authorized user to be logged in. This section lists the credentials of the 2 provided default users:

### admin
String UserId = root
String role = admin
byte array PIN = {1,2,3,4,5,6} 
byte array PUK = {1,1,2,3,5,8}

### timeAdmin
String UserId = timekeeper
String role = timeAdmin
byte array PIN = {0, 9, 8, 7, 6, 5}
byte array PUK = {5, 8, 13, 21, 34}


## registering clients
Even though BSI TR-03151 does not require it, the simualator needs clients to be registered before they can use the input functions mentioned in that Technical Guideline.
This can only be done by users in the admin role through the usage of the functions _registerClient_ and _deregisterClient_ inside the TSEController class.

## gracefulShutdown and persistence
The simulator can be used as a TSE that ceases to exist on program exit or it can be persisted on the file system. This is done through the usage of _gracefulShutdown_ inside the TSEController class.
By calling that function, the TSEController closes all remaining transactions and stores the following values in the file _persitentValues.ser_ inside the _persistentStorage_ directory:

* current CryptoCore clock status
* initialization status of the TSE
* status of SecurityModule
* signature counter status
* transaction counter status 
* description of the device

If the program you are using to call the TSEController class then creates a new instance of the TSEController, the simulator checks for that _persistentValues.ser_ file. If it exists, it loads the values stored there and goes on from there.

If you do now wish to use this feature, simply do not call gracefulShutdown. If you already have used that function, simply delete _persistentValues.ser_ manually or through the usage of _deletePersistedValues_ in the class _PersistentStorage_.

Please note that this does not affect user data or the log files stored in _normalStoring_.
Log files have to be either deleted manually or exported and then deleted using deleteStoredData.

## Configuration options
The file _config.properties_ provides some configuration options to the user. The configurations in the test-files directory can be used as a starting point to understanding the different options.
If you are not sure about what each configuration does, either experiment at your own risk or wait for the user guide to be published.

