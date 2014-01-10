package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.payment.IPaymentCard;
import com.xinqihd.payment.PaymentCardShenZhouFu;
import com.xinqihd.payment.PaymentCardYeepay;
import com.xinqihd.payment.yeepay.PaymentResult;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.db.mongo.ChargeManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceChargeCard.BceChargeCard;
import com.xinqihd.sns.gameserver.proto.XinqiBseChargeCard.BseChargeCard;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * 
 * @author wangqi
 *
 */
public class BceChargeCardHandler extends SimpleChannelHandler {

	private Logger logger = LoggerFactory.getLogger(BceChargeCardHandler.class);
	
	private static final BceChargeCardHandler instance = new BceChargeCardHandler();
	
	private BceChargeCardHandler() {
		super();
	}

	public static BceChargeCardHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceChargeCard");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		BceChargeCard chargeCard = (BceChargeCard)request.payload;
		String cardNo = chargeCard.getCardno();
		String cardPwd = chargeCard.getCardpwd();
		String cardType = chargeCard.getCardtype();
		int cardMoney = chargeCard.getMoney();

		/**
	   <UserString key="card" value="junwang" />
		 <UserString key="card" value="zhengtu" />
		 <UserString key="card" value="wangyi" />
		 <UserString key="card" value="shengda" />
		 <UserString key="card" value="qbei" />
		 
		 <UserString key="card" value="liantong" />
		 <UserString key="card" value="shengzhouxing" />
		 <UserString key="card" value="dianxing" />
		 */
		boolean useShenZhouFu = false;
		boolean useYeepay = false;
		int type = 0;
		if ( "shengzhouxing".equals(cardType) ) {
			useShenZhouFu = true;
			type = PaymentCardShenZhouFu.CARD_TYPE_CHINA_MOBLIE;
	  } else if ( "liantong".equals(cardType) ) {
	  	useShenZhouFu = true;
			type = PaymentCardShenZhouFu.CARD_TYPE_CHINA_UNITED_TELECOM;
		} else if ( "dianxing".equals(cardType) ) {
			useShenZhouFu = true;
			type = PaymentCardShenZhouFu.CARD_TYPE_CHINA_TELECOM;
		} else if ( "junwang".equals(cardType) ) {
			cardType = "JUNNET";
			useYeepay = true;
		} else if ( "zhengtu".equals(cardType) ) {
			cardType = "ZHENGTU";
			useYeepay = true;
		} else if ( "wangyi".equals(cardType) ) {
			cardType = "NETEASE";
			useYeepay = true;
		} else if ( "shengda".equals(cardType) ) {
			cardType = "SNDACARD";
			useYeepay = true;
		} else if ( "qbei".equals(cardType) ) {
			cardType = "QQCARD";
			useYeepay = true;
		} else if ( "JIUYOU".equals(cardType) ) {
			cardType = "JIUYOU";
			useYeepay = true;
		} else if ( "YPCARD".equals(cardType) ) {
			cardType = "YPCARD";
			useYeepay = true;
		} else if ( "WANMEI".equals(cardType) ) {
			cardType = "WANMEI";
			useYeepay = true;
		} else if ( "SOHU".equals(cardType) ) {
			cardType = "WANMEI";
			useYeepay = true;
		} else if ( "ZONGYOU".equals(cardType) ) {
			cardType = "ZONGYOU";
			useYeepay = true;
		} else if ( "TIANXIA".equals(cardType) ) {
			cardType = "TIANXIA";
			useYeepay = true;
		} else if ( "TIANHONG".equals(cardType) ) {
			cardType = "JUNNET";
			useYeepay = true;
		} else {
			String text = Text.text("chargecard.no.type", cardType);
			SysMessageManager.getInstance().sendClientInfoRawMessage(session, text, Action.NOOP, Type.NORMAL);
		}
		if ( useShenZhouFu ) {
			PaymentResult result = null;
			try {
				PaymentCardShenZhouFu shenZhouFu = new PaymentCardShenZhouFu(cardNo, cardPwd, cardMoney, type);
				result = shenZhouFu.doPost(user.getUsername());
				
				boolean success = false;
				String msg = PaymentCardShenZhouFu.getRespMessage(result.code);
				if ( "200".equals(result.code) ) {
					success = true;
				}

				SysMessageManager.getInstance().sendClientInfoRawMessage(sessionKey, msg, 3000);

				BseChargeCard.Builder builder = BseChargeCard.newBuilder();
				builder.setSuccess(success);
				builder.setDesc(msg);
				
				GameContext.getInstance().writeResponse(session, builder.build(), sessionKey);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if ( result != null ) {
				logger.info("Charge cardno:{}, cardPwd:{}, cardType:{}, result.code:{}, result:msg:{}", 
					new Object[]{cardNo, cardPwd, cardType, result.code, result.msg});
			} else {
				logger.info("Charge cardno:{}, cardPwd:{}, cardType:{}, result.code:{}, result:msg:{}", 
						new Object[]{cardNo, cardPwd, cardType, Constant.EMPTY, Constant.EMPTY});
			}
		}	else if ( useYeepay ) {
			PaymentCardYeepay payment =  new PaymentCardYeepay(cardNo, cardPwd, cardMoney);
			payment.setAmt(cardMoney);
			payment.setChannelID(cardType);
			payment.setOrderID(StringUtil.concat(System.currentTimeMillis(), user.getUsername()));
			PaymentResult result = payment.doPost(user.getUsername());
			//提交状态，“1”代表提交成功，非“1”代表提交失败：
			boolean success = false;
			if ( Constant.ONE.equals(result.code) ) {
				SysMessageManager.getInstance().sendClientInfoMessage(sessionKey, "chargecard.inprogress", Type.NORMAL);
			} else {
				if ( StringUtil.checkNotEmpty(result.msg) ) {
					SysMessageManager.getInstance().sendClientInfoRawMessage(sessionKey, result.msg, 3000);
				} else {
					SysMessageManager.getInstance().sendClientInfoMessage(sessionKey, "chargecard.error", Type.NORMAL);
				}
			}
			
			BseChargeCard.Builder builder = BseChargeCard.newBuilder();
			builder.setSuccess(success);
			
			GameContext.getInstance().writeResponse(session, builder.build(), sessionKey);
		}

	}
	
}
