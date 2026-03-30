package com.gdxsoft.easyweb.define.bussinessXmlCreator;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.gdxsoft.easyweb.script.template.EwaConfig;
import com.gdxsoft.easyweb.define.database.Table;
import com.gdxsoft.easyweb.utils.UXml;

/**
 * 业务 XML 创建器工厂类
 * 根据 Frame 类型创建相应的创建器实例
 */
public class BusinessXmlCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessXmlCreator.class);

    private BusinessXmlCreatorBase creator;

    /**
     * 构造函数 - 根据表结构自动选择创建器
     */
    public BusinessXmlCreator(EwaConfig config, Table table) {
        // 默认使用 ListFrame 创建器
        this.creator = new BusinessXmlCreatorListFrame(config, table);
    }

    /**
     * 创建指定 Frame 类型的创建器
     */
    public static BusinessXmlCreator create(EwaConfig config, Table table, String frameType) {
        BusinessXmlCreatorBase baseCreator;
        
        if ("ListFrame".equalsIgnoreCase(frameType)) {
            baseCreator = new BusinessXmlCreatorListFrame(config, table);
        } else if ("Frame".equalsIgnoreCase(frameType)) {
            baseCreator = new BusinessXmlCreatorFrame(config, table);
        } else {
            LOGGER.warn("未知的 Frame 类型：{}，使用 ListFrame 创建器", frameType);
            baseCreator = new BusinessXmlCreatorListFrame(config, table);
        }
        
        // 使用反射创建包装器
        BusinessXmlCreator instance = new BusinessXmlCreator(config, table);
        instance.creator = baseCreator;
        return instance;
    }

    /**
     * 生成并保存业务 XML
     */
    public boolean createAndSave(String db, String tableName, String selectSql,
            JSONObject tableJson, String frameType, String operationType,
            String xmlName, String itemName, String admId) {
        return creator.createAndSave(db, tableName, selectSql, tableJson, frameType, operationType, xmlName, itemName, admId);
    }

    /**
     * 生成预览 XML
     */
    public String createShowXml(String db, String tableName, String selectSql,
            JSONObject tableJson, String frameType, String operationType) {
        return creator.createShowXml(db, tableName, selectSql, tableJson, frameType, operationType);
    }

    /**
     * 获取底层创建器实例
     */
    public BusinessXmlCreatorBase getCreator() {
        return creator;
    }
}
