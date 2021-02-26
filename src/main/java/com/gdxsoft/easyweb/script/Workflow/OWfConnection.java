package com.gdxsoft.easyweb.script.Workflow;

public class OWfConnection {
	private String _Unid;
	
	private OWfUnit _From;
	private OWfUnit _To;
	private OWfLogic _Logic;

	/**
	 * @return the _From
	 */
	public OWfUnit getFrom() {
		return _From;
	}

	/**
	 * @param from
	 *            the _From to set
	 */
	public void setFrom(OWfUnit from) {
		_From = from;
		
	}

	/**
	 * @return the _To
	 */
	public OWfUnit getTo() {
		return _To;
	}

	/**
	 * @param to
	 *            the _To to set
	 */
	public void setTo(OWfUnit to) {
		_To = to;
	}

	/**
	 * @return the _Logic
	 */
	public OWfLogic getLogic() {
		return _Logic;
	}

	/**
	 * @param logic
	 *            the _Logic to set
	 */
	public void setLogic(OWfLogic logic) {
		_Logic = logic;
		_Logic.setCnn(this);
	}

	/**
	 * @return the _Unid
	 */
	public String getUnid() {
		return _Unid;
	}

	/**
	 * @param unid the _Unid to set
	 */
	public void setUnid(String unid) {
		_Unid = unid;
	}

}
