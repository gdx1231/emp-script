# 编码工作计划

> 基于 EwaConfig.xml 规则模板创建业务 XML

---

## Git 提交规范

每个大环节完成后执行 `git add` 和 `git commit`：

```bash
# 环节 1 完成后
git add .
git commit -m "feat: 创建通过 JSON 创建业务 XML 的功能"

# 环节 2 完成后
git add .
git commit -m "feat: 添加验证功能（SQL 语法检查和 UserConfig 验证）"

# 环节 3 完成后
git add .
git commit -m "test: 创建单元测试和集成测试用例"

# 环节 4 完成后
git add .
git commit -m "feat: Table 类扩展 toJson() 方法"

# 环节 5 完成后
git add .
git commit -m "feat: 集成 SqlUtils 支持复杂 SQL 语句"
```

---

## 1. JSON 创建业务 XML

### 1.1 核心类

| 类名 | 路径 | 说明 |
|-----|------|------|
| `BusinessXmlCreateParams` | `com.gdxsoft.easyweb.define.bussinessXmlCreator` | 入口参数 |
| `BusinessXmlCreator` | `com.gdxsoft.easyweb.define.bussinessXmlCreator` | 主创建器 |
| `XItemCreator` | `com.gdxsoft.easyweb.define.bussinessXmlCreator` | XItem 创建器 |
| `NodeCreator` | `com.gdxsoft.easyweb.define.bussinessXmlCreator` | 节点创建器 |

### 1.2 任务列表

- [ ] 1.1.1 `BusinessXmlCreateParams` - 入口参数类
  - 构造方法（tableName/selectSql/tableJson 三选一）
  - validate() 参数验证
  - getFrameTypeShort() Frame 类型简写
  
- [ ] 1.1.2 `BusinessXmlCreator` - 主创建器
  - createAndSave() 生成并保存
  - createShowXml() 生成预览
  - save() 保存（使用 IUpdateXml 接口）
  
- [ ] 1.1.3 `XItemCreator` - XItem 创建器
  - createXItem() 创建 XItem 节点
  - createTagNode() 创建 Tag 节点
  - createDataItemNode() 创建 DataItem 节点
  
- [ ] 1.1.4 `NodeCreator` - 节点创建器
  - createSetNode() 创建 Set 结构节点
  - createSingleNode() 创建单值节点
  - createDescriptionSet() 创建多语言描述

**依赖**: `EwaConfig.instance()`, `Table`, `UXml`

---

## 2. 验证

### 2.1 验证层次

```
参数验证 → SQL 语法检查 → UserConfig 验证
```

### 2.2 任务列表

- [ ] 2.1 `SqlValidator` - SQL 验证工具类
  - checkSqlSyntax() 使用 SqlSyntaxCheck 检查语法
  - validateSelectSql() 验证并获取字段元数据
  - extractTableNameFromSql() 提取表名（支持 WITH 语句）
  
- [ ] 2.2 `UserConfig` 扩展
  - createForValidation() 静态方法创建实例
  - setItemNode() 设置 XML 节点
  - setItemNodeXml() 设置 XML 字符串

**依赖**: `SqlSyntaxCheck`, `SqlUtils`, `UserConfig`

---

## 3. Test 用例

### 3.1 测试类

| 测试类 | 测试内容 |
|-------|---------|
| `BusinessXmlCreateParamsTest` | 参数验证、表名提取 |
| `SqlValidatorTest` | SQL 语法检查、表名提取 |
| `XItemCreatorTest` | XItem 节点创建 |
| `BusinessXmlCreatorTest` | 完整 XML 生成流程 |
| `UserConfigValidationTest` | UserConfig 验证 |

### 3.2 任务列表

- [ ] 3.1 单元测试
  - [ ] BusinessXmlCreateParamsTest
  - [ ] SqlValidatorTest
  - [ ] XItemCreatorTest
  
- [ ] 3.2 集成测试
  - [ ] BusinessXmlCreatorTest
  - [ ] UserConfigValidationTest
  
- [ ] 3.3 测试数据
  - CRM_COM (公司信息表)
  - ADM_USER (用户表)
  - GRP_COSTUMER (客人表)

**目标**: 覆盖率 > 80%

---

## 4. Table 扩展

### 4.1 新增方法

| 方法 | 说明 |
|-----|------|
| `toJson()` | 表元数据转 JSONObject |
| `fieldToJson(Field)` | 字段转 JSONObject |
| `fkToJson(TableFk)` | 外键转 JSONObject |
| `indexToJson(TableIndex)` | 索引转 JSONObject |

### 4.2 任务列表

- [ ] 4.1 `Table.toJson()` - 主方法
  - 表属性（TableName, SchemaName, DatabaseType）
  - 字段列表（Fields JSONArray）
  - 主键（Pk JSONObject）
  - 外键（Fks JSONArray）
  - 索引（Indexes JSONArray）
  
- [ ] 4.2 辅助方法
  - fieldToJson()
  - fkToJson()
  - indexToJson()

**依赖**: `org.json.JSONObject`, `Field`, `TableFk`, `TableIndex`

---

## 5. SQL 支持

### 5.1 SqlUtils 集成

| 方法 | 用途 |
|-----|------|
| `checkIsSelect(sql)` | 检查 SELECT 语句（支持 WITH） |
| `getSqlWithBlock(sql)` | 分离 WITH 块和主 SELECT |
| `extractTableNameFromSql(sql)` | 从 SQL 中提取表名 |

### 5.2 任务列表

- [ ] 5.1 SELECT 语句验证
  - 使用 SqlUtils.checkIsSelect()
  - 支持 WITH 语句
  - 支持多表关联
  
- [ ] 5.2 表名提取
  - 优先从主查询提取
  - 支持 schema.table 格式
  - 支持 FROM 子句解析

**依赖**: `SqlUtils`, `SqlSyntaxCheck`

---

## 开发顺序

```
4. Table 扩展 → 5. SQL 支持 → 1. JSON 创建 → 2. 验证 → 3. Test 用例
```

## 时间估算

| 阶段 | 任务数 | 估算时间 |
|-----|-------|---------|
| 4. Table 扩展 | 4 | 1 天 |
| 5. SQL 支持 | 2 | 0.5 天 |
| 1. JSON 创建 | 4 | 3 天 |
| 2. 验证 | 2 | 1 天 |
| 3. Test 用例 | 5 | 2 天 |
| **总计** | **17** | **7.5 天** |

---

## 验收标准

- [ ] 所有单元测试通过
- [ ] 集成测试通过
- [ ] 代码覆盖率 > 80%
- [ ] 无硬编码节点名和属性值
- [ ] JavaDoc 完整
