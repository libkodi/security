package io.github.libkodi.security.utils;

import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.util.Base64Utils;

/**
 * AES加密封装
 *
 */
public class AesUtils {
	/**
	 * 解密
	 * @param key
	 * @param encrypted
	 * @return
	 * @throws Exception
	 */
	public static String decrypt(String key, String encrypted) throws Exception {
		byte[] data = Base64Utils.decodeFromString(encrypted);
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, getKey(key), getIV(key));
		byte[] result = cipher.doFinal(data);
		return new String(result, "UTF-8");
	}
	
	/**
	 * 加密
	 * @param key
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static String encrypt(String key, byte[] data) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, getKey(key), getIV(key));
        byte[] result = cipher.doFinal(data);
		return Base64Utils.encodeToString(result);
	}
	
	/**
	 * 将key转换为偏移量
	 * @param key
	 * @return
	 */
	private static IvParameterSpec getIV(String key) {
		int len = key.length();
		
		if (len < 16) {
			key = addKeyPadding(key);
		} else if (len > 16) {
			key = key.substring(0, 16);
		}
		
		return new IvParameterSpec(key.getBytes());
	}
	
	/**
	 * 填充key, 使满足 key.length % 16 == 0
	 * @param key
	 * @return
	 */
	private static String addKeyPadding(String key) {
		if (key.length() < 32) {
			int length = 16 - (key.length() % 16);
			
			for (int i = 0; i < length; i ++) {
				key += "0";
			}
		} else {
			key = key.substring(0, 32);
		}
		
		return key;
	}
	
	/**
	 * 将key转换为加解官用的Key
	 * @param key
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	private static Key getKey(String key) throws NoSuchAlgorithmException {
		key = addKeyPadding(key);
		SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        byte[] enCodeFormat = secretKey.getEncoded();
        SecretKeySpec seckey = new SecretKeySpec(enCodeFormat, "AES");
        return seckey;
	}
}
