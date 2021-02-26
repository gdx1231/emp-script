package com.gdxsoft.easyweb.datasource;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.IHandleJsonBinary;
import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.Utils;

/**
 * 用于ORMAP的基类
 * 
 * @author admin
 *
 */
public class ClassBase {
	HashMap<String, Boolean> _MapFieldChanged = new HashMap<String, Boolean>();

	// 扩展属性
	Map<String, Object> _Extends = new HashMap<String, Object>();

	public Object getField(String filedName) {
		UObjectValue uv = new UObjectValue();
		uv.setObject(this);
		return uv.getProperty(filedName);
	}

	/**
	 * 获取扩展
	 * 
	 * @param name
	 * @return
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
	 * 设置扩展
	 * 
	 * @param name
	 * @param val
	 */
	public void setExt(String name, Object val) {
		String name1 = name.toUpperCase().trim();
		this._Extends.put(name1, val);
	}

	/**
	 * 是否有扩展
	 * 
	 * @param name
	 * @return
	 */
	public boolean isExtHave(String name) {
		String name1 = name.toUpperCase().trim();
		return this._Extends.containsKey(name1);
	}

	/**
	 * 开始记录变化的字段
	 */
	public void startRecordChanged() {
		this._MapFieldChanged = new HashMap<String, Boolean>();
	}

	/**
	 * 记录字段是否变化
	 * 
	 * @param field_name
	 * @param oriValue
	 * @param newValue
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

			this._MapFieldChanged.put(field_name, true);
		}
	}

	/**
	 * 根据JSON对象，初始化值
	 * 
	 * @param json
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
	 * 根据 DTRow对象，初始化值
	 * 
	 * @param row
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
	 * 利用 rv 初始化/修改对象
	 * 
	 * @param rv
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public void initOrUpdateValues(RequestValue rv)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		UObjectValue ov = new UObjectValue();
		ov.setObject(this);

		this.startRecordChanged();
		ov.setDaoValue(rv);

	}

	/**
	 * 根据JSON对象，初始化值
	 * 
	 * @param json
	 * @param handleJsonBinaryImpl 处理二进制的方法
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
	 * 返回JSON对象
	 * 
	 * @return
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
		} else {
			json.put(key, val);
		}
	}

	/**
	 * 获取扩展属性
	 * 
	 * @return the _Extends
	 */
	public Map<String, Object> getExtends() {
		return _Extends;
	}

	/**
	 * 获取变化字段的记录表
	 * 
	 * @return
	 */
	public HashMap<String, Boolean> getMapFieldChanged() {
		return _MapFieldChanged;
	}

}
