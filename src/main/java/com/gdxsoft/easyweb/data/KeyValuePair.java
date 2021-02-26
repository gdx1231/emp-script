package com.gdxsoft.easyweb.data;

/**
 * Key-Value Pair
 * 
 * @author admin
 *
 * @param <K>
 * @param <V>
 */
public class KeyValuePair<K, V> {
	K key;
	V value;

	public KeyValuePair() {

	}

	public KeyValuePair(K key, V value) {
		this.setPair(key, value);
	}

	public void setPair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * @return the key
	 */
	public K getKey() {
		return key;
	}

	/**
	 * @return the value
	 */
	public V getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "[" + key + "," + value + "]";
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(K key) {
		this.key = key;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(V value) {
		this.value = value;
	}
}
