# EWA Parameters Reference

This document provides a detailed explanation of all system parameters in the EWA framework.

## Parameter Classification

EWA parameters are divided into the following categories:

1. **Core Parameters** - Basic parameters such as XMLNAME, ITEMNAME
2. **AJAX Call Parameters** - EWA_AJAX, EWA_ACTION, etc.
3. **Frame Display Parameters** - Controls Frame rendering styles
4. **ListFrame Parameters** - List-specific parameters
5. **Tree Parameters** - Tree structure parameters
6. **System Parameters** - Internal parameters prefixed with SYS_
7. **Mobile Parameters** - EWA_MOBILE, EWA_VUE, etc.
8. **Debug Parameters** - Debug-related parameters

---

## 1. Core Parameters

### XMLNAME
- **Meaning**: Configuration file name
- **Format**: `|ewa|ewa_main.xml` or `/ewa/ewa_main.xml`
- **Description**: `|` represents the directory separator, `..` is not supported
- **Example**: 
  ```
  XMLNAME=|ewa|users.xml
  XMLNAME=/ewa/users.xml
  ```

### ITEMNAME
- **Meaning**: Configuration item name
- **Format**: `FrameType. TemplateType`
- **Example**:
  ```
  ITEMNAME=F.NM      # Frame NM template (New/Modify)
  ITEMNAME=LF.M      # ListFrame M template (modifiable list)
  ITEMNAME=T.V       # Tree V template (View)
  ```

### EWA_LANG
- **Meaning**: Specify the language
- **Options**: `zhcn` (Simplified Chinese), `enus` (English)
- **Description**: The parameter is persisted in the session after being set and retrieved from the session next time
- **Example**:
  ```
  EWA_LANG=enus
  ```

### EWA_SN
- **Meaning**: ShortName
- **Description**: Use a predefined short name instead of XMLNAME+ITEMNAME
- **Example**:
  ```
  EWA_SN=user_list
  ```

---

## 2. AJAX Call Parameters

### EWA_AJAX
- **Meaning**: AJAX call type
- **Options**:

| Value | Description | Return Format |
|----|------|---------|
| `JSON` | JSON format data | `{"DATA":[...]}` |
| `JSON_EXT` | JSON extended format (with pagination info) | `{"DATA":[...], "PAGE":{...}}` |
| `JSON_EXT1` | JSON extended format (with config script) | `{"DATA":[...], "CFG":{...}}` |
| `JSON_ALL` | Output query data for all Actions | Multiple JSON arrays |
| `JSON_OBJECTS` | Return ewa_table_name as JSON object | `{ewa_table_name: [...]}` |
| `XML` | XML string | `<?xml...>` |
| `XMLDATA` | XML data content | `<DATA>...</DATA>` |
| `HAVE_DATA` | Show whether data exists | `true/false` |
| `DOWN_DATA` | Download data | File stream |
| `DOWNLOAD` | Download file | File stream |
| `DOWNLOAD-INLINE` | Inline preview (image/PDF) | File stream (inline) |
| `VALIDCODE` | CAPTCHA image | image/jpeg |
| `ValidSlidePuzzle` | Slide puzzle verification | JSON |
| `LF_RELOAD` | ListFrame reload | HTML |
| `SELECT_RELOAD` | Select control reload | JSON |
| `WORKFLOW` | Workflow processing | JSON |
| `INSTALL` | Install mode | HTML |
| `TOP_CNT_BOTTOM` | Body content only (no header/footer) | HTML |

- **Example**:
  ```
  # Get JSON data
  /ewa?XMLNAME=users&ITEMNAME=LF.M&EWA_AJAX=JSON
  
  # Download file
  /ewa?XMLNAME=files&ITEMNAME=F.V&EWA_AJAX=DOWNLOAD&EWA_DOWNLOAD_NAME=file_path
  
  # Inline image preview
  /ewa?XMLNAME=images&ITEMNAME=F.V&EWA_AJAX=DOWNLOAD-INLINE
  ```

### EWA_ACTION
- **Meaning**: Specifies the Action name to invoke
- **Default**: `OnPageLoad` (GET), `OnPagePost` (POST)
- **Example**:
  ```
  # Call custom Action
  EWA_ACTION=OnFrameDelete
  EWA_ACTION=OnExportExcel
  ```

### EWA_MTYPE
- **Meaning**: Frame data processing mode
- **Options**:
  - `N` - New
  - `M` - Modify
  - `C` - Copy
- **Example**:
  ```
  # Add user
  /ewa?XMLNAME=users&ITEMNAME=F.NM&EWA_MTYPE=N
  
  # Modify user
  /ewa?XMLNAME=users&ITEMNAME=F.NM&EWA_MTYPE=M
  ```

### EWA_JSON_FIELD_CASE
- **Meaning**: JSON field name case conversion
- **Options**:
  - `lower` - Convert all fields to lowercase
  - `upper` - Convert all fields to uppercase
- **Example**:
  ```
  EWA_AJAX=JSON&EWA_JSON_FIELD_CASE=lower
  ```

### EWA_JSON_SKIP_NULL
- **Meaning**: Ignore null values in JSON output
- **Description**: Takes effect when non-empty; skips fields like `addr: null`
- **Example**:
  ```
  EWA_JSON_SKIP_NULL=1
  ```

### EWA_JSON_BIN_METHOD
- **Meaning**: Binary field handling method in JSON output
- **Options**: `HEX`, `BASE64`, `IMAGE`(default)
- **Example**:
  ```
  EWA_JSON_BIN_METHOD=BASE64
  ```

### EWA_JSON_NAME
- **Meaning**: JSONP callback function name
- **Example**:
  ```
  EWA_AJAX=JSON&EWA_JSON_NAME=callback123
  ```

---

## 3. Frame Display Parameters

### EWA_FRAME_COLS
- **Meaning**: Frame segmented display mode
- **Options**:
  - `C2` or `2` - 2 segments (no notes box)
  - `C1` or `1` - 1 segment (no title box)
  - `C11` - Title and input box stacked vertically
- **Example**:
  ```
  EWA_FRAME_COLS=C2
  ```

### EWA_WIDTH
- **Meaning**: Specify Frame width
- **Example**:
  ```
  EWA_WIDTH=800px
  EWA_WIDTH=100%
  ```

### EWA_HEIGHT
- **Meaning**: Specify Frame height
- **Example**:
  ```
  EWA_HEIGHT=600px
  ```

### EWA_IS_HIDDEN_CAPTION
- **Meaning**: Whether to show the title bar
- **Options**: `yes/no`, `1/0`
- **Description**: For ListFrame, it's the first row's field description; for Frame, it's the first row title
- **Example**:
  ```
  EWA_IS_HIDDEN_CAPTION=yes
  ```

### EWA_TEMP_NO
- **Meaning**: Do not use the custom frame template
- **Description**: Takes effect when non-empty
- **Example**:
  ```
  EWA_TEMP_NO=1
  ```

### EWA_LF_TEMP_NO
- **Meaning**: ListFrame does not use template
- **Description**: Takes effect when non-empty
- **Example**:
  ```
  EWA_LF_TEMP_NO=1
  ```

### EWA_FRAME_BOX_NO
- **Meaning**: ListFrame output does not generate table/tr/td
- **Description**: Only outputs item html
- **Example**:
  ```
  EWA_FRAME_BOX_NO=1
  ```

### EWA_REDRAW
- **Meaning**: Frame displayed in redraw mode
- **Description**: Takes effect when non-empty
- **Example**:
  ```
  EWA_REDRAW=1
  ```

### EWA_CELL_ADD_DES
- **Meaning**: Append title to Frame TD
- **Description**: When non-empty, adds attribute `ewa_cell_des` to each td
- **CSS**: `.ewa-col-name::before {content: attr(ewa_cell_des);}`
- **Example**:
  ```
  EWA_CELL_ADD_DES=1
  ```

### EWA_CELL_ADD_DES_NAME_MEMO
- **Meaning**: Append memo to Frame TD
- **Description**: When non-empty, adds attribute `ewa_cell_memo` to each td
- **Example**:
  ```
  EWA_CELL_ADD_DES_NAME_MEMO=1
  ```

### EWA_TITLE
- **Meaning**: Title passed via parameter (Chinese)
- **Description**: Overrides the Frame title
- **Example**:
  ```
  EWA_TITLE=User Management
  ```

### EWA_TITLE_EN
- **Meaning**: Title passed via parameter (English)
- **Description**: Used when `ewa_lang=enus`
- **Example**:
  ```
  EWA_TITLE_EN=User Management
  ```

### EWA_SKIP_TEST1
- **Meaning**: Do not use Test1 table
- **Description**: Takes effect when non-empty
- **Example**:
  ```
  EWA_SKIP_TEST1=1
  ```

### EWA_IN_DIALOG
- **Meaning**: Display in dialog mode
- **Description**: Limits height, width, scrollbar; adds class `ewa-in-dialog`
- **Example**:
  ```
  EWA_IN_DIALOG=1
  ```

### EWA_HIDDEN_FIELDS
- **Meaning**: Expression to hide Frame fields
- **Format**: Field names separated by `,`
- **Example**:
  ```
  EWA_HIDDEN_FIELDS=password,secret_key
  ```

### EWA_FRAME_UNID_PREFIX
- **Meaning**: Change SysFrameUnid prefix
- **Description**: Only retains letters, digits, _, and Chinese characters
- **Example**:
  ```
  EWA_FRAME_UNID_PREFIX=user_form
  ```

---

## 4. ListFrame Parameters

### EWA_PAGECUR
- **Meaning**: Current page number of the list
- **Example**:
  ```
  EWA_PAGECUR=2
  ```

### EWA_PAGESIZE
- **Meaning**: Number of records per page in the list
- **Example**:
  ```
  EWA_PAGESIZE=50
  ```

### EWA_IS_SPLIT_PAGE
- **Meaning**: User-specified pagination toggle
- **Options**: `yes/no`
- **Description**: Overrides PageSize.IsSplitPage definition
- **Example**:
  ```
  EWA_IS_SPLIT_PAGE=no
  ```

### EWA_LF_ORDER
- **Meaning**: Sort order
- **Format**: `field_name ASC/DESC`
- **Example**:
  ```
  EWA_LF_ORDER=create_date DESC
  ```

### EWA_SEARCH
- **Meaning**: Initialize list search box
- **Syntax**: `field [method] search_term`
- **Methods**:
  - `lk` - Contains: `field like '%search_term%'`
  - `llk` - Left contains: `field like 'search_term%'`
  - `rlk` - Right contains: `field like '%search_term'`
  - `eq` - Equals: `field='search_term'`
  - `or` - OR: multiple terms separated by semicolons `field='term1' OR field='term2'`
- **Example**:
  ```
  # Contains search
  EWA_SEARCH=nws_subject[lk]base
  
  # Multi-condition search
  EWA_SEARCH=nws_subject[lk]base,NWS_CAT_NAME[eq]documents
  
  # OR expression
  EWA_SEARCH=MEMO_STATE[or]MEMO_ING;MEMO_FINISH
  ```

### EWA_LF_SEARCH
- **Meaning**: ListFrame search (generated by client-side interaction)
- **Description**: Generally not used for manual URL calls

### EWA_LU_STICKY_HEADERS
- **Meaning**: Whether to fix table headers in the list
- **Options**: `yes/no`
- **Example**:
  ```
  EWA_LU_STICKY_HEADERS=yes
  ```

### EWA_LU_BUTTONS
- **Meaning**: List does not redraw buttons
- **Options**: `NO`
- **Example**:
  ```
  EWA_LU_BUTTONS=NO
  ```

### EWA_LU_SEARCH
- **Meaning**: List does not redraw search
- **Options**: `NO`
- **Example**:
  ```
  EWA_LU_SEARCH=NO
  ```

### EWA_LU_SELECT
- **Meaning**: List selection mode
- **Options**:
  - `S` - Single selection
  - `M` - Multiple selection
- **Example**:
  ```
  EWA_LU_SELECT=S
  ```

### EWA_LU_DBLCLICK / EWA_LU_DBL_CLICK
- **Meaning**: List row double-click
- **Options**: `yes/no`
- **Example**:
  ```
  EWA_LU_DBLCLICK=yes
  ```

### EWA_BOX
- **Meaning**: Display ListFrame as a BOX object
- **Description**: Takes effect when non-empty; requires BOX parameters set in the IDE
- **Example**:
  ```
  EWA_BOX=1
  ```

### EWA_BOX_PARENT_ID
- **Meaning**: Override BOX parameter parent_id
- **Description**: Effective when EWA_BOX=1
- **Example**:
  ```
  EWA_BOX=1&EWA_BOX_PARENT_ID=123
  ```

### EWA_RECYCLE
- **Meaning**: List shows recycle bin
- **Options**: `NO` (do not show)
- **Example**:
  ```
  EWA_RECYCLE=NO
  ```

### EWA_AJAX_DOWN_TYPE
- **Meaning**: ListFrame export format
- **Options**: `XLS`, `DBF`, `TXT`, `XML`
- **Example**:
  ```
  EWA_AJAX=DOWN_DATA&EWA_AJAX_DOWN_TYPE=XLS
  ```

### EWA_SQL_SPLIT_NO
- **Meaning**: Skip the select statement during ListFrame paginated query
- **Example**:
  ```
  EWA_SQL_SPLIT_NO=1
  ```

### EWA_IS_SELECT
- **Meaning**: Force as a SELECT query
- **Example**:
  ```
  EWA_IS_SELECT=1
  ```

### EWA_ROW_SIGN
- **Meaning**: MD5 sign all TD strings in each row
- **Options**: `yes/1`
- **Description**: Used for `refreshPage` or `replaceRowsData` comparison
- **Example**:
  ```
  EWA_ROW_SIGN=1
  ```

### EWA_GRID_AS
- **Meaning**: List content tag
- **Default**: `li`
- **Options**:
  - `a` - `<a>` tag
  - `div` - `<div>` tag
  - `div2` - `<div></div>` double div
- **Example**:
  ```
  EWA_GRID_AS=div
  ```

### EWA_GRID_TRANS
- **Meaning**: Transpose multi-dimensional table
- **Options**: `1` (transpose)
- **Example**:
  ```
  EWA_GRID_TRANS=1
  ```

---

## 5. Tree Parameters

### EWA_TREE_MORE
- **Meaning**: Tree loads hierarchical data
- **Options**: `1`
- **Example**:
  ```
  EWA_TREE_MORE=1
  ```

### EWA_TREE_STATUS
- **Meaning**: Get current Tree state
- **Options**: `1`
- **Example**:
  ```
  EWA_TREE_STATUS=1
  ```

### EWA_TREE_SKIP_GET_STATUS
- **Meaning**: Do not get current Tree state
- **Description**: Takes effect when non-empty
- **Example**:
  ```
  EWA_TREE_SKIP_GET_STATUS=1
  ```

### EWA_TREE_INIT_KEY
- **Meaning**: Value for Tree initialization display
- **Example**:
  ```
  EWA_TREE_INIT_KEY=root_node
  ```

### EWA_TREE_ROOT_ID
- **Meaning**: Specify Tree root node ID
- **Example**:
  ```
  EWA_TREE_ROOT_ID=1
  ```

### EWA_TREE_KEY
- **Meaning**: Tree node key field name
- **Example**:
  ```
  EWA_TREE_KEY=id
  ```

### EWA_TREE_PARENT_KEY
- **Meaning**: Tree parent node key field name
- **Example**:
  ```
  EWA_TREE_PARENT_KEY=parent_id
  ```

### EWA_TREE_TEXT
- **Meaning**: Tree node text field
- **Example**:
  ```
  EWA_TREE_TEXT=name
  ```

---

## 6. File Upload/Download Parameters

### EWA_DOWNLOAD_NAME
- **Meaning**: Field name corresponding to the downloaded file
- **Example**:
  ```
  EWA_AJAX=DOWNLOAD&EWA_DOWNLOAD_NAME=file_path
  ```

### EWA_IMAGE_RESIZE
- **Meaning**: Image rescaling
- **Format**: `width x height`
- **Example**:
  ```
  EWA_IMAGE_RESIZE=800x600
  ```

### EWA_UP_NEWSIZES
- **Meaning**: Generate new sizes upon upload
- **Format**: Multiple sizes separated by `,`
- **Example**:
  ```
  EWA_UP_NEWSIZES=100x100,200x200,400x400
  ```

### EWA_BIN_TYPE
- **Meaning**: Binary data storage method
- **Options**: `base64`, `16`(hex)
- **Default**: Store as file
- **Example**:
  ```
  EWA_BIN_TYPE=base64
  ```

---

## 7. Verification-Related Parameters

### EWA_VALIDCODE_CHECK
- **Meaning**: Do not check CAPTCHA
- **Description**: Used for mobile apps or AJAX calls
- **Options**: `NOT_CHECK`
- **Example**:
  ```
  EWA_VALIDCODE_CHECK=NOT_CHECK
  ```

### EWA_SLIDE_PUZZLE_CHECK
- **Meaning**: Skip slide puzzle verification
- **Options**: `NOT_CHECK`
- **Example**:
  ```
  EWA_SLIDE_PUZZLE_CHECK=NOT_CHECK
  ```

---

## 8. Mobile Parameters

### EWA_MOBILE
- **Meaning**: Mobile mode
- **Description**: Takes effect when non-empty
- **Example**:
  ```
  EWA_MOBILE=1
  ```

### EWA_VUE
- **Meaning**: Output in Vue format
- **Description**: Takes effect when non-empty
- **Example**:
  ```
  EWA_VUE=1
  ```

### EWA_H5
- **Meaning**: Use H5 header
- **Options**: `no` (do not use)
- **Default**: Uses H5 header
- **Example**:
  ```
  EWA_H5=no
  ```

### EWA_XHTML
- **Meaning**: Use XHTML header
- **Options**: `yes`
- **Example**:
  ```
  EWA_XHTML=yes
  ```

### EWA_APP
- **Meaning**: App call mode
- **Description**: Takes effect when non-empty; ListFrame removes events on tr
- **Example**:
  ```
  EWA_APP=1
  ```

---

## 9. Debug Parameters

### EWA_DEBUG_NO
- **Meaning**: Do not show debug information
- **Description**: Overrides the debug setting in ewa_conf.xml
- **Example**:
  ```
  EWA_DEBUG_NO=1
  ```

### EWA_DEBUG_KEY
- **Meaning**: Debug key
- **Description**: Used to record debug information to HSQL
- **Example**:
  ```
  EWA_DEBUG_KEY=abc123
  ```

### EWA_JS_DEBUG
- **Meaning**: Script debug mode
- **Description**: Loads uncompressed JS source files
- **Example**:
  ```
  EWA_JS_DEBUG=1
  ```

### EWA_DB_LOG
- **Meaning**: Write debug logs
- **Example**:
  ```
  EWA_DB_LOG=1
  ```

---

## 10. Workflow Parameters

### EWA_WF_CTRL
- **Meaning**: Workflow control point
- **Options**: `1`
- **Example**:
  ```
  EWA_WF_CTRL=1
  ```

### EWA_WF_NAME
- **Meaning**: Workflow name
- **Example**:
  ```
  EWA_WF_NAME=approval_flow
  ```

### EWA_WF_TYPE
- **Meaning**: Workflow type
- **Options**:
  - `cnns` - Continuous nodes
  - `units` - Unit nodes
  - `gunid` - Globally unique ID
  - `all` - All
  - `get` - Get
  - `ins_post` - User submit (POST)
  - `ins_get` - User submit (GET)
- **Example**:
  ```
  EWA_WF_TYPE=cnns
  ```

### EWA_WF_UOK
- **Meaning**: Workflow user confirmation
- **Example**:
  ```
  EWA_WF_UOK=1
  ```

---

## 11. Cache Parameters

### EWA_IS_SPLIT_PAGE
- **Meaning**: Whether to cache the page
- **Description**: Deprecated, of little significance (2025-12-21)

---

## 12. Other Parameters

### EWA_POST
- **Meaning**: Whether it is a POST submission
- **Options**: `1`

### EWA_P_BEHAVIOR
- **Meaning**: Script to execute after submission
- **Description**: Used for scripts executed after AJAX calls
- **Example**:
  ```
  EWA_P_BEHAVIOR=alert('Operation successful');
  ```

### EWA_ACTION_TIP
- **Meaning**: Tip displayed after submission
- **Example**:
  ```
  EWA_ACTION_TIP=Operation successful!
  ```

### EWA_ACTION_RELOAD
- **Meaning**: Whether to reload the page after execution
- **Options**: `0` (do not reload)
- **Example**:
  ```
  EWA_ACTION_RELOAD=0
  ```

### EWA_AFTER_EVENT
- **Meaning**: Event to execute after page submission
- **Description**: To prevent XSS attacks, only supports `EWA.F.FOS["xxxx"].NewNodeAfter`
- **Example**:
  ```
  EWA_AFTER_EVENT=EWA.F.FOS["frame1"].NewNodeAfter
  ```

### EWA_NO_CONTENT
- **Meaning**: Do not display content, execute only
- **Description**: Takes effect when non-empty
- **Example**:
  ```
  EWA_NO_CONTENT=1
  ```

### EWA_FRAMESET_NO
- **Meaning**: Do not display frame frameset
- **Description**: When HtmlFrame is defined in the config item, the frameset is displayed first, then the current config item
- **Example**:
  ```
  EWA_FRAMESET_NO=1
  ```

### EWA_TEMP_NO
- **Meaning**: Do not use custom frame
- **Example**:
  ```
  EWA_TEMP_NO=1
  ```

### EWA_RELOAD_ID
- **Meaning**: The UserXItem name corresponding to the select object's reload event
- **Example**:
  ```
  EWA_RELOAD_ID=category_id
  ```

### EWA_LEFT
- **Meaning**: List left guide mode
- **Example**:
  ```
  EWA_LEFT=1
  ```

### EWA_INIT_GRP
- **Meaning**: Initialize grouping
- **Example**:
  ```
  EWA_INIT_GRP=1
  ```

### EWA_KEY
- **Meaning**: Primary key field name
- **Example**:
  ```
  EWA_KEY=id
  ```

### EWA_ID
- **Meaning**: Record ID
- **Example**:
  ```
  EWA_ID=123
  ```

### EWA_R
- **Meaning**: Force refresh data
- **Example**:
  ```
  EWA_R=1
  ```

### EWA_TIMEDIFF
- **Meaning**: Time difference (minutes)
- **Example**:
  ```
  EWA_TIMEDIFF=480
  ```

### EWA_COOKIE_DOMAIN
- **Meaning**: Change cookie domain
- **Description**: Can only be used in HtmlControl calls
- **Example**:
  ```
  EWA_COOKIE_DOMAIN=.example.com
  ```

### EWA_ADDED_RESOURCES
- **Meaning**: Names of additional resources defined in ewa_conf's addedResource
- **Format**: Separated by `,`
- **Example**:
  ```
  EWA_ADDED_RESOURCES=addjs0,addjs1,addcss1
  ```

### EWA_CALL_METHOD
- **Meaning**: EWA call mode
- **Options**: `INNER_CALL` (invoked by ewaconfigitem or Jsp program)
- **Example**:
  ```
  EWA_CALL_METHOD=INNER_CALL
  ```

### INNER_CALL
- **Meaning**: Internal call flag
- **Description**: Internal invocation by ewaconfigitem or Jsp program

### EWA_SCRIPT_PATH
- **Meaning**: Script path
- **Example**:
  ```
  EWA_SCRIPT_PATH=/path/to/scripts
  ```

### RV_EWA_STYLE_PATH
- **Meaning**: Style path (in RequestValue)
- **Example**:
  ```
  RV_EWA_STYLE_PATH=/EmpScriptV2
  ```

### EWA_SKIN
- **Meaning**: Skin name
- **Example**:
  ```
  EWA_SKIN=Test1
  ```

### EWA_SKIN_SESSION
- **Meaning**: Skin name stored in session
- **Example**:
  ```
  EWA_SKIN_SESSION=dark
  ```

### EWA_ACTION_KEY
- **Meaning**: Frame's operation name
- **Example**:
  ```
  EWA_ACTION_KEY=save
  ```

### EWA_FRAME_URL
- **Meaning**: Frame URL
- **Example**:
  ```
  EWA_FRAME_URL=/ewa
  ```

### EWA_ERR_OUT
- **Meaning**: Check whether the Action execution table contains errors in the returned table
- **Example**:
  ```
  EWA_ERR_OUT=1
  ```

### EWA_IS_HIDDEN_CAPTION
- **Meaning**: Whether to show the title bar
- **Options**: `yes/no`, `1/0`
- **Example**:
  ```
  EWA_IS_HIDDEN_CAPTION=yes
  ```

---

## 13. System Parameters (SYS_*)

### SYS_FRAME_UNID
- **Meaning**: Frame unique ID
- **Format**: Hash value of `xmlName + "&&&GDX1231&&&" + itemName`
- **Example**:
  ```
  SYS_FRAME_UNID=12345G
  ```

### SYS_EWA_LANG
- **Meaning**: EWA_LANG retrieved from session
- **Description**: Internal use

### SYS_USER_AGENT
- **Meaning**: User agent string
- **Description**: Used to determine client platform (PC/mobile/WeChat, etc.)

### SYS_REMOTEIP
- **Meaning**: User IP address
- **Description**: Retrieved from RequestValue

### SYS_REMOTE_URL
- **Meaning**: User request URL
- **Description**: Full request address

### SYS_REMOTE_REFERER
- **Meaning**: Referer URL
- **Description**: Source page address

### SYS_CONTEXTPATH
- **Meaning**: Web application context path
- **Example**:
  ```
  SYS_CONTEXTPATH=/myapp
  ```

### SYS_DATE
- **Meaning**: System current date and time
- **Description**: Used for SQL parameters

### SYS_UNID
- **Meaning**: Globally unique ID
- **Description**: Generated using Twitter Snowflake algorithm

---

## Parameter Priority

Parameter priority from highest to lowest:

1. **URL Parameters** - Highest priority
2. **Session Parameters** - Medium priority (e.g., EWA_LANG)
3. **Configuration File Defaults** - Lowest priority

---

## Parameter Combination Examples

### 1. List Query (with pagination and search)
```
/ewa?XMLNAME=users&ITEMNAME=LF.M
  &EWA_AJAX=JSON
  &EWA_PAGECUR=2
  &EWA_PAGESIZE=20
  &EWA_LF_ORDER=create_date DESC
  &EWA_SEARCH=user_name[lk]Smith
```

### 2. Add Record
```
/ewa?XMLNAME=users&ITEMNAME=F.NM
  &EWA_MTYPE=N
  &EWA_ACTION=OnPagePost
  &EWA_AJAX=JSON
```

### 3. Delete Record
```
/ewa?XMLNAME=users&ITEMNAME=LF.M
  &EWA_ACTION=OnFrameDelete
  &EWA_AJAX=JSON
```

### 4. Download Excel
```
/ewa?XMLNAME=orders&ITEMNAME=LF.M
  &EWA_AJAX=DOWN_DATA
  &EWA_AJAX_DOWN_TYPE=XLS
```

### 5. Tree Loading
```
/ewa?XMLNAME=categories&ITEMNAME=T.V
  &EWA_TREE_MORE=1
  &EWA_TREE_ROOT_ID=0
  &EWA_AJAX=JSON
```

### 6. Mobile Call
```
/ewa?XMLNAME=users&ITEMNAME=LF.M
  &EWA_MOBILE=1
  &EWA_AJAX=JSON
  &EWA_VALIDCODE_CHECK=NOT_CHECK
```

### 7. Vue Integration
```
/ewa?XMLNAME=users&ITEMNAME=LF.M
  &EWA_VUE=1
  &EWA_AJAX=JSON
  &EWA_JSON_FIELD_CASE=lower
```

---

## Notes

1. **Case Sensitivity**:
   - Most parameters are case-insensitive (e.g., `EWA_AJAX=json` and `EWA_AJAX=JSON` are equivalent)
   - But the values of `XMLNAME` and `ITEMNAME` are case-sensitive

2. **Special Character Encoding**:
   - Parameter values containing `&`, `=`, `?` and other special characters need URL encoding
   - Example: `EWA_SEARCH=name[lk]John%26Doe`

3. **Parameter Conflicts**:
   - Some parameters are mutually exclusive, such as `EWA_MOBILE` and `EWA_VUE` are not recommended to use together
   - `EWA_XHTML=yes` and `EWA_H5=no` can be used together

4. **Performance Considerations**:
   - `EWA_JS_DEBUG=1` will load uncompressed JS files, affecting load speed
   - `EWA_DEBUG_NO=1` can hide debug information to improve security
