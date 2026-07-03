---
name: html-control
description: "Use when: 需要使用 EWA HtmlControl 类动态渲染 HTML 页面、编写 JSP/Java 页面渲染代码、或查询 HtmlControl API 用法和参数规范。"
---

# HtmlControl Skill

EWA 框架核心渲染类 `com.gdxsoft.easyweb.script.HtmlControl` 的使用指南。

## 参考示例

项目中大量 JSP 和 Java 文件使用了 HtmlControl，典型用法见：

```
src/main/webapp/ewa.jsp                          # EWA 通用入口（AJAX 分支 + frameset 检测）
src/main/webapp/project/project-data.jsp          # 动态切换 item（20+ 分支）
src/main/webapp/customer/survey/survey_info.jsp   # 多实例组合（7+ HtmlControl）
src/main/java/pf2023/HandleEwaImpl.java           # WebSocket 渲染（clone RV 模式）
src/main/java/pf2023/HandleChatImpl.java          # WebSocket 工厂方法 + 参数合并
```

**自动触发**：当用户要求编写 HtmlControl 相关代码时，先读取对应场景的参考文件。

## 框架文档参考

遇到 HtmlControl 初始化流程、页面渲染机制等概念不确定时，读取框架文档获取权威解释：

| 文档 | 说明 |
|------|------|
| `framework/emp-script/docs/zhcn/EXECUTION_FLOW.md` | HTTP 请求到 XML 的完整执行流程 |
| `framework/emp-script/docs/zhcn/FRAME_EXECUTION.md` | Frame 表单执行流程（init/渲染/ACL） |

**自动触发**：当对 HtmlControl 的 init 流程、页面渲染机制等概念不确定时，先 `read_file` 对应框架文档再操作。

## 核心 API

### 初始化方法

| 方法 | 说明 | 使用场景 |
|------|------|----------|
| `init(xmlName, itemName, paras, request, session, response)` | Servlet 原生对象初始化 | JSP 标准 HTTP 上下文（最常用） |
| `init(xmlName, itemName, paras, rv, null)` | RequestValue 初始化 | WebSocket / 后台任务（非 HTTP 上下文） |
| `init(xmlName, itemName, paras, rv, response)` | RequestValue + response | JSP 中已有 RequestValue 对象时（~13 处使用） |

### 输出方法

| 方法 | 说明 | 使用频率 |
|------|------|:--------:|
| `getHtml()` | 裁剪后 HTML（`<!--INC_TOP-->` 到 `<!--INC_END-->` 之间），含脚本 | 最高 |
| `getAllHtml()` | 完整未裁剪 HTML（`this.Html` 原值） | 高（34+ 处） |
| `getHtmlMin()` | 精简内容片段，含脚本 | 中（80 处） |
| `getHtmlFrameMin()` | 纯内容片段，**不含脚本**（按 `<!-- START: FRAME CONTENT-->` 标记截取） | 低 |

### 输出方法选择

| 场景 | 方法 |
|------|------|
| 独立新页面、iframe、弹窗 | `getHtml()` |
| 需要完整内容（含隐藏字段、签证/询价等复杂页面） | `getAllHtml()` |
| 页面内嵌入、AJAX 返回、Tab 面板 | `getHtmlMin()` |
| 纯内容片段、不需要脚本（如移动端嵌入） | `getHtmlFrameMin()` |

### 数据获取方法

| 方法 | 说明 | 使用频率 |
|------|------|:--------:|
| `getLastTable()` | 获取 init 时**最后一次**查询的 DTTable | 高（127 处） |
| `getTables()` | 获取 init 时**所有**查询的 DTTable 数组 | 低 |
| `getRequestValue()` | 获取内部 RequestValue 对象（含 `sys_frame_unid` 等） | 高（65 处） |
| `getTitle()` | 获取页面标题 | 高（70 处） |
| `getFrameUnid()` | 获取当前帧唯一标识符 | 中 |

### 配置方法

| 方法 | 说明 |
|------|------|
| `setFrameUnidPrefix(String)` / `getFrameUnidPrefix()` | FrameUnid 前缀修正，同一页面渲染多个相同模块实例时**必需**，避免 frame_unid 冲突 |
| `setAjaxCallUrl(String)` / `getAjaxCallUrl()` | 设置 Ajax 调用 URL |
| `setSkipAcl(boolean)` / `isSkipAcl()` | 控制是否跳过 ACL 权限校验（后台任务/WebSocket 场景设为 true） |
| `isError()` | 判断上一次 init 是否发生错误 |
| `getHtmlCreator()` | 获取底层 HtmlCreator 对象（高级用法：`isErrOut()`、`getItemValues()` 等） |
| `getDebugInfo()` | 获取调试信息对象 |

## 标准用法

### JSP 基本渲染

```jsp
<%
HtmlControl ht = new HtmlControl();
ht.init(xmlName, itemName, paras, request, session, response);
out.println(ht.getHtml());
%>
```

### 非 HTTP 上下文（WebSocket/后台任务）

```java
HtmlControl ht = new HtmlControl();
ht.init(xmlName, itemName, paras, rv, null);
String html = ht.getHtml();
```

> 注意：长连接场景中需克隆 RequestValue 避免并发问题。

### 带 ACL 跳过的后台渲染

```java
HtmlControl ht = new HtmlControl();
ht.setSkipAcl(true);
ht.init(xmlName, itemName, paras, rv, null);
String html = ht.getHtml();
```

## itemName 命名规范

遵循 `模块名.类型.操作` 三段式：

| 类型标识 | 含义 | 操作后缀 | 含义 |
|----------|------|----------|------|
| `F` | Form 表单 | `V` | 查看 |
| `Lf` / `LF` | List 列表 | `N` | 新增 |
| `T` | Table 表格 | `M` | 修改 |
| `Frame` | 框架页 | | |

示例：`ADM_USER.F.V`（用户表单-查看）、`BBS_TOPIC.LF.relay`（话题列表-转播）

## 常用 EWA 参数

| 参数 | 说明 |
|------|------|
| `EWA_AJAX=JSON` | AJAX JSON 模式 |
| `EWA_ACTION=OnPageLoad` | 页面加载 |
| `EWA_ACTION=OnPagePost` | 表单提交 |
| `EWA_MTYPE=M` | 手动模式 |
| `EWA_MTYPE=N` | 自动模式 |
| `EWA_PAGECUR=1` | 当前页码 |
| `EWA_PAGESIZE=20` | 每页条数 |
| `ewa_mobile=1` | 移动端模式 |

## 典型模式

### 模式 A：EWA 通用入口（AJAX 分支 + frameset 检测）

见 `ewa.jsp`。先判断 `ewa_ajax` 参数走 AJAX 分支，再检测 frameset 输出：

```jsp
<%
HtmlControl ht = new HtmlControl();
String xmlName = g_rv.s("xmlname");
String itemName = g_rv.s("itemname");
ht.init(xmlName, itemName, "", request, session, response);

// AJAX 分支
if (g_rv.s("ewa_ajax") != null) {
    out.println(ht.getHtml());
    return;
}

// frameset 检测
String html = ht.getHtml();
if (html.indexOf("<frameset") > 0) {
    out.println(html);
    return;
}

// 正常页面
%>
<y2019:header title="<%=ht.getTitle()%>"></y2019:header>
<%=html%>
```

### 模式 B：动态切换 item（单实例多分支）

见 `project/project-data.jsp`。一个 HtmlControl 实例根据参数反复 `init()` 渲染不同 item：

```jsp
<%
HtmlControl ht = new HtmlControl();
String xmlName = "/business/project/project.xml";
if ("nm".equals(gantt_mode)) {
    ht.init(xmlName, "OA_REQ.F.NM", paras, request, session, response);
    out.print(ht.getHtml());
} else if ("bat_add_subs".equals(gantt_mode)) {
    ht.init(xmlName, "OA_REQ.F.SubBat", paras, request, session, response);
    out.print(ht.getHtml());
}
// ... 更多分支
%>
```

### 模式 C：多实例组合页面

见 `customer/survey/survey_info.jsp`。单页面创建多个 HtmlControl 实例渲染不同模块：

```jsp
<%
HtmlControl ht = new HtmlControl();       // 主表单
ht.init(xml1, item1, paras, request, session, response);
out.println(ht.getAllHtml());

HtmlControl ht1 = new HtmlControl();     // 考题定义
ht1.init(xml2, item2, paras, request, session, response);
out.println(ht1.getHtmlMin());

HtmlControl htAtts = new HtmlControl();  // 附件
htAtts.init(xml3, item3, paras, request, session, response);
out.println(htAtts.getHtmlMin());
%>
```

### 模式 D：多实例 + FrameUnidPrefix（循环渲染）

同一页面循环渲染多个相同模块实例时，**必须**设置 `setFrameUnidPrefix` 避免 frame_unid 冲突：

```jsp
<%
for (int i = 0; i < list.size(); i++) {
    HtmlControl ht = new HtmlControl();
    ht.setFrameUnidPrefix("M" + i + "_");
    ht.init(xmlName, itemName, paras, request, session, response);
    out.println(ht.getHtmlMin());
}
%>
```

### 模式 E：WebSocket 渲染（clone RV）

见 `HandleEwaImpl.java` 和 `HandleChatImpl.java`。非 HTTP 上下文中必须克隆 RequestValue 并重置系统变量：

```java
// HandleEwaImpl 模式
HtmlControl ht = new HtmlControl();
RequestValue newRv = this.socket_.getRv().clone();
newRv.resetDateTime();    // 重置日期时间
newRv.resetSysUnid();     // 重置唯一标识
ht.init(xmlName, itemName, params, newRv, null);
String html = ht.getHtml();

// HandleChatImpl 模式 — 工厂方法 + 参数合并
private HtmlControl getHtmlControl(String itemName, String params) {
    HtmlControl ht = new HtmlControl();
    RequestValue rv_clone = this.getCloneRv();
    ht.init(defaultXmlName, itemName, params, rv_clone, null);
    return ht;
}

private RequestValue getCloneRv() {
    RequestValue rv_clone = this.socket_.getRv().clone();
    // 合并 command 参数
    Iterator<?> it = this.command_.keys();
    while (it.hasNext()) {
        String key = it.next().toString();
        rv_clone.getPageValues().remove(key);
        rv_clone.addValue(key, this.command_.optString(key));
    }
    rv_clone.resetDateTime();
    rv_clone.resetSysUnid();
    return rv_clone;
}
```

### 模式 F：获取查询数据做二次处理

`getLastTable()` 获取 SQL 查询结果，常用于判空、取值、转 JSON：

```jsp
<%
ht.init(xmlName, itemName, paras, request, session, response);

// 判空
if (ht.getLastTable().getCount() == 0) {
    // 无数据处理
}

// 取值
int grpId = ht.getLastTable().getCell(0, "GRP_ID").toInt();
String title = ht.getLastTable().getCell(0, "GRP_NAME").toString();

// 转 JSON 输出
out.println(ht.getLastTable().toJSONArray());

// 遍历
for (int i = 0; i < ht.getLastTable().getCount(); i++) {
    String sql = ht.getLastTable().getCell(i, "EXAM_SQL").toString();
}
%>
```

### 模式 G：获取 getRequestValue 中的系统生成值

`getRequestValue()` 常用于获取 init 过程中系统生成的值（如 `sys_frame_unid`、新 ID 等）：

```java
ht.init(xmlName, itemName, paras, rv, null);
String frameUnid = ht.getRequestValue().s("sys_frame_unid");
int newId = ht.getRequestValue().getInt("NEW_ADM_ID");
```

## 注意事项

1. `HtmlControl` **不是线程安全的**，每个请求应创建独立实例
2. 同一实例可多次 `init()` 复用（模式 B 就是利用这一点）
3. `getLastTable()` 返回最近一次 `init()` 的**最后一个** SQL 查询结果；如有多个查询，用 `getTables()` 获取数组
4. 长连接场景务必克隆 `RequestValue` 并调用 `resetDateTime()` + `resetSysUnid()`
5. 同一页面渲染多个相同模块时，**必须** `setFrameUnidPrefix()` 避免 frame_unid 冲突
6. `getHtml()` 会裁剪 `<!--INC_TOP-->` / `<!--INC_END-->` 标记之间的内容；如需完整输出用 `getAllHtml()`

## 参考

完整文档见项目根目录 `HTMLCONTROL.md`。
