package com.gdxsoft.easyweb.datasource;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.IHandleJsonBinary;
import com.gdxsoft.easyweb.utils.UConvert;
import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.Utils;

/**
 * 用于ORMAP的基类<br>
 * The ORMAP base class
 */
public class ClassBase {
	HashMap<String, Boolean> _MapFieldChanged = new HashMap<String, Boolean>();

	// 扩展属性
	Map<String, Object> _Extends = new HashMap<String, Object>();

	private UObjectValue uv;

	public ClassBase() {
		uv = new UObjectValue();
		uv.setObject(this);
	}

	public Object getField(String filedName) {
		return uv.getProperty(filedName);
	}

	public PageValue getFieldPageValue(String filedName) {
		PageValue pv = new PageValue();

		Method method = uv.getPropertyMethod(filedName);

		if (method == null) { // 404
			return null;
		}
		pv.setName(filedName);
		pv.setDataType(method.getReturnType().getName());
		Object value = uv.getProperty(filedName);
		pv.setValue(value);

		return pv;
	}

	/**
	 * 获取扩展<br>
	 * Return the extend object from the name
	 * 
	 * @param name the extend object name
	 * @return the extend object
	 */
	public Object getExt(String name) {
		String name1 = name.toUpperCase().trim();
		if (this._Extends.containsKey(name1)) {
			return this._Extends.get(name1);
		} else {
			return null;
		}

	}

	/**
	 * 设置扩展<br>
	 * Set the extend
	 * 
	 * @param name the extend name
	 * @param val  the extend object
	 */
	public void setExt(String name, Object val) {
		String name1 = name.toUpperCase().trim();
		this._Extends.put(name1, val);
	}

	/**
	 * Whether is have the extend
	 * 
	 * @param name the extend name
	 * @return true: yes/ false: no
	 */
	public boolean isExtHave(String name) {
		String name1 = name.toUpperCase().trim();
		return this._Extends.containsKey(name1);
	}

	/**
	 * 开始记录变化的字段，清空 MapFieldChanged<br>
	 * Start to record the changed field, clear the MapFieldChanged
	 */
	public void startRecordChanged() {
		this._MapFieldChanged = new HashMap<String, Boolean>();
	}

	/**
	 * 记录字段是否变化<br>
	 * Record the field value changed
	 * 
	 * @param field_name the field name
	 * @param oriValue   the original value
	 * @param newValue   the new value
	 */
	public void recordChanged(String field_name, Object oriValue, Object newValue) {
		if (this._MapFieldChanged == null || this._MapFieldChanged.containsKey(field_name)) {
			// 已经改变了，无需重新设置
			return;
		}
		if (oriValue == null && newValue == null) {
			return;
		}

		if (oriValue == null && newValue != null) {
			this._MapFieldChanged.put(field_name, true);
			return;
		}
		if (oriValue != null && newValue == null) {
			this._MapFieldChanged.put(field_name, true);
			return;
		}

		if (!oriValue.equals(newValue)) {

			String oriType = oriValue.getClass().getName();
			String newType = newValue.getClass().getName();
			if (!oriType.equals(newType)) {
				if (oriType.equals("java.sql.Timestamp") && newType.equals("java.util.Date")) {
					java.sql.Timestamp oriTime = (java.sql.Timestamp) oriValue;
					java.util.Date newTime = (java.util.Date) newValue;
					if (oriTime.getTime() == newTime.getTime()) {
						return;
					}
				}
			}

			// 二进制比较
			if (oriType.equals("[B") && newType.equals("[B")) {
				byte[] from = (byte[]) oriValue;
				byte[] to = (byte[]) newValue;
				if (from.length == to.length) {
					boolean equals = true;
					for (int i = 0; i < from.length; i++) {
						if (from[i] != to[i]) { // 按字节比较
							equals = false;
							break;
						}
					}
					if (equals) {
						return;
					}
				}
			}

			this._MapFieldChanged.put(field_name, true);
		}
	}

	/**
	 * 根据JSON对象，初始化值<br>
	 * Initialized the object properties through the JSON object
	 * 
	 * @param json the JSON object
	 */
	public void initValues(JSONObject json) {
		UObjectValue ov = new UObjectValue();
		ov.setObject(this);
		try {
			ov.setDaoValue(json);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * 根据 DTRow对象，初始化值<br>
	 * Initialized the object properties through the table row
	 * 
	 * @param row the table row
	 */
	public void initValues(DTRow row) {
		UObjectValue ov = new UObjectValue();
		ov.setObject(this);
		try {
			ov.setDaoValue(row);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * 利用 rv 初始化/修改对象<br>
	 * Initialized the class parameters through the RequestValue
	 * 
	 * @param rv the RequestValue
	 * @throws Exception the exception
	 */
	public void initOrUpdateValues(RequestValue rv) throws Exception {
		UObjectValue ov = new UObjectValue();
		ov.setObject(this);

		this.startRecordChanged();
		ov.setDaoValue(rv);

	}

	/**
	 * 根据JSON对象，初始化值 Initialized the class parameters through the JSON
	 * 
	 * @param json                 the JSON
	 * @param handleJsonBinaryImpl 处理二进制的接口 the interface of the get the JSON binary
	 */
	public void initValues(JSONObject json, IHandleJsonBinary handleJsonBinaryImpl) {
		UObjectValue ov = new UObjectValue();
		ov.setObject(this);
		try {
			ov.setDaoValue(json, handleJsonBinaryImpl);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * 返回JSON对象<br>
	 * Return the JSON object
	 * 
	 * @return the JSON object
	 */
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		for (String field : this._MapFieldChanged.keySet()) {
			Object val = this.getField(field);
			this.addToJson(json, field, val);
		}

		// 有扩展属性
		if (this._Extends != null && this._Extends.size() > 0) {
			for (String key : this._Extends.keySet()) {
				Object val = this._Extends.get(key);
				this.addToJson(json, key, val);
			}
		}

		return json;
	}

	private void addToJson(JSONObject json, String key, Object val) {
		if (val == null) {
			return;
		}
		String className = val.getClass().getName();
		if ("java.util.Date".equals(className)) {// 日期型
			java.util.Date date = (java.util.Date) val;
			String datestr = Utils.getDateString(date, "yyyy-MM-dd HH:mm:ss.SSS");
			json.put(key, datestr);
		} else if ("b[".equals(className)) {// 二进制
			byte[] buf = (byte[]) val;
			String base64 = UConvert.ToBase64String(buf);
			json.put(key, base64);
		} else {
			json.put(key, val);
		}
	}

	/**
	 * 获取扩展属性<br>
	 * Get the extends properties
	 * 
	 * @return the _Extends
	 */
	public Map<String, Object> getExtends() {
		return _Extends;
	}

	/**
	 * 获取变化字段的记录表<br>
	 * Get the changed fields
	 * 
	 * @return the changed field
	 */
	public HashMap<String, Boolean> getMapFieldChanged() {
		return _MapFieldChanged;
	}

}
