package com.gdxsoft.easyweb.define.bussinessXmlCreator;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.gdxsoft.easyweb.script.template.EwaConfig;
import com.gdxsoft.easyweb.define.database.Field;
import com.gdxsoft.easyweb.define.database.Table;
import com.gdxsoft.easyweb.utils.UXml;

/**
 * 业务 XML 创建器基类
 * 提供公共功能和默认实现
 */
public abstract class BusinessXmlCreatorBase {
    protected static final Logger LOGGER = LoggerFactory.getLogger(BusinessXmlCreatorBase.class);

    protected EwaConfig config;
    protected Table table;
    protected Document xmlDoc;
    protected EwaDefineConfig defineConfig;

    public BusinessXmlCreatorBase(EwaConfig config, Table table) {
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
            // 生成 XML
            String xmlContent = createShowXml(db, tableName, selectSql, tableJson, frameType, operationType);
            if (xmlContent == null) {
                return false;
            }

            // 保存 XML
            return save(xmlContent, xmlName, itemName, admId);
        } catch (Exception e) {
            LOGGER.error("创建并保存 XML 失败：" + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 生成预览 XML
     */
    public String createShowXml(String db, String tableName, String selectSql,
            JSONObject tableJson, String frameType, String operationType) {
        try {
            // 生成 XML
            this.xmlDoc = create(frameType, operationType);

            // 返回 XML 字符串
            return docToString(this.xmlDoc);
        } catch (Exception e) {
            LOGGER.error("生成 XML 失败：" + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将 Document 转换为字符串
     */
    private String docToString(Document doc) throws Exception {
        javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
        javax.xml.transform.Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        
        java.io.StringWriter writer = new java.io.StringWriter();
        transformer.transform(new javax.xml.transform.dom.DOMSource(doc), 
                             new javax.xml.transform.stream.StreamResult(writer));
        return writer.toString();
    }

    /**
     * 创建 XML 文档（由子类实现）
     */
    protected abstract Document create(String frameType, String operationType) throws Exception;

    /**
     * 保存 XML（由子类实现）
     */
    protected abstract boolean save(String xmlContent, String xmlName, String itemName, String admId);

    /**
     * 获取当前日期时间
     */
    protected String getCurrentDateTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
    }

    /**
     * 获取 Frame 类型简写
     */
    protected String getFrameTypeShort(String frameType) {
        switch (frameType.toUpperCase()) {
            case "FRAME": return "F";
            case "LISTFRAME": return "LF";
            case "TREE": return "T";
            default: return frameType;
        }
    }

    /**
     * 获取 Frame 名称
     */
    protected String getFrameName(String frameType) {
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
    protected String getOperationName(String operationType) {
        switch (operationType.toUpperCase()) {
            case "N": return "新增";
            case "M": return "修改";
            case "V": return "查看";
            case "NM": return "新增修改";
            default: return operationType;
        }
    }

    /**
     * 获取 Tag 类型
     * 从 EwaDefineSettings.xml/FieldTags 读取配置
     * 参考 EwaConfig.xml/Items/XItems 中定义的 XItem Name
     * 可用的类型：text, textarea, span, hidden, password, date, datetime, time, select, combo, checkbox, radio 等
     * 特殊后缀：_SQL→sqlEditor, _XML→xmlEditor, _JSON→jsonEditor, _HTML→dHtml5, _CSS→cssEditor
     * @param dbType 数据库字段类型
     * @param fieldName 字段名（用于后缀匹配）
     * @param frameType Frame 类型（Frame 或 ListFrame），用于选择对应的配置
     * @return Tag 类型
     */
    protected String getTagType(String dbType, String fieldName, String frameType) {
        // 从配置读取 Tag 类型（支持字段名后缀匹配和 Frame 类型区分）
        return EwaDefineSettings.getInstance().getTagType(dbType, fieldName, frameType);
    }

    /**
     * 获取 Tag 类型（不传字段名和 frameType）
     */
    protected String getTagType(String dbType) {
        return getTagType(dbType, null, null);
    }

    /**
     * 获取数据类型
     */
    protected String getDataType(String dbType) {
        if (dbType == null) return "String";
        String type = dbType.toUpperCase();
        if (type.contains("INT")) return "Int";
        if (type.contains("DECIMAL") || type.contains("NUM")) return "Double";
        if (type.contains("DATE") || type.contains("TIME")) return "Date";
        return "String";
    }

    /**
     * 获取日期格式
     */
    protected String getFormat(String dbType) {
        // 从配置读取格式
        return EwaDefineSettings.getInstance().getFormat(dbType);
    }

    /**
     * 获取数字精度
     */
    protected String getNumberScale(String dbType) {
        // 从配置读取精度
        return EwaDefineSettings.getInstance().getNumberScale(dbType);
    }

    /**
     * 获取验证类型
     */
    protected String getValidType(String dbType) {
        if (dbType == null) return "";
        String type = dbType.toUpperCase();
        if (type.contains("EMAIL")) return "Email";
        if (type.contains("URL")) return "Url";
        if (type.contains("INT") || type.contains("NUM")) return "Number";
        return "";
    }

    /**
     * 获取字段显示名称
     */
    protected String getFieldDisplayName(Field field) {
        String name = field.getName();
        if (name.endsWith("_ID")) {
            return name.substring(0, name.length() - 3) + "_NAME";
        }
        return name + "_NAME";
    }

    /**
     * 获取字段值名称
     */
    protected String getFieldValueName(Field field) {
        return field.getName();
    }

    /**
     * 判断字段是否应该隐藏
     */
    protected boolean shouldHideField(Field field) {
        String fieldName = field.getName().toUpperCase();
        
        // 自增主键不展示
        if (field.isIdentity()) {
            return true;
        }
        
        // **_CDATE, **_MDATE, **_STATUS, **_STATE 字段不展示
        if (fieldName.endsWith("_CDATE") || fieldName.endsWith("_MDATE") || 
            fieldName.endsWith("_STATUS") || fieldName.endsWith("_STATE")) {
            return true;
        }
        
        return false;
    }

    /**
     * 获取状态字段名
     */
    protected String getStatusField() {
        for (String fieldName : this.table.getFields().getFieldList()) {
            if (fieldName.endsWith("_STATE") || fieldName.equals("STATUS") || fieldName.endsWith("_STATUS")) {
                return fieldName;
            }
        }
        return null;
    }

    /**
     * 获取主键字段名
     */
    protected String getPrimaryKeyField() {
        if (this.table.getPk() != null) {
            java.util.ArrayList<Field> pkFields = this.table.getPk().getPkFields();
            if (pkFields != null && !pkFields.isEmpty()) {
                java.util.ArrayList<String> fieldNames = new java.util.ArrayList<String>();
                for (Field pkField : pkFields) {
                    fieldNames.add(pkField.getName());
                }
                return String.join(",", fieldNames);
            }
        }
        return null;
    }

    /**
     * 处理 JavaScript 表达式
     */
    protected String processJsExpression(String val) {
        if (val == null) return "";

        // 处理 define.Fields.GetPkParas() 调用
        String pkParas = this.table.getFields().GetPkParas();
        if (pkParas != null && !pkParas.isEmpty()) {
            if (pkParas.startsWith("&")) {
                pkParas = pkParas.substring(1);
            }
            val = val.replace("'+ define.Fields.GetPkParas() + '", pkParas);
            val = val.replace("' + define.Fields.GetPkParas() + '", pkParas);
            val = val.replace("define.Fields.GetPkParas()", pkParas);
        }

        // 移除字符串连接的 '+' 符号
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
    protected Element findChildElement(Element parent, String tagName) {
        org.w3c.dom.NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element elem = (Element) children.item(i);
                if (elem.getTagName().equals(tagName)) {
                    return elem;
                }
            }
        }
        return null;
    }

    /**
     * 创建 DescriptionSet
     */
    protected Element createDescriptionSet(Document doc, java.util.Map<String, String> descMap) {
        Element descSet = doc.createElement("DescriptionSet");
        for (java.util.Map.Entry<String, String> entry : descMap.entrySet()) {
            Element set = doc.createElement("Set");
            set.setAttribute("Lang", entry.getKey());
            set.setAttribute("Info", entry.getValue());
            set.setAttribute("Memo", "");
            descSet.appendChild(set);
        }
        return descSet;
    }
    
    /**
     * 获取 Tree 加载 SQL
     * 如果存在 _STATUS 或 _STATE 字段，添加 WHERE status='USED' 条件
     */
    protected String getSqlTreeLoad() {
        String tableName = this.table.getName();
        String levelField = findFieldBySuffix("_LVL");
        String orderField = findFieldBySuffix("_ORD");
        String statusField = findStatusField();
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(tableName);
        
        // 如果有状态字段，添加 WHERE 条件
        if (statusField != null) {
            sql.append(" WHERE ").append(statusField).append("='USED'");
        }
        
        // 添加 ORDER BY
        if (levelField != null || orderField != null) {
            sql.append(" ORDER BY ");
            if (levelField != null) {
                sql.append(levelField);
                if (orderField != null) {
                    sql.append(", ").append(orderField);
                }
            } else if (orderField != null) {
                sql.append(orderField);
            }
        }
        
        return sql.toString();
    }
    
    /**
     * 获取 Tree 删除节点 SQL
     * 如果存在 _STATUS 或 _STATE 字段，改为 UPDATE status='DEL' 而不是 DELETE
     */
    protected String getSqlTreeNodeDelete() {
        String pkField = getPrimaryKeyField();
        String statusField = findStatusField();
        
        if (pkField == null || pkField.isEmpty()) {
            return "-- Primary key not found";
        }
        
        // 如果有状态字段，使用 UPDATE 而不是 DELETE
        if (statusField != null) {
            return "UPDATE " + this.table.getName() + " SET " + statusField + "='DEL' WHERE " + pkField + " = @" + pkField;
        }
        
        return "DELETE FROM " + this.table.getName() + " WHERE " + pkField + " = @" + pkField;
    }
    
    /**
     * 获取 Tree 重命名节点 SQL
     */
    protected String getSqlTreeNodeRename() {
        String pkField = getPrimaryKeyField();
        String nameField = findFieldBySuffix("_NAME");
        
        if (pkField == null || pkField.isEmpty()) {
            return "-- Primary key not found";
        }
        if (nameField == null || nameField.isEmpty()) {
            return "-- Name field not found";
        }
        
        return "UPDATE " + this.table.getName() + " SET " + nameField + " = @" + nameField + 
               " WHERE " + pkField + " = @" + pkField;
    }
    
    /**
     * 获取 Tree 新增节点 SQL
     * 参考 SQL Server 模板，使用 CASE WHEN 适配不同数据库
     */
    protected String getSqlTreeNodeNew() {
        String pkField = getPrimaryKeyField();
        String parentField = findFieldBySuffix("_PID");
        String levelField = findFieldBySuffix("_LVL");
        String orderField = findFieldBySuffix("_ORD");
        String nameField = findFieldBySuffix("_NAME");
        String cdateField = findFieldBySuffix("_CDATE");
        String mdateField = findFieldBySuffix("_MDATE");
        String statusField = findStatusField();
        
        if (pkField == null || pkField.isEmpty()) {
            return "-- Primary key not found";
        }
        
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(this.table.getName()).append(" (");
        
        java.util.ArrayList<String> fields = new java.util.ArrayList<String>();
        java.util.ArrayList<String> selectFields = new java.util.ArrayList<String>();
        java.util.ArrayList<String> fromTables = new java.util.ArrayList<String>();
        java.util.ArrayList<String> whereConditions = new java.util.ArrayList<String>();
        
        // 名称字段
        if (nameField != null) {
            fields.add(nameField);
            selectFields.add("@EWA_TREE_TEXT " + nameField);
        }
        
        // 父 ID 字段 - 使用 CASE WHEN 适配不同数据库
        if (parentField != null) {
            fields.add(parentField);
            selectFields.add("CASE WHEN @EWA_TREE_PARENT_KEY IS NULL THEN 0 ELSE @EWA_TREE_PARENT_KEY END " + parentField);
        }
        
        // 层级字段 - 使用 CASE WHEN 适配不同数据库
        if (levelField != null) {
            fields.add(levelField);
            selectFields.add("CASE WHEN MAX(pp." + levelField + ") IS NULL THEN -1 ELSE MAX(pp." + levelField + ") END+1 " + levelField);
            fromTables.add(this.table.getName() + " pp");
        }
        
        // 排序字段 - 使用 CASE WHEN 适配不同数据库
        if (orderField != null) {
            fields.add(orderField);
            selectFields.add("CASE WHEN MAX(pc." + orderField + ") IS NULL THEN 0 ELSE MAX(pc." + orderField + ") END+1 " + orderField);
            if (fromTables.isEmpty()) {
                fromTables.add(this.table.getName() + " pc");
            } else {
                // 使用 LEFT JOIN
                String joinTable = this.table.getName() + " pc ON pc." + parentField + "=pp." + pkField;
                if (!fromTables.get(0).contains("JOIN")) {
                    fromTables.set(0, fromTables.get(0) + " LEFT JOIN " + joinTable);
                }
            }
        }
        
        // 创建时间字段
        if (cdateField != null) {
            fields.add(cdateField);
            selectFields.add("@SYS_DATE " + cdateField);
        }
        
        // 修改时间字段
        if (mdateField != null) {
            fields.add(mdateField);
            selectFields.add("@SYS_DATE " + mdateField);
        }
        
        // 状态字段 - 默认值为 'USED'
        if (statusField != null) {
            fields.add(statusField);
            selectFields.add("'USED' " + statusField);
        }
        
        // WHERE 条件
        if (parentField != null && !fromTables.isEmpty()) {
            whereConditions.add("WHERE pp." + parentField + "= @EWA_TREE_PARENT_KEY");
        }
        
        // 构建 SQL
        sql.append(String.join(", ", fields));
        sql.append(") \nSELECT \t ");
        sql.append(String.join(",\n\t ", selectFields));
        
        if (!fromTables.isEmpty()) {
            sql.append("\nFROM ").append(String.join(" ", fromTables));
        }
        
        if (!whereConditions.isEmpty()) {
            sql.append("\n").append(String.join(" ", whereConditions));
        }
        
        sql.append("\n-- auto ").append(pkField);
        
        return sql.toString();
    }
    
    /**
     * 查找带有指定后缀的字段
     */
    protected String findFieldBySuffix(String suffix) {
        for (String fieldName : this.table.getFields().getFieldList()) {
            if (fieldName.toUpperCase().endsWith(suffix.toUpperCase())) {
                return fieldName;
            }
        }
        return null;
    }
    
    /**
     * 查找状态字段 (_STATUS 或 _STATE)
     */
    protected String findStatusField() {
        // 优先查找 _STATUS
        for (String fieldName : this.table.getFields().getFieldList()) {
            if (fieldName.toUpperCase().endsWith("_STATUS")) {
                return fieldName;
            }
        }
        // 查找 _STATE
        for (String fieldName : this.table.getFields().getFieldList()) {
            if (fieldName.toUpperCase().endsWith("_STATE")) {
                return fieldName;
            }
        }
        return null;
    }
}
