package com.xinqihd.sns.gameserver.bootstrap;

/**
 * Represents the server lifecycle.
 * @author wangqi
 *
 */
public interface ServerLifecycle {

	/**
	 * Server first initializes
	 */
	public void init();
	
	/**
	 * Server pauses for accepting incoming requests.
	 */
	public void pause();
	
	/**
	 * Server resumes for accepting incomping requests
	 */
	public void resume();
	
	/**
	 * Server destroies.
	 */
	public void destroy();
	
}
