package io.github.libkodi.security.entity;

import lombok.Data;

@Data
public class DataSet<T> {
	private T value;
	private String prev = null;
	private String next = null;
	
	public DataSet(T value) {
		this.value = value;
	}
}
