package io.github.libkodi.security.utils.objectlist;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * 对象列表
 * 为对象添加顺序遍历操作(链表封装)
 * @param <K> 键
 * @param <V> 值
 */
public class ObjectList<K, V> {
	private final Object mutex; // 锁
	private ObjectListNode<K, V> start = null; // 链表头
	private ObjectListNode<K, V> end = null; // 链表结尾
	// 存储所有对象的map
	private HashMap<K, ObjectListNode<K, V>> data = new HashMap<K, ObjectListNode<K, V>>();
	
	public ObjectList() {
		mutex = this;
	}
	
	/**
	 * 往列表后面添加一个对象
	 * @param key
	 * @param value
	 */
	public void push(K key, V value) {
		synchronized (mutex) {
			ObjectListNode<K, V> node = new ObjectListNode<K, V>(key, value);
			
			if (data.containsKey(key)) { // 已经存在就替换
				ObjectListNode<K, V> oldNode = data.get(key);
				node.setNext(oldNode.getNext());
				node.setPrev(oldNode.getPrev());
			} else {
				if (start == null && end == null) { // 如果是第一个添加就修改开始与结尾的指向
					start = node;
					end = node;
				} else { // 否则一律视为向后添加
					end.setNext(node);
					node.setPrev(end);
					end = node;
				}
			}
			
			data.put(key, node);
		}
	}
	
	/**
	 * 向列表前面添加一个对象
	 * @param key
	 * @param value
	 */
	public void unshift(K key, V value) {
		synchronized (mutex) {
			ObjectListNode<K, V> node = new ObjectListNode<K, V>(key, value);
			
			if (data.containsKey(key)) { // 如果该对象已经存在
				ObjectListNode<K, V> oldNode = data.get(key);
				
				if (oldNode != start) { // 并且不为开头的对象，则先从当前列表中移除该对象
					ObjectListNode<K, V> next = oldNode.getNext();
					ObjectListNode<K, V> prev = oldNode.getPrev();
					
					if (prev != null) {
						prev.setNext(next);
					}
					
					if (next != null) {
						next.setPrev(prev);
					}
				}
			}
			
			// 将对象添加到列表头
			node.setNext(start);
			
			if (start != null) {
				start.setPrev(node);
			}
			
			start = node; // 并修改开始指向
			
			data.put(key, node);
		}
	}
	
	/**
	 * 移除对象
	 * @param key
	 * @return
	 */
	public V remove(K key) {
		synchronized (mutex) {
			if (data.containsKey(key)) {
				ObjectListNode<K, V> node = data.get(key);
				
				if (node != end && node != start) { // 不为开始与结尾
					ObjectListNode<K, V> next = node.getNext();
					ObjectListNode<K, V> prev = node.getPrev();
					
					prev.setNext(next);
					next.setPrev(prev);
					
					return data.remove(key).getValue();
				}
				
				if (node == end) { // 如果为移除末尾对象
					ObjectListNode<K, V> prev = node.getPrev();
					
					if (prev == null) {
						end = null;
					} else {
						prev.setNext(null);
						end = prev;
					}
				}
				
				if (node == start) { // 如果为移除开始对象
					ObjectListNode<K, V> next = node.getNext();
					
					if (next == null) {
						start = null;
					} else {
						next.setPrev(null);
						start = next;
					}
				}
				
				return data.remove(key).getValue();
			}
		}
		
		return null;
	}
	
	/**
	 * 获取对象
	 * @param key
	 * @return
	 */
	public V get(K key) {
		ObjectListNode<K, V> node = data.get(key);
		
		if (node != null) {
			return node.getValue();
		}
		
		return null;
	}
	
	/**
	 * 判断是否为空列表
	 * @return
	 */
	public boolean isEmpty() {
		synchronized (mutex) {
			return data.isEmpty();
		}
	}
	
	/**
	 * 清空列表
	 */
	public void clear() {
		synchronized (mutex) {
			data.clear();
			start = null;
			end = null;
		}
	}
	
	/**
	 * 判断是否包含指定的key
	 * @param key
	 * @return
	 */
	public boolean containsKey(K key) {
		synchronized (mutex) {
			return data.containsKey(key);
		}
	}
	
	/**
	 * 获取遍历
	 * @return
	 */
	public Iterator<Entry<K, V>> iterator() {
		return iterator(false);
	}
	
	/**
	 * 获取遍历
	 * @param back 是否反向遍历
	 * @return
	 */
	public Iterator<Entry<K, V>> iterator(boolean back) {
		return new Iterator<Entry<K, V>>() {
			private ObjectListNode<K, V> _next = back ? end : start;
			
			@Override
			public boolean hasNext() {
				return _next != null;
			}

			@Override
			public Entry<K, V> next() {
				if (_next != null) {
					Entry<K, V> entry = nodeToEntry(_next);
					_next = back ? _next.getPrev() : _next.getNext();
					return entry;
				}
				
				return null;
			}

		};
	}
	
	/**
	 * 将节点转为Entry对象
	 * @param _node
	 * @return
	 */
	private Entry<K, V> nodeToEntry(ObjectListNode<K, V> _node) {
		return new Entry<K, V>() {
			private ObjectListNode<K, V> node = _node;
			
			@Override
			public K getKey() {
				return node.getKey();
			}

			@Override
			public V getValue() {
				return node.getValue();
			}

			@Override
			public V setValue(V value) {
				return node.setValue(value);
			}
		};
	}
}
