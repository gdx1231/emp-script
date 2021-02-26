/**
 * 
 */
package com.gdxsoft.easyweb.script.template;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author Administrator
 * 
 */
public class SetBase<T> implements Cloneable   , Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2899140491503565568L;
	private HashMap<String, T> _V = new HashMap<String, T>();
	private ArrayList<String> _Names = new ArrayList<String>();
	private String _Xml;

	public T getItem(int index) throws Exception {
		if (index < 0 || index >= this._Names.size()) {
			throw new Exception("索引号错误（<0 或超出范围）index=" + index + ",size="
					+ this._Names.size());
		}
		return this.getItem(_Names.get(index));
	}

	public T getItem(String name) throws Exception {
		if (this._V.containsKey(name)) {
			return this._V.get(name);
		} else {
			String n1 = this.getName(name);
			if (n1 == null) {
				throw new Exception("名称未发现(" + name + ")");

			}
			return this._V.get(n1);
		}
	}
	
	/**
	 * 检查名称对象是否存在
	 * @param name 名称，不区分大小写
	 * @return 
	 */
	public boolean checkItemExists(String name){
		if(this._V.containsKey(name)){
			return true;
		}else{
			if(this.getName(name)==null){
				return false;
			}else{
				return true;
			}
		}
	}

	/**
	 * 获取名称的索引，不区分大小写
	 * 
	 * @param name
	 * @return
	 */
	public int getIndex(String name) {
		if (name == null)
			return -999;
		for (int i = 0; i < this._Names.size(); i++) {
			String n = this._Names.get(i);
			if (n != null && n.equalsIgnoreCase(name)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 获取索引对应的名字
	 * @param index
	 * @return
	 */
	public String getName(int index){
		return this._Names.get(index);
	}
	
	/**
	 * 不区分大小写获取名称
	 * 
	 * @param name
	 * @return
	 */
	private String getName(String name) {
		Iterator<String> a = this._V.keySet().iterator();
		String n1 = name.toUpperCase();
		while (a.hasNext()) {
			String n2 = a.next();
			if (n1.equals(n2.toUpperCase().trim())) {
				return n2;
			}
		}
		return null;
	}

	public boolean testName(String name) {
		boolean a = this._V.containsKey(name);
		if (!a) {
			if (this.getName(name) != null) {
				a = true;
			}
		}
		return a;
	}

	public void addObject(T obj, String name) {
		if(name == null){
			name=this._Names.size()+"";
		}
		this._V.put(name, obj);
		this._Names.add(name);
	}

	public int count() {
		return this._Names.size();
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * @return the _Xml
	 */
	public String getXml() {
		int m0 = this._Xml.indexOf("DataField=\"");
		if (m0 > 0) {
			int m1 = this._Xml.indexOf("\"", "DataField=\"".length() + m0);
			String s1 = this._Xml.substring(0, m0)
					+ this._Xml.substring(m1 + 2);
			// System.out.println(s1);
			return s1;
		} else {
			return _Xml;
		}
	}

	/**
	 * @param xml
	 *            the _Xml to set
	 */
	public void setXml(String xml) {
		_Xml = xml;
	}

}
