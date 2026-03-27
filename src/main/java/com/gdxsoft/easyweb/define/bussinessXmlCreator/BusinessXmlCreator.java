package com.gdxsoft.easyweb.define.bussinessXmlCreator;

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

    // 构造方法
    public BusinessXmlCreator(EwaConfig config, Table table) {
        this.config = config;
        this.table = table;
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
        createPageAction(doc, page, frameType, operationType);
        
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
        
        // 添加确定按钮
        org.w3c.dom.Element butOk = createSubmitButton(doc);
        xitems.appendChild(butOk);
        
        // 创建 Menus 节点
        createMenus(doc, root);
        
        // 创建 Charts 节点
        createCharts(doc, root);
        
        // 创建 PageInfos 节点
        createPageInfos(doc, root);
        
        // 创建 Workflows 节点
        createWorkflows(doc, root);
        
        this.xmlDoc = doc;
        return doc;
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
            
            // 添加 4 个按钮：新增、修改、复制、删除
            org.w3c.dom.Element buttons = doc.createElement("Buttons");
            page.appendChild(buttons);
            
            // 新增按钮
            buttons.appendChild(createButton(doc, "butNew", "新增", "New", "new"));
            // 修改按钮
            buttons.appendChild(createButton(doc, "butModify", "修改", "Modify", "modify"));
            // 复制按钮
            buttons.appendChild(createButton(doc, "butCopy", "复制", "Copy", "copy"));
            // 删除按钮
            buttons.appendChild(createButton(doc, "butDelete", "删除", "Delete", "delete"));
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
        
        // Size 节点
        org.w3c.dom.Element size = doc.createElement("Size");
        page.appendChild(size);
        org.w3c.dom.Element sizeSet = doc.createElement("Set");
        sizeSet.setAttribute("HAlign", "center");
        sizeSet.setAttribute("VAlign", "top");
        sizeSet.setAttribute("Width", "100%");
        size.appendChild(sizeSet);
        
        // AddHtml 节点
        org.w3c.dom.Element addHtml = doc.createElement("AddHtml");
        page.appendChild(addHtml);
        org.w3c.dom.Element addHtmlSet = doc.createElement("Set");
        org.w3c.dom.Element addHtmlTop = doc.createElement("Top");
        addHtmlTop.setTextContent(" ");
        addHtmlSet.appendChild(addHtmlTop);
        org.w3c.dom.Element addHtmlBottom = doc.createElement("Bottom");
        addHtmlSet.appendChild(addHtmlBottom);
        addHtml.appendChild(addHtmlSet);
        
        // AddScript 节点
        org.w3c.dom.Element addScript = doc.createElement("AddScript");
        page.appendChild(addScript);
        org.w3c.dom.Element addScriptSet = doc.createElement("Set");
        org.w3c.dom.Element addScriptTop = doc.createElement("Top");
        addScriptSet.appendChild(addScriptTop);
        org.w3c.dom.Element addScriptBottom = doc.createElement("Bottom");
        addScriptBottom.setTextContent(getDefaultScript(itemName));
        addScriptSet.appendChild(addScriptBottom);
        addScript.appendChild(addScriptSet);
        
        // AddCss 节点
        org.w3c.dom.Element addCss = doc.createElement("AddCss");
        page.appendChild(addCss);
        org.w3c.dom.Element addCssSet = doc.createElement("Set");
        org.w3c.dom.Element addCssContent = doc.createElement("AddCss");
        addCssContent.setTextContent(getDefaultCss());
        addCssSet.appendChild(addCssContent);
        addCss.appendChild(addCssSet);
    }
    
    /**
     * 获取默认 JavaScript 代码
     */
    private String getDefaultScript(String itemName) {
        StringBuilder sb = new StringBuilder();
        sb.append("function init").append(itemName.replace(".", "_")).append("() {\n");
        sb.append("    // 初始化代码\n");
        sb.append("    console.log('").append(itemName).append(" initialized');\n");
        sb.append("}\n");
        sb.append("\n");
        sb.append("// 页面加载完成后执行\n");
        sb.append("if (typeof EWA !== 'undefined' && EWA.F) {\n");
        sb.append("    EWA.F.FOS[\"@SYS_FRAME_UNID\"].LoadedAfter = init").append(itemName.replace(".", "_")).append(";\n");
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * 获取默认 CSS 样式
     */
    private String getDefaultCss() {
        return ".EWA_TD_L { width: 150px; }\n.EWA_TD_M { width: 300px; }";
    }
    
    /**
     * 创建按钮
     */
    private org.w3c.dom.Element createButton(Document doc, String name, String infoZhcn, String infoEnus, String action) {
        org.w3c.dom.Element button = doc.createElement("Button");
        button.setAttribute("Name", name);
        
        org.w3c.dom.Element buttonSet = doc.createElement("Set");
        buttonSet.setAttribute("Action", action);
        buttonSet.setAttribute("ConfirmInfo", "");
        buttonSet.setAttribute("Icon", "");
        buttonSet.setAttribute("IsShow", "1");
        buttonSet.setAttribute("Style", "");
        button.appendChild(buttonSet);
        
        org.w3c.dom.Element descSet = doc.createElement("DescriptionSet");
        org.w3c.dom.Element descZhcn = doc.createElement("Set");
        descZhcn.setAttribute("Info", infoZhcn);
        descZhcn.setAttribute("Lang", "zhcn");
        descZhcn.setAttribute("Memo", "");
        descSet.appendChild(descZhcn);
        
        org.w3c.dom.Element descEnus = doc.createElement("Set");
        descEnus.setAttribute("Info", infoEnus);
        descEnus.setAttribute("Lang", "enus");
        descEnus.setAttribute("Memo", "");
        descSet.appendChild(descEnus);
        
        button.appendChild(descSet);
        return button;
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
        tagSet.setAttribute("Tag", getTagType(field.getDatabaseType()));
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
        orderSearchSet.setAttribute("IsOrder", getOrderValue(field));
        orderSearchSet.setAttribute("IsSearchQuick", "1");
        orderSearchSet.setAttribute("OrderExp", "");
        orderSearchSet.setAttribute("SearchExp", "");
        orderSearchSet.setAttribute("SearchMulti", "2");
        orderSearchSet.setAttribute("SearchSql", getSearchSql(field));
        orderSearchSet.setAttribute("SearchType", getSearchType(field));
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
        if (type.contains("DATE") && !type.contains("TIME")) return "yyyy-MM-dd";
        if (type.contains("TIME") && !type.contains("DATE")) return "HH:mm:ss";
        if (type.contains("DATETIME") || type.contains("TIMESTAMP")) return "yyyy-MM-dd HH:mm:ss";
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
     * 规则：
     * - 数字/日期/时间：IsOrder=1
     * - 字符串：长度<=50 时 IsOrder=1，否则 IsOrder=0
     */
    private String getOrderValue(com.gdxsoft.easyweb.define.database.Field field) {
        String dbType = field.getDatabaseType();
        if (dbType == null) return "0";
        
        String type = dbType.toUpperCase();
        // 数字类型
        if (type.contains("INT") || type.contains("DECIMAL") || type.contains("NUM") || 
            type.contains("DOUBLE") || type.contains("FLOAT") || type.contains("MONEY")) {
            return "1";
        }
        // 日期/时间类型
        if (type.contains("DATE") || type.contains("TIME") || type.contains("TIMESTAMP")) {
            return "1";
        }
        // 字符串类型
        if (type.contains("CHAR") || type.contains("TEXT")) {
            if (field.getMaxlength() <= 50) {
                return "1";
            }
            return "0";
        }
        return "0";
    }
    
    /**
     * 获取 OrderSearch 的 SearchType 值
     * 规则：
     * - 字符串类型：SearchType=txt
     * - 其他类型：SearchType=fix
     */
    private String getSearchType(com.gdxsoft.easyweb.define.database.Field field) {
        String dbType = field.getDatabaseType();
        if (dbType == null) return "fix";
        
        String type = dbType.toUpperCase();
        // 字符串类型
        if (type.contains("CHAR") || type.contains("TEXT")) {
            return "txt";
        }
        return "fix";
    }
    
    /**
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
            String frameType, String operationType) {
        
        // Action 节点
        org.w3c.dom.Element action = doc.createElement("Action");
        page.appendChild(action);
        
        // ActionSet 节点
        org.w3c.dom.Element actionSet = doc.createElement("ActionSet");
        action.appendChild(actionSet);
        
        // OnPageLoad Action
        org.w3c.dom.Element onLoadAction = doc.createElement("Set");
        onLoadAction.setAttribute("LogMsg", "");
        onLoadAction.setAttribute("Transcation", "");
        onLoadAction.setAttribute("Type", "OnPageLoad");
        actionSet.appendChild(onLoadAction);
        
        org.w3c.dom.Element onLoadCall = doc.createElement("CallSet");
        org.w3c.dom.Element onLoadCallSet = doc.createElement("Set");
        onLoadCallSet.setAttribute("CallIsChk", "");
        onLoadCallSet.setAttribute("CallName", "OnPageLoad SQL");
        onLoadCallSet.setAttribute("CallType", "SqlSet");
        onLoadCallSet.setAttribute("Test", "");
        onLoadCall.appendChild(onLoadCallSet);
        onLoadAction.appendChild(onLoadCall);
        
        // OnPagePost Action (仅修改/新增模式)
        if (operationType.equals("M") || operationType.equals("N") || operationType.equals("NM")) {
            org.w3c.dom.Element onPostAction = doc.createElement("Set");
            onPostAction.setAttribute("LogMsg", "");
            onPostAction.setAttribute("Transcation", "");
            onPostAction.setAttribute("Type", "OnPagePost");
            actionSet.appendChild(onPostAction);
            
            org.w3c.dom.Element onPostCall = doc.createElement("CallSet");
            org.w3c.dom.Element onPostCallSet = doc.createElement("Set");
            onPostCallSet.setAttribute("CallIsChk", "");
            onPostCallSet.setAttribute("CallName", "OnPagePost SQL");
            onPostCallSet.setAttribute("CallType", "SqlSet");
            onPostCallSet.setAttribute("Test", "");
            onPostCall.appendChild(onPostCallSet);
            onPostAction.appendChild(onPostCall);
        }
        
        // SqlSet 节点
        org.w3c.dom.Element sqlSet = doc.createElement("SqlSet");
        action.appendChild(sqlSet);
        
        // OnPageLoad SQL
        org.w3c.dom.Element onLoadSql = doc.createElement("Set");
        onLoadSql.setAttribute("Name", "OnPageLoad SQL");
        onLoadSql.setAttribute("SqlType", "query");
        org.w3c.dom.Element onLoadSqlContent = doc.createElement("Sql");
        onLoadSqlContent.setTextContent("SELECT * FROM " + this.table.getName() + " WHERE 1=2");
        onLoadSql.appendChild(onLoadSqlContent);
        org.w3c.dom.Element onLoadCssSet = doc.createElement("CSSet");
        onLoadSql.appendChild(onLoadCssSet);
        sqlSet.appendChild(onLoadSql);
        
        // OnPagePost SQL (仅修改/新增模式)
        if (operationType.equals("M") || operationType.equals("N") || operationType.equals("NM")) {
            org.w3c.dom.Element onPostSql = doc.createElement("Set");
            onPostSql.setAttribute("Name", "OnPagePost SQL");
            onPostSql.setAttribute("SqlType", "update");
            org.w3c.dom.Element onPostSqlContent = doc.createElement("Sql");
            onPostSqlContent.setTextContent("-- UPDATE " + this.table.getName() + " SET ... WHERE ...");
            onPostSql.appendChild(onPostSqlContent);
            org.w3c.dom.Element onPostCssSet = doc.createElement("CSSet");
            onPostSql.appendChild(onPostCssSet);
            sqlSet.appendChild(onPostSql);
        }
        
        // JSONSet 节点
        org.w3c.dom.Element jsonSet = doc.createElement("JSONSet");
        action.appendChild(jsonSet);
        
        // ClassSet 节点
        org.w3c.dom.Element classSet = doc.createElement("ClassSet");
        action.appendChild(classSet);
        
        // XmlSet 节点
        org.w3c.dom.Element xmlSet = doc.createElement("XmlSet");
        action.appendChild(xmlSet);
        
        // XmlSetData 节点
        org.w3c.dom.Element xmlSetData = doc.createElement("XmlSetData");
        action.appendChild(xmlSetData);
        
        // ScriptSet 节点
        org.w3c.dom.Element scriptSet = doc.createElement("ScriptSet");
        action.appendChild(scriptSet);
        
        // UrlSet 节点
        org.w3c.dom.Element urlSet = doc.createElement("UrlSet");
        action.appendChild(urlSet);
        
        // CSSet 节点
        org.w3c.dom.Element csSet = doc.createElement("CSSet");
        action.appendChild(csSet);
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
    private void createPageInfos(Document doc, org.w3c.dom.Element root) {
        org.w3c.dom.Element pageInfos = doc.createElement("PageInfos");
        root.appendChild(pageInfos);
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
