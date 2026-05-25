# EwaConfig.xml 到业务 XML 生成器开发计划

> 包路径：`com.gdxsoft.easyweb.define.bussinessXmlCreator`
> 基于 `system.xml/EwaConfig.xml` 规则模板生成符合规范的业务 XML 配置

---

## 1. 项目概述

### 1.1 目标
开发一个 Java 工具类库，根据 `EwaConfig.xml` 中定义的规则模板，动态生成符合规范的业务 XML 配置文件，**禁止硬编码**任何节点名和属性值。

### 1.2 核心原则
1. **规则驱动**: 所有节点、属性必须来自 EwaConfig.xml 定义
2. **类型安全**: 属性值必须来自 XGroupValue 预定义值
3. **可扩展**: 新增 XItem 类型无需修改代码
4. **可验证**: 生成的 XML 可通过规则模板自动验证

### 1.3 包结构
```
com.gdxsoft.easyweb.define.bussinessXmlCreator/
├── creator/                     # XML 创建相关
│   ├── BusinessXmlCreator.java  # 主创建器
│   ├── XItemCreator.java        # XItem 创建器
│   └── NodeCreator.java         # 节点创建器
│
├── validator/                   # 验证相关
│   ├── XmlValidator.java        # XML 验证器
│   ├── ValidationResult.java    # 验证结果
│   └── ValidationError.java     # 验证错误
│
├── util/                        # 工具类
│   ├── FieldMetadataExt.java    # 字段元数据扩展
│   └── XmlBuilder.java          # XML 构建工具 (基于 UXml)
│
└── 依赖现有类:
    ├── com.gdxsoft.easyweb.script.template.*
    │   ├── EwaConfig            # EwaConfig.xml 解析器
    │   ├── XItem                # XItem 定义
    │   ├── XItemParameter       # 参数定义
    │   ├── XItemParameterValue  # 参数值
    │   ├── XItems               # XItem 集合
    │   ├── XItemParameters      # 参数集合
    │   └── Descriptions         # 多语言描述
    ├── com.gdxsoft.easyweb.define.database.*
    │   ├── Tables               # 表集合
    │   ├── Table                # 表元数据
    │   └── Field                # 字段元数据
    └── com.gdxsoft.easyweb.utils.UXml  # XML 工具
```

### 1.4 依赖的现有类

#### 1.4.1 EwaConfig 配置类 (`com.gdxsoft.easyweb.script.template`)

**核心类**:
| 类名 | 用途 | 主要方法 |
|-----|------|---------|
| `EwaConfig` | EwaConfig.xml 解析器 | `instance()`, `getConfigItems()`, `getConfigFrames()` |
| `XItem` | XItem 定义 | `getName()`, `getHtmlTag()`, `getParameters()`, `getClassName()` |
| `XItemParameter` | 参数定义 | `getName()`, `isSet()`, `isShow()`, `isMulti()`, `getValues()` |
| `XItemParameterValue` | 参数值定义 | `getName()`, `getType()`, `getCreateValue()`, `isUnique()` |
| `XItems` | XItem 集合 | `count()`, `getItem(index)`, `getItem(name)` |
| `XItemParameters` | 参数集合 | `count()`, `getItem(index)`, `getItem(name)` |
| `Descriptions` | 多语言描述 | `getInfo(lang)`, `getMemo(lang)` |
| `EwaConfigFrame` | Frame 定义 | `getName()`, `getFrameClassName()` |

**使用示例**:
```java
import com.gdxsoft.easyweb.script.template.EwaConfig;
import com.gdxsoft.easyweb.script.template.XItem;
import com.gdxsoft.easyweb.script.template.XItemParameter;
import com.gdxsoft.easyweb.script.template.XItems;

// 1. 获取 EwaConfig 实例（单例，已解析 EwaConfig.xml）
EwaConfig config = EwaConfig.instance();

// 2. 获取所有 XItem 定义
XItems xItems = config.getConfigItems().getItems();
for (int i = 0; i < xItems.count(); i++) {
    XItem item = xItems.getItem(i);
    String name = item.getName();           // text, span, select 等
    String htmlTag = item.getHtmlTag();     // HTML 标签
    String[] params = item.getParameters(); // 参数列表
}

// 3. 获取所有参数定义
XItemParameters parameters = config.getConfigItems().getParameters();
for (int i = 0; i < parameters.count(); i++) {
    XItemParameter param = parameters.getItem(i);
    String paramName = param.getName();
    boolean isSet = param.isSet();          // 是否 Set 结构
    boolean isMulti = param.isMulti();      // 是否多值
    XItemParameterValues values = param.getValues();
}
```

#### 1.4.2 数据模型类 (`com.gdxsoft.easyweb.define.database`)

| 类名 | 用途 | 说明 |
|-----|------|------|
| `Tables` | 表集合 | 加载和管理多个表 |
| `Table` | 表元数据 | 表名、注释、字段集合等 |
| `Field` | 字段元数据 | 字段名、类型、长度、注释等 |
| `Fields` | 字段集合 | 管理表的所有字段 |
| `TablePk` | 主键信息 | 主键字段 |
| `TableFk` | 外键信息 | 外键关联 |
| `TableIndex` | 索引信息 | 表索引 |

**使用示例**:
```java
// 加载表元数据
Tables tables = new Tables();
tables.loadFromDatabase("globaltravel");

// 获取特定表
Table table = tables.getTable("CRM_COM");

// 获取字段
Fields fields = table.getFields();
for (int i = 0; i < fields.getCount(); i++) {
    Field field = fields.getField(i);
    String fieldName = field.getName();
    String fieldType = field.getType();
    int fieldLength = field.getLength();
    String comment = field.getComment();
}
```

#### 1.4.2 XML 工具类 (`com.gdxsoft.easyweb.utils.UXml`)

**位置**: `emp-script-utils/src/main/java/com/gdxsoft/easyweb/utils/UXml.java`

| 方法 | 说明 |
|-----|------|
| `asDocument(String xml)` | 将 XML 字符串转为 Document |
| `retDocument(String path)` | 从文件加载 Document |
| `saveDocument(Document doc, String path)` | 保存 Document 到文件 |
| `asXml(Node node)` | 将节点转为 XML 字符串 |
| `createBlankDocument()` | 创建空白 Document |
| `retNodeValue(Node parent, String nodeName)` | 获取节点值 |
| `retNode(Node parent, String nodeName)` | 获取子节点 |
| `retNodeList(Node parent, String xpath)` | 获取节点列表 |
| `getElementAttributes(Element ele, boolean keyToLower)` | 获取元素属性 |
| `createXmlValue(String value)` | 创建 XML 属性值（转义） |
| `filterInvalidXMLcharacter(String xml)` | 过滤非法 XML 字符 |
| `findNode(Element fromNode, String findTag, String findAttr, String checkValue, boolean isIgnoreCase)` | 查找节点 |

**使用示例**:
```java
import com.gdxsoft.easyweb.utils.UXml;

// 创建空白文档
Document doc = UXml.createBlankDocument();

// 创建根节点
Element root = doc.createElement("EasyWebTemplates");
doc.appendChild(root);

// 创建子节点
Element template = doc.createElement("EasyWebTemplate");
template.setAttribute("Name", "CRM_COM.F.NM");
root.appendChild(template);

// 保存文件
UXml.saveDocument(doc, "/output/CRM_COM.F.NM.xml");

// 读取 XML
Document readDoc = UXml.retDocument("/output/CRM_COM.F.NM.xml");

// 获取节点值
String name = UXml.retNodeValue(template, "Name");

// 获取属性
Map<String, String> attrs = UXml.getElementAttributes(template, true);
```

**Maven 依赖**:
```xml
<dependency>
    <groupId>com.gdxsoft</groupId>
    <artifactId>emp-script-utils</artifactId>
    <version>${project.version}</version>
</dependency>
```

---

## 2. 开发任务分解

### 2.1 阶段一：XML 创建层 (creator 包)

**说明**: 利用现有的 `EwaConfig` 类获取配置规则，无需自定义配置解析类。

#### 任务 2.1.1: BusinessXmlCreator.java
**目标**: 主创建器，协调整个 XML 生成过程

```java
package com.gdxsoft.easyweb.define.bussinessXmlCreator.creator;

import com.gdxsoft.easyweb.script.template.EwaConfig;
import com.gdxsoft.easyweb.script.template.XItem;
import com.gdxsoft.easyweb.script.template.XItemParameter;
import com.gdxsoft.easyweb.script.template.XItems;
import com.gdxsoft.easyweb.script.template.XItemParameters;
import com.gdxsoft.easyweb.define.database.Table;
import com.gdxsoft.easyweb.utils.UXml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BusinessXmlCreator {
    private EwaConfig config;           // 使用现有的 EwaConfig 类
    private Table table;                // 使用现有的 Table 类
    private String frameType;           // Frame, ListFrame, Tree
    private String templateName;        // NM, V, M
    private Document xmlDoc;
    
    // 构造方法
    public BusinessXmlCreator(EwaConfig config, Table table) {
        this.config = config;
        this.table = table;
    }
    
    // 核心方法
    public Document create(String frameType, String templateName) {
        this.frameType = frameType;
        this.templateName = templateName;
        
        // 1. 创建根节点 EasyWebTemplates
        xmlDoc = UXml.createBlankDocument();
        Element root = xmlDoc.createElement("EasyWebTemplates");
        xmlDoc.appendChild(root);
        
        // 2. 创建 EasyWebTemplate 节点
        Element template = xmlDoc.createElement("EasyWebTemplate");
        template.setAttribute("Name", table.getName() + "." + frameType + "." + templateName);
        template.setAttribute("CreateDate", getCurrentDateTime());
        root.appendChild(template);
        
        // 3. 创建 Page 节点（根据模板）
        createPageNode(template);
        
        // 4. 创建 Action 节点（根据模板）
        createActionNode(template);
        
        // 5. 创建 XItems 节点
        createXItemsNode(template);
        
        // 6. 创建其他节点（Menus, Charts, PageInfos 等）
        createOtherNodes(template);
        
        return xmlDoc;
    }
    
    public String createXmlString(String frameType, String templateName) {
        Document doc = create(frameType, templateName);
        return UXml.asXml(doc);
    }
    
    // 配置方法
    public void setFrameType(String frameType);
    public void setTemplateName(String templateName);
    
    // 验证方法
    public ValidationResult validate() {
        XmlValidator validator = new XmlValidator(config);
        return validator.validate(xmlDoc);
    }
    
    // 辅助方法
    private void createPageNode(Element parent) { ... }
    private void createActionNode(Element parent) { ... }
    private void createXItemsNode(Element parent) { ... }
    private void createOtherNodes(Element parent) { ... }
}
```

---

#### 任务 2.1.2: XItemCreator.java
**目标**: 创建 XItem 节点

```java
package com.gdxsoft.easyweb.define.bussinessXmlCreator.creator;

import com.gdxsoft.easyweb.script.template.*;
import com.gdxsoft.easyweb.define.database.Field;
import com.gdxsoft.easyweb.define.bussinessXmlCreator.util.FieldMetadataExt;
import com.gdxsoft.easyweb.utils.UXml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XItemCreator {
    private EwaConfig config;
    private Field field;
    private Document doc;
    
    // 构造方法
    public XItemCreator(EwaConfig config, Field field, Document doc) {
        this.config = config;
        this.field = field;
        this.doc = doc;
    }
    
    // 核心方法
    public Element createXItem() {
        Element xItem = doc.createElement("XItem");
        xItem.setAttribute("Name", field.getName());
        
        // 推断字段类型
        String tag = FieldMetadataExt.getTag(field, "Frame");
        String dataType = FieldMetadataExt.getDataType(field);
        String valid = FieldMetadataExt.getValid(field);
        String format = FieldMetadataExt.getFormat(field);
        
        // 创建各参数节点
        createTagNode(xItem, tag);
        createNameNode(xItem);
        createDescriptionSetNode(xItem);
        createDataItemNode(xItem, dataType, valid, format);
        createIsMustInputNode(xItem);
        createMaxMinLengthNode(xItem);
        
        // 如果是外键，创建 List 节点
        if (field.getName().endsWith("_ID")) {
            createListNode(xItem);
        }
        
        return xItem;
    }
    
    // 创建 Tag 节点
    private void createTagNode(Element parent, String tag) {
        Element tagElem = doc.createElement("Tag");
        Element setElem = doc.createElement("Set");
        setElem.setAttribute("Tag", tag);
        setElem.setAttribute("IsLFEdit", "0");
        tagElem.appendChild(setElem);
        parent.appendChild(tagElem);
    }
    
    // 创建 Name 节点
    private void createNameNode(Element parent) {
        Element nameElem = doc.createElement("Name");
        Element setElem = doc.createElement("Set");
        setElem.setAttribute("Name", field.getName());
        nameElem.appendChild(setElem);
        parent.appendChild(nameElem);
    }
    
    // 创建 DescriptionSet 节点（多语言）
    private void createDescriptionSetNode(Element parent) {
        Element descSetElem = doc.createElement("DescriptionSet");
        
        // 解析注释（中文 | 英文格式）
        String[] comments = parseComment(field.getComment());
        String zhComment = comments[0];
        String enComment = comments.length > 1 ? comments[1] : zhComment;
        
        // 中文
        Element setZh = doc.createElement("Set");
        setZh.setAttribute("Lang", "zhcn");
        setZh.setAttribute("Info", zhComment);
        setZh.setAttribute("Memo", "");
        descSetElem.appendChild(setZh);
        
        // 英文
        Element setEn = doc.createElement("Set");
        setEn.setAttribute("Lang", "enus");
        setEn.setAttribute("Info", enComment);
        setEn.setAttribute("Memo", "");
        descSetElem.appendChild(setEn);
        
        parent.appendChild(descSetElem);
    }
    
    // 创建 DataItem 节点
    private void createDataItemNode(Element parent, String dataType, String valid, String format) {
        Element dataItemElem = doc.createElement("DataItem");
        Element setElem = doc.createElement("Set");
        
        // DataField - 必须来自字段名
        setElem.setAttribute("DataField", field.getName());
        
        // DataType - 验证是否在 XGroupValue 中定义
        // 注意：需要从 EwaConfig 中获取 XGroupValue 定义
        setElem.setAttribute("DataType", dataType);
        
        // Format - 验证是否在 XGroupValue 中定义
        if (format != null && !format.isEmpty()) {
            setElem.setAttribute("Format", format);
        }
        
        // Valid - 验证是否在 XGroupValue 中定义
        if (valid != null && !valid.isEmpty()) {
            setElem.setAttribute("Valid", valid);
        }
        
        dataItemElem.appendChild(setElem);
        parent.appendChild(dataItemElem);
    }
    
    // 创建 IsMustInput 节点
    private void createIsMustInputNode(Element parent) {
        Element isMustInputElem = doc.createElement("IsMustInput");
        Element setElem = doc.createElement("Set");
        
        // 如果字段不允许为空，则设为必填
        String isMust = field.isNullable() ? "0" : "1";
        setElem.setAttribute("IsMustInput", isMust);
        
        isMustInputElem.appendChild(setElem);
        parent.appendChild(isMustInputElem);
    }
    
    // 创建 MaxMinLength 节点
    private void createMaxMinLengthNode(Element parent) {
        Element maxMinLengthElem = doc.createElement("MaxMinLength");
        Element setElem = doc.createElement("Set");
        setElem.setAttribute("MaxLength", String.valueOf(field.getLength()));
        setElem.setAttribute("MinLength", "");
        maxMinLengthElem.appendChild(setElem);
        parent.appendChild(maxMinLengthElem);
    }
    
    // 创建 List 节点（外键）
    private void createListNode(Element parent) {
        // 根据外键表名生成 List SQL
        String fkTableName = getFkTableName(field.getName());
        
        Element listElem = doc.createElement("List");
        Element setElem = doc.createElement("Set");
        setElem.setAttribute("Sql", "SELECT * FROM " + fkTableName + " WHERE 1=1");
        setElem.setAttribute("ValueField", fkTableName + "_ID");
        setElem.setAttribute("DisplayField", fkTableName + "_NAME");
        listElem.appendChild(setElem);
        parent.appendChild(listElem);
    }
    
    // 辅助方法
    private String[] parseComment(String comment) {
        if (comment == null) {
            return new String[]{"", ""};
        }
        if (comment.contains("|")) {
            return comment.split("\\|");
        }
        return new String[]{comment.trim(), comment.trim()};
    }
    
    private String getFkTableName(String fieldName) {
        // 例如：CRM_COM_ID → CRM_COM
        if (fieldName.endsWith("_ID")) {
            return fieldName.substring(0, fieldName.length() - 3);
        }
        return fieldName;
    }
}
```

---

#### 任务 2.1.3: NodeCreator.java
**目标**: 通用节点创建器

```java
package com.gdxsoft.easyweb.define.bussinessXmlCreator.creator;

import com.gdxsoft.easyweb.script.template.*;
import com.gdxsoft.easyweb.utils.UXml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class NodeCreator {
    private Document doc;
    private EwaConfig config;
    
    public NodeCreator(Document doc, EwaConfig config) {
        this.doc = doc;
        this.config = config;
    }
    
    // 创建 Set 结构节点（多语言）
    public Element createSetNode(String paramName, Map<String, String> values) {
        Element setParent = doc.createElement(paramName + "Set");
        
        for (Map.Entry<String, String> entry : values.entrySet()) {
            Element set = doc.createElement("Set");
            set.setAttribute("Lang", entry.getKey());
            set.setAttribute("Info", entry.getValue());
            setParent.appendChild(set);
        }
        
        return setParent;
    }
    
    // 创建单值节点
    public Element createSingleNode(String paramName, String value) {
        Element setElem = doc.createElement("Set");
        setElem.setAttribute(paramName, value);
        return setElem;
    }
    
    // 创建 DescriptionSet 节点
    public Element createDescriptionSet(String infoZh, String infoEn) {
        Element descSetElem = doc.createElement("DescriptionSet");
        
        Element setZh = doc.createElement("Set");
        setZh.setAttribute("Lang", "zhcn");
        setZh.setAttribute("Info", infoZh);
        descSetElem.appendChild(setZh);
        
        Element setEn = doc.createElement("Set");
        setEn.setAttribute("Lang", "enus");
        setEn.setAttribute("Info", infoEn);
        descSetElem.appendChild(setEn);
        
        return descSetElem;
    }
}
```

---

### 2.2 阶段二：数据模型层 - 使用现有类

#### 任务 2.2.1: 使用 Table 和 Field 类

**无需创建新的数据模型类**，直接使用现有的：
- `com.gdxsoft.easyweb.define.database.Table`
- `com.gdxsoft.easyweb.define.database.Field`
- `com.gdxsoft.easyweb.define.database.Fields`
- `com.gdxsoft.easyweb.define.database.Tables`

**字段类型推断扩展**:

```java
package com.gdxsoft.easyweb.define.bussinessXmlCreator.util;

import com.gdxsoft.easyweb.define.database.Field;

public class FieldMetadataExt {
    
    /**
     * 根据数据库类型推断 EWA DataType
     */
    public static String getDataType(Field field) {
        String type = field.getType().toLowerCase();
        if (type.contains("date") || type.contains("time")) {
            return "Date";
        }
        if (type.contains("int")) {
            return "Int";
        }
        if (type.contains("num") || type.contains("money") || type.contains("decimal")) {
            return "Number";
        }
        if (type.contains("blob") || type.contains("image")) {
            return "Binary";
        }
        return "String";
    }
    
    /**
     * 根据字段特征推断 Tag
     */
    public static String getTag(Field field, String frameType) {
        String name = field.getName().toUpperCase();
        String type = field.getType().toLowerCase();
        
        if ("Frame".equals(frameType)) {
            if (name.contains("PWD")) {
                return "password";
            }
            if (type.contains("date") || type.contains("time")) {
                return "date";
            }
            if (field.getLength() > 200 || type.equals("text")) {
                return "textarea";
            }
            if (name.endsWith("_ID")) {
                return "select";  // 外键
            }
            return "text";
        } else {
            return "span";  // ListFrame 默认
        }
    }
    
    /**
     * 根据字段名推断验证规则
     */
    public static String getValid(Field field) {
        String name = field.getName().toUpperCase();
        if (name.contains("EMAIL")) {
            return "Email";
        }
        if (name.contains("MOBILE") || name.contains("PHONE")) {
            return "Mobile";
        }
        if (name.contains("URL")) {
            return "Url";
        }
        if (name.contains("NUMBER") || name.contains("NUM")) {
            return "Number";
        }
        return "";
    }
    
    /**
     * 获取格式化配置
     */
    public static String getFormat(Field field) {
        String type = field.getType().toLowerCase();
        if (type.contains("date") || type.contains("time")) {
            return "Date";
        }
        if (type.contains("money") || type.contains("decimal")) {
            return "Money";
        }
        return "";
    }
}
```

---

### 2.3 阶段三：XML 创建层 (creator 包)

#### 任务 2.3.1: BusinessXmlCreator.java
**目标**: 主创建器，协调整个 XML 生成过程

```java
package com.gdxsoft.easyweb.define.bussinessXmlCreator.creator;

import com.gdxsoft.easyweb.define.bussinessXmlCreator.config.EwaConfigLoader;
import com.gdxsoft.easyweb.define.database.Table;
import com.gdxsoft.easyweb.utils.UXml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BusinessXmlCreator {
    private EwaConfigLoader configLoader;
    private Table table;                  // 使用现有的 Table 类
    private String frameType;             // Frame, ListFrame, Tree
    private String templateName;          // NM, V, M
    private Document xmlDoc;
    
    // 构造方法
    public BusinessXmlCreator(EwaConfigLoader configLoader, Table table) {
        this.configLoader = configLoader;
        this.table = table;
    }
    
    // 核心方法
    public Document create(String frameType, String templateName) {
        this.frameType = frameType;
        this.templateName = templateName;
        
        // 1. 创建根节点 EasyWebTemplates
        xmlDoc = UXml.createBlankDocument();
        Element root = xmlDoc.createElement("EasyWebTemplates");
        xmlDoc.appendChild(root);
        
        // 2. 创建 EasyWebTemplate 节点
        Element template = xmlDoc.createElement("EasyWebTemplate");
        template.setAttribute("Name", table.getName() + "." + frameType + "." + templateName);
        template.setAttribute("CreateDate", getCurrentDateTime());
        root.appendChild(template);
        
        // 3. 创建 Page 节点（根据模板）
        createPageNode(template);
        
        // 4. 创建 Action 节点（根据模板）
        createActionNode(template);
        
        // 5. 创建 XItems 节点
        createXItemsNode(template);
        
        // 6. 创建其他节点（Menus, Charts, PageInfos 等）
        createOtherNodes(template);
        
        return xmlDoc;
    }
    
    public String createXmlString(String frameType, String templateName) {
        Document doc = create(frameType, templateName);
        return UXml.asXml(doc);
    }
    
    // 配置方法
    public void setFrameType(String frameType);
    public void setTemplateName(String templateName);
    
    // 验证方法
    public ValidationResult validate() {
        XmlValidator validator = new XmlValidator(configLoader);
        return validator.validate(xmlDoc);
    }
    
    // 辅助方法
    private void createPageNode(Element parent) { ... }
    private void createActionNode(Element parent) { ... }
    private void createXItemsNode(Element parent) { ... }
    private void createOtherNodes(Element parent) { ... }
}
```

---

#### 任务 2.3.2: XItemCreator.java
**目标**: 创建 XItem 节点

```java
package com.gdxsoft.easyweb.define.bussinessXmlCreator.creator;

import com.gdxsoft.easyweb.define.bussinessXmlCreator.config.*;
import com.gdxsoft.easyweb.define.database.Field;
import com.gdxsoft.easyweb.define.bussinessXmlCreator.util.FieldMetadataExt;
import com.gdxsoft.easyweb.utils.UXml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XItemCreator {
    private EwaConfigLoader configLoader;
    private Field field;
    private Document doc;
    
    // 构造方法
    public XItemCreator(EwaConfigLoader configLoader, Field field, Document doc) {
        this.configLoader = configLoader;
        this.field = field;
        this.doc = doc;
    }
    
    // 核心方法
    public Element createXItem() {
        Element xItem = doc.createElement("XItem");
        xItem.setAttribute("Name", field.getName());
        
        // 推断字段类型
        String tag = FieldMetadataExt.getTag(field, "Frame");
        String dataType = FieldMetadataExt.getDataType(field);
        String valid = FieldMetadataExt.getValid(field);
        String format = FieldMetadataExt.getFormat(field);
        
        // 创建各参数节点
        createTagNode(xItem, tag);
        createNameNode(xItem);
        createDescriptionSetNode(xItem);
        createDataItemNode(xItem, dataType, valid, format);
        createIsMustInputNode(xItem);
        createMaxMinLengthNode(xItem);
        
        // 如果是外键，创建 List 节点
        if (field.getName().endsWith("_ID")) {
            createListNode(xItem);
        }
        
        return xItem;
    }
    
    // 创建 Tag 节点
    private void createTagNode(Element parent, String tag) {
        Element tagElem = doc.createElement("Tag");
        Element setElem = doc.createElement("Set");
        setElem.setAttribute("Tag", tag);
        setElem.setAttribute("IsLFEdit", "0");
        tagElem.appendChild(setElem);
        parent.appendChild(tagElem);
    }
    
    // 创建 Name 节点
    private void createNameNode(Element parent) {
        Element nameElem = doc.createElement("Name");
        Element setElem = doc.createElement("Set");
        setElem.setAttribute("Name", field.getName());
        nameElem.appendChild(setElem);
        parent.appendChild(nameElem);
    }
    
    // 创建 DescriptionSet 节点（多语言）
    private void createDescriptionSetNode(Element parent) {
        Element descSetElem = doc.createElement("DescriptionSet");
        
        // 解析注释（中文 | 英文格式）
        String[] comments = parseComment(field.getComment());
        String zhComment = comments[0];
        String enComment = comments.length > 1 ? comments[1] : zhComment;
        
        // 中文
        Element setZh = doc.createElement("Set");
        setZh.setAttribute("Info", zhComment);
        setZh.setAttribute("Lang", "zhcn");
        setZh.setAttribute("Memo", "");
        descSetElem.appendChild(setZh);
        
        // 英文
        Element setEn = doc.createElement("Set");
        setEn.setAttribute("Info", enComment);
        setEn.setAttribute("Lang", "enus");
        setEn.setAttribute("Memo", "");
        descSetElem.appendChild(setEn);
        
        parent.appendChild(descSetElem);
    }
    
    // 创建 DataItem 节点
    private void createDataItemNode(Element parent, String dataType, String valid, String format) {
        Element dataItemElem = doc.createElement("DataItem");
        Element setElem = doc.createElement("Set");
        
        // DataField - 必须来自字段名
        setElem.setAttribute("DataField", field.getName());
        
        // DataType - 验证是否在 XGroupValue 中定义
        XGroupValue dataTypeValues = configLoader.getGroupValue("DataType");
        if (dataTypeValues != null && dataTypeValues.containsValue(dataType)) {
            setElem.setAttribute("DataType", dataType);
        } else {
            setElem.setAttribute("DataType", "String");  // 默认值
        }
        
        // Format - 验证是否在 XGroupValue 中定义
        XGroupValue formatValues = configLoader.getGroupValue("Format");
        if (formatValues != null && formatValues.containsValue(format)) {
            setElem.setAttribute("Format", format);
        }
        
        // Valid - 验证是否在 XGroupValue 中定义
        XGroupValue validValues = configLoader.getGroupValue("Valid");
        if (validValues != null && validValues.containsValue(valid)) {
            setElem.setAttribute("Valid", valid);
        }
        
        dataItemElem.appendChild(setElem);
        parent.appendChild(dataItemElem);
    }
    
    // 创建 IsMustInput 节点
    private void createIsMustInputNode(Element parent) {
        Element isMustInputElem = doc.createElement("IsMustInput");
        Element setElem = doc.createElement("Set");
        
        // 如果字段不允许为空，则设为必填
        String isMust = field.isNullable() ? "0" : "1";
        setElem.setAttribute("IsMustInput", isMust);
        
        isMustInputElem.appendChild(setElem);
        parent.appendChild(isMustInputElem);
    }
    
    // 创建 MaxMinLength 节点
    private void createMaxMinLengthNode(Element parent) {
        Element maxMinLengthElem = doc.createElement("MaxMinLength");
        Element setElem = doc.createElement("Set");
        setElem.setAttribute("MaxLength", String.valueOf(field.getLength()));
        setElem.setAttribute("MinLength", "");
        maxMinLengthElem.appendChild(setElem);
        parent.appendChild(maxMinLengthElem);
    }
    
    // 创建 List 节点（外键）
    private void createListNode(Element parent) {
        // 根据外键表名生成 List SQL
        String fkTableName = getFkTableName(field.getName());
        
        Element listElem = doc.createElement("List");
        Element setElem = doc.createElement("Set");
        setElem.setAttribute("Sql", "SELECT * FROM " + fkTableName + " WHERE 1=1");
        setElem.setAttribute("ValueField", fkTableName + "_ID");
        setElem.setAttribute("DisplayField", fkTableName + "_NAME");
        listElem.appendChild(setElem);
        parent.appendChild(listElem);
    }
    
    // 辅助方法
    private String[] parseComment(String comment) {
        if (comment == null) {
            return new String[]{"", ""};
        }
        if (comment.contains("|")) {
            return comment.split("\\|");
        }
        return new String[]{comment.trim(), comment.trim()};
    }
    
    private String getFkTableName(String fieldName) {
        // 例如：CRM_COM_ID → CRM_COM
        if (fieldName.endsWith("_ID")) {
            return fieldName.substring(0, fieldName.length() - 3);
        }
        return fieldName;
    }
}
```

---

#### 任务 2.3.3: NodeCreator.java
**目标**: 通用节点创建器

```java
package com.gdxsoft.easyweb.define.bussinessXmlCreator.creator;

import com.gdxsoft.easyweb.define.bussinessXmlCreator.config.XGroupValue;
import com.gdxsoft.easyweb.define.bussinessXmlCreator.config.XItemParameter;
import com.gdxsoft.easyweb.utils.UXml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class NodeCreator {
    private Document doc;
    private EwaConfigLoader configLoader;
    
    public NodeCreator(Document doc, EwaConfigLoader configLoader) {
        this.doc = doc;
        this.configLoader = configLoader;
    }
    
    // 创建 Set 结构节点（多语言）
    public Element createSetNode(String paramName, Map<String, String> values) {
        XItemParameter param = configLoader.getParameter(paramName);
        if (param == null || !param.isSet()) {
            throw new IllegalArgumentException("Parameter " + paramName + " is not a Set type");
        }
        
        Element setParent = doc.createElement(paramName + "Set");
        
        for (Map.Entry<String, String> entry : values.entrySet()) {
            Element set = doc.createElement("Set");
            set.setAttribute("Lang", entry.getKey());
            set.setAttribute("Info", entry.getValue());
            setParent.appendChild(set);
        }
        
        return setParent;
    }
    
    // 创建单值节点
    public Element createSingleNode(String paramName, String value) {
        XItemParameter param = configLoader.getParameter(paramName);
        Element setElem = doc.createElement("Set");
        
        if (param != null) {
            // 验证值是否在 XGroupValue 中定义
            XGroupValue groupValue = configLoader.getGroupValue(paramName);
            if (groupValue != null && !groupValue.containsValue(value)) {
                // 值不在预定义列表中，使用默认值或抛出警告
                value = groupValue.getDefaultValue();
            }
        }
        
        setElem.setAttribute(paramName, value);
        return setElem;
    }
    
    // 创建 DescriptionSet 节点
    public Element createDescriptionSet(String infoZh, String infoEn) {
        Element descSetElem = doc.createElement("DescriptionSet");
        
        Element setZh = doc.createElement("Set");
        setZh.setAttribute("Lang", "zhcn");
        setZh.setAttribute("Info", infoZh);
        descSetElem.appendChild(setZh);
        
        Element setEn = doc.createElement("Set");
        setEn.setAttribute("Lang", "enus");
        setEn.setAttribute("Info", infoEn);
        descSetElem.appendChild(setEn);
        
        return descSetElem;
    }
}
```

---

#### 任务 2.3.4: ValueResolver.java
**目标**: 解析参数值

```java
package com.gdxsoft.easyweb.define.bussinessXmlCreator.creator;

import com.gdxsoft.easyweb.define.bussinessXmlCreator.config.*;
import com.gdxsoft.easyweb.define.database.Field;

public class ValueResolver {
    private EwaConfigLoader configLoader;
    private Field field;
    
    public ValueResolver(EwaConfigLoader configLoader, Field field) {
        this.configLoader = configLoader;
        this.field = field;
    }
    
    // 解析参数值
    public String resolve(String paramName, ParameterValue paramValue) {
        String type = paramValue.getType();
        String createValue = paramValue.getCreateValue();
        
        if ("string".equals(type)) {
            return resolveStringValue(createValue);
        } else if ("int".equals(type)) {
            return resolveIntValue(createValue);
        } else if ("group".equals(type)) {
            return resolveGroupValue(paramName);
        } else if ("ref".equals(type)) {
            return resolveRefValue(paramValue.getRef());
        }
        
        return "";
    }
    
    // 从 XGroupValue 获取默认值
    public String getDefaultValue(String paramName) {
        XGroupValue groupValue = configLoader.getGroupValue(paramName);
        if (groupValue != null) {
            return groupValue.getDefaultValue();
        }
        return "";
    }
    
    // 辅助方法
    private String resolveStringValue(String createValue) {
        // 解析 CreateValue 表达式
        if (createValue.contains("this.Field")) {
            return resolveFieldProperty(createValue);
        }
        return createValue;
    }
    
    private String resolveFieldProperty(String createValue) {
        // 例如：this.Field.Name → field.getName()
        if (createValue.contains(".Name")) {
            return field.getName();
        }
        if (createValue.contains(".Description")) {
            return field.getComment();
        }
        return "";
    }
    
    private String resolveGroupValue(String paramName) {
        // 根据字段特征从 XGroupValue 中选择合适的值
        return FieldMetadataExt.getDataType(field);
    }
    
    private String resolveRefValue(String ref) {
        // 解析引用路径
        return "";
    }
}
```

---

### 2.4 阶段四：验证层 (validator 包)

#### 任务 2.4.1: XmlValidator.java
**目标**: 验证生成的 XML 是否符合规则

```java
package com.gdxsoft.easyweb.define.bussinessXmlCreator.validator;

import com.gdxsoft.easyweb.define.bussinessXmlCreator.config.*;
import com.gdxsoft.easyweb.utils.UXml;
import org.w3c.dom.*;

public class XmlValidator {
    private EwaConfigLoader configLoader;
    private ValidationResult result;
    
    public XmlValidator(EwaConfigLoader configLoader) {
        this.configLoader = configLoader;
    }
    
    // 验证 XML
    public ValidationResult validate(Document xmlDoc) {
        result = new ValidationResult();
        
        // 1. 验证所有节点
        validateNodes(xmlDoc.getDocumentElement());
        
        // 2. 验证所有属性值
        validateAttributes(xmlDoc.getDocumentElement());
        
        return result;
    }
    
    // 验证节点
    private void validateNodes(Element element) {
        String nodeName = element.getNodeName();
        
        // 检查节点是否在 XItemParameters 中定义
        // 注意：某些节点如 EasyWebTemplates, EasyWebTemplate, Page 等是根节点，不需要验证
        if (isConfigNode(nodeName) && !configLoader.hasParameter(nodeName)) {
            result.addError(new ValidationError(
                nodeName, null, null,
                "未定义的节点：" + nodeName,
                getElementPath(element),
                ValidationError.ErrorLevel.ERROR
            ));
        }
        
        // 递归验证子节点
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                validateNodes((Element) children.item(i));
            }
        }
    }
    
    // 验证属性值
    private void validateAttributes(Element element) {
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr attr = (Attr) attrs.item(i);
            String paramName = attr.getName();
            String value = attr.getValue();
            
            // 检查属性值是否在 XGroupValue 中定义
            XGroupValue groupValue = configLoader.getGroupValue(paramName);
            if (groupValue != null && !groupValue.containsValue(value)) {
                result.addError(new ValidationError(
                    element.getNodeName(),
                    paramName,
                    value,
                    "无效的属性值：" + paramName + "=" + value,
                    getElementPath(element),
                    ValidationError.ErrorLevel.ERROR
                ));
            }
        }
        
        // 递归验证子节点
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                validateAttributes((Element) children.item(i));
            }
        }
    }
    
    // 判断是否是配置节点
    private boolean isConfigNode(String nodeName) {
        // 根节点不需要验证
        String[] rootNodes = {"EasyWebTemplates", "EasyWebTemplate", "Page", "Action", 
                              "XItems", "Menus", "Charts", "PageInfos"};
        for (String root : rootNodes) {
            if (nodeName.equals(root)) {
                return false;
            }
        }
        return true;
    }
    
    // 获取元素路径
    private String getElementPath(Element element) {
        StringBuilder path = new StringBuilder();
        Node parent = element;
        while (parent != null) {
            if (path.length() > 0) {
                path.insert(0, "/");
            }
            path.insert(0, parent.getNodeName());
            parent = parent.getParentNode();
        }
        return path.toString();
    }
}
```

---

#### 任务 2.4.2: ValidationResult.java
**目标**: 验证结果

```java
package com.gdxsoft.easyweb.define.bussinessXmlCreator.validator;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {
    private boolean valid = true;
    private List<ValidationError> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    
    public void addError(ValidationError error) {
        errors.add(error);
        if (error.getLevel() == ValidationError.ErrorLevel.ERROR) {
            valid = false;
        } else {
            warnings.add(error.getMessage());
        }
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public List<ValidationError> getErrors() {
        return errors;
    }
    
    public List<String> getWarnings() {
        return warnings;
    }
    
    public String getErrorMessage() {
        StringBuilder sb = new StringBuilder();
        for (ValidationError error : errors) {
            sb.append(error.getMessage()).append("\n");
        }
        return sb.toString();
    }
}
```

---

#### 任务 2.4.3: ValidationError.java
**目标**: 验证错误

```java
package com.gdxsoft.easyweb.define.bussinessXmlCreator.validator;

public class ValidationError {
    private String nodeName;
    private String attributeName;
    private String attributeValue;
    private String message;
    private String xpath;
    private ErrorLevel level;
    
    public enum ErrorLevel {
        ERROR,
        WARNING
    }
    
    // 构造方法、getter、setter
}
```

---

### 2.5 阶段五：工具类 (util 包)

#### 任务 2.5.1: XmlBuilder.java
**目标**: XML 构建工具（基于 UXml）

```java
package com.gdxsoft.easyweb.define.bussinessXmlCreator.util;

import com.gdxsoft.easyweb.utils.UXml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlBuilder {
    
    // 创建空白文档
    public static Document createDocument() {
        return UXml.createBlankDocument();
    }
    
    // 创建元素
    public static Element createElement(Document doc, String tagName) {
        return doc.createElement(tagName);
    }
    
    // 创建带属性的元素
    public static Element createElement(Document doc, String tagName, String attrName, String attrValue) {
        Element elem = doc.createElement(tagName);
        elem.setAttribute(attrName, attrValue);
        return elem;
    }
    
    // 创建带多个属性的元素
    public static Element createElement(Document doc, String tagName, java.util.Map<String, String> attributes) {
        Element elem = doc.createElement(tagName);
        for (java.util.Map.Entry<String, String> entry : attributes.entrySet()) {
            elem.setAttribute(entry.getKey(), entry.getValue());
        }
        return elem;
    }
    
    // 添加 CDATA
    public static void addCData(Element parent, String content) {
        // UXml 没有直接的 CDATA 方法，使用标准 DOM API
        org.w3c.dom.CDATASection cdata = parent.getOwnerDocument().createCDATASection(content);
        parent.appendChild(cdata);
    }
    
    // 格式化 XML
    public static String formatXml(Document doc) {
        return UXml.asXml(doc);
    }
    
    // 保存 XML 到文件
    public static void saveToFile(Document doc, String filePath) {
        UXml.saveDocument(doc, filePath);
    }
    
    // 从文件加载 XML
    public static Document loadFromFile(String filePath) {
        return UXml.retDocument(filePath);
    }
}
```

---

## 3. 开发优先级

### 优先级 P0 (核心功能)
- [ ] 任务 2.1.1: BusinessXmlCreator.java
- [ ] 任务 2.1.2: XItemCreator.java
- [ ] 任务 2.2.1: FieldMetadataExt.java (字段元数据扩展)
- [ ] 任务 2.3.1: XmlBuilder.java

### 优先级 P1 (验证功能)
- [ ] 任务 2.4.1: XmlValidator.java
- [ ] 任务 2.4.2: ValidationResult.java
- [ ] 任务 2.4.3: ValidationError.java

### 优先级 P2 (增强功能)
- [ ] 任务 2.1.3: NodeCreator.java

---

## 4. 入口参数与保存路径

### 4.1 入口参数

```java
import org.json.JSONObject;

public class BusinessXmlCreateParams {
    private String db;              // 数据库连接配置名 (e.g. "globaltravel")
    private String tableName;       // 表名 (e.g. "CRM_COM") - 与 selectSql/tableJson 二选一
    private String selectSql;       // SELECT 语句 (e.g. "SELECT * FROM CRM_COM WHERE 1=1") - 与 tableName/tableJson 二选一
    private JSONObject tableJson;   // 表 JSON 对象 (org.json.JSONObject) - 与 tableName/selectSql 二选一
    private String frameType;       // Frame 类型 (Frame, ListFrame, Tree)
    private String operationType;   // 操作类型 (NM, V, M)
    private String savePath;        // 保存路径模式 (file, jdbc)
    private String outputPath;      // 输出路径或 XML 名称
    
    // 构造方法 - 使用表名
    public BusinessXmlCreateParams(String db, String tableName, 
                                   String frameType, String operationType) {
        this.db = db;
        this.tableName = tableName;
        this.frameType = frameType;
        this.operationType = operationType;
        // 自动生成 outputPath
        this.outputPath = generateOutputPath();
    }
    
    // 构造方法 - 使用 SELECT 语句
    public BusinessXmlCreateParams(String db, String selectSql, 
                                   String frameType, String operationType) {
        this.db = db;
        this.selectSql = selectSql;
        this.frameType = frameType;
        this.operationType = operationType;
        // 从 SELECT 语句中提取表名用于生成 outputPath
        this.tableName = extractTableNameFromSql(selectSql);
        this.outputPath = generateOutputPath();
    }
    
    // 构造方法 - 使用表 JSON 对象（org.json.JSONObject）
    public BusinessXmlCreateParams(String db, JSONObject tableJson, 
                                   String frameType, String operationType) {
        this.db = db;
        this.tableJson = tableJson;
        this.frameType = frameType;
        this.operationType = operationType;
        // 从 tableJson 中提取表名
        this.tableName = extractTableNameFromJson(tableJson);
        this.outputPath = generateOutputPath();
    }
    
    // 验证参数
    public boolean validate() {
        // tableName、selectSql、tableJson 必须三选一
        int count = 0;
        if (tableName != null && !tableName.trim().isEmpty()) count++;
        if (selectSql != null && !selectSql.trim().isEmpty()) count++;
        if (tableJson != null) count++;
        
        if (count != 1) {
            return false;
        }
        
        // 如果提供了 selectSql，检查是否为 SELECT 语句
        if (selectSql != null && !selectSql.trim().toUpperCase().startsWith("SELECT")) {
            return false;
        }
        
        // 如果提供了 tableJson，检查是否为有效的 JSON 对象
        if (tableJson != null && !tableJson.has("TableName")) {
            return false;
        }
        
        return true;
    }
    
    // 生成输出路径
    private String generateOutputPath() {
        // 格式 1: 文件路径 /bussiness/group/op.xml
        // 格式 2: XML 名称 |bussiness|group|op.xml
        String groupPath = "/bussiness/";
        String opName = tableName + "." + getFrameTypeShort() + "." + operationType;
        
        if (savePath != null && savePath.startsWith("|")) {
            // XML 名称模式：|bussiness|group|op.xml
            return groupPath.replace("/", "|") + opName + ".xml";
        } else {
            // 文件路径模式：/bussiness/group/op.xml
            return groupPath + opName + ".xml";
        }
    }
    
    // 从 SELECT 语句中提取表名（简单实现）
    private String extractTableNameFromSql(String sql) {
        // 简单解析：SELECT ... FROM table_name ...
        Pattern pattern = Pattern.compile("FROM\\s+([\\w\\.]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            String fullName = matcher.group(1);
            // 如果有 schema.table 格式，只取 table 部分
            if (fullName.contains(".")) {
                return fullName.split("\\.")[1];
            }
            return fullName;
        }
        return "UNKNOWN_TABLE";
    }
    
    // 从 tableJson 中提取表名
    private String extractTableNameFromJson(JSONObject tableJson) {
        if (tableJson != null && tableJson.has("TableName")) {
            return tableJson.getString("TableName");
        }
        return "UNKNOWN_TABLE";
    }
    
    // Frame 类型简写
    private String getFrameTypeShort() {
        switch (frameType.toUpperCase()) {
            case "FRAME": return "F";
            case "LISTFRAME": return "LF";
            case "TREE": return "T";
            default: return frameType;
        }
    }
    
    // Getters and Setters
    public String getDb() { return db; }
    public String getTableName() { return tableName; }
    public String getSelectSql() { return selectSql; }
    public JSONObject getTableJson() { return tableJson; }
    public String getFrameType() { return frameType; }
    public String getOperationType() { return operationType; }
    public String getOutputPath() { return outputPath; }
}
```

### 4.1.1 表 JSON 对象格式

**Table 类新增 toJson() 方法**:

```java
// com.gdxsoft.easyweb.define.database.Table.java
public JSONObject toJson() {
    JSONObject json = new JSONObject();
    
    // 表属性
    json.put("TableName", this._Name);
    json.put("SchemaName", this._SchemaName);
    json.put("DatabaseType", this._DatabaseType);
    
    // 字段列表
    JSONArray fieldsJson = new JSONArray();
    for (Field f : this._Fields) {
        JSONObject fieldJson = fieldToJson(f);
        fieldsJson.put(fieldJson);
    }
    json.put("Fields", fieldsJson);
    
    // 主键、外键、索引...
    return json;
}
```

**使用示例**:
```java
// 从数据库加载表元数据
Tables tables = new Tables();
tables.loadFromDatabase("globaltravel");
Table table = tables.getTable("CRM_COM");

// 转换为 JSON
JSONObject tableJson = table.toJson();
String tableJsonStr = tableJson.toString(2);  // 格式化输出
```

**JSON 输出格式**:
```json
{
  "TableName": "CRM_COM",
  "SchemaName": "dbo",
  "DatabaseType": "MSSQL",
  "ConnectionConfigName": "globaltravel",
  "Fields": [
    {
      "Name": "CRM_COM_ID",
      "Type": "INT",
      "Length": 0,
      "IsPrimaryKey": true,
      "IsIdentity": true,
      "Nullable": false,
      "Comment": "公司 ID"
    },
    {
      "Name": "CRM_COM_NAME",
      "Type": "NVARCHAR",
      "Length": 200,
      "IsPrimaryKey": false,
      "Nullable": false,
      "Comment": "公司名称"
    }
  ],
  "Pk": {
    "Fields": ["CRM_COM_ID"]
  },
  "Fks": [],
  "Indexes": []
}
```

**字段属性** (`Field`):
| 属性 | 说明 | 示例 |
|-----|------|------|
| `Name` | 字段名 | `CRM_COM_ID` |
| `Type` | 数据库类型 | `INT`, `NVARCHAR`, `DATETIME` |
| `Length` | 长度 | `200` |
| `IsPrimaryKey` | 是否主键 | `true/false` |
| `IsIdentity` | 是否自增 | `true/false` |
| `Nullable` | 是否允许空 | `true/false` |
| `Comment` | 注释 | `公司 ID` |

### 4.1.2 SQL 语法检查

使用 `SqlSyntaxCheck` 类检查 SELECT 语句语法：

```java
public class SqlValidator {
    
    /**
     * 检查 SELECT 语句语法
     * @param db 数据库连接配置名
     * @param selectSql SELECT 语句
     * @return 检查结果 {RST: true/false, ERR: 错误信息}
     */
    public static JSONObject checkSqlSyntax(String db, String selectSql) {
        RequestValue rv = new RequestValue();
        rv.addOrUpdateValue("DB", db);
        rv.addOrUpdateValue("SQL", selectSql);
        
        SqlSyntaxCheck syntaxCheck = new SqlSyntaxCheck(rv);
        String result = syntaxCheck.checkSyntax();
        
        return new JSONObject(result);
    }
    
    /**
     * 验证并获取字段元数据
     * @param params 参数
     * @return 验证结果
     */
    public static ValidationResult validateSelectSql(BusinessXmlCreateParams params) {
        ValidationResult result = new ValidationResult();
        
        // 1. 使用 SqlUtils 检查是否为 SELECT 语句（支持 WITH 语句）
        if (!SqlUtils.checkIsSelect(params.getSelectSql())) {
            result.addError("不是 SELECT 语句");
            return result;
        }
        
        // 2. 检查 SQL 语法
        JSONObject checkResult = checkSqlSyntax(params.getDb(), params.getSelectSql());
        
        if (!checkResult.getBoolean("RST")) {
            result.addError(checkResult.getString("ERR"));
            return result;
        }
        
        // 3. 执行 SELECT 获取字段元数据（使用 WHERE 1=2 不返回数据）
        String testSql = "SELECT * FROM (" + params.getSelectSql() + ") temp WHERE 1=2";
        DTTable tb = DTTable.getJdbcTable(testSql, params.getDb());
        
        if (!tb.isOk()) {
            result.addError(tb.getErrorInfo());
            return result;
        }
        
        // 4. 验证字段元数据
        for (int i = 0; i < tb.getColumns().getCount(); i++) {
            DTColumn col = tb.getColumns().getColumn(i);
            // 可以添加字段验证逻辑
        }
        
        result.setSuccess(true);
        return result;
    }
    
    /**
     * 从 SELECT 语句中提取表名（支持 WITH 语句）
     * @param selectSql SELECT 语句
     * @return 表名
     */
    public static String extractTableNameFromSql(String selectSql) {
        // 1. 使用 SqlUtils 去除 WITH 语句块，获取主 SELECT 语句
        String[] withBlocks = SqlUtils.getSqlWithBlock(selectSql);
        String mainSql = (withBlocks != null) ? withBlocks[1] : selectSql;
        
        // 2. 从主查询中提取第一个表名
        // 简单解析：SELECT ... FROM table_name ...
        Pattern pattern = Pattern.compile("FROM\\s+([\\w\\.]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(mainSql);
        if (matcher.find()) {
            String fullName = matcher.group(1);
            // 如果有 schema.table 格式，只取 table 部分
            if (fullName.contains(".")) {
                return fullName.split("\\.")[1];
            }
            return fullName;
        }
        
        return "UNKNOWN_TABLE";
    }
}
```

**SqlUtils 提供的方法**:
| 方法 | 说明 |
|-----|------|
| `checkIsSelect(sql)` | 检查是否为 SELECT 语句（支持 WITH 语句） |
| `getSqlWithBlock(sql)` | 分离 WITH 块和主 SELECT 语句 |
| `removeSqlMuitiComment(sql)` | 删除多行注释 `/* */` |
| `checkStartWord(sql, word)` | 检查 SQL 是否以特定关键字开头 |

### 4.2 保存路径说明

#### 4.2.1 文件路径模式

```
/bussiness/group/op.xml
```

**示例**:
- `/bussiness/crm/CRM_COM.F.NM.xml` - 公司信息新增/修改表单
- `/bussiness/crm/CRM_COM.LF.M.xml` - 公司信息列表管理
- `/bussiness/hr/ADM_USER.T.M.xml` - 用户树管理

**保存代码**:
```java
// 使用 UPath.getScriptPath() 获取脚本根路径
String fullPath = UPath.getScriptPath() + params.getOutputPath();

// 创建目录（如果不存在）
File outFile = new File(fullPath);
if (!outFile.getParentFile().exists()) {
    outFile.getParentFile().mkdirs();
}

// 保存 XML
UXml.saveDocument(xmlDoc, fullPath);
```

#### 4.2.2 XML 名称模式

```
|bussiness|group|op.xml
```

**说明**: 使用 `|` 分隔的路径，表示 XML 配置名称，存储在数据库中

**保存代码**:
```java
// 使用 JdbcConfigOperation 保存到数据库
JdbcConfigOperation op = new JdbcConfigOperation();
op.init(dbConfigName);

// XML 名称：|bussiness|group|CRM_COM.F.NM.xml
String xmlName = params.getOutputPath();

// 项名称：CRM_COM.F.NM
String itemName = params.getTableName() + "." + 
                  params.getFrameTypeShort() + "." + 
                  params.getOperationType();

// 保存到数据库
op.updateItem(xmlName, itemName, UXml.asXml(xmlDoc), admId);
```

### 4.3 参考代码

#### 4.3.1 IUpdateXml 接口

**位置**: `com.gdxsoft.easyweb.define.IUpdateXml`

**核心方法**:
```java
public interface IUpdateXml {
    // 保存 XML
    boolean saveXml(String itemName, String xml);
    
    // 更新配置项
    boolean updateItem(String itemName, String xml);
    boolean updateItem(String itemName, String xml, boolean isUpdateTime);
    
    // 写入文档
    boolean writeXml(Document doc);
    
    // 查询配置项
    Node queryItem(String itemName);
    String queryItemXml(String itemName);
    
    // 删除配置项
    boolean removeItem(String itemName);
    boolean removeItems(String itemNames);
    
    // 复制配置项
    boolean copyItem(String souceItemName, String newItemName);
    
    // 重命名
    boolean renameItem(String xmlName, String itemName, String newItemName);
    
    // 备份与恢复
    void saveBackup();
    void recoverFile();
    
    // 文件路径
    void setXmlFilePath(String xmlFilePath);
    String getXmlFilePath();
    
    // 管理员
    void setAdmin(String amdId);
    String getAdmin();
}
```

**实现类**:
| 实现类 | 说明 | 保存方式 |
|-------|------|---------|
| `UpdateXmlImpl` | 基于文件的 XML 更新 | 文件系统 |
| `UpdateXmlJdbcImpl` | 基于数据库的 XML 更新 | JDBC 数据库 |

#### 4.3.3 ServletXml.handleSave (参考)

```java
// com.gdxsoft.easyweb.define.servlets.ServletXml.java
private boolean handleSave(RequestValue rv, PageValue pvAdmin) {
    String xmlName = rv.getString(FrameParameters.XMLNAME);
    String itemName = rv.getString(FrameParameters.ITEMNAME);
    String xml = rv.getString("XML");
    
    IUpdateXml up = this.getUpdateXml(xmlName, pvAdmin.getStringValue());
    return up.updateItem(itemName, xml);
}
```

#### 4.3.4 UpdateXmlImpl.updateItem (文件保存参考)

```java
// com.gdxsoft.easyweb.define.UpdateXmlImpl.java
@Override
public boolean updateItem(String itemName, String xml, boolean isUpdateTime) {
    // 1. 查询是否已存在
    Node curNode = this.queryItem(itemName);
    if (curNode != null) {
        saveBackup();  // 保存备份
    }

    // 2. 修正 XML（设置 Name 属性等）
    xml = super.fixXml(xml, itemName);

    // 3. 追加到文档
    Document newDoc = UXml.appendNode(this._Document, xml, this._RootUri);

    // 4. 更新时间
    if (isUpdateTime) {
        this.updateTime(curNode);
    }

    // 5. 保存到文件
    return writeXml(newDoc);
}

@Override
public boolean writeXml(Document doc) {
    String path = super.getXmlFilePath();
    boolean rst = UXml.saveDocument(doc, path);
    LOGGER.info("SAVE(" + rst + "): " + path);
    return rst;
}
```

#### 4.3.5 JdbcConfigOperation.updateItem (数据库保存参考)

```java
// com.gdxsoft.easyweb.script.userConfig.JdbcConfigOperation.java
public void updateItem(String xmlname, String itemname, String xmlStr, String adm) {
    // 1. 计算哈希和 MD5
    int hashCode = xmlStr.hashCode();
    String md5 = Utils.md5(xmlStr);
    
    // 2. 检查是否已存在
    boolean exists = checkItemExists(xmlname, itemname);
    
    if (exists) {
        // 更新
        updateExistingItem(xmlname, itemname, xmlStr, adm, hashCode, md5);
    } else {
        // 新增
        insertNewItem(xmlname, itemname, xmlStr, adm, hashCode, md5);
    }
    
    // 3. 保存到缓存
    saveToCache(xmlName, xmlStr, hashCode, md5);
}
```

### 4.4 BusinessXmlCreator 保存方法

**说明**: 使用 `IUpdateXml` 接口保存，不关心底层是文件保存还是数据库保存。

```java
public class BusinessXmlCreator {
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
     * @param db 数据库连接配置名
     * @param tableName 表名（与 selectSql 二选一）
     * @param selectSql SELECT 语句（与 tableName 二选一）
     * @param tableJson 表 JSON 对象（与 tableName/selectSql 二选一）
     * @param frameType Frame 类型 (Frame, ListFrame, Tree)
     * @param operationType 操作类型 (NM, V, M)
     * @param xmlName XML 名称 (e.g. "/bussiness/crm/CRM_COM.F.NM.xml")
     * @param itemName 配置项名称 (e.g. "CRM_COM.F.NM")
     * @param admId 管理员 ID
     * @return 是否成功
     */
    public boolean createAndSave(
        String db,
        String tableName,
        String selectSql,
        JSONObject tableJson,
        String frameType,
        String operationType,
        String xmlName,
        String itemName,
        String admId
    ) {
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
     * 生成并保存业务 XML（使用表名）
     * @param tableName 表名
     * @param frameType Frame 类型
     * @param operationType 操作类型
     * @param xmlName XML 名称
     * @param itemName 配置项名称
     * @param admId 管理员 ID
     * @return 是否成功
     */
    public boolean createAndSave(
        String tableName,
        String frameType,
        String operationType,
        String xmlName,
        String itemName,
        String admId
    ) {
        return this.createAndSave(null, tableName, null, null, frameType, operationType, xmlName, itemName, admId);
    }
    
    /**
     * 生成并保存业务 XML（使用 SELECT 语句）
     * @param selectSql SELECT 语句
     * @param frameType Frame 类型
     * @param operationType 操作类型
     * @param xmlName XML 名称
     * @param itemName 配置项名称
     * @param admId 管理员 ID
     * @return 是否成功
     */
    public boolean createAndSave(
        String selectSql,
        String frameType,
        String operationType,
        String xmlName,
        String itemName,
        String admId
    ) {
        return this.createAndSave(null, null, selectSql, null, frameType, operationType, xmlName, itemName, admId);
    }
    
    /**
     * 生成并保存业务 XML（使用表 JSON 对象）
     * @param tableJson 表 JSON 对象
     * @param frameType Frame 类型
     * @param operationType 操作类型
     * @param xmlName XML 名称
     * @param itemName 配置项名称
     * @param admId 管理员 ID
     * @return 是否成功
     */
    public boolean createAndSave(
        JSONObject tableJson,
        String frameType,
        String operationType,
        String xmlName,
        String itemName,
        String admId
    ) {
        return this.createAndSave(null, null, null, tableJson, frameType, operationType, xmlName, itemName, admId);
    }
    
    /**
     * 生成业务 XML 并返回 XML 字符串（用于预览）
     * @param db 数据库连接配置名
     * @param tableName 表名（与 selectSql 二选一）
     * @param selectSql SELECT 语句（与 tableName 二选一）
     * @param tableJson 表 JSON 对象（与 tableName/selectSql 二选一）
     * @param frameType Frame 类型 (Frame, ListFrame, Tree)
     * @param operationType 操作类型 (NM, V, M)
     * @return XML 字符串，如果失败返回 null
     */
    public String createShowXml(
        String db,
        String tableName,
        String selectSql,
        JSONObject tableJson,
        String frameType,
        String operationType
    ) {
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
                ValidationResult vr = SqlValidator.validateSelectSql(params);
                if (!vr.isSuccess()) {
                    LOGGER.error("SQL 验证失败：" + vr.getErrors());
                    return null;
                }
            }
            
            // 4. 生成 XML
            this.xmlDoc = this.create(frameType, operationType);
            
            // 5. 使用 UserConfig 验证生成的 XML
            String itemName = params.getTableName() + "." + params.getFrameTypeShort() + "." + params.getOperationType();
            String xmlStr = UXml.asXml(this.xmlDoc.getFirstChild());
            try {
                UserConfig uc = UserConfig.createForValidation(params.getOutputPath(), itemName, xmlStr);
                LOGGER.info("UserConfig 验证成功");
            } catch (Exception e) {
                LOGGER.error("UserConfig 验证失败：" + e.getMessage());
                return null;
            }
            
            // 6. 返回格式化的 XML 字符串
            return UXml.asXmlPretty(this.xmlDoc);
        } catch (Exception e) {
            LOGGER.error("生成 XML 失败：" + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 生成业务 XML 并返回 XML 字符串（使用表名）
     * @param tableName 表名
     * @param frameType Frame 类型
     * @param operationType 操作类型
     * @return XML 字符串
     */
    public String createShowXml(
        String tableName,
        String frameType,
        String operationType
    ) {
        return this.createShowXml(null, tableName, null, null, frameType, operationType);
    }
    
    /**
     * 生成业务 XML 并返回 XML 字符串（使用 SELECT 语句）
     * @param selectSql SELECT 语句
     * @param frameType Frame 类型
     * @param operationType 操作类型
     * @return XML 字符串
     */
    public String createShowXml(
        String selectSql,
        String frameType,
        String operationType
    ) {
        return this.createShowXml(null, null, selectSql, null, frameType, operationType);
    }
    
    /**
     * 生成业务 XML 并返回 XML 字符串（使用表 JSON 对象）
     * @param tableJson 表 JSON 对象
     * @param frameType Frame 类型
     * @param operationType 操作类型
     * @return XML 字符串
     */
    public String createShowXml(
        JSONObject tableJson,
        String frameType,
        String operationType
    ) {
        return this.createShowXml(null, null, null, tableJson, frameType, operationType);
    }
    
    // ... 其他代码 ...
```

**使用示例**:

```java
// 1. 获取 EwaConfig 和 Table
EwaConfig config = EwaConfig.instance();
Tables tables = new Tables();
tables.loadFromDatabase("globaltravel");
Table table = tables.getTable("CRM_COM");

// 2. 创建 BusinessXmlCreator
BusinessXmlCreator creator = new BusinessXmlCreator(config, table);

// 3. 预览 XML（使用表名）
String xmlPreview = creator.createShowXml(
    "CRM_COM",           // tableName
    "Frame",             // frameType
    "NM"                 // operationType
);

if (xmlPreview != null) {
    // 显示 XML 预览
    System.out.println(xmlPreview);
    
    // 确认保存
    if (confirm("确认保存？")) {
        creator.save(
            "/bussiness/crm/CRM_COM.F.NM.xml",  // xmlName
            "CRM_COM.F.NM",                     // itemName
            "admin"                             // admId
        );
    }
}

// 4. 或者使用 SELECT 语句预览
xmlPreview = creator.createShowXml(
    "SELECT * FROM CRM_COM WHERE CRM_COM_ID > 0",  // selectSql
    "Frame",             // frameType
    "NM"                 // operationType
);

// 5. 或者使用表 JSON 对象预览
JSONObject tableJson = table.toJson();
xmlPreview = creator.createShowXml(
    tableJson,           // tableJson
    "Frame",             // frameType
    "NM"                 // operationType
);
```

**前端集成示例** (Servlet):

```java
@WebServlet("/api/businessXml/preview")
public class BusinessXmlPreviewServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        
        // 获取参数
        String tableName = request.getParameter("tableName");
        String selectSql = request.getParameter("selectSql");
        String frameType = request.getParameter("frameType");
        String operationType = request.getParameter("operationType");
        
        try {
            // 创建生成器
            EwaConfig config = EwaConfig.instance();
            Tables tables = new Tables();
            tables.loadFromDatabase("globaltravel");
            Table table = tables.getTable(tableName);
            BusinessXmlCreator creator = new BusinessXmlCreator(config, table);
            
            // 生成 XML 预览
            String xmlPreview;
            if (selectSql != null && !selectSql.trim().isEmpty()) {
                xmlPreview = creator.createShowXml(selectSql, frameType, operationType);
            } else {
                xmlPreview = creator.createShowXml(tableName, frameType, operationType);
            }
            
            if (xmlPreview != null) {
                // 返回成功
                JSONObject rst = new JSONObject();
                rst.put("RST", true);
                rst.put("XML", xmlPreview);
                response.getWriter().write(rst.toString());
            } else {
                // 返回失败
                JSONObject rst = new JSONObject();
                rst.put("RST", false);
                rst.put("ERR", "生成 XML 失败");
                response.getWriter().write(rst.toString());
            }
        } catch (Exception e) {
            JSONObject rst = new JSONObject();
            rst.put("RST", false);
            rst.put("ERR", e.getMessage());
            response.getWriter().write(rst.toString());
        }
    }
}
```

**使用示例**:

```java
// 1. 获取 EwaConfig 和 Table
EwaConfig config = EwaConfig.instance();
Tables tables = new Tables();
tables.loadFromDatabase("globaltravel");
Table table = tables.getTable("CRM_COM");

// 2. 创建 BusinessXmlCreator
BusinessXmlCreator creator = new BusinessXmlCreator(config, table);

// 3. 生成并保存（使用表名）
boolean success = creator.createAndSave(
    "CRM_COM",           // tableName
    "Frame",             // frameType
    "NM",                // operationType
    "/bussiness/crm/CRM_COM.F.NM.xml",  // xmlName
    "CRM_COM.F.NM",      // itemName
    "admin"              // admId
);

// 4. 或者使用 SELECT 语句
success = creator.createAndSave(
    "SELECT * FROM CRM_COM WHERE CRM_COM_ID > 0",  // selectSql
    "Frame",             // frameType
    "NM",                // operationType
    "|bussiness|crm|CRM_COM.F.NM.xml",  // xmlName (保存到数据库)
    "CRM_COM.F.NM",      // itemName
    "admin"              // admId
);

// 5. 或者使用表 JSON 对象
JSONObject tableJson = table.toJson();
success = creator.createAndSave(
    tableJson,           // tableJson
    "Frame",             // frameType
    "NM",                // operationType
    "/bussiness/crm/CRM_COM.F.NM.xml",  // xmlName
    "CRM_COM.F.NM",      // itemName
    "admin"              // admId
);
```

---

## 5. 使用示例

### 5.1 基本使用

```java
package com.gdxsoft.easyweb.define.bussinessXmlCreator.test;

import com.gdxsoft.easyweb.define.bussinessXmlCreator.config.EwaConfigLoader;
import com.gdxsoft.easyweb.define.bussinessXmlCreator.creator.BusinessXmlCreator;
import com.gdxsoft.easyweb.define.bussinessXmlCreator.validator.XmlValidator;
import com.gdxsoft.easyweb.define.bussinessXmlCreator.validator.ValidationResult;
import com.gdxsoft.easyweb.define.database.Tables;
import com.gdxsoft.easyweb.define.database.Table;
import com.gdxsoft.easyweb.utils.UXml;
import org.w3c.dom.Document;

public class TestBusinessXmlCreator {
    public static void main(String[] args) {
        // 1. 加载配置
        EwaConfigLoader configLoader = new EwaConfigLoader();
        configLoader.load("/system.xml/EwaConfig.xml");
        
        // 2. 加载表元数据（使用现有的 Tables 类）
        Tables tables = new Tables();
        tables.loadFromDatabase("globaltravel");
        Table table = tables.getTable("CRM_COM");
        
        // 3. 创建 XML
        BusinessXmlCreator creator = new BusinessXmlCreator(configLoader, table);
        Document xmlDoc = creator.create("Frame", "NM");
        
        // 4. 验证
        XmlValidator validator = new XmlValidator(configLoader);
        ValidationResult result = validator.validate(xmlDoc);
        
        if (!result.isValid()) {
            System.err.println("验证失败:");
            for (var error : result.getErrors()) {
                System.err.println("  " + error.getMessage());
            }
            return;
        }
        
        // 5. 保存
        UXml.saveDocument(xmlDoc, "/output/CRM_COM.F.NM.xml");
        System.out.println("生成成功！");
    }
}
```

### 5.2 批量生成

```java
// 为表生成所有类型的配置
String[] frameTypes = {"Frame", "ListFrame"};
String[] templates = {"NM", "V", "M"};

for (String frameType : frameTypes) {
    for (String template : templates) {
        BusinessXmlCreator creator = new BusinessXmlCreator(configLoader, table);
        Document xmlDoc = creator.create(frameType, template);
        
        String fileName = table.getName() + "." + frameType + "." + template + ".xml";
        UXml.saveDocument(xmlDoc, "/output/" + fileName);
        System.out.println("生成：" + fileName);
    }
}
```

---

## 5. 测试计划

### 5.1 单元测试

| 测试类 | 测试内容 |
|-------|---------|
| EwaConfigTest | 加载配置、获取 XItem、获取参数、获取全局值 |
| FieldMetadataExtTest | 类型推断、Tag 推断、Valid 推断 |
| XItemCreatorTest | 创建 XItem 节点、验证节点结构 |
| XmlValidatorTest | 验证合法 XML、验证非法 XML |

### 5.2 集成测试

| 测试类 | 测试内容 |
|-------|---------|
| BusinessXmlCreatorTest | 完整 XML 生成流程 |
| EndToEndTest | 从数据库表到 XML 文件的完整流程 |

### 5.3 测试数据

使用示例表进行测试：
- CRM_COM (公司信息表)
- ADM_USER (用户表)
- GRP_COSTUMER (客人表)

---

## 6. 验收标准

### 6.1 功能验收
- [ ] 能正确解析 EwaConfig.xml 的所有定义
- [ ] 能根据表元数据生成合法的 XItem 节点
- [ ] 所有节点名来自 XItemParameters 定义
- [ ] 所有属性值来自 XGroupValue 定义或字段元数据
- [ ] 生成的 XML 能通过验证器验证

### 6.2 质量验收
- [ ] 单元测试覆盖率 > 80%
- [ ] 无硬编码的节点名和属性值
- [ ] 代码符合项目编码规范
- [ ] 有完整的 JavaDoc 注释

### 6.3 性能验收
- [ ] 单个 XML 生成时间 < 100ms
- [ ] 内存占用 < 50MB
- [ ] 支持批量生成（100+ 表）

---

## 7. 风险与缓解

| 风险 | 影响 | 缓解措施 |
|-----|------|---------|
| EwaConfig.xml 结构复杂 | 高 | 先实现解析器，逐步验证每个节点 |
| 参数类型多样 | 中 | 使用策略模式处理不同类型 |
| 验证规则复杂 | 中 | 分阶段实现验证，先基础后复杂 |
| 性能问题 | 低 | 使用缓存，避免重复解析 |

---

## 8. 时间估算

| 阶段 | 任务数 | 估算时间 |
|-----|-------|---------|
| 阶段一：XML 创建层 | 3 | 5 天 |
| 阶段二：数据模型层 | 1 | 1 天 (使用现有类) |
| 阶段三：验证层 | 3 | 2 天 |
| 阶段四：工具类 | 1 | 1 天 |
| 测试与调试 | - | 5 天 |
| **总计** | **8** | **14 天** |

---

## 9. 参考资料

- `system.xml/EwaConfig.xml` - 配置规则模板
- `system.xml/EwaDefine.xml` - 系统模板定义
- `designs/tb2ewaCfg/TB2EWACFG_DESIGN.md` - 设计文档
- `designs/tb2ewaCfg/examples/*.xml` - 示例 XML 文件
- `com.gdxsoft.easyweb.define.database.Table` - 表元数据类
- `com.gdxsoft.easyweb.define.database.Field` - 字段元数据类
- `com.gdxsoft.easyweb.utils.UXml` - XML 工具类
