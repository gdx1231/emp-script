---
name: ewa-sql-params
description: Extract SQL parameters from EWA XML config (UserConfig ActionSet/SqlSet) for API documentation or analysis
source: auto-skill
extracted_at: '2026-06-26T08:37:53.918Z'
---

# Extract SQL Parameters from EWA XML Configuration

## EWA XML Config Structure

The EWA framework stores SQL in XML configuration files accessed via `UserConfig`:

```
EasyWebTemplate
  → Action (stored in UserActionItem, a UserXItem)
       → ActionSet (each has Type: OnPageLoad, OnFrameDelete, OnPagePost, etc.)
            → CallSet (CallType="SqlSet", CallName="OnFrameDelete SQL")
       → SqlSet (contains actual SQL statements)
            → Set (Name="OnFrameDelete SQL", SqlType="update")
                 → Sql (CDATA with SQL text containing @paramName placeholders)
```

## Accessing SQL from UserConfig

```java
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValues;

// 1. Load config by xmlName and itemName
UserConfig uc = UserConfig.instance(xmlName, itemName, null);

// 2. Get the Action item (UserXItem, not UserXItems)
UserXItem actionItem = uc.getUserActionItem();

// 3. Access SqlSet child items
if (actionItem.testName("SqlSet")) {
    UserXItemValues sqlSets = actionItem.getItem("SqlSet");
    for (int i = 0; i < sqlSets.count(); i++) {
        UserXItemValue sqlItem = sqlSets.getItem(i);
        if (sqlItem.testName("Sql")) {
            String sql = sqlItem.getItem("Sql"); // actual SQL text
            String sqlType = sqlItem.getItem("SqlType"); // "query", "update", "procedure"
        }
    }
}
```

## Extracting @parameter Placeholders

EWA uses `@paramName` syntax in SQL (replaced with JDBC `?` at runtime). Extract with regex:

```java
private static List<String> extractSqlParameters(String sql) {
    List<String> params = new ArrayList<>();
    Pattern pattern = Pattern.compile("@([A-Za-z_][A-Za-z0-9_]*)");
    Matcher matcher = pattern.matcher(sql);
    while (matcher.find()) {
        String param = matcher.group(1);
        if (!params.contains(param)) {
            params.add(param);
        }
    }
    return params;
}
```

## Key Classes

| Class | Package | Purpose |
|-------|---------|---------|
| `UserConfig` | `...script.userConfig` | XML config loader; `instance(xmlName, itemName, null)` |
| `UserXItem` | `...script.userConfig` | Action item container; `getUserActionItem()` returns this |
| `UserXItemValues` | `...script.userConfig` | Collection of `UserXItemValue` (from `SetBase<T>`) |
| `UserXItemValue` | `...script.userConfig` | Single SQL item; `getItem("Sql")` returns SQL text |
| `UserXItems` | `...script.userConfig` | Collection of `UserXItem` (field definitions) |

**Note:** `UserXItem` (field definitions) vs `UserXItemValue` (SqlSet items) — different classes with similar names.

## Common Action Types

- `OnPageLoad` — page load query
- `OnPagePost` — form POST / insert
- `OnFrameDelete` — DELETE operation (often soft-delete via UPDATE SET status='DEL')
- `OnFrameRestore` — restore from soft-delete (UPDATE SET status='USED')
