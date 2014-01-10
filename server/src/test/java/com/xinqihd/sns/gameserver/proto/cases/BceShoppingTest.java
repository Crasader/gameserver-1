package com.xinqihd.sns.gameserver.proto.cases;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.protobuf.MessageLite;
import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.proto.AssertResultType;
import com.xinqihd.sns.gameserver.proto.ContextKey;
import com.xinqihd.sns.gameserver.proto.ProtoTest;
import com.xinqihd.sns.gameserver.proto.XinqiBceShopping;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

@ProtoTest(order=2,times=1)
public class BceShoppingTest {
	
	private static final Log log = LogFactory.getLog(BceShoppingTest.class);
	
	public MessageLite generateMessge(Map<String, Object> context) {
		
		XinqiBceShopping.BceShopping.Builder builder = 
				XinqiBceShopping.BceShopping.getDefaultInstance().newBuilderForType();
		builder.setGender(Gender.MALE.ordinal());
		
		XinqiBceShopping.BceShopping request = builder.build();
		
		return request;
	}
	
	public AssertResultType assertResult(Map<ContextKey, Object> context, XinqiMessage message) {
    XinqiMessage response = (XinqiMessage)message;
    log.debug("GoodsInfo: " + response);
    return AssertResultType.SUCCESS;
	}

}
