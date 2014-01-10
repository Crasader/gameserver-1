package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.mongo.MailMessageManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceMailTake.BceMailTake;
import com.xinqihd.sns.gameserver.proto.XinqiBseMailTake.BseMailTake;
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
public class BceMailTakeHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceMailTakeHandler.class);
	
	private static final BceMailTakeHandler instance = new BceMailTakeHandler();
	
	private BceMailTakeHandler() {
		super();
	}

	public static BceMailTakeHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceMailTake");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceMailTake mailTake = (BceMailTake)request.payload;
		int mailIndex = mailTake.getMailIndex();
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		
		boolean success = MailMessageManager.getInstance().
				takeMailGift(user, mailTake.getMailIndex());
		
		BseMailTake.Builder bseMailTake = BseMailTake.newBuilder();
		bseMailTake.setSucceed(success);
		bseMailTake.setMailindex(mailIndex);
		GameContext.getInstance().writeResponse(sessionKey, bseMailTake.build());
		
		StatClient.getIntance().sendDataToStatServer(
				user, StatAction.MailTake, mailTake.getMailIndex());
	}
	
	
}
