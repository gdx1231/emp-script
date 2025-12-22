package com.gdxsoft.easyweb.cache;

import java.io.File;

import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.frame.FrameParameters;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.msnet.MStr;


/**
 * EWA脚本缓存文件, 用于缓存EWA脚本生成的内容
 * 
 * @author gdx
 *
 */
@Deprecated
public class CacheEwaScript {

	private RequestValue _Rv;
	private String _Path;
	private String _Name;
	private String _Content;
	private CacheLoadResult _Result;
	private Long _LastModifyed; //配置文件上次保存时间

	public CacheEwaScript(RequestValue rv) {
		_Rv = rv;
		this.init();
	}

	public CacheEwaScript(String xmlName, String itemName) {
		this.initPath(xmlName, itemName);
	}

	/**
	 * 清除Cache文件,用于ConfigCache发现配置文件改变时
	 */
	synchronized public void removeCached() {
		if (this._Path == null) {
			return;
		}
		File f = new File(this._Path);
		if (f.isFile()) {
			return;
		}
		File[] files = f.listFiles();
		for (int i = 0; i < files.length; i++) {
			files[i].delete();
		}
	}

	private void initPath(String xmlName, String itemName) {
		if (this._Rv == null) {
			return;
		}
		String cp = this._Rv.getString("EWA.CP");

		String xmlFileName = xmlName.replace("||", "").replace("..", "");
		while (xmlFileName.indexOf("..") >= 0) {
			xmlFileName = xmlFileName.replace("..", "");
		}
		while (xmlFileName.indexOf("||") >= 0) {
			xmlFileName = xmlFileName.replace("||", "");
		}

		File f = new File(UPath.getScriptPath() + "/" + xmlFileName.replace("|", "/"));
		_LastModifyed = f.lastModified();

		String xmlName1 = xmlFileName.replace("|", "_");
		if (!xmlName1.startsWith("_")) {
			xmlName1 = "_" + xmlName1;
		}
		xmlName1 = xmlName1.toUpperCase();

		String itemName1 = itemName.replace("||", "").replace("..", "")
				.replace("|", "_").replace("*", "G").replace("?", "D");
		itemName1 = itemName1.toUpperCase();
		String pathRoot = UPath.getCachedPath();
		_Path = pathRoot + "/" + cp + "/" + xmlName1 + "/" + itemName1;

		UFile.buildPaths(_Path);
	}

	public void init() {
		String xmlName = _Rv.getString(FrameParameters.XMLNAME);
		String itemName = _Rv.getString(FrameParameters.ITEMNAME);

		this.initPath(xmlName, itemName);

		_Name = _Rv.getParameterHashCode() + ".jzy";
	}

	/**
	 * 获取文件内容，如果文件超时，则删除文件并返回空
	 * 
	 * @param cachedSeconds
	 *            缓存时间
	 * @return
	 * @throws Exception
	 */
	public boolean getCachedContent(int cachedSeconds) throws Exception {
		File f = new File(this._Path + "/" + this._Name);
		File fHash = new File(this._Path + "/" + this._Name + ".hash.txt");
		if (!f.exists() || !fHash.exists()) {
			this._Result = CacheLoadResult.FILE_NOT_EXISTS;
			return false;
		}
		long t = System.currentTimeMillis();
		long dif = t - f.lastModified();
		if (dif / 1000 > cachedSeconds) {
			this.remove(f);
			this.remove(fHash);
			this._Result = CacheLoadResult.OVERTIME;
			return false;
		}
		String cnt = UFile.readFileText(f.getAbsolutePath());
		String hash = UFile.readFileText(fHash.getAbsolutePath());
		String[] hash1 = hash.split(",");
		if (hash1.length == 2 && (cnt.hashCode() + "").equals(hash1[0])
				&& (this._LastModifyed + "").equals(hash1[1])) {
			this._Result = CacheLoadResult.OK;
			this._Content = cnt;
			return true;
		} else {
			this.remove(f);
			this.remove(fHash);
			this._Result = CacheLoadResult.NO_VALID;
			return false;
		}
	}

	/**
	 * 写内容
	 * 
	 * @param content
	 * @return
	 */
	synchronized public boolean writeCache(String content) {
		File f = new File(this._Path + "/" + this._Name);
		File fHash = new File(this._Path + "/" + this._Name + ".hash.txt");
		MStr sb = new MStr();
		sb.appendLine(content.trim());
		sb.append("<!-- Cached by EWA; TIME: ");
		sb.append(_Rv.getString("EWA.DATE.STR1"));
		sb.append("; HASH: ");
		sb.append(this._Rv.getParameterHashCode());
		sb.append(" -->");
		if (f.exists()) {
			this.remove(f);
			this.remove(fHash);
		}

		try {
			UFile.createNewTextFile(f.getAbsolutePath(), sb.toString());
			// 内容的hash值，用于验证文件的完整性
			int hash = sb.toString().hashCode();
			String hashText = hash + "," + this._LastModifyed;
			UFile.createNewTextFile(fHash.getAbsolutePath(), hashText + "");
			return true;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return false;
		}
	}

	synchronized void remove(File f) {
		f.delete();
	}

	/**
	 * @return the _Path
	 */
	public String getPath() {
		return _Path;
	}

	/**
	 * @return the _Name
	 */
	public String getName() {
		return _Name;
	}

	/**
	 * 获取缓存内容
	 * 
	 * @return the _Content
	 */
	public String getCachedContent() {
		return _Content;
	}

	/**
	 * @return the _Result
	 */
	public CacheLoadResult getResult() {
		return _Result;
	}
}
