# XML 对比报告：标准 vs 生成

## 文件信息
- **标准文件**: `/Users/admin/java/ewa_page_cached_pf2023/scripts_cached/2014_center/test/10CRM_COM.LF.M.xml`
- **生成文件**: `temp/ewa_script_test/20260327_190013/CRM_COM.LF.M.xml`

## 主要差异

### 1. 根节点差异
| 属性 | 标准 | 生成 | 状态 |
|------|------|------|------|
| 根元素 | `EasyWebTemplates/EasyWebTemplate` | `EasyWebTemplate` | ⚠️ 不同 |
| Author | `gl` | `System` | ℹ️ 可接受 |
| CreateDate | `2026-03-27 19:00:34` | `2026-03-27 19:00:13` | ℹ️ 时间戳 |
| UpdateDate | `2026-03-27 19:07:10` | `2026-03-27 19:00:13` | ℹ️ 时间戳 |
| standalone | 无 | `no` | ℹ️ XML 声明 |

### 2. Page 节点差异

#### 2.1 缺失的节点（生成文件缺少）
- ❌ `PageAttributeSet/`
- ❌ `RowAttributeSet/`
- ❌ `GroupSet/`
- ❌ `ChartsShow/`
- ❌ `RedrawJson/`
- ❌ `BoxJson/`
- ❌ `LeftJson/`
- ❌ `FrameHtml`
- ❌ `PageSize/`
- ❌ `ListUI/`
- ❌ `MenuShow/`
- ❌ `Menu/`
- ❌ `Tree/`
- ❌ `HtmlFrame/`
- ❌ `TreeIconSet/`
- ❌ `MGAxisX/`
- ❌ `MGAxisY/`
- ❌ `MGCell/`
- ❌ `LogicShow/`

#### 2.2 新增的节点（生成文件额外有）
- ✅ `Tag/Set Tag="span"` - ListFrame 默认 Tag
- ✅ `Cached/Set CachedSeconds="" CachedType=""` - 缓存配置

#### 2.3 Size 节点差异
| 属性 | 标准 | 生成 |
|------|------|------|
| FrameCols | ✓ | ✗ |
| Height | ✓ | ✗ |
| HiddenCaption | ✓ | ✗ |
| TextareaAuto | ✓ | ✗ |

### 3. AddScript/AddCss 差异
| 项目 | 标准 | 生成 | 状态 |
|------|------|------|------|
| AddScript | 在 Page 节点内 | 在 Page 节点末尾 | ⚠️ 位置不同 |
| AddCss | `<AddCss/>` | 从 EwaDefine.xml 读取 | ✅ 配置驱动 |

### 4. Action/SqlSet 差异
| SQL 名称 | 标准 | 生成 | 状态 |
|----------|------|------|------|
| OnPageLoad SQL | ✅ 正确 | ✅ 正确 | ✅ 一致 |
| OnFrameDelete SQL | `WHERE 1>2 -- table not defined pk` | `WHERE 1>2 -- table not defined pk` | ✅ 一致 |
| OnFrameRestore SQL | `WHERE 1>2 -- table not defined pk` | `WHERE 1>2 -- table not defined pk` | ✅ 一致 |
| SAct0-2 SQL | `-- enter your sql` | 从 EwaDefine.xml 读取 | ✅ 配置驱动 |
| UAct0-2 SQL | `-- enter your sql` | 从 EwaDefine.xml 读取 | ✅ 配置驱动 |
| CheckError SQL | ✅ 有 | ❌ 缺失 | ⚠️ 需添加 |

### 5. XItems 差异

#### 5.1 字段 XItem
| 字段 | 标准 OrderSearch | 生成 OrderSearch | 状态 |
|------|-----------------|-----------------|------|
| CRM_COM_ID (INT) | `IsOrder="1" SearchType=""` | `IsOrder="1" SearchType=""` | ✅ 一致 |
| CRM_COM_NAME (NVARCHAR 200) | `IsOrder="0" SearchType="text"` | `IsOrder="0" SearchType="text"` | ✅ 一致 |
| CRM_COM_SNAME (NVARCHAR 150) | `IsOrder="0" SearchType="text"` | `IsOrder="0" SearchType="text"` | ✅ 一致 |
| CRM_COM_CODE (NVARCHAR 40) | `IsOrder="1" SearchType="text"` | `IsOrder="1" SearchType="text"` | ✅ 一致 |
| CRM_COM_ADDR (NVARCHAR 500) | `IsOrder="0" SearchType="text"` | `IsOrder="0" SearchType="text"` | ✅ 一致 |
| CRM_COM_EMAIL (NVARCHAR 100) | `IsOrder="1" SearchType="text"` | `IsOrder="1" SearchType="text"` | ✅ 一致 |
| CRM_COM_TELE (NVARCHAR 100) | `IsOrder="1" SearchType="text"` | `IsOrder="1" SearchType="text"` | ✅ 一致 |
| CRM_COM_MOBILE (NVARCHAR 50) | `IsOrder="1" SearchType="text"` | `IsOrder="1" SearchType="text"` | ✅ 一致 |
| CRM_COM_CDATE (DATETIME) | `IsOrder="1" SearchType=""` | `IsOrder="1" SearchType=""` | ✅ 一致 |
| CRM_COM_MDATE (DATETIME) | `IsOrder="1" SearchType=""` | `IsOrder="1" SearchType=""` | ✅ 一致 |
| CRM_COM_STATE (VARCHAR 10) | `IsOrder="1" SearchType="text"` | `IsOrder="1" SearchType="text"` | ✅ 一致 |

#### 5.2 按钮 XItem
| 按钮 | 标准 | 生成 | 状态 |
|------|------|------|------|
| butNew | ✅ | ✅ | ✅ 一致 |
| butModify | ✅ | ✅ | ✅ 一致 |
| butCopy | ✅ | ✅ | ✅ 一致 |
| butDelete | ✅ | ✅ | ✅ 一致 |
| butRestore | ✅ | ✅ | ✅ 一致 |

#### 5.3 空节点差异
标准文件每个 XItem 包含以下空节点（生成文件缺失）：
- `GroupIndex/`
- `InitValue/`
- `XStyle/`
- `Style/`
- `ParentStyle/`
- `AttributeSet/`
- `EventSet/` (按钮除外)
- `IsHtml/`
- `OrderSearch/` (按钮)
- `MaxMinLength/` (按钮)
- `MaxMinValue/` (按钮)
- `IsMustInput/` (按钮)
- `Switch/`
- `DataItem/` (按钮)
- `DispEnc/`
- `DataRef/`
- `List/`
- `UserSet/`
- `CallAction/` (butDelete/butRestore 有)
- `OpenFrame/`
- `Frame/`
- `UserControl/`
- `DefineFrame/`
- `PopFrame/`
- `signature/`
- `Upload/`
- `VaildEx/`
- `MGAddField/`
- `AnchorParas/`
- `LinkButtonParas/`
- `DopListShow/`
- `ReportCfg/`
- `CombineFrame/`
- `AddrMapRels/`
- `QRCode/`
- `ImageDefault/`

### 6. CallAction 差异
| 按钮 | 标准 | 生成 | 状态 |
|------|------|------|------|
| butDelete | `<Set Action="OnFrameDelete" ConfirmInfo="DeleteBefore"/>` | `<Set Action="OnFrameDelete"/>` | ⚠️ 缺失 ConfirmInfo |
| butRestore | `<Set Action="OnFrameRestore"/>` | `<Set Action="OnFrameRestore"/>` | ✅ 一致 |

## 总结

### ✅ 完全一致的功能
1. OrderSearch 配置（IsOrder, SearchType）
2. 按钮配置（butNew, butModify, butCopy, butDelete, butRestore）
3. 主要 SQL 生成（OnPageLoad, OnFrameDelete, OnFrameRestore）
4. 字段 Tag 配置（span）
5. 按钮 Tag 配置（button）
6. EventValue JavaScript 表达式

### ⚠️ 需要修复的差异
1. **根节点**: 应该是 `EasyWebTemplates/EasyWebTemplate` 而不是单独的 `EasyWebTemplate`
2. **缺失的空节点**: 需要添加标准的空节点以保持兼容性
3. **CallAction ConfirmInfo**: butDelete 需要添加 `ConfirmInfo="DeleteBefore"`
4. **CheckError SQL**: 需要添加默认的 CheckError SQL
5. **AddScript 位置**: 应该在 Page 节点开始处，而不是末尾

### ℹ️ 可接受的差异
1. Author 字段（System vs gl）
2. 时间戳
3. XML 声明的 standalone 属性
4. Page 节点的一些空节点（PageAttributeSet 等）
5. Size 节点的一些空属性

## 建议修复优先级

### P0 - 必须修复
1. 根节点结构（如果需要兼容旧系统）
2. CallAction ConfirmInfo

### P1 - 建议修复
1. 添加缺失的空节点（提高兼容性）
2. AddScript 位置调整
3. 添加 CheckError SQL

### P2 - 可选修复
1. Size 节点属性补充
2. Page 节点空节点补充
