package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceUserConfig.BceUserConfig;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleConfig.BseRoleConfig;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleInfo.BseRoleInfo;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * The BceUserConfigHandler is used for protocol UserConfig 
 * @author wangqi
 *
 */
public class BceUserConfigHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceUserConfigHandler.class);
	
	private static final BceUserConfigHandler instance = new BceUserConfigHandler();
	
	private BceUserConfigHandler() {
		super();
	}

	public static BceUserConfigHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceUserConfig");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceUserConfig config = (BceUserConfig)request.payload;
		
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		user.setConfigHideHat(config.getHideHat());
		user.setConfigHideGlass(config.getHideGlass());
		user.setConfigHideSuite(config.getHideSuit());
		user.setConfigLeadFinish(config.getLeadFinished());
		user.setConfigMusicSwitch(config.getMusicSwitch());
		user.setConfigEffectSwitch(config.getEffectSwitch());
		user.setConfigMusicVolume(config.getMusicVolume());
		user.setConfigEffectVolume(config.getEffectVolume());
		
		GameContext.getInstance().getUserManager().saveUser(user, false);
		
		//Update user's info
		//BseRoleInfo roleInfo = user.toBseRoleInfo();
		//GameContext.getInstance().writeResponse(user.getSessionKey(), roleInfo);
		BseRoleConfig.Builder builder = BseRoleConfig.newBuilder();
		builder.setHideHat(config.getHideHat());
		builder.setHideGlasses(config.getHideGlass());
		builder.setHideSuit(config.getHideSuit());
		builder.setMusicSwitch(config.getMusicSwitch());
		builder.setEffectSwitch(config.getEffectSwitch());
		builder.setMusicVolume(config.getMusicVolume());
		builder.setEffectVolume(config.getEffectVolume());
		builder.setGuidestep(0);
		GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
		
		StatClient.getIntance().sendDataToStatServer(user, 
				StatAction.UserConfig, config.getHideSuit(), config.getHideGlass(), config.getMusicSwitch(), config.getEffectSwitch());
	}
	
	
}
