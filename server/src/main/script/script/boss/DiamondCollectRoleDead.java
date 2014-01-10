package script.boss;

import java.util.concurrent.TimeUnit;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.ai.AIManager;
import com.xinqihd.sns.gameserver.battle.Battle;
import com.xinqihd.sns.gameserver.battle.BattleManager;
import com.xinqihd.sns.gameserver.boss.Boss;
import com.xinqihd.sns.gameserver.boss.BossManager;
import com.xinqihd.sns.gameserver.boss.BossPojo;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceDead.BceDead;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleAttack.BseRoleAttack;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * The AI role attack logic in combat.
 * @author wangqi
 *
 */
public class DiamondCollectRoleDead {
	
	private static final Logger logger = LoggerFactory.getLogger(DiamondCollectRoleDead.class);

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if ( result != null ) {
			return result;
		}
		IoSession serverIoSession = (IoSession) parameters[0];
		User aiUser = (User) parameters[1];
		Boss boss = (Boss)aiUser.getUserData(BossManager.USER_BOSS);
		BossPojo bossPojo = boss.getBossPojo();
		
		Object message = parameters[2];
//		String userName = aiUser.getUsername();
		String roleName = aiUser.getRoleName();
		
		if ( message instanceof BseRoleAttack ) {
			boolean isRoundOver = false;
			String aiUserSession = aiUser.getSessionKey().toString();
			BseRoleAttack roleAttack = (BseRoleAttack)message;
			boolean isAIUserAttack = false;
			if ( aiUserSession.equals(roleAttack.getSessionId()) ) {
				isAIUserAttack = true;
				Long beginMillisLong = (Long)aiUser.getUserData(BossManager.USER_BEGIN_TIME);
				long beginMillis = 0;
				if ( beginMillisLong == null ) {
					beginMillis = System.currentTimeMillis();
					aiUser.putUserData(BossManager.USER_BEGIN_TIME, beginMillis);
				}
				long currentMillis = System.currentTimeMillis();
				//60秒结束战斗
				if ( currentMillis - beginMillis > 60000) {
					isRoundOver = true;
				}
			}
			
			if ( isRoundOver ) {
				BceDead.Builder roleDead = BceDead.newBuilder();
				
				AIManager.getInstance().sendServerMessageToAIClient(
						serverIoSession, aiUser.getSessionKey(), 
						roleDead.build(), 1, TimeUnit.SECONDS);
			} else {
				//生成宝箱
				Battle battle = (Battle)BattleManager.getInstance().findBattleByUserSessionKey(
						aiUser.getSessionKey());
				if ( battle != null ) {
					//battle.getTreasureBox();
				}
			}
			
			//Now check the user's position
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
}
