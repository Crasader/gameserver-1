package com.xinqihd.sns.gameserver.transport;

import java.util.Locale;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.Stat;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.handler.BceBulletinHandler;
import com.xinqihd.sns.gameserver.handler.BceChargeHandler;
import com.xinqihd.sns.gameserver.handler.BceChargeInternalHandler;
import com.xinqihd.sns.gameserver.handler.BceCreateRoleHandler;
import com.xinqihd.sns.gameserver.handler.BceDeleteRoleHandler;
import com.xinqihd.sns.gameserver.handler.BceExitGameHandler;
import com.xinqihd.sns.gameserver.handler.BceForbidUserHandler;
import com.xinqihd.sns.gameserver.handler.BceForgetPasswordHandler;
import com.xinqihd.sns.gameserver.handler.BceInitHandler;
import com.xinqihd.sns.gameserver.handler.BceLoginHandler;
import com.xinqihd.sns.gameserver.handler.BceMailSendHandler;
import com.xinqihd.sns.gameserver.handler.BceOfflinePushHandler;
import com.xinqihd.sns.gameserver.handler.BceRegisterHandler;
import com.xinqihd.sns.gameserver.handler.BceReloadConfigHandler;
import com.xinqihd.sns.gameserver.handler.BceSelectRoleHandler;
import com.xinqihd.sns.gameserver.handler.BceServerListHandler;
import com.xinqihd.sns.gameserver.handler.BceUserRoleListHandler;
import com.xinqihd.sns.gameserver.proto.XinqiBseInit.BseInit;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.LocaleThreadLocal;

/**
 * It is a route channel handler responsible for finding the proper one and 
 * delegate the message to it.
 * 
 * @author wangqi
 *
 */
public class GameHandler extends IoHandlerAdapter {
	
	private static final String FREQUENCY_KEY = "op_freq_key";
	
	private static Logger logger = LoggerFactory.getLogger(GameHandler.class);

      
	/* (non-Javadoc)
	 * @see org.apache.mina.core.service.IoHandlerAdapter#sessionOpened(org.apache.mina.core.session.IoSession)
	 */
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		Stat.getInstance().gameServerOpen++;
		if ( logger.isDebugEnabled() ) {
			logger.debug("session opened");
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.core.service.IoHandlerAdapter#sessionClosed(org.apache.mina.core.session.IoSession)
	 */
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		Stat.getInstance().gameServerClose++;
		if ( logger.isDebugEnabled() ) {
			logger.debug("session closed");
		}
		GameContext.getInstance().deregisterUserByIoSession(session);
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.core.service.IoHandlerAdapter#sessionIdle(org.apache.mina.core.session.IoSession, org.apache.mina.core.session.IdleStatus)
	 */
	@Override
	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
		if ( StatClient.getIntance().isStatEnabled() ) {
			SessionKey userSessionKey = (SessionKey)session.getAttribute(Constant.SESSION_KEY);
			if ( userSessionKey != null ) {
				User user = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
				if ( user != null ) {
					GameContext.getInstance().deregisterUserBySessionKey(userSessionKey);
					logger.debug("User {} session is idle too much time. Close it.", user.getRoleName());
					
					StatClient.getIntance().sendDataToStatServer(user, StatAction.LogoutIdle);
				}
			}
		}
		try {
			session.close(false);
			//do cleaning
		} catch (Throwable e) {
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.core.service.IoHandlerAdapter#exceptionCaught(org.apache.mina.core.session.IoSession, java.lang.Throwable)
	 */
	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		Stat.getInstance().gameServerException++;
		
		if ( logger.isDebugEnabled() ) {
			logger.debug(cause.getMessage(), cause);
		}
	}

	/** 
	 * Call the corresponding business logic.
	 * @see org.apache.mina.core.service.IoHandlerAdapter#messageReceived(org.apache.mina.core.session.IoSession, java.lang.Object)
	 */
	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
//	SessionKey userSessionKey = GameContext.getInstance().findSessionKeyByIoSession(session);
		if ( message instanceof XinqiMessage ) {
			SessionKey userSessionKey = (SessionKey)session.getAttribute(Constant.SESSION_KEY);
			GameHandler.messageProcess(session, message, userSessionKey);
		} else if ( message instanceof XinqiProxyMessage ) {
			XinqiProxyMessage proxy = (XinqiProxyMessage)message;
			if ( proxy.userSessionKey != null && proxy.xinqi != null ) {
				SessionKey userSessionKey = proxy.userSessionKey;
				GameHandler.messageProcess(session, proxy.xinqi, userSessionKey);
			}
		}
	}

	/**
	 * Invoked when a message written by IoSession.write(Object) is sent out.
	 * @see org.apache.mina.core.service.IoHandlerAdapter#messageSent(org.apache.mina.core.session.IoSession, java.lang.Object)
	 */
	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		if ( logger.isDebugEnabled() ) {
			if ( message instanceof XinqiMessage ) {
				//logger.debug("message sent {}", ((XinqiMessage)message).payload.getClass().getName());
			}
		}
	}
	
	/**
	 * 
	 * @param session
	 * @param message
	 * @throws Exception 
	 */
	public static final void messageProcess(IoSession session, 
			Object message, SessionKey userSessionKey) throws Exception {
		
		//Check the frequency
		//AI may use this class too
	  /**
	   * Record user's last operation's time millis
	   * It prevents user to quickly press some buttons
	   * or the robot users.
	   */
		//Check frequency
		/*
		Long lastMillisLong = (Long)session.getAttribute(FREQUENCY_KEY);
		long lastMillis = 0;
		if ( lastMillisLong != null ) {
			lastMillis = lastMillisLong.longValue();
		}
		if ( System.currentTimeMillis() > lastMillis ) {
			int coolDownMillis =  1000;
			session.setAttribute(FREQUENCY_KEY, System.currentTimeMillis()+coolDownMillis);
		} else {
			/**
			 * The operation is too frequent for user.
			 *
			logger.debug("too frequent...");
			return;
		}
		*/

		Stat.getInstance().gameServerReceived++;
		
		SimpleChannelHandler handler = MessageToHandler.messageToHandler(message);
		
		if ( Constant.I18N_ENABLE ) {
			if ( userSessionKey != null ) {
				LocaleThreadLocal threadLocal = GameContext.getInstance().getLocaleThreadLocal();
				Locale userLocale = threadLocal.get();
				if ( userLocale == null ) {
					userLocale = GameContext.getInstance().getSessionManager().
							findUserLocale(userSessionKey);
					threadLocal.set(userLocale);
				}
			}
		}
		
		if ( handler != null ) {
			if ( handler instanceof BceLoginHandler || 
					handler instanceof BceInitHandler || 
					handler instanceof BceForgetPasswordHandler || 
					handler instanceof BceChargeInternalHandler || 
					handler instanceof BceChargeHandler || 
					handler instanceof BceOfflinePushHandler ||
					handler instanceof BceReloadConfigHandler ||
					handler instanceof BceCreateRoleHandler ||
					handler instanceof BceDeleteRoleHandler ||
					handler instanceof BceBulletinHandler ||
					handler instanceof BceUserRoleListHandler ||
					handler instanceof BceServerListHandler ||
					handler instanceof BceSelectRoleHandler ||
					handler instanceof BceExitGameHandler ||
					handler instanceof BceForbidUserHandler ||
					handler instanceof BceRegisterHandler ) {
				//logger.debug("User request login or reconnect.");
				handler.messageProcess(session, message, userSessionKey);
			} else {
				/**
				 * GameAdmin can use the SendGift protocol to send
				 * any reward to an user.
				 */
				if ( handler instanceof BceMailSendHandler) {
					handler.messageProcess(session, message, userSessionKey);
				} else {
					//logger.debug("Checking if users login");
					if ( userSessionKey == null ) {
						logger.debug("Client should send BceLogin or BceInit first. Ignore the request.");
						BseInit.Builder builder = BseInit.newBuilder();
						builder.setSuccess(false);
						builder.setRefresh(true);

						XinqiMessage response = new XinqiMessage();
						response.payload = builder.build();
						GameContext.getInstance().writeResponse(session, response, null);
					} else {
						handler.messageProcess(session, message, userSessionKey);					
					}
				}
			}
		} else {
			logger.warn("Failed to find specific game handler for {}", message);
		}
		/*
		if ( logger.isDebugEnabled() ) {
			logger.debug("messageReceived handler: " + handler.getClass().getName());
		}
		*/
	}

}
