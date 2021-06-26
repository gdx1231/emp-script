package com.gdxsoft.easyweb.websocket;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.script.HtmlControl;
import com.gdxsoft.easyweb.script.RequestValue;

/**
 * 执行EWA
 */
public class HandleEwaImpl extends Thread implements IHandleMsg {
	private static Logger LOGGER = LoggerFactory.getLogger(HandleEwaImpl.class);
	public final static String METHOD = "ewa";
	private EwaWebSocketBus socket_;
	private JSONObject command_;
	private String itemName_;
	private String xmlName_;
	private String params_;

	/**
	 * 初始化对象
	 * 
	 * @param socket  IndexWebSocket
	 * @param command 调用的命令
	 */
	public HandleEwaImpl(EwaWebSocketBus socket, JSONObject command) {
		this.socket_ = socket;
		this.command_ = command;

		this.xmlName_ = command.optString("XMLNAME");
		this.itemName_ = command.optString("ITEMNAME");
		this.params_ = command.optString("PARAMS");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cc.oneWorld.websocket.IHandleMsg#run()
	 */
	@Override
	public void run() {
		JSONObject result = new JSONObject();
		result.put("METHOD", METHOD);
		// 客户端提交的ID
		if (this.command_ != null && this.command_.has("ID")) {
			result.put("ID", this.command_.optString("ID"));
		}
		try {
			String rst = this.executeEwa();

			result.put("RST", true);
			result.put("HTML", rst);
		} catch (Exception err) {
			LOGGER.error(err.getMessage());

			result.put("RST", false);
			result.put("ERR", err);
		}

		this.socket_.sendToClient(result.toString());

	}

	/**
	 * 获取我的主页消息
	 * 
	 * @param cnn
	 * @return
	 * @throws CloneNotSupportedException
	 */
	private String executeEwa() throws CloneNotSupportedException {
		HtmlControl ht = new HtmlControl();
		// 克隆rv
		RequestValue newRv = this.socket_.getRv().clone();

		newRv.resetDateTime();
		newRv.resetSysUnid();

		ht.init(this.xmlName_, this.itemName_, this.params_, newRv, null);
		String html = ht.getHtml();
		return html;
	}

	/**
	 * 获取操作类型
	 */
	public String getMethod() {
		return METHOD;
	}

}
