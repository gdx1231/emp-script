package com.gdxsoft.easyweb.app;

import java.io.File;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gdxsoft.easyweb.cache.CachedValue;
import com.gdxsoft.easyweb.cache.CachedValueManager;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UFileCheck;

public class AppResources {

	private static Object RESOURCES;
	private String _AppName;
	private String _jsonNamePath;
	private HashMap<String, String> params;

	public AppResources(String jsonNamePath) {
		_jsonNamePath = jsonNamePath;
	}

	@SuppressWarnings("unchecked")
	public String getResources(RequestValue rv) throws Exception {

		String ids = rv.getString("ids");
		String keys = rv.getString("keys");
		if (ids == null || keys == null) {
			return "{rst:'parameter error'}";
		}
		String[] ids1 = ids.split(",");
		String[] keys1 = (keys + " ").split(",");
		if (ids1.length != keys1.length) {
			return "{rst:'ids != keys'}";
		}
		String app = rv.getString("app");
		if (app == null || app.trim().length() == 0) {
			return "{rst:'app need'}";
		}

		this._AppName = app;
		this.params = new HashMap<String, String>();
		for (int i = 0; i < ids1.length; i++) {
			params.put(ids1[i], keys1[i].trim());
		}

		Object oJson = RESOURCES;
		JSONObject filesJson;
		// String path = UPath.getRealContextPath() + "/app/resources.json";
		boolean isJsonChd = UFileCheck.fileChanged(_jsonNamePath);
		if (oJson == null || isJsonChd) {
			String jsonFilesTxt = UFile.readFileText(_jsonNamePath);
			filesJson = new JSONObject(jsonFilesTxt);
			RESOURCES = filesJson;

		} else {
			filesJson = (JSONObject) oJson;
		}
		JSONObject arr = new JSONObject();

		int life = 360;// one hour
		HashMap<String, HashMap<String, String>> map;

		String cacheName = "app_resources_cahce" + this._AppName;

		Object cachekey = CachedValueManager.getValue(cacheName);
		if (cachekey == null || isJsonChd) {
			map = this.createMap(filesJson);
			CachedValueManager.addValue(cacheName, map, life);
		} else {
			CachedValue cv = (CachedValue) cachekey;
			map = (HashMap<String, HashMap<String, String>>) cv.getValue();
		}
		for (String id : map.keySet()) {
			JSONObject obj = checkFile(map, id);
			arr.put(obj.getString("r"), obj);
		}

		return arr.toString();
	}

	private void addToCahce(HashMap<String, HashMap<String, String>> map) {
		int life = 360;// one hour
		String cacheName = "app_resources_cahce" + this._AppName;
		CachedValueManager.addValue(cacheName, map, life);
	}

	private JSONObject checkFile(HashMap<String, HashMap<String, String>> map,
			String id) throws JSONException {
		HashMap<String, String> mapFile = map.get(id);
		String pathfile = mapFile.get("f");
		File r = new File(pathfile);
		JSONObject obj = new JSONObject();
		obj.put("r", r.getName());
		if (r.exists()) {
			String key = this.getFileKey(r);
			String mapKey = mapFile.get("HASH");
			String cnt = "";
			String app = mapFile.get("APP");
			if (!key.equals(mapKey)) {
				mapFile = this.createResource(r, app);

				map.put(id, mapFile);
				addToCahce(map);
			}
			if (params.containsKey(r.getName())) {
				String paramKey = params.get(r.getName());
				if (!paramKey.equals(key)) {
					cnt = mapFile.get("CNT");
				}
			} else {
				cnt = mapFile.get("CNT");
			}
			obj.put("HASH", key);

			String tp = UFile.getFileExt(r.getName());
			obj.put("TYPE", tp);
			obj.put("CNT", cnt);
			obj.put("APP", app);
			obj.put("IDX", mapFile.get("idx"));
		} else { // 文件不存在了
			obj.put("HASH", "DELETE");
		}
		return obj;
	}

	public HashMap<String, HashMap<String, String>> createMap(
			JSONObject resourcesJson) throws JSONException {
		HashMap<String, HashMap<String, String>> map = new HashMap<String, HashMap<String, String>>();
		JSONArray ff = resourcesJson.getJSONArray("ALL");
		int idx = 0;
		for (int i = 0; i < ff.length(); i++) {
			try {
				File r = new File(ff.getString(i));
				if (r.exists()) {
					HashMap<String, String> obj = createResource(r, "ALL");
					obj.put("idx", idx + "");
					map.put(r.getName(), obj);
					idx++;
				}
			} catch (Exception err) {

			}
		}

		ff = resourcesJson.getJSONArray(this._AppName);
		for (int i = 0; i < ff.length(); i++) {
			try {
				File r = new File(ff.getString(i));
				if (r.exists()) {
					HashMap<String, String> obj = createResource(r,
							this._AppName);
					obj.put("idx", idx + "");
					map.put(r.getName(), obj);
					idx++;
				}
			} catch (Exception err) {

			}
		}
		return map;
	}

	public String getFileKey(File f) {
		String key1 = f.length() + "|" + f.lastModified() + "|" + f.getName();
		return key1.hashCode() + "";
	}

	public HashMap<String, String> createResource(File f, String appName) {
		String key = getFileKey(f);
		HashMap<String, String> obj1 = new HashMap<String, String>();
		obj1.put("HASH", key);
		// this.save(id, d.CNT, d.HASH, d.TYPE);
		try {
			String cnt = UFile.readFileText(f.getAbsolutePath());
			obj1.put("CNT", cnt);
		} catch (Exception err) {
			obj1.put("CNT", "[e]");
		}
		obj1.put("TYPE", UFile.getFileExt(f.getName()));
		obj1.put("f", f.getAbsolutePath());
		obj1.put("APP", appName);
		return obj1;
	}
}
