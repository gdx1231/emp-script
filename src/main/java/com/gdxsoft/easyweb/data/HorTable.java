package com.gdxsoft.easyweb.data;

import java.util.HashMap;

import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.msnet.MStr;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class HorTable {

	private DataConnection _Conn;
	private String _FromTableName;
	private DTRow _MainInfo;
	private DTTable _Fields;
	private String _RunType;
	private String _UpdateUNID;
	private String _Where;
	private MTable _UsedTableNames;

	public HorTable() {

	}

	public HorTable(String fromTableName, DataConnection conn) throws Throwable {

		loadMainInfo(fromTableName, conn);
	}

	public void loadMainInfo(String fromTableName, DataConnection conn)
			throws Throwable {
		this._FromTableName = fromTableName;
		this._Conn = conn;

		String sql = "SELECT * FROM _EWA_HOR_MAIN WHERE EWA_H_TABLE='"
				+ _FromTableName.replace("'", "''") + "'";
		DTTable tbMain = DTTable.getJdbcTable(sql, _Conn);
		this._MainInfo = tbMain.getRow(0);

		_UsedTableNames = new MTable();

		String sql1 = "SELECT * FROM _EWA_HOR_FIELD WHERE EWA_H_ID='"
				+ _MainInfo.getCell("EWA_H_ID").getString()
				+ "' ORDER BY EWA_F_ORD";
		_Fields = DTTable.getJdbcTable(sql1, this._Conn);
		for (int i = 0; i < _Fields.getCount(); i++) {
			DTRow r = _Fields.getRow(i);
			// 数据类型
			String dtType = r.getCell("EWA_F_TYPE").getString();

			// 关联数据表
			if (!_UsedTableNames.containsKey(dtType)) {
				_UsedTableNames.put(dtType, "_EWA_HOR_V" + dtType);
			}

		}

	}

	/**
	 * 加载垂直数据
	 * 
	 * @param fromTableName
	 * @param fromTable
	 * @param conn
	 * @throws Exception
	 */
	public void loadHorTable(DTTable fromTable) throws Exception {

		MStr strKeys = new MStr();
		String key = _MainInfo.getCell("EWA_H_PKS").getString();
		String[] keys = new String[1];
		keys[0] = key;

		fromTable.getColumns().setKeys(keys);
		fromTable.rebuildIndex();
		for (int i = 0; i < fromTable.getCount(); i++) {
			if (i > 0) {
				strKeys.a(",");
			}
			strKeys.a("'" + fromTable.getRow(i).getCell(key).getString() + "'");
		}

		// 添加字段
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		for (int i = 0; i < _Fields.getCount(); i++) {
			DTRow r = _Fields.getRow(i);
			DTColumn col = new DTColumn();
			// 数据类型
			String dtType = r.getCell("EWA_F_TYPE").getString();

			col.setName(r.getCell("EWA_F_NAME").getString());
			col.setDescription(r.getCell("EWA_F_DES").getString());
			col.setTypeName(dtType);
			fromTable.getColumns().addColumn(col);

			for (int k = 0; k < fromTable.getCount(); k++) {
				DTRow r1 = fromTable.getRow(k);
				DTCell cell = new DTCell();
				cell.setColumn(col);
				r1.addData(cell);
			}
			map.put(r.getCell("EWA_F_ID").getString(), col.getIndex());
		}

		for (int i = 0; i < _UsedTableNames.getCount(); i++) {
			String tbName = _UsedTableNames.getByIndex(i).toString();
			String sql = "select * from " + tbName + " where EWA_F_KEY0 IN ("
					+ strKeys.toString() + ")";
			// 根据来源表的UNID获取数据表的值
			DTTable tmp = DTTable.getJdbcTable(sql, this._Conn);
			loadHorTable(fromTable, tmp, key, map);
		}
	}

	/**
	 * 加载垂直数据
	 * 
	 * @param fromTable
	 * @param data
	 * @param key
	 * @param map
	 * @throws Exception
	 */
	private void loadHorTable(DTTable fromTable, DTTable data, String key,
			HashMap<String, Integer> map) throws Exception {
		for (int i = 0; i < data.getCount(); i++) {
			DTRow r = data.getRow(i);

			// 数据表的对应来源表的UNID
			String uid = r.getCell("EWA_F_KEY0").getString();

			// 来源表的数据行
			DTRow fromR = fromTable.getRowByKey(key, uid);
			if (fromR == null) {
				continue;
			}
			// 字段的UNID
			String fid = r.getCell("EWA_F_ID").getString();
			// 根据UNID获取字段的序号
			Integer c1 = map.get(fid);
			// 获取来源表的数据单元
			DTCell cell = fromR.getCell(c1);

			Object v = r.getCell("EWA_V").getValue();
			cell.setValue(v);
		}
	}

	/**
	 * 删除数据,原理是将不在主表中的 key值 删除
	 * 
	 * @param rv
	 * @throws Throwable
	 */
	public void delete() throws Throwable {
		String key = _MainInfo.getCell("EWA_H_PKS").getString().replace("'",
				"''");
		String hId = _MainInfo.getCell("EWA_H_ID").getString().replace("'",
				"''");
		
		String w1 = " WHERE EWA_F_ID IN (SELECT EWA_F_ID FROM _EWA_HOR_FIELD WHERE EWA_H_ID='" + hId
				+ "') AND EWA_F_KEY0 NOT IN ( SELECT " + key + " FROM "
				+ this._FromTableName + " )";
		
		for (int i = 0; i < _UsedTableNames.getCount(); i++) {
			String tbName = _UsedTableNames.getByIndex(i).toString();
			String sql = "DELETE FROM " + tbName + w1;
			this._Conn.executeUpdate(sql);
		}
	}

	private String getUnid() throws Throwable {
		RequestValue rv = this._Conn.getRequestValue();
		String unid = null;
		if (this._UpdateUNID == null || this._UpdateUNID.length() == 0) {
			String key = _MainInfo.getCell("EWA_H_PKS").getString();
			unid = rv.getString(key);
			if (unid == null && this._Where != null) {
				// 从SQL语句中取WHERE表达式，查询获取UNID
				String sql = "SELECT " + key + " FROM "
						+ _MainInfo.getCell("EWA_H_TABLE").getString()
						+ " WHERE " + this._Where;
				DTTable tmp = DTTable.getJdbcTable(sql, this._Conn);
				if (tmp != null && tmp.isOk() && tmp.getCount() > 0) {
					unid = tmp.getRow(0).getCell(0).getString();
				}
			}
		} else {
			unid = this._UpdateUNID;
			if (unid.startsWith("@")) {
				unid = rv.getString(unid.replace("@", ""));
			}
		}
		return unid;
	}

	/**
	 * 更新数据
	 * 
	 * @param fromTableName
	 * @throws Exception
	 */
	public void update() throws Exception {
		RequestValue rv = this._Conn.getRequestValue();
		String unid = null;
		try {
			unid = this.getUnid();
		} catch (Throwable e) {
			return;
		}
		if (unid == null) {
			// 空的unid，无法执行
			return;
		}

		HashMap<String, DTRow> map = new HashMap<String, DTRow>();
		for (int i = 0; i < _Fields.getCount(); i++) {
			DTRow r = _Fields.getRow(i);
			String key1 = r.getCell("EWA_F_NAME").getString().toUpperCase();
			map.put(key1, r);
		}

		// 从form中提取数据
		for (int i = 0; i < rv.getPageValues().getFormValues().getCount(); i++) {
			PageValue pv = (PageValue) rv.getPageValues().getFormValues()
					.getByIndex(i);
			this.update(pv, rv, unid, map);
		}
	}

	/**
	 * 根据每个From值更新数据
	 * 
	 * @param pv
	 * @param rv
	 * @param unid
	 * @param map
	 * @throws Exception
	 */
	private void update(PageValue pv, RequestValue rv, String unid,
			HashMap<String, DTRow> map) throws Exception {
		String fieldName = pv.getName().toUpperCase();
		// System.out.println(fieldName);
		if (!map.containsKey(fieldName)) {
			// 检查提交的FORM的字段名称是否在垂直表的定义中
			return;
		}

		// 垂直表的定义行
		DTRow r = map.get(fieldName);
		// 表名称
		String tbName = "_EWA_HOR_V" + r.getCell("EWA_F_TYPE").getString();
		// 字段UNID
		String fId = r.getCell("EWA_F_ID").getString();
		// 字段名称
		String fName = r.getCell("EWA_F_NAME").getString();
		// 字段类型
		pv.setDataType(r.getCell("EWA_F_TYPE").getString());

		// 先删除
		String sql = "DELETE FROM " + tbName + " WHERE EWA_F_ID='" + fId
				+ "' AND EWA_F_Key0='" + unid + "'";
		this._Conn.executeUpdateNoParameter(sql);

		// 插入新数据
		sql = "INSERT INTO " + tbName + "(EWA_F_ID,EWA_F_KEY0,EWA_V)values('"
				+ fId + "','" + unid + "',@" + fName + ")";
		this._Conn.executeUpdate(sql);
	}

	/**
	 * 运行模式
	 * 
	 * @return the _RunType
	 */
	public String getRunType() {
		return _RunType;
	}

	/**
	 * @param runType
	 *            the _RunType to set
	 */
	public void setRunType(String runType) {
		_RunType = runType;
	}

	/**
	 * 获取更新的 UNID
	 * 
	 * @return the _UpdateUNID
	 */
	public String getUpdateUNID() {
		return _UpdateUNID;
	}

	/**
	 * @param updateUNID
	 *            the _UpdateUNID to set
	 */
	public void setUpdateUNID(String updateUNID) {
		_UpdateUNID = updateUNID;
	}

	/**
	 * @return the _Where
	 */
	public String getWhere() {
		return _Where;
	}

	/**
	 * @param where
	 *            the _Where to set
	 */
	public void setWhere(String where) {
		_Where = where;
	}
}
