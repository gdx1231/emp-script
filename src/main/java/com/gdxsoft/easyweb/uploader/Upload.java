package com.gdxsoft.easyweb.uploader;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.HtmlClass;
import com.gdxsoft.easyweb.script.display.ItemValues;
import com.gdxsoft.easyweb.script.display.SysParameters;
import com.gdxsoft.easyweb.script.display.frame.FrameParameters;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValues;
import com.gdxsoft.easyweb.utils.UAes;
import com.gdxsoft.easyweb.utils.UConvert;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UImages;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.fileConvert.Cvt2Swf;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class Upload {
	private static Logger LOGGER = LoggerFactory.getLogger(Upload.class);
	/**
	 * 上传路径的最大长度限制 255，可以修改
	 */
	public static int UPLOAD_PATH_MAX_CHARS = 255;

	/**
	 * 默认的上传路径upload_files，不可修改
	 */
	public static final String DEFAULT_UPLOAD_PATH = "upload_files";
	static char[] hex = "0123456789ABCDEF".toCharArray();

	// private HttpServletRequest _request;
	private String _uploadDir;
	private String _uploadRealDir;
	private String _uploadTempDir;
	private String _rootPath;
	private String _upNewSizes; // 创建图片尺寸 "860x645,100x75,300x225,200x150"
	private String _NewSizesIn; // 创建图片尺寸模式（server/client）
	private boolean _IsRunUpSQLResized; // 重新生成图片是否执行Upsql

	private HashMap<String, Boolean> _UpExts; // 允许的扩展名

	private String _upSql;
	// private String _upSaveMethod = "File";
	private String _upSwf;
	private String _upDelete;
	private String _upUnZip;
	private DataConnection _Conn;
	private List<FileUpload> _AlFiles;
	private String _uploadName;

	private List<?> _UploadItems;

	private RequestValue _Rv;

	private String upLimit = "10M"; // 上传大小限制，默认10M
	private long limitBytes = 1024 * 1024 * 10L; // 10m bytes

	private boolean upJsonEncyrpt = true; // 返回Json是否加密

	// 配置文件定义的类型 ,写数据库用，因为Postgresql是强类型
	private HashMap<String, String> userConfigTypes;

	public RequestValue getRv() {
		return _Rv;
	}

	public void setRv(RequestValue rv) {
		_Rv = rv;
	}

	public List<?> getUploadItems() {
		return _UploadItems;
	}

	public void setUploadItems(List<?> uploadItems) {
		_UploadItems = uploadItems;
	}

	/**
	 * 从tomcat 7.6以上版本，|符号不能之间传递，必须转义，否则抛出400错误<br>
	 * 郭磊 2016-11-22
	 * 
	 * @param s1
	 * @return
	 */
	public String decode(String s1) {
		if (s1 == null || s1.isEmpty()) {
			return s1;
		}
		if (s1.indexOf("%") < 0) {
			return s1;
		}

		try {
			return java.net.URLDecoder.decode(s1, "utf-8");
		} catch (UnsupportedEncodingException e) {
			return s1;
		}
	}

	/**
	 * 初始化数据配置文件定义的类型 ,写数据库用，因为Postgresql是强类型
	 * 
	 * @param uc
	 * @throws Exception
	 */
	private void initUserConfigTypes(UserConfig uc) throws Exception {
		this.userConfigTypes = new HashMap<>();
		for (int i = 0; i < uc.getUserXItems().count(); i++) {

			UserXItem uxi = uc.getUserXItems().getItem(i);
			if (!uxi.testName("DataItem") || uxi.getItem("DataItem").count() == 0) {
				continue;
			}

			UserXItemValues uxv = uxi.getItem("DataItem");
			String dataType = uxv.getItem(0).getItem("DataType");

			String dataField = uxv.getItem(0).getItem("DataField").toUpperCase().trim();
			String name = uxi.getName().toUpperCase().trim();

			this.userConfigTypes.put(dataField, dataType);
			this.userConfigTypes.put(name, dataType);
		}
	}

	/**
	 * 初始化
	 * 
	 * @param uploadXmlName  上传文件配置文件
	 * @param uploadItemName 上传文件配置项
	 * @param name           配置项的上传参数名称
	 * @throws Exception
	 */
	public void init(String uploadXmlName, String uploadItemName, String name) throws Exception {

		ItemValues iv = new ItemValues();
		HtmlClass hc = new HtmlClass();
		hc.setItemValues(iv);
		iv.setHtmlClass(hc);

		SysParameters newSysParas = new SysParameters();
		iv.getHtmlClass().setSysParas(newSysParas);
		iv.getSysParas().setRequestValue(this._Rv);

		this._UpExts = new HashMap<String, Boolean>();

		UserConfig uc = UserConfig.instance(uploadXmlName, uploadItemName, null);
		if (!uc.getUserXItems().testName(name)) { // 无效
			throw new Exception("Can't find upload field name, " + name);
		}

		// 初始化数据配置文件定义的类型 ,写数据库用，因为Postgresql是强类型
		this.initUserConfigTypes(uc);

		_uploadDir = DEFAULT_UPLOAD_PATH;
		UserXItem uxi = uc.getUserXItems().getItem(name);
		if (!uxi.testName("Upload")) {
			LOGGER.error("Invalid cfg {},{},{}", uploadXmlName, uploadItemName, name);
			throw new Exception(_uploadName + " Invalid cfg");
		}
		_uploadName = uxi.getName();
		if (uxi.getItem("Upload").count() == 0) {
			LOGGER.error("Invalid cfg {},{},{}", uploadXmlName, uploadItemName, name);
			throw new Exception(_uploadName + " Invalid cfg");
		}
		UserXItemValue u = uxi.getItem("Upload").getItem(0);
		if (u.testName("UpPath")) {
			String p = u.getItem("UpPath");
			if (!p.trim().equals("")) {
				// p = iv.replaceParameters(p, false);
				//替换参数在upload中调用，因为此时的rv数据不完整
				_uploadDir = p;
			}
		}
		if (u.testName("UpSaveMethod")) {
			// _upSaveMethod = u.getItem("UpSaveMethod");
		}
		if (u.testName("UpSql")) {
			_upSql = u.getItem("UpSql");
		}
		if (u.testName("UpNewSizes")) {
			_upNewSizes = u.getItem("UpNewSizes");
		}
		// 是在客户端还是服务器端生成新尺寸图片
		if (u.testName("NewSizesIn")) {
			this._NewSizesIn = u.getItem("NewSizesIn");
		}
		if (u.testName("Up2Swf")) {
			_upSwf = u.getItem("Up2Swf");
			if (_upSwf.equalsIgnoreCase("yes")) {
				this._UpExts.put("SWF", true);
			}
		}
		if (u.testName("UpDelete")) {
			_upDelete = u.getItem("UpDelete");
		}
		if (u.testName("UpUnZip")) {
			_upUnZip = u.getItem("UpUnZip");
		}

		// 上传文件大小限制
		if (u.testName("UpLimit")) {
			String limit = u.getItem("UpLimit").trim();
			if (limit.length() > 0) {
				this.initLimitBytes(limit);
			}
		}

		this._IsRunUpSQLResized = false;
		// _IsRunUpSQLResized
		if (u.testName("RunUpSQLResized")) {
			String v = u.getItem("RunUpSQLResized");
			if (v != null && v.equalsIgnoreCase("yes")) {
				this._IsRunUpSQLResized = true;
			}
		}
		// UpExts
		if (u.testName("UpExts")) {
			String up_exts = u.getItem("UpExts");
			if (up_exts.trim().length() > 0) {
				String[] exts = up_exts.split(",");
				for (int i = 0; i < exts.length; i++) {
					String ext = exts[i].trim().toUpperCase();
					this._UpExts.put(ext, true);
					if (ext.equals("JPG")) {
						this._UpExts.put("JPEG", true);
					}
				}
			}
		}

		// UpJsonEncyrpt 返回Json是否加密
		if (u.testName("UpJsonEncyrpt")) {
			String val = u.getItem("UpJsonEncyrpt");
			if ("no".equalsIgnoreCase(val)) {
				this.upJsonEncyrpt = false; // 不加密
			}
		}

		if (this._upSql != null && this._upSql.trim().length() > 0) {
			String ds = uc.getUserPageItem().getSingleValue("DataSource");
			this._Conn = new DataConnection();
			this._Conn.setConfigName(ds);
		}

		// 覆盖新尺寸设定 例如：800x600,400x300
		if (this._Rv.getString(FrameParameters.EWA_UP_NEWSIZES) != null) {
			String para = this._Rv.getString(FrameParameters.EWA_UP_NEWSIZES).trim().toLowerCase().replace(" ", "");
			if (para.length() > 0) {
				_upNewSizes = para;
			}
		}
		String root;
		/*
		 * 在 ewa_conf.xml中的设定
		 * 
		 * <path des="图片缩略图保存根路径" Name="img_tmp_path"
		 * Value="@/Users/admin/java/img_tmps/" /> <path des=
		 * "图片缩略图保存根路径URL, ！！！需要在Tomcat或Apache或Nginx中配置虚拟路径！！！。" Name="img_tmp_path_url"
		 * Value="/img_tmps/" />
		 */
		root = UPath.getPATH_UPLOAD();

		File r = new File(root);
		root = r.getAbsolutePath();
		this._rootPath = root;

		_uploadRealDir = root + "/" + _uploadDir;
		_uploadTempDir = root + "/_temp_";

		File f2 = new File(this._uploadTempDir);
		_uploadTempDir = f2.getAbsolutePath();
		if (!f2.exists()) {
			f2.mkdirs();
		}

		_AlFiles = new ArrayList<FileUpload>();
	}

	/**
	 * 根据上传参数初始化上传文件大小bytes
	 * 
	 * @param limit 512k or 12m or 1024
	 * @throws Exception
	 */
	private void initLimitBytes(String limit) throws Exception {
		String limit1 = limit.trim().toUpperCase().replace(",", "").replace(" ", "");
		if (limit1.endsWith("M") || limit1.endsWith("K")) {
			String num0Str = limit1.substring(0, limit1.length() - 1);
			long num0;
			try {
				num0 = Long.parseLong(num0Str);
			} catch (Exception err) {
				throw new Exception("Invalid parameter UpLimit ->" + limit);
			}

			if (limit1.endsWith("M")) {
				this.limitBytes = num0 * 1024 * 1024; // 1MByte=1024KByte（M是英文Million的缩写，百万。）
			} else if (limit1.endsWith("K")) {
				this.limitBytes = num0 * 1024; // 1KByte=1024Byte（K是英文Kilo的缩写，千。）
			}

		} else {
			try {
				long v = Long.parseLong(limit);
				this.limitBytes = v;
			} catch (Exception err) {
				throw new Exception("Invalid parameter UpLimit ->" + limit);
			}
		}
		this.upLimit = limit;
	}

	public void init(HttpServletRequest request) throws Exception {
		// _request = request;
		RequestValue rv = this._Rv;
		// 从tomcat 7.6以上版本，|符号不能之间传递，必须转义，否则抛出400错误
		String xmlName = decode(rv.s(FrameParameters.XMLNAME));
		String itemName = decode(rv.s(FrameParameters.ITEMNAME));
		String name = decode(this._Rv.s("NAME"));
		if (xmlName == null || itemName == null || name == null) {
			LOGGER.error("上传配置失败：参数未传递(XMLNAME,ITEMNAME, NAME)");
			throw new Exception("上传配置失败：参数未传递(XMLNAME,ITEMNAME, NAME)");
		}

		this.init(xmlName, itemName, name);
	}

	/**
	 * 检查上传文件的合法文件名
	 * 
	 * @param fu
	 * @return
	 */
	public boolean checkValidExt(FileUpload fu) {
		if (fu == null || fu.getExt() == null) {
			return false;
		}
		String ext = fu.getExt().trim().toUpperCase();
		if (this._UpExts != null && this._UpExts.containsKey(ext)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 检查文件大小限制
	 * 
	 * @param fu
	 * @return
	 */
	private boolean checkSizeLimit(FileUpload fu) {
		if (fu == null) {
			return false;
		}

		return fu.getLength() <= this.limitBytes;
	}

	/**
	 * 处理文件上传
	 * 
	 * @throws Exception
	 */
	public String upload() throws Exception {
		if (_uploadDir == null) {
			throw new Exception("The upload dir not defined ");
		}
		// 替换路径的参数
		this._uploadDir = this._Rv.replaceParameters(_uploadDir);
		if (this._uploadDir.indexOf("..") >= 0) { // 避免出现 ../../../root的风险
			LOGGER.error("Invalid char '..' in upload dir, {}", _uploadDir);
			throw new Exception("Invalid char '..' in upload dir, " + this._uploadDir);
		}
		if (this._uploadDir.length() > UPLOAD_PATH_MAX_CHARS) { // 避免路径过长，超过系统限制
			LOGGER.error("Upload dir full name limit {} chars, {}", UPLOAD_PATH_MAX_CHARS, this._uploadDir);
			throw new Exception("Upload dir  full name limit " + UPLOAD_PATH_MAX_CHARS + " chars, " + this._uploadDir);
		}
		this._uploadRealDir = this._Rv.replaceParameters(_uploadRealDir);
		UFile.buildPaths(this._uploadRealDir);
		
		int m = 0;
		if (this._UploadItems != null) { // 处理文件上传
			for (int i = 0; i < this._UploadItems.size(); i++) {
				FileItem item = (FileItem) this._UploadItems.get(i);
				if (item.isFormField() && item.getFieldName().equals(_uploadName) || !item.isFormField()) {
					FileUpload fu = this.takeFileUpload(item, m);
					handleSubs(fu);
					if (!this.checkSizeLimit(fu)) {
						String msg = "the file size limit exceeded " + item.getName() + " " + fu.getLength() + " > "
								+ this.upLimit;
						LOGGER.error(msg);
						throw new Exception(msg);
					}
					if (!this.checkValidExt(fu)) {
						String msg = "the upload file ext " + item.getName() + " is invalid";
						LOGGER.error(msg);
						throw new Exception(msg);
					}
					this._AlFiles.add(fu);
					this.createNewSizeImages(fu);
					m++;
				}
			}
		} else {
			String v = this._Rv.getString(this._uploadName);
			FileUpload fu = this.takeFileUpload(v, m);
			handleSubs(fu);
			this._AlFiles.add(fu);
			this.createNewSizeImages(fu);
			m++;
		}

		this.handleClientNewSizes();

		// 调用SQL
		if (this._upSql != null && this._upSql.trim().length() > 0) {
			this.writeToDatabase();
		}
		return createJSon();
	}

	public void handleClientNewSizes() {
		if (this._upNewSizes == null || this._upNewSizes.trim().length() == 0) {
			return;
		}

		if (this._NewSizesIn != null && !this._NewSizesIn.equalsIgnoreCase("client")) {
			// 客户端生成
			return;
		}
		FileUpload root = null;
		for (int i = 0; i < this._AlFiles.size(); i++) {
			FileUpload a = this._AlFiles.get(i);
			if (a.getUserLocalPath() != null && a.getUserLocalPath().indexOf("resized$") == -1) {
				root = a;
				break;
			}
		}
		if (root == null) {
			return;
		}
		ArrayList<FileUpload> alFilesNew = new ArrayList<FileUpload>();
		for (int i = 0; i < this._AlFiles.size(); i++) {
			FileUpload a = this._AlFiles.get(i);
			if (a.getUserLocalPath() != null && a.getUserLocalPath().indexOf("resized$") == 0) {
				root.getSubs().add(a);
			} else {
				alFilesNew.add(a);
			}
		}
		this._AlFiles = alFilesNew;

		this.moveClientNewSizesFileToRootPath(root);
	}

	/**
	 * 将客户端生成文件移动到resized目录
	 * 
	 * @param root
	 * @param sub
	 */
	public void moveClientNewSizesFileToRootPath(FileUpload root) {
		File uploadedFile = new File(root.getSavePath());
		String path = uploadedFile.getAbsolutePath() + "$resized";
		File pathResized = new File(path);
		pathResized.mkdirs();

		for (int i = 0; i < root.getSubs().size(); i++) {
			FileUpload sub = root.getSubs().get(i);
			String subName = sub.getUserLocalPath().replace("resized$", "");
			File dest = new File(pathResized.getAbsoluteFile() + "/" + subName);
			File sub1 = new File(sub.getSavePath());

			sub1.renameTo(dest);

			sub.setSaveFileName(dest.getName());
			sub.setSavePath(dest.getAbsolutePath());
			sub.setFileUrl(this.createURL(dest));
			sub.setUnid(root.getUnid());
		}
	}

	/**
	 * 解压缩文件
	 * 
	 * @param zipFile
	 * @return
	 */
	public List<FileUpload> upzipFiles(String zipFile) {
		try {
			List<FileUpload> fus = new ArrayList<FileUpload>();
			List<String> files = UFile.unZipFile(zipFile);
			for (int i = 0; i < files.size(); i++) {
				File f1 = new File(files.get(i));
				if (f1.getName().startsWith(".")) {
					continue;
				}
				FileUpload fu = this.createFileUpload(f1);
				fu.setFileUrl(this.createURL(f1));
				fu.setUnid("");
				fus.add(fu);
			}
			return fus;
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	public String createJSon() {
		MStr sb = new MStr();
		sb.append("[");
		for (int i = 0; i < this._AlFiles.size(); i++) {
			FileUpload fu1 = this._AlFiles.get(i);
			String s = this.createJSon(fu1);

			if (i > 0) {
				sb.append(",");
			}

			char[] chars = s.toCharArray();
			for (int m = 0; m < chars.length; m++) {
				char c = chars[m];
				if (c > 0xff) {
					sb.a(unicode(c));
				} else {
					sb.a(c);
				}
			}
			// sb.append(s);

			for (int kk = 0; kk < fu1.getSubs().size(); kk++) {
				FileUpload fu2 = fu1.getSubs().get(kk);
				String s1 = this.createJSon(fu2);
				sb.append(",");
				sb.append(s1);

				for (int zz = 0; zz < fu2.getSubs().size(); zz++) {
					FileUpload fu21 = fu2.getSubs().get(zz);
					String s11 = this.createJSon(fu21);
					sb.append(",");
					sb.append(s11);
				}
			}
		}
		sb.append("]");
		return sb.toString();
	}

	public String unicode(char c) {
		MStr sb = new MStr();
		sb.a("\\u");
		int n = c;
		for (int i = 0; i < 4; ++i) {
			int digit = (n & 0xf000) >> 12;
			sb.a(hex[digit]);
			n <<= 4;
		}
		return sb.toString();
	}

	public String createJSon(FileUpload fu) {
		JSONObject json = new JSONObject();
		boolean isReal = UPath.getPATH_UPLOAD_URL() != null;

		try {
			json.put("UP_NAME", fu.getSaveFileName());
			json.put("UP_URL", fu.getFileUrl());
			json.put("UP_UNID", fu.getUnid());
			json.put("ISREAL", isReal);
			if (fu.getUserLocalPath() != null) {
				json.put("UP_LOCAL_NAME", fu.getUserLocalPath());
			}
			json.put("UP_SIZE", fu.getLength());
			if (isReal) {
				json.put("CT", UPath.getPATH_UPLOAD_URL());
			}
			if (!this.upJsonEncyrpt) {
				return json.toString();
			} else {
				String encrypt = UAes.getInstance().encrypt(json.toString());
				JSONObject json1 = new JSONObject();
				json1.put("UP", encrypt);
				return json1.toString();
			}
		} catch (Exception err) {
			return "{\"RST\":false, \"ERR\":\"" + Utils.textToJscript(err.getMessage()) + "\"}";
		}

	}

	public FileUpload createFileUpload(File f) {
		FileUpload fu = new FileUpload();
		fu.setSavePath(f.getAbsolutePath());
		fu.setExt(UFile.getFileExt(f.getName()));
		fu.setSaveFileName(f.getName());

		String url = createURL(f);
		fu.setFileUrl(url);

		fu.setUnid(Utils.getGuid());
		fu.setContextType("image/jpeg");
		fu.setUserLocalPath("");
		fu.setLength((int) f.length());
		return fu;
	}

	/**
	 * 生成上传文件的URL
	 * 
	 * @param f
	 * @return
	 */
	public String createURL(File f) {
		/*
		 * 在 ewa_conf.xml中的设定
		 * 
		 * <path des="图片缩略图保存根路径" Name="img_tmp_path"
		 * Value="@/Users/admin/java/img_tmps/" /> <path des=
		 * "图片缩略图保存根路径URL, ！！！需要在Tomcat或Apache或Nginx中配置虚拟路径！！！。" Name="img_tmp_path_url"
		 * Value="/img_tmps/" />
		 */

		String url;
		if (UPath.getPATH_UPLOAD_URL() == null) {
			url = f.getAbsolutePath().replace(this._rootPath, "/");
		} else {
			url = UPath.getPATH_UPLOAD_URL() + f.getAbsolutePath().replace(this._rootPath, "/");
		}
		url = url.replace("\\", "/").replace("//", "/");
		return url;
	}

	public void handleSubs(FileUpload fu) {
		File uploadedFile = fu.getUploadedFile();
		if (this._upUnZip != null && this._upUnZip.equalsIgnoreCase("yes")) {
			if (fu.getExt().equalsIgnoreCase("zip")) {
				List<FileUpload> lst = this.upzipFiles(uploadedFile.getAbsolutePath());
				for (int i = 0; i < lst.size(); i++) {
					lst.get(i).setUnid(fu.getUnid());
					fu.getSubs().add(lst.get(i));
				}
			}
		} else if (_upSwf != null && _upSwf.equalsIgnoreCase("yes")) {
			Cvt2Swf swf = new Cvt2Swf();
			String targetPath = UFile.changeFileExt(uploadedFile.getAbsolutePath(), "swf");
			File target = new File(targetPath);
			boolean rst = swf.cvt2Swf(uploadedFile.getAbsolutePath(), target.getAbsolutePath());
			if (rst) {
				fu.setSaveFileName(target.getName());
				fu.setSavePath(target.getAbsolutePath());
				String fileUrl = target.getAbsolutePath().replace(this._rootPath, "/").replace("\\", "/").replace("//",
						"/");
				fu.setFileUrl(fileUrl);

				// 删除上传原始文件
				if (this._upDelete != null && this._upDelete.equalsIgnoreCase("yes")) {
					swf.deletePdf();
					swf.deleteSource();
				}
			}
			this._AlFiles.add(fu);
		}
	}

	public void createNewSizeImages(FileUpload fu) {
		if (this._upNewSizes == null || this._upNewSizes.trim().length() == 0) {
			return;
		}

		// 默认服务器端创建 2020-01-12
		if (this._NewSizesIn != null && this._NewSizesIn.equalsIgnoreCase("client")) {
			// 客户端生成
			return;
		}

		String[] sizes = this._upNewSizes.split("\\,");
		ArrayList<Dimension> ds = new ArrayList<Dimension>();
		for (int i = 0; i < sizes.length; i++) {
			String s = sizes[i];
			String[] s1 = s.split("x");
			if (s1.length != 2) {
				continue;
			}
			int w = 0;
			int h = 0;
			try {
				w = Integer.parseInt(s1[0]);
				h = Integer.parseInt(s1[1]);
				if (w <= 5 || w > 3000 || h <= 5 || h > 3000) {
					// 尺寸太大或太小
					continue;
				}
			} catch (Exception e) {
				System.err.println(e.getMessage());
				continue;
			}
			Dimension d = new Dimension();
			d.setSize(w, h);
			ds.add(d);
		}
		if (ds.size() == 0)
			return;
		Dimension[] dd = new Dimension[ds.size()];
		dd = ds.toArray(dd);
		String imgPath = fu.getSavePath();
		try {
			// sub 记录生成图片
			for (int ia = 0; ia < fu.getSubs().size(); ia++) {
				FileUpload fuSub = fu.getSubs().get(ia);
				File[] ff = UImages.createResized(fuSub.getSavePath(), dd);
				for (int i = 0; i < ff.length; i++) {
					File f1 = ff[i];
					FileUpload fu2 = this.createFileUpload(f1);
					fu2.setUnid(fu.getUnid());
					fu2.setFileUrl(this.createURL(f1));
					fuSub.getSubs().add(fu2);
				}
			}

			File[] ffa = UImages.createResized(imgPath, dd);
			for (int i = 0; i < ffa.length; i++) {
				File f1 = ffa[i];
				FileUpload fu2 = this.createFileUpload(f1);
				fu2.setUnid(fu.getUnid());
				fu2.setFileUrl(this.createURL(f1));
				fu.getSubs().add(fu2);
			}
			return;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return;
		}
	}

	public void writeToDatabase() throws Exception {
		HashMap<String, Boolean> map = new HashMap<String, Boolean>();
		// 根据配置数据类型调整rv的数据类型，因为Postgresql是强类型
		this.userConfigTypes.forEach((String name, String dataType) -> {
			if (this._Rv.getObject(name) != null) {
				this._Rv.changeValue(name, this._Rv.getObject(name), dataType, 10000);
			}
		});
		try {
			for (int i = 0; i < this._AlFiles.size(); i++) {
				FileUpload fu = this._AlFiles.get(i);
				String url = fu.getFileUrl();
				if (map.containsKey(url)) {
					// 过滤重复的文件
					continue;
				}
				map.put(url, true);
				this.writeToDatabase(fu);
				if (this._IsRunUpSQLResized) {// 重新生成的图片
					for (int kk = 0; kk < fu.getSubs().size(); kk++) {
						FileUpload fuSub = fu.getSubs().get(kk);
						this.writeToDatabase(fuSub);
					}
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			this._Conn.close();
		}
	}

	public void writeToDatabase(FileUpload fu) throws Exception {
		File img = new File(fu.getSavePath());
		byte[] buf;
		try {
			if (this._upSql.toUpperCase().indexOf("@EWA_UP_FILE") > 0) {
				if (img.length() > 1024 * 1024 * 10) {
					buf = new byte[1];
					buf[0] = 0;
					LOGGER.error("上传文件[" + img.getAbsolutePath() + "]: 超过10M");
				} else {
					buf = UFile.readFileBytes(img.getAbsolutePath());
				}
			} else {
				buf = null;
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw e;
		}

		String name = fu.getSaveFileName();
		String ext = fu.getExt();
		String type = fu.getContextType();
		String url = fu.getFileUrl();
		String path = fu.getSavePath();
		String unid = fu.getUnid();
		String from = fu.getFrom() == null ? "" : fu.getFrom().getUnid();

		if (fu.getLength() == 0) {
			fu.setLength((int) img.length());
		}

		this.writeToDatabase(buf, name, path, type, ext, url, unid, from, fu.getUserLocalPath(), fu.getLength());

		if (this._upDelete != null && this._upDelete.equalsIgnoreCase("yes")) {
			try {
				img.delete();
			} catch (Exception err) {
				LOGGER.error("删除上传文件[" + img.getAbsolutePath() + "]: " + err.getMessage());
			}
		}

	}

	/**
	 * 参数列表<br>
	 * EWA_UP_MD5 文件的md5校验码<br>
	 * EWA_UP_FILE 文件的二进制<br>
	 * EWA_UP_NAME 文件名称<br>
	 * EWA_UP_URL 文件http的url<br>
	 * EWA_UP_EXT 文件扩展名<br>
	 * EWA_UP_PATH 文件服务器保存位置<br>
	 * EWA_UP_PATH_SHORT 文件服务器保存位置(去除UPath.getPATH_UPLOAD() 的路径)<br>
	 * EWA_UP_TYPE 文件的http的contentType<br>
	 * EWA_UP_UNID 文件的unid，用于数据库定位使用<br>
	 * EWA_UP_FROM 来源，图片重新生成尺寸的母体<br>
	 * EWA_UP_LOCAL 用户本地文件<br>
	 * EWA_UP_LENGTH 长度<br>
	 * 
	 * @param buf      文件二进制内容
	 * @param fileName 文件名
	 * @param filePath 服务器保存路径
	 * @param fileType 类型
	 * @param fileExt  扩展名
	 * @param fileUrl  http的url
	 * @param unid     文件的unid，用于数据库定位使用
	 * @param from     来源的UNID
	 * @throws Exception
	 */
	public void writeToDatabase(byte[] buf, String fileName, String filePath, String fileType, String fileExt,
			String fileUrl, String unid, String from, String userLocalName, int len) throws Exception {
		RequestValue rv = this._Rv;
		// INSERT INTO NWS_IMG(NWS_IMG,NWS_IMG_EXT,NWS_IMG_CDATE,NWS_IMG_MD5)
		// VALUES(@UP_FILE,@UP_EXT,@SYS_DATE,@UP_MD5)

		JSONObject uploadJson = new JSONObject();
		if (this._upSql.toUpperCase().indexOf("@EWA_UP_FILE") > 0) { // SQL包含@EWA_UP_FILE关键字
			if (buf != null && buf.length > 0) {
				// 文件二进制
				PageValue pvUpFile = new PageValue("EWA_UP_FILE", "binary", buf, buf.length);
				rv.addValue(pvUpFile);
			}
		}
		// 文件服务器名称
		PageValue pvUpFileName = new PageValue("EWA_UP_NAME", fileName);
		rv.addValue(pvUpFileName);
		uploadJson.put("EWA_UP_NAME", fileName);

		// 文件http的url
		PageValue pvUpFileUrl = new PageValue("EWA_UP_URL", fileUrl);
		rv.addValue(pvUpFileUrl);
		uploadJson.put("EWA_UP_URL", fileUrl);

		// 文件扩展名
		PageValue pvUpFileExt = new PageValue("EWA_UP_EXT", fileExt);
		rv.addValue(pvUpFileExt);
		uploadJson.put("EWA_UP_EXT", fileExt);

		// 文件服务器保存位置(全路径)
		PageValue pvUpFilePath = new PageValue("EWA_UP_PATH", filePath);
		rv.addValue(pvUpFilePath);
		uploadJson.put("EWA_UP_PATH", filePath);

		// EWA_UP_PATH_SHORT 文件服务器保存位置(去除UPath.getPATH_UPLOAD() 的路径)
		String shortPath = this._uploadDir + "/" + fileName;
		PageValue pvUpFilePathShort = new PageValue("EWA_UP_PATH_SHORT", shortPath);
		rv.addValue(pvUpFilePathShort);
		uploadJson.put("EWA_UP_PATH_SHORT", shortPath);

		// 文件的http的contenttype
		PageValue pvUpFileType = new PageValue("EWA_UP_TYPE", fileType);
		rv.addValue(pvUpFileType);
		uploadJson.put("EWA_UP_TYPE", fileType);

		// 文件GUNID
		PageValue pvUpUnid = new PageValue("EWA_UP_UNID", unid);
		rv.addValue(pvUpUnid);
		uploadJson.put("EWA_UP_UNID", unid);

		// 来源，用于图片的重新生成
		PageValue pvUpFrom = new PageValue("EWA_UP_FROM", from);
		rv.addValue(pvUpFrom);
		uploadJson.put("EWA_UP_FROM", from);

		// 服务器本地路径
		PageValue pvUpUserName = new PageValue("EWA_UP_LOCAL", userLocalName);
		rv.addValue(pvUpUserName);
		uploadJson.put("EWA_UP_LOCAL", userLocalName);

		rv.addValue("EWA_UP_LENGTH", len, "int", 10);
		rv.changeValue("EWA_UP_LENGTH", len, "int", 10);
		uploadJson.put("EWA_UP_LENGTH", len);

		PageValue pvUpJson = new PageValue("EWA_UP_JSON", uploadJson.toString());
		rv.addValue(pvUpJson);

		// 文件md5
		if (this._upSql.toUpperCase().indexOf("@EWA_UP_MD5") > 0) { // SQL包含@EWA_UP_MD5关键字
			try {
				String md5;
				if (buf == null) {
					md5 = UFile.createMd5(new File(filePath));
				} else {
					md5 = Utils.md5(buf);
				}
				PageValue pvUpMd5 = new PageValue("EWA_UP_MD5", md5);
				rv.addValue(pvUpMd5);
				PageValue pvUpMd5a = new PageValue("UP_MD5", md5);
				rv.addValue(pvUpMd5a);
				uploadJson.put("UP_MD5", md5);
			} catch (Exception err) {

			}
		}

		this._Conn.setRequestValue(rv);

		String[] sqls = this._upSql.split(";");
		for (int i = 0; i < sqls.length; i++) {
			String sql = sqls[i].trim();
			if (sql.length() == 0) {
				continue;
			}
			if (sql.toUpperCase().startsWith("SELECT")) {
				DTTable tb = DTTable.getJdbcTable(sql, this._Conn);
				rv.addValues(tb);

				this._Conn.executeQuery(sql);
			} else if (sql.toUpperCase().startsWith("CALL")) {
				this._Conn.executeProcdure(sql);
			} else {
				this._Conn.executeUpdate(sql);
			}
			if (this._Conn.getErrorMsg() != null && this._Conn.getErrorMsg().length() > 0) {
				LOGGER.error(this._Conn.getErrorMsg());
				throw new Exception(this._Conn.getErrorMsg());
			}
		}

	}

	/**
	 * 获取上传文件对象
	 * 
	 * @param itemobj
	 * @param index
	 * @return
	 */
	public FileUpload takeFileUpload(Object itemobj, int index) {
		FileUpload fu = new FileUpload();
		String name;
		if (itemobj instanceof String) {
			name = (Math.random() + "").replace(".", "_") + ".jpg";
			fu.setLength(itemobj.toString().length() / 3);
			fu.setContextType("image/jpeg");
		} else {
			FileItem item = (FileItem) itemobj;
			try {
				byte[] buf = item.getName().getBytes("UTF-8");
				name = new String(buf, "UTF-8");
				name = name.replace("?", ".");
			} catch (UnsupportedEncodingException e) {
				name = item.getName();
			}
			fu.setLength((int) item.getSize());
			fu.setContextType(item.getContentType());
		}
		// 用户本地文件名称
		fu.setUserLocalPath(name);
		String ext = UFile.getFileExt(name);
		String guid = Utils.getGuid();
		String fileName = guid + "_" + index;

		if (ext.length() > 0) {
			fileName = fileName + "." + ext;
		}
		String serverName = this._uploadDir + "/" + fileName;

		fu.setExt(ext);
		fu.setSaveFileName(fileName);
		fu.setSavePath(this._uploadRealDir + File.separator + fileName);
		fu.setFileUrl(serverName);
		fu.setUnid(guid);

		File uploadedFile = new File(fu.getSavePath());
		String url = createURL(uploadedFile);
		fu.setFileUrl(url);

		try {
			// 保持文件到本地目录
			this.writeFile(itemobj, uploadedFile);
			fu.setUploadedFile(uploadedFile);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return fu;
	}

	/**
	 * Save the upload file to the local file
	 * 
	 * @param itemobj
	 * @param uploadedFile
	 * @throws Exception
	 */
	public void writeFile(Object itemobj, File uploadedFile) throws Exception {
		if (itemobj instanceof String) {
			String base64 = itemobj.toString();
			// "data:image/jpeg;base64,
			String tag1 = ";base64,";
			int tagStart = base64.indexOf(tag1);
			String b64;
			if (tagStart > 0) {
				// String tag = base64.substring(0, tagStart + tag1.length());
				// System.out.println(tag);
				b64 = base64.substring(tagStart + tag1.length());
			} else {
				b64 = base64;
			}
			byte[] buf = UConvert.FromBase64String(b64);
			UFile.createBinaryFile(uploadedFile.getAbsolutePath(), buf, true);

		} else {
			FileItem item = (FileItem) itemobj;
			item.write(uploadedFile);
		}
	}

	public String getUploadDir() {
		return _uploadDir;
	}

	public void setUploadDir(String uploadDir) {
		this._uploadDir = uploadDir;
	}

	public String getUploadRealDir() {
		return _uploadRealDir;
	}

	public void setUploadRealDir(String uploadRealDir) {
		this._uploadRealDir = uploadRealDir;
	}

	public String getUploadTempDir() {
		return _uploadTempDir;
	}

	public void setUploadTempDir(String uploadTempDir) {
		this._uploadTempDir = uploadTempDir;
	}

	public String getUpNewSizes() {
		return _upNewSizes;
	}

	public void setUpNewSizes(String upNewSizes) {
		this._upNewSizes = upNewSizes;
	}

	public HashMap<String, Boolean> getUpExts() {
		return _UpExts;
	}

	public String getUpSql() {
		return _upSql;
	}

	public void setUpSql(String upSql) {
		this._upSql = upSql;
	}

	public String getUpSwf() {
		return _upSwf;
	}

	public void setUpSwf(String upSwf) {
		this._upSwf = upSwf;
	}

	public String getUpUnZip() {
		return _upUnZip;
	}

	public void setUpUnZip(String upUnZip) {
		this._upUnZip = upUnZip;
	}

	public String getUploadName() {
		return _uploadName;
	}

	public void setUploadName(String uploadName) {
		this._uploadName = uploadName;
	}

	public List<FileUpload> getAlFiles() {
		return _AlFiles;
	}

	/**
	 * @return the upLimit
	 */
	public String getUpLimit() {
		return upLimit;
	}

	/**
	 * @param upLimit the upLimit to set
	 */
	public void setUpLimit(String upLimit) {
		this.upLimit = upLimit;
	}

	/**
	 * 返回Json是否加密
	 * 
	 * @return the upJsonEncyrpt
	 */
	public boolean isUpJsonEncyrpt() {
		return upJsonEncyrpt;
	}

	/**
	 * 返回Json是否加密
	 * 
	 * @param upJsonEncyrpt the upJsonEncyrpt to set
	 */
	public void setUpJsonEncyrpt(boolean upJsonEncyrpt) {
		this.upJsonEncyrpt = upJsonEncyrpt;
	}
}
