package com.gdxsoft.easyweb.function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConfEwaFunction;
import com.gdxsoft.easyweb.conf.ConfEwaFunctions;
import com.gdxsoft.easyweb.utils.UObjectValue;

public class EwaFunctions {
	private static Logger LOGGER = LoggerFactory.getLogger(EwaFunctions.class);

	/**
	 * Call the static method from class
	 * 
	 * @param functionName
	 * @param methodParameters
	 * @return
	 */
	public static Object executeStaticFunction(String functionName, Object... methodParameters) {
		ConfEwaFunction func = ConfEwaFunctions.getInstance().getFunctions().get(functionName.toUpperCase().trim());
		if (func == null) {
			LOGGER.warn("Not found the function (" + functionName + ")");
			return null;
		}
		UObjectValue uv = new UObjectValue();
		// 静态方法调用
		try {
			return uv.invokeStatic(func.getClassName(), func.getMethodName(), methodParameters);
		} catch (Exception e) {
			LOGGER.warn("Invoke error ", func.getClassName(), func.getMethodName(), methodParameters, e.getMessage());
			return null;
		}

	}

	/**
	 * Call the function method with the construction parameters
	 * 
	 * @param functionName
	 * @param constructParameters
	 * @param methodParameters
	 * @return
	 */
	public static Object executeFunction(String functionName, Object[] constructParameters,
			Object... methodParameters) {
		ConfEwaFunction func = ConfEwaFunctions.getInstance().getFunctions().get(functionName.toUpperCase().trim());
		if (func == null) {
			LOGGER.warn("Not found the function (" + functionName + ")");
			return null;
		}
		UObjectValue uv = new UObjectValue();

		Object intance = uv.loadClass(func.getClassName(), constructParameters);
		return uv.invoke(intance, func.getMethodName(), methodParameters);
	}

}
