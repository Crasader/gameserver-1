package script.charge;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.charge.ChangyouCharge;
import com.xinqihd.sns.gameserver.transport.http.HttpMessage;

public class ChangyouTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testChangyou() {
		HttpMessage message = new HttpMessage();
		String post = "{\"exorderno\":\"xinqihd_babywar_yuanbao_4\",\"transid\":\"02112111121173800426\",\"waresid\":\"10000100000002100001\",\"chargepoint\":4,\"feetype\":2,\"money\":5000,\"result\":0,\"transtype\":0,\"transtime\":\"2012-11-11 21:22:39\",\"count\":1,\"sign\":\"9f603a83866c3431ac80c1a3a9760ab5\"}";
		message.appendRequestContent(post);
		HttpMessage response = ChangyouCharge.chargeNotify(message, null);
	}

}
