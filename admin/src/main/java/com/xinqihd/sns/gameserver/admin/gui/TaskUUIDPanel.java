package com.xinqihd.sns.gameserver.admin.gui;

import java.util.HashSet;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.action.CopyMongoCollectionAction;
import com.xinqihd.sns.gameserver.admin.action.game.MyTableModelDeleteRowAction;
import com.xinqihd.sns.gameserver.admin.config.ConfigKey;
import com.xinqihd.sns.gameserver.admin.config.ConfigManager;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyPanel;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellEditorFactory;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellRenderFactory;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTablePanel;
import com.xinqihd.sns.gameserver.admin.gui.table.MongoDBArrayEditor;
import com.xinqihd.sns.gameserver.admin.gui.table.MongoDBObjectEditor;
import com.xinqihd.sns.gameserver.admin.gui.table.ObjectArrayListEditor;
import com.xinqihd.sns.gameserver.admin.item.AddOrEditRewardAction;
import com.xinqihd.sns.gameserver.admin.model.GeneralMongoModel;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;
import com.xinqihd.sns.gameserver.config.MoneyType;
import com.xinqihd.sns.gameserver.service.RefreshMyTableService;

/**
 * It implements the GameDataPanel
 * 
 * @author wangqi
 * 
 */
public class TaskUUIDPanel extends AbstractTaskPanel {

	private static TaskUUIDPanel instance = new TaskUUIDPanel();

	public TaskUUIDPanel() {
		init();
	}

	public static TaskUUIDPanel getInstance() {
		return instance;
	}
	
	public void initModel(String collection) {
		//创建工作区域
		myTable.setEditable(false);
		String sourceDatabase = ConfigManager.getConfigAsString(ConfigKey.mongoDBName); 
		String sourceNamespace = ConfigManager.getConfigAsString(ConfigKey.mongoNamespace);
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

	public void init() {
		// 创建工作区域
		targetDatabase = "babywar";
		String collection = "uuids";
		initModel(collection);

		hiddenFields.add("class");

		model.setHiddenFields(hiddenFields);

		RefreshMyTableService service = new RefreshMyTableService(model);
		service.execute();

		myTable.setTitle("手机硬件屏蔽数据");
		myTable.setTableModel(model);
		myTable.setDelRowAction(new MyTableModelDeleteRowAction(myTable.getTable()));
		ShopEditorRenderFactory factory = new ShopEditorRenderFactory(this.myTable);
		myTable.getTable().setRenderFactory(factory);
		myTable.getTable().setEditorFactory(factory);
		// myTable.setColumnEditor(3, new GameDataEditor());
		myTable.setBackupAction(backupAction);
		this.add(myTable, "width 100%, height 100%");
	}

	static class ShopEditorRenderFactory implements MyTableCellEditorFactory,
			MyTableCellRenderFactory {

		private MyTablePanel myTable = null;

		public ShopEditorRenderFactory(MyTablePanel myTable) {
			this.myTable = myTable;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellRenderFactory#
		 * getCellRenderer(int, int, java.lang.String)
		 */
		@Override
		public TableCellRenderer getCellRenderer(int row, int column,
				String columnName, TableModel tableModel, JTable table) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellEditorFactory#
		 * getCellEditor(int, int, java.lang.String)
		 */
		@Override
		public TableCellEditor getCellEditor(int row, int column,
				String columnName, TableModel tableModel, JTable table) {
			return null;
		}

	}
}
