package io.github.libkodi.security.utils;

import java.util.Date;
import java.util.HashMap;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;

/**
 * jwt构建器
 */
public class JwtBuilder {
	private Builder jwt;
	private HashMap<String, Object> headerClaim = new HashMap<String, Object>();

	public JwtBuilder() {
		this.jwt = JWT.create();
		
		headerClaim.put("alg", "HS256");
		headerClaim.put("typ", "JWT");
	}
	
	public JwtBuilder withClaim(String name, String value) {
		jwt.withClaim(name, value);
		return this;
	}
	
	public JwtBuilder withClaim(String name, int value) {
		jwt.withClaim(name, value);
		return this;
	}
	
	public JwtBuilder withClaim(String name, long value) {
		jwt.withClaim(name, value);
		return this;
	}
	
	public JwtBuilder withClaim(String name, double value) {
		jwt.withClaim(name, value);
		return this;
	}
	
	public JwtBuilder withClaim(String name, Date value) {
		jwt.withClaim(name, value);
		return this;
	}
	
	public JwtBuilder withExpiresAt(long expires) {
		jwt.withExpiresAt(new Date(System.currentTimeMillis() + expires));
		return this;
	}
	
	public JwtBuilder withExpiresAt(Date expires) {
		jwt.withExpiresAt(expires);
		return this;
	}
	
	public JwtBuilder withHeaderClaim(String name, Object value) {
		headerClaim.put(name, value);
		return this;
	}
	
	public String sign(String key) throws Exception {
		jwt.withHeader(headerClaim);
		
		return jwt.sign(Algorithm.HMAC256(key.getBytes()));
	}
	
	public JwtBuilder withIssuedAt(Date issued) {
		jwt.withIssuedAt(issued);
		return this;
	}
	
	public JwtBuilder withIssuer(String issuer) {
		jwt.withIssuer(issuer);
		return this;
	}
	
	public JwtBuilder withJWTId(String jwtId) {
		jwt.withJWTId(jwtId);
		return this;
	}
	
	public JwtBuilder withKeyId(String keyId) {
		jwt.withKeyId(keyId);
		return this;
	}
	
	public JwtBuilder withNotBefore(Date notBefore) {
		jwt.withNotBefore(notBefore);
		return this;
	}
	
	public JwtBuilder withSubject(String subject) {
		jwt.withSubject(subject);
		return this;
	}
	
	public JwtBuilder withAudience(String... aud) {
		jwt.withAudience(aud);
		return this;
	}
}
