package com.gdxsoft.easyweb.document;

import java.util.ArrayList;

import org.json.JSONObject;

import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.RequestValue;

public class OdfLevel {
	public OdfLevel getParent() {
		return parent;
	}

	public void setParent(OdfLevel parent) {
		this.parent = parent;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMarkStart() {
		return markStart;
	}

	public void setMarkStart(String markStart) {
		this.markStart = markStart;
	}

	public String getMarkEnd() {
		return markEnd;
	}

	public void setMarkEnd(String markEnd) {
		this.markEnd = markEnd;
	}

	public int getLocStart() {
		return locStart;
	}

	public void setLocStart(int locStart) {
		this.locStart = locStart;
	}

	public int getLocEnd() {
		return locEnd;
	}

	public void setLocEnd(int locEnd) {
		this.locEnd = locEnd;
	}

	public String getJsonExp() {
		return jsonExp;
	}

	public void setJsonExp(String jsonExp) {
		this.jsonExp = jsonExp;
	}

	public JSONObject getJson() {
		return json;
	}

	public void setJson(JSONObject json) {
		this.json = json;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public ArrayList<OdfLevel> getSubs() {
		return subs;
	}

	public void setSubs(ArrayList<OdfLevel> subs) {
		this.subs = subs;
	}

	public String toString() {
		String s = "pid="
				+ (this.parent == null ? "none" : this.parent.getId())
				+ ", id=" + this.id + ", subs=" + this.subs.size()
				+ ", markStart:" + markStart + ", markEnd" + markEnd
				+ ", locStart:" + locStart + ", locEnd=" + locEnd;
		return s;
	}
	
	private String unid;

	public String getUnid() {
		return unid;
	}

	public void setUnid(String unid) {
		this.unid = unid;
	}

	private String markStart;
	private String markEnd;

	private int locStart;
	private int locEnd;

	private String jsonExp;
	private JSONObject json;

	private String content;
	private String contentFixed;
	/**
	 * 替换后的内容
	 * @return
	 */
	public String getContentFixed() {
		return contentFixed;
	}

	public void setContentFixed(String contentFixed) {
		this.contentFixed = contentFixed;
	}

	private RequestValue meRv;

	public RequestValue getMeRv() {
		return meRv;
	}

	public void setMeRv(RequestValue meRv) {
		this.meRv = meRv;
	}

	private ArrayList<OdfLevel> subs = new ArrayList<OdfLevel>();
	private ArrayList<DTTable> tables = new ArrayList<DTTable>();

	public ArrayList<DTTable> getTables() {
		return tables;
	}

	public void setTables(ArrayList<DTTable> tables) {
		this.tables = tables;
	}

	public String getCntTop() {
		return cntTop;
	}

	public void setCntTop(String cntTop) {
		this.cntTop = cntTop;
	}

	public String getCntBottom() {
		return cntBottom;
	}

	public void setCntBottom(String cntBottom) {
		this.cntBottom = cntBottom;
	}

	public String getCntMiddle() {
		return cntMiddle;
	}

	public void setCntMiddle(String cntMiddle) {
		this.cntMiddle = cntMiddle;
	}

	private DTRow curRow;
	public DTRow getCurRow() {
		return curRow;
	}

	public void setCurRow(DTRow curRow) {
		this.curRow = curRow;
	}

	private String cntTop;
	private String cntBottom;
	private String cntMiddle;
	
	private OdfLevel parent;
	private String id;
}
