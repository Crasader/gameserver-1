package script.ai;

import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.ai.AIManager;
import com.xinqihd.sns.gameserver.chat.ChatType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceChat.BceChat;
import com.xinqihd.sns.gameserver.proto.XinqiBseChat.BseChat;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.Text;

public class UserChat {

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if ( result != null ) {
			return result;
		}
		IoSession serverIoSession = (IoSession) parameters[0];
		User aiUser = (User) parameters[1];
		Object message = parameters[2];
		
		if ( message instanceof BseChat ) {
			BseChat bseChat = (BseChat) message;
			String content = bseChat.getMsgContent();
			if ( content != null && content.startsWith("ai:") ) {
				aiUser.putUserData(AIManager.BATTLE_USER_COMMAND, content);
			}
			String sourceUserId = bseChat.getUsrId();
			if ( bseChat.getMsgType() == ChatType.ChatPrivate.ordinal() 
					&& !aiUser.get_id().toString().equals(sourceUserId) ) {
				String chatMsg = Text.text("ai.chat.1");
				sendChat(chatMsg, sourceUserId, serverIoSession, aiUser, true);
			}
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}

	/**
	 * Send Chat message to server
	 * @param content
	 */
	public static final void sendChat(String content, String sourceUserId, 
			IoSession serverIoSession, User aiUser, boolean isPrivate) {
		
		BceChat.Builder myBceChat = BceChat.newBuilder();
		if ( isPrivate ) {
			myBceChat.setMsgType(ChatType.ChatPrivate.ordinal());
		} else {
			myBceChat.setMsgType(ChatType.ChatCurrent.ordinal());
		}
		myBceChat.setMsgContent(content);
		myBceChat.setUsrId(sourceUserId);
		
		AIManager.getInstance().sendServerMessageToAIClient(
				serverIoSession, aiUser.getSessionKey(), myBceChat.build());
		
	}
}
