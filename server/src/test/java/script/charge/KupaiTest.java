package script.charge;

import static org.junit.Assert.*;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.charge.CMCCCharge;
import com.xinqihd.sns.gameserver.charge.OppoCharge;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.ChargeManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.transport.http.HttpMessage;
import com.xinqihd.sns.gameserver.util.MessageFormatter;

public class KupaiTest {

	String post = "{\"exorderno\":\"10011310958\",\"transid\":\"03111122816103650001\",\"waresid\":\"10003100000001100031\",\"chargepoint\":\"1111\",\"feetype\":0,\"money\":5,\"count\":3,\"result\":1,\"transtype\":0,\"transtime\":\"2013-01-05 15:43:38\",\"sign\":\"9f5346265c614d0f536fd1dfc311c49f\"}";
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFunc() throws Exception {
		URL url = new URL("http://192.168.0.77/kupai");
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoInput(true);
		conn.setDoOutput(true);
		OutputStream os = conn.getOutputStream();
		os.write(post.getBytes());
		os.close();
		Object obj = conn.getContent();
		int code = conn.getResponseCode();
		System.out.println("code:"+code+", obj:"+obj);
		fail("Not yet implemented");
	}

	@Test
	public void testCall() throws Exception {
		Kupai.func(new Object[]{post});
	}
}
