package com.gdxsoft.easyweb.script.display.frame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.gdxsoft.easyweb.data.DTCell;
import com.gdxsoft.easyweb.data.DTColumn;
import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.HtmlUtils;
import com.gdxsoft.easyweb.script.display.ItemValues;
import com.gdxsoft.easyweb.script.display.items.IItem;
import com.gdxsoft.easyweb.script.html.HtmlDocument;
import com.gdxsoft.easyweb.script.template.SkinFrame;
import com.gdxsoft.easyweb.script.template.XItem;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValues;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MList;
import com.gdxsoft.easyweb.utils.msnet.MListStr;
import com.gdxsoft.easyweb.utils.msnet.MStr;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class FrameFrame extends FrameBase implements IFrame {
	private static Logger LOGGER = LoggerFactory.getLogger(FrameFrame.class);
	private boolean _IsGroup;
	private String[] _GroupInfos;
	private String _GroupShow;
	private String _MeargeMap;
	private boolean _IsRedrawJson;
	private int _ColSize;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IFrame#createHtml()
	 */
	public void createHtml() throws Exception {

		if (super.getHtmlClass().getSysParas().isVue()) {
			super.createHtmlVue();
		} else {
			this.createHtmlTraditional();
		}
	}

	public void createHtmlTraditional() throws Exception {
		try {
			super.createSkinTop();
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			throw e;
		}
		super.addDebug(this, "HTML", "createSkinTop");

		try {
			super.createCss();
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			throw e;
		}

		super.addDebug(this, "HTML", "createCss");
		super.createJsTop();
		super.addDebug(this, "HTML", "createJsTop");

		try {
			this.createContent();
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			throw e;
		}

		super.addDebug(this, "HTML", "createContent");

		// Frame脚本
		try {
			this.createJsFramePage();
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			throw e;
		}
		super.addDebug(this, "HTML", "createJsFramePage");

		try {
			super.createSkinBottom();
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			throw e;
		}

		super.addDebug(this, "HTML", "createSkinBottom");

		super.createJsBottom();
		super.addDebug(this, "HTML", "createJsBottom");
	}

	/**
	 * 生成页面的JSON数据
	 * 
	 * @return 页面的JSON数据
	 * @throws Exception
	 */
	public String createJsonContent() throws Exception {
		MStr sb = new MStr();
		RequestValue rv = super.getHtmlClass().getItemValues().getRequestValue();
		String jsonName = super.getHtmlClass().getItemValues().getRequestValue()
				.getString(FrameParameters.EWA_JSON_NAME);
		if (jsonName != null) {
			sb.append("" + Utils.textToJscript(jsonName) + "=");
		}
		int len = super.getHtmlClass().getItemValues().getDTTables().size();
		if (len == 0) {
			sb.a("[]");
			return sb.toString();
		}
		MList tbs = super.getHtmlClass().getItemValues().getDTTables();
		if (len == 1) {
			DTTable dt = (DTTable) tbs.get(0);
			String s1 = dt.toJson(rv);
			sb.a(s1);
			return sb.toString();
		}
		// 合并表
		DTTable mainTb = null;
		for (int i = 0; i < len; i++) {
			DTTable dt1 = (DTTable) tbs.get(i);
			if (dt1.getCount() == 0) {
				continue;
			}
			if (mainTb == null) {
				mainTb = dt1;
				continue;

			}
			for (int m = 0; m < dt1.getColumns().getCount(); m++) {
				DTColumn col = dt1.getColumns().getColumn(m);
				String name = dt1.getColumns().getColumn(m).getName();
				if (mainTb.getColumns().testName(name)) {
					continue;
				}
				DTCell v = dt1.getRow(0).getCell(m);
				mainTb.getColumns().addColumn(col);
				DTRow row = mainTb.getRow(0);
				row.addData(v);
			}
		}
		if (mainTb != null) {
			String s1 = mainTb.toJson(rv);
			sb.a(s1);
			return sb.toString();
		} else {
			sb.a("[]");
			return sb.toString();
		}
	}

	/**
	 * 获取用户自定义的html
	 * 
	 * @return 用户自定义的html
	 */
	public String getUserHtml() {
		UserConfig uc = this.getHtmlClass().getUserConfig();
		// 索引号错误（<0 或超出范围）index=0,size=0
		// 当 从 ListFrame 修改成 Frame 时候
		String userHtml;
		try {
			userHtml = uc.getUserPageItem().testName("FrameHtml")
					? (uc.getUserPageItem().getItem("FrameHtml").count() > 0
							? uc.getUserPageItem().getSingleValue("FrameHtml")
							: "")
					: "";
		} catch (Exception e) {
			userHtml = "";
		}
		return userHtml;
	}

	public void createContent() throws Exception {
		RequestValue rv = super.getHtmlClass().getItemValues().getRequestValue();

		HtmlDocument doc = this.getHtmlClass().getDocument();

		// 索引号错误（<0 或超出范围）index=0,size=0
		// 当 从 ListFrame 修改成 Frame 时候
		String userHtml = this.getUserHtml();

		String ewa_redraw = rv.s(FrameParameters.EWA_REDRAW);
		if (ewa_redraw != null) {
			// 如果ReDraw模式，则UserHtml(用户自定义模式)无效
			userHtml = "";
		}
		// 用户自定义头部html
		String pageAddTop = this.getPageItemValue("AddHtml", "Top");
		if (pageAddTop != null) {
			doc.addScriptHtml(pageAddTop);
			doc.addFrameHtml(pageAddTop);
		}

		if (userHtml.trim().length() == 0) {
			// 皮肤定义的头部
			MStr sb = new MStr();
			sb.append("<!--皮肤定义的头部-->");

			String skinTop = super.createSkinFCTop();
			String mtypeCss;
			if ("M".equalsIgnoreCase(rv.s(FrameParameters.EWA_MTYPE))) {
				mtypeCss = " ewa-mtype-m"; // 修改模式
			} else if ("N".equalsIgnoreCase(rv.s(FrameParameters.EWA_MTYPE))) {
				mtypeCss = " ewa-mtype-n"; // 新建模式
			} else if ("c".equalsIgnoreCase(rv.s(FrameParameters.EWA_MTYPE))) {
				mtypeCss = " ewa-mtype-c"; // 拷贝模式
			} else {
				mtypeCss = "";
			}
			skinTop = skinTop.replace("{EWA_MTYPE}", mtypeCss);

			sb.append(skinTop);

			// sb.replace("<table ", "<table id='EWA_FRAME_" +
			// super.getHtmlClass().getSysParas().getFrameUnid() + "' ");

			// Frame定义的页头
			sb.append("<!--Frame定义的页头-->");
			sb.append(createFrameHeader());

			doc.addScriptHtml(sb.toString());
			doc.addFrameHtml(sb.toString());
		}

		// Frame内容
		createFrameContent();

		if (userHtml.trim().length() == 0) {
			// Frame定义的页脚
			this.createFrameFooter();

			// 皮肤定义定义的尾部
			String bottom = super.createSkinFCBottom();
			doc.addScriptHtml(bottom);
			doc.addFrameHtml(bottom);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IFrame#createFrameHeader()
	 */
	public String createFrameHeader() throws Exception {
		MStr sb = new MStr();
		// 判断参数EWA_HIDDEN_CAPTION或Size.HiddenCaption
		if (super.isHiddenCaption()) {// 不显示列头
			return "";
		}
		/*
		 * if (!super.isShowHeader()) { // 不显示列头 return sb.toString(); }
		 */
		// header信息
		String header = super.createSkinFCHeader();

		sb.append("<TR EWA_TAG='HEADER'>");
		String pageDescription = Utils.textToInputValue(super.getHtmlClass().getSysParas().getTitle());
		sb.append(header.replace(SkinFrame.TAG_ITEM, pageDescription));
		sb.append("</tr>");
		int colSpan = this.getFrameColSize();
		return sb.toString().replace("3", colSpan + "");
	}

	/**
	 * 创建用户自定义html
	 * 
	 * @return
	 * @throws Exception
	 */
	private String createFrameContentUserHtml() throws Exception {
		MStr sb = new MStr();
		UserConfig uc = this.getHtmlClass().getUserConfig();
		String lang = this.getHtmlClass().getSysParas().getLang();

		String userHtml = this.getUserHtml();
		sb.append(userHtml);
		Map<String, String> cellData = new HashMap<String, String>();
		for (int i = 0; i < uc.getUserXItems().count(); i++) {
			UserXItem uxi = uc.getUserXItems().getItem(i);

			String des = HtmlUtils.getDescription(uxi.getItem("DescriptionSet"), "Info", lang);// 描述
			String memo = HtmlUtils.getDescription(uxi.getItem("DescriptionSet"), "Memo", lang);// memo
			IItem item = super.getHtmlClass().getItem(uxi);

			String itemHtml = item.createItemHtml();

			cellData.put(uxi.getName().toUpperCase(), itemHtml);

			itemHtml = itemHtml.replace(SkinFrame.TAG_DES, des);
			itemHtml = itemHtml.replace(SkinFrame.TAG_MSG, memo);

			// 老方法
			sb.replace("{" + uxi.getName() + "#ITEM}", itemHtml);
			sb.replace("{" + uxi.getName() + "#DES}", des);
			sb.replace("{" + uxi.getName() + "#MEMO}", memo);
		}

		ItemValues ivs = super.getHtmlClass().getItemValues();
		MListStr al = Utils.getParameters(userHtml, "@");
		// 新方法
		for (int i = 0; i < al.size(); i++) {
			String paraName = al.get(i);
			String name = paraName.toUpperCase();
			String paraValue;
			if (cellData.containsKey(name)) {
				paraValue = cellData.get(name);
			} else {
				paraValue = ivs.getValue(paraName, paraName);
				if (paraValue != null && paraValue.indexOf("@") >= 0) {
					paraValue = paraValue.replace("@", IItem.REP_AT_STR);
				}
				// 对于非格式化的数据，强制替换html符号
				paraValue = Utils.textToInputValue(paraValue);
			}
			if (paraValue == null) {
				paraValue = "";
			}

			sb.replace("@" + paraName, paraValue);
		}

		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IFrame#createFrameContent()
	 */
	public void createFrameContent() throws Exception {
		RequestValue rv = super.getHtmlClass().getItemValues().getRequestValue();

		MStr sb = new MStr();
		UserConfig uc = this.getHtmlClass().getUserConfig();
		String ewa_redraw = super.getHtmlClass().getItemValues().getRequestValue()
				.getString(FrameParameters.EWA_REDRAW);
		// 重绘
		if (ewa_redraw != null && ewa_redraw.equals("1") && uc.getUserPageItem().testName("RedrawJson")) {
			String RedrawJson = uc.getUserPageItem().getSingleValue("RedrawJson");
			if (RedrawJson.trim().length() > 0) {
				_IsRedrawJson = true;
				try {
					JSONObject obj = new JSONObject(RedrawJson);
					String html = createRedrawJsonItemHtmls(obj);
					sb.al(html);
					this.getHtmlClass().getDocument().addScriptHtml(sb.toString(), "FRAME CONTENT");
					return;
				} catch (Exception err) {
					String sss = "ERROR:" + err.getMessage() + "<br>" + RedrawJson;
					this.getHtmlClass().getDocument().addScriptHtml(sss, "FRAME CONTENT");
					return;
				}
			}
		}
		// 索引号错误（<0 或超出范围）index=0,size=0
		// 当 从 ListFrame 修改成 Frame 时候
		String userHtml = this.getUserHtml();

		if (userHtml.trim().length() > 0 && !_IsRedrawJson && rv.s(FrameParameters.EWA_TEMP_NO) == null) {// 用户自定义框架
			sb.append(this.createFrameContentUserHtml());
		} else {
			sb.append(this.createItemHtmls());
		}

		this.getHtmlClass().getDocument().addScriptHtml(sb.toString(), "FRAME CONTENT");
		this.getHtmlClass().getDocument().addFrameHtml(sb.toString());
	}

	private void initGroupInfo() {
		UserConfig uc = this.getHtmlClass().getUserConfig();
		String lang = this.getHtmlClass().getSysParas().getLang();
		if (!uc.getUserPageItem().testName("GroupSet")) {
			this._IsGroup = false;
		}
		try {
			UserXItemValues u = uc.getUserPageItem().getItem("GroupSet");
			for (int i = 0; i < u.count(); i++) {
				UserXItemValue u0 = u.getItem(i);
				String lang1 = u0.getItem("Lang");
				String groupInfo = u0.getItem("GroupInfo");
				if (lang1.equalsIgnoreCase(lang) && groupInfo.trim().length() > 0) { // find
																						// groupinfo
					String[] arrayGroup = groupInfo.split(",");
					this._IsGroup = true;
					this._GroupInfos = arrayGroup;
					this._GroupShow = u0.getItem("GroupShow");
					if (this._GroupShow == null)
						this._GroupShow = "LST";
					this._GroupShow = this._GroupShow.trim().toUpperCase();
					return;
				}
			}
			this._IsGroup = false;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			this._IsGroup = false;
		}
	}

	private String createRedrawJsonItemHtmls(JSONObject redrawJson) throws Exception {
		MStr sb = new MStr();

		UserConfig uc = this.getHtmlClass().getUserConfig();
		String lang = this.getHtmlClass().getSysParas().getLang();

		HashMap<String, ArrayList<String>> itemMap = new LinkedHashMap<String, ArrayList<String>>();
		for (int i = 0; i < uc.getUserXItems().count(); i++) {
			UserXItem uxi = uc.getUserXItems().getItem(i);
			XItem xItem = HtmlUtils.getXItem(uxi);
			super.addDebug(this, "Item", "Create item " + uxi.getName() + "[" + xItem.getName() + "]");

			String des = "", memo = "";
			try {
				des = HtmlUtils.getDescription(uxi.getItem("DescriptionSet"), "Info", lang);// 描述
				memo = HtmlUtils.getDescription(uxi.getItem("DescriptionSet"), "Memo", lang);// memo
			} catch (Exception err) {

			}

			IItem item = super.getHtmlClass().getItem(uxi);
			String itemHtml = item.createItemHtml();
			// 类型
			String tag = uxi.getSingleValue("Tag");

			if (itemHtml.indexOf(SkinFrame.TAG_DES) >= 0) {
				itemHtml = itemHtml.replace(SkinFrame.TAG_DES, des);
			}
			if (itemHtml.indexOf(SkinFrame.TAG_MSG) >= 0) {
				itemHtml = itemHtml.replace(SkinFrame.TAG_MSG, memo);
			}

			if (tag.equalsIgnoreCase("textarea")) {
				itemHtml = itemHtml.replaceFirst(">", " placeholder=\"" + Matcher.quoteReplacement(des) + "\">");
			}
			// 根据逻辑表达式去除属性
			itemHtml = this.removeAttrsByLogic(uxi, itemHtml);

			ArrayList<String> al = new ArrayList<String>();
			al.add(itemHtml);
			al.add(des);
			al.add(memo);
			// 是否必须输入 1=必须
			String IsMustInput = uxi.getSingleValue("IsMustInput");
			al.add(IsMustInput);

			al.add(tag);
			itemMap.put(uxi.getName(), al);
		}
		int rows = redrawJson.getInt("rows");
		int cols = redrawJson.getInt("cols");
		sb.append("<tr class='ewa-row-msg-box'><td class='ewa_msg_box' colspan='" + cols * 2
				+ "'><div ><span class='ewa_msg_tip'>Tip</span><span class='ewa_msg_err'></span></div></td></tr>");
		for (int i = 0; i < rows; i++) {
			sb.al("<tr class='ewa-row-" + i + "'>");
			for (int m = 0; m < cols; m++) {
				String tmp = i + "`" + m;
				sb.append("<td  class='ewa_redraw_info' [DSP" + tmp + "0] col='" + m + "' row='" + i
						+ "'>[!@#!@#--342‘‘’’" + tmp + "0]</td>");
				sb.append("<td SHOW_MSG='1' class='ewa_redraw_ctl' [DSP" + tmp + "1] col='" + (m) + "' row='" + i
						+ "'>[!@#!@#--342、、" + tmp + "1]</td>");

			}
			sb.al("</tr>");
		}
		JSONArray map = redrawJson.getJSONArray("map");
		for (int i = 0; i < map.length(); i++) {
			JSONObject o = map.getJSONObject(i);
			String id = o.getString("id");
			String col = o.getString("col");
			String row = o.getString("row");

			String html = id;
			String des = "找不到";
			String tag = "";
			if (itemMap.containsKey(id)) {
				html = itemMap.get(id).get(0);
				des = "<div class='ewa_d0'><div class='ewa_d1'>" + itemMap.get(id).get(1) + "</div>";
				// 是否必须输入
				String IsMustInput = itemMap.get(id).get(3);
				if (IsMustInput.equals("1")) {
					des = des + " <span class='ewa_must'>*</span>";
				}
				des += "</div>";

				tag = itemMap.get(id).get(4);
				itemMap.remove(id);
			}
			String tmp = row + "`" + col;

			if (!isRedraw2Col(tag)) { // 检测是否为跨2列对象
				sb.replace("[!@#!@#--342‘‘’’" + tmp + "0]", des);
				sb.replace(" [DSP" + tmp + "0] col=", " col=");

				sb.replace("[!@#!@#--342、、" + tmp + "1]", html);
				sb.replace(" [DSP" + tmp + "1] col=", " col=");
			} else {
				// 按钮放到第一各单元格，页面脚本合并单元格时好处理
				sb.replace(" [DSP" + tmp + "0] col=", " col=");
				sb.replace("[!@#!@#--342‘‘’’" + tmp + "0]", html);
			}

		}

		// 清除无用的标记，并隐含单元格
		for (int i = 0; i < rows; i++) {
			for (int m = 0; m < cols; m++) {
				String tmp = i + "`" + m;
				sb.replace(" [DSP" + tmp + "0] col=", " style='display:none' col=");
				sb.replace(" [DSP" + tmp + "1] col=", " style='display:none' col=");
				sb.replace("[!@#!@#--342、、" + tmp + "1]", "");
				sb.replace("[!@#!@#--342‘‘’’" + tmp + "0]", "");
			}
		}
		sb.al("<tr des='un maped' style='display:none'><td>");
		MStr sbButtons = new MStr();

		for (String key : itemMap.keySet()) {
			String html = itemMap.get(key).get(0);
			String tag = itemMap.get(key).get(4);
			if (tag.equalsIgnoreCase("button") || tag.equalsIgnoreCase("submit")) {
				sbButtons.al(html);
			} else {
				sb.al(html);
			}
		}
		sb.al("</td></tr>");
		if (sbButtons.length() > 0) { // 按钮
			sb.al("<tr des='buttons'><td align='right' colspan='" + cols * 2 + "' class='EWA_TD_B'>");
			sb.al(sbButtons);
			sb.al("</td></tr>");
		}
		String str = sb.toString().replace("·~！@", "");
		if (sb.indexOf("@") > 0) { // 替换未替换的值
			return super.getHtmlClass().getItemValues().replaceParameters(str, false);
		} else {
			return str;
		}
	}

	/**
	 * 检测是否为跨2列对象
	 * 
	 * @param tag
	 * @return
	 */
	private boolean isRedraw2Col(String tag) {
		return tag.equalsIgnoreCase("button") || tag.equalsIgnoreCase("submit") || tag.toUpperCase().startsWith("DHTML")
				|| tag.equalsIgnoreCase("h5upload") || tag.equalsIgnoreCase("textarea") || tag.equals("markDown")
				|| tag.equalsIgnoreCase("xmleditor") || tag.equalsIgnoreCase("jseditor")
				|| tag.equalsIgnoreCase("sqleditor") || tag.equalsIgnoreCase("user")
				|| tag.equalsIgnoreCase("ewaconfigitem") || tag.equalsIgnoreCase("signature");
	}

	/**
	 * 
	 * @param uxi
	 * @param doc
	 * @param mapGroup
	 * @param mapGroupId
	 * @param lang
	 * @param colSpan
	 * @param isC11
	 * @param meargeMap
	 * @throws Exception
	 */
	private void createItemHtml(UserXItem uxi, HtmlDocument doc, MTable mapGroup, MTable mapGroupId, String lang,
			int colSpan, boolean isC11, HashMap<String, ArrayList<String>> meargeMap) throws Exception {
		if (super.isHiddenField(uxi.getName())) {
			return; // 隐含字段
		}
		XItem xItem = HtmlUtils.getXItem(uxi);
		String tag = xItem.getName();

		super.addDebug(this, "Item", "Create item " + uxi.getName() + "[" + tag + "]");

		if (tag.equalsIgnoreCase("button") || tag.equalsIgnoreCase("submit") || tag.equalsIgnoreCase("hidden") // 隐含字段
				|| tag.equalsIgnoreCase("idempotence") // 幂等性，用于Frame
		) {
			return; // 在CreateFooter中
		}

		// Frame单独一行
		boolean oneCell = false;
		// 合并对象
		if (uxi.testName("DataItem")) {
			String mt = uxi.getSingleValue("DataItem", "MeargeTo");
			if (mt != null && mt.trim().length() > 0) {
				if (!meargeMap.containsKey(mt)) {
					meargeMap.put(mt, new ArrayList<String>());
				}
				meargeMap.get(mt).add(uxi.getName());

			}

			// Frame单独一行
			String paraOneCell = uxi.getSingleValue("DataItem", "FrameOneCell");
			if ("yes".equalsIgnoreCase(paraOneCell)) {
				oneCell = true;
			}
		}
		if (uxi.testName("List")) {
			UserXItemValues di = uxi.getItem("List");
			super.addDebug(this, "Item", di.getXml().replace("><", ">\n<"));
		}

		String parentHtml;
		// 每行的ID编号，用户页面脚本操作
		String trId = "_ewa_tr$" + uxi.getName();
		String trClass = "ewa-row-" + uxi.getName();
		int curIndex = 0;
		if (this._IsGroup && uxi.testName("GroupIndex")) {
			String grpIdx = uxi.getSingleValue("GroupIndex");
			if (grpIdx.trim().length() > 0) {
				try {
					curIndex = Integer.parseInt(grpIdx);
					curIndex = Math.abs(curIndex);
				} catch (Exception e) {
					// System.err.println(e.getMessage());
				}
			}
		}
		String des = "", memo = "";
		try {
			des = HtmlUtils.getDescription(uxi.getItem("DescriptionSet"), "Info", lang);// 描述
			memo = HtmlUtils.getDescription(uxi.getItem("DescriptionSet"), "Memo", lang);// memo
		} catch (Exception err) {

		}
		boolean isShow = true;
		if (tag.equalsIgnoreCase("hidden") || curIndex > 0) {
			isShow = false;
		}
		String disp = " style='·~！@display:" + (isShow ? "" : "none") + "' groupIndex='" + curIndex + "' ";
		// System.out.println(xItem.getName());
		if (xItem.getName().equalsIgnoreCase("h5upload")) {
			MStr s1 = new MStr();

			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("<tr SHOW_MSG='1' class='");
			stringBuilder.append(trClass);
			stringBuilder.append("' id='");
			stringBuilder.append(trId);
			stringBuilder.append("'");
			stringBuilder.append(disp);
			stringBuilder.append("><td ewa_des=\"");
			stringBuilder.append(Utils.textToInputValue(des));
			stringBuilder.append("\" ewa_memo=\"");
			stringBuilder.append(Utils.textToInputValue(memo));
			stringBuilder.append("\" colspan='");
			stringBuilder.append(colSpan);
			stringBuilder.append("'>");

			stringBuilder.append(SkinFrame.TAG_ITEM);
			stringBuilder.append("</td></tr>");

			s1.append(stringBuilder.toString());
			parentHtml = s1.toString();
		} else if (oneCell) {// Frame单独一行，参数：DataItem.FrameOneCell 2020-01-08
			MStr s1 = new MStr();

			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("\n<tr SHOW_MSG='1' class='ewa-frame-one-cell ");
			stringBuilder.append(trClass);
			stringBuilder.append("' id='");
			stringBuilder.append(trId);
			stringBuilder.append("'");
			stringBuilder.append(disp);
			stringBuilder.append("><td class='EWA_TD_1' ewa_des=\"");
			stringBuilder.append(Utils.textToInputValue(des));
			stringBuilder.append("\" ewa_memo=\"");
			stringBuilder.append(Utils.textToInputValue(memo));
			stringBuilder.append("\" colspan='");
			stringBuilder.append(colSpan);
			stringBuilder.append("'>");
			stringBuilder.append(SkinFrame.TAG_ITEM);
			stringBuilder.append("</td></tr>");

			s1.append(stringBuilder.toString());
			parentHtml = s1.toString();
		} else if (this.isRedraw2Col(tag) && !isC11) {
			MStr s1 = new MStr();

			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("\r\n<tr SHOW_MSG='1' class='ewa-tr-summary ");
			stringBuilder.append(trClass);
			stringBuilder.append("' id='");
			stringBuilder.append(trId);
			stringBuilder.append("'");
			stringBuilder.append(disp);
			stringBuilder.append(">\r\n<td class='EWA_TD' colspan='");
			stringBuilder.append(colSpan);
			stringBuilder.append("'> &bull; ");

			s1.append(stringBuilder.toString());
			s1.append(des);
			if (memo != null && memo.trim().length() > 0 && !memo.trim().equals(des.trim())) {
				s1.append("(");
				s1.append(memo);
				s1.a(")");
			}
			s1.append("</td>\r\n");

			StringBuilder stringBuilder2 = new StringBuilder();
			stringBuilder2.append("</tr>\r\n<tr SHOW_MSG=1  class='");
			stringBuilder2.append(trClass);
			stringBuilder2.append("' ");
			stringBuilder2.append(disp);
			stringBuilder2.append("><td colspan='");
			stringBuilder2.append(colSpan);
			stringBuilder2.append("' ewa_des=\"" + Utils.textToInputValue(des) + "\" ewa_memo=\""
					+ Utils.textToInputValue(memo) + "\" class='EWA_TD_1' !! >");
			stringBuilder2.append(SkinFrame.TAG_ITEM);
			stringBuilder2.append("</td></tr>");

			s1.append(stringBuilder2.toString());
			parentHtml = s1.toString();
		} else {
			String[] tmps = this.getParentHtml(uxi);
			StringBuilder sbTmp = new StringBuilder();
			sbTmp.append("<tr SHOW_MSG='1' class='");
			sbTmp.append(trClass);
			sbTmp.append(isC11 ? " ewa-row-des" : "");
			sbTmp.append("' id='");
			sbTmp.append(trId);
			sbTmp.append("' ");
			sbTmp.append(disp);
			sbTmp.append(">\r\n");
			sbTmp.append(this.createRowCols(tmps, colSpan, isC11, trClass, disp));
			sbTmp.append("</tr>\r\n");

			parentHtml = sbTmp.toString(); // 皮肤定义的页面样式;

			// parentHtml = "<tr SHOW_MSG='1' class='" + trClass + "' id='" + trId + "' " +
			// disp + ">\r\n"
			// + super.createItemParentHtml(uxi) + "</tr>\r\n"; // 皮肤定义的页面样式

		}
		// 元素父窗体样式
		String parentStyle = "";
		if (uxi.testName("ParentStyle")) {
			parentStyle = uxi.getSingleValue("ParentStyle");
		}
		if (parentStyle.length() > 0) {
			parentHtml = parentHtml.replaceFirst("!!", " style=\"" + Matcher.quoteReplacement(parentStyle) + "\" ");
		} else {
			parentHtml = parentHtml.replaceFirst("!!", "");
		}

		IItem item = super.getHtmlClass().getItem(uxi);
		String itemHtml = item.createItemHtml();

		doc.getItems().put(uxi.getName(), itemHtml);

		if (itemHtml.indexOf(SkinFrame.TAG_DES) >= 0) {
			itemHtml = itemHtml.replace(SkinFrame.TAG_DES, des);
		}
		if (itemHtml.indexOf(SkinFrame.TAG_MSG) >= 0) {
			itemHtml = itemHtml.replace(SkinFrame.TAG_MSG, memo);
		}
		// 根据逻辑表达式去除属性
		itemHtml = this.removeAttrsByLogic(uxi, itemHtml);

		String s2 = parentHtml.replace(SkinFrame.TAG_ITEM, itemHtml);

		if (!mapGroup.containsKey(curIndex)) {
			mapGroup.put(curIndex, new MStr());
			mapGroupId.put(curIndex, new MStr());
		}
		MStr sb0 = (MStr) mapGroup.get(curIndex);
		sb0.append(s2);
		MStr sbId = (MStr) mapGroupId.get(curIndex);
		if (sbId.length() > 0) {
			sbId.append(",");
		}
		sbId.append(trId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IFrame#createItemHtmls()
	 */
	public String createItemHtmls() throws Exception {
		MStr sb = new MStr();
		this.initGroupInfo();
		MTable mapGroup = new MTable(); // integer, MyStr
		MTable mapGroupId = new MTable();

		mapGroup.put(0, new MStr());
		mapGroupId.put(0, new MStr());

		UserConfig uc = this.getHtmlClass().getUserConfig();
		String lang = this.getHtmlClass().getSysParas().getLang();

		// 显示为3段还是2段
		int colSpan = this.getFrameColSize();
		// 一段的情况是否上下排列
		boolean isC11 = this.isC11();

		HtmlDocument doc = super.getHtmlClass().getDocument();

		HashMap<String, ArrayList<String>> meargeMap = new HashMap<String, ArrayList<String>>();
		for (int i = 0; i < uc.getUserXItems().count(); i++) {
			UserXItem uxi = uc.getUserXItems().getItem(i);
			this.createItemHtml(uxi, doc, mapGroup, mapGroupId, lang, colSpan, isC11, meargeMap);
		}
		if (!this._IsGroup) {
			sb = (MStr) mapGroup.get(0);
		} else {
			String groupHtml = this.createGroup(mapGroup, mapGroupId);
			sb.append(groupHtml);
		}

		// 生成合并对象的脚本
		MStr mjs = new MStr();
		for (String key : meargeMap.keySet()) {
			MStr mjs1 = new MStr();
			ArrayList<String> al = meargeMap.get(key);
			for (int k = 0; k < al.size(); k++) {
				if (k > 0) {
					mjs1.a(",");
				}
				mjs1.a("\"" + al.get(k) + "\"");

			}
			if (mjs.length() > 0) {
				mjs.a(", ");
			}
			mjs.al("\"" + key + "\":[" + mjs1.toString() + "]");
		}
		_MeargeMap = mjs.toString();

		String str = sb.toString().replace("·~！@", "");
		if (sb.indexOf("@") > 0) { // 替换未替换的值
			return super.getHtmlClass().getItemValues().replaceParameters(str, false);
		} else {
			return str;
		}
	}

	/**
	 * 生成Item的容器html
	 * 
	 * @param uxi
	 * @return 容器html
	 * @throws Exception
	 */
	public String[] getParentHtml(UserXItem uxi) throws Exception {
		String lang = super.getHtmlClass().getSysParas().getLang();

		String des = "", memo = "";
		try {
			des = HtmlUtils.getDescription(uxi.getItem("DescriptionSet"), "Info", lang);// 描述
			memo = HtmlUtils.getDescription(uxi.getItem("DescriptionSet"), "Memo", lang);// memo
		} catch (Exception err) {

		}

		/*
		 * <td class="EWA_TD_L">{__EWA_DES__}</td> <td
		 * class="EWA_TD_M">{__EWA_ITEM__}</td> <td class="EWA_TD_R">{__EWA_MSG__}</td>
		 */

		String[] tmps = new String[5];
		tmps[0] = "\t<td class=\"EWA_TD_L\">" + des + "</td>\n";
		tmps[1] = "\t<td class=\"EWA_TD_M\">{__EWA_ITEM__}</td>\n";
		tmps[2] = "\t<td class=\"EWA_TD_R\">" + memo + "</td>\n";
		tmps[3] = des;
		tmps[4] = memo;
		return tmps;
	}

	private String createRowCols(String[] tmps, int colSpan, boolean isC11, String trClass, String disp) {
		String des = Utils.textToInputValue(tmps[3]);
		String memo = Utils.textToInputValue(tmps[4]);
		StringBuilder stringBuilder = new StringBuilder(" ewa_des=\"");
		stringBuilder.append(des);
		stringBuilder.append("\" ewa_memo=\"");
		stringBuilder.append(memo);
		stringBuilder.append("\" ");
		// EWA_TD_M
		String template = tmps[1].replace(">{", stringBuilder.toString() + " >{");

		StringBuilder sb = new StringBuilder();
		if (isC11) { // 一段,上下排列
			sb.append(tmps[0]); //<td class="EWA_TD_L">您的邮箱</td>
			
			/*String info = tmps[3];
			if (tmps[4] != null && tmps[4].length() > 0 && !info.equals(tmps[4])) {
				info += ", " + tmps[4];
			}
			sb.append("<td class=\"EWA_TD_L\">").append(info).append("</td>");
			*/
			sb.append("</tr>\n<tr class='" + trClass + " ewa-row-item' " + disp + ">\n");
			sb.append(template);
		} else if (colSpan == 2) {
			sb.append(tmps[0]);
			sb.append(template);
		} else if (colSpan == 1) {

			sb.append(template);
		} else {
			sb.append(tmps[0]);
			sb.append(template);
			sb.append(tmps[2]);
		}
		return sb.toString();
	}

	/**
	 * 页面分组显示处理过程
	 * 
	 * @param mapGroup   页面分组
	 * @param mapGroupId 页面分组ID
	 * @return 页面分组
	 */
	private String createGroup(MTable mapGroup, MTable mapGroupId) {

		MStr sb = new MStr();
		MStr sb1 = new MStr();
		int colSize = this.getFrameColSize();
		// sb.append("<tr style='display:none'><td colspan='3'></td></tr>\r\n");
		for (int i = 0; i < this._GroupInfos.length; i++) {
			String ids = "";
			String html = "";
			if (mapGroup.containsKey(i)) {
				html = mapGroup.get(i).toString();
				ids = mapGroupId.get(i).toString();
				mapGroup.removeKey(i);
				mapGroupId.removeKey(i);
			}
			if (this._GroupShow.equals("GUIDE")) {// 向导模式
				JSONObject json = new JSONObject();
				String unid = super.getHtmlClass().getSysParas().getFrameUnid();
				try {
					json.put("DES", this._GroupInfos[i]);
					json.put("IDS", ids);

					sb.al("<script type='text/javascript'><!--");
					sb.al("if(window.ewa_groups_" + unid + "==null){ewa_groups_" + unid + "=[];}ewa_groups_" + unid
							+ "[" + i + "]=" + json.toString() + ";");
					sb.al("--></script>");
				} catch (Exception err) {
					sb.al(err.getMessage());
				}

			} else if (this._GroupShow.equals("") || this._GroupShow.equals("LST")) {
				// 水平分割模式
				StringBuilder stmp = new StringBuilder();
				stmp.append("<tr class='ewa-tr-grp ewa-tr-grp-");
				stmp.append(i);
				stmp.append("'><td colspan='");
				stmp.append(colSize);
				stmp.append("' class='EWA_GROUP'");
				stmp.append(" ewa_group_ids='");
				stmp.append(ids);
				stmp.append("'>");
				stmp.append(this._GroupInfos[i]);
				stmp.append("</td></tr>\r\n");
				sb.append(stmp);
			} else {
				// 分组模式
				StringBuilder stmp = new StringBuilder();
				stmp.append("<div onclick='EWA.F.FOS[\"");
				stmp.append(super.getHtmlClass().getSysParas().getFrameUnid());
				stmp.append("\"].GroupShow(this,");
				stmp.append(i);
				stmp.append(")' class='EWA_GROUP_CNT_");
				stmp.append(this._GroupShow);
				stmp.append((i == 0 ? "1" : ""));
				stmp.append("' ewa_group_ids='");
				stmp.append(ids);
				stmp.append("'>");
				stmp.append(this._GroupInfos[i]);
				stmp.append("</div>");
				sb1.append(stmp);
			}
			sb.append(html);
		}
		if (mapGroup.size() > 0) {
			MStr idsSb = new MStr();
			MStr htmlSb = new MStr();
			for (int i = 0; i < mapGroup.size(); i++) {
				Integer key = (Integer) mapGroup.getKey(i);
				htmlSb.append(mapGroup.get(key));
				MStr sbId = (MStr) mapGroupId.get(key);
				if (sbId.length() > 0) {
					if (idsSb.length() > 0) {
						idsSb.append(",");
					}
					idsSb.append(sbId);
				}
			}
			if (this._GroupShow.equals("") || this._GroupShow.equals("LST")) {
				sb.append("<tr><td colspan='" + colSize + "' class='EWA_GROUP'" + " ewa_group_ids='" + idsSb
						+ "'>错误GroupIndex定义</td></tr>\r\n");
			} else {
				sb1.append("<div ewa_group_ids='" + idsSb + "'>错误GroupIndex定义</div>");
			}
			sb.append(htmlSb.toString());
		}
		if (this._GroupShow.equals("TOP")) {
			sb.insert(0, "<tr><td colspan='" + colSize + "' class='EWA_GROUP_TOP EWA_GROUP_ITEM"
					+ this._GroupInfos.length + "'>" + sb1.toString() + "</td></tr>\r\n");
		} else if (this._GroupShow.equals("BOTTOM")) {
			sb.append("<tr><td colspan='" + colSize + "' class='EWA_GROUP_BOTTOM EWA_GROUP_ITEM"
					+ this._GroupInfos.length + "'>" + sb1.toString() + "</td></tr>\r\n");
		} else if (this._GroupShow.equals("GUIDE")) {
		}
		if (this._GroupShow.equals("LST")) { // 列表显示，没有隐含项目
			return sb.toString().replace("·~！@display:none", "");
		} else {
			return sb.toString().replace("·~！@", "");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IFrame#createFrameFooter()
	 */
	public void createFrameFooter() throws Exception {
		HtmlDocument doc = this.getHtmlClass().getDocument();
		// 索引号错误（<0 或超出范围）index=0,size=0
		// 当 从 ListFrame 修改成 Frame 时候
		String userHtml = this.getUserHtml();
		if (userHtml.trim().length() > 0 || _IsRedrawJson) {
			return;
		}

		String buttons = this.getButtonsHtml();
		if (buttons.length() > 0) {
			MStr sbButtons = new MStr(); //
			sbButtons.append("\r\n<tr class='ewa-frame-buttons'>\r\n" + super.createSkinFCItemButton() + "</tr>\r\n");// 皮肤定义的button样式
			int index = sbButtons.indexOf(SkinFrame.TAG_ITEM);
			sbButtons.insert(index, buttons);

			String sFrameHtml = sbButtons.toString().replace(SkinFrame.TAG_ITEM, "");
			// 显示为3段还是2段
			int colSpan = this.getFrameColSize();
			sFrameHtml = sFrameHtml.replace(SkinFrame.TAG_COL_SPAN, colSpan + "");
			doc.addScriptHtml(sFrameHtml, "FrameFooter");

			doc.addFrameHtml(sFrameHtml);
		}
		// 隐藏字段
		String hiddens = this.getHiddensHtml();
		if (hiddens.length() > 0) {
			doc.addScriptHtml(hiddens, "FrameFooter");
			doc.addFrameHtml(hiddens);
		}

	}

	/**
	 * 获取按钮的html
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getButtonsHtml() throws Exception {
		UserConfig uc = this.getHtmlClass().getUserConfig();
		String lang = this.getHtmlClass().getSysParas().getLang();
		MStr sbButtons = new MStr(); //
		for (int i = 0; i < uc.getUserXItems().count(); i++) {
			UserXItem uxi = uc.getUserXItems().getItem(i);
			XItem xItem = HtmlUtils.getXItem(uxi);

			if (!(xItem.getName().equalsIgnoreCase("button") || xItem.getName().equalsIgnoreCase("submit"))) {
				continue;
			}
			if (super.isHiddenField(uxi.getName())) {
				continue; // 隐含字段
			}
			IItem item = super.getHtmlClass().getItem(uxi);
			String s1 = item.createItemHtml();

			String des = HtmlUtils.getDescription(uxi.getItem("DescriptionSet"), "Info", lang);// 描述
			String memo = HtmlUtils.getDescription(uxi.getItem("DescriptionSet"), "Memo", lang);// memo

			// 根据逻辑表达式去除属性
			s1 = this.removeAttrsByLogic(uxi, s1);

			String buttonHtml = s1.replace(SkinFrame.TAG_DES, des);
			buttonHtml = buttonHtml.replace(SkinFrame.TAG_MSG, memo);
			sbButtons.a(buttonHtml);
		}

		return sbButtons.toString();
	}

	/**
	 * 获取隐含字段的 html
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getHiddensHtml() throws Exception {
		UserConfig uc = this.getHtmlClass().getUserConfig();

		MStr sbHiddens = new MStr();

		for (int i = 0; i < uc.getUserXItems().count(); i++) {
			UserXItem uxi = uc.getUserXItems().getItem(i);
			XItem xItem = HtmlUtils.getXItem(uxi);
			String tag = xItem.getName();
			if (!(tag.equalsIgnoreCase("hidden") || tag.equalsIgnoreCase("idempotence"))) {
				continue;
			}
			IItem item = super.getHtmlClass().getItem(uxi);
			String s1 = item.createItemHtml();
			sbHiddens.al(s1);

		}
		return sbHiddens.toString();
	}

	/**
	 * 显示为3段还是2段
	 * 
	 * @return 显示为3段还是2段
	 */
	private int getFrameColSize() {
		// String frameColSize =
		// super.getHtmlClass().getUserConfig().getUserPageItem().getSingleValue("Size",
		// "FrameCols");
		// int colSpan = (frameColSize != null && frameColSize.equals("C2")) ? 2
		// : 3;

		// 显示为3段还是2段

		if (this._ColSize == 0) {
			int colSpan = 3;
			// 用户指定参数 EWA_FRAME_COLS
			String EWA_FRAME_COLS = super.getHtmlClass().getItemValues().getRequestValue()
					.s(FrameParameters.EWA_FRAME_COLS);
			if (EWA_FRAME_COLS == null) {
				EWA_FRAME_COLS = super.getHtmlClass().getUserConfig().getUserPageItem().getSingleValue("Size",
						"FrameCols");

			}
			if (EWA_FRAME_COLS != null) {
				if (EWA_FRAME_COLS.equals("C2") || EWA_FRAME_COLS.equals("2")) {
					colSpan = 2;
				} else if (EWA_FRAME_COLS.equals("C1") || EWA_FRAME_COLS.equals("1") || EWA_FRAME_COLS.equals("C11")
						|| EWA_FRAME_COLS.equals("11")) {
					colSpan = 1;
				}
			}
			this._ColSize = colSpan;
		}
		return this._ColSize;
	}

	/**
	 * 显示为1段时候,是否为单行上下排列
	 * 
	 * @return 显示为1段
	 */
	public boolean isC11() {
		String EWA_FRAME_COLS = super.getHtmlClass().getItemValues().getRequestValue()
				.s(FrameParameters.EWA_FRAME_COLS);
		if (EWA_FRAME_COLS == null) {
			EWA_FRAME_COLS = super.getHtmlClass().getUserConfig().getUserPageItem().getSingleValue("Size", "FrameCols");
		}
		if (EWA_FRAME_COLS != null && (EWA_FRAME_COLS.equals("C11") || EWA_FRAME_COLS.equals("11"))) {
			return true;
		} else {
			return false;
		}
	}

	public void createJsFramePage() throws Exception {
		String gunid = super.getHtmlClass().getSysParas().getFrameUnid();
		String lang = super.getHtmlClass().getSysParas().getLang();

		// 页面URL的JS表达式
		String url = super.getUrlJs();

		MStr sJs = new MStr();

		// super.createJsFrameXml(); // item描述XML字符串

		String frameJs = super.createJsFrameXmlString();
		super.createJsFrameMenu(); // menu描述XML字符串

		// String funName = "EWA_F" + gunid + "()";

		String pageDescription = super.getPageJsTitle();

		sJs.al("(function () { ");
		sJs.al(" EWA.LANG='" + lang.toLowerCase() + "';");
		sJs.al(" var o1 = EWA.F.FOS['" + gunid + "'] = new EWA_FrameClass();");
		sJs.al(" o1._Id = '" + gunid + "';");
		sJs.al(" o1.Url = \"" + url + "\";");
		sJs.al(" o1.Title = \"" + pageDescription + "\";");
		sJs.al(" o1.Init(\"" + frameJs + "\");");
		sJs.al(" if(window.ewa_groups_" + gunid + " != null){o1.GuideShowCreate(ewa_groups_" + gunid + ");}");

		// 合并JS
		if (this._MeargeMap != null) {
			sJs.al(" o1.MeargeMap = {" + this._MeargeMap + "};");
			sJs.al(" o1.MergeItems();");
		}

		if (this._IsRedrawJson) {
			sJs.al(" o1.RedrawCreateSpans();");
		}

		// 显示为3段还是2段

		int colSpan = this.getFrameColSize();

		sJs.al(" o1.ShowPlaceHolder(" + colSpan + ");");

		// 配置文件xml的 hashCode
		sJs.al(" o1._HASH = " + this.getHtmlClass().getUserConfig().getItemNodeXml().hashCode() + ";");

		// textarea更加内容自动大小
		UserXItemValue size = super.getHtmlClass().getUserConfig().getUserPageItem().getItem("Size").getItem(0);
		if (size.testName("TextareaAuto")) {
			String textareaAuto = size.getItem("TextareaAuto");
			if ("yes".equalsIgnoreCase(textareaAuto)) {
				sJs.al(" o1.textareaAutoSize();");
			}
		}

		sJs.al(" o1 = null;");
		sJs.al("})();");
		// sJs.al(funName + ";");

		this.getHtmlClass().getDocument().addJs("FRAME_JS", sJs.toString(), false);
	}

	public String createaXmlData() throws Exception {
		Document doc = super.createXmlDataDocument();
		return UXml.asXmlAll(doc);
	}

}
