package com.gdxsoft.easyweb.define;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.conf.ConfScriptPath;
import com.gdxsoft.easyweb.conf.ConfScriptPaths;
import com.gdxsoft.easyweb.utils.UXml;

/**
 * XML 配置管理工具 — XML 操作层
 * 
 * 功能：
 * 1. XItem 管理 — 读取/修改 XML 中 EasyWebTemplate 的 XItem 定义
 * 2. SQL + ActionSet 管理 — 读取/修改 XML 中 Action 的 SqlSet 和 ActionSet
 * 
 * 直接操作 XML 文件，不涉及数据库表。
 * 此类仅包含 XML 操作逻辑，不含 Servlet/HTTP 相关代码。
 * 
 * 参数约定：
 * xmlPath   — XML 文件路径（如 /ewa/admin.xml）
 * itemName  — EasyWebTemplate Name（如 ADM_USER.Frame.ChangePWD）
 * xitemName — XItem Name（如 ADM_PWD）
 * sqlName   — SqlSet Name（如 OnPagePost SQL）
 * 
 * @author guolei
 * @date 2026-05-26
 */
public class UpdateCfgXml {
	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateCfgXml.class);

	/** 当前操作员（用于日志/审计） */
	private String operator;

	/**
	 * 设置当前操作员
	 */
	public void setOperator(String loginId) {
		this.operator = loginId;
	}

	// ========================================================================
	// 1. 扫描 XML 配置文件列表
	// ========================================================================

	/**
	 * 列出所有可编辑的 XML 文件
	 */
	public JSONObject listXmlFiles() {
		JSONObject result = new JSONObject();
		JSONArray arr = new JSONArray();

		try {
			ConfScriptPaths sps = ConfScriptPaths.getInstance();
			if (sps == null || sps.getLst() == null) {
				return result.put("success", true).put("data", arr);
			}

			for (ConfScriptPath sp : sps.getLst()) {
				if (sp.isResources()) continue;

				if (sp.isJdbc()) {
					try {
						UpdateXmlImpl ux = new UpdateXmlImpl(sp);
						String fullXml = ux.getDocXml();
						if (fullXml != null && !fullXml.isEmpty()) {
							JSONObject obj = new JSONObject();
							obj.put("xmlName", sp.getName());
							obj.put("xmlPath", sp.getPath());
							obj.put("isJdbc", true);

							Document doc = UXml.asDocument(fullXml);
							NodeList items = doc.getElementsByTagName("EasyWebTemplate");
							obj.put("itemCount", items.getLength());
							arr.put(obj);
						}
					} catch (Exception e) {
						LOGGER.debug("JDBC XML 加载失败: " + sp.getName(), e);
					}
				} else {
					String basePath = sp.getPath();
					File baseDir = new File(basePath);
					if (baseDir.exists() && baseDir.isDirectory()) {
						scanXmlFilesRecursive(baseDir, sp, arr, "");
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("扫描 XML 文件失败", e);
		}

		result.put("success", true).put("data", arr);
		return result;
	}

	private void scanXmlFilesRecursive(File dir, ConfScriptPath sp, JSONArray arr, String relativePath) {
		File[] files = dir.listFiles();
		if (files == null) return;

		for (File f : files) {
			if (f.isDirectory()) {
				if (f.getName().startsWith(".") || f.getName().equals("recycle")) continue;
				scanXmlFilesRecursive(f, sp, arr, relativePath + f.getName() + "/");
			} else if (f.getName().endsWith(".xml")) {
				try {
					String xmlPath = "/" + relativePath + f.getName();
					IUpdateXml ux = ConfigUtils.getUpdateXml(xmlPath);
					if (ux == null) continue;

					JSONObject obj = new JSONObject();
					obj.put("xmlName", xmlPath);
					obj.put("xmlPath", f.getAbsolutePath());
					obj.put("isJdbc", false);

					String fullXml = ux.getDocXml();
					if (fullXml != null && !fullXml.isEmpty()) {
						Document doc = UXml.asDocument(fullXml);
						NodeList items = doc.getElementsByTagName("EasyWebTemplate");
						obj.put("itemCount", items.getLength());

						Map<String, Integer> frameTypeCounts = new LinkedHashMap<>();
						for (int i = 0; i < items.getLength(); i++) {
							Element itemElem = (Element) items.item(i);
							String name = itemElem.getAttribute("Name");
							String[] parts = name.split("\\.");
							String type = "Frame";
							for (String part : parts) {
								if ("LF".equalsIgnoreCase(part)) { type = "ListFrame"; break; }
								if ("F".equalsIgnoreCase(part))  { type = "Frame"; break; }
								if ("T".equalsIgnoreCase(part))  { type = "Tree"; break; }
							}
							frameTypeCounts.put(type, frameTypeCounts.getOrDefault(type, 0) + 1);
						}
						obj.put("frameTypes", new JSONObject(frameTypeCounts));
					}

					arr.put(obj);
				} catch (Exception e) {
					LOGGER.debug("XML 加载失败: " + f.getName(), e);
				}
			}
		}
	}

	// ========================================================================
	// 2. 列出 XML 中的 EasyWebTemplate 列表
	// ========================================================================

	/**
	 * 列出指定 XML 中的所有 EasyWebTemplate
	 */
	public JSONObject listItems(String xmlPath) {
		JSONObject result = new JSONObject();
		JSONArray arr = new JSONArray();

		IUpdateXml ux = getUpdateXml(xmlPath);
		if (ux == null) {
			return result.put("success", false).put("message", "找不到 XML 配置: " + xmlPath);
		}

		try {
			String fullXml = ux.getDocXml();
			Document doc = UXml.asDocument(fullXml);
			NodeList items = doc.getElementsByTagName("EasyWebTemplate");

			for (int i = 0; i < items.getLength(); i++) {
				Element itemElem = (Element) items.item(i);
				JSONObject obj = new JSONObject();
				String name = itemElem.getAttribute("Name");
				obj.put("name", name);
				obj.put("index", i);

				NodeList descNodes = itemElem.getElementsByTagName("DescriptionSet");
				if (descNodes.getLength() > 0) {
					Element descSet = (Element) descNodes.item(0);
					NodeList setNodes = descSet.getElementsByTagName("Set");
					for (int j = 0; j < setNodes.getLength(); j++) {
						Element setElem = (Element) setNodes.item(j);
						String lang = setElem.getAttribute("Lang");
						String info = setElem.getAttribute("Info");
						if ("zhcn".equals(lang)) {
							obj.put("descZh", info);
						} else if ("enus".equals(lang)) {
							obj.put("descEn", info);
						}
					}
				}

				NodeList xitems = itemElem.getElementsByTagName("XItem");
				obj.put("xitemCount", xitems.getLength());

				NodeList sqlSets = itemElem.getElementsByTagName("SqlSet");
				int sqlCount = 0;
				for (int j = 0; j < sqlSets.getLength(); j++) {
					sqlCount += ((Element) sqlSets.item(j)).getElementsByTagName("Set").getLength();
				}
				obj.put("sqlCount", sqlCount);

				arr.put(obj);
			}
		} catch (Exception e) {
			LOGGER.error("列出 Items 失败: " + xmlPath, e);
			return result.put("success", false).put("message", e.getMessage());
		}

		result.put("success", true).put("data", arr);
		return result;
	}

	// ========================================================================
	// 3. XItem 管理
	// ========================================================================

	/**
	 * 列出指定 Template 中的所有 XItem
	 */
	public JSONObject listXItems(String xmlPath, String itemName) {
		JSONObject result = new JSONObject();
		JSONArray arr = new JSONArray();

		IUpdateXml ux = getUpdateXml(xmlPath);
		if (ux == null) {
			return result.put("success", false).put("message", "找不到 XML 配置: " + xmlPath);
		}

		try {
			String itemXml = ux.queryItemXml(itemName);
			if (itemXml == null || itemXml.isEmpty()) {
				return result.put("success", false).put("message", "找不到 Item: " + itemName);
			}

			Document doc = UXml.asDocument(itemXml);
			NodeList xitemNodes = doc.getElementsByTagName("XItem");

			for (int i = 0; i < xitemNodes.getLength(); i++) {
				Element xitemElem = (Element) xitemNodes.item(i);
				JSONObject obj = new JSONObject();
				obj.put("index", i);

				String name = xitemElem.getAttribute("Name");
				obj.put("name", name);
				obj.put("tag", getChildAttr(xitemElem, "Tag", "Tag"));
				obj.put("descZh", getDescription(xitemElem, "zhcn"));
				obj.put("descEn", getDescription(xitemElem, "enus"));

				Element dataItem = getFirstChild(xitemElem, "DataItem");
				if (dataItem != null) {
					Element setData = getFirstChild(dataItem, "Set");
					if (setData != null) {
						obj.put("dataField", setData.getAttribute("DataField"));
						obj.put("dataType", setData.getAttribute("DataType"));
						obj.put("isEncrypt", setData.getAttribute("IsEncrypt"));
					}
				}

				obj.put("isMustInput", getChildAttr(xitemElem, "IsMustInput", "IsMustInput"));

				Element orderSearch = getFirstChild(xitemElem, "OrderSearch");
				if (orderSearch != null) {
					Element osSet = getFirstChild(orderSearch, "Set");
					if (osSet != null) {
						obj.put("isOrder", osSet.getAttribute("IsOrder"));
						obj.put("searchType", osSet.getAttribute("SearchType"));
					}
				}

				Element maxMin = getFirstChild(xitemElem, "MaxMinLength");
				if (maxMin != null) {
					Element mmSet = getFirstChild(maxMin, "Set");
					if (mmSet != null) {
						obj.put("maxLength", mmSet.getAttribute("MaxLength"));
						obj.put("minLength", mmSet.getAttribute("MinLength"));
					}
				}

				arr.put(obj);
			}
		} catch (Exception e) {
			LOGGER.error("列出 XItems 失败: " + itemName, e);
			return result.put("success", false).put("message", e.getMessage());
		}

		result.put("success", true).put("data", arr);
		return result;
	}

	/**
	 * 获取单个 XItem 的完整详情（用于编辑）
	 */
	public JSONObject getXItem(String xmlPath, String itemName, String xitemName) {
		JSONObject result = new JSONObject();

		IUpdateXml ux = getUpdateXml(xmlPath);
		if (ux == null) {
			return result.put("success", false).put("message", "找不到 XML 配置: " + xmlPath);
		}

		try {
			String itemXml = ux.queryItemXml(itemName);
			Document doc = UXml.asDocument(itemXml);
			NodeList xitemNodes = doc.getElementsByTagName("XItem");

			Element targetXItem = null;
			for (int i = 0; i < xitemNodes.getLength(); i++) {
				Element elem = (Element) xitemNodes.item(i);
				if (elem.getAttribute("Name").equals(xitemName)) {
					targetXItem = elem;
					break;
				}
			}

			if (targetXItem == null) {
				return result.put("success", false).put("message", "找不到 XItem: " + xitemName);
			}

			JSONObject detail = new JSONObject();
			detail.put("name", xitemName);

			NodeList children = targetXItem.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				if (child.getNodeType() != Node.ELEMENT_NODE) continue;
				Element childElem = (Element) child;
				String nodeName = childElem.getNodeName();

				if ("DescriptionSet".equals(nodeName)) {
					NodeList setNodes = childElem.getElementsByTagName("Set");
					for (int j = 0; j < setNodes.getLength(); j++) {
						Element setElem = (Element) setNodes.item(j);
						String lang = setElem.getAttribute("Lang");
						detail.put("desc_" + lang, setElem.getAttribute("Info"));
					}
					continue;
				}

				Element setElem = getFirstChild(childElem, "Set");
				if (setElem != null) {
					JSONObject attrs = new JSONObject();
					org.w3c.dom.NamedNodeMap attrMap = setElem.getAttributes();
					for (int j = 0; j < attrMap.getLength(); j++) {
						org.w3c.dom.Node attr = attrMap.item(j);
						attrs.put(attr.getNodeName(), attr.getNodeValue());
					}
					detail.put(nodeName, attrs);
				}
			}

			result.put("success", true).put("data", detail);
		} catch (Exception e) {
			LOGGER.error("获取 XItem 详情失败: " + xitemName, e);
			result.put("success", false).put("message", e.getMessage());
		}

		return result;
	}

	/**
	 * 保存 XItem — 新增或修改
	 */
	public JSONObject saveXItem(String xmlPath, String itemName, UpdateCfgXItemData data) {
		JSONObject result = new JSONObject();

		IUpdateXml ux = getUpdateXml(xmlPath);
		if (ux == null) {
			return result.put("success", false).put("message", "找不到 XML 配置: " + xmlPath);
		}

		try {
			String itemXml = ux.queryItemXml(itemName);
			Document doc = UXml.asDocument(itemXml);
			Element root = doc.getDocumentElement();

			Element xitemsContainer = getFirstChild(root, "XItems");
			if (xitemsContainer == null) {
				xitemsContainer = doc.createElement("XItems");
				root.appendChild(xitemsContainer);
			}

			Element targetXItem = null;
			if (data.oldName != null && !data.oldName.isEmpty()) {
				NodeList xitemNodes = xitemsContainer.getElementsByTagName("XItem");
				for (int i = 0; i < xitemNodes.getLength(); i++) {
					Element elem = (Element) xitemNodes.item(i);
					if (elem.getAttribute("Name").equals(data.oldName)) {
						targetXItem = elem;
						break;
					}
				}
			}

			if (targetXItem == null) {
				targetXItem = doc.createElement("XItem");
				xitemsContainer.appendChild(targetXItem);
			}

			targetXItem.setAttribute("Name", data.name);
			while (targetXItem.hasChildNodes()) {
				targetXItem.removeChild(targetXItem.getFirstChild());
			}

			// 1. Tag
			if (data.tag != null && !data.tag.isEmpty()) {
				Element tagElem = doc.createElement("Tag");
				Element tagSet = doc.createElement("Set");
				tagSet.setAttribute("Tag", data.tag);
				tagElem.appendChild(tagSet);
				targetXItem.appendChild(tagElem);
			}

			// 2. Name
			Element nameElem = doc.createElement("Name");
			Element nameSet = doc.createElement("Set");
			nameSet.setAttribute("Name", data.name);
			nameElem.appendChild(nameSet);
			targetXItem.appendChild(nameElem);

			// 3. DescriptionSet
			if (data.descZh != null || data.descEn != null) {
				Element descSetElem = doc.createElement("DescriptionSet");
				if (data.descZh != null) {
					Element zhSet = doc.createElement("Set");
					zhSet.setAttribute("Lang", "zhcn");
					zhSet.setAttribute("Info", data.descZh);
					zhSet.setAttribute("Memo", "");
					descSetElem.appendChild(zhSet);
				}
				if (data.descEn != null) {
					Element enSet = doc.createElement("Set");
					enSet.setAttribute("Lang", "enus");
					enSet.setAttribute("Info", data.descEn);
					enSet.setAttribute("Memo", "");
					descSetElem.appendChild(enSet);
				}
				targetXItem.appendChild(descSetElem);
			}

			// 4. DataItem
			if (data.dataField != null || data.dataType != null) {
				Element dataItemElem = doc.createElement("DataItem");
				Element dataSet = doc.createElement("Set");
				if (data.dataField != null) dataSet.setAttribute("DataField", data.dataField);
				if (data.dataType != null) dataSet.setAttribute("DataType", data.dataType);
				if (data.isEncrypt != null && !data.isEncrypt.isEmpty()) dataSet.setAttribute("IsEncrypt", data.isEncrypt);
				if (data.valid != null && !data.valid.isEmpty()) dataSet.setAttribute("Valid", data.valid);
				if (data.format != null && !data.format.isEmpty()) dataSet.setAttribute("Format", data.format);
				dataItemElem.appendChild(dataSet);
				targetXItem.appendChild(dataItemElem);
			}

			// 5. IsMustInput
			if (data.isMustInput != null && !data.isMustInput.isEmpty()) {
				Element mustElem = doc.createElement("IsMustInput");
				Element mustSet = doc.createElement("Set");
				mustSet.setAttribute("IsMustInput", data.isMustInput);
				mustElem.appendChild(mustSet);
				targetXItem.appendChild(mustElem);
			}

			// 6. MaxMinLength
			if (data.maxLength != null || data.minLength != null) {
				Element mmElem = doc.createElement("MaxMinLength");
				Element mmSet = doc.createElement("Set");
				if (data.maxLength != null && !data.maxLength.isEmpty()) mmSet.setAttribute("MaxLength", data.maxLength);
				if (data.minLength != null && !data.minLength.isEmpty()) mmSet.setAttribute("MinLength", data.minLength);
				mmElem.appendChild(mmSet);
				targetXItem.appendChild(mmElem);
			}

			// 7. MaxMinValue
			if (data.maxValue != null || data.minValue != null) {
				Element mvElem = doc.createElement("MaxMinValue");
				Element mvSet = doc.createElement("Set");
				if (data.maxValue != null && !data.maxValue.isEmpty()) mvSet.setAttribute("MaxValue", data.maxValue);
				if (data.minValue != null && !data.minValue.isEmpty()) mvSet.setAttribute("MinValue", data.minValue);
				mvElem.appendChild(mvSet);
				targetXItem.appendChild(mvElem);
			}

			// 8. OrderSearch
			if (data.isOrder != null || data.searchType != null) {
				Element osElem = doc.createElement("OrderSearch");
				Element osSet = doc.createElement("Set");
				if (data.isOrder != null && !data.isOrder.isEmpty()) osSet.setAttribute("IsOrder", data.isOrder);
				if (data.searchType != null && !data.searchType.isEmpty()) osSet.setAttribute("SearchType", data.searchType);
				if (data.orderExp != null && !data.orderExp.isEmpty()) osSet.setAttribute("OrderExp", data.orderExp);
				osElem.appendChild(osSet);
				targetXItem.appendChild(osElem);
			}

			// 9. Style
			if (data.style != null && !data.style.isEmpty()) {
				Element styleElem = doc.createElement("Style");
				Element styleSet = doc.createElement("Set");
				styleSet.setAttribute("Style", data.style);
				styleElem.appendChild(styleSet);
				targetXItem.appendChild(styleElem);
			}

			// 10. ParentStyle
			if (data.parentStyle != null && !data.parentStyle.isEmpty()) {
				Element psElem = doc.createElement("ParentStyle");
				Element psSet = doc.createElement("Set");
				psSet.setAttribute("ParentStyle", data.parentStyle);
				psElem.appendChild(psSet);
				targetXItem.appendChild(psElem);
			}

			// 11. XStyle
			if (data.xstyle != null && !data.xstyle.isEmpty()) {
				Element xsElem = doc.createElement("XStyle");
				Element xsSet = doc.createElement("Set");
				xsSet.setAttribute("XStyle", data.xstyle);
				xsElem.appendChild(xsSet);
				targetXItem.appendChild(xsElem);
			}

			// 12. EventSet
			if (data.eventName != null || data.eventType != null) {
				Element eventElem = doc.createElement("EventSet");
				Element eventSet = doc.createElement("Set");
				if (data.eventName != null && !data.eventName.isEmpty()) eventSet.setAttribute("EventName", data.eventName);
				if (data.eventType != null && !data.eventType.isEmpty()) eventSet.setAttribute("EventType", data.eventType);
				if (data.eventValue != null && !data.eventValue.isEmpty()) eventSet.setAttribute("EventValue", data.eventValue);
				eventElem.appendChild(eventSet);
				targetXItem.appendChild(eventElem);
			}

			// 13. CallAction
			if (data.callAction != null && !data.callAction.isEmpty()) {
				Element caElem = doc.createElement("CallAction");
				Element caSet = doc.createElement("Set");
				caSet.setAttribute("Action", data.callAction);
				if (data.confirmInfo != null && !data.confirmInfo.isEmpty()) caSet.setAttribute("ConfirmInfo", data.confirmInfo);
				caElem.appendChild(caSet);
				targetXItem.appendChild(caElem);
			}

			// 14. List
			if (data.listSql != null || data.listValueList != null) {
				Element listElem = doc.createElement("List");
				Element listSet = doc.createElement("Set");
				if (data.listSql != null && !data.listSql.isEmpty()) listSet.setAttribute("Sql", data.listSql);
				if (data.listValueList != null && !data.listValueList.isEmpty()) listSet.setAttribute("ValueList", data.listValueList);
				if (data.listDisplayList != null && !data.listDisplayList.isEmpty()) listSet.setAttribute("DisplayList", data.listDisplayList);
				listElem.appendChild(listSet);
				targetXItem.appendChild(listElem);
			}

			// 15. UserSet
			if (data.userSet != null && !data.userSet.isEmpty()) {
				Element usElem = doc.createElement("UserSet");
				Element usSet = doc.createElement("Set");
				usSet.setAttribute("UserSet", data.userSet);
				usElem.appendChild(usSet);
				targetXItem.appendChild(usElem);
			}

			String modifiedXml = UXml.asXml(doc);
			boolean saved = ux.saveXml(itemName, modifiedXml);
			LOGGER.info("[UpdateCfgXml] saveXItem by={}, xml={}, item={}, xitem={}, result={}",
					operator, xmlPath, itemName, data.name, saved);
			result.put("success", saved).put("message", saved ? "保存成功" : "保存失败");
		} catch (Exception e) {
			LOGGER.error("保存 XItem 失败: " + data.name, e);
			result.put("success", false).put("message", "保存失败: " + e.getMessage());
		}

		return result;
	}

	/**
	 * 删除 XItem
	 */
	public JSONObject deleteXItem(String xmlPath, String itemName, String xitemName) {
		JSONObject result = new JSONObject();

		IUpdateXml ux = getUpdateXml(xmlPath);
		if (ux == null) {
			return result.put("success", false).put("message", "找不到 XML 配置: " + xmlPath);
		}

		try {
			String itemXml = ux.queryItemXml(itemName);
			Document doc = UXml.asDocument(itemXml);
			NodeList xitemNodes = doc.getElementsByTagName("XItem");

			for (int i = 0; i < xitemNodes.getLength(); i++) {
				Element elem = (Element) xitemNodes.item(i);
				if (elem.getAttribute("Name").equals(xitemName)) {
					elem.getParentNode().removeChild(elem);
					break;
				}
			}

			String modifiedXml = UXml.asXml(doc);
			boolean saved = ux.saveXml(itemName, modifiedXml);
			LOGGER.info("[UpdateCfgXml] deleteXItem by={}, xml={}, item={}, xitem={}, result={}",
					operator, xmlPath, itemName, xitemName, saved);
			result.put("success", saved).put("message", saved ? "删除成功" : "删除失败");
		} catch (Exception e) {
			LOGGER.error("删除 XItem 失败: " + xitemName, e);
			result.put("success", false).put("message", "删除失败: " + e.getMessage());
		}

		return result;
	}

	/**
	 * 移动 XItem 顺序（上移/下移）
	 */
	public JSONObject moveXItem(String xmlPath, String itemName, String xitemName, String direction) {
		JSONObject result = new JSONObject();

		IUpdateXml ux = getUpdateXml(xmlPath);
		if (ux == null) {
			return result.put("success", false).put("message", "找不到 XML 配置: " + xmlPath);
		}

		try {
			String itemXml = ux.queryItemXml(itemName);
			Document doc = UXml.asDocument(itemXml);
			Element xitemsContainer = getFirstChild(doc.getDocumentElement(), "XItems");

			if (xitemsContainer == null) {
				return result.put("success", false).put("message", "XItems 容器不存在");
			}

			NodeList xitemNodes = xitemsContainer.getElementsByTagName("XItem");
			List<Element> xitemList = new ArrayList<>();
			Element target = null;
			int targetIndex = -1;

			for (int i = 0; i < xitemNodes.getLength(); i++) {
				Element elem = (Element) xitemNodes.item(i);
				xitemList.add(elem);
				if (elem.getAttribute("Name").equals(xitemName)) {
					target = elem;
					targetIndex = i;
				}
			}

			if (target == null) {
				return result.put("success", false).put("message", "找不到 XItem: " + xitemName);
			}

			int swapIndex = "up".equals(direction) ? targetIndex - 1 : targetIndex + 1;
			if (swapIndex < 0 || swapIndex >= xitemList.size()) {
				return result.put("success", false).put("message", "无法移动：已在最" + ("up".equals(direction) ? "上" : "下") + "端");
			}

			while (xitemsContainer.hasChildNodes()) {
				xitemsContainer.removeChild(xitemsContainer.getFirstChild());
			}

			for (int i = 0; i < xitemList.size(); i++) {
				if (i == swapIndex) {
					xitemsContainer.appendChild(target);
				} else if (i == targetIndex) {
					xitemsContainer.appendChild(xitemList.get(swapIndex));
				} else {
					xitemsContainer.appendChild(xitemList.get(i));
				}
			}

			String modifiedXml = UXml.asXml(doc);
			boolean saved = ux.saveXml(itemName, modifiedXml);
			LOGGER.info("[UpdateCfgXml] moveXItem by={}, xml={}, item={}, xitem={}, direction={}, result={}",
					operator, xmlPath, itemName, xitemName, direction, saved);
			result.put("success", saved).put("message", saved ? "移动成功" : "移动失败");
		} catch (Exception e) {
			LOGGER.error("移动 XItem 失败: " + xitemName, e);
			result.put("success", false).put("message", "移动失败: " + e.getMessage());
		}

		return result;
	}

	// ========================================================================
	// 4. Action / SqlSet / ScriptSet 管理
	// ========================================================================

	/**
	 * 列出指定 Template 中的所有 Action + SqlSet + ScriptSet
	 */
	public JSONObject listActions(String xmlPath, String itemName) {
		JSONObject result = new JSONObject();

		IUpdateXml ux = getUpdateXml(xmlPath);
		if (ux == null) {
			return result.put("success", false).put("message", "找不到 XML 配置: " + xmlPath);
		}

		try {
			String itemXml = ux.queryItemXml(itemName);
			Document doc = UXml.asDocument(itemXml);
			JSONObject data = new JSONObject();

			JSONArray actions = new JSONArray();
			Element actionElem = getFirstChild(doc.getDocumentElement(), "Action");
			if (actionElem != null) {
				// 解析 ActionSet
				Element actionSetElem = getFirstChild(actionElem, "ActionSet");
				if (actionSetElem != null) {
					NodeList actionSetNodes = actionSetElem.getElementsByTagName("Set");
					for (int i = 0; i < actionSetNodes.getLength(); i++) {
						Element asElem = (Element) actionSetNodes.item(i);
						JSONObject actionObj = new JSONObject();
						actionObj.put("type", asElem.getAttribute("Type"));

						JSONArray calls = new JSONArray();
						Element callSetElem = getFirstChild(asElem, "CallSet");
						if (callSetElem != null) {
							NodeList callNodes = callSetElem.getElementsByTagName("Set");
							for (int j = 0; j < callNodes.getLength(); j++) {
								Element callElem = (Element) callNodes.item(j);
								JSONObject callObj = new JSONObject();
								callObj.put("callName", callElem.getAttribute("CallName"));
								callObj.put("callType", callElem.getAttribute("CallType"));
								callObj.put("test", callElem.getAttribute("Test"));
								calls.put(callObj);
							}
						}
						actionObj.put("calls", calls);
						actions.put(actionObj);
					}
				}

				// 解析 SqlSet
				JSONArray sqlSets = new JSONArray();
				Element sqlSetElem = getFirstChild(actionElem, "SqlSet");
				if (sqlSetElem != null) {
					NodeList sqlNodes = sqlSetElem.getElementsByTagName("Set");
					for (int i = 0; i < sqlNodes.getLength(); i++) {
						Element sqlElem = (Element) sqlNodes.item(i);
						JSONObject sqlObj = new JSONObject();
						sqlObj.put("name", sqlElem.getAttribute("Name"));
						sqlObj.put("sqlType", sqlElem.getAttribute("SqlType"));
						sqlObj.put("transType", sqlElem.getAttribute("TransType"));

						JSONArray referencedBy = new JSONArray();
						Element actionSetElem2 = getFirstChild(actionElem, "ActionSet");
						if (actionSetElem2 != null) {
							NodeList callSetNodes2 = actionSetElem2.getElementsByTagName("Set");
							for (int j = 0; j < callSetNodes2.getLength(); j++) {
								Element csElem = (Element) callSetNodes2.item(j);
								Element csCallSet = getFirstChild(csElem, "CallSet");
								if (csCallSet != null) {
									NodeList csCalls = csCallSet.getElementsByTagName("Set");
									for (int k = 0; k < csCalls.getLength(); k++) {
										Element ccElem = (Element) csCalls.item(k);
										if (sqlElem.getAttribute("Name").equals(ccElem.getAttribute("CallName"))) {
											referencedBy.put(csElem.getAttribute("Type"));
										}
									}
								}
							}
						}
						sqlObj.put("referencedBy", referencedBy);

						Element sqlContent = getFirstChild(sqlElem, "Sql");
						if (sqlContent != null) {
							String sql = sqlContent.getTextContent();
							sqlObj.put("sqlPreview", sql.length() > 100 ? sql.substring(0, 100) + "..." : sql);
						}

						sqlSets.put(sqlObj);
					}
				}
				data.put("actions", actions);
				data.put("sqlSets", sqlSets);

				// 解析 ScriptSet
				JSONArray scriptSets = new JSONArray();
				Element scriptSetElem = getFirstChild(actionElem, "ScriptSet");
				if (scriptSetElem != null) {
					NodeList scriptNodes = scriptSetElem.getElementsByTagName("Set");
					for (int i = 0; i < scriptNodes.getLength(); i++) {
						Element scriptElem = (Element) scriptNodes.item(i);
						JSONObject scriptObj = new JSONObject();
						scriptObj.put("name", scriptElem.getAttribute("Name"));
						scriptObj.put("scriptType", scriptElem.getAttribute("ScriptType"));
						Element scriptContent = getFirstChild(scriptElem, "Script");
						if (scriptContent != null) {
							String script = scriptContent.getTextContent();
							scriptObj.put("scriptPreview", script.length() > 100 ? script.substring(0, 100) + "..." : script);
						}
						scriptSets.put(scriptObj);
					}
				}
				data.put("scriptSets", scriptSets);
			}

			result.put("success", true).put("data", data);
		} catch (Exception e) {
			LOGGER.error("列出 Actions 失败: " + itemName, e);
			result.put("success", false).put("message", e.getMessage());
		}

		return result;
	}

	/**
	 * 获取 SqlSet 详情
	 */
	public JSONObject getSqlSet(String xmlPath, String itemName, String sqlName) {
		JSONObject result = new JSONObject();

		IUpdateXml ux = getUpdateXml(xmlPath);
		if (ux == null) {
			return result.put("success", false).put("message", "找不到 XML 配置: " + xmlPath);
		}

		try {
			String itemXml = ux.queryItemXml(itemName);
			Document doc = UXml.asDocument(itemXml);
			Element actionElem = getFirstChild(doc.getDocumentElement(), "Action");

			if (actionElem == null) {
				return result.put("success", false).put("message", "Action 节点不存在");
			}

			Element sqlSetElem = getFirstChild(actionElem, "SqlSet");
			if (sqlSetElem == null) {
				return result.put("success", false).put("message", "SqlSet 不存在");
			}

			NodeList sqlNodes = sqlSetElem.getElementsByTagName("Set");
			for (int i = 0; i < sqlNodes.getLength(); i++) {
				Element sqlElem = (Element) sqlNodes.item(i);
				if (sqlElem.getAttribute("Name").equals(sqlName)) {
					JSONObject detail = new JSONObject();
					detail.put("name", sqlName);
					detail.put("sqlType", sqlElem.getAttribute("SqlType"));
					detail.put("transType", sqlElem.getAttribute("TransType"));

					Element sqlContent = getFirstChild(sqlElem, "Sql");
					if (sqlContent != null) {
						detail.put("sqlContent", sqlContent.getTextContent());
					}

					result.put("success", true).put("data", detail);
					return result;
				}
			}
		} catch (Exception e) {
			LOGGER.error("获取 SqlSet 详情失败: " + sqlName, e);
		}

		return result.put("success", false).put("message", "找不到 SqlSet: " + sqlName);
	}

	/**
	 * 保存 SqlSet — 新增或修改
	 */
	public JSONObject saveSqlSet(String xmlPath, String itemName, UpdateCfgSqlSetData data) {
		JSONObject result = new JSONObject();

		IUpdateXml ux = getUpdateXml(xmlPath);
		if (ux == null) {
			return result.put("success", false).put("message", "找不到 XML 配置: " + xmlPath);
		}

		try {
			String itemXml = ux.queryItemXml(itemName);
			Document doc = UXml.asDocument(itemXml);
			Element actionElem = getFirstChild(doc.getDocumentElement(), "Action");

			if (actionElem == null) {
				actionElem = doc.createElement("Action");
				doc.getDocumentElement().appendChild(actionElem);
			}

			Element sqlSetElem = getFirstChild(actionElem, "SqlSet");
			if (sqlSetElem == null) {
				sqlSetElem = doc.createElement("SqlSet");
				actionElem.appendChild(sqlSetElem);
			}

			Element targetSql = null;
			if (data.oldName != null && !data.oldName.isEmpty()) {
				NodeList sqlNodes = sqlSetElem.getElementsByTagName("Set");
				for (int i = 0; i < sqlNodes.getLength(); i++) {
					Element elem = (Element) sqlNodes.item(i);
					if (elem.getAttribute("Name").equals(data.oldName)) {
						targetSql = elem;
						break;
					}
				}
			}

			if (targetSql == null) {
				targetSql = doc.createElement("Set");
				sqlSetElem.appendChild(targetSql);
			}

			targetSql.setAttribute("Name", data.name);
			if (data.sqlType != null) targetSql.setAttribute("SqlType", data.sqlType);
			if (data.transType != null) targetSql.setAttribute("TransType", data.transType);

			Element sqlContentElem = getFirstChild(targetSql, "Sql");
			if (sqlContentElem == null) {
				sqlContentElem = doc.createElement("Sql");
				targetSql.appendChild(sqlContentElem);
			}
			while (sqlContentElem.hasChildNodes()) {
				sqlContentElem.removeChild(sqlContentElem.getFirstChild());
			}
			sqlContentElem.appendChild(doc.createCDATASection(data.sqlContent != null ? data.sqlContent : ""));

			String modifiedXml = UXml.asXml(doc);
			boolean saved = ux.saveXml(itemName, modifiedXml);
			LOGGER.info("[UpdateCfgXml] saveSqlSet by={}, xml={}, item={}, sql={}, result={}",
					operator, xmlPath, itemName, data.name, saved);
			result.put("success", saved).put("message", saved ? "保存成功" : "保存失败");
		} catch (Exception e) {
			LOGGER.error("保存 SqlSet 失败: " + data.name, e);
			result.put("success", false).put("message", "保存失败: " + e.getMessage());
		}

		return result;
	}

	/**
	 * 删除 SqlSet
	 */
	public JSONObject deleteSqlSet(String xmlPath, String itemName, String sqlName) {
		JSONObject result = new JSONObject();

		IUpdateXml ux = getUpdateXml(xmlPath);
		if (ux == null) {
			return result.put("success", false).put("message", "找不到 XML 配置: " + xmlPath);
		}

		try {
			String itemXml = ux.queryItemXml(itemName);
			Document doc = UXml.asDocument(itemXml);
			Element actionElem = getFirstChild(doc.getDocumentElement(), "Action");

			if (actionElem == null) {
				return result.put("success", false).put("message", "Action 节点不存在");
			}

			Element sqlSetElem = getFirstChild(actionElem, "SqlSet");
			if (sqlSetElem == null) {
				return result.put("success", false).put("message", "SqlSet 不存在");
			}

			NodeList sqlNodes = sqlSetElem.getElementsByTagName("Set");
			for (int i = 0; i < sqlNodes.getLength(); i++) {
				Element elem = (Element) sqlNodes.item(i);
				if (elem.getAttribute("Name").equals(sqlName)) {
					sqlSetElem.removeChild(elem);
					break;
				}
			}

			String modifiedXml = UXml.asXml(doc);
			boolean saved = ux.saveXml(itemName, modifiedXml);
			LOGGER.info("[UpdateCfgXml] deleteSqlSet by={}, xml={}, item={}, sql={}, result={}",
					operator, xmlPath, itemName, sqlName, saved);
			result.put("success", saved).put("message", saved ? "删除成功" : "删除失败");
		} catch (Exception e) {
			LOGGER.error("删除 SqlSet 失败: " + sqlName, e);
			result.put("success", false).put("message", "删除失败: " + e.getMessage());
		}

		return result;
	}

	// ========================================================================
	// 5. ScriptSet 管理
	// ========================================================================

	/**
	 * 获取 ScriptSet 详情
	 */
	public JSONObject getScriptSet(String xmlPath, String itemName, String scriptName) {
		JSONObject result = new JSONObject();

		IUpdateXml ux = getUpdateXml(xmlPath);
		if (ux == null) {
			return result.put("success", false).put("message", "找不到 XML 配置: " + xmlPath);
		}

		try {
			String itemXml = ux.queryItemXml(itemName);
			Document doc = UXml.asDocument(itemXml);
			Element actionElem = getFirstChild(doc.getDocumentElement(), "Action");

			if (actionElem == null) {
				return result.put("success", false).put("message", "Action 节点不存在");
			}

			Element scriptSetElem = getFirstChild(actionElem, "ScriptSet");
			if (scriptSetElem == null) {
				return result.put("success", false).put("message", "ScriptSet 不存在");
			}

			NodeList scriptNodes = scriptSetElem.getElementsByTagName("Set");
			for (int i = 0; i < scriptNodes.getLength(); i++) {
				Element scriptElem = (Element) scriptNodes.item(i);
				if (scriptElem.getAttribute("Name").equals(scriptName)) {
					JSONObject detail = new JSONObject();
					detail.put("name", scriptName);
					detail.put("scriptType", scriptElem.getAttribute("ScriptType"));

					Element scriptContent = getFirstChild(scriptElem, "Script");
					if (scriptContent != null) {
						detail.put("scriptContent", scriptContent.getTextContent());
					}

					result.put("success", true).put("data", detail);
					return result;
				}
			}
		} catch (Exception e) {
			LOGGER.error("获取 ScriptSet 详情失败: " + scriptName, e);
		}

		return result.put("success", false).put("message", "找不到 ScriptSet: " + scriptName);
	}

	/**
	 * 保存 ScriptSet
	 */
	public JSONObject saveScriptSet(String xmlPath, String itemName, UpdateCfgScriptSetData data) {
		JSONObject result = new JSONObject();

		IUpdateXml ux = getUpdateXml(xmlPath);
		if (ux == null) {
			return result.put("success", false).put("message", "找不到 XML 配置: " + xmlPath);
		}

		try {
			String itemXml = ux.queryItemXml(itemName);
			Document doc = UXml.asDocument(itemXml);
			Element actionElem = getFirstChild(doc.getDocumentElement(), "Action");

			if (actionElem == null) {
				actionElem = doc.createElement("Action");
				doc.getDocumentElement().appendChild(actionElem);
			}

			Element scriptSetElem = getFirstChild(actionElem, "ScriptSet");
			if (scriptSetElem == null) {
				scriptSetElem = doc.createElement("ScriptSet");
				actionElem.appendChild(scriptSetElem);
			}

			Element targetScript = null;
			if (data.oldName != null && !data.oldName.isEmpty()) {
				NodeList scriptNodes = scriptSetElem.getElementsByTagName("Set");
				for (int i = 0; i < scriptNodes.getLength(); i++) {
					Element elem = (Element) scriptNodes.item(i);
					if (elem.getAttribute("Name").equals(data.oldName)) {
						targetScript = elem;
						break;
					}
				}
			}

			if (targetScript == null) {
				targetScript = doc.createElement("Set");
				scriptSetElem.appendChild(targetScript);
			}

			targetScript.setAttribute("Name", data.name);
			if (data.scriptType != null) targetScript.setAttribute("ScriptType", data.scriptType);

			Element scriptContentElem = getFirstChild(targetScript, "Script");
			if (scriptContentElem == null) {
				scriptContentElem = doc.createElement("Script");
				targetScript.appendChild(scriptContentElem);
			}
			while (scriptContentElem.hasChildNodes()) {
				scriptContentElem.removeChild(scriptContentElem.getFirstChild());
			}
			scriptContentElem.appendChild(doc.createCDATASection(data.scriptContent != null ? data.scriptContent : ""));

			String modifiedXml = UXml.asXml(doc);
			boolean saved = ux.saveXml(itemName, modifiedXml);
			LOGGER.info("[UpdateCfgXml] saveScriptSet by={}, xml={}, item={}, script={}, result={}",
					operator, xmlPath, itemName, data.name, saved);
			result.put("success", saved).put("message", saved ? "保存成功" : "保存失败");
		} catch (Exception e) {
			LOGGER.error("保存 ScriptSet 失败: " + data.name, e);
			result.put("success", false).put("message", "保存失败: " + e.getMessage());
		}

		return result;
	}

	/**
	 * 删除 ScriptSet
	 */
	public JSONObject deleteScriptSet(String xmlPath, String itemName, String scriptName) {
		JSONObject result = new JSONObject();

		IUpdateXml ux = getUpdateXml(xmlPath);
		if (ux == null) {
			return result.put("success", false).put("message", "找不到 XML 配置: " + xmlPath);
		}

		try {
			String itemXml = ux.queryItemXml(itemName);
			Document doc = UXml.asDocument(itemXml);
			Element actionElem = getFirstChild(doc.getDocumentElement(), "Action");

			if (actionElem == null) {
				return result.put("success", false).put("message", "Action 节点不存在");
			}

			Element scriptSetElem = getFirstChild(actionElem, "ScriptSet");
			if (scriptSetElem == null) {
				return result.put("success", false).put("message", "ScriptSet 不存在");
			}

			NodeList scriptNodes = scriptSetElem.getElementsByTagName("Set");
			for (int i = 0; i < scriptNodes.getLength(); i++) {
				Element elem = (Element) scriptNodes.item(i);
				if (elem.getAttribute("Name").equals(scriptName)) {
					scriptSetElem.removeChild(elem);
					break;
				}
			}

			String modifiedXml = UXml.asXml(doc);
			boolean saved = ux.saveXml(itemName, modifiedXml);
			LOGGER.info("[UpdateCfgXml] deleteScriptSet by={}, xml={}, item={}, script={}, result={}",
					operator, xmlPath, itemName, scriptName, saved);
			result.put("success", saved).put("message", saved ? "删除成功" : "删除失败");
		} catch (Exception e) {
			LOGGER.error("删除 ScriptSet 失败: " + scriptName, e);
			result.put("success", false).put("message", "删除失败: " + e.getMessage());
		}

		return result;
	}

	// ========================================================================
	// 工具方法
	// ========================================================================

	/**
	 * 获取 IUpdateXml 实例
	 */
	private IUpdateXml getUpdateXml(String xmlPath) {
		if (xmlPath == null || xmlPath.trim().isEmpty()) {
			return null;
		}
		return ConfigUtils.getUpdateXml(xmlPath.trim());
	}

	/**
	 * 获取元素的第一个指定名称的子元素
	 */
	private Element getFirstChild(Element parent, String tagName) {
		if (parent == null) return null;
		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(tagName)) {
				return (Element) child;
			}
		}
		return null;
	}

	/**
	 * 获取 XItem 的描述信息
	 */
	private String getDescription(Element xitemElem, String lang) {
		Element descSet = getFirstChild(xitemElem, "DescriptionSet");
		if (descSet != null) {
			NodeList setNodes = descSet.getElementsByTagName("Set");
			for (int i = 0; i < setNodes.getLength(); i++) {
				Element setElem = (Element) setNodes.item(i);
				if (lang.equals(setElem.getAttribute("Lang"))) {
					return setElem.getAttribute("Info");
				}
			}
		}
		return "";
	}

	/**
	 * 获取子节点中 Set 元素的指定属性值
	 */
	private String getChildAttr(Element parent, String childTag, String attrName) {
		Element child = getFirstChild(parent, childTag);
		if (child != null) {
			Element setElem = getFirstChild(child, "Set");
			if (setElem != null) {
				return setElem.getAttribute(attrName);
			}
		}
		return "";
	}
}
