package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoleAttack.BceRoleAttack;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceRoleAttackHandler is used for protocol RoleAttack 
 * @author wangqi
 *
 */
public class BceRoleAttackHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceRoleAttackHandler.class);
	
	private static final BceRoleAttackHandler instance = new BceRoleAttackHandler();
	
	private BceRoleAttackHandler() {
		super();
	}

	public static BceRoleAttackHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceRoleAttack");
		}
		
//		Room room = RoomManager.getInstance().getRoom(0);
//		XinqiMessage request = (XinqiMessage)message1;
//		XinqiBceRoleAttack.BceRoleAttack message = (XinqiBceRoleAttack.BceRoleAttack)request.payload;
//		XinqiMessage response = new XinqiMessage();
//		
//		response.payload = room.attack(session, message);;
//		session.write(response);
		
		XinqiMessage request = (XinqiMessage)message;
		BceRoleAttack attack = (BceRoleAttack)request.payload;
		
		GameContext.getInstance().getBattleManager().roleAttack(sessionKey, attack);
	
	}
	
	
}
