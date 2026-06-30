---
name: ewa-sql
description: "Use when: 编写 EWA XML 中的 SQL、理解 EWA SQL 参数规则、SQL 注释标记（ewa_test/ewa_block_test/auto/ewa_table_name/EWA_SQL_SPLIT_NO/EWA_IS_SELECT/EWA_JOIN/EWA_KV/COMPARATIVE_CHANGES）、分页查询、存储过程调用、INSERT/UPDATE/DELETE 模式。"
trigger: ewa-sql, EWA SQL, SqlSet, ewa_test, ewa_block_test, EWA_SQL_SPLIT_NO, -- auto, -- ewa_table_name, EWA_IS_SELECT, EWA_JOIN, EWA_KV, COMPARATIVE_CHANGES, @paramName, @G_SUP_ID, SQL XML, SQL 注释
---

# EWA SQL 编写指南

EWA 框架的 SQL 定义在 XML 配置文件的 `<SqlSet>` 中，通过参数绑定和 SQL 注释标记实现动态 SQL 模板。所有注释标记由 `SqlUtils.java` 解析。

---

## 1. XML SQL 定义结构

### 基本结构

```xml
<Page>
    <Action>
        <ActionSet>
            <Set Type="OnPageLoad" Transcation="no">
                <CallSet>
                    <Set CallName="OnPageLoad SQL" CallType="SqlSet" Test="'@EWA_MTYPE'='N'"/>
                </CallSet>
            </Set>
        </ActionSet>
    </Action>
    <SqlSet>
        <Set Name="OnPageLoad SQL" SqlType="query">
            <Sql><![CDATA[SELECT * FROM table WHERE id = @CRM_COM_ID]]></Sql>
        </Set>
    </SqlSet>
</Page>
```

### SqlType 值

| SqlType | 数量 | 用途 |
|---------|------|------|
| `query` | 8,956 | SELECT 查询 |
| `update` | 5,506 | INSERT/UPDATE/DELETE，可多语句 |
| `procedure` | 176 | 存储过程调用 |

### Action 类型

| Type | 用途 | 使用量 |
|------|------|--------|
| `OnPageLoad` | 页面加载时执行 | 3,947 |
| `OnPagePost` | 表单提交时执行 | 1,782 |
| `OnFrameDelete` | 删除记录 | 1,160 |
| `OnFrameRestore` | 恢复已删除记录 | 476 |
| `OnListFrameUpdateCell` | 行内编辑单元格 | 121 |
| `UAct0/1/2` | 用户自定义**更新** Action（新版推荐） | 1,511 / 1,564 / 908 |
| `SAct0/1/2` | 用户自定义**查询** Action（新版推荐） | 1,491 / 1,550 / 887 |
| `ExtendAction0/1/2` | 用户自定义扩展 Action（老版本） | 878 / 905 / 883 |
| 自定义名称 | 如 `getMasterContact`、`confirmEnroll` 等 | — |

> **注意**：`UAct0/1/2`（Update）和 `SAct0/1/2`（Select）是新版推荐的自定义 Action 命名，取代了老版本的 `ExtendAction0/1/2`。新 XML 应优先使用 `UAct`/`SAct`。

### Test 条件控制

```xml
<CallSet>
    <Set CallName="OnNew SQL" CallType="SqlSet" Test="'@EWA_MTYPE'='N'"/>
    <Set CallName="OnModify SQL" CallType="SqlSet" Test="'@EWA_MTYPE'='M'"/>
</CallSet>
```

---

## 2. 参数规则

### `@paramName` — 输入参数

从 URL 参数、表单 POST 数据、Session 变量中自动解析。

### `@G_*` — 会话全局变量

| 变量 | 含义 |
|------|------|
| `@G_SUP_ID` | 当前供应商 ID（95%+ SQL 都有此 WHERE 条件，多租户隔离） |
| `@G_ADM_ID` | 当前管理员 ID |
| `@G_SUB_IDS_SPLIT` | 下属用户 ID 列表，用于 `IN()` |
| `@G_USR_ID` | 网站用户 ID |

### `@SYS_*` — 系统变量

| 变量 | 含义 |
|------|------|
| `@SYS_DATE` | 当前日期时间 |
| `@SYS_UNID` | 唯一标识符（用于新建记录） |
| `@SYS_FRAME_UNID` | 当前帧 ID |
| `@SYS_EWA_LANG` | 当前语言 |
| `@SYS_REMOTE_IP` | 客户端 IP |

### `@XXX_SPLIT` — 逗号分隔 ID 列表

用于 SQL `IN()` 子句，框架自动转换格式：

```sql
WHERE city_id IN (@CITY_SPLIT)
WHERE A.ADM_ID IN (@G_SUB_IDS_SPLIT)
```

常见：`@CITY_SPLIT`、`@SUP_ID_SPLIT`、`@CRM_COM_ID_SPLIT`、`@IDS_SPLIT`、`@DEPT_SPLIT`

### `~EWA_FIELD_ID` — 行内编辑动态字段

用于 `OnListFrameUpdateCell` Action，运行时替换为实际列名：

```sql
UPDATE GRP_COSTUMER SET ~EWA_FIELD_ID = @EWA_FIELD_VAL 
WHERE GRP_COS_ID = @GRP_COS_ID AND RELATE_SUP_ID = @G_SUP_ID
```

### `@@IDENTITY` — SQL Server 最后插入的自增 ID

---

## 3. 框架内置函数

### `ewa_split(@param, ',')` — 字符串拆分函数

框架内置的虚拟函数（非真实 SQL 函数），将逗号分隔的字符串拆分为带索引的行集：

```sql
SELECT idx, col FROM ewa_split(@ids, ',')
```

返回结果：

| idx | col |
|-----|-----|
| 0 | 第一个值 |
| 1 | 第二个值 |
| 2 | 第三个值 |

**多表 JOIN 按索引对齐**：

```sql
SELECT a.col, b.col 
FROM (SELECT * FROM ewa_split(@s1, ',')) a
INNER JOIN (SELECT * FROM ewa_split(@s2, ',')) b ON a.idx = b.idx
```

**用于 IN() 子句**：

```sql
WHERE id IN (SELECT col FROM ewa_split(@ids, ','))
```

> **原理**：框架在 SQL 执行前扫描并替换 `ewa_split(...)` 为临时表查询 `(select idx, col from _ewa_spt_data where tag='{tag}')`。SQL Server 使用内存临时表 `#EWA_SPT_DATA_{uid}`，MySQL 使用物理表 `_EWA_SPT_DATA`。最多支持 50 个 `ewa_split` 调用。
>
> **重要**：SQL 注释中**不要包含 `ewa_split` 字符串**。框架会扫描整条 SQL 查找 `ewa_split` 模式，如果注释中写了"用 ewa_split 拆分 xxx"之类的文字，会被误判为函数调用并替换，导致 SQL 执行出错。

### `EWA_JSON()` — JSON 生成函数

在 SQL 中直接生成 JSON，支持两种模式：

```sql
-- INSERT 模式：生成 json_object
EWA_JSON(@NAME, @SEX, @AGE)
-- 等价于: json_object('name', @NAME, 'sex', @SEX, 'age', @AGE)

-- UPDATE 模式：生成 json_set
EWA_JSON(FIELD_NAME, @NAME, @SEX, @AGE)
-- 等价于: json_set(FIELD_NAME, '$.name', @NAME, '$.sex', @SEX, '$.age', @AGE)
```

### `ewa_func.*` — 工具函数集

通过 `ewa_func.` 前缀调用框架内置工具函数（定义在 `EwaFunctions.xml`）：

| 函数 | 用途 |
|------|------|
| `ewa_func.password_hash(@pwd)` | Argon2 密码哈希 |
| `ewa_func.password_verify(@pwd, @hash)` | Argon2 密码验证，返回 `'true'`/`'false'` |
| `ewa_func.encrypt(@src)` | AES 加密（默认配置） |
| `ewa_func.decrypt(@src)` | AES 解密（默认配置） |
| `ewa_func.md5(@src)` | MD5 哈希 |
| `ewa_func.sha1(@src)` | SHA1 哈希 |
| `ewa_func.digestHex(@src, @algo)` | 多算法摘要 |
| `ewa_func.http_get(@url)` | HTTP GET 请求 |
| `ewa_func.snowflake()` | Twitter Snowflake ID 生成 |
| `ewa_func.sendMail(@from, @tos, @subj, @content)` | 发送邮件 |

调用格式：
```sql
-- 静态调用
ewa_func.funcname(arg0, arg1)
-- 实例调用
ewa_func.funcname(constructArg0).(methodArg0)
```

结果存储在 `RequestValue` 中，键名为 `_ewa_func_{sha1_hash}`。

### `ewa_ids_sub()` / `ewa_ids_up()` — 树形 ID 递归查找

递归查找树形结构中的下级/上级 ID 列表：

```sql
-- 查找某部门及其所有下级部门
SELECT * FROM adm_dept 
WHERE dep_id IN (ewa_ids_sub('adm_dept', 'dep_id', 'dep_pid', @dep_id))

-- 查找某部门及其所有上级部门
SELECT * FROM adm_dept 
WHERE dep_id IN (ewa_ids_up('adm_dept', 'dep_id', 'dep_pid', @dep_id))
```

参数：`(表名, ID列名, 父ID列名, 起始ID)`。最多递归 100 层。字符串 ID 输出为 `'a','b','c'`，数字 ID 输出为 `1,2,3`。

### `~PARAM` — 动态标识符预替换

`~` 前缀的参数在 SQL 执行前直接替换为标识符（表名、列名等）：

```sql
SELECT * FROM ~TB_NAME WHERE id = @ID
-- 如果 ~TB_NAME = "USERS"，替换为: SELECT * FROM USERS WHERE id = @ID
```

> **安全**：来自 SESSION/SYSTEM/DTTABLE 等可信源的参数直接替换，其他源需通过 `SqlIdentifierValidator` 验证。

### `{@}` — @ 符号转义

在 SQL 中需要字面量 `@` 时使用（如邮箱地址）：

```sql
WHERE email = 'test{@}example.com'
-- 框架处理后变为: WHERE email = 'test@example.com'
```

---

## 4. SQL 注释标记

EWA 通过 SQL 中的 `--` 注释实现特殊功能。标记由 `SqlUtils.java` 解析，**从后向前扫描**以 `--` 开头的行。

### 3.1 `-- auto FIELD_NAME` — 自增 ID 返回

在 INSERT 语句后标记，框架获取最后插入的自增 ID 并赋值给参数：

```sql
INSERT INTO SUP_MAIN (SUP_NAME, SUP_MOBILE, SUP_USE_STATE)
VALUES (@SUP_NAME, @SUP_MOBILE, 'USED')
-- auto SUP_ID

-- 后续 SQL 可直接引用 @SUP_ID
INSERT INTO CRM_CONN (SUP_UP_ID, CRM_COM_ID, ADM_ID)
VALUES (@G_SUP_ID, @SUP_ID, @G_ADM_ID)
```

> **注意**：`-- auto` 必须紧跟在 INSERT 语句之后（可在同一 SqlSet 中，用 `;` 分隔的下一条 SQL 之前）。源码方法：`SqlUtils.getAutoField()`

### 3.2 `-- ewa_table_name TABLE_NAME` — 指定表名

告诉框架此 SQL 操作的目标表名：

```sql
-- ewa_table_name bct
SELECT BCT_CODE, BCT_VER, BCT_NAME FROM BCT WHERE BCT_CODE = @CODE
```

源码方法：`SqlUtils.getTableNameBySqlComment()`

### 3.3 `-- ewa_join join_name, key_field` — 数据拼接标记

将表的列数据拼接为字符串，供前端使用：

```sql
-- ewa_join join_name, key_field
SELECT key_field, display_field FROM some_table WHERE ...
```

源码方法：`SqlUtils.getJoinValueParameters()`

### 3.4 `-- ewa_kv json_name, key_field, value_field` — JSON 转换标记

将表列数据转为 RequestValue 的 JSON 对象：

```sql
-- ewa_kv json_name, key_field, value_field
SELECT key_field, value_field FROM some_table WHERE ...
```

源码方法：`SqlUtils.getKVParameters()`

### 3.5 `-- EWA_SQL_SPLIT_NO` — 禁止分页包装

默认情况下，框架会自动将 ListFrame 的查询 SQL 包装为分页查询。加此标记后，框架直接使用原始 SQL：

```sql
<Sql><![CDATA[-- EWA_SQL_SPLIT_NO
SELECT TOP 1 BAS_TAG CURRENT_YEAR FROM BAS_TAG WHERE BAS_TAG_GRP='YEARS'
;

SELECT * FROM CRM_COM V LEFT JOIN CRM_CONN CC ON V.CRM_COM_ID = CC.CRM_COM_ID
]]></Sql>
```

适用于：非列表数据查询、多语句查询、需要先查询再关联的场景。

### 3.6 `-- EWA_IS_SELECT` — 强制 SELECT 标记

强制框架将此 SQL 识别为 SELECT 语句。解决 `WITH...AS` 语句后框架无法正确识别 SELECT 的问题：

```sql
-- EWA_IS_SELECT
WITH t1 AS (
    SELECT * FROM GRP_SER_SP WHERE GSSM_ID > 0
)
SELECT * FROM t1
```

源码方法：`SqlUtils.ewaIsSelect()`

### 3.7 `-- COMPARATIVE_CHANGES` — 字段变化对比

告诉框架比较 UPDATE 前后字段的变化，记录变更日志：

```sql
UPDATE CRM_COM SET CRM_COM_NAME = @CRM_COM_NAME WHERE CRM_COM_ID = @CRM_COM_ID
-- COMPARATIVE_CHANGES
```

源码方法：`SqlUtils.isComparativeChanges()`

### 3.8 `-- ewa_test` — 单行条件注释

控制**紧随的一行 SQL** 是否执行：

```sql
-- ewa_test @SAI_ID is not null 
INSERT INTO SYS_CHANGE_LOG (LOG_REF_ID) VALUES (@SAI_ID)
-- ewa_test
```

- `-- ewa_test <条件>` — 条件为真时，**下一行** SQL 执行
- `-- ewa_test`（无条件）— 结束标记

### 3.9 `-- ewa_block_test` — 多行块条件注释

控制**多行 SQL 块**是否执行，块内可嵌套 `ewa_test`：

```sql
-- ewa_block_test @in_camp is not null
,
w_in_camp as (
    select distinct cgc.GRP_ID from CAMP_GRP_CONN cgc 
    where cgc.STATE = 'USED' 
    -- ewa_test @camp_id is not null
        and camp_id=@camp_id
    -- ewa_test
    group by cgc.GRP_ID
)
-- ewa_block_test
```

**两层条件**：
1. `@in_camp is not null` → 整个 CTE 是否存在
2. `@camp_id is not null` → CTE 内部是否加过滤条件

### 条件表达式语法

```sql
-- ewa_test @paramName is not null
-- ewa_test @paramName is null
-- ewa_test @paramName = 'VALUE'
-- ewa_test @paramName != 'VALUE'
-- ewa_test @GRP_TYPE='GRP_TYPE_AIR_TICKET' or @GRP_TYPE='GRP_QZ'
-- ewa_test (@abc is null)
-- ewa_test 1=2                    ← 永远不执行（占位用）
```

---

## 5. 常见 SQL 模式

### `EWA_ERR_OUT` — 错误输出列

SQL 查询返回 `EWA_ERR_OUT` 列时，框架逐行检查该列值。如有非空值，中断 SQL 执行并将其作为错误消息显示。常用于 `CheckError` Action：

```sql
<Set Name="CheckError SQL" SqlType="query">
    <Sql><![CDATA[
    -- select 不能执行，用作错误输出
    SELECT javascript_func() AS EWA_ERR_OUT FROM xxx WHERE 1=2
    ]]></Sql>
</Set>
```

存储过程的 `*_OUT` 参数（如 `ID_OUT`）也会被框架检查为错误消息。

### 分页控制 URL 参数

除 XML `<PageSize>` 配置外，可通过 URL 参数覆盖：

| 参数 | 效果 |
|------|------|
| `EWA_IS_SPLIT_PAGE=yes` | 强制开启分页 |
| `EWA_IS_SPLIT_PAGE=no` | 强制关闭分页 |
| `EWA_PAGESIZE=N` | 设置每页条数 |

### 多数据库支持

EWA 框架支持多种数据库，`@` 符号在各数据库中的转义方式不同：

| 数据库 | 识别关键词 | @ 转义方式 |
|--------|-----------|-----------|
| SQL Server | `mssql`, `sqlServer`, `sybase` | `'...' + char(64) + '...'` |
| MySQL/MariaDB | `mysql`, `MariaDB` | `CONCAT('...', CHAR(64), '...')` |
| PostgreSQL/人大金仓 | `PostgreSql`, `KingbaseES` | `'...' \|\| chr(64) \|\| '...'` |
| Oracle/达梦 | `Oracle`, `DM` | `'...' \|\| chr(64) \|\| '...'` |
| HSQLDB/H2 | `HSQLDB`, `H2` | `'...' \|\| char(64) \|\| '...'` |
| SQLite | `sqlite` | `CONCAT('...', CHAR(64), '...')` |

框架根据连接的数据库类型自动处理，编写 SQL 时无需关心转义细节。

### SQL 重建流水线

框架处理 SQL 的完整流程（`DataConnection.rebuildSql()`），按以下顺序执行：

1. `~PARAM` 波浪号预替换（动态标识符）
2. `-- ewa_block_test` 条件块评估
3. `-- ewa_test` 条件行评估
4. `@` 符号在注释行中的转义
5. `ewa_split()` 函数替换
6. `ewa_ids_sub()` / `ewa_ids_up()` 替换
7. `ewa_func.*` 函数提取
8. `EWA_JSON()` 替换
9. `@PARAM` 参数值替换（含 `_SPLIT` 逗号分割逻辑）

---

## 6. `/* {JSON} */` 扩展操作

在 SQL 后使用 `/* {JSON} */` 多行注释，触发查询后的表数据操作。框架解析注释中的 JSON 配置并执行：

### HOR（横向表操作）

对查询结果执行增删改操作：

```sql
SELECT * FROM WEB_USER WHERE XX = @AA
/* {"TYPE":"HOR", "RUN_TYPE":"Q", "FROM_TABLE":"WEB_USER", "UNID":"XX", "WHERE":"A=@AA"} */
```

| 字段 | 含义 |
|------|------|
| `RUN_TYPE` | `"Q"` 查询、`"U"` 更新、`"D"` 删除 |
| `FROM_TABLE` | 目标表名 |
| `UNID` | 唯一标识字段 |
| `WHERE` | 过滤条件 |

### JOIN（列合并）

按匹配键将另一表的列合并到结果中：

```sql
/* {"TYPE":"JOIN", "FROM_KEYS":"TAG1,TAG2", "TO_KEYS":"BAS_TAG1,BAS_TAG2"} */
```

### JOIN_HOR（纵向转横向）

将纵向（行式）数据转为横向（列式）显示：

```sql
/* {"TYPE":"JOIN_HOR", "FROM_KEYS":"TAG1,TAG2", "TO_KEYS":"BAS_TAG1,BAS_TAG2", "FIELDS":"A1,A2,A3", "NAME_KEY":"DT_NAME", "VALUE_KEY":"DT_VAL"} */
```

---

## 7. 存储过程调用

### `CALL` 语法

```sql
INSERT INTO SYS_ATTS (FILE_NAME, FILE_DES, FILE_UNID, FILE_CDATE)
VALUES ('signature', '', @SYS_UNID, getdate())
```

### UPDATE 模式

```sql
UPDATE GRP_COSTUMER 
SET ROOM_GROUP = @ROOM_GROUP, COS_NAME = @COS_NAME
WHERE GRP_COS_ID = @GRP_COS_ID AND RELATE_SUP_ID = @G_SUP_ID
```

### 软删除

```sql
UPDATE CRM_COM SET USE_STATE = 'DEL' WHERE CRM_COM_ID = @CRM_COM_ID
```

### 存储过程调用

框架通过检测 SQL 是否以 `CALL` 开头来判断是否为存储过程调用。支持两种语法：

```xml
<Set Name="setcar" SqlType="procedure" TransType="yes">
    <Sql><![CDATA[PR_SER_CAR(@SUP_ID, @g_SUP_ID, @SER_ID, @CITY_ID, @COIN_ID)]]></Sql>
</Set>
```

`CALL` 语法：

```sql
CALL PR_MODULE_TO_GROUP(@JNY_MDL_ID, @grp_id)
```

带 OUTPUT 参数：

```sql
EXEC PR_MODULE_TO_GROUP @JNY_MDL_ID, @grp_id, @index_out OUTPUT
EXEC PR_ADM_FROM_CRM_CUS @CRM_CUS_ID, null, null, @new_adm_id_output
```

> **注意**：存储过程的 `*_OUT` 参数会被框架检查为错误消息（见 `EWA_ERR_OUT` 章节）。

### 多语句

```sql
SELECT TOP 1 SUP_DOWN_ID, CRM_COM_ID FROM CRM_COM WHERE ...;
SELECT TOP 1 a.CRM_CUS_ID, b.ADM_ID FROM CRM_CUS a INNER JOIN ADM_USER b ON ...;
```

---

## 8. 批量操作模式

### 批量查询 — 弹窗显示多人信息

Frame 类型弹窗的 OnPageLoad SQL 用 `top 1` 限制返回一行，用子查询聚合多人信息：

```sql
select top 1
    m.MC_ID, m.MC_TYPE, m.MC_STATUS,
    -- 聚合多人姓名（SQL Server 2008 兼容）
    (
        SELECT STUFF((SELECT DISTINCT ', ' + isnull(gc2.COS_NAME, wu2.USR_NAME)
            from MEMBER_CARD mc2
            inner join ewa_split(@MC_IDS_BATCH, ',') i2 on mc2.MC_ID = i2.col
            left join web_user wu2 on mc2.USR_ID = wu2.USR_ID
            left join GRP_COSTUMER gc2 on mc2.GRP_COS_ID = gc2.GRP_COS_ID
            FOR XML PATH('')), 1, 2, '')
    ) USR_NAME,
    -- 去重聚合状态值
    (
        SELECT STUFF((SELECT DISTINCT ', ' + MC_STATUS
            from MEMBER_CARD mc3
            inner join ewa_split(@MC_IDS_BATCH, ',') i3 on mc3.MC_ID = i3.col
            FOR XML PATH('')), 1, 2, '')
    ) OLD_VALUE,
    -- 人数
    (select count(1) from ewa_split(@MC_IDS_BATCH, ',') x) BATCH_COUNT
from MEMBER_CARD m
inner join ewa_split(@MC_IDS_BATCH, ',') i on m.MC_ID = i.col
where m.SUP_ID = @G_SUP_ID
```

> **关键**：Frame 弹窗 SQL 必须返回一行，用 `top 1`；聚合用子查询而非 JOIN，避免返回多行。

### 批量更新

```sql
-- 日志记录
INSERT INTO MEMBER_CARD_LOG (MC_ID, CHANGE_TYPE, OLD_VALUE, NEW_VALUE, OPER_ADM_ID, OPER_TIME, REMARK, SUP_ID)
SELECT A.MC_ID, @CHANGE_TYPE, A.MC_STATUS, @NEW_VALUE, @G_ADM_ID, @SYS_DATE, '批量设置状态', @G_SUP_ID
FROM MEMBER_CARD A
INNER JOIN ewa_split(@MC_IDS_BATCH, ',') i ON A.MC_ID = i.col
WHERE A.SUP_ID = @G_SUP_ID;

-- 状态更新
UPDATE MEMBER_CARD SET
    MC_STATUS = @NEW_VALUE,
    MC_MADM_ID = @G_ADM_ID,
    MC_MDATE = @SYS_DATE
FROM MEMBER_CARD
INNER JOIN ewa_split(@MC_IDS_BATCH, ',') i ON MEMBER_CARD.MC_ID = i.col
WHERE MEMBER_CARD.SUP_ID = @G_SUP_ID
```

> **单条/批量统一**：单条操作时 `@MC_IDS_BATCH = "22"`，`ewa_split` 返回一行；批量时 `"22,23,24"` 返回多行。同一套 SQL 兼容两种模式。

---

## 8. SQL Server 特性

| 特性 | 示例 |
|------|------|
| `getdate()` | 当前时间戳 |
| `@@IDENTITY` | 最后插入的自增 ID |
| `isnull()` | NULL 合并 |
| `FOR XML PATH('')` | 字符串拼接 |
| `ROW_NUMBER()` | 窗口函数去重/排序 |
| `TOP N` | 限制行数 |
| `CONVERT(varchar(10), @SYS_DATE, 120)` | 日期格式化 |
| `DATEDIFF(MM, COS_BIRTH, GETDATE()) / 12` | 年龄计算 |
| `dbo.f_split()` | 逗号分隔字符串拆分 |
| `dbo.fn_GetQuanPin()` | 中文转拼音 |
| `stuff(... for xml path(''))` | 聚合拼接 |

---

## 9. 常见陷阱

| 陷阱 | 解决 |
|------|------|
| `@G_SUP_ID` 缺失导致跨租户数据泄漏 | 所有查询必须加 `WHERE ... = @G_SUP_ID` |
| 分页 SQL 被框架包装出错 | 加 `-- EWA_SQL_SPLIT_NO` 标记 |
| INSERT 后拿不到新 ID | 加 `-- auto fieldName` 标记 |
| 条件注释不生效 | 检查 `-- ewa_test` 和 `-- ewa_test` 成对出现 |
| `-- ewa_block_test` 块不完整 | 开始和结束标记必须配对 |
| 多语句中 `-- auto` 位置错误 | 必须紧跟 INSERT 语句（`;` 分隔后的下一条之前） |
| `~EWA_FIELD_ID` 只在行内编辑中有效 | 普通 Action 中不可用 |
| `_SPLIT` 参数用于 IN() | 框架自动转换为 SQL 兼容格式 |
| `WITH...AS` 后框架不识别 SELECT | 加 `-- EWA_IS_SELECT` 标记 |
| `ewa_test` 块内条件不生效 | 确保条件语法是 SQL Server 表达式 |
| `ewa_split()` 缺少别名导致 SQL 语法错误 | `ewa_split()` 返回的是表函数结果，必须加别名，如 `ewa_split(@MC_IDS_BATCH, ',') x`，否则 SQL Server 报"关键字附近有语法错误" |
| SQL Server 2008 不支持 `STRING_AGG` | 用 `STUFF((SELECT ', ' + col FROM ... FOR XML PATH('')), 1, 2, '')` 替代 |
| Frame 弹窗 OnPageLoad SQL 返回多行 | Frame（非 ListFrame）类型的弹窗 SQL 必须只返回一行数据，用 `top 1` 限制，否则报 `NullPointerException` |
| 注释中包含 `ewa_split` 文字 | 框架会误判为函数调用并替换，导致 SQL 报错 |
