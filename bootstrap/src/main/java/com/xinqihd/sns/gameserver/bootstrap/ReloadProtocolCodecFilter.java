package com.xinqihd.sns.gameserver.bootstrap;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;

/**
 * It is used to manage the IoFilter and IoHandler
 * @author wangqi
 *
 */
public class ReloadProtocolCodecFilter implements IoFilter, IoHandler {
	
	private static final Log log = LogFactory.getLog(ReloadProtocolCodecFilter.class);
	
	private IoFilter ioFilter = null;
	
	private IoHandler ioHandler = null;
	
	private ReloadClassLoader classLoader = null;
	
	private String ioFilterClassName = null;
	
	private String ioHandlerClassName = null;
	
	public ReloadProtocolCodecFilter(String ioFilterClassName, String ioHandlerClassName){
		try {
			this.ioFilterClassName = ioFilterClassName;
			this.ioHandlerClassName = ioHandlerClassName;
			this.classLoader = ReloadClassLoader.currentClassLoader();
			Class ioFilterClass = this.classLoader.loadClass(ioFilterClassName);
			Class ioHandlerClass = this.classLoader.loadClass(ioHandlerClassName);
			this.ioFilter = (IoFilter)ioFilterClass.newInstance();
			this.ioHandler = (IoHandler)ioHandlerClass.newInstance();
		} catch (Throwable e) {
			log.error("Failed to create ReloadProtocolCodecFilter", e);
		}
	}

	
	/**
	 * Reload the internal instances.
	 */
	public void reload() {
		try {
			this.classLoader = ReloadClassLoader.newClassloader(this.classLoader.getClasspathURLs());
			Class ioFilterClass = this.classLoader.loadClass(ioFilterClassName);
			Class ioHandlerClass = this.classLoader.loadClass(ioHandlerClassName);
			this.ioFilter = (IoFilter)ioFilterClass.newInstance();
			this.ioHandler = (IoHandler)ioHandlerClass.newInstance();
		} catch (Throwable e) {
			log.error("Reload error", e);
		}
	}
	
	// ------------------------------------------------------- Delegate implementation

	/**
	 * @throws Exception
	 * @see org.apache.mina.core.filterchain.IoFilter#init()
	 */
	public void init() throws Exception {
		ioFilter.init();
	}

	/**
	 * @throws Exception
	 * @see org.apache.mina.core.filterchain.IoFilter#destroy()
	 */
	public void destroy() throws Exception {
		ioFilter.destroy();
	}

	/**
	 * @param parent
	 * @param name
	 * @param nextFilter
	 * @throws Exception
	 * @see org.apache.mina.core.filterchain.IoFilter#onPreAdd(org.apache.mina.core.filterchain.IoFilterChain, java.lang.String, org.apache.mina.core.filterchain.IoFilter.NextFilter)
	 */
	public void onPreAdd(IoFilterChain parent, String name, NextFilter nextFilter)
			throws Exception {
		ioFilter.onPreAdd(parent, name, nextFilter);
	}

	/**
	 * @param parent
	 * @param name
	 * @param nextFilter
	 * @throws Exception
	 * @see org.apache.mina.core.filterchain.IoFilter#onPostAdd(org.apache.mina.core.filterchain.IoFilterChain, java.lang.String, org.apache.mina.core.filterchain.IoFilter.NextFilter)
	 */
	public void onPostAdd(IoFilterChain parent, String name, NextFilter nextFilter)
			throws Exception {
		ioFilter.onPostAdd(parent, name, nextFilter);
	}

	/**
	 * @param parent
	 * @param name
	 * @param nextFilter
	 * @throws Exception
	 * @see org.apache.mina.core.filterchain.IoFilter#onPreRemove(org.apache.mina.core.filterchain.IoFilterChain, java.lang.String, org.apache.mina.core.filterchain.IoFilter.NextFilter)
	 */
	public void onPreRemove(IoFilterChain parent, String name,
			NextFilter nextFilter) throws Exception {
		ioFilter.onPreRemove(parent, name, nextFilter);
	}

	/**
	 * @param parent
	 * @param name
	 * @param nextFilter
	 * @throws Exception
	 * @see org.apache.mina.core.filterchain.IoFilter#onPostRemove(org.apache.mina.core.filterchain.IoFilterChain, java.lang.String, org.apache.mina.core.filterchain.IoFilter.NextFilter)
	 */
	public void onPostRemove(IoFilterChain parent, String name,
			NextFilter nextFilter) throws Exception {
		ioFilter.onPostRemove(parent, name, nextFilter);
	}

	/**
	 * @param nextFilter
	 * @param session
	 * @throws Exception
	 * @see org.apache.mina.core.filterchain.IoFilter#sessionCreated(org.apache.mina.core.filterchain.IoFilter.NextFilter, org.apache.mina.core.session.IoSession)
	 */
	public void sessionCreated(NextFilter nextFilter, IoSession session)
			throws Exception {
		ioFilter.sessionCreated(nextFilter, session);
	}

	/**
	 * @param nextFilter
	 * @param session
	 * @throws Exception
	 * @see org.apache.mina.core.filterchain.IoFilter#sessionOpened(org.apache.mina.core.filterchain.IoFilter.NextFilter, org.apache.mina.core.session.IoSession)
	 */
	public void sessionOpened(NextFilter nextFilter, IoSession session)
			throws Exception {
		ioFilter.sessionOpened(nextFilter, session);
	}

	/**
	 * @param nextFilter
	 * @param session
	 * @throws Exception
	 * @see org.apache.mina.core.filterchain.IoFilter#sessionClosed(org.apache.mina.core.filterchain.IoFilter.NextFilter, org.apache.mina.core.session.IoSession)
	 */
	public void sessionClosed(NextFilter nextFilter, IoSession session)
			throws Exception {
		ioFilter.sessionClosed(nextFilter, session);
	}

	/**
	 * @param nextFilter
	 * @param session
	 * @param status
	 * @throws Exception
	 * @see org.apache.mina.core.filterchain.IoFilter#sessionIdle(org.apache.mina.core.filterchain.IoFilter.NextFilter, org.apache.mina.core.session.IoSession, org.apache.mina.core.session.IdleStatus)
	 */
	public void sessionIdle(NextFilter nextFilter, IoSession session,
			IdleStatus status) throws Exception {
		ioFilter.sessionIdle(nextFilter, session, status);
	}

	/**
	 * @param nextFilter
	 * @param session
	 * @param cause
	 * @throws Exception
	 * @see org.apache.mina.core.filterchain.IoFilter#exceptionCaught(org.apache.mina.core.filterchain.IoFilter.NextFilter, org.apache.mina.core.session.IoSession, java.lang.Throwable)
	 */
	public void exceptionCaught(NextFilter nextFilter, IoSession session,
			Throwable cause) throws Exception {
		ioFilter.exceptionCaught(nextFilter, session, cause);
	}

	/**
	 * @param nextFilter
	 * @param session
	 * @param message
	 * @throws Exception
	 * @see org.apache.mina.core.filterchain.IoFilter#messageReceived(org.apache.mina.core.filterchain.IoFilter.NextFilter, org.apache.mina.core.session.IoSession, java.lang.Object)
	 */
	public void messageReceived(NextFilter nextFilter, IoSession session,
			Object message) throws Exception {
		ioFilter.messageReceived(nextFilter, session, message);
	}

	/**
	 * @param nextFilter
	 * @param session
	 * @param writeRequest
	 * @throws Exception
	 * @see org.apache.mina.core.filterchain.IoFilter#messageSent(org.apache.mina.core.filterchain.IoFilter.NextFilter, org.apache.mina.core.session.IoSession, org.apache.mina.core.write.WriteRequest)
	 */
	public void messageSent(NextFilter nextFilter, IoSession session,
			WriteRequest writeRequest) throws Exception {
		ioFilter.messageSent(nextFilter, session, writeRequest);
	}

	/**
	 * @param nextFilter
	 * @param session
	 * @throws Exception
	 * @see org.apache.mina.core.filterchain.IoFilter#filterClose(org.apache.mina.core.filterchain.IoFilter.NextFilter, org.apache.mina.core.session.IoSession)
	 */
	public void filterClose(NextFilter nextFilter, IoSession session)
			throws Exception {
		ioFilter.filterClose(nextFilter, session);
	}

	/**
	 * @param nextFilter
	 * @param session
	 * @param writeRequest
	 * @throws Exception
	 * @see org.apache.mina.core.filterchain.IoFilter#filterWrite(org.apache.mina.core.filterchain.IoFilter.NextFilter, org.apache.mina.core.session.IoSession, org.apache.mina.core.write.WriteRequest)
	 */
	public void filterWrite(NextFilter nextFilter, IoSession session,
			WriteRequest writeRequest) throws Exception {
		ioFilter.filterWrite(nextFilter, session, writeRequest);
	}

	/**
	 * @param session
	 * @throws Exception
	 * @see org.apache.mina.core.service.IoHandler#sessionCreated(org.apache.mina.core.session.IoSession)
	 */
	public void sessionCreated(IoSession session) throws Exception {
		ioHandler.sessionCreated(session);
	}

	/**
	 * @param session
	 * @throws Exception
	 * @see org.apache.mina.core.service.IoHandler#sessionOpened(org.apache.mina.core.session.IoSession)
	 */
	public void sessionOpened(IoSession session) throws Exception {
		ioHandler.sessionOpened(session);
	}

	/**
	 * @param session
	 * @throws Exception
	 * @see org.apache.mina.core.service.IoHandler#sessionClosed(org.apache.mina.core.session.IoSession)
	 */
	public void sessionClosed(IoSession session) throws Exception {
		ioHandler.sessionClosed(session);
	}

	/**
	 * @param session
	 * @param status
	 * @throws Exception
	 * @see org.apache.mina.core.service.IoHandler#sessionIdle(org.apache.mina.core.session.IoSession, org.apache.mina.core.session.IdleStatus)
	 */
	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
		ioHandler.sessionIdle(session, status);
	}

	/**
	 * @param session
	 * @param cause
	 * @throws Exception
	 * @see org.apache.mina.core.service.IoHandler#exceptionCaught(org.apache.mina.core.session.IoSession, java.lang.Throwable)
	 */
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		ioHandler.exceptionCaught(session, cause);
	}

	/**
	 * @param session
	 * @param message
	 * @throws Exception
	 * @see org.apache.mina.core.service.IoHandler#messageReceived(org.apache.mina.core.session.IoSession, java.lang.Object)
	 */
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		ioHandler.messageReceived(session, message);
	}

	/**
	 * @param session
	 * @param message
	 * @throws Exception
	 * @see org.apache.mina.core.service.IoHandler#messageSent(org.apache.mina.core.session.IoSession, java.lang.Object)
	 */
	public void messageSent(IoSession session, Object message) throws Exception {
		ioHandler.messageSent(session, message);
	}

	
}
