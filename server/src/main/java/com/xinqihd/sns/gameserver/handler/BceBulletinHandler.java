package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.db.mongo.BulletinManager;
import com.xinqihd.sns.gameserver.proto.XinqiBceBulletin.BceBulletin;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceBagTidyHandler is used for protocol BagTidy 
 * @author wangqi
 *
 */
public class BceBulletinHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceBossTakeRewardHandler.class);
	
	private static final BceBulletinHandler instance = new BceBulletinHandler();
	
	private BceBulletinHandler() {
		super();
	}

	public static BceBulletinHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceBulletin");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceBulletin bulletin = (BceBulletin)request.payload;
		int type = bulletin.getType();
		int expire = bulletin.getExpire();
		String content = bulletin.getMessage();
		BulletinManager.getInstance().sendBulletinMessage(content, type, expire);
	}
	
}
