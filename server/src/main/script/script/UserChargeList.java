package script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.mongo.ChargeManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseChargeList.BseChargeList;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * 登陆时获取用户充值的信息列表
 * 
 * @author wangqi
 *
 */
public class UserChargeList {
	
	private static final int[] TAB_INDEX = new int[]{
			0, //中国移动
			1, //网银
			2, //联通
			3, //电信
	};
	
	private static final String[] defaultBillingChannels = new String[]{
		"cmcc_sms", "default",
	};
	
	private static final Logger logger = LoggerFactory.getLogger(UserChargeList.class);

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		String channel = (String)parameters[1];
		
		XinqiMessage chargeData = new XinqiMessage();
		
		String billingChannel = "ios_iap";
		if ( StringUtil.checkNotEmpty(channel) ) {
			if ( channel.contains("cmcc") ) {
				billingChannel = "cmcc_sms";
			} else if ( channel.contains("kupai") ) {
				billingChannel = "kupai";
			} else if ( channel.contains("tianyu") ) {
				billingChannel = "tianyu";
			} else if ( channel.contains("xiaomi") ) {
				billingChannel = "xiaomi";
			} else if ( channel.contains("weiyun") ) {
				billingChannel = "weiyun";
			} else if ( channel.contains("mobilemarket") ) {
				billingChannel = "mobilemarket";
			} else if ( channel.contains("appstore_xinqi") ) {
				billingChannel = "appstore_xinqi";
			} else if ( channel.contains("appstore") ) {
				billingChannel = "ios_iap";
			} else if ( channel.contains("@channel@") ) {
				if ( user.getClient() != null ) {
					if ( user.getClient().startsWith("iP") ) {
						logger.info("Readjust the channel for user:{}, client:{}, channel:{}", 
								new Object[]{user.getRoleName(), user.getClient(), user.getChannel()});
						billingChannel = "ios_iap";
					}
				}
			} else if ( channel.contains("oppo") ) {
				billingChannel = "oppo";
			} else if ( channel.contains("huawei") ) {
				billingChannel = "huawei";
			} else if ( channel.contains("dangle") ) {
				billingChannel = "dangle";
			} else {
				billingChannel = "default";
			}
		}
		System.out.println("UserChargeList: channel:"+channel+", billingChannel:"+billingChannel);
		/*
		if ( isVip ) {
			float chargeDiscount = (float)GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.VIP_CHARGE_DISCOUNT, 8.0);
			chargeData.payload = ChargeManager.getInstance().toBseChargeList(cu, chargeDiscount);
			logger.debug("User {} is VIP and charging will be discounted to {}", user.getRoleName(), chargeDiscount);
		} else {
			chargeData.payload = ChargeManager.getInstance().toBseChargeList(cu);
		}
		*/
		logger.debug("Check charge list for user {} of channel {}", new Object[]{user.getRoleName(), billingChannel});
		BseChargeList.Builder builder = BseChargeList.newBuilder();
		if ( channel.contains("general") ) {
			for (int i = 0; i < defaultBillingChannels.length; i++) {
				String bchannel = defaultBillingChannels[i];
				builder.addCharges(ChargeManager.getInstance().toBseChargeList(bchannel, true, TAB_INDEX[i]));
				GameContext.getInstance().writeResponse(user.getSessionKey(), chargeData);
			}
		} else {
			//defaut charge tab id: 1
			builder.addCharges(ChargeManager.getInstance().toBseChargeList(billingChannel, true, TAB_INDEX[1]));	
		}
		chargeData.payload = builder.build();
		GameContext.getInstance().writeResponse(user.getSessionKey(), chargeData);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
	
}
