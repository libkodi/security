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

import io.github.libkodi.objectlist.withexpires.PeriodMap;
import io.github.libkodi.security.interfaces.Cache;
import io.github.libkodi.security.properties.AuthProperties;
import io.github.libkodi.security.utils.StringUtils;

/**
 * 缓存管理器
 */
public class CacheManager implements Serializable {
	private static final long serialVersionUID = 6536390743435435098L;
	private static CacheManager instance = null;
	private ThreadLocal<JSONObject> threadVars = new ThreadLocal<JSONObject>();
	private RedisTemplate<String, Object> redis;
	private AuthProperties properties;
	final Object mutex;
	private PeriodMap<Cache> caches = new PeriodMap<Cache>();
	private HashMap<String, Object> envVars = new HashMap<String, Object>();
	
	/**
	 * 
	 * @param properties 属性配置
	 * @param redis redis模板
	 * @return CacheManager
	 */
	public static CacheManager getInstance(AuthProperties properties, RedisTemplate<String, Object> redis) {
		if (instance == null) {
			instance = new CacheManager(properties, redis);
		}
		
		return instance;
	}
	
	/**
	 * 
	 * @param properties 属性配置
	 * @param redis redis模板
	 */
	private CacheManager(AuthProperties properties, RedisTemplate<String, Object> redis) {
		this.mutex = this;
		this.properties = properties;
		this.redis = redis;
	}
	
	/**
	 * 
	 * 添加本地线程变量
	 *
	 * @param key 键
	 * @param value 值
	 */
	public void addThreadVar(String key, Object value) {
		synchronized (mutex) {
			JSONObject data = threadVars.get();
			
			if (data == null) {
				data = new JSONObject();
				threadVars.set(data);
			}
			
			data.put(key, value);
		}
	}
	
	/**
	 * 
	 * 获取本地线程变量
	 *
	 * @param key 键
	 * @return Object
	 */
	public Object getThreadVar(String key) {
		synchronized (mutex) {
			JSONObject data = threadVars.get();
			
			if (data == null) {
				return null;
			} else {
				return data.get(key);
			}
		}
	}
	
	/**
	 * 
	 * 获取本地线程变量并转换类型
	 *
	 * @param key 键
	 * @param clazz 需要转换的输出类型
	 * @return 类型转换后的结果
	 */
	public <T> T getThreadVar(String key, Class<T> clazz) {
		JSONObject data = threadVars.get();
		
		if (data == null) {
			return null;
		} else {
			return data.getObject(key, clazz);
		}
	}
	
	/**
	 * 
	 * 删除本地线程变量
	 *
	 * @param key 键
	 * @return 当前删除的key的value
	 */
	public Object removeThreadVar(String key) {
		synchronized (mutex) {
			JSONObject data = threadVars.get();
			
			if (data == null) {
				return null;
			} else {
				return data.remove(key);
			}
		}
	}
	
	/**
	 * 	
	 * 删除当前线程绑定的所有变量
	 *
	 */
	public void deleteAllThreadVars() {
		JSONObject data = threadVars.get();
		
		if (data != null) {
			threadVars.remove();
		}
	}
	
	/**
	 * 
	 * 添加环境变量，主要用于redis(永久的保存在本地，项目重启也不会丢失)
	 *
	 * @param key 键
	 * @param value 值
	 */
	public void addEnvVar(String key, Object value) {
		synchronized (mutex) {
			if (properties.getRedis().isEnable()) {
				redis.opsForValue().set(addPrefix(key), value);
			} else {
				envVars.put(key, value);
			}
		}
	}
	
	/**
	 * 
	 * 获取环境变量
	 *
	 * @param key 键
	 * @return Object
	 */
	public Object getEnvVar(String key) {
		synchronized (mutex) {
			if (properties.getRedis().isEnable()) {
				return redis.opsForValue().get(addPrefix(key));
			} else {
				return envVars.get(key);
			}
		}
	}
	
	/**
	 * 
	 * 删除环境变量
	 *
	 * @param key 键
	 */
	public void removeEnvVar(String key) {
		synchronized (mutex) {
			if (properties.getRedis().isEnable()) {
				redis.delete(addPrefix(key));
			} else {
				envVars.remove(key);
			}
		}
	}
		
	/**
	 * 刷新过期数据, 只有在没有配置redis的情况下才会实际调用
	 */
	public void update() {
		if (!properties.getRedis().isEnable()) {
			caches.update();
		}
	}
	
	/**
	 * 
	 * 是否包含指定的缓存
	 *
	 * @param CacheId 缓存ID
	 * @return true/false
	 */
	public boolean contains(String CacheId) {
		synchronized (mutex) {
			if (properties.getRedis().isEnable()) {
				return redis.hasKey(addPrefix(CacheId));
			} else {
				return caches.containsKey(CacheId);
			}
		}
	}
	
	/**
	 * 
	 * 生成一个随机的缓存ID
	 *
	 * @return 缓存ID
	 */
	private String randomKey() {
		return UUID.randomUUID().toString().replace("-", "");
	}
	
	/**
	 * 
	 * 为redis存储的key添加前缀
	 *
	 * @param key 键名
	 * @return 新键名
	 */
	private String addPrefix(String key) {
		String suffix = properties.getRedis().getPrefix();
		
		if (StringUtils.isEmpty(suffix)) {
			return key;
		} else {
			return suffix + ":" + key;
		}
	}
	
	/**
	 * 
	 * 从配置中读取最大闲置时间
	 * 
	 */
	public int getMaxIdle() {
		return properties.getCache().getMaxIdle();
	}
	
	/**
	 * 
	 * 从配置中读取最大存活时间
	 *
	 */
	public int getMaxAlive() {
		return properties.getCache().getMaxAlive();
	}
	
	/**
	 * 
	 * 获取缓存
	 *
	 * @param cacheId 缓存ID
	 * @return 缓存实例
	 */
	public Cache getCache(String cacheId) {
		return create(cacheId, 0, 0, false);
	}
	
	/**
	 * 
	 * 创建一个缓存
	 *
	 * @param idleTimeout 最大闲置时间
	 * @param aliveTimeout 最大存活时间
	 * @param creatable 不存在该ID时，是否可以自动创建
	 * @return 缓存实例
	 */
	public Cache create(int idleTimeout, int aliveTimeout, boolean creatable) {
		return create(randomKey(), idleTimeout, aliveTimeout, creatable);
	}
	
	/**
	 * 
	 * 创建一个缓存
	 *
	 * @param idleTimeout 最大闲置时间
	 * @param aliveTimeout 最大存活时间
	 * @return 缓存实例
	 */
	public Cache create(int idleTimeout, int aliveTimeout) {
		return create(idleTimeout, aliveTimeout, true);
	}
	
	/**
	 * 
	 * 创建一个缓存
	 *
	 * @param aliveTimeout 最大存活时间
	 * @return 缓存实例
	 */
	public Cache create(int aliveTimeout) {
		return create(0, aliveTimeout, true);
	}
	
	/**
	 * 
	 * 以配置文件来创建一个缓存
	 *
	 * @return 缓存实例
	 */
	public Cache create() {
		return create(getMaxIdle(), getMaxAlive(), true);
	}
	
	/**
	 * 
	 * 创建或获取一个缓存
	 *
	 * @param cacheId 缓存ID
	 * @param idleTimeout 最大闲置时间
	 * @param maxAliveTimeout 最大存活时间
	 * @param creatable 不存在该ID时，是否可以自动创建
	 * @return 缓存实例
	 */
	public Cache create(String cacheId, int idleTimeout, int maxAliveTimeout, boolean creatable) {
		synchronized (mutex) {
			Cache cache;
			
			boolean hasCache = contains(cacheId);
			
			if (!hasCache && !creatable) {
				// 如果不存在该缓存，并且不允许自动创建
				return null;
			} else if (hasCache && !properties.getRedis().isEnable()) {
				// 有该缓存，但没有配置开启redis
				return caches.get(cacheId);
			}
			
			/**
			 * 创建一个Cache实例
			 */
			cache = new Cache() {
				private Map<String, Object> map = Collections.synchronizedMap(new HashMap<String, Object>());
				
				@Override
				public void setRoles(String[] values) {
					if (values != null) {
						put("$roles", JSON.toJSONString(values));
					}
				}
				
				@Override
				public void setPermissions(String[] values) {
					if (values != null) {
						put("$permissions", JSON.toJSONString(values));
					}
				}
				
				@Override
				public void put(String key, Object value) {
					if (properties.getRedis().isEnable()) {
						redis.opsForHash().put(addPrefix(cacheId), key, value);
					} else {
						map.put(key, value);
					}
				}
				
				@Override
				public void remove(String key) {
					if (properties.getRedis().isEnable()) {
						redis.opsForHash().delete(addPrefix(cacheId), key);
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
						return redis.opsForHash().get(addPrefix(cacheId), key);
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
						redis.delete(addPrefix(cacheId));
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
							redis.expire(addPrefix(cacheId), idle, TimeUnit.SECONDS);
						}
					}
				}
			};
			
			if (!hasCache) {
				// 如果是第一次创建
				if (properties.getRedis().isEnable()) {
					// 记录下创建时的一些信息
					cache.put("$createtime", System.currentTimeMillis());
					cache.put("$aliveTimeout", maxAliveTimeout);
					cache.put("$idleTimeout", idleTimeout);
					
					if (idleTimeout < 1) { 
						// 如果没有指定闲置时间，则直接指定最大存活时间
						redis.expire(addPrefix(cacheId), maxAliveTimeout, TimeUnit.SECONDS);
					} else {
						// 更新活动时间
						cache.renew();
					}
				} else {
					// 向缓存组中添加当前创建的缓存
					caches.put(cacheId, cache, idleTimeout, maxAliveTimeout);
				}
			} else if (properties.getRedis().isEnable()) {
				// 如果开启了redis并且不是第一次创建，则判断是否已经过了最大存活时间
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
