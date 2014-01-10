package com.xinqihd.sns.gameserver.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.proto.*;
import com.xinqihd.sns.gameserver.proto.XinqiBceGuildApplyList.BceGuildApplyList;
import com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeAnnounce.BceGuildChangeAnnounce;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.*;

/**
 * The BceGuildChangeAnnounceHandler is used for protocol GuildChangeAnnounce 
 * @author wangqi
 *
 */
public class BceGuildChangeAnnounceHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceGuildChangeAnnounceHandler.class);
	
	private static final BceGuildChangeAnnounceHandler instance = new BceGuildChangeAnnounceHandler();
	
	private BceGuildChangeAnnounceHandler() {
		super();
	}

	public static BceGuildChangeAnnounceHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey userSessionKey) 
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceGuildChangeAnnounce");
		}
		XinqiMessage request = (XinqiMessage)message;
		User user = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
		BceGuildChangeAnnounce payload = (BceGuildChangeAnnounce)request.payload;
		
		GuildManager.getInstance().changeGuildAnnounce(user, payload.getAnnouncement(), payload.getType());
	}
	
}
