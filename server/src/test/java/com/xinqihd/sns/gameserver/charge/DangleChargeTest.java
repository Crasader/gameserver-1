package com.xinqihd.sns.gameserver.charge;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.transport.http.HttpMessage;

public class DangleChargeTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testChargeNotify() {
		HttpMessage message = new HttpMessage();
		String content = "result=1&orderid=391001001H3h8mmnj5b&amount=1.00&mid=391&gid=1&sid=1&uif=xq123&utp=0&eif=xq123&pcid=014&cardno=&timestamp=20121023142311&errorcode=&verstring=333547f4c346f6c1a2e8d29261d94e53&remark=1%3A%E4%BA%A4%E6%98%93%E5%AE%8C%E6%88%90";
		message.appendRequestContent(content);
		HttpMessage response = DangleCharge.chargeNotify(message, null);
		System.out.println(response.getResponseCode());
		System.out.println(response.getResponseContent());
	}

}
