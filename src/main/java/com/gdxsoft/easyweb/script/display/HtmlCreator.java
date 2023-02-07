package com.gdxsoft.easyweb.script.display;

import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Type;
import java.math.BigDecimal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.acl.IAcl;
import com.gdxsoft.easyweb.acl.IAcl2;
import com.gdxsoft.easyweb.cache.CacheEwaScript;
import com.gdxsoft.easyweb.cache.CacheLoadResult;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.debug.DebugFrames;
import com.gdxsoft.easyweb.global.EwaGlobals;
import com.gdxsoft.easyweb.log.ILog;
import com.gdxsoft.easyweb.log.Log;
import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.PageValueTag;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.ShortName;
import com.gdxsoft.easyweb.script.ShortNames;
import com.gdxsoft.easyweb.script.ValidCode;
import com.gdxsoft.easyweb.script.ValidCode1;
import com.gdxsoft.easyweb.script.Workflow.WfRst;
import com.gdxsoft.easyweb.script.Workflow.WfUnits;
import com.gdxsoft.easyweb.script.display.action.IAction;
import com.gdxsoft.easyweb.script.display.frame.FrameList;
import com.gdxsoft.easyweb.script.display.frame.FrameParameters;
import com.gdxsoft.easyweb.script.display.frame.IFrame;
import com.gdxsoft.easyweb.script.display.items.IItem;
import com.gdxsoft.easyweb.script.html.HtmlDocument;
import com.gdxsoft.easyweb.script.messageQueue.MsgQueueManager;
import com.gdxsoft.easyweb.script.template.Skin;
import com.gdxsoft.easyweb.script.template.SkinFrame;
import com.gdxsoft.easyweb.script.template.SkinFrames;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValues;
import com.gdxsoft.easyweb.script.userConfig.UserXItems;
import com.gdxsoft.easyweb.utils.UConvert;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UJSon;
import com.gdxsoft.easyweb.utils.ULogic;
import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MList;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class HtmlCreator {
	private static Logger LOGGER = LoggerFactory.getLogger(HtmlCreator.class);
	private EwaGlobals _EwaGlobals;
	private ItemValues _ItemValues;

	private DebugFrames _DebugFrames;
	private IAcl _Acl;
	private IAcl2 _Acl2;
	private UserConfig _UserConfig;
	private RequestValue _RequestValue;
	private Object _UserClass;
	private UObjectValue _UObjectValue;
	private MList _Tables;

	private HttpServletResponse _Response;

	private HtmlClass _HtmlClass;

	public HtmlClass getHtmlClass() {
		return _HtmlClass;
	}

	private IFrame _Frame;

	public IFrame getFrame() {
		return _Frame;
	}

	private IAction _Action;
	private WfUnits _Workflow;
	private Log _Log; // 日志类
	private SysParameters _SysParas; // 系统参数

	/**
	 * 系统参数
	 * 
	 * @return
	 */
	public SysParameters getSysParas() {
		return _SysParas;
	}

	private HtmlDocument _Document;

	private String _PageHtml;
	private String _ActionReturnValue; // Action执行返回的标志, "SCRIPT", "URL", ""
	private boolean _ErrOut; // 系统执行出现 errout;
	private String errOutMessage; // 系统执行出现 errout 的内容;

	private BufferedImage validCode;

	public HtmlCreator() {
		_DebugFrames = new DebugFrames();
		_SysParas = new SysParameters();
		_Tables = new MList();

		_Document = new HtmlDocument();

	}

	public void init(HttpServletRequest req, HttpSession session, HttpServletResponse response) throws Exception {
		this._RequestValue = new RequestValue(req, session);
		this._Response = response;
		String shortName = this._RequestValue.getString(FrameParameters.EWA_SN);
		if (shortName == null) {
			shortName = this._RequestValue.getString(FrameParameters.$S);
		}
		String xmlName = null;
		String itemName = null;
		if (shortName == null) {
			xmlName = this._RequestValue.getString(FrameParameters.XMLNAME);
			itemName = this._RequestValue.getString(FrameParameters.ITEMNAME);
		} else { // ShortName定义
			ShortName sn = ShortNames.Instance().getShortName(shortName);
			if (sn != null) {
				xmlName = sn.getXmlName();
				itemName = sn.getItemName();
				if (sn.getParameters() != null) { // 添加ShortName定义的参数
					String[] ps = sn.getParameters().split("\\&");
					for (int i = 0; i < ps.length; i++) {
						String[] pp = ps[i].split("=");
						if (pp.length == 0 || pp.length > 2) {
							continue;
						}
						String key = pp[0].trim();
						if (key.length() == 0) {
							continue;
						}
						this._RequestValue.addValue(key, pp[1]);
					}
				}
				if (sn.getHiddens() != null) {
					this._SysParas.setHiddenColumns(sn.getHiddens().split(","));
				}
			} else {
				throw new Exception("Short Name [" + shortName + "] 未定义");
			}
		}
		initParameters(xmlName, itemName);
	}

	/**
	 * 初始化HtmlCreator
	 * 
	 * @param xmlName  配置文件
	 * @param itemName 配置项
	 * @param paras    参数，用“&”分割，放到RV的OTHER中
	 * @param req      request
	 * @param session  session
	 * @param response response
	 * @throws Exception
	 */
	public void init(String xmlName, String itemName, String paras, HttpServletRequest req, HttpSession session,
			HttpServletResponse response) throws Exception {
		this._RequestValue = new RequestValue(req, session);
		this._Response = response;

		_RequestValue.addValue(FrameParameters.XMLNAME, xmlName);
		_RequestValue.addValue(FrameParameters.ITEMNAME, itemName);

		this.attachParas(paras);

		initParameters(xmlName, itemName);
	}

	/**
	 * 初始化 HtmlCreator
	 * 
	 * @param xmlName  配置文件
	 * @param itemName 配置项
	 * @param paras    参数，用“&”分割，放到RV的OTHER中，如果有特殊字符?&=等，需要用UrlEncode编码
	 * @param rv
	 * @param response
	 * @throws Exception
	 */
	public void init(String xmlName, String itemName, String paras, RequestValue rv, HttpServletResponse response)
			throws Exception {
		this._Response = response;
		this._RequestValue = rv;

		rv.getPageValues().remove(FrameParameters.XMLNAME);
		rv.getPageValues().remove(FrameParameters.ITEMNAME);

		// rv.getPageValues().remove("SYS_UNID");

		_RequestValue.addValue(FrameParameters.XMLNAME, xmlName);
		_RequestValue.addValue(FrameParameters.ITEMNAME, itemName);

		this.attachParas(paras);
		initParameters(xmlName, itemName);
	}

	/**
	 * 初始化的参数放到RV中
	 * 
	 * @param paras
	 */
	private void attachParas(String paras) {
		this._RequestValue.addValues(paras, PageValueTag.HTML_CONTROL_PARAS);
	}

	public void init(RequestValue requestValue, HttpServletResponse response) throws Exception {
		this._RequestValue = requestValue;
		this._Response = response;
		String xmlName = this._RequestValue.getString(FrameParameters.XMLNAME);
		String itemName = this._RequestValue.getString(FrameParameters.ITEMNAME);

		initParameters(xmlName, itemName);
	}

	public void init(String xmlName, String itemName) throws Exception {
		this.initParameters(xmlName, itemName);
	}

	public String getConfigItemXml() {
		return this._UserConfig.getItemNodeXml();
	}

	/**
	 * 初始化所有参数
	 * 
	 * @param xmlName
	 * @param itemName
	 * @throws Exception
	 */
	private void initParameters(String xmlName, String itemName) throws Exception {
		this._EwaGlobals = EwaGlobals.instance();

		_DebugFrames.addDebug(this, "INIT", "开始读取初始化数据");

		if (xmlName == null || xmlName.trim().length() == 0) {
			throw new Exception("XMLNAME 未定义");
		}
		if (itemName == null || itemName.trim().length() == 0) {
			throw new Exception("ITEMNAME 未定义");
		}

		if (xmlName.indexOf("<") >= 0) {
			throw new Exception("XMLNAME 非法字符");
		}
		if (itemName.indexOf("<") >= 0) {
			throw new Exception("ITEMNAME 非法字符");
		}
		xmlName = xmlName.replace("#", "");
		if (xmlName.toUpperCase().indexOf("%7C") >= 0) { // for apple
			xmlName = xmlName.replace("%7C", "|").replace("%7c", "|");
		}
		itemName = itemName.replace("#", "");

		// 提交参数
		if (this._RequestValue == null) {
			this._RequestValue = new RequestValue();
		}

		// 初始化参数，大部分内容
		this._SysParas.initParas(xmlName, itemName, _RequestValue);

		// 系统日志
		this._Log = new Log();
		this._Log.setItemName(itemName);
		this._Log.setXmlName(xmlName);
		this._Log.setIp(this._RequestValue.s(RequestValue.SYS_REMOTEIP));
		this._Log.setUrl(this._RequestValue.s(RequestValue.SYS_REMOTE_URL));
		this._Log.setRefererUrl(this._RequestValue.s(RequestValue.SYS_REMOTE_REFERER));
		this._Log.setRequestValue(_RequestValue);

		_DebugFrames.addDebug(this, "INIT", "开始读取用户模板 XMLNAME=" + xmlName + ",ITEMNAME=" + itemName);
		this._UserConfig = UserConfig.instance(xmlName, itemName, _DebugFrames);
		_DebugFrames.addDebug(this, "INIT", "结束读取用户模板");

		// 初始化皮肤
		_DebugFrames.addDebug(this, "INIT", "开始读取皮肤");

		String frameType = this.getPageItemValue("FrameTag", "FrameTag").toUpperCase().trim();

		// 设置配置类型，tree，listframe...
		this._SysParas.setFrameType(frameType);

		// 缓存时间
		this.initCached();

		// 事件调用名称
		String actionName = _SysParas.getActionName();
		if (actionName != null && actionName.length() > 0) {
			this.initParametersDoAction();
		}
		// 设置Log的执行名称
		this._Log.setActionName(actionName);

		// 初始化权限
		this.initAcl();

		// 初始化数据库连接
		this.openSqlConnection();

		// 初始化工作流
		// this.initWorkflow();

		// 初始化操作类
		this.initClass();

		// 初始化数据库数据参数
		this.initDataParameters();

		_DebugFrames.addDebug(this, "INIT", "结束赋值数据库参数");
		_DebugFrames.addDebug(this, "INIT", "结束初始化数据");

		// System.out.println(this._RequestValue.listValues(false));

	}

	/**
	 * 初始化数据库数据参数
	 * 
	 * @throws Exception
	 */
	private void initDataParameters() throws Exception {
		_DebugFrames.addDebug(this, "INIT", "开始赋值数据库参数");
		for (int i = 0; i < this._UserConfig.getUserXItems().count(); i++) {

			UserXItem uxi = this._UserConfig.getUserXItems().getItem(i);
			if (!uxi.testName("DataItem") || uxi.getItem("DataItem").count() == 0) {
				continue;
			}

			UserXItemValues uxv = uxi.getItem("DataItem");
			String dataField = uxv.getItem(0).getItem("DataField");

			String key = dataField.trim().toUpperCase().trim();
			if (key.length() == 0) {
				key = uxi.getName();
			}
			String val = this._RequestValue.getString(uxi.getName());

			if (val == null) {
				continue;
			}
			String tag = uxi.getSingleValue("tag");
			if (tag == null) {
				tag = "";
			}
			// 已经无用了
			if (tag.equals("SwfTakePhoto") || key.toLowerCase().endsWith("_base64")) { // 从页面获取的照片
				try {
					byte[] buf = UConvert.FromBase64String(val);
					this._RequestValue.addValue(key, buf, PageValueTag.SYSTEM);
					PageValue pv = this._RequestValue.getPageValues().getValue(key);
					pv.setDataType("binary");
					pv.setValue(buf);
					// 增加数据的长度
					this._RequestValue.addValue(key + "_LENGTH", buf.length, PageValueTag.SYSTEM);
				} catch (Exception err1) {
					LOGGER.error(err1.getMessage(), err1);
				}
				continue;
			}

			if ("signature".equalsIgnoreCase(tag)) { // 签名
				HtmlUtils.handleSignature(val, key, _RequestValue, uxi, _ItemValues);
				continue;
			}

			String dataType = uxv.getItem(0).getItem("DataType");
			if (dataType.equalsIgnoreCase("binary")) {
				byte[] b = null;
				if (val.length() > 0) {
					String fileName = val;
					String path = UPath.getRealContextPath() + fileName;
					try {
						java.io.File f1 = new java.io.File(path);
						if (f1.exists() && f1.isFile()) {
							b = UFile.readFileBytes(path);
							f1.deleteOnExit();
						}
					} catch (Exception e) {
					}

				}
				if (b != null && b.length > 0) {
					// int len = b == null ? 0 : b.length;
					this._RequestValue.addValue(key, b, PageValueTag.SYSTEM);
					PageValue pv = this._RequestValue.getPageValues().getValue(key);
					pv.setDataType("binary");

					if (!key.equalsIgnoreCase(uxi.getName().trim())) {
						// 将DateField作为参数加入到requestvalue中
						this._RequestValue.addValue(key, b, PageValueTag.SYSTEM);
					}
				}
				// 删除临时文件
			} else if (uxv.getItem(0).getItem("IsEncrypt").equals("1")) {
				// 去除 _SysParas.isPagePost() 判断，2015-09-01 郭磊
				// 加密内容，一般用于密码

				if (val.length() == 0) {
					// 空白字符串的加密也会有值 DA39A3EE5E6B4B0D3255BFEF95601890AFD80709
					this._RequestValue.addValue(key, ""); // 2018-05-22
				} else {
					this._RequestValue.addValue(key, Utils.sha1(val));
				}
			} else {
				int maxLength = 4000;
				if (uxi.testName("MaxMinLength") && uxi.getItem("MaxMinLength").count() > 0) {
					String mlen = uxi.getItem("MaxMinLength").getItem(0).getItem("MaxLength");
					if (mlen.trim().length() > 0) {
						try {
							maxLength = Integer.parseInt(mlen);
						} catch (Exception err) {
						}
					}
				}

				// 计算数字除以比例后的数值
				if (uxv.getItem(0).testName("NumberScale")) {
					String scale = uxv.getItem(0).getItem("NumberScale");
					if (scale.trim().length() > 0) {
						try {
							BigDecimal numberScale = new BigDecimal(scale);
							if (numberScale.longValue() > 0) {
								BigDecimal val1 = new BigDecimal(val);
								BigDecimal d1 = val1.multiply(numberScale);
								;
								val = d1.toPlainString();
							}
						} catch (Exception err) {
							val = "ERR:" + err.getMessage();
						}
					}
				}
				if (!key.equalsIgnoreCase(uxi.getName().trim())) {
					// 将DateField作为参数加入到requestvalue中
					this._RequestValue.addValue(key, val, PageValueTag.SYSTEM);
				}
				// 修改参数类型及长度
				this._RequestValue.changeValue(uxi.getName(), val, dataType, maxLength);
			}
		}
	}

	/**
	 * 初始化页面缓存参数
	 */
	private void initCached() {
		// 缓存时间
		String cachedType = this.getPageItemValue("Cached", "CachedType");
		String cachedSeconds = this.getPageItemValue("Cached", "CachedSeconds");
		cachedType = (cachedType == null || cachedType.trim().length() == 0) ? "none" : cachedType.trim().toLowerCase();
		this._SysParas.setCachedType(cachedType);
		if (!cachedType.trim().equals("none")) {
			try {
				int seconds = Integer.parseInt(cachedSeconds);
				this._SysParas.setIsCached(true);
				this._SysParas.setCachedSeconds(seconds);
			} catch (Exception e) {
				this._SysParas.setIsCached(false);
				this._SysParas.setCachedSeconds(-1);
				LOGGER.error(e.getMessage(), e);
			}
		} else {
			this._SysParas.setIsCached(false);
			this._SysParas.setCachedSeconds(-1);
		}
	}

	/**
	 * 初始化调用的类
	 * 
	 * @throws Exception
	 */
	private void initClass() throws Exception {
		this._ItemValues = new ItemValues();
		this._HtmlClass = new HtmlClass();
		this._HtmlClass.setHtmlCreator(this);

		this._ItemValues.setHtmlClass(this._HtmlClass);
		this._ItemValues.setUObjectValue(this._UObjectValue);
		this._ItemValues.setUserClass(this._UserClass);
		this._ItemValues.setDTTables(this._Tables);

		Skin skin = Skin.instance();

		String skinName = this._RequestValue.getString(FrameParameters.EWA_SKIN);
		if (skinName == null || skinName.trim().length() == 0) {
			skinName = this._RequestValue.getString(FrameParameters.EWA_SKIN_SESSION);
			if (skinName == null || skinName.trim().length() == 0) {
				skinName = getPageItemValue("SkinName", "SkinName");
			}
		} else {
			this._RequestValue.getSession().setAttribute(FrameParameters.EWA_SKIN_SESSION, skinName);
		}

		_DebugFrames.addDebug(this, "INIT", "开始读取皮肤 (" + skinName + ")");
		SkinFrames skinFrames = skin.getSkinFrames(skinName);
		if (skinFrames == null) {
			skinName = "Test1";
			skinFrames = skin.getSkinFrames(skinName);
			this._RequestValue.getSession().setAttribute(FrameParameters.EWA_SKIN_SESSION, skinName);
		}
		SkinFrame skinFrameAll = skinFrames.getItem("ALL");
		SkinFrame skinFrameCurrent = skinFrames.getItem(this._SysParas.getFrameType());
		_DebugFrames.addDebug(this, "INIT", "结束读取皮肤 (" + skinName + ")");

		this._HtmlClass.setItemValues(this._ItemValues);
		this._HtmlClass.setSysParas(this._SysParas);

		// 设置Document对象是否为xhtml
		this._Document.setIsXhtml(this._SysParas.isXhtml());
		// 传递 HtmlClass对象
		this._Document.setHtmlClass(this._HtmlClass);

		this._HtmlClass.setDocument(this._Document);

		this._HtmlClass.setEwaGlobals(this._EwaGlobals);
		this._HtmlClass.setDebugFrames(this._DebugFrames);
		this._HtmlClass.setItemValues(this._ItemValues);
		this._HtmlClass.setSkinFrameAll(skinFrameAll);
		this._HtmlClass.setSkinFrameCurrent(skinFrameCurrent);
		this._HtmlClass.setSkinFrames(skinFrames);
		this._HtmlClass.setUserConfig(this._UserConfig);
		this._HtmlClass.setSkin(skin);
		this._HtmlClass.setResponse(this._Response);

		if (this._Workflow != null) {
			this._HtmlClass.setWorkflow(this._Workflow);
		}
		this._Frame = this._HtmlClass.getFrame();

		this._Action = this._HtmlClass.getAction();

		// 生成页面标题
		createPageTitle();
	}

	/**
	 * 装载权限控制
	 * 
	 * @throws Exception
	 */
	private void initAcl() throws Exception {
		_DebugFrames.addDebug(this, "INIT", "加载权限控制");

		if (!this._UserConfig.getUserPageItem().testName("Acl")) {
			_DebugFrames.addDebug(this, "INIT", "无权限控制");
			return;
		}
		if (this._UserConfig.getUserPageItem().getItem("Acl").count() == 0) {
			_DebugFrames.addDebug(this, "INIT", "无权限控制");
			return;
		}
		UserXItemValue acl = this._UserConfig.getUserPageItem().getItem("Acl").getItem(0);
		String aclExp = acl.getItem("Acl");
		if (StringUtils.isBlank(aclExp)) {
			return;
		}
		aclExp = aclExp.trim();
		if (aclExp.length() <= 5) {
			return;
		}

		UObjectValue ov = new UObjectValue();
		try {
			Object o = ov.loadClass(aclExp, null);
			Type oAcl = o.getClass().getGenericInterfaces()[0];
			String name = oAcl.toString();
			if (name.equals("interface com.gdxsoft.easyweb.acl.IAcl")) { // 权限控制版本1
				this._Acl = (IAcl) o;
				this._Acl.setXmlName(this._UserConfig.getXmlName());
				this._Acl.setItemName(this._UserConfig.getItemName());
				this._Acl.setHtmlCreator(this);
				this._Acl.setRequestValue(this.getRequestValue());
			} else { // 权限控制版本2
				this._Acl2 = (IAcl2) o;
				this._Acl2.setXmlName(this._UserConfig.getXmlName());
				this._Acl2.setItemName(this._UserConfig.getItemName());
				this._Acl.setRequestValue(this.getRequestValue());
				this._Acl2.setHtmlCreator(this);
			}
			_DebugFrames.addDebug(this, "INIT", "加载了权限控制(" + o.getClass().getName() + ")");
		} catch (Exception e) {
			this._Acl = null;
			_DebugFrames.addDebug(this, "INIT", "权限错误(" + aclExp + ")");
			LOGGER.warn("Load acl (" + aclExp + ") error, skiped, {}", e.getMessage());
		}
	}

	/**
	 * 当为表达式的是否，例如 A.USER_ID 取第一个点后面的名称
	 * 
	 * @param keyField
	 * @return
	 */
	private String removeDotEwaActionKey(String keyField) {
		String field = keyField;
		int dotLoc = keyField.indexOf(".");
		if (dotLoc > 0) {
			// 当为表达式的是否，例如 A.USER_ID
			// 取第一个点后面的名称
			field = field.substring(dotLoc + 1).trim();
		} else {
			field = field.trim();
		}
		return field;
	}

	/**
	 * ListFrame EWA_ACTION_KEY
	 * 
	 * @throws Exception
	 */
	private void initParameterEwaActionKey() throws Exception {
		// 用于ListFrame调用
		String ewaActionKey = this._RequestValue.s(FrameParameters.EWA_ACTION_KEY);
		if (ewaActionKey == null || !this._UserConfig.getUserPageItem().testName("PageSize")) {
			return;
		}
		String keyField = null;
		UserXItemValues uxvs = this._UserConfig.getUserPageItem().getItem("PageSize");
		if (uxvs.count() == 0)
			return;

		keyField = uxvs.getItem(0).getItem("KeyField");
		String[] fields = keyField.split(",");
		if (fields.length == 1) {
			// 单字段表达式
			String field = this.removeDotEwaActionKey(keyField);
			this._RequestValue.addValue(field, ewaActionKey);

			return;
		}
		// 多字段表达式
		String[] vals = ewaActionKey.split(",");
		if (fields.length != vals.length) {
			LOGGER.warn("EWA_ACTION_KEY, fields.length != vals.length");
			return;
		}
		for (int i = 0; i < fields.length; i++) {
			String field = this.removeDotEwaActionKey(fields[i]);
			this._RequestValue.addValue(field, vals[i]);
		}

	}

	/**
	 * tree EWA_TREE_KEY
	 * 
	 * @throws Exception
	 */
	private void initParameterTree() throws Exception {
		// 用于Tree调用
		String ewaTreeKey = this._RequestValue.s(FrameParameters.EWA_TREE_KEY);
		if (ewaTreeKey == null || !this._UserConfig.getUserPageItem().testName("Tree")) {
			return;
		}
		UserXItemValues uxvs = this._UserConfig.getUserPageItem().getItem("Tree");
		if (uxvs.count() == 0)
			return;

		String key = uxvs.getItem(0).getItem("Key");
		String parentKey = uxvs.getItem(0).getItem("ParentKey");
		String text = uxvs.getItem(0).getItem("Text");

		// 将标准树传递的 EWA_TREE_KEY映射为表对应的字段
		this._RequestValue.addValue(key, ewaTreeKey);
		String pkey = this._RequestValue.getString(FrameParameters.EWA_TREE_PARENT_KEY);
		if (pkey != null && pkey.equals(TreeViewMain.TREE_ROOT_KEY)) {
			// 最高层主键值
			pkey = uxvs.getItem(0).getItem("RootId");
		}
		this._RequestValue.addValue(parentKey, pkey);
		this._RequestValue.addValue(text, this._RequestValue.getString(FrameParameters.EWA_TREE_TEXT));

	}

	/**
	 * 将Action传递的Key转换为表达式,例如：SALES_ID=1
	 */
	private void initParametersDoAction() {

		try {
			// 用于ListFrame调用 EWA_ACTION_KEY
			initParameterEwaActionKey();
		} catch (Exception e) {
			LOGGER.warn(e.getMessage());
		}
		try {
			// tree EWA_TREE_KEY
			initParameterTree();
		} catch (Exception e) {
			LOGGER.warn(e.getMessage());
		}

	}

	/**
	 * 生成Frame框架
	 * 
	 * @return
	 * @throws Exception
	 */
	private boolean createFrameSet() throws Exception {
		if (this._RequestValue.getString(FrameParameters.EWA_FRAMESET_NO) != null) {
			return false;
		}
		if (!this._UserConfig.getUserPageItem().testName("HtmlFrame")) {
			return false;
		}
		UserXItemValues us = this._UserConfig.getUserPageItem().getItem("HtmlFrame");
		if (us.count() == 0)
			return false;
		UserXItemValue u = us.getItem(0);
		String frameType = u.getItem("FrameType");
		if (frameType.trim().length() == 0)
			return false;
		String frameSize = u.getItem("FrameSize");

		if (frameType.equals("H5") || frameType.equals("V5")) { // html5
			return false;
		}

		String size = frameType.equals("H") ? "cols" : "rows";
		size += "='" + frameSize + "'";

		MStr sbUrl = new MStr();
		sbUrl.append(this.getRequestValue().getRequest().getRequestURI());
		sbUrl.append("?EWA_FRAMESET_NO=1");
		String q = this.getRequestValue().getRequest().getQueryString();
		if (q != null) {
			if (q.startsWith("&")) {
				sbUrl.append(q);
			} else {
				sbUrl.append("&");
				sbUrl.append(q);
			}
		}
		String url = Utils.encodeUrl(sbUrl.toString());
		// url= URLEncoder.encode(url,"iso8859-1");

		String url1 = this._RequestValue.getString(FrameParameters.EWA_FRAME_URL);
		if (url1 == null) {
			url1 = "about:blank";
		} else {
			url1 = Utils.encodeUrl(url1);
		}

		MStr sb = new MStr();
		sb.appendLine("<head>");
		sb.appendLine("<title>" + _SysParas.getTitle() + "</title>");
		String style = this._HtmlClass.getSkinFrameCurrent().getStyle();
		if (style == null || style.trim().length() == 0) {
			style = this._HtmlClass.getSkinFrameCurrent().getStyle();
		}
		sb.al("<style tyle='text/css'>" + style + "</style>");
		sb.al("</head>");

		String userAgent = this._RequestValue.getRequest().getHeader("User-Agent");
		// String frameBorder = "1";
		String frameBorder = u.getItem("FrameBorder");
		if (userAgent != null && userAgent.indexOf("MSIE") >= 0) {
			frameBorder = "no";
		}
		sb.appendLine(
				"<frameset " + size + " frameborder='" + frameBorder + "' class='frameborder' id='ewa_frameset'>");
		sb.appendLine("<frame class='frameborder' name='top' target='contents' src=\"" + url + "\" />");
		sb.appendLine("<frame class='frameborder' name='contents' src=\"" + Utils.textToJscript(url1) + "\" />");
		sb.appendLine("</frameset>");
		this._PageHtml = sb.toString();
		return true;
	}

	/**
	 * 检查是否通过ACL校验
	 * 
	 * @return
	 */
	public boolean checkAcl() {
		if (this._Acl == null) {
			return true;
		}

		_DebugFrames.addDebug(this, "HTML", "开始验证权限");
		if (_Acl.canRun()) {
			_DebugFrames.addDebug(this, "HTML", "验证通过");
			return true;

		}

		String msgCn = "没有权限执行，可能的原因是登录失效或没有授权";
		String msgEn = "You don't have permission. The possible reason is the login is invalid or NO authorization";
		String msg = "enus".equals(this._RequestValue.getLang()) ? msgEn : msgCn;

		JSONObject msgJson = UJSon.rstFalse(msg);

		_DebugFrames.addDebug(this, "HTML", "验证失败");

		if (!this.isAjaxCall()) {
			if (_Acl.getGoToUrl() == null) {
				this._PageHtml = msg;
				return false;
			}

			String s1 = "document.location.href=\"" + this._Acl.getGoToUrl() + "\";";
			this._PageHtml = "\r\n<script language=\"javascript\">\r\n" + s1 + "\r\n</script>\r\n";
			return false;
		}

		String ajax = _SysParas.getAjaxCallType();

		if (ajax.equalsIgnoreCase("XML") || ajax.equalsIgnoreCase("XMLDATA")) {
			this._PageHtml = "<root><error>" + msg + "</error></root>";
		} else if (ajax.equalsIgnoreCase("HAVE_DATA")) {
			this._PageHtml = msg;
		} else if (ajax.equalsIgnoreCase("DOWN_DATA")) { // DOWN_DATA 则表示下载数据
			this._PageHtml = msg;
		} else if (ajax.equalsIgnoreCase("TOP_CNT_BOTTOM")) {
			this._PageHtml = msg;
		} else if (ajax.equalsIgnoreCase("JSON")) {
			this._PageHtml = "[" + msgJson + "]";
		} else if (ajax.equalsIgnoreCase("JSON_EXT") || ajax.equalsIgnoreCase("JSON_EXT1")) {
			this._PageHtml = msgJson.toString();
		} else if (ajax.equalsIgnoreCase("JSON_ALL")) {
			this._PageHtml = "[[" + msgJson + "]]";
		} else if (ajax.equalsIgnoreCase("SELECT_RELOAD")) {
			this._PageHtml = msgJson.toString();
		} else if (ajax.equalsIgnoreCase("LF_RELOAD")) {
			this._PageHtml = msg;
		} else if (ajax.equalsIgnoreCase("INSTALL")) {
			this._PageHtml = msg;
		} else if (ajax.equalsIgnoreCase("WORKFLOW")) {
			this._PageHtml = msg;
		} else {
			this._PageHtml = msg;
		}
		return false;

	}

	/**
	 * 创建错误输出html
	 * 
	 * @param returnValue
	 * @return
	 */
	private String createEwaErrOut(String err) {
		try {
			this._Frame.createSkinTop();
		} catch (Exception e) {
			LOGGER.warn(e.getMessage());
		}
		String title = this._HtmlClass.getDocument().getTitle();
		String alert = "EWA.UI.Msg.ShowError(\"" + Utils.textToJscript(err) + "\",\"" + Utils.textToJscript(title)
				+ "\");";
		if (this.isAjaxCall()) {
			return alert;
		}

		String ua = this.getRequestValue().s("SYS_USER_AGENT");
		// 是否为移动调用
		boolean isMobile = false;
		if (ua != null) {
			ua = ua.toLowerCase();
			isMobile = ua.indexOf("android") > 0 || ua.indexOf("iphone") > 0 || ua.indexOf("ipad") > 0;
		}
		StringBuilder sber = new StringBuilder();
		if (isMobile) {
			// 移动处理
			sber.append("<div id='Test1' class='ewa-err-out MSG_INFO' >");
			sber.append("<table id='EWA_FRAME_MAIN' align=center border=0 width=100%><tr>");
			sber.append("<td align=center width=64><div class='ERR_ICON'></div></td><td class='MSG_TEXT'>");
			sber.append(Utils.textToInputValue(err));
			sber.append("</td></tr>");
			sber.append("<tr><td colspan=2><a onclick='EWA_App.back();' class='ewa-err-out-back'></a></td></tr>");
			sber.append("</table></div>");
			return sber.toString();
		}

		String id = "ewa_err_out_" + Utils.randomStr(20);
		try {
			String header = this._HtmlClass.getDocument().showHeader();
			header = this._HtmlClass.getItemValues().replaceLogicParameters(header);
			sber.append(header);
			sber.append("<style>.ewa-err-out {min-width: 390px;min-height:150px;}</style>");
		} catch (Exception e) {
			LOGGER.warn(e.getMessage());
		}
		sber.append("<div id='Test1' class='ewa-err-out'><div id='" + id + "'></div></div>");

		// 为了和列表弹出对话框一致
		sber.append("<script>(function(){");
		// 1.利用脚本弹出对话框
		sber.append("let a=" + alert);
		// 2.获取对话框内容
		sber.append("let b=a._Dialog._Dialog.GetFrameContent().innerHTML;");
		// 3.关闭对话框
		sber.append("a._Dialog.CloseWindow();");
		// 4.将对话框内容放到页面里
		sber.append("$('#" + id + "').html(b);");
		// 5.修改按钮事件
		sber.append("$('#" + id + " button').attr('onclick', '_EWA_DialogWnd.CloseWindow()')");
		sber.append("})();</script></body></html>");

		return sber.toString();
	}

	/**
	 * 生成页面信息
	 * 
	 * @throws Exception
	 */
	public void createPageHtml() throws Exception {
		if (!this.checkAcl()) {
			return;
		}
		_DebugFrames.addDebug(this, "HTML", "开始合成HTML");

		if (_SysParas.isPagePost() && !this.checkValidCode()) { // 验证码错误
			this._PageHtml = "EWA.F.FOS['" + this._SysParas.getFrameUnid() + "'].ValidCodeError();";
			return;
		} else {
			if (createFrameSet()) {
				return;
			}
		}

		CacheEwaScript cache = loadCached();
		try {
			if (cache == null || cache.getResult() != CacheLoadResult.OK) {
				// 调用活动，执行数据库操作
				_DebugFrames.addDebug(this, "HTML", "开始执行ACTION");
				String returnValue = this.executeAction(); // "",URL,SCRIPT
				this._ActionReturnValue = returnValue;
				_DebugFrames.addDebug(this, "HTML", "结束执行ACTION");

				if (returnValue != null && returnValue.equals("[NOT RUN]")) {
					if (_SysParas.isAjaxCall()) {
						this._PageHtml = "alert(\"" + this._Acl2.getNotRunTitle() + "\");";
					} else {
						this._PageHtml = "<h1>" + this._Acl2.getNotRunTitle() + "</h1>";
					}
					return;
				}

				// 生成提交后行为
				this.createPostBehavior();
				if (returnValue.equals("")) {// 不是执行脚本或页面跳转
					if (_SysParas.isAjaxCall()) {
						this.createPageAjax();
					} else {
						createPageHtm1();
					}
				} else if (returnValue.equals("URL")) {// 最后执行跳转页面
					String s = this._ItemValues.replaceParameters(this._Document.showJs(false), true);
					if (_SysParas.isAjaxCall()) {
						this._PageHtml = s;
					} else {
						this._PageHtml = Utils.getJavascript(s);
					}
				} else if (returnValue.equals("SCRIPT")) {// 最后执行脚本调用
					String s = this._ItemValues.replaceParameters(this._Document.showJs(false), true);
					if (_SysParas.isAjaxCall()) {
						this._PageHtml = s;
					} else {
						this._PageHtml = Utils.getJavascript(s);
					}
				} else {
					this._ErrOut = true;
					this.errOutMessage = returnValue;

					// 抛出错误
					this._PageHtml = this.createEwaErrOut(returnValue);
					this.getDocument().setTitle(returnValue);
				}
				writeCache(cache, this.getPageHtml());
			} else {
				_DebugFrames.addDebug(this, "HTML", "从缓存中加载");
				this._PageHtml = cache.getCachedContent();
			}

		} catch (Exception e) {
			_DebugFrames.setRunTimeException(e);
			_DebugFrames.addDebug(this, "ERR", "执行中错误：" + e.getMessage());

			LOGGER.error(e.getMessage(), e);
			throw e;
		} finally {
			// 写如日志系统
			writeLog();

			if (_SysParas.getDataConn() != null) {
				_SysParas.getDataConn().close();
			}
			// 当执行类IClassDao时，this._ItemValues的DataConn是Class的DataConn
			// 和当前的DataConn不一致，因此也要关闭
			if (this._ItemValues.getDataConn() != null) {
				this._ItemValues.getDataConn().close();
			}
		}
	}

	private void postMessage() {
		// 提交页面消息，用于前台
		if (MsgQueueManager.getCount() == 0) {
			return;
		}
		try {
			MsgQueueManager.postMessage(this._RequestValue, this._SysParas.getFrameUnid(),
					this._SysParas.getActionName(), this._RequestValue.queryToJson());
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	/**
	 * 读取Cache
	 * 
	 * @return 内容
	 */
	private CacheEwaScript loadCached() {
		CacheEwaScript cache = null;
		if (!this._SysParas.isCached() || this._SysParas.getCachedSeconds() <= 0) {
			return null;
		}
		String ct = this._SysParas.getCachedType().trim().toLowerCase();
		if (ct.equals("all") || (ct.equals("load") && !this._SysParas.isPagePost())
				|| (ct.equals("action") && this._SysParas.isPagePost())) {
			try {
				cache = new CacheEwaScript(this._SysParas.getRequestValue());
				// 读取Cache
				cache.getCachedContent(_SysParas.getCachedSeconds());
				return cache;
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		return null;
	}

	/**
	 * 写chache
	 * 
	 * @param cnt
	 * @return
	 */
	private boolean writeCache(CacheEwaScript cache, String cnt) {
		if (cache == null || !this._SysParas.isCached() && this._SysParas.getCachedSeconds() <= 0) {
			return false;
		}
		try {
			cache.writeCache(cnt);
			return true;
		} catch (Exception e1) {
			LOGGER.error(e1.getMessage(), e1);
			return false;
		}
	}

	public String createPageJson() throws Exception {
		String allowJsonExport = this.getPageItemValue("AllowJsonExport", "AllowJsonExport");
		if ("no".equals(allowJsonExport)) {
			return "['AllowJsonExport=no']";
		}
		MStr sb = new MStr();
		String jsonName = this._RequestValue.getString(FrameParameters.EWA_JSON_NAME);
		if (jsonName != null) {
			// 避免xss注入
			if (jsonName.indexOf("<") >= 0 || jsonName.indexOf("&") >= 0 || jsonName.indexOf("#") >= 0
					|| jsonName.indexOf("(") >= 0 || jsonName.indexOf(";") >= 0 || jsonName.indexOf("\n") >= 0
					|| jsonName.indexOf("=") >= 0 || jsonName.indexOf(" ") >= 0 || jsonName.indexOf("\t") >= 0
					|| jsonName.indexOf("-") >= 0) {
				jsonName = "ni_nong_sha_lei";
			}
			sb.append("" + Utils.textToJscript(jsonName) + "=");
		}
		int len = this._ItemValues.getDTTables().size();
		if (len == 0) {

			if (this._ItemValues.getJSONObjects().size() > 0) {
				// 有json objects
				return this._ItemValues.getJSONObjects().getLast().toString();
			}

			sb.a("[]");
			return sb.toString();
		}
		DTTable dt = (DTTable) this._ItemValues.getDTTables().get(len - 1);
		// 时差
		dt.setTimeDiffMinutes(this._HtmlClass.getSysParas().getTimeDiffMinutes());

		String s1 = dt.toJson(this._RequestValue);
		sb.a(s1);

		return sb.toString();
	}

	/**
	 * Create the download file path from the frame configuration
	 * 
	 * @return the download file path
	 * @throws Exception
	 */
	public String createResponseFrameDownload() throws Exception {
		File imageFile = null;
		UserXItems xitems = this._HtmlClass.getUserConfig().getUserXItems();

		String uploadXItemName = null;
		// 限定从 有上传文件的配置中获取，避免外部攻击
		for (int i = 0; i < xitems.count(); i++) {
			UserXItem xitem = xitems.getItem(i);
			String tag = xitem.getSingleValue("Tag");
			// find the upload item
			if ("h5upload".equals(tag) || "swffile".equals(tag) || "image".equals(tag)) {
				uploadXItemName = xitem.getName();
				break;
			}
		}

		if (uploadXItemName == null) {
			return null;
		}

		// 第一步检查表中的字段有 EWA_UP_PATH，这是上传文件的保存的物理路径
		// 从数据库返回的表中获取地址，不能从 RequestValue中获取，不安全
		String ewaUpPath = this.getValueFromFrameTables("EWA_UP_PATH");
		if (StringUtils.isNotBlank(ewaUpPath)) {
			imageFile = this.getUploadedFilePath(ewaUpPath, false);
			if (imageFile != null) {
				return imageFile.getAbsolutePath();
			}
		}

		// 第2步检查表中的字段有 EWA_UP_PATH_SHORT，这是上传文件的保存的物理路径，去除PATH_UPLOAD部分
		// 从数据库返回的表中获取地址，不能从 RequestValue中获取，不安全
		String ewaUpPathShort = this.getValueFromFrameTables("EWA_UP_PATH_SHORT");
		if (StringUtils.isNotBlank(ewaUpPathShort)) {
			String path = UPath.getPATH_UPLOAD() + File.separator + ewaUpPathShort;
			imageFile = this.getUploadedFilePath(path, false);
			if (imageFile != null) {
				return imageFile.getAbsolutePath();
			}
		}

		// 第3步检查表中的字段有 uploadXItemName
		// 从数据库返回的表中获取地址，不能从 RequestValue中获取，不安全
		String uploadPath = this.getValueFromFrameTables(uploadXItemName);
		if (StringUtils.isNotBlank(uploadPath)) {
			imageFile = this.getUploadedFilePath(uploadPath, true);
			if (imageFile != null) {
				return imageFile.getAbsolutePath();
			}
		}

		// 第4步检查表中的字段有 uploadXItemName_path
		// 从数据库返回的表中获取地址，不能从 RequestValue中获取，不安全
		String uploadPath1 = this.getValueFromFrameTables(uploadXItemName + "_path");
		if (StringUtils.isNotBlank(uploadPath1)) {
			imageFile = getUploadedFilePath(uploadPath1, true);
			if (imageFile != null) {
				return imageFile.getAbsolutePath();
			}
		}

		return null;

	}

	/**
	 * Get the field value from all tables
	 * 
	 * @param fieldName the field name
	 * @return the value
	 */
	public String getValueFromFrameTables(String fieldName) {
		try {
			Object val = this._ItemValues.getTableValue(fieldName, "");
			if (val == null) {
				return null;
			}
			return val.toString();
		} catch (Exception e) {
			LOGGER.warn("Get the field value from all tables. {}, {}", fieldName, e.getMessage());
			return null;
		}
	}

	private File getUploadedFilePath(String path, boolean checkPrefixUploadPath) {
		if (path == null || path.length() == 0) {
			return null;
		}
		if (path.indexOf("../") >= 0 || path.indexOf("..\\") >= 0) {
			// 不允许获取上级目录地址
			LOGGER.warn("Invalid path, include '../' -> {}", path);
			return null;
		}
		String uploadPath = new File(UPath.getPATH_UPLOAD()).getAbsolutePath();

		File f = new File(path);
		if (f.exists()) {
			// 限定获取文件的目录在 ewa_conf.xml指定的上传目录之下
			if (f.getAbsolutePath().startsWith(uploadPath)) {
				return f;
			} else {
				LOGGER.warn("Invalid download path {}, not in upload dir {}", path, uploadPath);
				return null;
			}
		}

		// 再次检查前面附加上传目录
		if (checkPrefixUploadPath) {
			path = uploadPath + File.separator + path;
			f = new File(path);
			if (f.exists()) {
				return f;
			}
		}
		return null;

	}

	/**
	 * 获取所有的表数据
	 * 
	 * @return
	 */
	public String createPageJsonAll() {
		String allowJsonExport = this.getPageItemValue("AllowJsonExport", "AllowJsonExport");
		if ("no".equals(allowJsonExport)) {
			return "[['AllowJsonExport=no']]";
		}
		int len = this._ItemValues.getDTTables().size();
		if (len == 0) {
			JSONArray arr = new JSONArray();
			// 有json objects

			len = this._ItemValues.getJSONObjects().size();
			for (int i = 0; i < len; i++) {
				Object o = this._ItemValues.getJSONObjects().get(i);
				arr.put(o);
			}

			return arr.toString();
		}
		MStr sb = new MStr();
		sb.a("[");
		for (int i = 0; i < len; i++) {
			if (i > 0) {
				sb.al(",");
			}
			DTTable dt = (DTTable) this._ItemValues.getDTTables().get(i);
			dt.setTimeDiffMinutes(this._HtmlClass.getSysParas().getTimeDiffMinutes());
			String s1 = dt.toJson(this._RequestValue);
			sb.a(s1);

		}
		sb.a("]");
		return sb.toString();
	}

	/**
	 * 获取JSON数据和页面表达
	 * 
	 * @return
	 * @throws Exception
	 */
	public String createPageJsonExt() throws Exception {
		String allowJsonExport = this.getPageItemValue("AllowJsonExport", "AllowJsonExport");
		if ("no".equals(allowJsonExport)) {
			return "{'AllowJsonExport':'no'}";
		}
		MStr sb = new MStr();
		sb.a("{\"CFG\":");
		sb.al(this._Frame.createJsonFrame());
		sb.a(",\"DATA\":");
		String dataJson = this._Frame.createJsonContent();
		if (dataJson == null) {
			sb.al(this.createPageJson());
		} else {
			sb.al(dataJson);
		}
		String wfJs = this._Frame.getWorkFlowButJson();
		if (wfJs != null) {
			sb.a(",\"WF\":");
			sb.al(wfJs);
		}

		if (this._Frame instanceof FrameList) {
			FrameList f = (FrameList) this._Frame;
			int records = f.queryRecords();

			sb.a(",\"RECORDS\":");
			sb.al(records);
		}
		sb.a("}");
		return sb.toString();
	}

	/**
	 * 获取JSON数据和页面表达
	 * 
	 * @return
	 * @throws Exception
	 */
	public String createPageJsonExt1() throws Exception {
		String allowJsonExport = this.getPageItemValue("AllowJsonExport", "AllowJsonExport");
		if ("no".equals(allowJsonExport)) {
			return "{'AllowJsonExport':'no'}";
		}
		MStr sb = new MStr();
		sb.a("{\"CFG\":");
		sb.al(this._Frame.createJsonFrame());
		sb.a(",\"DATA\":");
		String dataJson = this._Frame.createJsonContent();
		if (dataJson == null) {
			sb.al(this.createPageJson());
		} else {
			sb.al(dataJson);
		}
		String wfJs = this._Frame.getWorkFlowButJson();
		if (wfJs != null) {
			sb.a(",\"WF\":");
			sb.al(wfJs);
		}
		String jsonJs = this._Frame.createJsonJs();
		// jsonJs = jsonJs.replace(IItem.REP_AT_STR, "@");
		jsonJs = Utils.textToJscript(jsonJs);
		sb.a(",\"JS\":\"");
		sb.a(jsonJs);
		sb.a("\"");

		this._Frame.createJsFrameXml();
		this._Frame.createJsFramePage();
		String frameJs = this._Document.getJsBottom().getScripts(false);
		frameJs = frameJs.replace(IItem.REP_AT_STR, "@");

		frameJs = Utils.textToJscript(frameJs);

		sb.a(",\"JSFRAME\":\"");
		sb.a(frameJs);
		sb.a("\"");

		String frameUnid = this._SysParas.getFrameUnid();

		sb.a(",\"FRAME_UNID\":\"");
		sb.a(frameUnid);
		sb.a("\"");

		if (this._Frame instanceof FrameList) {
			FrameList f = (FrameList) this._Frame;
			int records = f.queryRecords();

			sb.a(",\"RECORDS\":");
			sb.al(records);
			sb.a(", \"PAGEINFO\":");
			sb.al(f.createJsonPageInfo());
		}

		sb.a(",\"FRAME_TYPE\":\"");
		sb.a(this._Frame.getClass().getSimpleName());
		sb.a("\"");

		sb.a("}");

		// int loc0=sb.toString().indexOf("EWA_ITEMS_XML_");
		// int loc1=sb.toString().indexOf("\";",loc0);
		// String tmp=sb.toString().substring(loc0,loc1);
		// System.out.println(tmp);
		return sb.toString().replace("\t", "\\t");
	}

	/**
	 * 创建重新刷新item的json，例如select的重新刷新
	 * 
	 * @return
	 */
	public String createSelectReload() {
		try {
			return this._Frame.createSelectReload();
		} catch (Exception e) {
			JSONObject rst = new JSONObject();
			rst.put("RST", false);
			rst.put("ERR", e.getMessage());

			return rst.toString();
		}
	}

	/**
	 * 生成数据XML
	 * 
	 * @return
	 * @throws Exception
	 */
	public String createPageXml() throws Exception {
		int len = this._ItemValues.getDTTables().size();
		if (len == 0) {
			return "";
		}
		DTTable dt = (DTTable) this._ItemValues.getDTTables().get(len - 1);
		return dt.toXml(this._RequestValue);
	}

	/**
	 * 仅生成对象本身，不包括头部和尾部等
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getPageMin() throws Exception {
		MStr sb = new MStr();
		_DebugFrames.addDebug(this, "HTML", "开始合成脚本-min");

		// 是否Action最后执行的是SCRIPT
		boolean is_script_return = "SCRIPT".equals(this._ActionReturnValue);

		if (is_script_return) {
			// Action最后执行的是SCRIPT，不附加 css内容
		} else {
			String css = this._Document.getCss().toString();
			if (css.trim().length() > 0) {
				sb.appendLine("<style type='text/css'>");
				sb.append(css);
				sb.appendLine("</style>");
			}
		}
		// js top
		// 是否Action最后执行的是SCRIPT，如果是的话，只返回脚本内容，不添加<script>标签
		String jsTop = this._Document.getJsTop().getScripts(!is_script_return);
		jsTop = _ItemValues.replaceJsParameters(jsTop);

		// js bottom
		// 是否Action最后执行的是SCRIPT，如果是的话，只返回脚本内容，不添加<script>标签
		String jsBottom = this._Document.getJsBottom().getScripts(!is_script_return);
		jsBottom = _ItemValues.replaceJsParameters(jsBottom);

		sb.al(jsTop);

		// 正文
		String cnt = this._Document.getFrameHtml();
		if (cnt.length() > 0) {
			cnt = _ItemValues.replaceParameters(cnt, true);
			sb.appendLine(cnt);
		}
		sb.al(jsBottom);

		_DebugFrames.addDebug(this, "HTML", "结束合成脚本");

		// "\1\2$$##GDX~##JZY$$\3\4"
		return sb.toString().replace(IItem.REP_AT_STR, "@");
	}

	/**
	 * 仅生成对象本身，不包括头部和尾部等
	 * 
	 * @return
	 * @throws Exception
	 */
	public String createPageOnlyCnt() throws Exception {
		MStr sb = new MStr();
		_DebugFrames.addDebug(this, "HTML", "开始合成脚本");

		// html
		if (!this._UserConfig.getUserPageItem().getSingleValue("FrameTag").equalsIgnoreCase("ListFrame")) {
			this._Frame.createFrameHeader();
		}
		this._Frame.createContent();
		// this._Frame.createFrameFooter();

		// js
		this._Frame.createCss();
		this._Frame.createJsTop();
		this._Frame.createJsFramePage();
		this._Frame.createJsBottom();

		// all
		sb.append("<style type='text/css'>");
		sb.append(this._Document.getCss());
		sb.append("</style>");

		sb.append(this._Document.getScriptHtml());
		sb.append(this._Document.getJsTop().getScripts(true));
		sb.append(this._Document.getJsBottom().getScripts(true));
		sb.replace(IItem.REP_AT_STR, "@"); // "\1\2$$##GDX~##JZY$$\3\4"
		String s1 = _ItemValues.replaceParameters(sb.toString(), true);

		_DebugFrames.addDebug(this, "HTML", "结束合成脚本");

		return s1;
	}

	/**
	 * 创建本配置的验证码
	 * 
	 * @return
	 * @throws Exception
	 */
	public BufferedImage createValidCode() throws Exception {
		UserConfig uc = this._UserConfig;

		UserXItem uxi_vc = null;
		for (int i = 0; i < uc.getUserXItems().count(); i++) {
			UserXItem uxi = uc.getUserXItems().getItem(i);
			String tag = uxi.getItem("Tag").getItem(0).getItem(0);
			if (tag.trim().equalsIgnoreCase("valid")) {
				uxi_vc = uxi;
				break;
			}
		}

		if (uxi_vc == null) {
			return null; // 没有配置验证码
		}

		int len = 6;
		String vcType = "string";
		boolean isNumberCode = false;// 默认是数字验证码
		if (uxi_vc != null) {
			if (uxi_vc.testName("MaxMinLength")) {
				try {
					len = Integer.parseInt(uxi_vc.getSingleValue("MaxMinLength", "MaxLength"));
				} catch (Exception err) {
				}
			}
			if (uxi_vc.testName("DataItem")) {
				vcType = uxi_vc.getSingleValue("DataItem", "DataType");
			}

			isNumberCode = !vcType.equalsIgnoreCase("string");
		}

		if (len > 10) {
			len = 10;
		} else if (len < 4) {
			len = 4;
		}

		ValidCode1 vc = new ValidCode1(len, isNumberCode);
		BufferedImage image = vc.createCode();
		String code = vc.getRandomNumber();

		HttpServletRequest req = this._RequestValue == null ? null : this._RequestValue.getRequest();
		if (req != null) {
			HttpSession session = req.getSession() == null ? req.getSession(true) : req.getSession();
			// save to session
			session.setAttribute(ValidCode.SESSION_NAME, code);
		} else {
			LOGGER.warn("The request is null, can't save the validcode to session");
		}

		this.validCode = image;
		return image;
	}

	/**
	 * 生成AJAX调用<br>
	 * XML为显示XML；<br>
	 * HAVE_DATA显示是否有数据;<br>
	 * BIN_FILE 则表示为二进制文件<br>
	 * TOP_CNT_BOTTOM 除了HTML头和尾
	 * 
	 * @throws Exception
	 */
	private void createPageAjax() throws Exception {
		boolean isNoCnt = false;
		if ("1".equals(this._RequestValue.getString(FrameParameters.EWA_NO_CONTENT))) {
			isNoCnt = true;
		}
		if (this._SysParas.getAjaxCallType() == null)
			this._SysParas.setAjaxCallType("");
		String ajax = _SysParas.getAjaxCallType();

		if (ajax.equalsIgnoreCase("DOWNLOAD") || ajax.equalsIgnoreCase("DOWNLOAD-INLINE")) {
			// 下载保存文件或在线文件（图片、pdf等）
			this._PageHtml = this.createResponseFrameDownload();
		} else if (ajax.equalsIgnoreCase("VALIDCODE")) { // 输出验证码
			this.createValidCode();
		} else if (ajax.equalsIgnoreCase("XML")) {
			// 显示为XML数据
			this._PageHtml = this._Frame.createaXmlData();
		} else if (ajax.equalsIgnoreCase("HAVE_DATA")) {
			// 显示为是否有数据
			MList tbs = this._ItemValues.getDTTables();
			if (tbs.size() > 0) {
				DTTable tb = (DTTable) tbs.get(tbs.size() - 1);
				if (tb.getCount() > 0) {
					this._PageHtml = "1";
				} else {
					this._PageHtml = "0";
				}
			} else {
				this._PageHtml = "0";
			}
		} else if (ajax.equalsIgnoreCase("DOWN_DATA")) {
			// DOWN_DATA 则表示下载数据
			String key = "DOWN_DATA_" + this._RequestValue.getString("EWA.ID");
			String name = this._RequestValue.getString(key);
			this._PageHtml = name == null ? "DENY" : name.replace("../../", this._RequestValue.getContextPath() + "/");
		} else if (ajax.equalsIgnoreCase("TOP_CNT_BOTTOM")) {
			this._PageHtml = this.createPageOnlyCnt();
		} else if (ajax.equalsIgnoreCase("JSON")) {
			this._PageHtml = this.createPageJson();
		} else if (ajax.equalsIgnoreCase("JSON_EXT")) {
			this._PageHtml = this.createPageJsonExt();
		} else if (ajax.equalsIgnoreCase("JSON_EXT1")) {
			this._PageHtml = this.createPageJsonExt1();
		} else if (ajax.equalsIgnoreCase("JSON_ALL")) {
			this._PageHtml = this.createPageJsonAll();
		} else if (ajax.equalsIgnoreCase("XMLDATA")) {
			this._PageHtml = this.createPageXml();
		} else if (ajax.equalsIgnoreCase("SELECT_RELOAD")) {
			this._PageHtml = this.createSelectReload();
		} else if (isNoCnt) {
			_DebugFrames.addDebug(this, "HTML", "开始合成脚本");
			this.createJsGlobal();
			this.createJsEventsBack();
			this.createJsActionTip();
			_DebugFrames.addDebug(this, "HTML", "结束合成脚本");
			String js = this._Document.showJs(false);
			if (js.length() > 0) {
				js = this._ItemValues.replaceParameters(js, true);
				this._PageHtml = js;
			}
		} else if (ajax.equalsIgnoreCase("LF_RELOAD")) { // listframe reload
			MStr sb = new MStr();
			this._Frame.createFrameContent();
			String content = this._Document.getScriptHtml().toString();
			content = this._ItemValues.replaceParameters(content, true);
			sb.append(content.replace(IItem.REP_AT_STR, "@"));
			this._PageHtml = sb.toString();
		} else if (ajax.equalsIgnoreCase("INSTALL")) { // listframe reload
			// MStr sb = new MStr();
			this._Frame.createHtml();

			String content = this._Document.showBody();
			// sb.append(content.replace(IItem.REP_AT_STR, "@"));
			String htmlInstall = this._ItemValues.replaceParameters(content, true);
			int loc0 = htmlInstall.indexOf("<!--INC_TOP-->");
			if (loc0 >= 0) {
				htmlInstall = htmlInstall.substring(loc0);
			}
			int loc1 = htmlInstall.indexOf("<!--INC_END-->");
			if (loc1 == -1) {
				loc1 = htmlInstall.indexOf("</body>");
			}
			if (loc1 > 0) {
				htmlInstall = htmlInstall.substring(0, loc1);
			}
			htmlInstall = htmlInstall.replace(IItem.REP_AT_STR, "@");
			this._PageHtml = htmlInstall;
		} else if (ajax.equalsIgnoreCase("WORKFLOW")) { // listframe reload
			MStr sb = new MStr();
			String tmp = "EWA.F.FOS['" + this._SysParas.getFrameUnid() + "']";
			if (this._Workflow == null) {
				sb.a("EWA.UI.Msg.Alter('工作流未定义，无法执行');");
			} else {
				String wfCtrl = this._ItemValues.getRequestValue().getString(FrameParameters.EWA_WF_CTRL);
				WfRst rst = this._Workflow.runNext();
				if (wfCtrl != null && wfCtrl.equals("1")) { // 控制
					if (rst.isOk()) {
						if (rst.getMsg() != null && rst.getMsg().indexOf("Dialog.OpenWindow") > 0) {
							sb.a(rst.getMsg());
						} else {
							sb.a("EWA.UI.Msg.Alter('提交成功','提交成功');");
							sb.a(tmp + ".Reload();");
						}
					} else {
						if (rst.isException()) {
							sb.a("EWA.UI.Msg.Alter(\"系统执行错误," + Utils.textToJscript(rst.getException())
									+ "\",'提交失败');");
						} else {
							sb.a("EWA.UI.Msg.Alter(\"" + Utils.textToJscript(rst.getMsg()) + "\",'提交失败');");
						}
					}
				} else { // 显示
					sb.a(this._Workflow.getWfXItem());
				}
			}
			this._PageHtml = this._ItemValues.replaceParameters(sb.toString(), true);
		} else {
			MStr sb = new MStr();
			this._Frame.createContent();
			String content = this._Document.getScriptHtml().toString();
			sb.append(content.replace(IItem.REP_AT_STR, "@"));
			this._PageHtml = this._ItemValues.replaceParameters(sb.toString(), true);
		}
	}

	/**
	 * 生成HTML页面
	 * 
	 * @throws Exception
	 */
	private void createPageHtm1() throws Exception {
		_DebugFrames.addDebug(this, "HTML", "开始合成脚本");
		this.createJsGlobal();
		this.createJsEventsBack();
		this.createJsActionTip();
		_DebugFrames.addDebug(this, "HTML", "结束合成脚本");

		// EWA_NO_CONTENT 是否显示内容
		if (this._RequestValue.s(FrameParameters.EWA_NO_CONTENT) != null) {
			return;
		}
		_DebugFrames.addDebug(this, "HTML", "开始合成HTML");
		if (_SysParas.isAjaxCall()) {
			this._Frame.createContent();
		} else {
			this._Frame.createHtml();
		}
		_DebugFrames.addDebug(this, "HTML", "结束合成HTML");
	}

	/**
	 * 调用提交后执行的脚本
	 */
	private void createPostBehavior() {
		if (!(_SysParas.isPagePost() || _SysParas.isAjaxCall()) || _SysParas.getBehavior() == null
				|| _SysParas.getBehavior().trim().length() == 0) {
			return;
		}
		if (_SysParas.getAjaxCallType().equalsIgnoreCase("install")) {
			return;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("EWA_PostBehavior(\"");
		sb.append(Utils.textToJscript(_SysParas.getBehavior()));
		sb.append("\", \"");
		sb.append(Utils.textToJscript(this._RequestValue.s(FrameParameters.EWA_PARENT_FRAME)));
		sb.append("\"");
		for (int i = 0; i < this._ItemValues.getDTTables().size(); i++) {
			DTTable tb = (DTTable) this._ItemValues.getDTTables().get(i);
			String tbName = tb.getName(); // = SQLset name
			if (tbName == null) {
				continue;
			}
			if (tbName.toLowerCase().indexOf("paramsout") == 0) { // 参数输出 SQLset name
				sb.append(", ");
				sb.append(tb.toJson(_RequestValue)); // 强制转成字符串表达式，避免long类型在js中溢出
			}
		}
		sb.append(");");

		String s1 = sb.toString();
		this._Document.addJs("PostBehavior", s1, false);
	}

	/**
	 * Action提交后，执行提示信息
	 */
	private void createJsActionTip() {
		// 提交后执行的提示
		String s1 = this._RequestValue.s(FrameParameters.EWA_ACTION_TIP);

		if (s1 == null) { // 页面第一次加载，没有该参数
			return;
		}
		if (s1.trim().length() >= 0) { // ajax调用后
			String tip = "EWA.F.Tip(\"" + Utils.textToJscript(s1) + "\");\r\n";
			this._Document.addJs("JsActionTip", tip, false);
		}

		// 执行后是否重新加载页面
		String reload = this._RequestValue.s(FrameParameters.EWA_ACTION_RELOAD);
		if (reload != null && reload.equals("0")) { // 不加载了
			return;
		}

		// 重新加载页面
		String s2 = "EWA.F.FOS['" + this._SysParas.getFrameUnid() + "'].Reload();";
		this._Document.addJs("JsActionTip1", s2, false);
	}

	private void createJsEventsBack() throws Exception {
		if (!(this._SysParas.isAjaxCall() || this._SysParas.isPagePost())) {
			return;
		}
		// 页面提交的要执行的事件
		String js = this._RequestValue.s(FrameParameters.EWA_AFTER_EVENT);

		if (js == null || js.trim().length() == 0) {
			return;
		}
		if (js.indexOf(".NewNodeAfter") > 0 && _SysParas.getFrameType().equalsIgnoreCase("Tree")) {
			UserXItemValue uv = this._UserConfig.getUserPageItem().getItem("Tree").getItem(0);

			String key = uv.getItem("Key");
			String mg = uv.getItem("MenuGroup");
			String adv1 = uv.getItem("AddPara1");
			String adv2 = uv.getItem("AddPara2");
			String adv3 = uv.getItem("AddPara3");

			String smg = this._ItemValues.getValue(mg, mg);
			String skey = this._ItemValues.getValue(key, key);
			String sadv1 = this._ItemValues.getValue(adv1, adv1);
			String sadv2 = this._ItemValues.getValue(adv2, adv2);
			String sadv3 = this._ItemValues.getValue(adv3, adv3);

			String adv = (sadv1 == null ? "" : sadv1) + "," + (sadv2 == null ? "" : sadv2) + ","
					+ (sadv3 == null ? "" : sadv3);
			String s1 = "(\"" + skey + "\",\"" + adv + "\",\"" + (smg == null ? "" : smg) + "\")";

			String s2 = js.replace(".NewNodeAfter", ".NewNodeAfter" + s1);
			this._Document.addJs("JsEventsBack", s2, false);
		} else {
			// To avoid the XSS (Cross Site Scripting) attacks, the others event are
			// discarded
			// this._Document.addJs("JsEventsBack", js, false);
		}
	}

	/**
	 * 检查验证码
	 * 
	 * @return
	 * @throws Exception
	 */
	private boolean checkValidCode() throws Exception {
		UserXItem uxiValid = this._UserConfig.getValidXItem();
		if (uxiValid == null) { // 没有验证码定义
			return true;
		}

		PageValue pv = this._RequestValue.getPageValues().getPageValue(FrameParameters.EWA_VALIDCODE_CHECK);
		// 不检查验证码，用于手机应用或AJAX调用
		if (pv != null && pv.getValue() != null && pv.getValue().toString().equals("NOT_CHECK")) {
			if (pv.getPVTag() == PageValueTag.HTML_CONTROL_PARAS || pv.getPVTag() == PageValueTag.SYSTEM
					|| pv.getPVTag() == PageValueTag.SESSION) {
				return true;
			} else {
				LOGGER.info("Invalid pageValueTag {} to skip validcode", pv.getPVTag());
			}
		}

		HttpServletRequest req = this._RequestValue.getRequest();
		if (req == null) {
			return false;
		}
		HttpSession session = req.getSession();
		if (session == null) {
			return false;
		}

		if (session.getAttribute(ValidCode.SESSION_NAME) == null) {
			return false;
		}

		String validCodeInSession = session.getAttribute(ValidCode.SESSION_NAME).toString();

		String userValue = this._RequestValue.getString(uxiValid.getName());
		if (validCodeInSession.equalsIgnoreCase(userValue)) {
			// 去除缓存中的验证码
			session.removeAttribute(ValidCode.SESSION_NAME);
			return true;
		} else {
			session.removeAttribute(ValidCode.SESSION_NAME);
			return false;
		}
	}

	/**
	 * 生成页面的title
	 * 
	 * @throws Exception
	 */
	private void createPageTitle() throws Exception {
		String pageDescription = HtmlUtils.getDescription(this._UserConfig.getUserPageItem().getItem("DescriptionSet"),
				"Info", this._SysParas.getLang());
		String title = this._ItemValues.replaceParameters(pageDescription, true);
		_SysParas.setTitle(title);
		this._Log.setDescription(title);
	}

	public String getPageItemValue(String itemName, String tagName) {
		if (this._UserConfig.getUserPageItem().testName(itemName)) {
			try {
				UserXItemValues v = this._UserConfig.getUserPageItem().getItem(itemName);
				if (v.count() == 0) {
					return null;
				}
				if (!v.getParameter().isMulti()) {
					return v.getItem(0).getItem(0);
				} else {
					return v.getItem(0).getItem(tagName);
				}
			} catch (Exception e) {
				return e.getMessage();
			}
		} else {
			return null;
		}
	}

	/**
	 * 执行Action
	 * 
	 * @throws Exception
	 */
	public String executeAction() throws Exception {
		// <ActionSet>
		// <Set Name="页面启动" Type="OnPageLoad">
		// <CallSet>
		// <Set Name="1" Test="" CallType="SqlSet" CallName="读取用户信息" />
		// <Set Name="用户存在" Test="@Count=1" CallType="UrlSet" CallName="到主页" />
		// <Set Name="用户不存在" Test="@Count=0" CallType="UrlSet" CallName="退出" />
		// </CallSet>
		// </Set>
		// </ActionSet>
		this.openSqlConnection();
		UserXItemValue actionItem;

		if (this._Acl2 != null) {
			boolean check = this._Acl2.canRunAction(_SysParas.getActionName());
			if (!check) {
				return "[NOT RUN]";
			}
		}
		String frameType = _SysParas.getFrameType();
		String returnValue = "";
		UserXItem action = this._UserConfig.getUserActionItem();

		if (frameType.equalsIgnoreCase("MultiGrid")) {
			if (!action.testName("ActionSet")) {
				return "";
			}
			UserXItemValues actionSet = action.getItem("ActionSet");
			for (int i = 0; i < actionSet.count(); i++) {
				actionItem = actionSet.getItem(i);
				java.util.Iterator<String> it1 = actionItem.getChildren().keySet().iterator();
				UserXItemValues callSet1 = null;
				while (it1.hasNext()) {
					String key = it1.next();
					if (key.equals("CallSet")) {
						callSet1 = actionItem.getChildren().get(key);
						break;
					}
				}
				if (callSet1 == null || callSet1.count() == 0) {
					continue;
				}
				UserXItemValue callItem1 = callSet1.getItem(0);
				// 当前数据表数量
				int curTableNum = this._ItemValues.getDTTables().size();
				this.executeCallItem(callItem1);
				String dtName = actionItem.getItem("Type");
				for (int mm = curTableNum; mm < this._ItemValues.getDTTables().size(); mm++) {
					// 将新增的表（1个或多个）设置名称
					DTTable dt = (DTTable) this._ItemValues.getDTTables().get(mm);
					dt.setName(dtName);
				}
			}
			if (_SysParas.getDataConn() != null && _SysParas.getDataConn().isTrans()) {
				_SysParas.getDataConn().transCommit();// 提交事物
			}
		} else {
			returnValue = this.executeAction(_SysParas.getActionName());
		}

		return returnValue;
	}

	public String executeAction(String actionName) throws Exception {
		// <ActionSet>
		// <Set Name="页面启动" Type="OnPageLoad">
		// <CallSet>
		// <Set Name="1" Test="" CallType="SqlSet" CallName="读取用户信息" />
		// <Set Name="用户存在" Test="@Count=1" CallType="UrlSet" CallName="到主页" />
		// <Set Name="用户不存在" Test="@Count=0" CallType="UrlSet" CallName="退出" />
		// </CallSet>
		// </Set>
		// </ActionSet>
		this.openSqlConnection();
		UserXItemValue actionItem;
		String frameType = _SysParas.getFrameType();

		String returnValue = "";
		// System.out.println("执行Action - " + this._ActionName);
		this._DebugFrames.addDebug(this, "ACT", "开始执行Action (" + _SysParas.getActionName() + ")");

		actionItem = this.queryActionItem(actionName);

		boolean isTreeGetStatus = false;
		if (actionItem == null && frameType.equalsIgnoreCase("Tree")) {
			// Tree的调用子节点没有定义，使用内部默认的SQL
			if ("1".equals(this._RequestValue.getString(FrameParameters.EWA_TREE_MORE))) {
				actionItem = this.queryActionItem("OnPageLoad");
			}
			// EWA_TREE_STATUS
			if ("1".equals(this._RequestValue.getString(FrameParameters.EWA_TREE_STATUS))) {
				actionItem = this.queryActionItem("OnPageLoad");
				isTreeGetStatus = true;
			}
		}
		if (actionItem == null) {// actionName not defined
			this._DebugFrames.addDebug(this, "ACT", "没有可执行的Action (" + _SysParas.getActionName() + ")");
			return returnValue;
		}

		// 是否需要事务，总开关
		boolean actionSetTranscation = false;
		if (actionItem.checkItemExists("transcation")) {
			String v = actionItem.getItem("transcation");
			actionSetTranscation = "yes".equalsIgnoreCase(v);
		}

		// 设置日志信息
		if (actionItem.checkItemExists("LogMsg")) {
			try {
				String msg = actionItem.getItem("LogMsg");
				this._Log.setMsg(msg);
				this._DebugFrames.addDebug(this, "ACT", "设置日志信息 (" + msg + ")");
			} catch (Exception e) {
				LOGGER.error(e.getLocalizedMessage(), e);
			}
		}

		java.util.Iterator<String> it = actionItem.getChildren().keySet().iterator();
		UserXItemValues callSet = null;
		while (it.hasNext()) {
			String key = it.next();
			if (key.equals("CallSet")) {
				callSet = actionItem.getChildren().get(key);
				break;
			}
		}
		if (callSet == null || callSet.count() == 0) {
			return returnValue;
		}
		String rst = "";
		UserXItemValue callItem = callSet.getItem(0);
		if (isTreeGetStatus && !callItem.getItem("CallType").equalsIgnoreCase("sqlset")) {
			// 如果是TREE获取状态，而且是从OnPageLoad获取，但不是SQL调用
			return returnValue;
		}
		int m = 0;
		int index = 0;

		try {
			if (actionSetTranscation) {
				_SysParas.getDataConn().transBegin();
			}
			while (true) {
				m++;
				rst = this.executeCallItem(callItem);
				if (m > 100) {// m>100 循环调用？
					break;
				}
				if (rst.indexOf("__EWA__STOP__") == 0) {
					returnValue = rst.split("\\$")[1];
					break;
				}
				if (rst.equals("")) {
					index++;
					if (index == callSet.count()) { // 没有更多的可执行部分
						break;
					}
					callItem = callSet.getItem(index);
				} else {
					callItem = callSet.getItem(rst);
				}
			}
			if (actionSetTranscation) {
				_SysParas.getDataConn().transCommit();
			}
		} catch (Exception err) {
			if (actionSetTranscation) {
				_SysParas.getDataConn().transRollback();
			}
		} finally {
			if (_SysParas.getDataConn() != null) {
				_SysParas.getDataConn().close();
			}
		}

		// 提交页面消息，用于前台
		// 已经取消 2023-02-07
		if (actionItem.checkItemExists("IsPostMsg")) {
			String v = actionItem.getItem("IsPostMsg");
			if (v != null && v.trim().equalsIgnoreCase("yes")) {
				this.postMessage();
			}
		}
		return returnValue;
	}

	/**
	 * 获取Action条目
	 * 
	 * @param actionName
	 * @return
	 * @throws Exception
	 */
	private UserXItemValue queryActionItem(String actionName) throws Exception {
		UserXItem action = this._UserConfig.getUserActionItem();
		if (!action.testName("ActionSet")) {
			return null;
		}
		UserXItemValues actionSet = action.getItem("ActionSet");
		UserXItemValue actionItem = null;
		for (int i = 0; i < actionSet.count(); i++) {
			if (actionSet.getItem(i).getItem("Type").trim().equalsIgnoreCase(actionName.trim())) {
				actionItem = actionSet.getItem(i);
				break;
			}
		}
		return actionItem;
	}

	/**
	 * 执行Call条目
	 * 
	 * @param callItem
	 * @return 结果 __EWA__STOP__表示执行结束
	 * @throws Exception
	 */
	private String executeCallItem(UserXItemValue callItem) throws Exception {
		// <Set Name="1" Test="" CallType="SqlSet" CallName="读取用户信息" />

		String callType = callItem.getItem("CallType");
		String callName = callItem.getItem("CallName");
		String test = callItem.getItem("Test");
		this._DebugFrames.addDebug(this, "ACT", "开始执行CallItem (" + callName + ", " + callType + ", " + test + ")");
		if (test.trim().length() > 0) {// 计算逻辑表达式
			this._DebugFrames.addDebug(this, "ACT", "进行逻辑判断 (" + test + ")");
			String exp = this._ItemValues.replaceParameters(test, true, true);
			this._DebugFrames.addDebug(this, "ACT", "逻辑表达式 (" + exp + ")");

			if (!ULogic.runLogic(exp)) {
				this._DebugFrames.addDebug(this, "ACT", "判断为 False");
				return ""; // 结果为false
			}
			this._DebugFrames.addDebug(this, "ACT", "判断为 True");
		}
		if (callType.equalsIgnoreCase("sqlset")) {
			this._Action.executeCallSql(callName);// 执行数据查询
			if (this._Action.getChkErrorMsg() != null) {
				return "__EWA__STOP__$" + this._Action.getChkErrorMsg();
			}
			return ""; // 继续不停止
		} else if (callType.equalsIgnoreCase("jsonset")) { // json调用

			this._Action.executeCallJSon(callName);//
			this._DebugFrames.addDebug(this, "ACT", "完成执行【" + callName + "】");
			return ""; // 继续不停止

		} else if (callType.equalsIgnoreCase("classset")) {
			this._Action.executeCallClass(callName);// 执行数据查询
			this._DebugFrames.addDebug(this, "ACT", "完成执行【" + callName + "】");
			return ""; // 继续不停止
		} else if (callType.equalsIgnoreCase("xmlset")) {
			this._Action.executeXml(callName);// 执行数据查询
			this._DebugFrames.addDebug(this, "ACT", "完成执行【" + callName + "】");
			return ""; // 继续不停止
		} else if (callType.equalsIgnoreCase("urlset")) {
			String s1 = this._Action.exceuteCallUrl(callName);
			this._Document.addJs("", s1, true);
			return "__EWA__STOP__$URL";
		} else {
			String s1 = this._Action.executeCallScript(callName);
			this._Document.addJs("", s1, true);
			return "__EWA__STOP__$SCRIPT";
		}

	}

	private void createJsGlobal() throws Exception {
		if (this._SysParas.isAjaxCall()) {
			return;
		}
		// this._PageScript.append(this._EwaGlobals.createJs(this._Lang));

		// 挪到 com.gdxsoft.easyweb.global.EwaGlobals.createJs 中
		// String js =
		// EwaConfig.instance().getConfigItems().getParameters().createJsAlert(this._SysParas.getLang());
		// this._Document.addJs("JsGlobal", js, false);

	}

	/**
	 * 打开数据连接
	 * 
	 * @throws Exception
	 */
	private void openSqlConnection() throws Exception {

		if (_SysParas.getDataConn() != null) {
			return;
		}

		// <DataSource DataSource="" />
		String dataSource = this.getPageItemValue("DataSource", "DataSource");
		if (dataSource != null && dataSource.trim().length() > 0) {
			_DebugFrames.addDebug(this, "SQL", "初始化数据库连接(" + dataSource + ")");
			_SysParas.setDataConn(new DataConnection());
			_SysParas.getDataConn().setDebugFrames(_DebugFrames);
			_SysParas.getDataConn().setConfigName(dataSource);
			_SysParas.getDataConn().setRequestValue(this._RequestValue);

			// 用户日期和系统的时间偏差值(分钟) 2018-03-09
			if (this._SysParas.getTimeDiffMinutes() != 0) {
				_SysParas.getDataConn().setTimeDiffMinutes(this._SysParas.getTimeDiffMinutes() * -1);
			}
			_DebugFrames.addDebug(this, "SQL", "结束初始化数据库连接");
			this._Log.setDataConn(_SysParas.getDataConn());

		} else {
			_DebugFrames.addDebug(this, "SQL", "无数据库连接");
		}

	}

	/**
	 * 写入日志
	 * 
	 * @param log
	 */
	public void writeLog() {
		ILog logInterface = this.getLogInterface();
		if (logInterface == null) {
			return;
		}

		String msg = this._Log.getMsg();

		// 替换信息中的参数
		this._Log.setMsg(this._ItemValues.replaceParameters(msg, true));

		logInterface.setCreator(this);
		logInterface.setLog(this._Log);
		try {
			logInterface.write();
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage(), e);
		}
	}

	/**
	 * 根据定义获取ILog对象
	 * 
	 * @return
	 */
	private ILog getLogInterface() {
		String logName = "Log";
		if (!this._UserConfig.getUserPageItem().checkItemExists(logName)) {
			return null;
		}
		String logClassName = null;
		try {
			UserXItemValues a = this._UserConfig.getUserPageItem().getItem(logName);
			if (a.count() == 0) {
				return null;
			} else {
				logClassName = a.getItem(0).getItem(logName);
			}
		} catch (Exception e) {
			LOGGER.warn("The Log instance error, {}", e.getLocalizedMessage());
			return null;
		}

		if (logClassName == null || logClassName.trim().length() == 0) {
			return null;
		}
		UObjectValue ov = new UObjectValue();
		try {
			Object o = ov.loadClass(logClassName, null);
			return (ILog) o;
		} catch (Exception e) {
			LOGGER.warn("The Log {} not instanced, {}", logClassName, e.getLocalizedMessage());
			return null;
		}
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
	 * @return the _UserClass
	 */
	public Object getUserClass() {
		return _UserClass;
	}

	/**
	 * @param userClass the _UserClass to set
	 */
	public void setUserClass(Object userClass) {
		if (userClass != null) {
			_UserClass = userClass;
			_UObjectValue = new UObjectValue();
			_UObjectValue.setObject(userClass);

		}
	}

	/**
	 * @return the _UserConfig
	 */
	public UserConfig getUserConfig() {
		return _UserConfig;
	}

	/**
	 * @param userConfig the _UserConfig to set
	 */
	public void setUserConfig(UserConfig userConfig) {
		_UserConfig = userConfig;
	}

	/**
	 * @return the _PageHtml
	 */
	public String getPageHtml() {
		if (this._PageHtml != null) {
			return _PageHtml;
		}
		String s1 = this._Document.showAll();
		s1 = this._ItemValues.replaceParameters(s1, false, true);
		s1 = s1.replace(IItem.REP_AT_STR, "@");
		return s1;

	}

	/**
	 * Ajax调用方式<br>
	 * XML为显示XML；<br>
	 * HAVE_DATA显示是否有数据;<br>
	 * BIN_FILE 则表示为二进制文件<br>
	 * TOP_CNT_BOTTOM 除了HTML头和尾
	 * 
	 * @return the _AjaxCallType
	 */
	public String getAjaxCallType() {
		return _SysParas.getAjaxCallType();
	}

	/**
	 * @return the _DataConn
	 */
	public DataConnection getDataConn() {
		return _SysParas.getDataConn();
	}

	public DebugFrames getDebugFrames() {
		return _DebugFrames;
	}

	public IAcl getAcl() {
		return _Acl;
	}

	public void setAcl(IAcl acl) {
		_Acl = acl;
	}

	/**
	 * @param debugFrames the _DebugFrames to set
	 */
	public void setDebugFrames(DebugFrames debugFrames) {
		_DebugFrames = debugFrames;
	}

	/**
	 * @return the _IsAjaxCall
	 */
	public boolean isAjaxCall() {
		return _SysParas.isAjaxCall();
	}

	/**
	 * 设置是否为AJAX调用
	 * 
	 * @param isAjaxCall the _IsAjaxCall to set
	 */
	public void setIsAjaxCall(boolean isAjaxCall) {
		_SysParas.setIsAjaxCall(isAjaxCall);
	}

	/**
	 * @return the _AjaxCallType
	 */

	/**
	 * 设置Ajax调用方式<br>
	 * XML为显示XML；<br>
	 * HAVE_DATA显示是否有数据;<br>
	 * BIN_FILE 则表示为二进制文件<br>
	 * TOP_CNT_BOTTOM 除了HTML头和尾
	 * 
	 * @param ajaxCallType the _AjaxCallType to set
	 */
	public void setAjaxCallType(String ajaxCallType) {
		_SysParas.setAjaxCallType(ajaxCallType);
	}

	/**
	 * @return the _IsXhtml
	 */
	public boolean isXhtml() {
		return _SysParas.isXhtml();
	}

	/**
	 * @param isXhtml the _IsXhtml to set
	 */
	public void setIsXhtml(boolean isXhtml) {
		_SysParas.setIsXhtml(isXhtml);
	}

	/**
	 * 获取生产的Document
	 * 
	 * @return the _Document
	 */
	public HtmlDocument getDocument() {
		return _Document;
	}

	/**
	 * 获取权限2接口
	 * 
	 * @return the _Acl2
	 */
	public IAcl2 getAcl2() {
		return _Acl2;
	}

	/**
	 * Action执行返回的标志, "SCRIPT", "URL", ""
	 * 
	 * @return
	 */
	public String getActionReturnValue() {
		return _ActionReturnValue;
	}

	/**
	 * 系统执行出现 err_out
	 * 
	 * @return true/false
	 */
	public boolean isErrOut() {
		return _ErrOut;
	}

	/**
	 * 系统执行出现 err_out抛出的消息
	 * 
	 * @return err_out抛出的消息
	 */
	public String getErrOutMessage() {
		return errOutMessage;
	}

	public IAction getAction() {
		return _Action;
	}

	public BufferedImage getValidCode() {
		return validCode;
	}

}
