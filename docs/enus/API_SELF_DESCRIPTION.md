# API Self-Description Feature Reference

## Overview

This implementation adds an API self-description feature to emp-script. Full API documentation can be accessed by visiting `/servletRestful/[your-path]/ewa-help-documents`.

## Usage

### Accessing API Documentation

**Endpoint:** `/servletRestful/[your-path]/ewa-help-documents`

**Method:** GET

**Description:** Get full descriptions of all configured RESTful APIs

### Return Format

```json
{
  "success": true,
  "generatedAt": "2026-06-26 02:30:00",
  "endpoints": [
    {
      "path": "/ewa-api/chat/v1/chatRooms",
      "method": "GET",
      "name": "Get chat room list",
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
              "info": "Room ID",
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

## Field Reference

### Endpoint Basic Info

| Field | Type | Description |
|------|------|------|
| path | string | API endpoint path |
| method | string | HTTP method (GET/POST/PUT/PATCH/DELETE) |
| name | string | Chinese name |
| nameEn | string | English name |
| memo | string | Chinese memo |
| memoEn | string | English memo |
| xmlName | string | Associated XML configuration file |
| itemName | string | Associated XML configuration item |
| parameters | string | Additional parameters |

### Field Info

| Field | Type | Description |
|------|------|------|
| name | string | Field name |
| tag | string | Field tag type (text/span/password/select, etc.) |
| description | object | Description info (supports Chinese/English) |
| dataItem | object | Data field configuration |
| isRequired | boolean | Required or not |
| length | object | Length constraints |
| listConfig | object | List configuration (dropdowns, etc.) |

## Implementation Details

### Main Classes

1. **ServletRestful** - Main Servlet, contains `ewaHelpDocuments` method
2. **ConfRestfuls** - Configuration management class
3. **UserConfig** - XML configuration loader class
4. **UserXItem** - Single field configuration class

### Core Methods

1. `ewaHelpDocuments()` - Entry method, generates full documentation
2. `addJdbcEndpoints()` - Load configuration from database
3. `addEwaConfEndpoints()` - Load from XML configuration (pending improvement)
4. `createEndpointDescription()` - Create single endpoint description
5. `addXItemFields()` - Load field details

### Data Sources

- **Database Configuration Tables:** `ewa_restful_catalog` and `ewa_restful`
- **XML Configuration Files:** Referenced via `rs_xmlname` and `rs_itemname`

## Development Plan

- [x] Implement API documentation generation from database configuration
- [ ] Improve API documentation generation from XML configuration
- [ ] Add OpenAPI/Swagger format support
- [ ] Add online testing interface
- [ ] Add version control
- [ ] Support multi-language switching
