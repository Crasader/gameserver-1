package com.xinqihd.sns.gameserver;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

public class JndiContextFactory implements InitialContextFactory {
	
	@Override
	public Context getInitialContext(Hashtable<?, ?> environment)
			throws NamingException {
		return GameContext.getInstance().getJndiContext();
	}

}
