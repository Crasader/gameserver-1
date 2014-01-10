package script.boss;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import script.ai.BattleInit;

import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.script.function.UserCalculator;
import com.xinqihd.sns.gameserver.util.MathUtil;

public class FindBossAbility {

	private static final Logger logger = LoggerFactory.getLogger(BattleInit.class);

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if ( result != null ) {
			return result;
		}
		int level = (Integer)parameters[0];
		
		User user = new User();
		user.setLevel(level);
		int strengthLevel = 5;
		WeaponColor color = WeaponColor.WHITE;
		if ( level >= 80 ) {
			strengthLevel = 10;
			color = WeaponColor.ORGANCE;
		} else if ( level >= 60 ) {
			strengthLevel = 8;
			color = WeaponColor.PINK;
		} else if ( level >= 40 ) {
			strengthLevel = 5;
			color = WeaponColor.GREEN;
		} else if ( level >= 40 ) {
			strengthLevel = 3;
			color = WeaponColor.WHITE;
		} else {
			strengthLevel = 0;
			color = WeaponColor.WHITE;
		}

		Bag bag = user.getBag();
		PropDataEquipIndex[] equips = PropDataEquipIndex.values();
		for ( int i=0; i<equips.length; i++ ) {
			EquipType type = null;
			if ( equips[i] == PropDataEquipIndex.BRACELET1 ) {
					type = EquipType.DECORATION;
			} else if ( equips[i] == PropDataEquipIndex.BRACELET2 ) {
					type = EquipType.DECORATION;
			} else if ( equips[i] == PropDataEquipIndex.BUBBLE ) {
					type = EquipType.BUBBLE;
			} else if ( equips[i] == PropDataEquipIndex.CLOTH ) {
					type = EquipType.CLOTHES;
			} else if ( equips[i] == PropDataEquipIndex.EYE ) {
					type = EquipType.EXPRESSION;
			} else if ( equips[i] == PropDataEquipIndex.FACE ) {
					type = EquipType.FACE;
			} else if ( equips[i] == PropDataEquipIndex.GLASS ) {
					type = EquipType.GLASSES;
			} else if ( equips[i] == PropDataEquipIndex.HAIR ) {
					type = EquipType.HAIR;
			} else if ( equips[i] == PropDataEquipIndex.HAT ) {
					type = EquipType.HAT;
			} else if ( equips[i] == PropDataEquipIndex.NECKLACE ) {
					type = EquipType.DECORATION;
			} else if ( equips[i] == PropDataEquipIndex.RING1 ) {
					type = EquipType.JEWELRY;
			} else if ( equips[i] == PropDataEquipIndex.RING2) {
					type = EquipType.JEWELRY;
			} else if ( equips[i] == PropDataEquipIndex.SUIT ) {
					type = EquipType.SUIT;
			} else if ( equips[i] == PropDataEquipIndex.WEAPON ) {
					type = EquipType.WEAPON;
			} else if ( equips[i] == PropDataEquipIndex.WEDRING ) {
					type = EquipType.JEWELRY;
			} else if ( equips[i] == PropDataEquipIndex.WING ) {
					type = EquipType.WING;
			}
			Collection slot = EquipManager.getInstance().getWeaponsBySlot(type);
			if ( slot != null ) {
				Object[] weaponObjs = MathUtil.randomPick(slot, 1);
				PropData propData = null;
				WeaponPojo weapon = (WeaponPojo)weaponObjs[0];
				weapon = EquipManager.getInstance().getWeaponByTypeNameAndUserLevel(weapon.getTypeName(), user.getLevel());
				propData = weapon.toPropData(10, color);
				EquipCalculator.weaponUpLevel(propData, strengthLevel);
				user.getBag().addOtherPropDatas(propData);
				user.getBag().wearPropData(propData.getPew(), equips[i].index());
			} else {
				logger.debug("Slot is null: {}", equips[i]);
			}
		}
		ScriptManager.getInstance().runScript(ScriptHook.USER_LEVEL_UPGRADE, 
				new Object[]{user, level});
		
		//UserCalculator.updateUserBasicProp(user);
		ArrayList list = new ArrayList();
		list.add(user);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}

}
