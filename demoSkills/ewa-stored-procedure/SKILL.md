---
name: ewa-stored-procedure
description: "Use when: 通过 EWA API 查看存储过程、函数或触发器的完整定义文本。INFORMATION_SCHEMA.ROUTINES 会截断，需用 sys.sql_modules 获取完整内容。"
---

# 通过 EWA API 查看存储过程完整定义

## 问题

`INFORMATION_SCHEMA.ROUTINES` 的 `ROUTINE_DEFINITION` 字段最大只返回 **4000 字符**，较长的存储过程会被截断。

## 解决方案：两步查询

### Step 1: 从 `sys.procedures` 获取 `object_id`

```bash
./ewa-api.sh getTableData globalTravel "sys.procedures" "name='PR_GRP_FILE_SET'" json
```

响应中提取 `object_id`（如 `2029458504`）。

### Step 2: 用 `object_id` 从 `sys.sql_modules` 获取完整定义

```bash
./ewa-api.sh getTableData globalTravel "sys.sql_modules" "object_id=2029458504" json
```

响应中 `DATA[0].definition` 字段包含完整的 `CREATE PROCEDURE` 文本，无长度限制。

## 注意事项

- `sys.sql_modules` 的列名是 `definition`（小写），不是 `ROUTINE_DEFINITION`。
- 同样适用于查看函数（`type_desc = 'SQL_SCALAR_FUNCTION'`）和触发器（`sys.triggers` → `object_id` → `sys.sql_modules`）。
- 如果 `getTableData` 对 `sys.*` 表返回失败，可能需要用 `INFORMATION_SCHEMA` 视图作为备选（接受 4000 字符截断）。
