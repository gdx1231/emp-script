# emp-script 项目上下文

## 项目概述

**emp-script** (Easy Web Application Builder) 是一个 Web 系统快速成型开发框架，主要用于 CRM、OA、SCM 等企业管理系统的快速开发。

### 核心目标
- 通过 XML 配置文件部署大量低技术含量工作，减少 80% 的开发周期
- 将开发工程师的时间解放，专注于数据库架构设计和核心业务逻辑
- 降低开发时间和开发成本

### 技术栈
- **语言**: Java 8
- **构建工具**: Maven
- **许可证**: MIT License
- **主要依赖**:
  - Servlet API 4.0 / WebSocket API 1.1
  - MySQL Connector / SQL Server JDBC / HSQLDB
  - HikariCP / Druid (数据库连接池)
  - Apache Commons (Lang3, IO, FileUpload, Exec)
  - SLF4J 日志框架
  - Jedis (Redis 客户端)
  - Jsoup (HTML 解析)
  - JODConverter (文档转换)

### 项目结构
```
emp-script/
├── src/main/java/com/gdxsoft/easyweb/   # 核心源代码
│   ├── acl/          # 权限控制接口
│   ├── app/          # 应用相关
│   ├── cache/        # 缓存模块 (HSQDB/Redis)
│   ├── conf/         # 配置管理
│   ├── data/         # 数据处理 (DTTable, DTCell 等)
│   ├── datasource/   # 数据源管理
│   ├── debug/        # 调试功能
│   ├── define/       # 定义解析
│   ├── document/     # 文档处理
│   ├── function/     # 函数库
│   ├── global/       # 全局变量
│   ├── script/       # 脚本引擎
│   ├── uploader/     # 文件上传
│   ├── websocket/    # WebSocket 支持
│   └── utils/        # 工具类
├── src/main/resources/
│   ├── define.xml    # 系统定义配置
│   ├── system.xml    # 系统配置
│   └── conf.help.hsqldb/  # HSQLDB 配置帮助
├── src/test/         # 测试代码
├── pom.xml           # Maven 配置
└── readme.md         # 项目说明
```

## 构建和运行

### 环境要求
- JDK 1.8+
- Maven 3.x+

### 构建命令
```bash
# 编译项目
mvn clean compile

# 打包 (生成 JAR 文件)
mvn clean package

# 安装到本地仓库
mvn clean install

# 发布到 Maven Central (需要 GPG 签名)
mvn clean deploy -P release
```

### 打包输出
- `target/emp-script-{version}.jar` - 主 JAR 包
- `target/emp-script-{version}-sources.jar` - 源代码 JAR
- `target/emp-script-{version}-javadoc.jar` - Javadoc JAR
- `target/emp-script-last.jar` - 副本 (用于部署)

## 核心模块说明

### 1. 数据处理 (data 包)
- **DTTable**: 数据表对象，支持 JDBC、XML、JSON 等多种数据源
- **DTCell/DTColumn/DTRow**: 单元格、列、行对象
- 支持数据库操作 (查询、插入、更新、删除)

### 2. 脚本引擎 (script 包)
- XML 配置驱动的页面生成引擎
- 支持 Ajax 调用 (XML/JSON 格式)
- 表单框架 (Frame) 处理

### 3. 缓存模块 (cache 包)
- 支持 HSQDB 和 Redis 两种缓存方式
- SQL 结果缓存

### 4. 配置管理 (conf 包)
- `ewa_conf.xml` - 主配置文件
- 支持数据库配置、路径配置、安全配置等

### 5. RESTful API
- 支持 RESTful 风格的 API 定义
- 配置化接口映射到 XML 配置项

## 开发约定

### 配置文件示例 (ewa_conf.xml)
```xml
<ewa_confs>
    <!-- 数据库配置 -->
    <databases>
        <database name="work" type="MYSQL">
            <pool driverClassName="com.mysql.cj.jdbc.Driver" 
                  url="jdbc:mysql://localhost:3306/db"/>
        </database>
    </databases>
    
    <!-- 脚本路径配置 -->
    <scriptPaths>
        <scriptPath name="ewa" path="resources:/define.xml"/>
        <scriptPath name="my-project" path="/path/to/config"/>
    </scriptPaths>
</ewa_confs>
```

### 数据库操作示例
```java
// 查询
String sql = "SELECT * FROM table_name WHERE id=@id";
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("id", someId);
DTTable tb = DTTable.getJdbcTable(sql, rv);

// 更新
String sql = "UPDATE table_name SET name=@name WHERE id=@id";
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("name", "newName");
rv.addOrUpdateValue("id", someId);
DataConnection.updateAndClose(sql, "", rv);
```

### MySQL 函数支持
项目提供以下 MySQL 函数 (见 `MYSQL.functions.md`):
- `GETDATE()` - 获取当前日期时间
- `CHARINDEX()` - 字符串查找 (兼容 SQL Server)
- `fn_chn_money()` - 货币金额转中文大写
- `f_getpy()` - 获取中文拼音首字母
- `fn_GetQuanPin()` - 获取中文全拼
- `FN_ZERO_ADD()` - 数字补零

## 数据库表结构

### EWA 配置表
- `ewa_cfg` - 配置主表
- `ewa_cfg_his` - 配置历史表
- `ewa_cfg_oth` - 其他配置表
- `ewa_cfg_tree` - 配置树结构

### RESTful API 表
- `ewa_restful` - RESTful 接口定义
- `ewa_restful_catalog` - 接口目录

### 模块管理表
- `ewa_mod` - 模块定义
- `ewa_mod_ver` - 模块版本
- `ewa_mod_ddl` - 数据库 DDL
- `ewa_mod_field` - 字段定义
- 等...

## 相关文档
- `readme.md` - 项目详细说明和数据库表结构
- `AI.prompt.md` - AI 辅助开发提示 (数据操作示例)
- `MYSQL.functions.md` - MySQL 自定义函数定义
- `LICENSE` - MIT 许可证

## GitHub 仓库
- 源码：https://github.com/gdx1231/emp-script
- 组织：gdxsoft (www.gdxsoft.com)
