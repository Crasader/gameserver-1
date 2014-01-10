package com.xinqihd.sns.gameserver.transport.admin;

import com.xinqihd.sns.gameserver.Stat;

/**
 * List the server statistic
 * 
 * @author wangqi
 *
 */
public class StatCommand implements Command {
	
	public static final String COMMAND = "stat";
	
	public static final String GET = "get";
	
	public static final String RESET = "reset";
	
	public static final String HELP = "stat: [get|reset] to get the stat data or reset it";

	@Override
	public String execute(String[] args) {
		String arg = GET;
		if ( args.length >= 2 ) {
			if ( RESET.equals(args[1]) ) {
				arg = RESET;
			}
		}
		
		if ( arg == RESET ) {
			Stat.getInstance().reset();
		}
		
		return Stat.getInstance().toString().concat(">");
	}
	
	public String toString() {
		return HELP;
	}

}
