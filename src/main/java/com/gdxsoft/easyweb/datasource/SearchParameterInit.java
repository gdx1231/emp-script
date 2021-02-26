package com.gdxsoft.easyweb.datasource;

/**
 * 用于 listframe 初始化检索使用
 * 
 * @author admin
 *
 */
public class SearchParameterInit {
	private String _Name;
	private String _Para1 = "";
	private boolean _IsValid = false;
	private String _Parameter = null;
	private String _Tag;

	/**
	 * 初始化检索
	 * 
	 * @param parameter
	 *            例如 bas_tag_grp[eq]acc ， bas_tag_grp [ lk] src
	 */
	public SearchParameterInit(String parameter) {
		this._Parameter = parameter;
		this.initParameter();
	}

	private void initParameter() {
		this._IsValid = false;
		try {
			int exp_start_loc = this._Parameter.indexOf("[");
			int exp_end_loc = this._Parameter.indexOf("]");

			if (exp_start_loc < 0 || exp_end_loc < exp_start_loc) {
				return;
			}

			this._Name = this._Parameter.substring(0, exp_start_loc).trim();
			if (this._Name.trim().length() == 0) {
				return;
			}

			this._Tag = this._Parameter.substring(exp_start_loc + 1, exp_end_loc).trim();

			if (this._Tag.trim().length() == 0) {
				return;
			}

			this._Para1 = this._Parameter.substring(exp_end_loc + 1).trim();

			if (this._Para1.equals("")) {
				return;
			}
			this._IsValid = true;
		} catch (Exception err) {
			System.out.println(this + _Parameter + err.getMessage());
			this._IsValid = false;
		}

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

	/**
	 * 检索方式 eq为=，lk为 like
	 * 
	 * @return the _Tag
	 */
	public String getTag() {
		return _Tag;
	}

	@Override
	public String toString() {
		return "NAME=" + _Name + "; TAG=" + this._Tag + "; PARA1=" + this._Para1 + "; P=" + this._Parameter;
	}

	public static void main(String[] args) {
		SearchParameterInit s = new SearchParameterInit("bas_tag_grp [ lk] src ");
		System.out.println(s.toString());

		SearchParameterInit s1 = new SearchParameterInit("bas_tag_grp[eq]acc");
		System.out.println(s1.toString());
	}
}
