package com.gdxsoft.easyweb.script.display;

import java.util.HashMap;

import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.script.PageValueTag;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.frame.FrameParameters;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class SysParameters {
	private String _XmlName;
	private String _ItemName;

	private boolean _IsPagePost = false;
	private boolean _IsAjaxCall = false;
	private boolean _IsNoContent = false; // 不显示内容

	/**
	 * 提交后行为 EWA_P_BEHAVIOR
	 */
	private String _Behavior; //
	// 是否显示为 doctype XHTML EWA_XHTML = yes
	private boolean _IsXhtml = false;
	// 显示为 doctype H5， 参数 EWA_H5=1
	private boolean _IsH5 = true;
	// 显示为手机显示模式， 参数 EWA_MOBILE=1
	private boolean _IsShowAsMobile = false;

	/**
	 * XML为显示XML；HAVE_DATA显示是否有数据;BIN_FILE
	 */
	private String _AjaxCallType; // Ajax调用方式
	private String _Lang; // 字符集
	private String _Title;
	private String _FrameType;// 显示类型

	private String _FrameUnid;

	private String _ActionName;

	private RequestValue _RequestValue;
	private DataConnection _DataConn;

	private boolean _IsCached; // 是否缓存页面
	private int _CachedSeconds; // 缓存时间（S）
	private String _CahcedType; // 缓存类型
	private MTable _HiddenColumns;

	private int _TimeDiffMinutes; // 和系统当前时区差值(分钟)

	private boolean _IsPc = true;
	private boolean _IsIPhone = false;
	private boolean _IsAndroid = false;
	private boolean _IsWeixin = false;
	private boolean _IsMiniProgram = false;
	private boolean _IsVue = false;

	/**
	 * 和系统当前时区差值(分钟)
	 * 
	 * @return
	 */
	public int getTimeDiffMinutes() {
		return _TimeDiffMinutes;
	}

	/**
	 * 和系统当前时区差值(分钟)
	 * 
	 * @param minutes 时区差值(分钟)
	 */
	public void setTimeDiffMinutes(int minutes) {
		this._TimeDiffMinutes = minutes;
	}

	private HashMap<String, Object> _CacheAny;// 缓存任何对象，用于临时保存对象使用
	private boolean _IsIPad;

	public HashMap<String, Object> getCacheAny() {
		if (_CacheAny == null) {
			_CacheAny = new HashMap<String, Object>();
		}
		return _CacheAny;
	}

	/**
	 * 初始化所有参数
	 * 
	 * @param xmlName
	 * @param itemName
	 * @param rv
	 */
	public void initParas(String xmlName, String itemName, RequestValue rv) {

		this._RequestValue = rv;

		this.initXml(xmlName, itemName, rv);
		this.initLang(rv);
		// 初始化客户端平台参数
		this.initPlateform();
		this.initCallType(rv);
		this.initAction(rv);

		// 初始化用户和系统当前时区差值(分钟)
		this.initTimeDiffMinitus(rv);

		// 是否为XHTML输出
		String isXHTML = rv.getString(FrameParameters.EWA_XHTML);
		if (isXHTML != null && isXHTML.equalsIgnoreCase("yes")) {
			this._IsXhtml = true;
		} else {
			this._IsXhtml = false;
		}
		
		// Default is h5
		String isH5 = rv.getString(FrameParameters.EWA_H5);
		if ("no".equalsIgnoreCase(isH5)) {
			this._IsH5 = false;
		}
		
		// 输出为手机显示模式， 参数 EWA_MOBILE=1
		if (rv.s(FrameParameters.EWA_MOBILE) != null) {
			this._IsShowAsMobile = true;
		}

		// 输出为VUE格式
		if (rv.s(FrameParameters.EWA_VUE) != null) {
			this._IsVue = true;
		}
	}

	/**
	 * 初始化客户端平台参数
	 */
	private void initPlateform() {
		if (this._RequestValue == null) {
			return;
		}
		if (this._RequestValue.s("SYS_USER_AGENT") == null) {
			return;
		}
		String ua = this._RequestValue.s("SYS_USER_AGENT").toLowerCase();
		this._IsWeixin = ua.indexOf("micromessenger") > 0;
		this._IsAndroid = ua.indexOf("android") > 0;
		this._IsIPhone = ua.indexOf("iphone") > 0;
		this._IsIPad = ua.indexOf("ipad") > 0;
		this._IsMiniProgram = ua.indexOf("miniprogram") > 0;

		if (_IsAndroid || _IsIPhone || _IsIPad) {
			this._IsPc = false;
		}
	}

	private void initAction(RequestValue rv) {
		// 事件调用名称
		String actionType = rv.getString(FrameParameters.EWA_ACTION);
		if (actionType == null || actionType.length() == 0) {
			String typeName = "OnPageLoad";
			if (isPagePost()) {
				typeName = "OnPagePost";
			}
			actionType = typeName;
		}
		this.setActionName(actionType);
	}

	private void initXml(String xmlName, String itemName, RequestValue rv) {

		setXmlName(xmlName);
		setItemName(itemName);

		// Frame的唯一编号
		String frameUnid = xmlName + "&&&GDX1231&&&" + itemName;
		int iFrameUnid = frameUnid.hashCode();
		frameUnid = (iFrameUnid + "").replace("-", "G");

		if (rv.s(FrameParameters.SYS_FRAME_UNID) != null) {
			rv.getPageValues().remove(FrameParameters.SYS_FRAME_UNID);
		}
		rv.addValue("SYS_FRAME_UNID", frameUnid);
		setFrameUnid(frameUnid);
	}

	private void initCallType(RequestValue rv) {
		// 是否Ajax调用
		if (rv.getString(FrameParameters.EWA_AJAX) != null) {
			// Ajax调用方式 XML为显示XML；
			// HAVE_DATA显示是否有数据;
			// BIN_FILE 则表示为二进制文件
			setIsAjaxCall(true);
			setAjaxCallType(rv.getString(FrameParameters.EWA_AJAX));
		}

		// 是否post提交
		if (rv.getString(FrameParameters.EWA_POST) != null) {
			setIsPagePost(true);
		}

		// 不显示内容
		String noContent = rv.getString(FrameParameters.EWA_NO_CONTENT);
		if (noContent == null || !noContent.equals("1")) {
			setIsNoContent(false);
		} else {
			setIsNoContent(true);
		}
		// 提交后行为 EWA_P_BEHAVIOR
		_Behavior = rv.getString(FrameParameters.EWA_P_BEHAVIOR);
	}

	/**
	 * 初始化用户和系统当前时区差值(分钟)
	 * 
	 * @param rv
	 */
	private void initTimeDiffMinitus(RequestValue rv) {
		if (rv == null || rv.s(FrameParameters.EWA_TIMEDIFF) == null || rv.s(FrameParameters.EWA_TIMEDIFF).trim().length() == 0) {
			return;
		}
		try {
			int diff = rv.getInt(FrameParameters.EWA_TIMEDIFF);
			this._TimeDiffMinutes = diff;
		} catch (Exception err) {
			System.out.println(this);
			System.out.println(err.getMessage());
		}
	}

	/**
	 * 字符集
	 * 
	 * @param rv
	 */
	private void initLang(RequestValue rv) {
		// 字符集
		String lang = rv.getLang();
		rv.addValue(FrameParameters.EWA_LANG, lang);
		if (rv.getSession() != null) {// 保存到Session
			rv.getSession().setAttribute(FrameParameters.SYS_EWA_LANG, lang);

			// 写入 rv的 SYS_EWA_LANG guolei 2017-04-27
			rv.getPageValues().getSessionValues().removeKey(FrameParameters.SYS_EWA_LANG);
			rv.getPageValues().addValue("SYS_EWA_LANG", lang, PageValueTag.SESSION);
		} else {
			// 写入 rv的 SYS_EWA_LANG guolei 2017-04-27

			rv.getPageValues().remove(FrameParameters.SYS_EWA_LANG);
			rv.addValue("SYS_EWA_LANG", lang);
		}

		setLang(lang);
	}

	/**
	 * @return the _IsPagePost
	 */
	public boolean isPagePost() {
		return _IsPagePost;
	}

	/**
	 * @param isPagePost the _IsPagePost to set
	 */
	public void setIsPagePost(boolean isPagePost) {
		_IsPagePost = isPagePost;
	}

	/**
	 * @return the _IsAjaxCall
	 */
	public boolean isAjaxCall() {
		return _IsAjaxCall;
	}

	/**
	 * @param isAjaxCall the _IsAjaxCall to set
	 */
	public void setIsAjaxCall(boolean isAjaxCall) {
		_IsAjaxCall = isAjaxCall;
	}

	/**
	 * 是否未不显示内容
	 * 
	 * @return the _IsNoContent
	 */
	public boolean isNoContent() {
		return _IsNoContent;
	}

	/**
	 * @param isNoContent the _IsNoContent to set
	 */
	public void setIsNoContent(boolean isNoContent) {
		_IsNoContent = isNoContent;
	}

	/**
	 * @return the _AjaxCallType
	 */
	public String getAjaxCallType() {
		return _AjaxCallType;
	}

	/**
	 * @param ajaxCallType the _AjaxCallType to set
	 */
	public void setAjaxCallType(String ajaxCallType) {
		_AjaxCallType = ajaxCallType;
	}

	/**
	 * @return the _Lang
	 */
	public String getLang() {
		return _Lang;
	}

	/**
	 * @param lang the _Lang to set
	 */
	public void setLang(String lang) {
		_Lang = lang;
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
	 * @return the _FrameType
	 */
	public String getFrameType() {
		return _FrameType;
	}

	/**
	 * @param frameType the _FrameType to set
	 */
	public void setFrameType(String frameType) {
		_FrameType = frameType;
	}

	/**
	 * @return the _Behavior
	 */
	public String getBehavior() {
		return _Behavior;
	}

	/**
	 * @param behavior the _Behavior to set
	 */
	public void setBehavior(String behavior) {
		_Behavior = behavior;
	}

	/**
	 * @return the _IsXhtml
	 */
	public boolean isXhtml() {
		return _IsXhtml;
	}

	/**
	 * @param isXhtml the _IsXhtml to set
	 */
	public void setIsXhtml(boolean isXhtml) {
		_IsXhtml = isXhtml;
	}

	/**
	 * @return the _XmlName
	 */
	public String getXmlName() {
		return _XmlName;
	}

	/**
	 * @param xmlName the _XmlName to set
	 */
	public void setXmlName(String xmlName) {
		_XmlName = xmlName;
	}

	/**
	 * @return the _ItemName
	 */
	public String getItemName() {
		return _ItemName;
	}

	/**
	 * @param itemName the _ItemName to set
	 */
	public void setItemName(String itemName) {
		_ItemName = itemName;
	}

	/**
	 * @return the _FrameUnid
	 */
	public String getFrameUnid() {
		return _FrameUnid;
	}

	/**
	 * @param frameUnid the _FrameUnid to set
	 */
	public void setFrameUnid(String frameUnid) {
		if (frameUnid.equals("2144533480")) {
			System.out.println(this.getXmlName());
			System.out.println(this.getItemName());
		}
		_FrameUnid = frameUnid;
	}

	/**
	 * @return the _ActionName
	 */
	public String getActionName() {
		return _ActionName;
	}

	/**
	 * @param actionName the _ActionName to set
	 */
	public void setActionName(String actionName) {
		_ActionName = actionName;
	}

	/**
	 * @return the _RequestValue
	 */
	public RequestValue getRequestValue() {
		return _RequestValue;
	}

	/**
	 * @param requestValue the _RequestValue to set
	 */
	public void setRequestValue(RequestValue requestValue) {
		_RequestValue = requestValue;
	}

	/**
	 * @return the _DataConn
	 */
	public DataConnection getDataConn() {
		return _DataConn;
	}

	/**
	 * @param dataConn the _DataConn to set
	 */
	public void setDataConn(DataConnection dataConn) {
		_DataConn = dataConn;
	}

	/**
	 * 是否缓存
	 * 
	 * @return the _IsCached
	 */
	public boolean isCached() {
		return _IsCached;
	}

	/**
	 * 是否缓存
	 * 
	 * @param isCached the _IsCached to set
	 */
	public void setIsCached(boolean isCached) {
		_IsCached = isCached;
	}

	/**
	 * 缓存时间
	 * 
	 * @return the _CachedSeconds
	 */
	public int getCachedSeconds() {
		return _CachedSeconds;
	}

	/**
	 * 缓存时间
	 * 
	 * @param cachedSeconds the _CachedSeconds to set
	 */
	public void setCachedSeconds(int cachedSeconds) {
		_CachedSeconds = cachedSeconds;
	}

	public String getCachedType() {
		return _CahcedType;
	}

	public void setCachedType(String cahcedType) {
		_CahcedType = cahcedType;
	}

	/**
	 * 获取隐含的列
	 * 
	 * @return the _HiddenColumns
	 */
	public MTable getHiddenColumns() {
		return _HiddenColumns;
	}

	/**
	 * 需要隐含的列
	 * 
	 * @param hiddenColumns the _HiddenColumns to set
	 */
	public void setHiddenColumns(String[] hiddenColumns) {
		this._HiddenColumns = new MTable();
		for (int i = 0; i < hiddenColumns.length; i++) {
			String s = hiddenColumns[i].trim().toUpperCase();
			this._HiddenColumns.add(s, true);
		}
	}

	/**
	 * 检查是否是隐藏的列，即在页面上不显示
	 * 
	 * @param colName 列名
	 * @return
	 */
	public boolean isHiddenColumn(String colName) {
		if (this._HiddenColumns == null || this._HiddenColumns.size() == 0) {
			return false;
		}
		if (colName == null || colName.trim().length() == 0) {
			return false;
		}
		String n = colName.trim().toUpperCase();
		return this._HiddenColumns.containsKey(n);
	}

	/**
	 * 是否为pc调用
	 * 
	 * @return the _IsPc
	 */
	public boolean isPc() {
		return _IsPc;
	}

	/**
	 * 是否为移动设备调用 iphone/ipand/android
	 * 
	 * @return
	 */
	public boolean isMobile() {
		return !this._IsPc;
	}

	/**
	 * 客户端是否为 iPhone
	 * 
	 * @return the _IsIPhone
	 */
	public boolean isIPhone() {
		return _IsIPhone;
	}

	/**
	 * 客户端是否为 android
	 * 
	 * @return the _IsAndroid
	 */
	public boolean isAndroid() {
		return _IsAndroid;
	}

	/**
	 * 客户端是否在微信里
	 * 
	 * @return the _IsWeixin
	 */
	public boolean isWeixin() {
		return _IsWeixin;
	}

	/**
	 * 客户端是否为小程序
	 * 
	 * @return the _IsMiniProgram
	 */
	public boolean isMiniProgram() {
		return _IsMiniProgram;
	}

	/**
	 * 客户端是否是 iPad
	 * 
	 * @return the _IsIPad
	 */
	public boolean isIPad() {
		return _IsIPad;
	}

	/**
	 * 显示为手机显示模式 参数 EWA_MOBILE=1
	 * 
	 * @return the _IsShowAsMobile
	 */
	public boolean isShowAsMobile() {
		return _IsShowAsMobile;
	}

	/**
	 * 显示为手机显示模式 参数 EWA_MOBILE=1
	 * 
	 * @param showAsMobile the showAsMobile to set
	 */
	public void setShowAsMobile(boolean showAsMobile) {
		this._IsShowAsMobile = showAsMobile;
	}

	/**
	 * 显示为 doctype H5， 参数 EWA_H5=1
	 * 
	 * @return the _IsH5
	 */
	public boolean isH5() {
		return _IsH5;
	}

	/**
	 * 显示为 doctype H5， 参数 EWA_H5=1
	 * 
	 * @param _IsH5 the _IsH5 to set
	 */
	public void setH5(boolean isH5) {
		this._IsH5 = isH5;
	}

	/**
	 * 是否显示为 VUE， 参数 EWA_VUE=1
	 * 
	 * @return the _IsVue
	 */
	public boolean isVue() {
		return _IsVue;
	}

	/**
	 * 是否显示为 VUE
	 * 
	 * @param _IsVue the _IsVue to set
	 */
	public void setVue(boolean vue) {
		this._IsVue = vue;
	}

}
