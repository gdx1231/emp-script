package com.gdxsoft.easyweb.cache;

/**
 * cahce文件读取结果
 * 
 * FILE_NOT_EXISTS 文件不存在<br>
 * OK 成功<br>
 * OVERTIME 超过缓存时间<br>
 * NO_VALID 不合法，hash值不对，可能是文件生成时错误<br>
 * OTHER 其他<br>
 * 
 * @author Administrator
 * 
 */
public enum CacheLoadResult {
	FILE_NOT_EXISTS, OK, OVERTIME, NO_VALID, OTHER
}
