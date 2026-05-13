---
name: html-control
description: "Use when: 需要使用 EWA HtmlControl 类动态渲染 HTML 页面、编写 JSP/Java 页面渲染代码、或查询 HtmlControl API 用法和参数规范。"
---

# HtmlControl Skill

EWA 框架核心渲染类 `com.gdxsoft.easyweb.script.HtmlControl` 的使用指南。

## 核心 API

| 方法 | 说明 |
|------|------|
| `init(xmlName, itemName, paras, request, session, response)` | 初始化控件（Servlet 原生对象） |
| `init(xmlName, itemName, paras, rv, null)` | 初始化控件（RequestValue 对象，用于非 HTTP 上下文） |
| `getHtml()` | 获取完整 HTML（含页面结构、脚本） |
| `getHtmlMin()` | 获取精简 HTML（仅内容片段） |
| `getTitle()` | 获取页面标题 |
| `getLastTable()` | 获取 init 时查询的 DTTable 数据 |
| `getRequestValue()` | 获取内部 RequestValue 对象 |

## 标准用法

```jsp
<%
HtmlControl ht = new HtmlControl();
ht.init(xmlName, itemName, paras, request, session, response);
out.println(ht.getHtml());
%>
```

## 非 HTTP 上下文（WebSocket/后台任务）

```java
HtmlControl ht = new HtmlControl();
ht.init(xmlName, itemName, paras, rv, null);
String html = ht.getHtml();
```

> 注意：长连接场景中需克隆 RequestValue 避免并发问题。

## getHtml() vs getHtmlMin() 选择

| 场景 | 方法 |
|------|------|
| 独立新页面、iframe、弹窗 | `getHtml()` |
| 页面内嵌入、AJAX 返回、Tab 面板 | `getHtmlMin()` |

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

### AJAX 模式判断

```jsp
<%
ht.init(xmlName, itemName, paras, request, session, response);
if (g_rv.s("ewa_ajax") != null) {
    out.println(ht.getHtml());
    return;
}
%>
```

### 多模块组合

```jsp
<%
HtmlControl ht1 = new HtmlControl();
ht1.init(xml1, item1, paras1, request, session, response);
out.println(ht1.getHtmlMin());

HtmlControl ht2 = new HtmlControl();
ht2.init(xml2, item2, paras2, request, session, response);
out.println(ht2.getHtmlMin());
%>
```

### 获取查询数据

```jsp
<%
ht.init(xmlName, itemName, paras, request, session, response);
DTTable tb = ht.getLastTable();
// tb.getCount() 获取行数
// tb.getCell(i, "column_name").toString() 获取单元格值
%>
```

## 注意事项

1. `HtmlControl` **不是线程安全的**，每个请求应创建独立实例
2. 同一实例可多次 `init()` 复用
3. `getLastTable()` 返回最近一次 `init()` 的查询结果
4. 长连接场景务必克隆 `RequestValue`

## 参考

完整文档见项目根目录 `HTMLCONTROL.md`。
