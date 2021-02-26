package com.gdxsoft.easyweb.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.gdxsoft.easyweb.utils.msnet.MTable;

public class ULogic {
	private static Connection _Conn;
	private static MTable CACHE = new MTable(); // 缓存
	public static String ERR_MSG;
	private static Logger LOGGER = Logger.getLogger(ULogic.class);

	private static boolean initLogic() {
		ERR_MSG = "";
		if (_Conn == null) {
			try {
				Class.forName("org.hsqldb.jdbcDriver");
				_Conn = DriverManager.getConnection("jdbc:hsqldb:mem:.", "sa", "");

				String sqlexist = "SELECT 1 a FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='A'";

				Statement st = _Conn.createStatement();

				ResultSet rst = st.executeQuery(sqlexist);
				if (!rst.next()) {
					st.execute("create table A(a int)");
					st.execute("insert into A(a) values(1)");
				}
				rst.close();
				st.close();

				LOGGER.info("创建逻辑表 A");
			} catch (Exception e) {
				ERR_MSG = e.getMessage();
				LOGGER.error(e);
				return false;
			}
		}
		return true;
	}

	/**
	 * 执行表达式
	 * 
	 * @param exp
	 * @return
	 */
	public static boolean runLogic(String exp) {
		if (_Conn == null) {
			initLogic();
		}
		if (exp == null) {
			return false;
		}
		String exp1 = exp.trim();

		if (exp1.length() == 0) {
			return false;
		}

		if (CACHE.containsKey(exp1.hashCode())) {
			return (Boolean) CACHE.get(exp1.hashCode());
		}
		boolean rst = execExpFromJdbc(exp);
		return rst;
	}

	/**
	 * 从数据库返回表达式
	 * 
	 * @param exp
	 * @return
	 */
	private static boolean execExpFromJdbc(String exp) {
		boolean rst = false;
		Statement st = null;
		ResultSet rs = null;

		String testSql = "select count(*) from A where " + exp;
		try {
			st = _Conn.createStatement();
			rs = st.executeQuery(testSql);
			rs.next();
			if (rs.getInt(1) == 0) {
				rst = false;
			} else {
				rst = true;
			}
		} catch (SQLException e) {
			ERR_MSG = e.getMessage();
			LOGGER.error(e);
			LOGGER.error(testSql);
			rst = false;
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					ERR_MSG = e.getMessage();
					LOGGER.error(e);
				}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					ERR_MSG = e.getMessage();
					LOGGER.error(e);
				}
			}
		}
		addToCahche(exp.hashCode(), rst);
		return rst;
	}

	/**
	 * 添加表达式到缓存中
	 * 
	 * @param code
	 * @param rst
	 */
	private synchronized static void addToCahche(int code, boolean rst) {
		CACHE.put(code, rst);
	}
}
