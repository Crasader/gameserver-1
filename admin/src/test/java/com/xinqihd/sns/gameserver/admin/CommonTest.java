package com.xinqihd.sns.gameserver.admin;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CommonTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		String line = "强化装备使用幸运符能够使强化的成功率大大提高。123\t5级以上的装备强化时若失败会下降一个强化等级。";
		String[] cells = line.split("\t");
		for ( String cell : cells ) {
			System.out.println("*:"+cell);
		}
	}

}
