package com.gdxsoft.easyweb.script.template;

import java.io.Serializable;

import com.gdxsoft.easyweb.utils.Utils;

/**
 * 配置参数
 * 
 * @author Administrator
 * 
 */
public class XItemParameters extends SetBase<XItemParameter> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4182170506915682353L;

	/**
	 * 生成页面的警告信息
	 * 
	 * @param Lang
	 *            国家语言
	 * @return
	 * @throws Exception
	 */
	public String createJsAlert(String Lang) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("var _EWA_JS_ALERT={\r\n");
		int inc = 0;
		for (int i = 0; i < this.count(); i++) {
			XItemParameter p = super.getItem(i);
			String js = this.createItemJs(p, Lang);
			if (js == null) {
				continue;
			}
			if (inc > 0) {
				sb.append(", ");
			}
			inc++;
			sb.append("\"" + p.getName() + "\":\"" + Utils.textToJscript(js) + "\"\r\n");

		}
		sb.append("};");
		return sb.toString();
	}

	private String createItemJs(XItemParameter p, String Lang) throws Exception {
		if (p.getDescriptions().count() == 0) {
			return null;
		}
		Description d = null;
		for (int i = 0; i < p.getDescriptions().count(); i++) {
			d = p.getDescriptions().getItem(i);
			if (d.getLang() != null && d.getLang().trim().equalsIgnoreCase(Lang)) {
				break;
			}
		}
		if (d == null) {
			d = p.getDescriptions().getItem(0);// 默认第一个
		}
		return d.getJs();
	}
}
