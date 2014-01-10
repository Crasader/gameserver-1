package com.xinqihd.sns.gameserver.transport.admin;

/**
 * Print the help message.
 * @author wangqi
 *
 */
public class QuitCommand implements Command {
	
	public static final String COMMAND = "quit";
	
	private static final String BYE = "Bye bye! \n>";
	
	
	@Override
	public String execute(String[] args) {
		return BYE;
	}
	
	/**
	 * Get the help content
	 */
	public String toString() {
		return "quit: quit from the command line.";
	}

}
