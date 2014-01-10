package com.xinqihd.sns.gameserver.script.function;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import script.guild.UserCreditCheck;

import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.RewardLevelPojo;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.ActivityManager;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.LevelManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEnhanceField;
import com.xinqihd.sns.gameserver.entity.user.PropDataEnhanceType;
import com.xinqihd.sns.gameserver.entity.user.PropDataSlot;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.Guild;
import com.xinqihd.sns.gameserver.guild.GuildCraftType;
import com.xinqihd.sns.gameserver.guild.GuildFacility;
import com.xinqihd.sns.gameserver.guild.GuildFacilityType;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.MathUtil;

/**
 * The collection of functions related to calculate equipment's and item's properties. 
 * @author wangqi
 *
 */
public class EquipCalculator {
	
	private static final Pattern s3serverPattern = Pattern.compile("s000[34]");
	
	/**
	 * 
	 * @param attack
	 * @param defend
	 * @param agility
	 * @param luck
	 * @param blood
	 * @param skin
	 * @return
	 */
	public static final double calculateWeaponPower(int attack, int defend, int agility, int luck, int blood, int skin) {
		return calculateWeaponPower(attack, defend, agility, luck, blood, skin, 0, 0);
	}
	
	/**
	 * 计算武器的标准战斗力
	 * 
	 * @param weapon
	 * @return
	 */
	public static final double calculateWeaponPower(WeaponPojo weapon) {
	  // 战斗力
		return calculateWeaponPower(weapon.getAddAttack(), weapon.getAddDefend(), 
				weapon.getAddAgility(), weapon.getAddLuck(), weapon.getAddBlood(), 
				weapon.getAddSkin(), weapon.getRadius(), weapon.getAddBloodPercent());
	}
	
	/**
	 * 
	 * @param user
	 * @return
	 */
	public static final double calculateWeaponPower(int attack, int defend, int agility, int luck, 
			int blood, int skin, int radius, int bloodPercent) {
	  // 战斗力
		double attackIndex = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.GAME_ATTACK_INDEX, 1.2);
		double defendIndex = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.GAME_DEFEND_INDEX, 1.3);
		double skinIndex = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.GAME_SKIN_INDEX, 0.2);
	
		/*
			1.5	200	0.0075
			1.4	190	0.007368421
			1.3	180	0.007222222
			1.2	165	0.007272727
			1.1	150	0.007333333
		 */
		double dpr = (blood*0.5 + skin*skinIndex + attack * attackIndex + defend *defendIndex + agility * 0.8 + luck * 0.8);
		double power = dpr;
		
		float radiusRatio = radius/150.0f;
		power = power + (int)(power*radiusRatio*0.2);
		
		if ( bloodPercent > 0 ) {
			float bloodPerent = bloodPercent/100.0f;
			power = power + (int)(power*(1/bloodPerent));
		}

	  return Math.round(power);
	}
	
	/**
	 * 
   * 强化武器攻击力效果说明		
   * 1 如果强化等级低于5，强化效果为上一级的1.02倍，强化为5-10，效果为1.05被，超过10级，为1.1倍
   * 2 效果为累乘		
   * 3 只能强化伤害和护甲		
   * 		
   * 
   * 当"upgradeLevel>0"时，表示装备升级；
   * 当"upgradeLevel<0"时，表示装备降级
   * 
   * @parameter attack 原有的攻击力
   * @parameter strengthLevel 强化等级
	 * @return
	 */
	public static final int calculateStrengthAttack(int attack, int upgradeLevel) {
		//wangqi modified 2012-04-16

		double base = GameDataManager.getInstance().
				getGameDataAsDouble(GameDataKey.STRENGTH_BASE_RATIO, 1.055);
		double normal = GameDataManager.getInstance().
				getGameDataAsDouble(GameDataKey.STRENGTH_NORMAL_RATIO, 1.5);
		double advance = GameDataManager.getInstance().
				getGameDataAsDouble(GameDataKey.STRENGTH_ADVANCE_RATIO, 2.0);

		double strengthBaseRatio = base;
		double strengthUpgradeRatio = 1.0;
		int absUpgradeLevel = Math.abs(upgradeLevel);
		for ( int i=0; i<absUpgradeLevel; i++ ) {
			if ( i <= 5 ) {
				strengthUpgradeRatio *= base;
			} else if ( i > 5 && i <= 10 ) {
				strengthUpgradeRatio *= normal;
			} else if ( i > 10 ) {
				strengthUpgradeRatio *= advance;
			}
		}
		int newAttack = 0;
		if ( upgradeLevel < 0 ) {
			strengthUpgradeRatio = Math.pow(1/strengthBaseRatio, absUpgradeLevel);
			newAttack = (int)Math.round(attack * strengthUpgradeRatio);
			if ( newAttack == attack ) {
				newAttack--;
			}
		} else {
			strengthUpgradeRatio = Math.pow(strengthBaseRatio, absUpgradeLevel);
			newAttack = (int)Math.round(attack * strengthUpgradeRatio);
			if ( newAttack == attack ) {
				newAttack++;
			}
		}
		
		return newAttack;
	}
	
	/**
   * 强化武器攻击力效果说明		
   * 1 强化效果为上一级的1.1倍		
   * 2 效果为累乘		
   * 
	 * @param attack
	 * @param strengthLevel
	 * @return
	 */
	public static final int calculateStrengthDefend(int skin, int upgradeLevel) {
		double base = GameDataManager.getInstance().
				getGameDataAsDouble(GameDataKey.STRENGTH_BASE_RATIO, 1.055);
		double normal = GameDataManager.getInstance().
				getGameDataAsDouble(GameDataKey.STRENGTH_NORMAL_RATIO, 1.065);
		double advance = GameDataManager.getInstance().
				getGameDataAsDouble(GameDataKey.STRENGTH_ADVANCE_RATIO, 1.2);

		double strengthBaseRatio = base;
		double strengthUpgradeRatio = 1.0;
		int absUpgradeLevel = Math.abs(upgradeLevel);
		for ( int i=0; i<absUpgradeLevel; i++ ) {
			if ( i <= 5 ) {
				strengthUpgradeRatio *= base;
			} else if ( i > 5 && i <= 10 ) {
				strengthUpgradeRatio *= normal;
			} else if ( i > 10 ) {
				strengthUpgradeRatio *= advance;
			}
		}
		//防御力要折算为DPR的1.5倍
		int newSkin = 0; 
		if ( upgradeLevel < 0 ) {
			strengthUpgradeRatio = Math.pow(1/strengthBaseRatio, absUpgradeLevel);
			newSkin = (int)Math.round(skin * strengthUpgradeRatio);
			if ( newSkin == skin ) {
				newSkin--;
			}
		} else {
			strengthUpgradeRatio = Math.pow(strengthBaseRatio, absUpgradeLevel);
			newSkin = (int)Math.round(skin * strengthUpgradeRatio);
			if ( newSkin == skin ) {
				newSkin++;
			}
		}
		return newSkin;
	}
	
	/**
   * 熔炼			
   * 					
   * 举例，一个戒指，基本属性为			
   * 			攻击+10		
   * 			防御+5		
   * 			敏捷+5		
   * 			幸运+5		
   * 则					
   * 熔炼次数	攻击	防御	敏捷	幸运	数量
   * 	10	5	5	5	
   * 	1	14	7	7	7	4
   * 	2	20	10	10	10	16
   * 	3	28	14	14	14	64
   * 	4	39	20	20	20	256
   * 	5	55	28	28	28	1024
	 *
	 * @param jeweryLevel
	 * @param upgradeLevel
	 * @return
	 */
	/*
	public static final PropData calculateStrengthJewery(PropData propData, int jeweryLevel, int upgradeLevel) {
		if ( propData == null ) {
			return propData;
		}
		PropData newPropData = propData.clone();
		if ( upgradeLevel <= jeweryLevel ) {
			return newPropData;
		}
		int diff = upgradeLevel - jeweryLevel;
		double strengthBaseRatio = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.STRENGTH_BASE_RATIO, 1.1);
		double ratio = Math.pow(strengthBaseRatio, diff);
	  //攻击合成等级
	  newPropData.setAttackLev((int)(newPropData.getAttackLev()*ratio));
	  //防御合成等级
	  newPropData.setDefendLev((int)(newPropData.getDefendLev()*ratio));
	  //敏捷合成等级
	  newPropData.setAgilityLev((int)(newPropData.getAgilityLev()*ratio));
	  //幸运合成等级
	  newPropData.setLuckLev((int)(newPropData.getLuckLev()*ratio));
	  
		return newPropData;
	}
	*/
	
	/**
   * 强化几率说明：
   * 
   * 1 强化石等级分为1,2,3,4级
   * 2 强化等级分为1~9共9级
   * 3 强化等级为目标等级
   * 4 可以混合放置不同等级的强化石
   * 5 最多能放3个强化石
   * 6 强化成功则装备的强化等级+1，失败则强化等级-1
   * 7 放置 神恩符 的时候，失败则强化等级不变
   * 8 目前只有 武器，帽子，衣服可以强化
   * 9 还有 幸运符 可以提高强化几率
   * 10 幸运符分为15%及25%两种
   * 11 只能放置一张幸运符
   * 12 装备强化到一定等级会有孔开启（以后会做，预留）
   * 
   * 幸运符的算法为：
   * 成功几率=（第一个强化石的几率+第二个强化石的几率+第三个强化石的几率）*k
   * 15%的幸运符，k值为1.15
   * 25%的幸运符，k值为1.25
   * 
   * 举例
   * 一件武器目前为+3，需要强化到+4
   * 放置了1个强化石2级，1个强化石3级，1个强化石4级。再放置了一个25%的幸运符
   * 则几率=（1.56%+6.25%+25%）*1.25=41.0%
   * 
   * ********** 伪随机 ******************
   * 可以使用tryTimes做伪随机。玩家对随机数经常有一种错觉，认为10%的成功率等同于玩10次成功一次，
   * 如果没有成功就会怀疑系统有问题。所以使用tryTimes跟踪用户尝试的次数，如果次数>=概率的倒数，
   * 则认为玩家成功
   * 
	 * @return
	 */
	public static final boolean forgeEquip(User user, double[] stoneSuccessRatio, 
			double luckyCardSuccessRatio, int tryTimes) {
		
		double successRatio = calculateStrengthWithLuckyStoneRatio(user, 
				stoneSuccessRatio, luckyCardSuccessRatio);
		boolean success = MathUtil.nextDouble() < successRatio;
		if ( !success ) {
			if ( tryTimes > 1/successRatio ) {
				return true;
			}
		}
		return success;
	}
	
	/**
	 * Strengthen the Weapon to the next level. 
	 * The attack and defend ability will be promoted.
	 * @return
	 */
	public static PropData weaponUpLevel(PropData equipPropData, int newLevel) {
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(equipPropData.getItemId());
		/**
		 * 防止装备强化等级超过最大上限
		 */
		if ( newLevel > equipPropData.getMaxLevel() ) {
			newLevel = equipPropData.getMaxLevel();
		}
		if ( weapon != null ) {
			if ( newLevel == 0 ) {
				//合成产生的攻击值和伤害值
				int forgeAttack = equipPropData.getSlotTotalValue(PropDataEnhanceField.ATTACK);
				int forgeDefend = equipPropData.getSlotTotalValue(PropDataEnhanceField.DEFEND);
				equipPropData.setAttackLev(equipPropData.getBaseAttack()+forgeAttack);
				equipPropData.setDefendLev(equipPropData.getBaseDefend()+forgeDefend);
				equipPropData.setDamageLev(weapon.getAddDamage());
				equipPropData.setSkinLev(weapon.getAddSkin());
				equipPropData.setLevel(newLevel);
				equipPropData.getEnhanceMap().remove(PropDataEnhanceType.STRENGTH);
				equipPropData.setLevel(newLevel);
				//equipPropData.setPower(equipPropData.getBasePower());
				int power = (int)calculateWeaponPower(equipPropData.getAttackLev(), equipPropData.getDefendLev(), equipPropData.getAgilityLev(), 
						equipPropData.getLuckLev(), equipPropData.getBloodLev(), equipPropData.getSkinLev(), 
						weapon.getRadius(), equipPropData.getBloodPercent());
				equipPropData.setPower(power);
			} else {
				int levelDiff = newLevel - equipPropData.getLevel();
				if ( levelDiff != 0 ) {
					if ( weapon.getSlot() == EquipType.WEAPON ) {
						//合成产生的攻击值
						int forgeAttack = equipPropData.getSlotTotalValue(PropDataEnhanceField.ATTACK);
						int baseAttack = equipPropData.getBaseAttack();
						int newAttack = (int)EquipCalculator.calculateStrengthAttack(baseAttack, newLevel);
					  //总值=基础值+强化值+合成值
						equipPropData.setAttackLev(newAttack+forgeAttack);
						//Calculate damage
						int newDamage = (int)EquipCalculator.calculateWeaponPower(newAttack, 0, 0, 0, 0, 0);
						int oldDamage = weapon.getAddDamage();
						if ( newDamage == oldDamage ) {
							if ( levelDiff>0 ) {
								newDamage = oldDamage + 1;
							} else {
								newDamage = oldDamage - 1;
							}
						}
						equipPropData.setDamageLev(newDamage);
						
						equipPropData.setEnhanceValue(
								PropDataEnhanceType.STRENGTH, PropDataEnhanceField.DAMAGE, Math.abs(newDamage-oldDamage));
					} else {
						double defendRatio = GameDataManager.getInstance().
								getGameDataAsDouble(GameDataKey.STRENGTH_DEFEND_RATIO, 1.5);
						int forgeDefend = equipPropData.getSlotTotalValue(PropDataEnhanceField.DEFEND);
						int baseDefend = equipPropData.getBaseDefend();
						int newDefend = (int)(EquipCalculator.calculateStrengthDefend(baseDefend, newLevel)); // * defendRatio);
					  //总值=基础值+强化值+合成值
						equipPropData.setDefendLev(newDefend+forgeDefend);
						//Calculate skin
						int newSkin = (int)EquipCalculator.calculateWeaponPower(0, newDefend, 0, 0, 0, weapon.getAddSkin());
						int oldSkin = weapon.getAddSkin();
						if ( newSkin == oldSkin ) {
							if ( levelDiff>0 ) {
								newSkin = oldSkin + 1;
							} else {
								newSkin = oldSkin - 1;
							}
						}
						//2012-6-13
						equipPropData.setSkinLev(newSkin);
						
						equipPropData.setEnhanceValue(
								PropDataEnhanceType.STRENGTH, PropDataEnhanceField.SKIN, Math.abs(newSkin-oldSkin));
					}
				}
				equipPropData.setLevel(newLevel);
				equipPropData.setPower(weapon.getPower());
				int totalPower = (int)(calculateWeaponPower(equipPropData.getAttackLev(), equipPropData.getDefendLev(), 
						equipPropData.getAgilityLev(), equipPropData.getLuckLev(), equipPropData.getBloodLev(), 
						equipPropData.getSkinLev(), weapon.getRadius(), weapon.getAddBloodPercent()));
				equipPropData.setPower(totalPower);
			}
		}
		return equipPropData;
	}
	
	/**
	 * 计算合成石合成更高等级时是否成功。
	 * 
	 * 与强化计算方法相同，这里也使用了伪随机方法，当尝试合成次数超过概率的倒数时，认为强化成功
	 * 
	 * @param stoneLevels
	 * @param stoneSuccessRatio
	 * @param tryTimes 
	 * @return
	 */
	public static final boolean composeItem(int stoneLevel, 
			double stoneSuccessRatio, int tryTimes) {
		
		boolean success = MathUtil.nextDouble() < stoneSuccessRatio;
		if ( !success ) {
			if ( tryTimes > 1/stoneSuccessRatio ) {
				return true;
			}
		}
		return success;
	}
	
	/**
	 * 在新的系统中，合成石每次合成都是成功的，但是效果会在一个正态分布的区间中分布
	 * 
	 * 计算合成石合成效果的函数。
	 * 合成的效果使用正态分布概率函数计算，u=0.0, q=0.0-unlimited，推荐q=1.0
	 * 每种合成石等级有自己的数据范围.
	 * 
	 * @param userLevel
	 * @param stoneLevel
	 * @param stoneTypeId ItemManager类中的attack, defend, luck和agility石头类型ID
	 * @param luckyCardSuccessRatio
	 * @return
	 */
	public static final double calculateForgeData(PropData equipPropData, 
			int stoneLevel, String stoneTypeId) {
		PropDataEnhanceField field = null;
		if ( ItemManager.attackStoneId.equals(stoneTypeId) ) {
			field = PropDataEnhanceField.ATTACK;
		} else if ( ItemManager.defendStoneId.equals(stoneTypeId) ) {
			field = PropDataEnhanceField.DEFEND;
		} else if ( ItemManager.agilityStoneId.equals(stoneTypeId) ) {
			field = PropDataEnhanceField.AGILITY;
		} else if ( ItemManager.luckStoneId.equals(stoneTypeId) ) {
			field = PropDataEnhanceField.LUCKY;
		}
		PropDataSlot slot = new PropDataSlot();
		slot.setSlotType(field);
		return calculateForgeData(equipPropData, stoneLevel, stoneTypeId, slot);
	}
	
	/**
	 * 在新的系统中，合成石每次合成都是成功的，但是效果会在一个正态分布的区间中分布
	 * 
	 * 计算合成石合成效果的函数。
	 * 合成的效果使用正态分布概率函数计算，u=0.0, q=0.0-unlimited，推荐q=1.0
	 * 每种合成石等级有自己的数据范围.
	 * 
	 * @param userLevel
	 * @param stoneLevel
	 * @param stoneTypeId ItemManager类中的attack, defend, luck和agility石头类型ID
	 * @param luckyCardSuccessRatio
	 * @return
	 */
	public static final double calculateForgeData(PropData equipPropData, 
			int stoneLevel, String stoneTypeId, PropDataSlot slot) {
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(equipPropData.getItemId());
		if ( weapon == null ) return 0;
		
		//double[] array = GameDataManager.getInstance().getGameDataAsDoubleArray(GameDataKey.FORGE_STONE_RANGE);
		/**
		 * Disable the lucky stone value
		 * wangqi 2013-03-04
		 */
		/*
		double luckyTimes = GameDataManager.getInstance().getGameDataAsDouble(
				GameDataKey.FORGE_LUCKY_TIMES, 6.0);
		*/
		double[] rangeArray = GameDataManager.getInstance().getGameDataAsDoubleArray(
				GameDataKey.FORGE_SIGMA_RATIO);

		int stoneIndex = stoneLevel - 1;

		double u = 0.0;
		double q = 1.0; // + ( luckyTimes * luckyCardSuccessRatio );
		int min = 0;
		int max = 0;
		int finalData = 0;
		double startRange = 0;
		if ( stoneIndex > 0 ) {
			startRange = rangeArray[stoneIndex-1];
		}
		double endRange = rangeArray[stoneIndex];
		if ( ItemManager.attackStoneId.equals(stoneTypeId) ) {
			min = (int)Math.round(equipPropData.getBaseAttack()* startRange);
			max = (int)Math.round(equipPropData.getBaseAttack()* endRange);
			if ( min == 0 ) min = 1;
			if ( max == 0 ) max = 10 + equipPropData.getUserLevel()*2;
			//如果之前做过火神石合成的话，减去这个合成的数值
			int oldAttack = equipPropData.getAttackLev() -  
					equipPropData.getSlotTotalValue(PropDataEnhanceField.ATTACK);
			finalData = (int)Math.round(MathUtil.nextGaussionInt(min, max, q));
			//加上新的合成数值
			slot.setValue(finalData);
			slot.setStoneLevel(stoneLevel);
			slot.setSlotType(PropDataEnhanceField.ATTACK);
			slot.setStoneId(stoneTypeId);
			equipPropData.setAttackLev(oldAttack+
					equipPropData.getSlotTotalValue(PropDataEnhanceField.ATTACK));
			//equipPropData.setEnhanceValue(slot, PropDataEnhanceField.ATTACK, finalData);
			
		} else if ( ItemManager.defendStoneId.equals(stoneTypeId) ) {
			min = (int)Math.round(equipPropData.getBaseDefend()* startRange);
			max = (int)Math.round(equipPropData.getBaseDefend()* endRange);
			if ( min == 0 ) min = 1;
			if ( max == 0 ) max = 10 + equipPropData.getUserLevel()*2;
			int oldDefend = equipPropData.getDefendLev() - 
					equipPropData.getSlotTotalValue(PropDataEnhanceField.DEFEND);
			finalData = Math.round(MathUtil.nextGaussionInt(min, max, q));
			
			slot.setValue(finalData);
			slot.setStoneLevel(stoneLevel);
			slot.setSlotType(PropDataEnhanceField.DEFEND);
			slot.setStoneId(stoneTypeId);
			//equipPropData.setEnhanceValue(slot, PropDataEnhanceField.DEFEND, finalData);
			equipPropData.setDefendLev(oldDefend + 
					equipPropData.getSlotTotalValue(PropDataEnhanceField.DEFEND));
			
		} else if ( ItemManager.agilityStoneId.equals(stoneTypeId) ) {
			min = (int)Math.round(equipPropData.getBaseAgility()* startRange);
			max = (int)Math.round(equipPropData.getBaseAgility()* endRange);
			if ( min == 0 ) min = 1;
			if ( max == 0 ) max = 10 + equipPropData.getUserLevel()*2;
			finalData = Math.round(MathUtil.nextGaussionInt(min, max, q));
			int oldValue = equipPropData.getAgilityLev() - 
					equipPropData.getSlotTotalValue(PropDataEnhanceField.AGILITY);
			equipPropData.setAgilityLev(oldValue+
					equipPropData.getSlotTotalValue(PropDataEnhanceField.AGILITY));
			slot.setValue(finalData);
			slot.setStoneLevel(stoneLevel);
			slot.setSlotType(PropDataEnhanceField.AGILITY);
			slot.setStoneId(stoneTypeId);
			equipPropData.setAgilityLev(oldValue+
					equipPropData.getSlotTotalValue(PropDataEnhanceField.AGILITY));
			//equipPropData.setEnhanceValue(slot, PropDataEnhanceField.AGILITY, finalData);
			
		} else if ( ItemManager.luckStoneId.equals(stoneTypeId) ) {
			min = (int)Math.round(equipPropData.getBaseLuck()* startRange);
			max = (int)Math.round(equipPropData.getBaseLuck()* endRange);
			if ( min == 0 ) min = 1;
			if ( max == 0 ) max = 10 + equipPropData.getUserLevel()*2;
			int oldValue = equipPropData.getLuckLev() - 
					equipPropData.getSlotTotalValue(PropDataEnhanceField.LUCKY);
			finalData = Math.round(MathUtil.nextGaussionInt(min, max, q));

			slot.setValue(finalData);
			slot.setStoneLevel(stoneLevel);
			slot.setSlotType(PropDataEnhanceField.LUCKY);
			slot.setStoneId(stoneTypeId);
			equipPropData.setLuckLev(oldValue+
					equipPropData.getSlotTotalValue(PropDataEnhanceField.LUCKY));
			//equipPropData.setEnhanceValue(slot, PropDataEnhanceField.LUCKY, finalData);
		}
		if ( finalData != 0 ) {
			calculatePropDataPower(equipPropData);
		}
		
		return finalData;
	}

	/**
	 * @param equipPropData
	 * @param weapon
	 * @param finalData
	 */
	public static void calculatePropDataPower(PropData equipPropData) {
		if ( equipPropData != null && equipPropData.isWeapon() ) {
			WeaponPojo weapon = (WeaponPojo)equipPropData.getPojo();
			int damageValue = equipPropData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.DAMAGE);
			int skinValue = equipPropData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.SKIN);
			/*
			int attackValue = equipPropData.getEnhanceValue(PropDataEnhanceType.FORGE, PropDataEnhanceField.ATTACK);
			int defendValue = equipPropData.getEnhanceValue(PropDataEnhanceType.FORGE, PropDataEnhanceField.DEFEND);
			int luckyValue = equipPropData.getEnhanceValue(PropDataEnhanceType.FORGE, PropDataEnhanceField.LUCKY);
			int agilityValue = equipPropData.getEnhanceValue(PropDataEnhanceType.FORGE, PropDataEnhanceField.AGILITY);
			
			int totalPower = (int)(calculateWeaponPower(equipPropData.getAttackLev(), equipPropData.getDefendLev(), 
			equipPropData.getAgilityLev()+agilityValue, equipPropData.getLuckLev()+luckyValue, equipPropData.getBloodLev(), equipPropData.getSkinLev(), 
			weapon.getRadius(), weapon.getAddBloodPercent()));
			*/
			int totalPower = (int)(calculateWeaponPower(equipPropData.getAttackLev(), equipPropData.getDefendLev(), 
					equipPropData.getAgilityLev(), equipPropData.getLuckLev(), 
					equipPropData.getBloodLev(), equipPropData.getSkinLev(), 
					weapon.getRadius(), weapon.getAddBloodPercent()));
			equipPropData.setPower(totalPower);
		}
	}
	
	/**
	 * 计算强化石强化武器和装备的成功概率
	 * 
   * 下表是几率表					
   * 目标强化等级	强化石1级	强化石2级	强化石3级	强化石4级	强化石5级
   * 1	25.00%	100.00%	100.00%	100.00%	100.00%
   * 2	6.25%		25.00%	100.00%	100.00%	100.00%
   * 3	1.56%		6.25%		25.00%	100.00%	100.00%
   * 4	0.39%		1.56%		6.25%		25.00%	100.00%
   * 5	0.20%		0.78%		3.13%		12.50%	50.00%
   * 6	0.10%		0.39%		1.56%		6.25%		25.00%
   * 7	0.05%		0.20%		0.78%		3.13%		12.50%
   * 8	0.02%		0.10%		0.39%		1.56%		6.25%
   * 9	0.01%		0.05%		0.20%		0.78%		3.13%
   * 10					1.56%
   * 11					0.78%
   * 12					0.39%
   * 
   * My revised version
   * 目标	强化等级	强化石1级		强化石2级		强化石3级		强化石4级		强化石5级
   * 1		45.15%	100.00%		100.00%		100.00%		100.00%
   * 2	31.83%	45.15%	100.00%	100.00%	100.00%
   * 3	15.83%	31.83%	45.15%	100.00%	100.00%
   * 4	5.55%		15.83%	31.83%	45.15%	100.00%
   * 5	1.37%		5.55%		15.83%	31.83%	45.15%
   * 6	0.24%		1.37%		5.55%		15.83%	31.83%
   * 7	0.03%		0.24%		1.37%		5.55%		15.83%
   * 8	0.00%		0.03%		0.24%		1.37%		5.55%
   * 9	0.00%		0.00%		0.03%		0.24%		1.37%
   * 10					0.00%		0.00%		0.03%		0.24%
   * 11					0.00%		0.00%		0.03%
   * 12					0.00%		0.00%
								0.00%
   * 
   * I plan to use Normal Distribution to calculate it.
   * 
	 * @param stoneLevel
	 * @param targetLevel
	 * @return
	 */
	public static final double calculateStrengthStoneSuccessRatio(
			int stoneLevel, int targetLevel) {
		double[][] stoneRatios = GameDataManager.getInstance().
				getGameDataAsDoubleArrayArray(GameDataKey.STRENGTH_STONE_RATIO);
		int stoneIndex = stoneLevel - 1;
		int index = targetLevel-1;
		if ( index >= stoneRatios[stoneIndex].length ) {
			//index = stoneRatios[stoneIndex].length - 1;
			return 0;
		}
		return stoneRatios[stoneIndex][index];
	}

	/**
	 * 计算强化石与幸运符一起强化时概率的变化
	 *  
	 * @param stoneSuccessRatio
	 * @param luckyCardSuccessRatio 0.15 or 0.25
	 * @return
	 */
	public static final double calculateStrengthWithLuckyStoneRatio(User user,
			double[] stoneSuccessRatio, double luckyCardSuccessRatio) {
		double successRatio = 0;
		for ( int i=0; i<stoneSuccessRatio.length; i++ ) {
			successRatio += stoneSuccessRatio[i];
		}
		if ( successRatio > 0 ) {
			/**
			 * Merge the trunk and beta-1_8_0_20121129 in the same code branch
			 * by just adding a if-check.
			 */
			/**
			 * Make the successRatio use multitimes
			 */
			if ( user.getServerId() != null && s3serverPattern.matcher(user.getServerId()).find() ) {
				successRatio *= (1+luckyCardSuccessRatio);
			} else {
				successRatio += luckyCardSuccessRatio;
			}
			//Check the strength activities
			float actStrRate = ActivityManager.getInstance().getActivityStrengthRate(user);
			successRatio += actStrRate;
		}
		return successRatio;
	}
		
	/**
	 * 计算强化石或者任意合成石，熔炼下一等级石头的概率
	 * @param stoneLevel
	 * @return
	 */
	public static final double calculateComposeItemSuccessRatio(int stoneCount, int stoneLevel) {
		/*
		int targetLevel = stoneLevel + 1;
		double[] stoneRatios = GameDataManager.getInstance().
				getGameDataAsDoubleArray(GameDataKey.FORGE_STONE_RATIO);
		int index = targetLevel;
		if ( targetLevel >= stoneRatios.length ) {
			index = stoneRatios.length - 1;
		}
		*/
		double baseRatio = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.COMPOSE_ITEM_BASE_RATIO, 0.4);
		double addRatio = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.COMPOSE_ITEM_ADD_RATIO, 0.15);
		addRatio -= 0.05*stoneLevel;
		if ( addRatio < 0 ) addRatio = 0;
		double finalRatio = baseRatio + stoneCount*addRatio;
		if ( finalRatio > 1.0 ) finalRatio = 1.0;
		return finalRatio;
	}
	
	/**
	 *
	 * 强化等级的稀有程度
				8: 17.22%
				9: 16.74%
				10: 15.38%
				11: 12.76%
				12: 12.08%
				13: 10.14%
				14: 8.39%
				15: 7.29%
	 */
	private static double[] strLevelRatio = new double[]{1.0, 0.1722, 0.1674, 0.1538, 0.1276, 0.1208, 0.1014, 0.0839, 0.0729};
	/**
	 * 合成插槽的稀有程度
			0: 24.41%
			1: 22.61%
			2: 18.36%
			3: 13.83%
			4: 10.31%
			5: 6.66%
			6: 3.82%
	 */
	private static double[] slotNumRatio = new double[]{0.2441, 0.2261, 0.1836, 0.1383, 0.1031, 0.0666, 0.0382};
	/**
	 * 合成插槽能容纳石头的数量
		1: 38.8%
		2: 32.73%
		3: 19.64%
		4: 8.83
	 */
	private static double[] slotTypeRatios = new double[]{0.388, 0.3273, 0.1964, 0.883};
	private static double[] colorRatio = new double[]{1.0, 0.8, 0.6, 0.4, 0.2, 0.05};
	private static double[] qualityRatio = new double[]{1.0, 0.5};
	/**
	 * 计算一件武器装备的稀有程度
	 * @return
	 */
	public static final double calculatePropDataRareRatio(PropData propData) {
		if ( !propData.isWeapon() ) {
			return 1.0;
		}
		int slotCount = propData.getTotalSlot();
		if ( slotCount >= slotNumRatio.length ) slotCount = slotNumRatio.length-1; 
		Collection<PropDataSlot> slots = propData.getSlots();
		double slotTypeRatio = 1.0;
		if ( slots != null ) {
			for (Iterator iter = slots.iterator(); iter.hasNext();) {
				PropDataSlot slot = (PropDataSlot) iter.next();
				int typeNumber = slot.getAvailabeTypes().size();
				if ( typeNumber > slotTypeRatios.length ) {
					typeNumber = slotTypeRatios.length;
				}
				if ( typeNumber == 0 ) typeNumber = 4;
				slotTypeRatio *= slotTypeRatios[typeNumber-1];
			}
		}
		int maxStrengthLevel = propData.getMaxLevel()-8;
		if ( maxStrengthLevel > strLevelRatio.length-1 ) maxStrengthLevel = strLevelRatio.length;
		if ( maxStrengthLevel<0 ) maxStrengthLevel=0;
		int colorIndex = propData.getWeaponColor().ordinal();
		if ( colorIndex >= colorRatio.length ) colorIndex=colorRatio.length-1;
		WeaponPojo weapon = (WeaponPojo)propData.getPojo();
		int quality = weapon.getQuality();
		RewardLevelPojo levelPojo = RewardManager.getInstance().getRewardLevelPojoByTypeId(propData.getItemId());
		double rareRate = 1.0;
		if ( levelPojo != null ) {
			rareRate = levelPojo.getRatio();
		}
		double finalRatio = strLevelRatio[maxStrengthLevel]*
				slotNumRatio[slotCount]*
				colorRatio[colorIndex]*
				qualityRatio[quality-1] * rareRate * slotTypeRatio;
		return finalRatio;
	}
		
	/**
	 * 计算颜色熔炼的成功率
	 * @param stoneLevel
	 * @return
	 */
	public static final double calculateComposeColorSuccessRatio(WeaponColor color, int stoneCount) {
		/*
		int targetLevel = stoneLevel + 1;
		double[] stoneRatios = GameDataManager.getInstance().
				getGameDataAsDoubleArray(GameDataKey.FORGE_STONE_RATIO);
		int index = targetLevel;
		if ( targetLevel >= stoneRatios.length ) {
			index = stoneRatios.length - 1;
		}
		*/
		double[] ratios  = GameDataManager.getInstance().getGameDataAsDoubleArray(GameDataKey.COMPOSE_WEAPON_COLOR_RATIO);
		int index = color.ordinal();
		if ( index >= 0 && index < WeaponColor.values().length ) {
			double finalRatio = ratios[index]*stoneCount;
			if ( finalRatio > 1.0 ) finalRatio = 1.0;
			return finalRatio;
		}
		return 0;
	}
	
	/**
	 * Calculate the final price according to user's level and current
	 * price unit for forging, composing and transferring.
	 * 
	 * @param userLevel
	 * @param priceUnit
	 * @return
	 */
	private static final int[][] PRICES_RATIO = new int[LevelManager.MAX_LEVEL+1][1];
	static {
		for ( int level=1; level<=LevelManager.MAX_LEVEL; level++ ) {
			PRICES_RATIO[level][0] = Math.round(0.02f * level * level);
			if ( PRICES_RATIO[level][0]<1 ) {
				PRICES_RATIO[level][0] = 1;
			}
		}
	}
	public static final int calculateCraftPrice(int userLevel, int priceUnit) {
		if ( userLevel <= 0 || userLevel >LevelManager.MAX_LEVEL ) {
			return priceUnit;
		}
		return priceUnit * PRICES_RATIO[userLevel][0];
	}
	
	private static final int[][] PRICES_BLOOD_RATIO = new int[LevelManager.MAX_LEVEL+1][1];
	static {
		for ( int level=1; level<=LevelManager.MAX_LEVEL; level++ ) {
			PRICES_BLOOD_RATIO[level][0] = Math.round(0.015f * level * level);
			if ( PRICES_BLOOD_RATIO[level][0]<1 ) {
				PRICES_BLOOD_RATIO[level][0] = 1;
			}
		}
	}
	public static final int calculateBloodPrice(int userLevel, int priceUnit) {
		if ( userLevel <= 0 || userLevel >LevelManager.MAX_LEVEL ) {
			return priceUnit;
		}
		return priceUnit * PRICES_BLOOD_RATIO[userLevel][0];
	}
}
