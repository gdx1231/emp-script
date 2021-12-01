package com.gdxsoft.easyweb.test;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.gdxsoft.easyweb.conf.ConfScriptPath;
import com.gdxsoft.easyweb.conf.ConfScriptPaths;
import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.define.database.Table;
import com.gdxsoft.easyweb.define.group.Exchange;
import com.gdxsoft.easyweb.define.group.ModuleExport;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.UJSon;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

public class TestTableExport extends TestBase {

	public static void main(String[] args) {
		try {
			testFileConvert();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testFileConvert() throws Exception {
		List<ConfScriptPath> lst = ConfScriptPaths.getInstance().getLst();
		// 导出用conf
		ConfScriptPath conf = new ConfScriptPath();
		conf.setName("b2b");
		conf.setPath("jdbc:ewa");
		conf.setReadOnly(false);
		lst.add(0, conf);

		System.out.println(UPath.getGroupPath());
		TestTableExport t = new TestTableExport();
		t.initConnPools();
		/*
		 * TestTableExport t = new TestTableExport(); t.initConnPools(); String
		 * moduleCode = "com.gdxsoft.backAdmin.menu"; String moduleVersion = "1.0";
		 * String ewaConntectionString = "ewa"; String replaceMetaDatabaseName =
		 * "`oneworld_main_data`"; String replaceWorkDatabaseName = "`b2b`";
		 * t.init(moduleCode, moduleVersion, ewaConntectionString,
		 * replaceMetaDatabaseName, replaceWorkDatabaseName); JSONObject result =
		 * t.makePackage(0); System.out.println(result);
		 * 
		 * System.out.println(UXml.asXmlPretty(t.getPackageSource()));
		 */
		t.test1();

		t.test2();
	}

	private void test1() throws Exception {
		String moduleCode = "com.gdxsoft.backAdmin.menu";
		String moduleVersion = "1.0";
		String ewaConntectionString = "ewa";

		ModuleExport moduleExport = new ModuleExport(moduleCode, moduleVersion, ewaConntectionString);
		JSONObject result = moduleExport.exportModule();
		System.out.println(result);

	}

	private void test2() throws Exception {
		String moduleCode = "com.gdxsoft.work.base";
		String moduleVersion = "2.0";
		String ewaConntectionString = "ewa";

		ModuleExport moduleExport = new ModuleExport(moduleCode, moduleVersion, ewaConntectionString);
		JSONObject result = moduleExport.exportModule();
		System.out.println(result);

	}

	private String moduleCode; // The module's code e.g. com.gdxsoft.backAdmin.menu
	private String moduleVersion; // The module's version
	private String ewaConntectionString; // Get the module's data connection string
	private String replaceMetaDatabaseName; // DDL replace meta database name, e.g. `main_data_db`
	private String replaceWorkDatabaseName; // DDL replace work database name, e.g. `work_db`
	private Document packageSource;

	public void init(String moduleCode, String moduleVersion, String ewaConntionString, String replaceMetaDatabaseName,
			String replaceWorkDatabaseName) {
		this.moduleCode = moduleCode;
		this.moduleVersion = moduleVersion;
		this.ewaConntectionString = ewaConntionString;
		this.replaceMetaDatabaseName = replaceMetaDatabaseName;
		this.replaceWorkDatabaseName = replaceWorkDatabaseName;

		this.createResXmlDocument();

	}

	public JSONObject makePackage(int moduleSupId) throws Exception {
		RequestValue rv = new RequestValue();
		rv.addOrUpdateValue("moduleSupId", moduleSupId);
		rv.addOrUpdateValue("moduleCode", moduleCode);
		rv.addOrUpdateValue("moduleVersion", moduleVersion);

		String sql1 = "select * from ewa_mod where mod_code=@moduleCode and mod_sup_id=@moduleSupId";
		DTTable tbMod = DTTable.getJdbcTable(sql1, this.ewaConntectionString, rv);
		if (tbMod.getCount() == 0) {
			return UJSon.rstFalse("No data of the class: " + moduleCode);
		}

		String sql2 = "select * from ewa_mod_ver where mod_code=@moduleCode and mod_ver=@moduleVersion and mod_ver_sup_id=@moduleSupId";
		DTTable tbModVer = DTTable.getJdbcTable(sql2, this.ewaConntectionString, rv);
		if (tbMod.getCount() == 0) {
			return UJSon.rstFalse("No data of the class: " + moduleCode + " version: " + this.moduleVersion);
		}

		long modVerId = tbModVer.getCell(0, "mod_ver_id").toLong();
		rv.addOrUpdateValue("modVerId", modVerId);
		String sql3 = "select * from ewa_mod_ddl where mod_ver_id=@modVerId order by emd_id";
		DTTable tbModDdl = DTTable.getJdbcTable(sql3, this.ewaConntectionString, rv);

		JSONObject result = UJSon.rstTrue(null);

		JSONArray arr = new JSONArray();
		result.put("tables", arr);
		for (int i = 0; i < tbModDdl.getCount(); i++) {
			DTRow r = tbModDdl.getRow(i);

			this.makeTableRecord(r);

			Table table = this.createTableData(r);
			JSONObject rJson = new JSONObject();
			rJson.put("name", table.getName());
			arr.put(r);
		}

		String sql4 = "select * from ewa_mod_cfgs where  mod_ver_id=@modVerId order by xmlname,itemname";
		DTTable tbModCfg = DTTable.getJdbcTable(sql4, this.ewaConntectionString, rv);

		for (int i = 0; i < tbModCfg.getCount(); i++) {
			DTRow r = tbModCfg.getRow(i);
			this.makeEasyWebTemplate(r);
		}

		String pkgZipPath = this.createPackageZip();

		result.put("package_file", pkgZipPath);
		return result;
	}

	private String createPackageZip() throws Exception {
		String id = moduleCode + "_" + moduleVersion;
		Exchange ex = new Exchange(id);
		String exportXmlName = ex.getExportPath() + "/" + Exchange.XML_DES;

		UXml.saveDocument(packageSource, exportXmlName);

		String pkgZipPath = ex.exportGroup();
		System.out.println(pkgZipPath);

		return pkgZipPath;
	}

	private void createResXmlDocument() {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><BaseInfo><Tables/><EasyWebTemplates/></BaseInfo>";
		packageSource = UXml.asDocument(xml);
	}

	private void makeTableRecord(DTRow row) throws Exception {
		String tableName = row.getCell("table_name").toString();
		// table view ...
		String emdType = row.getCell("emd_type").toString();
		// 数据库连接池
		String emdEwaConn = row.getCell("emd_ewa_conn").toString();
		String emdDatabaseType = row.getCell("emd_database_type").toString();
		String tableSchema = row.getCell("table_schema").toString();
		String emdExport = row.getCell("emd_export").toString();
		String emdExportWhere = row.getCell("emd_export_where").toString();
		// <Table DataSource="ow_main" DatabaseType="MSSQL" ExportWhere="1=1"
		// IsExport="true" Schema="dbo" TableName="ADM_DEPT" TableType="TABLE"/>
		Element node = packageSource.createElement("Table");
		node.setAttribute("DataSource", emdEwaConn);
		node.setAttribute("DatabaseType", emdDatabaseType);
		node.setAttribute("Schema", tableSchema);
		node.setAttribute("TableName", tableName);
		node.setAttribute("TableType", emdType);
		node.setAttribute("IsExport", emdExport);
		node.setAttribute("ExportWhere", emdExportWhere);

		System.out.println(UXml.asXml(node));

		this.packageSource.getElementsByTagName("Tables").item(0).appendChild(node);
	}

	private void makeEasyWebTemplate(DTRow row) throws DOMException, Exception {
		// <EasyWebTemplate Description="demo" FrameType="Complex"
		// ItemName="ACC_NO.F.NM" XmlName="/0test/guolei.xml"/>
		Element node = packageSource.createElement("EasyWebTemplate");
		node.setAttribute("XmlName", row.getCell("XmlName").toString());
		node.setAttribute("ItemName", row.getCell("ItemName").toString());
		node.setAttribute("Description", row.getCell("description").toString());

		// 默认输出目录
		node.setAttribute("ExportDefaultXmlname", row.getCell("emc_def_xmlname").toString());
		// 默认输出项名称
		node.setAttribute("ExportDefaultItmename", row.getCell("emc_def_itemname").toString());

		this.packageSource.getElementsByTagName("EasyWebTemplates").item(0).appendChild(node);
	}

	private Table createTableData(DTRow row) throws Exception {
		RequestValue rv = new RequestValue();

		long emdId = row.getCell("emd_id").toLong();
		rv.addOrUpdateValue("emdId", emdId);

		String tableName = row.getCell("table_name").toString();
		// table view ...
		String emdType = row.getCell("emd_type").toString();
		// 数据库连接池
		String emdEwaConn = row.getCell("emd_ewa_conn").toString();

		Table table = new Table(tableName, emdEwaConn);
		table.setTableType(emdType);

		table.setReplaceMetaDatabaseName(this.replaceMetaDatabaseName);
		table.setReplaceWorkDatabaseName(this.replaceWorkDatabaseName);

		table.init();

		// 表导出的XML
		String xml = table.toXml();

		rv.addOrUpdateValue("xml", xml);
		String sql = "update ewa_mod_ddl set emd_xml=@xml, emd_mdate=@sys_date";

		String ddl = table.getSqlTable();
		if (ddl != null && ddl.trim().length() > 0) { // 创建表/视图的DDL
			rv.addOrUpdateValue("ddl", ddl);
			sql += ", emd_ddl_sql = @ddl ";
		}

		sql += " where emd_id = @emdId";
		DataConnection.updateAndClose(sql, this.ewaConntectionString, rv);

		return table;
	}

	/**
	 * @return the packageSource
	 */
	public Document getPackageSource() {
		return packageSource;
	}

}
