package com.gdxsoft.easyweb.document;

import com.gdxsoft.easyweb.script.RequestValue;

public interface IWord {
	public String doWork(String tmplate, String exportName, RequestValue rv)
			throws Exception;

	public String downloadUrl();

	public String getTemplateName();

	public void setTemplateName(String templateName);

	public String getExportName();

	public void setExportName(String exportName);
	
}
