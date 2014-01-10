package script.ai;

import java.util.concurrent.TimeUnit;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.ai.AIAction;
import com.xinqihd.sns.gameserver.ai.AIManager;
import com.xinqihd.sns.gameserver.battle.RoomType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoundStart.BseRoundStart;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

public class BattleRoleAttack {
	
	private static final Logger logger = LoggerFactory.getLogger(BattleRoleAttack.class);

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
			RoomType roomType = (RoomType)aiUser.getUserData(AIManager.BATTLE_MODE);
			if ( roomType == RoomType.TRAINING_ROOM ) {
				return TrainingLogic.roleAttack(aiUser, serverIoSession, roundStart);
			} else {
				GameContext.getInstance().scheduleTask(new Runnable() {
					public void run() {
						AIAction.roleUseTool(aiUser, serverIoSession, roundStart);
						RoleAttackLogic.roleAttack(aiUser, serverIoSession, roundStart);
					}
				}, 3, TimeUnit.SECONDS);
			}
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
		
}
