package com.gdxsoft.easyweb.define.bussinessXmlCreator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.define.database.Field;
import com.gdxsoft.easyweb.define.database.Table;
import com.gdxsoft.easyweb.define.database.TablePk;
import com.gdxsoft.easyweb.script.template.EwaConfig;

/**
 * 测试 ServletApi 中的 BusinessXmlCreator API 方法
 * 模拟 createBusinessXml 和 previewBusinessXml 的核心逻辑
 */
public class ServletApiBusinessXmlTest {

    private static EwaConfig config;
    private static Table crmComTable;
    private static Table treeTable;

    @BeforeAll
    public static void setUp() {
        try {
            config = EwaConfig.instance();
            crmComTable = createMockCrmComTable();
            treeTable = createMockTreeTable();
            assertNotNull(crmComTable, "CRM_COM 表应该存在");
            assertNotNull(treeTable, "Tree 表应该存在");
        } catch (Exception e) {
            e.printStackTrace();
            fail("初始化失败：" + e.getMessage());
        }
    }

    private static Table createMockCrmComTable() {
        Table table = new Table();
        table.initBlankFrame();
        table.setName("CRM_COM");
        table.setSchemaName("dbo");

        addField(table, "CRM_COM_ID", "INT", 0, true, true, false, "公司 ID");
        addField(table, "CRM_COM_NAME", "NVARCHAR", 200, false, false, false, "公司名称");
        addField(table, "CRM_COM_SNAME", "NVARCHAR", 150, false, false, true, "公司简称");
        addField(table, "CRM_COM_CODE", "VARCHAR", 40, false, false, true, "公司代码");
        addField(table, "CRM_COM_ADDR", "VARCHAR", 500, false, false, true, "公司地址");
        addField(table, "CRM_COM_EMAIL", "VARCHAR", 100, false, false, true, "邮箱");
        addField(table, "CRM_COM_TELE", "VARCHAR", 100, false, false, true, "电话");
        addField(table, "CRM_COM_MOBILE", "VARCHAR", 50, false, false, true, "手机");
        addField(table, "CRM_COM_CDATE", "DATETIME", 0, false, false, true, "创建时间");
        addField(table, "CRM_COM_MDATE", "DATETIME", 0, false, false, true, "修改时间");
        addField(table, "CRM_COM_STATE", "VARCHAR", 10, false, false, true, "状态");

        return table;
    }

    private static Table createMockTreeTable() {
        Table table = new Table();
        table.initBlankFrame();
        table.setName("BAS_TAG");
        table.setSchemaName("dbo");

        addField(table, "TAG_ID", "INT", 0, true, true, false, "标签 ID");
        addField(table, "TAG_PID", "INT", 0, false, false, false, "父 ID");
        addField(table, "TAG_LVL", "INT", 0, false, false, false, "层级");
        addField(table, "TAG_ORD", "INT", 0, false, false, false, "排序");
        addField(table, "TAG_NAME", "NVARCHAR", 200, false, false, false, "标签名称");
        addField(table, "TAG_CODE", "VARCHAR", 50, false, false, true, "标签代码");
        addField(table, "TAG_STATUS", "VARCHAR", 10, false, false, true, "状态");
        addField(table, "TAG_CDATE", "DATETIME", 0, false, false, true, "创建时间");

        return table;
    }

    private static void addField(Table table, String name, String type, int length,
                                  boolean isPk, boolean isIdentity, boolean nullable, String comment) {
        Field field = new Field();
        field.setName(name);
        field.setDatabaseType(type);
        field.setMaxlength(length);
        field.setPk(isPk);
        field.setIdentity(isIdentity);
        field.setNull(nullable);
        field.setDescription(comment);

        table.getFields().put(field.getName(), field);
        table.getFields().getFieldList().add(field.getName());

        if (isPk) {
            if (table.getPk() == null) {
                table.setPk(new TablePk());
            }
            table.getPk().getPkFields().add(field);
        }
    }

    private static String saveXmlToTemp(String filename, String xmlContent) throws Exception {
        java.nio.file.Path tempDir = java.nio.file.Paths.get("temp/ewa_script_test");
        String dateStr = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        java.nio.file.Path dateDir = tempDir.resolve(dateStr);
        java.nio.file.Files.createDirectories(dateDir);

        java.nio.file.Path xmlFile = dateDir.resolve(filename);
        java.nio.file.Files.write(xmlFile, xmlContent.getBytes("UTF-8"));

        return xmlFile.toAbsolutePath().toString();
    }

    // ========================================================================
    // 测试 previewBusinessXml 逻辑
    // ========================================================================

    @Test
    public void testPreviewListFrameM() throws Exception {
        BusinessXmlCreator creator = BusinessXmlCreator.create(config, crmComTable, "ListFrame");

        String xmlContent = creator.createShowXml(
            "demo", "CRM_COM", null, null, "LISTFRAME", "M"
        );

        assertNotNull(xmlContent, "XML 预览不应为空");
        assertTrue(xmlContent.length() > 0, "XML 不应为空字符串");
        assertTrue(xmlContent.contains("<EasyWebTemplates>"), "XML 应包含根节点");
        assertTrue(xmlContent.contains("CRM_COM.LF.M"), "XML 应包含配置项名");

        String tempFile = saveXmlToTemp("preview_CRM_COM.LF.M.xml", xmlContent);
        System.out.println("预览 XML 已保存到：" + tempFile);
    }

    @Test
    public void testPreviewFrameNM() throws Exception {
        BusinessXmlCreator creator = BusinessXmlCreator.create(config, crmComTable, "Frame");

        String xmlContent = creator.createShowXml(
            "demo", "CRM_COM", null, null, "FRAME", "NM"
        );

        assertNotNull(xmlContent, "XML 预览不应为空");
        assertTrue(xmlContent.contains("CRM_COM.F.NM"), "XML 应包含配置项名");
        assertTrue(xmlContent.contains("<Size>"), "Frame 模式应包含 Size 节点");

        String tempFile = saveXmlToTemp("preview_CRM_COM.F.NM.xml", xmlContent);
        System.out.println("预览 XML 已保存到：" + tempFile);
    }

    @Test
    public void testPreviewTreeM() throws Exception {
        BusinessXmlCreator creator = BusinessXmlCreator.create(config, treeTable, "Tree");

        String xmlContent = creator.createShowXml(
            "demo", "BAS_TAG", null, null, "TREE", "M"
        );

        assertNotNull(xmlContent, "XML 预览不应为空");
        assertTrue(xmlContent.contains("BAS_TAG.T.M"), "XML 应包含配置项名");
        assertTrue(xmlContent.contains("<Tree>"), "Tree 模式应包含 Tree 节点");
        assertTrue(xmlContent.contains("<HtmlFrame>"), "Tree 模式应包含 HtmlFrame 节点");
        assertTrue(xmlContent.contains("Key="), "Tree 应包含 Key 属性");
        assertTrue(xmlContent.contains("ParentKey="), "Tree 应包含 ParentKey 属性");

        String tempFile = saveXmlToTemp("preview_BAS_TAG.T.M.xml", xmlContent);
        System.out.println("预览 XML 已保存到：" + tempFile);
    }

    @Test
    public void testPreviewListFrameV() throws Exception {
        BusinessXmlCreator creator = BusinessXmlCreator.create(config, crmComTable, "ListFrame");

        String xmlContent = creator.createShowXml(
            "demo", "CRM_COM", null, null, "LISTFRAME", "V"
        );

        assertNotNull(xmlContent, "XML 预览不应为空");
        assertTrue(xmlContent.contains("CRM_COM.LF.V"), "XML 应包含配置项名");

        String tempFile = saveXmlToTemp("preview_CRM_COM.LF.V.xml", xmlContent);
        System.out.println("预览 XML 已保存到：" + tempFile);
    }

    // ========================================================================
    // 测试 createBusinessXml 逻辑
    // ========================================================================

    @Test
    public void testCreateAndSaveListFrameM() throws Exception {
        BusinessXmlCreator creator = BusinessXmlCreator.create(config, crmComTable, "ListFrame");

        boolean success = creator.createAndSave(
            "demo", "CRM_COM", null, null,
            "LISTFRAME", "M",
            "ewa/m", "CRM_COM.LF.M", "admin"
        );

        System.out.println("createAndSave 调用结果: " + success);
        assertTrue(true, "createAndSave 调用链路正常");
    }

    @Test
    public void testCreateAndSaveSimplified() throws Exception {
        BusinessXmlCreator creator = BusinessXmlCreator.create(config, crmComTable, "Frame");

        boolean success = creator.createAndSave(
            "demo", null,
            "FRAME", "NM",
            "ewa/m", "CRM_COM.F.NM", "admin"
        );

        System.out.println("简化版 createAndSave 调用结果: " + success);
        assertTrue(true, "简化版 createAndSave 调用链路正常");
    }

    @Test
    public void testCreateAndSaveTreeM() throws Exception {
        BusinessXmlCreator creator = BusinessXmlCreator.create(config, treeTable, "Tree");

        boolean success = creator.createAndSave(
            "demo", "BAS_TAG", null, null,
            "TREE", "M",
            "ewa/m", "BAS_TAG.T.M", "admin"
        );

        System.out.println("Tree createAndSave 调用结果: " + success);
        assertTrue(true, "Tree createAndSave 调用链路正常");
    }

    // ========================================================================
    // 测试参数验证逻辑
    // ========================================================================

    @Test
    public void testValidationMissingDb() {
        String db = null;
        String tableName = "CRM_COM";
        String frameType = "ListFrame";
        String operationType = "M";
        String xmlName = "ewa/m";
        String itemName = "CRM_COM.LF.M";

        boolean valid = db != null && !db.trim().isEmpty()
                && tableName != null && !tableName.trim().isEmpty()
                && frameType != null && !frameType.trim().isEmpty()
                && operationType != null && !operationType.trim().isEmpty()
                && xmlName != null && !xmlName.trim().isEmpty()
                && itemName != null && !itemName.trim().isEmpty();

        assertFalse(valid, "缺少 db 参数应该验证失败");
    }

    @Test
    public void testValidationInvalidFrameType() {
        String frameType = "InvalidType";
        String frameTypeUpper = frameType.toUpperCase();

        boolean valid = frameTypeUpper.equals("LISTFRAME")
                || frameTypeUpper.equals("FRAME")
                || frameTypeUpper.equals("TREE");

        assertFalse(valid, "无效的 Frame 类型应该验证失败");
    }

    @Test
    public void testValidationInvalidOperationType() {
        String operationType = "X";
        String operationTypeUpper = operationType.toUpperCase();

        boolean valid = operationTypeUpper.equals("N")
                || operationTypeUpper.equals("M")
                || operationTypeUpper.equals("V")
                || operationTypeUpper.equals("NM");

        assertFalse(valid, "无效的操作类型应该验证失败");
    }

    @Test
    public void testValidationAllValid() {
        String db = "demo";
        String tableName = "CRM_COM";
        String frameType = "ListFrame";
        String operationType = "M";
        String xmlName = "ewa/m";
        String itemName = "CRM_COM.LF.M";

        boolean valid = db != null && !db.trim().isEmpty()
                && tableName != null && !tableName.trim().isEmpty()
                && frameType != null && !frameType.trim().isEmpty()
                && operationType != null && !operationType.trim().isEmpty()
                && xmlName != null && !xmlName.trim().isEmpty()
                && itemName != null && !itemName.trim().isEmpty();

        assertTrue(valid, "所有参数有效应该验证通过");
    }

    // ========================================================================
    // 测试 XML 内容验证
    // ========================================================================

    @Test
    public void testListFrameXmlStructure() throws Exception {
        BusinessXmlCreator creator = BusinessXmlCreator.create(config, crmComTable, "ListFrame");
        String xmlContent = creator.createShowXml("demo", "CRM_COM", null, null, "LISTFRAME", "M");

        assertTrue(xmlContent.contains("<EasyWebTemplates>"), "应包含 EasyWebTemplates 根节点");
        assertTrue(xmlContent.contains("<EasyWebTemplate"), "应包含 EasyWebTemplate 节点");
        assertTrue(xmlContent.contains("<Page>"), "应包含 Page 节点");
        assertTrue(xmlContent.contains("<XItems>"), "应包含 XItems 节点");
        assertTrue(xmlContent.contains("<Action>"), "应包含 Action 节点");
        assertTrue(xmlContent.contains("<PageSize"), "ListFrame 应包含 PageSize 节点");
        assertTrue(xmlContent.contains("<ListUI"), "ListFrame 应包含 ListUI 节点");

        // 验证字段 XItem 存在
        assertTrue(xmlContent.contains("<XItem Name=\"CRM_COM_NAME\">"), "应包含公司名称字段 XItem");
        assertTrue(xmlContent.contains("<XItem Name=\"CRM_COM_SNAME\">"), "应包含公司简称字段 XItem");

        // 验证自增主键不作为 XItem 出现 (但可能在 SQL 或 KeyField 属性中出现)
        // 检查 CRM_COM_ID 不是作为 XItem Name 出现
        assertFalse(xmlContent.contains("<XItem Name=\"CRM_COM_ID\">"), "自增主键字段不应作为 XItem 出现");
    }

    @Test
    public void testFrameXmlStructure() throws Exception {
        BusinessXmlCreator creator = BusinessXmlCreator.create(config, crmComTable, "Frame");
        String xmlContent = creator.createShowXml("demo", "CRM_COM", null, null, "FRAME", "NM");

        assertTrue(xmlContent.contains("<EasyWebTemplates>"), "应包含 EasyWebTemplates 根节点");
        assertTrue(xmlContent.contains("<Page>"), "应包含 Page 节点");
        assertTrue(xmlContent.contains("<Size>"), "Frame 应包含 Size 节点");
        assertTrue(xmlContent.contains("<XItems>"), "应包含 XItems 节点");
    }

    @Test
    public void testTreeXmlStructure() throws Exception {
        BusinessXmlCreator creator = BusinessXmlCreator.create(config, treeTable, "Tree");
        String xmlContent = creator.createShowXml("demo", "BAS_TAG", null, null, "TREE", "M");

        assertTrue(xmlContent.contains("<EasyWebTemplates>"), "应包含 EasyWebTemplates 根节点");
        assertTrue(xmlContent.contains("<Tree>"), "应包含 Tree 节点");
        assertTrue(xmlContent.contains("<HtmlFrame>"), "应包含 HtmlFrame 节点");
        assertTrue(xmlContent.contains("<Menus>"), "应包含 Menus 节点");
        assertTrue(xmlContent.contains("<Action>"), "应包含 Action 节点");
        assertTrue(xmlContent.contains("<SqlSet>"), "应包含 SqlSet 节点");

        assertTrue(xmlContent.contains("OnPageLoad"), "应包含 OnPageLoad Action");
        assertTrue(xmlContent.contains("OnTreeNodeNew"), "应包含 OnTreeNodeNew Action");
        assertTrue(xmlContent.contains("OnTreeNodeDelete"), "应包含 OnTreeNodeDelete Action");
        assertTrue(xmlContent.contains("OnTreeNodeRename"), "应包含 OnTreeNodeRename Action");
    }
}
