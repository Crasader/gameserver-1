package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseHarvestTree;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceHarvestTreeHandler is used for protocol HarvestTree 
 * @author wangqi
 *
 */
public class BceHarvestTreeHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceHarvestTreeHandler.class);
	
	private static final BceHarvestTreeHandler instance = new BceHarvestTreeHandler();
	
	private BceHarvestTreeHandler() {
		super();
	}

	public static BceHarvestTreeHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceHarvestTree");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseHarvestTree.BseHarvestTree.Builder builder = XinqiBseHarvestTree.BseHarvestTree.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceHarvestTree.BceHarvestTree)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceHarvestTree: " + response);
	}
	
	
}
