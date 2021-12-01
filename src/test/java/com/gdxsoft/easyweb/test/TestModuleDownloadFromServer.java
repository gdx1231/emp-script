package com.gdxsoft.easyweb.test;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gdxsoft.easyweb.conf.ConfScriptPath;
import com.gdxsoft.easyweb.conf.ConfScriptPaths;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.define.group.ModulePublish;
import com.gdxsoft.easyweb.utils.UPath;

public class TestModuleDownloadFromServer extends TestBase {

	public static void main(String[] args) {
		try {
			testFileConvert();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testFileConvert() throws Exception {
		System.out.println(UPath.getGroupPath());

		TestModuleDownloadFromServer t = new TestModuleDownloadFromServer();
		t.initConnPools();

		String sql1 = "select 1 from dual";
		DTTable tb = DTTable.getJdbcTable(sql1, "main_data");

		if (!tb.isOk()) {
			return;
		}

		List<ConfScriptPath> lst = ConfScriptPaths.getInstance().getLst();
		// 导出用conf
		ConfScriptPath conf = new ConfScriptPath();
		conf.setName("visa_ewa");
		conf.setPath("jdbc:visa_ewa");
		lst.add(0, conf);

		t.test1();
		
	}
	 
	private void test1() {
		 String downloadData = "[{\"id\":\"120947753277718528\"},{\"id\":\"120947513661325312\"}]";
		 ModulePublish p = new ModulePublish();
		 try {
			 JSONObject result = p.downloadFromPublishServer(new JSONArray(downloadData));
			 System.out.println(result);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
