package com.gdxsoft.easyweb.resources;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 和emp-script-static一致，避免当emp-script-static项目不存在时，EwaStyleController错误
 * @author admin
 *
 */
public class Resources {
	/**
	 * 
	 */
	private static Logger LOGGER = LoggerFactory.getLogger(Resources.class);

	private static Map<String, Resource> CACHED = new ConcurrentHashMap<String, Resource>();

	private static synchronized Resource loadResource(String path) {
		String ext = FilenameUtils.getExtension(path);
		Resource r = new Resource();

		if (path.indexOf("..") >= 0) {
			r.setPath(path);
			r.setStatus(502);
			LOGGER.error("Invalid path '..', {}", r.toString());
			return r;
		}

		// not allow blank ext or directory
		if (ext.trim().length() == 0 || ext.trim().endsWith("/")) {
			r.setPath(path);
			r.setStatus(403);
			LOGGER.error("Blank ext or directory. {}", r.toString());
			return r;
		}
		if (ext.equalsIgnoreCase("exe") || ext.equalsIgnoreCase("bat") || ext.equalsIgnoreCase("cmd")
				|| ext.equalsIgnoreCase("sh") || ext.equalsIgnoreCase("dmg") || ext.equalsIgnoreCase("java")
				|| ext.equalsIgnoreCase("jsp") || ext.equalsIgnoreCase("class") || ext.equalsIgnoreCase("jar")
				|| ext.equalsIgnoreCase("properties")) {
			r.setPath(path);
			r.setStatus(500);
			LOGGER.error("Invalid ext. {}", r.toString());
			return r;
		}

		if (path.indexOf("ewa_conf") >= 0 || path.indexOf("appliaction.yml") >= 0) {
			r.setPath(path);
			r.setStatus(501);
			LOGGER.error("Invalid file. {}", r.toString());
			return r;
		}

		URL url = Resources.class.getResource(path);
		r.setPath(path);
		if (url == null) {
			r.setStatus(404);
			LOGGER.error(r.toString());
			return r;
		}
		boolean binary = false;
		if (ext.equalsIgnoreCase("js")) {
			r.setType("text/javascript");
		} else if (ext.equalsIgnoreCase("htm") || ext.equalsIgnoreCase("html")) {
			r.setType("text/html");
		} else if (ext.equalsIgnoreCase("txt") || ext.equalsIgnoreCase("csv")) {
			r.setType("text/plain");
		} else if (ext.equalsIgnoreCase("json")) {
			r.setType("text/json");
		} else if (ext.equalsIgnoreCase("css")) {
			r.setType("text/css");
		} else if (ext.equalsIgnoreCase("xml")) {
			r.setType("text/xml");
		} else if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("png")
				|| ext.equalsIgnoreCase("gif")) {
			r.setType("image/" + ext);
			binary = true;
		} else {
			r.setType("application/octet-stream");
			binary = true;
		}
		r.setBinary(binary);

		try {
			if (binary) {
				byte[] buf = IOUtils.toByteArray(url);
				r.setBuffer(buf);
			} else {
				String text = IOUtils.toString(url, StandardCharsets.UTF_8);
				r.setContent(text);
			}
			LOGGER.debug(r.toString());
			return r;
		} catch (IOException e) {
			r.setStatus(500);
			LOGGER.error(r.toString());
			return r;
		}
	}

	/**
	 * Get the EWA static files, js, css, images ...
	 * 
	 * @param resourcePath static path
	 * @return the resource
	 */
	public static Resource getResource(String resourcePath) {
		// from cached
		if (CACHED.containsKey(resourcePath)) {
			return CACHED.get(resourcePath);
		}
		String path = resourcePath;
		// for compatible
		if (path.indexOf("EWA_ALL.js") > 0 || path.indexOf("EWA_ALL.2.0.js") > 0) {
			path = "/EWA_STYLE/js/ewa.js";
		} else if (path.indexOf("EWA_ALL.min.2.0.js") > 0) {
			path = "/EWA_STYLE/js/ewa.min.js";
		} else if (path.indexOf("/fas.js") >= 0) {
			path = "/EWA_STYLE/js/fas.js";
		} else if (path.indexOf("/jquery/jquery-1.") > 0) {
			path = "/third-party/jquery/jquery-1.12.4.min.js";
		} else if (path.indexOf("/jquery/jquery-3.") > 0) {
			path = "/third-party/jquery/jquery-3.6.0.min.js";
		} else if (path.indexOf("/thrid-party/") == 0) {
			path = path.replace("/thrid-party/", "/third-party/");
		} else if (path.indexOf("/js_jquery/") >= 0 && path.indexOf("EWA_ALL") < 0) {
			path = path.replace("/js_jquery/", "/source/");
		}

		path = path.replace("//", "/").replace("//", "/").replace("//", "/").replace("//", "/");

		// load from resources
		Resource r = loadResource(path);
		CACHED.put(resourcePath, r);

		return r;
	}

}
