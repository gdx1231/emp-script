package com.gdxsoft.easyweb.script.display;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class HtmlCombineItem {

	public static HtmlCombineItem parseFrom(Node itemXml) {
		HtmlCombineItem item = new HtmlCombineItem();

		Element ele = (Element) itemXml;
		item.setId(ele.getAttribute("id"));
		item.setDes(ele.getAttribute("des"));
		item.setX(ele.getAttribute("x"));
		item.setI(ele.getAttribute("i"));
		item.setP(ele.getAttribute("p"));
		item.setGrp(ele.getAttribute("grp"));
		item.setInstall(ele.getAttribute("install"));
		item.setJsRename(ele.getAttribute("js_rename"));
		
		item.setItemXml(itemXml);
		
		return item;
	}

	public String getId() {
		return id_;
	}

	public void setId(String id_) {
		this.id_ = id_;
	}

	public String getDes() {
		return des_;
	}

	public void setDes(String des_) {
		this.des_ = des_;
	}

	public String getInstall() {
		return install_;
	}

	public void setInstall(String install_) {
		this.install_ = install_;
	}

	public String getGrp() {
		return grp_;
	}

	public void setGrp(String grp) {
		this.grp_ = grp;
	}

	public String getX() {
		return x_;
	}

	public void setX(String x_) {
		this.x_ = x_;
	}

	public String getI() {
		return i_;
	}

	public void setI(String i_) {
		this.i_ = i_;
	}

	public String getP() {
		return p_;
	}

	public void setP(String p_) {
		this.p_ = p_;
	}

	public String getJsRename() {
		return jsRename_;
	}

	public void setJsRename(String jsRename_) {
		this.jsRename_ = jsRename_;
	}

	private String des_;
	private String install_;
	private String grp_;
	private String x_;
	private String i_;
	private String p_;
	private String jsRename_;
	private String id_;
	
	private Node itemXml_;

	public Node getItemXml() {
		return itemXml_;
	}

	public void setItemXml(Node itemXml_) {
		this.itemXml_ = itemXml_;
	}
}
