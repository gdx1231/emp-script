# BusinessXmlCreator Design Guide

## I. Overview

`BusinessXmlCreator` is the core module in the emp-script framework for **automatically generating EWA business XML configurations based on database table structures**. Through configuration-driven approaches and field inference, it achieves automated generation of Frame/ListFrame/Tree page configurations from database tables, significantly reducing manual configuration effort.

### Core Objectives
- **Reduce configuration cost**: Automatically generate 80%+ of XML configuration from database table structures
- **Configuration-driven**: Field rules, Tag types, default values, etc. are all externalized in configuration
- **Multi-database support**: Supports MySQL, SQL Server, HSQLDB, and other databases
- **Extensible**: Easily support new Frame types by extending the base class

### Branch
`cr_ewa_cfg` (not yet merged to main)

---

## II. Architecture Design

### 2.1 Class Structure

```
BusinessXmlCreator (Factory class)
    │
    ├── BusinessXmlCreatorBase (Abstract base class, 837 lines)
    │       ├── Common SQL generation methods
    │       ├── Field inference logic
    │       ├── Configuration read wrappers
    │       └── XML node creation utilities
    │
    ├── BusinessXmlCreatorListFrame (List mode, 1175 lines)
    │       ├── List page XML generation
    │       ├── OrderSearch auto-configuration
    │       └── Pagination / recycle bin support
    │
    ├── BusinessXmlCreatorFrame (Form mode, 951 lines)
    │       ├── Form page XML generation
    │       ├── Tag type suffix matching
    │       └── Special editor inference
    │
    └── BusinessXmlCreatorTree (Tree mode, 850 lines)
            ├── Tree page XML generation
            ├── Tree Action SQL auto-generation
            ├── Field name inference (PID/LVL/ORD/NAME)
            └── Logical delete support
```

### 2.2 Design Patterns

| Pattern | Application |
|------|------|
| **Factory Pattern** | `BusinessXmlCreator.create()` creates instances based on frameType |
| **Template Method** | `BusinessXmlCreatorBase.create()` defines generation flow; subclasses implement details |
| **Strategy Pattern** | Different Frame types use different XML generation strategies |
| **Singleton Pattern** | `EwaDefineSettings.getInstance()` for configuration reading |

---

## III. Core Class Descriptions

### 3.1 BusinessXmlCreator (Factory Class)

```java
// Usage example
BusinessXmlCreator creator = BusinessXmlCreator.create(config, table, "ListFrame");
String xml = creator.createShowXml(db, tableName, selectSql, tableJson, "ListFrame", "NM");
boolean saved = creator.createAndSave(db, tableName, selectSql, tableJson, "ListFrame", "NM", xmlName, itemName, admId);
```

| Method | Description |
|------|------|
| `create(config, table, frameType)` | Static factory method, creates creator of specified type |
| `createAndSave(...)` | Generate and save XML to configuration repository |
| `createShowXml(...)` | Generate XML string for preview |
| `getCreator()` | Get the underlying creator instance |

### 3.2 BusinessXmlCreatorBase (Abstract Base Class)

#### Abstract Methods (Must be implemented by subclasses)

| Method | Description |
|------|------|
| `create(frameType, operationType)` | Create XML Document |
| `save(xmlContent, xmlName, itemName, admId)` | Save XML to configuration repository |

#### Common SQL Generation Methods

| Method | Description |
|------|------|
| `getSqlTreeLoad()` | Tree load SQL, supports status field WHERE conditions |
| `getSqlTreeNodeDelete()` | Tree node delete SQL (logical delete: UPDATE SET status='DEL') |
| `getSqlTreeNodeRename()` | Tree rename SQL |
| `getSqlTreeNodeNew()` | Tree new node SQL (CASE WHEN compatible with multiple databases) |
| `getSqlSelectLF(statusField, includeRecycle)` | ListFrame query SQL |
| `getSqlDeleteA(statusField)` | Logical delete SQL |
| `getSqlRestore(statusField)` | Restore data SQL |
| `getSqlDelete()` | Physical delete SQL |
| `getSqlSelect()` | Single record load SQL |
| `getSqlUpdate(statusField)` | Update SQL |
| `getSqlNew(statusField)` | Insert SQL |

#### Field Inference Methods

| Method | Description |
|------|------|
| `findFieldBySuffix(suffix)` | Find field with specified suffix (_PID, _LVL, _ORD, _NAME, etc.) |
| `findStatusField()` | Find status field (_STATUS or _STATE) |
| `findIdentityField()` | Find auto-increment field |
| `getPrimaryKeyField()` | Get primary key field name |
| `shouldHideField(field)` | Determine whether a field should be hidden |

#### Configuration Reading Methods

| Method | Description |
|------|------|
| `getTagType(dbType, fieldName, frameType)` | Get UI control type |
| `getDataType(dbType)` | Database type → data type mapping |
| `getFormat(dbType)` | Date / number format |
| `getNumberScale(dbType)` | Number precision |
| `getValidType(dbType)` | Validation type (Email/Url/Number) |

#### Utility Methods

| Method | Description |
|------|------|
| `processJsExpression(val)` | Process JavaScript expression placeholders |
| `getFrameTypeShort(frameType)` | Frame type abbreviation (F/LF/T) |
| `getFrameName(frameType)` | Frame Chinese name |
| `getOperationName(operationType)` | Operation Chinese name (N=New/M=Modify/V=View) |
| `createDescriptionSet(doc, descMap)` | Create multilingual description node |
| `findChildElement(parent, tagName)` | Find child element |

### 3.3 BusinessXmlCreatorListFrame

**Responsibility**: Generate List mode (ListFrame) XML configurations

#### Key Features

1. **OrderSearch Auto-Configuration**
   ```
   Numeric types (INT/DECIMAL/NUM) → IsOrder=1, SearchType=""
   Date types (DATE/TIME)          → IsOrder=1, SearchType=""
   Short text (CHAR≤100)           → IsOrder=1, SearchType="text"
   Long text (CHAR>100/TEXT)       → IsOrder=0, SearchType="text"
   ```

2. **PageSize Configuration** (read from EwaDefineSettings)
   - `IsSplitPage`: Whether to paginate
   - `PageSize`: Records per page
   - `Recycle`: Recycle bin support
   - `KeyField`: Primary key field

3. **ListUI Configuration**
   - `luButtons`: Button layout
   - `luSearch`: Search bar
   - `luSelect`: Selection method

4. **Buttons Added as XItems**
   - Read from ButtonConfigs in EwaDefine.xml
   - Supports standard buttons like butDelete, butRestore

### 3.4 BusinessXmlCreatorFrame

**Responsibility**: Generate Form mode (Frame) XML configurations

#### Key Features

1. **Size Configuration** (read from EwaDefineSettings)
   - `Width`: Page width (default 700)
   - `HiddenCaption`: Hide caption
   - `FrameCols`: Column count (C2=two columns)
   - `HAlign/VAlign`: Alignment

2. **Tag Type Suffix Matching**
   ```
   Field name suffix → Tag type
   _SQL              → sqlEditor
   _XML              → xmlEditor
   _JSON             → jsonEditor
   _HTML             → dHtml5
   _CSS              → cssEditor
   ```

3. **Field Type Mapping**
   ```
   DATETIME          → datetime
   TEXT/CLOB         → textarea
   INT/DECIMAL       → number
   CHAR/VARCHAR      → text
   ```

### 3.5 BusinessXmlCreatorTree

**Responsibility**: Generate Tree mode (Tree) XML configurations

#### Key Features

1. **Tree Configuration Field Inference**
   ```
   Tree attribute → Inference rule
   Text           → Find _NAME suffix field
   Title          → Find _TITLE suffix field
   Key            → Primary key field
   ParentKey      → Find _PID suffix field
   Level          → Find _LVL suffix field
   Order          → Find _ORD suffix field
   ```

2. **Tree Action SQL Auto-Generation**
   - `OnPageLoad SQL`: Load tree nodes (sorted by LVL, ORD)
   - `OnTreeNodeDelete SQL`: Logical delete (UPDATE status='DEL')
   - `OnTreeNodeRename SQL`: Rename node
   - `OnTreeNodeNew SQL`: New node (CASE WHEN compatible with multiple databases)

3. **Tree Menu**
   - `itemNew`: Create new node
   - `itemRename`: Rename
   - `itemDelete`: Delete node
   - Separator (line)

4. **HtmlFrame Configuration**
   - `FrameType`: H5
   - `FrameSize`: 200,* (left 200px tree, right adaptive)

---

## IV. Configuration System

### 4.1 EwaDefineSettings.xml

**Path**: `src/main/resources/system.xml/EwaDefineSettings.xml`

#### Frame Field Hide Rules

```xml
<FrameHideRules>
    <!-- Hide by suffix -->
    <SuffixRule suffix="_CDATE" />
    <SuffixRule suffix="_MDATE" />
    <SuffixRule suffix="_STATUS" />
    <SuffixRule suffix="_STATE" />
    <!-- Hide by type -->
    <TypeRule type="IDENTITY" />
    <!-- Hide by name -->
    <NameRule name="ID" />
</FrameHideRules>
```

#### Frame Page Settings

```xml
<FramePageSettings>
    <Width>700</Width>
    <HiddenCaption>1</HiddenCaption>
    <FrameCols>C2</FrameCols>
    <HAlign>center</HAlign>
    <VAlign>top</VAlign>
</FramePageSettings>
```

#### ListFrame Page Settings

```xml
<ListFramePageSettings>
    <PageSize>10</PageSize>
    <PageSizeIsSplit>1</PageSizeIsSplit>
    <Recycle>1</Recycle>
    <LuButtons>1</LuButtons>
    <LuSearch>1</LuSearch>
    <LuSelect>listUI</LuSelect>
</ListFramePageSettings>
```

#### FieldTags Configuration (Defined separately for Frame and ListFrame)

```xml
<FieldTags frameType="Frame">
    <Tag dbType="DATETIME" tag="datetime" />
    <Tag dbType="TEXT" tag="textarea" />
    <Tag suffix="_SQL" tag="sqlEditor" />
    <Tag suffix="_XML" tag="xmlEditor" />
    <Tag suffix="_JSON" tag="jsonEditor" />
    <Tag suffix="_HTML" tag="dHtml5" />
</FieldTags>
```

#### Field Format Configuration

```xml
<FieldFormats>
    <Format dbType="DATE" format="DateShortTime" />
    <Format dbType="DATETIME" format="DateShortTime" />
    <Format dbType="INT" format="" />
    <Format dbType="DECIMAL" format="LeastMoney" />
</FieldFormats>
```

#### Field Default Value Configuration

```xml
<FieldDefaultValues>
    <DefaultValue suffix="_CDATE" value="@SYS_DATE" />
    <DefaultValue suffix="_UNID" value="@sys_unid" />
    <DefaultValue suffix="_CREATOR" value="@g_adm_id" />
    <DefaultValue suffix="_MODIFIER" value="@g_adm_id" />
    <!-- Supports Where attribute -->
    <DefaultValue name="SUP_ID" value="@g_sup_id" where="true" />
</FieldDefaultValues>
```

#### Status Field Configuration

```xml
<StatusField>
    <Suffix>_STATUS</Suffix>
    <DefaultValue>'USED'</DefaultValue>
    <DeleteValue>'DEL'</DeleteValue>
    <Where>true</Where>
</StatusField>
```

### 4.2 EwaDefine.xml

**Path**: `src/main/resources/system.xml/EwaDefine.xml`

Defines Frame templates, button configurations, Action SQL, AddScript/CSS, etc.

```xml
<EwaDefine>
    <Frame name="ListFrame" tmp="M">
        <Buttons>
            <Button name="butNew" tag="button" />
            <Button name="butDelete" tag="button" />
        </Buttons>
        <Actions>
            <Action name="OnPageLoad" sqlType="query">
                <Sql>SELECT * FROM ...</Sql>
            </Action>
        </Actions>
        <Adds>
            <AddScript><Top>...</Top><Bottom>...</Bottom></AddScript>
            <AddCss>...</AddCss>
        </Adds>
    </Frame>
</EwaDefine>
```

---

## V. Field Inference Rules

### 5.1 Tree Field Inference

| Suffix | Purpose | Example |
|------|------|------|
| `_PID` | Parent ID | `SUP_PID`, `CAT_PID` |
| `_LVL` | Level | `SUP_LVL`, `CAT_LVL` |
| `_ORD` | Sort order | `SUP_ORD`, `CAT_ORD` |
| `_NAME` | Name | `SUP_NAME`, `CAT_NAME` |

### 5.2 Audit Field Inference

| Suffix | Purpose | Default Value |
|------|------|--------|
| `_CDATE` | Created time | `@SYS_DATE` |
| `_MDATE` | Modified time | `@SYS_DATE` |
| `_CREATOR` | Created by | `@g_adm_id` |
| `_MODIFIER` | Modified by | `@g_adm_id` |
| `_STATUS` | Status | `'USED'` |
| `_STATE` | Status (alternative) | `'USED'` |
| `_UNID` | Unique ID | `@sys_unid` |

### 5.3 Hide Rules

The following fields are **automatically hidden** in the generated XML:
- Auto-increment primary key (Identity)
- `**_CDATE` (Created time)
- `**_MDATE` (Modified time)
- `**_STATUS` / `**_STATE` (Status fields)

---

## VI. SQL Generation Logic

### 6.1 Tree Load SQL

```sql
SELECT * FROM table_name
WHERE status_field='USED'
  AND where_field=@default_value
ORDER BY level_field, order_field
```

### 6.2 Tree New Node SQL (MySQL)

```sql
INSERT INTO table_name (name, pid, lvl, ord, cdate, status)
SELECT 
    @EWA_TREE_TEXT name,
    CASE WHEN @EWA_TREE_PARENT_KEY IS NULL THEN 0 ELSE @EWA_TREE_PARENT_KEY END pid,
    CASE WHEN MAX(pp.lvl) IS NULL THEN -1 ELSE MAX(pp.lvl) END+1 lvl,
    CASE WHEN MAX(pc.ord) IS NULL THEN 0 ELSE MAX(pc.ord) END+1 ord,
    @SYS_DATE cdate,
    'USED' status
FROM table_name pp
LEFT JOIN table_name pc ON pc.pid=pp.pk
WHERE pp.pid = @EWA_TREE_PARENT_KEY
```

### 6.3 Tree Delete Node SQL (Logical Delete)

```sql
-- When status field exists
UPDATE table_name SET status='DEL' WHERE pk = @pk AND where_field=@default_value

-- When no status field
DELETE FROM table_name WHERE pk = @pk AND where_field=@default_value
```

### 6.4 ListFrame Query SQL

```sql
SELECT A.* FROM table_name A WHERE 1=1
    -- @EWA_RECYCLE is null
    AND A.status = 'USED'
    -- @EWA_RECYCLE = '1'
    AND A.status = 'DEL'
    --
ORDER BY A.mdate DESC
```

---

## VII. Interaction with Define Module

### 7.1 Dependencies

```
BusinessXmlCreator
    ├── EwaConfig (script.template)     # EWA configuration templates
    ├── Table/Field (define.database)   # Database table structures
    ├── EwaDefineConfig                 # Read EwaDefine.xml template configuration
    ├── EwaDefineSettings               # Read EwaDefineSettings.xml rule configuration
    └── UXml (utils)                    # XML utilities
```

### 7.2 Position in Define Module

```
define/servlets/
    ├── ServletXml.java          # Main XML management Servlet
    ├── ServletApi.java          # RESTful API Servlet
    └── [TBD] ServletBusinessXmlCreator  # Business XML generation API

define/bussinessXmlCreator/
    ├── BusinessXmlCreator.java           # Factory class
    ├── BusinessXmlCreatorBase.java       # Abstract base class
    ├── BusinessXmlCreatorListFrame.java  # List mode
    ├── BusinessXmlCreatorFrame.java      # Form mode
    ├── BusinessXmlCreatorTree.java       # Tree mode
    ├── EwaDefineConfig.java              # Template configuration reader
    ├── EwaDefineSettings.java            # Rule configuration reader
    ├── BusinessXmlCreateParams.java      # Creation parameters
    └── SqlValidator.java                 # SQL validator
```

---

## VIII. API Interface

### 8.1 Servlet Routing

The API has been integrated into the existing `ServletApi.java`, route: `/EWA_DEFINE/cgi-bin/api/`

### 8.2 Interface List

#### Preview XML (Without Saving)

```
POST /EWA_DEFINE/cgi-bin/api/
Parameters:
  - method: previewBusinessXml
  - db: Database name
  - tablename: Table name
  - frametype: ListFrame/Frame/Tree
  - operationtype: N/M/V/NM
  - xmlname: Config file name (e.g. ewa/m)
  - output: xml/json (optional, default xml)
Returns:
  {
    "RST": true,
    "XMLNAME": "ewa/m",
    "FRAMETYPE": "LISTFRAME",
    "OPERATIONTYPE": "M",
    "OUTPUT": "xml",
    "XML": "<EasyWebTemplates>...</EasyWebTemplates>"
  }
```

#### Save XML

```
POST /EWA_DEFINE/cgi-bin/api/
Parameters:
  - method: createBusinessXml
  - db: Database name
  - tablename: Table name
  - frametype: ListFrame/Frame/Tree
  - operationtype: N/M/V/NM
  - xmlname: Config file name (e.g. ewa/m)
  - itemname: Config item name (e.g. CRM_COM.LF.M)
  - admid: Admin ID (optional, uses authenticated user)
Returns:
  {
    "RST": true,
    "MSG": "Business XML created and saved successfully",
    "XMLNAME": "ewa/m",
    "ITEMNAME": "CRM_COM.LF.M",
    "FRAMETYPE": "LISTFRAME",
    "OPERATIONTYPE": "M"
  }
```

### 8.3 Call Flow

```
User request
    └── ServletApi.processRequest()
            ├── 1. Authentication check (ApiTokenValidator)
            ├── 2. Parameter validation (db/tablename/frametype/operationtype/xmlname/itemname)
            ├── 3. Read database table structure (Table.readStructure(db, tableName))
            ├── 4. Create EwaConfig instance
            ├── 5. BusinessXmlCreator.create(config, table, frameType)
            ├── 6. creator.createShowXml(...) Generate XML (preview)
            └── 7. creator.createAndSave(...) Save to config repository (save)
```

### 8.4 Position in Define Module

```
define/servlets/
    ├── ServletXml.java          # Main XML management Servlet
    ├── ServletApi.java          # RESTful API Servlet (BusinessXmlCreator integrated)
    ├── ServletGroup.java        # Component management Servlet
    └── ServletWorkflow.java     # Workflow Servlet

define/bussinessXmlCreator/
    ├── BusinessXmlCreator.java           # Factory class
    ├── BusinessXmlCreatorBase.java       # Abstract base class
    ├── BusinessXmlCreatorListFrame.java  # List mode
    ├── BusinessXmlCreatorFrame.java      # Form mode
    ├── BusinessXmlCreatorTree.java       # Tree mode
    ├── EwaDefineConfig.java              # Template configuration reader
    ├── EwaDefineSettings.java            # Rule configuration reader
    ├── BusinessXmlCreateParams.java      # Creation parameters
    └── SqlValidator.java                 # SQL validator
```

---

## IX. Test Coverage

### 9.1 Existing Tests

| Test Class | Test Content |
|--------|----------|
| `BusinessXmlCreateTest` | ListFrame.M / ListFrame.V / Frame.NM creation |
| `DatabaseTableToXmlTest` | Full database table to XML flow |
| `CreateBusinessXmlFromDatabase` | Create business XML from database (merged config) |
| `TreeSqlWithStatusTest` | Tree SQL status field logical delete |
| `CreateBasTagTreeFromSqlServerTest` | SQL Server bas_TAG Tree creation |
| `CreateProductCatTreeTest` | Product category Tree creation |
| `CreateBusinessXmlFromDbDemo` | Demo database full flow test |

### 9.2 Test Data

- HSQLDB in-memory database (`conf.help.hsqldb`)
- SQL Server connection tests (optional)
- `CRM_COM` table structure tests

---

## X. Extension Guide

### 10.1 Adding a New Frame Type

```java
public class BusinessXmlCreatorNewType extends BusinessXmlCreatorBase {
    
    public BusinessXmlCreatorNewType(EwaConfig config, Table table) {
        super(config, table);
    }
    
    @Override
    protected Document create(String frameType, String operationType) throws Exception {
        // Implement XML generation logic
    }
    
    @Override
    protected boolean save(String xmlContent, String xmlName, String itemName, String admId) {
        // Implement save logic
        return true;
    }
}
```

Then add a branch in `BusinessXmlCreator.create()`:

```java
} else if ("NewType".equalsIgnoreCase(frameType)) {
    baseCreator = new BusinessXmlCreatorNewType(config, table);
}
```

### 10.2 Adding New Field Inference Rules

Simply add configuration in `EwaDefineSettings.xml` — no code changes required.

### 10.3 Adding New Database Support

In `BusinessXmlCreatorBase.getSqlTreeNodeNew()`, the current CASE WHEN syntax already supports MySQL and SQL Server. For other databases, add specific syntax in this method.

---

## XI. Version History

| Commit | Description |
|------|------|
| `c27d2a4` | refactor: Move Fields.GetSql* methods to BusinessXmlCreatorBase |
| `7145654` | fix: WHERE condition uses configured default value |
| `f4e7036` | feat: Add DeleteValue attribute for status field |
| `153c1e1` | feat: Add Where attribute for field default value config |
| `509c892` | feat: Add field default value config to EwaDefineSettings |
| `4d24458` | refactor: Move Tree SQL generation methods to BusinessXmlCreatorBase |
| `a356fe9` | feat: Tree SQL supports status fields (_STATUS/_STATE) |
| `aeb0ef4` | refactor: GetSqlTreeNodeNew uses CASE WHEN for cross-database compatibility |
| `de75fe5` | feat: GetSqlTreeNodeNew references SQL Server tree insert template |
| `8107cda` | feat: Tree Action SQL reads from EwaDefine.xml and auto-generates |
| `3920a76` | feat: BusinessXmlCreatorTree infers Tree config from actual field names |
| `1974e1f` | feat: Add BusinessXmlCreatorTree supporting FrameTree config |
| `b3f4e74` | fix: Use INFORMATION_SCHEMA to read HSQL table structure |
| `3a3b7a9` | feat: Support field suffix matching for special editors |
| `493dfb8` | refactor: Refactor BusinessXmlCreator into base class and subclasses |

---

## XII. Notes

1. **Branch status**: Current code is in the `cr_ewa_cfg` branch, not yet merged to `main`
2. **save method**: The `save()` method is currently a placeholder (`TODO`), needs integration with the `IUpdateXml` interface
3. **API interface**: ~~Not yet created~~ Integrated into `ServletApi.java`, supports `previewBusinessXml` and `createBusinessXml` methods
4. **SQL validation**: `SqlValidator` only supports basic syntax checking; complex SQL requires manual verification
5. **Database connection**: Ensure the database connection is available, otherwise `Table.readStructure()` will fail
