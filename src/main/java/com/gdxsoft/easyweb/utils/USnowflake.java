package com.gdxsoft.easyweb.utils;

import com.gdxsoft.easyweb.conf.ConfShowflake;
import com.gdxsoft.easyweb.function.Snowflake;

public class USnowflake {
	private static Snowflake SF;

	/**
	 * Create a new snowflake instance
	 * 
	 * @return
	 */
	public static synchronized Snowflake getInstance() {
		if (SF == null) {
			ConfShowflake conf = ConfShowflake.getInstance();

			if (conf == null) {
				return null;
			}

			SF = new Snowflake(conf.getWorkId(), conf.getDatacenterId());
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
