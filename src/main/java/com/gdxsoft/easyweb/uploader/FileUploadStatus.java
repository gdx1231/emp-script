/** 
 * 本例程演示了通过Web上传文件过程中的进度显示。您可以对本例程进行任何修改和使用。 
 * 
 * 如需要转载，请注明作者。 
 * 
 * 作者： 刘作晨 
 * 
 */
package com.gdxsoft.easyweb.uploader;

import java.util.HashMap;

public class FileUploadStatus {
	// 上传用户地址
	private String _guid;
	// 上传总量
	private long uploadTotalSize = 0;
	// 读取上传总量
	private long readTotalSize = 0;
	// 当前上传文件号
	private int currentUploadFileNum = 0;
	// 成功读取上传文件数
	private int successUploadFileCount = 0;
	// 状态
	private String status = "";
	// 处理起始时间
	private long processStartTime = 0l;
	// 处理终止时间
	private long processEndTime = 0l;
	// 处理执行时间
	private long processRunningTime = 0l;
	// 上传文件URL列表
	private HashMap<String,FileUpload> uploadFileUrlList = new HashMap<String,FileUpload>();
	// 取消上传
	private boolean cancel = false;
	// 上传base目录
	private String baseDir = "";

	public FileUploadStatus() {
		System.out.println("初始化 FileUploadStatus");
	}

	public String getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

	public boolean getCancel() {
		return cancel;
	}

	public void setCancel(boolean cancel) {
		this.cancel = cancel;
	}

	public HashMap<String,FileUpload> getUploadFileUrlList() {
		return uploadFileUrlList;
	}

	public void setUploadFileUrlList(HashMap<String,FileUpload> uploadFileUrlList) {
		this.uploadFileUrlList = uploadFileUrlList;
	}

	public long getProcessRunningTime() {
		return processRunningTime;
	}

	public void setProcessRunningTime(long processRunningTime) {
		this.processRunningTime = processRunningTime;
	}

	public long getProcessEndTime() {
		return processEndTime;
	}

	public void setProcessEndTime(long processEndTime) {
		this.processEndTime = processEndTime;
	}

	public long getProcessStartTime() {
		return processStartTime;
	}

	public void setProcessStartTime(long processStartTime) {
		this.processStartTime = processStartTime;
	}

	public long getReadTotalSize() {
		return readTotalSize;
	}

	public void setReadTotalSize(long readTotalSize) {
		this.readTotalSize = readTotalSize;
	}

	public int getSuccessUploadFileCount() {
		return successUploadFileCount;
	}

	public void setSuccessUploadFileCount(int successUploadFileCount) {
		this.successUploadFileCount = successUploadFileCount;
	}

	public int getCurrentUploadFileNum() {
		return currentUploadFileNum;
	}

	public void setCurrentUploadFileNum(int currentUploadFileNum) {
		this.currentUploadFileNum = currentUploadFileNum;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public long getUploadTotalSize() {
		return uploadTotalSize;
	}

	public String getGuid() {
		return _guid;
	}

	public void setUploadTotalSize(long uploadTotalSize) {
		this.uploadTotalSize = uploadTotalSize;
	}

	public void setGuid(String guid) {
		// System.out.println("FileUploadStatus SET GUID="+guid);
		this._guid = guid;
	}

	public String toXml() {
		StringBuilder strXml = new StringBuilder();
		strXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
		strXml.append("<UploadStatus ");
		strXml.append("UploadTotalSize='").append(getUploadTotalSize()).append(
				"' ").append("ReadTotalSize='").append(getReadTotalSize())
				.append("' ").append("CurrentUploadFileNum='").append(
						getCurrentUploadFileNum()).append("' ").append(
						"SuccessUploadFileCount='").append(
						getSuccessUploadFileCount()).append("' ").append(
						"Status='").append(getStatus()).append("' ").append(
						"ProcessStartTime='").append(getProcessStartTime())
				.append("' ").append("ProcessEndTime='").append(
						getProcessEndTime()).append("' ").append(
						"ProcessRunningTime='").append(getProcessRunningTime())
				.append("' ").append("Cancel='").append(getCancel()).append(
						"' />");
		return strXml.toString();

	}

}
