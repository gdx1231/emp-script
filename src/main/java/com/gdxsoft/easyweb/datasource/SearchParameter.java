package com.gdxsoft.easyweb.datasource;

public class SearchParameter {
	private String _Name;
	private String _Para1 = "";
	private String _Para2 = "";
	private boolean _IsDouble;
	private boolean _IsValid = false;
	private String _Parameter;
	private String[] _Paras;
	private String _Tag;

	public SearchParameter(String parameter) {
		_Parameter = parameter;
		initParameter();
	}

	private void initParameter() {
		String[] p1 = (_Parameter + " ").split("~!~");
		if (p1.length < 2) {
			this._IsValid = false;
			return;
		}

		_Name = p1[0].trim();
		this._Tag = p1[1].toLowerCase().trim();
		if (this._Tag.equals("fix")) {
			this._Paras = new String[p1.length - 2];
			for (int i = 2; i < p1.length; i++) {
				this._Paras[i - 2] = p1[i].trim();
			}
		} else {
			if (p1.length == 3) {
				// _Para1=XmlUtil.getUtf8(p1[1].trim());
				_Para1 = p1[2].trim();
				_IsDouble = false;
			} else if (p1.length == 4) {
				_Para1 = p1[2].trim();
				_Para2 = p1[3].trim();
				_IsDouble = true;
			}
		}
		if (!(this._Para1.equals("") && _Para2.equals("") && this._Paras ==null)) {
			this._IsValid = true;
		}
	}

	public boolean isDouble() {
		return _IsDouble;
	}

	public boolean isValid() {
		return this._IsValid;
	}

	public String getName() {
		return _Name;
	}

	public String getPara1() {
		return _Para1;
	}

	public String getPara2() {
		return _Para2;
	}

	/**
	 * @return the _Paras
	 */
	public String[] getParas() {
		return _Paras;
	}

	/**
	 * @return the _Tag
	 */
	public String getTag() {
		return _Tag;
	}
}
