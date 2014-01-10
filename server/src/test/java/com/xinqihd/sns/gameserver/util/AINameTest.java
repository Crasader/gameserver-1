package com.xinqihd.sns.gameserver.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Filter and import AI names
 * @author wangqi
 *
 */
public class AINameTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * ~/Documents/技术书籍/passwords
	 */
	@Test
	public void test() throws Exception {
		String filePath = "/Users/wangqi/disk/temp/name2.txt";
		FileInputStream fr = new FileInputStream(filePath);
		BufferedReader br = new BufferedReader(new InputStreamReader(fr, "gbk"));
		String line = br.readLine();
		HashSet<String> names = new HashSet<String>();
		while ( line != null ) {
			boolean valid = true;
			for ( char ch : line.toCharArray() ) {
				if ( !Character.isLetterOrDigit(ch) ) {
					valid = false;
					break;
				}
			}
			if ( valid ) {
				names.add(line.trim());
			}
			line = br.readLine();
		}
		StringBuilder buf = new StringBuilder(50000);
		buf.append("private static final String[] NAMES = {\n");
		int tab = 0;
		int count = 0;
		for ( String name : names ) {
			if ( tab++ % 8 == 0 ) {
				buf.append("\n");
			}
			buf.append("\"").append(name).append("\", ");
			if ( count++ > 5000 ) {
				break;
			}
		}
		buf.append("};\n");
		System.out.println(buf);
	}

}
