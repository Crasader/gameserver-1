package com.xinqihd.sns.gameserver.util;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.MapPojo;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.config.TipPojo;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.MapDBObject;
import com.xinqihd.sns.gameserver.db.mongo.MapManager;
import com.xinqihd.sns.gameserver.db.mongo.MongoDBUtil;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.db.mongo.TipManager;

public class TextTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testColor() throws Exception {
		for ( WeaponColor color : WeaponColor.values() ) {
			System.out.println(color+":"+color.toIntColor());
		}
	}

	@Test
	public void testI18nPermformance() throws Exception {
		final int loop = 10000;
		Runnable task1 = new Runnable() {
			public void run() {
				MessageFormat format = new MessageFormat("[公告]：玩家{0}人品大爆发，从阿波罗神罐中开出了狂.急速锯");
				String r = format.format(new String[]{"小明"});
//				System.out.println(r);
			}
		};
		TestUtil.doPerform(task1, "Java MessegeFormat", loop);
		
		Runnable task2 = new Runnable() {
			public void run() {
				String r = String.format("[公告]：玩家%s人品大爆发，从阿波罗神罐中开出了狂.急速锯", "小明");
//				System.out.println(r);
			}
		};
		TestUtil.doPerform(task2, "String.format", loop);
		
		Runnable task3 = new Runnable() {
			public void run() {
				String r = 
						MessageFormatter.arrayFormat("[公告]：玩家{}人品大爆发，从阿波罗神罐中开出了狂.急速锯", 
								"小明").getMessage();
//				System.out.println(r);
			}
		};
		TestUtil.doPerform(task3, "My MessegeFormat", loop);
	}
	
	@Test
	public void testi18N_1() throws Exception {
		String pattern = "notice.strength";
		String[] args = {"上帝", "泡泡手雷", "4"};
		String result = Text.text(pattern, args);
		assertEquals("#e53333玩家'#00d5ff上帝'#e53333成功将#009900泡泡手雷#e53333强化到#337fe54#e53333级，战斗力又上了一个新台阶。", result);
	}
	
	@Test
	public void testi18N_2() throws Exception {
		String pattern = "notice.strength";
		String result = Text.text(pattern);
		assertEquals("#e53333玩家'#00d5ff{}'#e53333成功将#009900{}#e53333强化到#337fe5{}#e53333级，战斗力又上了一个新台阶。", result);
	}
	
	@Test
	public void testi18N_3() throws Exception {
		String pattern = "notice.strength";
		String[] args = {"上帝", "泡泡手雷", "4", "5", "6", "7"};
		String result = Text.text(pattern, args);
		assertEquals("#e53333玩家'#00d5ff上帝'#e53333成功将#009900泡泡手雷#e53333强化到#337fe54#e53333级，战斗力又上了一个新台阶。", result);
	}
	
	@Test
	public void testi18N_Taiwan_ThreadLocal() throws Exception {
		final String pattern = "notice.strength";
		final String[] args = {"上帝", "泡泡手雷", "4"};
		final Semaphore sema = new Semaphore(0);
		
		Runnable r1 = new Runnable() {
			public void run() {
				Locale userLocale = Locale.TRADITIONAL_CHINESE;
				LocaleThreadLocal local = GameContext.getInstance().getLocaleThreadLocal();
				local.set(userLocale);
				//Wait for the other thread
				try {
					System.out.println("Wait for thread 2");
					sema.acquire();
				} catch (InterruptedException e) {
				}
				
				String result = Text.text(pattern, args);
				System.out.println("繁体: " + result);
				assertEquals("#e53333玩家'#00d5ff上帝'#e53333成功將#009900泡泡手雷#e53333強化到#337fe54#e53333級，戰鬥力又上了一個新臺階。", result);
			}
		};
		
		Runnable r2 = new Runnable() {
			public void run() {
				Locale userLocale = Locale.SIMPLIFIED_CHINESE;
				LocaleThreadLocal local = GameContext.getInstance().getLocaleThreadLocal();
				local.set(userLocale);
				
				String result = Text.text(pattern, args);
				System.out.println("简体: " + result);
				assertEquals("#e53333玩家'#00d5ff上帝'#e53333成功将#009900泡泡手雷#e53333强化到#337fe54#e53333级，战斗力又上了一个新台阶。", result);
				
				//Tell thread 1 to run
				sema.release(1);
			}
		};
		
		ExecutorService executor = Executors.newCachedThreadPool();
		executor.execute(r1);
		executor.execute(r2);
		executor.awaitTermination(10, TimeUnit.SECONDS);
	}
	
	@Test
	public void collectAllLocationMessage() {
		/**
		 * equipments_new 
		 * name
		 * 
		 * items:
		 * name, info
		 * 
		 * tasks
		 * name, desc, taskTarget
		 * 
		 * maps
		 * name, 
		 * 
		 * tips
		 * tip
		 */
		TreeSet<String> set = new TreeSet<String>(); 
		TreeSet<String> miniSet = new TreeSet<String>(); 
		Collection<WeaponPojo> weapons = EquipManager.getInstance().getWeapons();
		for ( WeaponPojo weapon: weapons ) {
			int index = weapon.getName().indexOf('●');
			String name = weapon.getName();
			if ( index > 0 ) {
				name = name.substring(index+1);
			}
			if ( name.length() == 0 ) {
				System.out.println(weapon.getId()+":"+weapon.getName());
			}
			String str = "equipments_new.name\t"+name+"\t"+name+"\"\n";
			set.add(str);
			miniSet.add(str.replaceAll("\\d+", "{}"));
		}
		Collection<ItemPojo> items = ItemManager.getInstance().getItems();
		for ( ItemPojo item : items ) {
			String str = "item.name\t"+item.getName()+"\t"+item.getName()+"\"\n";
			set.add(str);
			miniSet.add(str.replaceAll("\\d+", "{}"));
		}
		for ( ItemPojo item : items ) {
			String str = "item.info\t"+item.getInfo()+"\t\""+item.getInfo()+"\"\n";
			set.add(str);
			miniSet.add(str.replaceAll("\\d+", "{}"));
		}
		Collection<TaskPojo> tasks = TaskManager.getInstance().getTasks();
		for ( TaskPojo task : tasks ) {
			String str = "task.name\t"+task.getName()+"\t\""+task.getName()+"\"\n";
			set.add(str);
			miniSet.add(str.replaceAll("\\d+", "{}"));
		}
		for ( TaskPojo task : tasks ) {
			String desc = task.getDesc().replaceAll("\n", "\\\\n");
			String str = "task.desc\t"+desc+"\t\""+desc+"\"\n";
			set.add(str);
			miniSet.add(str.replaceAll("\\d+", "{}"));
		}
		for ( TaskPojo task : tasks ) {
			String str = "task.tasktarget\t"+task.getTaskTarget()+"\t\""+task.getTaskTarget().replaceAll("\n", "\\aaan")+"\"\n";
			set.add(str);
			miniSet.add(str.replaceAll("\\d+", "{}"));
		}
		Collection<MapPojo> maps = MapManager.getInstance().getMaps();
		for ( MapPojo map : maps ) {
			String str = "map.name\t"+map.getName()+"\t\""+map.getName()+"\"\n";
			set.add(str);
			miniSet.add(str.replaceAll("\\d+", "{}"));
		}
		Collection<TipPojo> tips = TipManager.getInstance().getTips();
		for ( TipPojo tip : tips ) {
			String str = "tips.tip\t"+tip.getTip()+"\t\""+tip.getTip()+"\"\n";
			set.add(str);
			miniSet.add(str.replaceAll("\\d+", "{}"));
		}
		
		StringBuilder buf = new StringBuilder(50000);
		for ( String str : set ) {
			buf.append(str);
		}
		
		try {
			FileWriter fw = new FileWriter("gameres.txt");
			fw.write(buf.toString());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		buf = new StringBuilder(50000);
		for ( String str : miniSet ) {
			buf.append(str);
		}
		
		try {
			FileWriter fw = new FileWriter("gameres_mini.txt");
			fw.write(buf.toString());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 */
	@Test
	public void replaceGameName() {
		String originalName = "宝贝战争";
		String newName = "小小飞弹";
		HashSet<Object> modified = new HashSet<Object>();
		Collection<WeaponPojo> weapons = EquipManager.getInstance().getWeapons();
		for ( WeaponPojo weapon: weapons ) {
			String name = weapon.getName();
			if ( name.contains(originalName) ) {
				name = name.replaceAll(originalName, newName);
				weapon.setName(name);
				modified.add(weapon);
			}
			String desc = weapon.getInfo();
			if ( desc.contains(originalName) ) {
				desc = desc.replaceAll(originalName, newName);
				weapon.setInfo(desc);
				modified.add(weapon);
			}
		}
		Collection<ItemPojo> items = ItemManager.getInstance().getItems();
		for ( ItemPojo item : items ) {
			String name = item.getName();
			if ( name.contains(originalName) ) {
				name = name.replaceAll(originalName, newName);
				item.setName(name);
				modified.add(item);
			}
			String desc = item.getInfo();
			if ( desc.contains(originalName) ) {
				desc = desc.replaceAll(originalName, newName);
				item.setInfo(desc);
				modified.add(item);
			}
		}
		Collection<TaskPojo> tasks = TaskManager.getInstance().getTasks();
		for ( TaskPojo task : tasks ) {
			String name = task.getName();
			if ( name.contains(originalName) ) {
				name = name.replaceAll(originalName, newName);
				task.setName(name);
				modified.add(task);
			}
			String desc = task.getDesc();
			if ( desc.contains(originalName) ) {
				desc = desc.replaceAll(originalName, newName);
				task.setDesc(desc);
				modified.add(task);
			}
		}
		Collection<MapPojo> maps = MapManager.getInstance().getMaps();
		for ( MapPojo map : maps ) {
			String name = map.getName();
			if ( name.contains(originalName) ) {
				name = name.replaceAll(originalName, newName);
				map.setName(name);
				modified.add(map);
			}
		}
		Collection<TipPojo> tips = TipManager.getInstance().getTips();
		for ( TipPojo tip : tips ) {
			String name = tip.getTip();
			if ( name.contains(originalName) ) {
				name = name.replaceAll(originalName, newName);
				tip.setTip(name);
				modified.add(tip);
			}
		}
		
		for ( Object pojo : modified ) {
			System.out.println(pojo);
		}

		String database = "babywar", namespace="server0001";
		for ( Object obj : modified ) {
			MapDBObject dbObject = new MapDBObject();
			dbObject.putAll(obj);
			DBObject query = MongoDBUtil.createDBObject("_id", dbObject.get("_id"));
			String collection = null;
			if ( obj instanceof WeaponPojo ) {
				collection = "equipments_new";
			} else if ( obj instanceof ItemPojo ) {
				collection = "items";
			} else if ( obj instanceof MapPojo ) {
				collection = "maps";
			} else if ( obj instanceof TaskPojo ) {
				collection = "tasks";
			} else if ( obj instanceof TipPojo ) {
				collection = "tips";
			}
			MongoDBUtil.saveToMongo(query, dbObject, database, namespace, collection, true);
		}

	}
	
	/**
	 * 读取一个翻译文件的内容，翻译文件格式为
	 *  简体中文 \t 翻译文字
	 * 程序会自动插入可变数字到翻译文字中，然后
	 * 将结果保存到mongo数据库
	 * 
	 */
	public void translate() {
		String database = "babywar";
		String namespace = "server0001";
		String collection = "gameres";
		String file = "gameres_zh_TW.txt";
		/**
				"黑铁", 
				"青铜",
				"赤钢",
				"白银",
				"黄金",
				"琥珀",
				"翡翠",
				"水晶",
				"钻石",
				"神圣",
		 */
		
		HashMap<String, String> transMap = new HashMap<String, String>();
		//Read the translation map
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			while ( line != null ) {
				String[] fields = line.split("\\t");
				transMap.put(fields[0], fields[1]);
				line = br.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//Do translation
		ArrayList<String> transList = new ArrayList<String>();
		Collection<WeaponPojo> weapons = EquipManager.getInstance().getWeapons();
		for ( WeaponPojo weapon: weapons ) {
			int index = weapon.getName().indexOf('●');
			String name = weapon.getName();
			String prefix = null;
			if ( index > 0 ) {
				prefix = name.substring(0, index);
				name = name.substring(index+1);
			}
			String tranStr = findTranslateStr(name, transMap);
			if ( prefix != null ) {
				prefix = findTranslateStr(prefix, transMap);
				tranStr = prefix+"●"+name;
			}
			String str = "equipments_new_name_"+weapon.getId()+"\t"+tranStr;
			transList.add(str);
		}
		Collection<ItemPojo> items = ItemManager.getInstance().getItems();
		for ( ItemPojo item : items ) {
			String tranStr = findTranslateStr(item.getName(), transMap);
			String str = "items_name_"+item.getId()+"\t"+tranStr;
			transList.add(str);
		}
		for ( ItemPojo item : items ) {
			String tranStr = findTranslateStr(item.getInfo(), transMap);
			String str = "items_info_"+item.getId()+"\t"+tranStr;
			transList.add(str);
		}
		Collection<TaskPojo> tasks = TaskManager.getInstance().getTasks();
		for ( TaskPojo task : tasks ) {
			String tranStr = findTranslateStr(task.getName(), transMap);
			String str = "tasks_name_"+task.getId()+"\t"+tranStr;
			transList.add(str);
		}
		for ( TaskPojo task : tasks ) {
			String tranStr = findTranslateStr(task.getDesc(), transMap);
			String str = "tasks_desc_"+task.getId()+"\t"+tranStr;
			transList.add(str);
		}
		for ( TaskPojo task : tasks ) {
			String tranStr = findTranslateStr(task.getTaskTarget(), transMap);
			String str = "tasks_taskTarget_"+task.getId()+"\t"+tranStr;
			transList.add(str);
		}
		Collection<MapPojo> maps = MapManager.getInstance().getMaps();
		for ( MapPojo map : maps ) {
			String tranStr = findTranslateStr(map.getName(), transMap);
			String str = "maps_name_"+map.getId()+"\t"+tranStr;
			transList.add(str);
		}
		Collection<TipPojo> tips = TipManager.getInstance().getTips();
		for ( TipPojo tip : tips ) {
			String tranStr = findTranslateStr(tip.getTip(), transMap);
			String str = "tips_tip_"+tip.getId()+"\t"+tranStr;
			transList.add(str);
		}
		
		for ( String str : transList ) {
			String[] fields = str.split("\t");
			DBObject idObj = MongoDBUtil.createDBObject("_id", fields[0]);
			DBObject dbObj = MongoDBUtil.createDBObject("_id", fields[0]);
			dbObj.put("zh_TW", fields[1].trim());
			MongoDBUtil.saveToMongo(idObj, 
					dbObj, database, namespace, collection, true);
		}
	}
	
	/**
	 * Find the translated i18n string.
	 * 
	 * @param zhCNString
	 * @param transMap
	 * @return
	 */
	private String findTranslateStr(String zhCNString, HashMap<String, String> transMap) {
		String key = zhCNString.replaceAll("\\d+", "{}");
		String value = transMap.get(key);
		if ( value == null || value.equals("null") ) {
			System.out.println("key="+key);
		}
		Pattern pattern = Pattern.compile("\\d+");
		Matcher macher = pattern.matcher(zhCNString);
		ArrayList<String> digits = new ArrayList<String>();
		while ( macher.find() ) {
			int start = macher.start();
			int end = macher.end();
			String digit = zhCNString.substring(start, end);
			digits.add(digit);
		}
		if ( digits.size() > 0 ) {
			FormattingTuple tuple = MessageFormatter.arrayFormat(value, digits.toArray());
			return tuple.getMessage();
		}
		return value;
	}
}
