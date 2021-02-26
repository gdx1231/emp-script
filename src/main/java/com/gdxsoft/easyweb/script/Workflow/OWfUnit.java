package com.gdxsoft.easyweb.script.Workflow;

import com.gdxsoft.easyweb.utils.msnet.MTable;

public class OWfUnit {

	private String _UNID;
	private String _Name;
	private String _Description;
	private String _UType;
	private MTable _Froms; // OWfConnection
	private MTable _Tos;

	public OWfUnit() {
		this._Froms = new MTable();
		this._Tos = new MTable();
	}

	/**
	 * 全局编号 GUNID
	 * 
	 * @return the _Unit
	 */
	public String getUNID() {
		return _UNID;
	}

	/**
	 * 全局编号
	 * 
	 * @param unid
	 *            the _Unit to set
	 */
	public void setUNID(String unid) {
		_UNID = unid;
	}

	/**
	 * 名称
	 * 
	 * @return the _Name
	 */
	public String getName() {
		return _Name;
	}

	/**
	 * 名称
	 * 
	 * @param name
	 *            the _Name to set
	 */
	public void setName(String name) {
		_Name = name;
	}

	/**
	 * 描述
	 * 
	 * @return the _Description
	 */
	public String getDescription() {
		return _Description;
	}

	/**
	 * 描述
	 * 
	 * @param description
	 *            the _Description to set
	 */
	public void setDescription(String description) {
		_Description = description;
	}

	/**
	 * @return the _Froms
	 */
	public MTable getFroms() {
		return _Froms;
	}

	/**
	 * @param froms
	 *            the _Froms to set
	 */
	public void setFroms(MTable froms) {
		_Froms = froms;
	}

	/**
	 * @return the _Tos
	 */
	public MTable getTos() {
		return _Tos;
	}

	/**
	 * @param tos
	 *            the _Tos to set
	 */
	public void setTos(MTable tos) {
		_Tos = tos;
	}

	/**
	 * 类型
	 * 
	 * @return the _UType
	 */
	public String getUType() {
		return _UType;
	}

	/**
	 * 类型
	 * 
	 * @param type
	 *            the _UType to set
	 */
	public void setUType(String type) {
		_UType = type;
	}

}
