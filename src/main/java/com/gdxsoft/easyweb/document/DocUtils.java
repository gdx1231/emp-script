package com.gdxsoft.easyweb.document;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.Utils;

public class DocUtils {

	public static boolean compress(String exportFilePathAndName, String sourcePath) {
		String os = System.getProperty("os.name");
		if (os != null && os.toUpperCase().indexOf("WINDOWS") >= 0) {
			// windows下用原生的程序压缩docx或odt文件会出现word文件不认的情况，原因未知
			return zipWith7zip(exportFilePathAndName, sourcePath);
		}
		// 非windows系统
		try {
			UFile.zipPaths(sourcePath, exportFilePathAndName);
			return true;
		} catch (Exception err) {
			System.out.println("DocUtils.compress: " + err.getMessage());
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
		String cmd = "7z a -r -tzip " + exportFilePathAndName + " " + sourcePath
				+ (sourcePath.endsWith(java.io.File.separatorChar + "") ? "" : java.io.File.separatorChar) + "*.*";
		System.out.println(cmd);

		CommandLine commandLine = CommandLine.parse(cmd);
		DefaultExecutor executor = new DefaultExecutor();
		executor.setExitValue(0);
		ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
		executor.setWatchdog(watchdog);

		// PumpStreamHandler h = new PumpStreamHandler(System.out, System.err,
		// System.in);

		// executor.setStreamHandler(h);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
		executor.setStreamHandler(streamHandler);
		try {
			executor.execute(commandLine);
			String s = outputStream.toString();
			outputStream.close();
			System.out.println(s);

			return true;
		} catch (ExecuteException e) {
			System.out.println("zipWith7zip: " + e.getMessage());
			return false;
		} catch (IOException e) {
			System.out.println("zipWith7zip: " + e.getMessage());
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
