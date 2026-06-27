package com.gdxsoft.easyweb.utils.fileConvert;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.msnet.MTableStr;

class WrapperConvertTest {

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

	// -------- File2Pdf --------

	@Test
	void testFile2PdfStringOverloadDelegatesToFile() {
		String in = tempDir.resolve("input1.txt").toString();
		String out = tempDir.resolve("output1.pdf").toString();
		try {
			new File(in).createNewFile();
		} catch (IOException ignored) {
		}

		File2Pdf converter = new File2Pdf();
		// OpenOffice 不可用时抛异常，验证 String→File 委托正确
		assertThrows(Exception.class,
				() -> converter.convert2PDF(in, out));
	}

	@Test
	void testFile2PdfFileOverloadWithNonexistentInput() {
		File nonexistent = new File(tempDir.resolve("noexist.txt").toAbsolutePath().toString());
		File out = tempDir.resolve("out.pdf").toFile();

		File2Pdf converter = new File2Pdf();
		assertThrows(Exception.class,
				() -> converter.convert2PDF(nonexistent, out));
	}

	// -------- File2Html --------

	@Test
	void testFile2HtmlStringOverloadDelegatesToFile() {
		String in = tempDir.resolve("input2.txt").toString();
		String out = tempDir.resolve("output2.html").toString();
		try {
			new File(in).createNewFile();
		} catch (IOException ignored) {
		}

		File2Html converter = new File2Html();
		assertThrows(Exception.class,
				() -> converter.convert2Html(in, out));
	}

	@Test
	void testFile2HtmlFileOverloadWithNonexistentInput() {
		File nonexistent = new File(tempDir.resolve("noexist.txt").toAbsolutePath().toString());
		File out = tempDir.resolve("out.html").toFile();

		File2Html converter = new File2Html();
		assertThrows(Exception.class,
				() -> converter.convert2Html(nonexistent, out));
	}

	// -------- Html2Docx --------

	@Test
	void testHtml2DocxStringOverloadDelegatesToFile() {
		String in = tempDir.resolve("input3.html").toString();
		String out = tempDir.resolve("output3.docx").toString();
		try {
			Files.writeString(new File(in).toPath(), "<html><body>test</body></html>");
		} catch (IOException ignored) {
		}

		Html2Docx converter = new Html2Docx();
		assertThrows(Exception.class,
				() -> converter.convert2Docx(in, out));
	}

	@Test
	void testHtml2DocxFileOverloadWithNonexistentInput() {
		String in = tempDir.resolve("noexist.html").toAbsolutePath().toString();
		String out = tempDir.resolve("out.docx").toAbsolutePath().toString();

		Html2Docx converter = new Html2Docx();
		assertThrows(Exception.class,
				() -> converter.convert2Docx(in, out));
	}

	// -------- Cross-type consistency --------

	@Test
	void testAllWrappersInstantiateWithoutException() {
		assertDoesNotThrow(() -> new File2Pdf());
		assertDoesNotThrow(() -> new File2Html());
		assertDoesNotThrow(() -> new Html2Docx());
	}
}
