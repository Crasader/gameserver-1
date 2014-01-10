package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceArmStrengthHandler is used for protocol ArmStrength 
 * @author wangqi
 *
 */
public class BceArmStrengthHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceArmStrengthHandler.class);
	
	private static final BceArmStrengthHandler instance = new BceArmStrengthHandler();
	
	private BceArmStrengthHandler() {
		super();
	}

	public static BceArmStrengthHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceArmStrength");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
    //TODO BseArmStrength not exists
		//XinqiBseArmStrength.BseArmStrength.Builder builder = XinqiBseArmStrength.BseArmStrength.newBuilder();
		//response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceArmStrength.BceArmStrength)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceArmStrength: " + response);
	}
	
	
}
