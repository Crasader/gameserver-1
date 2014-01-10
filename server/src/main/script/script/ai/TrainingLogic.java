package script.ai;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.ai.AIAction;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoundStart.BseRoundStart;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * The AI role attack logic in combat.
 * @author wangqi
 *
 */
public class TrainingLogic {
	
	private static final String TRAINING = "trainer.";
	
	private static final Logger logger = LoggerFactory.getLogger(TrainingLogic.class);

	public static ScriptResult roleAttack(User aiUser, IoSession serverIoSession, 
			BseRoundStart roundStart) {

		String aiUserSessionId = aiUser.getSessionKey().toString();
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

			if ( roundStart.getSessionId().equals(aiUserSessionId) ) {
				/*
				List tipList = TipManager.getInstance().getTips();

				int step = 0;
				Integer stepInt = (Integer)aiUser.getUserData(TRAINING);
				if ( stepInt == null ) {
					step = 0;
				} else {
					step = stepInt.intValue();
					step %= tipList.size();
				}
				aiUser.putUserData(TRAINING, step++);
				TipPojo tipPojo = (TipPojo)tipList.get(step);
				if ( tipPojo != null ) {
					AIAction.roleChat(aiUser, serverIoSession, tipPojo.getTip(), 200);
				}
				
				aiUser.putUserData(TRAINING, step);
				*/
				AIAction.askRoundOver(aiUser, serverIoSession, 3000);
			}

		}

		ScriptResult result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
}
