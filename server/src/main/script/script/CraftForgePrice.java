package script;

import java.util.ArrayList;
import java.util.regex.Pattern;

import script.guild.CraftRatio;

import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.MoneyType;
import com.xinqihd.sns.gameserver.config.Pojo;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.db.mongo.ActivityManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.ShopManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.forge.ForgeStatus;
import com.xinqihd.sns.gameserver.guild.GuildCraftType;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * The user want to compose lower level items to higher item. 
 * 
 */
public class CraftForgePrice {
	
	private static final Pattern s3serverPattern = Pattern.compile("s000[34]");

	/**
   * 计算强化几率和价格：
   * 
	 */
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		
		User user = (User)parameters[0];
		Object[] array = (Object[])parameters[1];
		PropData equipPropData  = (PropData)array[0];
		PropData[] stonePropDatas = (PropData[])array[1];
		
		int targetLevel = equipPropData.getLevel()+1;
		
		double luckyRatio = 0.0;
		double[] stoneRatios = new double[stonePropDatas.length];
		boolean useGodStone = false;
		int godStoneLevel = 0;
		//If the user want to strengthen an equipment
		boolean isDoingStrength = false;
		boolean isDoingLucky = false;
		boolean isDoingDefend = false;
		boolean isDoingAgility = false;
		boolean isDoingAttack = false;
		boolean isDoingDiamond = false;
		boolean isDoingCrystal = false;
		
		ForgeStatus forgeStatus = ForgeStatus.SUCCESS;
		
		int stoneLevel = 1;
		
		int totalStonePrice = 0;
		for ( int i=0; i<stonePropDatas.length; i++ ) {
			PropData propData = stonePropDatas[i];
			if ( propData != null ) {
				Pojo pojo =propData.getPojo();
				if ( pojo != null ) {
					if ( !(pojo instanceof ItemPojo) ) {
						continue;
					}
					ItemPojo itemPojo = (ItemPojo)pojo;
					if ( ItemManager.luckyStone15.equals(itemPojo.getId()) ) {
						luckyRatio = 0.15;
					} else if ( ItemManager.luckyStone25.equals(itemPojo.getId()) ) {
						luckyRatio = 0.25;
					} else if ( ItemManager.winStone.equals(itemPojo.getId()) ) {
						luckyRatio = Double.MAX_VALUE;
					} else if ( ItemManager.godStoneId.equals(itemPojo.getTypeId()) ) {
						useGodStone = true;
						godStoneLevel = itemPojo.getLevel();
					} else if ( ItemManager.strengthStoneId.equals(itemPojo.getTypeId()) ) {
						stoneRatios[i] = EquipCalculator.calculateStrengthStoneSuccessRatio(
								stonePropDatas[i].getLevel(), targetLevel);
						isDoingStrength = true;
						//Max strength stone level.
						if ( stoneLevel < stonePropDatas[i].getLevel() ) {
							stoneLevel = stonePropDatas[i].getLevel();
						}
						if ( stoneRatios[i] == 0 ) {
							String text = Text.text("strength.outrange", new Object[]{stoneLevel, stoneLevel+1});
							SysMessageManager.getInstance().sendClientInfoRawMessage(user.getSessionKey(), text, 3000);
							totalStonePrice = 0;
						} else {
							totalStonePrice += ShopManager.getInstance().findPriceForPropData(
								user, stonePropDatas[i], MoneyType.GOLDEN, null, null, false);
						}
					} else if ( ItemManager.luckStoneId.equals(itemPojo.getTypeId()) ) {
						isDoingLucky = true;
						stoneLevel = stonePropDatas[i].getLevel();
						totalStonePrice += ShopManager.getInstance().findPriceForPropData(
								user, stonePropDatas[i], MoneyType.GOLDEN, null, null, false);
					} else if ( ItemManager.defendStoneId.equals(itemPojo.getTypeId()) ) {
						isDoingDefend = true;
						stoneLevel = stonePropDatas[i].getLevel();
						totalStonePrice += ShopManager.getInstance().findPriceForPropData(
								user, stonePropDatas[i], MoneyType.GOLDEN, null, null, false);
					} else if ( ItemManager.agilityStoneId.equals(itemPojo.getTypeId()) ) {
						isDoingAgility = true;
						stoneLevel = stonePropDatas[i].getLevel();
						totalStonePrice += ShopManager.getInstance().findPriceForPropData(
								user, stonePropDatas[i], MoneyType.GOLDEN, null, null, false);
					} else if ( ItemManager.attackStoneId.equals(itemPojo.getTypeId()) ) {
						isDoingAttack = true;
						stoneLevel = stonePropDatas[i].getLevel();
						totalStonePrice += ShopManager.getInstance().findPriceForPropData(
								user, stonePropDatas[i], MoneyType.GOLDEN, null, null, false);
					} else if ( ItemManager.diamondStoneId.equals(itemPojo.getTypeId()) ) {
						isDoingDiamond = true;
						stoneLevel = stonePropDatas[i].getLevel();
					} else if ( ItemManager.crystalStoneId.equals(itemPojo.getTypeId()) ) {
						isDoingCrystal = true;
						stoneLevel = stonePropDatas[i].getLevel();
					}
				}
			}
		}

		double price = 0;
		double successRatio = 0;
		double guildAddRatio = 0;
		if ( isDoingStrength ) {
			boolean success = false;
			int maxLevel = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.STRENGTH_MAX_LEVEL, 15);
			int weaponMaxLevel = equipPropData.getMaxLevel();
			maxLevel = Math.min(weaponMaxLevel, maxLevel);
			if ( targetLevel > maxLevel ) {
				success = false;
			} else {
				success = true;
				if ( useGodStone ) {
					int[] godStoneRange = GameDataManager.getInstance().getGameDataAsIntArray(
							GameDataKey.STRENGTH_GODSTONE_RANGE);
					if ( targetLevel <= godStoneRange[godStoneLevel-1] ) {
						success = true;
					} else {
						success = false;
						String message = Text.text("strength.nogodstone", godStoneLevel, godStoneRange[godStoneLevel-1]);
						SysMessageManager.getInstance().sendClientInfoRawMessage(user.getSessionKey(), message, Action.NOOP, Type.NORMAL);
					}
				}
				if ( success ) {
					successRatio = calculateStrengthSuccessRatio(user, luckyRatio,
							stoneRatios);
					
					int equipPrice = ShopManager.getInstance().findPriceForPropData(user, equipPropData, MoneyType.GOLDEN, null, null, false);
					double unitPrice = (equipPrice + totalStonePrice) / 5.0;
					price = unitPrice;
					if ( useGodStone && targetLevel >= 5 ) {
						price += 0.2 * unitPrice;
					}
					if ( luckyRatio == 0.15 ) {
						price += 0.15 * unitPrice;
					}
					if ( luckyRatio == 0.25 ) {
						price += 0.4 * unitPrice;
					}
					if ( equipPropData.getWeaponColor() == WeaponColor.BLUE ) {
						price += 0.5 * unitPrice;
					} else if ( equipPropData.getWeaponColor() == WeaponColor.PINK ) {
						price += 1.5 * unitPrice;
						successRatio *= 0.8;
					} else if ( equipPropData.getWeaponColor() == WeaponColor.ORGANCE ) {
						price += 3 * unitPrice;
						successRatio *= 0.5;
					} else if ( equipPropData.getWeaponColor() == WeaponColor.PURPLE ) {
						price += 4 * unitPrice;
						successRatio *= 0.4;
					}
					price += 0.02 * targetLevel * targetLevel * unitPrice;
					//计算VIP加成
					if ( user != null && user.isVip() ) {
						double[] vipRatios = GameDataManager.getInstance().getGameDataAsDoubleArray(GameDataKey.VIP_STRENGTH_RATIO);
						int vipIndex = user.getViplevel()-1;
						double vipRatio = 0.0;
						if ( vipRatios != null && vipIndex >= 0 && vipIndex < vipRatios.length ) {
							vipRatio = vipRatios[vipIndex];
						}
						if ( vipRatio > 0 ) {
							successRatio += vipRatio;
						}
					}
					guildAddRatio = (Double)ScriptManager.getInstance().runScriptForObject(
							ScriptHook.GUILD_CRAFT_ADDRATIO,  new Object[]{user, 
							GuildCraftType.COMPOSE_STRENGTH});
					/*
					StatClient.getIntance().sendDataToStatServer(user, StatAction.Forge, 
							new Object[]{StoneType.STRENGTH, equipPropData.getName(), equipPropData.getLevel(), targetLevel, forgeStatus});
							*/
				}
			}
		} else if ( isDoingLucky || isDoingDefend || isDoingAgility || isDoingAttack) {
			int equipPrice = ShopManager.getInstance().findPriceForPropData(user, equipPropData, MoneyType.GOLDEN, null, null, false);
			double unitPrice = (equipPrice + totalStonePrice) / 5;
			price = unitPrice;
			
			int colorIndex = equipPropData.getWeaponColor().ordinal()+1;
			/*
			if ( equipPropData.getWeaponColor() == WeaponColor.BLUE ) {
				price += 0.5 * unitPrice;
			} else if ( equipPropData.getWeaponColor() == WeaponColor.PINK ) {
				price += 3 * unitPrice;
			} else if ( equipPropData.getWeaponColor() == WeaponColor.ORGANCE ) {
				price += 6 * unitPrice;
			}
			price += 0.02 * targetLevel * targetLevel * unitPrice;
			*/
			price += stoneLevel * 0.9  * colorIndex * 0.3 * unitPrice;

			double[] ratios = GameDataManager.getInstance().getGameDataAsDoubleArray(GameDataKey.CRAFT_EQUIP_STONE_RATIO);
			int index = stoneLevel - 1;
			if ( index < 0 ) index = 0;
			if ( index >= ratios.length ) index = ratios.length-1;
			successRatio = ratios[index] * (1+luckyRatio);
			
			guildAddRatio = (Double)ScriptManager.getInstance().runScriptForObject(
					ScriptHook.GUILD_CRAFT_ADDRATIO,  new Object[]{user, 
					GuildCraftType.COMPOSE_EQUIP_WITH_STONE});
		} else if ( isDoingDiamond ) {
			int slotCount = equipPropData.getSlots().size();
			/**
			 * 价格的计算方法
			 * slot golden
				1.00	1000
				2.00	4000
				3.00	9000
				4.00	16000
				5.00	25000
				6.00	36000
				7.00	49000
				8.00	64000
				9.00	81000
				price = slot*slot*golden
			 */
			price = slotCount * slotCount * 0.2 * 10000;
			/**
			 * 成功率的计算方法
			 * 每开一个孔，成功率在基础成功率上 * 0.8
			 * 
			 */
			double[] ratios =GameDataManager.getInstance().getGameDataAsDoubleArray(GameDataKey.CRAFT_DIAMOND_RATIO);
			if ( stoneLevel < 1 ) stoneLevel = 1;
			if ( stoneLevel > ratios.length ) stoneLevel = ratios.length;
			successRatio = ratios[stoneLevel-1] * (1+luckyRatio);
			if ( slotCount > 0 ) {
				successRatio = Math.pow(successRatio, slotCount/2);
			}
		} else if ( isDoingCrystal ) {
			int equipPrice = ShopManager.getInstance().findPriceForPropData(user, equipPropData, MoneyType.GOLDEN, null, null, true);
			price = (equipPrice + totalStonePrice)/2.0;
			double[] ratios = new double[]{0.3, 0.3, 0.3, 0.3, 0.3};
			if ( stoneLevel < 1 ) stoneLevel = 1;
			if ( stoneLevel > ratios.length ) stoneLevel = ratios.length;
			successRatio = ratios[stoneLevel-1] * (1+luckyRatio);;
		}

		ArrayList list = new ArrayList();
		list.add((int)Math.round(price));
		list.add(successRatio>1?1:successRatio);
		list.add(guildAddRatio);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}

	/**
	 * @param user
	 * @param luckyRatio
	 * @param stoneRatios
	 * @return
	 */
	public static double calculateStrengthSuccessRatio(User user,
			double luckyCardSuccessRatio, double[] stoneSuccessRatio) {
		
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
	
}
