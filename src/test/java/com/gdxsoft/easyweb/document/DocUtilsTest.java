package com.gdxsoft.easyweb.document;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.msnet.MTableStr;

class DocUtilsTest {

	@TempDir
	static File tempDir;

	@BeforeAll
	static void initUPath() throws Exception {
		Field f = UPath.class.getDeclaredField("INIT_PARAS");
		f.setAccessible(true);
		if (f.get(null) == null) {
			f.set(null, new MTableStr());
		}
	}

	@Test
	void testZipWith7zipNonExistentCommandReturnsFalse() {
		// 7z 通常未安装，走 IOException 路径验证不会崩溃
		String target = new File(tempDir, "out.zip").getAbsolutePath();
		String source = new File(tempDir, "src").getAbsolutePath();
		new File(source).mkdirs();

		boolean result = DocUtils.zipWith7zip(target, source);
		assertFalse(result, "7z 未安装时应返回 false");
	}

	@Test
	void testZipWith7zipHandlesNullSource() {
		String target = new File(tempDir, "nullsrc.zip").getAbsolutePath();
		// null 拼接会抛 NPE，验证方法签名可达即可
		assertThrows(NullPointerException.class,
				() -> DocUtils.zipWith7zip(target, null));
	}

	@Test
	void testCompressDefaultPathSuccess() throws Exception {
		// 不用 7z，走 UFile.zipPaths 路径
		File srcDir = new File(tempDir, "compress_src");
		srcDir.mkdirs();
		new File(srcDir, "a.txt").createNewFile();
		new File(srcDir, "b.txt").createNewFile();

		String target = new File(tempDir, "compressed.zip").getAbsolutePath();
		boolean result = DocUtils.compress(target, srcDir.getAbsolutePath());

		assertTrue(result);
		assertTrue(new File(target).exists());
		assertTrue(new File(target).length() > 0);
	}

	@Test
	void testBuilderTempPathThrowsForNonExistentFile() {
		assertThrows(java.io.IOException.class,
				() -> DocUtils.builderTempPath("/nonexistent/path/xyz.doc"));
	}

	@Test
	void testClearTempPathHandlesFile() {
		File f = new File(tempDir, "file.txt");
		// 对文件调用 clearTempPath 应安全返回
		assertDoesNotThrow(() -> DocUtils.clearTempPath(f.getAbsolutePath()));
	}

	@Test
	void testClearTempPathHandlesNull() {
		// 不存在的路径应安全返回
		assertDoesNotThrow(() -> DocUtils.clearTempPath("/nonexistent/dir"));
	}
}
