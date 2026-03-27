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
        
        // 设置 Name 属性（后续由 save() 方法设置）
        root.setAttribute("Name", this.table.getName() + "." + getFrameTypeShort(frameType) + "." + operationType);
        
        // 创建 Page 节点
        org.w3c.dom.Element page = doc.createElement("Page");
        root.appendChild(page);
        
        // 创建 Name 节点
        org.w3c.dom.Element nameNode = doc.createElement("Name");
        page.appendChild(nameNode);
        org.w3c.dom.Element nameSet = doc.createElement("Set");
        nameSet.setAttribute("Name", this.table.getName() + "." + getFrameTypeShort(frameType) + "." + operationType);
        nameNode.appendChild(nameSet);
        
        // 创建 XItems 节点
        org.w3c.dom.Element xitems = doc.createElement("XItems");
        page.appendChild(xitems);
        
        // 根据表字段创建 XItem
        for (int i = 0; i < this.table.getFields().getFieldList().size(); i++) {
            String fieldName = this.table.getFields().getFieldList().get(i);
            com.gdxsoft.easyweb.define.database.Field field = this.table.getFields().get(fieldName);
            
            org.w3c.dom.Element xitem = createXItem(doc, field, frameType, operationType);
            xitems.appendChild(xitem);
        }
        
        this.xmlDoc = doc;
        return doc;
    }
    
    /**
     * 创建 XItem 节点
     */
    private org.w3c.dom.Element createXItem(Document doc, 
            com.gdxsoft.easyweb.define.database.Field field, 
            String frameType, String operationType) {
        
        org.w3c.dom.Element xitem = doc.createElement("XItem");
        xitem.setAttribute("Name", field.getName());
        
        // 创建 Tag 节点
        org.w3c.dom.Element tag = doc.createElement("Tag");
        org.w3c.dom.Element tagSet = doc.createElement("Set");
        tagSet.setAttribute("IsLFEdit", "0");
        tag.appendChild(tagSet);
        xitem.appendChild(tag);
        
        // 创建 Name 节点
        org.w3c.dom.Element name = doc.createElement("Name");
        org.w3c.dom.Element nameSet = doc.createElement("Set");
        nameSet.setAttribute("Name", field.getName());
        name.appendChild(nameSet);
        xitem.appendChild(name);
        
        // 创建 DataItem 节点
        org.w3c.dom.Element dataItem = doc.createElement("DataItem");
        org.w3c.dom.Element dataItemSet = doc.createElement("Set");
        dataItemSet.setAttribute("DataField", field.getName());
        dataItemSet.setAttribute("DataType", field.getDatabaseType());
        if (field.getDescription() != null) {
            dataItemSet.setAttribute("DesZHCN", field.getDescription());
        }
        dataItem.appendChild(dataItemSet);
        xitem.appendChild(dataItem);
        
        // 创建 DescriptionSet 节点
        org.w3c.dom.Element descSet = doc.createElement("DescriptionSet");
        org.w3c.dom.Element descSetItem = doc.createElement("Set");
        descSetItem.setAttribute("Lang", "zhcn");
        if (field.getDescription() != null) {
            descSetItem.setAttribute("Info", field.getDescription());
        }
        descSet.appendChild(descSetItem);
        xitem.appendChild(descSet);
        
        return xitem;
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
