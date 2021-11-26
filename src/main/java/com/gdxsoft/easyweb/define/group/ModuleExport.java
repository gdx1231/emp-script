package com.gdxsoft.easyweb.define.group;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.define.database.Table;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UJSon;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.Utils;

/**
 * Export module from ewa_mod data
 * 
 * @author admin
 *
 */
public class ModuleExport extends ModuleBase {
	private static Logger LOGGER = LoggerFactory.getLogger(ModuleExport.class);

	String ewaConntectionString; // Get the module's data connection string
	private Document packageSource;

	public ModuleExport(String moduleCode, String moduleVersion, String ewaConntionString) {
		this.moduleCode = moduleCode;
		this.moduleVersion = moduleVersion;
		this.ewaConntectionString = ewaConntionString;

	}
	
	public byte[] getExportModuleFile(int moduleSupId) throws Exception {
		RequestValue rv = new RequestValue();
		rv.addOrUpdateValue("moduleSupId", moduleSupId);
		rv.addOrUpdateValue("moduleCode", moduleCode);
		rv.addOrUpdateValue("moduleVersion", moduleVersion);
		
		String sql2 = "select mod_ver_id from ewa_mod_ver where mod_code=@moduleCode and mod_ver=@moduleVersion and mod_ver_sup_id=@moduleSupId";
		DTTable tbModVer = DTTable.getJdbcTable(sql2, this.ewaConntectionString, rv);
		if (tbModVer.getCount() == 0) {
			return null;
		}

		long modVerId = tbModVer.getCell(0, "mod_ver_id").toLong();
		rv.addOrUpdateValue("modVerId", modVerId);
		
		String sqlExists = "select * from ewa_mod_package where mod_ver_id=@modVerId ";
		DTTable tbExistsPkg = DTTable.getJdbcTable(sqlExists, this.ewaConntectionString, rv);
		if (tbExistsPkg.getCount() == 0) {
			return null;
		}
		
		return (byte[]) tbExistsPkg.getCell(0, "pkg_file").getValue();
	}

	/**
	 * 导出模块
	 * 
	 * @param moduleSupId
	 * @return
	 * @throws Exception
	 */
	public JSONObject exportModule(int moduleSupId) throws Exception {
		this.createResXmlDocument();

		RequestValue rv = new RequestValue();
		rv.addOrUpdateValue("moduleSupId", moduleSupId);
		rv.addOrUpdateValue("moduleCode", moduleCode);
		rv.addOrUpdateValue("moduleVersion", moduleVersion);

		String sql1 = "select * from ewa_mod where mod_code=@moduleCode and mod_sup_id=@moduleSupId";
		DTTable tbMod = DTTable.getJdbcTable(sql1, this.ewaConntectionString, rv);
		if (tbMod.getCount() == 0) {
			return UJSon.rstFalse("No data of the class: " + moduleCode);
		}

		// 元数据库名称
		String replaceMetaDatabaseName = tbMod.getCell(0, "mod_meta_db_name").toString();
		super.setReplaceMetaDatabaseName(replaceMetaDatabaseName);

		// 工作数据库名称
		String replaceWorkDatabaseName = tbMod.getCell(0, "mod_work_db_name").toString();
		super.setReplaceWorkDatabaseName(replaceWorkDatabaseName);

		String sql2 = "select * from ewa_mod_ver where mod_code=@moduleCode and mod_ver=@moduleVersion and mod_ver_sup_id=@moduleSupId";
		DTTable tbModVer = DTTable.getJdbcTable(sql2, this.ewaConntectionString, rv);
		if (tbModVer.getCount() == 0) {
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
			// 创建的表的XML表达式
			this.makeTableRecord(r);
		}

		String sql4 = "select * from ewa_mod_cfgs where  mod_ver_id=@modVerId order by xmlname,itemname";
		DTTable tbModCfg = DTTable.getJdbcTable(sql4, this.ewaConntectionString, rv);

		for (int i = 0; i < tbModCfg.getCount(); i++) {
			DTRow r = tbModCfg.getRow(i);
			// 创建的配置项的XML表达式
			this.makeEasyWebTemplate(r);
		}

		String pkgZipPath = this.createModulePackageZip();
		result.put("package_file", pkgZipPath);

		byte[] buf = UFile.readFileBytes(pkgZipPath);

		int len = buf.length;
		rv.addOrUpdateValue("pkg_len", len);
		
		String md5 = Utils.md5(buf);

		String sqlExists = "select pkg_md5 from ewa_mod_package where mod_ver_id=@modVerId ";
		DTTable tbExistsPkg = DTTable.getJdbcTable(sqlExists, this.ewaConntectionString, rv);
		if (tbExistsPkg.getCount() > 0) {
			String existsMd5 = tbExistsPkg.getCell(0, 0).toString();
			if (md5.equalsIgnoreCase(existsMd5)) { // 包内容没有变化
				LOGGER.info("The module packaged file has no change, skip store!");
				return result;
			}
			
			// 删除已经存在的包数据
			String sqlDel = "delete from ewa_mod_package where mod_ver_id=@modVerId and pkg_sup_id = @moduleSupId";
			DataConnection.updateAndClose(sqlDel, this.ewaConntectionString, rv);
			LOGGER.info("the module packaged file has changed, delete from ewa_mod_package!");
		}


		// 创建新的包数据
		StringBuilder sbPkg = new StringBuilder();
		sbPkg.append("INSERT INTO ewa_mod_package (mod_ver_id, pkg_dlv_date, pkg_len, pkg_md5, pkg_sup_id \n");
		sbPkg.append(", pkg_adm_id, pkg_file) \n");
		sbPkg.append("VALUES(@modVerId, @sys_date, @pkg_len,@pkg_md5, @moduleSupId \n");
		sbPkg.append(", case when @g_adm_id is null then 0 else @g_adm_id end, @pkg_file)");
		String sql5 = sbPkg.toString();

		rv.addOrUpdateValue("pkg_md5", md5);
		rv.addValue("pkg_file", buf, "binary", buf.length);

		DataConnection.updateAndClose(sql5, this.ewaConntectionString, rv);
		LOGGER.info("Put the module packaged to the table ewa_mod_package");
		return result;
	}

	/**
	 * 创建模块的zip包
	 * 
	 * @return
	 * @throws Exception
	 */
	private String createModulePackageZip() throws Exception {
		String id = moduleCode + "_" + moduleVersion;

		Exchange ex = new Exchange(id);
		String exportXmlName = ex.getExportPath() + "/" + Exchange.XML_DES;

		LOGGER.info("Create the res file -> {}", exportXmlName);
		// 保存为资源文件，供Exchange读取
		UXml.saveDocument(packageSource, exportXmlName);

		ex.setReplaceMetaDatabaseName(this.replaceMetaDatabaseName);
		ex.setReplaceWorkDatabaseName(this.replaceWorkDatabaseName);

		// 执行导出模块
		String pkgZipPath = ex.exportGroup();

		// 更新 ewa_mod_ddl 数据库记录
		ex.getTables().forEach(t -> {
			createTableData(t);
		});

		LOGGER.info("Create the module -> {}", pkgZipPath);
		return pkgZipPath;
	}

	/**
	 * 创建数据输出的表达式
	 * 
	 * @param row
	 * @throws Exception
	 */
	private void makeTableRecord(DTRow row) throws Exception {
		String emd_id = row.getCell("emd_id").toString();

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
		node.setAttribute("RefId", emd_id); // 来源参考

		node.setAttribute("DataSource", emdEwaConn);
		node.setAttribute("DatabaseType", emdDatabaseType);
		node.setAttribute("Schema", tableSchema);
		node.setAttribute("TableName", tableName);
		node.setAttribute("TableType", emdType);
		node.setAttribute("IsExport", emdExport);
		node.setAttribute("ExportWhere", emdExportWhere);

		LOGGER.info(UXml.asXml(node));

		this.packageSource.getElementsByTagName("Tables").item(0).appendChild(node);
	}

	/**
	 * 创建配置文件表达式
	 * 
	 * @param row
	 * @throws DOMException
	 * @throws Exception
	 */
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
		LOGGER.info(UXml.asXml(node));
		this.packageSource.getElementsByTagName("EasyWebTemplates").item(0).appendChild(node);
	}

	/**
	 * 记录表的ddl和xml表达式
	 * 
	 * @param row
	 * @return
	 * @throws Exception
	 */
	private void createTableData(Table table) {
		RequestValue rv = new RequestValue();
		try {
			long emdId = Long.parseLong(table.getRefId());
			rv.addOrUpdateValue("emdId", emdId);
		} catch (Exception err) {
			LOGGER.warn("Convert to long {}, {}", table.getRefId(), err.getMessage());
		}

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

	}

	private void createResXmlDocument() {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><BaseInfo><Tables/><EasyWebTemplates/></BaseInfo>";
		packageSource = UXml.asDocument(xml);
	}

	/**
	 * @return the packageSource
	 */
	public Document getPackageSource() {
		return packageSource;
	}

	/**
	 * @return the ewaConntectionString
	 */
	public String getEwaConntectionString() {
		return ewaConntectionString;
	}

	/**
	 * @param ewaConntectionString the ewaConntectionString to set
	 */
	public void setEwaConntectionString(String ewaConntectionString) {
		this.ewaConntectionString = ewaConntectionString;
	}

}
