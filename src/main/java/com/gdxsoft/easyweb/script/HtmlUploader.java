package com.gdxsoft.easyweb.script;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.gdxsoft.easyweb.uploader.Upload;
import com.gdxsoft.easyweb.utils.UPath;

public class HtmlUploader {

	public HtmlUploader() {
	}

	/**
	 * 上传文件
	 * 
	 * @param rv
	 * @param uploadXmlName  上传文件配置文件
	 * @param uploadItemName 上传文件配置项
	 * @param name           配置项的上传参数名称
	 * @return
	 * @throws Exception
	 */
	public String upload(RequestValue rv, String uploadXmlName, String uploadItemName, String name) throws Exception {

		HttpServletRequest request = rv.getRequest();

		DiskFileItemFactory factory = new DiskFileItemFactory();
		// 设置内存缓冲区，超过后写入临时文件
		int bufferSize = 1024 * 1024 * 10; // 10M

		factory.setSizeThreshold(bufferSize);// 设置缓冲区大小，这里是10M
		// 设置临时文件存储位置
		File tempPath = new File(UPath.getPATH_UPLOAD() + "/" + Upload.DEFAULT_UPLOAD_PATH);
		factory.setRepository(tempPath);
		ServletFileUpload upload = new ServletFileUpload(factory);

		long maxSize = 1024 * 1024 * 1024 * 2; // 2g
		upload.setSizeMax(maxSize);

		List<?> items = null;
		try {
			items = upload.parseRequest(request);
			for (int i = 0; i < items.size(); i++) {
				FileItem item = (FileItem) items.get(i);
				if (item.isFormField()) {
					rv.addValue(item.getFieldName(), item.getString());
				}
			}
		} catch (Exception err) {
			System.err.println(err.getMessage());
		}

		Upload up = new Upload();
		up.setRv(rv);
		up.setUploadItems(items);

		up.init(uploadXmlName, uploadItemName, name);

		String s = up.upload();
		return s;

	}
}
