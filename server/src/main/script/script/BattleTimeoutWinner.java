package script;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.battle.Battle;
import com.xinqihd.sns.gameserver.battle.BattleCamp;
import com.xinqihd.sns.gameserver.battle.BattleUser;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * The battle is over. Calculate the final statistic data.
 * 
 * @author wangqi
 *
 */
public class BattleTimeoutWinner {
	
	private static final Logger logger = LoggerFactory.getLogger(BattleTimeoutWinner.class);

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if ( result != null ) {
			return result;
		}
		Battle battle = (Battle)parameters[0];
		List userLeftList = (List)parameters[1];
		List userRightList = (List)parameters[2];
		//If the battle in in draw. Check two sides blood
		int leftHurt =0;
		int rightHurt = 0;
		for ( int i=0; i<userLeftList.size(); i++ ) {
			BattleUser user = (BattleUser)userLeftList.get(i);
			if ( user == null ) continue;
			leftHurt += user.getUser().getBlood()-user.getBlood();
		}
		for ( int i=0; i<userRightList.size(); i++ ) {
			BattleUser user = (BattleUser)userRightList.get(i);
			if ( user == null ) continue;
			rightHurt += user.getUser().getBlood()-user.getBlood();
		}
		BattleCamp winCamp = BattleCamp.LEFT;
		if ( leftHurt > rightHurt ) {
			winCamp = BattleCamp.RIGHT;
		} else if ( leftHurt < rightHurt ) {
			winCamp = BattleCamp.LEFT;
		} else {
			winCamp = BattleCamp.LEFT;
		}
		logger.debug("Left hurt {} and right hurt: {}", leftHurt, rightHurt);
		
		ArrayList list = new ArrayList();
		list.add(winCamp);
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
}
