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
 * EWA 定义配置读取器
 * 读取 system.xml/EwaDefine.xml 配置文件
 */
public class EwaDefineConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(EwaDefineConfig.class);
    
    private Document defineDoc;
    private Map<String, FrameConfig> frameConfigs;
    
    public EwaDefineConfig() {
        loadDefineConfig();
    }
    
    /**
     * 加载 EwaDefine.xml 配置
     */
    private void loadDefineConfig() {
        try {
            InputStream is = getClass().getClassLoader()
                .getResourceAsStream("system.xml/EwaDefine.xml");
            if (is == null) {
                LOGGER.error("无法找到 system.xml/EwaDefine.xml");
                return;
            }
            // 读取 InputStream 为 String
            java.util.Scanner scanner = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A");
            String xmlContent = scanner.hasNext() ? scanner.next() : "";
            is.close();
            
            defineDoc = UXml.asDocument(xmlContent);
            frameConfigs = new HashMap<>();
            parseFrameConfigs();
            LOGGER.info("EwaDefine.xml 加载成功，共 {} 个 Frame 配置", frameConfigs.size());
        } catch (Exception e) {
            LOGGER.error("加载 EwaDefine.xml 失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 解析 Frame 配置
     */
    private void parseFrameConfigs() {
        NodeList frameNodes = defineDoc.getElementsByTagName("Frame");
        for (int i = 0; i < frameNodes.getLength(); i++) {
            Element frameElem = (Element) frameNodes.item(i);
            String frameName = frameElem.getAttribute("Name");
            
            FrameConfig config = new FrameConfig();
            config.setFrameName(frameName);
            
            // 解析 DescriptionSet
            NodeList descNodes = frameElem.getElementsByTagName("DescriptionSet");
            if (descNodes.getLength() > 0) {
                config.setDescriptionSet(parseDescriptionSet((Element) descNodes.item(0)));
            }
            
            // 解析 Tmp 模板
            NodeList tmpNodes = frameElem.getElementsByTagName("Tmp");
            for (int j = 0; j < tmpNodes.getLength(); j++) {
                Element tmpElem = (Element) tmpNodes.item(j);
                String tmpName = tmpElem.getAttribute("Name");
                TmpConfig tmpConfig = parseTmpConfig(tmpElem);
                config.addTmpConfig(tmpName, tmpConfig);
            }
            
            frameConfigs.put(frameName, config);
        }
    }
    
    /**
     * 解析 Tmp 配置
     */
    private TmpConfig parseTmpConfig(Element tmpElem) {
        TmpConfig tmpConfig = new TmpConfig();
        tmpConfig.setTmpName(tmpElem.getAttribute("Name"));
        tmpConfig.setReleateFrame(tmpElem.getAttribute("ReleateFrame"));
        tmpConfig.setReleateTmp(tmpElem.getAttribute("ReleateTmp"));
        
        // 解析 DescriptionSet
        NodeList descNodes = tmpElem.getElementsByTagName("DescriptionSet");
        if (descNodes.getLength() > 0) {
            tmpConfig.setDescriptionSet(parseDescriptionSet((Element) descNodes.item(0)));
        }
        
        // 解析 Buttons
        NodeList buttonNodes = tmpElem.getElementsByTagName("Button");
        for (int i = 0; i < buttonNodes.getLength(); i++) {
            ButtonConfig btnConfig = parseButtonConfig((Element) buttonNodes.item(i));
            tmpConfig.addButtonConfig(btnConfig);
        }
        
        // 解析 Actions
        NodeList actionNodes = tmpElem.getElementsByTagName("Action");
        for (int i = 0; i < actionNodes.getLength(); i++) {
            ActionConfig actConfig = parseActionConfig((Element) actionNodes.item(i));
            tmpConfig.addActionConfig(actConfig);
        }
        
        // 解析 Adds (AddScript, AddCss)
        NodeList addsNodes = tmpElem.getElementsByTagName("Adds");
        if (addsNodes.getLength() > 0) {
            parseAddsConfig((Element) addsNodes.item(0), tmpConfig);
        }
        
        // 解析 PageInfos
        NodeList pageInfoNodes = tmpElem.getElementsByTagName("PageInfos");
        if (pageInfoNodes.getLength() > 0) {
            parsePageInfosConfig((Element) pageInfoNodes.item(0), tmpConfig);
        }
        
        return tmpConfig;
    }
    
    /**
     * 解析 Button 配置
     */
    private ButtonConfig parseButtonConfig(Element btnElem) {
        ButtonConfig config = new ButtonConfig();
        config.setName(btnElem.getAttribute("Name"));
        config.setTag(btnElem.getAttribute("Tag"));
        
        // 解析 DescriptionSet
        NodeList descNodes = btnElem.getElementsByTagName("DescriptionSet");
        if (descNodes.getLength() > 0) {
            config.setDescriptionSet(parseDescriptionSet((Element) descNodes.item(0)));
        }
        
        // 解析 Para (用于配置 EventSet, CallAction 等)
        NodeList paraNodes = btnElem.getElementsByTagName("Para");
        for (int i = 0; i < paraNodes.getLength(); i++) {
            Element paraElem = (Element) paraNodes.item(i);
            ParaConfig paraConfig = new ParaConfig();
            paraConfig.setXmlPath(paraElem.getAttribute("XmlPath"));
            paraConfig.setName(paraElem.getAttribute("Name"));
            paraConfig.setVal(paraElem.getAttribute("Val"));
            config.addParaConfig(paraConfig);
        }
        
        return config;
    }
    
    /**
     * 解析 Action 配置
     */
    private ActionConfig parseActionConfig(Element actElem) {
        ActionConfig config = new ActionConfig();
        config.setName(actElem.getAttribute("Name"));
        config.setActionName(actElem.getAttribute("ActionName"));
        config.setSqlType(actElem.getAttribute("SqlType"));
        config.setTest(actElem.getAttribute("Test"));
        config.setSql(actElem.getAttribute("Sql"));
        
        // 解析 DescriptionSet
        NodeList descNodes = actElem.getElementsByTagName("DescriptionSet");
        if (descNodes.getLength() > 0) {
            config.setDescriptionSet(parseDescriptionSet((Element) descNodes.item(0)));
        }
        
        return config;
    }
    
    /**
     * 解析 Adds 配置 (AddScript, AddCss)
     */
    private void parseAddsConfig(Element addsElem, TmpConfig tmpConfig) {
        NodeList addNodes = addsElem.getElementsByTagName("Add");
        for (int i = 0; i < addNodes.getLength(); i++) {
            Element addElem = (Element) addNodes.item(i);
            AddConfig addConfig = new AddConfig();
            addConfig.setSetMethod(addElem.getAttribute("SetMethod"));
            addConfig.setXmlPath(addElem.getAttribute("XmlPath"));
            
            // 获取 CDATA 内容
            String content = getCDataContent(addElem);
            addConfig.setContent(content);
            
            tmpConfig.addAddConfig(addConfig);
        }
    }
    
    /**
     * 获取元素的 CDATA 内容
     */
    private String getCDataContent(Element elem) {
        org.w3c.dom.NodeList children = elem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() == org.w3c.dom.Node.CDATA_SECTION_NODE) {
                return child.getNodeValue();
            }
        }
        // 如果没有 CDATA，返回普通文本
        return elem.getTextContent();
    }
    
    /**
     * 解析 PageInfos 配置
     */
    private void parsePageInfosConfig(Element pageInfosElem, TmpConfig tmpConfig) {
        NodeList pageInfoNodes = pageInfosElem.getElementsByTagName("PageInfo");
        for (int i = 0; i < pageInfoNodes.getLength(); i++) {
            Element pageInfoElem = (Element) pageInfoNodes.item(i);
            PageInfoConfig pageInfoConfig = new PageInfoConfig();
            pageInfoConfig.setName(pageInfoElem.getAttribute("Name"));
            
            // 解析 DescriptionSet
            NodeList descNodes = pageInfoElem.getElementsByTagName("DescriptionSet");
            if (descNodes.getLength() > 0) {
                pageInfoConfig.setDescriptionSet(parseDescriptionSet((Element) descNodes.item(0)));
            }
            
            tmpConfig.addPageInfoConfig(pageInfoConfig);
        }
    }
    
    /**
     * 解析 DescriptionSet
     */
    private Map<String, String> parseDescriptionSet(Element descSetElem) {
        Map<String, String> descMap = new HashMap<>();
        NodeList setNodes = descSetElem.getElementsByTagName("Set");
        for (int i = 0; i < setNodes.getLength(); i++) {
            Element setElem = (Element) setNodes.item(i);
            String lang = setElem.getAttribute("Lang");
            String info = setElem.getAttribute("Info");
            descMap.put(lang, info);
        }
        return descMap;
    }
    
    /**
     * 获取 Frame 配置
     */
    public FrameConfig getFrameConfig(String frameName) {
        return frameConfigs.get(frameName);
    }
    
    /**
     * 获取 Tmp 配置
     */
    public TmpConfig getTmpConfig(String frameName, String tmpName) {
        FrameConfig frameConfig = getFrameConfig(frameName);
        if (frameConfig != null) {
            return frameConfig.getTmpConfig(tmpName);
        }
        return null;
    }
    
    /**
     * Frame 配置
     */
    public static class FrameConfig {
        private String frameName;
        private Map<String, String> descriptionSet;
        private Map<String, TmpConfig> tmpConfigs = new HashMap<>();
        
        public String getFrameName() { return frameName; }
        public void setFrameName(String frameName) { this.frameName = frameName; }
        public Map<String, String> getDescriptionSet() { return descriptionSet; }
        public void setDescriptionSet(Map<String, String> descriptionSet) { this.descriptionSet = descriptionSet; }
        public Map<String, TmpConfig> getTmpConfigs() { return tmpConfigs; }
        public void setTmpConfigs(Map<String, TmpConfig> tmpConfigs) { this.tmpConfigs = tmpConfigs; }
        public TmpConfig getTmpConfig(String tmpName) { return tmpConfigs.get(tmpName); }
        public void addTmpConfig(String tmpName, TmpConfig config) { tmpConfigs.put(tmpName, config); }
    }
    
    /**
     * Tmp 配置
     */
    public static class TmpConfig {
        private String tmpName;
        private String releateFrame;
        private String releateTmp;
        private Map<String, String> descriptionSet;
        private List<ButtonConfig> buttonConfigs = new ArrayList<>();
        private List<ActionConfig> actionConfigs = new ArrayList<>();
        private List<AddConfig> addConfigs = new ArrayList<>();
        private List<PageInfoConfig> pageInfoConfigs = new ArrayList<>();
        
        public String getTmpName() { return tmpName; }
        public void setTmpName(String tmpName) { this.tmpName = tmpName; }
        public String getReleateFrame() { return releateFrame; }
        public void setReleateFrame(String releateFrame) { this.releateFrame = releateFrame; }
        public String getReleateTmp() { return releateTmp; }
        public void setReleateTmp(String releateTmp) { this.releateTmp = releateTmp; }
        public Map<String, String> getDescriptionSet() { return descriptionSet; }
        public void setDescriptionSet(Map<String, String> descriptionSet) { this.descriptionSet = descriptionSet; }
        public List<ButtonConfig> getButtonConfigs() { return buttonConfigs; }
        public void setButtonConfigs(List<ButtonConfig> buttonConfigs) { this.buttonConfigs = buttonConfigs; }
        public void addButtonConfig(ButtonConfig config) { buttonConfigs.add(config); }
        public List<ActionConfig> getActionConfigs() { return actionConfigs; }
        public void setActionConfigs(List<ActionConfig> actionConfigs) { this.actionConfigs = actionConfigs; }
        public void addActionConfig(ActionConfig config) { actionConfigs.add(config); }
        public List<AddConfig> getAddConfigs() { return addConfigs; }
        public void setAddConfigs(List<AddConfig> addConfigs) { this.addConfigs = addConfigs; }
        public void addAddConfig(AddConfig config) { addConfigs.add(config); }
        public List<PageInfoConfig> getPageInfoConfigs() { return pageInfoConfigs; }
        public void setPageInfoConfigs(List<PageInfoConfig> pageInfoConfigs) { this.pageInfoConfigs = pageInfoConfigs; }
        public void addPageInfoConfig(PageInfoConfig config) { pageInfoConfigs.add(config); }
    }
    
    /**
     * Button 配置
     */
    public static class ButtonConfig {
        private String name;
        private String tag;
        private Map<String, String> descriptionSet;
        private List<ParaConfig> paraConfigs = new ArrayList<>();
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getTag() { return tag; }
        public void setTag(String tag) { this.tag = tag; }
        public Map<String, String> getDescriptionSet() { return descriptionSet; }
        public void setDescriptionSet(Map<String, String> descriptionSet) { this.descriptionSet = descriptionSet; }
        public List<ParaConfig> getParaConfigs() { return paraConfigs; }
        public void setParaConfigs(List<ParaConfig> paraConfigs) { this.paraConfigs = paraConfigs; }
        public void addParaConfig(ParaConfig config) { paraConfigs.add(config); }
    }
    
    /**
     * Para 配置 (用于配置 EventSet, CallAction 等)
     */
    public static class ParaConfig {
        private String xmlPath;
        private String name;
        private String val;
        
        public String getXmlPath() { return xmlPath; }
        public void setXmlPath(String xmlPath) { this.xmlPath = xmlPath; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getVal() { return val; }
        public void setVal(String val) { this.val = val; }
    }
    
    /**
     * Action 配置
     */
    public static class ActionConfig {
        private String name;
        private String actionName;
        private String sqlType;
        private String test;
        private String sql;
        private Map<String, String> descriptionSet;
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getActionName() { return actionName; }
        public void setActionName(String actionName) { this.actionName = actionName; }
        public String getSqlType() { return sqlType; }
        public void setSqlType(String sqlType) { this.sqlType = sqlType; }
        public String getTest() { return test; }
        public void setTest(String test) { this.test = test; }
        public String getSql() { return sql; }
        public void setSql(String sql) { this.sql = sql; }
        public Map<String, String> getDescriptionSet() { return descriptionSet; }
        public void setDescriptionSet(Map<String, String> descriptionSet) { this.descriptionSet = descriptionSet; }
    }
    
    /**
     * Add 配置 (AddScript, AddCss)
     */
    public static class AddConfig {
        private String setMethod;
        private String xmlPath;
        private String content;
        
        public String getSetMethod() { return setMethod; }
        public void setSetMethod(String setMethod) { this.setMethod = setMethod; }
        public String getXmlPath() { return xmlPath; }
        public void setXmlPath(String xmlPath) { this.xmlPath = xmlPath; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
    
    /**
     * PageInfo 配置
     */
    public static class PageInfoConfig {
        private String name;
        private Map<String, String> descriptionSet;
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Map<String, String> getDescriptionSet() { return descriptionSet; }
        public void setDescriptionSet(Map<String, String> descriptionSet) { this.descriptionSet = descriptionSet; }
    }
}
