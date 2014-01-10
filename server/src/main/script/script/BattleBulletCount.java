package script;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.battle.Battle;
import com.xinqihd.sns.gameserver.battle.BattleBitSetBullet;
import com.xinqihd.sns.gameserver.battle.BattleDataLoader4Bitmap;
import com.xinqihd.sns.gameserver.battle.BattleUser;
import com.xinqihd.sns.gameserver.battle.BuffToolType;
import com.xinqihd.sns.gameserver.battle.BulletTrack;
import com.xinqihd.sns.gameserver.battle.RoleAttack;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.geom.BitmapUtil;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoleAttack.BceRoleAttack;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * Calculate how many bullets and their angles in a role attack.
 * 
 * @author wangqi
 *
 */
public class BattleBulletCount {
	
	private static final Logger logger = LoggerFactory.getLogger(BattleBulletCount.class);

	public static ScriptResult func(Object[] parameters) {
    
		ScriptResult result = ScriptManager.checkParameters(parameters, 4);
		if ( result != null ) {
			return result;
		}
		
		Battle battle    				 = (Battle)parameters[0];
		BattleUser battleUser    = (BattleUser)parameters[1];
		BceRoleAttack bceRoleAttack = (BceRoleAttack)parameters[2];
		int roundWind        = (Integer)parameters[3];
		
		RoleAttack roleAttack = getBulletTracks(battle, battleUser, bceRoleAttack, roundWind);
		
		ArrayList list = new ArrayList();
		list.add(roleAttack);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
	
	/**
	 * 
	 * @param battle
	 * @param battleUser
	 * @param roleAttack
	 * @param roundWind
	 * @return
	 */
	public static RoleAttack getBulletTracks(Battle battle, BattleUser battleUser, 
			BceRoleAttack bceRoleAttack, int roundWind) {
		if ( battleUser.getTools().contains(BuffToolType.Wind) ) {
			roundWind = - roundWind;
		}
		
		String roleName = battleUser.getUser().getRoleName();
		int userx = bceRoleAttack.getUserx();
		int usery = bceRoleAttack.getUsery();
		if ( usery <= 0 ) {
			usery = 0;
		}
		int angle = bceRoleAttack.getAngle();
		if ( angle > 1000 ) {
			angle /= 1000;
		}
		//Calcualte the bombx and bomby
		int bombx = userx;
		if ( angle <= 90 ) {
			bombx += 15;
		} else {
			bombx -= 15;
		}
		int bomby = usery - 15;
		logger.debug("bombx: {}; bomby:{}", bombx, bomby);
		
		int power = bceRoleAttack.getPower();

		if ( logger.isDebugEnabled() ) {
			logger.info("RoleAttack: userPos: ({}, {}), angle: {}, power: {}", 
					new Object[]{userx, usery, angle, power});
		}
		battleUser.setPosX(userx);
		battleUser.setPosY(usery);
		
	  //每一轮的子弹数
		int bulletQuantity = 1;
		//子弹的攻击次数
		int attackTimes = 1;
		//Every bullet's angle difference. 
		int bulletAngleDiff = 0;
		int offx = 5;
		int offy = 30;
		if ( battleUser.getTools().contains(BuffToolType.AttackThreeBranch) ) {
			bulletQuantity *= 3;
			bulletAngleDiff = 5;
		}
		if ( battleUser.getTools().contains(BuffToolType.AttackOneMoreTimes) ) {
			attackTimes += 1;
		}
		if ( battleUser.getTools().contains(BuffToolType.AttackTwoMoreTimes) ) {
			attackTimes += 2;
		}
		if ( battleUser.getTools().contains(BuffToolType.Fly) ) {
			offx = offy = -1;
		}
		if ( battleUser.getTools().contains(BuffToolType.Guide) ) {
			offx = offy = 200;
		}
		int bulletCount = bulletQuantity * attackTimes;
		
		logger.debug("BulletQuantity: {}, AttackTimes: {}", bulletQuantity, attackTimes);
		
		//Get the bullet 
		String bulletId = Constant.EMPTY;
		List propDataList = battleUser.getUser().getBag().getWearPropDatas();
		PropData propData = (PropData)(propDataList.get(PropDataEquipIndex.WEAPON.ordinal()));
		if ( propData != null ) {
			WeaponPojo weapon = (WeaponPojo)propData.getPojo();
			bulletId = weapon.getBullet();
		}
		BattleBitSetBullet battleBullet = BattleDataLoader4Bitmap.getBattleBulletByName(bulletId);
		if ( battleBullet == null ) {
			logger.info("The bulletId {} is not found. Script will fail", bulletId);
		}
		
		BulletTrack[] bullets = new BulletTrack[bulletCount];
		for ( int i=0; i<bulletCount; i++ ) {
			bullets[i] = BitmapUtil.calculateBulletTrackSpeed(battleUser.getPosX(), battleUser.getPosY(), 
					power, angle+bulletAngleDiff*i, roundWind);
			//check if the map can be destroyed.
			if ( battle.getBattleMap().getMapPojo().isDamage() ) {
				bullets[i].pngNum = ((int)(100*BattleBitmapRoleAttack.calculateScale(bullets[i].angle)));
			} else {
				bullets[i].pngNum = 0;
			}
			bullets[i].bullet = bulletId;
			bullets[i].offx = offx;
			bullets[i].offy = offy;
		}
		
		RoleAttack roleAttack = new RoleAttack();
		roleAttack.setAttackTimes(attackTimes);
		roleAttack.setBulletQuatity(bulletQuantity);
		roleAttack.setBulletTracks(bullets);
		
		return roleAttack;
	}
}
