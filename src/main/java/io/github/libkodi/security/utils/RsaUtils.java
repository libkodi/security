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
 * @author solitpine
 *
 */
@Slf4j
public class RsaUtils {
	public static String decrypt(String content, String privateKey) throws Exception {
		return decrypt(content, privateKey, null, false);
	}
	
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
		
	public static String encrypt(String content, String publicKey) throws Exception {
		return encrypt(content, publicKey, null, false);
	}
	
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

	public static String decryptFromPublickey(String pubkey, String content) throws Exception {
		return decrypt(content, pubkey, null, true);
	}
	
	public static String encryptFromPrivateKey(String content, String prikey) throws Exception {
		return encrypt(content, prikey, null, true);
	}
	
	public static String formatToPem(String type, String key) throws IOException {
		PemObject obj = new PemObject(type, Base64.decodeBase64(key.getBytes()));
		StringWriter sw = new StringWriter();
		PemWriter pw = new PemWriter(sw);
		pw.writeObject(obj);
		pw.close();
		
		return sw.toString();
	}
	
	public static String sign(String privateKey, byte[] message) throws Exception {
		byte[] decode = Base64Utils.decodeFromString(privateKey);
		RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decode));
		Signature signature = Signature.getInstance("SHA256WithRSA");
		signature.initSign(priKey);
		signature.update(message);
		return Base64.encodeBase64String(signature.sign());
	}
	
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
	
	/**
	 -----BEGIN PUBLIC KEY-----
MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCqGowNFD200C9YkTjIjHqESlno
ewxkmq+cKYDqO3kgIAX9AhEBgIKkfOYVkkbPR0fRbGEiTPyzSz5PEEOAkssTzfkU
6b6peoZfWJBHnDhBBFvni1D4pqWTSrlsz6pTa4mqsqbVGImVMs2kfscUO3NW6gpx
wuUq6ylb3AIhIk2lOQIDAQAB
-----END PUBLIC KEY-----

-----BEGIN PRIVATE KEY-----
MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKoajA0UPbTQL1iR
OMiMeoRKWeh7DGSar5wpgOo7eSAgBf0CEQGAgqR85hWSRs9HR9FsYSJM/LNLPk8Q
Q4CSyxPN+RTpvql6hl9YkEecOEEEW+eLUPimpZNKuWzPqlNriaqyptUYiZUyzaR+
xxQ7c1bqCnHC5SrrKVvcAiEiTaU5AgMBAAECgYAhqgNRi1Hy+MiF9KeSOQr+J2DH
u6JmPp7Gfwg+UurTysKkocYP0AkK2JUIa9yLpQW3koVernHMYfXA2+0gAu0heLmd
Kqrvs2a44lMZUBDaPY3KnPn9jm/VuOh/kw2V1Ar/84iKtq8gQvtiitxqIFRJpr55
3trbxKj4cny4+W+4AQJBAPJgmIyJqyS/QlVxD4La35Nk6cv5Wl7CBbBT6Le6bKGV
KRdS1Y857E/D+9gWIQFxNbvmte4V3zBLh/2w2CRtmrkCQQCzqg38BAlNesL8pfT3
pjUQAwcqvmcLvWqc6yvVrIne0kfsFe//i0nyFb9sfDDplo8FUbX2bb0xUYxtl115
+R6BAkBeOs2z/aXK0IyyeRUls4AuZpI6mcPXiSj7B3Q3UcQAAeSKy8p5N3S3AiO8
O8YzMFdKqQRb/qbZuNOy/njZmL8JAkAuoxIGPTX8+sTquFmif25+vYp6ufodvgmS
uikiyirvwD4TNo6aExoa0TJjgslfmUl+4/sMAOh/iN+fAw+svdIBAkBYkGwriiNR
GbLOROQH+Ne/Y7a81iVyQx4XTp4fdvuuHslKX74E8EpXWslX7++qAOYW8K9C9iLt
KH2aC8ukwjmo
-----END PRIVATE KEY-----

-----BEGIN RSA PRIVATE KEY-----
MIICWwIBAAKBgQCqGowNFD200C9YkTjIjHqESlnoewxkmq+cKYDqO3kgIAX9AhEB
gIKkfOYVkkbPR0fRbGEiTPyzSz5PEEOAkssTzfkU6b6peoZfWJBHnDhBBFvni1D4
pqWTSrlsz6pTa4mqsqbVGImVMs2kfscUO3NW6gpxwuUq6ylb3AIhIk2lOQIDAQAB
AoGAIaoDUYtR8vjIhfSnkjkK/idgx7uiZj6exn8IPlLq08rCpKHGD9AJCtiVCGvc
i6UFt5KFXq5xzGH1wNvtIALtIXi5nSqq77NmuOJTGVAQ2j2Nypz5/Y5v1bjof5MN
ldQK//OIiravIEL7YorcaiBUSaa+ed7a28So+HJ8uPlvuAECQQDyYJiMiaskv0JV
cQ+C2t+TZOnL+VpewgWwU+i3umyhlSkXUtWPOexPw/vYFiEBcTW75rXuFd8wS4f9
sNgkbZq5AkEAs6oN/AQJTXrC/KX096Y1EAMHKr5nC71qnOsr1ayJ3tJH7BXv/4tJ
8hW/bHww6ZaPBVG19m29MVGMbZddefkegQJAXjrNs/2lytCMsnkVJbOALmaSOpnD
14ko+wd0N1HEAAHkisvKeTd0twIjvDvGMzBXSqkEW/6m2bjTsv542Zi/CQJALqMS
Bj01/PrE6rhZon9ufr2Kern6Hb4JkropIsoq78A+EzaOmhMaGtEyY4LJX5lJfuP7
DADof4jfnwMPrL3SAQJAWJBsK4ojURmyzkTkB/jXv2O2vNYlckMeF06eH3b7rh7J
Sl++BPBKV1rJV+/vqgDmFvCvQvYi7Sh9mgvLpMI5qA==
-----END RSA PRIVATE KEY-----

	 */
	
	public static void main(String[] args) {
		String prikey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAJ7Gw6hkbHumO9Ikx72D96LCtFRj+g4Yha5yJ/ZpKQFw7PNx4GJZmzFtKGrwDs+a3EaKW0UM9WS9ntCAqeqNEueqCfbFdEVmCgKQb0SwTwrJ/1xlBg+bKX7qoaBhk/KRyjn77m0txwDBbKTNh8v/hoICGNrAPFeNchIMelt3dVtdAgMBAAECgYAqWehMnWTWYpPoTMk+gtnl/ztsmzJS2jWmP2rrbkdyy/VYRlLMyQv9VhmG1LuOoscIxdTsiTVXhsC66ubn2nNlC0/yEaYo6MmO543Uv8xGWNRHkEwOyOAhZuaaACiQTXfbuQ/Kjczhbx6+61a618f4pJmhGATIE1Jot9JnbaoG3QJBAP7WWIZCue9UpDVPw7oIr2cdBSlOOVqwN/NzUEM/N6ZMTx6WGjySOQNYbaqyzB4DjkQKxiMBFSO3M4X70a/CXfMCQQCfgDfBkeWwZp1402+ZXrVngKvIMYRCKLqCjWLlbc8zt05rPg60eByx76IGWoowwWeSYZZ4zEDBtPPJJ3VhG6VvAkALeI6HjJ1d/otdsvd2ekma9J103IaMZH5AQSKNxFy26ZxuHgeUTmiuk95r3px9hV90BKAilzviXa6+CzXHGZKtAkBBkfZZ716bnDPUfid8x4CP4ke8bw7OG+xAy1sCspfhzNPCJqro6g/x3m+faQ0yae/oL5Iqat2cRIWMBlqs5y6lAkEAqp3OsTEjgyJ+FETVgK7zJuzK28askvBk34EMW2z2G5kG3JRJtIacgrSU6FT/TTbyUlAosnFrt++ItOtZV78cPA==";
		String con = "/v3/trade/query?appid=02d0839d09bbe5479cb39d44dc076787&orderno=16309003562651284533563602\n"
				+ "GET\n"
				+ "1632886715\n"
				+ "ADTLNFBRRYJNVABNRAJBKRPYWNCRKSXT";
		
		try {
			System.out.println(sign(prikey, con.getBytes()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
