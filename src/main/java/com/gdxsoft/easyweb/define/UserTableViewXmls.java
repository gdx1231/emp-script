package com.gdxsoft.easyweb.define;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import com.gdxsoft.easyweb.datasource.ConnectionConfig;
import com.gdxsoft.easyweb.datasource.ConnectionConfigs;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.define.database.DataXml;
import com.gdxsoft.easyweb.define.database.Field;
import com.gdxsoft.easyweb.define.database.Schema;
import com.gdxsoft.easyweb.define.database.Table;
import com.gdxsoft.easyweb.define.database.Tables;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.UXml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class UserTableViewXmls {

	private static Logger LOGGER = LoggerFactory.getLogger(UserTableViewXmls.class);
	private ConnectionConfigs _Cfgs;
	private Schema _Schema;
	private Tables _Tables;
	private String _linkType = null;

	public UserTableViewXmls() throws ParserConfigurationException, SAXException, IOException {
		_Cfgs = ConnectionConfigs.instance();
	}

	public String getCfgsXml() {
		Document doc = UXml.createBlankDocument();
		Element eRoot = doc.createElement("TableViewList");
		eRoot.setAttribute("DataRow", "Row");
		doc.appendChild(eRoot);

		for (String key : this._Cfgs.keySet()) {
			ConnectionConfig cfg = this._Cfgs.get(key);
			Element node1 = doc.createElement("Row");
			node1.setAttribute("Name", cfg.getName() + "|" + cfg.getType());
			node1.setAttribute("Key", "DATABASE;" + cfg.getName());
			node1.setAttribute("ParentKey", "");
			node1.setAttribute("EWAMORECNT", "1");
			eRoot.appendChild(node1);
		}

		return UXml.asXmlAll(doc);
	}

	public String getMoreXml1(String key, String linkType) {
		_linkType = linkType;
		return this.getMoreXml(key);
	}

	public String getMoreXml(String key) {
		String[] s1 = key.split(";");
		if (s1.length == 2) {
			return this.getTableXml(s1[1], key + ";");
		}
		if (s1.length == 4) {
			String xml = this.getFieldsXml(s1[1], s1[3]);
			return xml;
		}
		return "";
	}

	public String getTableXml(String configName, String keyPrefix) {
		_Schema = new Schema(configName);
		_Tables = _Schema.getTables();

		Document doc = UXml.createBlankDocument();
		Element eRoot = doc.createElement("TableViewList");
		eRoot.setAttribute("DataRow", "Row");
		doc.appendChild(eRoot);

		Element node = doc.createElement("Row");
		node.setAttribute("Name", "Table");
		node.setAttribute("Key", keyPrefix + "  TABLE  ");
		node.setAttribute("ParentKey", "");
		node.setAttribute("EWAMORECNT", "0");
		Element node1 = doc.createElement("Row");
		node1.setAttribute("Name", "View");
		node1.setAttribute("Key", keyPrefix + "  VIEW  ");
		node1.setAttribute("ParentKey", "");
		node1.setAttribute("EWAMORECNT", "0");
		eRoot.appendChild(node);
		eRoot.appendChild(node1);

		ArrayList<String> al = _Tables.getTableList();
		for (int i = 0; i < al.size(); i++) {
			Table tb = _Tables.get(al.get(i));
			createTableNode(doc, tb, keyPrefix);
		}
		return UXml.asXmlAll(doc);
	}

	private void createTableNode(Document doc, Table table, String keyPrefix) {
		Element node = doc.createElement("Row");
		if (_linkType != null && _linkType.equalsIgnoreCase("GROUP")) {
			String s = "<input type=checkbox value='" + table.getTableType() + ";" + this._Schema.getSchemaName() + ";"
					+ table.getName() + ";" + this._Schema.getDatabaseType() + ";"
					+ this._Schema.getConnectionConfigName() + "'>" + table.getName();
			node.setAttribute("Name", s);
		} else {
			node.setAttribute("Name", table.getName());
		}
		node.setAttribute("ParentKey", keyPrefix + table.getTableType());
		node.setAttribute("Key", keyPrefix + table.getTableType() + ";" + table.getName());
		node.setAttribute("EWAMORECNT", "0");
		doc.getFirstChild().appendChild(node);
	}

	public String getFieldsXml(String configName, String tableName) {
		_Schema = new Schema(configName);
		_Tables = _Schema.getTables();
		Table table = _Tables.get(tableName);
		return DataXml.tableXml(table);
	}

	/**
	 * 修改字段的描述
	 * 
	 * @param configName
	 * @param tableName  表名
	 * @param columnName 字段
	 * @param des        描述
	 */
	public void modifyColumnDescription(String configName, String tableName, String columnName, String des) {
		DataConnection cnn = new DataConnection();
		cnn.setConfigName(configName);
		if (cnn.getDatabaseType().equalsIgnoreCase("mssql")) {
			StringBuilder sb=new StringBuilder();
			sb.append("IF not EXISTS(\n");
			sb.append("	SELECT * from  sys.extended_properties where major_id=OBJECT_ID(@table) and minor_id in (\n");
			sb.append("		select colid from syscolumns where id=OBJECT_ID(@table) and name=@field\n");
			sb.append("	)\n");
			sb.append(")\n");
			sb.append("BEGIN\n");
			sb.append("	EXEC sys.sp_addextendedproperty N'MS_Description', @des , \n");
			sb.append("		N'SCHEMA', N'dbo', N'TABLE',\n");
			sb.append("		@table, N'COLUMN',@field;\n");
			sb.append("END\n");
			sb.append("ELSE\n");
			sb.append("BEGIN\n");
			sb.append("	EXECUTE sys.sp_updateextendedproperty N'MS_Description', @des , \n");
			sb.append("		N'SCHEMA',N'dbo', N'TABLE',\n");
			sb.append("		@table, N'COLUMN', @field;\n");
			sb.append("END\n");
			
			String sql =  sb.toString();
			RequestValue rv=new RequestValue();
			rv.addValue("table", tableName);
			rv.addValue("field", columnName);
			rv.addValue("des", des);
			
			cnn.setRequestValue(rv);
			cnn.executeUpdate(sql);
			if (cnn.getErrorMsg() != null) {
				LOGGER.error(cnn.getErrorMsg());
			}
			cnn.close();
		} else if (cnn.getDatabaseType().equalsIgnoreCase("mysql")) {
			_Schema = new Schema(configName);
			_Tables = _Schema.getTables();
			Table table = _Tables.get(tableName);

			Field field = null;
			for (int i = 0; i < table.getFields().size(); i++) {
				String fieldName = table.getFields().getFieldList().get(i);
				if (fieldName.equalsIgnoreCase(columnName)) {
					field = table.getFields().get(fieldName);
					break;
				}
			}
			if (field == null) {
				return;
			}
			String fieldType = field.getSqlType();
			StringBuilder sb = new StringBuilder();
			sb.append("ALTER TABLE `");
			sb.append(tableName);
			sb.append("` MODIFY COLUMN `");
			sb.append(columnName);
			sb.append("` ");
			sb.append(fieldType);

			if (!field.isNull()) {
				sb.append(" NOT NULL");
			}
			if (field.isIdentity()) {
				sb.append(" auto_increment");
			}
			sb.append(" COMMENT '" + des.replace("'", "''") + "'");

			String sql = sb.toString();

			cnn.executeUpdate(sql);
			if (cnn.getErrorMsg() != null) {
				LOGGER.error(cnn.getErrorMsg());
			}

			cnn.close();

		}
	}

}
