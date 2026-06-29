# Tomcat 10 Embedded Demo

基于 emp-script 框架的嵌入式 Tomcat 10 演示项目，集成 HSQLDB 数据库和完整的后台管理系统。

## 特性

- **嵌入式 Tomcat 10.1.40** - Jakarta EE 9 支持
- **HSQLDB 服务器模式** - 端口 11002，包含 3 个数据库
- **自动数据解压** - 首次启动自动从 zip 解压 HSQLDB 数据
- **P6Spy SQL 日志** - 完整的 SQL 执行跟踪
- **后台管理系统** - 基于 EWA HtmlControl 的管理界面
- **Oracle 语法兼容** - HSQLDB 启用 Oracle 语法模式

## 快速开始

### 1. 启动服务

```bash
cd tomcat10demo

# 后台启动
bin/start.sh

# 或调试模式（前台运行）
bin/debug.sh
```

### 2. 访问应用

- **首页**: http://localhost:8080/
- **后台管理**: http://localhost:8080/back_admin/login.jsp
- **登录账号**: demo / demo12345

### 3. 停止服务

```bash
bin/stop.sh
```

## 数据库

### HSQLDB 服务器

- **端口**: 11002
- **用户**: sa
- **密码**: (空)

### 数据库列表

| 数据库 | 说明 | 表数量 |
|--------|------|--------|
| emp_ewa | EWA 配置表 | 17 |
| emp_portal | 业务数据（已合并 emp_main_data） | 48 |

### 连接方式

```
jdbc:hsqldb:hsql://localhost:11002/emp_ewa
jdbc:hsqldb:hsql://localhost:11002/emp_portal
```

### 数据初始化

数据文件以 zip 格式存储（11MB），首次启动自动解压（323MB）：

```bash
# 手动解压（如需要）
unzip hsqldb-data.zip -d .

# 重新打包（数据修改后）
zip -r hsqldb-data.zip hsqldb/ -x "hsqldb/*.lck" "hsqldb/*.log" "hsqldb/*.tmp*"
```

## 项目结构

```
tomcat10demo/
├── bin/                          # 启动脚本
│   ├── start.sh                 # 后台启动
│   ├── stop.sh                  # 停止服务
│   └── debug.sh                 # 调试模式
├── hsqldb/                      # HSQLDB 数据目录（运行时生成）
├── hsqldb-data.zip              # 压缩的数据库文件
├── sql/                         # SQL 初始化脚本
│   ├── emp_ewa.sql
│   ├── emp_main_data.sql
│   └── emp_portal.sql
├── src/main/
│   ├── java/com/gdxsoft/emp/demo/
│   │   ├── Tomcat10EmbeddedServer.java    # 主入口
│   │   ├── HsqldbServerManager.java       # HSQLDB 管理
│   │   ├── MysqlToHsqldbExporter.java     # MySQL 导出工具
│   │   ├── MergeDatabases.java            # 数据库合并工具
│   │   └── HelloServlet.java              # 示例 Servlet
│   ├── resources/
│   │   ├── ewa_conf.xml                   # EWA 配置
│   │   └── spy.properties                 # P6Spy 配置
│   └── webapp/
│       ├── back_admin/
│       │   ├── login.jsp                  # 登录页（HtmlControl）
│       │   └── index.jsp                  # 管理首页（HtmlControl）
│       ├── WEB-INF/web.xml                # Servlet 配置
│       ├── index.jsp                      # 首页
│       └── hello.jsp                      # 示例 JSP
├── spy.log                      # SQL 日志（运行时生成）
└── pom.xml                      # Maven 配置
```

## 核心组件

### Tomcat10EmbeddedServer

主启动类，负责：
1. 检查并解压 HSQLDB 数据文件
2. 启动 HSQLDB 服务器
3. 启动嵌入式 Tomcat
4. 注册关闭钩子

### HsqldbServerManager

HSQLDB 服务器管理器：
- 启动/停止 HSQLDB 服务器
- 自动启用 Oracle 语法兼容模式
- 支持多数据库

### P6Spy SQL 日志

所有 SQL 执行记录在 `spy.log`：

```
2026-06-29 15:22:24|1ms|statement|connection0|select * from EWA_CFG where ...
```

实时监控：
```bash
tail -f spy.log
```

## 从 MySQL 重新导出

如果需要从 MySQL 重新生成 HSQLDB 数据：

```bash
mvn exec:java -Dexec.mainClass="com.gdxsoft.emp.demo.MysqlToHsqldbExporter" \
  -Dmysql.url="jdbc:mysql://192.168.1.252:53306" \
  -Dmysql.user=root \
  -Dmysql.password="your_password" \
  -Dmysql.databases="emp_ewa,emp_main_data,emp_portal"
```

## 配置说明

### ewa_conf.xml

数据库连接配置：

```xml
<database name="emp_portal" type="HSQLDB" connectionstring="emp_portal" schemaname="PUBLIC">
    <alias name="emp"/>
    <pool driverClassName="com.p6spy.engine.spy.P6SpyDriver" 
          url="jdbc:p6spy:hsqldb:hsql://localhost:11002/emp_portal"
          username="sa" password="" maxActive="20" maxIdle="10" />
</database>
```

### 数据库别名

- `emp_portal` → `emp`
- `emp_ewa` → `ewa2023`
- `emp_main_data` → `main_data`（已合并到 emp_portal）

## 常见问题

### 端口被占用

```bash
# 查看占用端口的进程
lsof -i:8080
lsof -i:11002

# 停止服务
bin/stop.sh
```

### 数据文件损坏

删除 hsqldb 目录，重启服务自动解压：

```bash
rm -rf hsqldb
bin/start.sh
```

### SQL 日志不输出

检查 spy.properties 配置，确保：
```properties
appender=com.p6spy.engine.spy.appender.FileLogger
logfile=spy.log
```

## 技术栈

- **Java**: 17
- **Tomcat**: 10.1.40 (Jakarta EE 9)
- **HSQLDB**: 2.7.4
- **emp-script**: 1.1.10 (jdk17)
- **P6Spy**: 3.9.1
- **Maven**: 3.x

## 开发说明

### 编译

```bash
mvn clean compile
```

### 打包

```bash
mvn clean package
```

生成的 JAR：`target/tomcat10demo-1.0.0-jar-with-dependencies.jar`

### 运行打包的 JAR

```bash
java -jar target/tomcat10demo-1.0.0-jar-with-dependencies.jar
```

## 许可证

MIT License

## 相关链接

- [emp-script 主项目](https://github.com/gdx1231/emp-script)
- [HSQLDB 文档](http://hsqldb.org/)
- [P6Spy 文档](https://p6spy.readthedocs.io/)
