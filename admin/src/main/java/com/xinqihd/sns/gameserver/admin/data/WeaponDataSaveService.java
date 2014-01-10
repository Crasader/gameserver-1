package com.xinqihd.sns.gameserver.admin.data;

import java.awt.Point;
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
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.MapDBObject;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;

/**
 * 程序启动前检查图标文件并且从网上预下载所有图标到本地缓冲目录
 * @author wangqi
 *
 */
public class WeaponDataSaveService extends SwingWorker<Void, Integer> {
	
	private JDialog dialog = null;
	private JProgressBar progressBar = new JProgressBar();;
	private JXPanel panel = null;
	private JXLabel label = new JXLabel("正在读取数据");
	private Stage stage = Stage.INIT;
	private int totalCount = 0;
	
	private String targetDatabase;
	private String targetNamespace;
	private String targetCollection;
	private Collection<WeaponPojo> weapons;
	
	public WeaponDataSaveService(
			Collection<WeaponPojo> weapons, 
			String targetDatabase, String targetNamespace, String targetCollection) {
		this.weapons = weapons;
		this.targetDatabase = targetDatabase;
		this.targetNamespace = targetNamespace;
		this.targetCollection = targetCollection;
		
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
		totalCount = this.weapons.size(); 
				
		stage = Stage.READ_DATA;
		publish();
		
		stage = Stage.SAVE_DATA;
		publish();
		MongoUtil.dropCollection(targetDatabase, targetNamespace, targetCollection);
		
		int i=0;
		for ( WeaponPojo weapon : this.weapons ) {
			try {
//				Object id = obj.get("_id");
//				if ( id != null ) {
//					query = MongoUtil.createDBObject("_id", id);
//				}
//				DBObject query = MongoUtil.createDBObject();
				MapDBObject obj = MongoUtil.createMapDBObject();
				obj.putAll(weapon);
				MongoUtil.saveToMongo(obj, obj, targetDatabase, targetNamespace, targetCollection, true);
				publish(i++);
			} catch (Exception e) {
				e.printStackTrace();
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
			progressBar.setMinimum(0);
			progressBar.setMaximum(totalCount*2);
			progressBar.setStringPainted(true);
		} else {
			int value = 0;
			if ( chunks != null && chunks.size()>0 ) {
				value = chunks.get(chunks.size()-1);
			}
			if ( stage == Stage.READ_DATA ) {
				label.setText("正在装备数据...");
				progressBar.setValue(value);
			} else if ( stage == Stage.SAVE_DATA ) {
				label.setText("正在保存装备数据...");
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
