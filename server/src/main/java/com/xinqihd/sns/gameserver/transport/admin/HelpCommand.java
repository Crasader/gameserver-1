package com.xinqihd.sns.gameserver.transport.admin;

/**
 * Print the help message.
 * @author wangqi
 *
 */
public class HelpCommand implements Command {
	
	public static final String COMMAND = "help";
		
	@Override
	public String execute(String[] args) {
		StringBuilder buf = new StringBuilder(500);
		for ( Command command : AdminCommand.commandMap.values() ) {
			if ( command.getClass() != NativeCommand.class )
				buf.append(command).append(" \n");
		}
		buf.append("\n>");
		return buf.toString();
	}
	
	/**
	 * Get the help content
	 */
	public String toString() {
		return "help: print this message.";
	}

}
