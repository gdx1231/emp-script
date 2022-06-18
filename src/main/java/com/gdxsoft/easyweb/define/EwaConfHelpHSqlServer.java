package com.gdxsoft.easyweb.define;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConnectionConfig;
import com.gdxsoft.easyweb.conf.ConnectionConfigs;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.utils.msnet.MTableStr;

public class EwaConfHelpHSqlServer {
	private static Logger LOGGER = LoggerFactory.getLogger(EwaConfHelpHSqlServer.class);

	private static EwaConfHelpHSqlServer INSTANCE;
	public static final String CONN_STR = "ewaconfhelp"; // 必须小写

	public static EwaConfHelpHSqlServer getInstance() {
		if (INSTANCE != null) {
			return INSTANCE;
		}
		
		init();
	
		int count = INSTANCE.ping();
		LOGGER.info("EwaConfHelpHSqlServer.EWA_CONF count=" + count);
		return INSTANCE;
	}

	/**
	 * 初始化
	 * 
	 * @return
	 */
	synchronized private static void init() {
		try {
			ConnectionConfigs c1 = ConnectionConfigs.instance();
			if (c1.containsKey(CONN_STR)) {
				// already defined in the ewa_conf.xml
				return;
			}

			ConnectionConfig poolCfg = new ConnectionConfig();
			poolCfg.setName(CONN_STR);
			poolCfg.setType("HSQLDB");
			poolCfg.setConnectionString(CONN_STR);
			poolCfg.setSchemaName("PUBLIC");

			String url = "jdbc:hsqldb:res:/conf.help.hsqldb/data/ewaconfhelp";

			MTableStr poolParams = new MTableStr();
			poolParams.put("driverClassName", "org.hsqldb.jdbc.JDBCDriver");
			poolParams.put("url", url);
			poolParams.put("username", "sa");
			poolParams.put("password", "");
			poolParams.put("maxActive", 10);
			poolParams.put("maxIdle", 100);

			poolCfg.setPool(poolParams);
			c1.put(CONN_STR, poolCfg);

			EwaConfHelpHSqlServer o = new EwaConfHelpHSqlServer();
			INSTANCE = o;
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
		}
	}

	public int ping() {
		String sql = "select count(*) a from EWA_CONF where 1=1";
		DTTable tb = DTTable.getJdbcTable(sql, CONN_STR);
		return tb.getCount();
	}
}
