package com.xinqihd.sns.gameserver.bootstrap;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URL;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.server.GameServer;

public class TestReloadClassLoader {
	
	String sourceCode1 = 
			"package com.xinqihd.test;        	\n"+
					"                               \n"+
					"public class ReloadTest {          \n"+
					"	public static String execute() {     \n"+
					"		try {                     	\n"+
					"			Thread.sleep(1000);   		\n"+
					"		} catch (Exception e) {   	\n"+
					"		}                         	\n"+
					"		return \"hello\";           \n"+
					"	}                             \n"+
					"}                              \n";
	
	String sourceCode2 = 
			"package com.xinqihd.test;        	\n"+
					"                               \n"+
					"public class ReloadTest {          \n"+
					"	public static String execute() {     \n"+
					"		try {                     	\n"+
					"			Thread.sleep(1000);   		\n"+
					"		} catch (Exception e) {   	\n"+
					"		}                         	\n"+
					"		return \"world\";           \n"+
					"	}                             \n"+
					"}                              \n";

	
	String source = System.getProperty("user.dir")+
			File.separator+"src"+File.separator+"test"+
			File.separator+"resources"+File.separator+"Hello.java";
	String className = "com.xinqihd.test.Hello";
	
	String reloadSourceDir = System.getProperty("user.dir")+
			File.separator+"src"+File.separator+"test"+
			File.separator+"resources"+File.separator+"classes";
	String reloadClassName = "com.xinqihd.test.Reload";

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test the load class function.
	 */
	@Test
	public void testLoadClass() throws Exception {
		File sourceFile = new File(reloadSourceDir);
		ReloadClassLoader cloader = ReloadClassLoader.newClassloader(new URL[]{sourceFile.toURL()});
		Class clazz = cloader.loadClass(reloadClassName);
		Class objectClazz = cloader.loadClass("java.lang.Math");
		assertTrue(clazz != null);
		assertTrue(objectClazz != null);
		assertEquals(Math.class, objectClazz);
	}
	
	/**
	 * Test the load class performance.
	 */
	@Test
	public void testLoadClassPerformance() throws Exception {
		File sourceFile = new File(reloadSourceDir);
		ReloadClassLoader cloader = ReloadClassLoader.newClassloader(new URL[]{sourceFile.toURL()});
		for ( int i=0; i<1000; i++ ) {
			Class clazz = cloader.loadClass(reloadClassName);
			Class objectClazz = cloader.loadClass("java.lang.Math");
		}
	}

	@Test
	public void testReload() throws Exception {
		//src/test/resources/classes
		String reloadClassName = "com.xinqihd.test.ReloadTest";
		File sourceDir = new File(reloadSourceDir);
		File sourceCodeFile = new File(reloadSourceDir, reloadClassName.replace('.', '/')+".java");
		
		writeToFile(sourceCode1, sourceCodeFile);
		ReloadClassLoader cloader = ReloadClassLoader.newClassloader(new URL[]{sourceDir.toURL()});
		
		//Load class version 1.
//		Class helloClazz = Class.forName(reloadClassName, false, cloader);
		Class helloClazz = cloader.loadClass(reloadClassName);
		assertTrue(helloClazz != null);
		Method method = helloClazz.getMethod("execute");
		String hello = (String)method.invoke(helloClazz, null);
		assertEquals("hello", hello);
		
		//Replace the source code.
		writeToFile(sourceCode2, sourceCodeFile);
		//Reload the source code
		cloader.reload();
				
//		Class worldClazz = Class.forName(reloadClassName, false, cloader);
		cloader = ReloadClassLoader.newClassloader(new URL[]{sourceDir.toURL()});
		Class worldClazz = cloader.loadClass(reloadClassName);
		method = worldClazz.getMethod("execute");
		String world = (String)method.invoke(worldClazz, null);
		assertEquals("world", world);

	}
	
	@Test
	public void testEmptyPath() throws Exception {
		ReloadClassLoader cloader = ReloadClassLoader.newClassloader(
				new URL[]{
						new URL("file:///non-exist.jar"),
						new URL("file:///non-exist-dir/")
						}
		);
	}
	
	@Test
	public void testLoadParentClass() throws Exception {
		File sourceDir = new File(reloadSourceDir);
		ReloadClassLoader cloader = ReloadClassLoader.newClassloader(new URL[]{sourceDir.toURL()});
		Class clazz = cloader.loadClass("com.google.protobuf.GeneratedMessage");
		assertTrue(clazz != null);
	}
	
	@Test
	public void testClassLoaderLeak() throws Exception {
		int max = 10000;
		File sourceFile = new File(reloadSourceDir);
		URL[] urls = new URL[]{sourceFile.toURL()};
		ReloadProtocolCodecFilter filter = new ReloadProtocolCodecFilter(
				GameServer.PROTOCOL_CODEC, GameServer.PROTOCOL_HANDLER);
		for ( int i=0; i<max; i++ ) {
			filter.reload();
		}
		System.out.println("done");
	}
	
	@Test
	public void testClassLoaderLeak2() throws Exception {
		int max = 10000;
		File sourceFile = new File(reloadSourceDir);
		URL[] urls = new URL[]{sourceFile.toURL()};
//		ReloadProtocolCodecFilter filter = ReloadProtocolCodecFilter.getInstance(
//				GameServer.PROTOCOL_CODEC, GameServer.PROTOCOL_HANDLER, urls);
		SocketConnector connector = new NioSocketConnector();
//		connector.getFilterChain().addLast("codec", filter);
		connector.setHandler(new ClientHandler());
    //Send 1000 connections.
    try {
			for ( int i=0; i<Integer.MAX_VALUE; i++ ) {
				ConnectFuture future = connector.connect(new InetSocketAddress("localhost", 3443));
				future.awaitUninterruptibly();
				IoSession session = future.getSession();
				IoBuffer buffer = IoBuffer.allocate(8);
				buffer.putShort((short)8);
				buffer.putShort((short)0);
				buffer.putInt(i);
				WriteFuture wfuture = session.write(buffer);
				wfuture.awaitUninterruptibly();
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	//TimeClientHandler
	class ClientHandler extends IoHandlerAdapter {
    @Override
    public void messageReceived(IoSession session, Object message)
    			throws Exception{
    	
    		IoBuffer buf = (IoBuffer)message;
        buf.skip(4);
        int index = buf.getInt();
        System.out.println("index = " + index);
        session.close(false);
    }

	}
	
	private void writeToFile(String content, File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(content.getBytes());
		fos.close();
	}

}
