package script;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEnhanceField;
import com.xinqihd.sns.gameserver.entity.user.PropDataEnhanceType;
import com.xinqihd.sns.gameserver.entity.user.PropDataSlot;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * Check if all the props in bag are legal
 * @author wangqi
 *
 */
public class BagCheck {

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if ( result != null ) {
			return result;
		}

		User user = (User)parameters[0];
		Bag bag = user.getBag();
		List others = bag.getOtherPropDatas();
		List wears = bag.getWearPropDatas();
		/**
		 * 矫正武器的数值
		 */
		for (Iterator iter = wears.iterator(); iter.hasNext();) {
			PropData propData = (PropData) iter.next();
			if ( propData == null ) continue;
			//checkPropDataEmptyStrength(propData);
			checkPropDataIsWeapon(propData);
			convertOldPropDataToSlot(propData);
		}
		for (Iterator iter = others.iterator(); iter.hasNext();) {
			PropData propData = (PropData) iter.next();
			if ( propData == null ) continue;
			//checkPropDataEmptyStrength(propData);
			checkPropDataIsWeapon(propData);
			convertOldPropDataToSlot(propData);
		}

		/**
		 * 校对VIP属性
		 */
		if ( user.isVip() && user.getViplevel()>0 ) {
			int[] bagSpace = GameDataManager.getInstance().getGameDataAsIntArray(GameDataKey.VIP_BAG_SPACE);
			int vipBag = bagSpace[user.getViplevel()-1];
			if ( bag.getMaxCount() != vipBag ) {
				bag.setMaxCount(vipBag);
				String text = Text.text("bag.check.vipcount", new Object[]{vipBag});
				SysMessageManager.getInstance().sendClientInfoMessage(user.getSessionKey(), text, Type.NORMAL);
			}
		}
		UserManager.getInstance().saveUserBag(user, true);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
	
	/**
	 * 偶尔出现了空白的颜色和非武器属性的道具
	 * @param propData
	 */
	public static void checkPropDataIsWeapon(PropData propData) {
		if ( !propData.isWeapon() ) {
			WeaponPojo weapon = EquipManager.getInstance().getWeaponById(propData.getItemId());
			if ( weapon != null ) {
				propData.setWeapon(true);
				if ( propData.getWeaponColor() == null ) {
					propData.setWeaponColor(WeaponColor.WHITE);
				}
			}
		}
	}
	
	/**
	 * 重新核对背包中的武器数值
	 * @param propData
	 */
	/*
	public static void checkWeaponProp(PropData propData) {
		if ( propData.isWeapon() ) {
			WeaponPojo weapon = EquipManager.getInstance().getWeaponById(propData.getItemId());
			if ( weapon != null ) {
				int attack = propData.getAttackLev();
				int totalAttack = propData.getSlotTotalValue(PropDataEnhanceField.ATTACK);
				int strAttack = propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.ATTACK);
				if ( attack < totalAttack + strAttack + propData.getBaseAttack() )  {
					propData.setAttackLev(totalAttack + strAttack + propData.getBaseAttack());
				}
				int defend = propData.getDefendLev();
				int totalDefend = propData.getSlotTotalValue(PropDataEnhanceField.DEFEND);
				int strDefend = propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.DEFEND);
				if ( defend < totalDefend + strDefend + propData.getBaseDefend())  {
					propData.setDefendLev(totalDefend + strDefend + propData.getBaseDefend());
				}
				int agility = propData.getAgilityLev();
				int totalAgility = propData.getSlotTotalValue(PropDataEnhanceField.AGILITY);
				if ( agility != totalAgility + propData.getBaseAgility())  {
					propData.setAgilityLev(totalAgility + propData.getBaseAgility());
				}
				int lucky = propData.getLuckLev();
				int totalLucky = propData.getSlotTotalValue(PropDataEnhanceField.LUCKY);
				if ( lucky != totalLucky + propData.getBaseLuck())  {
					propData.setLuckLev(totalLucky + propData.getBaseLuck());
				}
				int damage = propData.getDamageLev();
				int strDamage = propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.DAMAGE);
				if ( damage < strDamage )  {
					propData.setDamageLev(strDamage);
				}
				int skin = propData.getSkinLev();
				int strSkin = propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.SKIN);
				if ( skin < strSkin )  {
					propData.setSkinLev(strSkin);
				}
				EquipCalculator.calculatePropDataPower(propData);
			}
		}
	}
	*/
	
	public static void checkPropDataType(PropData propData) {
		if ( propData != null && propData.isWeapon() ) {
			HashMap enhanceMap = propData.getEnhanceMap();
			for (Iterator iter = propData.getEnhanceMap().keySet().iterator(); iter.hasNext();) {
				Object typeObj = (Object) iter.next();
				PropDataEnhanceType type = null;
				if ( typeObj instanceof String ) {
					type = PropDataEnhanceType.valueOf(typeObj.toString());
					Map fieldMap = (Map)enhanceMap.get(typeObj);
					for (Iterator iterator = fieldMap.keySet().iterator(); iterator.hasNext();) {
						Object fieldObj = (Object) iterator.next();
						PropDataEnhanceField field = null;
						if ( fieldObj instanceof String ) {
							field = PropDataEnhanceField.valueOf(fieldObj.toString());
							Object fieldValue = fieldMap.get(fieldObj);
							fieldMap.put(field, fieldValue);
							fieldMap.remove(fieldObj);
						}
					}
					enhanceMap.put(type, fieldMap);
					enhanceMap.remove(typeObj);
				}
			}
		}
	}
	
	/**
	 * 2013-2-27更新后，导致武器有强化等级但是未增加数值，需要修复这个问题。
	 * 
	 * @param propData
	 */
	public static void checkPropDataEmptyStrength(PropData propData) {
		/**
		 * 有强化等级但是没有加上数值
		 */
		if ( propData != null && propData.isWeapon() && 
				propData.getLevel()> 0 && propData.getEnhanceMap().size() == 0 ) {
			WeaponPojo weapon = EquipManager.getInstance().getWeaponById(propData.getItemId());
			PropData newPropData = weapon.toPropData(propData.getPropIndate(), propData.getWeaponColor());
			newPropData.setPropUsedTime(propData.getPropUsedTime());
			EquipCalculator.weaponUpLevel(newPropData, propData.getLevel());
			newPropData.setPew(propData.getPew());
			
			int attack = propData.getAttackLev() - newPropData.getAttackLev();
			int defend = propData.getDefendLev() - newPropData.getDefendLev();
			int agility = propData.getAgilityLev() - newPropData.getAgilityLev();
			int lucky = propData.getLuckLev() - newPropData.getLuckLev();
			if ( attack > 0 ) {
				newPropData.setAttackLev(propData.getAttackLev());
				newPropData.setEnhanceValue(PropDataEnhanceType.FORGE, PropDataEnhanceField.ATTACK, attack);
			}
			if ( defend > 0 ) {
				newPropData.setDefendLev(propData.getDefendLev());
				newPropData.setEnhanceValue(PropDataEnhanceType.FORGE, PropDataEnhanceField.DEFEND, defend);
			}
			if ( agility > 0 ) {
				newPropData.setAgilityLev(propData.getAgilityLev());
				newPropData.setEnhanceValue(PropDataEnhanceType.FORGE, PropDataEnhanceField.AGILITY, agility);
			}
			if ( lucky > 0 ) {
				newPropData.setLuckLev(propData.getLuckLev());
				newPropData.setEnhanceValue(PropDataEnhanceType.FORGE, PropDataEnhanceField.LUCKY, lucky);
			}				
			int power = (int)EquipCalculator.calculateWeaponPower(newPropData.getAttackLev(), newPropData.getDefendLev(), 
					newPropData.getAgilityLev(), newPropData.getLuckLev(), newPropData.getBloodLev(), 
					newPropData.getSkinLev(), weapon.getRadius(), newPropData.getBloodPercent());
			newPropData.setPower(power);
			
			propData.copyFrom(newPropData); 
		}
	}

	public static void checkPropData(PropData propData) {
		/**
		 * 我调整过武器的数值，所以在登陆时需要将旧武器的数值修改为新的数值
		 * wangqi 2013-2-6
		 */
		if ( propData != null && propData.isWeapon() ) {
			WeaponPojo weapon = EquipManager.getInstance().getWeaponById(propData.getItemId());
			PropData newPropData = weapon.toPropData(propData.getPropIndate(), propData.getWeaponColor());
			newPropData.setPropUsedTime(propData.getPropUsedTime());
			EquipCalculator.weaponUpLevel(newPropData, propData.getLevel());
			if ( newPropData.getBaseAttack() != propData.getBaseAttack() ||
					newPropData.getBaseDefend() != propData.getBaseDefend() ||
					newPropData.getBaseAgility() != propData.getBaseAgility() ||
					newPropData.getBaseLuck() != propData.getBaseLuck() ) {
				
				newPropData.setPew(propData.getPew());
				HashMap enhanceMap = newPropData.getEnhanceMap();
				for (Iterator iter = propData.getEnhanceMap().keySet().iterator(); iter.hasNext();) {
					PropDataEnhanceType type = (PropDataEnhanceType) iter.next();
					if ( type == PropDataEnhanceType.STRENGTH ) continue;
					HashMap omap = (HashMap)propData.getEnhanceMap().get(type);
					if ( omap != null ) {
						HashMap map = new HashMap();
						enhanceMap.put(type, map);
						for (Iterator iterator = omap.keySet().iterator(); iterator.hasNext();) {
							PropDataEnhanceField field = (PropDataEnhanceField) iterator.next();
							Integer enhanceValue = (Integer)omap.get(field);
							float ratio = 0.0f;
							int newBaseValue = 0;
							if ( field == PropDataEnhanceField.ATTACK ) {
								ratio = enhanceValue.intValue() * 1.0f / propData.getBaseAttack();
								newBaseValue = newPropData.getBaseAttack();
								int newValue = 0;
								if ( ratio > 0.0f ) {
									newValue = Math.round(ratio * newBaseValue);
									map.put(field, newValue);
								}
								newPropData.setAttackLev(newPropData.getAttackLev()+newValue);
							} else if ( field == PropDataEnhanceField.DEFEND ) {
								ratio = enhanceValue.intValue() * 1.0f / propData.getBaseDefend();
								newBaseValue = newPropData.getBaseDefend();
								int newValue = 0;
								if ( ratio > 0.0f ) {
									newValue = Math.round(ratio * newBaseValue);
									map.put(field, newValue);
								}
								newPropData.setDefendLev(newPropData.getDefendLev()+newValue);
							} else if ( field == PropDataEnhanceField.LUCKY ) {
								ratio = enhanceValue.intValue() * 1.0f / propData.getBaseLuck();
								newBaseValue = newPropData.getBaseLuck();
								int newValue = 0;
								if ( ratio > 0.0f ) {
									newValue = Math.round(ratio * newBaseValue);
									map.put(field, newValue);
								}
								newPropData.setLuckLev(newPropData.getLuckLev()+newValue);
							} else if ( field == PropDataEnhanceField.AGILITY ) {
								ratio = enhanceValue.intValue() * 1.0f / propData.getBaseAgility();
								newBaseValue = newPropData.getBaseAgility();
								int newValue = 0;
								if ( ratio > 0.0f ) {
									newValue = Math.round(ratio * newBaseValue);
									map.put(field, newValue);
								}
								newPropData.setAgilityLev(newPropData.getAgilityLev()+newValue);
							}
						}
					}
				}
				int power = (int)EquipCalculator.calculateWeaponPower(newPropData.getAttackLev(), newPropData.getDefendLev(), 
						newPropData.getAgilityLev(), newPropData.getLuckLev(), newPropData.getBloodLev(), 
						newPropData.getSkinLev(), weapon.getRadius(), newPropData.getBloodPercent());
				newPropData.setPower(power);
				
				propData.copyFrom(newPropData);
			} 
		}
	}
	
	/**
	 * 我更改了武器的插槽格式，所以需要将1.8版本的PropData转换
	 * @param propData
	 */
	public static void convertOldPropDataToSlot(PropData propData) {
		HashMap enhanceMap = propData.getEnhanceMap();
		boolean isOldPropData = false;
		for (Iterator iter = propData.getEnhanceMap().keySet().iterator(); iter.hasNext();) {
			PropDataEnhanceType type = (PropDataEnhanceType) iter.next();
			if ( type == PropDataEnhanceType.FORGE ) {
				isOldPropData = true;
				HashMap omap = (HashMap)propData.getEnhanceMap().get(type);
				if ( omap != null ) {
					for (Iterator iterator = omap.keySet().iterator(); iterator.hasNext();) {
						PropDataEnhanceType.Field field = (PropDataEnhanceType.Field) iterator.next();
						Integer value = (Integer)omap.get(field);
						PropDataSlot slot = new PropDataSlot();
						slot.setSlotType(field.toField());
						ItemPojo itemPojo = getStoneItemPojo(propData, value.intValue(), field.toField());
						slot.setStoneId(itemPojo.getId());
						slot.setStoneLevel(itemPojo.getLevel());
						slot.setValue(value.intValue());
						propData.addNewSlot(slot);
					}
				}
			}
		}
		if ( isOldPropData ) {
			int slotCount = propData.getSlotCurrentCount();
			if ( slotCount < 4 ) {
				for ( int i=slotCount; i<4; i++) {
					PropDataSlot slot = new PropDataSlot();
					slot.addAvailableTypes(PropDataEnhanceField.ATTACK);
					slot.addAvailableTypes(PropDataEnhanceField.DEFEND);
					slot.addAvailableTypes(PropDataEnhanceField.AGILITY);
					slot.addAvailableTypes(PropDataEnhanceField.LUCKY);
					propData.addNewSlot(slot);
				}
			}
		}
		propData.getEnhanceMap().remove(PropDataEnhanceType.FORGE);
	}
	
	private static ItemPojo getStoneItemPojo(PropData propData, int newValue, PropDataEnhanceField field) {
		double oldValue = 0;
		int typeId = 0;
		if ( field == PropDataEnhanceField.ATTACK ) {
			oldValue = propData.getBaseAttack();
			typeId = 20016;
		} else if ( field == PropDataEnhanceField.DEFEND ) {
			oldValue = propData.getBaseDefend();
			typeId = 20006;
		} else if ( field == PropDataEnhanceField.AGILITY ) {
			oldValue = propData.getBaseAgility();
			typeId = 20011;
		} else if ( field == PropDataEnhanceField.LUCKY ) {
			oldValue = propData.getBaseLuck();
			typeId = 20001;
		}
		double ratio = newValue / oldValue;
		String itemId = null;
		if ( ratio >= 3.0 ) {
			int level = 5;
			itemId = String.valueOf(typeId+level);
		} else if ( ratio >= 2.0 ) {
			int level = 4;
			itemId = String.valueOf(typeId+level);
		} else if ( ratio >= 1.0 ) {
			int level = 3;
			itemId = String.valueOf(typeId+level);
		} else if ( ratio >= 0.5 ) {
			int level = 2;
			itemId = String.valueOf(typeId+level);
		} else {
			int level = 1;
			itemId = String.valueOf(typeId+level);
		}
		return ItemManager.getInstance().getItemById(itemId);
	}
}
