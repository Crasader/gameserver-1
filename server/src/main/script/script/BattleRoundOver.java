package script;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.battle.ActionType;
import com.xinqihd.sns.gameserver.battle.BattleUser;
import com.xinqihd.sns.gameserver.battle.RoleStatus;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * 
 * 以下行为增加的DELAY值	
 * 行为	      DELAY值
 *  开炮	        100
 *  蓄力	        100
 *  小飞机	       55
 *  该轮时间结束	100
 *  移动	          0
 * 
 * @author wangqi
 *
 */
public class BattleRoundOver {

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		BattleUser battleUser = (BattleUser)parameters[0];
		if ( battleUser != null ) {
			boolean userAsked = (Boolean)parameters[1];
			/**
			 * 以下行为增加的DELAY值	
			 * 行为	DELAY值
			 * 蓄力	100
			 */
			/*
			battleUser.setDelay(battleUser.getDelay()+
					GameDataManager.getInstance().getGameDataAsInt(GameDataKey.DELAY_ROLE_SAVE, 100));
			*/
			//A new round will give full thew to an user.
			/**
			 * Every round will grow the thew for 20% percent
			 */
			int limitThew = battleUser.getUser().getTkew();
			int currentThew = battleUser.getThew();
			double thewRoundPercent = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.THEW_ROUND_PERCENT, 0.2);
			currentThew = (int)Math.round(currentThew + limitThew * thewRoundPercent);
			if ( currentThew > limitThew ) {
				currentThew = limitThew;
			}
			battleUser.setThew(currentThew);
			
			if ( battleUser.getActionType() == ActionType.FIRE) {
				int roleAttackDelay = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.DELAY_ROLE_ATTACK, 100); 
				battleUser.setDelay(battleUser.getDelay()+roleAttackDelay);
			} else if ( battleUser.getActionType() == ActionType.FLY) {
				int roleFlyDelay = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.DELAY_ROLE_FLY, 55); 
				battleUser.setDelay(battleUser.getDelay()+roleFlyDelay);
				battleUser.removeStatus(RoleStatus.FLYING);
			} else if ( userAsked || battleUser.getActionType() == ActionType.SAVE) {
				int roleSaveDelay = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.DELAY_ROLE_SAVE, 100); 
				battleUser.setDelay(battleUser.getDelay()+roleSaveDelay);
				//增加怒气值
				int energy = GameDataManager.getInstance().getGameDataAsInt(
						GameDataKey.TOOL_ENERGY_VALUE, 50);
				battleUser.setEnergy(battleUser.getEnergy() + energy);
			} else if ( battleUser.getActionType() == ActionType.TIMEOUT) {
				int roleSaveDelay = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.DELAY_ROLE_SAVE, 100);
				battleUser.setDelay(battleUser.getDelay()+roleSaveDelay);
			} else {
				battleUser.setDelay(battleUser.getDelay());
			}
			battleUser.setActionType(ActionType.DEFAULT);
		}
		ArrayList list = new ArrayList();
		list.add(battleUser);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
	
}
