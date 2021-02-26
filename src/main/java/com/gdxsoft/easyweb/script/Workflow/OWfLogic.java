package com.gdxsoft.easyweb.script.Workflow;

public class OWfLogic {
	private String _Exp;
	private OWfConnection _Cnn;
	
	/**
	 * 判断是否可执行
	 * @return
	 */
	public boolean isCanRun(){
		return true;
	}
	
	/**
	 * @return the _Exp
	 */
	public String getExp() {
		return _Exp;
	}

	/**
	 * @param exp the _Exp to set
	 */
	public void setExp(String exp) {
		_Exp = exp;
	}

	/**
	 * @return the _Cnn
	 */
	public OWfConnection getCnn() {
		return _Cnn;
	}

	/**
	 * @param cnn the _Cnn to set
	 */
	public void setCnn(OWfConnection cnn) {
		_Cnn = cnn;
	}
	

}
