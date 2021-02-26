package com.gdxsoft.easyweb.script.Workflow;

import java.io.UnsupportedEncodingException;

import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.HtmlClass;
import com.gdxsoft.easyweb.script.display.ItemValues;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItems;
import com.gdxsoft.easyweb.utils.ULogic;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class WfUnits {

	private MTable _Units;
	private String _WfCheckField;
	private String _WfLogAction;
	private HtmlClass _HtmlClass;
	private String _WfMainAction;
	private String _WfXItem;

	public WfUnits(String checkedField, String mainAction, String logAction) {
		_Units = new MTable();
		this._WfCheckField = checkedField;
		_WfLogAction = logAction;
		this._WfMainAction = mainAction;
	}

	/**
	 * 加载工作流节点
	 * 
	 * @param uc
	 * @param lang
	 * @throws Exception
	 */
	public void load(UserConfig uc, String lang) throws Exception {
		UserXItems ws = uc.getUserWorkflows();
		if (ws == null) {
			throw new Exception("工作流节点未定义");
		}
		if (ws.count() == 1) {
			throw new Exception("工作流节点未定义不足，至少两个");
		}
		WfUnit prev = null;
		for (int i = 0; i < ws.count(); i++) {
			UserXItem item = ws.getItem(i);
			WfUnit u = this.loadUnit(item, lang);
			this._Units.add(u.getName(), u);
			if (i == ws.count() - 1 || u.getWfType().equalsIgnoreCase("end")) {
				u.setLast(true);
			}
			if (i == 0) {
				u.setFirst(true);
			}
			if (prev != null && prev.getWFANextYes().length() == 0) {
				prev.setWFANextYes(u.getName());
			}
			prev = u;
		}

	}

	WfUnit loadUnit(UserXItem item, String lang) {
		String name = item.getName();
		String WfType = item.getSingleValue("WfType");
		String WfLogic = item.getSingleValue("WfLogic");
		String des = item
				.getItemValue("DescriptionSet", "Lang=" + lang, "Info");
		String WFABefore = item.getSingleValue("WfAction", "WFABefore");
		String WFABeforeMsg = item.getSingleValue("WfAction", "WFABeforeMsg");
		String WFAAfter = item.getSingleValue("WfAction", "WFAAfter");
		String WFANextYes = item.getSingleValue("WfAction", "WFANextYes");
		String WFANextNo = item.getSingleValue("WfAction", "WFANextNo");

		WfUnit u = new WfUnit();
		u.setDes(des);
		u.setName(name.trim().toUpperCase());
		u.setWFAAfter(WFAAfter);
		u.setWFABefore(WFABefore);
		u.setWFABeforeMsg(WFABeforeMsg);
		u.setWFANextNo(WFANextNo);

		u.setWFANextYes(WFANextYes);
		u.setWfType(WfType);

		u.setWfLogic(WfLogic);
		return u;
	}

	public WfRst runNext() {
		WfRst r = new WfRst();
		// 先获取当前状态
		ItemValues iv = this._HtmlClass.getItemValues();
		RequestValue rv = iv.getRequestValue();
		String tmp=rv.getRequest().getQueryString();
		System.out.print(tmp);
		//
		String fromWfName = rv.getString("EWA_WF_NAME");
		if (fromWfName == null) {
			r.setIsOk(false);
			r.setMsg("找不到EWA_WF_NAME");
			return r;
		}

		WfUnit cur = this.getUnit(fromWfName);
		if (cur == null) {
			r.setIsOk(false);
			r.setMsg("无效的到EWA_WF_NAME[" + fromWfName + "]");
			return r;

		}
		r.setUnit(cur);
		if (cur.isLast()) {
			r.setIsOk(false);
			r.setMsg("已经是最后一步了");
			return r;
		}
		// 检测是否有权限
		if (cur.getWfLogic() != null && cur.getWfLogic().trim().length() > 0) {
			String loc = iv.replaceParameters(cur.getWfLogic(), false, true);
			if (!ULogic.runLogic(loc)) {
				r.setIsOk(false);
				r.setMsg("您没有权限操作");
				return r;
			}
		}

		this.executeAction("OnPageLoad", r);
		if (r.isException()) {
			return r;
		}
		if (iv.getDTTables().size() == 0) {
			r.setIsOk(false);
			r.setMsg("找不到加载的数据/OnPageLoad");
			return r;
		}
		DTTable tb = (DTTable) iv.getDTTables().getLast();

		// 传递过来的主键值
		String keyValue = iv.getRequestValue().getString("EWA_ACTION_KEY");
		if (keyValue == null) {
			r.setIsOk(false);
			r.setMsg("找不到keyValue");
			return r;
		}
		String[] keyValues = keyValue.split(",");

		// 获取配置文件主键表达式
		UserXItem pv = this._HtmlClass.getUserConfig().getUserPageItem();
		String key = pv.getSingleValue("PageSize", "KeyField");
		if (key == null || key.trim().length() == 0) {
			r.setIsOk(false);
			r.setMsg("配置文件未定义PageSize-KeyField");
			return r;
		}
		String[] keys = key.trim().split(",");
		// 表建索引
		tb.getColumns().setKeys(keys);
		tb.rebuildIndex();
		DTRow row;
		String curWfName = null;
		try {
			row = tb.getRowByKeys(keys, keyValues);
			curWfName = row.getCell(this._WfCheckField).getString();
		} catch (Exception e) {
			r.setIsException(true);
			r.setIsOk(false);
			r.setException(e.getMessage());
			r.setMsg("执行 获取行 出错");
			return r;
		}

		if (!fromWfName.equalsIgnoreCase(curWfName)) {
			r.setIsOk(false);
			r.setMsg("数据状态发生变更，请重新刷新页面");
			return r;
		}

		// 执行检查
		if (cur.getWFABefore() != null
				&& cur.getWFABefore().trim().length() > 0) {
			this.executeAction(cur.getWFABefore(), r);
			if (r.isException()) {
				return r;
			}
			DTTable tb1 = (DTTable) this._HtmlClass.getItemValues()
					.getDTTables().getLast();
			if (tb1.getCount() > 0) {
				r.setIsOk(false);
				r.setMsg(cur.getWFABeforeMsg());
				return r;
			}
		}
		boolean yes = true;

		String v = iv.getRequestValue().getString("EWA_WF_UOK");
		if (v == null) { // 没有交互先显示界面
			String WfXmlName = pv.getSingleValue("Workflow", "WfXmlName");
			String WfItemName = pv.getSingleValue("Workflow", "WfItemName");
			String WfCallPara = pv.getSingleValue("Workflow", "WfCallPara");
			if (WfXmlName == null || WfXmlName.trim().length() == 0
					|| WfItemName == null || WfItemName.trim().length() == 0) {
				r.setIsOk(false);
				r.setMsg("配置项未定义");
				return r;
			}
			String des;
			try {
				des = java.net.URLEncoder.encode(cur.getDes(), "utf-8");
			} catch (UnsupportedEncodingException e) {
				des = e.getMessage();
			}
			String u = "./?XMLNAME=" + WfXmlName + "&ITEMNAME=" + WfItemName
					+ "&EWA_WF_NAME=" + cur.getName() + "&EWA_WF_TYPE="
					+ cur.getWfType() + "&EWA_ACTION_KEY=" + keyValue
					+ "&EWA_ID=" + rv.getString("EWA_ID") + "&EWA_WF_DES="
					+ des;
			if (WfCallPara != null && WfCallPara.trim().length() > 0) {
				u += "&" + WfCallPara.trim();
			}
			String js = "EWA.UI.Dialog.OpenWindow('" + u
					+ "','selectxitem',300,200)";
			r.setIsOk(true);
			r.setMsg(js);
			return r;
		}

		// 控制节点要取得 是否成功信息
		if (cur.getWfType().equalsIgnoreCase("control")) { // ctrl节点
			v = v.trim().toLowerCase();
			yes = false;
			if (v.equals("1") || v.indexOf("yes") >= 0 || v.indexOf("OK") >= 0
					|| v.equals("true") || v.indexOf("是") >= 0
					|| v.equals("同意")) {
				yes = true;
			}
		} else {
			yes = true;
		}

		// 执行下一步
		// EWA_WF_NAME/名称, EWA_WF_MSG/消息, EWA_WF_NEXT/下一步,
		// EWA_WF_UMSG/用户提交信息, EWA_WF_UOK/用户是否提交是否状态,
		// EWA_WF_ROK/是否执行成功, EWA_WF_RMSG/执行提示信息

		rv.addValue("EWA_WF_MSG", cur.getDes());
		rv.addValue("EWA_WF_UOK", yes ? 1 : 0);

		if (yes) {
			if (cur.getWFANextYes() == null
					|| cur.getWFANextYes().trim().length() == 0) {
				r.setIsOk(false);
				r.setMsg("WFANextYes not defined");
				return r;

			}
			rv.addValue("EWA_WF_NEXT", cur.getWFANextYes());

		} else {
			if (cur.getWFANextNo() == null
					|| cur.getWFANextNo().trim().length() == 0) {
				r.setIsOk(false);
				r.setMsg("WFANextNo not defined");
				return r;

			}
			rv.addValue("EWA_WF_NEXT", cur.getWFANextNo());

		}
		// 修改状态
		this.executeAction(this._WfMainAction, r);
		// 日志记录
		if (this._WfLogAction != null && this._WfLogAction.length() > 0) {
			rv.addValue("EWA_WF_ROK", r.isOk());
			rv.addValue("EWA_WF_RMSG", r.getException());
			this.executeAction(this._WfLogAction, null);
		}
		if (r.isException()) {
			return r;
		}
		r.setIsOk(true);
		return r;
	}

	private void executeAction(String actionName, WfRst r) {
		try {
			this._HtmlClass.getHtmlCreator().executeAction(actionName);
		} catch (Exception e) {
			if (r != null) {
				r.setIsException(true);
				r.setIsOk(false);
				r.setException(e.getMessage());
				r.setMsg("执行 [" + actionName + "] 出错");
			}
		}
	}

	public void addUnit(WfUnit unit) {
		this._Units.put(unit.getName().trim().toUpperCase(), unit);
	}

	/**
	 * 根据名称获取节点
	 * 
	 * @param name
	 * @return
	 */
	public WfUnit getUnit(String name) {
		if (name == null) {
			return null;
		}
		String n = name.trim().toUpperCase();
		if (this._Units.containsKey(n)) {
			return (WfUnit) this._Units.get(n);
		} else {
			return null;
		}
	}

	/**
	 * 根据行数据获取 WfUnit;
	 * 
	 * @param row
	 * @return
	 * @throws Exception
	 */
	public WfUnit getUnit(DTRow row) throws Exception {
		String curWfName = row.getCell(this._WfCheckField).getString();
		if (curWfName == null || curWfName.trim().length() == 0) {
			return null;
		}
		WfUnit wf = this.getUnit(curWfName);
		if (wf == null) {
			throw new Exception("Not found " + curWfName + "，该节点未定义");
		}
		return wf;
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
	 * @return the _WfCheckField
	 */
	public String getWfCheckField() {
		return _WfCheckField;
	}

	/**
	 * @param wfCheckField
	 *            the _WfCheckField to set
	 */
	public void setWfCheckField(String wfCheckField) {
		_WfCheckField = wfCheckField;
	}

	/**
	 * @return the _WfLogAction
	 */
	public String getWfLogAction() {
		return _WfLogAction;
	}

	/**
	 * @param wfLogAction
	 *            the _WfLogAction to set
	 */
	public void setWfLogAction(String wfLogAction) {
		_WfLogAction = wfLogAction;
	}

	/**
	 * @return the _HtmlClass
	 */
	public HtmlClass getHtmlClass() {
		return _HtmlClass;
	}

	/**
	 * @param htmlClass
	 *            the _HtmlClass to set
	 */
	public void setHtmlClass(HtmlClass htmlClass) {
		_HtmlClass = htmlClass;
	}

	/**
	 * 更新状态主调用
	 * 
	 * @return the _WfMainAction
	 */
	public String getWfMainAction() {
		return _WfMainAction;
	}

	/**
	 * @param wfMainAction
	 *            the _WfMainAction to set
	 */
	public void setWfMainAction(String wfMainAction) {
		_WfMainAction = wfMainAction;
	}

	/**
	 * 获取Item调用URL
	 * 
	 * @return the _WfXItem
	 */
	public String getWfXItem() {
		ItemValues iv = this._HtmlClass.getItemValues();
		RequestValue rv = iv.getRequestValue();
		UserXItem pv = this._HtmlClass.getUserConfig().getUserPageItem();

		String WfXmlName = pv.getSingleValue("Workflow", "WfXmlName");
		String WfItemName = pv.getSingleValue("Workflow", "WfItemName");
		String WfCallPara = pv.getSingleValue("Workflow", "WfCallPara");

		String keyValue = rv.getString("EWA_ACTION_KEY");
		String ewaWfCtrl = rv.getString("EWA_WF_CTRL");
		String u = "./?XMLNAME=" + WfXmlName + "&ITEMNAME=" + WfItemName
				+ "&EWA_ACTION_KEY=" + keyValue + "&EWA_WF_CTRL=" + ewaWfCtrl
				+ "&EWA_ID=" + rv.getString("EWA_ID");
		if (WfCallPara != null && WfCallPara.trim().length() > 0) {
			u += "&" + WfCallPara.trim();
		}
		String js = "EWA.UI.Dialog.OpenWindow('" + u
				+ "','selectxitem',300,200)";
		return js;
	}
}
