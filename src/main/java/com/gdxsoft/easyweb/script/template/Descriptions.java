/**
 * 
 */
package com.gdxsoft.easyweb.script.template;

import java.io.Serializable;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.UXml;

/**
 * @author Administrator
 * 
 */
public class Descriptions extends SetBase<Description> implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6769202754235497042L;
	private static UObjectValue _OV = new UObjectValue();

	/**
	 * 实例化描述
	 * @param nodeDescriptions xml节点
	 * @return
	 */
	public static Descriptions instanceDescriptions(Node nodeDescriptions) {
		Descriptions descriptions = new Descriptions();
		NodeList nl = UXml.retNodeList(nodeDescriptions, "DescriptionSet/Set");
		if (nl == null) {
			nl = UXml.retNodeList(nodeDescriptions, "Set");
		}
		if (nl != null) {
			for (int i = 0; i < nl.getLength(); i++) {
				Description d = initDescription(nl.item(i));
				descriptions.addObject(d, d.getLang());
			}
		}
		return descriptions;
	}

	private static Description initDescription(Node node) {
		Description d = new Description();
		initClass(node, d);
		if(d.getMemo()==null){
			d.setMemo(UXml.retNodeText(node));
		}
		return d;
	}

	private static void initClass(Node node, Object o) {
		_OV.setObject(o);
		_OV.setAllValue((Element) node);
	}

	public Description getDescription(String lang) throws Exception {
		if (super.count() == 0) {
			return null;
		}
		Description d = null;

		for (int i = 0; i < super.count(); i++) {
			d = super.getItem(i);
			
			if (d.getLang()!=null &&  d.getLang().equalsIgnoreCase(lang)) {
				break;
			}
		}
		if (d == null) {
			d = super.getItem(0);
		}
		return d;
	}

}
