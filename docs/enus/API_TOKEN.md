# Servlet API Token Usage Guide

## Overview

`ServletApi` provides RESTful API interfaces for configuration management, supporting three authentication modes:

| Auth Mode | Use Case | Security Level | Recommended |
|----------|----------|----------|--------|
| HMAC Signature | Server-side calls, automation scripts | ⭐⭐⭐⭐⭐ | Recommended |
| JWT Token | Client sessions, web applications | ⭐⭐⭐⭐ | Recommended |
| Simple Token | Legacy compatibility, test environments | ⭐⭐ | Not recommended |

---

## API Methods

| Method | Parameters | Description | Auth Required |
|------|------|------|--------|
| `login` | `login_id`, `password` | Login to get a Token | ❌ |
| `logout` | `token` | Logout and revoke Token | ❌ |
| `getConfXml` | `xmlname` | Get full configuration file XML | ✅ |
| `getConfItem` | `xmlname`, `itemname` | Get a specific configuration item | ✅ |
| `runConfItem` | `xmlname`, `itemname`, `new_itemname` | Copy a configuration item | ✅ |
| `updateConfItem` | `xmlname`, `itemname`, `xml` | Update a configuration item | ✅ |
| `deleteConfItem` | `xmlname`, `itemname` | Delete a configuration item | ✅ |
| `help` | - | Get API help documentation | ✅ |

---

## 1. JWT Token Mode

### 1.1 Login to Get a Token

```bash
# Login request
curl -X POST "http://localhost:8080/ewa/servletApi?method=login" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "login_id=admin&password=your_password"
```

**Response Example:**
```json
{
  "RST": true,
  "token": "a1b2c3d4e5f67890abcdef1234567890abcdef1234567890abcdef1234567890",
  "token_type": "Bearer",
  "expires_in": 7200,
  "login_id": "admin"
}
```

### 1.2 Using Token to Call the API

**Method 1: X-Api-Token Header**
```bash
curl -H "X-Api-Token: a1b2c3d4e5f67890abcdef..." \
  "http://localhost:8080/ewa/servletApi?method=getConfXml&xmlname=ewa/m"
```

**Method 2: Authorization Bearer Header**
```bash
curl -H "Authorization: Bearer a1b2c3d4e5f67890abcdef..." \
  "http://localhost:8080/ewa/servletApi?method=getConfXml&xmlname=ewa/m"
```

### 1.3 Logout / Revoke Token

```bash
curl -X POST "http://localhost:8080/ewa/servletApi?method=logout" \
  -H "X-Api-Token: a1b2c3d4e5f67890abcdef..."
```

**Response Example:**
```json
{
  "RST": true,
  "MSG": "Logged out successfully"
}
```

---

## 2. HMAC Signature Mode

### 2.1 Signature Algorithm

```
1. Build the string to sign:
   stringToSign = METHOD + "\n" + TIMESTAMP + "\n" + NONCE + "\n" + PATH + "\n" + SORTED_QUERY_PARAMS

2. Calculate the signature:
   signature = HMAC-SHA256(password, stringToSign)

3. Convert to lowercase hex string
```

### 2.2 Request Headers

| Header | Description | Example |
|--------|------|------|
| `X-Api-Key` | Admin login ID | `admin` |
| `X-Api-Timestamp` | Current timestamp (milliseconds) | `1700000000000` |
| `X-Api-Nonce` | Random string (anti-replay) | `abc123random` |
| `X-Api-Signature` | HMAC-SHA256 signature | `a1b2c3d4...` |

### 2.3 Signature Calculation Example

Assume:
- Login ID: `admin`
- Password: `mypassword123`
- Request Method: `GET`
- Request Path: `/ewa/servletApi`
- Timestamp: `1700000000000`
- Nonce: `abc123random`
- Query Parameters: `method=getConfXml&xmlname=ewa/m`

```
stringToSign = "GET\n1700000000000\nabc123random\n/ewa/servletApi\nmethod=getConfXml&xmlname=ewa/m"
signature = HMAC-SHA256("mypassword123", stringToSign)
```

### 2.4 Complete Request Example

```bash
curl -H "X-Api-Key: admin" \
  -H "X-Api-Timestamp: 1700000000000" \
  -H "X-Api-Nonce: abc123random" \
  -H "X-Api-Signature: calculated_signature_here" \
  "http://localhost:8080/ewa/servletApi?method=getConfXml&xmlname=ewa/m"
```

---

## 3. Simple Token Mode (Not Recommended)

Only for testing or legacy compatibility; directly use the password as the token:

```bash
curl -H "token: your_password" \
  "http://localhost:8080/ewa/servletApi?method=getConfXml&xmlname=ewa/m"
```

---

## 4. API Call Examples

### 4.1 Get Configuration File

```bash
# Using Token
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=getConfXml&xmlname=ewa/m"
```

**Response:**
```json
{
  "RST": true,
  "XML": "<?xml version=\"1.0\" encoding=\"UTF-8\"?>...",
  "XMLNAME": "ewa/m"
}
```

### 4.2 Get Configuration Item

```bash
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=getConfItem&xmlname=ewa/m&itemname=ChangePWD"
```

**Response:**
```json
{
  "RST": true,
  "XML": "<EasyWebTemplate Name=\"ChangePWD\">...</EasyWebTemplate>",
  "XMLNAME": "ewa/m",
  "ITEMNAME": "ChangePWD"
}
```

### 4.3 Copy Configuration Item

```bash
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=runConfItem&xmlname=ewa/m&itemname=ChangePWD&new_itemname=ChangePWD_Copy"
```

**Response:**
```json
{
  "RST": true,
  "MSG": "Item copied successfully",
  "XMLNAME": "ewa/m",
  "SOURCE_ITEM": "ChangePWD",
  "NEW_ITEM": "ChangePWD_Copy"
}
```

### 4.4 Update Configuration Item

```bash
# Use POST method; XML content is passed via POST body
curl -X POST "http://localhost:8080/ewa/servletApi?method=updateConfItem&xmlname=ewa/m&itemname=ChangePWD" \
  -H "X-Api-Token: your_token" \
  -H "Content-Type: application/xml" \
  -d '<EasyWebTemplate Name="ChangePWD">...</EasyWebTemplate>'
```

Or using URL parameters:

```bash
curl -X POST "http://localhost:8080/ewa/servletApi?method=updateConfItem" \
  -H "X-Api-Token: your_token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "xmlname=ewa/m&itemname=ChangePWD&xml=<EasyWebTemplate Name=\"ChangePWD\">...</EasyWebTemplate>"
```

**Response:**
```json
{
  "RST": true,
  "MSG": "Item updated successfully",
  "XMLNAME": "ewa/m",
  "ITEMNAME": "ChangePWD"
}
```

### 4.5 Delete Configuration Item

```bash
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=deleteConfItem&xmlname=ewa/m&itemname=ChangePWD_Copy"
```

**Response:**
```json
{
  "RST": true,
  "MSG": "Item deleted successfully",
  "XMLNAME": "ewa/m",
  "ITEMNAME": "ChangePWD_Copy"
}
```

### 4.6 Get Help

```bash
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=help"
```

---

## 5. Error Responses

All error response format:

```json
{
  "RST": false,
  "ERR": "Error message",
  "CODE": 401
}
```

| CODE | Description |
|------|------|
| 400 | Parameter error |
| 401 | Authentication failure |
| 403 | Insufficient permissions |
| 500 | Server error |

---

## 6. Security Features

### HMAC Signature Mode Security Features

| Feature | Description |
|------|------|
| Timestamp Validation | Request timestamp valid for 5 minutes |
| Nonce Anti-Replay | Each Nonce can only be used once |
| Constant-Time Comparison | Prevents timing attacks |
| Parameter Signing | Request parameters participate in signature calculation |

### JWT Token Mode Security Features

| Feature | Description |
|------|------|
| Token Expiration | Default 2-hour expiration |
| Server-Side Caching | Token stored in server memory |
| Active Revocation | Supports Token revocation via logout |

---

## 7. Shell Script Examples

### 7.1 ewa_api.sh - HMAC Signature Call Script

```bash
#!/bin/bash
#
# EWA API Call Script (HMAC Signature Mode)
# Usage: ./ewa_api.sh <method> [params...]
# Example: ./ewa_api.sh getConfXml "xmlname=ewa/m"
#

# ==================== Configuration ====================
API_BASE_URL="http://localhost:8080/ewa/servletApi"
API_KEY="admin"           # Login ID
API_SECRET="your_password" # Password
# =================================================

# Color output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Print help
print_help() {
    echo "EWA API Call Script"
    echo ""
    echo "Usage: $0 <method> [params...]"
    echo ""
    echo "Methods:"
    echo "  login                    Login to get Token"
    echo "  getConfXml <xmlname>     Get configuration file"
    echo "  getConfItem <xmlname> <itemname>  Get configuration item"
    echo "  runConfItem <xmlname> <itemname> <newname>  Copy configuration item"
    echo "  deleteConfItem <xmlname> <itemname>  Delete configuration item"
    echo "  help                     Show API help"
    echo ""
    echo "Examples:"
    echo "  $0 login"
    echo "  $0 getConfXml ewa/m"
    echo "  $0 getConfItem ewa/m ChangePWD"
    echo "  $0 runConfItem ewa/m ChangePWD ChangePWD_Copy"
    echo "  $0 deleteConfItem ewa/m ChangePWD_Copy"
}

# Calculate HMAC-SHA256
hmac_sha256() {
    local key="$1"
    local data="$2"
    echo -n "$data" | openssl dgst -sha256 -hmac "$key" | awk '{print $2}'
}

# Generate random Nonce
generate_nonce() {
    cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 32 | head -n 1
}

# Get current timestamp (milliseconds)
get_timestamp() {
    # macOS and Linux compatible
    if [[ "$OSTYPE" == "darwin"* ]]; then
        echo $(($(date +%s) * 1000))
    else
        echo $(date +%s%3N)
    fi
}

# Build string to sign
build_string_to_sign() {
    local method="$1"
    local timestamp="$2"
    local nonce="$3"
    local path="$4"
    local params="$5"
    
    echo -e "${method}\n${timestamp}\n${nonce}\n${path}\n${params}"
}

# Send HMAC signed request
send_hmac_request() {
    local http_method="GET"
    local query_params="$1"
    
    local timestamp=$(get_timestamp)
    local nonce=$(generate_nonce)
    local path="/ewa/servletApi"
    
    # Build string to sign
    local string_to_sign=$(build_string_to_sign "$http_method" "$timestamp" "$nonce" "$path" "$query_params")
    
    # Calculate signature
    local signature=$(hmac_sha256 "$API_SECRET" "$string_to_sign")
    
    # Send request
    local url="${API_BASE_URL}?${query_params}"
    
    echo -e "${YELLOW}Request URL:${NC} $url"
    echo -e "${YELLOW}Timestamp:${NC} $timestamp"
    echo -e "${YELLOW}Nonce:${NC} $nonce"
    echo -e "${YELLOW}Signature:${NC} $signature"
    echo ""
    
    curl -s -H "X-Api-Key: $API_KEY" \
         -H "X-Api-Timestamp: $timestamp" \
         -H "X-Api-Nonce: $nonce" \
         -H "X-Api-Signature: $signature" \
         "$url" | python3 -m json.tool 2>/dev/null || cat
}

# Login to get Token
do_login() {
    echo -e "${GREEN}Logging in to get Token...${NC}"
    
    local url="${API_BASE_URL}?method=login"
    
    curl -s -X POST "$url" \
         -H "Content-Type: application/x-www-form-urlencoded" \
         -d "login_id=${API_KEY}&password=${API_SECRET}" | python3 -m json.tool 2>/dev/null || cat
}

# Get configuration file
do_get_conf_xml() {
    local xmlname="$1"
    
    if [ -z "$xmlname" ]; then
        echo -e "${RED}Error: Missing xmlname parameter${NC}"
        echo "Usage: $0 getConfXml <xmlname>"
        exit 1
    fi
    
    echo -e "${GREEN}Getting configuration file: $xmlname${NC}"
    send_hmac_request "method=getConfXml&xmlname=${xmlname}"
}

# Get configuration item
do_get_conf_item() {
    local xmlname="$1"
    local itemname="$2"
    
    if [ -z "$xmlname" ] || [ -z "$itemname" ]; then
        echo -e "${RED}Error: Missing parameters${NC}"
        echo "Usage: $0 getConfItem <xmlname> <itemname>"
        exit 1
    fi
    
    echo -e "${GREEN}Getting configuration item: $xmlname / $itemname${NC}"
    send_hmac_request "method=getConfItem&xmlname=${xmlname}&itemname=${itemname}"
}

# Copy configuration item
do_run_conf_item() {
    local xmlname="$1"
    local itemname="$2"
    local newname="$3"
    
    if [ -z "$xmlname" ] || [ -z "$itemname" ] || [ -z "$newname" ]; then
        echo -e "${RED}Error: Missing parameters${NC}"
        echo "Usage: $0 runConfItem <xmlname> <itemname> <new_itemname>"
        exit 1
    fi
    
    echo -e "${GREEN}Copying configuration item: $itemname -> $newname${NC}"
    send_hmac_request "method=runConfItem&xmlname=${xmlname}&itemname=${itemname}&new_itemname=${newname}"
}

# Delete configuration item
do_delete_conf_item() {
    local xmlname="$1"
    local itemname="$2"
    
    if [ -z "$xmlname" ] || [ -z "$itemname" ]; then
        echo -e "${RED}Error: Missing parameters${NC}"
        echo "Usage: $0 deleteConfItem <xmlname> <itemname>"
        exit 1
    fi
    
    echo -e "${GREEN}Deleting configuration item: $xmlname / $itemname${NC}"
    send_hmac_request "method=deleteConfItem&xmlname=${xmlname}&itemname=${itemname}"
}

# Get help
do_help() {
    echo -e "${GREEN}Getting API help...${NC}"
    send_hmac_request "method=help"
}

# Main
main() {
    if [ $# -eq 0 ]; then
        print_help
        exit 0
    fi
    
    local command="$1"
    shift
    
    case "$command" in
        login)
            do_login "$@"
            ;;
        getConfXml)
            do_get_conf_xml "$@"
            ;;
        getConfItem)
            do_get_conf_item "$@"
            ;;
        runConfItem)
            do_run_conf_item "$@"
            ;;
        deleteConfItem)
            do_delete_conf_item "$@"
            ;;
        help)
            do_help
            ;;
        *)
            echo -e "${RED}Unknown command: $command${NC}"
            print_help
            exit 1
            ;;
    esac
}

main "$@"
```

### 7.2 ewa_api_token.sh - Token Mode Call Script

```bash
#!/bin/bash
#
# EWA API Call Script (Token Mode)
# Usage: ./ewa_api_token.sh <method> [params...]
#

# ==================== Configuration ====================
API_BASE_URL="http://localhost:8080/ewa/servletApi"
API_LOGIN_ID="admin"
API_PASSWORD="your_password"
TOKEN_FILE="/tmp/.ewa_api_token"
# =================================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Login and save Token
login() {
    echo -e "${GREEN}Logging in...${NC}"
    
    local response=$(curl -s -X POST "${API_BASE_URL}?method=login" \
         -H "Content-Type: application/x-www-form-urlencoded" \
         -d "login_id=${API_LOGIN_ID}&password=${API_PASSWORD}")
    
    local rst=$(echo "$response" | grep -o '"RST":[^,}]*' | cut -d':' -f2)
    
    if [ "$rst" = "true" ]; then
        local token=$(echo "$response" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
        echo "$token" > "$TOKEN_FILE"
        echo -e "${GREEN}Login successful!${NC}"
        echo "Token: $token"
        echo "Token saved to: $TOKEN_FILE"
    else
        echo -e "${RED}Login failed!${NC}"
        echo "$response"
    fi
}

# Logout
logout() {
    if [ -f "$TOKEN_FILE" ]; then
        local token=$(cat "$TOKEN_FILE")
        curl -s -X POST "${API_BASE_URL}?method=logout" \
             -H "X-Api-Token: $token"
        rm -f "$TOKEN_FILE"
        echo -e "${GREEN}Logged out${NC}"
    else
        echo -e "${YELLOW}Token file not found${NC}"
    fi
}

# Get Token
get_token() {
    if [ ! -f "$TOKEN_FILE" ]; then
        echo -e "${YELLOW}Token does not exist, logging in...${NC}"
        login
    fi
    
    cat "$TOKEN_FILE"
}

# Send request
send_request() {
    local params="$1"
    local token=$(get_token)
    
    curl -s -H "X-Api-Token: $token" \
         "${API_BASE_URL}?${params}" | python3 -m json.tool 2>/dev/null || cat
}

# Main
case "$1" in
    login)
        login
        ;;
    logout)
        logout
        ;;
    getConfXml)
        send_request "method=getConfXml&xmlname=$2"
        ;;
    getConfItem)
        send_request "method=getConfItem&xmlname=$2&itemname=$3"
        ;;
    runConfItem)
        send_request "method=runConfItem&xmlname=$2&itemname=$3&new_itemname=$4"
        ;;
    deleteConfItem)
        send_request "method=deleteConfItem&xmlname=$2&itemname=$3"
        ;;
    help)
        send_request "method=help"
        ;;
    *)
        echo "Usage: $0 {login|logout|getConfXml|getConfItem|runConfItem|deleteConfItem|help} [params...]"
        echo ""
        echo "Examples:"
        echo "  $0 login"
        echo "  $0 getConfXml ewa/m"
        echo "  $0 getConfItem ewa/m ChangePWD"
        echo "  $0 runConfItem ewa/m ChangePWD ChangePWD_Copy"
        echo "  $0 deleteConfItem ewa/m ChangePWD_Copy"
        echo "  $0 logout"
        ;;
esac
```

---

## 8. Windows BAT Script Examples

### 8.1 ewa_api.bat - HMAC Signature Call Script

```batch
@echo off
REM EWA API Call Script (HMAC Signature Mode)
REM Requires OpenSSL and curl

setlocal enabledelayedexpansion

REM ==================== Configuration ====================
set API_BASE_URL=http://localhost:8080/ewa/servletApi
set API_KEY=admin
set API_SECRET=your_password
REM =================================================

if "%1"=="" goto :help
if "%1"=="help" goto :help
if "%1"=="login" goto :login
if "%1"=="getConfXml" goto :getConfXml
if "%1"=="getConfItem" goto :getConfItem
if "%1"=="runConfItem" goto :runConfItem
if "%1"=="deleteConfItem" goto :deleteConfItem
goto :help

:help
echo EWA API Call Script
echo.
echo Usage: %0 ^<method^> [params...]
echo.
echo Methods:
echo   login                    Login to get Token
echo   getConfXml ^<xmlname^>     Get configuration file
echo   getConfItem ^<xmlname^> ^<itemname^>  Get configuration item
echo   runConfItem ^<xmlname^> ^<itemname^> ^<newname^>  Copy configuration item
echo   deleteConfItem ^<xmlname^> ^<itemname^>  Delete configuration item
echo   help                     Show help
echo.
echo Examples:
echo   %0 login
echo   %0 getConfXml ewa/m
echo   %0 getConfItem ewa/m ChangePWD
goto :eof

:login
echo Logging in to get Token...
curl -s -X POST "%API_BASE_URL%?method=login" -H "Content-Type: application/x-www-form-urlencoded" -d "login_id=%API_KEY%&password=%API_SECRET%"
echo.
goto :eof

:getConfXml
if "%2"=="" (
    echo Error: Missing xmlname parameter
    goto :eof
)
echo Getting configuration file: %2
call :send_hmac_request "method=getConfXml&xmlname=%2"
goto :eof

:getConfItem
if "%2"=="" (
    echo Error: Missing xmlname parameter
    goto :eof
)
if "%3"=="" (
    echo Error: Missing itemname parameter
    goto :eof
)
echo Getting configuration item: %2 / %3
call :send_hmac_request "method=getConfItem&xmlname=%2&itemname=%3"
goto :eof

:runConfItem
if "%2"=="" (
    echo Error: Missing xmlname parameter
    goto :eof
)
if "%3"=="" (
    echo Error: Missing itemname parameter
    goto :eof
)
if "%4"=="" (
    echo Error: Missing new_itemname parameter
    goto :eof
)
echo Copying configuration item: %3 -^> %4
call :send_hmac_request "method=runConfItem&xmlname=%2&itemname=%3&new_itemname=%4"
goto :eof

:deleteConfItem
if "%2"=="" (
    echo Error: Missing xmlname parameter
    goto :eof
)
if "%3"=="" (
    echo Error: Missing itemname parameter
    goto :eof
)
echo Deleting configuration item: %2 / %3
call :send_hmac_request "method=deleteConfItem&xmlname=%2&itemname=%3"
goto :eof

REM Send HMAC signed request
:send_hmac_request
set params=%~1

REM Get timestamp (Windows does not have milliseconds, use seconds*1000)
for /f %%i in ('powershell -command "[int64](Get-Date -UFormat %%s) * 1000"') do set timestamp=%%i

REM Generate random Nonce
for /f %%i in ('powershell -command "[guid]::NewGuid().ToString('N')"') do set nonce=%%i

REM Build string to sign
set string_to_sign=GET\n%timestamp%\n%nonce%\n/ewa/servletApi\n%params%

REM Calculate HMAC-SHA256 signature (using PowerShell)
for /f %%i in ('powershell -command "$key='%API_SECRET%'; $data='GET`n%timestamp%`n%nonce%`n/ewa/servletApi`n%params%'; $hmac = [System.Security.Cryptography.HMACSHA256]::new([System.Text.Encoding]::UTF8.GetBytes($key)); [BitConverter]::ToString($hmac.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($data))).Replace('-','').ToLower()"') do set signature=%%i

echo Timestamp: %timestamp%
echo Nonce: %nonce%
echo Signature: %signature%

curl -s -H "X-Api-Key: %API_KEY%" -H "X-Api-Timestamp: %timestamp%" -H "X-Api-Nonce: %nonce%" -H "X-Api-Signature: %signature%" "%API_BASE_URL%?%params%"
echo.
goto :eof
```

### 8.2 ewa_api_token.bat - Token Mode Call Script

```batch
@echo off
REM EWA API Call Script (Token Mode)

setlocal enabledelayedexpansion

REM ==================== Configuration ====================
set API_BASE_URL=http://localhost:8080/ewa/servletApi
set API_LOGIN_ID=admin
set API_PASSWORD=your_password
set TOKEN_FILE=%TEMP%\ewa_api_token.txt
REM =================================================

if "%1"=="" goto :help
if "%1"=="help" goto :help
if "%1"=="login" goto :login
if "%1"=="logout" goto :logout
if "%1"=="getConfXml" goto :getConfXml
if "%1"=="getConfItem" goto :getConfItem
if "%1"=="runConfItem" goto :runConfItem
if "%1"=="deleteConfItem" goto :deleteConfItem
goto :help

:help
echo EWA API Call Script (Token Mode)
echo.
echo Usage: %0 ^<method^> [params...]
echo.
echo Methods:
echo   login                    Login to get Token
echo   logout                   Revoke Token
echo   getConfXml ^<xmlname^>     Get configuration file
echo   getConfItem ^<xmlname^> ^<itemname^>  Get configuration item
echo   runConfItem ^<xmlname^> ^<itemname^> ^<newname^>  Copy configuration item
echo   deleteConfItem ^<xmlname^> ^<itemname^>  Delete configuration item
goto :eof

:login
echo Logging in...
curl -s -X POST "%API_BASE_URL%?method=login" -H "Content-Type: application/x-www-form-urlencoded" -d "login_id=%API_LOGIN_ID%&password=%API_PASSWORD%" > %TEMP%\login_response.json

REM Parse Token (using PowerShell)
for /f %%i in ('powershell -command "(Get-Content %TEMP%\login_response.json | ConvertFrom-Json).token"') do set token=%%i

if defined token (
    echo %token% > %TOKEN_FILE%
    echo Login successful!
    echo Token: %token%
    echo Token saved to: %TOKEN_FILE%
) else (
    echo Login failed!
    type %TEMP%\login_response.json
)
goto :eof

:logout
if exist %TOKEN_FILE% (
    set /p token=<%TOKEN_FILE%
    curl -s -X POST "%API_BASE_URL%?method=logout" -H "X-Api-Token: !token!"
    del %TOKEN_FILE%
    echo Logged out
) else (
    echo Token file not found
)
goto :eof

:get_token
if not exist %TOKEN_FILE% (
    echo Token does not exist, logging in...
    call :login
)
set /p TOKEN=<%TOKEN_FILE%
goto :eof

:getConfXml
call :get_token
if "%2"=="" (
    echo Error: Missing xmlname parameter
    goto :eof
)
curl -s -H "X-Api-Token: %TOKEN%" "%API_BASE_URL%?method=getConfXml&xmlname=%2"
echo.
goto :eof

:getConfItem
call :get_token
if "%2"=="" (
    echo Error: Missing xmlname parameter
    goto :eof
)
if "%3"=="" (
    echo Error: Missing itemname parameter
    goto :eof
)
curl -s -H "X-Api-Token: %TOKEN%" "%API_BASE_URL%?method=getConfItem&xmlname=%2&itemname=%3"
echo.
goto :eof

:runConfItem
call :get_token
if "%2"=="" (
    echo Error: Missing xmlname parameter
    goto :eof
)
if "%3"=="" (
    echo Error: Missing itemname parameter
    goto :eof
)
if "%4"=="" (
    echo Error: Missing new_itemname parameter
    goto :eof
)
curl -s -H "X-Api-Token: %TOKEN%" "%API_BASE_URL%?method=runConfItem&xmlname=%2&itemname=%3&new_itemname=%4"
echo.
goto :eof

:deleteConfItem
call :get_token
if "%2"=="" (
    echo Error: Missing xmlname parameter
    goto :eof
)
if "%3"=="" (
    echo Error: Missing itemname parameter
    goto :eof
)
curl -s -H "X-Api-Token: %TOKEN%" "%API_BASE_URL%?method=deleteConfItem&xmlname=%2&itemname=%3"
echo.
goto :eof
```

---

## 9. Web.xml Configuration

Add Servlet mapping in `web.xml`:

```xml
<servlet>
    <servlet-name>ServletApi</servlet-name>
    <servlet-class>com.gdxsoft.easyweb.define.servlets.ServletApi</servlet-class>
</servlet>
<servlet-mapping>
    <servlet-name>ServletApi</servlet-name>
    <url-pattern>/servletApi</url-pattern>
</servlet-mapping>
```

---

## 10. Notes

1. **Use HTTPS in production** to prevent Token and signature eavesdropping
2. **Rotate passwords regularly**; the Admin password in ewa_conf.xml should be changed periodically
3. **Restrict IP access**; you can limit API access IP at the reverse proxy layer
4. **Audit logging**; log all API calls for auditing purposes
5. **Token expiration**; default is 2 hours, adjustable based on requirements

---

## 11. FAQ

### Q1: What if the Token expires?
A: Tokens expire after 2 hours by default. After expiration, you need to call the `login` method again to get a new Token.

### Q2: HMAC signature verification failed?
A: Check the following:
- Whether the timestamp is within 5 minutes
- Whether the Nonce has been reused
- Whether the signature algorithm is correct (HMAC-SHA256)
- Whether parameter ordering is correct

### Q3: How to debug the signature?
A: Call `method=help` to see the signature algorithm description and compare the signing string on the server and client sides.

---

**Document Version**: v1.0  
**Last Updated**: 2024
