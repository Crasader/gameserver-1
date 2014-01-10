package script;

import java.util.ArrayList;
import java.util.Date;

import com.xinqihd.sns.gameserver.chat.ChatManager;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.Pojo;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.StoneType;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.db.mongo.UserActionManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEnhanceField;
import com.xinqihd.sns.gameserver.entity.user.PropDataSlot;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserActionKey;
import com.xinqihd.sns.gameserver.forge.ForgeStatus;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.script.function.UserCalculator;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.MathUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * The user want to compose lower level items to higher item. 
 * 
 */
public class CraftForgeEquip {
	
	private static final ArrayList DIAMOND_FIELDS = new ArrayList();
	static {
		DIAMOND_FIELDS.add(PropDataEnhanceField.DEFEND);
		DIAMOND_FIELDS.add(PropDataEnhanceField.LUCKY);
		DIAMOND_FIELDS.add(PropDataEnhanceField.AGILITY);
		DIAMOND_FIELDS.add(PropDataEnhanceField.ATTACK);
	}
	
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
	 */
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		
		User user = (User)parameters[0];
		Object[] array = (Object[])parameters[1];
		PropData origPropData  = (PropData)array[0];
		PropData[] stonePropDatas = (PropData[])array[1];
		int price = (Integer)array[2];
		double ratio = (Double)array[3];
		double guildRatio = (Double)array[4];
		
		PropData equipPropData = origPropData.clone();
		
		int targetLevel = equipPropData.getLevel()+1;
		
		double luckyRatio = 0.0;
		double[] stoneRatios = new double[stonePropDatas.length];
		boolean useGodStone = false;
		//If the user want to strengthen an equipment
		boolean isDoingStrength = false;
		boolean isDoingLucky = false;
		boolean isDoingDefend = false;
		boolean isDoingAgility = false;
		boolean isDoingAttack = false;
		boolean isDoingDiamond = false;
		boolean isDoingCrystal = false;
		int godStoneLevel = 0;
		
		ForgeStatus forgeStatus = ForgeStatus.SUCCESS;
		
		int stoneLevel = 1;
		
		for ( int i=0; i<stonePropDatas.length; i++ ) {
			Pojo pojo =stonePropDatas[i].getPojo();
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
				} else if ( ItemManager.luckStoneId.equals(itemPojo.getTypeId()) ) {
					isDoingLucky = true;
					stoneLevel = stonePropDatas[i].getLevel();
				} else if ( ItemManager.defendStoneId.equals(itemPojo.getTypeId()) ) {
					isDoingDefend = true;
					stoneLevel = stonePropDatas[i].getLevel();
				} else if ( ItemManager.agilityStoneId.equals(itemPojo.getTypeId()) ) {
					isDoingAgility = true;
					stoneLevel = stonePropDatas[i].getLevel();
				} else if ( ItemManager.attackStoneId.equals(itemPojo.getTypeId()) ) {
					isDoingAttack = true;
					stoneLevel = stonePropDatas[i].getLevel();
				} else if ( ItemManager.diamondStoneId.equals(itemPojo.getTypeId()) ) {
					isDoingDiamond = true;
					stoneLevel = stonePropDatas[i].getLevel();
				} else if ( ItemManager.crystalStoneId.equals(itemPojo.getTypeId()) ) {
					isDoingCrystal = true;
					stoneLevel = stonePropDatas[i].getLevel();
				}
			}
		}
		
		if ( isDoingStrength ) {
			forgeStatus = doStrength(user, equipPropData, targetLevel, ratio, guildRatio,
					useGodStone, forgeStatus, stoneLevel, godStoneLevel);
		} else if ( isDoingLucky ) {
			forgeStatus = doForgeStone(user, origPropData, equipPropData, ratio, guildRatio,
					stoneLevel, ItemManager.luckStoneId, StoneType.LUCKY);
		} else if ( isDoingDefend ) {
			forgeStatus = doForgeStone(user, origPropData, equipPropData, ratio, guildRatio,
					stoneLevel, ItemManager.defendStoneId, StoneType.DEFEND);
		} else if ( isDoingAgility ) {
			forgeStatus = doForgeStone(user, origPropData, equipPropData, ratio, guildRatio,
					stoneLevel, ItemManager.agilityStoneId, StoneType.AGILITY);
		} else if ( isDoingAttack ) {
			forgeStatus = doForgeStone(user, origPropData, equipPropData, ratio, guildRatio,
					stoneLevel, ItemManager.attackStoneId, StoneType.ATTACK);
		} else if ( isDoingDiamond ) {
			forgeStatus = doForgeSlot(user, origPropData, equipPropData, ratio, guildRatio,
					stoneLevel, ItemManager.diamondStoneId, StoneType.DIAMOND);
		} else if ( isDoingCrystal ) {
			forgeStatus = doForgeCrystal(user, origPropData, equipPropData, ratio, guildRatio,
					stoneLevel, ItemManager.crystalStoneId, StoneType.CRYSTAL);
		}
		if ( forgeStatus == ForgeStatus.SUCCESS ) {
			/**
			 * Store all the used stones with this propData
			 * 2013-03-04
			 */
			for (int j = 0; j < stonePropDatas.length; j++) {
				PropData pd = stonePropDatas[j];
				equipPropData.addItem(pd.getItemId(), 1);
			}
				
	  	/**
	  	 * After the resubscribe, should recaculate the 
	  	 * power
	  	 * 2012-12-16
	  	 */
			int power = (int)UserCalculator.calculatePower(user);
		  user.setPower(power);
		}

		ArrayList list = new ArrayList();
		list.add(forgeStatus);
		list.add(equipPropData);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
	
	/**
	 * @param user
	 * @param origPropData
	 * @param equipPropData
	 * @param luckyRatio
	 * @param stoneLevel
	 * @return
	 */
	private static ForgeStatus doForgeSlot(User user, PropData origPropData,
			PropData equipPropData, double ratio, double guildRatio, int stoneLevel, 
			String funcItemId, StoneType stoneType) {
		ForgeStatus forgeStatus = ForgeStatus.SUCCESS;
		double successRatio = ratio + guildRatio;
		boolean success = false;
		if ( successRatio <= 0 ) {
			forgeStatus = ForgeStatus.UNFORGABLE;
		} else {
			success = MathUtil.nextDouble() < successRatio;
			if ( success ) {
				PropDataSlot slot = new PropDataSlot();
				if ( stoneLevel == 1 ) {
					//1级石头只能开一个孔
					int index = MathUtil.nextGaussionInt(0, DIAMOND_FIELDS.size(), 3.0);
					slot.addAvailableTypes((PropDataEnhanceField)DIAMOND_FIELDS.get(index));
				} else if ( stoneLevel == 2 ) {
					//2级石头只能开一个孔
					int index = MathUtil.nextGaussionInt(0, DIAMOND_FIELDS.size(), 5.0);
					slot.addAvailableTypes((PropDataEnhanceField)DIAMOND_FIELDS.get(index));
				} else if ( stoneLevel == 3 ) {
					//3级石头只能开2个孔
					int number = MathUtil.nextGaussionInt(1, 2, 5.0);
					Object[] fieldObjs = MathUtil.randomPick(DIAMOND_FIELDS, number);
					for (int i = 0; i < fieldObjs.length; i++) {
						PropDataEnhanceField field = (PropDataEnhanceField)fieldObjs[i];
						slot.addAvailableTypes(field);
					}
				} else if ( stoneLevel == 4 ) {
					//4级石头只能开2-3个孔
					int number = MathUtil.nextGaussionInt(2, 4, 5.0);
					Object[] fieldObjs = MathUtil.randomPick(DIAMOND_FIELDS, number);
					for (int i = 0; i < fieldObjs.length; i++) {
						PropDataEnhanceField field = (PropDataEnhanceField)fieldObjs[i];
						slot.addAvailableTypes(field);
					}
				} else if ( stoneLevel == 5 ) {
					//4级石头只能开3-4个孔
					int number = MathUtil.nextGaussionInt(3, 5, 5.0);
					Object[] fieldObjs = MathUtil.randomPick(DIAMOND_FIELDS, number);
					for (int i = 0; i < fieldObjs.length; i++) {
						PropDataEnhanceField field = (PropDataEnhanceField)fieldObjs[i];
						slot.addAvailableTypes(field);
					}
				}
				equipPropData.addNewSlot(slot);
				forgeStatus = ForgeStatus.SUCCESS;

				//Call the TaskHook
				TaskManager.getInstance().processUserTasks(user, TaskHook.CRAFT_FORGE, 
						new Object[]{stoneType, equipPropData, stoneLevel});
			} else {
				//failure
				forgeStatus = ForgeStatus.FAILURE;
			}

		}

		StatClient.getIntance().sendDataToStatServer(user, StatAction.Forge, 
				new Object[]{stoneType, equipPropData.getName(), equipPropData.getLuckLev(), stoneLevel, forgeStatus});
		return forgeStatus;
	}

	/**
	 * @param user
	 * @param origPropData
	 * @param equipPropData
	 * @param luckyRatio
	 * @param stoneLevel
	 * @return
	 */
	private static ForgeStatus doForgeStone(User user, PropData origPropData,
			PropData equipPropData, double ratio, double guildRatio, int stoneLevel, 
			String funcItemId, StoneType stoneType) {
		ForgeStatus forgeStatus = ForgeStatus.SUCCESS;
		double successRatio = ratio + guildRatio;
		boolean success = false;
		if ( successRatio <= 0 ) {
			forgeStatus = ForgeStatus.UNFORGABLE;
		} else {
			/**
			 * 测试武器插槽数
			 */
			PropDataEnhanceField field = null;
			if ( ItemManager.attackStoneId.equals(funcItemId) ) {
				field = PropDataEnhanceField.ATTACK;
			} else if ( ItemManager.defendStoneId.equals(funcItemId) ) {
				field = PropDataEnhanceField.DEFEND;
			} else if ( ItemManager.agilityStoneId.equals(funcItemId) ) {
				field = PropDataEnhanceField.AGILITY;
			} else if ( ItemManager.luckStoneId.equals(funcItemId) ) {
				field = PropDataEnhanceField.LUCKY;
			}
			PropDataSlot currentSlot = equipPropData.getGivenSlot(field, stoneLevel);
			if ( currentSlot == null ) {
				forgeStatus = ForgeStatus.NO_SLOT;
				success = false;
			} else {
				success = MathUtil.nextDouble() < successRatio;
				if ( success ) {
					//success
					double finalData = EquipCalculator.calculateForgeData(equipPropData, stoneLevel, funcItemId, currentSlot);
					forgeStatus = ForgeStatus.SUCCESS;
					
					//Call the TaskHook
					TaskManager.getInstance().processUserTasks(user, TaskHook.CRAFT_FORGE, 
							new Object[]{stoneType, equipPropData, stoneLevel});
				} else {
					//failure
					forgeStatus = ForgeStatus.FAILURE;
				}
			}
		}
				
		StatClient.getIntance().sendDataToStatServer(user, StatAction.Forge, 
				new Object[]{stoneType, equipPropData.getName(), equipPropData.getLuckLev(), stoneLevel, forgeStatus});
		return forgeStatus;
	}
	
	/**
	 * Forge the crystal
	 * @param user
	 * @param origPropData
	 * @param equipPropData
	 * @param ratio
	 * @param guildRatio
	 * @param stoneLevel
	 * @param funcItemId
	 * @param stoneType
	 * @return
	 */
	private static ForgeStatus doForgeCrystal(User user, PropData origPropData,
			PropData equipPropData, double ratio, double guildRatio, int stoneLevel, 
			String funcItemId, StoneType stoneType) {
		ForgeStatus forgeStatus = ForgeStatus.SUCCESS;
		double successRatio = ratio + guildRatio;
		boolean success = false;
		if ( successRatio <= 0 ) {
			forgeStatus = ForgeStatus.UNFORGABLE;
		} else {
			/**
			 * 计算需要嵌入的水晶石数量
			 */
			ScriptResult result = CraftCalDiamond.func(new Object[]{user, origPropData});
			if ( result != null && result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
				int totalDiamond = (Integer)result.getResult().get(0);
				if ( totalDiamond > 0 ) {
					int currentDiamond = origPropData.getCrystal();
					success = MathUtil.nextDouble() < successRatio;
					if ( success ) {
						//success
						int[] stoneCount = new int[]{1, 2, 4, 6, 10};
						if ( stoneLevel<1 ) stoneLevel = 1;
						if ( stoneLevel>5 ) stoneLevel = 5;
						currentDiamond += stoneCount[stoneLevel-1];
						if ( currentDiamond >= totalDiamond ) {
							//uplevel the propData
							WeaponPojo weaponPojo = EquipManager.getInstance().
									getWeaponById(equipPropData.getItemId());
							if ( weaponPojo != null ) {
								WeaponPojo nextWeaponPojo = EquipManager.getInstance().
										getWeaponByTypeNameAndUserLevel(weaponPojo.getTypeName(), 
												weaponPojo.getUserLevel()+10);
								if ( nextWeaponPojo != null ) {
									PropData pd = nextWeaponPojo.toPropData(origPropData.getPropIndate(), origPropData.getWeaponColor());
									pd.setPew(origPropData.getPew());
									pd.setSlots(origPropData.getSlots());
									pd.setMaxLevel(origPropData.getMaxLevel());
									if ( origPropData.getLevel() > 1 ) {
										EquipCalculator.weaponUpLevel(pd, origPropData.getLevel()-1);
									}
									equipPropData.copyFrom(pd);
									//equipPropData.setTotalGolden(origPropData.getTotalGolden());
								} else {
									forgeStatus = ForgeStatus.UNFORGABLE;
								}
							} else {
								forgeStatus = ForgeStatus.UNFORGABLE;
							}
						} else {
							equipPropData.setCrystal(currentDiamond);
						}
						
						//Call the TaskHook
//						TaskManager.getInstance().processUserTasks(user, TaskHook.CRAFT_FORGE, s
//								new Object[]{stoneType, equipPropData, stoneLevel});
					} else {
						//failure
						forgeStatus = ForgeStatus.FAILURE;
					}
				}
			} else {
				forgeStatus = ForgeStatus.UNFORGABLE;
			}
			
		}
				
		StatClient.getIntance().sendDataToStatServer(user, StatAction.Forge, 
				new Object[]{stoneType, equipPropData.getName(), equipPropData.getLuckLev(), stoneLevel, forgeStatus});
		return forgeStatus;
	}

	/**
	 * @param user
	 * @param equipPropData
	 * @param targetLevel
	 * @param luckyRatio
	 * @param stoneRatios
	 * @param useGodStone
	 * @param forgeStatus
	 * @param stoneLevel
	 * @return
	 */
	private static ForgeStatus doStrength(User user, PropData equipPropData,
			int targetLevel, double ratio, double guildRatio,
			boolean useGodStone, ForgeStatus forgeStatus, int stoneLevel, int godStoneLevel) {

		boolean success = false;
		int maxLevel = GameDataManager.getInstance().getGameDataAsInt(
				GameDataKey.STRENGTH_MAX_LEVEL, 15);
		int weaponMaxLevel = equipPropData.getMaxLevel();
		maxLevel = Math.min(weaponMaxLevel, maxLevel);
		if ( targetLevel > maxLevel ) {
			success = false;
			forgeStatus = ForgeStatus.MAX_LEVEL;
			SysMessageManager.getInstance().sendClientInfoMessage(user, "strength.max", Type.NORMAL);
		} else {
			success = true;
			boolean godStoneAvailable = false;
			if ( useGodStone ) {
				int[] godStoneRange = GameDataManager.getInstance().getGameDataAsIntArray(
						GameDataKey.STRENGTH_GODSTONE_RANGE);
				if ( targetLevel <= godStoneRange[godStoneLevel-1] ) {
					success = true;
					godStoneAvailable = true;
				} else {
					success = false;
					String message = Text.text("strength.nogodstone", godStoneLevel, godStoneRange[godStoneLevel-1]);
					SysMessageManager.getInstance().sendClientInfoRawMessage(user.getSessionKey(), message, Action.NOOP, Type.NORMAL);
					forgeStatus = ForgeStatus.UNFORGABLE;
				}
			}
			if ( success ) {
				double successRatio = ratio + guildRatio;
				if ( successRatio <= 0 ) {
					forgeStatus = ForgeStatus.UNFORGABLE;
				} else {
					success = MathUtil.nextDouble() < successRatio;

					if ( success ) {
						//Update weapon to higher level.
						EquipCalculator.weaponUpLevel(equipPropData, targetLevel);

						//Call the TaskHook
						TaskManager.getInstance().processUserTasks(user, TaskHook.CRAFT_FORGE, 
								new Object[]{StoneType.STRENGTH, equipPropData, stoneLevel});
						
						//Send broadcast message
						if ( targetLevel >= 7 ) {
							String content = Text.text("notice.strength", 
								new Object[]{user.getRoleName(), equipPropData.getName(), targetLevel});
							ChatManager.getInstance().processChatToWorldAsyn(null, content);

							//Weibo message
							String weiboKey = StringUtil.concat(
									new Object[]{"weibo.str.", String.valueOf(MathUtil.nextFakeInt(2))});
							String weibo = Text.text(weiboKey, 
									new Object[]{equipPropData.getName(), String.valueOf(targetLevel), 
									DateUtil.formatDateTime(new Date())});
							
							SysMessageManager.getInstance().sendClientInfoWeiboMessage(
									user.getSessionKey(), content, weibo, Type.WEIBO);
						}

					} else {
						forgeStatus = ForgeStatus.FAILURE;
						int maxFailureLevel = GameDataManager.getInstance().getGameDataAsInt(
								GameDataKey.CRAFT_FAILURE_LEVEL_DOWN, 5);
						/*
						int maxVipFailureLevel = GameDataManager.getInstance().getGameDataAsInt(
								GameDataKey.CRAFT_FAILURE_VIP_LEVEL_DOWN, 9);
						if ( user.isVip() ) {
							if ( equipPropData.getLevel()>=maxVipFailureLevel && !useGodStone ) {
								targetLevel = equipPropData.getLevel()-1;
								EquipCalculator.weaponUpLevel(equipPropData, targetLevel);
							}
						} else {
							if ( equipPropData.getLevel()>=maxFailureLevel && !useGodStone ) {
								targetLevel = equipPropData.getLevel()-1;
								EquipCalculator.weaponUpLevel(equipPropData, targetLevel);
							}
						}
						*/
						if ( equipPropData.getLevel()>=maxFailureLevel ) {
							if ( !godStoneAvailable ) {
								targetLevel = equipPropData.getLevel()-1;
								EquipCalculator.weaponUpLevel(equipPropData, targetLevel);
							}
						}
					}
				}
			}

			StatClient.getIntance().sendDataToStatServer(user, StatAction.Forge, 
					new Object[]{StoneType.STRENGTH, equipPropData.getName(), equipPropData.getLevel(), targetLevel, forgeStatus});
			
			UserActionManager.getInstance().addUserAction(user.getRoleName(), 
					UserActionKey.Forge, equipPropData.getName());
		}
		return forgeStatus;
	}
	
}
