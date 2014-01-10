package com.xinqihd.sns.gameserver.service;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.util.WindowUtils;

import com.mongodb.CommandResult;
import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;

/**
 * 程序启动前检查图标文件并且从网上预下载所有图标到本地缓冲目录
 * @author wangqi
 *
 */
public class CopyMongoCollectionService extends SwingWorker<Void, Integer> {
	
	private JDialog dialog = null;
	private JProgressBar progressBar = new JProgressBar();;
	private JXPanel panel = null;
	private JXLabel label = new JXLabel("正在读取数据");
	private Stage stage = Stage.INIT;
	private int totalCount = 0;
	
	private String sourceDatabase;
	private String sourceNamespace;
	private String sourceCollection; 
	private String targetDatabase;
	private String targetNamespace;
	private String targetCollection;
	
	private static String code = 
			"function(sourceDatabase, sourceNamespace, sourceCollection, targetDatabase, targetNamespace, targetCollection) { \n"+
			" var currentdb = db.getSisterDB(sourceDatabase); \n"+
			" var gamedb = db.getSisterDB(targetDatabase); \n"+
			" currentdb.getCollection(sourceNamespace+\".\"+sourceCollection).find().forEach(function(x){gamedb.getCollection(targetNamespace+\".\"+targetCollection).insert(x)}); \n"+
			" return sourceNamespace;\n"+
			"}";
	
	public CopyMongoCollectionService(
			String sourceDatabase, String sourceNamespace, String sourceCollection, 
			String targetDatabase, String targetNamespace, String targetCollection) {
		this.sourceDatabase = sourceDatabase; 
		this.sourceNamespace = sourceNamespace;
		this.sourceCollection = sourceCollection; 
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
		DBObject query = MongoUtil.createDBObject();
		DBObject field = MongoUtil.createDBObject("_id", "1");
		List<DBObject> targetList = MongoUtil.queryAllFromMongo(query, targetDatabase, targetNamespace, targetCollection, null);
		if ( targetList.size() > 0 ) {
			int option = JOptionPane.showConfirmDialog(this.dialog, "是否清除原有工作区的内容?");
			if ( option != JOptionPane.YES_OPTION ) {
				return null;
			}
		}
		List<DBObject> idList = MongoUtil.queryAllFromMongo(query, sourceDatabase, sourceNamespace, sourceCollection, field);
		totalCount = idList.size();
		
		stage = Stage.READ_DATA;
		publish();
		progressBar.setMaximum(totalCount);
		List<DBObject> list = new ArrayList(totalCount); 
		for ( int i=0; i<idList.size(); i++ ) {
			DBObject obj = idList.get(i);
			Object id = obj.get("_id");
			query = MongoUtil.createDBObject("_id", id);
			try {
				DBObject value = MongoUtil.queryFromMongo(query, sourceDatabase, sourceNamespace, sourceCollection, null);
				list.add(value);
				publish(i);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		stage = Stage.SAVE_DATA;
		publish();
		MongoUtil.removeDocument(targetDatabase, targetNamespace, targetCollection, null);
		/*
		for ( int i=0; i<list.size(); i++ ) {
			DBObject obj = list.get(i);
			try {
//				Object id = obj.get("_id");
//				if ( id != null ) {
//					query = MongoUtil.createDBObject("_id", id);
//				}
				MongoUtil.saveToMongo(query, obj, targetDatabase, targetNamespace, targetCollection, true);
				publish(i);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		*/
		CommandResult result = MongoUtil.doEval(
				sourceDatabase, sourceNamespace, sourceCollection, code, 
				new Object[]{sourceDatabase, sourceNamespace, sourceCollection, 
				targetDatabase, targetNamespace, targetCollection});
		System.out.println(result);
		
		query = MongoUtil.createDBObject();
		field = MongoUtil.createDBObject("_id", "1");
		int count = (int)MongoUtil.countQueryResult(query, targetDatabase, targetNamespace, targetCollection);
		if ( count > 0 ) {
			if ( totalCount > count ) {
				JOptionPane.showMessageDialog(this.dialog, "警告，原有"+totalCount+"条数据，保存后为"+count+"项数据");
			}
		} else {
			JOptionPane.showMessageDialog(this.dialog, "保存失败，请重试保存");
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
				label.setText("正在读取数据...");
				progressBar.setValue(value);
			} else if ( stage == Stage.SAVE_DATA ) {
				label.setText("正在保存数据...");
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
