package com.xinqihd.sns.gameserver.handler;

import java.util.ArrayList;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.MailMessageManager;
import com.xinqihd.sns.gameserver.entity.user.BasicUser;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceMailSend.BceMailSend;
import com.xinqihd.sns.gameserver.proto.XinqiBseMailSend.BseMailSend;
import com.xinqihd.sns.gameserver.proto.XinqiMailData.MailData;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * The BceBagTidyHandler is used for protocol BagTidy 
 * @author wangqi
 *
 */
public class BceMailSendHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceMailSendHandler.class);
	
	private static final BceMailSendHandler instance = new BceMailSendHandler();
	
	private BceMailSendHandler() {
		super();
	}

	public static BceMailSendHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceMailSend");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceMailSend mailSend = (BceMailSend)request.payload;
		User user = null;
		if ( sessionKey != null ) {
			user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		}

		MailData mail = mailSend.getMail();
		String toUserName = mail.getTouser();
		BasicUser toUser = UserManager.getInstance().queryBasicUserByRoleName(toUserName);
		boolean success = false;
		if ( toUser != null ) {
			ArrayList<Reward> gifts = null;
			if ( user != null ) {
				int giftCount = mail.getGiftpewsCount();
				if ( giftCount > 0 ) {
					gifts = new ArrayList<Reward>();
					for ( int i=0; i<giftCount; i++ ) {
						int pew = mail.getGiftpews(i);
						PropData propData = user.getBag().getOtherPropData(pew);
						if ( propData != null ) {
							Reward reward = new Reward();
							reward.setId(propData.getItemId());
							reward.setPropId(propData.getItemId());
							reward.setPropColor(propData.getWeaponColor());
							reward.setPropCount(1);
							reward.setPropIndate(propData.getPropIndate());
							reward.setPropLevel(propData.getLevel());
							gifts.add(reward);
						}
					}
				}
				success = MailMessageManager.getInstance().sendMail(user.get_id(), toUser.get_id(), 
						mail.getSubject(), mail.getContent(), gifts, false);
			} else {
				//From admin user
				int giftCount = mail.getGiftsCount();
				if ( giftCount > 0 ) {
					gifts = new ArrayList<Reward>();
					for ( int i=0; i<giftCount; i++ ) {
						gifts.add(Reward.fromGift(mail.getGifts(i)));
					}
				}
				success = MailMessageManager.getInstance().sendMail(null, toUser.get_id(), 
						mail.getSubject(), mail.getContent(), gifts, true);
			}
			
			BseMailSend.Builder bseMailSend = BseMailSend.newBuilder();
			bseMailSend.setSucceed(success);
			GameContext.getInstance().writeResponse(sessionKey, bseMailSend.build());

			if ( user != null ) {
				MailMessageManager.getInstance().checkSendBox(user, null);
			}
			
			StatClient.getIntance().sendDataToStatServer(
					user, StatAction.MailSend, toUserName, mail.getSubject(), mail.getContent(), success);
		} else {
			logger.info("BceMailSend: Cannot find {} user", toUserName);
		}
	}
	
	
}
