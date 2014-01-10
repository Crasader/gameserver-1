package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseChgBtlType;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceChgBtlTypeHandler is used for protocol ChgBtlType 
 * @author wangqi
 *
 */
public class BceChgBtlTypeHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceChgBtlTypeHandler.class);
	
	private static final BceChgBtlTypeHandler instance = new BceChgBtlTypeHandler();
	
	private BceChgBtlTypeHandler() {
		super();
	}

	public static BceChgBtlTypeHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceChgBtlType");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseChgBtlType.BseChgBtlType.Builder builder = XinqiBseChgBtlType.BseChgBtlType.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceChgBtlType.BceChgBtlType)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceChgBtlType: " + response);
	}
	
	
}
