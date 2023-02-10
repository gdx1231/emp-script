package com.gdxsoft.easyweb.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.KeyValuePair;
import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.PageValueTag;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.msnet.MTable;
import com.gdxsoft.easyweb.utils.types.UInt16;
import com.gdxsoft.easyweb.utils.types.UInt32;
import com.gdxsoft.easyweb.utils.types.UInt64;

/**
 * Reflect the class by name
 */
public class UObjectValue {
	public static final String CAST_ERROR = "___UObjectValue______CAST_ERROR_______UObjectValue____";
	public static Map<String, Integer> GLOBL_CACHED = new ConcurrentHashMap<>();
	private static Logger LOGGER = LoggerFactory.getLogger(UObjectValue.class);

	/**
	 * Set the target class setRequestValue
	 * 
	 * @param instance The targetClass
	 * @param rv       the RequestValue
	 * @return true: successful /false: fail
	 */
	public static boolean setRequestValue(Object instance, RequestValue rv) {
		Method[] methods = instance.getClass().getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			Method m = methods[i];
			if (m.getParameterTypes().length != 1) {
				continue;
			}
			String paraClassName = m.getParameterTypes()[0].getName();
			if (paraClassName.equalsIgnoreCase("com.gdxsoft.easyweb.script.RequestValue")) {
				Object[] val = new Object[1];
				val[0] = rv;
				String rst = setValue(instance, m, val);
				if (rst == null) {
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	/**
	 * Set the corresponding attributes of the target element through the get
	 * methods of the source class
	 * 
	 * @param targetElement The target element
	 * @param source        the source class
	 */
	public static void writeXmlNodeAtts(Element targetElement, Object source) {
		UObjectValue o = new UObjectValue();
		o.setObject(source);

		for (int i = 0; i < o.getGetterMethods().size(); i++) {
			Method m = o.getGetterMethods().get(i);
			String name = m.getName();
			if (name.toUpperCase().indexOf("GET") == 0) {
				name = name.substring(3);
			} else if (name.toUpperCase().indexOf("IS") == 0) {
				name = name.substring(2);
			}
			// return the get method value
			String v = o.getValue(m);

			if (v != null) {
				targetElement.setAttribute(name, o.getValue(m));
			}
		}
	}

	/**
	 * Create the corresponding child text nodes of the target element through the
	 * get methods of the source class
	 * 
	 * @param targetElement The target element
	 * @param source        The source class
	 */
	public static void writeXmlNodeTexts(Element targetElement, Object source) {
		UObjectValue o = new UObjectValue();
		o.setObject(source);

		for (int i = 0; i < o.getGetterMethods().size(); i++) {
			Method m = o.getGetterMethods().get(i);
			String name = m.getName();

			if (name.toUpperCase().indexOf("GET") == 0) {
				name = name.substring(3);
			} else if (name.toUpperCase().indexOf("IS") == 0) {
				name = name.substring(2);
			}
			String v = o.getValue(m);
			if (v != null) {
				// Create the child node by class method
				Node n = UXml.retNode(targetElement, name);
				Element ele;
				if (n == null) {
					ele = targetElement.getOwnerDocument().createElement(name);
				} else {
					ele = (Element) n;
				}
				ele.setTextContent(v);
				targetElement.appendChild(ele);
			}
		}
	}

	/**
	 * Set the corresponding parameters of the target class through the children
	 * text content of the source element
	 * 
	 * @param parentNode  the source node
	 * @param targetClass the target class
	 */
	public static void fromXml(Node parentNode, Object targetClass) {
		fromXml((Element) parentNode, targetClass);
	}

	/**
	 * Set the corresponding parameters of the target class through the children
	 * text content of the source element
	 * 
	 * @param parentElement the source element
	 * @param targetClass   the target class
	 */
	public static void fromXmlNodes(Element parentElement, Object targetClass) {
		UObjectValue o = new UObjectValue();
		o.setObject(targetClass);
		for (int i = 0; i < parentElement.getChildNodes().getLength(); i++) {
			Node n = parentElement.getChildNodes().item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element ele = (Element) n;
				String name = ele.getNodeName();
				String v = ele.getTextContent();
				if (o.setValueAccurate("set" + name, v) == null) {
					o.setValueAccurate("is" + name, v);
				}
			}
		}
	}

	/**
	 ** Set the corresponding parameters of the target class through the attributes
	 * of the source element
	 * 
	 * @param ele         the source element
	 * @param targetClass the target class
	 */
	public static void fromXml(Element ele, Object targetClass) {
		UObjectValue o = new UObjectValue();
		o.setObject(targetClass);
		o.setAllValue(ele);
	}

	/**
	 * Set the target class parameter base on the method
	 * 
	 * @param instance target class
	 * @param method   the method
	 * @param para     the set value
	 * @return the result, when null is successful else is the error message
	 */
	public static String setValue(Object instance, Method method, Object[] para) {
		Object[] newVals = new Object[method.getParameterTypes().length];
		for (int i = 0; i < method.getParameterTypes().length; i++) {
			newVals[i] = convert(method.getParameterTypes()[i], para[i]);
			if (newVals[i] != null && newVals[i].toString().equals(CAST_ERROR)) {
				return CAST_ERROR;
			}
			if (newVals[i] != null && newVals[i].toString().equals("[com.gdxsoft.easyweb.script.RequestValue]")) {
				newVals[i] = para[i]; // 恢复rv
			}

		}
		try {
			method.invoke(instance, newVals);
			return null;
		} catch (IllegalArgumentException e) {
			LOGGER.error("{},{},{}", instance, method, e.getMessage());
			return e.getMessage();
		} catch (IllegalAccessException e) {
			LOGGER.error("{},{},{}", instance, method, e.getMessage());
			return e.getMessage();
		} catch (InvocationTargetException e) {
			LOGGER.error("{},{},{}", instance, method, e.getMessage());
			return e.getMessage();
		}
	}

	/**
	 * Convert to target object
	 * 
	 * @param t   the target object class
	 * @param src the source object
	 * @return the converted object
	 */
	public static Object convert(Class<?> t, Object src) {
		if (src == null)
			return null;
		if (t.isArray()) {
			if (src.getClass().getName().equals("java.lang.String")) {
				String[] v1 = src.toString().split(",");
				for (int i = 0; i < v1.length; i++) {
					v1[i] = v1[i].trim();

				}
				return v1;
			}
			return src;
		}
		String name = t.getName();
		if (name.equals("com.gdxsoft.easyweb.script.RequestValue")) {
			return "[com.gdxsoft.easyweb.script.RequestValue]";
		}
		if (name.equals("int")) {
			return UConvert.ToInt32(src.toString());
		} else if (name.equals("boolean")) {
			return Utils.cvtBool(src);
		} else if (name.equals("double")) {
			return Double.parseDouble(src.toString());
		} else if (name.equals("long")) {
			return Long.parseLong(src.toString());
		} else if (name.equals("float")) {
			return Float.parseFloat(src.toString());
		} else if (name.equals("byte")) {
			return Byte.parseByte(src.toString());
		} else if (name.equals("java.util.Date")) {
			return Utils.getDate(src.toString());
		}
		try {
			Object o = t.cast(src);
			return o;
		} catch (Exception e) {
			LOGGER.info("cast error {}", e.getMessage());
			return CAST_ERROR;
		}
	}

	private Object _object;
	private Method[] _Methods;
	private ArrayList<Method> _GetterMethods;
	private Object _Instance;
	private String _LastErrMsg;
	private RequestValue _Rv;
	private Method[] _MethodsSuper;
	private Method[] _MethodsSuper2;
	private Method[] _MethodsSuper3;
	// 未找到的方法名称
	private List<KeyValuePair<String, Object>> _NotFinds;

	public UObjectValue() {

	}

	/**
	 * Set the corresponding parameters of the specified class through the
	 * attributes of the source element
	 * 
	 * @param ele the source element
	 */
	public void setAllValue(Element ele) {
		for (int i = 0; i < ele.getAttributes().getLength(); i++) {
			String name = ele.getAttributes().item(i).getNodeName();

			if (this.setValueAccurate("set" + name, ele.getAttribute(name)) != null) {
				this.setValueAccurate("is" + name, ele.getAttribute(name));
			}
		}
	}

	/**
	 * Get the specified class all get methods value，String, boolean, int...
	 * 
	 * @return the all get methods value
	 */
	public ArrayList<String[]> getAllValue() {
		ArrayList<String[]> al = new ArrayList<String[]>();
		Object[] b = null;
		for (int i = 0; i < this._Methods.length; i++) {
			String name = this._Methods[i].getName();
			Class<?> returnType = this._Methods[i].getReturnType();
			if (name.equals("hashCode") || name.equals("toString") || _Methods[i].getParameterTypes().length > 0
					|| !(returnType.equals(String.class) || returnType.equals(java.util.Date.class)
							|| returnType.getName().equals("boolean") || returnType.getName().equals("int"))) {
				continue;
			}
			try {
				String ret;
				Object a = this._Methods[i].invoke(this._object, b);
				if (a == null) {
					ret = "";
				} else {
					if (returnType.equals(java.util.Date.class)) {
						ret = Utils.getDateTimeString((java.util.Date) a);
					} else {
						ret = a.toString();
					}
				}
				String[] s1 = new String[] { name, ret };
				al.add(s1);
			} catch (Exception e) {
			}
		}
		return al;
	}

	/**
	 * Return the get method result
	 * 
	 * @param method the get method
	 * @return the return value
	 */
	public String getValue(Method method) {
		String ret;
		Object[] b = null;
		String s = method.getReturnType().getName();
		try {
			Object a = method.invoke(this._object, b);
			if (a == null) {
				ret = null;
			} else {
				if (s.equals("java.util.Date")) {
					Date d = (Date) a;
					ret = Utils.getDateTimeString(d);
				} else {
					ret = a.toString();
				}
			}
		} catch (IllegalArgumentException e) {
			ret = e.getMessage();
		} catch (IllegalAccessException e) {
			ret = e.getMessage();
		} catch (InvocationTargetException e) {
			ret = e.getMessage();
		}
		return ret;
	}

	/**
	 * Get the value based on LIKE the get-method name
	 * 
	 * @param methodName the get-method name
	 * @return the get-method returns value
	 */
	public String getValue(String methodName) {
		String n1 = methodName.toLowerCase().trim();
		String ret = "";
		for (int i = 0; i < this._Methods.length; i++) {
			String name = this._Methods[i].getName().toLowerCase();

			if (this._Methods[i].getParameterTypes().length > 0 || name.indexOf(n1) < 0) {
				continue;
			}
			ret = this.getValue(this._Methods[i]);
			break;
		}
		return ret;
	}

	/**
	 * Get the value based on the get-method name
	 * 
	 * @param methodName the get-method name
	 * @return the get-method returns value
	 */
	public String getValueAccurate(String methodName) {
		String n1 = methodName.toLowerCase().trim();
		String ret = "";
		for (int i = 0; i < this._Methods.length; i++) {
			String name = this._Methods[i].getName().toLowerCase();

			if (this._Methods[i].getParameterTypes().length > 0 || !name.equals(n1)) {
				continue;
			}
			ret = this.getValue(this._Methods[i]);
			break;
		}
		return ret;
	}

	/**
	 * Get the value based on the method name without "get/is"
	 * 
	 * @param methodName the method name without "get/is"
	 * @return the get method returns value
	 */
	public Object getProperty(String methodName) {
		Method m = this.getPropertyMethod(methodName);
		if (m == null) {
			return null;
		}
		Object[] methodValues = new Object[0];

		return this.invoke(this.getObject(), m, methodValues);
	}

	/**
	 * 获取属性对应的方法
	 * 
	 * @param methodName
	 * @return
	 */
	public Method getPropertyMethod(String methodName) {
		Object[] methodValues = new Object[0];
		String cachedkey = this.createMethodCachedKey("getPropertyMethod-" + methodName, methodValues);
		if (GLOBL_CACHED.containsKey(cachedkey)) {
			return this._Methods[GLOBL_CACHED.get(cachedkey)];
		}
		// 获取super的模式
		String cachedkeySupper = this.createMethodCachedKey("getPropertyMethod-from-supper-" + methodName,
				methodValues);
		if (this._MethodsSuper != null && GLOBL_CACHED.containsKey(cachedkeySupper)) {
			return this._MethodsSuper[GLOBL_CACHED.get(cachedkeySupper)];
		}
		// 获取super的模式
		String cachedkeySupper2 = this.createMethodCachedKey("getPropertyMethod-from-supper2-" + methodName,
				methodValues);
		
		if (this._MethodsSuper2 != null && GLOBL_CACHED.containsKey(cachedkeySupper2)) {
			return this._MethodsSuper2[GLOBL_CACHED.get(cachedkeySupper2)];
		}

		String cachedkeySupper3 = this.createMethodCachedKey("getPropertyMethod-from-supper3-" + methodName,
				methodValues);
		if (this._MethodsSuper3 != null && GLOBL_CACHED.containsKey(cachedkeySupper3)) {
			return this._MethodsSuper3[GLOBL_CACHED.get(cachedkeySupper3)];
		}

		Method m0 = getMethod(this._Methods, methodName, 0, cachedkeySupper);
		if (m0 != null) {
			return m0;
		}
		if (this._MethodsSuper == null) { // 获取super的模式
			return null;
		}
		Method m1 = getMethod(this._MethodsSuper, methodName, 0, cachedkeySupper);
		if (m1 != null) {
			return m1;
		}
		if (this._MethodsSuper2 == null) { // 获取super2的模式
			return null;
		}
		Method m2 = getMethod(this._MethodsSuper2, methodName, 0, cachedkeySupper2);
		if (m2 != null) {
			return m2;
		}
		if (this._MethodsSuper3 == null) { // 获取super3的模式
			return null;
		}
		Method m3 = getMethod(this._MethodsSuper3, methodName, 0, cachedkeySupper3);

		return m3;

	}

	private Method getMethod(Method[] methods, String methodName, int getParametersLength, String cachedkey) {
		String name1 = methodName.toLowerCase().trim().replace("_", "");
		for (int i = 0; i < methods.length; i++) {
			Method m = methods[i];
			if (m.getParameterTypes().length != getParametersLength) {
				continue;
			}
			String name = m.getName().toLowerCase();
			if (!(("get" + name1).equals(name) || ("is" + name1).equals(name))) {
				continue;
			}

			GLOBL_CACHED.put(cachedkey, i);
			return m;
		}
		return null;
	}

	/**
	 * Set the specified class parameter base on LIKE the method name
	 * 
	 * @param methodName the method name
	 * @param val        the set value
	 * @return the result, when null is successful else is the error message
	 */
	public String setValue(String methodName, String val) {
		String n1 = methodName.toLowerCase().trim();
		Object[] b = new Object[1];
		b[0] = val;
		String ret = null;
		for (int i = 0; i < this._Methods.length; i++) {
			String name = this._Methods[i].getName().toLowerCase();

			if (this._Methods[i].getParameterTypes().length != 1 || name.indexOf(n1) < 0) {
				continue;
			}
			ret = setValue(this._Methods[i], b);
			break;
		}
		return ret;
	}

	/**
	 * Set the specified class parameter base on the method name
	 * 
	 * @param methodName the method name
	 * @param val        the set value
	 * @return the result, when null is successful else is the error message
	 */
	public String setValueAccurate(String methodName, String val) {

		String n1 = methodName.toLowerCase().trim();

		Object[] b = new Object[1];
		b[0] = val;
		String ret = null;
		for (int i = 0; i < this._Methods.length; i++) {
			String name = this._Methods[i].getName().toLowerCase();

			if (this._Methods[i].getParameterTypes().length != 1 || !name.equals(n1)) {
				continue;
			}
			ret = setValue(this._Methods[i], b);
			break;
		}
		return ret;
	}

	/**
	 * Set the specified class parameter base on the method
	 * 
	 * @param method the method
	 * @param para   the set value
	 * @return the result, when null is successful else is the error message
	 */
	private String setValue(Method method, Object[] para) {
		return setValue(this._object, method, para);
	}

	/**
	 * @return the specified class
	 */
	public Object getObject() {
		return _object;
	}

	/**
	 * Set the source class as specified class
	 * 
	 * @param sourceClass the source class
	 */
	public void setObject(Object sourceClass) {
		this._object = sourceClass;
		this._Methods = _object.getClass().getDeclaredMethods();
		Class<?> sup = _object.getClass().getSuperclass();
		if (sup != null) {
			this._MethodsSuper = sup.getDeclaredMethods();
			Class<?> sup2 = sup.getSuperclass();
			if (sup2 != null) {
				this._MethodsSuper2 = sup2.getDeclaredMethods();
				Class<?> sup3 = sup2.getSuperclass();
				if (sup3 != null) {
					this._MethodsSuper3 = sup3.getDeclaredMethods();
				}
			}
		}
		this._GetterMethods = new ArrayList<Method>();

		for (int i = 0; i < this._Methods.length; i++) {
			Method m = this._Methods[i];
			String name = m.getName();
			if (m.getParameterTypes().length > 0) {
				continue;
			}
			if (name.equals("hashCode") || name.equals("toString")) {
				continue;
			}
			this._GetterMethods.add(this._Methods[i]);
		}

	}

	/**
	 * 通过反射的方法调用类
	 * 
	 * @param className             类名
	 * @param constructorParameters 构造参数
	 * @param exeMethodName         执行方法名
	 * @param methodValues          执行参数
	 * @return 是否成功
	 */
	public boolean loadClass(String className, Object[] constructorParameters, String exeMethodName,
			Object[] methodValues) {

		Object instance = this.loadClass(className, constructorParameters);
		if (instance == null)
			return false;

		Object ovl = this.invoke(instance, exeMethodName, methodValues);
		this._Instance = instance;
		if (ovl != null) {
			this.setObject(ovl);
		}
		return true;
	}

	/**
	 * 通过反射的方法调用类
	 * 
	 * @param className             类名
	 * @param constructorParameters 构造参数
	 * @param exeMethodName         执行方法名
	 * @param methodValues          执行参数
	 * @param rv                    页面 RequestValue 对象
	 * @return 是否成功
	 */
	public boolean loadClass(String className, Object[] constructorParameters, String exeMethodName,
			Object[] methodValues, RequestValue rv) {
		this._Rv = rv;
		Object instance = this.loadClass(className, constructorParameters);

		if (instance == null)
			return false;
		setRequestValue(instance, rv);

		Object ovl = this.invoke(instance, exeMethodName, methodValues);
		this._Instance = instance;
		if (ovl != null) {
			this.setObject(ovl);
		}
		return true;
	}

	/**
	 * 通过反射加载类
	 * 
	 * @param className             类名
	 * @param constructorParameters 构造参数值
	 * @return 类实例
	 */
	public Object loadClass(String className, Object[] constructorParameters) {
		Class<?> c = null;
		try {
			c = Class.forName(className);
		} catch (Exception e) {
			LOGGER.error("loadClass " + className + ": " + e.getMessage());
			return null;
		}
		Constructor<?> constructor = this.getConstructor(c, constructorParameters);
		if (constructor == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("Not found constructor " + className);
			if (constructorParameters != null) {
				sb.append(", ");
				for (int i = 0; i < constructorParameters.length; i++) {
					sb.append(constructorParameters[i]);
					sb.append(" ");
				}
			}
			LOGGER.error(sb.toString());
			return null;
		}

		Object instance;
		try {
			Class<?>[] types = constructor.getParameterTypes();
			Object[] newParas = new Object[types.length];
			for (int i = 0; i < types.length; i++) {
				Class<?> t = types[i];
				newParas[i] = convert(t, constructorParameters[i]);
				if (newParas[i] != null && newParas[i].toString().equals("[com.gdxsoft.easyweb.script.RequestValue]")) {
					newParas[i] = this._Rv;
				}
			}
			instance = constructor.newInstance(newParas);
			this._LastErrMsg = null;
			return instance;
		} catch (IllegalArgumentException e) {
			LOGGER.error(e.getMessage());
			this._LastErrMsg = e.getCause().getMessage();
		} catch (InstantiationException e) {
			LOGGER.error(e.getMessage());
			this._LastErrMsg = e.getCause().getMessage();
		} catch (IllegalAccessException e) {
			// 转到静态方法
			return c;
		} catch (InvocationTargetException e) {
			LOGGER.error(e.getMessage());
			this._LastErrMsg = e.getCause().getMessage();
		}
		return null;
	}

	/**
	 * Invoke the static method
	 * 
	 * @param className    the class name
	 * @param methodName   the static method name
	 * @param methodValues the method parameters
	 * @return the result
	 * @throws Exception error
	 */
	public Object invokeStatic(String className, String methodName, Object[] methodValues) throws Exception {
		Class<?> c = null;
		try {
			c = Class.forName(className);
		} catch (Exception e) {
			LOGGER.error("", e);
			throw e;
		}
		Method method = this.getMethodByMatchParams(c.getDeclaredMethods(), methodName, methodValues);
		if (method == null) {
			String error = "The method " + methodName + " not found in " + className;
			LOGGER.error(error);
			throw new Exception(error);
		}
		return this.invoke(c, method, methodValues);
	}

	/**
	 * 通过类实例执行方法
	 * 
	 * @param instance      类实例
	 * @param exeMethodName 执行方法名
	 * @param methodValues  执行参数
	 * @return 执行后对象
	 */
	public Object invoke(Object instance, String exeMethodName, Object[] methodValues) {
		Method method = this.getMethodByMatchParams(instance.getClass().getDeclaredMethods(), exeMethodName,
				methodValues);

		return this.invoke(instance, method, methodValues);
	}

	/**
	 * 通过类实例执行方法
	 * 
	 * @param instance      类实例
	 * @param methods       方法数组
	 * @param exeMethodName 执行方法名
	 * @param methodValues  执行参数
	 * @return 执行后对象
	 */
	public Object invoke(Object instance, Method[] methods, String exeMethodName, Object[] methodValues) {
		Method method = this.getMethodByMatchParams(methods, exeMethodName, methodValues);

		return this.invoke(instance, method, methodValues);
	}

	private Object invoke(Object instance, Method method, Object[] methodValues) {
		if (method == null)
			return null;

		try {
			Class<?>[] types = method.getParameterTypes();
			Object[] newVals = new Object[types.length];
			for (int i = 0; i < types.length; i++) {
				newVals[i] = convert(types[i], methodValues[i]);
			}
			Object o = method.invoke(instance, newVals);
			this._LastErrMsg = null;
			return o;
		} catch (Exception e) {
			String msg = e.getCause().getMessage();
			this._LastErrMsg = msg;
			LOGGER.error(msg);
			return null;
		}
	}

	private Constructor<?> getConstructor(Class<?> c, Object[] constructorParameters) {
		int paraLength = 0;
		if (constructorParameters != null)
			paraLength = constructorParameters.length;
		Constructor<?>[] cc = c.getDeclaredConstructors();
		for (int i = 0; i < cc.length; i++) {
			Class<?>[] types = cc[i].getParameterTypes();
			if (types.length != paraLength) {
				continue;
			}
			if (paraLength == 0)
				return cc[i];
			for (int m = 0; m < paraLength; m++) {
				if (!constructorParameters[m].getClass().equals(types[m].getClass())) {
					break;
				}
			}
			return cc[i];
		}
		return null;
	}

	/**
	 * Set the specified class through the JDBC result and the specified field list
	 * 
	 * @param rs        the JDBC result
	 * @param fieldList the specified field list
	 * @return the specified class
	 * @throws Exception The exception
	 */
	public Object setDaoValue(ResultSet rs, String[] fieldList) throws Exception {
		// Object o1 = (Object) _object.getClass().newInstance();
		Object o1 = _object;
		String[] objFieldList = fieldList;
		List<KeyValuePair<String, Object>> notFinds = new ArrayList<KeyValuePair<String, Object>>();

		for (int i = 0; i < objFieldList.length; i++) {
			String filedName = objFieldList[i];
			int fieldIndex = -1;
			try {
				fieldIndex = rs.findColumn(filedName);
			} catch (Exception err) {
				// System.out.println( err.getMessage());
				// 字段不存在
				continue;
			}
			Object val = rs.getObject(fieldIndex);
			boolean isok = false;
			try {
				isok = this.invokeMethod(filedName, val, null);
			} catch (Exception err) {
				LOGGER.warn(filedName, val, err.getMessage());
			}
			if (!isok) { // 未找到赋值方法
				KeyValuePair<String, Object> kv = new KeyValuePair<String, Object>();
				kv.setPair(filedName, val);
				notFinds.add(kv);
			}
		}
		this._NotFinds = notFinds;
		return o1;
	}

	/**
	 * /** Set the specified class through the JSON object
	 * 
	 * @param json the JSON object
	 * @return the specified class
	 * @throws Exception The exception
	 */
	public Object setDaoValue(JSONObject json) throws Exception {
		return this.setDaoValue(json, null);
	}

	/**
	 * Set the specified class through the JSON object
	 * 
	 * @param obj              the JSON object
	 * @param handleJsonBinary The interface of the JSON binary
	 * @return the specified class
	 * @throws Exception The exception
	 */
	public Object setDaoValue(JSONObject obj, IHandleJsonBinary handleJsonBinary) throws Exception {
		// Object o1 = (Object) _object.getClass().newInstance();
		Object o1 = _object;
		List<KeyValuePair<String, Object>> notFinds = new ArrayList<KeyValuePair<String, Object>>();
		Iterator<?> it = obj.keys();
		while (it.hasNext()) {
			String key = it.next().toString();
			Object val = obj.get(key);
			try {
				boolean isok = this.invokeMethod(key, val, handleJsonBinary);
				if (!isok) { // 未找到赋值方法
					KeyValuePair<String, Object> kv = new KeyValuePair<String, Object>();
					kv.setPair(key, val);
					notFinds.add(kv);
				}
			} catch (Exception err) {
				LOGGER.warn(key + "," + val + ": " + err.getMessage());
			}

		}
		this._NotFinds = notFinds;
		return o1;
	}

	/**
	 * Set the specified class through the DTRow
	 * 
	 * @param row the DTRow
	 * @return the specified class
	 * @throws Exception The exception
	 */
	public Object setDaoValue(DTRow row) throws Exception {
		Object o1 = _object;
		List<KeyValuePair<String, Object>> notFinds = new ArrayList<KeyValuePair<String, Object>>();
		for (int i = 0; i < row.getTable().getColumns().getCount(); i++) {
			String name = row.getTable().getColumns().getColumn(i).getName();
			Object val = row.getCell(i).getValue();
			try {
				boolean isok = this.invokeMethod(name, val, null);
				if (!isok) { // 未找到赋值方法
					KeyValuePair<String, Object> kv = new KeyValuePair<String, Object>();
					kv.setPair(name, val);
					notFinds.add(kv);
				}
			} catch (Exception err) {
				LOGGER.warn(name + "," + val + ": " + err.getMessage());
			}
		}
		this._NotFinds = notFinds;
		return o1;
	}

	/**
	 * Set the specified class through the RequestValue
	 * 
	 * @param rv the RequestValue
	 * @return the specified class
	 * @throws Exception The exception
	 */
	public Object setDaoValue(RequestValue rv) throws Exception {
		// 未赋值的对象
		List<KeyValuePair<String, Object>> notFinds = new ArrayList<KeyValuePair<String, Object>>();

		// 放置已经执行过的参数名称
		HashMap<String, Boolean> vals = new HashMap<String, Boolean>();

		// 调用参数系的顺序
		PageValueTag[] tags = PageValueTag.getOrder();
		for (int i = 0; i < tags.length; i++) {
			MTable mt = rv.getPageValues().getTagValues(tags[i]);
			for (int m = 0; m < mt.getCount(); m++) {
				String key = mt.getKey(m).toString().toUpperCase();
				if (vals.containsKey(key)) {
					continue;
				}
				vals.put(key, true);

				PageValue pv = (PageValue) mt.get(key);
				Object val = pv.getValue();
				try {
					boolean isok = this.invokeMethod(key, val, null);
					if (!isok) { // 未找到赋值方法
						KeyValuePair<String, Object> kv = new KeyValuePair<String, Object>();
						kv.setPair(key, val);
						notFinds.add(kv);
					}
				} catch (Exception err) {
					LOGGER.warn(key, val, err.getMessage());
				}
			}
		}
		this._NotFinds = notFinds;
		return _object;
	}

	/**
	 * Find and invoke the method from the specified class
	 * 
	 * @param name             the method
	 * @param val              the invoke value
	 * @param handleJsonBinary The interface of the JSON binary
	 * @return true: successful/false: not found
	 * @throws Exception The exception
	 */
	private boolean invokeMethod(String name, Object val, IHandleJsonBinary handleJsonBinary) throws Exception {
		String methodName = "set" + name.replace("_", "");

		Object getMethodValue = val == null ? new ObjectNull() : val;

		Method mm = this.getMethodByMatchParams(this._Methods, methodName, getMethodValue);
		if (mm == null) {
			if (this._MethodsSuper != null) {
				// 获取super的模式
				mm = this.getMethodByMatchParams(this._MethodsSuper, methodName, getMethodValue);
			}
			if (mm == null) {// 方法不存在
				return false;
			}
		}

		String paraType = mm.getParameterTypes()[0].getName().toUpperCase();
		Object[] v = new Object[1];
		if (val == null) {
			v[0] = null; // 不处理
		} else if (paraType.equals("INT") || paraType.equals("JAVA.LANG.INTEGER")) {
			String str = val.toString();
			if (str.trim().length() == 0) {
				v[0] = null;
			} else {
				int intval = 0;
				try {
					intval = UConvert.ToInt32(str);
					if (paraType.equals("JAVA.LANG.INTEGER")) {
						v[0] = Integer.valueOf(intval);
					} else {
						v[0] = intval; // int
					}
				} catch (Exception err) {
					LOGGER.error(err.getLocalizedMessage() + ":" + err);
					return false;
				}

			}
		} else if (paraType.equals("LONG") || paraType.equals("JAVA.LANG.LONG")) {
			String str = val.toString();
			if (str.trim().length() == 0) {
				v[0] = null;
			} else {
				long longValue = 0;
				try {
					longValue = Long.parseLong(str);
					if (paraType.equals("JAVA.LANG.LONG")) {
						v[0] = Long.valueOf(longValue);
					} else {
						v[0] = longValue;
					}
				} catch (Exception err) {
					LOGGER.error(err.getLocalizedMessage() + ":" + err);
					return false;
				}
			}
		} else if (paraType.equals("JAVA.LANG.STRING")) {
			v[0] = val.toString();
		} else if (paraType.equals("JAVA.UTIL.DATE")) {
			String t = val.toString();
			if (t.trim().length() == 0) {
				v[0] = null;
			} else {
				try {// mysql 000-00-00 转换出错
					if (t.length() == 10) {
						t += " 00:00:00.000";
					}
					if (t.indexOf("/") > 0) {// 美国日期格式
						v[0] = Utils.getDate(t, "MM/dd/yyyy HH:mm:ss.SSS");
					} else {
						v[0] = Utils.getDate(t, "yyyy-MM-dd HH:mm:ss.SSS");
					}
				} catch (Exception err) {
					LOGGER.error(err.getLocalizedMessage(), err);
					return false;
				}
			}
		} else if (paraType.equals("DOUBLE") || paraType.equals("JAVA.LANG.DOUBLE")) {
			String str = val.toString();
			if (str.trim().length() == 0) {
				v[0] = null;
			} else {
				try {
					double dv = UConvert.ToDouble(str);
					if (paraType.equals("JAVA.LANG.DOUBLE")) {
						v[0] = Double.valueOf(dv);
					} else {
						v[0] = dv;
					}
				} catch (Exception err) {
					LOGGER.error(err.getLocalizedMessage(), err);
					return false;
				}
			}

		} else if (paraType.equals("[B")) {// 二进制
			if (handleJsonBinary != null) {
				v[0] = handleJsonBinary.getBinary(name, val);
			} else {
				v[0] = val; // 不处理
			}
		} else if ("COM.GDXSOFT.EASYWEB.UTILS.TYPES.UINT32".equals(paraType)) {
			v[0] = UInt32.valueOf(val.toString());
		} else if ("COM.GDXSOFT.EASYWEB.UTILS.TYPES.UINT16".equals(paraType)) {
			v[0] = UInt16.valueOf(val.toString());
		} else if ("COM.GDXSOFT.EASYWEB.UTILS.TYPES.UINT64".equals(paraType)) {
			v[0] = UInt64.valueOf(val.toString());
		} else {
			v[0] = val;
		}
		mm.invoke(this._object, v);
		return true;
	}

	/**
	 * Create the cached key of method and params
	 * 
	 * @param methodName
	 * @param params
	 * @return
	 */
	private String createMethodCachedKey(String methodName, Object... params) {
		String cachedKey = this._object.getClass().getName() + "," + methodName.toLowerCase();
		if (params != null) {
			for (int i = 0; i < params.length; i++) {
				cachedKey += "," + params[i].getClass().getName();
			}
		}
		return cachedKey;
	}

	private Method getMethodByMatchParams(Method[] methods, String methodName, Object... params) {
		if (params == null) {
			params = new Object[0];
		}
		String key = null;
		if (this._object != null) {
			key = this.createMethodCachedKey(methodName, params);
			if (GLOBL_CACHED.containsKey(key)) {
				return methods[GLOBL_CACHED.get(key)];
			}
		}
		List<Integer> found = new ArrayList<>();
		String m1 = methodName.trim();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (method.getName().equalsIgnoreCase(m1) && method.getParameterTypes().length == params.length) {
				found.add(i);
			}
		}
		if (found.size() == 0) {// not found
			LOGGER.debug("Not Found the method '{}'.", methodName);
			return null;
		}
		if (found.size() == 1) {
			if (key != null) {
				GLOBL_CACHED.put(key, found.get(0));
			}
			LOGGER.debug("Found the method '{}', key={}", methodName, key);
			return methods[found.get(0)];
		}

		LOGGER.debug("Found the method '{}'  {} times.", methodName, found.size());

		int maxMaches = -1;
		int methodMatched = -1;
		// Overloading methods
		for (int i = 0; i < found.size(); i++) {
			int methodIndex = found.get(i);
			Method method = methods[methodIndex];
			Class<?>[] paraTypes = method.getParameterTypes();
			int matcheLenth = 0;
			for (int m = 0; m < paraTypes.length; m++) {
				if (paraTypes[m].equals(params[m].getClass())) {
					matcheLenth++;
				}
			}
			if (matcheLenth == paraTypes.length) {
				if (key != null) {
					GLOBL_CACHED.put(key, methodIndex);
				}
				return method;
			}
			if (matcheLenth > maxMaches) {// the maximum matches
				methodMatched = methodIndex;
			}
		}
		if (methodMatched != -1) {
			if (key != null) {
				GLOBL_CACHED.put(key, methodMatched);
			}
			return methods[methodMatched];
		} else {
			return null;
		}
	}

	/**
	 * Returns all get methods
	 * 
	 * @return the _getterMethods
	 */
	public ArrayList<Method> getGetterMethods() {
		return _GetterMethods;
	}

	/**
	 * Get the loadClass instnace
	 * 
	 * @return the _Instance
	 */
	public Object getInstance() {
		return _Instance;
	}

	/**
	 * Get the last exception message
	 * 
	 * @return the _LastErrMsg
	 */
	public String getLastErrMsg() {
		return _LastErrMsg;
	}

	/**
	 * Get the not found methods of the setDaoValue
	 * 
	 * @return the _NotFinds
	 */
	public List<KeyValuePair<String, Object>> getNotFinds() {
		return _NotFinds;
	}
}

class ObjectNull {
	public ObjectNull() {

	}

	public String toString() {
		return "ObjectNull";
	}
}
