package com.gdxsoft.easyweb.utils;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

public class UFileCheck {
	private static Logger LOGGER = Logger.getLogger(UFileCheck.class);

	private static ReentrantLock LOCK = new ReentrantLock();
	private static ConcurrentHashMap<Integer, Integer> FILE_LIST = new ConcurrentHashMap<Integer, Integer>();
	private static ConcurrentHashMap<Integer, Long> PAST_TIME = new ConcurrentHashMap<Integer, Long>();

	/**
	 * 检查文件是否改变，延迟5s
	 * 
	 * @param filePath 文件路径
	 * @return
	 */
	public static boolean fileChanged(String filePath) {
		return fileChanged(filePath, 5);
	}

	/**
	 * 检查文件是否改变，延迟5s
	 * 
	 * @param filePath    文件路径
	 * @param spanSeconds 延迟时间自己指定
	 * @return
	 */
	public static boolean fileChanged(String filePath, int spanSeconds) {
		File f1 = new File(filePath);
		if (!f1.exists()) {// 文件不存在
			return true;
		}
		int fileCode = f1.getAbsolutePath().hashCode();
		Integer fileStatusCode = getFileCode(filePath);

		return isChanged(fileCode, fileStatusCode, spanSeconds);

	}

	/**
	 * 获取文件状态code
	 * 
	 * @param filePath
	 * @return
	 */
	public static int getFileCode(String filePath) {
		File f1 = new File(filePath);
		if (!f1.exists()) {// 文件不存在
			return -1;
		}
		String s1 = f1.getAbsolutePath() + "|" + f1.lastModified() + "|" + f1.length();
		Integer code = Integer.valueOf(s1.hashCode());
		return code;
	}

	/**
	 * 判断是否变化，初始化设置会返回false
	 * 
	 * @param id          唯一编码
	 * @param statusCode  状态码
	 * @param spanSeconds 判断间隔（秒）
	 * @return
	 */
	public static boolean isChanged(int id, int statusCode, int spanSeconds) {
		boolean isInitSetting = false; // 是否是初始化设置
		if (isHave(id)) {
			if (!isOverTime(id, spanSeconds)) {
				return false;
			}
		} else {
			isInitSetting = true;
		}

		// 当前时间
		long t1 = System.currentTimeMillis();

		if (FILE_LIST.containsKey(id)) {
			Integer statusCode1 = FILE_LIST.get(id);
			if (statusCode1 != null && statusCode1 == statusCode) {
				// 记录当前时间
				putTime(id, t1);
				return false;
			}
		} else {
			isInitSetting = true;
		}

		putTimeAndFileCode(id, t1, statusCode);

		if (isInitSetting) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 是否存在对象
	 * 
	 * @param id
	 * @return
	 */
	public static boolean isHave(int id) {
		return PAST_TIME.containsKey(id);
	}

	/**
	 * 是否过期
	 * 
	 * @param id
	 * @param spanSeconds
	 * @return
	 */
	public static boolean isOverTime(int id, int spanSeconds) {
		if (isHave(id)) {
			Long t1 = PAST_TIME.get(id);
			long time = System.currentTimeMillis();
			long diff = time - t1.longValue();
			if (diff < spanSeconds * 1000) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}

	}

	public static void putTimeAndFileCode(Integer fileCode, Long time, Integer code) {
		try {
			if (LOCK.tryLock()) {
				PAST_TIME.put(fileCode, time);
				FILE_LIST.put(fileCode, code);
				LOGGER.debug(fileCode + ", TIME=" + time + ", CODE=" + code);
			} else {
				// LOGGER.error("get Lock Failed");
			}
		} catch (Exception e) {
			LOGGER.error(e);
		} finally {
			// 查询当前线程是否保持此锁。
			if (LOCK.isHeldByCurrentThread()) {
				LOCK.unlock();
			}
		}
	}

	public static boolean remove(Integer fileCode) {
		if (!isHave(fileCode)) {
			return true;
		}
		try {
			if (LOCK.tryLock()) {
				PAST_TIME.remove(fileCode);
				FILE_LIST.remove(fileCode);

				return true;
			} else {
				// LOGGER.error("get Lock Failed");
				return false;
			}
		} catch (Exception e) {
			LOGGER.error(e);
			return false;
		} finally {
			// 查询当前线程是否保持此锁。
			if (LOCK.isHeldByCurrentThread()) {
				LOCK.unlock();
			}
		}
	}

	/**
	 * 放置文件下次检查比对的时间
	 * 
	 * @param fileCode
	 * @param t1
	 */
	public static void putTime(Integer fileCode, Long t1) {
		try {
			if (LOCK.tryLock()) {
				PAST_TIME.put(fileCode, t1);
				LOGGER.debug(fileCode + ", TIME=" + t1);
			} else {
				// LOGGER.error("get Lock Failed");
			}
		} catch (Exception e) {
			LOGGER.error(e);
		} finally {
			// 查询当前线程是否保持此锁。
			if (LOCK.isHeldByCurrentThread()) {
				LOCK.unlock();
			}
		}

	}
}
