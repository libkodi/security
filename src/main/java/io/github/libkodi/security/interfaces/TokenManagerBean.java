package io.github.libkodi.security.interfaces;

import javax.servlet.http.HttpServletRequest;

/**
 * Token管理器接口
 */
public interface TokenManagerBean {
	/**
	 * 
	 * 创建一个token
	 *
	 * @param maxIdle 最大闲置时间
	 * @param maxAlive 最大存活时间
	 *
	 * @return token字符串
	 * @throws Exception
	 */
	public String create(int maxIdle, int maxAlive) throws Exception;
	
	/**
	 * 
	 * 创建一个token
	 *
	 * @param maxAlive 最大存活时间
	 *
	 * @return token字符串
	 * @throws Exception
	 */
	public String create(int maxAlive) throws Exception;
	/**
	 * 判断该请求是否包含token
	 * @return true/false
	 */
	public boolean hasToken(HttpServletRequest request);
	/**
	 * 从请求对象中获取缓存实例
	 * @return Cache
	 */
	public Cache getCache(HttpServletRequest request);
}
