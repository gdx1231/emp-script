package com.gdxsoft.easyweb.script.display;

import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import com.gdxsoft.easyweb.debug.DebugFrames;
import com.gdxsoft.easyweb.global.EwaGlobals;
import com.gdxsoft.easyweb.script.InitValues;
import com.gdxsoft.easyweb.script.Workflow.WfUnits;
import com.gdxsoft.easyweb.script.display.action.IAction;
import com.gdxsoft.easyweb.script.display.frame.IFrame;
import com.gdxsoft.easyweb.script.display.items.IItem;
import com.gdxsoft.easyweb.script.display.items.ItemBase;
import com.gdxsoft.easyweb.script.html.HtmlDocument;
import com.gdxsoft.easyweb.script.template.EwaConfig;
import com.gdxsoft.easyweb.script.template.Skin;
import com.gdxsoft.easyweb.script.template.SkinFrame;
import com.gdxsoft.easyweb.script.template.SkinFrames;
import com.gdxsoft.easyweb.script.template.XItem;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.utils.UObjectValue;

public class HtmlClass {
	private Skin _Skin;
	private SkinFrames _SkinFrames;
	private SkinFrame _SkinFrameAll;
	private SkinFrame _SkinFrameCurrent;
	private UserConfig _UserConfig;
	private ItemValues _ItemValues;
	private EwaGlobals _EwaGlobals;
	private InitValues _InitValues = new InitValues(); // 初始值类
	private DebugFrames _DebugFrames;
	private HttpServletResponse _Response;
	private HashMap<UserXItem, IItem> _Items = new HashMap<UserXItem, IItem>();
	private IAction _Action;
	private HtmlCreator _HtmlCreator;
	private WfUnits _Workflow;
	private SysParameters _SysParas;

	private IFrame _Frame;
	private HtmlDocument _Document;

	public IItem getItem(UserXItem userXItem) {
		if (_Items.containsKey(userXItem)) {
			return _Items.get(userXItem);
		}
		IItem item;
		XItem xItem;
		try {
			xItem = HtmlUtils.getXItem(userXItem);
			String className = xItem.getClassName();
			item = this.getItem(className);
		} catch (Exception e) {
			item = this.getDefaultItem();
			System.err.println(e);
		}
		item.setHtmlClass(this);

		item.setUserXItem(userXItem);
		item.setInitValues(_InitValues);
		item.setResponse(_Response);
		_Items.put(userXItem, item);
		return item;
	}

	private IItem getDefaultItem() {
		return new ItemBase();
	}

	private IItem getItem(String className) throws Exception {
		UObjectValue o = new UObjectValue();
		Object item = o.loadClass(className, null);
		return (IItem) item;
	}

	public IFrame getFrame() throws Exception {
		IFrame item = this.getFrameItem();

		item.setHtmlClass(this);

		return item;
	}

	public IAction getAction() throws Exception {
		if (_Action == null) {
			IAction item = this.getActionItem();
			item.setResponse(_Response);
			item.setHtmlClass(this);
			this._Action = item;
		}
		return this._Action;
	}

	private IAction getActionItem() throws Exception {
		EwaConfig cfg = EwaConfig.instance();
		String className = cfg.getConfigFrames()
				.getItem(_SysParas.getFrameType()).getActionClassName().trim();
		UObjectValue o = new UObjectValue();
		try {
			this._DebugFrames.addDebug(this, "CLASS", "加载Action类(" + className
					+ ")");
			Object item = o.loadClass(className, null);
			return (IAction) item;
		} catch (Exception e) {
			this._DebugFrames.addDebug(this, "ERR", e.toString());
			throw e;
		}
	}

	private IFrame getFrameItem() throws Exception {
		if (_Frame == null) {
			EwaConfig cfg = EwaConfig.instance();
			String className = cfg.getConfigFrames()
					.getItem(_SysParas.getFrameType()).getFrameClassName();
			UObjectValue o = new UObjectValue();
			Object item = o.loadClass(className, null);
			_Frame = (IFrame) item;
		}
		return _Frame;
	}

	/**
	 * @return the _SkinFrames
	 */
	public SkinFrames getSkinFrames() {
		return _SkinFrames;
	}

	/**
	 * @param skinFrames
	 *            the _SkinFrames to set
	 */
	public void setSkinFrames(SkinFrames skinFrames) {
		_SkinFrames = skinFrames;
	}

	/**
	 * @return the _SkinFrameAll
	 */
	public SkinFrame getSkinFrameAll() {
		return _SkinFrameAll;
	}

	/**
	 * @param skinFrameAll
	 *            the _SkinFrameAll to set
	 */
	public void setSkinFrameAll(SkinFrame skinFrameAll) {
		_SkinFrameAll = skinFrameAll;
	}

	/**
	 * @return the _SkinFrameCurrent
	 */
	public SkinFrame getSkinFrameCurrent() {
		return _SkinFrameCurrent;
	}

	/**
	 * @param skinFrameCurrent
	 *            the _SkinFrameCurrent to set
	 */
	public void setSkinFrameCurrent(SkinFrame skinFrameCurrent) {
		_SkinFrameCurrent = skinFrameCurrent;
	}

	/**
	 * @return the _UserConfig
	 */
	public UserConfig getUserConfig() {
		return _UserConfig;
	}

	/**
	 * @param userConfig
	 *            the _UserConfig to set
	 */
	public void setUserConfig(UserConfig userConfig) {
		_UserConfig = userConfig;
	}

	/**
	 * @return the _ItemValues
	 */
	public ItemValues getItemValues() {
		return _ItemValues;
	}

	/**
	 * @param itemValues
	 *            the _ItemValues to set
	 */
	public void setItemValues(ItemValues itemValues) {
		_ItemValues = itemValues;
	}

	/**
	 * @return the _Skin
	 */
	public Skin getSkin() {
		return _Skin;
	}

	/**
	 * @param skin
	 *            the _Skin to set
	 */
	public void setSkin(Skin skin) {
		_Skin = skin;
	}

	/**
	 * @return the _EwaGlobals
	 */
	public EwaGlobals getEwaGlobals() {
		return _EwaGlobals;
	}

	/**
	 * @param ewaGlobals
	 *            the _EwaGlobals to set
	 */
	public void setEwaGlobals(EwaGlobals ewaGlobals) {
		_EwaGlobals = ewaGlobals;
	}

	/**
	 * @return the _DebugFrames
	 */
	public DebugFrames getDebugFrames() {
		return _DebugFrames;
	}

	/**
	 * @param debugFrames
	 *            the _DebugFrames to set
	 */
	public void setDebugFrames(DebugFrames debugFrames) {
		_DebugFrames = debugFrames;
	}

	/**
	 * @return the _Response
	 */
	public HttpServletResponse getResponse() {
		return _Response;
	}

	/**
	 * @param response
	 *            the _Response to set
	 */
	public void setResponse(HttpServletResponse response) {
		_Response = response;
	}

	/**
	 * @return the _SysParas
	 */
	public SysParameters getSysParas() {
		return _SysParas;
	}

	/**
	 * @param sysParas
	 *            the _SysParas to set
	 */
	public void setSysParas(SysParameters sysParas) {
		_SysParas = sysParas;
	}

	/**
	 * @return the _Document
	 */
	public HtmlDocument getDocument() {
		return _Document;
	}

	/**
	 * @param document
	 *            the _Document to set
	 */
	public void setDocument(HtmlDocument document) {
		_Document = document;
	}

	/**
	 * 主类
	 * 
	 * @return the _HtmlCreator
	 */
	public HtmlCreator getHtmlCreator() {
		return _HtmlCreator;
	}

	/**
	 * @param htmlCreator
	 *            the _HtmlCreator to set
	 */
	public void setHtmlCreator(HtmlCreator htmlCreator) {
		_HtmlCreator = htmlCreator;
	}

	/**
	 * @return the _Workflow
	 */
	public WfUnits getWorkflow() {
		return _Workflow;
	}

	/**
	 * @param workflow
	 *            the _Workflow to set
	 */
	public void setWorkflow(WfUnits workflow) {
		_Workflow = workflow;
		_Workflow.setHtmlClass(this);
	}
}
