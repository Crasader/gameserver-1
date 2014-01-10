package com.xinqihd.sns.gameserver.admin.util;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

/**
 * 用于在支持系统剪贴板拷贝时，将Excel拷贝的字符串转换为相应的Java对象格式， 比如Double, Integer,
 * 尤其是Json转换为DBObject格式
 * 
 * @author wangqi
 * 
 */
public class ObjectUtil {

	private static final Logger logger = LoggerFactory
			.getLogger(ObjectUtil.class);

	/**
	 * Support Format String Double Float Integer Short Byte DBObject
	 * 
	 * Note: array is not supported
	 * 
	 * @param value
	 * @param targetClass
	 * @return
	 */
	public static final Object parseStringToObject(String value, String escapeValue, Class<?> targetClass) {
		logger.debug("targetClass:{}, value:{}", targetClass, value);
		try {
			if (String.class.isAssignableFrom(targetClass)) {
				return value;
			} else if (targetClass == Boolean.class) {
				return Boolean.parseBoolean(value);
			} else if (Double.class.isAssignableFrom(targetClass)) {
				return Double.parseDouble(value);
			} else if (Float.class.isAssignableFrom(targetClass)) {
				return Float.parseFloat(value);
			} else if (Integer.class.isAssignableFrom(targetClass)) {
				return Integer.parseInt(value);
			} else if (Short.class.isAssignableFrom(targetClass)) {
				return Short.parseShort(value);
			} else if (Byte.class.isAssignableFrom(targetClass)) {
				return Byte.parseByte(value);
			} else if (DBObject.class.isAssignableFrom(targetClass)) {
				return JSON.parse(escapeValue);
			} else {
				return value;
			}
		} catch (NumberFormatException e) {
			logger.warn("Failed to convert '{}' to target. Exception", e);
			return value;
		}
	}

	public static boolean isPrimitiveObject(Object value) {
		Class targetClass = value.getClass();
		if (targetClass == String.class) {
			return true;
		} else if (targetClass == Boolean.class) {
			return true;
		} else if (targetClass == Integer.class) {
			return true;
		} else if (targetClass == Long.class) {
			return true;
		} else if (targetClass == Float.class) {
			return true;
		} else if (targetClass == Double.class) {
			return true;
		} else if (targetClass == Short.class) {
			return true;
		} else if (targetClass == Byte.class) {
			return true;
		}
		return false;
	}

	public static Object convertValue(Object value, Class<?> targetClass)
			throws NumberFormatException {
		// short-circuit String columns
		if (DBObject.class.isAssignableFrom(targetClass)) {
			return JSON.parse(value.toString());
		}
		if (targetClass == Object.class || targetClass == String.class)
			return value;
		if (targetClass == Boolean.class) {
			if (value instanceof Boolean)
				return value;
			return value == null ? Boolean.FALSE : Boolean.valueOf(value.toString());
		} else if (Number.class.isAssignableFrom(targetClass)) {
			String strValue = value.toString();
			if (targetClass == Integer.class) {
				value = Integer.valueOf(strValue);
			} else if (targetClass == Long.class) {
				value = Long.valueOf(strValue);
			} else if (targetClass == Float.class) {
				value = Float.valueOf(strValue);
			} else if (targetClass == Double.class) {
				value = Double.valueOf(strValue);
			} else if (targetClass == BigInteger.class) {
				return new BigInteger(strValue);
			} else if (targetClass == BigDecimal.class) {
				return new BigDecimal(strValue);
			} else if (targetClass == Short.class) {
				value = Short.valueOf(strValue);
			} else if (targetClass == Byte.class) {
				value = Byte.valueOf(strValue);
			}
		}
		return value;
	}
}
