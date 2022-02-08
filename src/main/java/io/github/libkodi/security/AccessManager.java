package io.github.libkodi.security;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.springframework.data.redis.core.RedisTemplate;

import io.github.libkodi.security.entity.Access;
import io.github.libkodi.security.interfaces.GetRemoteAddressHandle;
import io.github.libkodi.security.properties.AuthProperties;
import io.github.libkodi.security.utils.HttpRequestUtils;
import io.github.libkodi.security.utils.StringUtils;
import io.github.libkodi.security.utils.dataset.DataSet;

/**
 * 访问限制管理器
 * 记录每个IP规定时间内的请求次数并判断是否需要阻止该请求
 */
public class AccessManager {
	private static AccessManager instance;
	private AuthProperties properties;
	private RedisTemplate<String, Object> redis;
	private DataSet<Access> data = new DataSet<Access>();
	private GetRemoteAddressHandle getRemoteAddressHandle;
	private final Object mutex;
	
	/**
	 * 
	 * @param properties 属性配置
	 * @param redis redis模板
	 * @param getRemoteAddressHandle 获取IP的handle类
	 */
	private AccessManager(AuthProperties properties, RedisTemplate<String, Object> redis, GetRemoteAddressHandle getRemoteAddressHandle) {
		this.properties = properties;
		this.redis = redis;
		this.getRemoteAddressHandle = getRemoteAddressHandle;
		this.mutex = this;
	}
	
	/**
	 * 获取单例
	 * @param properties 属性配置
	 * @param redis redis模板
	 * @param getRemoteAddressHandle 获取IP的handle类
	 * @return AccessManager
	 */
	public static AccessManager getInstance(AuthProperties properties, RedisTemplate<String, Object> redis, GetRemoteAddressHandle getRemoteAddressHandle) {
		if (instance == null) {
			instance = new AccessManager(properties, redis, getRemoteAddressHandle);
		}
		
		return instance;
	}
	
	/**
	 * 刷新过期数据, 只有在没有配置redis的情况下才会实际调用
	 */
	public void update() {
		if (properties.getAccess().isEnable()) {
			data.update();
		}
	}
	
	/**
	 * 
	 * 验证该请求是否可以通过
	 *
	 * @param request 请求对象
	 * @return true/false
	 */
	public boolean verify(HttpServletRequest request) {
		synchronized (mutex) {
			try {
				if (!properties.getAccess().isEnable()) {
					return true;
				}
				
				String clientIp = getRemoteAddressHandle != null ? getRemoteAddressHandle.call(request) : HttpRequestUtils.getRemoteAddress(request);
				
				if (isBlocked(clientIp)) {
					return false;
				}
				
				increment(clientIp);
				
				return !checkBlocked(clientIp);
			} catch (Exception e) {
				return false;
			}
		}
	}
	
	/**
	 * 
	 * 检测该IP是否需要阻止
	 *
	 * @param ip 请求的ip地址
	 * @return true/false
	 */
	private boolean checkBlocked(String ip) {
		if (!properties.getRedis().isEnable()) {
			Access access = data.get(ip);
			
			if (access.getCount() > properties.getAccess().getLimit()) {
				access.setBlocked(true);
				data.remove(ip);
				data.put(ip, access, 0, properties.getAccess().getBlock());
				return true;
			} else {
				return false;
			}
		} else {
			int total = (int) redis.opsForValue().get(joinRedisKey(ip));
			
			if (total <= properties.getAccess().getLimit()) {
				return false;
			} else {
				redis.opsForValue().set(joinRedisKey(ip) + ":blocked", true, properties.getAccess().getBlock(), TimeUnit.SECONDS);
				redis.delete(joinRedisKey(ip));
				return true;
			}
		}
	}
	
	/**
	 *
	 * 检测该IP是否已经被阻止 
	 *
	 * @param ip 请求的ip地址
	 * @return true/false
	 */
	private boolean isBlocked(String ip) {
		if (!properties.getRedis().isEnable()) {
			if (data.containsKey(ip)) {
				return data.get(ip).isBlocked();
			} else {
				return false;
			}
		} else {
			return redis.hasKey(joinRedisKey(ip) + ":blocked");
		}
	}
	
	/**
	 * 
	 * 添加请求IP的访问记录数
	 *
	 * @param ip 请求的ip地址
	 * @return 当前请求次数
	 */
	private long increment(String ip) {
		if (!properties.getRedis().isEnable()) {
			long total = 0;
			
			if (data.containsKey(ip)) {
				total = data.get(ip).increment();
			} else {
				Access access = new Access();
				access.setIp(ip);
				total = access.increment();
				data.put(ip, access, 0, properties.getAccess().getRange());
			}
			
			return total;
		} else {
			String rkey = joinRedisKey(ip);
			boolean first = false;
			
			if (!redis.hasKey(rkey)) {
				first = true;
			}
			
			long total = redis.opsForValue().increment(rkey);
			
			if (first) {
				redis.expire(rkey, properties.getAccess().getRange(), TimeUnit.SECONDS);
			}
			
			return total;
		}
	}
	
	/**
	 * 
	 * 拼接一个redis上存储用的key
	 *
	 * @param key 键名
	 * @return 新键名
	 */
	private String joinRedisKey(String key) {
		return StringUtils.join(":", properties.getRedis().getPrefix(), "ip", key);
	}

}
