package com.gdxsoft.easyweb.define.servlets;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UXml;

/**
 * ConfigValidator 单元测试
 * 覆盖 validateItemXml / validateFrameType / validateOperationType
 */
class ConfigValidatorTest {

    // ==================== validateItemXml — 正常 ====================

    @Test
    @DisplayName("合法 XML 通过校验")
    void testValidItemXml() {
        String xml = buildValidXml("test_item", "ListFrame",
                "<XItem><Tag><Set Tag=\"text\"/></Tag><Name><Set Name=\"field1\"/></Name></XItem>");

        ConfigValidator.ValidationResult r = ConfigValidator.validateItemXml(xml, "test_item");
        assertTrue(r.isValid(), r.getErrorMessage());
    }

    @Test
    @DisplayName("无 XItem 的 XML 通过校验")
    void testValidXmlNoXItems() {
        String xml = buildValidXml("sql_item", "ListFrame", "");
        ConfigValidator.ValidationResult r = ConfigValidator.validateItemXml(xml, "sql_item");
        assertTrue(r.isValid(), r.getErrorMessage());
    }

    @Test
    @DisplayName("全部 FrameTag 值通过")
    void testAllValidFrameTags() {
        String[] tags = { "ListFrame", "Frame", "Tree", "Menu", "Grid",
                "MultiGrid", "Logic", "Report", "Combine", "Complex" };
        for (String ft : tags) {
            String xml = buildValidXml("t_" + ft, ft, "");
            ConfigValidator.ValidationResult r = ConfigValidator.validateItemXml(xml, "t_" + ft);
            assertTrue(r.isValid(), "FrameTag " + ft + " should be valid: " + r.getErrorMessage());
        }
    }

    @Test
    @DisplayName("多种合法 XItem Tag 通过")
    void testValidXItemTags() {
        String[] tags = { "text", "textarea", "span", "hidden", "password", "combo",
                "select", "checkbox", "switch", "radio", "button", "submit", "date",
                "datetime", "dHtml5", "h5upload", "valid", "QRCode", "SqlEditor" };
        for (String tag : tags) {
            String xml = buildValidXml("xi_" + tag, "Frame",
                    "<XItem><Tag><Set Tag=\"" + tag + "\"/></Tag><Name><Set Name=\"fld\"/></Name></XItem>");
            ConfigValidator.ValidationResult r = ConfigValidator.validateItemXml(xml, "xi_" + tag);
            assertTrue(r.isValid(), "Tag " + tag + " should be valid: " + r.getErrorMessage());
        }
    }

    @Test
    @DisplayName("带 CDATA 的 XML 通过")
    void testXmlWithCdata() {
        String xml = buildValidXml("cd", "Frame",
                "<XItem><Tag><Set Tag=\"SqlEditor\"/></Tag>"
                        + "<Name><Set Name=\"sql\"/></Name>"
                        + "<SqlSet><CSSet><CS><Sql><![CDATA[SELECT * FROM t WHERE x=@x]]></Sql></CS></CSSet></SqlSet>"
                        + "</XItem>");
        assertTrue(ConfigValidator.validateItemXml(xml, "cd").isValid());
    }

    // ==================== validateItemXml — 异常/XItem Tag ====================

    @Test
    @DisplayName("非法 XItem Tag 被拒绝")
    void testInvalidXItemTag() {
        String xml = buildValidXml("bad", "Frame",
                "<XItem><Tag><Set Tag=\"NONEXISTENT_TAG\"/></Tag><Name><Set Name=\"f1\"/></Name></XItem>");
        ConfigValidator.ValidationResult r = ConfigValidator.validateItemXml(xml, "bad");
        assertFalse(r.isValid());
        assertTrue(r.getErrorMessage().contains("NONEXISTENT_TAG"));
    }

    @Test
    @DisplayName("XItem 缺少 Tag 被拒绝")
    void testXItemMissingTag() {
        String xml = buildValidXml("no_tag", "Frame",
                "<XItem><Name><Set Name=\"f1\"/></Name></XItem>");
        ConfigValidator.ValidationResult r = ConfigValidator.validateItemXml(xml, "no_tag");
        assertFalse(r.isValid());
        assertTrue(r.getErrorMessage().contains("Tag"));
    }

    // ==================== validateItemXml — 异常/根元素 ====================

    @Test
    @DisplayName("空/null 被拒绝")
    void testEmptyOrNullXml() {
        assertFalse(ConfigValidator.validateItemXml("   ", "any").isValid());
        assertFalse(ConfigValidator.validateItemXml(null, "any").isValid());
    }

    @Test
    @DisplayName("错误根元素被拒绝")
    void testWrongRootElement() {
        ConfigValidator.ValidationResult r = ConfigValidator
                .validateItemXml("<OtherRoot Name=\"x\"><Page/></OtherRoot>", "x");
        assertFalse(r.isValid());
        assertTrue(r.getErrorMessage().contains("EasyWebTemplate"));
    }

    @Test
    @DisplayName("缺少 Name 属性被拒绝")
    void testMissingNameAttribute() {
        String xml = "<EasyWebTemplate><Page><FrameTag><Set FrameTag=\"Frame\"/></FrameTag></Page></EasyWebTemplate>";
        ConfigValidator.ValidationResult r = ConfigValidator.validateItemXml(xml, null);
        assertFalse(r.isValid());
        assertTrue(r.getErrorMessage().contains("Name"));
    }

    @Test
    @DisplayName("Name 与 itemname 不匹配被拒绝")
    void testNameMismatch() {
        String xml = buildValidXml("wrong_name", "ListFrame", "");
        ConfigValidator.ValidationResult r = ConfigValidator.validateItemXml(xml, "expected_name");
        assertFalse(r.isValid());
        assertTrue(r.getErrorMessage().contains("不匹配")
                || r.getErrorMessage().toLowerCase().contains("mismatch"),
                "Error: " + r.getErrorMessage());
    }

    // ==================== validateItemXml — 异常/结构 ====================

    @Test
    @DisplayName("非法 XML 语法被拒绝")
    void testMalformedXml() {
        // 缺少闭合标签
        String xml = "<EasyWebTemplate Name=\"x\"><Page><FrameTag><Set FrameTag=\"Frame\"/>";
        ConfigValidator.ValidationResult r = ConfigValidator.validateItemXml(xml, "x");
        assertFalse(r.isValid());
    }

    @Test
    @DisplayName("缺少 Page 被拒绝")
    void testMissingPage() {
        ConfigValidator.ValidationResult r = ConfigValidator
                .validateItemXml("<EasyWebTemplate Name=\"x\"></EasyWebTemplate>", "x");
        assertFalse(r.isValid());
        assertTrue(r.getErrorMessage().contains("Page"));
    }

    @Test
    @DisplayName("缺少 FrameTag 被拒绝")
    void testMissingFrameTag() {
        String xml = "<EasyWebTemplate Name=\"x\"><Page><Name><Set Name=\"x\"/></Name></Page></EasyWebTemplate>";
        ConfigValidator.ValidationResult r = ConfigValidator.validateItemXml(xml, "x");
        assertFalse(r.isValid());
        assertTrue(r.getErrorMessage().contains("FrameTag"));
    }

    @Test
    @DisplayName("无效 FrameTag 值被拒绝")
    void testInvalidFrameTagValue() {
        String xml = buildValidXml("bad_ft", "BOGUS_FRAME", "");
        ConfigValidator.ValidationResult r = ConfigValidator.validateItemXml(xml, "bad_ft");
        assertFalse(r.isValid());
        assertTrue(r.getErrorMessage().contains("BOGUS_FRAME"));
    }

    @Test
    @DisplayName("Page/Name 与根 Name 不一致（非阻断：fixXml 自动修正）")
    void testPageNameMismatchTolerated() {
        String xml = "<EasyWebTemplate Name=\"root\">"
                + "<Page><FrameTag><Set FrameTag=\"ListFrame\"/></FrameTag>"
                + "<Name><Set Name=\"different\"/></Name></Page></EasyWebTemplate>";
        ConfigValidator.ValidationResult r = ConfigValidator.validateItemXml(xml, "root");
        // fixXml 会自动修正 Page/Name，因此校验通过
        assertTrue(r.isValid(), "Page/Name mismatch is auto-corrected by fixXml: " + r.getErrorMessage());
    }

    // ==================== validateFrameType ====================

    @Test
    @DisplayName("全部合法 FrameType 通过")
    void testValidFrameTypes() {
        String[] types = { "ListFrame", "Frame", "Tree", "Menu", "Grid", "MultiGrid",
                "Logic", "Report", "Combine", "Complex" };
        for (String ft : types) {
            assertTrue(ConfigValidator.validateFrameType(ft).isValid(), ft);
            assertTrue(ConfigValidator.validateFrameType(ft.toLowerCase()).isValid(), ft.toLowerCase());
        }
    }

    @Test
    @DisplayName("非法 FrameType 被拒绝")
    void testInvalidFrameType() {
        ConfigValidator.ValidationResult r = ConfigValidator.validateFrameType("BogusFrame");
        assertFalse(r.isValid());
        assertTrue(r.getErrorMessage().contains("BogusFrame"));
        assertFalse(ConfigValidator.validateFrameType("").isValid());
        assertFalse(ConfigValidator.validateFrameType(null).isValid());
    }

    // ==================== validateOperationType ====================

    @Test
    @DisplayName("合法 OperationType 通过")
    void testValidOperationTypes() {
        for (String ot : new String[] { "N", "M", "V", "NM" }) {
            assertTrue(ConfigValidator.validateOperationType(ot).isValid(), ot);
            assertTrue(ConfigValidator.validateOperationType(ot.toLowerCase()).isValid(), ot.toLowerCase());
        }
    }

    @Test
    @DisplayName("非法 OperationType 被拒绝")
    void testInvalidOperationType() {
        ConfigValidator.ValidationResult r = ConfigValidator.validateOperationType("XX");
        assertFalse(r.isValid());
        assertTrue(r.getErrorMessage().contains("XX"));
        assertFalse(ConfigValidator.validateOperationType("").isValid());
        assertFalse(ConfigValidator.validateOperationType(null).isValid());
    }

    // ==================== 边界/空值 ====================

    @Test
    @DisplayName("null 输入被校验方法拒绝")
    void testNullRejected() {
        assertFalse(ConfigValidator.validateItemXml(null, "any").isValid());
        assertFalse(ConfigValidator.validateFrameType(null).isValid());
        assertFalse(ConfigValidator.validateOperationType(null).isValid());
        assertFalse(ConfigValidator.validateSql("demo", null).isValid());
        assertFalse(ConfigValidator.validateSql(null, "SELECT 1").isValid());
    }

    @Test
    @DisplayName("空字符串被校验方法拒绝")
    void testEmptyStringRejected() {
        assertFalse(ConfigValidator.validateItemXml("", "any").isValid());
        assertFalse(ConfigValidator.validateFrameType("").isValid());
        assertFalse(ConfigValidator.validateOperationType("").isValid());
        assertFalse(ConfigValidator.validateSql("demo", "").isValid());
        assertFalse(ConfigValidator.validateSql("", "SELECT 1").isValid());
    }

    // ==================== 集成测试：验证 define.xml/ewa/*.xml ====================

    @Test
    @DisplayName("define.xml/ewa/*.xml 中所有配置项通过校验")
    void testAllDefineXmlItems() {
        String defineDir = "src/main/resources/define.xml/ewa/";
        File dir = new File(defineDir);
        if (!dir.exists() || !dir.isDirectory()) {
            // CI 环境可能没有这个目录，跳过
            System.out.println("Skipping integration test: " + defineDir + " not found");
            return;
        }

        File[] xmlFiles = dir.listFiles((d, name) -> name.endsWith(".xml") && !name.endsWith(".json"));
        if (xmlFiles == null || xmlFiles.length == 0) {
            System.out.println("No XML files found in " + defineDir);
            return;
        }

        List<String> failures = new ArrayList<>();
        int totalItems = 0;
        int totalXItems = 0;

        for (File xmlFile : xmlFiles) {
            try {
                String content = new String(java.nio.file.Files.readAllBytes(xmlFile.toPath()),
                        java.nio.charset.StandardCharsets.UTF_8);
                Document doc = UXml.asDocument(content);
                if (doc == null) {
                    failures.add(xmlFile.getName() + ": XML 解析失败 (doc=null)");
                    continue;
                }

                Element root = doc.getDocumentElement();
                if (root == null || !"EasyWebTemplates".equals(root.getNodeName())) {
                    failures.add(xmlFile.getName() + ": 根元素不是 <EasyWebTemplates>，实际为 "
                            + (root != null ? root.getNodeName() : "null"));
                    continue;
                }

                // 遍历每个 EasyWebTemplate 子节点
                NodeList children = root.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node child = children.item(i);
                    if (child.getNodeType() != Node.ELEMENT_NODE
                            || !"EasyWebTemplate".equals(child.getNodeName())) {
                        continue;
                    }

                    totalItems++;
                    Element item = (Element) child;
                    String itemName = item.getAttribute("Name");
                    if (itemName == null || itemName.isEmpty()) {
                        itemName = "(unnamed #" + i + ")";
                    }

                    // 统计 XItem 数量
                    NodeList xitems = item.getElementsByTagName("XItem");
                    totalXItems += xitems.getLength();

                    // 提取单个 EasyWebTemplate 的 XML 并校验
                    String itemXml = UXml.asXml(item);

                    ConfigValidator.ValidationResult vr = ConfigValidator.validateItemXml(itemXml, itemName);
                    if (!vr.isValid()) {
                        failures.add(xmlFile.getName() + " / " + itemName + ": " + vr.getErrorMessage());
                    }
                }
            } catch (Exception e) {
                failures.add(xmlFile.getName() + ": 处理异常 - " + e.getMessage());
            }
        }

        System.out.println("Scanned " + xmlFiles.length + " files, "
                + totalItems + " EasyWebTemplate items, "
                + totalXItems + " XItem elements");

        if (!failures.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n========== define.xml 校验失败 (").append(failures.size()).append(" 项) ==========\n");
            for (String f : failures) {
                sb.append("  FAIL: ").append(f).append("\n");
            }
            sb.append("================================================================");
            System.out.println(sb);
            fail(sb.toString());
        }
    }

    // ==================== bad-ewa-cfgs 测试 ====================

    @Test
    @DisplayName("bad-ewa-cfgs/bad1.xml 校验")
    void testBad1Xml() throws Exception {
        File badFile = new File("src/test/resources/bad-ewa-cfgs/bad1.xml");
        assertTrue(badFile.exists(), "bad1.xml should exist");

        String content = new String(java.nio.file.Files.readAllBytes(badFile.toPath()),
                java.nio.charset.StandardCharsets.UTF_8);

        Document doc = UXml.asDocument(content);
        assertNotNull(doc, "XML should be parsable");

        Element root = doc.getDocumentElement();
        String itemName = root.getAttribute("Name");
        System.out.println("bad1.xml itemName=" + itemName);

        ConfigValidator.ValidationResult r = ConfigValidator.validateItemXml(content, itemName);
        System.out.println("bad1.xml valid=" + r.isValid());
        if (!r.isValid()) {
            System.out.println("bad1.xml error=" + r.getErrorMessage());
        }

        // bad1.xml 的 AddScript 内含有非法子元素 AddPreRow
        assertFalse(r.isValid(), "bad1.xml should be invalid");
        assertTrue(r.getErrorMessage().contains("AddPreRow"), "Should flag AddPreRow: " + r.getErrorMessage());
    }

    // ==================== validateSql 测试 ====================

    @Test
    @DisplayName("validateSql null/空参数被拒绝")
    void testValidateSqlNullEmpty() {
        assertFalse(ConfigValidator.validateSql(null, "SELECT 1").isValid());
        assertFalse(ConfigValidator.validateSql("", "SELECT 1").isValid());
        assertFalse(ConfigValidator.validateSql("demo", null).isValid());
        assertFalse(ConfigValidator.validateSql("demo", "").isValid());
        assertFalse(ConfigValidator.validateSql("demo", "   ").isValid());
    }

    @Test
    @DisplayName("validateSql 无效数据库名被拒绝")
    void testValidateSqlInvalidDb() {
        ConfigValidator.ValidationResult r = ConfigValidator.validateSql("NONEXISTENT_DB_XYZ", "SELECT 1");
        assertFalse(r.isValid(), "Non-existent DB should fail validation, got: " + r);
    }

    @Test
    @DisplayName("validateSql 合法 SQL 通过校验（HSQLDB）")
    void testValidateSqlValidHsqldb() {
        // 初始化 HSQLDB 测试库
        com.gdxsoft.easyweb.datasource.DataConnection cnn = null;
        try {
            cnn = com.gdxsoft.easyweb.testutils.SqlImporter.initTestDatabase();
        } catch (Exception e) {
            System.out.println("Skipping validateSql integration test: HSQLDB init failed: " + e.getMessage());
        }
        if (cnn == null) {
            System.out.println("Skipping validateSql integration test: no DB available");
            return;
        }
        try { cnn.close(); } catch (Exception ignored) { }

        // 合法 SELECT
        ConfigValidator.ValidationResult r1 = ConfigValidator.validateSql("demo",
                "SELECT * FROM CRM_COM WHERE 1=2");
        System.out.println("Valid SQL result: " + r1);
        assertTrue(r1.isValid(), "Valid SELECT should pass: " + r1.getErrorMessage());

        // 合法 INSERT（事务内回滚，不会实际写入）
        ConfigValidator.ValidationResult r2 = ConfigValidator.validateSql("demo",
                "INSERT INTO CRM_COM (SUP_ID, CRM_COM_NAME) VALUES (999, 'test')");
        assertTrue(r2.isValid(), "Valid INSERT should pass (rollback): " + r2.getErrorMessage());

        // 非法 SQL 应该被拒绝
        ConfigValidator.ValidationResult r3 = ConfigValidator.validateSql("demo",
                "SELECT * FROM NONEXISTENT_TABLE_XYZ");
        assertFalse(r3.isValid(), "Invalid table should fail");
    }

    @Test
    @DisplayName("validateSql 多条 SQL 语句校验")
    void testValidateSqlMultipleStatements() {
        com.gdxsoft.easyweb.datasource.DataConnection cnn = null;
        try {
            cnn = com.gdxsoft.easyweb.testutils.SqlImporter.initTestDatabase();
        } catch (Exception e) {
            System.out.println("Skipping multi-SQL test: " + e.getMessage());
        }
        if (cnn == null) {
            System.out.println("Skipping multi-SQL test: no DB available");
            return;
        }
        try { cnn.close(); } catch (Exception ignored) { }

        // 多条合法 SQL（分号分隔）
        String multiSql = "SELECT 1 FROM CRM_COM WHERE 1=2; SELECT 2 FROM CRM_COM WHERE 1=2";
        ConfigValidator.ValidationResult r = ConfigValidator.validateSql("demo", multiSql);
        assertTrue(r.isValid(), "Multiple valid SQLs should pass: " + r.getErrorMessage());
    }

    // ==================== validateSql 安全检查 ====================

    @Test
    @DisplayName("validateSql 拒绝 DROP TABLE")
    void testSafetyRejectDrop() {
        ConfigValidator.ValidationResult r = ConfigValidator.validateSql("demo", "DROP TABLE CRM_COM");
        assertFalse(r.isValid());
        assertTrue(r.getErrorMessage().contains("DDL"), r.getErrorMessage());
    }

    @Test
    @DisplayName("validateSql 拒绝 TRUNCATE")
    void testSafetyRejectTruncate() {
        ConfigValidator.ValidationResult r = ConfigValidator.validateSql("demo", "TRUNCATE TABLE CRM_COM");
        assertFalse(r.isValid());
        assertTrue(r.getErrorMessage().contains("DDL"), r.getErrorMessage());
    }

    @Test
    @DisplayName("validateSql 拒绝 DELETE without WHERE")
    void testSafetyRejectDeleteNoWhere() {
        ConfigValidator.ValidationResult r = ConfigValidator.validateSql("demo", "DELETE FROM CRM_COM");
        assertFalse(r.isValid());
        assertTrue(r.getErrorMessage().contains("WHERE"), r.getErrorMessage());
    }

    @Test
    @DisplayName("validateSql 允许 DELETE with WHERE")
    void testSafetyAllowDeleteWithWhere() {
        ConfigValidator.ValidationResult r = ConfigValidator.validateSql("demo",
                "DELETE FROM CRM_COM WHERE CRM_COM_ID=1");
        if (!r.isValid()) {
            assertFalse(r.getErrorMessage().contains("not allowed"),
                    "DELETE with WHERE should pass safety check: " + r.getErrorMessage());
        }
    }

    @Test
    @DisplayName("validateSql 拒绝 CREATE DATABASE")
    void testSafetyRejectCreateDatabase() {
        ConfigValidator.ValidationResult r = ConfigValidator.validateSql("demo", "CREATE DATABASE testdb");
        assertFalse(r.isValid());
        assertTrue(r.getErrorMessage().contains("DDL"), r.getErrorMessage());
    }

    @Test
    @DisplayName("validateSql 拒绝 SELECT INTO")
    void testSafetyRejectSelectInto() {
        ConfigValidator.ValidationResult r = ConfigValidator.validateSql("demo",
                "SELECT * INTO new_table FROM CRM_COM");
        assertFalse(r.isValid());
        assertTrue(r.getErrorMessage().contains("SELECT INTO"), r.getErrorMessage());
    }

    @Test
    @DisplayName("validateSql 允许 INSERT INTO SELECT")
    void testSafetyAllowInsertIntoSelect() {
        ConfigValidator.ValidationResult r = ConfigValidator.validateSql("demo",
                "INSERT INTO CRM_COM (SUP_ID, CRM_COM_NAME) SELECT 1, 'test' FROM CRM_COM WHERE 1=2");
        if (!r.isValid()) {
            assertFalse(r.getErrorMessage().contains("not allowed"),
                    "INSERT INTO SELECT should pass safety check: " + r.getErrorMessage());
        }
    }

    @Test
    @DisplayName("validateSql 拒绝注释内藏 DROP")
    void testSafetyRejectDropInComments() {
        ConfigValidator.ValidationResult r = ConfigValidator.validateSql("demo",
                "SELECT * FROM CRM_COM -- DROP TABLE CRM_COM");
        if (!r.isValid()) {
            assertFalse(r.getErrorMessage().contains("not allowed"),
                    "DROP in comment should be ignored: " + r.getErrorMessage());
        }
    }

    @Test
    @DisplayName("validateSql 拒绝多语句中含危险操作")
    void testSafetyRejectMultiWithDanger() {
        ConfigValidator.ValidationResult r = ConfigValidator.validateSql("demo",
                "SELECT 1 FROM CRM_COM; DROP TABLE CRM_COM; SELECT 2");
        assertFalse(r.isValid());
        assertTrue(r.getErrorMessage().contains("stmt #2"), "Should flag stmt #2: " + r.getErrorMessage());
        assertTrue(r.getErrorMessage().contains("DDL"), r.getErrorMessage());
    }

    @Test
    @DisplayName("validateSql 拒绝 ALTER TABLE")
    void testSafetyRejectAlter() {
        assertFalse(ConfigValidator.validateSql("demo", "ALTER TABLE CRM_COM ADD COLUMN x INT").isValid());
    }

    @Test
    @DisplayName("validateSql 拒绝 RENAME TABLE")
    void testSafetyRejectRename() {
        assertFalse(ConfigValidator.validateSql("demo", "RENAME TABLE CRM_COM TO CRM_COM_OLD").isValid());
    }

    @Test
    @DisplayName("validateSql 拒绝 GRANT")
    void testSafetyRejectGrant() {
        assertFalse(ConfigValidator.validateSql("demo", "GRANT SELECT ON CRM_COM TO user1").isValid());
    }

    @Test
    @DisplayName("validateSql 拒绝 REVOKE")
    void testSafetyRejectRevoke() {
        assertFalse(ConfigValidator.validateSql("demo", "REVOKE SELECT ON CRM_COM FROM user1").isValid());
    }

    @Test
    @DisplayName("validateSql 拒绝 REPLACE")
    void testSafetyRejectReplace() {
        assertFalse(ConfigValidator.validateSql("demo", "REPLACE INTO CRM_COM (SUP_ID, CRM_COM_NAME) VALUES (1,'x')").isValid());
    }

    @Test
    @DisplayName("validateSql 拒绝 LOCK TABLES")
    void testSafetyRejectLock() {
        assertFalse(ConfigValidator.validateSql("demo", "LOCK TABLES CRM_COM READ").isValid());
    }

    @Test
    @DisplayName("validateSql 拒绝 SET GLOBAL")
    void testSafetyRejectSetGlobal() {
        assertFalse(ConfigValidator.validateSql("demo", "SET GLOBAL max_connections=100").isValid());
    }

    @Test
    @DisplayName("validateSql 允许 SET session 变量")
    void testSafetyAllowSetSession() {
        ConfigValidator.ValidationResult r = ConfigValidator.validateSql("demo", "SET @myvar = 1");
        if (!r.isValid()) {
            assertFalse(r.getErrorMessage().contains("not allowed"), "SET session var: " + r.getErrorMessage());
        }
    }

    @Test
    @DisplayName("validateSql 拒绝 KILL")
    void testSafetyRejectKill() {
        assertFalse(ConfigValidator.validateSql("demo", "KILL CONNECTION 123").isValid());
    }

    @Test
    @DisplayName("validateSql 拒绝 FLUSH")
    void testSafetyRejectFlush() {
        assertFalse(ConfigValidator.validateSql("demo", "FLUSH TABLES").isValid());
    }

    @Test
    @DisplayName("validateSql 拒绝 HANDLER")
    void testSafetyRejectHandler() {
        assertFalse(ConfigValidator.validateSql("demo", "HANDLER CRM_COM OPEN").isValid());
    }

    @Test
    @DisplayName("validateSql 拒绝 LOAD DATA")
    void testSafetyRejectLoadData() {
        assertFalse(ConfigValidator.validateSql("demo", "LOAD DATA INFILE '/tmp/data.csv' INTO TABLE CRM_COM").isValid());
    }

    @Test
    @DisplayName("validateSql 拒绝 ANALYZE TABLE")
    void testSafetyRejectAnalyze() {
        assertFalse(ConfigValidator.validateSql("demo", "ANALYZE TABLE CRM_COM").isValid());
    }

    @Test
    @DisplayName("validateSql 拒绝 SELECT INTO OUTFILE")
    void testSafetyRejectSelectIntoOutfile() {
        assertFalse(ConfigValidator.validateSql("demo", "SELECT * INTO OUTFILE '/tmp/out.csv' FROM CRM_COM").isValid());
    }

    @Test
    @DisplayName("validateSql 拒绝 XA 事务")
    void testSafetyRejectXa() {
        assertFalse(ConfigValidator.validateSql("demo", "XA START 'xid1'").isValid());
    }

    @Test
    @DisplayName("validateSql 拒绝 CREATE TABLE (DDL)")
    void testSafetyRejectCreateTable() {
        assertFalse(ConfigValidator.validateSql("demo", "CREATE TABLE t1 (id INT)").isValid());
    }

    @Test
    @DisplayName("validateSql 拒绝 CREATE\\tTABLE (tab 分隔)")
    void testSafetyRejectCreateTableTab() {
        // Tab between keywords must not bypass the check
        assertFalse(ConfigValidator.validateSql("demo", "CREATE\tTABLE t1 (id INT)").isValid());
    }

    @Test
    @DisplayName("validateSql 拒绝多空格 CREATE  TABLE")
    void testSafetyRejectCreateTableMultiSpace() {
        assertFalse(ConfigValidator.validateSql("demo", "CREATE  TABLE t1 (id INT)").isValid());
    }

    @Test
    @DisplayName("validateSql 拒绝换行 CREATE\\nTABLE")
    void testSafetyRejectCreateTableNewline() {
        assertFalse(ConfigValidator.validateSql("demo", "CREATE\nTABLE t1 (id INT)").isValid());
    }

    @Test
    @DisplayName("validateSql 拒绝 CREATE/*comment*/TABLE")
    void testSafetyRejectCreateCommentTable() {
        assertFalse(ConfigValidator.validateSql("demo", "CREATE/* aa */TABLE t1 (id INT)").isValid());
    }

    @Test
    @DisplayName("validateSql 拒绝 DROP/*c1*/--c2\\nTABLE")
    void testSafetyRejectDropMixedComments() {
        assertFalse(ConfigValidator.validateSql("demo", "DROP/*c1*/--c2\nTABLE t1").isValid());
    }

    // ==================== bypass 修复验证 ====================

    @Test
    @DisplayName("validateSql 拒绝 /*!...*/ MySQL 可执行注释")
    void testSafetyRejectExecutableComment() {
        // /*!50000 DROP TABLE x */ is valid MySQL that WOULD execute
        assertFalse(ConfigValidator.validateSql("demo", "/*!50000 DROP TABLE x */").isValid());
        // Even safe-looking content via /*! should be blocked
        assertFalse(ConfigValidator.validateSql("demo", "/*!40000 ALTER TABLE t ADD COLUMN c INT */").isValid());
    }

    @Test
    @DisplayName("validateSql 拒绝 # MySQL 注释绕过")
    void testSafetyRejectHashComment() {
        // # is a MySQL line comment, CREATE#c\nTABLE = CREATE TABLE
        assertFalse(ConfigValidator.validateSql("demo", "CREATE#comment\nTABLE t1 (id INT)").isValid());
        assertFalse(ConfigValidator.validateSql("demo", "DROP#x\nTABLE t1").isValid());
    }

    @Test
    @DisplayName("validateSql 拒绝 ; 在注释中绕过")
    void testSafetyRejectSemicolonInComment() {
        // After removing /* */, DROP becomes visible as a separate statement
        assertFalse(ConfigValidator.validateSql("demo", "SELECT 1;/*comment*/DROP TABLE x").isValid());
    }

    @Test
    @DisplayName("validateSql 允许 SELECT INTO @variable")
    void testSafetyAllowSelectIntoVariable() {
        // SELECT ... INTO @var is legitimate MySQL variable assignment
        ConfigValidator.ValidationResult r = ConfigValidator.validateSql("demo",
                "SELECT a INTO @myvar FROM CRM_COM WHERE 1=2");
        if (!r.isValid()) {
            assertFalse(r.getErrorMessage().contains("SELECT INTO"),
                    "SELECT INTO @var should be allowed: " + r.getErrorMessage());
        }
    }

    @Test
    @DisplayName("validateSql 拒绝 SELECT INTO table (not @var)")
    void testSafetyRejectSelectIntoTable() {
        assertFalse(ConfigValidator.validateSql("demo",
                "SELECT * INTO new_table FROM CRM_COM").isValid());
    }

    // ==================== TEMPORARY TABLE 允许 ====================

    @Test
    @DisplayName("validateSql 允许 CREATE TEMPORARY TABLE")
    void testSafetyAllowCreateTempTable() {
        // CREATE TEMPORARY TABLE: no implicit commit, session-scoped
        ConfigValidator.ValidationResult r = ConfigValidator.validateSql("demo",
                "CREATE TEMPORARY TABLE temp_t (id INT)");
        if (!r.isValid()) {
            assertFalse(r.getErrorMessage().contains("DDL"),
                    "CREATE TEMPORARY TABLE should be allowed: " + r.getErrorMessage());
        }
    }

    @Test
    @DisplayName("validateSql 允许 DROP TEMPORARY TABLE")
    void testSafetyAllowDropTempTable() {
        ConfigValidator.ValidationResult r = ConfigValidator.validateSql("demo",
                "DROP TEMPORARY TABLE IF EXISTS temp_t");
        if (!r.isValid()) {
            assertFalse(r.getErrorMessage().contains("DDL"),
                    "DROP TEMPORARY TABLE should be allowed: " + r.getErrorMessage());
        }
    }

    @Test
    @DisplayName("validateSql 仍拒绝 CREATE TABLE (非 TEMPORARY)")
    void testSafetyRejectCreateRegularTable() {
        assertFalse(ConfigValidator.validateSql("demo", "CREATE TABLE t1 (id INT)").isValid());
    }

    @Test
    @DisplayName("validateSql 允许 CREATE TEMP TABLE (MySQL 同义词)")
    void testSafetyAllowCreateTempTableShort() {
        // TEMP is a MySQL synonym for TEMPORARY
        ConfigValidator.ValidationResult r = ConfigValidator.validateSql("demo",
                "CREATE TEMP TABLE t (id INT)");
        if (!r.isValid()) {
            assertFalse(r.getErrorMessage().contains("DDL"),
                    "CREATE TEMP TABLE should be allowed: " + r.getErrorMessage());
        }
    }

    @Test
    @DisplayName("validateSql 允许 CREATE GLOBAL TEMPORARY TABLE (Oracle/PostgreSQL)")
    void testSafetyAllowCreateGlobalTempTable() {
        ConfigValidator.ValidationResult r = ConfigValidator.validateSql("demo",
                "CREATE GLOBAL TEMPORARY TABLE t (id INT)");
        if (!r.isValid()) {
            assertFalse(r.getErrorMessage().contains("DDL"),
                    "CREATE GLOBAL TEMPORARY TABLE should be allowed: " + r.getErrorMessage());
        }
    }

    // ==================== 辅助方法 ====================

    static String buildValidXml(String itemName, String frameTag, String xitemsXml) {
        StringBuilder sb = new StringBuilder();
        sb.append("<EasyWebTemplate Name=\"").append(itemName).append("\">");
        sb.append("<Page>");
        sb.append("<FrameTag><Set FrameTag=\"").append(frameTag).append("\"/></FrameTag>");
        sb.append("<Name><Set Name=\"").append(itemName).append("\"/></Name>");
        sb.append("<SkinName><Set SkinName=\"Test1\"/></SkinName>");
        sb.append("<DataSource><Set DataSource=\"\"/></DataSource>");
        sb.append("<DescriptionSet><Set Info=\"Test\" Lang=\"zhcn\"/></DescriptionSet>");
        if (xitemsXml != null && !xitemsXml.isEmpty()) {
            sb.append("<XItems>").append(xitemsXml).append("</XItems>");
        }
        sb.append("</Page>");
        sb.append("</EasyWebTemplate>");
        return sb.toString();
    }
}
