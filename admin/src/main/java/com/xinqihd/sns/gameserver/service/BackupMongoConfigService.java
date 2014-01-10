package com.xinqihd.sns.gameserver.service;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
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
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;


/**
 * 备份所有的配置数据库
 * @author wangqi
 *
 */
public class BackupMongoConfigService extends SwingWorker<Void, Integer> {
		
	private JDialog dialog = null;
	private JProgressBar progressBar = new JProgressBar();;
	private JXPanel panel = null;
	private JXLabel label = new JXLabel("正在读取数据");
	private Stage stage = Stage.INIT;
	private int totalCount = 0;
	
	private String database = ConfigManager.getConfigAsString(ConfigKey.mongoConfigDBName);
	private String namespace = ConfigManager.getConfigAsString(ConfigKey.mongoConfigNamespace);
	
	private File backupDir = null;
	private String[] backupCollections = null;
	
	public BackupMongoConfigService(File backupDir, String[] backupCollections) {
		this.backupDir = backupDir;
		this.backupDir.getParentFile().mkdirs();
		
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
		
		this.backupCollections = backupCollections;
	}
	
	public JDialog getDialog() {
		return dialog;
	}
	 
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground() throws Exception {
		stage = Stage.INIT;
		publish();
		HashMap<String, List<DBObject>> idListMap = new HashMap<String, List<DBObject>>();
		
		DBObject query = MongoUtil.createDBObject();
		DBObject field = MongoUtil.createDBObject("_id", "1");
		for ( String collection : backupCollections ) {
			List<DBObject> idList = MongoUtil.queryAllFromMongo(query, database, namespace, collection, field);
			idListMap.put(collection, idList);
		}
		
		stage = Stage.READ_DATA;
		publish();
		totalCount = backupCollections.length * 2;
		progressBar.setMaximum(totalCount*2);
		HashMap<String, List<DBObject>> objMap = new HashMap<String, List<DBObject>>();
		int counter = 0;
		for ( String collection : idListMap.keySet() ) {
			List<DBObject> idList = idListMap.get(collection);
			List<DBObject> objList = new ArrayList<DBObject>();
			for ( int i=0; i<idList.size(); i++ ) {
				DBObject obj = idList.get(i);
				Object id = obj.get("_id");
				query = MongoUtil.createDBObject("_id", id);
				try {
					DBObject value = MongoUtil.queryFromMongo(query, database, namespace, collection, null);
					objList.add(value);
					publish(counter++);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			objMap.put(collection, objList);
		}
		
		stage = Stage.SAVE_DATA;
		publish();
		
		for ( String collection : objMap.keySet() ) {
			List<DBObject> objList = objMap.get(collection);
			File backupFile = new File(backupDir, collection.concat(".json"));
			FileWriter fw = new FileWriter(backupFile);
			BufferedWriter bw = new BufferedWriter(fw);
			for ( int i=0; i<objList.size(); i++ ) {
				DBObject obj = objList.get(i);
				try {
					//TODO save data
					bw.append(obj.toString());
					bw.append('\n');
					publish(counter++);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			bw.close();
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#process(java.util.List)
	 */
	@Override
	protected void process(List<Integer> chunks) {
		if ( stage == Stage.INIT ) {
//			dialog.setVisible(true);
			progressBar.setIndeterminate(true);
		} else {
			progressBar.setIndeterminate(false);
			int value = 0;
			if ( chunks != null && chunks.size()>0 ) {
				value = chunks.get(chunks.size()-1);
			}
			if ( stage == Stage.READ_DATA ) {
				label.setText("正在读取数据库的配置数据...");
				progressBar.setValue(value);
			} else if ( stage == Stage.SAVE_DATA ) {
				label.setText("正在保存配置数据到磁盘...");
				progressBar.setValue(totalCount+value);
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
		this.dialog.dispose();
//		JOptionPane.showMessageDialog(null, "数据同步成功!");
	}

	static enum Stage {
		INIT,
		READ_DATA,
		SAVE_DATA,
	}
}
