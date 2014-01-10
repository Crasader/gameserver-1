package script.ai;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.ai.AIManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleMove.BseRoleMove;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

public class BattleRoleMove {
	
	private static final Logger logger = LoggerFactory.getLogger(BattleRoleMove.class);

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
		
		if ( message instanceof BseRoleMove ) {
			String aiUserSession = aiUser.getSessionKey().toString();
			BseRoleMove roleMove = (BseRoleMove)message;
			boolean isAIUserMove = false;
			if ( aiUserSession.equals(roleMove.getSessionId()) ) {
				isAIUserMove = true;
			}
			if ( isAIUserMove ) {
				aiUser.putUserData(AIManager.BATTLE_AI_X, roleMove.getX());
				aiUser.putUserData(AIManager.BATTLE_AI_Y, roleMove.getY());
				if ( logger.isDebugEnabled() ) {
					logger.debug("update AI user {} position to {}:{}", 
						new Object[]{aiUser.getRoleName(), roleMove.getX(), roleMove.getY()});
				}
			}
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
}
