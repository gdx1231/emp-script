package com.gdxsoft.easyweb.datasource;

import java.util.ArrayList;
import java.util.List;

public class EwaSqlFunction {
	private String rvParamName;
	private String functionExp;
	private String groupConstructorParameters;
	private String groupMethodParameters;
	private String functionName;
	private List<String> constructorParameters = new ArrayList<>();
	private List<String> methodParameters = new ArrayList<>();

	private boolean staticCall = true;
	
	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public List<String> getConstructorParameters() {
		return constructorParameters;
	}

	public List<String> getMethodParameters() {
		return methodParameters;
	}

	public String getGroupConstructorParameters() {
		return groupConstructorParameters;
	}

	public void setGroupConstructorParameters(String groupConstructorParameters) {
		this.groupConstructorParameters = groupConstructorParameters;
	}

	public String getGroupMethodParameters() {
		return groupMethodParameters;
	}

	public void setGroupMethodParameters(String groupMethodParameters) {
		this.groupMethodParameters = groupMethodParameters;
	}

	public String getFunctionExp() {
		return functionExp;
	}

	public void setFunctionExp(String functionExp) {
		this.functionExp = functionExp;
	}

	public String getRvParamName() {
		return rvParamName;
	}

	public void setRvParamName(String rvParamName) {
		this.rvParamName = rvParamName;
	}

	public boolean isStaticCall() {
		return staticCall;
	}

	public void setStaticCall(boolean staticCall) {
		this.staticCall = staticCall;
	}

}
