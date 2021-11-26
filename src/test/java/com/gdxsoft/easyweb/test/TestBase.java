package com.gdxsoft.easyweb.test;

import java.io.IOException;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.gdxsoft.easyweb.conf.ConnectionConfig;
import com.gdxsoft.easyweb.conf.ConnectionConfigs;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.msnet.MTableStr;

public class TestBase {
	
	public void initConnPools() throws ParserConfigurationException, SAXException, IOException {
		this.initConnPool();
		this.initConnPool1();
		initConnPoolEwa();
		initConnPoolCm();
		
		initConnPoolVisaMainData();
		initConnPoolVisa();
		
		initConnPoolVisaEwa();
	}
	
	public void initConnPool() throws ParserConfigurationException, SAXException, IOException {
		ConnectionConfigs c1 = ConnectionConfigs.instance();
		String CONN_STR = "b2b";
		String CONN_URL = "jdbc:mysql://devmysql/b2b?useUnicode=true&characterEncoding=utf8&autoReconnect=true&useSSL=false&nullNamePatternMatchesAll=true&nullCatalogMeansCurrent=true";
		ConnectionConfig poolCfg = new ConnectionConfig();
		poolCfg.setName(CONN_STR);
		poolCfg.setType("MYSQL");
		poolCfg.setConnectionString(CONN_STR);
		poolCfg.setSchemaName("b2b");

		MTableStr poolParams = new MTableStr();
		poolParams.put("driverClassName", "com.mysql.cj.jdbc.Driver");
		poolParams.put("url", CONN_URL);
		poolParams.put("username", "root");

		String password = UFile.readFileText("d:/360Rec/mysql-test-password.txt").trim();
		poolParams.put("password", password);
		poolParams.put("maxActive", 10);
		poolParams.put("maxIdle", 100);

		poolCfg.setPool(poolParams);
		c1.put(CONN_STR, poolCfg);

		ConnectionConfig poolCfgEx = new ConnectionConfig();
		poolCfgEx.setName("ex");
		poolCfgEx.setType("MYSQL");
		poolCfgEx.setConnectionString("ex");
		poolCfgEx.setSchemaName("b2b");
		poolCfgEx.setPool(poolParams);

		c1.put("ex", poolCfgEx);
	}

	public void initConnPoolVisaMainData() throws ParserConfigurationException, SAXException, IOException {
		ConnectionConfigs c1 = ConnectionConfigs.instance();
		String CONN_STR = "visa_main";
		String CONN_URL = "jdbc:mysql://devmysql/visa_main_data?useUnicode=true&characterEncoding=utf8&autoReconnect=true&useSSL=false&nullNamePatternMatchesAll=true&nullCatalogMeansCurrent=true";
		
		ConnectionConfig poolCfg = new ConnectionConfig();
		poolCfg.setName(CONN_STR);
		poolCfg.setType("MYSQL");
		poolCfg.setConnectionString(CONN_STR);
		poolCfg.setSchemaName("visa_main_data");

		MTableStr poolParams = new MTableStr();
		poolParams.put("driverClassName", "com.mysql.cj.jdbc.Driver");
		poolParams.put("url", CONN_URL);
		poolParams.put("username", "root");

		String password = UFile.readFileText("d:/360Rec/mysql-test-password.txt").trim();
		poolParams.put("password", password);
		poolParams.put("maxActive", 10);
		poolParams.put("maxIdle", 100);

		poolCfg.setPool(poolParams);
		
		c1.put(CONN_STR, poolCfg);
	}
	
	public void initConnPoolVisa() throws ParserConfigurationException, SAXException, IOException {
		ConnectionConfigs c1 = ConnectionConfigs.instance();
		String CONN_STR = "visa";
		String CONN_URL = "jdbc:mysql://devmysql/visa?useUnicode=true&characterEncoding=utf8&autoReconnect=true&useSSL=false&nullNamePatternMatchesAll=true&nullCatalogMeansCurrent=true";
		
		ConnectionConfig poolCfg = new ConnectionConfig();
		poolCfg.setName(CONN_STR);
		poolCfg.setType("MYSQL");
		poolCfg.setConnectionString(CONN_STR);
		poolCfg.setSchemaName("visa");

		MTableStr poolParams = new MTableStr();
		poolParams.put("driverClassName", "com.mysql.cj.jdbc.Driver");
		poolParams.put("url", CONN_URL);
		poolParams.put("username", "root");

		String password = UFile.readFileText("d:/360Rec/mysql-test-password.txt").trim();
		poolParams.put("password", password);
		poolParams.put("maxActive", 10);
		poolParams.put("maxIdle", 100);

		poolCfg.setPool(poolParams);
		
		c1.put(CONN_STR, poolCfg);
	}
	
	public void initConnPoolCm() throws ParserConfigurationException, SAXException, IOException {
		ConnectionConfigs c1 = ConnectionConfigs.instance();
		String CONN_STR = "cm";
		String CONN_URL = "jdbc:mysql://devmysql/oneworld_main_data?useUnicode=true&characterEncoding=utf8&autoReconnect=true&useSSL=false&nullNamePatternMatchesAll=true&nullCatalogMeansCurrent=true";
		ConnectionConfig poolCfg = new ConnectionConfig();
		poolCfg.setName(CONN_STR);
		poolCfg.setType("MYSQL");
		poolCfg.setConnectionString(CONN_STR);
		poolCfg.setSchemaName("oneworld_main_data");

		MTableStr poolParams = new MTableStr();
		poolParams.put("driverClassName", "com.mysql.cj.jdbc.Driver");
		poolParams.put("url", CONN_URL);
		poolParams.put("username", "root");

		String password = UFile.readFileText("d:/360Rec/mysql-test-password.txt").trim();
		poolParams.put("password", password);
		poolParams.put("maxActive", 10);
		poolParams.put("maxIdle", 100);

		poolCfg.setPool(poolParams);
		c1.put(CONN_STR, poolCfg);

	}

	public void initConnPoolEwa() throws ParserConfigurationException, SAXException, IOException {
		ConnectionConfigs c1 = ConnectionConfigs.instance();
		String CONN_STR = "ewa";
		String CONN_URL = "jdbc:mysql://devmysql/ewa?useUnicode=true&characterEncoding=utf8&autoReconnect=true&useSSL=false&nullNamePatternMatchesAll=true&nullCatalogMeansCurrent=true";
		ConnectionConfig poolCfg = new ConnectionConfig();
		poolCfg.setName(CONN_STR);
		poolCfg.setType("MYSQL");
		poolCfg.setConnectionString(CONN_STR);
		poolCfg.setSchemaName("b2b");

		MTableStr poolParams = new MTableStr();
		poolParams.put("driverClassName", "com.mysql.cj.jdbc.Driver");
		poolParams.put("url", CONN_URL);
		poolParams.put("username", "root");

		String password = UFile.readFileText("d:/360Rec/mysql-test-password.txt").trim();
		poolParams.put("password", password);
		poolParams.put("maxActive", 10);
		poolParams.put("maxIdle", 100);

		poolCfg.setPool(poolParams);
		c1.put(CONN_STR, poolCfg);

	}

	public void initConnPoolVisaEwa() throws ParserConfigurationException, SAXException, IOException {
		ConnectionConfigs c1 = ConnectionConfigs.instance();
		String CONN_STR = "visa_ewa";
		String CONN_URL = "jdbc:mysql://devmysql/visa_ewa?useUnicode=true&characterEncoding=utf8&autoReconnect=true&useSSL=false&nullNamePatternMatchesAll=true&nullCatalogMeansCurrent=true";
		ConnectionConfig poolCfg = new ConnectionConfig();
		poolCfg.setName(CONN_STR);
		poolCfg.setType("MYSQL");
		poolCfg.setConnectionString(CONN_STR);
		poolCfg.setSchemaName("visa_ewa");

		MTableStr poolParams = new MTableStr();
		poolParams.put("driverClassName", "com.mysql.cj.jdbc.Driver");
		poolParams.put("url", CONN_URL);
		poolParams.put("username", "root");

		String password = UFile.readFileText("d:/360Rec/mysql-test-password.txt").trim();
		poolParams.put("password", password);
		poolParams.put("maxActive", 10);
		poolParams.put("maxIdle", 100);

		poolCfg.setPool(poolParams);
		c1.put(CONN_STR, poolCfg);

	}

	
	private void initConnPool1() throws ParserConfigurationException, SAXException, IOException {
		ConnectionConfigs c1 = ConnectionConfigs.instance();
		String CONN_STR = "pf";
		String CONN_URL = "jdbc:sqlserver://devsqlserver;DatabaseName=OneWorld;applicationName=gdx-pf";
		ConnectionConfig poolCfg = new ConnectionConfig();
		poolCfg.setName(CONN_STR);
		poolCfg.setType("MSSQL");
		poolCfg.setConnectionString(CONN_STR);
		poolCfg.setSchemaName("dbo");

		MTableStr poolParams = new MTableStr();
		poolParams.put("driverClassName", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
		poolParams.put("url", CONN_URL);
		poolParams.put("username", "sa");

		String password = UFile.readFileText("d:/360Rec/mysql-test-password.txt").trim();
		poolParams.put("password", password);
		poolParams.put("maxActive", 10);
		poolParams.put("maxIdle", 100);

		poolCfg.setPool(poolParams);
		c1.put(CONN_STR, poolCfg);

	}
	public void printCaption(String caption) {
		int width = 80;
		int capWidth = this.captionLength(caption);
		int aLen = (width - capWidth - 2) / 2;
		StringBuilder sb = new StringBuilder("\n");
		for (int i = 0; i < aLen; i++) {
			sb.append("-");
		}
		sb.append(" \033[32;1m");
		sb.append(caption);
		sb.append("\033[39;49;0m ");

		int start = capWidth + aLen + 1;
		for (int i = start; i < width; i++) {
			sb.append("-");
		}
		System.out.println(sb);
	}

	public int captionLength(String caption) {
		char[] chars = caption.toCharArray();
		int len = 0;
		for (int i = 0; i < chars.length; i++) {
			byte[] bytes = ("" + chars[i]).getBytes();
			if (bytes.length == 1) {
				len++;
			} else {
				len += 2;
			}
		}

		return len;
	}

	public static void printColor() {
		// 背景颜色代号(41-46)
		// 前景色代号(31-36)
		// 前景色代号和背景色代号可选，就是或可以写，也可以不写
		// 数字+m：1加粗；3斜体；4下划线
		// 格式：System.out.println("\33[前景色代号;背景色代号;数字m");
		Random backgroundRandom = new Random();
		Random fontRandom = new Random();
		for (int i = 1; i <= 50; i++) {
			int font = fontRandom.nextInt(6) + 31;
			int background = backgroundRandom.nextInt(6) + 41;
			System.out.format("前景色是%d,背景色是%d------\33[%d;%d;4m我是博主%n", font, background, font, background);
		}
	}
}
