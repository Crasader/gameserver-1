package com.xinqihd.sns.gameserver.handler;

import java.util.Collection;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.BasicUser;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.Apply;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.proto.XinqiBceGuildApplyList.BceGuildApplyList;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildApplyList.BseGuildApplyList;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * The BceGuildApplyListHandler is used for protocol GuildApplyList 
 * @author wangqi
 *
 */
public class BceGuildApplyListHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceGuildApplyListHandler.class);
	
	private static final BceGuildApplyListHandler instance = new BceGuildApplyListHandler();
	
	private BceGuildApplyListHandler() {
		super();
	}

	public static BceGuildApplyListHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey userSessionKey) 
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceGuildApplyList");
		}
		XinqiMessage request = (XinqiMessage)message;
		User user = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
		BceGuildApplyList payload = (BceGuildApplyList)request.payload;
		
		BseGuildApplyList.Builder builder = BseGuildApplyList.newBuilder();
		Collection<Apply> applies = GuildManager.getInstance().listGuildApplys(user, user.getGuildId(), 0, 200);
		for ( Apply apply : applies) {
			BasicUser buser = UserManager.getInstance().queryBasicUser(apply.getUserId());
			if ( buser != null ) {
				builder.addMember(apply.toMember(buser));
			}
		}
		GameContext.getInstance().writeResponse(userSessionKey, builder.build());

		StatClient.getIntance().sendDataToStatServer(user, StatAction.GuildListApply, user.getGuildId(), 0, 200);
	}
	
	
}
