package com.gdxsoft.easyweb.cache;

/**
 * 缓存对象
 * 
 * @author admin
 *
 */
public class SqlCachedValue {

	/**
	 * 检查是否超时 true-超时
	 * @param maxLifeSeconds 存活时间
	 * @return
	 */
	public boolean checkOvertime(int maxLifeSeconds) {
		Long span = System.currentTimeMillis() - this._LastTime;
		return span  > maxLifeSeconds * 1000;
	}

	public byte[] getBinary() {
		if (this._Value == null)
			return null;
		return (byte[]) this._Value;
	}

	public String toString() {
		if (this._Value == null)
			return null;
		return this._Value.toString();
	}
	
	private String memo;

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	private Object _Value;

	public Object getValue() {
		return _Value;
	}

	public void setValue(Object _Value) {
		this._Value = _Value;
	}

	public String getType() {
		return _Type;
	}

	public void setType(String _Type) {
		this._Type = _Type;
	}

	public long getLastTime() {
		return _LastTime;
	}

	public void setLastTime(long _LastTime) {
		this._LastTime = _LastTime;
	}

	private String _Type;
	private long _LastTime;

}
