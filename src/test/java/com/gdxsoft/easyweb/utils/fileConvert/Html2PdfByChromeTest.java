package com.gdxsoft.easyweb.utils.fileConvert;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.io.TempDir;

import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.msnet.MTableStr;

/**
 * Html2PdfByChrome 集成测试，需要本机安装 Chrome 或 Edge
 */
@TestMethodOrder(OrderAnnotation.class)
class Html2PdfByChromeTest {

	@TempDir
	static Path tempDir;

	@BeforeAll
	static void initUPath() throws Exception {
		Field f = UPath.class.getDeclaredField("INIT_PARAS");
		f.setAccessible(true);
		if (f.get(null) == null) {
			f.set(null, new MTableStr());
		}
	}

	private static boolean browserAvailable() {
		String os = System.getProperty("os.name", "").toUpperCase();
		if (os.contains("MAC")) {
			return new File("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome").exists()
					|| new File("/Applications/Microsoft Edge.app/Contents/MacOS/Microsoft Edge").exists();
		} else if (os.contains("WIN")) {
			String home = System.getProperty("user.home");
			return new File(home + "\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe").exists()
					|| new File("C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe").exists()
					|| new File("C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe").exists();
		}
		return false;
	}

	private static File createHtml(String name, String content) throws IOException {
		File html = tempDir.resolve(name).toFile();
		Files.writeString(html.toPath(), content);
		return html;
	}

	@Test
	@Order(1)
	void testConvertHtmlToPdf() throws IOException {
		assumeTrue(browserAvailable(), "需要 Chrome 或 Edge");

		File html = createHtml("basic.html",
				"<html><head><meta charset=\"UTF-8\"></head><body>"
						+ "<h1>Hello EWA</h1>"
						+ "<p>中文测试内容</p>"
						+ "</body></html>");
		File pdf = tempDir.resolve("basic.pdf").toFile();

		Html2PdfByChrome converter = new Html2PdfByChrome();
		converter.convert2PDF(html, pdf);

		assertTrue(pdf.exists(), "PDF 文件应生成");
		assertTrue(pdf.length() > 100, "PDF 应有实质内容, size=" + pdf.length());

		byte[] header = new byte[4];
		Files.newInputStream(pdf.toPath()).read(header);
		assertArrayEquals("%PDF".getBytes(), header, "应以 %%PDF 开头");
	}

	@Test
	@Order(2)
	void testConvertWithCss() throws IOException {
		assumeTrue(browserAvailable(), "需要 Chrome 或 Edge");

		File html = createHtml("styled.html",
				"<html><head><style>"
						+ "body{font-family:sans-serif;margin:40px}"
						+ "h1{color:#336699}"
						+ "table{border-collapse:collapse;width:100%}"
						+ "td,th{border:1px solid #999;padding:8px}"
						+ "</style></head><body>"
						+ "<h1>Report</h1>"
						+ "<table><tr><th>Name</th><th>Value</th></tr>"
						+ "<tr><td>A</td><td>100</td></tr></table>"
						+ "</body></html>");
		File pdf = tempDir.resolve("styled.pdf").toFile();

		Html2PdfByChrome converter = new Html2PdfByChrome();
		converter.convert2PDF(html, pdf);

		assertTrue(pdf.exists());
		assertTrue(pdf.length() > 100);
	}

	@Test
	@Order(3)
	void testConvertNoHeaderFooter() throws IOException {
		assumeTrue(browserAvailable(), "需要 Chrome 或 Edge");

		File html = createHtml("noheader.html",
				"<html><body><p>No header footer</p></body></html>");
		File pdf = tempDir.resolve("noheader.pdf").toFile();

		Html2PdfByChrome converter = new Html2PdfByChrome();
		converter.setNoHeaderAndFooter(true);
		converter.convert2PDF(html, pdf);

		assertTrue(pdf.exists());
		assertTrue(pdf.length() > 100);
	}

	@Test
	@Order(4)
	void testConvertDataUrl() throws IOException {
		assumeTrue(browserAvailable(), "需要 Chrome 或 Edge");

		File pdf = tempDir.resolve("dataurl.pdf").toFile();

		Html2PdfByChrome converter = new Html2PdfByChrome();
		converter.convertUrl2PDF("data:text/html,<h1>Data URI Test</h1>", pdf);

		assertTrue(pdf.exists());
		assertTrue(pdf.length() > 100);
	}

	@Test
	@Order(5)
	void testConvertWithEdge() throws IOException {
		File edgeFile = new File("/Applications/Microsoft Edge.app/Contents/MacOS/Microsoft Edge");
		assumeTrue(edgeFile.exists(), "需要 Edge");

		File html = createHtml("edge.html", "<html><body><p>Edge test</p></body></html>");
		File pdf = tempDir.resolve("edge.pdf").toFile();

		Html2PdfByChrome converter = new Html2PdfByChrome();
		converter.setChromeCmd("\"" + edgeFile.getAbsolutePath() + "\"");
		converter.convert2PDF(html, pdf);

		assertTrue(pdf.exists());
		assertTrue(pdf.length() > 100);
	}

	@Test
	@Order(6)
	void testConvertStringPathOverload() throws IOException {
		assumeTrue(browserAvailable(), "需要 Chrome 或 Edge");

		File html = createHtml("strpath.html", "<html><body><p>String overload</p></body></html>");
		File pdf = tempDir.resolve("strpath.pdf").toFile();

		Html2PdfByChrome converter = new Html2PdfByChrome();
		converter.convert2PDF(html.getAbsolutePath(), pdf.getAbsolutePath());

		assertTrue(pdf.exists());
		assertTrue(pdf.length() > 100);
	}

	@Test
	@Order(7)
	void testConvertNonExistentHtml() throws IOException {
		assumeTrue(browserAvailable(), "需要 Chrome 或 Edge");

		File fakeHtml = tempDir.resolve("nonexistent.html").toFile();
		File pdf = tempDir.resolve("fail_input.pdf").toFile();

		Html2PdfByChrome converter = new Html2PdfByChrome();
		// Chrome 对不存在的文件仍会生成包含错误页的 PDF，验证不抛异常即可
		assertDoesNotThrow(() -> converter.convert2PDF(fakeHtml, pdf));
	}

	@Test
	@Order(8)
	void testConvertWithInvalidBrowserCmd() throws IOException {
		File html = createHtml("fail.html", "<html><body><p>fail</p></body></html>");
		File pdf = tempDir.resolve("fail_cmd.pdf").toFile();

		Html2PdfByChrome converter = new Html2PdfByChrome();
		converter.setChromeCmd("/nonexistent/browser");
		converter.convert2PDF(html, pdf);

		assertFalse(pdf.exists(), "无效浏览器路径时不应生成 PDF");
	}
}
