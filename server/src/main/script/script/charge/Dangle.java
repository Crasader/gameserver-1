package script.charge;

import static com.xinqihd.sns.gameserver.util.CommonUtil.*;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
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
import com.xinqihd.sns.gameserver.entity.user.UserId;
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
public class Dangle {
	
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final Logger logger = LoggerFactory.getLogger(Dangle.class);
	private static final Logger BILLING_LOGGER = LoggerFactory.getLogger("BILLING");

	/**
	 * 
	 * @param parameters: User, ItemPojo, Pew(in user bag)
	 * @return
	 */
	public static ScriptResult func(Object[] parameters) {
		ScriptResult scriptResult = ScriptManager.checkParameters(parameters, 1);
		if ( scriptResult != null ) {
			return scriptResult;
		}
		String request = (String)parameters[0];
	
		boolean success = false;
		logger.debug("dangle charge post content:{}", request);
		HashMap paramMap = parseHttpParams(request);
		//固定值“1”代表成功，“0”代表失败
		String result = (String)paramMap.get("result");
		//orderid	订单号	Max(30)	Yes	支付订单号
		String orderId = (String)paramMap.get("orderid");
		//支付金额（实际支付金额，不一定等于订单的提交金额）
		String amount = (String)paramMap.get("amount");
		String mid = (String)paramMap.get("mid");
		String gid = (String)paramMap.get("gid");
		String sid = (String)paramMap.get("sid");
		//dangle uid
		String uif = (String)paramMap.get("uif");
		String transId = uif; //URLDecoder.decode(uif);
		//用户类型
		String utp = (String)paramMap.get("utp");
		//Ip
		String uip = (String)paramMap.get("uip");
		//扩展信息
		String eif = (String)paramMap.get("eif");
		//支付方式
		String pcid = (String)paramMap.get("pcid");
		//卡号
		String cardno = (String)paramMap.get("cardno");
		//支付卡密码
		String cardpwd = (String)paramMap.get("cardpwd");
		//时间戳 格式：yyyymmddHH24mmss月，日小时，分，秒小于10前面补充0
		String chargeDateStr = null;
		String field = (String)paramMap.get("timestamp");
		if ( field != null ) {
			try {
				chargeDateStr = DateUtil.formatDateTime(SDF.parse(field));
			} catch (ParseException e1) {
				chargeDateStr = SDF.format(new Date());
				logger.debug("Failed to parse the date str:{}", field);
			}
		} else {
			chargeDateStr = SDF.format(new Date());
		}
		String errorcode = (String)paramMap.get("errorcode");
		String remark = (String)paramMap.get("remark");
		String verstring = (String)paramMap.get("verstring");
		
		/**
		 * 32位小写MD5验证串，生成规则参见下方说明
		 * 如何生成verstring：
				1、拼串
				result=%s&orderid=%s&amount=%s&mid=s%&gid=s%&sid=s%&uif=s%&utp=s%&eif=%s&pcid=%s&cardno=%s&timestamp=%s&errorcode=%s&merchantkey=s%
				2、将步骤1中拼出的字符串按照约定加密方式(MD5)加密，生成VERIFY_STRING，并转为小写
		 */
		/*
		String dangleKey = GameDataManager.getInstance().getGameDataAsString(GameDataKey.CHARGE_DANGLE_KEY);
		StringBuilder verBuf = new StringBuilder(content.length());
		verBuf.append("mid=").append(mid).append("&gid=").append(gid).append("&sid=").append(sid).append("&uif=").
			append(roleName).append("&utp=").append(roleName).append("&eif=").append(eif).
			append("&bakurl=").append("http://charge.babywar.xinqihd.com:8080/dangle").
			append("&amount=").append(amount).
			append("&timestamp=").append(field).append("&merchantkey=").append(dangleKey);
		logger.debug(verBuf.toString());
		*/
		
		float moneyAmount = 0;
		try {
			moneyAmount = Float.parseFloat(amount);
		} catch (NumberFormatException e) {
			logger.warn("Money parsed exception.", e);
		}
		int boughtYuanbao = Math.round(moneyAmount * 10);
		
		try {
			//String myVerifyStr = DangleMD5.digest(verBuf.toString()).toLowerCase();
			String device = Constant.EMPTY;
			
			if ( true ) { //myVerifyStr.equals(verstring) ) {
				if ( Constant.ONE.equals(result) ) {
					success = ChargeManager.processTransacIdBilling(orderId,
							transId, device, moneyAmount, chargeDateStr, "dangle");
				} else {
					BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
							//orderId, username, device, chargeDateStr, amount, rmb_amount, currency, discount, origYuanbao, boughtYuanbao, 0, "dangle order fail"
							orderId, transId, device, chargeDateStr, amount, moneyAmount, "rmb", 0, 0, 0, 0, "dangle order fail"
					});
				}
			} else {
				/*
				ChargeManager.getInstance().doCharge(user, null, moneyAmount, 
						StringUtil.concat("verify error:", verstring, "/", myVerifyStr), false);
				logger.debug("The verstring {} is invalid with {}", verstring, myVerifyStr);
				*/
			}
		} catch (Exception e) {
			success = false;
			logger.warn("Failed to md5 the given verstring", e);
		}
		
		//Send response to client
		HttpMessage response = new HttpMessage();
		if ( success ) {
			response.setResponseContentType(HttpGameHandler.CONTENT_PLAIN_TYPE);
			response.setResponseContent("result=1&msg=success".getBytes());
		} else {
			response.setResponseContentType(HttpGameHandler.CONTENT_PLAIN_TYPE);
			response.setResponseContent("result=0&msg=failure".getBytes());
		}
		ArrayList list = new ArrayList();
		list.add(response);
		
		scriptResult = new ScriptResult();
		scriptResult.setType(ScriptResult.Type.SUCCESS_RETURN);
		scriptResult.setResult(list);
		return scriptResult;
	}

}
