package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.config.MoneyType;
import com.xinqihd.sns.gameserver.config.ShopPojo;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo.Avatar;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseEquipment.BseEquipment;
import com.xinqihd.sns.gameserver.proto.XinqiBseZip.BseZip;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class EquipManagerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetWeaponById() {
		WeaponPojo pojo = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		System.out.println(pojo);
		assertNotNull(pojo);
	}

	@Test
	public void testGetWeapons() {
		Collection<WeaponPojo> weapons = EquipManager.getInstance().getWeapons();
		assertEquals(3001, weapons.size());
		for ( WeaponPojo weaponPojo : weapons ) {
			System.out.println(weaponPojo);
		}
	}
	
	@Test
	public void testBseZip() throws Exception {
		BseZip zip = EquipManager.getInstance().toBseZip();
		byte[] bytes = zip.getPayload().toByteArray();
		FileOutputStream fos = new FileOutputStream(new File("BseEquipment.gz"));
		fos.write(bytes);
		fos.close();
	}
	
	@Test
	public void testGetWeaponsBySlot() {
		Collection<WeaponPojo> weapons = EquipManager.getInstance().getWeaponsBySlot(EquipType.WEAPON);
		assertTrue("weapons.size()="+weapons.size(), weapons.size()>=260);
//		for ( WeaponPojo weaponPojo : weapons ) {
//			System.out.println(weaponPojo);
//		}
	}
	
	@Test
	public void testGetRandomSuit() {
		User user = new User();
		user.setLevelSimple(10);
		
		WeaponPojo suit = EquipManager.getInstance().getRandomWeapon(user, EquipType.SUIT, 1);
		System.out.println(suit);
		assertEquals(1, suit.getQuality());
		assertEquals(10, suit.getUserLevel());
	}
	
	@Test
	public void testGetByTypeNameAndLevel() {
		User user = new User();
		user.setLevelSimple(10);
		
		WeaponPojo weapon = EquipManager.getInstance().getWeaponByTypeNameAndUserLevel("301", 10);
		System.out.println(weapon);
	}
	
	@Test
	public void testGetRandomSuitQuality() {
		User user = new User();
		user.setLevelSimple(20);
		
		WeaponPojo suit = EquipManager.getInstance().getRandomWeapon(user, EquipType.WEAPON, 2);
		System.out.println(suit);
		assertEquals(2, suit.getQuality());
		assertEquals(20, suit.getUserLevel());
		
	}

	/*
	public void testGetWeaponByGender() {
		EquipManager manager = EquipManager.getInstance();
		List<WeaponPojo> fWeapons = manager.getWeaponsByGender(Gender.FEMALE);
		assertEquals(1180, fWeapons.size());
		List<WeaponPojo> mWeapons = manager.getWeaponsByGender(Gender.MALE);
		assertEquals(1840, mWeapons.size());
		List<WeaponPojo> nWeapons = manager.getWeaponsByGender(Gender.NONE);
		assertEquals(null, nWeapons);
	}
	*/

	@Test
	public void testToBseEquipment() {
		BseEquipment bseEquipment = EquipManager.getInstance().toBseEquipment();
		assertEquals(3001, bseEquipment.getWeaponsCount());
		System.out.println(bseEquipment.getSerializedSize());
	}
	
	@Test
	public void testCheckUserLevel() {
		User user = UserManager.getInstance().createDefaultUser();
		user.setLevel(1);
		//0●南瓜头
		EquipManager manager = EquipManager.getInstance();
		WeaponPojo weapon = manager.getWeaponById("0");
		boolean result = manager.checkWeaponLevelForUser(user, weapon.getId());
		assertEquals(true, result);
		weapon = manager.getWeaponById("1");
		result = manager.checkWeaponLevelForUser(user, weapon.getId());
		assertEquals(false, result);
		user.setLevel(10);
		result = manager.checkWeaponLevelForUser(user, weapon.getId());
		assertEquals(true, result);
	}
	
	@Test
	public void testGetWeaponsByTypeName() {
		EquipManager manager = EquipManager.getInstance();
		List<WeaponPojo> weapons = manager.getWeaponsByTypeName("87");
		List<WeaponPojo> sortWeapons = new ArrayList<WeaponPojo>(weapons);
		Collections.sort(sortWeapons);
		assertArrayEquals(sortWeapons.toArray(), weapons.toArray());
		
		weapons = manager.getWeaponsByTypeName("100");
		assertTrue(weapons.size()>0);
	}
	
	@Test
	public void testGetRandomWeaponsByGenderAndLevel() {
		User user = new User();
		user.setLevel(0);
		EquipManager manager = EquipManager.getInstance();
		WeaponPojo weapon = manager.getRandomWeaponByGenderAndLevel(Gender.MALE, user);
		System.out.println(weapon.getName());
		assertTrue(weapon.getUserLevel() <= 10);
		assertTrue(weapon.getSex() == Gender.ALL || 
				weapon.getSex() == Gender.MALE);
		
		user.setLevel(11);
		weapon = manager.getRandomWeaponByGenderAndLevel(Gender.FEMALE, user);
		System.out.println(weapon);
		assertTrue(weapon.getUserLevel() <= 20);
		assertTrue(weapon.getSex() == Gender.ALL || 
				weapon.getSex() == Gender.FEMALE);
		
		user.setLevel(100);
		weapon = manager.getRandomWeaponByGenderAndLevel(Gender.FEMALE, user);
		System.out.println(weapon.getName());
		assertTrue(weapon.getUserLevel() <= 100);
		assertTrue(weapon.getSex() == Gender.ALL || 
				weapon.getSex() == Gender.FEMALE);
		
		for ( int i=0; i<100; i++ ) {
			weapon = manager.getRandomWeaponByGenderAndLevel(Gender.FEMALE, user);
			System.out.println(weapon.getName()+", "+weapon.getUserLevel());			
		}
	}
	
	@Test
	public void testGetWeaponsByUserLevelAndTypeName() {
		EquipManager manager = EquipManager.getInstance();
		WeaponPojo weapon = manager.getWeaponByTypeNameAndUserLevel("38", 100);
		assertNotNull(weapon);
	}
	
	@Test
	public void testGetRandomWeaponByQualityAndLevel() {
		EquipManager manager = EquipManager.getInstance();
		int userLevel = 20;
		int count = 1000;
		HashSet<String> set = new HashSet<String>();
		for ( int i=0; i<count; i++ ) {
			WeaponPojo weapon = manager.getRandomWeaponByQualityAndLevel(20, 2);
			set.add(weapon.getName());
		}
		for ( String name : set ) {
			System.out.println(name);
		}
	}
	
	@Test
	public void testCompareWeapon() {
		EquipManager manager = EquipManager.getInstance();
		WeaponPojo weapon = manager.getWeaponById(UserManager.basicWeaponItemId);
		PropData white = weapon.toPropData(30, WeaponColor.WHITE);
		System.out.println(white.toDetailString());
		PropData orange = weapon.toPropData(30, WeaponColor.ORGANCE);
		System.out.println(orange.toDetailString());
		orange.toXinqiPropData(null);
	}
	
	@Test
	public void testPrintAllWeaponColors() {
		WeaponColor colors[] = WeaponColor.values();
		for ( WeaponColor color : colors ) {
			System.out.println(color.name()+":"+color.toIntColor());
		}
	}
	
	/**
	 * 很多武器的性别都默认为男性，实际上应该根据图标名字的前缀判断性别
	 */
	public void fixWeaponGender() {
		EquipManager manager = EquipManager.getInstance();
		Collection<WeaponPojo> weapons = manager.getWeapons();
		ArrayList<WeaponPojo> modified = new ArrayList<WeaponPojo>();
		for ( WeaponPojo weapon : weapons ) {
			if ( weapon.getIcon().startsWith("Female") && weapon.getSex() != Gender.FEMALE) {
				System.out.println(weapon.getId()+":"+weapon.getName()+":"+weapon.getSex());
				weapon.setSex(Gender.FEMALE);
				modified.add(weapon);
			}
		}
		String database = "babywar", namespace="server0001", collection="equipments_new";
		for ( WeaponPojo weapon : modified ) {
			MapDBObject dbObject = new MapDBObject();
			dbObject.putAll(weapon);
			DBObject query = MongoDBUtil.createDBObject("_id", weapon.getId());
			MongoDBUtil.saveToMongo(query, dbObject, database, namespace, collection, true);
		}
	}
	
	public void fixWeaponAvatar() {
		/**
		 * weapon_back
		 * weapon_front
		 */
		EquipManager manager = EquipManager.getInstance();
		Collection<WeaponPojo> weapons = manager.getWeapons();
		ArrayList<WeaponPojo> modified = new ArrayList<WeaponPojo>();
		for ( WeaponPojo weapon : weapons ) {
			if ( 
					(
//					weapon.getSlot() == EquipType.WING ||
//					weapon.getSlot() == EquipType.WEAPON ||
//					weapon.getSlot() == EquipType.HAIR ||
//					weapon.getSlot() == EquipType.HAT ||
//					weapon.getSlot() == EquipType.CLOTHES || 
//					weapon.getSlot() == EquipType.GLASSES 
							weapon.getSlot() == EquipType.OFFHANDWEAPON
					) && weapon.getAvatar().size() == 0 ) {
				Avatar a = new Avatar();
				a.id = "";
				a.layer = "weapon_back";
				weapon.getAvatar().add(a);
				a = new Avatar();
				a.id = "";
				a.layer = "weapon_front";
				weapon.getAvatar().add(a);
				modified.add(weapon);
			}
		}
		String database = "babywar", namespace="server0001", collection="equipments_new";
		for ( WeaponPojo weapon : modified ) {
			MapDBObject dbObject = new MapDBObject();
			dbObject.putAll(weapon);
			DBObject query = MongoDBUtil.createDBObject("_id", weapon.getId());
			MongoDBUtil.saveToMongo(query, dbObject, database, namespace, collection, true);
			System.out.println(dbObject);
		}
	}
	
	/**
	 * 修改武器的战斗力数值
	 */
	@Test
	public void modifyThePower() {
		EquipManager manager = EquipManager.getInstance();
		Collection<WeaponPojo> weapons = manager.getWeapons();
		StringBuilder buf = new StringBuilder(2000);
		for ( WeaponPojo weapon : weapons ) {
			int newPower = (int)Math.round(EquipCalculator.calculateWeaponPower(
					weapon));
			int oldPower = weapon.getPower();
			weapon.setPower(newPower);
			buf.append(weapon.getName()).append("\t").append(oldPower).
				append("\t").append(newPower).append("\n");
		}
		String database = "babywar", namespace="server0001", collection="equipments_new";
		for ( WeaponPojo weapon : weapons ) {
			MapDBObject dbObject = new MapDBObject();
			dbObject.putAll(weapon);
			DBObject query = MongoDBUtil.createDBObject("_id", weapon.getId());
			MongoDBUtil.saveToMongo(query, dbObject, database, namespace, collection, true);
		}
		System.out.println(buf.toString());
	}
	
	/**
	 * 将武器的敏捷和幸运属性上限从10000降至1000
	 */
	public void modifyTheLuckyAndAgility() {
		EquipManager manager = EquipManager.getInstance();
		Collection<WeaponPojo> weapons = manager.getWeapons();
		for ( WeaponPojo weapon : weapons ) {
			weapon.setAddLuck( Math.round(weapon.getAddLuck()/10f) );
			weapon.setAddAgility( Math.round(weapon.getAddAgility()/10f) );
		}
		String database = "babywar", namespace="server0001", collection="equipments_new";
		for ( WeaponPojo weapon : weapons ) {
			MapDBObject dbObject = new MapDBObject();
			dbObject.putAll(weapon);
			DBObject query = MongoDBUtil.createDBObject("_id", weapon.getId());
			MongoDBUtil.saveToMongo(query, dbObject, database, namespace, collection, true);
		}
	}
	
	/**
	 * 将武器的护甲值/4加到防御上，护甲值清零
	 */
	public void modifySkinAndDefend() {
		EquipManager manager = EquipManager.getInstance();
		Collection<WeaponPojo> weapons = manager.getWeapons();
		for ( WeaponPojo weapon : weapons ) {
			int skin = weapon.getAddSkin();
			weapon.setAddSkin( 0 );
			weapon.setAddDefend( weapon.getAddDefend() + (int)Math.round(skin/4.0) );
		}
		String database = "babywar", namespace="server0001", collection="equipments_new";
		for ( WeaponPojo weapon : weapons ) {
			MapDBObject dbObject = new MapDBObject();
			dbObject.putAll(weapon);
			DBObject query = MongoDBUtil.createDBObject("_id", weapon.getId());
			MongoDBUtil.saveToMongo(query, dbObject, database, namespace, collection, true);
		}
	}
	
	/**
	 * 将武器的增加一个新属性：canBeRewarded
	 */
	public void addCanBeRewardedField() {
		EquipManager manager = EquipManager.getInstance();
		Collection<WeaponPojo> weapons = manager.getWeapons();
		String database = "babywar", namespace="server0001", collection="equipments_new";
		for ( WeaponPojo weapon : weapons ) {
			MapDBObject dbObject = new MapDBObject();
			dbObject.putAll(weapon);
			DBObject query = MongoDBUtil.createDBObject("_id", weapon.getId());
			MongoDBUtil.saveToMongo(query, dbObject, database, namespace, collection, true);
		}
	}
	
	/**
	 * 替换新装备表中的性别
	 */
	public void replaceSexInEquips() {
		String database = "babywar";
		String namespace = "server0001";
		String oldCol = "equipments";
		String newCol = "equipments_new";
		
		DBObject query = MongoDBUtil.createDBObject();
		List<DBObject> list = MongoDBUtil.queryAllFromMongo(
				query, database, namespace, oldCol, null);
		HashMap<String, DBObject> objMaps = new HashMap<String, DBObject>();
		for ( DBObject obj : list ) {
			String name = obj.get("name").toString();
			objMaps.put(name, obj);
		}
		
		Collection<WeaponPojo> weapons = EquipManager.getInstance().getWeapons();
		for ( WeaponPojo weapon : weapons ) {
			String wname = weapon.getName();
			if ( wname.indexOf('●')<0 ) {
				continue;
			}
			String name = weapon.getName().substring(3);
			DBObject obj = objMaps.get(name);
			String sex = obj.get("sex").toString();
			if ( !sex.equals(weapon.getSex().toString()) ) {
				System.out.println("weaponId:"+weapon.getName()+", old sex:"+sex+", sex: " + weapon.getSex().toString());
			}
			weapon.setSex(Gender.valueOf(sex));
			if ( weapon.getSlot() == EquipType.WEAPON || 
					weapon.getSlot() == EquipType.BUBBLE ||
					weapon.getSlot() == EquipType.JEWELRY
					) {
				weapon.setSex(Gender.ALL);
			}
		}
		
		for ( WeaponPojo weapon : weapons ) {
			MapDBObject objToSave = new MapDBObject();
			objToSave.putAll(weapon);
			query = MongoDBUtil.createDBObject("_id", weapon.getId());
			MongoDBUtil.saveToMongo(query, objToSave, database, namespace, 
					newCol, true);
		}
	}
	
	/**
	 * Add bullet range
	 */
	public void addBulletRange() {
		String[][] bullets = {
			//#ID","名称","类型ID","子弹宽度","子弹高度"},
				{"500","黑铁●大蝎子","50","180","143"},
				{"510","黑铁●小蝎子","51","135","107"},
				{"520","黑铁●火箭炮","52","165","75"},
				{"530","黑铁●大火球","53","180","143"},
				{"540","黑铁●大哥大","54","135","75"},
				{"550","黑铁●矿泉水","55","135","75"},
				{"560","黑铁●鹰眼刀","56","128","98"},
				{"570","黑铁●榴弹炮","57","165","75"},
				{"580","黑铁●夺命刀","58","128","98"},
				{"590","黑铁●高跟鞋诱惑","59","152","120"},
				{"600","黑铁●轰天炮","60","180","143"},
				{"610","黑铁●急速锯","61","165","105"},
				{"620","黑铁●玉米篮","62","135","75"},
				{"630","黑铁●西红柿盆","63","120","98"},
				{"640","黑铁●穿云弩","64","158","98"},
				{"650","黑铁●弹簧拳","65","143","113"},
				{"660","黑铁●电火枪","66","150","128"},
				{"670","黑铁●朱雀羽","67","105","98"},
				{"680","黑铁●青龙鳞","68","90","75"},
				{"690","黑铁●白虎牙","69","105","92"},
				{"700","黑铁●玄武壳","70","150","150"},
				{"710","黑铁●薇薇安","71","173","60"},
				{"720","黑铁●南瓜灯","72","128","105"},
				{"730","黑铁●圣枪-朗基努斯","73","345","84"},
				{"740","黑铁●泡泡手雷","74","162","105"},
				{"2010","黑铁●火枪","201","150","128"},
		};
		EquipManager manager = EquipManager.getInstance();
		Collection<WeaponPojo> weapons = manager.getWeapons();
		ArrayList<WeaponPojo> modified = new ArrayList<WeaponPojo>();
		for ( WeaponPojo weapon : weapons ) {
			for ( String[] bullet : bullets ) {
				if ( bullet[2].equals(weapon.getTypeName()) ) {
					System.out.println(weapon.getName()+";width(radius):"+bullet[3]+";height(sRadius):"+bullet[4]);
					weapon.setRadius(StringUtil.toInt(bullet[3], 0));
					weapon.setsRadius(StringUtil.toInt(bullet[4], 0));
				}
			}
		}
		String database = "babywar", namespace="server0001", collection="equipments_new";
		for ( WeaponPojo weapon : weapons ) {
			MapDBObject dbObject = new MapDBObject();
			dbObject.putAll(weapon);
			DBObject query = MongoDBUtil.createDBObject("_id", weapon.getId());
			MongoDBUtil.saveToMongo(query, dbObject, database, namespace, collection, true);
		}
	}
	
	/**
	 * 修改武器的颜色为白色，品质为普通
	 */
	public void changeWeaponColorAndQuality() {
		EquipManager manager = EquipManager.getInstance();
		Collection<WeaponPojo> weapons = manager.getWeapons();
		ArrayList<WeaponPojo> modified = new ArrayList<WeaponPojo>();
		for ( WeaponPojo weapon : weapons ) {
			boolean hasYuanbao = true;
			Collection<ShopPojo> shops = ShopManager.getInstance().getShopsByPropInfoId(weapon.getId());
			if ( shops != null ) {
				for ( ShopPojo shop : shops ) {
					MoneyType money = shop.getMoneyType();
					if ( money == MoneyType.GOLDEN ) {
						hasYuanbao = false;
						break;
					}
				}
			} else {
				hasYuanbao = false;
			}
			int quality = 1;
			if ( hasYuanbao ) {
				quality = 2;
			}
			weapon.setQuality(quality);
			weapon.setQualityColor(WeaponColor.WHITE);
			System.out.println(weapon.getName()+", "+weapon.getQuality()+", "+weapon.getQualityColor());
		}
		String database = "babywar", namespace="server0001", collection="equipments_new";
		for ( WeaponPojo weapon : weapons ) {
			MapDBObject dbObject = new MapDBObject();
			dbObject.putAll(weapon);
			DBObject query = MongoDBUtil.createDBObject("_id", weapon.getId());
			MongoDBUtil.saveToMongo(query, dbObject, database, namespace, collection, true);
		}
	}
	
	/**
	 * 为装备增加一个新的字段
	 */
	public void addNewFieldForWeaponPojo() {
		EquipManager manager = EquipManager.getInstance();
		Collection<WeaponPojo> weapons = manager.getWeapons();
		String database = "babywar", namespace="server0001", collection="equipments_new";
		for ( WeaponPojo weapon : weapons ) {
			MapDBObject dbObject = new MapDBObject();
			dbObject.putAll(weapon);
			DBObject query = MongoDBUtil.createDBObject("_id", weapon.getId());
			MongoDBUtil.saveToMongo(query, dbObject, database, namespace, collection, true);
		}
	}
}
