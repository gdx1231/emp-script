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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用 Chrome 转换 html或url 到 pdf 文件<br>
 * chrome --headless --disable-gpu --print-to-pdf=xx.pdf https://a/test.html
 * 
 * @author admin
 *
 */
public class Html2PdfByChrome {
	private static Logger LOGGER = LoggerFactory.getLogger(Html2PdfByChrome.class);
	private boolean noHeaderAndFooter = false;

	/**
	 * 无页头和页脚
	 * 
	 * @return
	 */
	public boolean isNoHeaderAndFooter() {
		return noHeaderAndFooter;
	}

	/**
	 * 无页头和页脚
	 * 
	 * @param noHeaderAndFooter
	 */
	public void setNoHeaderAndFooter(boolean noHeaderAndFooter) {
		this.noHeaderAndFooter = noHeaderAndFooter;
	}

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
		String cmd = this.getChromeCmd();
		cmd += " --headless --disable-gpu " + (this.noHeaderAndFooter ? "--no-pdf-header-footer" : "")
				+ " --print-to-pdf=\"" + pdfFile.toString() + "\" \"file://" + inputFile.getAbsolutePath() + "\" ";

		this.runCvt(cmd);
	}

	/**
	 * 获取 chrome 的执行目录
	 * 
	 * @return
	 */
	public String getChromeCmd() {
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
			String userName = System.getProperty("user.name");
			cmd = "C:\\Users\\" + userName + "\\AppData\\Local\\Google\\Chrome\\Application\\" + cmd;
		} else {
			cmd = "chrome";
		}

		return cmd;
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
		String cmd = this.getChromeCmd();

		cmd += " --headless --disable-gpu " + (this.noHeaderAndFooter ? "--no-pdf-header-footer" : "")
				+ " --print-to-pdf=\"" + pdfFile.toString() + "\" \"" + url + "\" ";

		this.runCvt(cmd);
	}

	private boolean runCvt(String line) {
		LOGGER.info(line);

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
			LOGGER.info(s);
			return true;
		} catch (ExecuteException e) {
			LOGGER.error(e.getMessage());
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}

		return false;
	}
}
