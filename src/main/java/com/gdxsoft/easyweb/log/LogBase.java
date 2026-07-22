package com.gdxsoft.easyweb.log;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.script.display.HtmlCreator;

/**
 * The log class base
 */
public class LogBase {
	private static Logger LOGGER = LoggerFactory.getLogger(LogBase.class);
	/**
	 * 写入数据库的细节长度最大值，默认4k，和表 log_detail.det_description对应
	 */
	private static final int MAX_DETAIL_LENGTH = 4096;
	/**
	 * 写入数据库的细节长度，默认4k，设为0则不限制，和表 log_detail.det_description对应
	 */
	private static final AtomicInteger DETAIL_MAX_SIZE_ATOM = new AtomicInteger(MAX_DETAIL_LENGTH);
	/**
	 * 写入日志数据库的连接池名称
	 */
	private static AtomicReference<String> CONN_CONFIG_NAME = new AtomicReference<>("");
	/**
	 * 日志写入线程池核心线程数
	 */
	private static AtomicInteger EXECUTOR_CORE_POOL_SIZE = new AtomicInteger(2);
	/**
	 * 日志写入任务队列容量，超出后丢弃并打 warn 日志。
	 * 不宜过大：每个任务持有一个 DataConnection（含 JDBC 连接），
	 * 队列深度应 ≤ 数据库连接池大小。
	 */
	private static AtomicInteger EXECUTOR_QUEUE_CAPACITY = new AtomicInteger(33);
	 
	/**
	 * 设置写入数据库的细节无限制长度，根据数据库的限制来，例如：
	 * <ul>
	 * <li>SqlServer的NVARCHAR(max)是2GB，NTEXT是1GB</li>
	 * <li>MySQL的TEXT是64k，MEDIUMTEXT是16MB，LONGTEXT是4GB</li>
	 * <li>Oracle的CLOB是128TB</li>
	 * <li>PostgreSQL的TEXT是1GB</li>
	 * </ul>
	 */
	public static void setDetailUnlimitSize() {
		setDetailMaxSize(0);
	}

	public static void setDetailMaxSize(int size) {
		DETAIL_MAX_SIZE_ATOM.set(size);
	}

	public static int getDetailMaxSize() {
		return DETAIL_MAX_SIZE_ATOM.get();
	}
	public static void setConnConfigName(String name) {
		CONN_CONFIG_NAME.set(name);
	}

	public static String getConnConfigName() {
		return CONN_CONFIG_NAME.get();
	}

	/**
	 * 设置日志写入线程池的核心线程数，运行时动态生效。
	 *
	 * @param size 核心线程数
	 */
	public static void setExecutorCorePoolSize(int size) {
		EXECUTOR_CORE_POOL_SIZE.set(size);
		EXECUTOR.setCorePoolSize(size);
	}

	/**
	 * 获取日志写入线程池的核心线程数。
	 *
	 * @return 核心线程数
	 */
	public static int getExecutorCorePoolSize() {
		return EXECUTOR_CORE_POOL_SIZE.get();
	}

	/**
	 * 设置日志写入任务队列容量。
	 * <p>
	 * 注意：队列容量在 {@code EXECUTOR} 初始化时固定，运行时修改仅更新原子值，
	 * 不影响已创建的 {@code LinkedBlockingQueue} 的实际容量。
	 *
	 * @param capacity 队列容量
	 */
	public static void setExecutorQueueCapacity(int capacity) {
		EXECUTOR_QUEUE_CAPACITY.set(capacity);
	}

	/**
	 * 获取日志写入任务队列容量（配置值）。
	 *
	 * @return 队列容量配置值
	 */
	public static int getExecutorQueueCapacity() {
		return EXECUTOR_QUEUE_CAPACITY.get();
	}
	
	public static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(
			EXECUTOR_CORE_POOL_SIZE.get(), Math.max(EXECUTOR_CORE_POOL_SIZE.get(), Runtime.getRuntime().availableProcessors()),
			60L, TimeUnit.SECONDS,
			new LinkedBlockingQueue<>(EXECUTOR_QUEUE_CAPACITY.get()),
			r -> {
				Thread t = new Thread(r, "ewa-script-log-writer");
				t.setDaemon(true);
				return t;
			},
			(r, executor) -> LOGGER.warn("Log queue full, task dropped. Queue size: {}", executor.getQueue().size()));

	
	
	private HtmlCreator _Creator;

	private Log _Log;

	public Log getLog() {
		return _Log;
	}

	public void setLog(Log log) {
		_Log = log;
	}

	/**
	 * Get the HtmlCreator (parent class)
	 * 
	 * @return HtmlCreator
	 */
	public HtmlCreator getCreator() {
		return _Creator;
	}

	/**
	 * Set the HtmlCreator (parent class)
	 * 
	 * @param creator the HtmlCreator
	 */
	public void setCreator(HtmlCreator creator) {
		this._Creator = creator;
	}

	/**
	 * 兼容老的日志调用
	 */
	public void write() {
		
		this.Write();
	}

	public void Write() {

	}

}
