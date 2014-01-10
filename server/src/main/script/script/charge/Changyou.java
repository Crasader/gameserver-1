package script.charge;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
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
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * 完成畅游爱贝渠道的计费
 * 
 * @author wangqi
 *
 */
public class Changyou {
	
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final Logger logger = LoggerFactory.getLogger(Changyou.class);
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
		Map map = (Map)JSON.parse(request);
		/**
		 * {
		 * result=0, feetype=2, transtype=0, 
		 * transid=02112111121173800426, 
		 * transtime=2012-11-11 21:22:39, 
		 * money=5000, 
		 * count=1, waresid=10000100000002100001, 
		 * exorderno=xinqihd_babywar_yuanbao_4, 
		 * sign=9f603a83866c3431ac80c1a3a9760ab5, 
		 * chargepoint=4
		 * }
		 */
		/**
		 * 交易结果:
		 * 0 – 交易成功
		 * 1 – 交易失败
		 */
		int chargeResult = StringUtil.toInt(String.valueOf(map.get("result")), 1);
		/**
		 * 计费类型:
			0 – 开放价格
			1 – 免费
			2 – 按次
			3 – 包自然时长
			4 – 包账期
			5 – 买断
			6 – 包次数
			7 – 按时长
			8 – 包活跃时长
			9 – 批量购买
			100 – 按次免费试用 101 – 按时长免费试用
		 */
		int feetype = StringUtil.toInt(String.valueOf(map.get("feetype")), 2);
		/**
		 * 交易类型: 0 – 交易 1 – 冲正
		 */
		int transtype = StringUtil.toInt(String.valueOf(map.get("transtype")), 0);
		/**
		 * 计费支付平台的交易订单号
		 */
		String transid = String.valueOf(map.get("transid"));
		/**
		 * 交易时间格式: yyyy-mm-dd hh24:mi:ss
		 */
		String transtime = String.valueOf(map.get("transtime"));
		/**
		 * 本次交易的金额,单位:分
		 */
		int money = Math.round(StringUtil.toInt(String.valueOf(map.get("money")), 0) / 100.0f);
		int boughtYuanbao = Math.round(money * 10);
		/**
		 * 本此购买数量
		 */
		int count = StringUtil.toInt(String.valueOf(map.get("count")), 0);
		/**
		 * 外部透传参数，保存用户昵称
		 */
		String exorderno = String.valueOf(map.get("exorderno"));
		String userName = exorderno;
		if ( chargeResult == 0 ) {
			if ( feetype == 2 ) {
				if ( transtype == 0 ) {
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
								ChargeManager.getInstance().doCharge(userSessionKey, user, transid, null, money, "changyou", true);									
							} else {
								logger.info("Proxy charge request to remote server {}", gameServerId);
								BceChargeInternal.Builder bceCharge = BceChargeInternal.newBuilder();
								bceCharge.setOrderid(transid);
								bceCharge.setUserid(user.get_id().toString());
								bceCharge.setFreecharge(true);
								bceCharge.setChargemoney(Math.round(money));
								bceCharge.setChannel("changyou");
								GameContext.getInstance().proxyToRemoteGameServer(userSessionKey, gameServerId, bceCharge.build());
							}
						} else {
							ChargeManager.getInstance().doCharge(null, user, transid, null, money, "changyou", true);
						}
					}
				} else {
					BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
							userName, "", transtime, 0, 0, "unknown", 0, 0, 0, boughtYuanbao, 
							StringUtil.concat(new Object[]{"transtype:", transtype})});
				}
			} else {
				BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
						userName, "", transtime, 0, 0, "unknown", 0, 0, 0, boughtYuanbao, 
						StringUtil.concat(new Object[]{"feetype:", feetype})});
			}
		} else {
			BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
					userName, "", transtime, 0, 0, "unknown", 0, 0, 0, boughtYuanbao, 
					StringUtil.concat(new Object[]{"result:", result})});
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
