package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.mongo.MailMessageManager;
import com.xinqihd.sns.gameserver.db.mongo.MailMessageManager.MailBoxType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceMailRead.BceMailRead;
import com.xinqihd.sns.gameserver.proto.XinqiBseMailRead.BseMailRead;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceBagTidyHandler is used for protocol BagTidy 
 * @author wangqi
 *
 */
public class BceMailReadHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceMailReadHandler.class);
	
	private static final BceMailReadHandler instance = new BceMailReadHandler();
	
	private BceMailReadHandler() {
		super();
	}

	public static BceMailReadHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceMailRead");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceMailRead mailRead = (BceMailRead)request.payload;
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		
		int count = mailRead.getReadIndexesCount();
	  /**
	   * 0: inbox
	   * 1: sentbox
	   */
		MailBoxType type = MailBoxType.inbox;
		try {
			int typeIndex = mailRead.getType();
			type = MailBoxType.values()[typeIndex];
		} catch (Exception e) {
		}
		for ( int i=0; i<count; i++ ) {
			int mailIndex = mailRead.getReadIndexes(i);
			MailMessageManager.getInstance().readMail(user, mailIndex, type);
		}
		
		BseMailRead.Builder bseMailRead = BseMailRead.newBuilder();
		bseMailRead.setSucceed(true);
		GameContext.getInstance().writeResponse(sessionKey, bseMailRead.build());
	}
	
}
