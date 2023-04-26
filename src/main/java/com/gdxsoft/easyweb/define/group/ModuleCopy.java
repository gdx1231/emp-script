package com.gdxsoft.easyweb.define.group;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.conf.ConfScriptPaths;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.datasource.SqlUtils;
import com.gdxsoft.easyweb.define.database.Table;
import com.gdxsoft.easyweb.define.group.dao.*;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UJSon;
import com.gdxsoft.easyweb.utils.USnowflake;

/**
 * 导入模块
 * 
 * @author admin
 *
 */
public class ModuleCopy extends ModuleBase {

	private String importDataConn;
	private String jdbcConfigName;
	private EwaMod ewaMod;
	private EwaModVer ewaModVer;
	private Map<String, Element> tableMap;

	public ModuleCopy(String replaceMetaDatabaseName, String replaceWorkDatabaseName) {
		this.replaceMetaDatabaseName = replaceMetaDatabaseName;
		this.replaceWorkDatabaseName = replaceWorkDatabaseName;

		ConfScriptPaths.getInstance().getLst().forEach(conf -> {
			if (this.jdbcConfigName == null && conf.isJdbc()) {
				// 获取第一个jdbc连接池
				this.jdbcConfigName = conf.getJdbcConfigName();
			}
		});

	}

	private EwaMod getOrNewMyEwaMod(EwaModDownload mod) {
		EwaModDao ewaModDao = new EwaModDao();
		ewaModDao.setConfigName(jdbcConfigName);
		EwaMod ewaMod = ewaModDao.getRecord(mod.getModCode());
		if (ewaMod != null) {
			return ewaMod;
		}
		ewaMod = new EwaMod();
		ewaMod.setModCode(this.moduleCode);
		ewaMod.setModCdate(mod.getModCdate());
		ewaMod.setModMdate(mod.getModMdate());
		ewaMod.setModCompany(mod.getModCompany());
		ewaMod.setModName(mod.getModName());
		ewaMod.setModNameEn(mod.getModNameEn());
		ewaMod.setModContact(mod.getModContact());
		ewaMod.setModEmail(mod.getModContact());
		ewaMod.setModEwaConn(this.jdbcConfigName);
		ewaMod.setModMemo(mod.getModMemo());
		ewaMod.setModMemoEn(mod.getModMemoEn());
		ewaMod.setModMetaDbName(this.replaceMetaDatabaseName);
		ewaMod.setModWorkDbName(this.replaceWorkDatabaseName);
		ewaMod.setModOpenSource(mod.getModOpenSource());
		ewaMod.setModOsp(mod.getModOsp());
		ewaMod.setModStatus("NEW");
		ewaMod.setModAdmId(0);
		ewaMod.setModSupId(0);
		ewaMod.setModWeb(mod.getModWeb());

		ewaModDao.newRecord(ewaMod);

		return ewaMod;
	}

	private EwaModVer getOrNewMyEwaModVer(EwaModDownload mod) {
		EwaModVerDao d = new EwaModVerDao();
		d.setConfigName(jdbcConfigName);
		ArrayList<EwaModVer> al = d.getRecords("MOD_CODE='" + this.moduleCode.replace("'", "''") + "' and MOD_VER='"
				+ this.moduleVersion.replace("'", "''") + "'");
		if (al.size() > 0) {
			return al.get(0);
		}

		long id = USnowflake.nextId();

		EwaModVer v = new EwaModVer();
		v.setModVerId(id);

		v.setModCode(this.moduleCode);
		v.setModVer(this.moduleVersion);
		v.setModVerAdmId(0);
		v.setModVer(this.moduleVersion);
		v.setModVerMemo("复制于：" + mod.getModCode() + ", ver:" + mod.getModVer() + "");
		v.setModVerMemoEn("");
		v.setModVerStatus("USED");
		v.setModVerAdmId(0);
		v.setModVerSupId(0);
		v.setModVerCdate(new Date());
		v.setModVerMdate(new Date());

		d.newRecord(v);
		return v;
	}

	private EwaModDdl addTable(Table table) {
		String ewaConn = table.fromMetaDatabase() ? this.replaceMetaDatabaseName : this.replaceWorkDatabaseName;

		boolean isSourceSqlServer = SqlUtils.isSqlServer(table.getDatabaseType());
		boolean isSourceMySql = SqlUtils.isMySql(table.getDatabaseType());

		EwaModDdlDao d = new EwaModDdlDao();
		d.setConfigName(jdbcConfigName);

		String w = "MOD_VER_ID=" + this.ewaModVer.getModVerId() + " and EMD_EWA_CONN='" + ewaConn.replace("'", "''")
				+ "' and TABLE_NAME='" + table.getName().replace("'", "''") + "'";
		ArrayList<EwaModDdl> al = d.getRecords(w);
		if (al.size() > 0) {
			return al.get(0);
		}
		DataConnection cnn = new DataConnection();
		cnn.setConfigName(ewaConn);
		boolean isTargetSqlServer = SqlUtils.isSqlServer(cnn);
		boolean isTargetMySql = SqlUtils.isMySql(cnn);

		EwaModDdl ddl = new EwaModDdl();
		ddl.setEmdDatabaseType(cnn.getDatabaseType());
		ddl.setEmdAdmId(0);
		ddl.setEmdSupId(0);
		ddl.setEmdCdate(new Date());
		ddl.setEmdMdate(new Date());
		ddl.setModVerId(this.ewaModVer.getModVerId());
		ddl.setEmdEwaConn(ewaConn);
		ddl.setTableName(table.getName());
		ddl.setEmdType(table.getTableType());

		if (isTargetSqlServer && isSourceSqlServer || isTargetMySql && isSourceMySql) {
			ddl.setTableCatalog(table.getCatalogName() == null ? "" : table.getCatalogName());
			ddl.setTableSchema(table.getSchemaName() == null ? "" : table.getSchemaName());
		} else {
			ddl.setTableSchema(table.getCatalogName() == null ? "" : table.getCatalogName());
			ddl.setTableCatalog(table.getSchemaName() == null ? "" : table.getSchemaName());
		}
		String export = "false"; // 导出数据
		String exportWhere = "1=2"; // 导出数据的where条件

		String key = table.getName() + "/" + table.getTableType();
		if (this.tableMap.containsKey(key)) { // 在des的Table节点
			Element ele = this.tableMap.get(key);
			export = ele.getAttribute("IsExport");
			exportWhere = ele.getAttribute("ExportWhere");
		}

		ddl.setEmdExport(export);
		ddl.setEmdExportWhere(exportWhere);

		d.newRecord(ddl);

		return ddl;
	}

	private EwaModCfgs importCfg(Element cfg) {
		String defaultItmename = cfg.getAttribute("ExportDefaultItmename");

		String defaultXmlName = cfg.getAttribute("ExportDefaultXmlname");
		defaultXmlName = UserConfig.filterXmlNameByJdbc(defaultXmlName);

		String description = "";
		NodeList nl = cfg.getElementsByTagName("DescriptionSet");
		if (nl.getLength() > 0) {
			Element e = (Element) nl.item(0);
			nl = e.getElementsByTagName("Set");
			if (nl.getLength() > 0) {
				Element e1 = (Element) nl.item(0);
				description = e1.getAttribute("Info");
			}
		}

		EwaModCfgsDao d = new EwaModCfgsDao();
		EwaModCfgs o = d.getRecord(this.ewaModVer.getModVerId(), defaultXmlName, defaultXmlName);
		if (o != null) {
			return o;
		}

		o = new EwaModCfgs();
		o.setXmlname(defaultXmlName);
		o.setItemname(defaultItmename);

		o.setEmcDefXmlname(defaultXmlName);
		o.setEmcDefItemname(defaultItmename);

		o.setEmcCdate(new Date());
		o.setDescription(description);
		o.setEmcSupId(0);
		o.setEmcAdmId(0);

		d.newRecord(o);

		return o;
	}

	public JSONObject copyDownloadModule(int modDlId, RequestValue rv) {
		// 记录导入DDL到日志中

		EwaModDownloadDao dao1 = new EwaModDownloadDao();
		dao1.setConfigName(jdbcConfigName);
		EwaModDownload mod = dao1.getRecord(modDlId);

		if (mod == null) {
			return UJSon.rstFalse("No data");
		}

		if (rv.isNotBlank("new_mod_code")) {
			this.moduleCode = rv.s("new_mod_code");
		} else {
			this.moduleCode = mod.getModCode();
		}
		if (rv.isNotBlank("new_mod_ver")) {
			this.moduleVersion = rv.s("new_mod_ver");
		} else {
			String[] codes = mod.getModVer().split(".");
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < codes.length - 1; i++) {
				sb.append(codes[i]).append(".");
			}
			String last = codes[codes.length - 1];
			try {
				long lastV = Long.parseLong(last);
				last = (lastV + 1) + "";
			} catch (Exception err) {
				last = last + ".1";
			}
			sb.append(last);
			this.moduleVersion = sb.toString();
		}

		File tempFile;
		try {
			tempFile = File.createTempFile("importModuleFromDownloadModule", ".zip");
		} catch (IOException e) {
			return UJSon.rstFalse(e.getMessage());
		}
		byte[] buf;
		try {
			// buf = (byte[]) tb.getCell(0, "pkg_file").getValue();
			buf = mod.getPkgFile();
		} catch (Exception e) {
			return UJSon.rstFalse(e.getMessage());
		}
		try {
			UFile.createBinaryFile(tempFile.getAbsolutePath(), buf, true);
		} catch (Exception e) {
			return UJSon.rstFalse(e.getMessage());
		}

		if (!tempFile.exists()) {
			return UJSon.rstFalse("The file " + tempFile + " not exists");
		}

		this.ewaMod = this.getOrNewMyEwaMod(mod);
		this.ewaModVer = getOrNewMyEwaModVer(mod);

		JSONObject result = UJSon.rstTrue();

		String id = moduleCode + "_" + moduleVersion;
		result.put(id, id);
		result.put("replaceMetaDatabaseName", replaceMetaDatabaseName);
		result.put("replaceWorkDatabaseName", replaceWorkDatabaseName);
		result.put("modulePackageFile", tempFile);

		Exchange ex = new Exchange(id, this.importDataConn);
		this.tableMap = new HashMap<>();
		try {
			ex.importGroup(tempFile.getAbsolutePath());
			NodeList nl1 = ex.getDocDes().getElementsByTagName("Table");
			for (int i = 0; i < nl1.getLength(); i++) {
				Element ele = (Element) nl1.item(i);
				String name = ele.getAttribute("TableName");
				String tableType = ele.getAttribute("TableType");
				this.tableMap.put(name + "/" + tableType, ele);
			}

			ImportTables importTables = new ImportTables(ex.getDocTable(), ex.getDocData(), null);
			importTables.setReplaceMetaDatabaseName(this.replaceMetaDatabaseName);
			importTables.setReplaceWorkDatabaseName(this.replaceWorkDatabaseName);

			importTables.readTables();

			for (int i = 0; i < importTables.getTables().length; i++) {
				Table table = importTables.getTables()[i];
				addTable(table);
			}

			NodeList nl = ex.getDocCfg().getElementsByTagName("EasyWebTemplate");
			for (int i = 0; i < nl.getLength(); i++) {
				Element cfg = (Element) nl.item(i);
				importCfg(cfg);
			}
		} catch (Exception e) {
			UJSon.rstSetFalse(result, e.getLocalizedMessage());
		}

		return result;

	}

	/**
	 * @return the ewaMod
	 */
	public EwaMod getEwaMod() {
		return ewaMod;
	}

	/**
	 * @return the ewaModVer
	 */
	public EwaModVer getEwaModVer() {
		return ewaModVer;
	}

}
