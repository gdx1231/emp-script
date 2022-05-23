package com.gdxsoft.easyweb.script.display.items;

import com.gdxsoft.easyweb.script.template.SkinFrame;

/**
 * Signature item
 * 
 * @author admin
 *
 */
public class ItemSignature extends ItemBase {
	public String createItemHtml() throws Exception {
		String s1 = super.getXItemFrameHtml();
		String val = super.getValue();
		s1 = s1.replace(SkinFrame.TAG_VAL, val == null ? "" : val);
		return s1.trim();
	}
}
