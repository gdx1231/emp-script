package com.gdxsoft.easyweb.script.display.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.HtmlUtils;
import com.gdxsoft.easyweb.script.template.SkinFrame;
import com.gdxsoft.easyweb.script.template.XItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValues;
import com.gdxsoft.easyweb.utils.UPinYin;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MListStr;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class ItemRepeat extends ItemBase {
	private DTTable _Table;
	private String _Value;
	private String _Text;
	private String _Title;

	/**
	 * 获取Item的JSON对象，用于APP
	 * 
	 * @return
	 * @throws Exception
	 */
	public JSONObject createItemJson() throws Exception {
		String val = super.getValue();
		UserXItem userXItem = super.getUserXItem();
		XItem xItem = HtmlUtils.getXItem(super.getUserXItem());

		JSONObject obj = new JSONObject();

		obj.put("NAME", userXItem.getName());
		obj.put("TAG", xItem.getName());
		obj.put("VAL", val);

		obj.put("LST", createRepeatItemJson());

		return obj;
	}

	private JSONArray createRepeatItemJson() throws Exception {
		UserXItem userXItem = super.getUserXItem();
		XItem xItem = HtmlUtils.getXItem(super.getUserXItem());

		UserXItemValues listXItems = userXItem.getItem("List");
		if (listXItems.count() == 0) {
			return new JSONArray();
		}
		UserXItemValue listXItem = listXItems.getItem(0);

		String sql = listXItem.getItem("Sql");
		// 从数据库显示列表信息
		if (!(sql == null || sql.trim().length() == 0)) {
			_Text = listXItem.getItem("DisplayField");
			_Value = listXItem.getItem("ValueField");
			String titleField = "";
			if (listXItem.testName("TitleField")) {
				titleField = listXItem.getItem("TitleField").trim();
			}
			_Title = titleField;
			return this.createdJsonBySql(userXItem, listXItem, xItem);
		} else {// 分割字符串
			_Value = listXItem.getItem("ValueList");
			_Text = listXItem.getItem("DisplayList");

			return this.createdJSonByString(userXItem, listXItem, xItem);
		}
	}

	private JSONArray createdJsonBySql(UserXItem userXItem, UserXItemValue listXItem, XItem xItem) throws Exception {
		if (this._Table == null) { // 获取数据
			this.getDataBySql(listXItem, this._Value);
		}

		JSONArray arr = new JSONArray();
		// select 增加第一行为 空选项
		if (xItem.getName().equalsIgnoreCase("select") && listXItem.testName("ListAddBlank")) {
			try {
				String listAddBlank = listXItem.getItem("ListAddBlank");
				if (listAddBlank != null && listAddBlank.trim().equals("1")) {
					JSONObject obj = new JSONObject();
					obj.put("T", "");
					obj.put("V", "");
					arr.put(obj);
				}
			} catch (Exception e) {
			}
		}
		RequestValue rv = super.getHtmlClass().getSysParas().getRequestValue();
		String lang = rv.getLang();
		boolean isEn = "enus".equalsIgnoreCase(lang);
		for (int i = 0; i < this._Table.getCount(); i++) {
			DTRow row = this._Table.getRow(i);
			// String v = row.getCell(this._Value).getValue() == null ? "" :
			// row.getCell(this._Value).toString();
			// String t = row.getCell(this._Text).getValue() == null ? "" :
			// row.getCell(this._Text).toString();
			String v = this.createOptionText(row, this._Value, isEn);
			String t = this.createOptionText(row, this._Text, isEn);
			JSONObject obj = new JSONObject();
			obj.put("T", t);
			obj.put("V", v);
			if (this._Title != null && this._Title.length() > 0) {
				// String title = row.getCell(this._Title).getValue() == null ? "" :
				// row.getCell(this._Title).toString();
				String title = this.createOptionText(row, this._Title, isEn);
				obj.put("TT", title);
			}
			obj.put("json", row.toJson().toString());

			arr.put(obj);

		}
		return arr;
	}

	private JSONArray createdJSonByString(UserXItem userXItem, UserXItemValue listXItem, XItem xItem) throws Exception {
		JSONArray arr = new JSONArray();

		String valueList = this._Value == null ? "" : this._Value;
		String displayList = this._Text == null ? "" : this._Text;

		String[] vls = valueList.split(",");
		String[] dls = displayList.split(",");
		for (int i = 0; i < vls.length; i++) {
			String v = vls[i];
			String t = dls[i];

			JSONObject obj = new JSONObject();
			obj.put("T", t);
			obj.put("V", v);
			arr.put(obj);

		}

		return arr;
	}

	public String createItemHtml() throws Exception {

		String s1 = super.getXItemFrameHtml();
		String repeatHtml = this.createRepeatItemHtml();

		s1 = s1.replace(SkinFrame.TAG_VAL, repeatHtml == null ? "" : repeatHtml.replace("@", IItem.REP_AT_STR)); // 替换值

		int tagIsLFEdit = super.isLfEdit();
		if (tagIsLFEdit > 0) {
			String showVal = this.getLfShowVal();
			return super.createEditSpan(s1, showVal, tagIsLFEdit);
		} else {
			return s1.trim();
		}

	}

	/**
	 * 生成Select,radio,checkbox
	 * 
	 * @param userXItem
	 * @return
	 * @throws Exception
	 */
	private String createRepeatItemHtml() throws Exception {
		UserXItem userXItem = super.getUserXItem();
		XItem xItem = HtmlUtils.getXItem(super.getUserXItem());
		String repeatHtml = xItem.getTemplateRepeat().trim();
		String val = super.getValue();

		String frameType = super.getHtmlClass().getSysParas().getFrameType();

		if (frameType.equalsIgnoreCase("ListFrame") && !xItem.getName().trim().equalsIgnoreCase("select")) {
			String s2 = repeatHtml.replace(SkinFrame.TAG_LST_VAL, val == null ? "" : val);
			s2 = s2.replace(SkinFrame.TAG_LST_TXT, "");
			s2 = s2.replace(SkinFrame.TAG_LST_IDS, "");
			s2 = s2.replace(SkinFrame.TAG_LST_TITLE, "");
			String rep1 = " NAME=\"" + userXItem.getName() + "\"";
			s2 = s2.replace("!!", rep1);
			return s2;
		}

		UserXItemValues listXItems = userXItem.getItem("List");
		if (listXItems.count() == 0) {
			// 没有定义内容的 Select
			return "";
		}
		UserXItemValue listXItem = listXItems.getItem(0);

		String sql = listXItem.getItem("Sql");
		String s;
		// 从数据库显示列表信息
		if (!(sql == null || sql.trim().length() == 0)) {
			_Text = listXItem.getItem("DisplayField");
			_Value = listXItem.getItem("ValueField");
			String titleField = "";
			if (listXItem.testName("TitleField")) {
				titleField = listXItem.getItem("TitleField").trim();
			}
			_Title = titleField;

			s = this.createdBySql(userXItem, listXItem, xItem, repeatHtml, val);
		} else {// 分割字符串
			_Value = listXItem.getItem("ValueList");
			_Text = listXItem.getItem("DisplayList");
			String titleList = "";
			if (listXItem.testName("TitleList")) {
				titleList = listXItem.getItem("TitleList").trim();
			}
			_Title = titleList;
			s = this.createdByString(userXItem, listXItem, xItem, repeatHtml, val);
		}
		return s;
	}

	private void buildTreeData(String parentField) throws Exception {
		String rootKey = "___ROOT___";
		JSONObject root = new JSONObject();
		Map<String, JSONObject> map = new HashMap<String, JSONObject>();
		map.put(rootKey, root);
		List<JSONObject> orphans = new ArrayList<JSONObject>();
		for (int i = 0; i < this._Table.getCount(); i++) {
			DTRow row = this._Table.getRow(i);
			JSONObject node = row.toJson();

			String key = row.getCell(this._Value).toString();
			map.put(key, node);

			String pKey = row.getCell(parentField).toString();
			if (pKey == null || pKey.trim().length() == 0 || pKey.equals("0")) {
				pKey = rootKey;
			}

			if (map.containsKey(pKey)) {
				JSONObject parent = map.get(pKey);
				if (!parent.has("__children__")) {
					parent.put("__children__", new JSONArray());
				}

				JSONArray children = parent.getJSONArray("__children__");
				children.put(node);
			} else {
				// 孤儿节点
				orphans.add(node);
			}
		}

		// 重复扫描5次，将孤儿找到父节点
		for (int i = 0; i < 5; i++) {
			List<JSONObject> orphans1 = new ArrayList<JSONObject>();
			for (int m = 0; m < orphans.size(); m++) {
				JSONObject node = orphans.get(m);
				String pKey = node.optString(parentField);

				if (map.containsKey(pKey)) {
					JSONObject parent = map.get(pKey);
					if (!parent.has("__children__")) {
						parent.put("__children__", new JSONArray());
					}

					JSONArray children = parent.getJSONArray("__children__");
					children.put(node);
				} else {
					orphans1.add(node);
				}
			}
			if (orphans1.size() == 0) {
				break;
			}
			orphans = orphans1;
		}

		JSONArray arr = new JSONArray();

		this.reverse(root, 0, arr);

		// 将孤儿数据放到列表里
		for (int m = 0; m < orphans.size(); m++) {
			JSONObject node = orphans.get(m);
			arr.put(node);
		}

		DTTable tb1 = new DTTable();
		tb1.setColumns(this._Table.getColumns());

		tb1.initData(arr);

		this._Table = tb1;

	}

	/**
	 * 递归排列数据
	 * 
	 * @param parent
	 * @param lvl
	 * @param arr
	 */
	private void reverse(JSONObject parent, int lvl, JSONArray arr) {
		if (!parent.has("__children__")) {
			return;
		}

		JSONArray children = parent.getJSONArray("__children__");
		for (int i = 0; i < children.length(); i++) {
			JSONObject chd = children.getJSONObject(i);
			if (lvl > 0) {
				String text = chd.optString(this._Text);
				StringBuilder sb = new StringBuilder();
				for (int m = 0; m < lvl; m++) {
					// 增加前导空格，中文空格
					sb.append("　");

				}
				sb.append(text);
				String newtext = sb.toString();

				chd.put(this._Text, newtext);
				chd.put("__old_" + this._Text + "__", text);
			}
			arr.put(chd);
			this.reverse(chd, lvl + 1, arr);
		}
	}

	/**
	 * 从数据库中生成
	 * 
	 * @param userXItem
	 * @param listXItem
	 * @param xItem
	 * @param repeatHtml
	 * @param val
	 * @return
	 * @throws Exception
	 */
	private String createdBySql(UserXItem userXItem, UserXItemValue listXItem, XItem xItem, String repeatHtml,
			String val) throws Exception {
		MStr sb = new MStr();

		if (this._Table == null) { // 获取数据
			this.getDataBySql(listXItem, this._Value);
		}

		// 英文的标记处理
		RequestValue rv = super.getHtmlClass().getSysParas().getRequestValue();
		String lang = rv.getLang();
		boolean isEn = "enus".equalsIgnoreCase(lang);

		String parentField = null;
		if (xItem.getName().equalsIgnoreCase("select") && listXItem.testName("ParentField")) {
			try {
				parentField = listXItem.getItem("ParentField").trim();
			} catch (Exception e) {
			}
		}

		if (parentField != null && parentField.length() > 0) {
			// 创建树状数据结构
			this.buildTreeData(parentField);
		}

		// select 增加第一行为 空选项
		if (xItem.getName().equalsIgnoreCase("select") && listXItem.testName("ListAddBlank")) {
			try {
				String listAddBlank = listXItem.getItem("ListAddBlank");
				if (listAddBlank != null && listAddBlank.trim().equals("1")) {
					sb.appendLine("<option></option>");
				}
			} catch (Exception e) {
			}
		}
		String groupField = null;
		if (xItem.getName().equalsIgnoreCase("select") && listXItem.testName("GroupField")) {
			try {
				groupField = listXItem.getItem("GroupField").trim();

			} catch (Exception e) {
			}
		}
		String last_groupValue = null;

		// 是否创建拼音
		boolean createPy = false;
		if (listXItem.testName("ListFilterType") && listXItem.testName("ListFilterField")) {
			String listFilterType = listXItem.getItem("ListFilterType").trim().toLowerCase();
			String listFilterField = listXItem.getItem("ListFilterField").trim().toLowerCase();
			if ("json".equalsIgnoreCase(listFilterType)
					&& listFilterField.startsWith("ewa_py_")) {
				createPy = true;
			}
		}

		for (int i = 0; i < this._Table.getCount(); i++) {
			String id = userXItem.getName() + "_" + i;
			DTRow row = this._Table.getRow(i);
			if (groupField != null && groupField.length() > 0) {
				String grpValue = row.getCell(groupField).getValue() == null ? "" : row.getCell(groupField).toString();
				if (last_groupValue == null || !last_groupValue.equals(grpValue)) {
					if (last_groupValue != null) {
						sb.al("</optgroup>");
					}
					sb.al("<optgroup label=\"" + grpValue + "\">");
				}
				last_groupValue = grpValue;
			}
			// String v = row.getCell(this._Value).getValue() == null ? "" :
			// row.getCell(this._Value).toString();
			// String t = row.getCell(this._Text).getValue() == null ? "" :
			// row.getCell(this._Text).toString();

			// 根据表达式生成表达内容，例如 @ADM_NAME(@DEP_NAME)
			String v = this.createOptionText(row, this._Value, isEn);
			String t = this.createOptionText(row, this._Text, isEn);

			// 过滤<符号
			v = Utils.textToInputValue(v);
			t = Utils.textToInputValue(t);

			// 根据显示模式生成text
			t = this.createSelectShowType(v, t, xItem, listXItem);

			String title = "";
			if (this._Title.length() > 0) {
				// title = row.getCell(_Title).getValue() == null ? "" :
				// row.getCell(_Title).toString();
				title = createOptionText(row, this._Title, isEn);
				title = Utils.textToInputValue(title);
			}
			String s2 = repeatHtml.replace(SkinFrame.TAG_LST_VAL, v);
			s2 = s2.replace(SkinFrame.TAG_LST_TXT, t);
			s2 = s2.replace(SkinFrame.TAG_LST_IDS, id);
			s2 = s2.replace(SkinFrame.TAG_LST_TITLE, title);

			String rep1 = "";
			if (!xItem.getName().equalsIgnoreCase("select")) {
				rep1 = " NAME=\"" + userXItem.getName() + "\"";
			}
			if (this.checkRepeatMarked(xItem, val, v)) {
				rep1 += " checked selected ";
			}
			try {
				JSONObject rowJson = row.toJson();
				if (createPy) { // 创建拼音
					for (int m = 0; m < row.getTable().getColumns().getCount(); m++) {
						String sv = row.getCell(m).toString();
						String name = row.getTable().getColumns().getColumn(m).getName();
						String py = "";
						if (sv != null) {
							sv = sv.trim();
							try {
								py = sv.length() == 0 ? ""
										: UPinYin.convertToPinyinFirstAlpha(sv.substring(0, 1), true);
							} catch (Exception e) {
								py = e.getMessage();
							}
						}
						// 添加拼音头字母
						rowJson.put("ewa_py_" + name, py.toUpperCase());
					}
				}
				rep1 += " json=\"" + Utils.textToInputValue(rowJson.toString()) + "\" ";
			} catch (JSONException err) {
				rep1 += " json=\"" + Utils.textToInputValue(err.toString()) + "\" ";
			}
			s2 = s2.replace("!!", rep1);
			sb.appendLine(s2);
		}
		if (last_groupValue != null) {
			sb.al("</optgroup>");
		}
		return sb.toString();
	}

	/**
	 * 根据表达式生成option选项内容<br>
	 * 1、 @ADM_NAME(@DEP_NAME)<br>
	 * 2、传统模式，字段名称
	 * 
	 * @param row
	 * @param textExp
	 * 
	 * @return
	 */
	private String createOptionText(DTRow row, String textExp, boolean isEn) {
		MListStr a = Utils.getParameters(textExp, "@");
		if (a.size() == 0) {
			int textIndex = this.tryGetFieldIndex(row.getTable(), textExp, isEn);
			if (textIndex >= 0) {
				// 传统模式，返回字段内容
				return row.getCell(textIndex).getValue() == null ? "" : row.getCell(textIndex).toString();
			} else {
				return textExp;
			}
		}

		// 表达式模式
		MStr sb = new MStr(textExp);
		for (int i = 0; i < a.size(); i++) {
			String name = a.get(i);
			int nameIndex = this.tryGetFieldIndex(row.getTable(), name, isEn);
			if (nameIndex == -1) {
				continue;
			}
			String val = row.getCell(nameIndex).getValue() == null ? "" : row.getCell(nameIndex).toString();

			String find = "@" + name;
			// 替换一次
			sb.replace(find, val);
		}
		return sb.toString();

	}

	/**
	 * 试图获取字段，如果是英文的话，则尝试在多个字段中获取
	 * 
	 * @param tb
	 * @param fieldName
	 * @param isEn
	 * @return
	 */
	private int tryGetFieldIndex(DTTable tb, String fieldName, boolean isEn) {
		if (!isEn) {
			return tb.getColumns().getNameIndex(fieldName);
		}

		String[] names = { fieldName + "_EN", fieldName + "EN", fieldName + "_ENUS", fieldName + "ENUS",
				"EN_" + fieldName, "EN" + fieldName, fieldName };
		for (int i = 0; i < names.length; i++) {
			int index = tb.getColumns().getNameIndex(names[i]);
			if (index >= 0) {
				return index;
			}
		}
		return -1;
	}

	/**
	 * 如果是select，根据显示模式生成
	 * 
	 * @param val
	 * @param text
	 * @param xItem
	 * @param listXItem
	 * @return
	 */
	private String createSelectShowType(String val, String text, XItem xItem, UserXItemValue listXItem) {
		if (!xItem.getName().equalsIgnoreCase("select")) {
			return text;
		}

		if (!listXItem.testName("ListShowType")) {
			return text;
		}

		try {
			String listShowType = listXItem.getItem("ListShowType");
			if (listShowType == null || listShowType.trim().length() == 0) {
				return text;
			}
			if (listShowType.equalsIgnoreCase("Text-Key")) {
				return text + " (" + val + ")";
			} else {
				return val + " (" + text + ")";
			}

		} catch (Exception e) {
			return text;
		}

	}

	/**
	 * 从字符串中生成
	 * 
	 * @param userXItem
	 * @param listXItem
	 * @param xItem
	 * @param repeatHtml
	 * @param val
	 * @return
	 * @throws Exception
	 */
	private String createdByString(UserXItem userXItem, UserXItemValue listXItem, XItem xItem, String repeatHtml,
			String val) throws Exception {
		MStr sb = new MStr();
		String valueList = this._Value == null ? "" : this._Value;
		String displayList = this._Text == null ? "" : this._Text;
		String titleList = this._Title == null ? "" : this._Title;

		// 英文的标记处理 2017-04-26 guolei
		RequestValue rv = super.getHtmlClass().getSysParas().getRequestValue();
		String lang = rv.getLang();
		if (lang != null && lang.equalsIgnoreCase("enus") && !titleList.isEmpty()) {
			displayList = titleList;
			titleList = "";
		}

		String[] vls = valueList.split(",");
		String[] dls = displayList.split(",");
		String[] titles = titleList.split(",");
		for (int i = 0; i < vls.length; i++) {
			String id = userXItem.getName() + "_" + i;
			String v = vls[i];
			String t = dls[i];

			// 根据显示模式生成text
			t = this.createSelectShowType(v, t, xItem, listXItem);

			String s2 = repeatHtml.replace(SkinFrame.TAG_LST_VAL, v);
			s2 = s2.replace(SkinFrame.TAG_LST_TXT, t);
			s2 = s2.replace(SkinFrame.TAG_LST_IDS, id);
			if (titles.length > 0 && titles.length == vls.length) {
				s2 = s2.replace(SkinFrame.TAG_LST_TITLE, titles[i]);
			} else {
				s2 = s2.replace(SkinFrame.TAG_LST_TITLE, "");
			}
			String rep1 = "";
			if (!xItem.getName().equalsIgnoreCase("select")) {
				rep1 = " NAME=\"" + userXItem.getName() + "\"";
			}
			if (this.checkRepeatMarked(xItem, val, vls[i])) {
				if (xItem.getName().equalsIgnoreCase("select")) {
					rep1 += " selected ";
				} else {
					rep1 += " checked ";
				}
			}
			s2 = s2.replace("!!", rep1);
			sb.append(s2 + "\r\n");
		}

		return sb.toString();
	}

	/**
	 * 检查select,radio,checkbox 是否选中
	 * 
	 * @param xItem
	 * @param requestValue
	 * @param optionValue
	 * @return
	 */
	private boolean checkRepeatMarked(XItem xItem, String requestValue, String optionValue) {
		if (requestValue == null || optionValue == null) {
			return false;
		}

		if (xItem.getName().trim().equalsIgnoreCase("checkbox")) {
			String[] rvs = requestValue.split(",");
			for (int i = 0; i < rvs.length; i++) {
				if (rvs[i].equals(optionValue)) {
					return true;
				}
			}
			return false;
		} else { // radio
			if (requestValue.trim().toLowerCase().equals("true")) {
				requestValue = "1";
			}
			if (requestValue.trim().toLowerCase().equals("false")) {
				requestValue = "0";
			}

			if (optionValue.trim().toLowerCase().equals("true")) {
				optionValue = "1";
			}
			if (optionValue.trim().toLowerCase().equals("false")) {
				optionValue = "0";
			}
			if (requestValue.trim().equals(optionValue)) {
				return true;
			} else {
				return false;
			}

		}
	}

	/**
	 * 获取重复项的数据定义
	 * 
	 * @param listXItem
	 * @param valueField
	 * @throws Exception
	 */
	private void getDataBySql(UserXItemValue listXItem, String valueField) throws Exception {
		DTTable tb;

		String sql = listXItem.getItem("Sql").trim();
		String[] keys = new String[1];
		keys[0] = valueField;

		tb = super.getHtmlClass().getItemValues().getRefTable(sql, keys);

		// Common data 定义了 FIELDVALUE和FIELDDISPLAY，如果当前未定义，使用common data的定义
		if (tb.getAttsTable().get("TYPE") != null && tb.getAttsTable().get("TYPE").toString().equals("COMMON_DATA")) {
			if (this._Value == null || this._Value.trim().length() == 0) {
				// DisplayField 未定义，取CommonSQLs的定义
				this._Value = tb.getAttsTable().get("FIELDVALUE").toString();
			}
			if (this._Text == null || this._Text.trim().length() == 0) {
				// DisplayField 未定义，取CommonSQLs的定义
				this._Text = tb.getAttsTable().get("FIELDDISPLAY").toString();
			}
		}
		this._Table = tb;
	}

	private String getLfShowVal() {
		try {
			String val = super.getValue();
			DTRow row = this._Table.getRowByKey(this._Value, val);
			return row == null ? "" : row.getCell(this._Text).toString();
		} catch (Exception e) {
			return e.getMessage();
		}

	}
}