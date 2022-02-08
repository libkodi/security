package io.github.libkodi.security.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import io.github.libkodi.security.CacheManager;
import io.github.libkodi.security.interceptor.WebServletRequestReplaceFilter;
import io.github.libkodi.security.properties.AuthProperties;

/**
 * 创建一些主要的bean 
 */
@Order(-1200)
@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class ApiConfiguration {
	/**
	 * 创建redis模板
	 */
	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
		RedisTemplate<String, Object> temp = new RedisTemplate<>();
		temp.setKeySerializer(new StringRedisSerializer());
		temp.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		temp.setConnectionFactory(factory);
		return temp;
	}
	
	/**
	 * 添加对requestBody的重复读取支持
	 */
	@SuppressWarnings("rawtypes")
	@Bean
	public FilterRegistrationBean httpFilterRegistrationBean(CacheManager cacheManager) {
		FilterRegistrationBean<WebServletRequestReplaceFilter> filter = new FilterRegistrationBean<WebServletRequestReplaceFilter>();
		filter.addUrlPatterns("/*");
		filter.setFilter(new WebServletRequestReplaceFilter(cacheManager));
//		filter.addInitParameter("paramName", "paramValue");
//		filter.addServletNames("httpServletRequestReplacedFilter");
		filter.setOrder(-1600);
		
		return filter;
	}
}
