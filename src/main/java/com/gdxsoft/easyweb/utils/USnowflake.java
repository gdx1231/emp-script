package com.gdxsoft.easyweb.utils;

import com.gdxsoft.easyweb.conf.ConfShowflake;
import com.gdxsoft.easyweb.function.Snowflake;

public class USnowflake {
	private static Snowflake SF;
	/**
	 * The default workId
	 */
	public static final long DEF_WORK_ID = 31;
	/**
	 * The default datacenterId
	 */
	public static final long DEF_DATACENTER_ID = 31;

	/**
	 * Create a new snowflake instance
	 * 
	 * @return
	 */
	public static synchronized Snowflake getInstance() {
		if (SF == null) {
			ConfShowflake conf = ConfShowflake.getInstance();
			long workId = DEF_WORK_ID;  
			long datacenterId = DEF_DATACENTER_ID;
			if (conf != null) {
				workId = conf.getWorkId();
				datacenterId = conf.getDatacenterId();
			}

			SF = new Snowflake(workId, datacenterId);
		}

		return SF;
	}

	/**
	 * Get the snowflake's next id
	 * 
	 * @return the next id
	 */
	public static long nextId() {
		Snowflake sf = getInstance();
		if (sf == null) {
			return -1;
		} else {
			return sf.nextId();
		}
	}
}
