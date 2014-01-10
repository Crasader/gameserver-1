package com.xinqihd.sns.gameserver.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

public class TestIOUtil extends TestCase {

	String extractDir = System.getProperty("java.io.tmpdir");
	
	String comDirName = System.getProperty("user.dir")+
			File.separator+"src"+File.separator+"test"+
			File.separator+"resources"+File.separator+"com";
	String txtFileName = System.getProperty("user.dir")+
			File.separator+"src"+File.separator+"test"+
			File.separator+"resources"+File.separator+"hello.txt";
	String jarFileName = System.getProperty("user.dir")+
			File.separator+"src"+File.separator+"test"+
			File.separator+"resources"+File.separator+"hello.jar";
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test extract jar file.
	 */
	public void testExtractJARStringString() {
		try {
			File extractHelloFile = new File(extractDir, "hello.txt");
			File extractMETAFile = new File(extractDir, "META-INF");
			IOUtil.extractJAR(jarFileName, extractDir);
			assert(extractHelloFile.exists());
			String content = new String( IOUtil.readFileBytes(extractHelloFile.getAbsolutePath() ));
			assertEquals("hello world", content);
			assert(!extractMETAFile.exists());
		} catch (IOException e) {
			fail("Fail to extract JAR");
		}
	}

	public void testExtractJARURLString() {
		File file = new File(jarFileName);
		try {
			URL url = file.toURL();
			IOUtil.extractJAR(url, extractDir);
			File extractHelloFile = new File(extractDir, "hello.txt");
			File helloJarFile = new File(extractDir, "hello.jar");
			File extractMETAFile = new File(extractDir, "META-INF");
			assert(helloJarFile.exists());
			String content = new String( IOUtil.readFileBytes(extractHelloFile.getAbsolutePath() ));
			assertEquals("hello world", content);
			assert(!extractMETAFile.exists());
		} catch (IOException e) {
			fail(file.getAbsolutePath());
			e.printStackTrace();
		}
	}
	
	public void testReadFileBytes() {
		try {
			String content = new String(IOUtil.readFileBytes(txtFileName));
			assertEquals("hello world", content);
		} catch (IOException e) {
			fail(txtFileName);
			e.printStackTrace();
		}
	}
	
	public void testCopyFile() {
		try {
			File sourceFile = new File(txtFileName);
			File destDir = new File(extractDir);
			File destFile = new File(destDir, sourceFile.getName());
			IOUtil.copyFile(sourceFile, destDir);
			assert(destFile.exists());
			String content = new String( IOUtil.readFileBytes(destFile.getAbsolutePath() ));
			assertEquals("hello world", content);
		} catch (IOException e) {
			fail("Fail to copy file.");
		}
	}
	
	public void testCopyDir() {
		try {
			File sourceDir = new File(comDirName);
			File destDir = new File(extractDir);
			File destFile = new File(destDir, "xinqihd/test/Reload.java");
			IOUtil.copyDir(sourceDir, destDir);
			assert(destFile.exists());
		} catch (IOException e) {
			e.printStackTrace();
			fail("Fail to copy dir.");
		}
	}

}
