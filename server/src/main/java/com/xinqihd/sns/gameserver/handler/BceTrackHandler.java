package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceTrack.BceTrack;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * The BceRoleUseToolHandler is used for protocol RoleUseTool 
 * @author wangqi
 *
 */
public class BceTrackHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceTrackHandler.class);
	
	private static final BceTrackHandler instance = new BceTrackHandler();
	
	private BceTrackHandler() {
		super();
	}

	public static BceTrackHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceTrack");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceTrack track = (BceTrack)request.payload;
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		
		String type = track.getTrack();
		String[] params = new String[4];
		int paramCount = track.getParamCount();
		paramCount = Math.min(paramCount, 4);
		for ( int i=0; i<paramCount; i++ ) {
			params[i] = track.getParam(i);
		}
		switch ( paramCount ) {
			case 0:
				StatClient.getIntance().sendDataToStatServer(
						user, StatAction.Track, type, params[0]);
				break;
			case 1:
				StatClient.getIntance().sendDataToStatServer(
						user, StatAction.Track, type, params[0], params[1]);
				break;
			case 2:
				StatClient.getIntance().sendDataToStatServer(
						user, StatAction.Track, type, params[0], params[1], params[2]);
				break;
			case 3:
				StatClient.getIntance().sendDataToStatServer(
						user, StatAction.Track, type, params[0], params[1], params[2], params[3]);
				break;
		}

	}
	
	
}
