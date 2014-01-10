package script.charge;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AnzhiTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		String request = "appKey=c318br6RLex12IeBs0Ta6wo1&amount=10.0&orderId=20130221143739432&payType=002&payResult=200&ext=MTM1MTUyNjQwMDrovaznlJ%2FkuYvngo4%3D&msg=SUCCESS&signStr=89FDD7F0E56439BA2984C0676F42D601";
		Anzhi.func(new Object[]{request});
	}

}
