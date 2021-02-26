package com.gdxsoft.easyweb.data;

import java.io.Serializable;

import org.json.JSONObject;

import com.gdxsoft.easyweb.script.display.items.ItemImage;
import com.gdxsoft.easyweb.utils.UFile;

public class DefaultBinaryHandle implements IBinaryHandle , Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3418278055773220212L;

	public DefaultBinaryHandle() {
	}

	@Override
	public String handle(byte[] buf, String contentpath) {
		String v = ItemImage.getImage(contentpath, buf);

		return v;
	}

	@Override
	public String handle(byte[] buf, String path, String url) {
		try {
			String binFileName = UFile.createMd5File(buf, "gdx", path, false);
			JSONObject objBinFile = new JSONObject();
			objBinFile.put("NAME", binFileName); // 文件名
			objBinFile.put("PATH", path);// 物理路径
			objBinFile.put("URL", url);// http路径

			// System.out.println("toJSONArraySync:" + objBinFile.toString());
			return "##BINARY_FILE[" + objBinFile.toString() + "]BINARY_FILE##";

		} catch (Exception e) {
			System.out.println("toJSONArraySync:" + e.toString());
			return "##BINARY_FILE_ERR[" + e.toString() + "]BINARY_FILE_ERR##";

		}
	}
}
