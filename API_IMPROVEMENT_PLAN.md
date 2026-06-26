# emp-script RESTful API 改进计划

## 当前状态分析

### 1. 已有的 API 配置

从数据库中可以看到，系统当前已有以下 API 配置：

#### 主要模块：
- **聊天系统** (/ewa-api/chat/v1/)
  - 聊天室管理 (chatRooms)
  - 用户管理 (chatUsers)
  - 权限管理 (acls)
  - 附件管理 (atts)
  - 主题/帖子 (topics)
  
- **用户系统** (/ewa-api/chat-user/v1/)
  - 用户管理
  - 聊天室管理
  - 附件管理（图片、音频、视频、文件）
  - 权限管理
  - 好友关系
  - 用户头像

### 2. API 配置统计
- **目录配置 (EWA_RESTFUL_CATALOG)**: 45 条记录
- **接口配置 (EWA_RESTFUL)**: 50 条记录
- **HTTP 方法分布**:
  - GET: 用于查询列表和详情
  - POST: 用于新增资源
  - PUT: 用于修改资源
  - PATCH: 用于恢复逻辑删除的资源
  - DELETE: 用于逻辑删除资源

---

## 改进计划

### 阶段一：用户管理 API（基础功能）

#### 1.1 添加用户管理目录结构

需要在 `EWA_RESTFUL_CATALOG` 表中添加以下目录：

```sql
-- 第一级：/ewa-api/users/v1
-- 第二级：/ewa-api/users/v1/users
-- 第二级：/ewa-api/users/v1/roles
-- 第二级：/ewa-api/users/v1/permissions
```

#### 1.2 添加用户管理 API 配置

需要在 `EWA_RESTFUL` 表中添加以下配置：

**用户基本 CRUD 操作：**
- GET /ewa-api/users/v1/users - 获取用户列表
- GET /ewa-api/users/v1/users/{adm_id} - 获取单个用户
- POST /ewa-api/users/v1/users - 新增用户
- PUT /ewa-api/users/v1/users/{adm_id} - 修改用户
- DELETE /ewa-api/users/v1/users/{adm_id} - 删除用户
- PATCH /ewa-api/users/v1/users/{adm_id}/restore - 恢复用户

#### 1.3 用户相关功能
- 用户密码修改
- 用户状态管理
- 用户搜索功能
- 批量操作用户

---

### 阶段二：系统配置管理 API

#### 2.1 配置管理模块
- EWA_CFG（主配置）
- EWA_CFG_TREE（配置树）
- EWA_CFG_HIS（配置历史）
- EWA_CFG_OTH（其他配置）
- EWA_CFG_RM（配置关系映射）

#### 2.2 API 目录结构
```
/ewa-api/system/v1/
├── configs/          # 配置管理
├── configs/tree/     # 配置树
├── configs/history/  # 配置历史
└── configs/import/   # 配置导入/导出
```

---

### 阶段三：模块管理 API

#### 3.1 模块管理模块
- EWA_MOD（模块定义）
- EWA_MOD_VER（模块版本）
- EWA_MOD_CFGS（模块配置）
- EWA_MOD_DATA（模块数据）
- EWA_MOD_DDL（数据库结构）
- EWA_MOD_FIELD（字段定义）
- EWA_MOD_IMPORT_LOG（导入日志）
- EWA_MOD_INDEX（索引配置）
- EWA_MOD_INDEX_FIELD（索引字段）
- EWA_MOD_PACKAGE（模块包）
- EWA_MOD_DOWNLOAD（下载记录）

#### 3.2 API 目录结构
```
/ewa-api/modules/v1/
├── modules/          # 模块管理
├── versions/         # 版本管理
├── fields/           # 字段管理
├── ddl/              # 数据库结构管理
└── packages/         # 包管理
```

---

### 阶段四：API 发现与文档

#### 4.1 API 发现端点
- GET /ewa-api/help/documents - 帮助文档（已有框架，待完善）
- GET /ewa-api/help/endpoints - 所有可用接口列表
- GET /ewa-api/help/spec/{format} - API 规范（OpenAPI/Swagger 格式）

#### 4.2 API 文档功能
- 自动生成 API 文档
- 接口测试界面
- 在线调试功能
- API 使用示例

---

### 阶段五：安全与认证增强

#### 5.1 当前安全功能
- ACL 访问控制列表
- 会话管理

#### 5.2 改进建议
- JWT Token 认证
- API Key 管理
- 限流与防刷
- 审计日志
- 敏感数据加密

---

### 阶段六：性能优化

#### 6.1 现有功能
- 支持分页查询（ewa_pagesize 参数）
- 缓存机制

#### 6.2 改进建议
- 响应压缩优化
- 批量操作接口
- 查询缓存优化
- 异步任务处理

---

## 详细实施步骤

### 步骤一：准备工作

1. 查看现有的 XML 配置文件结构
2. 理解当前的 EWA 配置机制
3. 确定用户表结构（从现有代码推断是 ADM_USER 或 EWA_ADMIN）

### 步骤二：创建用户管理的 XML 配置

1. 创建用户列表页面 (users.LF.M)
2. 创建用户表单页面 (users.F.NM)
3. 创建用户查看页面 (users.F.V)
4. 配置相应的 SQL 操作

### 步骤三：配置 RESTful 目录和接口

1. 在 `EWA_RESTFUL_CATALOG` 表中插入目录记录
2. 在 `EWA_RESTFUL` 表中插入接口配置记录
3. 关联到正确的 XML 配置项

### 步骤四：测试和验证

1. 测试各个 API 端点
2. 验证权限控制是否正常工作
3. 检查响应格式是否符合预期
4. 验证错误处理是否适当

---

## 示例 SQL（添加用户管理 API）

### 1. 添加目录记录

```sql
-- 1. 根目录 /ewa-api/users/v1
DECLARE @root_uid VARCHAR(36) = NEWID();
INSERT INTO EWA_RESTFUL_CATALOG 
    (CAT_UID, CAT_PUID, CAT_PATH, CAT_PATH_FULL, CAT_NAME, CAT_NAME_EN, CAT_MEMO, CAT_MEMO_EN, CAT_LVL, CAT_ORD, CAT_STATUS, CAT_CTIME, CAT_MTIME, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9)
VALUES 
    (@root_uid, '', '', '/ewa-api/users/v1', '用户管理', 'User Management', '', '', 1, 1, 'USED', GETDATE(), GETDATE(), 'ewa-api', 'users', 'v1', NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- 2. 用户管理目录 /ewa-api/users/v1/users
DECLARE @users_uid VARCHAR(36) = NEWID();
INSERT INTO EWA_RESTFUL_CATALOG 
    (CAT_UID, CAT_PUID, CAT_PATH, CAT_PATH_FULL, CAT_NAME, CAT_NAME_EN, CAT_MEMO, CAT_MEMO_EN, CAT_LVL, CAT_ORD, CAT_STATUS, CAT_CTIME, CAT_MTIME, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9)
VALUES 
    (@users_uid, @root_uid, 'users', '/ewa-api/users/v1/users', '用户', 'Users', '', '', 2, 1, 'USED', GETDATE(), GETDATE(), 'ewa-api', 'users', 'v1', 'users', NULL, NULL, NULL, NULL, NULL, NULL);

-- 3. 单个用户目录 /ewa-api/users/v1/users/{adm_id}
DECLARE @user_uid VARCHAR(36) = NEWID();
INSERT INTO EWA_RESTFUL_CATALOG 
    (CAT_UID, CAT_PUID, CAT_PATH, CAT_PATH_FULL, CAT_NAME, CAT_NAME_EN, CAT_MEMO, CAT_MEMO_EN, CAT_LVL, CAT_ORD, CAT_STATUS, CAT_CTIME, CAT_MTIME, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9)
VALUES 
    (@user_uid, @users_uid, '{adm_id}', '/ewa-api/users/v1/users/{adm_id}', '用户详情', 'User Detail', '', '', 3, 1, 'USED', GETDATE(), GETDATE(), 'ewa-api', 'users', 'v1', 'users', '{adm_id}', NULL, NULL, NULL, NULL, NULL);
```

### 2. 添加接口配置

```sql
-- GET 用户列表
INSERT INTO EWA_RESTFUL 
    (RS_UID, CAT_UID, RS_METHOD, RS_NAME, RS_NAME_EN, RS_XMLNAME, RS_ITEMNAME, RS_PARAMETERS, RS_MEMO, RS_MEMO_EN, RS_STATUS, RS_CTIME, RS_MTIME)
VALUES 
    (NEWID(), @users_uid, 'GET', '获取用户列表', 'Get User List', '/ewa/ewa_admin.xml', 'EWA_ADMIN.LF.M', 'ewa_pagesize=20', '', '', 'USED', GETDATE(), GETDATE());

-- POST 新增用户
INSERT INTO EWA_RESTFUL 
    (RS_UID, CAT_UID, RS_METHOD, RS_NAME, RS_NAME_EN, RS_XMLNAME, RS_ITEMNAME, RS_PARAMETERS, RS_MEMO, RS_MEMO_EN, RS_STATUS, RS_CTIME, RS_MTIME)
VALUES 
    (NEWID(), @users_uid, 'POST', '新增用户', 'Add User', '/ewa/ewa_admin.xml', 'EWA_ADMIN.F.NM', '', '', '', 'USED', GETDATE(), GETDATE());

-- GET 单个用户
INSERT INTO EWA_RESTFUL 
    (RS_UID, CAT_UID, RS_METHOD, RS_NAME, RS_NAME_EN, RS_XMLNAME, RS_ITEMNAME, RS_PARAMETERS, RS_MEMO, RS_MEMO_EN, RS_STATUS, RS_CTIME, RS_MTIME)
VALUES 
    (NEWID(), @user_uid, 'GET', '获取用户详情', 'Get User Detail', '/ewa/ewa_admin.xml', 'EWA_ADMIN.F.V', '', '', '', 'USED', GETDATE(), GETDATE());

-- PUT 修改用户
INSERT INTO EWA_RESTFUL 
    (RS_UID, CAT_UID, RS_METHOD, RS_NAME, RS_NAME_EN, RS_XMLNAME, RS_ITEMNAME, RS_PARAMETERS, RS_MEMO, RS_MEMO_EN, RS_STATUS, RS_CTIME, RS_MTIME)
VALUES 
    (NEWID(), @user_uid, 'PUT', '修改用户', 'Update User', '/ewa/ewa_admin.xml', 'EWA_ADMIN.F.NM', '', '', '', 'USED', GETDATE(), GETDATE());

-- DELETE 删除用户
INSERT INTO EWA_RESTFUL 
    (RS_UID, CAT_UID, RS_METHOD, RS_NAME, RS_NAME_EN, RS_XMLNAME, RS_ITEMNAME, RS_PARAMETERS, RS_MEMO, RS_MEMO_EN, RS_STATUS, RS_CTIME, RS_MTIME)
VALUES 
    (NEWID(), @user_uid, 'DELETE', '删除用户', 'Delete User', '/ewa/ewa_admin.xml', 'EWA_ADMIN.LF.M', '', '', '', 'USED', GETDATE(), GETDATE());

-- PATCH 恢复用户
INSERT INTO EWA_RESTFUL 
    (RS_UID, CAT_UID, RS_METHOD, RS_NAME, RS_NAME_EN, RS_XMLNAME, RS_ITEMNAME, RS_PARAMETERS, RS_MEMO, RS_MEMO_EN, RS_STATUS, RS_CTIME, RS_MTIME)
VALUES 
    (NEWID(), @user_uid, 'PATCH', '恢复用户', 'Restore User', '/ewa/ewa_admin.xml', 'EWA_ADMIN.LF.M', '', '', '', 'USED', GETDATE(), GETDATE());
```

---

## API 使用示例（添加后的功能）

### 用户管理

**获取用户列表：**
```http
GET /servletRestful/ewa-api/users/v1/users
```

**新增用户：**
```http
POST /servletRestful/ewa-api/users/v1/users
Content-Type: application/json

{
  "adm_lid": "newuser",
  "adm_name": "新用户",
  "adm_pwd": "password"
}
```

**获取单个用户：**
```http
GET /servletRestful/ewa-api/users/v1/users/{adm_id}
```

**修改用户：**
```http
PUT /servletRestful/ewa-api/users/v1/users/{adm_id}
Content-Type: application/json

{
  "adm_name": "修改的用户名"
}
```

**删除用户：**
```http
DELETE /servletRestful/ewa-api/users/v1/users/{adm_id}
```

**恢复用户：**
```http
PATCH /servletRestful/ewa-api/users/v1/users/{adm_id}
```

---

## 预期成果

1. **完整的用户管理 API** - 提供用户 CRUD 操作
2. **系统配置管理 API** - 管理系统配置
3. **模块管理 API** - 管理系统模块
4. **API 发现与文档** - 提供自我描述的 API
5. **增强的安全性** - 更好的认证和授权
6. **性能优化** - 更好的响应速度和并发处理

---

## 注意事项

1. 实施前需确认实际的 XML 配置文件名和配置项名
2. 需根据实际的表结构调整 SQL 操作
3. 权限控制需要根据实际需求配置 ACL
4. 建议先在开发环境测试，再部署到生产环境
5. 保留现有聊天 API 的完整性，不要删除现有配置
