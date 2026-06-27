---
name: write-unit-tests
description: Write JUnit 5 unit tests for emp-script classes, handling heavy-dependency classes by testing pure functions and stateless paths
source: auto-skill
extracted_at: '2026-06-27T01:33:59.395Z'
---

# Write Unit Tests for emp-script

## Framework

- **JUnit 5 (Jupiter)** — already in `pom.xml`
- **No mocking framework** (no Mockito/PowerMock) — cannot mock `UserConfig`, `ConfigStatus`, etc.
- Test location: `src/test/java/com/gdxsoft/easyweb/<package>/`

## Run Tests

```bash
# Single test class
mvn test -Dtest=com.gdxsoft.easyweb.cache.ConfigCacheTest

# All tests
mvn test

# Skip tests during packaging (pre-existing test compilation issues in bussinessXmlCreator)
mvn clean package -Dmaven.test.skip=true
```

## Strategy: Test What's Testable

Many emp-script classes have heavy dependencies (filesystem, database, XML parsing). Without mocking, focus on:

### Testable Without External Dependencies

| What | Example |
|------|---------|
| Pure functions / key generators | `ConfigCacheWidthSqlCached.getConfigFileKey(xmlName)` |
| Serialization roundtrips | `CachedXmlFileMeta.toSerialize()` → `fromSerialize()` |
| Empty/null-state behavior | `ConfigCache.getUserConfig("nonexistent", "x")` → null |
| Edge cases | empty `clearCache()`, empty `getArrayKey()` |
| Simple data structures | `CacheGroup.addItem()` / `getItem()` |
| Concurrent safety | multi-thread `setUserConfig` with `CountDownLatch` |
| String normalization | `getConfigItemKey("ABC")` equals `getConfigItemKey("abc")` |

### Hard to Test (Skip or Test Only Surface)

| Class | Why |
|-------|-----|
| `UserConfig` | Requires XML parsing, `ConfScriptPath`, filesystem |
| `ConfigStatus` | Requires `IConfig` → filesystem or JDBC |
| `getUserConfig` (full path) | Calls `UFileCheck`, `ConfigStatus.isChanged()` |

## Test Template

```java
package com.gdxsoft.easyweb.cache;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConfigCacheTest {

    @BeforeEach
    void setUp() {
        ConfigCache.clearCache();
    }

    @Test
    void testGetUserConfigReturnsNullWhenEmpty() {
        assertNull(ConfigCache.getUserConfig("nonexistent.xml", "item1"));
    }

    @Test
    void testClearCacheOnEmpty() {
        assertDoesNotThrow(() -> ConfigCache.clearCache());
    }
}
```

## Common Patterns

### Assert no exception on edge cases
```java
assertDoesNotThrow(() -> ConfigCache.removeUserConfig("nonexistent.xml", "item1"));
```

### Serialization roundtrip
```java
CachedXmlFileMeta meta = new CachedXmlFileMeta();
meta.setCode("c1");
byte[] buf = meta.toSerialize();
CachedXmlFileMeta restored = CachedXmlFileMeta.fromSerialize(buf);
assertEquals("c1", restored.getCode());
```

### Concurrent safety
```java
CountDownLatch latch = new CountDownLatch(threadCount);
AtomicInteger errors = new AtomicInteger(0);
// spawn threads, latch.countDown() in finally
latch.await();
assertEquals(0, errors.get());
```

## SLF4J Logger Conventions

```java
// Correct — Throwable as last arg, no {} placeholder → prints stack trace
LOGGER.warn("Descriptive message", err);

// Wrong — err.getMessage() becomes format arg, stack trace lost
LOGGER.warn("Descriptive message", err.getMessage());

// If message needs exception description + stack trace:
LOGGER.warn("Failed: {}", err.getMessage(), err);
```

## Testing ProcessBuilder / External Commands

When testing methods that use `ProcessBuilder` to run external commands:

```java
@Test
void testCommandFailsWhenBinaryNotInstalled() {
    // The command (e.g., 7z, pdf2swf, chrome) usually fails → verify graceful handling
    boolean result = DocUtils.zipWith7zip(target, source);
    assertFalse(result, "should return false when command not found");
}
```

Key principles:
- **Test failure paths**: External binaries may not exist → verify `return false`, no crash
- **No mocking**: Don't mock `Process` — test real behavior, expect IOException
- **Use `@TempDir`** for temporary filesystem paths

## Using @TempDir and Reflection

### @TempDir for file-based tests
```java
import org.junit.jupiter.api.io.TempDir;

class MyTest {
    @TempDir
    static File tempDir;  // JUnit creates and cleans up automatically

    @Test
    void test() {
        File f = new File(tempDir, "out.txt");
        // use f...
    }
}
```

### Reflection to initialize UPath.INIT_PARAS
Many classes access `UPath.getInitPara(...)` during construction, which NPEs if not initialized:

```java
@BeforeAll
static void initUPath() throws Exception {
    Field f = UPath.class.getDeclaredField("INIT_PARAS");
    f.setAccessible(true);
    if (f.get(null) == null) {
        f.set(null, new MTableStr());
    }
}
```

### Reflection to access private static fields
```java
// Reset cached state between tests
Field f = Html2PdfByChrome.class.getDeclaredField("detectedBrowser");
f.setAccessible(true);
f.set(null, null);

// Verify private field value
Field omField = OpenOfficeInstance.class.getDeclaredField("officeManager");
omField.setAccessible(true);
assertNull(omField.get(null));
```

### Environment-dependent tests with assumeTrue
```java
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Test
void testNeedsChrome() {
    assumeTrue(browserAvailable(), "requires Chrome or Edge");
    // ... actual test only runs if browser is installed
}
```

## Test Organization for fileConvert Package

```
src/test/java/com/gdxsoft/easyweb/utils/fileConvert/
├── FileConvertTest.java     — unit tests: getters, setters, browser detection, deprecations
├── Html2PdfByChromeTest.java — integration tests: actual PDF conversion (needs browser)
└── OpenOfficeInstanceTest.java — static state tests (no Office needed)
```

```
src/test/java/com/gdxsoft/easyweb/document/
└── DocUtilsTest.java        — ProcessBuilder failure paths, UFile.zipPaths success path
```
