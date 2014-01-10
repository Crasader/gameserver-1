package com.xinqihd.sns.gameserver.guild;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.MoneyType;
import com.xinqihd.sns.gameserver.config.ShopCatalog;
import com.xinqihd.sns.gameserver.config.ShopPojo;
import com.xinqihd.sns.gameserver.config.ShopPojo.BuyPrice;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.ShopManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.util.MathUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class GuildManagerTest {
	
	String guildName = "TestGuild";
	String announcement = "测试公会";
	GuildManager manager = GuildManager.getInstance();

	@Before
	public void setUp() throws Exception {
		manager.removeGuild(guildName);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreateGuildNoLevel() {
		GuildManager manager = GuildManager.getInstance();
		User user = prepareUser(10, 10);
		Guild guild = manager.createGuild(user, guildName, announcement);
		assertNull(guild);
	}
	
	@Test
	public void testCreateGuildNoGolden() {
		GuildManager manager = GuildManager.getInstance();
		User user = prepareUser(20, 10);
		Guild guild = manager.createGuild(user, guildName, announcement);
		assertNull(guild);
	}
	
	@Test
	public void testCreateGuildNoName() {
		String guildName = "";
		GuildManager manager = GuildManager.getInstance();
		User user = prepareUser(20, 500000);
		Guild guild = manager.createGuild(user, guildName, announcement);
		assertNull(guild);
	}
	
	@Test
	public void testCreateGuildNameSize() {
		String guildName = "测试公会测试";
		GuildManager manager = GuildManager.getInstance();
		User user = prepareUser(20, 500000);
		Guild guild = manager.createGuild(user, guildName, announcement);
		assertNull(guild);
	}
	
	@Test
	public void testCreateGuild() {
		GuildManager manager = GuildManager.getInstance();
		manager.removeGuild(guildName);
		
		User user = prepareUser(20, 500000);
		Guild guild = manager.createGuild(user, guildName, announcement);
		assertNotNull(guild);
		
		Guild actualGuild = manager.queryGuildById(guildName);
		assertNotNull(actualGuild);
	}
	
	@Test
	public void testCreateGuildDuplicate() {
		GuildManager manager = GuildManager.getInstance();
		manager.removeGuild(guildName);
		
		User user = prepareUser(20, 500000);
		Guild guild = manager.createGuild(user, guildName, announcement);
		assertNotNull(guild);
		
		guild = manager.createGuild(user, guildName, announcement);
		assertNull(guild);
	}
	
	@Test
	public void testCreateGuildMoreThanOne() {
		GuildManager manager = GuildManager.getInstance();
		manager.removeGuild(guildName);
		
		User user = prepareUser(20, 500000);
		Guild guild = manager.createGuild(user, guildName, announcement);
		assertNotNull(guild);
		
		guild = manager.createGuild(user, guildName+"new", announcement);
		assertNull(guild);
	}
	
	@Test
	public void testCreateGuildAnnouncement() {
		String announcement = "近期搜狗“绯闻”缠身，这源于其微妙的江湖地位：中国搜索市场的格局里，搜狗夹在百度、360和腾讯之间，体量最小，但却是一枚最关键的棋子。 “2013年中国搜索市场的棋局如何变化，取决于搜狗。”业内逐渐形成共识，即这家以输入法、浏览器、搜索“三级火箭”立足的公司无论倒向哪一方，都将改变几方力量对比的版图。";
		GuildManager manager = GuildManager.getInstance();
		manager.removeGuild(guildName);
		
		User user = prepareUser(20, 500000);
		Guild guild = manager.createGuild(user, guildName, announcement);
		assertNotNull(guild);
		
		Guild actualGuild = manager.queryGuildById(guildName);
		assertTrue("size:"+actualGuild.getAnnounce().length(), actualGuild.getAnnounce().length()<=128);
	}
	
	@Test
	public void testListGuildAll() {
		GuildManager manager = GuildManager.getInstance();
		User user = prepareUser(20, 500000);
		
		String guildName = "公会";
		ArrayList<String> names = new ArrayList<String>();
		int count = 1;
		for ( int i=0; i<count; i++ ) {
			names.add(guildName+i);
		}
		for ( int i=0; i<count; i++ ) {
			String name = names.get(i);
			manager.removeGuild(name);
			Guild guild = manager.createGuild(user, name, announcement);
			guild.setWealth(MathUtil.nextGaussionInt(1000, 100000, 10));
			guild.setLevel((int)(MathUtil.nextDouble() * 10));
			manager.saveGuild(guild);
		}

		Collection<Guild> guilds = manager.listGuilds(user, 0, -1);
		int lastLevel = 0;
		int lastWealth = 0;
		for ( Guild guild : guilds ) {
			int level = guild.getLevel();
			//assertTrue("lastLevel:"+lastLevel, lastLevel >= level);
			int wealth = guild.getWealth();
			//assertTrue("lastWealth:"+lastWealth, lastWealth >= wealth);
			System.out.println(guild);
		}
		
		for ( int i=0; i<count; i++ ) {
			String name = names.get(i);
			manager.removeGuild(name);
		}
		System.out.println("");
	}
	
	@Test
	public void testUpdateGuildRank() {
		GuildManager manager = GuildManager.getInstance();
		manager.removeGuild(guildName);
		
		User user = prepareUser(20, 500000);
		Guild guild = manager.createGuild(user, guildName, announcement);
		int newRank = 10;
		manager.updateGuildRank(guild, newRank);
		assertEquals(newRank, guild.getRank());
		
		Guild actualGuild = manager.queryGuildById(guildName);
		assertEquals(newRank, actualGuild.getRank());
	}
	
	@Test
	public void testSearchGuildAll() {
		GuildManager manager = GuildManager.getInstance();
		User user = prepareUser(20, 500000);
		
		String guildName = "1公会";
		ArrayList<String> names = new ArrayList<String>();
		int count = 1;
		for ( int i=0; i<count; i++ ) {
			names.add(guildName+i);
		}
		for ( int i=0; i<count; i++ ) {
			String name = names.get(i);
			manager.removeGuild(name);
			Guild guild = manager.createGuild(user, name, announcement);
			guild.setWealth(MathUtil.nextGaussionInt(1000, 100000, 10));
			guild.setLevel((int)(MathUtil.nextDouble() * 10));
			manager.saveGuild(guild);
		}

		Collection<Guild> guilds = manager.searchGuild("1公会", 0, -1);
		
		assertEquals(count, guilds.size());
		
		for ( int i=0; i<count; i++ ) {
			String name = names.get(i);
			manager.removeGuild(name);
		}
	}
	
	@Test
	public void testSearchGuildRange() {
		GuildManager manager = GuildManager.getInstance();
		User user = prepareUser(20, 500000);
		manager.removeGuildMember(null, user.get_id());
		
		String guildName = "1公会";
		ArrayList<String> names = new ArrayList<String>();
		int count = 1;
		for ( int i=0; i<count; i++ ) {
			names.add(guildName+i);
		}
		for ( int i=0; i<count; i++ ) {
			String name = names.get(i);
			manager.removeGuild(name);
			Guild guild = manager.createGuild(user, name, announcement);
			guild.setWealth(MathUtil.nextGaussionInt(1000, 100000, 10));
			guild.setLevel((int)(MathUtil.nextDouble() * 10));
			manager.saveGuild(guild);
		}

		Collection<Guild> guilds = manager.searchGuild("1公会", 0, 10);

		assertEquals(1, guilds.size());
		
		for ( int i=0; i<count; i++ ) {
			String name = names.get(i);
			manager.removeGuild(name);
		}
	}
	
	@Test
	public void testSearchGuild() {
		GuildManager manager = GuildManager.getInstance();
		User user = prepareUser(20, 500000);
		
		String guildName = "1公会";
		ArrayList<String> names = new ArrayList<String>();
		int count = 1;
		for ( int i=0; i<count; i++ ) {
			names.add(guildName+i);
		}
		for ( int i=0; i<count; i++ ) {
			String name = names.get(i);
			manager.removeGuild(name);
			Guild guild = manager.createGuild(user, name, announcement);
			guild.setWealth(MathUtil.nextGaussionInt(1000, 100000, 10));
			guild.setLevel((int)(MathUtil.nextDouble() * 10));
			manager.saveGuild(guild);
		}

		Collection<Guild> guilds = manager.searchGuild("1公", 0, -1);

		assertEquals(1, guilds.size());
		
		for ( int i=0; i<count; i++ ) {
			String name = names.get(i);
			manager.removeGuild(name);
		}
	}
	
	@Test
	public void testListGuildPartial() {
		User user = prepareUser(20, 500000);
		
		String guildName = "公会";
		ArrayList<String> names = new ArrayList<String>();
		int count = 1;
		for ( int i=0; i<count; i++ ) {
			names.add(guildName+i);
		}
		for ( int i=0; i<count; i++ ) {
			String name = names.get(i);
			manager.removeGuild(name);
			Guild guild = manager.createGuild(user, name, announcement);
			guild.setWealth(i*1000);
			guild.setLevel(5);
			manager.saveGuild(guild);
		}

		Collection<Guild> guilds = manager.listGuilds(user, 10, 10);
		assertEquals(1, guilds.size());
		for ( Guild guild : guilds ) {
			int level = guild.getLevel();
			//assertTrue("lastLevel:"+lastLevel, lastLevel >= level);
			int wealth = guild.getWealth();
			//assertTrue("lastWealth:"+lastWealth, lastWealth >= wealth);
			System.out.println(guild);
		}
		
		for ( int i=0; i<count; i++ ) {
			String name = names.get(i);
			manager.removeGuild(name);
		}
	}
	
	@Test
	public void testSaveGuildMember() {
		User user = prepareUser(20, 5000000);
		manager.removeGuild(guildName);
		manager.removeGuildMember(null, user.get_id());
		
		Guild guild = manager.createGuild(user, guildName, announcement);
		manager.saveGuild(guild);
		GuildMember member = new GuildMember();
		String id = StringUtil.concat(guild.get_id(), ":", user.get_id().toString());
		member.set_id(id);
		member.setGuildId(guild.get_id());
		member.setCredit(10000);
		member.setOnline(true);
		member.setRole(GuildRole.director);
		member.setUserId(user.get_id());
		GuildManager.getInstance().saveGuildMember(member);
		
		GuildMember actual = manager.queryGuildMemberByUserId(user.get_id());
		assertEquals(id, actual.get_id());
		assertEquals(member.getGuildId(), actual.getGuildId());
		assertEquals(member.getCredit(), actual.getCredit());
		assertEquals(member.getRole(), actual.getRole());
		assertEquals(member.getUserId(), actual.getUserId());
		assertEquals(member.isOnline(), actual.isOnline());
		
		boolean success = manager.removeGuildMember(guild, user.get_id(), guildName);
		assertTrue(success);
		actual = manager.queryGuildMemberByUserId(user.get_id());
		assertNull(actual);
	}
	
	@Test
	public void testListApply() {
		User user = prepareUser(20, 5000000);
		manager.removeGuild(guildName);
		
		manager.createGuild(user, guildName, announcement);
		
		User newUser = prepareUser2(20, 500000);
		boolean success = manager.applyGuild(newUser, guildName);
		assertTrue(success);
		
		Collection<Apply> applys = manager.listGuildApplys(newUser, guildName, 0, 200);
		assertEquals(1, applys.size());
	}
	
	@Test
	public void testApply() {
		User user = prepareUser(20, 5000000);
		manager.removeGuild(guildName);
		
		manager.createGuild(user, guildName, announcement);
		
		User newUser = prepareUser2(20, 500000);
		boolean success = manager.applyGuild(newUser, guildName);
		assertTrue(success);
		
		Collection<Apply> applys = manager.listGuildApplys(newUser, guildName, 0, -1);
		assertEquals(1, applys.size());
		Apply apply = applys.iterator().next();

		//Apply same guild again
		success = manager.applyGuild(newUser, guildName);
		assertFalse(success);

		//user is the owner.
		manager.processGuildApply(user, apply, false);
		
		//Apply same guild again
		success = manager.applyGuild(newUser, guildName);
		assertTrue(success);
		
		Map<String, String> map = manager.listUserGuildApply(newUser);
		assertEquals(1, map.size());
	}
	
	@Test
	public void testApplyMoreThan5() {
		User user = prepareUser(20, 5000000);
		
		ArrayList<String> guildNames = new ArrayList<String>();
	
		for ( int i=0; i<6; i++ ) {
			String name = guildName+i;
			guildNames.add(name);
			manager.removeGuild(name);
			manager.createGuild(user, name, announcement);
		}
		
		//should succeed
		User newUser = prepareUser2(20, 500000);
		for ( int i=0; i<5; i++ ) {
			String name = guildNames.get(i);
			boolean success = manager.applyGuild(newUser, name);
			assertTrue("i="+i, success);
		}
		Collection<Apply> applys = manager.queryGuildApply(newUser.get_id());
		assertEquals(5, applys.size());

		//should fail
		boolean success = manager.applyGuild(newUser, guildName+"5");
		assertFalse(success);

		//Approve or deny an apply
		Apply apply = (Apply)applys.iterator().next();
		Guild guild = manager.queryGuildById(apply.getGuildId());
		user.setGuild(guild);
		success = manager.processGuildApply(user, apply, true);
		assertTrue(success);
		/**
		 * processGuildApply将根据用户是否在线的状态修改User.setGuild属性
		 * 所以这里要手工设置一下
		 */
		newUser.setGuild(guild);
		//should fail because the user has joined a guild
		success = manager.applyGuild(newUser, guildName+"5");
		assertFalse(success);
		
		newUser.setGuild(null);
		success = manager.applyGuild(newUser, guildName+"5");
		assertTrue(success);
		
		//clean
		for ( String name : guildNames ) {
			manager.removeGuild(name);
		}
		System.out.println();
	}
	
	@Test
	public void testCheckGuildPayFee3Day() {
		GuildManager manager = GuildManager.getInstance();
		manager.removeGuild(guildName);
		
		User user = prepareUser(20, 500000);
		Guild guild = manager.createGuild(user, guildName, announcement);
		assertNotNull(guild);
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -3);
		guild.setLastchargetime(cal.getTimeInMillis());
		
		boolean success = GuildManager.getInstance().checkOperationFee(user, guild);
		assertTrue(success);
	}
	
	@Test
	public void testCheckGuildPayFee5Day() {
		GuildManager manager = GuildManager.getInstance();
		manager.removeGuild(guildName);
		
		User user = prepareUser(20, 500000);
		Guild guild = manager.createGuild(user, guildName, announcement);
		assertNotNull(guild);
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -5);
		guild.setLastchargetime(cal.getTimeInMillis());
		
		boolean success = GuildManager.getInstance().checkOperationFee(user, guild);
		assertTrue(success);
	}
	
	@Test
	public void testCheckGuildPayFee7Day() {
		GuildManager manager = GuildManager.getInstance();
		manager.removeGuild(guildName);
		
		User user = prepareUser(20, 500000);
		Guild guild = manager.createGuild(user, guildName, announcement);
		assertNotNull(guild);
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -7);
		guild.setLastchargetime(cal.getTimeInMillis());
		
		/**
		 * 公会可以运行7天
		 */
		boolean success = GuildManager.getInstance().checkOperationFee(user, guild);
		assertTrue(success);
		/**
		 * 第8天需要交费
		 */
		cal.add(Calendar.DAY_OF_MONTH, -8);
		guild.setLastchargetime(cal.getTimeInMillis());
		success = GuildManager.getInstance().checkOperationFee(user, guild);
		assertFalse(success);
	}
	
	@Test
	public void testGetGuildShopNotMeetLevel() {
		GuildManager manager = GuildManager.getInstance();
		manager.removeGuild(guildName);
		
		User user = prepareUser(20, 500000);
		Guild guild = manager.createGuild(user, guildName, announcement);
		assertNotNull(guild);
		
		Collection<ShopPojo> shops = manager.getGuildShop(user, user.getGuild(), 1);
		assertNull(shops);
	}
	
	@Test
	public void testGetGuildShopNotMeetCredit() {
		GuildManager manager = GuildManager.getInstance();
		manager.removeGuild(guildName);
		
		User user = prepareUser(20, 500000);
		Guild guild = manager.createGuild(user, guildName, announcement);
		GuildFacility shop = guild.getFacility(GuildFacilityType.shop);
		shop.setLevel(1);
		
		Collection<ShopPojo> shops = manager.getGuildShop(user, user.getGuild(), 1);
		assertNull(shops);
	}

	@Test
	public void testGetGuildShop() {
		GuildManager manager = GuildManager.getInstance();
		manager.removeGuild(guildName);
		
		User user = prepareUser(20, 500000);
		Guild guild = manager.createGuild(user, guildName, announcement);
		GuildFacility shop = guild.getFacility(GuildFacilityType.shop);
		shop.setLevel(1);
		user.getGuildMember().setCredit(10000);
		
		Collection<ShopPojo> shops = manager.getGuildShop(user, user.getGuild(), 1);
		assertNotNull(shops);
	}

	@Test
	public void testGuildBag() {
		GuildManager manager = GuildManager.getInstance();
		manager.removeGuild(guildName);
		
		User user = prepareUser(20, 500000);
		Guild guild = manager.createGuild(user, guildName, announcement);

		GuildBag guildBag = manager.queryGuildBag(guildName); 
		int count = 1;
		for ( int i=0; i<count; i++) {
			PropData propData = makePropData();
			guildBag.addPropData(propData);
		}
		manager.saveGuildBag(user, guildBag);
		
		GuildBag actualBag = manager.queryGuildBag(guild.get_id());
		assertNotNull(actualBag);
		assertEquals(count, actualBag.getPropList().size());
	}
	
	@Test
	public void testGuildBagVersionChange() {
		GuildManager manager = GuildManager.getInstance();
		manager.removeGuild(guildName);
		
		User user = prepareUser(20, 500000);
		Guild guild = manager.createGuild(user, guildName, announcement);

		GuildBag guildBag = new GuildBag();
		guildBag.set_id(guild.get_id());
		//Save the first guildBag
		manager.saveGuildBag(user, guildBag);
		//Check save operation and keep an old guild bag copy
		GuildBag actualBag = manager.queryGuildBag(guild.get_id());
		assertNotNull(actualBag);

		//Change the first guildbag copy
		int count = 5;
		for ( int i=0; i<count; i++) {
			PropData propData = makePropData();
			guildBag.addPropData(propData);
		}
		boolean success = manager.saveGuildBag(user, guildBag);
		assertTrue(success);
			
		//Changed by another user
		PropData propData = makePropData();
		actualBag.addPropData(propData);
		
		//Try to save the obsolete version
		success = manager.saveGuildBag(user, actualBag);
		assertFalse(success);
	}
	
	@Test
	public void testGuildBagAddPropData() {
		GuildManager manager = GuildManager.getInstance();
		manager.removeGuild(guildName);
		
		User user = prepareUser(20, 500000);
		Guild guild = manager.createGuild(user, guildName, announcement);

		GuildBag guildBag = manager.queryGuildBag(guildName);
		guildBag.set_id(guild.get_id());
		//Save the first guildBag
		manager.saveGuildBag(user, guildBag);

		//Change the first guildbag copy
		int count = 5;
		for ( int i=0; i<count; i++) {
			PropData propData = makePropData();
			manager.addGuildBagPropData(user, guildBag, propData);
		}
		assertEquals(count, guildBag.getCount());
		assertEquals(count, guildBag.getPropList().size());
		
		//Check save operation and keep an old guild bag copy
		GuildBag actualBag = manager.queryGuildBag(guild.get_id());
		assertNotNull(actualBag);
		assertEquals(count, actualBag.getCount());
		assertEquals(count, actualBag.getPropList().size());
	}

	@Test
	public void testAddGuildBagEvent() {
		User user = prepareUser(20, 5000000);
		manager.removeGuild(guildName);
		manager.createGuild(user, guildName, announcement);
		
		int count = 200;
		for ( int i=0; i<count; i++ ) {
			GuildBagEvent event = new GuildBagEvent();
			event.setEvent(""+i);
			event.setGuildId(guildName);
			event.setRoleName(user.getRoleName());
			event.setTimestamp(System.currentTimeMillis());
			event.setType(GuildBagEventType.TAKE);
			event.setUserId(user.get_id());
			manager.addGuildBagEvent(event);
		}
		
		int actualCount = manager.queryGuildBagEventCount(guildName);
		assertEquals(100, actualCount);
	}
	
	@Test
	public void testQueryBagEvent() {
		Collection<GuildBagEvent> events = manager.queryGuildBagEvents("aaa");
		for(GuildBagEvent event : events ) {
			System.out.println(event);
		}
	}
	
	@Test
	public void testDate() {
		System.out.println(WeaponColor.PURPLE.toIntColor());
	}
	
	@Test
	public void testSearchGuildMember() {
		GuildManager manager = GuildManager.getInstance();
		manager.removeGuild(guildName);
		
		User user = prepareUser(20, 500000);
		Guild guild = manager.createGuild(user, guildName, announcement);
		assertNotNull(guild);
		
		Collection<GuildMember> members = manager.searchGuildMember(guildName, "test");
		assertTrue(members.size()>0);
	}

	/**
	 * @return
	 */
	private PropData makePropData() {
		WeaponPojo weapon = EquipManager.getInstance().getRandomWeapon(10, EquipType.WEAPON, 1);
		PropData propData = weapon.toPropData(30, WeaponColor.WHITE);
		return propData;
	}
		
	public void addGuildShopPojo() {
		//一级商城
		String[][] items = new String[][]{
				{"20001", "20006", "20011", "20016", "26001"},
				{"20002", "20007", "20012", "20017", "20022", "26004"},
				{"20003", "20008", "20013", "20018", "20023", "26005", "24002"},
				{"20004", "20009", "20014", "20019", "20024", "26006", "29021", "26011"},
				{"20005", "20010", "20015", "20020", "20025", "26007", "24004", "26010", "99999"},
		};
		int[][] prices = new int[][]{
				{20, 20, 20, 20, 30, 10},
				{65, 65, 65, 65, 75, 70},
				{185, 185, 185, 185, 250, 150, 250},
				{550, 550, 550, 550, 650, 1100, 1200, 1500},
				{1650, 1650, 1650, 1650, 2000, 3500, 1500, 2100, 2300},
		};
		
		int id = 20000;
		for ( int i=0; i<items.length; i++ ) {
			for ( int j=0; j<items[i].length; j++ ) {
				int index = ShopCatalog.GUILD_LV1.ordinal()+i;
				ItemPojo item = ItemManager.getInstance().getItemById(items[i][j]);
				PropData propData = item.toPropData();
				ShopPojo shop = new ShopPojo();
				shop.setId(String.valueOf(id+i*items.length+j));
				shop.setPropInfoId(item.getId());
				shop.setInfo(item.getName());
				shop.setItem(true);
				shop.setLevel(-1);
				shop.setLimitCount(0);
				shop.setLimitGroup(0);
				shop.setSell(1);
				shop.setMoneyType(MoneyType.MEDAL);
				shop.setDiscount(100);
				shop.setBanded(0);
				shop.setType(0);	
				shop.addCatalog(ShopCatalog.values()[index]);
				List<BuyPrice> buyPrices = new ArrayList<BuyPrice>();
				BuyPrice price = new BuyPrice();
				price.price = prices[i][j];
				price.validTimes = Integer.MAX_VALUE;
				buyPrices.add(price);
				shop.setBuyPrices(buyPrices);
				
				ShopManager.getInstance().addShopPojo(shop);
			}
		}
	}
	
	private User prepareUser(int level, int golden) {
		String userName = "test-001";
		User user = new User();
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setLevel(level);
		user.setGoldenSimple(golden);
		return user;
	}
	
	private User prepareUser2(int level, int golden) {
		String userName = "test-002";
		User user = new User();
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setLevel(level);
		user.setGoldenSimple(golden);
		return user;
	}

}
