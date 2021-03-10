package com.gdxsoft.easyweb.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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

public class UObjectValue {
	private static Logger LOGGER = LoggerFactory.getLogger(UObjectValue.class);

	public static Object convert(Class<?> t, Object val) {
		if (val == null)
			return null;
		if (t.isArray()) {
			if (val.getClass().getName().equals("java.lang.String")) {
				String[] v1 = val.toString().split(",");
				for (int i = 0; i < v1.length; i++) {
					v1[i] = v1[i].trim();

				}
				return v1;
			}
			return val;
		}
		String name = t.getName();
		if (name.equals("com.gdxsoft.easyweb.script.RequestValue")) {
			return "[com.gdxsoft.easyweb.script.RequestValue]";
		}
		if (name.equals("int")) {
			return Integer.parseInt(val.toString().split("\\.")[0]);
		} else if (name.equals("boolean")) {
			boolean v = false;
			// if (val == null)
			// return v;
			String v1 = val.toString().toUpperCase().trim();
			if (v1.equals("0") || v1.equals("FALSE") || v1.equals("否") || v1.equals("NO") || v1.equals("N")) {
				return v;
			} else {
				v = true;
				return v;
			}
		} else if (name.equals("double")) {
			return Double.parseDouble(val.toString());
		} else if (name.equals("long")) {
			return Long.parseLong(val.toString());
		} else if (name.equals("float")) {
			return Float.parseFloat(val.toString());
		} else if (name.equals("byte")) {
			return Byte.parseByte(val.toString());
		} else if (name.equals("java.util.Date")) {
			return Utils.getDate(val.toString());
		}
		try {
			Object o = t.cast(val);
			return o;
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	private Object _object;
	private Method[] _Methods;
	private HashMap<String, Method> _ht;
	private ArrayList<Method> _GetterMethods;
	private Object _Instance;
	private String _LastErrMsg;
	private RequestValue _Rv;
	private Method[] _MethodsSuper;

	// 未找到的方法名称
	private List<KeyValuePair<String, Object>> _NotFinds;

	public UObjectValue() {

	}

	/**
	 * 设置所有属性
	 * 
	 * @param ele XmlNode
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
	 * 根据对象属性填充xml node
	 * 
	 * @param ele xmlnode
	 * @param obj 对象
	 */
	public static void writeXmlNodeAtts(Element ele, Object obj) {
		UObjectValue o = new UObjectValue();
		o.setObject(obj);

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
				ele.setAttribute(name, o.getValue(m));
			}
		}
	}

	/**
	 * 填充对象为node textcontent，tagName为对象属性名
	 * 
	 * @param eleParent
	 * @param obj
	 */
	public static void writeXmlNodeTexts(Element eleParent, Object obj) {
		UObjectValue o = new UObjectValue();
		o.setObject(obj);

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
				Node n = UXml.retNode(eleParent, name);
				Element ele;
				if (n == null) {
					ele = eleParent.getOwnerDocument().createElement(name);
				} else {
					ele = (Element) n;
				}
				ele.setTextContent(v);
				eleParent.appendChild(ele);
			}
		}
	}

	/**
	 * 从xml属性中恢复对象属性
	 * 
	 * @param ele xml node
	 * @param obj 对象
	 */
	public static void fromXml(Node node, Object obj) {
		fromXml((Element) node, obj);
	}

	/**
	 * 从xml的childNodes中恢复属性，获取的是node的textcontent
	 * 
	 * @param eleParent
	 * @param obj
	 */
	public static void fromXmlNodes(Element eleParent, Object obj) {
		UObjectValue o = new UObjectValue();
		o.setObject(obj);
		for (int i = 0; i < eleParent.getChildNodes().getLength(); i++) {
			Node n = eleParent.getChildNodes().item(i);
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
	 * 从xml属性中恢复对象属性
	 * 
	 * @param ele xml node
	 * @param obj 对象
	 */
	public static void fromXml(Element ele, Object obj) {
		UObjectValue o = new UObjectValue();
		o.setObject(obj);
		o.setAllValue(ele);
	}

	/**
	 * 获取所有对象值，String,boolean,int类型
	 * 
	 * @return
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
	 * 根据方法获取值
	 * 
	 * @param method 方法
	 * @return
	 */
	public String getValue(Method method) {
		String ret;
		Object[] b = null;
		String s = method.getReturnType().getName();

//		if (!(s.equals("long") || s.equals("int") || s.equals("boolean") || s.equals("byte") || s.indexOf("String") >= 0
//				|| s.equals("java.util.Date"))) {
//			return null;
//		}

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
	 * 根据方法名称获取值
	 * 
	 * @param methodName 方法名
	 * @return
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
	 * 获取对象的属性值
	 * 
	 * @param methodName
	 * @return
	 */
	public Object getProperty(String methodName) {
		String name1 = methodName.toLowerCase().trim().replace("_", "");
		for (int i = 0; i < this._Methods.length; i++) {
			Method m = this._Methods[i];
			if (m.getParameterTypes().length != 0) {
				continue;
			}
			String name = m.getName().toLowerCase();
			if (!(("get" + name1).equals(name) || ("is" + name1).equals(name))) {
				continue;
			}

			return this.getValue(m);
		}
		if (this._MethodsSuper != null) { // 获取super的模式
			for (int i = 0; i < this._MethodsSuper.length; i++) {
				Method m = this._MethodsSuper[i];
				if (m.getParameterTypes().length != 1) {
					continue;
				}
				String name = m.getName().toLowerCase();
				if (!(("get" + name1).equals(name) || ("is" + name1).equals(name))) {
					continue;
				}
				return this.getValue(m);
			}
		}
		return null;
	}

	/**
	 * 设置对象值，按照名称部分符合
	 * 
	 * @param methodName 方法名称
	 * @param val        值
	 * @return 错误信息
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
	 * 设置对象值,按照确定名称
	 * 
	 * @param methodName
	 * @param val
	 * @return
	 */
	public String setValueAccurate(String methodName, String val) {

		String n1 = methodName.toLowerCase().trim();
		// if (n1.toLowerCase().indexOf("js") > 0) {
		// int a = 1;
		// a++;
		// }
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
	 * 设置对象值
	 * 
	 * @param method 方法
	 * @param para   数据参数
	 * @return 错误信息
	 */
	private String setValue(Method method, Object[] para) {
		return this.setValue(this._object, method, para);
	}

	/**
	 * 执行方法
	 * 
	 * @param instance 类实例
	 * @param method   方法
	 * @param para     参数
	 * @return 错误信息，null为成功
	 */
	public String setValue(Object instance, Method method, Object[] para) {
		Object[] newVals = new Object[method.getParameterTypes().length];
		for (int i = 0; i < method.getParameterTypes().length; i++) {
			newVals[i] = convert(method.getParameterTypes()[i], para[i]);
			if (newVals[i] != null && newVals[i].toString().equals("[com.gdxsoft.easyweb.script.RequestValue]")) {
				newVals[i] = para[i]; // 恢复rv
			}
		}
		try {
			method.invoke(instance, newVals);
			return null;
		} catch (IllegalArgumentException e) {
			LOGGER.error(e.getMessage());
			return e.getMessage();
		} catch (IllegalAccessException e) {
			LOGGER.error(e.getMessage());
			return e.getMessage();
		} catch (InvocationTargetException e) {
			LOGGER.error(e.getMessage());
			return e.getMessage();
		}
	}

	/**
	 * 设置对象的 RequestValue
	 * 
	 * @param rv RequestValue对象
	 * @return 成功或失败
	 */
	public boolean setRequestValue(Object instance, RequestValue rv) {
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
				String rst = this.setValue(instance, m, val);
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
	 * @return the _object
	 */
	public Object getObject() {
		return _object;
	}

	/**
	 * 设置Class对象
	 * 
	 * @param _object 设置Class对象
	 */
	public void setObject(Object _object) {
		this._object = _object;
		this._Methods = _object.getClass().getDeclaredMethods();
		Class<?> sup = _object.getClass().getSuperclass();
		if (sup != null) {
			this._MethodsSuper = sup.getDeclaredMethods();
		}
		_ht = new HashMap<String, Method>();
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
		this.setRequestValue(instance, rv);

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
			LOGGER.error("", e);
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
			LOGGER.error("", e);
			this._LastErrMsg = e.getCause().getMessage();
		} catch (InstantiationException e) {
			LOGGER.error("", e);
			this._LastErrMsg = e.getCause().getMessage();
		} catch (IllegalAccessException e) {
			// 转到静态方法
			return c;
		} catch (InvocationTargetException e) {
			LOGGER.error("", e);
			this._LastErrMsg = e.getCause().getMessage();
		}
		return null;
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
		Method method = this.getMethod(instance.getClass().getDeclaredMethods(), exeMethodName,
				methodValues == null ? 0 : methodValues.length);

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
		Method method = this.getMethod(methods, exeMethodName, methodValues == null ? 0 : methodValues.length);

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
	 * 通过 jdbc结果集赋值对象
	 * 
	 * @param rs
	 * @param fieldList
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws SQLException
	 * @throws InvocationTargetException
	 */
	public Object setDaoValue(ResultSet rs, String[] fieldList) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, SecurityException, NoSuchFieldException, SQLException, InvocationTargetException {
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
			boolean isok = this.invokeMethod(filedName, val, null);

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
	 * 通过JSON 赋值
	 * 
	 * @param obj json对象
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public Object setDaoValue(JSONObject obj)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return this.setDaoValue(obj, null);
	}

	/**
	 * 通过JSON 赋值
	 * 
	 * @param obj               json对象
	 * @param IHandleJsonBinary 获取二进制的接口
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public Object setDaoValue(JSONObject obj, IHandleJsonBinary handleJsonBinary)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// Object o1 = (Object) _object.getClass().newInstance();
		Object o1 = _object;
		List<KeyValuePair<String, Object>> notFinds = new ArrayList<KeyValuePair<String, Object>>();
		Iterator<?> it = obj.keys();
		while (it.hasNext()) {
			String key = it.next().toString();
			Object val = obj.get(key);
			boolean isok = this.invokeMethod(key, val, handleJsonBinary);
			if (!isok) { // 未找到赋值方法
				KeyValuePair<String, Object> kv = new KeyValuePair<String, Object>();
				kv.setPair(key, val);
				notFinds.add(kv);
			}
		}
		this._NotFinds = notFinds;
		return o1;
	}

	/**
	 * 通过DTRow 赋值
	 * 
	 * @param row
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public Object setDaoValue(DTRow row)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object o1 = _object;
		List<KeyValuePair<String, Object>> notFinds = new ArrayList<KeyValuePair<String, Object>>();
		for (int i = 0; i < row.getTable().getColumns().getCount(); i++) {
			String name = row.getTable().getColumns().getColumn(i).getName();
			Object val = row.getCell(i).getValue();
			boolean isok = this.invokeMethod(name, val, null);
			if (!isok) { // 未找到赋值方法
				KeyValuePair<String, Object> kv = new KeyValuePair<String, Object>();
				kv.setPair(name, val);
				notFinds.add(kv);
			}
		}
		this._NotFinds = notFinds;
		return o1;
	}

	/**
	 * 用 rv 赋值
	 * 
	 * @param rv
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public Object setDaoValue(RequestValue rv)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
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
				boolean isok = this.invokeMethod(key, val, null);

				if (!isok) { // 未找到赋值方法
					KeyValuePair<String, Object> kv = new KeyValuePair<String, Object>();
					kv.setPair(key, val);
					notFinds.add(kv);
				}
			}
		}
		this._NotFinds = notFinds;
		return _object;
	}

	/**
	 * 查找并执行方法
	 * 
	 * @param name
	 * @param val
	 * @param handleJsonBinary
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	private boolean invokeMethod(String name, Object val, IHandleJsonBinary handleJsonBinary)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String methodName = "set" + name.replace("_", "");
		Method mm = this.getMethod(this._Methods, methodName, 1);
		if (mm == null) {
			if (this._MethodsSuper != null) {
				// 获取super的模式
				mm = this.getMethod(this._MethodsSuper, methodName, 1);
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
				try {
					v[0] = UConvert.ToInt32(str);
				} catch (Exception err) {
					LOGGER.error(err.getLocalizedMessage(), err);
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
						v[0] = Utils.getDate(t, "MM/dd/yyyy hh:mm:ss.SSS");
					} else {
						v[0] = Utils.getDate(t, "yyyy-MM-dd hh:mm:ss.SSS");
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
					v[0] = UConvert.ToDouble(str);
				} catch (Exception err) {
					LOGGER.error(err.getLocalizedMessage(), err);
					return false;
				}
			}

		} else if (paraType.equals("[B")) {// 二进制
			if (handleJsonBinary != null) {
				v[0] = handleJsonBinary.getBinary(name, val);
			} else {
				v[0] = null; // 不处理
			}
		}
		mm.invoke(this._object, v);
		return true;
	}

	private Method getMethod(Method[] methods, String methodName, int parameterLength) {
		String m1 = methodName.trim().toUpperCase();
		String key = m1 + "|" + parameterLength;
		if (this._ht != null && this._ht.containsKey(key)) {
			return this._ht.get(key);
		}
		for (int i = 0; i < methods.length; i++) {
			String mm = methods[i].getName().toUpperCase();
			if (mm.equals(m1) && methods[i].getParameterTypes().length == parameterLength) {
				if (this._ht != null) {
					_ht.put(key, methods[i]);
				}
				return methods[i];
			}
		}
		return null;
	}

	/**
	 * @return the _getterMethods
	 */
	public ArrayList<Method> getGetterMethods() {
		return _GetterMethods;
	}

	/**
	 * 获取实例
	 * 
	 * @return the _Instance
	 */
	public Object getInstance() {
		return _Instance;
	}

	/**
	 * 最后执行错误
	 * 
	 * @return the _LastErrMsg
	 */
	public String getLastErrMsg() {
		return _LastErrMsg;
	}

	/**
	 * 未找到的方法名称
	 * 
	 * @return the _NotFinds
	 */
	public List<KeyValuePair<String, Object>> getNotFinds() {
		return _NotFinds;
	}
}
