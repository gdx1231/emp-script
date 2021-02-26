package com.gdxsoft.easyweb.utils;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;


public class UConvert {
	public static byte[] FromBase64String(String s) throws IOException {
		return Base64.decodeBase64(s);
	}

	public static String ToBase64String(byte[] inArray) {
		return Base64.encodeBase64String(inArray);
	}

	public static String ToBase64String(byte[] inArray, int offset, int length) {
		byte[] buf1 = new byte[length];
		System.arraycopy(inArray, offset, buf1, 0, length);
		return ToBase64String(buf1);
	}

	public static boolean ToBoolean(boolean value) {
		return value;
	}

	public static boolean ToBoolean(byte value) {
		return (value != 0);
	}

	public static boolean ToBoolean(char value) {
		return Boolean.getBoolean(String.valueOf(value));
	}

	public static boolean ToBoolean(double value) {
		return (value != 0.0);
	}

	public static boolean ToBoolean(short value) {
		return (value != 0);
	}

	public static boolean ToBoolean(int value) {
		return (value != 0);
	}

	public static boolean ToBoolean(long value) {
		return (value != 0L);
	}

	public static boolean ToBoolean(float value) {
		return (value != 0f);
	}

	public static boolean ToBoolean(String value) {
		if (value == null) {
			return false;
		}
		return Boolean.getBoolean(value);
	}

	public static byte ToByte(boolean value) {
		if (!value) {
			return 0;
		}
		return 1;
	}

	public static byte ToByte(byte value) {
		return value;
	}

	public static byte ToByte(char value) {
		return (byte) value;
	}

	public static byte ToByte(double value) throws Exception {
		return ToByte(ToInt32(value));
	}

	public static byte ToByte(short value) {
		return (byte) value;
	}

	public static byte ToByte(int value) {

		return (byte) value;
	}

	public static byte ToByte(long value) {

		return (byte) value;
	}

	public static byte ToByte(Object value) {

		return 0;
	}

	public static byte ToByte(float value) throws Exception {
		return ToByte((double) value);
	}

	public static byte ToByte(String value) {
		if (value == null) {
			return 0;
		}
		return Byte.parseByte(value);
	}

	public static byte ToByte(String value, int fromBase) {

		int num = Integer.parseInt(value, fromBase);
		return (byte) num;
	}

	public static char ToChar(boolean value) {
		return String.valueOf(value).charAt(0);
	}

	public static char ToChar(byte value) {
		return (char) value;
	}

	public static char ToChar(char value) {
		return value;
	}

	public static char ToChar(double value) {
		return String.valueOf(value).charAt(0);
	}

	public static char ToChar(short value) {
		return String.valueOf(value).charAt(0);
	}

	public static char ToChar(int value) {
		return String.valueOf(value).charAt(0);
	}

	public static char ToChar(long value) {
		return String.valueOf(value).charAt(0);
	}

	public static char ToChar(Object value) {
		return String.valueOf(value).charAt(0);
	}

	//    
	//    
	// public static Date ToDate(boolean value)
	// {
	// return ((IConvertible) value).ToDate(null);
	// }
	//
	// public static Date ToDate(byte value)
	// {
	// return ((IConvertible) value).ToDate(null);
	// }
	//
	// public static Date ToDate(char value)
	// {
	// return ((IConvertible) value).ToDate(null);
	// }
	//
	// public static Date ToDate(Date value)
	// {
	// return value;
	// }
	//
	// public static Date ToDate(decimal value)
	// {
	// return ((IConvertible) value).ToDate(null);
	// }
	//
	// public static Date ToDate(double value)
	// {
	// return ((IConvertible) value).ToDate(null);
	// }
	//
	// public static Date ToDate(short value)
	// {
	// return ((IConvertible) value).ToDate(null);
	// }
	//
	// public static Date ToDate(int value)
	// {
	// return ((IConvertible) value).ToDate(null);
	// }
	//
	// public static Date ToDate(long value)
	// {
	// return ((IConvertible) value).ToDate(null);
	// }
	//
	// public static Date ToDate(Object value)
	// {
	// if (value != null)
	// {
	// return ((IConvertible) value).ToDate(null);
	// }
	// return Date.MinValue;
	// }
	//
	//    
	// public static Date ToDate(sbyte value)
	// {
	// return ((IConvertible) value).ToDate(null);
	// }
	//
	// public static Date ToDate(float value)
	// {
	// return ((IConvertible) value).ToDate(null);
	// }
	//
	// public static Date ToDate(String value)
	// {
	// if (value == null)
	// {
	// return new Date(0L);
	// }
	// return Date.Parse(value, CultureInfo.CurrentCulture);
	// }
	//
	//    
	// public static Date ToDate(ushort value)
	// {
	// return ((IConvertible) value).ToDate(null);
	// }
	//
	//    
	// public static Date ToDate(uint value)
	// {
	// return ((IConvertible) value).ToDate(null);
	// }
	//
	//    
	// public static Date ToDate(ulong value)
	// {
	// return ((IConvertible) value).ToDate(null);
	// }
	//
	// public static Date ToDate(Object value, IFormatProvider provider)
	// {
	// if (value != null)
	// {
	// return ((IConvertible) value).ToDate(provider);
	// }
	// return Date.MinValue;
	// }
	//
	// public static Date ToDate(String value, IFormatProvider provider)
	// {
	// if (value == null)
	// {
	// return new Date(0L);
	// }
	// return Date.Parse(value, provider);
	// }

	public static double ToDouble(boolean value) {
		return (value ? ((double) 1) : ((double) 0));
	}

	public static double ToDouble(byte value) {
		return (double) value;
	}

	public static double ToDouble(char value) {
		return (double) value;
	}

	public static double ToDouble(double value) {
		return value;
	}

	public static double ToDouble(short value) {
		return (double) value;
	}

	public static double ToDouble(int value) {
		return (double) value;
	}

	public static double ToDouble(long value) {
		return (double) value;
	}

	public static double ToDouble(Object value) {
		if (value != null) {
			return ToDouble(value.toString());
		}
		return 0.0;
	}

	public static double ToDouble(String value) {
		if (value == null) {
			return 0.0;
		}
		return Double.parseDouble(value);
	}

	public static short ToInt16(boolean value) {
		if (!value) {
			return 0;
		}
		return 1;
	}

	public static short ToInt16(byte value) {
		return value;
	}

	public static short ToInt16(char value) {

		return (short) value;
	}

	public static short ToInt16(double value) throws Exception {
		return ToInt16(ToInt32(value));
	}

	public static short ToInt16(short value) {
		return value;
	}

	public static short ToInt16(int value) throws Exception {
		if ((value < -32768) || (value > 0x7fff)) {
			throw new Exception("Overflow_Int16");
		}
		return (short) value;
	}

	public static short ToInt16(long value) throws Exception {
		if ((value < -32768L) || (value > 0x7fffL)) {
			throw new Exception("Overflow_Int16");
		}
		return (short) value;
	}

	public static short ToInt16(float value) throws Exception {
		return ToInt16((double) value);
	}

	public static short ToInt16(String value) {
		if (value == null) {
			return 0;
		}
		return Short.parseShort(value);
	}

	public static short ToInt16(String value, int fromBase) {

		return Short.parseShort(value, fromBase);
	}

	public static int ToInt32(boolean value) {
		if (!value) {
			return 0;
		}
		return 1;
	}

	public static int ToInt32(byte value) {
		return value;
	}

	public static int ToInt32(char value) {
		return value;
	}

	public static int ToInt32(double value) throws Exception {
		if (value >= 2147483647.5 || value < -2147483648.5) {
			throw new Exception("Overflow_int32");
		}
		if (value >= 0.0) {
			int num = (int) value;
			double num2 = value - num;
			if ((num2 > 0.5) || ((num2 == 0.5) && ((num & 1) != 0))) {
				num++;
			}
			return num;
		} else {
			int num3 = (int) value;
			double num4 = value - num3;
			if ((num4 < -0.5) || ((num4 == -0.5) && ((num3 & 1) != 0))) {
				num3--;
			}
		}
		return 0;
	}

	public static int ToInt32(short value) {
		return value;
	}

	public static int ToInt32(int value) {
		return value;
	}

	public static int ToInt32(long value) {

		return (int) value;
	}

	public static int ToInt32(float value) throws Exception {
		return ToInt32((double) value);
	}

	public static int ToInt32(String value) {

		return ToInt32(value, 10);
	}

	public static int ToInt32(String value, int fromBase) {
		if (value == null) {
			return 0;
		}
		String val1=value.split("\\.")[0];
		return Integer.parseInt(val1, fromBase);
	}

	public static long ToInt64(boolean value) {
		return (value ? ((long) 1) : ((long) 0));
	}

	public static long ToInt64(byte value) {
		return (long) value;
	}

	public static long ToInt64(char value) {
		return (long) value;
	}

	public static long ToInt64(double value) {
		return (long) Math.round(value);
	}

	public static long ToInt64(short value) {
		return (long) value;
	}

	public static long ToInt64(int value) {
		return (long) value;
	}

	public static long ToInt64(long value) {
		return value;
	}

	public static long ToInt64(float value) {
		return ToInt64((double) value);
	}

	public static long ToInt64(String value) {
		if (value == null) {
			return 0L;
		}
		return Long.parseLong(value);
	}

	public static long ToInt64(String value, int fromBase) {
		return Long.parseLong(value, fromBase);
	}

	public static float ToSingle(boolean value) {
		return (value ? ((float) 1) : ((float) 0));
	}

	public static float ToSingle(byte value) {
		return (float) value;
	}

	public static float ToSingle(char value) {
		return Float.parseFloat(String.valueOf(value));
	}

	public static float ToSingle(double value) {
		return (float) value;
	}

	public static float ToSingle(short value) {
		return (float) value;
	}

	public static float ToSingle(int value) {
		return (float) value;
	}

	public static float ToSingle(long value) {
		return (float) value;
	}

	public static float ToSingle(float value) {
		return value;
	}

	public static float ToSingle(String value) {
		if (value == null) {
			return 0f;
		}
		return Float.parseFloat(value);
	}

	public static String ToString(boolean value) {
		return value + "";
	}

	public static String ToString(byte value) {
		return value + "";
	}

	public static String ToString(char value) {
		return String.valueOf(value);
	}

	public static String ToString(double value) {
		String s1=String.valueOf(value);
		String[] s2=s1.split("\\.");
		if(s2.length>0){
			int p=ToInt32(s2[1]);
			if(p==0){
				return s2[0];
			}else{
				return s1;
			}
		}else{
			return s1;
		}
	}

	public static String ToString(short value) {
		return String.valueOf(value);
	}

	public static String ToString(int value) {
		return String.valueOf(value);
	}

	public static String ToString(long value) {
		return String.valueOf(value);
	}

	public static String ToString(Object value) {
		if (value == null) {
			return null;
		}
		return value.toString();
	}

	public static String ToString(float value) {
		return String.valueOf(value);
	}

	public static String ToString(String value) {
		return value;
	}
}
