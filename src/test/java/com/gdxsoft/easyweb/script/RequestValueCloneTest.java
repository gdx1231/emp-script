package com.gdxsoft.easyweb.script;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.utils.msnet.MTable;

public class RequestValueCloneTest {

	private RequestValue createPopulatedRv() throws Exception {
		RequestValue rv = new RequestValue();
		rv.addOrUpdateValue("key1", "val1");
		rv.addOrUpdateValue("key2", "val2");
		rv.setJsonBodyParameters(true);
		// set _ContextPath via reflection
		Field ctxField = RequestValue.class.getDeclaredField("_ContextPath");
		ctxField.setAccessible(true);
		ctxField.set(rv, "/app");

		// set _ParameterHashCode via reflection
		Field hashField = RequestValue.class.getDeclaredField("_ParameterHashCode");
		hashField.setAccessible(true);
		hashField.setInt(rv, 12345);

		// populate httpHeaders via reflection
		Map<String, String> headers = new HashMap<>();
		headers.put("X-Custom", "test-value");
		headers.put("Accept", "application/json");
		Field headersField = RequestValue.class.getDeclaredField("httpHeaders");
		headersField.setAccessible(true);
		headersField.set(rv, headers);

		// populate mapJson_ via reflection
		Map<String, org.json.JSONObject> mapJson = new HashMap<>();
		mapJson.put("data", new org.json.JSONObject("{\"a\":1}"));
		Field mapJsonField = RequestValue.class.getDeclaredField("mapJson_");
		mapJsonField.setAccessible(true);
		mapJsonField.set(rv, mapJson);

		// populate querys via reflection
		Map<String, Boolean> querys = new HashMap<>();
		querys.put("key1", true);
		querys.put("page", true);
		Field querysField = RequestValue.class.getDeclaredField("querys");
		querysField.setAccessible(true);
		querysField.set(rv, querys);

		return rv;
	}

	@Test
	void testCloneCopiesReqValues() throws Exception {
		RequestValue original = createPopulatedRv();
		RequestValue cloned = original.clone();

		assertEquals("val1", cloned.s("key1"));
		assertEquals("val2", cloned.s("key2"));
	}

	@Test
	void testCloneReqValuesAreIndependent() throws Exception {
		RequestValue original = createPopulatedRv();
		RequestValue cloned = original.clone();

		// modify clone, original should not change
		cloned.addOrUpdateValue("key1", "modified");
		cloned.addOrUpdateValue("key3", "new");

		assertEquals("val1", original.s("key1"));
		assertNull(original.s("key3"));
	}

	@Test
	void testCloneCopiesContextPath() throws Exception {
		RequestValue original = createPopulatedRv();
		RequestValue cloned = original.clone();

		assertEquals("/app", cloned.getContextPath());
	}

	@Test
	void testCloneCopiesParameterHashCode() throws Exception {
		RequestValue original = createPopulatedRv();
		RequestValue cloned = original.clone();

		assertEquals(12345, cloned.getParameterHashCode());
	}

	@Test
	void testCloneCopiesJsonBodyParameters() throws Exception {
		RequestValue original = createPopulatedRv();
		RequestValue cloned = original.clone();

		assertTrue(cloned.isJsonBodyParameters());
	}

	@Test
	void testCloneCopiesHttpHeaders() throws Exception {
		RequestValue original = createPopulatedRv();
		RequestValue cloned = original.clone();

		Map<String, String> clonedHeaders = cloned.getHttpHeaders();
		assertNotNull(clonedHeaders);
		assertEquals("test-value", clonedHeaders.get("X-Custom"));
		assertEquals("application/json", clonedHeaders.get("Accept"));
	}

	@Test
	void testCloneHttpHeadersAreIndependent() throws Exception {
		RequestValue original = createPopulatedRv();
		RequestValue cloned = original.clone();

		// modify cloned headers, original should not change
		cloned.getHttpHeaders().put("X-Custom", "changed");
		cloned.getHttpHeaders().put("X-New", "new-header");

		assertEquals("test-value", original.getHttpHeaders().get("X-Custom"));
		assertNull(original.getHttpHeaders().get("X-New"));
	}

	@SuppressWarnings("unchecked")
	@Test
	void testCloneCopiesMapJson() throws Exception {
		RequestValue original = createPopulatedRv();
		RequestValue cloned = original.clone();

		Field mapJsonField = RequestValue.class.getDeclaredField("mapJson_");
		mapJsonField.setAccessible(true);
		Map<String, org.json.JSONObject> clonedMap = (Map<String, org.json.JSONObject>) mapJsonField.get(cloned);

		assertNotNull(clonedMap);
		assertTrue(clonedMap.containsKey("data"));
		assertEquals(1, clonedMap.get("data").getInt("a"));
	}

	@SuppressWarnings("unchecked")
	@Test
	void testCloneMapJsonIsIndependent() throws Exception {
		RequestValue original = createPopulatedRv();
		RequestValue cloned = original.clone();

		Field mapJsonField = RequestValue.class.getDeclaredField("mapJson_");
		mapJsonField.setAccessible(true);
		Map<String, org.json.JSONObject> clonedMap = (Map<String, org.json.JSONObject>) mapJsonField.get(cloned);
		clonedMap.put("extra", new org.json.JSONObject());

		Map<String, org.json.JSONObject> originalMap = (Map<String, org.json.JSONObject>) mapJsonField.get(original);
		assertFalse(originalMap.containsKey("extra"));
	}

	@SuppressWarnings("unchecked")
	@Test
	void testCloneCopiesQuerys() throws Exception {
		RequestValue original = createPopulatedRv();
		RequestValue cloned = original.clone();

		Field querysField = RequestValue.class.getDeclaredField("querys");
		querysField.setAccessible(true);
		Map<String, Boolean> clonedQuerys = (Map<String, Boolean>) querysField.get(cloned);

		assertNotNull(clonedQuerys);
		assertTrue(clonedQuerys.containsKey("key1"));
		assertTrue(clonedQuerys.containsKey("page"));
	}

	@SuppressWarnings("unchecked")
	@Test
	void testCloneQuerysAreIndependent() throws Exception {
		RequestValue original = createPopulatedRv();
		RequestValue cloned = original.clone();

		Field querysField = RequestValue.class.getDeclaredField("querys");
		querysField.setAccessible(true);
		Map<String, Boolean> clonedQuerys = (Map<String, Boolean>) querysField.get(cloned);
		clonedQuerys.put("extra", true);
		clonedQuerys.remove("key1");

		Map<String, Boolean> originalQuerys = (Map<String, Boolean>) querysField.get(original);
		assertFalse(originalQuerys.containsKey("extra"));
		assertTrue(originalQuerys.containsKey("key1"));
	}

	@Test
	void testCloneWithNullMaps() {
		// clone should not NPE when optional maps are null
		RequestValue rv = new RequestValue();
		RequestValue cloned = rv.clone();
		assertNotNull(cloned);
		assertNull(cloned.getHttpHeaders());
	}
}
