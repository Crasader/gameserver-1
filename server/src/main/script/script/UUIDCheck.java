package script;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.db.mongo.LoginManager.ErrorCode;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.proto.XinqiBseLogin.BseLogin;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * Check user's device UUID
 * @author wangqi
 *
 */
public class UUIDCheck {

	private static final Logger logger = LoggerFactory.getLogger(VersionCheck.class);
	private static HashSet uuidSet = new HashSet();
	private static String message = "您的账号存在充值异常，无法登陆，请联系游戏GM(QQ:418313139)";
	static {
		uuidSet.add("YTAwMDAwMzY1ZjRhYjM=");
		uuidSet.add("ODYxMDM0MDAxMjcwNTE4");
		uuidSet.add("ODY3MTU2MDEyMzcwMjQ1");
		//rolename:1425257505; cysk_jiyou_0001_0001_0026_0001
		uuidSet.add("ODY1OTI0MDE5Nzg5NjMw");
		//52948 | OTkwMDAxNDAxODAwNjg1 | 燃烧吧m赛儿
		uuidSet.add("OTkwMDAxNDAxODAwNjg1");
		//Wangqi's G9100 For test purpose.
		//uuidSet.add("MzU3NDc0MDQzNzE1MzM5");
		//Wangqi's iPhone4S
		uuidSet.add("QjVDQzU4NUUtMzU4Ri00Q0VFLTk5OEEtRjhDQUVFRTExRjRG");
		//燃烧吧m白鲨 神风无敌
		uuidSet.add("861410010239167");
		uuidSet.add("ODYxNDEwMDEwMjM5MTY3");
	}

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		boolean checkPass = true;

		IoSession session = (IoSession)parameters[0];
		String uuid = (String)parameters[1];
		
		//Check client version first
		String base64Uuid = new String(Base64.encodeBase64(uuid.getBytes()));
		if ( uuidSet.contains(uuid) || uuidSet.contains(base64Uuid) ) {
			BseLogin.Builder rep = BseLogin.newBuilder();
			rep.setCode(ErrorCode.S_PAUSE.ordinal());
			rep.setDesc(message);
			XinqiMessage response = new XinqiMessage();
			response.payload = rep.build();
			session.write(response);

			SysMessageManager.getInstance().sendClientInfoRawMessage(session, message, Action.NOOP, Type.CONFIRM);

			checkPass = false;
		}

		ArrayList list = new ArrayList();
		list.add(checkPass);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
}
