package com.gdxsoft.easyweb.script.Workflow;

import org.w3c.dom.*;

import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class OWfWorkflow {
	private MTable _Units;
	private MTable _Cnns;
	private OWfUnit _UStart;
	private OWfUnit _UCur;

	public OWfWorkflow() {
		_Units = new MTable();
		_Cnns = new MTable();
	}

	public void init(String xmlName, String itemName) throws Throwable {
		Document doc = UXml.retDocument(xmlName);
		Element eleItem = UXml.findNode(doc.getDocumentElement(), "EwaWf",
				"Name", itemName, true);
		if (eleItem == null) {
			throw new Exception("NOT found " + itemName + " from " + xmlName);
		}
		this.load(eleItem);
	}

	void load(Element eleItem) {
		// 业务元
		NodeList nl = eleItem.getElementsByTagName("Unit");
		for (int i = 0; i < nl.getLength(); i++) {

			Element ele = (Element) nl.item(i);
			OWfUnit unit = this.loadUnit(ele);
			_Units.put(unit.getUNID(), unit);
			if (i == 0) {
				this._UStart = unit;
			}
		}
		// 连接
		nl = eleItem.getElementsByTagName("Cnn");
		for (int i = 0; i < nl.getLength(); i++) {
			Element ele = (Element) nl.item(i);
			OWfConnection cnn = loadCnn(ele);
			this._Cnns.add(cnn.getUnid(), cnn);

		}
	}

	OWfConnection loadCnn(Element ele) {
		OWfConnection cnn = new OWfConnection();
		String unid = ele.getAttribute("Unid");
		String from = ele.getAttribute("From");
		String to = ele.getAttribute("To");
		String logic = ele.getAttribute("Logic");

		OWfLogic lg = new OWfLogic();
		lg.setExp(logic);

		cnn.setUnid(unid);
		cnn.setLogic(lg);

		OWfUnit uf = this.getUnit(from);
		OWfUnit ut = this.getUnit(to);
		cnn.setFrom(uf);
		cnn.setTo(ut);

		uf.getFroms().add(cnn.getUnid(), cnn);
		ut.getTos().add(cnn.getUnid(), cnn);
		return cnn;
	}

	OWfUnit loadUnit(Element ele) {
		OWfUnit unit = new OWfUnit();
		String unid = ele.getAttribute("Unid");
		String name = ele.getAttribute("Name");
		String des = ele.getAttribute("Des");
		String type = ele.getAttribute("Type");

		unit.setDescription(des);
		unit.setUNID(unid);
		unit.setName(name);
		unit.setUType(type);
		return unit;
	}

	public OWfUnit getUnit(String unid) {
		if (this._Units.containsKey(unid)) {
			return (OWfUnit) this._Units.get(unid);
		}
		return null;
	}

	public OWfConnection getCnn(String unid) {
		if (this._Cnns.containsKey(unid)) {
			return (OWfConnection) this._Cnns.get(unid);
		}
		return null;
	}

	/**
	 * @return the _Units
	 */
	public MTable getUnits() {
		return _Units;
	}

	/**
	 * @param units
	 *            the _Units to set
	 */
	public void setUnits(MTable units) {
		_Units = units;
	}

	/**
	 * @return the _UStart
	 */
	public OWfUnit getUStart() {
		return _UStart;
	}

	/**
	 * @param start
	 *            the _UStart to set
	 */
	public void setUStart(OWfUnit start) {
		_UStart = start;
	}

	/**
	 * @return the _UCur
	 */
	public OWfUnit getUCur() {
		return _UCur;
	}

	/**
	 * @param cur
	 *            the _UCur to set
	 */
	public void setUCur(OWfUnit cur) {
		_UCur = cur;
	}
}
