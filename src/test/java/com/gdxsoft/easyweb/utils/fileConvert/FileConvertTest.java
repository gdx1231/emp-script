package com.gdxsoft.easyweb.utils.fileConvert;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.msnet.MTableStr;

class FileConvertTest {

	@BeforeAll
	static void initUPath() throws Exception {
		// UPath.INIT_PARAS 未初始化时 Html2PdfByChrome 构造器会 NPE
		Field f = UPath.class.getDeclaredField("INIT_PARAS");
		f.setAccessible(true);
		if (f.get(null) == null) {
			f.set(null, new MTableStr());
		}
	}

	@BeforeEach
	void resetDetectedBrowser() throws Exception {
		// 每次测试前重置缓存，确保探测逻辑被实际执行
		Field f = Html2PdfByChrome.class.getDeclaredField("detectedBrowser");
		f.setAccessible(true);
		f.set(null, null);
	}

	// -------- Html2PdfByChrome: getter/setter --------

	@Test
	void testNoHeaderAndFooterDefault() {
		Html2PdfByChrome c = new Html2PdfByChrome();
		assertFalse(c.isNoHeaderAndFooter());
	}

	@Test
	void testSetNoHeaderAndFooter() {
		Html2PdfByChrome c = new Html2PdfByChrome();
		c.setNoHeaderAndFooter(true);
		assertTrue(c.isNoHeaderAndFooter());
	}

	@Test
	void testSetChromeCmdOverridesDetection() {
		Html2PdfByChrome c = new Html2PdfByChrome();
		c.setChromeCmd("/custom/browser");
		assertEquals("/custom/browser", c.getChromeCmd());
	}

	// -------- Html2PdfByChrome: browser detection --------

	@Test
	void testGetChromeCmdReturnsNonNull() {
		Html2PdfByChrome c = new Html2PdfByChrome();
		String cmd = c.getChromeCmd();
		assertNotNull(cmd);
		assertFalse(cmd.isEmpty());
	}

	@Test
	void testGetChromeCmdCachingWorks() {
		Html2PdfByChrome c1 = new Html2PdfByChrome();
		String first = c1.getChromeCmd();

		// 第二个实例应返回相同结果（缓存）
		Html2PdfByChrome c2 = new Html2PdfByChrome();
		String second = c2.getChromeCmd();

		assertEquals(first, second);
	}

	@Test
	void testGetChromeCmdCustomOverridesCache() {
		Html2PdfByChrome c1 = new Html2PdfByChrome();
		c1.getChromeCmd(); // 触发缓存

		Html2PdfByChrome c2 = new Html2PdfByChrome();
		c2.setChromeCmd("/my/chrome");
		assertEquals("/my/chrome", c2.getChromeCmd());
	}

	@Test
	void testDetectedBrowserContainsKnownName() {
		Html2PdfByChrome c = new Html2PdfByChrome();
		String cmd = c.getChromeCmd();
		String lower = cmd.toLowerCase();
		// 结果应包含 chrome 或 edge
		assertTrue(lower.contains("chrome") || lower.contains("edge"),
				"Expected chrome or edge in: " + cmd);
	}

	// -------- File2Pdf / File2Html / Html2Docx wrappers --------

	@Test
	void testFile2PdfInstantiation() {
		assertDoesNotThrow(() -> new File2Pdf());
	}

	@Test
	void testFile2HtmlInstantiation() {
		assertDoesNotThrow(() -> new File2Html());
	}

	@Test
	void testHtml2DocxInstantiation() {
		assertDoesNotThrow(() -> new Html2Docx());
	}
}
