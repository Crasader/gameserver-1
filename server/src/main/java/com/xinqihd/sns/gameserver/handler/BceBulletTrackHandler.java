package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.BulletTrack;
import com.xinqihd.sns.gameserver.proto.XinqiBceBulletTrack.BceBulletTrack;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceBulletTrackHandler is used for distributed calculation 
 * @author wangqi
 *
 */
public class BceBulletTrackHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceBulletTrackHandler.class);
	
	private static final BceBulletTrackHandler instance = new BceBulletTrackHandler();
	
	private BceBulletTrackHandler() {
		super();
	}

	public static BceBulletTrackHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceBulletTrackHandler");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceBulletTrack bulletTracks = (BceBulletTrack)request.payload;
		int count = bulletTracks.getBulletTracksCount();
		BulletTrack[] tracks = new BulletTrack[count];
		for ( int i=0; i<count; i++ ) {
			tracks[i] = BulletTrack.fromBceBulletTrack(bulletTracks.getBulletTracks(i));
		}
		GameContext.getInstance().getBattleManager().bulletTrack(sessionKey, 
				tracks, bulletTracks.getRoundNo(), bulletTracks);
	}
	
	
}
