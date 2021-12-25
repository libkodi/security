package io.github.libkodi.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.fastjson.JSONObject;

import io.github.libkodi.security.interfaces.Cache;
import io.github.libkodi.security.properties.AuthProperties;
import io.github.libkodi.security.utils.OldMetabolicDataSet;
import io.github.libkodi.security.utils.StringUtils;

public class CacheManager implements Serializable {
	private static final long serialVersionUID = 6536390743435435098L;
	private static CacheManager instance = null;
	private ThreadLocal<JSONObject> tempVars = new ThreadLocal<JSONObject>();
	private RedisTemplate<String, Object> redis;
	private AuthProperties properties;
	final Object mutex;
	private OldMetabolicDataSet<Cache> caches = new OldMetabolicDataSet<Cache>();
	private HashMap<String, Object> vars = new HashMap<String, Object>();
	
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
	public void put(String key, Object value) {
		JSONObject data = tempVars.get();
		
		if (data == null) {
			data = new JSONObject();
			tempVars.set(data);
		}
		
		data.put(key, value);
	}
	
	public Object get(String key) {
		JSONObject data = tempVars.get();
		
		if (data == null) {
			return null;
		} else {
			return data.get(key);
		}
	}
	
	public <T> T get(String key, Class<T> clazz) {
		JSONObject data = tempVars.get();
		
		if (data == null) {
			return null;
		} else {
			return data.getObject(key, clazz);
		}
	}
	
	public Object remove(String key) {
		JSONObject data = tempVars.get();
		
		if (data == null) {
			return null;
		} else {
			return data.remove(key);
		}
	}
	
	public JSONObject delete() {
		JSONObject data = tempVars.get();
		
		if (data != null) {
			tempVars.remove();
		}
		
		return data;
	}
	
	/**
	 * 
	 * 其它变量操作
	 * 
	 */
	public void addEnvironment(String key, Object value) {
		synchronized (mutex) {
			if (properties.isRedisEnable()) {
				redis.opsForValue().set(addSuffix(key), value);
			} else {
				vars.put(key, value);
			}
		}
	}
	
	public Object getEnvironment(String key) {
		synchronized (mutex) {
			if (properties.isRedisEnable()) {
				return redis.opsForValue().get(addSuffix(key));
			} else {
				return vars.get(key);
			}
		}
	}
	
	public void removeEnvironment(String key) {
		synchronized (mutex) {
			if (properties.isRedisEnable()) {
				redis.delete(addSuffix(key));
			} else {
				vars.remove(key);
			}
		}
	}
	
	public void update() {
		Iterator<Cache> iter = caches.iterator();
		ArrayList<String> keys = new ArrayList<String>();
		
		while (iter.hasNext()) {
			Cache cache = iter.next();
			
			if (cache.isInvalid()) {
				keys.add(cache.getCacheId());
			} else {
				break;
			}
		}
		
		for (String key : keys) {
			caches.remove(key);
		}
	}
	
	public boolean contains(String CacheId) {
		if (properties.isRedisEnable()) {
			return redis.hasKey(addSuffix(CacheId));
		} else {
			return caches.containsKey(CacheId);
		}
	}
	
	public Cache instance() {
		return instance(getUUID(), properties.getCacheIdleTimeout(), properties.getCacheMaxAlive(), true);
	}
	
	public Cache instance(int expires) {
		return instance(getUUID(), 0, expires, true);
	}
	
	private String getUUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}
	
	private String addSuffix(String key) {
		String suffix = properties.getRedisKeySuffix();
		
		if (StringUtils.isEmpty(suffix)) {
			return key;
		} else {
			return suffix + ":" + key;
		}
	}
	
	public Cache instance(String CacheId) {
		return instance(CacheId, true);
	}
	
	public Cache instance(String CacheId, boolean creatable) {
		return instance(CacheId, properties.getCacheIdleTimeout(), properties.getCacheMaxAlive(), creatable);
	}
	
	public Cache instance(String cacheId, int expires) {
		return instance(cacheId, expires, true);
	}
	
	public Cache instance(String CacheId, int expires, boolean creatable) {
		return instance(CacheId, 0, expires, creatable);
	}
	
	public Cache instance(int idleTimeout, int maxAliveTimeout) {
		return instance(idleTimeout, maxAliveTimeout, true);
	}
	
	public Cache instance(int idleTimeout, int maxAliveTimeout, boolean creatable) {
		return instance(getUUID(), idleTimeout, maxAliveTimeout, creatable);
	}
	
	public Cache instance(String cacheId, int idleTimeout, int maxAliveTimeout) {
		return instance(cacheId, idleTimeout, maxAliveTimeout, true);
	}

	public Cache instance(String cacheId, int idleTimeout, int maxAliveTimeout, boolean creatable) {
		synchronized (mutex) {
			Cache cache;
			
			boolean hasCache = contains(cacheId);
			
			if (!hasCache && !creatable) {
				return null;
			} else if (!properties.isRedisEnable()) {
				cache = caches.get(cacheId);
				
				if (cache != null) {
					cache.renew();
					return cache;
				} else if (!creatable) {
					return null;
				}
			}
			
			cache = new Cache() {
				private Map<String, Object> map = Collections.synchronizedMap(new HashMap<String, Object>());
				
				@Override
				public void setRoles(String[] values) {
					put("roles", values);
				}
				
				@Override
				public void setPermissions(String[] values) {
					put("permissions", values);
				}
				
				@Override
				public void put(String key, Object value) {
					if (properties.isRedisEnable()) {
						redis.opsForHash().put(addSuffix(cacheId), key, value);
					} else {
						map.put(key, value);
					}
				}
				
				@Override
				public void remove(String key) {
					if (properties.isRedisEnable()) {
						redis.opsForHash().delete(addSuffix(cacheId), key);
					} else {
						map.remove(key);
					}
				}
				
				@Override
				public Set<String> keys() {
					if (properties.isRedisEnable()) {
						return redis.keys(properties.getRedisKeySuffix() + "*");
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
					if (properties.isRedisEnable()) {
						return redis.opsForHash().get(addSuffix(cacheId), key);
					} else {
						return map.get(key);
					}
				}
				
				@Override
				public String[] getRoles() {
					return (String[]) get("roles");
				}
				
				@Override
				public String[] getPermissions() {
					return (String[]) get("permissions");
				}
				
				@Override
				public void destory() {
					if (properties.isRedisEnable()) {
						redis.delete(addSuffix(cacheId));
					} else {
						caches.remove(cacheId);
					}
				}
				
				private int getIdleTimeout() {
					int idle;
					Object idle_timeout = get("$idle_timeout");
					
					if (idle_timeout == null) {
						return 0;
					} else {
						idle = (int) idle_timeout;
					}
					
					return idle;
				}
				
				private int getMaxAliveTimeout() {
					int alive;
					Object alive_timeout = get("$alive_timeout");
					
					if (alive_timeout == null) {
						return 0;
					} else {
						alive = (int) alive_timeout;
					}
					
					return alive;
				}
				
				@Override
				public void renew() {
					int idle = getIdleTimeout();
					
					if (idle > 0) {
						if (properties.isRedisEnable()) {
							redis.expire(addSuffix(cacheId), idle, TimeUnit.SECONDS);
						} else {
							map.put("$activetime", System.currentTimeMillis());
						}
					}
				}

				@Override
				public boolean isInvalid() {
					if (properties.isRedisEnable()) {
						if (!redis.hasKey(addSuffix(cacheId))) {
							return true;
						} else {
							return false;
						}
					} else {
						int idle = getIdleTimeout();
						int maxAlive = getMaxAliveTimeout();
						long createtime = (long) map.get("$createtime");
						long activetime = idle > 0 ? (long) map.get("$activetime") : 0;
						long now = System.currentTimeMillis();
						
						if (idle > 0 && ((now - activetime) / 1000) >= idle) {
							return true;
						} else if (maxAlive > 0 && ((now - createtime) / 1000) >= maxAlive) {
							return true;
						} else {
							return false;
						}
					}
				}
			};
			
			if (!hasCache) {
				cache.put("$createtime", System.currentTimeMillis());
				cache.put("$idle_timeout", idleTimeout);
				cache.put("$alive_timeout", maxAliveTimeout);
				
				if (properties.isRedisEnable()) {
					if (idleTimeout < 1) {
						redis.expire(addSuffix(cacheId), maxAliveTimeout, TimeUnit.SECONDS);
					} else {
						cache.renew();
					}
				} else {
					cache.put("$activetime", System.currentTimeMillis());
				}
				
				if (!properties.isRedisEnable()) {
					caches.put(cacheId, cache);
				}
			} else if (properties.isRedisEnable()) {
				long createtime = cache.get("$createtime", long.class);
				int maxAlive = cache.get("$alive_timeout", int.class);
				
				if ((System.currentTimeMillis() - createtime) / 1000 >= maxAlive) {
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
