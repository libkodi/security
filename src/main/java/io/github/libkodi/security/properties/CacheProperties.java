package io.github.libkodi.security.properties;

import lombok.Data;

@Data
public class CacheProperties {
	private int maxIdle = 120; // 最大闲置时间(s)
	private int maxAlive = 7200; // 最大存活时间(s)
	private String key = "CacheId"; // 缓存的KEY
}
