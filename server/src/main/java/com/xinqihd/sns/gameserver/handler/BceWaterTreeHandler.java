package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseWaterTree;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceWaterTreeHandler is used for protocol WaterTree 
 * @author wangqi
 *
 */
public class BceWaterTreeHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceWaterTreeHandler.class);
	
	private static final BceWaterTreeHandler instance = new BceWaterTreeHandler();
	
	private BceWaterTreeHandler() {
		super();
	}

	public static BceWaterTreeHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceWaterTree");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseWaterTree.BseWaterTree.Builder builder = XinqiBseWaterTree.BseWaterTree.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceWaterTree.BceWaterTree)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceWaterTree: " + response);
	}
	
	
}
