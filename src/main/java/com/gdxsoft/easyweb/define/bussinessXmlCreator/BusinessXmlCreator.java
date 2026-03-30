package com.gdxsoft.easyweb.define.bussinessXmlCreator;

import java.util.ArrayList;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.gdxsoft.easyweb.script.template.EwaConfig;
import com.gdxsoft.easyweb.define.database.Table;
import com.gdxsoft.easyweb.utils.UXml;

/**
 * 业务 XML 创建器
 */
public class BusinessXmlCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessXmlCreator.class);

    private EwaConfig config;
    private Table table;
    private Document xmlDoc;
    private EwaDefineConfig defineConfig;

    // 构造方法
    public BusinessXmlCreator(EwaConfig config, Table table) {
        this.config = config;
        this.table = table;
        this.defineConfig = new EwaDefineConfig();
    }

    /**
     * 生成并保存业务 XML
     */
    public boolean createAndSave(String db, String tableName, String selectSql,
            JSONObject tableJson, String frameType, String operationType,
            String xmlName, String itemName, String admId) {
        try {
            // 1. 创建参数
            BusinessXmlCreateParams params;
            if (tableJson != null) {
                params = new BusinessXmlCreateParams(db, tableJson, frameType, operationType);
            } else if (selectSql != null) {
                params = new BusinessXmlCreateParams(db, selectSql, frameType, operationType, true);
            } else {
                params = new BusinessXmlCreateParams(db, tableName, frameType, operationType);
            }

            // 2. 验证参数
            if (!params.validate()) {
                LOGGER.error("参数验证失败");
                return false;
            }

            // 3. 生成 XML
            this.xmlDoc = this.create(frameType, operationType);

            // 4. 保存
            return this.save(xmlName, itemName, admId);
        } catch (Exception e) {
            LOGGER.error("生成并保存失败：" + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 生成业务 XML 并返回 XML 字符串（用于预览）
     */
    public String createShowXml(String db, String tableName, String selectSql,
            JSONObject tableJson, String frameType, String operationType) {
        try {
            // 1. 创建参数
            BusinessXmlCreateParams params;
            if (tableJson != null) {
                params = new BusinessXmlCreateParams(db, tableJson, frameType, operationType);
            } else if (selectSql != null) {
                params = new BusinessXmlCreateParams(db, selectSql, frameType, operationType, true);
            } else {
                params = new BusinessXmlCreateParams(db, tableName, frameType, operationType);
            }

            // 2. 验证参数
            if (!params.validate()) {
                LOGGER.error("参数验证失败");
                return null;
            }

            // 3. 验证 SELECT 语句语法（如果提供了）
            if (selectSql != null) {
                SqlValidator.ValidationResult vr = SqlValidator.validateSelectSql(params.getDb(),
                        params.getSelectSql());
                if (!vr.isSuccess()) {
                    LOGGER.error("SQL 验证失败：" + vr.getError());
                    return null;
                }
            }

            // 4. 生成 XML
            this.xmlDoc = this.create(frameType, operationType);

            // 5. 返回格式化的 XML 字符串
            return UXml.asXmlPretty(this.xmlDoc);
        } catch (Exception e) {
            LOGGER.error("生成 XML 失败：" + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 创建 XML 文档
     */
    private Document create(String frameType, String operationType) throws Exception {
        Document doc = UXml.createBlankDocument();
        
        // 获取 Tmp 配置
        String tmpName = getTmpNameByOperation(operationType);
        EwaDefineConfig.TmpConfig tmpConfig = defineConfig.getTmpConfig(frameType, tmpName);
        
        if (tmpConfig == null) {
            LOGGER.warn("未找到配置：{}.{}，使用默认配置", frameType, tmpName);
        }
        
        // 创建根节点 EasyWebTemplate
        org.w3c.dom.Element root = doc.createElement("EasyWebTemplate");
        doc.appendChild(root);
        
        // 设置根节点属性
        String itemName = this.table.getName() + "." + getFrameTypeShort(frameType) + "." + operationType;
        root.setAttribute("Name", itemName);
        root.setAttribute("Author", "System");
        root.setAttribute("CreateDate", getCurrentDateTime());
        root.setAttribute("UpdateDate", getCurrentDateTime());
        
        // 创建 Page 节点
        org.w3c.dom.Element page = doc.createElement("Page");
        root.appendChild(page);
        
        // 创建 Page 的基本配置节点
        createPageBasicConfig(doc, page, frameType, operationType, itemName);
        
        // 创建 Action 节点
        createPageAction(doc, page, frameType, operationType, tmpConfig);
        
        // 创建 XItems 节点
        org.w3c.dom.Element xitems = doc.createElement("XItems");
        page.appendChild(xitems);
        
        // 根据表字段创建 XItem
        for (int i = 0; i < this.table.getFields().getFieldList().size(); i++) {
            String fieldName = this.table.getFields().getFieldList().get(i);
            com.gdxsoft.easyweb.define.database.Field field = this.table.getFields().get(fieldName);

            org.w3c.dom.Element xitem = createXItem(doc, field, frameType, operationType, i);
            xitems.appendChild(xitem);
        }

        // 根据配置添加 Buttons（作为 XItem）
        if (tmpConfig != null && !tmpConfig.getButtonConfigs().isEmpty()) {
            createButtonsFromConfig(doc, xitems, tmpConfig);
        }
        
        // 根据配置添加 AddScript 和 AddCss
        if (tmpConfig != null && !tmpConfig.getAddConfigs().isEmpty()) {
            addFromConfig(doc, tmpConfig);
        }
        
        // 创建 Menus 节点
        createMenus(doc, root);
        
        // 创建 Charts 节点
        createCharts(doc, root);
        
        // 创建 PageInfos 节点
        createPageInfos(doc, root, tmpConfig);
        
        // 创建 Workflows 节点
        createWorkflows(doc, root);

        this.xmlDoc = doc;
        return doc;
    }
    
    /**
     * 根据操作类型获取 Tmp 名称
     */
    private String getTmpNameByOperation(String operationType) {
        if (operationType.equals("N")) return "N";
        if (operationType.equals("M")) return "M";
        if (operationType.equals("V")) return "V";
        if (operationType.equals("NM")) return "NM";
        return "NM"; // 默认
    }
    
    /**
     * 获取当前日期时间
     */
    private String getCurrentDateTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
    }
    
    /**
     * 创建 Page 的基本配置
     */
    private void createPageBasicConfig(Document doc, org.w3c.dom.Element page,
            String frameType, String operationType, String itemName) {

        // Name 节点
        org.w3c.dom.Element nameNode = doc.createElement("Name");
        page.appendChild(nameNode);
        org.w3c.dom.Element nameSet = doc.createElement("Set");
        nameSet.setAttribute("Name", itemName);
        nameNode.appendChild(nameSet);

        // FrameTag 节点
        org.w3c.dom.Element frameTag = doc.createElement("FrameTag");
        page.appendChild(frameTag);
        org.w3c.dom.Element frameTagSet = doc.createElement("Set");
        frameTagSet.setAttribute("FrameTag", frameType);
        frameTag.appendChild(frameTagSet);

        // ListFrame 模式添加 Tag 配置
        if (frameType.equalsIgnoreCase("ListFrame")) {
            org.w3c.dom.Element tag = doc.createElement("Tag");
            page.appendChild(tag);
            org.w3c.dom.Element tagSet = doc.createElement("Set");
            tagSet.setAttribute("Tag", "span");
            tag.appendChild(tagSet);
        }

        // SkinName 节点
        org.w3c.dom.Element skinName = doc.createElement("SkinName");
        page.appendChild(skinName);
        org.w3c.dom.Element skinNameSet = doc.createElement("Set");
        skinNameSet.setAttribute("IsXhtml", "0");
        skinNameSet.setAttribute("SkinName", "Test1");
        skinName.appendChild(skinNameSet);

        // DataSource 节点
        org.w3c.dom.Element dataSource = doc.createElement("DataSource");
        page.appendChild(dataSource);
        org.w3c.dom.Element dataSourceSet = doc.createElement("Set");
        dataSourceSet.setAttribute("DataSource", "globaltravel");
        dataSource.appendChild(dataSourceSet);

        // AllowJsonExport 节点
        org.w3c.dom.Element allowJsonExport = doc.createElement("AllowJsonExport");
        page.appendChild(allowJsonExport);

        // ConfigMemo 节点
        org.w3c.dom.Element configMemo = doc.createElement("ConfigMemo");
        page.appendChild(configMemo);
        org.w3c.dom.Element configMemoSet = doc.createElement("Set");
        org.w3c.dom.Element configMemoInner = doc.createElement("ConfigMemo");
        configMemoSet.appendChild(configMemoInner);
        configMemo.appendChild(configMemoSet);

        // Cached 节点
        org.w3c.dom.Element cached = doc.createElement("Cached");
        page.appendChild(cached);
        org.w3c.dom.Element cachedSet = doc.createElement("Set");
        cachedSet.setAttribute("CachedSeconds", "");
        cachedSet.setAttribute("CachedType", "");
        cached.appendChild(cachedSet);

        // Acl 节点
        org.w3c.dom.Element acl = doc.createElement("Acl");
        page.appendChild(acl);
        org.w3c.dom.Element aclSet = doc.createElement("Set");
        aclSet.setAttribute("Acl", "com.gdxsoft.web.acl.BusinessImpl");
        acl.appendChild(aclSet);

        // Log 节点
        org.w3c.dom.Element log = doc.createElement("Log");
        page.appendChild(log);
        org.w3c.dom.Element logSet = doc.createElement("Set");
        logSet.setAttribute("Log", "com.gdxsoft.web.log.EwaScriptLog");
        log.appendChild(logSet);

        // DescriptionSet 节点
        org.w3c.dom.Element descSet = doc.createElement("DescriptionSet");
        page.appendChild(descSet);
        org.w3c.dom.Element descSetZhcn = doc.createElement("Set");
        descSetZhcn.setAttribute("Info", this.table.getName() + " " + getFrameName(frameType) + " " + getOperationName(operationType));
        descSetZhcn.setAttribute("Lang", "zhcn");
        descSetZhcn.setAttribute("Memo", "");
        descSet.appendChild(descSetZhcn);
        org.w3c.dom.Element descSetEnus = doc.createElement("Set");
        descSetEnus.setAttribute("Info", this.table.getName() + " " + getFrameName(frameType) + " " + getOperationName(operationType));
        descSetEnus.setAttribute("Lang", "enus");
        descSetEnus.setAttribute("Memo", "");
        descSet.appendChild(descSetEnus);

        // PageAttributeSet 节点
        org.w3c.dom.Element pageAttributeSet = doc.createElement("PageAttributeSet");
        page.appendChild(pageAttributeSet);

        // RowAttributeSet 节点
        org.w3c.dom.Element rowAttributeSet = doc.createElement("RowAttributeSet");
        page.appendChild(rowAttributeSet);

        // GroupSet 节点
        org.w3c.dom.Element groupSet = doc.createElement("GroupSet");
        page.appendChild(groupSet);

        // Size 节点
        org.w3c.dom.Element size = doc.createElement("Size");
        page.appendChild(size);
        org.w3c.dom.Element sizeSet = doc.createElement("Set");
        sizeSet.setAttribute("FrameCols", "");
        sizeSet.setAttribute("HAlign", "center");
        sizeSet.setAttribute("Height", "");
        sizeSet.setAttribute("HiddenCaption", "");
        sizeSet.setAttribute("TextareaAuto", "");
        sizeSet.setAttribute("VAlign", "top");
        sizeSet.setAttribute("Width", "100%");
        size.appendChild(sizeSet);

        // AddHtml 节点
        org.w3c.dom.Element addHtml = doc.createElement("AddHtml");
        page.appendChild(addHtml);
        org.w3c.dom.Element addHtmlSet = doc.createElement("Set");
        org.w3c.dom.Element addHtmlTop = doc.createElement("Top");
        addHtmlSet.appendChild(addHtmlTop);
        org.w3c.dom.Element addHtmlBottom = doc.createElement("Bottom");
        addHtmlSet.appendChild(addHtmlBottom);
        addHtml.appendChild(addHtmlSet);
        
        // AddScript 节点（空，实际内容从 EwaDefine.xml 读取）
        org.w3c.dom.Element addScript = doc.createElement("AddScript");
        page.appendChild(addScript);
        org.w3c.dom.Element addScriptSet = doc.createElement("Set");
        org.w3c.dom.Element addScriptTop = doc.createElement("Top");
        addScriptSet.appendChild(addScriptTop);
        org.w3c.dom.Element addScriptBottom = doc.createElement("Bottom");
        addScriptSet.appendChild(addScriptBottom);
        addScript.appendChild(addScriptSet);
        
        // AddCss 节点
        org.w3c.dom.Element addCss = doc.createElement("AddCss");
        page.appendChild(addCss);
        org.w3c.dom.Element addCssSet = doc.createElement("Set");
        org.w3c.dom.Element addCssContent = doc.createElement("AddCss");
        addCssSet.appendChild(addCssContent);
        addCss.appendChild(addCssSet);
        
        // ChartsShow 节点
        org.w3c.dom.Element chartsShow = doc.createElement("ChartsShow");
        page.appendChild(chartsShow);
        
        // RedrawJson 节点
        org.w3c.dom.Element redrawJson = doc.createElement("RedrawJson");
        page.appendChild(redrawJson);
        
        // BoxJson 节点
        org.w3c.dom.Element boxJson = doc.createElement("BoxJson");
        page.appendChild(boxJson);
        
        // LeftJson 节点
        org.w3c.dom.Element leftJson = doc.createElement("LeftJson");
        page.appendChild(leftJson);
        
        // FrameHtml 节点
        org.w3c.dom.Element frameHtml = doc.createElement("FrameHtml");
        page.appendChild(frameHtml);
        org.w3c.dom.Element frameHtmlSet = doc.createElement("Set");
        org.w3c.dom.Element frameHtmlInner = doc.createElement("FrameHtml");
        frameHtmlSet.appendChild(frameHtmlInner);
        frameHtml.appendChild(frameHtmlSet);
        
        // PageSize 节点
        org.w3c.dom.Element pageSize = doc.createElement("PageSize");
        page.appendChild(pageSize);
        org.w3c.dom.Element pageSizeSet = doc.createElement("Set");
        pageSizeSet.setAttribute("IsSplitPage", "1");
        pageSizeSet.setAttribute("PageSize", "10");
        pageSizeSet.setAttribute("Recycle", "1");
        // 设置 KeyField 为表的主键
        String keyField = getPrimaryKeyField();
        if (keyField != null && !keyField.isEmpty()) {
            pageSizeSet.setAttribute("KeyField", keyField);
        }
        pageSize.appendChild(pageSizeSet);
        
        // ListUI 节点
        org.w3c.dom.Element listUI = doc.createElement("ListUI");
        page.appendChild(listUI);
        org.w3c.dom.Element listUISet = doc.createElement("Set");
        listUISet.setAttribute("luButtons", "1");
        listUISet.setAttribute("luSearch", "1");
        listUISet.setAttribute("luSelect", "s");
        listUI.appendChild(listUISet);
        
        // MenuShow 节点
        org.w3c.dom.Element menuShow = doc.createElement("MenuShow");
        page.appendChild(menuShow);
        
        // Menu 节点
        org.w3c.dom.Element menu = doc.createElement("Menu");
        page.appendChild(menu);
        
        // Tree 节点
        org.w3c.dom.Element tree = doc.createElement("Tree");
        page.appendChild(tree);
        
        // HtmlFrame 节点
        org.w3c.dom.Element htmlFrame = doc.createElement("HtmlFrame");
        page.appendChild(htmlFrame);
        
        // TreeIconSet 节点
        org.w3c.dom.Element treeIconSet = doc.createElement("TreeIconSet");
        page.appendChild(treeIconSet);
        
        // MGAxisX 节点
        org.w3c.dom.Element mgAxisX = doc.createElement("MGAxisX");
        page.appendChild(mgAxisX);
        
        // MGAxisY 节点
        org.w3c.dom.Element mgAxisY = doc.createElement("MGAxisY");
        page.appendChild(mgAxisY);
        
        // MGCell 节点
        org.w3c.dom.Element mgCell = doc.createElement("MGCell");
        page.appendChild(mgCell);
        
        // LogicShow 节点
        org.w3c.dom.Element logicShow = doc.createElement("LogicShow");
        page.appendChild(logicShow);
    }
    
    /**
     * 获取 Frame 名称
     */
    private String getFrameName(String frameType) {
        switch (frameType.toUpperCase()) {
            case "FRAME": return "框架";
            case "LISTFRAME": return "列表";
            case "TREE": return "树形";
            default: return frameType;
        }
    }
    
    /**
     * 获取操作名称
     */
    private String getOperationName(String operationType) {
        switch (operationType.toUpperCase()) {
            case "N": return "新增";
            case "M": return "修改";
            case "V": return "查看";
            case "NM": return "新增修改";
            default: return operationType;
        }
    }
    
    /**
     * 创建 XItem 节点
     */
    private org.w3c.dom.Element createXItem(Document doc,
            com.gdxsoft.easyweb.define.database.Field field,
            String frameType, String operationType, int index) {

        org.w3c.dom.Element xitem = doc.createElement("XItem");
        xitem.setAttribute("Name", field.getName());

        // Tag 节点
        org.w3c.dom.Element tag = doc.createElement("Tag");
        org.w3c.dom.Element tagSet = doc.createElement("Set");
        tagSet.setAttribute("IsLFEdit", "0");
        tagSet.setAttribute("SpanShowAs", "");
        
        // ListFrame 模式默认 tag=span（按钮除外）
        if (frameType.equalsIgnoreCase("ListFrame")) {
            tagSet.setAttribute("Tag", "span");
        } else {
            tagSet.setAttribute("Tag", getTagType(field.getDatabaseType()));
        }
        
        tag.appendChild(tagSet);
        xitem.appendChild(tag);
        
        // Name 节点
        org.w3c.dom.Element name = doc.createElement("Name");
        org.w3c.dom.Element nameSet = doc.createElement("Set");
        nameSet.setAttribute("Name", field.getName());
        name.appendChild(nameSet);
        xitem.appendChild(name);
        
        // GroupIndex 节点
        org.w3c.dom.Element groupIndex = doc.createElement("GroupIndex");
        org.w3c.dom.Element groupIndexSet = doc.createElement("Set");
        groupIndexSet.setAttribute("GroupIndex", "0");
        groupIndex.appendChild(groupIndexSet);
        xitem.appendChild(groupIndex);
        
        // InitValue 节点
        org.w3c.dom.Element initValue = doc.createElement("InitValue");
        org.w3c.dom.Element initValueSet = doc.createElement("Set");
        initValueSet.setAttribute("InitValue", "");
        initValue.appendChild(initValueSet);
        xitem.appendChild(initValue);
        
        // DescriptionSet 节点
        org.w3c.dom.Element descSet = doc.createElement("DescriptionSet");
        org.w3c.dom.Element descSetZhcn = doc.createElement("Set");
        descSetZhcn.setAttribute("Info", field.getDescription() != null ? field.getDescription() : field.getName());
        descSetZhcn.setAttribute("Lang", "zhcn");
        descSetZhcn.setAttribute("Memo", "");
        descSet.appendChild(descSetZhcn);
        org.w3c.dom.Element descSetEnus = doc.createElement("Set");
        descSetEnus.setAttribute("Info", "");
        descSetEnus.setAttribute("Lang", "enus");
        descSetEnus.setAttribute("Memo", "");
        descSet.appendChild(descSetEnus);
        xitem.appendChild(descSet);
        
        // XStyle 节点
        org.w3c.dom.Element xstyle = doc.createElement("XStyle");
        xitem.appendChild(xstyle);
        
        // Style 节点
        org.w3c.dom.Element style = doc.createElement("Style");
        org.w3c.dom.Element styleSet = doc.createElement("Set");
        styleSet.setAttribute("Style", "");
        style.appendChild(styleSet);
        xitem.appendChild(style);
        
        // ParentStyle 节点
        org.w3c.dom.Element parentStyle = doc.createElement("ParentStyle");
        org.w3c.dom.Element parentStyleSet = doc.createElement("Set");
        parentStyleSet.setAttribute("ParentStyle", "");
        parentStyle.appendChild(parentStyleSet);
        xitem.appendChild(parentStyle);
        
        // AttributeSet 节点
        org.w3c.dom.Element attributeSet = doc.createElement("AttributeSet");
        org.w3c.dom.Element attributeSetItem = doc.createElement("Set");
        attributeSetItem.setAttribute("AttLogic", "");
        attributeSetItem.setAttribute("AttName", "");
        attributeSetItem.setAttribute("AttValue", "");
        attributeSet.appendChild(attributeSetItem);
        xitem.appendChild(attributeSet);
        
        // EventSet 节点
        org.w3c.dom.Element eventSet = doc.createElement("EventSet");
        org.w3c.dom.Element eventSetItem = doc.createElement("Set");
        eventSetItem.setAttribute("EventLogic", "");
        eventSetItem.setAttribute("EventName", "");
        eventSetItem.setAttribute("EventType", "");
        eventSetItem.setAttribute("EventValue", "");
        eventSet.appendChild(eventSetItem);
        xitem.appendChild(eventSet);
        
        // IsHtml 节点
        org.w3c.dom.Element isHtml = doc.createElement("IsHtml");
        xitem.appendChild(isHtml);

        // OrderSearch 节点
        org.w3c.dom.Element orderSearch = doc.createElement("OrderSearch");
        org.w3c.dom.Element orderSearchSet = doc.createElement("Set");
        orderSearchSet.setAttribute("GroupTestLength", "");
        orderSearchSet.setAttribute("IsGroup", "");
        orderSearchSet.setAttribute("IsGroupDefault", "");
        
        // 根据字段类型设置 Order 和 SearchType
        // 数字：Order=1, SearchType=""
        // 文字<=100: Order=1, SearchType="text"
        // 文字>100: Order=0, SearchType="text"
        // 日期：Order=1, SearchType=""
        String fieldType = field.getDatabaseType().toUpperCase();
        String order = "0";
        String searchType = "";
        
        if (fieldType.contains("INT") || fieldType.contains("DECIMAL") || 
            fieldType.contains("NUM") || fieldType.contains("DOUBLE") || 
            fieldType.contains("FLOAT") || fieldType.contains("MONEY")) {
            // 数字类型
            order = "1";
            searchType = "";
        } else if (fieldType.contains("DATE") || fieldType.contains("TIME")) {
            // 日期类型
            order = "1";
            searchType = "";
        } else if (fieldType.contains("CHAR") || fieldType.contains("TEXT")) {
            // 文字类型
            searchType = "text";
            if (field.getMaxlength() <= 100) {
                order = "1";
            } else {
                order = "0";
            }
        }
        
        orderSearchSet.setAttribute("IsOrder", order);
        orderSearchSet.setAttribute("IsSearchQuick", "1");
        orderSearchSet.setAttribute("OrderExp", "");
        orderSearchSet.setAttribute("SearchExp", "");
        orderSearchSet.setAttribute("SearchMulti", "2");
        orderSearchSet.setAttribute("SearchSql", "");
        orderSearchSet.setAttribute("SearchType", searchType);
        orderSearch.appendChild(orderSearchSet);
        xitem.appendChild(orderSearch);
        
        // MaxMinLength 节点
        org.w3c.dom.Element maxMinLength = doc.createElement("MaxMinLength");
        org.w3c.dom.Element maxMinLengthSet = doc.createElement("Set");
        maxMinLengthSet.setAttribute("MaxLength", String.valueOf(field.getMaxlength()));
        maxMinLengthSet.setAttribute("MinLength", "");
        maxMinLength.appendChild(maxMinLengthSet);
        xitem.appendChild(maxMinLength);
        
        // MaxMinValue 节点
        org.w3c.dom.Element maxMinValue = doc.createElement("MaxMinValue");
        org.w3c.dom.Element maxMinValueSet = doc.createElement("Set");
        maxMinValueSet.setAttribute("MaxValue", "");
        maxMinValueSet.setAttribute("MinValue", "");
        maxMinValue.appendChild(maxMinValueSet);
        xitem.appendChild(maxMinValue);
        
        // IsMustInput 节点
        org.w3c.dom.Element isMustInput = doc.createElement("IsMustInput");
        org.w3c.dom.Element isMustInputSet = doc.createElement("Set");
        isMustInputSet.setAttribute("IsMustInput", field.isPk() ? "0" : "1");
        isMustInput.appendChild(isMustInputSet);
        xitem.appendChild(isMustInput);
        
        // Switch 节点
        org.w3c.dom.Element switchNode = doc.createElement("Switch");
        xitem.appendChild(switchNode);
        
        // DataItem 节点
        org.w3c.dom.Element dataItem = doc.createElement("DataItem");
        org.w3c.dom.Element dataItemSet = doc.createElement("Set");
        dataItemSet.setAttribute("DataField", field.getName());
        dataItemSet.setAttribute("DataType", getDataType(field.getDatabaseType()));
        dataItemSet.setAttribute("DisableOnModify", "");
        dataItemSet.setAttribute("Format", getFormat(field.getDatabaseType()));
        dataItemSet.setAttribute("FrameOneCell", "");
        dataItemSet.setAttribute("Icon", "");
        dataItemSet.setAttribute("IconLoction", "");
        dataItemSet.setAttribute("IsEncrypt", "");
        dataItemSet.setAttribute("NumberScale", getNumberScale(field.getDatabaseType()));
        dataItemSet.setAttribute("SumBottom", "");
        dataItemSet.setAttribute("TransTarget", "");
        dataItemSet.setAttribute("Translation", "");
        dataItemSet.setAttribute("Trim", "");
        dataItemSet.setAttribute("Valid", getValidType(field.getDatabaseType()));
        dataItem.appendChild(dataItemSet);
        xitem.appendChild(dataItem);
        
        // DispEnc 节点
        org.w3c.dom.Element dispEnc = doc.createElement("DispEnc");
        xitem.appendChild(dispEnc);
        
        // DataRef 节点
        org.w3c.dom.Element dataRef = doc.createElement("DataRef");
        xitem.appendChild(dataRef);
        
        // List 节点
        org.w3c.dom.Element list = doc.createElement("List");
        if (needList(field.getDatabaseType())) {
            org.w3c.dom.Element listSet = doc.createElement("Set");
            listSet.setAttribute("DisplayField", getFieldDisplayName(field));
            listSet.setAttribute("DisplayList", "");
            listSet.setAttribute("GroupField", "");
            listSet.setAttribute("ListAddBlank", "1");
            listSet.setAttribute("ListFilterField", "");
            listSet.setAttribute("ListFilterType", "");
            listSet.setAttribute("ListShowType", "");
            listSet.setAttribute("ParentField", "");
            listSet.setAttribute("Sql", getListSql(field));
            listSet.setAttribute("TitleField", "");
            listSet.setAttribute("TitleList", "");
            listSet.setAttribute("ValueField", getFieldValueName(field));
            listSet.setAttribute("ValueList", "");
            list.appendChild(listSet);
        }
        xitem.appendChild(list);
        
        // UserSet 节点
        org.w3c.dom.Element userSet = doc.createElement("UserSet");
        xitem.appendChild(userSet);
        
        // CallAction 节点
        org.w3c.dom.Element callAction = doc.createElement("CallAction");
        xitem.appendChild(callAction);
        
        // OpenFrame 节点
        org.w3c.dom.Element openFrame = doc.createElement("OpenFrame");
        xitem.appendChild(openFrame);
        
        // Frame 节点
        org.w3c.dom.Element frame = doc.createElement("Frame");
        if (needFrame(field.getDatabaseType())) {
            org.w3c.dom.Element frameSet = doc.createElement("Set");
            frameSet.setAttribute("CallItemName", getFieldDisplayName(field) + ".ListFrame.View");
            frameSet.setAttribute("CallPara", "a");
            frameSet.setAttribute("CallXmlName", "business/common/droplist.xml");
            frame.appendChild(frameSet);
        }
        xitem.appendChild(frame);
        
        // UserControl 节点
        org.w3c.dom.Element userControl = doc.createElement("UserControl");
        xitem.appendChild(userControl);
        
        // DefineFrame 节点
        org.w3c.dom.Element defineFrame = doc.createElement("DefineFrame");
        xitem.appendChild(defineFrame);
        
        // PopFrame 节点
        org.w3c.dom.Element popFrame = doc.createElement("PopFrame");
        xitem.appendChild(popFrame);
        
        // signature 节点
        org.w3c.dom.Element signature = doc.createElement("signature");
        xitem.appendChild(signature);
        
        // Upload 节点
        org.w3c.dom.Element upload = doc.createElement("Upload");
        xitem.appendChild(upload);
        
        // VaildEx 节点
        org.w3c.dom.Element vaildEx = doc.createElement("VaildEx");
        org.w3c.dom.Element vaildExSet = doc.createElement("Set");
        vaildExSet.setAttribute("VXAction", "");
        vaildExSet.setAttribute("VXFail", "");
        vaildExSet.setAttribute("VXJs", getValidJs(field));
        vaildExSet.setAttribute("VXMode", "");
        vaildExSet.setAttribute("VXOk", "");
        vaildEx.appendChild(vaildExSet);
        xitem.appendChild(vaildEx);
        
        // MGAddField 节点
        org.w3c.dom.Element mgAddField = doc.createElement("MGAddField");
        xitem.appendChild(mgAddField);
        
        // AnchorParas 节点
        org.w3c.dom.Element anchorParas = doc.createElement("AnchorParas");
        xitem.appendChild(anchorParas);
        
        // LinkButtonParas 节点
        org.w3c.dom.Element linkButtonParas = doc.createElement("LinkButtonParas");
        xitem.appendChild(linkButtonParas);
        
        // DopListShow 节点
        org.w3c.dom.Element dopListShow = doc.createElement("DopListShow");
        xitem.appendChild(dopListShow);
        
        // ReportCfg 节点
        org.w3c.dom.Element reportCfg = doc.createElement("ReportCfg");
        xitem.appendChild(reportCfg);
        
        // CombineFrame 节点
        org.w3c.dom.Element combineFrame = doc.createElement("CombineFrame");
        xitem.appendChild(combineFrame);
        
        // AddrMapRels 节点
        org.w3c.dom.Element addrMapRels = doc.createElement("AddrMapRels");
        xitem.appendChild(addrMapRels);
        
        // ImageDefault 节点
        org.w3c.dom.Element imageDefault = doc.createElement("ImageDefault");
        xitem.appendChild(imageDefault);
        
        return xitem;
    }
    
    /**
     * 获取字段对应的 Tag 类型
     */
    private String getTagType(String dbType) {
        if (dbType == null) return "text";
        String type = dbType.toUpperCase();
        if (type.contains("DATE") || type.contains("TIME")) return "datetime";
        if (type.contains("INT") || type.contains("DECIMAL") || type.contains("NUM")) return "text";
        if (type.contains("TEXT") || type.contains("CHAR")) return "text";
        return "text";
    }
    
    /**
     * 获取数据类型
     */
    private String getDataType(String dbType) {
        if (dbType == null) return "String";
        String type = dbType.toUpperCase();
        if (type.contains("INT")) return "Int";
        if (type.contains("DECIMAL") || type.contains("NUM")) return "Double";
        if (type.contains("DATE") || type.contains("TIME")) return "Date";
        return "String";
    }
    
    /**
     * 获取验证类型
     */
    private String getValidType(String dbType) {
        if (dbType == null) return "";
        String type = dbType.toUpperCase();
        if (type.contains("EMAIL")) return "Email";
        if (type.contains("MOBILE") || type.contains("PHONE")) return "Mobile";
        return "";
    }
    
    /**
     * 获取验证 JavaScript
     */
    private String getValidJs(com.gdxsoft.easyweb.define.database.Field field) {
        String validType = getValidType(field.getDatabaseType());
        if (validType.equals("Email")) {
            return "if (v && !/^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$/.test(v)) { return '邮箱格式不正确'; }";
        }
        if (validType.equals("Mobile")) {
            return "if (v && !/^1[3-9]\\d{9}$/.test(v)) { return '手机号格式不正确'; }";
        }
        return "";
    }
    
    /**
     * 获取日期格式
     */
    private String getFormat(String dbType) {
        if (dbType == null) return "";
        String type = dbType.toUpperCase();
        
        // 日期时间类型（包含 DATE 和 TIME）
        if (type.contains("DATETIME") || type.contains("TIMESTAMP") || 
            (type.contains("DATE") && type.contains("TIME"))) {
            return "DateShortTime";
        }
        
        // 纯日期类型
        if (type.contains("DATE") && !type.contains("TIME")) {
            return "Date";
        }
        
        // 纯时间类型
        if (type.contains("TIME") && !type.contains("DATE")) {
            return "Time";
        }
        
        // 货币类型
        if (type.contains("MONEY") || type.contains("DECIMAL") || type.contains("NUMERIC")) {
            return "LeastMoney";
        }
        
        // 整数类型
        if (type.contains("INT")) {
            return "Int";
        }
        
        return "";
    }
    
    /**
     * 获取数字精度
     */
    private String getNumberScale(String dbType) {
        if (dbType == null) return "";
        String type = dbType.toUpperCase();
        if (type.contains("DECIMAL") || type.contains("NUMERIC") || type.contains("MONEY")) return "2";
        return "";
    }
    
    /**
     * 获取搜索 SQL
     */
    private String getSearchSql(com.gdxsoft.easyweb.define.database.Field field) {
        String name = field.getName();
        String desc = field.getDescription() != null ? field.getDescription() : name;
        // 为常用字段添加搜索 SQL
        if (name.endsWith("_ID") || name.contains("CODE") || name.contains("NAME")) {
            return "SELECT " + name + ", " + name + "_NAME FROM " + getTableNameFromField(name) + " WHERE " + name + "=@value";
        }
        return "";
    }
    
    /**
     * 从字段名获取表名
     */
    private String getTableNameFromField(String fieldName) {
        if (fieldName.startsWith("CRM_")) {
            // CRM_COM_ID -> CRM_COM
            int idx = fieldName.indexOf("_", 4);
            if (idx > 0) {
                return fieldName.substring(0, idx);
            }
            return fieldName;
        }
        return "DUAL";
    }
    
    /**
     * 获取 OrderSearch 的 IsOrder 值
     * 判断是否需要 List 数据源
     */
    private boolean needList(String dbType) {
        if (dbType == null) return false;
        String type = dbType.toUpperCase();
        // ID 结尾的字段通常需要关联
        if (dbType.endsWith("_ID")) return true;
        return false;
    }
    
    /**
     * 判断是否需要 Frame 关联
     */
    private boolean needFrame(String dbType) {
        return needList(dbType);
    }
    
    /**
     * 获取 List 显示字段名
     */
    private String getFieldDisplayName(com.gdxsoft.easyweb.define.database.Field field) {
        String name = field.getName();
        if (name.endsWith("_ID")) {
            // 将 CRM_COM_ID 转换为 CRM_COM_NAME
            return name.substring(0, name.length() - 3) + "_NAME";
        }
        return name + "_NAME";
    }
    
    /**
     * 获取 List 值字段名
     */
    private String getFieldValueName(com.gdxsoft.easyweb.define.database.Field field) {
        return field.getName();
    }
    
    /**
     * 获取 List SQL
     */
    private String getListSql(com.gdxsoft.easyweb.define.database.Field field) {
        String name = field.getName();
        if (name.endsWith("_ID")) {
            String tableName = name.substring(0, name.length() - 3);
            return "SELECT " + tableName + "_ID, " + tableName + "_NAME FROM " + tableName;
        }
        return "";
    }
    
    /**
     * 创建提交按钮
     */
    private org.w3c.dom.Element createSubmitButton(Document doc) {
        org.w3c.dom.Element xitem = doc.createElement("XItem");
        xitem.setAttribute("Name", "butOk");
        
        // Tag 节点
        org.w3c.dom.Element tag = doc.createElement("Tag");
        org.w3c.dom.Element tagSet = doc.createElement("Set");
        tagSet.setAttribute("IsLFEdit", "0");
        tagSet.setAttribute("SpanShowAs", "");
        tagSet.setAttribute("Tag", "submit");
        tag.appendChild(tagSet);
        xitem.appendChild(tag);
        
        // Name 节点
        org.w3c.dom.Element name = doc.createElement("Name");
        org.w3c.dom.Element nameSet = doc.createElement("Set");
        nameSet.setAttribute("Name", "butOk");
        name.appendChild(nameSet);
        xitem.appendChild(name);
        
        // GroupIndex 节点
        org.w3c.dom.Element groupIndex = doc.createElement("GroupIndex");
        org.w3c.dom.Element groupIndexSet = doc.createElement("Set");
        groupIndexSet.setAttribute("GroupIndex", "");
        groupIndex.appendChild(groupIndexSet);
        xitem.appendChild(groupIndex);
        
        // InitValue 节点
        org.w3c.dom.Element initValue = doc.createElement("InitValue");
        xitem.appendChild(initValue);
        
        // DescriptionSet 节点
        org.w3c.dom.Element descSet = doc.createElement("DescriptionSet");
        org.w3c.dom.Element descSetZhcn = doc.createElement("Set");
        descSetZhcn.setAttribute("Info", "确定");
        descSetZhcn.setAttribute("Lang", "zhcn");
        descSetZhcn.setAttribute("Memo", "");
        descSet.appendChild(descSetZhcn);
        org.w3c.dom.Element descSetEnus = doc.createElement("Set");
        descSetEnus.setAttribute("Info", "Ok");
        descSetEnus.setAttribute("Lang", "enus");
        descSetEnus.setAttribute("Memo", "");
        descSet.appendChild(descSetEnus);
        xitem.appendChild(descSet);
        
        // XStyle 节点
        org.w3c.dom.Element xstyle = doc.createElement("XStyle");
        xitem.appendChild(xstyle);
        
        // Style 节点
        org.w3c.dom.Element style = doc.createElement("Style");
        org.w3c.dom.Element styleSet = doc.createElement("Set");
        styleSet.setAttribute("Style", "");
        style.appendChild(styleSet);
        xitem.appendChild(style);
        
        // ParentStyle 节点
        org.w3c.dom.Element parentStyle = doc.createElement("ParentStyle");
        org.w3c.dom.Element parentStyleSet = doc.createElement("Set");
        parentStyleSet.setAttribute("ParentStyle", "width:40px; text-align: center");
        parentStyle.appendChild(parentStyleSet);
        xitem.appendChild(parentStyle);
        
        // 添加其他空节点
        String[] emptyNodes = {"AttributeSet", "EventSet", "IsHtml", "OrderSearch", "MaxMinLength", 
            "MaxMinValue", "IsMustInput", "Switch", "DataItem", "DispEnc", "DataRef", "List", 
            "UserSet", "CallAction", "OpenFrame", "Frame", "UserControl", "DefineFrame", "PopFrame", 
            "signature", "Upload", "VaildEx", "MGAddField", "AnchorParas", "LinkButtonParas", 
            "DopListShow", "ReportCfg", "CombineFrame", "AddrMapRels", "ImageDefault"};
        
        for (String nodeName : emptyNodes) {
            xitem.appendChild(doc.createElement(nodeName));
        }
        
        return xitem;
    }
    
    /**
     * 创建 Page 的 Action 配置
     */
    private void createPageAction(Document doc, org.w3c.dom.Element page, 
            String frameType, String operationType, EwaDefineConfig.TmpConfig tmpConfig) {
        
        // Action 节点
        org.w3c.dom.Element action = doc.createElement("Action");
        page.appendChild(action);
        
        // 如果配置中有 Actions，使用配置
        if (tmpConfig != null && !tmpConfig.getActionConfigs().isEmpty()) {
            // ActionSet 节点
            org.w3c.dom.Element actionSet = doc.createElement("ActionSet");
            action.appendChild(actionSet);
            
            for (EwaDefineConfig.ActionConfig actConfig : tmpConfig.getActionConfigs()) {
                actionSet.appendChild(createActionSetItem(doc, actConfig));
            }
            
            // SqlSet 节点
            org.w3c.dom.Element sqlSet = doc.createElement("SqlSet");
            action.appendChild(sqlSet);
            
            for (EwaDefineConfig.ActionConfig actConfig : tmpConfig.getActionConfigs()) {
                if (actConfig.getSqlType() != null) {
                    sqlSet.appendChild(createSqlSetItem(doc, actConfig));
                }
            }
        } else {
            // 默认配置
            createDefaultPageAction(doc, action, frameType, operationType);
        }
        
        // 添加其他空节点
        action.appendChild(doc.createElement("JSONSet"));
        action.appendChild(doc.createElement("ClassSet"));
        action.appendChild(doc.createElement("XmlSet"));
        action.appendChild(doc.createElement("XmlSetData"));
        action.appendChild(doc.createElement("ScriptSet"));
        action.appendChild(doc.createElement("UrlSet"));
        action.appendChild(doc.createElement("CSSet"));
    }
    
    /**
     * 创建 ActionSet 项
     */
    private org.w3c.dom.Element createActionSetItem(Document doc, EwaDefineConfig.ActionConfig config) {
        org.w3c.dom.Element set = doc.createElement("Set");
        set.setAttribute("LogMsg", "");
        set.setAttribute("Transcation", "");
        set.setAttribute("Type", config.getName());
        
        // CallSet 节点
        org.w3c.dom.Element callSet = doc.createElement("CallSet");
        org.w3c.dom.Element callSetItem = doc.createElement("Set");
        callSetItem.setAttribute("CallIsChk", "");
        callSetItem.setAttribute("CallName", config.getName() + " SQL");
        callSetItem.setAttribute("CallType", "SqlSet");
        callSetItem.setAttribute("Test", config.getTest());
        callSet.appendChild(callSetItem);
        set.appendChild(callSet);
        
        return set;
    }
    
    /**
     * 创建 SqlSet 项
     */
    private org.w3c.dom.Element createSqlSetItem(Document doc, EwaDefineConfig.ActionConfig config) {
        org.w3c.dom.Element set = doc.createElement("Set");
        set.setAttribute("Name", config.getName() + " SQL");
        set.setAttribute("SqlType", config.getSqlType());

        // Sql 节点
        org.w3c.dom.Element sql = doc.createElement("Sql");
        String sqlContent = config.getSql();
        
        // 解析 Sql 内容，如果是方法调用则生成实际 SQL
        if (sqlContent != null) {
            if (sqlContent.startsWith("'") && sqlContent.endsWith("'")) {
                sqlContent = sqlContent.substring(1, sqlContent.length() - 1);
            }
            
            // 处理 this.Fields.GetSqlXxx() 方法调用
            sqlContent = parseFieldsMethod(sqlContent);
        }
        
        sql.setTextContent(sqlContent != null ? sqlContent : "");
        set.appendChild(sql);

        // CSSet 节点
        set.appendChild(doc.createElement("CSSet"));

        return set;
    }
    
    /**
     * 解析 Fields 方法调用
     */
    private String parseFieldsMethod(String sqlContent) {
        if (sqlContent == null) return null;
        
        // 获取状态字段
        String statusField = getStatusField();
        
        // 替换方法调用为实际 SQL
        if (sqlContent.contains("this.Fields.GetSqlSelectLF()")) {
            return this.table.getFields().GetSqlSelectLF(statusField, true);
        } else if (sqlContent.contains("this.Fields.GetSqlSelect()")) {
            return this.table.getFields().GetSqlSelect();
        } else if (sqlContent.contains("this.Fields.GetSqlDeleteA()")) {
            return this.table.getFields().GetSqlDeleteA(statusField);
        } else if (sqlContent.contains("this.Fields.GetSqlRestore()")) {
            return this.table.getFields().GetSqlRestore(statusField);
        } else if (sqlContent.contains("this.Fields.GetSqlDelete()")) {
            return this.table.getFields().GetSqlDelete();
        } else if (sqlContent.contains("this.Fields.GetSqlUpdate()")) {
            return this.table.getFields().GetSqlUpdate(statusField);
        } else if (sqlContent.contains("this.Fields.GetSqlNew()")) {
            return this.table.getFields().GetSqlNew(statusField);
        }
        
        return sqlContent;
    }
    
    /**
     * 创建默认 Page Action
     */
    private void createDefaultPageAction(Document doc, org.w3c.dom.Element action, 
            String frameType, String operationType) {
        // ActionSet 节点
        org.w3c.dom.Element actionSet = doc.createElement("ActionSet");
        action.appendChild(actionSet);
        
        // OnPageLoad Action
        org.w3c.dom.Element onLoadAction = doc.createElement("Set");
        onLoadAction.setAttribute("Type", "OnPageLoad");
        org.w3c.dom.Element onLoadCall = doc.createElement("CallSet");
        org.w3c.dom.Element onLoadCallSet = doc.createElement("Set");
        onLoadCallSet.setAttribute("CallName", "OnPageLoad SQL");
        onLoadCallSet.setAttribute("CallType", "SqlSet");
        onLoadCall.appendChild(onLoadCallSet);
        onLoadAction.appendChild(onLoadCall);
        actionSet.appendChild(onLoadAction);
        
        // SqlSet 节点
        org.w3c.dom.Element sqlSet = doc.createElement("SqlSet");
        action.appendChild(sqlSet);

        // OnPageLoad SQL - 使用 Fields.GetSqlSelectLF() 生成的 SQL
        org.w3c.dom.Element onLoadSql = doc.createElement("Set");
        onLoadSql.setAttribute("Name", "OnPageLoad SQL");
        onLoadSql.setAttribute("SqlType", "query");
        org.w3c.dom.Element onLoadSqlContent = doc.createElement("Sql");
        
        // 获取状态字段名（ListFrame 模式）
        String statusField = getStatusField();
        
        // 根据 Frame 类型生成不同的 SQL
        if (frameType.equalsIgnoreCase("ListFrame")) {
            // ListFrame 使用 GetSqlSelectLF
            onLoadSqlContent.setTextContent(this.table.getFields().GetSqlSelectLF(statusField, true));
        } else {
            // Frame 使用 GetSqlSelect
            onLoadSqlContent.setTextContent(this.table.getFields().GetSqlSelect());
        }
        
        onLoadSql.appendChild(onLoadSqlContent);
        onLoadSql.appendChild(doc.createElement("CSSet"));
        sqlSet.appendChild(onLoadSql);
        
        // OnPagePost SQL (仅修改/新增模式)
        if (operationType.equals("M") || operationType.equals("N") || operationType.equals("NM")) {
            org.w3c.dom.Element onPostSql = doc.createElement("Set");
            onPostSql.setAttribute("Name", "OnPagePost SQL");
            onPostSql.setAttribute("SqlType", "update");
            org.w3c.dom.Element onPostSqlContent = doc.createElement("Sql");
            
            // 根据操作类型生成不同的 SQL
            if (operationType.equals("N")) {
                onPostSqlContent.setTextContent(this.table.getFields().GetSqlNew(statusField));
            } else if (operationType.equals("M")) {
                onPostSqlContent.setTextContent(this.table.getFields().GetSqlUpdate(statusField));
            } else { // NM
                onPostSqlContent.setTextContent("-- New or Update\n" + this.table.getFields().GetSqlNew(statusField));
            }
            
            onPostSql.appendChild(onPostSqlContent);
            onPostSql.appendChild(doc.createElement("CSSet"));
            sqlSet.appendChild(onPostSql);
        }
        
        // OnFrameDelete SQL (ListFrame.M 模式)
        if (frameType.equalsIgnoreCase("ListFrame") && 
            (operationType.equals("M") || operationType.equals("NM"))) {
            org.w3c.dom.Element onDeleteSql = doc.createElement("Set");
            onDeleteSql.setAttribute("Name", "OnFrameDelete SQL");
            onDeleteSql.setAttribute("SqlType", "update");
            org.w3c.dom.Element onDeleteSqlContent = doc.createElement("Sql");
            onDeleteSqlContent.setTextContent(this.table.getFields().GetSqlDeleteA(statusField));
            onDeleteSql.appendChild(onDeleteSqlContent);
            onDeleteSql.appendChild(doc.createElement("CSSet"));
            sqlSet.appendChild(onDeleteSql);
            
            org.w3c.dom.Element onRestoreSql = doc.createElement("Set");
            onRestoreSql.setAttribute("Name", "OnFrameRestore SQL");
            onRestoreSql.setAttribute("SqlType", "update");
            org.w3c.dom.Element onRestoreSqlContent = doc.createElement("Sql");
            onRestoreSqlContent.setTextContent(this.table.getFields().GetSqlRestore(statusField));
            onRestoreSql.appendChild(onRestoreSqlContent);
            onRestoreSql.appendChild(doc.createElement("CSSet"));
            sqlSet.appendChild(onRestoreSql);
        }
        
        // SAct0-2 SQL (标准查询 Action)
        for (int i = 0; i <= 2; i++) {
            org.w3c.dom.Element sActSql = doc.createElement("Set");
            sActSql.setAttribute("Name", "SAct" + i + " SQL");
            sActSql.setAttribute("SqlType", "query");
            org.w3c.dom.Element sActSqlContent = doc.createElement("Sql");
            sActSqlContent.setTextContent("-- enter your sql");
            sActSql.appendChild(sActSqlContent);
            sActSql.appendChild(doc.createElement("CSSet"));
            sqlSet.appendChild(sActSql);
        }
        
        // UAct0-2 SQL (标准更新 Action)
        for (int i = 0; i <= 2; i++) {
            org.w3c.dom.Element uActSql = doc.createElement("Set");
            uActSql.setAttribute("Name", "UAct" + i + " SQL");
            uActSql.setAttribute("SqlType", "update");
            org.w3c.dom.Element uActSqlContent = doc.createElement("Sql");
            uActSqlContent.setTextContent("-- enter your sql");
            uActSql.appendChild(uActSqlContent);
            uActSql.appendChild(doc.createElement("CSSet"));
            sqlSet.appendChild(uActSql);
        }
        
        // CheckError SQL
        org.w3c.dom.Element checkErrorSql = doc.createElement("Set");
        checkErrorSql.setAttribute("Name", "CheckError SQL");
        checkErrorSql.setAttribute("SqlType", "query");
        org.w3c.dom.Element checkErrorSqlContent = doc.createElement("Sql");
        checkErrorSqlContent.setTextContent("-- select 不能执行/javascript_func() as EWA_ERR_OUT FROM xxx where 1=2");
        checkErrorSql.appendChild(checkErrorSqlContent);
        checkErrorSql.appendChild(doc.createElement("CSSet"));
        sqlSet.appendChild(checkErrorSql);
    }
    
    /**
     * 获取状态字段名
     * @return 状态字段名，如果没有则返回 null
     */
    private String getStatusField() {
        // 查找状态字段（通常命名为 xxx_STATE 或 STATUS）
        for (String fieldName : this.table.getFields().getFieldList()) {
            if (fieldName.endsWith("_STATE") || fieldName.equals("STATUS") || fieldName.endsWith("_STATUS")) {
                return fieldName;
            }
        }
        return null;
    }
    
    /**
     * 获取主键字段名
     * @return 主键字段名，如果没有则返回 null
     */
    private String getPrimaryKeyField() {
        // 从 Table 对象获取主键
        if (this.table.getPk() != null) {
            ArrayList<com.gdxsoft.easyweb.define.database.Field> pkFields = this.table.getPk().getPkFields();
            if (pkFields != null && !pkFields.isEmpty()) {
                // 如果是复合主键，用逗号分隔
                ArrayList<String> fieldNames = new ArrayList<String>();
                for (com.gdxsoft.easyweb.define.database.Field pkField : pkFields) {
                    fieldNames.add(pkField.getName());
                }
                return String.join(",", fieldNames);
            }
        }
        return null;
    }
    
    /**
     * 创建 Menus 节点
     */
    private void createMenus(Document doc, org.w3c.dom.Element root) {
        org.w3c.dom.Element menus = doc.createElement("Menus");
        root.appendChild(menus);
    }
    
    /**
     * 创建 Charts 节点
     */
    private void createCharts(Document doc, org.w3c.dom.Element root) {
        org.w3c.dom.Element charts = doc.createElement("Charts");
        root.appendChild(charts);
    }
    
    /**
     * 创建 PageInfos 节点
     */
    private void createPageInfos(Document doc, org.w3c.dom.Element root, EwaDefineConfig.TmpConfig tmpConfig) {
        org.w3c.dom.Element pageInfos = doc.createElement("PageInfos");
        root.appendChild(pageInfos);
        
        // 如果配置中有 PageInfo，使用配置
        if (tmpConfig != null && !tmpConfig.getPageInfoConfigs().isEmpty()) {
            for (EwaDefineConfig.PageInfoConfig pageInfoConfig : tmpConfig.getPageInfoConfigs()) {
                pageInfos.appendChild(createPageInfoFromConfig(doc, pageInfoConfig));
            }
        } else {
            // 默认添加 3 个 PageInfo
            pageInfos.appendChild(createDefaultPageInfo(doc, "msg0", "消息 0", "Msg0"));
            pageInfos.appendChild(createDefaultPageInfo(doc, "msg1", "消息 1", "Msg1"));
            pageInfos.appendChild(createDefaultPageInfo(doc, "msg2", "消息 2", "Msg2"));
        }
    }
    
    /**
     * 创建 PageInfo
     */
    private org.w3c.dom.Element createPageInfoFromConfig(Document doc, EwaDefineConfig.PageInfoConfig config) {
        org.w3c.dom.Element pageInfo = doc.createElement("PageInfo");
        pageInfo.setAttribute("Name", config.getName());
        
        // Name 节点
        org.w3c.dom.Element name = doc.createElement("Name");
        org.w3c.dom.Element nameSet = doc.createElement("Set");
        nameSet.setAttribute("Name", config.getName());
        name.appendChild(nameSet);
        pageInfo.appendChild(name);
        
        // DescriptionSet 节点
        if (config.getDescriptionSet() != null) {
            pageInfo.appendChild(createDescriptionSet(doc, config.getDescriptionSet()));
        }
        
        return pageInfo;
    }
    
    /**
     * 创建默认 PageInfo
     */
    private org.w3c.dom.Element createDefaultPageInfo(Document doc, String name, String infoZhcn, String infoEnus) {
        org.w3c.dom.Element pageInfo = doc.createElement("PageInfo");
        pageInfo.setAttribute("Name", name);

        // Name 节点
        org.w3c.dom.Element nameElem = doc.createElement("Name");
        org.w3c.dom.Element nameSet = doc.createElement("Set");
        nameSet.setAttribute("Name", name);
        nameElem.appendChild(nameSet);
        pageInfo.appendChild(nameElem);

        // DescriptionSet 节点
        java.util.Map<String, String> descMap = new java.util.HashMap<>();
        descMap.put("zhcn", infoZhcn);
        descMap.put("enus", infoEnus);
        pageInfo.appendChild(createDescriptionSet(doc, descMap));

        return pageInfo;
    }
    
    /**
     * 根据配置创建 Buttons（作为 XItem 添加到 XItems）
     */
    private void createButtonsFromConfig(Document doc, org.w3c.dom.Element xitems, EwaDefineConfig.TmpConfig tmpConfig) {
        for (EwaDefineConfig.ButtonConfig btnConfig : tmpConfig.getButtonConfigs()) {
            xitems.appendChild(createButtonAsXItem(doc, btnConfig));
        }
    }
    
    /**
     * 将 Button 作为 XItem 创建
     */
    private org.w3c.dom.Element createButtonAsXItem(Document doc, EwaDefineConfig.ButtonConfig config) {
        org.w3c.dom.Element xitem = doc.createElement("XItem");
        xitem.setAttribute("Name", config.getName());

        // Tag 节点
        org.w3c.dom.Element tag = doc.createElement("Tag");
        org.w3c.dom.Element tagSet = doc.createElement("Set");
        if (config.getTag() != null && !config.getTag().isEmpty()) {
            tagSet.setAttribute("Tag", config.getTag());
        }
        tagSet.setAttribute("IsLFEdit", "0");
        tagSet.setAttribute("SpanShowAs", "");
        tag.appendChild(tagSet);
        xitem.appendChild(tag);

        // Name 节点
        org.w3c.dom.Element name = doc.createElement("Name");
        org.w3c.dom.Element nameSet = doc.createElement("Set");
        nameSet.setAttribute("Name", config.getName());
        name.appendChild(nameSet);
        xitem.appendChild(name);

        // GroupIndex 节点
        org.w3c.dom.Element groupIndex = doc.createElement("GroupIndex");
        xitem.appendChild(groupIndex);

        // InitValue 节点
        org.w3c.dom.Element initValue = doc.createElement("InitValue");
        xitem.appendChild(initValue);

        // DescriptionSet 节点
        if (config.getDescriptionSet() != null) {
            xitem.appendChild(createDescriptionSet(doc, config.getDescriptionSet()));
        }

        // XStyle 节点
        org.w3c.dom.Element xstyle = doc.createElement("XStyle");
        xitem.appendChild(xstyle);

        // Style 节点
        org.w3c.dom.Element style = doc.createElement("Style");
        org.w3c.dom.Element styleSet = doc.createElement("Set");
        styleSet.setAttribute("Style", "");
        style.appendChild(styleSet);
        xitem.appendChild(style);

        // ParentStyle 节点
        org.w3c.dom.Element parentStyle = doc.createElement("ParentStyle");
        org.w3c.dom.Element parentStyleSet = doc.createElement("Set");
        parentStyleSet.setAttribute("ParentStyle", "");
        parentStyle.appendChild(parentStyleSet);
        xitem.appendChild(parentStyle);

        // AttributeSet 节点
        org.w3c.dom.Element attributeSet = doc.createElement("AttributeSet");
        org.w3c.dom.Element attributeSetItem = doc.createElement("Set");
        attributeSetItem.setAttribute("AttLogic", "");
        attributeSetItem.setAttribute("AttName", "");
        attributeSetItem.setAttribute("AttValue", "");
        attributeSet.appendChild(attributeSetItem);
        xitem.appendChild(attributeSet);

        // EventSet 节点
        org.w3c.dom.Element eventSet = doc.createElement("EventSet");
        xitem.appendChild(eventSet);

        // IsHtml 节点
        org.w3c.dom.Element isHtml = doc.createElement("IsHtml");
        xitem.appendChild(isHtml);

        // OrderSearch 节点
        org.w3c.dom.Element orderSearch = doc.createElement("OrderSearch");
        xitem.appendChild(orderSearch);

        // MaxMinLength 节点
        org.w3c.dom.Element maxMinLength = doc.createElement("MaxMinLength");
        xitem.appendChild(maxMinLength);

        // MaxMinValue 节点
        org.w3c.dom.Element maxMinValue = doc.createElement("MaxMinValue");
        xitem.appendChild(maxMinValue);

        // IsMustInput 节点
        org.w3c.dom.Element isMustInput = doc.createElement("IsMustInput");
        xitem.appendChild(isMustInput);

        // Switch 节点
        org.w3c.dom.Element switchElem = doc.createElement("Switch");
        xitem.appendChild(switchElem);

        // DataItem 节点
        org.w3c.dom.Element dataItem = doc.createElement("DataItem");
        xitem.appendChild(dataItem);

        // DispEnc 节点
        org.w3c.dom.Element dispEnc = doc.createElement("DispEnc");
        xitem.appendChild(dispEnc);

        // DataRef 节点
        org.w3c.dom.Element dataRef = doc.createElement("DataRef");
        xitem.appendChild(dataRef);

        // List 节点
        org.w3c.dom.Element list = doc.createElement("List");
        xitem.appendChild(list);

        // UserSet 节点
        org.w3c.dom.Element userSet = doc.createElement("UserSet");
        xitem.appendChild(userSet);

        // 处理 Para 配置 (EventSet, CallAction 等)
        for (EwaDefineConfig.ParaConfig paraConfig : config.getParaConfigs()) {
            applyParaConfig(doc, xitem, paraConfig);
        }

        // CallAction 节点（butDelete 和 butRestore）
        if (config.getName().equals("butDelete") || config.getName().equals("butRestore")) {
            org.w3c.dom.Element callAction = doc.createElement("CallAction");
            org.w3c.dom.Element callActionSet = doc.createElement("Set");
            
            // butDelete 需要 ConfirmInfo
            if (config.getName().equals("butDelete")) {
                callActionSet.setAttribute("Action", "OnFrameDelete");
                callActionSet.setAttribute("ConfirmInfo", "DeleteBefore");
            } else {
                callActionSet.setAttribute("Action", "OnFrameRestore");
            }
            
            callAction.appendChild(callActionSet);
            xitem.appendChild(callAction);
        }

        // OpenFrame 节点
        org.w3c.dom.Element openFrame = doc.createElement("OpenFrame");
        xitem.appendChild(openFrame);

        // Frame 节点
        org.w3c.dom.Element frame = doc.createElement("Frame");
        xitem.appendChild(frame);

        // UserControl 节点
        org.w3c.dom.Element userControl = doc.createElement("UserControl");
        xitem.appendChild(userControl);

        // DefineFrame 节点
        org.w3c.dom.Element defineFrame = doc.createElement("DefineFrame");
        xitem.appendChild(defineFrame);

        // PopFrame 节点
        org.w3c.dom.Element popFrame = doc.createElement("PopFrame");
        xitem.appendChild(popFrame);

        // signature 节点
        org.w3c.dom.Element signature = doc.createElement("signature");
        xitem.appendChild(signature);

        // Upload 节点
        org.w3c.dom.Element upload = doc.createElement("Upload");
        xitem.appendChild(upload);

        // VaildEx 节点
        org.w3c.dom.Element vaildEx = doc.createElement("VaildEx");
        xitem.appendChild(vaildEx);

        // MGAddField 节点
        org.w3c.dom.Element mgAddField = doc.createElement("MGAddField");
        xitem.appendChild(mgAddField);

        // AnchorParas 节点
        org.w3c.dom.Element anchorParas = doc.createElement("AnchorParas");
        xitem.appendChild(anchorParas);

        // LinkButtonParas 节点
        org.w3c.dom.Element linkButtonParas = doc.createElement("LinkButtonParas");
        xitem.appendChild(linkButtonParas);

        // DopListShow 节点
        org.w3c.dom.Element dopListShow = doc.createElement("DopListShow");
        xitem.appendChild(dopListShow);

        // ReportCfg 节点
        org.w3c.dom.Element reportCfg = doc.createElement("ReportCfg");
        xitem.appendChild(reportCfg);

        // CombineFrame 节点
        org.w3c.dom.Element combineFrame = doc.createElement("CombineFrame");
        xitem.appendChild(combineFrame);

        // AddrMapRels 节点
        org.w3c.dom.Element addrMapRels = doc.createElement("AddrMapRels");
        xitem.appendChild(addrMapRels);

        // QRCode 节点
        org.w3c.dom.Element qrCode = doc.createElement("QRCode");
        xitem.appendChild(qrCode);

        // ImageDefault 节点
        org.w3c.dom.Element imageDefault = doc.createElement("ImageDefault");
        xitem.appendChild(imageDefault);

        return xitem;
    }
    
    /**
     * 应用 Para 配置到节点
     */
    private void applyParaConfig(Document doc, org.w3c.dom.Element parent, EwaDefineConfig.ParaConfig paraConfig) {
        String xmlPath = paraConfig.getXmlPath();
        String name = paraConfig.getName();
        String val = paraConfig.getVal();

        // 解析 XmlPath，例如 "EventSet/Set"
        String[] parts = xmlPath.split("/");
        org.w3c.dom.Element current = parent;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            // 查找或创建节点
            org.w3c.dom.Element child = findChildElement(current, part);
            if (child == null) {
                child = doc.createElement(part);
                current.appendChild(child);
            }
            current = child;
        }

        // 设置属性 - 处理 JavaScript 表达式
        // EwaDefine.xml 中的 &quot; 和 '+' 是用于在 XML 属性中存储引号的技巧
        // 例如：'EWA.F.FOS[&quot;@'+'sys_frame_unid&quot;]' 应该转换为 EWA.F.FOS["@sys_frame_unid"]
        if (name != null && !name.isEmpty()) {
            current.setAttribute(name, processJsExpression(val));
        }
    }
    
    /**
     * 处理 JavaScript 表达式
     * 将 XML 中的转义形式转换为实际的 JavaScript 表达式
     * 例如：'EWA.F.FOS[&quot;@'+'sys_frame_unid&quot;]' → EWA.F.FOS["@sys_frame_unid"]
     */
    private String processJsExpression(String val) {
        if (val == null) return "";
        
        // 移除字符串连接的 '+' 符号，合并被分割的字符串
        // 例如：'EWA.F.FOS[&quot;@'+'sys_frame_unid&quot;]' → 'EWA.F.FOS[&quot;@sys_frame_unid&quot;]'
        String result = val.replace("'+'", "");
        
        // 转义 XML 实体
        result = result.replace("&quot;", "\"")
                      .replace("&lt;", "<")
                      .replace("&gt;", ">")
                      .replace("&amp;", "&");
        
        // 去除前后的单引号 '
        if (result.startsWith("'") && result.endsWith("'")) {
            result = result.substring(1, result.length() - 1);
        }
        
        return result;
    }
    
    /**
     * 查找子元素
     */
    private org.w3c.dom.Element findChildElement(org.w3c.dom.Element parent, String tagName) {
        org.w3c.dom.NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                org.w3c.dom.Element elem = (org.w3c.dom.Element) children.item(i);
                if (elem.getTagName().equals(tagName)) {
                    return elem;
                }
            }
        }
        return null;
    }
    
    /**
     * 转义 XML 属性值
     */
    private String unescapeXml(String val) {
        if (val == null) return "";
        return val.replace("&quot;", "\"")
                  .replace("&lt;", "<")
                  .replace("&gt;", ">")
                  .replace("&amp;", "&");
    }
    
    /**
     * 根据配置添加 AddScript 和 AddCss
     */
    private void addFromConfig(Document doc, EwaDefineConfig.TmpConfig tmpConfig) {
        for (EwaDefineConfig.AddConfig addConfig : tmpConfig.getAddConfigs()) {
            String xmlPath = addConfig.getXmlPath();
            String content = addConfig.getContent();
            String setMethod = addConfig.getSetMethod();

            // 解析 XmlPath，例如 "EasyWebTemplate/Page/AddScript/Set/Bottom"
            String[] parts = xmlPath.split("/");

            // 从根节点开始（跳过 EasyWebTemplate，因为已经是根了）
            org.w3c.dom.Element current = doc.getDocumentElement();
            
            // 从第 2 个部分开始查找（跳过 EasyWebTemplate）
            for (int i = 1; i < parts.length; i++) {
                String part = parts[i];
                org.w3c.dom.Element child = findChildElement(current, part);
                if (child == null) {
                    child = doc.createElement(part);
                    current.appendChild(child);
                }
                current = child;
            }

            // 设置内容
            if ("CDATA".equals(setMethod)) {
                org.w3c.dom.CDATASection cdata = doc.createCDATASection(content);
                current.appendChild(cdata);
            } else {
                current.setTextContent(content);
            }
        }
    }
    
    /**
     * 创建 DescriptionSet
     */
    private org.w3c.dom.Element createDescriptionSet(Document doc, java.util.Map<String, String> descMap) {
        org.w3c.dom.Element descSet = doc.createElement("DescriptionSet");
        for (java.util.Map.Entry<String, String> entry : descMap.entrySet()) {
            org.w3c.dom.Element set = doc.createElement("Set");
            set.setAttribute("Lang", entry.getKey());
            set.setAttribute("Info", entry.getValue());
            set.setAttribute("Memo", "");
            descSet.appendChild(set);
        }
        return descSet;
    }
    
    /**
     * 创建 Workflows 节点
     */
    private void createWorkflows(Document doc, org.w3c.dom.Element root) {
        org.w3c.dom.Element workflows = doc.createElement("Workflows");
        root.appendChild(workflows);
    }
    
    /**
     * 获取 Frame 类型简写
     */
    private String getFrameTypeShort(String frameType) {
        switch (frameType.toUpperCase()) {
            case "FRAME": return "F";
            case "LISTFRAME": return "LF";
            case "TREE": return "T";
            default: return frameType;
        }
    }

    /**
     * 保存 XML（使用 IUpdateXml 接口）
     */
    private boolean save(String xmlName, String itemName, String admId) {
        // TODO: 实现保存逻辑
        return true;
    }
}
