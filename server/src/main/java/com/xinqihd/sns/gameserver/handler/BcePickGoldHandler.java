package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBsePickGold;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BcePickGoldHandler is used for protocol PickGold 
 * @author wangqi
 *
 */
public class BcePickGoldHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BcePickGoldHandler.class);
	
	private static final BcePickGoldHandler instance = new BcePickGoldHandler();
	
	private BcePickGoldHandler() {
		super();
	}

	public static BcePickGoldHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BcePickGold");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBsePickGold.BsePickGold.Builder builder = XinqiBsePickGold.BsePickGold.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBcePickGold.BcePickGold)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BcePickGold: " + response);
	}
	
	
}
