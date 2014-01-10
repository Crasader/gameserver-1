package com.xinqihd.sns.gameserver.transport.http;

import static com.xinqihd.sns.gameserver.config.Constant.*;
import static com.xinqihd.sns.gameserver.util.StringUtil.*;

import java.net.SocketTimeoutException;
import java.util.Collection;

import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.Stat;
import com.xinqihd.sns.gameserver.charge.AnzhiCharge;
import com.xinqihd.sns.gameserver.charge.BaoruanCharge;
import com.xinqihd.sns.gameserver.charge.CMCCCharge;
import com.xinqihd.sns.gameserver.charge.ChangyouCharge;
import com.xinqihd.sns.gameserver.charge.DangleCharge;
import com.xinqihd.sns.gameserver.charge.HuaweiCharge;
import com.xinqihd.sns.gameserver.charge.KupaiCharge;
import com.xinqihd.sns.gameserver.charge.LegendCharge;
import com.xinqihd.sns.gameserver.charge.MobageCharge;
import com.xinqihd.sns.gameserver.charge.OppoCharge;
import com.xinqihd.sns.gameserver.charge.ShenZhouFuCharge;
import com.xinqihd.sns.gameserver.charge.UCCharge;
import com.xinqihd.sns.gameserver.charge.Wanglong91Charge;
import com.xinqihd.sns.gameserver.charge.XiaomiCharge;
import com.xinqihd.sns.gameserver.charge.XinqihdCharge;
import com.xinqihd.sns.gameserver.charge.YeepayCharge;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.db.mongo.MailMessageManager;

public class HttpGameHandler extends IoHandlerAdapter {
	
	private static final Logger logger = LoggerFactory.getLogger(HttpGameHandler.class);

	public static final byte[] CONTENT_TYPE = "text/html;charset=utf8".getBytes();
	public static final byte[] CONTENT_PLAIN_TYPE = "text/plain;charset=utf8".getBytes();
	public static final byte[] CONTENT_XML_TYPE = "text/xml;charset=utf8".getBytes();
	
	private static final byte[] NOT_FOUND_MSG = "Request URI not configured in system.".getBytes();
	private static final byte[] NOT_FOUND_VAR = "Request variable not found in system.".getBytes();

	public HttpGameHandler() {
	}
	
	/**
	 * It is called by the admin interface. It force all config data from
	 * Zookeeper to be reloaded.
	 */
	public static void reloadConfig() {
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
				String uri = substring(httpMessage.getRequestUri(), null, HTTP);
				logger.debug("Request uri: {}, variable: {}", uri, variable);
				byte[] configData = NOT_FOUND_MSG;
				if ( uri.startsWith("/verifyemail") ) {
					VerifyEmailProcessor.getInstance().process(uri, variable, httpMessage);
				} else if ( uri.startsWith("/offlinepush") ) {
					Collection<String> offlineMessages = MailMessageManager.getInstance().
							listSimplePushMessages(variable);
					if ( offlineMessages != null ) {
						StringBuilder buf = new StringBuilder(200);
						for ( String offline : offlineMessages ) {
							buf.append(offline).append('\n');
						}
						httpMessage.setResponseContentType(HttpGameHandler.CONTENT_TYPE);
						httpMessage.setResponseContent(buf.toString().getBytes(Constant.charset));
					} else {
						httpMessage.setResponseContentType(HttpGameHandler.CONTENT_TYPE);
						httpMessage.setResponseContent(new byte[0]);
					}
				} else if ( uri.startsWith("/cmcccharge") ) {
					httpMessage = CMCCCharge.cmccChargeNotif(httpMessage, variable);
				} else if ( uri.startsWith("/dangle") ) {
					httpMessage = DangleCharge.chargeNotify(httpMessage, variable);
				} else if ( uri.startsWith("/xiaomi") ) {
					httpMessage = XiaomiCharge.chargeNotify(httpMessage, variable);
				} else if ( uri.startsWith("/changyou") ) {
					httpMessage = ChangyouCharge.chargeNotify(httpMessage, variable);
				} else if ( uri.startsWith("/lianxiang") ) {
					httpMessage = LegendCharge.chargeNotify(httpMessage, variable);
				} else if ( uri.startsWith("/xinqihd") ) {
					httpMessage = XinqihdCharge.chargeNotify(httpMessage, variable);
				} else if ( uri.startsWith("/baoruan") ) {
					httpMessage = BaoruanCharge.chargeNotify(httpMessage, variable);
				} else if ( uri.startsWith("/uc") ) {
					httpMessage = UCCharge.chargeNotify(httpMessage, variable);
				} else if ( uri.startsWith("/huawei") ) {
					httpMessage = HuaweiCharge.chargeNotify(httpMessage, variable);
				} else if ( uri.startsWith("/kupai") ) {
					httpMessage = KupaiCharge.chargeNotify(httpMessage, variable);
				} else if ( uri.startsWith("/91") ) {
					httpMessage = Wanglong91Charge.chargeNotify(httpMessage, variable);
				} else if ( uri.startsWith("/oppo") ) {
					httpMessage = OppoCharge.chargeNotify(httpMessage, variable);
				} else if ( uri.startsWith("/shenzhoufu") ) {
					httpMessage = ShenZhouFuCharge.chargeNotify(httpMessage, variable);
				} else if ( uri.startsWith("/yeepay") ) {
					httpMessage = YeepayCharge.chargeNotify(httpMessage, variable);
				} else if ( uri.startsWith("/anzhi") ) {
					httpMessage = AnzhiCharge.chargeNotify(httpMessage, variable);
				} else if ( uri.startsWith("/mobage/tmptoken") ) {
					httpMessage = MobageCharge.getInstance().processTmpToken(httpMessage, variable);
				} else if ( uri.startsWith("/mobage/accesstoken") ) {
					httpMessage = MobageCharge.getInstance().processAccessToken(httpMessage, variable);
				} else if ( uri.startsWith("/mobage/commit") ) {
					httpMessage = MobageCharge.getInstance().processTransaction(httpMessage, variable);
				} else {
					httpMessage.setResponseContent(configData);
				}
			} else {
				httpMessage.setResponseContent(NOT_FOUND_MSG);
			}
		} catch (Throwable e1) {
			e1.printStackTrace();
		}
		WriteFuture future = session.write(httpMessage);
		// We can wait for the client to close the socket.
		future.addListener(IoFutureListener.CLOSE);
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
			logger.debug("HTTP exception： ", cause);
			session.close(true);
		} catch (Throwable e) {
			logger.warn("Close session", e);
		}
	}
	
}
