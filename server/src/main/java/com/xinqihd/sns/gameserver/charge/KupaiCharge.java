package com.xinqihd.sns.gameserver.charge;

import static com.xinqihd.sns.gameserver.util.CommonUtil.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.ChargeManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceChargeInternal.BceChargeInternal;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.session.SessionManager;
import com.xinqihd.sns.gameserver.transport.http.HttpGameHandler;
import com.xinqihd.sns.gameserver.transport.http.HttpMessage;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;
/**
 * 根据当乐的SDK开发的HTTP计费同步接口:
 * 
 * CP接收支付结果通知的参数列表及处理方式
		参数名称	参数含义	长度	非空	备注/说明
		result	支付结果	1	Yes	固定值“1”代表成功，“0”代表失败
		orderid	订单号	Max(30)	Yes	支付订单号
		amount	支付金额	Max(10)	No	支付金额（实际支付金额，不一定等于订单的提交金额）
		mid	商户编号	Max(3)	Yes	原样返回
		gid	游戏编号	Max(3)	Yes	原样返回
		sid	服务器编号	Max(3)	Yes	原样返回
		uif	用户信息	Max(30)	Yes	原样返回（用户在厂商系统中的用户名，在用户忘记卡号的情况下，可以通过此用户名来查询用户的充值记录）
		utp	用户类型	1	Yes	原样返回
		eif	扩展信息	Max(100)		原样返回
		pcid	支付方式	Max(3)	Yes	支付通道ID，详见支付通道对照表
		cardno	卡号	Max(30)		
		timestamp	时间戳	14	Yes	格式：yyyymmddHH24mmss月，日小时，分，秒小于10前面补充0
		errorcode	错误码	4	No	此项传空值
		remark	备注说明	500	No	支付结果说明，中文说明。格式：
		code:说明，如：1:支付成功
		verstring	MD5验证串	32	Yes	32位小写MD5验证串，生成规则参见下方说明
 * 
 * 
 * @author wangqi
 *
 */
public class KupaiCharge {

	private static final Logger logger = LoggerFactory.getLogger(KupaiCharge.class);
	private static final Logger BILLING_LOGGER = LoggerFactory.getLogger("BILLING");
	
	/**
	 * 中国移动在充值成功后，会回调这个接口
	 * 
	 * @param httpMessage
	 */
	public static final HttpMessage chargeNotify(HttpMessage httpMessage, String variable) {
		logger.info("Kupai Charge request is coming");
		String content = null;
		boolean success = true;
		if ( httpMessage == null ) {
			logger.warn("Kupai Charge HTTP is null");
			success = false;
		} else {
			content = httpMessage.getRequestContent();
			if ( content == null || content.length()==0 ) {
				if ( variable != null ) {
					content = variable;
				} else {
					logger.warn("Kupai Charge HTTP request content is null");
					success = false;
				}
			}
		}
		if ( success ) {
			ScriptResult result = ScriptManager.getInstance().runScript(ScriptHook.CHARGE_KUPAI, content);
			if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
				HttpMessage response = (HttpMessage)result.getResult().get(0);
				return response;
			}
		}

		//Send response to client
		HttpMessage response = new HttpMessage();
		response.setResponseContentType(HttpGameHandler.CONTENT_PLAIN_TYPE);
		//2002: CP 对接收的信息有任何异议, 将返回该值, 处理流程改由人工处理;
		response.setResponseContent("2002".getBytes());
		return response;
	}
	
}
