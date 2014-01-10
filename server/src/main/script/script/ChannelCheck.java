package script;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.Account;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * 纠正一些不正确的渠道
 * @author wangqi
 *
 */
public class ChannelCheck {

	private static final Logger logger = LoggerFactory.getLogger(ChannelCheck.class);
	
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if ( result != null ) {
			return result;
		}
		String client = (String)parameters[0];
		String origChannel = (String)parameters[1];
		
		String channel = origChannel;
		if ( origChannel != null && origChannel.contains("@channel@") ) {
			if ( client != null ) {
				if ( client.startsWith("iP") ) {
					logger.info("Readjust the channel for client:{}, channel:{}", 
							new Object[]{client, origChannel});
					channel = "cysk_appstore_0001_0001_0001_0001";
				}
			}
		}
		
		ArrayList list = new ArrayList();
		list.add(channel);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
}
