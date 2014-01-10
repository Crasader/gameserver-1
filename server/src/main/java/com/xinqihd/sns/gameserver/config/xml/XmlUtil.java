package com.xinqihd.sns.gameserver.config.xml;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * This class provide some facility for xml processing.
 * @author wangqi
 *
 */
public class XmlUtil {
	
	private static final Log log = LogFactory.getLog(XmlUtil.class);

	/**
	 * Format the given string as xml content.
	 * @param xml
	 * @return
	 */
	public static String formatXml(String xml) {
		try {
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xml));
			Document doc = domBuilder.parse(is);
			OutputFormat format = new OutputFormat(doc);
			format.setLineWidth(80);
			format.setIndent(2);
			format.setIndenting(true);
			StringWriter out = new StringWriter();
			XMLSerializer xmls = new XMLSerializer(out, format);
			xmls.serialize(doc);
			return out.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return xml;
	}
		
}
