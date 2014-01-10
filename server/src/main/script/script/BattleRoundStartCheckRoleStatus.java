package script;

import com.xinqihd.sns.gameserver.battle.Battle;
import com.xinqihd.sns.gameserver.battle.BattleUser;
import com.xinqihd.sns.gameserver.battle.RoleStatus;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * BattleUser uses a BattleTool
 * 
 * @author wangqi
 * 
 */
public class BattleRoundStartCheckRoleStatus {

	/**
	 * 在每回合开始时，检查战斗用户的状态，
	 * 1. 如果用户处于冰冻状态，并且回合数尚未达到解冻条件，则自动蓄力
	 * 2. 如果用户处于隐身状态，并且回合数尚未达到显示条件，则继续隐身
	 * 
	 * @param parameters
	 * @return
	 */
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if (result != null) {
			return result;
		}
		Battle battle = (Battle) parameters[0];
		BattleUser battleUser = (BattleUser) parameters[1];
		
		checkBattle(battle, battleUser);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
	
	/**
	 * Check the battle user's status
	 * @param battle
	 * @param battleUser
	 */
	public static void checkBattle(Battle battle, BattleUser battleUser) {
		if ( battleUser.containStatus(RoleStatus.ICED) ) {
			int currentRound = battle.getRoundCount();
			int startRound = battleUser.getFrozenStartRound();
			int maxRound = GameDataManager.getInstance().getGameDataAsInt(
					GameDataKey.TOOL_ICED_VALUE, 3);
			if ( currentRound > maxRound + startRound ) {
				battleUser.clearStatus();
				battleUser.addStatus(RoleStatus.NORMAL);
				battleUser.setFrozenStartRound(-1);
				//增加delay回合数值，避免解冻后连续回合
				int delay = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.DELAY_ROLE_SAVE, 100);
				battleUser.setDelay(battleUser.getDelay()+(currentRound-startRound)*delay);
			}
		} else if ( battleUser.containStatus(RoleStatus.HIDDEN) ) {
			int currentRound = battle.getRoundCount();
			int startRound = battleUser.getHiddenStartRound();
			int maxRound = GameDataManager.getInstance().getGameDataAsInt(
					GameDataKey.TOOL_HIDDEN_VALUE, 3);
			if ( currentRound > maxRound + startRound ) {
				battleUser.clearStatus();
				battleUser.addStatus(RoleStatus.NORMAL);
				battleUser.setHiddenStartRound(-1);
			}
		}
	}
	
}
