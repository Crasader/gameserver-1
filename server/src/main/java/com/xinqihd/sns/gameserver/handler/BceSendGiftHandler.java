package com.xinqihd.sns.gameserver.handler;

import java.util.ArrayList;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.MailMessageManager;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBceSendGift.BceSendGift;
import com.xinqihd.sns.gameserver.proto.XinqiGift.Gift;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * The BceInitHandler is used for protocol Init 
 * @author wangqi
 *
 */
public class BceSendGiftHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceSendGiftHandler.class);
	
	private static final BceSendGiftHandler instance = new BceSendGiftHandler();
	
	private static final String GAME_ADMIN = "GameAdmin";
	
	private BceSendGiftHandler() {
		super();
	}

	public static BceSendGiftHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceSendGift");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		
		BceSendGift bceGift = (BceSendGift)request.payload;
		int giftCount = bceGift.getGiftCount();
		
		ArrayList<Reward> rewards = new ArrayList<Reward>();
		StringBuilder buf = new StringBuilder(30);
		for ( int i=0; i<giftCount; i++ ) {
			Gift gift = bceGift.getGift(i);
			Reward reward = Reward.fromGift(gift);
			rewards.add(reward);
			switch ( reward.getType() ) {
				case ITEM:
				case STONE:
					ItemPojo item = ItemManager.getInstance().getItemById(reward.getId());
					if ( item != null ) {
						buf.append(item.getName());
					}
					break;
				case WEAPON:
					WeaponPojo weapon = EquipManager.getInstance().
						getWeaponByTypeNameAndUserLevel(reward.getTypeId(), 1);
					if ( weapon != null ) {
						buf.append(weapon.getName().substring(3));
					}
					break;
				case EXP:
					buf.append(Text.text("gift.exp", reward.getPropCount()));
					break;
				case GOLDEN:
					buf.append(Text.text("gift.golden", reward.getPropCount()));
					break;
				case YUANBAO:
					buf.append(Text.text("gift.yuanbao", reward.getPropCount()));
					break;
			}
			buf.append(",");
		}
		if ( rewards.size() <= 0 ) return;
		buf.deleteCharAt(buf.length()-1);
		
		boolean isAdmin = false;
		String fromUserName = bceGift.getFromUserName();
		UserId fromUserId = null;
		if ( GAME_ADMIN.equals(fromUserName) ) {
			isAdmin = true;
			fromUserName = null;
		} else {
			if ( sessionKey != null ) {
				fromUserId = GameContext.getInstance().findUserIdBySessionKey(sessionKey);
			}
		}
		UserId toUserId = UserId.fromString(bceGift.getToUserIdStr());
		String content = Text.text("gift", buf.toString());
		MailMessageManager.getInstance().sendMail(
				fromUserId, toUserId, null, content, rewards, isAdmin);

//		BseSendGift.Builder builder = BseSendGift.newBuilder();
//		response.payload = builder.build();
//		GameContext.getInstance().writeResponse(session, response, sessionKey);
	}
	
	
}
