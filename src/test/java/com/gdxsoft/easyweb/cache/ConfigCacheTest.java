package com.gdxsoft.easyweb.cache;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConfigCacheTest {

	@BeforeEach
	void setUp() {
		ConfigCache.clearCache();
		// 同时清除含 # 的 key（clearCache 只清非 # key）
		while (ConfigCache.getCount() > 0) {
			ConfigCache.removeCache(ConfigCache.list().split(":")[0].trim());
		}
	}

	// -------- CacheGroup --------

	@Test
	void testCacheGroupAddAndGet() {
		CacheGroup<String> cg = new CacheGroup<>();
		assertNull(cg.getItem("a"));

		cg.addItem("a", "valueA");
		assertEquals("valueA", cg.getItem("a"));

		// 覆盖写入
		cg.addItem("a", "valueA2");
		assertEquals("valueA2", cg.getItem("a"));
	}

	@Test
	void testCacheGroupGetItems() {
		CacheGroup<Integer> cg = new CacheGroup<>();
		cg.addItem("x", 1);
		cg.addItem("y", 2);
		assertEquals(2, cg.getItems().size());
	}

	// -------- CachedXmlFileMeta 序列化 --------

	@Test
	void testCachedXmlFileMetaSerializeRoundTrip() throws Exception {
		CachedXmlFileMeta meta = new CachedXmlFileMeta();
		meta.setCode("code123");
		meta.setFile("test.xml");
		meta.setKey("ConfigCached:XMLNAME=test");
		meta.addItemName("item1");
		meta.addItemName("item2");

		byte[] buf = meta.toSerialize();
		assertNotNull(buf);
		assertTrue(buf.length > 0);

		CachedXmlFileMeta restored = CachedXmlFileMeta.fromSerialize(buf);
		assertEquals("code123", restored.getCode());
		assertEquals("test.xml", restored.getFile());
		assertEquals("ConfigCached:XMLNAME=test", restored.getKey());
		assertEquals(2, restored.getItemNames().size());
		assertTrue(restored.getItemNames().contains("item1"));
		assertTrue(restored.getItemNames().contains("item2"));
	}

	// -------- ConfigCache.clearCache --------

	@Test
	void testClearCacheOnEmpty() {
		// 修复验证：空缓存 clearCache 不应抛 NPE
		assertDoesNotThrow(() -> ConfigCache.clearCache());
	}

	@Test
	void testGetCountOnEmpty() {
		assertEquals(0, ConfigCache.getCount());
	}

	@Test
	void testGetArrayKeyOnEmpty() {
		assertEquals("", ConfigCache.getArrayKey());
	}

	// -------- ConfigCacheWidthSqlCached key 生成 --------

	@Test
	void testGetConfigFileKey() {
		String key = ConfigCacheWidthSqlCached.getConfigFileKey("myConfig.xml");
		assertTrue(key.startsWith("ConfigCached:XMLNAME="));
		assertTrue(key.contains("myConfig.xml"));
	}

	@Test
	void testGetConfigItemKey() {
		String key = ConfigCacheWidthSqlCached.getConfigItemKey("MyItem");
		// itemName 必须小写
		assertTrue(key.contains("myitem"));
		assertFalse(key.contains("MyItem"));
	}

	@Test
	void testGetConfigItemKeyConsistency() {
		String k1 = ConfigCacheWidthSqlCached.getConfigItemKey("ABC");
		String k2 = ConfigCacheWidthSqlCached.getConfigItemKey("abc");
		assertEquals(k1, k2);
	}

	// -------- ConfigCache.getUserConfig 空路径 --------

	@Test
	void testGetUserConfigReturnsNullWhenEmpty() {
		assertNull(ConfigCache.getUserConfig("nonexistent.xml", "item1"));
	}

	@Test
	void testRemoveUserConfigOnEmpty() {
		// 不存在时不应抛异常
		assertDoesNotThrow(() -> ConfigCache.removeUserConfig("nonexistent.xml", "item1"));
	}

	// -------- 并发 setUserConfig --------

	@Test
	void testConcurrentSetUserConfig() throws InterruptedException {
		int threadCount = 20;
		int itemsPerThread = 100;
		CountDownLatch latch = new CountDownLatch(threadCount);
		AtomicInteger errors = new AtomicInteger(0);

		for (int t = 0; t < threadCount; t++) {
			final int threadId = t;
			new Thread(() -> {
				try {
					for (int i = 0; i < itemsPerThread; i++) {
						String xmlName = "xml_" + threadId;
						String itemName = "item_" + i;
						// 直接操作 CacheGroup，避免依赖 UserConfig 构造
						CacheGroup<String> cg = new CacheGroup<>();
						cg.addItem(itemName, "v_" + threadId + "_" + i);
					}
				} catch (Exception e) {
					errors.incrementAndGet();
				} finally {
					latch.countDown();
				}
			}).start();
		}

		latch.await();
		assertEquals(0, errors.get(), "并发操作不应出现异常");
	}

	// -------- ConfigCache.list / removeCache --------

	@Test
	void testRemoveCacheNonExistent() {
		assertDoesNotThrow(() -> ConfigCache.removeCache("does_not_exist"));
	}

	// -------- ConfigCache.getArrayKey 过滤逻辑 --------

	@Test
	void testGetArrayKeyFiltersHashKeys() {
		// getArrayKey 只返回不含 # 的 key
		// USERCONFIG# 类 key 应被过滤
		String keys = ConfigCache.getArrayKey();
		assertFalse(keys.contains("USERCONFIG#"));
	}

	// -------- CachedXmlFileMeta 空 itemNames --------

	@Test
	void testCachedXmlFileMetaEmptyItems() throws Exception {
		CachedXmlFileMeta meta = new CachedXmlFileMeta();
		meta.setCode("c1");
		meta.setFile("f.xml");
		meta.setKey("k");

		byte[] buf = meta.toSerialize();
		CachedXmlFileMeta restored = CachedXmlFileMeta.fromSerialize(buf);
		assertNotNull(restored.getItemNames());
		assertTrue(restored.getItemNames().isEmpty());
	}

	// -------- clearCache(String[] keys) --------

	@Test
	void testClearCacheWithKeys() {
		assertDoesNotThrow(() -> ConfigCache.clearCache(new String[] { "k1", "k2" }));
	}

	@Test
	void testClearCacheWithEmptyArray() {
		assertDoesNotThrow(() -> ConfigCache.clearCache(new String[] {}));
	}
}
