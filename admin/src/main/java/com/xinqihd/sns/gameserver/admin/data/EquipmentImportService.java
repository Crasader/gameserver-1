package com.xinqihd.sns.gameserver.admin.data;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.util.WindowUtils;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.gui.MainFrame;
import com.xinqihd.sns.gameserver.admin.model.MyTableModel;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.MoneyType;
import com.xinqihd.sns.gameserver.config.ShopCatalog;
import com.xinqihd.sns.gameserver.config.ShopPojo;
import com.xinqihd.sns.gameserver.config.ShopPojo.BuyPrice;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.MapDBObject;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;
import com.xinqihd.sns.gameserver.db.mongo.ShopManager;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * 
 * @author wangqi
 *
 */
public class EquipmentImportService extends SwingWorker<Void, Integer> {
	
	private int count = 0;
	private JDialog dialog = null;
	private JProgressBar progressBar = new JProgressBar();;
	private JXPanel panel = null;
	private JXLabel label = new JXLabel("正在导入装备基本数据");
	private Stage stage = Stage.INIT;
	private MyTableModel model = null;
	private ShopDataPriceConfig config = null;
	private String database = "babywarcfg";
	private String namespace="server0001";
	private String collection="equipments_new";
	private String shopColl = "shops_new";
	
	public EquipmentImportService(MyTableModel model, ShopDataPriceConfig config) {
		this.model = model;
		this.config = config;
		
		panel = new JXPanel();
		panel.setLayout(new MigLayout("wrap 1"));
		panel.add(label, "growx, wrap 20");
		panel.add(progressBar, "grow, push");
		this.dialog = new JDialog();
		this.dialog.add(panel);
		this.dialog.setSize(300, 120);
		Point p = WindowUtils.getPointForCentering(dialog);
		this.dialog.setLocation(p);
		this.dialog.setModal(true);
		this.dialog.setResizable(false);
		this.dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground() throws Exception {
		try {
			stage = Stage.INIT;
			//initialize
			model.clearAll();
			publish();
			
			stage = Stage.PRINT_PRICE;
			//寻找商城的最大ID
			DBObject query = MongoUtil.createDBObject();
			DBObject fields = MongoUtil.createDBObject("_id", "1");
			List<DBObject> dbObjs = MongoUtil.queryAllFromMongo(query, database, namespace, shopColl, fields);
			int maxId = 0;
			for ( DBObject obj : dbObjs ) {
				int id = StringUtil.toInt(obj.get("_id").toString(), 0);
				if ( id < 20000 && maxId < id ) {
					maxId = id;
				} else {
					//throw new RuntimeException("The shop's id exceeds 20000");
				}
			}
			
			//导入EXCEL文件
			JFileChooser chooser = new JFileChooser(
					new File("/Users/wangqi/disk/documents/20110418/宝贝战争/游戏运营/游戏改造"));
			int result = chooser.showOpenDialog(dialog);
			File file = null;
			if ( result == JFileChooser.APPROVE_OPTION ) {
				file = chooser.getSelectedFile();
			} else {
				JOptionPane.showMessageDialog(dialog, "没有选择导入的文件");
				return null;
			}
			FileInputStream fis = new FileInputStream(file);
			HSSFWorkbook workbook = new HSSFWorkbook(fis);
			
			Sheet sheet = workbook.getSheet("装备数据");
			
			int totalRow = sheet.getLastRowNum();
			this.count = totalRow;
			
			ArrayList<WeaponPojo> weapons = new ArrayList<WeaponPojo>();
			ArrayList<ShopPojo> shops = new ArrayList<ShopPojo>();

			for ( int i=3; i<=totalRow; i++ ) {
				/**
				 * ID	名称	描述	品质	颜色	战斗力	
				 */
				int j = 0;
				Row row = sheet.getRow(i);
				//ID
				Cell cell = row.getCell(j++);
				if ( cell == null ) continue;
				String weaponId = getCellValue(cell).toString();
				
				WeaponPojo weapon = EquipManager.getInstance().getWeaponById(weaponId);
				if ( weapon == null ) {
					weapon = new WeaponPojo();
					weapon.setId(weaponId);
				}
				
				//名称
				cell = row.getCell(j++);
				String name = getCellValue(cell).toString();
				weapon.setName(name);
				weapon.setsName(name);
				
				//描述
				cell = row.getCell(j++);
				weapon.setInfo(getCellValue(cell).toString());
				
				//品质
				cell = row.getCell(j++);
				weapon.setQuality((Integer)getCellValue(cell));
				
				//颜色
				cell = row.getCell(j++);
				WeaponColor color = WeaponColor.valueOf(getCellValue(cell).toString());
				
				//战斗力
				cell = row.getCell(j++);
				int power = (Integer)getCellValue(cell);
				weapon.setPower(power);
				
				// 攻击	防御	敏捷	幸运	血量值	血量比率	伤害	护甲	半径(宽)	半径(高)	
				
				//攻击
				cell = row.getCell(j++);
				weapon.setAddAttack((Integer)getCellValue(cell));
				
				//防御
				cell = row.getCell(j++);
				weapon.setAddDefend((Integer)getCellValue(cell));
				
				//敏捷
				cell = row.getCell(j++);
				weapon.setAddAgility((Integer)getCellValue(cell));
				
				//幸运
				cell = row.getCell(j++);
				weapon.setAddLuck((Integer)getCellValue(cell));
				
				//血量值
				cell = row.getCell(j++);
				weapon.setAddBlood((Integer)getCellValue(cell));
				
				//血量比率
				cell = row.getCell(j++);
				weapon.setAddBloodPercent((Integer)getCellValue(cell));
				
				//伤害
				cell = row.getCell(j++);
				weapon.setAddDamage((Integer)getCellValue(cell));
				
				//护甲
				cell = row.getCell(j++);
				weapon.setAddSkin((Integer)getCellValue(cell));
				
				//半径(宽)
				cell = row.getCell(j++);
				weapon.setRadius((Integer)getCellValue(cell));
				
				//半径(高)
				cell = row.getCell(j++);
				weapon.setsRadius((Integer)getCellValue(cell));
				
				//子弹	性别	卡槽	类型ID	用户等级	是否抽奖
				
				//子弹
				cell = row.getCell(j++);
				if ( cell != null ) {
					String bullet = getCellValue(cell).toString();
					if ( StringUtil.checkNotEmpty(bullet) ) {
						weapon.setBullet(bullet);					
					}
				}
				
				//性别
				cell = row.getCell(j++);
				Gender gender = Gender.valueOf(getCellValue(cell).toString().toUpperCase());
				weapon.setSex(gender);
				
				//卡槽
				cell = row.getCell(j++);
				EquipType slot = EquipType.valueOf(getCellValue(cell).toString().toUpperCase());
				weapon.setSlot(slot);

				//类型ID
				cell = row.getCell(j++);
				String typeId = getCellValue(cell).toString();
				//weapon.setTypeName(typeId);
				
				//用户等级
				cell = row.getCell(j++);
				weapon.setUserLevel((Integer)getCellValue(cell));
				
				//是否抽奖
				cell = row.getCell(j++);
				weapon.setCanBeRewarded((Boolean)getCellValue(cell));
				
				//金币简陋	元宝简陋	金币普通	元宝普通	金币坚固	元宝坚固	金币恒久	元宝恒久
			  
				ShopPojo shopPojo = new ShopPojo();
				
			  //金币简陋
				cell = row.getCell(j++);
				
				//元宝简陋
				cell = row.getCell(j++);
				BuyPrice simple = new BuyPrice();
				simple.validTimes = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.WEAPON_INDATE_SIMPLE, 30);
				simple.price = (Integer)getCellValue(cell);
				
				//金币普通
				cell = row.getCell(j++);
				
   			//元宝普通
				cell = row.getCell(j++);
				BuyPrice normal = new BuyPrice();
				normal.validTimes = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.WEAPON_INDATE_NORMAL, 100);
				normal.price = (Integer)getCellValue(cell);
		
				//金币坚固
				cell = row.getCell(j++);
				
				//元宝坚固
				cell = row.getCell(j++);
				BuyPrice solid = new BuyPrice();
				solid.validTimes = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.WEAPON_INDATE_SOLID, 200);
				solid.price = (Integer)getCellValue(cell);
				
				//金币恒久
				cell = row.getCell(j++);
				
				//元宝恒久
				cell = row.getCell(j++);
				//BuyPrice external = new BuyPrice();
				//external.validTimes = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.WEAPON_INDATE_ETERNAL, Integer.MAX_VALUE);
				//external.price = (Integer)getCellValue(cell);
				
				ArrayList<BuyPrice> prices = new ArrayList<BuyPrice>();
				prices.add(simple);
				prices.add(normal);
				prices.add(solid);
				//prices.add(external);
				
				shopPojo.setBuyPrices(prices);
				shopPojo.setInfo(weapon.getName());
				shopPojo.setPropInfoId(weaponId);
				shopPojo.setSell(0);
				shopPojo.setMoneyType(MoneyType.YUANBAO);
				
				ArrayList<ShopCatalog> shopCatalogs = new ArrayList<ShopCatalog>();
				ShopCatalog catalog = ShopManager.convertSlotToCatalog(weapon.getSlot());
				shopCatalogs.add(catalog);
				shopPojo.setCatalogs(shopCatalogs);
				shopPojo.setDiscount(100);
				shopPojo.setLevel(weapon.getUserLevel());
				shopPojo.setBanded(1);
				
				weapons.add(weapon);
				shops.add(shopPojo);

				publish(i);
			}
			if ( weapons.size() > 0 && shops.size() == weapons.size() ) {
				saveWeapons(weapons, shops, maxId+1);
				JOptionPane.showMessageDialog(dialog, "成功导入了"+weapons.size()+"件装备");
			} else {
				JOptionPane.showMessageDialog(dialog, "未能成功导入数据，请检查格式");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	public JDialog getDialog() {
		return dialog;
	}
	
	/**
	 * 保存修改后的数值
	 * @param weapons
	 */
	private void saveWeapons(ArrayList<WeaponPojo> weapons, ArrayList<ShopPojo> shops, int maxShopId) {
		ArrayList<WeaponPojo> allWeapons = new ArrayList<WeaponPojo>();
		ArrayList<ShopPojo> allShops = new ArrayList<ShopPojo>();
		double[] ratios = GameDataManager.getInstance().getGameDataAsDoubleArray(GameDataKey.WEAPON_LEVEL_RATIO);
		String[] weaponLevel = {
				"青铜","赤钢","白银","黄金","琥珀","翡翠","水晶","钻石","神圣"
		};
		int index = 0;
		for ( int i=0; i<weapons.size(); i++ ) {
			WeaponPojo weapon = weapons.get(i);
			if ( weapon.getTypeName() == null ) {
				int id = StringUtil.toInt(weapon.getId(), 0);
				weapon.setTypeName(String.valueOf(id/10));
			}
			allWeapons.add(weapon);
			
			ShopPojo shop = shops.get(i);
			ShopPojo oldShop = getShopPojo(weapon);
			if ( oldShop != null ) {
				shop.setId(oldShop.getId());
			} else {
				int shopId = maxShopId+(index++);
				if ( shopId >= 20000 ) {
					throw new RuntimeException("The shop's id exceeds 20000");
				}
				shop.setId(String.valueOf(shopId));
			}
			
			allShops.add(shop);
			
			//拿到的只是黑铁武器，用数值扩展到其他等级武器
			//按比例增加血量的道具不做处理
			for ( int userLevel=10; userLevel<100; userLevel+=10 ) {
				WeaponPojo newWeapon = EquipManager.getInstance().getWeaponByTypeNameAndUserLevel(weapon.getTypeName(), userLevel);
				if ( newWeapon == null ) {
					newWeapon = weapon.clone();
					int id = StringUtil.toInt(weapon.getId(), 0) + userLevel/10;
					String prefix = weaponLevel[ userLevel/10-1 ];
					newWeapon.setId(String.valueOf(id));
					newWeapon.setName(prefix+"●WEAPON");
					newWeapon.setTypeName(weapon.getTypeName());
				}
				int levelIndex = userLevel/10;
				int attack = (int)Math.round(weapon.getAddAttack() * ratios[levelIndex]);
				newWeapon.setAddAttack(attack);
				int defend = (int)Math.round(weapon.getAddDefend() * ratios[levelIndex]);
				newWeapon.setAddDefend(defend);
				int agility = (int)Math.round(weapon.getAddAgility() * ratios[levelIndex]);
				newWeapon.setAddAgility(agility);
				int lucky = (int)Math.round(weapon.getAddLuck() * ratios[levelIndex]);
				newWeapon.setAddLuck(lucky);
				int blood = (int)Math.round(weapon.getAddBlood() * ratios[levelIndex]);
				newWeapon.setAddBlood(blood);
				int damage = (int)Math.round(weapon.getAddDamage() * ratios[levelIndex]);
				newWeapon.setAddDamage(damage);
				int skin = (int)Math.round(weapon.getAddSkin() * ratios[levelIndex]);
				newWeapon.setAddSkin(skin);
				//same field
				int radius = weapon.getRadius();
				newWeapon.setRadius(radius);
				newWeapon.setsRadius(weapon.getsRadius());
				newWeapon.setBullet(weapon.getBullet());
				newWeapon.setSex(weapon.getSex());
				newWeapon.setSlot(weapon.getSlot());
				newWeapon.setUserLevel(userLevel);
				newWeapon.setCanBeRewarded(weapon.isCanBeRewarded());
				
				int power = (int)Math.round(calculateWeaponPower(
						attack, defend, agility, lucky, blood, skin, radius));
				newWeapon.setPower(power);
				
				int beginIndex = weapon.getName().indexOf("●");
				String prefix = newWeapon.getName().substring(0, beginIndex);
				if ( beginIndex>0 ) {
					String oldName = weapon.getName().substring(beginIndex+1);
					int newBeginIndex = newWeapon.getName().indexOf("●");
					String newName = newWeapon.getName();
					if (newBeginIndex>0) {
						newName = newWeapon.getName().substring(newBeginIndex+1);
					}
					if ( !oldName.equals(newName) ) {
						newName = oldName;
					}
					newWeapon.setName(prefix+"●"+oldName);
					newWeapon.setsName(prefix+"●"+oldName);
					newWeapon.setInfo(weapon.getInfo());
				}
				allWeapons.add(newWeapon);
				
				ShopPojo shopPojo = getShopPojo(newWeapon);
				if ( shopPojo == null ) {
					shopPojo = new ShopPojo();
				}

				List<BuyPrice> oPrices = shop.getBuyPrices();
				//元宝简陋
				BuyPrice simple = new BuyPrice();
				simple.validTimes = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.WEAPON_INDATE_SIMPLE, 30);
				BuyPrice oPrice = oPrices.get(0);
				simple.price = (int)Math.round(oPrice.price * ratios[levelIndex]);
				
   			//元宝普通
				BuyPrice normal = new BuyPrice();
				normal.validTimes = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.WEAPON_INDATE_NORMAL, 100);
				oPrice = oPrices.get(1);
				normal.price = (int)Math.round(oPrice.price * ratios[levelIndex]);

				//元宝坚固
				BuyPrice solid = new BuyPrice();
				solid.validTimes = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.WEAPON_INDATE_SOLID, 200);
				oPrice = oPrices.get(2);
				solid.price = (int)Math.round(oPrice.price * ratios[levelIndex]);

				//元宝恒久
				//BuyPrice external = new BuyPrice();
				//external.validTimes = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.WEAPON_INDATE_ETERNAL, Integer.MAX_VALUE);

				ArrayList<BuyPrice> prices = new ArrayList<BuyPrice>();
				prices.add(simple);
				prices.add(normal);
				prices.add(solid);
				//prices.add(external);

				shopPojo.setBuyPrices(prices);
				shopPojo.setInfo(newWeapon.getName());
				shopPojo.setPropInfoId(newWeapon.getId());
				shopPojo.setMoneyType(MoneyType.YUANBAO);

				ArrayList<ShopCatalog> shopCatalogs = new ArrayList<ShopCatalog>();
				ShopCatalog catalog = ShopManager.convertSlotToCatalog(newWeapon.getSlot());
				shopCatalogs.add(catalog);
				shopPojo.setCatalogs(shopCatalogs);
				shopPojo.setDiscount(100);
				shopPojo.setLevel(newWeapon.getUserLevel());
				shopPojo.setBanded(1);
				
				if ( prefix == weaponLevel[0] )   {
					allShops.add(shopPojo);
				}
			}
		}
		
		for ( WeaponPojo weapon : allWeapons ) {
			//拿到的只是黑铁武器，用数值扩展到其他等级武器
			MapDBObject dbObject = new MapDBObject();
			dbObject.putAll(weapon);
			DBObject query = MongoUtil.createDBObject("_id", weapon.getId());
			//System.out.println(weapon.getName()+":"+weapon.getPower());
			MongoUtil.saveToMongo(query, dbObject, database, namespace, collection, true);
		}
		for ( int i=0; i<allShops.size(); i++ ) {
			ShopPojo shop = allShops.get(i);
			MapDBObject dbObject = new MapDBObject();
			dbObject.putAll(shop);
			DBObject query = MongoUtil.createDBObject("_id", shop.getId());
			System.out.println(shop.toString());
			MongoUtil.saveToMongo(query, dbObject, database, namespace, shopColl, true);
		}
		EquipManager.getInstance().reload();
		ShopManager.getInstance().reload();
	}

	/**
	 * @param maxShopId
	 * @param index
	 * @param newWeapon
	 * @return
	 */
	private ShopPojo getShopPojo(WeaponPojo newWeapon) {
		Collection<ShopPojo> weaponShopColl = ShopManager.getInstance().getShopsByPropInfoId(newWeapon.getId());
		ShopPojo shopPojo = null;
		if ( weaponShopColl != null && weaponShopColl.size() > 0 ) {
			shopPojo = (ShopPojo)weaponShopColl.iterator().next();
		}
		return shopPojo;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#process(java.util.List)
	 */
	@Override
	protected void process(List<Integer> chunks) {
		if ( stage == Stage.INIT ) {						
			label.setFont(MainFrame.BIG_FONT);
			progressBar.setMaximum(count);
			progressBar.setStringPainted(true);
			
			dialog.setVisible(true);
		} else if ( stage == Stage.PRINT_PRICE ) {
			label.setText("正在导入装备数据...");
			if ( chunks != null && chunks.size()>0 ) {
				int percent = chunks.get(chunks.size()-1);
				progressBar.setValue(percent);
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
		this.dialog.dispose();
	}

	static enum Stage {
		INIT,
		PRINT_PRICE,
	}

	private static final double calculateWeaponPower(
			int attack, int defend, int agility, int luck, int blood, int skin, int radius) {
	  // 战斗力
		Double attackIndex = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.GAME_ATTACK_INDEX, 1.2);
		Double defendIndex = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.GAME_DEFEND_INDEX, 1.3);
		Double skinIndex = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.GAME_SKIN_INDEX, 0.2);
		Double luckTotal = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.BATTLE_CRITICAL_MAX, 4000);
	
		/*
			1.5	200	0.0075
			1.4	190	0.007368421
			1.3	180	0.007222222
			1.2	165	0.007272727
			1.1	150	0.007333333
		 */
		double dpr = (blood*0.5 + skin*skinIndex + attack * attackIndex + defend *defendIndex);
		double power = dpr + dpr*(agility*0.0074) + dpr*(luck/luckTotal*(1.5+2*luck/luckTotal));
		
		float radiusRatio = radius/150.0f;
		power = power + (int)(power*radiusRatio/1.5);

	  return Math.round(power);
	}

	public Object getCellValue(Cell cell) {
		return getCellValue(cell, true);
	}
	
	public Object getCellValue(Cell cell, boolean toInt) {
		if ( cell == null ) {
			System.out.println(cell.getRowIndex()+":"+cell.getColumnIndex()+ " is null type");
		}
		switch (cell.getCellType()) {
			case Cell.CELL_TYPE_STRING:
				return cell.getRichStringCellValue().getString();
			case Cell.CELL_TYPE_NUMERIC:
				if (DateUtil.isCellDateFormatted(cell)) {
					return cell.getDateCellValue();
				} else {
					if ( toInt ) {
						Double d = cell.getNumericCellValue();
						return d.intValue();
					} else {
						return cell.getNumericCellValue();
					}
				}
			case Cell.CELL_TYPE_BOOLEAN:
				return cell.getBooleanCellValue();
			case Cell.CELL_TYPE_FORMULA:
				if ( toInt ) {
					Double d = cell.getNumericCellValue();
					return d.intValue();
				} else {
					return cell.getNumericCellValue();
				}
			default:
				return "";
		}
	}
}
