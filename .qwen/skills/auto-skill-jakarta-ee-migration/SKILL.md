---
name: jakarta-ee-migration
description: Migrate emp-script from javax.servlet/javax.websocket to Jakarta EE 9, replace commons-exec with ProcessBuilder, and upgrade commons-fileupload to jakarta-compatible version
source: auto-skill
extracted_at: '2026-06-27T03:23:07.815Z'
---

# Jakarta EE 9 Migration Guide

## Overview

When migrating emp-script from javax → jakarta namespace, three dependency migrations are required simultaneously:
1. Jakarta EE 9 (`javax.servlet` → `jakarta.servlet`, `javax.websocket` → `jakarta.websocket`)
2. Apache Commons Exec → JDK `ProcessBuilder` (zero-external-dependency goal)
3. commons-fileupload 1.x → commons-fileupload2 (Jakarta-compatible, with API changes)

## Step 1: pom.xml Changes

### Jakarta EE Dependencies
```xml
<!-- OLD -->
<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <version>4.0.1</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>javax.websocket</groupId>
    <artifactId>javax.websocket-api</artifactId>
    <version>1.1</version>
    <scope>provided</scope>
</dependency>

<!-- NEW (Jakarta EE 9) -->
<dependency>
    <groupId>jakarta.servlet</groupId>
    <artifactId>jakarta.servlet-api</artifactId>
    <version>5.0.0</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>jakarta.websocket</groupId>
    <artifactId>jakarta.websocket-api</artifactId>
    <version>2.0.0</version>
    <scope>provided</scope>
</dependency>
```

### Remove commons-exec
```xml
<!-- REMOVE -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-exec</artifactId>
    <version>1.3</version>
</dependency>
```

### Upgrade commons-fileupload
```xml
<!-- OLD -->
<dependency>
    <groupId>commons-fileupload</groupId>
    <artifactId>commons-fileupload</artifactId>
    <version>1.6.0</version>
</dependency>

<!-- NEW (Jakarta-compatible, needs core + servlet5) -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-fileupload2-core</artifactId>
    <version>2.0.0-M2</version>
</dependency>
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-fileupload2-jakarta-servlet5</artifactId>
    <version>2.0.0-M2</version>
</dependency>
```

## Step 2: Bulk javax → jakarta Rename

Use sed to rename across all Java files. Order matters: rename `javax.websocket` first to avoid double-renaming (some files import both):

```bash
# Rename javax.websocket first (3 files typically)
find src/main/java -name "*.java" -exec grep -l "javax\.websocket" {} \; \
  | xargs sed -i '' 's/javax\.websocket/jakarta.websocket/g'

# Then rename javax.servlet (40+ files)
find src/main/java -name "*.java" -exec grep -l "javax\.servlet" {} \; \
  | xargs sed -i '' 's/javax\.servlet/jakarta.servlet/g'

# Verify zero remaining
grep -rl "javax\.servlet" src/main/java --include="*.java" | wc -l  # must be 0
grep -rl "javax\.websocket" src/main/java --include="*.java" | wc -l  # must be 0
```

## Step 3: Replace commons-exec with ProcessBuilder

### Pattern A: Simple command execution with timeout (DocUtils, Pdf2Swf)

Replace:
```java
CommandLine commandLine = CommandLine.parse(cmd);
DefaultExecutor executor = new DefaultExecutor();
ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
executor.setWatchdog(watchdog);
ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
executor.setStreamHandler(streamHandler);
executor.execute(commandLine);
```

With:
```java
String[] args = cmd.split(" ");  // only safe if no quoted args with spaces
ProcessBuilder pb = new ProcessBuilder(args);
pb.redirectErrorStream(true);
Process process = pb.start();

// Read output in daemon thread
StringBuilder output = new StringBuilder();
Thread reader = new Thread(() -> {
    try {
        byte[] buf = new byte[1024];
        int len;
        while ((len = process.getInputStream().read(buf)) != -1) {
            output.append(new String(buf, 0, len));
        }
    } catch (IOException ignored) {}
});
reader.setDaemon(true);
reader.start();

// Wait with timeout
boolean finished = process.waitFor(60, TimeUnit.SECONDS);
if (!finished) {
    process.destroyForcibly();
    // handle timeout
}
reader.join(3000);
int exitCode = process.exitValue();
```

### Pattern B: Command with quoted arguments (Html2PdfByChrome)

When the command string contains quoted paths (e.g., `"/Applications/Google Chrome"`), do NOT use `String.split()`. Instead, refactor to build `List<String>` directly:

```java
// BEFORE: string concatenation
String cmd = this.getChromeCmd();
cmd += " --headless --disable-gpu";
cmd += " --print-to-pdf=\"" + pdfFile + "\" \"" + url + "\"";
CommandLine commandLine = CommandLine.parse(cmd);
ProcessBuilder pb = new ProcessBuilder(commandLine.toStrings());

// AFTER: List<String> construction
List<String> args = new ArrayList<>();
args.add(stripQuotes(this.getChromeCmd()));
args.add("--headless=new");
args.add("--disable-gpu");
args.add("--print-to-pdf=" + pdfFile.toString());
args.add(url);
ProcessBuilder pb = new ProcessBuilder(args);
```

Key points:
- Remove all `import org.apache.commons.exec.*` imports
- Remove `import java.io.ByteArrayOutputStream` (replace with StringBuilder + byte[] buf)
- `ExecuteException` → `IOException` (checked exception becomes standard)
- Add `Thread.currentThread().interrupt()` in `InterruptedException` catch blocks

## Step 4: commons-fileupload 1.x → 2.x Migration

The API has breaking changes in version 2.x. Package names and class names change:

### Import Changes
```java
// OLD                                   // NEW
org.apache.commons.fileupload.FileItem              → org.apache.commons.fileupload2.core.FileItem
org.apache.commons.fileupload.FileUploadException   → org.apache.commons.fileupload2.core.FileUploadException
org.apache.commons.fileupload.disk.DiskFileItemFactory → org.apache.commons.fileupload2.core.DiskFileItemFactory
org.apache.commons.fileupload.servlet.ServletFileUpload  → org.apache.commons.fileupload2.jakarta.servlet5.JakartaServletFileUpload
org.apache.commons.fileupload.ProgressListener      → org.apache.commons.fileupload2.core.ProgressListener
```

### DiskFileItemFactory: Setter → Builder Pattern
```java
// OLD
DiskFileItemFactory factory = new DiskFileItemFactory();
factory.setSizeThreshold(bufferSize);
factory.setRepository(tempPath);

// NEW (Builder pattern, get() throws IOException)
DiskFileItemFactory factory;
try {
    factory = DiskFileItemFactory.builder()
        .setPath(tempPath.toPath())
        .setBufferSize(bufferSize)
        .get();
} catch (Exception e) {
    throw new RuntimeException("Failed to create DiskFileItemFactory", e);
}
```

### FileItem.write(): File → Path
```java
// OLD
item.write(uploadedFile);

// NEW
item.write(uploadedFile.toPath());
```

### Class Name Change
- All `ServletFileUpload` references → `JakartaServletFileUpload`
  - Variable declarations: `ServletFileUpload upload` → `JakartaServletFileUpload upload`
  - Static calls: `ServletFileUpload.isMultipartContent(...)` → `JakartaServletFileUpload.isMultipartContent(...)`

### M2→M5 Size Limit API Changes

When upgrading commons-fileupload2 from 2.0.0-M2 to 2.0.0-M5, two size-limit methods were renamed:

```java
// OLD (M2)                              // NEW (M5)
upload.setSizeMax(long)        →         upload.setMaxSize(long)
upload.setFileSizeMax(int)     →         upload.setMaxFileSize(long)  // int → long!
```

**Affected files** (4 files, all use `JakartaServletFileUpload`):
- `src/main/java/.../script/servlets/ServletRestful.java`
- `src/main/java/.../script/servlets/ServletUpload.java`
- `src/main/java/.../script/HtmlUploader.java`
- `src/main/java/.../uploader/PostData.java`

**Example fix** (ServletRestful.java): `upload.setSizeMax(maxSize)` → `upload.setMaxSize(maxSize)`

**PostData.java** has both, also int→long type change:
```java
// OLD
upload.setFileSizeMax(102400000);
upload.setSizeMax(102400000);

// NEW
upload.setMaxFileSize(102400000L);
upload.setMaxSize(102400000L);
```

To verify the latest API when upgrading: decompile from local Maven repository:
```bash
mvn dependency:copy -Dartifact=org.apache.commons:commons-fileupload2-jakarta-servlet5:<version> -DoutputDirectory=/tmp/fu
javap -public org/apache/commons/fileupload2/jakarta/servlet5/JakartaServletFileUpload.class
```

## Verification

```bash
# Verify no remaining javax.servlet/websocket references
grep -rl "javax\.servlet\|javax\.websocket" src/main/java --include="*.java" | wc -l  # must be 0

# Verify no remaining commons-exec references
grep -rl "org\.apache\.commons\.exec\|CommandLine\|DefaultExecutor\|ExecuteWatchdog\|PumpStreamHandler" src/main/java --include="*.java" | wc -l  # must be 0

# Verify no old commons-fileupload imports
grep -rl "org\.apache\.commons\.fileupload\." src/main/java --include="*.java" | grep -v "fileupload2" | wc -l  # must be 0

# Compile
mvn clean compile
```

## Common Pitfalls

1. **Double-rename**: Some files import both `javax.servlet` AND `javax.websocket`. Rename `javax.websocket` first, then `javax.servlet`, to avoid `jakarta.servlet` → `jakarta.servlet` (no-op).

2. **commons-fileupload2 get() throws IOException**: The builder's `.get()` method declares `throws IOException`. Wrap in try-catch and use `catch (Exception e)` (not `catch (java.io.IOException e)`) if the enclosing method doesn't declare `throws IOException`.

3. **CommandLine.parse() removal**: When commands contain quoted arguments with spaces (e.g., `"/Applications/Google Chrome.app/.../Google Chrome"`), refactor to pass `List<String>` to ProcessBuilder instead of splitting strings.

4. **Strip quotes from browser path**: `detectBrowser()` returns paths like `"/path/with spaces/app"`. Use `stripQuotes()` helper before adding to `List<String>` args list.
