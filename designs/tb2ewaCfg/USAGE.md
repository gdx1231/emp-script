# 从数据库表创建业务 XML 使用指南

## 概述

`BusinessXmlCreator` 可以根据数据库表结构自动生成 EWA 业务 XML 配置文件。

## 使用示例

### 1. 基本用法

```java
// 1. 加载 EwaConfig
EwaConfig config = EwaConfig.instance();

// 2. 从数据库获取表结构
Table table = Table.getJdbcTable("CRM_COM", "demo");

// 3. 创建 BusinessXmlCreator
BusinessXmlCreator creator = new BusinessXmlCreator(config, table);

// 4. 生成 XML
String xml = creator.createShowXml(
    "demo",           // 数据库名
    "CRM_COM",        // 表名
    null,             // 自定义 SQL（null 表示自动生成）
    null,             // JSON 参数
    "ListFrame",      // Frame 类型：ListFrame/Frame/Tree
    "M"               // 操作类型：N/M/V/NM
);

// 5. 保存 XML
// 手动保存到文件
Files.write(Paths.get("output.xml"), xml.getBytes());
```

### 2. Frame 类型和操作类型

#### Frame 类型

| 类型 | 说明 | 用途 |
|------|------|------|
| ListFrame | 列表框 | 显示多条记录，支持分页、搜索 |
| Frame | 单帧表单 | 显示单条记录的详细信息 |
| Tree | 树形结构 | 显示层级结构数据 |

#### 操作类型

| 类型 | 说明 | 用途 |
|------|------|------|
| N | 新增 | 用于新增数据的表单 |
| M | 修改 | 用于修改数据的表单 |
| V | 查看 | 用于查看数据的只读表单 |
| NM | 新增修改 | 同时支持新增和修改的表单 |

### 3. 命名规则

生成的 XML 名称遵循以下规则：

```
{TABLE_NAME}.{FRAME_TYPE}.{OPERATION_TYPE}
```

示例：
- `CRM_COM.LF.M` - CRM_COM 表的列表修改模式
- `CRM_COM.F.NM` - CRM_COM 表的框架新增修改模式
- `CRM_COM.T.M` - CRM_COM 表的树形修改模式

### 4. 自动生成的配置

#### PageSize 配置

```xml
<PageSize>
  <Set IsSplitPage="1" KeyField="CRM_COM_ID" PageSize="10" Recycle="1"/>
</PageSize>
```

| 属性 | 值 | 说明 |
|------|------|------|
| IsSplitPage | 1 | 启用分页 |
| KeyField | CRM_COM_ID | 主键字段（自动从表结构获取） |
| PageSize | 10 | 每页 10 条记录 |
| Recycle | 1 | 启用回收站（逻辑删除） |

#### ListUI 配置

```xml
<ListUI>
  <Set luButtons="1" luSearch="1" luSelect="s"/>
</ListUI>
```

| 属性 | 值 | 说明 |
|------|------|------|
| luButtons | 1 | 显示操作按钮 |
| luSearch | 1 | 显示搜索栏 |
| luSelect | s | 单选模式（s=单选，m=多选） |

#### OrderSearch 配置

根据字段类型自动设置：

| 字段类型 | 条件 | IsOrder | SearchType |
|---------|------|---------|------------|
| 数字 (INT/DECIMAL/NUM...) | - | 1 | "" |
| 日期 (DATE/TIME) | - | 1 | "" |
| 文字 (CHAR/TEXT) | Length ≤ 100 | 1 | text |
| 文字 (CHAR/TEXT) | Length > 100 | 0 | text |

示例：
```xml
<!-- 数字类型 -->
<XItem Name="CRM_COM_ID">
  <OrderSearch>
    <Set IsOrder="1" SearchType="" SearchSql=""/>
  </OrderSearch>
</XItem>

<!-- 文字类型 (长度 200) -->
<XItem Name="CRM_COM_NAME">
  <OrderSearch>
    <Set IsOrder="0" SearchType="text" SearchSql=""/>
  </OrderSearch>
</XItem>

<!-- 日期类型 -->
<XItem Name="CRM_COM_CDATE">
  <OrderSearch>
    <Set IsOrder="1" SearchType="" SearchSql=""/>
  </OrderSearch>
</XItem>
```

### 5. 自动生成的 SQL

#### ListFrame.M 模式

```xml
<SqlSet>
  <!-- 加载数据 SQL -->
  <Set Name="OnPageLoad SQL" SqlType="query">
    <Sql>
      SELECT A.* FROM CRM_COM A WHERE 1=1
      -- ewa_test @EWA_RECYCLE is null
      AND A.CRM_COM_STATE = 'USED'
      -- ewa_test @EWA_RECYCLE = '1'
      AND A.CRM_COM_STATE = 'DEL'
      -- ewa_test
      ORDER BY A.CRM_COM_ID DESC
    </Sql>
  </Set>
  
  <!-- 逻辑删除 SQL -->
  <Set Name="OnFrameDelete SQL" SqlType="update">
    <Sql>
      UPDATE CRM_COM SET CRM_COM_STATE='DEL', CRM_COM_MDATE = @sys_date 
      WHERE CRM_COM_ID = @CRM_COM_ID
    </Sql>
  </Set>
  
  <!-- 恢复数据 SQL -->
  <Set Name="OnFrameRestore SQL" SqlType="update">
    <Sql>
      UPDATE CRM_COM SET CRM_COM_STATE='USED', CRM_COM_MDATE = @sys_date 
      WHERE CRM_COM_ID = @CRM_COM_ID
    </Sql>
  </Set>
</SqlSet>
```

### 6. 自动生成的按钮

ListFrame.M 模式自动生成 5 个按钮：

```xml
<Buttons>
  <Button Name="butNew" Tag="button">
    <EventSet>
      <Set EventName="ewa_click" EventType="Javascript" 
           EventValue="EWA.F.FOS[&quot;@sys_frame_unid&quot;].ext_NewOrModifyOrCopy(&quot;N&quot;)"/>
    </EventSet>
  </Button>
  <Button Name="butModify" Tag="button">
    <EventSet>
      <Set EventName="onclick" EventType="Javascript" 
           EventValue="EWA.F.FOS[&quot;@sys_frame_unid&quot;].ext_NewOrModifyOrCopy(&quot;M&quot;,&quot;'+ define.Fields.GetPkParas() + '&quot;)"/>
    </EventSet>
  </Button>
  <Button Name="butCopy" Tag="button">
    ...
  </Button>
  <Button Name="butDelete" Tag="button">
    <CallAction>
      <Set Action="OnFrameDelete" ConfirmInfo="DeleteBefore"/>
    </CallAction>
  </Button>
  <Button Name="butRestore" Tag="button">
    <CallAction>
      <Set Action="OnFrameRestore"/>
    </CallAction>
  </Button>
</Buttons>
```

## 测试示例

参考 `DatabaseTableToXmlTest.java`：

```java
@Test
public void testCreateCrmCom_LF_M() throws Exception {
    // 1. 创建参数
    BusinessXmlCreateParams params = new BusinessXmlCreateParams(
        "demo",           // db
        "CRM_COM",        // tableName
        "ListFrame",      // frameType
        "M"               // operationType
    );
    
    // 2. 验证参数
    assertTrue(params.validate());
    
    // 3. 创建表结构（从数据库或模拟）
    Table table = createMockCrmComTable();
    
    // 4. 创建 BusinessXmlCreator
    BusinessXmlCreator creator = new BusinessXmlCreator(config, table);
    
    // 5. 生成 XML
    String xml = creator.createShowXml(
        params.getDb(),
        params.getTableName(),
        null,
        null,
        params.getFrameType(),
        params.getOperationType()
    );
    
    // 6. 验证 XML
    assertTrue(xml.contains("CRM_COM.LF.M"));
    assertTrue(xml.contains("butNew"));
    assertTrue(xml.contains("IsSplitPage=\"1\""));
}
```

## 注意事项

1. **主键字段**：表必须有主键，否则无法生成删除/修改功能
2. **状态字段**：建议包含状态字段（如 `CRM_COM_STATE`），用于回收站功能
3. **修改时间字段**：建议包含修改时间字段（如 `CRM_COM_MDATE`），用于自动更新时间
4. **字段描述**：建议为数据库字段添加描述，会显示在 XML 的 DescriptionSet 中

## 相关文件

- `BusinessXmlCreator.java` - XML 创建器
- `BusinessXmlCreateParams.java` - 参数类
- `DatabaseTableToXmlTest.java` - 测试示例
- `EwaDefineConfig.java` - EWA 定义配置读取器
