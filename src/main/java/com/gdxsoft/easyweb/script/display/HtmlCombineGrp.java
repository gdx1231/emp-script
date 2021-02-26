package com.gdxsoft.easyweb.script.display;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.w3c.dom.Document;

public class HtmlCombineGrp {

	private HashMap<String, ArrayList<HtmlCombineItem>> map_;
	private Document combineDoc_;
	private ArrayList<String> grpIndex_;

	public HtmlCombineGrp(Document combineDoc) {
		combineDoc_ = combineDoc;
		map_ = new HashMap<String, ArrayList<HtmlCombineItem>>();
		grpIndex_ = new ArrayList<String>();
	}

	public ArrayList<String> getGrpIndex() {
		return grpIndex_;
	}

	public void init() {
		NodeList nl = combineDoc_.getElementsByTagName("item");

		// 先循环，以便将数据放到Rv中
		for (int i = 0; i < nl.getLength(); i++) {
			Node item = nl.item(i);

			HtmlCombineItem ci = HtmlCombineItem.parseFrom(item);

			if (ci.getGrp() != null) {
				if (!map_.containsKey(ci.getGrp())) {
					map_.put(ci.getGrp(), new ArrayList<HtmlCombineItem>());
					grpIndex_.add(ci.getGrp());
				}
				map_.get(ci.getGrp()).add(ci);
			}
		}
	}

	public HashMap<String, ArrayList<HtmlCombineItem>> getMap() {
		return map_;
	}

	public Document getCombineDoc() {
		return combineDoc_;
	}
}
