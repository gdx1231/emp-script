package com.gdxsoft.easyweb.define;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.utils.UFile;

/**
 * SyncRemote 文件扫描测试
 *
 * 使用临时目录构造文件树，不依赖 ewa_conf.xml 配置。
 *
 * 测试覆盖：
 * 1. 扩展名过滤（include/exclude）
 * 2. 特殊目录/文件排除（node_modules、_开头目录、application.*、ewa_conf.xml、超大文件）
 * 3. 扫描结果 JSON 字段（M=md5, H=hash）正确性
 * 4. 符号链接解析
 * 5. compareFiles 差异比对
 * 6. 多文件并行 MD5 扫描的完整性
 */
public class SyncRemoteTest {

	private Path testDir;
	private Path root;

	@BeforeEach
	public void setUp() throws IOException {
		testDir = Files.createTempDirectory("sync_remote_test_");
		root = testDir.resolve("root");
		Files.createDirectories(root);
	}

	@AfterEach
	public void tearDown() throws IOException {
		if (testDir != null) {
			FileUtils.deleteDirectory(testDir.toFile());
		}
	}

	private Path writeFile(String relativePath, String content) throws IOException {
		Path p = root.resolve(relativePath);
		Files.createDirectories(p.getParent());
		Files.write(p, content.getBytes(StandardCharsets.UTF_8));
		return p;
	}

	private SyncRemote scan(String filter) throws Exception {
		SyncRemote sr = new SyncRemote();
		sr.init(filter, root.toString());
		sr.getDir(null);
		return sr;
	}

	@Test
	public void testScanFilters() throws Exception {
		writeFile("a.xml", "aaa");
		writeFile("b.txt", "bbb");
		writeFile("c.bak", "ccc");
		writeFile(".DS_Store", "ddd");
		writeFile("sub/d.xml", "ddd");
		writeFile("node_modules/x/y.xml", "yyy");
		writeFile("_private/e.xml", "eee");
		writeFile("application.properties", "fff");
		writeFile("ewa_conf.xml", "ggg");

		SyncRemote sr = scan("xml");
		JSONObject json = sr.getJson();

		assertEquals(2, json.length(), "只应扫描到 2 个 xml 文件: " + json.keySet());
		assertTrue(json.has("/a.xml"));
		assertTrue(json.has("/sub/d.xml"), "子目录的 xml 应被扫描，key 为相对路径 linux 格式");
	}

	@Test
	public void testJsonFieldsAndMd5() throws Exception {
		Path f = writeFile("a.xml", "hello ewa");
		SyncRemote sr = scan("xml");
		JSONObject json = sr.getJson();

		JSONObject item = json.getJSONObject("/a.xml");
		assertTrue(item.has("M"), "应包含 MD5 字段 M");
		assertTrue(item.has("H"), "应包含缓存 hash 字段 H");

		// 与 UFile 结果一致
		assertEquals(UFile.createMd5(f.toFile()), item.getString("M"));

		// 与独立计算的 MD5 一致（不依赖被测代码使用的工具类）
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] digest = md.digest("hello ewa".getBytes(StandardCharsets.UTF_8));
		StringBuilder sb = new StringBuilder();
		for (byte b : digest) {
			sb.append(String.format("%02x", b));
		}
		assertEquals(sb.toString(), item.getString("M").toLowerCase(), "MD5 值应与独立计算结果一致");
	}

	@Test
	public void testStarFilter() throws Exception {
		writeFile("a.xml", "aaa");
		writeFile("b.txt", "bbb");
		writeFile("noext", "ccc");
		writeFile("c.bak", "ddd"); // 内置排除

		SyncRemote sr = scan(".*");
		JSONObject json = sr.getJson();

		assertTrue(json.has("/a.xml"));
		assertTrue(json.has("/b.txt"));
		assertTrue(json.has("/noext"), "无扩展名文件在 .* 下应被包含");
		assertFalse(json.has("/c.bak"), ".bak 为内置排除扩展名");
	}

	@Test
	public void testBigFileSkipped() throws Exception {
		writeFile("a.xml", "aaa");
		// 稀疏文件，无需真实写入 50M 内容
		Path big = root.resolve("big.xml");
		try (RandomAccessFile raf = new RandomAccessFile(big.toFile(), "rw")) {
			raf.setLength(51L * 1024 * 1024);
		}

		SyncRemote sr = scan("xml");
		JSONObject json = sr.getJson();

		assertTrue(json.has("/a.xml"));
		assertFalse(json.has("/big.xml"), "超过 50M 的文件应被跳过");
	}

	@Test
	public void testSymbolicLink() throws Exception {
		Path target = writeFile("d1/real.xml", "link target content");
		Path link = root.resolve("d2/link.xml");
		Files.createDirectories(link.getParent());
		try {
			Files.createSymbolicLink(link, target);
		} catch (UnsupportedOperationException | SecurityException e) {
			Assumptions.assumeTrue(false, "当前环境不支持创建符号链接");
		}

		SyncRemote sr = scan("xml");
		JSONObject json = sr.getJson();

		assertTrue(json.has("/d1/real.xml"));
		assertTrue(json.has("/d2/link.xml"), "符号链接文件应被扫描");
		assertEquals(json.getJSONObject("/d1/real.xml").getString("M"),
				json.getJSONObject("/d2/link.xml").getString("M"),
				"符号链接应解析到真实文件，MD5 与目标文件一致");
	}

	@Test
	public void testCompareFiles() throws Exception {
		writeFile("a.xml", "aaa");
		writeFile("b.xml", "bbb");

		// 第一次扫描（模拟本地）
		SyncRemote local = scan("xml");
		String localJson = local.getJson().toString();

		// 修改 b.xml，新增 c.xml
		writeFile("b.xml", "bbb changed");
		writeFile("c.xml", "ccc");

		// 第二次扫描（模拟远程，新实例无历史缓存，全量重新计算 MD5）
		SyncRemote remote = scan("xml");
		JSONObject diffs = remote.compareFiles(localJson);

		assertTrue(diffs.has("/b.xml"), "内容变化的文件应出现在差异中: " + diffs.keySet());
		assertFalse(diffs.has("/a.xml"), "未变化的文件不应出现在差异中");
		assertFalse(diffs.has("/c.xml"), "本地新增的文件不在本地 JSON 中，不参与比对");
	}

	@Test
	public void testParallelMd5ManyFiles() throws Exception {
		// 文件数超过 CPU 核数，确保走多线程并行路径
		int count = 200;
		for (int i = 0; i < count; i++) {
			writeFile("dir" + (i % 10) + "/f" + i + ".xml", "content " + i);
		}

		SyncRemote sr = scan("xml");
		JSONObject json = sr.getJson();

		assertEquals(count, json.length(), "并行扫描结果数量应完整");
		for (String key : json.keySet()) {
			JSONObject item = json.getJSONObject(key);
			assertEquals(32, item.getString("M").length(), key + " 的 MD5 应为 32 位");
			assertTrue(item.getString("H").length() > 0, key + " 的 H 不应为空");
		}

		// 抽查一个文件 MD5 内容正确
		assertEquals(UFile.createMd5(root.resolve("dir3/f103.xml").toFile()),
				json.getJSONObject("/dir3/f103.xml").getString("M"));
	}
}
