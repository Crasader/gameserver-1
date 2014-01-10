package script.boss;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import script.ai.BattleInit;

import com.xinqihd.sns.gameserver.boss.Boss;
import com.xinqihd.sns.gameserver.boss.BossManager;
import com.xinqihd.sns.gameserver.boss.BossPojo;
import com.xinqihd.sns.gameserver.boss.BossType;
import com.xinqihd.sns.gameserver.boss.BossWinType;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.script.function.UserCalculator;
import com.xinqihd.sns.gameserver.util.MathUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class CreateBossUser {

	private static final Logger logger = LoggerFactory.getLogger(BattleInit.class);

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if ( result != null ) {
			return result;
		}
		BossPojo bossPojo = (BossPojo) parameters[0];
		Boss boss = (Boss) parameters[1];
		User realUser = (User)parameters[2];
		int blood = 0;
		int totalBoss = 1;
		if ( bossPojo.getBossWinType() == BossWinType.KILL_ONE ) {
			/**
			 * 如果boss仅有一个，那么血量应为总进度减去当前进度的值
			 */
			blood = boss.getTotalProgress() - boss.getProgress();
		} else if ( bossPojo.getBossWinType() == BossWinType.KILL_MANY ) {
			blood = bossPojo.getBlood();
			totalBoss = 4;
		} else if ( bossPojo.getBossWinType() == BossWinType.COLLECT_DIAMOND ) {
			blood = bossPojo.getBlood();
		}
		if ( blood <= 0 ) {
			blood = 100;
		}
		User[] users = new User[totalBoss];
		int power = (int)EquipCalculator.calculateWeaponPower(
				bossPojo.getAttack(), bossPojo.getDefend(), bossPojo.getAgility(), 
				bossPojo.getLucky(), 0, 0);
		for ( int i=0; i<totalBoss; i++ ) {
			users[i] = new User();
			String bossName = LoginManager.getInstance().getRandomUserName();
			String roleName = bossPojo.getName()+(i+1);
			users[i].set_id(new UserId(bossName, 
					MathUtil.nextFakeInt(Integer.MAX_VALUE)));
			users[i].setUsername(bossName);
			users[i].setRoleName(roleName);
			users[i].setLevelSimple(bossPojo.getLevel());
			users[i].setAttack(bossPojo.getAttack());
			users[i].setDefend(bossPojo.getDefend());
			//same with real user
			if ( bossPojo.getBossWinType() == BossWinType.COLLECT_DIAMOND ) {
				users[i].setAgility(realUser.getAgility()-1);
			} else {
				if ( realUser != null ) {
					users[i].setAgility(realUser.getAgility()+10);
				} else {
					users[i].setAgility(bossPojo.getAgility());
				}
			}
			users[i].setLuck(bossPojo.getLucky());
			users[i].setBlood(blood);
			users[i].setTkew(bossPojo.getThew());
			users[i].setSkin(bossPojo.getDefend());
			users[i].setDamage(bossPojo.getAttack());
			users[i].setAI(true);
			users[i].setBoss(true);
			users[i].putUserData(BossManager.USER_ROLE_ATTACK, bossPojo.getRoleAttackScript());
			users[i].putUserData(BossManager.USER_ROLE_DEAD, bossPojo.getRoleDeadScript());
			users[i].putUserData(BossManager.USER_BOSS_POJO, bossPojo);
			users[i].putUserData(BossManager.USER_BOSS, boss);

			WeaponPojo suit = EquipManager.getInstance().getWeaponById(
					bossPojo.getSuitPropId());
			if ( suit != null ) {
				users[i].getBag().setWearPropData(suit.toPropData(100, WeaponColor.GREEN), PropDataEquipIndex.SUIT);
			}
			WeaponPojo weapon = EquipManager.getInstance().getWeaponById(
					bossPojo.getWeaponPropId());
			if ( weapon != null ) {
				users[i].getBag().setWearPropData(weapon.toPropData(
						100, WeaponColor.GREEN), PropDataEquipIndex.WEAPON);
			}
			users[i].setPowerSimple(power);
		}
		
		ArrayList list = new ArrayList();
		list.add(users);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
}
