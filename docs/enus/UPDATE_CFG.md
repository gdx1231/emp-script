# UPDATE_CFG — XML Configuration Management Tool

## Overview

`UPDATE_CFG` is a tool for directly manipulating EWA XML configuration files. It provides a complete set of APIs and frontend pages for managing **XItem definitions** and **SqlSet/ActionSet/ScriptSet configurations** in XML.

**Core Constraint**: Directly reads and writes XML files, does not use a database.

---

## Architecture

```
┌─────────────────────────────────────────────┐
│  HTTP Request                                │
│  (UpdateCfgAction.java - Servlet)            │
│  - Parameter parsing (RequestValue)          │
│  - Authorization (ApiTokenValidator)         │
│  - Method dispatch (dispatch)                │
└──────────────┬──────────────────────────────┘
               │ DTO parameters
               ▼
┌─────────────────────────────────────────────┐
│  XML Operation Layer                         │
│  (UpdateCfgXml.java - define package)        │
│  - DOM parsing (UXml)                        │
│  - Node CRUD (Create/Read/Update/Delete/Move)│
│  - Save (IUpdateXml)                         │
│  - Operation log (operator audit)            │
└──────────────┬──────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────┐
│  XML Files                                   │
│  (define.xml/ewa/*.xml)                      │
└─────────────────────────────────────────────┘
```

### File Inventory

| File | Path | Description |
|------|------|------|
| `UpdateCfgXml.java` | `src/main/java/.../define/UpdateCfgXml.java` | XML operation core class |
| `UpdateCfgAction.java` | `src/main/java/.../define/servlets/UpdateCfgAction.java` | Servlet entry |
| `ApiTokenValidator.java` | `src/main/java/.../define/servlets/ApiTokenValidator.java` | API Token validator |
| `UpdateCfgXItemData.java` | `src/main/java/.../define/UpdateCfgXItemData.java` | XItem parameter DTO |
| `UpdateCfgSqlSetData.java` | `src/main/java/.../define/UpdateCfgSqlSetData.java` | SqlSet parameter DTO |
| `UpdateCfgScriptSetData.java` | `src/main/java/.../define/UpdateCfgScriptSetData.java` | ScriptSet parameter DTO |
| `UPDATE_CFG.xml` | `src/main/resources/define.xml/ewa/UPDATE_CFG.xml` | Frontend page configuration |
| `UpdateCfgXmlTest.java` | `src/test/java/.../define/UpdateCfgXmlTest.java` | JUnit 5 tests |

---

## Authorization & Security

### Three-Layer Verification

1. **Global Switch** — `ConfDefine.isAllowDefine()` rejects all requests when disabled
2. **Token Verification** — `ApiTokenValidator.validate()` verifies admin identity
3. **Operation Audit** — All write operations log the `operator` to the log

### Token Verification Methods

Two modes supported:

**HMAC Signature Mode (recommended, server-side calls):**
```
Headers:
  X-Api-Key: {loginId}
  X-Api-Timestamp: {current timestamp ms}
  X-Api-Nonce: {random string}
  X-Api-Signature: {HMAC-SHA256 signature}
```

Signature algorithm:
```
signature = HMAC-SHA256(secret, method + "\n" + timestamp + "\n" + nonce + "\n" + path + "\n" + sortedQueryParams)
```

**JWT Token Mode (client-side calls):**
```
Headers:
  Authorization: Bearer {jwt_token}
  or
  X-Api-Token: {jwt_token}
```

### Security Features
- Timestamp tolerance: ±5 minutes
- Nonce cache: Anti-replay attack (10000 entries cache, 10 minutes expiry)
- Token expiry: 2 hours
- Whitelist mechanism: Extensible no-auth methods (currently empty)

---

## API Reference

All APIs are invoked via POST/GET with the `method` parameter specifying the operation.

### Common Parameters

| Parameter | Description |
|------|------|
| `method` | Method name (see list below) |
| `XML_PATH` | XML file path (e.g. `/ewa/admin.xml`) |
| `ITEM_NAME` | EasyWebTemplate Name (e.g. `ADM_USER.Frame.ChangePWD`) |

### Return Value

Success:
```json
{
  "success": true,
  "message": "...",
  "data": { ... }
}
```

Failure:
```json
{
  "success": false,
  "message": "Error description"
}
```

### 1. File & Template Management

#### `listXmlFiles` — List all editable XML
```
POST /api/updateCfg?method=listXmlFiles
```
Returns:
```json
{
  "success": true,
  "data": [
    { "xmlPath": "/ewa/admin.xml", "items": 12, "frames": {...} },
    ...
  ]
}
```

#### `listItems` — List all EasyWebTemplates in XML
```
POST /api/updateCfg?method=listItems&XML_PATH=/ewa/admin.xml
```
Returns:
```json
{
  "success": true,
  "data": [
    { "name": "ADM_USER.Frame.ChangePWD", "frameTag": "Frame", ... },
    ...
  ]
}
```

### 2. XItem Management

#### `listXItems` — List all XItems in a template
```
POST /api/updateCfg?method=listXItems&XML_PATH=/ewa/admin.xml&ITEM_NAME=ADM_USER.Frame.ChangePWD
```

#### `getXItem` — Get XItem details
```
POST /api/updateCfg?method=getXItem&XML_PATH=...&ITEM_NAME=...&XITEM_NAME=ADM_PWD
```

#### `saveXItem` — Save XItem
```
POST /api/updateCfg?method=saveXItem
```
Parameters:

| Parameter | Required | Description |
|------|------|------|
| `XITEM_NAME` | ✅ | XItem name |
| `OLD_XITEM_NAME` | | Original name (used when renaming) |
| `TAG` | | Type (text/password/select/submit/button...) |
| `DESC_ZH` | | Chinese description |
| `DESC_EN` | | English description |
| `DATA_FIELD` | | Data field |
| `DATA_TYPE` | | Data type (String/Int/Date...) |
| `IS_MUST_INPUT` | | Required (1=yes) |
| `IS_ENCRYPT` | | Encrypted (1=yes) |
| `VALID` | | Validation rules |
| `FORMAT` | | Formatting |
| `MAX_LENGTH` | | Maximum length |
| `MIN_LENGTH` | | Minimum length |
| `MAX_VALUE` | | Maximum value |
| `MIN_VALUE` | | Minimum value |
| `IS_ORDER` | | Sortable (1=yes) |
| `SEARCH_TYPE` | | List search type |
| `ORDER_EXP` | | Sort expression |
| `STYLE` | | Style |
| `PARENT_STYLE` | | Parent style |
| `XSTYLE` | | Extended style |
| `EVENT_NAME` | | Event name |
| `EVENT_TYPE` | | Event type |
| `EVENT_VALUE` | | Event value |
| `CALL_ACTION` | | Call action |
| `CONFIRM_INFO` | | Confirmation info |
| `LIST_SQL` | | Dropdown SQL |
| `LIST_VALUE_LIST` | | Dropdown value list |
| `LIST_DISPLAY_LIST` | | Dropdown display list |
| `USER_SET` | | Custom HTML |

#### `deleteXItem` — Delete XItem
```
POST /api/updateCfg?method=deleteXItem&XML_PATH=...&ITEM_NAME=...&XITEM_NAME=xxx
```

#### `moveXItem` — Adjust XItem order
```
POST /api/updateCfg?method=moveXItem&XML_PATH=...&ITEM_NAME=...&XITEM_NAME=xxx&direction=up
```
`direction`: `up` or `down`

### 3. SqlSet Management

#### `listActions` — List ActionSet + SqlSet + ScriptSet
```
POST /api/updateCfg?method=listActions&XML_PATH=...&ITEM_NAME=...
```

#### `getSqlSet` — Get SqlSet details
```
POST /api/updateCfg?method=getSqlSet&XML_PATH=...&ITEM_NAME=...&SQL_NAME=OnPageLoad SQL
```

#### `saveSqlSet` — Save SqlSet
```
POST /api/updateCfg?method=saveSqlSet
```
Parameters:

| Parameter | Required | Description |
|------|------|------|
| `SQL_NAME` | ✅ | SqlSet name |
| `OLD_SQL_NAME` | | Original name (used when renaming) |
| `SQL_TYPE` | ✅ | SQL type (query/update) |
| `TRANS_TYPE` | | Transaction type (no/yes/empty) |
| `SQL_CONTENT` | ✅ | SQL content (supports multi-line, wrapped in CDATA) |

#### `deleteSqlSet` — Delete SqlSet
```
POST /api/updateCfg?method=deleteSqlSet&XML_PATH=...&ITEM_NAME=...&SQL_NAME=xxx
```

### 4. ScriptSet Management

#### `getScriptSet` — Get ScriptSet details
```
POST /api/updateCfg?method=getScriptSet&XML_PATH=...&ITEM_NAME=...&SCRIPT_NAME=xxx
```

#### `saveScriptSet` — Save ScriptSet
```
POST /api/updateCfg?method=saveScriptSet
```
Parameters:

| Parameter | Required | Description |
|------|------|------|
| `SCRIPT_NAME` | ✅ | ScriptSet name |
| `OLD_SCRIPT_NAME` | | Original name (used when renaming) |
| `SCRIPT_TYPE` | ✅ | Script type (javascript) |
| `SCRIPT_CONTENT` | ✅ | Script content |

#### `deleteScriptSet` — Delete ScriptSet
```
POST /api/updateCfg?method=deleteScriptSet&XML_PATH=...&ITEM_NAME=...&SCRIPT_NAME=xxx
```

---

## XML Structure

### EasyWebTemplate

```xml
<EasyWebTemplate Name="ADM_USER.Frame.ChangePWD">
  <Action>
    <ActionSet>
      <Set Type="OnPageLoad">
        <CallSet>
          <Set CallName="OnPageLoad SQL" CallType="SqlSet" Test=""/>
        </CallSet>
      </Set>
    </ActionSet>
    <SqlSet>
      <Set Name="OnPageLoad SQL" SqlType="query" TransType="">
        <Sql><![CDATA[SELECT ...]]></Sql>
      </Set>
    </SqlSet>
    <ScriptSet>
      <Set Name="CHG_OK" ScriptType="javascript">
        <Script><![CDATA[ok()]]></Script>
      </Set>
    </ScriptSet>
  </Action>
  <XItems>
    <XItem Name="ADM_PWD">
      <Tag><Set Tag="password"/></Tag>
      <Name><Set Name="ADM_PWD"/></Name>
      <DescriptionSet>
        <Set Info="New Password" Lang="zhcn" Memo=""/>
        <Set Info="New Password" Lang="enus" Memo=""/>
      </DescriptionSet>
      <DataItem><Set DataField="ADM_PWD" DataType="String" IsEncrypt="1"/></DataItem>
      <MaxMinLength><Set MaxLength="20" MinLength="6"/></MaxMinLength>
      <IsMustInput><Set IsMustInput="1"/></IsMustInput>
      <!-- 50+ parameter nodes -->
    </XItem>
  </XItems>
</EasyWebTemplate>
```

### Key Relationships

- **ActionSet** defines execution flow and call chains (Type → CallSet → SqlSet/ScriptSet)
- **SqlSet** is referenced by ActionSet's `CallName` via its `Name`
- **Test** controls conditional execution (e.g. `'@result'=''` means execute only when result is empty)
- **SqlType**: `query` / `update`
- **TransType**: `no` (no transaction) / `yes` (transaction) / empty (default)
- SQL can return intermediate results (e.g. `@dup_mobile`, `@code_right`) for subsequent steps

---

## Testing

### Running Tests

```bash
mvn test -Dtest=UpdateCfgXmlTest
```

### Test Coverage

| # | Test | Description |
|---|------|------|
| 1 | XML parsing | Verify sample XML can be parsed |
| 2 | Find template | Verify target EasyWebTemplate can be found |
| 10 | List XItem | Verify XItem list structure and fields |
| 20 | Add XItem | Create XItem and verify Tag/Desc/DataItem/IsMustInput |
| 30 | Modify XItem | Modify description and DataField |
| 40 | Delete XItem | Delete and verify no longer exists |
| 50 | List SqlSet | Verify SqlSet list and SQL content |
| 60 | Add SqlSet | Create SqlSet (CDATA multi-line SQL) |
| 70 | Modify SqlSet | Modify SqlType and SQL content |
| 80 | Delete SqlSet | Delete and verify no longer exists |
| 90 | Add ScriptSet | Create ScriptSet |
| 95 | Delete ScriptSet | Delete and verify no longer exists |
| 100 | Multi-line CDATA | Verify multi-line SQL write and read consistency |
| 110 | Reference integrity | Verify ActionSet→SqlSet reference integrity |

Tests use `designs/tb2ewaCfg/examples/admin.xml` as sample, copied to a temporary directory for operations, leaving source files unchanged.

---

## Access Methods

### Frontend Pages

Pages provided via `UPDATE_CFG.xml` configuration:

| Page | Access Method |
|------|---------|
| XML Select | `./?XMLNAME=/ewa/UPDATE_CFG.xml&ITEMNAME=XmlSelect.LF.M` |
| Item Select | `./?XMLNAME=/ewa/UPDATE_CFG.xml&ITEMNAME=ItemSelect.LF.M&XML_PATH=xxx` |
| XItem List | `./?XMLNAME=/ewa/UPDATE_CFG.xml&ITEMNAME=XItemCfg.LF.M&XML_PATH=xxx&ITEM_NAME=yyy` |
| XItem Edit | `./?XMLNAME=/ewa/UPDATE_CFG.xml&ITEMNAME=XItemCfg.F.NM&XML_PATH=xxx&ITEM_NAME=yyy&XITEM_NAME=zzz` |
| SQL List | `./?XMLNAME=/ewa/UPDATE_CFG.xml&ITEMNAME=SqlCfg.LF.M&XML_PATH=xxx&ITEM_NAME=yyy` |
| SQL Edit | `./?XMLNAME=/ewa/UPDATE_CFG.xml&ITEMNAME=SqlCfg.F.NM&XML_PATH=xxx&ITEM_NAME=yyy&SQL_NAME=zzz` |
| Script Edit | `./?XMLNAME=/ewa/UPDATE_CFG.xml&ITEMNAME=ScriptCfg.F.NM&XML_PATH=xxx&ITEM_NAME=yyy&SCRIPT_NAME=zzz` |

---

## Implementation Status

- [x] Analyze sample XML structure (admin.xml, index.xml)
- [x] Write plan document
- [x] Write UpdateCfgXml.java — 14 API methods
- [x] Write UpdateCfgAction.java — Servlet entry + authorization
- [x] Write UPDATE_CFG.xml — 7 EasyWebTemplate frontend pages
- [x] Write DTO classes — UpdateCfgXItemData / UpdateCfgSqlSetData / UpdateCfgScriptSetData
- [x] Write ApiTokenValidator — Token validator
- [x] Write unit tests — All 14 test methods passing
- [ ] End-to-end deployment testing
- [ ] SQL test execution functionality
