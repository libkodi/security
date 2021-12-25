package io.github.libkodi.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.springframework.data.redis.core.RedisTemplate;

import io.github.libkodi.security.interfaces.GetRemoteAddressHandle;
import io.github.libkodi.security.properties.AuthProperties;
import io.github.libkodi.security.utils.HttpRequestUtils;
import io.github.libkodi.security.utils.StringUtils;

public class AccessManager {
	private static AccessManager instance;
	private AuthProperties properties;
	private RedisTemplate<String, Object> redis;
	private HashMap<String, HashMap<String, Object>> map = new HashMap<String, HashMap<String, Object>>();
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
		if (!properties.isIpLimitEnable()) {
			return;
		}
		
		try {
			ArrayList<String> keys = new ArrayList<String>();
			
			synchronized (mutex) {
				Collections.addAll(keys, map.keySet().toArray(new String[0]));
			}
			
			if (keys.size() > 0) {
				for (String key : keys) {
					try {
						if (map.containsKey(key)) {
							HashMap<String, Object> m = map.get(key);
							boolean blocked = (boolean) m.get("blocked");
							long createtime = (long) m.get("createtime");
							long now = System.currentTimeMillis() / 1000;
							
							if (!blocked && (now - createtime) >= properties.getIpLimitSeconds()) {
								synchronized (mutex) {
									map.remove(key);
								}
							} else if (blocked) {
								long blockedtime = (long) m.get("blockedtime");
								
								if (now - blockedtime >= properties.getIpBlockSeconds()) {
									synchronized (mutex) {
										map.remove(key);
									}
								}
							}
						}
					} catch (Exception e) {
						synchronized (mutex) {
							map.remove(key);
						}
					}
				}
			}
		} catch (Exception e) {}
	}

	public boolean verify(HttpServletRequest request) {
		synchronized (mutex) {
			try {
				if (!properties.isIpLimitEnable()) {
					return true;
				}
				
				String clientIp = getRemoteAddressHandle != null ? getRemoteAddressHandle.call(request) : HttpRequestUtils.getRemoteAddress(request);
				
				if (isBlocked(clientIp)) {
					return false;
				}
				
				increment(clientIp);
				
				return checkBlocked(clientIp);
			} catch (Exception e) {
				return false;
			}
		}
	}

	private boolean checkBlocked(String key) {
		if (!properties.isRedisEnable()) {
			HashMap<String, Object> m = map.get(key);
			long createtime = (long) m.get("createtime");
			long total = (long) m.get("count");
			long now = System.currentTimeMillis() / 1000;
			
			if (now - createtime < properties.getIpLimitSeconds() && total <= properties.getIpLimit()) {
				return true;
			} else {
				m.put("blocked", true);
				m.put("blockedtime", System.currentTimeMillis() / 1000);
				return false;
			}
		} else {
			int total = (int) redis.opsForValue().get(joinRedisKey(key));
			
			if (total <= properties.getIpLimit()) {
				return true;
			} else {
				redis.opsForValue().set(joinRedisKey(key) + ":blocked", true, properties.getIpBlockSeconds(), TimeUnit.SECONDS);
				redis.delete(joinRedisKey(key));
				return false;
			}
		}
	}

	private boolean isBlocked(String key) {
		if (!properties.isRedisEnable()) {
			if (map.containsKey(key)) {
				return (boolean) map.get(key).get("blocked");
			} else {
				return false;
			}
		} else {
			return redis.hasKey(joinRedisKey(key) + ":blocked");
		}
	}

	private long increment(String key) {
		if (!properties.isRedisEnable()) {
			long total = 0;
			
			if (map.containsKey(key)) {
				HashMap<String, Object> m = map.get(key);
				total = (long) m.get("count");
			} else {
				HashMap<String, Object> m = new HashMap<String, Object>();
				m.put("createtime", System.currentTimeMillis() / 1000);
				m.put("blocked", false);
				map.put(key, m);
			}
			
			total += 1;
			
			map.get(key).put("count", total);
			
			return total;
		} else {
			String rkey = joinRedisKey(key);
			boolean first = false;
			
			if (!redis.hasKey(rkey)) {
				first = true;
			}
			
			long total = redis.opsForValue().increment(rkey);
			
			if (first) {
				redis.expire(rkey, properties.getIpLimitSeconds(), TimeUnit.SECONDS);
			}
			
			return total;
		}
	}

	private String joinRedisKey(String key) {
		return StringUtils.join(":", properties.getRedisKeySuffix(), "ip", key);
	}

}
