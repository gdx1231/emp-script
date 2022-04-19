package com.gdxsoft.easyweb.uploader;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.Utils;

@Deprecated
public class PostData {
	private HttpServletRequest _request;
	private HttpServletResponse _response;
	private String _uploadDir;
	private String _uploadRealDir;
	private String _uploadTempDir;

	public PostData() {

	}

	public void init(HttpServletRequest request, HttpServletResponse response,
			String uploadDir) {
		_request = request;
		_response = response;
		_uploadDir = uploadDir;
		String root = UPath.getRealPath().split("WEB-INF")[0].replace("%20",
				" ");
		_uploadRealDir = root + _uploadDir;
		_uploadTempDir = root + "_temp_";

		File f1 = new File(this._uploadRealDir);
		if (!f1.exists()) {
			f1.mkdirs();
		}
		File f2 = new File(this._uploadTempDir);
		if (!f2.exists()) {
			f2.mkdirs();
		}

	}

	public void doPost() throws IOException {
		boolean isMultipart = ServletFileUpload.isMultipartContent(_request);
		if (_request.getParameter("EWA_UP_GUID") == null) {
			return;
		}
		if (isMultipart) { // 文件上传
			processFileUpload();
		}
	}

	/**
	 * 从文件路径中取出扩展名
	 */
	private String takeOutFileExt(String filePath) {
		int pos = filePath.lastIndexOf(".");
		if (pos > 0) {
			return filePath.substring(pos + 1, filePath.length());
		} else {
			return "";
		}
	}

	/**
	 * 从request中取出FileUploadStatus Bean
	 */
	public static FileUploadStatus getStatusBean(HttpServletRequest request) {
		BeanControler beanCtrl = BeanControler.getInstance();
		FileUploadStatus ss = beanCtrl.getUploadStatus(request
				.getParameter("EWA_UP_GUID"));
		return ss;
	}

	/**
	 * 把FileUploadStatus Bean保存到类控制器BeanControler
	 */
	public static void saveStatusBean(HttpServletRequest request,
			FileUploadStatus statusBean) {
		String guid = request.getParameter("EWA_UP_GUID");
		statusBean.setGuid(guid);
		BeanControler beanCtrl = BeanControler.getInstance();
		beanCtrl.setUploadStatus(statusBean);
	}

	/**
	 * 删除已经上传的文件
	 */
	private void deleteUploadedFile() {
		FileUploadStatus satusBean = getStatusBean(_request);
		if (satusBean == null)
			return;
		java.util.Iterator<String> it = satusBean.getUploadFileUrlList()
				.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			FileUpload fu = satusBean.getUploadFileUrlList().get(key);
			File uploadedFile = new File(fu.getSavePath());
			try {
				uploadedFile.delete();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		satusBean.getUploadFileUrlList().clear();
		satusBean.setStatus("删除已上传的文件");
		saveStatusBean(_request, satusBean);
	}

	/**
	 * 上传过程中出错处理
	 */
	private void uploadExceptionHandle(String errMsg) {
		// 首先删除已经上传的文件
		System.err.println("上传未知错误:" + errMsg);
		deleteUploadedFile();
		FileUploadStatus satusBean = getStatusBean(_request);
		if (satusBean != null) {
			satusBean.setStatus(errMsg);
			saveStatusBean(_request, satusBean);
		}
	}

	/**
	 * 初始化文件上传状态Bean
	 */
	private FileUploadStatus initStatusBean() {
		BeanControler beanCtrl = BeanControler.getInstance();
		String guid = _request.getParameter("EWA_UP_GUID");
		FileUploadStatus satusBean = beanCtrl.getUploadStatus(guid);
		if (satusBean == null) {
			satusBean = new FileUploadStatus();
			satusBean.setStatus("正在准备处理");
			satusBean.setUploadTotalSize(_request.getContentLength());
			satusBean.setProcessStartTime(System.currentTimeMillis());
			satusBean.setBaseDir(_request.getContextPath() + this._uploadDir);
			satusBean.setGuid(guid);
		}
		return satusBean;
	}

	/**
	 * 处理文件上传
	 */
	private void processFileUpload() {
		DiskFileItemFactory factory = new DiskFileItemFactory();
		// 设置内存缓冲区，超过后写入临时文件
		factory.setSizeThreshold(10240000);
		// 设置临时文件存储位置
		factory.setRepository(new File(this._uploadTempDir));
		ServletFileUpload upload = new ServletFileUpload(factory);
		// 设置单个文件的最大上传值
		upload.setFileSizeMax(102400000);
		// 设置整个request的最大值
		upload.setSizeMax(102400000);
		upload.setProgressListener(new FileUploadListener(_request));
		// 保存初始化后的FileUploadStatus Bean
		saveStatusBean(_request, initStatusBean());
		FileUploadStatus satusBean = getStatusBean(_request);
		try {
			List<?> items = upload.parseRequest(_request);
			// 获得返回url
			for (int i = 0; i < items.size(); i++) {
				FileItem item = (FileItem) items.get(i);
				if (item.isFormField()) {
					break;
				}
			}
			// 处理文件上传
			for (int i = 0; i < items.size(); i++) {
				FileItem item = (FileItem) items.get(i);

				// 取消上传
				if (getStatusBean(_request).getCancel()) {
					deleteUploadedFile();
					break;
				} else if (!item.isFormField() && item.getName().length() > 0) {
					System.out.println("上传文件"
							+ new String(item.getName().getBytes(), "utf-8"));
					FileUpload fu = this.addFileUpload(satusBean, item, i);

					File uploadedFile = new File(fu.getSavePath());
					item.write(uploadedFile);

					saveStatusBean(_request, satusBean);
					Thread.sleep(500);
				}
			}
		} catch (FileUploadException e) {
			uploadExceptionHandle("上传文件时发生错误:" + e.getMessage());
		} catch (Exception e) {
			uploadExceptionHandle("保存上传文件时发生错误:" + e.getMessage());
		}
		String s1 = "";
		java.util.Iterator<String> it = satusBean.getUploadFileUrlList()
				.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			FileUpload fu = satusBean.getUploadFileUrlList().get(key);
			if (s1.length() == 0) {
				s1 = fu.getFileUrl();
			} else {
				s1 += "|" + fu.getFileUrl();
			}

		}
		if (s1.length() > 0) {
			s1 = s1.replace("\"", "&quot;");
			s1 = "window.parent.document.forms[0].__EMP_UPLOAD_SERVER_NAME.value=\""
					+ s1 + "\"";
			_response.setContentType("text/html");
			_response.setCharacterEncoding("UTF-8");
			_response.setHeader("Cache-Control", "no-cache");
			try {
				_response.getWriter().write(Utils.getJavascript(s1));
				_response.getWriter().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private FileUpload addFileUpload(FileUploadStatus satusBean, FileItem item,
			int index) {
		FileUpload fu = satusBean.getUploadFileUrlList().get(index + "");
		if (fu == null) {
			fu = new FileUpload();
			String name;
			try {
				name = new String(item.getName().getBytes(), "utf-8");
			} catch (UnsupportedEncodingException e) {
				System.err.println(e.getMessage());
				name = item.getName();
			}
			String ext = this.takeOutFileExt(name);
			String guid = _request.getParameter("EWA_UP_GUID");
			String fileName = guid + "_" + index;

			if (ext.length() > 0) {
				fileName = fileName + "." + ext;
			}
			String serverName = this._uploadDir + "/" + fileName;

			fu.setExt(ext);
			fu.setUserLocalPath(item.getName());
			fu.setSaveFileName(fileName);
			fu.setSavePath(this._uploadRealDir + File.separator + fileName);
			fu.setFileUrl(serverName);
			satusBean.getUploadFileUrlList().put(index + "", fu);
		}
		return fu;
	}

	/**
	 * 回应上传状态查询
	 */
	public void queryStatus() throws IOException {
		FileUploadStatus satusBean = getStatusBean(_request);
		if (satusBean == null) {
			_response.setContentType("text/html");
			_response.setCharacterEncoding("UTF-8");
			_response.setHeader("Cache-Control", "no-cache");
			_response.getWriter().write("");
			_response.getWriter().close();
		} else {
			_response.setContentType("text/xml");
			_response.setCharacterEncoding("UTF-8");
			_response.setHeader("Cache-Control", "no-cache");
			_response.getWriter().write(satusBean.toXml());
			_response.getWriter().close();

			System.out.println(satusBean.getGuid() + ","
					+ satusBean.getReadTotalSize());
			if (satusBean.getUploadTotalSize() <= satusBean.getReadTotalSize()) {
				// 清除
				BeanControler.getInstance().removeUploadStatus(
						satusBean.getGuid());
				System.out.println("清除:" + satusBean.getGuid());
			}
		}
	}

	/**
	 * 处理取消文件上传
	 */
	public void cancelUpload() throws IOException {
		FileUploadStatus satusBean = getStatusBean(_request);
		satusBean.setCancel(true);
		saveStatusBean(_request, satusBean);
	}

}
