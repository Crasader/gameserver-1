package com.xinqihd.sns.gameserver.ai;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AIActionTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCalculatePower() {
		//a:0.766044443118978,b:0.0,c:0.0,d:299010.1195225712,wind:0,power:-73
		int angle = 50;
		int hitx = 1610-370;
		int hity = 42-35;
		int wind = 0;
		int power = AIAction.calculatePower(angle, hitx, hity, wind);
		System.out.println(power);
	}

}
