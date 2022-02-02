package io.github.libkodi.security.utils.objectlist;

/**
 *  对象列表的节点类
 * @param <K>
 * @param <V>
 */
public class ObjectListNode<K, V> {
	private V value; // 值
	private ObjectListNode<K, V> prev = null; // 前一个节点
	private ObjectListNode<K, V> next = null; // 后一个节点
	private K key; // 键名
	
	public ObjectListNode(K key, V value) {
		this.value = value;
		this.key = key;
	}
	
	public V getValue() {
		return value;
	}
	
	public V setValue(V value) {
		this.value = value;
		return value;
	}
	
	public K getKey() {
		return key;
	}
	
	public ObjectListNode<K, V> getPrev() {
		return prev;
	}
	
	public ObjectListNode<K, V> getNext() {
		return next;
	}
	
	public ObjectListNode<K, V> setPrev(ObjectListNode<K, V> prev) {
		this.prev = prev;
		return prev;
	}
	
	public ObjectListNode<K, V> setNext(ObjectListNode<K, V> next) {
		this.next = next;
		return next;
	}
}
