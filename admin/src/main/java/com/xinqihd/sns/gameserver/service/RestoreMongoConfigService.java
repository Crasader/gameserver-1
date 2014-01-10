package com.xinqihd.sns.gameserver.service;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.util.WindowUtils;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.xinqihd.sns.gameserver.admin.config.ConfigKey;
import com.xinqihd.sns.gameserver.admin.config.ConfigManager;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;

/**
 * 恢复所有的配置数据库
 * @author wangqi
 *
 */
public class RestoreMongoConfigService extends SwingWorker<Void, Integer> {
	
	private JDialog dialog = null;
	private JProgressBar progressBar = new JProgressBar();;
	private JXPanel panel = null;
	private JXLabel label = new JXLabel("正在读取数据");
	private Stage stage = Stage.INIT;
	private int totalCount = 0;
	
	private String database = ConfigManager.getConfigAsString(ConfigKey.mongoConfigDBName);
	private String namespace = ConfigManager.getConfigAsString(ConfigKey.mongoConfigNamespace);
	private File backupDir = null;
	private boolean cleanOldData = false;
	private String[] backupCollections = null;
	
	public RestoreMongoConfigService(File backupDir, boolean clean, String[] backupCollections) {
		this.backupDir = backupDir;
		this.backupDir.getParentFile().mkdirs();
		this.cleanOldData = clean;
		
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
		HashMap<String,ArrayList<String>> contentMap = new HashMap<String, ArrayList<String>>();
		
		for ( String collection : this.backupCollections ) {
			ArrayList<String> contentList = new ArrayList<String>();
			File file = new File(backupDir, collection.concat(".json") );
			if ( !file.exists() ) {
				JOptionPane.showMessageDialog(this.dialog, "缺少备份文件:"+file.getAbsolutePath()+", 无法恢复，请重新备份");
				return null;
			}
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			while ( line != null ) {
				contentList.add(line);
				line = br.readLine();
			}
			contentMap.put(collection, contentList);
			totalCount += contentList.size();
		}
		
		stage = Stage.READ_DATA;
		publish();
		progressBar.setMaximum(totalCount*2);
		
		HashMap<String, List<DBObject>> objMap = new HashMap<String, List<DBObject>>();
		int counter = 0;
		for ( String collection : this.backupCollections ) {
			List<String>  contentList = contentMap.get(collection);
			List<DBObject> objList = new ArrayList<DBObject>();
			for ( String line : contentList ) {
				DBObject obj = (DBObject)JSON.parse(line);
				objList.add(obj);
			}
			objMap.put(collection, objList);
		}
		
		stage = Stage.SAVE_DATA;
		publish();
		
		for ( String collection : this.backupCollections ) {
			if ( this.cleanOldData ) {
				MongoUtil.removeDocument(database, namespace, collection, null);
			}
			List<DBObject> objList = objMap.get(collection);
			for ( int i=0; i<objList.size(); i++ ) {
				DBObject obj = objList.get(i);
				try {
					Object id = obj.get("_id");
					DBObject query = MongoUtil.createDBObject("_id", id);
					try {
						MongoUtil.saveToMongo(query, obj, database, namespace, collection, true);
						publish(counter++);
					} catch (Exception e) {
						e.printStackTrace();
					}
					publish(counter++);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
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
			int value = 0;
			if ( chunks != null && chunks.size()>0 ) {
				value = chunks.get(chunks.size()-1);
			}
			if ( stage == Stage.READ_DATA ) {
				label.setText("正在读取磁盘备份数据...");
				progressBar.setValue(value);
			} else if ( stage == Stage.SAVE_DATA ) {
				label.setText("正在保存数据到数据库...");
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
