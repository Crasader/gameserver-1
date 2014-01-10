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
 * 联想使用爱贝的计费方式
 * 
 * 
 * @author wangqi
 *
 */
public class LegendCharge {

	private static final Logger logger = LoggerFactory.getLogger(LegendCharge.class);
	private static final Logger BILLING_LOGGER = LoggerFactory.getLogger("BILLING");
	
	/**
	 * 中国移动在充值成功后，会回调这个接口
	 * 
	 * @param httpMessage
	 */
	public static final HttpMessage chargeNotify(HttpMessage httpMessage, String variable) {
		logger.debug("LegendCharge request is coming");
		String content = null;
		boolean success = true;
		if ( httpMessage == null ) {
			logger.warn("LegendCharge HTTP is null");
			success = false;
		} else {
			content = httpMessage.getRequestContent();
			if ( content == null || content.length()==0 ) {
				if ( variable != null ) {
					content = variable;
				} else {
					logger.warn("LegendCharge HTTP request content is null");
					success = false;
				}
			}
		}
		if ( success ) {
			ScriptResult result = ScriptManager.getInstance().runScript(ScriptHook.CHARGE_LEGEND, content);
			if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
				HttpMessage response = (HttpMessage)result.getResult().get(0);
				return response;
			}
		}

		//Send response to client
		HttpMessage response = new HttpMessage();
		response.setResponseContentType(HttpGameHandler.CONTENT_PLAIN_TYPE);
		response.setResponseContent("FAILURE".getBytes());
		return response;
	}
	
}
