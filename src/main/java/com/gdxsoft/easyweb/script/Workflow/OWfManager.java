package com.gdxsoft.easyweb.script.Workflow;

public class OWfManager {

	private OWfWorkflow _Workflow;

	public OWfManager() {

	}

	public void init(String xmlName, String itemName) throws Throwable {
		_Workflow = new OWfWorkflow();
		_Workflow.init(xmlName, itemName);
	}

	public void start(){
		OWfUnit startUnit = _Workflow.getUStart();
		
	}
	
	/**
	 * @return the _Workflow
	 */
	public OWfWorkflow getWorkflow() {
		return _Workflow;
	}

	/**
	 * @param workflow
	 *            the _Workflow to set
	 */
	public void setWorkflow(OWfWorkflow workflow) {
		_Workflow = workflow;
	}

}
