# CRM_COM.LF.M XML 结构对比分析

## 生成的 XML vs 示例 XML

### 1. 根节点对比

**生成的 XML**:
```xml
<EasyWebTemplate Name="CRM_COM.LF.M">
```

**示例 XML**:
```xml
<EasyWebTemplate Author="" CreateDate="2018-12-27 16:08:31" 
                 Name="CRM_COM.Lf.M" 
                 UpdateDate="2024-01-06 15:40:39">
```

**差异**:
- ❌ 缺少 `Author` 属性
- ❌ 缺少 `CreateDate` 属性
- ❌ 缺少 `UpdateDate` 属性
- ❌ Name 大小写不同：`CRM_COM.LF.M` vs `CRM_COM.Lf.M`

---

### 2. Page 节点对比

#### 2.1 基本结构

**生成的 XML**:
```xml
<Page>
  <Name>
    <Set Name="CRM_COM.LF.M"/>
  </Name>
  <XItems>
    <!-- 11 个字段 -->
  </XItems>
</Page>
```

**示例 XML**:
```xml
<Page>
  <Name>
    <Set Name="CRM_COM.Lf.M"/>
  </Name>
  <FrameTag><Set FrameTag="ListFrame"/></FrameTag>
  <SkinName><Set IsXhtml="0" SkinName="Test1"/></SkinName>
  <DataSource><Set DataSource="globaltravel"/></DataSource>
  <AllowJsonExport/>
  <ConfigMemo><Set><ConfigMemo/></Set></ConfigMemo>
  <Cached><Set CachedSeconds="" CachedType=""/></Cached>
  <Acl><Set Acl="com.gdxsoft.web.acl.BusinessImpl"/></Acl>
  <Log><Set Log="com.gdxsoft.web.log.EwaScriptLog"/></Log>
  <DescriptionSet>
    <Set Info="客户关系列表" Lang="zhcn" Memo=""/>
    <Set Info="List of CRM" Lang="enus" Memo=""/>
  </DescriptionSet>
  <Size><Set FrameCols="" HAlign="center" Width="100%"/></Size>
  <AddHtml><Set><Top><![CDATA[ ]]></Top><Bottom/></Set></AddHtml>
  <AddScript><Set><Top/><Bottom><![CDATA[...]]></Bottom></Set></AddScript>
  <AddCss><Set><AddCss>...</AddCss></Set></AddCss>
  <XItems>
    <!-- 多个字段 -->
  </XItems>
</Page>
```

**缺失的节点**:
- ❌ FrameTag
- ❌ SkinName
- ❌ DataSource
- ❌ AllowJsonExport
- ❌ ConfigMemo
- ❌ Cached
- ❌ Acl
- ❌ Log
- ❌ DescriptionSet (多语言描述)
- ❌ Size
- ❌ AddHtml
- ❌ AddScript (JavaScript 代码)
- ❌ AddCss

---

### 3. XItem 节点对比

#### 3.1 生成的 XItem 结构

```xml
<XItem Name="CRM_COM_ID">
  <Tag><Set IsLFEdit="0"/></Tag>
  <Name><Set Name="CRM_COM_ID"/></Name>
  <DataItem>
    <Set DataField="CRM_COM_ID" DataType="INT" DesZHCN="公司 ID"/>
  </DataItem>
  <DescriptionSet>
    <Set Info="公司 ID" Lang="zhcn"/>
  </DescriptionSet>
</XItem>
```

#### 3.2 示例 XItem 结构

```xml
<XItem Name="CRM_COM_NAME">
  <Tag><Set IsLFEdit="0" SpanShowAs="" Tag="text"/></Tag>
  <Name><Set Name="CRM_COM_NAME"/></Name>
  <GroupIndex><Set GroupIndex="1"/></GroupIndex>
  <InitValue><Set InitValue=""/></InitValue>
  <DescriptionSet>
    <Set Info="公司名称" Lang="zhcn" Memo=""/>
    <Set Info="Company name" Lang="enus" Memo=""/>
  </DescriptionSet>
  <XStyle/>
  <Style><Set Style=""/></Style>
  <ParentStyle><Set ParentStyle=""/></ParentStyle>
  <AttributeSet><Set AttLogic="" AttName="" AttValue=""/></AttributeSet>
  <EventSet><Set EventLogic="" EventName="" EventType="" EventValue=""/></EventSet>
  <IsHtml/>
  <OrderSearch>
    <Set GroupTestLength="" IsGroup="" IsGroupDefault="" IsOrder="1" 
         IsSearchQuick="1" OrderExp="" SearchExp="" SearchMulti="2" 
         SearchSql="..." SearchType="fix"/>
  </OrderSearch>
  <MaxMinLength><Set MaxLength="200" MinLength=""/></MaxMinLength>
  <MaxMinValue><Set MaxValue="" MinValue=""/></MaxMinValue>
  <IsMustInput><Set IsMustInput="1"/></IsMustInput>
  <Switch/>
  <DataItem>
    <Set DataField="CRM_COM_NAME" DataType="String" DisableOnModify="" 
         Format="" FrameOneCell="" Icon="" IconLoction="" IsEncrypt="" 
         NumberScale="" SumBottom="" TransTarget="" Translation="" 
         Trim="" Valid=""/>
  </DataItem>
  <DispEnc/>
  <DataRef/>
  <List/>
  <UserSet/>
  <CallAction/>
  <OpenFrame/>
  <Frame/>
  <UserControl/>
  <DefineFrame/>
  <PopFrame/>
  <signature/>
  <Upload/>
  <VaildEx><Set VXAction="" VXFail="" VXJs="" VXMode="" VXOk=""/></VaildEx>
  <MGAddField/>
  <AnchorParas/>
  <LinkButtonParas/>
  <DopListShow/>
  <ReportCfg/>
  <CombineFrame/>
  <AddrMapRels/>
  <ImageDefault/>
</XItem>
```

**缺失的节点** (每个 XItem):
- ❌ GroupIndex
- ❌ InitValue
- ❌ DescriptionSet (英文描述)
- ❌ XStyle
- ❌ Style
- ❌ ParentStyle
- ❌ AttributeSet
- ❌ EventSet
- ❌ IsHtml
- ❌ OrderSearch
- ❌ MaxMinLength
- ❌ MaxMinValue
- ❌ IsMustInput
- ❌ Switch
- ❌ DataItem (缺少 14 个属性)
- ❌ DispEnc
- ❌ DataRef
- ❌ List
- ❌ UserSet
- ❌ CallAction
- ❌ OpenFrame
- ❌ Frame
- ❌ UserControl
- ❌ DefineFrame
- ❌ PopFrame
- ❌ signature
- ❌ Upload
- ❌ VaildEx
- ❌ MGAddField
- ❌ AnchorParas
- ❌ LinkButtonParas
- ❌ DopListShow
- ❌ ReportCfg
- ❌ CombineFrame
- ❌ AddrMapRels
- ❌ ImageDefault

---

### 4. 字段对比

**生成的 XML 字段** (11 个):
1. CRM_COM_ID
2. CRM_COM_NAME
3. CRM_COM_SNAME
4. CRM_COM_CODE
5. CRM_COM_ADDR
6. CRM_COM_EMAIL
7. CRM_COM_TELE
8. CRM_COM_MOBILE
9. CRM_COM_CDATE
10. CRM_COM_MDATE
11. CRM_COM_STATE

**示例 XML 字段** (以 CRM_COM.Lf.M 为例):
- 示例中实际是 CRM_COM.Lf.M 用于列表展示，字段不同
- 包含 CRM_CUS_MOBILE, CRM_CUS_EMAIL, CRM_CUS_NAME 等客户字段
- 包含 CRM_COM_NAME, CITY_ID1, CRM_COM_ADDR, CRM_COM_SIZE_TAG, CRM_COM_CAL_TAG 等公司字段
- 包含 butOk 按钮

---

### 5. 缺失的顶级节点

**生成的 XML 缺失**:
- ❌ Action (ActionSet, SqlSet, JSONSet, ClassSet 等)
- ❌ Menus
- ❌ Charts
- ❌ PageInfos
- ❌ Workflows

---

## 完成度评估

| 类别 | 生成 | 示例 | 完成度 |
|-----|------|------|--------|
| 根节点属性 | 1 | 4 | 25% |
| Page 节点 | 2 | 15+ | 13% |
| XItem 节点结构 | 4 | 30+ | 13% |
| XItem 属性 | 3-4 | 15+ | 20% |
| 顶级节点 | 1 | 5 | 20% |

**总体完成度**: 约 **15-20%**

---

## 下一步工作

### 优先级 1 - 基本结构
- [ ] 添加 Page 节点的 FrameTag, SkinName, DataSource
- [ ] 添加 DescriptionSet (中英文)
- [ ] 添加 Size 配置
- [ ] 添加 Acl, Log 配置

### 优先级 2 - XItem 完善
- [ ] 添加 GroupIndex
- [ ] 添加 InitValue
- [ ] 添加完整的 DataItem 属性
- [ ] 添加 MaxMinLength, MaxMinValue
- [ ] 添加 IsMustInput
- [ ] 添加 VaildEx

### 优先级 3 - 高级功能
- [ ] 添加 Action/SqlSet
- [ ] 添加 OrderSearch
- [ ] 添加 List (下拉框数据源)
- [ ] 添加 Frame (关联其他 XML)
- [ ] 添加 AddScript, AddCss

### 优先级 4 - 其他节点
- [ ] 添加 Menus
- [ ] 添加 PageInfos
- [ ] 添加 Workflows
