package script.charge;

import static com.xinqihd.sns.gameserver.util.CommonUtil.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.AccountManager;
import com.xinqihd.sns.gameserver.db.mongo.ChargeManager;
import com.xinqihd.sns.gameserver.entity.user.Account;
import com.xinqihd.sns.gameserver.entity.user.User;
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
public class Baoruan {
	
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final Logger logger = LoggerFactory.getLogger(Baoruan.class);
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
		logger.info("post charge content:{}", request);
		HashMap map = parseHttpParams(request);

		String cid = String.valueOf(map.get("cid"));
		String appid = String.valueOf(map.get("appid"));
		//Lewan 系统关于充值信息的订单号
		String order = String.valueOf(map.get("order"));
		/**
		 * 宝软渠道的UID是宝软的系统UID，他对应到Account的username
		 * 所以需要用Account查找充值的游戏账号
		 */
		String uid = String.valueOf(map.get("uid"));
		String accountName = uid;
		//充值的金额 Lewan币 1:1
		float money = Float.parseFloat(String.valueOf(map.get("amount")));
		int boughtYuanbao = Math.round(money * 10);
		int timestamp = StringUtil.toInt(String.valueOf(map.get("create")), 0);
		Date chargeDate = new Date(timestamp*1000l);
		String chargeDateStr = SDF.format(chargeDate);

		Account account = AccountManager.getInstance().queryAccountByName(accountName);
		if ( account == null ) {
			success = false;
			logger.warn("Failed to find user by accountId: {}", accountName);
			BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
					accountName, "", chargeDateStr, 0, 0, "unknown", 0, 0, 0, boughtYuanbao, 
					StringUtil.concat(new Object[]{"not found user:", accountName})});
		} else {
			String roleName = AccountManager.getInstance().getCurrentRoleName(account);
			if ( roleName == null ) {
				logger.info("Failed to find user by roleName: {}", roleName);
				BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
						accountName, "", chargeDateStr, 0, 0, "unknown", 0, 0, 0, boughtYuanbao, 
						StringUtil.concat(new Object[]{"not found user:", accountName})});
			} else {
				User user = UserManager.getInstance().queryUserByRoleName(roleName);
				if ( user == null ) {
					logger.info("Failed to find user by roleName: {}", roleName);
					user = UserManager.getInstance().queryUser(roleName);
				}
				if ( user == null ) {
					logger.warn("Failed to find user by userName: {}", roleName);
					BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
							roleName, "", chargeDateStr, 0, 0, "unknown", 0, 0, 0, boughtYuanbao, 
							StringUtil.concat(new Object[]{"not found user:", roleName})});
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
							ChargeManager.getInstance().doCharge(userSessionKey, user, order, null, money, "baoruan", true);									
						} else {
							logger.info("Proxy charge request to remote server {}", gameServerId);
							BceChargeInternal.Builder bceCharge = BceChargeInternal.newBuilder();
							bceCharge.setUserid(user.get_id().toString());
							bceCharge.setFreecharge(true);
							bceCharge.setChargemoney(Math.round(money));
							bceCharge.setChannel("baoruan");
							bceCharge.setOrderid(order);
							GameContext.getInstance().proxyToRemoteGameServer(userSessionKey, gameServerId, bceCharge.build());
						}
					} else {
						ChargeManager.getInstance().doCharge(null, user, order, null, money, "baoruan", true);
					}
				}
			}
		}
		
		//Send response to client
		HttpMessage response = new HttpMessage();
		if ( success ) {
			response.setResponseContentType(HttpGameHandler.CONTENT_PLAIN_TYPE);
			response.setResponseContent("1".getBytes());
		} else {
			response.setResponseContentType(HttpGameHandler.CONTENT_PLAIN_TYPE);
			response.setResponseContent("2002".getBytes());
		}
		ArrayList list = new ArrayList();
		list.add(response);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
}
