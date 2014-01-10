package script.charge;

import static com.xinqihd.sns.gameserver.util.CommonUtil.*;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.ChargePojo;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.ChargeManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBceChargeInternal.BceChargeInternal;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.session.SessionManager;
import com.xinqihd.sns.gameserver.transport.http.HttpGameHandler;
import com.xinqihd.sns.gameserver.transport.http.HttpMessage;
import com.xinqihd.sns.gameserver.util.BillingJedis;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.MessageFormatter;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * 完成畅游爱贝渠道的计费
 * 
 * @author wangqi
 *
 */
public class Yeepay {
	
	private static final HashMap ERR_MAP = new HashMap();
	static {
		ERR_MAP.put("0", "销卡成功，订单成功");
		ERR_MAP.put("1", "销卡成功，订单失败");
		ERR_MAP.put("7", "卡号卡密或卡面额不符合规则");
		ERR_MAP.put("1002", "本张卡密您提交过于频繁，请您稍后再试");
		ERR_MAP.put("1003", "不支持的卡类型（比如电信地方卡）");
		ERR_MAP.put("1004", "密码错误或充值卡无效");
		ERR_MAP.put("1006", "充值卡无效");
		ERR_MAP.put("1007", "卡内余额不足");
		ERR_MAP.put("1008", "余额卡过期（有效期1个月）");
		ERR_MAP.put("1010", "此卡正在处理中");
		ERR_MAP.put("10000", "未知错误");
		ERR_MAP.put("2005", "此卡已使用");
		ERR_MAP.put("2006", "卡密在系统处理中");
		ERR_MAP.put("2007", "该卡为假卡");
		ERR_MAP.put("2008", "该卡种正在维护");
		ERR_MAP.put("2009", "浙江省移动维护");
		ERR_MAP.put("2010", "江苏省移动维护");
		ERR_MAP.put("2011", "福建省移动维护");
		ERR_MAP.put("2012", "辽宁省移动维护");
		ERR_MAP.put("2013", "该卡已被锁定");
		ERR_MAP.put("2014", "系统繁忙，请稍后再试");
		ERR_MAP.put("3001", "卡不存在");
		ERR_MAP.put("3002", "卡已使用过");
		ERR_MAP.put("3003", "卡已作废");
		ERR_MAP.put("3004", "卡已冻结");
		ERR_MAP.put("3005", "卡未激活");
		ERR_MAP.put("3006", "密码不正确");
		ERR_MAP.put("3007", "卡正在处理中");
		ERR_MAP.put("3101", "系统错误");
		ERR_MAP.put("3102", "卡已过期");
	};
	
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final Logger logger = LoggerFactory.getLogger(Yeepay.class);
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
		logger.info("yeepay post charge content:{}", request);
		
		HashMap map = parseHttpParams(request);
		//值为“1”, 代表支付成功.非“1”失败。
		String payResult = String.valueOf(map.get("r1_Code"));
		/**
		 * 保留两位小数,不足两位小数的将保留一位！
		 * (如 0.10 将返回0.1, 0会返回0.0) . 
		 * 商户收到该返回数据后,一定用自己数据库中存储的金额与该金额进行比较.
		 */
		String payMoney = String.valueOf(map.get("p3_Amt"));
		//易宝支付返回商户订单号
		String orderId = String.valueOf(map.get("p2_Order"));
		//商户私有数据, 存储角色的用户名
		String userName = String.valueOf(map.get("p9_MP"));
		try {
			userName = URLDecoder.decode(userName, "gbk");
		} catch (UnsupportedEncodingException e1) {
		}
		//卡状态组
		String errorCode = String.valueOf(map.get("p8_cardStatus"));
		
		Date chargeDate = new Date();
		
		String chargeDateStr = DateUtil.formatDateTime(chargeDate);
		String transId = Constant.EMPTY;
		String device = Constant.EMPTY;

		/**
		 * 获取Float表示的钱数
		 */
		boolean success = false;
		String amount = "0";
		float money = 0;
		try {
			/**
			 * 1100.010000001192171129
			 */
			try {
				money = Float.parseFloat(payMoney);
			} catch (Exception e) {
				logger.warn("Failed to parse yeepay money:{}", payMoney);
			}
			if ( "1".equals(payResult) ) {
				User user = UserManager.getInstance().queryUser(userName);
				if ( user == null ) {
					logger.warn("Failed to find user by privateFiled: {}", userName);
					BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
							userName, "", chargeDateStr, 0, 0, "unknown", 0, 0, 0, 0, 
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
							ChargeManager.getInstance().doCharge(userSessionKey, user, orderId, null, money, "yeepay", true);									
						} else {
							logger.info("Proxy charge request to remote server {}", gameServerId);
							BceChargeInternal.Builder bceCharge = BceChargeInternal.newBuilder();
							bceCharge.setUserid(user.get_id().toString());
							bceCharge.setFreecharge(true);
							bceCharge.setChargemoney(Math.round(money));
							bceCharge.setChannel("yeepay");
							bceCharge.setOrderid(orderId);
							GameContext.getInstance().proxyToRemoteGameServer(userSessionKey, gameServerId, bceCharge.build());
						}
					} else {
						ChargeManager.getInstance().doCharge(null, user, orderId, null, money, "yeepay", true);
					}
				}
				
			} else {
				String errorMessage = (String)ERR_MAP.get(String.valueOf(errorCode));
				if ( !StringUtil.checkNotEmpty(errorMessage) ) {
					errorMessage = Text.text("charge.card.fail");
				}
				User user = UserManager.getInstance().queryUser(userName);
				if ( user != null ) {
					success = true;
					SessionManager manager = GameContext.getInstance().getSessionManager();
					SessionKey userSessionKey = manager.findSessionKeyByUserId(user.get_id());
					if ( userSessionKey != null ) {
						SysMessageManager.getInstance().sendClientInfoRawMessage(userSessionKey, errorMessage, 3000);
					}
				};
				BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
						//orderId, username, device, chargeDateStr, amount, rmb_amount, currency, discount, origYuanbao, boughtYuanbao, 0, "yeepay order fail"
						orderId, transId, device, chargeDateStr, amount, money, "rmb", 0, 0, 0, 0, "yeepay order fail"});
			}
		} catch (Exception e) {
			BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
					//orderId, username, device, chargeDateStr, amount, rmb_amount, currency, discount, origYuanbao, boughtYuanbao, 0, "yeepay order fail"
					orderId, transId, device, chargeDateStr, amount, money, "rmb", 0, 0, 0, 0, "exception:".concat(e.getMessage())
			});
			logger.warn("fail to process user charge request", e);
			success = false;
		}
		
		//Send response to client
		//操作结果,0 表示成功,非 0 表示失败
		HttpMessage response = new HttpMessage();
		response.setResponseContentType(HttpGameHandler.CONTENT_PLAIN_TYPE);
		response.setResponseContent("success".getBytes());
		
		ArrayList list = new ArrayList();
		list.add(response);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}

}
