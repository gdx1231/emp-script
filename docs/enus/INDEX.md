# EWA Documentation Index

This directory contains English technical documentation for the emp-script (EWA) framework.

---

## Getting Started

| Document | Description |
|----------|-------------|
| [WEB.XML.md](WEB.XML.md) | Servlet web.xml configuration guide with complete template |
| [EWA_CONF.md](EWA_CONF.md) | Main configuration file ewa_conf.xml reference |
| [XML.md](XML.md) | XML configuration file structure under src/main/resources |
| [EWA_PARAMETERS.md](EWA_PARAMETERS.md) | All EWA system parameters explained |

## Configuration & Templates

| Document | Description |
|----------|-------------|
| [EWA_TEMPLATE_REFERENCE.md](EWA_TEMPLATE_REFERENCE.md) | EasyWebTemplate complete reference (structure, data, interaction, style) |
| [CONFIG_RELATIONSHIP.md](CONFIG_RELATIONSHIP.md) | Relationship analysis between EwaConfig.xml and define.xml/ewa |
| [BUSINESS_XML_CREATOR_DESIGN.md](BUSINESS_XML_CREATOR_DESIGN.md) | Design for auto-generating EWA business XML from database tables |
| [UPDATE_CFG.md](UPDATE_CFG.md) | XML configuration management tool for XItem/SqlSet/ActionSet/ScriptSet |

## Execution Flow

| Document | Description |
|----------|-------------|
| [EXECUTION_FLOW.md](EXECUTION_FLOW.md) | Complete execution flow from HTTP request to XML file processing |
| [FRAME_EXECUTION.md](FRAME_EXECUTION.md) | Frame form execution flow (Add/Modify/Copy modes) |
| [LISTFRAME_EXECUTION.md](LISTFRAME_EXECUTION.md) | ListFrame data list execution flow (paging/search/sort/export) |
| [GRID_EXECUTION.md](GRID_EXECUTION.md) | Grid data display execution flow |
| [TREE_EXECUTION.md](TREE_EXECUTION.md) | Tree structure execution flow (hierarchical loading/node operations) |
| [FRAME_CALLS.md](FRAME_CALLS.md) | Frame type invocation methods explained |

## Servlet & API

| Document | Description |
|----------|-------------|
| [SERVLETS.md](SERVLETS.md) | Detailed explanation of 14 Servlets, URL mappings, processing logic |
| [API_USAGE.md](API_USAGE.md) | ServletApi configuration management REST API usage |
| [API_TOKEN.md](API_TOKEN.md) | ServletApi authentication modes (HMAC/JWT/Session) |
| [API_SELF_DESCRIPTION.md](API_SELF_DESCRIPTION.md) | API self-documentation feature via /ewa-help-documents |
| [API_IMPROVEMENT_PLAN.md](API_IMPROVEMENT_PLAN.md) | RESTful API improvement plan |

## Database

| Document | Description |
|----------|-------------|
| [DATABASE_USAGE.md](DATABASE_USAGE.md) | Database access methods (DTTable/DataConnection/XML config/SqlCached) |
| [MYSQL.functions.md](MYSQL.functions.md) | MySQL custom functions (GETDATE/CHARINDEX/fn_chn_money/f_getpy, etc.) |

## Testing & Debugging

| Document | Description |
|----------|-------------|
| [EWA_TEST_USAGE.md](EWA_TEST_USAGE.md) | ewa_test/ewa_block_test conditional SQL execution guide |
| [AI.prompt.md](AI.prompt.md) | AI-assisted development prompts and data operation examples |

---

## By Topic

### Page Types
- [FRAME_EXECUTION.md](FRAME_EXECUTION.md) — Form pages
- [LISTFRAME_EXECUTION.md](LISTFRAME_EXECUTION.md) — List pages
- [GRID_EXECUTION.md](GRID_EXECUTION.md) — Grid pages
- [TREE_EXECUTION.md](TREE_EXECUTION.md) — Tree pages

### Configuration Management
- [EWA_CONF.md](EWA_CONF.md) — Main configuration file
- [XML.md](XML.md) — Resource directory structure
- [UPDATE_CFG.md](UPDATE_CFG.md) — Configuration management tool
- [BUSINESS_XML_CREATOR_DESIGN.md](BUSINESS_XML_CREATOR_DESIGN.md) — Auto-generate configuration

### Integration & APIs
- [API_USAGE.md](API_USAGE.md) — REST API usage
- [API_TOKEN.md](API_TOKEN.md) — API authentication
- [API_SELF_DESCRIPTION.md](API_SELF_DESCRIPTION.md) — API self-documentation
- [SERVLETS.md](SERVLETS.md) — Servlet details
- [WEB.XML.md](WEB.XML.md) — web.xml configuration
