# EWA 文档索引

本目录包含 emp-script (EWA) 框架的中文技术文档。

---

## 快速入门

| 文档 | 说明 |
|------|------|
| [WEB.XML.md](WEB.XML.md) | Servlet web.xml 配置指南，完整配置模板 |
| [EWA_CONF.md](EWA_CONF.md) | 主配置文件 ewa_conf.xml 各配置项详解 |
| [XML.md](XML.md) | src/main/resources 下 XML 配置文件结构说明 |
| [EWA_PARAMETERS.md](EWA_PARAMETERS.md) | EWA 所有系统参数含义和用法 |

## 配置与模板

| 文档 | 说明 |
|------|------|
| [EWA_TEMPLATE_REFERENCE.md](EWA_TEMPLATE_REFERENCE.md) | EasyWebTemplate 配置项完整参考（页面结构、数据、交互、样式） |
| [CONFIG_RELATIONSHIP.md](CONFIG_RELATIONSHIP.md) | EwaConfig.xml 与 define.xml/ewa 的关系分析 |
| [BUSINESS_XML_CREATOR_DESIGN.md](BUSINESS_XML_CREATOR_DESIGN.md) | 根据数据库表结构自动生成 EWA 业务 XML 配置的设计说明 |
| [UPDATE_CFG.md](UPDATE_CFG.md) | XML 配置管理工具，直接操作 XItem/SqlSet/ActionSet/ScriptSet |

## 执行流程

| 文档 | 说明 |
|------|------|
| [EXECUTION_FLOW.md](EXECUTION_FLOW.md) | HTTP 请求到 XML 文件的完整执行流程分析 |
| [FRAME_EXECUTION.md](FRAME_EXECUTION.md) | Frame 表单执行流程（新增/修改/复制模式） |
| [LISTFRAME_EXECUTION.md](LISTFRAME_EXECUTION.md) | ListFrame 数据列表执行流程（分页/搜索/排序/导出） |
| [GRID_EXECUTION.md](GRID_EXECUTION.md) | Grid 网格化数据显示执行流程 |
| [TREE_EXECUTION.md](TREE_EXECUTION.md) | Tree 树形结构执行流程（分层加载/节点操作） |
| [FRAME_CALLS.md](FRAME_CALLS.md) | Frame 各类型调用方式详解 |

## Servlet 与 API

| 文档 | 说明 |
|------|------|
| [SERVLETS.md](SERVLETS.md) | 14 个 Servlet 的详细说明、URL 映射、处理逻辑 |
| [API_USAGE.md](API_USAGE.md) | ServletApi 配置管理 REST API 使用说明 |
| [API_TOKEN.md](API_TOKEN.md) | ServletApi 三种认证模式（HMAC/JWT/Session）说明 |
| [API_SELF_DESCRIPTION.md](API_SELF_DESCRIPTION.md) | API 自我描述功能，访问 /ewa-help-documents 获取文档 |
| [API_IMPROVEMENT_PLAN.md](API_IMPROVEMENT_PLAN.md) | RESTful API 改进计划 |

## 数据库

| 文档 | 说明 |
|------|------|
| [DATABASE_USAGE.md](DATABASE_USAGE.md) | 数据库调用方式详解（DTTable/DataConnection/XML配置/SqlCached） |
| [MYSQL.functions.md](MYSQL.functions.md) | MySQL 自定义函数（GETDATE/CHARINDEX/fn_chn_money/f_getpy 等） |

## 测试与调试

| 文档 | 说明 |
|------|------|
| [EWA_TEST_USAGE.md](EWA_TEST_USAGE.md) | ewa_test/ewa_block_test 条件化 SQL 执行指令使用指南 |
| [AI.prompt.md](AI.prompt.md) | AI 辅助开发提示，数据操作示例 |

---

## 按主题分类

### 页面类型
- [FRAME_EXECUTION.md](FRAME_EXECUTION.md) — 表单页面
- [LISTFRAME_EXECUTION.md](LISTFRAME_EXECUTION.md) — 列表页面
- [GRID_EXECUTION.md](GRID_EXECUTION.md) — 网格页面
- [TREE_EXECUTION.md](TREE_EXECUTION.md) — 树形页面

### 配置管理
- [EWA_CONF.md](EWA_CONF.md) — 主配置文件
- [XML.md](XML.md) — 资源目录结构
- [UPDATE_CFG.md](UPDATE_CFG.md) — 配置管理工具
- [BUSINESS_XML_CREATOR_DESIGN.md](BUSINESS_XML_CREATOR_DESIGN.md) — 自动生成配置

### 接口与集成
- [API_USAGE.md](API_USAGE.md) — REST API 使用
- [API_TOKEN.md](API_TOKEN.md) — API 认证
- [API_SELF_DESCRIPTION.md](API_SELF_DESCRIPTION.md) — API 自文档
- [SERVLETS.md](SERVLETS.md) — Servlet 详解
- [WEB.XML.md](WEB.XML.md) — web.xml 配置
