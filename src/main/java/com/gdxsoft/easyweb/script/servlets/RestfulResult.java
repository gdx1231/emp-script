package com.gdxsoft.easyweb.script.servlets;

import java.util.Iterator;

import org.json.JSONObject;

public class RestfulResult<T> {

	private boolean success_ = true;
	private T data_;
	private Integer code_;
	private Object rawData;

	private String message_;
	private Integer httpStatusCode_;

	private Integer ewaPageCur;
	private Integer ewaPageSize;

	private Integer pageCount;
	private Integer recordCount;

	public boolean isSuccess() {
		return success_;
	}

	public void setSuccess(boolean success) {
		this.success_ = success;
	}

	public T getData() {
		return data_;
	}

	public void setData(T data) {
		this.data_ = data;
	}

	public Integer getCode() {
		return code_;
	}

	public void setCode(int code) {
		this.code_ = code;
	}

	public String getMessage() {
		return message_;
	}

	public void setMessage(String message) {
		this.message_ = message;
	}

	public Integer getHttpStatusCode() {
		return httpStatusCode_;
	}

	public void setHttpStatusCode(Integer httpStatusCode) {
		this.httpStatusCode_ = httpStatusCode;
	}

	public JSONObject toJson() {
		JSONObject obj = new JSONObject();
		obj.put("code", this.code_);
		obj.put("http_status_code", this.httpStatusCode_);
		obj.put("success", success_);
		obj.put("message", this.message_);
		obj.put("data", this.data_);
		obj.put("ewa_page_cur", ewaPageCur);
		obj.put("ewa_page_size", ewaPageSize);
		obj.put("page_count", this.pageCount);
		obj.put("record_count", this.recordCount);

		return obj;
	}

	public void parse(String result) {
		JSONObject obj = new JSONObject(result);
		Iterator<String> keys = obj.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			if (key.equals("http_status_code")) {
				this.setHttpStatusCode(obj.optInt(key));
			} else if (key.equals("message")) {
				this.setMessage(obj.optString(key));
			} else if (key.equals("ewa_page_cur")) {
				this.setEwaPageCur(obj.optInt(key));
			} else if (key.equals("ewa_page_size")) {
				this.setEwaPageSize(obj.optInt(key));
			} else if (key.equals("record_count")) {
				this.setRecordCount(obj.optInt(key));
			} else if (key.equals("page_count")) {
				this.setPageCount(obj.optInt(key));
			} else if (key.equals("data")) {
				this.setRawData(obj.get(key));
			} else if (key.equals("success")) {
				this.setSuccess(obj.optBoolean(key));
			}
		}
	}

	public String toString() {
		return this.toJson().toString();
	}

	public Integer getEwaPageCur() {
		return ewaPageCur;
	}

	public void setEwaPageCur(Integer ewaPageCur) {
		this.ewaPageCur = ewaPageCur;
	}

	public Integer getEwaPageSize() {
		return ewaPageSize;
	}

	public void setEwaPageSize(Integer ewaPageSize) {
		this.ewaPageSize = ewaPageSize;
	}

	public Integer getPageCount() {
		return pageCount;
	}

	public void setPageCount(Integer pageCount) {
		this.pageCount = pageCount;
	}

	public Integer getRecordCount() {
		return recordCount;
	}

	public void setRecordCount(Integer recordCount) {
		this.recordCount = recordCount;
	}

	public Object getRawData() {
		return rawData;
	}

	public void setRawData(Object rawData) {
		this.rawData = rawData;
	}
}