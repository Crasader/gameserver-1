package com.xinqihd.sns.gameserver.ai;

import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.Stat;
import com.xinqihd.sns.gameserver.boss.BossManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseBattleInit.BseBattleInit;
import com.xinqihd.sns.gameserver.proto.XinqiBseChat.BseChat;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleAttack.BseRoleAttack;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleMove.BseRoleMove;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoundStart.BseRoundStart;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.SimpleClient;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * Internal AI message server handler
 * @author wangqi
 *
 */
public class AIServerHandler extends IoHandlerAdapter {
	
	private static final Logger logger = LoggerFactory.getLogger(AIServerHandler.class);
	
	//The client filter
	private AIProtocolCodecFilter filter = new AIProtocolCodecFilter();

	//The AI server client pool
	private SimpleClient aiClient = null;
	
	private static AIServerHandler instance = new AIServerHandler();
	
	
	public AIServerHandler() {
	}
	
	/**
	 * Get the instance
	 * @return
	 */
	public static AIServerHandler getInstance() {
		return instance;
	}
	
	// ----------------------------------------------- IoSession

	/**
	 * Session opened
	 */
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("session has been opened. ");
			}
		} finally {
		}
	}

	/**
	 * Session closed
	 */
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("session has been closed. ");
			}
		} finally {
		}
	}

	/**
	 * When a session is idle for configed seconds, it will send a heart-beat
	 * message to remote.
	 */
	@Override
	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("session has been idle for a while. Send a heartbeat message.");
			}
			Stat.getInstance().messageHearbeatSent++;
		} finally {
		}
	}

	/**
	 * Invoked when any exception is thrown by user IoHandler implementation or
	 * by MINA. If cause is an instance of IOException, MINA will close the
	 * connection automatically.
	 * 
	 */
	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		try {
			Stat.getInstance().messageClientSentFail++;
			if (logger.isDebugEnabled()) {
				logger.debug("Caught Exception: {}", cause.getMessage());
			}
		} finally {
		}
	}

	/**
	 * A message received when testing.
	 */
	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		// Server will never send a message to clients.
		try {
			if ( message instanceof SessionAIMessage ) {
				SessionAIMessage sessionAIMessage = (SessionAIMessage)message;
				if ( sessionAIMessage.getMessage() != null ) {
					XinqiMessage xinqiMessage = sessionAIMessage.getMessage();
	
					User aiUser = GameContext.getInstance().findLocalUserBySessionKey(
							sessionAIMessage.getSessionKey());
					if ( aiUser != null ) {
						if ( xinqiMessage.payload instanceof BseChat ) {
							logger.debug("AI User '{}' receive a chat message", aiUser.getRoleName());
							ScriptManager.getInstance().runScript(ScriptHook.AI_USER_CHAT, 
									session, aiUser, xinqiMessage.payload);
						} else if ( xinqiMessage.payload instanceof BseBattleInit ) {
							logger.debug("AI User '{}' receive a battleInit message", aiUser.getRoleName());
							ScriptManager.getInstance().runScript(ScriptHook.AI_BATTLE_INIT, 
									session, aiUser, xinqiMessage.payload);
						} else if ( xinqiMessage.payload instanceof BseRoundStart ) {
							logger.debug("AI User '{}' receive a battle roundstart message", aiUser.getRoleName());
							if ( !aiUser.isBoss() ) {
								ScriptManager.getInstance().runScript(ScriptHook.AI_BATTLE_ROLE_ATTACK, 
									session, aiUser, xinqiMessage.payload);
							} else {
								String script = (String)aiUser.getUserData(BossManager.USER_ROLE_ATTACK);
								ScriptManager.getInstance().runScript(ScriptHook.getScriptHook(script), 
										session, aiUser, xinqiMessage.payload);								
							}
						} else if ( xinqiMessage.payload instanceof BseRoleAttack ) {
							logger.debug("AI User '{}' receive a battle roleAttack message", aiUser.getRoleName());
							if ( !aiUser.isBoss() ) {
								ScriptManager.getInstance().runScript(ScriptHook.AI_BATTLE_ROLE_DEAD, 
										session, aiUser, xinqiMessage.payload);
							} else {
								String script = (String)aiUser.getUserData(BossManager.USER_ROLE_DEAD);
								ScriptManager.getInstance().runScript(ScriptHook.getScriptHook(script), 
										session, aiUser, xinqiMessage.payload);
							}
						} else if ( xinqiMessage.payload instanceof BseRoleMove ) {
							logger.debug("AI User '{}' receive a battle roleMove message", aiUser.getRoleName());
							ScriptManager.getInstance().runScript(ScriptHook.AI_BATTLE_ROLE_MOVE, 
									session, aiUser, xinqiMessage.payload);							
						} else {
							//logger.debug("AI does not support '{}' protocol.", xinqiMessage);
						}
					} else {
						logger.info("Cannot find the AI user by sessionKey: {}", sessionAIMessage.getSessionKey());
					}
				} else {
					logger.debug("Ignore the empty sessionAIMessage.");
				}
			} else {
				logger.debug("Ignore non SessionAIMessage: {}", message);
			}
		} catch (Exception e) {
			logger.error("Caught Exception: {}", e.getMessage());
		}
	}
	
	static class StatIoFilterListener implements IoFutureListener<IoFuture> {
		@Override
		public void operationComplete(IoFuture future) {
			Stat.getInstance().aiMessageSent++;
		}
	}
}
