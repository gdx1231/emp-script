package com.gdxsoft.easyweb.define.database;

import org.json.JSONException;
import org.json.JSONObject;

import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class SqlSyntaxCheck {

	private RequestValue _Rv;
	private DataConnection _Cnn;

	public SqlSyntaxCheck(RequestValue rv) {
		this._Rv = rv;
		DataConnection cnn = new DataConnection();
		cnn.setConfigName(rv.getString("DB"));
		cnn.setRequestValue(rv);

		this._Cnn = cnn;
	}

	/**
	 * 检查SQL语法
	 * @return
	 */
	public String checkSyntax() {
		try {
			if (this._Cnn.getDatabaseType().equals("MSSQL")) {
				return this.mssql();
			} else {
				return this.mysql();
			}
		} catch (Exception err) {
			JSONObject json = new JSONObject();
			json.put("RST", false);
			json.put("ERR", err.getMessage());

			return json.toString();
		} finally {
			this._Cnn.close();
		}
	}

	private String mysql() {
		String sql = this._Rv.getString("SQL");
		String[] sqls = sql.split(";");
		_Cnn.transBegin();
		for (int i = 0; i < sqls.length; i++) {
			String s = sqls[i].trim();
			if (s.length() == 0) {
				continue;
			}
			if (s.toUpperCase().indexOf("SELECT") == 0) {
				this._Cnn.executeQuery(sql);
			} else {
				this._Cnn.executeUpdate(sql);
			}
			if (_Cnn.getErrorMsg() != null) {
				String err = _Cnn.getErrorMsg();
				_Cnn.clearErrorMsg();
				_Cnn.executeUpdate("SET NOEXEC OFF;");
				JSONObject json = new JSONObject();

				json.put("ERR", err.split("<br>")[1]);
				json.put("RST", false);
				
				_Cnn.transRollback();
				_Cnn.transClose();
				
				return json.toString();

			}
		}
		_Cnn.transRollback();
		_Cnn.transClose();
		return "{RST:true}";
	}

	private String mssql() {
		String sql = this._Rv.getString("SQL");
		MStr sql1 = new MStr();
		sql1.al("");
		sql1.al("");
		sql1.al("");
		sql1.al("SET NOEXEC ON");
		sql1.al("");
		sql1.al("-- START --");
		sql1.al("");
		sql1.al(sql);
		sql1.al("-- END --");
		sql1.al("");
		sql1.al("");
		sql1.al("SET NOEXEC OFF");
		String cnt = "{}";
		_Cnn.executeUpdate(sql1.toString());
		if (_Cnn.getErrorMsg() != null) {
			String err = _Cnn.getErrorMsg();
			_Cnn.clearErrorMsg();
			_Cnn.executeUpdate("SET NOEXEC OFF;");
			JSONObject json = new JSONObject();
			try {
				json.put("ERR", err.split("<br>")[1]);
				json.put("RST", false);
				cnt = json.toString();
			} catch (JSONException e) {
				cnt = "{RST:false}";
			}
		} else {
			cnt = "{RST:true}";
		}
		_Cnn.close();
		return cnt;
	}
}
