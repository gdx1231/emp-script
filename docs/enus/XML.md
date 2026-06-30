# emp-script XML Configuration Files Reference

This document describes the structure and purpose of XML configuration files in the `src/main/resources` directory.

## Directory Structure

```
src/main/resources/
├── define.xml/              # System definition configuration directory
│   └── ewa/                 # EWA core configuration
│       ├── ewa.xml          # System template definitions
│       ├── m.xml            # Admin function templates
│       ├── database.xml     # Database-related templates
│       ├── ewa_restful.xml  # RESTful API configuration templates
│       └── ...
├── system.xml/              # System-level configuration directory
│   ├── EwaConfig.xml        # Main configuration file
│   ├── EwaDefine.xml        # Definition configuration
│   ├── EwaFunctions.xml     # Function definitions
│   ├── EwaGlobal.xml        # Global variables
│   ├── EwaSkin.xml          # Skin/Theme configuration
│   ├── EwaWorkflow.xml      # Workflow configuration
│   └── database/TypesMap.xml # Database type mapping
├── ewa_conf_tmp.xml         # Configuration file template
├── web-template.xml         # Web application deployment template
└── web-template.xml         # Web application configuration template
```

---

## Core Configuration Files

### 1. ewa_conf_tmp.xml - Application Configuration Template

**Purpose**: EWA application main configuration file template, used for configuring databases, caching, security, paths, etc.

**Main Configuration Nodes**:

| Node | Description |
|------|------|
| `<admins>` | Admin account configuration |
| `<sqlCached>` | Cache method configuration (hsqldb/redis) |
| `<securities>` | Security configuration (AES encryption) |
| `<requestValuesGlobal>` | Global request variables |
| `<scriptPaths>` | Script path configuration |
| `<addedResources>` | Additional resources (JS/CSS) |
| `<paths>` | System path configuration |
| `<debug>` | Debug IP configuration |
| `<smtps>` | SMTP email configuration |
| `<databases>` | Database connection pool configuration |
| `<restfuls>` | RESTful API configuration |
| `<remote_syncs>` | Remote sync configuration |

**Example**:
```xml
<ewa_confs>
    <!-- Cache configuration -->
    <sqlCached cachedMethod="hsqldb"/>
    
    <!-- Security configuration -->
    <securities>
        <security name="default" algorithm="aes-192-gcm" 
                  key="efsd91290123p9023sdkjvjdkl293048192"/>
    </securities>
    
    <!-- Database configuration -->
    <databases>
        <database name="work" type="MYSQL">
            <pool driverClassName="com.mysql.cj.jdbc.Driver" 
                  url="jdbc:mysql://localhost:3306/db"/>
        </database>
    </databases>
</ewa_confs>
```

---

### 2. web-template.xml - Web Application Deployment Template

**Purpose**: Java Web application `web.xml` deployment descriptor template.

**Configured Servlets**:

| Servlet Name | Class | URL Mapping | Purpose |
|-------------|-----|---------|------|
| EwaMain | `ServletMain` | `/ewa`, `/EWA_STYLE/cgi-bin/index.jsp` | Main entry |
| EwaUpload | `ServletUpload` | `/EWA_STYLE/cgi-bin/_up_/index.jsp` | File upload |
| EwaCode | `ServletCode` | `/EWA_STYLE/cgi-bin/_co_/index.jsp` | Verification code |
| EwaResources | `ServletResources` | `/r.ewa`, `/r1.ewa` | Static resources |
| EwaStatus | `ServletStatus` | `/EWA_STYLE/cgi-bin/_st_/index.jsp` | Tree status |
| EwaError | `ServletError` | `/EWA_STYLE/cgi-bin/_er_/index.jsp` | Error handling |
| EwaWorkflow | `ServletWorkflow` | `/EWA_STYLE/cgi-bin/_wf_/index.jsp` | Workflow |
| EwaDefineXml | `ServletXml` | `/EWA_DEFINE/cgi-bin/xml/index.jsp` | Definition XML |
| EwaDefineIndex | `ServletIndex` | `/EWA_DEFINE/index.jsp` | Definition index |

---

### 3. system.xml/EwaConfig.xml - System Core Configuration

**Purpose**: Defines system frame types, page elements (XItem), form types, etc.

**Main Configuration**:

#### Frames (Form Types)
- Frame - Input/edit interface
- ListFrame - Table list
- Grid - Grid display
- Tree - Tree structure
- Menu - Menu
- MultiGrid - Multi-dimensional table
- Logic - Logic control
- Report - Report
- Combine - Combined page
- Complex - Composite page

#### XItems (Page Elements)
Standard elements:
- `text` - Single-line text
- `textarea` - Multi-line text
- `password` - Password
- `date/datetime/time` - Date/time
- `select` - Dropdown select
- `checkbox/radio` - Checkbox/radio
- `switch` - Switch
- `button/submit` - Button
- `anchor` - Link
- `droplist` - Dynamic list
- `combo` - Combo box

Extended elements:
- `dHtml5` - HTML5 editor
- `markDown` - Markdown editor
- `h5upload` - HTML5 upload
- `file` - File upload (deprecated)

---

### 4. system.xml/EwaFunctions.xml - System Functions

**Purpose**: Defines system built-in functions callable from XML configurations.

| Function Name | Class/Method | Description |
|--------|--------|------|
| `password_hash` | `UArgon2.hashPwd` | Argon2 password encryption |
| `password_verify` | `UArgon2.verifyPwd` | Argon2 password verification |
| `encrypt` | `UAes.defaultEncrypt` | AES encryption |
| `decrypt` | `UAes.defaultDecrypt` | AES decryption |
| `md5` | `Utils.md5` | MD5 hash |
| `sha1` | `Utils.sha1` | SHA1 hash |
| `digest` | `UDigest.digestHex` | Digest algorithm (supports multiple) |
| `http_get` | `UNet.doGet` | HTTP GET request |
| `snowflake` | `USnowflake.nextId` | Twitter snowflake ID |
| `send_mail` | `UMail.sendHtmlMail` | Send email |

---

### 5. system.xml/EwaGlobal.xml - Global Variables

**Purpose**: Defines multilingual global variables, system messages, calendar formats, etc.

**Configuration Contents**:
- Calendar configuration (months, weeks, date format)
- Currency symbols
- System messages (Chinese/English)
- Validation error prompts
- Button text (OK/Cancel/Yes/No)
- Delete/Update prompt messages

**Example**:
```xml
<Globals>
    <Global Lang='zhcn'>
        <Calendar>
            <Date>yyyy-MM-dd</Date>
            <Currency>￥</Currency>
        </Calendar>
    </Global>
    <Global Lang='enus'>
        <Calendar>
            <Date>MM/dd/yyyy</Date>
            <Currency>$</Currency>
        </Calendar>
    </Global>
</Globals>
```

---

### 6. system.xml/EwaSkin.xml - Skin Theme

**Purpose**: Defines page HTML templates and skin styles.

**Supported Template Types**:
- `Head` - HTML4 standard
- `HeadXHtml` - XHTML version
- `HeadH5` - HTML5 mobile version
- `HeadVue` - Vue integration version

**Included Resources**:
- Font Awesome 4.7.0 icon library
- jQuery 3.6.0
- EWA core JS/CSS
- Autosize auto-resize textarea

---

### 7. system.xml/database/TypesMap.xml - Database Type Mapping

**Purpose**: Defines data type mapping rules between different databases.

**Supported Databases**:
- MySQL
- SQL Server (MSSQL)
- Oracle
- HSQLDB
- PostgreSQL

**Mapping Example**:
```xml
<Map Name="VARCHAR">
    <Database Name="MYSQL">
        <MapTo Name="VARCHAR"/>
        <MapTo Name="TINYTEXT"/>
    </Database>
    <Database Name="MSSQL">
        <MapTo Name="NVARCHAR" Scale="1"/>
        <MapTo Name="VARCHAR"/>
    </Database>
</Map>
```

---

## define.xml Configuration

### 8. define.xml/ewa/ewa.xml - System Templates

**Purpose**: Defines system built-in configuration templates for rapid page generation.

**Included Templates**:
- `frames_fields_list` - Configuration item list
- `frames_fields` - Configuration item editing
- `define_left` - Define management left frame
- `define_right` - Define management right frame
- `define_tree` - Define management tree

---

### 9. define.xml/ewa/m.xml - Admin Function Templates

**Purpose**: Template collection for system admin functions.

**Included Templates**:
- `ChangePWD` - Change password
- `admAdd` - Admin creation
- `admlist` - Admin list
- `ewa_conf` - Config file management
- `ewa_module` - Module management

---

### 10. define.xml/ewa/database.xml - Database Templates

**Purpose**: Templates for database-related operations.

**Included Templates**:
- `cr_view` - Create view
- `tables_same` - Table consistency check
- `table_field` - Table field management
- `db_connect` - Database connection

---

### 11. define.xml/ewa/ewa_restful.xml - RESTful API Templates

**Purpose**: RESTful API configuration and management templates.

**Included Templates**:
- `ewa_restful.F.NM` - RESTful config create/edit
- `ewa_restful.F.bat` - Batch generate RESTful config
- `ewa_restful.LF.M` - RESTful list management
- `ewa_restful_catalog.*` - Catalog management

**RESTful Configuration Example**:
```xml
<restful path="chatUsers">
    <get xmlname="/ewa-apps/chat.xml" itemname="chat_user.LF.M"/>
    <post xmlname="/ewa-apps/chat.xml" itemname="chat_user.F.NM"/>
    <delete xmlname="/ewa-apps/chat.xml" itemname="chat_user.LF.M"/>
    <patch xmlname="/ewa-apps/chat.xml" itemname="chat_user.LF.M"/>
</restful>
```

---

## Other Configuration Files

### 12. define.xml/ewa/ewa_conf_help.xml
Template definitions for the configuration help system.

### 13. define.xml/ewa/ewa_module.xml
Module import/export function templates.

### 14. define.xml/ewa/ewa_wf.xml
Workflow-related templates.

### 15. define.xml/ewa/group1.xml
Group configuration templates.

### 16. define.xml/ewa/tmp_img_resze.xml
Image resizing processing template.

---

## EwaDefine Related Configuration under system.xml

### EwaDefineConfig.xml - Define Management Interface Menu

**Purpose**: Defines the top menu structure of the EWA configuration management interface (EWA_DEFINE).

**Menu Position**: Top navigation bar of the management interface

**Menu Structure**:

| ID | Menu Name | Function Description |
|----|---------|---------|
| 10 | Save | Save current configuration |
| 20 | Adjust Info | Dropdown menu containing: Adjust Items, Adjust Menus, Adjust Info, Adjust Charts, Adjust Workflow |
| 50 | Components | Component management: Create Release, Download Import, Component Exchange Center |
| 60 | Tools | Development tools: Restful API, Database Mapping, Cache Viewer, Process Designer, etc. |
| 94 | Users | User management: User List, Change Password, BUG Tracking |
| 90 | Help | Help documentation, Theme Switch (Dark/Light), About |
| 99 | Exit | Exit system |

**Menu Properties**:
- `mcmd`: JavaScript command executed on click
- `micon`: Menu icon (Font Awesome class name or image path)
- `mtext`: Chinese display text
- `mtext_enus`: English display text
- `pid`: Parent menu ID (0 means top-level menu)
- `mid`: Unique menu ID

**Example**:
```xml
<Menu mcmd="saveXml();" micon="fa fa-save" mid="10" 
      mtext="保存" mtext_enus="Save" pid="0" />

<Menu micon="fa fa-plug" mid="60" 
      mtext="工具&lt;i class='fa fa-caret-down'>&lt;i>" 
      mtext_enus="Utils" pid="0" />

<Menu mcmd="window.open('./?XMLNAME=/ewa/ewa_restful.xml&amp;itemname=ewa_restful_catalog.T.Modify')"
      micon="fa fa-microchip" mid="600" 
      mtext="Restful API" mtext_enus="Restful API" pid="60" />
```

**Java Invocation**:
```java
EwaDefineMenu menu = new EwaDefineMenu();
DTTable menus = menu.getMenus();
// Returns a DataTable of all menu items
```

---

### EwaDefine.xml - Definition Generator Templates

**Purpose**: Defines the steps and templates for the definition configuration generator, used to quickly generate CRUD configurations for database tables.

**Main Configuration**:

#### 1. Generation Steps (Steps)
```xml
<Steps>
    <Step AfterEval="this.CreateMainInfo();">
        <DescriptionSet><Set Info="Step 1: Select a table from the database tree"/></DescriptionSet>
    </Step>
    <Step Eval="this.SelectType()">
        <DescriptionSet><Set Info="Step 2: Select type"/></DescriptionSet>
    </Step>
    <Step Eval="this.ModifyMainInfo();">
        <DescriptionSet><Set Info="Step 3: Modify main info"/></DescriptionSet>
    </Step>
    <Step Eval="this.ModifyFields();" Using="Frame,ListFrame">
        <DescriptionSet><Set Info="Step 4: Select fields"/></DescriptionSet>
    </Step>
    <Step Eval="this.ModifySQL();">
        <DescriptionSet><Set Info="Step 5: Modify SQL statements"/></DescriptionSet>
    </Step>
</Steps>
```

#### 2. Frame Templates (Tmp)
Preset templates defined for each Frame type:

```xml
<Frame Name="Frame">
    <!-- NM Template: Create/Edit -->
    <Tmp Name="NM">
        <Button Name="butOk" Tag="submit"/>
        <Button Name="butClose" Tag="button"/>
        <Action Name="OnNew" SqlType="update"/>
        <Action Name="OnModify" SqlType="update"/>
    </Tmp>
    
    <!-- V Template: View -->
    <Tmp Name="V">
        <Button Name="butClose" Tag="button"/>
        <Action Name="OnPageLoad" SqlType="query"/>
    </Tmp>
    
    <!-- Login Template: Login -->
    <Tmp Name="Login">
        <Button Name="_EWA_ValidCode" Tag="valid"/>
        <Button Name="butOk" Tag="submit"/>
    </Tmp>
</Frame>

<Frame Name="ListFrame">
    <!-- V Template: Browse -->
    <Tmp Name="V">
        <Action Name="OnPageLoad" SqlType="query"/>
    </Tmp>
    
    <!-- M Template: Editable List -->
    <Tmp Name="M" ReleateFrame="Frame" ReleateTmp="NM">
        <Button Name="butNew" Tag="button"/>
        <Button Name="butModify" Tag="button"/>
        <Button Name="butDelete" Tag="button"/>
        <Action Name="OnFrameDelete" SqlType="update"/>
    </Tmp>
</Frame>
```

**Use Cases**:
- In the EWA_DEFINE interface, automatically generate configurations after selecting a database table
- Based on the selected Frame type and template type, quickly generate CRUD page configurations
- Reduce manual configuration workload

---

## XML Configuration Naming Conventions

### Configuration Item Naming Rules
```
{TableName}.{Frame Type}.{Template Type}
```

**Frame Types**:
- `F` - Frame (form)
- `LF` - ListFrame (list)
- `T` - Tree (tree)
- `M` - Menu (menu)
- `G` - Grid (grid)
- `MG` - MultiGrid (multi-dimensional table)

**Template Types**:
- `N` - New
- `M` - Modify
- `V` - View
- `NM` - New/Modify
- `LF.M` - List editable

**Examples**:
- `users.F.NM` - User form new/modify
- `orders.LF.M` - Order list editable
- `products.T.V` - Product tree view

---

## Configuration File Best Practices

1. **ewa_conf.xml** - Project main configuration file, copy from template and modify
2. **Database Configuration** - Use connection pool management, configure max connections
3. **Security Configuration** - Use AES-192-GCM or AES-256-GCM encryption
4. **Cache Configuration** - Recommended to use Redis in production
5. **Path Configuration** - Use absolute paths or relative paths with `@` prefix
6. **Multi-language** - Use `DescriptionSet` nodes to define Chinese/English text
