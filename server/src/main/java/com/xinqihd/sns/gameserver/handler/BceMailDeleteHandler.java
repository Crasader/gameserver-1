package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.mongo.MailMessageManager;
import com.xinqihd.sns.gameserver.db.mongo.MailMessageManager.MailBoxType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceMailDelete.BceMailDelete;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * The BceBagTidyHandler is used for protocol BagTidy 
 * @author wangqi
 *
 */
public class BceMailDeleteHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceMailDeleteHandler.class);
	
	private static final BceMailDeleteHandler instance = new BceMailDeleteHandler();
	
	private BceMailDeleteHandler() {
		super();
	}

	public static BceMailDeleteHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceMailDelete");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceMailDelete mailDelete = (BceMailDelete)request.payload;
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		
		int count = mailDelete.getDeleteIndexesCount();
		int[] mailIndexes = new int[count];
		for ( int i=0; i<count; i++ ) {
			mailIndexes[i] = mailDelete.getDeleteIndexes(i);
		}
	  /**
	   * 0: inbox
	   * 1: sentbox
	   */
		MailBoxType type = MailBoxType.inbox;
		try {
			int typeIndex = mailDelete.getType();
			type = MailBoxType.values()[typeIndex];
		} catch (Exception e) {
		}
		if ( count == 1 && mailIndexes[0] == -1 ) {
			MailMessageManager.getInstance().deleteAllMail(user, type);
			
			StatClient.getIntance().sendDataToStatServer(
					user, StatAction.MailDelete, "-1");
		} else {
			int deleteCount = MailMessageManager.getInstance().deleteMail(user, mailIndexes, type);
			
			StatClient.getIntance().sendDataToStatServer(
					user, StatAction.MailDelete, deleteCount);
		}
	}
	
	
}
