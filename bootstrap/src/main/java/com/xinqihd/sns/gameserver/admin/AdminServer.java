package com.xinqihd.sns.gameserver.admin;

import java.util.EnumSet;

import org.apache.sshd.SshServer;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.bootstrap.ReloadCommandFactory;
import com.xinqihd.sns.gameserver.bootstrap.Server;

/**
 * This admin server listens on admin port and process admin command
 * 
 * shutdown: shutdown the server.
 * 
 * @author wangqi
 *
 */
public class AdminServer implements Server {
	
	private static final Logger logger = LoggerFactory.getLogger(AdminServer.class);
	
	private static final String COMMAND_FACTORY = "com.xinqihd.sns.gameserver.transport.admin.MyCommandFactory";
	
	private static AdminServer instance = new AdminServer();
	
	private SshServer sshd = null;
	
	private ReloadCommandFactory commandFactory = null;
	
	private AdminServer() {
		commandFactory = new ReloadCommandFactory(COMMAND_FACTORY);
	}
	
	/**
	 * Get the singleton object.
	 * @return
	 */
	public static AdminServer getInstance() {
		return instance;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.bootstrap.Server#reload()
	 */
	@Override
	public void reload() {
		if ( commandFactory != null ) {
			commandFactory.reload();
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.bootstrap.Server#startServer(java.lang.String, int)
	 */
	@Override
	public void startServer(String bindAddr, int port) {
		try {
			sshd = SshServer.setUpDefaultServer();
			sshd.setHost(bindAddr);
			sshd.setPort(port);
			
			SimpleGeneratorHostKeyProvider provider = new SimpleGeneratorHostKeyProvider("hostkey.ser", "RSA", 4096);
			sshd.setKeyPairProvider(provider);
			
			EnumSet<ProcessShellFactory.TtyOptions> options = EnumSet.allOf(ProcessShellFactory.TtyOptions.class);
			options.remove(ProcessShellFactory.TtyOptions.Echo);
			sshd.setShellFactory(new ProcessShellFactory(new String[] { "/bin/bash", "-i" }, options));
			
			sshd.setCommandFactory(commandFactory);
			
	    sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
	      public boolean authenticate(String username, String password, ServerSession session) {
	          return username != null && password.equals("VpWk5ujKA1c");
	      }
		  });
	    
			sshd.start();
			
			logger.info("AdminServer bind at " + bindAddr + ":" + port);
			
		} catch (Exception e) {
			logger.warn("Failed to start AdminServer", e);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.bootstrap.Server#stopServer()
	 */
	@Override
	public void stopServer() {
		try {
			if ( sshd != null ) {
				sshd.stop();
			}
		} catch (Exception e) {
			logger.warn("Failed to stop AdminServer: {}", e.getMessage());
		}
	}

}
