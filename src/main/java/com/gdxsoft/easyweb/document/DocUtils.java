package com.gdxsoft.easyweb.document;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.Utils;

public class DocUtils {
	private static Logger LOGGER = LoggerFactory.getLogger(DocUtils.class);

	public static boolean compress(String exportFilePathAndName, String sourcePath) {
		// String os = System.getProperty("os.name");
		if ("7z".equals(UPath.getInitPara("documentCompress"))) {
			// if (os != null && os.toUpperCase().indexOf("delWINDOWS") >= 0) {
			// windows下用原生的程序压缩docx或odt文件会出现word文件不认的情况，原因未知
			return zipWith7zip(exportFilePathAndName, sourcePath);
		}
		// 非windows系统
		try {
			LOGGER.info("Compress {} to {}", sourcePath, exportFilePathAndName);
			UFile.zipPaths(sourcePath, exportFilePathAndName);
			return true;
		} catch (Exception err) {
			LOGGER.error("DocUtils.compress: " + err.getMessage());
			return false;
		}
	}

	/**
	 * 用7z压缩文件，windows下用原生的程序压缩docx或odt文件会出现word文件不认的情况，原因未知
	 * 
	 * @param exportFilePathAndName
	 * @param sourcePath
	 * @return
	 */
	public static boolean zipWith7zip(String exportFilePathAndName, String sourcePath) {
		String cmd = "7z a -r -tzip \"" + exportFilePathAndName + "\" " + sourcePath
				+ (sourcePath.endsWith(java.io.File.separatorChar + "") ? "" : java.io.File.separatorChar) + "*.*";
		LOGGER.info(cmd);

		String[] args = cmd.split(" ");
		ProcessBuilder pb = new ProcessBuilder(args);
		pb.redirectErrorStream(true);
		long t0 = System.currentTimeMillis();
		try {
			Process process = pb.start();

			StringBuilder output = new StringBuilder();
			Thread reader = new Thread(() -> {
				try {
					byte[] buf = new byte[1024];
					int len;
					while ((len = process.getInputStream().read(buf)) != -1) {
						output.append(new String(buf, 0, len));
					}
				} catch (IOException ignored) {
				}
			});
			reader.setDaemon(true);
			reader.start();

			boolean finished = process.waitFor(60, java.util.concurrent.TimeUnit.SECONDS);
			if (!finished) {
				process.destroyForcibly();
				LOGGER.warn("zipWith7zip timed out ({}ms)", System.currentTimeMillis() - t0);
				return false;
			}
			reader.join(3000);
			int exitCode = process.exitValue();
			LOGGER.info("zipWith7zip completed ({}ms, exit={}): {}",
					System.currentTimeMillis() - t0, exitCode, output.toString().trim());
			return exitCode == 0;
		} catch (IOException e) {
			LOGGER.error("zipWith7zip failed ({}ms): {}", System.currentTimeMillis() - t0, e.getMessage());
			return false;
		} catch (InterruptedException e) {
			LOGGER.error("zipWith7zip interrupted ({}ms)", System.currentTimeMillis() - t0);
			Thread.currentThread().interrupt();
			return false;
		}
	}

	public static String builderTempPath(String filePath) throws IOException {
		File from = new File(filePath);
		String name = from.getName();
		String root = UPath.getPATH_IMG_CACHE() + "/document/" + Utils.getGuid();
		String to = root + "/" + name;
		UFile.buildPaths(root);

		UFile.copyFile(filePath, to);
		File f = new File(to);
		if (f.exists()) {
			return f.getAbsolutePath();
		} else {
			return null;
		}
	}

	public static void clearTempPath(String filePath) {
		File f = new File(filePath);
		if (!f.exists()) {
			return;
		}
		if (f.isFile()) {
			return;
		}
		File[] ff = f.listFiles();
		for (int i = 0; i < ff.length; i++) {
			File f1 = ff[i];
			if (f1.isFile()) {
				f1.delete();
			}
		}
		for (int i = 0; i < ff.length; i++) {
			File f1 = ff[i];
			if (f1.isDirectory()) {
				clearTempPath(f1.getAbsolutePath());
			}
		}
		f.delete();
	}
}
