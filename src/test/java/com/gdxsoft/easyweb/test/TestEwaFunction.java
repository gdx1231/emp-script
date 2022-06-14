package com.gdxsoft.easyweb.test;

import com.gdxsoft.easyweb.conf.ConfSecurities;
import com.gdxsoft.easyweb.function.EwaFunctions;
import com.gdxsoft.easyweb.utils.UAes;

public class TestEwaFunction extends TestBase{

	public static void main(String... strings) throws Exception {

		TestEwaFunction t = new TestEwaFunction();
		t.test();
	}

	public void test() throws Exception {
		ConfSecurities.getInstance();
		
		Object result4 = EwaFunctions.executeStaticFunction("md5", "aaa");
		System.out.println("md5:" + result4);

		Object result4a = EwaFunctions.executeStaticFunction("md5", "aaa".getBytes());
		System.out.println("md5:" + result4a);
		
		String result = UAes.defaultEncrypt("abc");
		System.out.println(result);
		
		// 加密
		Object resultA = EwaFunctions.executeStaticFunction("encrypt", "aaaa");
		System.out.println(resultA);
		// 解密
		Object resultB = EwaFunctions.executeStaticFunction("decrypt", resultA.toString());
		System.out.println(resultB);
		
		Object result0 = EwaFunctions.executeStaticFunction("password_hash", "aaaa");
		System.out.println(result0);

		Object result2 = EwaFunctions.executeStaticFunction("PASSWORD_VERIFY", "aaaa", result0);
		System.out.println(result2);

		Object result3 = EwaFunctions.executeStaticFunction("sha1", "aaa");
		System.out.println("sha1:" + result3);

		Object result3a = EwaFunctions.executeStaticFunction("sha1", "aaa".getBytes());
		System.out.println("sha1:" + result3a);

		Object result31 = EwaFunctions.executeStaticFunction("sha1", "bbb");
		System.out.println("sha1:" + result31);

		Object result31a = EwaFunctions.executeStaticFunction("sha1", "bbb".getBytes());
		System.out.println("sha1:" + result31a);

	

		Object result5 = EwaFunctions.executeFunction("http_get", null, "https://ip.gezz.cn");
		System.out.println("http_get:" + result5);

		for (int i = 0; i < 10; i++) {
			Object result6 = EwaFunctions.executeFunction("snowflake", null);
			System.out.println("snowflake:" + result6);
		}
	}
}
