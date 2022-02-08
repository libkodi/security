package io.github.libkodi.security.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.apache.tomcat.util.codec.binary.Base64;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.springframework.util.Base64Utils;

import io.github.libkodi.security.entity.B64KeyPair;
import lombok.extern.slf4j.Slf4j;

/**
 * rsa加密处理
 *
 */
@Slf4j
public class RsaUtils {
	/**
	 * 
	 * RSA解密
	 *
	 * @param content 加密后的内容
	 * @param privateKey 私钥
	 * @return 解密后的结果
	 * @throws Exception
	 */
	public static String decrypt(String content, String privateKey) throws Exception {
		return decrypt(content, privateKey, null, false);
	}
	
	/**
	 * 
	 * RSA解密
	 *
	 * @param content 加密后的内容
	 * @param privateKey 私钥
	 * @param padding 分割符
	 * @param isPublicKey 是否使用公钥解密
	 * @return 解密后的结果
	 * @throws Exception
	 */
	public static String decrypt(String content, String key, String padding, boolean isPublicKey) throws Exception {
		if (content != null && content.length() <= 172) {
			padding = "";
		}
		
		byte[] input = Base64Utils.decode(content.getBytes("UTF-8"));
		byte[] decode = Base64Utils.decode(key.getBytes("UTF-8"));
		Cipher cipher = Cipher.getInstance("RSA");
		
		if (isPublicKey) {
			RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decode));
			cipher.init(Cipher.DECRYPT_MODE, pubKey);
		} else {
			RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decode));
			cipher.init(Cipher.DECRYPT_MODE, priKey);
		}
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		int offset = 0, len = input.length, step, pl = 0;
		byte[] buffer = null;
		
		if (padding != null) {
			pl = padding.length();
		}
		
		try {
			do {
				step = Math.min(128, len - offset);
				byte[] res = cipher.doFinal(input, offset, step);
				bos.write(res);
				offset += step + pl;
			} while (offset < len);
			
			buffer = bos.toByteArray();
		} catch (Exception e) {
			log.error("", e);
		} finally {
			try {
				bos.close();
			} catch (Exception e) {
				log.error("", e);
			}
		}
		
		if (buffer != null) {
			return new String(buffer, "UTF-8");
		} else {
			throw new Exception();
		}
	}
	
	/**
	 * 
	 * RSA加密
	 *
	 * @param content 需要加密的内容
	 * @param key 密钥
	 * @return 加密后的结果
	 * @throws Exception
	 */
	public static String encrypt(String content, String publicKey) throws Exception {
		return encrypt(content, publicKey, null, false);
	}
	
	/**
	 * 
	 * RSA加密
	 *
	 * @param content 需要加密的内容
	 * @param key 密钥
	 * @param padding 分割符
	 * @param isPrivateKey 是否使用私钥加密
	 * @return 加密后的结果
	 * @throws Exception
	 */
	public static String encrypt(String content, String key, String padding, boolean isPrivateKey) throws Exception {
		byte[] decode = Base64Utils.decodeFromString(key);
		Cipher cipher = Cipher.getInstance("RSA");
		
		if (isPrivateKey) {
			RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decode));
			cipher.init(Cipher.ENCRYPT_MODE, priKey);
		} else {
			RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decode));
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		}
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buffer = null;
		
		try {
			byte[] cbytes = content.getBytes("UTF-8");
			int offset = 0, len = cbytes.length, step;
			
			do {
				step = Math.min(117, len - offset);
				byte[] res = cipher.doFinal(cbytes, offset, step);
				bos.write(res);
				
				if (padding != null && !padding.equals("")) {
					bos.write(padding.getBytes());
				}
				
				offset += step;
			} while (offset < len);
			
			buffer = bos.toByteArray();
		} catch (Exception e) {
			log.error("", e);
		} finally {
			try {
				bos.close();
			} catch (Exception e) {}
		}
		
		if (buffer != null) {
			return Base64Utils.encodeToString(buffer);
		} else {
			throw new Exception();
		}
	}
	
	/**
	 * 
	 * 生成一个密钥对
	 *
	 * @return B64KeyPair
	 * @throws Exception
	 */
	public static B64KeyPair getKeyPair() throws Exception {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
		gen.initialize(1024, new SecureRandom());
		KeyPair key = gen.generateKeyPair();
		RSAPrivateKey privateKey = (RSAPrivateKey) key.getPrivate();
		RSAPublicKey publicKey = (RSAPublicKey) key.getPublic();
		String privateKeyStr = Base64Utils.encodeToString(privateKey.getEncoded());
		String publicKeyStr = Base64Utils.encodeToString(publicKey.getEncoded());
		
		return new B64KeyPair(privateKeyStr, publicKeyStr);
	}
	
	/**
	 * 
	 * 以公钥解密
	 *
	 * @param pubkey 公钥
	 * @param content 加密内容
	 * @return 解密结果
	 * @throws Exception
	 */
	public static String decryptFromPublickey(String pubkey, String content) throws Exception {
		return decrypt(content, pubkey, null, true);
	}
	
	/**
	 * 
	 * 以私钥加密
	 *
	 * @param content 需要加密的内容
	 * @param prikey 私钥
	 * @return 加密结果
	 * @throws Exception
	 */
	public static String encryptFromPrivateKey(String content, String prikey) throws Exception {
		return encrypt(content, prikey, null, true);
	}
	
	/**
	 * 
	 * 格式化密钥输出形式
	 *
	 * @param type 输出类型
	 * @param key 密钥
	 * @return 格式化结果 
	 * @throws IOException
	 */
	public static String formatToPem(String type, String key) throws IOException {
		PemObject obj = new PemObject(type, Base64.decodeBase64(key.getBytes()));
		StringWriter sw = new StringWriter();
		PemWriter pw = new PemWriter(sw);
		pw.writeObject(obj);
		pw.close();
		
		return sw.toString();
	}
	
	/**
	 * 
	 * 生成签名
	 *
	 * @param privateKey 私钥
	 * @param message 签名内容
	 * @return 签名结果
	 * @throws Exception
	 */
	public static String sign(String privateKey, byte[] message) throws Exception {
		byte[] decode = Base64Utils.decodeFromString(privateKey);
		RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decode));
		Signature signature = Signature.getInstance("SHA256WithRSA");
		signature.initSign(priKey);
		signature.update(message);
		return Base64.encodeBase64String(signature.sign());
	}
	
	/**
	 * 
	 * 验证签名
	 *
	 * @param publicKey 公钥
	 * @param sign 签名
	 * @param message 签名内容
	 * @return true/false
	 * @throws Exception
	 */
	public static boolean verify(String publicKey, String sign, byte[] message) throws Exception {
		byte[] decode = Base64Utils.decodeFromString(publicKey);
		RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decode));
		Signature signature = Signature.getInstance("SHA256WithRSA");
		signature.initVerify(pubKey);
		signature.update(message);
		
		byte[] decodeBase64;
		
		try {
			decodeBase64 = Base64.decodeBase64(sign.getBytes());
		} catch (Exception e) {
			throw new Exception("解签名的base64失败, 无效的base64");
		}
		
		return signature.verify(decodeBase64);
	}
}
