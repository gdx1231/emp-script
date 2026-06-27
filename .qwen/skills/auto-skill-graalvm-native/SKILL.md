---
name: graalvm-native
description: Compile emp-script to GraalVM native-image and create interactive CLI config tools
source: auto-skill
extracted_at: '2026-06-27T07:50:56.490Z'
---

# GraalVM Native Image + Interactive CLI for emp-script

## Prerequisites

Oracle GraalVM JDK 21+ is required. On macOS:

```bash
# Check if installed
/usr/libexec/java_home -V | grep graalvm

# If not, install via Homebrew
brew install graalvm-jdk
```

Verify `native-image` is in `$JAVA_HOME/bin/`.

## Maven Native Profile Configuration

Add to `pom.xml` `<profiles>` section:

```xml
<profile>
    <id>native</id>
    <build>
        <plugins>
            <plugin>
                <groupId>org.graalvm.buildtools</groupId>
                <artifactId>native-maven-plugin</artifactId>
                <version>0.10.6</version>
                <extensions>true</extensions>
                <executions>
                    <execution>
                        <id>build-native</id>
                        <goals>
                            <goal>compile-no-fork</goal>
                        </goals>
                        <phase>package</phase>
                    </execution>
                </executions>
                <configuration>
                    <imageName>my-tool-name</imageName>
                    <mainClass>com.gdxsoft.easyweb.MyMainClass</mainClass>
                    <buildArgs>
                        <buildArg>--no-fallback</buildArg>
                        <buildArg>-H:+ReportExceptionStackTraces</buildArg>
                    </buildArgs>
                </configuration>
            </plugin>
        </plugins>
    </build>
</profile>
```

**Why:** `--no-fallback` ensures a true native image (fails instead of falling back to JVM). `-H:+ReportExceptionStackTraces` gives readable stack traces in the native binary.

**Build:**
```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/graalvm-jdk-21/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH
cd /Users/admin/java/com.gdxsoft/emp-script
mvn clean package -Pnative
```

The native executable appears in `target17/<imageName>`.

## Interactive CLI Pattern

When building interactive config generators (like `ewa_conf.xml` creator), follow this structure:

### Core pattern — every section MUST have a yes/no gate

**Why:** Without a yes/no prompt before field collection, piped input (e.g., `printf | java`) will desync, consuming the next section's answer as a field value. This cascades through all subsequent sections.

```java
private void askSectionX(StringBuilder sb) {
    section("Section Title");                        // 1. Print section header
    if (!askYesNo("配置 X?", true)) {                // 2. Gate — mandatory
        return;
    }
    String value = ask("字段", "默认值");            // 3. Collect fields only if Y
    // ... more fields ...
    sb.append(...);                                  // 4. Write XML
}
```

### Helper methods

```java
// Section header with highlight
private void section(String title) {
    System.out.println();
    System.out.println("  \033[1;33m▶ " + title + "\033[0m");
}

// Ask with default value; "skip" returns null
private String ask(String prompt, String defaultValue) {
    String label = "    " + prompt;
    if (!defaultValue.isEmpty()) {
        label += " [" + defaultValue + "]";
    }
    label += ": ";
    System.out.print(label);
    String input = scanner.nextLine().trim();
    if (input.equalsIgnoreCase("skip")) return null;
    return input.isEmpty() ? defaultValue : input;
}

// Yes/No with default; Enter uses default
private boolean askYesNo(String prompt, boolean defaultValue) {
    String yn = defaultValue ? "Y/n" : "y/N";
    System.out.print("    " + prompt + " (" + yn + ")? ");
    String input = scanner.nextLine().trim().toLowerCase();
    if (input.isEmpty()) return defaultValue;
    return input.startsWith("y");
}
```

### Dual-mode design

Offer both interactive and template modes:

```bash
./tool                    # Interactive mode (default)
./tool -t -o out.xml      # Template mode (skip all prompts, generate defaults)
```

```java
public static void main(String[] args) {
    // Parse -t/--template flag
    if (templateMode) {
        xml = creator.buildTemplateXml();
    } else {
        xml = creator.buildInteractiveXml();
    }
}
```

### Multi-entry loop pattern (databases, script paths)

```java
// Loop: collect entries until user presses Enter with empty name
while (true) {
    String name = ask("名称 (回车结束)", "");
    if (name == null || name.isEmpty()) break;
    // ... collect more fields ...
    // Ask if user wants another
    if (!askYesNo("再添加一个?", false)) break;
}
```

## Debugging interactive CLI

**Do NOT** use `printf | java` to test interactive CLI — input sequencing is fragile and masks real bugs. Instead:

- Use `yes "" | tool` to simulate all-defaults (Enter for everything)
- Test interactively with real terminal input
- Verify generated XML output, not prompt ordering

**Key gotcha found:** The `askSqlCached` section originally jumped straight to `ask("缓存方式")` without a yes/no gate. This consumed the next section's answer ("Y"/"N"), causing all downstream sections to desync. Every section must have a gate, no exceptions.
