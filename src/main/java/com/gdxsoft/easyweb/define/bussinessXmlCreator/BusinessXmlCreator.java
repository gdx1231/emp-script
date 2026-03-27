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
                params = new BusinessXmlCreateParams(db, selectSql, frameType, operationType);
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
                params = new BusinessXmlCreateParams(db, selectSql, frameType, operationType);
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
     * 创建 XML 文档（简化版本）
     */
    private Document create(String frameType, String operationType) throws Exception {
        Document doc = UXml.createBlankDocument();
        // TODO: 实现完整的 XML 创建逻辑
        return doc;
    }

    /**
     * 保存 XML（使用 IUpdateXml 接口）
     */
    private boolean save(String xmlName, String itemName, String admId) {
        // TODO: 实现保存逻辑
        return true;
    }
}
