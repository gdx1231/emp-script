/** 
 * 本例程演示了通过Web上传文件过程中的进度显示。您可以对本例程进行任何修改和使用。 
 * 
 * 如需要转载，请注明作者。 
 * 
 * 作者： 刘作晨 
 * 
 */
package com.gdxsoft.easyweb.uploader;

import java.util.Vector;

public class BeanControler {
	private static BeanControler beanControler = new BeanControler();

	private Vector<FileUploadStatus> vector = new Vector<FileUploadStatus>();

	private BeanControler() {
	}

	public static BeanControler getInstance() {
		return beanControler;
	}

	/**
	 * 取得相应FileUploadStatus类对象的存储位置
	 */
	private int indexOf(String strID) {
		int nReturn = -1;
		for (int i = 0; i < vector.size(); i++) {
			FileUploadStatus status = (FileUploadStatus) vector.elementAt(i);
			if (status.getGuid().equals(strID)) {
				nReturn = i;
				break;
			}
		}
		return nReturn;
	}

	/**
	 * 取得相应FileUploadStatus类对象
	 */
	public FileUploadStatus getUploadStatus(String strID) {
		int m = indexOf(strID);
		if (m == -1) {
			return null;
		} else {
			return vector.elementAt(m);
		}
	}

	/**
	 * 存储FileUploadStatus类对象
	 */
	public void setUploadStatus(FileUploadStatus status) {
		int nIndex = indexOf(status.getGuid());
		if (-1 == nIndex) {
			vector.add(status);
		} else {
			//vector.insertElementAt(status, nIndex);
			//vector.removeElementAt(nIndex + 1);
		}
	}

	/**
	 * 删除FileUploadStatus类对象
	 */
	public void removeUploadStatus(String strID) {
		int nIndex = indexOf(strID);
		if (-1 != nIndex)
			vector.removeElementAt(nIndex);
	}
}
