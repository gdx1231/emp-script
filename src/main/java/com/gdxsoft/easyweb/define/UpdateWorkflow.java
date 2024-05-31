package com.gdxsoft.easyweb.define;

import java.util.HashMap;

import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.Workflow.OrgSqls;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class UpdateWorkflow {

	DataConnection getCnn(RequestValue rv) {
		String configName = rv.getString("configName");
		DataConnection cnn = new DataConnection();
		cnn.setRequestValue(rv);
		cnn.setConfigName(configName);
		return cnn;
	}

	public String updateCnns(RequestValue rv) {
		if (rv.getString("FROMS") == null || rv.getString("TOS") == null) {
			return "{\"RST\":false, \"ERR\":\"FROMS/TOS NOT EXISTS\"}";
		}
		String[] froms = rv.getString("FROMS").split(",");
		String[] tos = rv.getString("TOS").split(",");
		if (froms.length != tos.length) {
			return "{\"RST\":false, \"ERR\":\"FROMS != TOS\"}";
		}
		DataConnection cnn = this.getCnn(rv);

		String sql = "DELETE FROM _EWA_WF_CNN WHERE WF_ID=@WF_ID AND WF_REF_ID=@REF_ID";

		cnn.executeUpdate(sql);

		for (int i = 0; i < froms.length; i++) {
			sql = "INSERT INTO _EWA_WF_CNN(WF_UNIT_FROM, WF_UNIT_TO, WF_ID,WF_REF_ID) " + "VALUES('"
					+ froms[i].replace("'", "''") + "', '" + tos[i].replace("'", "''") + "', @WF_ID,@REF_ID)";
			cnn.executeUpdate(sql);
		}
		cnn.close();
		return "{\"RST\":true}";
	}

	public String updateUnits(RequestValue rv) {
		String[] ids = rv.getString("ids").split(",");
		String[] names = rv.getString("names").split(",");
		String[] types = rv.getString("types").split(",");
		String[] xs = rv.getString("xs").split(",");
		String[] ys = rv.getString("ys").split(",");
		DataConnection cnn = this.getCnn(rv);

		String sql = "DELETE FROM _EWA_WF_UNIT WHERE WF_ID=@WF_ID AND WF_REF_ID=@REF_ID AND NOT WF_UNIT_ID IN ('`'";

		for (int i = 0; i < ids.length; i++) {
			sql += ",'" + ids[i].replace("'", "''") + "'";
		}
		sql += ")";

		cnn.transBegin();
		try {
			// 删除不存在的
			cnn.executeUpdate(sql);
			if (cnn.getErrorMsg() != null) {
				throw new Exception(cnn.getErrorMsg());
			}
			sql = "SELECT WF_UNIT_ID FROM _EWA_WF_UNIT WHERE WF_ID=@WF_ID AND WF_REF_ID=@REF_ID ";
			DTTable tb = DTTable.getJdbcTable(sql, cnn);
			HashMap<String, String> map = new HashMap<String, String>();
			for (int i = 0; i < tb.getCount(); i++) {
				map.put(tb.getCell(i, 0).toString(), "");
			}

			for (int i = 0; i < ids.length; i++) {
				String id = ids[i];
				if (map.containsKey(id)) {
					sql = "UPDATE _EWA_WF_UNIT SET WF_UNIT_NAME='" + names[i].replace("'", "''") + "', WF_UNIT_X="
							+ xs[i].replace("'", "") + ",WF_UNIT_Y=" + ys[i].replace("'", "")
							+ " WHERE WF_ID=@WF_ID AND WF_REF_ID=@REF_ID AND WF_UNIT_ID='" + id.replace("'", "''")
							+ "'";
				} else {
					sql = "INSERT INTO _EWA_WF_UNIT(WF_UNIT_ID, WF_ID, WF_REF_ID, WF_UNIT_NAME, "
							+ "WF_UNIT_TYPE, WF_UNIT_X, WF_UNIT_Y)" + " VALUES ('" + ids[i].replace("'", "''")
							+ "', @WF_ID, @REF_ID, '" + names[i].replace("'", "''") + "', '"
							+ types[i].replace("'", "''") + "', " + xs[i].replace("'", "") + ", "
							+ ys[i].replace("'", "") + ")";
				}
				cnn.executeUpdate(sql);
				if (cnn.getErrorMsg() != null) {
					throw new Exception(cnn.getErrorMsg());
				}
			}
			cnn.transCommit();
		} catch (Exception err) {
			cnn.transRollback();
		} finally {
			cnn.close();
		}
		return "{\"RST\":true}";
	}

	public String getUnitsCnns(RequestValue rv) {
		DataConnection cnn = this.getCnn(rv);
		try {
			String sql = "SELECT * FROM _EWA_WF_CNN WHERE WF_ID=@WF_ID and wf_ref_id=@ref_id";
			DTTable tableCnn = DTTable.getJdbcTable(sql, cnn);

			sql = "SELECT * FROM _EWA_WF_UNIT WHERE WF_ID='" + rv.getString("WF_ID").replace("'", "''")
					+ "' and wf_ref_id='" + rv.getString("ref_id").replace("'", "''") + "'";

			DTTable tableUnit = DTTable.getJdbcTable(sql, cnn);

			OrgSqls sqls = OrgSqls.instance();

			// admin
			sql = sqls.getSql("WF_ROLE");
			DTTable tableAdmin = DTTable.getJdbcTable(sql, cnn);

			MStr str = new MStr();
			str.al("{");
			str.al("\"UNITS\":");
			str.al(tableUnit.toJson(rv));

			str.al(",\"CNNS\":");
			str.al(tableCnn.toJson(rv));

			str.al(",\"ROLES\":");
			str.al(tableAdmin.toJson(rv));

			str.al("}");
			return str.toString();

		} catch (Exception err) {
			return "{\"RST\": false, \"ERR\":'???'}";
		} finally {
			cnn.close();

		}
	}

}
