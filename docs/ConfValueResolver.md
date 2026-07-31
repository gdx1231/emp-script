# 配置值解析器 (ConfValueResolver)

统一管理 EWA 框架中敏感配置的解析，支持 Admin 登录密码、数据库连接池密码、SMTP 邮件密码和脚本路径。

## 架构

```
emp-script-utils (基础库)
├── ConfValueResolver         (接口)
├── DefaultConfValueResolver  (默认实现)
└── ConfValueResolvers        (管理器)

使用方：
├── emp-script
│   ├── ConfAdmins        ← Admin 密码
│   ├── ConnectionConfig  ← 数据库连接池密码
│   └── ConfScriptPaths   ← 脚本路径
└── emp-script-utils
    ├── SmtpCfgs          ← SMTP 邮件密码
    └── UPath.initPaths   ← 系统路径配置
```

## 功能特性

- 环境变量引用：`${env.DB_PASSWORD}`
- 系统属性引用：`${user.home}`、`${user.dir}`
- 文件读取：`file:///path/to/password.txt`
- 路径变量：`file://${env.EWA_HOME}/secrets/db.pwd`
- 自定义扩展：实现接口对接 Vault、云密钥服务等

## 配置示例

### Admin 密码

```xml
<!-- ewa_conf.xml -->
<Admins>
  <!-- 环境变量 -->
  <Admin LoginId="admin" Password="${env.ADMIN_PASSWORD}" UserName="管理员" />
  
  <!-- 从文件读取 -->
  <Admin LoginId="dev" Password="file://${env.EWA_HOME}/secrets/dev.pwd" UserName="开发" />
  
  <!-- 明文（不推荐） -->
  <Admin LoginId="test" Password="test12345678" UserName="测试" />
</Admins>
```

### 数据库连接池密码

```xml
<!-- ewa_conf.xml -->
<databases>
  <database name="work" type="MYSQL">
    <pool driverClassName="com.mysql.cj.jdbc.Driver"
          url="jdbc:mysql://localhost:3306/db"
          username="root"
          password="${env.DB_PASSWORD}" />
  </database>
  
  <database name="log" type="MYSQL">
    <pool driverClassName="com.mysql.cj.jdbc.Driver"
          url="jdbc:mysql://localhost:3306/log_db"
          username="log_user"
          password="file://${user.home}/.ewa/db-log.pwd" />
  </database>
</databases>
```

### SMTP 邮件密码

```xml
<!-- ewa_conf.xml -->
<smtps>
  <!-- 环境变量 -->
  <smtp name="default" host="smtp.example.com" port="465" 
        user="noreply@example.com" pwd="${env.SMTP_PASSWORD}" />
  
  <!-- 从文件读取 -->
  <smtp name="backup" host="smtp.backup.com" port="587"
        user="backup@example.com" pwd="file://${env.EWA_HOME}/secrets/smtp.pwd" />
</smtps>
```

### 脚本路径 (scriptPath)

```xml
<!-- ewa_conf.xml -->
<scriptPaths>
  <!-- 使用环境变量 -->
  <scriptPath name="myapp" path="${env.EWA_HOME}/configs/myapp/" />
  
  <!-- 使用用户主目录 -->
  <scriptPath name="personal" path="~/ewa-configs/personal/" />
  
  <!-- 数据库配置（不支持变量解析） -->
  <scriptPath name="dbcfg" path="jdbc:ewa" />
  
  <!-- 资源文件（不支持变量解析） -->
  <scriptPath name="ewa" path="resources:/define.xml/" />
</scriptPaths>
```

### 系统路径 (UPath)

```xml
<!-- ewa_conf.xml -->
<paths>
  <!-- 使用环境变量 -->
  <path name="config_path" value="${env.EWA_HOME}/configs/" />
  <path name="script_path" value="${env.EWA_HOME}/scripts/" />
  
  <!-- 使用用户主目录 -->
  <path name="img_tmp_path" value="~/ewa-data/uploads/" />
  <path name="cached_path" value="${user.dir}/cache/" />
</paths>
```

## 变量语法

| 语法 | 说明 | 示例 |
|------|------|------|
| `${env.VAR_NAME}` | 环境变量 | `${env.DB_PASSWORD}` → `System.getenv("DB_PASSWORD")` |
| `${user.home}` | 用户主目录 | `${user.home}/.ewa/` → `/Users/admin/.ewa/` |
| `${user.dir}` | 工作目录 | `${user.dir}/conf/` → `/opt/ewa/conf/` |
| `${PROP_NAME}` | 系统属性，fallback 环境变量 | `${JAVA_HOME}` → `System.getProperty("JAVA_HOME")` |
| `~` | 用户主目录（仅文件路径） | `~/secrets/` → `/Users/admin/secrets/` |

## 自定义密码解析器

### 1. 实现接口

```java
package com.example;

import com.gdxsoft.easyweb.utils.ConfValueResolver;

public class VaultValueResolver implements ConfValueResolver {

    @Override
    public String resolve(String rawValue) {
        if (rawValue == null) {
            return null;
        }

        // 从 Vault 获取密码
        if (rawValue.startsWith("vault://")) {
            String key = rawValue.substring(8);
            return VaultClient.getSecret(key);
        }

        // 从加密文件读取
        if (rawValue.startsWith("enc://")) {
            String path = rawValue.substring(6);
            return decryptFile(path);
        }

        // 返回 null 表示不处理，保留原值
        return rawValue;
    }
}
```

### 2. 配置使用

**方式一：JVM 启动参数**

```bash
java -Dewa.value.resolver=com.example.VaultValueResolver -jar your-app.jar
```

**方式二：代码设置**

```java
ConfValueResolvers.setResolver(new VaultValueResolver());
```

### 3. 配置文件

```xml
<Admins>
  <Admin LoginId="admin" Password="vault://prod/admin-password" UserName="管理员" />
</Admins>

<databases>
  <database name="work" type="MYSQL">
    <pool password="vault://prod/db-password" />
  </database>
</databases>
```

## 解析流程

```
原始密码值
    ↓
检查是否包含 ${}
    ↓ 是
变量替换（环境变量/系统属性）
    ↓
检查是否 file:// 开头
    ↓ 是
读取文件内容（路径支持变量）
    ↓
返回最终密码
```

如果配置了自定义解析器，会在上述流程**之前**先调用自定义逻辑。

## 注意事项

1. **安全性**：避免在配置文件中明文存储密码，优先使用环境变量或文件引用
2. **文件权限**：密码文件应设置严格的读取权限（如 `chmod 600`）
3. **变量未解析**：如果变量不存在（如环境变量未设置），会保留原始 `${...}` 字符串
4. **解析器返回值**：自定义解析器返回 `null` 表示不处理，继续走默认逻辑
5. **热加载**：`ewa_conf.xml` 修改后会自动重新加载，密码解析器也会重新实例化

## 默认实现

`DefaultConfPasswordResolver` 提供以下功能：

- ✅ 环境变量解析 (`${env.XXX}`)
- ✅ 系统属性解析 (`${user.home}` 等)
- ✅ 文件读取 (`file://`)
- ✅ 路径变量 (`~`、`${user.dir}` 等)
- ✅ 混合内容 (`prefix-${env.XXX}-suffix`)

大多数场景无需自定义解析器。
