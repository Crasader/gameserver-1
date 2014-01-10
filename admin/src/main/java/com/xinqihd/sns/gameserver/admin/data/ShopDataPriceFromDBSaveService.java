package com.xinqihd.sns.gameserver.admin.data;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
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
import com.xinqihd.sns.gameserver.db.mongo.ShopManager;

/**
 * 这个类比'ShopDataPriceSaveService'更加安全，它从数据库中读取已经存在的商品数据，修改
 * 价格，并保存，对于数据库中没有的记录不会添加。也不会修改除价格外的其他字段。
 * 
 * @author wangqi
 *
 */
public class ShopDataPriceFromDBSaveService extends SwingWorker<Void, Integer> {
	
	private int count = 0;
	private JDialog dialog = null;
	private JProgressBar progressBar = new JProgressBar();;
	private JXPanel panel = null;
	private JXLabel label = new JXLabel("正在更新装备的商品数据");
	private Stage stage = Stage.INIT;
	private ShopDataPriceResultModel model = null;
	private String database = ConfigManager.getConfigAsString(ConfigKey.mongoConfigDBName);
	private String namespace = ConfigManager.getConfigAsString(ConfigKey.mongoConfigNamespace);
	private String collection = "shops_new";
	private int simpleTimes = 0;
	private int normalTimes = 0;
	private int solidTimes = 0;
	private int eternalTimes = 0;
	
	public ShopDataPriceFromDBSaveService(ShopDataPriceResultModel model) {
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
			ArrayList<ShopPojo> modifiedShops = new ArrayList<ShopPojo>(count);

			for ( int i=0; i<count; i++ ) {
				ShopDataPricePrintResult result = (ShopDataPricePrintResult)model.
						getRowObject(i);
				WeaponPojo weapon = result.getWeaponPojo();
				Collection<ShopPojo> existShops = ShopManager.getInstance().getShopsByPropInfoId(weapon.getId());
				if ( existShops != null ) {
					for ( ShopPojo shop : existShops ) {
						switch ( shop.getMoneyType() ) {
							case GOLDEN:
								List<BuyPrice> prices = shop.getBuyPrices();
								for ( BuyPrice price : prices ) {
									if (price.validTimes == simpleTimes ) {
										price.price = result.getGoldenPrice();
									} else if (price.validTimes == normalTimes ) {
										price.price = result.getNormalGoldPrice();
									} else if (price.validTimes == solidTimes ) {
										price.price = result.getSolidGoldPrice();
									} else if (price.validTimes == eternalTimes ) {
										price.price = result.getEternalGoldPrice();
									}
								}
								break;
							case YUANBAO:
								prices = shop.getBuyPrices();
								for ( BuyPrice price : prices ) {
									if (price.validTimes == simpleTimes ) {
										price.price = result.getYuanbaoPrice();
									} else if (price.validTimes == normalTimes ) {
										price.price = result.getNormalYuanbaoPrice();
									} else if (price.validTimes == solidTimes ) {
										price.price = result.getSolidYuanbaoPrice();
									} else if (price.validTimes == eternalTimes ) {
										price.price = result.getEternalYuanbaoPrice();
									}
								}
								break;
						}
						modifiedShops.add(shop);
					}
				}
			}
			
			int i=0;
			for ( ShopPojo shop : modifiedShops ) {
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
