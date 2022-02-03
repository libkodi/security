package io.github.libkodi.security;

import org.springframework.data.redis.core.RedisTemplate;

import io.github.libkodi.security.interfaces.GetCacheIdHandle;
import io.github.libkodi.security.properties.AuthProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author solitpine
 * @description 会话管理服务 
 *
 */
@Slf4j
public class SecurityManager {
	private static SecurityManager instance = null;
	private AuthProperties properties;
	private RedisTemplate<String, Object> redis;
	private Thread refreshThread = null;
	private AccessManager accessManager;
	private CacheManager cacheManager;
	private GetCacheIdHandle cacheIdHandle;
	
	public static SecurityManager getInstance(AuthProperties properties, RedisTemplate<String, Object> redis, CacheManager cacheManager, AccessManager accessSecurity, GetCacheIdHandle getCacheIdHandle) {
		if (instance == null) {
			instance = new SecurityManager(properties, redis, cacheManager, accessSecurity, getCacheIdHandle);
		}
		
		return instance;
	}
	
	private SecurityManager(AuthProperties properties, RedisTemplate<String, Object> redis, CacheManager cacheManager, AccessManager accessSecurity, GetCacheIdHandle getCacheIdHandle) {
		this.properties = properties;
		this.redis = redis;
		this.accessManager = accessSecurity;
		this.cacheManager = cacheManager;
		this.cacheIdHandle = getCacheIdHandle;
		init();
	}
	
	public GetCacheIdHandle getCacheIdHandle() {
		return cacheIdHandle;
	}
	
	public AuthProperties getProperties() {
		return properties;
	}
	
	public RedisTemplate<String, Object> getRedis() {
		return redis;
	}
	
	public AccessManager getAccessManager() {
		return accessManager;
	}
	
	public CacheManager getCacheManager() {
		return cacheManager;
	}
	
	private void init() {
		if (!properties.getRedis().isEnable()) {
			long sleepTime = Math.max(properties.getRefreshPeriod(), 1) * 1000;
			
			/**
			 * 启动一个线程来处理所有数据的过期清理
			 */
			refreshThread = new Thread(() -> {
				while (true) {
					try {
						Thread.sleep(sleepTime);
						accessManager.update();
						cacheManager.update();
					} catch (Exception e) {
						log.error("", e);
					}
				}
			});
			
			refreshThread.setDaemon(true); // 开启安全线程
			refreshThread.start();
		}
	}
}
