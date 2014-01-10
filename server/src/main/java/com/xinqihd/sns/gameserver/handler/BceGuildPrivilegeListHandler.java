package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.proto.XinqiBceGuildPrivilegeList.BceGuildPrivilegeList;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildPrivilegeListHandler is used for protocol GuildPrivilegeList 
 * @author wangqi
 *
 */
public class BceGuildPrivilegeListHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceGuildPrivilegeListHandler.class);
	
	private static final BceGuildPrivilegeListHandler instance = new BceGuildPrivilegeListHandler();
	
	private BceGuildPrivilegeListHandler() {
		super();
	}

	public static BceGuildPrivilegeListHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey userSessionKey) 
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceGuildPrivilegeList");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		User user = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
		BceGuildPrivilegeList payload = (BceGuildPrivilegeList)request.payload;
		
		GuildManager.getInstance().listGuildPrivilege(user, payload.getGuildID());
	}
	
	
}
