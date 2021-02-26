package com.gdxsoft.easyweb.debug;

import java.util.ArrayList;
import java.util.Date;

public class DebugRecords {
	private static DebugRecord CUR;
	private static int MAX = 10;
	private static ArrayList<DebugRecord> RECORDS = new ArrayList<DebugRecord>();
	private static boolean IS_RECORD = false;

	public synchronized static void setMax(int max) {
		MAX = max;
	}

	public synchronized static void add(DebugRecord record) {
		if (!IS_RECORD) {
			return;
		}

		if (RECORDS.size() == MAX) {
			RECORDS.remove(0);
		}

		RECORDS.add(record);
	}

	public static DebugRecord getCur() {
		return CUR;
	}

	public static ArrayList<DebugRecord> getRecords() {
		return RECORDS;
	}

	public static ArrayList<DebugRecord> getRecords(Date date) {
		ArrayList<DebugRecord> al = new ArrayList<DebugRecord>();
		for (int i = 0; i < RECORDS.size(); i++) {
			DebugRecord d = RECORDS.get(i);
			if (d.getDate().getTime() > date.getTime()) {
				al.add(d);
			}
		}
		return al;
	}

	/**
	 * @return the iS_RECORD
	 */
	public static boolean isRecord() {
		return IS_RECORD;
	}

	/**
	 * @param is_record
	 *            the iS_RECORD to set
	 */
	public synchronized static void setIsRecord(boolean is_record) {
		IS_RECORD = is_record;
	}

	public synchronized static void clear() {
		RECORDS.clear();
	}
}
