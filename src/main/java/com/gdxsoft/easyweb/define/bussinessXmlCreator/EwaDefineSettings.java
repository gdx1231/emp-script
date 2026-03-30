package com.gdxsoft.easyweb.define.bussinessXmlCreator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UXml;

/**
 * EWA Define Settings 配置读取器
 * 读取 system.xml/EwaDefineSettings.xml 配置文件
 * 
 * 配置结构:
 * EwaDefineSettings
 *   ├─ Frame
 *   │   ├─ FieldHideRules
 *   │   │   ├─ HideBySuffix
 *   │   │   ├─ HideByType
 *   │   │   └─ HideByName
 *   │   └─ PageSettings
 *   └─ ListFrame
 *       ├─ PageSettings
 *       └─ OrderSearchRules
 */
public class EwaDefineSettings {
    private static final Logger LOGGER = LoggerFactory.getLogger(EwaDefineSettings.class);
    
    private static EwaDefineSettings instance;
    
    private Document settingsDoc;
    private List<String> frameHideSuffixes;
    private List<String> frameHideTypes;
    private List<String> frameHideNames;
    private FramePageSettings framePageSettings;
    private ListFramePageSettings listFramePageSettings;
    private java.util.Map<String, String> fieldFormats;
    private java.util.Map<String, String> numberScales;
    
    private EwaDefineSettings() {
        loadSettings();
    }
    
    /**
     * 获取单例实例
     */
    public static synchronized EwaDefineSettings getInstance() {
        if (instance == null) {
            instance = new EwaDefineSettings();
        }
        return instance;
    }
    
    /**
     * 加载配置文件
     */
    private void loadSettings() {
        try {
            InputStream is = getClass().getClassLoader()
                .getResourceAsStream("system.xml/EwaDefineSettings.xml");
            if (is == null) {
                LOGGER.error("未找到 system.xml/EwaDefineSettings.xml，配置加载失败");
                throw new RuntimeException("未找到 system.xml/EwaDefineSettings.xml 配置文件");
            }

            // 读取 InputStream 为 String
            java.util.Scanner scanner = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A");
            String xmlContent = scanner.hasNext() ? scanner.next() : "";
            is.close();

            if (xmlContent.isEmpty()) {
                LOGGER.error("system.xml/EwaDefineSettings.xml 内容为空");
                throw new RuntimeException("system.xml/EwaDefineSettings.xml 配置文件为空");
            }

            settingsDoc = UXml.asDocument(xmlContent);

            // 加载 Frame 配置
            loadFrameSettings();

            // 加载 ListFrame 配置
            loadListFrameSettings();

            // 加载字段格式配置
            loadFieldFormats();

            LOGGER.info("EwaDefineSettings.xml 加载成功");
        } catch (Exception e) {
            LOGGER.error("加载 EwaDefineSettings.xml 失败：" + e.getMessage(), e);
            throw new RuntimeException("加载 EwaDefineSettings.xml 配置文件失败", e);
        }
    }
    
    /**
     * 加载 Frame 配置
     */
    private void loadFrameSettings() {
        frameHideSuffixes = new ArrayList<>();
        frameHideTypes = new ArrayList<>();
        frameHideNames = new ArrayList<>();

        // 查找 Frame/FieldHideRules 节点
        NodeList frameNodes = settingsDoc.getElementsByTagName("Frame");
        if (frameNodes.getLength() == 0) {
            LOGGER.error("未找到 Frame 配置");
            throw new RuntimeException("EwaDefineSettings.xml 中未找到 Frame 配置节点");
        }

        Element frameElem = (Element) frameNodes.item(0);

        // 加载字段隐藏规则
        NodeList hideRulesNodes = frameElem.getElementsByTagName("FieldHideRules");
        if (hideRulesNodes.getLength() > 0) {
            Element hideRulesElem = (Element) hideRulesNodes.item(0);
            
            // 加载后缀规则
            NodeList suffixNodes = hideRulesElem.getElementsByTagName("Suffix");
            for (int i = 0; i < suffixNodes.getLength(); i++) {
                Element suffixElem = (Element) suffixNodes.item(i);
                String name = suffixElem.getAttribute("Name");
                if (name != null && !name.isEmpty()) {
                    frameHideSuffixes.add(name.toUpperCase());
                }
            }
            
            // 加载类型规则
            NodeList typeNodes = hideRulesElem.getElementsByTagName("Type");
            for (int i = 0; i < typeNodes.getLength(); i++) {
                Element typeElem = (Element) typeNodes.item(i);
                String name = typeElem.getAttribute("Name");
                if (name != null && !name.isEmpty()) {
                    frameHideTypes.add(name.toUpperCase());
                }
            }
            
            // 加载名称规则
            NodeList nameNodes = hideRulesElem.getElementsByTagName("HideByName");
            for (int i = 0; i < nameNodes.getLength(); i++) {
                Element nameElem = (Element) nameNodes.item(i);
                String name = nameElem.getAttribute("Name");
                if (name != null && !name.isEmpty()) {
                    frameHideNames.add(name.toUpperCase());
                }
            }
        }
        
        // 加载页面配置
        NodeList pageSettingsNodes = frameElem.getElementsByTagName("PageSettings");
        if (pageSettingsNodes.getLength() > 0) {
            framePageSettings = new FramePageSettings();
            Element pageSettingsElem = (Element) pageSettingsNodes.item(0);
            NodeList sizeNodes = pageSettingsElem.getElementsByTagName("Size");
            if (sizeNodes.getLength() > 0) {
                Element sizeElem = (Element) sizeNodes.item(0);
                framePageSettings.setWidth(sizeElem.getAttribute("Width"));
                framePageSettings.setHiddenCaption(sizeElem.getAttribute("HiddenCaption"));
                framePageSettings.setFrameCols(sizeElem.getAttribute("FrameCols"));
                framePageSettings.setHAlign(sizeElem.getAttribute("HAlign"));
                framePageSettings.setVAlign(sizeElem.getAttribute("VAlign"));
            }
        } else {
            framePageSettings = new FramePageSettings();
        }
    }

    /**
     * 加载 ListFrame 配置
     */
    private void loadListFrameSettings() {
        // 查找 ListFrame 节点
        NodeList listFrameNodes = settingsDoc.getElementsByTagName("ListFrame");
        if (listFrameNodes.getLength() == 0) {
            LOGGER.error("未找到 ListFrame 配置");
            throw new RuntimeException("EwaDefineSettings.xml 中未找到 ListFrame 配置节点");
        }

        Element listFrameElem = (Element) listFrameNodes.item(0);
        listFramePageSettings = new ListFramePageSettings();

        // 加载页面配置
        NodeList pageSettingsNodes = listFrameElem.getElementsByTagName("PageSettings");
        if (pageSettingsNodes.getLength() > 0) {
            Element pageSettingsElem = (Element) pageSettingsNodes.item(0);
            
            // 加载 Size 配置
            NodeList sizeNodes = pageSettingsElem.getElementsByTagName("Size");
            if (sizeNodes.getLength() > 0) {
                Element sizeElem = (Element) sizeNodes.item(0);
                listFramePageSettings.setWidth(sizeElem.getAttribute("Width"));
                listFramePageSettings.setHiddenCaption(sizeElem.getAttribute("HiddenCaption"));
                listFramePageSettings.setFrameCols(sizeElem.getAttribute("FrameCols"));
                listFramePageSettings.setHAlign(sizeElem.getAttribute("HAlign"));
                listFramePageSettings.setVAlign(sizeElem.getAttribute("VAlign"));
            }
            
            // 加载 PageSize 配置
            NodeList pageSizeNodes = pageSettingsElem.getElementsByTagName("PageSize");
            if (pageSizeNodes.getLength() > 0) {
                Element pageSizeElem = (Element) pageSizeNodes.item(0);
                listFramePageSettings.setPageSizeIsSplit(pageSizeElem.getAttribute("IsSplitPage"));
                listFramePageSettings.setPageSize(pageSizeElem.getAttribute("PageSize"));
                listFramePageSettings.setRecycle(pageSizeElem.getAttribute("Recycle"));
            }
            
            // 加载 ListUI 配置
            NodeList listUINodes = pageSettingsElem.getElementsByTagName("ListUI");
            if (listUINodes.getLength() > 0) {
                Element listUIElem = (Element) listUINodes.item(0);
                listFramePageSettings.setLuButtons(listUIElem.getAttribute("luButtons"));
                listFramePageSettings.setLuSearch(listUIElem.getAttribute("luSearch"));
                listFramePageSettings.setLuSelect(listUIElem.getAttribute("luSelect"));
            }
        }
    }

    /**
     * 加载字段格式配置
     */
    private void loadFieldFormats() {
        fieldFormats = new java.util.HashMap<>();
        numberScales = new java.util.HashMap<>();

        // 加载 FieldFormats 配置
        NodeList fieldFormatsNodes = settingsDoc.getElementsByTagName("FieldFormats");
        if (fieldFormatsNodes.getLength() > 0) {
            Element fieldFormatsElem = (Element) fieldFormatsNodes.item(0);

            // 加载 Format 配置
            NodeList formatNodes = fieldFormatsElem.getElementsByTagName("Format");
            for (int i = 0; i < formatNodes.getLength(); i++) {
                Element formatElem = (Element) formatNodes.item(i);
                String typeAttr = formatElem.getAttribute("Type");
                String value = formatElem.getAttribute("Value");

                if (typeAttr != null && !typeAttr.isEmpty() && value != null && !value.isEmpty()) {
                    // 类型可能包含多个，用逗号分隔
                    String[] types = typeAttr.split(",");
                    for (String type : types) {
                        fieldFormats.put(type.trim().toUpperCase(), value);
                    }
                }
            }

            // 加载 NumberScale 配置
            NodeList numberScaleNodes = fieldFormatsElem.getElementsByTagName("NumberScale");
            for (int i = 0; i < numberScaleNodes.getLength(); i++) {
                Element numberScaleElem = (Element) numberScaleNodes.item(i);
                String typeAttr = numberScaleElem.getAttribute("Type");
                String value = numberScaleElem.getAttribute("Value");

                if (typeAttr != null && !typeAttr.isEmpty() && value != null && !value.isEmpty()) {
                    String[] types = typeAttr.split(",");
                    for (String type : types) {
                        numberScales.put(type.trim().toUpperCase(), value);
                    }
                }
            }
        }
    }
    
    /**
     * 获取字段格式
     */
    public String getFormat(String fieldType) {
        if (fieldType == null || fieldFormats == null) return "";
        String typeUpper = fieldType.toUpperCase();

        // 日期时间类型
        if (typeUpper.contains("DATETIME") || typeUpper.contains("TIMESTAMP")) {
            return fieldFormats.getOrDefault("DATETIME", "");
        }
        if (typeUpper.contains("DATE") && !typeUpper.contains("TIME")) {
            return fieldFormats.getOrDefault("DATE", "");
        }
        if (typeUpper.contains("TIME") && !typeUpper.contains("DATE")) {
            return fieldFormats.getOrDefault("TIME", "");
        }

        // 货币类型
        if (typeUpper.contains("MONEY") || typeUpper.contains("DECIMAL") || typeUpper.contains("NUMERIC")) {
            return fieldFormats.getOrDefault("MONEY", "");
        }

        // 整数类型
        if (typeUpper.contains("INT") || typeUpper.contains("BIGINT") || 
            typeUpper.contains("SMALLINT") || typeUpper.contains("TINYINT")) {
            return fieldFormats.getOrDefault("INT", "");
        }

        return "";
    }

    /**
     * 获取数字精度
     */
    public String getNumberScale(String fieldType) {
        if (fieldType == null || numberScales == null) return "";
        String typeUpper = fieldType.toUpperCase();

        // 检查配置
        for (String type : numberScales.keySet()) {
            if (typeUpper.contains(type)) {
                return numberScales.get(type);
            }
        }

        return "";
    }
    
    /**
     * 判断字段是否应该隐藏（Frame 模式）
     */
    public boolean shouldHideField(String fieldName, String fieldType) {
        String fieldNameUpper = fieldName.toUpperCase();
        String fieldTypeUpper = fieldType.toUpperCase();
        
        // 检查后缀
        for (String suffix : frameHideSuffixes) {
            if (fieldNameUpper.endsWith(suffix)) {
                return true;
            }
        }
        
        // 检查类型
        for (String type : frameHideTypes) {
            if (fieldTypeUpper.contains(type)) {
                return true;
            }
        }
        
        // 检查精确匹配
        for (String name : frameHideNames) {
            if (fieldNameUpper.equals(name)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 获取 Frame 页面配置
     */
    public FramePageSettings getFramePageSettings() {
        return framePageSettings;
    }
    
    /**
     * 获取 ListFrame 页面配置
     */
    public ListFramePageSettings getListFramePageSettings() {
        return listFramePageSettings;
    }
    
    /**
     * Frame 页面配置
     */
    public static class FramePageSettings {
        private String width = "700";
        private String hiddenCaption = "1";
        private String frameCols = "C2";
        private String hAlign = "center";
        private String vAlign = "top";
        
        public String getWidth() { return width; }
        public void setWidth(String width) { this.width = width; }
        public String getHiddenCaption() { return hiddenCaption; }
        public void setHiddenCaption(String hiddenCaption) { this.hiddenCaption = hiddenCaption; }
        public String getFrameCols() { return frameCols; }
        public void setFrameCols(String frameCols) { this.frameCols = frameCols; }
        public String getHAlign() { return hAlign; }
        public void setHAlign(String hAlign) { this.hAlign = hAlign; }
        public String getVAlign() { return vAlign; }
        public void setVAlign(String vAlign) { this.vAlign = vAlign; }
    }
    
    /**
     * ListFrame 页面配置
     */
    public static class ListFramePageSettings {
        private String width = "100%";
        private String hiddenCaption = "";
        private String frameCols = "";
        private String hAlign = "center";
        private String vAlign = "top";
        private String pageSizeIsSplit = "1";
        private String pageSize = "10";
        private String recycle = "1";
        private String luButtons = "1";
        private String luSearch = "1";
        private String luSelect = "S";
        
        public String getWidth() { return width; }
        public void setWidth(String width) { this.width = width; }
        public String getHiddenCaption() { return hiddenCaption; }
        public void setHiddenCaption(String hiddenCaption) { this.hiddenCaption = hiddenCaption; }
        public String getFrameCols() { return frameCols; }
        public void setFrameCols(String frameCols) { this.frameCols = frameCols; }
        public String getHAlign() { return hAlign; }
        public void setHAlign(String hAlign) { this.hAlign = hAlign; }
        public String getVAlign() { return vAlign; }
        public void setVAlign(String vAlign) { this.vAlign = vAlign; }
        public String getPageSizeIsSplit() { return pageSizeIsSplit; }
        public void setPageSizeIsSplit(String pageSizeIsSplit) { this.pageSizeIsSplit = pageSizeIsSplit; }
        public String getPageSize() { return pageSize; }
        public void setPageSize(String pageSize) { this.pageSize = pageSize; }
        public String getRecycle() { return recycle; }
        public void setRecycle(String recycle) { this.recycle = recycle; }
        public String getLuButtons() { return luButtons; }
        public void setLuButtons(String luButtons) { this.luButtons = luButtons; }
        public String getLuSearch() { return luSearch; }
        public void setLuSearch(String luSearch) { this.luSearch = luSearch; }
        public String getLuSelect() { return luSelect; }
        public void setLuSelect(String luSelect) { this.luSelect = luSelect; }
    }
}
