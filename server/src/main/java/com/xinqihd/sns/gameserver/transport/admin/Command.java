package com.xinqihd.sns.gameserver.transport.admin;

/**
 * Abstract command interface.
 * 
 * @author wangqi
 *
 */
public interface Command {

	/**
	 * Execute the command with given arguments.
	 * @param args
	 * @return The command execute response.
	 */
	public String execute(String[] args);
	
}
