package com.gdxsoft.easyweb.test;

import java.util.List;

import org.json.JSONObject;

import com.gdxsoft.easyweb.conf.ConfScriptPath;
import com.gdxsoft.easyweb.conf.ConfScriptPaths;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.define.group.ModuleImport;
import com.gdxsoft.easyweb.utils.UPath;

public class TestTableImport extends TestBase {

	public static void main(String[] args) {
		try {
			testFileConvert();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testFileConvert() throws Exception {
		System.out.println(UPath.getGroupPath());

		TestTableImport t = new TestTableImport();
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
		
		t.test2();
	}
	private void test2() {
		String moduleCode = "com.gdxsoft.work.base";
		String moduleVersion = "2.0";
		String importDataConn = "visa";
		String replaceMetaDatabaseName = "`visa_main_data`";
		String replaceWorkDatabaseName = "`visa`";

		ModuleImport moduleImport = new ModuleImport(moduleCode, moduleVersion, importDataConn, replaceMetaDatabaseName,
				replaceWorkDatabaseName);
		String module = UPath.getGroupPath() + "/exports/" + moduleCode + "_" + moduleVersion + ".zip";
		JSONObject result = moduleImport.importModule(module);
		
		System.out.println(result);
	}
	private void test1() {
		String moduleCode = "com.gdxsoft.backAdmin.menu";
		String moduleVersion = "1.0";
		String importDataConn = "visa_main";
		String replaceMetaDatabaseName = "`visa_main_data`";
		String replaceWorkDatabaseName = "`vias`";

		ModuleImport moduleImport = new ModuleImport(moduleCode, moduleVersion, importDataConn, replaceMetaDatabaseName,
				replaceWorkDatabaseName);
		String module = UPath.getGroupPath() + "/exports/" + moduleCode + "_" + moduleVersion + ".zip";
		JSONObject result = moduleImport.importModule(module);
		
		System.out.println(result);
	}

}
