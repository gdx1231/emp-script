package com.gdxsoft.easyweb.datasource;

import java.util.List;
import java.util.regex.Pattern;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateSetItem;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;

public class SqlPart {

	private String _Sql;
	private String _Fields = "";
	private String _Where = "";
	private String _OrderBy = "";
	private String _GroupBy = "";
	private String _Having = "";
	private String _TableName = "";

	/**
	 * 根据 order 或 where重建 SQL
	 * 
	 * @param order
	 * @param where
	 * @return
	 */
	public String rebuildSql(String order, String where) {
		StringBuilder sqlTmp = new StringBuilder();
		sqlTmp.append("SELECT " + this._Fields + "\nFROM " + this._TableName);
		sqlTmp.append(" WHERE (" + this._Where + " )");

		if (where != null && where.trim().length() > 0) {
			sqlTmp.append(" AND (" + where + ") ");
		}
		if (this._GroupBy.length() > 0) {
			sqlTmp.append("\n GROUP BY " + this._GroupBy);
		}
		if (this._Having.length() > 0) {
			sqlTmp.append("\n HAVING " + this._Having);
		}
		if (order != null && order.trim().length() > 0) {
			sqlTmp.append("\n ORDER BY " + order);
		} else {
			if (_OrderBy.length() > 0) {
				sqlTmp.append("\r\n ORDER BY " + _OrderBy);
			}
		}

		return sqlTmp.toString();
	}

	public String rebuildSql(String order, String where, boolean isMySqlChnOrder) {
		StringBuilder sqlTmp = new StringBuilder();
		sqlTmp.append("SELECT " + this._Fields + "\nFROM " + this._TableName);
		sqlTmp.append(" WHERE (" + this._Where + " )");

		if (where != null && where.trim().length() > 0) {
			sqlTmp.append(" AND (" + where + ") ");
		}
		if (this._GroupBy.length() > 0) {
			sqlTmp.append("\n GROUP BY " + this._GroupBy);
		}
		if (this._Having.length() > 0) {
			sqlTmp.append("\n HAVING " + this._Having);
		}
		if (order != null && order.trim().length() > 0) {
			sqlTmp.append("\n ORDER BY " + order);
		} else if (_OrderBy.length() > 0) {
			if (isMySqlChnOrder) {
				// mysql 按照中文进行排序
				sqlTmp.append("\n ORDER BY ");
				String[] fields = _OrderBy.split(",");
				for (int i = 0; i < fields.length; i++) {
					String f = fields[i].trim().toUpperCase();
					if (i > 0) {
						sqlTmp.append(", ");
					}
					if (f.endsWith(" DESC")) {
						f = f.substring(0, f.length() - 4);
						if (this.checkIsUseGbk(f)) {
							sqlTmp.append("CONVERT(" + f + " USING gbk) DESC");
						} else {
							sqlTmp.append(f + " DESC");
						}
					} else if (f.endsWith(" ASC")) {
						f = f.substring(0, f.length() - 3);
						if (this.checkIsUseGbk(f)) {
							sqlTmp.append("CONVERT(" + f + " USING gbk) ASC");
						} else {
							sqlTmp.append(f + " ASC");
						}
					} else {
						if (this.checkIsUseGbk(f)) {
							sqlTmp.append("CONVERT(" + f + " USING gbk)");
						} else {
							sqlTmp.append(f);
						}
					}
				}
			} else {
				sqlTmp.append("\r\n ORDER BY " + _OrderBy);
			}
		}

		return sqlTmp.toString();
	}

	/**
	 * 根据字段名称判断是字段否用 GBK转换 CONVERT(fieldName USING gbk)
	 * 
	 * @param fieldName
	 * @return
	 */
	public boolean checkIsUseGbk(String fieldName) {
		fieldName = fieldName.trim().toUpperCase();
		if (fieldName.indexOf("(") > 0 || fieldName.endsWith("ID") || fieldName.endsWith("IDX")
				|| fieldName.endsWith("UID") || fieldName.endsWith("ORD") || fieldName.endsWith("DATE")
				|| fieldName.endsWith("NUM") || fieldName.endsWith("DAY") || fieldName.endsWith("TIME")
				|| fieldName.endsWith("INC") || fieldName.indexOf("MOENY") >= 0 || fieldName.indexOf("PRICE") >= 0
				|| fieldName.indexOf("STAR") >= 0 || fieldName.indexOf("SCORE") >= 0 || fieldName.indexOf("COUNT") >= 0
				|| fieldName.indexOf("SIZE") >= 0 || fieldName.indexOf("UNID") >= 0 || fieldName.indexOf("AGE") >= 0) {
			return false;
		}
		return true;
	}

	public String getTableName() {
		return _TableName;
	}

	public String getFields() {
		return _Fields;
	}

	public String getOrderBy() {
		return _OrderBy;
	}

	public String getSql() {
		return _Sql;
	}

	public void setSql(String sql) {
		_Sql = sql;
		createPart();
	}

	public String getWhere() {
		return _Where;
	}

	public SqlPart() {

	}

	/**
	 * 分析Update语句的结构
	 * 
	 * @param sqlUpdate update更新语句
	 * @param sqlDbType 数据库类型 (com.alibaba.druid.util.JdbcUtils)
	 * @return 是否成功
	 */
	public boolean setUpdateSql(String sqlUpdate, String sqlDbType) {
		this._Sql = sqlUpdate;
		SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(this._Sql, sqlDbType);
		List<SQLStatement> stmtList = parser.parseStatementList();

		for (SQLStatement stmt : stmtList) {
			// stmt.accept(visitor);
			if (stmt instanceof SQLUpdateStatement) {
				SQLUpdateStatement up = (SQLUpdateStatement) stmt;
				this._TableName = up.getTableName().getSimpleName();
				this._Where = up.getWhere().toString();
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < up.getItems().size(); i++) {
					SQLUpdateSetItem item = up.getItems().get(i);
					if (i > 0) {
						sb.append("\t\n, ");
					}
					sb.append(item.toString());
				}
				this._Fields = sb.toString();

				return true;
			}
		}

		return false;
	}

	private void createPart() {
		String[] s1 = getSqlSplit(_Sql, "from", 2);
		String select = s1[0].toUpperCase();
		int m1 = select.indexOf("SELECT");
		this._Fields = s1[0].substring(m1 + 6).trim();

		String[] s2 = getSqlSplit(s1[1], "where", 112);
		if (s2.length > 2) {
			String[] s22 = new String[2];
			s22[0] = s2[0];
			for (int i = 1; i < s2.length - 1; i++) {
				s22[0] += " WHERE " + s2[i];
			}
			s22[1] = s2[s2.length - 1];
			s2 = s22;
		}

		this._TableName = s2[0];

		String[] s3 = getSqlSplit(s2[1], "order\\s*by");
		String[] where = this.getPart1(s3, "ORDER BY");
		this._OrderBy = where[1];

		String[] s4 = getSqlSplit(where[0], "group\\s*by");
		String[] groupby = getPart1(s4, "GROUP BY");

		this._Where = groupby[0];

		String[] s5 = getSqlSplit(groupby[1], "having");
		String[] having = getPart1(s5, "HAVING");

		this._GroupBy = having[0];
		this._Having = having[1];

	}

	private String[] getPart1(String[] expr, String keyWord) {
		String[] val = new String[2];
		val[0] = "";
		val[1] = "";
		if (expr.length == 1) {
			val[0] = expr[0];
		} else {
			val[0] = expr[0];
			for (int i = 1; i < expr.length - 1; i++) {
				val[0] += " " + keyWord + " " + expr[i];
			}
			val[1] = expr[expr.length - 1];
		}
		return val;
	}

	private String[] getSqlSplit(String sql, String tag, int limits) {
		Pattern pat = Pattern.compile("\\b" + tag + "\\b", Pattern.CASE_INSENSITIVE);

		return pat.split(sql, limits);
	}

	private String[] getSqlSplit(String sql, String tag) {
		Pattern pat = Pattern.compile("\\b" + tag + "\\b", Pattern.CASE_INSENSITIVE);

		return pat.split(sql);
	}

	public String getGroupBy() {
		return _GroupBy;
	}

	public String getHaving() {
		return _Having;
	}

}
