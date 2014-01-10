package script.ai;

import java.util.HashSet;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.ai.AIManager;
import com.xinqihd.sns.gameserver.battle.BattleCamp;
import com.xinqihd.sns.gameserver.battle.RoomType;
import com.xinqihd.sns.gameserver.chat.ChatType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceChat.BceChat;
import com.xinqihd.sns.gameserver.proto.XinqiBseBattleInit.BseBattleInit;
import com.xinqihd.sns.gameserver.proto.XinqiRoleInfo.RoleInfo;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

public class BattleInit {
	
	private static final Logger logger = LoggerFactory.getLogger(BattleInit.class);

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if ( result != null ) {
			return result;
		}
		IoSession serverIoSession = (IoSession) parameters[0];
		User aiUser = (User) parameters[1];
		Object message = parameters[2];
		
		if ( message instanceof BseBattleInit ) {
			BseBattleInit bseBattleInit = (BseBattleInit) message;
			String mapId = String.valueOf(bseBattleInit.getMapId());
			RoomType roomType = RoomType.values()[bseBattleInit.getBattleMode()];
			aiUser.putUserData(AIManager.BATTLE_MODE, roomType);
			aiUser.putUserData(AIManager.BATTLE_MAP_ID, mapId);

			int count = bseBattleInit.getRoleArrCount();
			int aiCampId = BattleCamp.LEFT.id();
			for ( int i=0; i<count; i++ ) {
				RoleInfo roleInfo = bseBattleInit.getRoleArr(i);
				if ( aiUser.getUsername().equals(roleInfo.getUserName()) ) {
					aiCampId = roleInfo.getCampId();
				}
			}
			HashSet enemies = new HashSet(); 
			for ( int i=0; i<count; i++ ) {
				RoleInfo roleInfo = bseBattleInit.getRoleArr(i);
				if ( roleInfo.getCampId() != aiCampId ) {
					enemies.add(roleInfo.getSessionId());
				}
			}
			aiUser.putUserData(AIManager.BATTLE_ENEMIES_KEY, enemies);
			logger.debug("BattleInit: mapId:{}, battleMode:", mapId, roomType);
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}

	/**
	 * Send Chat message to server
	 * @param content
	 */
	public static final void sendChat(String content, String sourceUserId, 
			IoSession serverIoSession, User aiUser, boolean isPrivate) {
		
		BceChat.Builder myBceChat = BceChat.newBuilder();
		if ( isPrivate ) {
			myBceChat.setMsgType(ChatType.ChatPrivate.ordinal());
		} else {
			myBceChat.setMsgType(ChatType.ChatCurrent.ordinal());
		}
		myBceChat.setMsgContent(content);
		myBceChat.setUsrId(sourceUserId);
		
		AIManager.getInstance().sendServerMessageToAIClient(
				serverIoSession, aiUser.getSessionKey(), myBceChat.build());
		
	}
}
