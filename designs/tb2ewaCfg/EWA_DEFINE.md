# EWA_DEFINE.xml 深度解析

`src/main/resources/system.xml/EwaDefine.xml` 是 EasyWeb 系统的 **设计时配置 (Design-Time Configuration)**。它不仅是 IDE 或向导工具的配置文件，更是系统中所有业务页面 (`.xml`) 的，参考`designs/tb2ewaCfg/examples/*.xml` **母板 (Template)**。

本文档详细解析其结构与创建逻辑，帮助理解业务代码的“源头”。

## 1. 核心结构概览

文件采用层级结构定义了创建向导的流程和产物模板：

```xml
<EwaDefine>
    <!-- 1. 向导步骤定义 -->
    <Steps>
        <Step Eval="..." AfterEval="..." />
    </Steps>

    <!-- 2. 组件模板定义 -->
    <Frame Name="Frame">      <!-- 表单类模板 -->
        <Tmp Name="NM">...</Tmp>  <!-- 新增/修改模式 -->
        <Tmp Name="V">...</Tmp>   <!-- 查看模式 -->
    </Frame>
    
    <Frame Name="ListFrame">  <!-- 列表类模板 -->
        <Tmp Name="V">...</Tmp>   <!-- 浏览模式 -->
        <Tmp Name="M">...</Tmp>   <!-- 管理模式 -->
    </Frame>
</EwaDefine>
```

## 2. 向导逻辑 (`<Steps>`)

`<Steps>` 节点定义了创建一个新页面时，向导工具执行的逻辑流。

| 步骤            | 描述                              | 逻辑 (`Eval`)      |
| :-------------- | :-------------------------------- | :----------------- |
| **1. 选择表**   | 从数据库选择主表                  | `CreateMainInfo()` |
| **2. 选择类型** | 选择 Frame 类型 (Frame/ListFrame) | `SelectType()`     |
| **3. 基础信息** | 修改名称、描述等                  | `ModifyMainInfo()` |
| **4. 选择字段** | 勾选需要显示的字段                | `ModifyFields()`   |
| **5. 选择菜单** | 配置关联菜单 (Tree)               | `ModifyMenus()`    |
| **6. SQL生成**  | 自动生成 SQL 语句                 | `ModifySQL()`      |

> **注意**: `Eval` 属性中的代码（如 `this.ModifyFields()`）是宿主环境（Java/C# IDE插件）执行的脚本方法。

## 3. 模板变体详解 (`<Tmp>`)

每个 `Frame` 类型下有多个变体 (`Tmp`)，决定了生成的 XML 结构。

### 3.1 `Frame` (表单) 变体

#### `NM` (New/Modify) - 标准增改模式
最常用的表单模板，用于数据的录入和编辑。
*   **预置按钮**: `submit` (确定), `button` (关闭)。
*   **预置动作**:
    *   `OnPageLoad`: 调用 `this.Fields.GetSqlSelect()` 生成查询 SQL (用于回显)。
    *   `OnNew`: 调用 `this.Fields.GetSqlNew()` 生成 INSERT SQL。
    *   `OnModify`: 调用 `this.Fields.GetSqlUpdate()` 生成 UPDATE SQL。
*   **预置脚本**: 注入 `DoPostBefore` 校验钩子。

#### `V` (View) - 详情查看模式
*   **特点**: 只有 `OnPageLoad` 查询数据，所有字段通常设为只读。

#### `Login` - 登录专用
*   **特点**: 包含验证码按钮 (`Tag="valid"`) 和登录逻辑 (`GetSqlLogin()`)。

### 3.2 `ListFrame` (列表) 变体

#### `V` (View) - 纯浏览列表
*   **预置动作**: `OnPageLoad` (查询列表数据)。
*   **扩展槽位**: 预留 `SAct0` - `SAct1` 供开发者添加额外查询。

## 4. 逻辑注入机制

`EwaDefine.xml` 通过以下方式将逻辑“遗传”给生成的业务文件：

### 4.1 动态 SQL 生成
在 `<Action>` 的 `Sql` 属性中，不直接写 SQL，而是写生成表达式：
```xml
<Action Name="OnPageLoad" Sql="this.Fields.GetSqlSelect()">
```
向导运行结束时，会遍历用户选择的字段，将 `GetSqlSelect()` 的执行结果（具体的 `SELECT A, B FROM T...`）写入到最终的 `admin.xml` 或 `flight.xml` 中。

### 4.2 脚本与样式注入 (`<Adds>`)
模板中通过 `<Adds>` 节点硬编码了标准 JS/CSS 块。

```xml
<Adds>
    <Add XmlPath="EasyWebTemplate/Page/AddScript/Set/Bottom"><![CDATA[
(function(){
    const ewa = EWA.F.FOS["@SYS_FRAME_UNID"];
    ewa.DoPostBefore = function(){
        return true; 
    };
    // ... Scope safe getObj helper ...
})();
    ]]></Add>
</Adds>
```
这解释了为什么所有业务文件底部都有一段极其相似的 `(function(){...})()` 代码——它们都是从这个母板复制过去的。

## 5. 对开发者的启示

1.  **遵循模式**: 手动修改 XML 时，应尽量保持与模板一致的结构（如 Action 命名、Script 闭包写法），以便维护。
2.  **理解冗余**: 看到 XML 中空的 `SAct0`, `UAct0` 节点不要奇怪，它们是模板留下的“占位符”。
3.  **脚本覆盖**: 如果需要修改默认的 `DoPostBefore` 逻辑，直接在生成的 XML `<AddScript>` 中修改即可，模板只提供初始状态。
