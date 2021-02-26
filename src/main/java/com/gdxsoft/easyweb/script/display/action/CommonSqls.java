package com.gdxsoft.easyweb.script.display.action;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.UFileCheck;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MList;
import com.gdxsoft.easyweb.utils.msnet.MListStr;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class CommonSqls {

	private static String PATH = "__ewa_common.sqls";

	private static MTable SQLS;

	/**
	 * 初始化
	 */
	private synchronized static void init() {
		SQLS = new MTable();
		String path = UPath.getScriptPath() + "/" + PATH;
		Document doc = null;
		try {
			doc = UXml.retDocument(path);

		} catch (Exception e) {
			System.out.println(e.getMessage());
			return;
		}
		// 先放到文件检查列表中，用于以后检测
		UFileCheck.fileChanged(path);

		NodeList nl = doc.getElementsByTagName("Sql");
		MList refs = new MList();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			MTable item = new MTable();
			for (int k = 0; k < node.getAttributes().getLength(); k++) {
				String key = node.getAttributes().item(k).getNodeName();
				String val = node.getAttributes().item(k).getNodeValue();
				if (key.equalsIgnoreCase("name")) {
					SQLS.put(val.toUpperCase(), item);
				}
				item.put(key.toUpperCase(), val);
			}
			item.add("SQL", node.getTextContent());
			if (item.get("REF") != null
					&& item.get("REF").toString().trim().length() > 0) {
				refs.add(item);
			}
		}

		// 进行REF的设置
		for (int i = 0; i < refs.size(); i++) {
			MTable item = (MTable) refs.get(i);
			String ref = item.get("REF").toString().trim();

			// 找到REF对象
			MTable refItem = (MTable) get1(ref);
			if (refItem == null) {
				continue;
			}

			String sql = refItem.get("SQL").toString();
			String fieldValue = refItem.get("FieldValue".toUpperCase())
					.toString();
			String fieldDisplay = refItem.get("FieldDisplay".toUpperCase())
					.toString();

			// 设置当前对象
			item.add("SQL", sql);
			item.add("FIELDVALUE", fieldValue);
			item.add("FIELDDISPLAY", fieldDisplay);
		}

	}

	private static void getInstance() {
		if (SQLS == null) {
			init();
		} else {
			String path = UPath.getScriptPath() + "/" + PATH;
			if (UFileCheck.fileChanged(path)) { // 文件发生变化了
				// 重新初始化
				init();
			}
		}
	}

	/**
	 * 内部调用，不进行初始化检查
	 * 
	 * @param name
	 * @return
	 */
	private static MTable get1(String name) {
		String n = name.trim().toUpperCase();
		if (SQLS.containsKey(n)) {
			MTable item = (MTable) SQLS.get(n);
			return item;
		}
		return null;
	}

	/**
	 * 外部调用，检查初始化状态
	 * 
	 * @param name
	 * @return
	 */
	public static MTable get(String name) {
		getInstance();
		return get1(name);
	}

	/**
	 * 是否是Common data 表达式, 例如： {cd: ADM_USER}
	 * 
	 * @param cdExp
	 * @return
	 */
	public static boolean isCommonDataExp(String cdExp) {
		String sql1 = cdExp.trim().toLowerCase();
		if (sql1.startsWith("{cd") && sql1.endsWith("}")) {
			return true;
		} else {
			return false;
		}
	}
	/**
	 * 获取 Common Data 的名称
	 * @param exp 表达式
	 * @return
	 */
	public static String getCommonDataName(String exp){
		String sql1 = exp.toLowerCase().trim();
		String cd = sql1.replace("{cd", "").replace("}", "").replace(":", "")
				.trim();
		return cd;
	}
	
	
	public CommonSqls() {

	}

	/**
	 * 执行CommonData
	 * 
	 * @param cdExp
	 * @param cnn
	 * @return
	 */
	public MList executeCommonData(String cdExp, DataConnection cnn) {
		String cd = getCommonDataName(cdExp);
		return this.executeSelect(cd, cnn);
	}

	
	

	
	/**
	 * 执行SELECT，返回DTTABLE，如果为空，则表示出错了
	 * 
	 * @param name
	 * @param cnn
	 * @return
	 */
	public MList executeSelect(String name, DataConnection cnn) {
		MTable tb = get(name);
		String paras = tb.get("PARAS").toString();
		String sql = tb.get("SQL").toString();
		RequestValue rv = cnn.getRequestValue();
		if (paras != null && paras.toString().length() > 0) {
			String[] ps = paras.split(",");
			MListStr al = Utils.getParameters(sql, "@");
			for (int i = 0; i < al.size(); i++) {
				String newParaName = al.get(i);
				String val = ps[i].trim();
				if (val.startsWith("@")) {
					val = rv.getString(val.replace("@", ""));
				}
				rv.addValue(newParaName, val);
			}
		}
		DTTable table = DTTable.getJdbcTable(sql, cnn);
		if (table.isOk()) {
			MList lst = new MList();
			lst.add(table);
			lst.add(tb.get("FIELDVALUE").toString());
			lst.add(tb.get("FIELDDISPLAY").toString());
			return lst;
		} else {
			return null;
		}
	}
}
