# ServletApi 使用说明

## 概述

`ServletApi` 是 EWA 框架的配置管理 REST API，提供安全的配置文件访问和操作能力。

## 认证方式

### 方式一：JWT Token（推荐用于客户端）

**登录获取 Token：**
```bash
curl -X POST "http://localhost:8080/ewa/servletApi?method=login" \
  -d "login_id=admin&password=your_password"
```

**响应示例：**
```json
{
  "RST": true,
  "token": "a1b2c3d4e5f6...",
  "token_type": "Bearer",
  "expires_in": 7200,
  "login_id": "admin"
}
```

**使用 Token 调用 API:**
```bash
# 方式1: Authorization Header
curl -H "Authorization: Bearer a1b2c3d4e5f6..." \
  "http://localhost:8080/ewa/servletApi?method=getConfXml&xmlname=ewa/m"

# 方式2: X-Api-Token Header
curl -H "X-Api-Token: a1b2c3d4e5f6..." \
  "http://localhost:8080/ewa/servletApi?method=getConfXml&xmlname=ewa/m"
```

**登出（撤销 Token）:**
```bash
curl -H "X-Api-Token: a1b2c3d4e5f6..." \
  "http://localhost:8080/ewa/servletApi?method=logout"
```

---

### 方式二：HMAC 签名（推荐用于服务端）

**请求 Headers:**
```
X-Api-Key: admin
X-Api-Timestamp: 1700000000000
X-Api-Nonce: abc123random
X-Api-Signature: a1b2c3d4e5f6...
```

**签名算法:**
```
stringToSign = METHOD + "\n" + TIMESTAMP + "\n" + NONCE + "\n" + PATH + "\n" + SORTED_QUERY_PARAMS
signature = HMAC-SHA256(password, stringToSign)
```

**示例:**
```bash
# 假设配置
API_KEY="admin"
API_SECRET="your_password"
BASE_URL="http://localhost:8080/ewa/servletApi"

# 生成参数
TIMESTAMP=$(date +%s%3N)
NONCE=$(uuidgen | tr -d '-')
METHOD="GET"
PATH="/ewa/servletApi"
QUERY="method=getConfXml&xmlname=ewa/m"

# 构建签名字符串
STRING_TO_SIGN="${METHOD}\n${TIMESTAMP}\n${NONCE}\n${PATH}\n${QUERY}"

# 计算签名 (需要 openssl 或其他工具)
SIGNATURE=$(echo -n "$STRING_TO_SIGN" | openssl dgst -sha256 -hmac "$API_SECRET" | awk '{print $NF}' | sed 's/.*=//;s/ //g')

# 发送请求
curl -H "X-Api-Key: $API_KEY" \
     -H "X-Api-Timestamp: $TIMESTAMP" \
     -H "X-Api-Nonce: $NONCE" \
     -H "X-Api-Signature: $SIGNATURE" \
     "${BASE_URL}?${QUERY}"
```

---

### 方式三：简单 Token（兼容旧版）

```bash
curl -H "token: your_password" \
  "http://localhost:8080/ewa/servletApi?method=getConfXml&xmlname=ewa/m"
```

---

## API 方法

| 方法 | 参数 | 说明 | 需认证 |
|------|------|------|--------|
| `login` | `login_id`, `password` | 登录获取 Token | ❌ |
| `logout` | Token (Header) | 登出撤销 Token | ❌ |
| `help` | 无 | 获取 API 帮助信息 | ❌ |
| `getConfXml` | `xmlname`, `output` | 获取完整配置文件 (output: xml/json) | ✅ |
| `getConfItem` | `xmlname`, `itemname`, `output` | 获取指定配置项 (output: xml/json) | ✅ |
| `runConfItem` | `xmlname`, `itemname`, `new_itemname` | 复制配置项 | ✅ |
| `updateConfItem` | `xmlname`, `itemname`, `xml` | 更新配置项 | ✅ |
| `deleteConfItem` | `xmlname`, `itemname` | 删除配置项 | ✅ |
| `getTables` | `db`, `filter`, `output` | 获取数据库表列表 (output: xml/json) | ✅ |
| `getTable` | `db`, `tablename`, `output` | 获取表结构详情 (output: xml/json) | ✅ |
| `getTableData` | `db`, `tablename`, `where`, `output` | 获取表数据(最多10条, output: xml/json/csv) | ✅ |

### 输出格式参数 (output)

`getConfXml` 和 `getConfItem` 方法支持 `output` 参数：

| 值 | 说明 |
|----|------|
| `xml` | 默认，输出 XML 格式 |
| `json` | 输出 JSON 格式 |

**示例：**

```bash
# 获取 XML 格式（默认）
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=getConfXml&xmlname=ewa/m"

# 获取 JSON 格式
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=getConfXml&xmlname=ewa/m&output=json"
```

**XML 格式响应：**
```json
{
  "RST": true,
  "XML": "<?xml version=\"1.0\"?>...",
  "XMLNAME": "ewa/m",
  "OUTPUT": "xml"
}
```

**JSON 格式响应：**
```json
{
  "RST": true,
  "DATA": {
    "EasyWebTemplates": {
      "EasyWebTemplate": [...]
    }
  },
  "XMLNAME": "ewa/m",
  "OUTPUT": "json"
}
```

### 数据库表操作

#### getTables - 获取数据库表列表

**参数：**
| 参数 | 必填 | 说明 |
|------|------|------|
| `db` | ✅ | 数据库连接名称（在 ewa_conf.xml 中定义） |
| `filter` | ❌ | 表名过滤，支持 `%` 通配符 |
| `output` | ❌ | 输出格式：xml（默认）或 json |

**示例：**
```bash
# 获取所有表（XML 格式）
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=getTables&db=mydb"

# 获取所有表（JSON 格式）
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=getTables&db=mydb&output=json"

# 过滤表名（包含 user 的表）
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=getTables&db=mydb&filter=user"

# 过滤表名（使用通配符，以 user 开头的表）
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=getTables&db=mydb&filter=user%"
```

**响应示例（JSON 格式）：**
```json
{
  "RST": true,
  "DB": "mydb",
  "OUTPUT": "json",
  "COUNT": 10,
  "TABLES": [
    {"name": "users", "type": "TABLE", "schema": "mydb"},
    {"name": "user_logs", "type": "TABLE", "schema": "mydb"},
    {"name": "user_view", "type": "VIEW", "schema": "mydb"}
  ]
}
```

#### getTable - 获取表结构详情

**参数：**
| 参数 | 必填 | 说明 |
|------|------|------|
| `db` | ✅ | 数据库连接名称 |
| `tablename` | ✅ | 表名 |
| `output` | ❌ | 输出格式：xml（默认）或 json |

**示例：**
```bash
# 获取表结构（XML 格式）
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=getTable&db=mydb&tablename=users"

# 获取表结构（JSON 格式）
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=getTable&db=mydb&tablename=users&output=json"
```

**响应示例（JSON 格式）：**
```json
{
  "RST": true,
  "DB": "mydb",
  "TABLENAME": "users",
  "OUTPUT": "json",
  "DATA": {
    "root": {
      "Table": {
        "Name": "users",
        "DatabaseType": "MYSQL",
        "SchemaName": "mydb",
        "TableType": "TABLE",
        "Fields": {
          "Field": [
            {"Name": "id", "DatabaseType": "int", "IsPk": "true", ...},
            {"Name": "username", "DatabaseType": "varchar", ...}
          ]
        },
        "Pk": {...},
        "Indexes": {...}
      }
    }
  }
}
```

#### getTableData - 获取表数据（支持分页）

**参数：**
| 参数 | 必填 | 说明 |
|------|------|------|
| `db` | ✅ | 数据库连接名称 |
| `tablename` | ✅ | 表名 |
| `where` | ❌ | WHERE 条件（不含 WHERE 关键字） |
| `page` | ❌ | 页码（默认 1） |
| `pagesize` | ❌ | 每页记录数（默认 10，最大 100） |
| `pk` | ❌ | 主键字段（用于分页排序） |
| `output` | ❌ | 输出格式：json（默认）、xml 或 csv |

**示例：**
```bash
# 获取表数据（默认第1页，每页10条）
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=getTableData&db=mydb&tablename=users"

# 获取第2页数据，每页20条
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=getTableData&db=mydb&tablename=users&page=2&pagesize=20"

# 带 WHERE 条件查询
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=getTableData&db=mydb&tablename=users&where=status=1&page=1&pagesize=20"

# CSV 格式输出
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=getTableData&db=mydb&tablename=users&output=csv"
```

**响应示例（JSON 格式）：**
```json
{
  "RST": true,
  "DB": "mydb",
  "TABLENAME": "users",
  "OUTPUT": "json",
  "PAGE": 1,
  "PAGESIZE": 10,
  "COUNT": 10,
  "DATA": [
    {"id": 1, "username": "admin", "email": "admin@example.com"},
    {"id": 2, "username": "user1", "email": "user1@example.com"}
  ]
}
```

**响应示例（CSV 格式）：**
```json
{
  "RST": true,
  "DB": "mydb",
  "TABLENAME": "users",
  "OUTPUT": "csv",
  "COUNT": 5,
  "MAX_ROWS": 10,
  "CSV": "\"id\",\"username\",\"email\"\n\"1\",\"admin\",\"admin@example.com\"\n\"2\",\"user1\",\"user1@example.com\"\n"
}
```

---

## 安全特性

### HMAC 签名模式
- ✅ 时间戳验证（5分钟有效期）
- ✅ Nonce 防重放攻击
- ✅ 常量时间比较（防时序攻击）
- ✅ 请求参数签名验证

### JWT Token 模式
- ✅ Token 2小时过期
- ✅ 服务端 Token 缓存
- ✅ 支持主动撤销

---

## 响应格式

所有响应均为 JSON 格式：

**成功响应:**
```json
{
  "RST": true,
  "XML": "...",
  "XMLNAME": "ewa/m",
  "ITEMNAME": "item1"
}
```

**错误响应:**
```json
{
  "RST": false,
  "ERR": "错误信息",
  "CODE": 401
}
```

---

## HTTP 状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 认证失败 |
| 403 | 权限不足 |
| 500 | 服务器错误 |

---

## 配置要求

需要在 `ewa_conf.xml` 中配置管理员：

```xml
<ewa_confs>
    <Admins>
        <Admin LoginId="admin" Password="your_secure_password" UserName="管理员"/>
    </Admins>
</ewa_confs>
```

---

## 最佳实践

1. **生产环境推荐使用 HMAC 签名模式**
   - 更安全，防重放攻击
   - 适合服务端调用

2. **客户端应用推荐使用 JWT Token 模式**
   - 登录后获取 Token
   - Token 自动过期
   - 支持主动登出

3. **密码安全**
   - 使用强密码（建议 16 位以上）
   - 定期更换密码

4. **HTTPS**
   - 生产环境务必使用 HTTPS
   - 防止 Token 被窃听