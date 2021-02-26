package com.gdxsoft.easyweb.define.database;

import com.gdxsoft.easyweb.utils.msnet.MStr;

/**
 * 表字段
 * 
 * @author Administrator
 * 
 */
public class Field {
	private String _TableName; // 表名称
	private String _Name; // 字段名称
	private String _Description; // 字段描述
	private String _DatabaseType; // 数据类型
	private int _Maxlength; // 长度
	private boolean _IsPk; // 主键
	private boolean _IsNull; // 是否为空
	private boolean _IsFk; // 是否外键
	private String _FKTableName; // 外键表名
	private String _FKColumnName; // 外键字段
	private int _ColumnSize; // 长度
	private int _DecimalDigits; // 小数点位置
	private int _DataType; // java类型
	private int _CharOctetLength; // 8进制字符长度
	private int _OrdinalPosition; // 字段顺序
	private boolean _IsIdentity; // 是否支持identity
	private int _MapLength; // 映射字段的长度

	public Field() {

	}

	/**
	 * 获取Sql的类型表达式
	 * 
	 * @return
	 */
	public String getSqlType() {
		MStr s = new MStr();
		s.a(this._DatabaseType);
		String t = this._DatabaseType.toLowerCase();

		if (t.indexOf("char") >= 0 || t.indexOf("var") == 0) {
			s.a("(" + this._ColumnSize + ")");
		} else if (t.indexOf("num") >= 0) {
			s.a("(" + this._ColumnSize + "," + this._DecimalDigits + ")");
		}

		return s.toString();
	}

	/**
	 * 获取 Alter SQL
	 * 
	 * @return
	 */
	public String getSqlAlter() {
		MStr s = new MStr();
		s.a("ALTER TABLE " + _TableName + " ADD " + this._Name + " ");

		String sqlType = this.getSqlType();
		s.a(sqlType);

		if (this._IsIdentity) {
			s.a(" IDENTITY(1,1)");
		}
		if (!this._IsNull) {
			s.a(" NOT NULL");
		}
		return s.toString();
	}

	public String getSqlChange() {
		MStr s = new MStr();
		s.a("ALTER TABLE " + _TableName + " ALTER COLUMN " + this._Name + " ");

		String sqlType = this.getSqlType();
		s.a(sqlType);
		
		if (this._IsIdentity) {
			s.a(" IDENTITY(1,1)");
		}
		if (!this._IsNull) {
			s.a(" NOT NULL");
		}
		return s.toString();
	}

	public String getDatabaseType() {
		return _DatabaseType;
	}

	public void setDatabaseType(String databaseType) {
		_DatabaseType = databaseType;
	}

	public String getDescription() {
		return _Description;
	}

	public void setDescription(String description) {
		_Description = description;
	}

	public boolean isNull() {
		return _IsNull;
	}

	public void setNull(boolean isNull) {
		_IsNull = isNull;
	}

	public boolean isPk() {
		return _IsPk;
	}

	public void setPk(boolean isPk) {
		_IsPk = isPk;
	}

	public int getMaxlength() {
		return _Maxlength;
	}

	public void setMaxlength(int maxlength) {
		_Maxlength = maxlength;
	}

	public String getName() {
		return _Name;
	}

	public void setName(String name) {
		_Name = name;
	}

	/**
	 * @return the _IsFk
	 */
	public boolean isFk() {
		return _IsFk;
	}

	/**
	 * @param isFk the _IsFk to set
	 */
	public void setFk(boolean isFk) {
		_IsFk = isFk;
	}

	/**
	 * @return the _FKTableName
	 */
	public String getFKTableName() {
		return _FKTableName;
	}

	/**
	 * @param tableName the _FKTableName to set
	 */
	public void setFKTableName(String tableName) {
		_FKTableName = tableName;
	}

	/**
	 * @return the _FKColumnName
	 */
	public String getFKColumnName() {
		return _FKColumnName;
	}

	/**
	 * @param columnName the _FKColumnName to set
	 */
	public void setFKColumnName(String columnName) {
		_FKColumnName = columnName;
	}

	/**
	 * @return the _ColumnSize
	 */
	public int getColumnSize() {
		return _ColumnSize;
	}

	/**
	 * @param columnSize the _ColumnSize to set
	 */
	public void setColumnSize(int columnSize) {
		_ColumnSize = columnSize;
	}

	/**
	 * @return the _DecimalDigits
	 */
	public int getDecimalDigits() {
		return _DecimalDigits;
	}

	/**
	 * @param decimalDigits the _DecimalDigits to set
	 */
	public void setDecimalDigits(int decimalDigits) {
		_DecimalDigits = decimalDigits;
	}

	/**
	 * @return the _DataType
	 */
	public int getDataType() {
		return _DataType;
	}

	/**
	 * @param dataType the _DataType to set
	 */
	public void setDataType(int dataType) {
		_DataType = dataType;
	}

	/**
	 * @return the _CharOctetLength
	 */
	public int getCharOctetLength() {
		return _CharOctetLength;
	}

	/**
	 * @param charOctetLength the _CharOctetLength to set
	 */
	public void setCharOctetLength(int charOctetLength) {
		_CharOctetLength = charOctetLength;
	}

	/**
	 * @return the _OrdinalPosition
	 */
	public int getOrdinalPosition() {
		return _OrdinalPosition;
	}

	/**
	 * @param ordinalPosition the _OrdinalPosition to set
	 */
	public void setOrdinalPosition(int ordinalPosition) {
		_OrdinalPosition = ordinalPosition;
	}

	/**
	 * @return the _TableName
	 */
	public String getTableName() {
		return _TableName;
	}

	/**
	 * @param tableName the _TableName to set
	 */
	public void setTableName(String tableName) {
		_TableName = tableName;
	}

	/**
	 * @return the _IsIdentity
	 */
	public boolean isIdentity() {
		return _IsIdentity;
	}

	/**
	 * @param isIdentity the _IsIdentity to set
	 */
	public void setIdentity(boolean isIdentity) {
		_IsIdentity = isIdentity;
	}

	/**
	 * @return the _MapLength
	 */
	public int getMapLength() {
		return _MapLength;
	}

	/**
	 * @param mapLength the _MapLength to set
	 */
	public void setMapLength(int mapLength) {
		_MapLength = mapLength;
	}
}
