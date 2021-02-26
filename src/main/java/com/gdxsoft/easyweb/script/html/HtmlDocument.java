package com.gdxsoft.easyweb.script.html;

import java.util.HashMap;

import com.gdxsoft.easyweb.script.display.HtmlClass;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class HtmlDocument {

	private HtmlClass _htmlClass;

	private HtmlDoctype _Doctype;
	private MStr _Head;
	private String _Title;
	private HtmlScripts _JsTop;
	private HtmlScripts _JsBottom;
	private MStr _Css;
	private MStr _BodyTop;
	private MStr _BodyBottom;
	private MStr _ScriptHtml;
	private MStr _FrameHtml; // 内容本身

	private HashMap<String, String> _Items;

	public HtmlDocument() {
		_Items = new HashMap<String, String>();

		this.init();
	}

	/**
	 * 合成的frame 的html 没有外部例如 <div id='EWA_FRAME_MAIN ' or <table id='Test1' ...
	 * 
	 * @return html
	 */
	public String getFrameHtml() {
		String cnt = this._FrameHtml.toString();
		if (cnt.trim().length() == 0) {
			cnt = this._ScriptHtml.toString();
		}
		return cnt;
	}

	public String showHeader() {
		MStr sb = new MStr();

		if(this._htmlClass.getSysParas().isVue() ) {
			// 显示为 VUE
			sb.al(this._htmlClass.getSkin().getHeadVue());
			
		} else if (this._htmlClass.getSysParas().isShowAsMobile()) {
			// 移动模式下
			sb.al(this._htmlClass.getSkin().getHeadMobile());
		} else if (this._htmlClass.getSysParas().isH5()) {
			// 移动模式下
			sb.al(this._htmlClass.getSkin().getHeadH5());
		} else if (this.isXhtml()) {
			sb.al(this._htmlClass.getSkin().getHeadXHtml());
		} else {
			sb.al(this._htmlClass.getSkin().getHead());
		}

		// docType
//		sb.al(this._Doctype.getDoctype());
//		sb.al("<head>");
//		sb.al("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");

		sb.al(_Head.toString());
		sb.al("<title>" + this._Title + "</title>");
		sb.al("</head>");
		return sb.toString();
	}

	public String showBody() {
		MStr sb = new MStr();
		// sb.appendLine(this._BodyTop.toString());
		if (this._htmlClass.getSysParas().isShowAsMobile()) {
			sb.al(this._htmlClass.getSkin().getBodyStartMobile());
		} else {
			sb.al(this._htmlClass.getSkin().getBodyStart());
		}
		String css = _Css.toString();
		if (css.trim().length() > 0) {
			sb.appendLine("<style type='text/css'>");
			sb.append(css);
			sb.appendLine("</style>");
		}
		sb.appendLine(this._JsTop.getScripts(true));
		sb.appendLine(this._ScriptHtml.toString());
		// js bottom
		sb.appendLine(this._JsBottom.getScripts(true));
		sb.append(this._BodyBottom.toString());

		if (this._htmlClass.getSysParas().isShowAsMobile()) {
			sb.al(this._htmlClass.getSkin().getBodyEndMobile());
		} else {
			sb.al(this._htmlClass.getSkin().getBodyEnd());
		}
		return sb.toString();
	}

	/**
	 * 获取页面含包含部分内容<br>
	 * [0]对应<!--INC_TOP-->前面的部分<br>
	 * [1]对应<!--INC_TOP-->到<!--INC_END-->中间的部分<br>
	 * [0]对应<!--INC_END-->后面的部分
	 * 
	 * @return
	 */
	public String[] showParts() {
		String shtml = this.showAll();
		int topLoc = shtml.indexOf("<!--INC_TOP-->");
		String[] parts = new String[3];
		if (topLoc > 0) {
			String topHtml = shtml.substring(0, topLoc);
			parts[0] = topHtml;
			int bottomLoc = shtml.indexOf("<!--INC_END-->");
			if (bottomLoc > 0 && bottomLoc > topLoc) {
				String mid = shtml.substring(topLoc, bottomLoc);
				parts[1] = mid;
				String bottom = shtml.substring(bottomLoc);
				parts[2] = bottom;
			} else {
				parts[1] = shtml.substring(topLoc);
				parts[2] = "";
			}
		} else {
			parts[0] = shtml;
			parts[1] = parts[2] = "";
		}
		return parts;
	}

	public String showAll() {
		MStr sb = new MStr();
		sb.appendLine(this.showHeader());
		sb.appendLine(this.showBody());

		return sb.toString();
	}

	/**
	 * 根据isShowScriptTag 显示脚本<br>
	 * isShowScriptTag=<b>true</b>，显示 script 标签
	 * 
	 * @param isShowScriptTag 是否显示script 标签
	 * @return
	 */
	public String showJs(boolean isShowScriptTag) {
		MStr sb = new MStr();
		sb.appendLine(this._JsTop.getScripts(isShowScriptTag));
		sb.appendLine(this._JsBottom.getScripts(isShowScriptTag));

		return sb.toString();
	}

	/**
	 * 合成的frame 的html 没有外部例如 <div id='EWA_FRAME_MAIN ' or <table id='Test1' ...
	 * 
	 * @param html
	 */
	public void addFrameHtml(String html) {
		this._FrameHtml.a(html);
	}

	public void addJs(String id, String script, boolean isTop) {
		if (isTop) {
			this._JsTop.addScript(id, script);
		} else {
			this._JsBottom.addScript(id, script);
		}
	}

	public void addBodyHtml(String html, boolean isTop) {
		if (isTop) {
			this._BodyTop.appendLine(html);
		} else {
			this._BodyBottom.appendLine(html);
		}
	}

	public void addScriptHtml(String html) {
		addScriptHtml(html, null);
	}

	public void addScriptHtml(String html, String memo) {
		if (memo != null) {
			_ScriptHtml.appendLine("<!-- Start: " + memo + "-->");
		}
		_ScriptHtml.appendLine(html.trim());
		if (memo != null) {
			_ScriptHtml.appendLine("<!-- End: " + memo + "-->");
		}

	}

	/**
	 * 增加Header
	 * 
	 * @param header
	 */
	public void addHeader(String header) {
		this._Head.appendLine(header);
	}

	/**
	 * 增加css
	 * 
	 * @param css
	 */
	public void addCss(String css) {
		this._Css.appendLine(css);
	}

	public void setIsXhtml(boolean isXhtml) {
		this._Doctype.setIsXhtml(isXhtml);
	}

	public boolean isXhtml() {
		return this._Doctype.isXhtml();
	}

	void init() {
		_Doctype = new HtmlDoctype();
		_JsTop = new HtmlScripts();
		_JsBottom = new HtmlScripts();
		_BodyTop = new MStr();
		_BodyBottom = new MStr();
		_ScriptHtml = new MStr();

		_Css = new MStr();

		_Head = new MStr();
		// 2016-04-15 郭磊 为了 HtmlControl 的 getFrameMin
		this._FrameHtml = new MStr();
	}

	/**
	 * @return the _Title
	 */
	public String getTitle() {
		return _Title;
	}

	/**
	 * @param title the _Title to set
	 */
	public void setTitle(String title) {
		_Title = title;
	}

	/**
	 * @return the _Doctype
	 */
	public HtmlDoctype getDoctype() {
		return _Doctype;
	}

	/**
	 * @return the _Head
	 */
	public MStr getHead() {
		return _Head;
	}

	/**
	 * @return the _JsTop
	 */
	public HtmlScripts getJsTop() {
		return _JsTop;
	}

	/**
	 * @return the _JsBottom
	 */
	public HtmlScripts getJsBottom() {
		return _JsBottom;
	}

	/**
	 * @return the _Css
	 */
	public MStr getCss() {
		return _Css;
	}

	/**
	 * @return the _BodyTop
	 */
	public MStr getBodyTop() {
		return _BodyTop;
	}

	/**
	 * @return the _BodyBottom
	 */
	public MStr getBodyBottom() {
		return _BodyBottom;
	}

	/**
	 * @return the _ScriptHtml
	 */
	public MStr getScriptHtml() {
		return _ScriptHtml;
	}

	/**
	 * @return the _htmlClass
	 */
	public HtmlClass getHtmlClass() {
		return _htmlClass;
	}

	/**
	 * @param _htmlClass the _htmlClass to set
	 */
	public void setHtmlClass(HtmlClass htmlClass) {
		this._htmlClass = htmlClass;
	}

	/**
	 * 
	 * @return
	 */
	public HashMap<String, String> getItems() {
		return _Items;
	}
}
