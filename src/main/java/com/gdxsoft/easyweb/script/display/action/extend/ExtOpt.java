package com.gdxsoft.easyweb.script.display.action.extend;

import org.json.JSONException;
import org.json.JSONObject;
/**
 * 扩展操作
 * @author Administrator
 *
 */
public class ExtOpt {

	private String _JsonStr;
	private JSONObject _Json;
	private boolean _IsOk;
	private String _ErrMsg;
	private String _WorkType;

	public ExtOpt() {

	}
	
	public String getPara(String name){
		try {
			return this._Json.getString(name);
		} catch (JSONException e) {
			return null;
		}
	}
	
	public JSONObject getParaObj(String name){
		try {
			return this._Json.getJSONObject(name);
		} catch (JSONException e) {
			return null;
		}
	}

	public void init(String jsonStr) {
		_JsonStr = jsonStr;
		try {
			_Json = new JSONObject(jsonStr);
			this._WorkType = this.getPara("TYPE");
			if (this._WorkType == null) {
				this._IsOk = false;
			} else {
				this._IsOk = true;
				//工作模式
				this._WorkType = this._WorkType.trim().toUpperCase();
			}
		} catch (JSONException e) {
			this._IsOk = false;
			this._ErrMsg = e.getMessage();
		}
	}

	/**
	 * JSON字符串
	 * 
	 * @return the _JsonStr
	 */
	public String getJsonStr() {
		return _JsonStr;
	}

	/**
	 * JSON字符串
	 * 
	 * @param jsonStr
	 *            the _JsonStr to set
	 */
	public void setJsonStr(String jsonStr) {
		_JsonStr = jsonStr;
	}

	/**
	 * JSON对象
	 * 
	 * @return the _Json
	 */
	public JSONObject getJson() {
		return _Json;
	}

	/**
	 * 是否正确
	 * 
	 * @return the _IsOk
	 */
	public boolean is_IsOk() {
		return _IsOk;
	}

	/**
	 * 是否正确
	 * 
	 * @param isOk
	 *            the _IsOk to set
	 */
	public void setIsOk(boolean isOk) {
		_IsOk = isOk;
	}

	/**
	 * 错误信息
	 * 
	 * @return the _ErrMsg
	 */
	public String getErrMsg() {
		return _ErrMsg;
	}

	/**
	 * @param errMsg
	 *            the _ErrMsg to set
	 */
	public void setErrMsg(String errMsg) {
		_ErrMsg = errMsg;
	}

	/**
	 * 工作模式
	 * 
	 * @return the _WorkType
	 */
	public String getWorkType() {
		return _WorkType;
	}

	/**
	 * @param workType
	 *            the _WorkType to set
	 */
	public void setWorkType(String workType) {
		_WorkType = workType;
	}
}
