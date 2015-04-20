package com.brightcove.consulting.alj.utils;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author ssayles
 */
public class EncryptionHandler {

	private String key;
	private SecretKeySpec keySpec;
	private boolean useEncryption;

	
	public String getKey() {
		return this.key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	public boolean isUseEncryption() {
		return useEncryption;
	}

	public void setUseEncryption(boolean useEncryption) {
		this.useEncryption = useEncryption;
	}

	public String encrypt(String input) {
		if (StringUtils.isEmpty(input)) {
			return input;
		}

		Key keySpec = getKeySpec();
		
		Cipher aes;
		try {
			aes = Cipher.getInstance("AES");
			aes.init(Cipher.ENCRYPT_MODE, keySpec);
			byte[] ciphertext = aes.doFinal(input.getBytes());
			byte[] encodeBase64Chunked = Base64.encodeBase64(ciphertext);
			return new String(encodeBase64Chunked);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String decrypt(String input) {
		if (StringUtils.isEmpty(input)) {
			return input;
		}

		Key keySpec = getKeySpec();

		Cipher aes;
		try {
			aes = Cipher.getInstance("AES");
			aes.init(Cipher.DECRYPT_MODE, keySpec);
			byte[] decodeBase64 = Base64.decodeBase64(input.getBytes());
			String cleartext = new String(aes.doFinal(decodeBase64));
			return cleartext;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}

	private Key getKeySpec() {
		if (keySpec != null) {
			return keySpec;
		}

		if (StringUtils.isEmpty(key)) {
			throw new IllegalStateException("secret key is not set.");
		}

		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA");
			digest.update(key.getBytes());
			keySpec = new SecretKeySpec(digest.digest(), 0, 16, "AES");
			return keySpec;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

}
