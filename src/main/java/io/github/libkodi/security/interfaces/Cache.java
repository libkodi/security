package io.github.libkodi.security.interfaces;

import java.util.Set;

public interface Cache {
	/**
	 * 获取该缓存ID
	 */
	public String getCacheId();
	/**
	 * 
	 * 向该缓存中添加数据
	 *
	 * @param key 键名
	 * @param value 值
	 */
	public void put(String key, Object value);
	/**
	 * 
	 * 从缓存中读取数据
	 *
	 * @param key 键名
	 * @return Object
	 */
	public Object get(String key);
	/**
	 * 
	 * 从缓存中读取数据并转换类型
	 *
	 * @param key 键名
	 * @param clazz 需要转换的类型
	 * @return 返回转换后的结果
	 */
	public <T> T get(String key, Class<T> clazz);
	/**
	 * 
	 * 获取当前缓存中的所有key
	 *
	 * @return key的集合
	 */
	public Set<String> keys();
	/**
	 * 
	 * 移除指定的数据
	 *
	 * @param key 键名
	 */
	public void remove(String key);
	/**
	 * 
	 * 向该缓存中添加roles信息, 用于用户访问控制
	 *
	 * @param values 规则组
	 */
	public void setRoles(String[] values);
	/**
	 * 
	 * 向该缓存中添加permissions信息, 用于用户访问控制
	 *
	 * @param values 许可列表
	 */
	public void setPermissions(String[] values);
	/**
	 * 从缓存中获取roles信息
	 */
	public String[] getRoles();
	/**
	 * 从缓存中获取permissions信息
	 */
	public String[] getPermissions();
	/**
	 * 
	 * 注销该缓存
	 *
	 */
	public void destory();
	/**
	 * 
	 * 更新该缓存的活动时间, 只有在配置了redis的情况下工作
	 *
	 */
	public void renew();
}
