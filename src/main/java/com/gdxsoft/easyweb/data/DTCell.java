package com.gdxsoft.easyweb.data;

import java.io.Reader;
import java.io.Serializable;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.Date;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DTCell implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8222833326259177391L;
	private Object _Value;
	private DTColumn _Column;
	private DTRow _Row;

	/**
	 * @return the _Value
	 */
	public Object getValue() {
		return _Value;
	}

	/**
	 * 判断是否是NULL 郭磊 2017-08-11
	 * 
	 * @return
	 */
	public boolean isNull() {
		return _Value == null;
	}

	public String getString() {
		return this.toString();
	}

	/**
	 * @param value the _Value to set
	 */
	public void setValue(Object value) {
		if (value == null && _Value == null) {
			return;
		}
		if (value != null && _Value != null && _Value.equals(value)) {
			return;
		}

		_Value = value;

		if (this._Row != null && this._Row.getNodeRow() != null) {
			this.updateNodeRow();
		}

		if (this.getTable() != null && this.getTable().isBuildIndex() && this.getTable().getIndexes() != null) {
			this.getTable().getIndexes().update(this);
		}
	}

	/**
	 * 更新xml数据
	 */
	private void updateNodeRow() {
		if (this._Column.isXmlAttribute()) {
			Element ele = (Element) this._Row.getNodeRow();
			if (_Value == null) {
				if (this._Column.getName().equalsIgnoreCase("INNERTEXT")) {// CDATA内部数据
					ArrayList<Node> al = new ArrayList<Node>();
					for (int i = 0; i < ele.getChildNodes().getLength(); i++) {
						Node n1 = ele.getChildNodes().item(i);
						if (n1.getNodeType() == Node.CDATA_SECTION_NODE) {
							al.add(n1);
						}
					}
					for (int i = 0; i < al.size(); i++) {
						ele.removeChild(al.get(i));
					}
				} else {
					ele.removeAttribute(this._Column.getName());
				}
			} else {
				if (this._Column.getName().equalsIgnoreCase("INNERTEXT")) { // CDATA内部数据
					boolean isFound = false;
					for (int i = 0; i < ele.getChildNodes().getLength(); i++) {
						Node n1 = ele.getChildNodes().item(i);
						if (n1.getNodeType() == Node.CDATA_SECTION_NODE) {
							isFound = true;
							n1.setTextContent(_Value.toString());
							break;
						}
					}
					if (!isFound) {
						CDATASection node = ele.getOwnerDocument().createCDATASection(_Value.toString());
						ele.appendChild(node);
					}
				} else {
					ele.setAttribute(this._Column.getName(), _Value.toString());
				}
			}
			return;
		}

		Node nodeRow = this._Row.getNodeRow();
		NodeList nl = nodeRow.getChildNodes();
		Node node = null;
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			if (n.getNodeName().equals(this._Column.getName())) {
				node = n;
				break;
			}
		}
		if (node == null && this._Value != null) {
			Element ele = nodeRow.getOwnerDocument().createElement(this._Column.getName());
			nodeRow.appendChild(ele);
			if (this._Column.isXmlCData()) {
				CDATASection sec = nodeRow.getOwnerDocument().createCDATASection(_Value.toString());
				ele.appendChild(sec);
			} else {
				ele.setTextContent(_Value.toString());
			}
		} else {
			if (this._Value == null) {
				nodeRow.removeChild(node);
			} else {
				if (this._Column.isXmlCData()) {
					for (int i = 0; i < node.getChildNodes().getLength(); i++) {
						if (node.getChildNodes().item(i).getNodeType() == Node.CDATA_SECTION_NODE) {
							node.getChildNodes().item(i).setTextContent(_Value.toString());
							return;
						}
					}
					CDATASection sec = nodeRow.getOwnerDocument().createCDATASection(_Value.toString());
					node.appendChild(sec);
				} else {
					node.setTextContent(_Value.toString());
				}
			}
		}
	}

	/**
	 * @return the _Column
	 */
	public DTColumn getColumn() {
		return _Column;
	}

	/**
	 * @param column the _Column to set
	 */
	public void setColumn(DTColumn column) {
		_Column = column;
	}

	/**
	 * @return the _Row
	 */
	public DTRow getRow() {
		return _Row;
	}

	/**
	 * @param row the _Row to set
	 */
	public void setRow(DTRow row) {
		_Row = row;
	}

	/**
	 * @return the _Table
	 */
	public DTTable getTable() {
		if (this._Row == null) {
			return null;
		}
		return this._Row.getTable();
	}

	public Integer toInt() {
		if (this._Value == null) {
			return null;
		}
		try {
			int v = Integer.parseInt((this._Value.toString().split("\\.")[0]).replace(",", ""));
			return v;
		} catch (Exception err) {
			return null;
		}
	}

	public Long toLong() {
		if (this._Value == null) {
			return null;
		}
		try {
			Long v = Long.parseLong((this._Value.toString().split("\\.")[0]).replace(",", ""));
			return v;
		} catch (Exception err) {
			return null;
		}
	}

	public java.lang.Double toDouble() {
		if (this._Value == null) {
			return null;
		}
		try {
			Double v = Double.parseDouble(this._Value.toString());
			return v;
		} catch (Exception err) {
			return null;
		}
	}

	/**
	 * 返回日期
	 * 
	 * @return
	 */
	public Date toDate() {
		if (this._Value == null) {
			return null;
		}
		try {
			Date d = (Date) this._Value;
			return d;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return null;

		}
	}

	/**
	 * 返回毫秒
	 * 
	 * @return
	 */
	public long toTime() {
		Date d = this.toDate();
		if (d == null) {
			return -1;
		}
		return d.getTime();
	}

	public String toString() {
		if (this._Value == null) {
			return null;
		}
		if (this._Column.getTypeName() != null && this._Column.getTypeName().toUpperCase().equals("CLOB")) {
			Clob cb = (Clob) this._Value;
			try {
				Reader r = cb.getCharacterStream();
				char[] buf = new char[Integer.parseInt(cb.length() + "")];
				r.read(buf);
				String c = new String(buf);
				r.close();

				return c;
			} catch (Exception e) {
				return e.getLocalizedMessage();

			}
		}
		return this._Value.toString();

	}
}
