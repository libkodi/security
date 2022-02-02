package io.github.libkodi.security.utils.dataset;

import java.util.Iterator;
import java.util.Map.Entry;

import io.github.libkodi.security.utils.objectlist.ObjectList;

public class DataSet<T> {
	private Object mutex;
	private ObjectList<String, DataNode<T>> value = new ObjectList<String, DataNode<T>>();
	
	public DataSet() {
		mutex = this;
	}
	
	public void put(String key, T value, int idleTimeout, int aliveTimeout) {
		synchronized (mutex) {
			DataNode<T> node = new DataNode<T>(value);
			node.setAliveTimeout(aliveTimeout);
			node.setIdleTimeout(idleTimeout);
			this.value.unshift(key, node);
		}
	}
	
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
	
	public boolean containsKey(String key) {
		synchronized (mutex) {
			return value.containsKey(key);
		}
	}
	
	public void remove(String key) {
		synchronized (mutex) {
			value.remove(key);
		}
	}
	
	public void update() {
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
