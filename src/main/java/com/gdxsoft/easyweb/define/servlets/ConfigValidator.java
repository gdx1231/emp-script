package com.gdxsoft.easyweb.define.servlets;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

/**
 * 配置提交合法性校验：Tag 值、XML 大结构 参考 EwaConfig.xml 及 define.xml/ewa/*.xml 中的实际格式。
 */
public class ConfigValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigValidator.class);

    /** 合法的 FrameTag 值（来源：EwaConfig.xml → /EasyWebConfig/Frames/Frame@Name） */
    private static final Set<String> VALID_FRAME_TAGS;
    static {
        Set<String> tags = new HashSet<>();
        for (String t : new String[] { "ListFrame", "Frame", "Tree", "Menu", "Grid",
                "MultiGrid", "Logic", "Report", "Combine", "Complex" }) {
            tags.add(t.toUpperCase());
        }
        VALID_FRAME_TAGS = Collections.unmodifiableSet(tags);
    }

    /** 合法的 XItem 指令 Tag 值（EwaConfig.xml → /EasyWebConfig/Items/XItems/XItem@Name），延迟加载 */
    private static volatile Set<String> VALID_XITEM_TAGS;

    /** 合法的操作类型 */
    private static final Set<String> VALID_OPERATION_TYPES = new HashSet<>(Arrays.asList("N", "M", "V", "NM"));

    /** 配置项 XML 的合法根元素名 */
    private static final String ITEM_ROOT_ELEMENT = "EasyWebTemplate";

    // ==================== 公共入口 ====================

    /**
     * 校验 updateConfItem / runConfItem / createBusinessXml 提交的配置项 XML。
     *
     * @param xml      待提交的 XML 片段（一个 EasyWebTemplate）
     * @param itemName 配置项名称（用于日志和校验匹配）
     * @return 校验结果；通过返回 ValidationResult.valid()，否则携带错误信息
     */
    public static ValidationResult validateItemXml(String xml, String itemName) {
        if (StringUtils.isBlank(xml)) {
            return ValidationResult.invalid("XML 内容为空");
        }

        // 1. 基础 XML 格式校验
        Document doc;
        try {
            doc = UXml.asDocument(xml);
        } catch (Exception e) {
            String msg = extractParseError(e);
            return ValidationResult.invalid("XML 解析失败：" + msg);
        }

        // 2. 根元素校验
        if (doc == null) {
            return ValidationResult.invalid("XML 解析失败：无法生成 Document");
        }
        Element root = doc.getDocumentElement();
        if (root == null) {
            return ValidationResult.invalid("XML 缺少根元素，期望 <" + ITEM_ROOT_ELEMENT + ">");
        }
        if (!ITEM_ROOT_ELEMENT.equals(root.getNodeName())) {
            return ValidationResult.invalid("XML 根元素应为 <" + ITEM_ROOT_ELEMENT
                    + ">，实际为 <" + root.getNodeName() + ">");
        }

        // 3. Name 属性校验
        String rootName = root.getAttribute("Name");
        if (StringUtils.isBlank(rootName)) {
            return ValidationResult.invalid("<" + ITEM_ROOT_ELEMENT + "> 缺少 Name 属性");
        }
        if (itemName != null && !itemName.equals(rootName)) {
            return ValidationResult.invalid("<" + ITEM_ROOT_ELEMENT + "> Name 属性 \"" + rootName
                    + "\" 与请求的 itemname \"" + itemName + "\" 不匹配");
        }

        // 4. Page 节点校验（doc 根即 EasyWebTemplate）
        Node pageNode = UXml.retNode(doc, "Page");
        if (pageNode == null) {
            return ValidationResult.invalid("缺少 <Page> 节点（doc 根为 EasyWebTemplate，期望子元素 Page）");
        }

        // 5. FrameTag 校验
        ValidationResult frameTagResult = validateFrameTag(doc);
        if (!frameTagResult.isValid()) {
            return frameTagResult;
        }

        // 6. XItems Tag 值校验
        ValidationResult xitemsResult = validateXItemTags(doc);
        if (!xitemsResult.isValid()) {
            return xitemsResult;
        }

        // 7. Page/Name 一致性（非阻断：UpdateXmlBase.fixXml 会自动修正）
        Node pageNameSet = UXml.retNode(doc, "Page/Name/Set");
        if (pageNameSet != null) {
            Element nameSet = (Element) pageNameSet;
            String pageItemName = nameSet.getAttribute("Name");
            if (StringUtils.isNotBlank(pageItemName) && itemName != null && !itemName.equals(pageItemName)) {
                LOGGER.debug("Page/Name/Set Name=\"{}\" 与根 Name=\"{}\" 不一致（提交时 fixXml 自动修正）",
                        pageItemName, rootName);
            }
        }

        return ValidationResult.valid();
    }

    /**
     * 校验 FrameType 参数（用于 createBusinessXml / previewBusinessXml）
     */
    public static ValidationResult validateFrameType(String frameType) {
        if (StringUtils.isBlank(frameType)) {
            return ValidationResult.invalid("frametype 不能为空");
        }
        String upper = frameType.toUpperCase();
        if (!VALID_FRAME_TAGS.contains(upper)) {
            return ValidationResult.invalid("无效的 frametype: " + frameType
                    + "，允许值：" + VALID_FRAME_TAGS);
        }
        return ValidationResult.valid();
    }

    /**
     * 校验 operationType 参数
     */
    public static ValidationResult validateOperationType(String operationType) {
        if (StringUtils.isBlank(operationType)) {
            return ValidationResult.invalid("operationtype 不能为空");
        }
        String upper = operationType.toUpperCase();
        if (!VALID_OPERATION_TYPES.contains(upper)) {
            return ValidationResult.invalid("无效的 operationtype: " + operationType
                    + "，允许值：" + VALID_OPERATION_TYPES);
        }
        return ValidationResult.valid();
    }

    // ==================== 私有方法 ====================

    /**
     * 校验 FrameTag 节点
     */
    private static ValidationResult validateFrameTag(Document doc) {
        Node frameTagNode = UXml.retNode(doc, "Page/FrameTag");
        if (frameTagNode == null) {
            return ValidationResult.invalid("缺少 <FrameTag> 节点（路径：Page/FrameTag）");
        }
        Node setNode = UXml.retNode(doc, "Page/FrameTag/Set");
        if (setNode == null) {
            return ValidationResult.invalid("<FrameTag> 内缺少 <Set FrameTag=\"...\"/> 子节点");
        }
        Element setElement = (Element) setNode;
        String frameTagValue = setElement.getAttribute("FrameTag");
        if (StringUtils.isBlank(frameTagValue)) {
            return ValidationResult.invalid("<FrameTag>/<Set> 缺少 FrameTag 属性");
        }
        if (!VALID_FRAME_TAGS.contains(frameTagValue.toUpperCase())) {
            return ValidationResult.invalid("无效的 FrameTag 值: \"" + frameTagValue
                    + "\"，允许值：" + VALID_FRAME_TAGS);
        }
        return ValidationResult.valid();
    }

    /**
     * 校验所有 XItem 的 Tag 值是否在 EwaConfig.xml 定义的合法列表中
     */
    private static ValidationResult validateXItemTags(Document doc) {
        NodeList xitems = doc.getElementsByTagName("XItem");
        if (xitems.getLength() == 0) {
            // 没有 XItem 元素是合法的（如纯 SQL 配置项）
            return ValidationResult.valid();
        }

        Set<String> validTags = getValidXItemTags();

        for (int i = 0; i < xitems.getLength(); i++) {
            Element xitem = (Element) xitems.item(i);

            // 尝试从 <Tag>/<Set Tag="..."> 获取
            NodeList tagNodes = xitem.getElementsByTagName("Tag");
            String tagValue = null;
            if (tagNodes.getLength() > 0) {
                Element tagElement = (Element) tagNodes.item(0);
                NodeList setNodes = tagElement.getElementsByTagName("Set");
                if (setNodes.getLength() > 0) {
                    tagValue = ((Element) setNodes.item(0)).getAttribute("Tag");
                }
            }
            // 或者直接 Tag 属性（某些生成方式）
            if (StringUtils.isBlank(tagValue)) {
                tagValue = xitem.getAttribute("Tag");
            }
            if (StringUtils.isBlank(tagValue)) {
                String itemName = xitem.getAttribute("Name");
                return ValidationResult.invalid("XItem" + (itemName != null ? " [Name=" + itemName + "]" : "")
                        + " 缺少 Tag 定义（需 <Tag><Set Tag=\"...\"/></Tag> 或 Tag 属性）");
            }

            if (!validTags.contains(tagValue)) {
                String itemName = xitem.getAttribute("Name");
                return ValidationResult.invalid("XItem" + (itemName != null ? " [Name=" + itemName + "]" : "")
                        + " 的 Tag 值 \"" + tagValue + "\" 不合法，允许值：" + validTags);
            }
        }

        return ValidationResult.valid();
    }

    /**
     * 从 EwaConfig.xml 加载合法 XItem Tag 列表（缓存）
     */
    private static Set<String> getValidXItemTags() {
        if (VALID_XITEM_TAGS != null) {
            return VALID_XITEM_TAGS;
        }
        synchronized (ConfigValidator.class) {
            if (VALID_XITEM_TAGS != null) {
                return VALID_XITEM_TAGS;
            }
            Set<String> tags = new HashSet<>();
            try {
                String pathConfigDoc = UPath.getConfigPath() + "/EwaConfig.xml";
                Document docConfig = UXml.retDocument(pathConfigDoc);
                NodeList nl = docConfig.getElementsByTagName("XItem");
                for (int i = 0; i < nl.getLength(); i++) {
                    Element ele = (Element) nl.item(i);
                    String name = ele.getAttribute("Name");
                    if (StringUtils.isNotBlank(name)) {
                        tags.add(name.trim());
                    }
                }
                LOGGER.info("Loaded {} valid XItem tags from EwaConfig.xml", tags.size());
            } catch (Exception e) {
                LOGGER.warn("Failed to load EwaConfig.xml for tag validation, fallback to static list", e);
                // 降级：使用常见 Tag 值（基本类型）
                tags.addAll(Arrays.asList(
                        "text", "textarea", "span", "hidden", "password", "passwordWithEye",
                        "combo", "select", "checkbox", "switch", "radio", "checkboxgrid", "radiogrid",
                        "anchor", "anchor2", "linkButton", "droplist", "submit", "button", "butFlow",
                        "date", "datetime", "time", "dHtml5", "markDown", "h5upload", "h5TakePhoto",
                        "valid", "smsValid", "signature", "user", "userControl", "dataType",
                        "addressMap", "gridImage", "gridBgImage", "popselect", "ewaconfigitem",
                        "MGAddField", "LogicItem", "ReportItem", "CombineItem", "ComplexItem",
                        "SqlEditor", "JsEditor", "CssEditor", "XMLEditor", "QRCode", "idempotence",
                        // 已废弃但仍存在于历史配置中的 Tag
                        "file", "dHtml", "dHtmlNoImages", "image",
                        "swffile", "SwfDoc", "SwfTakePhoto"
                ));
            }
            VALID_XITEM_TAGS = tags;
            return VALID_XITEM_TAGS;
        }
    }

    /**
     * 提取 XML 解析错误中的可读信息
     */
    private static String extractParseError(Exception e) {
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof SAXParseException) {
                SAXParseException spe = (SAXParseException) cause;
                return "行 " + spe.getLineNumber() + " 列 " + spe.getColumnNumber() + ": " + spe.getMessage();
            }
            cause = cause.getCause();
        }
        // 尝试提取常见错误特征
        String msg = e.getMessage();
        if (msg != null) {
            // 截短以免过长
            if (msg.length() > 200) {
                msg = msg.substring(0, 200) + "...";
            }
        }
        return msg != null ? msg : "未知解析错误";
    }

    // ==================== 结果对象 ====================

    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        @Override
        public String toString() {
            return valid ? "VALID" : "INVALID: " + errorMessage;
        }
    }
}
