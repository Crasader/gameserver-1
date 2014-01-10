package script.function;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.LevelManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEnhanceField;
import com.xinqihd.sns.gameserver.entity.user.PropDataEnhanceType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiPropData;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.util.MathUtil;
import com.xinqihd.sns.gameserver.util.Text;

public class EquipCalculatorTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testCalculateStrengthAttackUpAndDown() {
		int attack = 59;
		int newAttack = attack;
		int strengthLevel=5; 
		newAttack = EquipCalculator.calculateStrengthAttack(attack, strengthLevel);
		System.out.println(attack + "->" + strengthLevel + "=" + ((int)(newAttack*1000))/1000.0);
		
		int attack2 = (int) EquipCalculator.calculateStrengthAttack(newAttack, -strengthLevel);
		System.out.println(newAttack + " -> -" + strengthLevel + " \t " + attack2);
		assertEquals(attack, attack2);
		
		strengthLevel = 10;
		newAttack = EquipCalculator.calculateStrengthAttack(attack, strengthLevel);
		System.out.println(attack + "->" + strengthLevel + "=" + ((int)(newAttack*1000))/1000.0);
		attack2 = (int) EquipCalculator.calculateStrengthAttack(newAttack, -strengthLevel);
		System.out.println(newAttack + " -> -" + strengthLevel + " \t " + attack2);
		assertEquals(attack, attack2);
		
		strengthLevel = 12;
		newAttack = EquipCalculator.calculateStrengthAttack(attack, strengthLevel);
		System.out.println(attack + "->" + strengthLevel + "=" + ((int)(newAttack*1000))/1000.0);
		attack2 = (int) EquipCalculator.calculateStrengthAttack(newAttack, -strengthLevel);
		System.out.println(newAttack + " -> -" + strengthLevel + " \t " + attack2);
		assertEquals(attack, attack2);
	}
		
	@Test
	public void testCalculateStrengthAttackUpAndDownForLowValue() {
		int attack = 9;
		int newAttack = attack;
		int strengthLevel=12; 
		int lastAttack = attack;
		for ( int i=1; i<=strengthLevel; i++ ) {
			newAttack = EquipCalculator.calculateStrengthAttack(newAttack, 1);
			System.out.println(lastAttack + "->" + i + "=" + newAttack);
			lastAttack = newAttack;
		}
		int initAttack = newAttack;
		for ( int i=strengthLevel; i>=1; i-- ) {
			newAttack = EquipCalculator.calculateStrengthAttack(newAttack, -1);
			System.out.println(initAttack + "->" + i + "=" + newAttack);
			initAttack = newAttack;
		}
		assertEquals(attack, initAttack);
	}
	
	@Test
	public void testCalculateStrengthDefendUpAndDownForLowValue() {
		int defend = 9;
		int newDefend = defend;
		int strengthLevel=12; 
		int lastDefend = defend;
		for ( int i=1; i<=strengthLevel; i++ ) {
			newDefend = EquipCalculator.calculateStrengthDefend(newDefend, 1);
			System.out.println(lastDefend + "->" + i + "=" + newDefend);
			lastDefend = newDefend;
		}
		int initDefend = newDefend;
		for ( int i=strengthLevel; i>=1; i-- ) {
			newDefend = EquipCalculator.calculateStrengthDefend(newDefend, -1);
			System.out.println(initDefend + "->" + i + "=" + newDefend);
			initDefend = newDefend;
		}
		assertEquals(defend, initDefend);
	}
	
	@Test
	public void testCalculateStrengthDefendUpAndDown() {
		int defend = 59;
		int newDefend = defend;
		int strengthLevel=5; 
		newDefend = EquipCalculator.calculateStrengthDefend(defend, strengthLevel);
		System.out.println(defend + "->" + strengthLevel + "=" + ((int)(newDefend*1000))/1000.0);
		
		int defend2 = (int) EquipCalculator.calculateStrengthDefend(newDefend, -strengthLevel);
		System.out.println(newDefend + " -> -" + strengthLevel + " \t " + defend2);
		assertEquals(defend, defend2);
		
		strengthLevel = 10;
		newDefend = EquipCalculator.calculateStrengthDefend(defend, strengthLevel);
		System.out.println(defend + "->" + strengthLevel + "=" + ((int)(newDefend*1000))/1000.0);
		defend2 = (int) EquipCalculator.calculateStrengthDefend(newDefend, -strengthLevel);
		System.out.println(newDefend + " -> -" + strengthLevel + " \t " + defend2);
		assertEquals(defend, defend2);
		
		strengthLevel = 12;
		newDefend = EquipCalculator.calculateStrengthDefend(defend, strengthLevel);
		System.out.println(defend + "->" + strengthLevel + "=" + ((int)(newDefend*1000))/1000.0);
		defend2 = (int) EquipCalculator.calculateStrengthDefend(newDefend, -strengthLevel);
		System.out.println(newDefend + " -> -" + strengthLevel + " \t " + defend2);
		assertEquals(defend, defend2);
	}

	/**
	 * attack 	 strengthLevel 	 newAttack
   * 100 	 0 	 100.0
   * 100 	 1 	 140.0
   * 100 	 2 	 195.999
   * 100 	 3 	 274.399
   * 100 	 4 	 384.159
   * 100 	 5 	 537.823
   * 100 	 6 	 752.953
   * 100 	 7 	 1054.135
   * 100 	 8 	 1475.789
   * 100 	 9 	 2066.104
	 */
	@Test
	public void testCalculateStrengthAttack() {
		System.out.println("attack \t strengthLevel \t newAttack"); 
		int max = 13;
		for ( int attack = 100; attack<=100; attack+=50) {
			double newAttack = attack;
			for ( int strengthLevel=0; strengthLevel<max; strengthLevel++ ) {
				int attack1 = EquipCalculator.calculateStrengthAttack(attack, strengthLevel);
				System.out.println(attack + " \t " + strengthLevel + " \t " + ((int)(attack1*1000))/1000.0);
			}
		}
	}
		
	@Test
	public void testCalculateStrengthAttackTo10() {
		System.out.println("attack \t strengthLevel \t newAttack"); 
		int max = 10;
		int attack = 2000;
		int newAttack = EquipCalculator.calculateStrengthAttack(attack, 12);
		System.out.println(attack + " \t " + attack + " \t " + newAttack);
	}

	@Test
	public void testCalculateStrengthDefend() {
		System.out.println("attack \t strengthLevel \t newDefend"); 
		int max = 10;
		for ( int defend = 20; defend<=200; defend+=10) {
			double newDefend = defend;
			for ( int strengthLevel=0; strengthLevel<max; strengthLevel++ ) {
				newDefend = EquipCalculator.calculateStrengthAttack(defend, strengthLevel);
				System.out.println(defend + " \t " + strengthLevel + " \t " + ((int)(newDefend*1000))/1000.0);
			}
		}
	}

	/**
	 * 打印所有等级的强化石强化所有等级武器的成功概率
	 */
	@Test
	public void testCalculateStrengthStoneSuccessRatio() {
		System.out.println("stoneLevel \t targetLevel \t successRatio"); 
		for ( int stoneLevel = 1; stoneLevel < 6; stoneLevel++ ) {
			for ( int targetLevel = 1; targetLevel < 13; targetLevel++ ) {
				double successRatio = EquipCalculator.calculateStrengthStoneSuccessRatio(stoneLevel, targetLevel);
				System.out.println(stoneLevel + " \t " + targetLevel + " \t " + Math.round(successRatio*100000)/1000.0+"%");
			}
		}
	}
	
	/**
	 * 打印基础强化成功概率
	 * 
   * StoneLevel: 1, TargetLevel: 1, Success: 46.05
   * StoneLevel: 1, TargetLevel: 2, Success: 31.94
   * StoneLevel: 1, TargetLevel: 3, Success: 15.98
   * StoneLevel: 1, TargetLevel: 4, Success: 5.54
   * StoneLevel: 1, TargetLevel: 5, Success: 1.21
   * StoneLevel: 1, TargetLevel: 6, Success: 0.26
   * StoneLevel: 1, TargetLevel: 7, Success: 0.04
   * StoneLevel: 1, TargetLevel: 8, Success: 0.0
   * StoneLevel: 1, TargetLevel: 9, Success: 0.0
   * StoneLevel: 1, TargetLevel: 10, Success: 0.0
   * 
	 */
	@Test
	public void testCalculateEquipStrength() {
		int tryTimes = 10000;
		int stoneLevel = 1;
		double luckyCardSuccessRatio = 0.0;
		int[] targetSuccess = new int[10];
		for ( int targetLevel=1; targetLevel<10; targetLevel++ ) {
			for ( int i=0; i<tryTimes; i++ ) {
				boolean success = EquipCalculator.forgeEquip(null,  
						new double[]{EquipCalculator.calculateStrengthStoneSuccessRatio(stoneLevel, targetLevel)}, 
						luckyCardSuccessRatio, 0);
				if ( success ) {
					targetSuccess[targetLevel-1]++;
//					System.out.println("StoneLevel: " + stoneLevel + ", TargetLevel: " 
//							+ targetLevel + ", tryTimes: " + i );
//					break;
				}
			}
		}
		for ( int i=1; i<=targetSuccess.length; i++ ) {
			System.out.println("StoneLevel: " + stoneLevel + ", TargetLevel: " 
					+ i + ", Success: " + (targetSuccess[i-1]*100.0/tryTimes) );
		}
	}
	
	/**
	 * 打印基础合成成功概率
	 * StoneLevel 	 ComposeRatio: 
			1	 0.15829606
			2	 0.05548033
			3	 0.01369394
			4	 0.0023803
			5	 2.923E-4
			6	 2.519E-5
			7	 2.519E-5
			8	 2.519E-5
			9	 2.519E-5
	 */
	@Test
	public void testCalculateComposeItem() {
		double[] targetSuccess = new double[10];
		System.out.println("StoneLevel \t ComposeRatio: ");
		for ( int targetLevel=1; targetLevel<10; targetLevel++ ) {
			targetSuccess[targetLevel-1] = EquipCalculator.calculateComposeItemSuccessRatio( targetLevel, 1 );
			System.out.println(targetLevel + "\t " + targetSuccess[targetLevel-1]);
		}
	}
	
	/**
	 * 打印合成石合成效果的列表
	 * 品质	数量		概率
	 */
	@Test
	public void testCalculateForgeData() {
		double[] qArray = GameDataManager.getInstance().getGameDataAsDoubleArray(GameDataKey.FORGE_SIGMA_RATIO);
		int loop = 10000;
		double luckStone = 0.0;
		HashMap<Integer, Integer> countMap = new HashMap<Integer, Integer>();
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		PropData equip = weapon.toPropData(10, WeaponColor.WHITE);
		for ( int i=0; i<qArray.length; i++ ) {
			double q = qArray[i];
			System.out.println("Stone Level:" + i + "\t q="+q);
			System.out.println("quality"+"\t count:"+ "\t percent:%");
			for ( int k=0; k<loop; k++ ) {
				int finalData = (int)EquipCalculator.calculateForgeData(equip, i+1, ItemManager.attackStoneId);
				int number = 1;
				if ( countMap.containsKey(finalData) ) {
					number = countMap.get(finalData) + 1;
				}
				countMap.put(finalData, number);
			}
			ArrayList<Integer> keyList = new ArrayList<Integer>(countMap.keySet());
			Collections.sort(keyList);
			for ( Integer key : keyList ) {
				int number = countMap.get(key);
				System.out.println(key+"\t"+ number + "\t"+(number*100.0/loop/qArray.length)+"%");
			}
		}
		
		luckStone = 0.15;
		for ( int i=0; i<qArray.length; i++ ) {
			double q = qArray[i];
			for ( int k=0; k<loop; k++ ) {
				int finalData = (int)EquipCalculator.calculateForgeData(equip, i+1, ItemManager.attackStoneId);
				int number = 1;
				if ( countMap.containsKey(finalData) ) {
					number = countMap.get(finalData) + 1;
				}
				countMap.put(finalData, number);
			}
			ArrayList<Integer> keyList = new ArrayList<Integer>(countMap.keySet());
			Collections.sort(keyList);
			for ( Integer key : keyList ) {
				int number = countMap.get(key);
				System.out.println(key+"\t"+ number + "\t"+(number*100.0/loop)+"%");
			}
		}
		
		luckStone = 0.25;
		for ( int i=0; i<qArray.length; i++ ) {
			double q = qArray[i];
			System.out.println("LuckyStone: 0.25 \t Stone Level:" + i + "\t q="+q);
			System.out.println("quality"+"\t count:"+ "\t percent:%");
			for ( int k=0; k<loop; k++ ) {
				int finalData = (int)EquipCalculator.calculateForgeData(equip, i+1, 
						ItemManager.attackStoneId);
				int number = 1;
				if ( countMap.containsKey(finalData) ) {
					number = countMap.get(finalData) + 1;
				}
				countMap.put(finalData, number);
			}
			ArrayList<Integer> keyList = new ArrayList<Integer>(countMap.keySet());
			Collections.sort(keyList);
			for ( Integer key : keyList ) {
				int number = countMap.get(key);
				System.out.println(key+"\t"+ number + "\t"+(number*100.0/loop)+"%");
			}
		}
	}
	
	@Test
	public void testCalculateForgeDataWithAttack() {
		double luckStone = 0.25;
		int stoneLevel = 5;
		int max = 10000;
		String stoneTypeId = ItemManager.attackStoneId;
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		PropData prop = weapon.toPropData(1, WeaponColor.WHITE);
		HashMap<Integer, Integer> countMap = new HashMap<Integer, Integer>(); 
		for ( int s=1; s<=stoneLevel; s++ ) {
			for ( int i=0; i<max; i++ ) {
					int finalData = (int)EquipCalculator.calculateForgeData(
							prop, stoneLevel, stoneTypeId);
					Integer c = countMap.get(finalData);
					if ( c == null ) {
						countMap.put(finalData, 1);
					} else {
						countMap.put(finalData, c+1);
					}
			}
		}
		printCountMap(countMap, max*stoneLevel);
	}
	
	@Test
	public void testCalculateForgeDataWithDefend() {
		double luckStone = 0.25;
		int stoneLevel = 5;
		int max = 10000;
		String stoneTypeId = ItemManager.defendStoneId;
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		PropData prop = weapon.toPropData(1, WeaponColor.WHITE);
		HashMap<Integer, Integer> countMap = new HashMap<Integer, Integer>(); 
		for ( int s=1; s<=stoneLevel; s++ ) {
			for ( int i=0; i<max; i++ ) {
					int finalData = (int)EquipCalculator.calculateForgeData(
							prop, stoneLevel, stoneTypeId);
					Integer c = countMap.get(finalData);
					if ( c == null ) {
						countMap.put(finalData, 1);
					} else {
						countMap.put(finalData, c+1);
					}
			}
		}
		printCountMap(countMap, max*stoneLevel);
	}
	
	@Test
	public void testCalculateForgeDataWithLuck() {
		double luckStone = 0.25;
		int stoneLevel = 1;
		int max = 10000;
		String stoneTypeId = ItemManager.luckStoneId;
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		PropData prop = weapon.toPropData(1, WeaponColor.WHITE);
		HashMap<Integer, Integer> countMap = new HashMap<Integer, Integer>(); 
		for ( int s=1; s<=stoneLevel; s++ ) {
			for ( int i=0; i<max; i++ ) {
					int finalData = (int)EquipCalculator.calculateForgeData(
							prop, stoneLevel, stoneTypeId);
					Integer c = countMap.get(finalData);
					if ( c == null ) {
						countMap.put(finalData, 1);
					} else {
						countMap.put(finalData, c+1);
					}
			}
		}
		printCountMap(countMap, max*stoneLevel);
	}
	
	@Test
	public void testColorWeaponLevelUpAndDown() {
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		int oldAttack = weapon.getAddAttack();
		int oldDefend = weapon.getAddDefend();
		int oldPower = weapon.getPower();
		
		//level 1
		PropData whitePropData = weapon.toPropData(10, WeaponColor.WHITE);
		EquipCalculator.weaponUpLevel(whitePropData, 1);
		assertEquals(oldAttack+1, whitePropData.getAttackLev());
		assertEquals(1, whitePropData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.DAMAGE));
		XinqiPropData.PropData whiteXinqiPropData = whitePropData.toXinqiPropData();
		assertEquals(1, whiteXinqiPropData.getAttackLev());
		
		PropData orgPropData = weapon.toPropData(10, WeaponColor.ORGANCE);
		EquipCalculator.weaponUpLevel(orgPropData, 1);		
		assertEquals((oldAttack+1)*2, orgPropData.getAttackLev());
		assertEquals(25, orgPropData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.DAMAGE));
		XinqiPropData.PropData orgXinqiPropData = orgPropData.toXinqiPropData();
		assertEquals(2, orgXinqiPropData.getAttackLev());
	}
	
	@Test
	public void testWeaponLevelUpAndDown() {
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		int oldAttack = weapon.getAddAttack();
		int oldDefend = weapon.getAddDefend();
		int oldPower = weapon.getPower();
		//level 1
		PropData propData = weapon.toPropData(10, WeaponColor.WHITE);
		EquipCalculator.weaponUpLevel(propData, 1);		
		assertEquals(oldAttack+1, propData.getAttackLev());
		assertEquals(1, propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.DAMAGE));
		//level 2
		EquipCalculator.weaponUpLevel(propData, 2);
		assertEquals(oldAttack+2, propData.getAttackLev());
		assertEquals(3, propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.DAMAGE));
		//level 3
		EquipCalculator.weaponUpLevel(propData, 3);
		assertEquals(oldAttack+3, propData.getAttackLev());
		assertEquals(4, propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.DAMAGE));
		//level 4
		EquipCalculator.weaponUpLevel(propData, 4);
		assertEquals(oldAttack+4, propData.getAttackLev());
		assertEquals(6, propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.DAMAGE));
		//level 5
		EquipCalculator.weaponUpLevel(propData, 5);
		assertEquals(oldAttack+5, propData.getAttackLev());
		assertEquals(7, propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.DAMAGE));

		//level down
		EquipCalculator.weaponUpLevel(propData, 4);
		EquipCalculator.weaponUpLevel(propData, 3);
		EquipCalculator.weaponUpLevel(propData, 2);
		EquipCalculator.weaponUpLevel(propData, 1);
		assertEquals(oldAttack+1, propData.getAttackLev());
		assertEquals(1, propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.DAMAGE));

	}
	
	@Test
	public void testWeaponLevelUpAndDownWithCompose() {
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		int oldAttack = weapon.getAddAttack();
		int oldDefend = weapon.getAddDefend();
		int oldPower = weapon.getPower();
		User user = new User();
		user.set_id(new UserId("test-001"));
		user.setGoldenSimple(20000);
//		//20020 火神石lv5
//		ItemPojo itemPojo = ItemManager.getInstance().getItemById("20020");
//		Bag bag = user.getBag();
//		bag.addOtherPropDatas(itemPojo.toPropData());
//		
//		//level 1
		PropData propData = weapon.toPropData(10, WeaponColor.WHITE);
//		bag.addOtherPropDatas(propData);
//		CraftManager.getInstance().forgeEquip(user, 21, new int[]{20});
		propData.setAttackLev(propData.getAttackLev()+20);
		propData.setEnhanceValue(PropDataEnhanceType.FORGE, PropDataEnhanceField.ATTACK, 20);
		
		EquipCalculator.weaponUpLevel(propData, 1);
		assertEquals(20+oldAttack+1, propData.getAttackLev());
		assertEquals(1, propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.DAMAGE));
		//level 2
		EquipCalculator.weaponUpLevel(propData, 2);
		assertEquals(20+oldAttack+2, propData.getAttackLev());
		assertEquals(3, propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.DAMAGE));
		//level 3
		EquipCalculator.weaponUpLevel(propData, 3);
		assertEquals(20+oldAttack+3, propData.getAttackLev());
		assertEquals(4, propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.DAMAGE));
		//level 4
		EquipCalculator.weaponUpLevel(propData, 4);
		assertEquals(20+oldAttack+4, propData.getAttackLev());
		assertEquals(6, propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.DAMAGE));
		//level 5
		EquipCalculator.weaponUpLevel(propData, 5);
		assertEquals(20+oldAttack+5, propData.getAttackLev());
		assertEquals(7, propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.DAMAGE));

		//level down
		EquipCalculator.weaponUpLevel(propData, 4);
		EquipCalculator.weaponUpLevel(propData, 3);
		EquipCalculator.weaponUpLevel(propData, 2);
		EquipCalculator.weaponUpLevel(propData, 1);
		assertEquals(20+oldAttack+1, propData.getAttackLev());
		assertEquals(1, propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.DAMAGE));
	}
	
	@Test
	public void testSkinLevelUpAndDownWithCompose() {
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		int oldDefend = weapon.getAddDefend();
		int oldPower = weapon.getPower();
		User user = new User();
		user.set_id(new UserId("test-001"));
		user.setGoldenSimple(20000);
//		//20020 火神石lv5
//		ItemPojo itemPojo = ItemManager.getInstance().getItemById("20020");
//		Bag bag = user.getBag();
//		bag.addOtherPropDatas(itemPojo.toPropData());
//		
//		//level 1
		PropData propData = weapon.toPropData(10, WeaponColor.WHITE);
		//Change weapon's type.
		weapon.setSlot(EquipType.CLOTHES);
		propData.setDefendLev(propData.getDefendLev()+20);
		propData.setEnhanceValue(PropDataEnhanceType.FORGE, PropDataEnhanceField.DEFEND, 20);
		
		int up = 0;
		EquipCalculator.weaponUpLevel(propData, 1);
		up = propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.SKIN);
		assertEquals(6, up);
		assertEquals(20+oldDefend+1, propData.getDefendLev());
		//level 2
		EquipCalculator.weaponUpLevel(propData, 2);
		up = propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.SKIN);
		assertEquals(7, up);
		assertEquals(20+oldDefend+2, propData.getDefendLev());
		//level 3
		EquipCalculator.weaponUpLevel(propData, 3);
		up = propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.SKIN);
		assertEquals(8, up);
		assertEquals(20+oldDefend+3, propData.getDefendLev());
		//level 4
		EquipCalculator.weaponUpLevel(propData, 4);
		up = propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.SKIN);
		assertEquals(9, up);
		assertEquals(20+oldDefend+4, propData.getDefendLev());
		//level 5
		EquipCalculator.weaponUpLevel(propData, 5);
		up = propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.SKIN);
		assertEquals(10, up);
		assertEquals(20+oldDefend+5, propData.getDefendLev());

		//level down
		EquipCalculator.weaponUpLevel(propData, 4);
		EquipCalculator.weaponUpLevel(propData, 3);
		EquipCalculator.weaponUpLevel(propData, 2);
		EquipCalculator.weaponUpLevel(propData, 1);
		assertEquals(20+oldDefend+1, propData.getDefendLev());
		assertEquals(6, propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.SKIN));
	}
	
	/**
   * 熔炼			
   * StrengthRatio: 1.4
   * 					
   * 举例，一个戒指，基本属性为			
   * 			攻击+10		
   * 			防御+5		
   * 			敏捷+5		
   * 			幸运+5		
   * 则					
   * 熔炼次数	攻击	防御	敏捷	幸运	数量
   *  Attack 	 Defend 	 Agility 	 Lucky 	
	 * 		10 	 5 	 5 	 5
	 * 		14 	 7 	 7 	 7
	 * 		19 	 9 	 9 	 9
	 * 		27 	 13 	 13 	 13
	 * 		38 	 19 	 19 	 19
	 * 		53 	 26 	 26 	 26
	 * 		75 	 37 	 37 	 37
	 * 		105 	 52 	 52 	 52
	 * 		147 	 73 	 73 	 73	
	 * 		206 	 103 	 103 	 103
   * 
	 */
	/*
	public void testCalculateStrengthJewery() {
		PropData ring = new PropData();
		ring.setAttackLev(10);
		ring.setDefendLev(5);
		ring.setAgilityLev(5);
		ring.setLuckLev(5);
		int level = 10;
		System.out.println("Attack \t Defend \t Agility \t Lucky \t");
		for ( int i=0; i<level; i++ ) {
			PropData clone = EquipCalculator.calculateStrengthJewery(ring, 0, i);
			System.out.println(clone.getAttackLev()+" \t "+clone.getDefendLev()+" \t "+clone.getAgilityLev()+" \t "+clone.getLuckLev());
		}
	}
	*/
	
	@Test
	public void testCalculateCraftPrice() {
		for ( int i=0; i<=LevelManager.MAX_LEVEL; i++ ) {
			int finalPrice = EquipCalculator.calculateCraftPrice(i, 200);
			System.out.println("user level "+ i + ", " + ", price : " + finalPrice);
		}
	}
	
	@Test
	public void testCalculateBloodPrice() {
		for ( int i=0; i<=LevelManager.MAX_LEVEL; i++ ) {
			int finalPrice = EquipCalculator.calculateBloodPrice(i, 200);
			System.out.println("user level "+ i + ", " + ", price : " + finalPrice);
		}
	}
	
	@Test
	public void testPropDataRareRatio() {
		int max =1000;
		for ( int i=0; i<max; i++ ) {
			WeaponPojo weapon = EquipManager.getInstance().getRandomWeaponByGenderAndLevel(Gender.MALE, 10);
			WeaponColor color = WeaponColor.values()[(int)(MathUtil.nextDouble()*5)];
			PropData pd = weapon.toPropData(30, color);
			double ratio = EquipCalculator.calculatePropDataRareRatio(pd);
			System.out.println(pd.getName()+","+pd.getMaxLevel()+","+pd.getTotalSlot()+": "+(ratio*100)+"%");
		}
	}
	
	@Test
	public void printWeaponPower() {
		StringBuilder buf = new StringBuilder();
		Collection<WeaponPojo> weapons = EquipManager.getInstance().getWeapons();
		for ( WeaponPojo weapon : weapons ) {
			if ( weapon.getSlot() != EquipType.WEAPON ) continue;
			double power = EquipCalculator.calculateWeaponPower(
					weapon.getAddAttack(), weapon.getAddDefend(), 
					weapon.getAddAgility(), weapon.getAddLuck(), 
					weapon.getAddBlood(), weapon.getAddSkin(), weapon.getRadius(), weapon.getAddBloodPercent());
			PropData propData = weapon.toPropData(30, WeaponColor.ORGANCE);
			double orangePower = EquipCalculator.calculateWeaponPower(
					propData.getAttackLev(), propData.getDefendLev(), 
					propData.getAgilityLev(), propData.getLuckLev(), 
					propData.getBloodLev(), propData.getSkinLev(), weapon.getRadius(), weapon.getAddBloodPercent());
			PropData strenPropData = EquipCalculator.weaponUpLevel(propData, 8);
			buf.append(weapon.getName()).append("\t").append(power).append("\n");
			buf.append(weapon.getName()).append("-orange").append("\t").append(orangePower).append("\n");
			buf.append(weapon.getName()).append("-s8").append("\t").append(strenPropData.getPower()).append("\n");
		}
		System.out.println(buf.toString());
	}
	
	/**
	 * 打印各个等级提升的数值范围
	 */
	@Test
	public void printEachLevelImprove() {
		String[] weaponLevel = {
				"黑铁","青铜","赤钢","白银","黄金","琥珀","翡翠","水晶","钻石","神圣"
		};
		StringBuilder buf = new StringBuilder();
		double baseLevelUpRatio = EquipCalculator.calculateStrengthAttack(100, 5)/100.0;
		for ( int i=1; i<=10; i++ ) {
			String weapon = weaponLevel[i];
			buf.append(weapon+"\t"+i*baseLevelUpRatio+"\r\n");
		}
		System.out.println(buf.toString());
	}

	private void printCountMap(HashMap<Integer, Integer> countMap, int max) {
		/*
		ArrayList<Integer> array = new ArrayList<Integer>(countMap.keySet());
		Collections.sort(array);
		for ( Integer finalData : array ) {
			System.out.println(finalData + " : " + countMap.get(finalData) + 
					" : " + (countMap.get(finalData)*100.0/max) + "%");
		}
		*/
	}
}
