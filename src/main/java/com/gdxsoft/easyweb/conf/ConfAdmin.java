package com.gdxsoft.easyweb.conf;

public class ConfAdmin {

	// <Admin CreateDate="2009-09-01" LoginId="admin" Password="sdkfjsdkfsdkfsjd"
	// UserName="系统管理员" />
	String loginId;
	String password;
	String userName;
	String createDate;

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

}
