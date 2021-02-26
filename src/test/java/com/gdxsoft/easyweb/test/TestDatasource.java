package com.gdxsoft.easyweb.test;

import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;

public class TestDatasource {

	public static void main(String[] args) {
		TestDatasource t= new TestDatasource();
		t.test1("el");
		t.test1("ms");
	}
	
	public void test1(String datasourceName) {
		String sql="select * from adm_user where 1=1 order by adm_id desc";
		DTTable tb = DTTable.getJdbcTable(sql, "adm_id", 10, 1, datasourceName);
		
		
		for(int i=0;i<tb.getRows().getCount();i++) {
			 DTRow r = tb.getRow(i);
			 System.out.println(r.toJson());
		}
	}
}
