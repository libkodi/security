package io.github.libkodi.security.factory;

import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import io.github.libkodi.security.CacheManager;
import io.github.libkodi.security.interfaces.Cache;
import io.github.libkodi.security.interfaces.TokenManagerBean;
import io.github.libkodi.security.utils.JwtBuilder;
import io.github.libkodi.security.utils.JwtParser;
import io.github.libkodi.security.utils.StringUtils;

/**
 * Token管理器
 */
public class TokenManager implements TokenManagerBean {
	private CacheManager cacheManager;
	private String key;
	private String tokenKey = "token";
	
	public TokenManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
		initJwtKey();
	}
	
	public TokenManager(CacheManager cacheManager, String key) {
		this.cacheManager = cacheManager;
		tokenKey = key;
		initJwtKey();
	}
	
	/**
	 * 初始化生成token用的jwt密钥
	 */
	private void initJwtKey() {
		key = (String) cacheManager.getEnvVar("jwtkey");
		
		if (StringUtils.isEmpty(key)) {
			key = getRandomKey(64);
			cacheManager.addEnvVar("jwtkey", key);
		}
	}
	
	/**
	 * 
	 * 生成一个指定长度的随机字符串
	 *
	 * @param length长度
	 * @return 随机字符串
	 */
	private String getRandomKey(int length) {
		String key = "";
		Random rand = new Random();
		
		for (int i = 0; i < length; i ++) {
			key += (char)(65 + rand.nextInt(24));
		}
		
		return key;
	}
	
	/**
	 * 
	 * 将缓存实例添加到线程变量中
	 * @param cache 缓存实例
	 */
	private void setCache(Cache cache) {
		cacheManager.addThreadVar("$cache", cache);
	}
	
	/**
	 * 
	 * 从线程实例中获取缓存实例
	 *
	 * @return 缓存实例
	 */
	public Cache getCache() {
		return (Cache) cacheManager.getThreadVar("$cache");
	}
	
	/**
	 * 创建一个token，并生成一个对应的缓存，使token也可以存储数据
	 */
	@Override
	public String create(int maxIdle, int maxAlive) throws Exception {
		JwtBuilder builder = new JwtBuilder();
		
		Cache cache = cacheManager.create(maxIdle, maxAlive);		
		builder.withJWTId(cache.getCacheId());
		setCache(cache);
		
		return builder.sign(key);
	}
	
	@Override
	public String create(int maxAlive) throws Exception {
		return create(0, maxAlive);
	}
	
	/**
	 * 从请求对象中获取缓存实例
	 */
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
		Cache cache = cacheManager.create(cacheId, 0, 0, false);
		setCache(cache);
		
		return cache;
	}
	
	/**
	 * 判断该请求是否包含token
	 */
	@Override
	public boolean hasToken(HttpServletRequest request) {
		return !StringUtils.isEmpty(request.getParameter(tokenKey));
	}
}
