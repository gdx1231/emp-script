# API 自我描述功能说明

## 概述

本次实现为 emp-script 添加了 API 自我描述功能，可以通过访问 `/servletRestful/[your-path]/ewa-help-documents` 获取完整的 API 文档。

## 使用方法

### 访问 API 文档

**端点：** `/servletRestful/[your-path]/ewa-help-documents`

**方法：** GET

**描述：** 获取所有配置的 RESTful API 的完整描述

### 返回格式

```json
{
  "success": true,
  "generatedAt": "2026-06-26 02:30:00",
  "endpoints": [
    {
      "path": "/ewa-api/chat/v1/chatRooms",
      "method": "GET",
      "name": "获取聊天室列表",
      "nameEn": "Get chat room list",
      "memo": "",
      "memoEn": "",
      "xmlName": "/aichatroom/chat/chat.xml",
      "itemName": "chat_room.LF.M",
      "parameters": "",
      "fields": [
        {
          "name": "roomId",
          "tag": "span",
          "description": {
            "zhcn": {
              "info": "聊天室ID",
              "memo": ""
            },
            "enus": {
              "info": "Room ID",
              "memo": ""
            }
          },
          "dataItem": {
            "field": "room_id",
            "type": "int",
            "format": ""
          },
          "isRequired": true,
          "length": {
            "max": "",
            "min": ""
          },
          "listConfig": {
            "sql": "",
            "displayField": "",
            "valueField": ""
          }
        }
      ]
    }
  ]
}
```

## 字段说明

### 端点基本信息

| 字段 | 类型 | 描述 |
|------|------|------|
| path | string | API 端点路径 |
| method | string | HTTP 方法 (GET/POST/PUT/PATCH/DELETE) |
| name | string | 中文名称 |
| nameEn | string | 英文名称 |
| memo | string | 中文备注 |
| memoEn | string | 英文备注 |
| xmlName | string | 关联的 XML 配置文件 |
| itemName | string | 关联的 XML 配置项 |
| parameters | string | 附加参数 |

### 字段信息

| 字段 | 类型 | 描述 |
|------|------|------|
| name | string | 字段名称 |
| tag | string | 字段标签类型 (text/span/password/select等) |
| description | object | 描述信息（支持中英文） |
| dataItem | object | 数据字段配置 |
| isRequired | boolean | 是否必填 |
| length | object | 长度限制 |
| listConfig | object | 列表配置（下拉框等） |

## 实现细节

### 主要类

1. **ServletRestful** - 主 Servlet，包含 `ewaHelpDocuments` 方法
2. **ConfRestfuls** - 配置管理类
3. **UserConfig** - XML 配置加载类
4. **UserXItem** - 单个字段配置类

### 核心方法

1. `ewaHelpDocuments()` - 入口方法，生成完整文档
2. `addJdbcEndpoints()` - 从数据库加载配置
3. `addEwaConfEndpoints()` - 从 XML 配置加载（待完善）
4. `createEndpointDescription()` - 创建单个端点描述
5. `addXItemFields()` - 加载字段详细信息

### 数据来源

- **数据库配置表：** `ewa_restful_catalog` 和 `ewa_restful`
- **XML 配置文件：** 通过 `rs_xmlname` 和 `rs_itemname` 引用

## 开发计划

- [x] 实现数据库配置的 API 文档生成
- [ ] 完善 XML 配置的 API 文档生成
- [ ] 添加 OpenAPI/Swagger 格式支持
- [ ] 添加在线测试界面
- [ ] 添加版本控制
- [ ] 支持多语言切换