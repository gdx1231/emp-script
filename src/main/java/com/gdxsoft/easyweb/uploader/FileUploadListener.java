/** 
 * 本例程演示了通过Web上传文件过程中的进度显示。您可以对本例程进行任何修改和使用。 
 * 
 * 如需要转载，请注明作者。 
 * 
 * 作者： 刘作晨 
 * 
 */
package com.gdxsoft.easyweb.uploader;

import org.apache.commons.fileupload.ProgressListener;
import javax.servlet.http.HttpServletRequest;

@Deprecated
public class FileUploadListener implements ProgressListener {
	private HttpServletRequest request = null;

	public FileUploadListener(HttpServletRequest request) {
		this.request = request;
	}

	/**
	 * 更新状态
	 */
	public void update(long pBytesRead, long pContentLength, int pItems) {
		FileUploadStatus statusBean = PostData.getStatusBean(request);
		statusBean.setUploadTotalSize(pContentLength);
		// 读取完成
		if (pContentLength == -1) {
			statusBean.setStatus("完成对" + pItems + "个文件的读取:读取了 " + pBytesRead
					+ " bytes.");
			statusBean.setReadTotalSize(pBytesRead);
			statusBean.setSuccessUploadFileCount(pItems);
			statusBean.setProcessEndTime(System.currentTimeMillis());
			statusBean.setProcessRunningTime(statusBean.getProcessEndTime());
			// 读取中
		} else {
			statusBean.setStatus("当前正在处理第" + pItems + "个文件:已经读取了 " + pBytesRead
					+ " / " + pContentLength + " bytes.");
			statusBean.setReadTotalSize(pBytesRead);
			statusBean.setCurrentUploadFileNum(pItems);
			statusBean.setProcessRunningTime(System.currentTimeMillis());
		}
		PostData.saveStatusBean(request, statusBean);
		//System.out.println(statusBean.getReadTotalSize());
	}
}
