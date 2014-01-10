package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceTransfer.BceTransfer;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceTransferHandler is used for protocol Transfer 
 * @author wangqi
 *
 */
public class BceTransferHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceTransferHandler.class);
	
	private static final BceTransferHandler instance = new BceTransferHandler();
	
	private BceTransferHandler() {
		super();
	}

	public static BceTransferHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceTransfer");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceTransfer transfer = (BceTransfer)request.payload;
		int srcEquipPew = transfer.getSrcEquipPew();
		int tarEquipPew = transfer.getTarEquipPew();
		
		//The User object should not be null because GameHandler is checking it.
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		GameContext.getInstance().getCraftManager().transferEquip(user, srcEquipPew, tarEquipPew);
	}
	
	
}
