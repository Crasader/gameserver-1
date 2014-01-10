package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.proto.XinqiBseItem.BseItem;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardType;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class ItemManagerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetItemById() {
		ItemPojo pojo = ItemManager.getInstance().getItemById("20001");
		assertNotNull(pojo);
	}

	/**
	 * 检查是否有宝箱中的武器ID是错误的
	 */
	@Test
	public void testGetItemsAndCheckRewards() {
		Collection<ItemPojo> maps = ItemManager.getInstance().getItems();
		assertEquals(140, maps.size());

		int notFound = 0;
		for ( ItemPojo item : maps ) {
//			System.out.println(item);
			ArrayList<Reward> rewards = item.getRewards();
			for ( Reward reward : rewards ) {
				String idStr = reward.getPropId();
				int id = StringUtil.toInt(idStr, Integer.MAX_VALUE);
				if ( id == Integer.MAX_VALUE ) {
					System.out.println("Not found: "+item.getId()+"\t"+item.getName()+"\t"+reward.getPropId());
					notFound++;
				} else if ( id < 0 ) {
					if ( reward.getType() == RewardType.EXP || 
							reward.getType() == RewardType.GOLDEN ||
							reward.getType() == RewardType.MEDAL ||
							reward.getType() == RewardType.YUANBAO ||
							reward.getType() == RewardType.VOUCHER ||
							reward.getType() == RewardType.WEAPON
							) {
					} else {
						System.out.println("Not found: "+item.getId()+"\t"+item.getName()+"\t"+reward.getPropId());
						notFound++;
					}
				} else {
					ItemPojo itemPojo = ItemManager.getInstance().getItemById(idStr);
					if ( itemPojo == null ) {
						WeaponPojo weaponPojo = EquipManager.getInstance().
								getWeaponByTypeNameAndUserLevel(reward.getTypeId(), 1);
						if ( weaponPojo == null ) {
							System.out.println("Not found: "+item.getId()+"\t"+item.getName()+"\t"+reward.getPropId());
							notFound++;
						}
					}
				}
			}
		}
		assertEquals(0, notFound);
	}
	
	@Test
	public void testGetItemsAndPrintRewards() {
		Collection<ItemPojo> maps = ItemManager.getInstance().getItems();
		System.out.println("itemId \t itemName \t itemLevel \t script \t count \t q \t"+
				"rewardId \t rewardName \t rewardType \t rewardLevel \t rewardCount \t rewardIndate");
		for ( ItemPojo item : maps ) {
			ArrayList<Reward> rewards = item.getRewards();
			String rewardName = null;
			for ( Reward reward : rewards ) {
				ItemPojo itemPojo = ItemManager.getInstance().getItemById(reward.getPropId());
				if ( itemPojo == null ) {
					WeaponPojo weaponPojo = EquipManager.getInstance().getWeaponById(reward.getPropId());
					if ( weaponPojo != null ) {
						rewardName = weaponPojo.getName();
					}
				} else {
					rewardName = itemPojo.getName();
				}
				if ( rewardName == null ) {
					rewardName = reward.getType().name();
				}
				System.out.println(item.getId()+"\t"+item.getName()+"\t"+item.getLevel()+"\t"+ item.getScript() + "\t" +
						item.getCount() + "\t" +item.getQ() + "\t" +
						reward.getPropId()+"\t"+rewardName +"\t"+reward.getType()+"\t"+reward.getPropLevel()+"\t"+reward.getPropCount()+"\t"+reward.getPropIndate());
			}
		}
	}
	
	@Test
	public void testGetStones() {
		Collection<String> stoneTypes = ItemManager.getInstance().getStoneTypes();
//		System.out.println(stoneTypes);
		assertEquals(7, stoneTypes.size());
	}
	
	@Test
	public void testGetItemByTypeIdAndLevel() {
		ItemPojo itemPojo = ItemManager.getInstance().getItemByTypeIdAndLevel("25011", 1);
		assertEquals("25011", itemPojo.getId());
		
		itemPojo = ItemManager.getInstance().getItemByTypeIdAndLevel("25011", 8);
		assertEquals("25018", itemPojo.getId());

		itemPojo = ItemManager.getInstance().getItemByTypeIdAndLevel("25061", 0);
		assertEquals("25061", itemPojo.getId());
	}

	@Test
	public void testToBseItem() {
		BseItem bseItem = ItemManager.getInstance().toBseItem();
		assertEquals(179, bseItem.getItemsCount());
	}
	
	public void addCanbeRewarded() {
		ItemManager manager = ItemManager.getInstance();
		Collection<ItemPojo> items = manager.getItems();
		ArrayList<ItemPojo> modified = new ArrayList<ItemPojo>();
		for ( ItemPojo item : items ) {
			item.setCanBeRewarded(false);
			modified.add(item);
		}
		String database = "babywar", namespace="server0001", collection="items";
		for ( ItemPojo item : modified ) {
			MapDBObject dbObject = new MapDBObject();
			dbObject.putAll(item);
			DBObject query = MongoDBUtil.createDBObject("_id", item.getId());
			MongoDBUtil.saveToMongo(query, dbObject, database, namespace, collection, true);
		}
	}
	
	@Test
	public void testAllBoxRewardsIsRight() {
		ItemManager manager = ItemManager.getInstance();
		Collection<ItemPojo> items = manager.getItems();
		for ( ItemPojo item: items ) {
			ArrayList<Reward> rewards = item.getRewards();
			for ( Reward reward : rewards ) {
				switch ( reward.getType() ) {
					case ACHIVEMENT:
						assertEquals(Constant.ONE_NEGATIVE, reward.getId());
						break;
					case EXP:
						assertEquals(Constant.ONE_NEGATIVE, reward.getId());
						break;
					case GOLDEN:
						assertEquals(Constant.ONE_NEGATIVE, reward.getId());
						break;
					case MEDAL:
						assertEquals(Constant.ONE_NEGATIVE, reward.getId());
						break;
					case VOUCHER:
						assertEquals(Constant.ONE_NEGATIVE, reward.getId());
						break;
					case YUANBAO:
						assertEquals(Constant.ONE_NEGATIVE, reward.getId());
						break;
					case UNKNOWN:
						fail("should not contain UNKNOWN type");
						break;
					case ITEM:
					case STONE:
						String id = reward.getId();
						ItemPojo ip = ItemManager.getInstance().getItemById(id);
						assertNotNull("item="+ip.toString(), ip);
						break;
					case WEAPON:
						id = reward.getId();
						String typeId = reward.getTypeId();
						assertEquals(Constant.ONE_NEGATIVE, reward.getId());
						List<WeaponPojo> weapon = EquipManager.getInstance().getWeaponsByTypeName(typeId);
						assertTrue(weapon.size()>0);
						break;
				}
			}
		}
	}
}
