package com.gdxsoft.easyweb.script.userConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import com.gdxsoft.easyweb.cache.ConfigCache;
import com.gdxsoft.easyweb.cache.ConfigCacheWidthSqlCached; //有问题
import com.gdxsoft.easyweb.cache.ConfigStatus;
import com.gdxsoft.easyweb.conf.ConfScriptPath;
import com.gdxsoft.easyweb.conf.ConfScriptPaths;
import com.gdxsoft.easyweb.debug.DebugFrames;
import com.gdxsoft.easyweb.script.template.EwaConfig;
import com.gdxsoft.easyweb.script.template.XItemParameter;
import com.gdxsoft.easyweb.script.template.XItemParameterValue;
import com.gdxsoft.easyweb.utils.*;

/**
 * 用户配置文件定义
 * 
 * @author Administrator
 * 
 */
public class UserConfig implements Serializable, Cloneable {
	private static Logger LOGGER = LoggerFactory.getLogger(UserConfig.class);

	// the cached of the ConfScriptPath instances
	private static Map<String, ConfScriptPath> UC_MAP = new ConcurrentHashMap<String, ConfScriptPath>();

	private static final long serialVersionUID = 1872059554611340437L;

	/**
	 * 检查配置信息检查的间隔（秒）
	 */
	public static int CHECK_CHANG_SPAN_SECONDS = 5;

	/**
	 * 获取默认的XItem的xml模板，用于自定义字段显示，参数：<br>
	 * [NAME]<br>
	 * [DES_INFO_ZHCN]<br>
	 * [DES_MEMO_ZHCN]<br>
	 * [DES_INFO_ENUS]<br>
	 * [DES_MEMO_ENUS]<br>
	 * [DATA_FIELD]<br>
	 * [DATA_TYPE]<br>
	 * [DATA_FORMAT]
	 * 
	 * @return XItem的xml模板
	 */
	public static String getXItemXmlTemplate() {
		StringBuilder sb = new StringBuilder();
		sb.append("<XItem Name=\"[NAME]\">\n");
		sb.append("<Tag>\n");
		sb.append("  <Set IsLFEdit=\"0\" SpanShowAs=\"\" Tag=\"span\"/>\n");
		sb.append("</Tag>\n");
		sb.append("<Name>\n");
		sb.append("  <Set Name=\"[NAME]\"/>\n");
		sb.append("</Name>\n");
		sb.append("<GroupIndex>\n");
		sb.append("  <Set GroupIndex=\"\"/>\n");
		sb.append("</GroupIndex>\n");
		sb.append("<InitValue>\n");
		sb.append("  <Set InitValue=\"\"/>\n");
		sb.append("</InitValue>\n");
		sb.append("<DescriptionSet>\n");
		sb.append("  <Set Info=\"[DES_INFO_ZHCN]\" Lang=\"zhcn\" Memo=\"[DES_MEMO_ZHCN]\"/>\n");
		sb.append("  <Set Info=\"[DES_INFO_ENUS]\" Lang=\"enus\" Memo=\"[DES_MEMO_ENUS]\"/>\n");
		sb.append("</DescriptionSet>\n");
		sb.append("<XStyle>\n");
		sb.append(
				"  <Set XStyleAlign=\"\" XStyleBold=\"\" XStyleColor=\"\" XStyleCursor=\"\" XStyleFixed=\"\" XStyleNoWrap=\"\" XStyleVAlign=\"\" XStyleWidth=\"\"/>\n");
		sb.append("</XStyle>\n");
		sb.append("<Style>\n");
		sb.append("  <Set Style=\"\"/>\n");
		sb.append("</Style>\n");
		sb.append("<ParentStyle>\n");
		sb.append("  <Set ParentStyle=\"\"/>\n");
		sb.append("</ParentStyle>\n");
		sb.append("<AttributeSet>\n");
		sb.append("  <Set AttLogic=\"\" AttName=\"\" AttValue=\"\"/>\n");
		sb.append("</AttributeSet>\n");
		sb.append("<EventSet>\n");
		sb.append("  <Set EventLogic=\"\" EventName=\"\" EventType=\"\" EventValue=\"\"/>\n");
		sb.append("</EventSet>\n");
		sb.append("<IsHtml>\n");
		sb.append("  <Set IsHtml=\"\"/>\n");
		sb.append("</IsHtml>\n");
		sb.append("<OrderSearch>\n");
		sb.append(
				"  <Set GroupTestLength=\"\" IsGroup=\"\" IsGroupDefault=\"\" IsOrder=\"1\" IsSearchQuick=\"\" OrderExp=\"\" SearchExp=\"\" SearchMulti=\"\" SearchSql=\"\" SearchType=\"\"/>\n");
		sb.append("</OrderSearch>\n");
		sb.append("<MaxMinLength>\n");
		sb.append("  <Set MaxLength=\"\" MinLength=\"\"/>\n");
		sb.append("</MaxMinLength>\n");
		sb.append("<MaxMinValue>\n");
		sb.append("  <Set MaxValue=\"\" MinValue=\"\"/>\n");
		sb.append("</MaxMinValue>\n");
		sb.append("<IsMustInput>\n");
		sb.append("  <Set IsMustInput=\"\"/>\n");
		sb.append("</IsMustInput>\n");
		sb.append("<Switch>\n");
		sb.append("  <Set SwtAction=\"\" SwtOnValue=\"\"/>\n");
		sb.append("</Switch>\n");
		sb.append("<DataItem>\n");
		sb.append(
				"  <Set DataField=\"[DATA_FIELD]\" DataType=\"[DATA_TYPE]\" DisableOnModify=\"\" Format=\"[DATA_FORMAT]\" FrameOneCell=\"\" Icon=\"\" IconLoction=\"\" IsEncrypt=\"\" NumberScale=\"\" SumBottom=\"\" TransTarget=\"\" Translation=\"\" Trim=\"\" Valid=\"\"/>\n");
		sb.append("</DataItem>\n");
		sb.append("<DispEnc>\n");
		sb.append("  <Set EncShowUrl=\"\" EncType=\"\"/>\n");
		sb.append("</DispEnc>\n");
		sb.append("<DataRef>\n");
		sb.append(
				"  <Set RefKey=\"\" RefMulti=\"\" RefMultiSplit=\"\" RefShow=\"\" RefShowStyle=\"\" RefShowType=\"\" RefSql=\"\"/>\n");
		sb.append("</DataRef>\n");
		sb.append("<List>\n");
		sb.append(
				"  <Set DisplayField=\"\" DisplayList=\"\" GroupField=\"\" ListAddBlank=\"\" ListFilterField=\"\" ListFilterType=\"\" ListShowType=\"\" ParentField=\"\" Sql=\"\" TitleField=\"\" TitleList=\"\" ValueField=\"\" ValueList=\"\"/>\n");
		sb.append("</List>\n");
		sb.append("<UserSet>\n");
		sb.append("  <Set Lang=\"\" User=\"\"/>\n");
		sb.append("</UserSet>\n");
		sb.append("<CallAction>\n");
		sb.append("  <Set Action=\"\" AfterTip=\"\" ConfirmInfo=\"\"/>\n");
		sb.append("</CallAction>\n");
		sb.append("<OpenFrame>\n");
		sb.append("  <Set AttatchParas=\"\" CallItemName=\"\" CallMethod=\"\" CallParas=\"\" CallXmlName=\"\"/>\n");
		sb.append("</OpenFrame>\n");
		sb.append("<Frame>\n");
		sb.append("  <Set CallItemName=\"\" CallPara=\"\" CallXmlName=\"\"/>\n");
		sb.append("</Frame>\n");
		sb.append("<UserControl>\n");
		sb.append("  <Set UCCallItem=\"\" UCCallItemName=\"\" UCCallPara=\"\" UCCallXmlName=\"\"/>\n");
		sb.append("</UserControl>\n");
		sb.append("<DefineFrame>\n");
		sb.append("  <Set CallItemName=\"\" CallPara=\"\" CallUrlMethod=\"\" CallXmlName=\"\"/>\n");
		sb.append("</DefineFrame>\n");
		sb.append("<PopFrame>\n");
		sb.append("  <Set PopItemName=\"\" PopPara=\"\" PopTitleField=\"\" PopXmlName=\"\"/>\n");
		sb.append("</PopFrame>\n");
		sb.append("<signature>\n");
		sb.append("  <Set SignBgColor=\"\" SignColor=\"\" SignFormat=\"\" SignLineWidth=\"\" SignPath=\"\"/>\n");
		sb.append("</signature>\n");
		sb.append("<Upload>\n");
		sb.append(
				"  <Set NewSizesIn=\"\" RunUpSQLResized=\"\" UpDelete=\"\" UpExts=\"\" UpJsonEncyrpt=\"\" UpLimit=\"\" UpMulti=\"\" UpNewSizes=\"\" UpPath=\"\" UpSQL=\"\" UpSaveMethod=\"\" UpUnZip=\"\"/>\n");
		sb.append("</Upload>\n");
		sb.append("<VaildEx>\n");
		sb.append("  <Set VXAction=\"\" VXFail=\"\" VXJs=\"\" VXMode=\"\" VXOk=\"\"/>\n");
		sb.append("</VaildEx>\n");
		sb.append("<MGAddField>\n");
		sb.append("  <Set MgfCalc=\"\" MgfComput=\"\" MgfId=\"\" MgfTarget=\"\"/>\n");
		sb.append("</MGAddField>\n");
		sb.append("<AnchorParas>\n");
		sb.append("  <Set aHref=\"\" aTarget=\"\"/>\n");
		sb.append("</AnchorParas>\n");
		sb.append("<LinkButtonParas>\n");
		sb.append("  <Set lkbButtonId=\"\"/>\n");
		sb.append("</LinkButtonParas>\n");
		sb.append("<DopListShow>\n");
		sb.append("  <Set DlsAction=\"\" DlsAfterEvent=\"\" DlsShow=\"\" isDlsEventLoad=\"\"/>\n");
		sb.append("</DopListShow>\n");
		sb.append("<ReportCfg>\n");
		sb.append("  <Set IsReportRepeat=\"\" ReportAction=\"\">\n");
		sb.append("    <ReportTemplate/>\n");
		sb.append("  </Set>\n");
		sb.append("</ReportCfg>\n");
		sb.append("<CombineFrame>\n");
		sb.append(
				"  <Set CbAll=\"\" CbGrp=\"\" CbInstall=\"\" CbItemName=\"\" CbJsRename=\"\" CbLst=\"\" CbMearge=\"\" CbPara=\"\" CbXmlName=\"\">\n");
		sb.append("    <CbJs/>\n");
		sb.append("  </Set>\n");
		sb.append("</CombineFrame>\n");
		sb.append("<AddrMapRels>\n");
		sb.append("  <Set AmrCity=\"\" AmrCountry=\"\" AmrLat=\"\" AmrLng=\"\" AmrProvince=\"\" AmrZip=\"\"/>\n");
		sb.append("</AddrMapRels>\n");
		sb.append("<ImageDefault>\n");
		sb.append("  <Set ImageDefault=\"\" ImageLazyLoad=\"\" ImageUrl=\"\"/>\n");
		sb.append("</ImageDefault>\n");
		sb.append("</XItem>");

		return sb.toString();
	}

	/**
	 * Get a UserConfig instance from all configurations
	 * 
	 * @param xmlName     the configuration XML file name
	 * @param itemName    the configuration item name
	 * @param debugFrames the DebugFrames
	 * @return
	 * @throws Exception
	 */
	public static UserConfig instance(String xmlName, String itemName, DebugFrames debugFrames) throws Exception {
		if (xmlName.indexOf("../") >= 0 || xmlName.indexOf("..|") >= 0 || xmlName.indexOf("..\\") >= 0) {
			String err = "invalid string '../' in the " + xmlName;
			LOGGER.error(err);
			throw new Exception(err);
		}
		UserConfig uc = new UserConfig();

		// load instance from cached
		if (debugFrames != null) {
			debugFrames.addDebug(uc, "instance", "Start load instance from cached");
		}
		UserConfig o = getInstanceFromCahced(xmlName, itemName, debugFrames);
		if (o != null) {
			if (debugFrames != null) {
				debugFrames.addDebug(uc, "instance", "return the cachaed instance");
			}
			return o;
		}

		if (debugFrames != null) {
			debugFrames.addDebug(uc, "instance", "Not found in the cache, create a new instance");
		}

		IConfig iConfig = getConfig(xmlName, itemName);

		if (debugFrames != null) {
			debugFrames.addDebug(uc, "instance", "Get the IConfig instance from ->" + xmlName + ", " + iConfig);
		}

		o = new UserConfig(xmlName, itemName);
		o.configType = iConfig;
		o.setDebugFrames(debugFrames);
		o.loadUserDefined();
		o.setDebugFrames(null);

		String msg = "Load new instance of [" + iConfig.getFixedXmlName() + ":" + iConfig.getItemName() + "] from ["
				+ iConfig.getScriptPath().getPath() + "]";

		LOGGER.info(msg);
		if (debugFrames != null) {
			debugFrames.addDebug(uc, "instance", msg);
		}
		if ("sqlcached".equals(UPath.getCfgCacheMethod())) {
			ConfigCacheWidthSqlCached.setUserConfig(iConfig.getFixedXmlName(), itemName, o);
		} else {
			ConfigCache.setUserConfig(iConfig.getFixedXmlName(), itemName, o);
		}
		return o;
	}

	/**
	 * Get the cloned configure from the cached
	 * 
	 * @param xmlName
	 * @param itemName
	 * @param debugFrames
	 * @return instance of null
	 */
	private static UserConfig getInstanceFromCahced(String xmlName, String itemName, DebugFrames debugFrames) {
		String fixedXmlName = UserConfig.filterXmlName(xmlName);
		UserConfig o = null;
		if ("sqlcached".equals(UPath.getCfgCacheMethod())) {
			o = ConfigCacheWidthSqlCached.getUserConfig(fixedXmlName, itemName, debugFrames);
		} else { // 利用内存模式
			UserConfig obj = ConfigCache.getUserConfig(fixedXmlName, itemName);
			if (obj == null) {
				return null;
			}
			try {
				o = (UserConfig) obj.clone();
			} catch (CloneNotSupportedException e) {
				ConfigCache.removeUserConfig(fixedXmlName, itemName);
				LOGGER.error(e.getLocalizedMessage(), e);
			}
		}
		return o;
	}

	/**
	 * Get the configuration from all ScriptPaths
	 * 
	 * @param xmlPath the configuration name
	 * @return IConfig
	 */
	public static IConfig getConfigByPath(String xmlPath) {
		return getConfig(xmlPath, null);
	}

	/**
	 * Get the configuration from all ScriptPaths
	 * 
	 * @param xmlName  the configuration name
	 * @param itemName the item name
	 * @return IConfig
	 */
	public static IConfig getConfig(String xmlName, String itemName) {
		IConfig ic = null;
		ConfScriptPaths sps = ConfScriptPaths.getInstance();
		String key = UserConfig.filterXmlName(xmlName);

		ConfScriptPath sp = null;

		// check exists ConfScriptPath from the cached
		if (UC_MAP.containsKey(key)) {
			sp = UC_MAP.get(key);
			ic = createConfig(sp, xmlName, itemName);
			return ic;
		}

		for (int i = 0; i < sps.getLst().size(); i++) {
			sp = sps.getLst().get(i);
			ic = createConfig(sp, xmlName, itemName);

			// the ResourceConfig checkConfigurationExists is slowly
			if (ic.checkConfigurationExists()) {
				UC_MAP.put(key, sp);
				return ic;
			}

		}
		return null;
	}

	public static IConfig createConfig(ConfScriptPath sp, String xmlName, String itemName) {
		IConfig ic;
		if (sp.isResources()) {
			ic = new ResourceConfig(sp, xmlName, itemName);

		} else if (sp.isJdbc()) {
			ic = new JdbcConfig(sp, xmlName, itemName);

		} else { // file
			ic = new FileConfig(sp, xmlName, itemName);

		}

		return ic;
	}

	/**
	 * 从序列化二进制中获取
	 * 
	 * @param buf
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static UserConfig fromSerialize(byte[] buf) throws IOException, ClassNotFoundException {
		// Serialize
		ByteArrayInputStream fis = new ByteArrayInputStream(buf);
		ObjectInputStream ois = new ObjectInputStream(fis);
		UserConfig tb = (UserConfig) ois.readObject();
		ois.close();
		fis.close();
		return tb;
	}

	/**
	 * 处理 xmlName 中的特殊字符
	 * 
	 * @param xmlName
	 * @return
	 */
	public static String filterXmlName(String xmlName) {
		String xmlFileName = xmlName.trim();
		xmlFileName = xmlFileName.replace("%25", "%");
		xmlFileName = xmlFileName.replace("%7c", "/");
		xmlFileName = xmlFileName.replace("%7C", "/");
		// 去除危险的 ../符号
		xmlFileName.replace("..", "");
		xmlFileName = xmlFileName.replace("|", "/");
		while (xmlFileName.indexOf("//") >= 0) {
			xmlFileName = xmlFileName.replace("//", "/");
		}
		if (!xmlFileName.startsWith("/")) {
			xmlFileName = "/" + xmlFileName;
		}
		return xmlFileName;
	}

	/**
	 * 处理 xmlName 中的特殊字符, 用于数据库调用
	 * 
	 * @param xmlName
	 * @return
	 */
	public static String filterXmlNameByJdbc(String xmlName) {
		String xmlFileName = filterXmlName(xmlName);
		xmlFileName = xmlFileName.replace("/", "|").replace("\\", "|");
		while (xmlFileName.indexOf("||") >= 0) {
			xmlFileName = xmlFileName.replace("||", "|");
		}
		if (!xmlFileName.startsWith("|")) {
			xmlFileName = "|" + xmlFileName;
		}
		return xmlFileName;
	}

	private DebugFrames _DebugFrames;
	private UserXItems _UserXItems; // EasyWebTemplates/EasyWebTemplate/Xitems
	private UserXItem _UserPageItem; // EasyWebTemplates/EasyWebTemplate/Page
	private UserXItem _UserActionItem; // EasyWebTemplates/EasyWebTemplate/Action
	private UserXItems _UserMenuItems; // EasyWebTemplates/EasyWebTemplate/Menu
	private UserXItems _UserPageInfos; // EasyWebTemplates/EasyWebTemplate/PageInfos
	private UserXItems _UserCharts; // EasyWebTemplates/EasyWebTemplate/Charts
	private UserXItems _UserWorkflows; // EasyWebTemplates/EasyWebTemplate/Workflows

	private String _XmlName;
	private String _ItemName;
	private Node _ItemNode;

	private String _ItemNodeXml;
	private String _JS_XML; // 配置文件的对象的 JS表达式

	private IConfig configType;

	private UserXItem validXItem; // The validCode item

	public IConfig getConfigType() {
		if (this.configType != null && this.configType.getFixedXmlName() == null) {
			// parse from Serializable, the configType parameters are null value
			this.configType = getConfig(this.getXmlName(), this.getItemName());
		}
		return configType;
	}

	public UserConfig() {
	}

	public UserConfig(String xmlFileName, String itemName) throws Exception {
		this._XmlName = xmlFileName.trim();
		this._ItemName = itemName.trim();
	}

	/**
	 * 获取配置文件状态
	 * 
	 * @return
	 */
	public ConfigStatus getConfigStatus() {
		ConfigStatus configStatus = new ConfigStatus(this.getConfigType());
		return configStatus;
	}

	public void loadUserDefined() throws Exception {
		this._ItemNode = this.getConfigType().loadItem();

		this.initXItems();
		this.initPage();
		this.initAction();
		initMenus();
		this.initPageInfos();
		this.initCharts();
		this.initWorkflows();

		_ItemNodeXml = UXml.asXmlAll(this._ItemNode);
		this._ItemNode = null;
	}

	public String getItemNodeXml() {
		return _ItemNodeXml;
	}

	/**
	 * 初始化用户配置中的Page下的所有信息
	 * 
	 * @throws Exception
	 */
	private void initAction() throws Exception {
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "开始加载配Action");
		_UserActionItem = new UserXItem();
		Node pageNode = UXml.retNode(this._ItemNode, "Action");
		if (pageNode == null)
			return;
		int len = pageNode.getChildNodes().getLength();
		for (int i = 0; i < len; i++) {
			Node childNode = pageNode.getChildNodes().item(i);
			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			// String s1 = UXml.asXml(childNode);
			// System.out.println(s1);
			/*
			 * if (s1.indexOf("Set") >= 0) { s1 += ""; }
			 */
			UserXItemValues uxvs = this.initUserXItemValues(childNode, "action");
			this._UserActionItem.addObject(uxvs, uxvs.getName());
		}
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "结束加载配Action. (" + len + ")");

	}

	/**
	 * 初始化用户配置中的Page下的所有信息
	 * 
	 * @throws Exception
	 */
	private void initPage() throws Exception {
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "开始加载配Page");

		_UserPageItem = new UserXItem();
		Node pageNode = UXml.retNode(this._ItemNode, "Page");
		int len = pageNode.getChildNodes().getLength();
		for (int i = 0; i < len; i++) {
			Node childNode = pageNode.getChildNodes().item(i);
			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			// String s1 = UXml.asXml(childNode);
			// System.out.println(s1);
			// if (s1.indexOf("IsSplit") >= 0) {
			// s1 += "";
			// }
			UserXItemValues uxvs = this.initUserXItemValues(childNode, "page");
			if (uxvs != null)
				this._UserPageItem.addObject(uxvs, uxvs.getName());
		}
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "结束加载配Page");
	}

	/**
	 * 初始化用户配置中的XItems/XItem所有信息
	 * 
	 * @throws Exception
	 */
	private void initXItems() throws Exception {
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "开始加载配 XItems. ");
		this._UserXItems = new UserXItems();
		NodeList nl = UXml.retNodeList(this._ItemNode, "XItems/XItem");
		if (nl == null) {
			return;
		}
		for (int i = 0; i < nl.getLength(); i++) {
			this.initXItem(nl.item(i), "xitem");
		}
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "结束加载配 XItems. (" + nl.getLength() + ")");

		// find the valid code item
		for (int i = 0; i < this.getUserXItems().count(); i++) {
			UserXItem uxi = this.getUserXItems().getItem(i);
			String tag = uxi.getItem("Tag").getItem(0).getItem(0);
			if (tag.trim().equalsIgnoreCase("valid")) {
				this.validXItem = uxi;
				break;
			}
		}
	}

	/**
	 * 初始化用户配置中的Menus/Menu所有信息
	 * 
	 * @throws Exception
	 */
	private void initMenus() throws Exception {
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "开始加载配 Menus. ");
		this._UserMenuItems = new UserXItems();
		NodeList nl = UXml.retNodeList(this._ItemNode, "Menus/Menu");
		if (nl == null) {
			return;
		}
		for (int i = 0; i < nl.getLength(); i++) {
			this.initXItem(nl.item(i), "menu");
		}
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "结束加载配 Menus. (" + nl.getLength() + ")");
	}

	private void initWorkflows() throws Exception {
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "开始加载配 Workflows. ");
		this._UserWorkflows = new UserXItems();
		NodeList nl = UXml.retNodeList(this._ItemNode, "Workflows/Workflow");
		if (nl == null) {
			return;
		}
		for (int i = 0; i < nl.getLength(); i++) {
			this.initXItem(nl.item(i), "workflow");
		}
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "结束加载配 Workflows. (" + nl.getLength() + ")");
	}

	private void initPageInfos() throws Exception {
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "开始加载配 PageInfos. ");
		this._UserPageInfos = new UserXItems();
		NodeList nl = UXml.retNodeList(this._ItemNode, "PageInfos/PageInfo");
		if (nl == null) {
			return;
		}
		for (int i = 0; i < nl.getLength(); i++) {
			this.initXItem(nl.item(i), "pageinfo");
		}
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "结束加载配 PageInfos. (" + nl.getLength() + ")");
	}

	private void initCharts() throws Exception {
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "开始加载配 Charts. ");
		this._UserCharts = new UserXItems();
		NodeList nl = UXml.retNodeList(this._ItemNode, "Charts/Chart");
		if (nl == null) {
			return;
		}
		for (int i = 0; i < nl.getLength(); i++) {
			this.initXItem(nl.item(i), "chart");
		}
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "结束加载配 Charts. (" + nl.getLength() + ")");
	}

	
	/**
	 * 根据SqlSet的callName修改Action的Sql
	 * @param callName 例如：OnPageLoad Sql
	 * @param newSql 需要修改的SQL
	 * @return
	 * @throws Exception
	 */
	public boolean changeSqlSetSql(String callName, String newSql) throws Exception{
		UserXItem action = this.getUserActionItem();

		UserXItemValues actionSet = action.getItem("ActionSet");
		for (int i = 0; i < actionSet.count(); i++) {
			UserXItemValue actionItem = actionSet.getItem(i);
			 
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
			String callType = callItem1.getItem("CallType");
			String callName1 = callItem1.getItem("CallName");
			//System.out.println(callType + ", " + callName);
			if("SqlSet".equals(callType) && callName.equalsIgnoreCase(callName1) ){
				UserXItemValues sqlset = this.getUserActionItem().getItem("SqlSet");
				UserXItemValue sqlItem = sqlset.getItem(callName1);
				sqlItem.addObject(newSql, "Sql");
				return true;
			}
		}
		return false;
	}
	/**
	 * 初始化用户配置中的XItems/XItem中当前XItem信息
	 * 
	 * @param node
	 * @param type xitem,menu,workflow,chart,default
	 * @throws Exception
	 */
	public void initXItem(Node node, String type) throws Exception {
		String nodeXml = UXml.asXml(node);
		UserXItem ui = new UserXItem();
		ui.setName(UXml.retNodeValue(node, "Name"));
		if (type.equals("chart")) {
			this._UserCharts.addObject(ui, ui.getName());
		} else if (type.equals("xitem")) {
			this._UserXItems.addObject(ui, ui.getName());
		} else if (type.equals("menu")) {
			this._UserMenuItems.addObject(ui, ui.getName());
		} else if (type.equals("workflow")) {
			this._UserWorkflows.addObject(ui, ui.getName());
		} else {
			this._UserPageInfos.addObject(ui, ui.getName());
		}
		ui.setXml(nodeXml);
		NodeList nl = node.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node childNode = nl.item(i);
			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			// System.out.println(UXml.asXml(childNode));

			UserXItemValues uvs = this.initUserXItemValues(childNode, type);
			if (uvs != null) {
				ui.addObject(uvs, uvs.getName());
			}
		}
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "Init XItem ->" + ui.getName() + ", " + type);
	}

	/**
	 * 获取用户配置文件中所用的字段定义参数值<font color=blue><br>
	 * &lt;XItem&gt;<br>
	 * &lt;Tag Tag="text" /&gt;<br>
	 * &lt;Name Name="test_name" /&gt;<br>
	 * .......<br>
	 * &lt;/XItem&gt;</font>
	 * 
	 * @param node
	 * @return UserXItemValues
	 * @throws Exception
	 */
	private UserXItemValues initUserXItemValues(Node node, String type) throws Exception {
		XItemParameter itemPara = this.getItemParameter(node, type);
		if (itemPara == null) {
			return null;
		}
		UserXItemValues uxvs = new UserXItemValues();
		uxvs.setParameter(itemPara);
		uxvs.setXml(UXml.asXml(node));
		/*
		 * String aaa=UXml.asXml(node); if(aaa.indexOf("DescriptionSet")>=0){ int a=1;
		 * a++; }
		 */
		/*
		 * 集合参数，例如: <DescriptionSet> <Set Lang="zh-cn" Value="" Memo="用户名" /> <Set
		 * Lang="en-us" Value="" Memo="User Name" /> </DescriptionSet>
		 */
		NodeList childList = node.getChildNodes();
		// int m = 0;
		for (int i = 0; i < childList.getLength(); i++) {
			Node childNode = childList.item(i);
			if (childNode.getNodeType() == Node.ELEMENT_NODE && childNode.getNodeName().equalsIgnoreCase("set")) {
				UserXItemValue uxv = initUserXItemValue(childNode, itemPara, type);
				uxvs.addObject(uxv, uxv.getUniqueName());
				// m++;
			}
		}

		return uxvs;
	}

	/**
	 * 获取用户配置文件中字段的参数值, <br>
	 * 例如 &lt;MaxMinLength MaxLength="20" MinLength="8" /&gt;
	 * 
	 * @param node     Xml节点
	 * @param itemPara XItemParameter的定义
	 * @param uxvs
	 * @throws Exception
	 */
	private UserXItemValue initUserXItemValue(Node node, XItemParameter itemPara, String type) throws Exception {
		UserXItemValue uxv = new UserXItemValue();
		uxv.setXml(UXml.asXml(node));
		// EwaConfig中XItemParameter定义的Value
		for (int m = 0; m < itemPara.getValues().count(); m++) {
			XItemParameterValue v = itemPara.getValues().getItem(m);
			String name = v.getName();
			String val;
			if (v.isCDATA()) {
				Node cdataNode = UXml.retNode(node, name);
				if (cdataNode != null) {
					val = UXml.retNodeText(cdataNode);
				} else {
					val = UXml.retNodeText(node);
				}
			} else {
				val = UXml.retNodeValue(node, name);
			}
			uxv.addObject(val, name);
			if (v.isUnique()) {
				uxv.setUniqueName(val);
			}
		}
		if (itemPara.getChildren().size() > 0) {
			Iterator<String> it = itemPara.getChildren().keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				XItemParameter p = itemPara.getChildren().get(key);
				Node nodeChild = UXml.retNode(node, p.getName());
				if (nodeChild == null) {
					continue;
				}
				UserXItemValues o = this.initUserXItemValues(nodeChild, type);
				uxv.getChildren().put(o.getName(), o);
			}
		}
		return uxv;
	}

	/**
	 * 获取用户配置信息中对应的 XItemParameter定义(EwaConfig.xml)
	 * 
	 * @param node 当前节点
	 * @return
	 * @throws Exception
	 */
	private XItemParameter getItemParameter(Node node, String type) throws Exception {
		String nodeName = node.getNodeName();
		XItemParameter para = null;
		EwaConfig ec = EwaConfig.instance();
		try {
			if (type.equals("page")) {
				para = ec.getConfigPage().getParameters().getItem(nodeName);
			} else if (type.equals("xitem")) {
				para = ec.getConfigItems().getParameters().getItem(nodeName);
			} else if (type.equals("action")) {
				para = ec.getConfigAction().getParameters().getItem(nodeName);
			} else if (type.equals("menu")) {
				para = ec.getConfigMenu().getParameters().getItem(nodeName);
			} else if (type.equals("pageinfo")) {
				para = ec.getConfigMenu().getParameters().getItem(nodeName);
			} else if (type.equals("chart")) {
				para = ec.getConfigChart().getParameters().getItem(nodeName);
			} else if (type.equals("workflow")) {
				para = ec.getConfigWorkflow().getParameters().getItem(nodeName);
			}
		} catch (Exception e) {
			return null;
		}
		return para;
	}

	/**
	 * 序列化表
	 * 
	 * @return
	 * @throws IOException
	 */
	public byte[] toSerialize() throws IOException {
		// Serialize
		ByteArrayOutputStream fos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		oos.writeObject(this);
		oos.close();

		byte[] buf = fos.toByteArray();
		fos.close();

		return buf;
	}

	/**
	 * 获取用户配置信息
	 * 
	 * @return the _Values
	 */
	public UserXItems getUserXItems() {
		return _UserXItems;
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
	 * @return the _UserActionItem
	 */
	public UserXItem getUserActionItem() {
		return _UserActionItem;
	}

	/**
	 * @return the _UserPageItem
	 */
	public UserXItem getUserPageItem() {
		return _UserPageItem;
	}

	/**
	 * @return the _UserMenuItems
	 */
	public UserXItems getUserMenuItems() {
		return _UserMenuItems;
	}

	/**
	 * @return the _UserPageInfos
	 */
	public UserXItems getUserPageInfos() {
		return _UserPageInfos;
	}

	/**
	 * 获取工作流定义
	 * 
	 * @return
	 */
	public UserXItems getUserWorkflows() {
		return this._UserWorkflows;
	}

	public DebugFrames getDebugFrames() {
		return _DebugFrames;
	}

	public void setDebugFrames(DebugFrames debugFrames) {
		_DebugFrames = debugFrames;
	}

	/**
	 * @return the _UserCharts
	 */
	public UserXItems getUserCharts() {
		return _UserCharts;
	}

	/**
	 * @return the _JS_XML
	 */
	public String getJS_XML() {
		return _JS_XML;
	}

	/**
	 * @param _js_xml the _JS_XML to set
	 */
	public void setJS_XML(String _js_xml) {
		_JS_XML = _js_xml;
	}

	/**
	 * the valid code item
	 * 
	 * @return item or null
	 */
	public UserXItem getValidXItem() {
		return validXItem;
	}

}