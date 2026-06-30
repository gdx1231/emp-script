# ewa_test 和 ewa_block_test 使用指南

## 概述

`ewa_test` 和 `ewa_block_test` 是 emp-script 框架提供的**条件化SQL执行**功能，允许在SQL语句中通过注释方式添加逻辑判断，动态控制SQL语句的包含或排除。

这两个指令在**SQL语句**和**Frame HTML**中都可以使用，是实现动态查询的强大工具。

---

## 一、ewa_test - 单行条件测试

### 1.1 功能说明

`ewa_test` 用于**条件化包含/排除下一行SQL**。只有当条件为 `true` 时，下一行SQL才会被包含在最终执行的SQL中。

### 1.2 语法

```sql
-- ewa_test <条件表达式>
要执行的SQL行
```

**条件表达式**：
- 可以使用 `@参数名` 引用RequestValue中的参数
- 支持SQL逻辑运算符：`is null`, `is not null`, `=`, `<>`, `and`, `or` 等
- 条件通过HSQLDB引擎执行判断

### 1.3 工作原理

```
解析SQL按行分割
    ↓
遇到 -- ewa_test
    ↓
提取条件表达式
    ↓
替换 @参数 为实际值
    ↓
通过HSQLDB执行逻辑判断
    ↓
    ├─ true  → ✅ 包含下一行SQL
    └─ false → ❌ 排除下一行SQL
```

### 1.4 使用示例

#### 示例1: 可选的WHERE条件

```sql
SELECT * FROM users
WHERE status = 'ACTIVE'
-- ewa_test @name is not null
  AND name LIKE @name
-- ewa_test @age is not null
  AND age > @age_int
ORDER BY id
```

**执行场景**：

| 参数 | 最终SQL |
|------|---------|
| 无参数 | `SELECT * FROM users WHERE status = 'ACTIVE' ORDER BY id` |
| `name=张` | `SELECT * FROM users WHERE status = 'ACTIVE' AND name LIKE '张' ORDER BY id` |
| `name=张`, `age=18` | `SELECT * FROM users WHERE status = 'ACTIVE' AND name LIKE '张' AND age > 18 ORDER BY id` |

#### 示例2: 多条件组合

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

#### 示例3: 动态排序

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

#### 示例4: 空条件（总是执行）

```sql
SELECT * FROM users
WHERE 1=1
-- ewa_test
  AND status = @status
```

`-- ewa_test` 后面无条件表达式时，始终返回 `true`，下一行总是被包含。

---

## 二、ewa_block_test - 块条件测试

### 2.1 功能说明

`ewa_block_test` 用于**条件化包含/排除多行SQL块**。只有当条件为 `true` 时，整个块的SQL才会被包含。

### 2.2 语法

```sql
-- ewa_block_test <条件表达式>
SQL行1
SQL行2
...
-- ewa_block_test
```

**说明**：
- 第一个 `-- ewa_block_test <条件>` 开始块
- 最后一个 `-- ewa_block_test`（无条件）结束块
- 块内的所有SQL行要么全部包含，要么全部排除

### 2.3 工作原理

```
解析SQL按行分割
    ↓
遇到 -- ewa_block_test <条件>
    ↓
提取条件表达式并执行判断
    ↓
    ├─ true  → 记录 lastResult = true
    │           ↓
    │         包含块内所有SQL
    │           ↓
    │         遇到 -- ewa_block_test (结束)
    │           ↓
    │         重置 lastResult = true
    │
    └─ false → 记录 lastResult = false
                ↓
              排除块内所有SQL
                ↓
              遇到 -- ewa_block_test (结束)
                ↓
              重置 lastResult = true
```

### 2.4 使用示例

#### 示例1: 可选的JOIN块

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

**执行场景**：

| 参数 | 最终SQL |
|------|---------|
| 无 `include_orders` | `SELECT u.* FROM users u WHERE u.status = 'ACTIVE'` |
| `include_orders=1` | 包含所有JOIN的完整SQL |

#### 示例2: 可选的WHERE条件块

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

#### 示例3: 嵌套使用

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

## 三、在Frame HTML中使用

### 3.1 HTML中的语法差异

在HTML中使用`ewa_test`时，使用**HTML注释**而非SQL注释：

```html
<!-- ewa_test -->        ✅ HTML中使用
-- ewa_test              ✅ SQL中使用
```

### 3.2 处理流程

在 `FrameBase.java` 的 `createItemHtmlsByFrameHtml()` 方法中，HTML模板按以下顺序处理：

```java
public String createItemHtmlsByFrameHtml(String template, String frameTag) throws Exception {
    // 第一步：处理 ewa_block_test 条件判断
    template = super.createHtmlByEwaBlockTest(template);
    // 第二步：处理 ewa_test 条件判断
    template = super.createHtmlByEwaTest(template);
    
    // 第三步：替换@参数为实际字段值
    // ...
}
```

### 3.3 在Grid/ListFrame的SQL中使用

在XML配置文件的Frame定义中，SQL查询可以使用 `ewa_test`：

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

**调用方式**：
```html
<!-- 查询所有用户 -->
<a href="?XMLNAME=user_config&ITEMNAME=userList">所有用户</a>

<!-- 按名称搜索 -->
<a href="?XMLNAME=user_config&ITEMNAME=userList&search_name=张">
    姓张的用户
</a>

<!-- 按状态过滤 -->
<a href="?XMLNAME=user_config&ITEMNAME=userList&status=ACTIVE">
    活跃用户
</a>

<!-- 组合条件 -->
<a href="?XMLNAME=user_config&ITEMNAME=userList&search_name=张&status=ACTIVE">
    活跃的张姓用户
</a>
```

### 3.4 在自定义操作中使用

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

### 3.5 在表单数据加载中

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

### 3.6 在HTML模板中使用ewa_test

在Frame的HTML模板中，可以使用`ewa_test`控制HTML元素的显示：

#### 示例1: 条件显示HTML元素

```html
<div class="user-card">
    <h3>@NAME</h3>
    <p>邮箱: @EMAIL</p>
    
    <!-- ewa_test @PHONE is not null -->
    <p>电话: @PHONE</p>
    
    <!-- ewa_test @ADDRESS is not null -->
    <p>地址: @ADDRESS</p>
    
    <!-- ewa_test @BIRTHDAY is not null -->
    <p>生日: @BIRTHDAY</p>
</div>
```

#### 示例2: 块级条件显示

```html
<div class="order-detail">
    <h2>订单 @ORDER_NO</h2>
    <p>状态: @STATUS</p>
    
    <!-- ewa_block_test @STATUS = 'PAID' or @STATUS = 'SHIPPED' -->
    <div class="payment-info">
        <h3>支付信息</h3>
        <p>支付方式: @PAYMENT_METHOD</p>
        <p>支付时间: @PAYMENT_TIME</p>
        <p>交易号: @TRANSACTION_ID</p>
    </div>
    <!-- ewa_block_test -->
    
    <!-- ewa_block_test @STATUS = 'SHIPPED' or @STATUS = 'COMPLETED' -->
    <div class="shipping-info">
        <h3>物流信息</h3>
        <p>快递公司: @COURIER</p>
        <p>快递单号: @TRACKING_NO</p>
    </div>
    <!-- ewa_block_test -->
</div>
```

#### 示例3: 动态样式和类名

```html
<div class="product-item">
    <!-- ewa_test @DISCOUNT > 0 -->
    <span class="discount-badge">优惠 @DISCOUNT%</span>
    
    <!-- ewa_test @STOCK = 0 -->
    <span class="out-of-stock-badge">售罄</span>
    
    <!-- ewa_test @IS_NEW = 1 -->
    <span class="new-badge">新品</span>
    
    <h3>@PRODUCT_NAME</h3>
    <p class="price <!-- ewa_test @DISCOUNT > 0 -->text-red<!-- ewa_test -->">
        ¥@PRICE
    </p>
</div>
```

#### 示例4: 嵌套条件

```html
<div class="user-profile">
    <h2>@USERNAME</h2>
    
    <!-- ewa_block_test @USER_TYPE = 'VIP' -->
    <div class="vip-section">
        <h3>VIP专属权益</h3>
        
        <!-- ewa_test @VIP_LEVEL >= 3 -->
        <p>尊享钻石VIP服务</p>
        
        <!-- ewa_test @VIP_LEVEL >= 2 and @VIP_LEVEL < 3 -->
        <p>尊享黄金VIP服务</p>
        
        <!-- ewa_test @VIP_LEVEL < 2 -->
        <p>尊享白银VIP服务</p>
        
        <!-- ewa_block_test @VIP_EXPIRE_DATE is not null -->
        <p>到期时间: @VIP_EXPIRE_DATE</p>
        <!-- ewa_block_test -->
    </div>
    <!-- ewa_block_test -->
</div>
```

### 3.7 HTML中的参数来源

在`createHtmlByEwaTest()`方法中，参数值从`ItemValues`获取：

```java
private void ewaTestValues(String len2, RequestValue rv, ItemValues iv) {
    MListStr al = Utils.getParameters(len2, "@");
    
    for (int i = 0; i < al.size(); i++) {
        String paramName = al.get(i);
        try {
            // 从ItemValues中获取参数值
            String paramValue = iv.getValue(paramName, paramName);
            rv.addOrUpdateValue(paramName, paramValue);
        } catch (Exception e) {
            rv.addOrUpdateValue(paramName, null);
            LOGGER.warn("Parameter not found: " + paramName);
        }
    }
}
```

**参数来源优先级**：
1. Frame查询结果中的字段
2. URL/FORM传递的参数
3. SESSION/SYSTEM中定义的参数

---

## 四、支持的逻辑表达式

### 4.1 空值检查

```sql
-- ewa_test @param is null         -- 参数不存在或为null
-- ewa_test @param is not null     -- 参数存在且不为null
-- ewa_test @param = ''            -- 参数为空字符串
-- ewa_test @param <> ''           -- 参数不为空字符串
```

### 4.2 值比较

```sql
-- ewa_test @age > 18              -- 大于
-- ewa_test @age >= 18             -- 大于等于
-- ewa_test @age < 60              -- 小于
-- ewa_test @age <= 60             -- 小于等于
-- ewa_test @status = 'ACTIVE'     -- 等于
-- ewa_test @status <> 'DISABLED'  -- 不等于
```

### 4.3 逻辑运算

```sql
-- ewa_test @name is not null and @age > 18
-- ewa_test @status = 'A' or @status = 'B'
-- ewa_test @type in ('X', 'Y', 'Z')
-- ewa_test @flag = 1 and (@type is null or @type = 'ADMIN')
```

### 4.4 函数调用

```sql
-- ewa_test LEN(@name) > 0
-- ewa_test DATE(@date) is not null
-- ewa_test CAST(@num AS INT) > 10
```

---

## 五、参数类型后缀

在 `ewa_test` 中使用时，需要注意参数类型：

```sql
-- 整数比较
-- ewa_test @age_int > 18

-- 字符串比较（需要引号）
-- ewa_test @name = 'John'

-- 日期比较
-- ewa_test @start_date is not null

-- 空值检查（无类型后缀）
-- ewa_test @param is not null
```

**推荐做法**：
- 使用类型后缀明确参数类型：`@age_int`, `@price_number`
- 字符串比较时注意引号处理
- 日期比较使用 `is not null` 而非直接比较

---

## 六、实际应用场景

### 6.1 高级搜索功能

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

### 6.2 权限控制

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

### 6.3 多语言支持

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

### 6.4 报表动态列

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

## 七、调试和日志

### 7.1 调试输出

启用SQL调试后，可以看到 `ewa_test` 的解析结果：

```
SQL: [createSqlByEwaTest] Start excute query. 

SELECT * FROM users
WHERE status = 'ACTIVE'
-- ewa_test<true> @name is not null (@name = '张')
  AND name LIKE '张'
-- ewa_test<false> @age is not null (@age = null)
  AND age > 18
ORDER BY id
```

### 7.2 注释转换

`ewa_test` 注释会被转换为带结果标记的注释：

```sql
-- ewa_test<true> @name is not null (张)
-- ewa_test<false> @age is not null (null)
-- ewa_block_test<true> @include_orders is not null (1)
-- ewa_block_test<false> @detailed is not null (null)
```

这在调试时非常有用，可以看到每个条件的判断结果。

---

## 八、注意事项和最佳实践

### 8.1 缩进保持

```sql
-- ✅ 推荐：保持SQL缩进一致
SELECT * FROM users
WHERE 1=1
-- ewa_test @name is not null
  AND name LIKE @name    -- 两个空格缩进
-- ewa_test @age is not null
  AND age > @age_int

-- ❌ 不推荐：缩进混乱
SELECT * FROM users
WHERE 1=1
-- ewa_test @name is not null
AND name LIKE @name      -- 无缩进
```

### 8.2 条件互斥

```sql
-- ✅ 推荐：使用互斥条件
ORDER BY 
-- ewa_test @sort = 'price_asc'
  price ASC
-- ewa_test @sort = 'price_desc'
  price DESC
-- ewa_test @sort is null or @sort = 'date'
  create_date DESC

-- ❌ 不推荐：多个条件同时为true导致语法错误
ORDER BY 
-- ewa_test @sort is not null
  @sort   -- 这会出错，ORDER BY后不能跟参数
```

### 8.3 块结束标记

```sql
-- ✅ 推荐：明确结束标记
-- ewa_block_test @condition is not null
SQL行1
SQL行2
-- ewa_block_test

-- ❌ 不推荐：忘记结束标记（块会持续到文件末尾）
-- ewa_block_test @condition is not null
SQL行1
SQL行2
（没有结束标记）
```

### 8.4 性能考虑

```sql
-- ✅ 推荐：在JOIN前使用ewa_test
SELECT * FROM users u
-- ewa_test @include_orders is not null
LEFT JOIN orders o ON u.id = o.user_id

-- ❌ 不推荐：ewa_test在WHERE中可能导致全表扫描
SELECT * FROM users u
LEFT JOIN orders o ON u.id = o.user_id
WHERE 
-- ewa_test @user_id is null OR u.id = @user_id_int
```

### 8.5 参数命名

```sql
-- ✅ 推荐：使用描述性参数名
-- ewa_test @include_orders is not null
-- ewa_test @min_price is not null
-- ewa_test @sort_by = 'date'

-- ❌ 不推荐：模糊的参数名
-- ewa_test @p1 is not null
-- ewa_test @x = 'y'
```

---

## 九、常见问题

### Q1: ewa_test 和 ewa_block_test 有什么区别？

| 特性 | ewa_test | ewa_block_test |
|------|----------|----------------|
| 作用范围 | 下一行SQL | 多行SQL块 |
| 语法 | `-- ewa_test <条件>` | `-- ewa_block_test <条件>` ... `-- ewa_block_test` |
| 使用场景 | 单个可选条件 | 多个相关的可选SQL行 |

### Q2: 条件表达式在哪里执行？

条件表达式通过**HSQLDB内存数据库**执行，不依赖于实际配置的数据库。

### Q3: 可以使用SQL函数吗？

可以，但需要使用HSQLDB支持的函数语法。

### Q4: 在HTML页面中如何使用？

在XML配置的Frame `<sql>` 标签中直接使用，参数通过URL或表单传递。

### Q5: 性能影响如何？

- `ewa_test` 只在SQL解析时执行一次判断
- 不会在每次查询时重复执行
- 对查询性能无影响

---

## 十、总结

| 功能 | 说明 |
|------|------|
| **ewa_test** | 条件化包含下一行SQL |
| **ewa_block_test** | 条件化包含多行SQL块 |
| **支持位置** | SQL查询、Frame XML配置 |
| **判断引擎** | HSQLDB内存数据库 |
| **参数来源** | RequestValue (FORM/QUERY/SESSION/SYSTEM) |
| **调试支持** | 注释中显示判断结果 `<true/false>` |

**核心优势**：
- ✅ 动态SQL无需编程，配置即可实现
- ✅ 提高SQL复用性，减少重复代码
- ✅ 易于维护，逻辑清晰
- ✅ 支持复杂条件组合
- ✅ 调试友好，结果可视化
