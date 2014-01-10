package script.ai;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.ai.AIManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.geom.SimplePoint;
import com.xinqihd.sns.gameserver.proto.XinqiAtkBltInfo.AtkBltInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBceDead.BceDead;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleAttack.BseRoleAttack;
import com.xinqihd.sns.gameserver.proto.XinqiDropUserInfo.DropUserInfo;
import com.xinqihd.sns.gameserver.proto.XinqiHurtUserInfo.HurtUserInfo;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

public class BattleRoleDead {
	
	private static final Logger logger = LoggerFactory.getLogger(BattleRoleDead.class);

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if ( result != null ) {
			return result;
		}
		IoSession serverIoSession = (IoSession) parameters[0];
		User aiUser = (User) parameters[1];
		Object message = parameters[2];
//		String userName = aiUser.getUsername();
		String roleName = aiUser.getRoleName();
		
		if ( message instanceof BseRoleAttack ) {
			boolean isAIDead = false;
			String aiUserSession = aiUser.getSessionKey().toString();
			BseRoleAttack roleAttack = (BseRoleAttack)message;
			boolean isAIUserAttack = false;
			if ( aiUserSession.equals(roleAttack.getSessionId()) ) {
				isAIUserAttack = true;
			}
			boolean isEnemyAttack = false;
			HashSet enemies = (HashSet)aiUser.getUserData(AIManager.BATTLE_ENEMIES_KEY);
			if ( enemies != null && enemies.contains(roleAttack.getSessionId()) ) {
				isEnemyAttack = true;
			}

			List atkBltInfos = roleAttack.getBltInfoList();
			LOOP:
			for ( int i=0; i<atkBltInfos.size(); i++) {
				AtkBltInfo atkBltInfo = (AtkBltInfo)atkBltInfos.get(i);
				SimplePoint hitPoint = new SimplePoint(atkBltInfo.getBltX(), atkBltInfo.getBltY());
				if ( isAIUserAttack ) {
					if ( hitPoint.getX() < Integer.MAX_VALUE ) {
						aiUser.putUserData(AIManager.BATTLE_LAST_ROUND_HITPOINT, hitPoint);
						logger.debug("The bullet is fired by aiUser. Update the hit point: {}.", hitPoint);
					} else {
						aiUser.putUserData(AIManager.BATTLE_LAST_ROUND_HITPOINT, null);
					}
				} else {
					//Collect user data
					aiUser.putUserData(AIManager.BATTLE_USER_ANGLE, roleAttack.getAngle());
					aiUser.putUserData(AIManager.BATTLE_USER_POWER, roleAttack.getPower());
					if ( hitPoint.getX() < Integer.MAX_VALUE ) {
						aiUser.putUserData(AIManager.BATTLE_USER_HITPOINT, hitPoint);
					} else {
						aiUser.putUserData(AIManager.BATTLE_USER_HURTAI, null);
					}
					int hurtCount = atkBltInfo.getHurtUserCount();
					Boolean isAIHurt = Boolean.FALSE;
					Boolean aiHurtEnemy = Boolean.FALSE;
					for ( int j=0; j<hurtCount; j++ ) {
						HurtUserInfo userInfo = atkBltInfo.getHurtUser(j);
						if ( aiUserSession.equals(userInfo.getUserId()) ) {
							isAIHurt = Boolean.TRUE;
							break;
						} else if (enemies != null && enemies.contains(userInfo.getUserId()) ) {
							aiHurtEnemy = Boolean.TRUE;
						}
					}
					aiUser.putUserData(AIManager.BATTLE_USER_HURTAI, isAIHurt);
					aiUser.putUserData(AIManager.BATTLE_AI_HURTENEMY, aiHurtEnemy);
				}
				List dropUserInfos = atkBltInfo.getDropUserList();
				for ( int j=0; j<dropUserInfos.size(); j++ ) {
					DropUserInfo dropUserInfo = (DropUserInfo)dropUserInfos.get(j);
					if ( aiUserSession.equals(dropUserInfo.getUserId()) ) {
						logger.debug("AI User '{}' is dropping to dead.", roleName);
						isAIDead = true;
						break LOOP;
					}
				}
				List hurtUserInfos = atkBltInfo.getHurtUserList();
				String targetEnemyKey = (String)aiUser.getUserData(AIManager.BATTLE_TARGET_ENEMY);
				for ( int j=0; j<hurtUserInfos.size(); j++ ) {
					HurtUserInfo hurtUserInfo = (HurtUserInfo)hurtUserInfos.get(j);
					String hurtUserId = hurtUserInfo.getUserId();
					if ( aiUserSession.equals(hurtUserId) ) {
						logger.debug("AI User '{}' is hurted.", roleName);
						if ( hurtUserInfo.getBlood() <= 0 ) {
							logger.debug("AI User '{}' is hurted to dead.", roleName);
							isAIDead = true;
							break LOOP;
						}
					} else if ( targetEnemyKey != null && targetEnemyKey.equals(hurtUserId) ) {
						if ( hurtUserInfo.getBlood() <= 0 ) {
							logger.debug("aiuser {} current target {} is dead. change another one", aiUser.getRoleName(), hurtUserId);
							aiUser.putUserData(AIManager.BATTLE_LAST_ROUND_ANGLE, null);
							aiUser.putUserData(AIManager.BATTLE_LAST_ROUND_POWER, null);
						}
					}
				}
			}
			
			if ( isAIDead ) {
				BceDead.Builder roleDead = BceDead.newBuilder();
				
				AIManager.getInstance().sendServerMessageToAIClient(
						serverIoSession, aiUser.getSessionKey(), 
						roleDead.build(), 1, TimeUnit.SECONDS);
			}
			
			//Now check the user's position
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
}
