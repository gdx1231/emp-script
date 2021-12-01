package com.gdxsoft.easyweb.utils;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandleJsonBinaryBase64Impl implements IHandleJsonBinary {
	private static Logger LOGGER = LoggerFactory.getLogger(HandleJsonBinaryBase64Impl.class);

	@Override
	public byte[] getBinary(String fieldName, Object src) {
		if (src == null) {
			return null;
		}
		try {
			return UConvert.FromBase64String(src.toString());
		} catch (IOException e) {
			LOGGER.error("Convert to binary, {}", e.getMessage());
			return null;
		}
	}

}
