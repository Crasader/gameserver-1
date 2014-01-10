package script.charge;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.charge.BaoruanCharge;
import com.xinqihd.sns.gameserver.transport.http.HttpMessage;

public class BaoruanTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		String content = "cid=817&uid=145795637953138039&order_id=111001695646201211131012254714&amount=1.00&verifystring=68b6cdfc127bfb7c1c64aad7eee03c0f";
		HttpMessage message = new HttpMessage();
		message.appendRequestContent(content);
		HttpMessage response = BaoruanCharge.chargeNotify(message, null);
	}

}
