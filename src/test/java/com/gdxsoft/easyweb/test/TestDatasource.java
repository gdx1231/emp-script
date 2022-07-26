package com.gdxsoft.easyweb.test;


import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;

public class TestDatasource extends TestBase {

	public static void main(String[] args) {
		TestDatasource t = new TestDatasource();
		try {
			t.initConnPools();
		} catch (Exception e) {
			e.printStackTrace();
		}
		t.test1("b2b");
		t.test1("pf");
	}

	public void test1(String datasourceName) {
		System.out.println(datasourceName);
		String sql = "select adm_id,adm_name from adm_user where 1=1 order by adm_id desc";
		DTTable tb = DTTable.getJdbcTable(sql, "adm_id", 4, 1, datasourceName);

		for (int i = 0; i < tb.getRows().getCount(); i++) {
			DTRow r = tb.getRow(i);
			System.out.println(r.toJson());
		}
	}
	
}
