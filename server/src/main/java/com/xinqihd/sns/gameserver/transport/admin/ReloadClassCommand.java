package com.xinqihd.sns.gameserver.transport.admin;

import com.xinqihd.sns.gameserver.admin.AdminServer;
import com.xinqihd.sns.gameserver.bootstrap.Bootstrap;
import com.xinqihd.sns.gameserver.server.GameServer;
import com.xinqihd.sns.gameserver.server.SimpleHttpServer;

/**
 * Print the help message.
 * @author wangqi
 *
 */
public class ReloadClassCommand implements Command {
	
	public static final String COMMAND = "reloadclass";
	
	private static final String RESPONSE = "Reload all classes into system. \n>";
	
	@Override
	public String execute(String[] args) {
		Bootstrap.getInstance().reload();
		GameServer.getInstance().reload();
		SimpleHttpServer.getInstance().reload();
		AdminServer.getInstance().reload();
		return RESPONSE;
	}

	/**
	 * Get the help content
	 */
	public String toString() {
		return "reloadclass: reload all the classes.";
	}

}
