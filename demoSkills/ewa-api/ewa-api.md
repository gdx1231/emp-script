# EWA API 参考文档

调用 EWA Servlet API 的完整方法参考。

- **API 端点**: `https://gdx/cm/EWA_DEFINE/cgi-bin/api`

## 配置

复制并填写配置文件后 `source` 加载：

```bash
cp ewa-api.conf.example ewa-api.conf
# 编辑 ewa-api.conf，填写 EWA_API_URL / EWA_API_LOGIN_ID / EWA_API_PASSWORD
source ewa-api.conf
```

## 全部方法

| 方法 | 参数 | 说明 |
|------|------|------|
| `login` | `[login_id] [password]` | 登录获取 Token |
| `logout` | — | 注销 Token |
| `help` | — | 显示 API 帮助（无需认证） |
| `getConfXml` | `<xmlname> [output]` | 获取完整配置文件 |
| `getConfItem` | `<xmlname> <itemname> [output]` | 获取单个配置项 |
| `runConfItem` | `<xmlname> <itemname> <new_itemname>` | 复制配置项 |
| `updateConfItem` | `<xmlname> <itemname> <xml>` | 更新配置项 |
| `deleteConfItem` | `<xmlname> <itemname>` | 删除配置项 |
| `getTables` | `<db> [filter] [output]` | 获取数据库表列表 |
| `getTable` | `<db> <tablename> [output]` | 获取表结构详情 |
| `getTableData` | `<db> <tablename> [where] [output] [page] [pagesize]` | 获取表数据（分页） |
| `previewBusinessXml` | `<db> <tablename> <frametype> <operationtype> <xmlname> [output]` | 预览业务 XML（不保存） |
| `createBusinessXml` | `<db> <tablename> <frametype> <operationtype> <xmlname> <itemname> [admid]` | 生成并保存业务 XML |

### 参数说明

| 参数 | 说明 |
|------|------|
| `db` | 数据库连接名（定义于 `ewa_conf.xml`，如 `globalTravel`、`gyap`） |
| `xmlname` | 配置文件名，如 `ewa/m`、`ewa/studyabroad` |
| `itemname` | 配置项名，如 `ESL_SA_MAIN.F.NM` |
| `output` | 输出格式：`xml`（默认）\| `json` \| `csv`（仅 getTableData） |
| `filter` | 表名过滤，支持 `%` 通配符，如 `ESL_SA_%` |
| `frametype` | 业务 XML 框架类型：`ListFrame` \| `Frame` \| `Tree` |
| `operationtype` | 操作类型：`N`（新增）\| `M`（修改）\| `V`（查看）\| `NM`（新增+修改） |
| `page` | 页码（默认 1） |
| `pagesize` | 每页记录数（默认 10，最大 100） |
| `pk` | 主键字段（用于 getTableData 分页） |
| `admid` | 管理员 ID（createBusinessXml 可选，默认使用当前登录用户） |

## 示例

### 登录
```bash
./ewa-api.sh login
./ewa-api.sh login admin mypassword
```

### 数据库探索
```bash
# 列出 study-abroad 相关表
./ewa-api.sh getTables globalTravel "ESL_SA_%" json

# 查看表结构
./ewa-api.sh getTable globalTravel ESL_SA_MAIN json
./ewa-api.sh getTable globalTravel ESL_SA_MAJOR json

# 查看表数据（前10条）
./ewa-api.sh getTableData globalTravel ESL_SA_MAIN "" json

# 带 WHERE 条件 + 分页
./ewa-api.sh getTableData globalTravel ESL_SA_MAIN "ESL_SA_COUNTRY='CN'" json 1 20
```

### 业务 XML 生成
```bash
# 预览学校主表列表 XML（不保存，用于确认生成结果）
./ewa-api.sh previewBusinessXml globalTravel ESL_SA_MAIN ListFrame V ewa/studyabroad json

# 生成并保存学校主表表单 XML（新增+修改）
./ewa-api.sh createBusinessXml globalTravel ESL_SA_MAIN Frame NM ewa/studyabroad ESL_SA_MAIN.F.NM

# 生成专业表列表 XML
./ewa-api.sh createBusinessXml globalTravel ESL_SA_MAJOR ListFrame V ewa/studyabroad ESL_SA_MAJOR.LF.V
```

### 配置项操作
```bash
# 读取配置项
./ewa-api.sh getConfItem "ewa/studyabroad" "ESL_SA_MAIN.F.NM" json

# 复制配置项
./ewa-api.sh runConfItem "ewa/studyabroad" "ESL_SA_MAIN.F.NM" "ESL_SA_MAIN.F.V"

# 删除配置项
./ewa-api.sh deleteConfItem "ewa/studyabroad" "ESL_SA_MAIN.F.NM"
```

## 脚本位置

- Linux/macOS: `ewa-api.sh` → `shell/call-ewa-api.sh`
- Windows: `shell/call-ewa-api.bat`