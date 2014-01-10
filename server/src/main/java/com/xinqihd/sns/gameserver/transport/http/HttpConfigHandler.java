package com.xinqihd.sns.gameserver.transport.http;

import static com.xinqihd.sns.gameserver.config.Constant.*;
import static com.xinqihd.sns.gameserver.util.StringUtil.*;

import java.net.SocketTimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.Stat;

public class HttpConfigHandler extends IoHandlerAdapter {
	
	private static final Log log = LogFactory.getLog(HttpConfigHandler.class);

	
	private static final byte[] NOT_FOUND_MSG = "Request URI not configured in system.".getBytes();
	private static final byte[] NOT_FOUND_VAR = "Request variable not found in system.".getBytes();

	private static GameContext gameContext = GameContext.getInstance();
	
	public HttpConfigHandler() {
	}
	
	/**
	 * It is called by the admin interface. It force all config data from
	 * Zookeeper to be reloaded.
	 */
	public static void reloadConfig() {
		gameContext.reloadContext();
	}

	/**
	 * When a new session is opened, we add it to our session cache group.
	 */
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		super.sessionOpened(session);
		Stat.getInstance().incHttpConnects();
	}

	/**
	 * When a session is closed, we remove it from our session cache group.
	 */
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		super.sessionClosed(session);
		Stat.getInstance().decHttpConnects();
	}

	/**
	 * When a session is idle from sometime, it will be closed and removed,
	 * for the sake of resources saving.
	 */
	@Override
	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
	}

	/**
	 * Receive the http request. Check its request uri.
	 */
	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		HttpMessage httpMessage = (HttpMessage)message;
		try {
			if ( httpMessage.getRequestUri() != null ) {
				String variable = null;
				if ( httpMessage.getRequestUri().indexOf('?') >= 0 ) {
					variable = substring(httpMessage.getRequestUri(), "?", null);
				}
				String url = substring(httpMessage.getRequestUri(), null, HTTP);
				String uri = substringR(url, PATH_SEP, QUESTION).trim();
				if ( log.isDebugEnabled() ) {
					log.debug("Request url: " + url + ".  uri: " + uri + ", variable: " + variable);
				}
				byte[] configData = null;
//				GameConfig gameConfig = gameContext.getGameConfig(uri);
//				if ( gameConfig != null ) {
//					BaseManager manager = gameConfig.getBaseManager(variable);
//					if ( manager != null ) {
//						httpMessage.setResponseLastModified(manager.getConfigLastModified());
//						configData = manager.getConfigCachedData();
//					} else {
//						configData = NOT_FOUND_VAR;
//					}
//					if ( log.isDebugEnabled() ) {
//						log.debug("Sent uri data to client");
//					}
//				} else {
//					log.warn("Failed to find the uri game config object: " + uri);
//				}
				httpMessage.setResponseContent(configData);
			} else {
				httpMessage.setResponseContent(NOT_FOUND_MSG);
			}
		} catch (Throwable e1) {
			e1.printStackTrace();
		}
		WriteFuture future = session.write(httpMessage);
		// We can wait for the client to close the socket.
//		future.addListener(IoFutureListener.CLOSE);
//		GameContext.getInstance().writeResponse(session, httpMessage);
	}

	/**
	 * Log the exception and close the session.
	 */
	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		try {
			if ( cause.getClass() == SocketTimeoutException.class ) {
				Stat.getInstance().incHttpTimeouts();
			} else {
				//java.io.IOException: Connection reset by peer
				Stat.getInstance().incHttpReset();
			}
			if ( log.isDebugEnabled() ) {
				log.debug("HTTP exception： ", cause);
			}
			session.close(true);
		} catch (Throwable e) {
			if ( log.isWarnEnabled() ) {
				log.warn("Close session", e);
			}
		}
	}
	
}
