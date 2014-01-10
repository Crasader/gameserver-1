package com.xinqihd.sns.gameserver.charge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.transport.http.HttpGameHandler;
import com.xinqihd.sns.gameserver.transport.http.HttpMessage;
/**
 * Oppo的NearMe计费
 * 
 * @author wangqi
 *
 */
public class OppoCharge {

	private static final Logger logger = LoggerFactory.getLogger(OppoCharge.class);
	private static final Logger BILLING_LOGGER = LoggerFactory.getLogger("BILLING");
	
	/**
	 * 中国移动在充值成功后，会回调这个接口
	 * 
	 * @param httpMessage
	 */
	public static final HttpMessage chargeNotify(HttpMessage httpMessage, String variable) {
		logger.info("Oppo Charge request is coming");
		String content = null;
		boolean success = true;
		if ( httpMessage == null ) {
			logger.warn("Oppo Charge HTTP is null");
			success = false;
		} else {
			content = httpMessage.getRequestContent();
			if ( content == null || content.length()==0 ) {
				if ( variable != null ) {
					content = variable;
				} else {
					logger.warn("Oppo Charge HTTP request content is null");
					success = false;
				}
			}
		}
		if ( success ) {
			ScriptResult result = ScriptManager.getInstance().runScript(ScriptHook.CHARGE_OPPO, content);
			if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
				HttpMessage response = (HttpMessage)result.getResult().get(0);
				return response;
			}
		}
		//Send response to client
		HttpMessage response = new HttpMessage();
		response.setResponseContentType(HttpGameHandler.CONTENT_PLAIN_TYPE);
		response.setResponseContent("failure".getBytes());
		return response;
	}
	
}
