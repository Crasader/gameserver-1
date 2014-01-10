package script;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import script.guild.CraftRatio;

import com.xinqihd.sns.gameserver.config.CraftComposeFuncType;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.MoneyType;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.ShopManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.forge.ComposeStatus;
import com.xinqihd.sns.gameserver.guild.GuildCraftType;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * 计算合成几率和价格：
 * 
 */
public class CraftComposePrice {

	private static final Logger logger = LoggerFactory
			.getLogger(CraftComposePrice.class);

	/**
	 * 熔炼说明： 
	 * 1 只有名称和等级完全相同的4个物品才能熔炼 
	 * 2 需要添加相应的公式才可以熔炼 
	 * 3 熔炼的成功率为100% (目前使用的是正态分布概率)
	 * 4 熔炼分为2种，石头熔炼和装备熔炼 
	 * 5 石头熔炼即对于火神石，土神石，风神石，水神石，强化石的熔炼 6
	 * 石头熔炼是将低级石头熔炼为高一级的石头，即4个1级石头熔炼为1个2级石头，4个2级石头熔炼为1个3级石头，类推 7 石头最高为4级 8
	 * 装备熔炼即对戒指和手镯的熔炼 9
	 * 装备熔炼是将低级装备熔炼为高一级的装备，即4个+0的装备熔炼为1个+1的装备，4个+1的装备熔炼为1个+2的装备，类推 10 装备熔炼最大到+5 11
	 * 装备熔炼后基本属性会有增加，效果为上一级的1.4倍 12 装备熔炼后得到的合成属性为0
	 * 
	 * 判断玩家熔炼符的类型
	 *   绿色熔炼符：需要1-4把相同的白色装备，熔炼成一把绿色装备，基础成功率为40%，熔炼的武器每增加一把，几率提升15%，
	 *   蓝色熔炼符：需要1-4把相同的绿色装备，熔炼成一把绿色装备，基础成功率为40%，熔炼的武器每增加一把，几率提升15%，
	 *   粉色熔炼符：需要1-4把相同的粉色装备，熔炼成一把绿色装备，基础成功率为40%，熔炼的武器每增加一把，几率提升15%，
	 *   橙色熔炼符：需要1-4把相同的橙色装备，熔炼成一把绿色装备，基础成功率为40%，熔炼的武器每增加一把，几率提升15%，
	 *   
	 *   强化石熔炼符：需要1-4块相同等级的强化石，熔炼下一等级强化石，基础成功率为40%，熔炼的武器每增加一把，几率提升15%，
	 *   水神石熔炼符：需要1-4块相同等级的水神石，熔炼下一等级强化石，基础成功率为40%，熔炼的武器每增加一把，几率提升15%，
	 *   火神石熔炼符：需要1-4块相同等级的火神石，熔炼下一等级强化石，基础成功率为40%，熔炼的武器每增加一把，几率提升15%，
	 *   风神石熔炼符：需要1-4块相同等级的风神石，熔炼下一等级强化石，基础成功率为40%，熔炼的武器每增加一把，几率提升15%，
	 *   土神石熔炼符：需要1-4块相同等级的土神石，熔炼下一等级强化石，基础成功率为40%，熔炼的武器每增加一把，几率提升15%，
	 *   
	 *   武器熔炼符：随机四把同级别装备，生成一把新的普通武器
	 *   装备熔炼符：随机四把同级别装备，生成一把新的普通装备（非武器）
	 *   精良武器熔炼符：随机四把同级别装备，生成一把新的高级武器
	 *   精良装备熔炼符：随机四把同级别装备，生成一把新的高级装备（非武器）
	 */
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if (result != null) {
			return result;
		}

		ComposeStatus composeStatus = ComposeStatus.SUCCESS;
		User user = (User) parameters[0];
		Object[] array = (Object[]) parameters[1];
		
		//寻找熔炼符
		/**
		 *   0: 绿色熔炼
		 *   1: 蓝色熔炼
		 *   2: 粉色熔炼
		 *   3: 橙色熔炼
		 *  
		 *   4: 强化石熔炼
		 *   5: 水神石熔炼
		 *   6: 火神石熔炼
		 *   7: 风神石熔炼
		 *   8: 土神石熔炼
		 *  
		 *   9: 武器熔炼
		 *   10: 装备熔炼
		 *   11: 精良武器熔炼
		 *   12: 精良装备熔炼
		 */
		CraftComposeFuncType funcType = null;
		//All other item ids
		Set itemIds = new HashSet(4);
		ArrayList itemList = new ArrayList();

		for (int i = 0; i < array.length; i++) {
			PropData propData = (PropData) array[i];
			String itemId = propData.getItemId();
			if ( ItemManager.greenColorFuncId.equals(itemId) ) {
				funcType = CraftComposeFuncType.COLOR_GREEN;
			} else if ( ItemManager.blueColorFuncId.equals(itemId) ) {
				funcType = CraftComposeFuncType.COLOR_BLUE;
			} else if ( ItemManager.pinkColorFuncId.equals(itemId) ) {
				funcType = CraftComposeFuncType.COLOR_PINK;
			} else if ( ItemManager.orangeColorFuncId.equals(itemId) ) {
				funcType = CraftComposeFuncType.COLOR_ORANGE;
			} else if ( ItemManager.purpleColorFuncId.equals(itemId) ) {
				funcType = CraftComposeFuncType.COLOR_PURPLE;
			} else if (ItemManager.strengthFuncId.equals(itemId)) {
				funcType = CraftComposeFuncType.COMPOSE_STRENGTH;
			} else if (ItemManager.luckyFuncId.equals(itemId)) {
				funcType = CraftComposeFuncType.COMPOSE_WATER;
			} else if (ItemManager.attackFuncId.equals(itemId)) {
				funcType = CraftComposeFuncType.COMPOSE_FIRE;
			} else if (ItemManager.agilityFuncId.equals(itemId)) {
				funcType = CraftComposeFuncType.COMPOSE_WIND;
			} else if (ItemManager.defendFuncId.equals(itemId)) {
				funcType = CraftComposeFuncType.COMPOSE_EARTH;
			} else if (ItemManager.godFuncId.equals(itemId)) {
				funcType = CraftComposeFuncType.COMPOSE_GOD;
			} else if (ItemManager.weaponFuncId.equals(itemId)) {
				funcType = CraftComposeFuncType.MAKE_WEAPON;
			} else if (ItemManager.equipFuncId.equals(itemId)) {
				funcType = CraftComposeFuncType.MAKE_EQUIP;
			} else if (ItemManager.weaponProFuncId.equals(itemId)) {
				funcType = CraftComposeFuncType.MAKE_WEAPON2;
			} else if (ItemManager.equipProFuncId.equals(itemId)) {
				funcType = CraftComposeFuncType.MAKE_EQUIP2;
			} else {
				itemIds.add(itemId);
				itemList.add(propData);
			}
		}
		
		ArrayList list = null;
		if ( funcType.ordinal() <= CraftComposeFuncType.COLOR_PURPLE.ordinal() ) {
				//绿色熔炼
				//蓝色熔炼
				//粉色熔炼
				//橙色熔炼
				//紫色熔炼
				list = composeColorEquip(user, itemIds, itemList, funcType);
		} else if ( funcType.ordinal() <= CraftComposeFuncType.COMPOSE_GOD.ordinal() ) { 
				//强化石熔炼
				//水神石熔炼
				//火神石熔炼
				//风神石熔炼
				//土神石熔炼
				list = composeStone(user, itemIds, itemList);
		} else if ( funcType.ordinal() <= CraftComposeFuncType.MAKE_EQUIP2.ordinal() ) { 
				//武器熔炼
				//装备熔炼
				//精良武器熔炼
				//精良装备熔炼
				list = composeEquipOrWeapon(user, itemIds, itemList, funcType);
		}

		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
	
	/**
	 *   武器熔炼符：随机四把同级别装备，生成一把新的普通武器
	 *   装备熔炼符：随机四把同级别装备，生成一把新的普通装备（非武器）
	 *   精良武器熔炼符：随机四把同级别装备，生成一把新的高级武器
	 *   精良装备熔炼符：随机四把同级别装备，生成一把新的高级装备（非武器）
	 *   
	 * @param user
	 * @param itemIds
	 * @param itemList
	 * @param funcType
	 * @return
	 */
	public static final ArrayList composeEquipOrWeapon(User user,
			Set itemIds, ArrayList itemList, CraftComposeFuncType funcType) {
		
		PropData newPropData = null;
		double stoneSuccessRatio = 0;
		double guildAddRatio = 0;
		int price = 0;
		
		if ( itemList.size() == 4 ) {
			//Check all the weapons should have same level
			int level = -1;
			int count = itemList.size();
			boolean success = true;
			for ( int i=0; i<count; i++ ) {
				PropData item = (PropData)itemList.get(i);
				if ( level < 0 ) {
					level = item.getUserLevel();
				} else if ( level != item.getUserLevel() ) {
					SysMessageManager.getInstance().sendClientInfoMessage(user, "craft.compose.equip.level", Type.NORMAL);
					success = false;
					break;
				}
			}
			
			if ( success ) {
				price = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.PRICE_CRAFT_COMPOSE, 2000);
				PropData stonePropData = (PropData)itemList.get(0);
				WeaponPojo weaponPojo = EquipManager.getInstance().getWeaponById(stonePropData.getItemId());

				stoneSuccessRatio = EquipCalculator
						.calculateComposeItemSuccessRatio(itemList.size(), stonePropData.getLevel());

				guildAddRatio = (Double)ScriptManager.getInstance().runScriptForObject(
						ScriptHook.GUILD_CRAFT_ADDRATIO,  new Object[]{user, 
						GuildCraftType.COMPOSE_EQUIP});
				
				//累计四把武器的金币价格
				/*
				for (Iterator iter = itemList.iterator(); iter.hasNext();) {
					PropData propData = (PropData) iter.next();
					price += ShopManager.getInstance().findPriceForPropData(user, propData, MoneyType.GOLDEN);
				}
				*/
				price = EquipCalculator.calculateCraftPrice(user.getLevel(), price);
				if ( funcType == CraftComposeFuncType.MAKE_WEAPON ) {
				  //武器熔炼
					price /= 2;
				} else if ( funcType == CraftComposeFuncType.MAKE_EQUIP ) {
					//装备熔炼
					price /= 2;
				} else if ( funcType == CraftComposeFuncType.MAKE_WEAPON2 ) {
					//精良武器熔炼
					price *= 6;
				} else if ( funcType == CraftComposeFuncType.MAKE_EQUIP2 ) {
					//精良装备熔炼
					price *= 3;
				}
			}
		} else {
			SysMessageManager.getInstance().sendClientInfoMessage(user, "craft.compose.equip.notenough", Type.NORMAL);
		}
		
		ArrayList list = new ArrayList();
		list.add(price);
		list.add(stoneSuccessRatio>1?1:stoneSuccessRatio);
		list.add(guildAddRatio);
		return list;
	}
	
	/**
	 * 绿色熔炼符：需要1-4把相同的白色装备，熔炼成一把绿色装备，基础成功率为40%，熔炼的武器每增加一把，几率提升15%，
	 * 蓝色熔炼符：需要1-4把相同的绿色装备，熔炼成一把蓝色装备，基础成功率为40%，熔炼的武器每增加一把，几率提升15%，
	 * 粉色熔炼符：需要1-4把相同的粉色装备，熔炼成一把粉色装备，基础成功率为40%，熔炼的武器每增加一把，几率提升15%，
	 * 橙色熔炼符：需要1-4把相同的橙色装备，熔炼成一把橙色装备，基础成功率为40%，熔炼的武器每增加一把，几率提升15%，
	 * 
	 * 熔炼成功后，强化等级将清零
	 * 
	 * @param newPropData
	 * @param user
	 * @param itemIds
	 * @param itemIdList
	 */
	public static final ArrayList composeColorEquip(User user,
			Set itemIds, ArrayList itemList, CraftComposeFuncType funcType) {
		
		double stoneSuccessRatio = 0;
		double guildAddRatio = 0;
		double price = 0;
		
		//检查武器的颜色及等级相同
		ComposeStatus composeStatus = ComposeStatus.SUCCESS;
		String propId = null;
		WeaponColor color = null;
		int strengthLevel = 0;
		//检查武器的颜色符合熔炼
		for (Iterator iter = itemList.iterator(); iter.hasNext();) {
			PropData propData = (PropData) iter.next();
			if ( propId == null ) {
				propId = propData.getItemId();
				color = propData.getWeaponColor();
				if ( propData.isWeapon() && strengthLevel < propData.getLevel() ) {
					strengthLevel = propData.getLevel();
				}
			} else {
				if ( !propId.equals(propData.getItemId()) || 
						color != propData.getWeaponColor() ) {
					composeStatus = ComposeStatus.NOT_SAME_WEAPON;
					SysMessageManager.getInstance().sendClientInfoMessage(user, "craft.compose.color.notsame", Type.NORMAL);
					break;
				}
			}
		}
		//检查武器的颜色为特定色
		if ( funcType == CraftComposeFuncType.COLOR_GREEN ) {
			//绿色熔炼
			if ( color != WeaponColor.WHITE ) {
				composeStatus = ComposeStatus.NOT_WHITE_WEAPON;
				SysMessageManager.getInstance().sendClientInfoMessage(user, "craft.compose.color", 
						Action.NOOP, new Object[]{Text.text("WHITE"), Text.text("GREEN")});
			} 
		} else if ( funcType == CraftComposeFuncType.COLOR_BLUE ) {
			//蓝色熔炼
			if ( color != WeaponColor.GREEN ) {
				composeStatus = ComposeStatus.NOT_GREEN_WEAPON;
				SysMessageManager.getInstance().sendClientInfoMessage(user, "craft.compose.color", 
						Action.NOOP, new Object[]{Text.text("GREEN"), Text.text("BLUE")});
			}
		} else if ( funcType == CraftComposeFuncType.COLOR_PINK ) {
			//粉色熔炼
			if ( color != WeaponColor.BLUE ) {
				composeStatus = ComposeStatus.NOT_BLUE_WEAPON;
				SysMessageManager.getInstance().sendClientInfoMessage(user, "craft.compose.color", 
						Action.NOOP, new Object[]{Text.text("BLUE"), Text.text("PINK")});
			}
		} else if ( funcType == CraftComposeFuncType.COLOR_ORANGE ) {
			//橙色熔炼
			if ( color != WeaponColor.PINK ) {
				composeStatus = ComposeStatus.NOT_PINK_WEAPON;
				SysMessageManager.getInstance().sendClientInfoMessage(user, "craft.compose.color", 
						Action.NOOP, new Object[]{Text.text("PINK"), Text.text("ORGANCE")});
			}
		} else if ( funcType == CraftComposeFuncType.COLOR_PURPLE ) {
			//紫色熔炼
			if ( color != WeaponColor.ORGANCE ) {
				composeStatus = ComposeStatus.NOT_ORANGE_WEAPON;
				SysMessageManager.getInstance().sendClientInfoMessage(user, "craft.compose.color", 
						Action.NOOP, new Object[]{Text.text("ORGANCE"), Text.text("PURPLE")});
			}
		}
		
		if ( composeStatus == ComposeStatus.SUCCESS ) {
			if ( itemList.size()>0 ) {
				PropData propData = (PropData)itemList.get(0);
				price = ShopManager.getInstance().findPriceForPropData(user, propData, MoneyType.GOLDEN, null, null, false) / 4.0;
			}
			//检查武器的颜色为特定色
			if ( funcType == CraftComposeFuncType.COLOR_GREEN ) {
				//绿色熔炼
				price = price * 1.5;
			} else if ( funcType == CraftComposeFuncType.COLOR_BLUE ) {
				//蓝色熔炼
				price = price * 1.5;
			} else if ( funcType == CraftComposeFuncType.COLOR_PINK ) {
				//粉色熔炼
				price *= 3;
			} else if ( funcType == CraftComposeFuncType.COLOR_ORANGE ) {
				//橙色熔炼
				price *= 5;
			} else if ( funcType == CraftComposeFuncType.COLOR_PURPLE ) {
				//紫色熔炼
				price *= 6;
			}
			
			int count = itemList.size();
			price = Math.round( (count * count * 0.25) * price); 
			if ( itemList.size() >= 1 && itemIds.size() == 1 ) {
				PropData stonePropData = (PropData)itemList.get(0);
				stoneSuccessRatio = EquipCalculator
						.calculateComposeColorSuccessRatio(color, itemList.size());
				
				guildAddRatio = (Double)ScriptManager.getInstance().runScriptForObject(
						ScriptHook.GUILD_CRAFT_ADDRATIO,  new Object[]{user, 
						GuildCraftType.COMPOSE_COLOR});

			}
		}
		
		ArrayList list = new ArrayList();
		list.add((int)price);
		list.add(stoneSuccessRatio>1?1:stoneSuccessRatio);
		list.add(guildAddRatio);
		list.add(strengthLevel);
		list.add(funcType);
		return list;
	}

	/**
	 *   强化石熔炼符：需要1-4块相同等级的强化石，熔炼下一等级强化石，基础成功率为40%，熔炼的武器每增加一把，几率提升15%，
	 *   水神石熔炼符：需要1-4块相同等级的水神石，熔炼下一等级强化石，基础成功率为40%，熔炼的武器每增加一把，几率提升15%，
	 *   火神石熔炼符：需要1-4块相同等级的火神石，熔炼下一等级强化石，基础成功率为40%，熔炼的武器每增加一把，几率提升15%，
	 *   风神石熔炼符：需要1-4块相同等级的风神石，熔炼下一等级强化石，基础成功率为40%，熔炼的武器每增加一把，几率提升15%，
	 *   土神石熔炼符：需要1-4块相同等级的土神石，熔炼下一等级强化石，基础成功率为40%，熔炼的武器每增加一把，几率提升15%，
	 *   
	 * @param newPropData
	 * @param user
	 * @param itemIds
	 * @param itemList
	 */
	public static final ArrayList composeStone(User user,
			Set itemIds, ArrayList itemList) {

		double stoneSuccessRatio = 0;
		double guildAddRatio = 0;
		int price = 0;
		/**
		 * 需要1-4块相同等级的石头，熔炼下一等级石头，基础成功率为40%，熔炼的武器每增加一把，几率提升15%，
		 */
		int stoneCount = itemList.size();
		if ( stoneCount >= 1 && itemIds.size() == 1 ) {
			PropData stonePropData = (PropData)itemList.get(0);
			ItemPojo itemPojo = ItemManager.getInstance().getItemById(
					stonePropData.getItemId());
			if ( itemPojo != null ) {
				String stoneType = itemPojo.getTypeId();
				int itemLevel = stonePropData.getLevel();
				if (itemLevel < 5) {
					double stonePrice = ShopManager.getInstance().findPriceForPropData(user, stonePropData, MoneyType.GOLDEN, null, null, false);
					price = (int)Math.round(stoneCount * stoneCount * 1.5 * stonePrice);
					stoneSuccessRatio = EquipCalculator
							.calculateComposeItemSuccessRatio(itemList.size(), itemLevel);

					guildAddRatio = (Double)ScriptManager.getInstance().runScriptForObject(
							ScriptHook.GUILD_CRAFT_ADDRATIO,  new Object[]{user, 
							GuildCraftType.COMPOSE_STONE});

				}
			}
		}
		ArrayList list = new ArrayList();
		list.add(price);
		list.add(stoneSuccessRatio>1?1:stoneSuccessRatio);
		list.add(guildAddRatio);
		return list;
	}

}
