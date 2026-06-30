# EWA 参数详解

本文档详细说明 EWA 框架中所有系统参数的含义和用法。

## 参数分类

EWA 参数分为以下几类：

1. **核心参数** - XMLNAME, ITEMNAME 等基础参数
2. **AJAX 调用参数** - EWA_AJAX, EWA_ACTION 等
3. **Frame 显示参数** - 控制 Frame 渲染样式
4. **ListFrame 参数** - 列表专用参数
5. **Tree 参数** - 树形结构参数
6. **系统参数** - SYS_开头的内部参数
7. **移动端参数** - EWA_MOBILE, EWA_VUE 等
8. **调试参数** - Debug 相关参数

---

## 1. 核心参数

### XMLNAME
- **含义**: 配置文件名
- **格式**: `|ewa|ewa_main.xml` 或 `/ewa/ewa_main.xml`
- **说明**: `|` 表示目录分割符，`..` 不被支持
- **示例**: 
  ```
  XMLNAME=|ewa|users.xml
  XMLNAME=/ewa/users.xml
  ```

### ITEMNAME
- **含义**: 配置项名称
- **格式**: `Frame 类型。模板类型`
- **示例**:
  ```
  ITEMNAME=F.NM      # Frame 的 NM 模板（新增/修改）
  ITEMNAME=LF.M      # ListFrame 的 M 模板（可修改列表）
  ITEMNAME=T.V       # Tree 的 V 模板（查看）
  ```

### EWA_LANG
- **含义**: 指定语言
- **可选值**: `zhcn` (简体中文), `enus` (英语)
- **说明**: 参数指定后保留在 session 中，下次从 session 获取
- **示例**:
  ```
  EWA_LANG=enus
  ```

### EWA_SN
- **含义**: ShortName 短名称
- **说明**: 使用预定义的短名称代替 XMLNAME+ITEMNAME
- **示例**:
  ```
  EWA_SN=user_list
  ```

---

## 2. AJAX 调用参数

### EWA_AJAX
- **含义**: AJAX 调用类型
- **可选值**:

| 值 | 说明 | 返回格式 |
|----|------|---------|
| `JSON` | JSON 格式数据 | `{"DATA":[...]}` |
| `JSON_EXT` | JSON 扩展格式（含分页信息） | `{"DATA":[...], "PAGE":{...}}` |
| `JSON_EXT1` | JSON 扩展格式（含配置脚本） | `{"DATA":[...], "CFG":{...}}` |
| `JSON_ALL` | 输出所有 Action 的查询数据 | 多个 JSON 数组 |
| `JSON_OBJECTS` | 返回 ewa_table_name 的 JSON 对象 | `{ewa_table_name: [...]}` |
| `XML` | XML 字符串 | `<?xml...>` |
| `XMLDATA` | XML 数据内容 | `<DATA>...</DATA>` |
| `HAVE_DATA` | 显示是否有数据 | `true/false` |
| `DOWN_DATA` | 下载数据 | 文件流 |
| `DOWNLOAD` | 下载文件 | 文件流 |
| `DOWNLOAD-INLINE` | 在线预览（图片/PDF） | 文件流 (inline) |
| `VALIDCODE` | 验证码图片 | image/jpeg |
| `ValidSlidePuzzle` | 滑动拼图验证 | JSON |
| `LF_RELOAD` | ListFrame 重新加载 | HTML |
| `SELECT_RELOAD` | Select 控件重新加载 | JSON |
| `WORKFLOW` | 工作流处理 | JSON |
| `INSTALL` | 安装模式 | HTML |
| `TOP_CNT_BOTTOM` | 仅主体内容（无头尾） | HTML |

- **示例**:
  ```
  # 获取 JSON 数据
  /ewa?XMLNAME=users&ITEMNAME=LF.M&EWA_AJAX=JSON
  
  # 下载文件
  /ewa?XMLNAME=files&ITEMNAME=F.V&EWA_AJAX=DOWNLOAD&EWA_DOWNLOAD_NAME=file_path
  
  # 在线预览图片
  /ewa?XMLNAME=images&ITEMNAME=F.V&EWA_AJAX=DOWNLOAD-INLINE
  ```

### EWA_ACTION
- **含义**: 指定调用的 Action 名称
- **默认值**: `OnPageLoad` (GET), `OnPagePost` (POST)
- **示例**:
  ```
  # 调用自定义 Action
  EWA_ACTION=OnFrameDelete
  EWA_ACTION=OnExportExcel
  ```

### EWA_MTYPE
- **含义**: Frame 数据处理方式
- **可选值**:
  - `N` - New (新增)
  - `M` - Modify (修改)
  - `C` - Copy (复制)
- **示例**:
  ```
  # 新增用户
  /ewa?XMLNAME=users&ITEMNAME=F.NM&EWA_MTYPE=N
  
  # 修改用户
  /ewa?XMLNAME=users&ITEMNAME=F.NM&EWA_MTYPE=M
  ```

### EWA_JSON_FIELD_CASE
- **含义**: JSON 字段名称大小写转换
- **可选值**:
  - `lower` - 所有字段转为小写
  - `upper` - 所有字段转为大写
- **示例**:
  ```
  EWA_AJAX=JSON&EWA_JSON_FIELD_CASE=lower
  ```

### EWA_JSON_SKIP_NULL
- **含义**: JSON 输出时忽略 null 值
- **说明**: 非空即生效，不输出 `addr: null` 这样的字段
- **示例**:
  ```
  EWA_JSON_SKIP_NULL=1
  ```

### EWA_JSON_BIN_METHOD
- **含义**: JSON 输出时二进制字段处理方式
- **可选值**: `HEX`, `BASE64`, `IMAGE`(默认)
- **示例**:
  ```
  EWA_JSON_BIN_METHOD=BASE64
  ```

### EWA_JSON_NAME
- **含义**: JSONP 回调函数名称
- **示例**:
  ```
  EWA_AJAX=JSON&EWA_JSON_NAME=callback123
  ```

---

## 3. Frame 显示参数

### EWA_FRAME_COLS
- **含义**: Frame 分段显示方式
- **可选值**:
  - `C2` 或 `2` - 2 段（无备注框）
  - `C1` 或 `1` - 1 段（无标题框）
  - `C11` - 标题和输入框上下排列
- **示例**:
  ```
  EWA_FRAME_COLS=C2
  ```

### EWA_WIDTH
- **含义**: 指定 Frame 宽度
- **示例**:
  ```
  EWA_WIDTH=800px
  EWA_WIDTH=100%
  ```

### EWA_HEIGHT
- **含义**: 指定 Frame 高度
- **示例**:
  ```
  EWA_HEIGHT=600px
  ```

### EWA_IS_HIDDEN_CAPTION
- **含义**: 是否显示标题栏
- **可选值**: `yes/no`, `1/0`
- **说明**: 对于 ListFrame 是第一行字段描述，对于 Frame 是第一行标题
- **示例**:
  ```
  EWA_IS_HIDDEN_CAPTION=yes
  ```

### EWA_TEMP_NO
- **含义**: 不使用自定义框架模板
- **说明**: 非空即生效
- **示例**:
  ```
  EWA_TEMP_NO=1
  ```

### EWA_LF_TEMP_NO
- **含义**: ListFrame 不使用模板
- **说明**: 非空即生效
- **示例**:
  ```
  EWA_LF_TEMP_NO=1
  ```

### EWA_FRAME_BOX_NO
- **含义**: ListFrame 输出时不生成 table/tr/td
- **说明**: 仅输出 item 的 html
- **示例**:
  ```
  EWA_FRAME_BOX_NO=1
  ```

### EWA_REDRAW
- **含义**: Frame 按重绘模式显示
- **说明**: 非空即生效
- **示例**:
  ```
  EWA_REDRAW=1
  ```

### EWA_CELL_ADD_DES
- **含义**: 在 Frame 的 TD 附加标题
- **说明**: 非空时，在每个 td 上添加属性 `ewa_cell_des`
- **CSS**: `.ewa-col-name::before {content: attr(ewa_cell_des);}`
- **示例**:
  ```
  EWA_CELL_ADD_DES=1
  ```

### EWA_CELL_ADD_DES_NAME_MEMO
- **含义**: 在 Frame 的 TD 附加备注
- **说明**: 非空时，在每个 td 上添加属性 `ewa_cell_memo`
- **示例**:
  ```
  EWA_CELL_ADD_DES_NAME_MEMO=1
  ```

### EWA_TITLE
- **含义**: 通过参数传递的标题（中文）
- **说明**: 覆盖 Frame 的 title
- **示例**:
  ```
  EWA_TITLE=用户管理
  ```

### EWA_TITLE_EN
- **含义**: 通过参数传递的标题（英文）
- **说明**: 当 `ewa_lang=enus` 时使用
- **示例**:
  ```
  EWA_TITLE_EN=User Management
  ```

### EWA_SKIP_TEST1
- **含义**: 不使用 Test1 的 table
- **说明**: 非空即生效
- **示例**:
  ```
  EWA_SKIP_TEST1=1
  ```

### EWA_IN_DIALOG
- **含义**: 以 dialog 模式显示
- **说明**: 限定高度宽度滚动条，添加 class `ewa-in-dialog`
- **示例**:
  ```
  EWA_IN_DIALOG=1
  ```

### EWA_HIDDEN_FIELDS
- **含义**: 隐藏 Frame 字段的表达式
- **格式**: 用 `,` 分割字段名
- **示例**:
  ```
  EWA_HIDDEN_FIELDS=password,secret_key
  ```

### EWA_FRAME_UNID_PREFIX
- **含义**: 更改 SysFrameUnid 前缀
- **说明**: 只保留英文、数字、_、中文
- **示例**:
  ```
  EWA_FRAME_UNID_PREFIX=user_form
  ```

---

## 4. ListFrame 参数

### EWA_PAGECUR
- **含义**: 列表当前页编号
- **示例**:
  ```
  EWA_PAGECUR=2
  ```

### EWA_PAGESIZE
- **含义**: 列表分页记录数
- **示例**:
  ```
  EWA_PAGESIZE=50
  ```

### EWA_IS_SPLIT_PAGE
- **含义**: 用户指定是否分页
- **可选值**: `yes/no`
- **说明**: 超越 PageSize.IsSplitPage 定义
- **示例**:
  ```
  EWA_IS_SPLIT_PAGE=no
  ```

### EWA_LF_ORDER
- **含义**: 排序方式
- **格式**: `字段名 ASC/DESC`
- **示例**:
  ```
  EWA_LF_ORDER=create_date DESC
  ```

### EWA_SEARCH
- **含义**: 初始化列表搜索框
- **语法**: `字段 [方式] 检索词`
- **方式**:
  - `lk` - 包含：`字段 like '%检索词%'`
  - `llk` - 左包含：`字段 like '检索词%'`
  - `rlk` - 右包含：`字段 like '%检索词'`
  - `eq` - 等于：`字段='检索词'`
  - `or` - 或：多个词用分号分割 `字段='词 1' OR 字段='词 2'`
- **示例**:
  ```
  # 包含搜索
  EWA_SEARCH=nws_subject[lk]base
  
  # 多条件搜索
  EWA_SEARCH=nws_subject[lk]base,NWS_CAT_NAME[eq]documents
  
  # 或表达式
  EWA_SEARCH=MEMO_STATE[or]MEMO_ING;MEMO_FINISH
  ```

### EWA_LF_SEARCH
- **含义**: ListFrame 检索（客户端操作产生）
- **说明**: 一般不用于 URL 手动调用

### EWA_LU_STICKY_HEADERS
- **含义**: 列表是否固定表头
- **可选值**: `yes/no`
- **示例**:
  ```
  EWA_LU_STICKY_HEADERS=yes
  ```

### EWA_LU_BUTTONS
- **含义**: 列表不重绘按钮
- **可选值**: `NO`
- **示例**:
  ```
  EWA_LU_BUTTONS=NO
  ```

### EWA_LU_SEARCH
- **含义**: 列表不重绘搜索
- **可选值**: `NO`
- **示例**:
  ```
  EWA_LU_SEARCH=NO
  ```

### EWA_LU_SELECT
- **含义**: 列表选择模式
- **可选值**:
  - `S` - 单选
  - `M` - 多选
- **示例**:
  ```
  EWA_LU_SELECT=S
  ```

### EWA_LU_DBLCLICK / EWA_LU_DBL_CLICK
- **含义**: 列表行双击
- **可选值**: `yes/no`
- **示例**:
  ```
  EWA_LU_DBLCLICK=yes
  ```

### EWA_BOX
- **含义**: ListFrame 显示为 BOX 对象
- **说明**: 非空即生效，需要在 IDE 中设定 BOX 参数
- **示例**:
  ```
  EWA_BOX=1
  ```

### EWA_BOX_PARENT_ID
- **含义**: 覆盖 BOX 参数的 parent_id
- **说明**: EWA_BOX=1 时有效
- **示例**:
  ```
  EWA_BOX=1&EWA_BOX_PARENT_ID=123
  ```

### EWA_RECYCLE
- **含义**: 列表显示回收站
- **可选值**: `NO` (不显示)
- **示例**:
  ```
  EWA_RECYCLE=NO
  ```

### EWA_AJAX_DOWN_TYPE
- **含义**: ListFrame 导出格式
- **可选值**: `XLS`, `DBF`, `TXT`, `XML`
- **示例**:
  ```
  EWA_AJAX=DOWN_DATA&EWA_AJAX_DOWN_TYPE=XLS
  ```

### EWA_SQL_SPLIT_NO
- **含义**: ListFrame 分页查询时跳过 select 语句
- **示例**:
  ```
  EWA_SQL_SPLIT_NO=1
  ```

### EWA_IS_SELECT
- **含义**: 强制制定为 SELECT 查询
- **示例**:
  ```
  EWA_IS_SELECT=1
  ```

### EWA_ROW_SIGN
- **含义**: 列表每行所有 TD 字符串进行 MD5 签名
- **可选值**: `yes/1`
- **说明**: 用于刷新数据 `refreshPage` 或 `replaceRowsData` 的比对
- **示例**:
  ```
  EWA_ROW_SIGN=1
  ```

### EWA_GRID_AS
- **含义**: 列表内容标签
- **默认值**: `li`
- **可选值**:
  - `a` - `<a>` 标签
  - `div` - `<div>` 标签
  - `div2` - `<div></div>` 双 div
- **示例**:
  ```
  EWA_GRID_AS=div
  ```

### EWA_GRID_TRANS
- **含义**: 多维表格转置
- **可选值**: `1` (转置)
- **示例**:
  ```
  EWA_GRID_TRANS=1
  ```

---

## 5. Tree 参数

### EWA_TREE_MORE
- **含义**: Tree 加载分层数据
- **可选值**: `1`
- **示例**:
  ```
  EWA_TREE_MORE=1
  ```

### EWA_TREE_STATUS
- **含义**: 获取 Tree 当前状态
- **可选值**: `1`
- **示例**:
  ```
  EWA_TREE_STATUS=1
  ```

### EWA_TREE_SKIP_GET_STATUS
- **含义**: 不获取 Tree 当前状态
- **说明**: 非空即生效
- **示例**:
  ```
  EWA_TREE_SKIP_GET_STATUS=1
  ```

### EWA_TREE_INIT_KEY
- **含义**: Tree 初始化显示的值
- **示例**:
  ```
  EWA_TREE_INIT_KEY=root_node
  ```

### EWA_TREE_ROOT_ID
- **含义**: 指定 Tree 的根节点 Id
- **示例**:
  ```
  EWA_TREE_ROOT_ID=1
  ```

### EWA_TREE_KEY
- **含义**: Tree 节点键名字段
- **示例**:
  ```
  EWA_TREE_KEY=id
  ```

### EWA_TREE_PARENT_KEY
- **含义**: Tree 父节点键名字段
- **示例**:
  ```
  EWA_TREE_PARENT_KEY=parent_id
  ```

### EWA_TREE_TEXT
- **含义**: Tree 节点文本字段
- **示例**:
  ```
  EWA_TREE_TEXT=name
  ```

---

## 6. 文件上传/下载参数

### EWA_DOWNLOAD_NAME
- **含义**: 下载文件对应的字段名称
- **示例**:
  ```
  EWA_AJAX=DOWNLOAD&EWA_DOWNLOAD_NAME=file_path
  ```

### EWA_IMAGE_RESIZE
- **含义**: 图片重新缩放
- **格式**: `宽 x 高`
- **示例**:
  ```
  EWA_IMAGE_RESIZE=800x600
  ```

### EWA_UP_NEWSIZES
- **含义**: 上传生成新尺寸
- **格式**: 多个尺寸用 `,` 分割
- **示例**:
  ```
  EWA_UP_NEWSIZES=100x100,200x200,400x400
  ```

### EWA_BIN_TYPE
- **含义**: 二进制数据存储方式
- **可选值**: `base64`, `16`(16 进制)
- **默认**: 存储为文件
- **示例**:
  ```
  EWA_BIN_TYPE=base64
  ```

---

## 7. 验证相关参数

### EWA_VALIDCODE_CHECK
- **含义**: 不检查验证码
- **说明**: 用于手机应用或 AJAX 调用
- **可选值**: `NOT_CHECK`
- **示例**:
  ```
  EWA_VALIDCODE_CHECK=NOT_CHECK
  ```

### EWA_SLIDE_PUZZLE_CHECK
- **含义**: 跳过图片滑动验证
- **可选值**: `NOT_CHECK`
- **示例**:
  ```
  EWA_SLIDE_PUZZLE_CHECK=NOT_CHECK
  ```

---

## 8. 移动端参数

### EWA_MOBILE
- **含义**: 移动模式
- **说明**: 非空即生效
- **示例**:
  ```
  EWA_MOBILE=1
  ```

### EWA_VUE
- **含义**: 输出为 Vue 格式
- **说明**: 非空即生效
- **示例**:
  ```
  EWA_VUE=1
  ```

### EWA_H5
- **含义**: 使用 H5 头
- **可选值**: `no` (不使用)
- **默认**: 使用 H5 头
- **示例**:
  ```
  EWA_H5=no
  ```

### EWA_XHTML
- **含义**: 使用 XHTML 头
- **可选值**: `yes`
- **示例**:
  ```
  EWA_XHTML=yes
  ```

### EWA_APP
- **含义**: App 调用模式
- **说明**: 非空即生效，ListFrame 删除在 tr 上的事件
- **示例**:
  ```
  EWA_APP=1
  ```

---

## 9. 调试参数

### EWA_DEBUG_NO
- **含义**: 不显示 debug 信息
- **说明**: 覆盖 ewa_conf.xml 的 debug 设置
- **示例**:
  ```
  EWA_DEBUG_NO=1
  ```

### EWA_DEBUG_KEY
- **含义**: Debug 密钥
- **说明**: 用于记录 Debug 信息到 HSQL
- **示例**:
  ```
  EWA_DEBUG_KEY=abc123
  ```

### EWA_JS_DEBUG
- **含义**: 脚本调试模式
- **说明**: 加载未压缩的 JS 源文件
- **示例**:
  ```
  EWA_JS_DEBUG=1
  ```

### EWA_DB_LOG
- **含义**: 写入 Debug 日志
- **示例**:
  ```
  EWA_DB_LOG=1
  ```

---

## 10. 工作流参数

### EWA_WF_CTRL
- **含义**: 工作流控制点
- **可选值**: `1`
- **示例**:
  ```
  EWA_WF_CTRL=1
  ```

### EWA_WF_NAME
- **含义**: 工作流名称
- **示例**:
  ```
  EWA_WF_NAME=approval_flow
  ```

### EWA_WF_TYPE
- **含义**: 工作流类型
- **可选值**:
  - `cnns` - 连续节点
  - `units` - 单位节点
  - `gunid` - 全局唯一 ID
  - `all` - 所有
  - `get` - 获取
  - `ins_post` - 用户提交 (POST)
  - `ins_get` - 用户提交 (GET)
- **示例**:
  ```
  EWA_WF_TYPE=cnns
  ```

### EWA_WF_UOK
- **含义**: 工作流用户确认
- **示例**:
  ```
  EWA_WF_UOK=1
  ```

---

## 11. 缓存参数

### EWA_IS_SPLIT_PAGE
- **含义**: 是否缓存页面
- **说明**: 已废弃，意义不大 (2025-12-21)

---

## 12. 其他参数

### EWA_POST
- **含义**: 是否是 POST 提交
- **可选值**: `1`

### EWA_P_BEHAVIOR
- **含义**: 提交后执行的脚本
- **说明**: 用于 AJAX 调用后再执行的脚本
- **示例**:
  ```
  EWA_P_BEHAVIOR=alert('操作成功');
  ```

### EWA_ACTION_TIP
- **含义**: 提交后执行的提示
- **示例**:
  ```
  EWA_ACTION_TIP=操作成功！
  ```

### EWA_ACTION_RELOAD
- **含义**: 执行后是否重新加载页面
- **可选值**: `0` (不加载)
- **示例**:
  ```
  EWA_ACTION_RELOAD=0
  ```

### EWA_AFTER_EVENT
- **含义**: 页面提交的要执行的事件
- **说明**: 避免 XSS 攻击，仅支持 `EWA.F.FOS["xxxx"].NewNodeAfter`
- **示例**:
  ```
  EWA_AFTER_EVENT=EWA.F.FOS["frame1"].NewNodeAfter
  ```

### EWA_NO_CONTENT
- **含义**: 不显示内容，仅执行
- **说明**: 非空即生效
- **示例**:
  ```
  EWA_NO_CONTENT=1
  ```

### EWA_FRAMESET_NO
- **含义**: 不显示 frame 框架
- **说明**: 在配置项中定义了 HtmlFrame 后首先显示框架，然后显示当前配置项
- **示例**:
  ```
  EWA_FRAMESET_NO=1
  ```

### EWA_TEMP_NO
- **含义**: 不使用自定义框架
- **示例**:
  ```
  EWA_TEMP_NO=1
  ```

### EWA_RELOAD_ID
- **含义**: 创建 select 对象的 reload 事件对应的 UserXItem 名称
- **示例**:
  ```
  EWA_RELOAD_ID=category_id
  ```

### EWA_LEFT
- **含义**: 列表左引导模式
- **示例**:
  ```
  EWA_LEFT=1
  ```

### EWA_INIT_GRP
- **含义**: 初始化分组
- **示例**:
  ```
  EWA_INIT_GRP=1
  ```

### EWA_KEY
- **含义**: 主键字段名
- **示例**:
  ```
  EWA_KEY=id
  ```

### EWA_ID
- **含义**: 记录 ID
- **示例**:
  ```
  EWA_ID=123
  ```

### EWA_R
- **含义**: 强制刷新数据
- **示例**:
  ```
  EWA_R=1
  ```

### EWA_TIMEDIFF
- **含义**: 时差（分钟）
- **示例**:
  ```
  EWA_TIMEDIFF=480
  ```

### EWA_COOKIE_DOMAIN
- **含义**: 更改 cookie 的域
- **说明**: 只能用于 HtmlControl 调用
- **示例**:
  ```
  EWA_COOKIE_DOMAIN=.example.com
  ```

### EWA_ADDED_RESOURCES
- **含义**: ewa_conf 中 addedResource 定义的附加资源 names
- **格式**: 用 `,` 分割
- **示例**:
  ```
  EWA_ADDED_RESOURCES=addjs0,addjs1,addcss1
  ```

### EWA_CALL_METHOD
- **含义**: EWA 调用模式
- **可选值**: `INNER_CALL` (ewaconfigitem 或 Jsp 程序调用)
- **示例**:
  ```
  EWA_CALL_METHOD=INNER_CALL
  ```

### INNER_CALL
- **含义**: 内部调用标志
- **说明**: ewaconfigitem 或 Jsp 程序调用内部调用

### EWA_SCRIPT_PATH
- **含义**: 脚本路径
- **示例**:
  ```
  EWA_SCRIPT_PATH=/path/to/scripts
  ```

### RV_EWA_STYLE_PATH
- **含义**: 样式路径（RequestValue 中）
- **示例**:
  ```
  RV_EWA_STYLE_PATH=/EmpScriptV2
  ```

### EWA_SKIN
- **含义**: 皮肤名称
- **示例**:
  ```
  EWA_SKIN=Test1
  ```

### EWA_SKIN_SESSION
- **含义**: 存在于 session 的皮肤名称
- **示例**:
  ```
  EWA_SKIN_SESSION=dark
  ```

### EWA_ACTION_KEY
- **含义**: Frame 执行操作的名称
- **示例**:
  ```
  EWA_ACTION_KEY=save
  ```

### EWA_FRAME_URL
- **含义**: Frame 的 URL
- **示例**:
  ```
  EWA_FRAME_URL=/ewa
  ```

### EWA_ERR_OUT
- **含义**: 在返回的表中判断 Action 执行的表中是否包含错误
- **示例**:
  ```
  EWA_ERR_OUT=1
  ```

### EWA_IS_HIDDEN_CAPTION
- **含义**: 是否显示标题栏
- **可选值**: `yes/no`, `1/0`
- **示例**:
  ```
  EWA_IS_HIDDEN_CAPTION=yes
  ```

---

## 13. 系统参数 (SYS_*)

### SYS_FRAME_UNID
- **含义**: Frame 的唯一编号
- **格式**: `xmlName + "&&&GDX1231&&&" + itemName` 的 hash 值
- **示例**:
  ```
  SYS_FRAME_UNID=12345G
  ```

### SYS_EWA_LANG
- **含义**: 从 session 取的 EWA_LANG
- **说明**: 内部使用

### SYS_USER_AGENT
- **含义**: 用户代理字符串
- **说明**: 用于判断客户端平台 (PC/手机/微信等)

### SYS_REMOTEIP
- **含义**: 用户 IP 地址
- **说明**: 从 RequestValue 获取

### SYS_REMOTE_URL
- **含义**: 用户请求 URL
- **说明**: 完整的请求地址

### SYS_REMOTE_REFERER
- **含义**: Referer URL
- **说明**: 来源页面地址

### SYS_CONTEXTPATH
- **含义**: Web 应用上下文路径
- **示例**:
  ```
  SYS_CONTEXTPATH=/myapp
  ```

### SYS_DATE
- **含义**: 系统当前日期时间
- **说明**: 用于 SQL 参数

### SYS_UNID
- **含义**: 全局唯一 ID
- **说明**: Twitter 雪花算法生成

---

## 参数优先级

参数优先级从高到低：

1. **URL 参数** - 最高优先级
2. **Session 参数** - 中等优先级 (如 EWA_LANG)
3. **配置文件默认值** - 最低优先级

---

## 参数组合示例

### 1. 列表查询（带分页和搜索）
```
/ewa?XMLNAME=users&ITEMNAME=LF.M
  &EWA_AJAX=JSON
  &EWA_PAGECUR=2
  &EWA_PAGESIZE=20
  &EWA_LF_ORDER=create_date DESC
  &EWA_SEARCH=user_name[lk]张
```

### 2. 新增记录
```
/ewa?XMLNAME=users&ITEMNAME=F.NM
  &EWA_MTYPE=N
  &EWA_ACTION=OnPagePost
  &EWA_AJAX=JSON
```

### 3. 删除记录
```
/ewa?XMLNAME=users&ITEMNAME=LF.M
  &EWA_ACTION=OnFrameDelete
  &EWA_AJAX=JSON
```

### 4. 下载 Excel
```
/ewa?XMLNAME=orders&ITEMNAME=LF.M
  &EWA_AJAX=DOWN_DATA
  &EWA_AJAX_DOWN_TYPE=XLS
```

### 5. 树形加载
```
/ewa?XMLNAME=categories&ITEMNAME=T.V
  &EWA_TREE_MORE=1
  &EWA_TREE_ROOT_ID=0
  &EWA_AJAX=JSON
```

### 6. 移动端调用
```
/ewa?XMLNAME=users&ITEMNAME=LF.M
  &EWA_MOBILE=1
  &EWA_AJAX=JSON
  &EWA_VALIDCODE_CHECK=NOT_CHECK
```

### 7. Vue 集成
```
/ewa?XMLNAME=users&ITEMNAME=LF.M
  &EWA_VUE=1
  &EWA_AJAX=JSON
  &EWA_JSON_FIELD_CASE=lower
```

---

## 注意事项

1. **参数大小写**:
   - 大部分参数大小写无关（如 `EWA_AJAX=json` 和 `EWA_AJAX=JSON` 等效）
   - 但 `XMLNAME` 和 `ITEMNAME` 的值大小写有关

2. **特殊字符编码**:
   - 参数值包含 `&`, `=`, `?` 等特殊字符时需要 URL 编码
   - 例如：`EWA_SEARCH=name[lk]张%26李`

3. **参数冲突**:
   - 某些参数互斥，如 `EWA_MOBILE` 和 `EWA_VUE` 不建议同时使用
   - `EWA_XHTML=yes` 和 `EWA_H5=no` 可以同时使用

4. **性能考虑**:
   - `EWA_JS_DEBUG=1` 会加载未压缩的 JS 文件，影响加载速度
   - `EWA_DEBUG_NO=1` 可以隐藏调试信息，提高安全性
