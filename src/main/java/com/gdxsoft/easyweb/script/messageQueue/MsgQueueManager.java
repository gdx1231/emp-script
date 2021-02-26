package com.gdxsoft.easyweb.script.messageQueue;

import java.util.HashMap;
import java.util.Iterator;

import com.gdxsoft.easyweb.script.RequestValue;

public class MsgQueueManager {

	private static HashMap<Integer, MsgQueue> MSGS = new HashMap<Integer, MsgQueue>();

	public static MsgQueue addListener(String checkPara, String checkValue) {
		int code = getCode(checkPara, checkValue);
		MsgQueue m = null;
		if (MSGS.containsKey(code)) {
			m = MSGS.get(code);
			if (m == null) {
				MSGS.remove(code);
			}
		}
		if (m == null) {
			m = add(code, checkPara, checkValue);
		}

		return m;
	}

	public static int getCount() {
		return MSGS.size();
	}

	private synchronized static MsgQueue add(int code, String checkPara,
			String checkValue) {
		MsgQueue q = new MsgQueue();
		q.setChkPara(checkPara);
		q.setChkVal(checkValue);
		MSGS.put(code, q);
		return q;
	}

	/**
	 * 提交JSON
	 * 
	 * @param rv
	 * @param id
	 *            Frame的ID
	 * @param type
	 * @param queryJson
	 *            Url所带参数
	 */
	public static void postMessage(RequestValue rv, String id, String type,
			String queryJson) {
		Iterator<Integer> it = MSGS.keySet().iterator();
		while (it.hasNext()) {
			int code = it.next();
			MsgQueue q = MSGS.get(code);
			int code1 = getCode(q.getChkPara(), rv.getString(q.getChkPara()));
			if (code == code1) {
				q.addMsg(id, type, queryJson);
				q.setT(System.currentTimeMillis());
			}

		}
	}

	private static int getCode(String name, String val) {
		if (name == null || val == null) {
			return -1;
		}
		String exp = name + "!@gg#!dd@xx$" + val;
		return exp.hashCode();
	}
}
