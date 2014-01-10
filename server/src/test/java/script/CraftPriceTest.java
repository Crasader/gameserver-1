package script;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.CraftComposeFuncType;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.util.TestUtil;

/**
 * 打印和输出铁匠铺相关的各种价格
 * 
 * @author wangqi
 *
 */
public class CraftPriceTest {
	
	private String userName = "test-001";

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	/**
	 * 打印强化石的价格，强化的价格取决与目标装备的价格，强化等级，
	 * 强化石的价格,强化石的数量，是否使用了神恩符，是否使用了幸运符
	 * 
	 * 装备，强化等级，强化石, 强化石数量，神恩符，幸运符，价格，概率
	 * @throws Exception
	 */
	@Test
	public void testForgeStrength() throws Exception {
		/**
		 * 20021	强化石Lv1
		 * 20022	强化石Lv2
		 * 20023	强化石Lv3
		 * 20024	强化石Lv4
		 * 20025	强化石Lv5
		 * 24001	神恩符
	   * 24002	幸运符+15%
		 * 24004	幸运符+25%
		 * 
		 * 570	黑铁●榴弹炮
		 * 730	黑铁●朗基努斯
		 */
		StringBuilder buf = new StringBuilder(200);
		int[] weaponIds = new int[]{570, 730};
		String[] strengthStones = new String[]{
				"20021", "20022", "20023", "20024", "20025"
		};
		int maxUserLevel = 100;
		PropData godStone = ItemManager.getInstance().getItemById("24001").toPropData();
		PropData luckStone15 = ItemManager.getInstance().getItemById("24002").toPropData();
		PropData luckStone25 = ItemManager.getInstance().getItemById("24004").toPropData();
		User user = prepareUser();
		for ( int w=0; w<weaponIds.length; w++ ) {
			for ( int u=0; u<maxUserLevel; u+=10 ) {
				WeaponPojo weapon = EquipManager.getInstance().getWeaponById(String.valueOf(weaponIds[w]+u/10));
				String weaponName = weapon.getName();
				for ( int strengthLevel=1; strengthLevel<12; strengthLevel++ ) {
					PropData equipPropData = weapon.toPropData(30, WeaponColor.WHITE);
					equipPropData.setLevel(strengthLevel);
					for ( int stoneIndex=0; stoneIndex<strengthStones.length; stoneIndex++ ) {
						ItemPojo stone = ItemManager.getInstance().getItemById(strengthStones[stoneIndex]);
						PropData stonePropData = stone.toPropData();
						for ( int stoneCount=1; stoneCount<=4; stoneCount++ ) {
							buf.append(weaponName).append('\t').append(strengthLevel).append('\t');
							
							buf.append(stone.getName()).append('\t').append(stoneCount).append('\t');
							PropData[] stonePropDatas = new PropData[stoneCount];
							for ( int i=0; i<stonePropDatas.length; i++ ) {
								stonePropDatas[i] = stonePropData.clone();
							}
							Object[] parameters = new Object[]{
									user, new Object[]{equipPropData, stonePropDatas}
							};
							List result = CraftForgePrice.func(parameters).getResult();
							buf.append(result.get(0)).append('\t').append(result.get(1)).append('\t');

						  //使用幸运符15
							stonePropDatas = new PropData[stoneCount+1];
							for ( int i=0; i<stonePropDatas.length; i++ ) {
								stonePropDatas[i] = stonePropData.clone();
							}
							stonePropDatas[stoneCount] = luckStone15;
							parameters = new Object[]{
									user, new Object[]{equipPropData, stonePropDatas}
							};
							result = CraftForgePrice.func(parameters).getResult();
							buf.append(result.get(0)).append('\t').append(result.get(1)).append('\t');
							
						  //使用幸运符25
							stonePropDatas = new PropData[stoneCount+1];
							for ( int i=0; i<stonePropDatas.length; i++ ) {
								stonePropDatas[i] = stonePropData.clone();
							}
							stonePropDatas[stoneCount] = luckStone25;
							parameters = new Object[]{
									user, new Object[]{equipPropData, stonePropDatas}
							};
							result = CraftForgePrice.func(parameters).getResult();
							buf.append(result.get(0)).append('\t').append(result.get(1)).append('\t');
							
						  //使用神恩符价格
							stonePropDatas = new PropData[stoneCount+2];
							for ( int i=0; i<stonePropDatas.length; i++ ) {
								stonePropDatas[i] = stonePropData.clone();
							}
							stonePropDatas[stoneCount] = luckStone25;
							stonePropDatas[stoneCount+1] = godStone;
							parameters = new Object[]{
									user, new Object[]{equipPropData, stonePropDatas}
							};
							result = CraftForgePrice.func(parameters).getResult();
							buf.append(result.get(0)).append('\t').append(result.get(1)).append('\t');
							
							buf.append('\n');
						}
					}
				}
			}
		}
		System.out.println(buf.toString());
	}
	
	/**
	 * 打印合成石的价格和概率
	 * 强化石的价格,强化石的数量，是否使用了神恩符，是否使用了幸运符
	 * 
	 * 装备，合成石，价格, 概率，幸运符15价格，幸运符15概率, 幸运符25价格，幸运符25概率
	 * @throws Exception
	 */
	@Test
	public void testForgeFuncStone() throws Exception {
		/**
			20001	水神石Lv1
			20002	水神石Lv2
			20003	水神石Lv3
			20004	水神石Lv4
			20005	水神石Lv5
			20006	土神石Lv1
			20007	土神石Lv2
			20008	土神石Lv3
			20009	土神石Lv4
			20010	土神石Lv5
			20011	风神石Lv1
			20012	风神石Lv2
			20013	风神石Lv3
			20014	风神石Lv4
			20015	风神石Lv5
			20016	火神石Lv1
			20017	火神石Lv2
			20018	火神石Lv3
			20019	火神石Lv4
			20020	火神石Lv5
			
	   * 24002	幸运符+15%
		 * 24004	幸运符+25%
		 * 
		 * 570	黑铁●榴弹炮
		 * 730	黑铁●朗基努斯
		 */
		StringBuilder buf = new StringBuilder(200);
		int[] weaponIds = new int[]{570, 730};
		int maxUserLevel = 100;
		String[] stoneIds = {
				"20001", "20002", "20003", "20004", "20005",
				"20006", "20007", "20008", "20009", "20010",
				"20011", "20012", "20013", "20014", "20015",
				"20016", "20017", "20018", "20019", "20020",
		};
		PropData luckStone15 = ItemManager.getInstance().getItemById("24002").toPropData();
		PropData luckStone25 = ItemManager.getInstance().getItemById("24004").toPropData();
		User user = prepareUser();
		for ( int w=0; w<weaponIds.length; w++ ) {
			for ( int u=0; u<maxUserLevel; u+=10 ) {
				WeaponPojo weapon = EquipManager.getInstance().getWeaponById(String.valueOf(weaponIds[w]+u/10));
				String weaponName = weapon.getName();
				for ( int stoneIndex=0; stoneIndex<stoneIds.length; stoneIndex++ ) {
					PropData equipPropData = weapon.toPropData(30, WeaponColor.WHITE);
					ItemPojo stone = ItemManager.getInstance().getItemById(stoneIds[stoneIndex]);
					PropData stonePropData = stone.toPropData();
					buf.append(weaponName).append('\t').append('\t');
					//仅使用合成石
					buf.append(stone.getName()).append('\t');
					PropData[] stonePropDatas = new PropData[1];
					stonePropDatas[0] = stonePropData.clone();
					Object[] parameters = new Object[]{
							user, new Object[]{equipPropData.clone(), stonePropDatas}
					};
					List result = CraftForgePrice.func(parameters).getResult();
					buf.append(result.get(0)).append('\t').append(result.get(1)).append('\t');

				  //使用幸运符15
					stonePropDatas = new PropData[2];
					stonePropDatas[0] = stonePropData.clone();
					stonePropDatas[1] = luckStone15;
					parameters = new Object[]{
							user, new Object[]{equipPropData.clone(), stonePropDatas}
					};
					result = CraftForgePrice.func(parameters).getResult();
					buf.append(result.get(0)).append('\t').append(result.get(1)).append('\t');
					
				  //使用幸运符25
					stonePropDatas = new PropData[2];
					stonePropDatas[0] = stonePropData.clone();
					stonePropDatas[1] = luckStone25;
					parameters = new Object[]{
							user, new Object[]{equipPropData.clone(), stonePropDatas}
					};
					result = CraftForgePrice.func(parameters).getResult();
					buf.append(result.get(0)).append('\t').append(result.get(1)).append('\t');
					
					//蓝颜色
					equipPropData = weapon.toPropData(30, WeaponColor.BLUE);
					stonePropDatas = new PropData[1];
					stonePropDatas[0] = stonePropData.clone();
					parameters = new Object[]{
							user, new Object[]{equipPropData.clone(), stonePropDatas}
					};
					result = CraftForgePrice.func(parameters).getResult();
					buf.append(result.get(0)).append('\t').append(result.get(1)).append('\t');
					
					//粉色颜色
					equipPropData = weapon.toPropData(30, WeaponColor.PINK);
					stonePropDatas = new PropData[1];
					stonePropDatas[0] = stonePropData.clone();
					parameters = new Object[]{
							user, new Object[]{equipPropData.clone(), stonePropDatas}
					};
					result = CraftForgePrice.func(parameters).getResult();
					buf.append(result.get(0)).append('\t').append(result.get(1)).append('\t');
					
					//橙颜色
					equipPropData = weapon.toPropData(30, WeaponColor.ORGANCE);
					stonePropDatas = new PropData[1];
					stonePropDatas[0] = stonePropData.clone();
					parameters = new Object[]{
							user, new Object[]{equipPropData.clone(), stonePropDatas}
					};
					result = CraftForgePrice.func(parameters).getResult();
					buf.append(result.get(0)).append('\t').append(result.get(1)).append('\t');
					
					buf.append('\n');
				}
			}
		}
		Thread.sleep(1000);
		System.out.println(buf.toString());
	}

	/**
	 * 
			20001	水神石Lv1
			20002	水神石Lv2
			20003	水神石Lv3
			20004	水神石Lv4
			20005	水神石Lv5
			20006	土神石Lv1
			20007	土神石Lv2
			20008	土神石Lv3
			20009	土神石Lv4
			20010	土神石Lv5
			20011	风神石Lv1
			20012	风神石Lv2
			20013	风神石Lv3
			20014	风神石Lv4
			20015	风神石Lv5
			20016	火神石Lv1
			20017	火神石Lv2
			20018	火神石Lv3
			20019	火神石Lv4
			20020	火神石Lv5
			20021	强化石Lv1
			20022	强化石Lv2
			20023	强化石Lv3
			20024	强化石Lv4
			20025	强化石Lv5
			21001	水神石炼化符
			21002	土神石炼化符
			21003	风神石炼化符
			21004	火神石炼化符
			21005	强化石炼化符
	 * 
	 * 打印合成各等级的石头的价格差距，因为价格与石头的数量和石头的价格相关，
	 * 因此打印各种合成石，各个合成等级，1-4块石头的金币价格
	 * 
	 * 强化石lv1 1块 15%概率 200金币， 2块， 30%概率 400金币
	 * 
	 */
	@Test
	public void testComposeStonePrice() {
		String[] stoneIds = {
				"20001", "20002", "20003", "20004", //"20005",
				"20006", "20007", "20008", "20009", //"20010",
				"20011", "20012", "20013", "20014", //"20015",
				"20016", "20017", "20018", "20019", //"20020",
				"20021", "20022", "20023", "20024", //"20025",
		};
		StringBuilder buf = new StringBuilder(200);
		for ( int i=0; i<stoneIds.length; i++ ) {
			ItemPojo itemPojo = ItemManager.getInstance().getItemById(stoneIds[i]);
			buf.append(itemPojo.getName()+"\t");
			User user = prepareUser();
			HashSet itemIds = new HashSet();
			itemIds.add(itemPojo.getId());
			ArrayList itemList = new ArrayList();
			for ( int j=0; j<4; j++ ) {
				itemList.add(itemPojo.toPropData());
				ArrayList result = CraftComposePrice.composeStone(user, itemIds, itemList);
				buf.append(result.get(0)).append("\t").append(result.get(1)).append("\t");
			}
			buf.append("\n");
		}
		System.out.println(buf.toString());
	}
	
	/**
	 * 测试武器熔炼符的价格，熔炼价格与目标武器，颜色符，武器数量相关，打印结果
	 * 
	 * 黑铁.榴弹炮 绿色熔炼 1把价格 1把概率 2把价格 2把概率...
	 */
	@Test
	public void testComposeColorEquip() {
		StringBuilder buf = new StringBuilder(200);
		Collection<WeaponPojo> weapons = EquipManager.getInstance().getWeaponsBySlot(EquipType.WEAPON);
		Collection<WeaponPojo> suits = EquipManager.getInstance().getWeaponsBySlot(EquipType.SUIT);
		ArrayList<WeaponPojo> all = new ArrayList<WeaponPojo>(500);
		all.addAll(weapons);
		all.addAll(suits);
		User user = prepareUser();
		for ( WeaponPojo weapon : all ) {
			buf.append(weapon.getName()).append("\t");
			for ( WeaponColor color : WeaponColor.values() ) {
				CraftComposeFuncType funcType = null;
				switch ( color ) {
					case WHITE:
						funcType = CraftComposeFuncType.COLOR_GREEN;
						break;
					case GREEN:
						funcType = CraftComposeFuncType.COLOR_BLUE;
						break;
					case BLUE:
						funcType = CraftComposeFuncType.COLOR_PINK;
						break;
					case PINK:
						funcType = CraftComposeFuncType.COLOR_ORANGE;
						break;
					default:
						break;
				}
				buf.append(color).append("\t");
				for ( int c=0; c<4; c++ ) {
					PropData propData = weapon.toPropData(30, color);
					HashSet<String> itemIds = new HashSet<String>();
					itemIds.add(weapon.getId());
					ArrayList<PropData> itemList = new ArrayList<PropData>();
					
					for ( int j=0; j<=c; j++ ) {
						itemList.add(propData.clone());
					}
					ArrayList result = CraftComposePrice.composeColorEquip(user, itemIds, itemList, funcType);
					buf.append(result.get(0)).append("\t").append(result.get(1)).append("\t");
				}
			}
			buf.append("\n");
		}
		System.out.println(buf.toString());
	}
	
	/**
	 * 合成武器或者装备的价格，与合成符类型和目标等级相关
	 * 26011	精良装备熔炼符
	 * 26010	精良武器熔炼符
	 * 26009	装备熔炼符
	 * 26008	武器熔炼符
	 * 
	 * 	case 9 ://武器熔炼
			case 10://装备熔炼
			case 11://精良武器熔炼
			case 12://精良装备熔炼
	 */
	@Test
	public void testComposeEquipOrWeapon() {
		StringBuilder buf = new StringBuilder(200);
		User user = prepareUser();
		String[] stoneIds = new String[]{"26011", "26010", "26009", "26008"};
		CraftComposeFuncType[] funcTypes = new CraftComposeFuncType[]{
				CraftComposeFuncType.MAKE_EQUIP2, CraftComposeFuncType.MAKE_WEAPON2, 
				CraftComposeFuncType.MAKE_EQUIP, CraftComposeFuncType.MAKE_WEAPON};
		for ( int level = 0; level<=100; level+=10 ) {
			user.setLevelSimple(level);
			for ( int i=0; i<stoneIds.length; i++ ) {
				String stoneId = stoneIds[i];
				ItemPojo itemPojo = ItemManager.getInstance().getItemById(stoneId);
				PropData stone = itemPojo.toPropData();
				HashSet<String> itemIds = new HashSet<String>();
				itemIds.add(stoneId);
				ArrayList<PropData> itemList = new ArrayList<PropData>();
				WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
				for ( int j=0; j<4; j++) {
					itemList.add(weapon.toPropData(30, WeaponColor.WHITE));
				}
				ArrayList result = CraftComposePrice.composeEquipOrWeapon(user, itemIds, itemList, funcTypes[i]);
				buf.append(result.get(0)).append("\t").append(result.get(1)).append("\t");
			}
			buf.append('\n');
		}
		System.out.println(buf.toString());
	}
	
	/**
	 * 转移的价格与武器的定价、武器的颜色差异、武器的强化等级差异相关
	 * 
	 * 普通武器之间的转换
	 * 610	黑铁●急速锯 -> 570	黑铁●榴弹炮
	 * 
	 * 普通武器与精良武器的转换
	 * 570	黑铁●榴弹炮 -> 730	黑铁●朗基努斯
	 * 
	 * 精良武器之间的转换
	 * 730	黑铁●朗基努斯 -> 680	黑铁●青龙鳞
	 * 
	 * 源武器 目标武器 源武器等级 源武器颜色 源武器强化等级 目标武器等级 目标武器颜色 目标武器强化等级 价格 概率
	 */
	@Test
	public void testTransferPrice() throws Exception {
		User user = prepareUser();
		int normalWeaponId1 = 610;
		int normalWeaponId2 = 570;
		int proWeaponId1 = 730;
		int proWeaponId2 = 680;
		//普通武器之间的转换
		PrintWriter buf = new PrintWriter("trasferprice.txt");
		transferPrice(user, normalWeaponId1, normalWeaponId2, buf);
		transferPrice(user, normalWeaponId2, proWeaponId1, buf);
		transferPrice(user, proWeaponId1, proWeaponId2, buf);
		//System.out.println(buf.toString());
	}

	/**
	 * @param user
	 * @param srcId
	 * @param tarId
	 * @param buf
	 */
	public void transferPrice(User user, int srcId, int tarId, PrintWriter buf) {
		int[] levels = new int[]{0, 5, 8, 10, 12};
		//int[] levels = new int[]{0, 5, 8};
		int maxUserLevel = 100;
		//int maxUserLevel = 50;
		for ( int srcUserLevel=0; srcUserLevel<maxUserLevel; srcUserLevel+=10 ) {
			for ( int srcColor=0; srcColor<WeaponColor.values().length; srcColor++ ) {
				for ( int srcLevel=0; srcLevel<levels.length; srcLevel++ ) {
					for ( int tarUserLevel=srcUserLevel; tarUserLevel<maxUserLevel; tarUserLevel+=10 ) {
						for ( int tarColor=srcColor; tarColor<WeaponColor.values().length; tarColor++ ) {
							for ( int tarLevel=srcLevel+1; tarLevel<levels.length; tarLevel++ ) {
								WeaponPojo srcWeapon = EquipManager.getInstance().getWeaponById(String.valueOf(srcId+srcUserLevel/10));
								WeaponPojo tarWeapon = EquipManager.getInstance().getWeaponById(String.valueOf(tarId+tarUserLevel/10));
								PropData srcPropData = srcWeapon.toPropData(30, WeaponColor.values()[srcColor]);
								PropData tarPropData = tarWeapon.toPropData(30, WeaponColor.values()[tarColor]);
								srcPropData.setLevel(levels[srcLevel]);
								tarPropData.setLevel(levels[tarLevel]);
								buf.append(srcWeapon.getName()).append("\t").
									append(srcPropData.getWeaponColor().toString()).append("\t").append(levels[srcLevel]+"").append("\t");
								buf.append(tarWeapon.getName()).append("\t").
									append(tarPropData.getWeaponColor().toString()).append("\t").append(levels[tarLevel]+"").append("\t");
								int price = CraftTransferPrice.transferPrice(user, srcPropData, tarPropData);
								buf.append(price+"");
								buf.append("\n");
								buf.flush();
							}
						}
					}
				}
			}
		}
	}

	public User prepareUser() {
		UserId userId = new UserId(userName);
		ArrayList list = new ArrayList();
		IoSession session = TestUtil.createIoSession(list);
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setGolden(Integer.MAX_VALUE);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		UserManager.getInstance().removeUser(userName);
		
		user.set_id(userId);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		
		UserManager.getInstance().saveUser(user, true);
		UserManager.getInstance().saveUserBag(user, true);

		return user;
	}
}
