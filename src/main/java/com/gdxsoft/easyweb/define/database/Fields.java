/**
 * 
 */
package com.gdxsoft.easyweb.define.database;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Administrator
 * 
 */
public class Fields extends HashMap<String, Field> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4894257715150499521L;


	private ArrayList<String> _FieldList=new ArrayList<String>();

	public Fields(){
	}
	
	public ArrayList<String> getFieldList() {
		return _FieldList;
	}
}
