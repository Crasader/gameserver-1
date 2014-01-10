package script;

import org.junit.After;
import org.junit.Before;

import com.xinqihd.sns.gameserver.config.GlobalConfig;

public class AbstractScriptTestCase {

	@Before
	public void setUp() throws Exception {
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_SCRIPT_DIR, "src/main/script");
	}

	@After
	public void tearDown() throws Exception {
	}

}
