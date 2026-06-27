package com.gdxsoft.easyweb.utils.fileConvert;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

class OpenOfficeInstanceTest {

	@Test
	void testServiceStartFlagDefaultValue() {
		assertFalse(OpenOfficeInstance.SERVICE_START);
	}

	@Test
	void testHtmlTemplateIsValid() throws Exception {
		Field htmlField = OpenOfficeInstance.class.getDeclaredField("HTML");
		htmlField.setAccessible(true);
		String html = (String) htmlField.get(null);
		assertNotNull(html);
		assertTrue(html.contains("<!DOCTYPE HTML>"));
		assertTrue(html.contains("charset=\"UTF-8\""));
	}

	@Test
	void testOfficeManagerInitiallyNull() throws Exception {
		Field omField = OpenOfficeInstance.class.getDeclaredField("officeManager");
		omField.setAccessible(true);
		Object om = omField.get(null);
		// 未启动服务时 officeManager 应为 null
		assertNull(om);
	}
}
