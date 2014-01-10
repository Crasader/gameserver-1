package script.function;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.script.function.BattleCalculator;

public class BattleCalculatorTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
			X	Y	t
			0	0	0
			20	11.2694937	0.027301905
			40	21.98285886	0.054630986
			60	32.13843157	0.081987322
			80	41.73453958	0.109370996
			100	50.76950227	0.13678209
			120	59.2416306	0.164220686
			140	67.14922702	0.191686868
			160	74.49058546	0.219180718
			180	81.26399122	0.246702321
			200	87.46772092	0.274251761
			220	93.10004246	0.301829122
			240	98.15921494	0.32943449
			260	102.6434886	0.357067949
			280	106.5511048	0.384729587
	 */
	@Test
	public void testCalculateY() {
		int power = 50, angle = 30, wind = 1; 
		double  F = 0.075, K = 0.059081;
		int g = 760;
		int[] x = new int[]{20,40,60,80,100,120,140,160,180,200,220,240,260,280};
		//Make sure wind = 0 is ok
		wind = 0;
		assertTrue( BattleCalculator.calculateTByX(power, angle, wind, F, K, g, 20)>0.0 );
		//Make sure wind = -5 is ok
		wind = -5;
		assertTrue( BattleCalculator.calculateTByX(power, angle, wind, F, K, g, 20)>0.0 );
		for ( int i=0; i<x.length; i++ ) {
			double t  = BattleCalculator.calculateTByX(power, angle, wind, F, K, g, x[i]);
			double y  = BattleCalculator.calculateYByX(power, angle, wind, F, K, g, x[i]);
			double y1 = BattleCalculator.calculateYByT(power, angle, wind, F, K, g, t);
			assertEquals(y, y1, 0.01);
			assertTrue(y > 0 );
			System.out.println("y = "+ y + ", t = " + t + ", y1 = " + y1);
		}
	}

	@Test
	public void testCalculateYAngle90() {
		int power = 100, angle = 89, wind = 1; 
		double  F = 0.075, K = 0.059081;
		int g = 760;
		for ( int x=0; x<2500; x++) {
			double t = 0.01 * x;
			double y  = BattleCalculator.calculateYByT(power, angle, wind, F, K, g, t);
			double y1 = BattleCalculator.calculateYByX(power, angle, wind, F, K, g, x);
			System.out.println("y="+y+",y1="+y1+",t="+t);
		}
	}
	
}
