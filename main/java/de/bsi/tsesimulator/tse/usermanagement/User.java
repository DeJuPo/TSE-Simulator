/**
 * 
 */
package main.java.de.bsi.tsesimulator.tse.usermanagement;

import java.io.Serializable;

import org.bouncycastle.util.Arrays;

import main.java.de.bsi.tsesimulator.tlv.TLVUtility;

/**
 * Represents a user with his userID, his role, his PIN & PUK and the number of retries he has for logging in.
 * The PIN & PUK have to be provided as hashed PIN & PUK for this class and it is the responsibility of the calling class to
 * ensure PIN & PUK are hashed and not stored in plain text.
 * @author dpottkaemper
 * @since 1.0
 * @version 1.5
 */
public class User implements Serializable{

	private static final long serialVersionUID = -7727797749421401633L;
	private String userid;
	private String role;
	private byte[] hashedpin;					//should not be stored in plaintext
	private byte[] hashedpuk;					//should not be stored in plaintext
	private short remainingPINRetries;
	private short remainingPUKRetries;
	
	/**
	 * private constructor for the sake of only persisting complete users
	 */
	@SuppressWarnings("unused")
	private User() {
	}
	
	
	/**
	 * Normally used constructor for a user.
	 * @param id name of the user or application
	 * @param role either Admin or TimeAdmin. This determines what the user can and cannot do
	 * @param hashedPin the hashed PIN of the user
	 * @param hashedPuk the hashed PUK of the user
	 * @param remainingPINRetries how many retries that user still has for their PIN entry
	 * @param remainingPUKRetries how many retries that user still has for their PUK entry
	 */
	public User(String id, String role, byte[] hashedPin, byte[] hashedPuk, short remainingPINRetries, short remainingPUKRetries) {
		this.userid = id;
		this.role = role;
		this.hashedpin = hashedPin;
		this.hashedpuk = hashedPuk;
		this.remainingPINRetries = remainingPINRetries;
		this.remainingPUKRetries = remainingPUKRetries;
	}
	
//----------------------------------------------------------GETTERS-------------------------------------------------------------
	public String getUserID() {
		return this.userid;
	}
	
	public String getRole() {
		return this.role;
	}
	
	public byte[] getHashedPUK() {
		return this.hashedpuk;
	}
	
	public byte[] getHashedPIN() {
		return this.hashedpin;
	}
	
	public short getRemainingPINRetries() {
		return this.remainingPINRetries;
	}
	
	public short getRemainingPUKRetries() {
		return this.remainingPUKRetries;
	}

	/**
	 * The toString method for a user. It lists all contents of this user and gives them back as a pretty, readable String.
	 */
	@Override
	public String toString() {
		StringBuilder userStringBuilder = new StringBuilder("UserID:\t\t\t" +this.userid +"\n");
		userStringBuilder.append("Role:\t\t\t" +this.role +"\nHashed PIN:\t\t" +TLVUtility.byteArrayToString(hashedpin) +"\n");
		userStringBuilder.append("Hashed PUK:\t\t" +TLVUtility.byteArrayToString(hashedpuk) +"\nRemaining PIN Retries:\t" +remainingPINRetries +"\n");
		userStringBuilder.append("Remaining PUK Retries:\t" +remainingPUKRetries +"\n");
		return userStringBuilder.toString();
	}
	
	/**
	 * Implementation of the equals method for users. Two users are the same, if they have the same user ID, the same role, the same PIN and the same PUK.
	 * The number of retries on either of them does not matter.  
	 * @param u the user to be compared to this
	 * @return true, if they are equal. False otherwise.
	 */
	public boolean equals(User u) {
		boolean areEqual = false;
		boolean uidSame = this.userid.equals(u.userid);
		boolean roleSame = this.role.equals(u.role);
		boolean pinHashesSame = Arrays.areEqual(this.hashedpin, u.hashedpin);
		boolean pukHashesSame = Arrays.areEqual(this.hashedpuk, u.hashedpuk);
		areEqual = (uidSame && roleSame && pinHashesSame && pukHashesSame);
		
		return areEqual;
	}

}
