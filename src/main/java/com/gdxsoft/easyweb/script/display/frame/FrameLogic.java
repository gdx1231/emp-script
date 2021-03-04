/**
 * 
 */
package com.gdxsoft.easyweb.script.display.frame;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.gdxsoft.easyweb.script.HtmlControl;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;

/**
 * @author Administrator
 * 
 */
public class FrameLogic extends FrameBase implements IFrame {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.frame.IFrame#createContent()
	 */
	public void createContent() throws Exception {

	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.frame.IFrame#createFrameContent()
	 */
	public void createFrameContent() throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.frame.IFrame#createFrameFooter()
	 */
	public void createFrameFooter() throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.frame.IFrame#createFrameHeader()
	 */
	public String createFrameHeader() throws Exception {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.frame.IFrame#createHtml()
	 */
	public void createHtml() throws Exception {
		RequestValue rv = super.getHtmlClass().getSysParas().getRequestValue();
		HttpSession session = super.getHtmlClass().getSysParas()
				.getRequestValue().getSession();
		HttpServletResponse response = super.getHtmlClass().getResponse();
		HttpServletRequest request = rv.getRequest();

		UserConfig uc = this.getHtmlClass().getUserConfig();
		String lgName = rv.getString("LG_NAME");
		UserXItem userXItem = null;
		for (int i = 0; i < uc.getUserXItems().count(); i++) {
			UserXItem uxi = uc.getUserXItems().getItem(i);
			if (uxi.getName().trim().toUpperCase().equals(
					lgName.trim().toUpperCase())) {
				userXItem = uxi;
				break;
			}
		}
		if (userXItem == null) {
			return;
		}
		String xmlName = userXItem.getSingleValue("LogicParas", "LgXmlName");
		String itemName = userXItem.getSingleValue("LogicParas", "LgItemName");
		String paras = userXItem.getSingleValue("LogicParas", "LgParas");
		// String hiddens = userXItem.getSingleValue("LogicParas", "LgHidden");
		HtmlControl ht = new HtmlControl();
		ht.init(xmlName, itemName, paras, request, session, response);
		String s = ht.getAllHtml();
		this.getHtmlClass().getDocument().addBodyHtml(s, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.frame.IFrame#createItemHtmls()
	 */
	public String createItemHtmls() throws Exception {
		return null;
	}

 

 
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.frame.IFrame#createJsFramePage()
	 */
	public void createJsFramePage() throws Exception {

	}

	 

 

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.frame.IFrame#createaXmlData()
	 */
	public String createaXmlData() throws Exception {
		return null;
	}
 
 
}
