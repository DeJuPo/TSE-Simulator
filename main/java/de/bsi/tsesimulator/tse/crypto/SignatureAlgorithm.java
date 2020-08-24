/**
 * 
 */
package main.java.de.bsi.tsesimulator.tse.crypto;

import org.bouncycastle.jce.interfaces.ECPublicKey;

import main.java.de.bsi.tsesimulator.exceptions.ECCException;
import main.java.de.bsi.tsesimulator.exceptions.TR_03111_ECC_V2_1_Exception;

/**
 * The abstract class that makes it possible for the CryptoCore to use a signature algorithm regardless of the question <b>which</b> signature algorithm is 
 * really used. It requires each implementation of a signature algorithm to have a sign and a verify method. Additionally, each signature algorithm
 * has to provide a method to get the public key from it and a method to get information on the algorithm.
 * @author dpottkaemper
 * @since 1.0
 *
 */
public abstract class SignatureAlgorithm {

	/**
	 * an abstract algorithm class that makes sure that the CryptoCore can either use an ECDSA or an ECSDSA
	 */
	public SignatureAlgorithm() {}
	
	public abstract byte[] sign(byte[] toBeSigned) throws TR_03111_ECC_V2_1_Exception;
	
	public abstract boolean verify(byte[] signature, byte[] notSignedValue) throws ECCException, TR_03111_ECC_V2_1_Exception;
	
	public abstract ECPublicKey getPublicKey();
	
	public abstract String getAlgorithmDefinition();

}
