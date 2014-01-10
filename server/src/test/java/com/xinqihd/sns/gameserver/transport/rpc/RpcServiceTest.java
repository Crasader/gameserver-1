package com.xinqihd.sns.gameserver.transport.rpc;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.Semaphore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.ServiceException;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.bootstrap.ReloadClassLoader;
import com.xinqihd.sns.gameserver.proto.RpcTest.RpcSleep;
import com.xinqihd.sns.gameserver.proto.RpcTest.RpcTestReq;
import com.xinqihd.sns.gameserver.proto.RpcTest.RpcTestResp;
import com.xinqihd.sns.gameserver.server.RpcServer;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class RpcServiceTest {
	
	private Random r = new Random();
	private String remoteHost = "localhost";
	private int remotePort = 10077;

	@Before
	public void setUp() throws Exception {
		
		File classFile1 = new File("classes");
		if ( classFile1.exists() ) {
			Files.deleteDirectoryContents(classFile1);
		}
		ReloadClassLoader.newClassloader(new URL[]{});
//		if ( ReloadClassLoader.currentClassLoader() == null ) {
//			File classFile1 = new File("target/test-classes");
//			ReloadClassLoader.newClassloader(new URL[]{});
//		}
//		else {
//			File classFile2 = new File("target/test-classes");
//			ReloadClassLoader.currentClassLoader().addClasspathURL(classFile2.toURL());
//		}
//		Thread.currentThread().setContextClassLoader(ReloadClassLoader.currentClassLoader());
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testNonBlocking() throws Exception {
		GameContext.getTestInstance().registerRpcService(new RpcTestService.RpcNormalTestService());
		
		final Semaphore sema = new Semaphore(0);
		RpcServer server = RpcServer.getInstance();
		server.startServer(remoteHost, remotePort);

		RpcTestReq.Builder reqBuilder = RpcTestReq.newBuilder();
		final int sleep = r.nextInt(1000);
		reqBuilder.setSleep(sleep);

		MinaRpcChannel rpcClient = new MinaRpcChannel(remoteHost, remotePort);
		RpcSleep.Stub stub = RpcSleep.Stub.newStub(rpcClient);

		final boolean[] result = new boolean[]{true};

		stub.test(null, reqBuilder.build(), new RpcCallback<RpcTestResp>() {

			@Override
			public void run(RpcTestResp parameter) {
				try {
					assertEquals(sleep, parameter.getSleep());
				} catch (Throwable e) {
					e.printStackTrace();
					result[0] = false;
				}
				sema.release();
			}
		});
		
		sema.acquire();
		server.stopServer();
		
		if ( !result[0] ) {
			fail("Test failed");
		}
	}
	
	@Test
	public void testNonBlockingConcurrent() throws Exception {		
		GameContext.getTestInstance().registerRpcService(new RpcTestService.RpcNormalTestService());
		final Semaphore sema = new Semaphore(0);
		RpcServer server = RpcServer.getInstance();
		server.startServer(remoteHost, remotePort);
		
		RpcTestReq.Builder reqBuilder = RpcTestReq.newBuilder();
		
		MinaRpcChannel rpcClient = new MinaRpcChannel(remoteHost, remotePort);
		RpcSleep.Stub stub = RpcSleep.Stub.newStub(rpcClient);
		
		final boolean[] result = new boolean[]{true};
			
		int max = 10;
		Callback lastCallback = null;
		for ( int i=0; i<max; i++ ) {
			final int sleep = r.nextInt(1000);
			reqBuilder.setSleep(sleep);
			lastCallback = new Callback(sleep, result, sema);
			stub.test(null, reqBuilder.build(), lastCallback);
		}
		
		sema.acquire();
		server.stopServer();
		
		if ( !result[0] ) {
			fail("Test failed");
		}
	}
	
	private static class Callback implements RpcCallback<RpcTestResp> {
		int sleep = 0;
		boolean[] result = null;
		Semaphore sema;
		
		public Callback(int sleep, boolean[] result, Semaphore sema) {
			this.sleep = sleep;
			this.result = result;
			this.sema = sema;
		}
		
		@Override
		public void run(RpcTestResp parameter) {
			try {
				assertEquals(sleep, parameter.getSleep());
				System.out.println("check sleep:"+sleep+" with " + parameter.getSleep());
			} catch (Throwable e) {
				e.printStackTrace();
				result[0] = false;
			}
			sema.release();
		}
	}
	
	@Test
	public void testBlocking() throws Exception {
		GameContext.getTestInstance().registerRpcService(new RpcTestService.RpcBlockingTestService());
		RpcServer server = RpcServer.getInstance();
		server.startServer(remoteHost, remotePort);
		
		RpcTestReq.Builder reqBuilder = RpcTestReq.newBuilder();
		final int sleep = r.nextInt(1000);
		reqBuilder.setSleep(sleep);
		
		MinaRpcChannel rpcChannel = new MinaRpcChannel(remoteHost, remotePort);
		RpcSleep.BlockingInterface stub = RpcSleep.Stub.newBlockingStub(rpcChannel);
		
		RpcTestResp resp = stub.test(null, reqBuilder.build());
		
		assertEquals(sleep, resp.getSleep());
		server.stopServer();
	}
	
	@Test
	public void testBlockingConcurrent() throws Exception {
		GameContext.getTestInstance().registerRpcService(new RpcTestService.RpcBlockingTestService());
		RpcServer server = RpcServer.getInstance();
		server.startServer(remoteHost, remotePort);
		
		int max = 5;
		MinaRpcChannel rpcChannel = new MinaRpcChannel(remoteHost, remotePort);
		RpcSleep.BlockingInterface stub = RpcSleep.Stub.newBlockingStub(rpcChannel);
		
		for ( int i = 0; i<max; i++ ) {
			RpcTestReq.Builder reqBuilder = RpcTestReq.newBuilder();
			final int sleep = r.nextInt(1000);
			reqBuilder.setSleep(sleep);		
			RpcTestResp resp = stub.test(null, reqBuilder.build());
			assertEquals(sleep, resp.getSleep());
		}
		
		server.stopServer();
	}
	
	@Test
	public void testBlockingConcurrentPool() throws Exception {
		GameContext.getTestInstance().registerRpcService(new RpcTestService.RpcBlockingTestService());
		RpcServer server = RpcServer.getInstance();
		server.startServer(remoteHost, remotePort);
		
		int max = 5;
		MinaRpcPoolChannel rpcChannel = new MinaRpcPoolChannel(remoteHost, remotePort, 3);
		RpcSleep.BlockingInterface stub = RpcSleep.Stub.newBlockingStub(rpcChannel);
		
		for ( int i = 0; i<max; i++ ) {
			RpcTestReq.Builder reqBuilder = RpcTestReq.newBuilder();
			final int sleep = r.nextInt(1000);
			reqBuilder.setSleep(sleep);		
			RpcTestResp resp = stub.test(null, reqBuilder.build());
			assertEquals(sleep, resp.getSleep());
		}
		
		server.stopServer();
	}
	
	@Test
	public void testSpeed() throws Exception {
		GameContext.getTestInstance().registerRpcService(new RpcTestService.RpcBlockingTestService());
		RpcServer server = RpcServer.getInstance();
		server.startServer(remoteHost, remotePort);
		
		final int max = 10;
		final int threadCount = 3;
		
		MinaRpcChannel rpcChannel = new MinaRpcChannel(remoteHost, remotePort);
		final RpcSleep.BlockingInterface stub = RpcSleep.Stub.newBlockingStub(rpcChannel);
		MinaRpcPoolChannel rpcPoolChannel = new MinaRpcPoolChannel(remoteHost, remotePort);
		final RpcSleep.BlockingInterface stubPool = RpcSleep.Stub.newBlockingStub(rpcPoolChannel);
		
		
		TestUtil.doPerformMultiThread(new Runnable(){
			public void run() {
				RpcTestReq.Builder reqBuilder = RpcTestReq.newBuilder();
				final int sleep = r.nextInt(1000);
				reqBuilder.setSleep(sleep);		
				RpcTestResp resp = null;
				try {
					resp = stub.test(null, reqBuilder.build());
					int actual = resp.getSleep();
					assertEquals(sleep, actual);
				} catch (ServiceException e) {
					e.printStackTrace();
				}
			}
		}, "RPC test", max, threadCount);

		TestUtil.doPerformMultiThread(new Runnable(){
			public void run() {
				RpcTestReq.Builder reqBuilder = RpcTestReq.newBuilder();
				final int sleep = r.nextInt(1000);
				reqBuilder.setSleep(sleep);		
				RpcTestResp resp = null;
				try {
					resp = stubPool.test(null, reqBuilder.build());
					int actual = resp.getSleep();
					assertEquals(sleep, actual);
				} catch (ServiceException e) {
					e.printStackTrace();
				}
			}
		}, "RPC Pool test", max, threadCount);
				
		server.stopServer();
	}
	
	@Test
	public void testEmptyCall() throws Exception {
		GameContext.getTestInstance().registerRpcService(new RpcTestService.RpcBlockingTestService());
		RpcServer server = RpcServer.getInstance();
		server.startServer(remoteHost, remotePort);
		
		MinaRpcChannel rpcChannel = new MinaRpcChannel(remoteHost, remotePort);
		RpcSleep.BlockingInterface stub = RpcSleep.Stub.newBlockingStub(rpcChannel);
		
		RpcTestResp resp = stub.test(null, null);
		
		assertNull(resp);
		server.stopServer();
	}

}
