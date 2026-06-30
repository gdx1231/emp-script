# emp-script RESTful API Improvement Plan

## Current Status Analysis

### 1. Existing API Configuration

The system currently has the following API configurations from the database:

#### Main Modules:
- **Chat System** (/ewa-api/chat/v1/)
  - Chat room management (chatRooms)
  - User management (chatUsers)
  - Permission management (acls)
  - Attachment management (atts)
  - Topics/Posts (topics)
  
- **User System** (/ewa-api/chat-user/v1/)
  - User management
  - Chat room management
  - Attachment management (images, audio, video, files)
  - Permission management
  - Friend relationships
  - User avatars

### 2. API Configuration Statistics
- **Catalog Configuration (EWA_RESTFUL_CATALOG)**: 45 records
- **Endpoint Configuration (EWA_RESTFUL)**: 50 records
- **HTTP Method Distribution**:
  - GET: For querying lists and details
  - POST: For creating resources
  - PUT: For modifying resources
  - PATCH: For restoring logically deleted resources
  - DELETE: For logically deleting resources

---

## Improvement Plan

### Phase 1: User Management API (Basic Functions)

#### 1.1 Add User Management Catalog Structure

Add the following catalogs to the `EWA_RESTFUL_CATALOG` table:

```sql
-- Level 1: /ewa-api/users/v1
-- Level 2: /ewa-api/users/v1/users
-- Level 2: /ewa-api/users/v1/roles
-- Level 2: /ewa-api/users/v1/permissions
```

#### 1.2 Add User Management API Configuration

Add the following configurations to the `EWA_RESTFUL` table:

**Basic User CRUD Operations:**
- GET /ewa-api/users/v1/users - Get user list
- GET /ewa-api/users/v1/users/{adm_id} - Get single user
- POST /ewa-api/users/v1/users - Create user
- PUT /ewa-api/users/v1/users/{adm_id} - Update user
- DELETE /ewa-api/users/v1/users/{adm_id} - Delete user
- PATCH /ewa-api/users/v1/users/{adm_id}/restore - Restore user

#### 1.3 User-Related Features
- User password change
- User status management
- User search functionality
- Batch user operations

---

### Phase 2: System Configuration Management API

#### 2.1 Configuration Management Modules
- EWA_CFG (Main configuration)
- EWA_CFG_TREE (Configuration tree)
- EWA_CFG_HIS (Configuration history)
- EWA_CFG_OTH (Other configurations)
- EWA_CFG_RM (Configuration relation mapping)

#### 2.2 API Catalog Structure
```
/ewa-api/system/v1/
├── configs/          # Configuration management
├── configs/tree/     # Configuration tree
├── configs/history/  # Configuration history
└── configs/import/   # Configuration import/export
```

---

### Phase 3: Module Management API

#### 3.1 Module Management Modules
- EWA_MOD (Module definition)
- EWA_MOD_VER (Module version)
- EWA_MOD_CFGS (Module configuration)
- EWA_MOD_DATA (Module data)
- EWA_MOD_DDL (Database structure)
- EWA_MOD_FIELD (Field definitions)
- EWA_MOD_IMPORT_LOG (Import logs)
- EWA_MOD_INDEX (Index configuration)
- EWA_MOD_INDEX_FIELD (Index fields)
- EWA_MOD_PACKAGE (Module packages)
- EWA_MOD_DOWNLOAD (Download records)

#### 3.2 API Catalog Structure
```
/ewa-api/modules/v1/
├── modules/          # Module management
├── versions/         # Version management
├── fields/           # Field management
├── ddl/              # Database schema management
└── packages/         # Package management
```

---

### Phase 4: API Discovery & Documentation

#### 4.1 API Discovery Endpoints
- GET /ewa-api/help/documents - Help documentation (framework exists, needs improvement)
- GET /ewa-api/help/endpoints - All available endpoint listing
- GET /ewa-api/help/spec/{format} - API specification (OpenAPI/Swagger format)

#### 4.2 API Documentation Features
- Auto-generate API documentation
- API testing interface
- Online debugging functionality
- API usage examples

---

### Phase 5: Security & Authentication Enhancement

#### 5.1 Current Security Features
- ACL access control lists
- Session management

#### 5.2 Improvement Suggestions
- JWT Token authentication
- API Key management
- Rate limiting & anti-abuse
- Audit logging
- Sensitive data encryption

---

### Phase 6: Performance Optimization

#### 6.1 Existing Features
- Pagination support (ewa_pagesize parameter)
- Caching mechanism

#### 6.2 Improvement Suggestions
- Response compression optimization
- Batch operation endpoints
- Query cache optimization
- Async task processing

---

## Detailed Implementation Steps

### Step 1: Preparation

1. Review existing XML configuration file structure
2. Understand current EWA configuration mechanism
3. Determine user table structure (inferred as ADM_USER or EWA_ADMIN from existing code)

### Step 2: Create User Management XML Configuration

1. Create user list page (users.LF.M)
2. Create user form page (users.F.NM)
3. Create user view page (users.F.V)
4. Configure corresponding SQL operations

### Step 3: Configure RESTful Catalog and Endpoints

1. Insert catalog records into the `EWA_RESTFUL_CATALOG` table
2. Insert endpoint configuration records into the `EWA_RESTFUL` table
3. Link to correct XML configuration items

### Step 4: Testing and Verification

1. Test each API endpoint
2. Verify authorization controls work correctly
3. Verify response format matches expectations
4. Verify error handling is appropriate

---

## Example SQL (Adding User Management API)

### 1. Add Catalog Records

```sql
-- 1. Root catalog /ewa-api/users/v1
DECLARE @root_uid VARCHAR(36) = NEWID();
INSERT INTO EWA_RESTFUL_CATALOG 
    (CAT_UID, CAT_PUID, CAT_PATH, CAT_PATH_FULL, CAT_NAME, CAT_NAME_EN, CAT_MEMO, CAT_MEMO_EN, CAT_LVL, CAT_ORD, CAT_STATUS, CAT_CTIME, CAT_MTIME, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9)
VALUES 
    (@root_uid, '', '', '/ewa-api/users/v1', 'User Management', 'User Management', '', '', 1, 1, 'USED', GETDATE(), GETDATE(), 'ewa-api', 'users', 'v1', NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- 2. Users catalog /ewa-api/users/v1/users
DECLARE @users_uid VARCHAR(36) = NEWID();
INSERT INTO EWA_RESTFUL_CATALOG 
    (CAT_UID, CAT_PUID, CAT_PATH, CAT_PATH_FULL, CAT_NAME, CAT_NAME_EN, CAT_MEMO, CAT_MEMO_EN, CAT_LVL, CAT_ORD, CAT_STATUS, CAT_CTIME, CAT_MTIME, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9)
VALUES 
    (@users_uid, @root_uid, 'users', '/ewa-api/users/v1/users', 'Users', 'Users', '', '', 2, 1, 'USED', GETDATE(), GETDATE(), 'ewa-api', 'users', 'v1', 'users', NULL, NULL, NULL, NULL, NULL, NULL);

-- 3. Single user catalog /ewa-api/users/v1/users/{adm_id}
DECLARE @user_uid VARCHAR(36) = NEWID();
INSERT INTO EWA_RESTFUL_CATALOG 
    (CAT_UID, CAT_PUID, CAT_PATH, CAT_PATH_FULL, CAT_NAME, CAT_NAME_EN, CAT_MEMO, CAT_MEMO_EN, CAT_LVL, CAT_ORD, CAT_STATUS, CAT_CTIME, CAT_MTIME, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9)
VALUES 
    (@user_uid, @users_uid, '{adm_id}', '/ewa-api/users/v1/users/{adm_id}', 'User Detail', 'User Detail', '', '', 3, 1, 'USED', GETDATE(), GETDATE(), 'ewa-api', 'users', 'v1', 'users', '{adm_id}', NULL, NULL, NULL, NULL, NULL);
```

### 2. Add Endpoint Configurations

```sql
-- GET user list
INSERT INTO EWA_RESTFUL 
    (RS_UID, CAT_UID, RS_METHOD, RS_NAME, RS_NAME_EN, RS_XMLNAME, RS_ITEMNAME, RS_PARAMETERS, RS_MEMO, RS_MEMO_EN, RS_STATUS, RS_CTIME, RS_MTIME)
VALUES 
    (NEWID(), @users_uid, 'GET', 'Get User List', 'Get User List', '/ewa/ewa_admin.xml', 'EWA_ADMIN.LF.M', 'ewa_pagesize=20', '', '', 'USED', GETDATE(), GETDATE());

-- POST create user
INSERT INTO EWA_RESTFUL 
    (RS_UID, CAT_UID, RS_METHOD, RS_NAME, RS_NAME_EN, RS_XMLNAME, RS_ITEMNAME, RS_PARAMETERS, RS_MEMO, RS_MEMO_EN, RS_STATUS, RS_CTIME, RS_MTIME)
VALUES 
    (NEWID(), @users_uid, 'POST', 'Add User', 'Add User', '/ewa/ewa_admin.xml', 'EWA_ADMIN.F.NM', '', '', '', 'USED', GETDATE(), GETDATE());

-- GET single user
INSERT INTO EWA_RESTFUL 
    (RS_UID, CAT_UID, RS_METHOD, RS_NAME, RS_NAME_EN, RS_XMLNAME, RS_ITEMNAME, RS_PARAMETERS, RS_MEMO, RS_MEMO_EN, RS_STATUS, RS_CTIME, RS_MTIME)
VALUES 
    (NEWID(), @user_uid, 'GET', 'Get User Detail', 'Get User Detail', '/ewa/ewa_admin.xml', 'EWA_ADMIN.F.V', '', '', '', 'USED', GETDATE(), GETDATE());

-- PUT update user
INSERT INTO EWA_RESTFUL 
    (RS_UID, CAT_UID, RS_METHOD, RS_NAME, RS_NAME_EN, RS_XMLNAME, RS_ITEMNAME, RS_PARAMETERS, RS_MEMO, RS_MEMO_EN, RS_STATUS, RS_CTIME, RS_MTIME)
VALUES 
    (NEWID(), @user_uid, 'PUT', 'Update User', 'Update User', '/ewa/ewa_admin.xml', 'EWA_ADMIN.F.NM', '', '', '', 'USED', GETDATE(), GETDATE());

-- DELETE user
INSERT INTO EWA_RESTFUL 
    (RS_UID, CAT_UID, RS_METHOD, RS_NAME, RS_NAME_EN, RS_XMLNAME, RS_ITEMNAME, RS_PARAMETERS, RS_MEMO, RS_MEMO_EN, RS_STATUS, RS_CTIME, RS_MTIME)
VALUES 
    (NEWID(), @user_uid, 'DELETE', 'Delete User', 'Delete User', '/ewa/ewa_admin.xml', 'EWA_ADMIN.LF.M', '', '', '', 'USED', GETDATE(), GETDATE());

-- PATCH restore user
INSERT INTO EWA_RESTFUL 
    (RS_UID, CAT_UID, RS_METHOD, RS_NAME, RS_NAME_EN, RS_XMLNAME, RS_ITEMNAME, RS_PARAMETERS, RS_MEMO, RS_MEMO_EN, RS_STATUS, RS_CTIME, RS_MTIME)
VALUES 
    (NEWID(), @user_uid, 'PATCH', 'Restore User', 'Restore User', '/ewa/ewa_admin.xml', 'EWA_ADMIN.LF.M', '', '', '', 'USED', GETDATE(), GETDATE());
```

---

## API Usage Examples (After Addition)

### User Management

**Get user list:**
```http
GET /servletRestful/ewa-api/users/v1/users
```

**Create user:**
```http
POST /servletRestful/ewa-api/users/v1/users
Content-Type: application/json

{
  "adm_lid": "newuser",
  "adm_name": "New User",
  "adm_pwd": "password"
}
```

**Get single user:**
```http
GET /servletRestful/ewa-api/users/v1/users/{adm_id}
```

**Update user:**
```http
PUT /servletRestful/ewa-api/users/v1/users/{adm_id}
Content-Type: application/json

{
  "adm_name": "Updated Username"
}
```

**Delete user:**
```http
DELETE /servletRestful/ewa-api/users/v1/users/{adm_id}
```

**Restore user:**
```http
PATCH /servletRestful/ewa-api/users/v1/users/{adm_id}
```

---

## Expected Outcomes

1. **Complete User Management API** - Provides user CRUD operations
2. **System Configuration Management API** - Manage system configurations
3. **Module Management API** - Manage system modules
4. **API Discovery & Documentation** - Provide self-describing APIs
5. **Enhanced Security** - Better authentication and authorization
6. **Performance Optimization** - Better response speed and concurrency handling

---

## Notes

1. Confirm actual XML configuration filenames and item names before implementation
2. Adjust SQL operations based on actual table structure
3. Configure ACL for authorization based on actual requirements
4. Recommended to test in development environment before deploying to production
5. Preserve existing chat API integrity, do not delete existing configurations
