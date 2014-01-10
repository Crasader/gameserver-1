package com.xinqihd.sns.gameserver.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IOUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCompressString() throws Exception {
		String content = "您好, Hello!";
		byte[] bytes = IOUtil.compressString("entry", content);
		FileOutputStream fos = new FileOutputStream(new File("compress.zip"));
		fos.write(bytes);
		fos.close();
		String result = IOUtil.uncompressString(bytes);
		assertEquals(content, result);
	}

	@Test
	public void testCompressStringZlib() throws Exception {
		String content = "您好, Hello!";
		byte[] bytes = IOUtil.compressStringZlib(content);
		FileOutputStream fos = new FileOutputStream(new File("compress.gz"));
		fos.write(bytes);
		fos.close();
		String result = IOUtil.uncompressStringZlib(bytes);
		assertEquals(content, result);
	}
}
