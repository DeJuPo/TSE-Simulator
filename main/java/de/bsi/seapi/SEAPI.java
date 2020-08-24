package main.java.de.bsi.seapi;

import java.time.ZonedDateTime;

import main.java.de.bsi.seapi.exceptions.ErrorExportCertFailed;
import main.java.de.bsi.seapi.exceptions.ErrorFinishTransactionFailed;
import main.java.de.bsi.seapi.exceptions.ErrorIdNotFound;
import main.java.de.bsi.seapi.exceptions.ErrorNoDataAvailable;
import main.java.de.bsi.seapi.exceptions.ErrorNoLogMessage;
import main.java.de.bsi.seapi.exceptions.ErrorParameterMismatch;
import main.java.de.bsi.seapi.exceptions.ErrorReadingLogMessage;
import main.java.de.bsi.seapi.exceptions.ErrorRestoreFailed;
import main.java.de.bsi.seapi.exceptions.ErrorRetrieveLogMessageFailed;
import main.java.de.bsi.seapi.exceptions.ErrorUpdateTimeFailed;
import main.java.de.bsi.seapi.exceptions.ErrorStartTransactionFailed;
import main.java.de.bsi.seapi.exceptions.ErrorStorageFailure;
import main.java.de.bsi.seapi.exceptions.ErrorStoringInitDataFailed;
import main.java.de.bsi.seapi.exceptions.ErrorTooManyRecords;
import main.java.de.bsi.seapi.exceptions.ErrorTransactionNumberNotFound;
import main.java.de.bsi.seapi.exceptions.ErrorUpdateTransactionFailed;
import main.java.de.bsi.seapi.exceptions.ErrorNoTransaction;
import main.java.de.bsi.seapi.exceptions.ErrorTimeNotSet;
import main.java.de.bsi.seapi.exceptions.ErrorSeApiNotInitialized;
import main.java.de.bsi.seapi.exceptions.ErrorCertificateExpired;
import main.java.de.bsi.seapi.exceptions.ErrorUserNotAuthorized;
import main.java.de.bsi.seapi.exceptions.ErrorUserNotAuthenticated;
import main.java.de.bsi.seapi.exceptions.ErrorDescriptionNotSetByManufacturer;
import main.java.de.bsi.seapi.exceptions.ErrorDescriptionSetByManufacturer;
import main.java.de.bsi.seapi.exceptions.ErrorExportSerialNumbersFailed;
import main.java.de.bsi.seapi.exceptions.ErrorGetMaxNumberOfClientsFailed;
import main.java.de.bsi.seapi.exceptions.ErrorGetCurrentNumberOfClientsFailed;
import main.java.de.bsi.seapi.exceptions.ErrorGetMaxNumberTransactionsFailed;
import main.java.de.bsi.seapi.exceptions.ErrorGetCurrentNumberOfTransactionsFailed;
import main.java.de.bsi.seapi.exceptions.ErrorGetSupportedUpdateVariantsFailed;
import main.java.de.bsi.seapi.exceptions.ErrorGetTimeSyncVariantFailed;
import main.java.de.bsi.seapi.exceptions.ErrorDeleteStoredDataFailed;
import main.java.de.bsi.seapi.exceptions.ErrorUnexportedStoredData;
import main.java.de.bsi.seapi.exceptions.ErrorSigningSystemOperationDataFailed;
import main.java.de.bsi.seapi.exceptions.ErrorUserIdNotManaged;
import main.java.de.bsi.seapi.exceptions.ErrorUserIdNotAuthenticated;
import main.java.de.bsi.seapi.exceptions.ErrorSecureElementDisabled;
import main.java.de.bsi.seapi.exceptions.ErrorDisableSecureElementFailed;
import main.java.de.bsi.seapi.holdertypes.ByteArrayHolder;
import main.java.de.bsi.seapi.holdertypes.LongHolder;
import main.java.de.bsi.seapi.holdertypes.ShortHolder;
import main.java.de.bsi.seapi.holdertypes.SyncVariantsHolder;
import main.java.de.bsi.seapi.holdertypes.UpdateVariantsHolder;
import main.java.de.bsi.seapi.holdertypes.AuthenticationResultHolder;
import main.java.de.bsi.seapi.holdertypes.UnblockResultHolder;
import main.java.de.bsi.seapi.holdertypes.ZonedDateTimeHolder;
import main.java.de.bsi.tsesimulator.exceptions.ErrorSignatureCounterOverflow;
import main.java.de.bsi.tsesimulator.exceptions.ErrorTransactionCounterOverflow;
import main.java.de.bsi.seapi.exceptions.ErrorInvalidTime;


/**
 * This interface defines the functions that are provided by the Secure Element
 * API (SE API) version 1.0.1
 * 
 * <br><br>
 * The following functions were modified by the author of the TSE-Simulator due to demands (throwing exception when signature and/or transaction counter(s) 
 * would overflow made by BSI TR-03153:<br>
 * <ul>
 * <li>short initialize(String description) </li>
 * <li>short initialize() </li>
 * <li>short updateTime(ZonedDateTime newDateTime) </li>
 * <li>short updateTime() </li>          
 * <li>short disableSecureElement() </li>
 * <li>short startTransaction(String clientId, byte[] processData, String processType, byte[] additionalData, LongHolder transactionNumber, ZonedDateTimeHolder logTime,  ByteArrayHolder serialNumber, LongHolder signatureCounter, ByteArrayHolder signatureValue) </li>
 * <li>short updateTransaction(String clientId, long transactionNumber, byte[] processData, String processType, ZonedDateTimeHolder logTime, ByteArrayHolder signatureValue, LongHolder signatureCounter) </li>
 * <li>short finishTransaction(String clientId, long transactionNumber, byte[] processData, String processType, byte[] additionalData, ZonedDateTimeHolder logTime, ByteArrayHolder signatureValue, LongHolder signatureCounter) </li>
 * <li>short authenticateUser(String userId, byte[] pin, AuthenticationResultHolder authenticationResult, ShortHolder remainingRetries)   </li>
 * <li>short logOut(String userId) </li>
 * <li>short unblockUser(String userId, byte[] puk, byte[] newPin, UnblockResultHolder unblockResult)    </li>
 * </ul>       
                      
   <br>                                         
 * The following functions were modified by the author of the TSE-Simulator to allow throwing ErrorParameterMismatch, which is expected for all exportData
 * functions by TR-03151. In addition to that, the author added ErrorParameterMismatch to other functions that did not have a way to handle illegal inputs:<br><br>       
 * <ul>      
 * <li>short exportData(long transactionNumber, String clientId, ByteArrayHolder exportedData) </li>  
 * <li>short exportData(long transactionNumber, ByteArrayHolder exportedData)</li>
 * <li>short exportData(int maximumNumberRecords, ByteArrayHolder exportedData)</li>
 * <li>short authenticateUser(String userId, byte[] pin, AuthenticationResultHolder authenticationResult, ShortHolder remainingRetries)</li>
 * <li>short unblockUser(String userId, byte[] puk, byte[] newPin, UnblockResultHolder unblockResult)</li>
 * <li>short readLogMessage(ByteArrayHolder logMessage) </li>
 * </ul>
 */

public interface SEAPI {

    /**
     * Represents the variants that are supported by the Secure Element to update
     * transactions.
     */
    enum UpdateVariants {
        signedUpdate, unsignedUpdate, signedAndUnsignedUpdate
    };

    /**
     * Represents the variants that are supported by the Secure Element to update
     * the current date/time.
     */
    enum SyncVariants {
        noInput, utcTime, generalizedTime, unixTime
    };

    /**
     * Represents the result of an authentication The value ok SHALL indicate that
     * the authentication has been successful. The value failed SHALL indicate that
     * the authentication has failed. The value pinIsBlocked SHALL indicate that the
     * PIN entry for the userId was blocked before the authentication attempt. The
     * value unknownUserId SHALL indicate that the passed userId is not managed by
     * the SE API.
     */
    enum AuthenticationResult {
        ok, failed, pinIsBlocked, unknownUserId
    };

    /**
     * Represents the result of the unblock process. The value ok SHALL indicate
     * that the unblocking has been successful. The value failed SHALL indicate that
     * the unblocking has failed. The value unknownUserId SHALL indicate that the
     * passed userId is not managed by the SE API. The value error SHALL indicate
     * that an error has occurred during the execution of the function unblockUser.
     */
    enum UnblockResult {
        ok, failed, unknownUserId, error
    };

    /**
     * The function initialize starts the initialization of the SE API by the
     * operator of the corresponding application. The initialization data in form of
     * the description of the SE API is passed by the input parameter description.
     * The description of the SE API MUST NOT have been set by the manufacturer.
     * 
     * @param description
     *            short description of the SE API. The parameter SHALL only be used
     *            if the description of the SE API has not been set by the
     *            manufacturer [INPUT PARAMETER, REQUIRED]
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned
     * @throws ErrorSigningSystemOperationDataFailed
     *             determination of the log message parts for the system operation
     *             data by the Secure Element failed
     * @throws ErrorStoringInitDataFailed
     *             storing of the initialization data failed
     * @throws ErrorRetrieveLogMessageFailed
     *             execution of the Secure Element functionality to retrieve log
     *             message parts has failed
     * @throws ErrorStorageFailure
     *             storing of the log message has failed
     * @throws ErrorCertificateExpired
     *             the certificate with the public key for the verification of the
     *             appropriate type of log messages is expired. Even if a
     *             certificate expired, the log message parts are created by the
     *             Secure Element and stored by the SE API. In this case, the
     *             exception ErrorCertificateExpired is raised only after the data
     *             of the log message has been stored.
     * @throws ErrorSecureElementDisabled
     *             the Secure Element has been disabled
     * @throws ErrorUserNotAuthorized
     *             the user who has invoked the function initialize is not
     *             authorized to execute this function
     * @throws ErrorUserNotAuthenticated
     *             the user who has invoked the function initialize has not the
     *             status authenticated
     * @throws ErrorDescriptionSetByManufacturer
     *             the function initialize has been invoked with a value for the
     *             input parameter description although the description of the SE
     *             API has been set by the manufacturer
     *             
     * @throws ErrorSignatureCounterOverflow
     * 				the function initialize has been invoked although the value of the 
     * 				signature counter in the CryptoCore module has entered an illegal state
     */
    short initialize(String description) 
                     throws ErrorSigningSystemOperationDataFailed, 
                            ErrorStoringInitDataFailed,
                            ErrorRetrieveLogMessageFailed, 
                            ErrorStorageFailure, 
                            ErrorCertificateExpired, 
                            ErrorSecureElementDisabled,
                            ErrorUserNotAuthorized, 
                            ErrorUserNotAuthenticated, 
                            ErrorDescriptionSetByManufacturer,
                            ErrorSignatureCounterOverflow;

    /**
     * The function initialize starts the initialization of the SE API by the
     * operator of the corresponding application. The description of the SE API
     * SHALL has been set by the manufacturer.
     * 
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned
     * @throws ErrorSigningSystemOperationDataFailed
     *             determination of the log message parts for the system operation
     *             data by the Secure Element failed
     * @throws ErrorRetrieveLogMessageFailed
     *             execution of the Secure Element functionality to retrieve log
     *             message parts has failed
     * @throws ErrorStorageFailure
     *             storing of the log message has failed
     * @throws ErrorCertificateExpired
     *             the certificate with the public key for the verification of the
     *             appropriate type of log messages is expired. Even if a
     *             certificate expired, the log message parts are created by the
     *             Secure Element and stored by the SE API. In this case, the
     *             exception ErrorCertificateExpired is raised only after the data
     *             of the log message has been stored.
     * @throws ErrorSecureElementDisabled
     *             the Secure Element has been disabled
     * @throws ErrorUserNotAuthorized
     *             the user who has invoked the function initialize is not
     *             authorized to execute this function
     * @throws ErrorUserNotAuthenticated
     *             the user who has invoked the function initialize has not the
     *             status authenticated
     * @throws ErrorDescriptionNotSetByManufacturer
     *             the function initialize has been invoked without a value for the
     *             input parameter description although the description of the SE
     *             API has not been set by the manufacturer
     *             
     *  @throws ErrorSignatureCounterOverflow
     * 				the function initialize has been invoked although the value of the 
     * 				signature counter in the CryptoCore module has entered an illegal state           
     */
    short initialize() throws ErrorSigningSystemOperationDataFailed, 
                              ErrorRetrieveLogMessageFailed, 
                              ErrorStorageFailure,
                              ErrorCertificateExpired, 
                              ErrorSecureElementDisabled, 
                              ErrorUserNotAuthorized, 
                              ErrorUserNotAuthenticated,
                              ErrorDescriptionNotSetByManufacturer,
                              ErrorSignatureCounterOverflow;

    /**
     * The function updateTime updates the current date/time that is maintained by
     * the Secure Element by passing a new date/time value
     * 
     * @param newDateTime
     *            new value for the date/time maintained by the Secure Element
     *            [INPUT PARAMETER, REQUIRED]
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned
     * @throws ErrorUpdateTimeFailed
     *             execution of the Secure Element functionality to set the time has
     *             failed
     * @throws ErrorRetrieveLogMessageFailed
     *             execution of the Secure Element functionality to retrieve log
     *             message parts has failed
     * @throws ErrorStorageFailure
     *             storing of the log message has failed
     * @throws ErrorSeApiNotInitialized
     *             the SE API has not been initialized
     * @throws ErrorCertificateExpired
     *             the certificate with the public key for the verification of the
     *             appropriate type of log messages is expired. Even if a
     *             certificate expired, the log message parts are created by the
     *             Secure Element and stored by the SE API. In this case, the
     *             exception ErrorCertificateExpired is raised only after the data
     *             of the log message has been stored.
     * @throws ErrorSecureElementDisabled
     *             the Secure Element has been disabled
     * @throws ErrorUserNotAuthorized
     *             the user who has invoked the function updateTime is not
     *             authorized to execute this function
     * @throws ErrorUserNotAuthenticated
     *             the user who has invoked the function updateTime has not the
     *             status authenticated
     * @throws ErrorInvalidTime
     * 				the time provided is out of bounds
     * 
     * @throws ErrorSignatureCounterOverflow
     * 				the function updateTime has been invoked although the value of the 
     * 				signature counter in the CryptoCore module has entered an illegal state
     */
    short updateTime(ZonedDateTime newDateTime) 
                     throws ErrorUpdateTimeFailed, 
                            ErrorRetrieveLogMessageFailed, 
                            ErrorStorageFailure, 
                            ErrorSeApiNotInitialized,
                            ErrorCertificateExpired, 
                            ErrorSecureElementDisabled, 
                            ErrorUserNotAuthorized, 
                            ErrorUserNotAuthenticated,
                            ErrorInvalidTime,
                            ErrorSignatureCounterOverflow;

    /**
     * The function updateTime updates the current date/time that is maintained by
     * the Secure Element by using the functionality for time synchronization of the
     * Secure Element
     * 
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned
     * @throws ErrorUpdateTimeFailed
     *             execution of the Secure Element functionality to update the time
     *             has failed
     * @throws ErrorRetrieveLogMessageFailed
     *             execution of the Secure Element functionality to retrieve log
     *             message parts has failed
     * @throws ErrorStorageFailure
     *             storing of the log message has failed
     * @throws ErrorSeApiNotInitialized
     *             the SE API has not been initialized
     * @throws ErrorCertificateExpired
     *             the certificate with the public key for the verification of the
     *             appropriate type of log messages is expired. Even if a
     *             certificate expired, the log message parts are created by the
     *             Secure Element and stored by the SE API. In this case, the
     *             exception ErrorCertificateExpired is raised only after the data
     *             of the log message has been stored.
     * @throws ErrorSecureElementDisabled
     *             the Secure Element has been disabled
     * @throws ErrorUserNotAuthorized
     *             the user who has invoked the function updateTime is not
     *             authorized to execute this function
     * @throws ErrorUserNotAuthenticated
     *             the user who has invoked the function updateTime has not the
     *             status authenticated
     *             
     * @throws ErrorSignatureCounterOverflow
     * 				the function updateTime has been invoked although the value of the 
     * 				signature counter in the CryptoCore module has entered an illegal state
     */
    short updateTime() throws ErrorUpdateTimeFailed, 
                              ErrorRetrieveLogMessageFailed, 
                              ErrorStorageFailure, 
                              ErrorSeApiNotInitialized,
                              ErrorCertificateExpired, 
                              ErrorSecureElementDisabled, 
                              ErrorUserNotAuthorized, 
                              ErrorUserNotAuthenticated,
                              ErrorSignatureCounterOverflow;

    /**
     * The function disableSecureElement disables the Secure Element in a way that
     * none of its functionality can be used anymore
     * 
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned
     * @throws ErrorDisableSecureElementFailed
     *             the deactivation of the Secure Element failed
     * @throws ErrorTimeNotSet
     *             the managed data/time in the Secure Element has not been updated
     *             after the initialization of the SE API or a period of absence of
     *             current for the Secure Element
     * @throws ErrorRetrieveLogMessageFailed
     *             execution of the Secure Element functionality to retrieve log
     *             message parts has failed
     * @throws ErrorStorageFailure
     *             storing of the data of the log message has failed
     * @throws ErrorCertificateExpired
     *             the certificate with the public key for the verification of the
     *             appropriate type of log messages is expired. Even if a
     *             certificate expired, the log message parts are created by the
     *             Secure Element and stored by the SE API. In this case, the
     *             exception ErrorCertificateExpired is raised only after the data
     *             of the log message has been stored.
     * @throws ErrorSecureElementDisabled
     *             the Secure Element has been disabled
     * @throws ErrorUserNotAuthorized
     *             the user who has invoked the function disableSecureElement is not
     *             authorized to execute this function
     * @throws ErrorUserNotAuthenticated
     *             the user who has invoked the function disableSecureElement has
     *             not the status authenticated
     *             
     * @throws ErrorSignatureCounterOverflow
     * 				the function disableSecureElement has been invoked although the value of the 
     * 				signature counter in the CryptoCore module has entered an illegal state
     * @throws ErrorSeApiNotInitialized
     *             the SE API has not been initialized
     */
    short disableSecureElement() throws ErrorDisableSecureElementFailed, 
                                        ErrorTimeNotSet, 
                                        ErrorRetrieveLogMessageFailed, 
                                        ErrorStorageFailure,
                                        ErrorCertificateExpired, 
                                        ErrorSecureElementDisabled, 
                                        ErrorUserNotAuthorized, 
                                        ErrorUserNotAuthenticated,
                                        ErrorSignatureCounterOverflow,
                                        ErrorSeApiNotInitialized;

    /**
     * Starts a new transaction
     * 
     * @param clientId
     *            represents the ID of the application that has invoked the function
     *            [INPUT PARAMETER, REQUIRED]
     * @param processData
     *            represents all the necessary information regarding the initial
     *            state of the process [INPUT PARAMETER, REQUIRED]
     * @param processType
     *            identifies the type of the transaction as defined by the
     *            application. The String representing the processType SHALL be
     *            restricted to a length of 100 [INPUT PARAMETER, OPTIONAL]
     * @param additionalData
     *            reserved for future use [INPUT PARAMETER, OPTIONAL]
     * @param transactionNumber
     *            represents a transaction number that has been assigned by the
     *            Secure Element to the process [OUTPUT PARAMETER, REQUIRED]
     * @param logTime
     *            represents the point in time of the Secure Element when the log
     *            message was created [OUTPUT PARAMETER, REQUIRED]
     * @param serialNumber
     *            represents hash value over the public key of the key pair that is
     *            used for the creation of signature values in transaction log
     *            messages [OUTPUT PARAMETER, REQUIRED]
     * @param signatureCounter
     *            represents the current value of the signature counter 
     *            [OUTPUT PARAMETER, REQUIRED]
     * @param signatureValue
     *            represents the signature value [OUTPUT PARAMETER, OPTIONAL]
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned
     * @throws ErrorStartTransactionFailed
     *             the execution of the Secure Element functionality to start a
     *             transaction failed
     * @throws ErrorRetrieveLogMessageFailed
     *             the execution of the Secure Element functionality to retrieve log
     *             message parts has failed
     * @throws ErrorStorageFailure
     *             storing of the log message failed
     * @throws ErrorSeApiNotInitialized
     *             the SE API has not been initialized
     * @throws ErrorTimeNotSet
     *             the managed data/time in the Secure Element has not been updated
     *             after the initialization of the SE API or a period of absence of
     *             current for the Secure Element
     * @throws ErrorCertificateExpired
     *             the certificate with the public key for the verification of the
     *             appropriate type of log messages is expired. Even if a
     *             certificate expired, the log message parts are created by the
     *             Secure Element and stored by the SE API. In this case, the
     *             exception ErrorCertificateExpired is raised only after the data
     *             of the log message has been stored.
     * @throws ErrorSecureElementDisabled
     *             the Secure Element has been disabled
     *             
     *             
     *             
     * @throws ErrorSignatureCounterOverflow
     * 				the function startTransaction has been invoked although the value of the 
     * 				signature counter in the CryptoCore module has entered an illegal state
     * @throws ErrorTransactionCounterOverflow
     * 				the function startTransaction has been invoked although the value of the transaction 
     * 				counter in the SecurityModule has entered an illegal state
     */
    short startTransaction(String clientId, 
                           byte[] processData, 
                           String processType, 
                           byte[] additionalData,
                           LongHolder transactionNumber, 
                           ZonedDateTimeHolder logTime, 
                           ByteArrayHolder serialNumber,
                           LongHolder signatureCounter, 
                           ByteArrayHolder signatureValue)
                           throws ErrorStartTransactionFailed, 
                                  ErrorRetrieveLogMessageFailed, 
                                  ErrorStorageFailure,
                                  ErrorSeApiNotInitialized, 
                                  ErrorTimeNotSet, 
                                  ErrorCertificateExpired, 
                                  ErrorSecureElementDisabled,
                                  ErrorTransactionCounterOverflow, 
                                  ErrorSignatureCounterOverflow;

    /**
     * Updates an open transaction
     * 
     * @param clientId
     *            represents the ID of the application that has invoked the function
     *            [INPUT PARAMETER, REQUIRED]
     * @param transactionNumber
     *            parameter is used to unambiguously identify the current
     *            transaction [INPUT PARAMETER, REQUIRED]
     * @param processData
     *            represents all the new information regarding the state of the
     *            process since the start of the corresponding transaction or its
     *            last update [INPUT PARAMETER, REQUIRED]
     * @param processType
     *            identifies the type of the transaction as defined by the
     *            application. The String representing the processType SHALL be
     *            restricted to a length of 100 [INPUT PARAMETER, OPTIONAL]
     * @param logTime
     *            represents the point in time of the Secure Element when the log
     *            message was created [OUTPUT PARAMETER, CONDITIONAL]
     * @param signatureValue
     *            represents the signature value [OUTPUT PARAMETER, CONDITIONAL]
     * @param signatureCounter
     *            represents the current value of the signature counter 
     *            [OUTPUT PARAMETER, CONDITIONAL]
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned
     * @throws ErrorUpdateTransactionFailed
     *             the execution of the Secure Element functionality to update a
     *             transaction failed
     * @throws ErrorStorageFailure
     *             storing of the log message failed
     * @throws ErrorRetrieveLogMessageFailed
     *             the execution of the Secure Element functionality to retrieve log
     *             message parts has failed
     * @throws ErrorNoTransaction
     *             no transaction is known to be open under the provided transaction
     *             number
     * @throws ErrorSeApiNotInitialized
     *             the SE API has not been initialized
     * @throws ErrorTimeNotSet
     *             the managed data/time in the Secure Element has not been updated
     *             after the initialization of the SE API or a period of absence of
     *             current for the Secure Element
     * @throws ErrorCertificateExpired
     *             the certificate with the public key for the verification of the
     *             appropriate type of log messages is expired. Even if a
     *             certificate expired, the log message parts are created by the
     *             Secure Element and stored by the SE API. In this case, the
     *             exception ErrorCertificateExpired is raised only after the data
     *             of the log message has been stored.
     * @throws ErrorSecureElementDisabled
     *             the Secure Element has been disabled
     *             
     * @throws ErrorSignatureCounterOverflow
     * 				the function updateTransaction has been invoked although the value of the 
     * 				signature counter in the CryptoCore module has entered an illegal state            
     */
    short updateTransaction(String clientId, 
                            long transactionNumber, 
                            byte[] processData, 
                            String processType,
                            ZonedDateTimeHolder logTime, 
                            ByteArrayHolder signatureValue, 
                            LongHolder signatureCounter)
                            throws ErrorUpdateTransactionFailed, 
                                   ErrorStorageFailure, 
                                   ErrorRetrieveLogMessageFailed, 
                                   ErrorNoTransaction,
                                   ErrorSeApiNotInitialized, 
                                   ErrorTimeNotSet, 
                                   ErrorCertificateExpired, 
                                   ErrorSecureElementDisabled,
                                   ErrorSignatureCounterOverflow;

    /**
     * Finishes an open transaction
     * 
     * @param clientId
     *            represents the ID of the application that has invoked the function
     *            [INPUT PARAMETER, REQUIRED]
     * @param transactionNumber
     *            parameter is used to unambiguously identify the current
     *            transaction [INPUT PARAMETER, REQUIRED]
     * @param processData
     *            represents all the information regarding the final state of the
     *            process [INPUT PARAMETER, REQUIRED]
     * @param processType
     *            identifies the type of the transaction as defined by the
     *            application. The String representing the processType SHALL be
     *            restricted to a length of 100 [INPUT PARAMETER, OPTIONAL]
     * @param additionalData
     *            reserved for future use [INPUT PARAMETER, OPTIONAL]
     * @param logTime
     *            represents the point in time of the Secure Element when the log
     *            message was created [OUTPUT PARAMETER, REQUIRED]
     * @param signatureValue
     *            represents the signature value [OUTPUT PARAMETER, OPTIONAL]
     * @param signatureCounter
     *            represents the current value of the signature counter 
     *            [OUTPUT PARAMETER, REQUIRED]
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned
     * @throws ErrorFinishTransactionFailed
     *             the execution of the Secure Element functionality to finish a
     *             transaction failed
     * @throws ErrorRetrieveLogMessageFailed
     *             the execution of the Secure Element functionality to retrieve log
     *             message parts has failed
     * @throws ErrorStorageFailure
     *             storing of the log message failed
     * @throws ErrorSeApiNotInitialized
     *             the SE API has not been initialized
     * @throws ErrorTimeNotSet
     *             the managed data/time in the Secure Element has not been updated
     *             after the initialization of the SE API or a period of absence of
     *             current for the Secure Element
     * @throws ErrorCertificateExpired
     *             the certificate with the public key for the verification of the
     *             appropriate type of log messages is expired. Even if a
     *             certificate expired, the log message parts are created by the
     *             Secure Element and stored by the SE API. In this case, the
     *             exception ErrorCertificateExpired is raised only after the data
     *             of the log message has been stored.
     * @throws ErrorSecureElementDisabled
     *             the Secure Element has been disabled
     * @throws ErrorNoTransaction 
     * 				no transaction is known to be open under the provided transaction
     *             	number
     *             
     * @throws ErrorSignatureCounterOverflow
     * 				the function finishTransaction has been invoked although the value of the 
     * 				signature counter in the CryptoCore module has entered an illegal state            
     */
    short finishTransaction(String clientId, 
                            long transactionNumber, 
                            byte[] processData, 
                            String processType,
                            byte[] additionalData, 
                            ZonedDateTimeHolder logTime, 
                            ByteArrayHolder signatureValue,
                            LongHolder signatureCounter)
                            throws ErrorFinishTransactionFailed, 
                                   ErrorRetrieveLogMessageFailed, 
                                   ErrorStorageFailure,
                                   ErrorSeApiNotInitialized, 
                                   ErrorTimeNotSet, 
                                   ErrorCertificateExpired, 
                                   ErrorSecureElementDisabled, 
                                   ErrorNoTransaction,
                                   ErrorSignatureCounterOverflow;

    /**
     * Exports the transaction log messages, containing the process and protocol
     * data, that correspond to a certain transaction and clientId. Additionally,
     * the function SHALL export all system log messages and audit log messages
     * whose signature counters are contained in the following interval: 
     *    Signature counter of the transaction log message for the start of the transaction and
     *    the signature counter of the transaction log message for the end of the transaction (inclusive).
     * Furthermore, additional files that are needed to verify the signatures, included in the 
     * log messages, are exported.
     * 
     * @param transactionNumber
     *            indicates the transaction whose corresponding log messages are
     *            relevant for the export [INPUT PARAMETER, REQUIRED]
     * @param clientId
     *            ID of a client application that has used the API to log
     *            transactions Only transaction log messages that correspond to the
     *            clientId are relevant for the export. [INPUT PARAMETER, REQUIRED]
     * @param exportedData
     *            selected log messages and additional files needed to verify the
     *            signatures included in the log messages [OUTPUT PARAMETER,
     *            REQUIRED]
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned
     * @throws ErrorTransactionNumberNotFound
     *             no data has been found for the provided transactionNumber
     * @throws ErrorIdNotFound
     *             no data has been found for the provided clientId
     * @throws ErrorSeApiNotInitialized
     *             the SE API has not been initialized
     * @throws ErrorParameterMismatch 
     * 				mismatch in parameters of the function
     */
    short exportData(long transactionNumber, 
                     String clientId, 
                     ByteArrayHolder exportedData)
                     throws ErrorTransactionNumberNotFound, 
                            ErrorIdNotFound, 
                            ErrorSeApiNotInitialized, 
                            ErrorParameterMismatch;

    /**
     * Exports the transaction log messages, containing the process and protocol
     * data, that correspond to a certain transaction. Additionally, the function
     * SHALL export all system log messages and audit log messages whose signature
     * counters are contained in the following interval: 
     *     Signature counter of the transaction log message for the start of the transaction 
     *     and the signature counter of the transaction log message for the end of the transaction
     *     (inclusive). 
     * Furthermore, additional files that are needed to verify the signatures, included in the log messages, 
     * are exported.
     * 
     * @param transactionNumber
     *            indicates the transaction whose corresponding log messages are
     *            relevant for the export [INPUT PARAMETER, REQUIRED]
     * @param exportedData
     *            selected log messages and additional files needed to verify the
     *            signatures included in the log messages [OUTPUT PARAMETER,
     *            REQUIRED]
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned
     * @throws ErrorTransactionNumberNotFound
     *             no data has been found for the provided transactionNumber
     * @throws ErrorSeApiNotInitialized
     *             the SE API has not been initialized
     * @throws ErrorParameterMismatch 
     * 				mismatch in parameters of the function
     */
    short exportData(long transactionNumber, 
                     ByteArrayHolder exportedData)
                     throws ErrorTransactionNumberNotFound, 
                            ErrorSeApiNotInitialized, 
                            ErrorParameterMismatch;

    /**
     * Exports the transaction log messages, containing the process and protocol
     * data, that are relevant for a certain interval of transactions. Additionally,
     * the function SHALL export all system log messages and audit log messages
     * whose signature counters are contained in this interval. Furthermore,
     * additional files that are needed to verify the signatures, included in the
     * log messages, are exported
     * 
     * @param startTransactionNumber
     *            defines the transaction number (inclusive) regarding the start of
     *            the interval of relevant log messages [INPUT PARAMETER, REQUIRED]
     * @param endTransactionNumber
     *            defines the transaction number (inclusive) regarding the end of
     *            the interval of relevant log messages [INPUT PARAMETER, REQUIRED]
     * @param maximumNumberRecords
     *            if the value of this parameter is not 0, the function SHALL only
     *            return the log messages if the number of relevant records is less
     *            or equal to the number of maximum records. If the value of the
     *            parameter is 0, the function SHALL return all selected log
     *            messages [INPUT PARAMETER, REQUIRED]
     * @param exportedData
     *            selected log messages and additional files needed to verify the
     *            signatures included in the log messages [OUTPUT PARAMETER,
     *            REQUIRED]
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned
     * @throws ErrorParameterMismatch
     *             mismatch in parameters of the function
     * @throws ErrorTransactionNumberNotFound
     *             no data has been found for the provided transaction numbers
     * @throws ErrorTooManyRecords
     *             the amount of requested records exceeds the parameter
     *             maximumNumberRecords
     * @throws ErrorSeApiNotInitialized
     *             the SE API has not been initialized
     */
    short exportData(long startTransactionNumber, 
                     long endTransactionNumber, 
                     int maximumNumberRecords,
                     ByteArrayHolder exportedData) 
                     throws ErrorParameterMismatch, 
                            ErrorTransactionNumberNotFound,
                            ErrorTooManyRecords, 
                            ErrorSeApiNotInitialized;

    /**
     * Exports the transaction log messages, containing the process and protocol
     * data, that are relevant for a certain interval of transactions. The
     * transaction log messages in this interval SHALL correspond to the passed
     * clientId. Additionally, the function SHALL export all system log messages and
     * audit log messages whose signature counters are contained in the interval.
     * Furthermore, additional files that are needed to verify the signatures,
     * included in the log messages, are exported
     * 
     * @param startTransactionNumber
     *            defines the transaction number (inclusive) regarding the start of
     *            the interval of relevant log messages [INPUT PARAMETER, REQUIRED]
     * @param endTransactionNumber
     *            defines the transaction number (inclusive) regarding the end of
     *            the interval of relevant log messages [INPUT PARAMETER, REQUIRED]
     * @param clientId
     *            ID of a client application that has used the API to log
     *            transactions. Only transaction log messages that corresponds to
     *            the clientId are relevant for the export [INPUT PARAMETER,
     *            REQUIRED]
     * @param maximumNumberRecords
     *            if the value of this parameter is not 0, the function SHALL only
     *            return the log messages if the number of relevant records is less
     *            or equal to the number of maximum records. If the value of the
     *            parameter is 0, the function SHALL return all selected log
     *            messages [INPUT PARAMETER, REQUIRED]
     * @param exportedData
     *            selected log messages and additional files needed to verify the
     *            signatures included in the log messages [OUTPUT PARAMETER,
     *            REQUIRED]
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned
     * @throws ErrorParameterMismatch
     *             mismatch in parameters of the function
     * @throws ErrorTransactionNumberNotFound
     *             no data has been found for the provided transaction numbers
     * @throws ErrorIdNotFound
     *             no data has been found for the provided clientId
     * @throws ErrorTooManyRecords
     *             the amount of requested records exceeds the parameter
     *             maximumNumberRecords
     * @throws ErrorSeApiNotInitialized
     *             the SE API has not been initialized
     */
    short exportData(long startTransactionNumber, 
                     long endTransactionNumber, 
                     String clientId, 
                     int maximumNumberRecords,
                     ByteArrayHolder exportedData) 
                     throws ErrorParameterMismatch, 
                            ErrorTransactionNumberNotFound,
                            ErrorIdNotFound, 
                            ErrorTooManyRecords, 
                            ErrorSeApiNotInitialized;

    /**
     * Exports the transaction log messages, system log messages and audit log
     * messages that have been created in a certain period of time. Furthermore,
     * additional files that are needed to verify the signatures included in the log
     * messages are exported.
     * 
     * @param startDate
     *            defines the starting time (inclusive) for the period in that the
     *            relevant log messages have been created. The value for the
     *            parameter SHALL be encoded in a format that conforms to BSI
     *            TR-03151. 
     *            If a value for the input parameter endDate is passed, startDate SHALL be 
     *            [INPUT PARAMETER, OPTIONAL]. 
     *            If no value for the input parameter endDate is passed, startDate SHALL be 
     *            [INPUT PARAMETER, REQUIRED].
     * @param endDate
     *            defines the end time (inclusive) for the period in that relevant
     *            log messages have been created. The value for the parameter SHALL
     *            be encoded in a format that conforms to BSI TR-03151. 
     *            If a value for the input parameter startDate is passed, endDate SHALL be
     *            [INPUT PARAMETER, OPTIONAL]. 
     *            If no value for the input parameter startDate is passed, endDate SHALL be 
     *            [INPUT PARAMETER, REQUIRED].
     * @param maximumNumberRecords
     *            if the value of this parameter is not 0, the function SHALL only
     *            return the log messages if the number of relevant records is less
     *            or equal to the number of maximum records. If the value of the
     *            parameter is 0, the function SHALL return all selected log
     *            messages [INPUT PARAMETER, REQUIRED]
     * @param exportedData
     *            selected log messages and additional files needed to verify the
     *            signatures included in the log messages [OUTPUT PARAMETER,
     *            REQUIRED]
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned
     * @throws ErrorParameterMismatch
     *             mismatch in parameters of the function
     * @throws ErrorNoDataAvailable
     *             no data has been found for the provided selection
     * @throws ErrorTooManyRecords
     *             the amount of requested records exceeds the parameter
     *             maximumNumberRecords
     * @throws ErrorSeApiNotInitialized
     *             the SE API has not been initialized
     */
    short exportData(ZonedDateTime startDate, 
                     ZonedDateTime endDate, 
                     int maximumNumberRecords,
                     ByteArrayHolder exportedData)
                     throws ErrorParameterMismatch, 
                            ErrorNoDataAvailable, 
                            ErrorTooManyRecords, 
                            ErrorSeApiNotInitialized;

    /**
     * Exports the transaction log messages, system log messages and audit log
     * messages that have been created in a certain period of time. The transaction
     * log messages in this period of time SHALL correspond to the passed clientId.
     * Furthermore, additional files that are needed to verify the signatures
     * included in the log messages are exported
     * 
     * @param startDate
     *            defines the starting time (inclusive) for the period in that the
     *            relevant log messages have been created. The value for the
     *            parameter SHALL be encoded in a format that conforms to BSI
     *            TR-03151. 
     *            If a value for the input parameter endDate is passed, startDate SHALL be 
     *            [INPUT PARAMETER, OPTIONAL]. 
     *            If no value for the input parameter endDate is passed, startDate SHALL be 
     *            [INPUT PARAMETER, REQUIRED].
     * @param endDate
     *            defines the end time (inclusive) for the period in that relevant
     *            log messages have been created. The value for the parameter SHALL
     *            be encoded in a format that conforms to BSI TR-03151. If a value
     *            for the input parameter startDate is passed, endDate SHALL be
     *            [INPUT PARAMETER, OPTIONAL]. If no value for the input parameter
     *            startDate is passed, endDate SHALL be [INPUT PARAMETER, REQUIRED].
     * @param clientId
     *            ID of a client application that has used the API to log
     *            transactions Only transaction log messages that corresponds to the
     *            clientId are relevant for the export [INPUT PARAMETER, REQUIRED]
     * @param maximumNumberRecords
     *            if the value of this parameter is not 0, the function SHALL only
     *            return the log messages if the number of relevant records is less
     *            or equal to the number of maximum records. If the value of the
     *            parameter is 0, the function SHALL return all selected log
     *            messages [INPUT PARAMETER, REQUIRED]
     * @param exportedData
     *            selected log messages and additional files needed to verify the
     *            signatures included in the log messages [OUTPUT PARAMETER,
     *            REQUIRED]
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned
     * @throws ErrorParameterMismatch
     *             mismatch in parameters of the function
     * @throws ErrorNoDataAvailable
     *             no data has been found for the provided selection
     * @throws ErrorIdNotFound
     *             no data has been found for the provided clientId
     * @throws ErrorTooManyRecords
     *             the amount of requested records exceeds the parameter
     *             maximumNumberRecords
     * @throws ErrorSeApiNotInitialized
     *             the SE API has not been initialized
     */
    short exportData(ZonedDateTime startDate, 
                     ZonedDateTime endDate, 
                     String clientId, 
                     int maximumNumberRecords,
                     ByteArrayHolder exportedData) 
                     throws ErrorParameterMismatch, 
                            ErrorNoDataAvailable, 
                            ErrorIdNotFound,
                            ErrorTooManyRecords, 
                            ErrorSeApiNotInitialized;

    /**
     * Exports all stored transaction log messages, system log message and audit log
     * messages. Furthermore, additional files that are needed to verify the
     * signatures included in the log messages are exported.
     * 
     * @param maximumNumberRecords
     *            if the value of this parameter is not 0, the function SHALL only
     *            return the log messages if the number of relevant records is less
     *            or equal to the number of maximum records. If the value of the
     *            parameter is 0, the function SHALL return all stored log messages
     *            [INPUT PARAMETER, REQUIRED]
     * @param exportedData
     *            all stored log messages and additional files needed to verify the
     *            signatures included in the log messages [OUTPUT PARAMETER,
     *            REQUIRED]
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned
     * @throws ErrorTooManyRecords
     *             the amount of requested records exceeds the parameter
     *             maximumNumberRecords
     * @throws ErrorSeApiNotInitialized
     *             the SE API has not been initialized
     * @throws ErrorParameterMismatch 
     * 				mismatch in the parameters of the function
     */
    short exportData(int maximumNumberRecords, 
                     ByteArrayHolder exportedData)
                     throws ErrorTooManyRecords, 
                            ErrorSeApiNotInitialized, 
                            ErrorParameterMismatch;

    /**
     * exports the certificates of the certificate chains. These certificates belong
     * to the public keys of the key pairs that are used for the creation of
     * signature values in log messages
     * 
     * @param certificates
     *            the TAR archive that contains all certificates that are necessary
     *            for the verification of log messages. The format of the TAR
     *            archive and the contained certificates SHALL conform to BSI
     *            TR-03151 [OUTPUT PARAMETER, REQUIRED]
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned
     * @throws ErrorExportCertFailed
     *             the collection of the certificates for the export failed
     * @throws ErrorSeApiNotInitialized
     *             the SE API has not been initialized
     */
    short exportCertificates(ByteArrayHolder certificates) 
                             throws ErrorExportCertFailed, 
                                    ErrorSeApiNotInitialized;

    /**
     * Restores a backup in the SE API and storage. The backup data includes log
     * messages and certificates that have been exported by using the exportData
     * function. Log messages and certificates are passed in the TAR archive that
     * has been returned during the export of the log messages and certificates. The
     * function SHALL store the data of the passed log messages in the storage. If
     * an imported log message has a file name that already exists in the storage, a
     * counter SHALL be appended to the file name of the imported log message. The
     * function SHALL store an imported certificate only if no certificate of the
     * same name is managed by the SE API.
     * 
     * @param restoreData
     *            represents the TAR archive that contains the log messages and
     *            certificates for the restore process [INPUT PARAMETER, REQUIRED]
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned
     * @throws ErrorRestoreFailed
     *             the restore process has failed
     * @throws ErrorSeApiNotInitialized
     *             the SE API has not been initialized
     * @throws ErrorUserNotAuthorized
     *             the user who has invoked the function restoreFromBackup is not
     *             authorized to execute this function
     * @throws ErrorUserNotAuthenticated
     *             the user who has invoked the function restoreFromBackup has not
     *             the status authenticated
     */
    short restoreFromBackup(byte[] restoreData)
                            throws ErrorRestoreFailed, 
                                   ErrorSeApiNotInitialized, 
                                   ErrorUserNotAuthorized, 
                                   ErrorUserNotAuthenticated;

    /**
     * Reads a log message that bases on the last log message parts that have been
     * produced and processed by the Secure Element
     * 
     * @param logMessage
     *            contains the last log message that the Secure Element has produced
     *            [OUTPUT PARAMETER, REQUIRED]
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned
     * @throws ErrorNoLogMessage
     *             no log message parts have been found
     * @throws ErrorReadingLogMessage
     *             error while retrieving log message parts
     * @throws ErrorSeApiNotInitialized
     *             the SE API has not been initialized
     * @throws ErrorSecureElementDisabled
     *             the Secure Element has been disabled
     * @throws ErrorParameterMismatch
     * 				mismatch in parameters of the function    
     */
    short readLogMessage(ByteArrayHolder logMessage)
                         throws ErrorNoLogMessage, 
                                ErrorReadingLogMessage, 
                                ErrorSeApiNotInitialized, 
                                ErrorSecureElementDisabled,
                                ErrorParameterMismatch;

    /**
     * Exports the serial number(s) of the SE API. A serial number is a hash value
     * of a public key that belongs to a key pair whose private key is used to
     * create signature values of log messages.
     * 
     * @param serialNumbers
     *            the serial number(s) of the SE API. The serial number(s) SHALL be
     *            encoded in the TLV structure defined in BSI TR-03151. 
     *            [OUTPUT PARAMETER, REQUIRED]
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned
     * @throws ErrorExportSerialNumbersFailed
     *             the collection of the serial number(s) failed
     * @throws ErrorSeApiNotInitialized
     *             the SE API has not been initialized
     */
    short exportSerialNumbers(ByteArrayHolder serialNumbers)
                              throws ErrorExportSerialNumbersFailed, 
                                     ErrorSeApiNotInitialized;

    /**
     * Supplies the maximal number of clients that can use the functionality to log
     * transactions of the SE API simultaneously
     * 
     * @param maxNumberClients
     *            maximum number of clients that can use the functionality to log
     *            transactions of the SE API simultaneously [OUTPUT PARAMETER,
     *            REQUIRED]
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned
     * @throws ErrorGetMaxNumberOfClientsFailed
     *             the determination of the maximum number of clients that could use
     *             the SE API simultaneously failed
     * @throws ErrorSeApiNotInitialized
     *             the SE API has not been initialized
     * @throws ErrorSecureElementDisabled
     *             the Secure Element has been disabled
     */
    short getMaxNumberOfClients(LongHolder maxNumberClients)
                                throws ErrorGetMaxNumberOfClientsFailed, 
                                       ErrorSeApiNotInitialized, 
                                       ErrorSecureElementDisabled;

    /**
     * Supplies the number of clients that are currently using the functionality to
     * log transactions of the SE API.
     * 
     * @param currentNumberClients
     *            the number of clients that are currently using the functionality
     *            of the SE API [OUTPUT PARAMETER, REQUIRED]
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned
     * @throws ErrorGetCurrentNumberOfClientsFailed
     *             the determination of the current number of clients using the SE
     *             API failed
     * @throws ErrorSeApiNotInitialized
     *             the SE API has not been initialized
     * @throws ErrorSecureElementDisabled
     *             the Secure Element has been disabled
     */
    short getCurrentNumberOfClients(LongHolder currentNumberClients)
                                    throws ErrorGetCurrentNumberOfClientsFailed, 
                                           ErrorSeApiNotInitialized, 
                                           ErrorSecureElementDisabled;

    /**
     * Supplies the maximal number of simultaneously opened transactions that can be
     * managed by the SE API
     * 
     * @param maxNumberTransactions
     *            maximum number of simultaneously opened transactions that can be
     *            managed by the SE API [OUTPUT PARAMETER, REQUIRED]
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned
     * @throws ErrorGetMaxNumberTransactionsFailed
     *             the determination of the maximum number of transactions that can
     *             be managed simultaneously failed
     * @throws ErrorSeApiNotInitialized
     *             the SE API has not been initialized
     * @throws ErrorSecureElementDisabled
     *             the Secure Element has been disabled
     */
    short getMaxNumberOfTransactions(LongHolder maxNumberTransactions)
                                     throws ErrorGetMaxNumberTransactionsFailed, 
                                            ErrorSeApiNotInitialized, 
                                            ErrorSecureElementDisabled;

    /**
     * Supplies the number of open transactions that are currently managed by the SE
     * API
     * 
     * @param currentNumberTransactions
     *            the number of open transactions that are currently managed by the
     *            SE API [OUTPUT PARAMETER, REQUIRED]
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned
     * @throws ErrorGetCurrentNumberOfTransactionsFailed
     *             the determination of the number of open transactions that are
     *             currently managed by the SE API failed
     * @throws ErrorSeApiNotInitialized
     *             the SE API has not been initialized
     * @throws ErrorSecureElementDisabled
     *             the Secure Element has been disabled
     */
    short getCurrentNumberOfTransactions(LongHolder currentNumberTransactions)
                                         throws ErrorGetCurrentNumberOfTransactionsFailed, 
                                                ErrorSeApiNotInitialized, 
                                                ErrorSecureElementDisabled;

    /**
     * Supplies the supported variants to update transactions
     * 
     * @param supportedUpdateVariants
     *            the supported variant(s) to update a transaction 
     *            [OUTPUT PARAMETER, REQUIRED]
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned
     * @throws ErrorGetSupportedUpdateVariantsFailed
     *             the identification of the supported variant(s) to update
     *             transactions failed
     * @throws ErrorSeApiNotInitialized
     *             the SE API has not been initialized
     * @throws ErrorSecureElementDisabled
     *             the Secure Element has been disabled
     */
    short getSupportedTransactionUpdateVariants(UpdateVariantsHolder supportedUpdateVariants)
                                                throws ErrorGetSupportedUpdateVariantsFailed, 
                                                       ErrorSeApiNotInitialized, 
                                                       ErrorSecureElementDisabled;

    /**
     * Deletes all data that is stored in the storage. The function SHALL delete
     * only data that has been exported.
     * 
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned
     * @throws ErrorDeleteStoredDataFailed
     *             the deletion of the data from the storage failed
     * @throws ErrorUnexportedStoredData
     *             the deletion of data from the storage failed because the storage
     *             contains data that has not been exported
     * @throws ErrorSeApiNotInitialized
     *             the SE API has not been initialized
     * @throws ErrorUserNotAuthorized
     *             the user who has invoked the function deleteStoredData is not
     *             authorized to execute this function
     * @throws ErrorUserNotAuthenticated
     *             the user who has invoked the function deleteStoredData has not
     *             the status authenticated
     */
    short deleteStoredData() throws ErrorDeleteStoredDataFailed, 
                                    ErrorUnexportedStoredData, 
                                    ErrorSeApiNotInitialized,
                                    ErrorUserNotAuthorized, 
                                    ErrorUserNotAuthenticated;

    /**
     * Supplies the supported variants to update the current date/time
     * 
     * @param supportedSyncVariant
     *            the supported variant to update the current date/time 
     *            [OUTPUT PARAMETER, REQUIRED]
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned
     * @throws ErrorGetTimeSyncVariantFailed
     *             the identification of the supported variant to update
     *             the current date/time failed
     * @throws ErrorSeApiNotInitialized
     *             the SE API has not been initialized
     * @throws ErrorSecureElementDisabled
     *             the Secure Element has been disabled
     */
    short getTimeSyncVariant (SyncVariantsHolder supportedSyncVariant)
                                                throws ErrorGetTimeSyncVariantFailed, 
                                                       ErrorSeApiNotInitialized, 
                                                       ErrorSecureElementDisabled;
    /**
     * Enables an authorized user or application to authenticate to the SE API for
     * the usage of restricted SE API functions
     * 
     * @param userId
     *            the ID of the user who or application that wants to be
     *            authenticated [INPUT PARAMETER, REQUIRED]
     * @param pin
     *            the PIN for the authentication [INPUT PARAMETER, REQUIRED]
     * @param authenticationResult
     *            the result of the authentication [OUTPUT PARAMETER, REQUIRED]
     * @param remainingRetries
     *            the number of remaining retries to enter a PIN [OUTPUT PARAMETER,
     *            REQUIRED]
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned. If the authentication attempt
     *         has failed, the return value AUTHENTICATION_FAILED SHALL be returned.
     * @throws ErrorSigningSystemOperationDataFailed
     *             the determination of the log message parts for the system
     *             operation data by the Secure Element failed
     * @throws ErrorRetrieveLogMessageFailed
     *             the execution of the Secure Element functionality to retrieve log
     *             message parts has failed
     * @throws ErrorStorageFailure
     *             storing of the data of the log message failed
     * @throws ErrorSecureElementDisabled
     *             the Secure Element has been disabled
     * 
     * @throws ErrorSignatureCounterOverflow
     * 				the function authenticateUser has been invoked although the value of the 
     * 				signature counter in the CryptoCore module has entered an illegal state
     * @throws ErrorParameterMismatch 
     * 				mismatch in parameters of the function
     */
    short authenticateUser(String userId, 
                           byte[] pin, 
                           AuthenticationResultHolder authenticationResult,
                           ShortHolder remainingRetries) 
                           throws ErrorSigningSystemOperationDataFailed, 
                                  ErrorRetrieveLogMessageFailed,
                                  ErrorStorageFailure, 
                                  ErrorSecureElementDisabled, 
                                  ErrorSignatureCounterOverflow, 
                                  ErrorParameterMismatch;

    /**
     * Enables the log out of an authenticated user or application from the SE API
     * 
     * @param userId
     *            the ID of the user who or application that wants to log out from
     *            the SE API [INPUT PARAMETER, REQUIRED]
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned
     * @throws ErrorUserIdNotManaged
     *             the passed userId is not managed by the SE API
     * @throws ErrorSigningSystemOperationDataFailed
     *             the determination of the log message parts for the system
     *             operation data by the Secure Element failed
     * @throws ErrorUserIdNotAuthenticated
     *             the passed userId has not the status authenticated
     * @throws ErrorRetrieveLogMessageFailed
     *             the execution of the Secure Element functionality to retrieve log
     *             message parts has failed
     * @throws ErrorStorageFailure
     *             storing of the data of the log message failed
     * @throws ErrorSecureElementDisabled
     *             the Secure Element has been disabled
     *             
     * @throws ErrorSignatureCounterOverflow
     * 				the function logOut has been invoked although the value of the 
     * 				signature counter in the CryptoCore module has entered an illegal state            
     */
    short logOut(String userId) 
                 throws ErrorUserIdNotManaged, 
                        ErrorSigningSystemOperationDataFailed,
                        ErrorUserIdNotAuthenticated, 
                        ErrorRetrieveLogMessageFailed, 
                        ErrorStorageFailure, 
                        ErrorSecureElementDisabled,
                        ErrorSignatureCounterOverflow;

    /**
     * Enables the unblocking for the entry of a PIN and the definition of a new PIN
     * for the authentication of authorized users or applications
     * 
     * @param userId
     *            the ID of the user who or application that wants to unblock the
     *            corresponding PIN entry [INPUT PARAMETER, REQUIRED]
     * @param puk
     *            the PUK of the user/application [INPUT PARAMETER, REQUIRED]
     * @param newPin
     *            the new PIN for the user/application [INPUT PARAMETER, REQUIRED]
     * @param unblockResult
     *            the result of the unblock procedure [OUTPUT PARAMETER, REQUIRED]
     * @return if the execution of the function has been successful, the return
     *         value EXECUTION_OK SHALL be returned. If the execution of attempt to
     *         unblock a PIN entry has failed, the return value UNBLOCK_FAILED SHALL
     *         be returned.
     * @throws ErrorSigningSystemOperationDataFailed
     *             the determination of the log message parts for the system
     *             operation data by the Secure Element failed
     * @throws ErrorRetrieveLogMessageFailed
     *             the execution of the Secure Element functionality to retrieve log
     *             message parts has failed
     * @throws ErrorStorageFailure
     *             storing of the data of the log message failed
     * @throws ErrorSecureElementDisabled
     *             the Secure Element has already been disabled
     *             
     * @throws ErrorSignatureCounterOverflow
     * 				the function unblockUser has been invoked although the value of the 
     * 				signature counter in the CryptoCore module has entered an illegal state            
     * @throws ErrorParameterMismatch 
     * 				mismatch in parameters of function
     */
    short unblockUser(String userId, 
                      byte[] puk, 
                      byte[] newPin, 
                      UnblockResultHolder unblockResult)
                      throws ErrorSigningSystemOperationDataFailed, 
                             ErrorRetrieveLogMessageFailed, 
                             ErrorStorageFailure,
                             ErrorSecureElementDisabled,
                             ErrorSignatureCounterOverflow, 
                             ErrorParameterMismatch;

}
