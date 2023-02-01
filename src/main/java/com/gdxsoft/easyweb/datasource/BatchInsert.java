package com.gdxsoft.easyweb.datasource;

import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MListStr;
import com.gdxsoft.easyweb.utils.msnet.MStr;

/***
 * Batch insert into the table
 */
public class BatchInsert {
	private static Logger LOGGER = LoggerFactory.getLogger(BatchInsert.class);

	private int maxInsertCount = 100;
	private int maxInsertSizeKilobit = 32; // ORACLE插入字符串限制 默认32k
	private boolean transcation;
	private DataConnection cnn;

	private String errors;

	private PreparedStatement ps;
	private MListStr al;

	private int batchCount;

	/**
	 * Initial class
	 * 
	 * @param cnn         the data connection
	 * @param transcation is using transcation , rollback data when an error occurs
	 */
	public BatchInsert(DataConnection cnn, boolean transcation) {
		this.cnn = cnn;
		this.transcation = transcation;
	}

	public boolean batchStart(String sql) {
		al = Utils.getParameters(sql, "@");
		batchCount = 0;
		this.cnn.connect();
		String sql1 = this.cnn.replaceSqlParameters(sql);
		try {
			ps = this.cnn.getConnection().prepareStatement(sql1);
			if (this.transcation) {
				if (cnn.transBegin()) {
					return true;
				} else {
					errors = cnn.getErrorMsg();
					LOGGER.error(errors);
					return false;
				}
			}
			return true;
		} catch (SQLException e) {
			errors = e.getMessage();
			LOGGER.error(errors);
			return false;
		}
	}

	public boolean batchAddData(RequestValue rv) {
		RequestValue old = cnn.getRequestValue();
		cnn.setRequestValue(rv);
		try {
			cnn.addSqlParameter(al, ps);
			ps.addBatch();
			batchCount++;
			return true;
		} catch (SQLException e) {
			if (this.transcation) {
				cnn.transRollback();
			}
			errors = cnn.getErrorMsg();
			LOGGER.error(errors);

			return false;
		} finally {
			cnn.setRequestValue(old);
		}

	}

	public int[] batchExceute() {
		int[] results = null;
		try {
			if (batchCount > 0) {
				results = ps.executeBatch();
			}
			if (this.transcation) {
				cnn.transCommit();
			}
			return results;
		} catch (SQLException e) {
			this.errors = e.getMessage();
			LOGGER.error(errors);
			return null;
		} finally {
			if (this.transcation) {
				cnn.transRollback();
			}
			try {
				ps.close();
				ps = null;
			} catch (SQLException e) {
				LOGGER.warn(errors);
			}

		}
	}

	/**
	 * Batch insert
	 * 
	 * @param insertHeader the insert SQL header, e.g. insert into tb_a(a,b) values
	 * @param values       the list of values e.g. (0,'a')(1,'b')
	 * @return the errors
	 */
	public String insertBatch(String insertHeader, List<String> values) {

		if (values == null || values.size() == 0) {
			return null;
		}

		boolean mysql = SqlUtils.isMySql(cnn);// "mysql".equalsIgnoreCase(this.cnn.getDatabaseType());
		boolean sqlserver = SqlUtils.isSqlServer(cnn);// "sqlserver".equalsIgnoreCase(this.cnn.getDatabaseType())
		boolean oracle = SqlUtils.isOracle(cnn);// "oracle".equalsIgnoreCase(this.cnn.getDatabaseType());

		if (this.transcation) {
			this.cnn.transBegin();
		}
		LOGGER.debug("Start batch insert, ({}) ", values.size());
		long t0 = System.currentTimeMillis();
		if (mysql) {
			errors = this.batchMysql(insertHeader, values);
		} else if (sqlserver) {
			errors = this.batchSqlServer(insertHeader, values);
		} else if (oracle) {
			errors = this.batchOracle(insertHeader, values);
		} else {
			errors = this.defaultBatch(insertHeader, values);
		}
		long t1 = System.currentTimeMillis();
		if (errors != null && errors.trim().length() > 0) {
			if (this.transcation) {
				this.cnn.transRollback();
				this.cnn.transClose();
			}
			LOGGER.error(errors);
		} else {
			if (this.transcation) {
				this.cnn.transCommit();
				this.cnn.transClose();
			}
		}
		LOGGER.debug("End batch insert, time {}ms ", t1 - t0);

		return errors;
	}

	/**
	 * Default batch insert method
	 * 
	 * @param insertHeader then insert SQL header, e.g. insert into tb_a(a,b) values
	 * @param values       the list of values e.g. (0,'a')(1,'b')
	 * @return errors
	 */
	private String defaultBatch(String insertHeader, List<String> values) {
		if (this.transcation) {
			this.cnn.transBegin();
		}
		MStr sbError = new MStr();
		// 批量插入数据的数量
		int bulkMax = this.getBulkMax();

		List<String> sqls = new ArrayList<String>();
		for (int i = 0; i < values.size(); i++) {
			StringBuilder sb = new StringBuilder();
			sb.append(insertHeader).append(values.get(i));

			sqls.add(sb.toString());
			if (i > 0 && i % bulkMax == 0) {
				this.cnn.batchUpdate(sqls, false);
				if (this.cnn.getErrorMsg() != null) {
					sbError.al(this.cnn.getErrorMsg());
					if (this.transcation) {
						return sbError.toString();
					}
					this.cnn.clearErrorMsg();
				}
				sqls.clear();
			}
		}
		if (sqls.size() > 0) {
			this.cnn.batchUpdate(sqls, false);
			if (this.cnn.getErrorMsg() != null) {
				sbError.al(this.cnn.getErrorMsg());
				if (this.transcation) {
					return sbError.toString();
				}
				this.cnn.clearErrorMsg();
			}
		}
		if (this.transcation) {
			this.cnn.transCommit();
			this.cnn.transClose();
		}

		return sbError.toString();
	}

	/**
	 * MYSQL Batch insert
	 * 
	 * @param insertHeader insertHeader then insert SQL header, e.g. insert into
	 *                     tb_a(a,b) values
	 * @param values       the list of values e.g. (0,'a')(1,'b')
	 * @return
	 */
	private String batchMysql(String insertHeader, List<String> values) {

		// 批量插入数据的数量
		int bulkMax = this.getBulkMax();
		MStr sbError = new MStr();
		MStr batSql = new MStr();

		// mysq 插入方法 insert aa (a,b) values (1,2),(3,4),(5,6)
		int inc = 0;
		for (int i = 0; i < values.size(); i++) {
			if (i % bulkMax == 0) {
				if (i > 0) {
					String err = this.execInsert(batSql.toString());
					if (err != null) {
						sbError.append(err);
						if (this.transcation) {
							return sbError.toString();
						}
					}
				}
				batSql = new MStr();

				batSql.al(insertHeader);
				inc = 0;
			}
			String sqlValues = values.get(i);
			if (inc > 0) {
				batSql.a(",");
			}
			batSql.al(sqlValues);
			inc++;
		}
		if (batSql.length() > 0)

		{
			String err = this.execInsert(batSql.toString());
			if (err != null) {
				sbError.append(err);
			}
		}

		return sbError.toString();
	}

	/**
	 * SQLSERVER Batch insert
	 * 
	 * @param insertHeader
	 * @param values
	 * @return
	 */
	private String batchSqlServer(String insertHeader, List<String> values) {
		// 批量插入数据的数量
		int bulkMax = this.maxInsertCount;
		if (bulkMax <= 0) {
			bulkMax = 100;
		}
		MStr sbError = new MStr();
		MStr batSql = new MStr();

		// sqlserver 插入方法 insert aa (a,b) values (1,2) insert aa (a,b) values (1,2)
		for (int i = 0; i < values.size(); i++) {
			if (i % bulkMax == 0) {
				if (i > 0) {
					String err = this.execInsert(batSql.toString());
					if (err != null) {
						sbError.append(err);
						if (this.transcation) {
							return sbError.toString();
						}
					}
				}
				batSql = new MStr();

			}
			String sqlValues = values.get(i);
			batSql.al(insertHeader);
			batSql.al(sqlValues);
		}
		if (batSql.length() > 0) {
			String err = this.execInsert(batSql.toString());
			if (err != null) {
				sbError.append(err);
			}
		}
		return sbError.toString();
	}

	private int getBulkMax() {
		// 批量插入数据的数量
		int bulkMax = this.maxInsertCount;
		if (bulkMax <= 0) {
			bulkMax = 100;
		}
		return bulkMax;
	}

	/**
	 * Check the SQL packet limit exceeded
	 * 
	 * @param batSql
	 * @param newSql
	 * @return
	 */
	private boolean checkPackageSizeOver(MStr batSql, MStr newSql) {
		int limitsPackageSize = (maxInsertSizeKilobit <= 0 ? 32 : maxInsertSizeKilobit) * 1024 - 1000;
		int len = batSql.toString().getBytes(StandardCharsets.ISO_8859_1).length
				+ newSql.toString().getBytes(StandardCharsets.ISO_8859_1).length;

		return len > limitsPackageSize;
	}

	/**
	 * Oracle batch insert
	 * 
	 * @param insertHeader
	 * @param values
	 * @return
	 */
	private String batchOracle(String insertHeader, List<String> values) {
		MStr sbError = new MStr();
		MStr batSql = new MStr();

//		INSERT INTO T1 (fa,fb)
//	    SELECT a.* FROM
//	    (
//	        SELECT 1, 'a' FROM DUAL UNION
//	        SELECT 2, 'b' FROM DUAL UNION
//	        SELECT 3, 'c' FROM DUAL
//	    ) a

		// 批量插入数据的数量
		int bulkMax = this.getBulkMax();
		int inc = 0;
		batSql.al(insertHeader);
		batSql.al("select a.* from (");
		for (int i = 0; i < values.size(); i++) {
			// 按照数量进行限制
			if (inc >= bulkMax) {
				batSql.al(") a");

				String err = this.execInsert(batSql.toString());
				if (err != null) {
					sbError.append(err);
				}
				batSql = new MStr();
				batSql.al(insertHeader);
				batSql.al("select a.* from (");
				inc = 0; // 重新计数
			}

			String sqlValues = values.get(i).trim();

			MStr tmp = new MStr();
			if (inc > 0) {
				tmp.al(" union ");
			}
			tmp.a("SELECT ");
			// 去掉前"("和 ")"
			tmp.a(sqlValues.substring(1, sqlValues.length() - 1));
			tmp.a(" FROM DUAL");

			// ORACLE插入字符串不能大于32k ？
			// 根据Oracle文档描述，在Oracle9i中，SQL Statement Length - Maximum length of statements -
			// 64 K maximum; particular
			// tools may impose lower limits。
			// 这里的64K限制实际上并不确切，很多更长的SQL也可以执行，在Oracle10g的文档中，记录如下定义：
			// The limit on how long a SQL statement can be depends on many factors,
			// including database configuration,
			// disk space, and memory。
			// 判断是否超过sql包字节限制
			if (inc > 0 && this.checkPackageSizeOver(batSql, tmp)) { // 超过数据包的字节限制
				batSql.al(") a");

				String err = this.execInsert(batSql.toString());
				if (err != null) {
					sbError.append(err);
				}
				batSql = new MStr();
				batSql.al(insertHeader);
				batSql.al("select a.* from (");
				inc = 0; // 重新计数
			}
			batSql.al(tmp);
			inc++;
		}
		if (batSql.length() > 0) {
			batSql.al(") a");

			String err = this.execInsert(batSql.toString());
			if (err != null) {
				sbError.append(err);
			}
		}
		return sbError.toString();
	}

	/**
	 * 执行插入
	 * 
	 * @param sql
	 * @return
	 */
	private String execInsert(String sql) {
		this.cnn.executeUpdateNoParameter(sql);
		String err = this.cnn.getErrorMsg();

		this.cnn.clearErrorMsg();
		return err;
	}

	/**
	 * Max records insert once (defaults 100)
	 * 
	 * @return
	 */
	public int getMaxInsertCount() {
		return maxInsertCount;
	}

	/**
	 * Max records insert once (defaults 100)
	 * 
	 * @param maxInsertCount the max records
	 */
	public void setMaxInsertCount(int maxInsertCount) {
		this.maxInsertCount = maxInsertCount;
	}

	/**
	 * The max kb of the insert sql (defaults 32k)
	 * 
	 * @return max kb
	 */
	public int getMaxInsertSizeKilobit() {
		return maxInsertSizeKilobit;
	}

	/**
	 * The max kb of the insert sql (defaults 32k)
	 * 
	 * @param maxInsertSizeKilobit max kb
	 */
	public void setMaxInsertSizeKilobit(int maxInsertSizeKilobit) {
		this.maxInsertSizeKilobit = maxInsertSizeKilobit;
	}

	/**
	 * Whether is using transcation method (defaults false)
	 * 
	 * @return
	 */
	public boolean isTranscation() {
		return transcation;
	}

	/**
	 * Whether is using transcation method (defaults false)
	 * 
	 * @param transcation
	 */
	public void setTranscation(boolean transcation) {
		this.transcation = transcation;
	}

	/**
	 * The database connection
	 * 
	 * @return connection
	 */
	public DataConnection getCnn() {
		return cnn;
	}

	/**
	 * The database connection
	 * 
	 * @param cnn connection
	 */
	public void setCnn(DataConnection cnn) {
		this.cnn = cnn;
	}

	/**
	 * @return the errors
	 */
	public String getErrors() {
		return errors;
	}

	/**
	 * @return the ps
	 */
	public PreparedStatement getPs() {
		return ps;
	}

	/**
	 * @return the batchCount
	 */
	public int getBatchCount() {
		return batchCount;
	}

}
