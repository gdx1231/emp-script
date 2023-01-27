package com.gdxsoft.easyweb.define.group;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.gdxsoft.easyweb.conf.ConfScriptPaths;
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

	
	private String jdbcConfigName; // Get the module's data connection string
	private Document packageSource;
	private DTTable tbMod;
	private DTTable tbModVer;
	private DTTable tbPkg;
	
	public ModuleExport(String moduleCode, String moduleVersion) {
		this.init(moduleCode, moduleVersion, null);
	}
	
	public ModuleExport(String moduleCode, String moduleVersion, String jdbcConfigName) {
		this.init(moduleCode, moduleVersion, jdbcConfigName);
	}

	

	private void init(String moduleCode, String moduleVersion, String jdbcConfigName) {
		this.moduleCode = moduleCode;
		this.moduleVersion = moduleVersion;
		this.jdbcConfigName = jdbcConfigName;

		ConfScriptPaths.getInstance().getLst().forEach(conf -> {
			if (this.jdbcConfigName == null && conf.isJdbc()) {
				// 获取第一个jdbc连接池
				this.jdbcConfigName = conf.getJdbcConfigName();
			}
		});
	}

	public byte[] getExportModuleFile() throws Exception {
		JSONObject initResult = this.initData();
		if (UJSon.checkFalse(initResult)) {
			return null;
		}
		long modVerId = tbModVer.getCell(0, "mod_ver_id").toLong();
		RequestValue rv = new RequestValue();
		rv.addOrUpdateValue("modVerId", modVerId);

		String sqlExists = "select * from ewa_mod_package a where a.mod_ver_id = @modVerId ";
		DTTable tbExistsPkg = DTTable.getJdbcTable(sqlExists, this.jdbcConfigName, rv);
		if (tbExistsPkg.getCount() == 0) {
			return null;
		}
		tbPkg = tbExistsPkg;
		return (byte[]) tbExistsPkg.getCell(0, "pkg_file").getValue();
	}

	/**
	 * 输出元信息
	 * 
	 * @param tbMod
	 * @param tbModVer
	 * @throws Exception
	 */
	private void exportModuleMetaInfo(DTTable tbMod, DTTable tbModVer) throws Exception {
		// <Description>
		// <Name>module</Name>
		// <Version>1</Version>
		// <Description>modulemodulemodule</Description>
		// <Website>123</Website>
		// <Email>123</Email>
		// <Company>sdfsdf</Company>
		// <Contact>123</Contact>
		// <Copyright>12312312</Copyright>
		// <Telephone>123</Telephone>
		// <InnerVer>1</InnerVer>
		// <InnerVerSub0>1</InnerVerSub0>
		// <InnerVerSub1>1</InnerVerSub1>
		// </Description>
		Element root = (Element) this.packageSource.getFirstChild();

		root.setAttribute("Code", this.moduleCode); // = tbMod.getCell(0, "mod_code").toString()
		root.setAttribute("Version", this.moduleVersion); // = tbModVer.getCell(0, "mod_ver").toString()

		Element eleDes = (Element) this.packageSource.getElementsByTagName("Description").item(0);
		// 设置版本
		this.createNode(eleDes, "Code", tbMod.getCell(0, "mod_code").toString());
		this.createNode(eleDes, "Name", tbMod.getCell(0, "mod_name").toString());
		this.createNode(eleDes, "NameEn", tbMod.getCell(0, "mod_name_en").toString());
		this.createNode(eleDes, "Company", tbMod.getCell(0, "mod_company").toString());
		this.createNode(eleDes, "Contact", tbMod.getCell(0, "mod_contact").toString());
		this.createNode(eleDes, "Email", tbMod.getCell(0, "mod_email").toString());
		this.createNode(eleDes, "Website", tbMod.getCell(0, "mod_web").toString());
		this.createNode(eleDes, "OpenSource", tbMod.getCell(0, "mod_open_source").toString());
		// meta库名称
		this.createNode(eleDes, "metaDatabase", tbMod.getCell(0, "MOD_META_DB_NAME").toString());
		// 工作库名称
		this.createNode(eleDes, "workDatabase", tbMod.getCell(0, "MOD_WORK_DB_NAME").toString());
		
		this.createNodeCData(eleDes, "Description", tbMod.getCell(0, "mod_memo").toString());
		this.createNodeCData(eleDes, "DescriptionEn", tbMod.getCell(0, "mod_memo_en").toString());
		this.createNodeCData(eleDes, "Copyright", tbMod.getCell(0, "mod_osp").toString());

		this.createNode(eleDes, "Version", tbModVer.getCell(0, "mod_ver").toString());
		this.createNodeCData(eleDes, "VersionMemo", tbModVer.getCell(0, "mod_ver_memo").toString());
		this.createNodeCData(eleDes, "VersionMemoEn", tbModVer.getCell(0, "mod_ver_memo_en").toString());

	}

	private void createNode(Element parent, String tagName, String innerText) {
		if (innerText == null) {
			return;
		}
		Element item = this.packageSource.createElement(tagName);
		item.setTextContent(innerText);

		parent.appendChild(item);
	}

	private void createNodeCData(Element parent, String tagName, String innerText) {
		if (innerText == null) {
			return;
		}
		Element item = this.packageSource.createElement(tagName);
		CDATASection cdata = this.packageSource.createCDATASection(innerText);
		item.appendChild(cdata);
		parent.appendChild(item);
	}

	public JSONObject initData() {
		RequestValue rv = new RequestValue();
		rv.addOrUpdateValue("moduleCode", moduleCode);
		rv.addOrUpdateValue("moduleVersion", moduleVersion);

		String sql1 = "select * from ewa_mod where mod_code=@moduleCode";
		this.tbMod = DTTable.getJdbcTable(sql1, this.jdbcConfigName, rv);
		if (tbMod.getCount() == 0) {
			return UJSon.rstFalse("No data of the class: " + moduleCode);
		}

		String sql2 = "select * from ewa_mod_ver where mod_code=@moduleCode and mod_ver=@moduleVersion";
		this.tbModVer = DTTable.getJdbcTable(sql2, this.jdbcConfigName, rv);
		if (tbModVer.getCount() == 0) {
			return UJSon.rstFalse("No data of the class: " + moduleCode + " version: " + this.moduleVersion);
		}

		return UJSon.rstTrue(null);
	}

	/**
	 * 导出模块
	 * 
	 * @return
	 * @throws Exception
	 */
	public JSONObject exportModule() throws Exception {
		JSONObject initResult = this.initData();
		if (UJSon.checkFalse(initResult)) {
			return initResult;
		}
		this.createResXmlDocument();

		// 元数据库名称
		String replaceMetaDatabaseName = tbMod.getCell(0, "mod_meta_db_name").toString();
		super.setReplaceMetaDatabaseName(replaceMetaDatabaseName);

		// 工作数据库名称
		String replaceWorkDatabaseName = tbMod.getCell(0, "mod_work_db_name").toString();
		super.setReplaceWorkDatabaseName(replaceWorkDatabaseName);

		// 输出元信息
		this.exportModuleMetaInfo(tbMod, tbModVer);

		long modVerId = tbModVer.getCell(0, "mod_ver_id").toLong();
		RequestValue rv = new RequestValue();
		rv.addOrUpdateValue("modVerId", modVerId);

		String sql3 = "select * from ewa_mod_ddl where mod_ver_id=@modVerId order by emd_id";
		DTTable tbModDdl = DTTable.getJdbcTable(sql3, this.jdbcConfigName, rv);

		JSONObject result = UJSon.rstTrue(null);

		JSONArray arr = new JSONArray();
		result.put("tables", arr);
		for (int i = 0; i < tbModDdl.getCount(); i++) {
			DTRow r = tbModDdl.getRow(i);
			// 创建的表的XML表达式
			this.makeTableRecord(r);
		}

		String sql4 = "select * from ewa_mod_cfgs where  mod_ver_id=@modVerId order by xmlname,itemname";
		DTTable tbModCfg = DTTable.getJdbcTable(sql4, this.jdbcConfigName, rv);

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
		DTTable tbExistsPkg = DTTable.getJdbcTable(sqlExists, this.jdbcConfigName, rv);
		if (tbExistsPkg.getCount() > 0) {
			String existsMd5 = tbExistsPkg.getCell(0, 0).toString();
			if (md5.equalsIgnoreCase(existsMd5)) { // 包内容没有变化
				LOGGER.info("The module packaged file has no change, skip store!");
				return result;
			}

			// 删除已经存在的包数据
			String sqlDel = "delete from ewa_mod_package where mod_ver_id=@modVerId ";
			DataConnection.updateAndClose(sqlDel, this.jdbcConfigName, rv);
			LOGGER.info("the module packaged file has changed, delete from ewa_mod_package!");
		}

		// 创建新的包数据
		StringBuilder sbPkg = new StringBuilder();
		sbPkg.append("INSERT INTO ewa_mod_package (mod_ver_id, pkg_dlv_date, pkg_len, pkg_md5, pkg_sup_id \n");
		sbPkg.append(", pkg_adm_id, pkg_file) \n");
		sbPkg.append("VALUES(@modVerId, @sys_date, @pkg_len,@pkg_md5, 0 \n");
		sbPkg.append(", 0, @pkg_file)");
		String sql5 = sbPkg.toString();

		rv.addOrUpdateValue("pkg_md5", md5);
		rv.addValue("pkg_file", buf, "binary", buf.length);

		DataConnection.updateAndClose(sql5, this.jdbcConfigName, rv);
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
		DataConnection.updateAndClose(sql, this.jdbcConfigName, rv);

	}

	private void createResXmlDocument() {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
		sb.append("<BaseInfo>");
		sb.append("<Description/>");
		sb.append("<Tables/>");
		sb.append("<EasyWebTemplates/>");
		sb.append("</BaseInfo>");
		String xml = sb.toString();
		packageSource = UXml.asDocument(xml);
	}

	/**
	 * @return the packageSource
	 */
	public Document getPackageSource() {
		return packageSource;
	}

	/**
	 * @return the jdbcConfigName
	 */
	public String getJdbcConfigName() {
		return jdbcConfigName;
	}

	/**
	 * @param jdbcConfigName the jdbcConfigName to set
	 */
	public void setJdbcConfigName(String jdbcConfigName) {
		this.jdbcConfigName = jdbcConfigName;
	}

	/**
	 * @return the tbPkg
	 */
	public DTTable getTbPkg() {
		return tbPkg;
	}

	/**
	 * @return the tbMod
	 */
	public DTTable getTbMod() {
		return tbMod;
	}

	/**
	 * @return the tbModVer
	 */
	public DTTable getTbModVer() {
		return tbModVer;
	}

}
