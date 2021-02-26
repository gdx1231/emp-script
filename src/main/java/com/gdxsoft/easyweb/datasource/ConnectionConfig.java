/**
 * 
 */
package com.gdxsoft.easyweb.datasource;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.msnet.MTableStr;
/**
 * @author Administrator
 * 
 */
public class ConnectionConfig {

	private String _Name;
	private String _Type;
	private String _ConnectionString;
	private String _SchemaName;

	private MTableStr _Pool;
	/**
	 * @return the _Name
	 */
	public String getName() {
		return _Name;
	}

	/** 数据库类型，如MSSQL，ORACLE ...
	 * @return the _Type
	 */
	public String getType() {
		return _Type;
	}

	/**
	 * @return the _ConnectionString
	 */
	public String getConnectionString() {
		return _ConnectionString;
	}

	/**
	 * @return the _SchemaName
	 */
	public String getSchemaName() {
		return _SchemaName;
	}
	public ConnectionConfig(){
		// create structure;
	}
	public void setName(String _Name) {
		this._Name = _Name;
	}

	public void setType(String _Type) {
		this._Type = _Type;
	}

	public void setConnectionString(String _ConnectionString) {
		this._ConnectionString = _ConnectionString;
	}

	public void setSchemaName(String _SchemaName) {
		this._SchemaName = _SchemaName;
	}

	public ConnectionConfig(Node node) {
		this.setObj(node);
	}

	private void setObj(Node node) {
		this._Name = GetNodeValue(node, "Name").toLowerCase();
		this._ConnectionString = GetNodeValue(node, "ConnectionString");
		this._Type = GetNodeValue(node, "Type");
		_SchemaName = GetNodeValue(node, "SchemaName");
		
		Element ele=(Element)node;
		if(ele.getElementsByTagName("pool").getLength()>0){
			this._Pool=new MTableStr();
			Element p = (Element)ele.getElementsByTagName("pool").item(0);
			for(int i=0;i<p.getAttributes().getLength();i++){
				Node att = p.getAttributes().item(i);
				_Pool.add(att.getNodeName(),att.getNodeValue());
			}
		}
	}

	/**
	 * 获取节点的Attribute的值
	 * 
	 * @param node
	 *            节点
	 * @param name
	 *            名称
	 * @return 值，null转换为""
	 */
	private String GetNodeValue(Node node, String name) {
		String s1 = UXml.retNodeValue(node, name).trim();
		return s1;
	}

	/**
	 * @return the _Pool
	 */
	public MTableStr getPool() {
		return _Pool;
	}

	/**
	 * @param pool the _Pool to set
	 */
	public void setPool(MTableStr pool) {
		_Pool = pool;
	}

	 
}