package script;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.config.CraftComposeFuncType;
import com.xinqihd.sns.gameserver.config.MoneyType;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.ShopManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.db.mongo.UserActionManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserActionKey;
import com.xinqihd.sns.gameserver.forge.ComposeStatus;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.MathUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * The user want to compose lower level items to higher item.
 * 
 */
public class CraftComposeItem {

	private static final Logger logger = LoggerFactory
			.getLogger(CraftComposeItem.class);

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

		User user = (User) parameters[0];
		Object[] array = (Object[]) parameters[1];
		int price = (Integer)parameters[2];
		
		//寻找熔炼符
		/**
		 *   0: 绿色熔炼
		 *   1: 蓝色熔炼
		 *   2: 粉色熔炼
		 *   3: 橙色熔炼
		 *   4: 紫色熔炼
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
			} else if (ItemManager.weaponFuncId.equals(itemId)) {
				funcType = CraftComposeFuncType.MAKE_WEAPON;
			} else if (ItemManager.equipFuncId.equals(itemId)) {
				funcType = CraftComposeFuncType.MAKE_EQUIP;
			} else if (ItemManager.weaponProFuncId.equals(itemId)) {
				funcType = CraftComposeFuncType.MAKE_WEAPON2;
			} else if (ItemManager.equipProFuncId.equals(itemId)) {
				funcType = CraftComposeFuncType.MAKE_EQUIP2;
			} else if (ItemManager.godFuncId.equals(itemId)) {
				funcType = CraftComposeFuncType.COMPOSE_GOD;
			} else {
				itemIds.add(itemId);
				itemList.add(propData);
			}
		}
		/**
		 * Make sure the funcId and stoneType are matched.
		 * 水神石熔炼符不应该熔炼火神石
		 * 2012-12-16
		 */
		ComposeStatus composeStatus = ComposeStatus.SUCCESS;
		if ( funcType.ordinal() >= CraftComposeFuncType.COMPOSE_STRENGTH.ordinal() 
				&& funcType.ordinal() <= CraftComposeFuncType.COMPOSE_GOD.ordinal() ) {
			for (Iterator iterator = itemList.iterator(); iterator.hasNext();) {
				PropData propData = (PropData) iterator.next();
				if ( !propData.isWeapon() ) {
					ItemPojo itemPojo = (ItemPojo)propData.getPojo();
					if ( funcType == CraftComposeFuncType.COMPOSE_STRENGTH ) {
						//强化石熔炼
						if ( !ItemManager.strengthStoneId.equals(itemPojo.getTypeId()) ) {
							composeStatus = ComposeStatus.UNCOMPOSABLE;  
						}
					} else if ( funcType == CraftComposeFuncType.COMPOSE_WATER ) { 
						//水神石熔炼
						if ( !ItemManager.luckStoneId.equals(itemPojo.getTypeId()) ) {
							composeStatus = ComposeStatus.UNCOMPOSABLE;  
						}
					} else if ( funcType == CraftComposeFuncType.COMPOSE_FIRE ) {
						//火神石熔炼
						if ( !ItemManager.attackStoneId.equals(itemPojo.getTypeId()) ) {
							composeStatus = ComposeStatus.UNCOMPOSABLE;  
						}
					} else if ( funcType == CraftComposeFuncType.COMPOSE_WIND ) {
						//风神石熔炼
						if ( !ItemManager.agilityStoneId.equals(itemPojo.getTypeId()) ) {
							composeStatus = ComposeStatus.UNCOMPOSABLE;  
						}
					} else if ( funcType == CraftComposeFuncType.COMPOSE_EARTH ) {
						//土神石熔炼
						if ( !ItemManager.defendStoneId.equals(itemPojo.getTypeId()) ) {
							composeStatus = ComposeStatus.UNCOMPOSABLE;  
						}
					} else if ( funcType == CraftComposeFuncType.COMPOSE_GOD ) {
						//土神石熔炼
						if ( !ItemManager.godStoneId.equals(itemPojo.getTypeId()) ) {
							composeStatus = ComposeStatus.UNCOMPOSABLE;  
						}
					}
				} else {
					composeStatus = ComposeStatus.INVALID_STONE;
				}
			}
		}
		
		ArrayList list = null;
		StatAction action = null;
		UserActionKey actionKey = null;
		if ( composeStatus == ComposeStatus.SUCCESS ) {
			if ( funcType == CraftComposeFuncType.COLOR_GREEN ) {
					//绿色熔炼
					action = StatAction.CraftComposeColorGreen;
					actionKey = UserActionKey.CraftComposeColorGreen;
					list = composeColorEquip(user, itemIds, itemList, funcType, price);
			} else if ( funcType == CraftComposeFuncType.COLOR_BLUE ) {
					//蓝色熔炼
					action = StatAction.CraftComposeColorBlue;
					actionKey = UserActionKey.CraftComposeColorBlue;
					list = composeColorEquip(user, itemIds, itemList, funcType, price);
			} else if ( funcType == CraftComposeFuncType.COLOR_PINK ) {
					//粉色熔炼
					action = StatAction.CraftComposeColorPink;
					actionKey = UserActionKey.CraftComposeColorPink;
					list = composeColorEquip(user, itemIds, itemList, funcType, price);
			} else if ( funcType == CraftComposeFuncType.COLOR_ORANGE ) {
					//橙色熔炼
					action = StatAction.CraftComposeColorOrange;
					actionKey = UserActionKey.CraftComposeColorOrange;
					list = composeColorEquip(user, itemIds, itemList, funcType, price);
			} else if ( funcType == CraftComposeFuncType.COLOR_PURPLE ) {
				//橙色熔炼
				action = StatAction.CraftComposeColorPurple;
				actionKey = UserActionKey.CraftComposeColorPurple;
				list = composeColorEquip(user, itemIds, itemList, funcType, price);
			} else if ( funcType == CraftComposeFuncType.COMPOSE_STRENGTH ) {
					//强化石熔炼
					action = StatAction.CraftComposeStoneStrength;
					actionKey = UserActionKey.CraftComposeStoneStrength;
					list = composeStone(user, itemIds, itemList, funcType, price);
			} else if ( funcType == CraftComposeFuncType.COMPOSE_WATER ) {
					//水神石熔炼
					action = StatAction.CraftComposeStoneWater;
					actionKey = UserActionKey.CraftComposeStoneWater;
					list = composeStone(user, itemIds, itemList, funcType, price);
			} else if ( funcType == CraftComposeFuncType.COMPOSE_FIRE ) {
					//火神石熔炼
					action = StatAction.CraftComposeStoneFire;
					actionKey = UserActionKey.CraftComposeStoneFire;
					list = composeStone(user, itemIds, itemList, funcType, price);
			} else if ( funcType == CraftComposeFuncType.COMPOSE_WIND ) {
					//风神石熔炼
					action = StatAction.CraftComposeStoneWind;
					actionKey = UserActionKey.CraftComposeStoneWind;
					list = composeStone(user, itemIds, itemList, funcType, price);
			} else if ( funcType == CraftComposeFuncType.COMPOSE_EARTH ) {
					//土神石熔炼
					action = StatAction.CraftComposeStoneEarth;
					actionKey = UserActionKey.CraftComposeStoneEarth;
					list = composeStone(user, itemIds, itemList, funcType, price);
			} else if ( funcType == CraftComposeFuncType.COMPOSE_GOD ) {
					//神恩符熔炼
					action = StatAction.CraftComposeStoneGod;
					actionKey = UserActionKey.CraftComposeStoneGod;
					list = composeStone(user, itemIds, itemList, funcType, price);
			} else if ( funcType == CraftComposeFuncType.MAKE_WEAPON ) {
					//武器熔炼
					action = StatAction.CraftComposeWeapon;
					actionKey = UserActionKey.CraftComposeWeapon;
					list = composeEquipOrWeapon(user, itemIds, itemList, funcType, price);
			} else if ( funcType == CraftComposeFuncType.MAKE_EQUIP ) {
					//装备熔炼
					action = StatAction.CraftComposeEquip;
					actionKey = UserActionKey.CraftComposeEquip;
					list = composeEquipOrWeapon(user, itemIds, itemList, funcType, price);
			} else if ( funcType == CraftComposeFuncType.MAKE_WEAPON2 ) {
					//精良武器熔炼
					action = StatAction.CraftComposeWeaponPro;
					actionKey = UserActionKey.CraftComposeWeaponPro;
					list = composeEquipOrWeapon(user, itemIds, itemList, funcType, price);
			} else if ( funcType == CraftComposeFuncType.MAKE_EQUIP2 ) {
					//精良装备熔炼
					action = StatAction.CraftComposeEquipPro;
					actionKey = UserActionKey.CraftComposeEquipPro;
					list = composeEquipOrWeapon(user, itemIds, itemList, funcType, price);
			}
		}
		
		if ( action != null ) {
			composeStatus = ComposeStatus.NEED_CONFIRM;
			if ( list != null && list.size() >= 1) {
				composeStatus = (ComposeStatus)list.get(0);
			}
			if ( action != null ) {
				composeStatus = ComposeStatus.NEED_CONFIRM;
				if ( list != null && list.size() >= 1) {
					composeStatus = (ComposeStatus)list.get(0);
				}
				String[] itemNames = new String[itemList.size()+1];
				itemNames[0] = composeStatus.toString();
				for ( int i=0; i<itemList.size(); i++ ) {
					PropData propData = (PropData)itemList.get(i);
					if ( propData != null ) {
						itemNames[i+1] = propData.getName();
					}
				}
				StatClient.getIntance().sendDataToStatServer(user, action, itemNames);
			}
		} else {
			list = new ArrayList();
			list.add(composeStatus);
			list.add(null);
			list.add(funcType);
			UserActionManager.getInstance().addUserAction(user.getRoleName(), 
					actionKey);
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
	private static ArrayList composeEquipOrWeapon(User user,
			Set itemIds, ArrayList itemList, CraftComposeFuncType funcType, int price) {
		
		PropData newPropData = null;
		ComposeStatus composeStatus = ComposeStatus.SUCCESS;

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
					composeStatus = ComposeStatus.DIFF_LEVEL;
					break;
				}
			}
			
			if ( success ) {
				boolean hasPayed = ShopManager.getInstance().payForSomething(user, MoneyType.GOLDEN, price, 1, null);
				if ( hasPayed ) {
					PropData stonePropData = (PropData)itemList.get(0);
					WeaponPojo weaponPojo = EquipManager.getInstance().getWeaponById(stonePropData.getItemId());

					double stoneSuccessRatio = EquipCalculator
							.calculateComposeItemSuccessRatio(itemList.size(), stonePropData.getLevel());
					success = EquipCalculator.composeItem(-1,
							stoneSuccessRatio, 0);
					if (success) {
						if ( composeStatus == ComposeStatus.SUCCESS ) {
							//检查生成类型
							WeaponPojo weapon = null;
							if ( funcType == CraftComposeFuncType.MAKE_WEAPON ) {
									//武器熔炼
									weapon = EquipManager.getInstance().getRandomWeapon(level, EquipType.WEAPON, 1);
									newPropData = weapon.toPropData(30, WeaponColor.WHITE);
									composeStatus = ComposeStatus.SUCCESS;
							} else if ( funcType == CraftComposeFuncType.MAKE_EQUIP ) {
									//装备熔炼
								{
									for ( int i=0; i<EquipType.values().length; i++ ) {
										int index = (int)(MathUtil.nextDouble()*EquipType.values().length);
										if ( index == EquipType.WEAPON.ordinal() || index == EquipType.SUIT.ordinal() ) {
											continue;
										}
										EquipType type = EquipType.values()[index];
										weapon = EquipManager.getInstance().getRandomWeapon(level, type, 1);
										if ( weapon != null ) {
											break;
										}
									}
									newPropData = weapon.toPropData(30, WeaponColor.WHITE);
								}
							} else if ( funcType == CraftComposeFuncType.MAKE_WEAPON2 ) {
									//精良武器熔炼
									weapon = EquipManager.getInstance().getRandomWeapon(level, EquipType.WEAPON, 2);
									newPropData = weapon.toPropData(30, WeaponColor.WHITE);
									composeStatus = ComposeStatus.SUCCESS;
							} else if ( funcType == CraftComposeFuncType.MAKE_EQUIP2 ) {
								{
									for ( int i=0; i<EquipType.values().length; i++ ) {
										int index = (int)(MathUtil.nextDouble()*EquipType.values().length);
										//不可生成武器或者套装
										if ( index == EquipType.WEAPON.ordinal() || index == EquipType.SUIT.ordinal() 
												|| index == EquipType.OFFHANDWEAPON.ordinal() 
												|| index == EquipType.GIFT_PACK.ordinal() ) {
											continue;
										}
										EquipType type = EquipType.values()[index];
										weapon = EquipManager.getInstance().getRandomWeapon(level, type, 2);
										if ( weapon != null ) {
											break;
										}
									}
									newPropData = weapon.toPropData(30, WeaponColor.WHITE);
								}
							}
							newPropData = weapon.toPropData(30, WeaponColor.WHITE);
						}
						// Call the TaskHook
						TaskManager.getInstance().processUserTasks(user,
								TaskHook.CRAFT_COMPOSE_EQUIP,
								new Object[] { funcType, newPropData, composeStatus });
					} else {
						composeStatus = ComposeStatus.FAILURE;
						newPropData = null;
					}
					
					StatClient.getIntance().sendDataToStatServer(user, 
							StatAction.ConsumeCompose, new Object[]{MoneyType.GOLDEN, price, 
							newPropData.getName(), newPropData.getLevel(), newPropData.getPropUsedTime()});
				} else {
					success = false;
					composeStatus = ComposeStatus.NO_MONEY;
				}
				
			}

		} else {
			composeStatus = ComposeStatus.NOT_ENOUGH_WEAPON;
			SysMessageManager.getInstance().sendClientInfoMessage(user, "craft.compose.equip.notenough", Type.NORMAL);
		}
		
		ArrayList list = new ArrayList();
		list.add(composeStatus);
		list.add(newPropData);
		list.add(funcType);
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
	private static ArrayList composeColorEquip(User user,
			Set itemIds, ArrayList itemList, CraftComposeFuncType funcType, int price) {
		
		PropData newPropData = null;
		ComposeStatus composeStatus = ComposeStatus.SUCCESS;
		//检查武器的颜色及等级相同
		String propId = null;
		WeaponColor color = null;
		int strengthLevel = 0;
		PropData oldPropData = null;
		for (Iterator iter = itemList.iterator(); iter.hasNext();) {
			PropData propData = (PropData) iter.next();
			if ( propId == null ) {
				oldPropData = propData;
				propId = propData.getItemId();
				color = propData.getWeaponColor();
				if ( strengthLevel < propData.getLevel() ) {
					strengthLevel = propData.getLevel();
				}
			} else {
				if ( !propData.isWeapon() || !propId.equals(propData.getItemId()) || 
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
			String name = null;
			int level = 0;
			int propUsedTime = 0;
			boolean hasPayed = ShopManager.getInstance().payForSomething(user, MoneyType.GOLDEN, price, 1, null);
			if ( hasPayed ) {
				if ( itemList.size() >= 1 && itemIds.size() == 1 ) {
					double stoneSuccessRatio = EquipCalculator.
							calculateComposeColorSuccessRatio(color, itemList.size());
					boolean success = EquipCalculator.composeItem(-1,
							stoneSuccessRatio, 0);
					if (success) {
						WeaponPojo weapon = EquipManager.getInstance().getWeaponById(propId);
						if ( weapon != null ) {
							newPropData = weapon.toPropData(30, WeaponColor.values()[color.ordinal()+1]);
							if ( oldPropData != null && oldPropData.getMaxLevel()>newPropData.getMaxLevel() ) {
								newPropData.setMaxLevel(oldPropData.getMaxLevel());
								newPropData.setSlots(oldPropData.getSlots());
								newPropData.getSlots().clear();
							}
							name = newPropData.getName();
							level = newPropData.getLevel();
							propUsedTime = newPropData.getPropUsedTime();
							
							composeStatus = ComposeStatus.SUCCESS;
	
							// Call the TaskHook
							TaskManager.getInstance().processUserTasks(user,
									TaskHook.CRAFT_COMPOSE_COLOR,
									new Object[] { funcType, newPropData, composeStatus });
						} else {
							composeStatus = ComposeStatus.UNCOMPOSABLE;
							newPropData = null;
						}
					} else {
						composeStatus = ComposeStatus.FAILURE;
						newPropData = null;
					}

				} else {
					composeStatus = ComposeStatus.INVALID_STONE;
				}
				StatClient.getIntance().sendDataToStatServer(user, 
						StatAction.ConsumeCompose, new Object[]{MoneyType.GOLDEN, price, 
						name, level, propUsedTime});
			} else {
				composeStatus = ComposeStatus.NO_MONEY;
			}
		}
		
		ArrayList list = new ArrayList();
		list.add(composeStatus);
		list.add(newPropData);
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
			Set itemIds, ArrayList itemList, CraftComposeFuncType funcType, int price) {
		PropData newPropData = null;
		ComposeStatus composeStatus = ComposeStatus.FAILURE;
		/**
		 * 需要1-4块相同等级的石头，熔炼下一等级石头，基础成功率为40%，熔炼的武器每增加一把，几率提升15%，
		 */
		if ( itemList.size() >= 1 && itemIds.size() == 1 ) {
			boolean hasPayed = ShopManager.getInstance().payForSomething(user, MoneyType.GOLDEN, price, 1, null);
			if ( hasPayed ) {
				PropData stonePropData = (PropData)itemList.get(0);
				ItemPojo itemPojo = ItemManager.getInstance().getItemById(stonePropData.getItemId());
				if ( itemPojo != null ) {
					String stoneType = itemPojo.getTypeId();
					int itemLevel = stonePropData.getLevel();
					if (itemLevel < 5) {
						double stoneSuccessRatio = EquipCalculator
								.calculateComposeItemSuccessRatio(itemList.size(), itemLevel);
						boolean success = EquipCalculator.composeItem(itemLevel,
								stoneSuccessRatio, 0);
						if (success) {
							int newLevel = itemLevel + 1;

							String itemId = ItemPojo.toId(stoneType, newLevel);
							ItemPojo newItem = ItemManager.getInstance().getItemById(itemId);
							newPropData = newItem.toPropData();
							composeStatus = ComposeStatus.SUCCESS;

							// Call the TaskHook
							TaskManager.getInstance().processUserTasks(user,
									TaskHook.CRAFT_COMPOSE,
									new Object[] { funcType, newPropData, composeStatus });
						} else {
							composeStatus = ComposeStatus.FAILURE;
							newPropData = null;
						}
					} else {
						composeStatus = ComposeStatus.MAX_LEVEL;
						
						newPropData = null;
						logger.debug("The stone {} is already at max level",
								stonePropData.getName());
					}
				} else {
					composeStatus = ComposeStatus.INVALID_STONE;
				}
			} else {
				composeStatus = ComposeStatus.NO_MONEY;
			}
		} else {
			composeStatus = ComposeStatus.INVALID_STONE;
		}
		ArrayList list = new ArrayList();
		list.add(composeStatus);
		list.add(newPropData);
		list.add(funcType);
		return list;
	}

}
