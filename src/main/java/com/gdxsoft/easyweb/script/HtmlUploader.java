package com.gdxsoft.easyweb.script;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import com.gdxsoft.easyweb.uploader.Upload;
import com.gdxsoft.easyweb.uploader.UploadUtils;

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

		List<Part> items = new ArrayList<>();
		try {
			for (Part part : request.getParts()) {
				if (part.getSubmittedFileName() == null) {
					rv.addValue(part.getName(), UploadUtils.readPartValue(part));
				}
				items.add(part);
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
