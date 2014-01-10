package com.xinqihd.sns.gameserver.transport.admin;

import com.xinqihd.sns.gameserver.transport.http.HttpConfigHandler;

/**
 * Print the help message.
 * @author wangqi
 *
 */
public class ReloadConfigCommand implements Command {
	
	public static final String COMMAND = "reloadconfig";
	
	private static final String RESPONSE = "Reload zookeeper config data \n>";
	
	@Override
	public String execute(String[] args) {
		HttpConfigHandler.reloadConfig();
		return RESPONSE;
	}

	/**
	 * Get the help content.
	 */
	public String toString() {
		return "reloadconfig: reload zookeeper config data.";
	}
}
