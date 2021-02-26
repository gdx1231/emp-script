package com.gdxsoft.easyweb.msgbus;

import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class MsgBusBody {
	private String _Id;
	private String _MsgType;
	private String _QueryJson;
	private Long _Time = System.currentTimeMillis();
	private String _XmlName;
	private String _ItemName;
	
	public String toJson() {
		MStr s = new MStr();
		s.a("{");
		
		s.a(Utils.toJsonPair("ID", this._Id));
		
		s.a(",");
		s.a(Utils.toJsonPair("XN", this._XmlName));
		
		s.a(",");
		s.a(Utils.toJsonPair("IN", this._ItemName));
		
		s.a(",");
		s.a(Utils.toJsonPair("ID", this._Id));
		
		s.a(",");
		s.a(Utils.toJsonPair("TP", this._MsgType));
		
		s.a(",");
		s.a(Utils.toJsonPair("V", this._QueryJson));
		
		s.a("}");
		return s.toString();
	}

	public Long getTime() {
		return this._Time;
	}

	/**
	 * @return the _Id
	 */
	public String getId() {
		return _Id;
	}

	/**
	 * @param id
	 *            the _Id to set
	 */
	public void setId(String id) {
		_Id = id;
	}

	/**
	 * @return the _MsgType
	 */
	public String getMsgType() {
		return _MsgType;
	}

	/**
	 * @param msgType
	 *            the _MsgType to set
	 */
	public void setMsgType(String msgType) {
		_MsgType = msgType;
	}

	/**
	 * @return the _QueryJson
	 */
	public String getQueryJson() {
		return _QueryJson;
	}

	/**
	 * @param queryJson
	 *            the _QueryJson to set
	 */
	public void setQueryJson(String queryJson) {
		_QueryJson = queryJson;
	}

	/**
	 * @return the _XmlName
	 */
	public String getXmlName() {
		return _XmlName;
	}

	/**
	 * @param xmlName the _XmlName to set
	 */
	public void setXmlName(String xmlName) {
		_XmlName = xmlName;
	}

	/**
	 * @return the _ItemName
	 */
	public String getItemName() {
		return _ItemName;
	}

	/**
	 * @param itemName the _ItemName to set
	 */
	public void setItemName(String itemName) {
		_ItemName = itemName;
	}
}
