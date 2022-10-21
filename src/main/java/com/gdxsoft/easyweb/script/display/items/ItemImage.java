/**
 * 
 */
package com.gdxsoft.easyweb.script.display.items;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import com.gdxsoft.easyweb.script.template.SkinFrame;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValues;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UImages;
import com.gdxsoft.easyweb.utils.UPath;

/**
 * @author Administrator
 * 
 */
public class ItemImage extends ItemBase {

	/**
	 * 根据二进制数据获取图片
	 * 
	 * @param contentPath
	 * @param images
	 * @return
	 */
	public static String getImage(String contentPath, byte[] images) {
		try {
			if (images == null || images.length == 0) {
				return contentPath + "/EWA_STYLE/images/pic_no.jpg";
			} else {
				// String path = UPath.getRealContextPath() +
				// UPath.PATH_IMG_CACHE;
				String path = UPath.getPATH_IMG_CACHE();
				String fileName = UFile.createMd5File(images, "jpg", path, false);

				// 缩略图
				String smallPicPath = path + "/" + fileName.replace(".jpg", "_SMAILL.jpg");

				File f1 = new File(smallPicPath);
				if (!f1.exists()) {
					java.awt.Dimension[] dims = new java.awt.Dimension[1];
					dims[0] = new java.awt.Dimension();
					dims[0].width = 100;
					dims[0].height = 100;
					File[] f2 = UImages.createResized(path + "/" + fileName, dims);
					if (f2.length > 0) {
						UFile.copyFile(f2[0].getAbsolutePath(), smallPicPath);
						f2[0].delete();
						f2[0].getParentFile().delete();
					}
				}

				// 页面显示内容URL
				String url = UPath.getPATH_IMG_CACHE_URL() + "/" + f1.getName();

				if (UPath.PATH_IMG_CACHE.startsWith("@")) {
					return url;
				} else {
					return contentPath + "/" + url;
				}
			}
		} catch (Exception e) {
			return e.getMessage() == null ? "error" : e.getMessage();
		}
	}

	public static String getImageOri(String contentPath, byte[] images) {
		try {
			if (images == null || images.length == 0) {
				return contentPath + "/EWA_STYLE/images/pic_no.jpg";
			} else {
				// String path = UPath.getRealContextPath() +
				// UPath.PATH_IMG_CACHE;
				String path = UPath.getPATH_IMG_CACHE();
				String fileName = UFile.createMd5File(images, "jpg", path, false);

				File f1 = new File(fileName);
				// 页面显示内容URL
				String url = UPath.getPATH_IMG_CACHE_URL() + "/" + f1.getName();
				if (UPath.PATH_IMG_CACHE.startsWith("@")) {
					return url;
				} else {
					return contentPath + "/" + url;
				}
				// return contentPath + "/" + UPath.PATH_IMG_CACHE + "/" +
				// f1.getName();
			}
		} catch (Exception e) {
			return e.getMessage() == null ? "error" : e.getMessage();
		}
	}

	public String createItemHtml() throws Exception {
		String html = super.getXItemFrameHtml();

		UserXItemValues st = super.getUserXItem().getItem("Style");
		if (st.count() > 0) {
			UserXItemValue s = st.getItem(0);
			String s1 = s.getItem("Style");
			html = html.replace("{__EWA_IMG_STYLE__}", s1);
		}

		UserXItemValues us = super.getUserXItem().getItem("DataItem");
		if (us.count() == 0) {
			return null;
		}

		// 是否后加载
		boolean isLazyLoad = false;
		try {
			if (super.getUserXItem().testName("ImageDefault")) {
				UserXItemValues imageDefault = super.getUserXItem().getItem("ImageDefault");
				if (imageDefault.count() > 0) {
					UserXItemValue s = imageDefault.getItem(0);
					String ImageLazyLoad = s.getItem("ImageLazyLoad");
					if (ImageLazyLoad.equals("yes")) {
						isLazyLoad = true;
					}
				}
			}
		} catch (Exception err) {
		}

		// ewa_conf.xml 自定义的 静态文件前缀
		String rvEwaStylePath = super.getHtmlClass().getSysParas().getRequestValue().s("RV_EWA_STYLE_PATH");
		if (StringUtils.isBlank(rvEwaStylePath)) {
			rvEwaStylePath = "/EmpScriptV2";
		}
		Object a = super.getHtmlClass().getItemValues().getTableValue(super.getUserXItem());
		if (a == null) {
			String default_img = getImageDefult();
			// 替换成默认图
			html = html.replace(SkinFrame.TAG_VAL, default_img);

			// 后加载先不显示背景图
			String background = isLazyLoad ? "" : "background-image:url('" + default_img + "')";
			html = html.replace("{__BACKGROUND_IMAGE__}", background);
			html = html.replace("{__IS_LAZY_LOAD__}", isLazyLoad + "");
			return html;
		}

		String t = a.getClass().toString();
		byte[] test = new byte[1];
		String image = "";
		if (t.equalsIgnoreCase(test.getClass().toString())) {// binary
			try {
				byte[] images = (byte[]) a;
				String imgPath;
				if (images == null || images.length == 0) {
					imgPath = this.getImageDefult(); // 默认图
				} else {
					imgPath = getImage(rvEwaStylePath, images);
				}
				image = imgPath;
			} catch (Exception e) {
				return e.getMessage() == null ? "error" : e.getMessage();
			}
		} else {
			String v = "";
			String a1 = a.toString();
			if (a1.length() > rvEwaStylePath.length()
					&& a1.substring(0, rvEwaStylePath.length()).equals(rvEwaStylePath)) {
				v = a1;
			} else {
				if (a1.startsWith("http:") || a1.startsWith("https:")) {
					v = a1;
				} else {
					a1 = a1.replace("\\", "/"); // 替换windows 目录分隔符 2018-02-13
					v = a1.replace("//", "/").replace("//", "/").replace("//", "/");
				}
			}
			image = v;
		}
		html = html.replace(SkinFrame.TAG_VAL, image);
		// 后加载先不显示背景图
		String background = isLazyLoad ? "" : "background-image:url('" + image + "')";
		html = html.replace("{__BACKGROUND_IMAGE__}", background);
		html = html.replace("{__IS_LAZY_LOAD__}", isLazyLoad + "");

		return html;
	}

	/**
	 * 当图片字段不存在时候，返回的默认图片
	 * 
	 * @return
	 */
	private String getImageDefult() {
		String p = super.getHtmlClass().getSysParas().getRequestValue().s("RV_EWA_STYLE_PATH");
		if (StringUtils.isBlank(p)) {
			p = "/EmpScriptV2"; // default static url
		}
		String path = p + "/EWA_STYLE/images/pic_no.jpg";
		try {
			if (super.getUserXItem().testName("ImageDefault")) {
				UserXItemValues imageDefault = super.getUserXItem().getItem("ImageDefault");
				if (imageDefault.count() > 0) {
					UserXItemValue s = imageDefault.getItem(0);
					String defImg = s.getItem("ImageDefault");
					String url = s.getItem("ImageUrl");

					if (defImg.equals("transparent")) {
						return p + "/EWA_STYLE/images/transparent.png";
					} else if (defImg.equals("url")) {
						return super.getHtmlClass().getItemValues().replaceParameters(url, false);
					} else { // 默认图片 pic_no.jpg
						return path;
					}
				}
			}
		} catch (Exception err) {
			System.err.println(err.getMessage());
		}

		return path;
	}
}
