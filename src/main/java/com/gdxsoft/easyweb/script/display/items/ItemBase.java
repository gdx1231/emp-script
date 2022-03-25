package com.gdxsoft.easyweb.script.display.items;

import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.gdxsoft.easyweb.data.DTCell;
import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.global.EwaEvents;
import com.gdxsoft.easyweb.script.InitValues;
import com.gdxsoft.easyweb.script.display.HtmlClass;
import com.gdxsoft.easyweb.script.display.HtmlUtils;
import com.gdxsoft.easyweb.script.template.SkinFrame;
import com.gdxsoft.easyweb.script.template.XItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValues;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MListStr;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class ItemBase implements IItem {
	private UserXItem _UserXItem;
	private HtmlClass _HtmlClass;
	private InitValues _InitValues; // 初始值类
	private HttpServletResponse _Response;
	private int _TagIsLfEdit = -1; // tag 的 IsLFEdit 0 不可编辑，1双击，2单击

	/**
	 * 获取Item的JSON对象，用于APP
	 * 
	 * @return
	 * @throws Exception
	 */
	public JSONObject createItemJson() throws Exception {
		String val = getValue();
		UserXItem userXItem = getUserXItem();
		XItem xItem = HtmlUtils.getXItem(getUserXItem());

		JSONObject obj = new JSONObject();

		obj.put("NAME", userXItem.getName());
		obj.put("TAG", xItem.getName());
		obj.put("VAL", val);
		return obj;
	}

	/**
	 * 生成对象HTML
	 */
	public String createItemHtml() throws Exception {
		String s1 = this.getXItemFrameHtml();
		UserXItemValue vs = _UserXItem.getItem("Tag").getItem(0);
		String tag = vs.getItem("Tag");

		String val = getValue();
		int tagIsLFEdit = this.isLfEdit();

		// 用dataRef替换id
		if (this._UserXItem.getUsingRef() == null || this._UserXItem.getUsingRef()) {
			val = this.createRefValue(val);
		}

		if ((tagIsLFEdit > 0 || tag.equalsIgnoreCase("hidden, 2018-08-30郭磊去除")) && val != null) {
			val = Utils.textToInputValue(val);
		}

		String[] encTypes = this.getEncyrptType();
		String encType = encTypes[0];
		boolean is_encyrption = true;
		if ("all".equals(encType)) {
			if (("text".equals(tag) || "textarea".equals(tag)) && (val == null || val.trim().length() == 0)) {
				val = "";
			} else if (val == null || val.length() <= 5) {
				val = "XXXXXX";
			} else {
				val = "XXXXXXXXXXXXX";
			}
		} else if ("end3".equals(encType)) {
			if (("text".equals(tag) || "textarea".equals(tag)) && (val == null || val.trim().length() == 0)) {
				val = "";
			} else if (val.length() > 3) {
				val = val.substring(0, val.length() - 3) + "xxx";
			} else {
				val = "XXX";
			}
		} else if ("before3".equals(encType)) {
			if (("text".equals(tag) || "textarea".equals(tag)) && (val == null || val.trim().length() == 0)) {
				val = "";
			} else if (val.length() > 3) {
				val = "XXX" + val.substring(3);
			} else {
				val = "XXX";
			}
		} else if ("email".equals(encType)) {
			if (("text".equals(tag) || "textarea".equals(tag)) && (val == null || val.trim().length() == 0)) {
				val = "";
			} else {
				val = "xxx@xxx.xx";
			}
		} else if ("phone".equals(encType)) {
			if (("text".equals(tag) || "textarea".equals(tag)) && (val == null || val.trim().length() == 0)) {
				val = "";
			} else {
				val = "xxx@xxx.xx";
			}
		} else {
			is_encyrption = false;
		}

		s1 = s1.replace(SkinFrame.TAG_VAL, val == null ? "" : val.replace("@", REP_AT_STR)); // 替@

		if (val != null && tag.equalsIgnoreCase("span")) {
			if (!is_encyrption) { // 在span上显示Title
				s1 = this.handleSpanTitle(s1, val);
			}
			// 将span标签显示为h1,h2....
			s1 = this.handleSpanShowAs(s1, vs);
		}
		if (tagIsLFEdit > 0) {
			return this.createEditSpan(s1, val, tagIsLFEdit);
		} else {
			return s1.trim();
		}
	}

	/**
	 * 获取加密方式 guolei 2018-03-23
	 * 
	 * @return
	 */
	public String[] getEncyrptType() {
		UserXItemValue vs;
		String[] rets = new String[2];
		rets[0] = "";
		rets[1] = "";
		try {
			if (!_UserXItem.testName("DispEnc") && _UserXItem.getItem("DispEnc").count() == 0) {
				return rets;
			}
			vs = _UserXItem.getItem("DispEnc").getItem(0);
			if (!vs.testName("EncType") && vs.getItem("EncType").equalsIgnoreCase("")) {
				return rets;
			}
			String encType = vs.getItem("EncType");
			rets[0] = encType;
			rets[1] = vs.getItem("EncShowUrl"); // 地址
			return rets;

		} catch (Exception err) {
			return rets;
		}
	}

	/**
	 * 在span上显示Title
	 * 
	 * @param s1
	 * @param val
	 * @return
	 */
	private String handleSpanTitle(String s1, String val) {
		Object title = this._HtmlClass.getItemValues().getLastValue();
		if (title == null || title.toString().equals(val)) {
			return s1;
		}
		// 引用
		/*
		 * if (this._UserXItem.getUsingRef() != null && this._UserXItem.getUsingRef()) {
		 * return s1; }
		 */
		if (title.toString().length() < 50) {
			String t = title.toString();
			String[] times = t.split(" ");
			if (times.length == 2) { // 时间去除为0的
				if (times[1].replace(":", "").replace(".", "").replace("0", "").length() == 0) {
					t = times[0];
				}
			}
			String title1 = t.toLowerCase().indexOf("<script") > 0 ? "发现脚本" : Utils.textToInputValue(t);
			s1 = s1.replace("<span", "<span title=\"" + title1 + "\"");
		}

		return s1;
	}

	/**
	 * 将span标签显示为h1,h2....
	 * 
	 * @param s1
	 * @param vs
	 * @return
	 * @throws Exception
	 */
	private String handleSpanShowAs(String s1, UserXItemValue vs) throws Exception {
		// 将span标签显示为h1,h2....
		String SpanShowAs = vs.getItem("SpanShowAs");
		if (SpanShowAs != null && SpanShowAs.trim().length() > 0) {
			String tagA = "<" + SpanShowAs.toLowerCase();
			String tagB = "</" + SpanShowAs.toLowerCase() + ">";

			int locStart = s1.indexOf("<span");
			int locEnd = s1.lastIndexOf("</span>");

			if (locStart >= 0 && locEnd > locStart) {
				String mid = s1.substring(locStart + 5, locEnd);
				MStr spanAs = new MStr();
				spanAs.a(tagA);
				spanAs.a(mid);
				spanAs.a(tagB);
				s1 = spanAs.toString();
			}
		}

		return s1;
	}

	/**
	 * 获取格式化好的单元格值
	 * 
	 * @return
	 * @throws Exception
	 */
	public String createFormatValue() throws Exception {
		String val = getValue();

		val = this.createJsonRefValue(val);
		return val;
	}

	/**
	 * 是否ListFrame的可编辑
	 * 
	 * @return
	 */
	int isLfEdit() {
		if (this._TagIsLfEdit >= 0) {
			return this._TagIsLfEdit;
		}
		if (!_HtmlClass.getSysParas().getFrameType().equalsIgnoreCase("ListFrame")) {
			this._TagIsLfEdit = 0;
			return 0; // 不是可编辑对象
		}
		try {
			UserXItemValue vs = _UserXItem.getItem("Tag").getItem(0);
			if (!vs.testName("IsLFEdit") || vs.getItem("Tag").equalsIgnoreCase("span")) {
				this._TagIsLfEdit = 0;
				return 0;
			}
			String v1 = vs.getItem("IsLFEdit");
			if (v1 != null && v1.trim().equals("1")) {
				this._TagIsLfEdit = 1;
				return 1; // double click
			} else if (v1 != null && v1.trim().equals("2")) {
				this._TagIsLfEdit = 2;
				return 2; // single click
			} else {
				this._TagIsLfEdit = 0;
				return 0;
			}
		} catch (Exception e) {
			this._TagIsLfEdit = 0;
			return 0;
		}
	}

	/**
	 * 生成ListFrame 参考值
	 * 
	 * @param val
	 * @return
	 */
	private String createRefValue(String val) {
		if (!_UserXItem.testName("DataRef")) {
			this._UserXItem.setUsingRef(false);
			return val;
		}
		try {
			if (_UserXItem.getItem("DataRef").count() == 0) {
				this._UserXItem.setUsingRef(false);
				return val;
			}
			UserXItemValue vs = _UserXItem.getItem("DataRef").getItem(0);
			String refSql = vs.getItem("RefSql");
			String refKey = vs.getItem("RefKey");
			String refShow = vs.getItem("RefShow");

			String refShowType = vs.getItem("RefShowType");
			String refShowStype = vs.getItem("RefShowStyle");
			if (refSql == null || refKey == null || refShow == null || refSql.trim().length() == 0
					|| refKey.trim().length() == 0 || refShow.trim().length() == 0) {
				this._UserXItem.setUsingRef(false);
				return val;
			}
			if (this._UserXItem.getUsingRef() == null) {
				this._UserXItem.setUsingRef(true);
			}

			// 是否多个ID的匹配，例如：CAMP_OPT_TSG,CAMP_OPT_LQC,CAMP_OPT_ZQC
			boolean refShowMulti = "yes".equals(vs.checkItemExists("RefMulti") ? vs.getItem("RefMulti") : "");

			String[] refKeys = new String[1];
			refKeys[0] = refKey;
			DTTable dt = this._HtmlClass.getItemValues().getRefTable(refSql, refKeys);
			if (dt == null) {
				return val;
			}

			if (this._HtmlClass.getSysParas().getLang().equalsIgnoreCase("enus")) {
				if (dt.getColumns().testName(refShow + "_en")) {
					refShow += "_en";
				} else if (dt.getColumns().testName(refShow + "en")) {
					refShow += "en";
				} else if (dt.getColumns().testName(refShow + "_enus")) {
					refShow += "_enus";
				} else if (dt.getColumns().testName(refShow + "enus")) {
					refShow += "enus";
				}
			}

			if (!refShowMulti) { // 单个id匹配
				DTRow row = dt.getRowByKey(refKey, val);
				return this.createRefHtml(row, refShowStype, refShowType, refShow, val);
			}

			// 多个ID的匹配， 例如 CAMP_OPT_TSG,CAMP_OPT_LQC,CAMP_OPT_ZQC
			// 字符串分割字符，默认英文逗号
			String splitChar = vs.checkItemExists("RefMultiSplit") ? vs.getItem("RefMultiSplit").trim() : ",";
			if (splitChar.length() == 0) {
				splitChar = ","; // 默认英文逗号
			}
			String[] vals = val.split(splitChar); // 正则表达式需要用户定义
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < vals.length; i++) {
				String v = vals[i].trim();
				DTRow row = dt.getRowByKey(refKey, v);
				if (i > 0) {
					sb.append(", ");
				}
				String html = this.createRefHtml(row, refShowStype, refShowType, refShow, v);
				sb.append(html);
			}
			return sb.toString();

		} catch (Exception e) {
			return e.getMessage();
		}

	}

	private String createRefHtml(DTRow row, String refShowStype, String refShowType, String refShow, String val)
			throws Exception {
		if (row == null) {
			return val;
		}
		DTCell refValCell = row.getCell(refShow);
		String refVal = refValCell == null ? "" : refValCell.getString();
		refVal = Utils.textToInputValue(refVal.trim());
		String st = refShowStype == null || refShowStype.trim().length() == 0 ? ""
				: row.getCell(refShowStype).getString();
		String span = "<Span class=\"" + st + "\" ref_key=\""+ Utils.textToInputValue(val) +"\" ";

		// Text-Key, Key-Text, Text-Title, Key-Title
		// ,文字(Key),Key(文字),文字-提示,Key-提示
		if (refShowType == null || refShowType.trim().length() == 0) {
			return span + ">" + refVal + "</span>";
		} else if (refShowType.trim().equals("Text-Key")) {
			return span + ">" + refVal + "(" + val + ")</span>";
		} else if (refShowType.trim().equals("Key-Text")) {
			return span + ">" + val + "(" + refVal + ")</span>";
		} else if (refShowType.trim().equals("Text-Title")) {
			return span + " title=\"" + Utils.textToInputValue(val) + "\">" + refVal + "</span>";
		} else if (refShowType.trim().equals("Key-Title")) {
			return span + " title=\"" + Utils.textToInputValue(val) + "\">" + refVal + "</span>";
		} else {
			return refVal;
		}
	}

	private String createJsonRefValue(String val) {
		if (!_UserXItem.testName("DataRef")) {
			return val;
		}
		try {
			if (_UserXItem.getItem("DataRef").count() == 0) {
				return val;
			}
			UserXItemValue vs = _UserXItem.getItem("DataRef").getItem(0);
			String refSql = vs.getItem("RefSql");
			String refKey = vs.getItem("RefKey");
			String refShow = vs.getItem("RefShow");
			if (refSql == null || refKey == null || refShow == null || refSql.trim().length() == 0
					|| refKey.trim().length() == 0 || refShow.trim().length() == 0) {
				return val;
			}
			String[] refKeys = new String[1];
			refKeys[0] = refKey;
			DTTable dt = this._HtmlClass.getItemValues().getRefTable(refSql, refKeys);
			if (dt == null) {
				return val;
			}

			DTRow row = dt.getRowByKey(refKey, val);
			if (row == null) {
				return val;
			}

			if (this._HtmlClass.getSysParas().getLang().equalsIgnoreCase("enus")) {
				if (dt.getColumns().testName(refShow + "_en")) {
					refShow += "_en";
				} else if (dt.getColumns().testName(refShow + "en")) {
					refShow += "en";
				} else if (dt.getColumns().testName(refShow + "_enus")) {
					refShow += "_enus";
				} else if (dt.getColumns().testName(refShow + "enus")) {
					refShow += "enus";
				}
			}

			DTCell refValCell = row.getCell(refShow);
			String refVal = refValCell == null ? "" : refValCell.getString();
			refVal = refVal == null ? "" : refVal.trim();

			return val + "~!@`" + refVal;
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	/**
	 * 获取数据字段名称，第一个为Item的名称，<br>
	 * 第二个为DataItem定义的DataField名称
	 * 
	 * @return
	 */
	String[] getDataNames() {
		String[] names = new String[2];
		names[0] = _UserXItem.getName().trim();
		String dataFieldName = names[0];

		try {
			UserXItemValues us = _UserXItem.getItem("DataItem");
			if (us.count() > 0) {
				UserXItemValue u = us.getItem(0);

				if (u.testName("DataField") && u.getItem("DataField").trim().length() > 0) {
					dataFieldName = u.getItem("DataField");
				}
			}
		} catch (Exception e) {

		}
		names[1] = dataFieldName;
		return names;
	}

	/**
	 * 生成Span的可编辑数据
	 * 
	 * @param itemHtmlTemplate
	 * @param val
	 * @param tagIsLFEdit      可编辑的鼠标点击方式（1=双击还是2=单击）
	 * @return
	 */
	public String createEditSpan(String itemHtmlTemplate, String val, int tagIsLFEdit) {
		// String js1 = "EWA.F.FOS['" + _HtmlClass.getSysParas().getFrameUnid() +
		// "'].ShowEdit(this);";

		String html = itemHtmlTemplate.replace(" id=\"", " edit_id=\"");
		String js1 = "EWA.F.FOS['@sys_frame_unid'].ShowEdit( this );";
		// ListFrame的对象可编辑IsLFEdit是运行编辑的
		MStr sb = new MStr();

		String clickType = tagIsLFEdit == 2 ? "onclick" : "ondblclick";

		sb.append("<div class='EWA_LF_EDIT EWA_LF_EDIT_TXT' " + clickType + "=\"");
		sb.append(js1);
		sb.append("\">");
		sb.append(val == null ? "&nbsp;" : val.replace("<", "&lt;").replace("\n", "<br>"));
		sb.append(" </div><div class='EWA_LF_EDIT EWA_LF_EDIT_CTRL' style='display:none'>" + html + "</div>");
		return sb.toString();
	}

	/**
	 * 获取对象值，如果为空，则取初始化值的定义
	 */
	public String getValue() throws Exception {
		String val;
		if (_UserXItem.testName("InitValue")) {
			UserXItemValues uxvs = _UserXItem.getItem("InitValue");
			if (uxvs.count() > 0) {
				String initValue = uxvs.getItem(0).getItem(0);
				if (initValue != null && initValue.trim().length() > 0) {
					val = this._InitValues.getInitValue(initValue);
					return val;
				}
			}
		}

		val = this._HtmlClass.getItemValues().getValue(_UserXItem);
		if (val == null) {
			return null;
		}
		String tag = this._UserXItem.getItem("Tag").getItem(0).getItem(0).trim().toLowerCase();
		if (tag.indexOf("dhtml") >= 0) {
			if (val.toLowerCase().indexOf("<script") >= 0) {
				val = val.toLowerCase().replace("<script", "[脚本1").replace("</script", "[/脚本1");
			}

			val = Utils.textToInputValue(val);
			return val;
		}
		String isHtmlValue = "";
		boolean isHtml = false; // 显示为HTML内码
		if (_UserXItem.testName("IsHtml") && _UserXItem.getItem("IsHtml").count() > 0) {

			isHtmlValue = _UserXItem.getItem("IsHtml").getItem(0).getItem(0);

			if (isHtmlValue.equals("1")) {
				isHtml = true;
			}
		}
		if (!isHtml) { // 替换标签
			val = Utils.textToInputValue(val);
			if (tag.equals("span") || tag.equals("user")) {
				if (isHtmlValue.equals("2")) { // 替换回车为P标签
					StringBuilder sb = new StringBuilder();
					sb.append("<p>");
					sb.append(val.replace("\n", "</p><p>"));
					sb.append("</p>");
					val = sb.toString();
				} else if (isHtmlValue.equals("3")) {
					// 不替换回车
				} else {// 替换回车为<br>标签
					val = val.replace("\n", "<br />");
				}
			}
			return val;
		} else {
			if (val.toLowerCase().indexOf("<script") >= 0) {
				val = val.toLowerCase().replace("<script", "[脚本2").replace("</script", "[/脚本2");
			}
		}

		return val;
	}

	/**
	 * 获取对象的模板文件
	 * 
	 * @return
	 * @throws Exception
	 */
	String getXItemFrameHtml() throws Exception {
		if (_UserXItem.getHtml() != null && _UserXItem.getHtml().length() > 0) {
			return _UserXItem.getHtml();
		}
		XItem xItem = HtmlUtils.getXItem(this._UserXItem);
		String s1 = xItem.getTemplateHtml();
		StringBuilder sb = new StringBuilder();
		// 附加属性 Attribute ,Event, Style ...

		HashMap<String, HashMap<String, String>> addParas = this.createItemAddHtml();
		for (String key : addParas.keySet()) {
			HashMap<String, String> paras = addParas.get(key);
			boolean is_find_class = false;
			if (paras.containsKey("AttName")) {
				String val = paras.get("AttName");
				if (val.trim().equalsIgnoreCase("class")) {
					is_find_class = true;
				}
			}

			if (is_find_class) { // attributeSet 指定了class属性
				String val = paras.get("AttValue").trim();
				if (s1.indexOf(" class=\"") > 0) {
					s1 = s1.replace(" class=\"", " class=\"" + val + " ");
				} else if (s1.indexOf(" class='") > 0) {
					s1 = s1.replace(" class='", " class='" + val + " ");
				}
			} else {
				String val = paras.get("---GDX-RST---").trim();
				if (val.indexOf("EWA.UI.Dialog.@CallMethod") > 0) {
					// 参数指定 OpenFrame没有定义 CallMethod参数，这是无效的
				} else {
					sb.append(" " + val);
				}
			}
		}

		MListStr a = Utils.getParameters(s1, "@");
		if (a.size() > 0) {
			String h1 = s1;
			for (int i = 0; i < _UserXItem.count(); i++) {
				// html参数模板 EwaConfig.Xml定义
				// <XItemParameter Name="Name" Html='name="@Name" id="@Name"'
				// IsJsShow="1">
				UserXItemValues _UserXItemValues = _UserXItem.getItem(i);
				for (int i0 = 0; i0 < _UserXItemValues.count(); i0++) {
					UserXItemValue uv = _UserXItemValues.getItem(i0);
					for (int i1 = 0; i1 < a.size(); i1++) {
						String key = a.get(i1);
						if (!uv.testName(key)) {
							continue;
						}
						String v = uv.getItem(key);
						if (v == null || v.trim().equals("")) {
							continue;
						}

						v = v.replace("\"", "&quot;"); // 属性替换
						h1 = h1.replace("@" + key, v);
					}
					if (h1.indexOf("@") < 0) {
						break;
					}
				}

			}
			s1 = h1;
		}
		String tag = xItem.getName().trim().toLowerCase();
		if (tag.equals("select")) {
			// 避免当xmlname不一致时，sys_frame_unid会被缓存的bug;
			// guolei 2017-04-01
			String event = "EWA.F.FOS['@sys_frame_unid'].CheckValid(this);";
			sb.append(" onblur=\"" + event + "\"");

			if (this._UserXItem.testName("List") && this._UserXItem.getItem("List").count() > 0) {
				UserXItemValue listXItem = this._UserXItem.getItem("List").getItem(0);
				if (listXItem.testName("ListFilterType")) {
					String ListFilterType = listXItem.getItem("ListFilterType");
					String ListFilterField = listXItem.getItem("ListFilterField");
					if (ListFilterType.trim().length() > 0) {
						sb.append(" _ListFilterType=\"" + ListFilterType + "\"");
						sb.append(" _ListFilterField=\"" + ListFilterField + "\"");
					}
				}
			}
			// 从数据库显示列表信息

		} else if ("valid".equals(tag)) {
			String event = "EWA.F.FOS['@sys_frame_unid'].CheckValid(this);";
			sb.append(" onchange=\"" + event + "\"");
			sb.append(" onfocus='if(window.EWA_FrameRemoveAlert){EWA_FrameRemoveAlert(this)}' ");
		} else if (!(tag.equalsIgnoreCase("user") || tag.equalsIgnoreCase("ewaconfigitem")
				|| tag.equalsIgnoreCase("hidden") || tag.equalsIgnoreCase("anchor") || tag.equalsIgnoreCase("anchor2")
				|| tag.equalsIgnoreCase("span") || tag.equalsIgnoreCase("gridBgImage")
				|| tag.equalsIgnoreCase("gridImage") || tag.equalsIgnoreCase("button") || tag.equalsIgnoreCase("submit")
				|| tag.equalsIgnoreCase("span"))) {

			// String event = "EWA.F.FOS['" +
			// this._HtmlClass.getSysParas().getFrameUnid() +
			// "'].CheckValid(this);";

			// 避免当xmlname不一致时，sys_frame_unid会被缓存的bug;
			// guolei 2016-11-18
			String event = "EWA.F.FOS['@sys_frame_unid'].CheckValid(this);";
			sb.append(" oninput=\"" + event + "\"");
			sb.append(" onmousedown=\"" + event + "\"");
		}
		MStr ss = new MStr(s1);

		// 检查是否带图标
		String icon = null;
		String iconLoction = "left";
		if (this._UserXItem.testName("DataItem") && this._UserXItem.getItem("DataItem").count() > 0) {
			UserXItemValue dataItem = this._UserXItem.getItem("DataItem").getItem(0);
			if (dataItem.testName("Icon")) {
				icon = dataItem.getItem("Icon");

			}
			if (dataItem.testName("IconLoction")) {
				iconLoction = dataItem.getItem("IconLoction");
			}
		}
		if (icon != null && icon.trim().length() > 0) {
			String for_id = this._UserXItem.getItem("Name").getItem(0).getItem("Name");
			MStr s_icon = new MStr();
			String css_left_right = "ewa-with-icon-left ";
			if (iconLoction.equals("right")) {
				css_left_right = "ewa-with-icon-right ";
			}
			css_left_right += "ewa-tag-" + tag.toLowerCase();
			String s_icon_exp = "<label for=\"" + for_id + "\" class=\"" + Utils.textToInputValue(icon) + "\"></label>";
			if (tag.equals("valid")) { // 验证码
				String html = ss.toString();
				int loc0 = html.indexOf("<input");
				int loc1 = html.indexOf("</td");
				String top = html.substring(0, loc0);
				String bottom = html.substring(loc1);
				String mid = html.substring(loc0, loc1);

				s_icon.a(top);
				s_icon.a("<div class=\"ewa-with-icon " + css_left_right + "\">");
				if (iconLoction.equals("right")) {
					s_icon.a(mid);
					s_icon.a(s_icon_exp);
				} else {
					s_icon.a(s_icon_exp);
					s_icon.a(mid);
				}
				s_icon.a("</div>");
				s_icon.a(bottom);
			} else {

				s_icon.a("<div class=\"ewa-with-icon " + css_left_right + "\">");
				if (iconLoction.equals("right")) {
					s_icon.a(ss.toString());
					s_icon.a(s_icon_exp);
				} else {
					s_icon.a(s_icon_exp);
					s_icon.a(ss.toString());
				}
				s_icon.a("</div>");
			}
			ss = s_icon;
		}

		// 替换一次
		ss.replace("!!", sb.toString());
		s1 = ss.toString();
		_UserXItem.setHtml(s1);
		// if (s1.indexOf("disabled") >= 0) {
		// int z = 1;
		// z++;
		// }
		return s1;
	}

	/**
	 * 生成附加的属性 attributeSet, eventSet 在此替换
	 * 
	 * @param _UserXItem
	 * @return
	 * @throws Exception
	 */
	HashMap<String, HashMap<String, String>> createItemAddHtml() throws Exception {
		HashMap<String, HashMap<String, String>> sb = new HashMap<String, HashMap<String, String>>();

		EwaEvents events = this.getHtmlClass().getEwaGlobals().getEwaEvents();
		int inc = 0;
		for (int i = 0; i < _UserXItem.count(); i++) {
			// html参数模板 EwaConfig.Xml定义
			// <XItemParameter Name="Name" Html='name="@Name" id="@Name"'
			// IsJsShow="1">
			UserXItemValues _UserXItemValues = _UserXItem.getItem(i);
			String html = _UserXItemValues.getParameter().getHtml();
			if (html == null || html.trim().length() == 0) {
				continue;
			}
			if (html.indexOf("\"@ParentStyle\"") > 0) {
				continue;
			}
			MListStr a = Utils.getParameters(html, "@");
			for (int i0 = 0; i0 < _UserXItemValues.count(); i0++) {
				String h1 = html;
				UserXItemValue uv = _UserXItemValues.getItem(i0);
				HashMap<String, String> paras = new HashMap<String, String>();

				boolean ok = false;
				for (int i1 = 0; i1 < a.size(); i1++) {
					String key = a.get(i1);
					if (!uv.testName(key)) {
						continue;
					}
					String v = uv.getItem(key);
					if (v == null || v.trim().equals("")) {
						continue;
					}
					if (events.testName(v)) {// 是否为内置的事件
						v = events.getItem(v).getFrontValue(); // 获取内部前端脚本
						_UserXItem.setIsUsingEwaEvent(true);
					}
					if (key.equalsIgnoreCase("DlsShow")) {
						v = v.replace("@", ItemBase.REP_AT_STR);
					}
					v = v.replace("\"", "&quot;");
					h1 = h1.replace("@" + key, v);
					ok = true;

					paras.put(key, v);
				}
				if (ok) {
					inc++;
					paras.put("---GDX-RST---", h1);
					sb.put(html + inc, paras);
				}
			}
		}

		return sb;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IItem#getUserXItem()
	 */
	public UserXItem getUserXItem() {
		return _UserXItem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdxsoft.easyweb.script.display.IItem#setUserXItem(com.gdxsoft.easyweb
	 * .script.userConfig.UserXItem)
	 */
	public void setUserXItem(UserXItem userXItem) {
		_UserXItem = userXItem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IItem#getInitValues()
	 */
	public InitValues getInitValues() {
		return _InitValues;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IItem#setInitValues(com.gdxsoft.
	 * easyweb .script.InitValues)
	 */
	public void setInitValues(InitValues initValues) {
		_InitValues = initValues;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IItem#getResponse()
	 */
	public HttpServletResponse getResponse() {
		return _Response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IItem#setResponse(javax.servlet.http
	 * .HttpServletResponse)
	 */
	public void setResponse(HttpServletResponse response) {
		_Response = response;
	}

	/**
	 * @return the _HtmlClass
	 */
	public HtmlClass getHtmlClass() {
		return _HtmlClass;
	}

	/**
	 * @param htmlClass the _HtmlClass to set
	 */
	public void setHtmlClass(HtmlClass htmlClass) {
		_HtmlClass = htmlClass;
	}

	/**
	 * tag 的 IsLFEdit 0 不可编辑，1双击，2单击
	 * 
	 * @return the _TagIsLfEdit
	 */
	public int getTagIsLfEdit() {
		return _TagIsLfEdit;
	}

}
