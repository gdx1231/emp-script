package com.gdxsoft.easyweb.data;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Node;

import com.gdxsoft.easyweb.datasource.ClassBase;
import com.gdxsoft.easyweb.utils.UObjectValue;

public class DTRow implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3704865894615681404L;
	private ArrayList<DTCell> _RowData;
	private DTTable _Table;
	private Node _NodeRow; // xml数据行
	private String _NodeRowTagName; // xml数据行的tagName
	private int _Index;
	private String _Name;
	private int _KeysExp;

	public DTRow() {
		this._RowData = new ArrayList<DTCell>();
	}

	/**
	 * 映射到 指定的对象
	 * 
	 * @param obj 指定的对象
	 * @return 指定的对象
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 */
	public Object parseToClass(Object obj)
			throws  Exception {
		UObjectValue ov = new UObjectValue();
		ov.setObject(obj);
		ov.setDaoValue(this);
		List<KeyValuePair<String, Object>> unfinds = ov.getNotFinds();
		if (unfinds != null && unfinds.size() > 0 && obj instanceof ClassBase) {
			// 对于基于ClassBase表类的对象，未找到固定属性的值，放置到扩展属性中
			ClassBase cb = (ClassBase) obj;
			for (int i = 0; i < unfinds.size(); i++) {
				KeyValuePair<String, Object> kv = unfinds.get(i);
				cb.setExt(kv.getKey(), kv.getValue());
			}
		}
		return obj;
	}

	public void addData(DTCell cell) {
		cell.setRow(this);
		while (this._RowData.size() < cell.getColumn().getIndex()) {
			// 占位
			this._RowData.add(null);
		}
		this._RowData.add(cell.getColumn().getIndex(), cell);
	}

	/**
	 * @return the _Table
	 */
	public DTTable getTable() {
		return _Table;
	}

	/**
	 * @param table the _Table to set
	 */
	protected void setTable(DTTable table) {
		_Table = table;
	}

	public int getCount() {
		return this._Table.getColumns().getCount();
	}

	public DTCell getCell(int index) {

		if (this._RowData.size() <= index || this._RowData.get(index) == null) {
			// 创建空的cell
			DTCell cell = new DTCell();
			cell.setColumn(this._Table.getColumns().getColumn(index));
			cell.setValue(null);
			this.addData(cell);
			return cell;
		}
		return this._RowData.get(index);
	}

	public DTCell getCell(String colName) throws Exception {
		int index = this._Table.getColumns().getColumn(colName).getIndex();
		return this.getCell(index);
	}

	/**
	 * 返回json对象
	 * 
	 * @return
	 * @throws JSONException
	 */
	public JSONObject toJson() throws JSONException {
		return this.toJson(null);
	}

	/**
	 * 返回json对象
	 * 
	 * @param upperOrLower UPPER or LOWER 指定字段大写或小写
	 * @return
	 * @throws JSONException
	 */
	public JSONObject toJson(String upperOrLower) throws JSONException {
		JSONObject rowJson = new JSONObject();
		for (int i = 0; i < this._Table.getColumns().getCount(); i++) {
			Object val = this._Table.getCellValueByJson(this.getCell(i), "");
			String name = this._Table.getColumns().getColumn(i).getName();
			if (upperOrLower != null) {
				if (upperOrLower.toUpperCase().equals("UPPER")) {
					name = name.toUpperCase();
				} else if (upperOrLower.toUpperCase().equals("LOWER")) {
					name = name.toLowerCase();
				}
			}
			rowJson.put(name, val);
		}
		return rowJson;
	}

	/**
	 * @return the _NodeRow
	 */
	public Node getNodeRow() {
		return _NodeRow;
	}

	/**
	 * @param nodeRow the _NodeRow to set
	 */
	public void setNodeRow(Node nodeRow) {
		_NodeRow = nodeRow;
		this._NodeRowTagName = nodeRow.getNodeName();
	}

	/**
	 * @return the _NodeRowTagName
	 */
	public String getNodeRowTagName() {
		return _NodeRowTagName;
	}

	/**
	 * @param nodeRowTagName the _NodeRowTagName to set
	 */
	public void setNodeRowTagName(String nodeRowTagName) {
		_NodeRowTagName = nodeRowTagName;
	}

	/**
	 * @return the _Index
	 */
	public int getIndex() {
		return _Index;
	}

	/**
	 * @param index the _Index to set
	 */
	public void setIndex(int index) {
		_Index = index;
	}

	/**
	 * @return the _Name
	 */
	public String getName() {
		return _Name;
	}

	/**
	 * @param name the _Name to set
	 */
	public void setName(String name) {
		_Name = name;
		if (_Name != null) {
			String n = _Name.trim().toUpperCase();
			if (!this._Table.getRows().getNameIndexes().containsKey(n)) {
				this._Table.getRows().getNameIndexes().put(n, this._Table.getCount() - 1);
			}
		}
	}

	/**
	 * @return the _KeysExp
	 */
	public int getKeysExp() {
		return _KeysExp;
	}

	/**
	 * @param keysExp the _KeysExp to set
	 */
	public void setKeysExp(int keysExp) {
		_KeysExp = keysExp;
	}
}
