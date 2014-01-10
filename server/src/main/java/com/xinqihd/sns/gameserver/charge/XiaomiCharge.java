package com.xinqihd.sns.gameserver.charge;

import static com.xinqihd.sns.gameserver.util.CommonUtil.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.ChargeManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBceChargeInternal.BceChargeInternal;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.session.SessionManager;
import com.xinqihd.sns.gameserver.transport.http.HttpMessage;
import com.xinqihd.sns.gameserver.util.MessageFormatter;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * @author wangqi
 *
 */
public class XiaomiCharge {
	
	private static final String MAC_NAME = "HmacSHA1";
	private static final String ENCODING = "UTF-8";
	
	private static final String CODE_SUCCESS = "200";
	private static final String CP_ORDER_ID_ERROR = "1506";
	private static final String APP_ID_ERROR = "1515";
	private static final String UID_ERROR = "1516";
	private static final String SIG_ERROR = "1525";
	
	private static final String RESPONSE = 
			"{\"errcode\": {}}";
	
	/**
	 * 状态码
	    200 成功
			1506 cpOrderId 错误
			1515 appId 错误
			1516 uid 错误
			1525 signature 错误
	 */
	
	private static final Logger logger = LoggerFactory.getLogger(XiaomiCharge.class);
	private static final Logger BILLING_LOGGER = LoggerFactory.getLogger("BILLING");
	
	
	/**
	 * 中国移动在充值成功后，会回调这个接口
	 * 
	 * @param httpMessage
	 */
	public static final HttpMessage chargeNotify(HttpMessage httpMessage, String variable) {
		logger.info("Xiaomi Charge request is coming");
		String content = null;
		boolean success = true;
		String errorCode = CODE_SUCCESS;
		
		if ( httpMessage == null ) {
			logger.warn("Xiaomi Charge HTTP is null");
			success = false;
		} else {
			content = httpMessage.getRequestContent();
			if ( content == null || content.length()<=0 ) {
				if ( variable != null ) {
					content = variable;
				} else {
					logger.warn("Xiaomi Charge HTTP request content is null");
					success = false;
				}
			}
		}
		if ( success ) {
			success = false;
			logger.debug("request:{}", content);
			HashMap<String, String> paramMap = parseHttpParams(content);
		  //固定值“1”代表成功，“0”代表失败
			int i=0;
			String appId = paramMap.get("appId");
			//开发商订单 ID
			String cpOrderId = paramMap.get("cpOrderId");
		  //开发商透传信息
			String cpUserInfo = paramMap.get("cpUserInfo");
			//用户 ID
			String uid = paramMap.get("uid");
			//游戏平台订单 ID
			String orderId = paramMap.get("orderId");
			//订单状态 TRADE_SUCCESS:成功
			String orderStatus = paramMap.get("orderStatus");
			//支付金额,单位为分,即 0.01 米币。
			String payFee = paramMap.get("payFee");
			
			//商品代码
			String productCode = paramMap.get("productCode");
			//商品名称
			String productName = paramMap.get("productName");
			if ( productName != null ) {
				productName = URLDecoder.decode(productName);
			}
			//商品数量
			String productCount = paramMap.get("productCount");
			//支付时间,格式 yyyy-MM-dd HH:mm:ss
			String payTime = paramMap.get("payTime");
			if ( payTime != null ) {
				payTime = URLDecoder.decode(payTime);
			}
			//￼签名:以上参数按字母顺序排序然后进行
			String signature = paramMap.get("signature");

			String roleName = cpUserInfo;
			String chargeDateStr = payTime;
			/**
			 * payFee为分为单位的货币数量，转换为元为单位的数量
			 */
			float moneyAmount = StringUtil.toInt(payFee, 0)/100.0f;
			int boughtYuanbao = Math.round(moneyAmount * 10);
			/**
			 * 验证请求的完整性
			 */
			/*
			String xiaomiKey = GameDataManager.getInstance().getGameDataAsString(GameDataKey.CHARGE_XIAOMI_KEY);
			if ( StringUtil.checkNotEmpty(xiaomiKey) ) {
				StringBuilder buf = new StringBuilder(200);
				buf.append("appId=").append(appId).append("&");
				buf.append("cpOrderId=").append(cpOrderId).append("&");
				buf.append("cpUserInfo=").append(cpUserInfo).append("&");
				buf.append("orderId=").append(orderId).append("&");
				buf.append("orderStatus=").append(orderStatus).append("&");
				buf.append("payFee=").append(payFee).append("&");
				buf.append("payTime=").append(payTime).append("&");
				buf.append("productCode=").append(productCode).append("&");
				buf.append("productCount=").append(productCount).append("&");
				buf.append("productName=").append(productName).append("&");
				buf.append("uid=").append(uid);
				String msg = buf.toString();
				String hmacSha1 = null;
				try {
					hmacSha1 = HmacSHA1Encrypt(msg, xiaomiKey);
				} catch (Exception e) {
					logger.warn("HmacSHA1Encrypt exp:{}", e);
				}
				if ( !signature.equals(hmacSha1) ) {
					success = false;
					errorCode = SIG_ERROR;
					BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
							roleName, Constant.EMPTY, chargeDateStr, 0, 0, "unknown", 0, 0, 0, boughtYuanbao, 
							StringUtil.concat("sig error:", signature, "/", hmacSha1)});
				} else {
					success = true;
				}
			}
			*/
			success = true;
			if ( success ) {
				String transId = cpOrderId;
				String device = Constant.EMPTY;
				if ( "TRADE_SUCCESS".equals(orderStatus) ) {
					success = ChargeManager.processTransacIdBilling(orderId,
							transId, device, moneyAmount, chargeDateStr, transId);
				} else {
					BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
							//orderId, username, device, chargeDateStr, amount, rmb_amount, currency, discount, origYuanbao, boughtYuanbao, 0, "xiaomi order fail"
							orderId, transId, device, chargeDateStr, payFee, moneyAmount, "rmb", 0, 0, 0, 0, "xiaomi order fail"
					});
				}
			}
		}

		//Send fail response to client
		HttpMessage response = new HttpMessage();
		String msg = MessageFormatter.format(RESPONSE, errorCode).getMessage();
		try {
			response.setResponseContent(msg.getBytes(Constant.ENC_UTF8));
		} catch (UnsupportedEncodingException e) {
		}
		return response;
	}

	/**
	* 使用 HMAC-SHA1 签名方法对对encryptText进行签名 
	* @param encryptText 被签名的字符串
	* @param encryptKey 密钥
	* @return 返回被加密后的字符串
	* @throws Exception
	*/
	public static final String HmacSHA1Encrypt( String encryptText, String encryptKey ) throws Exception{
		byte[] data = encryptKey.getBytes( ENCODING );
		// 根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
		SecretKey secretKey = new SecretKeySpec( data, MAC_NAME );
		// 生成一个指定 Mac 算法 的 Mac 对象 
		Mac mac = Mac.getInstance( MAC_NAME );
		//用给定密钥初始化 Mac 对象
		mac.init( secretKey );
		byte[] text = encryptText.getBytes( ENCODING );
		// 完成 Mac 操作
		byte[] digest = mac.doFinal( text );
		StringBuilder sBuilder = bytesToHexString( digest ); 
		return sBuilder.toString();
	}
	
	/**
	* 使用 HMAC-SHA1 签名方法对对encryptText进行签名
	* @param encryptData 被签名的字符串
	* @param encryptKey 密钥
	* @return 返回被加密后的字符串 * @throws Exception
	*/
	public static String HmacSHA1Encrypt( byte[] encryptData, String encryptKey ) throws Exception {
		byte[] data = encryptKey.getBytes( ENCODING );
		// 根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
		SecretKey secretKey = new SecretKeySpec( data, MAC_NAME ); // 生成一个指定 Mac 算法 的 Mac 对象
		Mac mac = Mac.getInstance( MAC_NAME );
		// 用给定密钥初始化 Mac 对象 mac.init( secretKey );
		// 完成 Mac 操作
		byte[] digest = mac.doFinal( encryptData ); 
		StringBuilder sBuilder = bytesToHexString( digest ); 
		return sBuilder.toString();
	}
	
	/**
	* 转换成Hex
	*
	* @param bytesArray 
	*/
	private static StringBuilder bytesToHexString( byte[] bytesArray ) {
		if ( bytesArray == null ) { 
			return null;
		}
		StringBuilder sBuilder = new StringBuilder(); 
		for ( byte b : bytesArray ){
			String hv = String.format("%02x", b);
			sBuilder.append( hv );
		}
		return sBuilder; 
	}
	
}
