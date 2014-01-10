package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.guild.Guild;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.proto.XinqiBcePropDataQuery.BcePropDataQuery;
import com.xinqihd.sns.gameserver.proto.XinqiBsePropDataQuery.BsePropDataQuery;
import com.xinqihd.sns.gameserver.proto.XinqiGift.Gift;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.reward.RewardType;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * 
 * @author wangqi
 *
 */
public class BcePropDataQueryHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BcePropDataQueryHandler.class);
	
	private static final BcePropDataQueryHandler instance = new BcePropDataQueryHandler();
	
	private BcePropDataQueryHandler() {
		super();
	}

	public static BcePropDataQueryHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BcePropDataQuery");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		BcePropDataQuery propDataQuery = (BcePropDataQuery)request.payload;
		String giftStr = propDataQuery.getGiftstr();
		Gift gift = propDataQuery.getGift();
		String weaponId = propDataQuery.getWeaponid();
		int pew = propDataQuery.getPew();
		/**
		 * idStr 有可能为用户id，或者公会的背包id，需要进行判断
		 */
		String idStr = propDataQuery.getUserid();
		Reward reward = null;
		
		if ( StringUtil.checkNotEmpty(weaponId) ) {
			WeaponPojo weapon = EquipManager.getInstance().getWeaponById(weaponId);
			if ( weapon != null ) {
				PropData propData = weapon.toPropData(30, WeaponColor.WHITE, 12, null);
				
				BsePropDataQuery.Builder builder = BsePropDataQuery.newBuilder();
				builder.setPropData(propData.toXinqiPropDataDesc(user));
				GameContext.getInstance().writeResponse(sessionKey, builder.build());
				return; 
			}
		} else {
			PropData propData = null;
			if ( pew >= 0 ) {
				Bag targetUserBag = null;
				if ( StringUtil.checkNotEmpty(idStr) ) {
					UserId targetUserId = UserId.fromString(idStr);
					if ( targetUserId != null ) {
						SessionKey targetSessionKey = GameContext.getInstance().findSessionKeyByUserId(targetUserId);
						if ( targetSessionKey != null ) {
							User targetUser = GameContext.getInstance().findGlobalUserBySessionKey(targetSessionKey);
							if ( targetUser != null ) {
								targetUserBag = targetUser.getBag();
							}
						}
						if ( targetUserBag == null ) {
							targetUserBag = UserManager.getInstance().queryUserBag(targetUserId);
						}
						if ( targetUserBag == null ) {
							targetUserBag = user.getBag();
						}
					} else {
						Guild guild = user.getGuild();
						propData = GuildManager.getInstance().queryGuildBagPropData(idStr, String.valueOf(pew));
					}
				} else {
					targetUserBag = user.getBag();
				}
				if ( propData == null && targetUserBag != null ) {
					if ( pew < Bag.BAG_WEAR_COUNT ) {
						propData = targetUserBag.getWearPropDatas().get(pew);
					} else {
						propData = targetUserBag.getOtherPropData(pew);
					}
				}
			} else {
				if ( StringUtil.checkNotEmpty(giftStr) ){
					reward = Reward.fromString(giftStr);	
				} else {
					reward = Reward.fromGift(gift);				
				}
				
				if ( reward != null ) {
					if ( reward.getType() == RewardType.ITEM || reward.getType() == RewardType.STONE ) {
						propData = RewardManager.getInstance().convertRewardItemToPropData(reward);
					} else if ( reward.getType() == RewardType.WEAPON ) {
						propData = RewardManager.getInstance().convertRewardWeaponToPropData(reward, user);
						propData.setReward(true);
					}
				} else {
					logger.info("The giftStr or gift is both not valid:{}",giftStr);
				}
			}
			
			if ( propData != null ) {
				BsePropDataQuery.Builder builder = BsePropDataQuery.newBuilder();
				builder.setPropData(propData.toXinqiPropDataDesc(user));
				GameContext.getInstance().writeResponse(sessionKey, builder.build());
				return;
			}
		}
		
		BsePropDataQuery.Builder builder = BsePropDataQuery.newBuilder();
		GameContext.getInstance().writeResponse(sessionKey, builder.build());
	}
	
}
