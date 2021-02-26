package com.gdxsoft.easyweb.script;
import com.gdxsoft.easyweb.utils.*;
public class InitValues {

	private int _SeqId = 0;

	public String getInitValue(String name) {
		if (name.equalsIgnoreCase("SEQID")) {
			this._SeqId++;
			return this._SeqId + "";
		}
		if(name.equalsIgnoreCase("GUID")){
			return Utils.getGuid();	
		}
		if(name.equalsIgnoreCase("ZERO")){
			return "0";	
		}
		return null;
	}
	
}
