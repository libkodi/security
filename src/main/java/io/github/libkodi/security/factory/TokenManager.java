package io.github.libkodi.security.factory;

import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import io.github.libkodi.security.CacheManager;
import io.github.libkodi.security.interfaces.Cache;
import io.github.libkodi.security.interfaces.TokenManagerBean;
import io.github.libkodi.security.utils.JwtBuilder;
import io.github.libkodi.security.utils.JwtParser;
import io.github.libkodi.security.utils.StringUtils;

public class TokenManager implements TokenManagerBean {
	private CacheManager cacheManager;
	private String key;
	private int idleTimeout = 120;
	private int maxAliveTimeout = 720;
	private String tokenKey = "token";
	
	public TokenManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
		initJwtKey();
	}
	
	public int getIdleTimeout() {
		return idleTimeout;
	}
	
	public void setIdleTimeout(int idleTimeout) {
		this.idleTimeout = idleTimeout;
	}
	
	public int getMaxAliveTimeout() {
		return maxAliveTimeout;
	}
	
	public void setMaxAliveTimeout(int maxAliveTimeout) {
		this.maxAliveTimeout = maxAliveTimeout;
	}
	
	private void initJwtKey() {
		key = (String) cacheManager.getEnvironment("jwtkey");
		
		if (StringUtils.isEmpty(key)) {
			key = getRandomKey(64);
			cacheManager.addEnvironment("jwtkey", key);
		}
	}

	private String getRandomKey(int length) {
		String key = "";
		Random rand = new Random();
		
		for (int i = 0; i < length; i ++) {
			key += (char)(65 + rand.nextInt(24));
		}
		
		return key;
	}
	
	private void setCache(Cache cache) {
		cacheManager.put("$cache", cache);
	}
	
	public Cache getCache() {
		return (Cache) cacheManager.get("$cache");
	}
	
	@Override
	public String create() throws Exception {
		JwtBuilder builder = new JwtBuilder();
		
		Cache cache = cacheManager.instance(idleTimeout, maxAliveTimeout);		
		builder.withJWTId(cache.getCacheId());
		setCache(cache);
		
		return builder.sign(key);
	}
	
	@Override
	public Cache getCache(HttpServletRequest request) {
		String token = request.getParameter(tokenKey);
		
		if (StringUtils.isEmpty(token)) {
			token = request.getHeader(tokenKey);
			
			if (StringUtils.isEmpty(token)) {
				return null;
			}
		}
		
		JwtParser parser = new JwtParser(key, token);
		String cacheId = parser.getJwtId();
		Cache cache = cacheManager.instance(cacheId, 0, 0, false);
		setCache(cache);
		
		return cache;
	}

	@Override
	public boolean hasToken(HttpServletRequest request) {
		return !StringUtils.isEmpty(request.getParameter(tokenKey));
	}
}
