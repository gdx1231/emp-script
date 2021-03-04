package com.gdxsoft.easyweb.script.userConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.define.ConfigUtils;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.Utils;

public class JdbcConfig {
	private static Logger LOGER = LoggerFactory.getLogger(JdbcConfig.class);

	/**
	 * 导入文件
	 * 
	 * @param xml
	 * @param xmlname
	 * @throws Exception
	 */
	public static void importXml(File xml, String xmlname) throws Exception {
		String xmlStr = UFile.readFileText(xml.getAbsolutePath());
		Document doc = UXml.asDocument(xmlStr);

		xmlname = UserConfig.filterXmlNameByJdbc(xmlname);

		String cdate = Utils.getDateTimeString(new Date(xml.lastModified()));

		importXml(xmlStr, xmlname, cdate, cdate);

		NodeList nl = doc.getElementsByTagName("EasyWebTemplate");
		for (int i = 0; i < nl.getLength(); i++) {
			Element ele = (Element) nl.item(i);
			importXmlEle(ele, xmlname);
		}
	}

	/**
	 * 导入整个配置文件
	 * 
	 * @param xmlStr
	 * @param xmlname
	 * @param cdate
	 * @param udate
	 */
	public static void importXml(String xmlStr, String xmlname, String cdate, String udate) {
		JdbcConfig.updateItem(xmlname, "", xmlStr, "");

	}

	/**
	 * 导入配置
	 * 
	 * @param ele
	 * @param xmlname
	 */
	public static void importXmlEle(Element ele, String xmlname) {
		String name = ele.getAttribute("Name");
		JdbcConfig.updateItem(xmlname, name, UXml.asXml(ele), "");
	}

	/**
	 * 获取其它配置的值
	 * 
	 * @param tag
	 * @return
	 */
	public static String getOth(String tag) {
		if (tag == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("select * from EWA_CFG_OTH where oth_tag='");
		sb.append(tag.replace("'", "''"));
		sb.append("'");
		String sql = sb.toString();
		DTTable tb = getJdbcTable(sql);

		if (tb.getCount() == 0) {
			return null;
		} else {
			try {
				return tb.getCell(0, "OTH_TXT").toString();
			} catch (Exception e) {
				return e.getMessage();
			}
		}
	}

	/**
	 * 更新其它数据
	 * 
	 * @param tag
	 * @param txt
	 * @param admId
	 */
	public static void updateOth(String tag, String txt, String admId) {
		RequestValue rv = new RequestValue();
		rv.addValue("OTH_TAG", tag);
		rv.addValue("OTH_TXT", txt);
		rv.addValue("ADM_LID", admId);

		StringBuilder sb = new StringBuilder();
		sb.append("select 1 from EWA_CFG_OTH  where oth_tag='");
		sb.append(tag.replace("'", "''"));
		sb.append("'");
		String sql0 = sb.toString();
		DTTable tb = getJdbcTable(sql0);
		String sql;

		if (tb.getCount() == 0) {
			sql = "INSERT INTO EWA_CFG_OTH (OTH_TAG, OTH_TXT, OTH_CDATE, ADM_LID) VALUES(@OTH_TAG, @OTH_TXT, @sys_DATE, @ADM_LID)";
		} else {
			sql = "UPDATE EWA_CFG_OTH SET OTH_TXT=@OTH_TXT, OTH_MDATE=@sys_date, ADM_LID=@ADM_LID WHERE OTH_TAG=@OTH_TAG";
		}
		update(sql, rv);

	}

	/**
	 * 删除备份
	 * 
	 * @param xmlname
	 * @return
	 */
	public static int deleteBaks(String xmlname) {
		String xmlName = UserConfig.filterXmlNameByJdbc(xmlname);
		StringBuilder sb = new StringBuilder();
		sb.append("select count(*) from ewa_cfg_his where xmlname like '");
		sb.append(xmlName.replace("'", "''"));
		sb.append("%'");
		String sql1 = sb.toString();
		DTTable tb = getJdbcTable(sql1);
		int count = tb.getCell(0, 0).toInt();
		if (count > 0) {
			StringBuilder sb2 = new StringBuilder();
			sb2.append("delete from ewa_cfg_his where xmlname like '");
			sb2.append(xmlName.replace("'", "''"));
			sb2.append("%'");
			String sql2 = sb2.toString();
			update(sql2, null);
		}
		return count;
	}

	/**
	 * 创建空白xml配置
	 * 
	 * @param fileName
	 * @param admId
	 */
	public static void createXml(String fileName, String admId) {
		String xmlName = UserConfig.filterXmlNameByJdbc(fileName);
		String sql = "insert into EWA_CFG_TREE (XMLNAME) values( @XMLNAME)";

		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO EWA_CFG(XMLNAME, ITEMNAME, XMLDATA, HASH_CODE, ADM_LID, CREATE_DATE)");
		sb.append("VALUES(@XMLNAME, '', @xmldata, 0, @ADM_ID,@SYS_DATE)");
		// 创建一个空白的项目，用于标记ewa_cfg_tree为文件
		String sql1 = sb.toString();

		List<String> sqls = new ArrayList<String>();
		sqls.add(sql);
		sqls.add(sql1);

		RequestValue rv = new RequestValue();
		rv.addValue("XMLNAME", xmlName);
		rv.addValue("ADM_ID", admId);
		rv.addValue("XMLDATA", ConfigUtils.XML_ROOT);

		updates(sqls, rv);
	}

	/**
	 * 创建目录
	 * 
	 * @param fileName
	 */
	public static void createPath(String fileName) {
		String xmlName = UserConfig.filterXmlNameByJdbc(fileName);
		String sql = "insert into EWA_CFG_TREE (XMLNAME) values( @XMLNAME)";

		List<String> sqls = new ArrayList<String>();
		sqls.add(sql);
		RequestValue rv = new RequestValue();
		rv.addValue("XMLNAME", xmlName);

		updates(sqls, rv);
	}

	/**
	 * 复制XML配置
	 * 
	 * @param from
	 * @param to
	 * @param admId
	 */
	public static void copyXml(String from, String to, String admId) {
		String fromXmlName = UserConfig.filterXmlNameByJdbc(from);
		String toXmlName = UserConfig.filterXmlNameByJdbc(to);

		String sql = "insert into EWA_CFG_TREE (XMLNAME) values( @TO_XMLNAME)";

		StringBuilder sb = new StringBuilder();
		sb.append(
				"INSERT INTO EWA_CFG(XMLNAME, ITEMNAME, XMLDATA, HASH_CODE, ADM_LID, CREATE_DATE, UPDATE_DATE, DATASOURCE, MD5, CLASS_ACL, CLASS_LOG, DESCRIPTION) \n");
		sb.append(
				" SELECT @TO_XMLNAME, ITEMNAME, XMLDATA, HASH_CODE, @ADM_ID, @SYS_DATE, null , DATASOURCE, MD5, CLASS_ACL, CLASS_LOG, DESCRIPTION \n");
		sb.append(" FROM EWA_CFG WHERE xmlname = @FROM_XMLNAME");
		String sql1 = sb.toString();

		List<String> sqls = new ArrayList<String>();
		sqls.add(sql);
		sqls.add(sql1);

		RequestValue rv = new RequestValue();
		rv.addValue("TO_XMLNAME", toXmlName);
		rv.addValue("FROM_XMLNAME", fromXmlName);
		rv.addValue("ADM_ID", admId);
		updates(sqls, rv);

	}

	/**
	 * 修改目录的名称
	 * 
	 * @param path
	 * @param newName
	 */
	public static void renameTree(String path, String newName) {
		String xmlName = UserConfig.filterXmlNameByJdbc(path);
		StringBuilder newPath = new StringBuilder();
		String[] olds = xmlName.split("\\|");
		for (int i = 1; i < olds.length - 1; i++) {
			newPath.append("|");
			newPath.append(olds[i]);
		}
		newPath.append("|");
		newPath.append(newName);

		StringBuilder sbtree = new StringBuilder();
		sbtree.append("select * from ewa_cfg_tree where xmlName='");
		sbtree.append(xmlName.replace("'", "''"));
		sbtree.append("' or xmlName like '");
		sbtree.append(xmlName.replace("'", "''"));
		sbtree.append("%'");
		String sqlTree = sbtree.toString();
		DTTable tb = getJdbcTable(sqlTree);

		List<String> sqls = new ArrayList<String>();
		for (int i = 0; i < tb.getCount(); i++) {
			String old_xmlname = tb.getCell(i, 0).toString();
			String new_xmlname = newPath.toString() + old_xmlname.substring(xmlName.length());
			StringBuilder sb = new StringBuilder();
			sb.append("update ewa_cfg_tree set xmlName='");
			sb.append(new_xmlname.replace("'", "''"));
			sb.append("' where xmlName='");
			sb.append(old_xmlname.replace("'", "''"));
			sb.append("'");
			String sql = sb.toString();

			sqls.add(sql);

			StringBuilder sb1 = new StringBuilder();
			sb1.append("update ewa_cfg set xmlName='");
			sb1.append(new_xmlname.replace("'", "''"));
			sb1.append("' where xmlName='");
			sb1.append(old_xmlname.replace("'", "''"));
			sb1.append("'");
			String sql1 = sb1.toString();

			sqls.add(sql1);
		}

		updates(sqls, null);
	}

	/**
	 * 重命名
	 * 
	 * @param xmlname
	 * @param newItemName
	 * @param oldItemName
	 * @param xml
	 */
	public static void renameItem(String xmlname, String newItemName, String oldItemName, String xml) {
		String xmlName = UserConfig.filterXmlNameByJdbc(xmlname);

		backToRm(xmlName, oldItemName);

		List<String> sqls = new ArrayList<String>();

		// 更新名称
		String sql1 = "update ewa_cfg set itemname=@NEW_ITEMNAME, XMLDATA=@XMLDATA WHERE xmlname=@XMLNAME and itemname=@OLD_ITEMNAME";
		sqls.add(sql1);

		RequestValue rv = new RequestValue();
		rv.addValue("XMLNAME", xmlName);
		rv.addValue("OLD_ITEMNAME", oldItemName);
		rv.addValue("NEW_ITEMNAME", newItemName);
		rv.addValue("XMLDATA", xml);
		updates(sqls, rv);

	}

	/**
	 * 备份到 rm表中
	 * 
	 * @param xmlname
	 * @param itemname
	 */
	public static void backToRm(String xmlname, String itemname) {
		String xmlName = UserConfig.filterXmlNameByJdbc(xmlname);

		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO EWA_CFG_RM (RM_DATE, XMLNAME, ITEMNAME, XMLDATA, HASH_CODE \n");
		sb.append(", ADM_LID, CREATE_DATE, UPDATE_DATE, DATASOURCE, MD5, CLASS_ACL, CLASS_LOG, DESCRIPTION) \n");
		sb.append(" SELECT @SYS_DATE, XMLNAME, ITEMNAME, XMLDATA, HASH_CODE \n");
		sb.append(", ADM_LID, CREATE_DATE, UPDATE_DATE , DATASOURCE, MD5, CLASS_ACL, CLASS_LOG, DESCRIPTION \n");
		sb.append(" FROM EWA_CFG WHERE xmlname = @XMLNAME and itemname = @ITEMNAME");
		RequestValue rv = new RequestValue();
		rv.addValue("XMLNAME", xmlName);
		rv.addValue("ITEMNAME", itemname);

		update(sb.toString(), rv);
	}

	/**
	 * 删除配置
	 * 
	 * @param xmlname
	 * @param itemname
	 */
	public static void removeItem(String xmlname, String itemname) {
		String xmlName = UserConfig.filterXmlNameByJdbc(xmlname);

		backToRm(xmlName, itemname);

		List<String> sqls = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();

		// 备份到 rm表中
		String sql = sb.toString();
		sqls.add(sql);

		// 删除
		String sql1 = "delete from ewa_cfg WHERE xmlname=@XMLNAME and itemname=@ITEMNAME";
		sqls.add(sql1);

		// 删除
		if (itemname.equals("")) {
			String sql2 = "delete from ewa_cfg_tree WHERE xmlname=@XMLNAME";
			sqls.add(sql2);
		}
		RequestValue rv = new RequestValue();
		rv.addValue("XMLNAME", xmlName);
		rv.addValue("ITEMNAME", itemname);

		updates(sqls, rv);
	}

	/**
	 * 备份配置
	 * 
	 * @param xmlname
	 * @param itemname
	 */
	public static void saveBackup(String xmlname, String itemname) {
		String xmlName = UserConfig.filterXmlNameByJdbc(xmlname);
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO EWA_CFG_HIS (HIS_DATE, XMLNAME, ITEMNAME, XMLDATA, HASH_CODE \n");
		sb.append(", ADM_LID, CREATE_DATE, UPDATE_DATE, DATASOURCE, MD5, CLASS_ACL, CLASS_LOG, DESCRIPTION) \n");
		sb.append(" SELECT @SYS_DATE, XMLNAME, ITEMNAME, XMLDATA, HASH_CODE \n");
		sb.append(", ADM_LID, CREATE_DATE, UPDATE_DATE , DATASOURCE, MD5, CLASS_ACL, CLASS_LOG, DESCRIPTION \n");
		sb.append(" FROM EWA_CFG WHERE xmlname = @XMLNAME and itemname = @ITEMNAME");
		String sql = sb.toString();

		RequestValue rv = new RequestValue();
		rv.addValue("XMLNAME", xmlName);
		rv.addValue("ITEMNAME", itemname);

		update(sql, rv);
	}

	/**
	 * 获取配置文件的概要信息
	 * 
	 * @param xmlname
	 * @return
	 */
	public static DTTable getXmlMeta(String xmlname) {
		return getXmlMeta(xmlname, null);
	}

	/**
	 * 获取配置项的概要信息
	 * 
	 * @param xmlname
	 * @param itemname
	 * @return
	 */
	public static DTTable getXmlMeta(String xmlname, String itemname) {

		if (itemname == null) {
			itemname = "";
		}

		String xmlName = UserConfig.filterXmlNameByJdbc(xmlname);
		StringBuilder sb = new StringBuilder();
		sb.append(
				"select HASH_CODE, UPDATE_DATE, MD5, DATASOURCE, CLASS_ACL, CLASS_LOG, ADM_LID from EWA_CFG where xmlname='");
		sb.append(xmlName.replace("'", "''"));
		sb.append("' and itemname='");
		sb.append(itemname.replace("'", "''"));
		sb.append("'");
		String sql = sb.toString();

		DTTable tb = getJdbcTable(sql);

		return tb;
	}

	/**
	 * 获取 节点的 属性 valueTag = tag<br>
	 * 调用 getCfgParam(Node node, String tag, String valueTag)
	 * 
	 * @param node Xml Node对象
	 * @param tag  节点tag名称，例如 DataSource, Acl ... <br>
	 * 
	 * @return
	 */
	public static String getCfgParam(Node node, String tag) {
		return getCfgParam(node, tag, tag);
	}

	/**
	 * 获取 节点的 属性
	 * 
	 * @param node     Xml Node对象
	 * @param tag      节点tag名称，例如 DataSource, Acl ...
	 * @param valueTag Set 的属性名
	 * @return
	 */
	public static String getCfgParam(Node node, String tag, String valueTag) {
		// <DataSource>
		// <Set DataSource="globaltravel"/>
		// </DataSource>

		// <DescriptionSet>
		// <Set Info="签证模板文件参数修改" Lang="zhcn" Memo=""/>
		// <Set Info="Visa template file parameters to modify" Lang="enus" Memo=""/>
		// </DescriptionSet>
		Element ele = (Element) node;

		NodeList nl = ele.getElementsByTagName(tag);
		if (nl.getLength() == 0) {
			return null;
		}

		Element e1 = (Element) nl.item(0);
		NodeList sets = e1.getElementsByTagName("Set");
		if (sets.getLength() == 0) {
			return null;
		}

		Element e2 = (Element) sets.item(0);

		if (e2.hasAttribute(valueTag)) {
			return e2.getAttribute(valueTag);
		} else {
			return null;
		}

	}

	/**
	 * 更新配置项
	 * 
	 * @param xmlname
	 * @param itemname
	 * @param xmlStr
	 * @param adm
	 * @param hashCode
	 * @param md5
	 */
	public static void updateItem(String xmlname, String itemname, String xmlStr, String adm, int hashCode,
			String md5) {
		String xmlName = UserConfig.filterXmlNameByJdbc(xmlname);

		DTTable tb = getXmlMeta(xmlname, itemname);

		RequestValue rv = new RequestValue();

		rv.addValue("XMLDATA", xmlStr);
		rv.addValue("md5", md5);
		rv.addValue("hashCode", hashCode);
		rv.addValue("ADM_LID", adm);
		rv.addValue("XMLNAME", xmlName);
		rv.addValue("ITEMNAME", itemname);

		if (itemname.length() > 0) {
			Node node = UXml.asNode(xmlStr);
			String dataSource = getCfgParam(node, "DataSource");
			String acl = getCfgParam(node, "Acl");
			String log = getCfgParam(node, "Log");
			String description = getCfgParam(node, "DescriptionSet", "Info");

			rv.addValue("DATASOURCE", dataSource); // 数据源
			rv.addValue("CLASS_ACL", acl); // 权限类
			rv.addValue("CLASS_LOG", log); // 日志类
			rv.addValue("DESCRIPTION", description); // 描述
		}
		adm = (adm == null ? "" : adm);
		if (tb.getCount() == 0) {
			StringBuilder sb1 = new StringBuilder();
			sb1.append("INSERT INTO EWA_CFG (XMLNAME, ITEMNAME, XMLDATA, DATASOURCE, MD5, CLASS_ACL, CLASS_LOG \n");
			sb1.append(", HASH_CODE, ADM_LID, CREATE_DATE, DESCRIPTION) \n");
			sb1.append(" VALUES(@XMLNAME, @ITEMNAME, @XMLDATA, @DATASOURCE, @MD5, @CLASS_ACL, @CLASS_LOG \n");
			sb1.append(", @hashCode, @ADM_LID, @sys_date, @DESCRIPTION)");

			String sql = sb1.toString();
			update(sql, rv);
			LOGER.info("NEW: " + xmlName + ", " + itemname + ", " + hashCode + ", " + md5);
		} else if (tb.getCell(0, 0).toInt() != hashCode) {
			StringBuilder sb2 = new StringBuilder();
			sb2.append("update EWA_CFG set ");
			sb2.append("  XMLDATA		= @XMLDATA");
			sb2.append(", UPDATE_DATE	= @sys_date \n");
			sb2.append(", ADM_LID		= @ADM_LID \n");
			sb2.append(", HASH_CODE		= @hashCode  \n");
			sb2.append(", DATASOURCE	= @DATASOURCE \n");
			sb2.append(", MD5			= @MD5 \n");
			sb2.append(", CLASS_ACL		= @CLASS_ACL \n");
			sb2.append(", CLASS_LOG 	= @CLASS_LOG \n");
			sb2.append(", DESCRIPTION 	= @DESCRIPTION \n");
			sb2.append("  where xmlname = @XMLNAME and itemname = @ITEMNAME");
			String sql = sb2.toString();
			update(sql, rv);
			LOGER.info("UPDATE: " + xmlName + ", " + itemname + ", " + hashCode + ", " + md5);
		}

	}

	/**
	 * 更新配置项
	 * 
	 * @param xmlname
	 * @param itemname
	 * @param xmlStr
	 * @param adm
	 */
	@Deprecated
	public static void updateItem(String xmlname, String itemname, String xmlStr, String adm) {
		int hashCode = xmlStr.hashCode();
		String md5 = Utils.md5(xmlStr);
		updateItem(xmlname, itemname, xmlStr, adm, hashCode, md5);

	}

	/**
	 * 获取整个文档的xml
	 * 
	 * @param xmlname
	 * @return
	 */
	public static String getDocXml(String xmlname) {
		String xmlName = UserConfig.filterXmlNameByJdbc(xmlname);
		StringBuilder sb1 = new StringBuilder();
		sb1.append("select * from EWA_CFG where xmlname='");
		sb1.append(xmlName.replace("'", "''"));
		sb1.append("' and itemname!='' order by case when UPDATE_DATE is null then CREATE_DATE else UPDATE_DATE end");
		String sql = sb1.toString();
		DTTable tb = getJdbcTable(sql);
		if (tb.getCount() == 0) {
			return null;
		}

		// <?xml version=\"1.0\" encoding=\"UTF-8\"?><EasyWebTemplates />
		StringBuilder sb = new StringBuilder(
				ConfigUtils.XML_ROOT.replace("<EasyWebTemplates />", "<EasyWebTemplates>"));
		for (int i = 0; i < tb.getCount(); i++) {
			try {
				String xml = tb.getCell(i, "XMLDATA").toString();
				sb.append(xml);
			} catch (Exception e) {
				LOGER.error(e.getLocalizedMessage());
			}
		}
		sb.append("</EasyWebTemplates>");
		String xml = sb.toString();

		return xml;
	}

	/**
	 * 合并成一个文件的xml
	 * 
	 * @param xmlname
	 */
	public static int combine2OneXml(String xmlname) {
		String xml = getDocXml(xmlname);
		if (xml == null) {
			return 0;
		}

		String xmlName = UserConfig.filterXmlNameByJdbc(xmlname);
		int hashCode = xml.hashCode();
		String md5 = Utils.md5(xml);

		updateItem(xmlname, "", xml, "", hashCode, md5);

		if (checkXmlChanged(xmlName, hashCode, md5)) {
			// 保存到本地 缓存
			saveToCache(xmlName, xml, -1, hashCode, md5);
		}
		return hashCode;
	}

	/**
	 * 保存到本地缓存
	 * 
	 * @param xmlName
	 * @param xml
	 * @param time
	 * @param hashCode
	 * @param md5
	 * @return
	 */
	public static boolean saveToCache(String xmlName, String xml, long time, int hashCode, String md5) {
		// 保存到本地 缓存
		String cahced_file_name = UPath.getCachedPath() + "/scripts_cached/" + xmlName.replace("|", "/");

		JSONObject obj = new JSONObject();
		obj.put("HASH", hashCode);
		obj.put("XMLNAME", xmlName);
		obj.put("DATE", new Date());
		obj.put("MD5", md5);

		try {
			UFile.createNewTextFile(cahced_file_name, xml);
			File f1 = new File(cahced_file_name);
			if (time > 0) {
				f1.setLastModified(time);
			}
			obj.put("FILE_LENGTH", f1.length());
			obj.put("LAST_MODIFIED", f1.lastModified());

			UFile.createNewTextFile(cahced_file_name + ".json", obj.toString());
			return true;
		} catch (IOException e) {
			LOGER.error(e.getLocalizedMessage());

			return false;
		}

	}

	/**
	 * 保存到本地缓存
	 * 
	 * @param xmlName
	 * @param xml
	 * @param time
	 * @return
	 * 
	 */
	@Deprecated
	public static boolean saveToCache(String xmlName, String xml, long time) {
		int code = xml.hashCode();
		return saveToCache(xmlName, xmlName, time, code, null);

	}

	/**
	 * 检查文件是否被修改
	 * 
	 * @param xmlName
	 * @param hashCode
	 * @return
	 */
	@Deprecated
	public static boolean checkXmlChanged(String xmlName, int hashCode) {
		return checkXmlChanged(xmlName, hashCode, null);
	}

	/**
	 * 检查文件是否被修改
	 * 
	 * @param xmlName
	 * @param hashCode
	 * @return
	 */
	public static boolean checkXmlChanged(String xmlName, int hashCode, String md5) {
		// if (xmlName.equals("|ow_web|web_fly.xml")) {
		// int a = 1;
		// a++;
		// }
		String name = UPath.getCachedPath() + "/scripts_cached/" + xmlName.replace("|", "/");
		String json_name = name + ".json";
		File xmlFile = new File(name);
		File jsonFile = new File(json_name);
		if (!xmlFile.exists() || !jsonFile.exists()) {
			return true;
		}
		try {
			String json = UFile.readFileText(json_name);
			JSONObject obj = new JSONObject(json);
			long FILE_LENGTH = obj.optLong("FILE_LENGTH");
			long LAST_MODIFIED = obj.optLong("LAST_MODIFIED");
			int HASH = obj.optInt("HASH");
			String MD5 = obj.optString("MD5");
			if (xmlFile.length() == FILE_LENGTH && xmlFile.lastModified() == LAST_MODIFIED) {
				if (md5 != null) {
					if (MD5.equals(md5)) {
						return false;
					} else {
						return true;
					}
				}
				if (HASH == hashCode) {
					return false;
				}
			}
		} catch (Exception e) {
			LOGER.error(e.getLocalizedMessage());

		}
		return true;
	}

	/**
	 * 输出所有配置到缓存目录 (UPath.getCachedPath )
	 * 
	 * @throws Exception
	 */
	public static void exportAll() throws Exception {
		DTTable tb = getAllXmlnames();
		for (int i = 0; i < tb.getCount(); i++) {
			int hashCode = tb.getCell(i, "HASH_CODE").toInt();
			String xmlName = tb.getCell(i, "XMLNAME").toString();
			long CREATE_DATE = tb.getCell(i, "CREATE_DATE").toTime();
			long UPDATE_DATE = tb.getCell(i, "UPDATE_DATE").toTime();
			String md5 = tb.getCell(i, "MD5").toString();

			// 文件不存在或变化了
			if (checkXmlChanged(xmlName, hashCode, md5)) {
				LOGER.info(xmlName);
				String xmlData = getXml(xmlName);
				if (xmlData == null) {
					continue;
				}
				/*
				 * ??? int xmlhashCode = xmlData.hashCode(); if (xmlhashCode != hashCode) {
				 * String sqlup = "update EWA_CFG set HASH_CODE=" + xmlhashCode +
				 * " where  ITEMNAME='' and xmlname='" + xmlName.replace("'", "''") + "'";
				 * update(sqlup, null); }
				 */
				saveToCache(xmlName, xmlData, UPDATE_DATE == -1 ? CREATE_DATE : UPDATE_DATE, hashCode, md5);
			}
		}
	}

	/**
	 * 获取所有的 xmlname名称
	 * 
	 * @return
	 */
	public static DTTable getAllXmlnames() {
		String sql = "select XMLNAME, HASH_CODE,CREATE_DATE,UPDATE_DATE,MD5 from EWA_CFG where ITEMNAME='' order by XMLNAME ";
		DTTable tb = getJdbcTable(sql);

		return tb;
	}

	/**
	 * 获取xmlname对应的xml
	 * 
	 * @param xmlName
	 * @return
	 */
	public static String getXml(String xmlName) {
		xmlName = UserConfig.filterXmlNameByJdbc(xmlName);

		StringBuilder sb = new StringBuilder();
		sb.append("select XMLDATA from EWA_CFG where ITEMNAME='' and xmlname='");
		sb.append(xmlName.replace("'", "''"));
		sb.append("'");
		String sql2 = sb.toString();
		DTTable tb2 = getJdbcTable(sql2);
		if (tb2.getCount() == 0) {
			return null;
		}
		String xmlData;
		try {
			xmlData = tb2.getCell(0, "XMLDATA").toString();
		} catch (Exception e) {
			xmlData = e.getMessage();
			LOGER.error(e.getLocalizedMessage());
		}
		return xmlData;
	}

	/**
	 * 获取所有目录
	 * 
	 * @return
	 */
	public static DTTable getJdbcCfgDirs() {
		StringBuilder sb = new StringBuilder();
		sb.append("select   a.XMLNAME,B.CNT from EWA_CFG_TREE A ");
		sb.append(" LEFT JOIN (SELECT XMLNAME, COUNT(*) CNT FROM EWA_CFG GROUP BY XMLNAME) B ");
		sb.append(" ON A.XMLNAME=B.XMLNAME order by XMLNAME ");
		String sql = sb.toString();
		DTTable tb = getJdbcTable(sql);
		return tb;
	}

	/**
	 * 获取items
	 * 
	 * @param xmlname
	 * @return
	 */
	public static DTTable getJdbcItems(String xmlname) {
		String xmlName = UserConfig.filterXmlNameByJdbc(xmlname);
		StringBuilder sb = new StringBuilder();
		sb.append("select itemname,xmldata from EWA_CFG where xmlname='");
		sb.append(xmlName.replace("'", "''"));
		sb.append("' and itemname!='' order by itemname ");
		String sql = sb.toString();

		return getJdbcTable(sql);

	}

	/**
	 * 获取 item
	 * 
	 * @param xmlname
	 * @param itemname
	 * @return
	 */
	public static DTTable getJdbcItem(String xmlname, String itemname) {
		String xmlName = UserConfig.filterXmlNameByJdbc(xmlname);
		StringBuilder sb = new StringBuilder();
		sb.append("select * from EWA_CFG where xmlname='");
		sb.append(xmlName.replace("'", "''"));
		sb.append("' and itemname='");
		sb.append(itemname.replace("'", "''"));
		sb.append("'");
		String sql = sb.toString();

		return getJdbcTable(sql);
	}

	/**
	 * 获取 item 的 xml字符串
	 * 
	 * @param xmlname
	 * @param itemname
	 * @return
	 */
	public static String getJdbcItemXml(String xmlname, String itemname) {
		DTTable tb = getJdbcItem(xmlname, itemname);
		if (tb.getCount() == 0) {
			return null;
		} else {
			try {
				return tb.getCell(0, "XMLDATA").toString();
			} catch (Exception e) {
				return null;
			}
		}
	}

	/**
	 * 批量更新
	 * 
	 * @param sqls
	 * @param rv
	 */
	public static void updates(List<String> sqls, RequestValue rv) {
		String configName = getJdbcConfigName();
		DataConnection.updateBatchAndClose(sqls, configName, rv);
	}

	/**
	 * 更新数据
	 * 
	 * @param sql
	 * @param rv
	 */
	public static void update(String sql, RequestValue rv) {
		String configName = getJdbcConfigName();
		DataConnection.updateAndClose(sql, configName, rv);
	}

	/**
	 * 获取表
	 * 
	 * @param sql
	 * @return
	 */
	public static DTTable getJdbcTable(String sql) {
		return getJdbcTable(sql, null);
	}

	/**
	 * 获取表
	 * 
	 * @param sql
	 * @param rv
	 * @return
	 */
	public static DTTable getJdbcTable(String sql, RequestValue rv) {
		String configName = getJdbcConfigName();
		DTTable tb = DTTable.getJdbcTable(sql, configName, rv);

		return tb;
	}

	/**
	 * 用户配置信息是在数据库保存
	 * 
	 * @return
	 */
	public static boolean isJdbcResources() {
		String path = UPath.getScriptPath();
		if (path == null) {
			return false;
		}
		return path.startsWith("jdbc:");
	}

	/**
	 * 获取用户配置信息的jdbc
	 * 
	 * @return
	 */
	public static String getJdbcConfigName() {
		if (isJdbcResources()) {
			String path = UPath.getScriptPath();
			return path.replace("jdbc:", "").trim().replace("/", "").replace("\\", "");
		} else {
			return null;
		}
	}

}
