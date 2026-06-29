---
name: local-acl-impl
description: Create a local BackAdminImpl ACL class when emp-script-web is not on the classpath, and update database CLASS_ACL references.
source: auto-skill
extracted_at: '2026-06-29T07:56:53.241Z'
---

# Local ACL Implementation for emp-script Demo

## When to Use

- Running an emp-script demo project **without** `emp-script-web` in the classpath
- Error: `loadClass com.gdxsoft.web.acl.BackAdminImpl` — class not found
- Need a standalone ACL (`IAcl`) implementation for back-office pages
- EWA XML configurations reference `CLASS_ACL=com.gdxsoft.web.acl.BackAdminImpl` in the database

## Root Cause

`emp-script`'s EWA XML configs (stored in `EWA_CFG` table) reference `com.gdxsoft.web.acl.BackAdminImpl` in the `CLASS_ACL` column. This class is provided by the separate `emp-script-web` project. When running a demo without `emp-script-web`, the framework fails with:

```
ERROR UObjectValue - loadClass com.gdxsoft.web.acl.BackAdminImpl: com.gdxsoft.web.acl.BackAdminImpl
```

## Solution Overview

1. Create a local `BackAdminImpl` that implements `IAcl` — copy/simplify from `emp-script-web`
2. Create a local `AclBase` helper class
3. Update database `EWA_CFG.CLASS_ACL` column to point to the new class

## Step-by-Step

### 1. Create AclBase.java

Package: `com.gdxsoft.emp.demo.acl` (adjust package as needed)

```java
package com.gdxsoft.emp.demo.acl;

import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.PageValueTag;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.HtmlCreator;

public class AclBase {
    private RequestValue _RequestValue;
    private String _XmlName;
    private String _ItemName;
    private String _GoToUrl;
    private HtmlCreator htmlCreator;

    // --- Getters / Setters (IAcl interface contract) ---
    public HtmlCreator getHtmlCreator() { return htmlCreator; }
    public void setHtmlCreator(HtmlCreator hc) { this.htmlCreator = hc; }
    public String getDenyMessage() { return null; }
    public String getGoToUrl() { return _GoToUrl; }
    public void setGoToUrl(String url) { _GoToUrl = url; }
    public String getItemName() { return _ItemName; }
    public void setItemName(String name) { _XmlName = name; }
    public RequestValue getRequestValue() { return _RequestValue; }
    public void setRequestValue(RequestValue rv) { _RequestValue = rv; }
    public String getXmlName() { return _XmlName; }
    public void setXmlName(String name) { this._XmlName = name; }

    // --- Static helpers (from original AclBase) ---
    public static int getId(RequestValue rv, String key) {
        String v = getString(rv, key);
        if (v == null) return -1;
        try { return Integer.parseInt(v); } catch (Exception e) { return -1; }
    }

    public static String getString(RequestValue rv, String key) {
        PageValue pv = rv.getPageValues().getPageValue(key);
        if (pv == null) return null;
        if (pv.getPVTag() != PageValueTag.COOKIE_ENCYRPT
            && pv.getPVTag() != PageValueTag.SESSION) return null;
        try { return pv.getStringValue(); } catch (Exception e) { return null; }
    }
}
```

**Key contract:** `IAcl` requires `canRun()`, `goToUrl`, `xmlName`, `itemName`, `requestValue`, `htmlCreator`. The original `AclBase` in `emp-script-web` provides defaults for all of these.

### 2. Create BackAdminImpl.java

```java
package com.gdxsoft.emp.demo.acl;

import com.gdxsoft.easyweb.acl.IAcl;
import com.gdxsoft.easyweb.script.RequestValue;

public class BackAdminImpl extends AclBase implements IAcl {

    /**
     * Check session-based login via G_ADM_ID and G_SUP_ID
     */
    public static boolean isLogined(RequestValue rv) {
        int iAdmId = AclBase.getId(rv, "G_ADM_ID");
        int iSupId = AclBase.getId(rv, "G_SUP_ID");
        return iAdmId >= 0 && iSupId >= 0;
    }

    @Override
    public boolean canRun() {
        if (isLogined(super.getRequestValue())) {
            return true;
        }
        // Redirect to login page
        RequestValue rv = super.getRequestValue();
        String loginUrl = rv.s("EWA.CP") + "/back_admin/login.jsp";

        // Encode referrer URL
        if (rv.getRequest() != null) {
            String ref = rv.getRequest().getContextPath()
                + rv.getRequest().getServletPath();
            if (rv.getRequest().getQueryString() != null) {
                ref += "?" + rv.getRequest().getQueryString();
            }
            try {
                loginUrl += "?ref=" + java.net.URLEncoder.encode(ref, "UTF-8");
            } catch (java.io.UnsupportedEncodingException e) {
                loginUrl += "?ref=" + ref;
            }
        }

        super.setGoToUrl(loginUrl);
        return false;
    }
}
```

**Key differences from the original `com.gdxsoft.web.acl.BackAdminImpl`:**
- Uses `/back_admin/login.jsp` instead of `/back_admin/login` (Spring Controller path)
- Simplified `gotoLogin()` — no language parameter or complex URL building

### 3. Update Database CLASS_ACL References

The `EWA_CFG` table stores `CLASS_ACL` values for each XML configuration item. Replace all references from `com.gdxsoft.web.acl.*` to the new class:

```java
// Connect to HSQLDB (file or server mode)
try (Connection conn = DriverManager.getConnection(url, "sa", "")) {
    String old1 = "com.gdxsoft.web.acl.BackAdminImpl";
    String new1 = "com.gdxsoft.emp.demo.acl.BackAdminImpl";
    String old2 = "com.gdxsoft.web.acl.BusinessImpl";

    try (Statement st = conn.createStatement();
         ResultSet rs = st.executeQuery(
             "SELECT XMLNAME, ITEMNAME, CLASS_ACL"
             + " FROM EWA_CFG WHERE CLASS_ACL IS NOT NULL"
             + " AND CLASS_ACL LIKE '%com.gdxsoft.web.acl%'")) {

        while (rs.next()) {
            String xmlname = rs.getString(1);
            String itemname = rs.getString(2);
            String acl = rs.getString(3);
            String newAcl = acl.replace(old1, new1).replace(old2, new1);
            if (!newAcl.equals(acl)) {
                try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE EWA_CFG SET CLASS_ACL=? WHERE XMLNAME=? AND ITEMNAME=?")) {
                    ps.setString(1, newAcl);
                    ps.setString(2, xmlname);
                    ps.setString(3, itemname);
                    ps.executeUpdate();
                }
            }
        }
    }
    conn.commit();
}
```

### 4. Verify

Restart the server and check logs:

```bash
# No more errors
grep "BackAdminImpl" app.log
# → (empty)
```

## Implementation Notes

### Package Naming

Choose a package that is **not** `com.gdxsoft.web.acl` — avoid conflicting with the real `emp-script-web` project. Use the demo project's own package:

```java
package com.gdxsoft.emp.demo.acl;  // good
// NOT: package com.gdxsoft.web.acl;  // would conflict with emp-script-web
```

### What CLASS_ACL Values to Expect

Typical CLASS_ACL values in the database:

| Value | Source | Replace With |
|-------|--------|-------------|
| `com.gdxsoft.web.acl.BackAdminImpl` | `emp-script-web` | `com.gdxsoft.emp.demo.acl.BackAdminImpl` |
| `com.gdxsoft.web.acl.BusinessImpl` | `emp-script-web` | `com.gdxsoft.emp.demo.acl.BackAdminImpl` (or create a separate impl) |

**Note:** `BusinessImpl` is used by business-facing XML configs. For a demo where all users are admins, mapping both to `BackAdminImpl` is sufficient. For granular control, create separate implementations.

### Database Updates Are Persisted

CLASS_ACL is stored in the database (`EWA_CFG` table). If you use the zip-based data distribution pattern:
1. Update the CLASS_ACL references
2. Re-zip the data: `zip -r hsqldb-data.zip hsqldb/ -x "*.lck" "*.log" "*.tmp" "*.tmp/*"`
3. Replace `hsqldb-data.zip` in git

The next developer who clones and runs will automatically get the updated CLASS_ACL references.

### How CLASS_ACL is Loaded

The EWA framework loads the ACL class dynamically by name using `UObjectValue.loadClass()`:

```java
// In UObjectValue (pseudocode)
Class<?> aclClass = Class.forName(className);
IAcl acl = (IAcl) aclClass.getDeclaredConstructor().newInstance();
acl.setRequestValue(rv);
acl.setXmlName(xmlName);
acl.setItemName(itemName);
if (!acl.canRun()) {
    // redirect to acl.getGoToUrl()
}
```

This is why changing the class name in the database is sufficient — the framework loads it by reflection at runtime.

## Alternative Approach: Package Override

If you cannot update the database (e.g., it's shared across environments), create the class at the EXACT original package path:

```java
// Match the original package exactly — no database changes needed
package com.gdxsoft.web.acl;

public class BackAdminImpl extends AclBase implements IAcl { ... }
```

**Downside:** These files would need to be in every project that uses emp-script, making the workaround less centralized.

## Verification Checklist

- [ ] `BackAdminImpl.java` and `AclBase.java` compile without errors
- [ ] Database `CLASS_ACL` column has been updated
- [ ] No `loadClass com.gdxsoft.web.acl.BackAdminImpl` errors in server logs
- [ ] Admin pages (`/back_admin/` login + index) load correctly
- [ ] Session-based authentication works (login → page access → logout)
