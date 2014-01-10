package com.xinqihd.sns.gameserver.admin.data;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.util.WindowUtils;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.config.ConfigKey;
import com.xinqihd.sns.gameserver.admin.config.ConfigManager;
import com.xinqihd.sns.gameserver.admin.gui.MainFrame;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.MoneyType;
import com.xinqihd.sns.gameserver.config.ShopCatalog;
import com.xinqihd.sns.gameserver.config.ShopPojo;
import com.xinqihd.sns.gameserver.config.ShopPojo.BuyPrice;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.MapDBObject;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;

/**
 * 
 * @author wangqi
 *
 */
public class ShopDataPriceSaveService extends SwingWorker<Void, Integer> {
	
	private int count = 0;
	private JDialog dialog = null;
	private JProgressBar progressBar = new JProgressBar();;
	private JXPanel panel = null;
	private JXLabel label = new JXLabel("正在保存装备的商品数据");
	private Stage stage = Stage.INIT;
	private ShopDataPriceResultModel model = null;
	private String database = ConfigManager.getConfigAsString(ConfigKey.mongoConfigDBName);
	private String namespace = ConfigManager.getConfigAsString(ConfigKey.mongoConfigNamespace);
	private String collection = "shops_new";
	private int simpleTimes = 0;
	private int normalTimes = 0;
	private int solidTimes = 0;
	private int eternalTimes = 0;
	
	public ShopDataPriceSaveService(ShopDataPriceResultModel model) {
		this.model = model;
		this.count = model.getRowCount();
		
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
		
		simpleTimes = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.WEAPON_INDATE_SIMPLE, 30);
		normalTimes = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.WEAPON_INDATE_NORMAL, 100);
		solidTimes = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.WEAPON_INDATE_SOLID, 200);
		eternalTimes = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.WEAPON_INDATE_ETERNAL, Integer.MAX_VALUE);
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground() throws Exception {
		try {
			stage = Stage.INIT;
			//initialize
			publish();
			Thread.sleep(100);
			
			stage = Stage.DO_SAVE;
			int base = 1000;
			ArrayList<ShopPojo> shops = new ArrayList<ShopPojo>(count);

			for ( int i=0; i<count; i++ ) {
				ShopDataPricePrintResult result = (ShopDataPricePrintResult)model.
						getRowObject(i);
				WeaponPojo weapon = result.getWeaponPojo();
				int m = 0;
				for ( MoneyType moneyType : MoneyType.values() ) {
					if ( moneyType != MoneyType.GOLDEN ) {
						ShopPojo shop = createShopPojo(
								weapon, moneyType, base+i*MoneyType.values().length+(m++), result);
						shops.add(shop);
					}
				}
			}
			
			int i=0;
			for ( ShopPojo shop : shops ) {
				MapDBObject dbObject = new MapDBObject();
				dbObject.putAll(shop);
				DBObject query = MongoUtil.createDBObject("_id", shop.getId());
				MongoUtil.saveToMongo(query, dbObject, database, namespace, collection, true);
				publish(i++);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private ShopPojo createShopPojo(WeaponPojo weapon, MoneyType moneyType, int id, 
			ShopDataPricePrintResult result) {
		ShopPojo shop = new ShopPojo();
		shop.setId(String.valueOf(id));
		shop.setType(Integer.parseInt(weapon.getTypeName()));
		shop.setInfo(weapon.getName());
		shop.setPropInfoId(weapon.getId());
		shop.setLevel(weapon.getUserLevel());
		shop.setMoneyType(moneyType);
		shop.setBanded(1);
		shop.setDiscount(100);
		shop.setSell(1);
		shop.setLimitCount(-1);
		shop.setLimitGroup(-1);
		shop.setShopId(id);
		EquipType equipType = weapon.getSlot();
		switch ( equipType ) {
			case BUBBLE:
			case CLOTHES:
			case DECORATION:
			case EXPRESSION:
			case GLASSES:	
			case HAIR:
			case HAT:
			case JEWELRY:
			case OTHER:
			case WING:
			case FACE:
				shop.addCatalog(ShopCatalog.DECORATION);
				break;
			case GIFT_PACK:
				shop.addCatalog(ShopCatalog.GIFTPACK);
				break;
			case ITEM:
				shop.addCatalog(ShopCatalog.ITEM);
				break;
			case SUIT:
				shop.addCatalog(ShopCatalog.SUITE);
				break;
			case WEAPON:
			case OFFHANDWEAPON:
				shop.addCatalog(ShopCatalog.WEAPON);
				break;
		}
		int price = 0;
		BuyPrice simplePrice = null, normalPrice = null, 
				solidPrice = null, eternalPrice = null;
		ArrayList<BuyPrice> buyPrices = new ArrayList<BuyPrice>();
		switch ( moneyType ) {
			case GOLDEN:
				price = result.getGoldenPrice();
				simplePrice = new BuyPrice();
				simplePrice.price = price;
				simplePrice.validTimes = simpleTimes;

				price = result.getNormalGoldPrice();
				normalPrice = new BuyPrice();
				normalPrice.price = price;
				normalPrice.validTimes = normalTimes;

				price = result.getSolidGoldPrice();
				solidPrice = new BuyPrice();
				solidPrice.price = price;
				solidPrice.validTimes = solidTimes;

				price = result.getEternalGoldPrice();
				eternalPrice = new BuyPrice();
				eternalPrice.price = price;
				eternalPrice.validTimes = eternalTimes;
				break;
			case MEDAL:
				price = result.getMedalPrice();
				simplePrice = new BuyPrice();
				simplePrice.price = price;
				simplePrice.validTimes = simpleTimes;

				price = result.getNormalMedalPrice();
				normalPrice = new BuyPrice();
				normalPrice.price = price;
				normalPrice.validTimes = normalTimes;

				price = result.getSolidMedalPrice();
				solidPrice = new BuyPrice();
				solidPrice.price = price;
				solidPrice.validTimes = solidTimes;

				price = result.getEternalMedalPrice();
				eternalPrice = new BuyPrice();
				eternalPrice.price = price;
				eternalPrice.validTimes = eternalTimes;
				break;
			case VOUCHER:
				price = result.getVoucherPrice();
				simplePrice = new BuyPrice();
				simplePrice.price = price;
				simplePrice.validTimes = simpleTimes;

				price = result.getNormalVoucherPrice();
				normalPrice = new BuyPrice();
				normalPrice.price = price;
				normalPrice.validTimes = normalTimes;

				price = result.getSolidVoucherPrice();
				solidPrice = new BuyPrice();
				solidPrice.price = price;
				solidPrice.validTimes = solidTimes;

				price = result.getEternalVoucherPrice();
				eternalPrice = new BuyPrice();
				eternalPrice.price = price;
				eternalPrice.validTimes = eternalTimes;
				break;
			case YUANBAO:
				price = result.getYuanbaoPrice();
				simplePrice = new BuyPrice();
				simplePrice.price = price;
				simplePrice.validTimes = simpleTimes;

				price = result.getNormalYuanbaoPrice();
				normalPrice = new BuyPrice();
				normalPrice.price = price;
				normalPrice.validTimes = normalTimes;

				price = result.getSolidYuanbaoPrice();
				solidPrice = new BuyPrice();
				solidPrice.price = price;
				solidPrice.validTimes = solidTimes;

				price = result.getEternalYuanbaoPrice();
				eternalPrice = new BuyPrice();
				eternalPrice.price = price;
				eternalPrice.validTimes = eternalTimes;
				break;
		}
		buyPrices.add(simplePrice);
		buyPrices.add(normalPrice);
		buyPrices.add(solidPrice);
		buyPrices.add(eternalPrice);
		shop.setBuyPrices(buyPrices);
		
		return shop;
	}
	
	public JDialog getDialog() {
		return dialog;
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
		} else if ( stage == Stage.DO_SAVE ) {
			label.setText("正在保存商品数据...");
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
		DO_SAVE,
	}

}
