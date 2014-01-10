package com.xinqihd.sns.gameserver.admin.gui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.action.game.MyTableModelDeleteRowAction;
import com.xinqihd.sns.gameserver.admin.config.ConfigKey;
import com.xinqihd.sns.gameserver.admin.config.ConfigManager;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellEditorFactory;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellRenderFactory;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTablePanel;
import com.xinqihd.sns.gameserver.admin.gui.table.EquipAndItemIdEditor;
import com.xinqihd.sns.gameserver.admin.gui.table.IconCellEditor;
import com.xinqihd.sns.gameserver.admin.gui.table.IconCellRenderer;
import com.xinqihd.sns.gameserver.admin.gui.table.MongoDBObjectEditor;
import com.xinqihd.sns.gameserver.admin.item.AddOrEditRewardAction;
import com.xinqihd.sns.gameserver.admin.item.EditItemDialog;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.db.mongo.MapDBObject;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;
import com.xinqihd.sns.gameserver.service.RefreshMyTableService;

/**
 * It implements the GameDataPanel
 * @author wangqi
 *
 */
public class TaskLevelPanel extends AbstractTaskPanel {
	
	private static TaskLevelPanel instance = new TaskLevelPanel(); 

	private final String database = ConfigManager.getConfigAsString(ConfigKey.mongoConfigDBName); 
	private final String namespace = ConfigManager.getConfigAsString(ConfigKey.mongoConfigNamespace);
	private final String collection = "levels";
	
	public TaskLevelPanel() {
		init();
	}
	
	public static TaskLevelPanel getInstance() {
		return instance;
	}

	public void init() {
		//创建工作区域
		super.initModel(collection);
		
		myTable.getTable().setColumnOrders(new String[]{"_id", "level", "exp", "dpr", "blood", "skin"});
		
		hiddenFields.add("class");
		
		model.setHiddenFields(hiddenFields);
		
		RefreshMyTableService service = new RefreshMyTableService(model);
		service.execute();
		
		myTable.setTitle("游戏等级数据");
		myTable.setTableModel(model);
//		myTable.setAddRowAction(new AddItemPojoAction());
		myTable.setDelRowAction(new MyTableModelDeleteRowAction(myTable.getTable()));
		myTable.setEditable(true);
		myTable.setBackupAction(backupAction);
//		myTable.setColumnEditor(3, new GameDataEditor());
		this.add(myTable, "width 100%, height 100%");
	}
	
	/**
	 * 
	 * @param selectedItemPojo
	 */
	private void saveItemPojo(ItemPojo selectedItemPojo, int modelRowIndex) {
		if ( selectedItemPojo != null ) {
			MapDBObject dbObject = new MapDBObject();
			dbObject.putAll(selectedItemPojo);
			if ( modelRowIndex >= 0 ) {
				myTable.getTableModel().updateRow(dbObject, modelRowIndex);
			} else {
				myTable.getTableModel().insertRow(dbObject);
			}
		}
	}
	
	/**
	 * 道具管理界面中负责编辑各个字段的工厂实现
	 * @author wangqi
	 *
	 */
	public class LevelEditorRenderFactory implements MyTableCellEditorFactory, 
		MyTableCellRenderFactory {
		
		private MyTablePanel myTable = null;
		
		public LevelEditorRenderFactory(MyTablePanel myTable) {
			this.myTable = myTable;
		}

		/* (non-Javadoc)
		 * @see com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellRenderFactory#getCellRenderer(int, int, java.lang.String)
		 */
		@Override
		public TableCellRenderer getCellRenderer(int row, int column,
				String columnName, TableModel tableModel, JTable table) {
			return null;
		}

		/* (non-Javadoc)
		 * @see com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellEditorFactory#getCellEditor(int, int, java.lang.String)
		 */
		@Override
		public TableCellEditor getCellEditor(int row, int column, 
				String columnName, TableModel tableModel, JTable table) {
			return null;
		}
		
	}
}
