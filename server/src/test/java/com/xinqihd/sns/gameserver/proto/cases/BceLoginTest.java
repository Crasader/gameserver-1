package com.xinqihd.sns.gameserver.proto.cases;

import java.util.HashSet;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.protobuf.MessageLite;
import com.xinqihd.sns.gameserver.proto.AssertResultType;
import com.xinqihd.sns.gameserver.proto.ContextKey;
import com.xinqihd.sns.gameserver.proto.ProtoTest;
import com.xinqihd.sns.gameserver.proto.XinqiBceLogin;
import com.xinqihd.sns.gameserver.proto.XinqiBseInit.BseInit;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleInfo.BseRoleInfo;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

@ProtoTest(order=1,times=1)
public class BceLoginTest {
	
	private static final Log log = LogFactory.getLog(BceLoginTest.class);
	
	private HashSet<Class> responseClasses = new HashSet<Class>();
	
	public MessageLite generateMessge(Map<String, Object> context) {
		String userName = (String)context.get(ContextKey.USERNAME);
		String password = (String)context.get(ContextKey.PASSWORD);
		if ( userName == null ) {
			userName = "test-001";
		}
		if ( password == null ) {
			password = "123456";
		}
		
		XinqiBceLogin.BceLogin.Builder msg = XinqiBceLogin.BceLogin.getDefaultInstance().newBuilderForType();
		msg.setUsername(userName);
		msg.setPassword(password);
		
		XinqiBceLogin.BceLogin request = msg.build();
		
		return request;
	}
	
	public AssertResultType assertResult(Map<ContextKey, Object> context, XinqiMessage message) {
    XinqiMessage response = (XinqiMessage)message;
    responseClasses.add(response.payload.getClass());
    
    if ( response.payload instanceof BseRoleInfo ) {
    	String userId = ((BseRoleInfo)response.payload).getUserid();
    	context.put(ContextKey.USERID, userId);
    	log.debug("userid: " + userId);
    } else if ( response.payload instanceof BseInit ) {
    	String sessionId = ((BseInit)response.payload).getToken();
    	context.put(ContextKey.SESSIONID, sessionId);
    	log.debug("sessionId: "+sessionId);
    }
    if ( responseClasses.size() >= 5 ) {
    	return AssertResultType.SUCCESS;
    }
    return AssertResultType.CONTINUE;
	}

}
