/**
 * 
 */
package main.java.de.bsi.tsesimulator.tse.crypto;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;

import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import main.java.de.bsi.tsesimulator.constants.ConfigConstants;
import main.java.de.bsi.tsesimulator.exceptions.ECCException;
import main.java.de.bsi.tsesimulator.exceptions.LoadingFailedException;
import main.java.de.bsi.tsesimulator.exceptions.TR_03111_ECC_V2_1_Exception;
import main.java.de.bsi.tsesimulator.preferences.PropertyValues;
import main.java.de.bsi.tsesimulator.tse.CryptoCore;
import main.java.de.bsi.tsesimulator.utils.TR_03111_Utils;
import main.java.de.bsi.tsesimulator.utils.Utils;


/**
 * Implements the ECSDSA (Elliptic Curve based Schnorr Digital Signature Algorithm) according to BSI TR-03111.
 * <br>
 * Used by {@linkplain CryptoCore}
 * @author dpottkaemper
 * @since 1.0
 * @version 1.5
 */
public class ECSDSA extends SignatureAlgorithm{
	
	private ECPrivateKey privateKey;
	private ECPublicKey publicKey;
	private String hashFunction;						//Stores what hash function shall be used
	private String curveName;							
	private ECParameterSpec ecSpec;			 			//save the specs from the chosen named curve for easy access to G, n and H
	
	
	/**
	 * The new default constructor for ECSDSA objects that gets called by {@linkplain CryptoCore} if ECSDSA is the desired {@linkplain SignatureAlgorithm}.
	 * This method relies on the file config.properties to determine the format of the private key and the directory this file is stored in. 
	 * It fetches the private key and uses it to calculate the public key. Then it saves both values for later use in 
	 * {@linkplain #sign(byte[])} and {@linkplain #verify(byte[], byte[])}. 
	 * @since 1.4
	 * @throws IOException if reading the private key file fails
	 * @version 1.5
	 */
	public ECSDSA() throws IOException {
		//add the BouncyCastle Provider
		Security.addProvider(new BouncyCastleProvider());
		//store curve name and curve specifications for later use
		curveName = PropertyValues.getInstance().getValue(ConfigConstants.CFG_TAG_CURVE);
		ecSpec = ECNamedCurveTable.getParameterSpec(curveName);
		
		//the correct file name for the private key has to be present in config.properties
		//PropertyValues class has path to resource directory, can build the path to privateKey from that resource directory and the entries in config.properties
		String privKeyFullPath = PropertyValues.getInstance().getPathToPrivateKey();
		String privKeyEncoding = PropertyValues.getInstance().getValue(ConfigConstants.CFG_TAG_PRIV_KEY_ENCODING);
		
		//change behavior depending on the encoding:
		if(privKeyEncoding.equalsIgnoreCase("DER")) {
			//1. find the file with the private key and get a reader on that file
			File privKeyFile = new File(privKeyFullPath);
			Path path = Paths.get(privKeyFile.toURI());			//.toURI necessary, otherwise Java does not find the file
			
			//read all bytes as binary information into a byte array
			byte[] privKeyCompleteByteArray = Files.readAllBytes(path); 
			
			//2. do not parse the asn1 DER encoded key, because there seems to be no method to create an ECPrivateKey directly from a BigInteger
				//instead, use KeyFactory.generatePrivate and KeyFactory.generatePublic
			try {
				KeyFactory ecKeyFactory = KeyFactory.getInstance("ECDSA", "BC");
				//how to get the public and the private key:	
					//for private keys use PKCS8EncodedKeySpec; for public keys use X509EncodedKeySpec
					//PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(keyBytes);
					//PrivateKey pk = kf.generatePrivate(ks);
				//a conversion from OpenSSL DER EC Key into PKCS8 format is necessary, otherwise, the key can not be read this way:
				PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(privKeyCompleteByteArray);
				
				privateKey = (ECPrivateKey) ecKeyFactory.generatePrivate(privKeySpec);
				
				//calculate public point W (aka Pa in TR-03111) to use it to create a java.security.ECPublicKeySpec and use that to assign the ECPublicKey
					//Pa = dA*G
				BigInteger privateKeyInteger = privateKey.getD();				 	//privateKeyInteger = privateKey dA
				ECPoint generatorG = ecSpec.getG();									//domain parameter G
				generatorG = generatorG.normalize();								
				
				ECPoint Pa = generatorG.multiply(privateKeyInteger);
				Pa = Pa.normalize();
				
				org.bouncycastle.jce.spec.ECPublicKeySpec pubKeySpec = new org.bouncycastle.jce.spec.ECPublicKeySpec(Pa, ecSpec);
				
				//use that BC ECPublicKeySpec implements java.security.spec.KeySpec to generate the PublicKey
				publicKey = (ECPublicKey) ecKeyFactory.generatePublic(pubKeySpec);
				
			} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
				System.out.println("Stack trace:\n");
				e.printStackTrace();
				System.out.println("+\n\n");
				throw new LoadingFailedException(e);
			}
			
		}
		if(privKeyEncoding.equalsIgnoreCase("PEM")) {
			//1. find the file with the private key and get a reader on that file
			File privKeyFile = new File(privKeyFullPath);
			
			//2. JcaPEMKeyConverter is needed to convert the PEMKeyPair to a java.security.KeyPair
			JcaPEMKeyConverter keyconverter = new JcaPEMKeyConverter();
			keyconverter.setProvider("BC");
	
			//3. try to extract a PEMKeyPair from the pem file, then convert it to java.security.KeyPair
			try {
				PEMKeyPair keypairPEM = readKeyPair(privKeyFile);
				KeyPair keypair = keyconverter.getKeyPair(keypairPEM);
				
				privateKey = (ECPrivateKey) keypair.getPrivate();
				publicKey = (ECPublicKey) keypair.getPublic();
			} catch (IOException e) {
				throw new IOException(e.getMessage());
			} catch (ECCException e) {
				throw new LoadingFailedException("Unable to load private key: failed to read PEM-encoded key!", e);
			}
		}
		if((!privKeyEncoding.equalsIgnoreCase("DER")) && (!privKeyEncoding.equalsIgnoreCase("PEM")))  {
			//throw an exception because clearly, something went wrong!
			//loading failed is best choice, because it signifies loading something did not work.
			throw new LoadingFailedException("Unable to load private key: unknown format in config.properties!");
		}
		
		//set the hashFunction to easily access it later
		StringBuilder hashNameBuilder = new StringBuilder(PropertyValues.getInstance().getValue(ConfigConstants.CFG_TAG_HASH_METHOD));
		//check if SHA3 is selected, because BC then requires a "SHA3-xyz" String instead of a "SHAxyz"
		if(PropertyValues.getInstance().getValue(ConfigConstants.CFG_TAG_HASH_METHOD).equalsIgnoreCase(ConfigConstants.CFG_ENTRY_HASH_METHOD_SHA3)) {
			hashNameBuilder.append('-');
		}
		
		hashNameBuilder.append(PropertyValues.getInstance().getValue(ConfigConstants.CFG_TAG_HASH_LENGTH));
		hashFunction = hashNameBuilder.toString();
	}
	
	
	
	/**
	 * A little method to help extract a key pair from a pem file. If the private key file that was passed did not 
	 * contain enough information to form a PEMKeyPair, a method extracting the private and the public key separately should be used.
	 * @param privateKey a PEM-file containing both a private key and a public key.
	 * @return the PEMKeyPair extracted from the file.
	 * @throws IOException if some sort of IOError occurred.
	 * @throws ECCException if the file passed could not be decoded to a PEMKeyPair.
	 */
	private static PEMKeyPair readKeyPair(File privateKey) throws IOException, ECCException {
        PEMParser pemParser = new PEMParser(new FileReader(privateKey));
        
        Object PEMkeyPairParsedFromPemFile = null;
        try {
        	PEMkeyPairParsedFromPemFile = pemParser.readObject();
        	if(!(PEMkeyPairParsedFromPemFile instanceof PEMKeyPair)) {
        		throw new ECCException("private key file did not yield a PEMKEyPair like it should.");
        	}
        } catch (IOException e) {
            throw new IOException("The private key could not be decrypted", e);
        } finally {
        	pemParser.close();
        }
        return (PEMKeyPair) PEMkeyPairParsedFromPemFile;
    }
	
	/**
	 * Creates a random BigInteger inside the provided limits. The limits are inclusive.
	 * Could theoretically run into a seemingly endless loop, if every new randomValue is outside of the specified limits.
	 * This is highly unlikely but could happen.
	 * @param lowerLimit the lower boundary of the requested random value (inclusive)
	 * @param upperLimit the upper boundary of the requested random value (inclusive)
	 * @return the randomly generated BigInteger
	 */
	private static BigInteger calculateRandomK(BigInteger lowerLimit, BigInteger upperLimit) {
		//create the first random value
		BigInteger randomValue = new BigInteger(upperLimit.bitLength(), new SecureRandom());
		
		//check if the random value is in the specified bounds
		while((randomValue.compareTo(upperLimit) > 0) || (randomValue.compareTo(lowerLimit)) < 0) {
			randomValue = new BigInteger(upperLimit.bitLength(), new SecureRandom());
		}
		return randomValue;
	}
	
	
	/**
	 * Implements the calculation of an ECSDSA signature according to TR-03111 chapter 4.2.3.1.
	 * Could run into a seemingly endless loop if the conditions ( s == 0 ) or ( r mod n == 0 ) are met too often.
	 * Note: May not be cryptographically safe to use in other applications!
	 * @throws TR_03111_ECC_V2_1_Exception if the bit length of the digest of the chosen hash function is less than the bit length
	 * of the order of the base point of the chosen curve.
	 */
	@Override
	public byte[] sign(byte[] message) throws TR_03111_ECC_V2_1_Exception {
		//define everything that does not change, even if the loop has to be run through again.
		
		BigInteger privateKeyInteger = privateKey.getD();				 	//privateKeyInteger = privateKey dA
		
		BigInteger n = ecSpec.getN();										//domain parameter n
		BigInteger nMinusOne = ecSpec.getN().subtract(BigInteger.ONE); 		//nMinusOne = domain parameter n minus 1
		ECPoint generatorG = ecSpec.getG();									//domain parameter G
		BigInteger p = ecSpec.getCurve().getField().getCharacteristic(); 	//domain parameter p
		
		BigInteger k;														//k = RNG{1, ... , (n-1)}
		ECPoint Q;															//Q = k * generatorG
		
		int tau = ecSpec.getN().bitLength(); 								//tau is the bit length of the order of the base point. tau = log2(n)
		
		BigInteger r;														//r = OS2I (hash tau(FE2OS(xQ) || FE2OS(yQ) || M))
		BigInteger s;														//s = k + r*dA mod n
		
		//create the byte array that will hold the signature value consisting of "r" and "s"
		//signature byte array should be 2*l long where l = roundedUp(log256(n))
		double nAsDouble = n.doubleValue();
		double lAsDouble = (Math.log(nAsDouble)) / Math.log(256);
		lAsDouble = Math.ceil(lAsDouble);
		int l = (int) lAsDouble;
		
		byte[] signature = new byte[(2*l)];


		//using goto statements in java with named breakpoints.
		createNewSignature : while(true) {
			
			//1... k = RNG({1, 2, 3, ..., n-1}) where n = order of point G in E(Fp)
			k = calculateRandomK(BigInteger.ONE, nMinusOne);

			//2. Q = k * G
			Q = generatorG.multiply(k);
			Q = Q.normalize();			//normalize the point 
			
				//3a.a FE2OS(xQ)
			BigInteger xCoordinateQ = Q.getAffineXCoord().toBigInteger();
			byte[] xQ = TR_03111_Utils.FE2OS(xCoordinateQ, p);
			
				//3a.b FE2OS(yQ)
			BigInteger yCoordinateQ = Q.getAffineYCoord().toBigInteger();
			byte[] yQ = TR_03111_Utils.FE2OS(yCoordinateQ, p);
			
				//3a.c toBeHashed = FE2OS(xQ) concat FE2OS(yQ) concat M
//byte[] toBeHashed = Utils.concatAnyNumberOfByteArrays(xQ, yQ, message);
			
				//3b.a hash = hash(toBeHashed)
			MessageDigest md = null;
					
					try {
						md = MessageDigest.getInstance(hashFunction, "BC");
						//check, if length of the output of the hash function in bits < bit length of the order of the base point
						//if yes, that is illegal according to BSI TR-03111 V2.1
						if((md.getDigestLength()*8) < tau) {
							throw new TR_03111_ECC_V2_1_Exception("The length of the hash function SHOULD NOT be chosen so that digestBitLength < tau");
						}
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					} catch (NoSuchProviderException e) {
						e.printStackTrace();
					}

					
					//feed the concatenation of FE2OS(xQ), FE2OS(yQ) and toBeSigned into hash function
					md.update(xQ);
					md.update(yQ);
					md.update(message);
					
				//3b.b finish the hash calculation
					byte[] hashByteArray = md.digest();
		
				//3c.a truncate the hashByteArray to the tau leftmost bits in accordance with the TR-03111
					byte[] truncatedHashByteArray = TR_03111_Utils.truncatedleftmostBits(hashByteArray, tau);
						
				//3c.b create a BigInteger that can hold the truncated hash value byte array 
					r = TR_03111_Utils.OS2BigInt(truncatedHashByteArray);
					
				//3d if r % n == 0 goto 1.
					if(r.mod(n) == BigInteger.ZERO){
						continue createNewSignature;
					}
					
			//4. s = (k + r*dA) % n	
				//4b rDa = r*dA
					BigInteger rDa = r.multiply(privateKeyInteger);
					
				//4c kPlusRdA = rDa + k
					BigInteger kPlusRdA = rDa.add(k);
					
				//4d s = kPlusRdA mod n
					s = kPlusRdA.mod(n);
					
				//4e check if s == 0
					if(s == BigInteger.ZERO) {
						continue createNewSignature;
					}
				//break out of the loop
				break;
			} 
		
		//5a convert r into byte array with I2OS(rAsBigInt, l)
			byte[] rAsByteArray = null;
			rAsByteArray = TR_03111_Utils.I2OS(r, l);
			
			
		//5b convert s into byte array with I2OS(s, l)
			byte[] sAsByteArray = null;
			sAsByteArray = TR_03111_Utils.I2OS(s, l);
			
			
		//5c concat rAsByteArray and sAsByteArray to get signature
			signature = Utils.concatTwoByteArrays(rAsByteArray, sAsByteArray);

		return signature;
	}

	
	/**
	 * Mainly used for testing purposes, this method uses the public key of a loaded ECSDSA to verify the signature created with that keys
	 * private counterpart. The caller has to make sure that the keys are correct, otherwise the signature validation will fail.
	 * @param signature the signature as a byte array over the message. Has to be just the signature, without any ASN.1 attachment.
	 * @param message the message that was fed into the signature creation.
	 * @throws ECCException
	 * @throws TR_03111_ECC_V2_1_Exception
	 */
	public boolean verify(byte[] signature, byte[] message) throws ECCException, TR_03111_ECC_V2_1_Exception {
		boolean isVerified = false;
		
		//define everything that can be defined at this point of the program
		BigInteger n = ecSpec.getN();										//domain parameter n
		BigInteger nMinusOne = ecSpec.getN().subtract(BigInteger.ONE); 		//nMinusOne = domain parameter n minus 1
		ECPoint generatorG = ecSpec.getG();									//domain parameter G
		BigInteger p = ecSpec.getCurve().getField().getCharacteristic(); 	//domain parameter p
		
		int tau = n.bitLength(); 								//tau is the bit length of the order of the base point. tau = log2(n)
		
		BigInteger two = new BigInteger("2");
		BigInteger twoPowTau = two.pow(tau); 								//side calculation for (2 pow tau) minus 1
		BigInteger twoPowTauMinusOne = twoPowTau.subtract(BigInteger.ONE); 	//used for verifying r e {1, ... , 2^tau -1}
		
		
		//get the length of each byte array of r and s
		//should be signature.length / 2
		if((signature.length % 2) != 0) {
			throw new ECCException("total length of r and s combined should be an even value.");
		}
		int l = signature.length / 2;
		
		//1a. split the signature into its components r and s
		byte[] rAsByteArray = new byte[l];
		byte[] sAsByteArray = new byte[l];
		System.arraycopy(signature, 0, rAsByteArray, 0, rAsByteArray.length);
		System.arraycopy(signature, (rAsByteArray.length), sAsByteArray, 0, sAsByteArray.length);
		
		
		//1b. turn rAsByteArray and sAsByteArray into BigIntegers
		BigInteger r = TR_03111_Utils.OS2BigInt(rAsByteArray);
		BigInteger s = TR_03111_Utils.OS2BigInt(sAsByteArray);
		
		//1c. verify that r e {1, ... , 2^tau -1} && s e {1, 2, ... , n-1}
			//1c.a calculate 2pow(r) -1
			
			//r >= 1 
			boolean b1= r.compareTo(BigInteger.ONE) >=0 ;
			//r <= 2^tau -1
			boolean b2 = r.compareTo(twoPowTauMinusOne) <= 0;
			//s >= 1
			boolean b3 = s.compareTo(BigInteger.ONE) >= 0;
			//s <= n-1
			boolean b4 = s.compareTo(nMinusOne) <= 0;
		
			//1c.b if r OR s fail to verify, output false
		if((!b1) || (!b2) || (!b3) || (!b4)) {
			return false;
		}
		
		//2a. calculate sG = s*G
		ECPoint sG = generatorG.multiply(s);
		sG = sG.normalize(); 		//normalize the point
		
		
		//2b. calculate rPa = r*Pa
			ECPoint Pa = publicKey.getQ(); 		//get the public point from the public key
			Pa = Pa.normalize();
				
			ECPoint rPa = Pa.multiply(r);
			rPa = rPa.normalize();
		//2c. calculate Q = sG - rPa
			ECPoint Q = sG.subtract(rPa);
			Q = Q.normalize();
		
		//2d check if Q == Point Infinity
		if(Q.isInfinity()) {	//Q.equals(ecSpec.getG().getCurve().getInfinity()) if Q.isInfinity is not working
			System.out.println("INFO: signature verification failed because Q is infinity!");
			return false;
		}
		
		//3a.a FE2OS(xQ)
		BigInteger xCoordinateQ = Q.getAffineXCoord().toBigInteger();
		byte[] xQ = TR_03111_Utils.FE2OS(xCoordinateQ, p);
			
		//3a.b FE2OS(yQ)
		BigInteger yCoordinateQ = Q.getAffineYCoord().toBigInteger();
		byte[] yQ = TR_03111_Utils.FE2OS(yCoordinateQ, p);
		
		//3a.c toBeHashed = FE2OS(xQ) concat FE2OS(yQ) concat M

		//3b.a hash = hash(toBeHashed)
		MessageDigest md = null;	
				try {
					md = MessageDigest.getInstance(hashFunction, "BC");
					//TODO: throw additional exception? if Hashlength < tau?: Currently not necessary, maybe in the future another exception could be implemented.
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (NoSuchProviderException e) {
					e.printStackTrace();
				}
				
				//feed the concatenation of FE2OS(xQ), FE2OS(yQ) and message into hash function
				md.update(xQ);
				md.update(yQ);
				md.update(message);
				
			//3b.b finish the hash calculation
				byte[] hashByteArray = md.digest();
				
				//3c.a truncate the hashByteArray to the tau leftmost bits in accordance with the TR-03111
					//TR_03111_Utils.OS2BigInt may throw a TR_03111_ECC_V2_1_Exception
				byte[] truncatedHashByteArray = TR_03111_Utils.truncatedleftmostBits(hashByteArray, tau);
			
			//3d.a v = OS2BigInt(truncatedHashByteArray)
				BigInteger v = TR_03111_Utils.OS2BigInt(truncatedHashByteArray);
				
			//3d.b check if v == r
				if(v.equals(r)) {
					isVerified = true;
				}
				
		return isVerified;
	}
	
	
	
	@Override
	public ECPublicKey getPublicKey() {
		return this.publicKey;
	}

	/**
	 * Returns a String of the used signature algorithm so it may be used as a key in the {@linkplain main.java.de.bsi.tsesimulator.constants.Constants#ALGORITHM_OID_MAP}.
	 * Mainly used by the ERSSpecificModule to compare the algorithm it is using and putting into the log messages to the actual algorithm used to sign those
	 * messages.
	 * <br><br>Examples:<br> "SHA3-512" becomes "ESCDSA_PLAIN_SHA3_512" <br> "SHA512" becomes "ECSDSA_PLAIN_SHA_512"
	 */
	@Override
	public String getAlgorithmDefinition() {
		StringBuilder keyForHashMap = new StringBuilder("ECSDSA_PLAIN_");
		//check if the hashFunction has a "-" in it because it's SHA3
		if(hashFunction.contains("-")) {
			String[] splitStringArray = hashFunction.split("-");		//split the string into "SHA3" and "bitlength"
			keyForHashMap.append(splitStringArray[0]).append("_");		//append "SHA3_"
			keyForHashMap.append(splitStringArray[1]);					//append the "bitlength"
		}
		else {
			keyForHashMap.append("SHA_");
			keyForHashMap.append(hashFunction.substring(3, 6));
		}
		return keyForHashMap.toString();
	}
	
	
	
	/**
	 * Returns information about the algorithm, the curve it is using and the hash function. 
	 * @return - a String in the form of "Algorithm:	ESCDSA (Schnorr)	on curve: CURVENAME	with hash function SHA/SHA3"
	 * @throws IOException if {@linkplain PropertyValues#getInstance()} throws it
	 */
	public String getAlgorithmInformation() throws IOException {
		StringBuilder algorithmDefBuild = new StringBuilder("Algortihm:\tECSDSA (Schnorr)\ton curve:\t");
		algorithmDefBuild.append(PropertyValues.getInstance().getValue(ConfigConstants.CFG_TAG_CURVE));
		algorithmDefBuild.append("\t\twith hash function:\t" +this.hashFunction);
		return algorithmDefBuild.toString();
	}
	

	
}
