# ewa_test and ewa_block_test Usage Guide

## Overview

`ewa_test` and `ewa_block_test` are **conditional SQL execution** features provided by the emp-script framework. They allow adding logical judgments in SQL statements via comments, dynamically controlling the inclusion or exclusion of SQL statements.

These two directives can be used in both **SQL statements** and **Frame HTML**, making them powerful tools for implementing dynamic queries.

---

## 1. ewa_test - Single-Line Conditional Test

### 1.1 Description

`ewa_test` is used to **conditionally include/exclude the next line of SQL**. Only when the condition is `true` will the next line of SQL be included in the final executed SQL.

### 1.2 Syntax

```sql
-- ewa_test <condition_expression>
SQL line to execute
```

**Condition Expressions**:
- Use `@parameter_name` to reference parameters in RequestValue
- Support SQL logical operators: `is null`, `is not null`, `=`, `<>`, `and`, `or`, etc.
- Conditions are evaluated via the HSQLDB engine

### 1.3 How It Works

```
Parse SQL split by line
    ↓
Encounter -- ewa_test
    ↓
Extract condition expression
    ↓
Replace @parameter with actual value
    ↓
Evaluate logic via HSQLDB
    ↓
    ├─ true  → ✅ Include next SQL line
    └─ false → ❌ Exclude next SQL line
```

### 1.4 Usage Examples

#### Example 1: Optional WHERE Conditions

```sql
SELECT * FROM users
WHERE status = 'ACTIVE'
-- ewa_test @name is not null
  AND name LIKE @name
-- ewa_test @age is not null
  AND age > @age_int
ORDER BY id
```

**Execution Scenarios**:

| Parameters | Final SQL |
|------|---------|
| No parameters | `SELECT * FROM users WHERE status = 'ACTIVE' ORDER BY id` |
| `name=Smith` | `SELECT * FROM users WHERE status = 'ACTIVE' AND name LIKE 'Smith' ORDER BY id` |
| `name=Smith`, `age=18` | `SELECT * FROM users WHERE status = 'ACTIVE' AND name LIKE 'Smith' AND age > 18 ORDER BY id` |

#### Example 2: Multi-Condition Combination

```sql
SELECT u.*, o.order_count
FROM users u
LEFT JOIN (
    SELECT user_id, COUNT(*) order_count
    FROM orders
    -- ewa_test @start_date is not null
    WHERE order_date >= @start_date
    -- ewa_test @end_date is not null
      AND order_date <= @end_date
    GROUP BY user_id
) o ON u.id = o.user_id
WHERE u.status = 'ACTIVE'
```

#### Example 3: Dynamic Sorting

```sql
SELECT * FROM products
WHERE category_id = @category_id_int
-- ewa_test @sort_by = 'price'
ORDER BY price ASC
-- ewa_test @sort_by = 'name'
ORDER BY name ASC
-- ewa_test @sort_by = 'date'
ORDER BY create_date DESC
```

#### Example 4: Empty Condition (Always Execute)

```sql
SELECT * FROM users
WHERE 1=1
-- ewa_test
  AND status = @status
```

When `-- ewa_test` has no condition expression after it, it always returns `true`, and the next line is always included.

---

## 2. ewa_block_test - Block Conditional Test

### 2.1 Description

`ewa_block_test` is used to **conditionally include/exclude multi-line SQL blocks**. Only when the condition is `true` will the entire block of SQL be included.

### 2.2 Syntax

```sql
-- ewa_block_test <condition_expression>
SQL line 1
SQL line 2
...
-- ewa_block_test
```

**Description**:
- The first `-- ewa_block_test <condition>` starts the block
- The last `-- ewa_block_test` (no condition) ends the block
- All SQL lines within the block are either all included or all excluded

### 2.3 How It Works

```
Parse SQL split by line
    ↓
Encounter -- ewa_block_test <condition>
    ↓
Extract and evaluate condition expression
    ↓
    ├─ true  → Record lastResult = true
    │           ↓
    │         Include all SQL in the block
    │           ↓
    │         Encounter -- ewa_block_test (end)
    │           ↓
    │         Reset lastResult = true
    │
    └─ false → Record lastResult = false
                ↓
               Exclude all SQL in the block
                ↓
               Encounter -- ewa_block_test (end)
                ↓
               Reset lastResult = true
```

### 2.4 Usage Examples

#### Example 1: Optional JOIN Block

```sql
SELECT u.*, p.product_name, c.category_name
FROM users u
-- ewa_block_test @include_orders is not null
LEFT JOIN orders o ON u.id = o.user_id
LEFT JOIN products p ON o.product_id = p.id
LEFT JOIN categories c ON p.category_id = c.id
-- ewa_block_test
WHERE u.status = 'ACTIVE'
```

**Execution Scenarios**:

| Parameters | Final SQL |
|------|---------|
| No `include_orders` | `SELECT u.* FROM users u WHERE u.status = 'ACTIVE'` |
| `include_orders=1` | Full SQL with all JOINs included |

#### Example 2: Optional WHERE Condition Block

```sql
SELECT * FROM orders
WHERE 1=1
-- ewa_block_test @user_id is not null
  AND user_id = @user_id_int
  AND status IN ('PAID', 'SHIPPED', 'COMPLETED')
  AND order_date >= DATE_SUB(NOW(), INTERVAL 30 DAY)
-- ewa_block_test
ORDER BY order_date DESC
```

#### Example 3: Nested Usage

```sql
SELECT *
FROM users u
-- ewa_block_test @detailed is not null
LEFT JOIN profiles p ON u.id = p.user_id
LEFT JOIN (
    SELECT user_id, COUNT(*) cnt
    FROM orders
    -- ewa_test @min_orders is not null
    HAVING COUNT(*) >= @min_orders_int
) o ON u.id = o.user_id
-- ewa_block_test
WHERE u.status = 'ACTIVE'
```

---

## 3. Using in Frame HTML

### 3.1 Syntax Differences in HTML

When using `ewa_test` in HTML, use **HTML comments** instead of SQL comments:

```html
<!-- ewa_test -->        ✅ Used in HTML
-- ewa_test              ✅ Used in SQL
```

### 3.2 Processing Flow

In the `FrameBase.java` `createItemHtmlsByFrameHtml()` method, the HTML template is processed in the following order:

```java
public String createItemHtmlsByFrameHtml(String template, String frameTag) throws Exception {
    // Step 1: Process ewa_block_test condition evaluation
    template = super.createHtmlByEwaBlockTest(template);
    // Step 2: Process ewa_test condition evaluation
    template = super.createHtmlByEwaTest(template);
    
    // Step 3: Replace @parameters with actual field values
    // ...
}
```

### 3.3 Using in Grid/ListFrame SQL

In the Frame definition of XML configuration files, SQL queries can use `ewa_test`:

```xml
<frame name="userList" type="LISTFRAME">
    <sql>
        SELECT u.id, u.name, u.email, u.status
        FROM users u
        -- ewa_test @search_name is not null
        WHERE u.name LIKE '%' + @search_name + '%'
        -- ewa_test @status is not null
          AND u.status = @status
        ORDER BY u.create_date DESC
    </sql>
</frame>
```

**Calling Methods**:
```html
<!-- Query all users -->
<a href="?XMLNAME=user_config&ITEMNAME=userList">All Users</a>

<!-- Search by name -->
<a href="?XMLNAME=user_config&ITEMNAME=userList&search_name=Smith">
    Users with surname Smith
</a>

<!-- Filter by status -->
<a href="?XMLNAME=user_config&ITEMNAME=userList&status=ACTIVE">
    Active Users
</a>

<!-- Combined conditions -->
<a href="?XMLNAME=user_config&ITEMNAME=userList&search_name=Smith&status=ACTIVE">
    Active users with surname Smith
</a>
```

### 3.4 Using in Custom Actions

```xml
<action name="search" type="JSON">
    <sql>
        SELECT * FROM products
        WHERE 1=1
        -- ewa_test @category is not null
          AND category_id = @category_int
        -- ewa_test @min_price is not null
          AND price >= @min_price
        -- ewa_test @max_price is not null
          AND price <= @max_price
        -- ewa_block_test @in_stock is not null
          AND stock > 0
          AND status = 'AVAILABLE'
        -- ewa_block_test
        ORDER BY 
        -- ewa_test @sort = 'price_asc'
          price ASC
        -- ewa_test @sort = 'price_desc'
          price DESC
        -- ewa_test @sort = 'newest'
          create_date DESC
    </sql>
</action>
```

### 3.5 In Form Data Loading

```xml
<frame name="editUser" type="FRAME">
    <sql>
        SELECT u.*, r.role_name
        FROM users u
        LEFT JOIN roles r ON u.role_id = r.id
        WHERE u.id = @id_int
        -- ewa_block_test @include_audit is not null
        LEFT JOIN audit_logs a ON u.id = a.user_id
        LEFT JOIN (
            SELECT user_id, MAX(action_date) last_action
            FROM audit_logs
            GROUP BY user_id
        ) al ON u.id = al.user_id
        -- ewa_block_test
    </sql>
</frame>
```

### 3.6 Using ewa_test in HTML Templates

In Frame HTML templates, `ewa_test` can be used to control the display of HTML elements:

#### Example 1: Conditional Display of HTML Elements

```html
<div class="user-card">
    <h3>@NAME</h3>
    <p>Email: @EMAIL</p>
    
    <!-- ewa_test @PHONE is not null -->
    <p>Phone: @PHONE</p>
    
    <!-- ewa_test @ADDRESS is not null -->
    <p>Address: @ADDRESS</p>
    
    <!-- ewa_test @BIRTHDAY is not null -->
    <p>Birthday: @BIRTHDAY</p>
</div>
```

#### Example 2: Block-Level Conditional Display

```html
<div class="order-detail">
    <h2>Order @ORDER_NO</h2>
    <p>Status: @STATUS</p>
    
    <!-- ewa_block_test @STATUS = 'PAID' or @STATUS = 'SHIPPED' -->
    <div class="payment-info">
        <h3>Payment Information</h3>
        <p>Payment Method: @PAYMENT_METHOD</p>
        <p>Payment Time: @PAYMENT_TIME</p>
        <p>Transaction ID: @TRANSACTION_ID</p>
    </div>
    <!-- ewa_block_test -->
    
    <!-- ewa_block_test @STATUS = 'SHIPPED' or @STATUS = 'COMPLETED' -->
    <div class="shipping-info">
        <h3>Shipping Information</h3>
        <p>Courier: @COURIER</p>
        <p>Tracking Number: @TRACKING_NO</p>
    </div>
    <!-- ewa_block_test -->
</div>
```

#### Example 3: Dynamic Styles and Class Names

```html
<div class="product-item">
    <!-- ewa_test @DISCOUNT > 0 -->
    <span class="discount-badge">-@DISCOUNT%</span>
    
    <!-- ewa_test @STOCK = 0 -->
    <span class="out-of-stock-badge">Sold Out</span>
    
    <!-- ewa_test @IS_NEW = 1 -->
    <span class="new-badge">New</span>
    
    <h3>@PRODUCT_NAME</h3>
    <p class="price <!-- ewa_test @DISCOUNT > 0 -->text-red<!-- ewa_test -->">
        ¥@PRICE
    </p>
</div>
```

#### Example 4: Nested Conditions

```html
<div class="user-profile">
    <h2>@USERNAME</h2>
    
    <!-- ewa_block_test @USER_TYPE = 'VIP' -->
    <div class="vip-section">
        <h3>VIP Exclusive Benefits</h3>
        
        <!-- ewa_test @VIP_LEVEL >= 3 -->
        <p>Enjoy Diamond VIP Service</p>
        
        <!-- ewa_test @VIP_LEVEL >= 2 and @VIP_LEVEL < 3 -->
        <p>Enjoy Gold VIP Service</p>
        
        <!-- ewa_test @VIP_LEVEL < 2 -->
        <p>Enjoy Silver VIP Service</p>
        
        <!-- ewa_block_test @VIP_EXPIRE_DATE is not null -->
        <p>Expiration: @VIP_EXPIRE_DATE</p>
        <!-- ewa_block_test -->
    </div>
    <!-- ewa_block_test -->
</div>
```

### 3.7 Parameter Sources in HTML

In the `createHtmlByEwaTest()` method, parameter values are obtained from `ItemValues`:

```java
private void ewaTestValues(String len2, RequestValue rv, ItemValues iv) {
    MListStr al = Utils.getParameters(len2, "@");
    
    for (int i = 0; i < al.size(); i++) {
        String paramName = al.get(i);
        try {
            // Get parameter value from ItemValues
            String paramValue = iv.getValue(paramName, paramName);
            rv.addOrUpdateValue(paramName, paramValue);
        } catch (Exception e) {
            rv.addOrUpdateValue(paramName, null);
            LOGGER.warn("Parameter not found: " + paramName);
        }
    }
}
```

**Parameter Source Priority**:
1. Fields from Frame query results
2. Parameters passed via URL/FORM
3. Parameters defined in SESSION/SYSTEM

---

## 4. Supported Logical Expressions

### 4.1 Null Checks

```sql
-- ewa_test @param is null         -- Parameter does not exist or is null
-- ewa_test @param is not null     -- Parameter exists and is not null
-- ewa_test @param = ''            -- Parameter is an empty string
-- ewa_test @param <> ''           -- Parameter is not an empty string
```

### 4.2 Value Comparisons

```sql
-- ewa_test @age > 18              -- Greater than
-- ewa_test @age >= 18             -- Greater than or equal to
-- ewa_test @age < 60              -- Less than
-- ewa_test @age <= 60             -- Less than or equal to
-- ewa_test @status = 'ACTIVE'     -- Equal
-- ewa_test @status <> 'DISABLED'  -- Not equal
```

### 4.3 Logical Operations

```sql
-- ewa_test @name is not null and @age > 18
-- ewa_test @status = 'A' or @status = 'B'
-- ewa_test @type in ('X', 'Y', 'Z')
-- ewa_test @flag = 1 and (@type is null or @type = 'ADMIN')
```

### 4.4 Function Calls

```sql
-- ewa_test LEN(@name) > 0
-- ewa_test DATE(@date) is not null
-- ewa_test CAST(@num AS INT) > 10
```

---

## 5. Parameter Type Suffixes

When using in `ewa_test`, be aware of parameter types:

```sql
-- Integer comparison
-- ewa_test @age_int > 18

-- String comparison (requires quotes)
-- ewa_test @name = 'John'

-- Date comparison
-- ewa_test @start_date is not null

-- Null check (no type suffix)
-- ewa_test @param is not null
```

**Recommended Practices**:
- Use type suffixes to specify parameter types: `@age_int`, `@price_number`
- Pay attention to quote handling for string comparisons
- Use `is not null` instead of direct comparison for dates

---

## 6. Real-World Application Scenarios

### 6.1 Advanced Search Functionality

```sql
SELECT * FROM products
WHERE 1=1
-- ewa_test @keyword is not null
  AND (name LIKE '%' + @keyword + '%' 
       OR description LIKE '%' + @keyword + '%')
-- ewa_test @category is not null
  AND category_id = @category_int
-- ewa_test @min_price is not null
  AND price >= @min_price
-- ewa_test @max_price is not null
  AND price <= @max_price
-- ewa_block_test @brand is not null
  AND brand_id IN (
      SELECT id FROM brands 
      WHERE name LIKE '%' + @brand + '%'
  )
-- ewa_block_test
-- ewa_test @in_stock is not null
  AND stock > 0
ORDER BY 
-- ewa_test @sort = 'price'
  price ASC
-- ewa_test @sort = 'popular'
  sales_count DESC
-- ewa_test @sort = 'newest'
  create_date DESC
```

### 6.2 Permission Control

```sql
SELECT * FROM documents
WHERE status = 'PUBLIC'
-- ewa_block_test @user_id is not null
  OR (
      status = 'PRIVATE' 
      AND owner_id = @user_id_int
  )
  OR (
      status = 'TEAM'
      AND team_id IN (
          SELECT team_id FROM team_members 
          WHERE user_id = @user_id_int
      )
  )
-- ewa_block_test
```

### 6.3 Multi-Language Support

```sql
SELECT 
    p.id,
-- ewa_test @lang = 'en'
    p.name_en AS name,
    p.description_en AS description
-- ewa_test @lang = 'zh'
    p.name_zh AS name,
    p.description_zh AS description
-- ewa_test @lang = 'ja'
    p.name_ja AS name,
    p.description_ja AS description
FROM products p
WHERE p.id = @id_int
```

### 6.4 Dynamic Report Columns

```sql
SELECT 
    u.name,
    u.email
-- ewa_block_test @show_orders is not null
    , COUNT(DISTINCT o.id) AS order_count
    , SUM(o.amount) AS total_amount
    , AVG(o.amount) AS avg_amount
-- ewa_block_test
-- ewa_block_test @show_activity is not null
    , MAX(a.action_date) AS last_activity
    , COUNT(DISTINCT a.id) AS action_count
-- ewa_block_test
FROM users u
-- ewa_block_test @show_orders is not null
LEFT JOIN orders o ON u.id = o.user_id
-- ewa_block_test
-- ewa_block_test @show_activity is not null
LEFT JOIN activities a ON u.id = a.user_id
-- ewa_block_test
WHERE u.status = 'ACTIVE'
GROUP BY u.id, u.name, u.email
```

---

## 7. Debugging and Logging

### 7.1 Debug Output

After enabling SQL debugging, you can see the parsing results of `ewa_test`:

```
SQL: [createSqlByEwaTest] Start excute query. 

SELECT * FROM users
WHERE status = 'ACTIVE'
-- ewa_test<true> @name is not null (@name = 'Smith')
  AND name LIKE 'Smith'
-- ewa_test<false> @age is not null (@age = null)
  AND age > 18
ORDER BY id
```

### 7.2 Comment Transformation

`ewa_test` comments are transformed into comments with result markers:

```sql
-- ewa_test<true> @name is not null (Smith)
-- ewa_test<false> @age is not null (null)
-- ewa_block_test<true> @include_orders is not null (1)
-- ewa_block_test<false> @detailed is not null (null)
```

This is very useful for debugging, as you can see the evaluation result of each condition.

---

## 8. Notes and Best Practices

### 8.1 Maintain Indentation

```sql
-- ✅ Recommended: Maintain consistent SQL indentation
SELECT * FROM users
WHERE 1=1
-- ewa_test @name is not null
  AND name LIKE @name    -- Two-space indentation
-- ewa_test @age is not null
  AND age > @age_int

-- ❌ Not Recommended: Messy indentation
SELECT * FROM users
WHERE 1=1
-- ewa_test @name is not null
AND name LIKE @name      -- No indentation
```

### 8.2 Mutually Exclusive Conditions

```sql
-- ✅ Recommended: Use mutually exclusive conditions
ORDER BY 
-- ewa_test @sort = 'price_asc'
  price ASC
-- ewa_test @sort = 'price_desc'
  price DESC
-- ewa_test @sort is null or @sort = 'date'
  create_date DESC

-- ❌ Not Recommended: Multiple conditions being true simultaneously causing syntax errors
ORDER BY 
-- ewa_test @sort is not null
  @sort   -- This will error; ORDER BY cannot be followed by a parameter
```

### 8.3 Block End Marker

```sql
-- ✅ Recommended: Explicit end marker
-- ewa_block_test @condition is not null
SQL line 1
SQL line 2
-- ewa_block_test

-- ❌ Not Recommended: Forgetting the end marker (block continues to end of file)
-- ewa_block_test @condition is not null
SQL line 1
SQL line 2
(no end marker)
```

### 8.4 Performance Considerations

```sql
-- ✅ Recommended: Use ewa_test before JOIN
SELECT * FROM users u
-- ewa_test @include_orders is not null
LEFT JOIN orders o ON u.id = o.user_id

-- ❌ Not Recommended: ewa_test in WHERE may cause full table scan
SELECT * FROM users u
LEFT JOIN orders o ON u.id = o.user_id
WHERE 
-- ewa_test @user_id is null OR u.id = @user_id_int
```

### 8.5 Parameter Naming

```sql
-- ✅ Recommended: Use descriptive parameter names
-- ewa_test @include_orders is not null
-- ewa_test @min_price is not null
-- ewa_test @sort_by = 'date'

-- ❌ Not Recommended: Ambiguous parameter names
-- ewa_test @p1 is not null
-- ewa_test @x = 'y'
```

---

## 9. FAQ

### Q1: What's the difference between ewa_test and ewa_block_test?

| Feature | ewa_test | ewa_block_test |
|------|----------|----------------|
| Scope | Next SQL line | Multi-line SQL block |
| Syntax | `-- ewa_test <condition>` | `-- ewa_block_test <condition>` ... `-- ewa_block_test` |
| Use Case | Single optional condition | Multiple related optional SQL lines |

### Q2: Where is the condition expression evaluated?

Condition expressions are evaluated via the **HSQLDB in-memory database**, independent of the actual configured database.

### Q3: Can SQL functions be used?

Yes, but you need to use function syntax supported by HSQLDB.

### Q4: How to use in HTML pages?

Use directly within the Frame `<sql>` tag in XML configuration. Parameters are passed via URL or form.

### Q5: What is the performance impact?

- `ewa_test` only evaluates once during SQL parsing
- It does not execute repeatedly on each query
- No impact on query performance

---

## 10. Summary

| Feature | Description |
|------|------|
| **ewa_test** | Conditionally includes the next SQL line |
| **ewa_block_test** | Conditionally includes a multi-line SQL block |
| **Supported Locations** | SQL queries, Frame XML configuration |
| **Evaluation Engine** | HSQLDB in-memory database |
| **Parameter Sources** | RequestValue (FORM/QUERY/SESSION/SYSTEM) |
| **Debug Support** | Comments show evaluation result `<true/false>` |

**Core Benefits**:
- ✅ Dynamic SQL without programming — achievable via configuration
- ✅ Improved SQL reusability, reduced duplicate code
- ✅ Easy to maintain with clear logic
- ✅ Supports complex condition combinations
- ✅ Debug-friendly with visualized results
