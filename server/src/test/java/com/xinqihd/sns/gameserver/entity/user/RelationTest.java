package com.xinqihd.sns.gameserver.entity.user;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.Relation.People;

public class RelationTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testAddPeople() {
		Relation relation = makeRelation(3);
		
		ArrayList<People> list = convertToList(relation.listPeople());
		assertEquals(3, list.size());
		relation.clearChangeMark();
		relation.addPeople(makePeople("test999"));
		Set<String> flags = relation.clearChangeMark();
		assertEquals(1, flags.size());
		assertEquals("test999", flags.iterator().next());
		assertEquals(0, relation.clearChangeMark().size());
	}
	
	@Test
	public void testAddPeopleOver50() {
		int count = 60;
		Relation relation = new Relation();
		for ( int i=0; i<count; i++ ) {
			String userName = "test-" + i;
			relation.addPeople(makePeople(userName));
		}
		
		ArrayList<People> list = convertToList(relation.listPeople());
		assertEquals(50, list.size());
		relation.clearChangeMark();
		relation.addPeople(makePeople("test999"));
		Set<String> flags = relation.clearChangeMark();
		assertEquals(2, flags.size());
		assertEquals(0, relation.clearChangeMark().size());
		
		for ( int i=0; i<list.size(); i++ ) {
			People p = list.get(i);
			System.out.println(p.getUsername());
			assertEquals("test-"+(10+i), p.getUsername());
		}
	}
	
	@Test
	public void testAddPeopleOver50SaveAndQuery() {
		String userName = "test-001";
		User user = new User();
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		UserManager.getInstance().removeUser(userName);
		UserManager.getInstance().saveUser(user, true);
		
		int count = 60;
		Relation relation = new Relation();
		relation.setType(RelationType.RIVAL);
		user.addRelation(relation);
		for ( int i=0; i<count; i++ ) {
			String name = "test-" + i;
			relation.addPeople(makePeople(name));
			UserManager.getInstance().saveUserRelation(user.getRelations());
		}
		
		User actualUser = UserManager.getInstance().queryUser(userName);
		UserManager.getInstance().queryUserRelation(actualUser);
		relation = actualUser.getRelation(RelationType.RIVAL);
		ArrayList<People> list = convertToList(relation.listPeople());
		assertEquals(50, list.size());
		relation.clearChangeMark();
		relation.addPeople(makePeople("test999"));
		Set<String> flags = relation.clearChangeMark();
		assertEquals(2, flags.size());
		assertEquals(0, relation.clearChangeMark().size());
		
		for ( int i=0; i<list.size(); i++ ) {
			People p = list.get(i);
			System.out.println(p.getUsername());
			assertEquals("test-"+(10+i), p.getUsername());
		}
	}

	@Test
	public void testRemovePeople() {
		Relation relation = makeRelation(3);
		
		ArrayList<People> list = convertToList(relation.listPeople());
		assertEquals(3, list.size());
		relation.clearChangeMark();
		
		relation.removePeople(makePeople("test-1"));
		Set<String> flags = relation.clearChangeMark();
		assertEquals(1, flags.size());
		assertEquals("test-1", flags.iterator().next());
		assertEquals(0, relation.clearChangeMark().size());
	}

	@Test
	public void testModifyPeople() {
		Relation relation = makeRelation(3);
		
		ArrayList<People> list = convertToList(relation.listPeople());
		assertEquals(3, list.size());
		relation.clearChangeMark();
		relation.modifyPeople(makePeople("test-1"));
		Set<String> flags = relation.clearChangeMark();
		assertEquals(1, flags.size());
		assertEquals("test-1", flags.iterator().next());
		assertEquals(0, relation.clearChangeMark().size());
	}
	
	private Relation makeRelation(int count) {
		Relation relation = new Relation();
		for ( int i=0; i<count; i++ ) {
			String userName = "test-" + i;
			relation.addPeople(makePeople(userName));
		}
		return relation;
	}
	
	private People makePeople(String userName) {
		UserId userId = new UserId(userName);
		People p = new People();
		p.setId(userId);
		p.setUsername(userName);
		return p;
	}
	
	/**
	 * Convert collection to ArrayList
	 * @param c
	 * @return
	 */
	private ArrayList<People> convertToList(Collection<People> c) {
		ArrayList<People> list = new ArrayList<People>(c);
		return list;
	}
}
