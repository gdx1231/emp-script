---
name: restful-vs-frame
description: EWA RESTful handler 开发/调试时，与 Frame 路径 (ServletUpload/ActionFrame) 对照确保参数映射和 SQL 执行一致性
source: auto-skill
extracted_at: '2026-07-15T09:00:10.895Z'
---

# RESTful vs Frame 路径对照

## 核心原则

当开发或调试 `ServletRestful` 的 handler（upload/download/image/conf）时，**必须对照 Frame 路径的等效实现**，确保：

1. RequestValue 参数映射完整
2. Action SQL 被执行
3. 响应结构匹配客户端预期

## 对照表

| 功能 | RESTful 路径 | Frame 路径 |
|------|------------|-----------|
| 上传文件 | `ServletRestful.handleUpload()` | `ServletUpload.show()` |
| 文件→参数映射 | **需手动实现** | `ActionFrame.createUploadPara()` |
| 参数→SQL | `ServletRestful.handleConf()` | `ActionFrame.executeCallSql()` |
| 下载 | `ServletRestful.handleDownload()` | `HtmlCreator` (inline) |
| 图片 | `ServletRestful.handleImage()` | `HtmlCreator.getUploadedFilePath()` |

## 关键差异

### 上传流程

**Frame 路径**（两步式，两次请求）：
```
客户端 --Ajax上传--> ServletUpload → Upload.upload() → 返回 [{"UP":"加密JSON"}]
客户端 --表单提交--> ActionFrame.createUploadPara() → 解密UP → 映射字段参数 → 执行Action SQL
```

**RESTful 路径**（一步式，一次请求）：
```
客户端 --POST multipart--> ServletRestful.handleUpload()
  1. Upload.upload()           // 保存文件，执行 UpSql
  2. mapUploadParametersToRv() // 迭代 UserXItems → UploadUtils.createUploadPara()
  3. handleConf()              // 执行 Item Action SQL (INSERT/UPDATE)
```

### UploadUtils — 统一上传入口

`UploadUtils` 提供两个公共方法，是上传流程的**唯一权威实现**：

```java
// com.gdxsoft.easyweb.uploader.UploadUtils

// 1. 完整上传流程：创建 Upload → 解析 multipart → 执行 upload() → 返回 Upload 实例
public static Upload parseAndUpload(HttpServletRequest request, RequestValue rv) throws Exception

// 2. 参数映射：FileUpload 列表 → RequestValue 字段级参数
public static void createUploadPara(String uploadName, String dataType,
        List<FileUpload> alFiles, String uploadDir, RequestValue rv) throws Exception
```

**调用方**：

| 方法 | ServletUpload.show() | ServletRestful.handleUpload() | ActionFrame.createUploadPara() |
|------|:---:|:---:|:---:|
| `parseAndUpload()` | ✅ | ✅ | — |
| `createUploadPara()` | — | ✅ (via `mapUploadParametersToRv`) | ✅ |

**`parseAndUpload()` 内部流程**：
1. `new Upload()` → `setRv(rv)` → `init(request)`（从 rv 读取 XMLNAME/ITEMNAME/NAME）
2. 设置 `DiskFileItemFactory`（buffer 10M, tempPath=`upload_files`）
3. 创建 `ServletFileUpload`（maxSize 2G）
4. `parseRequest(request)` → 遍历 items → form fields 写入 rv
5. `setUploadItems(items)` → `upload()` → 返回 Upload 实例

**`createUploadPara()` 调用方式差异**：
- `ServletRestful`：直接传 `up.getAlFiles()` + `up.getUploadDir()`
- `ActionFrame`：从解密后的 UP JSON 构建 `FileUpload`（设置 savePath/fileUrl/unid 等），传 `p`（UpPath）作为 uploadDir

### 上传参数映射（UploadUtils 设置的参数）

| 参数名 | 来源 | 说明 |
|-------|------|------|
| `字段名` | `UFile.readFileBytes(path)` 或 `fu.getFileUrl()` | binary 或 string 类型 |
| `字段名_EXT` | `fu.getExt()` | 扩展名 |
| `字段名_MD5` | `UFile.md5(file)` | MD5 校验 |
| `字段名_NAME` | `file.getName()` | 保存文件名 |
| `字段名_PATH` | `file.getAbsolutePath()` | 完整物理路径 |
| `字段名_PATH_SHORT` | `up.getUploadDir() + "/" + file.getName()` | 短路径 |
| `字段名_SIZE` / `_LENGTH` | `file.length()` | 文件大小 |
| `字段名_URL` | `fu.getFileUrl()` | 文件URL |
| `字段名_UP_UNID` | `fu.getUnid()` | 文件UNID |
| `字段名_CT` | `UPath.getPATH_UPLOAD_URL()` | URL前缀 |
| `字段名_LOCAL_NAME` | `fu.getUserLocalPath()` | 用户本地文件名 |
| `字段名_JSON` | JSONArray of all files | 所有上传文件JSON |

### 哪些 Tag 需要处理上传

在遍历 `UserXItems` 时，只处理以下 Tag：
- `h5upload`
- `image`
- `swffile`

且必须 `item.testName("Upload")` 为 true。

## 调试检查清单

当 ServletRestful 的 upload 不工作时：

- [ ] `handleUpload()` 是否调用了 `handleConf()`？（否则 Item Action SQL 不会执行）
- [ ] 是否调用 `mapUploadParametersToRv()` → `UploadUtils.createUploadPara()`？
- [ ] `handleUpload` 签名是否有 `HttpServletResponse` 参数？（`handleConf` 需要）
- [ ] `initUploadParameters()` 是否设置了 `name`（小写）在 rv 中？（`Upload.init()` 通过 `rv.s("NAME")` 读取，大小写不敏感）

## 依赖类

```java
// 统一参数映射
import com.gdxsoft.easyweb.uploader.UploadUtils;
// 文件数据载体
import com.gdxsoft.easyweb.uploader.FileUpload;
// 上传引擎
import com.gdxsoft.easyweb.uploader.Upload;
// 文件操作
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UPath;
```
