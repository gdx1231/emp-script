package com.gdxsoft.easyweb.script.display;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.display.frame.FrameTree;
import com.gdxsoft.easyweb.script.display.items.IItem;
import com.gdxsoft.easyweb.script.template.XItem;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValues;
import com.gdxsoft.easyweb.utils.ULogic;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class TreeViewMain {
	private static Logger LOGGER = Logger.getLogger(TreeViewMain.class);
	private String _Caption;
	private String _FieldDispVal;
	private String _FieldKey;
	private String _FieldParentKey;
	private String _FieldMenuGroup;
	private String _FieldTitle; // 提示

	private String _RootId = "";
	private boolean _IsLoadByLevel;// 是否分层加载
	private ArrayList<String> _AddParasName;
	private UserConfig _UserConfig;
	private String _Lang = "zhcn";
	private String _Guid;
	private TreeOtherIcons _Icons;
	private String _AddCols; // 附加的字段

	private int _AddColsLength = 0;// 附加的长度
	private FrameTree _FrameTree;

	public static String TREE_ROOT_KEY = "EWA_TREE_ROOT";
	// 节点
	private static String NODE_TEMPLATE = "<table [TMP_NODE_MORE] EWA_T='1' EWA_MG=\"[TMPLATE_MENU_GROUP]\" "
			+ "id=\"[TMPLATE_KEY]\" "
			+ "cellspacing='0' cellpadding='0' border='0' class='ewa-tree-node ewa-tree-lvl-[LVL]'>\n"
			+ "<tr class='ewa-node-row-0'>"
			+ "<td class='[EWA_TREE_TD00] ewa-node-open-close'><div style='width:20px;'>&nbsp;</div></td>\n"
			+ "<td class='[EWA_TREE_TD01] ewa-node-icon'><div style='width:20px;'>&nbsp;</div></td>\n"
			+ "<td class='ewa-node-caption' nowrap>"
			+ "<span EWA_CMD='1' [TMP_TITLE] [TMP_ADD_PARAS] style='cursor: pointer'>[TMP_NODE_VALUE]</span></td>"
			+ "[TEMP_NODE_ADD_FIELDS]" // 附加字段替换
			+ "</tr>[TMPLATE_CHILD_NODE]</table>\n";
	// 节点的子节点容器
	private static String NODE_CHILD_TEMPLATE = "<tr class='ewa-node-row-1' style='display: [TEMP_DISPLAY];'>\n"
			+ "<td class='[EWA_TREE_TD10]'></td>\n" + "<td colspan=12>[TMP_CONTENT]</td>\n</tr>\n";
	// 头部
	private static String NODE_ROOT_TEMPLATE = "<div class='ewa-tree' id='EWA_TREE_[GUID]' style='-moz-user-select: none;'"
			+ " onselectstart='return EWA.F.FOS[\"[GUID]\"].OnSelect(event)' "
			+ "oncontextmenu='EWA.F.FOS[\"[GUID]\"].ShowMenu(event);return false;' "
			+ "onmousedown='EWA.F.FOS[\"[GUID]\"].OnMouseDown(event)' "
			+ "onmouseup='EWA.F.FOS[\"[GUID]\"].OnMouseUp(event)' "
			+ "onmousemove='EWA.F.FOS[\"[GUID]\"].OnMouseMove(event)' "
			+ "onmouseout='EWA.F.FOS[\"[GUID]\"].OnMouseOut(event)' " + "onclick='EWA.F.FOS[\"[GUID]\"].Click(event)'>"
			+ "<table id='" + TREE_ROOT_KEY + "' class='ewa-tree-header' cellspacing='0' cellpadding='0' border='0'>\n"
			+ "<tr class='ewa-node-row-0'><td></td><td class='CAPTION ewa-node-open-close'></td>"
			+ "<td class='ewa-node-caption'><span style='cursor:pointer' EWA_MG='' EWA_TREE_TOP='1'>[TMP_CAPTION_TEXT]</span>"
			+ "[TEMP_NODE_ADD_FIELDS]" // 附加字段替换
			+ "</td></tr>\n[TMPLATE_CHILD_NODE]</table>" + "<div style='display:none'>"
			+ "<input style='-moz-user-select: normal;user-select: normal;' type='text' onblur='EWA.F.FOS[\"[GUID]\"].RenameBlur(this)'"
			+ " onkeypress='EWA.F.FOS[\"[GUID]\"].RenameKeyDown(event,this)'></div>\n</div>";

	/**
	 * 初始化TreeView
	 * 
	 * @param xmlTemplateName 配置文件路径
	 * @param itemName        配置项名称
	 * @param requestValue    参数
	 * @throws Exception
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public TreeViewMain(UserConfig userConfig, String lang, String guid) throws Exception {
		this._UserConfig = userConfig;
		this._Lang = lang;
		this.initParameters();
		this._Guid = guid;

		this._RootId = this._UserConfig.getUserPageItem().getSingleValue("Tree", "RootId");
		this._Icons = new TreeOtherIcons();
		this._Icons.init(userConfig);

	}

	/**
	 * 创建节点的附加字段
	 * 
	 * @param treeClass
	 * @throws Exception
	 */
	public void initCreateAddCols(FrameTree treeClass) throws Exception {
		this._FrameTree = treeClass;
		StringBuilder sb = new StringBuilder();
		int inc = 0;
		for (int i = 0; i < this._UserConfig.getUserXItems().count(); i++) {
			UserXItem uxi = this._UserConfig.getUserXItems().getItem(i);
			XItem xItem = HtmlUtils.getXItem(uxi);
			String tag = xItem.getName();

			treeClass.addDebug(this, "Item", "Create item " + uxi.getName() + "[" + tag + "]");

			String col = this.createItemHtmlCell(uxi);
			sb.append("<td class='ewa-node-add ewa-node-col-" + uxi.getName() + "'>");
			sb.append(col);
			sb.append("</td>");

			inc++;
		}
		this._AddCols = sb.toString();
		this._AddColsLength = inc;
	}

	private String createTreeAddHtml(TreeViewNode node) throws Exception {
		if (this._FrameTree == null) {
			return "";
		}

		if (node.getData() == null) { // 根节点没有关联数据
			return this._AddCols;
		}
		DTRow row = (DTRow) node.getData();
		// 设置当前的数据行，以便 createItemHtmlCell 提取数据
		row.getTable().getRows().setCurRow(row);

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < this._UserConfig.getUserXItems().count(); i++) {
			UserXItem uxi = this._UserConfig.getUserXItems().getItem(i);
			XItem xItem = HtmlUtils.getXItem(uxi);
			String tag = xItem.getName();

			this._FrameTree.addDebug(this, "Item", "Create item " + uxi.getName() + "[" + tag + "]");

			String col = this.createItemHtmlCell(uxi);
			sb.append("<td class='ewa-node-add ewa-node-col-" + uxi.getName() + "'>");
			sb.append(col);
			sb.append("</td>");

		}

		return sb.toString();
	}

	/**
	 * 生成列表的每个单元格数据
	 * 
	 * @param uxi
	 * @return
	 * @throws Exception
	 */
	private String createItemHtmlCell(UserXItem uxi) throws Exception {
		if (this._FrameTree == null) {
			return "";
		}

		this.createCellParentStyle(uxi);

		IItem item = this._FrameTree.getHtmlClass().getItem(uxi);

		boolean haveStyle = false;
		if (uxi.getParentStyle().trim().length() > 0) {
			haveStyle = true;
		}
		// 元素父窗体样式
		String style = "style=\"" + uxi.getParentStyle();

		String itemHtml = item.createItemHtml();
		ItemValues iv = this._FrameTree.getHtmlClass().getItemValues();
		if (uxi.testName("AttributeSet")) {
			// 根据逻辑表达式去除属性
			UserXItemValues atts = uxi.getItem("AttributeSet");
			for (int i = 0; i < atts.count(); i++) {
				UserXItemValue att = atts.getItem(i);
				if (!att.testName("AttLogic")) {
					continue;
				}

				String logic = att.getItem("AttLogic").trim();
				if (logic.length() == 0) {
					continue;
				}
				logic = iv.replaceParameters(logic, false, true);
				if (ULogic.runLogic(logic)) {
					// 表达式true
					continue;
				}

				String attName = att.getItem("AttName");
				String attValue = att.getItem("AttValue");
				String exp = attName + "=\"" + attValue + "\"";
				// 去除属性
				itemHtml = itemHtml.replace(exp, "");
			}
		}

		String tag = uxi.getSingleValue("Tag");

		if (haveStyle) {
			style += "\" ";
		} else {
			style = "";
		}

		itemHtml = itemHtml.replaceFirst("!!", style.replace("a:1;display:block;a:2", ""));
		if (tag.equalsIgnoreCase("span") && style.indexOf(";a:1;") > 0) {
			String v1 = item.getValue();
			if (v1 == null || v1.trim().length() == 0) {
				return itemHtml;
			}
			String title = " title=\""
					+ item.getValue().replace("<br />", "\n").replace("\"", "&quot;").replace("<", "&lt;") + "\" ";
			if (itemHtml.indexOf("title=\"") > 0) {
				title = "";
			}
			itemHtml = itemHtml.replace("!!", title + style);
			if (itemHtml.indexOf("><span ") > 0) {
				itemHtml = itemHtml.replace("><span ", "><span " + title + style);
			} else {
				itemHtml = itemHtml.replace("><SPAN ", "><SPAN " + title + style);
			}
		} else {
		}
		return itemHtml;

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

	public String createTreeHtml(DTTable table) throws Exception {
		TreeViewNode node = this.createTreeNodes(table);
		String s1 = createHtml(node, 0);
		return s1;
	}

	public void createTreeXml(TreeViewNode node, Document doc) {
		this.createTreeNodeXml(node, doc);
	}

	public void createTreeNodeXml(TreeViewNode node, Document doc) {
		Element ele = doc.createElement("Row");
		doc.getFirstChild().appendChild(ele);
		ele.setAttribute("Key", node.getKey());
		ele.setAttribute("ParentKey", node.getParentKey());
		ele.setAttribute("Text", node.getDispVal());

		// 菜单组
		ele.setAttribute("MenuGroup", node.getMenuGroup());

		// 是否有子节点，LoadByLevel=1时起作用
		ele.setAttribute("IsMoreChildren", node.isMoreChild() ? "1" : "0");

		// 提示信息
		if (node.getTitle() != null && node.getTitle().trim().length() > 0) {
			ele.setAttribute("title", node.getTitle());
		}
		for (int i = 0; i < node.getAddParas().size(); i++) {
			String val = node.getAddParas().get(i);
			if (val != null)
				ele.setAttribute("EWA_P" + i, val);
		}
		for (int i = 0; i < node.getChildNodes().size(); i++) {
			TreeViewNode childNode = node.getChildNodes().get(i);
			this.createTreeNodeXml(childNode, doc);
		}
	}

	public void createTreeXml(DTTable dtTable, Document doc) throws Exception {
		for (int i = 0; i < dtTable.getCount(); i++) {
			this.createTreeNodeXml(dtTable.getRow(i), doc);
		}
	}

	public void createTreeNodeXml(DTRow dtRow, Document doc) throws Exception {
		Element ele = doc.createElement("Row");
		doc.getFirstChild().appendChild(ele);
		String pid = dtRow.getCell(this._FieldParentKey).getValue().toString();
		if (pid == null || pid.equals("") || pid.equals("0") || pid.equals(this._RootId)) {
			pid = "";
		}
		String key = dtRow.getCell(this._FieldKey).getValue().toString();
		String dispVal = dtRow.getCell(this._FieldDispVal).getValue().toString();
		ele.setAttribute("Key", key);
		ele.setAttribute("ParentKey", pid);
		ele.setAttribute("Text", dispVal);
		String menuGroup = "";
		if (this._FieldMenuGroup != null && this._FieldMenuGroup.trim().length() > 0) {
			menuGroup = dtRow.getCell(this._FieldMenuGroup).toString();
		}
		ele.setAttribute("MenuGroup", menuGroup);
		String more = "0";
		if (_IsLoadByLevel) {// 分层调用
			String m = dtRow.getCell("EWAMORECNT").toString();
			if (m != null) {
				int childNodes = Integer.parseInt(dtRow.getCell("EWAMORECNT").toString());
				if (childNodes > 0) {// 有更多的子节点
					more = "1";
				}
			}
		}
		ele.setAttribute("IsMoreChildren", more);

		for (int i = 0; i < _AddParasName.size(); i++) {
			String addParaName = _AddParasName.get(i);
			if (addParaName == null || addParaName.trim().length() == 0) {
			} else {
				ele.setAttribute("AddPara" + i, dtRow.getCell(addParaName).toString());
			}
		}

	}

	private void initParameters() throws Exception {
		/*
		 * <Tree> <Set Key="a1" LoadByLevel="1" ParentKey="parent" Text="name" />
		 * </Tree>
		 */
		_AddParasName = new ArrayList<String>();
		UserXItem userXitem = this._UserConfig.getUserPageItem();
		this._FieldKey = userXitem.getSingleValue("Tree", "Key");
		this._FieldDispVal = userXitem.getSingleValue("Tree", "Text");
		this._FieldParentKey = userXitem.getSingleValue("Tree", "ParentKey");
		this._FieldMenuGroup = userXitem.getSingleValue("Tree", "MenuGroup");
		this._FieldTitle = userXitem.getSingleValue("Tree", "Title");

		String s1 = userXitem.getSingleValue("Tree", "LoadByLevel");
		String addPara1 = userXitem.getSingleValue("Tree", "AddPara1");
		String addPara2 = userXitem.getSingleValue("Tree", "AddPara2");
		String addPara3 = userXitem.getSingleValue("Tree", "AddPara3");

		_AddParasName.add(addPara1);
		_AddParasName.add(addPara2);
		_AddParasName.add(addPara3);

		if (s1 != null && s1.equals("1")) {
			this._IsLoadByLevel = true;
		} else {
			this._IsLoadByLevel = false;
		}

		this._Caption = HtmlUtils.getDescription(userXitem.getItem("DescriptionSet"), "Info", this._Lang);
	}

	/**
	 * 生成Treeview节点
	 * 
	 * @throws Exception
	 * 
	 * @throws SQLException
	 */
	public TreeViewNode createTreeNodes(DTTable table) throws Exception {
		TreeViewNode nodeRoot = new TreeViewNode();
		// 初始化根节点
		nodeRoot.setDispVal(this._Caption);
		nodeRoot.setKey("");

		// 键值对应的TreeViewNode
		HashMap<String, TreeViewNode> nodesHsahMap = new HashMap<String, TreeViewNode>();
		// 按照顺序加载列表
		List<TreeViewNode> nodesList = new ArrayList<TreeViewNode>();
		nodesHsahMap.put(nodeRoot.getKey(), nodeRoot);
		nodeRoot.setParentKey(null);
		for (int i = 0; i < table.getCount(); i++) {
			DTRow row = table.getRow(i);
			this.createTreeNode(row, nodesHsahMap, nodesList);
		}

		this.bulidFatherAndChildren(nodesHsahMap, nodesList);

		nodeRoot.setKey(TreeViewNode.NODE_ROOT_KEY);
		return nodeRoot;
	}

	/**
	 * 创建父子关系
	 * 
	 * @param nodesHsahMap 键值对应的TreeViewNode
	 * @param nodesList    按照顺序加载列表
	 */
	private void bulidFatherAndChildren(HashMap<String, TreeViewNode> nodesHsahMap, List<TreeViewNode> nodesList) {
		for (int i = 0; i < nodesList.size(); i++) {
			TreeViewNode tvNode = nodesList.get(i);
			String parentKey = tvNode.getParentKey();
			if (!nodesHsahMap.containsKey(parentKey)) {
				// LOGGER.warn("孤立的节点" + tvNode);
				continue;
			}
			TreeViewNode nodeParent = nodesHsahMap.get(tvNode.getParentKey());
			if (nodeParent.getChildNodes().size() > 0) {
				TreeViewNode prevNode = nodeParent.getChildNodes().get(nodeParent.getChildNodes().size() - 1);
				tvNode.setPrevNode(prevNode); // 设置前节点
				prevNode.setNextNode(tvNode); // 前节点设置后节点，当前节点
			}
			nodeParent.getChildNodes().add(tvNode);
			nodesHsahMap.put(tvNode.getKey(), tvNode);
		}
	}

	/**
	 * 生成树的所有节点
	 * 
	 * @param row
	 * @param nodesHsahMap 键值对应的TreeViewNode
	 * @param nodesList    按照顺序加载列表
	 * @throws Exception
	 */
	private void createTreeNode(DTRow row, HashMap<String, TreeViewNode> nodesHsahMap, List<TreeViewNode> nodesList)
			throws Exception {
		Object oId = row.getCell(this._FieldParentKey).getValue();
		Object oKey = row.getCell(this._FieldKey).getValue();
		String pid = oId == null ? null : oId.toString();
		String key = oKey == null ? "null" : oKey.toString();
		if (pid != null && pid.equals(key)) {
			// 主键和父键一致的话，弃用
			LOGGER.warn("主键和父键一致，弃用" + pid);
			return;
		}
		
		
		
//		if (this._IsLoadByLevel) {
//			// 只显示第一级别，分层调用
//			if (!(pid == null || pid.toUpperCase().trim().equals(
//					this._RootId.trim().toUpperCase()))) {
//				return;
//			}
//		}

		if (pid == null || pid.equals("0") || pid.trim().equals("") || pid.equals(this._RootId)) {
			pid = "";
		}
		String dispVal = row.getCell(this._FieldDispVal).toString();
		if (dispVal == null) {
			dispVal = "[NULL]";
		}
		String menuGroup = "";
		if (this._FieldMenuGroup != null && this._FieldMenuGroup.trim().length() > 0) {
			menuGroup = row.getCell(this._FieldMenuGroup).toString();
			if (menuGroup == null) {
				menuGroup = "";
			}
		}

		String title = "";
		if (this._FieldTitle != null && this._FieldTitle.trim().length() > 0) {
			title = row.getCell(this._FieldTitle).toString();
			if (title == null) {
				title = "";
			}
		}

		String cmd = "link";
		TreeViewNode tvNode = this.createTreeNode(key, pid, dispVal, menuGroup, cmd, title, nodesHsahMap);
		// 2019-05-05 郭磊
		if(key.equals(this._RootId)) {
			LOGGER.warn("主键和主节点的值一致，弃用：" + tvNode);
			return;
		}
		nodesHsahMap.put(tvNode.getKey(), tvNode);
		nodesList.add(tvNode);

		tvNode.setData(row); // 设置关联的数据，在AddHtml中使用

		for (int i = 0; i < _AddParasName.size(); i++) {
			String addParaName = _AddParasName.get(i);
			if (addParaName == null || addParaName.trim().length() == 0) {
				tvNode.getAddParas().add(null);
			} else {
				tvNode.getAddParas().add(row.getCell(addParaName).getString());
			}
		}

		if (_IsLoadByLevel) {// 分层调用
			Object oMore = row.getCell("EWAMORECNT").getValue();
			try {
				int childNodes = oMore == null ? 0 : Integer.parseInt(oMore.toString());
				if (childNodes > 0) {// 有更多的子节点
					tvNode.setMoreChild(true);
				} else {
					tvNode.setMoreChild(false);
				}
			} catch (Exception e) {// 可能的错误
				tvNode.setMoreChild(false);
			}
		}
	}

	/**
	 * 生成树的节点
	 * 
	 * @param key          主键
	 * @param parentKey    父键
	 * @param dispVal      显示文字
	 * @param menuGroup    菜单组
	 * @param cmd          执行脚本
	 * @param title        提示信息
	 * @param nodesHsahMap
	 * @return
	 */
	private TreeViewNode createTreeNode(String key, String parentKey, String dispVal, String menuGroup, String cmd,
			String title, HashMap<String, TreeViewNode> nodesHsahMap) {
		TreeViewNode tvNode = new TreeViewNode();
		tvNode.setDispVal(dispVal);
		tvNode.setKey(key);
		tvNode.setParentKey(parentKey);
		tvNode.setJavaScriptCmd(cmd);
		tvNode.setMenuGroup(menuGroup);
		tvNode.setTitle(title);

		// 根据表达式设置附加的图标
		// testAddIcon(tvNode);

		// 检查父节点
//		TreeViewNode nodeParent = nodesHsahMap.get(tvNode.getParentKey());
//		if (nodeParent != null) {// 父节点存在,否则丢弃
//			if (nodeParent.getChildNodes().size() > 0) {
//				TreeViewNode prevNode = nodeParent.getChildNodes().get(nodeParent.getChildNodes().size() - 1);
//				tvNode.setPrevNode(prevNode); // 设置前节点
//				prevNode.setNextNode(tvNode); // 前节点设置后节点，当前节点
//			}
//			nodeParent.getChildNodes().add(tvNode);
//			nodesHsahMap.put(tvNode.getKey(), tvNode);
//		}
		return tvNode;
	}

	private String createHtml(TreeViewNode node, int lvl) {
		StringBuilder sb = new StringBuilder();
		if (node.getKey().equals(TreeViewNode.NODE_ROOT_KEY)) {// 根节点
			sb.append(NODE_ROOT_TEMPLATE.replace("[GUID]", this._Guid));
			replaceStr(sb, "[TMP_CAPTION_TEXT]", node.getDispVal());
		} else {
			sb.append(createNodeHtml(node));
		}
		StringBuilder sbChildren = new StringBuilder();
		for (int i = 0; i < node.getChildNodes().size(); i++) {
			int new_lvl = lvl + 1;
			// 递归调用创建html方法
			String childenTreeHtml = createHtml(node.getChildNodes().get(i), new_lvl);
			sbChildren.append(childenTreeHtml);
		}
		String rep1 = "";
		if (node.getChildNodes().size() > 0) {
			StringBuilder childTemplate = new StringBuilder();
			childTemplate.append(NODE_CHILD_TEMPLATE);
			if (node.getParentKey() == null) {
				replaceStr(childTemplate, "[TEMP_DISPLAY]", "");
			} else {
				replaceStr(childTemplate, "[TEMP_DISPLAY]", "none");
			}
			if (node.getChildNodes().size() > 0 && node.getParentKey() != null) {
				if (node.getNextNode() == null) {// 最后一个节点，背景直线设为空
					replaceStr(childTemplate, "[EWA_TREE_TD10]", "TD10A");
				} else {// 设置节点的背景直线
					replaceStr(childTemplate, "[EWA_TREE_TD10]", "TD10B");
				}
			} else {
				replaceStr(childTemplate, "[EWA_TREE_TD10]", "TD10A");
			}
			replaceStr(childTemplate, "[TMP_CONTENT]", sbChildren.toString());
			rep1 = childTemplate.toString();
		}
		replaceStr(sb, "[TMPLATE_CHILD_NODE]", rep1);
		replaceStr(sb, "[LVL]", lvl + "");

		if (lvl == 0) {
			lvl += 0;
		}
		String addColsHtml;
		try {
			addColsHtml = this.createTreeAddHtml(node);
		} catch (Exception e) {
			LOGGER.error(e);
			addColsHtml = "";
		}

		if (addColsHtml == null) {
			addColsHtml = "";
		}

		replaceStr(sb, "[TEMP_NODE_ADD_FIELDS]", addColsHtml);
		return sb.toString();
	}

	private String createNodeHtml(TreeViewNode node) {
		StringBuilder sb = new StringBuilder();

		sb.append(NODE_TEMPLATE);

		// 附加参数
		String sAddParas = "";
		for (int i = 0; i < node.getAddParas().size(); i++) {
			String val = node.getAddParas().get(i);
			if (val != null)
				sAddParas += " EWA_P" + i + "=\"" + Utils.textToInputValue(val) + "\"";
		}
		replaceStr(sb, "[TMP_ADD_PARAS]", sAddParas);

		// 提示信息
		if (node.getTitle() != null && node.getTitle().trim().length() > 0) {
			String title = " title=\"" + node.getTitle() + "\" ";
			replaceStr(sb, "[TMP_TITLE]", title);
		} else {
			replaceStr(sb, "[TMP_TITLE]", "");

		}
		// 主键（id）
		replaceStr(sb, "[TMPLATE_KEY]", node.getKey());

		replaceStr(sb, "[TMPLATE_MENU_GROUP]", node.getMenuGroup());
		// 替换节点的显示文字
		replaceStr(sb, "[TMP_NODE_VALUE]", node.getDispVal());

		// 替换节点的显示图标，文字边上的
		String icon = this._Icons.test(node);
		if (icon.length() == 0) {
			replaceStr(sb, "[EWA_TREE_TD01]", "TD01A");
		} else {
			replaceStr(sb, "[EWA_TREE_TD01]", "F_" + this._Guid + "_" + icon + "_A");
		}
		// 更多子节点调用
		if (this._IsLoadByLevel && node.getChildNodes().size() == 0 && node.isMoreChild()) {
			replaceStr(sb, "[TMP_NODE_MORE]", "EWA_TREE_MORE='1'");
			// 替换Tree的打开，关闭图标
			replaceStr(sb, "[EWA_TREE_TD00]", "TD00A");
		} else {
			replaceStr(sb, "[TMP_NODE_MORE]", "");
			if (node.getChildNodes().size() == 0) {
				if (node.getNextNode() == null) {
					replaceStr(sb, "[EWA_TREE_TD00]", "TD00D");
				} else {
					replaceStr(sb, "[EWA_TREE_TD00]", "TD00C");
				}
			} else {
				replaceStr(sb, "[EWA_TREE_TD00]", "TD00A");
			}
		}
		return sb.toString();
	}

	private void replaceStr(StringBuilder sb, String find, String repStr) {
		if (repStr == null || find == null)
			return;
		int m = sb.indexOf(find);
		if (m > 0) {
			sb.replace(m, m + find.length(), repStr);
		}
	}

	/**
	 * @return the _Icons
	 */
	public TreeOtherIcons getIcons() {
		return _Icons;
	}

	public String getRootId() {
		return _RootId;
	}

	/**
	 * 外部设置RootId，当指定URL参数EWA_TREE_ROOT_ID时候调用
	 * 
	 * @param rootId
	 */
	public void setRootId(String rootId) {
		_RootId = rootId;
	}

	public boolean isLoadByLevel() {
		return _IsLoadByLevel;
	}

	public void setIsLoadByLevel(boolean isLoadByLevel) {
		_IsLoadByLevel = isLoadByLevel;
	}

	/**
	 * 附加字段的表达式
	 * 
	 * @return
	 */
	public String getAddCols() {
		return _AddCols;
	}

	/**
	 * 附加字段的个数
	 * 
	 * @return
	 */
	public int getAddColsLength() {
		return _AddColsLength;
	}
}
