package com.gdxsoft.easyweb.utils;

import com.gdxsoft.easyweb.conf.ConfShowflake;
import com.gdxsoft.easyweb.function.Snowflake;

public class USnowflake {
	/**
	 * Create a new snowflake instance
	 * 
	 * @return
	 */
	public static Snowflake newSnowflake() {
		ConfShowflake conf = ConfShowflake.getInstance();

		if (conf == null) {
			return null;
		}

		return new Snowflake(conf.getWorkId(), conf.getDatacenterId());

	}

	/**
	 * Get the snowflake's next id
	 * 
	 * @return the next id
	 */
	public static long nextId() {
		Snowflake sf = newSnowflake();
		if (sf == null) {
			return -1;
		} else {
			return sf.nextId();
		}
	}
}
