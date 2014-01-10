package script.reward;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

public class UserLoginReward {
	
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		
		User user = (User)parameters[0];
		int slot = (Integer)parameters[1];
		
		ArrayList rewards = RewardManager.generateRandomRewards(user, slot, null);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(rewards);
		return result;
	}
	
}
