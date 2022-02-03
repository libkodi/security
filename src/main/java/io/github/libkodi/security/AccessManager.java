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

public class AccessManager {
	private static AccessManager instance;
	private AuthProperties properties;
	private RedisTemplate<String, Object> redis;
	private DataSet<Access> data = new DataSet<Access>();
	private GetRemoteAddressHandle getRemoteAddressHandle;
	private final Object mutex;
	
	public AccessManager(AuthProperties properties, RedisTemplate<String, Object> redis, GetRemoteAddressHandle getRemoteAddressHandle) {
		this.properties = properties;
		this.redis = redis;
		this.getRemoteAddressHandle = getRemoteAddressHandle;
		this.mutex = this;
	}

	public static AccessManager getInstance(AuthProperties properties, RedisTemplate<String, Object> redis, GetRemoteAddressHandle getRemoteAddressHandle) {
		if (instance == null) {
			instance = new AccessManager(properties, redis, getRemoteAddressHandle);
		}
		
		return instance;
	}

	public void update() {
		if (properties.getAccess().isEnable()) {
			data.update();
		}
	}

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

	private boolean checkBlocked(String key) {
		if (!properties.getRedis().isEnable()) {
			Access access = data.get(key);
			
			if (access.getCount() > properties.getAccess().getLimit()) {
				access.setBlocked(true);
				data.remove(key);
				data.put(key, access, 0, properties.getAccess().getBlock());
				return true;
			} else {
				return false;
			}
		} else {
			int total = (int) redis.opsForValue().get(joinRedisKey(key));
			
			if (total <= properties.getAccess().getLimit()) {
				return false;
			} else {
				redis.opsForValue().set(joinRedisKey(key) + ":blocked", true, properties.getAccess().getBlock(), TimeUnit.SECONDS);
				redis.delete(joinRedisKey(key));
				return true;
			}
		}
	}

	private boolean isBlocked(String key) {
		if (!properties.getRedis().isEnable()) {
			if (data.containsKey(key)) {
				return data.get(key).isBlocked();
			} else {
				return false;
			}
		} else {
			return redis.hasKey(joinRedisKey(key) + ":blocked");
		}
	}

	private long increment(String key) {
		if (!properties.getRedis().isEnable()) {
			long total = 0;
			
			if (data.containsKey(key)) {
				total = data.get(key).increment();
			} else {
				Access access = new Access();
				access.setIp(key);
				total = access.increment();
				data.put(key, access, 0, properties.getAccess().getRange());
			}
			
			return total;
		} else {
			String rkey = joinRedisKey(key);
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

	private String joinRedisKey(String key) {
		return StringUtils.join(":", properties.getRedis().getPrefix(), "ip", key);
	}

}
