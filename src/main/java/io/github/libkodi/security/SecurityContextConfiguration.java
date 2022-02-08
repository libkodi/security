package io.github.libkodi.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import io.github.libkodi.security.interfaces.GetCacheIdHandle;
import io.github.libkodi.security.interfaces.GetRemoteAddressHandle;
import io.github.libkodi.security.properties.AuthProperties;

/**
 * 主配置类
 */
@Configuration
public class SecurityContextConfiguration {
	/**
	 * 配置参数
	 */
	@Autowired
	private AuthProperties properties;
	
	/**
	 * redis对象的注入
	 */
	@Autowired(required = false)
	private RedisTemplate<String, Object> redis;
	
	/**
	 * 获取请求IP的句柄
	 */
	@Autowired(required = false)
	private GetRemoteAddressHandle getRemoteAddresshandle;
	
	/**
	 * 获取缓存ID的句柄
	 */
	@Autowired(required = false)
	private GetCacheIdHandle getCacheIdHandle;
	
	/**
	 * @description 初始化主类
	 */
	@Bean
	public SecurityManager getSpringWebSecurity(CacheManager cacheManager, AccessManager accessSecurity) {
		return SecurityManager.getInstance(properties, redis, cacheManager, accessSecurity, getCacheIdHandle);
	}
	
	@Bean
	public CacheManager getCacheManager() {
		return CacheManager.getInstance(properties, redis);
	}
	
	/**
	 * @description 初始化访问ip与次数管理
	 */
	@Bean
	public AccessManager getAccessSecutiry() {
		return AccessManager.getInstance(properties, redis, getRemoteAddresshandle);
	}
}
