package io.github.libkodi.security.interfaces;

import java.util.Set;

public interface Cache {
	public String getCacheId();
	public void put(String key, Object value);
	public Object get(String key);
	public <T> T get(String key, Class<T> clazz);
	public Set<String> keys();
	public void remove(String key);
	public void setRoles(String[] values);
	public void setPermissions(String[] values);
	public String[] getRoles();
	public String[] getPermissions();
	public void destory();
	public void renew();
	public boolean isInvalid();
}
