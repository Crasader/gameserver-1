package script.charge;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;

public class MobageTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		String json = "{'published':'2013-02-21T04:21:33','comment':'','id':'06107D20-07FF-3F54-8C84-DAB8D9FD6991','updated':'2013-02-21T04:21:35','items':[{'item':{'imageUrl':'','name':'100元宝','id':'1','price':10,'description':''},'quantity':1}],'state':'open'}";
		ScriptManager.getInstance().runScript(ScriptHook.CHARGE_MOBAGE, json, "test001");
		fail("Not yet implemented");
	}

}
