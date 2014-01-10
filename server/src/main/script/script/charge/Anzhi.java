package script.charge;

import static com.xinqihd.sns.gameserver.util.CommonUtil.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.util.Base64;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.ChargeManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBceChargeInternal.BceChargeInternal;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.session.SessionManager;
import com.xinqihd.sns.gameserver.transport.http.HttpGameHandler;
import com.xinqihd.sns.gameserver.transport.http.HttpMessage;
import com.xinqihd.sns.gameserver.util.BillingJedis;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * 完成畅游爱贝渠道的计费
 * 
 * @author wangqi
 *
 */
public class Anzhi {
	
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final Logger logger = LoggerFactory.getLogger(Anzhi.class);
	private static final Logger BILLING_LOGGER = LoggerFactory.getLogger("BILLING");

	/**
	 * 
	 * @param parameters: User, ItemPojo, Pew(in user bag)
	 * @return
	 */
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if ( result != null ) {
			return result;
		}
		String request = (String)parameters[0];
		logger.info("anzhi charge post content: {}", request);
		
		HashMap map = parseHttpParams(request);
		//支付结果，200表示支付成功，-1表示支付失败
		String orderResult = String.valueOf(map.get("payResult"));
	  //支付金额(单位：元) 
		String amount = String.valueOf(map.get("amount"));
		//安智支付平台订单号
		String orderId = String.valueOf(map.get("orderId"));
	  //扩展字段，存放通过SDK中pay方法传进来的callBackInfo信息，需要用Base64进行decode，编码为UTF-8
		String userName = null;
	  try {
	  	userName = URLDecoder.decode(String.valueOf(map.get("ext")), "utf8");
			userName = new String(Base64.decodeFast(userName), "utf8");
		} catch (UnsupportedEncodingException e1) {
			logger.warn("userName:{}", e1);
		}
		//应用key
		String appkey = String.valueOf(map.get("appkey"));
		/**
		 * 支付方式编号，001充值卡，002支付宝，003银联
		 */
		String payType = String.valueOf(map.get("payType"));

		Date chargeDate = new Date();
		//signStr加密规则 signStr=MD5(appKey+amount+orderId+payResult+ext+msg+appSecret)，appSecret与上面app_secret参数一样
		String sign = String.valueOf(map.get("signStr"));

		String chargeDateStr = DateUtil.formatDateTime(chargeDate);
		String transId = Constant.EMPTY;
		String device = Constant.EMPTY;

		/**
		 * 获取Float表示的钱数
		 */
		boolean success = false;
		float money = Float.parseFloat(amount);
		int boughtYuanbao = Math.round(money * 10);
		try {
			if ( "200".equals(orderResult) ) {
				User user = null;
				String userIdStr = userName;
				try {
					userIdStr = URLDecoder.decode(userName, Constant.ENC_UTF8);
				} catch (UnsupportedEncodingException e1) {
				}
				UserId userId = UserId.fromString(userIdStr);
				if ( userId != null ) {
					user = UserManager.getInstance().queryUser(userId);
				}
				if ( user == null ) {
					logger.info("Failed to find user by userId: {}", userName);
					user = UserManager.getInstance().queryUser(userName);
				}
				if ( user == null ) {
					logger.info("Failed to find user by username: {}", userName);
					user = UserManager.getInstance().queryUserByRoleName(userName);
				}
				if ( user == null ) {
					logger.warn("Failed to find user by all methods: {}", userName);
					BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
							userName, "", chargeDateStr, 0, 0, "unknown", 0, 0, 0, boughtYuanbao, 
							StringUtil.concat(new Object[]{"not found user:", userName})});
				} else {
					success = true;
					SessionManager manager = GameContext.getInstance().getSessionManager();
					SessionKey userSessionKey = manager.findSessionKeyByUserId(user.get_id());
					if ( userSessionKey == null ) {
						userSessionKey = BillingJedis.getInstance().findSessionKeyByUserId(userId);
					}
					if ( userSessionKey != null ) {
						//The user is online now
						String gameServerId = manager.findUserGameServerId(userSessionKey);
						if ( GameContext.getInstance().getGameServerId().equals(gameServerId) )  {
							ChargeManager.getInstance().doCharge(userSessionKey, user, orderId, null, money, "anzhi", true);									
						} else {
							logger.info("Proxy charge request to remote server {}", orderId);
							BceChargeInternal.Builder bceCharge = BceChargeInternal.newBuilder();
							bceCharge.setOrderid(orderId);
							bceCharge.setUserid(user.get_id().toString());
							bceCharge.setFreecharge(true);
							bceCharge.setChargemoney(Math.round(money));
							bceCharge.setChannel("anzhi");
							GameContext.getInstance().proxyToRemoteGameServer(userSessionKey, gameServerId, bceCharge.build());
						}
					} else {
						ChargeManager.getInstance().doCharge(null, user, orderId, null, money, "anzhi", true);
					}
				}
			} else {
				BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
						//orderId, username, device, chargeDateStr, amount, rmb_amount, currency, discount, origYuanbao, boughtYuanbao, 0, "anzhi order fail"
						orderId, transId, device, chargeDateStr, amount, money, "rmb", 0, 0, 0, 0, "anzhi order fail"
				});
			}
		} catch (Exception e) {
			BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
					//orderId, username, device, chargeDateStr, amount, rmb_amount, currency, discount, origYuanbao, boughtYuanbao, 0, "anzhi order fail"
					orderId, transId, device, chargeDateStr, amount, money, "rmb", 0, 0, 0, 0, "exception:".concat(e.getMessage())
			});
			logger.warn("#anzhi: fail to process user charge request", e);
			success = false;
		}
		
		//Send response to client
		//操作结果,0 表示成功,非 0 表示失败
		HttpMessage response = new HttpMessage();
		if ( success ) {
			response.setResponseContentType(HttpGameHandler.CONTENT_PLAIN_TYPE);
			response.setResponseContent("success".getBytes());
		} else {
			response.setResponseContentType(HttpGameHandler.CONTENT_PLAIN_TYPE);
			response.setResponseContent("failure".getBytes());
		}
		ArrayList list = new ArrayList();
		list.add(response);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}

}
