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
    @DisplayName("null 输入被三个校验方法拒绝")
    void testNullRejected() {
        assertFalse(ConfigValidator.validateItemXml(null, "any").isValid());
        assertFalse(ConfigValidator.validateFrameType(null).isValid());
        assertFalse(ConfigValidator.validateOperationType(null).isValid());
    }

    @Test
    @DisplayName("空字符串被三个校验方法拒绝")
    void testEmptyStringRejected() {
        assertFalse(ConfigValidator.validateItemXml("", "any").isValid());
        assertFalse(ConfigValidator.validateFrameType("").isValid());
        assertFalse(ConfigValidator.validateOperationType("").isValid());
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
