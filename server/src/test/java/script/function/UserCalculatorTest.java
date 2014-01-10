package script.function;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.battle.BattleAuditItem;
import com.xinqihd.sns.gameserver.battle.BuffToolIndex;
import com.xinqihd.sns.gameserver.battle.BuffToolType;
import com.xinqihd.sns.gameserver.config.LevelPojo;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.LevelManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.function.UserCalculator;
import com.xinqihd.sns.gameserver.util.MathUtil;

public class UserCalculatorTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
    * 伤害	HP	死亡系数	命中率	等级差	人数系数	胜负系数
    * 0~HP	伤害~10000	0.3	0~1	-4~4	1	1
    * 		0			1.1	0.5
    * 					1.2	
    * 1000	1000	0.3	1	4	1.2	1
    * 0	1000	0	0	-4	1	0.5
    * 最大经验:	22.17419757					
    * 最低经验:	0					
    * 通常胜利:	9.6					
    * 通常失败:	2.2					
	 */
	@Test
	public void testBattleExpPrintResult() {
		/**
		 * totalHurt:54.0, 
		 * totalBlood:477.0, 
		 * killOthers:0, 
		 * hitRatio:0.27272728, 
		 * powerRatio:0.7502283, 
		 * numOfUsers:2, 
		 * winTheGame:false
		 */
		int[] numOfUsers = new int[]{1};
		
		for ( int i=0; i<numOfUsers.length; i++ ) {
			int numOfUser = numOfUsers[i];
			int totalBlood = 500;
			for ( int totalHurt = 100; totalHurt < totalBlood; totalHurt+=100 ) {
				for ( int kills = 0; kills <= numOfUser/2; kills++ ) {
					for ( int powerRatio = 20; powerRatio<=200; powerRatio+=20 ) {
						for ( int hitRatio = 0; hitRatio<=100; hitRatio+=10 ) {
							for ( int win = 0; win<=1; win++ ) {
								float hitIndex = hitRatio/100f;
								float powerIndex = powerRatio/100f;
								double finalExp = UserCalculator.calculateBattleExp(
										totalHurt, totalBlood, kills, hitIndex, powerIndex, numOfUser, win==1, true,
										5, true, 1, 1, 5, false, new HashMap<BattleAuditItem, Integer>());
								System.out.println("人数系数 \t 总伤害 \t 总血量 \t 杀死人数 \t 战斗力比值 \t 命中率 \t 胜负系数 \t 经验值");
								System.out.println(numOfUser+"\t"+totalHurt+"\t"+totalBlood+"\t"+kills+"\t"+powerIndex+"\t"+hitIndex+"\t"+(win==1)+"\t"+(int)(finalExp));
							}
						}
					}
				}
			}
		}
		

	}
	
	@Test
	public void testBattleExpZeroHurt() {
		int totalHurt = 351;
		int totalHp = 997;
		int killOthers =1;
		int hitRatio = 0;
		int powerRatio = 0;
		int numOfUser = 1;
		boolean win = true;
		
		double finalExp = UserCalculator.calculateBattleExp(
				totalHurt, totalHp, killOthers, hitRatio, powerRatio, numOfUser, win, true, true);
		System.out.println("finalExp: " + finalExp);
		assertEquals(158, (int)finalExp, 0.01);
	}
	
	@Test
	public void testBattleExpStrongPower() {
		int totalHurt = 351;
		int totalHp = 997;
		int killOthers =1;
		int hitRatio = 0;
		//Attacker/myself
		float powerRatio = 200.0f/100;
		int numOfUser = 1;
		boolean win = true;
		
		double finalExp = UserCalculator.calculateBattleExp(
				totalHurt, totalHp, killOthers, hitRatio, powerRatio, numOfUser, win, true, true);
		System.out.println("finalExp: " + finalExp);
		assertTrue("powerExp >0", (int)finalExp>0);
	}
	
	@Test
	public void testBattleExpWeakerPower() {
		int totalHurt = 351;
		int totalHp = 997;
		int killOthers =1;
		int hitRatio = 0;
		//Attacker/myself
		float powerRatio = 100.0f/200;
		int numOfUser = 1;
		boolean win = true;
		
		double finalExp = UserCalculator.calculateBattleExp(
				totalHurt, totalHp, killOthers, hitRatio, powerRatio, numOfUser, win, true, false);
		System.out.println("finalExp: " + finalExp);
		assertEquals("powerExp ==1", (int)finalExp, 1.0, 0.01);
	}


	@Test
	public void testBlood() {
		System.out.println("level \t defend \t IsCrits"); 
		for ( int level = 1; level < 100; level++ ) {
			for ( int defend = 100; defend< 1500; defend+=100 ) {
				double blood = UserCalculator.calculateBlood(level, defend);
				System.out.println(level + " \t " + defend + " \t " + blood);
			}
		}
	}
	
	/**
	 * Level Exp
			1	60
			2	70
			3	82
			4	96
			5	112
			6	130
			7	150
			8	172
			9	196
			10	222
			11	250
			12	280
			13	312
			14	346
			15	382
			16	420
			17	460
			18	502
			19	546
			20	592
			21	640
			22	690
			23	742
			24	796
			25	852
			26	910
			27	970
			28	1032
			29	1096
			30	1162
			31	1230
	 */
	/*
	public void testUserExp() {
		System.out.println("level \t exp");
		for ( int level=1; level<=100; level++ ) {
			int exp = UserCalculator.calculateLevelExp(level);
			System.out.println(level+"\t"+exp);
		}
	}
	*/
	
	@Test
	public void testCalculateMaxHurtRatio() {
		HashMap<String, Integer>  doubleMap = new HashMap<String, Integer>();
		ArrayList<Integer> thewList = new ArrayList<Integer>();
		for ( int i=0; i<100; i++ ) {
			User user = createRandomUser();
			thewList.add(user.getTkew());
			ArrayList<BuffToolIndex> tools = UserCalculator.calculateMaxHurtRatio(user, user.getTkew());
			String key = null;
			for ( BuffToolIndex tool : tools ) {
				key = tool.name();
				Integer count = doubleMap.get(key);
				if ( count != null ) {
					doubleMap.put(key, count+1);
				} else {
					doubleMap.put(key, 1);
				}
			}
		}
		/*
		for ( int i=0; i<thewList.size(); i++ ) {
			int thew = thewList.get(i);
			System.out.println(thew);
		}
		*/
		System.out.println(doubleMap);
	}
	
	@Test
	public void testCalculateCriticalAttack() {
		int count = 100;
		ArrayList<Double> lucks = new ArrayList<Double>();
		ArrayList<User> users = new ArrayList<User>();
		for ( int i=0; i<count; i++ ) {
			User user = createRandomUser();
			user.setUsername(""+i);
			users.add(user);
			double luck = UserCalculator.calculateCritialAttack(user);
			lucks.add(luck);
		}
		for ( User user : users ) {
			System.out.print(user.getUsername()+"\t\t");
		}
		System.out.println();
		for ( PropDataEquipIndex index : PropDataEquipIndex.values() ) {
			for ( User user : users ) {
				Bag bag = user.getBag();
				List<PropData> equips = (List<PropData>)bag.getWearPropDatas();
				PropData propData = equips.get(index.index());
				if ( propData != null ) {
					System.out.print(propData.getName()+"\t"+propData.getLuckLev()+"\t");
				}
			}
			System.out.println();
		}
		int i=0;
		for ( User user : users ) {
			System.out.print("\t"+lucks.get(i++)+"\t");
		}
	}
	
	@Test
	public void testCalculateCriticalAttackWith200() {
		int count = 100;
		for ( int i=0; i<count; i++ ) {
			User user = new User();
			user.setLevelSimple(40);
			user.setLuck(4000);
			user.setUsername(""+i);
			double luck = UserCalculator.calculateCritialAttack(user);
			System.out.println("luck="+luck);
		}
	}
	
	@Test
	public void testHurt() {
		int count = 1;
		int userLevel = 9;
		int totalExp = 0;
		for ( int i=0; i<userLevel; i++ ) {
			LevelPojo level = LevelManager.getInstance().getLevel(i);
			totalExp += level.getExp();
		}
		ArrayList<User> users = new ArrayList<User>();
		for ( int i=0; i<count; i++ ) {
			User user1 = createRandomUser();
			user1.setUsername(""+i);
			user1.setExp(totalExp);
			
			User user2 = createRandomUser();
			user2.setUsername(""+i);
			user2.setExp(totalExp);
			
		  //计算玩家的敏捷值
			int roundNumber = 0;
			while ( user1.getBlood() > 0 && user2.getBlood() > 0 ) {
				double user1CriticalRatio = UserCalculator.calculateCritialAttack(user1);
				double user2CriticalRatio = UserCalculator.calculateCritialAttack(user2);
				int hurt = UserCalculator.calculateHurt(user1, user2, 
						UserCalculator.calculateMaxHurtRatio(user1), 1.0, 
						user1CriticalRatio);
				user2.setBlood( user2.getBlood() - hurt);
				roundNumber++;
			}
		}
	}
	
	@Test
	public void testHurtWithHighCritical() {
		int count = 1;
		int userLevel = 9;
		int totalExp = 0;
		for ( int i=0; i<userLevel; i++ ) {
			LevelPojo level = LevelManager.getInstance().getLevel(i);
			totalExp += level.getExp();
		}
		ArrayList<User> users = new ArrayList<User>();
		for ( int i=0; i<count; i++ ) {
			User user1 = createRandomUser();
			user1.setUsername(""+i);
			user1.setExp(totalExp);
			user1.setLuck(11000);
			
			User user2 = createRandomUser();
			user2.setUsername(""+i);
			user2.setExp(totalExp);
			
		  //计算玩家的敏捷值
			int roundNumber = 0;
			while ( user1.getBlood() > 0 && user2.getBlood() > 0 ) {
				double user1CriticalRatio = UserCalculator.calculateCritialAttack(user1);
				double user2CriticalRatio = UserCalculator.calculateCritialAttack(user2);
				int hurt = UserCalculator.calculateHurt(user1, user2, UserCalculator.calculateMaxHurtRatio(user1), 1.0, user1CriticalRatio);
				user2.setBlood( user2.getBlood() - hurt);
				System.out.println("user1 crit:"+user1CriticalRatio+", user2 crit:"+user2CriticalRatio+", hurt:"+hurt);
				//最大暴击率设定为3.5
				assertEquals(3.5, user1CriticalRatio, 0.01);
				roundNumber++;
			}
		}
	}
	
	private User createRandomUser() {
		User user = UserManager.getInstance().createDefaultUser();
		PropDataEquipIndex[] equips = PropDataEquipIndex.values();
		for ( int i=0; i<equips.length; i++ ) {
			EquipType type = null;
			switch ( equips[i] ) {
				case BRACELET1:
					type = EquipType.DECORATION;
					break;
				case BRACELET2:
					type = EquipType.DECORATION;
					break;
				case BUBBLE:
					type = EquipType.BUBBLE;
					break;
				case CLOTH:
					type = EquipType.CLOTHES;
					break;
				case EYE:
					type = EquipType.EXPRESSION;
					break;
				case FACE:
					type = EquipType.FACE;
					break;
				case GLASS:
					type = EquipType.GLASSES;
					break;
				case HAIR:
					type = EquipType.HAIR;
					break;
				case HAT:
					type = EquipType.HAT;
					break;
				case NECKLACE:
					type = EquipType.DECORATION;
					break;
				case RING1:
					type = EquipType.JEWELRY;
					break;
				case RING2:
					type = EquipType.JEWELRY;
					break;
				case SUIT:
					type = EquipType.SUIT;
					break;
				case WEAPON:
					type = EquipType.WEAPON;
					break;
				case WEDRING:
					type = EquipType.JEWELRY;
					break;
				case WING:
					type = EquipType.WING;
					break;
			}
			Collection<WeaponPojo> slot = EquipManager.getInstance().getWeaponsBySlot(type);
			if ( slot != null ) {
				Object[] weaponObjs = MathUtil.randomPick(slot, 1);
				PropData propData = null;
				WeaponPojo weapon = (WeaponPojo)weaponObjs[0];
				weapon = EquipManager.getInstance().getWeaponByTypeNameAndUserLevel(weapon.getTypeName(), user.getLevel());
				propData = weapon.toPropData(10, WeaponColor.WHITE);
				user.getBag().addOtherPropDatas(propData);
				user.getBag().wearPropData(propData.getPew(), equips[i].index());
			}
		}
		return user;
	}
}
