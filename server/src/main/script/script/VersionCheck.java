package script;

import java.util.ArrayList;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager.ErrorCode;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseLogin.BseLogin;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

public class VersionCheck {
	
	private static final Logger logger = LoggerFactory.getLogger(VersionCheck.class);

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 7);
		if ( result != null ) {
			return result;
		}
		boolean checkPass = true;

		int clientMajorVersion = (Integer)parameters[0];
		int clientMinorVersion = (Integer)parameters[1];
		int clientTinyVersion = (Integer)parameters[2];
		String channel = (String)parameters[3];
		int serverMajorVersion = (Integer)parameters[4];
		int serverMinorVersion = (Integer)parameters[5];
		IoSession session = (IoSession)parameters[6];
		
		//Check client version first
		if ( clientMajorVersion < serverMajorVersion || 
				(clientMajorVersion == serverMajorVersion && clientMinorVersion < serverMinorVersion )) {
			// Client version is too low
			String message = Text.text(ErrorCode.VERSION.desc(), 
					new Object[]{
						StringUtil.concat(new Object[]{clientMajorVersion, '.', clientMinorVersion}),
						StringUtil.concat(new Object[]{serverMajorVersion, '.', serverMinorVersion})
					}
			);
			
			String url = "http://update.babywar.xinqihd.com/index.htm";
			SysMessageManager.getInstance().sendClientInfoRawMessage(
					session, url, Action.NOOP, Type.MAINTAINCE);
			
			checkPass = false;
		}
		
		/**
		 * 华为终端存在计费BUG，需要强制升级到1.7.1
		 */
		if ( channel != null && channel.contains("_huawei_") ) {
			if ( clientMajorVersion <= 1 && clientMinorVersion <= 7 && clientTinyVersion<=0 ) {
				String message = "您的版本存在充值失败的问题，请您进入华为商店下载更新1.7.1版本软件包，给您带来的不变深表歉意";

				BseLogin.Builder rep = BseLogin.newBuilder();
				rep.setCode(ErrorCode.VERSION.ordinal());
				rep.setDesc(message);
				XinqiMessage response = new XinqiMessage();
				response.payload = rep.build();
				session.write(response);
				
				checkPass = false;

				logger.info("huawei 1.7.0 login");
			} else {
				logger.info("huawei 1.7.1 login");
			}
		}

		ArrayList list = new ArrayList();
		list.add(checkPass);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
}
