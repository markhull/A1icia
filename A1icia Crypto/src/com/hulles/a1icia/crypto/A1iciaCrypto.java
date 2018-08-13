/*******************************************************************************
 * Copyright © 2017, 2018 Hulles Industries LLC
 * All rights reserved
 *  
 * This file is part of A1icia.
 *  
 * A1icia is free software: you can redistribute it and/or modify
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
package com.hulles.a1icia.crypto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import com.hulles.a1icia.api.jebus.JebusApiBible;
import com.hulles.a1icia.api.jebus.JebusApiHub;
import com.hulles.a1icia.api.jebus.JebusPool;
import com.hulles.a1icia.api.shared.A1iciaAPIException;
import com.hulles.a1icia.api.shared.ApplicationKeys;
import com.hulles.a1icia.api.shared.ApplicationKeys.ApplicationKey;
import com.hulles.a1icia.api.shared.Serialization;

import redis.clients.jedis.Jedis;

/**
 * Here we deliver cryptographic services to A1icia, of various kinds.
 * 
 * @author hulles
 * most of the BCrypt-related code by 
 * @author: Ian Gallagher <igallagher@securityinnovation.com>
 *
 */
public class A1iciaCrypto {
  	// Define the BCrypt workload to use when generating password hashes. 10-31 is a valid value.
	private static final int BCRYPTWORKLOAD = 12;
	private static final String AESALGORITHM = "AES/ECB/PKCS5Padding";
	private static ApplicationKeys appKeys = null;
	
	public A1iciaCrypto() {
		
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
		String salt;
		String hashed_password;
		
		salt = BCrypt.gensalt(BCRYPTWORKLOAD);
		hashed_password = BCrypt.hashpw(passwordPlaintext, salt);

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
	public static boolean checkPassword(String passwordPlaintext, String stored_hash) {
		boolean password_verified = false;

		if (null == stored_hash || !stored_hash.startsWith("$2a$")) {
			throw new A1iciaAPIException("Invalid hash provided for comparison in A1iciaCrypto.checkPassword()");
		}
		password_verified = BCrypt.checkpw(passwordPlaintext, stored_hash);
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
	 * Get an A1icia AES key using Jebus. 
	 * 
	 * @return The key
	 * @throws Exception
	 */
    @SuppressWarnings("resource")
	public static SecretKey getA1iciaJebusAESKey() throws Exception {
    	JebusPool jebusPool;
	    SecretKey key = null;
	    byte[] aesKey;
	    byte[] aesBytes;
	    
    	jebusPool = JebusApiHub.getJebusCentral();
    	aesKey = JebusApiBible.getA1iciaAESKey(jebusPool).getBytes();
    	try (Jedis jebus = jebusPool.getResource()) {
    		aesBytes = jebus.get(aesKey);
    		if (aesBytes != null) {
    			key = (SecretKey) Serialization.deSerialize(aesBytes);
    		}
    	}
    	return key;
    }
    
	/**
	 * Get an A1icia AES key from a file. 
	 * 
	 * @return The key
	 * @throws Exception
	 */
	public static SecretKey getA1iciaFileAESKey() throws Exception {
	    SecretKey key = null;
	    byte[] aesBytes;
	    String fileName;
	    
	    if (appKeys == null) {
	    	appKeys = ApplicationKeys.getInstance();
	    }
    	fileName = appKeys.getKey(ApplicationKey.AESKEYPATH);
    	
    	aesBytes = fileToByteArray(fileName);
		if (aesBytes != null) {
			key = (SecretKey) Serialization.deSerialize(aesBytes);
		}
    	return key;
    }
    
    /**
     * Save an A1icia AES key using Jebus.
     * 
     * @throws Exception
     */
	@SuppressWarnings("resource")
	public static void setA1iciaJebusAESKey(SecretKey key) throws Exception {
    	JebusPool jebusPool;
	    byte[] aesKey;
	    byte[] aesBytes;
	    
    	jebusPool = JebusApiHub.getJebusCentral();
    	aesKey = JebusApiBible.getA1iciaAESKey(jebusPool).getBytes();
	    aesBytes = Serialization.serialize(key);
    	try (Jedis jebus = jebusPool.getResource()) {
    	    jebus.set(aesKey, aesBytes);
    	}
    }
    
    /**
     * Save an A1icia AES key to a file.
     * 
     * @throws Exception
     */
	public static void setA1iciaFileAESKey(SecretKey key) throws Exception {
	    byte[] aesBytes;
	    String fileName;
	    
	    if (appKeys == null) {
	    	appKeys = ApplicationKeys.getInstance();
	    }
    	fileName = appKeys.getKey(ApplicationKey.AESKEYPATH);
        // fileName = "/media/hulles/persistence/a1icia_aeskey";
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
			e.printStackTrace();
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
			e.printStackTrace();
		}
	}
}
