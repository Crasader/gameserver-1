package script.charge;

import static com.xinqihd.sns.gameserver.util.CommonUtil.*;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
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
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBceChargeInternal.BceChargeInternal;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.session.SessionManager;
import com.xinqihd.sns.gameserver.transport.http.HttpGameHandler;
import com.xinqihd.sns.gameserver.transport.http.HttpMessage;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.MessageFormatter;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * 完成畅游爱贝渠道的计费
 * 
 * @author wangqi
 *
 */
public class Oppo {
	
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final Logger logger = LoggerFactory.getLogger(Oppo.class);
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
		logger.info("oppo post charge content:{}", request);
		
		HashMap map = parseHttpParams(request);
		//OK表示计费成功
		String orderResult = String.valueOf(map.get("pay_result"));
		//订单信息orders = productName + amount + count + partner_order
		String orders = String.valueOf(map.get("orders"));
		//开发者的code
		String productName = String.valueOf(map.get("partner_code"));
		//通知id
		String orderId = String.valueOf(map.get("notify_id"));
		Date chargeDate = new Date();
		//开发者支付请求 ID,原样返回开发者 App 调 用支付 SDK 时填写的 requestId 参数值。
		String requestId = String.valueOf(map.get("partner_order"));
		//RSA 签名(使用开发者公钥进行验签,待验 签内容生成方法见 2.5 签名机制)
		String sign = String.valueOf(map.get("sign"));
		
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
			String oppoMoney = orders.substring(1, orders.length()-17);
			try {
				money = Float.parseFloat(oppoMoney)/100.0f;
			} catch (Exception e) {
				logger.warn("Failed to parse oppo money:{}. orders:{}", oppoMoney, orders);
			}
			if ( "OK".equals(orderResult) ) {
				success = ChargeManager.processTransacIdBilling(orderId,
						requestId, device, money, chargeDateStr, "oppo");
			} else {
				BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
						//orderId, username, device, chargeDateStr, amount, rmb_amount, currency, discount, origYuanbao, boughtYuanbao, 0, "oppo order fail"
						orderId, transId, device, chargeDateStr, amount, money, "rmb", 0, 0, 0, 0, "oppo order fail"
				});
			}
		} catch (Exception e) {
			BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
					//orderId, username, device, chargeDateStr, amount, rmb_amount, currency, discount, origYuanbao, boughtYuanbao, 0, "oppo order fail"
					orderId, transId, device, chargeDateStr, amount, money, "rmb", 0, 0, 0, 0, "exception:".concat(e.getMessage())
			});
			logger.warn("#CMCCCharge: fail to process user charge request", e);
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
