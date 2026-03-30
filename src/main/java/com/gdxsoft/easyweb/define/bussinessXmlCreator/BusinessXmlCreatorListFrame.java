package com.gdxsoft.easyweb.define.bussinessXmlCreator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.gdxsoft.easyweb.script.template.EwaConfig;
import com.gdxsoft.easyweb.define.database.Field;
import com.gdxsoft.easyweb.define.database.Table;

/**
 * ListFrame 业务 XML 创建器
 */
public class BusinessXmlCreatorListFrame extends BusinessXmlCreatorBase {

    public BusinessXmlCreatorListFrame(EwaConfig config, Table table) {
        super(config, table);
    }

    @Override
    protected Document create(String frameType, String operationType) throws Exception {
        Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();

        // 获取 Tmp 配置
        String tmpName = getTmpNameByOperation(operationType);
        EwaDefineConfig.TmpConfig tmpConfig = defineConfig.getTmpConfig(frameType, tmpName);

        if (tmpConfig == null) {
            LOGGER.warn("未找到配置：{}.{}，使用默认配置", frameType, tmpName);
        }

        // 创建根节点 EasyWebTemplates
        Element rootTemplates = doc.createElement("EasyWebTemplates");
        doc.appendChild(rootTemplates);

        // 创建 EasyWebTemplate 节点
        Element root = doc.createElement("EasyWebTemplate");
        rootTemplates.appendChild(root);

        // 设置根节点属性
        String itemName = this.table.getName() + "." + getFrameTypeShort(frameType) + "." + operationType;
        root.setAttribute("Name", itemName);
        root.setAttribute("Author", "System");
        root.setAttribute("CreateDate", getCurrentDateTime());
        root.setAttribute("UpdateDate", getCurrentDateTime());

        // 创建 Page 节点
        Element page = doc.createElement("Page");
        root.appendChild(page);

        // 创建 Page 的基本配置节点
        createPageBasicConfig(doc, page, frameType, operationType, itemName);

        // 创建 Action 节点
        createPageAction(doc, page, frameType, operationType, tmpConfig);

        // 创建 XItems 节点
        Element xitems = doc.createElement("XItems");
        page.appendChild(xitems);

        // 根据表字段创建 XItem
        for (int i = 0; i < this.table.getFields().getFieldList().size(); i++) {
            String fieldName = this.table.getFields().getFieldList().get(i);
            Field field = this.table.getFields().get(fieldName);

            Element xitem = createXItem(doc, field, frameType, operationType, i);
            // 如果字段应该隐藏，跳过
            if (xitem != null) {
                xitems.appendChild(xitem);
            }
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

    @Override
    protected boolean save(String xmlContent, String xmlName, String itemName, String admId) {
        // TODO: 实现保存逻辑
        LOGGER.info("XML 已生成：{}", xmlName);
        return true;
    }

    /**
     * 根据操作类型获取 Tmp 名称
     */
    private String getTmpNameByOperation(String operationType) {
        if (operationType.equals("N")) return "N";
        if (operationType.equals("M")) return "M";
        if (operationType.equals("V")) return "V";
        if (operationType.equals("NM")) return "NM";
        return "NM";
    }

    /**
     * 创建 Page 的基本配置
     */
    private void createPageBasicConfig(Document doc, Element page,
            String frameType, String operationType, String itemName) {

        // Name 节点
        Element nameNode = doc.createElement("Name");
        page.appendChild(nameNode);
        Element nameSet = doc.createElement("Set");
        nameSet.setAttribute("Name", itemName);
        nameNode.appendChild(nameSet);

        // FrameTag 节点
        Element frameTag = doc.createElement("FrameTag");
        page.appendChild(frameTag);
        Element frameTagSet = doc.createElement("Set");
        frameTagSet.setAttribute("FrameTag", frameType);
        frameTag.appendChild(frameTagSet);

        // ListFrame 模式添加 Tag 配置
        Element tag = doc.createElement("Tag");
        page.appendChild(tag);
        Element tagSet = doc.createElement("Set");
        tagSet.setAttribute("Tag", "span");
        tag.appendChild(tagSet);

        // SkinName 节点
        Element skinName = doc.createElement("SkinName");
        page.appendChild(skinName);
        Element skinNameSet = doc.createElement("Set");
        skinNameSet.setAttribute("IsXhtml", "0");
        skinNameSet.setAttribute("SkinName", "Test1");
        skinName.appendChild(skinNameSet);

        // DataSource 节点
        Element dataSource = doc.createElement("DataSource");
        page.appendChild(dataSource);
        Element dataSourceSet = doc.createElement("Set");
        dataSourceSet.setAttribute("DataSource", "globaltravel");
        dataSource.appendChild(dataSourceSet);

        // AllowJsonExport 节点
        Element allowJsonExport = doc.createElement("AllowJsonExport");
        page.appendChild(allowJsonExport);

        // ConfigMemo 节点
        Element configMemo = doc.createElement("ConfigMemo");
        page.appendChild(configMemo);
        Element configMemoSet = doc.createElement("Set");
        Element configMemoInner = doc.createElement("ConfigMemo");
        configMemoSet.appendChild(configMemoInner);
        configMemo.appendChild(configMemoSet);

        // Cached 节点
        Element cached = doc.createElement("Cached");
        page.appendChild(cached);
        Element cachedSet = doc.createElement("Set");
        cachedSet.setAttribute("CachedSeconds", "");
        cachedSet.setAttribute("CachedType", "");
        cached.appendChild(cachedSet);

        // Acl 节点
        Element acl = doc.createElement("Acl");
        page.appendChild(acl);
        Element aclSet = doc.createElement("Set");
        aclSet.setAttribute("Acl", "com.gdxsoft.web.acl.BusinessImpl");
        acl.appendChild(aclSet);

        // Log 节点
        Element log = doc.createElement("Log");
        page.appendChild(log);
        Element logSet = doc.createElement("Set");
        logSet.setAttribute("Log", "com.gdxsoft.web.log.EwaScriptLog");
        log.appendChild(logSet);

        // DescriptionSet 节点
        Element descSet = doc.createElement("DescriptionSet");
        page.appendChild(descSet);
        Element descSetZhcn = doc.createElement("Set");
        descSetZhcn.setAttribute("Info", this.table.getName() + " " + getFrameName(frameType) + " " + getOperationName(operationType));
        descSetZhcn.setAttribute("Lang", "zhcn");
        descSetZhcn.setAttribute("Memo", "");
        descSet.appendChild(descSetZhcn);
        Element descSetEnus = doc.createElement("Set");
        descSetEnus.setAttribute("Info", this.table.getName() + " " + getFrameName(frameType) + " " + getOperationName(operationType));
        descSetEnus.setAttribute("Lang", "enus");
        descSetEnus.setAttribute("Memo", "");
        descSet.appendChild(descSetEnus);

        // PageAttributeSet 节点
        Element pageAttributeSet = doc.createElement("PageAttributeSet");
        page.appendChild(pageAttributeSet);

        // RowAttributeSet 节点
        Element rowAttributeSet = doc.createElement("RowAttributeSet");
        page.appendChild(rowAttributeSet);

        // GroupSet 节点
        Element groupSet = doc.createElement("GroupSet");
        page.appendChild(groupSet);

        // Size 节点
        Element size = doc.createElement("Size");
        page.appendChild(size);
        Element sizeSet = doc.createElement("Set");
        sizeSet.setAttribute("FrameCols", "");
        sizeSet.setAttribute("HAlign", "center");
        sizeSet.setAttribute("Height", "");
        sizeSet.setAttribute("HiddenCaption", "");
        sizeSet.setAttribute("TextareaAuto", "");
        sizeSet.setAttribute("VAlign", "top");
        sizeSet.setAttribute("Width", "100%");
        size.appendChild(sizeSet);

        // AddHtml 节点
        Element addHtml = doc.createElement("AddHtml");
        page.appendChild(addHtml);
        Element addHtmlSet = doc.createElement("Set");
        Element addHtmlTop = doc.createElement("Top");
        addHtmlSet.appendChild(addHtmlTop);
        Element addHtmlBottom = doc.createElement("Bottom");
        addHtmlSet.appendChild(addHtmlBottom);
        addHtml.appendChild(addHtmlSet);

        // AddScript 节点
        Element addScript = doc.createElement("AddScript");
        page.appendChild(addScript);
        Element addScriptSet = doc.createElement("Set");
        Element addScriptTop = doc.createElement("Top");
        addScriptSet.appendChild(addScriptTop);
        Element addScriptBottom = doc.createElement("Bottom");
        addScriptSet.appendChild(addScriptBottom);
        addScript.appendChild(addScriptSet);

        // AddCss 节点
        Element addCss = doc.createElement("AddCss");
        page.appendChild(addCss);
        Element addCssSet = doc.createElement("Set");
        Element addCssContent = doc.createElement("AddCss");
        addCssSet.appendChild(addCssContent);
        addCss.appendChild(addCssSet);

        // ChartsShow 节点
        Element chartsShow = doc.createElement("ChartsShow");
        page.appendChild(chartsShow);

        // RedrawJson 节点
        Element redrawJson = doc.createElement("RedrawJson");
        page.appendChild(redrawJson);

        // BoxJson 节点
        Element boxJson = doc.createElement("BoxJson");
        page.appendChild(boxJson);

        // LeftJson 节点
        Element leftJson = doc.createElement("LeftJson");
        page.appendChild(leftJson);

        // FrameHtml 节点
        Element frameHtml = doc.createElement("FrameHtml");
        page.appendChild(frameHtml);
        Element frameHtmlSet = doc.createElement("Set");
        Element frameHtmlInner = doc.createElement("FrameHtml");
        frameHtmlSet.appendChild(frameHtmlInner);
        frameHtml.appendChild(frameHtmlSet);

        // PageSize 节点（从配置读取）
        EwaDefineSettings.ListFramePageSettings lfSettings = EwaDefineSettings.getInstance().getListFramePageSettings();
        Element pageSize = doc.createElement("PageSize");
        page.appendChild(pageSize);
        Element pageSizeSet = doc.createElement("Set");
        pageSizeSet.setAttribute("IsSplitPage", lfSettings.getPageSizeIsSplit());
        pageSizeSet.setAttribute("PageSize", lfSettings.getPageSize());
        pageSizeSet.setAttribute("Recycle", lfSettings.getRecycle());
        String keyField = getPrimaryKeyField();
        if (keyField != null && !keyField.isEmpty()) {
            pageSizeSet.setAttribute("KeyField", keyField);
        }
        pageSize.appendChild(pageSizeSet);

        // ListUI 节点（从配置读取）
        Element listUI = doc.createElement("ListUI");
        page.appendChild(listUI);
        Element listUISet = doc.createElement("Set");
        listUISet.setAttribute("luButtons", lfSettings.getLuButtons());
        listUISet.setAttribute("luSearch", lfSettings.getLuSearch());
        listUISet.setAttribute("luSelect", lfSettings.getLuSelect());
        listUI.appendChild(listUISet);

        // MenuShow 节点
        Element menuShow = doc.createElement("MenuShow");
        page.appendChild(menuShow);

        // Menu 节点
        Element menu = doc.createElement("Menu");
        page.appendChild(menu);

        // Tree 节点
        Element tree = doc.createElement("Tree");
        page.appendChild(tree);

        // HtmlFrame 节点
        Element htmlFrame = doc.createElement("HtmlFrame");
        page.appendChild(htmlFrame);

        // TreeIconSet 节点
        Element treeIconSet = doc.createElement("TreeIconSet");
        page.appendChild(treeIconSet);

        // MGAxisX 节点
        Element mgAxisX = doc.createElement("MGAxisX");
        page.appendChild(mgAxisX);

        // MGAxisY 节点
        Element mgAxisY = doc.createElement("MGAxisY");
        page.appendChild(mgAxisY);

        // MGCell 节点
        Element mgCell = doc.createElement("MGCell");
        page.appendChild(mgCell);

        // LogicShow 节点
        Element logicShow = doc.createElement("LogicShow");
        page.appendChild(logicShow);
    }

    /**
     * 创建 XItem 节点
     */
    private Element createXItem(Document doc, Field field,
            String frameType, String operationType, int index) {

        Element xitem = doc.createElement("XItem");
        xitem.setAttribute("Name", field.getName());

        // 检查字段是否应该隐藏
        if (shouldHideField(field)) {
            return null;
        }

        // Tag 节点
        Element tag = doc.createElement("Tag");
        Element tagSet = doc.createElement("Set");
        tagSet.setAttribute("IsLFEdit", "0");
        tagSet.setAttribute("SpanShowAs", "");
        tagSet.setAttribute("Tag", "span");
        tag.appendChild(tagSet);
        xitem.appendChild(tag);

        // Name 节点
        Element name = doc.createElement("Name");
        Element nameSet = doc.createElement("Set");
        nameSet.setAttribute("Name", field.getName());
        name.appendChild(nameSet);
        xitem.appendChild(name);

        // GroupIndex 节点
        Element groupIndex = doc.createElement("GroupIndex");
        Element groupIndexSet = doc.createElement("Set");
        groupIndexSet.setAttribute("GroupIndex", "0");
        groupIndex.appendChild(groupIndexSet);
        xitem.appendChild(groupIndex);

        // InitValue 节点
        Element initValue = doc.createElement("InitValue");
        Element initValueSet = doc.createElement("Set");
        initValueSet.setAttribute("InitValue", "");
        initValue.appendChild(initValueSet);
        xitem.appendChild(initValue);

        // DescriptionSet 节点
        Element descSet = doc.createElement("DescriptionSet");
        Element descSetZhcn = doc.createElement("Set");
        descSetZhcn.setAttribute("Info", field.getDescription() != null ? field.getDescription() : field.getName());
        descSetZhcn.setAttribute("Lang", "zhcn");
        descSetZhcn.setAttribute("Memo", "");
        descSet.appendChild(descSetZhcn);
        Element descSetEnus = doc.createElement("Set");
        descSetEnus.setAttribute("Info", "");
        descSetEnus.setAttribute("Lang", "enus");
        descSetEnus.setAttribute("Memo", "");
        descSet.appendChild(descSetEnus);
        xitem.appendChild(descSet);

        // XStyle 节点
        Element xstyle = doc.createElement("XStyle");
        xitem.appendChild(xstyle);

        // Style 节点
        Element style = doc.createElement("Style");
        Element styleSet = doc.createElement("Set");
        styleSet.setAttribute("Style", "");
        style.appendChild(styleSet);
        xitem.appendChild(style);

        // ParentStyle 节点
        Element parentStyle = doc.createElement("ParentStyle");
        Element parentStyleSet = doc.createElement("Set");
        parentStyleSet.setAttribute("ParentStyle", "");
        parentStyle.appendChild(parentStyleSet);
        xitem.appendChild(parentStyle);

        // AttributeSet 节点
        Element attributeSet = doc.createElement("AttributeSet");
        Element attributeSetItem = doc.createElement("Set");
        attributeSetItem.setAttribute("AttLogic", "");
        attributeSetItem.setAttribute("AttName", "");
        attributeSetItem.setAttribute("AttValue", "");
        attributeSet.appendChild(attributeSetItem);
        xitem.appendChild(attributeSet);

        // EventSet 节点
        Element eventSet = doc.createElement("EventSet");
        Element eventSetItem = doc.createElement("Set");
        eventSetItem.setAttribute("EventLogic", "");
        eventSetItem.setAttribute("EventName", "");
        eventSetItem.setAttribute("EventType", "");
        eventSetItem.setAttribute("EventValue", "");
        eventSet.appendChild(eventSetItem);
        xitem.appendChild(eventSet);

        // IsHtml 节点
        Element isHtml = doc.createElement("IsHtml");
        xitem.appendChild(isHtml);

        // OrderSearch 节点
        Element orderSearch = doc.createElement("OrderSearch");
        Element orderSearchSet = doc.createElement("Set");
        orderSearchSet.setAttribute("GroupTestLength", "");
        orderSearchSet.setAttribute("IsGroup", "");
        orderSearchSet.setAttribute("IsGroupDefault", "");

        // 根据字段类型设置 Order 和 SearchType
        String fieldType = field.getDatabaseType().toUpperCase();
        String order = "0";
        String searchType = "";

        if (fieldType.contains("INT") || fieldType.contains("DECIMAL") ||
            fieldType.contains("NUM") || fieldType.contains("DOUBLE") ||
            fieldType.contains("FLOAT") || fieldType.contains("MONEY")) {
            order = "1";
            searchType = "";
        } else if (fieldType.contains("DATE") || fieldType.contains("TIME")) {
            order = "1";
            searchType = "";
        } else if (fieldType.contains("CHAR") || fieldType.contains("TEXT")) {
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
        Element maxMinLength = doc.createElement("MaxMinLength");
        Element maxMinLengthSet = doc.createElement("Set");
        maxMinLengthSet.setAttribute("MaxLength", String.valueOf(field.getMaxlength()));
        maxMinLengthSet.setAttribute("MinLength", "");
        maxMinLength.appendChild(maxMinLengthSet);
        xitem.appendChild(maxMinLength);

        // MaxMinValue 节点
        Element maxMinValue = doc.createElement("MaxMinValue");
        Element maxMinValueSet = doc.createElement("Set");
        maxMinValueSet.setAttribute("MaxValue", "");
        maxMinValueSet.setAttribute("MinValue", "");
        maxMinValue.appendChild(maxMinValueSet);
        xitem.appendChild(maxMinValue);

        // IsMustInput 节点
        Element isMustInput = doc.createElement("IsMustInput");
        Element isMustInputSet = doc.createElement("Set");
        isMustInputSet.setAttribute("IsMustInput", field.isNull() ? "0" : "1");
        isMustInput.appendChild(isMustInputSet);
        xitem.appendChild(isMustInput);

        // Switch 节点
        Element switchNode = doc.createElement("Switch");
        xitem.appendChild(switchNode);

        // DataItem 节点
        Element dataItem = doc.createElement("DataItem");
        Element dataItemSet = doc.createElement("Set");
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
        dataItemSet.setAttribute("TriggerValid", "");
        dataItemSet.setAttribute("Trim", "");
        dataItemSet.setAttribute("Valid", getValidType(field.getDatabaseType()));
        dataItem.appendChild(dataItemSet);
        xitem.appendChild(dataItem);

        // DispEnc 节点
        Element dispEnc = doc.createElement("DispEnc");
        xitem.appendChild(dispEnc);

        // DataRef 节点
        Element dataRef = doc.createElement("DataRef");
        xitem.appendChild(dataRef);

        // List 节点
        Element list = doc.createElement("List");
        xitem.appendChild(list);

        // UserSet 节点
        Element userSet = doc.createElement("UserSet");
        xitem.appendChild(userSet);

        // CallAction 节点
        Element callAction = doc.createElement("CallAction");
        xitem.appendChild(callAction);

        // OpenFrame 节点
        Element openFrame = doc.createElement("OpenFrame");
        xitem.appendChild(openFrame);

        // Frame 节点
        Element frame = doc.createElement("Frame");
        xitem.appendChild(frame);

        // UserControl 节点
        Element userControl = doc.createElement("UserControl");
        xitem.appendChild(userControl);

        // DefineFrame 节点
        Element defineFrame = doc.createElement("DefineFrame");
        xitem.appendChild(defineFrame);

        // PopFrame 节点
        Element popFrame = doc.createElement("PopFrame");
        xitem.appendChild(popFrame);

        // signature 节点
        Element signature = doc.createElement("signature");
        xitem.appendChild(signature);

        // Upload 节点
        Element upload = doc.createElement("Upload");
        xitem.appendChild(upload);

        // VaildEx 节点
        Element vaildEx = doc.createElement("VaildEx");
        xitem.appendChild(vaildEx);

        // MGAddField 节点
        Element mgAddField = doc.createElement("MGAddField");
        xitem.appendChild(mgAddField);

        // AnchorParas 节点
        Element anchorParas = doc.createElement("AnchorParas");
        xitem.appendChild(anchorParas);

        // LinkButtonParas 节点
        Element linkButtonParas = doc.createElement("LinkButtonParas");
        xitem.appendChild(linkButtonParas);

        // DopListShow 节点
        Element dopListShow = doc.createElement("DopListShow");
        xitem.appendChild(dopListShow);

        // ReportCfg 节点
        Element reportCfg = doc.createElement("ReportCfg");
        xitem.appendChild(reportCfg);

        // CombineFrame 节点
        Element combineFrame = doc.createElement("CombineFrame");
        xitem.appendChild(combineFrame);

        // AddrMapRels 节点
        Element addrMapRels = doc.createElement("AddrMapRels");
        xitem.appendChild(addrMapRels);

        // QRCode 节点
        Element qrCode = doc.createElement("QRCode");
        xitem.appendChild(qrCode);

        // ImageDefault 节点
        Element imageDefault = doc.createElement("ImageDefault");
        xitem.appendChild(imageDefault);

        return xitem;
    }

    /**
     * 根据配置创建 Buttons（作为 XItem 添加到 XItems）
     */
    private void createButtonsFromConfig(Document doc, Element xitems, EwaDefineConfig.TmpConfig tmpConfig) {
        for (EwaDefineConfig.ButtonConfig btnConfig : tmpConfig.getButtonConfigs()) {
            xitems.appendChild(createButtonAsXItem(doc, btnConfig));
        }
    }

    /**
     * 将 Button 作为 XItem 创建
     */
    private Element createButtonAsXItem(Document doc, EwaDefineConfig.ButtonConfig config) {
        Element xitem = doc.createElement("XItem");
        xitem.setAttribute("Name", config.getName());

        // Tag 节点
        Element tag = doc.createElement("Tag");
        Element tagSet = doc.createElement("Set");
        if (config.getTag() != null && !config.getTag().isEmpty()) {
            tagSet.setAttribute("Tag", config.getTag());
        }
        tagSet.setAttribute("IsLFEdit", "0");
        tagSet.setAttribute("SpanShowAs", "");
        tag.appendChild(tagSet);
        xitem.appendChild(tag);

        // Name 节点
        Element name = doc.createElement("Name");
        Element nameSet = doc.createElement("Set");
        nameSet.setAttribute("Name", config.getName());
        name.appendChild(nameSet);
        xitem.appendChild(name);

        // GroupIndex 节点
        Element groupIndex = doc.createElement("GroupIndex");
        xitem.appendChild(groupIndex);

        // InitValue 节点
        Element initValue = doc.createElement("InitValue");
        xitem.appendChild(initValue);

        // DescriptionSet 节点
        if (config.getDescriptionSet() != null) {
            xitem.appendChild(createDescriptionSet(doc, config.getDescriptionSet()));
        }

        // XStyle 节点
        Element xstyle = doc.createElement("XStyle");
        xitem.appendChild(xstyle);

        // Style 节点
        Element style = doc.createElement("Style");
        Element styleSet = doc.createElement("Set");
        styleSet.setAttribute("Style", "");
        style.appendChild(styleSet);
        xitem.appendChild(style);

        // ParentStyle 节点
        Element parentStyle = doc.createElement("ParentStyle");
        Element parentStyleSet = doc.createElement("Set");
        parentStyleSet.setAttribute("ParentStyle", "");
        parentStyle.appendChild(parentStyleSet);
        xitem.appendChild(parentStyle);

        // AttributeSet 节点
        Element attributeSet = doc.createElement("AttributeSet");
        Element attributeSetItem = doc.createElement("Set");
        attributeSetItem.setAttribute("AttLogic", "");
        attributeSetItem.setAttribute("AttName", "");
        attributeSetItem.setAttribute("AttValue", "");
        attributeSet.appendChild(attributeSetItem);
        xitem.appendChild(attributeSet);

        // EventSet 节点
        Element eventSet = doc.createElement("EventSet");
        xitem.appendChild(eventSet);

        // IsHtml 节点
        Element isHtml = doc.createElement("IsHtml");
        xitem.appendChild(isHtml);

        // OrderSearch 节点
        Element orderSearch = doc.createElement("OrderSearch");
        xitem.appendChild(orderSearch);

        // MaxMinLength 节点
        Element maxMinLength = doc.createElement("MaxMinLength");
        xitem.appendChild(maxMinLength);

        // MaxMinValue 节点
        Element maxMinValue = doc.createElement("MaxMinValue");
        xitem.appendChild(maxMinValue);

        // IsMustInput 节点
        Element isMustInput = doc.createElement("IsMustInput");
        xitem.appendChild(isMustInput);

        // Switch 节点
        Element switchElem = doc.createElement("Switch");
        xitem.appendChild(switchElem);

        // DataItem 节点
        Element dataItem = doc.createElement("DataItem");
        xitem.appendChild(dataItem);

        // DispEnc 节点
        Element dispEnc = doc.createElement("DispEnc");
        xitem.appendChild(dispEnc);

        // DataRef 节点
        Element dataRef = doc.createElement("DataRef");
        xitem.appendChild(dataRef);

        // List 节点
        Element list = doc.createElement("List");
        xitem.appendChild(list);

        // UserSet 节点
        Element userSet = doc.createElement("UserSet");
        xitem.appendChild(userSet);

        // 处理 Para 配置 (EventSet, CallAction 等)
        for (EwaDefineConfig.ParaConfig paraConfig : config.getParaConfigs()) {
            applyParaConfig(doc, xitem, paraConfig);
        }

        // CallAction 节点（butDelete 和 butRestore）
        if (config.getName().equals("butDelete") || config.getName().equals("butRestore")) {
            Element callAction = doc.createElement("CallAction");
            Element callActionSet = doc.createElement("Set");

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
        Element openFrame = doc.createElement("OpenFrame");
        xitem.appendChild(openFrame);

        // Frame 节点
        Element frame = doc.createElement("Frame");
        xitem.appendChild(frame);

        // UserControl 节点
        Element userControl = doc.createElement("UserControl");
        xitem.appendChild(userControl);

        // DefineFrame 节点
        Element defineFrame = doc.createElement("DefineFrame");
        xitem.appendChild(defineFrame);

        // PopFrame 节点
        Element popFrame = doc.createElement("PopFrame");
        xitem.appendChild(popFrame);

        // signature 节点
        Element signature = doc.createElement("signature");
        xitem.appendChild(signature);

        // Upload 节点
        Element upload = doc.createElement("Upload");
        xitem.appendChild(upload);

        // VaildEx 节点
        Element vaildEx = doc.createElement("VaildEx");
        xitem.appendChild(vaildEx);

        // MGAddField 节点
        Element mgAddField = doc.createElement("MGAddField");
        xitem.appendChild(mgAddField);

        // AnchorParas 节点
        Element anchorParas = doc.createElement("AnchorParas");
        xitem.appendChild(anchorParas);

        // LinkButtonParas 节点
        Element linkButtonParas = doc.createElement("LinkButtonParas");
        xitem.appendChild(linkButtonParas);

        // DopListShow 节点
        Element dopListShow = doc.createElement("DopListShow");
        xitem.appendChild(dopListShow);

        // ReportCfg 节点
        Element reportCfg = doc.createElement("ReportCfg");
        xitem.appendChild(reportCfg);

        // CombineFrame 节点
        Element combineFrame = doc.createElement("CombineFrame");
        xitem.appendChild(combineFrame);

        // AddrMapRels 节点
        Element addrMapRels = doc.createElement("AddrMapRels");
        xitem.appendChild(addrMapRels);

        // QRCode 节点
        Element qrCode = doc.createElement("QRCode");
        xitem.appendChild(qrCode);

        // ImageDefault 节点
        Element imageDefault = doc.createElement("ImageDefault");
        xitem.appendChild(imageDefault);

        return xitem;
    }

    /**
     * 应用 Para 配置到节点
     */
    private void applyParaConfig(Document doc, Element parent, EwaDefineConfig.ParaConfig paraConfig) {
        String xmlPath = paraConfig.getXmlPath();
        String name = paraConfig.getName();
        String val = paraConfig.getVal();

        String[] parts = xmlPath.split("/");
        Element current = parent;

        for (String part : parts) {
            Element child = findChildElement(current, part);
            if (child == null) {
                child = doc.createElement(part);
                current.appendChild(child);
            }
            current = child;
        }

        if (name != null && !name.isEmpty()) {
            current.setAttribute(name, processJsExpression(val));
        }
    }

    /**
     * 根据配置添加 AddScript 和 AddCss
     */
    private void addFromConfig(Document doc, EwaDefineConfig.TmpConfig tmpConfig) {
        for (EwaDefineConfig.AddConfig addConfig : tmpConfig.getAddConfigs()) {
            String xmlPath = addConfig.getXmlPath();
            String content = addConfig.getContent();
            String setMethod = addConfig.getSetMethod();

            // 替换占位符 {@define.Fields.TableName} 为实际表名
            if (content != null) {
                content = content.replace("{@define.Fields.TableName}", this.table.getName());
            }

            String[] parts = xmlPath.split("/");
            Element current = doc.getDocumentElement();

            for (String part : parts) {
                Element child = findChildElement(current, part);
                if (child == null) {
                    child = doc.createElement(part);
                    current.appendChild(child);
                }
                current = child;
            }

            if ("CDATA".equals(setMethod)) {
                while (current.hasChildNodes()) {
                    current.removeChild(current.getFirstChild());
                }
                org.w3c.dom.CDATASection cdata = doc.createCDATASection(content);
                current.appendChild(cdata);
            } else {
                current.setTextContent(content);
            }
        }
    }

    /**
     * 创建 Page 的 Action 配置
     */
    private void createPageAction(Document doc, Element page,
            String frameType, String operationType, EwaDefineConfig.TmpConfig tmpConfig) {

        Element action = doc.createElement("Action");
        page.appendChild(action);

        if (tmpConfig != null && !tmpConfig.getActionConfigs().isEmpty()) {
            Element actionSet = doc.createElement("ActionSet");
            action.appendChild(actionSet);

            for (EwaDefineConfig.ActionConfig actConfig : tmpConfig.getActionConfigs()) {
                actionSet.appendChild(createActionSetItem(doc, actConfig));
            }

            Element sqlSet = doc.createElement("SqlSet");
            action.appendChild(sqlSet);

            for (EwaDefineConfig.ActionConfig actConfig : tmpConfig.getActionConfigs()) {
                if (actConfig.getSqlType() != null) {
                    sqlSet.appendChild(createSqlSetItem(doc, actConfig));
                }
            }
        } else {
            createDefaultPageAction(doc, action, frameType, operationType);
        }

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
    private Element createActionSetItem(Document doc, EwaDefineConfig.ActionConfig config) {
        Element set = doc.createElement("Set");
        set.setAttribute("LogMsg", "");
        set.setAttribute("Transcation", "");
        set.setAttribute("Type", config.getName());

        Element callSet = doc.createElement("CallSet");
        Element callSetItem = doc.createElement("Set");
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
    private Element createSqlSetItem(Document doc, EwaDefineConfig.ActionConfig config) {
        Element set = doc.createElement("Set");
        set.setAttribute("Name", config.getName() + " SQL");
        set.setAttribute("SqlType", config.getSqlType());

        Element sql = doc.createElement("Sql");
        String sqlContent = config.getSql();
        if (sqlContent != null) {
            if (sqlContent.startsWith("'") && sqlContent.endsWith("'")) {
                sqlContent = sqlContent.substring(1, sqlContent.length() - 1);
            }
            sqlContent = parseFieldsMethod(sqlContent);
        }
        sql.setTextContent(sqlContent != null ? sqlContent : "");
        set.appendChild(sql);
        set.appendChild(doc.createElement("CSSet"));

        return set;
    }

    /**
     * 解析 Fields 方法调用
     */
    private String parseFieldsMethod(String sqlContent) {
        if (sqlContent == null) return null;

        String statusField = findStatusField();

        if (sqlContent.contains("this.Fields.GetSqlSelectLF()")) {
            return getSqlSelectLF(statusField, true);
        } else if (sqlContent.contains("this.Fields.GetSqlSelect()")) {
            return getSqlSelect();
        } else if (sqlContent.contains("this.Fields.GetSqlDeleteA()")) {
            return getSqlDeleteA(statusField);
        } else if (sqlContent.contains("this.Fields.GetSqlRestore()")) {
            return getSqlRestore(statusField);
        } else if (sqlContent.contains("this.Fields.GetSqlDelete()")) {
            return getSqlDelete();
        } else if (sqlContent.contains("this.Fields.GetSqlUpdate()")) {
            return getSqlUpdate(statusField);
        } else if (sqlContent.contains("this.Fields.GetSqlNew()")) {
            return getSqlNew(statusField);
        }

        return sqlContent;
    }

    /**
     * 创建默认 Page Action
     */
    private void createDefaultPageAction(Document doc, Element action,
            String frameType, String operationType) {

        Element actionSet = doc.createElement("ActionSet");
        action.appendChild(actionSet);

        Element onLoadAction = doc.createElement("Set");
        onLoadAction.setAttribute("Type", "OnPageLoad");
        Element onLoadCall = doc.createElement("CallSet");
        Element onLoadCallSet = doc.createElement("Set");
        onLoadCallSet.setAttribute("CallName", "OnPageLoad SQL");
        onLoadCallSet.setAttribute("CallType", "SqlSet");
        onLoadCall.appendChild(onLoadCallSet);
        onLoadAction.appendChild(onLoadCall);
        actionSet.appendChild(onLoadAction);

        Element sqlSet = doc.createElement("SqlSet");
        action.appendChild(sqlSet);

        String statusField = getStatusField();

        Element onLoadSql = doc.createElement("Set");
        onLoadSql.setAttribute("Name", "OnPageLoad SQL");
        onLoadSql.setAttribute("SqlType", "query");
        Element onLoadSqlContent = doc.createElement("Sql");
        onLoadSqlContent.setTextContent(getSqlSelectLF(statusField, true));
        onLoadSql.appendChild(onLoadSqlContent);
        onLoadSql.appendChild(doc.createElement("CSSet"));
        sqlSet.appendChild(onLoadSql);

        if (operationType.equals("M") || operationType.equals("NM")) {
            Element onDeleteSql = doc.createElement("Set");
            onDeleteSql.setAttribute("Name", "OnFrameDelete SQL");
            onDeleteSql.setAttribute("SqlType", "update");
            Element onDeleteSqlContent = doc.createElement("Sql");
            onDeleteSqlContent.setTextContent(getSqlDeleteA(statusField));
            onDeleteSql.appendChild(onDeleteSqlContent);
            onDeleteSql.appendChild(doc.createElement("CSSet"));
            sqlSet.appendChild(onDeleteSql);

            Element onRestoreSql = doc.createElement("Set");
            onRestoreSql.setAttribute("Name", "OnFrameRestore SQL");
            onRestoreSql.setAttribute("SqlType", "update");
            Element onRestoreSqlContent = doc.createElement("Sql");
            onRestoreSqlContent.setTextContent(getSqlRestore(statusField));
            onRestoreSql.appendChild(onRestoreSqlContent);
            onRestoreSql.appendChild(doc.createElement("CSSet"));
            sqlSet.appendChild(onRestoreSql);
        }

        for (int i = 0; i <= 2; i++) {
            Element sActSql = doc.createElement("Set");
            sActSql.setAttribute("Name", "SAct" + i + " SQL");
            sActSql.setAttribute("SqlType", "query");
            Element sActSqlContent = doc.createElement("Sql");
            sActSqlContent.setTextContent("-- enter your sql");
            sActSql.appendChild(sActSqlContent);
            sActSql.appendChild(doc.createElement("CSSet"));
            sqlSet.appendChild(sActSql);
        }

        for (int i = 0; i <= 2; i++) {
            Element uActSql = doc.createElement("Set");
            uActSql.setAttribute("Name", "UAct" + i + " SQL");
            uActSql.setAttribute("SqlType", "update");
            Element uActSqlContent = doc.createElement("Sql");
            uActSqlContent.setTextContent("-- enter your sql");
            uActSql.appendChild(uActSqlContent);
            uActSql.appendChild(doc.createElement("CSSet"));
            sqlSet.appendChild(uActSql);
        }

        Element checkErrorSql = doc.createElement("Set");
        checkErrorSql.setAttribute("Name", "CheckError SQL");
        checkErrorSql.setAttribute("SqlType", "query");
        Element checkErrorSqlContent = doc.createElement("Sql");
        checkErrorSqlContent.setTextContent("-- select 不能执行/javascript_func() as EWA_ERR_OUT FROM xxx where 1=2");
        checkErrorSql.appendChild(checkErrorSqlContent);
        checkErrorSql.appendChild(doc.createElement("CSSet"));
        sqlSet.appendChild(checkErrorSql);
    }

    /**
     * 创建 Menus 节点
     */
    private void createMenus(Document doc, Element root) {
        Element menus = doc.createElement("Menus");
        root.appendChild(menus);
    }

    /**
     * 创建 Charts 节点
     */
    private void createCharts(Document doc, Element root) {
        Element charts = doc.createElement("Charts");
        root.appendChild(charts);
    }

    /**
     * 创建 PageInfos 节点
     */
    private void createPageInfos(Document doc, Element root, EwaDefineConfig.TmpConfig tmpConfig) {
        Element pageInfos = doc.createElement("PageInfos");
        root.appendChild(pageInfos);

        if (tmpConfig != null && !tmpConfig.getPageInfoConfigs().isEmpty()) {
            for (EwaDefineConfig.PageInfoConfig pageInfoConfig : tmpConfig.getPageInfoConfigs()) {
                pageInfos.appendChild(createPageInfoFromConfig(doc, pageInfoConfig));
            }
        } else {
            pageInfos.appendChild(createDefaultPageInfo(doc, "msg0", "消息 0", "Msg0"));
            pageInfos.appendChild(createDefaultPageInfo(doc, "msg1", "消息 1", "Msg1"));
            pageInfos.appendChild(createDefaultPageInfo(doc, "msg2", "消息 2", "Msg2"));
        }
    }

    /**
     * 创建 PageInfo
     */
    private Element createPageInfoFromConfig(Document doc, EwaDefineConfig.PageInfoConfig config) {
        Element pageInfo = doc.createElement("PageInfo");
        pageInfo.setAttribute("Name", config.getName());

        Element name = doc.createElement("Name");
        Element nameSet = doc.createElement("Set");
        nameSet.setAttribute("Name", config.getName());
        name.appendChild(nameSet);
        pageInfo.appendChild(name);

        if (config.getDescriptionSet() != null) {
            pageInfo.appendChild(createDescriptionSet(doc, config.getDescriptionSet()));
        }

        return pageInfo;
    }

    /**
     * 创建默认 PageInfo
     */
    private Element createDefaultPageInfo(Document doc, String name, String infoZhcn, String infoEnus) {
        Element pageInfo = doc.createElement("PageInfo");
        pageInfo.setAttribute("Name", name);

        Element nameElem = doc.createElement("Name");
        Element nameSet = doc.createElement("Set");
        nameSet.setAttribute("Name", name);
        nameElem.appendChild(nameSet);
        pageInfo.appendChild(nameElem);

        java.util.Map<String, String> descMap = new java.util.HashMap<>();
        descMap.put("zhcn", infoZhcn);
        descMap.put("enus", infoEnus);
        pageInfo.appendChild(createDescriptionSet(doc, descMap));

        return pageInfo;
    }

    /**
     * 创建 Workflows 节点
     */
    private void createWorkflows(Document doc, Element root) {
        Element workflows = doc.createElement("Workflows");
        root.appendChild(workflows);
    }
}
