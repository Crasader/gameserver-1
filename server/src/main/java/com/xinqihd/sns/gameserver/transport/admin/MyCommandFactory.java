package com.xinqihd.sns.gameserver.transport.admin;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;

/**
 * Create our customized commands
 * @author wangqi
 *
 */
public class MyCommandFactory implements CommandFactory {

	@Override
	public Command createCommand(String command) {
		if ( command.equals("admin") ) {
			return new AdminCommand();
		}
		return null;
	}

}
