package io.github.libkodi.security;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import io.github.libkodi.security.interfaces.Cache;
import io.github.libkodi.security.properties.AuthProperties;
import io.github.libkodi.security.utils.StringUtils;
import io.github.libkodi.security.utils.dataset.DataSet;

public class CacheManager implements Serializable {
	private static final long serialVersionUID = 6536390743435435098L;
	private static CacheManager instance = null;
	private ThreadLocal<JSONObject> threadVars = new ThreadLocal<JSONObject>();
	private RedisTemplate<String, Object> redis;
	private AuthProperties properties;
	final Object mutex;
	private DataSet<Cache> caches = new DataSet<Cache>();
	private HashMap<String, Object> envVars = new HashMap<String, Object>();
	
	public static CacheManager getInstance(AuthProperties properties, RedisTemplate<String, Object> redis) {
		if (instance == null) {
			instance = new CacheManager(properties, redis);
		}
		
		return instance;
	}
	
	private CacheManager(AuthProperties properties, RedisTemplate<String, Object> redis) {
		this.mutex = this;
		this.properties = properties;
		this.redis = redis;
	}
	
	/**
	 * 
	 *  线程变量操作
	 * 
	 */
	public void addThreadVar(String key, Object value) {
		JSONObject data = threadVars.get();
		
		if (data == null) {
			data = new JSONObject();
			threadVars.set(data);
		}
		
		data.put(key, value);
	}
	
	public Object getThreadVar(String key) {
		JSONObject data = threadVars.get();
		
		if (data == null) {
			return null;
		} else {
			return data.get(key);
		}
	}
	
	public <T> T getThreadVar(String key, Class<T> clazz) {
		JSONObject data = threadVars.get();
		
		if (data == null) {
			return null;
		} else {
			return data.getObject(key, clazz);
		}
	}
	
	public Object removeThreadVar(String key) {
		JSONObject data = threadVars.get();
		
		if (data == null) {
			return null;
		} else {
			return data.remove(key);
		}
	}
	
	public JSONObject deleteAllThreadVars() {
		JSONObject data = threadVars.get();
		
		if (data != null) {
			threadVars.remove();
		}
		
		return data;
	}
	
	/**
	 * 
	 * 其它变量操作
	 * 
	 */
	public void addEnvVar(String key, Object value) {
		synchronized (mutex) {
			if (properties.getRedis().isEnable()) {
				redis.opsForValue().set(addSuffix(key), value);
			} else {
				envVars.put(key, value);
			}
		}
	}
	
	public Object getEnvVar(String key) {
		synchronized (mutex) {
			if (properties.getRedis().isEnable()) {
				return redis.opsForValue().get(addSuffix(key));
			} else {
				return envVars.get(key);
			}
		}
	}
	
	public void removeEnvVar(String key) {
		synchronized (mutex) {
			if (properties.getRedis().isEnable()) {
				redis.delete(addSuffix(key));
			} else {
				envVars.remove(key);
			}
		}
	}
	
	public void update() {
		if (!properties.getRedis().isEnable()) {
			caches.update();
		}
	}
	
	public boolean contains(String CacheId) {
		synchronized (mutex) {
			if (properties.getRedis().isEnable()) {
				return redis.hasKey(addSuffix(CacheId));
			} else {
				return caches.containsKey(CacheId);
			}
		}
	}
	
	public String randomKey() {
		return UUID.randomUUID().toString().replace("-", "");
	}
	
	private String addSuffix(String key) {
		String suffix = properties.getRedis().getPrefix();
		
		if (StringUtils.isEmpty(suffix)) {
			return key;
		} else {
			return suffix + ":" + key;
		}
	}
	
	public int getMaxIdle() {
		return properties.getCache().getMaxIdle();
	}
	
	public int getMaxAlive() {
		return properties.getCache().getMaxAlive();
	}
	
	public Cache getCache(String cacheId) {
		return create(cacheId, 0, 0, false);
	}
	
	public Cache create(int idleTimeout, int aliveTimeout, boolean creatable) {
		return create(randomKey(), idleTimeout, aliveTimeout, creatable);
	}
	
	public Cache create(int idleTimeout, int aliveTimeout) {
		return create(idleTimeout, aliveTimeout, true);
	}
	
	public Cache create(int aliveTimeout) {
		return create(0, aliveTimeout, true);
	}
	
	public Cache create() {
		return create(getMaxIdle(), getMaxAlive(), true);
	}

	public Cache create(String cacheId, int idleTimeout, int maxAliveTimeout, boolean creatable) {
		synchronized (mutex) {
			Cache cache;
			
			boolean hasCache = contains(cacheId);
			
			if (!hasCache && !creatable) {
				return null;
			} else if (hasCache && !properties.getRedis().isEnable()) {
				return caches.get(cacheId);
			}
			
			cache = new Cache() {
				private Map<String, Object> map = Collections.synchronizedMap(new HashMap<String, Object>());
				
				@Override
				public void setRoles(String[] values) {
					put("$roles", JSON.toJSONString(values));
				}
				
				@Override
				public void setPermissions(String[] values) {
					put("$permissions", JSON.toJSONString(values));
				}
				
				@Override
				public void put(String key, Object value) {
					if (properties.getRedis().isEnable()) {
						redis.opsForHash().put(addSuffix(cacheId), key, value);
					} else {
						map.put(key, value);
					}
				}
				
				@Override
				public void remove(String key) {
					if (properties.getRedis().isEnable()) {
						redis.opsForHash().delete(addSuffix(cacheId), key);
					} else {
						map.remove(key);
					}
				}
				
				@Override
				public Set<String> keys() {
					if (properties.getRedis().isEnable()) {
						return redis.keys(properties.getRedis().getPrefix() + "*");
					} else {
						return map.keySet();
					}
				}
				
				@Override
				public String getCacheId() {
					return cacheId;
				}
				
				@SuppressWarnings("unchecked")
				@Override
				public <T> T get(String key, Class<T> clazz) {
					return (T) get(key);
				}
				
				@Override
				public Object get(String key) {
					if (properties.getRedis().isEnable()) {
						return redis.opsForHash().get(addSuffix(cacheId), key);
					} else {
						return map.get(key);
					}
				}
				
				@Override
				public String[] getRoles() {
					String roles = get("$roles", String.class);
					
					if (roles != null) {
						return JSON.parseArray(roles).toArray(new String[0]);
					} else {
						return new String[0];
					}
				}
				
				@Override
				public String[] getPermissions() {
					String permissions = get("$permissions", String.class);
					
					if (permissions != null) {
						return JSON.parseArray(permissions).toArray(new String[0]);
					} else {
						return new String[0];
					}
				}
				
				@Override
				public void destory() {
					if (properties.getRedis().isEnable()) {
						redis.delete(addSuffix(cacheId));
					} else {
						caches.remove(cacheId);
					}
				}
				@Override
				public void renew() {
					if (properties.getRedis().isEnable()) {
						Integer idle = get("$idleTimeout", Integer.class);
						idle = idle == null ? idleTimeout : idle;
						
						if (idle > 0) {
							redis.expire(addSuffix(cacheId), idle, TimeUnit.SECONDS);
						}
					}
				}
			};
			
			if (!hasCache) {
				if (properties.getRedis().isEnable()) {
					cache.put("$createtime", System.currentTimeMillis());
					cache.put("$aliveTimeout", maxAliveTimeout);
					cache.put("$idleTimeout", idleTimeout);
					
					if (idleTimeout < 1) {
						redis.expire(addSuffix(cacheId), maxAliveTimeout, TimeUnit.SECONDS);
					} else {
						cache.renew();
					}
				} else {
					caches.put(cacheId, cache, idleTimeout, maxAliveTimeout);
				}
			} else if (properties.getRedis().isEnable()) {
				Long createtime = cache.get("$createtime", Long.class);
				Integer maxAlive = cache.get("$aliveTimeout", Integer.class);
				
				if (createtime == null || maxAlive == null) {
					return null;
				}
				
				if (((System.currentTimeMillis() - createtime) / 1000) >= maxAlive) {
					cache.destory();
					return null;
				} else {
					cache.renew();
				}
			}
			
			return cache;
		}
	}

}
