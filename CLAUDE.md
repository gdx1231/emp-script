# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**emp-script** (Easy Web Application Builder / EWA) is a Java web application rapid-development framework for building CRM, OA, SCM and other enterprise management systems. It uses XML configuration to drive page generation, reducing development cycles by ~80% by shifting low-tech coding work into declarative XML configs.

- **GitHub**: https://github.com/gdx1231/emp-script
- **License**: MIT
- **Java**: 1.8

## Build & Test Commands

```bash
# Compile
mvn clean compile

# Package (produces target/emp-script-{version}.jar)
mvn clean package

# Install to local Maven repo
mvn clean install

# Deploy to Maven Central (requires GPG signing)
mvn clean deploy -P release

# Run a single test class
mvn test -Dtest=TestEwaFunction

# Run a specific test method
mvn test -Dtest=TestEwaFunction#testMethodName
```

**Note**: Tests in `src/test/` require live database connections (MySQL / SQL Server). The `TestBase` class initializes connection pools programmatically. See `TestBase.java` for connection setup patterns.

## Architecture at a Glance

The framework is an XML-driven page-generation engine. The core flow is:

```
HTTP Request → ServletMain → EwaWebPage → HtmlCreator → (XML config) → HTML/JSON/XML Response
```

### Entry Points

| Class | Role |
|-------|------|
| `ServletMain` | Main servlet; handles GET/POST/PUT/DELETE/PATCH. Initializes all EWA singletons on startup. |
| `EwaWebPage` | Per-request orchestrator; delegates to `HtmlCreator` to build page output. |
| `HtmlCreator` (`script/display/HtmlCreator.java`) | Core engine — parses XML config, executes SQL, renders HTML/JSON/XML. |

### Key Packages

| Package | Responsibility |
|---------|----------------|
| `script/` | Request handling (`RequestValue`), page values, servlets, templates, workflows |
| `script/display/` | HTML generation — `HtmlCreator`, `HtmlClass`, frame rendering, item rendering |
| `script/display/frame/` | Frame (form) parameters and rendering logic |
| `script/display/items/` | Individual form/list item rendering |
| `script/display/action/` | Action handlers (save, delete, custom actions) |
| `data/` | `DTTable`, `DTCell`, `DTRow`, `DTColumn` — in-memory data structures for query results |
| `datasource/` | Database connection pooling (HikariCP, Druid); multi-datasource support |
| `conf/` | Configuration parsing from `ewa_conf.xml` — connections, security, RESTful, Redis, etc. |
| `define/` | EWA configuration management (CRUD for XML configs), sync, RESTful, BusinessXmlCreator |
| `define/bussinessXmlCreator/` | Auto-generates EWA XML configs from database table schemas (Frame / ListFrame / Tree) |
| `define/database/` | Database introspection utilities |
| `cache/` | SQL result caching (HSQLDB and Redis implementations) |
| `global/` | Global settings and singletons (`EwaGlobals`, `EwaSettings`) |
| `function/` | EWA expression language functions |
| `acl/` | Access control interface (`IAcl`) and sample implementation |
| `utils/` | Utility classes — strings, paths, files, encryption, network |
| `websocket/` | WebSocket support |
| `uploader/` | File upload handling |
| `document/` | Document conversion (via JODConverter) |
| `statusControl/` | Status control / state machine logic |
| `log/` | Logging utilities |

### Configuration Files

| File | Purpose |
|------|---------|
| `ewa_conf.xml` | Main EWA configuration (databases, script paths, security, etc.) |
| `define.xml` | System definition config |
| `system.xml` | System-level settings |
| `ewa_conf.xml` location | Set via `web.xml` init-param `ewa_conf` or `UPath.CONF_NAME` |

### Core Data Flow

1. **Request arrives** at `ServletMain`
2. `EwaWebPage` is created with request/session/response
3. `HtmlCreator.init()` parses the XML config identified by `XMLNAME` + `ITEMNAME` parameters
4. ACL is checked (`IAcl` interface — pluggable)
5. `HtmlCreator.createPage()` executes SQL, builds data tables, renders HTML/JSON/XML
6. Response is output with optional debug info

### BusinessXmlCreator (Auto XML Generation)

The `define/bussinessXmlCreator/` module generates EWA XML configurations from database tables:

- `BusinessXmlCreator` — factory class
- `BusinessXmlCreatorBase` — abstract base with common SQL generation and field inference
- `BusinessXmlCreatorListFrame` — list page XML generation
- `BusinessXmlCreatorFrame` — form page XML generation
- `BusinessXmlCreatorTree` — tree page XML generation (with auto field name inference: PID/LVL/ORD/NAME)

## Maven Dependency

```xml
<dependency>
    <groupId>com.gdxsoft.easyweb</groupId>
    <artifactId>emp-script</artifactId>
    <version>1.1.10</version>
</dependency>
```

## Key Database Tables

- `ewa_cfg` / `ewa_cfg_his` / `ewa_cfg_oth` — EWA XML configuration storage
- `ewa_restful` / `ewa_restful_catalog` — RESTful API definitions
- `ewa_mod` / `ewa_mod_ver` / `ewa_mod_ddl` / `ewa_mod_field` — Module import/export
- `ewa_cfg_tree` — Configuration tree structure

## Important Documentation Files

| File | Content |
|------|---------|
| `readme.md` | Full project documentation, database schemas, Maven usage |
| `AGENTS.md` | Project context, build commands, core module descriptions |
| `DATABASE_USAGE.md` | Database usage patterns |
| `EWA_CONF.md` | EWA configuration guide |
| `FRAME_EXECUTION.md` | Frame execution flow |
| `BUSINESS_XML_CREATOR_DESIGN.md` | BusinessXmlCreator auto-generation design |
| `API_USAGE.md` | RESTful API usage |
| `TREE_EXECUTION.md` | Tree component execution flow |
