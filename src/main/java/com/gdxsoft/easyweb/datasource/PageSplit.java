package com.gdxsoft.easyweb.datasource;

import org.json.JSONObject;

import com.gdxsoft.easyweb.script.RequestValue;

/**
 * 分页对象
 * 
 * @author admin
 *
 */
public class PageSplit {

	private int _PageCurrent;
	private int _PageCount;
	private int _RecordCount;
	private int _PageSize;
	public static final String TAG_PAGE_CURRENT = "EWA_PAGECUR";
	public static final String TAG_PAGE_SIZE = "EWA_PAGESIZE";

	public PageSplit(int recordCount, RequestValue requestValue, int defPageSize) {
		String pageCur = requestValue.getString(TAG_PAGE_CURRENT);
		String pageSize = requestValue.getString(TAG_PAGE_SIZE);
		int iPageCur = 1;
		try {
			iPageCur = Integer.parseInt(pageCur);
			if (iPageCur <= 0) {
				iPageCur = 1;
			}
		} catch (Exception e) {

		}
		int iPageSize = defPageSize;
		try {
			iPageSize = Integer.parseInt(pageSize);
		} catch (Exception e) {

		}
		if (iPageSize <= 0) {
			iPageSize = 10;
		} else if (iPageSize > 500) {
			iPageSize = 500;
		}

		this._PageCurrent = iPageCur;
		this._PageSize = iPageSize;
		this._RecordCount = recordCount;
		int m = _RecordCount / this._PageSize;
		if (_RecordCount % this._PageSize > 0) {
			m++;
		}
		this._PageCount = m;
	}

	/**
	 * 返回JSON对象
	 * 
	 * @return
	 */
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();
		obj.put("PAGE_SIZE", this._PageSize);
		obj.put("PAGE_COUNT", this.getPageCount());
		obj.put("PAGE_RECORDS", this.getRecordCount());
		obj.put("PAGE_CURRENT", this.getPageCurrent());

		return obj;
	}

	/**
	 * 当前页编号
	 * 
	 * @return the _PageCurrent
	 */
	public int getPageCurrent() {
		return _PageCurrent;
	}

	/**
	 * 总页数
	 * 
	 * @return the _PageCount
	 */
	public int getPageCount() {
		return _PageCount;
	}

	/**
	 * 记录数
	 * 
	 * @return the _RecordCount
	 */
	public int getRecordCount() {
		return _RecordCount;
	}

	/**
	 * 每页的记录数
	 * 
	 * @return the _PageSize
	 */
	public int getPageSize() {
		return _PageSize;
	}

}
