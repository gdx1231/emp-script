package com.gdxsoft.easyweb.script.display.action.extend;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gdxsoft.easyweb.utils.msnet.MList;

public class ExtOpts {

	private MList _Opts;
	private String _Exp;

	public void init(String exp) {
		this._Exp = exp;
		String exp1 = exp.trim().toUpperCase();
		_Opts = new MList();

		MList lst = new MList();
		String tmp = null;
		
		Pattern pat = Pattern.compile("(\\/\\*[^\\/]*\\*\\/)",
				Pattern.CASE_INSENSITIVE);
		Matcher mat = pat.matcher(exp1);
		while (mat.find()) {
			MatchResult mr = mat.toMatchResult();
			lst.add(mr.group());
		}
		 
		for (int i = 0; i < lst.size(); i++) {
			tmp = lst.get(i).toString();
			tmp=tmp.replace("/*", "");
			tmp=tmp.replace("*/", "");
			int loc0 = tmp.indexOf("{");
			int loc1 = tmp.indexOf("}");
			if (loc0 >= 0 && loc1 > loc0) {
				ExtOpt o = new ExtOpt();
				o.init(tmp);
				if (o.is_IsOk()) {
					_Opts.add(o);
				}
			}
		}
	}

	public int size() {
		return this._Opts.size();
	}

	public ExtOpt get(int index) {
		return (ExtOpt) this._Opts.get(index);
	}

	/**
	 * @return the _Exp
	 */
	public String getExp() {
		return _Exp;
	}
}
