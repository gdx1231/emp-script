package com.gdxsoft.easyweb.utils.fileConvert;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import com.gdxsoft.easyweb.utils.UPath;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

public class Html2PdfByChrome {
	private String chromeCmd = null;

	public void setChromeCmd(String cmd) {
		chromeCmd = cmd;
	}

	public Html2PdfByChrome() {
		String cmd = UPath.getInitPara("pdf_chrome_exe");
		if (cmd != null && cmd.trim().length() > 0) {
			this.chromeCmd = cmd;
		}
	}

	/**
	 * 转换成PDF
	 * 
	 * @param inputFile
	 * @param pdfFile
	 */
	public void convert2PDF(File inputFile, File pdfFile) {
		String os = System.getProperty("os.name");
		if (os == null) {
			os = "??";
		} else {
			os = os.toUpperCase();
		}
		String cmd = "";
		if (this.chromeCmd != null) {
			cmd = this.chromeCmd;
		} else if (os.indexOf("MAC") >= 0) {
			cmd = "\"/Applications/Google Chrome.app/Contents/MacOS/Google Chrome\"";
		} else if (os.indexOf("WINDOWS") >= 0) {
			cmd = "chrome";
			cmd = "C:\\Users\\Administrator\\AppData\\Local\\Google\\Chrome\\Application\\" + cmd;
		} else {
			cmd = "chrome";
		}
		cmd += " --headless --disable-gpu --print-to-pdf=\"" + pdfFile.toString() + "\" \"file://"
				+ inputFile.getAbsolutePath() + "\" ";

		// System.out.println(cmd);
		this.runCvt(cmd);
	}

	/**
	 * 转换成PDF
	 * 
	 * @param inputFile
	 * @param pdfFile
	 */
	public void convert2PDF(String inputFile, String pdfFile) {
		this.convert2PDF(new File(inputFile), new File(pdfFile));
	}

	public void convertUrl2PDF(String url, String pdfFile) {
		this.convertUrl2PDF(url, new File(pdfFile));
	}

	public void convertUrl2PDF(String url, File pdfFile) {
		String os = System.getProperty("os.name");
		if (os == null) {
			os = "??";
		} else {
			os = os.toUpperCase();
		}
		String cmd = "";
		if (this.chromeCmd != null) {
			cmd = this.chromeCmd;
		} else if (os.indexOf("MAC") >= 0) {
			cmd = "\"/Applications/Google Chrome.app/Contents/MacOS/Google Chrome\"";
		} else if (os.indexOf("WINDOWS") >= 0) {
			cmd = "chrome";
			cmd = "C:\\Users\\Administrator\\AppData\\Local\\Google\\Chrome\\Application\\" + cmd;
		} else {
			cmd = "chrome";
		}
		cmd += " --headless --disable-gpu --print-to-pdf=\"" + pdfFile.toString() + "\" \"" + url + "\" ";

		// System.out.println(cmd);
		this.runCvt(cmd);
	}

	private boolean runCvt(String line) {
		System.out.println(line);
		CommandLine commandLine = CommandLine.parse(line);
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
			System.out.println(this + ": " + e.getMessage());
		} catch (IOException e) {
			System.out.println(this + ": " + e.getMessage());
		}

		return false;
	}
}
