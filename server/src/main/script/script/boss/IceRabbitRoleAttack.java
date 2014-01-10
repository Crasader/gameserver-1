package script.boss;

import java.util.concurrent.TimeUnit;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import script.ai.BattleRoleAttack;
import script.ai.RoleAttackLogic;
import script.ai.TrainingLogic;
import script.ai.UserChat;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.ai.AIAction;
import com.xinqihd.sns.gameserver.ai.AIManager;
import com.xinqihd.sns.gameserver.battle.BuffToolIndex;
import com.xinqihd.sns.gameserver.battle.BuffToolType;
import com.xinqihd.sns.gameserver.battle.RoleStatus;
import com.xinqihd.sns.gameserver.battle.RoomType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoleAttack.BceRoleAttack;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoleUseTool.BceRoleUseTool;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoundStart.BseRoundStart;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.MathUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * The AI role attack logic in combat.
 * @author wangqi
 *
 */
public class IceRabbitRoleAttack {
	
	private static final Logger logger = LoggerFactory.getLogger(IceRabbitRoleAttack.class);

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if ( result != null ) {
			return result;
		}
		final IoSession serverIoSession = (IoSession) parameters[0];
		final User aiUser = (User) parameters[1];
		final Object message = parameters[2];
		
		String aiUserSessionId = aiUser.getSessionKey().toString();
		if ( message instanceof BseRoundStart ) {
			final BseRoundStart roundStart = (BseRoundStart)message;
			if ( roundStart.getSessionId().equals(aiUserSessionId) ) {
				//AIAction.roleChat(aiUser, serverIoSession, "你大爷的！", 1);
				BceRoleUseTool useTool = BceRoleUseTool.newBuilder().setSlot(
						BuffToolIndex.AttackTwoMoreTimes.slot()).build();
				AIManager.getInstance().sendServerMessageToAIClient(
						serverIoSession, aiUser.getSessionKey(), useTool, 1000, TimeUnit.MILLISECONDS);
				useTool = BceRoleUseTool.newBuilder().setSlot(
						BuffToolType.HurtAdd50.id()).build();
				AIManager.getInstance().sendServerMessageToAIClient(
						serverIoSession, aiUser.getSessionKey(), useTool, 1000, TimeUnit.MILLISECONDS);
			  
				roleAttack(aiUser, serverIoSession, roundStart);
			}
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
	
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
					final int ux = myX;
					final int uy = myY;

					if ( roundNumber % 5 == 0 ) {
						//间隔几个回合后发动引导
						aiUser.setTool(0, BuffToolType.Guide);
						BceRoleUseTool useTool = BceRoleUseTool.newBuilder().setSlot(
								BuffToolIndex.UserTool1.slot()).build();
							AIManager.getInstance().sendServerMessageToAIClient(
									serverIoSession, aiUser.getSessionKey(), useTool, 1000, TimeUnit.MILLISECONDS);
					}
					doAction(aiUser, serverIoSession, blood, energy, roundStart, aiUserSessionId, delay, ux, uy, myCampId);

					ScriptResult result = new ScriptResult();
					result.setType(ScriptResult.Type.SUCCESS);
					result.setResult(null);
					return result;
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
