---
name: ewa-listframe
description: "Use when: 编写 EWA ListFrame 列表前端 JS 代码、行数据操作、选中行、翻页排序、检索过滤、展开行详情、动态列、底部汇总、行内编辑。按任务组织，含完整代码示例。"
trigger: ewa-listframe, EWA ListFrame, 列表操作, 行数据, SelectChecked, GetRowKey, AddPreRow, 翻页, 排序, 检索, 展开行, 动态列, 底部汇总, 行内编辑, EWA_ListFrameClass
---

# EWA ListFrame 操作指南

面向前端 JS 开发者的 EWA ListFrame 列表操作参考。**按任务组织**，每个场景给完整可用的代码。

> `ewa` 变量 = `EWA.F.FOS['@SYS_FRAME_UNID']` — 当前 ListFrame 实例。
> 列表帧容器 ID 为 `#EWA_LF_@SYS_FRAME_UNID`。

---

## 速查表

| 任务 | 方法 | 优先级 |
|------|------|--------|
| 🔥 获取选中行键值 | `ewa.SelectChecked()` | 高频 |
| 🔥 从 DOM 获取行信息 | `ewa.GetRowKey()` / `ewa.GetRow()` | 高频 |
| 🔥 刷新列表 | `ewa.Reload()` | 高频 |
| 🔥 调用后端 Action | `ewa.DoAction()` | 高频 |
| 🔥 翻页 / 排序 | `ewa.Goto()` / `ewa.Sort()` | 高频 |
| 📋 打开检索对话框 | `ewa.Search()` | 中频 |
| 📋 内联检索栏 | `ewa.ShowSearch()` | 中频 |
| 📋 展开行详情 | `ewa.AddPreRow()` | 中频 |
| 📋 动态添加列 | `ewa.AddColumns()` | 中频 |
| 📋 底部汇总 | `ewa.SubBottoms()` | 中频 |
| 📋 合并列 | `ewa.Merge()` / `ewa.MergeExp()` | 中频 |
| 📋 行内编辑 | `ewa.ShowEdit()` | 中频 |
| 🔧 局部刷新 | `ewa.replaceRowsData()` | 低频 |
| 🔧 固定表头/列 | `ewa.stickyHeaders()` / `ewa.stickyColumns()` | 低频 |

---

## 1. 行数据操作

### 获取选中行键值

```javascript
// 获取选中行的键值（逗号分隔字符串）
var keys = ewa.SelectChecked();
// 例: "123,456,789"

// 获取选中行的 TR 元素数组
var rows = ewa.SelectCheckedRows();

// 获取选中行中的 checkbox input 元素数组
var inputs = ewa.SelectCheckedInputs();
```

### 从 DOM 元素获取所在行信息

```javascript
// 从按钮/链接等元素获取所在行的键值
var rowKey = ewa.GetRowKey(buttonEl);

// 从按钮/链接等元素获取所在行的 TR 元素
var tr = ewa.GetRow(buttonEl);
```

### 获取行内字段值

```javascript
// 限定在当前 ListFrame
function getObj(exp){
    return exp ? $('#EWA_LF_@SYS_FRAME_UNID').find(exp) : $('#EWA_LF_@SYS_FRAME_UNID');
}

// 获取某行某列的值（span 显示模式）
var value = getObj('#FIELD_NAME').text();

// 获取某行某列的值（编辑模式 input）
var value = getObj('#FIELD_NAME input').val();

// 遍历选中行获取字段值
ewa.SelectCheckedRows().forEach(function(tr){
    var key = ewa.GetRowKey(tr);
    var name = $(tr).find('#FIELD_NAME').text();
    console.log(key, name);
});
```

### 行点击事件

```javascript
// 设置行点击回调（MDownEvent）
ewa.MDownEvent = function(frameUnid, tr, key, newTr, evt){
    console.log("行键值:", key);
    // key 是该行的主键值
};

// 自定义行点击前检查（返回 false 阻止选中）
ewa.AddPreRowCheck = function(tr, key, evt){
    return true; // 允许选中
};
```

### 双击行触发按钮

```javascript
// 启用双击行触发第 0 个按钮的动作
ewa.DblClick(0);
```

---

## 2. 翻页与排序

### 翻页

```javascript
ewa.Goto(2);          // 跳到第 2 页
ewa.Goto(1, refUrl);  // 带 referer 跳到第 1 页
```

### 排序

```javascript
ewa.Sort("CREATE_DATE");        // 按 CREATE_DATE 升序
ewa.Sort("CREATE_DATE desc");   // 降序
```

> 排序有 500ms 防抖。URL 参数 `EWA_LF_ORDER` 控制排序字段。

### 切换每页条数

```javascript
ewa.NewPageSize(50);   // 每页 50 条
```

---

## 3. 检索

### 打开检索对话框

```javascript
ewa.Search();
// 弹出 EWA 标准检索对话框，用户输入条件后自动提交检索
```

### 显示内联检索栏

```javascript
ewa.ShowSearch();
// 在列表顶部显示内联检索输入框

ewa.ShowSearch(true);
// true = 组合多个文本检索为一个复合检索框
```

### 清除检索

```javascript
ewa.SearchClear();
```

### 标记检索关键词

```javascript
ewa.SearchMark();
// 高亮显示检索关键词（红色）
```

### 日期范围检索

```javascript
// 设置日期范围检索
ewa.SearchFilterDate("Today");      // 今日
ewa.SearchFilterDate("Week");       // 本周
ewa.SearchFilterDate("Month");      // 本月
ewa.SearchFilterDate("Quarter");    // 本季度
ewa.SearchFilterDate("Year");       // 本年
ewa.SearchFilterDate("Today-EOM");  // 今天到月末
ewa.SearchFilterDate("Clear");      // 清除
```

### URL 参数初始化检索

```javascript
var u = ewa.getUrlClass();
u.RemoveEwa();
u.AddParameter("ewa_search", "FIELD_NAME[lk]关键词,STATUS[eq]1");
ewa.Reload(u.GetUrl());
```

---

## 4. 展开行详情

### 基本展开行模式

```javascript
ewa.AddPreRow(function(frameUnid, tr, key, newTr, evt){
    // tr: 数据行 TR
    // newTr: 展开详情行的 TR（初始 display:none）
    // key: 该行键值
    var u = new EWA_UrlClass(ewa.getUrlClass().GetUrl());
    u.AddParameter("itemname", "DETAIL.LF.View");
    u.AddParameter("id", key);
    $Install(u.GetUrl(), newTr.cells[0].id, function(){});
});
```

### 自定义展开前检查

```javascript
ewa.AddPreRowCheck = function(tr, key, evt){
    // 返回 false 阻止展开
    if (!hasPermission(key)) {
        $Tip("无权限查看");
        return false;
    }
    return true;
};
```

### 展开行关闭事件

```javascript
ewa.AddPreRowCloseBeforeEvent = function(frameUnid, tr, key, newTr, evt){
    // 关闭前清理
};

ewa.AddPreRowCloseEvent = function(frameUnid, tr, key, newTr, evt){
    // 关闭后清理
};
```

---

## 5. 选中行模式

### 单选模式

```javascript
ewa.SelectSingle();
// 设置 IsTrSelect=true, _TrSelectMulti=false
// 点击行时只选中一行，取消其他行的选中
```

### 多选模式

```javascript
ewa.SelectMulti();
// 设置 IsTrSelect=true, _TrSelectMulti=true
// 点击行时切换该行的选中状态
```

### 全选 / 取消全选

```javascript
ewa.CheckedAll();
// 切换全选状态，调用 CheckedAllAfter 回调
```

```javascript
ewa.CheckedAllAfter = function(){
    var count = ewa.SelectCheckedRows().length;
    $Tip("已选中 " + count + " 行");
};
```

### 禁用自动点击选中

```javascript
ewa.IsNotMDownAutoChecked = true;
// 点击行时不自动勾选 checkbox
```

---

## 6. 动态列操作

### 添加列

```javascript
ewa.AddColumns([
    {
        colId: "NEW_COL",
        colText: "新列",
        colHtml: "@@VALUE",      // @@VALUE 替换为单元格值
        colType: "span"          // span / input / checkbox
    }
]);

// 带额外属性
ewa.AddColumns([
    { colId: "ACTIONS", colText: "操作", colHtml: '<button onclick="doAction(@@ID)">编辑</button>' }
], "ACTIONS", "操作", "", "span", [
    { Name: "class", Value: "action-col" }
]);
```

### 显示/隐藏列

```javascript
ewa.ShowHiddenColumn(3, "block");   // 显示第 3 列
ewa.ShowHiddenColumn(3, "none");    // 隐藏第 3 列

ewa.ShowHiddenColumns([1, 3, 5], "none");  // 批量隐藏
```

### 固定表头 / 固定列

```javascript
ewa.stickyHeaders();
// 表头固定，滚动时保持在顶部

ewa.stickyColumns(2);
// 固定前 2 列，横向滚动时保持可见
```

> **注意**：`stickyHeaders` 和 `stickyColumns` 互斥，不能同时使用。

---

## 7. 底部汇总

### 对列求和

```javascript
ewa.SubBottoms("AMOUNT,QANTITY");
// 对 AMOUNT 和 QANTITY 列求和，显示在底部汇总行
```

### 重新计算汇总

```javascript
ewa.reCalcBottoms();
// 数据变化后重新计算汇总值
```

---

## 8. 合并列

### 简单合并

```javascript
// 将 FROM_COL 列合并到 TO_COL 列
ewa.Merge("FROM_COL_ID", "TO_COL_ID");
```

### 表达式合并

```javascript
var exp = "@@NAME (@AGE) - @@DEPT";
ewa.MergeExp("TO_COL_ID", exp);
```

### 批量合并

```javascript
ewa.Merges([
    { from: "COL_A", to: "COL_B", str: "@@COL_A @@COL_B" },
    { from: "COL_C", to: "COL_D", header: true }  // 合并表头
]);
```

### 合并表头

```javascript
ewa.mergeHeaders("FROM_ID", "合并后的标题", 2);
// 合并表头单元格，跨 2 行
```

---

## 9. 行内编辑

### 显示编辑控件

```javascript
ewa.ShowEdit(cellElement);
// 将 span 显示切换为 input 编辑模式
```

### 编辑完成

编辑完成后框架自动触发 `OnListFrameUpdateCell` Action。

---

## 10. 局部刷新（不重载页面）

### 基于数据对比的局部刷新

```javascript
ewa.replaceRowsData(
    searchExp,                    // 检索表达式，null 表示当前检索
    function(sourceTd, targetTd){ // 自定义单元格替换逻辑
        targetTd.innerHTML = sourceTd.innerHTML;
    },
    httpReferer,                  // 来源 URL
    function(changedTrClones){    // 回调：变化的行
        console.log(changedTrClones.length + " 行已更新");
    },
    true                          // isStopReload: true 阻止完整重载
);
```

### 基于 HTML 的局部刷新

```javascript
ewa.replaceRowsWithDataHtml(
    newHtmlString,                // 新的列表 HTML
    function(sourceTd, targetTd){
        targetTd.innerHTML = sourceTd.innerHTML;
    },
    httpReferer,
    function(changedTrClones){},
    true
);
```

> **GOTCHA**：需要 URL 参数 `ewa_row_sign=yes` 启用 MD5 行签名对比，才能跳过未变化的行。

### 刷新页面（局部）

```javascript
ewa.refreshPage(httpReferer, callBack, isStopReload);
// 等价于 replaceRowsData 的快捷调用
```

---

## 11. 添加行

### 追加新行

```javascript
ewa.AddRow(["单元格1", "单元格2", "单元格3"]);
// 在列表末尾添加一行
```

### 插入展开行

```javascript
var newTr = ewa.newRowOneTd(currentTr);
// 在 currentTr 下方创建/获取展开详情行
// 返回 TR 元素，初始 display:none
```

---

## 12. 按钮映射

### 按钮点击映射

```javascript
// 将按钮 A 的 onclick 映射到按钮 B
ewa.BindButton("BUTTON_A", "BUTTON_B");
// 每行中 BUTTON_A 的点击事件会转发到 BUTTON_B
```

---

## 13. 调用后端 Action

### 带行键值的 Action

```javascript
// ListFrame 的 DoAction 会自动附加：
// - EWA_ACTION_KEY = 当前行键值
// - IDS_SPLIT = 所有选中行的键值（逗号分隔）
ewa.DoAction(buttonEl, "UAct0", "确认删除？", "删除成功");
```

### 批量操作选中行

```javascript
var keys = ewa.SelectChecked();
if (!keys) {
    $Tip("请先选中要操作的行");
    return;
}

// IDS_SPLIT 会自动发送所有选中行键值到后端
ewa.DoAction(this, "BatchAction", "确认批量操作？", "操作完成");
```

### 批量操作 — 弹出对话框

```javascript
ewa.ext_batchAction = function(){
    var ids = ewa.SelectChecked();
    if(!ids){ $Tip("请先勾选要操作的记录"); return; }
    var u1 = ewa.getUrlClass();
    u1.RemoveEwa();  // 清除当前 XMLNAME、ITEMNAME 等 EWA 参数
    u1.AddParameter("MC_IDS_BATCH", ids);
    u1.AddParameter("EWA_MTYPE", "N");
    EWA.UI.Dialog.OpenReloadClose("@SYS_FRAME_UNID", "@xmlName", "TARGET_FRAME.F.NM", false, u1.GetParas());
};
```

> **关键**：传给弹窗前必须 `u1.RemoveEwa()`，否则弹窗会继承当前列表的 `XMLNAME`/`ITEMNAME`，导致加载错误的模板。

---

## 14. 新建 / 修改记录

### 方式一：框架自动生成的 `ext_NewOrModifyOrCopy`

ListFrame 创建时框架自动注入此方法（定义在 `EwaDefine.xml` 模板中）：

```javascript
// 打开新建对话框
ewa.ext_NewOrModifyOrCopy("N");

// 打开修改对话框（带主键参数）
ewa.ext_NewOrModifyOrCopy("M", "id=123");

// 打开复制对话框
ewa.ext_NewOrModifyOrCopy("C", "id=123");
```

**优势**：可添加额外参数，如 `EWA_IN_DIALOG`：

```javascript
// 在固定高度窗口打开，内容自适应滚动
ewa.ext_NewOrModifyOrCopy = function(mtype, pkParas){
    const u1 = new EWA_UrlClass(ewa.Url);
    u1.RemoveEwa();
    u1.AddParameter("EWA_MTYPE", mtype);
    u1.AddParameter("EWA_IN_DIALOG", "1");  // 固定高度窗口
    let paras = u1.GetParas();
    if(pkParas) paras += '&' + pkParas;
    EWA.UI.Dialog.OpenReloadClose('@SYS_FRAME_UNID', '@xmlName', 'TABLE_NAME.F.NM', false, paras);
};
```

### 方式二：内置方法 `RecordNew` / `RecordModify`

```javascript
ewa.RecordNew("@xmlName", "ITEM.F.N", "extra_param1=v1");
// 自动打开新建表单对话框

ewa.RecordModify("@xmlName", "ITEM.F.M", "extra_param1=v1");
// 自动获取当前选中行，打开修改表单对话框
```

---

## 15. 分组显示

### 分组折叠/展开

```javascript
ewa.GroupShowHidden(this);
// 切换分组的展开/折叠状态
```

---

## 16. 组合检索框

```javascript
ewa.composeSearchTexts();
// 将多个检索输入框合并为一个复合检索框
// 每个 input 的 name 变为逗号分隔的多字段名
```

---

## 17. 导出数据

```javascript
ewa.DownloadData("excel");   // 导出 Excel
ewa.DownloadData("csv");     // 导出 CSV
ewa.DownloadData("pdf", "ExportAction");  // 通过指定 Action 导出
```

---

## 18. 工具方法

### 获取 URL 对象

```javascript
var u = ewa.getUrlClass();
u.AddParameter("EWA_MTYPE", "N");
ewa.Reload(u.GetUrl());
```

### 带参数重新加载

```javascript
ewa.changeTag("param1=v1&param2=v2");
// 带新参数重新加载列表
```

### 切换按钮显示位置

```javascript
ewa.ReShow();                   // 操作按钮显示在工具栏
ewa.ReShowWithNoButtons();      // 隐藏操作按钮
ewa.ReShowButtonsInDailogTitle(); // 按钮移到对话框标题栏
```

---

## 19. EWA_ URL 参数速查

### 高频实用

| 参数 | 说明 | 示例 |
|------|------|------|
| `EWA_IN_DIALOG` | `1` = 固定高度窗口打开，内容自适应滚动 | `u1.AddParameter("EWA_IN_DIALOG", "1")` |
| `EWA_P_BEHAVIOR` | 行为链：`RELOAD_PARENT,CLOSE_SELF` | `EWA_P_BEHAVIOR=RELOAD_PARENT,CLOSE_SELF` |
| `EWA_PARENT_FRAME` | 父帧 UNID，配合行为链使用 | `EWA_PARENT_FRAME=@SYS_FRAME_UNID` |
| `EWA_SEARCH` | 高级检索表达式 | `ewa_search=FIELD[lk]关键词,STATUS[eq]1` |
| `EWA_NO_CONTENT` | `1` = 仅执行不输出内容 | 用于纯后端操作 |
| `EWA_JSON_FIELD_CASE` | JSON 字段大小写：`lower`/`upper` | `EWA_JSON_FIELD_CASE=lower` |
| `EWA_RECYCLE` | `NO` = 隐藏回收箱 | `EWA_RECYCLE=NO` |
| `EWA_PAGESIZE` | 每页条数 | `EWA_PAGESIZE=50` |
| `EWA_IS_SPLIT_PAGE` | `yes`/`no` 强制分页开关 | `EWA_IS_SPLIT_PAGE=no` |

### 少用但好用

| 参数 | 说明 | 示例 |
|------|------|------|
| `EWA_HIDDEN_FIELDS` | 按条件隐藏字段，在 `<LogicShow>` 中配置 | `<Set HiddenFields="FIELD_A,FIELD_B" Name="rule" ParaExp="'@MODE'='VIEW'"/>` |
| `EWA_FRAME_UNID_PREFIX` | 更改帧 UNID 前缀，避免同页面多个帧冲突 | `EWA_FRAME_UNID_PREFIX=my_prefix_` |
| `EWA_LANG` | 语言切换：`zhcn`/`enus`，会保留到 session | `EWA_LANG=enus` |
| `EWA_FRAMESET_NO` | `1` = 不显示 frame 框架 | `EWA_FRAMESET_NO=1` |
| `EWA_WIDTH` / `EWA_HEIGHT` | 覆盖帧尺寸 | `EWA_WIDTH=900&EWA_HEIGHT=600` |

### `EWA_HIDDEN_FIELDS` 按条件隐藏字段

在 XML 的 `<LogicShow>` 中配置，根据参数条件动态隐藏字段：

```xml
<LogicShow>
    <!-- 当 @MODE='VIEW' 时隐藏 FIELD_A 和 FIELD_B -->
    <Set HiddenFields="FIELD_A,FIELD_B" Name="hideInView" ParaExp="'@MODE'='VIEW'"/>
    <!-- 当 @EWA_AJAX='DOWN_DATA' 时隐藏操作按钮 -->
    <Set HiddenFields="butAdd,butDelete" Name="hideButtons" ParaExp="'@EWA_AJAX'='DOWN_DATA'"/>
</LogicShow>
```

`ParaExp` 使用 SQL 表达式语法，支持 `and`/`or`/`1=2`（永远隐藏）等。

---

## 常见陷阱

| 陷阱 | 解决 |
|------|------|
| 列表帧选择器前缀 | 用 `#EWA_LF_@SYS_FRAME_UNID`，不是 `#EWA_FRAME_` |
| `SelectChecked()` 返回空 | 检查 `IsTrSelect` 是否启用（调用 `SelectSingle()` 或 `SelectMulti()`） |
| 局部刷新不生效 | URL 需带 `ewa_row_sign=yes` 启用 MD5 行签名 |
| `stickyHeaders` 和 `stickyColumns` 冲突 | 两者互斥，只能用其中一个 |
| DoAction 无响应 | ListFrame 的 DoAction 有 1000ms 防抖（APP 模式） |
| 检索条件不清除 | 调用 `ewa.SearchClear()` 清除检索 |
| `getUrlClass().GetParameter()` 取不到值 | EWA 的 URL 参数优先用 `ewa.Url` 上的 `EWA_UrlClass` 实例获取：`let u = ewa.getUrlClass(); u.GetParameter("KEY")` |
| 批量操作需要多选模式 | XML 中 `<ListUI>` 设置 `luSelect="M"` 启用多选，单条模式 `luSelect="S"` |
| 按钮 URL 缺少上下文路径 | JS 中拼接 URL 用 `EWA.CP + '/path'`，不要硬编码 `/path` |
