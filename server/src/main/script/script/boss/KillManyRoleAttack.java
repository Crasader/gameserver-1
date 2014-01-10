package script.boss;

import java.util.concurrent.TimeUnit;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import script.ai.RoleAttackLogic;

import com.xinqihd.sns.gameserver.ai.AIAction;
import com.xinqihd.sns.gameserver.ai.AIManager;
import com.xinqihd.sns.gameserver.battle.BuffToolIndex;
import com.xinqihd.sns.gameserver.battle.BuffToolType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoleAttack.BceRoleAttack;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoleUseTool.BceRoleUseTool;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoundStart.BseRoundStart;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.MathUtil;

/**
 * The AI role attack logic in combat.
 * @author wangqi
 *
 */
public class KillManyRoleAttack {
	
	private static final Logger logger = LoggerFactory.getLogger(KillManyRoleAttack.class);
	
	private static final String[] chat = new String[]{
		"这是属于我们的小岛，快滚开!",
		"大月亮帝国万岁",
		"炸沉他们的船",
		"搜打死内",
	};

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
			int roundNumber = roundStart.getCurRound();
			if ( roundStart.getSessionId().equals(aiUserSessionId) ) {
				int roundNum = roundStart.getCurRound();
				/*
				if ( roundNum < chat.length ) {
					AIAction.roleChat(aiUser, serverIoSession, chat[roundNum], 1);
				}
				*/
				/**
				 * 此副本固定一个攻击数值
				 */
				int sign = 1;
				if ( MathUtil.nextDouble() < 0.5 ) {
					sign = -1;
				}
				int aiUserIndex = getAiIndex(aiUserSessionId, roundStart);
				if ( aiUserIndex < 2 ) {
				  //后两个士兵以散弹攻击为主
					int angle = 110 + sign*(int)(MathUtil.nextDouble()*5);
					int power = 65 + sign*(int)(MathUtil.nextDouble()*5);
					int myX = 0;
					int myY = 0;
					
					BceRoleUseTool useTool = BceRoleUseTool.newBuilder().setSlot(
						BuffToolIndex.AttackThreeBranch.slot()).build();
					AIManager.getInstance().sendServerMessageToAIClient(
							serverIoSession, aiUser.getSessionKey(), useTool, 1000, TimeUnit.MILLISECONDS);
					useTool = BceRoleUseTool.newBuilder().setSlot(
							BuffToolIndex.AttackTwoMoreTimes.slot()).build();
						AIManager.getInstance().sendServerMessageToAIClient(
								serverIoSession, aiUser.getSessionKey(), useTool, 1000, TimeUnit.MILLISECONDS);
					useTool = BceRoleUseTool.newBuilder().setSlot(
							BuffToolType.HurtAdd50.id()).build();
					AIManager.getInstance().sendServerMessageToAIClient(
							serverIoSession, aiUser.getSessionKey(), useTool, 1000, TimeUnit.MILLISECONDS);
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
					BceRoleAttack.Builder roleAttackBuilder = BceRoleAttack.newBuilder();
					roleAttackBuilder.setAngle(angle*1000);
					roleAttackBuilder.setAtkAngle(angle*1000);
					roleAttackBuilder.setPower(power);
					roleAttackBuilder.setUserx(myX);
					roleAttackBuilder.setUsery(myY);
					roleAttackBuilder.setDirection(0);
					BceRoleAttack attack = roleAttackBuilder.build();
					
					//Disable ai attack for debug purpose.
					AIManager.getInstance().sendServerMessageToAIClient(
							serverIoSession, aiUser.getSessionKey(), attack, 
							2000, TimeUnit.MILLISECONDS);
				} else {
				  //前两个士兵以引导攻击为主
					aiUser.setTool(0, BuffToolType.Guide);
					BceRoleUseTool useTool = BceRoleUseTool.newBuilder().setSlot(
							BuffToolIndex.UserTool1.slot()).build();
						AIManager.getInstance().sendServerMessageToAIClient(
								serverIoSession, aiUser.getSessionKey(), useTool, 1000, TimeUnit.MILLISECONDS);
					if ( aiUser.getLevel()>=40 ) {
					useTool = BceRoleUseTool.newBuilder().setSlot(
							BuffToolIndex.AttackTwoMoreTimes.slot()).build();
						AIManager.getInstance().sendServerMessageToAIClient(
								serverIoSession, aiUser.getSessionKey(), useTool, 1000, TimeUnit.MILLISECONDS);
					}
					if ( aiUser.getLevel()>=60 ) {
						useTool = BceRoleUseTool.newBuilder().setSlot(
							BuffToolType.HurtAdd50.id()).build();
						AIManager.getInstance().sendServerMessageToAIClient(
								serverIoSession, aiUser.getSessionKey(), useTool, 1000, TimeUnit.MILLISECONDS);
						useTool = BceRoleUseTool.newBuilder().setSlot(
							BuffToolType.HurtAdd50.id()).build();
						AIManager.getInstance().sendServerMessageToAIClient(
								serverIoSession, aiUser.getSessionKey(), useTool, 1000, TimeUnit.MILLISECONDS);
					}
					RoleAttackLogic.roleAttack(aiUser, serverIoSession, roundStart);
				}
			}
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}

	/**
	 * @param aiUserSessionId
	 * @param roundStart
	 * @param aiUserIndex
	 * @return
	 */
	private static int getAiIndex(String aiUserSessionId,
			final BseRoundStart roundStart) {
		boolean loop = true;
		int aiUserIndex = 0;
		for ( int i=0; loop && i<roundStart.getUserIdCount(); i++ ) {
			String sessionId = roundStart.getUserId(i);
			
			if (aiUserSessionId.equals(sessionId)) {
				aiUserIndex = i;
				loop = false;
			}
		}
		return aiUserIndex;
	}
}
