package com.gdxsoft.easyweb.script.messageQueue;

import java.util.ArrayList;

import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class MsgQueue {
	private String _ChkPara;
	private String _ChkVal;
	private long _T = 0;
	private Object[] _Msgs; // 消息池
	private int _MaxCount = 100;
	private int _CurIdx = 0;

	public MsgQueue() {
		_Msgs = new Object[3];
		for (int i = 0; i < _Msgs.length; i++) {
			_Msgs[i] = new ArrayList<MsgBody>();
		}
	}

	@SuppressWarnings("unchecked")
	public String toJson(long prevTime) {
		MStr s = new MStr();
		s.a("{");
		s.a(Utils.toJsonPair("T", this._T + ""));
		s.a(",E: true");
		s.a(",V:[");
		if (prevTime == _T || prevTime<=0) { //无最新事件或第一次加载
			s.a("]}");
			return s.toString();
		}
		Object[] mm = _Msgs.clone();
		Object[] oo = new Object[2];
		if (_CurIdx == 0) {
			oo[0] = mm[2];
			oo[1] = mm[0];
		} else if (_CurIdx == 1) {
			oo[0] = mm[0];
			oo[1] = mm[1];
		} else {
			oo[0] = mm[1];
			oo[1] = mm[2];
		}

		int inc = 0;
		for (int i = 0; i < oo.length; i++) {
			ArrayList<MsgBody> m1 = (ArrayList<MsgBody>) oo[i];
			if (inc > 0) {
				s.a(",");
			}
			String s1 = this.toJson(prevTime, m1);
			if (s1.length() > 0) {
				s.a(s1);
				inc++;
			}
		}
		s.a("]}");
		return s.toString();
	}

	private String toJson(long prevTime, ArrayList<MsgBody> m1) {
		if (m1.size() == 0) {
			return "";
		}
		MStr s = new MStr();
		int inc = 0;
		for (int i = 0; i < m1.size(); i++) {
			MsgBody m = m1.get(i);
			if (m.getTime() <= prevTime) {
				continue;
			}
			if (inc > 0) {
				s.a(",");
			}
			s.a(m1.get(i).toJson());
			inc++;
		}
		return s.toString();
	}

	@SuppressWarnings( { "unchecked", "unchecked" })
	public void addMsg(String id, String type, String q) {
		MsgBody m = new MsgBody();
		m.setId(id);
		m.setMsgType(type);
		m.setQueryJson(q);
		ArrayList<MsgBody> m1 = (ArrayList<MsgBody>) _Msgs[_CurIdx];
		if (m1.size() == this._MaxCount) {
			_CurIdx++;
			int freeIdx = -1;
			if (_CurIdx == 3) { // 2,0
				freeIdx = 1;
				_CurIdx = 0;
			} else if (_CurIdx == 1) { // 0,1
				freeIdx = 2;
			} else if (_CurIdx == 2) { // 1,2
				freeIdx = 0;
			}
			ArrayList<MsgBody> free = (ArrayList<MsgBody>) _Msgs[freeIdx];
			free.clear();
			m1 = (ArrayList<MsgBody>) _Msgs[_CurIdx];
		}
		m1.add(m);
	}

	/**
	 * @return the _ChkPara
	 */
	public String getChkPara() {
		return _ChkPara;
	}

	/**
	 * @param chkPara
	 *            the _ChkPara to set
	 */
	public void setChkPara(String chkPara) {
		_ChkPara = chkPara;
	}

	/**
	 * @return the _ChkVal
	 */
	public String getChkVal() {
		return _ChkVal;
	}

	/**
	 * @param chkVal
	 *            the _ChkVal to set
	 */
	public void setChkVal(String chkVal) {
		_ChkVal = chkVal;
	}

	/**
	 * @return the _T
	 */
	public long getT() {
		return _T;
	}

	/**
	 * @param _t
	 *            the _T to set
	 */
	public void setT(long _t) {
		_T = _t;
	}
}
