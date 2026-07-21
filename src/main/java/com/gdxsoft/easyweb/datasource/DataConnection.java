package com.gdxsoft.easyweb.datasource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConfExtraGlobal;
import com.gdxsoft.easyweb.conf.ConfExtraGlobals;
import com.gdxsoft.easyweb.conf.ConnectionConfig;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.debug.DebugFrames;
import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.PageValueTag;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.frame.FrameParameters;
import com.gdxsoft.easyweb.utils.UFormat;
import com.gdxsoft.easyweb.utils.ULogic;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MList;
import com.gdxsoft.easyweb.utils.msnet.MListStr;
import com.gdxsoft.easyweb.utils.msnet.MStr;
import com.gdxsoft.easyweb.utils.types.UInt16;
import com.gdxsoft.easyweb.utils.types.UInt32;
import com.gdxsoft.easyweb.utils.types.UInt64;

/**
 * Database connection wrapper providing SQL execution, transaction management,
 * batch operations, and parameter handling.
 * 
 * <p><b>Thread Safety:</b> This class is <i>not thread-safe</i>.
 * Instances hold mutable state (Statements, ResultSets, Connection references)
 * and should not be shared across threads. Each thread should create its own
 * instance, or use the static utility methods which create and dispose instances
 * internally.
 * 
 * @author guolei
 */
public class DataConnection {
	private static Logger LOGGER = LoggerFactory.getLogger(DataConnection.class);

	/** Delegate for connection/transaction/statement lifecycle (God Class refactor). */
	private ConnectionSession _session;

	/** Delegate for SQL building and parameter binding (God Class refactor). */
	private SqlBuilder _sqlBuilder;

	private String _errorMsg; // 错误信息和 SQL
	private String _errorMsgOnly; // 只有错误信息

	private DebugFrames _DebugFrames;
	private RequestValue _RequestValue;
	// 用于分割字符串，生成临时数据

	private int _TimeDiffMinutes; // 用户和系统的时差

	// 批处理更新返回的表
	private List<DTTable> updateBatchTables;

	/**
	 * 用户和系统的时差(分钟)
	 * 
	 * @return
	 */
	public int getTimeDiffMinutes() {
		return _TimeDiffMinutes;
	}

	/**
	 * 用户和系统的时差(分钟)
	 * 
	 * @param timeDiffMinutes 分钟
	 */
	public void setTimeDiffMinutes(int timeDiffMinutes) {
		this._TimeDiffMinutes = timeDiffMinutes;
	}

	/**
	 * 执行更新并关闭连接
	 * 
	 * @param sql
	 * @param configName
	 * @param rv
	 * @return 返回是否错误
	 */
	public static String updateAndClose(String sql, String configName, RequestValue rv) {
		DataConnection cnn = new DataConnection(configName, rv);
		if (rv == null) {
			cnn.executeUpdateNoParameter(sql);
		} else {
			cnn.executeUpdate(sql);
		}
		String rst = cnn.getErrorMsg();
		cnn.close();
		return rst;
	}

	/**
	 * 新建记录并返回自增字段，数据库连接自动关闭
	 * 
	 * @param sql
	 * @param configName
	 * @param rv
	 * @return
	 */
	public static long insertAndReturnAutoIdLong(String sql, String configName, RequestValue rv) {
		DataConnection cnn = new DataConnection(configName, rv);
		Object v1 = cnn.executeUpdateReturnAutoIncrementObject(sql);
		cnn.close();
		if (cnn.getErrorMsg() != null) {
			return -1L;
		}
		if (v1 == null) {
			return 0L;
		} else {
			return Long.parseLong(v1.toString());
		}
	}

	/**
	 * 新建记录并返回自增字段，数据库连接自动关闭
	 * 
	 * @param sql
	 * @param configName
	 * @param rv
	 * @return
	 */
	public static int insertAndReturnAutoIdInt(String sql, String configName, RequestValue rv) {
		DataConnection cnn = new DataConnection(configName, rv);
		int id = cnn.executeUpdateReturnAutoIncrement(sql);
		cnn.close();
		if (cnn.getErrorMsg() != null) {
			return -1;
		}
		return id;
	}

	public static String updateAndClose(StringBuilder sb, String configName, RequestValue rv) {
		return updateAndClose(sb.toString(), configName, rv);
	}

	/**
	 * 获取数据量
	 * 
	 * @param tableName  表名
	 * @param where      查询条件
	 * @param configName
	 * @param rv
	 * @return =0 无数据，>0有数据，-1 执行错误
	 */
	public static int queryCount(String tableName, String where, String configName, RequestValue rv) {
		DataConnection cnn = new DataConnection(configName, rv);
		int count = cnn.executeQueryCount(tableName, where);
		cnn.close();

		return count;
	}

	/**
	 * 检查数据是否存在
	 * 
	 * @param tableName  表名
	 * @param where      查询条件
	 * @param configName
	 * @param rv
	 * @return 有数据true,无数据和执行错误false
	 */
	public static boolean queryExists(String tableName, String where, String configName, RequestValue rv) {
		DataConnection cnn = new DataConnection(configName, rv);
		boolean exists = cnn.executeQueryExists(tableName, where);
		cnn.close();

		return exists;
	}

	/**
	 * 批量执行更新并关闭连接
	 * 
	 * @param sqls       用;分割的sql字符串
	 * @param configName
	 * @param rv
	 * @return 返回是否错误
	 */
	public static String updateBatchAndClose(String sqls, String configName, RequestValue rv) {
		DataConnection cnn = new DataConnection(configName, rv);
		cnn.executeUpdateBatch(sqls);
		String rst = cnn.getErrorMsg();
		cnn.close();
		return rst;
	}

	/**
	 * 批量执行更新并关闭连接
	 * 
	 * @param sb         用;分割的sql字符串
	 * @param configName
	 * @param rv
	 * @return
	 */
	public static String updateBatchAndClose(StringBuilder sb, String configName, RequestValue rv) {
		return updateBatchAndClose(sb.toString(), configName, rv);
	}

	/**
	 * 批量执行更新并关闭连接
	 * 
	 * @param sqls       sql列表
	 * @param configName
	 * @param rv
	 * @return 返回是否错误
	 */
	public static String updateBatchAndClose(List<String> sqls, String configName, RequestValue rv) {
		DataConnection cnn = new DataConnection(configName, rv);
		cnn.executeUpdateBatch(sqls);
		String rst = cnn.getErrorMsg();
		cnn.close();
		return rst;
	}

	/**
	 * 批量执行更新并关闭连接 (Transaction)
	 * 
	 * @param sqls       sql列表
	 * @param configName
	 * @param rv
	 * @return 返回是否错误
	 */
	public static String updateBatchAndCloseTransaction(List<String> sqls, String configName, RequestValue rv) {
		DataConnection cnn = new DataConnection(configName, rv);
		cnn.transBegin();
		try {
			int runInc = cnn.executeUpdateBatch(sqls);
			if (runInc == -1) {
				cnn.transRollback();
			} else {
				cnn.transCommit();
			}
			String rst = cnn.getErrorMsg();
			return rst;
		} catch (Exception err) {
			cnn.transRollback();
			return err.getMessage();
		} finally {
			cnn.close();
		}

	}

	/**
	 * 执行混合SQL语句(查询，更新(包含返回自增)，存储过程)
	 * 
	 * @param sqls
	 * @param configName
	 * @param rv
	 * @return 返回的所有表
	 * @throws Exception
	 */
	public static List<DTTable> runMultiSqlsAndClose(String sqls, String configName, RequestValue rv) throws Exception {
		List<String> listSqls = getSqls(sqls);
		return runMultiSqlsAndClose(listSqls, configName, rv);
	}

	/**
	 * 执行混合SQL语句(查询，更新(包含返回自增)，存储过程)
	 * 
	 * @param listSqls
	 * @param configName
	 * @param rv
	 * @return 返回的所有表
	 * @throws Exception
	 */
	public static List<DTTable> runMultiSqlsAndClose(List<String> listSqls, String configName, RequestValue rv)
			throws Exception {
		DataConnection cnn = new DataConnection(configName, rv);
		List<DTTable> tbs = cnn.runMultiSqls(listSqls);
		cnn.close();
		return tbs;
	}

	/**
	 * 执行SQL查询返回多条结果集
	 * 
	 * @param sql
	 * @param configName
	 * @param rv
	 * @return 结果集
	 * @throws SQLException
	 */
	public static List<DTTable> executeQueryAndReturnTables(String sql, String configName, RequestValue rv)
			throws SQLException {
		DataConnection cnn = new DataConnection(configName, rv);
		boolean resultStatus = cnn.executeQuery(sql);
		if (!resultStatus) {
			cnn.close();
			throw new SQLException(cnn.getErrorMsg());
		}
		List<DTTable> tables = new ArrayList<DTTable>();
		cnn.getMoreResults();
		for (int i = 0; i < cnn.getResultSetList().size(); i++) {
			DataResult r = (DataResult) cnn.getResultSetList().get(i);
			DTTable tb = new DTTable();
			tb.initData(r.getResultSet());
				tables.add(tb);
		}
		cnn.close();
		return tables;
	}

	/**
	 * 查找自增的sql的返回字段, 例如 -- auto MEMO_ID
	 * 
	 * @param sql
	 * @return
	 */
	public static String getAutoField(String sql) {
		return SqlUtils.getAutoField(sql);
	}

	/**
	 * 检查是否为 存储过程，例如 CALL pr_batAdd(@a)
	 * 
	 * @param sql
	 * @return
	 */
	public static boolean checkIsProcdure(String sql) {
		return checkStartWord(sql, "CALL");
	}

	/**
	 * 检查是否为 select语句<br>
	 * 或标记为<b>-- EWA_IS_SELECT</b><br>
	 * 或with block语句
	 * 
	 * @param sql
	 * @return
	 */
	public static boolean checkIsSelect(String sql) {
		return SqlUtils.checkIsSelect(sql);
	}

	/**
	 * 判断 特定字符出现在非注释的SQL的0位置，多行的SQL只进行第一次判断
	 * 
	 * @param sql
	 * @param word 关键单词，例如SELECT
	 * @return
	 */
	public static boolean checkStartWord(String sql, String word) {
		return SqlUtils.checkStartWord(sql, word);
	}

	/**
	 * 删除sql 的多行备注
	 * 
	 * @param sql
	 * @return
	 */
	public static String removeSqlMuitiComment(String sql) {
		return SqlUtils.removeSqlMuitiComment(sql);
	}

	/**
	 * 是否比较更新前和更新后字段的变化, 方式：<br>
	 * SQL 添加 -- COMPARATIVE_CHANGES
	 * 
	 * @param sql
	 * @return
	 */
	public static boolean isComparativeChanges(String sql) {
		return SqlUtils.isComparativeChanges(sql);
	}

	/**
	 * 将sql混合语句，通过“;”符号，分解为sql列表
	 * 
	 * @param sqls
	 * @return sql列表
	 */
	public static List<String> getSqls(String sqls) {
		String[] sqlArray = sqls.split(";");
		List<String> al = new ArrayList<String>();
		for (int i = 0; i < sqlArray.length; i++) {
			String sql = sqlArray[i].trim();
			if (sql.length() == 0) { // 空语句
				continue;
			}
			al.add(sql);
		}

		return al;
	}

	/**
	 * 数据库名称
	 * 
	 * @return
	 */
	public String getDataBaseName() {
		return _session.getDataBaseName();
	}

	/**
	 * 数据库名称
	 * 
	 * @param dataBaseName
	 */
	public void setDataBaseName(String dataBaseName) {
		_session.setDataBaseName(dataBaseName);
	}

	/**
	 * 开始事务处理
	 * 
	 * @return
	 */
	public boolean transBegin() {
		boolean ok = _session.transBegin();
		if (!ok) {
			this._errorMsg = "事务开始错误";
		}
		return ok;
	}

	/**
	 * 提交事务
	 * 
	 * @throws SQLException
	 */
	public boolean transCommit() {
		boolean ok = _session.transCommit();
		if (!ok) {
			this._errorMsg = "提交事务失败";
		}
		return ok;
	}

	/**
	 * 事务回滚
	 * 
	 * @throws SQLException
	 */
	public void transRollback() {
		_session.transRollback();
	}

	/**
	 * 关闭事务连接
	 */
	public void transClose() {
		_session.transClose();
	}

	public ConnectionConfig getCurrentConfig() {
		return _session.getCurrentConfig();
	}

	public DataConnection() {
		try {
			_session = new ConnectionSession();
			_sqlBuilder = new SqlBuilder(this);
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			this._errorMsg = e.getMessage();
		}

	}

	public DataConnection(RequestValue rv) {
		try {
			_session = new ConnectionSession();
			_sqlBuilder = new SqlBuilder(this);

			this.setConfigName("");
			this.setRequestValue(rv);

		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			this._errorMsg = e.getMessage();
		}

	}

	public DataConnection(String configName, RequestValue rv) {
		try {
			_session = new ConnectionSession(configName);
			_sqlBuilder = new SqlBuilder(this);

			this.setConfigName(configName);
			this.setRequestValue(rv);

		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			this._errorMsg = e.getMessage();
		}

	}

	public void setConfigName(String configName) {
		_session.setConfigName(configName);
	}

	/**
	 * 设置数据库连接池句柄
	 * 
	 * @param configName
	 * @param dataBaseName
	 */
	public void setConfigName(String configName, String dataBaseName) {
		_session.setConfigName(configName);
	}

	public boolean connect() {
		return _session.connect();
	}

	private void initConnection() {
		if (this._DebugFrames != null) {
			this._DebugFrames.addDebug(this, "SQL", "[initConnection()] Start building connection. ("
					+ _session.getCurrentConfig().getConnectionString() + ")");
		}

		_session.setCurrentConfig(_session.getCurrentConfig());

		if (this._DebugFrames != null) {
			this._DebugFrames.addDebug(this, "SQL",
					"[initConnection()] build a connection. (" + _session.getCurrentConfig().getConnectionString() + ")");
		}
	}

	public boolean executeQueryNoParameter(String sql) {

		StringBuilder debuginfo = new StringBuilder();
		debuginfo.append("[executeQuery(sql)] Start excute query. \n\n");
		debuginfo.append(sql);
		writeDebug(this, "SQL", debuginfo.toString());

		this.closeStatment(_session.getQueryStatement());
		try {
			this.useDatabase();

			sql = this.rebuildSql(sql);

			_session.getDataHelper().connect();

			_session.setQueryStatement(_session.getDataHelper().getStatement());
			ResultSet rs = _session.getQueryStatement().executeQuery(sql);
			this.addResult(rs, sql, sql);
			writeDebug(this, "SQL", "[executeQuery(sql)] End query.");
			return true;
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			LOGGER.error(sql);
			this.setError(e, sql);
			if (!this.isTrans()) {
				this.close();
			}
			return false;
		}
	}

	private DataResult addResult(ResultSet rs, String sqlExecute, String sqlOrigin) {
		return _session.addResult(rs, sqlExecute, sqlOrigin);
	}

	private void executeEwaFunctions() {
		if (_sqlBuilder.getEwaSqlFunctions() == null || _sqlBuilder.getEwaSqlFunctions().getTempData().size() == 0) {
			return;
		}
		_sqlBuilder.getEwaSqlFunctions().executeEwaFunctions(_RequestValue, this._DebugFrames);
	}

	/**
	 * 根据逻辑判断组合SQL, 判断条件是 <b>"-- ewa_block_test"</b><br>
	 * 
	 * @param orignalSql
	 * @return
	 */
	public String createSqlByEwaBlockTest(String orignalSql) {
		return _sqlBuilder.createSqlByEwaBlockTest(orignalSql);
	}

	/**
	 * 根据逻辑判断组合SQL, 判断条件是 <b>"-- ewa_test"</b><br>
	 * SELECT A.*, B.GRP <br>
	 * &nbsp;&nbsp; FROM TABLE1 A<br>
	 * INNER JOIN TABLE2 B ON A.ID = B.ID<br>
	 * WHERE A.STATUS = 'USED'<br>
	 * -- ewa_test @tag is not null<br>
	 * &nbsp;&nbsp; and A.tag = @tag<br>
	 * -- ewa_test @abc is not null<br>
	 * &nbsp;&nbsp; and A.tag = 'TAG_CAR'<br>
	 * &nbsp;&nbsp; and B.GRP = 'CAR0'<br>
	 * -- ewa_test<br>
	 * order by A.ID, B.GRP desc<br>
	 * 
	 * @param orignalSql
	 * @return
	 */
	public String createSqlByEwaTest(String orignalSql) {
		return _sqlBuilder.createSqlByEwaTest(orignalSql);
	}

	/**
	 * 替换SQL注释行中的@符号，防止被当作参数处理
	 * 
	 * @param orignalSql
	 * @return
	 */
	public String replaceSqlCommentAtSymbol(String orignalSql) {
		return _sqlBuilder.replaceSqlCommentAtSymbol(orignalSql);
	}

	/**
	 * 执行query
	 * 
	 * @param sql    执行的SQL语句
	 * @param oriSql 原始SQL语句
	 * @return
	 */
	private boolean executeQuery(String sql, String oriSql) {
		if (sql == null || oriSql == null) {
			return false;
		}
		if (this._RequestValue == null)
			return this.executeQueryNoParameter(sql);

		if (this._DebugFrames != null) {
			StringBuilder debuginfo = new StringBuilder();
			debuginfo.append("[executeQuery(sql,rv)] Prepare excute query. \n\n");
			debuginfo.append(sql);
			this._DebugFrames.addDebug(this, "SQL", debuginfo.toString());
		}
		String sql1;
		try {
			sql1 = rebuildSql(sql);
		} catch (Exception e) {
			writeDebug(this, "ERR", "[executeQuery(sql,rv)] <font color=red>" + e.getMessage() + "</font>" + ")");
			LOGGER.error(e.getLocalizedMessage());
			setError(e, sql);
			return false;
		}
		this.createEwaSplitTempData(); // guolei 2015-09-08
		this.executeEwaFunctions(); // guolei 2021-03-16

		// 替换select的参数为实际的值
		sql1 = this.replaceSqlSelectParameters(sql1);

		MListStr parameters = Utils.getParameters(sql1, "@");
		try {
			if (parameters.size() > 0) {
				sql1 = replaceSqlParameters(sql1);
			}
			if (this._DebugFrames != null) {
				StringBuilder debuginfo = new StringBuilder();
				debuginfo.append("[executeQuery(sql,rv)] Start excute query. \n\n");
				debuginfo.append(sql1);
				writeDebug(this, "SQL", debuginfo.toString());
			}
			if (_session.getDataHelper() == null) {
				// 没有设置configName,取第一个配置
				this.setConfigName(null);
			}

			this.useDatabase();

			ResultSet rs;

			this.closeStatment(_session.getQueryStatement());
			if (parameters.size() > 0) {
				_session.setPst(_session.getDataHelper().getPreparedStatement(sql1));
				this._errorMsg = null;
				// add parameter
				addSqlParameter(parameters, _session.getPst());
				_session.setQueryStatement(_session.getPst());
				rs = _session.getPst().executeQuery();
			} else {
				_session.getDataHelper().connect();
				this._errorMsg = null;
				_session.setQueryStatement(_session.getDataHelper().getStatement());
				rs = _session.getQueryStatement().executeQuery(sql1);
			}
			this.addResult(rs, sql, oriSql);
			writeDebug(this, "SQL", "[executeQuery(sql,rv)] End query.");
			return true;
		} catch (Exception e) {
			StringBuilder errInfo = new StringBuilder();
			errInfo.append("\nconnStr=").append(_session.getConnectionString()).append("\nCurrentConfig:\n")
					.append(_session.isTrans() ? "Transaction," : "No Transaction,").append("\nname=")
					.append(_session.getCurrentConfig().getName()).append("\n").append(_session.getCurrentConfig().getConnectionString())
					.append("\n\n").append(sql1).append("\n\n").append(e.getLocalizedMessage());
			writeDebug(this, "ERR", "[executeQuery(sql,rv)] <font color=red>" + e.getMessage() + "</font>" + ")");
			LOGGER.error(errInfo.toString());
			setError(e, sql1);
			return false;
		} finally {
			// 放到close处理，以便复用时不用重复创建
			// this.clearEwaSplitTempData();
		}
	}

	/**
	 * 从执行完的sql中获取更多的结果集
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<DataResult> getMoreResults() throws SQLException {
		return _session.getMoreResults();
	}

	/**
	 * 获取数据量
	 * 
	 * @param tableName 表名
	 * @param where     查询条件
	 * @return =0 无数据，>0有数据，-1 执行错误
	 */
	public int executeQueryCount(String tableName, String where) {
		String sql = "select count(*) gdx from " + tableName + " where 1=1 and " + where;
		DTTable tb = DTTable.getJdbcTable(sql, this);
		if (tb.isOk()) {
			return tb.getCell(0, 0).toInt();
		} else {
			// 执行错误
			return -1;
		}
	}

	/**
	 * 检查数据是否存在
	 * 
	 * @param tableName 表名
	 * @param where     查询条件
	 * @return 有数据true,无数据和执行错误false
	 */
	public boolean executeQueryExists(String tableName, String where) {
		String sql = "select 1 gdx from " + tableName + " where 1=1 and " + where;
		DTTable tb = DTTable.getJdbcTable(sql, "gdx", 1, 1, this);
		if (tb.isOk()) {
			if (tb.getCount() == 0) {
				return false;
			} else {
				return true;
			}
		} else {
			// 执行错误
			return false;
		}
	}

	/**
	 * 执行查询
	 * 
	 * @param sql
	 * @param requestValue
	 * @return
	 */
	public boolean executeQuery(String sql) {
		if (this._RequestValue == null) {
			return this.executeQueryNoParameter(sql);
		} else {
			return this.executeQuery(sql, sql);
		}
	}

	/**
	 * 执行分页查询
	 * 
	 * @param sql
	 * @param pkFieldName 主键名称
	 * @param currentPage 当前页码
	 * @param pageSize    每页记录数
	 * @return
	 */
	public boolean executeQueryPage(String sql, String pkFieldName, int currentPage, int pageSize) {

		SqlPart sp = new SqlPart();
		sp.setSql(sql);

		MStr sb = new MStr();
		if (_session.getDatabaseType().equals("ORACLE")) {
			sb.append("SELECT * FROM (SELECT ROWNUM EMP__X___G____D_RN, ");
			// if(fields.equals("*")){
			sb.append("GGDDXX.*");
			// }else{
			// sb.append(sp.getFields());
			// }
			sb.append(" FROM (");
			sb.append(sql);

			sb.append(")GGDDXX WHERE ROWNUM <=" + currentPage * pageSize);

			sb.append(") AGDXA WHERE EMP__X___G____D_RN >" + (currentPage - 1) * pageSize);
		} else if (_session.getDatabaseType().equals("MSSQL")) {
			MStr sqlTmp = new MStr();

			if (sp.getGroupBy().length() > 0) {
				sqlTmp.append("\r\n GROUP BY " + sp.getGroupBy());
			}
			if (sp.getHaving().length() > 0) {
				sqlTmp.append("\r\n HAVING " + sp.getHaving());
			}
			/// 20190816
			if (currentPage == 1) {
				if (sp.getOrderBy().length() > 0) {
					sqlTmp.append("\r\n ORDER BY " + sp.getOrderBy());
				}
				String sql0 = (sp.isHasWithBlock() ? sp.getWithBlock() : "") + "SELECT TOP " + pageSize + " ";
				sb.insert(0, sql0);
				sb.append(sp.getFields());
				sb.append("\r\n FROM ");
				sb.append(sp.getTableName());
				sb.append("\r\n WHERE " + sp.getWhere());
				sb.append(sqlTmp.toString());
			} else {
				String overOrderBy = "";
				if (sp.getOrderBy().length() > 0) {
					overOrderBy = sp.getOrderBy();
				} else if (sp.getGroupBy().length() > 0) {
					overOrderBy = sp.getGroupBy();
				} else if (pkFieldName != null && pkFieldName.trim().length() > 0) {
					overOrderBy = pkFieldName;
				}
				if (sp.isHasWithBlock()) {
					sb.al(sp.getWithBlock());
				}
				sb.append("SELECT * FROM (");
				sb.append("SELECT ");
				sb.append("ROW_NUMBER() OVER(ORDER BY " + overOrderBy + ") EMP__X___G____D_RN,");
				sb.append(sp.getFields());
				sb.append("\r\n FROM ");
				sb.append(sp.getTableName());
				sb.append("\r\n WHERE " + sp.getWhere());
				sb.append(sqlTmp.toString());
				sb.append(")GGDDXX WHERE EMP__X___G____D_RN BETWEEN " + ((currentPage - 1) * pageSize + 1));
				sb.append(" AND " + currentPage * pageSize);
				sb.append("\r\n ORDER BY EMP__X___G____D_RN");
			}
		} else if (_session.getDatabaseType().equals("HSQLDB") || _session.getDatabaseType().equals("MYSQL")) {
			sb.append(sql);
			sb.append(" limit " + pageSize + " offset " + (currentPage - 1) * pageSize);
		} else { // 默认模式
			sb.append(sql);
			sb.append(" limit " + pageSize + " offset " + (currentPage - 1) * pageSize);
		}
		String sqla = sb.toString();
		return this.executeQuery(sqla, sql);

	}

	public String closeStatment(Statement stmt) {
		String err = _session.closeStatment(stmt);
		if (err != null) {
			this.writeDebug(this, "ERR", err);
			setError(new SQLException(err), "Close the statment");
		}
		return err;
	}
	/**
	 * Log and debug SQLWarnings from a Statement. Extracted to eliminate
	 * duplicated warning-handling logic across multiple methods.
	 * 
	 * @param stmt  the Statement whose warnings to process
	 * @param sql   the associated SQL for logging context
	 */
	private void logSqlWarnings(Statement stmt, String sql) {
		try {
			SQLWarning warning = stmt.getWarnings();
			int incWarnings = 0;
			while (warning != null) {
				String msg = warning.getLocalizedMessage();
				if (incWarnings == 0) {
					LOGGER.warn("SQL: {}", sql);
				}
				LOGGER.warn("Warning: {}", msg);
				this.writeDebug(this, "SQL-INFO", msg);
				warning = warning.getNextWarning();
				incWarnings++;
			}
		} catch (SQLException e) {
			LOGGER.warn("Failed to retrieve SQLWarnings: {}", e.getMessage());
		}
	}

	/**
	 * 批处理导入数据
	 * 
	 * @param sqls
	 * @param transcation
	 */
	public void batchUpdate(List<String> sqls, boolean transcation) {
		if (sqls == null || sqls.size() == 0) {
			return;
		}
		Statement stmt = null;
		this.connect();
		try {
			stmt = this.getConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
		} catch (SQLException e) {
			LOGGER.error(e.getLocalizedMessage());
			this.writeDebug(this, "ERR", e.getMessage());
			setError(e, "Create the batch statement");
			return;
		}

		if (transcation) {
			this.transBegin();
		}

		boolean haveData = false;
		for (int i = 0; i < sqls.size(); i++) {
			String sql = sqls.get(i);
			if (sql == null || sql.trim().length() == 0) { // 空语句
				continue;
			}
			try {
				stmt.addBatch(sql);
				haveData = true;
			} catch (SQLException e) {
				LOGGER.error(e.getLocalizedMessage());
				this.writeDebug(this, "ERR", e.getMessage());
				setError(e, sql);

				this.closeStatment(stmt);
				if (transcation) {
					this.close();
				}

				return;
			}
		}

		try {
			if (haveData) { // 有数据
				stmt.executeBatch();
				if (transcation) {
					this.transCommit();
				} else {
					if (!_session.getConnection().getAutoCommit()) {
						_session.getConnection().commit();
					}
				}
			}
		} catch (SQLException e) {
			if (transcation) {
				this.transRollback();
			}
			LOGGER.error(e.getLocalizedMessage());
			this.writeDebug(this, "ERR", e.getMessage());
			setError(e, "");
		} finally {
			this.closeStatment(stmt);
			if (transcation) {
				this.transClose();
			}
		}

	}

	/**
	 * EWA_SPLIT(@ids, ',') <br>
	 * 将字符串分割成表数据，字符串限制长度1000
	 */
	private void createEwaSplitTempData() {
		if (_sqlBuilder.getCreateSplitData() == null) {
			return;
		}
		_sqlBuilder.getCreateSplitData().createEwaSplitTempData();
	}

	/**
	 * 执行多个更新 sql语句，语句用“;”分割，不能在拼接的Sql中有“;”出现
	 * 
	 * @param sql
	 * @return
	 */
	public boolean executeMultipleUpdate(String sql) {
		String[] sqls = sql.split("\\;");

		for (int i = 0; i < sqls.length; i++) {
			String sqlItem = sqls[i];
			if (sqlItem.trim().length() == 0) {
				continue;
			}

			boolean isrunok = this.executeUpdate(sqlItem);
			if (!isrunok) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 批量执行sql语句，混合update和select的语句集合
	 * 
	 * @param sqls
	 * @return
	 */
	public int executeUpdateBatch(List<String> sqls) {
		int runInc = 0;

		this.updateBatchTables = new ArrayList<>();

		for (int i = 0; i < sqls.size(); i++) {
			String sql = sqls.get(i);
			if (sql == null) {
				continue;
			}
			sql = sql.trim();
			if (sql.length() == 0) { // 空语句
				continue;
			}

			boolean isok;
			if (checkIsSelect(sql)) {
				isok = this.executeQuery(sql);
				if (!isok) {
					LOGGER.error(sql);
					return -1;
				}

				DTTable tb = new DTTable(); // 映射到自定义数据表
				tb.initData(this.getLastResult().getResultSet());
				try {
					this.getLastResult().getResultSet().close();

					updateBatchTables.add(tb);
				} catch (SQLException e) {
					LOGGER.warn(sql, e.getMessage());
				}
				// 添加到 rv 中
				if (this._RequestValue != null && tb.isOk()) {
					if (tb.getCount() == 0) {
						LOGGER.debug("The table count = 0", sql);
					} else {
						LOGGER.debug("Add table count = " + tb.getCount() + " to the rv", sql);
						this._RequestValue.addValues(tb);
					}
				}
			} else {
				LOGGER.debug(sql);
				if (this._RequestValue == null) {
					// 无参数执行
					isok = this.executeUpdateNoParameter(sql);
				} else {
					isok = this.executeUpdate(sql);
				}
			}
			if (isok) {
				runInc++;
			} else {
				LOGGER.error(sql);
				return -1;
			}
		}

		return runInc;
	}

	/**
	 * 批处理执行sql语句，sql语句用 ; 分割，getUpdateBatchTables获取执行国产中的所有表
	 * 
	 * @param sqls sql语句用 ; 分割
	 * @return -1 有错误
	 */
	public int executeUpdateBatch(String sqls) {
		String[] sqlArray = sqls.split(";");
		List<String> al = this.convertToList(sqlArray);

		return this.executeUpdateBatch(al);
	}

	public List<String> convertToList(String[] sqlArray) {
		List<String> al = new ArrayList<String>();
		for (int i = 0; i < sqlArray.length; i++) {
			String sql = sqlArray[i].trim();
			al.add(sql);
		}

		return al;
	}

	/**
	 * 执行混合SQL语句(查询，更新(包含返回自增)，存储过程)<br>
	 * 1. 如果是select返回数据的话，回将第一行数据放到 rv中<br>
	 * 2. 自动创建变量 --auto xxx 会放到rv中<br>
	 * 3. 存储过程返回的结果，带 _OUT参数，例如 ID_OUT 会放到rv中
	 * 
	 * @param sqlArray
	 * @return select 返回 DTTable集合
	 * @throws Exception
	 */
	public List<DTTable> runMultiSqls(String[] sqlArray) throws Exception {
		List<String> al = this.convertToList(sqlArray);
		return this.runMultiSqls(al);
	}

	/**
	 * 执行带自动返回值的SQL，返回执行后的自动字段的名称<br>
	 * 如果成功，则在_RequestValue里保存<br>
	 * 如果失败，则清除_RequestValue里的Form,Query,Cookie的值，避免外部攻击
	 * 
	 * @param sql 自增的sql, 例如 -- auto MEMO_ID
	 * @return 返回值的字段名称，保存在_RequestValue里， null无自动字段
	 */
	public String executeAutoFieldReturnName(String sql) {
		String autoField = DataConnection.getAutoField(sql);
		if (autoField == null || autoField.length() == 0) {
			return null;
		}
		// 执行自增的插入
		Object autov = this.executeUpdateReturnAutoIncrementObject(sql);
		if (autov != null && autov.toString().equals("-1")) {
			// 删除可能存在的字段，避免外部攻击
			this._RequestValue.getPageValues().remove(autoField, PageValueTag.FORM);
			this._RequestValue.getPageValues().remove(autoField, PageValueTag.QUERY_STRING);
			this._RequestValue.getPageValues().remove(autoField, PageValueTag.COOKIE);
		} else {
			this._RequestValue.addValue(autoField, autov, PageValueTag.DTTABLE);
		}
		return autoField;
	}

	/**
	 * 执行混合SQL语句(查询，更新(包含返回自增)，存储过程)<br>
	 * 1. 如果是select返回数据的话，回将第一行数据放到 rv中<br>
	 * 2. 自动创建变量 --auto xxx 会放到rv中<br>
	 * 3. 存储过程返回的结果，带 _OUT参数，例如 ID_OUT 会放到rv中
	 * 
	 * @param listSqls
	 * @return
	 * @throws Exception
	 */
	public List<DTTable> runMultiSqls(List<String> listSqls) throws Exception {
		List<DTTable> tbs = new ArrayList<DTTable>();
		for (int i = 0; i < listSqls.size(); i++) {
			String sql1 = listSqls.get(i).trim();
			if (sql1.length() == 0) {
				continue;
			}
			if (sql1.toUpperCase().indexOf("SELECT") == 0) {
				DTTable tb = DTTable.getJdbcTable(sql1, this);
				tb.setName(sql1);

				this._RequestValue.addValues(tb);
				tbs.add(tb);
			} else if (sql1.toUpperCase().indexOf("CALL ") == 0) {
				// 存储过程
				HashMap<String, Object> outparams = this.executeProcdure(sql1);
				for (String key : outparams.keySet()) {
					Object val = outparams.get(key);
					this._RequestValue.addValue(key, val, PageValueTag.DTTABLE);
				}
			} else {
				if (this.executeAutoFieldReturnName(sql1) == null) {
					this.executeUpdate(sql1);
				}
			}
			String rst = this.getErrorMsg();
			if (rst != null) {
				this.close();
				throw new Exception(rst);
			}
		}
		return tbs;
	}

	/**
	 * 执行查询并返回 自动增加值（返回为对象，需要自己判断是int还是long）
	 * 
	 * @param sql
	 * @return
	 */
	public Object executeUpdateReturnAutoIncrementObject(String sql) {
		if (this._DebugFrames != null) {
			this._DebugFrames.addDebug(this, "SQL", "[executeUpdate(sql,rv)] Prepare update. (" + sql + ")");
		}
		sql = sql + "\n\n\n"; // 避免出现被注释掉 -- auto MEMO_ID select SCOPE_IDENTITY() AS GENERATED_KEYS
		String sql1;
		try {
			sql1 = rebuildSql(sql);
		} catch (Exception e) {
			writeDebug(this, "ERR", "[executeQuery(sql,rv)] <font color=red>" + e.getMessage() + "</font>" + ")");
			LOGGER.error(e.getLocalizedMessage());
			setError(e, sql);
			return -1;
		}

		this.createEwaSplitTempData(); // guolei 2015-09-08
		this.executeEwaFunctions();// guolei 2021-03-16
		MListStr parameters = Utils.getParameters(sql1, "@");
		Object autoKey = -1;
		try {
			sql1 = replaceSqlParameters(sql1);
			this.writeDebug(this, "SQL", "[executeUpdate(sql,rv)] Start update. (" + sql1 + ")");

			// sqlserver 更换数据库
			this.useDatabase();

			closeStatment(_session.getPst());
			_session.setPst(_session.getDataHelper().getPreparedStatementAutoIncrement(sql1));
			this.writeDebug(this, "SQL", "[创建自增] PST");
			// add parameter
			addSqlParameter(parameters, _session.getPst());
			_session.getPst().executeUpdate();
			this.writeDebug(this, "SQL", "[executeUpdate(sql,rv)] End update.");

			ResultSet rs = _session.getPst().getGeneratedKeys();
			if (rs.next()) {
				autoKey = rs.getObject(1);
				if (autoKey == null) {
					this.writeDebug(this, "SQL", "[返回自增] null , SQL 没有执行成功.");
				} else {
					this.writeDebug(this, "SQL", "[返回自增] " + rs.getObject(1) + ".");
				}
			}

			if (!_session.isTrans()) {
				if (!_session.getDataHelper().getConnection().getAutoCommit()) {
					_session.getDataHelper().getConnection().commit();
				}
			}
			// 放到close处理，以便复用时不用重复创建
			// this.writeDebug(this, "SQL", "删除分割临时数据.");
			// this.clearEwaSplitTempData();

			return autoKey;
		} catch (Exception err) {
			LOGGER.error(err.getLocalizedMessage());
			this.writeDebug(this, "ERR", err.getMessage());
			setError(err, sql1);
			return -1;
		}
	}

	/**
	 * 执行自增的插入
	 * 
	 * @param sql
	 * @return
	 */
	public int executeUpdateReturnAutoIncrement(String sql) {
		Object autoInc = executeUpdateReturnAutoIncrementObject(sql);
		if (autoInc == null) {
			return -1; // 没有执行
		}
		try {
			return Integer.parseInt(autoInc.toString());
		} catch (Exception err) {
			LOGGER.error(err.getLocalizedMessage());
			this.writeDebug(this, "ERR", err.getMessage());
			setError(err, sql);
			return -1;
		}
	}

	/**
	 * 执行更新
	 * 
	 * @param sql
	 */
	public boolean executeUpdate(String sql) {
		if (this._DebugFrames != null) {
			this._DebugFrames.addDebug(this, "SQL", "[executeUpdate(sql,rv)] Prepare update. (" + sql + ")");
		}

		String sql1;
		try {
			sql1 = rebuildSql(sql);
		} catch (Exception e) {
			writeDebug(this, "ERR", "[executeQuery(sql,rv)] <font color=red>" + e.getMessage() + "</font>" + ")");
			LOGGER.error(e.getLocalizedMessage());
			setError(e, sql);
			return false;
		}

		this.createEwaSplitTempData(); // guolei 2015-09-08
		this.executeEwaFunctions(); // guolei 2021-03-16

		MListStr parameters = Utils.getParameters(sql1, "@");
		if (parameters.size() == 0) {
			return this.executeUpdateNoParameter(sql1);
		}
		sql1 = replaceSqlParameters(sql1);

		// 除去注释，没有可执行的sql
		if (!SqlUtils.checkHaveSql(sql1)) {
			this.writeDebug(this, "SQL", "[executeUpdate(sql,rv)] Not execute. (" + sql1 + ")");
			return true;
		}

		this.writeDebug(this, "SQL", "[executeUpdate(sql,rv)] Start update. (" + sql1 + ")");
		// sqlserver, mysql 更换数据库

		try {
			this.useDatabase();
			closeStatment(_session.getPst());
			_session.setPst(_session.getDataHelper().getPreparedStatement(sql1));
			// add parameter
			this.addSqlParameter(parameters, _session.getPst());
			_session.getPst().executeUpdate();

			if (sql.toLowerCase().indexOf("update") != -1 || sql.toLowerCase().indexOf("insert") != -1
					|| sql.toLowerCase().indexOf("delete") != -1) {
				this.writeDebug(this, "SQL", "[执行更新] 影响行数: " + _session.getPst().getUpdateCount());
			} else {
				logSqlWarnings(_session.getPst(), sql);
			}
			this.writeDebug(this, "SQL", "[executeUpdate(sql,rv)] End update.");
			if (!_session.isTrans()) {
				if (!_session.getDataHelper().getConnection().getAutoCommit()) {
					_session.getDataHelper().getConnection().commit();
				}
			}
			// 放到close处理，以便复用时不用重复创建
			// this.writeDebug(this, "SQL", "删除分割临时数据.");
			// this.clearEwaSplitTempData();

			return true;
		} catch (Exception err) {
			// if (_IsTrans) {// 如果事务处理开始
			// this.transRollback();
			// }
			LOGGER.error(err.getLocalizedMessage());
			LOGGER.error(sql1);
			this.writeDebug(this, "ERR", err.getMessage());
			setError(err, sql1);
			return false;
		}
	}

	/**
	 * sqlserver, mysql 更换数据库
	 * 
	 * @param sql
	 * @return
	 * @throws Exception
	 * @throws SQLException
	 */
	public boolean useDatabase() throws Exception {
		if (_session.getDataBaseName() == null || _session.getDataBaseName().trim().length() == 0) {
			return false;
		}
		String dbName = _session.getDataBaseName();
		String sql = null;
		if (SqlUtils.isMySql(this)) {
			sql = "USE `" + dbName + "`";
		} else if (SqlUtils.isSqlServer(this)) {
			sql = "USE [" + dbName + "]";
		}

		if (sql != null) {
			this.writeDebug(this, "SQL", "[useDatabase] " + sql);
			_session.useDatabase();
			return true;
		} else {
			this.writeDebug(this, "SQL", "[useDatabase] Not sqlserver or mysql [" + dbName + "].");
			return false;
		}

	}

	/**
	 * 执行更新并返回数据的前后差异
	 * 
	 * @param sql
	 * @return
	 */
	public UpdateChanges executeUpdateAndReturnChanges(String sql) {
		if (this._DebugFrames != null) {
			this._DebugFrames.addDebug(this, "SQL", "[executeUpdate(sql,rv)] Prepare update. (" + sql + ")");
		}

		String sql1;
		try {
			sql1 = rebuildSql(sql);
		} catch (Exception e) {
			writeDebug(this, "ERR", "[executeQuery(sql,rv)] <font color=red>" + e.getMessage() + "</font>" + ")");
			LOGGER.error(e.getLocalizedMessage());
			setError(e, sql);
			return null;
		}

		this.createEwaSplitTempData(); // guolei 2015-09-08
		this.executeEwaFunctions();// guolei 2021-03-16

		SqlPart sp = new SqlPart();
		String sqlDbType = this.getDatabaseType().toLowerCase();

		if ("mssql".equals(sqlDbType)) {
			sqlDbType = "sqlserver";
		}
		// 解析update ，提取表名和where值
		boolean is_ok = false;
		try {
			is_ok = sp.setUpdateSql(sql1, sqlDbType);
		} catch (Exception err) {
			LOGGER.error("comparative changes {} {} {}", sqlDbType, sql, err.getMessage());
		}
		String sqlGet = "";
		DTTable tbBeforeUpdate = null;
		DTTable tbAfterUpdate = null;

		if (is_ok) {
			sqlGet = "select * from " + sp.getTableName() + " where " + sp.getWhere();
			tbBeforeUpdate = DTTable.getJdbcTable(sqlGet, this);
		}

		MListStr parameters = Utils.getParameters(sql1, "@");
		try {
			sql1 = replaceSqlParameters(sql1);
			this.writeDebug(this, "SQL", "[executeUpdate(sql,rv)] Start update. (" + sql1 + ")");

			// sqlserver 更换数据库
			this.useDatabase();

			closeStatment(_session.getPst());
			_session.setPst(_session.getDataHelper().getPreparedStatement(sql1));
			// add parameter
			addSqlParameter(parameters, _session.getPst());
			_session.getPst().executeUpdate();
			this.writeDebug(this, "SQL", "[executeUpdate(sql,rv)] End update.");
			if (!_session.isTrans()) {
				if (!_session.getDataHelper().getConnection().getAutoCommit()) {
					_session.getDataHelper().getConnection().commit();
				}
			}
			// 放到close处理，以便复用时不用重复创建
			// this.writeDebug(this, "SQL", "删除分割临时数据.");
			// this.clearEwaSplitTempData();

			if (is_ok) {
				tbAfterUpdate = DTTable.getJdbcTable(sqlGet, this);

				UpdateChanges ucs = new UpdateChanges();
				ucs.setTbAfter(tbAfterUpdate);
				ucs.setTbBefore(tbBeforeUpdate);
				ucs.setSqlPart(sp);
				return ucs;
			}

			return null;
		} catch (Exception err) {
			// if (_IsTrans) {// 如果事务处理开始
			// this.transRollback();
			// }
			LOGGER.error(err.getLocalizedMessage());
			LOGGER.error(sql1);
			this.writeDebug(this, "ERR", err.getMessage());
			setError(err, sql1);
			return null;
		}
	}

	public boolean executeUpdateNoParameter(String sql) {
		// 除去注释，没有可执行的sql
		if (!SqlUtils.checkHaveSql(sql)) {
			this.writeDebug(this, "SQL", "[executeUpdate(sql,rv)] Not execute. (" + sql + ")");
			return true;
		}
		// sqlserver, mysql 更换数据库 USE xxxx
		try {
			this.useDatabase();
			this.writeDebug(this, "SQL", "[executeUpdateNoParameter(sql)] update. (" + sql + ")");
			Statement st = _session.getDataHelper().getStatement();
			st.executeUpdate(sql);

			if (sql.toLowerCase().indexOf("update") != -1 || sql.toLowerCase().indexOf("insert") != -1
					|| sql.toLowerCase().indexOf("delete") != -1) {
				this.writeDebug(this, "SQL", "[执行更新] 影响行数: " + st.getUpdateCount());
			} else {
				logSqlWarnings(st, sql);
			}

			this.writeDebug(this, "SQL", "[executeUpdateNoParameter(sql,rv)] End update.");
			if (!_session.isTrans()) {
				if (!_session.getDataHelper().getConnection().getAutoCommit()) {
					_session.getDataHelper().getConnection().commit();
				}
			}
			closeStatment(st);
			return true;
		} catch (Exception err) {
			// if (_IsTrans) {// 如果事务处理开始
			// this.transRollback();
			// }
			LOGGER.error(err.getLocalizedMessage());
			LOGGER.error(sql);
			this.writeDebug(this, "ERR", err.getMessage());
			setError(err, sql);
			return false;
		}
	}

	/**
	 * 重新组合SQL，获取记录数，创建出SELECT COUNT(*) GDX FROM ...
	 * 
	 * @param sql
	 * @return
	 */
	public int getRecordCount(String sql) {
		SqlPart sp = new SqlPart();
		sp.setSql(sql);

		StringBuilder sb = new StringBuilder();
		if (sp.isHasWithBlock()) {
			sb.append(sp.getWithBlock()).append("\n");
		}
		sb.append("SELECT COUNT(*) GDX FROM \n");
		if (sp.getGroupBy().length() > 0) {
			sb.append("(").append(sql).append(") tmp");
		} else {
			sb.append(sp.getTableName());

			if (!sp.getWhere().equals("")) {
				sb.append(" WHERE ");
				sb.append(sp.getWhere());
			}

		}
		int m1 = 0;
		if (executeQuery(sb.toString())) {
			int rsIndex = _session.getResultSetList().size() - 1;
			ResultSet rs = ((DataResult) _session.getResultSetList().get(rsIndex)).getResultSet();
			try {
				rs.next();
				m1 = rs.getInt("GDX");
			} catch (Exception err) {
				LOGGER.error(err.getLocalizedMessage());
				LOGGER.error(sb.toString());
				this.setError(err, sb.toString());
				m1 = -1;
			} finally {
				try {
					rs.close();
					rs = null;
				} catch (SQLException e) {
					LOGGER.error(e.getLocalizedMessage());
				}
				_session.getResultSetList().removeAt(rsIndex);
			}
		}
		return m1;
	}

	void writeDebug(Object obj, String eventName, String des) {
		this.showSqlDebug(obj.toString() + ": " + des);
		if (this._RequestValue != null && this._RequestValue.getString(FrameParameters.EWA_DB_LOG) != null) {
			String x = this._RequestValue.getString(FrameParameters.XMLNAME);
			String i = this._RequestValue.getString(FrameParameters.ITEMNAME);
			String COMBINE_ID = this._RequestValue.getString("COMBINE_ID");
			// if (x != null && i != null && des.indexOf("executeUpdate") > 0) {
			String log = "X=" + x + ", I=" + i + ", COMBINE_ID=" + (COMBINE_ID == null ? "" : COMBINE_ID) + " : " + des;
			// String name = this._RequestValue.getString("EWA_DB_LOG")
			// .replace("/", "").replace("\\", "").replace(".", "")
			// + ".log";
			// String path = UPath.getPATH_IMG_CACHE() + "/" + name;
			// File f=new File(path);
			// if(f.exists()){
			// String cnt=UFile.readFileText(f.getAbsolutePath());
			// }
			// if (des.indexOf("End update") > 0) {
			// System.out.println(this._RequestValue.listValues(false));
			// }
			LOGGER.debug(log);
			// }
		}
		if (this._DebugFrames == null)
			return;
		this._DebugFrames.addDebug(obj, eventName, des);
	}

	/**
	 * 特殊的不需要替换为参数的参数名称，例如rownum<br>
	 * <code>select user_id, @rownum := @rownum+1 from users b<br>
	 * ,(select @rownum :=0) c
	 * </code>
	 * 
	 * @param paramName
	 * @return
	 */
	public boolean skipReplaceParameter(String paramName) {
		return _sqlBuilder.skipReplaceParameter(paramName);
	}

	/**
	 * 1. 合成SQL语句，如果参数名为XX_SPLIT，为分割参数, <br>
	 * 例如 select * from users where id in(1,2,3) <br>
	 * 2. SQL语句预替换，即在SQL语句执行前，替换SQL语句本身的参数 <br>
	 * 例如 SELECT * FROM ~TB, 如参数TB是 USERS 则替换成 SELECT * FROM USERS
	 *
	 * @param sql
	 * @param requestValue
	 * @return
	 * @throws Exception
	 */
	public String rebuildSql(String sql) throws Exception {
		return _sqlBuilder.rebuildSql(sql);
	}

	/**
	 * 替换SQL中的参数名为“？”,不替换rownum开头的参数，mysql使用
	 * 
	 * @param sql sql表达式
	 * @return 替换后的sql
	 */
	public String replaceSqlParameters(String sql) {
		return _sqlBuilder.replaceSqlParameters(sql);
	}

	/**
	 * 替换select 查询中的参数名为具体值，原因是sqlserver在查询中出现参数表达式，会进行全表扫描<br>
	 * 郭磊 2016-11-02
	 * 
	 * @param sql sql表达式
	 * @return 替换后的sql
	 */
	public String replaceSqlSelectParameters(String sql) {
		return _sqlBuilder.replaceSqlSelectParameters(sql);
	}

	/**
	 * 替换select 查询中的参数名为具体值，原因是sqlserver在查询中出现参数表达式，会进行全表扫描<br>
	 * 郭磊 2016-11-02
	 * 
	 * @param sql sql表达式
	 * @return 替换后的sql
	 */
	public String replaceSqlSelectParameters(String sql, String databaseType) {
		return _sqlBuilder.replaceSqlSelectParameters(sql, databaseType);
	}

	/**
	 * 获取参数值表达式，用于select替换
	 * 
	 * @param paramName 参数名称
	 * @return
	 */
	public String getReplaceParameterValueExp(String paramName) {
		return _sqlBuilder.getReplaceParameterValueExp(paramName);
	}

	/**
	 * 获取参数值表达式，用于select替换
	 * 
	 * @param paramName    参数名称
	 * @param databaseType 数据库类型
	 * @return
	 */
	public String getReplaceParameterValueExp(String paramName, String databaseType) {
		return _sqlBuilder.getReplaceParameterValueExp(paramName, databaseType);
	}

	/**
	 * 设置SQL参数
	 * 
	 * @param parameters
	 * @param pst
	 * @throws SQLException
	 */
	public void addSqlParameter(MListStr parameters, PreparedStatement pst) throws SQLException {
		_sqlBuilder.addSqlParameter(parameters, pst);
	}

	public PageValue getParameterByEndWithType(String parameterName) {
		return _sqlBuilder.getParameterByEndWithType(parameterName);
	}

	/**
	 * Bind a single named parameter to a PreparedStatement at the given index.
	 * <p>Optimized: debug strings only built when _DebugFrames is active;
	 * null-safe on _RequestValue; constant-left .equals().
	 */
	public void addStatementParameter(PreparedStatement cst, String parameterName, int index) throws SQLException {
		_sqlBuilder.addStatementParameter(cst, parameterName, index);
	}

	/**
	 * 清除所有返回的 resultSet，当长期执行查询的时，会造成内存占用过高 关闭连接会自动执行清除
	 */
	public void clearResultSets() {
		_session.clearResultSets();
	}

	/**
	 * 关闭所有连接,包括ResultSet，Cst，Pst
	 */
	public void close() {
		// 放到close处理，以便复用时不用重复创建
		_sqlBuilder.clearCreateSplitData();
		if (_DebugFrames != null) {
			_DebugFrames.addDebug(this, "SQL", "[close] Close connection.");
		}
		_session.close();

	}

	/**
	 * Return the error with SQL
	 * 
	 * @return
	 */
	public String getErrorMsg() {
		return _errorMsg;
	}

	/**
	 * Manual set conn error message
	 * 
	 * @param errorMessage
	 */
	public void setErrorMsg(String errorMessage) {
		this._errorMsg = errorMessage;
		this._errorMsgOnly = errorMessage;
	}

	/**
	 * Clear the error
	 */
	public void clearErrorMsg() {
		this._errorMsg = null;
		this._errorMsgOnly = null;
	}

	/**
	 * Set the error and SQL
	 * 
	 * @param e
	 * @param sql
	 */
	private void setError(Exception e, String sql) {
		this._errorMsg = "SQL: " + sql + "<br>\r\nERROR: " + e.getMessage() + "<br>DATASOURCE: "
				+ _session.getCurrentConfig().getName() + "(" + _session.getConnectionString() + ")";
		this._errorMsgOnly = e.getMessage();
		// LOGGER.error(this._errorMsg);
	}

	/**
	 * Show the debug info when UPath.isDebugSql()
	 * 
	 * @param v
	 */
	private void showSqlDebug(String v) {
		if (UPath.isDebugSql()) {
			LOGGER.info(v);
		}
	}

	/**
	 * 修补存储过程的大括号 {CALL prTest(@a1, ...)}
	 * 
	 * @param sqlSource
	 * @return
	 */
	private String repaireProcdureSqlBrackets(String sqlSource) {
		String sql = sqlSource.trim();
		String chkSql = sql.toUpperCase();

		int leftBracket0 = chkSql.indexOf("{");
		int rightBracket0 = chkSql.indexOf("}");
		int call0 = chkSql.indexOf("CALL");
		if (leftBracket0 >= 0 && call0 > leftBracket0 && rightBracket0 > call0) {
			// {@id_int_out = call proc(@name, @age)}
			return sqlSource;
		}

		// ﬁ 是单个字符，toUpperCase() 后变成两个字符 FI
		if (sql.length() != chkSql.length()) {
			sql = chkSql; // 例如 ﬁ 64257
			LOGGER.warn("出现大小写长度不一致情况：" + sql);
		}

		if (chkSql.indexOf("{CALL") < 0) {
			int start = chkSql.indexOf("CALL");
			if (start >= 0) {
				int leftBracket = chkSql.indexOf("(", start);
				int rightBracket = chkSql.indexOf(")", leftBracket);
				if (leftBracket > 0 && rightBracket > leftBracket) {
					StringBuilder sbSql = new StringBuilder();

					sbSql.append("{");
					sbSql.append(sql.substring(start, rightBracket + 1));
					sbSql.append("}");

					sql = sbSql.toString();
				} else {
					// 应该是错误
				}
			} else {
				sql = "{CALL " + sql + "}";
			}
		}
		return sql;
	}

	/**
	 * 执行存储过程
	 * 
	 * @param callProcduceSql 存储过程调用方法: call pr_some(@name, @id_int_output)<br>
	 *                        结尾为_output，表示是输出参数<br>
	 *                        _int_output,表示输出参数为整形<br>
	 *                        _smallint_output,表示输出参数为小整形<br>
	 *                        _tinyint_output,表示输出参数为短整形<br>
	 *                        _number_output,_money_output,_numeric_output,_decimal_output表示输出参数为bcd数值<br>
	 *                        _long_output/_bigint_output,表示输出参数为长整形<br>
	 *                        _text_output/_clob_output,表示输出参数为长文本<br>
	 *                        _image_output/_blob_output,表示输出参数为长二进制<br>
	 *                        _binary_output/_byte_output,表示输出参数为二进制(varbinary)<br>
	 *                        _date_output/_datetime_output/_timestamp_output,表示输出参数为日期时间<br>
	 *                        _time_output,表示输出参数为时间<br>
	 *                        默认，为varchar字符串
	 * @return 输出参数Map，key转换为大写
	 */
	public HashMap<String, Object> executeProcdure(String callProcduceSql) {
		// 修补存储过程的大括号 {CALL prTest(@a1, ...)}
		String sql = this.repaireProcdureSqlBrackets(callProcduceSql);

		this.writeDebug(this, "开始执行", sql);
		String sql1;
		try {
			sql1 = rebuildSql(sql);
		} catch (Exception e) {
			writeDebug(this, "ERR",
					"[executeProcdure(callProcduceSql)] <font color=red>" + e.getMessage() + "</font>" + ")");
			LOGGER.error(e.getLocalizedMessage());
			setError(e, sql);
			return null;
		}

		MListStr al = Utils.getParameters(sql, "@");
		HashMap<String, Object> outValues = null;

		sql1 = this.replaceSqlParameters(sql);
		this.writeDebug(this, "开始执行", sql1);
		try {
			CallableStatement cst = _session.getDataHelper().getCallableStatement(sql1);
			outValues = _sqlBuilder.addSqlParameter(al, cst);
			cst.execute();

			this.getOutValues(outValues, cst);
			if (!_session.getDataHelper().getConnection().getAutoCommit()) {
				_session.getDataHelper().getConnection().commit();
			}
			cst.close();
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			LOGGER.error(sql1);
			this.setError(e, sql);
		}
		this.writeDebug(this, "执行完毕", sql1);
		return outValues;
	}

	/**
	 * 执行存储过程
	 * 
	 * @param callProcduceSql 存储过程调用方法: call pr_some(@name, @id_int_output)<br>
	 *                        结尾为_output，表示是输出参数<br>
	 *                        _int_output,表示输出参数为整形<br>
	 *                        _smallint_output,表示输出参数为小整形<br>
	 *                        _tinyint_output,表示输出参数为短整形<br>
	 *                        _number_output,_money_output,_numeric_output,_decimal_output表示输出参数为bcd数值<br>
	 *                        _long_output/_bigint_output,表示输出参数为长整形<br>
	 *                        _text_output/_clob_output,表示输出参数为长文本<br>
	 *                        _image_output/_blob_output,表示输出参数为长二进制<br>
	 *                        _binary_output/_byte_output,表示输出参数为二进制(varbinary)<br>
	 *                        _date_output/_datetime_output/_timestamp_output,表示输出参数为日期时间<br>
	 *                        _time_output,表示输出参数为时间<br>
	 *                        默认，为varchar字符串
	 * @return 输出参数Map，key转换为大写，<br>
	 *         RS_SIZE 表示返回的结果集数量<br>
	 *         返回的结果集转换为DTTable，名字以RS开头,RS0,RS1,RS2 ...
	 * @return
	 */
	public HashMap<String, Object> executeProcdureReturnResults(String callProcduceSql) {
		// 修补存储过程的大括号 {CALL prTest(@a1, ...)}
		String sql = this.repaireProcdureSqlBrackets(callProcduceSql);

		this.writeDebug(this, "开始执行", callProcduceSql);
		String sql1;
		try {
			sql1 = rebuildSql(sql);
		} catch (Exception e) {
			writeDebug(this, "ERR", "[executeProcdureReturnResults(callProcduceSql)] <font color=red>" + e.getMessage()
					+ "</font>" + ")");
			LOGGER.error(e.getLocalizedMessage());
			setError(e, sql);
			return null;
		}

		MListStr al = Utils.getParameters(sql, "@");
		HashMap<String, Object> outValues = null;

		sql1 = this.replaceSqlParameters(sql);
		this.writeDebug(this, "开始执行", sql1);
		int inc = 0;
		try {
			CallableStatement cst = _session.getDataHelper().getCallableStatement(sql1);
			_session.setQueryStatement(cst);

			outValues = _sqlBuilder.addSqlParameter(al, cst);
			this.getOutValues(outValues, cst);

			ResultSet rs = cst.executeQuery();

			logSqlWarnings(cst, sql);

			if (!_session.getDataHelper().getConnection().getAutoCommit()) {
				_session.getDataHelper().getConnection().commit();
			}

			DataResult ds0 = this.addResult(rs, sql, sql1);
			List<DataResult> results = this.getMoreResults();
			results.add(0, ds0);
			for (int i = 0; i < results.size(); i++) {
				this.writeDebug(this, "添加返回表", "RS" + i);
				DataResult dr = results.get(i);
				DTTable tbMore = new DTTable();
				tbMore.initData(dr.getResultSet());
				outValues.put("RS" + inc, tbMore);
			}
			outValues.put("RS_SIZE", results.size());

			cst.close();
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			LOGGER.error(sql1);
			this.setError(e, sql);
		}
		this.writeDebug(this, "执行完毕", sql1);
		return outValues;
	}

	/**
	 * 执行存储过程, 不带参数
	 * 
	 * @param sql
	 * @param requestValue
	 * @return 输出参数
	 */
	public void executeProcdureNoParameter(String sql) {
		// sql = "{CALL " + sql + "}";
		// 修补存储过程的大括号 {CALL prTest(@a1, ...)}
		sql = this.repaireProcdureSqlBrackets(sql);

		this.writeDebug(this, "开始执行", sql);
		try {
			CallableStatement cst = _session.getDataHelper().getCallableStatement(sql);
			cst.execute();

			logSqlWarnings(cst, sql);

			if (!_session.getDataHelper().getConnection().getAutoCommit()) {
				_session.getDataHelper().getConnection().commit();
			}
			cst.close();
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			LOGGER.error(sql);
			this.setError(e, sql);
		}
		this.writeDebug(this, "执行完毕", sql);
	}

	private void getOutValues(HashMap<String, Object> outValues, CallableStatement cst) {
		if (outValues == null)
			return;
		Iterator<String> it = outValues.keySet().iterator();
		HashMap<String, Object> outValues1 = new HashMap<String, Object>();
		while (it.hasNext()) {
			String key = it.next();
			int index = Integer.parseInt(outValues.get(key).toString());
			try {
				Object v1 = cst.getObject(index);
				outValues1.put(key, v1);
			} catch (Exception e) {
				LOGGER.error(e.getLocalizedMessage());
			}
		}
		outValues.clear();
		for (String key : outValues1.keySet()) {
			outValues.put(key, outValues1.get(key));
		}
	}

	/**
	 * SQL字符串参数值表达式，替换'号和\转译符号（mysql）
	 * 
	 * @param parameter 字符串参数值
	 * @return
	 */
	public String sqlParameterStringExp(String parameter) {
		return _sqlBuilder.sqlParameterStringExp(parameter);
	}

	/**
	 * SQL字符串参数值表达式，替换'号和\转译符号（mysql）
	 * 
	 * @param parameter 字符串参数值
	 * @return
	 */
	public String sqlParameterStringExp(String parameter, String databaseType) {
		return _sqlBuilder.sqlParameterStringExp(parameter, databaseType);
	}

	/**
	 * 处理字段或表名的字符串表达式，带上[(sqlserver)或`(mysql)
	 * 
	 * @param fieldOrTable 字段或表名
	 * @return 带上[(sqlserver)或`(mysql)的字段或表名
	 */
	public String sqlFieldOrTableExp(String fieldOrTable) {
		return _sqlBuilder.sqlFieldOrTableExp(fieldOrTable);
	}

	public String getConnectionString() {
		return _session.getConnectionString();
	}

	public String getDatabaseType() {
		return _session.getDatabaseType();
	}

	/**
	 * 获取ListFrame查询的日期表达式
	 * 
	 * @param s1
	 * @return
	 */
	public String getDateTimePara(String s1) {
		return _sqlBuilder.getDateTimePara(s1);
	}

	/**
	 * 获取ListFrame查询的日期表达式，ISO 8601 格式（YYYY-MM-DD HH:MI:SS），<br>
	 * oracle 使用to_date('YYYY-MM-DD HH24:MI:SS')
	 * 
	 * @param dt
	 * @return
	 */
	public String getDateTimePara(java.sql.Timestamp dt) {
		return _sqlBuilder.getDateTimePara(dt);
	}

	/**
	 * 获取sql的timestamp数据
	 * 
	 * @param s1
	 * @return
	 */
	public java.sql.Timestamp getTimestamp(String s1) {
		return _sqlBuilder.getTimestamp(s1);
	}

	public String getSchemaName() {
		return _session.getSchemaName();
	}

	public Connection getConnection() {
		return _session.getConnection();
	}

	public void setConnection(Connection cnn) {
		_session.setConnection(cnn);
	}

	/**
	 * @param currentConfig the _CurrentConfig to set
	 */
	public void setCurrentConfig(ConnectionConfig currentConfig) {
		_session.setCurrentConfig(currentConfig);
		initConnection();
	}

	/**
	 * @return the _DebugFrames
	 */
	public DebugFrames getDebugFrames() {
		return _DebugFrames;
	}

	/**
	 * @param debugFrames the _DebugFrames to set
	 */
	public void setDebugFrames(DebugFrames debugFrames) {
		_DebugFrames = debugFrames;
	}

	/**
	 * @return the _RequestValue
	 */
	public RequestValue getRequestValue() {
		return _RequestValue;
	}

	/**
	 * @param requestValue the _RequestValue to set
	 */
	public void setRequestValue(RequestValue requestValue) {
		if (this._RequestValue != null) {
			this._RequestValue = null;
		}
		_RequestValue = requestValue;
	}

	/**
	 * @return the _ResultSetList
	 */
	public MList getResultSetList() {
		return _session.getResultSetList();
	}

	public DataResult getLastResult() {
		return _session.getLastResult();
	}

	/**
	 * 是否使用事物处理
	 * 
	 * @return the _IsTrans
	 */
	public boolean isTrans() {
		return _session.isTrans();
	}

	/**
	 * Return the error without sql
	 * 
	 * @return
	 */
	public String getErrorMsgOnly() {
		return _errorMsgOnly;
	}

	/**
	 * @return the DataHelper
	 */
	public DataHelper getDataHelper() {
		return _session.getDataHelper();
	}

	/**
	 * @param dataHelper the DataHelper to set
	 */
	public void setDataHelper(DataHelper dataHelper) {
		_session.setDataHelper(dataHelper);
	}

	/**
	 * executeUpdateBatch 批处理更新后产生的所有表
	 * 
	 * @return the updateBatchTables
	 */
	public List<DTTable> getUpdateBatchTables() {
		return updateBatchTables;
	}
}
