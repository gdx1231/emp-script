package com.gdxsoft.easyweb.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.gdxsoft.easyweb.define.Dir;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class UFile {

	/**
	 * 根据二进制流，获取文件扩展名
	 * 
	 * @param buf
	 * @return
	 */
	public static String getExtFromFileBytes(byte[] buf) {
		if (buf.length < 120) {
			return "bin";
		}
		byte[] bytes = new byte[120];
		System.arraycopy(buf, 0, bytes, 0, bytes.length);
		String s = new String(bytes).toUpperCase();
		if (s.substring(0, 3).equalsIgnoreCase("CWS")) {
			return "swf";
		} else if (s.toUpperCase().indexOf("JFIF") > 0 || s.toUpperCase().indexOf("XIF") > 0) {
			return "jpg";
		} else if (s.indexOf("PDF") > 0) {
			return "pdf";
		} else if (s.indexOf("RAR") == 0) {
			return "rar";
		} else if (s.indexOf("PK") == 0) {
			return "zip";
		} else if (s.indexOf("NG") == 1 || s.indexOf("PNG") >= 0) {
			return "png";
		} else if (s.indexOf("GIF") == 0) {
			return "gif";
		} else if (s.indexOf("BM") == 0) {
			return "bmp";
		}  else if (s.indexOf("{\\rtf1") == 0) {
			return "rtf";
		} else {
			// D0-CF-11-E0-A1-B1-1A-E1 doc,ppt,xls...
			byte[] bytesDoc = new byte[8];
			System.arraycopy(buf, 0, bytesDoc, 0, bytesDoc.length);
			String hex = Utils.byte2hex(bytesDoc).toUpperCase();
			if (hex.indexOf("FFD8FF") == 0) {
				return "jpg";
			} else if (hex.equals("D0CF11E0A1B11AE1")) {
				return "doc";
			} else if (hex.indexOf("49492A00") == 0) {
				return "tif";
			}
		}
		return "bin";
	}

	/**
	 * 删除文件
	 * 
	 * @param name
	 * @return
	 */
	public static boolean delete(String name) {
		if (name == null || name.trim().length() == 0) {
			return false;
		}
		File f = new File(name);
		if (!f.exists()) {
			return false;
		}
		try {
			return f.delete();
		} catch (Exception err) {
			System.err.println(err.getMessage());
			return false;
		}
	}

	/**
	 * 获取文件扩展名
	 * 
	 * @param name
	 * @return
	 */
	public static String getFileExt(String name) {
		int m = name.lastIndexOf(".");
		if (m > 0) {
			if (name.endsWith(".")) {
				return "";
			}
			return name.substring(m + 1);
		} else {
			return "";
		}
	}

	/**
	 * 获取文件名，没有扩展名
	 * 
	 * @param name
	 * @return
	 */
	public static String getFileNoExt(String name) {
		File f = new File(name);
		String name1 = f.getName();
		int m = name1.lastIndexOf(".");
		if (m > 0) {
			return name1.substring(0, m);
		} else {
			return name1;
		}
	}

	/**
	 * 替换文件名的扩展名
	 * 
	 * @param name
	 *            文件名包括目录
	 * @param newExt
	 *            扩展名
	 * @return
	 */
	public static String changeFileExt(String name, String newExt) {
		File f = new File(name);
		String nameNoExt = getFileNoExt(f.getName());
		String path = (f.getParent() == null ? "" : f.getParent() + File.separator) + nameNoExt + "." + newExt;
		f = new File(path);
		return f.getAbsolutePath();
	}

	public static Dir[] getFiles(String rootPath, String[] filter) {
		File f = new File(rootPath);
		if (!f.exists() || !f.isDirectory()) {
			return null;
		}

		FilenameFilter ff = UFileFilter.getInstance(filter);
		File[] fs = f.listFiles(ff);
		if (fs.length == 0) {
			return null;
		}
		Dir[] dirs = new Dir[fs.length];
		for (int i = 0; i < fs.length; i++) {
			dirs[i] = new Dir(fs[i]);
		}
		return dirs;
	}

	public static String readFileGzipBase64(String path) throws IOException {

		int BUFFER = 4096;
		BufferedInputStream origin = null;
		ByteArrayOutputStream bytesStream = new ByteArrayOutputStream();
		GZIPOutputStream gzipOut = new GZIPOutputStream(bytesStream);
		byte data[] = new byte[BUFFER];

		File f = new File(path);

		FileInputStream fi = new FileInputStream(f);
		origin = new BufferedInputStream(fi, BUFFER);

		int count;

		while ((count = origin.read(data, 0, BUFFER)) != -1) {
			gzipOut.write(data, 0, count);
		}
		origin.close();
		gzipOut.close();

		String s1 = UConvert.ToBase64String(bytesStream.toByteArray());

		bytesStream.close();
		return s1;
	}

	public static String readFileBase64(String path) throws Exception {
		byte[] buf = readFileBytes(path);
		String s1 = UConvert.ToBase64String(buf);
		return s1;
	}

	/**
	 * 读取二进制文件内容
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static byte[] readFileBytes(String path) throws Exception {

		File file = new File(path);
		if (file.exists()) { // 按照文件读取
			FileInputStream fi = null;
			try {
				fi = new FileInputStream(path);
				byte[] b = new byte[fi.available()];
				fi.read(b);
				return b;
			} catch (Exception e) {
				throw e;
			} finally {
				if (fi != null) {
					try {
						fi.close();
					} catch (IOException e) {
						System.err.println(e.toString());
					}
				}
			}
		} else { // 从jar包中读取
			InputStream f = null;
			try {
				f = UFile.class.getResourceAsStream(path);
				byte[] b = new byte[f.available()];
				f.read(b);
				return b;
			} catch (Exception err) {
				throw err;
			} finally {
				if (f != null) {
					try {
						f.close();
					} catch (IOException e) {
						System.err.println(e.toString());
					}
				}
			}
		}
	}

	/**
	 * 读取文本文件内容
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public static String readFileText(String filePath) throws Exception {
		byte[] buf = readFileBytes(filePath);
		return new String(buf, "UTF-8");
		/*
		 * BufferedReader br = null; try { FileInputStream fis = new
		 * FileInputStream( filePath); InputStreamReader isr = new
		 * InputStreamReader(fis);
		 * 
		 * br = new BufferedReader(isr);
		 * 
		 * String data = null; StringBuilder sb = new StringBuilder(); while
		 * ((data = br.readLine()) != null) { sb.append(data); sb.append("\n");
		 * } return sb.toString(); } catch (FileNotFoundException e) { throw e;
		 * } finally { if (br != null) { try { br.close(); } catch (IOException
		 * e) { System.err.println(e.toString()); } } }
		 */
	}

	/**
	 * 复制文件
	 * 
	 * @param fileFrom
	 *            源文件路径
	 * @param fileTo
	 *            目标文件路径
	 * @throws IOException
	 */
	public static void copyFile(String fileFrom, String fileTo) throws IOException {
		File ft = new File(fileTo);
		if (!ft.getParentFile().exists()) {
			ft.getParentFile().mkdirs();
		}
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(fileFrom)));
		byte[] date = new byte[in.available()];
		in.read(date);
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileTo)));
		out.write(date);
		in.close();
		out.close();
	}

	public static String zipFile(String filePath) throws IOException {
		int BUFFER = 4096;
		String zipFileName = filePath + ".zip";
		BufferedInputStream origin = null;
		FileOutputStream dest = new FileOutputStream(zipFileName);
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
		byte data[] = new byte[BUFFER];
		File f = new File(filePath);

		FileInputStream fi = new FileInputStream(f);
		origin = new BufferedInputStream(fi, BUFFER);
		ZipEntry entry = new ZipEntry(f.getName());
		out.putNextEntry(entry);
		int count;
		while ((count = origin.read(data, 0, BUFFER)) != -1) {
			out.write(data, 0, count);
		}
		origin.close();
		out.close();
		return zipFileName;
	}

	/**
	 * 压缩目录，只支持一级目录
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static String zipPath(String path) throws IOException {
		File f = new File(path);
		String zipFileName = f.getPath() + ".zip";
		if (f.isFile()) {
			return zipFile(path);
		}
		zipFiles(f.listFiles(), zipFileName);
		return zipFileName;
	}

	public static void zipFiles(String[] files, String zipFileName) throws IOException {
		File[] file = new File[files.length];
		for (int i = 0; i < files.length; i++) {
			file[i] = new File(files[i]);
		}
		zipFiles(file, zipFileName);
	}

	/**
	 * 压缩目录及所有文件
	 * 
	 * @param pathRoot
	 *            根目录
	 * @param zipFileName
	 * @throws IOException
	 */
	public static void zipPaths(String pathRoot, String zipFileName) throws IOException {
		FileOutputStream dest = new FileOutputStream(zipFileName);
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
		File root = new File(pathRoot);
		zipPathFiles(out, root, root.getAbsolutePath());
		out.close();
	}

	/**
	 * 递归压缩目录文件
	 * 
	 * @param out
	 * @param parent
	 * @param rootPath
	 * @throws IOException
	 */
	private static void zipPathFiles(ZipOutputStream out, File parent, String rootPath) throws IOException {
		int BUFFER = 1024 * 100;// 100k
		byte data[] = new byte[BUFFER];
		File[] files = parent.listFiles();
		for (int i = 0; i < files.length; i++) {
			File f1 = files[i];
			if (f1.isDirectory()) {

				zipPathFiles(out, f1, rootPath);
			} else {
				FileInputStream fi = new FileInputStream(f1);
				BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);
				String entryName = f1.getAbsolutePath().replace(rootPath + File.separator, "");
				ZipEntry entry = new ZipEntry(entryName);
				out.putNextEntry(entry);
				int count;
				while ((count = origin.read(data, 0, BUFFER)) != -1) {
					out.write(data, 0, count);
				}
				origin.close();
			}
		}
	}

	public static void zipFiles(File[] files, String zipFileName) throws IOException {
		int BUFFER = 4096;
		FileOutputStream dest = new FileOutputStream(zipFileName);
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
		byte data[] = new byte[BUFFER];
		for (int i = 0; i < files.length; i++) {
			File f1 = files[i];
			if (f1.isDirectory()) {
				continue;
			}
			FileInputStream fi = new FileInputStream(f1);
			BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);
			ZipEntry entry = new ZipEntry(f1.getName());
			out.putNextEntry(entry);
			int count;
			while ((count = origin.read(data, 0, BUFFER)) != -1) {
				out.write(data, 0, count);
			}
			origin.close();

		}
		out.close();
	}

	/**
	 * 解压ZIP格式文件
	 * 
	 * @param zipFilePath
	 *            zip文件路径
	 * @return 解压后的文件
	 * @throws IOException
	 */
	public static List<String> unZipFile(String zipFilePath) throws IOException {
		String unzipPath = "";
		for (int i = 0; i < 100; i++) {
			String unPath = zipFilePath + "_" + i + ".unzip";
			File file1 = new File(unPath);
			if (file1.exists()) {
				continue;
			} else {
				if (file1.mkdir()) {
					unzipPath = unPath;
					break;
				} else {
					continue;
				}
			}
		}

		if (unzipPath.length() == 0) {
			throw new IOException("不能建立解压缩目录！");
		}
		String path = unzipPath + File.separator;

		Enumeration<?> entries;
		ZipFile zipFile;
		zipFile = new ZipFile(zipFilePath);
		entries = zipFile.entries();
		List<String> fileList = new ArrayList<String>();
		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) entries.nextElement();

			if (entry.isDirectory()) {
				continue;
			}
			try {
				String filePath = path + entry.getName();
				File ftmp = new File(filePath);
				UFile.buildPaths(ftmp.getParentFile().getAbsolutePath());

				copyInputStream(zipFile.getInputStream(entry),
						new BufferedOutputStream(new FileOutputStream(filePath)));
				fileList.add(filePath);
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
		zipFile.close();
		return fileList;
	}

	private static final void copyInputStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int len;

		while ((len = in.read(buffer)) >= 0)
			out.write(buffer, 0, len);

		in.close();
		out.close();
	}

	/**
	 * 修改文件名
	 * 
	 * @param path
	 *            原始文件名（包含路径）
	 * @param newName
	 *            新文件名（不包含路径）
	 */
	public static void renameFile(String path, String newName) {
		String from = UPath.getScriptPath() + path.replace("|", "/");
		File fFrom = new File(from);
		String to = fFrom.getParent() + "/" + newName;
		File fTo = new File(to);
		fFrom.renameTo(fTo);
	}

	/**
	 * 根据文本内容生成文本文件
	 * 
	 * @param content
	 *            文本内容
	 * @param ext
	 *            扩展名
	 * @param path
	 *            路径
	 * @param isOverWrite
	 *            是否覆盖
	 * @return 生成的文件名（不包含路径）
	 * @throws Exception
	 */
	public static String createHashTextFile(String content, String ext, String path, boolean isOverWrite)
			throws Exception {
		String hash = "t_" + content.hashCode();
		path = path.trim() + "/";

		if (!buildPaths(path)) {
			throw new Exception("目录不能建立 (" + path + ")");
		}

		String fileName = hash + "." + ext.trim().toLowerCase();
		String filePath = path + fileName;
		File img = new File(filePath);
		if (isOverWrite || (!isOverWrite && !img.exists())) {
			createNewTextFile(filePath, content);
		}
		return fileName;
	}

	/**
	 * 生成新的文本文件
	 * 
	 * @param fileName
	 *            文件名
	 * @param content
	 *            内容
	 * @throws IOException
	 */
	public static void createNewTextFile(String fileName, String content) throws IOException {
		File file = new File(fileName);
		if (!file.getParentFile().exists()) {
			UFile.buildPaths(file.getParent());
		}
		OutputStreamWriter os = null;
		try {
			os = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
			os.write(content);
			os.flush();

		} catch (IOException e) {
			throw e;
		} finally {
			if (os != null)
				os.close();
		}
	}

	/**
	 * 根据二进制md5, 生成二进制文件，
	 * 
	 * @param bytes
	 * @param ext
	 * @param path
	 * @param isOverWrite
	 *            是否覆盖文件
	 * @return 生成的文件名（不包含路径）
	 * @throws Exception
	 */
	public static String createMd5File(byte[] bytes, String ext, String path, boolean isOverWrite) throws Exception {
		String md5 = Utils.md5(bytes);
		String s1 = createMd5File(bytes, md5, ext, path, isOverWrite);
		return s1;
	}

	/**
	 * 得到文件的md5标记
	 * 
	 * @param file
	 * @return
	 */
	public static String createMd5(File file) {
		try {
			byte[] bytes = readFileBytes(file.getPath());
			return Utils.md5(bytes);
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public static String createMd5File(byte[] bytes, String md5, String ext, String path, boolean isOverWrite)
			throws Exception {

		path = path.trim() + "/";

		if (!buildPaths(path)) {
			throw new Exception("目录不能建立 (" + path + ")");
		}

		String fileName = md5 + "." + ext.trim().toLowerCase();
		String filePath = path + fileName;
		createBinaryFile(filePath, bytes, isOverWrite);
		return fileName;
	}

	/**
	 * 生成二进制文件
	 * 
	 * @param path
	 *            路径
	 * @param bytes
	 *            二进制
	 * @param isOverWrite
	 *            是否覆盖
	 * @throws Exception
	 */
	public static void createBinaryFile(String path, byte[] bytes, boolean isOverWrite) throws Exception {
		File img = new File(path);
		UFile.buildPaths(img.getParent());
		if (isOverWrite || (!isOverWrite && !img.exists())) {
			FileOutputStream fs = new FileOutputStream(img);
			try {
				fs.write(bytes);
				fs.flush();
			} catch (IOException e) {
				throw e;
			} finally {
				fs.close();
				fs = null;
			}
		}
	}

	/**
	 * 将GZIP压缩的BASE64编码的字符串转换成文件
	 * 
	 * @param base64String
	 * @param ext
	 * @param path
	 * @param isOverWrite
	 * @return 根据BASE64哈希值生成文件名（不包含）
	 * @throws Exception
	 */
	public static String createUnGZipHashFile(String base64String, String ext, String path, boolean isOverWrite)
			throws Exception {
		path = path.trim() + "/";
		String hash = base64String.hashCode() + "";
		String fileName = hash + "." + ext.toLowerCase();
		String filePath = path.trim() + "/" + fileName;
		File f = new File(filePath);
		if (f.exists()) {
			return fileName;
		}

		byte[] bytes = UConvert.FromBase64String(base64String);

		ByteArrayInputStream in0 = new ByteArrayInputStream(bytes);
		GZIPInputStream inGZip = new GZIPInputStream(in0);

		int BUFFER = 4096;

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		byte[] data = new byte[BUFFER];
		int count;
		while ((count = inGZip.read(data, 0, BUFFER)) != -1) {
			out.write(data, 0, count);
		}

		inGZip.close();

		createBinaryFile(filePath, out.toByteArray(), isOverWrite);
		return fileName;
	}

	/**
	 * 根据名称分割文件名或目录名称
	 * 
	 * @param name
	 *            原始文件名
	 * @param len
	 *            分割长度
	 * @return
	 */
	public static String createSplitDirPath(String name, int len) {
		if (name == null || name.length() <= len) {
			return name;
		}
		if (len <= 0) {
			len = 2;
		}
		String name1 = name.trim().replace(" ", "_");
		name1 = name1.replace("?", "_");
		name1 = name1.replace("|", "_");
		name1 = name1.replace("*", "_");
		name1 = name1.replace("/", "_");
		name1 = name1.replace("\\", "_");
		name1 = name1.replace(".", "gdx");
		MStr s = new MStr();
		while (name1.length() > 0) {
			String tmp = name1.substring(0, len);
			name1 = name1.substring(len);
			s.a(tmp + "/");
			if (name1.length() <= len) {
				s.a(name1 + "/");
				break;
			}
		}
		return s.toString();
	}

	/**
	 * 建立路径
	 * 
	 * @param path
	 *            路径
	 * @return
	 */
	public static boolean buildPaths(String path) {
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir.exists();
	}
}
