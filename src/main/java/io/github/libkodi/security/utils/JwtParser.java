package io.github.libkodi.security.utils;

import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;

/**
 * jwt解析器
 */
public class JwtParser {
	private Claims claims;
	@SuppressWarnings("rawtypes")
	private JwsHeader headerClaims;
	private String signature;

	public JwtParser(String key, String claimText) {
		parseToJwt(key, claimText);
	}

	private void parseToJwt(String key, String claimText) {
		io.jsonwebtoken.JwtParser parser = Jwts.parser().setSigningKey(key.getBytes());
		Jws<Claims> jwt = parser.parseClaimsJws(claimText);
		this.claims = jwt.getBody();
		this.headerClaims = jwt.getHeader();
		this.signature = jwt.getSignature();
	}
	
	public Object getClaim(String name) {
		return claims.get(name);
	}
	
	public <T> T getClaim(String name, Class<T> requiredType) {
		return claims.get(name, requiredType);
	}
	
	public Date getExpiration() {
		return claims.getExpiration();
	}
	
	public String getJwtId() {
		return claims.getId();
	}
		
	public String getAudience() {
		return claims.getAudience();
	}
	
	public Date getIssuedAt() {
		return claims.getIssuedAt();
	}
	
	public String getIssuer() {
		return claims.getIssuer();
	}
	
	public Date getNotBefore() {
		return claims.getNotBefore();
	}
	
	public String getSubject() {
		return claims.getSubject();
	}
	
	public String getSignature() {
		return signature;
	}
	
	public String getKeyId() {
		return headerClaims.getKeyId();
	}
	
	public String getAlgorithm() {
		return headerClaims.getAlgorithm();
	}
	
	public String getType() {
		return headerClaims.getType();
	}
	
	public Object getHeader(String key) {
		return headerClaims.get(key);
	}
}
