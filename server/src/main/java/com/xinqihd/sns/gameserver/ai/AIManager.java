package com.xinqihd.sns.gameserver.ai;

import static com.xinqihd.sns.gameserver.util.StringUtil.*;
import i18n.AINames;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.BasicUser;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.session.SessionManager;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.MathUtil;
import com.xinqihd.sns.gameserver.util.OtherUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * AIManager is used to manage game's AI logic.
 * 
 * An AI User will be stored into the 'User' collection in database as 
 * a real user. Besides, the AI user will also be stored in 'AIUser' 
 * collection.
 * 
 * AIUser {
 *  _id:      <user id>
 *  username: <user name>
 *  level:    <user level>
 *  power:    <user power>
 * }
 * 
 * The AI system architecture:
 *      _____________         _____________         _____________
 *      |           |         |           |        |             |
 *      |Game Server|-------->| AI Client |------->| AI  Server  |
 *      |___________|         |___________|        |_____________|
 * 
 *       ___________               _____________           
 *      |           |             |             |
 *      | Real User |             | AI  Manager |
 *      |___________|             |_____________|
 *                                        
 * @author wangqi
 *
 */
public class AIManager {
	
	public static final String BATTLE_MODE = "mode";
	
	//The current battle's map id
	public static final String BATTLE_MAP_ID = "map_id";
	
	//The battle camp id
	public static final String BATTLE_CAMP_ID = "camp_id"; 
	
	//The power used in last round in a battle for AI 
	public static final String BATTLE_LAST_ROUND_POWER = "last_round_power"; 

	//The angle used in last round in a battle for AI 
	public static final String BATTLE_LAST_ROUND_ANGLE = "last_round_angle";
	
	//The last round user's hitpoint
	public static final String BATTLE_LAST_ROUND_HITPOINT = "last_round_hitpoint";
	
	//The last round target point
	public static final String BATTLE_LAST_ROUND_TARGETPOINT = "last_round_targetpoint";

	//The power used by real user in a battle 
	public static final String BATTLE_USER_POWER = "user_power"; 

	//The angle used by real user in a battle 
	public static final String BATTLE_USER_ANGLE = "user_angle";
	
	//The hit point by real user in a battle
	public static final String BATTLE_USER_HITPOINT = "user_hitpoint";
	
	//The user hurt ai
	public static final String BATTLE_USER_HURTAI = "user_hurtai";
	
	//The ai hurt enemy
	public static final String BATTLE_AI_HURTENEMY = "ai_hurtenemy";
	
  //The user hurt ai
	public static final String BATTLE_USER_COMMAND = "user_command";
	
	//The enemies' sessionKey set
	public static final String BATTLE_ENEMIES_KEY = "enemies_key";
	
  //The ai user's position
	public static final String BATTLE_AI_X = "ai_x";
	public static final String BATTLE_AI_Y = "ai_y";
	
	//The current target enemy
	public static final String BATTLE_TARGET_ENEMY = "target_enemy";
	
  //The total number of round that ai does not hit user.
	public static final String BATTLE_TOTAL_ROUND_NOT_HIT = "total_round_not_hit";
	
	private static final Logger logger = LoggerFactory.getLogger(AIManager.class);
		
	public static final AIManager instance = new AIManager();
	
	public static volatile int aiNameIndex = 0;
	
	private final HashMap<String, AIClient> aiClientChannelMap = 
			new HashMap<String, AIClient>();
	
	private AIClient handler = null;
	
	private String aiServerIdStr = null;
	
	private AILocalIoSession localIoSession = new AILocalIoSession();
	
	AIManager() {
		aiServerIdStr = GlobalConfig.getInstance().getStringProperty(
				GlobalConfig.RUNTIME_AI_SERVERID);
		if ( aiServerIdStr == null ) {
			aiServerIdStr = OtherUtil.getHostName()+":3446";
		}
		String remoteHost = "localhost";
		int remotePort = 0;
		String[] remoteStrs = StringUtil.splitMachineId(aiServerIdStr);
		if ( remoteStrs != null ) {
			remoteHost = remoteStrs[0];
			remotePort = StringUtil.toInt(remoteStrs[1], 0);
		}
		this.handler = new AIClient(remoteHost, remotePort);
		this.localIoSession.getFilterChain().addFirst("filter", new AIProtocolCodecFilter());
	}
	
	/**
	 * Get the singleton AIManager
	 * @return
	 */
	public static final AIManager getInstance() {
		return instance;
	}
	
	/**
	 * Create an AI user and log it in into game.
	 * @param realUser The AI user's properties will be generated according the real user.
	 * @return
	 */
	public final User createAIUser(User realUser) {
		ScriptResult result = ScriptManager.getInstance().runScript(
				ScriptHook.AI_USER_CREATE, realUser);
		if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
			List list = result.getResult();
			User aiUser = (User)list.get(0);
			//Register the AI user
			UserManager userManager = UserManager.getInstance();
			BasicUser existUser = userManager.queryBasicUser(aiUser.getUsername());
			if ( existUser != null ) {
				logger.debug("AI user '{}' already exists in database", aiUser.getRoleName());
				/**
				 * It cannot use the existing userid because that will cause only 
				 * one AI user exists in Redis.
				 * wangqi 2012-7-13
				 */
				//aiUser.set_id( existUser.get_id() );
				aiUser.set_id( new UserId(aiUser.getUsername(), 
						MathUtil.nextFakeInt(Integer.MAX_VALUE)) );
				/*
				if (existUser.getLevel()>aiUser.getLevel()) {
					userManager.saveUser(aiUser, true);
					userManager.saveUserBag(aiUser, true);
					logger.debug("Save the AI user's status into database");
				}
				*/
			} else {
				userManager.saveUser(aiUser, true);
				userManager.saveUserBag(aiUser, true);
				logger.debug("Save the AI user's status into database");
			}
			//Login the AI user
			return registerAIUser(aiUser);
		} else {
			logger.warn("Failed to create AI user according to the real user: {}", realUser);
		}
		return null;
	}
	
	/**
	 * Bind the user with AI session.
	 * @param aiUser
	 * @return
	 */
	public final User registerAIUser(User aiUser) {
		//Login the AI user
		IoSession session = this.handler.getIoSession();
		GameContext.getInstance().registerUserSession(session, aiUser, null);
		return aiUser;
	}
	
	/**
	 * When the battle is over, the aiUser should be 
	 * cleaned from system.
	 */
	public void removeAIUser(User aiUser) {
		if ( aiUser != null && aiUser.getSessionKey() != null ) {
			GameContext.getInstance().deregisterUserBySessionKey(aiUser.getSessionKey());
		} else {
			logger.debug("Failed to deregister ai user because it is null:{}", aiUser);
		}
	}
	
	/**
	 * Deregister the AIUser from game server. However the user record
	 * in database is not deleted.
	 * 
	 * @param aiUser
	 */
	public final void destroyAIUser(SessionKey sessionKey) {
		if ( sessionKey != null ) {
			GameContext.getInstance().deregisterUserBySessionKey(sessionKey);
		} else {
			logger.debug("Failed to deregister ai user because it is null:{}", sessionKey);
		}
	}
	
	/**
	 * Client a client message to AI Server. It will be processed by 
	 * AIServerHandler
	 * 
	 * @param userSessionKey
	 * @param message
	 */
	public void sendClientMessageToAIServer(SessionKey userSessionKey, Object message) {
		String machineId = GameContext.getInstance().findMachineId(userSessionKey);
		if ( machineId != null && !machineId.equals(this.aiServerIdStr) ) {
			AIClient client = null;
			client = aiClientChannelMap.get(machineId);
			if ( client == null ) {
				String[] results = splitMachineId(machineId);
				if ( results != null ) {
					client = new AIClient(
							results[0], toInt(results[1], 3446));
				}
				aiClientChannelMap.put(machineId, client);
			}
			if ( message instanceof SessionAIMessage ) {
				client.sendMessageToServer((SessionAIMessage)message);
			} else if ( message instanceof MessageLite ) {
				XinqiMessage xinqi = new XinqiMessage();
				xinqi.payload = (MessageLite)message;
				SessionAIMessage sessionAIMessage = new SessionAIMessage();
				sessionAIMessage.setMessage(xinqi);
				sessionAIMessage.setSessionKey(userSessionKey);
				client.sendMessageToServer(sessionAIMessage);
			} else {
				logger.warn("#AIManager.sendClientMessageToAIServer does not support:{}", 
						message);
			}	
		} else {
			//client = this.handler;
			try {
				AIServerHandler.getInstance().messageReceived(localIoSession, message);
			} catch (Exception e) {
				logger.warn("#sendClientMessageToAIServer.localhost: {}", e.getMessage());
			}
		}		
	}
	
	/**
	 * Send sessionAIMessage from AIServer to AIClient (GameServer)
	 * 
	 * @param userSessionKey
	 * @param message
	 */
	public void sendServerMessageToAIClient(
			IoSession serverIoSession, SessionAIMessage sessionAIMessage) {
		if ( serverIoSession instanceof AILocalIoSession ) {
			AIClientHandler.messageProcess(serverIoSession, sessionAIMessage);
		} else {
			serverIoSession.write(sessionAIMessage);
		}
	}
	
	/**
	 * Send sessionAIMessage from AIServer to AIClient (GameServer)
	 * 
	 * @param userSessionKey
	 * @param message
	 */
	public void sendServerMessageToAIClient(
			IoSession serverIoSession, SessionKey userSessionKey, Message message) {
		XinqiMessage xinqiMessage = new XinqiMessage();
		xinqiMessage.payload = message;
		
		SessionAIMessage sessionAIMessage = new SessionAIMessage();
		sessionAIMessage.setSessionKey(userSessionKey);
		sessionAIMessage.setMessage(xinqiMessage);
		if ( serverIoSession instanceof AILocalIoSession ) {
			AIClientHandler.messageProcess(serverIoSession, sessionAIMessage);
		} else {			
			serverIoSession.write(sessionAIMessage);			
		}
	}
	
	/**
	 * Send the message to client after a while.
	 * 
	 * @param serverIoSession
	 * @param userSessionKey
	 * @param message
	 * @param delay
	 * @param timeUnit
	 */
	public void sendServerMessageToAIClient(
			IoSession serverIoSession, SessionKey userSessionKey, Message message, int delay, TimeUnit timeUnit) {
		XinqiMessage xinqiMessage = new XinqiMessage();
		xinqiMessage.payload = message;
		
		SessionAIMessage sessionAIMessage = new SessionAIMessage();
		sessionAIMessage.setSessionKey(userSessionKey);
		sessionAIMessage.setMessage(xinqiMessage);
		
		DelayedMessage delayedMessage = new DelayedMessage(serverIoSession, sessionAIMessage);
		
		GameContext.getInstance().scheduleTask(delayedMessage, delay, timeUnit);
	}
	
	/**
	 * Check if an user is an AI user.
	 * @param userSessionKey
	 * @return
	 */
	public final boolean isAIUser(SessionKey userSessionKey) {
		String isAI = JedisFactory.getJedis().hget(userSessionKey.toString(), SessionManager.H_ISAI);
		if ( SessionManager.V_TRUE.equals(isAI) ) {
			return true;
		}
		return false;
	}

	/**
	 * Get a random AI user name
	 * @return
	 */
	public static final String getRandomAIName() {
		if ( aiNameIndex < 0 ) aiNameIndex = 0;
		return AINames.NAMES[aiNameIndex++ % AINames.NAMES.length];
	}
	
	/**
	 * Send the message after a while
	 * @author wangqi
	 *
	 */
	public static class DelayedMessage implements Runnable {
		SessionAIMessage message = null;
		IoSession session = null;
		
		DelayedMessage(IoSession session, SessionAIMessage message) {
			this.message = message;
			this.session = session;
		}
		
		public void run() {
			if ( this.session instanceof AILocalIoSession ) {
				AIClientHandler.messageProcess(this.session, message);
			} else {
				if ( this.session != null && this.session.isConnected() ) {
					this.session.write(message);
				} else {
					logger.info("#DelayedMessage AI session is null or not connected.");
				}
			}
		}
	}
}
