package com.xinqihd.sns.gameserver.admin.gui;

import java.util.HashSet;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;

import com.xinqihd.sns.gameserver.admin.action.CopyMongoCollectionAction;
import com.xinqihd.sns.gameserver.admin.config.ConfigKey;
import com.xinqihd.sns.gameserver.admin.config.ConfigManager;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyPanel;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTablePanel;
import com.xinqihd.sns.gameserver.admin.model.GeneralMongoModel;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

/**
 * 为所有的基于MyTable的任务面板创建一个超类，封装通用行为
 * @author wangqi
 *
 */
public class AbstractTaskPanel extends MyPanel {
	
	protected GeneralMongoModel model = null;
	protected MyTablePanel myTable = new MyTablePanel();
	protected HashSet<String> hiddenFields = new HashSet<String>();
	
	protected String targetDatabase = MainFrame.MONGO_WORKSPACE_DB;
	protected String targetNamespace = MainFrame.MONGO_WORKSPACE_NS;
	protected String targetCollection = null;
	
	protected CopyMongoCollectionAction backupAction = null;
	protected CopyMongoCollectionAction saveAction   = null;

	public void initModel(String collection) {
		//创建工作区域
		myTable.setEditable(false);
		String sourceDatabase = ConfigManager.getConfigAsString(ConfigKey.mongoConfigDBName); 
		String sourceNamespace = ConfigManager.getConfigAsString(ConfigKey.mongoConfigNamespace);
		String sourceCollection = collection;
		targetCollection = MainFrame.loginUserName+"_"+collection;
		
		backupAction = new CopyMongoCollectionAction(
				sourceDatabase, sourceNamespace, sourceCollection, 
				targetDatabase, targetNamespace, targetCollection);
		saveAction = new CopyMongoCollectionAction(
				targetDatabase, targetNamespace, targetCollection,
				sourceDatabase, sourceNamespace, sourceCollection
				);
		saveAction.setIcon(ImageUtil.createImageSmallIcon("Folder.png", "保存设置"));
//		int option = JOptionPane.showConfirmDialog(this, "需要创建工作区才能编辑该表，是否继续?");
//		if ( option == JOptionPane.YES_OPTION ) {
			backupAction.actionPerformed(null);
			myTable.setEditable(true);
			myTable.setEnableSaveButton(true);
			myTable.setSaveButtonAction(saveAction);
//		} else {
//			myTable.setEditable(false);
//			myTable.getTable().setCellSelectionEnabled(false);
//			myTable.getTable().setRowSelectionAllowed(true);
//		}
		model = new GeneralMongoModel(targetDatabase, targetNamespace, targetCollection);
	}
	
	public Action getSaveAction() {
		return this.saveAction;
	}
}
