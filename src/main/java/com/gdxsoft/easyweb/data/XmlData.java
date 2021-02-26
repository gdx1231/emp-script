package com.gdxsoft.easyweb.data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.Utils;

public class XmlData {

	private String _XmlName;
	private Document _Doc;
	private DTTable _Table;

	public XmlData() {
		this._Table = new DTTable();
	}

	/**
	 * 读取XML文件数据，如果存在则读取，否则生成空数据
	 * 
	 * @param xmlName
	 *            文件名
	 * @param fields
	 *            字段
	 * @param tagPath
	 *            用“/”分割的路径
	 * @param isAttribute
	 *            是否通过属性获取
	 * @throws Exception
	 */
	public void readData(String xmlName, String[] fields, String tagPath,
			boolean isAttribute) throws Exception {
		this._XmlName = xmlName;
		this._Table.setIsBuildIndex(true);
		this._Table.initXmlColumnsByFields(fields, isAttribute);

		File f = new File(xmlName);

		if (!f.exists()) {
			f.getParentFile().mkdirs();

			this._Doc = UXml.createBlankDocument();

			String[] paths = tagPath.split("/");

			int len = paths.length;
			if (isAttribute) {
				len = len - 1;
			}
			Element e = null;
			for (int i = 0; i < len; i++) {

				Element e1 = this._Doc.createElement(paths[i]);
				if (i == 0) {
					this._Doc.appendChild(e1);
				} else {
					e.appendChild(e1);
				}
				e = e1;
			}
			UXml.saveDocument(this._Doc, this._XmlName);
		}
		this.readData(xmlName, tagPath, isAttribute);

	}

	/**
	 * 读取XML文件数据，自动获取字段信息
	 * 
	 * @param xmlName
	 *            文件名
	 * @param tagPath
	 *            用“/”分割的路径
	 * @param isAttribute
	 *            是否通过属性获取
	 * @throws Exception
	 */
	public void readData(String xmlName, String tagPath, boolean isAttribute)
			throws Exception {
		this._XmlName = xmlName;
		this._Doc = UXml.retDocument(xmlName);
		this._Table.setIsBuildIndex(true);

		if (isAttribute) {
			this._Table.initData(this._Doc, tagPath);
		} else {
			this._Table.initDataByXmlChildNodes(this._Doc, tagPath);
		}
	}

	/**
	 * 更新一行数据
	 * 
	 * @param index
	 * @param names
	 * @param vals
	 */
	public void updateRow(int index, String[] names, String[] vals) {
		if (index < 0 || index >= this._Table.getCount()) {
			return;
		}
		DTRow row = this._Table.getRow(index);
		for (int i = 0; i < names.length; i++) {
			if (this._Table.getColumns().testName(names[i])) {
				try {
					DTCell cell = row.getCell(names[i]);
					cell.setValue(vals[i]);
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}
		}
	}

	public void updateRow(int index, ArrayList<String> names,
			ArrayList<String> vals) {
		String[] names1 = new String[names.size()];
		String[] vals1 = new String[names1.length];

		names1 = names.toArray(names1);
		vals1 = vals.toArray(vals1);

		this.updateRow(index, names1, vals1);
	}

	public void updateRow(int index, String names, String vals) {
		String[] names1 = Utils.splitString(names, ",");
		String[] vals1 = Utils.splitString(vals, ",");

		this.updateRow(index, names1, vals1);
	}

	/**
	 * 新增一行数据
	 * 
	 * @param names
	 * @param vals
	 */
	public void insertRow(String[] names, String[] vals) {
		DTRow row = this._Table.addXmlNewRow();
		this.updateRow(row.getIndex(), names, vals);
	}

	public void insertRow(ArrayList<String> names, ArrayList<String> vals) {
		String[] names1 = new String[names.size()];
		String[] vals1 = new String[names1.length];

		names1 = names.toArray(names1);
		vals1 = vals.toArray(vals1);

		this.insertRow(names1, vals1);
	}

	/**
	 * 新增一行数据
	 * 
	 * @param names
	 * @param vals
	 */
	public void insertRow(String names, String vals) {
		String[] names1 = Utils.splitString(names, ",");
		String[] vals1 = Utils.splitString(vals, ",");

		this.insertRow(names1, vals1);
	}

	/**
	 * 更新或新增多行
	 * 
	 * @param names
	 *            用“，”分割的字符串，表示字段名
	 * @param vals
	 *            用“~”分割行，用“，”分割字段内容
	 * @param pks
	 *            用“，”分割的查找字段
	 */
	public void updateOrInsertRows(String names, String vals, String pks) {
		String[] name = Utils.splitString(names, ",");
		String[][] val = Utils.split2String(vals, "~", ",");
		String[] pk = Utils.splitString(pks, ",");
		updateOrInsertRows(name, val, pk);
	}

	public void updateOrInsertRows(ArrayList<String> names,
			ArrayList<ArrayList<String>> vals, ArrayList<String> pks) {
		String[] names1 = new String[names.size()];
		String[][] vals1 = new String[vals.size()][];
		String[] pks1 = new String[pks.size()];
		for (int i = 0; i < vals.size(); i++) {
			ArrayList<String> v = vals.get(i);
			vals1[i] = new String[v.size()];
			vals1[i] = v.toArray(vals1[i]);
		}
		names1 = names.toArray(names1);
		pks1 = pks.toArray(pks1);
		updateOrInsertRows(names1, vals1, pks1);
	}

	/**
	 * 
	 * @param names
	 * @param vals
	 *            传递的值数组
	 * @param pks
	 *            主键数组
	 */
	public void updateOrInsertRows(String[] names, String[][] vals, String[] pks) {
		ArrayList<HashMap<String, String>> al = new ArrayList<HashMap<String, String>>();
		for (int m = 0; m < vals.length; m++) {
			HashMap<String, String> v = new HashMap<String, String>();
			al.add(v);
			for (int i = 0; i < names.length; i++) {
				v.put(names[i].trim().toUpperCase(), vals[m][i]);
			}
		}

		for (int kk = 0; kk < al.size(); kk++) {
			String exp = "";
			HashMap<String, String> v = al.get(kk);
			for (int i = 0; i < pks.length; i++) {
				if (i > 0) {
					exp += ",";
				}
				exp += pks[i] + "=" + v.get(pks[i].toUpperCase().trim());
			}
			Integer[] rows = this._Table.getIndexes().find(exp);

			if (rows == null || rows.length == 0) {
				this.insertRow(names, vals[kk]);
			} else {
				this.updateRow(rows[0], names, vals[kk]);
			}
		}
	}

	public String getXml() {
		return UXml.asXml(this._Doc);
	}

	public void save() {
		UXml.saveDocument(this._Doc, this._XmlName);
	}

	/**
	 * @return the _XmlName
	 */
	public String getXmlName() {
		return _XmlName;
	}

	/**
	 * @param xmlName
	 *            the _XmlName to set
	 */
	public void setXmlName(String xmlName) {
		_XmlName = xmlName;
	}

	/**
	 * @return the _Doc
	 */
	public Document getDoc() {
		return _Doc;
	}

	/**
	 * @param doc
	 *            the _Doc to set
	 */
	public void setDoc(Document doc) {
		_Doc = doc;
	}

	/**
	 * @return the _Table
	 */
	public DTTable getTable() {
		return _Table;
	}

	/**
	 * @param table
	 *            the _Table to set
	 */
	public void setTable(DTTable table) {
		_Table = table;
	}
}
