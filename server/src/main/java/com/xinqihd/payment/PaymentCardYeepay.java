package com.xinqihd.payment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.payment.yeepay.DigestUtil;
import com.xinqihd.payment.yeepay.HttpUtils;
import com.xinqihd.payment.yeepay.NonBankcardPaymentResult;
import com.xinqihd.payment.yeepay.PaymentResult;

public class PaymentCardYeepay implements IPaymentCard {
	private String WEB_URL = "https://www.yeepay.com/app-merchant-proxy/command.action";

	private static Logger log = LoggerFactory.getLogger(PaymentCardYeepay.class);
	private static String p0_Cmd = "ChargeCardDirect"; // 请求命令名称
	private static String decodeCharset = "GBK"; // 字符方式

	private String _p0_cmd = "ChargeCardDirect"; // 业务类型
	private String _p1_MerId = "10011835526"; // 商户编号
	private String _p2_Order = ""; // 商户订单号
	private String _p3_Amt = ""; // 支付金额
	private String _v4_verifyAmt = "true"; // 是否校验金额
	public String _p5_Pid = ""; // 产品名称
	public String _p6_Pcat = ""; // 产品类型
	public String _p7_Pdesc = ""; // 产品描述
	private String _p8_Url = "http://charge.babywar.xinqihd.com:8080/yeepay"; // 商户接收支付成功数据的地址
	private String _pa7_cardAmt = ""; // 卡面额组
	private String _pa8_cardNo = ""; // 卡号组
	private String _pa9_cardPwd = ""; // 卡密组
	private String _pd_FrpId = ""; // 支付渠道编码
	private String _pr_NeedResponse = "1"; // 应答机制
	public String _pz_userId = ""; // 用户在商户处的唯一ID
	public String _pz_userRegTime = ""; // 用户注册时间
	private String _hmac = ""; // 签名数据
	private int _cardMoney;
	private String _privateKey = "YE59r179ZWx363X08ELJkNK062vd01i3IMxq2o6awb8Lq13v2w77P2bp34gh";

	public PaymentCardYeepay(String cardNo, String password, int cardMoney) {
		_cardMoney = cardMoney;
		_pa7_cardAmt = String.valueOf(cardMoney);
		_pa8_cardNo = cardNo;
		_pa9_cardPwd = password;
	}

	public void setOrderID(String orderID) {
		_p2_Order = orderID;
	}

	/**
	 * 支付通道编码列表 pd_FrpId参数值 对应支付通道名称 JUNNET 骏网一卡通 SNDACARD 盛大卡 SZX 神州行 ZHENGTU 征途卡
	 * QQCARD Q币卡 UNICOM 联通卡 JIUYOU 久游卡 YPCARD 易宝e卡通 NETEASE 网易卡 WANMEI 完美卡 SOHU
	 * 搜狐卡 TELECOM 电信卡 ZONGYOU 纵游一卡通 TIANXIA 天下一卡通 TIANHONG 天宏一卡通
	 */
	public void setChannelID(String id) {
		_pd_FrpId = id;
	}

	public void setAmt(int amt) {
		_p3_Amt = String.valueOf(amt);
	}


	public void appendParams(StringBuilder sb, String key, String value) {
		sb.append("&" + key + "=");
		if (value == null) {
			sb.append("");
		} else {
			sb.append(value);
		}
	}

	// @Override
	public PaymentResult doPost(String userName) {
		// TODO Auto-generated method stub
		PaymentResult paymentResult = new PaymentResult();
		NonBankcardPaymentResult result = pay(_p2_Order, _p3_Amt, _v4_verifyAmt,
				_p5_Pid, _p6_Pcat, _p7_Pdesc, _p8_Url, userName, _pa7_cardAmt,
				_pa8_cardNo, _pa9_cardPwd, _pd_FrpId, _pr_NeedResponse, _pz_userId,
				_pz_userRegTime);

		paymentResult.statusCode = 200;
		paymentResult.code = result.getR1_Code();
		paymentResult.msg = result.getRq_ReturnMsg();
		return paymentResult;// result.getR1_Code();
	}

	/**
	 * 消费请求 该方法是根据《易宝支付非银行卡支付专业版接口文档 v3.0》对发起支付请求进行的封装
	 * 具体参数含义请仔细阅读《易宝支付非银行卡支付专业版接口文档 v3.0》 商户订单号
	 * 
	 * @param p2_Order
	 *          订单金额
	 * @param p3_Amt
	 *          是否较验订单金额
	 * @param p4_verifyAmt
	 *          产品名称
	 * @param p5_Pid
	 *          产品类型
	 * @param p6_Pcat
	 *          产品描述
	 * @param p7_Pdesc
	 *          通知地址
	 * @param p8_Url
	 *          扩展信息
	 * @param pa_MP
	 *          卡面额组
	 * @param pa7_cardAmt
	 *          卡号组
	 * @param pa8_cardNo
	 *          支付方式
	 * @param pd_FrpId
	 *          通知是否需要应答
	 * @param pr_NeedResponse
	 *          用户ID
	 * @param pz_userId
	 *          用户注册时间
	 * @param pz1_userRegTime
	 * @return
	 */
	public NonBankcardPaymentResult pay(String p2_Order, String p3_Amt,
			String p4_verifyAmt, String p5_Pid, String p6_Pcat, String p7_Pdesc,
			String p8_Url, String pa_MP, String pa7_cardAmt, String pa8_cardNo,
			String pa9_cardPwd, String pd_FrpId, String pr_NeedResponse,
			String pz_userId, String pz1_userRegTime) {

		// 卡号和卡密不得为空
		if (pa8_cardNo == null || pa8_cardNo.equals("") || pa9_cardPwd == null
				|| pa9_cardPwd.equals("")) {
			log.error("pa7_cardNo or pa8_cardPwd is empty.");
			throw new RuntimeException("pa7_cardNo or pa8_cardPwd is empty.");
		}

		// 生成hmac，保证交易信息不被篡改,关于hmac详见《易宝支付非银行卡支付专业版接口文档 v3.0》
		String hmac = "";
		hmac = DigestUtil.getHmac(new String[] { _p0_cmd, _p1_MerId, p2_Order,
				p3_Amt, p4_verifyAmt, p5_Pid, p6_Pcat, p7_Pdesc, p8_Url, pa_MP,
				pa7_cardAmt, pa8_cardNo, pa9_cardPwd, pd_FrpId, pr_NeedResponse,
				pz_userId, pz1_userRegTime }, _privateKey);
		// 封装请求参数，参数说明详见《易宝支付非银行卡支付专业版接口文档 v3.0》
		Map<String, String> reqParams = new HashMap<String, String>();
		reqParams.put("p0_Cmd", _p0_cmd);
		reqParams.put("p1_MerId", _p1_MerId);
		reqParams.put("p2_Order", p2_Order);
		reqParams.put("p3_Amt", p3_Amt);
		reqParams.put("p4_verifyAmt", p4_verifyAmt);
		reqParams.put("p5_Pid", p5_Pid);
		reqParams.put("p6_Pcat", p6_Pcat);
		reqParams.put("p7_Pdesc", p7_Pdesc);
		reqParams.put("p8_Url", p8_Url);
		reqParams.put("pa_MP", pa_MP);
		reqParams.put("pa7_cardAmt", pa7_cardAmt);
		reqParams.put("pa8_cardNo", pa8_cardNo);
		reqParams.put("pa9_cardPwd", pa9_cardPwd);
		reqParams.put("pd_FrpId", pd_FrpId);
		reqParams.put("pr_NeedResponse", pr_NeedResponse);
		reqParams.put("pz_userId", pz_userId);
		reqParams.put("pz1_userRegTime", pz1_userRegTime);
		reqParams.put("hmac", hmac);
		List responseStr = null;
		try {
			// 发起支付请求
			log.debug("Begin http communications,request params[" + reqParams + "]");
			responseStr = HttpUtils.URLPost(WEB_URL, reqParams);
			log.debug("End http communications.responseStr:" + responseStr);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
		if (responseStr.size() == 0) {
			log.error("no response.");
			throw new RuntimeException("no response.");
		}
		// 创建非银行卡专业版消费请求结果
		NonBankcardPaymentResult rs = new NonBankcardPaymentResult();
		// 解析易宝支付返回的消费请求结果,关于返回结果数据详见《易宝支付非银行卡支付专业版接口文档 v3.0》
		for (int t = 0; t < responseStr.size(); t++) {
			String currentResult = (String) responseStr.get(t);
			log.debug("responseStr[" + t + "]:" + currentResult);
			if (currentResult == null || currentResult.equals("")) {
				continue;
			}
			int i = currentResult.indexOf("=");
			log.debug("i=" + i);
			int j = currentResult.length();
			if (i >= 0) {
				log.debug("find =.");
				String sKey = currentResult.substring(0, i);
				String sValue = currentResult.substring(i + 1);
				if (sKey.equals("r0_Cmd")) {
					rs.setR0_Cmd(sValue);
				} else if (sKey.equals("r1_Code")) {
					rs.setR1_Code(sValue);
				} else if (sKey.equals("r6_Order")) {
					rs.setR6_Order(sValue);
				} else if (sKey.equals("rq_ReturnMsg")) {
					rs.setRq_ReturnMsg(sValue);
				} else if (sKey.equals("hmac")) {
					rs.setHmac(sValue);
				} else {
					log.error("throw exception:" + currentResult);
					throw new RuntimeException(currentResult);
				}
			} else {
				log.error("throw exception:" + currentResult);
				throw new RuntimeException(currentResult);
			}
		}
		// 不成功则抛出异常
		if (!rs.getR1_Code().equals("1")) {
			log.error("errorCode:" + rs.getR1_Code() + ";errorMessage:"
					+ rs.getRq_ReturnMsg());
			return rs;
		}
		String newHmac = "";
		newHmac = DigestUtil.getHmac(new String[] { rs.getR0_Cmd(),
				rs.getR1_Code(), rs.getR6_Order(), rs.getRq_ReturnMsg() }, _privateKey);
		// hmac不一致则抛出异常
		if (!newHmac.equals(rs.getHmac())) {
			log.error("交易签名被篡改:{},{}", newHmac, rs.getHmac());
			//throw new RuntimeException("交易签名被篡改");
		}
		return (rs);
	}

}
