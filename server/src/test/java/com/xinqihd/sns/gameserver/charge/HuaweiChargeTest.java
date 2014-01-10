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

public class HuaweiChargeTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCmccChargeNotif() throws UnsupportedEncodingException {
		String roleName = "test-001";
		String requestContent = 
				"result=0&userName=Sohu鐣呮父&productName=灏忓皬椋炲脊&payType=4&amount=10.0&orderId=A2012120402302878666F1FD&notifyTime=1354638600021&requestId=1354602444701&sign=QzZY%2FaxKrL%2FGl9E0UANSxvoP5NYGxKB3ln8b9IbsJECSAad45baI%2F%2FyvLf0ef2NJNxgSV6MNPf7GRgGy5vaTEg%3D%3D";
		final HttpMessage request = new HttpMessage();
		request.setRequestUri("/huawei");
		
		User user = new User();
		user.set_id(new UserId(roleName));
		user.setRoleName(roleName);
		user.setUsername(roleName);
		UserManager.getInstance().removeUser(roleName);
		UserManager.getInstance().saveUser(user, true);
		
		String transId = ChargeManager.getInstance().generateTranscationID("s0001", user);
		String post = MessageFormatter.format(requestContent, transId).getMessage();
		request.appendRequestContent(post);
		
		HttpMessage response = HuaweiCharge.chargeNotify(request, null);
		String text = new String(response.getResponseContent(), "utf8");
		System.out.println(text);
		
	}

}
