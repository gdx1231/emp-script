package com.gdxsoft.easyweb.script;

import java.util.Iterator;

import org.apache.log4j.Logger;

import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class PageValues {
	private static Logger LOGGER = Logger.getLogger(PageValues.class);
	private MTable _Values;

	public PageValues() {
		_Values = new MTable();
		PageValueTag[] tags = PageValueTag.values();
		for (int i = 0; i < tags.length; i++) {
			_Values.add(tags[i], new MTable());
		}
	}

	public PageValue getPageValue(String key) {
		if (key == null)
			return null;
		String key1 = key.trim().toUpperCase();
		PageValueTag[] tags = PageValueTag.getOrder();
		for (int i = 0; i < tags.length; i++) {
			MTable mt = this.getTagValues(tags[i]);
			if (mt.containsKey(key1)) {
				return (PageValue) mt.get(key1);
			}
		}
		return null;
	}

	public MTable getTagValues(PageValueTag pvTag) {
		MTable mt = (MTable) this._Values.get(pvTag);
		return mt;
	}

	/**
	 * 添加数据，已存在则不添加
	 * 
	 * @param key   名称
	 * @param val   值
	 * @param pvTag 来源
	 */
	public void addValue(String key, Object val, PageValueTag pvTag) {
		PageValue pv = new PageValue();
		String key1 = key.trim().toUpperCase();
		pv.setName(key1);
		pv.setValue(val);
		pv.setPVTag(pvTag);
		pv.setTag(pvTag.toString());

		// oracle 出现错误，因此暂时禁止自动检测类型
		// pv.autoDetectDataType();

		this.addValue(pv);

	}

	/**
	 * 添加或修改数据
	 * 
	 * @param key
	 * @param val
	 * @param pvTag
	 */
	public void addOrUpdateValue(String key, Object val, PageValueTag pvTag) {
		this.remove(key);
		this.addValue(key, val, pvTag);
	}

	/**
	 * 从所有对象中删除Key
	 * 
	 * @param key
	 */
	public void remove(String key) {
		Iterator<Object> it = this._Values.getTable().keySet().iterator();
		while (it.hasNext()) {
			Object key1 = it.next();
			MTable mt = (MTable) this._Values.get(key1);
			mt.removeKey(key.toUpperCase().trim());
		}

	}

	/**
	 * 从指定来源对象中删除key
	 * 
	 * @param key
	 * @param pvTag
	 */
	public void remove(String key, PageValueTag pvTag) {
		String key1 = key.trim().toUpperCase();
		MTable mt = (MTable) this._Values.get(pvTag);
		if (mt.containsKey(key1)) {
			mt.removeKey(key1);
		}
	}

	/**
	 * 添加或替换参数值
	 * 
	 * @param pv
	 */
	public void addOrUpdateValue(PageValue pv) {
		this.remove(pv.getName());
		this.addValue(pv);
	}

	/**
	 * 添加数据,如果已经存在则不覆盖
	 * 
	 * @param pv
	 */
	public void addValue(PageValue pv) {

		// if(pv.getName().equals("HOM_ROOM_TYPE")){
		// int aaa=0;
		// aaa++;
		// }
		MTable mt = (MTable) this._Values.get(pv.getPVTag());
		pv.setTag(pv.getPVTag().toString());

		// 如果pv没有设定类型，根据ewa_conf.xml中定义的数据类型进行设置
		if (pv.getDataType() == null || pv.getDataType().trim().length() == 0) {
			String key = pv.getName().toUpperCase();
			if (UPath.getRvTypes() == null) {
				LOGGER.warn("UPath.getRvTypes()is null, pv=" + key);
			} else {
				if (UPath.getRvTypes().containsKey(key)) {
					// paramName, paramType
					String paramType = UPath.getRvTypes().get(key);
					String paramValue = pv.getStringValue();
					if (paramValue != null && "int".equals(paramType)) {
						// 测试整型
						try {
							int intVal = Integer.parseInt(paramValue);
							pv.setValue(intVal);
							pv.setDataType(paramType);
						} catch (Exception err) {

						}
					} else if (paramValue != null && "number".equals(paramType)) {
						// 测试数字
						try {
							double douVal = Double.parseDouble(paramValue);
							pv.setValue(douVal);
							pv.setDataType(paramType);
						} catch (Exception err) {

						}
					} else {
						pv.setDataType(paramType);
					}
				}
			}
		}

		if (!mt.containsKey(pv.getName())) {
			mt.add(pv.getName(), pv);
		}
	}

	public PageValue getValue(String name) {
		PageValue pv = this.getPageValue(name);
		return pv;
	}

	public String getString(String name) {
		PageValue pv = this.getPageValue(name);
		if (pv == null)
			return null;
		return pv.getValue() == null ? null : pv.getValue().toString();
	}

	public Object getObject(String name) {
		PageValue pv = this.getPageValue(name);
		if (pv == null)
			return null;
		return pv.getValue();
	}

	public PageValue getPageValue(String name, PageValueTag pvTag) {
		MTable mt = this.getTagValues(pvTag);
		String name1 = name.toUpperCase().trim();
		if (mt.containsKey(name1)) {
			PageValue pv = (PageValue) mt.get(name1);
			return pv;
		} else {
			return null;
		}
	}

	public String getQueryValue(String name) {
		PageValue pv = this.getPageValue(name, PageValueTag.QUERY_STRING);
		return pv == null ? null : pv.getStringValue();

	}

	public String getFormValue(String name) {
		PageValue pv = this.getPageValue(name, PageValueTag.FORM);
		return pv == null ? null : pv.getStringValue();
	}

	public String getCookieValue(String name) {
		PageValue pv = this.getPageValue(name, PageValueTag.COOKIE_ENCYRPT);
		if (pv == null) {
			pv = this.getPageValue(name, PageValueTag.COOKIE);
		}
		return pv == null ? null : pv.getStringValue();
	}

	public Object getSessionObject(String name) {
		PageValue pv = this.getPageValue(name, PageValueTag.SESSION);
		return pv == null ? null : pv.getValue();
	}

	public String getSessionValue(String name) {
		PageValue pv = this.getPageValue(name, PageValueTag.SESSION);
		return pv == null ? null : pv.getStringValue();
	}

	/**
	 * @return the _QueryValues
	 */
	public MTable getQueryValues() {
		return (MTable) this._Values.get(PageValueTag.QUERY_STRING);
	}

	/**
	 * @return the _FormValues
	 */
	public MTable getFormValues() {
		return (MTable) this._Values.get(PageValueTag.FORM);
	}

	/**
	 * @return the _CookieValues
	 */
	public MTable getCookieValues() {
		MTable c0 = (MTable) this._Values.get(PageValueTag.COOKIE_ENCYRPT);
		MTable c1 = (MTable) this._Values.get(PageValueTag.COOKIE);
		c0.conact(c1, false);
		return c0;
	}

	/**
	 * @return the _SessionValues
	 */
	public MTable getSessionValues() {
		return (MTable) this._Values.get(PageValueTag.SESSION);
	}

	/**
	 * 克隆对象
	 * 
	 * @return
	 */
	public PageValues clone() {
		PageValues pvs = new PageValues();
		this.copyTo(pvs);
		return pvs;
	}

	/**
	 * 复制到参数到目标对象
	 * 
	 * @param pvs
	 */
	public void copyTo(PageValues target) {
		PageValueTag[] tags = PageValueTag.values();
		for (int i = 0; i < tags.length; i++) {
			PageValueTag tag = tags[i];

			MTable tb = (MTable) this._Values.get(tag);
			MTable tb1 = (MTable) target._Values.get(tag);

			for (Object key : tb.getTable().keySet()) {
				if (key == null) {
					// 不复制key 为 NULL值
					continue;
				}
				PageValue pv = (PageValue) tb.get(key);
				PageValue pv1 = pv.clone();
				tb1.put(key, pv1);
			}
		}
	}
}
