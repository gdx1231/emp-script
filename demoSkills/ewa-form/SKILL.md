---
name: ewa-form
description: "Use when: 编写 EWA 表单前端 JS 代码、表单提交与验证、表单与列表联动、对话框中表单操作、动态字段控制、合并单元格、分组向导。按任务组织，含完整代码示例。"
trigger: ewa-form, EWA form, 表单提交, DoPostBefore, doPostAfter, setMust, CheckValid, DoAction, RecordNew, RecordModify, EWA.OW, 表单验证, 表单联动, 对话框表单, EWA_FrameClass
---

# EWA Form 操作指南

面向前端 JS 开发者的 EWA 表单操作参考。**按任务组织**，每个场景给完整可用的代码。

> `ewa` 变量 = `EWA.F.FOS['@SYS_FRAME_UNID']` — 当前帧实例。`@SYS_FRAME_UNID` 是服务端替换的占位符，运行时为实际帧 ID 字符串。

---

## 速查表

| 任务 | 方法 | 优先级 |
|------|------|--------|
| 🔥 表单提交前验证 | `ewa.DoPostBefore` | 高频 |
| 🔥 表单提交后回调 | `ewa.doPostAfter` | 高频 |
| 🔥 调用后端 Action | `ewa.DoAction()` | 高频 |
| 🔥 打开新建对话框 | `ewa.RecordNew()` | 高频 |
| 🔥 打开修改对话框 | `ewa.RecordModify()` | 高频 |
| 🔥 刷新父列表 | `EWA.OW.Frame.Reload()` | 高频 |
| 📋 动态设置必填 | `ewa.setMust()` / `ewa.setUnMust()` | 中频 |
| 📋 合并单元格 | `ewa.Merge()` / `ewa.MergeExp()` | 中频 |
| 📋 分组 Tab 切换 | `ewa.GroupShow()` | 中频 |
| 📋 下拉框刷新 | `ewa.itemReload()` | 中频 |
| 🔧 向导分步表单 | `ewa.GuideShowCreate()` | 低频 |
| 🔧 开关按钮回调 | `ewa.extSwitchCallBack` | 低频 |

---

## 1. 表单生命周期

### 完整提交链路

```
用户点击提交
  → ewa.DoPostBefore()     ← 返回 false 阻止提交
  → 内置验证 (CheckValidAll)
  → 触发验证 (滑块/验证码)
  → DoPostStart(ajax)      ← Ajax 发出前
  → Ajax POST
  → DoPostEnd(ajax, status, responseText)
  → ewa.doPostAfter(ret)   ← 返回 true 阻止默认 eval(ret)
  → EWA_PostBehavior 执行  ← RELOAD_PARENT / CLOSE_SELF 等
```

### DoPostBefore — 提交前验证

```javascript
(function(){
    var ewa = EWA.F.FOS['@SYS_FRAME_UNID'];

    ewa.DoPostBefore = function(){
        // 1. 防止重复提交
        if (ewa._submitting) {
            $Tip("提交中...");
            return false;
        }

        // 2. 自定义业务验证
        var money = getObj('#MONEY input').val();
        if (parseFloat(money) > 100000) {
            $Confirm("金额超过 10 万，确认提交？", "确认", function(){
                ewa._submitting = true;
                ewa.DoPost(); // 手动触发
            });
            return false; // 阻止默认提交，由确认后手动触发
        }

        // 3. 返回 true 继续提交（内置 CheckValidAll 会自动调用）
        return true;
    };
})();
```

**GOTCHA — 异步预检查模式：** `DoPostBefore` 可返回数组 `[result, tipMsg, useConfirm]`，框架会以 232ms 轮询直到 `result` 非 null：

```javascript
ewa.DoPostBefore = function(){
    if (!window._uploadDone) {
        return [null, "文件上传中...", false];  // 继续轮询
    }
    if (window._uploadError) {
        return [false, "上传失败", false];       // 阻止提交
    }
    return [true];                                // 允许提交
};
```

### DoPostStart — Ajax 发出前

```javascript
ewa.DoPostStart = function(ajax){
    // ajax 是 EWA_AjaxClass 实例
    // 可追加参数
    ajax.AddParameter("customFlag", "1");
};
```

### DoPostEnd — Ajax 响应返回后（行为执行前）

```javascript
ewa.DoPostEnd = function(ajax, status, responseText, statusText){
    if (status === 200) {
        $Tip("服务器响应成功");
    }
};
```

### doPostAfter — 提交后回调（替代已废弃的 ReloadAfter）

```javascript
ewa.doPostAfter = function(ret){
    // ret 是服务器返回的响应文本
    if (ret.indexOf("success") > -1) {
        $Tip("操作成功");
        // 关闭当前对话框
        if (EWA.OW && EWA.OW.Close) {
            EWA.OW.Close();
        }
        // 刷新父窗口列表
        if (EWA.OW && EWA.OW.Frame) {
            EWA.OW.Frame.Reload();
        }
        // 返回 true 阻止后续的 eval(ret)，避免页面重载
        return true;
    }
    return false; // 允许默认行为（eval 返回内容）
};
```

> **`doPostAfter` vs `ReloadAfter`**：`doPostAfter` 是当前推荐名称。`ReloadAfter` 是旧名称，框架仍兼容但不再推荐。

> **`ret` 参数内容**：后端 SQL 返回 `EWA_ERR_OUT` 时，框架生成 `EWA.UI.Msg.ShowError("错误信息", "标题");` 作为 `ret` 的内容。常见错误做法是不检查 `ret` 直接刷新父帧，导致报错后父列表仍然刷新。

```javascript
// 推荐做法：先检查 ret 是否有错误，再刷新父帧
ewa.doPostAfter = function(ret) {
    // ret 包含 "ShowError" 或 "EWA_ERR" 说明有错误
    if (ret && ret.indexOf && (ret.indexOf("ShowError") >= 0 || ret.indexOf("EWA_ERR") >= 0)) {
        return; // 有错误，不刷新父帧
    }
    if ("@from_pid" && EWA.F.FOS["@from_pid"]) {
        EWA.F.FOS["@from_pid"].Reload();
    }
};
```

---

## 2. 表单字段操作

### 取值与赋值

```javascript
// 通过 getObj 限定在当前帧
// 表单帧使用 #EWA_FRAME_@SYS_FRAME_UNID
// 列表帧使用 #EWA_LF_@SYS_FRAME_UNID
function getObj(exp){
    return exp ? $('#EWA_FRAME_@SYS_FRAME_UNID').find(exp) : $('#EWA_FRAME_@SYS_FRAME_UNID');
}

// 获取字段值
var name = getObj('#FIELD_NAME input').val();

// 赋值
getObj('#FIELD_NAME input').val("新值");

// 下拉框选中值
var selected = getObj('#FIELD_ID option:selected').val();
```

> **注意**：如果是在 **ListFrame** 中操作，选择器前缀应改为 `#EWA_LF_@SYS_FRAME_UNID`。`@SYS_FRAME_UNID` 是服务端替换的占位符，运行时为实际帧 ID 字符串。

### 动态设置必填 / 非必填

```javascript
// 设置字段必填（会在字段前加红色 *）
ewa.setMust("FIELD_NAME");   // 名称不区分大小写

// 取消必填
ewa.setUnMust("FIELD_NAME");
```

### 启用 / 禁用所有控件

```javascript
ewa.setDisable();  // 禁用所有 input/textarea/select，标记 ewadisabled="1"
ewa.setEnable();   // 仅恢复有 ewadisabled 标记的控件
```

### 刷新下拉框选项

```javascript
// 刷新指定 id 的下拉框，并选中新值
ewa.itemReload("DROP_LIST_ID", "default_value", function(){
    // 可选：刷新完成后的回调
});

// 带 Ajax URL 的动态刷新
ewa.itemReload("DROP_LIST_ID", "", "afterChangeEvent()");
```

### 创建 A-Z 字母筛选下拉

```javascript
// 将现有 checkboxes 转为带字母筛选的样式
ewa.convertFilterCheckbox(
    "checkbox_container_id",   // checkbox 容器 id
    "filter_container_id",     // 字母筛选显示位置
    "PY",                      // 按 JSON 的哪个字段筛选（默认 PY=拼音）
    true,                      // 是否合并单元格
    function(){ /* 转换完成回调 */ },
    function(){ /* 执行筛选回调 */ }
);

// 从 Ajax 数据创建 checkbox + 字母筛选
ewa.createFilterCheckbox(
    "checkbox_target_id",
    "filter_target_id",
    "val1,val2",   // 初始选中的值（逗号分隔）
    "/ewa?XMLNAME=xxx&ITEMNAME=yyy&EWA_AJAX=JSON",
    "id", "name", "py"
);
```

---

## 3. 表单 + 列表联动

### 打开新建记录对话框

```javascript
ewa.RecordNew("@xmlName", "ITEM.F.N", "extra_param1=v1&extra_param2=v2");
// 等价于：
// EWA.UI.Dialog.OpenReloadClose("@SYS_FRAME_UNID", "@xmlName", "ITEM.F.N", false, paras)
```

### 打开修改选中记录对话框

```javascript
// 自动获取当前选中行，打开修改对话框
ewa.RecordModify("@xmlName", "ITEM.F.M", "extra_param1=v1");
```

### 对话框内表单 — 关闭并刷新父列表

```javascript
// 在对话框内的帧中调用
EWA.OW.Load();            // 必须先调用，初始化 EWA.OW.Dia / PWin / Frame
EWA.OW.Close();           // 关闭对话框

// 或者：关闭前先刷新父窗口的某个帧
EWA.OW.Load();
EWA.OW.PWin.EWA.F.FOS['parent_frame_unid'].Reload();
EWA.OW.Close();
```

> **GOTCHA**：必须先在对话框内的 JS 中调用 `EWA.OW.Load()`，否则 `EWA.OW.Frame` 等为 null。

### 行为链（Behavior Chain）

提交后自动执行一系列操作，通过 URL 参数 `EWA_P_BEHAVIOR` 设置：

```
EWA_P_BEHAVIOR=RELOAD_PARENT,CLOSE_SELF
EWA_P_BEHAVIOR=RELOAD_PARENT,CLEAR_SELF
EWA_P_BEHAVIOR=CLOSE_SELF
```

```javascript
// 在 URL 中附加
var u = ewa.getUrlClass();
u.AddParameter("EWA_P_BEHAVIOR", "RELOAD_PARENT,CLOSE_SELF");
u.AddParameter("EWA_PARENT_FRAME", "@SYS_FRAME_UNID");
ewa.Reload(u.GetUrl());
```

---

## 4. 表单验证

### 内置验证类型

在 XML `<XItem>` 的 `<DataItem>` 中配置：

```xml
<DataItem><Set DataField="EMAIL" DataType="String" Valid="Email"/></DataItem>
<DataItem><Set DataField="AGE" DataType="Int" Valid="Number"/></DataItem>
<DataItem><Set DataField="CODE" DataType="String" Valid="required"/></DataItem>
<DataItem><Set DataField="PHONE" DataType="String" Valid="^1[3-9]\d{9}$"/></DataItem>
```

| Valid 值 | 含义 |
|----------|------|
| `required` | 必填 |
| `Email` | 邮箱格式 |
| `Number` | 数字 |
| 正则表达式 | 自定义正则（如 `^1[3-9]\d{9}$`） |

### 全部字段验证

```javascript
if (!ewa.CheckValidAll()) {
    $Tip("请检查必填字段");
    return;
}
```

### 单个字段验证

```javascript
var field = getObj('#FIELD_NAME input')[0];
if (!ewa.CheckValid(field)) {
    // 该字段验证失败
}
```

### 滑块验证（Captcha）

XML 中配置 `TriggerValid` 的字段会触发滑块验证：

```javascript
// 框架自动处理，验证结果存入 ewa.triggerValids[name]
// 提交时自动附加 name_TRIGGER_VALID_RESULT 参数

// 如需在提交前手动触发：
ewa.callTriggerValid(getObj('#VALID_CODE input')[0]);
```

### 扩展验证（服务端验证）

```javascript
ewa.DoValidEx(
    getObj('#FIELD_NAME input')[0],  // 要验证的元素
    "action",                         // 模式: "action" 或 "javascript"
    "checkNameExists",                // 后端 Action 名称或 JS 代码
    "CheckNameAction",                // 仅 action 模式: 后端 Action
    "名称可用",                        // 成功消息
    "名称已存在"                       // 失败消息
);
```

---

## 5. 合并单元格

### 简单合并

```javascript
// 将 fromId 字段所在行合并到 toId 字段所在行
ewa.Merge("FROM_FIELD_ID", "TO_FIELD_ID");
```

### 表达式合并

```javascript
// @@ 前缀匹配字段 ID，生成合并后的 HTML
var exp = "@@DEPT x @PERSON = @@RESULT (@DATE)";
ewa.MergeExp("RESULT_FIELD_ID", exp);

// 合并多个字段到一个容器
ewa.merges("RESULT_FIELD_ID", ["FIELD_A", "FIELD_B", "FIELD_C"], true);
// 第三个参数 isAddMemo=true 会添加备注 spans
```

表达式中：
- `@@FIELD_ID` — 匹配字段值
- `@FIELD_ID` — 同上（单 @ 也行）
- 其他文本原样输出

---

## 6. 分组与向导

### 分组 Tab 切换

将表单字段按 `groupIndex` 分成多组，点击切换显示：

```javascript
// HTML 中添加分组按钮
// <div onclick="ewa.GroupShow(this, 0)">基本信息</div>
// <div onclick="ewa.GroupShow(this, 1)">详细信息</div>

ewa.GroupShowBefore = function(obj, grpIdx){
    // 切换前回调
};

ewa.GroupShowAfter = function(obj, grpIdx){
    // 切换后回调
    $Tip("已切换到第 " + (grpIdx + 1) + " 组");
};
```

> XML 中通过 `<GroupIndex>` 元素设置字段所属组。

### 向导式分步表单

```javascript
// infos 是步骤描述数组
ewa.GuideShowCreate([
    { DES: "第一步：填写基本信息" },
    { DES: "第二步：填写详细信息" },
    { DES: "第三步：确认提交" }
]);

// 向导会自动：
// 1. 隐藏原始提交按钮
// 2. 添加"上一步"/"下一步"按钮
// 3. 按 groupIndex 显示/隐藏行
// 4. "下一步"时调用 GuideShowCheck(idx) 验证当前步字段

// 自定义步骤验证
ewa.GuideShowCheck = function(idx){
    // 默认会调用 CheckValid 验证当前步可见字段
    // 可覆盖添加额外逻辑
    return ewa.CheckValidAll();
};
```

---

## 7. 对话框中的表单

### EWA.OW 完整用法

```javascript
// 在对话框加载的帧中执行：
(function(){
    EWA.OW.Load();  // 初始化 EWA.OW.Dia / PWin / Frame

    var parentFrame = EWA.OW.Frame;      // 父窗口中的帧
    var parentWin = EWA.OW.PWin;         // 父窗口
    var dialog = EWA.OW.Dia;             // 对话框对象

    // 示例：表单提交后刷新父列表
    ewa.doPostAfter = function(ret){
        if (ret.indexOf("success") > -1) {
            parentFrame.Reload();
            EWA.OW.Close();
            return true;
        }
        return false;
    };
})();
```

### 对话框内获取父帧数据

```javascript
EWA.OW.Load();
var parentEwa = EWA.OW.Frame;

// 获取父帧 URL 参数
var parentUrl = parentEwa.getUrlClass();
var mtype = parentUrl.GetParameter("EWA_MTYPE");

// 获取父帧的 outParams（SQL 输出参数）
var outParams = parentEwa.outParams;
```

### 关闭对话框

```javascript
// 方式 1：EWA.OW（推荐，在对话框内使用）
EWA.OW.Load();
EWA.OW.Close();

// 方式 2：Behavior 链（URL 参数控制）
// EWA_P_BEHAVIOR=CLOSE_SELF

// 方式 3：直接调用
if (window._EWA_DialogWnd) {
    window._EWA_DialogWnd.CloseWindow();
}
```

---

## 8. DoAction — 调用后端 Action

### 简单调用

```javascript
// 触发后端 OnFrameDelete Action
ewa.DoAction(this, "OnFrameDelete");
```

### 带确认消息和回调

```javascript
ewa.DoAction(
    this,                        // 触发事件的 DOM 元素
    "UAct0",                     // Action 名称
    "DeleteConfirm",             // 确认消息（从 _EWA_INFO_MSG 读取）或纯文本
    "操作成功",                   // 成功提示
    [                            // 附加参数
        { Name: "targetId", Value: "123" }
    ],
    function(){                  // 回调
        $Tip("删除完成");
        ewa.Reload();
    }
);
```

### JSON 返回方式（不刷新页面）

```javascript
ewa.DoActionJSON(
    "SAct0",                     // Action 名称
    "resultJson",                // JSON 变量名
    [                            // 附加参数
        { Name: "id", Value: "456" }
    ],
    function(json){              // 回调，json 为解析后的对象
        $Tip("查询到 " + json.count + " 条记录");
    }
);
```

### 简单 JSON 加载

```javascript
ewa.LoadJson("GetStats", function(json){
    getObj('#TOTAL_COUNT').text(json.total);
});
// 等价于: GET /ewa?...&EWA_ACTION=GetStats&EWA_AJAX=JSON
```

---

## 9. 开关按钮（Switch Button）

XML 中 `<Tag Tag="switch"/>` 的控件：

```javascript
ewa.extSwitchCallBack = function(source, rst){
    // source: <input type=checkbox> 元素
    // rst: 服务器返回文本
    if (rst.indexOf("true") > -1) {
        $Tip("状态切换成功");
    } else {
        $Tip("切换失败");
        // 恢复开关状态
        source.checked = !source.checked;
    }
};
```

---

## 10. 隐藏无内容行

```javascript
ewa.hiddenNoContentRow();
// 自动隐藏 .EWA_TD_M 中无文本且无 img/a/input/textarea 的行
// 被隐藏的行设置 hiddennocontentrow=1（GroupShow 不会显示它们）
```

---

## 11. Memo 合并

```javascript
ewa.MergeMemo();
// 将所有 textarea 类型的 memo 字段合并到一个带 Tab 的行中
// 生成 ewa_merge_tab span 用于切换
```

---

## 12. 重写 Info/Memo 行

```javascript
var data = [
    { id: "1", info: "审批通过", memo: "同意" },
    { id: "2", info: "驳回", memo: "需要修改" }
];

var trs = ewa.RewriteInfo(
    JSON.stringify(data),
    "id",     // id 字段名
    "info",   // info 字段名
    "memo",   // memo 字段名
    function(tr, entry, index){
        // 每行回调，可自定义样式
        tr.style.color = entry.info === "驳回" ? "red" : "";
    }
);
```

---

## 常见陷阱

| 陷阱 | 解决 |
|------|------|
| `@SYS_FRAME_UNID` 在 JS 中是什么 | 是服务端替换的占位符，运行时是具体帧 ID 字符串（如 `ewa_frame_abc123`） |
| `doPostAfter` 返回什么阻止默认行为 | 返回 `true` 会阻止后续的 `eval(ret)` |
| `ReloadAfter` 和 `doPostAfter` 用哪个 | 用 `doPostAfter`。`ReloadAfter` 是旧名，仅作为兼容回退 |
| 表单提交无响应 | 检查 `DoPostBefore` 是否返回了 `false` |
| 对话框内 `EWA.OW.Frame` 为 null | 必须先调用 `EWA.OW.Load()` 初始化 |
| `setMust` 无效 | 字段名不区分大小写，但必须匹配 XML 中定义的 `XItem Name` |
| 重复提交 | 框架有 `ewa.posting` 防重标记，但自定义 AJAX 需自行处理 |
| `Mearge` 拼写 | 源码中 `Mearge` 是 `Merge` 的废弃别名，会打印警告，勿用 |
| 对话框传参带入了错误的 XMLNAME/ITEMNAME | 调用 `getUrlClass()` 获取 URL 后，先 `u1.RemoveEwa()` 清除 EWA 框架参数（XMLNAME、ITEMNAME 等），再 `AddParameter()` 添加新参数 |

---

## 13. EWA_ URL 参数速查

### 高频实用

| 参数 | 说明 | 示例 |
|------|------|------|
| `EWA_IN_DIALOG` | `1` = 固定高度窗口打开，内容自适应滚动 | `u1.AddParameter("EWA_IN_DIALOG", "1")` |
| `EWA_P_BEHAVIOR` | 行为链：`RELOAD_PARENT,CLOSE_SELF` | `EWA_P_BEHAVIOR=RELOAD_PARENT,CLOSE_SELF` |
| `EWA_PARENT_FRAME` | 父帧 UNID，配合行为链使用 | `EWA_PARENT_FRAME=@SYS_FRAME_UNID` |
| `EWA_NO_CONTENT` | `1` = 仅执行不输出内容 | 用于纯后端操作 |
| `EWA_LANG` | 语言切换：`zhcn`/`enus`，会保留到 session | `EWA_LANG=enus` |

### 少用但好用

| 参数 | 说明 | 示例 |
|------|------|------|
| `EWA_HIDDEN_FIELDS` | 按条件隐藏字段，在 `<LogicShow>` 中配置 | `<Set HiddenFields="FIELD_A" Name="rule" ParaExp="'@MODE'='VIEW'"/>` |
| `EWA_FRAME_UNID_PREFIX` | 更改帧 UNID 前缀，避免同页面多个帧冲突 | `EWA_FRAME_UNID_PREFIX=my_prefix_` |
| `EWA_FRAMESET_NO` | `1` = 不显示 frame 框架 | `EWA_FRAMESET_NO=1` |
| `EWA_WIDTH` / `EWA_HEIGHT` | 覆盖帧尺寸 | `EWA_WIDTH=900&EWA_HEIGHT=600` |
