package com.xinqihd.sns.gameserver.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.UUID;

/**
 * Format Json object to string for debug.
 * 
 * @author wangqi
 *
 */
public class JsonUtil {

	/**
	 * Serializes an object into it's JSON form
	 * 
	 * @param o
	 *          object to serialize
	 * @return String containing JSON form of the object
	 */
	public static String serialize(Object o) {
		StringBuilder buf = new StringBuilder();
		serialize(o, buf);
		return buf.toString();
	}

	@SuppressWarnings("unchecked")
	public static void serialize(Object o, StringBuilder buf) {

		if (o == null) {
			buf.append(" null ");
			return;
		}

		if (o instanceof Number) {
			buf.append(o);
			return;
		}

		if (o instanceof String) {
			string(buf, o.toString());
			return;
		}

		if (o instanceof Iterable) {

			boolean first = true;
			buf.append("[ ");

			for (Object n : (Iterable) o) {
				if (first)
					first = false;
				else
					buf.append(" , ");

				serialize(n, buf);
			}

			buf.append("]");
			return;
		}

		if (o instanceof Map) {

			boolean first = true;
			buf.append("{ ");

			Map m = (Map) o;

			for (Map.Entry entry : (Set<Map.Entry>) m.entrySet()) {
				if (first)
					first = false;
				else
					buf.append(" , ");

				string(buf, entry.getKey().toString());
				buf.append(" : ");
				serialize(entry.getValue(), buf);
			}

			buf.append("}");
			return;
		}

		if (o instanceof Date) {
			Date d = (Date) o;
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			format.setCalendar(new GregorianCalendar(new SimpleTimeZone(0, "GMT")));
			buf.append("$date").append(" : ").append(format.format(d));
			return;
		}

		if (o instanceof Boolean) {
			buf.append(o);
			return;
		}

		if (o instanceof byte[]) {
			buf.append("<Binary Data>");
			return;
		}

		if (o.getClass().isArray()) {
			buf.append("[ ");

			for (int i = 0; i < Array.getLength(o); i++) {
				if (i > 0)
					buf.append(" , ");
				serialize(Array.get(o, i), buf);
			}

			buf.append("]");
			return;
		}

		if (o instanceof UUID) {
			UUID uuid = (UUID) o;
			buf.append("uuid").append(" : ").append(uuid.toString());
			return;
		}
		
		//General object
		Field[] fields = o.getClass().getFields();
		for ( Field field : fields ) {
			int modifier = field.getModifiers();
			if ( !Modifier.isTransient(modifier) && !Modifier.isStatic(modifier) ) {
				field.setAccessible(true);
				buf.append(field.getName()).append(" : ");
				try {
					string(buf, field.get(o).toString());
				} catch (Exception e) {
//					e.printStackTrace();
				}
			}
		}

		return;
	}

	private static void string(StringBuilder a, String s) {
		a.append("\"");
		for (int i = 0; i < s.length(); ++i) {
			char c = s.charAt(i);
			if (c == '\\')
				a.append("\\\\");
			else if (c == '"')
				a.append("\\\"");
			else if (c == '\n')
				a.append("\\n");
			else if (c == '\r')
				a.append("\\r");
			else if (c == '\t')
				a.append("\\t");
			else if (c == '\b')
				a.append("\\b");
			else if (c < 32)
				continue;
			else
				a.append(c);
		}
		a.append("\"");
	}
}
