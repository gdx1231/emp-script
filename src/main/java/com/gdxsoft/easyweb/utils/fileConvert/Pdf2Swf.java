package com.gdxsoft.easyweb.utils.fileConvert;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.utils.UPath;

/**
 * @deprecated Flash/SWF is end-of-life (Adobe discontinued Flash Player on 2020-12-31).
 * Use PDF or HTML5 based solutions instead.
 */
@Deprecated
public class Pdf2Swf {
	private static Logger LOGGER = LoggerFactory.getLogger(Pdf2Swf.class);

	/**
	 * 转换Pdf到Swf
	 *
	 * @param src
	 * @param target
	 * @throws IOException
	 */
	public boolean cvt2Swf(String src, String target) throws IOException {
		return this.cvt2Swf(new File(src), new File(target));
	}

	/**
	 * 转换Pdf到Swf
	 * 
	 * @param src
	 * @param target
	 * @throws IOException
	 */
	public boolean cvt2Swf(File src, File target) throws IOException {
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
		LOGGER.info("Pdf2Swf: {}", line);
		String[] args = line.split(" ");
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
				LOGGER.warn("Pdf2Swf timed out ({}ms)", System.currentTimeMillis() - t0);
				return false;
			}
			reader.join(3000);
			int exitCode = process.exitValue();
			LOGGER.info("Pdf2Swf completed ({}ms, exit={}): {}",
					System.currentTimeMillis() - t0, exitCode, output.toString().trim());
			return exitCode == 0;
		} catch (IOException e) {
			LOGGER.error("Pdf2Swf failed ({}ms): {}", System.currentTimeMillis() - t0, e.getMessage());
			return false;
		} catch (InterruptedException e) {
			LOGGER.error("Pdf2Swf interrupted ({}ms)", System.currentTimeMillis() - t0);
			Thread.currentThread().interrupt();
			return false;
		}
	}
}
