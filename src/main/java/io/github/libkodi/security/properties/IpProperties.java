package io.github.libkodi.security.properties;

import lombok.Data;

@Data
public class IpProperties {
	private boolean enable = false;
	private int limit = 100;
	private int range = 60;
	private int block = 300;
}
