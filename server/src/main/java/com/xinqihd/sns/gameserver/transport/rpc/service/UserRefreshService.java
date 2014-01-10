package com.xinqihd.sns.gameserver.transport.rpc.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.Rpc.BoolResponse;
import com.xinqihd.sns.gameserver.proto.RpcUserRefresh;
import com.xinqihd.sns.gameserver.proto.RpcUserRefresh.RefreshReq;
import com.xinqihd.sns.gameserver.proto.RpcUserRefresh.UserRefresh;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.rpc.MinaRpcPoolChannel;

public class UserRefreshService extends UserRefresh {
	
	private static final Logger logger = LoggerFactory.getLogger(UserRefreshService.class);
	
	private ExecutorService service = Executors.newCachedThreadPool();
	
	private static final UserRefreshService instance = new UserRefreshService();
	
	private UserRefreshService() {
		
	}
	
	public static UserRefreshService getInstance() {
		return instance;
	}

  /**
   * 1: refresh user data
   * 2: refresh user bag data
   * 4: refresh user relation
   */
	@Override
	public void refresh(RpcController controller, final RefreshReq request,
			final RpcCallback<BoolResponse> done) {
		service.execute(new Runnable() {
			public void run() {
				int mode = request.getRefreshmode();
				ByteString pbSessionKey = request.getUserSessionKey();
				SessionKey sessionKey = SessionKey.createSessionKey(pbSessionKey.toByteArray());
				User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
				boolean result = false;
				if ( user != null ) {
					if ( (mode & 0x1) == 0x1 ) {
						logger.debug("refresh user {} basic data.", user.getRoleName());
						UserManager.getInstance().queryUser(user.get_id());
						result = true;
					}
					if ( (mode & 0x2) == 0x2 ) {
						logger.debug("refresh user {} bag data.", user.getRoleName());
						UserManager.getInstance().queryUserBag(user);
						result = true;
					}
					if ( (mode & 0x4) == 0x4 ) {
						logger.debug("refresh user {} relation data.", user.getRoleName());
						UserManager.getInstance().queryUserRelation(user);
						result = true;
					}
				} else {
					logger.info("#UserRefreshService: cannot find user in this server for sessionkye {}", sessionKey);
				}
				BoolResponse.Builder respBuilder = BoolResponse.newBuilder();
				respBuilder.setResult(result);
				done.run(respBuilder.build());
			}
		});
	}

	/**
	 * Call the remote rpc server's refresh method
	 * @param refreshMode
	 * @param userSessionKey
	 */
	public void remoteRefresh(int refreshMode, SessionKey userSessionKey) {
		if ( userSessionKey == null ) return;
		
		RefreshReq.Builder reqBuilder = RefreshReq.newBuilder();
		reqBuilder.setUserSessionKey(ByteString.copyFrom(userSessionKey.getRawKey()));
		reqBuilder.setRefreshmode(refreshMode);
		
		String rpcServerId = GameContext.getInstance().getSessionManager().findUserRpcId(userSessionKey);
		MinaRpcPoolChannel channel = GameContext.getInstance().findRpcChannel(rpcServerId);
		
		RpcUserRefresh.UserRefresh.Stub stub = RpcUserRefresh.UserRefresh.newStub(channel);

		final boolean[] result = new boolean[]{true};
		
		stub.refresh(null, reqBuilder.build(), new RpcCallback<BoolResponse>() {

			@Override
			public void run(BoolResponse parameter) {
				try {
					result[0] = parameter.getResult();
				} catch (Throwable e) {
					logger.warn("Failed to call remote rpc method: refresh.", e.getMessage());
					result[0] = false;
				}
			}
		});
		
		logger.debug("#remoteRefresh to rpcServer:{} succeed!", rpcServerId);
	}
}
