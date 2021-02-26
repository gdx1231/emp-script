/**
 * 工作流消息的接口
 */
package com.gdxsoft.easyweb.script.Workflow;

import com.gdxsoft.easyweb.script.RequestValue;

/**
 * @author admin
 *
 */
public interface IEwaWfNotification {

	/**
	 * 通知
	 * 
	 * @param rv
	 * @param wf       工作流
	 * @param unitCur  当前节点
	 * @param unitNext 下一个节点
	 */
	public abstract void notification(RequestValue rv, EwaWf wf, EwaWfUnit unitCur, EwaWfUnit unitNext);

}
