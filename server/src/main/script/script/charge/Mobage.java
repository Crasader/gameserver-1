package script.charge;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xinqihd.sns.gameserver.GameContext;
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
public class Mobage {
	
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final Logger logger = LoggerFactory.getLogger(Mobage.class);
	private static final Logger BILLING_LOGGER = LoggerFactory.getLogger("BILLING");

	/**
	 * 
	 * @param parameters: User, ItemPojo, Pew(in user bag)
	 * @return
	 */
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		String request = (String)parameters[0];
		String userName = (String)parameters[1];
		
		boolean success = false;
		logger.info("post charge content:{}, userName", request, userName);
		
		Map map = (Map)JSON.parse(request);
		/**
		 * {"published":"2013-02-21T04:21:33","comment":"","id":"06107D20-07FF-3F54-8C84-DAB8D9FD6991",
		 * "updated":"2013-02-21T04:21:35",
		 * "items":[{"item":{"imageUrl":"","name":"100元宝","id":"1","price":10,"description":""},
		 * "quantity":1}],"state":"open"}
		 */
		/**
		 * 交易结果:
		 * 0 – 交易成功
		 * 1 – 交易失败
		 */
		int chargeResult = 0;
		/**
		 * 计费支付平台的交易订单号
		 */
		String transid = String.valueOf(map.get("id"));
		/**
		 * 交易时间格式: yyyy-mm-dd hh24:mi:ss
		 */
		String transtime = String.valueOf(DateUtil.formatDateTime(new Date()));
		/**
		 * 本次交易的金额,单位:元
		 */
		JSONArray items = (JSONArray)map.get("items");
		int money = 0;
		int boughtYuanbao = 0;
		/**
		 * 本此购买数量
		 */
		int count = 0;
		if ( items != null && items.size() > 0 ) {
			JSONObject itemObj = (JSONObject)items.get(0);
			JSONObject item = (JSONObject)itemObj.get("item");
			money = StringUtil.toInt(String.valueOf(item.get("price")), 0);
			count = StringUtil.toInt(String.valueOf(itemObj.get("quantity")), 0);
			boughtYuanbao = Math.round(money * 10);
		}

		{
			User user = null;
			String userIdStr = userName;
//			try {
//				userIdStr = URLDecoder.decode(userName, Constant.ENC_UTF8);
//			} catch (UnsupportedEncodingException e1) {
//			}
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
						userName, "", transtime, 0, 0, "unknown", 0, 0, 0, boughtYuanbao, 
						StringUtil.concat(new Object[]{"not found user:", userName})});
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
						ChargeManager.getInstance().doCharge(userSessionKey, user, transid, null, money, "mobage", true);									
					} else {
						logger.info("Proxy charge request to remote server {}", gameServerId);
						BceChargeInternal.Builder bceCharge = BceChargeInternal.newBuilder();
						bceCharge.setOrderid(transid);
						bceCharge.setUserid(user.get_id().toString());
						bceCharge.setFreecharge(true);
						bceCharge.setChargemoney(Math.round(money));
						bceCharge.setChannel("mobage");
						GameContext.getInstance().proxyToRemoteGameServer(userSessionKey, gameServerId, bceCharge.build());
					}
				} else {
					ChargeManager.getInstance().doCharge(null, user, transid, null, money, "mobage", true);
				}
			}
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
