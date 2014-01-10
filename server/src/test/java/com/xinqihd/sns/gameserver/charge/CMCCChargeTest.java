package com.xinqihd.sns.gameserver.charge;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.ChargeManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.transport.http.HttpMessage;
import com.xinqihd.sns.gameserver.util.MessageFormatter;

public class CMCCChargeTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCmccChargeNotif() throws UnsupportedEncodingException {
		String roleName = "test-001";
		String billingIdentifier = "000052411001";
		String requestContent = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?><request><userId>311215051</userId><cpServiceId>628410052412</cpServiceId><consumeCode>000052411001</consumeCode><cpParam>{}</cpParam><hRet>0</hRet><status>1101</status><transIDO>4151901PONE10200E</transIDO><versionId>100</versionId></request>";
		final HttpMessage request = new HttpMessage();
		request.setRequestUri("/cmcccharge");
		
		User user = new User();
		user.set_id(new UserId(roleName));
		user.setRoleName(roleName);
		user.setUsername(roleName);
		UserManager.getInstance().removeUser(roleName);
		UserManager.getInstance().saveUser(user, true);
		
		String transId = ChargeManager.getInstance().generateTranscationID("s0001", user);
		String post = MessageFormatter.format(requestContent, transId).getMessage();
		request.appendRequestContent(post);
		
		HttpMessage response = CMCCCharge.cmccChargeNotif(request, null);
		String text = new String(response.getResponseContent(), "utf8");
		System.out.println(text);
		
		String expect = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><response>	<transIDO>12345678901234567</transIdo>	<hRet>0</hRet>	<message>Successful</message></response>";
		assertEquals(expect, text);
	}

}
