package script.boss;

import java.util.concurrent.TimeUnit;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import script.ai.BattleRoleAttack;
import script.ai.RoleAttackLogic;
import script.ai.TrainingLogic;

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
public class DiamondCollectRoleAttack {
	
	private static final Logger logger = LoggerFactory.getLogger(DiamondCollectRoleAttack.class);

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
				AIAction.askRoundOver(aiUser, serverIoSession, 0);
			}
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
}
