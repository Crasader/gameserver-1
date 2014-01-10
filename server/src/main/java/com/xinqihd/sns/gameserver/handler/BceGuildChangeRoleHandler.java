package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.guild.GuildRole;
import com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildChangeRoleHandler is used for protocol GuildChangeRole 
 * @author wangqi
 *
 */
public class BceGuildChangeRoleHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceGuildChangeRoleHandler.class);
	
	private static final BceGuildChangeRoleHandler instance = new BceGuildChangeRoleHandler();
	
	private BceGuildChangeRoleHandler() {
		super();
	}

	public static BceGuildChangeRoleHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey userSessionKey) 
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceGuildChangeRole");
		}
		XinqiMessage request = (XinqiMessage)message;
		User user = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
		BceGuildChangeRole payload = (BceGuildChangeRole)request.payload;
		String userIdStr = payload.getUserid();
		String targetRole = payload.getTargetrole();
		
		GuildManager.getInstance().changeGuildMemberRole(user, userIdStr, GuildRole.valueOf(targetRole));
	}
	
	
}
