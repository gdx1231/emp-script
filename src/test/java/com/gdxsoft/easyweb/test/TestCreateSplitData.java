package com.gdxsoft.easyweb.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.gdxsoft.easyweb.datasource.CreateSplitData;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.datasource.EwaSqlFunctions;
import com.gdxsoft.easyweb.script.RequestValue;

public class TestCreateSplitData extends TestBase {
	public static void main(String[] args) {

		new TestCreateSplitData().test();

	}

	@Test
	public void test() {
		testSqlFunctions();
		// testSqlSplitData();
	}

	public void testSqlSplitData() {
		RequestValue rv = new RequestValue();
		rv.addValue("s1", "1,2,3");
		rv.addValue("s2", "a,b,c");

		CreateSplitData p = new CreateSplitData(rv);
		String sql = "insert into a(v1,v2) \nselect a.col,b.col from (select * from ewa_split(@s1,',')) a \n"
				+ "inner join (select * from ewa_split ( @s2,',')) b on a.idx=b.idx";
		String rst = p.replaceSplitData(sql);

		System.out.println(rst);
	}

	public void testSqlFunctions() {
		RequestValue rv = new RequestValue();
		rv.addOrUpdateValue("new_password", "xsd92342834jdkjcjvdfuh");
		rv.addOrUpdateValue("lid", "guolei@sina1.com");
		String sql1 = "update web_user set usr_pwd = ewa_func.password_hash(@new_password), USR_COMPANY = ewa_func.http_get().(https://ip.gezz.cn) where usr_id=2;";
		String sql2 = "select usr_pwd from web_user where usr_lid=@lid;";
		String sql3 = "select usr_id, usr_name, usr_lid from web_user where usr_lid=@lid \n  and ewa_func.password_verify(@new_password, @usr_pwd) ='true';";
		StringBuilder sb = new StringBuilder(sql1);
		sb.append("\n");
		sb.append(sql2);
		sb.append("\n");
		sb.append(sql3);
		super.printCaption("source");
		System.out.println(sb.toString());

		EwaSqlFunctions esf = new EwaSqlFunctions();

		String sqlf = esf.extractEwaSqlFunctions(sb.toString());
		super.printCaption("after");
		System.out.println(sqlf);

		List<String> sqls  = new ArrayList<>();
		sqls.add(sql1);
		sqls.add(sql2);
		sqls.add(sql3);
		
		DataConnection.updateBatchAndCloseTransaction(sqls, "", rv);
		
	}
}
