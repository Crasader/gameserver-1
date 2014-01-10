package script.function;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.script.function.HurtCalculator;

public class HurtCalculatorTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * 数值范围			
   * 实际伤害		200~1000	
   * 实际护甲		60~700	
   * 护甲减免		20%~65%	
   * 最终伤害		70~800	
	 */
	@Test
	public void testCalculateHurt() {
		double attackUserAttack = 200;
		double targetUserDefend = 60;
		double attackUserLucky  = 100;
		double bloomArea = 0.8;
		System.out.println("attackUserDamage \t gattackUserAttack \t targetUserDefend \t attackUserLucky \t targetUserSkin \t bloomAreaRatio \t finalDamage");
		for ( int damage = 200; damage <= 1000; damage+=50 ) {
			for ( int skin = 60; skin < 700; skin+=50 ) {
				double hurt = HurtCalculator.calculateHurt(damage, attackUserAttack, targetUserDefend, attackUserLucky, skin, bloomArea);
				System.out.println(damage + "\t" + attackUserAttack + "\t" + targetUserDefend + "\t" + attackUserLucky + "\t" + skin + "\t" + bloomArea + "\t" + hurt);
			}
		}
	}

	@Test
	public void testCriticalHit() {
		System.out.println("attackUserLucky \t hurtRatio \t IsCrits"); 
		int max = 1000;
		for ( int lucky = 0; lucky<=2000; lucky+=50) {
			int happenCount = 0;
			for ( int i=0; i<max; i++ ) {
				double hurtRatio = HurtCalculator.calculateCritsRatio(lucky);
				if ( (hurtRatio>1.005) ) {
					happenCount++;
//					System.out.println(lucky + " \t " + hurtRatio);
				}
			}
			System.out.println("Lucky:" + lucky + " \t Happen:" + (happenCount*100.0/max)+"%");
		}
	}
}
