package com.gdxsoft.easyweb.script.messageQueue;

import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class MsgBody {
	private String _Id;
	private String _MsgType;
	private String _QueryJson;
	private Long _Time = System.currentTimeMillis();

	public String toJson() {
		MStr s = new MStr();
		s.a("{");
		s.a(Utils.toJsonPair("T", this._Time + ""));
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
}
