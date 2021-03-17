package com.gdxsoft.easyweb.function;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.SystemXmlUtils;
import com.gdxsoft.easyweb.utils.UObjectValue;

public class EwaFunctions {
	private static final String CFG_NAME = "EwaFunctions.xml";
	private static Logger LOGGER = LoggerFactory.getLogger(EwaFunctions.class);
	private static EwaFunctions INST = null;

	public static EwaFunctions getInstance() {
		if (INST != null) {
			return INST;
		}
		INST = createNewScriptPaths();
		return INST;
	}

	private synchronized static EwaFunctions createNewScriptPaths() {
		try {
			Document doc = SystemXmlUtils.getSystemConfDocument(CFG_NAME);

			EwaFunctions sps = new EwaFunctions();
			NodeList nl = doc.getElementsByTagName("EwaFunction");
			for (int i = 0; i < nl.getLength(); i++) {
				Element item = (Element) nl.item(i);
				EwaFunction sp = new EwaFunction();
				UObjectValue.fromXml(item, sp);
				sps.functions.put(sp.getName().toUpperCase().trim(), sp);
			}
			return sps;
		} catch (Exception e) {
			LOGGER.error("load " + CFG_NAME, e.getMessage());
			return null;
		}

	}

	public static Object executeStaticFunction(String functionName, Object... methodParameters) {
		EwaFunction func = getInstance().getFunctions().get(functionName.toUpperCase().trim());
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

	public static Object executeFunction(String functionName, Object[] construnctParameters,
			Object... methodParameters) {
		EwaFunction func = getInstance().getFunctions().get(functionName.toUpperCase().trim());
		if (func == null) {
			LOGGER.warn("Not found the function (" + functionName + ")");
			return null;
		}
		UObjectValue uv = new UObjectValue();

		Object intance = uv.loadClass(func.getClassName(), construnctParameters);
		return uv.invoke(intance, func.getMethodName(), methodParameters);
	}

	private Map<String, EwaFunction> functions;

	public EwaFunctions() {
		this.functions = new HashMap<>();
	}

	public Map<String, EwaFunction> getFunctions() {
		return this.functions;
	}

}
