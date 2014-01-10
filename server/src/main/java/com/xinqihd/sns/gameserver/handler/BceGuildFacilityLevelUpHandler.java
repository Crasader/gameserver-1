package com.xinqihd.sns.gameserver.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildFacilityType;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.proto.*;
import com.xinqihd.sns.gameserver.proto.XinqiBceGuildFacilityLevelUp.BceGuildFacilityLevelUp;
import com.xinqihd.sns.gameserver.proto.XinqiBceGuildFacilityList.BceGuildFacilityList;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.*;

/**
 * The BceGuildFacilityLevelUpHandler is used for protocol GuildFacilityLevelUp 
 * @author wangqi
 *
 */
public class BceGuildFacilityLevelUpHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceGuildFacilityLevelUpHandler.class);
	
	private static final BceGuildFacilityLevelUpHandler instance = new BceGuildFacilityLevelUpHandler();
	
	private BceGuildFacilityLevelUpHandler() {
		super();
	}

	public static BceGuildFacilityLevelUpHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey userSessionKey) 
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceGuildFacilityLevelUp");
		}
		XinqiMessage request = (XinqiMessage)message;
		User user = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
		BceGuildFacilityLevelUp payload = (BceGuildFacilityLevelUp)request.payload;
		GuildFacilityType type = GuildFacilityType.fromId(payload.getType());
		GuildManager.getInstance().levelUpGuildFacility(user, type, payload.getCooldown());
	}
	
	
}
