package io.github.libkodi.security.properties;

import lombok.Data;

@Data
public class RedisProperties {
	private boolean enable = false; // 是否开启redis设置
	private String prefix = "redis"; // redis保存的变量的前缀
}
