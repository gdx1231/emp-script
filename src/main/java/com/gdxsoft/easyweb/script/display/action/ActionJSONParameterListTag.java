package com.gdxsoft.easyweb.script.display.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.gdxsoft.easyweb.utils.Utils;

/**
 * { "tag":"productList", "key": "HOTEL_ID", "table": "V_CACHED_HOTEL_MAIN" }
 * 
 * @author admin
 *
 */
public class ActionJSONParameterListTag {
	private String tag;// 数据表对于的field
	private String key; // pk
	private String table; // table name
	private String connConfigName; // database connection configure name

	private boolean skipError = false; // 忽略错误
	private boolean skipExists = false; // 跳过已存在数据，不更新

	// 子表
	private List<ActionJSONParameterListTag> subListTags = new ArrayList<>();
	private JSONObject jsonObject;

	private boolean asLfData = false; // 作为列表数据

	// 字段对应关系，key = json key, value = table fieldname
	// 用于数据库save操作
	private Map<String, String> fieldsMap = new HashMap<>();

	/**
	 * 保持数据的设置是否完整
	 * 
	 * @return
	 */
	public boolean isSaveValid() {
		return StringUtils.isNoneBlank(this.key) && StringUtils.isNoneBlank(this.table);
	}

	/**
	 * { "tag":"productList", "key": "p_id", "table": "product",
	 * "connConfigName":"cm"}
	 * 
	 * @param jsonStr
	 */
	public void init(String jsonStr) {
		JSONObject obj = new JSONObject(jsonStr);
		this.init(obj);
	}

	/**
	 * { "tag":"productList", "key": "p_id", "table": "product",
	 * "connConfigName":"cm"}
	 * 
	 * @param obj
	 */
	public void init(JSONObject obj) {
		this.jsonObject = obj;
		Iterator<String> it = obj.keys();
		while (it.hasNext()) {
			String key = it.next();
			String val = obj.optString(key);
			if (key.equalsIgnoreCase("key")) {
				this.key = val.trim();
			} else if (key.equalsIgnoreCase("table")) {
				this.table = val;
			} else if (key.equalsIgnoreCase("connConfigName")) {
				this.connConfigName = val;
			} else if (key.equalsIgnoreCase("tag")) {
				this.tag = val;
			} else if (key.equalsIgnoreCase("skipError")) {
				this.skipError = Utils.cvtBool(val);
			} else if (key.equalsIgnoreCase("skipExists")) {
				this.skipExists = Utils.cvtBool(val);
			} else if (key.equalsIgnoreCase("asLfData")) {
				// 作为列表数据
				this.asLfData = Utils.cvtBool(val);
			} else if (key.equalsIgnoreCase("listTags")) {
				JSONArray arr = obj.optJSONArray(key);
				for (int i = 0; i < arr.length(); i++) {
					JSONObject tag = arr.getJSONObject(i);
					ActionJSONParameterListTag o = new ActionJSONParameterListTag();
					o.init(tag);

					this.subListTags.add(o);
				}
			} else if (key.equalsIgnoreCase("fieldsMap")) {
				// 自定义字段对应关系
				JSONObject fieldsMap = obj.optJSONObject(key);
				Iterator<String> itfieldsMap = fieldsMap.keys();
				while (itfieldsMap.hasNext()) {
					String jsonKey = itfieldsMap.next();
					String tableField = fieldsMap.optString(jsonKey);
					this.fieldsMap.put(jsonKey, tableField);
				}
			}
		}
		this.subListTags.forEach(o -> {
			if (StringUtils.isBlank(o.getConnConfigName())) {
				o.setConnConfigName(this.connConfigName);
			}
		});
	}

	/**
	 * pk
	 * 
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * pk
	 * 
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the table
	 */
	public String getTable() {
		return table;
	}

	/**
	 * @param table the table to set
	 */
	public void setTable(String table) {
		this.table = table;
	}

	/**
	 * database connection configure name
	 * 
	 * @return the connConfigName
	 */
	public String getConnConfigName() {
		return connConfigName;
	}

	/**
	 * database connection configure name
	 * 
	 * @param connConfigName the connConfigName to set
	 */
	public void setConnConfigName(String connConfigName) {
		this.connConfigName = connConfigName;
		this.subListTags.forEach(t -> {
			if (StringUtils.isBlank(t.getConnConfigName())) {
				t.setConnConfigName(this.connConfigName);
			}
		});
	}

	/**
	 * @return the tag
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * @param tag the tag to set
	 */
	public void setTag(String tag) {
		this.tag = tag;
	}

	/**
	 * @return the skipError
	 */
	public boolean isSkipError() {
		return skipError;
	}

	/**
	 * @param skipError the skipError to set
	 */
	public void setSkipError(boolean skipError) {
		this.skipError = skipError;
	}

	/**
	 * @return the skipExists
	 */
	public boolean isSkipExists() {
		return skipExists;
	}

	/**
	 * @param skipExists the skipExists to set
	 */
	public void setSkipExists(boolean skipExists) {
		this.skipExists = skipExists;
	}

	/**
	 * @return the jsonObject
	 */
	public JSONObject getJsonObject() {
		return jsonObject;
	}

	/**
	 * @return the subListTags
	 */
	public List<ActionJSONParameterListTag> getSubListTags() {
		return subListTags;
	}

	/**
	 * @return the asLfData
	 */
	public boolean isAsLfData() {
		return asLfData;
	}

	/**
	 * @param asLfData the asLfData to set
	 */
	public void setAsLfData(boolean asLfData) {
		this.asLfData = asLfData;
	}

	public String toString() {
		if (this.jsonObject == null) {
			return "not initialized";
		} else {
			return this.jsonObject.toString(3);
		}
	}

	/**
	 * 字段对应关系，jsonKey:fieldName
	 * 
	 * @return the fieldsMap
	 */
	public Map<String, String> getFieldsMap() {
		return fieldsMap;
	}
}
