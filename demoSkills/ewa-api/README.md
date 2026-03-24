# EWA API Skill

调用 EWA Servlet API 的技能包，用于配置管理和数据库操作。

## 文件说明

| 文件 | 说明 |
|------|------|
| `ewa-api.md` | Skill 说明文档 |
| `ewa-api.sh` | Linux/macOS 包装脚本 |
| `ewa-api.conf.example` | 配置文件模板 |
| `examples.sh` | Linux/macOS 示例脚本 |
| `examples.bat` | Windows 示例脚本 |

## 快速开始

### 1. 配置环境变量

```bash
# 复制配置模板
cp ewa-api.conf.example ewa-api.conf

# 编辑配置
vim ewa-api.conf

# 加载配置
source ewa-api.conf
```

### 2. 登录

```bash
./ewa-api.sh login
```

### 3. 调用 API

```bash
# 获取帮助
./ewa-api.sh help

# 获取数据库表列表
./ewa-api.sh getTables work "ADM_%" json

# 获取表结构
./ewa-api.sh getTable work ADM_USER json

# 获取表数据
./ewa-api.sh getTableData work ADM_USER "" json
```

## API 方法

| 方法 | 参数 | 说明 |
|------|------|------|
| `login` | `[login_id] [password]` | 登录获取 Token |
| `logout` | - | 注销 Token |
| `help` | - | 显示 API 帮助 |
| `getConfXml` | `<xmlname> [output]` | 获取配置文件 |
| `getConfItem` | `<xmlname> <itemname> [output]` | 获取配置项 |
| `runConfItem` | `<xmlname> <itemname> <new_itemname>` | 复制配置项 |
| `updateConfItem` | `<xmlname> <itemname> <xml>` | 更新配置项 |
| `deleteConfItem` | `<xmlname> <itemname>` | 删除配置项 |
| `getTables` | `<db> [filter] [output]` | 获取数据库表列表 |
| `getTable` | `<db> <tablename> [output]` | 获取表结构 |
| `getTableData` | `<db> <tablename> [where] [output]` | 获取表数据(最多10条) |

## 输出格式

- `json` - JSON 格式（默认）
- `xml` - XML 格式
- `csv` - CSV 格式（仅 getTableData）

## 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `EWA_API_URL` | API 基础地址 | `http://localhost:8080/ewa/servletApi` |
| `EWA_API_LOGIN_ID` | 登录用户名 | `admin` |
| `EWA_API_PASSWORD` | 登录密码 | - |
| `EWA_TOKEN_FILE` | Token 缓存文件 | `/tmp/.ewa_api_token` |
| `EWA_AUTH_MODE` | 认证模式 | `token` |

## 示例

查看 `examples.sh` 或 `examples.bat` 获取更多使用示例。