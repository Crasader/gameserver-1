package com.xinqihd.sns.gameserver.transport.rpc.service;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import org.apache.mina.core.session.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;
import com.google.protobuf.ByteString;
import com.google.protobuf.RpcCallback;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.bootstrap.ReloadClassLoader;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.Rpc.BoolResponse;
import com.xinqihd.sns.gameserver.proto.RpcUserRefresh;
import com.xinqihd.sns.gameserver.proto.RpcUserRefresh.RefreshReq;
import com.xinqihd.sns.gameserver.server.RpcServer;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.rpc.MinaRpcChannel;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class UserRefreshServiceTest {
	
	private String remoteHost = "localhost";
	private int remotePort = 10077;

	public UserRefreshServiceTest() {
		File classFile1 = new File("classes");
		if ( classFile1.exists() ) {
			try {
				Files.deleteDirectoryContents(classFile1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ReloadClassLoader.newClassloader(new URL[]{});
	}
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testUserRefreshBasicMode() throws Exception {
		SessionKey userSessionKey = SessionKey.createSessionKeyFromRandomString();
		RefreshReq.Builder reqBuilder = RefreshReq.newBuilder();
		reqBuilder.setUserSessionKey(ByteString.copyFrom(userSessionKey.getRawKey()));
		reqBuilder.setRefreshmode(1);
		
		GameContext.getTestInstance().registerRpcService(UserRefreshService.getInstance());
		
		final Semaphore sema = new Semaphore(0);
		RpcServer server = RpcServer.getInstance();
		server.startServer(remoteHost, remotePort);

		MinaRpcChannel rpcClient = new MinaRpcChannel(remoteHost, remotePort);
		RpcUserRefresh.UserRefresh.Stub stub = RpcUserRefresh.UserRefresh.newStub(rpcClient);

		final boolean[] result = new boolean[]{true};

		stub.refresh(null, reqBuilder.build(), new RpcCallback<BoolResponse>() {

			@Override
			public void run(BoolResponse parameter) {
				try {
					//Cannot find the user.
					assertEquals(false, parameter.getResult());
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
	public void testUserRefreshBasicModeWithNativeMethod() throws Exception {
		String rpcServerId = remoteHost+":"+remotePort;
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_RPC_SERVERID, rpcServerId);
		
		SessionKey userSessionKey = SessionKey.createSessionKeyFromRandomString();
		
		GameContext.getTestInstance().registerRpcService(UserRefreshService.getInstance());
		
		final Semaphore sema = new Semaphore(0);
		RpcServer server = RpcServer.getInstance();
		server.startServer(remoteHost, remotePort);
		
		String username = "test";
		User user = new User();
		user.set_id(new UserId(username));
		ArrayList list = new ArrayList();
		IoSession session = TestUtil.createIoSession(list);
		GameContext.getInstance().getSessionManager().registerSession(session, user);


		final boolean[] result = new boolean[]{true};
		int refreshMode = 1;
		UserRefreshService.getInstance().remoteRefresh(refreshMode, user.getSessionKey());
		
		sema.acquire();
		server.stopServer();
		
		if ( !result[0] ) {
			fail("Test failed");
		}
	}
	
	/**
	 * You should start the real game server for this test to work
	 * @throws Exception
	 */
	@Test
	public void testUserRefreshBasicModeForRealServer() throws Exception {
		String remoteHost = "192.168.0.7";
		int    remotePort =  3445;
		String rpcServerId = remoteHost+":"+remotePort;
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_RPC_SERVERID, rpcServerId);
		
		SessionKey userSessionKey = SessionKey.createSessionKeyFromRandomString();
		
		GameContext.getInstance().registerRpcService(UserRefreshService.getInstance());
		
		final Semaphore sema = new Semaphore(0);
		
		String username = "test";
		User user = new User();
		user.set_id(new UserId(username));
		user.setSessionKey(userSessionKey);
		
		ArrayList list = new ArrayList();
		IoSession session = TestUtil.createIoSession(list);
		GameContext.getInstance().getSessionManager().registerSession(session, user, userSessionKey);


		final boolean[] result = new boolean[]{true};
		int refreshMode = 1;
		UserRefreshService.getInstance().remoteRefresh(refreshMode, user.getSessionKey());
		
		//sema.acquire();
		
		if ( !result[0] ) {
			fail("Test failed");
		}
	}
}
