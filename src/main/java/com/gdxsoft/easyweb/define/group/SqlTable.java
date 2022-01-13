/**
 * 
 */
package com.gdxsoft.easyweb.define.group;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.define.database.Field;
import com.gdxsoft.easyweb.define.database.IndexField;
import com.gdxsoft.easyweb.define.database.Table;
import com.gdxsoft.easyweb.define.database.TableIndex;
import com.gdxsoft.easyweb.define.database.maps.MapSqlTemplate;
import com.gdxsoft.easyweb.define.database.maps.MapSqlTemplates;
import com.gdxsoft.easyweb.define.database.maps.MapFieldType;
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

	public SqlTable() {
		this._Indexes = new ArrayList<String>();
		this._Comments = new ArrayList<String>();
		try {
			this._Maps = Maps.instance();
		} catch (Exception e) {
			System.out.println(e.getMessage());
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
		boolean sameDatebaseType = databaseType != null && databaseType.equalsIgnoreCase(table.getDatabaseType());
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
			if (table.getSqlTable() != null && table.getSqlTable().trim().length() > 0) {
				LOGGER.info("The view DDL maybe error, you should fixed this");
				this.setCreate(table.getSqlTable());
				return;
			}
		}

		HashMap<String, MapFieldType> alFrom = _Maps.getMapFieldTypes().getTypes(table.getDatabaseType());
		StringBuilder sb = new StringBuilder();
		StringBuilder sbPk = new StringBuilder();
		String fixCharBefore = "";
		String fixCharAfter = "";
		// if (table.getDatabaseType().equalsIgnoreCase("MSSQL")) {
		// fixCharBefore = "[";
		// fixCharAfter = "]";
		// }
		if (databaseType.equalsIgnoreCase("MSSQL")) {
			fixCharBefore = "[";
			fixCharAfter = "]";
		}
		sb.append("CREATE TABLE " + fixCharBefore + table.getName() + fixCharAfter + "(\r\n");
		for (int i = 0; i < table.getFields().size(); i++) {
			Field f = table.getFields().get(table.getFields().getFieldList().get(i));
			if (f.isPk()) {
				if (sbPk.length() > 0) {
					sbPk.append(", ");
				}
				sbPk.append(f.getName());
			}
			String fieldType = f.getDatabaseType().toUpperCase();

			if (fieldType.endsWith("IDENTITY")) {
				fieldType = fieldType.replace("IDENTITY", "").replace("(", "").replace(")", "").trim();
			}
			MapFieldType mapTo;

			MapFieldType mapType = alFrom.get(fieldType);
			if (mapType == null) {
				throw new Exception("数据类型：" + fieldType + "未定义");
			}
			if (mapType.getDatabaseName().equalsIgnoreCase(databaseType)) {
				// 数据库类型一致的话则保持原始数据类型，不用转换
				mapTo = mapType;
			} else {
				MapFieldType[] b = mapType.getEwa().getMapTo().get(databaseType);

				if (b.length == 0) {
					throw new Exception("数据类型：" + fieldType + "未找到对应的类型《" + databaseType + "》！");
				}
				mapTo = b[0];
			}
			if (i > 0) {
				sb.append(",\r\n");
			}
			sb.append("\t" + fixCharBefore + f.getName() + fixCharAfter + " " + mapTo.getName());
			if (mapTo.getEwa().getCreateNumber() == 1) {
				int len = f.getColumnSize() * mapType.getScale() / mapTo.getScale();
				String lenDes = len + "";
				if ((len < 0 || len == 2147483647 || len == 2147483647 / 2)
						&& table.getDatabaseType().equalsIgnoreCase("MSSQL")) {
					lenDes = "MAX";
				}
				sb.append("(" + lenDes + ")");
			} else if (mapTo.getEwa().getCreateNumber() == 2) {
				sb.append("(" + f.getColumnSize() + "," + f.getDecimalDigits() + ")");
			}
			if (f.isIdentity() && databaseType.equalsIgnoreCase("MSSQL")) {
				sb.append(" IDENTITY(1,1) ");
			}

			if (!f.isNull() || f.isPk()) {
				sb.append(" NOT NULL");
			} else {
				sb.append(" NULL");
			}
			if (f.isIdentity() && databaseType.equalsIgnoreCase("MYSQL")) {
				sb.append(" AUTO_INCREMENT, PRIMARY KEY(`" + f.getName() + "`) ");
			}
			// 如果是mysql的话，在建表过程中，将注解建好
			if (databaseType.equalsIgnoreCase("MYSQL")) {
				sb.append(" COMMENT '" + f.getDescription().replace("'", "''") + "' ");
			}
		}
		sb.append(")");
		this._Pk = this.createPrimaryKey(table, databaseType, sbPk.toString());

		this.setCreate(sb.toString().toUpperCase());

		this.createSqlIndexes(table);
		this.createTableComment(table, databaseType);
	}

	private String createPrimaryKey(Table table, String databaseType, String sPk) {
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
	 * @param table
	 */
	private void createSqlIndexes(Table table) {
		this.setIndexes(new ArrayList<String>());
		for (int i = 0; i < table.getIndexes().size(); i++) {
			StringBuilder sb = new StringBuilder();
			TableIndex idx = table.getIndexes().get(i);
			if (idx.isUnique() && idx.getIndexName().toLowerCase().startsWith("pk_")
					|| idx.getIndexName().toLowerCase().endsWith("_pk")) {
				// 主键已经创建
				continue;
			}

			sb.append("CREATE " + (idx.isUnique() ? "UNIQUE" : "") + " INDEX " + idx.getIndexName() + " ON "
					+ table.getName() + "(");
			for (int m = 0; m < idx.getIndexFields().size(); m++) {
				if (m > 0) {
					sb.append(", ");
				}
				IndexField f = idx.getIndexFields().get(m);
				sb.append(f.getName() + (f.isAsc() ? "" : " DESC"));
			}
			sb.append(")");
			this.getIndexes().add(sb.toString().toUpperCase());
		}
	}

	/**
	 * 生成表字段的备注
	 * 
	 * @param table
	 * @param databaseType
	 */
	private void createTableComment(Table table, String databaseType) {
		MapSqlTemplates c = this._Maps.getMapSqlTemplates();
		MapSqlTemplate c1 = c.getSqlTemplate(databaseType);
		if (c1 == null) {
			return;
		}
		String s = c1.getSqlTemplate("FieldCommentSet");

		if (s == null || s.trim().length() == 0) {
			return;
		}
		for (int i = 0; i < table.getFields().size(); i++) {
			Field f = table.getFields().get(table.getFields().getFieldList().get(i));
			if (f.getDescription().equals(f.getName())) {
				continue;
			}
			String sql = s;
			sql = sql.replace("{TABLE_NAME}", table.getName());
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
