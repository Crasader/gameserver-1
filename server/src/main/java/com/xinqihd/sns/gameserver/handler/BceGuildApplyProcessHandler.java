package com.xinqihd.sns.gameserver.handler;

import java.util.Collection;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.BasicUser;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.guild.Apply;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.proto.XinqiBceGuildApplyProcess.BceGuildApplyProcess;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildApplyList.BseGuildApplyList;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildApplyProcessHandler is used for protocol GuildApplyProcess 
 * @author wangqi
 *
 */
public class BceGuildApplyProcessHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceGuildApplyProcessHandler.class);
	
	private static final BceGuildApplyProcessHandler instance = new BceGuildApplyProcessHandler();
	
	private BceGuildApplyProcessHandler() {
		super();
	}

	public static BceGuildApplyProcessHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey userSessionKey) 
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceGuildApplyProcess");
		}
		XinqiMessage request = (XinqiMessage)message;
		User user = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
		BceGuildApplyProcess payload = (BceGuildApplyProcess)request.payload;
		int action = payload.getAction();
		String userId = payload.getUserid();
	   /**
	   * 处理的行为：
	   * 0: 全部同意
	   * 1: 全部拒绝
	   * 2: 同意
	   * 3: 拒绝
	   */ 
		GuildManager manager = GuildManager.getInstance();
		switch ( action ) {
			case 0:
				manager.processMultiGuildApply(user, true);
				break;
			case 1:
				manager.processMultiGuildApply(user, false);
				break;
			case 2:
				Apply apply = manager.queryGuildApply(user.getGuildId(), UserId.fromString(userId));
				if ( apply != null ) {
					manager.processGuildApply(user, apply, true);
				}
				break;
			case 3:
				apply = manager.queryGuildApply(user.getGuildId(), UserId.fromString(userId));
				if ( apply != null ) {
					manager.processGuildApply(user, apply, false);
				}
				break;
		}
		/**
		 * 要求客户端重新刷新列表
		 */
		BseGuildApplyList.Builder builder = BseGuildApplyList.newBuilder();
		Collection<Apply> applies = GuildManager.getInstance().listGuildApplys(user, user.getGuildId(), 0, 200);
		for ( Apply apply : applies) {
			BasicUser buser = UserManager.getInstance().queryBasicUser(apply.getUserId());
			if ( buser != null ) {
				builder.addMember(apply.toMember(buser));
			}
		}
		GameContext.getInstance().writeResponse(userSessionKey, builder.build());
	}
	
	
}
