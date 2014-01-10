package com.xinqihd.sns.gameserver.service;

import java.awt.Point;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
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
import com.xinqihd.sns.gameserver.admin.user.UserManageAction;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;
import com.xinqihd.sns.gameserver.util.IOUtil;

/**
 * 程序启动前检查图标文件并且从网上预下载所有图标到本地缓冲目录
 * @author wangqi
 *
 */
public class DownloadIconsService extends SwingWorker<Void, Integer> {
	
	private JDialog dialog = new JDialog();
	private JProgressBar progressBar = new JProgressBar();;
	private JXPanel panel = null;
	private JXLabel label = new JXLabel("下载游戏图标资源");
	private ArrayList<String> iconNames = new ArrayList<String>();
	private Stage stage = Stage.GET_NUM_OF_ICONS;
	private List<String> icons = new ArrayList<String>(500);
	
	public DownloadIconsService() {
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground() throws Exception {
		Thread t = new Thread(new Runnable(){
			public void run() {
				EquipManager.getInstance();
				ItemManager.getInstance();				
			}
		});
		t.start();
		publish();
		String database = ConfigManager.getConfigAsString(ConfigKey.mongoConfigDBName);
		String namespace = ConfigManager.getConfigAsString(ConfigKey.mongoConfigNamespace);
		String iconColl = "icons";
//		String itemColl = "items";
		/*
		DBObject query = MongoUtil.createDBObject();
		List<DBObject> list = MongoUtil.queryAllFromMongo(query, database, namespace, iconColl, null);
		for ( DBObject obj : list ) {
			icons.add(obj.get("_id").toString());
		}
		*/
		/*
		DBObject query = MongoUtil.createDBObject();
		DBObject fields = MongoUtil.createDBObject("icon", "1");
		List<DBObject> list = MongoUtil.queryAllFromMongo(query, database, namespace, equipColl, fields);
		for ( DBObject dbObj : list ) {
			icons.add(dbObj.get("icon").toString());
		}
		list = MongoUtil.queryAllFromMongo(query, database, namespace, itemColl, fields);
		for ( DBObject dbObj : list ) {
			icons.add(dbObj.get("icon").toString());
		}
		*/
		stage = Stage.GOT_NUM_OF_ICONS;
		publish();
		stage = Stage.DOWNLOAD_ICONS;
		for ( int i=0; i<icons.size(); i++ ) {
			String iconName = icons.get(i);
			System.out.println("--"+iconName);
			/*
			if ( !MainFrame.ICON_MAPS.containsKey(iconName) ) {
				try {
					File file = new File(ImageUtil.TMP_ASSETS_ICONS_FILE, iconName.concat(".png"));
					if ( !file.exists() || file.length() <= 0 ) {
						URL url = new URL(ImageUtil.ASSETS_ICONS_DIR + file.getName());
						InputStream is = url.openStream();
						IOUtil.writeStreamToFile(is, file.getAbsolutePath());
					}
					MainFrame.ICON_MAPS.put(iconName, ImageUtil.createImageIconFromAssets(file.getName(), 24));
					publish(i);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			*/
		}
		stage = Stage.INITIAL_EQUIPS;
		publish();
		t.join();
		return null;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#process(java.util.List)
	 */
	@Override
	protected void process(List<Integer> chunks) {
		if ( stage == Stage.GET_NUM_OF_ICONS ) {
			panel = new JXPanel();
			panel.setLayout(new MigLayout("wrap 1"));
			panel.add(label, "growx, wrap 20");
			panel.add(progressBar, "grow, push");
			
			dialog = new JDialog();
			dialog.add(panel);
			dialog.setSize(300, 120);
			Point p = WindowUtils.getPointForCentering(dialog);
			dialog.setLocation(p);
			dialog.setModal(true);
			dialog.setResizable(false);
			dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			dialog.setVisible(true);
			
			label.setText("正在获取图标列表...");
			label.setFont(MainFrame.BIG_FONT);
			progressBar.setIndeterminate(true);
		} else if ( stage == Stage.GOT_NUM_OF_ICONS ) {
			label.setFont(MainFrame.BIG_FONT);
			label.setText("总图标数量: " + icons.size());
			label.repaint();
			progressBar.setIndeterminate(false);
			progressBar.setMinimum(0);
			progressBar.setMaximum(icons.size());
			progressBar.setStringPainted(true);
		} else if ( stage == Stage.DOWNLOAD_ICONS ) {
			if ( chunks != null && chunks.size()>0 ) {
				int percent = chunks.get(chunks.size()-1);
				progressBar.setValue(percent);
			}
		} else if ( stage == Stage.INITIAL_EQUIPS ) {
			label.setText("正在初始化装备数据...");
			progressBar.setIndeterminate(true);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
		this.dialog.dispose();
		Action action = new UserManageAction();
		action.actionPerformed(null);
	}

	static enum Stage {
		GET_NUM_OF_ICONS,
		GOT_NUM_OF_ICONS,
		DOWNLOAD_ICONS,
		INITIAL_EQUIPS,
	}
}
