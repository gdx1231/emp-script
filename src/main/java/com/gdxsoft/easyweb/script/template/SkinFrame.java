package com.gdxsoft.easyweb.script.template;

import java.io.Serializable;

public class SkinFrame implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6573436592904386030L;
	public final static String TAG_ITEM = "{__EWA_ITEM__}";
	public final static String TAG_DES = "{__EWA_DES__}"; // 描述替换符号
	public final static String TAG_NAME = "{__EWA_NAME__}"; // ID 替换符号
	public final static String TAG_MSG = "{__EWA_MSG__}"; // 信息替换符号
	public final static String TAG_VAL = "{__EWA_VAL__}"; // 值替换符号
	public final static String TAG_LST_VAL = "{__EWA_LST_VAL__}"; // 列表值替换符号
	public final static String TAG_LST_TXT = "{__EWA_LST_TXT__}"; // 列表描述替换符号
	public final static String TAG_LST_IDS = "{__EWA_LST_IDS__}"; // 用于LABEL的
	public final static String TAG_LST_TITLE = "{__EWA_LST_TITLE__}"; // 用于TITLE
	
	// ID的编号
	public final static String TAG_REP = "{__EWA_REP__}"; // 内容替换

	public final static String TAG_LF_CURPAGE = "{__EWA_LF_CURPAGE__}"; // 当前页码
	public final static String TAG_LF_PAGECOUNT = "{__EWA_LF_PAGECOUNT__}"; // 页数
	public final static String TAG_LF_PAGESIZE = "{__EWA_LF_PAGESIZE__}"; // 页面记录数
	public final static String TAG_LF_RECORDCOUNT = "{__EWA_LF_RECORDCOUNT__}"; // 总记录数字

	public final static String TAG_LF_FIRST = "{__EWA_LF_FIRST__}"; // 首页
	public final static String TAG_LF_NEXT = "{__EWA_LF_NEXT__}"; // 下一页
	public final static String TAG_LF_PREV = "{__EWA_LF_PREV__}"; // 上一页
	public final static String TAG_LF_LAST = "{__EWA_LF_LAST__}"; // 末页

	private String _BodyStart;
	private String _BodyEnd;
	
	private String _FrameType = "";
	private String _Top = "";
	private String _Bottom = "";
	private String _Item = "";
	private String _ItemButton = "";
	private String _ItemHeader = "";
	private Descriptions _ItemFooter;
	private String _Style = "";
	private String _Script = "";

	private SkinFrameLang _PageFirst;
	private SkinFrameLang _PageNext;
	private SkinFrameLang _PagePrev;
	private SkinFrameLang _PageLast;

	/**
	 * @return the _Top
	 */
	public String getTop() {
		return _Top == null ? "" : _Top;
	}

	/**
	 * @param top
	 *            the _Top to set
	 */
	public void setTop(String top) {
		_Top = top;
	}

	/**
	 * @return the _Bottom
	 */
	public String getBottom() {
		return _Bottom == null ? "" : _Bottom;
	}

	/**
	 * @param bottom
	 *            the _Bottom to set
	 */
	public void setBottom(String bottom) {
		_Bottom = bottom;
	}

	/**
	 * @return the _Item
	 */
	public String getItem() {
		return _Item == null ? "" : _Item;
	}

	/**
	 * @param item
	 *            the _Item to set
	 */
	public void setItem(String item) {
		_Item = item;
	}

	/**
	 * @return the _ItemButton
	 */
	public String getItemButton() {
		return _ItemButton == null ? "" : _ItemButton;
	}

	/**
	 * @param itemButton
	 *            the _ItemButton to set
	 */
	public void setItemButton(String itemButton) {
		_ItemButton = itemButton;
	}

	/**
	 * @return the _FrameType
	 */
	public String getFrameType() {
		return _FrameType;
	}

	/**
	 * @param frameType
	 *            the _FrameType to set
	 */
	public void setFrameType(String frameType) {
		_FrameType = frameType;
	}

	/**
	 * @return the _Style
	 */
	public String getStyle() {
		return _Style == null ? "" : _Style;
	}

	/**
	 * @param style
	 *            the _Style to set
	 */
	public void setStyle(String style) {
		_Style = style;
	}

	/**
	 * @return the _Script
	 */
	public String getScript() {
		return _Script == null ? "" : _Script;
	}

	/**
	 * @param script
	 *            the _Script to set
	 */
	public void setScript(String script) {
		_Script = script;
	}

	/**
	 * @return the _ItemHeader
	 */
	public String getItemHeader() {
		return _ItemHeader == null ? "" : _ItemHeader;
	}

	/**
	 * @param itemHeader
	 *            the _ItemHeader to set
	 */
	public void setItemHeader(String itemHeader) {
		_ItemHeader = itemHeader;
	}

	/**
	 * @return the _ItemFooter
	 */
	public Descriptions getItemFooter() {
		return _ItemFooter ;
	}

	/**
	 * @param itemFooter
	 *            the _ItemFooter to set
	 */
	public void setItemFooter(Descriptions itemFooter) {
		_ItemFooter = itemFooter;
	}

	/**
	 * @return the _PageFirst
	 */
	public SkinFrameLang getPageFirst() {
		return _PageFirst;
	}

	/**
	 * @param pageFirst
	 *            the _PageFirst to set
	 */
	public void setPageFirst(SkinFrameLang pageFirst) {
		_PageFirst = pageFirst;
	}

	/**
	 * @return the _PageNext
	 */
	public SkinFrameLang getPageNext() {
		return _PageNext;
	}

	/**
	 * @param pageNext
	 *            the _PageNext to set
	 */
	public void setPageNext(SkinFrameLang pageNext) {
		_PageNext = pageNext;
	}

	/**
	 * @return the _PagePrev
	 */
	public SkinFrameLang getPagePrev() {
		return _PagePrev;
	}

	/**
	 * @param pagePrev
	 *            the _PagePrev to set
	 */
	public void setPagePrev(SkinFrameLang pagePrev) {
		_PagePrev = pagePrev;
	}

	/**
	 * @return the _PageLast
	 */
	public SkinFrameLang getPageLast() {
		return _PageLast;
	}

	/**
	 * @param pageLast
	 *            the _PageLast to set
	 */
	public void setPageLast(SkinFrameLang pageLast) {
		_PageLast = pageLast;
	}

	/**
	 * @return the _BodyStart
	 */
	public String getBodyStart() {
		return _BodyStart;
	}

	/**
	 * @param bodyStart the _BodyStart to set
	 */
	public void setBodyStart(String bodyStart) {
		_BodyStart = bodyStart;
	}

	/**
	 * @return the _BodyEnd
	 */
	public String getBodyEnd() {
		return _BodyEnd;
	}

	/**
	 * @param bodyEnd the _BodyEnd to set
	 */
	public void setBodyEnd(String bodyEnd) {
		_BodyEnd = bodyEnd;
	}


}
