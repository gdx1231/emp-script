---
name: java-code-review
description: Systematic code review checklist for emp-script Java modules covering concurrency, resource management, logging, and encapsulation
source: auto-skill
extracted_at: '2026-06-27T01:41:52.912Z'
---

# Java 模块代码审查

对 emp-script 项目的 Java 模块进行系统性代码审查，重点关注以下 8 类常见问题。

## 审查清单

### 1. 并发竞态（TOCTOU）
**模式**: `containsKey()` + `get()` 两步操作
```java
// ❌ 非原子操作，另一线程可能在此期间 remove
if (!_Objects.containsKey(key)) return null;
Object obj = _Objects.get(key);  // 可能为 null

// ✅ 单次 get 后判 null
Object obj = _Objects.get(key);
if (obj == null) return null;
```

### 2. 资源泄漏
**模式**: 重复创建对象但未释放旧实例
```java
// ❌ 旧 officeManager 未 stop
public synchronized static void startService() {
    officeManager = LocalOfficeManager.builder().build();
}

// ✅ 创建前先清理
public synchronized static void startService() {
    if (officeManager != null) stopService();
    officeManager = LocalOfficeManager.builder().build();
}
```

### 3. 空指针防护
**模式**: 外部 API 返回 null 未检查
```java
// ❌ getFormatByExtension 可能返回 null
DocumentFormat formatInput = dfr.getFormatByExtension(ext);
boolean isPpt = formatInput.getInputFamily() == ...;  // NPE

// ✅ 增加 null 检查
if (formatInput == null) {
    LOGGER.error("Unsupported format: {}", ext);
    return;
}
```

### 4. 异常堆栈丢失
**模式**: `getMessage()` 作为 format 参数
```java
// ❌ 堆栈信息丢失
LOGGER.error("Convert fail", e.getLocalizedMessage());

// ✅ 传入 Throwable 对象
LOGGER.error("Convert fail", e);
```
**注意**: `LOGGER.error("msg", err)` 比 `LOGGER.error("msg {}", err)` 更干净（后者异常信息重复）。

### 5. 空字符串拆分
**模式**: 空串 split 返回 `[""]` 而非 `[]`
```java
// ❌ 空串时 split(",") 返回 [""]，导致对空 key 操作
String[] keys = getArrayKey().split(",");

// ✅ 先判空
String keys = getArrayKey();
if (keys.isEmpty()) return;
String[] arr = keys.split(",");
```

### 6. 命名拼写错误
**模式**: 变量名拼写不一致
```java
// ❌ 拼写错误
private static Logger LOOGER = ...;  // 应为 LOGGER
LOOGER.error(...);  // 全文多处使用

// ✅ 全局替换修正
private static Logger LOGGER = ...;
```

### 7. 访问修饰符过宽
**模式**: `public static` 字段无外部访问必要
```java
// ❌ 过度暴露
public static OfficeManager officeManager;

// ✅ 先 grep 确认无外部引用，改为 private
private static OfficeManager officeManager;
```

### 8. 重复实例化
**模式**: 连续 new 同一对象
```java
// ❌ 第一次 new 立即被丢弃
CachedXmlFileMeta meta = new CachedXmlFileMeta();
meta = new CachedXmlFileMeta();

// ✅ 删除多余实例化
CachedXmlFileMeta meta = new CachedXmlFileMeta();
```

### 9. System.out 代替 LOGGER
**模式**: 工具类中使用 System.out.println 而非日志框架
```java
// ❌ 无法配置日志级别和输出
System.out.println(s);
System.out.println(this + ": " + e.getMessage());

// ✅ 使用 SLF4J Logger
LOGGER.info(s);
LOGGER.error("Operation failed", e);
```

### 10. 耗时操作未缓存
**模式**: 每次调用都执行文件系统探测或网络请求
```java
// ❌ 每次调用都扫描文件系统
public String getChromeCmd() {
    if (new File("/Applications/Google Chrome.app/...").exists()) {
        return "\"...\"";
    }
    // ... more checks
}

// ✅ volatile + DCL 缓存结果
private static volatile String detectedBrowser = null;

public String getChromeCmd() {
    if (detectedBrowser != null) return detectedBrowser;
    synchronized (this.getClass()) {
        if (detectedBrowser != null) return detectedBrowser;
        detectedBrowser = detectBrowser();
        return detectedBrowser;
    }
}
```

### 11. 外部进程使用用户默认配置
**模式**: 调用外部程序（如 headless Chrome）时读取用户默认 profile
```java
// ❌ 可能与其他 Chrome 实例冲突，或读取用户隐私数据
cmd = chrome + " --headless --print-to-pdf=out.pdf";

// ✅ 使用独立临时目录
File userDataDir = Files.createTempDirectory("ewa-chrome-").toFile();
userDataDir.deleteOnExit();
cmd = chrome + " --headless --user-data-dir=\"" + userDataDir.getAbsolutePath() + "\" --print-to-pdf=out.pdf";
```

### 12. 外部进程不自行退出
**模式**: 启动外部进程（如 headless Chrome）完成任务后进程不退出，需等 watchdog 超时
```java
// ❌ 使用 commons-exec ExecuteWatchdog，只能等满超时后强杀（60秒）
ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
executor.setWatchdog(watchdog);
executor.execute(commandLine);  // 阻塞直到超时

// ✅ ProcessBuilder + 输出监控线程，检测到完成标记后立即终止
ProcessBuilder pb = new ProcessBuilder(commandLine.toStrings());
Process process = pb.start();
Thread reader = new Thread(() -> {
    while ((len = process.getInputStream().read(buf)) != -1) {
        String chunk = new String(buf, 0, len);
        if (chunk.contains("written to file")) {
            lock.notifyAll();  // 通知主线程
        }
    }
});
reader.start();
synchronized (lock) { if (!success) lock.wait(60000); }
if (process.isAlive()) process.destroyForcibly();
// 实际转换 2.4s 而非 60s
```

### 13. 资源集合清理不完整
**模式**: close() 关闭了列表中每个资源但未 clear()
```java
// ❌ 关闭后列表未清空，实例复用时会重复关闭已关闭的资源
public void close() {
    for (Statement st : _ListStatement) {
        try { st.close(); } catch (SQLException e) { ... }
    }
    // 缺少 _ListStatement.clear()
}

// ✅ 每个列表关闭后立即 clear
public void close() {
    for (Statement st : _ListStatement) {
        try { st.close(); } catch (SQLException e) { ... }
    }
    _ListStatement.clear();
    // 同理 _ListPrepared.clear(), _ListCallable.clear()
}
```

### 14. 重复校验/创建逻辑
**模式**: 多个工厂方法包含几乎相同的参数校验代码
```java
// ❌ createHikariCP() 和 createDruid() 各 30 行重复的 driver/url/user/password 校验
private DataSource createHikariCP() {
    MStr errors = new MStr();
    if (StringUtils.isBlank(driverClassName)) errors.al("...");
    if (StringUtils.isBlank(url)) errors.al("...");
    // ...
}
private DataSource createDruid() {
    MStr errors = new MStr();
    if (StringUtils.isBlank(driverClassName)) errors.al("...");
    if (StringUtils.isBlank(url)) errors.al("...");
    // ...
}

// ✅ 提取公共校验方法
private void validatePoolBasicConfig() throws Exception {
    MStr errors = new MStr();
    if (StringUtils.isBlank(driverClassName)) errors.al("...");
    if (StringUtils.isBlank(url)) errors.al("...");
    if (StringUtils.isBlank(username)) errors.al("...");
    if (StringUtils.isBlank(password)) errors.al("...");
    if (errors.length() > 0) throw new Exception(errors.toString());
}
// 各工厂方法先调用 validatePoolBasicConfig()，只保留差异化代码
```

### 15. 校验逻辑不一致
**模式**: 两个工厂方法对同一配置项的校验规则不同
```java
// ❌ createHikariCP() 不校验 password，createDruid() 校验了
// 原因: 两段校验代码独立维护，差异未被发现

// ✅ 统一使用公共校验方法（见 #14），天然保证一致性
```

### 16. Logger 类名错误
**模式**: `LoggerFactory.getLogger()` 传入了错误的类
```java
// ❌ 日志来源标记为其他类
public class ConfSOffice {
    private static Logger LOGGER = LoggerFactory.getLogger(ConfSecurities.class);
}

// ✅ 使用当前类
private static Logger LOGGER = LoggerFactory.getLogger(ConfSOffice.class);
```
**注意**: 同类中 Logger 名称不一致时通常意味着复制粘贴错误。

## 审查流程

1. **读取目标文件**: 列出目录，读取所有 `.java` 文件
2. **逐项检查**: 对照上述 16 项逐一扫描
3. **汇总报告**: 以表格形式列出发现的问题、文件、行号、修复建议
4. **逐项修复**: 每个 fix 单独 edit，修复后立即 `mvn compile` 验证
5. **编写测试**: 为纯函数和无状态路径编写 JUnit 5 测试
6. **提交**: `git add` + `git commit` 带详细描述

## 修复优先级

- **P0**: 并发竞态(#1)、资源泄漏(#2)、NPE(#3)、外部进程配置隔离(#11)、外部进程不退出(#12)（影响稳定性/安全性/性能）
- **P1**: 异常堆栈丢失(#4)、耗时操作未缓存(#10)、资源集合清理不完整(#13)（影响可调试性/性能/健壮性）
- **P2**: 空字符串拆分(#5)、命名错误(#6)、System.out 代替 LOGGER(#9)、Logger 类名错误(#16)、重复校验/创建逻辑(#14)、校验逻辑不一致(#15)（影响正确性/可读性/可维护性）
- **P3**: 访问修饰符(#7)、重复实例化(#8)（影响代码质量）
