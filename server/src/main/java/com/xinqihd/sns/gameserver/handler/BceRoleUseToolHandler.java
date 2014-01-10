package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoleUseTool.BceRoleUseTool;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceRoleUseToolHandler is used for protocol RoleUseTool 
 * @author wangqi
 *
 */
public class BceRoleUseToolHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceRoleUseToolHandler.class);
	
	private static final BceRoleUseToolHandler instance = new BceRoleUseToolHandler();
	
	private BceRoleUseToolHandler() {
		super();
	}

	public static BceRoleUseToolHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceRoleUseTool");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceRoleUseTool tool = (BceRoleUseTool)request.payload;
		
		GameContext.getInstance().getBattleManager().roleUseTool(sessionKey, tool);
	}
	
	
}
