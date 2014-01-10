package com.xinqihd.sns.gameserver.bootstrap;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;

public class ReloadCommandFactory implements CommandFactory {
	
	private CommandFactory delegate = null;
	private String factoryName = null;
	
	public ReloadCommandFactory(String factoryName) {
		this.factoryName = factoryName;
		reload();
	}
	
	public final void reload() {
		try {
			delegate = (CommandFactory)ReloadClassLoader.currentClassLoader().loadClass(factoryName).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Command createCommand(String command) {
		return delegate.createCommand(command);
	}

}
