package com.xinqihd.sns.gameserver.util;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestJavacUtil {

	String source = System.getProperty("user.dir")+
			File.separator+"src"+File.separator+"test"+
			File.separator+"resources"+File.separator+"Hello.java";
	String destDir = System.getProperty("java.io.tmpdir") + "bin";
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCompile() {
		File destFile = new File(destDir);
		File classFile = new File(destDir, "com/xinqihd/test/Hello.class");
		destFile.mkdirs();
		assertTrue(JavacUtil.compile(source, destDir));
		assertTrue(classFile.exists());
	}

}
