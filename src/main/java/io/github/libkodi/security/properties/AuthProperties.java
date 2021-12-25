package io.github.libkodi.security.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "ws")
public class AuthProperties {
	private int refreshPeriod = 1; // 线程定时刷新
	private int cacheIdleTimeout = 120; // 会话闲置过期
	private int cacheMaxAlive = 7200; // 会话最大生命周期
	private String cacheKey = "CacheId";
	private boolean redisEnable = false; // 启动redis
	private String redisKeySuffix = "redis"; // redis的key的前缀
	private boolean ipLimitEnable = false; // 开启ip限制
	private int ipLimit = 100; // 指定时间内的ip请求上限
	private int ipLimitSeconds = 60; // 上限判定时间段
	private int ipBlockSeconds = 300; // 到达上限后的锁定时间
}
