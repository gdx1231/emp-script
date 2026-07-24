# emp-script (Easy Web Application Builder)

English | [中文](README.md)

A rapid prototyping framework for web application systems such as CRM, OA, and SCM.

## Overview

When developing a web application system (CRM, OA, SCM, etc.), a large amount of time is spent on page composition. Even with MVC middleware, low-tech coding and testing still account for 60%-80% of the entire development process — HTML synthesis, page unit testing, data validation and display effects, browser compatibility, and endless requirement changes. This work is the devil of both development engineers and project management.

In a typical application system, complex business logic only makes up about 10%-20% of the project. Most of the work is relatively simple data entry, modification, deletion, and querying (including multi-table operations and summaries). Complex business logic must be hard-coded to ensure its integrity, but hard-coding the simple parts as well (even with a code generator) and synthesizing them into HTML greatly extends development time.

The goal of emp-script is to cut 80% of the development cycle: deploy the large amount of low-tech work through XML configuration files, free up engineers' time to focus on database architecture design and core business logic, and reduce both development time and cost.

## Key Features

- **XML configuration driven**: pages (Frame forms, ListFrame lists, Grid, Tree) are generated from XML configuration — no hard-coded HTML
- **Data processing**: the `DTTable` data table object supports JDBC / XML / JSON and other data sources
- **RESTful API**: configuration-based endpoint mapping with HMAC / JWT / Session authentication modes
- **Caching**: HSQLDB and Redis cache backends
- **Multiple databases**: MySQL / SQL Server / HSQLDB / Oracle / SQLite, etc.
- **Others**: WebSocket support, file upload, document conversion (JODConverter), config import/export

## Tech Stack

- Java 8+, Maven build, Servlet API 4.0 / WebSocket API 1.1
- Main dependencies: HikariCP / Druid (connection pools), Apache Commons, SLF4J, Jedis, Jsoup, JODConverter

## Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>com.gdxsoft.easyweb</groupId>
    <artifactId>emp-script</artifactId>
    <version>1.1.12</version>
</dependency>
```

### Build

```bash
mvn clean compile   # compile
mvn clean package   # package (JAR / sources / javadoc)
mvn clean install   # install to local repository
```

### Database Setup

The framework requires a set of system tables (EWA config tables, RESTful API tables, Module tables). DDL scripts:

- [Database Tables (DDL)](docs/enus/DATABASE_TABLES.md) — MySQL / HSQLDB / SQL Server versions

### Configuration Example (ewa_conf.xml)

```xml
<ewa_confs>
    <!-- Database configuration -->
    <databases>
        <database name="work" type="MYSQL">
            <pool driverClassName="com.mysql.cj.jdbc.Driver"
                  url="jdbc:mysql://localhost:3306/db"/>
        </database>
    </databases>

    <!-- Script path configuration -->
    <scriptPaths>
        <scriptPath name="ewa" path="resources:/define.xml"/>
        <scriptPath name="my-project" path="/path/to/config"/>
    </scriptPaths>
</ewa_confs>
```

See [EWA_CONF.md](docs/enus/EWA_CONF.md) for full configuration details and [WEB.XML.md](docs/enus/WEB.XML.md) for Servlet deployment.

### British Date Format

```xml
<!-- Global variable definitions -->
<globals>
        <!-- Override the definitions in EwaGlobal.xml -->
        <!-- Date format -->
        <global lang="enus" date="dd/MM/yyyy"></global>
</globals>

<addedResources>
        <addedResource name="ukdateformat" resourceType="js" defaultConf="true" last="false">
<![CDATA[
_EWA_G_SETTINGS.DATE_old = _EWA_G_SETTINGS.DATE;
_EWA_G_SETTINGS.DATE = 'dd/MM/yyyy'; // UK
]]>
        </addedResource>
</addedResources>
```

## Documentation

Full technical documentation index:

- **English Documentation**: [docs/enus/README.md](docs/enus/README.md)
- **中文文档**: [docs/zhcn/README.md](docs/zhcn/README.md)

Common entry points:

| Topic | Documents |
|-------|-----------|
| Getting Started | [web.xml setup](docs/enus/WEB.XML.md) · [ewa_conf.xml reference](docs/enus/EWA_CONF.md) · [System parameters](docs/enus/EWA_PARAMETERS.md) |
| Page Types | [Frame forms](docs/enus/FRAME_EXECUTION.md) · [ListFrame lists](docs/enus/LISTFRAME_EXECUTION.md) · [Grid](docs/enus/GRID_EXECUTION.md) · [Tree](docs/enus/TREE_EXECUTION.md) |
| Templates | [EasyWebTemplate reference](docs/enus/EWA_TEMPLATE_REFERENCE.md) · [Config management tool](docs/enus/UPDATE_CFG.md) |
| Servlet & API | [Servlet details](docs/enus/SERVLETS.md) · [REST API usage](docs/enus/API_USAGE.md) · [API authentication](docs/enus/API_TOKEN.md) |
| Database | [Database access](docs/enus/DATABASE_USAGE.md) · [Table DDL](docs/enus/DATABASE_TABLES.md) · [MySQL custom functions](docs/enus/MYSQL.functions.md) |

## Links

- Official website: https://www.gdxsoft.com
- GitHub: https://github.com/gdx1231/emp-script

## License

[MIT License](LICENSE)
