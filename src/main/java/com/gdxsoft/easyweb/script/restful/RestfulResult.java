package com.gdxsoft.easyweb.script.restful;

import java.util.Iterator;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.utils.UJSon;

public class RestfulResult<T> {
	private static Logger LOGGER = LoggerFactory.getLogger(RestfulResult.class);
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

	private String returnResult;

	private Long start; // 执行开始时间
	private Long end; // 执行结束时间

	public RestfulResult() {
		this.start = System.currentTimeMillis();
	}

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

	/**
	 * Integer 重载，允许显式置 null（与 JSON {@code null} ↔ 未设的语义保持一致）。
	 *
	 * @param code 业务码，传 null 表示"未设置"
	 */
	public void setCode(Integer code) {
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
		obj.put("success", success_);
		if(success_) {
			UJSon.rstSetTrue(obj, null);
		} else {
			UJSon.rstSetFalse(obj, message_);
		}
		// 仅在已设置时输出数值字段，避免客户端 json.optInt 等调用出现歧义
		if (this.code_ != null) {
			obj.put("code", this.code_);
		}
		if (this.httpStatusCode_ != null) {
			obj.put("http_status_code", this.httpStatusCode_);
		}
		if (this.message_ != null) {
			obj.put("message", this.message_);
		}
		if (this.data_ != null) {
			obj.put("data", this.data_);
		}
		if (this.ewaPageCur != null) {
			obj.put("ewa_page_cur", this.ewaPageCur);
		}
		if (this.ewaPageSize != null) {
			obj.put("ewa_page_size", this.ewaPageSize);
		}
		if (this.pageCount != null) {
			obj.put("page_count", this.pageCount);
		}
		if (this.recordCount != null) {
			obj.put("record_count", this.recordCount);
		}
		if (this.start != null) {
			obj.put("start", this.start);
		}
		if (this.end != null) {
			obj.put("end", this.end);
			if (this.start != null) {
				long duration = this.end - this.start;
				obj.put("duriation", duration); // 历史拼写，向后兼容
				obj.put("duration", duration); // 新字段（正确拼写）
			}
		}
		return obj;
	}

	public void parse(String result) {
		if(result == null || result.trim().length() == 0) {
			return;
		}

		this.returnResult = result;
		try {
			JSONObject obj = new JSONObject(result);

			this.rawData = obj;
			Iterator<String> keys = obj.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				if (key.equals("code")) {
					if (!obj.isNull(key)) {
						this.setCode(obj.optInt(key));
					}
				} else if (key.equals("http_status_code")) {
					if (!obj.isNull(key)) {
						this.setHttpStatusCode(obj.optInt(key));
					}
				} else if (key.equals("message")) {
					this.setMessage(obj.optString(key));
				} else if (key.equals("ewa_page_cur")) {
					if (!obj.isNull(key)) {
						this.setEwaPageCur(obj.optInt(key));
					}
				} else if (key.equals("ewa_page_size")) {
					if (!obj.isNull(key)) {
						this.setEwaPageSize(obj.optInt(key));
					}
				} else if (key.equals("record_count")) {
					if (!obj.isNull(key)) {
						this.setRecordCount(obj.optInt(key));
					}
				} else if (key.equals("page_count")) {
					if (!obj.isNull(key)) {
						this.setPageCount(obj.optInt(key));
					}
				} else if (key.equals("data")) {
					Object data = obj.get(key);
					this.setRawData(data);
				} else if (key.equals("success")) {
					this.setSuccess(obj.optBoolean(key));
				} else if (key.equals("start")) {
					if (!obj.isNull(key)) {
						this.setStart(obj.optLong(key));
					}
				} else if (key.equals("end")) {
					if (!obj.isNull(key)) {
						this.setEnd(obj.optLong(key));
					}
				} else if (key.equals("duration") || key.equals("duriation")) {
					// 历史字段名 duriation 也读，仅用于日志，不影响业务字段
					if (!obj.isNull(key)) {
						LOGGER.debug("RestfulResult duration: {}ms", obj.optLong(key));
					}
				}
			}
		} catch (Exception err) {
			LOGGER.warn("Parse RestfulResult error! source: {}, error: {}", result, err.getMessage());
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

	public String getReturnResult() {
		return returnResult;
	}

	public void setReturnResult(String returnResult) {
		this.returnResult = returnResult;
	}

	public Long getStart() {
		return start;
	}

	public void setStart(Long start) {
		this.start = start;
	}

	public Long getEnd() {
		return end;
	}

	public void setEnd(Long end) {
		this.end = end;
	}
}