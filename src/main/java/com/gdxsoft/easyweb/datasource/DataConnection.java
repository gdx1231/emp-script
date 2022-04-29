package com.gdxsoft.easyweb.datasource;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConnectionConfig;
import com.gdxsoft.easyweb.conf.ConnectionConfigs;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.debug.DebugFrames;
import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.PageValueTag;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MList;
import com.gdxsoft.easyweb.utils.msnet.MListStr;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class DataConnection {
	private static Logger LOGGER = LoggerFactory.getLogger(DataConnection.class);

	private DataHelper _ds;
	private CallableStatement _cst;
	private PreparedStatement _pst;

	private String _errorMsg; // 错误信息和 SQL
	private String _errorMsgOnly; // 只有错误信息

	private MList _ResultSetList = new MList();
	private String _DatabaseType;
	private String _SchemaName;
	private String _ConnectionString;
	private ConnectionConfigs _Configs;
	private ConnectionConfig _CurrentConfig;
	private Connection _Connection;
	private DebugFrames _DebugFrames;
	private RequestValue _RequestValue;
	private boolean _IsTrans;
	private String _DataBaseName;

	// 用于分割字符串，生成临时数据
	private CreateSplitData _CreateSplitData;

	private int _TimeDiffMinutes; // 用户和系统的时差

	private EwaSqlFunctions ewaSqlFunctions;

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

	public static String updateAndClose(StringBuilder sb, String configName, RequestValue rv) {
		return updateAndClose(sb.toString(), configName, rv);
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
	 * 查找自增的sql的返回字段, 例如 -- auto MEMO_ID
	 * 
	 * @param sql
	 * @return
	 */
	public static String getAutoField(String sql) {
		// 查找自增的sql, 例如 -- auto MEMO_ID
		String[] sqls = sql.split("\n");
		String auto_field = null;
		// 从后向前找
		for (int m = sqls.length - 1; m >= 0; m--) {
			String len = sqls[m].trim().toUpperCase();
			if (len.startsWith("--")) {
				len = len.replace("--", "").trim();
				// 第3个的空格是 32 &nbsp;
				if (len.indexOf("AUTO ") >= 0 || len.indexOf("AUTO\t") >= 0 || len.indexOf("AUTO ") >= 0) {
					auto_field = len.replaceFirst("AUTO", "").trim();
					return auto_field;
				}
			}
		}
		return null;
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
	 * 检查是否为 存储过程，例如 CALL pr_batAdd(@a)
	 * 
	 * @param sql
	 * @return
	 */
	public static boolean checkIsSelect(String sql) {
		return checkStartWord(sql, "SELECT");
	}

	/**
	 * 判断 特定字符出现在非注释的SQL的0位置，多行的SQL只进行第一次判断
	 * 
	 * @param sql
	 * @param word 关键单词，例如SELECT
	 * @return
	 */
	public static boolean checkStartWord(String sql, String word) {
		if (word == null || word.trim().length() == 0) {
			return false;
		}
		if (sql == null || sql.trim().length() == 0) {
			return false;
		}
		// 删除sql 的多行备注 /* */
		sql = removeSqlMuitiComment(sql);

		String[] sqls = sql.split("\n");
		word = word.toUpperCase().trim();
		String chk0 = word + " ";
		String chk1 = word + "\t";
		String chk2 = word + " "; // 空格是 32 &nbsp;
		for (int m = 0; m < sqls.length; m++) {
			String line = sqls[m].trim().toUpperCase();
			if (line.length() == 0 || line.startsWith("--")) {
				// 空行和 -- 注释
				continue;
			}

			if (line.indexOf(chk0) == 0 || line.indexOf(chk1) == 0 || line.indexOf(chk2) == 0 || line.equals(word)) {
				// 非注释和空行的第一次出现进行判断
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	/**
	 * 删除sql 的多行备注
	 * 
	 * @param sql
	 * @return
	 */
	public static String removeSqlMuitiComment(String sql) {
		// 删除sql 的多行备注 /* */
		Pattern pat = Pattern.compile("(\\/\\*[^\\/]*\\*\\/)", Pattern.CASE_INSENSITIVE);
		Matcher mat = pat.matcher(sql);
		String s1 = mat.replaceAll("");
		return s1;
	}

	/**
	 * 是否比较更新前和更新后字段的变化, 方式：<br>
	 * SQL 添加 -- COMPARATIVE_CHANGES
	 * 
	 * @param sql
	 * @return
	 */
	public static boolean isComparativeChanges(String sql) {
		// 记录更新前后变化的 ，方式 -- COMPARATIVE_CHANGES
		String[] sqls = sql.split("\n");
		for (int m = sqls.length - 1; m >= 0; m--) {
			String len = sqls[m].trim().toUpperCase();
			if (len.startsWith("--")) {
				len = len.replace("--", "").trim();
				if (len.equalsIgnoreCase("COMPARATIVE_CHANGES")) {
					// Comparative changes
					return true;
				}
			}
		}
		return false;
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
		return _DataBaseName;
	}

	/**
	 * 数据库名称
	 * 
	 * @param dataBaseName
	 */
	public void setDataBaseName(String dataBaseName) {
		_DataBaseName = dataBaseName;
	}

	/**
	 * 开始事务处理
	 * 
	 * @return
	 */
	public boolean transBegin() {
		try {
			_ds.connect();
			_Connection = _ds.getConnection();
		} catch (Exception e2) {
			_IsTrans = false;
			LOGGER.error(e2.getLocalizedMessage());
			this.setError(e2, "连接到数据库");
			return false;
		}
		try {
			_IsTrans = true;
			_Connection.setAutoCommit(false);
			LOGGER.debug("Start tansaction");
			return true;
		} catch (SQLException e) {
			_IsTrans = false;
			try {
				_Connection.close();
			} catch (SQLException e1) {
				LOGGER.error(e1.getLocalizedMessage());
				this.setError(e1, "关闭错误的事务");
			}
			LOGGER.error(e.getLocalizedMessage());
			this.setError(e, "事务开始错误");
			return false;
		}
	}

	/**
	 * 提交事务
	 * 
	 * @throws SQLException
	 */
	public boolean transCommit() {
		try {
			this._Connection.commit();
			LOGGER.debug("Commit tansaction");
			return true;
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			this.setError(e, "提交事务");
			this.close();
			return false;
		} finally {
			this._IsTrans = false;
		}

	}

	/**
	 * 事务回滚
	 * 
	 * @throws SQLException
	 */
	public void transRollback() {
		try {
			this._Connection.rollback();
			LOGGER.debug("Rollback tansaction");
		} catch (SQLException e) {
			LOGGER.error(e.getLocalizedMessage());
			this.setError(e, "回滚事务");
		} finally {
			this.close();
		}
	}

	/**
	 * 关闭事务连接
	 */
	public void transClose() {
		this.close();
	}

	public ConnectionConfig getCurrentConfig() {
		return this._CurrentConfig;
	}

	public DataConnection() {
		try {
			_Configs = ConnectionConfigs.instance();
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			this._errorMsg = e.getMessage();
		}

	}

	public DataConnection(RequestValue rv) {
		try {
			_Configs = ConnectionConfigs.instance();

			this.setConfigName("");
			this.setRequestValue(rv);

		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			this._errorMsg = e.getMessage();
		}

	}

	public DataConnection(String configName, RequestValue rv) {
		try {
			_Configs = ConnectionConfigs.instance();

			this.setConfigName(configName);
			this.setRequestValue(rv);

		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			this._errorMsg = e.getMessage();
		}

	}

	public void setConfigName(String configName) {
		if (configName == null || !this._Configs.containsKey(configName.trim().toLowerCase())) {
			// 第一个数据库配置
			_CurrentConfig = this._Configs.getConfig(0);
		} else {
			_CurrentConfig = this._Configs.get(configName.trim().toLowerCase());
		}
		initConnection();
	}

	/**
	 * 设置数据库连接池句柄
	 * 
	 * @param configName
	 * @param dataBaseName
	 */
	public void setConfigName(String configName, String dataBaseName) {
		this.setConfigName(configName);

		initConnection();
	}

	public boolean connect() {
		try {
			_ds.connect();
			return true;
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			return false;
		}
	}

	private void initConnection() {
		if (this._DebugFrames != null) {
			this._DebugFrames.addDebug(this, "SQL", "[initConnection()] Start building connection. ("
					+ this._CurrentConfig.getConnectionString() + ")");
		}

		_ds = new DataHelper(this._CurrentConfig);

		if (this._DebugFrames != null) {
			this._DebugFrames.addDebug(this, "SQL",
					"[initConnection()] build a connection. (" + this._CurrentConfig.getConnectionString() + ")");
		}
		this._ConnectionString = this._CurrentConfig.getConnectionString();
		this._SchemaName = this._CurrentConfig.getSchemaName();
		this._DatabaseType = this._CurrentConfig.getType();

	}

	public boolean executeQueryNoParameter(String sql) {
		if (this._DataBaseName != null && this._DatabaseType.equalsIgnoreCase("mssql")) {
			sql = "USE " + this._DataBaseName + ";\r\n" + sql;
		}
		StringBuilder debuginfo = new StringBuilder();
		debuginfo.append("[executeQuery(sql)] Start excute query. \n\n");
		debuginfo.append(sql);
		writeDebug(this, "SQL", debuginfo.toString());

		try {
			sql = this.rebuildSql(sql);

			_ds.connect();
			ResultSet rs = _ds.getStatement().executeQuery(sql);
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

	private void addResult(ResultSet rs, String sqlExecute, String sqlOrigin) {
		DataResult ds = new DataResult();
		ds.setIsEof(false);
		ds.setIsNext(false);
		ds.setResultSet(rs);

		ds.setSqlExecute(sqlExecute);
		ds.setSqlOrigin(sqlOrigin);
		this._ResultSetList.add(ds);
	}

	private void executeEwaFunctions() {
		if (this.ewaSqlFunctions == null || this.ewaSqlFunctions.getTempData().size() == 0) {
			return;
		}
		this.ewaSqlFunctions.executeEwaFunctions(_RequestValue, this._DebugFrames);
	}

	/**
	 * 执行query
	 * 
	 * @param sql    执行的SQL语句
	 * @param oriSql 原始SQL语句
	 * @return
	 */
	private boolean executeQuery(String sql, String oriSql) {
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
			if (this._ds == null) {
				// 没有设置configName,取第一个配置
				this.setConfigName(null);
			}

			if (this._DataBaseName != null && this._DatabaseType.equalsIgnoreCase("mssql")) {
				sql1 = "USE " + this._DataBaseName + ";\r\n" + sql1;
			}
			ResultSet rs;
			if (parameters.size() > 0) {
				_pst = this._ds.getPreparedStatement(sql1);

				this._errorMsg = null;

				// add parameter
				addSqlParameter(parameters, _pst);
				rs = _pst.executeQuery();
			} else {
				_ds.connect();
				this._errorMsg = null;

				rs = _ds.getStatement().executeQuery(sql1);
			}
			this.addResult(rs, sql, oriSql);
			writeDebug(this, "SQL", "[executeQuery(sql,rv)] End query.");
			return true;
		} catch (Exception e) {
			writeDebug(this, "ERR", "[executeQuery(sql,rv)] <font color=red>" + e.getMessage() + "</font>" + ")");
			LOGGER.error(e.getLocalizedMessage());
			LOGGER.error(sql1);
			setError(e, sql1);
			return false;
		} finally {
			this.clearEwaSplitTempData();
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
		if (this._DatabaseType.equals("ORACLE")) {
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
		} else if (this._DatabaseType.equals("MSSQL")) {
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
				sb.insert(0, "SELECT TOP " + pageSize + " ");
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
			/// 20190816
			/*
			 * if (sp.getOrderBy().length() > 0) { sqlTmp.append("\r\n ORDER BY " +
			 * sp.getOrderBy()); } sb.insert(0, "SELECT TOP " + currentPage * pageSize +
			 * " "); sb.append(sp.getFields()); sb.append("\r\n FROM ");
			 * sb.append(sp.getTableName()); if (currentPage == 1) { sb.append("\r\n WHERE "
			 * + sp.getWhere()); sb.append(sqlTmp.toString()); } else { String pk =
			 * "GGDDXX." + pkFieldName; String pk1 = pkFieldName; if
			 * (pkFieldName.indexOf(",") > 0) { String[] pks = pkFieldName.split(",");
			 * String tmp = ""; String tmp1 = ""; for (int i = 0; i < pks.length; i++) { if
			 * (i > 0) { tmp += " + '~`" + i + "!' + "; tmp1 += " + '~`" + i + "!' + "; }
			 * String[] filedName = pks[i].trim().split("\\."); String name = "GGDDXX.[" +
			 * filedName[filedName.length - 1] + "]"; tmp += "ISNULL(CONVERT(VARCHAR(222),"
			 * + name + "),'--null--')"; tmp1 += "ISNULL(CONVERT(VARCHAR(222), " +
			 * pks[i].trim() + "),'--null--')"; } pk = tmp; pk1 = tmp1; } else { String[]
			 * filedName = pkFieldName.trim().split("\\."); pk = "GGDDXX." +
			 * filedName[filedName.length - 1];
			 * 
			 * }
			 * 
			 * StringBuilder where = new StringBuilder(); where.append("SELECT TOP " +
			 * (currentPage - 1) * pageSize + " "); where.append(pk1 + " FROM ");
			 * where.append(sp.getTableName()); where.append("\r\n WHERE " + sp.getWhere());
			 * where.append(sqlTmp.toString());
			 * 
			 * sb.append(" WHERE " + sp.getWhere()); sb.append(sqlTmp.toString());
			 * 
			 * sb.insert(0, "SELECT * FROM(\r\n"); sb.append(") GGDDXX\r\n WHERE NOT " + pk
			 * + " IN (\r\n\t"); sb.append(where.toString()); sb.append(")"); }
			 */

		} else if (this._DatabaseType.equals("HSQLDB") || this._DatabaseType.equals("MYSQL")) {
			sb.append(sql);
			sb.append(" limit " + pageSize + " offset " + (currentPage - 1) * pageSize);
		}
		String sqla = sb.toString();
		return this.executeQuery(sqla, sql);

	}

	/**
	 * 删除临时数据
	 */
	private void clearEwaSplitTempData() {
		if (this._CreateSplitData == null) {
			return;
		}
		if (this._CreateSplitData.getTempData() == null) {
			return;
		}
		if (this._CreateSplitData.getTempData().size() == 0) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("delete from _EWA_SPT_DATA where tag in (");
		int i = 0;
		for (String key : this._CreateSplitData.getTempData().keySet()) {
			if (i > 0) {
				sb.append(",");
			}
			i++;
			String keyExp = this.sqlParameterStringExp(key);
			sb.append(keyExp);
		}
		sb.append(")");
		String sqlDelete = sb.toString();
		this.executeUpdateNoParameter(sqlDelete);
	}

	public String closeStatment(Statement stmt) {
		if (stmt == null) {
			return null;
		}
		try {
			stmt.close();
			return null;
		} catch (SQLException e) {
			String err = e.getLocalizedMessage();
			LOGGER.error("Close the statment {}", err);
			this.writeDebug(this, "ERR", err);
			setError(e, "Close the statment");

			return err;
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
			if (!haveData) { // 有数据
				stmt.executeBatch();
				if (transcation) {
					this.transCommit();
				} else {
					if (!this._Connection.getAutoCommit()) {
						this._Connection.commit();
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
	 * 将字符串分割成表数据
	 */
//	private void createEwaSplitTempData1() {
//		if (this._CreateSplitData == null) {
//			return;
//		}
//		if (this._CreateSplitData.getTempData() == null) {
//			return;
//		}
//		if (this._CreateSplitData.getTempData().size() == 0) {
//			return;
//		}
//		this.connect();
//		this.getConnection();
//		boolean isMysql = this.getDatabaseType().equals("MYSQL");
//		Statement stmt = null;
//		try {
//			stmt = _Connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
//
//			for (String key : this._CreateSplitData.getTempData().keySet()) {
//				ArrayList<String> al = this._CreateSplitData.getTempData().get(key);
//				if (isMysql) {
//					// mysql 批量更新
//					StringBuilder sb = new StringBuilder();
//					sb.append("insert into _EWA_SPT_DATA(idx,col,tag) values\n");
//					for (int i = 0; i < al.size(); i++) {
//						if (i > 0) {
//							sb.append(",\n");
//						}
//						sb.append("(");
//						sb.append(i);
//						sb.append(",'");
//						sb.append(al.get(i).replace("'", "''"));
//						sb.append("','");
//						sb.append(key.replace("'", "''"));
//						sb.append("')");
//					}
//					String sql1 = sb.toString();
//					stmt.addBatch(sql1);
//				} else {
//					for (int i = 0; i < al.size(); i++) {
//						StringBuilder sb = new StringBuilder();
//						sb.append("insert into _EWA_SPT_DATA(idx,col,tag) values(");
//						sb.append(i);
//						sb.append(",'");
//						sb.append(al.get(i).replace("'", "''"));
//						sb.append("','");
//						sb.append(key.replace("'", "''"));
//						sb.append("')");
//						String sql1 = sb.toString();
//
//						// System.out.println(sql1);
//
//						stmt.addBatch(sql1);
//					}
//				}
//			}
//			stmt.executeBatch();
//			if (!this._Connection.getAutoCommit()) {
//				this._Connection.commit();
//			}
//		} catch (SQLException e) {
//			LOGGER.error(e.getLocalizedMessage());
//		} finally {
//			if (stmt != null) {
//				try {
//					stmt.close();
//				} catch (SQLException e) {
//					LOGGER.error(e.getLocalizedMessage());
//				}
//			}
//		}
//
//	}

	/**
	 * 将字符串分割成表数据，字符串限制长度1000
	 */
	private void createEwaSplitTempData() {
		if (this._CreateSplitData == null) {
			return;
		}
		if (this._CreateSplitData.getTempData() == null) {
			return;
		}
		if (this._CreateSplitData.getTempData().size() == 0) {
			return;
		}
		boolean isMysql = this.getDatabaseType().equalsIgnoreCase("MYSQL");
		String insertHeader = "insert into _EWA_SPT_DATA(idx, col, tag) values ";
		List<String> values = new ArrayList<String>();
		for (String key : this._CreateSplitData.getTempData().keySet()) {
			ArrayList<String> al = this._CreateSplitData.getTempData().get(key);
			String keyExp = this.sqlParameterStringExp(key);
			for (int i = 0; i < al.size(); i++) {
				StringBuilder sb = new StringBuilder();
				sb.append("(");
				sb.append(i);
				sb.append(", ");

				String col = al.get(i);
				if (isMysql && col.length() > 1000) {
					col = col.substring(0, 1000);
					LOGGER.warn("EwaSplitTempData col size > 1000, truncation");
				}
				String colExp = this.sqlParameterStringExp(col);
				sb.append(colExp);

				sb.append(",");
				sb.append(keyExp);
				sb.append(")");

				values.add(sb.toString());
			}

		}
		BatchInsert bi = new BatchInsert(this, false);

		String result = bi.insertBatch(insertHeader, values);
		if (result != null) {
			LOGGER.error(result);
		}
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
	 * 批量执行sql语句
	 * 
	 * @param sqls
	 * @return
	 */
	public int executeUpdateBatch(List<String> sqls) {
		int runInc = 0;
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
	 * 批处理执行sql语句，sql语句用 ; 分割
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
				HashMap<String, String> outparams = this.executeProcdure(sql1);
				for (String key : outparams.keySet()) {
					String val = outparams.get(key);
					this._RequestValue.addValue(key, val, PageValueTag.DTTABLE);
				}
			} else {
				String auto_field = DataConnection.getAutoField(sql1);
				if (auto_field != null && auto_field.length() > 0) {
					// 执行自增的插入
					int autov = this.executeUpdateReturnAutoIncrement(sql1);
					if (autov > -1) {
						this._RequestValue.addValue(auto_field, autov, PageValueTag.DTTABLE);
					}
				} else {
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
			return false;
		}

		this.createEwaSplitTempData(); // guolei 2015-09-08
		this.executeEwaFunctions();// guolei 2021-03-16
		MListStr parameters = Utils.getParameters(sql1, "@");
		Object autoKey = -1;
		try {
			sql1 = replaceSqlParameters(sql1);
			this.writeDebug(this, "SQL", "[executeUpdate(sql,rv)] Start update. (" + sql1 + ")");

			// sqlserver 更换数据库
			if (this._DataBaseName != null && this._DatabaseType.equalsIgnoreCase("mssql")) {
				sql1 = "USE " + this._DataBaseName + ";\r\n" + sql1;
			}

			_pst = this._ds.getPreparedStatementAutoIncrement(sql1);
			this.writeDebug(this, "SQL", "[创建自增] PST");
			// add parameter
			addSqlParameter(parameters, _pst);
			_pst.executeUpdate();
			this.writeDebug(this, "SQL", "[executeUpdate(sql,rv)] End update.");

			ResultSet rs = _pst.getGeneratedKeys();
			if (rs.next()) {
				autoKey = rs.getObject(1);
				if (autoKey == null) {
					this.writeDebug(this, "SQL", "[返回自增] null , SQL 没有执行成功.");
				} else {
					this.writeDebug(this, "SQL", "[返回自增] " + rs.getObject(1) + ".");
				}
			}

			if (!this._IsTrans) {
				if (!this._ds.getConnection().getAutoCommit()) {
					this._ds.getConnection().commit();
				}
			}
			this.writeDebug(this, "SQL", "删除分割临时数据.");
			this.clearEwaSplitTempData();

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
		try {
			sql1 = replaceSqlParameters(sql1);
			this.writeDebug(this, "SQL", "[executeUpdate(sql,rv)] Start update. (" + sql1 + ")");

			// sqlserver 更换数据库
			if (this._DataBaseName != null && this._DatabaseType.equalsIgnoreCase("mssql")) {
				sql1 = "USE " + this._DataBaseName + ";\r\n" + sql1;
			}

			_pst = this._ds.getPreparedStatement(sql1);
			// add parameter
			this.addSqlParameter(parameters, _pst);
			_pst.executeUpdate();
			this.writeDebug(this, "SQL", "[executeUpdate(sql,rv)] End update.");
			if (!this._IsTrans) {
				if (!this._ds.getConnection().getAutoCommit()) {
					this._ds.getConnection().commit();
				}
			}
			this.writeDebug(this, "SQL", "删除分割临时数据.");
			this.clearEwaSplitTempData();

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
			if (this._DataBaseName != null && this._DatabaseType.equalsIgnoreCase("mssql")) {
				sql1 = "USE " + this._DataBaseName + ";\r\n" + sql1;
			}

			_pst = this._ds.getPreparedStatement(sql1);
			// add parameter
			addSqlParameter(parameters, _pst);
			_pst.executeUpdate();
			this.writeDebug(this, "SQL", "[executeUpdate(sql,rv)] End update.");
			if (!this._IsTrans) {
				if (!this._ds.getConnection().getAutoCommit()) {
					this._ds.getConnection().commit();
				}
			}
			this.writeDebug(this, "SQL", "删除分割临时数据.");
			this.clearEwaSplitTempData();

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
		// sqlserver 更换数据库
		if (this._DataBaseName != null && this._DatabaseType.equalsIgnoreCase("mssql")) {
			sql = "USE " + this._DataBaseName + ";\r\n" + sql;
		}

		this.writeDebug(this, "SQL", "[executeUpdateNoParameter(sql)] update. (" + sql + ")");
		try {

			this._ds.getStatement().executeUpdate(sql);
			this.writeDebug(this, "SQL", "[executeUpdateNoParameter(sql,rv)] End update.");
			if (!this._IsTrans) {
				if (!this._ds.getConnection().getAutoCommit()) {
					this._ds.getConnection().commit();
				}
			}
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
	 * 获取记录数
	 * 
	 * @param sql
	 * @return
	 */
	public int getRecordCount(String sql) {
		SqlPart sp = new SqlPart();
		sp.setSql(sql);

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT COUNT(*) GDX ");
		sb.append(" FROM ");
		if (sp.getGroupBy().length() > 0) {
			sb.append("( SELECT " + sp.getFields() + " FROM " + sp.getTableName());
			if (!sp.getWhere().equals("")) {
				sb.append(" WHERE ");
				sb.append(sp.getWhere());
			}
			sb.append(" GROUP BY " + sp.getGroupBy());
			if (!sp.getHaving().equals("")) {
				sb.append(" HAVING " + sp.getHaving());
			}
			sb.append(") GDX1231");
		} else {

			sb.append(sp.getTableName());

			if (!sp.getWhere().equals("")) {
				sb.append(" WHERE ");
				sb.append(sp.getWhere());
			}
			if (!sp.getGroupBy().equals("")) {
				sb.append(" GROUP BY " + sp.getGroupBy());
			}
			if (!sp.getHaving().equals("")) {
				sb.append(" HAVING " + sp.getHaving());
			}
		}
		int m1 = 0;
		if (executeQuery(sb.toString())) {
			int rsIndex = this._ResultSetList.size() - 1;
			ResultSet rs = ((DataResult) this._ResultSetList.get(rsIndex)).getResultSet();
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
				this._ResultSetList.removeAt(rsIndex);
			}
		}
		return m1;
	}

	private void writeDebug(Object obj, String eventName, String des) {
		this.showSqlDebug(obj.toString() + ": " + des);
		if (this._RequestValue != null && this._RequestValue.getString("EWA_DB_LOG") != null) {
			String x = this._RequestValue.getString("xmlname");
			String i = this._RequestValue.getString("itemname");
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
			System.out.println(log);
			// }
		}
		if (this._DebugFrames == null)
			return;
		this._DebugFrames.addDebug(obj, eventName, des);
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
		String sql1 = sql;
		// SQL语句预替换，即在SQL语句执行前，替换SQL语句本身的参数
		// 例如 SELECT * FROM ~TB, 如参数TB是 USERS 则替换成 SELECT * FROM USERS
		MListStr preReplaces = Utils.getParameters(sql, "~");
		for (int i = 0; i < preReplaces.size(); i++) {
			String para = preReplaces.get(i);
			PageValue pv = this._RequestValue.getPageValues().getValue(para);
			String v1 = pv.getStringValue();
			if (v1 == null || v1.trim().length() == 0) {
				LOGGER.error(this + ".rebuildSql, 参数" + para + "不存在");
				throw new Exception("The param (~" + para + ") not exists");
			}

			if (!(pv.getPVTag() == PageValueTag.SESSION || pv.getPVTag() == PageValueTag.SYSTEM
					|| pv.getPVTag() == PageValueTag.DTTABLE || pv.getPVTag() == PageValueTag.OTHER)) {
				// form or query传递
				if (!v1.matches("[a-zA-Z0-9_.\\-]*")) {
					if (v1.indexOf("--") >= 0) {
						LOGGER.error(this + ".rebuildSql, 参数" + para + "非法字符：" + v1);
						throw new Exception("Invalid param (~" + para + ") -> " + v1);
					}
				}
			}

			sql1 = sql1.replace("~" + para, v1);
		}

		// 分割字符串函数 ewa_split(@xxx, ',')
		if (this._RequestValue != null) {
			_CreateSplitData = new CreateSplitData(this._RequestValue);
			sql = _CreateSplitData.replaceSplitData(sql1);
			sql1 = sql;
		}

		// 查询上级或下级id
		ReverseIds reverseIds = new ReverseIds(this);
		String sqlReverseIds = reverseIds.replaceReverseIds(sql1);
		sql1 = sqlReverseIds;

		// 提取 EWA定义的 方法，在 EwaFunctions.xml中
		EwaSqlFunctions esf = new EwaSqlFunctions();
		sql1 = esf.extractEwaSqlFunctions(sql1);
		this.ewaSqlFunctions = esf;

		// 替换json表达式， 例如 EWA_JSON(FIELD_NAME,@NAME, @SEX, @AGE, @MOBILE)
		CreateJsonData createJsonData = new CreateJsonData(this._RequestValue);
		try {
			sql1 = createJsonData.replaceJsonData(sql1);
		} catch (Exception err) {
			LOGGER.error(err.getLocalizedMessage());
		}
		// 合成SQL语句，如果参数名为XX_SPLIT，为分割参数
		// 例如 select * from users where id in(1,2,3)
		MListStr paras = Utils.getParameters(sql, "@");
		for (int i = 0; i < paras.size(); i++) {
			String para = paras.get(i);
			if (para.toUpperCase().indexOf("_SPLIT") < 0)
				continue;
			String v1 = this._RequestValue.getString(para.toUpperCase());
			StringBuilder sb = new StringBuilder();
			if (v1 != null) {
				String[] v2 = v1.split(",");
				for (int m = 0; m < v2.length; m++) {
					String v3 = this.sqlParameterStringExp(v2[m]);
					if (m == 0) {
						sb.append(v3);
					} else {
						sb.append(", ");
						sb.append(v3);
					}
				}
			} else {
				sb.append("null");
			}
			sql1 = sql1.replace("@" + para, sb.toString());
		}
		return sql1;
	}

	/**
	 * 替换SQL中的参数名为“？”,不替换rownum开头的参数，mysql使用
	 * 
	 * @param sql sql表达式
	 * @return 替换后的sql
	 */
	private String replaceSqlParameters(String sql) {
		String sql1 = sql;
		MListStr al = Utils.getParameters(sql, "@");
		for (int i = 0; i < al.size(); i++) {
			String paramName = al.get(i);
			if (paramName.toLowerCase().indexOf("rownum") == 0) {
				// 不替换rownum开头的参数，mysql使用
				// select les_out_idx, @rownum := @rownum+1
				// from camp_lesson_outline b ,(select @rownum :=0) c
				continue;
			}
			sql1 = sql1.replaceFirst("@" + al.get(i), "?");
		}

		// 保留@符号
		if (sql1.indexOf("{@}") >= 0) {
			sql1 = sql1.replace("{@}", "@");
		}

		return sql1;
	}

	/**
	 * 替换select 查询中的参数名为具体值，原因是sqlserver在查询中出现参数表达式，会进行全表扫描<br>
	 * 郭磊 2016-11-02
	 * 
	 * @param sql sql表达式
	 * @return 替换后的sql
	 */
	private String replaceSqlSelectParameters(String sql) {
		String sql1 = sql;
		MListStr al = Utils.getParameters(sql, "@");

		for (int i = 0; i < al.size(); i++) {
			String paramName = al.get(i);
			String paramValue = null;

			if (paramName.toLowerCase().indexOf("rownum") == 0) {
				continue;
			}
			try {
				paramValue = this.getReplaceParameterValueExp(paramName);
			} catch (Exception err) {
				LOGGER.error("replaceSqlSelectParameters[" + paramName + "]: {}", err.getMessage());
			}
			// 参数值带@符号不提换，否则出现{@}问题，例如邮件 郭磊 2019-11-11
			if (paramValue == null || paramValue.indexOf("@") >= 0) {
				paramValue = "[[@]]" + paramName;
			}
			// "$"导致报错：java.sql.Exception:Illegal group reference 2022-04-21
			String paramValue1 = Matcher.quoteReplacement(paramValue);
			sql1 = sql1.replaceFirst("@" + paramName, paramValue1);
		}

		sql1 = sql1.replace("[[@]]", "@");

		return sql1;
	}

	/**
	 * 获取参数值表达式，用于select替换
	 * 
	 * @param paramName
	 * @return
	 */
	private String getReplaceParameterValueExp(String paramName) {
		PageValue pv = this._RequestValue.getPageValues().getValue(paramName);
		if (pv == null || pv.getValue() == null) {
			String vOther = this._RequestValue.s(paramName);
			if (vOther == null) {
				return "null";
			} else {
				String vOtherExp = this.sqlParameterStringExp(vOther);
				return vOtherExp.replace("@", "{@}");
			}
		}

		String dt = pv.getDataType();
		dt = dt == null ? "STRING" : dt.toUpperCase().trim();
		String v1 = pv.getStringValue();
		if (dt.equals("BINARY") || dt.equals("B[")) {
			return null;
		} else if (dt.equals("INT")) {
			if (v1.trim().length() > 0) {
				return Integer.parseInt(v1.split("\\.")[0]) + "";
			} else {
				return "null";
			}

		} else if (dt.equals("NUMBER")) {
			if (v1.trim().length() > 0) {
				return Double.parseDouble(v1) + "";
			} else {
				return "null";
			}
		} else if (dt.equals("DATE")) {
			return null;
		} else {
			String v1Exp = this.sqlParameterStringExp(v1);
			StringBuilder sb = new StringBuilder();
			sb.append(v1Exp.replace("@", "{@}"));
			return sb.toString();
		}

	}

	/**
	 * 设置SQL参数
	 * 
	 * @param parameters
	 * @param pst
	 * @throws SQLException
	 */
	private void addSqlParameter(MListStr parameters, PreparedStatement pst) throws SQLException {
		if (parameters == null || this._RequestValue == null) {
			return;
		}
		int index = 0;
		for (int i = 0; i < parameters.size(); i++) {
			String PKey = parameters.get(i).toUpperCase();
			if (PKey.indexOf("ROWNUM") == 0) {
				// 不替换rownum开头的参数，mysql使用
				// select les_out_idx, @rownum := @rownum+1
				// from camp_lesson_outline b ,(select @rownum :=0) c
				continue;
			}
			index++;
			this.addStatementParameter(pst, PKey, index);
		}
	}

	/**
	 * 获取参数的二进制
	 * 
	 * @param pv
	 * @return
	 */
	private byte[] getParaBinary(PageValue pv) {
		Object v = pv.getValue();
		byte[] b = v == null ? null : (byte[]) pv.getValue();
		return b;
	}

	/**
	 * 获取参数时间
	 * 
	 * @param pv
	 * @return
	 */
	private Timestamp getParaTimestamp(PageValue pv) {
		Object t1 = pv.getValue();
		if (t1 == null) {
			return null;
		}

		Timestamp tt1;

		if (t1 instanceof java.util.Date) {
			java.util.Date tt0 = (java.util.Date) t1;
			tt1 = new java.sql.Timestamp(tt0.getTime());
		} else {
			String v1 = t1.toString();
			if (v1.trim().length() > 0) {
				tt1 = this.getTimestamp(v1);
			} else {
				return null;
			}
		}
		// 系统时间不用计算时差
		if (pv.getName() != null && pv.getName().equalsIgnoreCase("SYS_DATE")) {
			return tt1;
		}
		if (this.getTimeDiffMinutes() != 0) {
			tt1 = (Timestamp) Utils.getTimeDiffValue(tt1, this.getTimeDiffMinutes());
		}
		return tt1;
	}

	/**
	 * 获取参数的整数
	 * 
	 * @param pv
	 * @return
	 */
	private Integer getParaInteger(PageValue pv) {
		Object t1 = pv.getValue();
		if (t1 == null) {
			return null;
		}
		String v1 = t1.toString();
		if (v1 != null && v1.trim().length() > 0) {
			if (v1.equals("undefined")) {
				return null;
			}
			int intVal = Integer.parseInt(v1.split("\\.")[0]);
			return intVal;
		} else {
			return null;
		}
	}

	/**
	 * 获取参数的整数
	 * 
	 * @param pv
	 * @return
	 */
	private Long getParaLong(PageValue pv) {
		Object t1 = pv.getValue();
		if (t1 == null) {
			return null;
		}
		String v1 = t1.toString();
		if (v1 != null && v1.trim().length() > 0) {
			if (v1.equals("undefined")) {
				return null;
			}
			long intVal = Long.parseLong(v1.split("\\.")[0]);
			return intVal;
		} else {
			return null;
		}
	}

	/**
	 * 获取参数的浮点
	 * 
	 * @param pv
	 * @return
	 */
	private Double getParaDouble(PageValue pv) {
		Object t1 = pv.getValue();
		if (t1 == null) {
			return null;
		}
		String v1 = t1.toString();
		if (v1 != null && v1.trim().length() > 0) {
			if (v1.equals("undefined")) {
				return null;
			}
			double dbVal = Double.parseDouble(v1);
			return dbVal;
		} else {
			return null;
		}
	}

	private void addStatementParameter(PreparedStatement cst, String parameterName, int index) throws SQLException {
		PageValue pv = this._RequestValue.getPageValues().getValue(parameterName);
		if (pv == null) {
			// hash, md5 ...
			String othVal = this._RequestValue.getOtherValue(parameterName);
			if (othVal == null) {
				cst.setObject(index, null);
				this.writeDebug(this, "添加参数(Object)" + index, parameterName + "=null");
			} else {
				if (parameterName.endsWith(".HASH")) { // hashCode
					Integer intVal = Integer.parseInt(othVal);
					cst.setInt(index, intVal);
					this.writeDebug(this, "添加参数(INTEGER)" + index, parameterName + "=" + intVal);
				} else {
					cst.setString(index, othVal);
					String des1 = parameterName + "=" + othVal;
					this.writeDebug(this, "添加参数(String)" + index, des1);
				}
			}
			return;
		}

		String dt = pv.getDataType();
		dt = dt == null ? "STRING" : dt.toUpperCase().trim();
		String v1 = pv.getStringValue();
		if (dt.equals("BINARY") || dt.equals("[B") || dt.equals("BYTE[]")) {
			byte[] b = this.getParaBinary(pv);
			String des = b == null ? "null" : b.length + "";
			this.writeDebug(this, "添加参数(byte[])" + index, parameterName + "=(" + des + ")");
			cst.setBytes(index, b);
			b = null;
		} else if (dt.equals("INT") || dt.equals("INTEGER") || dt.equals("JAVA.LANG.INTEGER")) {
			Integer intVal = this.getParaInteger(pv);
			if (intVal == null) {
				cst.setNull(index, java.sql.Types.INTEGER);
				this.writeDebug(this, "添加参数(INT)" + index, parameterName + "=null");
			} else {
				cst.setInt(index, intVal);
				this.writeDebug(this, "添加参数(INTEGER)" + index, parameterName + "=" + intVal);
			}
		} else if (dt.equals("BIGINT") || dt.equals("LONG") || dt.equals("JAVA.LANG.LONG")) {
			Long longVal = this.getParaLong(pv);
			if (longVal == null) {
				cst.setNull(index, java.sql.Types.BIGINT);
				this.writeDebug(this, "添加参数(LONG)" + index, parameterName + "=null");
			} else {
				cst.setLong(index, longVal);
				this.writeDebug(this, "添加参数(LONG)" + index, parameterName + "=" + longVal);
			}
		} else if (dt.equals("NUMBER") || dt.equals("DOUBLE") || dt.equals("JAVA.LANG.DOUBLE")) {
			Double dbVal = this.getParaDouble(pv);
			if (dbVal == null) {
				cst.setNull(index, java.sql.Types.DOUBLE);
				this.writeDebug(this, "添加参数(double)" + index, parameterName + "=null");
			} else {
				cst.setDouble(index, dbVal);
				this.writeDebug(this, "添加参数(double)" + index, parameterName + "=" + dbVal);
			}
		} else if (dt.equals("DATE") || dt.equals("JAVA.UTIL.DATE") || dt.equals("JAVA.SQL.DATE")) {
			Timestamp t1 = this.getParaTimestamp(pv);
			if (t1 == null) {
				cst.setNull(index, java.sql.Types.TIMESTAMP);
				this.writeDebug(this, "添加参数(Timestamp)" + index, parameterName + "=null");
			} else {
				cst.setTimestamp(index, t1);
				this.writeDebug(this, "添加参数(Timestamp)" + index, parameterName + "=" + t1);
			}
		} else {
			cst.setString(index, v1);
			String des1 = parameterName + "=" + (v1 == null ? "null" : v1);
			this.writeDebug(this, "添加参数(String)" + index, des1);
		}
	}

	/**
	 * 设置SQL参数 CallableStatement <br>
	 * 对象为所有的DBMS 提供了一种以标准形式调用已储存过程的方法。已储 存过程储存在数据库中。对已储存过程的调用是
	 * CallableStatement对象所含的内容。这种调用是 用一种换码语法来写的，有两种形式：一种形式带结果参，另一种形式不带结果参数。结果参数是
	 * 一种输出 (OUT) 参数，是已储存过程的返回值。两种形式都可带有数量可变的输入（IN 参数）、 输出（OUT 参数）或输入和输出（INOUT
	 * 参数）的参数。问号将用作参数的占位符。
	 * 
	 * @param parameters
	 * @param cst
	 * @throws SQLException
	 */
	private HashMap<String, String> addSqlParameter(MListStr parameters, CallableStatement cst) throws SQLException {
		HashMap<String, String> outValues = new HashMap<String, String>();
		if (parameters == null || this._RequestValue == null) {
			return outValues;
		}
		PreparedStatement pst = (PreparedStatement) cst;
		int index = 0;
		for (int i = 0; i < parameters.size(); i++) {
			String PKey = parameters.get(i).toUpperCase().trim();
			if (PKey.indexOf("ROWNUM") == 0) {
				// 不替换rownum开头的参数，mysql使用
				// select les_out_idx, @rownum := @rownum+1
				// from camp_lesson_outline b ,(select @rownum :=0) c
				continue;
			}
			index++;
			if (!PKey.endsWith("_OUT")) { // 非输出参数
				this.addStatementParameter(pst, PKey, index);
			} else {
				// 输出参数
				cst.registerOutParameter(index, java.sql.Types.VARCHAR);
				outValues.put(PKey, "" + (index));
				this.writeDebug(this, "添返回加参数(String)" + index, PKey);
			}
		}
		return outValues;
	}

	/**
	 * 清除所有返回的 resultSet，当长期执行查询的时，会造成内存占用过高 关闭连接会自动执行清除
	 */
	public void clearResultSets() {
		if (this._ResultSetList != null && this._ResultSetList.size() > 0) {
			for (int i = 0; i < this._ResultSetList.size(); i++) {
				DataResult r = (DataResult) _ResultSetList.get(i);
				try {
					r.getResultSet().close();
				} catch (SQLException e) {
					LOGGER.error(e.getLocalizedMessage());
					setError(e, "Error _resultSet close");
				}
			}
			// 清除数据所有返回的 resultSet
			this._ResultSetList.clear();
		}
	}

	/**
	 * 关闭所有连接,包括ResultSet，Cst，Pst
	 */
	public void close() {
		// 清除所有返回的 resultSet，当长期执行查询的时，会造成内存占用过高
		// 郭磊 2019-02-14
		this.clearResultSets();
		if (_cst != null) {
			try {
				_cst.close();
			} catch (SQLException e) {
				LOGGER.error(e.getLocalizedMessage());
				setError(e, "Error _cst close !");
			}
		}
		if (_pst != null) {
			try {
				_pst.close();
			} catch (SQLException e) {
				LOGGER.error(e.getLocalizedMessage());
				setError(e, "Error _pst close !");
			}
		}
		if (_DebugFrames != null) {
			_DebugFrames.addDebug(this, "SQL", "[close] Close connection.");
		}
		_ds.close();

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
				+ this._CurrentConfig.getName() + "(" + this._ConnectionString + ")";
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
		if (chkSql.indexOf("{CALL") > 0) {
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
	 * @param sql
	 * @param requestValue
	 * @return 输出参数
	 */
	public HashMap<String, String> executeProcdure(String sql) {
		// 修补存储过程的大括号 {CALL prTest(@a1, ...)}
		sql = this.repaireProcdureSqlBrackets(sql);

		this.writeDebug(this, "开始执行", sql);
		String sql1;
		try {
			sql1 = rebuildSql(sql);
		} catch (Exception e) {
			writeDebug(this, "ERR", "[executeQuery(sql,rv)] <font color=red>" + e.getMessage() + "</font>" + ")");
			LOGGER.error(e.getLocalizedMessage());
			setError(e, sql);
			return null;
		}

		MListStr al = Utils.getParameters(sql, "@");
		HashMap<String, String> outValues = null;

		sql1 = this.replaceSqlParameters(sql);
		this.writeDebug(this, "开始执行", sql1);
		try {
			CallableStatement cst = this._ds.getCallableStatement(sql1);
			outValues = this.addSqlParameter(al, cst);
			cst.execute();
			this.getOutValues(outValues, cst);
			if (!this._ds.getConnection().getAutoCommit()) {
				this._ds.getConnection().commit();
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
			CallableStatement cst = this._ds.getCallableStatement(sql);
			cst.execute();
			if (!this._ds.getConnection().getAutoCommit()) {
				this._ds.getConnection().commit();
			}
			cst.close();
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			LOGGER.error(sql);
			this.setError(e, sql);
		}
		this.writeDebug(this, "执行完毕", sql);
	}

	private void getOutValues(HashMap<String, String> outValues, CallableStatement cst) {
		if (outValues == null)
			return;
		Iterator<String> it = outValues.keySet().iterator();
		HashMap<String, String> outValues1 = new HashMap<String, String>();
		while (it.hasNext()) {
			String key = it.next();
			String index = outValues.get(key);
			try {
				String v1 = cst.getString(Integer.parseInt(index));
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
		if (parameter == null) {
			return "NULL";
		}
		if (parameter.length() == 0) {
			return "''";
		}
		boolean isMysql = this.getDatabaseType().equalsIgnoreCase("MYSQL");
		boolean isSqlServer = this.getDatabaseType().equalsIgnoreCase("MSSQL");

		parameter = parameter.replace("'", "''");
		if (isMysql) {
			// MySql字符转义符 反斜线(‘\’)开始，
			parameter = parameter.replace("\\", "\\\\");
		}
		parameter = "'" + parameter + "'";
		if (isSqlServer) {
			parameter = "N" + parameter;
		}
		return parameter;
	}

	/**
	 * 处理字段或表名的字符串表达式，带上[(sqlserver)或`(mysql)
	 * 
	 * @param fieldOrTable 字段或表名
	 * @return 带上[(sqlserver)或`(mysql)的字段或表名
	 */
	public String sqlFieldOrTableExp(String fieldOrTable) {
		boolean isMysql = this.getDatabaseType().equalsIgnoreCase("MYSQL");
		boolean isSqlServer = this.getDatabaseType().equalsIgnoreCase("MSSQL");
		if (isMysql) {
			return "`" + fieldOrTable + "`";
		} else if (isSqlServer) {
			return "[" + fieldOrTable + "]";
		} else {
			return fieldOrTable; // 不处理
		}
	}

	public String getConnectionString() {
		return _ConnectionString;
	}

	public String getDatabaseType() {
		return _DatabaseType;
	}

	/**
	 * 获取ListFrame查询的日期表达式
	 * 
	 * @param s1
	 * @return
	 */
	public String getDateTimePara(String s1) {
		if (s1 == null || s1.trim().length() == 0) {
			return null;
		}
		String s2 = s1.replace("'", "");
		// 先获取字符串的转换的 yyyy-mm-dd表达式
		s2 = Utils.getDateTimeString(new Date(this.getTimestamp(s2).getTime()));
		if (this._DatabaseType.equals("ORACLE") || this._DatabaseType.equals("HSQLDB")) {
			return "to_date('" + s2 + "','YYYY-MM-DD')";
		} else {
			return "'" + s2 + "'";
		}
	}

	public String getDateTimePara(java.sql.Timestamp dt) {
		if (dt == null) {
			return null;
		}
		// 先获取字符串的转换的 yyyy-mm-dd表达式
		String s2 = Utils.getDateTimeString(new Date(dt.getTime()));
		if (this._DatabaseType.equals("ORACLE") || this._DatabaseType.equals("HSQLDB")) {
			return "to_date('" + s2 + "','YYYY-MM-DD')";
		} else {
			return "'" + s2 + "'";
		}
	}

	/**
	 * 获取sql的timestamp数据
	 * 
	 * @param s1
	 * @return
	 */
	public java.sql.Timestamp getTimestamp(String s1) {
		String lang = this._RequestValue != null ? this._RequestValue.getString("ewa_lang") : "zhcn";
		boolean isUKFormat = this._RequestValue != null && this._RequestValue.getString("SYS_EWA_ENUS_YMD") != null
				&& this._RequestValue.getString("SYS_EWA_ENUS_YMD").toLowerCase().equals("dd/mm/yyyy");
		return Utils.getTimestamp(s1, lang, isUKFormat);
	}

	public String getSchemaName() {
		return _SchemaName;
	}

	public Connection getConnection() {
		if (this._Connection == null) {
			this._Connection = _ds.getConnection();
		}
		return _Connection;
	}

	/**
	 * @param currentConfig the _CurrentConfig to set
	 */
	public void setCurrentConfig(ConnectionConfig currentConfig) {
		_CurrentConfig = currentConfig;
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
		return _ResultSetList;
	}

	public DataResult getLastResult() {
		return (DataResult) this._ResultSetList.getLast();
	}

	/**
	 * 是否使用事物处理
	 * 
	 * @return the _IsTrans
	 */
	public boolean isTrans() {
		return _IsTrans;
	}

	/**
	 * Return the error without sql
	 * 
	 * @return
	 */
	public String getErrorMsgOnly() {
		return _errorMsgOnly;
	}
}
