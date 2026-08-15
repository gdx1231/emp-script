package com.gdxsoft.easyweb.uploader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UPath;

/**
 * 上传文件工具类，统一了 ServletUpload、ServletRestful、ActionFrame 中的公共逻辑。
 * <p>
 * 提供两个核心方法：
 * <ul>
 * <li>{@link #parseAndUpload(HttpServletRequest, RequestValue)} — 完整的上传流程</li>
 * <li>{@link #createUploadPara(String, String, List, String, RequestValue)} — 上传参数映射</li>
 * </ul>
 * </p>
 */
public class UploadUtils {

	/**
	 * 执行 multipart 文件上传的完整流程：创建 Upload、解析请求、执行上传。
	 * <p>
	 * 统一了 ServletUpload.show() 和 ServletRestful.handleUpload() 的公共部分。
	 * </p>
	 *
	 * @param request HTTP 请求
	 * @param rv      RequestValue（需已设置 XMLNAME/ITEMNAME/NAME）
	 * @return 已调用 upload() 的 Upload 实例，可通过 getAlFiles() 获取上传结果
	 * @throws Exception 上传过程中的任何异常
	 */
	public static Upload parseAndUpload(HttpServletRequest request, RequestValue rv) throws Exception {
		Upload up = new Upload();
		up.setRv(rv);
		up.init(request);

		List<Part> items = new ArrayList<>();
		for (Part part : request.getParts()) {
			if (part.getSubmittedFileName() == null) {
				rv.addValue(part.getName(), readPartValue(part));
			}
			items.add(part);
		}

		up.setUploadItems(items);
		up.upload();

		return up;
	}

	public static String readPartValue(Part part) throws IOException {
		try (InputStream in = part.getInputStream()) {
			return IOUtils.toString(in, StandardCharsets.UTF_8);
		}
	}

	/**
	 * 将上传文件元数据映射到 RequestValue。
	 *
	 * @param uploadName 上传字段名（对应 UserXItem.getName()）
	 * @param dataType   数据类型（"binary" 存储文件字节，其他存储文件 URL）
	 * @param alFiles    已上传文件列表
	 * @param uploadDir  已解析的上传目录（来自 Upload.getUploadDir()）
	 * @param rv         请求参数对象
	 */
	public static void createUploadPara(String uploadName, String dataType,
			List<FileUpload> alFiles, String uploadDir, RequestValue rv) throws Exception {

		if (alFiles == null || alFiles.isEmpty()) {
			return;
		}

		// 标记已执行，避免 ActionFrame 重复处理
		rv.addValue("____createUploadPara____", "ADDED");

		JSONArray arrJson = new JSONArray();

		for (FileUpload fu : alFiles) {
			File f = new File(fu.getSavePath());
			if (!f.exists() || !f.isFile() || !f.canRead()) {
				continue;
			}

			String ext = fu.getExt();

			// --- 主字段值：二进制 或 URL ---
			if (dataType != null && dataType.equalsIgnoreCase("binary")) {
				long m10 = 1024 * 1024 * 10; // 10M
				if (f.length() <= m10) {
					byte[] buf = UFile.readFileBytes(f.getAbsolutePath());
					if (rv.getPageValues().getValue(uploadName) == null) {
						rv.addValue(uploadName, buf, "binary", buf.length);
					} else {
						rv.changeValue(uploadName, buf, "binary", buf.length);
					}
				}
			} else {
				String fileUrl = fu.getFileUrl();
				if (rv.getPageValues().getValue(uploadName) == null) {
					rv.addValue(uploadName, fileUrl);
				} else {
					rv.changeValue(uploadName, fileUrl, "string", fileUrl.length());
				}
			}

			// --- 后缀参数 ---

			// 扩展名
			rv.addValue(uploadName + "_EXT", ext);

			// MD5
			String md5 = UFile.md5(f);
			rv.addValue(uploadName + "_MD5", md5);

			// 保存文件名
			rv.addValue(uploadName + "_NAME", f.getName());

			// 完整物理路径
			rv.addValue(uploadName + "_PATH", f.getAbsolutePath());

			// 短路径（去除 PATH_UPLOAD）
			String pathShort = uploadDir + "/" + f.getName();
			rv.addValue(uploadName + "_PATH_SHORT", pathShort);

			// 文件大小
			rv.addValue(uploadName + "_SIZE", f.length());
			rv.addValue(uploadName + "_LENGTH", f.length());

			// 文件 URL
			rv.addValue(uploadName + "_URL", fu.getFileUrl());

			// 文件 UNID
			rv.addValue(uploadName + "_UP_UNID", fu.getUnid());

			// URL 前缀
			String ct = UPath.getPATH_UPLOAD_URL();
			if (ct != null) {
				rv.addValue(uploadName + "_CT", ct);
			}

			// 用户本地文件名
			String userLocalPath = fu.getUserLocalPath();
			if (userLocalPath != null) {
				rv.addValue(uploadName + "_LOCAL_NAME", userLocalPath);
			}

			// 构建文件 JSON（存入 _JSON 参数）
			JSONObject fileJson = new JSONObject();
			fileJson.put("UP_NAME", fu.getSaveFileName());
			fileJson.put("UP_URL", fu.getFileUrl());
			fileJson.put("UP_UNID", fu.getUnid());
			fileJson.put("UP_SIZE", fu.getLength());
			fileJson.put("UP_EXT", ext);
			fileJson.put("UP_PATH", f.getAbsolutePath());
			fileJson.put("UP_PATH_SHORT", pathShort);
			if (userLocalPath != null) {
				fileJson.put("UP_LOCAL_NAME", userLocalPath);
			}
			arrJson.put(fileJson);
		}

		if (arrJson.length() > 0) {
			rv.addValue(uploadName + "_JSON", arrJson.toString());
		}
	}
}
