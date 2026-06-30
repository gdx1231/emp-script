# EwaConfig.xml and define.xml/ewa Relationship Analysis

## Core Relationship Overview

**EwaConfig.xml** and **define.xml/ewa/*.xml** are two different levels of configuration files in the EWA framework:

| Feature | EwaConfig.xml | define.xml/ewa/*.xml |
|------|---------------|---------------------|
| **Level** | System-level meta configuration | Feature-level template configuration |
| **Role** | Defines framework types and page elements | Concrete business template instances |
| **Root Node** | `<EasyWebConfig>` | `<EasyWebTemplates>` |
| **Purpose** | Component definitions | Template instances |

---

## 1. EwaConfig.xml - System Meta Configuration

### 1.1 Core Functionality

`EwaConfig.xml` defines the **base components** of the EWA system:

```
EwaConfig.xml
├── <Frames>           # Defines 10 form types
│   ├── Frame          # Input / edit interface
│   ├── ListFrame      # Table list
│   ├── Grid           # Grid display
│   ├── Tree           # Tree structure
│   ├── Menu           # Menu
│   ├── MultiGrid      # Multi-dimensional table
│   ├── Logic          # Logic control
│   ├── Report         # Report
│   ├── Combine        # Combined page
│   └── Complex        # Composite page
│
└── <Items>/<XItems>   # Defines page elements (40+ types)
    ├── text           # Single-line text
    ├── textarea       # Multi-line text
    ├── password       # Password
    ├── date/datetime  # Date / time
    ├── select         # Dropdown select
    ├── checkbox/radio # Checkbox / radio
    ├── button/submit  # Button
    ├── anchor         # Link
    ├── dHtml5         # HTML5 editor
    └── ...
```

### 1.2 Frame Definition Example

```xml
<Frame Name="Frame" 
       FrameClassName="com.gdxsoft.easyweb.script.display.frame.FrameFrame"
       ActionClassName="com.gdxsoft.easyweb.script.display.action.ActionFrame">
    <DescriptionSet>
        <Set Lang="zhcn" Info="输入修改界面" />
        <Set Lang="enus" Info="Frame" />
    </DescriptionSet>
</Frame>
```

**Explanation**: Defines the Java implementation class and multilingual description for the `Frame` type.

### 1.3 XItem Definition Example

```xml
<XItem Name="text" HtmlTag="input$text" BaseType="1" UIGroup="Standard"
       ClassName="com.gdxsoft.easyweb.script.display.items.ItemBase"
       Parameters="GroupIndex,Name,DescriptionSet,AttributeSet,...">
    <DescriptionSet>
        <Set Lang="zhcn" Info="单行文本" />
        <Set Lang="enus" Info="Textbox" />
    </DescriptionSet>
    <Template>
        <Html><![CDATA[<input type="text" class="EWA_INPUT" value="{__EWA_VAL__}" !!/>]]></Html>
    </Template>
</XItem>
```

**Explanation**: Defines the HTML template, Java class, and available parameters for the `text` element.

---

## 2. define.xml/ewa/*.xml - Feature Templates

### 2.1 File List and Purpose

| File | Purpose | Template Count |
|------|------|---------|
| `ewa.xml` | System definition management templates | ~50 |
| `m.xml` | System administration feature templates | ~20 |
| `database.xml` | Database-related templates | ~30 |
| `ewa_restful.xml` | RESTful API configuration templates | ~10 |
| `ewa_conf_help.xml` | Configuration help templates | ~10 |
| `ewa_module.xml` | Module management templates | ~15 |
| `ewa_wf.xml` | Workflow templates | ~10 |
| `app_cfg.xml` | APP configuration templates | ~5 |
| `data_hor.xml` | Data horizontal comparison templates | ~5 |
| `dev.xml` | Development tool templates | ~10 |
| `doc.xml` | Document management templates | ~5 |
| `ewa_admin.xml` | Administrator management templates | ~5 |
| `group1.xml` | Group configuration templates | ~5 |
| `m1.xml` | Extended management templates | ~10 |
| `tmp_img_resze.xml` | Image processing templates | ~3 |

### 2.2 EasyWebTemplate Structure

Each template file contains multiple `<EasyWebTemplate>` nodes:

```xml
<EasyWebTemplates>
    <EasyWebTemplate Name="frames_fields_list" CreateDate="2011-05-05">
        <Page>
            <FrameTag><Set FrameTag="ListFrame"/></FrameTag>
            <Name><Set Name="frames_fields_list"/></Name>
            <SkinName><Set SkinName="Test1"/></SkinName>
            <DescriptionSet>
                <Set Info="Configuration item list" Lang="zhcn"/>
            </DescriptionSet>
            <Size><Set Width="700"/></Size>
            <AddHtml>...</AddHtml>
            <AddScript>...</AddScript>
        </Page>
        <Action>
            <ActionSet>...</ActionSet>
            <SqlSet>...</SqlSet>
        </Action>
        <XItems>
            <XItem Name="inc">
                <Tag><Set Tag="span"/></Tag>
                <DescriptionSet><Set Info="Sequence number"/></DescriptionSet>
            </XItem>
            <!-- More fields -->
        </XItems>
    </EasyWebTemplate>
    
    <EasyWebTemplate Name="ChangePWD">
        <!-- Another template -->
    </EasyWebTemplate>
</EasyWebTemplates>
```

---

## 3. Relationship Between the Two Configuration Layers

### 3.1 Reference Relationship Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                   EwaConfig.xml (Meta Config)                │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ <Frames>                                               │  │
│  │   - Frame (ClassName: FrameFrame)                     │  │
│  │   - ListFrame (ClassName: FrameList)                  │  │
│  │   - Tree (ClassName: FrameTree)                       │  │
│  │   - ...                                                │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ <XItems>                                               │  │
│  │   - text (HtmlTag: input$text)                        │  │
│  │   - select (HtmlTag: select)                          │  │
│  │   - button (HtmlTag: input$button)                    │  │
│  │   - ...                                                │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ Instantiate / Use
                            ▼
┌─────────────────────────────────────────────────────────────┐
│             define.xml/ewa/*.xml (Template Instances)        │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ <EasyWebTemplate Name="ChangePWD">                    │  │
│  │   <Page>                                               │  │
│  │     <FrameTag><Set FrameTag="Frame"/></FrameTag>  ◄────┼── Uses meta config
│  │   </Page>                                              │  │
│  │   <XItems>                                             │  │
│  │     <XItem Name="ADM_LID">                            │  │
│  │       <Tag><Set Tag="span"/></Tag>              ◄─────┼── Uses meta config
│  │     </XItem>                                           │  │
│  │     <XItem Name="ADM_PWD">                            │  │
│  │       <Tag><Set Tag="password"/></Tag>          ◄─────┼── Uses meta config
│  │     </XItem>                                           │  │
│  │   </XItems>                                            │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 Concrete Usage Examples

#### Example 1: Frame Type Reference

**EwaConfig.xml Definition**:
```xml
<Frame Name="ListFrame" 
       FrameClassName="com.gdxsoft.easyweb.script.display.frame.FrameList"/>
```

**ewa.xml Usage**:
```xml
<EasyWebTemplate Name="frames_fields_list">
    <Page>
        <FrameTag><Set FrameTag="ListFrame"/></FrameTag>
        <!-- Uses ListFrame type -->
    </Page>
</EasyWebTemplate>
```

#### Example 2: XItem Type Reference

**EwaConfig.xml Definition**:
```xml
<XItem Name="password" HtmlTag="input$password" BaseType="1"
       ClassName="com.gdxsoft.easyweb.script.display.items.ItemBase">
    <Template>
        <Html><![CDATA[<input type="password" class="EWA_INPUT" />]]></Html>
    </Template>
</XItem>
```

**m.xml Usage**:
```xml
<EasyWebTemplate Name="ChangePWD">
    <XItems>
        <XItem Name="ADM_PWD">
            <Tag><Set Tag="password"/></Tag>
            <!-- Renders as a password input field -->
        </XItem>
    </XItems>
</EasyWebTemplate>
```

---

## 4. Configuration Inheritance and Extension

### 4.1 Template Inheritance Mechanism

Some templates reference configurations from other templates:

```xml
<!-- Template in m.xml -->
<EasyWebTemplate Name="admAdd">
    <Page>
        <FrameTag><Set FrameTag="Frame"/></FrameTag>
    </Page>
    <!-- Full Page/Action/XItems definition -->
</EasyWebTemplate>

<!-- Another template in m.xml referencing the above -->
<EasyWebTemplate Name="admlist">
    <Page>
        <FrameTag><Set FrameTag="ListFrame"/></FrameTag>
    </Page>
    <!-- Can reference admAdd's configuration -->
</EasyWebTemplate>
```

### 4.2 Tmp Template Definitions

Shortcut templates are defined in `EwaDefine.xml`:

```xml
<Frame Name="Frame">
    <Tmp Name="NM">
        <DescriptionSet><Set Info="New and modify data"/></DescriptionSet>
        <Button Name="butOk" Tag="submit"/>
        <Button Name="butClose" Tag="button"/>
        <Action Name="OnNew" SqlType="update"/>
        <Action Name="OnModify" SqlType="update"/>
    </Tmp>
    <Tmp Name="V">
        <DescriptionSet><Set Info="View page"/></DescriptionSet>
        <Button Name="butClose" Tag="button"/>
    </Tmp>
</Frame>
```

**Usage Patterns**:
- `users.F.NM` - Uses Frame's NM template
- `users.F.V` - Uses Frame's V template
- `orders.LF.M` - Uses ListFrame's M template

---

## 5. Runtime Processing Flow

### 5.1 Configuration Loading Order

```
1. System startup
   │
   ├─► 2. Load EwaConfig.xml
   │    └─► Register Frames and XItems definitions
   │
   ├─► 3. Load define.xml/ewa/*.xml
   │    └─► Register EasyWebTemplate templates
   │
   └─► 4. Load user-defined configurations
        └─► Load business configs from database or files
```

### 5.2 Page Rendering Flow

```
User request: /ewa?XMLNAME=users&ITEMNAME=F.NM
   │
   ▼
1. Parse configuration path
   - XMLNAME = users (config file)
   - ITEMNAME = F.NM (Frame's NM template)
   │
   ▼
2. Look up template
   - Get Frame definition from EwaConfig.xml
   - Get NM template structure from EwaDefine.xml
   │
   ▼
3. Render page
   - Select FrameFrame class based on FrameTag
   - Select corresponding HTML template based on XItem Tag
   - Populate data
   │
   ▼
4. Output HTML
```

---

## 6. Configuration Naming Conventions

### 6.1 Configuration Item Naming Rules

```
{table_name}.{Frame type}.{Template type}

Examples:
- users.F.NM        # Users table - Form - New/Modify
- orders.LF.M       # Orders table - List - Modifiable
- products.T.V      # Products table - Tree - View
```

### 6.2 Frame Type Abbreviations

| Abbreviation | Full Name | Corresponding Name in EwaConfig |
|------|------|-------------------------|
| F | Frame | `Frame` |
| LF | ListFrame | `ListFrame` |
| T | Tree | `Tree` |
| M | Menu | `Menu` |
| G | Grid | `Grid` |
| MG | MultiGrid | `MultiGrid` |

### 6.3 Template Type Abbreviations

| Abbreviation | Meaning | Purpose |
|------|------|------|
| N | New | Create new |
| M | Modify | Modify existing |
| V | View | View only |
| NM | New/Modify | New / modify shared |
| LF.M | ListFrame Modify | List with modification |

---

## 7. Configuration File Comparison Summary

| Comparison | EwaConfig.xml | define.xml/ewa/*.xml |
|--------|---------------|---------------------|
| **Config Level** | Meta Config | Instance Config |
| **Root Node** | `<EasyWebConfig>` | `<EasyWebTemplates>` |
| **Defined Content** | Frames, XItems | EasyWebTemplate |
| **Java Class Mapping** | Yes (ClassName) | No (uses meta config definitions) |
| **HTML Template** | Yes (Template) | Yes (full page structure) |
| **Multilingual Support** | Yes (DescriptionSet) | Yes (DescriptionSet) |
| **Inheritability** | No (base definitions) | Yes (can reference other templates) |
| **Runtime Modification** | No (requires restart) | Yes (hot-reloadable) |
| **Quantity** | ~10 Frames, ~40 XItems | ~200+ EasyWebTemplates |

---

## 8. Practical Development Advice

### 8.1 When to Modify EwaConfig.xml

- Adding new page element types
- Modifying existing element HTML rendering templates
- Adding new Frame types
- Changing existing Frame Java implementation classes

### 8.2 When to Modify define.xml/ewa/*.xml

- Adding new business templates
- Modifying existing template layouts
- Adding new management features
- Adjusting RESTful API configurations

### 8.3 When to Create User-Defined Configurations

- CRUD pages for business tables
- Forms for specific business processes
- Custom reports and statistics pages
