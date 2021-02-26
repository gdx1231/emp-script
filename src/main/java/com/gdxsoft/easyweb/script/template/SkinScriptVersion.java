package com.gdxsoft.easyweb.script.template;

import com.gdxsoft.easyweb.utils.UFileCheck;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.msnet.MStr;
import com.gdxsoft.easyweb.utils.msnet.MTableStr;

/**
 * 用于页面脚本变化的检查
 * 
 * @author guolei
 * 
 */
public class SkinScriptVersion {

	private static SkinScriptVersion EWASCRIPTS;

	public static SkinScriptVersion ewaScripts(String cp) {
		if (EWASCRIPTS == null) {
			SkinScriptVersion chk = new SkinScriptVersion();
			EWASCRIPTS = chk;
			chk._LastChk = 0;
		}
		Long t = System.currentTimeMillis();
		if ((t - EWASCRIPTS._LastChk) > 20 * 1000) {
			EWASCRIPTS.addScript(cp, "EWA_STYLE/js/EWA.js");
			EWASCRIPTS.addScript(cp, "EWA_STYLE/js/EWA_UI.js");
			EWASCRIPTS.addScript(cp, "EWA_STYLE/js/EWA_FRAME.js");
			EWASCRIPTS.addScript(cp, "EWA_STYLE/js/EWA_UP.js");

			EWASCRIPTS._LastChk = System.currentTimeMillis();

			MStr s = new MStr();
			for (int i = 0; i < EWASCRIPTS.MAP.size(); i++) {
				String js = EWASCRIPTS.MAP.getByIndex(i);
				s.al("<script type=\"text/javascript\" src=\"" + js
						+ "\"></script>");
			}
			EWASCRIPTS._LastValue = s.toString();
		}
		return EWASCRIPTS;
	}

	private MTableStr MAP = new MTableStr();
	private long _LastChk;
	private String _LastValue;

	public void addScript(String cp, String path) {
		String realPath = UPath.getRealContextPath() + "/" + path;
		int fileCode = realPath.hashCode();
		String urlPath = cp + (cp.endsWith("/") ? "" : "/") + path;
		if (!MAP.containsKey(fileCode)) {
			addToMap(fileCode, urlPath, realPath);
		} else if (UFileCheck.fileChanged(realPath, 20)) {
			addToMap(fileCode, urlPath, realPath);
		}
	}

	private synchronized void addToMap(int code, String path, String realPath) {
		int changedCode = UFileCheck.getFileCode(realPath);
		String rst = path + "?gdx=" + changedCode;
		if (MAP.containsKey(code)) {
			MAP.removeKey(code);
		}
		//System.out.println(rst);
		MAP.put(code, rst);
	}

	public String toHtml() {
		return _LastValue;
	}

}
