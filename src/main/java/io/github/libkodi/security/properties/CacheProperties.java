package io.github.libkodi.security.properties;

import lombok.Data;

@Data
public class CacheProperties {
	private int maxIdle = 120;
	private int maxAlive = 7200;
	private String key = "CacheId";
}
