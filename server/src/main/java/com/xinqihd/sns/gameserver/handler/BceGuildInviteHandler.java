package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.proto.XinqiBceGuildInvite.BceGuildInvite;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildInviteHandler is used for protocol GuildInvite 
 * @author wangqi
 *
 */
public class BceGuildInviteHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceGuildInviteHandler.class);
	
	private static final BceGuildInviteHandler instance = new BceGuildInviteHandler();
	
	private BceGuildInviteHandler() {
		super();
	}

	public static BceGuildInviteHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceGuildInvite");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		BceGuildInvite payload = (BceGuildInvite)request.payload;
		
		GuildManager.getInstance().sendRecruitInvite(user, payload.getUserid());
	}
	
	
}
