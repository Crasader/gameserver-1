package com.xinqihd.sns.gameserver.transport.admin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

public class AdminCommand implements org.apache.sshd.server.Command, Runnable {
	
	private static final Log log = LogFactory.getLog(AdminCommand.class);
	
	public static final String COMMAND = "admin";
	
	public static final String ENCODING = "utf8";
	
	private InputStream in;
	
	private OutputStream out;
	
	private volatile boolean isStop = false;
	
	static final HashMap<String, Command> commandMap = new HashMap<String, Command>();
	
	private static LinkedList<String> commandHistory = new LinkedList<String>();
	
	static {
		commandMap.put(HelpCommand.COMMAND, new HelpCommand());
		commandMap.put(StatCommand.COMMAND, new StatCommand());
		commandMap.put(QuitCommand.COMMAND, new QuitCommand());
		commandMap.put(ReloadClassCommand.COMMAND, new ReloadClassCommand());
		commandMap.put(ReloadConfigCommand.COMMAND, new ReloadConfigCommand());
		commandMap.put(ShutdownCommand.COMMAND, new ShutdownCommand());
		commandMap.put(NativeCommand.COMMAND, new NativeCommand());
	}

	/* (non-Javadoc)
	 * @see org.apache.sshd.server.Command#setInputStream(java.io.InputStream)
	 */
	@Override
	public void setInputStream(InputStream in) {
		this.in = in;
	}

	/* (non-Javadoc)
	 * @see org.apache.sshd.server.Command#setOutputStream(java.io.OutputStream)
	 */
	@Override
	public void setOutputStream(OutputStream out) {
		this.out = out;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.sshd.server.Command#start(org.apache.sshd.server.Environment)
	 */
	@Override
	public void start(Environment env) throws IOException {
		this.out.write(("Welcome to " + InetAddress.getLocalHost().getHostName() + "! \n").getBytes(ENCODING));
		this.out.write(("It is " + new Date() + " now. \n>").getBytes(ENCODING));
		this.out.flush();
		
		Thread t = new Thread(this);
		t.start();
	}
	
	public void run() {
		while ( !isStop ) {
			try {
				String line = readLine();
				if ( line != null ) {
					String[] commands = line.split(" ");
					
					Command command = commandMap.get(commands[0]);
					if ( command == null ) {
						command = commandMap.get(HelpCommand.COMMAND);
					} else {
						if ( commandHistory.size() > 20 ) {
							commandHistory.removeFirst();
						}
						commandHistory.add(line);
					}
					String response = command.execute(commands);
					this.out.write(response.getBytes(ENCODING));
					this.out.flush();
					if ( command instanceof QuitCommand ) {
						isStop = true;
					}
				}
			} catch (Exception e) {
				log.error(e, e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.sshd.server.Command#setErrorStream(java.io.OutputStream)
	 */
	@Override
	public void setErrorStream(OutputStream err) {
	}

	/* (non-Javadoc)
	 * @see org.apache.sshd.server.Command#setExitCallback(org.apache.sshd.server.ExitCallback)
	 */
	@Override
	public void setExitCallback(ExitCallback callback) {
	}

	/* (non-Javadoc)
	 * @see org.apache.sshd.server.Command#destroy()
	 */
	@Override
	public void destroy() {
		isStop = true;
	}

  private String readLine() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    for (;;) {
    	//0x1b 0x5b 0x41 0xa
        int c = in.read();
        if (c == '\n') {
        	byte[] array = baos.toByteArray();
        	if ( array.length >= 3 && array[0] == 0x1b 
        			&& array[1] == 0x5b && array[2] == 0x41 ) {
        		return commandHistory.getLast();
        	}
          return baos.toString();
        } else if (c == -1) {
          throw new IOException("End of stream");
        } else {
          baos.write(c);
        }
    }
}
}
