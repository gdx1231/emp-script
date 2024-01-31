package com.gdxsoft.easyweb.script.idempotance;

import java.util.ArrayList;
import java.util.List;

import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.Utils;

/**
 * 数据库操作演示的例子
 */
public abstract class OpJdbcDempImpl extends OpBase implements IOp {

	private static String SQL_NEW = "insert into ewa_idempotance(key,val) values(@key, @val)";
	private static String SQL_GET = "select val from ewa_idempotance where key=@key";
	private static String SQL_DEL = "delete from ewa_idempotance where key=@key";

	private String configName = "demo";

	/**
	 * Idempotance值，保存到系统中
	 */
	public void save() {
		String idempotanceValue = this.generateValue();
		// 幂等性KEY

		RequestValue rv = new RequestValue();
		rv.addOrUpdateValue("key", this.generateKey());
		rv.addOrUpdateValue("val", idempotanceValue);
		List<String> sqls = new ArrayList<>();
		sqls.add(SQL_DEL);
		sqls.add(SQL_NEW);
		
		DataConnection.updateBatchAndClose(sqls, configName, rv);
	}

	public String generateKey() {
		if (key == null) {
			RequestValue rv = htmlClass.getSysParas().getRequestValue();
			key = Utils.md5(rv.getSession().getId() + "_" + uxi.getName().toUpperCase() + "_"
					+ htmlClass.getSysParas().getFrameUnid());
		}
		return key;
	}

	/**
	 * 获取系统保存的Idempotance值
	 * 
	 * @return
	 */
	public String getValue() {
		RequestValue rv = new RequestValue();
		rv.addOrUpdateValue("key", this.generateKey());
		DTTable tb = DTTable.getJdbcTable(SQL_GET, configName, rv);
		if (tb.getCount() == 0) {
			return null;
		}
		return tb.getCell(0, 0).toString();
	}

	/**
	 * 检查是否匹配，只能执行一次，因为清除了session值
	 * 
	 * @return
	 */
	public boolean checkOnlyOnce() {
		// 从前端传递的值
		String idempotenceValue = this.getIdempontance();

		String jdbcValue = this.getValue();
		if (jdbcValue != null) {
			RequestValue rv1 = new RequestValue();
			rv1.addOrUpdateValue("key", this.generateKey());
			DataConnection.updateAndClose(SQL_DEL, configName, rv1);
		}
		return idempotenceValue != null && idempotenceValue.length() == 32 && idempotenceValue.equals(jdbcValue);
	}

}
