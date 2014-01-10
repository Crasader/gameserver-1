package com.xinqihd.sns.gameserver.entity.rank;

import static org.junit.Assert.*;

import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.entity.user.BasicUser;

public class RankUserTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCompareTo() {
		Random random = new Random();
		RankUser[] rankUsers = new RankUser[5];
		Set<RankUser> sortUsers = new TreeSet<RankUser>();
		for ( int i=0; i<rankUsers.length; i++ ) {
			rankUsers[i] = new RankUser();
			rankUsers[i].setRank(random.nextInt(10));
			rankUsers[i].setScore(random.nextInt(10000));
			rankUsers[i].setRankChange(random.nextInt(10));
			rankUsers[i].setBasicUser(new BasicUser());
			
			sortUsers.add(rankUsers[i]);
		}
		
		for ( RankUser r : sortUsers ) {
			System.out.println(r);
		}
		assertTrue("Not yet implemented", true);
	}

}
