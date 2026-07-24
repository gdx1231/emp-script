# emp-script (Easy Web Application Builder)

[English](README_EN.md) | 中文

一个 Web 系统快速成型开发框架，适用于 CRM、OA、SCM 等企业管理系统的快速开发。

## 项目简介

针对一个 Web 应用系统开发，如 CRM、OA、SCM 等，在系统开发过程中，大量的时间被应用于页面合成上面。虽然有一些 MVC 中间件的应用，但低技术水准的编码和测试仍然占了整个开发过程 60%-80% 的时间，包括 HTML 代码合成、页面单元测试、数据验证与页面显示效果、浏览器兼容性、用户没完没了的需求变更等。这部分工作既是开发工程师的恶魔，也是项目管理中的恶魔。

在典型的应用系统中，业务逻辑复杂的部分只占项目整体的 10%-20% 左右，大部分工作是较为简单的数据录入、修改、删除和查询（包括多表和汇总）。复杂业务逻辑必须采用硬编码方式以保证业务逻辑的完整性，但简单的部分如果还是硬编码（即使使用代码生成器）并合成为 HTML，这样低技术水准的工作会大大延长开发时间。

emp-script 的目标是降低 80% 部分的开发周期：将大量低技术含量的工作通过 XML 配置文件进行部署，解放工程师的时间，使其将主要精力放到数据库架构设计和核心业务逻辑上，同时减少开发时间和开发成本。

## 核心特性

- **XML 配置驱动**：页面（表单 Frame、列表 ListFrame、网格 Grid、树 Tree）通过 XML 配置生成，无需硬编码 HTML
- **数据处理**：`DTTable` 数据表对象，支持 JDBC / XML / JSON 等多种数据源
- **RESTful API**：配置化接口映射，支持 HMAC / JWT / Session 三种认证模式
- **缓存模块**：支持 HSQLDB 和 Redis 两种缓存方式
- **多数据库**：MySQL / SQL Server / HSQLDB / Oracle / SQLite 等
- **其他**：WebSocket 支持、文件上传、文档转换（JODConverter）、配置导入/导出

## 技术栈

- Java 8+，Maven 构建，Servlet API 4.0 / WebSocket API 1.1
- 主要依赖：HikariCP / Druid（连接池）、Apache Commons、SLF4J、Jedis、Jsoup、JODConverter

## 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>com.gdxsoft.easyweb</groupId>
    <artifactId>emp-script</artifactId>
    <version>1.1.12</version>
</dependency>
```

### 构建

```bash
mvn clean compile   # 编译
mvn clean package   # 打包（生成 JAR / sources / javadoc）
mvn clean install   # 安装到本地仓库
```

### 数据库初始化

框架运行需要一组系统表（EWA 配置表、RESTful API 表、Module 表），DDL 见：

- [数据库表结构 (DDL)](docs/zhcn/DATABASE_TABLES.md) — MySQL / HSQLDB / SQL Server 版本

### 配置示例 (ewa_conf.xml)

```xml
<ewa_confs>
    <!-- 数据库配置 -->
    <databases>
        <database name="work" type="MYSQL">
            <pool driverClassName="com.mysql.cj.jdbc.Driver"
                  url="jdbc:mysql://localhost:3306/db"/>
        </database>
    </databases>

    <!-- 脚本路径配置 -->
    <scriptPaths>
        <scriptPath name="ewa" path="resources:/define.xml"/>
        <scriptPath name="my-project" path="/path/to/config"/>
    </scriptPaths>
</ewa_confs>
```

详细配置说明见 [EWA_CONF.md](docs/zhcn/EWA_CONF.md)，Servlet 部署见 [WEB.XML.md](docs/zhcn/WEB.XML.md)。

### 英式日期格式定义

```xml
<!-- 全局变量定义 -->
<globals>
        <!-- 覆盖EwaGlobal.xml的定义 -->
        <!-- 日期格式 -->
        <global lang="enus" date="dd/MM/yyyy"></global>
</globals>

<addedResources>
        <addedResource name="ukdateformat" resourceType="js" defaultConf="true" last="false">
<![CDATA[
_EWA_G_SETTINGS.DATE_old = _EWA_G_SETTINGS.DATE;
_EWA_G_SETTINGS.DATE = 'dd/MM/yyyy'; //英国
]]>
        </addedResource>
</addedResources>
```

## 文档

完整技术文档索引：

- **中文文档**: [docs/zhcn/README.md](docs/zhcn/README.md)
- **English Documentation**: [docs/enus/README.md](docs/enus/README.md)

常用入口：

| 主题 | 文档 |
|------|------|
| 快速入门 | [web.xml 配置](docs/zhcn/WEB.XML.md) · [ewa_conf.xml 详解](docs/zhcn/EWA_CONF.md) · [系统参数](docs/zhcn/EWA_PARAMETERS.md) |
| 页面类型 | [Frame 表单](docs/zhcn/FRAME_EXECUTION.md) · [ListFrame 列表](docs/zhcn/LISTFRAME_EXECUTION.md) · [Grid 网格](docs/zhcn/GRID_EXECUTION.md) · [Tree 树形](docs/zhcn/TREE_EXECUTION.md) |
| 配置模板 | [EasyWebTemplate 参考](docs/zhcn/EWA_TEMPLATE_REFERENCE.md) · [配置管理工具](docs/zhcn/UPDATE_CFG.md) |
| Servlet 与 API | [Servlet 详解](docs/zhcn/SERVLETS.md) · [REST API 使用](docs/zhcn/API_USAGE.md) · [API 认证](docs/zhcn/API_TOKEN.md) |
| 数据库 | [数据库调用](docs/zhcn/DATABASE_USAGE.md) · [表结构 DDL](docs/zhcn/DATABASE_TABLES.md) · [MySQL 自定义函数](docs/zhcn/MYSQL.functions.md) |

## 相关链接

- 官网: https://www.gdxsoft.com
- GitHub: https://github.com/gdx1231/emp-script

## 许可证

[MIT License](LICENSE)
