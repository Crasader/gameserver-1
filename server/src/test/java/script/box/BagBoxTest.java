package script.box;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.reward.Box;
import com.xinqihd.sns.gameserver.reward.RewardManager;

public class BagBoxTest {
	
	private String userName = "test-001";

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEnlargeBag() {
		User user = new User();
		user.setRoleName(userName);
		user.setUsername(userName);
		//26002	小背包
		ItemPojo littleBagItem = ItemManager.getInstance().getItemById("26002");
		PropData littleBag = littleBagItem.toPropData();
		Bag bag = user.getBag();
		bag.addOtherPropDatas(littleBag.clone());
		assertEquals(1, bag.getCurrentCount());
		assertEquals("小背包", bag.getOtherPropData(20).getName());
		assertEquals(70, bag.getMaxCount());
		
		//Enlarge it
		RewardManager.getInstance().openItemBox(user, 20);
		assertEquals(0, bag.getCurrentCount());
		assertEquals(100, bag.getMaxCount());
		
		//Use it again. should not exceed 100 items.
		bag.addOtherPropDatas(littleBag.clone());
		RewardManager.getInstance().openItemBox(user, 20);
		assertEquals(1, bag.getCurrentCount());
		assertEquals(100, bag.getMaxCount());
		
		//Try to use the big bag 
		//26003	大背包
		ItemPojo bigBagItem = ItemManager.getInstance().getItemById("26003");
		PropData bigBag = bigBagItem.toPropData();
		bag.addOtherPropDatas(bigBag.clone());
		
		RewardManager.getInstance().openItemBox(user, 21);
		assertEquals(1, bag.getCurrentCount());
		assertEquals(140, bag.getMaxCount());
	}

}
