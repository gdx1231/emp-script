package com.gdxsoft.easyweb.utils.fileConvert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import com.gdxsoft.easyweb.utils.UPath;

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
		List<String> args = this.buildBaseArgs();
		args.add("--print-to-pdf=" + pdfFile.toString());
		args.add("file://" + inputFile.getAbsolutePath());

		this.runCvt(args);
	}

	/**
	 * 构建 Chrome 基础命令参数，使用独立临时目录避免读取当前用户的 Chrome 默认配置
	 */
	private List<String> buildBaseArgs() {
		List<String> args = new ArrayList<>();
		args.add(stripQuotes(this.getChromeCmd()));
		args.add("--headless=new");
		args.add("--disable-gpu");
		args.add("--disable-extensions");
		if (this.noHeaderAndFooter) {
			args.add("--no-pdf-header-footer");
		}
		File dir = getOrCreateUserDataDir();
		if (dir != null) {
			args.add("--user-data-dir=" + dir.getAbsolutePath());
		}
		return args;
	}

	private static String stripQuotes(String s) {
		if (s.startsWith("\"") && s.endsWith("\"")) {
			return s.substring(1, s.length() - 1);
		}
		return s;
	}

	/**
	 * 获取或创建共享的临时 user-data-dir，同一 JVM 生命周期内只创建一次
	 */
	private static File getOrCreateUserDataDir() {
		if (userDataDir != null && userDataDir.exists()) {
			return userDataDir;
		}
		synchronized (Html2PdfByChrome.class) {
			if (userDataDir != null && userDataDir.exists()) {
				return userDataDir;
			}
			long t0 = System.currentTimeMillis();
			try {
				userDataDir = Files.createTempDirectory("ewa-chrome-").toFile();
				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
					deleteRecursive(userDataDir);
				}));
				long span = System.currentTimeMillis() - t0;
				LOGGER.info("Created shared user-data-dir ({}ms): {}", span, userDataDir.getAbsolutePath());
				return userDataDir;
			} catch (IOException e) {
				long span = System.currentTimeMillis() - t0;
				LOGGER.warn("Failed to create temp user-data-dir ({}ms)", span, e);
				return null;
			}
		}
	}

	private static void deleteRecursive(File file) {
		if (file == null || !file.exists()) {
			return;
		}
		File[] children = file.listFiles();
		if (children != null) {
			for (File child : children) {
				deleteRecursive(child);
			}
		}
		file.delete();
	}

	// 缓存探测到的浏览器路径，避免每次调用都重新查找
	private static volatile String detectedBrowser = null;

	// 复用的临时 user-data-dir，避免每次转换都初始化新 Chrome profile
	private static volatile File userDataDir = null;

	/**
	 * 获取 Chrome 或 Edge 的执行路径，优先使用 Chrome，不存在时回退到 Edge
	 *
	 * @return
	 */
	public String getChromeCmd() {
		if (this.chromeCmd != null) {
			return this.chromeCmd;
		}
		if (detectedBrowser != null) {
			return detectedBrowser;
		}
		synchronized (Html2PdfByChrome.class) {
			if (detectedBrowser != null) {
				return detectedBrowser;
			}
			detectedBrowser = detectBrowser();
			return detectedBrowser;
		}
	}

	private static String detectBrowser() {
		String os = System.getProperty("os.name");
		if (os != null) {
			os = os.toUpperCase();
		} else {
			os = "";
		}

		if (os.contains("MAC")) {
			return findExistingBrowser(
					"\"/Applications/Google Chrome.app/Contents/MacOS/Google Chrome\"",
					"\"/Applications/Microsoft Edge.app/Contents/MacOS/Microsoft Edge\"");
		} else if (os.contains("WIN")) {
			String userHome = System.getProperty("user.home");
			return findExistingBrowser(
					"\"" + userHome + "\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe\"",
					"\"C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe\"",
					"\"C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe\"");
		} else {
			return findExistingBrowser(
					"google-chrome-stable", "google-chrome",
					"microsoft-edge-stable", "microsoft-edge");
		}
	}

	private static String findExistingBrowser(String... candidates) {
		for (String candidate : candidates) {
			// 去掉引号后检查文件是否存在
			String path = candidate.replace("\"", "");
			File f = new File(path);
			if (f.exists()) {
				LOGGER.info("Found browser: {}", path);
				return candidate;
			}
		}
		LOGGER.warn("No Chromium-based browser found, falling back to: {}", candidates[0]);
		return candidates[0];
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
		List<String> args = this.buildBaseArgs();
		args.add("--print-to-pdf=" + pdfFile.toString());
		args.add(url);

		this.runCvt(args);
	}

	private boolean runCvt(List<String> args) {
		LOGGER.info("Chrome convert start: {}", String.join(" ", args));

		long t0 = System.currentTimeMillis();
		try {
			ProcessBuilder pb = new ProcessBuilder(args);
			pb.redirectErrorStream(true);
			Process process = pb.start();

			StringBuilder output = new StringBuilder();
			final boolean[] success = { false };
			final Object lock = new Object();

			// 读取输出，检测到完成标记后立即通知主线程
			Thread reader = new Thread(() -> {
				try {
					byte[] buf = new byte[1024];
					int len;
					while ((len = process.getInputStream().read(buf)) != -1) {
						String chunk = new String(buf, 0, len);
						synchronized (output) {
							output.append(chunk);
						}
						if (chunk.contains("written to file")) {
							synchronized (lock) {
								success[0] = true;
								lock.notifyAll();
							}
						}
					}
				} catch (IOException ignored) {
				}
			});
			reader.setDaemon(true);
			reader.start();

			// 等待完成信号或超时
			synchronized (lock) {
				if (!success[0]) {
					lock.wait(60000);
				}
			}

			// Chrome 生成 PDF 后不会自行退出，主动终止
			if (process.isAlive()) {
				process.destroyForcibly();
				process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
			}
			reader.join(3000);

			long span = System.currentTimeMillis() - t0;
			String result = output.toString();
			if (success[0]) {
				LOGGER.info("Chrome convert completed ({}ms): {}", span, result.trim());
				return true;
			} else {
				LOGGER.warn("Chrome convert timeout or failed ({}ms): {}", span, result.trim());
				return false;
			}
		} catch (IOException e) {
			long span = System.currentTimeMillis() - t0;
			LOGGER.error("Chrome conversion failed ({}ms)", span, e);
		} catch (InterruptedException e) {
			long span = System.currentTimeMillis() - t0;
			LOGGER.error("Chrome conversion interrupted ({}ms)", span, e);
			Thread.currentThread().interrupt();
		}

		return false;
	}
}
