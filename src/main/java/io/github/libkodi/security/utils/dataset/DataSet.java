package io.github.libkodi.security.utils.dataset;

import java.util.Iterator;
import java.util.Map.Entry;

import io.github.libkodi.security.utils.objectlist.ObjectList;

/**
 * 数据集，用来管理一个数据列表并使其具备过期性质
 *
 * @param <T> 该数据集保存的数据类型
 */
public class DataSet<T> {
	private Object mutex;
	private ObjectList<String, DataNode<T>> value = new ObjectList<String, DataNode<T>>();
	
	public DataSet() {
		mutex = this;
	}
	
	/**
	 * 
	 * 添加一个数据
	 *
	 * @param key 键
	 * @param value 值
	 * @param idleTimeout 最大闲置时间
	 * @param aliveTimeout 最大存活时间
	 */
	public void put(String key, T value, int idleTimeout, int aliveTimeout) {
		synchronized (mutex) {
			DataNode<T> node = new DataNode<T>(value);
			node.setAliveTimeout(aliveTimeout);
			node.setIdleTimeout(idleTimeout);
			this.value.unshift(key, node);
		}
	}
	
	/**
	 * 
	 * 获取数据
	 *
	 * @param key 键
	 * @return Object
	 */
	public T get(String key) {
		synchronized (mutex) {
			DataNode<T> node = value.get(key);
			
			if (node != null) {
				node.renew();
				value.unshift(key, node);
				return node.getValue();
			}
			
			return null;
		}
	}
	
	/**
	 * 
	 * 是否为空
	 *
	 * @return true/false
	 */
	public boolean isEmpty() {
		synchronized (mutex) {
			return value.isEmpty();
		}
	}
	
	/**
	 * 清空列表
	 */
	public void clear() {
		synchronized (mutex) {
			value.clear();
		}
	}
	
	/**
	 * 
	 * 是否包含指定数据
	 *
	 * @param key 键
	 * @return true/false
	 */
	public boolean containsKey(String key) {
		synchronized (mutex) {
			return value.containsKey(key);
		}
	}
	
	/**
	 * 
	 * 移除数据
	 *
	 * @param key 键
	 */
	public void remove(String key) {
		synchronized (mutex) {
			value.remove(key);
		}
	}
	
	/**
	 * 
	 * 更新数据并移除过期数据
	 *
	 */
	public void update() {
		synchronized (mutex) {
			Iterator<Entry<String, DataNode<T>>> iter = value.iterator(true);
			
			while(iter.hasNext()) {
				Entry<String, DataNode<T>> entry = iter.next();
				DataNode<T> node = entry.getValue();
				
				if (node.isIdleTimeout() || node.isAliveTimeout()) {
					value.remove(entry.getKey());
				} else {
					break;
				}
			}
		}
	}
}
