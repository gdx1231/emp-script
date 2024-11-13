/**
 * 
 */
package com.gdxsoft.easyweb.define.group;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.datasource.SqlUtils;
import com.gdxsoft.easyweb.define.database.Field;
import com.gdxsoft.easyweb.define.database.IndexField;
import com.gdxsoft.easyweb.define.database.Table;
import com.gdxsoft.easyweb.define.database.TableIndex;
import com.gdxsoft.easyweb.define.database.maps.MapFieldType;
import com.gdxsoft.easyweb.define.database.maps.MapSqlTemplate;
import com.gdxsoft.easyweb.define.database.maps.MapSqlTemplates;
import com.gdxsoft.easyweb.define.database.maps.Maps;

/**
 * @author Administrator
 * 
 */
public class SqlTable {
	private static Logger LOGGER = LoggerFactory.getLogger(SqlTable.class);

	private Maps _Maps;
	private String _Create;
	private String _Pk;
	private ArrayList<String> _Indexes;
	private ArrayList<String> _Comments;
	private Table _Table;
	private String databaseType;
	private FixTableOrField fix;

	public String getDatabaseType() {
		return databaseType;
	}

	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}

	public SqlTable() {
		this._Indexes = new ArrayList<String>();
		this._Comments = new ArrayList<String>();
		try {
			this._Maps = Maps.instance();
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}

	/**
	 * 生成创建表
	 * 
	 * @param table        表
	 * @param databaseType 数据库类型
	 * @return
	 * @throws Exception
	 */
	public void createSqlTable(Table table, String databaseType) throws Exception {
		this.setTable(table);
		this.setDatabaseType(databaseType);
		this.fix = FixTableOrField.getInstance(databaseType);

		boolean sameDatebaseType = databaseType != null && databaseType.equalsIgnoreCase(table.getDatabaseType());

		boolean isSourceMysql = SqlUtils.isMySql(table.getDatabaseType());
		boolean isSourceSqlServer = SqlUtils.isSqlServer(table.getDatabaseType());
		boolean isMysql = SqlUtils.isMySql(databaseType);
		boolean isSqlServer = SqlUtils.isSqlServer(databaseType);

		LOGGER.info("Create {} {} DDL from {} to {}", table.getTableType(), table.getName(), table.getDatabaseType(),
				databaseType);
		// 数据库一致而且带有创建对象的DDL语句
		if (sameDatebaseType) {
			if (table.getSqlTable() != null && table.getSqlTable().trim().length() > 0) {
				LOGGER.info("DatabaseType same as, using the original DDL");
				this.setCreate(table.getSqlTable());
				return;
			}
		}

		if ("VIEW".equalsIgnoreCase(table.getTableType())) { // 视图
			String viewDdl = table.getSqlTable();
			if (viewDdl != null && viewDdl.trim().length() > 0) {
				LOGGER.info("The view DDL maybe error, you should fixed this");
				if (isSourceSqlServer && isMysql) {
					// 来源是SQLServer，目标数据库是 MYSQL
					viewDdl = StringUtils.replaceIgnoreCase(viewDdl, "isnull(", "ifnull(");
					viewDdl = StringUtils.replaceIgnoreCase(viewDdl, "getdate()", "now()");
					viewDdl = viewDdl.replace("[", "`").replace("]", "`");
				} else if (isSourceMysql && isSqlServer) {
					// 来源是MYSQL，目标数据库是SQLServer
					viewDdl = StringUtils.replaceIgnoreCase(viewDdl, "ifnull(", "ISNULL(");
					viewDdl = StringUtils.replaceIgnoreCase(viewDdl, "now()", "GETDATE()");
					viewDdl = viewDdl.replace("`", "");
				}
				this.setCreate(table.getSqlTable());
				return;
			}
		}

		HashMap<String, MapFieldType> alFrom = _Maps.getMapFieldTypes().getTypes(table.getDatabaseType());
		StringBuilder sb = new StringBuilder();
		StringBuilder sbPk = new StringBuilder();

		sb.append("CREATE TABLE ");

		// 替换数据前缀，例如：my_work.dbo. (sqlserver), my_work.
		sb.append(this.getDatabasePrefix());

		sb.append(fix.getFixCharBefore());
		sb.append(table.getName());
		sb.append(fix.getFixCharAfter());
		sb.append("(\n");
		for (int i = 0; i < table.getFields().size(); i++) {
			Field f = table.getFields().get(table.getFields().getFieldList().get(i));
			if (f.isPk()) {
				if (sbPk.length() > 0) {
					sbPk.append(", ");
				}
				sbPk.append(f.getName());
			}
			if (i > 0) {
				sb.append(",\n");
			}
			String fieldExp = this.createTableField(f, alFrom);
			sb.append(fieldExp);
		}

		boolean isMySql = SqlUtils.isMySql(databaseType);
		// 主键表达式
		if (table.getPk() != null && table.getPk().getPkFields().size() > 0) {
			ArrayList<Field> pks = table.getPk().getPkFields();
			sb.append(",\n\t");
			if (!isMySql) {
				String pkName=table.getPk().getPkName() ;
				if("primary".equalsIgnoreCase(pkName)) {
					pkName = "PK_" + table.getName();
				}
				sb.append(" constraint " + fix.getFixCharBefore() + pkName + fix.getFixCharAfter());
			}
			sb.append(" primary key(");
			for (int i = 0; i < pks.size(); i++) {
				Field f = pks.get(i);
				if (i > 0) {
					sb.append("\n, ");
				}
				sb.append(fix.getFixCharBefore());
				sb.append(f.getName());
				sb.append(fix.getFixCharAfter());
			}
			sb.append(")\n");
		}
		sb.append(")\n");
		// this._Pk = this.createPrimaryKey(table, databaseType, sbPk.toString());

		this.setCreate(sb.toString());

		this.createSqlIndexes();
		this.createTableComment();
	}

	private MapFieldType getMapTo(String fieldType, HashMap<String, MapFieldType> alFrom) throws Exception {
		MapFieldType mapType = alFrom.get(fieldType);
		if (mapType == null) {
			throw new Exception("数据类型：" + fieldType + "未定义");
		}
		if (mapType.getDatabaseName().equalsIgnoreCase(databaseType) && fieldType.indexOf("_MAX")<0) {
			// 数据库类型一致的话则保持原始数据类型，不用转换			
			return mapType;
		}
		MapFieldType[] b = mapType.getEwa().getMapTo().get(databaseType);
		if (b.length == 0) {
			throw new Exception("数据类型：" + fieldType + "未找到对应的类型《" + databaseType + "》！");
		}
		return b[0];

	}

	/**
	 * 创建表字段表达式
	 * 
	 * @param f
	 * @param alFrom
	 * @return
	 * @throws Exception
	 */
	private String createTableField(Field f, HashMap<String, MapFieldType> alFrom) throws Exception {
		StringBuilder sb = new StringBuilder();
		boolean isPostgresql = SqlUtils.isPostgreSql(databaseType);
		boolean isMySql = SqlUtils.isMySql(databaseType);
		boolean isSqlServer = SqlUtils.isSqlServer(databaseType);

		// 来源是否为SQLServer
		boolean isSourceSqlServer = SqlUtils.isSqlServer(this._Table.getDatabaseType());

		// INT , NVARCHAR, CHAR , DATETIME ...
		String fieldType = f.getDatabaseType().toUpperCase();

		if (isSourceSqlServer && fieldType.endsWith("IDENTITY")) {
			fieldType = fieldType.replace("IDENTITY", "").replace("(", "").replace(")", "").trim();
		}

		// 映射的对象
		MapFieldType mapTo = this.getMapTo(fieldType, alFrom);

		// 映射的字段类型
		String fieldType1 = mapTo.getName();

		// 自增序列
		if (f.isIdentity() && isPostgresql) {
			fieldType1 = " serial ";
			if ("bigint".equals(mapTo.getName())) {
				fieldType1 = " bigserial ";
			} else if ("smallint".equals(mapTo.getName())) {
				fieldType1 = " smallserial ";
			}
		}

		// 不需要长度表达式
		boolean noLenExp = false;
		if ("varchar".equalsIgnoreCase(fieldType1) || "nvarchar".equalsIgnoreCase(fieldType1)) { // sqlserver max 转换为
			int len = f.getColumnSize();
			if (len == 2147483647 || len == 2147483647 / 2) {
				MapFieldType mapTo1 = this.getMapTo("NVARCHAR_MAX", alFrom);
				fieldType1 = mapTo1.getName();
				noLenExp = true;
			}
		} else if ("varbinary".equalsIgnoreCase(fieldType1)) { // sqlserver max 转换为
			int len = f.getColumnSize();
			if (len == 2147483647 || len == 2147483647 / 2) {
				MapFieldType mapTo1 = this.getMapTo("VARBINARY_MAX", alFrom);
				fieldType1 = mapTo1.getName();
				noLenExp = true;
			}
		}

		sb.append("\t");
		sb.append(fix.getFixCharBefore());
		sb.append(f.getName());
		sb.append(fix.getFixCharAfter());
		sb.append(" ");
		sb.append(fieldType1);

		if (!noLenExp) { // 不需要长度表达式
			if (mapTo.getEwa().getCreateNumber() == 1) {
				int len = f.getColumnSize();
				String lenDes = len + "";
				if ((len < 0 || len == 2147483647 || len == 2147483647 / 2) && isSqlServer) {
					lenDes = "MAX";
				}
				sb.append("(" + lenDes + ")");
			} else if (mapTo.getEwa().getCreateNumber() == 2) {
				sb.append("(" + f.getColumnSize() + "," + f.getDecimalDigits() + ")");
			}
		}
		if (f.isIdentity() && isSqlServer) {
			sb.append(" IDENTITY(1,1) ");
		}

		if (!f.isNull() || f.isPk()) {
			sb.append(" NOT NULL");
		} else {
			sb.append(" NULL");
		}
		if (f.isIdentity() && isMySql) {
			sb.append(" AUTO_INCREMENT ");
		}
		// 如果是mysql的话，在建表过程中，将注解建好
		if (isMySql) {
			sb.append(" COMMENT '" + f.getDescription().replace("'", "''") + "' ");
		}

		return sb.toString();
	}

	/**
	 * 替换数据前缀，例如：my_work.dbo. (sqlserver), my_work.
	 * 
	 * @param table
	 * @param databaseType
	 * @return
	 */
	private String getDatabasePrefix() {
		return this._Table.getDatabasePrefix(databaseType);
	}

	/**
	 * 不用了
	 * 
	 * @param table
	 * @param databaseType
	 * @param sPk
	 * @return
	 */
	@Deprecated
	String createPrimaryKey(Table table, String databaseType, String sPk) {
		if (sPk.trim().length() == 0) {
			return "";
		}
		MapSqlTemplates c = this._Maps.getMapSqlTemplates();
		MapSqlTemplate c1 = c.getSqlTemplate(databaseType);

		if (c1 == null) {
			return "";
		}
		String pkTmp = c1.getSqlTemplate("PrimaryKey");

		if (pkTmp == null || pkTmp.trim().length() == 0) {
			return "";
		}

		pkTmp = pkTmp.replace("{TABLE_NAME}", table.getName());
		pkTmp = pkTmp.replace("{FIELD_NAMES}", sPk);

		return pkTmp;
	}

	/**
	 * 生成表索引
	 * 
	 * @param table         表
	 * @param databaseType  目标数据库类型
	 * @param fixCharBefore 表名限定字符前缀
	 * @param fixCharAfter  表名限定字符后缀
	 */
	private void createSqlIndexes() {
		this.setIndexes(new ArrayList<String>());
		for (int i = 0; i < _Table.getIndexes().size(); i++) {
			TableIndex idx = _Table.getIndexes().get(i);
			if (idx.isUnique() && idx.getIndexName().toLowerCase().startsWith("pk_")
					|| idx.getIndexName().toLowerCase().endsWith("_pk")) {
				// 主键已经创建
				continue;
			}

			String indexDdl = this.createSqlIndex(idx);

			this.getIndexes().add(indexDdl);
		}
	}

	/**
	 * 创建索引 DDL
	 * 
	 * @param table                表
	 * @param idx                  索引
	 * @param databaseType         目标数据库类型
	 * @param fixCharBefore        表名限定字符前缀
	 * @param fixCharAfter表名限定字符后缀
	 * @return DDL
	 */
	private String createSqlIndex(TableIndex idx) {

		StringBuilder sb = new StringBuilder();

		sb.append("CREATE ");
		sb.append((idx.isUnique() ? "UNIQUE" : ""));
		sb.append(" INDEX ");
		sb.append(idx.getIndexName());
		sb.append(" ON ");
		sb.append(this.getDatabasePrefix());
		sb.append(fix.getFixCharBefore());
		sb.append(_Table.getName());
		sb.append(fix.getFixCharAfter());
		sb.append("(\n");
		for (int m = 0; m < idx.getIndexFields().size(); m++) {
			if (m > 0) {
				sb.append("\n  , ");
			}
			IndexField f = idx.getIndexFields().get(m);
			sb.append(fix.getFixCharBefore());
			sb.append(f.getName());
			sb.append(fix.getFixCharAfter());
			sb.append((f.isAsc() ? "" : " DESC"));
		}
		sb.append(")\n");

		return sb.toString();
	}

	/**
	 * 生成表字段的备注
	 * 
	 * @param table
	 * @param databaseType
	 */
	private void createTableComment() {
		MapSqlTemplates c = this._Maps.getMapSqlTemplates();
		MapSqlTemplate c1 = c.getSqlTemplate(databaseType);
		if (c1 == null) {
			return;
		}
		String s = c1.getSqlTemplate("FieldCommentSet");

		if (s == null || s.trim().length() == 0) {
			return;
		}
		for (int i = 0; i < _Table.getFields().size(); i++) {
			Field f = _Table.getFields().get(_Table.getFields().getFieldList().get(i));
			if (f.getDescription().equals(f.getName())) {
				continue;
			}
			String sql = s;
			sql = sql.replace("{TABLE_NAME}", _Table.getName());
			sql = sql.replace("{FIELD_NAME}", f.getName());
			sql = sql.replace("{COMMENT}", f.getDescription());

			_Comments.add(sql);
		}
	}

	/**
	 * @return the _Create
	 */
	public String getCreate() {
		return _Create;
	}

	/**
	 * @param create the _Create to set
	 */
	public void setCreate(String create) {
		_Create = create;
	}

	/**
	 * @return the _Pk
	 */
	public String getPk() {
		return _Pk;
	}

	/**
	 * @param pk the _Pk to set
	 */
	public void setPk(String pk) {
		_Pk = pk;
	}

	/**
	 * @return the _Indexes
	 */
	public ArrayList<String> getIndexes() {
		return _Indexes;
	}

	/**
	 * @param indexes the _Indexes to set
	 */
	public void setIndexes(ArrayList<String> indexes) {
		_Indexes = indexes;
	}

	/**
	 * @return the _Table
	 */
	public Table getTable() {
		return _Table;
	}

	/**
	 * @param table the _Table to set
	 */
	public void setTable(Table table) {
		_Table = table;
	}

	/**
	 * 获取字段备注的SQL
	 * 
	 * @return the _Comments
	 */
	public ArrayList<String> getComments() {
		return _Comments;
	}
}
