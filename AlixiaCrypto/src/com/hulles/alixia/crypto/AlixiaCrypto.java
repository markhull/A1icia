/*******************************************************************************
 * Copyright © 2017, 2018 Hulles Industries LLC
 * All rights reserved
 *  
 * This file is part of Alixia.
 *  
 * Alixia is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *    
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * SPDX-License-Identifer: GPL-3.0-or-later
 *******************************************************************************/
package com.hulles.alixia.crypto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import com.hulles.alixia.api.jebus.JebusBible;
import com.hulles.alixia.api.jebus.JebusBible.JebusKey;
import com.hulles.alixia.api.jebus.JebusHub;
import com.hulles.alixia.api.jebus.JebusPool;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.ApplicationKeys;
import com.hulles.alixia.api.shared.ApplicationKeys.ApplicationKey;
import com.hulles.alixia.api.shared.Serialization;

import redis.clients.jedis.Jedis;

/**
 * Here we deliver cryptographic services to Alixia, of various kinds.
 * 
 * @author hulles
 * most of the BCrypt-related code by 
 * @author: Ian Gallagher <igallagher@securityinnovation.com>
 *
 */
public class AlixiaCrypto {
	private static final String AESALGORITHM = "AES/ECB/PKCS5Padding";
	private static ApplicationKeys appKeys = null;
	// Storing the salt here is not a perfect solution but it works for now
	private static final String ALIXIA_SALT = "$2a$16$j68pu2D/uqqZn2YtF/TtmO";
	
	public AlixiaCrypto() {
		
	}

	/**
	 * This method can be used to generate a string representing an account password
	 * suitable for storing in a database. It will be an OpenBSD-style crypt(3) formatted
	 * hash string of length=60
	 * The bcrypt workload is specified in the above static variable, a value from 10 to 31.
	 * A workload of 12 is a very reasonable safe default as of 2013.
	 * This automatically handles secure 128-bit salt generation and storage within the hash.
	 * @param passwordPlaintext The account's plaintext password as provided during account creation,
	 *			     or when changing an account's password.
	 * @return String - a string of length 60 that is the bcrypt hashed password in crypt(3) format.
	 */
	public static String hashPassword(String passwordPlaintext) {
		String hashed_password;
		
		hashed_password = BCrypt.hashpw(passwordPlaintext, ALIXIA_SALT);
		return(hashed_password);
	}
	
	/**
	 * This method can be used to verify a computed hash from a plaintext (e.g. during a login
	 * request) with that of a stored hash from a database. The password hash from the database
	 * must be passed as the second variable.
	 * @param passwordPlaintext The account's plaintext password, as provided during a login request
	 * @param stored_hash The account's stored password hash, retrieved from the authorization database
	 * @return boolean True if the password matches the password of the stored hash, false otherwise
	 */
	public static boolean checkPassword(String passwordPlaintext, String stored_password) {
		boolean password_verified;

		if (null == stored_password || !stored_password.startsWith("$2a$")) {
			throw new AlixiaException("Invalid hash provided for comparison in AlixiaCrypto.checkPassword()");
		}
		password_verified = BCrypt.checkpw(passwordPlaintext, stored_password, ALIXIA_SALT);
		return(password_verified);
	}
    
	/**
	 * Generate an AES key.
	 * 
	 * @return The key
	 * @throws Exception
	 */
	public static SecretKey generateAESKey() throws Exception {
	    KeyGenerator keygen;
	    SecretKey key;
	    
		keygen = KeyGenerator.getInstance("AES");
	    key = keygen.generateKey();
    	return key;
    }
    
	/**
	 * Get an Alixia AES key using Jebus. 
	 * 
	 * @return The key
	 * @throws Exception
	 */
	public static SecretKey getAlixiaJebusAESKey() throws Exception {
    	JebusPool jebusPool;
	    SecretKey key = null;
	    byte[] aesKey;
	    byte[] aesBytes;
	    
    	jebusPool = JebusHub.getJebusCentral();
    	aesKey = JebusBible.getBytesKey(JebusKey.ALIXIAAESKEY, jebusPool);
    	try (Jedis jebus = jebusPool.getResource()) {
    		aesBytes = jebus.get(aesKey);
    		if (aesBytes != null) {
    			key = (SecretKey) Serialization.deSerialize(aesBytes);
    		}
            return key;
    	}
    }
    
	/**
	 * Get an Alixia AES key from a file. 
	 * 
	 * @return The key
	 * @throws Exception
	 */
	public static SecretKey getAlixiaFileAESKey() throws Exception {
	    SecretKey key = null;
	    byte[] aesBytes;
	    String fileName;
	    
	    if (appKeys == null) {
	    	appKeys = ApplicationKeys.getInstance();
	    }
    	fileName = appKeys.getKey(ApplicationKey.SECRETKEYPATH);
    	
    	aesBytes = fileToByteArray(fileName);
		if (aesBytes != null) {
			key = (SecretKey) Serialization.deSerialize(aesBytes);
		}
    	return key;
    }
    
    /**
     * Save an Alixia AES key using Jebus.
     * 
     * @throws Exception
     * @param key
     * 
     */
	@SuppressWarnings("resource")
    public static void setAlixiaJebusAESKey(SecretKey key) throws Exception {
    	JebusPool jebusPool;
	    byte[] aesKey;
	    byte[] aesBytes;
	    
    	jebusPool = JebusHub.getJebusCentral();
    	aesKey = JebusBible.getBytesKey(JebusKey.ALIXIAAESKEY, jebusPool);
	    aesBytes = Serialization.serialize(key);
    	try (Jedis jebus = jebusPool.getResource()) {
    	    jebus.set(aesKey, aesBytes);
    	}
    }
    
    /**
     * Save an Alixia AES key to a file.
     * 
     * @param key
     * @throws Exception
     */
	public static void setAlixiaFileAESKey(SecretKey key) throws Exception {
	    byte[] aesBytes;
	    String fileName;
	    
	    if (appKeys == null) {
	    	appKeys = ApplicationKeys.getInstance();
	    }
    	fileName = appKeys.getKey(ApplicationKey.SECRETKEYPATH);
        // fileName = "/media/hulles/persistence/alixia_aeskey";
	    aesBytes = Serialization.serialize(key);
	    byteArrayToFile(aesBytes, fileName);
    }
	
	/**
	 * Encrypt a string using an AES key.
	 * 
	 * @param aesKey The key
	 * @param textToEncrypt
	 * @return The encrypted byte array
	 * @throws Exception
	 */
	public static byte[] encryptString(SecretKey aesKey, String textToEncrypt) throws Exception {
	    byte[] clearBytes;
	    
	    clearBytes = textToEncrypt.getBytes(StandardCharsets.UTF_8);
		return encrypt(aesKey, clearBytes);
	}
	
	/**
	 * Encrypt a byte array using an AES key.
	 * 
	 * @param aesKey The key
	 * @param bytesToEncrypt The byte array to encrypt
	 * @return The encrypted byte array
	 * @throws Exception
	 */
	public static byte[] encrypt(SecretKey aesKey, byte[] bytesToEncrypt) throws Exception {
	    Cipher aesCipher;
	    byte[] cipherBytes;
	    
	    aesCipher = Cipher.getInstance(AESALGORITHM);
	    aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
	    cipherBytes = aesCipher.doFinal(bytesToEncrypt);
		return cipherBytes;
	}
	
	/**
	 * Decrypt an encrypted byte array to a string.
	 * 
	 * @param aesKey The AES key
	 * @param encryptedBytes
	 * @return The cleartext String
	 * @throws Exception
	 */
	public static String decryptString(SecretKey aesKey, byte[] encryptedBytes) throws Exception {
	    byte[] clearBytes;
	    String clearText;
	    
	    clearBytes = decrypt(aesKey, encryptedBytes);
	    clearText = new String(clearBytes, StandardCharsets.UTF_8);
		return clearText;
	}
	
	/**
	 * Decrypt an encrypted byte array to a clear byte array.
	 * 
	 * @param aesKey The AES key
	 * @param encryptedBytes The encrypted byte array
	 * @return The clear (decrypted) byte array
	 * @throws Exception
	 */
	public static byte[] decrypt(SecretKey aesKey, byte[] encryptedBytes) throws Exception {
	    Cipher aesCipher;
	    byte[] clearBytes;
	    
	    aesCipher = Cipher.getInstance(AESALGORITHM);
	    aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
	    clearBytes = aesCipher.doFinal(encryptedBytes);
		return clearBytes;
	}

	/**
	 * Retrieve a byte array from a file (NIO).
	 * 
	 * @param path
	 * @return
	 */
	private static byte[] fileToByteArray(String path) {
		Path infile;
		byte[] bytes = null;
		
		infile = Paths.get(path);
		try {
			bytes = Files.readAllBytes(infile);
		} catch (IOException e) {
			throw new AlixiaException("Can't create byte array from file", e);
		}
		return bytes;
	}

	/**
	 * Save a byte array to a file (NIO).
	 * 
	 * @param bytes
	 * @param path
	 */
	private static void byteArrayToFile(byte[] bytes, String path) {
		Path infile;
		
		infile = Paths.get(path);
		try {
			Files.write(infile, bytes);
		} catch (IOException e) {
			throw new AlixiaException("Can't create file from byte array", e);
		}
	}
}
