package script.charge;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.AccountManager;
import com.xinqihd.sns.gameserver.db.mongo.ChargeManager;
import com.xinqihd.sns.gameserver.entity.user.Account;
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
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * 完成畅游爱贝渠道的计费
 * 
 * @author wangqi
 *
 */
public class UC {
	
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final Logger logger = LoggerFactory.getLogger(UC.class);
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
		boolean success = false;
		logger.info("uc post charge content:{}", request);
		Map allMap = (Map)JSON.parse(request);
		Map map = (Map)allMap.get("data");
		/**
			{
			    "data":{
			       "orderId":"abcf1330",
			       "gameId":123,
			       "serverId":654,
			       "ucid":123456,
			       "payWay":1,
			       "amount":"100.00",
			       "callbackInfo":"custominfo=xxxxx",
			       "orderStatus":"S",
			       "failedDesc":""
			},
			    "sign":"e49bd00c3cf0744c7049e73e16ae8acd"
			}
		 */
		/**
		 * 交易结果:S-成功支付 F-支付失败
		 */
		String orderStatus = String.valueOf(map.get("orderStatus"));
		/**
		 * UC 分配 
		 */
		String orderId = String.valueOf(map.get("orderId"));
		String gameId = String.valueOf(map.get("gameId"));
		String serverId = String.valueOf(map.get("serverId"));
		String ucid = String.valueOf(map.get("ucid"));
		/**
		 * 支付通道代码
		 */
		String payWay = String.valueOf(map.get("payWay"));
		/**
		 * 如果是成功支付,则为空串
		 */
		String failedDesc = String.valueOf(map.get("failedDesc"));
		/**
		 * 交易时间格式: yyyy-mm-dd hh24:mi:ss
		 */
		String transtime = SDF.format(new Date());
		/**
		 * 本次交易的金额,单位:元。
		 */
		float money = Float.parseFloat(String.valueOf(map.get("amount")));
		int boughtYuanbao = Math.round(money * 10);
		/**
		 * 外部透传参数，保存用户昵称
		 */
		String callbackInfo = String.valueOf(map.get("callbackInfo"));
		String accountName = ucid;
		
		if ( "S".equals(orderStatus) ) {
			Account account = AccountManager.getInstance().queryAccountByName(accountName);
			if ( account != null ) {
				UserId userId = account.getLastUserId();
				if ( userId != null ) {
					User user = UserManager.getInstance().queryUser(userId);
					if ( user == null ) {
						logger.warn("Failed to find user by : callbackInfo{}, rolename:{}", callbackInfo, accountName);
						BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
								accountName, "", transtime, 0, 0, "unknown", 0, 0, 0, boughtYuanbao, 
								StringUtil.concat(new Object[]{"not found user:", accountName})});
					} else {
						success = true;
						SessionManager manager = GameContext.getInstance().getSessionManager();
						SessionKey userSessionKey = manager.findSessionKeyByUserId(user.get_id());
						if ( userSessionKey == null ) {
							userSessionKey = BillingJedis.getInstance().findSessionKeyByUserId(user.get_id());
						}
						if ( userSessionKey != null ) {
							//The user is online now
							String gameServerId = manager.findUserGameServerId(userSessionKey);
							if ( GameContext.getInstance().getGameServerId().equals(gameServerId) )  {
								ChargeManager.getInstance().doCharge(userSessionKey, user, orderId, null, money, "uc", true);									
							} else {
								logger.info("Proxy charge request to remote server {}", gameServerId);
								BceChargeInternal.Builder bceCharge = BceChargeInternal.newBuilder();
								bceCharge.setUserid(user.get_id().toString());
								bceCharge.setFreecharge(true);
								bceCharge.setChargemoney(Math.round(money));
								bceCharge.setChannel("uc");
								bceCharge.setOrderid(orderId);
								GameContext.getInstance().proxyToRemoteGameServer(userSessionKey, gameServerId, bceCharge.build());
							}
						} else {
							ChargeManager.getInstance().doCharge(null, user, orderId, null, money, "uc", true);
						}
					}
				} else {
					BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
							accountName, "", transtime, 0, 0, "no lastuserid:"+accountName, 0, 0, 0, boughtYuanbao, failedDesc});
				}
			} else {
				BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
						accountName, "", transtime, 0, 0, "no accountName:"+accountName, 0, 0, 0, boughtYuanbao, failedDesc});
			}
		} else {
			BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
					accountName, "", transtime, 0, 0, "unknown", 0, 0, 0, boughtYuanbao, failedDesc});
		}
		
		//Send response to client
		HttpMessage response = new HttpMessage();
		if ( success ) {
			response.setResponseContentType(HttpGameHandler.CONTENT_PLAIN_TYPE);
			response.setResponseContent("SUCCESS".getBytes());
		} else {
			response.setResponseContentType(HttpGameHandler.CONTENT_PLAIN_TYPE);
			response.setResponseContent("FAILURE".getBytes());
		}
		ArrayList list = new ArrayList();
		list.add(response);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
}
