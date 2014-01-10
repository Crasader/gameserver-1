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

public class YeepayTest {

	String post = "r0_Cmd=ChargeCardDirect&r1_Code=2&p1_MerId=10011835526&p2_Order=1357613434097%C9%F1%C1%FA%B0%DA%CE%B2&p3_Amt=0.0&p4_FrpId=JUNNET&p5_CardNo=5566852&p6_confirmAmount=0.0&p7_realAmount=0.0&p8_cardStatus=7&p9_MP=%C9%F1%C1%FA%B0%DA%CE%B2&pb_BalanceAmt=&pc_BalanceAct=&hmac=829b08ce661f3eb6131c7d16d11f74f9";
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
		Yeepay.func(new Object[]{post});
	}
}
