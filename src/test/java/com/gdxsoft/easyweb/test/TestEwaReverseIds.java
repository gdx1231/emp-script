package com.gdxsoft.easyweb.test;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.script.RequestValue;

public class TestEwaReverseIds extends TestBase {

	public static void main(String[] args) {
		TestEwaReverseIds t = new TestEwaReverseIds();
		try {
			t.initConnPools();
			DTTable.getJdbcTable("select 1", "pf");
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
			return;
		}

		//t.test1("pf");
		t.test2("pf");
	}

	public void test1(String datasourceName) {
		RequestValue rv = new RequestValue();
		rv.addOrUpdateValue("dep_id", 14);
		DataConnection cnn = new DataConnection(datasourceName,rv);
		String sql = "select DEP_ID, DEP_PID, DEP_NAME from adm_dept where dep_id in "
				+"(ewa_ids_sub('adm_dept','dep_id', 'dep_pid', @dep_id) )";
		DTTable tb = DTTable.getJdbcTable(sql, cnn);
		cnn.close();
		for (int i = 0; i < tb.getRows().getCount(); i++) {
			DTRow r = tb.getRow(i);
			System.out.println(r.toJson());
		}
		
	}
	public void test2(String datasourceName) {
		RequestValue rv = new RequestValue();
		rv.addOrUpdateValue("dep_id", 243);
		DataConnection cnn = new DataConnection(datasourceName,rv);
		String sql = "select DEP_ID,DEP_PID,DEP_NAME,DEP_LVL,DEP_ORD from adm_dept where dep_id in "
				+"(ewa_ids_up('adm_dept','dep_id', 'dep_pid', @dep_id)) or dep_id=@dep_id";
		DTTable tb = DTTable.getJdbcTable(sql, cnn);
		cnn.close();
		for (int i = 0; i < tb.getRows().getCount(); i++) {
			DTRow r = tb.getRow(i);
			System.out.println(r.toJson());
		}
	}
}
