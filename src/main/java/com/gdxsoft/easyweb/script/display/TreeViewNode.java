package com.gdxsoft.easyweb.script.display;

import java.util.ArrayList;

public class TreeViewNode {
	private String _strKey;
	private String _strDispVal;
	private String _strParentKey;
	private String _strMenuGroup = "";
	private String _strJavaScriptCmd; // 执行脚本
	private String _strTitle; // 节点提示信息

	private String _IconOpen;
	private String _IconClose;
	private boolean _IsMoreChild = false; // 更多的子节点，用于分层调用
	private TreeViewNode _ParentNode;
	private TreeViewNode _NextNode;
	private TreeViewNode _PrevNode;
	private ArrayList<TreeViewNode> _ChildNodes;
	private ArrayList<String> _AddParas = new ArrayList<String>();
	public static String NODE_ROOT_KEY = TreeViewMain.TREE_ROOT_KEY;// EWA_TREE_ROOT

	// 关联的数据
	private Object _Data;

	/**
	 * 关联的数据
	 * 
	 * @return
	 */
	public Object getData() {
		return _Data;
	}

	/**
	 * 关联的数据
	 * 
	 * @param data 关联的数据
	 */
	public void setData(Object data) {
		this._Data = data;
	}

	public TreeViewNode() {
		_ChildNodes = new ArrayList<TreeViewNode>();
	}

	/**
	 * @return the _IconClose
	 */
	public String getIconClose() {
		return _IconClose;
	}

	/**
	 * @param iconClose the _IconClose to set
	 */
	public void setIconClose(String iconClose) {
		_IconClose = iconClose;
	}

	/**
	 * @return the _IconOpen
	 */
	public String getIconOpen() {
		return _IconOpen;
	}

	/**
	 * @param iconOpen the _IconOpen to set
	 */
	public void setIconOpen(String iconOpen) {
		_IconOpen = iconOpen;
	}

	public String getDispVal() {
		return _strDispVal;
	}

	public void setDispVal(String dispVal) {
		_strDispVal = dispVal;
	}

	public String getJavaScriptCmd() {
		return _strJavaScriptCmd;
	}

	public void setJavaScriptCmd(String javaScriptCmd) {
		_strJavaScriptCmd = javaScriptCmd;
	}

	public String getKey() {
		return _strKey;
	}

	public void setKey(String key) {
		_strKey = key;
	}

	public String getParentKey() {
		return _strParentKey;
	}

	public void setParentKey(String parentKey) {
		_strParentKey = parentKey;
	}

	/**
	 * @return the _ParentNode
	 */
	public TreeViewNode getParentNode() {
		return _ParentNode;
	}

	/**
	 * @param parentNode the _ParentNode to set
	 */
	public void setParentNode(TreeViewNode parentNode) {
		_ParentNode = parentNode;
	}

	/**
	 * @return the _ChildNodes
	 */
	public ArrayList<TreeViewNode> getChildNodes() {
		return _ChildNodes;
	}

	/**
	 * @return the _NextNode
	 */
	public TreeViewNode getNextNode() {
		return _NextNode;
	}

	/**
	 * @param nextNode the _NextNode to set
	 */
	public void setNextNode(TreeViewNode nextNode) {
		_NextNode = nextNode;
	}

	/**
	 * @return the _PrevNode
	 */
	public TreeViewNode getPrevNode() {
		return _PrevNode;
	}

	/**
	 * @param prevNode the _PrevNode to set
	 */
	public void setPrevNode(TreeViewNode prevNode) {
		_PrevNode = prevNode;
	}

	/**
	 * @return the _IsMoreChild
	 */
	public boolean isMoreChild() {
		return _IsMoreChild;
	}

	/**
	 * @param isMoreChild the _IsMoreChild to set
	 */
	public void setMoreChild(boolean isMoreChild) {
		_IsMoreChild = isMoreChild;
	}

	/**
	 * @return the _AddParas
	 */
	public ArrayList<String> getAddParas() {
		return _AddParas;
	}

	/**
	 * @param addParas the _AddParas to set
	 */
	public void setAddParas(ArrayList<String> addParas) {
		_AddParas = addParas;
	}

	/**
	 * @return the _strMenuGroup
	 */
	public String getMenuGroup() {
		return _strMenuGroup;
	}

	/**
	 * @param menuGroup the _strMenuGroup to set
	 */
	public void setMenuGroup(String menuGroup) {
		_strMenuGroup = menuGroup == null ? "" : menuGroup;
	}

	/**
	 * 节点提示信息
	 * 
	 * @return the _strTitle
	 */
	public String getTitle() {
		return _strTitle;
	}

	/**
	 * 节点提示信息
	 * 
	 * @param title the _strTitle to set
	 */
	public void setTitle(String title) {
		_strTitle = title;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Key=" + this.getKey() + ", DispVal=" + this.getDispVal() + ", Title=" + this.getTitle() + ", PKey="
				+ this.getParentKey());
		return sb.toString();
	}

}
