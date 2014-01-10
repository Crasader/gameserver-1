package com.xinqihd.sns.gameserver.reward;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleInfo.BseRoleInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBseUseProp.BseUseProp;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;

public class Box {
	
	private static final Logger logger = LoggerFactory.getLogger(Box.class);
	
	/**
	 * Pickup the rewards and delete the box from user's bag.
	 * Send the changed data back to client.
	 * 
	 * @param user
	 * @param rewards
	 * @param pew
	 */
	public static final PickRewardResult openBox(User user, Collection rewards, int pew) {
		//Remove the item from user's bag
		PickRewardResult pickResult = PickRewardResult.NOTHING;

		Bag bag = user.getBag();
		PropData propData = bag.getOtherPropData(pew);
		if ( propData == null ) {
			return pickResult;
		}
		
		String itemId = propData.getItemId();
		ItemPojo itemPojo = ItemManager.getInstance().getItemById(itemId);
		
		//add reward condition
		boolean reachCondition = true;
		HashMap<String, Integer> totalCountMap = new HashMap<String, Integer>();  
		ArrayList<PropData> condPropData = new ArrayList<PropData>();
		ArrayList<RewardCondition> conditions = itemPojo.getConditions();
		if ( conditions != null && conditions.size() > 0 ) {
			reachCondition = false;
			for (RewardCondition rc : conditions ) {
				String id = rc.getId();
				int count = rc.getCount();
				totalCountMap.put(id, count);
				ItemPojo condItemPojo = ItemManager.getInstance().getItemById(id);
				if ( condItemPojo != null ) {
					List<PropData> list = bag.getOtherPropDatas();
					for ( PropData pd : list ) {
						if ( pd == null ) continue;
						if ( id.equals(pd.getItemId()) ) {
							condPropData.add(pd);
							count -= pd.getCount();
							if ( count <= 0 ) {
								reachCondition = true;
								break;
							}
						}
					}
				} else {
					logger.warn("#openItemBox: failed to find condition item pojo {} for item {}", 
							id, itemPojo.getName());
				}
			}
		}

		if ( !reachCondition ) {
			RewardCondition condition = conditions.get(conditions.size()-1);
			ItemPojo condItemPojo = ItemManager.getInstance().getItemById(condition.getId());
			SysMessageManager.getInstance().sendClientInfoMessage(
					user, "box.not_enough_key", Action.NOOP, 
					new Object[]{
							itemPojo.getName(), 
							condition.getCount(),
							condItemPojo!=null?condItemPojo.getName():Constant.EMPTY
							});
			
			return PickRewardResult.CONDITION_FAIL;
		}

		pickResult = RewardManager.getInstance().pickRewardWithResult(
				user, rewards, StatAction.ProduceOpenBox);
		
		ArrayList<Integer> keyPews = new ArrayList<Integer>(); 
		if ( pickResult == PickRewardResult.SUCCESS ) {
			//subtract the propdata condition
			for ( PropData pd : condPropData ) {
				Integer totalCountInt = totalCountMap.get(pd.getItemId());
				if ( totalCountInt!=null ) {
					int totalCount = totalCountInt.intValue();
					while ( totalCount-- > 0 ) {
						keyPews.add(pd.getPew());
						bag.removeOtherPropDatas(pd.getPew());
						if ( pd.getPew() == -1 ) {
							//The pd is deleted.
							break;
						}
					}
				} else {
					logger.warn("Failed to find totalCount for id{}", pd.getItemId());
				}
			}
			
			//Remove the box item
			bag.removeOtherPropDatas(pew);

			//Save user and user's bag to database.
			GameContext.getInstance().getUserManager().saveUser(user, false);
			GameContext.getInstance().getUserManager().saveUserBag(user, false);
			
			//Notify client user's role data is changed.
			//Send the data back to client
			BseRoleInfo roleInfo = user.toBseRoleInfo();
			GameContext.getInstance().writeResponse(user.getSessionKey(), roleInfo);
			
			XinqiMessage response = new XinqiMessage();
			BseUseProp.Builder builder = BseUseProp.newBuilder();
			builder.setSuccessed(pickResult.ordinal());
			builder.addDelPew(pew);
			for ( int condPew : keyPews ) {
				builder.addDelPew(condPew);
				logger.debug("condPew: {}", condPew);
			}
			response.payload = builder.build();
			
			GameContext.getInstance().writeResponse(user.getSessionKey(), response);
		}

		return pickResult;
	}

}
