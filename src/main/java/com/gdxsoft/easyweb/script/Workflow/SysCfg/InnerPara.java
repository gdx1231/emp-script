package com.gdxsoft.easyweb.script.Workflow.SysCfg;

import java.util.Date;

import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class InnerPara {
	private MTable _Paras;

	public InnerPara() {
		this._Paras = new MTable();
	}

	void initSysPara() {
		MTable sys = new MTable();
		sys.put("DATE", new Date());
		sys.put("DATETIME", new Date());
		sys.put("UNID", Utils.getGuid());

		this._Paras.put("SYS", sys);
	}

	public Object get(String name) {
		if (name == null) {
			return null;
		}
		String n = name.trim().toUpperCase();
		String[] ns = n.split(".");
		if (ns.length == 1 || n.length() > 2) {
			return "ERROR NAME: [" + name + "]";
		}

		Object p = this._Paras.get(ns[0]);
		if (p == null) {
			return "ERROR PARAS: [" + name + "]";
		}
		MTable tb = (MTable) p;
		if (!tb.containsKey(ns[1])) {
			return "ERROR PARA: [" + name + "]";
		}
		Object v = tb.get(ns[1]);
		return v;
	}

	public void addParas(String name) {

	}
}