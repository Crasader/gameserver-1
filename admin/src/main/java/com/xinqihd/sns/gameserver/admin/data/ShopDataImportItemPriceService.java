package com.xinqihd.sns.gameserver.admin.data;

import java.awt.Point;
import java.util.HashMap;
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
import com.xinqihd.sns.gameserver.admin.data.CraftStoneResult;
import com.xinqihd.sns.gameserver.admin.gui.MainFrame;
import com.xinqihd.sns.gameserver.config.ShopPojo;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;

/**
 * 生成模拟的玩家数据，并进行战斗测试
 * @author wangqi
 *
 */
public class ShopDataImportItemPriceService extends SwingWorker<Void, Integer> {
	
	private int count = 0;
	private JDialog dialog = null;
	private JProgressBar progressBar = new JProgressBar();;
	private JXPanel panel = null;
	private JXLabel label = new JXLabel("正在导入道具商品的价格");
	private Stage stage = Stage.INIT;
	private String database = ConfigManager.getConfigAsString(ConfigKey.mongoConfigDBName);
	private String namespace = ConfigManager.getConfigAsString(ConfigKey.mongoConfigNamespace);
	private String shopsNew = "shops_new";
	private String shops = "shops";

	
	public ShopDataImportItemPriceService() {
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
		this.progressBar.setIndeterminate(true);
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground() throws Exception {
		try {
			stage = Stage.INIT;
			DBObject query = MongoUtil.createDBObject("isItem", Boolean.TRUE);
			List<DBObject> list = MongoUtil.queryAllFromMongo(
					query, database, namespace, shops, null);
			//initialize
			this.count = list.size();
			publish();
			Thread.sleep(100);
			
			stage = Stage.DO_BALANCE_TEST;
			int i=0;
			for ( DBObject obj : list ) {
				query = MongoUtil.createDBObject("_id", obj.get("_id"));
				MongoUtil.saveToMongo(query, 
						obj, database, namespace, shopsNew, true);
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
		} else if ( stage == Stage.DO_BALANCE_TEST ) {
			label.setText("正在导入道具商品的价格...");
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
		DO_BALANCE_TEST,
	}

}
