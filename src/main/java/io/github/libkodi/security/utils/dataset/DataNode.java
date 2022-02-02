package io.github.libkodi.security.utils.dataset;

public class DataNode<T> {
	private T value = null;
	private long createtime;
	private long activetime;
	private int idleTimeout = 120;
	private int aliveTimeout = 7200;
	
	public DataNode(T value) {
		this.value = value;
		createtime = System.currentTimeMillis();
		activetime = createtime;
	}
	
	public T getValue() {
		return value;
	}
	
	public DataNode<T> setIdleTimeout(int idleTimeout) {
		this.idleTimeout = idleTimeout;
		return this;
	}
	
	public int getIdleTimeout() {
		return idleTimeout;
	}
	
	public int getAliveTimeout() {
		return aliveTimeout;
	}
	
	public DataNode<T> setAliveTimeout(int aliveTimeout) {
		this.aliveTimeout = aliveTimeout;
		return this;
	}
	
	public void renew() {
		activetime = System.currentTimeMillis();
	}
	
	public boolean isIdleTimeout() {
		return ((System.currentTimeMillis() - activetime) / 1000) >= idleTimeout;
	}
	
	public boolean isAliveTimeout() {
		return ((System.currentTimeMillis() - createtime) / 1000) >= aliveTimeout;
	}
}
