package com.gdxsoft.easyweb.uploader;

import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.HtmlCreator;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MListStr;

public class UploaderPage {
	private UploaderUserTemplate _UserTemplate;
	private RequestValue _RequestValue;
	private String _UpPath;
	private String _UpExts;

	public UploaderPage() {

	}

	public void init(RequestValue rv) throws Exception {
		this._RequestValue = rv;
		String uploadType = _RequestValue.getString("EWA_UP_TYPE");
		String upExts = null;
		String upPath = null;
		if (uploadType == null) {
			HtmlCreator hc = new HtmlCreator();
			hc.init(rv.getRequest(), rv.getSession(), null);
			String fromItemName = hc.getRequestValue().getString("FROMITEM");
			UserXItem item = hc.getUserConfig().getUserXItems().getItem(
					fromItemName);
			UserXItemValue v = item.getItem("Upload").getItem(0);
			uploadType = v.getItem("UpType");
			upExts = v.getItem("UpExts");
			upPath = v.getItem("UpPath");
		}
		_UserTemplate = new UploaderUserTemplate(uploadType);
		this._UpExts = upExts == null ? _UserTemplate.getExts() : upExts;
		this._UpPath = upPath == null ? _UserTemplate.getUploadPath() : upPath;
	}

	public String getHtml() {
		StringBuilder sb = new StringBuilder();
		sb.append("<style>");
		sb.append(replaceParameters(_UserTemplate.getCss()));
		sb.append("</style>");
		_RequestValue.addValue("EWA_UP_EXTS", this._UpExts);
		String js = replaceParameters(_UserTemplate.getJavascript());
		sb.append(js);

		sb.append(_UserTemplate.getHtml());
		return sb.toString();
	}

	private String replaceParameters(String s1) {
		if (s1 == null)
			return s1;
		MListStr a = Utils.getParameters(s1, "@");
		String s2 = s1;
		for (int i = 0; i < a.size(); i++) {
			String name = a.get(i);
			String val = this._RequestValue.getString(name);

			if (val != null) {
				s2 = s2.replace("@" + name, val);
			}
		}
		return s2;
	}

	/**
	 * @return the _UserTemplate
	 */
	public UploaderUserTemplate getUserTemplate() {
		return _UserTemplate;
	}

	/**
	 * @return the _UpPath
	 */
	public String getUpPath() {
		return _UpPath;
	}

	/**
	 * @return the _UpExts
	 */
	public String getUpExts() {
		return _UpExts;
	}

}
