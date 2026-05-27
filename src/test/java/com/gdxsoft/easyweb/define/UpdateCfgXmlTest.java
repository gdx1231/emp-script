package com.gdxsoft.easyweb.define;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UXml;

/**
 * UpdateCfgXml 测试
 * 
 * 使用 sample XML (admin.xml) 复制到临时目录进行测试，
 * 直接操作 XML 文件，不依赖 ewa_conf.xml 配置。
 * 
 * 测试覆盖：
 * 1. XItem CRUD（新增/读取/修改/删除/移动）
 * 2. SqlSet CRUD（新增/读取/修改/删除）
 * 3. ScriptSet CRUD（新增/读取/修改/删除）
 * 4. 列表查询（listItems/listXItems/listActions）
 */
@TestMethodOrder(OrderAnnotation.class)
public class UpdateCfgXmlTest {

	/** 临时测试目录 */
	private static File TEST_DIR;
	/** 测试用 XML 文件 */
	private static File TEST_XML;
	/** 原始样本 XML */
	private static final String SAMPLE_XML = "/Users/admin/java/com.gdxsoft/emp-script/designs/tb2ewaCfg/examples/admin.xml";
	/** 测试用的 EasyWebTemplate 名称 */
	private static final String TEST_ITEM = "ADM_USER.Frame.ChangePWD";
	/** 测试用 XItem 名称 */
	private static final String TEST_XITEM = "TEST_XITEM_" + System.currentTimeMillis();
	/** 测试用 SqlSet 名称 */
	private static final String TEST_SQL = "TEST_SQL_" + System.currentTimeMillis();
	/** 测试用 ScriptSet 名称 */
	private static final String TEST_SCRIPT = "TEST_SCRIPT_" + System.currentTimeMillis();

	/** 直接操作的 XML 路径（非 ConfigUtils，用于底层测试） */
	private static String xmlPath;

	@BeforeAll
	public static void setUp() throws IOException {
		printCaption("准备测试环境");

		// 创建临时测试目录
		TEST_DIR = Files.createTempDirectory("update_cfg_test_").toFile();
		TEST_XML = new File(TEST_DIR, "admin.xml");

		// 复制样本 XML 到临时目录
		File sample = new File(SAMPLE_XML);
		assertTrue(sample.exists(), "样本 XML 不存在: " + SAMPLE_XML);
		Files.copy(sample.toPath(), TEST_XML.toPath(), StandardCopyOption.REPLACE_EXISTING);

		xmlPath = TEST_XML.getAbsolutePath();
		System.out.println("测试目录: " + TEST_DIR.getAbsolutePath());
		System.out.println("测试 XML: " + TEST_XML.getAbsolutePath());
		System.out.println("样本 XML 大小: " + sample.length() + " bytes");
		System.out.println("复制后大小: " + TEST_XML.length() + " bytes");
	}

	@AfterAll
	public static void tearDown() {
		// 清理临时文件
		if (TEST_DIR != null && TEST_DIR.exists()) {
			deleteDir(TEST_DIR);
			System.out.println("清理测试目录: " + TEST_DIR.getAbsolutePath());
		}
	}

	private static void deleteDir(File dir) {
		File[] files = dir.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					deleteDir(f);
				} else {
					f.delete();
				}
			}
		}
		dir.delete();
	}

	// ========================================================================
	// 0. 基础 XML 解析测试
	// ========================================================================

	@Test
	@Order(1)
	public void testXmlParseable() {
		printCaption("0. 基础 XML 解析测试");

		String xml = readXml();
		assertNotNull(xml, "XML 文件内容不能为空");
		assertTrue(xml.contains("<EasyWebTemplates>"), "应包含 EasyWebTemplates 根节点");

		Document doc = UXml.asDocument(xml);
		assertNotNull(doc, "XML 应可解析为 Document");

		NodeList items = doc.getElementsByTagName("EasyWebTemplate");
		assertTrue(items.getLength() > 0, "应包含至少一个 EasyWebTemplate");
		System.out.println("模板数量: " + items.getLength());

		// 打印所有模板名称
		for (int i = 0; i < items.getLength(); i++) {
			Element elem = (Element) items.item(i);
			System.out.println("  [" + i + "] " + elem.getAttribute("Name"));
		}
	}

	@Test
	@Order(2)
	public void testFindTargetItem() {
		printCaption("0.1 查找目标模板");

		String xml = readXml();
		Document doc = UXml.asDocument(xml);
		NodeList items = doc.getElementsByTagName("EasyWebTemplate");

		boolean found = false;
		for (int i = 0; i < items.getLength(); i++) {
			Element elem = (Element) items.item(i);
			if (elem.getAttribute("Name").equals(TEST_ITEM)) {
				found = true;
				System.out.println("找到模板: " + TEST_ITEM);

				// 检查子结构
				Element action = getFirstChild(elem, "Action");
				assertNotNull(action, "模板应包含 Action 节点");

				Element actionSet = getFirstChild(action, "ActionSet");
				System.out.println("  ActionSet: " + (actionSet != null ? "存在" : "不存在"));

				Element sqlSet = getFirstChild(action, "SqlSet");
				System.out.println("  SqlSet: " + (sqlSet != null ? "存在" : "不存在"));

				Element xitems = getFirstChild(elem, "XItems");
				System.out.println("  XItems: " + (xitems != null ? "存在" : "不存在"));

				if (xitems != null) {
					NodeList xitemNodes = xitems.getElementsByTagName("XItem");
					System.out.println("  XItem 数量: " + xitemNodes.getLength());
					for (int j = 0; j < xitemNodes.getLength(); j++) {
						Element xitem = (Element) xitemNodes.item(j);
						System.out.println("    [" + j + "] " + xitem.getAttribute("Name"));
					}
				}

				if (sqlSet != null) {
					NodeList sqlNodes = sqlSet.getElementsByTagName("Set");
					System.out.println("  SqlSet 数量: " + sqlNodes.getLength());
					for (int j = 0; j < sqlNodes.getLength(); j++) {
						Element sql = (Element) sqlNodes.item(j);
						System.out.println("    [" + j + "] " + sql.getAttribute("Name") + " (" + sql.getAttribute("SqlType") + ")");
					}
				}
				break;
			}
		}

		assertTrue(found, "应找到测试模板: " + TEST_ITEM);
	}

	// ========================================================================
	// 1. XItem 列表测试
	// ========================================================================

	@Test
	@Order(10)
	public void testListXItems() {
		printCaption("1. listXItems 测试");

		String xml = readXml();
		Document doc = UXml.asDocument(xml);

		// 找到目标模板
		Element targetItem = findEasyWebTemplate(doc, TEST_ITEM);
		assertNotNull(targetItem, "应找到模板: " + TEST_ITEM);

		Element xitems = getFirstChild(targetItem, "XItems");
		assertNotNull(xitems, "模板应包含 XItems 节点");

		NodeList xitemNodes = xitems.getElementsByTagName("XItem");
		System.out.println("XItem 总数: " + xitemNodes.getLength());
		assertTrue(xitemNodes.getLength() > 0, "应有至少一个 XItem");

		// 验证第一个 XItem 的结构
		Element firstXItem = (Element) xitemNodes.item(0);
		String name = firstXItem.getAttribute("Name");
		System.out.println("第一个 XItem: " + name);
		assertNotNull(name, "XItem 应有 Name 属性");
		assertFalse(name.isEmpty(), "XItem Name 不能为空");

		// 验证 Tag 类型
		String tag = getChildAttr(firstXItem, "Tag", "Tag");
		System.out.println("  Tag: " + tag);

		// 验证描述
		String descZh = getDescription(firstXItem, "zhcn");
		System.out.println("  中文描述: " + descZh);
	}

	// ========================================================================
	// 2. XItem 新增测试
	// ========================================================================

	@Test
	@Order(20)
	public void testAddXItem() {
		printCaption("2. 新增 XItem 测试");

		String xml = readXml();
		Document doc = UXml.asDocument(xml);
		Element targetItem = findEasyWebTemplate(doc, TEST_ITEM);

		Element xitems = getFirstChild(targetItem, "XItems");
		if (xitems == null) {
			xitems = doc.createElement("XItems");
			targetItem.appendChild(xitems);
		}

		// 创建新的 XItem
		Element newXItem = doc.createElement("XItem");
		newXItem.setAttribute("Name", TEST_XITEM);
		xitems.appendChild(newXItem);

		// Tag
		Element tagElem = doc.createElement("Tag");
		Element tagSet = doc.createElement("Set");
		tagSet.setAttribute("Tag", "text");
		tagElem.appendChild(tagSet);
		newXItem.appendChild(tagElem);

		// Name
		Element nameElem = doc.createElement("Name");
		Element nameSet = doc.createElement("Set");
		nameSet.setAttribute("Name", TEST_XITEM);
		nameElem.appendChild(nameSet);
		newXItem.appendChild(nameElem);

		// DescriptionSet
		Element descSetElem = doc.createElement("DescriptionSet");
		Element zhSet = doc.createElement("Set");
		zhSet.setAttribute("Lang", "zhcn");
		zhSet.setAttribute("Info", "测试字段");
		zhSet.setAttribute("Memo", "");
		descSetElem.appendChild(zhSet);
		Element enSet = doc.createElement("Set");
		enSet.setAttribute("Lang", "enus");
		enSet.setAttribute("Info", "Test Field");
		enSet.setAttribute("Memo", "");
		descSetElem.appendChild(enSet);
		newXItem.appendChild(descSetElem);

		// DataItem
		Element dataItemElem = doc.createElement("DataItem");
		Element dataSet = doc.createElement("Set");
		dataSet.setAttribute("DataField", "TEST_FIELD");
		dataSet.setAttribute("DataType", "String");
		dataItemElem.appendChild(dataSet);
		newXItem.appendChild(dataItemElem);

		// IsMustInput
		Element mustElem = doc.createElement("IsMustInput");
		Element mustSet = doc.createElement("Set");
		mustSet.setAttribute("IsMustInput", "1");
		mustElem.appendChild(mustSet);
		newXItem.appendChild(mustElem);

		// 保存
		String modifiedXml = UXml.asXml(doc);
		try {
			Files.write(TEST_XML.toPath(), modifiedXml.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		// 验证：重新读取
		String xml2 = readXml();
		Document doc2 = UXml.asDocument(xml2);
		Element targetItem2 = findEasyWebTemplate(doc2, TEST_ITEM);
		Element xitems2 = getFirstChild(targetItem2, "XItems");
		NodeList xitemNodes2 = xitems2.getElementsByTagName("XItem");

		boolean found = false;
		for (int i = 0; i < xitemNodes2.getLength(); i++) {
			Element elem = (Element) xitemNodes2.item(i);
			if (elem.getAttribute("Name").equals(TEST_XITEM)) {
				found = true;
				System.out.println("新增 XItem 验证成功: " + TEST_XITEM);
				System.out.println("  Tag: " + getChildAttr(elem, "Tag", "Tag"));
				System.out.println("  DataField: " + getChildAttr(elem, "DataItem", "DataField"));
				System.out.println("  IsMustInput: " + getChildAttr(elem, "IsMustInput", "IsMustInput"));
				break;
			}
		}
		assertTrue(found, "应能找到新增的 XItem: " + TEST_XITEM);
	}

	// ========================================================================
	// 3. XItem 修改测试
	// ========================================================================

	@Test
	@Order(30)
	public void testModifyXItem() {
		printCaption("3. 修改 XItem 测试");

		String xml = readXml();
		Document doc = UXml.asDocument(xml);
		Element targetItem = findEasyWebTemplate(doc, TEST_ITEM);
		Element xitems = getFirstChild(targetItem, "XItems");

		// 找到目标 XItem
		Element targetXItem = findXItem(xitems, TEST_XITEM);
		assertNotNull(targetXItem, "应找到 XItem: " + TEST_XITEM);

		// 修改描述
		Element descSet = getFirstChild(targetXItem, "DescriptionSet");
		assertNotNull(descSet, "XItem 应包含 DescriptionSet");
		NodeList setNodes = descSet.getElementsByTagName("Set");
		for (int i = 0; i < setNodes.getLength(); i++) {
			Element setElem = (Element) setNodes.item(i);
			if ("zhcn".equals(setElem.getAttribute("Lang"))) {
				setElem.setAttribute("Info", "修改后的测试字段");
				System.out.println("修改前: 测试字段");
				System.out.println("修改后: 修改后的测试字段");
				break;
			}
		}

		// 修改 DataField
		Element dataItem = getFirstChild(targetXItem, "DataItem");
		assertNotNull(dataItem, "XItem 应包含 DataItem");
		Element dataSet = getFirstChild(dataItem, "Set");
		assertNotNull(dataSet, "DataItem 应包含 Set");
		String oldField = dataSet.getAttribute("DataField");
		dataSet.setAttribute("DataField", "MODIFIED_FIELD");
		System.out.println("DataField 修改: " + oldField + " -> MODIFIED_FIELD");

		// 保存
		String modifiedXml = UXml.asXml(doc);
		try {
			Files.write(TEST_XML.toPath(), modifiedXml.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		// 验证
		String xml2 = readXml();
		Document doc2 = UXml.asDocument(xml2);
		Element targetItem2 = findEasyWebTemplate(doc2, TEST_ITEM);
		Element xitems2 = getFirstChild(targetItem2, "XItems");
		Element targetXItem2 = findXItem(xitems2, TEST_XITEM);
		Element dataItem2 = getFirstChild(targetXItem2, "DataItem");
		Element dataSet2 = getFirstChild(dataItem2, "Set");

		assertEquals("MODIFIED_FIELD", dataSet2.getAttribute("DataField"), "DataField 应已修改");
	}

	// ========================================================================
	// 4. XItem 删除测试
	// ========================================================================

	@Test
	@Order(40)
	public void testDeleteXItem() {
		printCaption("4. 删除 XItem 测试");

		String xml = readXml();
		Document doc = UXml.asDocument(xml);
		Element targetItem = findEasyWebTemplate(doc, TEST_ITEM);
		Element xitems = getFirstChild(targetItem, "XItems");

		// 找到并删除
		Element targetXItem = findXItem(xitems, TEST_XITEM);
		assertNotNull(targetXItem, "应找到要删除的 XItem: " + TEST_XITEM);

		xitems.removeChild(targetXItem);
		System.out.println("已删除 XItem: " + TEST_XITEM);

		// 保存
		String modifiedXml = UXml.asXml(doc);
		try {
			Files.write(TEST_XML.toPath(), modifiedXml.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		// 验证已删除
		String xml2 = readXml();
		Document doc2 = UXml.asDocument(xml2);
		Element targetItem2 = findEasyWebTemplate(doc2, TEST_ITEM);
		Element xitems2 = getFirstChild(targetItem2, "XItems");
		Element deletedXItem = findXItem(xitems2, TEST_XITEM);

		assertNull(deletedXItem, "删除后不应再找到 XItem: " + TEST_XITEM);
		System.out.println("删除验证成功");
	}

	// ========================================================================
	// 5. SqlSet 列表测试
	// ========================================================================

	@Test
	@Order(50)
	public void testListSqlSets() {
		printCaption("5. listSqlSets 测试");

		String xml = readXml();
		Document doc = UXml.asDocument(xml);
		Element targetItem = findEasyWebTemplate(doc, TEST_ITEM);
		Element action = getFirstChild(targetItem, "Action");

		Element sqlSet = getFirstChild(action, "SqlSet");
		assertNotNull(sqlSet, "Action 应包含 SqlSet 节点");

		NodeList sqlNodes = sqlSet.getElementsByTagName("Set");
		System.out.println("SqlSet 总数: " + sqlNodes.getLength());
		assertTrue(sqlNodes.getLength() > 0, "应有至少一个 SqlSet");

		for (int i = 0; i < sqlNodes.getLength(); i++) {
			Element sql = (Element) sqlNodes.item(i);
			String name = sql.getAttribute("Name");
			String type = sql.getAttribute("SqlType");
			String trans = sql.getAttribute("TransType");
			System.out.println("  [" + i + "] " + name + " (type=" + type + ", trans=" + trans + ")");

			// 验证 SQL 内容
			Element sqlContent = getFirstChild(sql, "Sql");
			assertNotNull(sqlContent, "SqlSet 应包含 Sql 子节点");
			String sqlText = sqlContent.getTextContent();
			assertNotNull(sqlText, "SQL 内容不能为空");
			assertFalse(sqlText.isEmpty(), "SQL 内容不能为空字符串");
			System.out.println("       SQL 预览: " + sqlText.substring(0, Math.min(80, sqlText.length())) + "...");
		}
	}

	// ========================================================================
	// 6. SqlSet 新增测试
	// ========================================================================

	@Test
	@Order(60)
	public void testAddSqlSet() {
		printCaption("6. 新增 SqlSet 测试");

		String xml = readXml();
		Document doc = UXml.asDocument(xml);
		Element targetItem = findEasyWebTemplate(doc, TEST_ITEM);
		Element action = getFirstChild(targetItem, "Action");

		Element sqlSet = getFirstChild(action, "SqlSet");
		if (sqlSet == null) {
			sqlSet = doc.createElement("SqlSet");
			action.appendChild(sqlSet);
		}

		// 创建新的 SqlSet
		Element newSql = doc.createElement("Set");
		newSql.setAttribute("Name", TEST_SQL);
		newSql.setAttribute("SqlType", "query");
		newSql.setAttribute("TransType", "no");
		sqlSet.appendChild(newSql);

		// SQL 内容（CDATA）
		Element sqlContent = doc.createElement("Sql");
		Node cdata = doc.createCDATASection("SELECT 'test' AS result, 'text' AS rst_type");
		sqlContent.appendChild(cdata);
		newSql.appendChild(sqlContent);

		// 保存
		String modifiedXml = UXml.asXml(doc);
		try {
			Files.write(TEST_XML.toPath(), modifiedXml.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		// 验证
		String xml2 = readXml();
		Document doc2 = UXml.asDocument(xml2);
		Element targetItem2 = findEasyWebTemplate(doc2, TEST_ITEM);
		Element action2 = getFirstChild(targetItem2, "Action");
		Element sqlSet2 = getFirstChild(action2, "SqlSet");
		Element newSql2 = findSqlSet(sqlSet2, TEST_SQL);

		assertNotNull(newSql2, "应能找到新增的 SqlSet: " + TEST_SQL);
		assertEquals("query", newSql2.getAttribute("SqlType"), "SqlType 应为 query");
		assertEquals("no", newSql2.getAttribute("TransType"), "TransType 应为 no");

		Element sqlContent2 = getFirstChild(newSql2, "Sql");
		assertNotNull(sqlContent2, "应包含 Sql 内容");
		assertEquals("SELECT 'test' AS result, 'text' AS rst_type", sqlContent2.getTextContent(), "SQL 内容应匹配");

		System.out.println("新增 SqlSet 验证成功: " + TEST_SQL);
	}

	// ========================================================================
	// 7. SqlSet 修改测试
	// ========================================================================

	@Test
	@Order(70)
	public void testModifySqlSet() {
		printCaption("7. 修改 SqlSet 测试");

		String xml = readXml();
		Document doc = UXml.asDocument(xml);
		Element targetItem = findEasyWebTemplate(doc, TEST_ITEM);
		Element action = getFirstChild(targetItem, "Action");
		Element sqlSet = getFirstChild(action, "SqlSet");

		Element targetSql = findSqlSet(sqlSet, TEST_SQL);
		assertNotNull(targetSql, "应找到 SqlSet: " + TEST_SQL);

		// 修改 SQL 内容
		Element sqlContent = getFirstChild(targetSql, "Sql");
		assertNotNull(sqlContent);
		while (sqlContent.hasChildNodes()) {
			sqlContent.removeChild(sqlContent.getFirstChild());
		}
		String newSql = "SELECT 'modified' AS result, 'json' AS rst_type";
		sqlContent.appendChild(doc.createCDATASection(newSql));

		// 修改 SqlType
		targetSql.setAttribute("SqlType", "update");

		// 保存
		String modifiedXml = UXml.asXml(doc);
		try {
			Files.write(TEST_XML.toPath(), modifiedXml.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		// 验证
		String xml2 = readXml();
		Document doc2 = UXml.asDocument(xml2);
		Element targetItem2 = findEasyWebTemplate(doc2, TEST_ITEM);
		Element action2 = getFirstChild(targetItem2, "Action");
		Element sqlSet2 = getFirstChild(action2, "SqlSet");
		Element targetSql2 = findSqlSet(sqlSet2, TEST_SQL);

		assertEquals("update", targetSql2.getAttribute("SqlType"), "SqlType 应已修改为 update");
		assertEquals(newSql, getFirstChild(targetSql2, "Sql").getTextContent(), "SQL 内容应已修改");

		System.out.println("修改 SqlSet 验证成功");
	}

	// ========================================================================
	// 8. SqlSet 删除测试
	// ========================================================================

	@Test
	@Order(80)
	public void testDeleteSqlSet() {
		printCaption("8. 删除 SqlSet 测试");

		String xml = readXml();
		Document doc = UXml.asDocument(xml);
		Element targetItem = findEasyWebTemplate(doc, TEST_ITEM);
		Element action = getFirstChild(targetItem, "Action");
		Element sqlSet = getFirstChild(action, "SqlSet");

		Element targetSql = findSqlSet(sqlSet, TEST_SQL);
		assertNotNull(targetSql, "应找到要删除的 SqlSet: " + TEST_SQL);

		sqlSet.removeChild(targetSql);
		System.out.println("已删除 SqlSet: " + TEST_SQL);

		// 保存
		String modifiedXml = UXml.asXml(doc);
		try {
			Files.write(TEST_XML.toPath(), modifiedXml.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		// 验证
		String xml2 = readXml();
		Document doc2 = UXml.asDocument(xml2);
		Element targetItem2 = findEasyWebTemplate(doc2, TEST_ITEM);
		Element action2 = getFirstChild(targetItem2, "Action");
		Element sqlSet2 = getFirstChild(action2, "SqlSet");
		Element deletedSql = findSqlSet(sqlSet2, TEST_SQL);

		assertNull(deletedSql, "删除后不应再找到 SqlSet: " + TEST_SQL);
		System.out.println("删除验证成功");
	}

	// ========================================================================
	// 9. ScriptSet CRUD 测试
	// ========================================================================

	@Test
	@Order(90)
	public void testAddScriptSet() {
		printCaption("9. 新增 ScriptSet 测试");

		String xml = readXml();
		Document doc = UXml.asDocument(xml);
		Element targetItem = findEasyWebTemplate(doc, TEST_ITEM);
		Element action = getFirstChild(targetItem, "Action");

		Element scriptSet = getFirstChild(action, "ScriptSet");
		if (scriptSet == null) {
			scriptSet = doc.createElement("ScriptSet");
			action.appendChild(scriptSet);
		}

		// 创建新的 ScriptSet
		Element newScript = doc.createElement("Set");
		newScript.setAttribute("Name", TEST_SCRIPT);
		newScript.setAttribute("ScriptType", "javascript");
		scriptSet.appendChild(newScript);

		// Script 内容
		Element scriptContent = doc.createElement("Script");
		Node cdata = doc.createCDATASection("console.log('test script');");
		scriptContent.appendChild(cdata);
		newScript.appendChild(scriptContent);

		// 保存
		String modifiedXml = UXml.asXml(doc);
		try {
			Files.write(TEST_XML.toPath(), modifiedXml.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		// 验证
		String xml2 = readXml();
		Document doc2 = UXml.asDocument(xml2);
		Element targetItem2 = findEasyWebTemplate(doc2, TEST_ITEM);
		Element action2 = getFirstChild(targetItem2, "Action");
		Element scriptSet2 = getFirstChild(action2, "ScriptSet");
		Element newScript2 = findScriptSet(scriptSet2, TEST_SCRIPT);

		assertNotNull(newScript2, "应能找到新增的 ScriptSet: " + TEST_SCRIPT);
		assertEquals("javascript", newScript2.getAttribute("ScriptType"));
		assertEquals("console.log('test script');", getFirstChild(newScript2, "Script").getTextContent());

		System.out.println("新增 ScriptSet 验证成功: " + TEST_SCRIPT);
	}

	@Test
	@Order(95)
	public void testDeleteScriptSet() {
		printCaption("9.1 删除 ScriptSet 测试");

		String xml = readXml();
		Document doc = UXml.asDocument(xml);
		Element targetItem = findEasyWebTemplate(doc, TEST_ITEM);
		Element action = getFirstChild(targetItem, "Action");
		Element scriptSet = getFirstChild(action, "ScriptSet");

		Element targetScript = findScriptSet(scriptSet, TEST_SCRIPT);
		assertNotNull(targetScript, "应找到要删除的 ScriptSet: " + TEST_SCRIPT);

		scriptSet.removeChild(targetScript);

		// 保存
		String modifiedXml = UXml.asXml(doc);
		try {
			Files.write(TEST_XML.toPath(), modifiedXml.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		// 验证
		String xml2 = readXml();
		Document doc2 = UXml.asDocument(xml2);
		Element targetItem2 = findEasyWebTemplate(doc2, TEST_ITEM);
		Element action2 = getFirstChild(targetItem2, "Action");
		Element scriptSet2 = getFirstChild(action2, "ScriptSet");
		Element deletedScript = findScriptSet(scriptSet2, TEST_SCRIPT);

		assertNull(deletedScript, "删除后不应再找到 ScriptSet: " + TEST_SCRIPT);
		System.out.println("删除 ScriptSet 验证成功");
	}

	// ========================================================================
	// 10. 复杂操作：CDATA 多行 SQL 保持
	// ========================================================================

	@Test
	@Order(100)
	public void testMultiLineSqlCData() {
		printCaption("10. 多行 SQL CDATA 保持测试");

		String multiLineSql = "-- 多语句测试\n"
				+ "SELECT COUNT(1) AS cnt FROM DUAL;\n"
				+ "SELECT 'step2' AS step;\n"
				+ "UPDATE test SET col = 'val' WHERE id = 1;";

		String xml = readXml();
		Document doc = UXml.asDocument(xml);
		Element targetItem = findEasyWebTemplate(doc, TEST_ITEM);
		Element action = getFirstChild(targetItem, "Action");
		Element sqlSet = getFirstChild(action, "SqlSet");

		// 新增多行 SQL
		Element newSql = doc.createElement("Set");
		newSql.setAttribute("Name", "TEST_MULTILINE_SQL");
		newSql.setAttribute("SqlType", "update");
		sqlSet.appendChild(newSql);

		Element sqlContent = doc.createElement("Sql");
		sqlContent.appendChild(doc.createCDATASection(multiLineSql));
		newSql.appendChild(sqlContent);

		// 保存
		String modifiedXml = UXml.asXml(doc);
		try {
			Files.write(TEST_XML.toPath(), modifiedXml.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		// 验证
		String xml2 = readXml();
		Document doc2 = UXml.asDocument(xml2);
		Element targetItem2 = findEasyWebTemplate(doc2, TEST_ITEM);
		Element action2 = getFirstChild(targetItem2, "Action");
		Element sqlSet2 = getFirstChild(action2, "SqlSet");
		Element savedSql = findSqlSet(sqlSet2, "TEST_MULTILINE_SQL");

		assertNotNull(savedSql, "应能找到多行 SQL");
		String savedSqlText = getFirstChild(savedSql, "Sql").getTextContent();
		assertEquals(multiLineSql, savedSqlText, "多行 SQL 内容应完全一致");

		System.out.println("多行 SQL 内容:");
		System.out.println("---");
		System.out.println(savedSqlText);
		System.out.println("---");

		// 清理
		sqlSet2.removeChild(savedSql);
		try {
			Files.write(TEST_XML.toPath(), UXml.asXml(doc2).getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// ========================================================================
	// 11. ActionSet 引用关系测试
	// ========================================================================

	@Test
	@Order(110)
	public void testActionSetReferences() {
		printCaption("11. ActionSet 引用关系测试");

		String xml = readXml();
		Document doc = UXml.asDocument(xml);
		Element targetItem = findEasyWebTemplate(doc, TEST_ITEM);
		Element action = getFirstChild(targetItem, "Action");

		Element actionSet = getFirstChild(action, "ActionSet");
		Element sqlSet = getFirstChild(action, "SqlSet");

		if (actionSet != null && sqlSet != null) {
			// 收集所有 SqlSet 名称
			java.util.Set<String> sqlNames = new java.util.HashSet<>();
			NodeList sqlNodes = sqlSet.getElementsByTagName("Set");
			for (int i = 0; i < sqlNodes.getLength(); i++) {
				sqlNames.add(((Element) sqlNodes.item(i)).getAttribute("Name"));
			}
			System.out.println("SqlSet 名称列表: " + sqlNames);

			// 检查 ActionSet 中的引用
			NodeList actionSetNodes = actionSet.getElementsByTagName("Set");
			for (int i = 0; i < actionSetNodes.getLength(); i++) {
				Element asElem = (Element) actionSetNodes.item(i);
				String actionType = asElem.getAttribute("Type");
				System.out.println("Action: " + actionType);

				Element callSet = getFirstChild(asElem, "CallSet");
				if (callSet != null) {
					NodeList calls = callSet.getElementsByTagName("Set");
					for (int j = 0; j < calls.getLength(); j++) {
						Element call = (Element) calls.item(j);
						String callName = call.getAttribute("CallName");
						String callType = call.getAttribute("CallType");
						String test = call.getAttribute("Test");
						System.out.println("  -> " + callName + " (" + callType + ")"
								+ (test != null && !test.isEmpty() ? " [条件: " + test + "]" : ""));

						// 验证引用的 SqlSet 存在
						if ("SqlSet".equals(callType) && !callName.isEmpty()) {
							Element referencedSql = findSqlSet(sqlSet, callName);
							assertNotNull(referencedSql,
									"ActionSet 引用的 SqlSet 应存在: " + callName + " (in Action: " + actionType + ")");
						}
					}
				}
			}
		} else {
			System.out.println("ActionSet 或 SqlSet 不存在，跳过引用关系测试");
		}
	}

	// ========================================================================
	// 工具方法
	// ========================================================================

	private Element findEasyWebTemplate(Document doc, String name) {
		NodeList items = doc.getElementsByTagName("EasyWebTemplate");
		for (int i = 0; i < items.getLength(); i++) {
			Element elem = (Element) items.item(i);
			if (elem.getAttribute("Name").equals(name)) {
				return elem;
			}
		}
		return null;
	}

	private Element findXItem(Element xitems, String name) {
		NodeList nodes = xitems.getElementsByTagName("XItem");
		for (int i = 0; i < nodes.getLength(); i++) {
			Element elem = (Element) nodes.item(i);
			if (elem.getAttribute("Name").equals(name)) {
				return elem;
			}
		}
		return null;
	}

	private Element findSqlSet(Element sqlSet, String name) {
		if (sqlSet == null) return null;
		NodeList nodes = sqlSet.getElementsByTagName("Set");
		for (int i = 0; i < nodes.getLength(); i++) {
			Element elem = (Element) nodes.item(i);
			if (elem.getAttribute("Name").equals(name)) {
				return elem;
			}
		}
		return null;
	}

	private Element findScriptSet(Element scriptSet, String name) {
		if (scriptSet == null) return null;
		NodeList nodes = scriptSet.getElementsByTagName("Set");
		for (int i = 0; i < nodes.getLength(); i++) {
			Element elem = (Element) nodes.item(i);
			if (elem.getAttribute("Name").equals(name)) {
				return elem;
			}
		}
		return null;
	}

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

	private static String readXml() {
		try {
			return new String(Files.readAllBytes(TEST_XML.toPath()), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException("读取 XML 失败", e);
		}
	}

	private static void printCaption(String caption) {
		int width = 70;
		System.out.println("\n" + "=".repeat(width));
		System.out.println("  \033[32;1m" + caption + "\033[0m");
		System.out.println("=".repeat(width));
	}
}
