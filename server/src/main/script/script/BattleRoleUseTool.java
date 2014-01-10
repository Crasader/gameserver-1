package script;

import java.util.List;

import com.xinqihd.sns.gameserver.battle.Battle;
import com.xinqihd.sns.gameserver.battle.BattleUser;
import com.xinqihd.sns.gameserver.battle.BuffToolType;
import com.xinqihd.sns.gameserver.battle.RoleStatus;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.LevelPojo;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.LevelManager;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * BattleUser uses a BattleTool
 * 
 * @author wangqi
 * 
 */
public class BattleRoleUseTool {

	/**
	 * 道具的名称 DELAY值 体力
	 * 
	 * 附加攻击2次 40 110 三叉戟导弹 40 110 附加攻击1次 40 110 伤害增加50% 25 85 伤害增加40% 20 80
	 * 伤害增加30% 15 70 伤害增加20% 10 55 伤害增加10% 5 50 血瓶 55 150 团队血瓶 60 170 隐身 20 50
	 * 团队隐身 55 150 改变风向 20 50 冰弹 55 150 传送 55 150 引导 45 120 怒气 45 120 核弹 40 110 免坑
	 * 20 50
	 * 
	 * @param parameters
	 * @return
	 */
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if (result != null) {
			return result;
		}
		Battle battle = (Battle) parameters[0];
		BattleUser battleUser = (BattleUser) parameters[1];
		BuffToolType toolType = (BuffToolType) parameters[2];

		if (toolType == BuffToolType.POWER) {
		} else if (toolType == BuffToolType.AllHidden) {
			List battleUsers = battleUser.getFriendUsers();
			for (int i = 0; i < battleUsers.size(); i++) {
				BattleUser bu = (BattleUser) battleUsers.get(i);
				bu.addStatus(RoleStatus.HIDDEN);
				bu.setHiddenStartRound(battle.getRoundCount());
			}
		} else if (toolType == BuffToolType.Atom) {
		} else if (toolType == BuffToolType.AttackOneMoreTimes) {
		} else if (toolType == BuffToolType.AttackThreeBranch) {
		} else if (toolType == BuffToolType.AttackTwoMoreTimes) {
		} else if (toolType == BuffToolType.Energy) {
			int energy = GameDataManager.getInstance().getGameDataAsInt(
					GameDataKey.TOOL_ENERGY_VALUE, 50);
			battleUser.setEnergy(battleUser.getEnergy() + energy);
		} else if (toolType == BuffToolType.Fly) {
		} else if (toolType == BuffToolType.Guide) {
		} else if (toolType == BuffToolType.Hidden) {
			battleUser.addStatus(RoleStatus.HIDDEN);
			battleUser.setHiddenStartRound(battle.getRoundCount());
		} else if (toolType == BuffToolType.HurtAdd10) {
		} else if (toolType == BuffToolType.HurtAdd20) {
		} else if (toolType == BuffToolType.HurtAdd30) {
		} else if (toolType == BuffToolType.HurtAdd40) {
		} else if (toolType == BuffToolType.HurtAdd50) {
		} else if (toolType == BuffToolType.Ice) {
			battleUser.setFrozenStartRound(battle.getRoundCount());
		} else if (toolType == BuffToolType.NoHole) {
		} else if (toolType == BuffToolType.AllRecover) {
			int blood = GameDataManager.getInstance().getGameDataAsInt(
					GameDataKey.TOOL_ALL_RECOVER_VALUE, 300);
			List battleUsers = battleUser.getFriendUsers();
			for (int i = 0; i < battleUsers.size(); i++) {
				BattleUser bu = (BattleUser) battleUsers.get(i);
				addBlood(bu, blood);
			}
		} else if (toolType == BuffToolType.Recover) {
			int blood = GameDataManager.getInstance().getGameDataAsInt(
					GameDataKey.TOOL_RECOVER_VALUE, 500);
			addBlood(battleUser, blood);
		} else if (toolType == BuffToolType.Wind) {
			//Change the next round's wind
			battle.setRoundWindDir( battle.getRoundWindDir() * -1 );
		}

		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
	
	/**
	 * Add blood to given battle user.
	 * @param battleUser
	 * @param blood
	 */
	public static final void addBlood(BattleUser battleUser, int blood) {
		LevelPojo level = LevelManager.getInstance().getLevel(battleUser.getUser().getLevel());
		int levelBlood = level.getBlood();
		int finalBlood = battleUser.getBlood() + (int)(levelBlood * blood/1000f);
		if ( finalBlood > levelBlood ) {
			finalBlood = levelBlood;
		}
		battleUser.setBlood(finalBlood);
	}
}
