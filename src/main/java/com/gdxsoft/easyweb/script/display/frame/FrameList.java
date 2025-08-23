/**
 * 
 */
package com.gdxsoft.easyweb.script.display.frame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.cache.CachedValue;
import com.gdxsoft.easyweb.cache.CachedValueManager;
import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.datasource.PageSplit;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.Workflow.EwaWfMain;
import com.gdxsoft.easyweb.script.Workflow.WfUnit;
import com.gdxsoft.easyweb.script.Workflow.WfUnits;
import com.gdxsoft.easyweb.script.display.HtmlUtils;
import com.gdxsoft.easyweb.script.display.ItemValues;
import com.gdxsoft.easyweb.script.display.SysParameters;
import com.gdxsoft.easyweb.script.display.action.ActionListFrame;
import com.gdxsoft.easyweb.script.display.items.IItem;
import com.gdxsoft.easyweb.script.display.items.ItemEwaConfigItem;
import com.gdxsoft.easyweb.script.display.items.ItemImage;
import com.gdxsoft.easyweb.script.html.HtmlDocument;
import com.gdxsoft.easyweb.script.template.SkinFrame;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValues;
import com.gdxsoft.easyweb.utils.ULogic;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MList;
import com.gdxsoft.easyweb.utils.msnet.MListStr;
import com.gdxsoft.easyweb.utils.msnet.MStr;

/**
 * 
 */
public class FrameList extends FrameBase implements IFrame {
	private static Logger LOGGER = LoggerFactory.getLogger(FrameList.class);

	int _ListFrameRecordCount = -1;

	private UserXItem _GroupUserXItem;
	private MStr _SearchExp = new MStr();
	private boolean _IsLuButtons;
	private boolean _IsLuSearch;
	private boolean luStickyHeaders; // 是否启用表头固定（sticky headers）
	private boolean _ComposeSearchTexts; // 合并文字搜索
	// 分组搜索，就是先文字、日期，最后固定搜索（select,checkbox,radio)
	private boolean _SearchGroup = true;

	private String _LuSelect = "";
	private boolean _IsLuDblClick;
	private String _LuDblClickIdx;
	private String _LuAddPreRowFunc;

	private String _WorkFlowBut;
	private String _WorkFlowButJson;

	private ArrayList<String> _JsonUsedFields;

	private HashMap<String, String> _SubBottoms;

	private HashMap<Integer, String> _TdAddCssClass = new HashMap<Integer, String>();

	private IItem _LastItem;

	/**
	 * 获取工作流的js表达式
	 * 
	 * @return the _WorkFlowBut
	 */
	public String getWorkFlowButJson() {
		try {
			this.loadWorkFlowApp();
		} catch (Exception err) {
			// System.err.println(err.getMessage());
		}
		return _WorkFlowButJson;
	}

	private String[] _KeyFileds;

	/**
	 * 获取分页的TABLE （EXECUTE_SPLIT_SQL标记）
	 * 
	 * @return
	 */
	public DTTable getSplitPageTable() {
		MList tbs;
		try {
			tbs = super.getHtmlClass().getAction().getDTTables();
			for (int i = 0; i < tbs.size(); i++) {
				DTTable tb = (DTTable) tbs.get(i);

				if (tb.getAttsTable().containsKey(ActionListFrame.EXECUTE_SPLIT_SQL)) {
					return tb;
				}
			}
		} catch (Exception e) {
			LOGGER.warn("getSplitPageTable: {}", e.getLocalizedMessage());
		}
		return null;
	}

	/**
	 * 查询数据总数
	 * 
	 * @return
	 */
	public int queryRecords() {
		if (this._ListFrameRecordCount >= 0) {
			return this._ListFrameRecordCount;
		}
		// 在 ActionListFrame.executeSqlWithPageSplit创建
		DTTable tb = this.getSplitPageTable();
		if (tb == null) {
			this._ListFrameRecordCount = -1;
			return this._ListFrameRecordCount;
		}
		int pagesize = -1231;
		int pageCurrent = -1;
		try {
			// 每页面的记录数
			PageSplit ps = (PageSplit) tb.getAttsTable().get(ActionListFrame.PAGE_SIZE);
			pagesize = ps.getPageSize();
			pageCurrent = ps.getPageCurrent();
		} catch (Exception err) {
			LOGGER.warn(err.getLocalizedMessage());
		}
		if (tb.getCount() == 0 || (pageCurrent <= 1 && tb.getCount() < pagesize)) {
			// 返回的数量 < 分页记录数
			_ListFrameRecordCount = tb.getCount();
		} else {
			DataConnection conn = super.getHtmlClass().getItemValues().getSysParas().getDataConn();
			String sql = tb.getAttsTable().get(ActionListFrame.SPLIT_SQL).toString();
			_ListFrameRecordCount = conn.getRecordCount(sql);
			conn.close();
		}
		tb.getAttsTable().put("RECORDS", _ListFrameRecordCount);

		return _ListFrameRecordCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IFrame#createJsFramePage()
	 */
	public void createJsFramePage() throws Exception {
		// 页面脚本初始化ListFrame
		RequestValue rv = super.getHtmlClass().getSysParas().getRequestValue();
		String gunid = super.getHtmlClass().getSysParas().getFrameUnid();

		String url = super.getUrlJs();

		String userSort = rv.getString(FrameParameters.EWA_LF_ORDER);
		if (userSort == null) {
			userSort = "";
		}
		super.createJsFrameXml(); // item描述XML字符串
		super.createJsFrameMenu(); // menu描述XML字符串

		String pageDescription = super.getPageJsTitle();

		MStr sJs = new MStr();
		String lang = super.getHtmlClass().getSysParas().getLang();
		sJs.al("EWA.LANG='" + lang.toLowerCase() + "';");
		// String ewaPath = rv.s("rv_ewa_style_path");
		// if (StringUtils.isBlank(ewaPath)) {
		// ewaPath = "/EmpScriptV2"; // default static url prefix
		// }
		// sJs.al("EWA.RV_STATIC_PATH = \"" + ewaPath + "\";");
		// String funName = "EWA_F" + gunid + "()";
		sJs.al("(function() {");
		// sJs.al("function "+funName+" {");
		sJs.al(" var o1 = EWA.F.FOS['" + gunid + "'] = new EWA_ListFrameClass();");
		sJs.al(" o1._Id = o1.Id = '" + gunid + "';");
		sJs.al(" o1.Title = \"" + pageDescription + "\";");
		sJs.al(" o1.Init(EWA_ITEMS_XML_" + gunid + ");");

		PageSplit ps = this._PageSplit;
		if (ps == null) {
			ps = new PageSplit(_ListFrameRecordCount, super.getHtmlClass().getItemValues().getRequestValue(),
					this.getUserSettingPageSize());

		}
		sJs.al(" o1.SetPageParameters(" + ps.getPageCurrent() + "," + ps.getPageCount() + "," + ps.getPageSize() + ","
				+ ps.getRecordCount() + ",'" + userSort + "');");

		sJs.al(" o1.SetPageParametersName('" + PageSplit.TAG_PAGE_CURRENT + "','" + "" + "','" + PageSplit.TAG_PAGE_SIZE
				+ "','" + (_PageSplit == null ? 0 : _PageSplit.getRecordCount()) + "','EWA_LF_ORDER');");

		sJs.al(" o1.Url = \"" + url + "\";");
		sJs.al(" o1._SearchJson = {" + this._SearchExp.toString() + "};");
		sJs.al(" o1.Description = \"" + Utils.textToJscript(this.getHtmlClass().getDocument().getTitle()) + "\";");

		sJs.al(" o1 = null;");
		sJs.al("})()");
		// sJs.al(funName + ";");

		String fname = "EWA.F.FOS['" + gunid + "']";
		if (this._IsLuButtons) {
			String js = fname + ".ReShow();";
			sJs.al(js);
		}
		if (this._IsLuSearch) {
			boolean compose = this._ComposeSearchTexts; // 合并搜索
			boolean denySearchGroup = !this._SearchGroup; // 分组搜索，就是先文字、日期，最后固定搜索（select,checkbox,radio)
			String js = fname + ".ShowSearch(" + compose + ", " + denySearchGroup + ");";
			sJs.al(js);
		}

		if (this._LuSelect.length() > 0) {
			if (this._LuSelect.equals("S")) { // 单选
				String js = fname + ".SelectSingle();";
				sJs.al(js);
			} else if (this._LuSelect.equals("M")) { // 多选
				String js = fname + ".SelectMulti();";
				sJs.al(js);
			}
		}
		if (this._IsLuDblClick) {
			String js = fname + ".DblClick(" + this._LuDblClickIdx + ");";
			sJs.al(js);
		}

		if (this._LuAddPreRowFunc != null && this._LuAddPreRowFunc.length() > 0) {
			// 由于用户定义 js_bottom放到页面最后，因此用setTimeout执行，避免方法找不到
			String js = "setTimeout(function(){" + fname + ".AddPreRow("
					+ super.getHtmlClass().getItemValues().replaceParameters(this._LuAddPreRowFunc, false) + ");},12);";
			sJs.al(js);
		}
		// 工作流程信息
		if (super.getHtmlClass().getWorkflow() != null) {
			WfUnits wf = super.getHtmlClass().getWorkflow();
			MStr wfStr = new MStr();
			wfStr.al(fname + ".WorkflowCfg = [");
			for (int i = 0; i < wf.getUnits().getCount(); i++) {
				WfUnit unit = (WfUnit) wf.getUnits().getByIndex(i);
				if (i > 0) {
					wfStr.a(",");
				}
				wfStr.al("{NAME: \"" + unit.getName() + "\", DES: \"" + unit.getDes() + "\", NEXT_YES: \""
						+ unit.getWFANextYes() + "\", NEXT_NO: \"" + unit.getWFANextNo() + "\", TYPE: \""
						+ unit.getWfType() + "\"}");
			}
			wfStr.al("];");
			sJs.al(wfStr.toString());
		}
		String frameType = super.getPageItemValue("HtmlFrame", "FrameType");
		if (frameType != null && frameType.trim().length() > 0) {
			String subUrl = super.getPageItemValue("HtmlFrame", "FrameSubUrl");
			if (subUrl != null && subUrl.trim().length() > 0) {
				sJs.al(fname + ".MDownEvent=function(tr,evt){");
				sJs.al("	var keys=tr.getAttribute('EWA_KEY');");
				sJs.al("	var p=$U();");
				sJs.al("	if(p==null || p==''){p='';}else{p='&'+p;}");
				sJs.al("	var uu=new EWA_UrlClass('" + subUrl + "'+keys+p);");
				sJs.al("	var x = uu.GetParameter('xmlname');");
				sJs.al("	if(x && x.indexOf('|')>=0){");
				sJs.al("		uu.AddParameter('xmlname',x);");
				sJs.al("	}");
				sJs.al("	window.parent.frames[1].location=uu.GetUrl();");
				sJs.al("}");
			}
		}

		if (_SubBottoms != null && _SubBottoms.size() > 0) {
			MStr ss = new MStr();
			for (String s : _SubBottoms.keySet()) {
				if (ss.length() > 0) {
					ss.a(",");
				}
				ss.a(s);
			}
			sJs.al(fname + ".SubBottoms( \"" + ss.toString() + "\");");
		}

		// 固定表头
		if (this.luStickyHeaders) {
			String js = fname + ".stickyHeaders();";
			sJs.al(js);
		}
		// box json
		String boxJson = super.getPageItemValue("BoxJson", "BoxJson");
		if (boxJson != null && boxJson.trim().length() > 0) {
			// JSONObject json=new JSONObject(boxJson);
			// boolean isPid=false;
			// if(json.has("parent_id")){
			// String pid=json.getString("parent_id");
			// if(pid.indexOf("@")>=0){
			// pid=pid.replace("@", "[kkkk\123\123\19---]");
			// isPid=true;
			// }
			// }
			// boxJson=json.toString();
			boxJson = boxJson.replace("@", IItem.REP_AT_STR);

			// if(isPid){
			// boxJson=boxJson.replace("[kkkk\123\123\19---]", "@");
			// }
			// char[] chs = new char[4];
			// chs[0] = 1;
			// chs[1] = 2;
			// chs[2] = 3;
			// chs[3] = 4;
			// for (int i = 0; i < chs.length; i++) {
			// boxJson = boxJson.replace(chs[i] + "", "");
			// }
			sJs.al(fname + ".BoxJson=" + boxJson);
		}

		String LeftJson = super.getPageItemValue("LeftJson", "LeftJson");
		if (LeftJson != null && LeftJson.trim().length() > 0) {
			LeftJson = LeftJson.replace("@", IItem.REP_AT_STR);
			sJs.al(fname + ".LeftJson=" + LeftJson);
			sJs.al(fname + ".LeftJson.XMLNAME=\"" + rv.s(FrameParameters.XMLNAME) + "\";");
			sJs.al(fname + ".LeftJson.ITEMNAME=\"" + rv.s(FrameParameters.ITEMNAME) + "\";");
		}
		// show box
		if (super.getHtmlClass().getSysParas().getRequestValue().getString(FrameParameters.EWA_BOX) != null) {
			sJs.al(fname + ".BOX_CLASS=new EWA_UI_BoxClass();");
			sJs.al(fname + ".BOX_CLASS.Create(" + fname + ".BoxJson);");
		}

		// show left
		if (super.getHtmlClass().getSysParas().getRequestValue().getString(FrameParameters.EWA_LEFT) != null) {
			sJs.al(fname + ".LEFT_CLASS=new EWA_UI_LeftClass();");
			sJs.al(fname + ".LEFT_CLASS.Create(" + fname + ".LeftJson);");
		}
		try {
			String recycle = super.getPageItemValue("PageSize", "recycle");
			if (recycle != null && recycle.equals("1") && !"NO".equalsIgnoreCase(rv.s(FrameParameters.EWA_RECYCLE))) {
				sJs.al(fname + ".ShowRecycle();");
			}

		} catch (Exception err) {

		}

		this.getHtmlClass().getDocument().addJs("FRAME_JS", sJs.toString(), false);
	}

	/**
	 * 针对ListFrame生成主键表达式
	 */
	private void initKeys() {
		if (!super.getHtmlClass().getUserConfig().getUserPageItem().testName("PageSize")) {
			return;
		}
		String keyField = null;
		try {
			UserXItemValues uxvs = super.getHtmlClass().getUserConfig().getUserPageItem().getItem("PageSize");
			if (uxvs.count() == 0) {
				_KeyFileds = new String[0];
				return;
			}
			keyField = uxvs.getItem(0).getItem("KeyField");
		} catch (Exception e) {
			_KeyFileds = new String[0];
			return;
		}
		if (keyField == null || keyField.trim().length() == 0) {
			_KeyFileds = new String[0];
			return;
		}

		_KeyFileds = keyField.split(",");
	}

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

	public void createHtmlTraditional() {
		HtmlDocument doc = this.getHtmlClass().getDocument();
		try {
			super.createSkinTop();
		} catch (Exception err) {
			LOGGER.error(err.getLocalizedMessage());
		}

		try {
			super.createCss();
		} catch (Exception err) {
			LOGGER.error(err.getLocalizedMessage());
		}

		try {
			super.createJsTop();
		} catch (Exception err) {
			LOGGER.error(err.getLocalizedMessage());
		}

		// 重绘为box
		String box = super.getHtmlClass().getSysParas().getRequestValue().getString(FrameParameters.EWA_BOX);
		// 重绘为左引导
		String left = super.getHtmlClass().getSysParas().getRequestValue().getString(FrameParameters.EWA_LEFT);

		try {
			if (box == null && left == null) {
				doc.addScriptHtml("<div>");
				this.createContent();
				doc.addScriptHtml("</div>");

			} else { // box redraw
				this.createJsonFrame(); // 生成查询排序表达式
				// 用户自定义头部html

				String pageAddTop = this.getPageItemValue("AddHtml", "Top");
				doc.addScriptHtml(pageAddTop == null ? "" : pageAddTop.trim());

				String pageAddBottom = this.getPageItemValue("AddHtml", "Bottom");
				if (pageAddBottom != null && pageAddBottom.trim().length() > 0) {
					doc.addScriptHtml(pageAddBottom);
				}
			}
		} catch (Exception err) {
			LOGGER.error(err.getLocalizedMessage());
		}

		try {
			// 生成主底部 AddHtml-Bottom
			this.createSkinBottom();
		} catch (Exception err) {
			LOGGER.error(err.getLocalizedMessage());
		}
		try {
			// 配置页面定义的底部附加 Js
			this.createJsBottom();
		} catch (Exception err) {
			LOGGER.error(err.getLocalizedMessage());
		}
		try {
			// Frame脚本
			this.createJsFramePage();
		} catch (Exception err) {
			LOGGER.error(err.getLocalizedMessage());
		}
	}

	public void createContent() throws Exception {
		HtmlDocument doc = this.getHtmlClass().getDocument();
		UserXItem a = super.getHtmlClass().getUserConfig().getUserPageItem();
		String type = "";
		if (a.testName("ChartsShow")) {
			type = a.getSingleValue("ChartsShow").toUpperCase().trim();
		}

		// 用户自定义头部html
		String pageAddTop = this.getPageItemValue("AddHtml", "Top");
		doc.addScriptHtml(pageAddTop == null ? "" : pageAddTop.trim());
		doc.addFrameHtml(pageAddTop == null ? "" : pageAddTop.trim());
		if (!type.equals("CHART")) {// 仅图

			// Frame内容
			createFrameContent();

		}

	}

	/**
	 * 初始化列表重绘，搜索...参数
	 * 
	 * @param item
	 * @throws Exception
	 */
	private void initListUIParams(UserXItem item) throws Exception {
		if (!item.checkItemExists("ListUI")) {
			return;
		}
		UserXItemValues uv = item.getItem("ListUI");
		if (uv.count() == 0) {
			return;
		}

		RequestValue rv = super.getHtmlClass().getItemValues().getRequestValue();
		UserXItemValue x = uv.getItem(0);

		// 固定头部
		if ("yes".equalsIgnoreCase(rv.s(FrameParameters.EWA_LU_STICKY_HEADERS))) {
			this.luStickyHeaders = true;
		} else if (x.checkItemExists("luStickyHeaders")) {
			String v1 = x.getItem("luStickyHeaders");
			if (v1.equals("yes")) {
				this.luStickyHeaders = true;
			}
		}

		// 重绘按钮
		if (x.checkItemExists("luButtons") && !("no".equalsIgnoreCase(rv.s(FrameParameters.EWA_LU_BUTTONS)))) {
			String v1 = x.getItem("luButtons");
			if (v1.equals("1")) {
				this._IsLuButtons = true;
			}
		}
		// 重绘搜索
		if (x.checkItemExists("luSearch") && !("no".equalsIgnoreCase(rv.s(FrameParameters.EWA_LU_SEARCH)))) {
			String v1 = x.getItem("luSearch");
			if (v1.equals("1")) {
				this._IsLuSearch = true;
			} else if (v1.equals("2")) {
				this._IsLuSearch = true;
				// 合并文字搜索
				this._ComposeSearchTexts = true;
			} else if (v1.equals("3")) {
				// 分组搜索，就是先文字、日期，最后固定搜索（select,checkbox,radio)
				this._IsLuSearch = true;
				this._SearchGroup = false;
			}
		}
		// 单选多选
		if (x.checkItemExists("luSelect") && !("no".equalsIgnoreCase(rv.s(FrameParameters.EWA_LU_SELECT)))) {
			String v1 = x.getItem("luSelect");
			this._LuSelect = v1;
		}

		// 行上双击
		if (x.checkItemExists("luDblClick") && !("no".equalsIgnoreCase(rv.s(FrameParameters.EWA_LU_DBLCLICK)))) {
			String v1 = x.getItem("luDblClick");
			if (v1.equals("1")) {
				this._IsLuDblClick = true;
			}
			// 关联的按钮
			String v2 = x.getItem("luDblClickIdx");
			this._LuDblClickIdx = v2;
		}

		// 每行点击下来js
		if (x.checkItemExists("luAddPreRowFunc")) {
			String v1 = x.getItem("luAddPreRowFunc");
			this._LuAddPreRowFunc = v1.trim();
		}
	}

	/**
	 * 是否使用FrameHtml模板
	 * 
	 * @return
	 */
	public boolean isUsingTemplate() {
		RequestValue rv = super.getHtmlClass().getSysParas().getRequestValue();
		// 是否使用模板
		String frameTemplate = super.getPageItemValue("FrameHtml", "FrameHtml");
		if (frameTemplate == null || frameTemplate.trim().length() == 0) {
			return false;
		}
		if (rv.s(FrameParameters.EWA_LF_TEMP_NO) == null || rv.s(FrameParameters.EWA_TEMP_NO) == null) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IFrame#createFrameContent()
	 */
	public void createFrameContent() throws Exception {
		UserConfig uc = this.getHtmlClass().getUserConfig();
		UserXItem item = uc.getUserPageItem();

		// 初始化列表重绘，搜索...参数
		this.initListUIParams(item);

		RequestValue rv = super.getHtmlClass().getSysParas().getRequestValue();

		// 是否来自App调用，如果是的话，取消 onmouseover...的事件
		String paraEwaApp = rv.s(FrameParameters.EWA_APP);
		boolean isApp = paraEwaApp == null ? false : true;
		// 列表每行所有TD字符串进行md5签名，用于刷新数据 refreshPage or replaceRowsData 的比对
		boolean ewaRowSign = Utils.cvtBool(rv.s(FrameParameters.EWA_ROW_SIGN));

		HtmlDocument doc = this.getHtmlClass().getDocument();

		// 滑动脚本
		String fos = "EWA.F.FOS[\"" + super.getHtmlClass().getSysParas().getFrameUnid() + "\"]";

		// 皮肤定义的头部
		doc.addScriptHtml("<div>");
		String top = super.createSkinFCTop();

		if (!isApp) {
			// 鼠标滑出脚本
			top = top.replace("<table",
					"<table onmouseout='if(window.EWA && EWA.F && EWA.F.FOS && " + fos + "){" + fos + ".MOut(event)}'");
		}
		doc.addScriptHtml(top);
		doc.addFrameHtml(top);

		// Frame定义的页头
		String frameHeader = this.createFrameHeader();
		doc.addScriptHtml(frameHeader, "frame head");
		doc.addFrameHtml(frameHeader);

		MList tbs = super.getHtmlClass().getAction().getDTTables();
		if (tbs == null || tbs.size() == 0) {
			createItemHtmls(false);
			doc.addScriptHtml("<!-- no data -->");
		} else {
			DTTable tb = this.getSplitPageTable();

			if (tb == null) {
				tb = (DTTable) tbs.get(tbs.size() - 1);
			}

			super.getHtmlClass().getItemValues().setListFrameTable(tb);

			if (this.isSplitPage()) { // 分页
				// 获取记录总数
				this.queryRecords();
			}
			int colSize = 1; // 分栏数
			String s1 = super.getPageItemValue("PageSize", "ColSize");
			if (!(s1 == null || s1.trim().length() == 0)) {
				try {
					colSize = Integer.parseInt(s1);
				} catch (Exception e) {
					// nothing
				}
			}
			if (colSize <= 0) {
				colSize = 1;
			}

			FrameListGroup flGroup = null;
			if (this._GroupUserXItem != null) {
				flGroup = new FrameListGroup();
				flGroup.init(this._GroupUserXItem);

				String des = HtmlUtils.getDescription(this._GroupUserXItem.getItem("DescriptionSet"), "Info",
						super.getHtmlClass().getSysParas().getLang());// 描述
				des = "<span class='ewa-lf-grp-icon'>&nbsp;-&nbsp;</span> <span class='ewa-lf-grp-des'>" + des
						+ ": </span>";
				flGroup.setDescription(des);

				int colSpan = tb.getColumns().getCount();
				flGroup.setColSpan(colSpan);
				flGroup.setFrameGUID(super.getHtmlClass().getSysParas().getFrameUnid());
			}
			int colSizeInc = 1;

			MStr sb = new MStr();

			// 是否使用模板
			boolean isUseTemplate = this.isUsingTemplate();
			String frameTemplate = super.getPageItemValue("FrameHtml", "FrameHtml");

			MStr rowContents = new MStr();
			for (int i = 0; i < tb.getCount(); i++) {
				tb.getRow(i); // 将数据移动到当前行
				String keyExp = "EWA_KEY=\"" + this.createItemKeys() + "\" ";
				// 行属性表达式，2022-08-01 （RowAttributeSet ）

				String[] rowAttrExp = this.createRowAttrs(uc);
				String rowAttrs = rowAttrExp[0]; // 属性表达式
				String rowClassName = rowAttrExp[1]; // 类表达式 class

				String rowHtml = isUseTemplate ? this.createItemHtmlsByFrameHtml(frameTemplate, "FrameList")
						: this.createItemHtmls();
				String groupHtml = this.createFrameGroup(flGroup);
				sb.append(groupHtml);

				if (colSizeInc == 1 || colSize == 1) {
					sb.a("<tr ").a(keyExp).a(" class=\"ewa-lf-data-row");
					if (StringUtils.isNotBlank(rowClassName)) {
						sb.a(" ").a(rowClassName);// class
					}
					sb.a("\" ");
					if (StringUtils.isNotBlank(rowAttrs)) {
						sb.a(rowAttrs);
					}
					rowContents = new MStr();
					if (!isApp) {
						// 是否来自App调用，如果是的话，取消 onmouseover...的事件
						StringBuilder stringBuilder = new StringBuilder();
						stringBuilder.append(" onmouseover='if(window.EWA&&");
						stringBuilder.append(fos);
						stringBuilder.append("){").append(fos).append(".MOver(this,event)}'");
						stringBuilder.append(" onmousedown='if(window.EWA&&");
						stringBuilder.append(fos);
						stringBuilder.append("){").append(fos).append(".MDown(this,event)}'");
						sb.a(stringBuilder);
					}

				}
				rowContents.append(rowHtml);
				if (colSizeInc == colSize || colSize == 1 || i == tb.getCount() - 1) {
					if (ewaRowSign) {
						// 列表每行所有TD字符串进行md5签名，用于刷新数据 refreshPage or replaceRowsData 的比对
						String rowMd5 = Utils.md5(rowContents.toString());
						sb.a(" ewa_row_sign='" + rowMd5 + "'");
					}
					sb.al(">");
					sb.a(rowContents);
					if (colSize == 1 || colSizeInc == colSize || i < tb.getCount() - 1) {
						sb.al("</tr>");
					}
				}
				colSizeInc++;
				if (colSizeInc > colSize) {
					colSizeInc = 1;
				}
			}
			if (colSize > 1) {
				for (int i = colSizeInc; i <= colSize; i++) {
					sb.appendLine("<td></td>");
				}
				if (colSizeInc <= colSize) {
					sb.appendLine("</tr>");
				}
			}
			doc.addScriptHtml(sb.toString(), "Frame content");
			doc.addFrameHtml(sb.toString());
		}
		// 皮肤定义定义的尾部
		String bottom = super.createSkinFCBottom();
		doc.addScriptHtml(bottom);
		doc.addFrameHtml(bottom);

		// Frame定义的页脚
		try {
			this.createFrameFooter();
		} catch (Exception err) {

		}
		doc.addScriptHtml("</div>");
	}

	public int getListFrameRecordCount() {
		return _ListFrameRecordCount;
	}

	private String createFrameGroup(FrameListGroup flGroup) throws Exception {
		if (flGroup == null) {
			return "";
		}
		String val = super.getHtmlClass().getItemValues().getValue(this._GroupUserXItem);
		String s1 = flGroup.makeHtml(val);
		return s1;
	}

	/**
	 * 生成每行数据的key值
	 * 
	 * @return
	 */
	String createItemKeys() {
		if (this._KeyFileds == null) {
			// 初始化Keys表达式
			initKeys();
		}
		if (this._KeyFileds.length == 0) {
			return "";
		}
		String[] fileds = _KeyFileds;
		String keyExp = "";
		for (int m = 0; m < fileds.length; m++) {
			String key = fileds[m].trim();
			if (key.indexOf(".") >= 0) { // 去除类似 A.ID的表示方法的A.
				String[] keys1 = key.split("\\.");
				key = keys1[keys1.length - 1].trim();
			}
			String keyVal;
			try {
				keyVal = Utils.textToInputValue(super.getHtmlClass().getItemValues().getValue(key, key));
			} catch (Exception e) {
				keyVal = e.getLocalizedMessage();
			}
			if (m == 0) {
				keyExp += keyVal;
			} else {
				keyExp += "," + keyVal;
			}
		}
		return keyExp;
	}

	/**
	 * 行属性表达式，2022-08-01 （RowAttributeSet ）
	 * 
	 * @param uc
	 * @return 行属性表达式 0=属性，1=CSS类名
	 * @throws Exception
	 */
	String[] createRowAttrs(UserConfig uc) throws Exception {
		if (!uc.getUserPageItem().testName("RowAttributeSet")) {
			String[] returns = { "", "" };
			return returns;
		}
		ItemValues iv = super.getHtmlClass().getItemValues();

		MStr sbPageAttr = new MStr(); // 属性表达式
		String className = ""; // CSS类名称

		UserXItemValues u = uc.getUserPageItem().getItem("RowAttributeSet");
		Map<String, Boolean> map = new HashMap<>();
		for (int i = 0; i < u.count(); i++) {
			UserXItemValue u0 = u.getItem(i);

			String logic = u0.getItem("RowAttLogic").trim();
			if (StringUtils.isNotBlank(logic)) {
				// 逻辑判断，如果有逻辑判断，则执行，否则不进行判断

				String logic1 = iv.replaceLogicParameters(logic);
				if (!ULogic.runLogic(logic1)) {
					continue;
				}
			}

			String name = u0.getItem("RowAttName").trim();
			String value = u0.getItem("RowAttValue").trim();

			String attName = HtmlUtils.createAttNameByValue(name, value);
			if (StringUtils.isBlank(attName)) {
				continue;
			}

			if (map.containsKey(attName)) {
				// 重复的属性
				LOGGER.warn("Repeated row attr: {}={}", attName, value);
				continue;
			}
			map.put(attName, true);

			String attValue = iv.replaceLogicParameters(value);

			if ("class".equalsIgnoreCase(attName)) {
				className = attValue;
			} else {
				sbPageAttr.a(" ");
				sbPageAttr.a(attName);
				sbPageAttr.a("=\"");
				sbPageAttr.a(Utils.textToInputValue(attValue));
				sbPageAttr.a("\"");
			}
		}
		String[] returns = { sbPageAttr.toString(), className };

		return returns;
	}

	/**
	 * 是否分页 用户定义参数 EWA_IS_SPLIT_PAGE 或 PageSize.IsSplitPage
	 * 
	 * @return
	 */
	public boolean isSplitPage() {
		// 用户定义参数 EWA_IS_SPLIT_PAGE yes表示分页，no表示不分页
		String paramIsSplit = super.getHtmlClass().getItemValues().getRequestValue()
				.getString(FrameParameters.EWA_IS_SPLIT_PAGE);
		if (paramIsSplit != null) {
			return Utils.cvtBool(paramIsSplit);
		}
		String isSplitPage = this.getPageItemValue("PageSize", "IsSplitPage");

		return "1".equals(isSplitPage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IFrame#createFrameFooter()
	 */
	public void createFrameFooter() throws Exception {

		if (!this.isSplitPage()) {
			return;
		}

		String footer = super.createSkinFCFooter();
		PageSplit ps = new PageSplit(_ListFrameRecordCount, super.getHtmlClass().getItemValues().getRequestValue(),
				this.getUserSettingPageSize());

		super._PageSplit = ps;

		// System.out.println(this);

		String s1 = footer.replace(SkinFrame.TAG_LF_RECORDCOUNT, ps.getRecordCount() + "");
		s1 = s1.replace(SkinFrame.TAG_LF_CURPAGE, ps.getPageCurrent() + "");
		s1 = s1.replace(SkinFrame.TAG_LF_PAGESIZE, ps.getPageSize() + "");
		s1 = s1.replace(SkinFrame.TAG_LF_PAGECOUNT, ps.getPageCount() + "");

		String lang = getHtmlClass().getSysParas().getLang();

		String pageFirst = this.createSkinFCFist(lang);
		if (pageFirst == null) {
			pageFirst = "null";
		}
		String pageNext = this.createSkinFCNext(lang);
		if (pageNext == null) {
			pageNext = "null";
		}
		String pagePrev = this.createSkinFCPrev(lang);
		if (pagePrev == null) {
			pagePrev = "null";
		}
		String pageLast = this.createSkinFCLast(lang);
		if (pageLast == null) {
			pageLast = "null";
		}

		String gunid = super.getHtmlClass().getSysParas().getFrameUnid();

		String u = super.getHtmlClass().getItemValues().getRequestValue().getString("EWA.CPF");
		String q = super.getHtmlClass().getItemValues().getRequestValue().getString("EWA_QUERY_ALL");
		if (q != null && q.length() > 0) {
			int loc1 = q.indexOf("&EWA_PAGECUR=");
			if (loc1 < 0) {
				loc1 = q.indexOf("EWA_PAGECUR=");
			}
			if (loc1 > 0) {
				int loc2 = q.indexOf("&", loc1 + 1);

				if (loc2 > 0) {
					q = q.substring(0, loc1) + q.substring(loc2);
				} else {
					q = q.substring(0, loc1);
				}
			}
			// q = q.replace("&EWA_PAGECUR=", "&_1=");
			u = u + "?" + q + "&EWA_PAGECUR=IDX";
		} else {
			u = u + "?" + "EWA_PAGECUR=IDX";
		}
		String js = "<a href=\"" + u + "\" onclick=\"EWA.F.FOS['" + gunid + "'].Goto(IDX);return false;\">";
		if (ps.getPageCount() > 1) {
			if (ps.getPageCurrent() > 1) {
				pageFirst = js.replace("IDX", "1") + pageFirst + "</a>";
				pagePrev = js.replace("IDX", (ps.getPageCurrent() - 1) + "") + pagePrev + "</a>";
			}
			if (ps.getPageCurrent() < ps.getPageCount()) {
				pageLast = js.replace("IDX", ps.getPageCount() + "") + pageLast + "</a>";
				pageNext = js.replace("IDX", (ps.getPageCurrent() + 1) + "") + pageNext + "</a>";
			}
		}

		s1 = s1.replace(SkinFrame.TAG_LF_FIRST, pageFirst);
		s1 = s1.replace(SkinFrame.TAG_LF_NEXT, pageNext);
		s1 = s1.replace(SkinFrame.TAG_LF_PREV, pagePrev);
		s1 = s1.replace(SkinFrame.TAG_LF_LAST, pageLast);

		JSONObject json = new JSONObject();
		json.put("RECORD_COUNT", ps.getRecordCount());
		json.put("PAGE_CURRENT", ps.getPageCurrent());
		json.put("PAGE_SIZE", ps.getPageSize());
		json.put("PAGE_COUNT", ps.getPageCount());
		s1 = s1.replace("{JSONEXP}", json.toString().replace("\"", "&quot;"));
		// 在class里记录页数，可以根据count=1时隐含 （ewa-lf-page-count-1）
		s1 = s1.replace("ewa-lf-frame-split", "ewa-lf-frame-split ewa-lf-page-count-" + ps.getPageCount());
		this.getHtmlClass().getDocument().addScriptHtml(s1, "SPLIT PAGE");
		this.getHtmlClass().getDocument().addFrameHtml(s1);
	}

	/**
	 * 获取页面的参数
	 * 
	 * @return
	 */
	public JSONObject createJsonPageInfo() {
		PageSplit ps = new PageSplit(_ListFrameRecordCount, super.getHtmlClass().getItemValues().getRequestValue(),
				this.getUserSettingPageSize());
		super._PageSplit = ps;

		JSONObject json = new JSONObject();
		json.put("RECORD_COUNT", ps.getRecordCount());
		json.put("PAGE_CURRENT", ps.getPageCurrent());
		json.put("PAGE_SIZE", ps.getPageSize());
		json.put("PAGE_COUNT", ps.getPageCount());

		return json;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IFrame#createFrameHeader()
	 */
	public String createFrameHeader() throws Exception {
		// 判断参数EWA_HIDDEN_CAPTION或Size.HiddenCaption
		if (super.isHiddenCaption()) {// 不显示列头
			return "";
		}

		MStr sb = new MStr();
		// header信息
		String header = super.createSkinFCHeader();
		String userOrder = super.getHtmlClass().getItemValues().getRequestValue()
				.getString(FrameParameters.EWA_LF_ORDER);
		String fUnid = super.getHtmlClass().getSysParas().getFrameUnid();
		sb.appendLine("<tr class='ewa-lf-header' EWA_TAG='HEADER'>");
		for (int i = 0; i < super.getHtmlClass().getUserConfig().getUserXItems().count(); i++) {
			UserXItem uxi = super.getHtmlClass().getUserConfig().getUserXItems().getItem(i);

			// 检查是否为隐含字段，在逻辑控制页面生成
			if (super.getHtmlClass().getSysParas().isHiddenColumn(uxi.getName())) {
				continue;
			}

			// 检查是否为隐含字段，在Page的LogicShow中定义
			if (this.isHiddenField(uxi.getName())) {
				continue;
			}

			String s1 = this.createFrameHeaderCell(uxi, fUnid, userOrder, header);
			sb.appendLine(s1);
		}
		sb.appendLine("</tr>");
		return sb.toString();
	}

	/**
	 * 生成列表的标头的每个单元格
	 * 
	 * @param uxi
	 * @param fUnid
	 * @param userOrder
	 * @param header
	 * @return
	 * @throws Exception
	 */
	private String createFrameHeaderCell(UserXItem uxi, String fUnid, String userOrder, String header)
			throws Exception {
		String des = HtmlUtils.getDescription(uxi.getItem("DescriptionSet"), "Info",
				super.getHtmlClass().getSysParas().getLang());// 描述
		String memo = HtmlUtils.getDescription(uxi.getItem("DescriptionSet"), "Memo",
				super.getHtmlClass().getSysParas().getLang());// 描述
		if (uxi.getName().equals("*")) {
			String[] ffs = uxi.getSingleValue("DataItem", "DataField").replace(" ", "").split(",");
			String[] infos = new String[ffs.length];
			String[] memos = new String[ffs.length];
			String[] infos1 = des.split(",");
			String[] memos1 = memo.split(",");
			MStr sb = new MStr();
			for (int i = 0; i < ffs.length; i++) {
				if (infos1.length > i) {
					infos[i] = infos1[i].trim();
				} else {
					infos[i] = "未定义";
				}
				if (memos1.length > i) {
					memos[i] = memos1[i].trim();
				} else {
					memos[i] = "";
				}
			}
			for (int i = 0; i < ffs.length; i++) {
				String title = memos[i] == null || memos[i].length() == 0 ? ""
						: " title=\"" + Utils.textToInputValue(memos[i]) + "\"";
				String s1 = header.replace(SkinFrame.TAG_ITEM,
						"<nobr id='" + ffs[i] + "'" + title + ">" + infos[i] + "</nobr>");
				s1 = s1.replaceFirst("!!", "");
				sb.al(s1);
			}

			return sb.toString();

		}

		header = header.replace("class=\"", "class=\"ewa-col-" + uxi.getName() + " ");

		String cellOrder = this.createOrderCell(uxi, userOrder, fUnid, des);

		String orderSearchExp = this.createCellSearch(uxi, fUnid);

		String title = memo == null || memo.trim().length() == 0 ? ""
				: " title=\"" + Utils.textToInputValue(memo) + "\"";
		String s1 = header.replace(SkinFrame.TAG_ITEM,
				"<nobr id='" + uxi.getName() + "'" + title + ">" + cellOrder + orderSearchExp + "</nobr>");
		String style = "style=\"";
		boolean haveStyle = false;
		if (uxi.testName("ParentStyle") && uxi.getItem("ParentStyle").count() > 0) {
			String st = uxi.getItem("ParentStyle").getItem(0).getItem(0);
			if (st.trim().length() > 0) {
				style += st + "; ";
				haveStyle = true;
			}
		}

		String tag = uxi.getSingleValue("Tag");
		if (this._IsLuButtons && (tag.equalsIgnoreCase("button") || tag.equalsIgnoreCase("submit")
				|| tag.equalsIgnoreCase("butFlow"))) {
			style += "display: none;";
			haveStyle = true;
		}
		if (haveStyle) {
			style += "\" ";
			s1 = s1.replaceFirst("!!", Matcher.quoteReplacement(style));
		} else {
			s1 = s1.replaceFirst("!!", "");
		}
		return s1;

	}

	/**
	 * 生成排序头表达式
	 * 
	 * @param uxi
	 * @param userOrder
	 * @param fUnid
	 * @param des
	 * @return
	 * @throws Exception
	 */
	private String createOrderCell(UserXItem uxi, String userOrder, String fUnid, String des) throws Exception {
		String tag = uxi.getItem("Tag").getItem(0).getItem(0);
		if (tag.equalsIgnoreCase("checkbox") || tag.equalsIgnoreCase("checkboxgrid")) {
			return "<a class='EWA_TD_HA' href='javascript:EWA.F.FOS[\"" + fUnid + "\"].CheckedAll()'>" + des + "</a>";
		}
		if (!uxi.testName("OrderSearch")) {
			return des;
		}
		UserXItemValues us = uxi.getItem("OrderSearch");
		if (us.count() == 0) {
			return des;
		}

		UserXItemValue u = uxi.getItem("OrderSearch").getItem(0);
		boolean isOrder = u.getItem("IsOrder").equals("1") ? true : false;
		if (!isOrder) {
			return des;
		}
		boolean isGroup = false;
		boolean isGroupDefault = false;
		if (u.testName("IsGroup")) {
			isGroup = u.getItem("IsGroup").equals("1") ? true : false;
			isGroupDefault = u.getItem("IsGroupDefault").equals("1") ? true : false;
		}
		String exp = uxi.getName();

		// 排序表达式
		String mark = "";
		if (userOrder != null && userOrder.trim().length() > 0) {
			String[] orderNames = userOrder.split(" ");
			String asc = "";
			if (orderNames.length > 1) {
				asc = orderNames[1].trim();
			}

			if (uxi.getName().equalsIgnoreCase(orderNames[0].trim())) {
				if (orderNames.length == 1 || asc.equalsIgnoreCase("asc")) {
					exp += " desc";
					mark = " v";
				} else {
					mark = " ^";
					exp += " asc";
				}
			}
			if (isGroup && orderNames[0].trim().equalsIgnoreCase(uxi.getName())) {
				this._GroupUserXItem = uxi;
			}
		} else {
			if (isGroup && isGroupDefault && this._GroupUserXItem == null) {
				_GroupUserXItem = uxi;
			}
		}
		String rst = "<a class='EWA_TD_HA' href='javascript:EWA.F.FOS[\"" + fUnid + "\"].Sort(\"" + exp + "\")'>" + des
				+ mark + "</a>";
		return rst;
	}

	/**
	 * 检查是否为检索
	 * 
	 * @param uxi
	 * @return
	 * @throws Exception
	 */
	private String createCellSearch(UserXItem uxi, String fUnid) throws Exception {
		if (!uxi.testName("OrderSearch")) {
			return "";
		}
		UserXItemValues us = uxi.getItem("OrderSearch");
		if (us.count() == 0) {
			return "";
		}
		UserXItemValue u = uxi.getItem("OrderSearch").getItem(0);
		String searchType = u.getItem("SearchType");
		if (searchType.equals("")) {
			return "";
		}
		MStr s = new MStr();
		s.a(uxi.getName() + ": {\"T\": \"" + searchType + "\"");

		if (u.testName("IsSearchQuick")) {
			String isSearchQuick = u.getItem("IsSearchQuick");
			if (isSearchQuick.equals("1")) {
				s.a(", \"isSearchQuick\":true");
			}
		}
		RequestValue rv = super.getHtmlClass().getItemValues().getRequestValue();
		boolean isEn = rv.getLang().equals("enus");

		if (searchType.equals("fix")) {
			String searchSql = u.getItem("SearchSql");
			String searchMulti = u.getItem("SearchMulti");

			if (searchSql.trim().length() == 0) {
				LOGGER.error("固定查询没有定义SQL");
				return "";
			}

			// 区分中英文
			String cacheKey = searchSql + " . " + isEn;
			CachedValue c = CachedValueManager.getValue(cacheKey);
			if (c == null) {// 不在缓存中
				MStr s1 = new MStr();
				DataConnection cnn = super.getHtmlClass().getItemValues().getDataConn();

				cnn.executeQuery(searchSql);
				DTTable tb = DTTable.getJdbcTable(searchSql, cnn);

				if (tb == null || !tb.isOk()) {
					LOGGER.error("数据查询错误");
					return "";
				}

				cnn.getResultSetList().removeValue(cnn.getLastResult());
				s1.a(", D: [");
				int idxId = 0;
				int idxTxt = 1;
				if (isEn) {
					String colName = tb.getColumns().getColumn(idxTxt).getName();
					int idxEn = tb.getColumns().getNameIndex(colName + "_en");
					if (idxEn == -1) {
						idxEn = tb.getColumns().getNameIndex(colName + "en");
					}
					if (idxEn == -1) {
						idxEn = tb.getColumns().getNameIndex(colName + "_enus");
					}
					if (idxEn == -1) {
						idxEn = tb.getColumns().getNameIndex(colName + "enus");
					}
					if (idxEn > 0) {
						idxTxt = idxEn;
					}
				}
				int inc = 0;
				for (int i = 0; i < tb.getCount(); i++) {
					DTRow r = tb.getRow(i);

					String t1 = r.getCell(idxId).getString();
					String t2 = r.getCell(idxTxt).toString();
					if (t1 == null || t2 == null) {
						continue;
					}
					if (inc > 0) {
						s1.a(",");
					}
					inc++;
					s1.a("[\"" + Utils.textToJscript(t1.trim()) + "\", \"" + Utils.textToJscript(t2.trim()) + "\"]");
				}
				if (searchSql.indexOf("@") == -1) { // searchSql 有参数的话不缓存
					CachedValueManager.addValue(cacheKey, s1.toString());
				}
				s.a(s1.toString());
				tb.setName(searchSql);
				super.getSearchFixTables().put(uxi.getName(), tb);
			} else { // 从缓存中获取
				s.a(c.getValue().toString());
			}
			s.a("], M:'" + searchMulti + "'");
		}
		s.a("}");
		if (this._SearchExp.length() > 0) {
			this._SearchExp.a(", ");
		}
		this._SearchExp.al(s);
		// String exp = " <a class='EWA_TD_HA' href=\"javascript:EWA.F.FOS['"
		// + fUnid + "'].Search()\">?</a>";
		String exp = "";
		return exp;
	}

	public String createItemHtmls() throws Exception {
		return createItemHtmls(true);
	}

	/**
	 * 根据 FrameHtml定义的 html模板，创建单元数据
	 * 
	 * @param template 模板
	 * @return
	 * @throws Exception
	 */
	public String createItemHtmlsByFrameHtml(String template, String frameTag) throws Exception {
		ItemValues ivs = super.getHtmlClass().getItemValues();
		Map<String, String> cellData = new HashMap<String, String>();
		for (int i = 0; i < super.getHtmlClass().getUserConfig().getUserXItems().count(); i++) {
			UserXItem uxi = super.getHtmlClass().getUserConfig().getUserXItems().getItem(i);

			// 检查是否为隐含字段，在Page的LogicShow中定义
			if (this.isHiddenField(uxi.getName())) {
				continue;
			}

			IItem item = super.getHtmlClass().getItem(uxi);
			this._LastItem = item;

			this.setItemEwaConfigItemUnidPrefix(item);

			String itemHtml = item.createItemHtml();

			if (itemHtml.indexOf("@") >= 0) {
				// 替换其它@参数
				itemHtml = super.getHtmlClass().getItemValues().replaceParameters(itemHtml, false, false);
			}
			cellData.put(uxi.getName().toUpperCase(), itemHtml);
		}

		MListStr al = Utils.getParameters(template, "@");

		StringBuilder tmp = new StringBuilder();

		tmp.append(template);
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

			Utils.replaceStringBuilder(tmp, "@" + paraName, paraValue);
		}
		StringBuilder tmp1 = new StringBuilder();
		if (frameTag.equals("FrameList")) {
			tmp1.append("<td class='EWA_TD_M ewa-template-lf'>");
		} else if (frameTag.equals("Grid")) {
			tmp1.append("<div class='ewa-template-grid'>");
		}

		tmp1.append(tmp);
		if (frameTag.equals("FrameList")) {
			tmp1.append("</td>");
		} else if (frameTag.equals("Grid")) {
			tmp1.append("</div>");
		}
		return tmp1.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IFrame#createItemHtmls()
	 */
	public String createItemHtmls(boolean hasData) throws Exception {
		MStr sb = new MStr();
		SysParameters sysParas = super.getHtmlClass().getSysParas();
		boolean isCheckSubBottom = false;
		if (_SubBottoms == null) {
			_SubBottoms = new HashMap<String, String>();
			isCheckSubBottom = true;
		}

		for (int i = 0; i < super.getHtmlClass().getUserConfig().getUserXItems().count(); i++) {
			UserXItem uxi = super.getHtmlClass().getUserConfig().getUserXItems().getItem(i);
			if (sysParas.isHiddenColumn(uxi.getName())) {
				continue;
			}
			if (isCheckSubBottom) {
				String subBottom = uxi.getSingleValue("DataItem", "SumBottom");
				if (subBottom.equalsIgnoreCase("yes")) {
					_SubBottoms.put(uxi.getName(), "1");
				}
			}

			// 检查是否为隐含字段，在Page的LogicShow中定义
			if (this.isHiddenField(uxi.getName())) {
				continue;
			}
			if (!hasData) {
				continue;
			}
			String s2 = this.createItemHtmlCell(uxi);

			// td 附加的class
			if (!this._TdAddCssClass.containsKey(i)) {
				StringBuilder sbSt1 = new StringBuilder();
				sbSt1.append("class=\"EWA_TD_M ewa-col-");
				sbSt1.append(uxi.getName());
				if (uxi.testName("DataItem")) {
					String item_format = uxi.getSingleValue("DataItem", "Format");
					if (item_format != null && item_format.trim().length() > 0) {
						sbSt1.append(" ewa-lf-fm-");
						sbSt1.append(item_format.toLowerCase().trim());
					}
				}
				sbSt1.append("\"");
				this._TdAddCssClass.put(i, sbSt1.toString());
			}
			String st1 = this._TdAddCssClass.get(i);

			// tag 的 IsLFEdit 0 不可编辑，1双击，2单击
			if (this._LastItem != null && this._LastItem.getTagIsLfEdit() > 0) {
				st1 = st1.substring(0, st1.length() - 1) + " ewa-lf-edit\""; // 可编辑td
			}

			s2 = s2.replace("class=\"EWA_TD_M\"", st1);
			sb.appendLine(s2);
		}
		if (sb.indexOf("@") > 0) { // 替换未替换的值
			return super.getHtmlClass().getItemValues().replaceParameters(sb.toString(), false);
		} else {
			return sb.toString();
		}
	}

	/**
	 * 生成页面的JSON数据
	 * 
	 * @return
	 * @throws Exception
	 */
	public String createJsonContent() throws Exception {
		MList tbs = super.getHtmlClass().getAction().getDTTables();

		if (tbs == null || tbs.size() == 0) {
			return "[]";
		}
		DTTable tb = (DTTable) tbs.get(tbs.size() - 1);
		super.getHtmlClass().getItemValues().setListFrameTable(tb);

		JSONArray arr = new JSONArray();

		for (int i = 0; i < tb.getCount(); i++) {
			DTRow row = tb.getRow(i); // 将数据移动到当前行
			String keyExp = this.createItemKeys();
			JSONObject rowJson = this.createJsonRow(row);
			rowJson.put(FrameParameters.EWA_KEY, keyExp);
			arr.put(rowJson);
		}
		return arr.toString();
	}

	/**
	 * 生成页面的每一行JSON数据
	 * 
	 * @return
	 * @throws Exception
	 */
	private JSONObject createJsonRow(DTRow row) throws Exception {
		JSONObject obj = new JSONObject();
		SysParameters sysParas = super.getHtmlClass().getSysParas();
		HashMap<String, Boolean> map = null;

		RequestValue rv = super.getHtmlClass().getSysParas().getRequestValue();

		boolean isUnScaned = _JsonUsedFields == null;
		if (isUnScaned) {
			_JsonUsedFields = new ArrayList<String>();
			map = new HashMap<String, Boolean>();
		}
		for (int i = 0; i < super.getHtmlClass().getUserConfig().getUserXItems().count(); i++) {
			UserXItem uxi = super.getHtmlClass().getUserConfig().getUserXItems().getItem(i);
			if (sysParas.isHiddenColumn(uxi.getName())) {
				continue;
			}
			// if (uxi.getName().equals("ENQ_JNY_CONTENT")) {
			// int k = 0;
			// k++;
			// }
			if (isUnScaned) {
				map.put(uxi.getName().toUpperCase().trim(), true);
			}

			// 检查是否为隐含字段，在Page的LogicShow中定义
			// if (this.isHiddenField(uxi.getName())) {
			// continue;
			// }

			String s2 = this.createJsonCell(uxi);
			if (s2 != null && s2.indexOf("~!@`") > 0) {
				String[] ss = s2.split("~!@`");
				obj.put(uxi.getName(), ss[0]);
				if (ss.length > 1) {
					obj.put(uxi.getName() + "_HTML", "null".equals(ss[1]) ? null : ss[1]);
				} else {
					obj.put(uxi.getName() + "_HTML", "null".equals(ss[0]) ? null : ss[0]);
				}
			} else {
				if (s2 != null) {
					if (s2.indexOf("[B") == 0) {
						byte[] buf = (byte[]) row.getCell(uxi.getName()).getValue();
						s2 = ItemImage.getImage(rv.getContextPath(), buf);
					}
				}
				obj.put(uxi.getName(), "null".equals(s2) ? null : s2);
			}
		}
		if (isUnScaned) {
			for (int i = 0; i < row.getTable().getColumns().getCount(); i++) {
				String name = row.getTable().getColumns().getColumn(i).getName();
				if (!map.containsKey(name.toUpperCase())) {
					_JsonUsedFields.add(name);
				}
			}
		}
		// 将不在页面框架上的数据显示出来
		for (int i = 0; i < this._JsonUsedFields.size(); i++) {
			String name = this._JsonUsedFields.get(i);
			if (!row.getCell(name).isNull()) {
				obj.put(name, row.getCell(name).getValue());
			}
		}
		return obj;

	}

	/**
	 * 生成单元格JSON数据
	 * 
	 * @param uxi
	 * @return
	 * @throws Exception
	 */
	private String createJsonCell(UserXItem uxi) throws Exception {
		IItem item = super.getHtmlClass().getItem(uxi);
		String val = item.createFormatValue();
		return val;
	}

	/**
	 * 配置文件的对象的 JSON表达式(在FrameBase中生成)
	 * 
	 * @return
	 * @throws Exception
	 */
	public String createJsonFrame() throws Exception {
		SysParameters sysParas = super.getHtmlClass().getSysParas();
		for (int i = 0; i < super.getHtmlClass().getUserConfig().getUserXItems().count(); i++) {
			UserXItem uxi = super.getHtmlClass().getUserConfig().getUserXItems().getItem(i);
			this.createCellSearch(uxi, sysParas.getFrameUnid());
		}
		String s1 = super.createJsonFrame();

		return s1;
	}

	/**
	 * 获取流程应用表
	 * 
	 * @throws Exception
	 */
	private void loadWorkFlowApp() throws Exception {
		if (this._WorkFlowBut != null) {
			return;
		}
		RequestValue rv = super.getHtmlClass().getItemValues().getRequestValue();
		DataConnection cnn = super.getHtmlClass().getItemValues().getDataConn();
		String gunid = super.getHtmlClass().getSysParas().getFrameUnid();

		String thisXmlName = super.getHtmlClass().getSysParas().getXmlName();
		thisXmlName = UserConfig.filterXmlName(thisXmlName);

		// 工作量应用表 （_EWA_WF_APP）
		JSONObject cfg = EwaWfMain.getAppCfg(thisXmlName, super.getHtmlClass().getSysParas().getItemName(), cnn);

		String xmlNameApp = null, itemNameApp = null, wfXmlName = null, wfItemName = null, wfRefTable = null,
				checkField = null;

		xmlNameApp = UserConfig.filterXmlName(cfg.optString("APP_XMLNAME"));
		itemNameApp = cfg.optString("APP_ITEMNAME");
		wfXmlName = cfg.optString("APP_FRAME_XMLNAME");
		wfItemName = cfg.optString("APP_FRAME_ITEMNAME");
		wfRefTable = cfg.optString("APP_WF_TABLE");
		checkField = cfg.optString("APP_WF_FIELD");

		// EWA.UI.Dialog.OpenReloadClose('1366885157','ewa|ewa_wf.xml',
		// '_EWA_WF_UNIT.Frame.NewModify', false, paras);

		if (wfXmlName == null || wfXmlName.trim().length() == 0) {
			throw new Exception("App未定义的配置参数 APP_FRAME_XMLNAME");
		}
		if (wfItemName == null || wfItemName.trim().length() == 0) {
			throw new Exception("App未定义的配置参数 APP_FRAME_ITEMNAME");
		}
		wfXmlName = UserConfig.filterXmlName(wfXmlName);
		String para = "APP_XMLNAME=" + xmlNameApp + "&APP_ITEMNAME=" + itemNameApp
				+ "&SYS_STA_RID=[RID]&EWA_ACTION_KEY=[RID]&APP_WF_UNIT_CUR=@" + checkField + "&SYS_STA_TABLE="
				+ wfRefTable + "&WF_ID=" + cfg.optString("WF_ID").toString().trim();

		// //逻辑分值的值
		// String wfLogic = super.getPageItemValue("Workflow", "WfLogic");
		// if(wfLogic==null){
		// wfLogic="";
		// }
		// para+="&APP_WF_LOGIC="+wfLogic;

		String q = rv.getString("EWA_QUERY");
		if (q != null && q.length() > 0) {
			para += "&" + q;
		}

		String appPks = cfg.optString("APP_WF_PKS");
		String[] pks = appPks.split(",");
		for (int i = 0; i < pks.length; i++) {
			para += "&" + pks[i] + "=@" + pks[i];
		}
		String wfTmp = " onclick=\"EWA.UI.Dialog.OpenReloadClose('" + gunid + "','" + wfXmlName + "','" + wfItemName
				+ "',false,'" + para + "')\"";
		_WorkFlowBut = wfTmp;
		JSONObject json = new JSONObject();
		json.put("X", wfXmlName);
		json.put("I", wfItemName);
		json.put("P", para);
		if (this._KeyFileds == null) {
			initKeys();
		}
		json.put("RID", this._KeyFileds);
		_WorkFlowButJson = json.toString();
	}

	private void createCellParentStyle(UserXItem uxi) throws Exception {
		if (uxi.getParentStyle() != null) { // 已经设置过了
			return;
		}
		MStr style = new MStr();
		if (uxi.testName("ParentStyle") && uxi.getItem("ParentStyle").count() > 0) {
			style.a(uxi.getItem("ParentStyle").getItem(0).getItem(0));
			if (style.length() > 0) {
				style.a("; ");
			}
		}
		if (uxi.testName("XStyle") && uxi.getItem("XStyle").count() > 0) {
			UserXItemValue vs = uxi.getItem("XStyle").getItem(0);
			for (int i = 0; i < vs.count(); i++) {
				String v = vs.getItem(i).trim();
				String name = vs.getName(i);
				if (v.equalsIgnoreCase("no") || v.equals("")) {
					continue;
				}
				v = Utils.textToInputValue(v);
				if (name.equalsIgnoreCase("XStyleAlign")) {
					style.a("text-align:center;");
				} else if (name.equalsIgnoreCase("XStyleVAlign")) {
					style.a("vertical-align:middle; ");
				} else if (name.equalsIgnoreCase("XStyleNoWrap")) {
					style.a("white-space:nowrap;");
				} else if (name.equalsIgnoreCase("XStyleFixed")) {
					style.a("overflow:hidden;text-overflow:ellipsis;a:1;display:block;a:2;");
				} else if (name.equalsIgnoreCase("XStyleBold")) {
					style.a("font-weight:bold;");
				} else if (name.equalsIgnoreCase("XStyleColor")) {
					style.a("color:" + v + ";");
				} else if (name.equalsIgnoreCase("XStyleWidth")) {
					style.a("width:" + v.toLowerCase() + ";");
				} else if (name.equalsIgnoreCase("XStyleCursor")) {
					style.a("cursor:" + v.toLowerCase() + ";");
				}
			}
		}

		uxi.setParentStyle(style.toString());
	}

	/**
	 * 设置 ItemEwaConfigItem的 FrameUnid前缀
	 * 
	 * @param item
	 */
	private void setItemEwaConfigItemUnidPrefix(IItem item) {
		if ("ItemEwaConfigItem".equals(item.getClass().getSimpleName())) {
			ItemEwaConfigItem item0 = (ItemEwaConfigItem) item;
			String key = this.createItemKeys();
			item0.setFrameUnidPrefix("IECI_" + key + "_");
		}
	}

	/**
	 * 生成列表的每个单元格数据
	 * 
	 * @param uxi 配置单元
	 * @return 每个单元格HTML
	 * @throws Exception
	 */
	private String createItemHtmlCell(UserXItem uxi) throws Exception {
		this.createCellParentStyle(uxi);

		IItem item = super.getHtmlClass().getItem(uxi);

		this._LastItem = item;

		this.setItemEwaConfigItemUnidPrefix(item);

		String parentHtml = super.createItemParentHtml(uxi); // 皮肤定义的页面样式

		boolean haveStyle = false;
		if (uxi.getParentStyle().trim().length() > 0) {
			haveStyle = true;
		}
		// 元素父窗体样式
		String style = "style=\"" + uxi.getParentStyle();

		if (uxi.getName().equals("*")) { // 不指定类型
			String[] ffs = uxi.getSingleValue("DataItem", "DataField").replace(" ", "").split(",");
			MStr sb = new MStr();
			for (int i = 0; i < ffs.length; i++) {
				Object val = this.getHtmlClass().getItemValues().getTableValue(ffs[i], "String");
				String v1 = val == null ? "" : val.toString();
				String tmp = "<div>" + v1 + "</div>";
				String s2 = parentHtml.replace(SkinFrame.TAG_ITEM, tmp);
				s2 = s2.replaceFirst("!!", Matcher.quoteReplacement(style) + "\" ");
				sb.al(s2);
			}
			return sb.toString();
		}

		String itemHtml = item.createItemHtml();
		itemHtml = super.removeAttrsByLogic(uxi, itemHtml);

		String tag = uxi.getSingleValue("Tag");
		if (this._IsLuButtons && (tag.equalsIgnoreCase("button") || tag.equalsIgnoreCase("submit")
				|| tag.equalsIgnoreCase("butFlow"))) {
			// 在页面上隐含
			style += "display: none;";
			haveStyle = true;
		}
		if (haveStyle) {
			style += "\" ";
		} else {
			style = "";
		}

		String s2 = parentHtml.replace(SkinFrame.TAG_ITEM, itemHtml);

		// 流程控制按钮
		if (tag.equalsIgnoreCase("butFlow")) {
			loadWorkFlowApp();
			String wf = this._WorkFlowBut.replace("[RID]", this.createItemKeys());
			s2 = s2.replace("{WF}", wf);
		}
		s2 = s2.replaceFirst("!!", style.replace("a:1;display:block;a:2", ""));
		if ((tag.equalsIgnoreCase("span") || tag.equalsIgnoreCase("linkButton")) && style.indexOf(";a:1;") > 0) {
			String v1 = item.getValue();
			if (v1 == null || v1.trim().length() == 0) {
				return s2;
			}
			String title = " title=\""
					+ v1.replace("<br>", "\n").replace("<br />", "\n").replace("\"", "&quot;").replace("<", "&lt;")
					+ "\" ";

			// 保留@符号
			title = title.replace("@", IItem.REP_AT_STR);

			if (s2.indexOf("title=\"") > 0) {
				title = "";
			} else {
				// Illegal group reference的解决办法
				title = java.util.regex.Matcher.quoteReplacement(title);
			}
			// s2 = s2.replace("!!", title + style);
			String style1 = style.replace("a:1;display:block;a:2;", "display:block;");

			if (tag.equalsIgnoreCase("span")) {
				if (s2.indexOf("><span ") > 0) {
					s2 = s2.replaceFirst("><span ", "><span " + title + style1);
				} else {
					s2 = s2.replaceFirst("><SPAN ", "><SPAN " + title + style1);
				}
			} else { // linkButton
				s2 = s2.replaceFirst("><a ", "><a " + title + style1 + " ");
			}
		} else {

		}
		return s2;
	}

	private int getUserSettingPageSize() {
		String pageSize = this.getPageItemValue("PageSize", "PageSize");
		int iPageSize = 10;
		try {
			iPageSize = Integer.parseInt(pageSize);
		} catch (Exception e) {

		}
		return iPageSize;
	}

	public String createaXmlData() throws Exception {
		MList tbs = super.getHtmlClass().getAction().getDTTables();

		if (tbs == null || tbs.size() == 0) {
			return "";
		} else {
			DTTable tb = (DTTable) tbs.get(tbs.size() - 1);
			return tb.toXml(super.getHtmlClass().getItemValues().getRequestValue());
		}

	}

	private String createSkinFCFist(String lang) {

		try {
			return this.getHtmlClass().getSkinFrameCurrent().getPageFirst().getDescriptions().getDescription(lang)
					.getInfo();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	private String createSkinFCLast(String lang) {
		try {
			return this.getHtmlClass().getSkinFrameCurrent().getPageLast().getDescriptions().getDescription(lang)
					.getInfo();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	private String createSkinFCNext(String lang) {
		try {
			return this.getHtmlClass().getSkinFrameCurrent().getPageNext().getDescriptions().getDescription(lang)
					.getInfo();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	private String createSkinFCPrev(String lang) {
		try {
			return this.getHtmlClass().getSkinFrameCurrent().getPagePrev().getDescriptions().getDescription(lang)
					.getInfo();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

}
