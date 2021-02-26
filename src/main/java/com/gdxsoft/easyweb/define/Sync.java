package com.gdxsoft.easyweb.define;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

public class Sync {

	private String _Source;
	private String _Target;
	private ArrayList<File> _Diffs;
	private HashMap<String, Boolean> _Filters;
	private HashMap<String, SyncCfg> _Cfgs;

	public Sync() throws ParserConfigurationException, SAXException,
			IOException {
		try {
			initCfgs();
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}
	}

	public void syncFiles(String id, String ids) {
		if (id == null || id.trim().length() == 0) {
			return;
		}
		SyncCfg cfg = this._Cfgs.get(id);
		if (cfg == null) {
			return;
		}
		String s = (new File(cfg.getSource())).getAbsolutePath();
		String t = (new File(cfg.getTarget())).getAbsolutePath();
		String[] names = ids.split(",");
		for (int i = 0; i < names.length; i++) {
			File f1 = new File(names[i]);
			if (!f1.exists()) {
				continue;
			}

			String target = f1.getAbsolutePath().replace(s, t);
			try {
				UFile.copyFile(f1.getAbsolutePath(), target);
			} catch (Exception ee) {
				System.out.println(ee.getMessage());
			}
		}
	}

	public HashMap<String, SyncCfg> getCfgs() {
		return this._Cfgs;
	}

	private void initCfgs() throws ParserConfigurationException, SAXException,
			IOException {
		_Cfgs = new HashMap<String, SyncCfg>();

		String name = UPath.getRealPath() + "/ewa_conf.xml";
		Document doc = UXml.retDocument(name);
		NodeList nl = doc.getElementsByTagName("sync");
		for (int i = 0; i < nl.getLength(); i++) {
			Element ele = (Element) nl.item(i);
			UObjectValue uo = new UObjectValue();
			SyncCfg cfg = new SyncCfg();
			uo.setObject(cfg);
			uo.setAllValue(ele);
			_Cfgs.put(cfg.getId(), cfg);
		}
	}

	public ArrayList<SyncFile> getDiffsById(String id) {
		if (id == null || id.trim().length() == 0) {
			return null;
		}
		SyncCfg cfg = this._Cfgs.get(id);
		if (cfg == null) {
			return null;
		}
		return this.getDiffs(cfg.getSource(), cfg.getTarget(), cfg.getFilter());
	}

	public ArrayList<SyncFile> getDiffs(String sourceDir, String targetDir,
			String filters) {
		File f1 = new File(sourceDir);
		this._Source = f1.getAbsolutePath();

		File f2 = new File(targetDir);

		this._Target = f2.getAbsolutePath();

		this._Diffs = new ArrayList<File>();

		_Filters = new HashMap<String, Boolean>();

		String[] exps = filters.split(",");
		for (int i = 0; i < exps.length; i++) {
			String exp = exps[i].trim().toUpperCase();
			if (!exp.startsWith(".")) {
				exp = "." + exp;
			}
			if (exp.length() > 0) {
				this._Filters.put(exp, true);
			}
		}
		_Filters.put(".BAK", true);

		this.getDir(this._Source);

		Object[] arrs = this._Diffs.toArray();
		Arrays.sort(arrs, new FileComprator());

		ArrayList<SyncFile> diffs = new ArrayList<SyncFile>();

		for (int i = 0; i < arrs.length; i++) {
			SyncFile f = new SyncFile((File) arrs[i]);
			diffs.add(f);
		}
		return diffs;
	}

	private void getDir(String path) {
		File f1 = new File(path);
		String name = f1.getName().toUpperCase();
		if (name.indexOf("_") == 0 || name.indexOf("TMP_") == 0
				|| name.indexOf("TEMP_") == 0 || name.indexOf("CVS") == 0) {
			return;
		}
		File[] ff = f1.listFiles();
		if (ff == null) {
			return;
		}
		for (int i = 0; i < ff.length; i++) {
			if (ff[i].isDirectory()) {
				continue;
			}

			if (!checkExts(ff[i])) {
				continue;
			}
			if (!this.checkSame(ff[i])) {
				this._Diffs.add(ff[i]);
			}
		}

		for (int i = 0; i < ff.length; i++) {
			if (ff[i].isDirectory()) {
				this.getDir(ff[i].getAbsolutePath());
			}
		}
	}

	private boolean checkExts(File source) {
		if (this._Filters.size() == 0 || this._Filters.containsKey(".*")) {
			return true;
		}
		String name = source.getName();
		int loc = name.indexOf(".");
		if (loc >= 0) {
			String ext = name.substring(loc).toUpperCase();
			if (this._Filters.containsKey(ext)) {
				return true;
			}
		}
		return false;
	}

	private boolean checkSame(File source) {
		if (source.getName().startsWith(".")) {
			return true;
		}
		String s = source.getAbsolutePath().replace(this._Source, this._Target);
		File target = new File(s);
		if (!target.exists()) {
			return false;
		}
		if (source.lastModified() == target.lastModified()) {
			return true;
		}
		if (source.length() == target.length()) {
			String name = UPath.getCachedPath() + "/sync/"
					+ source.getAbsolutePath().hashCode() + "_"
					+ source.length() + "_" + source.lastModified() + ".txt";
			name=name.replace("-", "G");
			File fCache = new File(name);
			String sHash = getFileCacheHash(source);
			String tHash = getFileCacheHash(target);
			if (fCache.exists()) {

				try {
					String txt = UFile.readFileText(fCache.getAbsolutePath());
					String[] hashs = txt.split(",");
					if (hashs.length == 3) {
						if (hashs[0].equals(sHash) && hashs[1].equals(tHash)) {
							if (hashs[2].equals("true")) {
								return true;
							}
							if (hashs[2].equals("false")) {
								return false;
							}
						}
					}
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}

			}
			String md5source = UFile.createMd5(source);
			String md5target = UFile.createMd5(target);
			if (md5source.equals(md5target)) {
				try {
					UFile
							.createNewTextFile(name, sHash + "," + tHash
									+ ",true");
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
				return true;
			} else {
				try {
					UFile.createNewTextFile(name, sHash + "," + tHash
							+ ",false");
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
				return false;
			}
		}
		return false;

	}

	private String getFileCacheHash(File f) {
		String txt = f.getAbsolutePath() + "_" + f.length() + "_"
				+ f.lastModified();
		return txt.hashCode() + "";
	}

	public static void main(String[] args) throws ParserConfigurationException,
			SAXException, IOException {
		Sync ss = new Sync();
		ArrayList<SyncFile> a = ss.getDiffs(
				"/Users/admin/Documents/java/tomcat6/webapps/el",
				"/Volumes/d$-1/java/tomcat-6.0.33/webapps/el", "*");
		// ArrayList<SyncFile> a = ss.getDiffsById("1");
		for (int i = 0; i < a.size(); i++) {
			System.out.println(a.get(i).getPath());
		}
	}
}

class FileComprator implements Comparator<Object> {
	public int compare(Object arg0, Object arg1) {
		File f0 = (File) arg0;
		File f1 = (File) arg1;
		if (f0.lastModified() > f1.lastModified()) {
			return -1;
		} else {
			return 1;
		}
	}
}