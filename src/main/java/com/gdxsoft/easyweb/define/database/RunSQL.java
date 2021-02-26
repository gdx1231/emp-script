package com.gdxsoft.easyweb.define.database;

import java.util.HashMap;

import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class RunSQL {

	public HashMap<String, String> runSQL(String sql, String cnnStr) {
		HashMap<String, String> map = new HashMap<String, String>();

		DataConnection cnn = new DataConnection();
		cnn.setConfigName(cnnStr);

		String[] sqls = sql.split(";");
		for (int i = 0; i < sqls.length; i++) {
			String sql1 = sqls[i].trim();
			if (sql1.length() == 0) {
				continue;
			}
			if (sql1.toUpperCase().startsWith("SELECT")) {
				DTTable tb = DTTable.getJdbcTable(sql1, cnn);
				if (cnn.getErrorMsg() != null) {
					map.put("ERR<" + i + ">", cnn.getErrorMsg());
					break;
				} else {
					map.put("RST<" + i + ">", this.createTable(tb));
				}
			} else {
				cnn.executeUpdate(sql1);
				if (cnn.getErrorMsg() != null) {
					map.put("ERR<" + i + ">", cnn.getErrorMsg());
					break;
				}
			}

		}
		cnn.close();

		return map;
	}

	private String createTable(DTTable tb) {
		MStr s = new MStr();
		s
				.al("<table border=0 cellpadding=2 bgcolor='#cdcdcd' cellspacing=1 style='font-size:10px'><tr>");
		for (int i = 0; i < tb.getColumns().getCount(); i++) {
			s.al("<th bgcolor=darkblue height=25><nobr><b style='color:#fff'>"
					+ tb.getColumns().getColumn(i).getName()
					+ "</b></nobr></th>");
		}

		for (int i = 0; i < tb.getCount(); i++) {
			if (i > 50) {
				break;
			}
			s.al("<tr>");
			for (int ia = 0; ia < tb.getColumns().getCount(); ia++) {
				Object o = tb.getRow(i).getCell(ia).getValue();
				String v = tb.getRow(i).getCell(ia).toString();
				if (v == null) {
					v = "[null]";
				} else {
					//System.out.println(o.getClass().getName());
					if (o.getClass().getName().toUpperCase().indexOf("TIME") >= 0) {
						v = v.replace(" 00:00:00.0", "");
					}
				}
				s
						.al("<td bgcolor='white'><div title=\""
								+ Utils.textToHtml(v)
								+ "\" style='margin-top:2px;height:16px;max-width:200px;overflow:hidden'><nobr>");

				s.a(v);
				s.al("</nobr></div></td>");
			}
			s.al("</tr>");
		}
		s.a("</table>");

		return s.toString();
	}
}
