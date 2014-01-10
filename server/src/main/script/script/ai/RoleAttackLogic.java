package script.ai;

import java.util.concurrent.TimeUnit;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.ai.AIAction;
import com.xinqihd.sns.gameserver.ai.AIManager;
import com.xinqihd.sns.gameserver.battle.BuffToolIndex;
import com.xinqihd.sns.gameserver.battle.BuffToolType;
import com.xinqihd.sns.gameserver.battle.RoleStatus;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoleAttack.BceRoleAttack;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoundStart.BseRoundStart;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.MathUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * The AI role attack logic in combat.
 * @author wangqi
 *
 */
public class RoleAttackLogic {
	
	private static final Logger logger = LoggerFactory.getLogger(RoleAttackLogic.class);

	public static ScriptResult roleAttack(final User aiUser, final IoSession serverIoSession, 
			final BseRoundStart roundStart) {
		if ( aiUser == null || aiUser.getSessionKey() == null) {
			logger.warn("#RoleAttackLogic. aiUser is null");
		} else {
			final String aiUserSessionId = aiUser.getSessionKey().toString();
			if ( roundStart.getSessionId().equals(aiUserSessionId) ) {
				int aiUserIndex = 0;
				boolean loop = true;
				for ( int i=0; loop && i<roundStart.getUserIdCount(); i++ ) {
					String sessionId = roundStart.getUserId(i);
					
					if (aiUserSessionId.equals(sessionId)) {
						aiUserIndex = i;
						loop = false;
					}
				}
				final int roundNumber = roundStart.getCurRound();
				final int myUserMode = roundStart.getUserMode(aiUserIndex);
				final int blood = roundStart.getBlood(aiUserIndex);
				final int energy = roundStart.getEnergy(aiUserIndex);
				int myX = 0;
				int myY = 0;
				Integer x = (Integer)aiUser.getUserData(AIManager.BATTLE_AI_X);
				Integer y = (Integer)aiUser.getUserData(AIManager.BATTLE_AI_Y);
				if ( x == null ) {
					myX = roundStart.getPosX(aiUserIndex);
				} else {
					myX = x.intValue();
				}
				if ( y == null ) {
					myY = roundStart.getPosY(aiUserIndex);
				} else {
					myY = y.intValue();
				}
				final int myCampId = roundStart.getCampid(aiUserIndex);
				logger.warn("AI user status: blood:{}, energy:{}, myX:{}, myY:{}", new Object[]{blood, energy, myX, myY});
				final int delay = (int)(MathUtil.nextDouble() * 1000);
				logger.debug("It is the AI user {} turn. delay: {}", aiUser.getRoleName());
				if ( RoleStatus.checkStatus(myUserMode) == RoleStatus.DEAD ) {
					logger.debug("The AI user {} is dead", aiUser.getRoleName());
				} else if ( RoleStatus.checkStatus(myUserMode) == RoleStatus.ICED ) {
					logger.debug("The AI user {} is frozen", aiUser.getRoleName());
					AIAction.askRoundOver(aiUser, serverIoSession, delay);
				} else {
					//check user command
					String command = (String)aiUser.getUserData(AIManager.BATTLE_USER_COMMAND);
					aiUser.putUserData(AIManager.BATTLE_USER_COMMAND, null);
					if ( StringUtil.checkNotEmpty(command) ) {
						if ( command.contains("stopattack") ) {
							AIAction.askRoundOver(aiUser, serverIoSession, delay);
						} else if ( command.contains("startattack") ) {
							aiUser.putUserData(AIManager.BATTLE_USER_COMMAND, null);
						} else if ( command.contains("hide") ) {
							aiUser.setTool(0, BuffToolType.Hidden);
							AIAction.roleUseGivenTool(aiUser, serverIoSession, BuffToolIndex.UserTool1);
							AIAction.askRoundOver(aiUser, serverIoSession, delay);
						} else if ( command.contains("ice") ) {
							aiUser.setTool(1, BuffToolType.Ice);
							AIAction.roleUseGivenTool(aiUser, serverIoSession, BuffToolIndex.UserTool1);
							AIAction.askRoundOver(aiUser, serverIoSession, delay);
						} else if ( command.contains("selfkill") ) {
							BceRoleAttack.Builder roleAttackBuilder = BceRoleAttack.newBuilder();
							roleAttackBuilder.setAngle(30);
							roleAttackBuilder.setAtkAngle(30);
							roleAttackBuilder.setPower(1);
							roleAttackBuilder.setUserx(myX);
							roleAttackBuilder.setUsery(myY);
							roleAttackBuilder.setDirection(0);
							BceRoleAttack attack = roleAttackBuilder.build();
							AIManager.getInstance().sendServerMessageToAIClient(
									serverIoSession, aiUser.getSessionKey(), attack, 
									2000+delay*6, TimeUnit.MILLISECONDS);
						} else if ( command.contains("help") ) {
							String help = "ai:stopattack & ai:startattack & ai:hide & ai:ice";
							UserChat.sendChat(help, aiUser.getUsername(), serverIoSession, aiUser, false);
							aiUser.putUserData(AIManager.BATTLE_USER_COMMAND, null);
						}
					} else {
						final int ux = myX;
						final int uy = myY;

						AIAction.roleSendExpress(aiUser, serverIoSession, 1000);
						doAction(aiUser, serverIoSession, blood, energy, roundStart, aiUserSessionId, delay, ux, uy, myCampId);

						ScriptResult result = new ScriptResult();
						result.setType(ScriptResult.Type.SUCCESS);
						result.setResult(null);
						return result;
					}
				}
			}
		}		
		ScriptResult result = new ScriptResult();
		result.setType(ScriptResult.Type.SCRIPT_FAIL);
		result.setResult(null);
		return result;
	}
	
	public static final void doAction(User aiUser, IoSession serverIoSession, 
			int blood, int energy, BseRoundStart roundStart, String aiUserSessionId, 
			int delay, int myX, int myY, int myCampId) {
		
		//attack
		//1. get target user's position
		int targetX = 0;
		int targetY = 0;
		String targetSessionId = null;
		int highestThew = 0;
		
		for ( int i=0; i<roundStart.getPosXCount(); i++ ) {
			String sessionId = roundStart.getUserId(i);
			int targetMode = roundStart.getUserMode(i);
			int campId = roundStart.getCampid(i);
			if (!aiUserSessionId.equals(sessionId) && myCampId != campId &&
					RoleStatus.checkStatus(targetMode) != RoleStatus.DEAD ) {
				if ( targetSessionId == null ) {
					targetSessionId = sessionId;
					targetX = roundStart.getPosX(i);
					targetY = roundStart.getPosY(i);
				}
				if ( highestThew < roundStart.getStrength(i) ) {
					highestThew = roundStart.getStrength(i);
					targetSessionId = sessionId;
					targetX = roundStart.getPosX(i);
					targetY = roundStart.getPosY(i);
				}
			}
		}
		aiUser.putUserData(AIManager.BATTLE_TARGET_ENEMY, targetSessionId);
		int wind = roundStart.getWind();
		BceRoleAttack attack = AIAction.roleAttack(aiUser, serverIoSession, wind, myX, myY, targetX, targetY);
		
		//Disable ai attack for debug purpose.
		AIManager.getInstance().sendServerMessageToAIClient(
				serverIoSession, aiUser.getSessionKey(), attack, 
				2000+delay, TimeUnit.MILLISECONDS);
	}
}
