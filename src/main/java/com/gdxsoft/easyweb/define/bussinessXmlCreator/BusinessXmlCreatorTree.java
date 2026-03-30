package com.gdxsoft.easyweb.define.bussinessXmlCreator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.gdxsoft.easyweb.script.template.EwaConfig;
import com.gdxsoft.easyweb.define.database.Table;

/**
 * Tree 业务 XML 创建器
 */
public class BusinessXmlCreatorTree extends BusinessXmlCreatorBase {

    public BusinessXmlCreatorTree(EwaConfig config, Table table) {
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

        // Tree 模式不需要 XItems
        Element xitems = doc.createElement("XItems");
        page.appendChild(xitems);

        // 创建 Menus 节点（Tree 模式需要菜单）
        createMenus(doc, root, frameType);

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
        LOGGER.info("XML 已生成：{}", xmlName);
        return true;
    }

    private String getTmpNameByOperation(String operationType) {
        if (operationType.equals("N")) return "N";
        if (operationType.equals("M")) return "M";
        if (operationType.equals("V")) return "V";
        if (operationType.equals("NM")) return "NM";
        return "M";
    }

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

        // Acl 节点
        Element acl = doc.createElement("Acl");
        page.appendChild(acl);
        Element aclSet = doc.createElement("Set");
        aclSet.setAttribute("Acl", "");
        acl.appendChild(aclSet);

        // Log 节点
        Element log = doc.createElement("Log");
        page.appendChild(log);
        Element logSet = doc.createElement("Set");
        logSet.setAttribute("Log", "");
        log.appendChild(logSet);

        // DescriptionSet 节点
        Element descSet = doc.createElement("DescriptionSet");
        page.appendChild(descSet);
        Element descSetZhcn = doc.createElement("Set");
        descSetZhcn.setAttribute("Info", this.table.getName() + " Tree");
        descSetZhcn.setAttribute("Lang", "zhcn");
        descSetZhcn.setAttribute("Memo", "");
        descSet.appendChild(descSetZhcn);
        Element descSetEnus = doc.createElement("Set");
        descSetEnus.setAttribute("Info", this.table.getName() + " Tree");
        descSetEnus.setAttribute("Lang", "enus");
        descSetEnus.setAttribute("Memo", "");
        descSet.appendChild(descSetEnus);

        // PageAttributeSet 节点
        Element pageAttributeSet = doc.createElement("PageAttributeSet");
        page.appendChild(pageAttributeSet);
        Element pageAttributeSetSet = doc.createElement("Set");
        pageAttributeSetSet.setAttribute("PageAttName", "");
        pageAttributeSetSet.setAttribute("PageAttValue", "");
        pageAttributeSet.appendChild(pageAttributeSetSet);

        // RowAttributeSet 节点
        Element rowAttributeSet = doc.createElement("RowAttributeSet");
        page.appendChild(rowAttributeSet);
        Element rowAttributeSetSet = doc.createElement("Set");
        rowAttributeSetSet.setAttribute("RowAttLogic", "");
        rowAttributeSetSet.setAttribute("RowAttName", "");
        rowAttributeSetSet.setAttribute("RowAttValue", "");
        rowAttributeSet.appendChild(rowAttributeSetSet);

        // GroupSet 节点
        Element groupSet = doc.createElement("GroupSet");
        page.appendChild(groupSet);

        // Size 节点（从配置读取）
        EwaDefineSettings.FramePageSettings settings = EwaDefineSettings.getInstance().getFramePageSettings();
        Element size = doc.createElement("Size");
        page.appendChild(size);
        Element sizeSet = doc.createElement("Set");
        sizeSet.setAttribute("FrameCols", "C2");
        sizeSet.setAttribute("HAlign", settings.getHAlign());
        sizeSet.setAttribute("Height", "");
        sizeSet.setAttribute("HiddenCaption", "0");  // Tree 模式显示标题
        sizeSet.setAttribute("TextareaAuto", "");
        sizeSet.setAttribute("VAlign", settings.getVAlign());
        sizeSet.setAttribute("Width", "100%");  // Tree 模式使用 100% 宽度
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
        // 添加 Tree 默认的 link 函数
        String bottomScript = "EWA.F.FOS[\"@sys_frame_unid\"].link = function (id) {\n" +
            "    const u = new EWA_UrlClass(this.Url);\n" +
            "    u.AddParameter(\"itemname\", \"" + this.table.getName() + ".F.NM\");\n" +
            "    u.AddParameter(\"" + getPrimaryKeyField() + "\", id);\n" +
            "    u.AddParameter(\"EWA_MTYPE\", \"M\");\n" +
            "    $Install(u, 'F1_@sys_frame_unid', function(){});\n" +
            "}";
        org.w3c.dom.CDATASection cdata = doc.createCDATASection(bottomScript);
        addScriptBottom.appendChild(cdata);
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

        // PageSize 节点
        Element pageSize = doc.createElement("PageSize");
        page.appendChild(pageSize);

        // ListUI 节点
        Element listUI = doc.createElement("ListUI");
        page.appendChild(listUI);

        // MenuShow 节点
        Element menuShow = doc.createElement("MenuShow");
        page.appendChild(menuShow);

        // Menu 节点
        Element menu = doc.createElement("Menu");
        page.appendChild(menu);

        // Tree 节点（关键配置）
        Element tree = doc.createElement("Tree");
        page.appendChild(tree);
        Element treeSet = doc.createElement("Set");
        // Tree 配置属性
        treeSet.setAttribute("Text", this.table.getName() + "_NAME");  // 显示文本字段
        treeSet.setAttribute("Title", this.table.getName() + "_NAME_EN");  // 英文标题字段
        treeSet.setAttribute("Key", getPrimaryKeyField());  // 主键字段
        treeSet.setAttribute("ParentKey", this.table.getName() + "_PID");  // 父 ID 字段
        treeSet.setAttribute("Level", this.table.getName() + "_LVL");  // 层级字段
        treeSet.setAttribute("Order", this.table.getName() + "_ORD");  // 排序字段
        treeSet.setAttribute("RootId", "");  // 根 ID
        treeSet.setAttribute("LoadByLevel", "");  // 是否按层级加载
        treeSet.setAttribute("AddPara1", "");
        treeSet.setAttribute("AddPara2", "");
        treeSet.setAttribute("AddPara3", "");
        treeSet.setAttribute("MenuGroup", "");
        treeSet.setAttribute("Filter", "");
        tree.appendChild(treeSet);

        // HtmlFrame 节点（Tree 模式需要）
        Element htmlFrame = doc.createElement("HtmlFrame");
        page.appendChild(htmlFrame);
        Element htmlFrameSet = doc.createElement("Set");
        htmlFrameSet.setAttribute("FrameType", "H5");
        htmlFrameSet.setAttribute("FrameSize", "200,*");
        htmlFrameSet.setAttribute("FrameBorder", "");
        htmlFrameSet.setAttribute("FrameSubUrl", "");
        htmlFrame.appendChild(htmlFrameSet);

        // TreeIconSet 节点
        Element treeIconSet = doc.createElement("TreeIconSet");
        page.appendChild(treeIconSet);
        Element treeIconSetSet = doc.createElement("Set");
        treeIconSetSet.setAttribute("Open", "");
        treeIconSetSet.setAttribute("Close", "");
        treeIconSetSet.setAttribute("Test", "");
        treeIconSetSet.setAttribute("Filter", "");
        treeIconSet.appendChild(treeIconSetSet);

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

    private void createPageAction(Document doc, Element page,
            String frameType, String operationType, EwaDefineConfig.TmpConfig tmpConfig) {

        Element action = doc.createElement("Action");
        page.appendChild(action);

        Element actionSet = doc.createElement("ActionSet");
        action.appendChild(actionSet);

        // OnPageLoad Action
        Element onLoadAction = doc.createElement("Set");
        onLoadAction.setAttribute("Type", "OnPageLoad");
        onLoadAction.setAttribute("LogMsg", "");
        onLoadAction.setAttribute("Transcation", "");
        Element onLoadCallSet = doc.createElement("CallSet");
        Element onLoadCallSetItem = doc.createElement("Set");
        onLoadCallSetItem.setAttribute("CallIsChk", "");
        onLoadCallSetItem.setAttribute("CallName", "OnPageLoad SQL");
        onLoadCallSetItem.setAttribute("CallType", "SqlSet");
        onLoadCallSetItem.setAttribute("Test", "");
        onLoadCallSet.appendChild(onLoadCallSetItem);
        onLoadAction.appendChild(onLoadCallSet);
        actionSet.appendChild(onLoadAction);

        // OnTreeNodeDelete Action
        Element onDeleteAction = doc.createElement("Set");
        onDeleteAction.setAttribute("Type", "OnTreeNodeDelete");
        onDeleteAction.setAttribute("LogMsg", "");
        onDeleteAction.setAttribute("Transcation", "");
        Element onDeleteCallSet = doc.createElement("CallSet");
        Element onDeleteCallSetItem = doc.createElement("Set");
        onDeleteCallSetItem.setAttribute("CallIsChk", "");
        onDeleteCallSetItem.setAttribute("CallName", "OnTreeNodeDelete SQL");
        onDeleteCallSetItem.setAttribute("CallType", "SqlSet");
        onDeleteCallSetItem.setAttribute("Test", "");
        onDeleteCallSet.appendChild(onDeleteCallSetItem);
        onDeleteAction.appendChild(onDeleteCallSet);
        actionSet.appendChild(onDeleteAction);

        // OnTreeNodeRename Action
        Element onRenameAction = doc.createElement("Set");
        onRenameAction.setAttribute("Type", "OnTreeNodeRename");
        onRenameAction.setAttribute("LogMsg", "");
        onRenameAction.setAttribute("Transcation", "");
        Element onRenameCallSet = doc.createElement("CallSet");
        Element onRenameCallSetItem = doc.createElement("Set");
        onRenameCallSetItem.setAttribute("CallIsChk", "");
        onRenameCallSetItem.setAttribute("CallName", "OnTreeNodeRename SQL");
        onRenameCallSetItem.setAttribute("CallType", "SqlSet");
        onRenameCallSetItem.setAttribute("Test", "");
        onRenameCallSet.appendChild(onRenameCallSetItem);
        onRenameAction.appendChild(onRenameCallSet);
        actionSet.appendChild(onRenameAction);

        // OnTreeNodeNew Action
        Element onNewAction = doc.createElement("Set");
        onNewAction.setAttribute("Type", "OnTreeNodeNew");
        onNewAction.setAttribute("LogMsg", "");
        onNewAction.setAttribute("Transcation", "");
        Element onNewCallSet = doc.createElement("CallSet");
        Element onNewCallSetItem = doc.createElement("Set");
        onNewCallSetItem.setAttribute("CallIsChk", "");
        onNewCallSetItem.setAttribute("CallName", "OnTreeNodeNew SQL");
        onNewCallSetItem.setAttribute("CallType", "SqlSet");
        onNewCallSetItem.setAttribute("Test", "");
        onNewCallSet.appendChild(onNewCallSetItem);
        onNewAction.appendChild(onNewCallSet);
        actionSet.appendChild(onNewAction);

        // SqlSet
        Element sqlSet = doc.createElement("SqlSet");
        action.appendChild(sqlSet);

        // OnPageLoad SQL
        Element onLoadSql = doc.createElement("Set");
        onLoadSql.setAttribute("Name", "OnPageLoad SQL");
        onLoadSql.setAttribute("SqlType", "query");
        Element onLoadSqlContent = doc.createElement("Sql");
        String statusField = getStatusField();
        if (statusField != null) {
            onLoadSqlContent.setTextContent("SELECT * FROM " + this.table.getName() + 
                " WHERE " + statusField + " = 'USED' ORDER BY " + 
                this.table.getName() + "_LVL, " + this.table.getName() + "_ORD");
        } else {
            onLoadSqlContent.setTextContent("SELECT * FROM " + this.table.getName() + 
                " ORDER BY " + this.table.getName() + "_LVL, " + this.table.getName() + "_ORD");
        }
        onLoadSql.appendChild(onLoadSqlContent);
        onLoadSql.appendChild(doc.createElement("CSSet"));
        sqlSet.appendChild(onLoadSql);

        // OnTreeNodeDelete SQL
        Element onDeleteSql = doc.createElement("Set");
        onDeleteSql.setAttribute("Name", "OnTreeNodeDelete SQL");
        onDeleteSql.setAttribute("SqlType", "update");
        Element onDeleteSqlContent = doc.createElement("Sql");
        onDeleteSqlContent.setTextContent(" ");
        onDeleteSql.appendChild(onDeleteSqlContent);
        onDeleteSql.appendChild(doc.createElement("CSSet"));
        sqlSet.appendChild(onDeleteSql);

        // OnTreeNodeRename SQL
        Element onRenameSql = doc.createElement("Set");
        onRenameSql.setAttribute("Name", "OnTreeNodeRename SQL");
        onRenameSql.setAttribute("SqlType", "update");
        Element onRenameSqlContent = doc.createElement("Sql");
        onRenameSql.appendChild(onRenameSqlContent);
        onRenameSql.appendChild(doc.createElement("CSSet"));
        sqlSet.appendChild(onRenameSql);

        // OnTreeNodeNew SQL
        Element onNewSql = doc.createElement("Set");
        onNewSql.setAttribute("Name", "OnTreeNodeNew SQL");
        onNewSql.setAttribute("SqlType", "update");
        Element onNewSqlContent = doc.createElement("Sql");
        onNewSql.appendChild(onNewSqlContent);
        onNewSql.appendChild(doc.createElement("CSSet"));
        sqlSet.appendChild(onNewSql);

        // JSONSet
        action.appendChild(doc.createElement("JSONSet"));
        // ClassSet
        action.appendChild(doc.createElement("ClassSet"));
        // XmlSet
        action.appendChild(doc.createElement("XmlSet"));
        // XmlSetData
        action.appendChild(doc.createElement("XmlSetData"));
        // ScriptSet
        action.appendChild(doc.createElement("ScriptSet"));
        // UrlSet
        action.appendChild(doc.createElement("UrlSet"));
        // CSSet
        action.appendChild(doc.createElement("CSSet"));
        // CallSet
        Element callSet = doc.createElement("CallSet");
        Element callSetItem = doc.createElement("Set");
        callSetItem.setAttribute("CallIsChk", "");
        callSetItem.setAttribute("CallName", "OnPageLoad SQL");
        callSetItem.setAttribute("CallType", "SqlSet");
        callSetItem.setAttribute("Test", "");
        callSet.appendChild(callSetItem);
        action.appendChild(callSet);
    }

    private void createMenus(Document doc, Element root, String frameType) {
        Element menus = doc.createElement("Menus");
        root.appendChild(menus);

        // itemNew 菜单
        menus.appendChild(createMenu(doc, "itemNew", "新建", "New", 
            "/EmpScriptV2/EWA_STYLE/images/defined/new.gif", "EWA.CurUI.NewNode();"));

        // itemRename 菜单
        menus.appendChild(createMenu(doc, "itemRename", "修改名称", "Rename", 
            "", "EWA.CurUI.Rename();"));

        // line 分隔符
        menus.appendChild(createMenu(doc, "line", "<hr>", "<hr>", 
            "", ""));

        // itemDelete 菜单
        menus.appendChild(createMenu(doc, "itemDelete", "删除", "Delete", 
            "/EmpScriptV2/EWA_STYLE/images/defined/del.gif", "EWA.CurUI.Delete();"));
    }

    private Element createMenu(Document doc, String name, String infoZhcn, String infoEnus, 
                               String icon, String cmd) {
        Element menu = doc.createElement("Menu");
        menu.setAttribute("Name", name);

        // Name 节点
        Element nameElem = doc.createElement("Name");
        Element nameSet = doc.createElement("Set");
        nameSet.setAttribute("Name", name);
        nameElem.appendChild(nameSet);
        menu.appendChild(nameElem);

        // DescriptionSet 节点
        Element descSet = doc.createElement("DescriptionSet");
        Element descSetZhcn = doc.createElement("Set");
        descSetZhcn.setAttribute("Info", infoZhcn);
        descSetZhcn.setAttribute("Lang", "zhcn");
        descSet.appendChild(descSetZhcn);
        Element descSetEnus = doc.createElement("Set");
        descSetEnus.setAttribute("Info", infoEnus);
        descSetEnus.setAttribute("Lang", "enus");
        descSet.appendChild(descSetEnus);
        menu.appendChild(descSet);

        // Icon 节点
        Element iconElem = doc.createElement("Icon");
        Element iconSet = doc.createElement("Set");
        iconSet.setAttribute("Icon", icon);
        iconElem.appendChild(iconSet);
        menu.appendChild(iconElem);

        // Cmd 节点
        Element cmdElem = doc.createElement("Cmd");
        Element cmdSet = doc.createElement("Set");
        cmdSet.setAttribute("Cmd", cmd);
        cmdElem.appendChild(cmdSet);
        menu.appendChild(cmdElem);

        // Group 节点
        Element groupElem = doc.createElement("Group");
        Element groupSet = doc.createElement("Set");
        groupSet.setAttribute("Group", "");
        groupElem.appendChild(groupSet);
        menu.appendChild(groupElem);

        return menu;
    }

    private void createCharts(Document doc, Element root) {
        Element charts = doc.createElement("Charts");
        root.appendChild(charts);
    }

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

    private void createWorkflows(Document doc, Element root) {
        Element workflows = doc.createElement("Workflows");
        root.appendChild(workflows);
    }
}
