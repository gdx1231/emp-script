package com.gdxsoft.easyweb.datasource;

import java.util.HashMap;

import com.gdxsoft.easyweb.data.DTCell;
import com.gdxsoft.easyweb.data.DTColumn;
import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.utils.Utils;

public class UpdateChanges {
	private String keysExp_; // 主键表达式，多个字段","分割，例如三主键 name,birth,sex
	private String[] keys_ = null;
	private SqlPart sqlPart_;
	private DTTable tbBefore_;
	private DTTable tbAfter_;

	private HashMap<String, DTRow> tbAfterMap_ = null;

	/**
	 * 获取第一行的数据
	 * 
	 * @return
	 */
	public UpdateChangeRow getRowChange() {
		return this.getRowChange(0);
	}

	/**
	 * 获取数据行的主键表达式
	 * 
	 * @param r
	 * @return
	 */
	private String getKeysVal(DTRow r) {
		if (keys_ == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < keys_.length; i++) {
			if (i > 0) {
				sb.append("[&]");
			}
			String key = keys_[i].trim();
			try {
				DTColumn col = r.getTable().getColumns().getColumn(key);
				Object v = r.getCell(key).getValue();
				if (v == null) {
					sb.append("[--NULL--]");
				} else if (col.getTypeName() != null && (col.getTypeName().indexOf("bin") == 0
						|| col.getTypeName().equalsIgnoreCase("image") || col.getTypeName().equals("blob"))) { // 二进制
					// 二进制
					byte[] bufBefore = (byte[]) v;
					String md5 = Utils.md5(bufBefore);
					sb.append(md5);

				} else {
					sb.append(v.toString());
				}
			} catch (Exception e) {
				sb.append(e.getMessage());
			}
		}
		return sb.toString();
	}

	/**
	 * 根据主键表达式创建主键-数据的映射
	 */
	private void builderAfterKeysMap() {
		if (keys_ == null) {
			return;
		}
		tbAfterMap_ = new HashMap<String, DTRow>();

		for (int i = 0; i < tbAfter_.getCount(); i++) {
			DTRow r = tbAfter_.getRow(i);
			String keysVal = this.getKeysVal(r);
			this.tbAfterMap_.put(keysVal, r);
		}
	}

	/**
	 * 获取变化
	 * 
	 * @param rowIndex 记录第几行
	 * @return
	 */
	public UpdateChangeRow getRowChange(int rowIndex) {
		DTRow rBefore = this.tbBefore_.getRow(rowIndex);
		DTRow rAfter = null;
		if (this.keys_ == null) {
			rAfter = this.tbAfter_.getRow(rowIndex);
		} else {
			if (tbAfterMap_ == null) {
				this.builderAfterKeysMap();
			}
			String keysVal1 = this.getKeysVal(rBefore);
			if (tbAfterMap_.containsKey(keysVal1)) {
				rAfter = tbAfterMap_.get(keysVal1);
			}
		}
		if (rAfter != null) { // 找到对应的数据
			UpdateChangeRow rowChanges = this.getRowChanges(rBefore, rAfter);
			return rowChanges;
		} else {
			return null;
		}
	}

	/**
	 * 获取变化
	 * 
	 * @param rBefore 原来的行
	 * @param rAfter  更新后的行
	 * @return
	 */
	public UpdateChangeRow getRowChanges(DTRow rBefore, DTRow rAfter) {
		HashMap<String, UpdateChange> map = new HashMap<String, UpdateChange>();

		UpdateChangeRow ucr = new UpdateChangeRow();
		ucr.setAfter(rAfter);
		ucr.setBefore(rBefore);
		ucr.setChanges(map);

		for (int i = 0; i < rBefore.getCount(); i++) {
			DTCell cBefore = rBefore.getCell(i);
			DTCell cAfter = rAfter.getCell(i);
			Object before = cBefore.getValue();
			Object after = cAfter.getValue();

			DTColumn col = cBefore.getColumn();
			UpdateChange uc = null;
			if (before == null && after == null) {
				continue;
			} else if (before == null && after != null || before != null && after == null) {
				uc = this.createChange(col, before, after);

			} else if (col.getTypeName() != null && (col.getTypeName().indexOf("bin") == 0
					|| col.getTypeName().equalsIgnoreCase("image") || col.getTypeName().equals("blob"))) { // 二进制

				byte[] bufBefore = (byte[]) before;
				byte[] bufAfter = (byte[]) after;
				if (bufBefore.length != bufAfter.length) {
					uc = this.createChange(col, before, after);
				} else if (Utils.md5(bufBefore).equals(Utils.md5(bufAfter))) {
					uc = this.createChange(col, before, after);
				}

			} else if (col.getTypeName() != null
					&& (col.getTypeName().indexOf("date") == 0 || col.getTypeName().indexOf("time") == 0)) { // 时间

				if (cBefore.toTime() != cAfter.toTime()) {
					uc = this.createChange(col, before, after);
				}

			} else if (!before.toString().equals(after.toString())) {
				uc = this.createChange(col, before, after);
			}
			if (uc != null) {
				map.put(uc.getCol().getName().toUpperCase(), uc);
			}
		}

		return ucr;
	}

	/**
	 * 创建变化
	 * 
	 * @param col
	 * @param before
	 * @param after
	 * @return
	 */
	private UpdateChange createChange(DTColumn col, Object before, Object after) {
		UpdateChange uc = new UpdateChange();
		uc.setCol(col);
		uc.setBefore(before);
		uc.setAfter(after);

		return uc;
	}

	/**
	 * 更新前数据
	 * 
	 * @return
	 */
	public DTTable getTbBefore() {
		return tbBefore_;
	}

	/**
	 * 更新前数据
	 * 
	 * @param tbBefore
	 */
	public void setTbBefore(DTTable tbBefore) {
		this.tbBefore_ = tbBefore;
	}

	/**
	 * 更新后数据
	 * 
	 * @return
	 */
	public DTTable getTbAfter() {
		return tbAfter_;
	}

	/**
	 * 更新后数据
	 * 
	 * @param tbAfter
	 */
	public void setTbAfter(DTTable tbAfter) {
		this.tbAfter_ = tbAfter;
	}

	/**
	 * 原始SQL
	 * 
	 * @return
	 */
	public SqlPart getSqlPart() {
		return sqlPart_;
	}

	public void setSqlPart(SqlPart sqlPart) {
		this.sqlPart_ = sqlPart;
	}

	/**
	 * 主键表达式，多个字段","分割，例如三主键 name,birth,sex
	 * 
	 * @return
	 */
	public String getKeysExp() {
		return keysExp_;
	}

	/**
	 * /主键表达式，多个字段","分割，例如三主键 name,birth,sex
	 * 
	 * @param keysExp
	 */
	public void setKeysExp(String keysExp) {
		this.keysExp_ = keysExp;
		if (keysExp_ != null && keysExp_.trim().length() > 0) {
			this.keys_ = keysExp_.split(",");
		} else {
			this.keys_ = null;
		}
	}

}
