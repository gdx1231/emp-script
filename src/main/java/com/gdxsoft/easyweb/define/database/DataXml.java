/**
 * 
 */
package com.gdxsoft.easyweb.define.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.w3c.dom.*;

import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.datasource.DataResult;
import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.Utils;

/**
 * @author Administrator
 * 
 */
public class DataXml {

	/**
	 * 将Table对象映射成XML
	 * 
	 * @param table
	 * @return
	 */
	public static String tableXml(Table table) {

		Document doc = UXml.createBlankDocument();
		Element ele = doc.createElement("table");
		Element eleFields = doc.createElement("fields");
		Element elePk = doc.createElement("pk");
		Element eleFks = doc.createElement("fks");
		Element eleIndexes = doc.createElement("indexes");

		ele.appendChild(eleFields);
		ele.appendChild(elePk);
		ele.appendChild(eleFks);
		ele.appendChild(eleIndexes);
		doc.appendChild(ele);

		UObjectValue ov = new UObjectValue();
		ov.setObject(table);
		ArrayList<String[]> al = ov.getAllValue();
		createParameter(ele, al);

		// fields
		for (int i = 0; i < table.getFields().size(); i++) {
			Field f1 = table.getFields().get(table.getFields().getFieldList().get(i));
			Element eleField = doc.createElement("field");
			eleFields.appendChild(eleField);
			ov.setObject(f1);
			al = ov.getAllValue();
			createParameter(eleField, al);

			// System.out.println(UXml.asXml(eleField));
		}
		// pk
		if (table.getPk() != null) {
			ov.setObject(table.getPk());
			al = ov.getAllValue();
			createParameter(elePk, al);
			for (int i = 0; i < table.getPk().getPkFields().size(); i++) {
				Element eleField = doc.createElement("pkfield");
				eleField.setAttribute("name", table.getPk().getPkFields().get(i).getName());
				elePk.appendChild(eleField);
			}
		}
		// fk
		if (table.getFks().size() > 0) {
			for (int i = 0; i < table.getFks().size(); i++) {
				Element eleFk = doc.createElement("fk");
				eleFks.appendChild(eleFk);
				ov.setObject(table.getFks().get(i));
				al = ov.getAllValue();
				createParameter(eleFk, al);
				for (int m = 0; m < table.getFks().get(i).getFkFields().size(); m++) {
					Element eleField = doc.createElement("fkfield");
					eleField.setAttribute("name", table.getFks().get(i).getFkFields().get(m).getName());
					eleFk.appendChild(eleField);
				}

				Element eleFkPk = doc.createElement("pk");
				TablePk fkpk = table.getFks().get(i).getPk();
				eleFkPk.setAttribute("tablename", fkpk.getTableName());
				eleFk.appendChild(eleFkPk);
				for (int m = 0; m < fkpk.getPkFields().size(); m++) {
					Element f1 = doc.createElement("pkfield");
					eleFkPk.appendChild(f1);
					f1.setAttribute("name", fkpk.getPkFields().get(m).getName());
				}
			}
		}
		// indexes
		if (table.getIndexes().size() > 0) {
			for (int i = 0; i < table.getIndexes().size(); i++) {
				Element eleIdx = doc.createElement("index");
				eleIndexes.appendChild(eleIdx);
				TableIndex ti = table.getIndexes().get(i);
				ov.setObject(ti);
				al = ov.getAllValue();
				createParameter(eleIdx, al);
				for (int m = 0; m < ti.getIndexFields().size(); m++) {
					Element eleField = doc.createElement("indexfield");
					IndexField indexField = ti.getIndexFields().get(m);
					ov.setObject(indexField);
					al = ov.getAllValue();
					createParameter(eleField, al);
					eleIdx.appendChild(eleField);
				}
			}
		}
		return UXml.asXml(doc);
	}

	public static void createParameter(Element ele, ArrayList<String[]> al) {
		for (int i = 0; i < al.size(); i++) {
			String name = al.get(i)[0].toLowerCase().replaceFirst("get", "").replaceFirst("is", "");
			if (name.equals("tojs")) {
				continue;
			}
			String val = al.get(i)[1];
			ele.setAttribute(name, val);
		}
	}

	public static String fieldXml() {
		return "";
	}

	/**
	 * 从Xml中生成Table类
	 * 
	 * @param node Xml节点
	 * @return Table
	 */
	public static Table parseTable(Node node) {
		Table table = new Table();
		table.initBlankFrame();
		Fields fs = table.getFields();
		TablePk pk = table.getPk();
		ArrayList<TableFk> fks = table.getFks();
		ArrayList<TableIndex> indexes = table.getIndexes();
		// table
		UObjectValue ov = new UObjectValue();
		ov.setObject(table);
		ov.setAllValue((Element) node);
		ov.setObject(table);

		// fields
		NodeList nl = UXml.retNodeList(node, "fields/field");
		for (int i = 0; i < nl.getLength(); i++) {
			Field f1 = new Field();
			ov.setObject(f1);
			ov.setAllValue((Element) nl.item(i));
			fs.put(f1.getName(), f1);
			fs.getFieldList().add(f1.getName());
		}

		// pk
		Node nodePk = UXml.retNode(node, "pk");
		if (nodePk != null) {
			ov.setObject(pk);
			ov.setAllValue((Element) nodePk);
			nl = UXml.retNodeList(node, "pk/pkfield");
			for (int i = 0; i < nl.getLength(); i++) {
				Field f1 = new Field(); // pk 字段
				ov.setObject(f1);
				ov.setAllValue((Element) nl.item(i));
				pk.getPkFields().add(f1);
			}
		}
		// fk
		nl = UXml.retNodeList(node, "fks/fk");
		for (int i = 0; i < nl.getLength(); i++) {
			TableFk fk = new TableFk(); // fk信息
			ov.setObject(fk);
			ov.setAllValue((Element) nl.item(i));
			fks.add(fk);
			NodeList nl1 = UXml.retNodeList(nl.item(i), "fkfield");
			for (int m = 0; m < nl1.getLength(); m++) {
				Field f1 = new Field(); // fk 字段
				ov.setObject(f1);
				ov.setAllValue((Element) nl1.item(m));
				fk.getFkFields().add(f1);
			}
			TablePk fkpk = new TablePk();
			Node nodeFkPk = UXml.retNode(nl.item(i), "pk");
			ov.setObject(fkpk);
			ov.setAllValue((Element) nodeFkPk);
			fk.setPk(fkpk);

			nl1 = UXml.retNodeList(nodeFkPk, "pkfield");
			for (int m = 0; m < nl1.getLength(); m++) {
				Field f1 = new Field(); // fk 字段
				ov.setObject(f1);
				ov.setAllValue((Element) nl1.item(m));
				fk.getPk().getPkFields().add(f1);
			}

		}

		// index
		nl = UXml.retNodeList(node, "indexes/index");
		for (int i = 0; i < nl.getLength(); i++) {
			TableIndex index = new TableIndex(); // fk信息
			ov.setObject(index);
			ov.setAllValue((Element) nl.item(i));
			indexes.add(index);
			NodeList nl1 = UXml.retNodeList(nl.item(i), "indexfield");
			for (int m = 0; m < nl1.getLength(); m++) {
				IndexField f1 = new IndexField(); // fk 字段
				ov.setObject(f1);
				ov.setAllValue((Element) nl1.item(m));
				index.getIndexFields().add(f1);
			}
		}

		return table;
	}

	public static String paraseTableXml(Node node) {
		return "";
	}

	public static String paraseTableXml(Table table) {
		StringBuilder sb = new StringBuilder();

		return sb.toString();
	}

	public static String paraseTableFieldXml(Field field) {
		StringBuilder sb = new StringBuilder();
		sb.append(field.getName() + " ");
		return "";
	}

	public static String paraseTableFiledType(Field field) {
		StringBuilder sb = new StringBuilder();
		if (field.getDataType() == 4) {

		}
		return sb.toString();
	}

	public static String tableDataXml(Table table, String datasourceConfigName) throws SQLException {
		DataConnection conn = new DataConnection();
		conn.setConfigName(datasourceConfigName);
		StringBuilder sb = new StringBuilder();
		String sql = "SELECT * FROM " + table.getName();
		sb.append("<data>\r\n");
		if (!conn.executeQuery(sql)) {
			conn.close();
			throw new SQLException(conn.getErrorMsg());
		}
		DataResult dr = (DataResult) conn.getResultSetList().getLast();
		ResultSet rs = dr.getResultSet();
		try {
			while (rs.next()) {
				sb.append("<val");
				for (int i = 0; i < table.getFields().size(); i++) {
					String val = rs.getString(i + 1);
					if (val == null) {
						val = "null";
					} else {
						val = Utils.textToInputValue(val);
					}
					sb.append(" v" + i + "=\"" + val + "\"");
				}
				sb.append(" />\r\n");
			}
			sb.append("</data>");
			return sb.toString();
		} catch (SQLException e) {
			throw e;
		} finally {
			conn.close();
		}

	}
}
