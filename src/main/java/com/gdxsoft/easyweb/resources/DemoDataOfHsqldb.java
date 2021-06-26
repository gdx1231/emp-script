package com.gdxsoft.easyweb.resources;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.utils.UFile;

public class DemoDataOfHsqldb {
	private static Logger LOGGER = LoggerFactory.getLogger(DemoDataOfHsqldb.class);

	public static boolean extract(String targetPath) throws Exception {
		LOGGER.info("Extract the demo data to " + targetPath);
		String savedZip = targetPath + "/hsql.data.zip";

		File existsZip = new File(savedZip);
		if (existsZip.exists()) {
			LOGGER.info("Exists " + existsZip.getAbsolutePath());
			return false;
		}
		URL u1 = DemoDataOfHsqldb.class.getResource("/hsql.data.zip");
		if (u1 == null) {
			throw new Exception("The resource '/hsql.data.zip' not exists");
		}
		byte[] buf = IOUtils.toByteArray(u1);

		UFile.createBinaryFile(savedZip, buf, true);

		List<String> lst = UFile.unZipFile(savedZip, targetPath);
		lst.forEach(v -> {
			File f = new File(v);
			LOGGER.info(f.getAbsolutePath());
		});

		return true;

	}
}
