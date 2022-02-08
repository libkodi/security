package io.github.libkodi.security.entity;

import lombok.Data;

@Data
public class Access {
	private long count = 0;
	private boolean blocked = false;
	private String ip;
	
	public long increment() {
		count += 1;
		return count;
	}
}
