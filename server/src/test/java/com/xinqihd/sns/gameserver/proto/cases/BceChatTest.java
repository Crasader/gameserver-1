package com.xinqihd.sns.gameserver.proto.cases;

import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.protobuf.MessageLite;
import com.xinqihd.sns.gameserver.chat.ChatType;
import com.xinqihd.sns.gameserver.proto.AssertResultType;
import com.xinqihd.sns.gameserver.proto.ContextKey;
import com.xinqihd.sns.gameserver.proto.ProtoTest;
import com.xinqihd.sns.gameserver.proto.XinqiBceChat.BceChat;
import com.xinqihd.sns.gameserver.proto.XinqiBseChat.BseChat;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

@ProtoTest(order=4,times=1)
public class BceChatTest {
	
	private static final Log log = LogFactory.getLog(BceChatTest.class);
	
	public MessageLite generateMessge(Map<String, Object> context) {
		BceChat.Builder payload = BceChat.getDefaultInstance().newBuilderForType();
		payload.setMsgType(ChatType.ChatPrivate.ordinal());
		payload.setMsgContent("这个消息发送在:"+new Date());
		String userId = (String)context.get(ContextKey.USERID);
		if ( userId != null ) {
			payload.setUsrId(userId);
		}
		BceChat msg = payload.build();
		return msg;
	}
	
	public AssertResultType assertResult(Map<ContextKey, Object> context, XinqiMessage message) {
    XinqiMessage response = (XinqiMessage)message;
    BseChat chat = (BseChat)response.payload;
    log.debug("Chat Message: " + chat.getMsgContent() + ", from user: " + chat.getUsrNickname());
//    assertEquals(1, response);
    return AssertResultType.SUCCESS;
	}

}
