package com.gdxsoft.easyweb.cache;

public interface ISqlCached {
	/**
	 * 删除单个缓存
	 * 
	 * @param key
	 * @param type
	 * @return
	 */
	public boolean remove(String key, String type);

	/**
	 * 删除多个缓存
	 * 
	 * @param keys
	 * @param type
	 * @return
	 */
	public boolean removes(String[] keys, String type);

	/**
	 * 添加 文字
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	boolean add(String key, String value);

	boolean add(String key, String value, String memo);
	/**
	 * 添加 二进制
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	boolean add(String key, byte[] value);
	
	boolean add(String key, byte[] value, String memo);
	/**
	 * 获取二进制
	 * 
	 * @param key
	 * @return
	 */
	SqlCachedValue getBinary(String key);

	/**
	 * 获取文本
	 * 
	 * @param key
	 * @return
	 */
	SqlCachedValue getText(String key);

	/**
	 * 获取对象
	 * 
	 * @param key
	 * @param type
	 *            类型，bin, text ...
	 * @return
	 */
	SqlCachedValue get(String key, String type);

}