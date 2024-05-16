package com.gdxsoft.easyweb.script.display.items;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.template.SkinFrame;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.utils.UConvert;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UNet;
import com.gdxsoft.easyweb.utils.UQRCode;
import com.gdxsoft.easyweb.utils.Utils;

/**
 * Signature item
 * 
 * @author admin
 *
 */
public class ItemQRCode extends ItemBase {
	private static Logger LOGGER = LoggerFactory.getLogger(ItemQRCode.class);

	public String createItemHtml() throws Exception {
		UserXItem userXItem = super.getUserXItem();
		String logoUrl = null;
		if (userXItem.testName("QRCode") && userXItem.getItem("QRCode").count() > 0) {

			UserXItemValue cfg = userXItem.getItem("QRCode").getItem(0);

			if (cfg.testName("QRLogoUrl")) {
				logoUrl = cfg.getItem("QRLogoUrl");
			}

		}

		String s1 = super.getXItemFrameHtml();
		String val = super.getValue();

		int width = 300;

		// 拼接参数，用于判断是否已经创建
		StringBuilder hash_string = new StringBuilder();
		hash_string.append("msg=");
		hash_string.append(val);
		hash_string.append("~~~~width=");
		hash_string.append(width);

		boolean hasLogo = false;
		if (logoUrl != null && logoUrl.length() > 0) {
			hash_string.append("~~~~logoUrl=");
			hash_string.append(logoUrl);
			
			hasLogo = true;
		}

		String md5 = Utils.md5(hash_string.toString());

		byte[] img = null;
		String[] paths = UQRCode.getQRCodeSavedPath(md5, "jpeg");
		String path = paths[0];
		File exists = new File(path);
		
		if (exists.exists()) {
			try {
				img = UFile.readFileBytes(exists.getAbsolutePath());
			} catch (Exception err) {
				LOGGER.warn(err.getMessage());
			}
		} else if(hasLogo){
			byte[] logoBytes = loadLogo(logoUrl);

			if (logoBytes == null || logoBytes.length == 0) {
				LOGGER.warn("Load logo fail {}", logoUrl);
				img = UQRCode.createQRCode(val, width);
			} else {
				img = UQRCode.createQRCode(val, width, logoBytes);
				// 保存缓存
				UFile.createBinaryFile(path, img, true);
			}
		} else {
			img = UQRCode.createQRCode(val, width);
			// 保存缓存
			UFile.createBinaryFile(path, img, true);
		}

		StringBuilder sb = new StringBuilder();
		sb.append("data:image/jpeg;base64,");
		String base64 = UConvert.ToBase64String(img);
		sb.append(base64);

		s1 = s1.replace(SkinFrame.TAG_VAL, val == null ? "" : sb.toString());
		return s1.trim();
	}

	private byte[] loadLogo(String logoUrl) {
		if (logoUrl == null || logoUrl.length() == 0) {
			return null;
		}

		RequestValue rv = super.getHtmlClass().getItemValues().getRequestValue();
		String url = logoUrl.toLowerCase().startsWith("https://") || logoUrl.toLowerCase().startsWith("http://")
				? logoUrl
				: rv.s(RequestValue.EWAdotHOST_PROTOCOL) + "://" + rv.s(RequestValue.EWAdotHOST) + logoUrl;

		String md5 = Utils.md5(url);

		String[] paths = UQRCode.getQRCodeSavedPath(md5, ".qrcodelogo");
		String path = paths[0];
		File exists = new File(path);
		if (exists.exists()) {
			try {
				return UFile.readFileBytes(exists.getAbsolutePath());
			} catch (Exception err) {
				LOGGER.warn(err.getMessage());
			}
		}

		UNet net = new UNet();
		byte[] logoBytes = net.downloadData(url);
		if (net.getLastStatusCode() == 200) {
			return logoBytes;
		} else {
			LOGGER.error(net.getLastErr());
			return null;
		}
	}
}
