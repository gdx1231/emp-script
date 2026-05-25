# 任务分配通知

**发件人**: 项目负责人  
**收件人**: 编码工程师、测试工程师  
**日期**: 2025-03-27  
**优先级**: 高  

---

## 项目背景

基于 `EwaConfig.xml` 规则模板，开发从数据库表/SELECT 语句/表 JSON 对象生成业务 XML 配置的工具类库。

**设计文档**: `designs/tb2ewaCfg/PLAN.md`  
**任务清单**: `designs/tb2ewaCfg/TODO_CODE.md`  

---

## 开发阶段

### 阶段 1: Table 扩展 (1 天)

**负责人**: 编码工程师

**任务**:
- [ ] 在 `Table.java` 中添加 `toJson()` 方法
- [ ] 添加辅助方法：`fieldToJson()`, `fkToJson()`, `indexToJson()`
- [ ] 编写单元测试 `TableJsonTest.java`

**输出**:
- `src/main/java/.../define/database/Table.java` (扩展)
- `src/test/java/.../define/database/TableJsonTest.java`

**验收标准**:
- `toJson()` 返回完整的表元数据 JSON
- 单元测试覆盖率 > 80%

**Git 提交**:
```bash
git add .
git commit -m "feat: Table 类扩展 toJson() 方法"
```

---

### 阶段 2: SQL 支持 (0.5 天)

**负责人**: 编码工程师

**任务**:
- [ ] 集成 `SqlUtils.checkIsSelect()` 检查 SELECT 语句
- [ ] 实现 `extractTableNameFromSql()` 提取表名（支持 WITH 语句）
- [ ] 编写测试用例

**输出**:
- `src/main/java/.../bussinessXmlCreator/SqlValidator.java`

**验收标准**:
- 支持 WITH 语句
- 支持多表关联
- 支持 schema.table 格式

**Git 提交**:
```bash
git add .
git commit -m "feat: 集成 SqlUtils 支持复杂 SQL 语句"
```

---

### 阶段 3: JSON 创建业务 XML (3 天)

**负责人**: 编码工程师

**任务**:
- [ ] `BusinessXmlCreateParams.java` - 入口参数类
- [ ] `BusinessXmlCreator.java` - 主创建器
- [ ] `XItemCreator.java` - XItem 创建器
- [ ] `NodeCreator.java` - 节点创建器

**输出**:
- `src/main/java/.../bussinessXmlCreator/BusinessXmlCreateParams.java`
- `src/main/java/.../bussinessXmlCreator/BusinessXmlCreator.java`
- `src/main/java/.../bussinessXmlCreator/XItemCreator.java`
- `src/main/java/.../bussinessXmlCreator/NodeCreator.java`

**验收标准**:
- 支持 tableName/selectSql/tableJson 三种输入方式
- 所有节点名来自 XItemParameters 定义
- 所有属性值来自 XGroupValue 定义

**Git 提交**:
```bash
git add .
git commit -m "feat: 创建通过 JSON 创建业务 XML 的功能"
```

---

### 阶段 4: 验证 (1 天)

**负责人**: 编码工程师

**任务**:
- [ ] `SqlValidator.checkSqlSyntax()` - SQL 语法检查
- [ ] `SqlValidator.validateSelectSql()` - 验证并获取字段元数据
- [ ] `UserConfig.createForValidation()` - 静态验证方法

**输出**:
- `src/main/java/.../bussinessXmlCreator/SqlValidator.java`
- `src/main/java/.../script/userConfig/UserConfig.java` (扩展)

**验收标准**:
- SQL 语法检查通过 `SqlSyntaxCheck`
- UserConfig 验证所有组件

**Git 提交**:
```bash
git add .
git commit -m "feat: 添加验证功能（SQL 语法检查和 UserConfig 验证）"
```

---

### 阶段 5: Test 用例 (2 天)

**负责人**: 测试工程师

**任务**:
- [ ] `BusinessXmlCreateParamsTest` - 参数验证测试
- [ ] `SqlValidatorTest` - SQL 验证测试
- [ ] `XItemCreatorTest` - XItem 创建测试
- [ ] `BusinessXmlCreatorTest` - 完整流程集成测试
- [ ] `UserConfigValidationTest` - UserConfig 验证测试

**输出**:
- `src/test/java/.../bussinessXmlCreator/BusinessXmlCreateParamsTest.java`
- `src/test/java/.../bussinessXmlCreator/SqlValidatorTest.java`
- `src/test/java/.../bussinessXmlCreator/XItemCreatorTest.java`
- `src/test/java/.../bussinessXmlCreator/BusinessXmlCreatorTest.java`
- `src/test/java/.../bussinessXmlCreator/UserConfigValidationTest.java`

**验收标准**:
- 所有测试通过
- 代码覆盖率 > 80%
- 缺陷率 < 5%

**Git 提交**:
```bash
git add .
git commit -m "test: 创建单元测试和集成测试用例"
```

---

## 时间计划

| 阶段 | 开始日期 | 结束日期 | 负责人 |
|-----|---------|---------|-------|
| 1. Table 扩展 | Day 1 AM | Day 1 PM | 编码工程师 |
| 2. SQL 支持 | Day 2 AM | Day 2 AM | 编码工程师 |
| 3. JSON 创建 | Day 2 PM | Day 4 PM | 编码工程师 |
| 4. 验证 | Day 5 AM | Day 5 PM | 编码工程师 |
| 5. Test 用例 | Day 6 AM | Day 7 PM | 测试工程师 |

---

## 沟通机制

### 每日站会
- 时间：每天上午 9:00
- 内容：昨日进展、今日计划、问题反馈

### 任务完成报告
```
[Agent 角色] 完成 [任务名称]
输出文件：文件列表
测试结果：通过/失败
下一步：...
```

### 问题报告
```
[Agent 角色] 发现问题
问题描述：...
影响范围：...
建议方案：...
需要支持：...
```

---

## 资源支持

### 文档
- 设计文档：`designs/tb2ewaCfg/TB2EWACFG_DESIGN.md`
- 开发计划：`designs/tb2ewaCfg/PLAN.md`
- 任务清单：`designs/tb2ewaCfg/TODO_CODE.md`

### 示例数据
- CRM_COM (公司信息表)
- ADM_USER (用户表)
- GRP_COSTUMER (客人表)

### 工具
- Maven: `mvn clean test`
- Git: `git add . && git commit -m "..."`
- IDE: IntelliJ IDEA / Eclipse

---

## 确认回复

请收到通知后回复：

```
[Agent 角色] 收到任务分配
确认开始时间：YYYY-MM-DD HH:mm
预计完成时间：YYYY-MM-DD HH:mm
```

---

**项目负责人**  
gdxsoft  
2025-03-27
