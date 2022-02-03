package io.github.libkodi.security.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "security")
public class AuthProperties {
	private int refreshPeriod = 1; // 线程定时刷新
	private CacheProperties cache;
	private IpProperties access;
	private RedisProperties redis;
}
