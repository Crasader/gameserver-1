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

public class OppoTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFunc() throws Exception {
		URL url = new URL("http://192.168.0.77:8080/oppo?notify_id=13212&partner_code=c5217trjnrmU6gO5jG8VvUFU0&partner_order=0000001192171129&orders=1100.010000001192171129&pay_result=OK&sign=dNJDN5Ov8FdMZIvLZmnh9rwpKiqwvCzJaja%2B0qpKb3zlFafYxjxCDnNJrtxLqnPOGXHxWZO69onAHzTNfyKZBL3PR57%2F87rwuYk87OthYkIGm4dNunZDnUmMp5m5b0Joe6DOW28NqZETGcKUMeKwQcJmE7c%2FN5aQcWAyazksf0g%3D");
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoInput(true);
		conn.setDoOutput(true);
		OutputStream os = conn.getOutputStream();
		//os.write("\r\n".getBytes());
		os.close();
		Object obj = conn.getContent();
		int code = conn.getResponseCode();
		System.out.println("code:"+code+", obj:"+obj);
		fail("Not yet implemented");
	}
	
	@Test
	public void testOppo() throws Exception {
		String roleName = "test-001";
		String requestContent = 
				"notify_id=13212&partner_code=c5217trjnrmU6gO5jG8VvUFU0&partner_order=0000001192171129&orders=1100.010000001192171129&pay_result=OK&sign=dNJDN5Ov8FdMZIvLZmnh9rwpKiqwvCzJaja%2B0qpKb3zlFafYxjxCDnNJrtxLqnPOGXHxWZO69onAHzTNfyKZBL3PR57%2F87rwuYk87OthYkIGm4dNunZDnUmMp5m5b0Joe6DOW28NqZETGcKUMeKwQcJmE7c%2FN5aQcWAyazksf0g%3D";
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
		
		HttpMessage response = OppoCharge.chargeNotify(request, requestContent);
		String text = new String(response.getResponseContent(), "utf8");
		System.out.println(text);
		
		assertEquals("", text);
	}

}
