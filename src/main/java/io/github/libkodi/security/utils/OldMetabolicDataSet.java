package io.github.libkodi.security.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import io.github.libkodi.security.entity.DataSet;

public class OldMetabolicDataSet<V> {
	private Map<String, DataSet<V>> values;
	final Object mutex;
	private String first = null;
	private String tail = null;
	
	public OldMetabolicDataSet() {
		this.values = new HashMap<String, DataSet<V>>();
		this.mutex = this;
	}
	
	public OldMetabolicDataSet(Object mutex) {
		this.mutex = mutex;
	}
	
	public int size() {
		synchronized (mutex) {
			return values.size();
		}
	}
	
	public boolean isEmpty() {
		synchronized (mutex) {
			return values.isEmpty();
		}
	}
	
	public boolean containsKey(String key) {
		synchronized (mutex) {
			return values.containsKey(key);
		}
	}
	
	public boolean containsValue(Object value) {
		synchronized (mutex) {
			return values.containsValue(value);
		}
	}
	
	public V get(String key) {
		return renew(key);
	}
	
	public V put(String key, V value) {
		synchronized (mutex) {
			DataSet<V> ds = new DataSet<V>(value);
			
			if (tail == null || first == null) {
				tail = key;
			} else {
				values.get(first).setPrev(key);
				ds.setNext(key);
			}
			
			first = key;
			
			values.put(key, ds);
			
			renew(key);
			
			return value;
		}
	}
	
	public V remove(String key) {
		synchronized (mutex) {
			DataSet<V> ds = values.remove(key);
			
			if (ds == null) {
				return null;
			}
			
			if (key.equals(first)) {
				first = ds.getNext();
				DataSet<V> temp = values.get(ds.getNext());
				
				if (temp != null) {
					temp.setPrev(null);
					tail = null;
				}
			} else if (key.equals(tail)) {
				values.get(ds.getPrev()).setNext(null);
				tail = ds.getPrev();
			} else {
				values.get(ds.getPrev()).setNext(ds.getNext());
				values.get(ds.getNext()).setPrev(ds.getPrev());
			}
			
			return ds.getValue();
		}
	}
	
	public V renew(String key) {
		synchronized (mutex) {
			DataSet<V> ds = values.get(key);
			
			if (ds == null) {
				return null;
			}
			
			if (!key.equals(first)) {
				ds.setNext(first);
				ds.setPrev(null);
				
				if (key.equals(tail)) {
					values.get(ds.getPrev()).setNext(null);
					first = tail;
					tail = ds.getPrev();
				} else {
					values.get(ds.getNext()).setPrev(ds.getPrev());
					values.get(ds.getPrev()).setNext(ds.getNext());
					values.get(first).setPrev(key);
					first = key;
				}
			}
			
			return ds.getValue();
		}
	}
	
	public Set<String> keySet() {
		synchronized (mutex) {
			return values.keySet();
		}
	}

	public void clear() {
		synchronized (mutex) {
			tail = null;
			first = null;
			values.clear();
		}
	}
	
	public Iterator<V> iterator() {
		return new Iterator<V>() {
			private String current = null;
			private DataSet<V> data = null;
			
			@Override
			public boolean hasNext() {
				if (current == null) {
					current = tail;
				} else {
					current = data.getPrev();
				}
				
				data = values.get(current);
				
				if (data == null) {
					return false;
				} else {
					return true;
				}
			}

			@Override
			public V next() {
				if (data != null) {
					return data.getValue();
				} else {
					return null;
				}
			}
			
		};
	}
}
