package com.gdxsoft.easyweb.datasource;

import javax.servlet.http.HttpServletRequest;

public class ScriptPostData {

	private String _FieldName;

	private String _FieldType;

	private int _FieldLength;

	private String _FieldValue;

	private boolean _IsEncrypt;

	public int getFieldLength() {
		return _FieldLength;
	}

	public String getFieldName() {
		return _FieldName;
	}

	public String getFieldType() {
		return _FieldType;
	}

	public String getFieldValue() {
		return _FieldValue;
	}

	public ScriptPostData(String fieldDescription, HttpServletRequest request) {
		String[] obj = fieldDescription.split("\\|");
		if (obj[1].trim() == "")
			obj[1] = "string";
		if (obj[0].trim() == "")
			return;
		this._FieldType = obj[1].toLowerCase();

		String RKey, RVal;

		RKey = obj[0].trim();
		RVal = request.getParameter(RKey);
		this._FieldValue = RVal;
		this._FieldName = RKey;

		if (_FieldType.equals("string") || _FieldType.equals("")) {
			int Size = obj[2].equals("") ? 200 : Integer.parseInt(obj[2]);
			if(Size==0)
				Size=400000;
			this._FieldLength = Size;
			
			if (RVal == null)
				return;

			if (RVal.length() > Size) {
				RVal = RVal.substring(0, Size);
				this._FieldValue = RVal;
			}
		}
		if (obj[3].equals("1")) {
			this._IsEncrypt = true;
		}
	}

	public ScriptPostData(){
		
	}
	
	public boolean isEncrypt() {
		return _IsEncrypt;
	}

	/**
	 * @param fieldName the _FieldName to set
	 */
	public void setFieldName(String fieldName) {
		_FieldName = fieldName;
	}

	/**
	 * @param fieldType the _FieldType to set
	 */
	public void setFieldType(String fieldType) {
		_FieldType = fieldType;
	}

	/**
	 * @param fieldLength the _FieldLength to set
	 */
	public void setFieldLength(int fieldLength) {
		_FieldLength = fieldLength;
	}

	/**
	 * @param fieldValue the _FieldValue to set
	 */
	public void setFieldValue(String fieldValue) {
		_FieldValue = fieldValue;
	}

	/**
	 * @param isEncrypt the _IsEncrypt to set
	 */
	public void setIsEncrypt(boolean isEncrypt) {
		_IsEncrypt = isEncrypt;
	}
}
