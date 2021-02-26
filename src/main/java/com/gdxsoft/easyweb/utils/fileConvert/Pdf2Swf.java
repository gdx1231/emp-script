package com.gdxsoft.easyweb.utils.fileConvert;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

import com.gdxsoft.easyweb.utils.UPath;

public class Pdf2Swf {
	/**
	 * 转换Pdf到Swf
	 * 
	 * @param src
	 * @param target
	 * @throws ExecuteException
	 * @throws IOException
	 */
	public boolean cvt2Swf(String src, String target) throws ExecuteException,
			IOException {
		return this.cvt2Swf(new File(src), new File(target));
	}

	/**
	 * 转换Pdf到Swf
	 * 
	 * @param src
	 * @param target
	 * @throws ExecuteException
	 * @throws IOException
	 */
	public boolean cvt2Swf(File src, File target) throws ExecuteException,
			IOException {
		// Usage: pdf2swf.exe [-options] file.pdf -o file.swf
		String path = UPath.getCVT_SWFTOOL_HOME();
		File f = new File(path);

		String exeName = "pdf2swf";
		// String ext = UFile.getFileExt(src.getName()).toLowerCase();

		// if (ext.equals("jpeg") || ext.equals("jpg")) {
		// exeName = "jpeg2swf";
		// } else if (ext.equals("gif")) {
		// exeName = "gif2swf";
		// }

		String line = "\"" + f.getAbsolutePath() + "\\" + exeName + "\" \""
				+ src.getAbsolutePath() + "\" -s flashversion=9 -z -o \""
				+ target.getAbsolutePath() + "\"";
		if (runCvt(line)) {
			if (target.exists()) {
				return true;
			}
		}
		return false;
	}

	private boolean runCvt(String line) {
		CommandLine commandLine = CommandLine.parse(line);
		DefaultExecutor executor = new DefaultExecutor();
		executor.setExitValue(0);
		ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
		executor.setWatchdog(watchdog);
		
		//PumpStreamHandler h = new PumpStreamHandler(System.out, System.err, System.in);
		 
		//executor.setStreamHandler(h);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
		executor.setStreamHandler(streamHandler);
		try {
			executor.execute(commandLine);
			String s=outputStream.toString();
			outputStream.close();
			System.out.println(s);
			
			return true;
		} catch (ExecuteException e) {
			System.out.println(this + ": " + e.getMessage());
		} catch (IOException e) {
			System.out.println(this + ": " + e.getMessage());
		}

	
		return false;
	}
}
