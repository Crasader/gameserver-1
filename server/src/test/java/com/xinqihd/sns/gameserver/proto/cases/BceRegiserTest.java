package com.xinqihd.sns.gameserver.proto.cases;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.Random;

import com.google.protobuf.MessageLite;
import com.xinqihd.sns.gameserver.proto.AssertResultType;
import com.xinqihd.sns.gameserver.proto.ContextKey;
import com.xinqihd.sns.gameserver.proto.ProtoTest;
import com.xinqihd.sns.gameserver.proto.XinqiBceRegister;
import com.xinqihd.sns.gameserver.proto.XinqiBseRegister;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

@ProtoTest(order=0,times=1)
public class BceRegiserTest {
	
	public static final String PREFIX = "test-";
	public static final String PASSWORD = "12345678";
	
	private static final Random r = new Random();
	
	public MessageLite generateMessge(Map<ContextKey, Object> context) {
		
		String userName = PREFIX + r.nextInt(Integer.MAX_VALUE);
		String password = PASSWORD;
		
		context.put(ContextKey.USERNAME, userName);
		context.put(ContextKey.PASSWORD, password);

		XinqiBceRegister.BceRegister.Builder msg = XinqiBceRegister.BceRegister.getDefaultInstance().newBuilderForType();

		msg.setUsername(userName);
		msg.setPassword(password);
		msg.setRolename(userName);
		msg.setEmail(userName+"@test.com");
		msg.setGender(r.nextInt(2)+1);
		msg.setClient("testclient");
		msg.setChannel("xinqihd");
		msg.setCountry("china");
		msg.setLocx(r.nextInt(1000));
		msg.setLocy(r.nextInt(1000));
		
		XinqiBceRegister.BceRegister request = msg.build();
		
		return request;
	}
	
	public AssertResultType assertResult(Map<ContextKey, Object> context, XinqiMessage message) {
    try {
			XinqiMessage response = (XinqiMessage)message;
			XinqiBseRegister.BseRegister payload = (XinqiBseRegister.BseRegister) response.payload;
			assertEquals(0, payload.getCode());
			if ( payload.getCode() == 0 || payload.getCode() == 1 ) {
				return AssertResultType.SUCCESS;
			}
			return AssertResultType.FAILURE;
		} catch (Exception e) {
			e.printStackTrace();
			return AssertResultType.EXCEPTION;
		}
	}

}
