package com.xinqihd.payment;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;

import com.xinqihd.payment.yeepay.PaymentResult;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class PaymentCardShenZhouFu implements IPaymentCard {
	
	private static HashMap<String, String> map = new HashMap<String, String>(); 
	static {
		map.put("101",	"md5验证失败");
		map.put("102",	"订单号重复");
		map.put("103",	"恶意用户");
		map.put("104",	"序列号或密码简单验证失败");
		map.put("105",	"密码正在处理中");
		map.put("106",	"系统繁忙，暂停提交");
		map.put("107",	"多次支付时卡内余额不足");
		map.put("109",	"解密失败");
		map.put("201",	"证书验失败");
		map.put("501",	"插入数据库失败");
		map.put("502",	"插入数据库失败");
		map.put("200",	"您提交的计费信息正在处理中...");
		map.put("902",	"商户参数不全");
		map.put("903",	"商户 ID 不存在");
		map.put("904",	"商户没有激活");
		map.put("905",	"商户没有使用该接口的权限");
		map.put("906",	"商户没有设置密钥(privateKey privateKey privateKey privateKey)");
		map.put("907",	"商户没有设置DES密钥");
		map.put("908",	"该笔订单已经处理完成");
		map.put("909",	"该笔订单不符合重复支付的条件");
		map.put("910",	"服务器返回地址不符合规范");
		map.put("911",	"订单号不符合规范");
		map.put("912",	"非法订单");
		map.put("913",	"该地方卡暂时不支持");
		map.put("914",	"支付金额非法");
		map.put("915",	"卡面额非法");
		map.put("916",  "商户不支持该充值卡的付");
		map.put("917",	"卡号或者密码格式不正确");
		map.put("0",	  "网络连接失败");
	};
	
	private static final String WEB_URL = "http://pay3.shenzhoufu.com/interface/version3/serverconnszx/entry-noxml.aspx";
	private String _cardNo;
	private String _password;
	private int _cardMoney;
	private int _cardType;

	String DES_KEY = "CNk4Kbyw3CA=";
	String PRIVATE_KEY = "123456";
	String MER_ID = "109347";
	String VERSION = "3";
	String SERVER_URL = "http://charge.babywar.xinqihd.com/shenzhoufu";
	String MER_USER_NAME = "";
	String MER_USER_EMAIL = "";
	String ITEM_NAME = "";
	String ITEM_DESC = "";

	String GATEWAY_ID = "0";
	String VERIFY_TYPE = "1";
	String RETURN_TYPE = "1";
	String ISDEBUG = "0";
	String signString = "2";

	public static final int CARD_TYPE_CHINA_MOBLIE = 0;
	public static final int CARD_TYPE_CHINA_UNITED_TELECOM = 1;
	public static final int CARD_TYPE_CHINA_TELECOM = 2;

	public PaymentCardShenZhouFu(String cardNo, String password, int cardMoney,
			int cardType) {
		_cardNo = cardNo;
		_password = password;
		_cardMoney = cardMoney;
		_cardType = cardType;
	}

	private String getPostString(String userName) throws Exception {
		// TODO Auto-generated method stub

		StringBuilder sb = new StringBuilder();
		//sb.append("?");
		sb.append("version=");
		sb.append(VERSION);
		sb.append("&merId=");
		sb.append(MER_ID);
		sb.append("&payMoney=");
		sb.append(_cardMoney * 100);
		sb.append("&orderId=");
		String orderID = getOrderID();
		sb.append(orderID);
		sb.append("&returnUrl=");
		sb.append(SERVER_URL);
		sb.append("&cardInfo=");
		String cardInfo = getDes(String.valueOf(_cardMoney), _cardNo, _password);
		cardInfo = cardInfo.replaceAll("\\n", "");
		sb.append(URLEncoder.encode(cardInfo, "utf-8"));
		sb.append("&merUserName=");
		sb.append(MER_USER_NAME);
		sb.append("&merUeserMail=");
		sb.append(MER_USER_EMAIL);
		sb.append("&privateField=");
		sb.append(userName);
		sb.append("&verifyType=1");
		sb.append("&cardTypeCombine=");
		sb.append(_cardType);
		sb.append("&md5String=");
		sb.append(getMD5(userName, _cardMoney * 100, _cardType, orderID, cardInfo));

		//System.out.println(sb.toString());
		return sb.toString();
	}

	public PaymentResult doPost(String userName) {
		try {
			PaymentResult result = new PaymentResult();

			URL url = new URL(getUrl());
			HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
			urlConn.setRequestMethod("POST");
			urlConn.setDoInput(true);
			urlConn.setDoOutput(true);
			OutputStream os = urlConn.getOutputStream();
			os.write(getPostString(userName).getBytes());
			os.close();

			result.statusCode = urlConn.getResponseCode();

			if (result.statusCode == 200) {
				InputStream is = urlConn.getInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream(3);
				int ch = is.read();
				while ( ch != -1 ) {
					baos.write(ch);
					ch = is.read();
				}
				result.code = new String(baos.toByteArray());
				return result;
			} else {
				return result;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public void setPrivateFiled(String privateField) {
		PRIVATE_KEY = privateField;
	}
	
	/**
	 * Get the corresponding error message
	 * @param code
	 * @return
	 */
	public static final String getRespMessage(String code) {
		String message = "未知错误";
		if ( code != null ) {
			message = map.get(code);
		}
		return message;
	}

	private String getUrl() {
		// TODO Auto-generated method stub
		return WEB_URL;
	}

	/**
	 * Get the order ID
	 * @return
	 */
  private String getOrderID() {
    Date d = new Date();
    return String.valueOf((d.getYear() + 1900)) + (d.getMonth() < 10 ? "0" + 
    		(d.getMonth() + 1) : (d.getMonth() + 1)) + 
    		(d.getDate() < 10 ? "0" + d.getDate() : d.getDate()) + 
    		"-" + MER_ID + "-" + System.currentTimeMillis();
  }

	/**
	 * Get the MD5 string.
	 * @param userName
	 * @param money
	 * @param type
	 * @param orderID
	 * @param cardInfo
	 * @return
	 */
	private String getMD5(String userName, int money, int type, String orderID, String cardInfo) {
		StringBuilder sb = new StringBuilder();
		sb.append(VERSION);
		sb.append(MER_ID);
		sb.append(money);
		sb.append(orderID);
		sb.append(SERVER_URL);
		sb.append(cardInfo);
		sb.append(userName);
		sb.append(VERIFY_TYPE);
		sb.append(PRIVATE_KEY);
		return MD5Util.MD5Encode(sb.toString());
	}

	private String getDes(String money, String Sn, String passwrod) {
		try {
			return DES.getDesEncryptBase64String(money, Sn, passwrod, DES_KEY);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static void main(String[] args) {
		String cardNo = "12165150294386085";
		String password = "151730938294842368";
		int cardMoney = 10;
		PaymentCardShenZhouFu shenzhoufu = new PaymentCardShenZhouFu(cardNo, password, cardMoney, CARD_TYPE_CHINA_MOBLIE);
		PaymentResult result = shenzhoufu.doPost("123");
		System.out.println(result.code);
		System.out.println(result.msg);
	}
}
