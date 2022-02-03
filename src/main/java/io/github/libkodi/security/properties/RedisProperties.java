package io.github.libkodi.security.properties;

import lombok.Data;

@Data
public class RedisProperties {
	private boolean enable = false;
	private String prefix = "redis";
}
