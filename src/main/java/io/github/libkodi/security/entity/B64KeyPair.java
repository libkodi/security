package io.github.libkodi.security.entity;

import java.io.IOException;

import org.apache.tomcat.util.codec.binary.Base64;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPrivateKeyStructure;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class B64KeyPair {
	private String privateKey;
	private String publicKey;
	
	public String getPkcs1PrivateKey() throws IOException {
		byte[] encode = Base64.decodeBase64(privateKey.getBytes());
		
		PrivateKeyInfo pk1 = PrivateKeyInfo.getInstance(encode);
		RSAPrivateKeyStructure pkcs1Key = RSAPrivateKeyStructure.getInstance(pk1.getPrivateKey());
		byte[] pkcs1Bytes = pkcs1Key.getEncoded();

		return Base64.encodeBase64String(pkcs1Bytes);
		
	}
}
