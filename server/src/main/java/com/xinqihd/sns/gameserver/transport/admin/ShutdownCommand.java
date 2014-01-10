package com.xinqihd.sns.gameserver.transport.admin;

import com.xinqihd.sns.gameserver.bootstrap.Bootstrap;

/**
 * Print the help message.
 * @author wangqi
 *
 */
public class ShutdownCommand implements Command {
	
	public static final String COMMAND = "shutdown";
	
	private static final String RESPONSE = "Shutdown server! \n>";
	
	@Override
	public String execute(String[] args) {
		Bootstrap.exit(RESPONSE, 0);
		return RESPONSE;
	}
	
	/**
	 * Get the help content
	 */
	public String toString() {
		return "shutdown: shutdown the whole server.";
	}


}
