package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseGuildIronLevelup;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildIronLevelupHandler is used for protocol GuildIronLevelup 
 * @author wangqi
 *
 */
public class BceGuildIronLevelupHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceGuildIronLevelupHandler.class);
	
	private static final BceGuildIronLevelupHandler instance = new BceGuildIronLevelupHandler();
	
	private BceGuildIronLevelupHandler() {
		super();
	}

	public static BceGuildIronLevelupHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceGuildIronLevelup");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseGuildIronLevelup.BseGuildIronLevelup.Builder builder = XinqiBseGuildIronLevelup.BseGuildIronLevelup.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceGuildIronLevelup.BceGuildIronLevelup)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceGuildIronLevelup: " + response);
	}
	
	
}
