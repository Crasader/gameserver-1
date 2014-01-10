package com.xinqihd.sns.gameserver.handler;

import java.util.HashMap;
import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceGetValue.BceGetValue;
import com.xinqihd.sns.gameserver.proto.XinqiBseGetValue.BseGetValue;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.ScriptResult.Type;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.treasure.TreasureHuntManager;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * The BceGetValue is a common protocol for getting values from Server.
 * Now it supports the following action:
 * VipLevelQuery: return the user's diff yuanbao for every vip levels.
 * @author wangqi
 *
 */
public class BceGetValueHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceGetValueHandler.class);
	
	private static final BceGetValueHandler instance = new BceGetValueHandler();
	
	private BceGetValueHandler() {
		super();
	}

	public static BceGetValueHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceGetValue");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceGetValue query = (BceGetValue)request.payload;
		String action = query.getAction();
		
		BseGetValue.Builder builder = BseGetValue.newBuilder(); 
		builder.setAction(action);
		if ( StringUtil.checkNotEmpty(action) ) {
			String script = StringUtil.concat("script.query.", action);
			logger.debug("Try to get action {}", script);
			ScriptHook hook = ScriptHook.getScriptHook(script);
			User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
			List<String> keys = query.getKeysList();
			List<String> values = query.getValuesList();
			//Run the script
			ScriptResult result = ScriptManager.getInstance().runScript(hook, user, keys, values);
			if ( result.getType() == Type.SUCCESS_RETURN ) {
				HashMap map = (HashMap<String, String>)result.getResult().get(0);
				builder.addAllKeys(map.keySet());
				builder.addAllValues(map.values());
			}
		}
		GameContext.getInstance().writeResponse(sessionKey, builder.build());
	}
	
}
