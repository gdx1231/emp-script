package com.gdxsoft.easyweb.define.bussinessXmlCreator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UXml;

/**
 * EWA Define Settings 配置读取器
 * 读取 system.xml/EwaDefineSettings.xml 配置文件
 */
public class EwaDefineSettings {
    private static final Logger LOGGER = LoggerFactory.getLogger(EwaDefineSettings.class);
    
    private static EwaDefineSettings instance;
    
    private Document settingsDoc;
    private List<String> hideSuffixes;
    private List<String> hideTypes;
    private List<String> hideNames;
    private FramePageSettings framePageSettings;
    private ListFramePageSettings listFramePageSettings;
    
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
                LOGGER.warn("未找到 system.xml/EwaDefineSettings.xml，使用默认配置");
                loadDefaultSettings();
                return;
            }
            
            // 读取 InputStream 为 String
            java.util.Scanner scanner = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A");
            String xmlContent = scanner.hasNext() ? scanner.next() : "";
            is.close();
            
            settingsDoc = UXml.asDocument(xmlContent);
            
            // 加载隐藏字段规则
            loadHideFieldRules();
            
            // 加载页面配置
            loadPageSettings();
            
            LOGGER.info("EwaDefineSettings.xml 加载成功");
        } catch (Exception e) {
            LOGGER.error("加载 EwaDefineSettings.xml 失败：" + e.getMessage(), e);
            loadDefaultSettings();
        }
    }
    
    /**
     * 加载默认配置
     */
    private void loadDefaultSettings() {
        hideSuffixes = new ArrayList<>();
        hideSuffixes.add("_CDATE");
        hideSuffixes.add("_MDATE");
        hideSuffixes.add("_STATUS");
        hideSuffixes.add("_STATE");
        
        hideTypes = new ArrayList<>();
        hideTypes.add("IDENTITY");
        
        hideNames = new ArrayList<>();
        
        framePageSettings = new FramePageSettings();
        framePageSettings.setWidth("700");
        framePageSettings.setHiddenCaption("1");
        framePageSettings.setFrameCols("C2");
        framePageSettings.setHAlign("center");
        framePageSettings.setVAlign("top");
        
        listFramePageSettings = new ListFramePageSettings();
        listFramePageSettings.setWidth("100%");
        listFramePageSettings.setHiddenCaption("");
        listFramePageSettings.setFrameCols("");
        listFramePageSettings.setHAlign("center");
        listFramePageSettings.setVAlign("top");
    }
    
    /**
     * 加载隐藏字段规则
     */
    private void loadHideFieldRules() {
        hideSuffixes = new ArrayList<>();
        hideTypes = new ArrayList<>();
        hideNames = new ArrayList<>();
        
        NodeList suffixNodes = settingsDoc.getElementsByTagName("Suffix");
        for (int i = 0; i < suffixNodes.getLength(); i++) {
            Element suffixElem = (Element) suffixNodes.item(i);
            String name = suffixElem.getAttribute("Name");
            if (name != null && !name.isEmpty()) {
                hideSuffixes.add(name.toUpperCase());
            }
        }
        
        NodeList typeNodes = settingsDoc.getElementsByTagName("Type");
        for (int i = 0; i < typeNodes.getLength(); i++) {
            Element typeElem = (Element) typeNodes.item(i);
            String name = typeElem.getAttribute("Name");
            if (name != null && !name.isEmpty()) {
                hideTypes.add(name.toUpperCase());
            }
        }
        
        NodeList nameNodes = settingsDoc.getElementsByTagName("HideByName");
        for (int i = 0; i < nameNodes.getLength(); i++) {
            Element nameElem = (Element) nameNodes.item(i);
            String name = nameElem.getAttribute("Name");
            if (name != null && !name.isEmpty()) {
                hideNames.add(name.toUpperCase());
            }
        }
    }
    
    /**
     * 加载页面配置
     */
    private void loadPageSettings() {
        // 加载 Frame 页面配置
        NodeList frameSizeNodes = settingsDoc.getElementsByTagName("FramePageSettings");
        if (frameSizeNodes.getLength() > 0) {
            framePageSettings = new FramePageSettings();
            Element frameSizeElem = (Element) frameSizeNodes.item(0);
            NodeList sizeNodes = frameSizeElem.getElementsByTagName("Size");
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
        
        // 加载 ListFrame 页面配置
        NodeList listFrameSizeNodes = settingsDoc.getElementsByTagName("ListFramePageSettings");
        if (listFrameSizeNodes.getLength() > 0) {
            listFramePageSettings = new ListFramePageSettings();
            Element listFrameSizeElem = (Element) listFrameSizeNodes.item(0);
            
            NodeList sizeNodes = listFrameSizeElem.getElementsByTagName("Size");
            if (sizeNodes.getLength() > 0) {
                Element sizeElem = (Element) sizeNodes.item(0);
                listFramePageSettings.setWidth(sizeElem.getAttribute("Width"));
                listFramePageSettings.setHiddenCaption(sizeElem.getAttribute("HiddenCaption"));
                listFramePageSettings.setFrameCols(sizeElem.getAttribute("FrameCols"));
                listFramePageSettings.setHAlign(sizeElem.getAttribute("HAlign"));
                listFramePageSettings.setVAlign(sizeElem.getAttribute("VAlign"));
            }
            
            NodeList pageSizeNodes = listFrameSizeElem.getElementsByTagName("PageSize");
            if (pageSizeNodes.getLength() > 0) {
                Element pageSizeElem = (Element) pageSizeNodes.item(0);
                listFramePageSettings.setPageSizeIsSplit(pageSizeElem.getAttribute("IsSplitPage"));
                listFramePageSettings.setPageSize(pageSizeElem.getAttribute("PageSize"));
                listFramePageSettings.setRecycle(pageSizeElem.getAttribute("Recycle"));
            }
            
            NodeList listUINodes = listFrameSizeElem.getElementsByTagName("ListUI");
            if (listUINodes.getLength() > 0) {
                Element listUIElem = (Element) listUINodes.item(0);
                listFramePageSettings.setLuButtons(listUIElem.getAttribute("luButtons"));
                listFramePageSettings.setLuSearch(listUIElem.getAttribute("luSearch"));
                listFramePageSettings.setLuSelect(listUIElem.getAttribute("luSelect"));
            }
        } else {
            listFramePageSettings = new ListFramePageSettings();
        }
    }
    
    /**
     * 判断字段是否应该隐藏
     */
    public boolean shouldHideField(String fieldName, String fieldType) {
        String fieldNameUpper = fieldName.toUpperCase();
        String fieldTypeUpper = fieldType.toUpperCase();
        
        // 检查后缀
        for (String suffix : hideSuffixes) {
            if (fieldNameUpper.endsWith(suffix)) {
                return true;
            }
        }
        
        // 检查类型
        for (String type : hideTypes) {
            if (fieldTypeUpper.contains(type)) {
                return true;
            }
        }
        
        // 检查精确匹配
        for (String name : hideNames) {
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
