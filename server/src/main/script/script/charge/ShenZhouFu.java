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
public class ShenZhouFu {
	
	private static final HashMap ERR_MAP = new HashMap();
	static {
		ERR_MAP.put("200","支付成功 支付成功");
		ERR_MAP.put("201","您输入的充值卡密码错误");
		ERR_MAP.put("202","您输入的充值卡已被使用");
		ERR_MAP.put("203","您输入的充值卡密码非法");
		ERR_MAP.put("204","您输入的卡号或密码错误次数过多");
		ERR_MAP.put("205","卡号密码正则不匹配或者被禁止");
		ERR_MAP.put("206","本卡之前被提交过，本次订单失败，不再继续处理");
		ERR_MAP.put("207","暂不支持该充值卡的支付");
		ERR_MAP.put("208","您输入的充值卡卡号错误");
		ERR_MAP.put("209","您输入的充值卡未激活（生成卡）");
		ERR_MAP.put("210","您输入的充值卡已经作废（能查到有该卡，但是没卡的信息）");
		ERR_MAP.put("211","您输入的充值卡已过期");
		ERR_MAP.put("212","您选择的卡面额不正确");
		ERR_MAP.put("213","该卡为特殊本地业务卡，系统不支持");
		ERR_MAP.put("214","该卡为增值业务卡，系统不支持");
		ERR_MAP.put("215","新生卡");
		ERR_MAP.put("216","系统维护");
		ERR_MAP.put("217","接口维护");
		ERR_MAP.put("218","运营商系统维护");
		ERR_MAP.put("219","系统忙，请稍后再试");
		ERR_MAP.put("220","未知错误");
		ERR_MAP.put("221","本卡之前被处理完毕，本次订单失败，不再继续处理");
	};
	
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final Logger logger = LoggerFactory.getLogger(ShenZhouFu.class);
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
		logger.info("shenzhoufu post charge content:{}", request);
		
		HashMap map = parseHttpParams(request);
		//版本号值为: 3
		String version = String.valueOf(map.get("version"));
		//1:成功 0:失败
		String payResult = String.valueOf(map.get("payResult"));
		//充值金额 (单位:分)
		String payMoney = String.valueOf(map.get("payMoney"));
		//充值卡面值 (单位:分);
		String cardMoney = String.valueOf(map.get("cardMoney"));
		//商户网站形成的订单号,请按 照神州付订单规范组织订单, 同一商户的订单号不能重复
		String orderId = String.valueOf(map.get("orderId"));
		//商户私有数据, 存储角色的用户名
		String userName = String.valueOf(map.get("privateField"));
		try {
			userName = URLDecoder.decode(userName, "gbk");
		} catch (UnsupportedEncodingException e1) {
		}
		//MD5 校验串
		String md5 = String.valueOf(map.get("md5String"));
		//失败原因代码
		String errorCode = String.valueOf(map.get("errcode"));
		
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
				money = Float.parseFloat(payMoney)/100.0f;
			} catch (Exception e) {
				logger.warn("Failed to parse shenzhoufu money:{}", payMoney);
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
							ChargeManager.getInstance().doCharge(userSessionKey, user, orderId, null, money, "shenzhoufu", true);									
						} else {
							logger.info("Proxy charge request to remote server {}", gameServerId);
							BceChargeInternal.Builder bceCharge = BceChargeInternal.newBuilder();
							bceCharge.setUserid(user.get_id().toString());
							bceCharge.setFreecharge(true);
							bceCharge.setChargemoney(Math.round(money));
							bceCharge.setChannel("shenzhoufu");
							bceCharge.setOrderid(orderId);
							GameContext.getInstance().proxyToRemoteGameServer(userSessionKey, gameServerId, bceCharge.build());
						}
					} else {
						ChargeManager.getInstance().doCharge(null, user, orderId, null, money, "shenzhoufu", true);
					}
				}
				
			} else {
				String errorMessage = (String)map.get(String.valueOf(errorCode));
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
						//orderId, username, device, chargeDateStr, amount, rmb_amount, currency, discount, origYuanbao, boughtYuanbao, 0, "shenzhoufu order fail"
						orderId, transId, device, chargeDateStr, amount, money, "rmb", 0, 0, 0, 0, "shenzhoufu order fail"
				});
			}
		} catch (Exception e) {
			BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
					//orderId, username, device, chargeDateStr, amount, rmb_amount, currency, discount, origYuanbao, boughtYuanbao, 0, "shenzhoufu order fail"
					orderId, transId, device, chargeDateStr, amount, money, "rmb", 0, 0, 0, 0, "exception:".concat(e.getMessage())
			});
			logger.warn("fail to process user charge request", e);
			success = false;
		}
		
		//Send response to client
		//操作结果,0 表示成功,非 0 表示失败
		HttpMessage response = new HttpMessage();
		if ( success ) {
			response.setResponseContentType(HttpGameHandler.CONTENT_PLAIN_TYPE);
			response.setResponseContent("ok".getBytes());
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
