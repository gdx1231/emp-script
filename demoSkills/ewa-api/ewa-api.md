# EWA API Skill

调用 EWA Servlet API 的技能，用于配置管理和数据库操作。

## 配置

在使用前，请设置以下环境变量：

```bash
export EWA_API_URL="https://your-server/ewa/servletApi"
export EWA_API_LOGIN_ID="your_login_id"
export EWA_API_PASSWORD="your_password"
```

## 可用方法

| 方法 | 参数 | 说明 |
|------|------|------|
| `login` | `[login_id] [password]` | 登录获取 Token |
| `logout` | - | 注销 Token |
| `help` | - | 显示 API 帮助 |
| `getConfXml` | `<xmlname> [output]` | 获取配置文件 |
| `getConfItem` | `<xmlname> <itemname> [output]` | 获取配置项 |
| `getTables` | `<db> [filter] [output]` | 获取数据库表列表 |
| `getTable` | `<db> <tablename> [output]` | 获取表结构 |
| `getTableData` | `<db> <tablename> [where] [output]` | 获取表数据(最多10条) |

## 输出格式

- `json` - JSON 格式（默认）
- `xml` - XML 格式
- `csv` - CSV 格式（仅 getTableData）

## 示例

### 登录
```bash
./call-ewa-api.sh login admin password123
```

### 获取配置项
```bash
./call-ewa-api.sh getConfItem "/meta-data/services/ser_main.xml" "SER_MAIN_CAT.T.Modify" json
```

### 获取数据库表列表
```bash
./call-ewa-api.sh getTables work "ADM_%" json
```

### 获取表结构
```bash
./call-ewa-api.sh getTable work ADM_USER json
```

### 获取表数据
```bash
# JSON 格式
./call-ewa-api.sh getTableData work ADM_USER json

# CSV 格式
./call-ewa-api.sh getTableData work ADM_USER "" csv

# 带 WHERE 条件
./call-ewa-api.sh getTableData work ADM_USER "status=1" json
```

## 脚本位置

- Linux/macOS: `shell/call-ewa-api.sh`
- Windows: `shell/call-ewa-api.bat`