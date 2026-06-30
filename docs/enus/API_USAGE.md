# ServletApi Usage Guide

## Overview

`ServletApi` is the configuration management REST API of the EWA framework, providing secure access and manipulation of configuration files.

## Authentication Methods

### Method 1: JWT Token (Recommended for Clients)

**Login to obtain a Token:**
```bash
curl -X POST "http://localhost:8080/ewa/servletApi?method=login" \
  -d "login_id=admin&password=your_password"
```

**Response Example:**
```json
{
  "RST": true,
  "token": "a1b2c3d4e5f6...",
  "token_type": "Bearer",
  "expires_in": 7200,
  "login_id": "admin"
}
```

**Using Token to call API:**
```bash
# Method 1: Authorization Header
curl -H "Authorization: Bearer a1b2c3d4e5f6..." \
  "http://localhost:8080/ewa/servletApi?method=getConfXml&xmlname=ewa/m"

# Method 2: X-Api-Token Header
curl -H "X-Api-Token: a1b2c3d4e5f6..." \
  "http://localhost:8080/ewa/servletApi?method=getConfXml&xmlname=ewa/m"
```

**Logout (Revoke Token):**
```bash
curl -H "X-Api-Token: a1b2c3d4e5f6..." \
  "http://localhost:8080/ewa/servletApi?method=logout"
```

---

### Method 2: HMAC Signature (Recommended for Server-side)

**Request Headers:**
```
X-Api-Key: admin
X-Api-Timestamp: 1700000000000
X-Api-Nonce: abc123random
X-Api-Signature: a1b2c3d4e5f6...
```

**Signature Algorithm:**
```
stringToSign = METHOD + "\n" + TIMESTAMP + "\n" + NONCE + "\n" + PATH + "\n" + SORTED_QUERY_PARAMS
signature = HMAC-SHA256(password, stringToSign)
```

**Example:**
```bash
# Assume configuration
API_KEY="admin"
API_SECRET="your_password"
BASE_URL="http://localhost:8080/ewa/servletApi"

# Generate parameters
TIMESTAMP=$(date +%s%3N)
NONCE=$(uuidgen | tr -d '-')
METHOD="GET"
PATH="/ewa/servletApi"
QUERY="method=getConfXml&xmlname=ewa/m"

# Build signature string
STRING_TO_SIGN="${METHOD}\n${TIMESTAMP}\n${NONCE}\n${PATH}\n${QUERY}"

# Compute signature (requires openssl or other tools)
SIGNATURE=$(echo -n "$STRING_TO_SIGN" | openssl dgst -sha256 -hmac "$API_SECRET" | awk '{print $NF}' | sed 's/.*=//;s/ //g')

# Send request
curl -H "X-Api-Key: $API_KEY" \
     -H "X-Api-Timestamp: $TIMESTAMP" \
     -H "X-Api-Nonce: $NONCE" \
     -H "X-Api-Signature: $SIGNATURE" \
     "${BASE_URL}?${QUERY}"
```

---

### Method 3: Simple Token (Legacy Compatibility)

```bash
curl -H "token: your_password" \
  "http://localhost:8080/ewa/servletApi?method=getConfXml&xmlname=ewa/m"
```

---

## API Methods

| Method | Parameters | Description | Auth Required |
|------|------|------|--------|
| `login` | `login_id`, `password` | Login to obtain a Token | No |
| `logout` | Token (Header) | Logout to revoke Token | No |
| `help` | None | Get API help information | No |
| `getConfXml` | `xmlname`, `output` | Get full configuration file (output: xml/json) | Yes |
| `getConfItem` | `xmlname`, `itemname`, `output` | Get a specific configuration item (output: xml/json) | Yes |
| `runConfItem` | `xmlname`, `itemname`, `new_itemname` | Copy configuration item | Yes |
| `updateConfItem` | `xmlname`, `itemname`, `xml` | Update configuration item | Yes |
| `deleteConfItem` | `xmlname`, `itemname` | Delete configuration item | Yes |
| `getTables` | `db`, `filter`, `output` | Get database table list (output: xml/json) | Yes |
| `getTable` | `db`, `tablename`, `output` | Get table structure details (output: xml/json) | Yes |
| `getTableData` | `db`, `tablename`, `where`, `output` | Get table data (max 10 rows, output: xml/json/csv) | Yes |

### Output Format Parameter (output)

The `getConfXml` and `getConfItem` methods support the `output` parameter:

| Value | Description |
|----|------|
| `xml` | Default, output XML format |
| `json` | Output JSON format |

**Example:**

```bash
# Get XML format (default)
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=getConfXml&xmlname=ewa/m"

# Get JSON format
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=getConfXml&xmlname=ewa/m&output=json"
```

**XML Format Response:**
```json
{
  "RST": true,
  "XML": "<?xml version=\"1.0\"?>...",
  "XMLNAME": "ewa/m",
  "OUTPUT": "xml"
}
```

**JSON Format Response:**
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

### Database Table Operations

#### getTables - Get Database Table List

**Parameters:**
| Parameter | Required | Description |
|------|------|------|
| `db` | Yes | Database connection name (defined in ewa_conf.xml) |
| `filter` | No | Table name filter, supports `%` wildcard |
| `output` | No | Output format: xml (default) or json |

**Example:**
```bash
# Get all tables (XML format)
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=getTables&db=mydb"

# Get all tables (JSON format)
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=getTables&db=mydb&output=json"

# Filter table names (tables containing "user")
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=getTables&db=mydb&filter=user"

# Filter table names (using wildcard, tables starting with "user")
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=getTables&db=mydb&filter=user%"
```

**Response Example (JSON format):**
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

#### getTable - Get Table Structure Details

**Parameters:**
| Parameter | Required | Description |
|------|------|------|
| `db` | Yes | Database connection name |
| `tablename` | Yes | Table name |
| `output` | No | Output format: xml (default) or json |

**Example:**
```bash
# Get table structure (XML format)
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=getTable&db=mydb&tablename=users"

# Get table structure (JSON format)
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=getTable&db=mydb&tablename=users&output=json"
```

**Response Example (JSON format):**
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

#### getTableData - Get Table Data (Supports Pagination)

**Parameters:**
| Parameter | Required | Description |
|------|------|------|
| `db` | Yes | Database connection name |
| `tablename` | Yes | Table name |
| `where` | No | WHERE condition (without the WHERE keyword) |
| `page` | No | Page number (default 1) |
| `pagesize` | No | Records per page (default 10, max 100) |
| `pk` | No | Primary key field (for pagination sorting) |
| `output` | No | Output format: json (default), xml, or csv |

**Example:**
```bash
# Get table data (default page 1, 10 per page)
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=getTableData&db=mydb&tablename=users"

# Get page 2 data, 20 per page
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=getTableData&db=mydb&tablename=users&page=2&pagesize=20"

# Query with WHERE condition
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=getTableData&db=mydb&tablename=users&where=status=1&page=1&pagesize=20"

# CSV format output
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=getTableData&db=mydb&tablename=users&output=csv"
```

**Response Example (JSON format):**
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

**Response Example (CSV format):**
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

## Security Features

### HMAC Signature Mode
- Time-stamp validation (5-minute validity window)
- Nonce anti-replay protection
- Constant-time comparison (anti-timing attack)
- Request parameter signature verification

### JWT Token Mode
- Token expires in 2 hours
- Server-side Token caching
- Supports active revocation

---

## Response Format

All responses are in JSON format:

**Success Response:**
```json
{
  "RST": true,
  "XML": "...",
  "XMLNAME": "ewa/m",
  "ITEMNAME": "item1"
}
```

**Error Response:**
```json
{
  "RST": false,
  "ERR": "Error message",
  "CODE": 401
}
```

---

## HTTP Status Codes

| Status Code | Description |
|--------|------|
| 200 | Success |
| 400 | Bad parameter |
| 401 | Authentication failed |
| 403 | Insufficient permissions |
| 500 | Server error |

---

## Configuration Requirements

Administrator must be configured in `ewa_conf.xml`:

```xml
<ewa_confs>
    <Admins>
        <Admin LoginId="admin" Password="your_secure_password" UserName="Administrator"/>
    </Admins>
</ewa_confs>
```

---

## Best Practices

1. **HMAC signature mode is recommended for production environments**
   - More secure, anti-replay protection
   - Suitable for server-side calls

2. **JWT Token mode is recommended for client applications**
   - Obtain Token after login
   - Token auto-expires
   - Supports active logout

3. **Password security**
   - Use strong passwords (16+ characters recommended)
   - Rotate passwords regularly

4. **HTTPS**
   - Always use HTTPS in production environments
   - Prevent Token eavesdropping
