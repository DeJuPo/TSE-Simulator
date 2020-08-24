/**
 * 
 */
package main.java.de.bsi.tsesimulator.tse.storage;

import java.io.Serializable;

/**
 * Used to save all data that is not a log message or belongs to user management via the Java Serialization. Is used by the {@linkplain PersistentStorage}
 * to store and load the saved values into the TSE.
 * @author dpottkaemper
 * @since 1.0
 * @version 1.5
 */
public class PersistedValues implements Serializable{
	/**
	 * Generated serial version UID. Is of version 1.5 of the TSE Simulator.
	 */
	private static final long serialVersionUID = 2663377102783268575L;
	
	
	private long cryptoCoreClockStatus;
	private boolean tseIsInitialized;
	private boolean seIsDisabled;
	private long signatureCounterStatus;
	private long transactionNumberStatus;
	private String descriptionOfTheSEAPI;
	
	/**
	 * private constructor for the sake of only persisting complete sets of values
	 */
	@SuppressWarnings("unused")
	private PersistedValues() {
	}
	
	/**
	 * Constructor which stores all relevant data in the instance variables.
	 * @param clockStatus the time of the clock inside the CryptoCore at the time of the method call. The time has to be a long in unix time.
	 * @param initialized the initialization status of the TSE. True if it has been initialized.
	 * @param disabled the state of the SE API, false if the SE API has not been disabled.
	 * @param signatureCounterStatus the signature counter in the CryptoCore at the time of the method call.
	 * @param transactionNumberStatus the transaction counter of the ERSSpecificModule at the time of the method call.
	 * @since 1.0
	 * @version 1.5
	 */
	public PersistedValues(long clockStatus, boolean initialized, boolean disabled, long signatureCounterStatus, long transactionNumberStatus, String descriptionOfTheSEAPI) {
		this.cryptoCoreClockStatus = clockStatus;
		this.tseIsInitialized = initialized;
		this.seIsDisabled = disabled;
		this.signatureCounterStatus = signatureCounterStatus;
		this.transactionNumberStatus = transactionNumberStatus;
		this.descriptionOfTheSEAPI = descriptionOfTheSEAPI;
	}
	
//---------------------------------------------------getter methods for the values------------------------------------------------------------------
	/**
	 * Getter for the saved clock status.
	 * @return the saved time as a long.
	 * @since 1.0
	 */
	public long getCryptoCoreClockStatus() {
		return this.cryptoCoreClockStatus;
	}

	/**
	 * Getter for the saved state of the TSE.
	 * @return whether the tse is initialized or not.
	 * @since 1.0
	 */
	public boolean getTseIsInitialized() {
		return tseIsInitialized;
	}

	/**
	 * Getter method for the saved state of the SE API.
	 * @return whether the SE API has been disabled.
	 * @since 1.0
	 */
	public boolean getSeIsDisabled() {
		return seIsDisabled;
	}

	/**
	 * Getter method for the saved signature counter of the CryptoCore.
	 * @return the saved signature counter.
	 * @since 1.0
	 */
	public long getSignatureCounterStatus() {
		return signatureCounterStatus;
	}

	/**
	 * Getter method for the saved transaction number of the ERSSpecificModule.
	 * @return the saved transaction number of the ERSSPecificModule.
	 * @since 1.0
	 */
	public long getTransactionNumberStatus() {
		return transactionNumberStatus;
	}
	
	/**
	 * Getter method for the saved description of the SE API of the TSEController.
	 * @return the saved description of the SE API of the TSEController
	 * @since 1.5
	 */
	public String getDescriptionOfTheSEAPI() {
		return descriptionOfTheSEAPI;
	}
}
