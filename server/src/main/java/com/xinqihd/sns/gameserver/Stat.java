package com.xinqihd.sns.gameserver;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;

/**
 * The system simple statistic
 * 
 * @author wangqi
 *
 */
public class Stat {
	
	private static Stat instance = new Stat();
	
	
	private Date startDate = new Date();
	
	// ------------------------ HTTP related statistic
	
	//The total http socket connect number
	public volatile int httpConnects = 0;
	
	//The reset by peer error's number
	public volatile int httpResets = 0;
	
	//The connection timeout error's number
	public volatile int httpTimeouts = 0;
	
	//------------------------ Gameserver related statistic
	
	//The total game socket connect number
	public volatile int gameConnects = 0;
	
	//The reset by peer error's number
	public volatile int gameResets = 0;
	
  //The connection timeout error's number
	public volatile int gameTimeouts = 0;
	
	//------------------------ Gameserver related statistic
	
	//The global writing queue size
	public volatile int writeQueueSize = 0;
	
	//The global thread pool size.
	public volatile int writePoolSize = 0;
	
	//------------------------ MessageQueue related statistic
	
	public volatile int messageClientSent = 0;
		
	public volatile int messageServerReceived = 0;
	
	public volatile int messageClientSentFail = 0;
	
	public volatile int messageServerReceivedFail = 0;
	
	public volatile int messageHearbeatSent = 0;
	
	public volatile int messageHearbeatReceived = 0;
	
	public volatile int aiMessageSent = 0;
	
	public volatile int gameClientSent = 0;
	
	public volatile int gameClientSentFail = 0;
	
	public volatile int gameServerOpen = 0;
	
	public volatile int gameServerClose = 0;
	
	public volatile int gameServerException = 0;
	
	public volatile int gameServerReceived = 0;
	
	public volatile int gameServerReceivedFail = 0;
	
	public volatile int gameServerFinished = 0;
	
	public volatile int gameServerSent = 0;	
	
	public volatile int gameHearbeatSent = 0;
	
	public volatile int gameHearbeatReceived = 0;
	
  //------------------------ Login User Session
	
	public volatile int userSessionOnline = 0;
	
	public volatile int userSessionTotal = 0;
	
	public volatile int userSessionClose = 0;
	
  //------------------------ Login User Session
	
	public volatile int chatSent = 0;
	
	public volatile int chatReceived = 0;
	
	public volatile int chatBuffered = 0;
	
  //------------------------ Rpc Client Statistic
	
	public volatile int rpcClientSent = 0;
	
	public volatile int rpcClientSentFail = 0;
	
	
	//------------------------ Methods
	
	private Stat() {
		
	}
	
	/**
	 * Get a Stat instance.
	 * @return
	 */
	public static Stat getInstance() {
		return instance;
	}
	
	/**
	 * Reset all stat 
	 */
	public void reset() {
		startDate = new Date();
		try {
			Field[] fields = Stat.class.getDeclaredFields();
			int maxNameSize = 0;
			for ( int i=0; i<fields.length; i++ ) {
				Field field = fields[i];
				if ( !Modifier.isPublic(field.getModifiers()) ) {
					continue;
				}
				field.setAccessible(true);
				field.set(this, 0);
			}
		} catch ( Exception e ) {
		}
	}
	
	/**
	 * Output the stat information
	 */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(300);
		//Use reflection
		buf.append("\nStat from: ").append(startDate).append('\n');
		buf.append("=======================================\n");
		try {
			Field[] fields = Stat.class.getDeclaredFields();
			String[] statValues = new String[fields.length];
			int maxNameSize = 0;
			for ( int i=0; i<fields.length; i++ ) {
				Field field = fields[i];
				if ( !Modifier.isPublic(field.getModifiers()) ) {
					continue;
				}
				field.setAccessible(true);
				statValues[i] = String.valueOf(field.get(this).toString());
				if (maxNameSize < fields[i].getName().length()) {
					maxNameSize = fields[i].getName().length();
				}
			}
			maxNameSize += 2;
			//output
			for ( int i=0; i<statValues.length; i++ ) {
				if ( fields[i].getType() != int.class ) {
					continue;
				}
				String name = fields[i].getName();
				buf.append(name);
				for ( int j=name.length(); j<maxNameSize; j++ ) {
					buf.append(' ');
				}
				buf.append(':').append(' ');
				buf.append(statValues[i]);
				buf.append('\n');
			}
		} catch (Exception e) {
		}
		return buf.toString();
	}
	
	
	//------------------------ Properties 

	/**
	 * @return the httpConnects
	 */
	public int getHttpConnects() {
		return httpConnects;
	}

	/**
	 * @param httpConnects the httpConnects to set
	 */
	public void incHttpConnects() {
		this.httpConnects++;
	}
	
	/**
	 * @param httpConnects the httpConnects to set
	 */
	public void decHttpConnects() {
		this.httpConnects--;
	}

	/**
	 * @return the httpReset0
	 */
	public int getHttpResets() {
		return httpResets;
	}

	/**
	 * @param httpReset0 the httpReset0 to set
	 */
	public void incHttpReset() {
		this.httpResets++;
	}
	
	/**
	 * @param httpReset0 the httpReset0 to set
	 */
	public void decHttpReset() {
		this.httpResets--;
	}

	/**
	 * @return the httpTimeouts
	 */
	public int getHttpTimeouts() {
		return httpTimeouts;
	}

	/**
	 * @param httpTimeouts the httpTimeouts to set
	 */
	public void incHttpTimeouts() {
		this.httpTimeouts++;
	}
	
	/**
	 * @param httpTimeouts the httpTimeouts to set
	 */
	public void decHttpTimeouts() {
		this.httpTimeouts--;
	}

	/**
	 * @return the gameConnects
	 */
	public int getGameConnects() {
		return gameConnects;
	}

	/**
	 * @param gameConnects the gameConnects to set
	 */
	public void incGameConnects() {
		this.gameConnects++;
	}
	
	/**
	 * @param gameConnects the gameConnects to set
	 */
	public void decGameConnects() {
		this.gameConnects--;
	}

	/**
	 * @return the gameResets
	 */
	public int getGameResets() {
		return gameResets;
	}

	/**
	 * @param gameResets the gameResets to set
	 */
	public void incGameResets() {
		this.gameResets++;
	}
	
	/**
	 * @param gameResets the gameResets to set
	 */
	public void decGameResets() {
		this.gameResets--;
	}

	/**
	 * @return the gameTimeouts
	 */
	public int getGameTimeouts() {
		return gameTimeouts;
	}

	/**
	 * @param gameTimeouts the gameTimeouts to set
	 */
	public void incGameTimeouts() {
		this.gameTimeouts++;
	}
	
	/**
	 * @param gameTimeouts the gameTimeouts to set
	 */
	public void decGameTimeouts() {
		this.gameTimeouts--;
	}

	/**
	 * @return the writeQueueSize
	 */
	public int getWriteQueueSize() {
		return writeQueueSize;
	}

	/**
	 * @param writeQueueSize the writeQueueSize to set
	 */
	public void incWriteQueueSize() {
		this.writeQueueSize++;
	}
	
	/**
	 * @param writeQueueSize the writeQueueSize to set
	 */
	public void decWriteQueueSize() {
		this.writeQueueSize--;
	}

	/**
	 * @return the writePoolSize
	 */
	public int getWritePoolSize() {
		return writePoolSize;
	}

	/**
	 * @param writePoolSize the writePoolSize to set
	 */
	public void incWritePoolSize() {
		this.writePoolSize++;
	} 
	
	/**
	 * @param writePoolSize the writePoolSize to set
	 */
	public void decWritePoolSize() {
		this.writePoolSize--;
	}
	
	/**
	 * @param writePoolSize the writePoolSize to set
	 */
	public void incGameServerFinished() {
		this.gameServerFinished++;
	} 
		
}
