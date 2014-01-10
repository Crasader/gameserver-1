package com.xinqihd.sns.gameserver.forge;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.CraftComposeFuncType;
import com.xinqihd.sns.gameserver.config.MoneyType;
import com.xinqihd.sns.gameserver.config.Pojo;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.ConfirmManager;
import com.xinqihd.sns.gameserver.db.mongo.ConfirmManager.ConfirmCallback;
import com.xinqihd.sns.gameserver.db.mongo.ShopManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseCompose.BseCompose;
import com.xinqihd.sns.gameserver.proto.XinqiBseForge.BseForge;
import com.xinqihd.sns.gameserver.proto.XinqiBseTransfer.BseTransfer;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.function.UserCalculator;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * This is the typical RPG forge system.
 * Users can use special stones to strengthen his weapons or equipments, or
 *  use more than one special stones to forge an advanced same type special stone.
 *  
 * 
 * @author wangqi
 *
 */
public class CraftManager {
	
	private static final Logger logger = LoggerFactory.getLogger(CraftManager.class);
	
	private static CraftManager instance = new CraftManager();
	
	/**
	 * Get the ForgeManager through this method.
	 * @return
	 */
	public static CraftManager getInstance() {
		return instance;
	}
	
	/**
	 * Internal or tests used constructor.
	 */
	CraftManager() {
		
	}
	
	/**
	 * Query for the 合成's price and ratio
	 * @param user
	 * @param pews
	 */
	public ArrayList composeItemPriceAndRatio(User user, int[] pews) {
		ComposeStatus status = ComposeStatus.SUCCESS;
		PropData newPropData = null;
		PropData[] propDatas = null;
		
		//Call script
		boolean success = true;
		Bag bag = user.getBag();
		propDatas = new PropData[pews.length];
		for ( int i =0; i<propDatas.length; i++ ) {
			if ( pews[i] >= Bag.BAG_WEAR_COUNT ) {
				propDatas[i] = bag.getOtherPropData(pews[i]);
			} else if ( pews[i] >= 0 ) {
				propDatas[i] = bag.getWearPropDatas().get(pews[i]);
			}
			if ( propDatas[i] == null ) {
				logger.debug("Cannot compose item for a null propData in composeItem. pew {}", pews[i]);
				success = false;
			}
		}
		if ( success ) {
			ScriptResult result = GameContext.getInstance().getScriptManager().
					runScript(ScriptHook.CRAFT_COMPOSE_PRICE, user, propDatas);
			ArrayList list = (ArrayList)result.getResult();
			return list;
		} else {
			return null;
		}
	}
	
	/**
	 * 
	 * @param user
	 * @param pews
	 * @return
	 */
	public ArrayList forgeEquipPriceAndRatio(User user, int equipPew, int[] stonePews) {
		ForgeStatus status = ForgeStatus.SUCCESS;
		PropData equipPropData = null;
		PropData[] stonePropDatas = null;
		PropData newPropData = null;
		
		//Call script
		Bag bag = user.getBag();
		if ( equipPew >= Bag.BAG_WEAR_COUNT ) {
			equipPropData = bag.getOtherPropData(equipPew);
		} else if ( equipPew >= 0 ) {
			equipPropData = bag.getWearPropDatas().get(equipPew);
		}
		if ( equipPropData == null ) {
			status = ForgeStatus.UNFORGABLE;
			logger.warn("Cannot forge equip because the give equip in user bag does not exist.");
			return null;
		} else {
			stonePropDatas = new PropData[stonePews.length];
			for ( int i =0; i<stonePropDatas.length; i++ ) {
				//Maybe null
				stonePropDatas[i] = bag.getOtherPropData(stonePews[i]);
				if ( stonePropDatas[i] == null ) {
					logger.debug("Cannot forge equip for a null propData in forgeEquip. pew {}", stonePews[i]);
					status = ForgeStatus.UNFORGABLE;
				}
			}
			
			ScriptResult result = GameContext.getInstance().getScriptManager().
					runScript(ScriptHook.CRAFT_FORGE_PRICE, user, 
							new Object[]{equipPropData, stonePropDatas});
			ArrayList list = (ArrayList)result.getResult();
			return list;
		}
	}
	
	/**
	 * 玩家请求将源武器的属性转移到目标武器之上
	 * 
	 * @param srcEquipPew
	 * @param tarEquipPew
	 * @return
	 */
	public ArrayList transferEquipPriceAndRatio(final User user, int srcEquipPew, int tarEquipPew) {
		TransferStatus status = TransferStatus.SUCCESS;
		Bag bag = user.getBag();
		PropData srcData = null;
		PropData targetData = null;
		PropData newPropData = null;
		
		if ( srcEquipPew >= Bag.BAG_WEAR_COUNT ) {
			srcData = bag.getOtherPropData(srcEquipPew);
		} else if (srcEquipPew>=0) {
			srcData = bag.getWearPropDatas().get(srcEquipPew);
		}
		if ( tarEquipPew>= Bag.BAG_WEAR_COUNT ) {
			targetData = bag.getOtherPropData(tarEquipPew);
		} else if ( tarEquipPew >= 0 ) {
			targetData = bag.getWearPropDatas().get(tarEquipPew);
		}
		
		if ( srcData == null || targetData == null ) {
			status = TransferStatus.FAILURE;
			logger.debug("Cannot find srcData or targetData");
		}
		if ( status == TransferStatus.SUCCESS ) {
			//Call script
			ScriptResult result = GameContext.getInstance().getScriptManager().
					runScript(ScriptHook.CRAFT_TRANSFER_PRICE, user, 
							new Object[]{srcData, targetData});
			ArrayList list = (ArrayList)result.getResult();	
			return list;
		}
		
		return null;
	}
	
	/**
	 * 玩家请求合成物品
	 * 合成是指用相同的石头合成品级更高的石头，合成过程有一定的失败概率
	 * 
	 * 0：无法熔炼；1：熔炼成功；2：熔炼失败 3:操作异常
	 * 
	 * @param equipPew   要锻造的装备的格子号
	 * @param stonePews  辅助物品的格子号
	 * @param saveStone  是否使用了神恩符
	 */
	public void composeItem(final User user, final int[] pews) {
		ComposeStatus status = ComposeStatus.SUCCESS;
		
		//Check the price first
		ArrayList priceResult = composeItemPriceAndRatio(user, pews);
		if ( priceResult == null ) {
			BseCompose.Builder builder = BseCompose.newBuilder();
			builder.setResult(ComposeStatus.FAILURE.ordinal());
			GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
			return;
		}
		final int price = (Integer)priceResult.get(0);
		if ( user.getGolden() < price ) {
			status = ComposeStatus.NO_MONEY;
		}
		/**
		 * 颜色熔炼时会判断一次强化等级，并给予用户提示
		 * wangqi 2012-10-24
		 */
		if ( priceResult.size()>=5 ) {
			int strengthLevel = (Integer)priceResult.get(3);
			CraftComposeFuncType funcType = (CraftComposeFuncType)priceResult.get(4);
			String message = null;
			if ( funcType == CraftComposeFuncType.COLOR_PURPLE ) {
				status = ComposeStatus.NEED_CONFIRM;
				message = Text.text("craft.compose.color.purple.warning");
				
			} else if ( strengthLevel > 0 ) {
				status = ComposeStatus.NEED_CONFIRM;
				message = Text.text("craft.compose.color.str.warning");
			}

			if ( status == ComposeStatus.NEED_CONFIRM ) {
				ConfirmManager.getInstance().sendConfirmMessage(user, message, "craft.compose.str", new ConfirmCallback() {
					@Override
					public void callback(User user, int selected) {
						if ( selected == ConfirmManager.ConfirmResult.YES.ordinal() ) {
							doComposeItem(user, pews, price);
						} else {
							BseCompose.Builder builder = BseCompose.newBuilder();
							builder.setResult(ComposeStatus.FAILURE.ordinal());
							GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
						}
					}
				});
			} else {
				doComposeItem(user, pews, price);
			}
		} else {
			doComposeItem(user, pews, price);
		}
		
		//StatClient.getIntance().sendDataToStatServer(user, StatAction.Compose, price, status);
	}

	/**
	 * Do the actual compose item
	 * @param user
	 * @param pews
	 * @param status
	 * @param newPropData
	 * @param propDatas
	 * @param price
	 */
	private void doComposeItem(User user, int[] pews, int price) {
		ComposeStatus status = ComposeStatus.SUCCESS;
		PropData newPropData = null;
		CraftComposeFuncType funcType = null;
		PropData[] propDatas = null;

		if ( user == null ) {
			logger.warn("Cannot compose item for a null user");
			status = ComposeStatus.EXCEPTION;
		} else if ( user.getBag() == null ) {
			logger.warn("Cannot compose item for a null bag in user {}", user.getRoleName());
			status = ComposeStatus.EXCEPTION;
		} else if ( pews == null || pews.length <= 0 ) {
			logger.warn("Cannot compose item for a null pews array");
			status = ComposeStatus.EXCEPTION;
		} else {
			Bag bag = user.getBag();
			propDatas = new PropData[pews.length];
			for ( int i =0; i<propDatas.length; i++ ) {
				//Maybe null
				if ( pews[i] >= Bag.BAG_WEAR_COUNT ) {
					propDatas[i] = bag.getOtherPropData(pews[i]);
				} else if ( pews[i] >= 0 ) {
					propDatas[i] = bag.getWearPropDatas().get(pews[i]);
				}
				if ( propDatas[i] == null ) {
					logger.debug("Cannot compose item for a null propData in composeItem. pew {}", pews[i]);
					status = ComposeStatus.EXCEPTION;
				}
			}
			if ( status == ComposeStatus.SUCCESS ) {
				//Call script
				ScriptResult result = GameContext.getInstance().getScriptManager().
						runScript(ScriptHook.CRAFT_COMPOSE_ITEM, user, propDatas, price);
				ArrayList list = (ArrayList)result.getResult();
				status = (ComposeStatus)list.get(0);
				newPropData = (PropData)list.get(1);
				funcType = (CraftComposeFuncType)list.get(2);
			}
		}
		
		BseCompose.Builder builder = BseCompose.newBuilder();
		if ( status == ComposeStatus.SUCCESS || status == ComposeStatus.FAILURE ) {
			Bag bag = user.getBag();
			builder.setResult(status.ordinal());
			if ( newPropData != null ) {
				newPropData.setTotalGolden(newPropData.getTotalGolden()+price);
				/**
				 * 当玩家背包已满，这时如果将身上穿着的装备熔炼颜色，那么新熔炼出来的物品无法
				 * 加入背包，会丢失。所以如果熔炼身上的装备，要提前保存下它的位置PEW。
				 * 2013-02-18
				 */
				int wearPew = -1;
				int totalGolden = price;
				for ( PropData propData : propDatas ) {
					totalGolden += propData.getTotalGolden();
					builder.addPews(propData.getPew());
					/**
					 * 熔炼武器颜色时，如果熔炼成功，旧的道具全部消失
					 * 2012-10-09
					 */
					if ( propData.getPew() < Bag.BAG_WEAR_COUNT ) {
						//Unwear the equip
						if ( propData.getPew() > 0 && propData.getPew() < PropDataEquipIndex.values().length ) {
							wearPew = propData.getPew();
							bag.removeWearPropDatas(PropDataEquipIndex.values()[propData.getPew()]);
							UserCalculator.updateWeaponPropData(user, propData, false);
							user.updatePowerRanking();
							
							GameContext.getInstance().getUserManager().saveUser(user, false);
						}
					} else {
						bag.removeOtherPropDatas(propData.getPew());
					}
				}
				newPropData.setTotalGolden(totalGolden);
				/**
				 * Delete the old prop first then add the new one.
				 * 2012-11-12
				 */
				if ( wearPew != -1 ) {
					/**
					 * Check the type first
					 */
					EquipType slot = PropDataEquipIndex.values()[wearPew].getPropEquipType();
					Pojo pojo = newPropData.getPojo();
					if ( pojo instanceof WeaponPojo ) {
						WeaponPojo weapon = (WeaponPojo)pojo;
						if ( weapon.getSlot() == slot ) {
							bag.setWearPropData(newPropData, wearPew);
							UserCalculator.updateWeaponPropData(user, newPropData, true);
							user.updatePowerRanking();
						} else {
							/**
							 * There should have enough place for it.
							 */
							bag.addOtherPropDatas(newPropData);
						}
					} else {
						/**
						 * There should have enough place for it.
						 */
						bag.addOtherPropDatas(newPropData);
					}
					
					GameContext.getInstance().getUserManager().saveUser(user, false);
				} else {
					bag.addOtherPropDatas(newPropData);
				}
				builder.setNewProp(newPropData.toXinqiPropData(user));
			} else {
				for ( PropData propData : propDatas ) {
					/**
					 * 熔炼武器颜色时，如果熔炼失败，武器不消失，其他道具消失
					 * 2012-10-09
					 */
					if ( !propData.isWeapon() || 
							(funcType == CraftComposeFuncType.COLOR_PURPLE && status == ComposeStatus.FAILURE ) ) {
						builder.addPews(propData.getPew());
						if ( propData.getPew() > Bag.BAG_WEAR_COUNT ) {
							bag.removeOtherPropDatas(propData.getPew());
						} else { 
							bag.removeWearPropDatas(PropDataEquipIndex.values()[propData.getPew()]);
							UserCalculator.updateWeaponPropData(user, propData, false);
							user.updatePowerRanking();
							
							UserManager.getInstance().saveUser(user, false);
						}
					}
				}
			}
			
			GameContext.getInstance().getUserManager().saveUserBag(user, false);
			//GameContext.getInstance().writeResponse(user.getSessionKey(), user.toBseRoleBattleInfo(true));

		} else {
			builder.setResult(status.ordinal());
		}
		
		if ( status != ComposeStatus.NEED_CONFIRM ) {
			GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
			/**
			 * Force the client to update the bag pews
			 * 2012-11-26
			 */
			GameContext.getInstance().writeResponse(user.getSessionKey(), user.toBseRoleBattleInfo(true));
		}
	}
	
	/**
	 * 玩家请求熔炼物品(提升武器的数值)
	 * 
	 * @param equipPew
	 * @param stonePews
	 * @param saveStone Ignore it because the stonePews contain all the stones used.
	 * @return
	 */
	public void forgeEquip(User user, int equipPew, int[] stonePews) {
		ForgeStatus status = ForgeStatus.SUCCESS;
		PropData equipPropData = null;
		PropData[] stonePropDatas = null;
		PropData newPropData = null;
		
		//Check the price first
		ArrayList priceResult = forgeEquipPriceAndRatio(user, equipPew, stonePews);
		int price = (Integer)priceResult.get(0);
		double ratio = (Double)priceResult.get(1);
		double guildRatio = (Double)priceResult.get(2);
		
		boolean hasPayed = ShopManager.getInstance().payForSomething(user, MoneyType.GOLDEN, price, 1, null);
		if ( !hasPayed ) {
			status = ForgeStatus.NO_MONEY;
		}
		
		if ( status == ForgeStatus.SUCCESS ) {
			if ( user == null ) {
				logger.warn("Cannot forge equip for a null user");
				status = ForgeStatus.UNFORGABLE;
			} else if ( user.getBag() == null ) {
				logger.warn("Cannot forge equip for a null bag in user {}", user.getRoleName());
				status = ForgeStatus.UNFORGABLE;
			} else if ( stonePews == null || stonePews.length <= 0 ) {
				logger.warn("Cannot forge equip for a null pews array");
				status = ForgeStatus.UNFORGABLE;
			} else {
				Bag bag = user.getBag();
				if ( equipPew >= Bag.BAG_WEAR_COUNT ) {
					equipPropData = bag.getOtherPropData(equipPew);
				} else if ( equipPew >= 0 ) {
					equipPropData = bag.getWearPropDatas().get(equipPew);
				}
				if ( equipPropData == null ) {
					status = ForgeStatus.UNFORGABLE;
					logger.warn("Cannot forge equip because the give equip in user bag does not exist.");
				} else {
					stonePropDatas = new PropData[stonePews.length];
					for ( int i =0; i<stonePropDatas.length; i++ ) {
						//Maybe null
						stonePropDatas[i] = bag.getOtherPropData(stonePews[i]);
						if ( stonePropDatas[i] == null ) {
							logger.debug("Cannot forge equip for a null propData in forgeEquip. pew {}", stonePews[i]);
							status = ForgeStatus.UNFORGABLE;
						}
					}
					
					if ( status == ForgeStatus.SUCCESS ) {
						//Call script
						ScriptResult result = GameContext.getInstance().getScriptManager().
								runScript(ScriptHook.CRAFT_FORGE_EQUIP, user, 
										new Object[]{equipPropData, stonePropDatas, price, ratio, guildRatio});
						ArrayList list = (ArrayList)result.getResult();
						status = (ForgeStatus)list.get(0);
						newPropData = (PropData)list.get(1); 
					}
					
					StatClient.getIntance().sendDataToStatServer(user, 
							StatAction.ConsumeForge, MoneyType.GOLDEN, price,
							equipPropData.getName(), equipPropData.getLevel(), status);
				}
			} //else
		}
		
		BseForge.Builder builder = BseForge.newBuilder();
		if ( status == ForgeStatus.SUCCESS || status == ForgeStatus.FAILURE) {
			newPropData.setTotalGolden(newPropData.getTotalGolden()+price);
			
			Bag bag = user.getBag();
			/**
			 * Check if the Equipment is wearing
			 */
			if ( newPropData.getPew() < Bag.BAG_WEAR_COUNT ) {
				//The user wears it
				bag.recalculateUserProperties(equipPropData, false);
				bag.recalculateUserProperties(newPropData, true);
				bag.setWearPropData(newPropData, newPropData.getPew());
			} else {
				bag.setOtherPropDataAtPew(newPropData, newPropData.getPew());
			}
			
			builder.setResult(status.ordinal());
			builder.setUpdateProp(newPropData.toXinqiPropData(user));
			//Update the bag
			bag.markChangeFlag(equipPew);
			for ( PropData propData : stonePropDatas ) {
				builder.addOtherPews(propData.getPew());
				bag.removeOtherPropDatas(propData.getPew());
			}
			GameContext.getInstance().getUserManager().saveUserBag(user, false);
			
			if ( status == ForgeStatus.SUCCESS ) {
				//update user prop
				GameContext.getInstance().writeResponse(user.getSessionKey(), user.toBseRoleBattleInfo());
			}
		} else {
			builder.setResult(status.ordinal());
		}
		
		GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
		GameContext.getInstance().writeResponse(user.getSessionKey(), user.toBseRoleBattleInfo(true));
	}
	
	/**
	 * 玩家请求将源武器的属性转移到目标武器之上
	 * 
	 * @param srcEquipPew
	 * @param tarEquipPew
	 * @return
	 */
	public TransferStatus transferEquip(final User user, final int srcEquipPew, final int tarEquipPew) {
		TransferStatus status = TransferStatus.SUCCESS;
		
		Bag bag = user.getBag();
		PropData srcData = null;
		PropData targetData = null;
		PropData newPropData = null;
		boolean srcIsWeared = false;
		boolean targetIsWeared = false;

		if ( srcEquipPew >= Bag.BAG_WEAR_COUNT ) {
			srcData = bag.getOtherPropData(srcEquipPew);
		} else if (srcEquipPew>=0) {
			srcData = bag.getWearPropDatas().get(srcEquipPew);
			srcIsWeared = true;
		}
		if ( tarEquipPew>= Bag.BAG_WEAR_COUNT ) {
			targetData = bag.getOtherPropData(tarEquipPew);
		} else if ( tarEquipPew >= 0 ) {
			targetData = bag.getWearPropDatas().get(tarEquipPew);
			targetIsWeared = true;
		}

		if ( srcData == null || targetData == null ) {
			status = TransferStatus.FAILURE;
			logger.debug("Cannot find srcData or targetData");
		}
		if ( status == TransferStatus.SUCCESS ) {
			final PropData finalSrcData = srcData;
			final PropData finalTargetData = targetData;
			final boolean finalSrcIsWeared = srcIsWeared;
			final boolean finalTargetIsWeared = targetIsWeared;
			int srcStrLevel = srcData.getLevel();
			int targetStrLevel = targetData.getLevel();
			int srcMaxStrLevel = srcData.getMaxLevel();
			int targetMaxStrLevel = targetData.getMaxLevel();
			int maxLevel = Math.min(srcMaxStrLevel, targetMaxStrLevel);
			if ( srcStrLevel > targetMaxStrLevel ) {
				String message = Text.text("strength.transfer.outrange", 
						targetData.getName(), targetMaxStrLevel, srcStrLevel, targetMaxStrLevel);
				ConfirmManager.getInstance().sendConfirmMessage(user, 
						message, "strength.confirm", new ConfirmCallback() {
					
					@Override
					public void callback(User user, int selected) {
						// TODO Auto-generated method stub
						doTransfer(user, srcEquipPew, tarEquipPew, finalSrcData,
								finalTargetData, finalSrcIsWeared, finalTargetIsWeared);
						
					}
				});
			} else if ( targetStrLevel > srcMaxStrLevel ) {
				String message = Text.text("strength.transfer.outrange", srcData.getName(), srcMaxStrLevel, targetStrLevel, srcMaxStrLevel);
				ConfirmManager.getInstance().sendConfirmMessage(user, 
						message, "strength.confirm", new ConfirmCallback() {
					
					@Override
					public void callback(User user, int selected) {
						// TODO Auto-generated method stub
						doTransfer(user, srcEquipPew, tarEquipPew, finalSrcData,
								finalTargetData, finalSrcIsWeared, finalTargetIsWeared);
						
					}
				});
			} else {
				status = doTransfer(user, srcEquipPew, tarEquipPew, srcData,
					targetData, srcIsWeared, targetIsWeared);
			}
		}
		
		return status;
	}

	/**
	 * @param user
	 * @param srcEquipPew
	 * @param tarEquipPew
	 * @param status
	 * @param srcData
	 * @param targetData
	 * @param srcIsWeared
	 * @param targetIsWeared
	 * @return
	 */
	public TransferStatus doTransfer(final User user, int srcEquipPew,
			int tarEquipPew, PropData srcData,
			PropData targetData, boolean srcIsWeared, boolean targetIsWeared) {
		TransferStatus status = TransferStatus.SUCCESS;
		//Check the price first
		ArrayList priceResult = transferEquipPriceAndRatio(user, srcEquipPew, tarEquipPew);
		int price = (Integer)priceResult.get(0);
		
		boolean hasEnoughMoney = user.getGolden()>=price;
		if (!hasEnoughMoney) {
			status = TransferStatus.FAILURE;
			BseTransfer.Builder builder = BseTransfer.newBuilder();
			builder.setResult(status.ordinal());
			builder.setSrcEquip(srcData.toXinqiPropData(user));
			builder.setTarEquip(targetData.toXinqiPropData(user));
			GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
			
			SysMessageManager.getInstance().sendClientInfoMessage(user, "shop.error.nogold", Type.NORMAL);
		} else {
			//Call script
			ScriptResult result = GameContext.getInstance().getScriptManager().
					runScript(ScriptHook.CRAFT_TRANSFER_EQUIP, user, 
							new Object[]{srcData, targetData, price});
			srcData.setTotalGolden(srcData.getTotalGolden()+price);
			targetData.setTotalGolden(targetData.getTotalGolden()+price);
			/**
			 * Update the whole basic prop
			 * wangqi 2012-11-12
			 */
			//update user power if the equip is wearing
			/*
			if ( srcIsWeared ) {
				user.getBag().recalculateUserProperties(srcData, true);
			}
			if ( targetIsWeared ) {
				user.getBag().recalculateUserProperties(targetData, true);
			}
			*/
			if ( srcIsWeared || targetIsWeared ) {
				UserCalculator.updateUserBasicProp(user);
				UserManager.getInstance().saveUser(user, false);
			}
		}
		return status;
	}
	
}
