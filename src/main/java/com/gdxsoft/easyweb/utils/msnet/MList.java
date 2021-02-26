package com.gdxsoft.easyweb.utils.msnet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 用于兼容 MS.NET的生成，省得麻烦
 * 
 * @author Administrator
 * 
 */
public class MList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1710041296734505566L;
	private ArrayList<Object> _List;

	public ArrayList<Object> getList() {
		return _List;
	}

	public MList() {
		_List = new ArrayList<Object>();
	}

	public MList(ArrayList<?> al) {
		_List = new ArrayList<Object>();
		for (int i = 0; i < al.size(); i++) {
			this._List.add(al.get(i));
		}
	}

	public int size() {
		return this._List.size();
	}

	public void add(Object o) {
		this._List.add(o);

	}

	/**
	 * 重新初始化
	 */
	public void reset() {
		_List.clear();
	}

	/**
	 * 连接成字符串
	 * 
	 * @param splitString 连接字符
	 * @return
	 */
	public String join(String splitString) {
		MStr s = new MStr();
		for (int i = 0; i < this.size(); i++) {
			Object v = this.get(i);
			if (i > 0) {
				s.append(splitString);
			}
			s.append(v == null ? "" : v.toString());
		}
		return s.toString();
	}

	/**
	 * 排序
	 */
	public void sort() {
		try {
			Object[] array = this.getValus();
			Arrays.sort(array);
			this.reset();
			for (int i = 0; i < array.length; i++) {
				this.getList().add(array[i]);
			}
		} catch (Exception err) {
			// 当key未null时出错
		}
	}

	public void clear() {
		this.reset();
	}

	public int indexOf(Object o) {
		return this._List.indexOf(o);
	}

	/**
	 * 获取所有值
	 * 
	 * @return
	 */
	public Object[] getValus() {
		return _List.toArray();
	}

	public Object get(int index) {
		return this._List.get(index);
	}

	public Object getLast() {
		if (this.size() > 0) {
			return this._List.get(this.size() - 1);
		} else {
			return null;
		}
	}

	/**
	 * 根据值移除对象
	 * 
	 * @param value
	 */
	public boolean removeValue(Object value) {
		return _List.remove(value);
	}

	/**
	 * 根据索引移除对象
	 * 
	 * @param index
	 * @return 被移除的对象
	 */
	public Object removeAt(int index) {
		return _List.remove(index);
	}
}
